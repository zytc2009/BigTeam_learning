[TOC]

### Android卡顿分析和优化

#### 卡顿表象

##### 简单认知

- 大家都有玩过一些手机游戏，比如说现在的王者荣耀，吃鸡啊等等，有时候我们玩着玩着，发现我的英雄怎么不动了，或者是半天才走一步。
- 再有就是比如说我们的列表页面，有时候滑起来一顿一顿的等，这些都是属于卡顿的范畴，更有甚者，包括ANR。
- **那到底卡顿是怎么一回事呢？**希望通过今天的文章，大家都能找到你满意的答案。

##### 16ms问题

再详细聊我们的卡顿之前，我们先简单介绍下16ms问题。

先接触两个图形概念： **帧率**（Frame Rate，单位FPS）– GPU显卡生成帧的速率，也可以认为是数据处理的速度）， **屏幕刷新频率** （Refresh Rate单位赫兹/HZ）：是指硬件设备刷新屏幕的频率。屏幕刷新率一般是固定的，比如60Hz的每16ms就刷一次屏幕，可以类比一下黑白电视的电子扫描枪，每16ms电子枪从上到下从左到右一行一行逐渐把图片绘制出来，如果GPU显卡性能非常强悍，帧率可以非常高，甚至会高于屏幕刷新频率。

下面我们看下这张图：

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LTE0NmFhMjcyOWY3NTQyYjIucG5n?x-oss-process=image/format,png)

如果只有一块缓存，在没有加锁的情况下，容易出现。即：在屏幕更新的时候，如果显卡输出帧率很高，在A帧的数据上半部分刚更新完时，B帧就到了，如果没有任何机制，可以**认为帧到了就可用**，在继续刷新下半部分时，由于只有一块存储，A被B覆盖，绘制用的数据就是B帧，此时就会出现上半部分是A下半部分是B，这就是屏幕撕裂。

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LWQ4YTdiMjUyMTkxYjdhZDgucG5n?x-oss-process=image/format,png)

相比较画面撕裂如下：

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LTQ0MjRjNjZkMzZiMjkxZjIucG5n?x-oss-process=image/format,png)



这是只有一块显示存储的情况，其实**只要加锁就能解决**。那如果多增加一块显示存储区能解决吗？显卡绘制成功后，先写入BackBuffer，不影响当前正在展示的FrameBuffer，这就是双缓冲，但是理论上其实也不行，因为BackBuffer毕竟也是要展示的，也要”拷贝“到FrameBuffer，在A帧没画完，BackBuffer如果不加干预，直接”拷贝“到FrameBuffer同样出现撕裂。所以**同步锁的机制才是关键**，必须有这么一个机制告诉GPU显卡，要等待**当前帧绘完整，才能替换当前帧**。但如果仅仅单缓存加锁的话GPU显卡效率会变低。所以既然这样，那就一边加同步锁，同时再多加一个缓存，垂直同步（VSYNC）就可看做是这么个东西，其实两者是配合使用的。

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LTMwYWMzZWE0MTE4ZTkzOTAucG5n?x-oss-process=image/format,png)

再来看下VSYNC，屏幕刷新从左到右水平扫描（Horizontal Scanning），从上到下**垂直扫描Vertical Scanning，垂直扫描完成则整个屏幕刷新完毕，便告诉外界可以绘制下一帧的时机，在这里发出VSync信号**，通知GPU给FrameBuffer传数据，完成后，屏幕便可以开始刷新，所以或许称之为帧同步更合适。VSYNC强制帧率和显示器刷新频率同步，如果当前帧没绘制完，即使下一帧准备好了，也禁止使用下一帧，直到显示器绘制完当前帧，等下次刷新的时候，才会用下一帧。对Android系统而言，垂直同步信号除了强制帧率和显示器刷新频率同步外，还有其他很多作用，VSYNC是APP端重绘、SurfaceFlinger图层合成的触发点，只有收到VSYNC信号，它们才会工作。

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LTk2NmNhNWY0MjU5MmVlZmYucG5n?x-oss-process=image/format,png)

如果想要达到60FPS的流畅度，每16毫秒必须刷新一帧，否则动画、视频就没那么丝滑，扩展后：

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LWY2MWJhNjVkOWUyNTBhY2EucG5n?x-oss-process=image/format,png)

对于没采用VSYNC做调度的系统来说（4.1以下），CPU的对于显示帧的处理是凌乱的，优先级也没有保障，处理完一帧后，CPU可能并不会及时处理下一帧，可能会优先处理其他消息，等到它开始处理UI生成帧的时候，可能已经处于VSYNC的中间，这样就很**容易跨两个VYSNC信号**，导致掉帧。在Jelly Bean中，下一帧的处理被限定在VSync信号到达时，并且看Android的处理UI重绘消息的优先级是比较高的，其他的同步消息均不会执行，从而保证每16ms处理一帧有序进行，同时由于是**在每个VSYNC信号到达时就处理帧，可以尽量避免跨越两帧的情况出现**。

上面的流程中，Android已经采用了双缓冲，**双缓冲不仅仅是两份存储，它是一个概念，双缓冲是一条链路，不是某一个环节，是整个系统采用的一个机制，需要各个环节的支持，从APP到SurfaceFlinger、到图像显示都要参与协作。**对于APP端而言，每个Window都是一个双缓冲的模型，一个Window对应一个Surface，而每个Surface里至少映射两个存储区，一个给图层合成显示用，一个给APP端图形处理，这便是应于上层的双缓冲。Android4.0之后基本都是默认硬件加速，CPU跟GPU都是并发处理任务的，CPU处理完之后就完工，等下一个VSYNC到来就可以进行下一轮操作。也就是CPU、GPU、显示都会用到Buffer，VSYNC+双缓冲在理想情况下是没有问题的，但如果某个环节出现问题，那就不一样了如下（帧耗时超过16ms）：

![双缓冲jank](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LWM4YjI1ZWZhYTQ4YzhhYTIucG5n?x-oss-process=image/format,png)

可以看到在第二个阶段，存在CPU资源浪费，为什么呢？双缓冲Surface只会提供两个Buffer，一个Buffer被DisPlay占用（SurfaceFlinger用完后不会释放当前的Buffer，只会释放旧的Buffer,直观的想一下，**如果新Buffer生成受阻，那么肯定要保留一个备份给SF用，才能不阻碍合成显示，就必定要一直占用一个Buffer，新的Buffer来了才释放老的**），另一个被GPU处理占用，所以，CPU就无法获取到Buffer处理当前UI，在Jank的阶段空空等待。一般出现这种场景都是连续的：比如复杂视觉效果每一帧可能需要20ms（CPU 8ms +GPU 12ms），GPU可能会一直超负荷，CPU跟GPU一直抢Buffer，这样带来的问题就是滚雪球似的掉帧，一直浪费，**完全没有利用CPU与GPU并行处理的效率，成了串行处理**，如下所示：

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LTNkZTA2MjJiZjJlMDVhMTQucG5n?x-oss-process=image/format,png)

这种情况怎么办？**三缓冲**这个大兄弟就要上场了，多增加一个Buffer给CPU用，让它提前忙起来，这样就能做到三方都有Buffer可用，CPU跟GPU不用争一个Buffer，真正实现并行处理。如下：

**三缓冲（4.1之后）：**

在Android系统里，除了双缓冲，还有个三缓冲，不过这个三缓冲是对于屏幕硬件刷新之外而言，它关注的是整个Android图形系统的消费者模型，跟Android自身的VSYNC用法有关系，在 Jelly Bean 中Android扩大了VSYNC使用场景与效果，不仅用在屏幕刷新防撕裂，同时也用在APP端绘制及SurfaceFlinger合成。

![image.png](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8xNDYwNDY4LWI4OGNmOWIyZWIzZDZiYjAucG5n?x-oss-process=image/format,png)

上图所示，虽然即使每帧需要20ms（CPU 8ms +GPU 12ms），但是由于多加了一个Buffer，实现了CPU跟GPU并行，便可以做到了只在开始掉一帧，后续却不掉帧，双缓冲充分利用16ms做到低延时，三缓冲保障了其稳定性，为什么4缓冲没必要呢？因为三个既可保证并行，四个徒增资源浪费。

**总结一下：**

