## 问：编译 ffmpeg 方法

答：在各个平台编译方法基本都是一样的。

- 首先下载 ffmpeg 源码，地址为: https://www.ffmpeg.org/download.html

- 进入到下载后的

  ffmpeg目

  录下，执行下面的指令：

  

  - Mac 平台：

    ```
    ./configure --prefix=/usr/local/ffmpeg
                      --enable-gpl
                      --enable-nonfree
                      --enable-libfdk-aac
                      --enable-libx264
                      --enable-libx265
                      --enable-filter=delogo
                      --enable-debug
                      --disable-optimizations
                      --enable-libspeex
                      --enable-videotoolbox
                      --enable-shared
                      --enable-pthreads
                      --enable-version3
                      --enable-hardcoded-tables
                      --cc=clang
                      --host-cflags=
                      --host-ldflags=
    ```

  - Linux平台

    ```
    ./configure --prefix=/usr/local/ffmpeg
                      --enable-gpl
                      --enable-nonfree
                      --enable-libfdk-aac
                      --enable-libx264
                      --enable-libx265
                      --enable-filter=delogo
                      --enable-debug
                      --disable-optimizations
                      --enable-libspeex
                      --enable-shared
                      --enable-pthreads
    ```

  - Windows平台

    ```
    ./configure --prefix=/usr/local/ffmpeg
                      --enable-gpl
                      --enable-nonfree
                      --enable-libfdk-aac
                      --enable-libx264
                      --enable-libx265
                      --enable-filter=delogo
                      --enable-debug
                      --disable-optimizations
                      --enable-libspeex
                      --enable-static
    ```

## 问：make && make install失败

答：make && make install 之所以会失败，则由于该用户没有操作目录的权限引起的。所以只需要在make install 之前加 sudo即可。

另一种方法是将用户切换成 root用户，因 root用户的权力最大，所以这样做也是没问题的。但建议还是用 非 root用户操作，这样更安全。

## 问：库已经安装好了，但仍报找不到它的错误？

答：首先确认是否已经将 `pkg-config`工具安装好了。可以执行下面的命令：

```
pkg-config
```

如果提示没有安装，则先将该工具安装好，安装命令如下：

- ubuntu

  ```
  apt intall pkg-config
  ```

- mac

  ```
  brew install pkg-config
  ```

- cygwin

  ```
  apt-cyg install pkg-config
  ```

- centos

  ```
  yum install pkg-config
  ```

  安装完该工具后仍然报错？那再按照具体的错误看下面的解决方案吧。

## 问：libx264库找不到（[ERROR: libx264 not found]）

该问题可能由下面三个方面的问题引起。

- libx264库没有安装。对于这种情况有两种解决办法:

  - 其一，是通过平台的安装工作进行安装，如 apt/brew/yum install x264。（注：在Linux下应该安装 libx264-dev）。

  - 其二，是通过源码安装，步骤如下：

    ```
    1. wget https://code.videolan.org/videolan/x264/-/archive/master/x264-master.tar.bz2
    2. bunzip2 x264-master.tar.bz2
    3. tar -vxf x264-master.tar.tar
    4. ./configure --enable-static --enable-shared --disable-asm --disable-avs
    5. make && sudo make install
    ```

- 通过 pkg-config 命令无法找到。对于这类问题，我们可以使用下面的命

   

  ```
  pkg-config --cflags --lib libx264
  ```

  看是否可以找到 libx264，如果找不到，就说明确实是这个问题。解决的办法是设置环境变量。

  ```
  export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/xxx/xxx/lib/pkgconfig
  ```

  其中

   

  ```
  xxx
  ```

   

  由你的实际路径代替。

> 注，有可能你的系统中并没有装 pkg-config工具，可以使用 brew/apt/yum install pkg-config进行安装。

- 无法链接上libx264, 这是由于通过环境变量 LD_LIBRARY_PATH 无法定位到 libx264库。解决的办法也很简单，与上面类似，可以通过设置环境变量来解决该问题。

  ```
  export LD_LIBRARY_PATH=$LIB_LIBRARY_PATH:/xxx/xxx/lib
  ```

  其中，

  ```
  xxx
  ```

  由你的实际路径代替。

当然，你也可以将环境变量设置在脚本里，这样就不用每次都写export命令了。只需要将上面的两条语句加入到 ~~/.bashrc / 或~~/.bash_profil 文件里，然后在控制台执行 `source ~/.bashrc` 或 `source ~/.bash_profile`即可。

## 问：fdk-aac库找不到（[ERROR: libfdk_aac not found]）

答：该问题与上面的 libx264 问题类似，所以只需要将上面的 libx264替换为libfdk-aac即可。这里不在赘述。

## 问：speex库找不到（[ERROR: speex not found]）

答：该问题与上面的 libx264 问题类似，所以只需要将上面的 libx264替换为speex即可。这里不在赘述。

## 问：编译不出ffplay

答：引起该问题有以下几方面的原因：

