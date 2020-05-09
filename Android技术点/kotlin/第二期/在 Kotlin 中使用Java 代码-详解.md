# 在 Kotlin 中调用 Java 代码

[TOC]

Kotlin 在设计时就考虑了 Java 互操作性。可以从 Kotlin 中自然地调用现存的 Java 代码，并且在 Java 代码中也可以<!--
-->很顺利地调用 Kotlin 代码。

几乎所有 Java 代码都可以使用而没有任何问题：



```kotlin
import java.util.*

fun demo(source: List<Int>) {
    val list = ArrayList<Int>()
    // “for”-循环用于 Java 集合：
    for (item in source) {
        list.add(item)
    }
    // 操作符约定同样有效：
    for (i in 0..source.size - 1) {
        list[i] = source[i] // 调用 get 和 set
    }
}
```



## 调用 Getter 和 Setter 

遵循 Java 约定的 getter 和 setter 的方法（名称以 `get` 开头的无参数方法和以 `set` 开头的单参数方法）在 Kotlin 中表示为属性。
`Boolean` 访问器方法（其中 getter 的名称以 `is` 开头而 setter 的名称以 `set` 开头）<!--
-->会表示为与 getter 方法具有相同名称的属性。
例如：



```kotlin
public static class User{
    private String userName;
    private long userId;
    private boolean isVip;
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public boolean isVip() {
        return isVip;
    }
    public void setVip(boolean vip) {
        isVip = vip;
    }
}
```

访问上述 Java 方法：

```kotlin
// 访问 getter 和 setter
val user = InteropMethed.User()
user.isVip = false
user.userId = 90
user.userName = "张三"
```



请注意，如果 Java 类只有一个 setter，它在 Kotlin 中不会作为属性可见，因为 Kotlin 目前不支持只写（set-only）属性。

## 返回 void 的方法

如果一个 Java 方法返回 void，那么从 Kotlin 调用时中返回 `Unit`。

万一有人使用其返回值，它将由 Kotlin 编译器在调用处赋值，

因为该值本身是预先知道的（是 `Unit`）。


## 将 Kotlin 中是关键字的 Java 标识符进行转义

