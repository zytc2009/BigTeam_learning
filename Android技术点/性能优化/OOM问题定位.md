[Toc]

定位线上OOM问题是android开发比较重要的一个技术，如何构建高效的OOM框架，值得我们一直追寻和学习

### OOM原因分析

<img src="images\OOM的原因.png" width="300" />

Android 虚拟机最终抛出OutOfMemoryError的代码位于/art/runtime/thread.cc。

```c++
void Thread::ThrowOutOfMemoryError(const char* msg)
参数 msg 携带了 OOM 时的错误信息
```

下面两个地方都会调用上面方法抛出OutOfMemoryError错误，这也是Android中发生OOM的主要原因。

#### 堆内存分配失败

系统源码文件：/art/runtime/gc/heap.cc

```c++
void Heap::ThrowOutOfMemoryError(Thread* self, size_t byte_count, AllocatorType allocator_type)
抛出时的错误信息：
    oss << "Failed to allocate a " << byte_count << " byte allocation with " << total_bytes_free  << " free bytes and " << PrettySize(GetFreeMemoryUntilOOME()) << " until OOM";
```

这是在进行堆内存分配时抛出的OOM错误，这里也可以细分成两种不同的类型：

1. 为对象分配内存时达到进程的内存上限。由Runtime.getRuntime.MaxMemory()可以得到Android中每个进程被系统分配的内存上限，当进程占用内存达到这个上限时就会发生OOM，这也是Android中最常见的OOM类型。
2. 没有足够大小的连续地址空间。这种情况一般是进程中存在大量的内存碎片导致的，其堆栈信息会比第一种OOM堆栈多出一段信息：failed due to fragmentation (required continguous free “<< required_bytes << “ bytes for a new buffer where largest contiguous free ” << largest_continuous_free_pages << “ bytes)”; 其详细代码在art/runtime/gc/allocator/rosalloc.cc中，这里不作详述。

#### 创建线程失败

系统源码文件：/art/runtime/thread.cc

```C++
void Thread::CreateNativeThread(JNIEnv* env, jobject java_peer, size_t stack_size, bool is_daemon)
抛出时的错误信息：
    "Could not allocate JNI Env"
  或者
    StringPrintf("pthread_create (%s stack) failed: %s", PrettySize(stack_size).c_str(), strerror(pthread_create_result)));
```

这是创建线程时抛出的OOM错误，且有多种错误信息。下面是根据源码整理的Android中创建线程的步骤，其中两个关键节点是创建JNIEnv结构体和创建线程，而这两步均有可能抛出OOM。

<img src="images\创建线程失败.png" width="800" />

##### 创建JNI失败

创建JNIEnv可以归为两个步骤：

- 通过Andorid的匿名共享内存（Anonymous Shared Memory）分配 4KB（一个page）内核态内存。
- 再通过Linux的mmap调用映射到用户态虚拟内存地址空间。

第一步：创建匿名共享内存时，需要打开/dev/ashmem文件，所以需要一个FD（文件描述符）。此时，如果创建的FD数已经达到上限，则会导致创建JNIEnv失败，抛出错误信息如下：

```
E/art: ashmem_create_region failed for 'indirect ref table': Too many open files
 java.lang.OutOfMemoryError: Could not allocate JNI Env
   at java.lang.Thread.nativeCreate(Native Method)
   at java.lang.Thread.start(Thread.java:730)
```

第二步：调用mmap时，如果进程虚拟内存地址空间耗尽，也会导致创建JNIEnv失败，抛出错误信息如下：

```
E/art: Failed anonymous mmap(0x0, 8192, 0x3, 0x2, 116, 0): Operation not permitted. See process maps in the log.
java.lang.OutOfMemoryError: Could not allocate JNI Env
  at java.lang.Thread.nativeCreate(Native Method)
  at java.lang.Thread.start(Thread.java:1063)
```

##### 创建线程失败

创建线程也可以归纳为两个步骤：

1. 调用mmap分配栈内存。这里mmap flag中指定了MAP_ANONYMOUS，即匿名内存映射。这是在Linux中分配大块内存的常用方式。其分配的是虚拟内存，对应页的物理内存并不会立即分配，而是在用到的时候触发内核的缺页中断，然后中断处理函数再分配物理内存。
2. 调用clone方法进行线程创建。

第一步：分配栈内存失败是由于进程的虚拟内存不足，抛出错误信息如下：

```c++
W/libc: pthread_create failed: couldn't allocate 1073152-bytes mapped space: Out of memory
W/tch.crowdsourc: Throwing OutOfMemoryError with VmSize  4191668 kB "pthread_create (1040KB stack) failed: Try again"
java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Try again
        at java.lang.Thread.nativeCreate(Native Method)
        at java.lang.Thread.start(Thread.java:753)
```

第二步：clone方法失败是因为线程数超出了限制，抛出错误信息如下：

