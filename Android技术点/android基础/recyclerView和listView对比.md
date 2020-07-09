[Toc]

### RecyclerView的缓存原理

RecyclerView的复用启动，取决于`LayoutManager`。不同的`LayoutManager`在`onLayoutChildren`中有不同的实现，但它们都一定会调用一个方法。那就是`getViewForPosition`，所以，我们就从`getViewForPosition`开始讲起。

```java
        /**
         * Obtain a view initialized for the given position.
         *
         * This method should be used by {@link LayoutManager} implementations to obtain
         * views to represent data from an {@link Adapter}.
         * <p>
         * The Recycler may reuse a scrap or detached view from a shared pool if one is
         * available for the correct view type. If the adapter has not indicated that the
         * data at the given position has changed, the Recycler will attempt to hand back
         * a scrap view that was previously initialized for that data without rebinding.
         *
         * @param position Position to obtain a view for
         * @return A view representing the data at <code>position</code> from <code>adapter</code>
         */
        @NonNull
        public View getViewForPosition(int position) {
            return getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            return tryGetViewHolderForPositionByDeadline(position, dryRun, FOREVER_NS).itemView;
        }
```

这个方法就是根据position去返回View的，根据不同情况，可能从**share pool**里面取，可能从**scrap view**中取，总而言之，就是尽量低成本地去获取一个可用的View。

#### mAttachedScrap & mChangedScrap

```java
        ViewHolder tryGetViewHolderForPositionByDeadline(int position,
                boolean dryRun, long deadlineNs) {
            ...
            boolean fromScrapOrHiddenOrCache = false;
            ViewHolder holder = null;
            // 0) If there is a changed scrap, try to find from there
            if (mState.isPreLayout()) {
                holder = getChangedScrapViewForPosition(position);
                fromScrapOrHiddenOrCache = holder != null;
            }
            ...
```

这一层的代码很简单，就是从`mChangedScrap`中取ViewHolder。首先我们要明白，`mChangedScrap`里面放的是什么。

我们可以注意到这一层缓存的判断条件，isPrelayout。只有当我们需要**用动画改变屏幕上已有ViewHolder**时，会通过这个条件。并且此时在真正发生改变之前。

mChangedScrap 表示的是数据已经改变的但还在屏幕中的ViewHolder列表。所以在改动之前，我们会从中获取ViewHolder。

```java
ViewHolder tryGetViewHolderForPositionByDeadline(int position,
                boolean dryRun, long deadlineNs) {
            boolean fromScrapOrHiddenOrCache = false;
            ViewHolder holder = null;
            ...
            // 1) 根据位置在mAttachedScrap和mCachedViews中查找，如果找到直接使用
            if (holder == null) {
                holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
                if (holder != null) {
                    if (!validateViewHolderForOffsetPosition(holder)) {
                        if (!dryRun) {
                            holder.addFlags(ViewHolder.FLAG_INVALID);
                            if (holder.isScrap()) {
                                removeDetachedView(holder.itemView, false);
                                holder.unScrap();
                            } else if (holder.wasReturnedFromScrap()) {
                                holder.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(holder);
                        }
                        holder = null;
                    } else {
                        fromScrapOrHiddenOrCache = true;
                    }
                }
            }
            ...省略部分代码
          if (mAdapter.hasStableIds()) {
                //根据id查找，在mAttachedScrap和mCachedViews中查找
               holder = getScrapOrCachedViewForId(mAdapter.getItemId(offsetPosition),
                            type, dryRun);
              if (holder != null) {
                   //找到后更新位置
                   holder.mPosition = offsetPosition;
                   fromScrapOrHiddenOrCache = true;
              }
           }
           if (holder == null && mViewCacheExtension != null) {
               //尝试从mViewCacheExtension中获取View并创建ViewHolder
              final View view = mViewCacheExtension
                            .getViewForPositionAndType(this, position, type);
              if (view != null) {
                        holder = getChildViewHolder(view);                       
              }
           }
        if (holder == null) { //从回收池查找
              holder = getRecycledViewPool().getRecycledView(type);
             if (holder != null) {
                        holder.resetInternal();
                        if (FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(holder);
                        }
              }
        }
       if (holder == null) {
                    long start = getNanoTime();
                    if (deadlineNs != FOREVER_NS
                            && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) 		            {
                        // 如果超时
                        return null;
                    }
                    //重新创建holder
                    holder = mAdapter.createViewHolder(RecyclerView.this, type);
                    if (ALLOW_THREAD_GAP_WORK) {
                        // only bother finding nested RV if prefetching
                        RecyclerView innerView = findNestedRecyclerView(holder.itemView);
                        if (innerView != null) {
                            holder.mNestedRecyclerView = new WeakReference<>(innerView);
                        }
                    }
                    long end = getNanoTime();
                    mRecyclerPool.factorInCreateTime(type, end - start);
       }
}             
```

