
# Kotlin-函数详解

[TOC]

## 函数声明

Kotlin 中的函数使用 *fun*{: .keyword } 关键字声明：


```kotlin
fun double(x: Int): Int {
    return 2 * x
}
```

## 函数用法

调用函数使用传统的方法：



```kotlin
val result = double(2)
```


调用成员函数使用点表示法：


```kotlin
Stream().read() // 创建类 Stream 实例并调用 read()
```


### 参数

函数参数使用 Pascal 表示法定义，即 *name*: *type*。参数用逗号隔开。每个参数必须有显式类型：


```kotlin
fun powerOf(number: Int, exponent: Int) { /*……*/ }
```


### 默认参数

函数参数可以有默认值，当省略相应的参数时使用默认值。与其他语言相比，这可以减少<!--
-->重载数量：



```kotlin
fun read(b: Array<Byte>, off: Int = 0, len: Int = b.size) { /*……*/ }
```


默认值通过类型后面的 **=** 及给出的值来定义。

覆盖方法总是使用与基类型方法相同的默认参数值。
当覆盖一个带有默认参数值的方法时，必须从签名中省略默认参数值：


```kotlin
open class A {
    open fun foo(i: Int = 10) { /*……*/ }
}

class B : A() {
    override fun foo(i: Int) { /*……*/ }  // 不能有默认值
}
```


