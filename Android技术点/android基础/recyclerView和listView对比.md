[Toc]

### 刷新的原理

Adapter.notifyItemChanged()方法会导致RecyclerView的onMeasure()和onLayout()方法调用。

```
protected void onMeasure(int widthSpec, int heightSpec) {
    if (mLayout == null) {
        defaultOnMeasure(widthSpec, heightSpec);
        return;
    }
    //LinearLayoutManager返回true
    if (mLayout.isAutoMeasureEnabled()) {     
        mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);    
        final boolean measureSpecModeIsExactly =
                widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;
        if (measureSpecModeIsExactly || mAdapter == null) {
            return;
        }

        if (mState.mLayoutStep == RecyclerView.State.STEP_START) {
            dispatchLayoutStep1();
        }
        // set dimensions in 2nd step. Pre-layout should happen with old dimensions for
        // consistency
        mLayout.setMeasureSpecs(widthSpec, heightSpec);
        mState.mIsMeasuring = true;
        dispatchLayoutStep2();

        // now we can get the width and height from the children.
        mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);

        // if RecyclerView has non-exact width and height and if there is at least one child
        // which also has non-exact width & height, we have to re-measure.
        if (mLayout.shouldMeasureTwice()) {
            mLayout.setMeasureSpecs(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
            mState.mIsMeasuring = true;
            dispatchLayoutStep2();
            // now we can get the width and height from the children.
            mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);
        }
    } else {
        if (mHasFixedSize) {
            mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
            return;
        }
        // custom onMeasure
        if (mAdapterUpdateDuringMeasure) {
            processAdapterUpdatesAndSetAnimationFlags();
            if (mState.mRunPredictiveAnimations) {
                mState.mInPreLayout = true;
            } else {
                // consume remaining updates to provide a consistent state with the layout pass.
                mAdapterHelper.consumeUpdatesInOnePass();
                mState.mInPreLayout = false;
            }
            mAdapterUpdateDuringMeasure = false;
        } else if (mState.mRunPredictiveAnimations) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            Log.d("RecyclerView", "whb onMeasure() end ");
            return;
        }
        
        mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
    }
}
```

在onLayout()方法中会调用调用dispatchLayout()方法,内部执行dispatchLayoutStep1()、dispatchLayoutStep2()和dispatchLayoutStep3()三个方法，

```
void dispatchLayout() {
    if (mAdapter == null) { return; }
    if (mLayout == null)  { return; }
    
    mState.mIsMeasuring = false;
    if (mState.mLayoutStep == RecyclerView.State.STEP_START) {
        dispatchLayoutStep1();
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } else if (mAdapterHelper.hasUpdates() || mLayout.getWidth() != getWidth()
            || mLayout.getHeight() != getHeight()) {
        // First 2 steps are done in onMeasure but looks like we have to run again due to
        // changed size.
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } else {
        // always make sure we sync them (to ensure mode is exact)
        mLayout.setExactMeasureSpecsFrom(this);
    }
    dispatchLayoutStep3();
}
```

其中dispatchLayoutStep1()将更新信息存储到ViewHolder中，dispatchLayoutStep2()进行子View的布局，dispatchLayoutStep3()触发动画。

在dispatchLayoutStep2()中，会通过DefaultItemAnimator的canReuseUpdatedViewHolder()方法判断position处是否复用之前的ViewHolder，如果调用notifyItemChanged()时的payload不为空，则复用；否则，不复用。

在dispatchLayoutStep3()中，如果position处的ViewHolder与之前的ViewHolder相同，则执行DefaultItemAnimator的move动画；如果不同，则执行DefaultItemAnimator的change动画，旧View动画消失（alpha值从1到0），新View动画展现（alpha值从0到1），这样就出现了闪烁效果。

##### dispatchLayoutStep1