我们可以看到这个方法中，我们会从`mAttachedScrap`寻找合适的ViewHolder。

mAttachedScrap 表示屏幕内未与RecyclerView分离的ViewHolder列表。值得注意的是`mAttachedScrap`是不限制大小的。想一想也很容易明白，屏幕中显示多少ViewHolder，是无法确定的。并且ViewHolder既然都已经显示了，`mAttachedScrap`也不会造成额外的内存占用。

通常我们把`mChangedScrap`和`mAttachedScrap`称为RecyclerView的第一级缓存，它们的共同特点就是，只缓存屏幕上的View，且没有大小限制。

#### mCachedViews

`mCachedViews`是RecyclerView第二层缓存。

当列表滑动出了屏幕时，ViewHolder会被缓存在 mCachedViews ，其大小由mViewCacheMax决定，默认DEFAULT_CACHE_SIZE为2，可通过Recyclerview.setItemViewCacheSize()动态设置。

我们来看一下从`mCachedViews`中获取ViewHolder的代码：

```java
            // Search in our first-level recycled view cache.
            final int cacheSize = mCachedViews.size();
            for (int i = 0; i < cacheSize; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                // invalid view holders may be in cache if adapter has stable ids as they can be
                // retrieved via getScrapOrCachedViewForId
                if (!holder.isInvalid() && holder.getLayoutPosition() == position) {
                    if (!dryRun) {
                        mCachedViews.remove(i);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "getScrapOrHiddenOrCachedHolderForPosition(" + position
                                + ") found match in cache: " + holder);
                    }
                    return holder;
                }
            }
            return null;
```

值得注意的是，`holder.getLayoutPosition() == position`，所以我们在`mCachedViews`中存的ViewHolder，虽然是屏幕外的，但只能是对应position的。也就是说，只能是RecyclerView的ViewHolder被滑出屏幕后，再滑回来显示的情景。

也不难看出，从`mCachedViews`中，我们取得的ViewHolder是不需要重新绑定数据的。

那么，我们的ViewHolder是何时被加入`mCachedViews`中的呢？

```java
            if (forceRecycle || holder.isRecyclable()) {
                if (mViewCacheMax > 0
                        && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID
                        | ViewHolder.FLAG_REMOVED
                        | ViewHolder.FLAG_UPDATE
                        | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)) {
                    // Retire oldest cached view
                    int cachedViewSize = mCachedViews.size();
                    if (cachedViewSize >= mViewCacheMax && cachedViewSize > 0) {
                        recycleCachedViewAt(0);
                        cachedViewSize--;
                    }

                    int targetCacheIndex = cachedViewSize;
                    if (ALLOW_THREAD_GAP_WORK
                            && cachedViewSize > 0
                            && !mPrefetchRegistry.lastPrefetchIncludedPosition(holder.mPosition)) {
                        // when adding the view, skip past most recently prefetched views
                        int cacheIndex = cachedViewSize - 1;
                        while (cacheIndex >= 0) {
                            int cachedPos = mCachedViews.get(cacheIndex).mPosition;
                            if (!mPrefetchRegistry.lastPrefetchIncludedPosition(cachedPos)) {
                                break;
                            }
                            cacheIndex--;
                        }
                        targetCacheIndex = cacheIndex + 1;
                    }
                    mCachedViews.add(targetCacheIndex, holder);
                    cached = true;
                }
                if (!cached) {
                    addViewHolderToRecycledViewPool(holder, true);
                    recycled = true;
                }
            }
```

