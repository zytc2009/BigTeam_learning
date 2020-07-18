[Toc]

### NDK与JNI基础

### 什么是NDK

NDK 其中NDK的全拼是：Native Develop Kit。是一套允许您使用原生代码语言(例如C和C++) 实现部分应用的工具集。在开发某些类型应用时，这有助于您重复使用以这些语言编写的代码库。

### 为什么使用NDK

1、在平台之间移植其应用

2、重复使用现在库，或者提供其自己的库重复使用

3、在某些情况下提性能，特别是像游戏这种计算密集型应用

4、使用第三方库，现在许多第三方库都是由C/C++库编写的，比如Ffmpeg这样库。

5、不依赖于Dalvik Java虚拟机的设计

6、代码的保护。由于APK的Java层代码很容易被反编译，而C/C++库反编译难度大。

### NDK到so

![](..\images\ndk到so.png)

目前Android系统支持以下七种不用的CPU架构，每一种对应着各自的应用程序二进制接口ABI：

> ARMv5——armeabi
>
> ARMv7 ——armeabi-v7a
>
> ARMv8——arm64- v8a
>
> x86——x86
>
> MIPS ——mips
>
> MIPS64——mips64
>
> x86_64——x86_64

### JNI

#### 什么是JNI

JNI，全称为Java Native Interface，即Java本地接口，JNI是Java调用Native 语言的一种特性。通过JNI可以使得Java与C/C++机型交互。

> 开发JNI程序会受到系统环境限制，因为用C/C++ 语言写出来的代码或模块，编译过程当中要依赖当前操作系统环境所提供的一些库函数，并和本地库链接在一起。而且编译后生成的二进制代码只能在本地操作系统环境下运行，因为不同的操作系统环境，有自己的本地库和CPU指令集，而且各个平台对标准C/C++的规范和标准库函数实现方式也有所区别。这就造成了各个平台使用JNI接口的Java程序，不再像以前那样自由的跨平台。如果要实现跨平台， 就必须将本地代码在不同的操作系统平台下编译出相应的动态库。



JNI下一共涉及到三个角色：C/C++代码、本地方法接口类、Java层中具体业务类。

#### JNI的命名规则

```undefined
JNIExport jstring JNICALL Java_com_zy_test_MainActivity_testJNI( JNIEnv* env,jobject thiz ) 
```

**`jstring`** 是**返回值类型**
 **`Java_com_zy_test`** 是**包名**
 **`MainActivity`** 是**类名**
 **`stringFromJNI`** 是**方法名**

其中**`JNIExport`**和**`JNICALL`**是不固定保留的关键字不要修改

#### 如何实现JNI

> 第1步：在Java中先声明一个native方法
>
> 第2步：编译Java源文件javac得到.class文件
>
> 第3步：通过javah -jni命令导出JNI的.h头文件
>
> 第4步：使用Java需要交互的本地代码，实现在Java中声明的Native方法（如果Java需要与C++交互，那么就用C++实现Java的Native方法。）
>
> 第5步：将本地代码编译成动态库(Windows系统下是.dll文件，如果是Linux系统下是.so文件，如果是Mac系统下是.jnilib)
>
> 第6步：通过Java命令执行Java程序，最终实现Java调用本地代码。

PS：javah 是JDK自带的一个命令，-jni参数表示将class 中用到native 声明的函数生成JNI 规则的函数

####  JNI结构

![](..\images\jni结构.png)

这张JNI函数表的组成就像C++的虚函数表。虚拟机可以运行多张函数表，举例来说，一张调试函数表，另一张是调用函数表。JNI接口指针仅在当前线程中起作用。这意味着指针不能从一个线程进入另一个线程。然而，可以在不同的咸亨中调用本地方法。

![](..\images\jni接口.png)

示例：

```rust
jdouble Java_pkg_Cls_test(JNIEnv *env, jobject obj, jint i, jstring s)
{
     const char *str = (*env)->GetStringUTFChars(env, s, 0); 
     (*env)->ReleaseStringUTFChars(env, s, str); 
     return 10;
}
```

