1.Java内存分配策略

Java 程序运行时的内存分配策略有三种：静态分配、栈式分配和堆式分配。对应的存储区域如下：

静态存储区（方法区）：主要存放静态数据、全局 static 数据和常量。这块内存在程序编译时就已经分配好，并且在程序整个运行期间都存在。

栈区 ：方法体内的局部变量都在栈上创建，并在方法执行结束时这些局部变量所持有的内存将会自动被释放。

堆区 ： 又称动态内存分配，通常就是指在程序运行时直接 new 出来的内存。这部分内存在不使用时将会由 Java 垃圾回收器来负责回收。

2.堆与栈的区别

栈内存：在方法体内定义的局部变量（一些基本类型的变量和对象的引用变量）都是在方法的栈内存中分配的。当在一段方法块中定义一个变量时，Java 就会在栈中为该变量分配内存空间，当超过该变量的作用域后，分配给它的内存空间也将被释放掉，该内存空间可以被重新使用。

堆内存：用来存放所有由 new 创建的对象（包括该对象其中的所有成员变量）和数组。在堆中分配的内存，将由 Java 垃圾回收器来自动管理。在堆中产生了一个数组或者对象后，还可以在栈中定义一个特殊的变量，这个变量的取值等于数组或者对象在堆内存中的首地址，这个特殊的变量就是我们上面说的引用变量。我们可以通过这个引用变量来访问堆中的对象或者数组。

例子：

public class A {    int a = 0;    B b = new B();    public voidtest(){        int a1 = 1;        B b1 = new B();    }}A object = new A();

A类内的局部变量都存在于栈中，包括基本数据类型a1和引用变量b1，b1指向的B对象实体存在于堆中

引用变量object存在于栈中，而object指向的对象实体存在于堆中，包括这个对象的所有成员变量a和b，而引用变量b指向的B类对象实体存在于堆中

3.Java管理内存的机制

Java的内存管理就是对象的分配和释放问题。内存的分配是由程序员来完成，内存的释放由GC（垃圾回收机制）完成。GC 为了能够正确释放对象，必须监控每一个对象的运行状态，包括对象的申请、引用、被引用、赋值等。这是Java程序运行较慢的原因之一。

释放对象的原则：

该对象不再被引用。

GC的工作原理：

将对象考虑为有向图的顶点，将引用关系考虑为有向图的有向边，有向边从引用者指向被引对象。另外，每个线程对象可以作为一个图的起始顶点，例如大多程序从 main 进程开始执行，那么该图就是以 main 进程为顶点开始的一棵根树。在有向图中，根顶点可达的对象都是有效对象，GC将不回收这些对象。如果某个对象与这个根顶点不可达，那么我们认为这个对象不再被引用，可以被 GC 回收。

下面举一个例子说明如何用有向图表示内存管理。对于程序的每一个时刻，我们都有一个有向图表示JVM的内存分配情况。以下右图，就是左边程序运行到第6行的示意图。