很容易看出，当我们将ViewHolder滑出屏幕时，我们会尝试回收ViewHolder，将其放入`mCachedViews`中。如果`mCachedViews`已满，我们会将其中的第0个拿出来，放到`mRecyclerPool`中。

`mRecyclerPool`与`mCachedViews`最大的不同是，从`mCachedViews`中取出的ViewHolder是不需要重新bind数据的。

我们可以通过以下方法来设置`mCacheViews`的最大值。



```java
        /**
         * Set the maximum number of detached, valid views we should retain for later use.
         *
         * @param viewCount Number of views to keep before sending views to the shared pool
         */
        public void setViewCacheSize(int viewCount) {
            mRequestedCacheMax = viewCount;
            updateViewCacheSize();
        }
```

很明显，这是一个空间换时间的设置项。我们增大mRequestedCacheMax，可以在展示已经展示过的ViewHolder时，减少bind的次数，但需要缓存更多的ViewHolder。

#### mViewCacheExtension

`mViewCacheExtension`是RecyclerView的第三层缓存。当我们在`mAttachedScrap & mChangedScrap`和`mCachedViews`中均未获得ViewHolder时，我们会尝试从`mViewCacheExtension`中获取View并创建ViewHolder。

```java
                if (holder == null && mViewCacheExtension != null) {
                    // We are NOT sending the offsetPosition because LayoutManager does not
                    // know it.
                    final View view = mViewCacheExtension
                            .getViewForPositionAndType(this, position, type);
                    if (view != null) {
                        holder = getChildViewHolder(view);
                        if (holder == null) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view which does not have a ViewHolder"
                                    + exceptionLabel());
                        } else if (holder.shouldIgnore()) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view that is ignored. You must call stopIgnoring before"
                                    + " returning this view." + exceptionLabel());
                        }
                    }
                }
```

我们可以看一下`ViewCacheExtension`的定义：

```java
    public abstract static class ViewCacheExtension {

        /**
         * Returns a View that can be binded to the given Adapter position.
         * <p>
         * This method should <b>not</b> create a new View. Instead, it is expected to return
         * an already created View that can be re-used for the given type and position.
         * If the View is marked as ignored, it should first call
         * {@link LayoutManager#stopIgnoringView(View)} before returning the View.
         * <p>
         * RecyclerView will re-bind the returned View to the position if necessary.
         *
         * @param recycler The Recycler that can be used to bind the View
         * @param position The adapter position
         * @param type     The type of the View, defined by adapter
         * @return A View that is bound to the given position or NULL if there is no View to re-use
         * @see LayoutManager#ignoreView(View)
         */
        @Nullable
        public abstract View getViewForPositionAndType(@NonNull Recycler recycler, int position,
                int type);
    }
```

这一层看起来很简单，就是RecyclerView为我们开发者在`mCachedViews`和`RecycledViewPool`中加了一层缓存。让我们可以通过position和type返回一个View。然后RecyclerView帮我们找到View对应的ViewHolder。这一层缓存的实现完全可以靠开发者的想象。

值得注意的是，这一层如果能成功获得ViewHolder，也是不会绑定数据的。所以这一次缓存，通常也用来获取不可变的ViewHolder。

#### RecycledViewPool

```java
    /**
     * RecycledViewPool lets you share Views between multiple RecyclerViews.
     * <p>
     * If you want to recycle views across RecyclerViews, create an instance of RecycledViewPool
     * and use {@link RecyclerView#setRecycledViewPool(RecycledViewPool)}.
     * <p>
     * RecyclerView automatically creates a pool for itself if you don't provide one.
     */
    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;

        /**
         * Tracks both pooled holders, as well as create/bind timing metadata for the given type.
         *
         * Note that this tracks running averages of create/bind time across all RecyclerViews
         * (and, indirectly, Adapters) that use this pool.
         *
         * 1) This enables us to track average create and bind times across multiple adapters. Even
         * though create (and especially bind) may behave differently for different Adapter
         * subclasses, sharing the pool is a strong signal that they'll perform similarly, per type.
         *
         * 2) If {@link #willBindInTime(int, long, long)} returns false for one view, it will return
         * false for all other views of its type for the same deadline. This prevents items
         * constructed by {@link GapWorker} prefetch from being bound to a lower priority prefetch.
         */
        static class ScrapData {
            final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
            int mMaxScrap = DEFAULT_MAX_SCRAP;
            long mCreateRunningAverageNs = 0;
            long mBindRunningAverageNs = 0;
        }
        SparseArray<ScrapData> mScrap = new SparseArray<>();
```