里面的方法有三个入参，我们就依次来看下：

> - *env：一个接口指针
> - obj：在本地方法中声明的对象引用
> - i和s：用于传递的参数

关于obj、i和s的类型大家可以参考下面的JNI数据类型，JNI有自己的原始数据类型和数据引用类型如下：

![](..\images\JNI的原始数据类型.png)

#### JNI原理

Java语言的执行环境是Java虚拟机(JVM)，JVM其实是主机环境中的一个进程，每个JVM虚拟机都在本地环境中有一个JavaVM结构体，该结构体在创建Java虚拟机时被返回，在JNI环境中创建JVM的函数为**JNI_CreateJavaVM。**

```cpp
JNI_CreateJavaVM(JavaVM **pvm, void **penv, void*args);
```

##### 1、JavaVM

其中JavaVM是Java虚拟机在JNI层的代表，JNI全局仅仅有一个JavaVM结构中封装了一些函数指针（或叫函数表结构），JavaVM中封装的这些函数指针主要是对JVM操作接口。

##### 2.JNIEnv

JNIEnv是当前Java线程的执行环境，一个JVM对应一个JavaVM结构，而一个JVM中可能创建多个Java线程，每个线程对应一个JNIEnv结构，它们保存在线程本地存储TLS中。因此，不同的线程的JNIEnv是不同，也不能相互共享使用。JNIEnv结构也是一个函数表，在本地代码中通过JNIEnv的函数表来操作Java数据或者调用Java方法。也就是说，只要在本地代码中拿到了JNIEnv结构，就可以在本地代码中调用Java代码。

###### 2.1JNIEnv是什么？

> JNIEnv是一个线程相关的结构体，该结构体代表了Java在本线程的执行环境

###### 2.2、JNIEnv和JavaVM的区别：

> - JavaVM：JavaVM是Java虚拟机在JNI层的代表，JNI全局仅仅有一个
> - JNIEnv：JavaVM 在线程中的代码，每个线程都有一个，JNI可能有非常多个JNIEnv；

###### 2.3、JNIEnv的作用：

> - 调用Java 函数：JNIEnv代表了Java执行环境，能够使用JNIEnv调用Java中的代码
> - 操作Java代码：Java对象传入JNI层就是jobject对象，需要使用JNIEnv来操作这个Java对象

###### 2.4、JNIEnv的创建与释放

**2.4.1、JNIEnv的创建**

JNIEnv 创建与释放：从JavaVM获得，这里面又分为C与C++，我们就依次来看下：

> - C 中——**JNIInvokeInterface**：JNIInvokeInterface是C语言环境中的JavaVM结构体，调用 (*AttachCurrentThread)(JavaVM*, JNIEnv**, void*) 方法，能够获得JNIEnv结构体
> - C++中 ——**_JavaVM**：_JavaVM是C++中JavaVM结构体，调用jint AttachCurrentThread(JNIEnv** p_env, void* thr_args) 方法，能够获取JNIEnv结构体；

**2.4.2、JNIEnv的释放**

> - C 中释放：调用JavaVM结构体JNIInvokeInterface中的(*DetachCurrentThread)(JavaVM*)方法，能够释放本线程的JNIEnv
> - C++ 中释放：调用JavaVM结构体_JavaVM中的jint DetachCurrentThread(){ return functions->DetachCurrentThread(this); } 方法，就可以释放 本线程的JNIEnv

###### 2.5、JNIEnv与线程

JNIEnv是线程相关的，即在每一个线程中都有一个JNIEnv指针，每个JNIEnv都是线程专有的，其他线程不能使用本线程中的JNIEnv，即线程A不能调用线程B的JNIEnv。所以JNIEnv不能跨线程。

