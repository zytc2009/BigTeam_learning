# UI优化

背景知识：

### 1.屏幕与适配

Android 开发都应该很清楚 px、dp、dpi、ppi、density 这些概念

![](D:\github\Android_learning\Android技术点\images\dp_px.png)

除了直接 dp 适配之外，目前业界比较常用的 UI 适配方法主要有下面几种：限制符适配方案。主要有宽高限定符与 smallestWidth 限定符适配方案，具体可以参考[《Android 目前稳定高效的 UI 适配方案》](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650826034&idx=1&sn=5e86768d7abc1850b057941cdd003927&chksm=80b7b1acb7c038ba8912b9a09f7e0d41eef13ec0cea19462e47c4e4fe6a08ab760fec864c777&scene=21#wechat_redirect) [《smallestWidth 限定符适配方案》](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650826381&idx=1&sn=5b71b7f1654b04a55fca25b0e90a4433&chksm=80b7b213b7c03b0598f6014bfa2f7de12e1f32ca9f7b7fc49a2cf0f96440e4a7897d45c788fb&scene=21#wechat_redirect)。今日头条适配方案。通过反射修正系统的 density 值，具体可以参考[《一种极低成本的 Android 屏幕适配方式》](https://mp.weixin.qq.com/s?__biz=MzI1MzYzMjE0MQ==&mid=2247484502&idx=2&sn=a60ea223de4171dd2022bc2c71e09351&scene=21#wechat_redirect)[《今日头条适配方案》](https://mp.weixin.qq.com/s/oSBUA7QKMWZURm1AHMyubA)。

### 2.CPU 与 GPU

![](..\images\cpu_gpu.png)

你可以从图上看到，软件绘制使用的是 Skia 库，它是一款能在低端设备如手机上呈现高质量的 2D 跨平台图形框架，类似 Chrome、Flutter 内部使用的都是 Skia 库

![](..\images\opengles_api.png)

Android 7.0 把 OpenGL ES 升级到最新的 3.2 版本同时，还添加了对Vulkan的支持。Vulkan 是用于高性能 3D 图形的低开销、跨平台 API。相比 OpenGL ES，Vulkan 在改善功耗、多核优化提升绘图调用上有着非常明显的优势。

![](..\images\android图形架构.png)

如果把应用程序图形渲染过程当作一次绘画过程，那么绘画过程中 Android 的各个图形组件的作用是：

画笔：Skia 或者 OpenGL。我们可以用 Skia 画笔绘制 2D 图形，也可以用 OpenGL 来绘制 2D/3D 图形。正如前面所说，前者使用 CPU 绘制，后者使用 GPU 绘制。

画纸：Surface。所有的元素都在 Surface 这张画纸上进行绘制和渲染。在 Android 中，Window 是 View 的容器，每个窗口都会关联一个 Surface。而 WindowManager 则负责管理这些窗口，并且把它们的数据传递给 SurfaceFlinger。

画板：Graphic Buffer。Graphic Buffer 缓冲用于应用程序图形的绘制，在 Android 4.1 之前使用的是双缓冲机制；在 Android 4.1 之后，使用的是三缓冲机制。

显示：SurfaceFlinger。它将 WindowManager 提供的所有 Surface，通过硬件合成器 Hardware Composer 合成并输出到显示屏。



**view渲染过程**：

![view渲染过程](..\images\view渲染过程.png)



**view绘制流水线模型**：

![](..\images\view绘制流水线模型.png)



**view重绘**：

![](..\images\view重绘.png)



### 3.UI 渲染测量

#### 1)图形化界面工具:

- 测试工具：Profile GPU Rendering 和 Show GPU Overdraw，具体的使用方法你可以参考[《检查 GPU 渲染速度和绘制过度》](https://developer.android.google.cn/studio/profile/cpu-profiler)。

- 问题定位工具：Systrace 和 Tracer for OpenGL ES，具体使用方法可以参考《Slow rendering》。

- GAPID: Android Studio 3.1 之后，Android 推荐使用Graphics API Debugger（GAPID）

#### 2)自动化测量工具:

- gfxinfogfxinfo 可以输出包含各阶段发生的动画以及帧相关的性能信息，具体命令如下：

  ```
  adb shell dumpsys gfxinfo 包名
  ```

-  SurfaceFlinger

  你可以通过下面的命令拿到系统 SurfaceFlinger 相关的信息：

  ```
  adb shell dumpsys SurfaceFlinger
  ```

  

### 4.UI 优化的常用手段

#### 1)尽量使用硬件加速

#### 2)Create View 优化

  使用代码创建view,  异步线程创建view,   View 重用

#### 3)measure/layout 优化

  减少 UI 布局层次,  优化 layout 的开销,  背景优化。  2018 年的 Google I/O 大会，发布了PrecomputedText并已经集成在 Jetpack 中，它给我们提供了接口，可以异步进行 measure 和 layout，不必在主线程中执行。



### 5.UI 优化的进阶手段

#### 1) Litho：异步布局, 界面扁平化, 优化RecyclerView

​      [Litho的使用及原理剖析](https://tech.meituan.com/2019/03/14/litho-use-and-principle-analysis.html)

####  2) Flutter：自己的布局 + 渲染引擎

​      ![](..\images\flutter架构.png)

​     [《Flutter 原理与实践》](https://tech.meituan.com/2018/08/09/waimai-flutter-practice.html)

#### 3) RenderThread 与 RenderScript

  [RenderThread:异步渲染动画](https://mp.weixin.qq.com/s?__biz=MzUyMDAxMjQ3Ng==&mid=2247489230&idx=1&sn=adc193e35903ab90a4c966059933a35a&source=41#wechat_redirect)

  如何将它们应用到我们的项目中？你可以参考下面的一些实践方案：

  [RenderScript 渲染利器](https://www.jianshu.com/p/b72da42e1463)

  [RenderScript :简单而快速的图像处理](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2016/0504/4205.html?utm_source=itdadao&utm_medium=referral)

  [Android RenderScript 简单高效实现图片的高斯模糊效果](http://yifeng.studio/2016/10/20/android-renderscript-blur/)





相关文章：

1. [Android开发高手课](https://time.geekbang.org/column/article/80921)

   