### Android UI渲染

#### UI优化究竟是指什么

我认为所谓的 UI 优化，应该包含两个方面：一个是效率的提升，我们可以非常高效地把 UI 的设计图转化成应用界面，并且保证 UI 界面在不同尺寸和分辨率的手机上都是一致的；另一个是性能的提升，在正确实现复杂、炫酷的 UI 设计的同时，需要保证用户有流畅的体验。

#### 屏幕与适配

作为消费者来说，通常会比较关注屏幕的尺寸、分辨率以及厚度这些指标。Android 的碎片化问题令人痛心疾首，屏幕的差异正是碎片化问题的“中心”。屏幕的尺寸从 3 英寸到 10 英寸，分辨率从 320 到 1920 应有尽有，对我们 UI 适配造成很大困难。

除此之外，材质也是屏幕至关重要的一个评判因素。目前智能手机主流的屏幕可分为两大类：一种是 LCD（Liquid Crystal Display），即液晶显示器；另一种是 OLED（Organic Light-Emitting Diode 的）即有机发光二极管。

例如 iPhone XS Max 和华为 Mate 20 Pro 使用的都是 OLED 屏幕。相比 LCD 屏幕，OLED 屏幕在色彩、可弯曲程度、厚度以及耗电都有优势。正因为这些优势，全面屏、曲面屏以及未来的柔性折叠屏，使用的都是 OLED 材质。

不过 OLED 的单价成本要比 LCD 高很多。对于屏幕碎片化的问题，Android 推荐使用 dp 作为尺寸单位来适配 UI，因此每个 Android 开发都应该很清楚 px、dp、dpi、ppi、density 这些概念。

