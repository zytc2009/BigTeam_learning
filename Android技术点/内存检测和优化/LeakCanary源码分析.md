

修改者 | 修改时间|所属类别|备注
---|---|---|---
王丽云 | 2020/02/15 |Android进阶| 内存优化分析


作为android进阶知识，性能优化不管是在社招面试还是在日常工作中都是相当实用的知识，并且也是区分中级和高级程序员的试金石。小笨鸟就会以不同的专题来进行讲解，希望大家喜欢，如果想了解更多的话，欢迎关注我一起学习。
今天我们先学习内存优化中的一个小知识点，就是内存泄露的检测和解决。当然，如何解决是大多数中级工程师都要去学习的东西，网上也有大量的资料，所以我这里不会详解。而是主要着眼于内存泄露的检测。Square公司出品的大名鼎鼎的LeakCanary，就是业界知名的内存泄露检测的利器。

阅读本篇文章，你将会学习到：

- LeakCanary检测内存泄露的原理
- 使用ContentProvider进行三方库初始化的方法

# 原理概述

关于LeakCanary的原理，官网上已经给出了详细的解释。翻译过来就是：
1. LeakCanary使用ObjectWatcher来监控Android的生命周期。当Activity和Fragment被destroy以后，这些引用被传给ObjectWatcher以WeakReference的形式引用着。如果gc完5秒钟以后这些引用还没有被清除掉，那就是内存泄露了。
2. 当被泄露掉的对象达到一个阈值，LeakCanary就会把java的堆栈信息dump到 ==.hprof==文件中。
3. LeakCanary用Shark库来解析 ==.hprof==文件，找到无法被清理的引用的引用栈，然后再根据对Android系统的知识来判定是哪个实例导致的泄露。
4. 通过泄露信息，LeakCanary会将一条完整的引用链缩减到一个小的引用链，其余的因为这个小的引用链导致的泄露链都会被聚合在一起。
通过官网的介绍，我们很容易就抓住了学习LeakCanary这个库的重点：
LeakCanary是如何使用ObjectWatcher 监控生命周期的？
LeakCanary如何dump和分析 ==.hprof==文件的？
看官方原理总是感觉不过瘾，下面我们从代码层面上来分析。本文基于LeakCanary 2.0 beta版。
基本使用

LeakCanary的使用相当的简单。只需要在module的build.gradle添加一行依赖，代码侵入少。

```
dependencies {
  // debugImplementation because LeakCanary should only run in debug builds.
  debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.0-beta-3'
}
```

就这样，应用非常简单就接入了LeakCanary内存检测功能。当然还有一些更高级的用法，比如更改自定义config，增加监控项等，大家可以参考官网

# 源码分析

## 1. 初始化

和之前的1.x版本相比，2.0甚至都不需要再在Application里面增加install的代码。可能很多的同学都会疑惑，LeakCanary是如何插入自己的初始化代码的呢? 其实这里LeakCanary是使用了ContentProvider来进行初始化。我之前在介绍Android插件化系列三：技术流派和四大组件支持的时候曾经介绍过ContentProvider的特点，即在打包的过程中来自不同module的ContentProvider最后都会merge到一个文件中，启动app的时候ContentProvider是自动安装，并且安装会比Application的onCreate还早。LeakCanary就是依据这个原理进行的设计。具体可以参考【译】你的Android库是否还在Application中初始化？

我们可以查看LeakCanary源码，发现它在leakcanary-object-watcher-android的AndroidManifest.xml中有一个ContentProvider。

```
<provider
    android:name="leakcanary.internal.AppWatcherInstaller$MainProcess"
    android:authorities="${applicationId}.leakcanary-installer"
    android:exported="false"/>
```

然后我们查看AppWatcherInstaller的代码，发现内部是使用InternalAppWatcher进行的install。