```
private void dispatchLayoutStep1() {
    mState.assertLayoutStep(State.STEP_START);
    startInterceptRequestLayout();
    //1.更新 mRunSimpleAnimations 和 mRunPredictiveAnimations flag 其实还有其他一些骚操作
    processAdapterUpdatesAndSetAnimationFlags();
    //2.mInPreLayout 设置为 true 后面有用
    mState.mInPreLayout = mState.mRunPredictiveAnimations;
    ...
    if (mState.mRunSimpleAnimations) {
        // Step 0: Find out where all non-removed items are, pre-layout
        int count = mChildHelper.getChildCount();
        for (int i = 0; i < count; ++i) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if (holder.shouldIgnore() || (holder.isInvalid() && !mAdapter.hasStableIds())) {
                continue;
            }
            final ItemHolderInfo animationInfo = mItemAnimator
                    .recordPreLayoutInformation(mState, holder,
                            ItemAnimator.buildAdapterChangeFlagsForAnimations(holder),
                            holder.getUnmodifiedPayloads());
            //5.保存动画信息相关
            mViewInfoStore.addToPreLayout(holder, animationInfo);
            if (mState.mTrackOldChangeHolders && holder.isUpdated() && !holder.isRemoved()
                    && !holder.shouldIgnore() && !holder.isInvalid()) {
                //3.如果holder确定要更新，就把它添加到 oldChangeHolders 集合中
                long key = getChangedHolderKey(holder);
                mViewInfoStore.addToOldChangeHolders(key, holder);
            }
        }
    }
    if (mState.mRunPredictiveAnimations) {
        ...
        //4.很重要，LayoutManager 开始工作
        mLayout.onLayoutChildren(mRecycler, mState);
        mState.mStructureChanged = didStructureChange;
 
        for (int i = 0; i < mChildHelper.getChildCount(); ++i) {
            final View child = mChildHelper.getChildAt(i);
            final ViewHolder viewHolder = getChildViewHolderInt(child);
            if (viewHolder.shouldIgnore()) {
                continue;
            }
            if (!mViewInfoStore.isInPreLayout(viewHolder)) {
                ...
                //5.保存动画信息相关
                mViewInfoStore.addToAppearedInPreLayoutHolders(viewHolder, animationInfo);
            }
        }
        ...
    } ...
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
    mState.mLayoutStep = State.STEP_LAYOUT;
}
```

##### dispatchLayoutStep2()

```
private void dispatchLayoutStep2() {  
    startInterceptRequestLayout(); //方法执行期间不能重入
    ...    //设置好初始状态
    mState.mItemCount = mAdapter.getItemCount();
    mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
    mState.mInPreLayout = false;

    mLayout.onLayoutChildren(mRecycler, mState); //调用布局管理器去布局

    mState.mStructureChanged = false;
    mPendingSavedState = null;
    ...
    mState.mLayoutStep = State.STEP_ANIMATIONS; //接下来执行布局的第三步

    stopInterceptRequestLayout(false);
}
```

这里有一个`mState`，它是一个`RecyclerView.State`对象。顾名思义它是用来保存`RecyclerView`状态的一个对象，主要是用在`LayoutManager、Adapter等`组件之间共享`RecyclerView状态`的。可以看到这个方法将布局的工作交给了`mLayout`。这里它的实例是`LinearLayoutManager`，因此接下来看一下`LinearLayoutManager.onLayoutChildren()`:

```
//LinearLayoutManger
@Override
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    //计算anchor的位置和偏移量
    updateAnchorInfoForLayout(recycler, state, mAnchorInfo);
    detachAndScrapAttachedViews(recycler);
    ...
    //以anchor为起点开始填充
    if (mAnchorInfo.mLayoutFromEnd) {//从后向前填充
         // fill towards start
         updateLayoutStateToFillStart(mAnchorInfo);
         mLayoutState.mExtraFillSpace = extraForStart;
         fill(recycler, mLayoutState, state, false);
         
         // fill towards end
         updateLayoutStateToFillEnd(mAnchorInfo);
         mLayoutState.mExtraFillSpace = extraForEnd;
         fill(recycler, mLayoutState, state, false);
         
         if (mLayoutState.mAvailable > 0) {//多余填充到头部
             // end could not consume all. add more items towards start
             extraForStart = mLayoutState.mAvailable;
             updateLayoutStateToFillStart(firstElement, startOffset);
             mLayoutState.mExtraFillSpace = extraForStart;
             fill(recycler, mLayoutState, state, false);
             startOffset = mLayoutState.mOffset;
         }
    } else {//从前向后填充
        //向后填充
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtra = extraForEnd;
        fill(recycler, mLayoutState, state, false);
        
        //向前填充
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForStart;
        fill(recycler, mLayoutState, state, false);
        
        if (mLayoutState.mAvailable > 0) {//多余填充到末尾
                extraForEnd = mLayoutState.mAvailable;
                // start could not consume all it should. add more items towards end
                updateLayoutStateToFillEnd(lastElement, endOffset);
                mLayoutState.mExtraFillSpace = extraForEnd;
                fill(recycler, mLayoutState, state, false);
        }
    }
    ...
}
```

