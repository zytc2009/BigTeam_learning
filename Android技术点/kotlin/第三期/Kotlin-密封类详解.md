# Kotlin-密封类详解

[TOC]

## 定义

密封类用来表示受限的类继承结构：当一个值为有限几种的<!--
-->类型、而不能有任何其他类型时。在某种意义上，他们是枚举类的扩展：枚举类型的值集合<!--
-->也是受限的，但每个枚举常量只存在一个实例，而密封类<!--
-->的一个子类可以有可包含状态的多个实例。


要声明一个密封类，需要在类名前面添加 `sealed` 修饰符。虽然密封类也可以<!--
-->有子类，但是所有子类都必须在与密封类自身相同的文件中声明。

该规则更加严格：子类必须嵌套在密封类声明的内部）。

## 使用

```kotlin
// 定义一个密封类
sealed class Expr{
    // 定义抽象方法
    abstract fun getExprName()
    
    fun foo(){
    }
}
data class Const(val number: Double) : Expr(){
    override fun getExprName() {
    }
}
data class Sum(val e1: Expr, val e2: Expr) : Expr(){
    override fun getExprName() {
    }
}
object NotANumber : Expr() {
    override fun getExprName() {
        
    }
}
```



- 一个密封类自身就是一个抽象类，它不能直接实例化并可以有抽象 `abstract` 成员。

- 密封类不允许有非-`private` 构造函数（其构造函数默认为 `private`）。

> 注意: 扩展密封类可以放在任何位置，而无需在同一个文件中。

使用密封类的关键好处在于使用 `when` 表达式 的时候，如果能够验证语句覆盖了所有情况，就不需要为该语句再添加一个 `else` 子句了。当然，这只有当你用 `when` 作为表达式（使用结果）而不是作为语句时才有用。

比如：

```kotlin
fun eval(expr: Expr): Double = when (expr) {
    
    is Const -> expr.number
    is Sum -> eval(expr.e1) + eval(expr.e2)
    NotANumber -> Double.NaN
    // 不再需要 `else` 子句，因为我们已经覆盖了所有的情况
}
```