`RecycledViewPool`的结构非常清晰：

- `SparseArray<ScrapData> mScrap`中存放在viewType对应的`ScrapData`。
- `ScrapData`中，则是缓存的`ViewHolder`。

获取方法很简单：

```java
        /**
         * Acquire a ViewHolder of the specified type from the pool, or {@code null} if none are
         * present.
         *
         * @param viewType ViewHolder type.
         * @return ViewHolder of the specified type acquired from the pool, or {@code null} if none
         * are present.
         */
        @Nullable
        public ViewHolder getRecycledView(int viewType) {
            final ScrapData scrapData = mScrap.get(viewType);
            if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
                final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;
                return scrapHeap.remove(scrapHeap.size() - 1);
            }
            return null;
        }
```

从`mScrap`中找到对应ViewType的`ScrapData`，然后从队尾拿走一个。

插入方法稍微复杂一点：

```java
        /**
         * Add a scrap ViewHolder to the pool.
         * <p>
         * If the pool is already full for that ViewHolder's type, it will be immediately discarded.
         *
         * @param scrap ViewHolder to be added to the pool.
         */
        public void putRecycledView(ViewHolder scrap) {
            final int viewType = scrap.getItemViewType();
            final ArrayList<ViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
            if (mScrap.get(viewType).mMaxScrap <= scrapHeap.size()) {
                return;
            }
            if (DEBUG && scrapHeap.contains(scrap)) {
                throw new IllegalArgumentException("this scrap item already exists");
            }
            scrap.resetInternal();
            scrapHeap.add(scrap);
        }
```

包含了一下最大值和重复插入的容错。其中`resetInternal`方法，则是会清除ViewHolder中的所有内容。让它成为一个干干净净的ViewHolder。

```java
        void resetInternal() {
            mFlags = 0;
            mPosition = NO_POSITION;
            mOldPosition = NO_POSITION;
            mItemId = NO_ID;
            mPreLayoutPosition = NO_POSITION;
            mIsRecyclableCount = 0;
            mShadowedHolder = null;
            mShadowingHolder = null;
            clearPayload();
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
            mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
            clearNestedRecyclerViewIfNotNested(this);
        }
```

### RecyclerView每层缓存的作用

整体来说RecyclerView的缓存可分为四层。每一层缓存的目的都不尽相同。当我们想要修改某一层缓存的配置，甚至重写某一层缓存时，我们需要慎重地考虑这一层缓存的作用，当我对它进行修改会带来什么样的后果。

- mAttachedScrap和mChangedScrap，是缓存的屏幕上的可见内容。它本身的大小是无限的，因为屏幕上显示多少item是无法限制的，这一层缓存并不会带来额外的缓存。当我们改变它时，改变的是在屏幕内的item，收到刷新通知时的行为。通常来说，这样的需求是比较少的。
- mCachedViews，是缓存的屏幕外的内容。mCachedViews中的缓存是携带了ViewHolder的数据的。也就是说，它只能缓存已经显示过的ViewHolder。显而易见，它的主要作用是让已经显示过的ViewHolder需要再次显示时，能够快速显示。RecyclerView中，mCachedViews的默认大小为2 。但mCachedViews我们是可以修改的，缓存的越多，用户回看时就越快，同时消耗的内存也越多。**这是一个内存和时间置换的配置**。当我们内存充裕，或者显示的item比较小时，可以考虑适当地放大这个配置，来增加回看的流畅性。
- mViewCahceExtension，是一层自定义缓存，位于mCacheViews之后，RecycledViewPool之前。首先，我们要明确，mViewCahceExtension还是缓存的带数据的ViewHolder，所以，它本质上和mCachedViews一样，是提升回看性能的。 所以我们通常用它来提升某个特定position的ItemView的回看性能。比如，我们有某个ItemView，界面构建很废时，处在RecyclerView的固定位置中，且界面不需要刷新。这样的ItemView在内存允许的情况下，我们建议在mViewCacheExtension中单独缓存。它不会因为mCachedViews中缓存到上限被回收，回看时也不需要重新构建View。
- RecycledViewPool，是RecyclerView缓存的最后一层。当我们在上面三层缓存都没取到时，才会用到RecycledViewPool。RecycledViewPool也是唯一可以用于尚未展示过的ItemView的一层缓存。RecycledViewPool中存放的都是被清除了数据的ViewHolder。也就是说，它保持着onCreateView后ViewHolder最初的状态。当我们要使用ViewHolder时，就从RecycledViewPool中，拿出对应ViewType的ViewHolder，然后绑上数据，刷新界面。我们从它的结构可以看出，RecycledViewPool几乎是和RecycerView解耦的，它只与ViewHolder有关，和position、数据一概没有关系。所以，我们甚至可以让多个RecyclerView共用一个RecycledViewPool，以此来优化内存。