> - JNIEnv只在当前线程有效：JNIEnv仅仅在当前线程有效，JNIEnv不能在线程之间进行传递，在同一个线程中，多次调用JNI层方便，传入的JNIEnv是同样的
> - 本地方法匹配多个JNIEnv：在Java层定义的本地方法，能够在不同的线程调用，因此能够接受不同的JNIEnv

###### 2.6、JNIEnv结构

JNIEnv是一个指针，指向一个线程相关的结构，线程相关结构，线程相关结构指向JNI函数指针数组，这个数组中存放了大量的JNI函数指针，这些指针指向了详细的JNI函数。

![img](..\images\JNIEnv结构.png)

###### 2.7、与JNIEnv相关的常用函数

**2.7.1 创建Java中的对象**

> - jobject NewObject(JNIEnv *env, jclass clazz,jmethodID methodID, ...)：
> - jobject NewObjectA(JNIEnv *env, jclass clazz,jmethodID methodID, const jvalue *args)：
> - jobject NewObjectV(JNIEnv *env, jclass clazz,jmethodID methodID, va_list args)：

第一个参数jclass class  代表的你要创建哪个类的对象，第二个参数,jmethodID methodID代表你要使用那个构造方法ID来创建这个对象。只要有jclass和jmethodID，我们就可以在本地方法创建这个Java类的对象。

**2.7.2 创建Java类中的String对象**

> - jstring NewString(JNIEnv *env, const jchar *unicodeChars,jsize len)：

通过Unicode字符的数组来创建一个新的String对象。
 env是JNI接口指针；unicodeChars是指向Unicode字符串的指针；len是Unicode字符串的长度。返回值是Java字符串对象，如果无法构造该字符串，则为null。

> 那有没有一个直接直接new一个utf-8的字符串的方法呢？答案是有的，就是`jstring NewStringUTF(JNIEnv *env, const char *bytes)`这个方法就是直接new一个编码为utf-8的字符串。

**2.7.3 创建类型为基本类型PrimitiveType的数组**

> - ArrayType New<PrimitiveType>Array(JNIEnv *env, jsize length);
>    指定一个长度然后返回相应的Java基本类型的数组

| 方法                             |    返回值     |
| -------------------------------- | :-----------: |
| New<PrimitiveType>Array Routines |  Array Type   |
| NewBooleanArray()                | jbooleanArray |
| NewByteArray()                   |  jbyteArray   |
| NewCharArray()                   |  jcharArray   |
| NewShortArray()                  |  jshortArray  |
| NewIntArray()                    |   jintArray   |
| NewLongArray()                   |  jlongArray   |
| NewFloatArray()                  |  jfloatArray  |
| NewDoubleArray()                 | jdoubleArray  |

用于构造一个新的数组对象，类型是原始类型。基本的原始类型如下：

| 方法                             |    返回值     |
| -------------------------------- | :-----------: |
| New<PrimitiveType>Array Routines |  Array Type   |
| NewBooleanArray()                | jbooleanArray |
| NewByteArray()                   |  jbyteArray   |
| NewCharArray()                   |  jcharArray   |
| NewShortArray()                  |  jshortArray  |
| NewIntArray()                    |   jintArray   |
| NewLongArray()                   |  jlongArray   |
| NewFloatArray()                  |  jfloatArray  |
| NewDoubleArray()                 | jdoubleArray  |

**2.7.4 创建类型为elementClass的数组**

> - jobjectArray NewObjectArray(JNIEnv *env, jsize length,
>    jclass elementClass, jobject initialElement);

造一个新的数据组，类型是elementClass，所有类型都被初始化为initialElement。

**2.7.5 获取数组中某个位置的元素**

> jobject GetObjectArrayElement(JNIEnv *env,
>  jobjectArray array, jsize index);

返回Object数组的一个元素

**2.7.6 获取数组的长度**

> jsize GetArrayLength(JNIEnv *env, jarray array);

获取array数组的长度.



#### JNI的引用