一些 Kotlin 关键字在 Java 中是有效标识符：`in`、 `object`、 `is` 等等。
如果一个 Java 库使用了 Kotlin 关键字作为方法，你仍然可以通过反引号（`）字符转义它<!--
-->来调用该方法：


```kotlin
foo.`is`(bar)
```



## 空安全与平台类型

Java 中的任何引用都可能是 `null`，这使得 Kotlin 对来自 Java 的对象要求严格空安全是不现实的。

Java 声明的类型在 Kotlin 中会被特别对待并称为*平台类型*。对这种类型的空检测会放宽，
因此它们的安全保证与在 Java 中相同

示例：



```kotlin
val list = ArrayList<String>() // 非空（构造函数结果）
list.add("Item")
val size = list.size // 非空（原生 int）
val item = list[0] // 推断为平台类型（普通 Java 对象）
```



当我们调用平台类型变量的方法时，Kotlin 不会在编译时报告可空性错误，
但在运行时调用可能会失败，因为空指针异常或者 Kotlin 生成的<!--
-->阻止空值传播的断言：



```kotlin
item.substring(1) // 允许，如果 item == null 可能会抛出异常
```

平台类型是*不可标示*的，意味着不能在语言中明确地写下它们。
当把一个平台值赋值给一个 Kotlin 变量时，可以依赖类型推断（该变量会具有推断出的的平台类型，
如上例中 `item` 所具有的类型），或者我们可以选择我们期望的类型（可空或非空类型均可）：


```kotlin
val nullable: String? = item // 允许，没有问题
val notNull: String = item // 允许，运行时可能失败
```



如果我们选择非空类型，编译器会在赋值时触发一个断言。这防止 Kotlin 的非空变量保存<!--
-->空值。当我们把平台值传递给期待非空值等的 Kotlin 函数时，也会触发断言。
总的来说，编译器尽力阻止空值通过程序向远传播（尽管鉴于泛型的原因，有时这<!--
-->不可能完全消除）。

### 平台类型表示法

如上所述，平台类型不能在程序中显式表述，因此在语言中没有相应语法。
然而，编译器和 IDE 有时需要（在错误信息中、参数信息中等）显示他们，所以我们用<!--
-->一个助记符来表示他们：

* `T!` 表示“`T` 或者 `T?`”，
* `(Mutable)Collection<T>!` 表示“可以可变或不可变、可空或不可空的 `T` 的 Java 集合”，
* `Array<(out) T>!` 表示“可空或者不可空的 `T`（或 `T` 的子类型）的 Java 数组”

### 可空性注解

具有可空性注解的Java类型并不表示为平台类型，而是表示为实际可空或非空的
Kotlin 类型。编译器支持多种可空性注解，包括：

  * JetBrains
（`org.jetbrains.annotations` 包中的  `@Nullable` 和 `@NotNull`）
  * Android（`com.android.annotations` 和 `android.support.annotations`)
  * JSR-305（`javax.annotation`，详见下文）
  * FindBugs（`edu.umd.cs.findbugs.annotations`）
  * Eclipse（`org.eclipse.jdt.annotation`）
  * Lombok（`lombok.NonNull`）。


### 注解类型参数

可以标注泛型类型的类型参数，以便同时为其提供可空性信息。例如，考虑这些 Java 声明的注解：


```java
@NotNull
Set<@NotNull String> toSet(@NotNull Collection<@NotNull String> elements) { …… }
```



在 Kotlin 中可见的是以下签名：



```kotlin
fun toSet(elements: (Mutable)Collection<String>) : (Mutable)Set<String> { …… }
```



请注意 `String` 类型参数上的 `@NotNull` 注解。如果没有的话，类型参数会是平台类型：



```kotlin
fun toSet(elements: (Mutable)Collection<String!>) : (Mutable)Set<String!> { …… }
```



标注类型参数适用于面向 Java 8 或更高版本环境，并且要求可空性注解支持 `TYPE_USE` 目标（`org.jetbrains.annotations` 15 或以上版本支持）。

> 注：由于当前的技术限制，IDE 无法正确识别用作依赖的已编译 Java 库中类型参数上的这些注解。



## 已映射类型

Kotlin 特殊处理一部分 Java 类型。这样的类型不是“按原样”从 Java 加载，而是 _映射_ 到相应的 Kotlin 类型。
映射只发生在编译期间，运行时表示保持不变。
Java 的原生类型映射到相应的 Kotlin 类型（请记住[平台类型](#空安全与平台类型)）：

| **Java 类型** | **Kotlin 类型**  |
|---------------|------------------|
| `byte`        | `kotlin.Byte`    |
| `short`       | `kotlin.Short`   |
| `int`         | `kotlin.Int`     |
| `long`        | `kotlin.Long`    |
| `char`        | `kotlin.Char`    |
| `float`       | `kotlin.Float`   |
| `double`      | `kotlin.Double`  |
| `boolean`     | `kotlin.Boolean` |


一些非原生的内置类型也会作映射：

| **Java 类型** | **Kotlin 类型**  |
|---------------|------------------|
| `java.lang.Object`       | `kotlin.Any!`    |
| `java.lang.Cloneable`    | `kotlin.Cloneable!`    |
| `java.lang.Comparable`   | `kotlin.Comparable!`    |
| `java.lang.Enum`         | `kotlin.Enum!`    |
| `java.lang.Annotation`   | `kotlin.Annotation!`    |
| `java.lang.CharSequence` | `kotlin.CharSequence!`   |
| `java.lang.String`       | `kotlin.String!`   |
| `java.lang.Number`       | `kotlin.Number!`     |
| `java.lang.Throwable`    | `kotlin.Throwable!`    |


Java 的装箱类型映射到可空的 Kotlin 类型：

| **Java type**           | **Kotlin type**  |
|-------------------------|------------------|
| `java.lang.Byte`        | `kotlin.Byte?`   |
| `java.lang.Short`       | `kotlin.Short?`  |
| `java.lang.Integer`     | `kotlin.Int?`    |
| `java.lang.Long`        | `kotlin.Long?`   |
| `java.lang.Character`   | `kotlin.Char?`   |
| `java.lang.Float`       | `kotlin.Float?`  |
| `java.lang.Double`      | `kotlin.Double?`  |
| `java.lang.Boolean`     | `kotlin.Boolean?` |


请注意，用作类型参数的装箱原始类型映射到平台类型：
例如，`List<java.lang.Integer>` 在 Kotlin 中会成为 `List<Int!>`。

集合类型在 Kotlin 中可以是只读的或可变的，因此 Java 集合类型作如下映射：
（下表中的所有 Kotlin 类型都驻留在 `kotlin.collections`包中）:

| **Java 类型** | **Kotlin 只读类型**  | **Kotlin 可变类型** | **加载的平台类型** |
|---------------|------------------|----|----|
| `Iterator<T>`        | `Iterator<T>`        | `MutableIterator<T>`            | `(Mutable)Iterator<T>!`            |
| `Iterable<T>`        | `Iterable<T>`        | `MutableIterable<T>`            | `(Mutable)Iterable<T>!`            |
| `Collection<T>`      | `Collection<T>`      | `MutableCollection<T>`          | `(Mutable)Collection<T>!`          |
| `Set<T>`             | `Set<T>`             | `MutableSet<T>`                 | `(Mutable)Set<T>!`                 |
| `List<T>`            | `List<T>`            | `MutableList<T>`                | `(Mutable)List<T>!`                |
| `ListIterator<T>`    | `ListIterator<T>`    | `MutableListIterator<T>`        | `(Mutable)ListIterator<T>!`        |
| `Map<K, V>`          | `Map<K, V>`          | `MutableMap<K, V>`              | `(Mutable)Map<K, V>!`              |
| `Map.Entry<K, V>`    | `Map.Entry<K, V>`    | `MutableMap.MutableEntry<K,V>` | `(Mutable)Map.(Mutable)Entry<K, V>!` |


Java 的数组述映射：

| **Java 类型** | **Kotlin 类型**  |
|---------------|------------------|
| `int[]`       | `kotlin.IntArray!` |
| `String[]`    | `kotlin.Array<(out) String>!` |


注意：这些 Java 类型的静态成员不能在相应 Kotlin 类型的伴生对象中直接访问。要调用它们，请使用 Java 类型的完整限定名，例如 `java.lang.Integer.toHexString(foo)`。

## Kotlin 中的 Java 泛型

Kotlin 的泛型与 Java 有点不同。当将 Java 类型导入 Kotlin 时，我们会执行一些转换：

* Java 的通配符转换成类型投影，

  * `Foo<? extends Bar>` 转换成 `Foo<out Bar!>!`，
  * `Foo<? super Bar>` 转换成 `Foo<in Bar!>!`；

* Java的原始类型转换成星投影，

  * `List` 转换成 `List<*>!`，即 `List<out Any?>!`。

和 Java 一样，Kotlin 在运行时不保留泛型，即对象不携带传递到他们构造器中的那些类型参数的实际类型。

即 `ArrayList<Integer>()` 和 `ArrayList<Character>()` 是不能区分的。
这使得执行 `is` -检测不可能照顾到泛型。
Kotlin 只允许 `is` -检测星投影的泛型类型：



```kotlin
val a = ArrayList<String>(2)