以垂直布局来说明，mAnchorInfo为布局锚点信息，包含了子控件在Y轴上起始绘制偏移量（coordinate），ItemView在Adapter中的索引位置（position）和布局方向（mLayoutFromEnd）——这里是指start、end方向。这部分代码的功能就是：确定布局锚点，以此为起点向开始和结束方向填充ItemView

```
确定从锚点View到RecyclerView底部有多少可用空间
void updateLayoutStateToFillEnd(int itemPosition, int offset) {
    mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
    ...
    mLayoutState.mCurrentPosition = itemPosition;
    mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
    mLayoutState.mOffset = offset;
    mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
}
```

`mLayoutState`是`LinearLayoutManager`用来保存布局状态的一个对象。`mLayoutState.mAvailable`就是用来表示`有多少空间可用来布局`。`mOrientationHelper.getEndAfterPadding() - offset`其实大致可以理解为`RecyclerView`的高度。*所以这里可用布局空间`mLayoutState.mAvailable`就是RecyclerView的高度*

现在来看下fill()方法,主要是摆放子view：

```
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    ...
    int remainingSpace = layoutState.mAvailable + layoutState.mExtra;
    LayoutChunkResult layoutChunkResult = new LayoutChunkResult();
    while (...&&layoutState.hasMore(state)) {
        ...
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        ...
        if (...) {
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            remainingSpace -= layoutChunkResult.mConsumed;
        }
        if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {
            layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
    }
    ...
}
```

下面是layoutChunk()方法：

```
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    View view = layoutState.next(recycler);//这个方法会向 recycler view 要一个holder
    ...
    if (layoutState.mScrapList == null) {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addView(view);
        } else {
            addView(view, 0);
        }
    }
    ...
    measureChildWithMargins(view, 0, 0);////调用view的measure
    ...
    // We calculate everything with View's bounding box (which includes decor and margins)
    // To calculate correct layout position, we subtract margins.
   layoutDecoratedWithMargins(view, left, top, right, bottom);
    ...
}
```

这里的addView()方法，其实就是ViewGroup的addView()方法；measureChildWithMargins()方法看名字就知道是用于测量子控件大小的，简单地理解为测量子控件大小就好了。

```
public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
 
        final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;
 
        final int widthSpec = ...
        final int heightSpec = ...
        if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
            child.measure(widthSpec, heightSpec);
        }
}

 Rect getItemDecorInsetsForChild(View child) {
    ...
    final Rect insets = lp.mDecorInsets;
    insets.set(0, 0, 0, 0);
    final int decorCount = mItemDecorations.size();
    for (int i = 0; i < decorCount; i++) {//遍历所有装饰，累加
        mTempRect.set(0, 0, 0, 0);
        mItemDecorations.get(i).getItemOffsets(mTempRect, child, this, mState);
        insets.left += mTempRect.left;
        insets.top += mTempRect.top;
        insets.right += mTempRect.right;
        insets.bottom += mTempRect.bottom;
    }
    lp.mInsetsDirty = false;
    return insets;
}

```



### RecyclerView的缓存原理

#### Recycler