![img](http://upload-images.jianshu.io/upload_images/6627136-c17cb6ba34e0fd70.gif!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/582/format/webp)

另外，Java使用有向图的方式进行内存管理，可以消除引用循环的问题，例如有三个对象相互引用，但只要它们和根进程不可达，那么GC也是可以回收它们的。当然，除了有向图的方式，还有一些别的内存管理技术，不同的内存管理技术各有优缺点，在这里就不详细展开了。

4.Java中的内存泄漏

如果一个对象满足以下两个条件：

（1）这些对象是可达的，即在有向图中，存在通路可以与其相连

（2）这些对象是无用的，即程序以后不会再使用这些对象

就可以判定为Java中的内存泄漏，这些对象不会被GC所回收，继续占用着内存。

在C++中，内存泄漏的范围更大一些。有些对象被分配了内存空间，然后却不可达，由于C++中没有GC，这些内存将永远收不回来。在Java中，这些不可达的对象都由GC负责回收，因此程序员不需要考虑这部分的内存泄漏。

![img](http://upload-images.jianshu.io/upload_images/6627136-b553556dcd30786b.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/507/format/webp)

5.Android中常见的内存泄漏

（1）单例造成的内存泄漏

![img](http://upload-images.jianshu.io/upload_images/6627136-d5edd1d4e04be421.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/829/format/webp)

![img](http://upload-images.jianshu.io/upload_images/6627136-df1b556fb961da3a.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/645/format/webp)

这是一个普通的单例模式，当创建这个单例的时候，由于需要传入一个Context，所以这个Context的生命周期的长短至关重要：

1.如果此时传入的是 Application 的 Context，因为 Application 的生命周期就是整个应用的生命周期，所以没有任何问题。

2.如果此时传入的是 Activity 的 Context，当这个 Context 所对应的 Activity 退出时，由于该 Context 的引用被单例对象所持有，其生命周期等于整个应用程序的生命周期，所以当前 Activity 退出时它的内存并不会被回收，这就造成泄漏了。

当然，Application 的 context 不是万能的，所以也不能随便乱用，例如Dialog必须使用 Activity 的 Context。对于这部分有兴趣的读者可以自行搜索相关资料。

（2）非静态内部类创建静态实例造成的内存泄漏

public class MainActivity extends AppCompatActivity {  private static TestResource mResource = null;    @Override  protected void onCreate(Bundle savedInstanceState) {      super.onCreate(savedInstanceState);      setContentView(R.layout.activity_main);if(mManager == null){          mManager = new TestResource();      }//...  }  class TestResource {//...  }}

非静态内部类默认会持有外部类的引用，而该非静态内部类又创建了一个静态的实例，该实例的生命周期和应用的一样长，这就导致了该静态实例一直会持有该Activity的引用，导致Activity的内存资源不能正常回收。

（3）匿名内部类造成的内存泄漏

匿名内部类默认也会持有外部类的引用。

如果在Activity/Fragment中使用了匿名类，并被异步线程持有,如果没有任何措施这样一定会导致泄漏。

![img](http://upload-images.jianshu.io/upload_images/6627136-0b00574407f9e8b6.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/763/format/webp)

ref1和ref2的区别是，ref2使用了匿名内部类。我们来看看运行时这两个引用的内存：

![img](http://upload-images.jianshu.io/upload_images/6627136-a58b999cc2c36a73.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

可以看到，ref1没什么特别的。但ref2这个匿名类的实现对象里面多了一个引用：

this$0这个引用指向MainActivity.this，也就是说当前的MainActivity实例会被ref2持有，如果将这个引用再传入一个异步线程，此线程和此Acitivity生命周期不一致的时候，就会造成Activity的泄漏。

例子：Handler造成的内存泄漏

![img](http://upload-images.jianshu.io/upload_images/6627136-40fc6bef8164f1f2.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/793/format/webp)

在该MainActivity 中声明了一个延迟10分钟执行的消息 Message，mHandler 将其 push 进了消息队列 MessageQueue 里。当该 Activity 被 finish() 掉时，延迟执行任务的 Message 还会继续存在于主线程中，它持有该 Activity 的 Handler 引用，然后又因 为 Handler 为匿名内部类，它会持有外部类的引用（在这里就是指MainActivity），所以此时 finish() 掉的 Activity 就不会被回收了，从而造成内存泄漏。

修复方法：在 Activity 中避免使用非静态内部类或匿名内部类，比如将 Handler 声明为静态的，则其存活期跟 Activity 的生命周期就无关了。如果需要用到Activity，就通过弱引用的方式引入 Activity，避免直接将 Activity 作为 context 传进去。另外， Looper 线程的消息队列中还是可能会有待处理的消息，所以我们在 Activity 的 Destroy 时或者 Stop 时应该移除消息队列 MessageQueue 中的消息。见下面代码：

![img](http://upload-images.jianshu.io/upload_images/6627136-7b5902ccb1ab137b.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/781/format/webp)

（4）资源未关闭造成的内存泄漏

对于使用了BraodcastReceiver，ContentObserver，File， Cursor，Stream，Bitmap等资源的使用，应该在Activity销毁时及时关闭或者注销，否则这些资源将不会被回收，造成内存泄漏。

（5）一些不良代码造成的内存压力

有些代码并不造成内存泄漏，但是它们，或是对没使用的内存没进行有效及时的释放，或是没有有效的利用已有的对象而是频繁的申请新内存。比如，Adapter里没有复用convertView等。

6.Android中内存泄漏的排查与分析

（1）利用Android Studio的Memory Monitor来检测内存情况

先来看一下Android Studio 的 Memory Monitor界面：

![img](http://upload-images.jianshu.io/upload_images/6627136-7e63b74ec33b1e69.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

最原始的内存泄漏排查方式如下：

重复多次操作关键的可疑的路径，从内存监控工具中观察内存曲线，看是否存在不断上升的趋势，且退出一个界面后，程序内存迟迟不降低的话,可能就发生了严重的内存泄漏。

这种方式可以发现最基本，也是最明显的内存泄漏问题，对用户价值最大，操作难度小，性价比极高。

下面就开始用一个简单的例子来说明一下如何排查内存泄漏。

首先，创建了一个TestActivity类，里面的测试代码如下：

@Override  protected voidprocessBiz() {      mHandler = new Handler();      mHandler.postDelayed(newRunnable() {          @Override          public voidrun() {              MLog.d("------postDelayed------");          }      }, 800000L);  }

运行项目，并执行以下操作：进入TestActivity，然后退出，再重新进入，如此操作几次后，最后最终退出TestActivity。这时发现，内存持续增高，如图所示：

![img](http://upload-images.jianshu.io/upload_images/6627136-8b36eba7c6d74653.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

好了，这时我们可以假设，这里可能出现了内存泄漏的情况。那么，如何继续定位到内存泄漏的地址呢？这时候就得点击“Dump java heap”按钮来收集具体的信息了。

（2）使用Android Studio生成Java Heap文件来分析内存情况

注意，在点击 Dump java heap 按钮之前，一定要先点击Initate GC按钮强制GC，建议点击后等待几秒后再次点击，尝试多次，让GC更加充分。然后再点击Dump Java Heap按钮。

这时候会生成一个Java heap文件并在新的窗口打开：

![img](http://upload-images.jianshu.io/upload_images/6627136-1772859b7a9640f7.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

这时候，点击右上角的“Analyzer Task”，再点击出现的绿色按钮，让Android studio帮我们自动分析出有可能潜在的内存泄漏的地方：

![img](http://upload-images.jianshu.io/upload_images/6627136-e1856642f5caf5e9.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

如上图所示，Android studio提示有3个TestActivity对象可能出现了内存泄漏。而且左边的Reference Tree（引用树），也大概列出了该实体类被引用的路径。如果是一些比较简单的内存泄漏情况，仅仅看这里就大概能猜到是哪里导致了内存泄漏。

但如果是比较复杂的情况，还是推荐使用MAT工具（Memory Analyzer）来继续分析比较好。

（3）使用Memory Analyzer（MAT）来分析内存泄漏

MAT是Eclipse出品的一个插件，当然也有独立的版本。下载链接：[MAT下载地址](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fwww.eclipse.org%2Fmat%2Fdownloads.php)

在这里先提醒一下：MAT并不会准确地告诉我们哪里发生了内存泄漏，而是会提供一大堆的数据和线索，我们需要根据自己的实际代码和业务逻辑去分析这些数据，判断到底是不是真的发生了内存泄漏。

MAT支持对标准格式的hprof文件进行内存分析，所以，我们要先在Android Studio里先把Java heap文件转成标准格式的hprof文件，具体步骤如下：

点击左侧的capture，选择对应的文件，并右键选择“Export to standard .hprof”导出标准的hprof文件：

![img](http://upload-images.jianshu.io/upload_images/6627136-5708f72cbe483c00.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

导出标准的hprof文件后，在MAT工具里导入，则看到以下界面：

![img](http://upload-images.jianshu.io/upload_images/6627136-41c2eb516ed4323d.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

MAT中提供了非常多的功能，这里我们只要学习几个最常用的就可以了。上图那个饼状图展示了最大的几个对象所占内存的比例，这张图中提供的内容并不多，我们可以忽略它。在这个饼状图的下方就有几个非常有用的工具：

**Histogram：**直方图，可以列出内存中每个对象的名字、数量以及大小。

**Dominator Tree：**会将所有内存中的对象按大小进行排序，并且我们可以分析对象之间的引用结构。

1）**Dominator Tree**

![img](http://upload-images.jianshu.io/upload_images/6627136-f233215e56686c11.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/872/format/webp)

从上图可以看到右边存在着3个参数。Retained Heap表示这个对象以及它所持有的其它引用（包括直接和间接）所占的总内存，因此从上图中看，前两行的Retained Heap是最大的，分析内存泄漏时，内存最大的对象也是最应该去怀疑的。

另外大家应该可以注意到，在每一行的最左边都有一个文件型的图标，这些图标有的左下角带有一个红色的点，有的则没有。带有红点的对象就表示是可以被GC Roots访问到的，

可以被GC Root访问到的对象都是无法被回收的。那么这就可以说明所有带红色的对象都是泄漏的对象吗？当然不是，因为有些对象系统需要一直使用，本来就不应该被回收。

如果发现有的对象右边有写着System Class，那么说明这是一个由系统管理的对象，并不是由我们自己创建并导致内存泄漏的对象。

根据我们在Android studio的Java heap文件的提示，TestActivity对象有可能发生了内存泄漏，于是我们直接在上面搜TestActivity（这个搜索功能也是很强大的）：

左边的inspector可以查看对象内部的各种信息：

![img](http://upload-images.jianshu.io/upload_images/6627136-90718b125cba4288.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

当然，如果你觉得按照默认的排序方式来查看不方便，你可以自行设置排序的方式：

Group by class

Group by class loader

Group by package

![img](http://upload-images.jianshu.io/upload_images/6627136-e07841d138765e31.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/882/format/webp)

从上图可以看出，我们搜出了3个TestActivity的对象，一般在退出某个activity后，就结束了一个activity的生命周期，应该会被GC正常回收才对的。通常情况下，一个activity应该只有1个实例对象，但是现在居然有3个TestActivity对象存在，说明之前的操作，产生了3个TestActivity对象，并且无法被系统回收掉。

接下来继续查看引用路径。

对着TestActivity对象点击右键 -> Merge Shortest Paths to GC Roots（当然，这里也可以选择Path To GC Roots） -> exclude all phantom/weak/soft etc. references

为什么选择exclude all phantom/weak/soft etc. references呢？因为弱引用等是不会阻止对象被垃圾回收器回收的，所以我们这里直接把它排除掉

![img](http://upload-images.jianshu.io/upload_images/6627136-36b85d3550d042ab.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

接下来就能看到引用路径关系图了：

![img](http://upload-images.jianshu.io/upload_images/6627136-c152a9b24ffbf556.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/906/format/webp)

从上图可以看出，TestActivity是被this$0所引用的，它实际上是匿名类对当前类的引用。this$0又被callback所引用，接着它又被Message中一串的next所引用...到这里，我们就已经分析出内存泄漏的原因了，接下来就是去改善存在问题的代码了。

2）Histogram

![img](http://upload-images.jianshu.io/upload_images/6627136-08dc10b8725fc35d.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/702/format/webp)

这里是把当前应用程序中所有的对象的名字、数量和大小全部都列出来了，那么Shallow Heap又是什么意思呢？就是当前对象自己所占内存的大小，不包含引用关系的。

上图当中，byte[]对象的Shallow Heap最高，说明我们应用程序中用了很多byte[]类型的数据，比如说图片。可以通过右键 -> List objects -> with incoming references来查看具体是谁在使用这些byte[]。

当然，除了一般的对象，我们还可以专门查看线程对象的信息：

![img](http://upload-images.jianshu.io/upload_images/6627136-8dc7b1d5edc38911.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

Histogram中是可以显示对象的数量的，比如说我们现在怀疑TestActivity中有可能存在内存泄漏，就可以在第一行的正则表达式框中搜索“TestActivity”，如下所示：

![img](http://upload-images.jianshu.io/upload_images/6627136-8162ff2fc1012fec.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/610/format/webp)

接下来对着TestActivity右键 -> List objects -> with outgoing references查看具体TestActivity实例

注：

List objects -> with outgoing

references ：表示该对象的出节点（被该对象引用的对象）

List objects -> with incoming references：表示该对象的入节点（引用到该对象的对象）

如果想要查看内存泄漏的具体原因，可以对着任意一个TestActivity的实例右键 -> Merge Shortest Paths to GC Roots（当然，这里也可以选择Path To GC Roots） ->

exclude all phantom/weak/soft etc. references,如下图所示：

![img](http://upload-images.jianshu.io/upload_images/6627136-4b9121d12425ccf4.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

![img](http://upload-images.jianshu.io/upload_images/6627136-ce240d021c5f76bb.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/918/format/webp)

从这里可以看出，Histogram和Dominator Tree两种方式下操作都是差不多的，只是两种统计图展示的侧重点不太一样，实际操作中，根据需求选择不同的方式即可。

3）两个hprof文件的对比

为了排查内存泄漏，经常会需要做一些前后的对比。下面简单说一下两种对比方式：

1.直接对比

工具栏最右边有个“Compare to another heap dump”的按钮，只要点击，就可以生成对比后的结果。（注意，要先在MAT中打开要对比的hprof文件，才能选择对比的文件）：

![img](http://upload-images.jianshu.io/upload_images/6627136-092ec6c32e3716f3.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/661/format/webp)

2.添加到campare basket里对比

在window菜单下面选择compare basket：

![img](http://upload-images.jianshu.io/upload_images/6627136-ddd8e2a169f5afe0.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/784/format/webp)

在文件的Histogram view模式下，在navigation history下选择add to compare basket：

![img](http://upload-images.jianshu.io/upload_images/6627136-0c062f1f6b89ccff.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/392/format/webp)

然后就可以通过 Compare Tables 来进行对比了：

![img](http://upload-images.jianshu.io/upload_images/6627136-fa00e4f79ad88a6b.png!thumbnail?imageMogr2/auto-orient/strip%7CimageView2/2/w/721/format/webp)

7.总结

最后，还是要再次提醒一下，工具是死的，人是活的，MAT也没有办法保证一定可以将内存泄漏的原因找出来，还是需要我们对程序的代码有足够多的了解，知道有哪些对象是存活的，以及它们存活的原因，然后再结合MAT给出的数据来进行具体的分析，这样才有可能把一些隐藏得很深的问题原因给找出来。

文章同步发布在[zhuanlan.zhihu.com/p/27593816](https://link.jianshu.com/?t=https://link.juejin.im/?target=https%3A%2F%2Fzhuanlan.zhihu.com%2Fp%2F27593816)

参考资料：

（1）[Java的内存泄漏](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fwww.ibm.com%2Fdeveloperworks%2Fcn%2Fjava%2Fl-JavaMemoryLeak%2F%3Fspm%3D5176.100239.blogcont3009.29.P23cEc)

（2）[Android内存优化之一：MAT使用入门](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fju.outofmemory.cn%2Fentry%2F172684)

（3）[Android内存优化之二：MAT使用进阶](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fju.outofmemory.cn%2Fentry%2F172685)

（4）[内存泄漏从入门到精通三部曲之基础知识篇](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fbugly.qq.com%2Fbbs%2Fforum.php%3Fmod%3Dviewthread%26amp%3Btid%3D21%26amp%3Bhighlight%3D%25E5%2586%2585%25E5%25AD%2598%25E6%25B3%2584%25E9%259C%25B2)

（5）[内存泄漏从入门到精通三部曲之常见原因与用户实践](https://link.jianshu.com/?t=https://link.juejin.im/?target=http%3A%2F%2Fbugly.qq.com%2Fbbs%2Fforum.php%3Fmod%3Dviewthread%26amp%3Btid%3D125%26amp%3Bhighlight%3D%25E5%2586%2585%25E5%25AD%2598)

链接：<https://www.jianshu.com/p/f553fbd07d74>