if (a is List<Int>){  // 错误：无法检测它是否真的是一个 Int 列表
    println("a is List<Int>")
}
if (a is List<*>){  
    // OK：不保证列表的内容
    println("a is List<*>")
}
```



### Java 数组

与 Java 不同，Kotlin 中的数组是`不型变`的。这意味着 Kotlin 不允许我们把一个 `Array<String>` 赋值给一个 `Array<Any>`，
从而避免了可能的运行时故障。

Kotlin 也禁止我们把一个子类的数组当做超类的数组传递给 Kotlin 的方法，
但是对于 Java 方法，这是允许的（通过 `Array<(out) String>!` 这种形式的 平台类型

> Java 平台上，数组会使用原生数据类型以避免装箱/拆箱操作的开销。

由于 Kotlin 隐藏了这些实现细节，因此需要一个变通方法来与 Java 代码进行交互。

对于每种原生类型的数组都有一个特化的类（`IntArray`、 `DoubleArray`、 `CharArray` 等等）来处理这种情况。
它们与 `Array` 类无关，并且会编译成 Java 原生类型数组以获得最佳性能。

假设有一个接受 int 数组索引的 Java 方法：


``` java
public void removeIndices(int[] indices) {
    
}
```

在 Kotlin 中你可以这样传递一个原生类型的数组：



```kotlin
val array = intArrayOf(0, 1, 2, 3)
interop.removeIndices(array)  // 将 int[] 传给方法
```


当编译为 JVM 字节代码时，编译器会优化对数组的访问，这样就不会引入任何开销：



```kotlin
val array = arrayOf(1, 2, 3, 4)
array[1] = array[1] * 2 // 不会实际生成对 get() 和 set() 的调用
for (x in array) { // 不会创建迭代器
    print(x)
}
```


即使当我们使用索引定位时，也不会引入任何开销：



```kotlin
for (i in array.indices) {// 不会创建迭代器
    array[i] += 2
}
```



最后，`in` -检测也没有额外开销：


```kotlin
if (i in array.indices) { // 同 (i >= 0 && i < array.size)
    print(array[i])
}
```



## Java 可变参数

Java 类有时声明一个具有可变数量参数（varargs）的方法来使用索引：

``` java
public void removeIndicesVarArg(int... indices) {
    //....
}
```

在这种情况下，你需要使用展开运算符 `*` 来传递 `IntArray`：


```kotlin
val arr = intArrayOf(0, 1, 2, 3)
interop.removeIndicesVarArg(*arr) // 通过 *传递可变参数

