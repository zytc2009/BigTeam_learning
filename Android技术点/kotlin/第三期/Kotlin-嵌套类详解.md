# Kotlin-嵌套类详解

[TOC]


## 嵌套类

类可以嵌套在其他类中：

```kotlin
class Others {
    
    private val bar: Int = 1
    // 嵌套类
    class Nested {
        fun foo() = 2
    }
}
```
使用：
```kotlin
val nested = Others.Nested()
nested.foo()
```


## 内部类

使用 `inner`关键字创建内部类，能够访问其外部类的成员。内部类会带有一个对外部类的对象的引用


```kotlin
class Others {
    private val bar: Int = 1
    // 嵌套类
    class Nested {
        fun foo() = 2
    }
    
    // 内部类
    inner class Inner{
        fun getBar() = bar
    }
}
```
使用：
```kotlin
// 先创建外部类对象 在创建内部类对象
val inner = Others().Inner()
inner.getBar()
```


## 匿名内部类

使用 对象表达式 `object` 创建匿名内部类实例：

```kotlin
// 创建接口
public interface OnViewClickener{
    fun onClick(view:View)
}

// 创建一个接受接口的函数
fun setOnViewClicker(onViewClickener: OnViewClickener){
    //......
}
```

使用：

```kotlin
// 创建匿名内部类
setOnViewClicker(object :OnViewClickener{
    override fun onClick(view: View) {
        //......
    }
})
```

> 注：对于 JVM 平台, 如果对象是函数式 Java 接口（即具有单个抽象方法的 Java 接口）的实例，
你可以使用带接口类型前缀的lambda表达式创建它：

```java
public interface OnViewListener {
    String getName(String name);
}
```

```kotlin
fun setOnViewListener(onViewListener: OnViewListener){
    //......
}
```
使用：
```kotlin
// 使用lambda 表达式创建匿名内部类
setOnViewListener(OnViewListener {
    name -> name+""
})
```