1、 同步是防止画面撕裂的关键，VSYNC同步能防止画面撕裂。
2、 VSYNC+双缓冲在Android中能有序规划渲染流程，降低延时。
3、 Android已经采用了双缓冲，双缓冲不仅仅是两份存储，它是一个概念，双缓冲是一条链路，不是某一个环节，是整个系统采用的一个机制，需要各个环节的支持，从APP到SurfaceFlinger、到图像显示都要参与协作。
4、 三缓冲在UI复杂情况下能保证画面的连续性，提高柔韧性。


#### 卡顿原因

讲完16ms问题，下面开始我们今天的正式表演，其实有的时候像内存泄漏、耗电、耗流量不太容易被察觉，相比下卡顿是最容易被发现的问题，且导致卡顿的原因有很多，跟 CPU、内存、磁盘 I/O 都可能有关，跟用户当时的系统也有很大关系。而且线上的卡顿问题在线下是很难复现的，因为它与当时的场景是强相关的，比如说线上用户的磁盘IO空间不足了，它影响了磁盘IO的写入性能，所以导致卡顿。针对这种问题，我们最好在发现卡顿的时候尽量地去记录用户当时发生卡顿时的具体的场景信息。

尽管造成卡顿的原因有很多种，**不过最终都会反映到CPU时间上**。

CPU时间分为两种：

- 用户时间：执行用户态应用程序代码所消耗的时间。
- 系统时间：执行内核态系统调用所消耗的时间，包括I/O、锁、中断和其它系统调用所消耗的时间。

CPU的问题大致可以分为以下三类：

##### CPU资源冗余使用

- 算法效率太低：明明可以遍历一次的却需要去遍历两次，主要出现在查找、排序、删除等环节。
- 没有使用cache：明明解码过一次的图片还去重复解码。
- 计算时使用的基本类型不对：明明使用int就足够，却要使用long，这会导致CPU的运算压力多出不少。

##### CPU资源争抢

抢主线程的CPU资源：这是最常见的问题，并且在Android 6.0版本之前没有RenderThread的时候，主线程的繁忙程度就决定了是否会引发用户的卡顿问题。

抢音视频的CPU资源：音视频编解码本身会消耗大量的CPU资源，并且其对于解码的速度是有硬性要求的，如果达不到就可能产生播放流畅度的问题。我们可以采取两种方式去优化：1、尽量排除非核心业务的消耗。2、优化自身的性能消耗，把CPU负载转化为GPU负载，如使用RenderScript来处理视频中的影像信息。

大家平等，互相抢：比如在自定义的相册中，我开了20个线程做图片解码，那就是互相抢CPU了，结果就是会导致图片的显示速度非常慢。这简直就是三个和尚没水喝的典型案例。因此，在自定义线程池的时候我们需要按照系统核心数去控制线程数。

##### CPU资源利用率低

对于启动、界面切换、音视频编解码这些场景，为了保证其速度，我们需要去好好利用CPU。而导致无法充分利用CPU的因素，不仅有磁盘和网络I/O，还有锁操作、sleep等等。对于锁的优化，通常是尽可能地缩减锁的范围。


#### 卡顿定位

**那上面我们罗列了这么多点，我们则呢知道当前CPU情况呢**？我们可以通过CPU的主频、核心数、缓存等参数去评估CPU的性能，这些参数的好坏能表现出CPU计算能力和指令执行能力的强弱，也就是CPU每秒执行的浮点计算数和每秒执行的指令数的多少。

我们还可以通过shell命令直接查看手机的CPU核心数与频率等信息，如下所示：

```
// 先输入adb shell进入手机的shell环境
adb shell

// 获取 CPU 核心数，我的手机是8核
platina:/ $ cat /sys/devices/system/cpu/possible
0-7

// 获取第一个 CPU 的最大频率
platina:/ $ cat
/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq
1843200

// 获取第二个CPU的最小频率
platina:/ $ cat
/sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_min_freq
633600
```

当应用出现卡顿问题之后，首先我们应该查看系统CPU的使用率。
我们通过读取 /proc/stat 文件获取总的 CPU 时间，并读取 /proc/[PID]/stat 获取应用进程 的CPU 时间，然后，采样两个足够短的时间间隔的 CPU 快照与进程快照来计算其 CPU 使用率。

##### 计算总的 CPU 使用率:

采样两个足够短的时间间隔的 CPU 快照，即需要前后两次去读取 /proc/stat 文件，获取两个时间点对应的数据,我以我的手机为例，截取了两次cpu的消耗指标：


```
HWCOL:/ $ cat /proc/stat
cpu  26479720 4567525 18118834 81883470 89305 0 909408 0 0 0
cpu0 5818716 613795 4720112 81452750 88352 0 460403 0 0 0
cpu1 5455361 601708 4171845 57717 91 0 241182 0 0 0
cpu2 5533351 820669 3841038 56607 140 0 125359 0 0 0
cpu3 5039056 765387 3368369 56749 146 0 65380 0 0 0
cpu4 2362460 626423 1051006 62472 241 0 7753 0 0 0
cpu5 1040082 472352 468473 65682 118 0 5495 0 0 0
cpu6 669251 364220 272028 65886 107 0 2393 0 0 0
cpu7 561440 302968 225960 65604 107 0 1441 0 0 0
...

HWCOL:/ $ cat /proc/stat
cpu  26479811 4567764 18118910 81885036 89306 0 909413 0 0 0
cpu0 5818742 613796 4720141 81452936 88352 0 460407 0 0 0
cpu1 5455390 601708 4171860 57916 91 0 241182 0 0 0
cpu2 5533371 820670 3841056 56812 140 0 125359 0 0 0
cpu3 5039065 765388 3368381 56970 146 0 65380 0 0 0
cpu4 2362460 626578 1051006 62566 241 0 7753 0 0 0
cpu5 1040088 472428 468476 65846 120 0 5495 0 0 0
cpu6 669251 364226 272028 66132 107 0 2393 0 0 0
cpu7 561440 302968 225960 65855 107 0 1441 0 0 0
...
```

我的手机是8核，所以这里的cpu个数是8个，从cpu0到cpu7，第一行的cpu即是8个cpu的指标数据汇总，因为是要计算系统cpu的使用率，那当然应该以cpu为基准了。两次采样的CPU指标数据如下：


```
第一次：26479720 4567525 18118834 81883470 89305 0 909408 0 0 0 
第二次：26479811 4567764 18118910 81885036 89306 0 909413 0 0 0
```

其对应的各项指标如下（不同linux版本可能略有差别）：

```
CPU (user, nice, system, idle, iowait, irq, softirq, stealstolen, guest);
```

拿第一次数据来说（26479720 4567525 18118834 81883470 89305 0 909408 0 0 0）的数据来说，下面，我就来详细地解释下这些指标的含义。

user(26479720)： 表示从系统启动开始至今处于用户态的运行时间，注意不包含 nice 值为负的进程。
nice(4567525) ：表示从系统启动开始至今nice 值为负的进程所占用的 CPU 时间。
system(18118834)： 表示从系统启动开始至今处于内核态的运行时间。
idle(81883470) ：表示从系统启动开始至今除 IO 等待时间以外的其他等待时间。
iowait(89305)：表示从系统启动开始至今的IO 等待时间。(从Linux V2.5.41开始包含)
irq(0)：表示从系统启动开始至今的硬中断时间。(从Linux V2.6.0-test4开始包含)
softirq(909408)：表示从系统启动开始至今的软中断时间。(从Linux V2.6.0-test4开始包含)
stealstolen(0) ：表示当在虚拟化环境中运行时在其他操作系统中所花费的时间。在Android系统下此值为0。(从Linux V2.6.11开始包含)
guest(0) ：表示当在Linux内核的控制下为其它操作系统运行虚拟CPU所花费的时间。在Android系统下此值为0。(从 V2.6.24开始包含)

这些数值的单位都是 jiffies，jiffies 是内核中的一个全局变量，用来记录系统启动以来产生的节拍数，在 Linux 中，一个节拍大致可以理解为操作系统进程调度的最小时间片，不同的 Linux 系统内核中的这个值可能不同，通常在 1ms 到 10ms 之间。

了解了/proc/stat命令下各项参数的含义之后，我们就可以由前后两次时间点的CPU数据计算得到cpu1与cpu2的活动时间，如下所示：


```
cpu1 = 26479720 + 4567525 + 18118834 +  81883470 + 89305 + 0 + 909408 + 0 + 0 + 0 = 132048262jiffies
cpu2 = 26479811 + 4567764 + 18118910 +  81885036 + 89306 + 0 + 909413 + 0 + 0 + 0 = 132050240jiffies
```