```
// AppWatcherInstaller
override fun onCreate(): Boolean {
    val application = context!!.applicationContext as Application
    InternalAppWatcher.install(application)
    return true
}

  // InternalAppWatcher
fun install(application: Application) {
    // 省略部分代码
    checkMainThread()
    if (this::application.isInitialized) {
      return
    }
    InternalAppWatcher.application = application

    val configProvider = { AppWatcher.config }
    ActivityDestroyWatcher.install(application, objectWatcher, configProvider)
    FragmentDestroyWatcher.install(application, objectWatcher, configProvider)
    onAppWatcherInstalled(application)
}
```
可以看到这里主要把Activity和Fragment区分了开来，然后分别进行注册。Activity的生命周期监听是借助于Application.ActivityLifecycleCallbacks。

```
private val lifecycleCallbacks =
    object : Application.ActivityLifecycleCallbacks by noOpDelegate() {
      override fun onActivityDestroyed(activity: Activity) {
        if (configProvider().watchActivities) {
          objectWatcher.watch(activity)
        }
      }
    }
```


application.registerActivityLifecycleCallbacks(activityDestroyWatcher.lifecycleCallbacks)
而Fragment的生命周期监听是借助了Activity的ActivityLifecycleCallbacks生命周期回调，当Activity创建的时候去调用FragmentManager.registerFragmentLifecycleCallbacks方法注册Fragment的生命周期监听。
    
```
override fun onFragmentViewDestroyed(
      fm: FragmentManager,
      fragment: Fragment
    ) {
      val view = fragment.view
      if (view != null && configProvider().watchFragmentViews) {
        objectWatcher.watch(view)
      }
    }

    override fun onFragmentDestroyed(
      fm: FragmentManager,
      fragment: Fragment
    ) {
      if (configProvider().watchFragments) {
        objectWatcher.watch(fragment)
      }
    }
  }
```

最终，Activity和Fragment都将自己的引用传入了ObjectWatcher.watch()进行监控。从这里开始进入到LeakCanary的引用监测逻辑。

题外话：LeakCanary 2.0版本和1.0版本相比，增加了Fragment的生命周期监听，每个类的职责也更加清晰。但是我个人觉得使用 (Activty)->Unit 这种lambda表达式作为类的写法不是很优雅，倒不如面向接口编程。完全可以设计成ActivityWatcher和FragmentWatcher都继承自某个接口，这样也方便后续扩展。
## 2. 引用监控

### 2.1 引用和GC

1. 引用
    首先我们先介绍一点准备知识。大家都知道，java中存在四种引用：

- 强引用：垃圾回收器绝不会回收它，当内存空间不足，Java虚拟机宁愿抛出OOM
- 软引用：只有在内存不足的时候JVM才会回收仅有软引用指向的对象所占的空间
- 弱引用：当JVM进行垃圾回收时，无论内存是否充足，都会回收仅被弱引用关联的对象。
- 虚引用：和没有任何引用一样，在任何时候都可能被垃圾回收。

一个对象在被gc的时候，如果发现还有软引用（或弱引用，或虚引用）指向它，就会在回收对象之前，把这个引用加入到与之关联的引用队列(ReferenceQueue)中去。如果一个软引用（或弱引用，或虚引用）对象本身在引用队列中，就说明该引用对象所指向的对象被回收了。

当软引用（或弱引用，或虚引用）对象所指向的对象被回收了，那么这个引用对象本身就没有价值了，如果程序中存在大量的这类对象（注意，我们创建的软引用、弱引用、虚引用对象本身是个强引用，不会自动被gc回收），就会浪费内存。因此我们这就可以手动回收位于引用队列中的引用对象本身。

比如我们经常看到这种用法

```
WeakReference<ArrayList> weakReference = new WeakReference<ArrayList>(list);
```

还有也有这样一种用法

```
WeakReference<ArrayList> weakReference = new WeakReference<ArrayList>(list, new ReferenceQueue<WeakReference<ArrayList>>());
```

