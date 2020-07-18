[Toc]

思维导图如下：

![](..\images\java与native相互调用.png)

> 1、注册native函数
>
> 2、JNI中的签名
>
> 3、native代码反调用Java层代码

### 注册native函数

JNI有如下两种注册native方法的途径：

> - 静态注册：
>    先由Java得到本地方法的声明，然后再通过JNI实现该声明方法
> - 动态注册：
>    先通过JNI重载JNI_OnLoad()实现本地方法，然后直接在Java中调用本地方法。

#### (一)、静态注册native函数

> 根据函数名找到对应的JNI函数；Java层调用某个函数时，会从对应的JNI中寻找该函数，如果没有就会报错，如果存在就会建立一个关联关系，以后再调用时会直接使用这个函数，这部分的操作由虚拟机完成。

静态注册就是根据函数名来遍历Java和JNI函数之间的关联，而且要求JNI层函数的名字必须遵循特定的格式。具体的实现很简单，首先在Java代码中声明native函数，然后通过javah来生成native函数的具体形式，接下来在JNI代码中实现这些函数即可。

举例如下：

```java
public class JniDemo1{
       static {
             System.loadLibrary("samplelib_jni");
        }

        private native void nativeMethod();
}
```

接来下通过`javah`来产生jni代码，假设你的包名为`com.zy.jnidemo`

```undefined
javah -jni com.zy.jnidemo.JniDemo  我一般在项目中的java目录下执行
```

然后就会得到一个JNI的.h文件，里面包含这几个native函数的声明，观察一下文件名以及函数名。其实JNI方法名的规范就出来了：

###### 返回值 + Java前缀+全路径类名+方法名+参数1JNIEnv+参数2jobject+其他参数

**注意事项**：

> - 注意分隔符：
>    Java前缀与`类名`以及`类名之间的包名`和方法名之间使用"_"进行分割；
> - 注意静态：
>    如果在Java中声明的方法是"静态的"，则native方法也是static。否则不是
> - 如果你的JNI的native方法不是通过**静态注册**方式来实现的，则不需要符合上面的这些规范，可以格局自己习惯随意命名

#### (二)、动态注册native函数

> 上面我们介绍了静态注册native方法的过程，就是Java层声明的nativ方法和JNI函数一一对应。刚开始做JNI的前期，可能会遵守静态注册的流程：1、编写带有native方法的Java类，2、使用Javah命令生成.h头文件；3、编写代码实现头文件中的方法，这样的单调的标准流程，而且还要忍受这么"长"的函数名。那有没有更简单的方式呢？比如让Java层的native方法和任意JNI函数连接起来？答案是有的——动态注册，也就是通过`RegisterNatives`方法把C/C++中的方法映射到Java中的native方法，而无需遵循特定的方法命名格式。

当我们使用System.loadLibarary()方法加载so库的时候，Java虚拟机就会找到这个`JNI_OnLoad`函数兵调用该函数，这个函数的作用是告诉Dalvik虚拟机此C库使用的是哪一个JNI版本，如果你的库里面没有写明JNI_OnLoad()函数，VM会默认该库使用最老的JNI 1.1版本。由于最新版本的JNI做了很多扩充，也优化了一些内容，如果需要使用JNI新版本的功能，就必须在JNI_OnLoad()函数声明JNI的版本。同时也可以在该函数中做一些初始化的动作，其实这个函数有点类似于`Android`中的`Activity`中的`onCreate()`方法。该函数前面也有三个关键字分别是`JNIEXPORT`，`JNICALL` ，`jint`。其中`JNIEXPORT`和`JNICALL`是两个宏定义，用于指定该函数时JNI函数。jint是JNI定义的数据类型，因为Java层和C/C++的数据类型或者对象不能直接相互的引用或者使用，JNI层定义了自己的数据类型，用于衔接Java层和JNI层，这块前面已经介绍过了，我这里就不唠叨了。

> PS：与JNI_OnLoad()函数相对应的有JNI_OnUnload()函数，当虚拟机释放的该C库的时候，则会调用JNI_OnUnload()函数来进行善后清除工作。

该函数会有两个参数，其中`*jvm`为Java虚拟机实例，`JavaVM`结构体定义以下函数：

```undefined
DestroyJavaVM
AttachCurrentThread
DetachCurrentThread
GetEnv
```

下面我们就举例说明

举例说明，首先是加载so库

```cpp
public class JniDemo1{
       static {
             System.loadLibrary("samplelib_jni");
        }
}
```

在jni中的实现

```cpp
jint JNI_OnLoad(JavaVM* vm, void* reserved)
```

并且在这个函数里面去动态的注册native方法，完整的参考代码如下：

```cpp
#include <jni.h>
#include "Log4Android.h"
#include <stdio.h>
#include <stdlib.h>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

static const char *className = "com/zy/jnidemo/JNIDemo";

static void sayHello(JNIEnv *env, jobject, jlong handle) {
    LOGI("JNI", "native: say hello ###");
}

static JNINativeMethod gJni_Methods_table[] = {
    {"sayHello", "(J)V", (void*)sayHello},
};

static int jniRegisterNativeMethods(JNIEnv* env, const char* className,
    const JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    LOGI("JNI","Registering %s natives\n", className);
    clazz = (env)->FindClass( className);
    if (clazz == NULL) {
        LOGE("JNI","Native registration unable to find class '%s'\n", className);
        return -1;
    }

    int result = 0;
    if ((env)->RegisterNatives(clazz, gJni_Methods_table, numMethods) < 0) {
        LOGE("JNI","RegisterNatives failed for '%s'\n", className);
        result = -1;
    }

    (env)->DeleteLocalRef(clazz);
    return result;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved){
    LOGI("JNI", "enter jni_onload");

    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    jniRegisterNativeMethods(env, className, gJni_Methods_table, sizeof(gJni_Methods_table) / sizeof(JNINativeMethod));

    return JNI_VERSION_1_4;
}

#ifdef __cplusplus
}
#endif
```

我们一个个来说，首先看`JNI_OnLoad`函数的实现，里面代码很简单，主要就是两个代码块，一个是if语句，一个是jniRegisterNativeMethods函数的实现。那我们一个一个来分析。

```cpp
if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    return result ;
}
```

这里调用了GetEnv函数时为了获取JNIEnv结构体指针，其实JNIEnv结构体指向了一个函数表，该函数表指向了对应的JNI函数，我们通过这些JNI函数实现JNI编程。

然后就调用了`jniRegisterNativeMethods`函数来实现注册，这里面注意一个静态变量`gJni_Methods_table`。它其实代表了一个**native方法的数组**，如果你在一个Java类中有一个native方法，这里它的size就是1，如果是两个native方法，它的size就是2，大家看下我这个`gJni_Methods_table`变量的实现

```cpp
static JNINativeMethod gJni_Methods_table[] = {
    {"sayHello", "(J)V", (void*)sayHello},
};
```

我们看到他的类型是JNINativeMethod ，那我们就来研究下JNINativeMethod

> JNI允许我们提供一个函数映射表，注册给Java虚拟机，这样JVM就可以用函数映射表来调用相应的函数。这样就可以不必通过函数名来查找需要调用的函数了。Java与JNI通过JNINativeMethod的结构来建立联系，它被定义在jni.h中，其结构内容如下：

```cpp
typedef struct { 
    const char* name; 
    const char* signature; 
    void* fnPtr; 
} JNINativeMethod; 
```

这里面有3个变量，那我们就依次来讲解下：

> - 第一个变量`name`，代表的是Java中的**函数名**
> - 第二个变量`signature`，代表的是Java中的**参数和返回值**
> - 第三个变量`fnPtr`，代表的是的**指向C函数的函数指针**

下面我们再来看下`jniRegisterNativeMethods`函数内部的实现

```cpp
static int jniRegisterNativeMethods(JNIEnv* env, const char* className,
    const JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    LOGI("JNI","Registering %s natives\n", className);
    clazz = (env)->FindClass( className);
    if (clazz == NULL) {
        LOGE("JNI","Native registration unable to find class '%s'\n", className);
        return -1;
    }

    int result = 0;
    if ((env)->RegisterNatives(clazz, gJni_Methods_table, numMethods) < 0) {
        LOGE("JNI","RegisterNatives failed for '%s'\n", className);
        result = -1;
    }

    (env)->DeleteLocalRef(clazz);
    return result;
}
```

首先通过`clazz = (env)->FindClass( className);`找到声明native方法的类
 然后通过调用`RegisterNatives函数`将注册函数的Java类，以及注册函数的数组，以及个数注册在一起，这样就实现了绑定。

上面在讲解`JNINativeMethod`结构体的时候，提到一个概念，就是"signature"即签名，这个是什么东西？我们下面就来讲解下。

### JNI中的签名

#### (一)、为什么JNI中突然多出了一个概念叫"签名"？

因为Java是支持函数重载的，也就是说，可以定义相同方法名，但是不同参数的方法，然后Java根据其不同的参数，找到其对应的实现的方法。这样是很好，所以说JNI肯定要支持的，那JNI要怎么支持那，如果仅仅是根据函数名，没有办法找到重载的函数的，所以为了解决这个问题，JNI就衍生了一个概念——"签名"，即将参数类型和返回值类型的组合。如果拥有一个该函数的签名信息和这个函数的函数名，我们就可以顺序的找到对应的Java层中的函数了。

#### (二)、如果查看类中的方法的签名