**CPU总消耗时间**：

```
totalCPUTime = cpu2 - cpu1 = (132050240 - 132048262) = 1978jiffies
```

**CPU空闲消耗时间**：

```
idleCPUTime = idle2 – idle1 = 1566jiffies
```

**CPU使用率：**

```
totalCPUUse = (totalCPUTime – idleCPUTime) / totalCPUTime = （1978 - 1566）/ 1978 = 20%
```

前后两次的CPU使用率为20%，说明CPU还是比较空闲的。
**如果CPU 使用率一直大于 60% ，则表示系统处于繁忙状态，此时就需要进一步分析用户时间和系统时间的比例，看看到底是系统占用了CPU还是应用进程占用了CPU。**

##### 使用top命令查看应用进程的CPU消耗情况：

我们可以使用top命令查看哪些进程是 CPU 的主要消耗者。


```
PID   USER         PR  NI VIRT  RES  SHR S[%CPU] %MEM     TIME+ ARGS
  753 root         20   0  25M 1.7M 0.9M S 42.0   0.0  40:54.93 hwpged
  604 system       20   0  25M 2.8M 1.9M S  6.3   0.0 331:43.05 vendor.huawei.hardware.hwdisplay.displayengine@1.2-serv+
24298 shell        20   0  12M 2.8M 1.6M R  5.6   0.0   0:04.15 top
 1245 system       20   0 4.8G 247M 148M S  3.6   4.3 162:58.90 system_server
12477 u0_a671      20   0 2.6G 189M 106M S  2.6   3.3  24:05.88 com.seebaby
```

从上面我们可以看出，com.seebaby占用了%2.6的cpu（我是推到后台测试的）
还有一些可能用到的top命令。

```
// 排除0%的进程信息
adb shell top | grep -v '0% S'
```

```
// 获取指定进程的CPU、内存消耗，并设置刷新间隔
adb shell top -d 1 | grep com.seebaby
|platina:/ $ top -d 1|grep com.see+
```

除了top命令可以比较全面的查看cpu信息外，如果**我们只想查看当前指定进程已经消耗的CPU时间占系统总时间的百分比或其它的状态信息**的话，可以使用ps命令，常用的ps命令如下所示：


```
HWCOL:/ $ ps -p 12477
USER     PID  PPID   VSZ    RSS    WCHAN  ADDR S NAME
u0_a671  12477 567  2775096 179568   0      0  S com.seebaby

// 查看指定进程已经消耗的CPU时间占系统总时间的百分比
127|HWCOL:/ $ ps -o PCPU -p 12477
%CPU
6.9

```

输出参数的含义如下所示：

- USER：用户名
- PID：进程ID
- PPID：父进程ID
- VSZ：虚拟内存大小（1k为单位）
- RSS：常驻内存大小（正在使用的页）
- WCHAN：进程在内核态中的运行时间
- Instruction pointer：指令指针
- NAME：进程名字

最后的输出参数S表示的是进程当前的状态，总共有10种可能的状态，如下所示：

```
R (running) S (sleeping) D (device I/O) T (stopped)  t (traced)
Z (zombie)  X (deader)   x (dead)       K (wakekill) W (waking)
```

##### dumpsys cpuinfo：

dumpsys cpuinfo命令获得的信息比top命令更加精炼：

```
HWCOL:/ $ dumpsys cpuinfo
Load: 42.19 / 42.49 / 42.23
CPU usage from 160026ms to 102809ms ago (2020-06-01 17:56:07.263 to 2020-06-01 17:57:04.480):
  13% 1245/system_server: 6.9% user + 6.9% kernel / faults: 1636 minor 1 major
  5.5% 12477/com.seebaby: 3.9% user + 1.5% kernel / faults: 303 minor 2 major
  0.5% 22503/com.seebaby:QS: 0.1% user + 0.3% kernel / faults: 56 minor
  1.1% 21520/com.android.systemui: 0.8% user + 0.3% kernel / faults: 4522 minor 9 major
  0.9% 1975/com.huawei.powergenie: 0.3% user + 0.6% kernel / faults: 23 minor
  0.6% 772/hiview: 0.1% user + 0.5% kernel / faults: 119 minor
  0% 27891/com.huawei.hwid.persistent: 0% user + 0% kernel / faults: 44 minor 2 major
  0.5% 1333/gnss_control_hisi: 0% user + 0.4% kernel
  0.5% 1955/com.huawei.HwOPServer: 0.3% user + 0.1% kernel / faults: 37 minor
  0.4% 1067/hisi_frw/0: 0% user + 0.4% kernel
  0.3% 501/logd: 0.2% user + 0.1% kernel
  0.3% 625/lmkd: 0% user + 0.3% kernel
  0.3% 3385/com.tencent.mobileqq: 0.2% user + 0.1% kernel / faults: 24 minor 2 major
  0.3% 7/rcu_preempt: 0% user + 0.3% kernel
  0.2% 643/powerlogd: 0.1% user + 0% kernel
  0.2% 1995/com.android.phone: 0.1% user + 0% kernel / faults: 2 minor
  0.2% 5399/com.tencent.mobileqq:TMAssistantDownloadSDKService: 0.1% user + 0% kernel / faults: 7 minor
  0.2% 24637/kworker/u16:0: 0% user + 0.2% kernel
  0.2% 720/dubaid: 0.1% user + 0.1% kernel
  0.2% 753/hwpged: 0% user + 0.2% kernel
  0.2% 27864/com.huawei.hwid.core: 0.1% user + 0% kernel
  0.1% 585/android.hardware.memtrack@1.0-service: 0% user + 0.1% kernel
  0.1% 17923/com.huawei.pengine: 0.1% user + 0% kernel / faults: 98 minor 4 major
  0.1% 22492/com.seebaby:pushservice: 0.1% user + 0% kernel / faults: 15 minor
  0.1% 23905/com.huawei.fastapp: 0.1% user + 0% kernel / faults: 15 minor 2 major
  0.1% 24350/com.huawei.health:DaemonService: 0.1% user + 0% kernel / faults: 2 minor
  0.1% 743/CameraDaemon: 0% user + 0% kernel / faults: 28 minor
  0.1% 1908/com.huawei.systemserver: 0.1% user + 0% kernel / faults: 1 minor
  0.1% 3461/com.tencent.mobileqq:MSF: 0.1% user + 0% kernel / faults: 4 minor
  0.1% 83/khungtaskd: 0% user + 0.1% kernel
  0.1% 582/android.hardware.graphics.composer@2.2-service: 0% user + 0.1% kernel
  0.1% 790/vendor.huawei.hardware.biometrics.fingerprint@2.1-service: 0% user + 0.1% kernel
  0.1% 9669/com.tencent.mm:push: 0.1% user + 0% kernel / faults: 7 minor
  0.1% 12411/android.process.media: 0.1% user + 0% kernel / faults: 211 minor
  0.1% 24651/kworker/u16:6: 0% user + 0.1% kernel
  0.1% 25126/kworker/u16:2: 0% user + 0.1% kernel
  0.1% 9773/com.tencent.mm: 0.1% user + 0% kernel / faults: 31 minor 1 major
  0.1% 18896/com.seebaby:AnalysysService: 0.1% user + 0% kernel / faults: 23 minor
  0.1% 565/netd: 0% user + 0.1% kernel / faults: 67 minor
  0.1% 699/irqbalance: 0% user + 0% kernel
  0.1% 1085/hisi_hcc: 0% user + 0.1% kernel
  0.1% 1938/com.huawei.hiview: 0% user + 0% kernel
  0.1% 26682/com.huawei.android.pushagent.PushService: 0% user + 0% kernel / faults: 16 minor
  0.1% 502/servicemanager: 0% user + 0% kernel
  0.1% 626/surfaceflinger: 0% user + 0% kernel
  0% 3577/com.huawei.recsys: 0% user + 0% kernel / faults: 44 minor
  0.1% 13017/com.seebaby:core: 0% user + 0% kernel / faults: 1 minor
  0.1% 22345/kworker/u16:4: 0% user + 0.1% kernel
  0% 27330/com.huawei.skytone: 0% user + 0% kernel / faults: 44 minor
  0% 18457/com.huawei.android.pushagent: 0% user + 0% kernel / faults: 9 minor
  0% 3/ksoftirqd/0: 0% user + 0% kernel
  0% 96/sys_heap: 0% user + 0% kernel
  0% 1045/oal_gpio_rx_dat: 0% user + 0% kernel
  0% 1086/hisi_rxdata: 0% user + 0% kernel
  0% 1332/gnss_engine_hisi: 0% user + 0% kernel
  0% 22377/com.huawei.intelligent:intelligentService: 0% user + 0% kernel / faults: 9 minor 4 major
  0% 24339/kworker/0:2: 0% user + 0% kernel
  0% 18/ksoftirqd/1: 0% user + 0% kernel
  0% 98/camera_heap: 0% user + 0% kernel
  0% 566/zygote64: 0% user + 0% kernel / faults: 77 minor
  0% 573/activity_recognition_service: 0% user + 0% kernel
  0% 2369/com.google.android.gms.persistent: 0% user + 0% kernel / faults: 5 major
  0% 3060/com.huawei.hiaction: 0% user + 0% kernel / faults: 14 minor
  0% 8236/com.huawei.systemmanager:service: 0% user + 0% kernel / faults: 85 minor 2 major
  0% 18902/com.google.android.gms: 0% user + 0% kernel
  0% 24354/kworker/1:3: 0% user + 0% kernel
  0% 1//init: 0% user + 0% kernel
  0% 24/ksoftirqd/2: 0% user + 0% kernel
  0% 30/ksoftirqd/3: 0% user + 0% kernel
  0% 72/mailbox-16: 0% user + 0% kernel
  0% 249/kworker/0:1H: 0% user + 0% kernel
  0% 253/cfinteractive: 0% user + 0% kernel
  0% 581/android.hardware.graphics.allocator@2.0-service: 0% user + 0% kernel / faults: 2 minor
  0% 760/vendor.huawei.hardware.sensors@1.0-service: 0% user + 0% kernel
  0% 1387/kworker/1:1H: 0% user + 0% kernel
  0% 1770/webview_zygote: 0% user + 0% kernel / faults: 82 minor
  0% 3630/com.huawei.hiai.engineservice: 0% user + 0% kernel / faults: 1 minor
  0% 7325/file-storage: 0% user + 0% kernel
  0% 25033/kworker/1:0: 0% user + 0% kernel
  0% 8/rcu_sched: 0% user + 0% kernel
  0% 22/watchdog/2: 0% user + 0% kernel
  0% 28/watchdog/3: 0% user + 0% kernel
  0% 36/ksoftirqd/4: 0% user + 0% kernel
  0% 259/rpmsg_tx_tsk: 0% user + 0% kernel
  0% 471/oeminfo_nvm_server: 0% user + 0% kernel / faults: 1 minor 6 major
  0% 478/f2fs_gc-259:48: 0% user + 0% kernel
  0% 503/hwservicemanager: 0% user + 0% kernel
  0% 572/healthd: 0% user + 0% kernel
  0% 583/android.hardware.health@2.0-service: 0% user + 0% kernel
  0% 596/vendor.huawei.hardware.audio@4.0-service: 0% user + 0% kernel
  0% 614/vendor.huawei.hardware.light@2.0-service: 0% user + 0% kernel / faults: 13 minor
  0% 620/vendor.huawei.hardware.wifi@1.1-service: 0% user + 0% kernel
  0% 624/audioserver: 0% user + 0% kernel
  0% 714/cameraserver: 0% user + 0% kernel
  0% 736/statsd: 0% user + 0% kernel
  0% 740/thermal-daemon: 0% user + 0% kernel
  0% 754/bastetd: 0% user + 0% kernel / faults: 13 minor
  0% 789/storage_info: 0% user + 0% kernel
  0% 844/kworker/3:1H: 0% user + 0% kernel
  0% 1331/oam_hisi: 0% user + 0% kernel
  0% 1334/gnss_supl20clientd_hisi: 0% user + 0% kernel
  0% 1486/kworker/2:1H: 0% user + 0% kernel
  0% 1891/com.huawei.lbs: 0% user + 0% kernel
  0% 2020/com.huawei.android.launcher: 0% user + 0% kernel / faults: 4 minor
  0% 14220/com.baidu.input_huawei: 0% user + 0% kernel
  0% 24179/kworker/u17:2: 0% user + 0% kernel
  0% 25023/kworker/2:3: 0% user + 0% kernel
  0% 25042/kworker/3:0: 0% user + 0% kernel
 +0% 25171/com.android.keyguard: 0% user + 0% kernel
 +0% 25212/iptables-restore: 0% user + 0% kernel
 +0% 25218/ip6tables-restore: 0% user + 0% kernel
 +0% 25235/com.google.android.webview:sandboxed_process0: 0% user + 0% kernel
4.9% TOTAL: 2.5% user + 2.3% kernel + 0% iowait + 0% softirq
```


