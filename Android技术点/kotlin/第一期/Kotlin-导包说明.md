
# Kotlin-导包说明

[TOC]

## 包

源文件通常以包声明开头:


```kotlin
package com.app.kotlin.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.text.*

// ……
```


## 默认导入

有多个包会默认导入到每个 Kotlin 文件中：

- [kotlin.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/index.html)  主要包含 kotlin 基本数据类型、数组和相关封装类
- [kotlin.annotation.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/index.html) 注解
- [kotlin.collections.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/index.html) 集合
- [kotlin.comparisons.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.comparisons/index.html)  集合排序、计算相关
- [kotlin.io.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/index.html) io 流
- [kotlin.ranges.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/index.html) 集合范围
- [kotlin.sequences.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/index.html) 集合序列相关
- [kotlin.text.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/index.html) 字符串操作、正则相关

根据目标平台还会导入额外的包：

- JVM:
  - java.lang.*
  - [kotlin.jvm.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/index.html)  主要包含JVM平台注解

- JS:    
  - [kotlin.js.*](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/index.html)

## 导入

除了默认导入之外，每个文件可以包含它自己的导入指令。
可以导入一个单独的名字，如.


```kotlin
import com.app.kotlin.simple // 现在 Message 可以不用限定符访问
```


也可以导入一个作用域下的所有内容（包、类、对象等）:


```kotlin
import com.app.kotlin.simple.* // “com.app.kotlin.simple”中的一切都可访问
```

关键字 `import` 并不仅限于导入类；也可用它来导入其他声明：

  * 顶层函数及属性；
  * 在对象声明
  * 枚举常量
  

### 关于导包

#### 在Java语言中

不建议使用 .* 进行导包 避免使用类重复，建议使用 使用全路径进行导包 如： ``import androidx.appcompat.app.AppCompatActivity`` 而不是 ``import androidx.appcompat.app.*``

如： Date 类 JDK 就存在 ``java.util.Date`` 和 ``java.sql.Date`` 而且他们具有相同的方法名，如果使用 .* 导包容易使用混淆。

#### 在Kotlin语言中

如果出现名字冲突，可以使用 *as*{: .keyword } 关键字在本地重命名冲突项来消歧义：


```kotlin
import org.example.Message // Message 可访问
import org.test.Message as testMessage // testMessage 代表“org.test.Message”
```



