
# Kotlin Android 环境搭建

[TOC]

## 开发环境搭建

###  Android Studio 版本 小于 3.0
- Android Studio 3.0 以前需要下载 kotlin 插件继承 步骤略。

### Android Studio 版本 3.0 以上

Android Studio 3.0 版本开始 开始自带 kotlin 开发环境

#### 以Android Studio入门

##### 创建一个工程

首先，为你的应用创建一个新的 Kotlin Android 工程。

1. 打开 Android Studio，在欢迎页面点击 Start a new Android Studio project 或者 File | New | New project。

2. 选择一个定义应用程序行为的 activity 。对于第一个 "Hello world" 应用程序，选择仅显示空白屏幕的 Empty Activity，然后点击 Next。

3. 在下一个对话框中，填写工程的详细信息：

- 名字和包名
- 位置
- 开发语音：选择 Kotlin、java 

![新建项目](A9599B2B181946CA8901C6C1011374C1)

完成这些步骤后，Android Studio 会创建一个项目。 该项目已包含用于构建可在 Android 设备或模拟器上运行的应用程序的所有代码和资源。

##### 构建和运行工程

![运行项目](8843A302EDB14FBCBC2DD9EC22579E69)

至此 我们的 kotlin for android 开发环境搭建完成 

##### 项目版本介绍

![kotlin版本](625D4A173BC84585AF8252F8A3C351AB)

![kotlin 配置](EA110B8686B84ACF986433C086993708)

教程项目环境如下：

- JDK：1.8
- Android Studio : 3.5.3
- gradle : 5.4.1
- gradle for android : com.android.tools.build:gradle:3.5.3
- kotlin : 1.3.50

APP module 配置： 

```
implementation"org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'androidx.core:core-ktx:1.2.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
```

Kotlin有着极小的运行时文件体积：整个库的大小约 1347 KB（1.3.70 版本）。这意味着 Kotlin 对 apk 文件大小影响微乎其微。

就对比 Kotlin 与 Java所编写的程序而言，Kotlin 编译器所生成的字节码看上去几乎毫无差异。