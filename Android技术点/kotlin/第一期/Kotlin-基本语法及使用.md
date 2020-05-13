# 二、Kotlin-基本语法及使用

[TOC]

## 包的定义和导入

包的声明应处于源文件顶部：

```
package com.app.kotlin.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.text.*
```

关于 包的详细内容 请参考 《Kotlin-导包和使用》

## Main 函数


Kotlin 应用程序的入口点是 `main` 函数。


```kotlin
// 主函数 无入参
fun main(){
    println("Hello Kotlin")
}
// 主函数 有入参
fun main(args : Array<String>){
    println("Hello Kotlin")
}
```

Java 的 ``main`` 函数
```java
public static void main(String[] args){
    System.out.println("Hello Java");
}
```

## 函数

带有两个 `Int` 参数、返回 `Int` 的函数：


```kotlin

fun sum(a: Int, b: Int): Int {
    return a + b
}


fun main() {
    print("sum of 3 and 5 is ")
    println(sum(3, 5))
}
```


将表达式作为函数体、返回值类型自动推断的函数：


```kotlin

fun sum(a: Int, b: Int) = a + b


fun main() {
    println("sum of 19 and 23 is ${sum(19, 23)}")
}
```


函数返回无意义的值：


```kotlin

fun printSum(a: Int, b: Int): Unit {
    println("sum of $a and $b is ${a + b}")
}


fun main() {
    printSum(-1, 8)
}
```


`Unit` 返回类型可以省略：


```kotlin

fun printSum(a: Int, b: Int) {
    println("sum of $a and $b is ${a + b}")
}


fun main() {
    printSum(-1, 8)
}
```

关于更多函数使用请参考 《Kotlin-函数详解》


## 变量

kotlin 的类型定义属于自动推导型根据 数值 进行确定类型、而 Java 属于静态类型 定义变量时必须指定类型。

定义**只读**局部变量使用关键字 `val` 定义。只能为其赋值一次。



```kotlin
fun main() {

    val a: Int = 1  // 立即赋值
    val b = 2   // 自动推断出 `Int` 类型
    val c: Int  // 如果没有初始值类型不能省略
    c = 3       // 明确赋值

    println("a = $a, b = $b, c = $c")
}
```



可重新赋值的变量使用 `var` 关键字： 



```kotlin
fun main() {

    var x = 5 // 自动推断出 `Int` 类型
    x += 1

    println("x = $x")
}
```



顶层变量：


```kotlin

val PI = 3.14
var x = 0

fun incrementX() { 
    x += 1 
}


fun main() {
    println("x = $x; PI = $PI")
    incrementX()
    println("incrementX()")
    println("x = $x; PI = $PI")
}
```

辅助理解：

- val 可以理解为 Java中的 final 关键字作用
- var 就是普通的变量定义


更多变量详解 请参考《Kotlin-属性与字段详解》


## 注释

与大多数现代语言一样，Kotlin 支持单行（或*行末*）与多行（*块*）注释。


```kotlin
// 这是一个行注释

/* 这是一个多行的
   块注释。 */
```



Kotlin 中的块注释可以嵌套。


```kotlin
/* 注释从这里开始
/* 包含嵌套的注释 */     
并且在这里结束。 */
```



## 字符串模板


```kotlin
var a = 1
// 模板中的简单名称：
val s1 = "a is $a"
a = 2
// 模板中的任意表达式：
val s2 = "${s1.replace("is", "was")}, but now is $a"
println(s2)

```

### 模板表达式

字符串字面值可以包含 *模板表达式* ，即一些小段代码，会求值并把结果合并到字符串中。
模板表达式以美元符（`$`）开头，由一个简单的名字构成:



```kotlin
val i = 10
println("i = $i") // 输出“i = 10”

```


或者用花括号括起来的任意表达式:


```kotlin
// 使用 {} 嵌入少量代码
val s = "abc"
println("$s.length is ${s.length}") // 输出“abc.length is 3”

```


原始字符串与转义字符串内部都支持模板。
如果你需要在原始字符串中表示字面值 `$` 字符（它不支持反斜杠转义），你可以用下列语法：


```kotlin
// 在字符串中 使用 $
val price = """${'$'}9.99"""
```


## 程序控制流


### if 表达式


```kotlin
fun maxOf(a: Int, b: Int): Int {
    if (a > b) {
        return a
    } else {
        return b
    }
}

```


在 Kotlin 中，*if* 也可以用作表达式：


```kotlin

fun maxOf(a: Int, b: Int) = if (a > b) a else b

fun main() {
    println("max of 0 and 42 is ${maxOf(0, 42)}")
}
```

