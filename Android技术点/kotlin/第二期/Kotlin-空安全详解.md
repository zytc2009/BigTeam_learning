# Kotlin-空安全详解

[TOC]

## 前言

### Null


对于Java程序员来说，null是令人头痛的东西。时常会受到空指针异常（NullPointerException）的骚扰。相信很多程序员都特别害怕出现程序中出现NPE，因为这种异常往往伴随着代码的非预期运行。

在Java 1 中就包含了了Null引用和NPE了，但是其实，Null引用是伟大的计算机科学家Tony Hoare 早在1965年发明的，最初作为编程语言ALGOL W的一部分。

> 1965年，英国一位名为Tony Hoare的计算机科学家在设计ALGOL W语言时提出了null引用的想法。ALGOL W是第一批在堆上分配记录的类型语言之一。Hoare选择null引用这种方式，“只是因为这种方法实现起来非常容易”。虽然他的设计初衷就是要“通过编译器的自动检测机制，确保所有使用引用的地方都是绝对安全的”，他还是决定为null引用开个绿灯，因为他认为这是为“不存在的值”建模最容易的方式。

但是在2009年，很多年后，他开始为自己曾经做过这样的决定而后悔不已，把它称为“一个价值十亿美元的错误”。

实际上，Hoare的这段话低估了过去五十年来数百万程序员为修复空引用所耗费的代价。因为在ALGOL W之后出现的大多数现代程序设计语言，包括Java，都采用了同样的设计方式，其原因是为了与更老的语言保持兼容，或者就像Hoare曾经陈述的那样，“仅仅是因为这样实现起来更加容易”。

相信很多Java程序员都一样对null和NPE深恶痛绝，因为他确实会带来各种各样的问题（来自《Java 8 实战》）。如：

- 它是错误之源。NullPointerException是目前Java程序开发中最典型的异常。它会使你的代码膨胀。
- 它让你的代码充斥着深度嵌套的null检查，代码的可读性糟糕透顶。
- 它自身是毫无意义的。null自身没有任何的语义，尤其是是它代表的是在静态类型语言中以一种错误的方式对缺失变量值的建模。
- 它破坏了Java的哲学。Java一直试图避免让程序员意识到指针的存在，唯一的例外是:null指针。
- 它在Java的类型系统上开了个口子。null并不属于任何类型，这意味着它可以被赋值给任意引用类型的变量。这会导致问题， 原因是当这个变量被传递到系统中的另一个部分后，你将无法获知这个null变量最初赋值到底是什么类型。


#### 其他语言如何解决NPE问题

我们知道，出了Java语言外，还有很多其他的面向对象语言，那么在其他的一些语言中，是如何解决NPE的问题的呢？

##### Groovy