### RecyclerView和listView对比

#### 缓存机制对比

**缓存机制：**

listView 是二级缓存

mActiveViews：屏幕内itemView快速重用不需要重写调用createView和bindView；

onSCrapViews：缓存离开屏幕的itemView，目的是让即将进入屏幕的itemView重用。

RecycleView 是四级缓存:

mAttachedScrap：同listview的mActiveViews；

mCachedViews和RecyclerViewPool同listView的onScrapViews；mViewCachedExtension：需要用户自定义实现默认不实现RecyclerView比ListView多两级缓存，支持多个离ItemView缓存，支持开发者自定义缓存处理逻辑，支持多个RecyclerView共用同一个RecyclerViewPool(缓存池)，需要自己创建维护，并设置给RecyclerView。在特定场景下有优势（如viewPage下多个列表）

**缓存基本一致**：

1). mActiveViews和mAttachedScrap功能相似，意义在于快速重用屏幕上可见的列表项ItemView，而不需要重新createView和bindView；
2). mScrapView和mCachedViews + mReyclerViewPool功能相似，意义在于缓存离开屏幕的ItemView，目的是让即将进入屏幕的ItemView重用.
3). RecyclerView的优势在于

​     mCacheViews的使用，可以做到屏幕外的列表项ItemView进入屏幕内时也无须bindView快速重用；

​    mRecyclerPool可以供多个RecyclerView共同使用，在特定场景下，如viewpaper+多个列表页下有优势.客观来说，RecyclerView在特定场景下对ListView的缓存机制做了补强和完善。

**不同之处**：

1). RecyclerView缓存RecyclerView.ViewHolder，抽象可理解为：
View + ViewHolder(避免每次createView时调用findViewById) + flag(标识状态)；
2). ListView缓存View。

3). 缓存使用不同：

RecyclerView中mCachedView获取屏幕外item时通过匹配Pos获取viewhodler，这样在无数据改变时不需要重新bindView。

ListView中mScrapViews获取屏幕外item是通过pos获取view，没有直接使用而是重新getView，必然会重新执行bindView



#### 局部刷新

RecyclerView更大的亮点在于提供了局部刷新的接口，通过局部刷新，就能避免调用许多无用的bindView。



#### 布局效果

 LayoutManager 只是一个抽象类而已，系统已经为我们提供了三个相关的实现类 **LinearLayoutManager（线性布局效果）**、**GridLayoutManager（网格布局效果）**、**StaggeredGridLayoutManager（瀑布流布局效果）**

如果你想用 RecyclerView 来实现自己 YY 出来的一种效果，则可以去继承实现自己的 LayoutManager，并重写相应的方法。



#### HeaderView 和 FooterView

在 ListView 的设计中，存在着 HeaderView 和 FooterView 两种类型的视图，并且系统也提供了相应的 API 来让我们设置。RecyclerView 没有，需要自己处理



#### 动画效果

 ListView 自身并没有为我们提供封装好的 API 来实现动画效果切换。所以，如果要给 ListView 的 Item 加动画，我们只能自己通过属性动画来操作 Item 的视图。RecyclerView 则为我们提供了很多基本的动画 API ，如**增删移改**动画



相关文章：

https://www.jianshu.com/p/6b0c66d30d8e