# 1. **ffmpeg编译64位**

1. 下载安装MSYS2(按照官⽹安装到⾃⼰指定的⽬录下，本⼈安装于D:/msys64)
2. 安装完成之后,先把安装⽬录下的msys2_shell.cmd中注释掉的 rem set

MSYS2_PATH_TYPE=inherit 改成 set MSYS2_PATH_TYPE=inherit ，这是为了将vs的环境继承给MSYS2。

下载地址：https://www.msys2.org/

## **1.编译环境修改pacman的源**

pacman是⼀个软件包管理器，需要修改为国内源，具体如下：

注意：是在⽂件夹打开⽂件进⾏编辑，不是在shell窗⼝编辑。

编辑 /etc/pacman.d/mirrorlist.mingw32 ，在⽂件开头添加：

Server = https://mirrors.tuna.tsinghua.edu.cn/msys2/mingw/i686/

Server = http://mirrors.ustc.edu.cn/msys2/mingw/i686/

编辑 /etc/pacman.d/mirrorlist.mingw64 ，在⽂件开头添加：

Server = https://mirrors.tuna.tsinghua.edu.cn/msys2/mingw/x86_64/

Server = http://mirrors.ustc.edu.cn/msys2/mingw/x86_64/

编辑 /etc/pacman.d/mirrorlist.msys ，在⽂件开头添加：

Server = https://mirrors.tuna.tsinghua.edu.cn/msys2/msys/$arch/

Server = http://mirrors.ustc.edu.cn/msys2/msys/$arch/

## 2. **安装编译环境**

### **1.启动窗口mingw64窗⼝**

启动命令⾏窗⼝，在窗⼝中输⼊,Click on **Start Menu** → **Visual Studio 2019** → **x64 Native Tools Command Prompt for VS 2019**

\#进⼊msys2安装⽬录

D:

cd D:\msys64

\#打开msys2的mingw64窗⼝

msys2_shell.cmd -mingw64

然后执⾏：

pacman -Sy

刷新软件包数据。

### **2.MinGW-w64安装编译链**

MSYS2 MinGW64 ，在shell窗⼝中输⼊：

pacman -S mingw-w64-x86_64-toolchain

然后默认全部安装即可（直接回⻋）。

### **3.安装git**

安装git：任⼀⽅式打开shell窗⼝输⼊：

pacman -S git

### **4.安装make等⼯具**

pacman -S make

pacman -S automake

pacman -S autoconf

pacman -S perl

pacman -S libtool

pacman -S mingw-w64-i686-cmake

pacman -S pkg-config

如果需要编译出ffplayer的话，还需要安装SDL

pacman -S mingw-w64-x86_64-SDL2

## **3.编译环境的其他准备⼯作**

1.重命名link.exe

重命名 msys64/usr/bin/link.exe 为 msys64/usr/bin/link.bak , 避免和MSVC 的link.exe抵触。

2.添加vs的64位环境变量， 如：C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.16.27023\bin\HostX64

1. 下载和安装yasm和nasm

在mingw64的窗口中执行，安装yasm和nasm

pacman -S yasm

pacman -S nasm

4.检查编译环境⼯具

mingw64的shell窗⼝输⼊：

which cl link yasm cpp

看返回结果，没有no的结果⼀般就没问题。

5.修改⽀持中⽂显示

窗⼝右键->Options->Text，然后locale选择：zh_CN，Character set 选择 UTF-8。

## **4.编译第三⽅库**

### **4.1 下载和编译x264**

#### **1.下载x264**

将下载下的放置于/home/yingchun.sun/ffmpeg，把第三⽅库编译的库⽂件放在 /home/yingchun.sun/ffmpeg/build

git clone http://git.videolan.org/git/x264.git

或者⽤码云的链接

git clone https://gitee.com/mirrors_addons/x264.git

#### **2.编译x264方案1**

cd进⼊x264⽬录下：

./configure --prefix=/home/yingchun.sun/ffmpeg/build/libx264 --host=x86_64-w64-mingw32 --enable-shared --enable-static --extra-ldflags=-Wl,--output-def=libx264.def

make

make install

⽣成libx264.lib

上⾯编译出来的结果没有包含lib⽂件，需要⾃⼰⼿⼯⽣成。

configure时我们⽣成了 libx264.def 此时就派上⽤场。

cp ./libx264.def /home/yingchun.sun/ffmpeg/build/libx264/lib/

cd /home/yingchun.sun/ffmpeg/build/libx264/lib

