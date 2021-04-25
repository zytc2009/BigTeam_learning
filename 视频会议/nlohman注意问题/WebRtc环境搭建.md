## 环境搭建

webrtc_android编译

1.安装必要的软件sudo apt install git python

2.切换到 homecd /home/webrtc 

3.安装和设置代码下载工具

```
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
export PATH=$PATH:/home/webrtc/depot_tools # 可以写在 .bashrc 里
# echo "export PATH=/opt/depot_tools:$PATH" > $HOME/.bash_profile
source $HOME/.bash_profile
```

4.创建工作目录并进入

mkdir webrtc_android

cd webrtc_android # 注意接下来执行命令始终在这个目录下

5.下载第一步：

   fetch --nohooks webrtc_android

​	//fetch --nohooks webrtc_ios

6.设置 gclient 代理，原因是 gclient 无法使用 $HTTP_PROXY 设置代理，

   而要使用 .boto 文件来设置。这就是上文提到的坑:)不过我没有设置

  export  NO_AUTH_BOTO_CONFIG=/home/webrtc/.boto # 可以写在 .bashrc 里

  echo -e "[Boto]proxy = 10.211.55.2\nproxy_port = 1087" > /home/webrtc/.boto

7.下载第二步：gclient，官网是一个步骤 "gclient sync"，这里可以拆成两个

gclient sync --nohooks # 同步代码用时较短

gclient runhooks # 运行一些 hooks，会下载一些文件，其实这个步骤才需要 .boto 设置代理

```
安装JDK8
$ sudo apt-get install python-software-properties software-properties-common
$ sudo add-apt-repository ppa:openjdk-r/ppa
$ sudo apt-get update
$ sudo apt-get install openjdk-8-jre openjdk-8-jdk
将默认JDK环境切换到JDK8
sudo update-alternatives --config java
sudo update-alternatives --config javac
sudo update-alternatives --config javaws
sudo update-alternatives --config javap
sudo update-alternatives --config jar
sudo update-alternatives --config jarsigner
```

**安装必要的软件和包**