这样就可以把对象和ReferenceQueue关联起来，进行对象是否gc的判断了。另外我们从弱引用的特征中看到，弱引用是不会影响到这个对象是否被gc的，很适合用来监控对象的gc情况。

2. GC
java中有两种手动调用GC的方式。

```
System.gc();
// 或者
Runtime.getRuntime().gc();
```

### 2.2 监控

我们在第一节中提到，Activity和Fragment都依赖于响应的LifecycleCallback来回调销毁信息，然后调用了ObjectWatcher.watch添加了销毁后的监控。接下来我们看ObjectWatcher.watch做了什么操作。
  
```
@Synchronized fun watch(
    watchedObject: Any,
    name: String
  ) {
    removeWeaklyReachableObjects()
    val key = UUID.randomUUID().toString()
    val watchUptimeMillis = clock.uptimeMillis()
    val reference =
      KeyedWeakReference(watchedObject, key, name, watchUptimeMillis, queue)
    watchedObjects[key] = reference
    checkRetainedExecutor.execute {
      moveToRetained(key)
    }
  }

  private fun removeWeaklyReachableObjects() {
    // WeakReferences are enqueued as soon as the object to which they point to becomes weakly
    // reachable. This is before finalization or garbage collection has actually happened.
    var ref: KeyedWeakReference?
    do {
      ref = queue.poll() as KeyedWeakReference?
      if (ref != null) {
        watchedObjects.remove(ref.key)
      }
    } while (ref != null)
  }

  @Synchronized private fun moveToRetained(key: String) {
    removeWeaklyReachableObjects()
    val retainedRef = watchedObjects[key]
    if (retainedRef != null) {
      retainedRef.retainedUptimeMillis = clock.uptimeMillis()
      onObjectRetainedListeners.forEach { it.onObjectRetained() }
    }
  }
```

这里我们看到，有一个存储着KeyedWeakReference的ReferenceQueue对象。在每次增加watch object的时候，都会去把已经处于ReferenceQueue中的对象给从监控对象的map即watchObjects中清理掉，因为这些对象都已经被回收了。然后再去生成一个KeyedWeakReference，这个对象就是一个持有了key和监测开始时间的WeakReference对象。最后再去调用moveToRetained，相当于记录和回调给监控方这个对象正式开始监测的时间。

那么我们现在已经拿到了需要监控的对象了，但是又是怎么去判断这个对象已经内存泄露的呢？这就要继续往下面看。我们主要到前面在讲解InternalAppWatcher的install方法的时候，除了install了Activity和Fragment的检测器，还调用了onAppWatcherInstalled(application)方法，看代码发现这个方法就是InternalLeakCanary的invoke方法。
  
```
override fun invoke(application: Application) {
    this.application = application

    AppWatcher.objectWatcher.addOnObjectRetainedListener(this)

    val heapDumper = AndroidHeapDumper(application, leakDirectoryProvider)

    val gcTrigger = GcTrigger.Default

    val configProvider = { LeakCanary.config }

    val handlerThread = HandlerThread(LEAK_CANARY_THREAD_NAME)
    handlerThread.start()
    val backgroundHandler = Handler(handlerThread.looper)

    heapDumpTrigger = HeapDumpTrigger(
        application, backgroundHandler, AppWatcher.objectWatcher, gcTrigger, heapDumper,
        configProvider
    )
  }

  override fun onObjectRetained() {
    if (this::heapDumpTrigger.isInitialized) {
      heapDumpTrigger.onObjectRetained()
    }
  }
```

我们看到首先是初始化了heapDumper，gcTrigger，heapDumpTrigger等对象用于gc和heapDump，同时还实现了OnObjectRetainedListener，并把自己添加到了上面的onObjectRetainedListeners中，以便每个对象moveToRetained的时候，InternalLeakCanary都能获取到onObjectRetained()的回调，回调里就只是回调了heapDumpTrigger.onObjectRetained()方法。看来都是依赖于HeapDumpTrigger这个类。

