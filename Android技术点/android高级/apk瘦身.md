### 瘦身优势：

转换率：下载转化率

头部app都有Lite版

渠道合作商要求



### Apk组成

代码相关：classes.dex

资源文件：res，assets，resourse.arsc

so相关：

>   一个完整 APK 包含以下目录（将 APK 文件拖到 AndroidStudio）：
>
> - **META-INF/**：包含 **CERT.SF** 和 **CERT.RSA** 签名文件以及 **MANIFEST.MF** 清单文件。
>
> - **assets/**：包含应用可以使用 **AssetManager** 对象检索的应用资源。
>
> - **res/**：包含未编译到的资源 resources.arsc。 
>
> - **lib/**：包含特定于处理器软件层的编译代码。该目录包含了每种平台的子目录，像 **armeabi armeabi-v7a， arm64-v8a，x86，x86_64，和mips**。 
>
> - **resources.arsc**：包含已编译的资源。该文件包含**res/values/** 文件夹所有配置中的 XML 内容。打包工具提取此 XML 内容，将其编译为二进制格式，并将内容归档。此内容包括语言字符串和样式，以及直接包含在**resources.arsc** 文件中的内容路径 ，例如布局文件和图像。
>
> - **classes.dex**：包含以 **Dalvik / ART** 虚拟机可理解的**DEX** 文件格式编译的类。
>
> - **AndroidManifest.xml**：包含核心 Android 清单文件。该文件列出应用程序的名称，版本，访问权限和引用的库文件。该文件使用 Android 的二进制XML 格式。
>
> - lib、class.dex 和 res 占用了超过 90%的空间，所
>
>   以这三块是优化 Apk 大小的重点（实际情况不唯一）
>



### APK分析

1.apktool

2.Analyze APK： apk组成，大小，占比；查看dex文件组成；apk对比

3.https://nimbledroid.com/ app性能分析：文件大小，排行；dex方法数，sdk方法数；启动时间，内存等

4.android-classyshark：二进制检查工具

   https://github.com/google/android-classyshark

   支持：apk，jar，class，so等



### 代码混淆

花指令，功能等价但改变形式：改名，更复杂的重写，打乱格式

Proguard：类文件处理工具，优化字节码

shrinkResources 为 true 表示移除未引用资源，和代码压缩协同工作。



### 三方库处理

基础库统一；选择更小的，methods Count；仅引入所需的部分代码

### 移除无用的代码

业务代码只加不减

代码太多不敢删除

AOP统计使用情况



### 冗余资源

右键，Refactor，remove unused Resources

图片压缩：tinyPng及TinyPngPlugin插件，图片格式选择

资源混淆：AndResGuard

图片只留一份

资源在线化



### so优化

so移除，一般选择armeabi

> 由于引用了很多第三方库，lib 文件夹占用的空间通常都很大，特别是有 so 库的情况下。很多so 库会同时引入 armeabi、armeabi-v7a 和 x86 这几种类型，这里可以只保留 armeabi或armeabi-v7a 的其中一个就可以了，实际上微信等主流 app 都是这么做的。
>
> 只需在 build.gradle 直接配置即可，NDK 配置同理
>
> ```
> defaultConfig{
> 	ndk{
> 		abiFilters 'armeabi'
> 	}
> }
> ```

动态选择加载哪个so，只是对部分有明显差别的so

动态下载

插件化：replugin



### 长效治理

发版前包比较，推进插件化结构