//interop.removeIndicesVarArg(null)   // 编译报错
```

> 注意：目前无法传递 `null` 给一个声明为可变参数的方法。

## 操作符

由于 Java 无法标记用于运算符语法的方法，Kotlin 允许<!--
-->具有正确名称和签名的任何 Java 方法作为运算符重载和其他约定（`invoke()` 等）使用。
不允许使用中缀调用语法调用 Java 方法。


## 编译期检查异常

在 Kotlin 中，所有异常都是非受检的，这意味着编译器不会强迫你捕获其中的任何一个。
因此，当你调用一个声明受检异常的 Java 方法时，Kotlin 不会强迫你做任何事情：

Java 使用 `IO`尝试读取文件 必须使用 `try`、 `catch` 捕获显示抛出异常

```java
File file = new File("");
FileInputStream fis = null;
try {
    fis = new FileInputStream(file);
    fis.read();
} catch (FileNotFoundException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```
Kotlin 不会强制要求捕获异常

```kotlin
val file = File("")
val ins = FileInputStream(file)
ins.read()
```



## 对象方法

当 Java 类型导入到 Kotlin 中时，类型 `java.lang.Object` 的所有引用都成了 `Any`。
而因为 `Any` 不是平台指定的，它只声明了 `toString()`、`hashCode()` 和 `equals()` 作为其成员，
所以为了能用到 `java.lang.Object` 的其他成员，Kotlin 要用到 扩展函数



### wait()/notify()

类型 `Any` 的引用没有提供 `wait()` 与 `notify()` 方法。通常不鼓励使用它们，而建议使用 `java.util.concurrent`。
如果确实需要调用这两个方法的话，那么可以将引用转换为 `java.lang.Object`：


```kotlin
(foo as java.lang.Object).wait()
```


### getClass()

要取得对象的 Java 类，请在 类引用 上使用 `java` 扩展属性：


```kotlin
val fooClass = foo::class.java
```


你也可以使用 `javaClass` 扩展属性：


```kotlin
val fooClass = foo.javaClass
```


### clone()

要覆盖 `clone()`，需要继承 `kotlin.Cloneable`：



```kotlin
class SimpleClone:Cloneable{
    override fun clone(): Any {
        return SimpleClone()
    }
}
```


### finalize()

要覆盖 `finalize()`，所有你需要做的就是简单地声明它，而不需要 `override` 关键字：


```kotlin
class SimpleClone:Cloneable{
   
    protected fun finalize(){
    }
}
```


根据 Java 的规则，`finalize()` 不能是 `private` 的。


## 访问静态成员

Java 类的静态成员会形成该类的“伴生对象”。我们无法将这样的“伴生对象”作为值来传递，
但可以显式访问其成员，例如：


```kotlin
if (Character.isLetter(a)) {
}
```


要访问[已映射](#已映射类型)到 Kotlin 类型的 Java 类型的静态成员，请使用 Java 类型的完整限定名：`java.lang.Integer.bitCount(foo)`。

## Java 反射

Java 反射适用于 Kotlin 类，反之亦然。如上所述，你可以使用 `instance::class.java`,
`ClassName::class.java` 或者 `instance.javaClass` 通过 `java.lang.Class` 来进入 Java 反射。

其他支持的情况包括为一个 Kotlin 属性获取一个 Java 的 getter/setter 方法或者幕后字段、为一个 Java 字段获取一个 `KProperty`、为一个 `KFunction` 获取一个 Java 方法或者构造函数，反之亦然。

## SAM 转换

关于 SAM接口请参考 《Java 8 SAM 接口详解》

就像 Java 8 一样，Kotlin 支持 SAM 转换。

这意味着 Kotlin 函数字面值可以被自动的转换成<!--
-->只有一个非默认方法的 Java 接口的实现，只要这个方法的参数类型<!--
-->能够与这个 Kotlin 函数的参数类型相匹配。

你可以这样创建 SAM 接口的实例：


```kotlin
val runnable = Runnable {
    println("This runs in a runnable")
}
```


.....或者


```kotlin
val executor = Executors.newSingleThreadExecutor()

// Java 签名：void execute(Runnable command)
executor.execute {
    println("This runs in a thread pool")
}
```


如果 Java 类有多个接受函数式接口的方法，那么可以通过使用<!--
-->将 lambda 表达式转换为特定的 SAM 类型的适配器函数来选择需要调用的方法。这些适配器函数也会按需<!--
-->由编译器生成：



```kotlin
executor.execute(Runnable { println("This runs in a thread pool") })
```



> 注意 
1. SAM 转换只适用于接口，而不适用于抽象类，即使这些抽象类也只有一个抽象方法。
2. 此功能只适用于 Java 互操作；因为 Kotlin 具有合适的函数类型，所以不需要将函数自动转换<!--
为 Kotlin 接口的实现，因此不受支持。

## 在 Kotlin 中使用 JNI

要声明一个在本地（C 或 C++）代码中实现的函数，你需要使用 `external` 修饰符来标记它：

和 Java 中的 `native` 方式一样

```kotlin
external fun foo(x: Int): Double
```

其余的过程与 Java 中的工作方式完全相同。