Recycler的作用就是重用ItemView。在填充ItemView的时候，ItemView是从它获取的；滑出屏幕的ItemView是由它回收的。对于不同状态的ItemView存储在了不同的集合中，比如有scrapped、cached、exCached、recycled，当然这些集合并不是都定义在同一个类里。

回到之前的layoutChunk方法中，有行代码layoutState.next(recycler)，它的作用自然就是获取ItemView，我们进入这个方法查看，最终它会调用到RecyclerView.Recycler.getViewForPosition()方法：

```java
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

**mChangedScrap 表示的是数据已经改变的但还在屏幕中的ViewHolder列表**。所以在改动之前，我们会从中获取ViewHolder。

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
                //2)根据id查找，在mAttachedScrap和mCachedViews中查找
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
      ...
       if (mState.isPreLayout() && holder.isBound()) {
                // do not update unless we absolutely have to.
                holder.mPreLayoutPosition = position;
       } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
            if (DEBUG && holder.isRemoved()) {
                    throw new IllegalStateException("Removed holder should be bound and it should"
                            + " come here only in pre-layout. Holder: " + holder
                            + exceptionLabel());
           }
           final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            //3) 主要调用了mAdapter.bindViewHolder(holder, offsetPosition);
           // 进而调用了onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());和holder.clearPayload();  
            bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
       }
     
         final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final RecyclerView.LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = (RecyclerView.LayoutParams) generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (!checkLayoutParams(lp)) {
                rvLayoutParams = (RecyclerView.LayoutParams) generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else {
                rvLayoutParams = (RecyclerView.LayoutParams) lp;
            }
            rvLayoutParams.mViewHolder = holder;
            rvLayoutParams.mPendingInvalidate = fromScrapOrHiddenOrCache && bound;
            return holder;
    
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
    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;
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

#### RecyclerView每层缓存的作用

整体来说RecyclerView的缓存可分为四层。每一层缓存的目的都不尽相同。当我们想要修改某一层缓存的配置，甚至重写某一层缓存时，我们需要慎重地考虑这一层缓存的作用，当我对它进行修改会带来什么样的后果。

- mAttachedScrap和mChangedScrap，是缓存的屏幕上的可见内容。它本身的大小是无限的，因为屏幕上显示多少item是无法限制的，这一层缓存并不会带来额外的缓存。当我们改变它时，改变的是在屏幕内的item，收到刷新通知时的行为。通常来说，这样的需求是比较少的。
- mCachedViews，是缓存的屏幕外的内容。mCachedViews中的缓存是携带了ViewHolder的数据的。也就是说，它只能缓存已经显示过的ViewHolder。显而易见，它的主要作用是让已经显示过的ViewHolder需要再次显示时，能够快速显示。RecyclerView中，mCachedViews的默认大小为2 。但mCachedViews我们是可以修改的，缓存的越多，用户回看时就越快，同时消耗的内存也越多。**这是一个内存和时间置换的配置**。当我们内存充裕，或者显示的item比较小时，可以考虑适当地放大这个配置，来增加回看的流畅性。
- mViewCahceExtension，是一层自定义缓存，位于mCacheViews之后，RecycledViewPool之前。首先，我们要明确，mViewCahceExtension还是缓存的带数据的ViewHolder，所以，它本质上和mCachedViews一样，是提升回看性能的。 所以我们通常用它来提升某个特定position的ItemView的回看性能。比如，我们有某个ItemView，界面构建很废时，处在RecyclerView的固定位置中，且界面不需要刷新。这样的ItemView在内存允许的情况下，我们建议在mViewCacheExtension中单独缓存。它不会因为mCachedViews中缓存到上限被回收，回看时也不需要重新构建View。
- RecycledViewPool，是RecyclerView缓存的最后一层。当我们在上面三层缓存都没取到时，才会用到RecycledViewPool。RecycledViewPool也是唯一可以用于尚未展示过的ItemView的一层缓存。RecycledViewPool中存放的都是被清除了数据的ViewHolder。也就是说，它保持着onCreateView后ViewHolder最初的状态。当我们要使用ViewHolder时，就从RecycledViewPool中，拿出对应ViewType的ViewHolder，然后绑上数据，刷新界面。我们从它的结构可以看出，RecycledViewPool几乎是和RecycerView解耦的，它只与ViewHolder有关，和position、数据一概没有关系。所以，我们甚至可以让多个RecyclerView共用一个RecycledViewPool，以此来优化内存。



#### 绘制过程

RecyclerView负责绘制所有decoration；ItemView的绘制由ViewGroup处理，这里的绘制是android常规绘制逻辑。下面来看看RecyclerView的draw()和onDraw()方法：

```
@Override
public void draw(Canvas c) {
    super.draw(c); 
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        mItemDecorations.get(i).onDrawOver(c, this, mState);
    }
    ...
}
 