在 Kotlin 中，if 是一个表达式，即它会返回一个值。
因此就不需要三元运算符（条件 ? 然后 : 否则），因为普通的 if 就能胜任这个角色。


```kotlin
// 传统用法
var max = a 
if (a < b) max = b

// With else 
var max: Int
if (a > b) {
    max = a
} else {
    max = b
}
 
// 作为表达式
val max = if (a > b) a else b
```


if 的分支可以是代码块，最后的表达式作为该块的值：


```kotlin
fun max(a : Int ,b : Int) : Int{
    val max = if (a > b) {
        print("Choose a")
        a
    } else {
        print("Choose b")
        b
    }
    return max
}
```


如果你使用 if 作为表达式而不是语句（例如：返回它的值或者<!--
-->把它赋给变量），该表达式需要有 `else` 分支。



### `for` 循环


```kotlin
// 遍历
val items = listOf("apple", "banana", "kiwifruit")
for (item in items) {
    println(item)
}
```


或者


```kotlin
// 遍历集合索引位置
val items = listOf("apple", "banana", "kiwifruit")
for (index in items.indices) {
    println("item at $index is ${items[index]}")
}
```

for 循环可以对任何提供迭代器（iterator）的对象进行遍历，这相当<!--
-->于像 C# 这样的语言中的 `foreach` 循环。语法如下：


```kotlin
for (item in collection) print(item)
```

循环体可以是一个代码块。


```kotlin
for (item: Int in ints) {
    // ……
}
```



如上所述，for可以循环遍历任何提供了迭代器的对象。即：

- 有一个成员函数或者扩展函数 `iterator()`，它的返回类型
- 有一个成员函数或者扩展函数 `next()`，并且
- 有一个成员函数或者扩展函数 `hasNext()` 返回 `Boolean`。

这三个函数都需要标记为 `operator`。



对区间或者数组的 `for` 循环会被编译为并不创建迭代器的基于索引的循环。

如果你想要通过索引遍历一个数组或者一个 list，你可以这么做：


```kotlin
val array = arrayOf("a", "b", "c")
for (i in array.indices) {
    println(array[i])
}
```


或者你可以用库函数 `withIndex`：


```kotlin
// 使用 库函数 withIndex 访问下标
val array = arrayOf("a", "b", "c")
for ((index, value) in array.withIndex()) {
    println("the element at $index is $value")
}
```

### 区间表达式

使用 in 运算符来检测某个数字是否在指定区间内：

```kotlin
// 检查是否在这个范围
val x = 10
val y = 9
if (x in 1..y+1) {
    println("fits in range")
}
```


检测某个数字是否在指定区间外: 

对 in 结果取反

```kotlin
// 检查是否不再一个范围内
val list = listOf("a", "b", "c")
if (-1 !in 0..list.lastIndex) {
    println("-1 is out of range  ${list.lastIndex}")
}
if (list.size !in list.indices) {
    println("list size is out of valid list indices range, too ${list.indices}")
}
```


区间迭代:


```kotlin
// 区间迭代
for (i in 1..5) {
    print(i)
}
// 12345
```



或数列迭代：


```kotlin

// step ： 步长 即每次迭代的差值
for (z in 1..10 step 2) {
    print(z)
}

for (w in 9 downTo 0 step 3) {
    print(w)
}
```


#### 区间与数列

Kotlin 可通过调用 `kotlin.ranges` 包中的 [`rangeTo()`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/range-to.html) 函数及其操作符形式的 `..` 轻松地创建两个值的区间。
通常，`rangeTo()` 会辅以 `in` 或 `!in` 函数。



```kotlin
if (i in 1..4) {  // 等同于 1 <= i && i <= 4
    print(i)
}
```