```c++
W/libc: pthread_create failed: clone failed: Out of memory
W/art: Throwing OutOfMemoryError "pthread_create (1040KB stack) failed: Out of memory"
java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Out of memory
  at java.lang.Thread.nativeCreate(Native Method)
  at java.lang.Thread.start(Thread.java:1078)
```

### OOM问题定位

#### 堆内存不足

Android中最常见的OOM就是Java堆内存不足，对于堆内存不足导致的OOM问题，发生Crash时的堆栈信息往往只是“压死骆驼的最后一根稻草”，它并不能有效帮助我们准确地定位到问题。

堆内存分配失败，通常说明进程中大部分的内存已经被占用了，且不能被垃圾回收器回收，一般来说此时内存占用都存在一些问题，例如内存泄漏等。要想定位到问题所在，就需要知道进程中的内存都被哪些对象占用，以及这些对象的引用链路。而这些信息都可以在Java内存快照文件中得到，调用Debug.dumpHprofData(String fileName)函数就可以得到当前进程的Java内存快照文件（即HPROF文件）。所以，关键在于要获得进程的内存快照。

对于线上场景可以做内存监控，在一个后台线程中每隔nS去获取当前进程的内存占用（通过Runtime.getRuntime.totalMemory()-Runtime.getRuntime.freeMemory()计算得到），当内存占用达到设定的阈值时（阈值根据当前系统分配给应用的最大内存计算），就去执行dump函数，得到内存快照文件。

在得到内存快照文件之后，我们有两种思路，一种想法是直接将HPROF文件回传到服务器，我们拿到文件后就可以使用分析工具进行分析。另一种想法是在用户手机上直接分析HPROF文件，将分析完得到的分析结果回传给服务器。但这两种方案都存在着一些问题，下面分别介绍我们在这两种思路的实践过程中遇到的挑战和对应的解决方案。

#### 线上分析

首先，我们介绍几个基本概念：

- **Dominator**：从GC Roots到达某一个对象时，必须经过的对象，称为该对象的Dominator。例如在上图中，B就是E的Dominator，而B却不是F的Dominator。
- **ShallowSize**：对象自身占用的内存大小，不包括它引用的对象。
- **RetainSize**：对象自身的ShallowSize和对象所支配的（可直接或间接引用到的）对象的ShallowSize总和，就是该对象GC之后能回收的内存总和。例如上图中，D的RetainSize就是D、H、I三者的ShallowSize之和。

JVM在进行GC的时候会进行可达性分析，当一个对象到GC Roots没有任何引用链相连（用图论的话来说，就是从GC Roots到这个对象不可达）时，则证明此对象是可回收的。

Github上有一个开源项目HAHA库，用于自动解析和分析Java内存快照文件（即HPROF文件）。下面是HAHA库的分析步骤：

<img src="images\HAHA库的分析步骤.png" width="500" />

#### 分析进程自身OOM

测试时遇到的最大问题就是分析进程自身经常会发生OOM，导致分析失败。

通过实验可以发现分析进程占用内存与HPROF文件中的Instance数量是正相关的，在将HPROF文件映射到内存中解析时，如果Instance的数量太大，就会导致OOM。

HPROF文件映射到内存中会被解析成Snapshot对象（如下图所示），它构建了一颗对象引用关系树，我们可以在这颗树中查询各个Object的信息，包括Class信息、内存地址、持有的引用以及被持有引用的关系。

<img src="images\Snapshot对象.png" style="zoom:50%;" />

HPROF文件映射到内存的过程：

```
// 1.构建内存映射的 HprofBuffer 针对大文件的一种快速的读取方式，其原理是将文件流的通道与  ByteBuffer 建立起关联，并只在真正发生读取时才从磁盘读取内容出来。
HprofBuffer buffer = new MemoryMappedFileBuffer(heapDumpFile);  
// 2.构造 Hprof 解析器
HprofParser parser = new HprofParser(buffer);
// 3.获取快照
Snapshot snapshot = parser.parse();
// 4.去重 gcRoots
deduplicateGcRoots(snapshot);
```

为了解决分析进程OOM的问题，在HprofParser的解析逻辑中加入了计数压缩逻辑（如下图），目的是在文件映射过程去控制Instance的数量。在解析过程中对于ClassInstance和ArrayInstance，以类型为key进行计数，具体参考文后链接。

#### 链路分析时间过长

使用HAHA算法在PC上可以快速地对所有对象都进行链路分析，但是在手机上由于性能的局限性，如果对所有对象都进行链路分析会导致分析耗时非常长。

考虑到RetainSize越大的对象对内存的影响也越大，即RetainSize比较大的那部分Instance是最有可能造成OOM的“元凶”。

生成Reference之后，做了链路归并，然后排序，选出TopN，细节参考文后链接。

#### 基础类型检测不到

