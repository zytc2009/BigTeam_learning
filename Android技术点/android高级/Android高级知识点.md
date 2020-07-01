[TOC]

#### 热修复的核心原理

利用Android类加载机制，把需要修复的类打包成dex文件，把这个修复过的dex文件排在dexElements最前面，ClassLoader在修复过的dex文件加载到类之后就不会再去加载错误的类了。

那art下，热修复的实现上有什么需要注意的地方？
原来aot会生成一个base.art文件，在ClassLoader创建之后就会把base.art中的类注入到缓存中去，所以会导致这些类无法修复。

#### jvm和dalvik、art的区别

那什么是基于栈的虚拟机，什么又是基于寄存器的虚拟机？


#### AIDL

> 1. 定义 AIDL 是 android Interface Dialog Launguage , 是一个android 接口 对话语言。
> 2. 作用是 为了实现进程间之间服务的通信,AIDL是Binder的一种具体应用。
> 3. 实现方式， Service Client 和 Service Service 同时 持有一个 AIDL文件，编译的时候， 会自动变成一个可以引用的Binder子类。 Client 绑定服务成功后， 获取这个子类，可以使用里面的方法。 Service 在 onBind 方法里面 返回 这个Binder 类。
> 4. 内部实现原理 是Binder ，实现了进程之间的通信。
> 5. 就像 retrofit 和okhttp 关系一样, retrofit 提供更加友好的api,真正的网络请求还是由 okhttp发起的
> 6. binder一旦died，要重连或者关闭

#### Binder，为什么选择Binder实现Android中跨进程通信

> Binder机制：
>
> 1. 为了保证进程空间不被其他进程破坏或干扰，Linux中的进程是相互独立或相互隔离的。
> 2. 进程空间分为用户空间和内核空间。用户空间不可以进行数据交互；内核空间可以进行数据交互，所有进程共用一个内核空间。
> 3. Binder机制相对于Linux内传统的进程间通信方式：（1）性能更好；Binder机制只需要拷贝数据一次，管道、消息队列、Socket等都需要拷贝数据两次；而共享内存虽然不需要拷贝，但实现复杂度高。（2）安全性更高；Binder机制通过UID/PID在内核空间添加了身份标识，安全性更高。
> 4. Binder跨进程通信机制：基于C/S架构，由Client、Server、Server Manager和Binder驱动组成。
> 5. Binder驱动实现的原理：通过内存映射，即系统调用了mmap（）函数。
> 6. Server Manager的作用：管理Service的注册和查询。
> 7. Binder驱动的作用：（1）传递进程间的数据，通过系统调用mmap()函数；（2）实现线程的控制，通过Binder驱动的线程池，并由Binder驱动自身进行管理。
> 8. Server进程会创建很多线程处理Binder请求，这些线程采用Binder驱动的线程池，由Binder驱动自身进行管理。一个进程的Binder线程池默认最大是16个，超过的请求会阻塞等待空闲的线程。
> 9. Android中进行进程间通信主要通过Binder类（已经实现了IBinder接口），即具备了跨进程通信的能力。
>
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
>

#### 事件分发

> Activity => ViewGroup => View 的顺序进行事件分发。
>
> onTouch() 执行总优先于 onClick()。
>
> - dispatchTouchEvent()
> - onTouchEvent()
> - onInterceptTouchEvent()

> https://www.jianshu.com/p/d3758eef1f72

#### LeakCanary的工作过程以及原理

> LeakCanary 主要利用了弱引用的对象, 当 GC 回收了这个对象后, 会被放进 ReferenceQueue 中;
> 在页面消失, 也就是 activity.onDestroy 的时候, 判断利用 idleHandler 发送一条延时消息, 5秒之后,
> 分析 ReferenceQueue 中存在的引用, 如果当前 activity 仍在引用队列中, 则认为可能存在泄漏, 再利用系统类 VMDebug 提供的方法, 获取内存快照,
> 找出 GC roots 的最短强引用路径, 并确定是否是泄露, 如果泄漏, 建立导致泄露的引用链;


```
Retryable.Result ensureGone(final KeyedWeakReference reference, final long watchStartNanoTime){
    //  1.. 从 retainedKeys 移除掉已经被会回收的弱引用  
    removeWeaklyReachableReferences();
    //  3.. 若当前引用不在 retainedKeys, 说明不存在内存泄漏
    if (gone(reference)) {
        return DONE;
    }
    //  4.. 触发一次gc
    gcTrigger.runGc();
    //  5.. 再次从 retainedKeys 移除掉已经被会回收的弱引用
    removeWeaklyReachableReferences();
    if (!gone(reference)) {
        //  存在内存泄漏  
        long startDumpHeap = System.nanoTime();
        long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);
        //  获得内存快照  
        File heapDumpFile = heapDumper.dumpHeap();
        if (heapDumpFile == RETRY_LATER) {
            // Could not dump the heap.
            return RETRY;
        }
        long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
        
        HeapDump heapDump = heapDumpBuilder.heapDumpFile(heapDumpFile).referenceKey(reference.key)
          .referenceName(reference.name)
          .watchDurationMs(watchDurationMs)
          .gcDurationMs(gcDurationMs)
          .heapDumpDurationMs(heapDumpDurationMs)
          .build();
        
        heapdumpListener.analyze(heapDump);
    }
    return DONE;
}
```

#### SVG、webp格式

> svg格式：
>
> svg是矢量图，这意味着svg图片由直线和曲线以及绘制它们的方法组成。当你放大一个svg图片的时候，你看到的还是线和曲线，而不会出现像素点。svg图片在放大时，不会失真，所以它非常适合用来绘制企业Logo、Icon
>
> SVG格式特点：
>
> 1. SVG 指可伸缩矢量图形 (Scalable Vector Graphics)
> 2. SVG 用来定义用于网络的基于矢量的图形
> 3. SVG 使用 XML 格式定义图形
> 4. SVG 图像在放大或改变尺寸的情况下其图形质量不会有所损失
> 5. SVG 是万维网联盟的标准
> 6. SVG 与诸如 DOM和 XSL 之类的W3C标准是一个整体
>
> webp格式：
>
> webp是谷歌开发的一种新图片格式，是同时支持有损和无损压缩的、使用直接色的、点阵图。

#### Android分包原理

> 65536问题：
>
> Davlik模式下利用dexopt工具进行优化,
> Dexopt 会把每一个类的方法id检索起来，存在一个链表结构里面，但是这个链表的长度是用一个 short 类型来保存的，导致了方法 id 的数目不能够超过65536个。
>
> 在ART模式下 ，采用的是dexoat工具，对应生成art虚拟执行可执行的.oat文件，这个是包含多个dex文件；
>
> 如何解决：
>
> 在gradle中添加MultiDex支持：
>
> ```
> multiDexEnabled true
> ```
> 执行MultiDex.install()：
>
>
> ```
> @Override protected void attachBaseContext (Context base) {
>     super.attachBaseContext(base);
>     MultiDex.install(this);
> }
> ```
>
> https://www.jianshu.com/p/c78ec01be9ae

#### AndroidStudio-Gradle多渠道打包

> 1. AndroidManifest.xml中配置如下值
>
> ```
> <meta-data
>             android:name="UMENG_CHANNEL"
>             android:value="${CHANNEL_VALUE}" />
> ```
>
> 2. 在app的build.gradle的android{}中添加如下内容：
>
>
> ```
> android {
>     flavorDimensions "default"
>     productFlavors {
>         web {}
>         baidu {}
>         c360 {}
>         qq {}
>         wandoujia {}
>         }
>
>         productFlavors.all {
>         flavor -> flavor.manifestPlaceholders = [CHANNEL_VALUE: name]
>         }
>     }
> ```
> 3. 自定义APK名称,多渠道打包：
>
>
> ```
> android.applicationVariants.all { variant ->
>         variant.outputs.all { output ->
>             variant.productFlavors.each { flavor ->
>                 def project = "cpm"
>                 def separator = "_"
>                 def buildType = variant.variantData.variantConfiguration.buildType.name
>                 def versionName = variant.versionName
>                 def versionCode = variant.versionCode
>                 def date = new Date()
>                 def formattedDate = date.format('yyyyMMdd')
>
>                 def apkName = project + separator + "v" + versionName + separator + versionCode + separator + buildType + separator + flavor.name + ".apk"
>                 if (buildType == "release") {
>                     apkName = project + separator + "v" + versionName + separator + versionCode + separator + buildType + separator + flavor.name + separator + formattedDate + ".apk"
>                 }
>
>                 output.outputFileName = apkName
>             }
>         }
>     }
>
> ```
>
> 4. 配置签名信息：
>
>
> ```
> android {
>     signingConfigs {
>             release {
>                 storeFile file(props['release.keystore'])
>                 storePassword '123456'
>                 keyAlias '123456'
>                 keyPassword '123456'
>             }
>         }
>     }
> ```
>
> 5. 执行打包命令：
>
>
> ```
> assembleRelease
> ```
>
> https://blog.csdn.net/k_bb_666/article/details/79113222