#### 卡顿工具

上面呢是一些命令可以辅助我们的，但如果觉的通过命令来排查问题不够直观，太麻烦，我们还有图形化的一些工具来使用，TraceView和systrace都是我们熟悉的排查卡顿工具，从实现上这些工具分为两个流派。

第一个流派是**instrument**。获取一段时间内所有函数的调用过程，可以通过分析这些函数调用流程，再进一步分析待优化的点。

第二个流派是**sample**。有选择性或者通过采样方式观察某些函数调用过程，可以通过这些信息推测出可疑的点，然后再继续细化分析。

##### TraceView

Traceview（instrument类型）是用来检查性能的工具，也是吐槽较多的工具，它利用Android Runtime函数调用的Event事件，将函数运行的耗时和调用关系写入到trace文件中，它可以用来查看整个过程有哪些函数调用，但是工具本身带来的性能开销比较大，有时无法反应真是情况。比如一个函数本身耗时是1s，开启TraceView后，可能会变成5s，而且这些函数的耗时并不是成比例放大。

在 Android 5.0 之后，新增了startMethodTracingSampling方法，可以使用基于样本的方式进行分析，以减少分析对运行时的性能影响,新增了 sample 类型后，就需要我们在开销和信息丰富度之间做好权衡。

**优势：**

图形的形式展示执行时间、调用栈等。
信息全面，包含所有线程。

**劣势：**

运行时开销严重，整体都会变慢，可能会带偏我们的优化方向。

使用方式：

```
Debug.startMethodTracing("");
// 需要检测的代码片段
...
Debug.stopMethodTracing();
```

最终生成的生成文件在sd卡：Android/data/packagename/files。


##### Systrace

systrace是 Android 4.1 新增的性能分析工具，它可以跟踪系统的I/O操作、CPU负载、Surface渲染、GC等事件。

systrace 利用了 Linux 的ftrace调试工具，相当于在系统各个关键位置都添加了一些性能探针，也就是在代码里加了一些性能监控的埋点。Android 在 ftrace 的基础上封装了atrace，并增加了更多特有的探针，例如 Graphics、Activity Manager、Dalvik VM、System Server 等。

systrace工具只能监控特定系统调用的耗时情况，所以它是sample类型，而且性能开销特别低。但是它不支持应用程序代码的耗时分析，所以在使用时有一些局限性。

**作用：**

监控和跟踪API调用、线程运行情况，生成HTML报告。

**使用方式：**

1，通过Android Device Monitor获取。

AndroidSDK/tools目录下，通过monitor.bat用Android Device Monitor可视化工具得到。

2，通过python脚本抓取。

- 装python2.X版本，Systrace脚本不支持3.X版本。

- 通过python脚本执行AndroidSDK\platform-tools\systrace\目录下的systrace.py文件

- 可以配置一些参数，类似于通过Android Device Monitor抓取时步骤2配置的显示信息，若不选择则默认全部抓取。

- 配置一些其他实用参数:

> - -o： 指定文件输出位置和文件名
> - -t： 抓取systrace的时间长度
> - -a： 指定特殊进程包名（自己加Label时必须加上）

3，Chrome浏览器（必须）。

在地址栏输入chrome://tracing命令，然后将生成的trace.html文件拖进来，或者通过load按钮导入。

4，常用的一些快捷键：

- W: 放大横轴，用于查看耗时方法细节；
- S: 缩小横轴，用于查看整体情况；
- A： 将面板左移；
- D: 将面板右移；
- M: 高亮某一段耗时内容。

**优势：**

- 轻量级，开销小。
- 它能够直观地反映CPU的利用率。
- 右侧的Alerts能够根据我们应用的问题给出具体的建议，比如说，它会告诉我们App界面的绘制比较慢或者GC比较频繁。


