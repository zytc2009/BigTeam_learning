### Zygote启动

Zygote作用:

启动SystemServer

孵化应用进程



启动三段式：

进程启动-》准备工作-》Loop



#### **Zygote启动**：

init进程（init.rc） ->fork + exec 

 启动zygote和servicemanager，surfaceflinger等服务

Zygote Native:

启动虚拟机-》注册android JNI函数-》进入java世界，调用Zygoteinite.main()方法

ZygoteInit.main方法主要做四件事：1.注册soceket，2.启动类加载器，预加载资源，3.启动SystemServer，在try catch中执行MethodAndArgsCaller.run；在这里通过反射执行SystemServer.main()方法  4.进入runSelectLoop循环处理事件。

**binder启动**：
打开binder驱动，映射内存，注册binder线程，进入binder loop循环

#### **SystemServer启动过程**：

1.ZygoteInit.startSystemServer -> ZygoteInit.handleSystemServerProcess -> RuntimeInit.zygoteInit

>   RuntimeInit.zygoteInit : 
>
>   	 nativeZygoteInit（）- 》ProcessState->startThreadPool() 启用binder机制
>	
>    	applicationInit()  -》 SystemServer.main()

2.SystemServer.main()->new SystemServer().run():

> 1.Looper.prepareMainLooper(); 
>
> 2.createSystemContext(),创建ActivityThread，通知主线程application初始化; 
>
> 3.创建SystemServiceManager；
>
> 4.启动bootstrap,core,other服务
>
> 5.进入looper循环

```
ActivityThread：
private void attach(boolean system) {
	...
   	final IActivityManager mgr = ActivityManager.getService();
   	try {//这里是关键。mAppThread是一个ApplicationThread实例，通过AMS的attachApplication方法将mAppThread对象关联到了AMS。
        mgr.attachApplication(mAppThread);
    } catch (RemoteException ex) {
       throw ex.rethrowFromSystemServer();
    }
 }
 
IActivityManager的实例是一个ActivityManagerService通过Binder机制得到的远程对象，而ActivityManagerService即AMS是运行在系统进程，主要完成管理应用进程的生命周期以及进程的Activity，Service，Broadcast和Provider等。通过AMS的attachApplication方法将mAppThread对象关联到了AMS。
而AMS通过调用mAppThread的相关方法进行Application的创建、生命周期的管理和Activity的创建、生命周期管理等

ApplicationThread:
public final void bindApplication(String processName, ApplicationInfo appInfo ...) {
    。。。
    AppBindData data = new AppBindData();
    data.processName = processName;
    data.appInfo = appInfo;
    ...
    sendMessage(H.BIND_APPLICATION, data);
}

//真正的创建Application，又回到了ActivityThread类
ActivityThread 类:
private void handleBindApplication(AppBindData data) {
    ...
    mInstrumentation = (Instrumentation)
        cl.loadClass(data.instrumentationName.getClassName())
        .newInstance();
    //通过反射初始化一个Instrumentation仪表。
    ...
    Application app;
    try {
         //通过LoadedApk的makeApplication创建Application实例
        app = data.info.makeApplication(data.restrictedBackupMode, null);
        mInitialApplication = app;   
        if (!data.restrictedBackupMode) {
                if (!ArrayUtils.isEmpty(data.providers)) {
                   //处理contentProviders
                    installContentProviders(app, data.providers);
                    //对于有contentProvider的进程，我们需要缺包JIT启用 "at some point".
                    mH.sendEmptyMessageDelayed(H.ENABLE_JIT, 10*1000);
                }
         }
        ...
        //让Instrumentation仪表调用Application的onCreate()方法
        mInstrumentation.callApplicationOnCreate(app);
        ...
    }
    ...
}

```

**ActivityThread**

负责通知AMS，创建application，启动activity，启动service等



**注意细节**：

zygote fork要单线程操作，可以暂停其他线程

zygote IPC没有使用binder机制

为何zygote创建进程，而不是systemServer？AMS和zygote为啥不用binder通讯？
总结起来就是怕父进程binder线程有锁，然后子进程的主线程一直在等其子线程(从父进程拷贝过来的子进程)的资源，
但是其实父进程的子进程并没有被拷贝过来，造成死锁，所以fork不允许存在多线程。
而非常巧的是Binder通讯偏偏就是多线程，所以干脆父进程（Zgote）这个时候就不使用binder线程

### App启动流程