#### Android-Drawable高级用法

> 这块不多赘述了，看下面链接就好了。
>
> https://www.cnblogs.com/Free-Thinker/p/7809970.html

#### Android图像处理 - 高斯模糊的原理及实现

> 均值滤波器：
>
> 均值滤波器（Mean Filter）是最简单的一种滤波器，它是最粗糙的一种模糊图像的方法，高斯滤波是均值滤波的高级版本。实际上不同的滤波器就是通过改变卷积核（滤波器），从而改变最后的结果矩阵，中间步骤都一样，都是求加权和。均值滤波器的卷积核通常是m*m的矩阵，其中每个元素为1/(m^2)，可以看出卷积核的元素总和为1。比如3*3的均值滤波器，卷积核的每个元素就是1/9。
>
> 高斯滤波器：
>
> 高斯滤波器是均值滤波器的高级版本，唯一的区别在于，均值滤波器的卷积核的每个元素都相同，而高斯滤波器的卷积核的元素服从高斯分布。
>
> 高斯模糊的实现：
>
> Java版本
>
> 这里实现了简单版本的高斯模糊，通过使用横向和纵向的一维高斯滤波器分别对源矩阵卷积，通过设置sigma的大小能控制图片的模糊程度，值越大越模糊。但是算法速度仍比较慢，建议直接使用RenderScript版本或直接使用成熟的开源项目。
>
>
> RenderScript版本
>
> RenderScript是Android提出的一个计算密集型任务的高性能框架，能并行的处理任务，他可以充分利用多核CPU和GPU，你不需要管怎么调度你的任务，只需要管任务具体做什么。这里不深入介绍RenderScript，因为RenderScript已经提供了一个实现高斯模糊的类：ScriptIntrinsicBlur。
>

> https://blog.csdn.net/weixin_33841722/article/details/87977030

#### Android包管理机制，核心PackageManagerService

> PackageManagerService（简称 PKMS），是 Android 系统中核心服务之一，管理着所有跟 package 相关的工作，常见的比如安装、卸载应用。
>
> PackageManagerService 是在 SystemServer 进程中启动的。
>
> https://blog.csdn.net/freekiteyu/article/details/82774947

#### Window管理，核心WindowManagerService

> WindowManagerService是一个系统服务，Android framework层主要是由它和另外一个系统服务ActivityManagerService还有View所构成，这3个模块穿插交互在整个framework中。
>
> WMS也是由SystemServer启动的。
>
> WMS主要功能可以分为两个方面，一是对窗口的管理，二是对事件的管理和分发。其接口方法以AIDL的方式定义在IWinodwManager.aidl文件中，编译后会生成一个IWindowManager.java接口文件，这个接口文件定义了WMS绝大部分的功能方法。作为窗口的管理承担着，WMS中定义了许多各种不同的窗口，它们都被定义在WMS成员变量中。
>
> https://blog.csdn.net/zhangying1994/article/details/86563515

#### Android Activity启动和管理，核心ActivityManagerService