可以使用 `javap`命令：

```cpp
javap -s -p MainActivity.class

Compiled from "MainActivity.java"
public class com.zy.hellojni.MainActivity extends android.app.Activity {
  static {};
    Signature: ()V

  public com.example.hellojni.MainActivity();
    Signature: ()V

  protected void onCreate(android.os.Bundle);
    Signature: (Landroid/os/Bundle;)V

  public boolean onCreateOptionsMenu(android.view.Menu);
    Signature: (Landroid/view/Menu;)Z

  public native java.lang.String stringFromJNI(); //native 方法
    Signature: ()Ljava/lang/String;  //签名

  public native int max(int, int); //native 方法
    Signature: (II)I    //签名
}
```

我们看到上面有`()V` ，`(Landroid/os/Bundle;)V`，`(Landroid/view/Menu;)Z`，`(II)I`我们一脸懵逼，这是什么鬼，所以我们要来研究下签名的格式

#### (三)、JNI规范定义的函数签名信息

具体格式如下：

###### (参数1类型标示；参数2类型标示；参数3类型标示...)返回值类型标示

> 当参数为引用类型的时候，参数类型的标示的根式为"L包名"，其中包名的`.`(点)要换成"/"，看我上面的例子就差不多，比如`String`就是`Ljava/lang/String`，`Menu`为`Landroid/view/Menu`。

| 类型标示 | Java类型 |
| -------- | :------: |
| Z        | boolean  |
| B        |   byte   |
| C        |   char   |
| S        |  short   |
| I        |   int    |
| J        |   long   |
| F        |  float   |
| D        |  double  |

如果是基本类类型，其签名如下：

| 类型标示 | Java类型 |
| -------- | :------: |
| Z        | boolean  |
| B        |   byte   |
| C        |   char   |
| S        |  short   |
| I        |   int    |
| J        |   long   |
| F        |  float   |
| D        |  double  |

这个 其实很好记的，除了boolean和long，其他都是首字母大写。

如果返回值是void，对应的签名是**V**。
 这里重点说1个特殊的类型，一个是数组及Array