> 三个进程：
> Launcher进程：整个App启动流程的起点，负责接收用户点击屏幕事件，它其实就是一个Activity，里面实现了点击事件，长按事件，触摸等事件，可以这么理解，把Launcher想象成一个总的Activity，屏幕上各种App的Icon就是这个Activity的button，当点击Icon时，会从Launcher跳转到其他页面；
>
> SystemServer进程：这个进程在整个的Android进程中是非常重要的一个，地位和Zygote等同，它是属于Application Framework层的，Android中的所有服务，例如AMS, WindowsManager, PackageManagerService等等都是由这个SystemServer fork出来的。所以它的地位可见一斑。
>
> App进程：你要启动的App所运行的进程；
>
> 六个大类：
> ActivityManagerService：（AMS）AMS是Android中最核心的服务之一，主要负责系统中四大组件的启动、切换、调度及应用进程的管理和调度等工作，其职责与操作系统中的进程管理和调度模块相类似，因此它在Android中非常重要，它本身也是一个Binder的实现类。
>
> Instrumentation：监控应用程序和系统的交互；
>
> ActivityThread：应用的入口类，通过调用main方法，开启消息循环队列。ActivityThread所在的线程被称为主线程；
>
> ApplicationThread：ApplicationThread提供Binder通讯接口，AMS则通过代理调用此App进程的本地方法
>
> ActivityManagerProxy：AMS服务在当前进程的代理类，负责与AMS通信。
>
> ApplicationThreadProxy：ApplicationThread在AMS服务中的代理类，负责与ApplicationThread通信。
>

可以说，启动的流程就是通过这六个大类在这三个进程之间不断通信的过程；

我先简单的梳理一下app的启动的步骤：

（1）点击图标后Launcher进程会通过Binder机制向AMS发起打开Activity的请求【IPC->Binder】

（2）AMS得到Launcher的通知，就需要响应这个通知，主要就是新建一个Task去准备启动Activity，并且告诉Launcher你可以休息了（Paused）；

（3）Launcher得到AMS让自己“休息”的消息，那么就直接挂起，并告诉AMS我已经Paused了；

（4）AMS知道了Launcher已经挂起之后，就可以准备启动新的Activity了，通过Socket去和Zygote协商，如果ProcessRecord为空或者它的thread为空，需要创建进程，那么就会fork自身，创建一个进程，新的进程会导入ActivityThread类，这就是每一个应用程序都有一个ActivityThread与之对应的原因；

（5）进程创建好了，通过调用上述的ActivityThread的main方法，创建主线程Looper和Handler，创建ActivityThread对象，在attach方法把ApplicationThread注册到AMS，然后开启消息循环队列，这也是主线程默认绑定Looper的原因；   

```
 public static void main(String[] args) {
        Looper.prepareMainLooper();
        ActivityThread thread = new ActivityThread();
        thread.attach(false, startSeq);
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
}
```

（6）这时候，App还没有启动完，要永远记住，四大组建的启动都需要AMS去启动，必须将上述的应用进程信息注册到AMS中，AMS再在堆栈顶部取得要启动的Activity，通过一系列链式调用去完成App启动；

下面这张图很好的描述了上面的六大步：

![app启动2](..\images\app启动2.png)



​     ![app启动2](..\images\app启动.png)

#### **Application初始化**：

ActvityThread.attach()向ams报告，AMS添加消息BIND_APPLICATION到应用主线程处理
主handler的handleBindApplication():
**创建application，   attachBaseContext(),   安装ContentProvider，调用app的onCreate()**

```
private void handleBindApplication(AppBindData data) {
    ...
    Application app;
    app = data.info.makeApplication(data.restrictedBackupMode, null);
    if (!data.restrictedBackupMode) {
        if (!ArrayUtils.isEmpty(data.providers)) {
             installContentProviders(app, data.providers);
        }
    }
    mInstrumentation.callApplicationOnCreate(app);
    ...
}
LoadApk类：
public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
        if (mApplication != null) {
            return mApplication;
        }
        Application app = null;
        String appClass = mApplicationInfo.className;
        try {
            java.lang.ClassLoader cl = getClassLoader();
            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
            //创建Application，执行attach(context)
            app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
        } catch (Exception e) {
        }
        mActivityThread.mAllApplications.add(app);
        mApplication = app;
        if (instrumentation != null) {
            try {
                instrumentation.callApplicationOnCreate(app);
            } catch (Exception e) {
            }
        }
        return app;
 }
```

所以生命周期方法不要有耗时操作

> Application作用：
> 保存应用进程的全局变量
> 初始化操作
> 提供应用上下文

#### **activity启动**:

ContextImpl的startActivity，然后内部会通过Instrumentation来尝试启动Activity，这是一个跨进程过程，它会调用ams的startActivity方法，当ams校验完activity的合法性后，会通过ApplicationThread回调到我们的进程，这也是一次跨进程过程，而applicationThread就是一个binder，回调逻辑是在binder线程池中完成的，所以需要通过Handler H将其切换到ui线程，第一个消息是LAUNCH_ACTIVITY，它对应handleLaunchActivity，在这个方法里完成了Activity的创建和启动，接着，在activity的onResume中，activity的内容将开始渲染到window上，然后开始绘制直到我们看见



**启动过程**：**创建activity对象，准备好application，创建ContextImpl，attach上下文，生命周期回调**

    AMS   《--  attachApplication        应用      
           bindApplication -》   创建application   
         scheduleLaunchActivity 发消息给主线程
         
    ActivityThread类：
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

生命周期：attach->onCreate->onStart->onResume

> Activity启动过程
>
> ![img](https://upload-images.jianshu.io/upload_images/10018045-030ad27bf157e330.png)
>
> 具体可以先看下链接：
> https://www.jianshu.com/p/7d0d548ebbb4



参考文章：

1.https://blog.csdn.net/pgg_cold/java/article/details/79491791