@Override
public void onDraw(Canvas c) {
    super.onDraw(c); 
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        mItemDecorations.get(i).onDraw(c, this, mState);
    }
}
```



### 局部刷新原理

Adapter.notifyItemChanged(int position, Object payload)方法会导致RecyclerView的onMeasure()和onLayout()方法调用。

```
private final AdapterDataObservable mObservable = new RecyclerView.AdapterDataObservable();
 
public final void notifyItemChanged(int position, @Nullable Object payload) {
    //通知订阅者
    mObservable.notifyItemRangeChanged(position, 1, payload);
}

RecyclerView中最终调用registerObserver的地方为setAdapterInternal()方法，这个方法又被setAdapter()和swapAdapter()调用，registerObserver()方法传入的参数为RecyclerView的mObserver变量。
mObserver一旦收到消息，会通知mAdapterHelper来处理
我们看一下RecyclerViewDataObserver类：
public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
     //通知mAdapterHelper来处理
     if (mAdapterHelper.onItemRangeChanged(positionStart, itemCount, payload)) {
        triggerUpdateProcessor();
     }
}

mAdapterHelper的onItemRangeChanged()方法。
boolean onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        mPendingUpdates.add(obtainUpdateOp(UpdateOp.UPDATE, positionStart, itemCount, payload));
        return mPendingUpdates.size() == 1;
}
该方法将数据更新信息封装为UpdateOp对象，并将其添加到mPendingUpdates列表中，如果列表中数目为1，则返回true，否则false。在3.0的前提下，由于是在列表已经刷新完成的情况下调用notifyItemChanged()，所以应该返回true，会调用triggerUpdateProcessor方法。

再看下triggerUpdateProcessor()方法
void triggerUpdateProcessor() {
    if (POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
    } else {
                mAdapterUpdateDuringMeasure = true;
                requestLayout();
    }
}