如在  [Groovy](http://www.groovy-lang.org/) 中使用安全导航操作符（Safe Navigation Operator）可以访问可能为null的变量：

```groovy
def carInsuranceName = person?.car?.insurance?.name
```

Groovy的安全导航操作符能够避免在访问这些可能为null引用的变量时发生NullPointerException，在调用链中的变量遭遇null时将null引用沿着调用链传递下去，返回一个null。


##### Java

一直以来Java 对于null和NPE的改进还是做出了一些努力的。


首先在Java 8中提供了 Optional，其实在Java 8 推出之前，Google的 [Guava](https://github.com/google/guava) 库中就率先提供过Optional接口来使null快速失败。



Optional在可能为null的对象上做了一层封装，Optional对象包含了一些方法来显式地处理某个值是存在还是缺失，Optional类强制你思考值不存在的情况，这样就能避免潜在的空指针异常。



但是设计Optional类的目的并不是完全取代null，它的目的是设计更易理解的API。通过Optional，可以从方法签名就知道这个函数有可能返回一个缺失的值，这样强制你处理这些缺失值的情况。



关于Optional的用法，不是本文的重点，就不在这里详细介绍了，笔者在日常开发中经常结合Stream一起使用Optional，还是比较好用的。

###### JDK 14

另外一个值得一提的就是最近（2020年03月17日）发布的JDK 14中对于NPE有了一个增强。那就是JEP 358: Helpful NullPointerExceptions


JDK 14中对于NEP有了一个增强，既然NPE暂时无法避免，那么就让他对开发者更有帮助一些。

![snip20200330_8.png](http://note.youdao.com/yws/res/12552/WEBRESOURCEa3d59a454b761bdd1533832d3b5434ad)


每个Java开发人员都遇到过NullPointerException (NPE)。由于NPE可以发生在程序的几乎任何地方，试图捕获并从它们中恢复通常是不切实际的。因此，开发人员通常依赖于JVM来确定NPE实际发生时的来源。例如，假设在这段代码中出现了一个NPE:

```java
a.i = 99;
```

JVM将打印出导致NPE的方法、文件名和行号:

```java
Exception in thread "main" java.lang.NullPointerException
at Prog.main(Prog.java:5)
```

通过以上堆栈信息，开发人员可以定位到a.i= 99这一行，并推断出a一定是null。


但是，对于更复杂的代码，如果不使用调试器，就不可能确定哪个变量是null。假设在这段代码中出现了一个NPE:

```java
a.b.c.i = 99;
```

我们根本无法确定到底是a还是b或者是c在运行时是个null值。


但是，在JDK14以后，这种窘境就有解了。


在JDK14中，当运行期，试图对一个null对象进行应用时，JVM依然会抛出一个NullPointerException (NPE)，除此之外，还会通过通过分析程序的字节码指令，JVM将精确地确定哪个变量是null，并且在堆栈信息中明确的提示出来。


在JDK 14中，如果上文中的a.i = 99发生NPE，将会打印如下堆栈：

```java
Exception in thread "main" java.lang.NullPointerException:
•   Cannot assign field "i" because "a" is null
•   at Prog.main(Prog.java:5)
```

如果是a.b.c.i = 99;中的b为null导致了空指针，则会打印以下堆栈信息：

```java
Exception in thread "main" java.lang.NullPointerException:
•   Cannot read field "c" because "a.b" is null
•   at Prog.main(Prog.java:5)
```

可见，堆栈中明确指出了到底是哪个对象为null而导致了NPE，这样，一旦应用中发生NPE，开发者可以通过堆栈信息第一时间定位到到底是代码中的那个对象为null导致的。

这算是JDK的一个小小的改进，但是这个改进对于开发者来说确实是非常友好的。


## Kotlin 空安全

前面我们对 null 的历史缘由、以及各个编程语言中对 null 的处理做了一个简单了解、下面我们着重了解一下kotlin 是如何做到 null 安全的。

### 简单使用

当某个变量的值可以为 null 的时候，必须在声明处的类型后添加 `?` 来标识该引用可为空。

如果 `str` 的内容不是数字返回 null：

```kotlin
fun parseInt(str: String): Int? {
    // ……
}
```

使用返回可空值的函数:

```kotlin
fun parseInt(str: String): Int? {
    return str.toIntOrNull()
}

fun printProduct(arg1: String, arg2: String) {
    val x = parseInt(arg1)
    val y = parseInt(arg2)

    // 直接使用 `x * y` 会导致编译错误，因为它们可能为 null
    if (x != null && y != null) {
        // 在空检测后，x 与 y 会自动转换为非空值（non-nullable）
        println(x * y)
    }
    else {
        println("'$arg1' or '$arg2' is not a number")
    }    
}

fun main() {
    printProduct("6", "7")
    printProduct("a", "7")
    printProduct("a", "b")
}
```


或者


```kotlin
fun parseInt(str: String): Int? {
    return str.toIntOrNull()
}

fun printProduct(arg1: String, arg2: String) {
    val x = parseInt(arg1)
    val y = parseInt(arg2)
    
    // ……
    if (x == null) {
        println("Wrong number format in arg1: '$arg1'")
        return
    }
    if (y == null) {
        println("Wrong number format in arg2: '$arg2'")
        return
    }

    // 在空检测后，x 与 y 会自动转换为非空值
    println(x * y)
}

fun main() {
    printProduct("6", "7")
    printProduct("a", "7")
    printProduct("99", "b")
}
```



### 可空类型与非空类型

Kotlin 的类型系统旨在从我们的代码中消除 `NullPointerException`。NPE 的唯一可能的原因可能是：

* 显式调用 `throw NullPointerException()`
* 使用了下文描述的 `!!` 操作符；
* 有些数据在初始化时不一致，例如当：
  * 传递一个在构造函数中出现的未初始化的 this 并用于其他地方（“泄漏 *this
  超类的构造函数调用一个开放成员 该成员在派生中类的实现使用了未初始化的状态

* Java 互操作：

  * 企图访问 平台类型的 `null` 引用的成员；
  * 用于具有错误可空性的 Java 互操作的泛型类型，例如一段 Java 代码可能会向 Kotlin 的 `MutableList<String>` 中加入 `null`，这意味着应该使用 `MutableList<String?>` 来处理它；
  * 由外部 Java 代码引发的其他问题。
  

关于平台类型：请参考 《kotlin-中调用Java详解》

在 Kotlin 中，类型系统区分一个引用可以容纳 null 还是不能容纳非空引用

例如，String 类型的常规变量不能容纳 null


```kotlin
var a: String = "abc"
a = null // 编译错误 不能显示赋 null 
```


如果要允许为空，我们可以声明一个变量为可空字符串，写作 `String?`：


```kotlin
var str : String? = "abc"
str = null
println("str = $str") // null
```


现在，如果你调用 `a` 的方法或者访问它的属性，它保证不会导致 `NPE`，这样你就可以放心地使用：


```kotlin
val l = a.length
println("l = $l")
```


但是如果你想访问 `str` 的同一个属性，那么这是不安全的，并且编译器会报告一个错误：


```kotlin
val c = str.length  // 编译报错 错误：变量“str”可能为空
```

但是我们还是需要访问该属性，对吧？有几种方式可以做到。

### 在条件中判断 null

首先，你可以显式检测 `str` 是否为 null，并分别处理两种可能：


```kotlin
// 显示判断
val d = if (str != null) str.length else -1
```

### 安全的调用

你的第二个选择是安全调用操作符，写作 `?.`：


```kotlin
val k = "Kotlin"
val p: String? = null
println("k = ${k?.length}") // 无需安全调用
println("p = ${p?.length}") 
```


如果 `k` 非空，就返回 `k.length`，否则返回 null，这个表达式的类型是 `Int?`。

安全调用在链式调用中很有用。

例如：

```kotlin
class School{
    var classData : ClassData? = null
}

class ClassData{
    var user : User? = null
}

class User{
    var name : String? = null
}
```

我们有三个类 分别模拟 学校信息、班级信息、学生信息 如果我们要取 User 类中 name的长度、那么分别使用 Java 和 kotlin 会怎么写呢？

Java 版本

```java
// Java 连缀调用
int userNameLength = school.getClassData().getUser().getName().length();
```
如果感觉上述 代码不够安全可能有人会这样写：

```java
// java 判断处理
if (school != null && school.getClassData() != null && school.getClassData().getUser() != null){
    int userNameLength = school.getClassData().getUser().getName().length();
}
```
那么使用 Kotlin 怎么写呢？

Kotlin 版本

```kotlin
val userNameLength = school?.classData?.user?.name?.length
```


如果任意一个属性（环节）为空，这个链式调用就会返回 null

如果要只对非空值执行某个操作，安全调用操作符可以与 [`let`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/let.html) 一起使用：


```kotlin
// 使用 let 函数
val list = listOf("java","kotlin",null,"c++")
for (li in list){
    li?.let { println(li) }
}
// java kotlin c++ 忽略 null
```


安全调用也可以出现在赋值的左侧。这样，如果调用链中的任何一个接收者为空都会跳过赋值，而右侧的表达式根本不会求值：

我们拿上述例子给 `User`类中 `name` 赋值为例：
```kotlin
// 如果 `school` 或者 `classData` 、`user` 其中之一为空，都不会调用该函数：
// 赋值时使用 安全调用
school?.classData?.user?.name = "小明"
```

### Elvis 操作符

当我们有一个可空的引用 `r` 时，我们可以说“如果 `r` 非空，我使用它；否则使用某个非空的值 `x`”：这种可以提供默认选项的操作 可以利用 kotlin 的 Elvis 操作符来实现比较简单。

if else 操作符

```kotlin
val b : String? = null
val l: Int = if (b != null) b.length else -1
```


除了完整的 *if*{}-表达式，这还可以通过 Elvis 操作符表达，写作 `?:`：


```kotlin
val j = b?.length ?: -1
```

如果 `?:` 左侧表达式非空，elvis 操作符就返回其左侧表达式，否则返回右侧表达式。
请注意，当且仅当左侧为空时，才会对右侧表达式求值。

当然 `?:`右边也是可以调用一个函数的。

```kotlin
// 使用 ?: Elvis 表达式
val j = b?.length ?: getDefLength()
println(" j = $j")

fun getDefLength():Int{
    return -4
}
```

因为 `throw` 和 `return` 在 Kotlin 中都是表达式，所以它们也可以用在
 elvis 操作符右侧。这可能会非常方便，例如，检测函数参数：


```kotlin
fun renElvis():String?{
    val user :User? = null
    val parent = user?.name ?: return null
    val name = user.name ?: throw IllegalArgumentException("name expected")
    return null
}
```

### `!!` 操作符

第三种选择是为 NPE 爱好者准备的：非空断言运算符（`!!`）将任何值转换为非空<!--
-->类型，若该值为空则抛出异常。

我们可以写 `str!!` ，这会返回一个非空的 `str` 值
（例如：在我们例子中的 `String`）或者如果 `str` 为空，就会抛出一个 `NPE` 异常：


```kotlin
val l = str!!.length
```


因此，这种情况如果 `str` 为 `null` 的还是会抛出 NPE的

```kotlin
Exception in thread "main" kotlin.KotlinNullPointerException
```

### 安全的类型转换

如果对象不是目标类型，那么常规类型转换可能会导致 `ClassCastException`。
另一个选择是使用安全的类型转换，如果尝试转换不成功则返回 `null`

所以 kotlin 使用 `as?` 进行类型转换

```kotlin
var a: String = "abc"
val aInt: Int? = a as? Int
```


### 可空类型的集合

如果你有一个可空类型元素的集合，并且想要过滤非空元素，你可以使用 `filterNotNull` 来实现：


```kotlin
val nullableList: List<Int?> = listOf(1, 2, null, 4)
val intList: List<Int> = nullableList.filterNotNull()
```




## 参考

《Java 8 In Action》

http://www.groovy-lang.org/

https://openjdk.java.net/jeps/358

https://bugs.openjdk.java.net/browse/JDK-8220715

https://mp.weixin.qq.com/s/O5sIYVdpyIp-BoMn8bitaA

https://github.com/google/guava

https://en.wikipedia.org/wiki/Tony_Hoare#Apologies_and_retractions