整数类型区间（[`IntRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-int-range/index.html)、[`LongRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-long-range/index.html)、[`CharRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-char-range/index.html)）还有一个拓展特性：可以对其进行迭代。
这些区间也是相应整数类型的[等差数列](https://zh.wikipedia.org/wiki/%E7%AD%89%E5%B7%AE%E6%95%B0%E5%88%97)。
这种区间通常用于 `for` 循环中的迭代。



```kotlin

for (i in 1..4) print(i)

```


要反向迭代数字，请使用 [`downTo`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/down-to.html) 函数而不是 `..` 。



```kotlin

for (i in 4 downTo 1) print(i)

```


也可以通过任意步长（不一定为 1 ）迭代数字。 这是通过 [`step`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/step.html) 函数完成的。


```kotlin
for (i in 1..8 step 2) print(i) //1357
println()
for (i in 8 downTo 1 step 2) print(i)   // 8642
```


要迭代不包含其结束元素的数字区间，请使用 [`until`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/until.html) 函数：



```kotlin
for (i in 1 until 10) {       // i in [1, 10), 10被排除
    print(i)
}

```


##### 区间

区间从数学意义上定义了一个封闭的间隔：它由两个端点值定义，这两个端点值都包含在该区间内。
区间是为可比较类型定义的：具有顺序，可以定义任意实例是否在两个给定实例之间的区间内。
区间的主要操作是 `contains`，通常以 `in` 与 `!in` 操作符的形式使用。

要为类创建一个区间，请在区间起始值上调用 `rangeTo()` 函数，并提供结束值作为参数。
`rangeTo()` 通常以操作符 `..` 形式调用。


```kotlin
class Version(val major: Int, val minor: Int): Comparable<Version> {
    override fun compareTo(other: Version): Int {
        if (this.major != other.major) {
            return this.major - other.major
        }
        return this.minor - other.minor
    }
}


val versionRange = Version(1, 11)..Version(1, 30)
println(Version(0, 9) in versionRange)
println(Version(1, 20) in versionRange)



```





##### 数列

如上个示例所示，整数类型的区间（例如 `Int`、`Long` 与 `Char`）可视为[等差数列](https://zh.wikipedia.org/wiki/%E7%AD%89%E5%B7%AE%E6%95%B0%E5%88%97)。
在 Kotlin 中，这些数列由特殊类型定义：[`IntProgression`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-int-progression/index.html)、[`LongProgression`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-long-progression/index.html) 与 [`CharProgression`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-char-progression/index.html)。

数列具有三个基本属性：`first` 元素、`last` 元素和一个非零的 `step`。
首个元素为 `first`，后续元素是前一个元素加上一个 `step`。
以确定的步长在数列上进行迭代等效于 Java/JavaScript 中基于索引的 `for` 循环。



```java
for (int i = first; i <= last; i += step) {
  // ……
}
```


通过迭代数列隐式创建区间时，此数列的 `first` 与 `last` 元素是区间的端点，`step` 为 1 。



```kotlin

for (i in 1..10) print(i)

```


要指定数列步长，请在区间上使用 `step` 函数。



```kotlin

for (i in 1..8 step 2) print(i)

```


数列的 `last` 元素是这样计算的：

* 对于正步长：不大于结束值且满足 `(last - first) % step == 0` 的最大值。
* 对于负步长：不小于结束值且满足 `(last - first) % step == 0` 的最小值。

因此，`last` 元素并非总与指定的结束值相同。



```kotlin

for (i in 1..9 step 3) print(i) // 最后一个元素是 7

```


要创建反向迭代的数列，请在定义其区间时使用 `downTo` 而不是 `..`。



```kotlin

 for (i in 4 downTo 1) print(i)

```


数列实现 `Iterable<N>`，其中 `N` 分别是 `Int`、`Long` 或 `Char`，因此可以在各种 集合函数（如 `map`、`filter` 与其他）中使用它们。



```kotlin

println((1..10).filter { it % 2== 0 })  // 输出区间内偶数

```



### `while` 循环

while 和 do .. while 循环和 Java 使用方式一致 无变化。


```kotlin

// while 
var x = 9
while (x > 0) {
    x--
}

// do while 循环
do {
    val y = getObj()
} while (y != null) // y 在此处可见
```

使用 while 循环 访问集合下标索引

```kotlin
val items = listOf("apple", "banana", "kiwifruit")
var index = 0
while (index < items.size) {
    println("item at $index is ${items[index]}")
    index++
}    
```




### `when` 表达式

when取代了 Java、C 语言的 switch 操作符。其最简单的形式如下：


```kotlin
when (x) {
    1 -> print("x == 1")
    2 -> print("x == 2")
    else -> { // 注意这个块
        print("x is neither 1 nor 2")
    }
}
```



when 将它的参数与所有的分支条件顺序比较，直到某个分支满足条件。

when 既可以被当做表达式使用也可以被当做语句使用。如果它被当做表达式，
符合条件的分支的值就是整个表达式的值，如果当做语句使用，
则忽略个别分支的值。if 一样，每一个分支可以是一个代码块，它的值<!--
-->是块中最后的表达式的值.

如果其他分支都不满足条件将会求值 else分支。
如果 when作为一个表达式使用，则必须有 else 分支，
除非编译器能够检测出所有的可能情况都已经覆盖了

如果很多分支需要用相同的方式处理，则可以把多个分支条件放在一起，用逗号分隔：
类似于 Java 中的 case 穿透。


```kotlin
when (x) {
    0, 1 -> print("x == 0 or x == 1")
    else -> print("otherwise")
}
```


我们可以用任意表达式（而不只是常量）作为分支条件,这里相对于 Java 中 switch 分支类型只能是 int、short、char、byte 以及 JDK7增加的 String 类型。



```kotlin
// 使用 方法作为匹配项
when (x) {
    parseInt() -> println("s encodes x")
    else -> println("s does not encode x")
}
```

我们也可以检测一个值在 in 或者不在 !in 一个[区间] 或者集合中：


```kotlin
// 检测是否在 .. 一个区间 或 一个集合中
val b = -1
val validNumbers = listOf(2,3,5,-1)
when (b) {
    in 1..10 -> println("x is in the range")
    in validNumbers -> println("x is valid")
    !in 10..20 -> println("x is outside the range")
    else -> println("none of the above")
}
```



另一种可能性是检测一个值是 is 或者不是 !is 一个特定类型的值。注意：
由于 智能转换，你可以访问该类型的方法与属性而无需<!--
-->任何额外的检测。



```kotlin
fun hasPrefix(x: Any) = when(x) {
    is String -> x.startsWith("prefix")
    else -> false
}
```

注: 关于 智能转换可参考 《Kotlin-类型检测与类型转换详解》



when 也可以用来取代 if else if 链。
如果不提供参数，所有的分支条件都是简单的布尔表达式，而当一个分支的条件为真时则执行该分支：



```kotlin
// 使用 when 代替 if else 表达式
val xw = Xwhen()
when {
    xw.isOdd() -> print("x is odd")
    xw.isEven() -> print("x is even")
    else -> print("x is funny")
}

class Xwhen{

    fun isOdd() : Boolean = false

    fun isEven() : Boolean = true
}
```



自 Kotlin 1.3 起，可以使用以下表达式 将函数结果 赋值与变量。



```kotlin
fun Request.getBody() =
        when (val response = executeRequest()) {
            is Success -> response.body
            is HttpError -> throw HttpException(response.status)
        }
```



在 when 主语中引入的变量的作用域仅限于 when 主体，是一个局部变量。



```kotlin

fun describe(obj: Any): String =
    when (obj) {
        1          -> "One"
        "Hello"    -> "Greeting"
        is Long    -> "Long"
        !is String -> "Not a string"
        else       -> "Unknown"
    }
```


## 集合

对集合进行迭代:


```kotlin
    val items = listOf("apple", "banana", "kiwifruit")

    for (item in items) {
        println(item)
    }

```



使用 in 运算符来判断集合内是否包含某实例：


```kotlin

    val items = setOf("apple", "banana", "kiwifruit")

    when {
        "orange" in items -> println("juicy")
        "apple" in items -> println("apple is fine too")
    }

```


使用 lambda 表达式来过滤（filter）与映射（map）集合：


```kotlin
    val fruits = listOf("banana", "avocado", "apple", "kiwifruit")
    fruits
      .filter { it.startsWith("a") }
      .sortedBy { it }
      .map { it.toUpperCase() }
      .forEach { println(it) }

```

关于 kotlin-集合相关 涉及比较广内容比较多，在接下来的培训中单独讲。

## 创建基本类及其实例


```kotlin
fun testInstance() {

    val rectangle = RectangleBean(5.0, 2.0)
    val triangle = Triangle(3.0, 4.0, 5.0)

    println(" rectangle = $rectangle")
    println(" triangle = $triangle")

    println("Area of rectangle is ${rectangle.calculateArea()}, its perimeter is ${rectangle.perimeter}")
    println("Area of triangle is ${triangle.calculateArea()}, its perimeter is ${triangle.perimeter}")
}

// 创建抽象类
private abstract class BaseShape(val sides: List<Double>) {

    val perimeter: Double get() = sides.sum()
    abstract fun calculateArea(): Double
}

// 创建接口
interface RectangleProperties {
    val isSquare: Boolean
}

private class RectangleBean(
    var height: Double,
    var length: Double ) : BaseShape(listOf(height, length, height, length)), RectangleProperties {

    override val isSquare: Boolean get() = length == height

    override fun calculateArea(): Double = height * length


    override fun toString(): String {
        return "RectangleBean(height=$height, length=$length)"
    }


}

private class Triangle(
    var sideA: Double,
    var sideB: Double,
    var sideC: Double
) : BaseShape(listOf(sideA, sideB, sideC)) {

    override fun calculateArea(): Double {
        val s = perimeter / 2
        return Math.sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))
    }

    override fun toString(): String {
        return "Triangle(sideA=$sideA, sideB=$sideB, sideC=$sideC)"
    }


}
```



