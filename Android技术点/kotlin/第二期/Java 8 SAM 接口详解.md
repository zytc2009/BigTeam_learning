# Java 8 SAM 接口详解

[TOC]


## 前言

SAM（Single Abstract Method）

JDK 8 新增加函数接口

定义了这种类型的接口，使得以其为参数的方法，可以在调用时，使用一个lambda表达式作为参数
从SAM原则上讲，这个接口中，只能有一个函数需要被实现，但是也可以有如下例外:

1. 默认方法与静态方法并不影响函数式接口的契约，可以任意使用，即函数式接口中可以有静态方法，
一个或者多个静态方法不会影响SAM接口成为函数式接口，并且静态方法可以提供方法实现可以由 default 修饰的默认方法方法，
 这个关键字是Java 8中新增的，为的目的就是使得某一些接口，原则上只有一个方法被实现，但是由于历史原因，
不得不加入一些方法来兼容整个JDK中的API，所以就需要使用default关键字来定义这样的方法

2. 可以有 Object 中覆盖的方法，也就是 equals，toString，hashcode等方法。

## 使用

定义一个 接口 内部一个抽象方法：

```java
interface IGetName {
    String getName(String name);
}
```

旧有使用方式

```java
//老方法
IGetName old = new IGetName() {
    @Override
    public String getName(String name) {
        return "Old : Hello " + name;
    }
};
System.out.println(" old : " + old.getName("world"));
```

新的使用方式

```java
//新方法
IGetName get = (name) -> "Hello "+name;

System.out.println("new : " + get.getName("Java"));
```

当作为方法时使用：

```java
public static void testFun(IGetName getName) {
    System.out.println(getName.getName("Hello"));
}
```

使用：

```java
// 使用方法 传入 lambda 表达式
testFun(val -> "Hello=" + val);
```
