操作系统：Windows 10

IDE： Visual Studio 2019

官网：https://mediasoup.org/documentation/v3/libmediasoupclient/

一、前言
libmediasoupclient是基于libwebrtc的C ++库，用于构建基于mediasoup的C ++客户端应用程序，支持Windows、Linux、Mac等主流操作系统。关于libwebrtc的下载及配置请参考我前一篇文章：Google开源项目WebRTC下载及编译。

 

二、安装CMake
Mediasoup官网对CMake的要求是

cmake > = 3.5
我们直接去官网下载最新版本就好了，下载地址：https://cmake.org/download/，安装时注意加入环境变量。



三、编译libwebrtc
gn gen out/Default --ide=vs2019 --args="is_debug=false use_custom_libcxx=false"

ninja -C out/Default
参数说明：

--ide=vs2019：使用的集成开发环境为VS2019

is_debug=false：编译非调试版本的库文件

use_custom_libcxx=false：不使用libwebrtc自带的编译器，使用系统提供的编译器，也就是VS2019的。

 常用命令：

1、清空ninja项目文件
gn clean out/Default

2、查看所有的参数变量的值
gn args --list out/Default

 

四、下载源码
使用git命令或去Github上手动下载源码

git clone https://github.com/versatica/libmediasoupclient.git
然后使用控制台进入源码目录中，输入以下命令来配置libwebrtc的路径：

cmake . -Bbuild -DLIBWEBRTC_INCLUDE_PATH:PATH=D:\libwebrtc\src -DLIBWEBRTC_BINARY_PATH:PATH=D:\libwebrtc\src\out\Default\obj



五、编译源码
找到源码中libmediasoupclient\build\mediasoupclient.sln文件，双击使用vs2019打开该工程。

从项目的依赖关系，我们可以看出mediasoupclient依赖于sdptransform和ZERO_CHECK，所以首先编译生成ZERO_CHECK，再编译生成sdptransform。

接着我们开始编译mediasoupclient工程，但是很不幸，没刚刚那么顺利了，会报出很多错误。

1、max函数问题

![](image\nominmax.png)

由于webrtc内部使用的min,max函数与windows头文件的定义冲突，会引发编译错误，在项目预处理器中增加宏NOMINMAX可以解决这个问题。

![](image\add_nominmax.png)

2、auto类型不明确问题

```
D:\Github\libmediasoupclient\src\Handler.cpp(103,32): error C2593: “operator =”不明确
```


我们可以直接将const auto& iceServerUri改为const std::string& iceServerUri

3、警告被视为错误问题

![](image\警告被视为错误.png)

在项目属性中，选择 "配置属性" " > c/c + + > 高级" 属性页。编辑 "禁用特定警告" 属性以添加出现过的警告编号，比如4996。 选择 "确定" 以应用所做的更改，即可消除编译警告。

这里我忽略的警告有4996; 4101; 4244; 4834，然后就可以顺利的编译完成了，编译生成的静态库在libmediasoupclient\build\Release目录下。

4、补充

项目属性 > 配置属性 > c/c + + > 代码生成 > 运行库，这项需要改为MT模式，不然后续使用静态库时会报错

```
值“MT_StaticRelease”不匹配值“MD_DynamicRelease”
```











原文链接：https://blog.csdn.net/A18373279153/article/details/111241998