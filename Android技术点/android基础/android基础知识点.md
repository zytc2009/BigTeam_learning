###android基础

[TOC]

#### Application生命周期

> Android程序启动后的第一个入口点是Application的onCreate();
>
> onCreate():Application创建的时候调用
>
> onConfigurationChanged(Configuration newConfig):当配置信息改变的时候会调用，如屏幕旋转、语言切换时。
>
> onLowMemory():Android系统整体内存较低时候调用，通常在这里释放一些不重要的资源，或者提醒用户清一下垃圾，来保证内存足够而让APP进程不被系统杀掉。它和OnTrimMemory中的TRIM_MEMORY_COMPLETE级别相同。
>
> onTrimMemory(int level)：Android 4.0 之后提供的一个API，用于取代onLowMemory()。在系统内存不足的时会被调用，提示开发者清理部分资源来释放内存，从而避免被 Android 系统杀死。
>
> onTerminate():Application结束的时候会调用,由系统决定调用的时机

#### Activity生命周期

> onCreate - onStart - onResume - onPause - onStop - onRestart - onDestroy 
>
> 页面A启动页面B，生命周期如何联动？
>
> 部署程序
>
> D/MainActivity: onCreate------A
> D/MainActivity: onStart-------A
> D/MainActivity: onResume------A
>
> 点击A中按钮开始跳转B
>
> D/MainActivity: onPause-------A
> D/SecondActivity: onCreate----B
> D/SecondActivity: onStart-----B
> D/SecondActivity: onResume----B
> D/MainActivity: onStop--------A
>
> 然后点击返回键从B返回A
>
> D/SecondActivity: onPause-----BD/MainActivity: onRestart-----A D/MainActivity:  onStart-------AD/MainActivity: onResume------AD/SecondActivity: onStop------BD/SecondActivity: onDestroy---B
>
> <https://blog.csdn.net/weixin_43589682/article/details/97030740>

#### 异常情况下的Activity的生命周期 & 数据如何保存和恢复

> 当系统配置发生变化后(比如屏幕旋转)，Activity 会被销毁，它的onPause、onStop、onDestroy会被调用，不过由于是在异常情况下终止的，系统会在调用onStop 方法之前调用 onSaveInstanceState 方法保存 Activity 的状态（UI状态和数据），在Activity 重建时，从onCreate 或onRestoreInstanceState 中获取保存的Activity的状态，重新恢复Activity。

#### Android Service、IntentService，Service和组件间通信

> IntentService是继承Service的，所以IntentService是Service的子类。IntentService 封装了 HandlerThread 和 Handler。
> IntentService是一个特殊的Service类，是实现了多线程处理异步请求的一个服务类，在handleIntent方法中进行耗时的操作，如果有多个耗时的操作任务，会按照顺序去一个一个的执行，执行完一个关闭一个。

#### Android异步任务机制之AsycTask

> AsyncTask本质上是一个封装了线程池、工作线程和Handler的异步框架，主要是执行异步任务，由于AsyncTask里面封装了Handler，所以它可以在主线程和子线程之间灵活的切换。
> 在Android 1.6之前的版本中，AsyncTask都是串行的，所有的任务都会一串一串的放到线程池中。
> 在Android 1.6到 Android 2.3中，AsyncTask改成并行
> 在Android 2.3之后，又改成串行了，但是还可以执行并行，想要执行并行，可以调用executeOnExecutor(...)

#### 简述一下Android 8.0、9.0 分别增加了哪些新特性

> 8.0：画中画、Notification Dots、智能文本选择、自动填写、系统/应用启动程序加速、WiFi感知功能；
>
> 画中画：Android8.0又带了另一种更高级的多窗口模式，号称“Picture in Picture”（简称PIP，即“画中画”）。应用一旦进入画中画模式，就会缩小为屏幕上的一个小窗口，该窗口可拖动可调整大小，非常适合用来播放视频。
>
> 9.0：自适应电池、黑暗模式、应用程序操作、应用定时器、自适应亮度、新截图快捷方式、通知信息等。

#### 图片缓存原理

