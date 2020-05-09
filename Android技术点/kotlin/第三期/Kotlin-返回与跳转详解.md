# Kotlin-返回与跳转详解

[TOC]


## 前言
 

Kotlin 有三种结构化跳转表达式：

- `return` 默认从最直接包围它的函数或者匿名函数返回。
- `break`   终止最直接包围它的循环。
- `continue`继续下一次最直接包围它的循环。


## Break 与 Continue 标签

在 Kotlin 中任何表达式都可以用标签 `label` 来标记。
标签的格式为标识符后跟 `@` 符号，例如：`abc@`、`fooBar@`都是有效的标签。
要为一个表达式加标签，我们只要在其前加标签即可。


```kotlin
loop@ for (i in 1..100) {
    // ……
}
```


现在，我们可以用标签限制 `break` 或者 `continue`：


```kotlin
loop@ for (i in 1..10) {
    println("外层循环 i = $i")
    for (j in 1..10) {
        println(" j = $j")
        if (i == 5) break@loop
    }
}
```
标签限制的 `break` 跳转到刚好位于该标签指定的循环后面的执行点。

当然跳转位置可以自定义：

```kotlin
// 指定跳转位置
loop@ for (i in 1..10) {
    println("外层循环 i = $i")
    inter@ for (j in 1..10) {
        println(" j = $j")
        if (i == 5) break@inter
    }
}
```
可以指定跳转位置。


`continue` 继续标签指定的循环的下一次迭代。


## 返回到标签

Kotlin 有函数字面量、局部函数和对象表达式。因此 Kotlin 的函数可以被嵌套。
标签限制的 `return` 允许我们从外层函数返回。
最重要的一个用途就是从 lambda 表达式中返回。回想一下我们这么写的时候：



```kotlin
private fun foo() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return // 非局部直接返回到 foo() 的调用者
        print(it)
    }
    println("this point is unreachable")
}
```


这个 `return` 表达式从最直接包围它的函数即 `foo` 中返回。
如果我们需要从 lambda 表达式中返回，我们必须给它加标签并用以限制 `return`。



```kotlin
private fun foo2() {
    println(" foo2 ---")
    listOf(1, 2, 3, 4, 5).forEach lit@{
        if (it == 3) return@lit // 局部返回到该 lambda 表达式的调用者，即 forEach 循环
        print(it)
    }
    print(" done with explicit label")
}
```


现在，它只会从 lambda 表达式中返回。通常情况下使用隐式标签更方便。
该标签与接受该 lambda 的函数同名。


```kotlin
fun foo3() {
    println(" foo3 ---")
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return@forEach // 局部返回到该 lambda 表达式的调用者，即 forEach 循环
        print(it)
    }
    print(" done with implicit label")
}
```

或者，我们用一个[匿名函数替代 lambda 表达式。
匿名函数内部的 `return` 语句将从该匿名函数自身返回



```kotlin
fun foo4() {
    println(" foo4 ---")
    listOf(1, 2, 3, 4, 5).forEach(fun(value: Int) {
        if (value == 3) return  // 局部返回到匿名函数的调用者，即 forEach 循环
        print(value)
    })
    print(" done with anonymous function")
}
```


请注意，前文三个示例中使用的局部返回类似于在常规循环中使用 `continue`。并没有 `break` 的直接等价形式，不过可以通过增加另一层嵌套 lambda 表达式并从其中非局部返回来模拟：



```kotlin
private fun foo5() {
    println(" foo5 ---")
    run loop@{
        listOf(1, 2, 3, 4, 5).forEach {
            if (it == 3) return@loop // 从传入 run 的 lambda 表达式非局部返回
            print(it)
        }
    }
    print(" done with nested loop")
}
```


当要返一个回值的时候，解析器优先选用标签限制的 return，即


```kotlin
return@a 1
```


意为“返回 `1` 到 `@a`”，而不是“返回一个标签标注的表达式 `(@a 1)`”。
