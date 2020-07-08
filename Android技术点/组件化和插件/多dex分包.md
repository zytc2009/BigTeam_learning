如果想控制class所在dex，必须要清楚那些类放到主dex。redex中的方案是dump出程序启动时的hprof文件，再从中分析出加载的类，比较麻烦。这里我们采用的方案是hook住ClassLoader.findClass方法，在系统加载类时日志打印出类名，这样分析日志就可以得到启动时加载的类序列了。

 那怎么分包呢？redex的做法应该是解析出所有dex中的类，再按配置的加载类序列，从主dex开始重新生成各个dex，所以会打乱原有的dex分布。而在手q中，分dex规则是编译脚本中维护的，因此我们可以修改分包逻辑，将需要的类放到主dex。

如何调整主dex中类的顺序?Android编译时把.class转换成.dex是依靠dx.bat，这个工具实际执行的是sdk中的dx.jar。我们可以修改dx的源码，替换这个jar包，就可以执行自定义的dx逻辑了。从dex的文件格式我们可以知道，dex被据划分为多个section，一个类的完整信息也被分散到各个section里。想从dex中解析一个类必须要先从classDef段找到类定义，从中找到类包含的各种信息的偏移地址，再从对应地址去读取数据，所以要调整dex的类排列顺序，理论上只需要对classDef段修改即可。（从这里看其实类的排列顺序对读取时的内存影响应该不大，因为在dex中类的数据并不是连续存储的）

在dx执行时，最终将dex数据写入到文件也是以section为单位逐个写入，并且每个section写入前都会执行orderItems做排序，修改这个方法即可实现我们的目的。

### 控制多Dex分包

首先，采用Google的方案我们不需要关心Dex分包，开发工具会自动的分析依赖关系，把需要的class文件及其依赖class文件放在Main Dex中，因此如果产生了多个Dex文件，那么classes.dex内的方法数一般都接近65535这个极限，剩下的class才会被放到Other Dex中。如果我们可以减小Main Dex中的class数量，是可以加快冷启动速度的。



#### 微信加载方案

首次加载在地球中页中, 并用线程去加载（但是 5.0 之前加载 dex 时还是会挂起主线程一段时间（不是全程都挂起））。

- dex 形式

微信是将包放在 assets 目录下的，在加载 Dex 的代码时，实际上传进去的是 zip，在加载前需要验证 MD5，确保所加载的 Dex 没有被篡改。

- dex 类分包规则

分包规则即将所有 Application、ContentProvider 以及所有 export 的 Activity、Service 、Receiver 的间接依赖集都必须放在主 dex。

- 加载 dex 的方式

加载逻辑这边主要判断是否已经 dexopt，若已经 dexopt，即放在 attachBaseContext 加载，反之放于地球中用线程加载。怎么判断？因为在微信中，若判断 revision 改变，即将 dex 以及 dexopt 目录清空。只需简单判断两个目录 dex 名称、数量是否与配置文件的一致。

总的来说，这种方案用户体验较好，缺点在于太过复杂，每次都需重新扫描依赖集，而且使用的是比较大的间接依赖集。

#### Facebook 加载方案

Facebook的思路是将 **MultiDex.install()** 操作放在另外一个进程进行。

- dex 形式

与微信相同。

- dex 类分包规则

Facebook 将加载 dex 的逻辑单独放于一个单独的 nodex 进程中。

```
<activity 
android:exported="false"
android:process=":nodex"android:name="com.facebook.nodex.startup.splashscreen.NodexSplashActivity">
```

所有的依赖集为 Application、NodexSplashActivity 的间接依赖集即可。

- 加载 dex 的方式

因为 NodexSplashActivity 的 intent-filter 指定为 Main 和LAUNCHER ，所以一打开 App 首先拉起 nodex 进程，然后打开 NodexSplashActivity 进行 MultiDex.install() 。如果已经进行了 dexpot 操作的话就直接跳转主界面，没有的话就等待 dexpot 操作完成再跳转主界面。

这种方式好处在于依赖集非常简单，同时首次加载 dex 时也不会卡死。但是它的缺点也很明显，即每次启动主进程时，都需先启动 nodex 进程。尽管 nodex 进程逻辑非常简单，这也需100ms以上。

#### 美团加载方案

dex 形式 在 gradle 生成 dex 文件的这步中，自定义一个 task 来干预 dex 的生产过程，从而产生多个 dex 。

```dart
tasks.whenTaskAdded { task ->
   if (task.name.startsWith('proguard') &amp;&amp; (task.name.endsWith('Debug') || task.name.endsWith('Release'))) {
       task.doLast {
           makeDexFileAfterProguardJar();
       }
       task.doFirst {
           delete "${project.buildDir}/intermediates/classes-proguard";
           String flavor = task.name.substring('proguard'.length(), task.name.lastIndexOf(task.name.endsWith('Debug') ? "Debug" : "Release"));
           generateMainIndexKeepList(flavor.toLowerCase());
       }
   } else if (task.name.startsWith('zipalign') &amp;&amp; (task.name.endsWith('Debug') || task.name.endsWith('Release'))) {
       task.doFirst {
           ensureMultiDexInApk();
       }
   }
} 
```

实现Dex自定义分包的关键是分析出class之间的依赖关系，并且干涉Dex文件的生成过程。

加载 dex 的方式 ，[Multidex加载流程](Multidex流程.md)

**需要注意**，上面给出的gradle task，**只在gradle1.4以下**管用，在1.4+版本的gradle中，app:dexXXX task 被隐藏了(更多信息请参考[Gradle plugin的更新信息](https://link.jianshu.com?t=http://tools.android.com/tech-docs/new-build-system))，jacoco, progard, multi-dex三个task被合并了。

**1.5.0之后怎么办？**继续找方法

官方解释 Gralde`1.5.0`以上已经将(jacoco, progard, multi-dex)统一移到[Transform API](http://tools.android.com/tech-docs/new-build-system/transform-api)里，然而 Transform API 并没有想象的那么简单好用，翻遍 Google 终于找到一个兼容 Gradle `1.5.0`以上的分包插件[DexKnifePlugin](https://github.com/ceabie/DexKnifePlugin)。
扩展=>这篇[Android 热修复使用 Gradle Plugin1.5 改造 Nuwa 插件](http://blog.csdn.net/sbsujjbcy/article/details/50839263)比较好的介绍了 Transform API 的使用。

坑 ：NoClassDefFoundError ，are you kiding me？

**原因：**通过插件手动指定 main dex 中要保留的类，虽然分包成功，但是 main dex 中的类及其直接引用类很难通过手动的方式指定。

**解决方式：**
[美团 Android DEX 自动拆包及动态加载简介](http://tech.meituan.com/mt-android-auto-split-dex.html),他们是通过编写了一个能够自动分析 Class 依赖的脚本去算出主 Dex 需要包含的所有必要依赖。看来写脚本是跑不掉了。

坑 ：自定义脚本 ，read the fuck source！

**问题一：**哪些类是需要放入主 Dex 中？
查看 sdk\build-tools\platform-version\mainDexClasses.rules 发现放入主 Dex 相关类有 Instrumentation，Application，Activity，Service，ContentProvider，BroadcastReceiver，BackupAgent 的所有子类。

**问题二：**gradle 是在哪里算出主 Dex 依赖？






参考文章：

1. [关于MultiDex方案的一点研究与思考](https://www.jianshu.com/p/33f22b21ef1e)
2. [Android 傻瓜式分包插件](http://p.codekk.com/detail/Android/TangXiaoLv/Android-Easy-MultiDex)