> 1.内存缓存：读取速度最快。
>
> 2.硬盘缓存（文件缓存）：读取速度比内存缓存稍慢。
>
> 3.网络缓存：读取速度最慢。
>
> 缓存机制的通用调度算法是LRU(最近最久未使用)，与内存缓存和硬盘缓存对应的类分别是LruCache和DiskLruCache，网络缓存okHttp。

#### Android数据存储的五种方式

> 1.使用SharedPreferences存储数据(commit,apply)
>
> 2.文件存储数据
>
> 3.SQLite数据库存储数据
>
> 4.使用ContentProvider存储数据
>
> 5.网络存储数据

#### 初识ConstraintLayout及性能优势

> 它的出现主要是为了解决布局嵌套过多的问题，以灵活的方式定位和调整小部件。从 Android Studio 2.3 起，官方的模板默认使用 ConstraintLayout。
> 该布局拥有一个完全扁平的层次结构。这是因为 ConstraintLayout 允许您构建复杂的布局，而不必嵌套 View 和 ViewGroup 元素。
>
> ConstraintLayout 速度更快。
> ConstraintLayout 在测量/布局阶段的性能比RelativeLayout大约高 40%。
> https://blog.csdn.net/lyb2518/article/details/77942517

#### Activity的onNewIntent

> 该方法是在除standard启动模式下，Activity实例已经存在，不再调用onCreate方法，转而调用该方法，若该实例不存在，则调用onCreate方法，onNewIntent()生命周期在onStart之前，另外调用onNewIntent()时，要调用setIntent()方法，之后再使用getIntent()方法才有效，谨记！

#### Fragment的懒加载实现，参数传递与保存

