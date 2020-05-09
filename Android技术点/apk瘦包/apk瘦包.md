> 1. 一个完整 APK 包含以下目录（将 APK 文件拖到 AndroidStudio）：
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
>   以这三块是优化 Apk 大小的重点（实际情况不唯
>
>   一）
>
> 1. **减少 res，压缩图文文件**
>
>    图片文件压缩是针对 jpg 和 png 格式的图片。我们通常会放置多套不同分辨率的图片以适配不同的屏幕，这里可以进行适当的删减。在实际使用中，只保留一到两套就足够了（保留一套的话建议保留xxhdpi，两套的话就加上 hdpi），然后再对剩余的图片进行压缩(jpg 采用优图压缩，png 尝试采用pngquant 压缩) 
>
> 2. **减少 dex 文件大小**
>
>    - 添加资源混淆
>    - shrinkResources 为 true 表示移除未引用资源，和代码压缩协同工作。
>    - minifyEnabled 为 true 表示通过 ProGuard 启用代码压缩，配合 proguardFiles 的配置对代码进行混淆并移除未使用的代码。
>
> 3. **减少 lib 文件大小**
>
>    - 由于引用了很多第三方库，lib 文件夹占用的空间通常都很大，特别是有 so 库的情况下。很多so 库会同时引入 armeabi、armeabi-v7a 和 x86 这几种类型，这里可以只保留 armeabi或armeabi-v7a 的其中一个就可以了，实际上微信等主流 app 都是这么做的。
>
>    - 只需在 build.gradle 直接配置即可，NDK 配置同理
>
>      - ```ruby
>        defaultConfig{
>        	ndk{
>        		abiFilters 'armeabi'
>        	}
>        }
>        ```