由于未设置hasFixedSize，直接调用requestLayout()方法。requestLayout()方法会导致RecyclerView的onMeasure()和onLayout()方法得到调用。
```

在onLayout()方法中会调用dispatchLayoutStep1()、dispatchLayoutStep2()和dispatchLayoutStep3()三个方法，

在dispatchLayoutStep2()中，会通过DefaultItemAnimator的canReuseUpdatedViewHolder()方法判断position处是否复用之前的ViewHolder，如果调用notifyItemChanged()时的payload不为空，则复用；否则，不复用。

我们看一下上文分析的tryGetViewHolderForPositionByDeadline方法：

由于mState.isPreLayout()为false，所以步骤1不会执行，也就是不会从mChangedScrap列表中查找，但使用notifyItemChanged(int position)方法进行刷新的ViewHolder，在scrapView()方法中被存入了mChangedScrap列表中，这就导致步骤2之后，holder 仍为null。之后的流程就是从缓存（cacheExtension、recyclerPool）中查找，如果查找到相同类型的ViewHolder，则从缓存中取出复用；如果没有相同类型的ViewHolder，就只能通过onCreateViewHolder来创建新的viewHolder。但如果对于notifyItemChanged(int position, Object payload)方式进行刷新的ViewHolder，在scrapView()方法中被存入了mAttachedScrap列表中，经过步骤2后，就取得了之前的holder。这两种情况的holder，在步骤3处，由于mState.isPreLayout()为false，且holder.needsUpdate() 为true，所以会调用onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());来更新数据



我把recyclerView的代码拉下来测试的日志：

> 先看没有payload的
>
> D/RecyclerView: whb onMeasure() start
> D/RecyclerView: whb mLayout.onMeasure() 
> D/RecyclerView: whb onMeasure() dispatchLayoutStep1() 
> D/RecyclerView: whb dispatchLayoutStep1()  start 
> D/RecyclerView: whb dispatchLayoutStep1() addToOldChangeHolders  i=3
> D/RecyclerView: whb dispatchLayoutStep1() onLayoutChildren()  
> D/RecyclerView: whb onLayoutChildren() start
> D/RecyclerView: whb onLayoutChildren() updateAnchorInfoForLayout
> D/RecyclerView: whb onLayoutChildren() detachAndScrapAttachedViews()
> D/RecyclerView: whb onLayoutChildren() fromStart  updateLayoutStateToFillEnd()
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= true,position=0
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{61fb620 position=0 id=-1, oldPos=0, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= true,position=1
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{1be7bd9 position=1 id=-1, oldPos=1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= true,position=2
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{aad099e position=2 id=-1, oldPos=2, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= true,position=3
>
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{2b222c8 position=3 id=-1, oldPos=3, pLpos:-1 scrap [attachedScrap] update tmpDetached no parent}
>
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep1() end 
>
> D/RecyclerView: whb onMeasure()  dispatchLayoutStep2() 
> D/RecyclerView: whb dispatchLayoutStep2() start
> D/RecyclerView: whb dispatchLayoutStep2() onLayoutChildren()
> D/RecyclerView: whb onLayoutChildren() start
> D/RecyclerView: whb onLayoutChildren() updateAnchorInfoForLayout
> D/RecyclerView: whb onLayoutChildren() detachAndScrapAttachedViews()
> D/RecyclerView: whb onLayoutChildren() fromStart  updateLayoutStateToFillEnd()
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=0
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{61fb620 position=0 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=1
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{1be7bd9 position=1 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=2
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{aad099e position=2 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
>
> //此处与有payload不同，需要bindview了
>
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=3
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=null
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getRecycledViewPool holder=null
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() tryBindViewHolderByDeadline 
> D/RecyclerView: whb onBindViewHolder()  position=3
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep2() end
> D/RecyclerView: whb onMeasure() end 
>
> onLayout方法执行：
> D/RecyclerView: whb dispatchLayout() start 
> D/RecyclerView: whb dispatchLayout() dispatchLayoutStep22 
> D/RecyclerView: whb dispatchLayoutStep2() start
> D/RecyclerView: whb dispatchLayoutStep2() onLayoutChildren()
> D/RecyclerView: whb onLayoutChildren() start
> D/RecyclerView: whb onLayoutChildren() detachAndScrapAttachedViews()
> D/RecyclerView: whb onLayoutChildren() fromStart  updateLayoutStateToFillEnd()
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=0
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{61fb620 position=0 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=1
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{1be7bd9 position=1 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=2
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{aad099e position=2 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=3
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{6fd165f position=3 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep2() end
> D/RecyclerView: whb dispatchLayoutStep3() start
> D/RecyclerView: whb dispatchLayoutStep3() end
> D/RecyclerView: whb dispatchLayout() end 

再看有payload的

> D/RecyclerView: whb onMeasure() start
> D/RecyclerView: whb mLayout.onMeasure() 
> D/RecyclerView: whb onMeasure() dispatchLayoutStep1() 
> D/RecyclerView: whb dispatchLayoutStep1()  start 
> D/RecyclerView: whb onLayoutChildren() start
>
> //省略重复代码
>
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep1() end 
>
> D/RecyclerView: whb onMeasure()  dispatchLayoutStep2() 
> D/RecyclerView: whb dispatchLayoutStep2() start
> D/RecyclerView: whb dispatchLayoutStep2() onLayoutChildren()
> D/RecyclerView: whb onLayoutChildren() start
> //省略重复代码
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() mState.isPreLayout()= false,position=3
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() getScrapOrHiddenOrCachedHolder holder=CommonViewHolder{2b222c8 position=3 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] update tmpDetached no parent}
> D/RecyclerView: whb tryGetViewHolderForPositionByDeadline() tryBindViewHolderByDeadline 
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep2() end
> D/RecyclerView: whb onMeasure() end 
>
> onLayout方法执行：
> D/RecyclerView: whb dispatchLayout() start 
> D/RecyclerView: whb dispatchLayout() dispatchLayoutStep22 
> D/RecyclerView: whb dispatchLayoutStep2() start
> D/RecyclerView: whb dispatchLayoutStep2() onLayoutChildren()
> D/RecyclerView: whb onLayoutChildren() start
> //省略重复代码
> D/RecyclerView: whb onLayoutChildren() end
> D/RecyclerView: whb dispatchLayoutStep2() end
> D/RecyclerView: whb dispatchLayout() dispatchLayoutStep3 
> D/RecyclerView: whb dispatchLayoutStep3() start
> D/RecyclerView: whb dispatchLayoutStep3() end
> D/RecyclerView: whb dispatchLayout() end 

数据显示：onMeasure方法执行了dispatchLayoutStep1和dispatchLayoutStep2，onLayout方法执行了dispatchLayoutStep2和dispatchLayoutStep3；dispatchLayoutStep2中流程见上文分析

根本原因是因为mLayout.isAutoMeasureEnabled()，如果用的是LinearLayoutManager，返回true。细节见onMeasure方法



### 滑动时的刷新逻辑

recyclerView在onTouchEvent对滑动事件做了监听，然后派发到scrollStep()方法:

```
public boolean onTouchEvent(MotionEvent e) {
    scrollByInternal(canScrollHorizontally ? dx : 0,   
                       canScrollVertically ? dy : 0,    e)    
}