| 类型标示           | Java类型 |
| ------------------ | :------: |
| [签名              |   数组   |
| [i                 |  int[]   |
| [Ljava/lang/Object | String[] |

### native代码反调用Java层代码

上面讲解了如何从JNI中调用Java类中的方法，其实在jni.h中已经定义了一系列函数来实现这一目的，下面我们就以此举例说明：

#### (一)、获取`Class`对象

为了能够在C/C++中调用Java中的类，`jni.h`的头文件专门定义了jclass类型表示Java中Class类。JNIEnv中有3个函数可以获取jclass。

> - jclass FindClass(const char* clsName)：
>    通过类的名称(类的全名，这时候包名不是用'"."点号而是用"/"来区分的)来获取jclass。

```php
jclass jcl_string=env->FindClass("java/lang/String");
```

来获取Java中的String对象的class对象

> - jclass GetObjectClass(jobject obj)：
>    通过对象实例来获取jclass，相当于Java中的getClass()函数
> - jclass getSuperClass(jclass obj)：
>    通过jclass可以获取其父类的jclass对象

#### (二)、获取属性方法

在Native本地代码中访问Java层的代码，一个常用的常见的场景就是获取Java类的属性和方法。所以为了在C/C++获取Java层的属性和方法，JNI在jni.h头文件中定义了jfieldID和jmethodID这两种类型来分别代表Java端的属性和方法。在访问或者设置Java某个属性的时候，首先就要现在本地代码中取得代表该Java类的属性的jfieldID，然后才能在本地代码中进行Java属性的操作，同样，在需要调用Java类的某个方法时，也是需要取得代表该方法的jmethodID才能进行Java方法操作。

常见的调用Java层的方法如下：

> 一般是使用JNIEnv来进行操作
>
> - GetFieldID/GetMethodID：获取某个属性/某个方法
> - GetStaticFieldID/GetStaticMethodID：获取某个静态属性/静态方法

方法的具体实现如下：

```cpp
jfieldID GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
jfieldID GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz,const char *name, const char *sig);
```

大家发现什么规律没有？对了，我们发现他们都是4个入参，而且每个入参的都是`*JNIEnv *env`，`jclass clazz`，`const char *name`，`const char *sig`。关于`JNIEnv`，前面我们已经讲过了，这里我们就不详细讲解了，`JNIEnv`代表一个JNI环境接口，`jclass`上面也说了代表Java层中的"类"，`name`则代表方法名或者属性名。那最后一个`char *sig`代表什么？它其实代表了JNI中的一个**特殊字段**——签名，上面已经讲解过了。我们这里就不在冗余了。

#### (三)、构造一个对象

常用的JNI中创建对象的方法如下：

```undefined
jobject NewObject(jclass clazz, jmethodID methodID, ...)
```

比如有我们知道Java类中可能有多个构造函数，当我们要指定调用某个构造函数的时候，会调用下面这个方法

```php
jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
obj = (*env)->NewObject(env, cls, mid);
```

即把指定的构造函数传入进去即可。
 现在我们来看下他上面的二个主要参数

> - clazz：是需要创建的Java对象的Class对象
> - methodID：是传递一个方法ID，想一想Java对象创建的时候，需要执行什么操作？就是执行构造函数。

有人会说这要走两行代码，有没有一行代码的，是有的，如下：

```cpp
jobject NewObjectA(JNIEnv *env, jclass clazz, 
jmethodID methodID, jvalue *args);
```

这里多了一个参数，即`jvalue *args`，这里是`args`代表的是对应构造函数的所有参数的，我们可以应将传递给构造函数的所有参数放在jvalues类型的数组args中，该数组紧跟着放在methodID参数的后面。NewObject()收到数组中的这些参数后，将把它们传给编程任索要调用的Java方法。

上面说到，参数是个数组，如果参数不是数组怎么处理，jni.h同样也提供了一个方法，如下：

```cpp
jobject NewObjectV(JNIEnv *env, jclass clazz, 
jmethodID methodID, va_list args);
```

这个方法和上面不同在于，这里将构造函数的所有参数放到在va_list类型的参数args中，该参数紧跟着放在methodID参数的后面。



### JNI的常用方法的中文API

![](..\images\jni常用方法.png)

#### 一、Interface Function Table(接口函数表)

每个函数都可以通过`JNIEnv`参数访问，JNIEnv类型是指向一个存放所有JNI接口指针的指针，其定义如下：

```cpp
typedef const struct JNINativeInterface *JNIEnv;
```

> 虚拟机初始化函数表，如下面代码所示，前三个条目是为了将来和COM兼容而保留的。另外，我们在函数表的开头附近保留了一些额外的NULL条目，例如，可以在FindClass之后添加未来与类相关的JNI操作，而不是在表的末尾。请注意，函数表可以在所有JNI接口指针之间共享。

首先我们来看下`JNINativeInterface`

```cpp
const struct JNINativeInterface ... = {
    DefineClass,
    FindClass,
    ... 具体参考文后链接
  };
```

#### 二、获取JNI版本信息

在JNIEnv指针中，有个函数用于获取JNI的版本：

```cpp
jint GetVersion(JNIEnv *env);
```

该方法主要返回本地JNI方法接口的版本信息。在不同的JDK环境下返回值是不同的

其实是早就被定义为一个宏了，如下：

```csharp
#define JNI_VERSION_1_1 0x00010001
#define JNI_VERSION_1_2 0x00010002

/* Error codes */
#define JNI_EDETACHED    (-2)              /* thread detached from the VM */
#define JNI_EVERSION     (-3)              /* JNI version error 
SINCE JDK/JRE 1.4:
    #define JNI_VERSION_1_4 0x00010004
SINCE JDK/JRE 1.6:
    #define JNI_VERSION_1_6 0x00010006
```

#### 三、Java 类 操作

##### (一)、定义类(加载类)

```cpp
jclass DefineClass(JNIEnv *env,const char* name,jobject loader,const jbyte *buf, jsize bufLen)
```

这个函数，主要是从包含数据的buffer中加载类，该buffer包含类调用时未被虚拟机所引用的原始类数据。

入参解释：

> - env：JNI接口指针
> - name：所定义的类名或者接口名，该字符串有modefied UTF-8编码
> - loader：指派给定义的类加载器
> - buf：包含.class文件数据的buffer
> - bufLen：buffer长度

返回：Java类对象，当错误出现时返回NULL

可能抛出的异常：

> - 如果没有指定这个Java类的，则会抛出`ClassFormatError`
> - 如果是一个类/接口是它自己的一个父类/父接口，则会抛出`ClassCircularityError`
> - 如果内存不足，则会抛出`OutOfMemoryError`
> - 如果想尝试在Java包中定义一个类，则会抛出`SecurityException`

##### (二)、查找类

```cpp
jclass FindClass(JNIEnv *env,const char *name);
```

这里面有两种情况一个是JDK release1.1，另外一种是JDK release 1.2
 。从JDK release 1.1，该函数加载一个本地定义类，它搜索CLASSPATH环境变量里的目录及zip文件查找特定名字的类。自从Java 2 release 1.2，Java安全模型允许非系统类加载跟调用本地方法。FindClass定义与当前本地方法关联的类加载，也就是声明本地方法的类的类加载类。如果本地方法属于系统类，则不会涉及类加载器；否则，将调用适当的类加载来加载和链接指定的类。从Java 2 SDK1.2版本开始，通过调用接口调用FindClass时，没有当前的本机方法或关联的的类加载器。在这种情况下，在这种情况下，使用ClassLoader.getSystemClassLoader的结果。这是虚拟机为应用程序创建的类加载器，并且能够找到java.class.path属性列出的类。

入参解释：

> - env：JNI接口指针
> - name：一个完全限定的类名，即包含“包名”+“/”+类名。举个例子：如`java.lang.String`，该参数为`java/lang/String`；如果类名以`[`开头，将返回一个数组类。比如数组类的签名为`java.lang.Object[]`，该参数应该为"[Ljava/lang/Object"

返回：
 返回对应完全限定类对象，当找不到类时，返回NULL

可能抛出的异常：

> - 如果没有指定这个Java类的，则会抛出`ClassFormatError`
> - 如果是一个类/接口是它自己的一个父类/父接口，则会抛出`ClassCircularityError`
> - 如果没有找到该类/接口的定义，则抛出`NoClassDefFoundError`
> - 如果内存不足，则会抛出`OutOfMemoryError`

##### (三)、查找父类

```cpp
jclass GetSuperclass(JNIEnv *env,jclass clazz);
```

如果clazz不是Object类，则此函数将返回表示该clazz的父类的Class对象，如果该类是Object，或者clazz代表接口，则此函数返回NULL。

入参解释：

> - env：JNI接口指针
> - clazz：Java的Class类

返回：
 如果clazz有父类则返回其父类，如果没有其父类则返回NULL

##### (四)、安全转换

```cpp
jboolean IsAssignableFrom(JNIEnv *env,jclass clazz1,jclass clazz2);
```

判断clazz1的对象是否可以安全地转化为clazz2的对象

入参解释：

> - env：JNI接口指针
> - clazz1：Java的Class类，即需要被转化的类
> - clazz2：Java的Class类，即需要转化为目标的类

返回：
 如果满足以下任一条件，则返回JNI_TRUE：

- 如果clazz1和clazz2是同一个Java类。
- 如果clazz1是clazz2的子类
- 如果clazz1是clazz2接口的实现类

#### 四、异常 操作

##### (一)、抛出异常

```cpp
jint Throw(JNIEnv *env,jthrowable obj);
```

传入一个jthrowable对象，并且在JNI并将其抛起

入参解释：

> - env：JNI接口指针
> - jthrowable：一个Java的java.lang.Throwable对象

返回：
 成功返回0，失败返回一个负数。

可能抛出的异常：
 抛出一个java.lang.Throwable 对象

##### (二)、构造一个新的异常并抛出

```cpp
jint ThrowNew(JNIEnv *env,jclass clazz,const char* message);
```

传入一个message，并用其构造一个异常并且抛出。

入参解释：

> - env：JNI接口指针
> - jthrowable：一个Java的java.lang.Throwable对象
> - message：用于构造一个java.lang.Throwable对象的消息，该字符串用modified UTF-8编码

返回：
 如果成功返回0，失败返回一个负数

可能抛出的异常：
 抛出一个新构造的java.lang.Throwable 对象

##### (三)、检查是否发生异常，并抛出异常

```cpp
jthrowable ExceptionOccurred(JNIEnv *env);
```

检测是否发生了异常，如果发生了，则返回该异常的引用(再调用ExceptionClear()函数前，或者Java处理异常前)，如果没有发生异常，则返回NULL。

入参解释：

> - env：JNI接口指针

返回：
 jthrowable的异常引用或者NULL

##### (四)、打印异常的堆栈信息

```cpp
void ExceptionDescribe(JNIEnv *env)
```

打印这个异常的堆栈信息

入参解释：

> - env：JNI接口指针

##### (五)、清除异常的堆栈信息

```cpp
void ExceptionClear(JNIEnv *env);
```

清除正在抛出的异常，如果当前没有异常被抛出，这个函数不起作用

入参解释：

> - env：JNI接口指针

##### (六)、致命异常

```cpp
void FatalError(JNIEnv *env,const char* msg);
```

致命异常，用于输出一个异常信息，并终止当前VM实例，即退出程序。

入参解释：

> - env：JNI接口指针
> - msg：异常的错误信息，该字符串用modified UTF-8编码

##### (七)、仅仅检查是否发生异常

```cpp
jboolean ExceptionCheck(JNIEnv *env);
```

检查是否已经发生了异常，如果已经发生了异常，则返回JNI_TRUE，否则返回JNI_FALSE

入参解释：

> - env：JNI接口指针

返回：
 如果已经发生异常，返回JNI_TRUE，如果没有发生异常则返回JNI_FALSE

#### 五、全局引用和局部引用

##### (一)、创建全局引用

```csharp
jobject NewGlobalRef(JNIEnv *env,object obj);
```

给对象obj创建一个全局引用，obj可以是全局或局部引用。全局引用必须通过DeleteGlobalRef()显示处理。

参数解释：

> - env：JNI接口指针
> - obj：object对象

返回：
 全局引用jobject，如果内存溢出则返回NULL

##### (二)、删除全局引用

```cpp
void DeleteGlobalRef(JNIEnv *env,jobject globalRef);
```

删除全局引用

参数解释：

> - env：JNI接口指针
> - globalRef：需要被删除的全局引用

##### (三)、删除局部引用

局部引用只在本地接口调用时的生命周期内有效，当本地方法返回时，它们会被自动释放。每个局部引用都会消耗一定的虚拟机资源，虽然局部引用可以被自动销毁，但是程序员也需要注意不要在本地方法中过度分配局部引用，过度分配局部引用会导致虚拟机在执行本地方法时内存溢出。

```cpp
void DeleteLocalRef(JNIEnv *env, jobject localRef); 
```

通过localRef删除局部引用

参数解释

> - env：JNI接口指针
> - localRef：需要被删除的局部引用

JDK/JRE 1.1提供了上面的DeleteLocalRef函数，这样程序员就可以手动删除本地引用。
 从JDK/JRE 1.2开始，提供可一组生命周期管理的函数，他们是下面四个函数。

##### (四)、设定局部变量的容量

```cpp
jint EnsureLocalCapacity(JNIEnv *env,jint capacity);
```

在当前线程中，通过传入一个容量capacity，，限制局部引用创建的数量。成功则返回0，否则返回一个负数，并抛出一个OutOfMemoryError。VM会自动确保至少可以创建16个局部引用。

参数解释

> - env：JNI接口指针
> - capacity：容量

返回：
 成功返回0，失败返回一个负数，并会抛出一个OutOfMemoryError

为了向后兼容，如果虚拟机创建了超出容量的局部引用。VM调用FatalError，来保证不能创建更多的本地引用。(如果是debug模式，虚拟机回想用户发出warning，并提示创建了更多的局部引用，在JDK中，程序员可以提供-verbose：jni命令行选项来打开这个消息)

##### (五)、在老的上创建一个新的帧

```cpp
jint PushLocalFram(JNIEnv *env ,jint capacity);
```

在已经设置设置了局部变量容量的情况下， 重新创建一个局部变量容器。成功返回0，失败返回一个负数并抛出一个OutOfMemoryError异常。

注意：当前的局部帧中，前面的局部帧创建的局部引用仍然是有效的

参数解释

> - env：JNI接口指针
> - capacity：容量

##### (六)、释放一个局部引用

```undefined
jobject PopLocalFrame(JNIEnv *env,jobject result)
```

弹出当前的局部引用帧，并且释放所有的局部引用。返回在之前局部引用帧与给定result对象对应的局部引用。如果不需要返回任何引用，则设置result为NULL

参数解释

> - env：JNI接口指针
> - result：需要释放的局部引用

##### (七)、创建一个局部引用

```csharp
jobject NewLocalRef(JNIEnv *env,jobject ref);
```

创建一个引用自ref的局部引用。ref可以是全局或者局部引用，如果ref为NULL，则返回NULL。

参数解释

> - env：JNI接口指针
> - ref：可以试试局部引用也可以是全局引用。

##### (八)、弱全局引用

弱全局引用是一种特殊的全局引用，不像一般的全局引用，一个弱全局引用允许底层Java对象能够被垃圾回收。弱全局引用能够应用在任何全局或局部引用被使用的地方。当垃圾回收器运行的时候，如果对象只被弱引用所引用时，它将释放底层变量。一个弱阮菊引用指向一个被释放的对象相当于等于NULL。编程人员可以通过使用isSampleObject对比弱引用和NULL来检测一个弱全局应用是否指向一个被释放的对象。弱全局引用在JNI中是Java弱引用的一个简化版本，在Java平台API中有有效。

当Native方法正在运行的时候，垃圾回收器可能正在工作，被弱引用所指向的对象可能在任何时候被释放。弱全局引用能够应用在任何全局引用所使用的地方，通常是不太适合那么做的，因为它们可能在不注意的时候编程NULL。

当IsSampleObject能够识别一个弱全局引用是不是指向一个被释放的对象，但是这不妨碍这个对象在被检测之后马上被释放。这就说明了，程序员不能依赖这个方法来识别一个弱全局引用是否能够在后续的JNI函数调用中被使用。

如果想解决上述的问题，建议使用JNI函数NewLocalRef或者NewGlobalRef来用标准的全局也引用或者局部引用来指向相同的对象。如果这个独享已经被释放了这些函数会返回NULL。否则会返回一个强引用(这样就可以保证这个对象不会被释放)。当不需要访问这个对象时，新的引用必须显式被删除。

###### 1、创建全局弱引用

```cpp
jweak NewWeakGlobalRef(JNIEnv *env,jobject obj);
```

创建一个新的弱全局引用。如果obj指向NULL，则返回NULL。如果VM内存溢出，将会抛出异常OutOfMemoryError。

参数解释

> - env：JNI接口指针
> - obj：引用对象

返回：
 全局弱引用

###### 2、删除全局弱引用

```cpp
void DeleteWeakGlobalRef(JNIEnv *env,jweak obj);
```

VM根据所给定的弱全局引用删除对应的资源。

参数解释

> - env：JNI接口指针
> - obj：将删除的弱全局引用

#### 六、对象操作

##### (一)、直接创建一个Java对象

```cpp
jobject AllocObject(JNIEnv *env,jclass clazz);
```

不借助任何构造函数的情况下分配一个新的Java对象，返回对象的一个引用。

参数解释：

> - env：JNI接口指针
> - clazz:：Java类对象

返回：
 返回一个Java对象，如果该对象无法被创建，则返回NULL。

异常：

- 如果该类是接口或者是抽象类，则抛出InstantiationException
- 如果是内存溢出，则抛出OutOfMemoryError

##### (二)、根据某个构造函数来创建Java对象

```cpp
jobject NewObject(JNIEnv *env,jclass clazz,jmethodID methodID,...);
jobject NewObjectA(JNIEnv *env,jclass clazz,jmethodID methodID,const jvalue *args);
jobject NewObjectV(JNIEnv *env,jclass clazz,jmethodID methodID,va_list args);
```

构造一个新的Java对象，methodID表明需要调用一个构造函数。这个ID必须通过调用GetMethodID()获得，GetMethodID()为函数名，void(V)为返回值。clazz参数不能纸箱一个数组类

- `NewObject`：需要把所有构造函数的入参，放在参数methodID之后。NewObject()接受这些参数并将它们传递给需要被调用的Java的构造函数
- `NewObjectA`：在methodID后面，放了一个类型为jvalue的参数数组——args，该数组存放着所有需要传递给构造函数的参数。NewObjectA()接收到这个数组中的所有参数，并且按照顺序将它们传递给需要调用的Java方法。
- `NewObjectV`：在methodID后面，放了一个类型为va_list的args，参数存放着所有需要传递给构造函数的参数。NewObjectv()接收到所有的参数，并且按照顺序将它们传递给需要调用的Java方法。

参数解释：

> - env：JNI接口指针
> - clazz:：Java类
> - methodID：构造函数的方法ID

附加参数：

- NewObject的附加参数：arguments是构造函数的参数
- NewObjectA的附加参数：args是构造函数的参数数组
- NewObjectV的附加参数：args是构造函数的参数list

返回：
 Java对象，如果无法创建该对象，则返回NULL

异常：
 如果传入的类是接口或者抽象类，则抛出InstantiationException
 如果内存溢出，则抛出OutOfMemoryError
 `所有的异常都是通过构造函数抛出`

##### (三)、获取某个对象的“类”

```csharp
jclass GetObjectClass(JNIEnv *env,object obj);
```

返回obj对应的类

参数解释

> - env：JNI接口指针
> - obj：Java对象，不能为NULL

参数：
 env：JNI接口指针
 obj：JAVA对象，不能为NULL

返回：
 返回一个Java“类”对象

##### (四)、获取某个对象的“类型”

```cpp
jobjectRefType GetObjectRefType(JNIEnv *env,jobject obj);
```

返回obj参数所以指向对象的类型，参数obj可以是局部变量，全局变量或者若全局引用。

参数解释

> - env：JNI接口指针
> - obj：局部、全局或弱全局引用

返回：

- JNIInvalidRefType=0：代表obj参数不是有效的引用类型
- JNILocalRefType=1：代表obj参数是局部变量类型
- JNIGlobalRefType=2：代表obj参数是全局变量类型
- JNIWeakGlobalRefType=3：代表obj参数是弱全局有效引用

无效的引用就是没有引用的引用。也就是说，obj的指针没有指向内存中创建函数时候的地址，或者已经从JNI函数中返回了。所以说NULL就是无效的引用。并且`GetObjectRefType(env,NULL)`将返回类型是`JNIInvalidRefType`。但是空引用返回的不是`JNIInvalidRefType`，而是它被创建时候的引用类型。

> PS:不能在引用在删除的时候，调用该函数

##### (五)、判断某个对象是否是某个“类”的子类

```cpp
jboolean IsInstanceOf(JNIEnv *env, jobject obj,jclass clazz); 
```

测试obj是否是clazz的一个实例

参数：

> - env：JNI接口指针
> - obj：一个Java对象
> - clazz：一个Java的类

返回：
 如果obj是clazz的实例，则返回JNI_TRUE；否则则返回JNI_FALSE；一个空对象可以是任何类的实例。

##### (六)、判断两个引用是否指向同一个引用

```cpp
jboolean IsSampleObject(JNIEnv *env,jobject ref1,jobject ref2);
```

判断两个引用是否指向同一个对象

参数解释：

> - env：JNI接口指针
> - ref1：Java对象
> - ref2：Java对象

返回：
 如果同一个类对象，返回JNI_TRUE；否则，返回JNI_FALSE；

##### (七)、返回属性id

```cpp
jfieldID GetFieldID(JNIEnv *env,jclass clazz,const char *name,const char *sig);
```

获取某个类的非静态属性id。通过方法`属性名`以及·属性的签名`(也就是属性的类型)，来确定对应的是哪个属性。通过检索这个属性ID，我们就可以调用Get <type>Field和Set <type>Field了，就是我们常用的`get`和`set`方法

参数解释：

> - env：JNI接口指针
> - clazz：一个Java类对象
> - name：以"0"结尾的，而且字符类型是"utf-8"的属性名称
> - sig：以"0"结尾的，而且字符类型是"utf-8"的属性签名

返回
 属性对应ID，如果操作失败，则返回NULL

异常：
 如果找不到指定的属性，则抛出NoSuchFieldError
 如果类初始化失败，则抛出ExceptionInitializerError
 如果内存不足了，则抛出OutOfMemoryError

> PS：`GetFieldID()`可能会导致还未初始化的类开始初始化，同时在获取数组的长度不能使用`GetFieldID()`，而应该使用`GetArrayLength()`。

##### (八)、返回属性id系列

```cpp
NativeType GetField(JNIEnv *env,jobject obj,jfieldID fielD);
```

返回某个类的非静态属性的值，这是一组函数的简称，具体如下：

```undefined
jobject        GetObjectField(JNIEnv *env,jobject obj,jfieldID fielD)   
jboolean     GetBooleanField(JNIEnv *env,jobject obj,jfieldID fielD)
jbyte           GetByteField(JNIEnv *env,jobject obj,jfieldID fielD)
jchar           GetCharField(JNIEnv *env,jobject obj,jfieldID fielD)
jshort          GetShortField(JNIEnv *env,jobject obj,jfieldID fielD)
jint              GetIntField(JNIEnv *env,jobject obj,jfieldID fielD)
jlong           GetLongField(JNIEnv *env,jobject obj,jfieldID fielD)
jfloat           GetFloatField(JNIEnv *env,jobject obj,jfieldID fielD)
jdouble       GetDoubleField(JNIEnv *env,jobject obj,jfieldID fielD)
```

参数解释：
 env：JNI接口指针
 obj：Java对象，不能为空
 fieldID：有效的fieldID

返回：
 对应属性的值

##### (九)、设置属性id系列

```csharp
void Set<type>Field(JNIEnv *env,jobject obj,jfieldID fieldID,NativeType value)
```

设置某个类的的非静态属性的值。其中具体哪个属性通过`GetFieldID()`来确定哪个属性。这是一组函数的简称，具体如下：

```cpp
void SetObjectField(jobject)
void SetBooleanField(jboolean)
void SetByteField(jbyte)
void SetCharField(jchar)
void SetShortField(jshort)
void SetIntField(jint)
void SetLongField(jlong)
void SetFloatField(jfloat)
void SetDoubleField(jdouble)
```

参数解释：

> - env：JNI接口指针
> - obj：Java对象，不能为空
> - fieldID：有效的属性ID
> - value：属性的新值

##### (十)、获取某个类的某个方法id

```cpp
jmethodID GetMethodID(JNIEnv *env,jclass clazz,const char*name,const char* sig);
```

返回某个类或者接口的方法ID，该方法可以是被定义在clazz的父类中，然后被clazz继承。我们是根据方法的名字以及签名来确定一个方法的。

> PS:`GetMethodID()`会造成还未初始化的类，进行初始化
>  如果想获取构造函数的ID,请提供`init`作为方法名称，并将`void(V)`作为返回类型

参数解释：

> - env：JNI接口指针
> - clazz：Java类对象
> - name：以0结尾的，并且是"utf-8"的字符串的方法名称
> - sig：以0结尾的，并且是"utf-8"的字符串的方法签名

返回：
 返回一个方法ID，没有找到指定的方法，则返回NULL

异常：
 如果找不到指定的方法，则抛出NoSuchMethodError
 如果累初始化失败，则抛出ExceptionInInitializerError
 如果内存不够，则抛出OutOfMemoryError

##### (十一)、调用Java实例的某个非静态方法“系列”

```go
NativeType Call<type>Method(JNIEnv *env,jobject obj,jmethodID methodID,...);
NativeType Call<type>MethodA(JNIEnv *env,jobjct obj,jmethodID methodID ,const jvalue *args);
NativeType  Call<type>MethodV(JNEnv *env,jobject obj,jmethodID methodID,va_list args); 
```

这一些列都是在`native`中调用Java对象的某个非静态方法，它们的不同点在于传参不同。是根据方法ID来指定对应的Java对象的某个方法。methodID参数需要调用`GetMethodID()`获取。

> PS：当需要调用某个"private"函数或者构造函数时，这个methodID必须是obj类的方法，不能是它的父类的方法。

下面我们来看下他们的不同点

- CallMethod：需要把方法的`入参`放在参数`methodID`后面。`CallMethod()`其实把这些参数传递给需要调用的Java方法。
- CallMethodA：在`methodID`后面，有一个类型为`jvalue`的args数组，该数组存放所有需要传递给构造函数的参数。`CallMethodA()`收到这个数组中的参数，是按照顺序将他们传递给对应的Java方法
- CallMethodV：在`methodID`后面，有一个类型Wie`va_list`的参数args，它存放着所有需要传递给构造函数的参数。CallMethodV()接收所有的参数，并且按照顺序将它们传递给需要调用的Java方法。

```dart
Call<type>Method Routine Name            Native Type
... //参考文后链接
```

参数解释：

> - env：JNI接口指针
> - obj：对应的Java对象
> - methodID：某个方法的方法id

返回：
 返回调用Java方法对应的结果

异常：
 在Java方法执行过程中产生的异常。

##### (十二)、调用某个类的非抽象方法

调用父类中的实例方法，如下系列

```bash
CallNonvirtual<type>Method 
CallNonvirtual<type>MethodA 
CallNonvirtual<type>MethodV 
```

具体如下：

```dart
NativeType CallNonvirtual<Type>Method(JNIEnv *env,jobject obj,jclass clazz,jmethodID methodID,....);
NativeType CallNonvirtual<Type>MethodA(JNIEnv *env,jobject obj,jclass clazz,jmethodID methodID,const jvalue *args);
NativeType CallNonvirtual<type>MethodV(JNIEnv *env, jobject obj,
jclass clazz, jmethodID methodID, va_list args);
```

这一系列操作就是根据特定的类，和其方法ID来调用Java对象的实例的非静态方法，methodID参数需要调用GetMethodID()获取。

`CallNonvirtual<Type>Method`和Call<type>Method`是不同的，其中`CallNonvirtual<Type>Method`是基于"类"，而`和Call<type>Method`是基于类的对象。所以说`CallNonvirtual<Type>Method`的入参是 clazz，methodID必须来源与obi的类，而不是它的父类

下面我们来看下他们的不同点

- CallNonvirtual<type>Method ：需要把方法的`入参`放在参数`methodID`后面。`CallNonvirtual<type>Method()`其实把这些参数传递给需要调用的Java方法。
- CallNonvirtual<type>Method：在`methodID`后面，有一个类型为`jvalue`的args数组，该数组存放所有需要传递给构造函数的参数。`CallNonvirtual<type>Method()`收到这个数组中的参数，是按照顺序将他们传递给对应的Java方法
- CallNonvirtual<type>MethodV ：在`methodID`后面，有一个类型Wie`va_list`的参数args，它存放着所有需要传递给构造函数的参数。 CallNonvirtual<type>MethodV()接收所有的参数，并且按照顺序将它们传递给需要调用的Java方法。

将上面这系列方法展开如下：

```dart
CallNonvirtual<type>Method Routine Name      Native Type
CallNonvirtualVoidMethod()
CallNonvirtualVoidMethodA()                  void
CallNonvirtualVoidMethodV()
... //参考文后链接
```

参数解释：

> - env：JNI接口指针
> - obj：Java对象
> - clazz：Java类
> - methodID：方法ID

返回：
 调用Java方法的结果

抛出异常：
 在Java方法中执行过程可能产生的异常

##### (十三)、获取静态属性

```cpp
jfieldID GetStaticFieldID(JNIEnv *env,jclass clazz,const char* name,const char *sig);
```

获取某个类的某个静态属性ID，根据属性名以及标签来确定是哪个属性。`GetStaticField()`和`SetStaticField()`通过使用属性ID来对属性进行操作的。如果这个类还没有初始化，直接调用`GetStaticFieldID()`会引起这个类进行初始化。

参数解释：

> - env：JNI接口指针
> - clazz：Java类
> - name：静态属性的属性名，是一个编码格式"utf-8"并且以0结尾的字符串。
> - sig：属性的签名，是一个编码格式"utf-8"并且以0结尾的字符串。

返回：
 返回静态属性ID，如果指定的静态属性无法找则返回NULL

异常：
 如果指定的静态属性无法找到则抛出`NoSuchFieldError`
 如果类在初始化失败，则抛出`ExceptionInInitializerError`
 如果内存不够，则抛出`OutOfMemoryError`

##### (十四)、获取静态属性系列

```bash
NativeType GetStatic<type>Field(JNIEnv *env,jclass clazz,jfieldID fieldID);
```

这个系列返回一个对象的静态属性的值。可以通过`GetStaticFieldID()`来获取静态属性的的ID，有了这个ID，我们就可以获取这个对其进行操作了

下面表明了函数名和函数的返回值，所以只需要替换`GetStatic<type>Field`中的类替换为该字段的Java类型或者表中的实际静态字段存取器。并将`NativeType`替换为相应的本地类型

```bash
GetStatic<type>Field Routine Name      Native Type
... //参考文后链接
```

参数解释：

> - env：JNI接口指针
> - clazz：Java类
> - field：静态属性ID

返回：
 返回静态属性

##### (十五)、设置静态属性系列

```csharp
void SetStatic<type>Field(JNIEnv *env,jclass clazz,jfieldID fieldID,NativeType value);
```

这个系列是设置类的静态属性的值。可以通过`GetStaticFieldID()`来获取静态属性的ID。

下面详细介绍了函数名和其值，你可以通过`SetStatic<type>`并传入的NativeType来设置Java中的静态属性。

```bash
SetStatic<type>Field Routine Name         NativeType
... //参考文后链接
```

参数解释：

> - env：JNI接口指针
> - clazz：Java类
> - field：静态属性ID
> - value：设置的值

##### (十六)、获取静态函数ID

```cpp
jmethodID GetStaticMethodID(JNIEnv *env,jclass clazz,const char *name,const char sig);
```

返回类的静态方法ID，通过它的方法名以及签名来确定哪个方法。如果这个类还没被初始化，调用`GetStaticMethodID()`将会导致这个类初始化。

参数解释：

> - env：JNI接口指针
> - clazz：Java类
> - name：静态方法的方法名，以"utf-8"编码的，并且以0结尾的字符串
> - sig：方法签名，以"utf-8"编码的，并且以0结尾的字符串

返回：
 返回方法ID，如果操作失败，则返回NULL

异常：
 如果没有找到对应的静态方法，则抛出`NoSuchMethodError`
 如果类初始化失败，则抛出`ExceptionInInitializerError`
 如果系统内存不足，则抛出`OutOfMemoryError`

##### (十七)、调用静态函数系列

```bash
NativeType CallStatic<type>Method(JNIEnv *env,jclass clazz,jmethodID methodID,...);
NativeType CallStatic<type>MethodA(JNIEnv *env,jclass clazz,jmethodID methodID,... jvalue *args);
NativeType CallStatic<type>MethodV(JNIEnv *env,jclass,jmethodID methodid, va_list args)
```

根据指定的方法ID，就可以操作Java对象的静态方法了。可以通过`GetStaticMethodID()`来获得methodID。方法的ID必须是clazz的，而不是其父类的方法ID。

下面就是详细的方法了

```dart
CallStatic<type>Method Routine Name                Native Type
... //参考文后链接
```

参数解释：

> - env：JNI接口指针
> - clazz：Java类
> - methodID：静态方法ID

返回：
 返回静态的Java方法的调用方法

异常：
 在Java方法中执行中抛出的异常

#### 七、字符串操作

##### （一)、创建一个字符串

```cpp
jstring NewString(JNIEnv *env,const jchar *unicodeChars,jszie len);
```

参数解释：

> - env：JNI接口指针
> - unicodeChars：指向Unicode字符串的指针
> - len：unicode字符串的长度

返回：
 返回一个Java字符串对象，如果该字符串无法被创建在，则返回NULL

异常：
 如果内存不足，则抛出`OutOfMemoryError`

##### （二)、获取字符串的长度

```cpp
jsize  GetStringLength(JNIEnv *env,jstring string);
```

返回Java字符串的长度(unicode字符的个数)

参数解释：

> - env：JNI接口指针
> - string：Java字符串对象

返回：
 返回Java字符串的长度

##### （三)、获取字符串的指针

```cpp
const jchar* GetStringChar(JNIEnv *env,jstring string , jboolean *isCopy);
```

返回指向字符串的UNICODE字符数组的指针，该指针一直有效直到被`ReleaseStringchars()`函数调用。
 如果`isCopy`为非空，则在复制完成后将`isCopy`设为`JNI_TRUE`。如果没有复制，则设为`JNI_FALSE`。

参数解释：

> - env：JNI接口指针
> - string：Java字符串对象
> - isCopy：指向布尔值的指针

返回：
 返回一个指向unicode字符串的指针，如果操作失败，则返回NULL

##### （四)、释放字符串

```cpp
void ReleaseStringChars(JNIEnv *env,jstring string,const jchar *chars);
```

通过VM，native代码不会再访问`chars`了。参数`chars`是一个指针。可以通过`GetStringChars()`函数获得。

参数解释：

> - env：JNI接口指针
> - string：Java字符串对象
> - chars：指向Unicode字符串的指针

##### （五)、创建一个UTF-8的字符串

```cpp
jstring NewStringUTF(JNIEnv *env,const char *bytes);
```

创建一个UTF-8的字符串。

参数解释：

> - env：JNI接口指针
> - bytes：指向UTF-8字符串的指针

返回：
 Java字符串对象，如果无法构造该字符串，则为NULL。

异常：
 如果系统内存不足，则抛出`OutOfMemoryError`

##### （六)、获取一个UTF-8的字符串的长度

```cpp
jsize GetStringUTFLength(JNIEnv *env,jstring string);
```

以字节为单位，返回字符串UTF-8的长度。

参数解释：

> - env：JNI接口指针
> - String：Java字符串对象

返回：
 字符串的UTF-8的长度

##### （七)、获取StringUTFChars的指针

```cpp
const char *GetStringUFTChars(JNIEnv *env, jString string, jboolean *isCopy);
```

返回指向UTF-8字符数组的指针，除非该数组被`ReleaseStringUTFChars()`函数调用释放，否则一直有效。
 如果`isCopy`不是NULL，`*isCopy`在赋值完成后即被设置为`JNI_TRUE`。如果未复制，则设置为`JNI_FALSE`。

参数解释：

> - env：JNI接口指针
> - String：Java字符串对象
> - isCopy：指向布尔值的指针

返回：
 指向UTF-8的字符串指针，如果操作是啊白，则返回NULL

##### （八)、释放UTFChars

```cpp
void ReleaseStringUTFChars(JNIEnv *env,jstring string,const char *urf)
```

通过虚拟机，native代码不再访问了utf了。utf是一个指针，可以调用`GetStringUTFChars()`获取。

参数解释：

> - env：JNI接口指针
> - string：Java字符串对象
> - utf：指向utf-8字符串的指针

> 注意：在JDK/JRE 1.1，程序员可以在用户提供的缓冲区获取基本类型数组元素，从JDK/JRE1.2开始，提供了额外方法，这些方法允许在用户提供的缓冲区获取Unicode字符(UTF-16编码)或者是UTF-8的字符。这些方法如下：

##### （九)、1.2新的字符串操作方法

###### 1 截取一个字符串

```cpp
void GetStringRegion(JNIEnv *env,jstring str,jsize start,jsize len,jchar *buf)
```

在str(Unicode字符)从start位置开始截取len长度放置在buf中。如果越界，则抛出StringIndexOutOfBoundsException。

###### 2 截取一个字符串并将其转换为UTF-8格式

```cpp
void GetStringUTFRegion(JNIEnv *env,jstring str,jsize start ,jsize len,char *buf);
```

将str(Unicode字符串)从start位置开始截取len长度并且将其转换为UTF-8编码，然后将结果防止在buf中。

###### 3 截取一个字符串并将其转换为UTF-8格式

```cpp
const jchar * GetStringCritical(JNIEnv *env,jstring string,jboolean *isCopy);
void ReleaseStringCritical(JNIEnv *env,jstring string,cost jchar * carray);
```

上面这两个函数有点类似于`GetStringChars()`和`ReleaseStringChars()`功能。如果可能的话虚拟机会返回一个指向字符串元素的指针；否则，则返回一个复制的副本。

> PS：`GetStringChars()`和`ReleaseStringChars()`这里两个函数有很大的限制。在使用这两个函数时，这两个函数中间的代码不能调用任何让线程阻塞或者等待JVM的其他线程的本地函数或者JNI函数。有了这些限制，JVM就可以在本地方法持有一个从GetStringCritical得到的字符串的指指针时，禁止GC。当GC被禁止时，任何线程如果出发GC的话，都会被阻塞。而`GetStringChars()`和`ReleaseStringChars()`这两个函数中间的任何本地代码都不可以执行会导致阻塞的调用或者为新对象在JVM中分配内存。否则，JVM有可能死活，想象一下这样的场景：

- 1、只有当前线程触发的GC完成阻塞并释放GC时，由其他线程出发的GC才可能由阻塞中释放出来继续执行。
- 2、在这个过程中，当前线程会一直阻塞，因为任何阻塞性调用都需要获取一个正在被其他线程持有的锁，而其他线程正等待GC。
   `GetStringChars()`和`ReleaseStringChars()`的交替迭代调用是安全的，这种情况下，它们的使用必须有严格的顺序限制。而且，我们一定要记住检查是否因为内存溢出而导致它的返回值是NULL。因为JVM在执行`GetStringChars()`这个函数时，仍有发生数据复制的可能性，尤其是当JVM在内存存储的数组不连续时，为了返回一个指向连续内存空间的指针，JVM必须复制所有数据。
   **总之，为了避免死锁，在GetStringChars()`和`ReleaseStringChars()`之间不要调用任何JNI函数。**

#### 八、数组操作

##### (一)、获取数组的长度

```cpp
jsize GetArrayLength(JNIEnv *env,jarray array)
```

返回数组的长度

参数解释：

> - env：JNI接口指针
> - array：Java数组

返回：
 数组的长度

##### (二)、创建对象数组

```cpp
jobjectArray NewObjectArray(JNIEnv *env,jsize length,jclass elementClass, jobject initialElement);
```

创建一个新的对象数组，它的元素的类型是`elementClass`，并且所有元素的默认值是`initialElement`。

参数解释：

> - env：JNI接口指针
> - length：数组大小
> - elementClass：数组元素类
> - initialElement：数组元素的初始值

返回：
 Java数组对象，如果无法构造数组，则返回NULL

异常：
 如果内存不足，则抛出`OutOfMemoryError`

##### (三)、获取数组元中的某个元素

```cpp
jobject GetObjectArrayElement(JNIEnv *env,jobjectArray array,jsize index);
```

返回元素中某个位置的元素

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - index：数组下标

返回：
 Java对象

异常：
 如果index下标不是一个有效的下标，则会抛出`ArrayIndexOutOfBoundsException`

##### (四)、设置数组中某个元素的值

```cpp
void SetObjectArrayElement(JNIEnv *env,jobjectArray array,jsize index,jobject value);
```

设置下标为index元素的值。

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - index：数组下标
> - value：数组元素的新值

异常：
 如果index不是有效下标，则会抛出`ArrayIndexOutOfBoundsException`
 如果value不是元素类的子类，则会抛出`ArrayStoreException`

##### (五)、创建基本类型数组系列

```php
ArrayType New<PrimitiveType>Array(JNIEnv *env,jsize length);
```

用于构造基本类型数组对象的一系列操作。下面说明了特定基本类型数组的创建函数。可以把New<PrimitiveType>Array替换为某个实际的基本类型数组创建函数 ，然后将ArrayType替换为相应的数组类型

```php
New<PrimitiveType>Array Routines           Array Type
NewBooleanArray()                          jbooleanArray
NewByteArray()                             jbyteArray
NewCharArray()                             jcharArray
NewShortArray()                            jshortArray
NewIntArray()                              jintArray
NewLongArray()                             jlongArray
NewFloatArray()                            jfloatArray
NewDoubleArray()                           jdoubleArray
```

参数解释：

> - env：JNI接口指针
> - length：数组长度

返回：
 Java数组，如果无法创建该数组，则返回NULL。

##### (六)、获取基本类型数组的中数组指针系列

```cpp
NativeType * Get<PrimitiveType>ArrayElements(JNIEnv *env,ArrayType array,jboolean * isCopy);
```

一组返回类型是基本类型的数组指针。在调用相应的`Release<PrimitiveType>ArrayElements()`函数前将一直有效。由于返回的数组可能是Java数组的副本，因此，对返回数组的变更没有在基本类型中反应出来。除非了调用

一组返回基本类型数组体的函数。结果在调用相应的 Release<PrimitiveType>ArrayElements()函数前将一直有效。由于返回的数组可能是 Java 数组的副本，因此对返回数组的更改不必在基本类型数组中反映出来，直到调用``Release<PrimitiveType>ArrayElements()`函数。
 如果isCopy不是NULL，*isCopy在复制完成后即被设为JNI_TRUE。如果未复制，则设为JNI_FALSE。

> 下面说明了特定的基本类型数组元素的具体函数：
>
> - 将`Get<PrimitiveType>ArrayElements`替换为表中某个实际的基本> 类型的函数
> - 将ArrayType替换为对应的数组类型
> - 将NativeType替换为本地变量

不管布尔数组在Java虚拟机总如何表示，`GetBooleanArrayElements()`将始终返回一个`jboolean`类型的指针，其中每一个字节代表一个元素(开包表示)。内存中将确保所有其他类型的数组为连续的。

```dart
Get<PrimitiveType>ArrayElements Routines     Array Type         Native Type
GetBooleanArrayElements()                    jbooleanArray      jboolean
GetByteArrayElements()                       jbyteArray         jbyte
GetCharArrayElements()                       jcharArray         jchar
GetShortArrayElements()                      jshortArray        jshort
GetIntArrayElements()                        jintArray          jint
GetLongArrayElements()                       jlongArray         jlong
GetFloatArrayElements()                      jfloatArray        jfloat
GetDoubleArrayElements()                     jdoubleArray       jdouble
```

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - isCopy：指向布尔值的指针

返回：
 返回指向数组元素的指针，如果操作失败，则返回NULL

##### (七)、释放基本类型的数组系列

```cpp
void Release<PrimitiveType>ArrayElements(JNIEnv *env,ArrayType array,NativeType *elems,jint mode);
```

通知虚拟机Native不再访问数组的元素了。`elems`参数是使用相应的`Get <PrimitiveType> ArrayElements()`函数数组范返回的指针。如果有需要的话，该函数复制复制所有的elems上的变换到原始数组元素上去。mode参数提供了数组buffer应该怎么被释放。如果`elems`不是被array的一个副本，mode并没有什么影响。否则

果需要，该函数复制所有的在elems上的变换到原始的数组元素上去。
 mode参数提供了数组buffer应该怎样被释放。如果elems不是array的一个副本，mode并没有什么影响。

mode的取值 有如下3种情况：

- 0：复制内容并释放elems缓冲区
- JNI_COMMIT：复制内容但不释放elems缓冲区
- JNI_ABORT：释放缓冲区而不复制可能的更改

大多数情况下，程序员将“0”作为参数传递，因为这样可以确保固定和复制数组的一致行为。其他选项可以让程序员更好的控制内存。

> 下面说明了特定的基本类型数组元素的具体函数：
>
> - 将`Release <PrimitiveType> ArrayElements`替换下面中某个实际的基本> 类型的函数
> - 将ArrayType替换为对应的基本数组类型
> - 将NativeType替换为本地变量

下面描述了基本类型数组释放的详情。 您应该进行以下替换：

```dart
Release<PrimitiveType>ArrayElements Routines     Array Type               Native Type
ReleaseBooleanArrayElements()                    jbooleanArray            jboolean
ReleaseByteArrayElements()                       jbyteArray               jbyte
ReleaseCharArrayElements()                       jcharArray               jchar
ReleaseShortArrayElements()                      jshortArray              jshort
ReleaseIntArrayElements()                        jintArray                jint
ReleaseLongArrayElements()                       jlongArray               jlong
ReleaseFloatArrayElements()                      jfloatArray              jfloat
ReleaseDoubleArrayElements()                     jdoubleArray             jdouble
```

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - elems：指向基本类型的数组的指针
> - mode：释放模式

##### (八)、复制过去基本类型的数组系列

```cpp
void Get<PrimitiveType> ArrayRegion(JNIEnv *env,ArrayType array,jsize start,jsize len,NativeType *buf);
```

复制基本类型的数组给buff

> 下面说明了特定的基本类型数组元素的具体函数：
>
> - 将`Get<PrimitiveType> ArrayRegion`替换下面中某个实际的基本> 类型的函数
> - 将ArrayType替换为对应的基本数组类型
> - 将NativeType替换为本地变量

```dart
Get<PrimitiveType>ArrayRegion Routine           Array Type              Native Type
GetBooleanArrayRegion()                         jbooleanArray           jboolean
GetByteArrayRegion()                            jbyteArray              jbyte
GetCharArrayRegion()                            jcharArray              jchar
GetShortArrayRegion()                           jshortArray             jhort
GetIntArrayRegion()                             jintArray               jint
GetLongArrayRegion()                            jlongArray              jlong
GetFloatArrayRegion()                           jfloatArray             jloat
GetDoubleArrayRegion()                          jdoubleArray            jdouble
```

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - start：开始索引
> - len：需要复制的长度
> - buf：目标buffer

异常：
 如果索引无效，则抛出`ArrayIndexOutOfBoundsException`

##### (九)、把基本类型数组的数组复制回来系列

```cpp
void Set<PrimitiveType> ArrayRegion(JNIEnv *env,ArrayType array,jsize start,jsize len,const NativeType *buf);
```

主要是冲缓冲区复制基本类型的数组的函数

> 下面说明了特定的基本类型数组元素的具体函数：
>
> - 将`Set<PrimitiveType>ArrayRegion`替换下面中某个实际的基本> 类型的函数
> - 将ArrayType替换为对应的基本数组类型
> - 将NativeType替换为本地变量

```dart
Set<PrimitiveType>ArrayRegion Routine        Array Type            Native Type
SetBooleanArrayRegion()                      jbooleanArray         jboolean
SetByteArrayRegion()                         jbyteArray            jbyte
SetCharArrayRegion()                         jcharArray            jchar
SetShortArrayRegion()                        jshortArray           jshort
SetIntArrayRegion()                          jintArray             jint
SetLongArrayRegion()                         jlongArray            jlong
SetFloatArrayRegion()                        jfloatArray           jfloat 
SetDoubleArrayRegion()                       jdoubleArray          jdouble
```

参数解释：

> - env：JNI接口指针
> - array：Java数组
> - start：开始索引
> - len：需要复制的长度
> - buf：源buffer

异常：
 如果索引无效则会抛出ArrayIndexOutOfBoundsException

##### (十)、补充

> 从JDK/JER 1.1开始提供`Get/Release<primitivetype>ArrayElements`函数获取指向原始数组元素的指针。如果VM支持锁定，则返回指向原始数组的指针，否则，复制。
>  从JDK/JRE 1.3 开始引入新的功能即便VM不支持锁定，本地代码也可以获取数组元素的直接指针

```cpp
void *GetPrimitiveArrayCritical(JNIEnv *env,jarray array,jboolean *isCopy);
void ReleasePrimitiveArrayCritical(JNIEnv *env,jarray array,void *carray,jint mode);
```

虽然这两个函数与上面的`Get/Release <primitivetype> ArrayElements`函数很像，但是在使用这个功能的时候，还是有很多的限制。

###### 在调用`GetPrimitiveArrayCritical`之后，调用`ReleasePrimitiveArrayCritical`之前，这个区域是不能调用其他JNI函数，而且也不能调用任何可能导致线程阻塞病等待另一个Java线程的系统调用。

比如，当前线程不能调用read函数来读取，正在被其他所写入的stream。

#### 九 系统级别的操作

##### (一) 注册方法

```cpp
jint RegisterNatives(JNIEnv *env,jclass clazz,const JNINativeMethod *methods,jint nMethod);
```

根据clazz参数注册本地方法，methods参数制定JNINativeMethod结构数组，该数组包含本地方法的名字、签名及函数指针。其中名字及签名是指向编码为“UTF-8”的指针；nMethod参数表明数组中本地方法的个数。

这里说下`JNINativeMethod`这个结构体

```cpp
typedef struct { 
char *name; 
char *signature; 
void *fnPtr; 
} JNINativeMethod; 
```

参数解释：

> - env：JNI接口指针
> - clazz：Java类对象
> - methods：类中的native方法
> - nMethod：类中本地方法的个数

返回；
 成功返回0，失败返回负数

异常：
 如果没有找到指定的方法或者方法不是本地方法，则抛出NoSuchMethodError。

##### (二) 注销方法

```cpp
jint UnregisterNatives(JNIEnv *env,jclass clazz);
```

注销本地方法。类回收之前还没有被函数注册的状态。该函数一般不能再Native代码中被调用，它为特定的程序提供了一种重加载重链接本地库的方法。

参数解释：

> - JNI：接口指针
> - clazz：Java类对象

返回：
 注销成功返回0，失败返回负数

##### (三) 监视操作

```cpp
jint MonitorEnter(JNIEnv *env,jobject obj);
```

obj引用的底层Java对象关联的监视器。obj引用不能为空。每个Java对象都有一个相关的监视器。如果当前线程已经有关联到obj的监视器，它将添加监视器的计数器来表示这个线程进入监视器的次数。如果关联至obj的监视器不属于任何线程，那当前线程将变成该监视器的拥有者，并设置计数器为1，如果其他计数器已经拥有了这个监视器，当前线程将进行等待直到监视器被释放，然后再获得监视器的拥有权。

通过`MonitorEnter JNI`函数调用的监视器不能用`monitorexit`Java虚拟机指令或者同步方法退出。`MonitorEnter`JNI函数调用和`monitorenter` Java虚拟机指令可能用同样的对象竞争地进入监视器。

为了避免死锁，通过`MoniterEnter`JNI函数调用进入的监视器必须用`MonitorExit`JNI调用退出，除非`DetachCurrentThread`接口被隐式的调用来释放JNI监视器

参数解释：

> - env：JNI接口指针
> - obj：普通的Java对象或类对象

返回：
 成功返回0，失败返回负数

##### (四) 监视器退出

```cpp
jint MonitorExit(JNIEnv *env,jobject obj);
```

当前线程拥有与该obj关联的监视器，线程减少计数器的值来指示线程进入监视器的次数。如果计数器的值变为0，则线程释放该监视器。Native代码不能直接调用`MonitorExit`来释放监视器。而是应该通过同步方法来使用Java虚拟机指令来释放监视器

参数解释：

> - env：JNI接口指针
> - obj：普通的Java对象或类对象

返回：
 成功返回0，失败返回负数

异常：
 如果当前线程不拥有该监视器，则应该抛出IllegalMonitorStateException

#### 十 NIO操作

NIO相关操作允许Native代码直接访问`java.nio`的直接缓冲区。直接缓冲区的内容可能存在于普通的垃圾回收器以外的本地内存。有关直接缓冲区的信息，可以参考 NIO和java.nio.ByteBuffer类的规范。

在JDK/JRE 1.4中引入了新的JNI函数，允许检查和操作做直接缓冲区

> - NewDirectByteBuffer
> - GetDirectBufferAddress
> - GetDirectBufferCapacity

每个Java虚拟机的实现都必须支持这些功能，但并不是每个实现都需要支持对直接缓冲区的JNI访问。如果JVM不支持这种访问，那么`NewDirectByteBuffer`和`GetDirectBufferAddress`函数必须始终返回NULL，并且`GetDirectBufferCapacity`函数必须始终返回-1。如果JVM确实支持这种访问，那么必须实现这三个函数才能返回合适的值。

##### (一) 返回ByteBuffer

```cpp
jobject NewDirectByteBuffer(JNIEnv *env,void *address,jlong capacity);
```

分配并返回一个直接的`java.nio.ByteBuffer`内存块从内存地址`address`开始的`capacity`个字节.

调用这个函数并返回字节缓冲区的对象的Native代码必须保证缓冲区指向一个可靠的可被读写的内存区域。进入非法的内存位置有可能会返回任意数值，DNA不会有明显的印象，也有可能抛出异常。

参数解释：

> - env：JNIEnv接口指针
> - address：内存区域的起始地址
> - capacity：内存区域的大小

返回：
 返回一个新开辟的java.nio.ByteBuffer对象的本地引用。如果产生异常，则返回NULL。如果JVM不支持JNI访问直接缓冲区，也会返回NULL

异常：
 如果缓冲区分配失败，则返回OutOfMemoryError

##### (二) 返回直接缓冲区中对象的初始地址

```cpp
void* GetDirectBufferAddress(JNIEnv *env,jobject buf);
```

获取并返回java.nio.Buffer的内存初始地址

该函数允许Native代码通过直接缓冲区对象访问Java代码的同一内存区域

参数解释：

> - env：JNIEnv接口指针
> - buf：java.nio.Buffer对象

返回：
 返回内存区域的初始地址。如果内存区域未定义，返回NULL，如果给定的对象不是java.nio.buffer，则返回NULL，如果虚拟机不支持JNI访问，则返回NULL。

##### (三) 返回直接缓冲区中对象的内存容量

```cpp
jlong GetDirectBufferCapacity(JNIEnv *env,jobject buf);
```

获取并返回java.nio.Buffer的内存容量。该容量是内存区域可容纳的元素的个数

参数：

> - env：JNIEnv接口指针
> - buf：java.nio.Buffer对象

返回：
 返回内存区域的容量。如果指定的对象不是`java.nio.buffer`，则返回-1，或者如果对象是未对齐的view buffer且处理器架构不支持对齐访问。如果虚拟机不支持JNI访问则返回-1。

#### 十一、反射支持

如果程序员知道`方法`和`属性`的名称和类型，则直接使用JNI调用Java方法或者访问Java字段。Java核心反射API允许在运行时反射Java类。JNI提供了JNI中使用的字段和方法ID与`Java Core Reflection API`中使用的字段和方法对象之间的一组转换函数。

##### (一)、转化获取方法ID

```cpp
jmethodID FromReflectedMethod(JNIEnv *env,jobject method);
```

将java.lang.reflect.Method或者java.lang.reflect.Constructor对象转换为方法ID

参数解释：

> - env：JNIEnv接口指针
> - method：java.lang.reflect.Method或者java.lang.reflect.Constructor对象

返回：
 方法ID

##### (二)、转化获取属性ID

```cpp
jfield FromReflectedField(JNIEnv *env,jobject field);
```

将java.lang.reflect.Field转化域ID

参数解释：

> - env：JNIEnv接口指针
> - field：java.lang.reflect.Field对象

返回：
 域ID

##### (三)、反转化并获取方法对象

```cpp
jobject ToReflectedMethod(JNIEnv *env,jclass clazz,jmethodID methodID, jboolean isStatic);
```

将源自cls的方法ID转化为`java.lang.reflect.Method`或者`java.lang.reflect.Constructor`对象。如果方法ID指向一个静态属性，isStatic必须设置为JNI_TRUE，否则为JNI_FALSE。

参数解释：

> - env：JNIEnv接口指针
> - clazz：Java类对象
> - methodID：Java类对应的方法id
> - isStatic：是否是静态方法

返回
 对应Java层 `java.lang.reflect.Method`或者`java.lang.reflect.Constructor`对象。如果失败，则返回0

异常：
 如果内存不足，则抛出`OutOfMemoryError`。

##### (四)、反转化并获取属性对象

```undefined
jobject ToReflectedField(JNIEnv *env,jclass cls,jfieldID field,jboolean isStatic)
```

将来源于cls的属性ID转化为`java.lang.reflect.Field`对象。如果属性ID指向一个静态属性，`isStatic`必须设置为`JNI_TRUE`，否则为`JNI_FALSE`。

参数解释：

> - env：JNIEnv接口指针
> - cls：Java类对象
> - methodID：Java对应的属性ID
> - isStatic：是否是静态属性

返回：
 成功返回`java.lang.reflect.Field`对象，失败返回0

异常：
 如果内存不足，则抛出`OutOfMemoryError`

#### 十二、获取虚拟机

```cpp
jint GetJavaVM(JNIEnv *env,JavaVM **vm);
```

返回当前线程对应的java虚拟机接口。返回的结果保存在vm。

参数解释：

> - env：JNI接口指针
> - vm：保存虚拟机指针

返回：
 成功返回0，失败返回负数



### C++调用java示例：  

  那么现在介绍一下c/c++调用java方法的基本步骤：

#### 1.需要把java方法所在类的实例通过JNI方法传到c/c++

  java:JNI,   这是c需要回调的java方法，然后通过调用自身init()方法，把java实例传到c层  

```
class JNI {
  public native void init(JNI obj);
  public void error(int code) {
       Log.i(""JNI"", ""c++ call error  "");
  }
}
可以用javah 生成.h文件
```

 c:  这里把java传递进来的objListener，保存到c的jniobj结构体内。

```
#define TAG    "zy-jni-test" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
JNIEXPORT void JNICALL Java_com_zy_thread_JNI_init(JNIEnv *env,
      jobject oj,  jobject objListener){

        if(objListener == NULL){
           LOGD("objListener is null");
        }else{
          LOGD("get  java obj");
          jniobj->g_obj =  env->NewGlobalRef(objListener);
        }
}


typedef struct _tagJNIObj{//这个是刚才保存java实例的结构体，在还有其他参数
    jmethodID JNI_error;
    JavaVM* g_jvm;
    JNIEnv* g_ThreadEnv;
    jclass g_class;
    jobject g_obj;
}JNIObj;
static JNIObj* jniobj = NULL;
```

#### 2.在c层拿到java class

  c: 通过jni提供的FindClass方法和完整类名，可以拿到class引用

```
static const char* const DL_CLASS_NAME =  "com/zy/test/JNI";
jniobj->g_class = env->FindClass(DL_CLASS_NAME);
```

#### 3.在c层拿到java method

  c:根据刚才拿到的java class引用有jni提供的GetMethodID方法，和方法名，入参，就可以拿到method引用

```
// error
jniobj->JNI_error =  env->GetMethodID(jniobj->g_class, ""error"",
       ""(I)V"");
if(jniobj->JNI_error ==  MNull){
   MVLOG(""create  JNI_error is error"");
}
```

#### 4.调用method

  c:在需要调用的地方调用这个java方法

```
static void error(int code, void* pObj)
{
        LOGD("RtcMessageJNI   error is in code : %d", code );
        if(jniobj->g_ThreadEnv ==   NULL)
        {
            LOGD("attach   current thread start");
            jniobj->g_jvm ->AttachCurrentThread(&jniobj->g_ThreadEnv, NULL);

            if(jniobj->g_ThreadEnv ==   NULL){
                LOGD("attach   current thread is error");
                return;
            }
        }

        if(jniobj &&   jniobj->JNI_error){
              LOGD("RtcMessageJNI error is called");
            //这里是最关键的调用过程，通过JNI提供的CallVoidMethod，来调用，加入参数，class引用，method应用，已经入参，这样调用java方法就完成了。
              jniobj->g_ThreadEnv->CallVoidMethod(jniobj->g_obj,   jniobj->JNI_error, code);
        }

        if(jniobj->g_jvm){
              LOGD("RtcMessageJNI error method   detach");
              jniobj->g_jvm->DetachCurrentThread();
            jniobj->g_ThreadEnv =   NULL;
        }
}
```

  这里在调用java方法的时候调用了，AttachCurrentThread和DetachCurrentThread方法，这是必须的，如果不调用AttachCurrentThread就拿不到线程的引用，会报错误。然后在调用结束的时候要调用DetachCurrentThread，也就是释放线程。最好每次调用java方法结束的时候都调用DetachCurrentThread，这样基本不会出错。

#### 5.编译

```
Android.mk:
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
#必须放到CLEAR_VARS后，放在其他位置可能编译报错
LOCAL_LDLIBS :=-llog

LOCAL_MODULE := JNISample
LOCAL_SRC_FILES := com_zy_thread_JNI.cpp

include $(BUILD_SHARED_LIBRARY)
```



参考文档：

1.[JNI的常用方法的中文API](https://www.jianshu.com/p/67081d9b0a9c)

2.https://www.jianshu.com/p/b71aeb4ed13d