**思考：**

由于系统预留了Trace.beginSection() 接口来**监听应用程序的调用耗时**，那我们有没有办法在 systrace 上面自动增加应用程序的耗时分析呢？

划重点了，我们可以通过**编译时给每个函数插桩的方式**来实现，也就是在重要函数的入口和出口分别增加Trace.beginSection和Trace.endSection，为了性能的考虑，我们需要过滤掉大部分指令数比较少的函数，这样就能实现应用程序的耗时监控。


##### Simpleperf

如果我们想分析Navive函数的调用，上面的工具就不能使用了，Android5.0新增了Simpleperf性能分析工具，它利用 CPU 的性能监控单元（PMU）提供的硬件 perf 事件。使用 Simpleperf可以看到所有的 Native 代码的耗时，有时候一些 Android系统库的调用对分析问题有比较大的帮助，例如加载 dex、verify class 的耗时等。这块涉及到好多C++的东西，如果感兴趣的同学可以继续深入下。

**方法选型：**

选择哪种工具，需要看具体的场景。我来汇总一下，如果需要分析Native 代码的耗时，可以选择 Simpleperf，如果想分析系统调用，可以选择 systrace，如果想分析整个程序执行流程的耗时，可以选择 Traceview或者插桩版本的systrace。

##### StrictMode

我们也可以在开发过程中加入一些检测机制，严苛模式就是此类方法，它是一种运行时检测机制，帮助程序员来检测代码中一些不规范的问题，StrictMode这个工具是非常强大的，但是我们可能因为对它不熟悉而忽略掉它。StrictMode主要用来检测两大问题：

1、线程策略：

线程策略的检测内容，是一些自定义的耗时调用、磁盘读取操作以及网络请求等。

2、虚拟机策略：

虚拟机策略的检测内容如下：

- Activity泄漏
- Sqlite对象泄漏
- 检测实例数量

**使用方式：**

如果要在应用中使用StrictMode，只需要在Applicaitoin的onCreate方法中对StrictMode进行统一配置，代码如下所示：


```
private void initStrictMode() {
    // 1、设置Debug标志位，仅仅在线下环境才使用StrictMode
    if (DEV_MODE) {
        // 2、设置线程策略
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog() //在Logcat 中打印违规异常信息
//              .penaltyDialog() //也可以直接跳出警报dialog
//              .penaltyDeath() //或者直接崩溃
                .build());
        // 3、设置虚拟机策略
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects() //API等级11
                .penaltyLog()
                .build());
    }
}
```

**注意：**

- 通常情况下StrictMode给出的耗时相对实际情况偏高，并不是真正的耗时数据。
- 在线上环境即Release版本不建议开启严格模式。
- 严格模式无法监控JNI中的磁盘IO和网络请求。
- 应用中并非需要解决全部的违例情况，比如有些IO操作必须在主线程中进行。


##### Profilo

Profilo是一个用于收集应用程序生产版本的性能跟踪的Android库。

对于Profilo来说，它集成了atrace功能，ftrace 所有的性能埋点数据都会通过 trace_marker 文件写入到内核缓冲区，Profilo 使用了 PLT Hook 拦截了写入操作，以选择部分关心的事件去做特定的分析。这样所有的 systrace 的探针我们都可以拿到，例如四大组件生命周期、锁等待时间、类校验、GC 时间等等。不过大部分的 atrace 事件都比较笼统，从事件“B|pid|activityStart”，我们无法明确知道该事件具体是由哪个 Activity 来创建的。

此外，使用Profilo还能够快速获取Java堆栈。由于获取堆栈需要暂停主线程的运行，所以profilo通过间隔发送 SIGPROF 信号这样一种类似 Native 崩溃捕捉的方式去快速获取 Java 堆栈。

Profilo能够低耗时地快速获取Java堆栈的具体实现原理为当Signal Handler 捕获到信号后，它就会获取到当前正在执行的 Thread，通过 Thread 对象就可以拿到当前线程的 ManagedStack，ManagedStack 是一个单链表，它保存了当前的 ShadowFrame 或者 QuickFrame 栈指针，先依次遍历 ManagedStack 链表，然后遍历其内部的 ShadowFrame 或者 QuickFrame 还原一个可读的调用栈，从而 unwind 出当前的 Java 堆栈。

但是目前 Profilo 快速获取堆栈的功能不支持 Android 8.0 和 Android 9.0，并且它内部使用了Hook等大量的黑科技手段，鉴于稳定性问题，建议采取抽样部分用户的方式来开启该功能。

