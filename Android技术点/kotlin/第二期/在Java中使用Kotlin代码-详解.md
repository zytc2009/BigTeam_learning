# 在Java中使用Kotlin代码-详解

[TOC]


Java 可以轻松调用 Kotlin 代码。

例如，可以在 Java 方法中无缝创建与操作 Kotlin 类的实例。
然而，在将 Kotlin 代码集成到 Java 中时，
需要注意 Java 与 Kotlin 之间的一些差异。



## 属性

Kotlin 属性会编译成以下 Java 元素：

 * 一个 getter 方法，名称通过加前缀 `get` 算出；
 * 一个 setter 方法，名称通过加前缀 `set` 算出（只适用于 `var` 属性）；
 * 一个私有字段，与属性名称相同（仅适用于具有幕后字段的属性）。

例如，`var firstName: String` 编译成以下 Java 声明：



``` java
private String firstName;

public String getFirstName() {
    return firstName;
}

public void setFirstName(String firstName) {
    this.firstName = firstName;
}
```


如果属性的名称以 `is` 开头，则使用不同的名称映射规则：getter 的名称<!--
-->与属性名称相同，并且 setter 的名称是通过将 `is` 替换为 `set` 获得。

例如，对于属性 `isOpen`，其 getter 会称做 `isOpen()`，而其 setter 会称做 `setOpen()`。
这一规则适用于任何类型的属性，并不仅限于 `Boolean`。

## 包级函数

在 `com.kotlin.simple` 包内的 `InteropKotlin.kt` 文件中声明的所有的函数和属性，包括扩展函数，
都编译成一个名为 `com.kotlin.simple.InteropKotlinKt` 的 Java 类的静态方法。



```kotlin
// app.kt
package com.kotlin.simple.simple2

fun getTime() { /*……*/ }

```


``` java
// Java
//调用包级函数
InteropKotlinKt.getTime();
```

可以使用 `@JvmName` 注解修改生成的 Java 类的类名：



```kotlin
@file:JvmName("Simple2Name")

package com.kotlin.simple.simple2

class Util

fun getSimpleName() {
    /*……*/
}

```


``` java
// Java
new com.kotlin.simple.simple2.Util().hashCode();

Simple2Name.getSimpleName();
```