![img](https://static001.geekbang.org/resource/image/e3/ce/e3094e900dccacb9d9e72063ca3084ce.png)

通过 **dp 加上自适应布局可以基本解决屏幕碎片化的问题**，也是 Android 推荐使用的屏幕兼容性适配方案。但是它会存在两个比较大的问题：

- 不一致性。因为 dpi 与实际 ppi 的差异性，导致在相同分辨率的手机上，控件的实际大小会有所不同。
- 效率。设计师的设计稿都是以 px 为单位的，开发人员为了 UI 适配，需要手动通过百分比估算出 dp 值。

除了直接 dp 适配之外，目前业界比较常用的 UI 适配方法主要有下面几种：

- 限制符适配方案。主要有宽高限定符与 smallestWidth 限定符适配方案，具体可以参考[《Android 目前稳定高效的 UI 适配方案》](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650826034&idx=1&sn=5e86768d7abc1850b057941cdd003927&chksm=80b7b1acb7c038ba8912b9a09f7e0d41eef13ec0cea19462e47c4e4fe6a08ab760fec864c777&scene=21#w)[《smallestWidth 限定符适配方案》](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650826381&idx=1&sn=5b71b7f1654b04a55fca25b0e90a4433&chksm=80b7b213b7c03b0598f6014bfa2f7de12e1f32ca9f7b7fc49a2cf0f96440e4a7897d45c788fb&scene=21#w)。
- 今日头条适配方案。通过反射修正系统的 density 值，具体可以参考[《一种极低成本的 Android 屏幕适配方式》](https://mp.weixin.qq.com/s?__biz=MzI1MzYzMjE0MQ==&mid=2247484502&idx=2&sn=a60ea223de4171dd2022bc2c71e09351&scene=21#wechat_redirect)[《今日头条适配方案》](https://mp.weixin.qq.com/s/oSBUA7QKMWZURm1AHMyubA)。

#### CPU 与 GPU

除了屏幕，UI 渲染还依赖两个核心的硬件：CPU 与 GPU。UI 组件在绘制到屏幕之前，都需要经过 Rasterization（栅格化）操作，而栅格化操作又是一个非常耗时的操作。GPU（Graphic Processing Unit ）也就是图形处理器，它主要用于处理图形运算，可以帮助我们加快栅格化操作。

![img](https://static001.geekbang.org/resource/image/1c/8d/1c94e50372ff29ef68690da92c6b468d.png)

你可以从图上看到，**软件绘制使用的是 Skia 库**，它是一款能在低端设备如手机上呈现高质量的 2D 跨平台图形框架，类似 **Chrome、Flutter 内部使用的都是 Skia 库**。

#### OpenGL 与 Vulkan

对于硬件绘制，我们通过调用 OpenGL ES 接口利用 GPU 完成绘制。

![img](https://static001.geekbang.org/resource/image/cf/31/cf13332abe87502c7d60ff78b6aeb931.png)

Android 7.0 把 OpenGL ES 升级到最新的 3.2 版本同时，还添加了对[Vulkan](https://source.android.com/devices/graphics/arch-vulkan)的支持。Vulkan 是用于高性能 3D 图形的低开销、跨平台 API。相比 OpenGL ES，Vulkan 在改善功耗、多核优化提升绘图调用上有着非常明显的优势。

在国内，“王者荣耀”是比较早适配 Vulkan 的游戏，虽然目前兼容性还有一些问题，但是 Vulkan 版本的王者荣耀在流畅性和帧数稳定性都有大幅度提升，即使是战况最激烈的团战阶段，也能够稳定保持在 55～60 帧。

#### Android 渲染的演进

看下渲染架构：

![img](https://static001.geekbang.org/resource/image/7e/66/7efc5431b860634224f1cd7dda8abd66.png)

如果把应用程序图形渲染过程当作一次绘画过程，那么绘画过程中 Android 的各个图形组件的作用是：

- **画笔**：Skia 或者 OpenGL。我们可以用 Skia 画笔绘制 2D 图形，也可以用 OpenGL 来绘制 2D/3D 图形。正如前面所说，前者使用 CPU 绘制，后者使用 GPU 绘制。
- **画纸**：Surface。所有的元素都在 Surface 这张画纸上进行绘制和渲染。在 Android 中，Window 是 View 的容器，每个窗口都会关联一个 Surface。而 WindowManager 则负责管理这些窗口，并且把它们的数据传递给 SurfaceFlinger。
- **画板**：Graphic Buffer。Graphic Buffer 缓冲用于应用程序图形的绘制，在 Android 4.1 之前使用的是双缓冲机制；在 Android 4.1 之后，使用的是三缓冲机制。
- **显示**：SurfaceFlinger。它将 WindowManager 提供的所有 Surface，通过硬件合成器 Hardware Composer 合成并输出到显示屏。

##### Android 4.0：开启硬件加速

在 Android 3.0 之前，或者没有启用硬件加速时，系统都会使用软件方式来渲染 UI。

![img](https://static001.geekbang.org/resource/image/8f/97/8f85be65392fd7b575393e5665f49a97.png)

整个流程如上图所示：

- Surface。每个 View 都由某一个窗口管理，而每一个窗口都关联有一个 Surface。
- Canvas。通过 Surface 的 lock 函数获得一个 Canvas，Canvas 可以简单理解为 Skia 底层接口的封装。
- Graphic Buffer。SurfaceFlinger 会帮我们托管一个BufferQueue，我们从 BufferQueue 中拿到 Graphic Buffer，然后通过 Canvas 以及 Skia 将绘制内容栅格化到上面。
- SurfaceFlinger。通过 Swap Buffer 把 Front Graphic Buffer 的内容交给 SurfaceFinger，最后硬件合成器 Hardware Composer 合成并输出到显示屏。

整个渲染流程是不是非常简单？但是正如我前面所说，CPU 对于图形处理并不是那么高效，这个过程完全没有利用到 GPU 的高性能。

**硬件加速绘制：**

所以从 Androd 3.0 开始，Android 开始支持硬件加速，到 Android 4.0 时，默认开启硬件加速。

![img](https://static001.geekbang.org/resource/image/79/e8/79c315275abac0823971e5d6b9657be8.png)

硬件加速绘制与软件绘制整个流程差异非常大，最核心就是我们通过 GPU 完成 Graphic Buffer 的内容绘制。此外硬件绘制还引入了一个 DisplayList 的概念，每个 View 内部都有一个 DisplayList，当某个 View 需要重绘时，将它标记为 Dirty。

当需要重绘时，仅仅只需要重绘一个 View 的 DisplayList，而不是像软件绘制那样需要向上递归。这样可以大大减少绘图的操作数量，因而提高了渲染效率。

![img](https://static001.geekbang.org/resource/image/f9/51/f9da12b7c4d49f47d650cd8e14303c51.png)

##### Android 4.1：Project Butter

优化是无止境的，Google 在 2012 年的 I/O 大会上宣布了 Project Butter 黄油计划，并且在 Android 4.1 中正式开启了这个机制。

Project Butter 主要包含两个组成部分，一个是 VSYNC，一个是 Triple Buffering。

**VSYNC 信号：**

对于 Android 4.0，CPU 可能会因为在忙别的事情，导致没来得及处理 UI 绘制。

为解决这个问题，Project Buffer 引入了[VSYNC](https://source.android.com/devices/graphics/implement-vsync)，它类似于时钟中断。每收到 VSYNC 中断，CPU 会立即准备 Buffer 数据，由于大部分显示设备刷新频率都是 60Hz（一秒刷新 60 次），也就是说一帧数据的准备工作都要在 16ms 内完成。

![img](https://static001.geekbang.org/resource/image/06/bd/06753998a26642edd3481f85fc93c8bd.png)

这样应用总是在 VSYNC 边界上开始绘制，而 SurfaceFlinger 总是 VSYNC 边界上进行合成。这样可以消除卡顿，并提升图形的视觉表现。

**三缓冲机制 Triple Buffering：**

在 Android 4.1 之前，Android 使用双缓冲机制。怎么理解呢？一般来说，不同的 View 或者 Activity 它们都会共用一个 Window，也就是共用同一个 Surface。

而每个 Surface 都会有一个 BufferQueue 缓存队列，但是这个队列会由 SurfaceFlinger 管理，通过匿名共享内存机制与 App 应用层交互。

![img](https://static001.geekbang.org/resource/image/88/96/887c5ff4ae381733a95634c115c7a296.png)

整个流程如下：

- 每个 Surface 对应的 BufferQueue 内部都有两个 Graphic Buffer ，一个用于绘制一个用于显示。我们会把内容先绘制到离屏缓冲区（OffScreen Buffer），在需要显示时，才把离屏缓冲区的内容通过 Swap Buffer 复制到 Front Graphic Buffer 中。
- 这样 SurfaceFlinge 就拿到了某个 Surface 最终要显示的内容，但是同一时间我们可能会有多个 Surface。这里面可能是不同应用的 Surface，也可能是同一个应用里面类似 SurefaceView 和 TextureView，它们都会有自己单独的 Surface。
- 这个时候 SurfaceFlinger 把所有 Surface 要显示的内容统一交给 Hareware Composer，它会根据位置、Z-Order 顺序等信息合成为最终屏幕需要显示的内容，而这个内容会交给系统的帧缓冲区 Frame Buffer 来显示（Frame Buffer 是非常底层的，可以理解为屏幕显示的抽象）

如果你理解了双缓冲机制的原理，那就非常容易理解什么是三缓冲区了。如果只有两个 Graphic Buffer 缓存区 A 和 B，如果 CPU/GPU 绘制过程较长，超过了一个 VSYNC 信号周期，因为缓冲区 B 中的数据还没有准备完成，所以只能继续展示 A 缓冲区的内容，这样缓冲区 A 和 B 都分别被显示设备和 GPU 占用，CPU 无法准备下一帧的数据。

![img](https://static001.geekbang.org/resource/image/55/53/551fb7b5a8a0bed7d81edde6aff99653.png)

如果再提供一个缓冲区，CPU、GPU 和显示设备都能使用各自的缓冲区工作，互不影响。

简单来说，三缓冲机制就是在双缓冲机制基础上增加了一个 Graphic Buffer 缓冲区，这样可以最大限度的利用空闲时间，带来的坏处是多使用的了一个 Graphic Buffer 所占用的内存。

![img](https://static001.geekbang.org/resource/image/4d/ed/4d84d2d6a8f8e25e1622665141d993ed.png)

**数据测量：**

“工欲善其事，必先利其器”，Project Butter 在优化 UI 渲染性能的同时，也希望可以帮助我们更好地排查 UI 相关的问题。

在 Android 4.1，新增了 Systrace 性能数据采样和分析工具。在卡顿和启动优化中，我们已经使用过 Systrace 很多次了，也可以用它来检测每一帧的渲染情况。

Tracer for OpenGL ES 也是 Android 4.1 新增加的工具，它可逐帧、逐函数的记录 App 用 OpenGL ES 的绘制过程。它提供了每个 OpenGL 函数调用的消耗时间，所以很多时候用来做性能分析。但因为其强大的记录功能，在分析渲染问题时，当 Traceview、Systrace 都显得棘手时，还找不到渲染问题所在时，此时这个工具就会派上用场了。

在 Android 4.2，系统增加了检测绘制过度工具。

##### Android 5.0：RenderThread

经过 Project Butter 黄油计划之后，Android 的渲染性能有了很大的改善。但是不知道你有没有注意到一个问题，虽然我们利用了 GPU 的图形高性能运算，但是从计算 DisplayList，到通过 GPU 绘制到 Frame Buffer，整个计算和绘制都在 UI 主线程中完成。

![img](https://static001.geekbang.org/resource/image/77/b1/778a18e6f9f9c1d08a5f5e12645c21b1.png)

UI 主线程“既当爹又当妈”，任务过于繁重。如果整个渲染过程比较耗时，可能造成无法响应用户的操作，进而出现卡顿。GPU 对图形的绘制渲染能力更胜一筹，如果使用 GPU 并在不同线程绘制渲染图形，那么整个流程会更加顺畅。

正因如此，在 Android 5.0 引入了两个比较大的改变。一个是引入了 RenderNode 的概念，它对 DisplayList 及一些 View 显示属性做了进一步封装。另一个是引入了 RenderThread，所有的 GL 命令执行都放到这个线程上，渲染线程在 RenderNode 中存有渲染帧的所有信息，可以做一些属性动画，这样即便主线程有耗时操作的时候也可以保证动画流畅。

如下图：

CPU 将数据同步（sync）给 GPU 之后，一般不会阻塞等待 GPU 渲染完毕，而是通知结束后就返回。而 RenderThread 承担了比较多的绘制工作，分担了主线程很多压力，提高了 UI 线程的响应速度。

![img](https://static001.geekbang.org/resource/image/7f/7d/7f349aefe7a081259218af30b9a9fc7d.png)

#### 未来

在 Android 6.0 的时候，Android 在 gxinfo 添加了更详细的信息；在 Android 7.0 又对 HWUI 进行了一些重构，而且支持了 Vulkan；在 Android P 支持了 Vulkun 1.1。我相信在未来不久的 Android Q，更好地支持 Vulkan 将是一个必然的方向。

总的来说，UI 渲染的优化必然会朝着两个方向。一个是进一步压榨硬件的性能，让 UI 可以更加流畅。一个是改进或者增加更多的分析工具，帮助我们更容易地发现以及定位问题。

今天我们通过 Android 渲染的演进历程，一步一步加深对 Android 渲染机制的理解，这对我们 UI 渲染优化工作会有很大的帮助。

但是凡事都要两面看**，硬件加速绘制虽然极大地提高了 Android 系统显示和刷新的速度**，但它也存在那么一些问题。**一方面是内存消耗**，OpenGL API 调用以及 Graphic Buffer 缓冲区会占用至少几 MB 的内存，而实际上会占用更多一些。**不过最严重的还是兼容性问题**，部分绘制函数不支持是其中一部分原因，更可怕的是硬件加速绘制流程本身的 Bug。由于 Android 每个版本对渲染模块都做了一些重构，在某些场景经常会出现一些莫名其妙的问题。

#### UI优化常用手段：

##### 1、尽量使用硬件加速

硬件加速绘制的性能是远远高于软件绘制的。

所以说 UI 优化的第一个手段就是保证渲染尽量使用硬件加速。

有哪些情况我们不能使用硬件加速呢？之所以不能使用硬件加速，是因为硬件加速不能支持所有的 Canvas API，具体 API 兼容列表可以见[drawing-support](https://developer.android.com/guide/topics/graphics/hardware-accel#drawing-support)文档。如果使用了不支持的 API，系统就需要通过 CPU 软件模拟绘制，这也是渐变、磨砂、圆角等效果渲染性能比较低的原因。

##### 2、Create View 优化

在优化之前我们先来分解一下 View 创建的耗时，可能会包括各种 XML 的随机读的 I/O 时间、解析 XML 的时间、生成对象的时间（Framework 会大量使用到反射）。

**使用代码创建：**

使用 XML 进行 UI 编写可以说是十分方便，可以在 Android Studio 中实时预览到界面。如果我们要对一个界面进行极致优化，就可以使用代码进行编写界面。

但是**这种方式对开发效率来说简直是灾难**，因此我们可以使用一些开源的 XML 转换为 Java 代码的工具，例如[X2C](https://github.com/iReaderAndroid/X2C)。但坦白说，还是有不少情况是不支持直接转换的。

这块要兼容性能和开发效率，只有在**性能要求非常高但修改又不非常频繁**的的情况下才考虑这么处理。

**异步创建：**

那我们能不能在线程提前创建 View，实现 UI 的预加载吗？尝试过的同学都会发现系统会抛出下面这个异常：

```
java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()      
  at android.os.Handler.<init>(Handler.java:121)
```

事实上，我们可以通过又一个非常取巧的方式来实现。在使用线程创建 UI 的时候，先把线程的 Looper 的 MessageQueue 替换成 UI 线程 Looper 的 Queue。

![img](https://static001.geekbang.org/resource/image/54/55/54ab7385263b71ded795a5001df24a55.png)

不过需要注意的是，在**创建完 View 后我们需要把线程的 Looper 恢复成原来**的。

**View 重用：**

正常来说，View 会随着 Activity 的销毁而同时销毁。ListView、RecycleView 通过 View 的缓存与重用大大地提升渲染性能。因此我们可以参考它们的思想，实现一套可以在不同 Activity 或者 Fragment 使用的 View 缓存机制。但是这里需要保证所有进入缓存池的 View 都已经“净身出户”，不会保留之前的状态。微信曾经就因为这个缓存，导致出现不同的用户聊天记录错乱。

这块有点**超纲**了。。

##### 3、measure/layout 优化

- **减少 UI 布局层次**

  例如尽量扁平化，使用ViewStub、Merge 等优化。

- **优化 layout 的开销**

  尽量不使用 RelativeLayout 或者基于 weighted LinearLayout，它们 layout 的开销（Measure这一步）非常巨大。这里我推荐使用 ConstraintLayout 替代 RelativeLayout 或者 weighted LinearLayout。

- **背景优化**

  尽量不要重复去设置背景，这里需要注意的是主题背景（theme)， theme 默认会是一个纯色背景，如果我们自定义了界面的背景，那么主题的背景我们来说是无用的。但是由于主题背景是设置在 DecorView 中，所以这里会带来重复绘制，也会带来绘制性能损耗。

对于 measure 和 layout，我们能不能像 Create View 一样实现线程的预布局呢？这样可以大大地提升首次显示的性能。

Textview 是系统控件中非常强大也非常重要的一个控件，强大的背后就代表着需要做很多计算。在 2018 年的 Google I/O 大会，发布了**PrecomputedText**并已经集成在 Jetpack 中，它给我们提供了接口，可以异步进行 measure 和 layout，不必在主线程中执行。

#### UI 优化的进阶手段

那对于其他的控件我们是不是也可以采用相同的方式？

接下来我们一起来看看近两年新框架的做法，我来介绍一下 Facebook 的一个开源库 [Litho](https://github.com/facebook/litho) 以及 Google 开源的 Flutter。

##### 1、Litho：异步布局

Litho是 Facebook 开源的声明式 Android UI 渲染框架，它是基于另外一个 Facebook 开源的布局引擎[Yoga](https://github.com/facebook/yoga)开发的。

Litho 本身非常强大，内部做了很多非常不错的优化。下面我来简单介绍一下它是如何优化 UI 的。

**异步布局：**

﻿一般来说的 Android 所有的控件绘制都要遵守 measure -> layout -> draw 的流水线，并且这些都发生在主线程中。

![img](https://static001.geekbang.org/resource/image/b8/5c/b8bd2cb5ad88a64f301381b0cf45b15c.png)

Litho 如我前面提到的 PrecomputedText 一样，把 measure 和 layout 都放到了后台线程，只留下了必须要在主线程完成的 draw，这大大降低了 UI 线程的负载。它的渲染流水线如下：

![img](https://static001.geekbang.org/resource/image/40/63/40ed08e561093024b58b0840af80a663.png)



**界面扁平化：**

前面也提到过，降低 UI 的层级是一个非常通用的优化方法。你肯定会想，有没有一种方法可以直接降低 UI 的层级，而不通过代码的改变呢？Litho 就给了我们一种方案，由于 Litho 使用了自有的布局引擎（Yoga)，在布局阶段就可以检测不必要的层级、减少 ViewGroups，来实现 UI 扁平化。比如下面这样图，上半部分是我们一般编写这个界面的方法，下半部分是 Litho 编写的界面，可以看到只有一层层级。

![img](https://static001.geekbang.org/resource/image/17/90/1758d00240d0eda842570038caf92090.png)

**优化 RecyclerView：**

﻿Litho 还优化了 RecyclerView 中 UI 组件的缓存和回收方法。原生的 RecyclerView 或者 ListView 是按照 viewType 来进行缓存和回收，但如果一个 RecyclerView/ListView 中出现 viewType 过多，会使缓存形同虚设。但 Litho 是按照 text、image 和 video 独立回收的，这可以提高缓存命中率、降低内存使用率、提高滚动帧率。

![img](https://static001.geekbang.org/resource/image/9d/8d/9d8a2830ef39dd84ca8165a08a38098d.png)

Litho 虽然强大，但也有自己的缺点。它为了实现 measure/layout 异步化，使用了类似 react 单向数据流设计，这一定程度上**加大了 UI 开发的复杂性**。并且 Litho 的 **UI 代码是使用 Java/Kotlin 来进行编写**，无法做到在 AS 中预览。

##### 2、Flutter：自己的布局 + 渲染引擎

这块暂时就不研究了。

##### 3、RenderThread 与 RenderScript

在 Android 5.0，系统增加了 RenderThread，对于 ViewPropertyAnimator 和 CircularReveal 动画，我们可以使用[RenderThead 实现动画的异步渲染](https://mp.weixin.qq.com/s?__biz=MzUyMDAxMjQ3Ng==&mid=2247489230&amp;idx=1&amp;sn=adc193e35903ab90a4c966059933a35a&source=41#wechat_redirect)。当主线程阻塞的时候，普通动画会出现明显的丢帧卡顿，而使用 RenderThread 渲染的动画即使阻塞了主线程仍不受影响。

现在越来越多的应用会使用一些高级图片或者视频编辑功能，例如图片的高斯模糊、放大、锐化等。拿日常我们使用最多的“扫一扫”这个场景来看，这里涉及大量的图片变换操作，例如缩放、裁剪、二值化以及降噪等。

图片的变换涉及大量的计算任务，而根据我们上一期的学习，这个时候使用 GPU 是更好的选择。那如何进一步压榨系统 GPU 的性能呢？

我们可以通过[RenderScript](https://developer.android.com/guide/topics/renderscript/compute)，它是 Android 操作系统上的一套 API。它基于异构计算思想，专门用于密集型计算。RenderScript 提供了三个基本工具：一个硬件无关的通用计算 API；一个类似于 CUDA、OpenCL 和 GLSL 的计算 API；一个类[C99](https://zh.wikipedia.org/wiki/C99)的脚本语言。允许开发者以较少的代码实现功能复杂且性能优越的应用程序。

示例：

[RenderScript 渲染利器](https://www.jianshu.com/p/b72da42e1463)

[RenderScript : 简单而快速的图像处理](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2016/0504/4205.html?utm_source=itdadao&utm_medium=referral)

#### 参考文档

1，极客时间 - Andorid开发高手课

2，<https://blog.csdn.net/mysimplelove/article/details/92635237>（Android 屏幕(View)刷新机制(原理)）

3，<https://zhuanlan.zhihu.com/p/87332093> 安卓子线程更新UI.

4，https://www.jianshu.com/p/0d18ed263db6 APkChecker