> Java内存管理这块是完全透明的，new一个实例时，只知道创建这个类的实例后，会返回这个实例的一个引用，然后拿着这个引用去访问它的成员(属性、方法)，完全不用管JVM内部是怎么实现的，如何为新建的对象申请内存，使用完之后如何释放内存，只需要知道有个垃圾回收器在处理这些事情就行了，然而，从Java虚拟机创建的对象传到C/C++代码就会产生引用，根据Java的垃圾回收机制，只要有引用存在就不会触发该该引用所指向Java对象的垃圾回收。

在JNI规范中定义了三种引用：局部引用（Local Reference）、全局引用（Global Reference）、弱全局引用（Weak Global Reference）。

在JNI中也同样定义了类似与Java的应用类型，在JNI中，定义了三种引用类型：

> - 局部引用(Local Reference)
> - 全局引用(Global Reference)
> - 弱全局引用(Weak Global Reference)

下面我们就依次来看下：

##### 1、局部引用(Local Reference)

> 局部引用，也叫本地引用，通常是在函数中创建并使用。会阻止GC回收所有引用对象。

最常见的引用类型，基本上通过JNI返回来的引用都是局部引用，例如使用NewObject，就会返回创建出来的实例的局部引用，局部引用值在该native函数有效，所有在该函数中产生的局部引用，都会在函数返回的时候自动释放(freed)，也可以使用DeleteLocalRef函数手动释放该应用。之所以使用DeleteLocalRef函数：实际上局部引用存在，就会防止其指向对象被垃圾回收期回收，尤其是当一个局部变量引用指向一个很庞大的对象，或是在一个循环中生成一个局部引用，最好的做法就是在使用完该对象后，或在该循环尾部把这个引用是释放掉，以确保在垃圾回收器被触发的时候被回收。在局部引用的有效期中，可以传递到别的本地函数中，要强调的是它的有效期仍然只是在第一次的Java本地函数调用中，所以千万不能用C++全部变量保存它或是把它定义为C++静态局部变量。

##### 2、全局引用(Global Reference)

> 全局引用可以跨方法、跨线程使用，直到被开发者显式释放。类似局部引用，一个全局引用在被释放前保证引用对象不被GC回收。和局部应用不同的是，没有俺么多函数能够创建全局引用。能创建全部引用的函数只有NewGlobalRef，而释放它需要使用ReleaseGlobalRef函数

##### 3、弱全局引用(Weak Global Reference)

> 是JDK 1.2 新增加的功能，与全局引用类似，创建跟删除都需要由编程人员来进行，这种引用与全局引用一样可以在多个本地带阿妈有效，不一样的是，弱引用将不会阻止垃圾回收期回收这个引用所指向的对象，所以在使用时需要多加小心，它所引用的对象可能是不存在的或者已经被回收。

通过使用NewWeakGlobalRef、ReleaseWeakGlobalRef来产生和解除引用。

##### 4、引用比较

在给定两个引用，不管是什么引用，我们只需要调用IsSameObject函数来判断他们是否是指向相同的对象。代码如下：

```php
(*env)->IsSameObject(env, obj1, obj2)
```

如果obj1和obj2指向相同的对象，则返回**JNI_TRUE(或者1)**，否则返回**JNI_FALSE(或者0)**,

> PS：有一个特殊的引用需要注意：NULL，JNI中的NULL引用指向JVM中的null对象，如果obj是一个全局或者局部引用，使用`(*env)->IsSameObject(env, obj, NULL)`或者`obj == NULL`用来判断obj是否指向一个null对象即可。但是需要注意的是，`IsSameObject`用于弱全局引用与NULL比较时，返回值的意义是不同于局部引用和全局引用的。代码如下：

```php
jobject local_obj_ref = (*env)->NewObject(env, xxx_cls,xxx_mid);
jobject g_obj_ref = (*env)->NewWeakGlobalRef(env, local_ref);
// ... 业务逻辑处理
jboolean isEqual = (*env)->IsSameObject(env, g_obj_ref, NULL);
```



相关文章：

https://www.jianshu.com/p/87ce6f565d37