HeapDumpTrigger主要的处理逻辑都在checkRetainedObjects方法中。
  
```
private fun checkRetainedObjects(reason: String) {
    val config = configProvider()
    var retainedReferenceCount = objectWatcher.retainedObjectCount

    if (retainedReferenceCount > 0) {
      gcTrigger.runGc()  // 触发一次GC操作，只保留不能被回收的对象
      retainedReferenceCount = objectWatcher.retainedObjectCount
    }

    if (checkRetainedCount(retainedReferenceCount, config.retainedVisibleThreshold)) return

    if (!config.dumpHeapWhenDebugging && DebuggerControl.isDebuggerAttached) {
      showRetainedCountWithDebuggerAttached(retainedReferenceCount)
      scheduleRetainedObjectCheck("debugger was attached", WAIT_FOR_DEBUG_MILLIS)
      return
    }

    val heapDumpUptimeMillis = SystemClock.uptimeMillis()
    KeyedWeakReference.heapDumpUptimeMillis = heapDumpUptimeMillis
    dismissRetainedCountNotification()
    val heapDumpFile = heapDumper.dumpHeap()
    if (heapDumpFile == null) {
      scheduleRetainedObjectCheck("failed to dump heap", WAIT_AFTER_DUMP_FAILED_MILLIS)
      showRetainedCountWithHeapDumpFailed(retainedReferenceCount)
      return
    }
    lastDisplayedRetainedObjectCount = 0
    objectWatcher.clearObjectsWatchedBefore(heapDumpUptimeMillis)

    HeapAnalyzerService.runAnalysis(application, heapDumpFile)
  }
```

那么HeapDumpTrigger具体做了些啥呢？我理了一下主要是下面几个功能：
- 后台线程轮询当前还存活着的对象
- 如果存活的对象大于0，那就触发一次GC操作，回收掉没有泄露的对象
- GC完后，仍然存活着的对象数和预定的对象数相比较，如果多了就调用heapDumper.dumpHeap()方法把对象dump成文件，并交给HeapAnalyzerService去分析
- 根据存活情况展示通知

### 2.3 总结

看到了这里，我们应该脑海中有概念了。Activity和Fragment通过注册系统的监听在onDestroy的时候把自己的引用放入ObjectWatcher进行监测，监测主要是通过HeapDumpTrigger类轮询进行，主要是调用AndroidHeapDumper来dump出文件来，然后依赖于HeapAnalyzerService来进行分析。后面一小节，我们将会聚焦于对象dump操作和HeapAnalyzerService的分析过程。
## 3. dump对象及分析

### 3.1 dump对象

hprof是JDK提供的一种JVM TI Agent native工具。JVM TI，全拼是JVM Tool interface，是JVM提供的一套标准的C/C++编程接口，是实现Debugger、Profiler、Monitor、Thread Analyser等工具的统一基础，在主流Java虚拟机中都有实现。hprof工具事实上也是实现了这套接口，可以认为是一套简单的profiler agent工具。我们在新知周推：10.8-10.14（启动篇）中也提到过，可以参考其中美团的文章。

用过Android Studio Profiler工具的同学对hprof文件都不会陌生，当我们使用Memory Profiler工具的Dump Java heap图标的时候，profiler工具就会去捕获你的内存分配情况。但是捕获以后，只有在Memory Profiler正在运行的时候我们才能查看，那么我们要怎么样去保存当时的内存使用情况呢，又或者我想用别的工具来分析堆分配情况呢，这时候hprof文件就派上用场了。Android Studio可以把这些对象给export到hprof文件中去。

LeakCanary也是使用的hprof文件进行对象存储。hprof文件比较简单，整体按照 前置信息 + 记录表的格式来组织的。但是记录的种类相当之多。具体种类可以查看HPROF Agent。