为了解决HAHA算法中检测不到基础类型泄漏的问题，在遍历堆中的Instance时，如果发现是ArrayInstance，且是byte类型时，将它自身舍弃掉，并将它的RetainSize加在它的父Instance上，然后用父Instance进行后面的排序。

<img src="images\HAHA优化思路.png" style="zoom:50%;" />

### 裁剪回捞HPROF文件

一个HPROF文件主要分为这四部分：

- 文件头。
- 字符串信息：保存着所有的字符串，在解析的时候通过索引id被引用。
- 类的结构信息：是所有Class的结构信息，包括内部的变量布局，父类的信息等等。
- 堆信息：即我们关心的内存占用与对象引用的详细信息。

其中我们最关心的堆信息是由若干个相同格式的元素组成，这些元素的大体格式如下图：

![](images\堆信息元素格式.png)

每个元素都有个TAG用来标识自己的身份，而后续字节数则表示元素的内容长度。元素携带的内容则是若干个子元素组合而成，通过子TAG来标识身份。

具体的TAG和身份的对应关系可以在hrpof.cc源码中找到

对IO的关键函数open和write进行Hook。Hook方案使用的是爱奇艺开源的[xHook库](https://github.com/iqiyi/xHook/blob/master/README.zh-CN.md)。

在执行dump的准备阶段，调用Native层的open函数获得一个文件句柄，但实际执行时会进入到Hook层中，然后将返回的FD保存下来，用作write时匹配。

在dump开始时，系统会不断的调用write函数将内容写入到文件中。由于Hook是以so为目标的，系统运行时也会有许多写文件的操作，所以需要对前面保存的FD进行匹配。若FD匹配成功则进行裁剪，否则直接调用origin-write进行写入操作。

流程结束后，就会得到裁剪后的mini-file，裁剪后的文件大小只有原始文件大小的十分之一左右，用于线上可以节省大部分的流量消耗。拿到mini-file后，将裁剪部分的位置填上字节0来进行恢复，这样就可以使用传统工具打开进行分析了。

### 线程数超出限制

/proc/sys/kernel/threads-max规定了每个进程创建线程数目的上限。在华为的部分机型上，这个上限被修改的很低（大约500），比较容易出现线程数溢出的问题，而大部分手机这个限制都很大（一般为1W多）。在这些手机上创建线程失败大多都是因为虚拟内存空间耗尽导致的，进程所使用的虚拟内存可以查看/proc/pid/status的VmPeak/VmSize记录。

然后通过Thread.getAllStackTraces()可以得到进程中的所有线程以及对应的堆栈信息。

一般来说，当进程中线程数异常增多时，都是某一类线程被大量的重复创建。所以我们只需要定位到这类线程的创建时机，就能知道问题所在。如果线程是有自定义名称的，那么直接就可以在代码中搜索到创建线程的位置，从而定位问题.如果线程创建时没有指定名称，那么就需要通过该线程的堆栈信息来辅助定位。

### FD数超出限制

在后台启动一个线程，每隔1s读取一次当前进程创建的FD数量，当检测到FD数量达到阈值时（FD最大限制的95%），读取当前进程的所有FD信息归并后上报。

在/proc/pid/limits描述着Linux系统对对应进程的限制，其中Max open files就代表可创建FD的最大数目。

进程中创建的FD记录在/proc/pid/fd中，通过遍历/proc/pid/fd，可以得到FD的信息。

获取FD信息：

```java
File fdFile=new File("/proc/" + Process.myPid() + "/fd");
File[] files = fdFile.listFiles();  
int length = files.length; //即进程中的fd数量
for (int i = 0; i < length ; i++) {
  if (Build.VERSION.SDK_INT >= 21) {
         Os.readlink(files[i].getAbsolutePath()); //得到软链接实际指向的文件
     } else {
      //6.0以下系统可以通过执行readlink命令去得到软连接实际指向文件，但是耗时较久
  }
}
```

得到进程中所有的FD信息后，我们会先按照FD的类型进行一个归并，FD的用途主要有打开文件、创建socket连接、创建handlerThread等。

![](images\FD数超限分析.png)

### 内存镜像dump

快手开发的框架，利用**Copy-on-write**机制fork子进程dump,可以快速实现dump内存镜像

#### Java-oom 组件介绍

- **内存监控组件** 定时采集内存资源占用情况，超过阈值触发内存镜像采集，决定镜像dump与分析时机，关键代码参考`Monitor.java`
- **内存镜像采集组件** 高性能内存镜像采集组件，包含fork dump和strip dump两个部分，关键代码参考`HeapDumper.java`
- **内存镜像解析组件** 高性能内存镜像解析组件，基于shark解析器定制优化，泄露判定关键代码参考`LeakDetector.java`



参考文章：

1. [Probe：Android线上OOM问题定位组件](https://tech.meituan.com/2019/11/14/crash-oom-probe-practice.html)
2. https://github.com/KwaiAppTeam/KOOM