[Profilo项目地址](httpshttps://github.com/facebookincubator/profilo)


#### 卡顿监控

##### 为什么还需要自动化卡顿检测方案？

- Cpu Profiler、Systrace等系统工具仅适合线下针对性分析。
- 线上及测试环境需要自动化的卡顿检方案来定位卡顿，同时，更重要的是，它能记录卡顿发生时的场景。

##### 卡顿检测方案原理

原理源于Android的消息处理机制，**一个线程不管有多少Handler，它只会有一个Looper存在，主线程执行的任何代码都会通过Looper.loop()方法执行。而在Looper函数中，它有一个mLogging对象，这个对象在每个message处理前后都会被调用。主线程发生了卡顿，那一定是在dispatchMessage()方法中执行了耗时操作。那么，我们就可以通过这个mLogging对象对dispatchMessage()进行监控。**

**具体实现：**

首先，我们看下Looper用于执行消息循环的loop()方法，关键代码如下所示：

```
/**
 * Run the message queue in this thread. Be sure to call
 * {@link #quit()} to end the loop.
 */
public static void loop() {

    ...
    
    for (;;) {
        Message msg = queue.next(); // might block
        if (msg == null) {
            // No message indicates that the message queue is quitting.
            return;
        }

        // This must be in a local variable, in case a UI event sets the logger
        final Printer logging = me.mLogging;
        if (logging != null) {
            // 1
            logging.println(">>>>> Dispatching to " + msg.target + " " +
                    msg.callback + ": " + msg.what);
        }
    
        ...
        
        try {
             // 2 
             msg.target.dispatchMessage(msg);
            dispatchEnd = needEndTime ? SystemClock.uptimeMillis() : 0;
        } finally {
            if (traceTag != 0) {
                Trace.traceEnd(traceTag);
            }
        }
        
        ...
        
        if (logging != null) {
            // 3
            logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
        }
}
```

loop()方法中,我们可以看到，在执行消息前输出的">>>>> Dispatching to "，在执行消息后输出的"<<<<< Finished to ",我们就可以由此来判断消息执行的前后时间点。

具体步骤：

- **首先，我们使用Looper.getMainLooper().setMessageLogging()去设置我们自己的Printer实现类去打印输出logging。在每个message执行的之前和之后都会调用我们设置的这个Printer实现类。**
- **当我们匹配到">>>>> Dispatching to "之后，我们就可以执行一行代码：也就是在指定的时间阈值之后，我们在子线程去执行一个任务，这个任务去获取当前主线程的堆栈信息以及当前的一些场景信息，比如：内存大小、网络状态等。**
- **如果在指定的阈值之内匹配到了"<<<<< Finished to "，那么说明message就被执行完成了，则表明此时没有产生我们认为的卡顿效果，我们就可以将这个子线程任务取消。**

##### AndroidPerformanceMonitor（BlockCanary）

它是一个非侵入式的性能监控组件，可以通过通知的形式弹出卡顿信息。**它的原理**就是我们刚刚讲述到的卡顿监控的实现原理。

**使用步骤：**

首先，我们需要在moudle的build.gradle下配置依赖，如下所示：

```
// release：项目中实现了线上监控体系的时候去使用
api 'com.github.markzhai:blockcanary-android:1.5.0'

// 仅在debug包启用BlockCanary进行卡顿监控和提示的话，可以这么用
debugApi 'com.github.markzhai:blockcanary-android:1.5.0'
releaseApi 'com.github.markzhai:blockcanary-no-op:1.5.0'
```

其次，在Application的onCreate方法中开启卡顿监控：


```
// 注意在主进程初始化调用
BlockCanary.install(this, new AppBlockCanaryContext()).start();
```

最后，继承BlockCanaryContext类去实现自己的监控配置上下文类：


```
public class AppBlockCanaryContext extends BlockCanaryContext {
    // 实现各种上下文，包括应用标识符，用户uid，网络类型，卡顿判断阙值，Log保存位置等等

    /**
    * 提供应用的标识符
    *
    * @return 标识符能够在安装的时候被指定，建议为 version + flavor.
    */
    public String provideQualifier() {
        return "unknown";
    }

    /**
    * 提供用户uid，以便在上报时能够将对应的
    * 用户信息上报至服务器 
    *
    * @return user id
    */
    public String provideUid() {
        return "uid";
    }

    /**
    * 提供当前的网络类型
    *
    * @return {@link String} like 2G, 3G, 4G, wifi, etc.
    */
    public String provideNetworkType() {
        return "unknown";
    }

    /**
    * 配置监控的时间区间，超过这个时间区间    ，BlockCanary将会停止, use
    * with {@code BlockCanary}'s isMonitorDurationEnd
    *
    * @return monitor last duration (in hour)
    */
    public int provideMonitorDuration() {
        return -1;
    }

    /**
    * 指定判定为卡顿的阈值threshold (in millis),  
    * 你可以根据不同设备的性能去指定不同的阈值
    *
    * @return threshold in mills
    */
    public int provideBlockThreshold() {
        return 1000;
    }

    /**
    * 设置线程堆栈dump的间隔, 当阻塞发生的时候使用, BlockCanary 将会根据
    * 当前的循环周期在主线程去dump堆栈信息
    * <p>
    * 由于依赖于Looper的实现机制, 真实的dump周期 
    * 将会比设定的dump间隔要长(尤其是当CPU很繁忙的时候).
    * </p>
    *
    * @return dump interval (in millis)
    */
    public int provideDumpInterval() {
        return provideBlockThreshold();
    }

    /**
    * 保存log的路径, 比如 "/blockcanary/", 如果权限允许的话，
    * 会保存在本地sd卡中
    *
    * @return path of log files
    */
    public String providePath() {
        return "/blockcanary/";
    }

    /**
    * 是否需要通知去通知用户发生阻塞
    *
    * @return true if need, else if not need.
    */
    public boolean displayNotification() {
        return true;
    }

    /**
    * 用于将多个文件压缩为一个.zip文件
    *
    * @param src  files before compress
    * @param dest files compressed
    * @return true if compression is successful
    */
    public boolean zip(File[] src, File dest) {
        return false;
    }

    /**
    * 用于将已经被压缩好的.zip log文件上传至
    * APM后台
    *
    * @param zippedFile zipped file
    */
    public void upload(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    /**
    * 用于设定包名, 默认使用进程名，
    *
    * @return null if simply concern only package with process name.
    */
    public List<String> concernPackages() {
        return null;
    }

    /**
    * 使用 @{code concernPackages}方法指定过滤的堆栈信息 
    *
    * @return true if filter, false it not.
    */
    public boolean filterNonConcernStack() {
        return false;
    }

    /**
    * 指定一个白名单, 在白名单的条目将不会出现在展示阻塞信息的UI中
    *
    * @return return null if you don't need white-list filter.
    */
    public List<String> provideWhiteList() {
        LinkedList<String> whiteList = new LinkedList<>();
        whiteList.add("org.chromium");
        return whiteList;
    }

    /**
    * 使用白名单的时候，是否去删除堆栈在白名单中的文件
    *
    * @return true if delete, false it not.
    */
    public boolean deleteFilesInWhiteList() {
        return true;
    }

    /**
    * 阻塞拦截器, 我们可以指定发生阻塞时应该做的工作
    */
    public void onBlock(Context context, BlockInfo blockInfo) {

    }
}
```

发生卡顿时BlockCanary提供的图形界面可供开发和测试人员直接查看卡顿原因之外。其最大的作用还是在线上环境或者自动化monkey测试的环节进行大范围的log采集与分析，对于分析的纬度，可以从以下两个纬度来进行：

- 卡顿时间。
- 根据同堆栈出现的卡顿次数来进行排序和归类。

**BlockCanary优势：**

- 非侵入式。
- 方便精准，能够定位到代码的某一行代码。


**这种自动检测卡顿的方案有什么问题吗？**

假设主线程在T1到T2的时间段内发生了卡顿，卡顿检测方案获取卡顿时的堆栈信息是T2时刻，但是实际上发生卡顿的时刻可能是在这段时间区域内另一个耗时过长的函数，那么可能在我们捕获卡顿的时刻时，真正的卡顿时机已经执行完成了，所以在T2时刻捕获到的一个卡顿信息并不能够反映卡顿的现场，也就是最后呈现出来的堆栈信息仅仅只是一个表象，并不是真正问题的藏身之处。

**不过对于线上大数据来说，抓取到线上耗时方法的概率会更大一些。线上依然可以使用基于消息队列的方法。**

**我们如何对这种情况进行优化呢？**

我们可以**获取卡顿周期内的多个堆栈**，而不仅仅是最后一个，这样的话，如果发生了卡顿，我们就**可以根据这些堆栈信息来清晰地还原整个卡顿现场**。因为我们有卡顿现场的多个堆栈信息，我们完全知道卡顿时究竟发生了什么，**到底哪些函数它的调用时间比较长**。

但是这种海量卡顿堆栈的处理又存在着另一个问题，那就是高频卡顿上报量太大，服务器压力较大，这里我们来分析下**如何减少服务端对堆栈信息的处理量**。

在出现卡顿的情况下，我们**采集到了多个堆栈，大概率的情况下，可能会存在多个重复的堆栈**，而这个重复的堆栈信息才是我们应该关注的地方。我们可以**对一个卡顿下的堆栈进行能hash排重，找出重复的堆栈**。这样，**服务器需要处理的数据量就会大大减少，同时也过滤出了我们需要重点关注的对象**。对于开发人员来说，就能更快地找到卡顿的原因。


##### ANR分析与实战

首先我们回顾下ANR的常见类型：

- KeyDispatchTimeout：按键事件在5s的时间内没有处理完成。
- BroadcastTimeout：广播接收器在前台10s，后台60s的时间内没有响应完成。
- ServiceTimeout：服务在前台20s，后台200s的时间内没有处理完成。

线下ANR的排查步骤就不赘述了。

现在思考一个问题，我们如何线上监控？

**线上ANR监控方式：**

理论上我我们使用FileObserver**可以监听 /data/anr/traces.txt的变化，利用它可以实现线上ANR的监控**，但是它有一个致命的缺点，就是高版本ROM需要root权限，解决方案是只能通过海外Google Play服务、国内Hardcoder的方式去规避。这在国内显然是不现实的，有没有更好的实现方式呢？

那就是**ANR-WatchDog**。

[ANR-WatchDog项目地址](httpshttps://github.com/SalomonBrys/ANR-WatchDog)

ANR-WatchDog是一种非侵入式的ANR监控组件，可以用于线上ANR的监控，接下来，我们就使用ANR-WatchDog来监控ANR。

首先，在我们项目的app/build.gradle中添加如下依赖：

```
implementation 'com.github.anrwatchdog:anrwatchdog:1.4.0'
```

然后，在应用的Application的onCreate方法中添加如下代码启动ANR-WatchDog：


```
new ANRWatchDog().start();
```

它的初始化方式非常地简单它内部的实现也非常简单，整个库只有两个类，一个是ANRWatchDog，另一个是ANRError。

接下来我们来看一下ANRWatchDog的实现方式。


```
/**
* A watchdog timer thread that detects when the UI thread has frozen.
*/
public class ANRWatchDog extends Thread {
```

可以看到，ANRWatchDog实际上是继承了Thread类，也就是它是一个线程，对于线程来说，最重要的就是其run方法，如下所示：


```
private static final int DEFAULT_ANR_TIMEOUT = 5000;

private volatile long _tick = 0;
private volatile boolean _reported = false;

private final Runnable _ticker = new Runnable() {
    @Override public void run() {
        _tick = 0;
        _reported = false;
    }
};

@Override
public void run() {
    // 1、首先，将线程命名为|ANR-WatchDog|。
    setName("|ANR-WatchDog|");

    // 2、接着，声明了一个默认的超时间隔时间，默认的值为5000ms。
    long interval = _timeoutInterval;
    // 3、然后，在while循环中通过_uiHandler去post一个_ticker Runnable。
    while (!isInterrupted()) {
        // 3.1 这里的_tick默认是0，所以needPost即为true。
        boolean needPost = _tick == 0;
        // 这里的_tick加上了默认的5000ms
        _tick += interval;
        if (needPost) {
            _uiHandler.post(_ticker);
        }

        // 接下来，线程会sleep一段时间，默认值为5000ms。
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            _interruptionListener.onInterrupted(e);
            return ;
        }

        // 4、如果主线程没有处理Runnable，即_tick的值没有被赋值为0，则说明发生了ANR，第二个_reported标志位是为了避免重复报道已经处理过的ANR。
        if (_tick != 0 && !_reported) {
            //noinspection ConstantConditions
            if (!_ignoreDebugger && (Debug.isDebuggerConnected() || Debug.waitingForDebugger())) {
                Log.w("ANRWatchdog", "An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))");
                _reported = true;
                continue ;
            }

            interval = _anrInterceptor.intercept(_tick);
            if (interval > 0) {
                continue;
            }

            final ANRError error;
            if (_namePrefix != null) {
                error = ANRError.New(_tick, _namePrefix, _logThreadsWithoutStackTrace);
            } else {
                // 5、如果没有主动给ANR_Watchdog设置线程名，则会默认会使用ANRError的NewMainOnly方法去处理ANR。
                error = ANRError.NewMainOnly(_tick);
            }
           
           // 6、最后会通过ANRListener调用它的onAppNotResponding方法，其默认的处理会直接抛出当前的ANRError，导致程序崩溃。 
           _anrListener.onAppNotResponding(error);
            interval = _timeoutInterval;
            _reported = true;
        }
    }
}
```

- 在注释1处，我们将线程命名为了|ANR-WatchDog|
- 在注释2处，声明了一个默认的超时间隔时间，默认的值为5000ms。
- 注释3处，在while循环中通过_uiHandler去post一个_ticker Runnable。线程会sleep一段时间，默认值为5000ms。
- 在注释4处，如果主线程没有处理Runnable，即_tick的值没有被赋值为0，则说明发生了ANR，第二个_reported标志位是为了避免重复报道已经处理过的ANR。如果发生了ANR，就会调用接下来的代码，开始会处理debug的情况，
- 注释5处，如果没有主动给ANR_Watchdog设置线程名，则会默认会使用ANRError的NewMainOnly方法去处理ANR。ANRError的NewMainOnly方法如下所示：


```
/**
 * The minimum duration, in ms, for which the main thread has been blocked. May be more.
 */
public final long duration;

static ANRError NewMainOnly(long duration) {
    // 1、获取主线程的堆栈信息
    final Thread mainThread = Looper.getMainLooper().getThread();
    final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();

    // 2、返回一个包含主线程名、主线程堆栈信息以及发生ANR的最小时间值的实例。
    return new ANRError(new $(getThreadTitle(mainThread), mainStackTrace).new _Thread(null), duration);
}
```

可以看到，在注释1处，首先获了主线程的堆栈信息，然后返回了一个包含主线程名、主线程堆栈信息以及发生ANR的最小时间值的实例。（我们可以**改造其源码在此时添加更多的卡顿现场信息，如CPU 使用率和调度信息、内存相关信息、I/O 和网络相关的信息等等**）

接下来，我们再回到ANRWatchDog的run方法中的注释6处，最后这里会通过ANRListener调用它的onAppNotResponding方法，其默认的处理会直接抛出当前的ANRError，导致程序崩溃。对应的代码如下所示：


```
private static final ANRListener DEFAULT_ANR_LISTENER = new ANRListener() {
    @Override public void onAppNotResponding(ANRError error) {
        throw error;
    }
};
```

ANR-WatchDog自身就实现了一个我们自身也可以去实现的**ANRListener，通过它，我们就可以对ANR事件去做一个自定义的处理**，比如将堆栈信息压缩后保存到本地，并在适当的时间上传到APM后台。

**小结：**

ANR-WatchDog是一种非侵入式的ANR监控方案，它能够弥补我们在高版本中没有权限去读取traces.txt文件的问题，在线上这两种方案我们需要结合使用。

在之前我们还讲到了AndroidPerformanceMonitor，那么它和ANR-WatchDog有什么区别呢？

对于AndroidPerformanceMonitor来说，它是监控我们主线程中每一个message的执行，它会在主线程的每一个message的前后打印一个时间戳，然后，我们就可以据此计算每一个message的具体执行时间，但是我们需要注意的是一个message的执行时间通常是非常短暂的，也就是很难达到ANR这个级别。然后我们来看看ANR-WatchDog的原理，它是不管应用是如何执行的，它只会看最终的结果，即sleep 5s之后，我就看主线程的这个值有没有被更改。如果说被改过，就说明没有发生ANR，否则，就表明发生了ANR。

根据这两个库的原理，我们便可以判断出它们分别的适用场景**，对于AndroidPerformanceMonitor来说，它适合监控卡顿，因为每一个message它执行的时间并不长。对于ANR-WatchDog来说，它更加适合于ANR监控的补充**。

虽然ANR-WatchDog解决了在高版本系统没有权限读取 /data/anr/traces.txt 文件的问题，但是在Java层去获取所有线程堆栈以及各种信息非常耗时，对于卡顿场景不一定合适，它可能会进一步加剧用户的卡顿。**如果是对性能要求比较高的应用,可以通过Hook Native层的方式去获得所有线程的堆栈信息（这块的知识点有些深入,具体可以参考[下面的链接](httpshttps://juejin.im/post/5e49fc29e51d4526d326b056#heading-17)）**。

##### 卡顿单点问题检测方案

除了自动化的卡顿与ANR监控之外，我们还需要进行卡顿单点问题的检测，因为上述两种检测方案的并不能满足所有场景的检测要求，比如：


```
比如我有很多的message要执行，但是每一个message的执行时间
都不到卡顿的阈值，那自动化卡顿检测方案也就不能够检测出卡
顿，但是对用户来说，用户就觉得你的App就是有些卡顿。
```

常见的单点问题有主线程IPC、DB操作等等，这里我就拿主线程IPC来说，因为IPC其实是一个很耗时的操作，但是在实际开发过程中，我们可能对IPC操作没有足够的重视，所以，我们经常在主程序中去做频繁IPC操作，所以说，这种耗时它可能并不到你设定卡顿的一个阈值，接下来，我们看一下，对于IPC问题，我们应该去监测哪些指标。

**常规方案：**

常规方案就是在**IPC的前后加上埋点**。但是，这种方式**不够优雅**，而且，在平常开发过程中我们经常忘记某个埋点的真正用处，同时它的**维护成本也非常大**。

接下来，我们讲解一下IPC问题监测的技巧。

这里我们介绍一种优雅的实现方案，实现方案无非就是ARTHook或AspectJ这两种方案，这里我们需要去监控IPC操作，那么，我们应该选用哪种方式会更好一些呢？（利用epic实现ARTHook)

要回答这个问题，就需要我们对ARTHook和AspectJ这两者的思想有足够的认识，**对应ARTHook来说，其实我们可以用它来去Hook系统的一些方法，因为对于系统代码来说，我们无法对它进行更改，但是我们可以Hook住它的一个方法，在它的方法体里面去加上自己的一些代码。但是，对于AspectJ来说，它只能针对于那些非系统方法，也就是我们App自己的源码，或者是我们所引用到的一些jar、aar包**。因为AspectJ实际上是往我们的具体方法里面插入相对应的代码，所以说，他不能够针对于我们的系统方法去做操作，在这里，我们就需要采用ARTHook的方式去进行IPC操作的监控。

这块本次就不带大家深入了解了，后面有机会再详细深入。

##### 优雅监控耗时盲区

尽管我们在应用中监控了很多的耗时区间，但是还是有一些耗时区间我们还没有捕捉到，如onResume到列表展示的间隔时间，这些时间在我们的统计过程中很容易被忽视，这里我们举一个小栗子：


```
我们在Activity的生命周期中post了一个message，那这个message很可能其中
执行了一段耗时操作，那你知道这个message它的具体执行时间吗？这个message其实
很有可能在列表展示之前就执行了，如果这个message耗时1s，那么列表的展示
时间就会延迟1s，如果是200ms，那么我们设定的自动化卡顿检测就无法
发现它，那么列表的展示时间就会延迟200ms。
```


###### 1、耗时盲区监控难点

首先，我们可以通过细化监控的方式去获取耗时的一些盲区，但是我们却不知道在这个盲区中它执行了什么操作。其次，对于线上的一些耗时盲区，我们是无法进行排查的。

这里，我们先来看看如何建立耗时盲区监控的线下方案。

###### 2、耗时盲区监控线下方案

这里我们直接使用TraceView去检测即可，因为它能够清晰地记录线程在具体的时间内到底做了什么操作，特别适合一段时间内的盲区监控。

然后，我们来看下如何建立耗时盲区监控的线上方案。

###### 3、耗时盲区监控线上方案

我们知道主线程的所有方法都是通过message来执行的，还记得在之前我们学习了一个库：AndroidPerformanceMonitor，我们是否可以通过这个mLogging来做盲区检测呢？通过这个mLogging确实可以知道我们主线程发生的message，但是通过mLogging无法获取具体的调用栈信息，因为它**所获取的调用栈信息都是系统回调回来的，它并不知道当前的message是被谁抛出来的**，所以说，这个方案并不够完美。

那么，我们是否可以通过**AOP的方式去切Handler方法**呢？比如sendMessage、sendMessageDeleayd方法等等，这样我们就可以知道发生message的一个堆栈，但是这种方案也存在着一个问题，就是**它不清楚准确的执行时间**，我们切了这个handler的方法，仅仅只知道它具体是在哪个地方被发的和它所对应的堆栈信息，但是无法获取准确的执行时间。如果我们想知道在onResume到列表展示之间执行了哪些message，那么通过AOP的方式也无法实现。

那么，**最终的耗时盲区监控的一个线上方案就是使用一个统一的Handler**，定制了它的两个方法，一个是sendMessageAtTime，另外一个是dispatchMessage方法。因为对于发送message，不管调用哪个方法最终都会调用到一个是sendMessageAtTime这个方法，而处理message呢，它最终会调用dispatchMessage方法。然后，我们**需要定制一个gradle插件，来实现自动化的接入我们定制好的handler，通过这种方式，我们就能在编译期间去动态地替换所有使用Handler的父类为我们定制好的这个handler。这样，在整个项目中，所有的sendMessage和handleMessage都会经过我们的回调方法**。

对于实现全局替换handler的gradle插件，除了使用AspectJ实现之外，这里推荐一个已有的项目：[DroidAssist](httpshttps://github.com/didi/DroidAssist)。


###### 4、耗时盲区监控方案总结

耗时盲区监控是我们卡顿监控中不可或缺的一个环节，也是卡顿监控全面性的一个重要保障。而需要注意的是，TraceView仅仅适用于线下的一个场景，同时对于TraceView来说，它可以用于监控我们系统的message。而最后介绍的动态替换的方式其实是适合于线上的，同时，它仅仅监控应用自身的一个message。


#### 卡顿优化技巧总结

##### 1、卡顿优化实践经验

如果应用出现了卡顿现象，那么可以考虑以下方式进行优化：

- 对于耗时的操作，我们可以考虑**异步或延迟初始化**的方式，这样可以解决大多数的问题。但大家一定要注意代码的优雅性。
- 对于布局加载优化，可以采用**AsyncLayoutInflater或者是X2C的方式来优化主线程IO以及反射导致的消耗**，同时需要注意，对于重绘问题，要给与一定的重视。
- 此外，内存问题也可能会导致应用界面的卡顿，我们可以通过**降低内存占用的方式来减少GC的次数以及时间**，而GC的次数和时间我们可以通过log查看。


##### 2、卡顿优化工具建设

- Systrace：我们可以很方便地看出来它的CPU使用情况。开销也比较小。
- TraceView：我们可以很方便地看出来每一个线程它在特定的时间内做了什么操作，但TraceView它的开销相对比较大，有时候可能会被带偏优化方向。
- 同时，需要注意，StrictMode也是一个非常强大的工具。

然后，我们介绍了自动化工具建设以及优化方案。我们介绍了两个工具**，AndroidPerformanceMonitor以及ANR-WatchDog**。同时针对于AndroidPerformanceMonitor的问题，我们采用了高频采集，以找出重复率高的堆栈这样一种方式进行优化，在学习的过程中，我们不仅需要学会怎样去使用工具，更要去理解它们的实现原理以及各自的使用场景。

#### 卡顿优化的常见问题

##### 1、你是怎么做卡顿优化的？

项目初期 - 成长期 - 成熟期

- 系统工具定位、解决
- 自动化卡顿方案及优化
- 线上监控及线下监测工具的建设

**引用文章的一段话：**

我做卡顿优化也是经历了一些阶段，最初我们的项目当中的一些模块出现了卡顿之后，我是通过系统工具进行了定位，我使用了Systrace，然后看了卡顿周期内的CPU状况，同时结合代码，对这个模块进行了重构，将部分代码进行了异步和延迟，在项目初期就是这样解决了问题。

但是呢，随着我们项目的扩大，线下卡顿的问题也越来越多，同时，在线上，也有卡顿的反馈，但是线上的反馈卡顿，我们在线下难以复现，于是我们开始寻找自动化的卡顿监测方案，其思路是来自于Android的消息处理机制，主线程执行任何代码都会回到Looper.loop方法当中，而这个方法中有一个mLogging对象，它会在每个message的执行前后都会被调用，我们就是利用这个前后处理的时机来做到的自动化监测方案的。同时，在这个阶段，我们也完善了线上ANR的上报，我们采取的方式就是监控ANR的信息，同时结合了ANR-WatchDog，作为高版本没有文件权限的一个补充方案。

在做完这个卡顿检测方案之后呢，我们还做了线上监控及线下检测工具的建设，最终实现了一整套完善，多维度的解决方案。


##### 2、你是怎么样自动化的获取卡顿信息？

我们的思路是来自于Android的消息处理机制，主线程执行任何代码它都会走到Looper.loop方法当中，而这个函数当中有一个mLogging对象，它会在每个message处理前后都会被调用，而主线程发生了卡顿，那就一定会在dispatchMessage方法中执行了耗时的代码，那我们在这个message执行之前呢，我们可以在子线程当中去postDelayed一个任务，这个Delayed的时间就是我们设定的阈值，如果主线程的messaege在这个阈值之内完成了，那就取消掉这个子线程当中的任务，如果主线程的message在阈值之内没有被完成，那子线程当中的任务就会被执行，它会获取到当前主线程执行的一个堆栈，那我们就可以知道哪里发生了卡顿。

经过实践，我们发现这种方案获取的堆栈信息它不一定是准确的，因为获取到的堆栈信息它很可能是主线程最终执行的一个位置，而真正耗时的地方其实已经执行完成了，于是呢，我们就对这个方案做了一些优化，我们采取了高频采集的方案，也就是在一个周期内我们会多次采集主线程的堆栈信息，如果发生了卡顿，那我们就将这些卡顿信息压缩之后上报给APM后台，然后找出重复的堆栈信息，这些重复发生的堆栈大概率就是卡顿发生的一个位置，这样就提高了获取卡顿信息的一个准确性。

##### 3、卡顿优化的一整套方案？

首先，针对卡顿，我们采用了线上、线下工具相结合的方式，线下工具我们需要尽可能早地去暴露问题，而针对于线上工具呢，我们侧重于监控的全面性、自动化以及异常感知的灵敏度。

#### 总结：

其实看到这里，大家会发现要做好应用的卡顿优化的确不是一件简单的事，它需要你有成体系的知识构建基底。

相信看到这里，你也一定有所收获,但不论什么方案，都需要我们**多思考、多实践、多总结**。


#### 参考链接

https://www.jianshu.com/p/7e9ca2c73c97(TraceView使用)

https://blog.csdn.net/niubitianping/article/details/72617864(TraceView分析)

https://blog.csdn.net/vicwudi/article/details/100191529(systrace分析)

https://www.cnblogs.com/ldq2016/p/8480153.html

https://juejin.im/post/5e41fb7de51d4526c80e9108

https://juejin.im/post/5e49fc29e51d4526d326b056#heading-17

https://blog.csdn.net/lyb2518/article/details/75026895（StrictMode）

https://blog.csdn.net/happylishang/article/details/104196560（缓冲模式）

https://time.geekbang.org/column/article/a36d394d00f5efecd12ca8e6c3775d30/share?code=XowV4att8DiSVrHktj9%2FFSsAaiAqqZq70iwM52xn-zA%3D&oss_token=8f3cf31255d57c57

[微信卡顿分析工具之Trace Canary](https://mp.weixin.qq.com/s/0EprsJ7sXKmphghMsU3aGw)