boolean scrollByInternal(int x, int y, MotionEvent ev) {
	...
	if (mAdapter != null) {
		scrollStep(x, y, mReusableIntPair);
		...
	}
	...
	dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, mScrollOffset,
                TYPE_TOUCH, mReusableIntPair);
    ...
    dispatchOnScrolled(consumedX, consumedY);
    return consumedNestedScroll || consumedX != 0 || consumedY != 0;
}

void scrollStep(int dx, int dy, @Nullable int[] consumed) {
    startInterceptRequestLayout(); //处理滑动时不能重入
    ...    
    if (dx != 0) {
        consumedX = mLayout.scrollHorizontallyBy(dx, mRecycler, mState);
    }    if (dy != 0) {
        consumedY = mLayout.scrollVerticallyBy(dy, mRecycler, mState);
    }
    ...
    stopInterceptRequestLayout(false);    
    if (consumed != null) { //记录消耗
        consumed[0] = consumedX;
        consumed[1] = consumedY;
    }
}
```

即把滑动的处理交给了mLayout, 这里继续看LinearLayoutManager.scrollVerticallyBy, 它直接调用了scrollBy(), 这个方法就是LinearLayoutManager处理滚动的核心方法。

```
LinearLayoutManager:
int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (getChildCount() == 0 || delta == 0) {
        return 0;
    }
    mLayoutState.mRecycle = true;
    final int absDelta = Math.abs(delta);
    //根据布局方向和滑动的距离来确定可用布局空间mLayoutState.mAvailable
    updateLayoutState(layoutDirection, absDelta, true, state);
    //摆放子view
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);
    if (consumed < 0) {
        return 0;
    }
    final int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
    //设置子view的偏移，实现滚动效果
    mOrientationHelper.offsetChildren(-scrolled);
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}