>
> ActivityManagerService(AMS)是Android中核心的服务之一，主要负责系统中四大组件的启动、切换、调度及应用程序的管理和调度等工作。
>
> ![这里写图片描述](https://img-blog.csdn.net/2018021317481510?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbWljaGFlbF95dA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
>
> https://blog.csdn.net/michael_yt/article/details/79322483

#### Context关联类

![img](https://upload-images.jianshu.io/upload_images/1417629-6b8a5c834a20c289.png?imageMogr2/auto-orient/strip|imageView2/2/w/651/format/webp)

#### 自定义LayoutManager

> 一、复写获得默认的LayoutParams对象:
>
> generateDefaultLayoutParams()，新建继承类就必须复写的。
>
> 二、复写子View 的摆放：
>
> onLayoutChildren()。需要在里面计算每一个子View摆放的位置。
>
> 1. 通过addView（）将View添加到RecyclerView里面。
> 2. measureChildWithMargins()，测量view的布局。
> 3. layoutDecorated（），将view 真正摆放到相应的位置。
>
> 三、允许RecyclerView水平或竖直滑动：
>
> 1. canScrollVertically() 或 canScrollHorizontally()，这两个默认是false，不允许滑动。
> 2. scrollVerticallyBy()，处理整体滑动时view的回收显示。
>
>
> 四、添加缓存，从而快速复用:
>
> detachAndScrapAttachedViews()移除所有，重新添加（复用），通过addView，measureChildWithMargins，layoutDecorated添加到RecyclerView 里面。 这里使用的最简单的方法，其实还可以在里面做一个当前可见的集合，然后根据上滑下滑添加某一个，性能会高，但是本质一样，都是复用。

#### Glide加载原理，与Picasso，Fresco对比。

> Glide的缓存机制，主要分为2种缓存，一种是内存缓存，一种是磁盘缓存。
> 使用内存缓存的原因是：防止应用重复将图片读入到内存，造成内存资源浪费。
> 使用磁盘缓存的原因是：防止应用重复的从网络或者其他地方下载和读取数据。
>
> 具体来讲，缓存分为加载和存储：
>
> ①当加载一张图片的时候，获取顺序：Lru算法缓存-》弱引用缓存-》磁盘缓存（如果设置了的话）。
>
> 当想要加载某张图片时，先去LruCache中寻找图片，如果LruCache中有，则直接取出来使用，并将该图片放入WeakReference中，如果LruCache中没有，则去WeakReference中寻找，如果WeakReference中有，则从WeakReference中取出图片使用，如果WeakReference中也没有图片，则从磁盘缓存/网络中加载图片。
>
> ②将缓存图片的时候，写入顺序：弱引用缓存-》Lru算法缓存-》磁盘缓存中。
>
> 当图片不存在的时候，先从网络下载图片，然后将图片存入弱引用中，glide会采用一个acquired（int）变量用来记录图片被引用的次数， 当acquired变量大于0的时候，说明图片正在使用中，也就是将图片放到弱引用缓存当中；如果acquired变量等于0了，说明图片已经不再被使用了，那么此时会调用方法来释放资源，首先会将缓存图片从弱引用中移除，然后再将它put到LruResourceCache当中。这样也就实现了正在使用中的图片使用弱引用来进行缓存，不在使用中的图片使用LruCache来进行缓存的功能。
>
> 另：从Glide4.x开始，读取图片的顺序有所改变：弱引用缓存-》Lru算法缓存-》磁盘缓存

| 对比项            | Picasso  | Glide    | Fresco     |
| -------------- | -------- | -------- | ---------- |
| 是否支持gif        | false    | true     | true       |
| 是否支持webP       | true     | true     | true       |
| 视频缩略图          | false    | true     | true       |
| 大小             | 100k     | 500 KB   | 2～3M       |
| 加载速度           | 中        | 高        | 高          |
| Disk+Men Cache | true     | true     | true       |
| Easy of use    | low      | mediun   | difficult  |
| star           | 13160    | 14709    | 12444      |
| 开发者            | Square主导 | Google主导 | Facebook主导 |

> 从上图可以看出，Picasso是弱于后两者的。所以只比较后两者即可。

> 优点：
>
> Glide:
>
> - 多种图片格式的缓存，适用于更多的内容表现形式（如Gif、WebP、缩略图、Video）
> - 生命周期集成（根据Activity或者Fragment的生命周期管理图片加载请求）
> - 高效处理Bitmap（bitmap的复用和主动回收，减少系统回收压力）
> - 高效的缓存策略，灵活（Picasso只会缓存原始尺寸的图片，Glide缓存的是多种规格），加载速度快且内存开销小（默认Bitmap格式的不同，使得内存开销是Picasso的一半）
>
>
> Fresco:
>
> - 最大的优势在于5.0以下(最低2.3)的bitmap加载。在5.0以下系统，Fresco将图片放到一个特别的内存区域(Ashmem区)
> - 大大减少OOM（在更底层的Native层对OOM进行处理，图片将不再占用App的内存）
> - 适用于需要高性能加载大量图片的场景
>
> 缺点：
>
> Glide:
>
> - java heap比Fresco高
>
> Fresco
>
> - 包较大（2~3M）
> - 用法复杂
> - 底层涉及c++领域，阅读源码深入学习难度大

> 结论：Fresco虽然很强大，但是包很大，依赖很多，使用复杂，而且还要在布局使用SimpleDraweeView控件加载图片。相对而言Glide会轻好多，上手快，使用简单，配置方便，而且从加载速度和性能方面不相上下。对于一般的APP来说Glide是一个不错的选择，如果是专业的图片APP那么Fresco还是必要的。
>
> https://blog.csdn.net/github_33304260/article/details/70213300

#### 使用Glide的时候with方法中传入Activity的上下文和Application的上下文有什么区别？

> 生命周期不同，activty是跟页面生命周期一致；Application是跟app进程一致。
> https://www.jianshu.com/p/bb08d5fb97ae

#### Retrofit的实现与原理

> 1. 性能最好，处理最快
> 2. 使用REST API时非常方便；
> 3. 传输层默认就使用OkHttp；
> 4. 支持NIO；
> 5. 拥有出色的API文档和社区支持
> 6. 速度上比volley更快；
> 7. 如果你的应用程序中集成了OKHttp，Retrofit默认会使用OKHttp处理其他网络层请求。
> 8. 默认使用Gson
>
> Retrofit是一个基于AOP思想，对RestfulApi注解进行动态代理的网络框架。
>
> Retrofit非常巧妙的用注解来描述一个HTTP请求，将一个HTTP请求抽象成一个Java接口，然后用了Java动态代理的方式，动态的将这个接口的注解“翻译”成一个HTTP请求，最后再执行这个HTTP请求。
> Retrofit的功能非常多的依赖Java反射，代码中其实还有很多细节，比如异常的捕获、抛出和处理，大量的Factory设计模式。
> Retrofit中接口设计的恰到好处，在你创建Retrofit对象时，让你有更多更灵活的方式去处理你的需求，比如使用不同的Converter、使用不同的CallAdapter，这也就提供了你使用RxJava来调用Retrofit的可能。
>
> https://blog.csdn.net/csdn_aiyang/article/details/80692384

#### Volley优缺点

> volley中为了提高请求处理的速度，采用了ByteArrayPool进行内存中的数据存储的，如果下载大量的数据，这个存储空间就会溢出，所以不适合大量的数据。
>
> 但是由于它的这个存储空间是内存中分配的，当存储的时候会先从ByteArrayPool中取出一块已经分配的内存区域,不必每次存数据都要进行内存分配，而是先查找缓冲池中有无适合的内存区域，如果有，直接拿来用，从而减少内存分配的次数，所以他比较适合大量的数据量少的网络数据交互情况。
>
> 还有一个原因是volley的线程池是基于数组实现的，即newFixedThreadPool（4）核心线程数不超过4个，也不会自动扩展，一旦大数据上传或者下载长时间占用了线程资源，后续所有的请求都会被阻塞。最后，Volley是不适合上次和下载大文件,但不代表不能处理大文件。BasicNetwork是volley处理返回response的默认实现，它是把server返回的流全部导入内存，ByteArrayPool只是一个小于4k的内存缓存池，在BasicNetwork里实现。上传和BasicNetwork应该没有多大关系，volley也是可以上传大数据的，volley也是可以下载大数据的，只是你不要使用BasicNetwork就行了。

**HttpClient**、**HttpURLConnection**、**OKHttp**和**Volley**优缺点和性能对比,如何选择？

>一、HttpClient：
>HttpClient 是Apache的一个三方网络框架，网络请求做了完善的封装，api众多，用起来比较方便，开发快。实现比较稳定，bug比较少，但是正式由于其api众多，是我们很难再不破坏兼容性的情况下对其进行扩展。所以，Android团队对提升和优化httpclient积极性并不高。android5.0被废弃，6.0逐渐删除。
>二、HttpURLConnection
>HttpURLConnection是一个多用途、轻量级的http客户端。它对网络请求的封装没有HttpClient彻底，api比较简单，用起来没有那么方便。但是正是由于此，使得我们能更容易的扩展和优化的HttpURLConnection。不过，再android2.2之前一直存在着一些令人烦的bug，比如一个人可读的inputstream调用它的close方法的时候，会使得连接池实效，通常的做法就是禁用连接池。因此，在android2.2之前建议使用稳定的HttpClient，android2.2之后使用更容易扩展和优化的HttpURLConnection。
>三、okhttp
>支持Android 2.3及其以上版本；
>支持Java JDK 1.7以上版本；
>
>okhttp是专注于提升网络连接效率的http客户端。
>1、它能实现同一ip和端口的请求重用一个socket，这种方式能大大降低网络连接的时间，和每次请求都建立socket，再断开socket的方式相比，降低了服务器服务器的压力。
>2、okhttp 对http和https都有良好的支持。
>3、okhttp 不用担心android版本变换的困扰。
>4、成熟的网络请求解决方案，比HttpURLConnection更好用。
>4、缺点，okhttp请求网络切换回来是在线程里面的，不是在主线程，不能直接刷新UI，需要我们手动处理。封装比较麻烦。
>
>四、Volley
>Volley是google在2013 io大会上推出的网络通信框架，特别适合处理数据量小，通信频繁的网络操作。优点是内部封装了异步线程，可直接在主线程请求网络，并处理返回的结果。同时可以取消请求，容易扩展。缺点是：面对大数据量的请求，比如下载表现糟糕，不支持https。Volley的底层在针对android2.3以下系统使用httpclicent，在android2.3以上采用HttpUrlConnection请求网络。



#### EventBus实现原理

> EventBus 是一款用在 Android 开发中的发布/订阅事件总线框架，基于观察者模式，将事件的接收者和发送者分开，简化了组件之间的通信操作，使用简单、效率高、体积小！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190812224935679.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190812224950815.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190812225005772.png)

> https://blog.csdn.net/xiangzhihong8/article/details/99333594

#### ButterKnife实现原理

> ButterKnife 是一个 Android 系统的 View 注入框架，能够通过『注解』的方式来绑定 View 的属性或方法。
>
> 比如使用它能够减少 findViewById() 的书写，使代码更为简洁明了，同时不消耗额外的性能。
>
> 当然这样也有个缺点，就是可读性会差一些，好在 ButterKnife 比较简单，学习难度也不大。

> 流程：
> 1. 扫描 ButterKnife 注解。
> 2. 根据注解，生成 Java 类。
> 3. 动态注入。
>
> 最后当我们执行 ButterKnife.bind(this) 时，ButterKnife 会加载上面生成的类，然后调用其 bind 方法。
>
> 这里首先调用了 findRequiredView 去寻找 R.id.text1 所对应的控件，其实相当于我们的 findViewById()
> 其次调用 castView，相当于类型转换，把找到的 View 转化为 TextView 类型
> 至此，我们就完成了一次 ButterKnife 的工作流程。
> https://www.cnblogs.com/wgha/p/5897857.html

#### RxJava实现原理、设计模式

> Rxjava基于一种扩展的观察者模式，整个模式中有4个角色(被观察者、观察者、订阅、事件)。
>
> 原理可总结为：
>
> 1. 被观察者 （Observable） 通过 订阅（Subscribe） 按顺序发送事件 给观察者 （Observer）
> 2. 观察者（Observer） 按顺序接收事件 & 作出对应的响应动作。具体如下图：
>   ![示意图](https://imgconvert.csdnimg.cn/aHR0cDovL3VwbG9hZC1pbWFnZXMuamlhbnNodS5pby91cGxvYWRfaW1hZ2VzLzk0NDM2NS05OGVjOTJkZjBhNGQ3ZTBiLnBuZw?x-oss-process=image/format,png)
>   https://blog.csdn.net/carson_ho/article/details/100112005

#### Dagger依赖注入

> Dagger2 主要是通过Java注解（Annotation）来工作的。
>
> 解决的问题：
>
> 第一：dagger 是一个依赖注入框架，首要任务当然是解决依赖注入的问题。
>
> 第二：dagger主要想通过编译时产生代码的方式来解决那些基于反射的依赖注入框架所存在的缺点，例如性能问题，开发过程中存在的问题。
>
> https://blog.csdn.net/shusheng0007/article/details/80950117

#### 热修复实现原理，解决方案

> 热修复分为三个部分，分别是Java代码部分热修复，Native代码部分热修复，还有资源热修复。
>
> 资源部分热更新直接反射更改所有保存的AssetManager和Resources对象就行（可能需要重启应用）
>
> Native代码部分也很简单，系统找到一个so文件的路径是根据ClassLoader找的，修改ClassLoader里保存的路径就行（可能需要重启应用）
>
> Java部分的话目前主流有两种方式，一种是Java派，一种是Native派。
>
> 1. java派：通过修改ClassLoader来让系统优先加载补丁包里的类
>   代表作有腾讯的tinker，谷歌官方的Instant Run，包括multidex也是采用的这种方案
>   优点是稳定性较好，缺点是可能需要重启应用
>
> 2. native派：通过内存操作实现，比如方法替换等
>   代表作是阿里的SopHix，如果算上hook框架的话，还有dexposed，epic等等
>   优点是即时生效无需重启，缺点是稳定性不好：
>   如果采用方法替换方式实现，假如这个方法被内联/Sharpening优化了，那么就失效了；inline hook则无法修改超短方法。
>   热修复后使用反射调用对应方法时可能发生IllegalArgumentException。

#### 组件化原理和解决方案

> 组件化东西太多，就不一一详细赘述了，可以参考下面链接。
>
> https://blog.csdn.net/guiying712/article/details/55213884

#### Gradle，自动化构建，持续集成相关

> 此处推荐两篇文章，自动化构建入门篇和进阶篇。
>
> https://www.jianshu.com/p/20cdcb1bce1b
> https://www.jianshu.com/p/68e148de32a7

#### Android Studio编译过程

> 流程：
>
> 1. 首先是预编译，如果主module依赖了其它module，那么被依赖的module也要进行编译。
> 2. 然后是打包资源文件。
> 3. 理配置清单文件和处理资源文件。
> 4. 编译，源码被编译成字节码。
> 5. 执行所有transform开头的任务。
> 6. 依赖的library生成.aar文件，application生成.apk文件。

> 下面是找的一张图片：
> ![这里写图片描述](https://img-blog.csdn.net/20180315113222496?watermark/2/text/Ly9ibG9nLmNzZG4ubmV0L0Nob25nWHVlOTE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

#### App启动加载过程

> 1. 点击图标后Launcher进程会通过Binder机制向AMS发起打开Activity的请求【IPC->Binder】
> 2. AMS会通过ActivityStarter处理Intent和Flag(启动模式相关的内容)，然后通过Socket通知zygote进程【IPC->Socket】
> 3. zygote进程会进行孵化(虚拟机和资源的复制)，然后fork出新进程
> 4. 然后zygote进程会通过invokeDynamicMain()方法调用到ActivityThread的main方法【ActivityThread】
> 5. main()中关于Handler相关的工作是：主线程Looper和Handler的创建、开启主线程Looper的消息循环；
> 6. main()另一工作就是创建ActivityThread对象，执行attach方法。【Application、ContentProvider】
>
>
> > 1. 先通过AMS执行bindApplication方法
> > 2. 内部会创建属于Application的ContextImpl对象，并且创建Application对象，建立两者的联系
> > 3. 创建ContentProvider，执行其onCreate()方法，并进行发布相关操作(前提是该app有ContentProvider)
> > 4. 执行Application的onCreate()方法

#### Activity启动过程

> ![img](https://upload-images.jianshu.io/upload_images/10018045-030ad27bf157e330.png)
> 具体可以先看下链接：
> https://www.jianshu.com/p/7d0d548ebbb4

#### GreenDao用法 & 原理。了解LitePal、OrmLite、DBFlow用法

> GreenDao 不再赘述了。基本使用可以参考此链接。
> https://www.jianshu.com/p/53083f782ea2
> 其它几种自己了解下就好。

#### Android targetSdkVersion 升级都需要注意什么？（权限、文件）

> 下面是一些注意点：
> https://blog.csdn.net/EaskShark/article/details/89531659
> https://www.jianshu.com/p/79db2e557455
> https://www.jianshu.com/p/5f429c3d1256

> 

#### 组件化开发

> **1. 引入组件化的原因**：项目随着需求的增加规模变得越来越大，规模的增大导致了各种业务错中复杂的交织在一起,每个业务模块之间，代码没有约束，带来了代码边界的模糊，代码冲突时有发生, 更改一个小问题可能引起一些新的问题, 牵一发而动全身，增加一个新需求，需要熟悉相关的代码逻辑，增加开发时间。
>
> - **避免重复造轮子，可以节省开发和维护的成本。**
> - **可以通过组件和模块为业务基准合理地安排人力提高开发效率。**
> - **不同的项目可以共用一个组件或模块，确保整体技术方案的统一性**
> - **为未来插件化共用同一套底层模型做准备。**
>
> **2. 组件化开发流程：就是把一个功能完整的 App 或模块拆分成 **多个子模块（Module）**，每个子模块可以**独立编译运行**，也可以任意组合成另一个新的 App 或模块，每个模块即不相互依赖但又可以相互交互，但是最终发布的时候是将这些组件合并统一成一个 apk，遇到某些特殊情况甚至可以
>
> **升级**或者**降级**
>
> https://blog.csdn.net/guiying712/article/details/55213884



### Android优化

#### ANR 定位和处理

> 可以通过查看/data/anr/traces.txt 查看 ANR 信息。 
>
> 根本原因是：主线程被卡了，导致应用在 5 秒时间未响应用户的输入事件。 
>
> 很多种 ANR 错误出现的场景： 
>
> 1） 主线程当中执行 IO/网络操作，容易阻塞。 
>
> 2） 主线程当中执行了耗时的计算。----自定义控件的时候 onDraw 方法里面经常这么 
>
> 做。
>
> （同时聊一聊自定义控件的性能优化：在 onDraw 里面创建对象容易导致内存抖动 
>
> ---绘制动作会大量不断调用，产生大量垃圾对象导致 GC 很频繁就造成了内存抖动。）内存抖动就容易造成 UI 出现掉帧卡顿的问题 
>
> 3） BroadCastReceiver 没有在 10 秒内完成处理。
>
> 4） BroadCastReceiver 的 onReceived 代码中也要尽量减少耗时的操作，建议使用 
>
> IntentService 处理。 
>
> 5） Service 执行了耗时的操作，因为 service 也是在主线程当中执行的，所以耗时操 
>
> 作应该在 service 里面开启子线程来做。 
>
> 6） 使用 AsyncTask 处理耗时的 IO 等操作。 
>
> 7） 使 用 Thread 或 者 HandlerThread 时 ， 使 用 
>
> Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND) 或 者 
>
> java.lang.Thread.setPriority （int priority）设置优先级为后台优先级，这 
>
> 样可以让其他的多线程并发消耗 CPU 的时间会减少，有利于主线程的处理。 
>
> 8） Activity 的 onCreate 和 onResume 回调中尽量耗时的操作。 
>
> 9）导出trace文件：
>
> 1. adb pull /data/anr/traces.txt d:/
> ```
> 1、adb shell 
>
> 2、cat  /data/anr/xxx   >/mnt/sdcard/yy/zz.txt   
>
> 3、exit
>
> 4、adb pull /mnt/sdcard/yy/zz.txt  d:  ,即可将文件导出到了d盘。
> ```
>
> 2. adb bugreport  /..目录
>
>    完成之后导出Zip包 解压出来 
>
>    在 /FS/data/anr/  目录中查看

#### [apk瘦包](apk瘦包/apk瘦包.md)

#### 如何对Android 应用进行性能分析以及优化?

> <img src="https://upload-images.jianshu.io/upload_images/15843920-e086723199dbb997.png" style="zoom:75%;" />
>
> https://www.jianshu.com/p/da2a4bfcba68

#### 性能优化如何分析systrace？

> Systrace原理：在系统一些关键链路（比如System Service，虚拟机，Binder驱动）插入一些信息（这里称为Label），通过Label的开始和结束来确定某个核心过程的执行时间，然后把这些Label信息收集起来得到系统关键路径的运行时间信息，进而得到整个系统的运行性能信息。
>
> https://www.jianshu.com/p/fa6cfad8ccc2

#### 用IDE如何分析内存泄漏？

> （1）、把java应用程序使用的heap dump下来。
>
> （2）、使用java heap分析工具，找出内存占用超出预期的嫌疑对象。
>
> （3）、必要时，需要分析嫌疑对象和其他对象的引用关系。
>
> （4）、查看程序的源代码，找出嫌疑对象数量过多的原因。
>
> ​    [https://blog.csdn.net/u010944680/article/details/51721532](https://links.jianshu.com/go?to=https%3A%2F%2Fblog.csdn.net%2Fu010944680%2Farticle%2Fdetails%2F51721532)

#### Java多线程引发的性能问题，怎么解决？

> 1. 消耗时间：线程的创建和销毁都需要时间，当有大量的线程创建和销毁时，那么这些时间的消耗则比较明显，将导致性能上的缺失
> 2. 非常耗CPU和内存：大量的线程创建、执行和销毁是非常耗cpu和内存的，这样将直接影响系统的吞吐量，导致性能急剧下降，如果内存资源占用的比较多，还很可能造成OOM
> 3. 容易导致GC频繁的执行：大量的线程的创建和销毁很容易导致GC频繁的执行，从而发生内存抖动现象，而发生了内存抖动，对于移动端来说，最大的影响就是造成界面卡顿
>    而针对上述所描述的问题，解决的办法归根到底就是：重用已有的线程，从而减少线程的创建。所以这就涉及到线程池（ExecutorService）的概念了，线程池的基本作用就是进行线程的复用，下面将具体介绍线程池的使用
>
> 使用线程池管理线程的优点
>
> 1. 节省系统的开销：线程的创建和销毁由线程池维护，一个线程在完成任务后并不会立即销毁，而是由后续的任务复用这个线程，从而减少线程的创建和销毁，节约系统的开销
> 2. 节省时间：线程池旨在线程的复用，这就可以节约我们用以往的方式创建线程和销毁所消耗的时间，减少线程频繁调度的开销，从而节约系统资源，提高系统吞吐量
> 3. 提高性能：在执行大量异步任务时提高了性能
> 4. 方便控制：Java内置的一套ExecutorService线程池相关的api，可以更方便的控制线程的最大并发数、线程的定时任务、单线程的顺序执行等
>    优先级线程池的优点
>
> 从上面我们可以得知，创建一个优先级线程池非常有用，它可以在线程池中线程数量不足或系统资源紧张时，优先处理我们想要先处理的任务，而优先级低的则放到后面再处理，这极大改善了系统默认线程池以FIFO方式处理任务的不灵活.

#### 启动页白屏及黑屏解决？

> 当系统启动一个APP时，zygote进程会首先创建一个新的进程去运行这个APP，但是进程的创建是需要时间的，在创建完成之前，界面是呈现假死状态的，因为用户会以为没有点到APP而再次点击，这极大的降低用户体验，Android需要及时做出反馈去避免这段迷之尴尬。于是系统根据你的manifest文件设置的主题颜色的不同来展示一个白屏或者黑屏。而这个黑（白）屏正式的称呼应该是Preview Window,即预览窗口。
>
> 1. 在Activity启动onCreate()方法，执行setContentView()时出现白屏;
> 2. 页面的窗体绘制先于资源加载，这个时候就会出现短暂的白屏;
>    选用的主题不同造成闪屏的效果不同：
>
> ```ruby
>   1. <style name="ThemeSplash"  parent="Theme.AppCompat.Light">  这种亮色系造成了白色闪屏;
>   2. <style name="ThemeSplash"  parent="ThemeOverlay.AppCompat.Dark"> 这种亮色系造成了黑色闪屏;
> ```
>
>   启动页不设置自己的布局，也就是不调用setContentView()方法，而是通过theme的方式来进行设置，这样就可以避免出现闪屏的问题。theme的相关配置如下，自己也可自行增删不需要的选项，不过最后一个item不能删除，其中最后item的资源可以使用layer_list叠加层的方式，可参考：https://www.jb51.net/article/130850.htm
>
>   <style name="SplashTheme" parent="Theme.AppCompat.Light.NoActionBar">
>
> ```
>   <item name="android:windowNoTitle">true</item>
>   <item name="android:windowFullscreen">true</item>
>   <item name="android:windowActionBar">false</item>
>   <item name="android:windowBackground">@drawable/welcome</item>
> ```
>
>   </style>

#### 启动太慢怎么解决(启动优化)？

> 在讨论如何优化App启动速度之前，我们要清楚启动的分类：
>
> - **冷启动**
> - **热启动**
> - **温启动**
>
> 冷启动指应用从零开始启动，一切资源都要从头开始获取和初始化。而其他两个状态都是系统把在后台运行的应用切换到前台。我们在讨论优化启动状态的时候一般是指冷启动，因为这样的优化也会覆盖其他两个状态的启动。
>
> 我们来看看应用在不同的状态启动时，系统和应用层面都发生了什么。
>
> ##### 冷启动
>
> 在应用冷启动之前，系统进程还没有创建应用进程，在应用启动时，系统进程会做以下三件事：
>
> - 加载启动应用。
> - 启动后展现一个空白窗口。
> - 创建应用进程。
>
> 应用进程被创建后，就进入了应用进程的主导阶段，应用进程主要做以下六件事：
>
> - 创建应用对象。
> - 启动应用主线程。
> - 创建应用主Activity。
> - 填充视图。
> - 屏幕视图布局。
> - 渲染视图。
>
> 在应用进程完成了第一次渲染后，系统进程将会用主Activity替换当前显示的默认窗口，然后用户就可以使用应用了。
>
> 如果开发者重载了[Application.oncreate()](https://link.jianshu.com?t=https://developer.android.com/reference/android/app/Application.html#onCreate())，应用将通过调用重载方法启动。此后应用将会产生UI主线程，并且主线程将会创建主Activity，从而应用进程按照[应用生命周期](https://link.jianshu.com?t=https://developer.android.com/guide/topics/processes/process-lifecycle.html)阶段执行。
>
> 在应用进程创建了Activity之后，应用将会执行如下操作：
>
> - 初始化变量。
> - 调用构造函数。
> - 调用回调函数，例如Activity.onCreate()，对应Activity的生命周期状态。
>
> 一般来说，onCreate()方法对加载时间有最大的影响，因为它需要加载和填充View，并且初始化供Activity运行的对象。
>
> ##### 热启动
>
> 在热启动时，系统所做的是把应用从后台切到前台。如果应用的所有Activity仍在内存里存在，可以避免重复初始化对象，布局创建和填充。
>
> 但是如果一些内存由于类[onTrimMemory()](https://link.jianshu.com?t=https://developer.android.com/reference/android/content/ComponentCallbacks2.html#onTrimMemory(int))的原因被回收，这些对象需要被重新创建。
>
> 而系统进程在热启动和冷启动时做工作的一样：系统进程展示一个空白的屏幕，直到应用完成渲染当前Activity。
>
> ##### 温启动
>
> 温启动复杂度介于冷启动和热启动之间，比冷启动简单，但却比热启动开销大。
>
> 有不少状态可以被称为温启动状态，如：
>
> - 用户退出应用，但是随后重新启动它，进程有可能还在继续运行，但是应用将会通过onCreate方法重新创建Activity。
> - 系统把应用从内存中清除，然后用户重新启动它。进程和Activity将会被重新创建，但是启动速度因为onCreate方法的已保存实例得到加速。
>
> 接下来我们来讨论测量应用启动的性能，注意不要使用Debug版本的应用做调试。
>
> ###### Logcat Displayed
>
> 从Android 4.4版本开始， logcat提供了一个包含Displayed的输出：
>
> ```undefined
> ActivityManager: Displayed com.android.myexample/.StartupTiming: +3s534ms (total +1m22s643ms)
> ```
>
> 这个值表示了启动应用进程和结束绘制相关Activity中间所需的时间。花费的时间包含如下部分：
>
> - 启动应用进程。
> - 初始化对象。
> - 创建和初始化Activity。
> - 填充布局。
> - 绘制应用。
>
> **基于以上分析得出如下**：
>
> - 当Application对象被重载，并且在初始化对象时执行了繁重的操作和复杂的逻辑，启动性能可能会受到影响。有些初始化可能是完全不必要的，对于一些不必要多初始化和磁盘资源读取，可以使用延迟加载对象，如使用单例模式来替换静态变量，这样只有在第一次被访问才被初始化，依赖性注入框架如[Dagger](https://link.jianshu.com?t=http://google.github.io/dagger/)也可做相应优化。
> - 视图层级越多，应用需要花费更多的时间去填充它。两个步骤可以解决这个问题：
>   - 通过减少多余的和内嵌的布局，简化视图层级。
>   - 在启动期间不需要展示给用户的布局暂时不要进行绘制填充。使用[ViewStub](https://link.jianshu.com?t=https://developer.android.com/reference/android/view/ViewStub.html)对象替代父层级，这样可以在合适的时候再对这样的布局进行绘制。
> - 在主线程里初始化所有的资源也会减慢启动的速度。可以尝试如下处理这种问题：
>   - 把资源初始化放在非主线程上执行，以便进行懒加载。
>   - 允许应用去加载和展示视图，随后更新依赖于 bitmap和其他资源的可视部分。

#### App启动崩溃异常捕捉

> 在Android开发中在所难免的会出现程序crash，俗称崩溃。用户的随意性访问出现测试时未知的Bug导致我们的程序crash，此时我们是无法直接获取的错误log的，也就无法修复Bug。这就会极大的影响用户体验，此时我们需要注册一个功能来捕获全局的异常信息，当程序出现crash信息，我们把错误log记录下来，上传到服务器，以便于我们能及时修复bug。实现这个功能我们需要依赖于UncaughtExceptionHandler这个类，UncaughtExceptionHandler是一个接口，在Thread中。里面只有一个方法uncaughtException。当我们注册一个UncaughtExceptionHandler之后，当我们的程序crash时就会回调uncaughtException方法，而uncaughtException方法带有两个参数，参数中就存放这crash信息。

#### 自定义View注意事项

> 减少不必要的invalidate()方法。
>
> （1）降低刷新频率
>
> （2）使用硬件加速
>
> （3）初始化时创建对象；不要在onDraw方法内创建绘制对象，一般都在构造函数里初始化对象。
>
> （4）状态存储和恢复：如果内存不足时，而恰好我们的Activity置于后台，不行被重启，或者用户旋转屏幕造成Activity重启，我们的View也应该尽量的去保存自己的属性。

#### 现在下载速度很慢,试从网络协议的角度分析原因,并优化(提示：网络的5层都可以涉及)。

#### Https请求慢的解决办法（提示：DNS，携带数据，直接访问IP）

> Http耗时 = TCP握手
>
> HTTPS耗时 = TCP握手 + SSL握手

#### 如何保持应用的稳定性

需要借助内存分析工具防止内存泄漏。

#### RecyclerView和ListView的性能对比

> https://www.jianshu.com/p/171b20389634

#### ListView的优化

> ①、如果item中有图片一定要用异步加载。而且里面的图片尽量要用缩略图或者小图。
>
> ②、判断手势，快速滑动时不加载里面的图片。
>
> ③、要对数据进行分页加载。
>
> ④、item的布局层级要越少越好。

#### RecycleView优化

> 给item设置点击事件和长按事件。
>
> 给item中的控件设置点击事件和长按事件。

#### View渲染

> <img src="https://img-blog.csdn.net/20180525122509697?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NzZG4xMTI1NTUwMjI1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" style="zoom:67%;" />
>
> https://blog.csdn.net/csdn1125550225/article/details/80401365

#### Bitmap如何处理大图，如一张30M的大图，如何预防OOM？

> 1. 需要将这张大图进行压缩。
> 2. 使用图片缓存。
>
> https://blog.csdn.net/guolin_blog/article/details/9316683

#### 如何模拟系统回收App页面？

> 开发者选项 - 应用 - 不保留活动。开启后，当页面不可见时（比如推到后台或者启动新页面），就会被系统杀死。我们可以利用这个选项来模拟系统杀死页面情况。

### Kotlin

#### Kotlin 中注解 @JvmOverloads 的作用？

> 在Kotlin中@JvmOverloads注解的作用就是：在有默认参数值的方法中使用@JvmOverloads注解，则Kotlin就会暴露多个重载方法

#### Kotlin中List与MutableList的区别？

> List：有序接口，只能读取，不能更改元素；
> MutableList：有序接口，可以读写与更改、删除、增加元素。

#### Kotlin中的Unit？它和Java中的void有什么区别？

> 相同点：两点概念相同；
> 不同点：Unit 是一个类，继承自 Any 类，单例（目的在于函数返回 Unit 时避免分配内存），正因为 Unit 是一个普通的对象（这里指用 object 关键字定义的单例类型），所以可以调用它的 toString() 方法：结果一定是 "Kotlin.Unit"（源代码写死了）

#### Kotlin中集合遍历有哪几种方式？

> for,foreach,while,do while,递归,还有集合的高阶方法

#### Kotlin中的数据类型有隐式转换吗？为什么？

> kotlin中没有所谓的'基本类型'，本质上不存在拆装箱过程，所有的基本类型都可以想象成Java中的包装类型，所以也不存在隐式转换，对应的都是强类型，一旦声明之后，所有转换都是显示转换。

#### Kotlin中的Any与Java中的Object有何异同？

> 同：都是顶级父类；
> 异：成员方法不同，Any只声明了toString()、hashCode()和equals()作为成员方法。
>
> 我们思考下，为什么 Kotlin 设计了一个 Any ？
>
> 当我们需要和 Java 互操作的时候，Kotlin 把 Java 方法参数和返回类型中用到的 Object 类型看作 Any，这个 Any 的设计是 Kotlin 兼容 Java 时的一种权衡设计。所有 Java 引用类型在 Kotlin 中都表现为平台类型。当在 Kotlin 中处理平台类型的值的时候，它既可以被当做可空类型来处理，也可以被当做非空类型来操作。试想下，如果所有来自Java的值都被看成非空，那么就容易写出比较危险的代码。反之，如果 Java 值都强制当做可空，则会导致大量的null检查。综合考量，平台类型是一种折中的设计方案。

#### Kotlin中该如何安全地处理可空类型？

```
fun a(tag: String?, type: String) {
    if (tag != null && type != null){
        // do something
    }
}

a?.let{}
a?.also{}
a?.run{}
a?.apply{}

a?.let{
    b?.let{
        //do something
    }
}

```
> 补充一点，尽量不要出现空，也就不需要处理空，参考 Java 的空对象模式

#### Kotlin中可见型修饰符有哪些？相比于Java有什么区别？

> java：public，protected，default，private
>
> kotlin: private，protected，internal，public（默认）
>
> 1.private、protected、public是和java中的一样的。
> 不同的是java中默认是default修饰符（包可见），而kotlin存在internal修饰符（模块内部可见）。
> 2.kotlin可以直接在文件顶级声明方法、变量等。
> 比如：
>
>
> ```
> package foo
>
> fun baz() { ... }
> class Bar { ... }
> ```
> 其中protected不能用来修饰在文件顶级声明的类、方法、变量等。
>
> 3.变量的get和set方法可以有不同的修饰符
>
> 4.方法、变量的重写默认不改变可见性（跟随父类）
>
> 5.局部变量、方法、类不能使用可见性修饰符
>
> 6.构造方法默认是public修饰，可以使用可见性修饰符修饰constructor关键字来改变构造方法的可见性。

#### Kotlin中实现单例的几种常见方式

> 饿汉式：
>
> ```
> object StateManagementHelper {
>
>     fun init() {
>         //do some initialization works
>
>     }
> }
> ```
>
> 懒汉式：
>
> ```
> class StateManagementHelper private constructor(){
>     
>     companion object {
>         private var instance: StateManagementHelper? = null 
>             @Synchronized get() {
>             if (field == null)
>                 field = StateManagementHelper()
>             return field
>         }
>     }
>
>     fun init() {
>         //do some initialization works
>         
>     }
> }
> ```
>
> 双重检测：
>
> ```
> class StateManagementHelper private constructor(){
>
>     companion object {
>         val instance: StateManagementHelper 
>                 by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { StateManagementHelper() }
>     }
>
>     fun init() {
>         //do some initialization works
>
>     }
> }
> ```
>
> 静态内部类：
>
> ```
> class StateManagementHelper private constructor(){
>
>     companion object {
>        val INSTANCE = StateHelperHolder.holder
>     }
>     
>     private object StateHelperHolder {
>         val holder = StateManagementHelper()
>     }
>
>     fun init() {
>         //do some initialization works
>         
>     }
> }
> ```

#### Kotlin中，什么是内联函数？有什么作用？

> 对比java：
>
> 函数反复调用时，会有压栈出栈的性能消耗。
>
> kotlin优化 内联函数 用来解决 频繁调用某个函数导致的性能消耗。
>
> 使用 inline标记
> 内联函数，调用非内联函数会报错，，需要加上noinline标记
> noinline，让原本的内联函数形参函数不是内联的，保留原有数据特征。
>
> crossinline 非局部返回标记
>
> 为了不让lamba表达式直接返回内联函数，所做的标记
> 相关知识点：我们都知道,kotlin中,如果一个函数中,存在一个lambda表达式，在该lambda中不支持直接通过return退出该函数的,只能通过return@XXXinterface这种方式
> reified 具体化泛型
> java中，不能直接使用泛型的类型。
>
> kotlin可以直接使用泛型的类型。
>
> 使用内联标记的函数，这个函数的泛型，可以具体化展示，所有能解决方法重复的问题。
>
> 注：inline 真正发挥它的作用的是在包含 lambda 参数的函数中使用 inline 注解，这时才会真正的起到它的节省开销的作用，因为 kotlin 中有大量的高阶函数。

#### Kotlin中，何为解构？该如何使用？

> 给一个包含N个组件函数（component）的对象分解为替换等于N个变量的功能，而实现这样功能只需要一个表达式就可以了。
>
> 有时把一个对象 解构 成很多变量会很方便，例如:
>
> val (name, age) = person
>
> 这种语法称为 解构声明 。一个解构声明同时创建多个变量。 我们已经声明了两个新变量： name 和 age，并且可以独立使用它们：
>
> println(name)
>
> println(age)
>
> 一个解构声明会被编译成以下代码：
>
> val name = person.component1()
>
> val age = person.component2()

#### Kotlin中的Coroutines，它与线程有什么区别？有哪些优点？

> 这个可以这么理解，Coroutines 本身就是一个轻量级的线程，简单来说协程是一个非抢占式或者说协作式的计算机程序并发调度的实现（手动问好脸），什么是轻量级的呢？这个就要和线程对比理解了，我们常见的大多数线程，以操作系统方面来说线程是映射到内核的线程的，也就是说线程中代码的逻辑在线程抢到CPU资源的时间时才可以执行，否则就是歇着，那么Coroutines说是一个轻量级的线程的意思是，Coroutines并不会映射程内核的线程或者其他重资源，他的调度在用户态就可以搞定，任务之间的调度并非是抢占式的，而是协作式的，Coroutines可以主动挂起和恢复执行，所以说Coroutines是轻量级的线程，优点：可以替换回调地狱、解决RxJava这类复杂的调度逻辑、替换线程（Android开发中耗费资源的任务需要在线程中执行，然而只能在UI线程更新，Coroutines可以通过上下文切换线程，既可以执行耗时任务也可以更新UI线程）、在和Retrofit等框架配合使用、可以通过拦截上下文、自定义拦截器等实现很多想要的东西。

#### Kotlin中的Sequence，为什么它处理集合操作更加高效？

> 集合操作低效在哪？
>
> 处理集合时性能损耗的最大原因是循环。集合元素迭代的次数越少性能越好。
>
> 我们写个例子：
>
>
> ```
> list
>   .map { it ++ }
>   .filter { it % 2 == 0 }
>   .count { it < 3 }
> ```
>
>
> 反编译一下，你会发现：Kotlin编译器会创建三个while循环。
>
> Sequences 减少了循环次数:
>
> Sequences提高性能的秘密在于这三个操作可以共享同一个迭代器(iterator)，只需要一次循环即可完成。Sequences允许 map 转换一个元素后，立马将这个元素传递给 filter操作 ，而不是像集合(lists) 那样，等待所有的元素都循环完成了map操作后，用一个新的集合存储起来，然后又遍历循环从新的集合取出元素完成filter操作。
>
> Sequences 是懒惰的:
>
> 上面的代码示例，map、filter、count 都是属于中间操作，只有等待到一个终端操作，如打印、sum()、average()、first()时才会开始工作，不信？你跑下下面的代码？
>
>
> ```
> val list = listOf(1, 2, 3, 4, 5, 6)
> val result = list.asSequence()
>         .map{ println("--map"); it * 2 }
>         .filter { println("--filter");it % 3  == 0 }
> println("go~")
> println(result.average())
> ```

#### Kotlin与Java混合开发时需要注意哪些问题？

> Kotlin 默认是非null 类型，java 返回 null，kotlin 需要添加? 表示可为null。
>
> kotlin 使用!! 时，要确认变量不为null, 不然会直接抛异常。
>
> kotlin调用Java的方法或者属性，不能推断出是否为空，除非加上空或者非空注解。

#### Kotlin 中 infix 关键字？

> Kotlin允许在不使用括号和点号的情况下调用函数，那么这种函数被称为 infix函数。
>
> 使用中缀需要满足三个条件：
>
> 1.只有一个参数。
>
> 2.在方法前必须加infix关键字。
>
> 3.必须是成员方法或扩展方法。
>
> 中缀函数调用的优先级低于算术操作符、类型转换以及 rangeTo 操作符。
>
> 中缀函数调用的优先级高于布尔操作符 && 与 ||、is- 与 in- 检测以及其他一些操作符。

#### Kotlin中的 data 关键字的理解？相比于普通类有哪些特点？

> 在编程过程中，我们肯定会经常创建一些model模型类。 如果使用Java来写的话，在这些类中一般都需要写一大堆方法，例如
>
> ```
> public class People {
>
>     private String name;
>     private int age;
>
>     public String getName() {
>         return name;
>     }
>
>     public void setName(String name) {
>         this.name = name;
>     }
>
>     public int getAge() {
>         return age;
>     }
>
>     public void setAge(int age) {
>         this.age = age;
>     }
>
>     @Override
>     public String toString() {
>         return "People{" +
>                 "name='" + name + '\'' +
>                 ", age=" + age +
>                 '}';
>     }
>
>     @Override
>     public boolean equals(Object o) {
>         if (this == o) return true;
>         if (o == null || getClass() != o.getClass()) return false;
>         People people = (People) o;
>         return age == people.age &&
>                 Objects.equals(name, people.name);
>     }
>     
> }
> ```
> 在 Kotlin 中，我们如果想创建一个类似的模型类，只需要使用data关键字
>
> data class People(val name: String, val age: Int)



### 移动开发外围

#### 性能分析工具：Memory Monitor

> 一图看懂 Memory Monitor
>
> <img src="https://upload-images.jianshu.io/upload_images/851999-b00b15a281537a38.png" style="zoom:67%;" />
>
> 具体使用详见：
>
> https://www.jianshu.com/p/080473ae050b

#### 性能追踪及方法执行分析： TraceView

> TraceView 工具
>
> <img src="https://upload-images.jianshu.io/upload_images/1836169-b5a02c64fdaa044c.png" style="zoom:67%;" />

#### 视图分析：Hierarchy Viewer

> Hierarchy Viewer是随AndroidSDK发布的工具，位置在tools文件夹下，名为hierarchyviewer.bat。它是Android自带的非常有用而且使用简单的工具，可以帮助我们更好地检视和设计用户界面(UI)，绝对是UI检视的利器，下面来详细介绍如何在Android Studio开发环境下使用Hierarchy Viewer。
>
> 使用步骤：
>
> 
>
> 1.启动模拟器，通过模拟器运行你的应用
>
>   ※HierarchyViewer是无法连接真机进行调试
>
>   关于HierarchyViewer，可参考官方文档[http://developer.android.com/tools/debugging/debugging-ui.html](https://link.jianshu.com?t=http://developer.android.com/tools/debugging/debugging-ui.html)
>
>   文档中提及To preserve security, Hierarchy Viewer can only connect to devices running a developer version of the Android system.即出于安全考虑，Hierarchy Viewer只能连接Android开发版手机或是模拟器(准确地说，只有ro.secure参数等于0且ro.debuggable等于1的android系统)。Hierarchy Viewer在连接手机时，手机上必须启动一个叫View Server的客户端与其进行socket通信。而在商业手机上，是无法开启View Server的，所以Hierarchy Viewer是无法连接到普通的商业手机。
>
> 可以通过命令检验一台手机是否开启了View Server：adb shell service call window 3
>
> 若返回值是：Result: Parcel(00000000 00000000 '........')" 说明View Server处于关闭状态
>
> 若返回值是：Result: Parcel(00000000 00000001 '........')" 说明View Server处于开启状态
>
> 若是一台可以打开View Server的手机（Android开发版手机 、模拟器or 按照本帖步骤给系统打补丁的手机），我们可以使用以下命令打开View Server：
>
> ```ruby
> adb shell service call window 1 i32 4939
> ```
>
> 使用以下命令关闭View Server：
>
> ```ruby
> adb shell service call window 2 i32 4939
> ```
>
> <img src="https://upload-images.jianshu.io/upload_images/1502545-5477cd2575021e94.png" style="zoom:67%;" />
>
> 2.打开Android Device Monitor（两种打开方式）
>
> 
>
> <img src="https://upload-images.jianshu.io/upload_images/1502545-8796ce4ff253ce04.png" style="zoom:67%;" />
>
> 3.进入Android Device Monitor界面，打开HierarchyViewer
>
> 
>
> <img src="https://upload-images.jianshu.io/upload_images/1502545-0358e8b52c8c2240.png" style="zoom:67%;" />
>
> 4.加载出当前Activity的节点，可选中进行分析
>
> 
>
> <img src="https://upload-images.jianshu.io/upload_images/1502545-b4c672fb6365274b.png" style="zoom:67%;" />
>
> **例子：**
>
> <img src="https://upload-images.jianshu.io/upload_images/1502545-7c5b4918d34a08c1.png" style="zoom:67%;" />

#### ApkTool- 用于反向工程Android Apk文件的工具

> 获取资源文件
>
> 方法步骤
>
> ```ruby
> 1.下载apktool最新版本 ，地址：https://ibotpeaches.github.io/Apktool/install/
> 2.配置好java环境后在开始菜单中输入cmd，打开dos命令窗口，定位到apktool目录
> ```
>
> <img src="https://upload-images.jianshu.io/upload_images/5213938-5df280041c36385b.png" style="zoom:67%;" />
>
> ```ruby
> 3.然后执行命令java -jar apktool.jar d -f XXXX.apk文件所在的路径。然后程序就会开始反编译。反编译完成会在该文件夹内生成一个和apk同名的文件夹，apk反编译完的内容即存在于该文件夹内。
> ```
>
> <img src="https://upload-images.jianshu.io/upload_images/5213938-142ba1489b4140a9.png" style="zoom:67%;" />

#### Lint- Android lint工具是一个静态代码分析工具

> 作为移动应用开发者，我们总希望发布的apk文件越小越好，不希望资源文件没有用到的图片资源也被打包进apk，不希望应用中使用了高于minSdk的api，也不希望AndroidManifest文件存在异常，lint就能解决我们的这些问题。Android lint是在ADT 16提供的新工具，它是一个代码扫描工具，能够帮助我们识别代码结构存在的问题，主要包括：
>
> 1）布局性能（以前是 layoutopt工具，可以解决无用布局、嵌套太多、布局太多）
>
> 2）未使用到资源
>
> 3）不一致的数组大小
>
> 4）国际化问题（硬编码）
>
> 5）图标的问题（重复的图标，错误的大小）
>
> 6）可用性问题（如不指定的文本字段的输入型）
>
> 7）manifest文件的错误
>
>  Android lint可以解决如上的问题，当然还有更多，具体的可以参考[Android Lint Checks](http://tools.android.com/tips/lint-checks)。Android官方也总结了lint能解决的问题，如下图。
>
> <img src="https://img-blog.csdn.net/20131227095352671" style="zoom:67%;" />
>
>  Android Studio 中内置了 Lint，我们小手一点就可以直接使用。
>
> **Lint 的使用路径：**
> **工具栏 -> Analyze -> Inspect Code…**
>
> 点击 **Inspect Code** 后会弹出检查范围的对话框：
>
> <img src="https://img-blog.csdn.net/20170106190648357?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 默认是检查整个项目，我们可以点击 **Custom scope** 自定义检查范围。
>
> 点击右边的下拉框，会出现以下选择：
>
> <img src="https://img-blog.csdn.net/20170106190728058?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 分别有：
>
> Project Files：所有项目文件
> Project Production Files：项目的代码文件
> Project Test Files：项目的测试文件
> OpenFiles：当前打开的文件
> Module ‘app’：主要的 app 模块
> Current File：当前文件
> …
> 除了内置的选项我们还可以自己选择特定的类进行检查，点击下图中的红色框部分：
>
> <img src="https://img-blog.csdn.net/20170106190813124?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 会弹出自定义范围选择框，默认是空的，我们可以点击左上角的“+”号新增一个检查范围：
>
> <img src="https://img-blog.csdn.net/20170106190835796?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> \- **Local**：只能当前项目使用
> \- **Shared**：其他 Android Studio 项目也可以使用
>
> 我们选择 Shared，然后起个帅气的名字 “ShixinCuteLint”，默认按项目显示，这时检查的文件数为 0 ：
>
> <img src="https://img-blog.csdn.net/20170106190903156?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 上图中右边的四个按钮表示要操作的类型：
>
> - Include：包括当前文件夹内的文件，但不包括他的子文件夹
> - Include Recursively：包括当前文件夹以及它的子文件夹内所有的文件夹，递归添加
> - Exclude：移除当前文件夹，不包括子文件夹
> - Exclude Recursively：移除当前文件夹及所有子文件夹
>   我们点击左边的 app 文件夹后，点击右边的 Include Recursively 按钮，把 app 下的所有文件添加到检查列表：
>
> <img src="https://img-blog.csdn.net/20170106191237802?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 可以看到，这时 app 下的文件都变绿了，总共有 689 个文件夹要扫描。
>
> 点击 OK 进行检测，稍等一会儿，会弹出 Inspection 对话框，显示检查结果，没想到我的代码居然有 1769 个警告！这数字触目惊心啊：
>
> <img src="https://img-blog.csdn.net/20170106191043075?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 我们主要关注红框内的警告，先来看看我的代码 Performance 有什么问题：
>
> <img src="https://img-blog.csdn.net/20170106191307888?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTI0MDg3Nw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" style="zoom:67%;" />
>
> 上图可以看到，Lint 真是神器，可以帮我们发现自己忽略或者没有意识到的问题，尤其是性能方面，如果你觉得自己代码想优化又不知道从何做起，不妨让 Lint 给你指指路。

#### Dex2Jar- 使用android .dex和java .class文件的工具

> 将apk反编译成[Java](https://link.jianshu.com/?t=http://lib.csdn.net/base/javaee)源码（classes.dex转化成jar文件）
>
> 将要需要反编译的APK后缀名改为.rar或则 .zip，并解压，得到其中的额classes.dex文件（它就是java文件编译再通过dx工具打包而成的），将获取到的**classes.dex放到之前解压出来的工具dex2jar-0.0.9.15文件夹内**
>
> 在命令行下定位到dex2jar.bat所在目录，输入dex2jar.bat classes.dex，效果如下：
>
> <img src="https://upload-images.jianshu.io/upload_images/642281-e5a1716864aa3f87.png" style="zoom:67%;" />
>
> **在改目录下会生成一个**classes_dex2jar.jar的文件，然后打开工具jd-gui文件夹里的jd-gui.exe，之后用该工具打开之前生成的classes_dex2jar.jar文件，便可以看到源码了

#### Git详细教程

> 1. Git 常用命名
>
>    ```ruby
>    git init：仓库的初始化
>
>    git status：查看当前仓库的状态
>
>    git diff：查看仓库与上次修改的内容
>
>    git add：将文件放进暂存区
>
>    git commit：提交代码
>
>    git clone：克隆代码
>
>    git bransh：查看当前分支
>
>    git checkout：切换当前分支
>    ```

> 1. 解决冲突三连 
>
>    ```
>    git stash 
>    git pull 
>    git stash pop 
>    ```
>
>    更多可参考廖雪峰Git 教程网站或其他网站描述
>
>    1. https://www.liaoxuefeng.com/wiki/896043488029600/896067008724000
>    2. https://juejin.im/post/5a2cdfe26fb9a0452936b07f


#### 备注：
>    SparseArray和ArrayMap原理和优点  胡丹待整理
