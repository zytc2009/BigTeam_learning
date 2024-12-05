[toc]

### 两个重要的配置文件

.gn文件: 定义了构建的根目录

//build/config/BUILDCONFIG.gn: 全局变量和配置的配置文件

输出构架目录

**使用gn编译项目的过程**：

1. 编写.gn文件

   模块化各个模块，分别写一个build.gn，添加进主目录的build.gn的依赖中。

   .gn是[源文件](https://zhida.zhihu.com/search?q=源文件)；.gni是头文件，类似C++中的头文件.h 通过import进行引用。

   ```shell
   import("//build/settings.gni")
   ```

2. gn gen out/Default

3. ninja -C out/Default base

### Targets

目标是构建图中的一个节点。它通常表示将生成某种可执行文件或库文件。整个构建是由一个个的目标组成。

以下是一些常使用的内置目标：

action：运行一个脚本产生一个文件

executable：生成可执行文件

shared_library：动态库，一个.dll或.so

static_library：静态库，一个.lib文件或者.a

app：可执行程序

android_apk：生成一个APK

```shell
shared_library("cameraApp") #生成一个cameraApp的动态库

static_library(“base”) {
  visibility = [ ":*" ] #模块可见性，可以设置指定模块依赖
  sources = [
    “a.cc”,
    “b.cc”,
  ]

  deps = [
  #依赖放到一个group中，再依赖
    “//fancypants”,
    “//foo/bar:baz”,
  ]
}
```



### **配置项 | Configs**

记录完成目标项所需的配置信息, 配置信息可以包括flags,defines,include_dirs等，但是不包括sources和deps/public_deps等依赖性文件。例如:

```shell
configs = [
    ":config_json_creator_test",
    "$ace_root:ace_test_config",    
]
configs -= [
    "//build/config/compiler:chromium_code",
]
configs += [
    "//build/config/compiler:no_chromium_code",
]
#config放入一个group
config(“myconfig”) {
  defines = [ “EVIL_BIT=1” ]
}

executable(“doom_melon”) {
  ...
  configs += [ “:myconfig” ]
}
```



### **源文件 | Sources**

这个标签的意思是列出来需要编译的源文件，当然，可以在其中使用条件语句进行[条件编译](https://zhida.zhihu.com/search?q=条件编译)。

```text
sources = [ #需要编译的源码
    "cameraApp/src/main/cpp/camera_ability.cpp",
    "cameraApp/src/main/cpp/camera_ability_slice.cpp",
    "cameraApp/src/main/cpp/camera_manager.cpp",
 ]
```



### **依赖项 | Deps**

即编译Targets所用到的依赖，依赖应按字母顺序排列。

当前文件中的 Deps 应首先写入，并且不能使用文件名限定。

其他 deps 应始终使用完全限定的路径名，除非出于某种原因需要[相对路径](https://zhida.zhihu.com/search?q=相对路径)名。

```text
deps = [
    "${aafwk_lite_path}/frameworks/ability_lite:aafwk_abilitykit_lite",
    "${appexecfwk_lite_path}/frameworks/bundle_lite:bundle",
]
```



### 示例

```bash
cflags = -Wall

rule cc
  command = gcc $cflags -c $in -o $out

build foo.o: cc foo.c
```

为字符串生成一个更可读的名字

```undefined
cflags = -g
```

使用$符号取值

```bash
rule cc
  command = gcc $cflags -c $in -o $out
```

定义一个叫cc的rule，然后这条rule的内容是一个可执行的命令，`$in` 展开为输入文件(`foo.c`)，`$out` 展开为输出文件(`foo.o`) for the command.

   Build语句声明输入文件和输出文件之间的关系，以build关键词开始，格式是`build outputs: rulename inputs`这个规则说明所有的输出文件都是从输入文件产生的。当output不存在或者input改变时，都会重新创建output。上面的例子里面![in代表输入列表](https://math.jianshu.com/math?formula=in%E4%BB%A3%E8%A1%A8%E8%BE%93%E5%85%A5%E5%88%97%E8%A1%A8)out代表输出列表。

```shell
#设置构建参数
gn gen out/my_build --args="..."
#查看所有的参数
gn args --list out/Default
#可以设置多个target os以及target cpugn args out/Default（或者使用--args方式）
target_os = "chromeos"
target_os = "android"

target_cpu = "arm"
target_cpu = "x86"
target_cpu = "x64"
```

















https://zhuanlan.zhihu.com/p/656507103