对于RecyclerView的滚动， mOrientationHelper.offsetChildren最终调用到了RecyclerView类的offsetChildrenVertical()方法
//dy这里就是滚动的距离
public void offsetChildrenVertical(@Px int dy) {    
	final int childCount = mChildHelper.getChildCount();
	for (int i = 0; i < childCount; i++) {
        mChildHelper.getChildAt(i).offsetTopAndBottom(dy);
    }
}
就是改变当前子View布局的top和bottom来达到滚动的效果。
```

##### 根据布局方向和滑动的距离来确定可用布局空间

以向下滚动为为例，看一下`updateLayoutState`方法:

```
// requiredSpace是滑动的距离;  canUseExistingSpace是true
void updateLayoutState(int layoutDirection, int requiredSpace,boolean canUseExistingSpace, RecyclerView.State state) {
	if (layoutDirection == LayoutState.LAYOUT_END) { //滚动方法为向下
        final View child = getChildClosestToEnd(); //获得RecyclerView底部的View
        ...
        mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection; //view的位置
        mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child); //view的偏移 offset
        scrollingOffset = mOrientationHelper.getDecoratedEnd(child) - mOrientationHelper.getEndAfterPadding();
    } else {
       ...
    }
    
    mLayoutState.mAvailable = requiredSpace;  
    if (canUseExistingSpace)  mLayoutState.mAvailable -= scrollingOffset;
    mLayoutState.mScrollingOffset = scrollingOffset;
}
```

*所以可用的布局空间就是滑动的距离*。那`mLayoutState.mScrollingOffset`是什么呢？

上面方法它的值是`mOrientationHelper.getDecoratedEnd(child) - mOrientationHelper.getEndAfterPadding();`，其实就是`（childView的bottom + childView的margin） - RecyclerView的Padding`。 看下图：

![](..\images\recyclerview_scroll.png)

`RecyclerView的padding`没标注,不过相信上图可以让你理解滑动布局可用空间`mLayoutState.mAvailable`。同时`mLayoutState.mScrollingOffset`就是`滚动的距离 - mLayoutState.mAvailable`

所以 `consumed`也可以理解:

```cpp
int consumed = mLayoutState.mScrollingOffset + fill(recycler, mLayoutState, state, false);
```





### RecyclerView和listView对比

#### 缓存机制对比

**缓存机制：**

listView 是二级缓存

mActiveViews：屏幕内itemView快速重用不需要重写调用createView和bindView；

onScrapViews：缓存离开屏幕的itemView，目的是让即将进入屏幕的itemView重用。

RecycleView 是四级缓存:

mAttachedScrap：同listview的mActiveViews；

mCachedViews和RecyclerViewPool同listView的onScrapViews；mCacheViews只存两个viewHolder；

mViewCachedExtension：需要用户自定义实现默认不实现。

RecyclerView比ListView多两级缓存，支持多个ItemView缓存，支持开发者自定义缓存处理逻辑，支持**多个RecyclerView共用同一个RecyclerViewPool(缓存池)，需要自己创建维护**，并设置给RecyclerView。RecyclerViewPool默认最多缓存5个viewholder。在特定场景下有优势（如viewPage下多个列表）

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

#### 布局效果

 LayoutManager 只是一个抽象类而已，系统已经为我们提供了三个相关的实现类 **LinearLayoutManager（线性布局效果）**、**GridLayoutManager（网格布局效果）**、**StaggeredGridLayoutManager（瀑布流布局效果）**

如果你想用 RecyclerView 来实现自己 YY 出来的一种效果，则可以去继承实现自己的 LayoutManager，并重写相应的方法。



#### HeaderView 和 FooterView

在 ListView 的设计中，存在着 HeaderView 和 FooterView 两种类型的视图，并且系统也提供了相应的 API 来让我们设置。RecyclerView 没有，需要自己处理



#### 动画效果

 ListView 自身并没有为我们提供封装好的 API 来实现动画效果切换。所以，如果要给 ListView 的 Item 加动画，我们只能自己通过属性动画来操作 Item 的视图。RecyclerView 则为我们提供了很多基本的动画 API ，如**增删移改**动画

#### 局部刷新

RecyclerView更大的亮点在于提供了局部刷新的接口，通过局部刷新，就能避免调用许多无用的bindView。






相关文章：

https://www.jianshu.com/p/6b0c66d30d8e

https://www.jianshu.com/p/a57608f2695f