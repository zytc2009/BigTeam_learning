### Zygote启动

Zygote作用:

启动SystemServer

孵化应用进程



启动三段式：

进程启动-》准备工作-》Loop



**Zygote启动**：

init进程（init.rc） ->fork + exec 

 启动zygote和servicemanager，surfaceflinger等服务，servicemanager服务会启动binder机制

Zygote Native:

启动虚拟机-》注册android JNI函数-》进入java世界，调用Zygoteinite.main()方法

ZygoteInit.main方法主要做四件事：1.注册soceket，2.启动类加载器，预加载资源，3.启动SystemServer进程，经过层层调用，最终会调用到SystemServer.main方法。4进入runSelectLoop循环处理事件。

**SystemServer**：

1.Looper.prepareMainLooper(); 2.createSystemContext(),创建ActivityThread; 3. 创建SystemServiceManager 4.启动bootstrap,core,other服务 5.进入looper循环



**注意细节**：

zygote fork要单线程操作，可以暂停其他线程

zygote IPC没有使用binder机制

为何zygote创建进程，而不是systemServer？AMS和zygote为啥不用binder通讯？
总结起来就是怕父进程binder线程有锁，然后子进程的主线程一直在等其子线程(从父进程拷贝过来的子进程)的资源，
但是其实父进程的子进程并没有被拷贝过来，造成死锁，所以fork不允许存在多线程。
而非常巧的是Binder通讯偏偏就是多线程，所以干脆父进程（Zgote）这个时候就不使用binder线程

### App启动流程

三个进程：
Launcher进程：整个App启动流程的起点，负责接收用户点击屏幕事件，它其实就是一个Activity，里面实现了点击事件，长按事件，触摸等事件，可以这么理解，把Launcher想象成一个总的Activity，屏幕上各种App的Icon就是这个Activity的button，当点击Icon时，会从Launcher跳转到其他页面；

SystemServer进程：这个进程在整个的Android进程中是非常重要的一个，地位和Zygote等同，它是属于Application Framework层的，Android中的所有服务，例如AMS, WindowsManager, PackageManagerService等等都是由这个SystemServer fork出来的。所以它的地位可见一斑。

App进程：你要启动的App所运行的进程；

六个大类：
ActivityManagerService：（AMS）AMS是Android中最核心的服务之一，主要负责系统中四大组件的启动、切换、调度及应用进程的管理和调度等工作，其职责与操作系统中的进程管理和调度模块相类似，因此它在Android中非常重要，它本身也是一个Binder的实现类。

Instrumentation：监控应用程序和系统的交互；

ActivityThread：应用的入口类，通过调用main方法，开启消息循环队列。ActivityThread所在的线程被称为主线程；

ApplicationThread：ApplicationThread提供Binder通讯接口，AMS则通过代理调用此App进程的本地方法

ActivityManagerProxy：AMS服务在当前进程的代理类，负责与AMS通信。

ApplicationThreadProxy：ApplicationThread在AMS服务中的代理类，负责与ApplicationThread通信。

可以说，启动的流程就是通过这六个大类在这三个进程之间不断通信的过程；

我先简单的梳理一下app的启动的步骤：

（1）启动的起点发生在Launcher活动中，启动一个app说简单点就是启动一个Activity，那么我们说过所有组件的启动，切换，调度都由AMS来负责的，所以第一步就是Launcher响应了用户的点击事件，然后通知AMS

（2）AMS得到Launcher的通知，就需要响应这个通知，主要就是新建一个Task去准备启动Activity，并且告诉Launcher你可以休息了（Paused）；

（3）Launcher得到AMS让自己“休息”的消息，那么就直接挂起，并告诉AMS我已经Paused了；

（4）AMS知道了Launcher已经挂起之后，就可以放心的为新的Activity准备启动工作了，首先，APP肯定需要一个新的进程去进行运行，所以需要创建一个新进程，这个过程是需要Zygote参与的，AMS通过Socket去和Zygote协商，如果需要创建进程，那么就会fork自身，创建一个线程，新的进程会导入ActivityThread类，这就是每一个应用程序都有一个ActivityThread与之对应的原因；

（5）进程创建好了，通过调用上述的ActivityThread的main方法，这是应用程序的入口，在这里开启消息循环队列，这也是主线程默认绑定Looper的原因；

（6）这时候，App还没有启动完，要永远记住，四大组建的启动都需要AMS去启动，必须将上述的应用进程信息注册到AMS中，AMS再在堆栈顶部取得要启动的Activity，通过一系列链式调用去完成App启动；

下面这张图很好的描述了上面的六大步：

![app启动2](..\images\app启动2.png)



​     ![app启动2](..\images\app启动.png)

**activity启动**:

创建activity对象，准备好application，创建ContextImpl，attach上下文，生命周期回调

源码：
    void handleLaunchActivity(){
       Activity activity = performLaunchActivity();
       if(activity != null){
          handleResumeActivity()
       }
    }
    
    void performLaunchActivity(){
      Activity activity = mInstrumentation.newActivity(...);
      Application app = r.packetInfo.makeApplication(false, mInstrumentation);  
      Context appContext = createBaseContextForActivity(r, activity);
      activity.attach(appContext, ...);
    
      mInstrumentation.callActivityOnCreate(activity, r.state);
      activity.performStart();  
      return activity;
    }
 

    AMS   《--  attachApplication        应用      
           bindApplication -》     
         scheduleLaunchActivity 
生命周期：attach->onCreate->onStart->onResume



参考文章：

1.https://blog.csdn.net/pgg_cold/java/article/details/79491791