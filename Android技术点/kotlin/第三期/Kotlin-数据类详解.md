# Kotlin-数据类详解

[TOC]


## 前言

我们经常创建一些只保存数据的类。
在这些类中，一些标准函数往往是从<!--
-->数据机械推导而来的。在 Kotlin 中，这叫做 数据类 并标记为 `data`：


```kotlin
data class User(val name: String, val age: Int)
```


编译器自动从主构造函数中声明的所有属性导出以下成员：

- `equals()`、`hashCode()` 
- `toString()` 格式是 `"User(name=John, age=42)"`
- `componentN()` 函数按声明顺序对应于所有属性
- `copy()` 函数

为了确保生成的代码的一致性以及有意义的行为，数据类必须满足以下要求：

- 主构造函数需要至少有一个参数
- 主构造函数的所有参数需要标记为 `val` 或 `var`；
- 数据类不能是抽象、开放、密封或者内部的；

此外，成员生成遵循关于成员继承的这些规则：

- 如果在数据类体中有显式实现 `equals()`、 `hashCode()` 或者 `toString()`，或者这些函数在父类中有
final 实现，那么不会生成这些函数，而会使用现有<!--
-->函数

- 如果超类型具有 `open` 的 `componentN()` 函数并且返回兼容的类型，
那么会为数据类生成相应的函数，并覆盖超类的实现。如果超类型的这些函数<!--
-->由于签名不兼容或者是 final 而导致无法覆盖，那么会报错

- 从一个已具 `copy(……)` 函数且签名匹配的类型派生一个数据类在
Kotlin 1.2 中已弃用，并且在 Kotlin 1.3 中已禁用。
- 不允许为 `componentN()` 以及 `copy()` 函数提供显式实现。


在 JVM 中，如果生成的类需要含有一个无参的构造函数，则所有的属性必须指定默认值。


```kotlin
data class User(val name: String = "", val age: Int = 0)
```


## 在类体中声明的属性

注意，对于那些自动生成的函数，编译器只使用在主构造函数内部定义的属性。如需在生成的实现中排除一个属性，请将其声明在类体中：


```kotlin
data class Person(val name: String) {
    var age: Int = 0
}
```

在 `toString()`、 `equals()`、 `hashCode()` 以及 `copy()` 的实现中只会用到 `name` 属性，并且只有一个 component 函数 `component1()`。虽然两个 `Person` 对象可以有不同的年龄，但它们会视为相等。


```kotlin
data class Person(val name: String) {
    var age: Int = 0
}
```


## 复制

在很多情况下，我们需要复制一个对象改变它的一些属性，但其余部分保持不变。
 `copy()` 函数就是为此而生成。对于上文的 `User` 类，其实现会类似下面这样：


```kotlin
fun copy(name: String = this.name, age: Int = this.age) = User(name, age)
```

这让我们可以写：

```kotlin
val user = User("张三",23)
val user2 = user.copy(name = "李四")

println("user = $user , user2 = $user2")
```

## 数据类与解构声明

为数据类生成的 Component 函数

使用：

```kotlin
// 使用解构声明 
val jane = User("王五", 35)
val (name, age) = jane

println("$name, $age years of age") // 输出 "王五, 35 years of age"
```

## 标准数据类

标准库提供了 `Pair` 与 `Triple`。尽管在很多情况下具名数据类是更好的设计选择，
因为它们通过为属性提供有意义的名称使代码更具可读性。

使用：

```kotlin
val pair = Pair<String,Long>("李四",23)
val triple = Triple<Long,String,Int>(4,"init",9)

// 使用 Pair 初始化map集合
val map = mapOf(Pair<String,Int>("李四",23),Pair<String,Int>("哈哈",23),
Pair<String,Int>("老罗",56))

println("map $map")
```