如果一个默认参数在一个无默认值的参数之前，那么该默认值只能通过使用[具名参数](#具名参数)调用该函数来使用：



```kotlin
fun foo(bar: Int = 0, baz: Int) { /*……*/ }

foo(baz = 1) // 使用默认值 bar = 0
```


如果在默认参数之后的最后一个参数是 [lambda 表达式](lambdas.html#lambda-表达式语法)，那么它既可以作为具名参数在括号内传入，也可以在[括号外](lambdas.html#passing-a-lambda-to-the-last-parameter)传入：



```kotlin
fun foo(bar: Int = 0, baz: Int = 1, qux: () -> Unit) { /*……*/ }

foo(1) { println("hello") }     // 使用默认值 baz = 1
foo(qux = { println("hello") }) // 使用两个默认值 bar = 0 与 baz = 1
foo { println("hello") }        // 使用两个默认值 bar = 0 与 baz = 1
```


### 具名参数

可以在调用函数时使用具名的函数参数。当一个函数有大量的参数或默认参数时这会非常方便。

给定以下函数：


```kotlin
fun reformat(str: String,
             normalizeCase: Boolean = true,
             upperCaseFirstLetter: Boolean = true,
             divideByCamelHumps: Boolean = false,
             wordSeparator: Char = ' ') {
/*……*/
}
```


我们可以使用默认参数来调用它：



```kotlin
reformat(str)
```


然而，当使用非默认参数调用它时，该调用看起来就像：



```kotlin
reformat(str, true, true, false, '_')
```


使用具名参数我们可以使代码更具有可读性：



```kotlin
reformat(str,
    normalizeCase = true,
    upperCaseFirstLetter = true,
    divideByCamelHumps = false,
    wordSeparator = '_'
)
```


并且如果我们不需要所有的参数：



```kotlin
reformat(str, wordSeparator = '_')
```


当一个函数调用混用位置参数与具名参数时，所有位置参数都要放在第一个具名参数之前。例如，允许调用 `f(1, y = 2)` 但不允许 `f(x = 1, 2)`。

可以通过使用**星号**操作符将[可变数量参数（*vararg*{: .keyword }）](#可变数量的参数varargs) 以具名形式传入：



```kotlin
fun foo(vararg strings: String) { /*……*/ }

foo(strings = *arrayOf("a", "b", "c"))
```



> **对于 JVM 平台**：在调用 Java 函数时不能使用具名参数语法，因为 Java 字节码并不<!--
-->总是保留函数参数的名称。

### 返回 Unit 的函数

如果一个函数不返回任何有用的值，它的返回类型是 `Unit`。`Unit` 是一种只有一个值——`Unit` 的类型。这个<!--
-->值不需要显式返回：



```kotlin
fun printHello(name: String?): Unit {
    if (name != null)
        println("Hello ${name}")
    else
        println("Hi there!")
    // `return Unit` 或者 `return` 是可选的
}
```

`Unit` 返回类型声明也是可选的。上面的代码等同于：



```kotlin
fun printHello(name: String?) { …… }
```


### 单表达式函数

当函数返回单个表达式时，可以省略花括号并且在 **=** 符号之后指定代码体即可：



```kotlin
fun double(x: Int): Int = x * 2
```


当返回值类型可由编译器推断时，显式声明返回类型是[可选](#显式返回类型)的：



```kotlin
fun double(x: Int) = x * 2
```

单项表达式函数 可以省略{}表达方便便于理解.


### 可变数量的参数（Varargs）

函数的参数（通常是最后一个）可以用 `vararg` 修饰符标记：

Java 定义可变数量参数

```java
private void asList(int state, String[]...) { /*……*/ }
```

Kotlin 定义可变数量参数

```kotlin
fun <T> asList(vararg ts: T): List<T> {
    val result = ArrayList<T>()
    for (t in ts) // ts is an Array
        result.add(t)
    return result
}
```


允许将可变数量的参数传递给函数：



```kotlin
val list = asList(1, 2, 3)
```


在函数内部，类型 `T` 的 `vararg` 参数的可见方式是作为 `T` 数组，即上例中的 `ts` 变量具有类型 `Array <out T>`。

只有一个参数可以标注为 `vararg`。如果 `vararg` 参数不是列表中的最后一个参数， 可以使用<!--
-->具名参数语法传递其后的参数的值，或者，如果参数具有函数类型，则通过在括号外部<!--
-->传一个 lambda。

当我们调用 `vararg`-函数时，我们可以一个接一个地传参，例如 `asList(1, 2, 3)`，或者，如果我们已经有一个数组<!--
-->并希望将其内容传给该函数，我们使用**伸展（spread）**操作符（在数组前面加 `*`）：


```kotlin
val a = arrayOf(1, 2, 3)
val list = asList(-1, 0, *a, 4)
```

这种传参表示方式使函数使用更加方便简洁。


### 中缀表示法

标有 *infix* 关键字的函数也可以使用中缀表示法（忽略该调用的点与圆括号）调用。中缀函数必须满足以下要求：

- 它们必须是成员函数或扩展函数
- 它们必须只有一个参数；
- 其参数不得 接受可变数量的参数varargs且参数不能有默认值。



```kotlin
// kotlin/primitives.kt

infix fun Int.shl(x: Int): Int { …… }

// 用中缀表示法调用该函数
1 shl 2

// 等同于这样
1.shl(2)
```


> 中缀函数调用的优先级低于算术操作符、类型转换以及 `rangeTo` 操作符。
> 以下表达式是等价的：
> * `1 shl 2 + 3` 等价于 `1 shl (2 + 3)`
> * `0 until n * 2` 等价于 `0 until (n * 2)`
> * `xs union ys as Set<*>` 等价于 `xs union (ys as Set<*>)`
>
> 另一方面，中缀函数调用的优先级高于布尔操作符 `&&` 与 `||`、`is-` 与 `in-` 检测以及其他一些操作符。这些表达式也是等价的：
> * `a && b xor c` 等价于 `a && (b xor c)`
> * `a xor b in c` 等价于 `(a xor b) in c`


请注意，中缀函数总是要求指定接收者与参数。当<!--
-->使用中缀表示法在当前接收者上调用方法时，需要显式使用 `this`；不能像常规方法调用<!--
-->那样省略。这是确保非模糊解析所必需的。



```kotlin
class MyStringCollection {
    infix fun add(s: String) { /*……*/ }
    
    fun build() {
        this add "abc"   // 正确
        add("abc")       // 正确
        //add "abc"        // 错误：必须指定接收者
    }
}
```

中缀函数表示使用存在局限性。特别是 调用存在优先级关系 中缀函数调用的优先级低于算术操作符、类型转换以及 `rangeTo` 操作符。所以使用时谨慎处理。



## 函数作用域

在 Kotlin 中函数可以在文件顶层声明，这意味着你不需要像一些语言如 Java、C# 或 Scala 那样需要创建一个类来保存一个函数。此外<!--
-->除了顶层函数，Kotlin 中函数也可以声明在局部作用域、作为成员函数以及扩展函数。

### 局部函数

Kotlin 支持局部函数，即一个函数在另一个函数内部：



```kotlin
// 内部函数
fun dfs(graph: Int) {
    fun dfs(current: Int, visited: Array<Int>) {
        for (v in visited)
            // 这里进行递归操作
            dfs(0, visited)
    }
    //这里可以调用内部函数
    dfs(0, arrayOf(1,2,5))
}
```


局部函数可以访问外部函数（即闭包）的局部变量，所以在上例中，*visited* 可以是局部变量：



```kotlin
// 内部函数
fun dfs(graph: Int) {
    val visited = HashSet<Int>()
    
    fun dfs(current: Int, visited: Array<Int>) {
        for (v in visited) {
            // 这里进行递归操作
            dfs(0, visited)
        }
        // 访问外部局部变量
        println("visited $visited")
    }
    //这里可以调用内部函数
    dfs(0, arrayOf(1,2,5))
}
```


### 成员函数

成员函数是在类或对象内部定义的函数：



```kotlin
class Sample() {
    fun foo() { print("Foo") }
}
```


成员函数以点表示法调用：



```kotlin
Sample().foo() // 创建类 Sample 实例并调用 foo
```



## 泛型函数

函数可以有泛型参数，通过在函数名前使用尖括号指定：



```kotlin
fun <T> singletonList(item: T): List<T> { /*……*/ }
```


关于泛型函数中的Kotlin[泛型] 详细内容 请参考 Kotlin-泛型详解。

## 内联函数

关于内联函数详细内容 请参考 Kotlin-内联函数详解。

## 扩展函数

关于内联函数详细内容 请参考 Kotlin-扩展函数详解。

## 高阶函数和 Lambda 表达式

关于 高阶函数和 Lambda 表达式详细内容 请参考 Kotlin-高阶函数和 Lambda详解。

## 尾递归函数

Kotlin 支持一种称为[尾递归](https://zh.wikipedia.org/wiki/%E5%B0%BE%E8%B0%83%E7%94%A8)的函数式编程风格。
这允许一些通常用循环写的算法改用递归函数来写，而无堆栈溢出的风险。
当一个函数用 `tailrec` 修饰符标记并满足所需的形式时，编译器会优化该递归，留下一个快速而高效的基于循环的版本：



```kotlin
val eps = 1E-10 // "good enough", could be 10^-15

tailrec fun findFixPoint(x: Double = 1.0): Double
        = if (Math.abs(x - Math.cos(x)) < eps) x else findFixPoint(Math.cos(x))
```


这段代码计算余弦的不动点（fixpoint of cosine），这是一个数学常数。 它只是重复地从 1.0 开始调用 Math.cos，直到结果不再改变，对于这里指定的 `eps` 精度会产生 0.7390851332151611 的结果。最终代码相当于这种更传统风格的代码：



```kotlin
val eps = 1E-10 // "good enough", could be 10^-15

private fun findFixPoint(): Double {
    var x = 1.0
    while (true) {
        val y = Math.cos(x)
        if (Math.abs(x - y) < eps) return x
        x = Math.cos(x)
    }
}
```


要符合 `tailrec` 修饰符的条件的话，函数必须将其自身调用作为它执行的最后一个操作。在递归调用后有更多代码时，不能使用尾递归，并且不能用在 try/catch/finally 块中。目前在 Kotlin for JVM 与 Kotlin/Native 中支持尾递归。