参考官网： >> [Install additional build dependencies](https://chromium.googlesource.com/chromium/src/+/master/docs/android_build_instructions.md#Install-additional-build-dependencies)

进入到src目录中

cd src # 执行完后，当前目录应为 /home/webrtc/webrtc_android/src

下载 java相关命令和包，以及其他一些必要的软件和包

build/install-build-deps.sh 

build/install-build-deps-android.sh

工具

WebRTC项目使用了大量的第三方开源项目，代码庞大复杂，整个构建系统采用了gn [gn官方文档](https://chromium.googlesource.com/chromium/src/+/master/tools/gn/README.md) 和ninja [ninja官方文档](https://ninja-build.org/)。

**GN**

gn命令处理的是名称为 BUILD.gn 的文件，BUILD.gn 文件中可以定义若干参数，这些参数使 gn 命令执行时可以通过不同的参数值创建不同的编译配置。例如WebRTC的目标系统是在android上还是ios上。

gn使用文件目录层次来组织不同的编译目标，这是非常自然合理的。查看 WebRTC 的目录结构可以发现每个文件夹下面都对应着一个 BUILD.gn 的文件，含有 BUILD.gn 文件表示这个目录下是有编译目标的，这些编译目标可以依赖子目录的编译目标从而组成一套复杂而有序的构建图。

**NINJA**

gn的输出就是扩展名为 .ninja 的文件，这些文件保存在编译目录中，可以被 ninja 命令直接使用，ninja文件中的指令都是简单和明确的，不需要任何额外的逻辑判断和计算，这使得ninja具有小而快的特点，也是ninja本身的设计初衷。

编译所有目标

```shell
cd webrtc_android/src
//android编译
gn gen out/android --args='target_os="android" target_cpu="arm"'
//ios版本 
gn gen out/ios --args='target_os="ios" target_cpu="arm64" is_debug=true'
//mac平台编译，待验证
gn gen out/mac --args='target_os="mac" target_cpu="x64" use_rtti=true is_debug=true is_component_build=false rtc_use_h264=true' --ide=xcode --workplace="audio_fec"

ninja -C out/Debug
```

**编译 example app** 

cd webrtc_android/src

编译可指定带或不带AppRTCMobile，带AppRTCMobile生成的Debug目录较小，不带AppRTCMobile生成的文件较多，也更大。

```
source build/android/envsetup.sh
gn gen out/Debug --args=‘target_os="android" target_cpu="arm64"‘
ninja -C out/Debug AppRTCMobile
```

最后生成的apk位于out/Debug/apks/AppRTCMobile.apk

参考官方文档：https://webrtc.googlesource.com/src/+/master/examples/androidapp/README

生成给Android Studio使用的gradle

build/android/gradle/generate_gradle.py --output-directory $PWD/out/Debug \--target "//examples:AppRTCMobile" --use-gradle-process-resources \--split-projects --canary

结果在out/Debug/gradle下面

**打包 aar 文件**

WebRTC提供了一个脚本工具可以直接构建一个安卓 aar 的文件，我们直接用这个就可以了。

cd webrtc_android/src

tools_webrtc/android/build_aar.py --build-dir out --arch "armeabi-v7a" "arm64-v8a"
--build-dir out 指定输出目录，如果不指定会在系统临时目录下创建

--arch "armeabi-v7a" "arm64-v8a" 指定两种cpu架构，相当于gn命令中的target_cpu="arm"和target_cpu="arm64"。对每一种cpu架构会创建一个编译目录，依次执行gn和ninja命令。

最后将生成的jar和so打包成了aar文件，aar文件位于webrtc_android/src/libwebrtc.aar

编好了 aar 文件后，可以拷贝到 mac 下使用了，在 Android Studio 中可以替换预编译的 dependency

implementation 'org.webrtc:google-webrtc:1.0.+'

如何替换可以参考安卓官方文档：https://developer.android.com/studio/projects/android-library#AddDependency

**推荐方法**

直接将 src/examples/androidapp/ 目录下的代码导入到 Android Studio 中，导入后生成的gradle文件也不太完整，可以参考 src/examples/aarproject 中的 gradle 文件来补全。

注意 src/examples/androidapp/third_part/autobanh/lib/autobanh.jar 文件需要拷贝到 libs 目录下。

以下是 build.gradle 文件内容，供参考

```groovy
apply plugin: 'com.android.application'
android {
    compileSdkVersion 27
    buildToolsVersion "27.0.3"

    defaultConfig {
        applicationId "org.appspot.apprtc"
        minSdkVersion 16
        targetSdkVersion 21
        versionCode 1
        versionName "1.0"
        testApplicationId "org.appspot.apprtc.test"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
	
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'}
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:27.1.1'
    implementation 'com.google.code.findbugs:jsr305:3.0.2'
    implementation 'org.webrtc:google-webrtc:1.0.+'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
}
```

### Turn Server

//参考https://blog.csdn.net/u011077027/article/details/86225524

```
sudo apt install coturn 
which turnserver
```

/usr/local/etc/turnserver.conf

配置 如下

```shell
verbose
fingerprint
lt-cred-mech
realm=test 
user=whb:123456
stale-nonce
no-loopback-peers
no-multicast-peers
mobility
no-cli
12345678910
```

或者下面这个配置，只配置stun（stun-only）

```shell
listening-ip=本地ip
listening-port=3478
#relay-ip=0.0.0.0
external-ip=111.196.163.80
min-port=59000
max-port=65000
Verbose
fingerprint
no-stdout-log
syslog
user=whb:123456
no-tcp
no-tls
no-tcp-relay
stun-only
# 下面是配置证书，不懂就问后端人员怎么用openssl生成这个
cert=pem/turn_server_cert.pem 
pkey=pem/turn_server_pkey.pem 
#secure-stun
```

启动:

```shell
# 如果按照上面的配置直接运行
turnserver

# 如果没有配置上述配置文件，可采用其他运行方法
/usr/local/bin/turnserver --syslog -a -f --min-port=32355 --max-port=65535 --user=dds:123456 -r dds --cert=turn_server_cert.pem --pkey=turn_server_pkey.pem --log-file=stdout -v

--syslog 使用系统日志
-a 长期验证机制
-f 使用指纹
--min-port   起始用的最小端口
--max-port   最大端口号
--user=dds:123456  turn用户名和密码
-r realm组别
--cert PEM格式的证书
--pkey PEM格式的私钥文件
-l, --log-file,<filename> 指定日志文件
-v verbose
```

测试地址，请分别测试stun和turn，relay代表turn

https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/

### 启动Server

```shell
sudo apt-get install npm

#我用apt-get安装完，系统发现不了 
sudo npm install socket.io 
sudo npm install node-static
sudo npm install nodejs
#如果有警告，需要处理：我的报错跟这2个有关
sudo npm install bufferutil
sudo npm install utf-8-validate

#下载https://github.com/cmyeyi/webrtc_for_android.git
#multipeers_server目录
node index.js
#就可以安装android客户端，用两个手机测试了
```

**windows**：我在windows安装了NodeJs，用他启动服务，启动前先执行npm install 安装依赖的模块, 然后执行node xx.js. 如果执行失败，npm audit fix修复一下不一致版本的模块

1. 下载 代码

```shell
# 代码检出来
git clone https://github.com/ddssingsong/webrtc_server_node.git  
cd webrtc_server
123
```

2. 修改`/public/dist/js/SkyRTC-client.js`，设置穿透服务器，可查看https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/

```js
   var iceServer = {
        "iceServers": [
          {
            "url": "stun:stun.l.google.com:19302"
          },
          {
            "url": "stun:111.196.163.80:3478"
          },
          {
             "url": "turn:111.196.163.80:3478",
             "username":"ddssingsong",
             "credential":"123456"
          }
        ]
    };

12345678910111213141516
```

3. 修改`/public/dist/js/conn.js`

```js
## 最后一行
##  如果没有配wss代理
rtc.connect("ws:" + window.location.href.substring(window.location.protocol.length).split('#')[0], window.location.hash.slice(1));

如果配了nginx wss代理
rtc.connect("wss:" + window.location.href.substring(window.location.protocol.length).split('#')[0]+"/wss", window.location.hash.slice(1));

# 后面的那个“/wss”是根据自己配的代理路径
```



相关文章

1. https://www.jianshu.com/p/e0239bb43f48

2. https://github.com/cmyeyi/webrtc_for_android.git （含client和server）

3. [快速搭建 视频通话 视频会议](https://blog.csdn.net/u011077027/article/details/86225524)（含client和server）