- 所使用的操作系统没有安装图形库。如有的同学在阿里的云服务器上想安装ffplay是肯定不行的。
- 没有安装 SDL2 库。解决办法是通过 brew/apt/yum install sdl2 即可 或 通过 SDL2 源码安装。**安装完成后，要重新进行到 ffmpeg目录下，执行 ./configure…这指命令。**
- 有的同学编译ffmpeg时，在ffmpeg目录执行./configure … 之后，没有修改config.h文件，这样也编译不出ffplay来。解决办法是进入到 ffmpeg 目录下，打开 config.h文件找 FFPLAY 关键字，将其后面的 0 修改为 1，保存并退出该文件。在当前目录下执行 make && sudo make install。**注意，修改 config.h文件后，不要再执行./configure …了，否则config.h中的值又都恢复为原来的值了。**
- SDL2与最新的Mac系统 mojave不兼容。解决办法：
  - 方法一
    - 从 [SDL](https://www.libsdl.org/tmp/release/SDL2-2.0.9.tar.gz)下载 2.0.9版本或以后的SDL2代码。
    - 执行 ./configure
    - 执行 make && sudo make install
    - 执行 export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/local/lib/pkgconfig
    - [重新]编译ffmpeg
  - 方法二
    - 从[SDL](https://www.libsdl.org/tmp/release/SDL2-2.0.9.dmg)下载dmg包。
    - 执行 open SDL2-2.0.9.dmg
    - [重新]编译ffmpeg

## SDL在windows的 cygwin下无法打印日志

在编译 SDL 时，将configure 中的 `-mwindows` 参数去掉。

## 问：如何在Windows下编译ffmpeg问题

答：参见 [Windows下编译ffmpeg问题](https://www.imooc.com/article/247113)

## 问：在Windows下编译安装ffmpeg是不是特别麻烦？

答：确实是这样。所以建议在Window开发者最好装一个 Ubuntu的虚拟机，这样就方便很多了。如果条件允许的话，最好能用Mac开发。可以说Mac Pro是关专为开发者制造的，一旦你用上它会让你爱不释手。

## 问：编译时，各种库找不到问题

答：该问题与上面的 libx264类似，我们可以通过 `brew/apt/yum install xxx` 命令进行安装，一般情况下这种方式都可以满足我们的需求。如果始终不行的话，就需要源码安装了。源码安装的方法可以参见 [Windows下编译ffmpeg问题](https://www.imooc.com/article/247113) 里的说明。

## 问：ffmpeg如何使用 libx265

答：准确的讲ffmpeg应该是一个音视频框架，所有的音视频编解码器都是以插件的方式与ffmpeg联系起来的。换句话说，ffmpeg在上层提供了统一的 API，无法你使用的编解决是 x264, open264, x265, vp8/vp9/av1 上层用户都不管心，它仍然使用同样的 API， 只是在find_decoder 或 find_encoder时，指定具体的编解码器就可以了。也就是说在find_xxx时，你要设置了 x264 它最终就会调用 x264进行编解码，设置了 x265它就使用 x265进行编解码。如些而已。

## 问：这门课有群吗？

答：有的，群号：883069602，不过[需要购买课程](https://coding.imooc.com/class/279.html?mc_marking=59c321c0417e144904c49c366f94dd57&mc_channel=shouji)后输入验证码才可以进群。大家可以在群里讨论问题，也可以在课程的评论区里搜索答案。目前评论区里已经积累了大量问题的解决方案。

## 问：为什么我使用课程中的程序无法成功抽取AAC音频？

答：[详细回答在这里](https://www.imooc.com/article/254733)

## 问：Android播放器例子为什么调API失败

答：你目前使用的 NDK 版本与我编译时使用的 NDK 版本不一致造成的，换成 NDK10e试试。

## 问：NDK10e 从哪里下载

答：可以到[这里](https://developer.android.com/ndk/downloads/older_releases)下载。

- [mac NDK10e](https://dl.google.com/android/repository/android-ndk-r10e-darwin-x86_64.zip)
- [windows 32 位 NDK10e](https://dl.google.com/android/repository/android-ndk-r10e-windows-x86.zip)
- [windows 64位 NDK10e](https://dl.google.com/android/repository/android-ndk-r10e-windows-x86_64.zip)
- [linux NDK10e](https://dl.google.com/android/repository/android-ndk-r10e-linux-x86_64.zip)

## 问：NDKr21 从哪里下载

答：可以到这里下载。

- [mac NDKr21](https://dl.google.com/android/repository/android-ndk-r21-darwin-x86_64.zip)
- [windows NDKr21](https://dl.google.com/android/repository/android-ndk-r21-windows-x86_64.zip)
- [linux NDKr21](https://dl.google.com/android/repository/android-ndk-r21-linux-x86_64.zip)

## 问：ffmpeg安装好后，编译视频中的ffmpeg_log不成功

答：执行下面的命令进行编译

```
gcc -g -o ffmpeg_log ffmpeg_log.c `pkg-config --libs --cflags libavutil`
```

**注意：pkg-confg 前面的符号不是`'`哟！它是键盘左上角 ESC键下面的键。**





https://avdancedu.com/f3f66133/