同时，android中也提供了一个简便的方法Debug.dumpHprofData(filePath)可以把对象dump到指定路径下的hprof文件中。LeakCanary使用使用Shark库来解析Hprof文件中的各种record，比较高效，使用Shark中的HprofReader和HprofWriter来进行读写解析，获取我们需要的信息。大家可以关注一些比较重要的，比如：
- Class Dump
- Instance Dump
- Object Array Dump
- Primitive Array Dump
dump具体的代码在AndroidHeapDumper类中。HprofReader和HprofWriter过于复杂，有兴趣的直接查看源码吧

```
override fun dumpHeap(): File? {
    val heapDumpFile = leakDirectoryProvider.newHeapDumpFile() ?: return null

    return try {
      Debug.dumpHprofData(heapDumpFile.absolutePath)
      if (heapDumpFile.length() == 0L) {
        null
      } else {
        heapDumpFile
      }
    } catch (e: Exception) {
      null
    } finally {
    }
  }
```

### 3.2 对象分析

前面我们已经分析到了，HeapDumpTrigger主要是依赖于HeapAnalyzerService进行分析。那么这个HeapAnalyzerService究竟有什么玄机？让我们继续往下面看。可以看到HeapAnalyzerService其实是一个ForegroundService。在接收到分析的Intent后就会调用HeapAnalyzer的analyze方法。所以最终进行分析的地方就是HeapAnalyzer的analyze方法。

核心代码如下
    
```
try {
      listener.onAnalysisProgress(PARSING_HEAP_DUMP)
      Hprof.open(heapDumpFile)
          .use { hprof ->
           // 1.生成graph
            val graph = HprofHeapGraph.indexHprof(hprof, proguardMapping)
            // 2.寻找Leak
            val findLeakInput = FindLeakInput(
                graph, leakFinders, referenceMatchers, computeRetainedHeapSize, objectInspectors
            )
            val (applicationLeaks, libraryLeaks) = findLeakInput.findLeaks()
            listener.onAnalysisProgress(REPORTING_HEAP_ANALYSIS)
            return HeapAnalysisSuccess(
                heapDumpFile, System.currentTimeMillis(), since(analysisStartNanoTime),
                applicationLeaks, libraryLeaks
            )
          }
    } catch (exception: Throwable) {
      listener.onAnalysisProgress(REPORTING_HEAP_ANALYSIS)
      return HeapAnalysisFailure(
          heapDumpFile, System.currentTimeMillis(), since(analysisStartNanoTime),
          HeapAnalysisException(exception)
      )
    }
  }
```

这段代码中涉及到了专为LeakCanary设计的Shark库的用法，在这里就不多解释了。大概介绍一下每一步的作用：
- 首先调用HprofHeapGraph.indexHprof方法，这个方法会把dump出来的各种实例instance，Class类对象和Array对象等都建立起查询的索引，以record的id作为key，把需要的信息都存储在Map中便于后续取用
- 调用findLeakInput.findLeak方法，这个方法会从GC Root开始查询，找到最短的一条导致泄露的引用链，然后再根据这条引用链构建出LeakTrace。
- 把查询出来的LeakTrace对外展示

## 总结

本篇文章分析了LeakCanary检测内存泄露的思路和一些代码的设计思想，但是限于篇幅不能面面俱到。接下来我们回答一下文章开头提出的问题。

1.LeakCanary是如何使用ObjectWatcher 监控生命周期的？
LeakCanary使用了Application的ActivityLifecycleCallbacks和FragmentManager的FragmentLifecycleCallbacks方法进行Activity和Fragment的生命周期检测，当Activity和Fragment被回调onDestroy以后就会被ObjectWatcher生成KeyedReference来检测，然后借助HeapDumpTrigger的轮询和触发gc的操作找到弹出提醒的时机。

2.LeakCanary如何dump和分析.hprof文件的？
使用Android平台自带的Debug.dumpHprofData方法获取到hprof文件，使用自建的Shark库进行解析，获取到LeakTrace。