> 当页面可见的时候，才加载当前页面，主要的方法是Fragment中的setUserVisibleHint()，此方法会在onCreateView(）之前执行，当viewPager中fragment改变可见状态时也会调用,当fragment 从可见到不见，或者从不可见切换到可见，都会调用此方法，使用getUserVisibleHint() 可以返回fragment是否可见状态。 
>
> 通过bundle方式传递数据，也可以通过set方式，比较下优缺点。

#### ContentProvider实例详解

> 你觉得Android设计ContentProvider的目的是什么呢？
>
> 1.藏数据的实现方式，对外提供统一的数据访问接口；
>
> 2.更好的数据访问权限管理。ContentProvider 可以对开发的数据进行权限设置，不同的 URI 可以对应不同的权限，只有符合权限要求的组件才能访问到 ContentProvider 的具体操作。
>
> 3.ContentProvider 封装了跨进程共享的逻辑，我们只需要 Uri 即可访问数据。由系统来管理 ContentProvider 的创建、生命周期及访问的线程分配，简化我们在应用间共享数据（进程间通信）的方式。我们只管通过 ContentResolver 访问 ContentProvider 所提示的数据接口，而不需要担心它所在进程是启动还是未启动。

> 运行在主线程的 ContentProvider 为什么不会影响主线程的UI操作?
>
> ContentProvider 的 onCreate() 是运行在 UI 线程的，而 query()，insert()，delete()，update() 是运行在线程池中的工作线程的，所以调用这四个方法并不会阻塞 ContentProvider 所在进程的主线程，但可能会阻塞调用者所在的进程的 UI 线程！
> 所以，调用 ContentProvider 的操作仍然要放在子线程中去做。虽然直接的 CRUD 的操作是在工作线程的，但系统会让你的调用线程等待这个异步的操作完成，你才可以继续线程之前的工作。

#### BroadcastReceiver使用总结

> 实现机制：
>
> 1.自定义广播类继承BroadcastReceiver,复写onReceiver()
>
> 2.通过Bnder机制向AndroidManifest进行注册广播
>
> 3.广播发送者通过Binder机制向AndroidManifest发送广播
>
> 4.AMS查找符合相应条件的广播发送到BroadcastReceiver相应的循环队列
>
> 5.消息队列执行拿到广播，回调BroadcastReceiver的onReceiver()

> BroadcastReceiver的生命周期：
>
> 1.动态注册：存活周期是在Context.registerReceiver和Context.unregisterReceiver之间，BroadcastReceiver每次收到广播都是使用注册传入的对象处理的。
>
> 2.静态注册：进程在的情况下，receiver会正常收到广播，调用onReceive方法，生命周期只存活在onReveive函数中，此方法结束，BroadcastReceiver就销毁了。进程不存在时，广播相应的进程会被激活，Application.onCreate会被调用，再调用onReceive()

> 广播分类：
>
> 1.普通广播：完全异步的，可以在同一时刻被所有接收者接收到，消息传递的效率比较高，并且无法中断广播的传播
>
> 2.有序广播：发送有序广播后，广播接收者将按预先声明的优先级依次接收Broadcast.优先级高的先接收到广播，而在其onRecevier()执行过程中，广播不会传播到下一个接收者，此时当前的广播接收者可以abortBroadcast()来总之广播继续向下传播。sendOrderedBroadcast(intent,null)发送有序广播
>
> 3.粘性广播:sendStickyBroadcast()来发送该类型的广播消息，当粘性广播发送后，最后一个粘性广播会滞留在操作系统中，在粘性广播发送后的一段时间里，如果有新的符合广播的动态注册广播接收者，将会收到这个广播消息。对于静态注册的广播接收者来说，这个等同于普通广播。
>
> 4.本地广播

> 本地广播和全局广播差别
>
> 1.LocalBroadcastReveiver仅在自己的应用内发送接受广播，数据更加安全。只能动态注册，在发送和注册时采用sendBroadcast和registerReceiver方法。
>
> 2.全局广播：发送的广播事件可被其他应用程序获取，也能响应其他应用程序发送的广播事件。全局广播既可动态注册也可以静态注册。

#### Android消息机制、runOnUiThread 、Handler.post、View.post之间的区别

> Handler机制：
>
> 1.在应用启动的时候，也就是ActivityThread的main方法里面，创建了Looper和MessageQueue，然后调用Looper.loop 开启消息循环
>
> 2.消息循环是这样的，调用MessageQueue的next方法，循环从消息队列中取出一条消息，然后交给Handler去处理，一般是回调handleMessage方法，取不到消息就阻塞，直到下一个消息入队或者其它延时消息到时间了就唤醒消息队列。
>
> 3.消息入队，通过调用handler的sendMessage方法，内部是调用MessageQueue的enqueueMessage方法，进行消息入队，入队的规制是：队列没有消息，或者要入队的消息没有设置delay，或者delay时间比队列头的消息delay时间短，则将要入队的消息放到队列头，否则就插到队列中间，需要移动链表。

> 发送延时消息是怎么处理的：
>
> 根据消息队列入队规制，如果队列中没消息，那么不管要入队的消息有没有延时，都放到队列头。如果队列不空，那么要跟队列头的消息比较一下延时，如果要入队的消息延时短，则放队列头，否则，放到队列中去，需要移动链表。

> 入队的规制的好处是，延时越长的消息在队列越后面，所以next方法取到一个延时消息时，如果判断时间没有到，就进行阻塞，不用管后面的消息，因为队列后面的消息延迟时间更长。

> runOnUiThread 、Handler.post、View.post之间的区别：

> 当在主线程分别调用 View.post 、Handler.post 、 runOnUiThread , new Thread() - [runOnUiThread] 四个方法执行顺序从快到慢为：

```
runOnUiThread - Handler.post - new Thread() - [runOnUiThread] - View.post
```

> 分析：
>
> runOnUiThread 因为当前执行在 UI线程，无需线程切换，直接执行。
>
> Handler.post 需要将请求加入 UI线程 Handler , 多了 入队 及 出队 时间。
>
> new Thread() - [runOnUiThread] 开启新线程，在启动完成后将请求加入 UI线程 Handler， 多了 线程切换 、入队 及 出队 时间。
>
> View.post 需要在view attach 到 Window 后，通过 ViewRootImpl 的 ViewRootHandler 执行请求。线程切换时间远小于UI渲染时间，所以执行最慢。

#### Binder机制，共享内存实现原理

> IPC机制简介：
>
> IPC是Inter-Process Communication的缩写，含义就是跨进程通信；
> 1.IPC（进程间通信）机制不是Android系统所独有的，其他系统也有相应的进程间通信机制。
> 2.Android系统架构中，大量采用了Binder机制作为IPC，是Android系统中最重要的组成。
> 3.当然也存在部分其他的IPC方式，比如Zygote通信便是采用socket。
>
> Android系统中，每个应用程序是由Android的Activity，Service，Broadcast，ContentProvider这四大组件的中一个或多个组合而成，这四大组件所涉及的多进程间的通信底层都是依赖于Binder IPC机制。
>
> 直观的看，Binder是Android中的一个类，实现了IBinder接口
> 从不同角度理解Binder：
>
> 1 从IPC角度，Binder是跨进程通信方式
>
> 2 从FrameWork角度，Binder是ServiceManager连接各种Manager（如am，wm
> ）等的桥梁
>
> 3 从应用层角度，Binder是客户端与服务端通信的媒介
>
> IPC原理:
>
> 每个Android的进程，只能运行在自己进程所拥有的虚拟地址空间。对于用户空间，不同进程之间彼此是不能共享的，而内核空间却是可共享的。Client进程向Server进程通信，就是利用进程间可共享的内核内存空间来完成底层通信工作的，Client端与Server端进程往往采用ioctl等方法跟内核空间的驱动进行交互。

> https://www.jianshu.com/p/b35e0716bce1

#### ActivityThread工作原理

> ActivityThread其实不是一个Thread，而是一个final类型的Java类,并且拥有main(String[] args) 方法,Java的JVM启动的入口就是main(String[] args)。
>
> 主要关注以下几点：
>
> 1. Looper.prepareMainLooper();(主程序Looper的初始化工作)
> 2. Looper.loop();(Loop方法中无限循环的去MessageQueue)
> 3. thread.attach(false, startSeq);(通过thread.attach(false, startSeq);把ActivityThread 和主线程进行绑定)
> 4. thread.getHandler();

#### 嵌套滑动实现原理

> 实现嵌套滑动有三种方案：
>
> 1. 纯事件拦截与派发方案
> 2. 基于NestingScroll机制的实现方案
> 3. 基于CoordinatorLayout与Behavior的实现方案
>
> 第一种方案：灵活性最高，也最繁琐。因为事件的拦截是一锤子买卖，谁拦截了事件，当前手势接下来的事件都会交给拦截者来处理，除非等到下一次Down事件触发。这很不方便多个View对同一个事件进行处理。
>
> 第二种方案：其实就是对原始的事件拦截机制做了一层封装，通过子View实现NestedScrollingChild接口，父View实现NestedScrollingParent 接口，并且在子View和父View中都分别有一个NestedScrollingChildHelper、NestedScrollingParentHelper来代理了父子之间的联动，开发者不用关心具体是怎么联动的，这一点很方便。
>
> 第三种方案：其实就是对原始的NestedScrolling机制再次做了一层封装。CoordinatorLayout默认实现了NestedScrollingParent接口。第二种方案只能由子View通知父View，但有时候除了需要通知父View，还需要通知兄弟View,这个时候就该是Behavior出场了。
> https://blog.csdn.net/m0_37218227/article/details/82937655

#### View的绘制原理，自定义View，自定义ViewGroup

> View的绘制是从上往下一层层迭代下来的。
>
> DecorView–>ViewGroup（— >ViewGroup）–>View ，按照这个流程从上往下，依次measure(测量),layout(布 局),draw(绘制)
> https://blog.csdn.net/Android_SE/article/details/104450788

> 自定义 View：
>
> 1. onMeasure()方法用于测量自己宽高，前提是继承View。如果继承系统已经有的控件比如TextView,Button等等 则不需要重写，因为系统已经给你计算好了。
> 2. onDraw()方法用于绘制自己想实现的样式。
> 3. onTouch()用于用户和控件的交互处理。

> 自定义 ViewGroup：
>
> 1. onMeasure方法，for循环获取所有子view,然后根据子view的宽高来计算自己的宽高。
> 2. onDraw() 一般不需要，默认是不会调用的。如果需要绘制就要实现dispatchDraw()来进行绘制。
> 3. onLayout()用来摆放子view,前提view是可见。
> 4. 很多情况下不是不会继承ViewGroup的，一般都是继承系统控件。

#### View、SurfaceView GLSurfaceView 与 TextureView

> View: 显示视图，内置画布，提供了图形绘制函数、触屏事件、按键事件函数等，必须在UI主线程内更新画面，速度较慢；
>
> SurfaceView: 基于view视图进行拓展的视图类，更适合2D游戏的开发，是view的子类，使用了双缓冲机制，即：允许在子线程中更新画面，所以刷新界面速度比view快。
>
> GLSurfaceView: 基于SurfaceView视图再次进行拓展的视图类，在SurfaceView基础上封装了EGL环境管理以及render线程，专用于3D游戏开发的视图。是SurfaceView的子类，openGL专用。
>
> TextrueView: 前面的SurfaceView的工作方式是创建一个置于应用窗口之后的新窗口，脱离了Android的普通窗口，因此无法对其应用变换操作(平移、缩放、旋转等)，而TextureView则解决了此问题，Android4.0引入。

#### 主线程Looper.loop为什么不会造成死循环

> 可以这样简单的来理解一下，一个Thread对应一个Looper和一个MessageQueue
> 这个MessageQueue是个一个阻塞队列，类似BlockingQueue，不同之处在于MessageQueue的阻塞方式是通过Pipe机制实现的。
> 阻塞队列，就是当队列里没有数据时，如果调用获取队首数据的方法时，当前线程会被阻塞（相当于执行了线程的wait方法），如果队列里面有了插入了新数据，则会唤醒被阻塞的方法（相当于执行了线程的notify方法），并返回该数据。再来看MessageQueue，这里的数据指的就是是每一个消息，这个消息则是通过handler来发送的。
>
> 综上所述，线程并没有一直死循环的工作，而是在没消息时被暂时挂起了，当有新消息进来的时候，就会又开始工作。

#### ViewPager的缓存实现

> 1. ViwePager 的缓存机制会默认缓存旁边的页面，是为了让页面更加流畅.在缓存旁边页面的时候会执行到onCreateView方法，如果你两个碎片的onCreateView 中都有执行请求数据的时候，旁边的页面也会发送请求，这样就会造成网络请求的一些问题。
>
> 2. 使用setOffscreenPagerLimit方法可以设置缓存页面的个数，默认为1，就算你传入负数他还是默认缓存旁边页面，也就是1
>
> 3. 由于ViewPager的默认缓存是1，所以ViewPager中至少会预加载一个页面的数据。如果页面有请求数据或者下载数据的时候，这就会影响当前显示的页面。
>
> 4. 我们知道在碎片中 只有在onCreateView方法中才可以拿到View 然后加载控件进行一些数据的设置.但是当碎片在ViewPager中的时候，缓存机制会导致旁边的页面中的onCreateView方法执行，这就是我们不想看到的结果。
>
> 5. Fragment里面有一个方法 setUserVisibleHint(boolean isVisibleToUser)方法，这个方法可以判断当前页面是否对用户可见。
>
> 6. 还有一点，当ViewPager缓存了旁边的那个碎片之后，那个碎片执行onCreateView之后，他碎片的生命周期会失效。当他被缓存抛弃的时候这个碎片就会被销毁。直到他又一次被缓存或者当他可见的时候才会执行onCreateView方法。
>
>    <https://www.jianshu.com/p/1cbaf784c29c>

#### requestLayout，invalidate，postInvalidate区别与联系

> 子View调用requestLayout方法，会标记当前View及父容器，同时逐层向上提交，直到ViewRootImpl处理该事件，ViewRootImpl会调用三大流程，从measure开始，对于每一个含有标记位的view及其子View都会进行测量、布局、绘制。
>
> invalidate()用来重绘UI，需要在UI线程调用。
>
> postInvalidate()也是用来重新绘制UI,它可以在UI线程调用，也可以在子线程中调用，postInvalidate()方法内部通过Handler发送了一个消息将线程切回到UI线程通知重新绘制，并不是说postInvalidate()可以在子线程更新UI,本质上还是在UI线程发生重绘，只不过我们使用postInvalidate()它内部会帮我们切换线程。
>
> 一般来说，如果View确定自身不再适合当前区域，比如说它的LayoutParams发生了改变，需要父布局对其进行重新测量、布局、绘制这三个流程，往往使用requestLayout。而invalidate则是刷新当前View，使当前View进行重绘，不会进行测量、布局流程，因此如果View只需要重绘而不需要测量，布局的时候，使用invalidate方法往往比requestLayout方法更高效。
> https://blog.csdn.net/a553181867/article/details/51583060

#### Android两种虚拟机

> Dalvik虚拟机：
>
> Dalvik虚拟机采用的是JIT（Just-In-Time）编译模式，意思为即时编译。
>
> Dalvik虚拟机负责解释dex文件为机器码，如果我们不做处理的话，每次执行代码，都需要Dalvik将dex代码翻译为微处理器指令，然后交给系统处理，这样效率不高。为了解决这个问题，Google在2.2版本添加了JIT编译器，当App运行时，每当遇到一个新类，JIT编译器就会对这个类进行编译，经过编译后的代码，会被优化成相当精简的原生型指令码（即native code），这样在下次执行到相同逻辑的时候，速度就会更快。
>
> ART虚拟机：
>
> Android4.4版本以前是Dalvik虚拟机，4.4版本开始引入ART虚拟机（Android Runtime）。在4.4版本上，两种运行时环境共存，可以相互切换，但是在5.0版本以后，Dalvik虚拟机（简称DVM）则被彻底的丢弃，全部采用ART。
>
> 区别：
>
> 1. Dalvik每次都要编译再运行，ART只会安装时启动编译
> 2. ART占用空间比Dalvik大（原生代码占用的存储空间更大），就是用“空间换时间”
> 3. ART减少编译，减少了CPU使用频率，使用明显改善电池续航
> 4. ART应用启动更快、运行更快、体验更流畅、触感反馈更及时
> 5. 基于的架构不同
> 6. 执行的字节码不同

#### ADB常用命令

> adb version（查看adb version）
>
> adb devices（查看连接的设备）
>
> adb root（以root权限运行adb）
>
> adb shell pm list packages（所有应用列表）
>
> adb shell pm list packages -s（系统应用列表）
>
> adb shell pm list packages -3（第三方应用列表）
>
> adb shell dumpsys activity activities | grep mFcusedActivity（查看前台Activity）
>
> ...
>
> https://blog.csdn.net/geanwen/article/details/80505178

#### Asset目录与res目录的区别

| 区别        | assets   | res/raw   | res/drawable   |
| --------- | -------- | --------- | -------------- |
| 获取资源方式    | 文件路径+文件名 | R.raw.xxx | R.drawable.xxx |
| 是否被压缩     | NO       | NO        | YES(失真压缩)      |
| 能否获取子目录资源 | YES      | NO        | NO             |

>  res会在R.java生成索引ID，在打包的时候判断资源有没有用到，没用到的时候不会被打包进apk中(res/raw文件夹除外)，而assets不会。
>
>  res用getResource()访问，assets用AssetsManager访问。
>
>  res/raw与assets里的文件在打包的时候都不会被系统二进制编译，都被原封不动打包进APK，通常用来存放游戏资源、脚本、字体文件等。但res/raw不可以创建子文件夹，而assets可以。
>
>  res/xml会被编译成二进制文件。res/anim存放动画资源。



#### Android SQLite的使用入门

> SQLite的特性:
>
> 1. ACID事务
> 2. 零配置 – 无需安装和管理配置
> 3. 储存在单一磁盘文件中的一个完整的数据库
> 4. 数据库文件可以在不同字节顺序的机器间自由的共享
> 5. 支持数据库大小至2TB
> 6. 足够小, 大致3万行C代码, 250K
> 7. 比一些流行的数据库在大部分普通数据库操作要快
> 8. 简单, 轻松的API
> 9. 包含TCL绑定, 同时通过Wrapper支持其他语言的绑定
> 10. 良好注释的源代码, 并且有着90%以上的测试覆盖率
> 11. 独立: 没有额外依赖
> 12. Source完全的Open, 你可以用于任何用途, 包括出售它
> 13. 支持多种开发语言，C，PHP，Perl，Java，ASP.NET，Python
>
> .....
>
> https://blog.csdn.net/qq_36982160/article/details/89215226