\#若要⽣成64位lib⽂件则输⼊如下命令：

lib /machine:X64 /def:libx264.def

即得到 libx264 .lib ，然后将 /home/yingchun.sun/ffmpeg/build/libx264/bin/libx264-

164.dll (具体名字和x264版本有关) 改名或者复制⼀份为 libx264.dll 。

如果想在程序中直接使⽤x264的话，将include中的.h头⽂件、 libx264.lib 和 libx264.dll 复制到项⽬中对应位置，并且在程序中添加头⽂件，然后就可以使⽤x264中的⽅法了。

加上

libx264官⽹下载

https://www.videolan.org/developers/x264.html

#### **3.编译x264方案2**

cd进⼊x264⽬录下：

将config中的prefix去掉，利用linux系统的机制，生成的文件在usr/local/lib目录下



./configure --host=x86_64-w64-mingw32 --enable-shared --enable-static --extra-ldflags=-Wl,--output-def=libx264.def

make

make install

生成lib⽂件的过程，参见以上编译方案1

### **4.2 下载和编译fdk-aac**

#### **下载fdk-aac**

git clone --depth 1 https://gitee.com/mirrors/fdk-aac.git

cd fdk-aac

#### **编译fdk-aac**

./autogen.sh

./configure --prefix=/home/yingchun.sun/ffmpeg/build/libfdk-aac --enable-static --enable-shared

make -j4

make install

libfdk_aac官⽹下载

https://sourceforge.net/projects/opencore-amr/files/fdk-aac/

### **4.3 下载编译mp3**

#### **下载**

git clone --depth 1 https://gitee.com/hqiu/lame.git

#### **编译**

./configure --prefix=/home/yingchun.sun/ffmpeg//build/libmp3lame --disable-shared --disable-frontend --enable-static

make

make install

libmp3lame官⽹下载（选择版本>= 3.98.3）

https://sourceforge.net/projects/lame/files/lame/

### **4.4 下载编译libvpx**

#### **下载**

git clone --depth 1 https://github.com/webmproject/libvpx.git

#### **编译**

cd libvpx

./configure --prefix=/home/yingchun.sun/ffmpeg/build/libvpx --disable-examples --disable-unit-tests --enable-vp9-highbitdepth --as=yasm

make -j4

make install

## **5.下载和编译ffmpeg**

#### **下载ffmpeg**

git clone [git://source.ffmpeg.org/ffmpeg.git](git://source.ffmpeg.org/ffmpeg.git)

或者码云的链接

git clone https://gitee.com/mirrors/ffmpeg.git

cd ffmpeg

\# 查看版本

git branch -a

\# 选择4.2版本

git checkout remotes/origin/release/4.2

git checkout remotes/origin/release/4.2

由于ffmpeg⽐较⼤，更好的选择官⽹下载ffmpeg。

#### **编译ffmpeg**

创建⼀个build.sh

将下载好的ffmpeg与x264放在⼀个⽬录下，本⼈是/home/yingchun.sun/ffmpeg。

build_ffmpeg.sh内容是：

./configure \

--prefix=/home/yingchun.sun/ffmpeg/build/ffmepg-4.2 \

--arch=x86_64 \

--enable-shared \

--enable-gpl \

--enable-libfdk-aac \

--enable-nonfree \

--enable-libvpx \

--enable-libx264 \

--enable-libmp3lame \

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libfdk-aac/includ

e" \

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libfdk-aac/lib" \

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libvpx/include" \

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libvpx/lib" \

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libx264/include" \

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libx264/lib" \

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libmp3lame/include" \

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libmp3lame/lib"

执⾏：

sh build_ffmpeg.sh

然后

make -j8

make install

#### **编译ffmpeg方案2：如果提示sh不识别命令**

不利用shell脚本，直接在shell窗口执行脚本。

./configure --prefix=/home/yingchun.sun/ffmpeg/build/ffmepg-4.2 --arch=x86_64 --enable-shared --enable-gpl --enable-libfdk-aac --enable-nonfree --enable-libvpx --enable-libx264 --enable-libmp3lame --extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libfdk-aac/includ

e"

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libfdk-aac/lib"

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libvpx/include"

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libvpx/lib" 

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libx264/include"  

--extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libx264/lib" 

--extra-cflags="-I/home/yingchun.sun/ffmpeg/build/libmp3lame/include" --extra-ldflags="-L/home/yingchun.sun/ffmpeg/build/libmp3lame/lib"