如果多个文件中生成了相同的 Java 类名（包名相同并且类名相同或者有相同的
[`@JvmName`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html) 注解）通常是错误的。然而，编译器能够生成一个单一的 Java 外观<!--
-->类，它具有指定的名称且包含来自所有文件中具有该名称的所有声明。
要启用生成这样的外观，请在所有相关文件中使用 [`@JvmMultifileClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-multifile-class/index.html) 注解。



```kotlin
// Simple2.kt
@file:JvmName("Simple2Name")
@file:JvmMultifileClass

package com.kotlin.simple.simple2

class Util

fun getSimpleName() {
    /*……*/
}
```

在创建一个 `Simple3.kt` 使用和 `Simple2.kt` 一样的代码:


```kotlin
// Simple3.kt
@file:JvmName("Simple2Name")
@file:JvmMultifileClass

package com.kotlin.simple.simple2
fun getDataName() {
    /*……*/
}
```


``` java
// Java
Simple2Name.getSimpleName();
Simple2Name.getDataName();
```

## 实例字段

如果需要在 Java 中将 Kotlin 属性作为字段暴露，那就使用 [`@JvmField`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-field/index.html) 注解对其标注。

该字段将具有与底层属性相同的可见性。如果一个属性有幕后字段（backing field）、非私有、没有 `open`
/`override` 或者 `const` 修饰符并且不是被委托的属性，那么你可以用 `@JvmField` 注解该属性。


```kotlin
class Person{

    @JvmField
    var peronId:Int = 23
    
    var personName : String? = null
}
```

使用

``` java
// Java
Person person  = new Person();
person.peronId = 1;
person.setPersonName("23");
```


延迟初始化的属性（在Java中）也会暴露为字段。
该字段的可见性与 `lateinit` 属性的 setter 相同。

## 静态字段

在具名对象或伴生对象中声明的 Kotlin 属性会在该具名对象或包含伴生对象的类中<!--
-->具有静态幕后字段。

通常这些字段是私有的，但可以通过以下方式之一暴露出来：

 - [`@JvmField`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-field/index.html) 注解；
 - `lateinit` 修饰符；
 - `const` 修饰符。

使用 `@JvmField` 标注这样的属性使其成为与属性本身具有相同可见性的静态字段。



```kotlin
class Key(val value: Int) {
    // 伴生对象
    companion object {
        
        @JvmField
        val COMPARATOR: Comparator<Key> = compareBy<Key> {
            it.value 
        }
    }
}
```

``` java
// Java
Key key1 = new Key(2);
Key key2 = new Key(-4);

Key.COMPARATOR.compare(key1,key2);
// Key 类中的 public static final 字段
```


在具名对象或者伴生对象中的一个延迟初始化的 属性<!--
-->具有与属性 setter 相同可见性的静态幕后字段。


```kotlin
object Singleton {
    lateinit var provider: Provider
}
```


``` java
// Java
Singleton.provider = new Provider();
// 在 Singleton 类中的 public static 非-final 字段
```

（在类中以及在顶层）以 `const` 声明的属性在 Java 中会成为静态字段：


```kotlin
object Obj {
    const val CONST = 1
}

class C {
    companion object {
        const val VERSION = 9
    }
}

const val MAX = 239
```

在 Java 中：


``` java
// 访问常量
int c = Obj.CONST;
int max = InteropKotlinKt.MAX;
int version = C.VERSION;
```


## 静态方法

如上所述，Kotlin 将包级函数表示为静态方法。

Kotlin 还可以为具名对象或伴生对象中定义的函数生成静态方法，如果你将这些函数标注为 [`@JvmStatic`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-static/index.html) 的话。
如果你使用该注解，编译器既会在相应对象的类中生成静态方法，也会在对象自身中生成实例方法。
例如：



```kotlin
class C {
    companion object {
       // 静态方法
       @JvmStatic
       fun callStatic() {
        }
        // 非静态方法
        fun callNonStatic() {
    }
    }
}
```


现在，`callStatic()` 在 Java 中是静态的，而 `callNonStatic()` 不是：



``` java
// 调用静态方法
InteropC.callStatic();

// 通过实例对象调用 非静态方法
InteropC.Companion.callNonStatic();
```


对于具名对象也同样：



```kotlin
object Obj {
    @JvmStatic fun callStatic() {
        
    }
    fun callNonStatic() {
        
    }
}
```


在 Java 中：

``` java
Obj.callStatic(); // 没问题
Obj.callNonStatic(); // 编译错误
Obj.INSTANCE.callNonStatic(); // 没问题，通过单例实例调用
Obj.INSTANCE.callStatic(); // 也没问题
```


自 Kotlin 1.3 起，`@JvmStatic` 也适用于在接口的伴生对象中定义的函数。

这类函数会编译为接口中的静态方法。请注意，接口中的静态方法是 Java 1.8 中引入的，
因此请确保使用相应的编译目标。



```kotlin
interface ChatBot {
    companion object {
        @JvmStatic fun greet(username: String) {
            println("Hello, $username")
        }
    }
}
```
调用接口中 伴生对象静态方法

```java
// 调用接口中 伴生对象中静态方法
ChatBot.Companion.greet("123");
```



`@JvmStatic`　注解也可以应用于对象或伴生对象的属性，
使其 getter 和 setter 方法在该对象或包含该伴生对象的类中是静态成员。

## 接口中的默认方法

> 默认方法仅适用于面向 JVM 1.8 及更高版本。

> `@JvmDefault` 注解在 Kotlin 1.3 中是实验性的。其名称与行为都可能发生变化，导致将来不兼容。


自 JDK 1.8 起，Java 中的接口可以包含[默认方法](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)。
可以将 Kotlin 接口的非抽象成员为实现它的 Java 类声明为默认。
如需将一个成员声明为默认，请使用 [`@JvmDefault`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-default/index.html) 注解标记之。

这是一个带有默认方法的 Kotlin 接口的一个示例：


```kotlin
interface Robot {
    @JvmDefault fun move() {
    println("~walking~")
    }
    
    fun speak(): Unit
}
```


默认实现对于实现该接口的 Java 类都可用。



```java
//Java 实现
public class C3PO implements Robot {
    // 来自 Robot 的 move() 实现隐式可用
    @Override
    public void speak() {
        System.out.println("I beg your pardon, sir");
    }
}
```



```java
C3PO c3po = new C3PO();
c3po.move(); // 来自 Robot 接口的默认实现
c3po.speak();
```


接口的实现者可以覆盖默认方法。



```java
//Java
public class BB8 implements Robot {

    //自己实现默认方法
    @Override
    public void move() {
        System.out.println("~rolling~");
    }

    @Override
    public void speak() {
        System.out.println("Beep-beep");
    }
}
```


为了让 `@JvmDefault` 生效，编译该接口必须带有 `-Xjvm-default` 参数。
根据添加注解的情况，指定下列值之一：

* `-Xjvm-default=enabled` 只添加带有 `@JvmDefault` 注解的新方法时使用。
   这包括为 API 添加整个接口。
* `-Xjvm-default=compatibility` 将 `@JvmDefault` 添加到以往 API 中就有的方法时使用。
   这种模式有助于避免兼容性破坏：为先前版本编写的所有接口实现都会与新版本完全兼容。
   然而，兼容模式可能会增大生成字节码的规模并且影响性能。

关于兼容性的更多详情请参见 `@JvmDefault` [参考页](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-default/index.html)。



## 访问权限

Kotlin 的可见性以下列方式映射到 Java：

* `private` 成员编译成 `private` 成员；
* `private` 的顶层声明编译成包级局部声明；
* `protected` 保持 `protected`（注意 Java 允许访问同一个包中其他类的受保护成员，
而 Kotlin 不能，所以 Java 类会访问更广泛的代码）；
* `internal` 声明会成为 Java 中的 `public`。`internal` 类的成员会通过名字修饰，使其<!--
-->更难以在 Java 中意外使用到，并且根据 Kotlin 规则使其允许重载相同签名的成员<!--
-->而互不可见；
* `public` 保持 `public`。

## KClass

有时你需要调用有 `KClass` 类型参数的 Kotlin 方法。
因为没有从 `Class` 到 `KClass` 的自动转换，所以你必须通过调用
`Class<T>.kotlin` 扩展属性的等价形式来手动进行转换：



```kotlin
/**
 * 传入 KClass 字节码对象
 */
fun getInteropClass(kClass: KClass<Any>){
}
```
使用

```java
// 获取 kotlin 字节码对象
KClass kotlinKClass = kotlin.jvm.JvmClassMappingKt.getKotlinClass(InteropKotlin.class);

//interopKotlin.getInteropClass(InteropKotlin.class);  编译错误
interopKotlin.getInteropClass(kotlinKClass);
```


## 用 `@JvmName` 解决签名冲突

有时我们想让一个 Kotlin 中的具名函数在字节码中有另外一个 JVM 名称。
最突出的例子是由于*类型擦除*引发的：



```kotlin
fun List<String>.filterValid(): List<String>{
    return ArrayList<String>(1)
}
fun List<Int>.filterValid(): List<Int>{
    return ArrayList<Int>(1)
}
```


这两个函数不能同时定义，因为它们的 JVM 签名是一样的：`filterValid(Ljava/util/List;)Ljava/util/List;`。
如果我们真的希望它们在 Kotlin 中用相同名称，我们需要用 [`@JvmName`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html) 去标注其中的一个（或两个），并指定不同的名称作为参数：



```kotlin
fun List<String>.filterValid(): List<String>{
    return ArrayList<String>(1)
}

@JvmName("filterValidInt")
fun List<Int>.filterValid(): List<Int>{
    return ArrayList<Int>(1)
}
```


在 Kotlin 中它们可以用相同的名称 `filterValid` 来访问，而在 Java 中，它们分别是 `filterValid` 和 `filterValidInt`。

```java
// 访问
interopKotlin.filterValid(new ArrayList<String>());
interopKotlin.filterValidInt(new ArrayList<Integer>());
```


同样的技巧也适用于属性 `x` 和函数 `getX()` 共存：



```kotlin
val x: Int
    @JvmName("getX_prop")
    get() = 15

fun getX() = 10
```

```java
interopKotlin.getX();
interopKotlin.getX_prop();
```

如需在没有显式实现 getter 与 setter 的情况下更改属性生成的访问器方法的名称，可以使用 `@get:JvmName` 与 `@set:JvmName`：



```kotlin
@get:JvmName("z")
@set:JvmName("changeZ")
var z: Int = 23
```


## 生成重载

通常，如果你写一个有默认参数值的 Kotlin 函数，在 Java 中只会有一个所有参数都存在的完整参数<!--
-->签名的方法可见，每个参数必传、如果希望向 Java 调用者暴露多个重载，可以使用
[`@JvmOverloads`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html) 注解。

该注解也适用于构造函数、静态方法等。它不能用于抽象方法，包括<!--
-->在接口中定义的方法。



```kotlin
// 使用 @JvmOverloads 标识可以重载体现的类 和 函数
class Circle @JvmOverloads constructor(centerX: Int, centerY: Int, radius: Double = 1.0) {
    
    @JvmOverloads
    fun draw(label: String, lineWidth: Int = 1, color: String = "red") { /*……*/
    }
}
```


对于每一个有默认值的参数，都会生成一个额外的重载，这个重载会把这个参数和<!--
-->它右边的所有参数都移除掉。在上例中，会生成以下代码：


``` java
// 构造函数：
Circle(int centerX, int centerY, double radius)
Circle(int centerX, int centerY)

// 方法
void draw(String label, int lineWidth, String color) { }
void draw(String label, int lineWidth) { }
void draw(String label) { }
```

```java
Circle circle1 = new Circle(10,20);
Circle circle2 = new Circle(10,20,5);

circle1.draw("");
circle1.draw("",2);
circle1.draw("",2,"");
```


请注意，如次构造函数 中所述，如果一个类的所有构造函数参数都有默认<!--
-->值，那么会为其生成一个公有的无参构造函数。这就算<!--
-->没有 `@JvmOverloads` 注解也有效。


## 编译期检查异常

如上所述，Kotlin 没有受检异常。
所以，通常 Kotlin 函数的 Java 签名不会声明抛出异常。
于是如果我们有一个这样的 Kotlin 函数：



```kotlin
fun writeToFile() {
    /*……*/
    throw IOException()
}
```


然后我们想要在 Java 中调用它并捕捉这个异常：


``` java
// Java
try {
  InteropKotlinKt.writeToFile();
}catch (IOException e) { // 编译错误：writeToFile() 未在 throws 列表中声明 IOException
}
```


因为 `writeToFile()` 没有声明 `IOException`，我们从 Java 编译器得到了一个报错消息。
为了解决这个问题，要在 Kotlin 中使用 [`@Throws`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-throws/index.html) 注解。


```kotlin
@Throws(IOException::class)
fun writeToFile() {
    throw IOException()
}
```


## 空安全性

当从 Java 中调用 Kotlin 函数时，没人阻止我们将 null 作为非空参数传递。
这就是为什么 Kotlin 给所有期望非空参数的公有函数生成运行时检测。

这样我们就能在 Java 代码里立即得到 `NullPointerException`。





