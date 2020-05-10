#### 什么是注解 & 它和注释的区别？

> 注解就是用来描述包、类、成员变量、方法或者参数的元数据，注解本身也是一个类

> 元注解顾名思义我们可以理解为注解的注解，它是作用在注解中，方便我们使用注解实现想要的功能。元注解分别有@Retention、 @Target、 @Document、 @Inherited和@Repeatable（JDK1.8加入）五种。

> 元注解具体功能https://www.cnblogs.com/1573hj/p/10650569.html

> 注解的工作机制是什么？

```
  //举例
  @Test("test")
  public class AnnotationTest {
      public void test(){
      }
  }
  链接：https://blog.csdn.net/bobozai86/article/details/103704833
```

> java源码到class字节码是由编译器完成的，编译器会对java源码进行解析并生成class文件，而注解也是在编译时由编译器进行处理，编译器会对注解符号处理并附加到class结构中，根据jvm规范，class文件结构是严格有序的格式，唯一可以附加信息到class结构中的方式就是保存到class结构的attributes属性中。我们知道对于类、字段、方法，在class结构中都有自己特定的表结构，而且各自都有自己的属性，而对于注解，作用的范围也可以不同，可以作用在类上，也可以作用在字段或方法上，这时编译器会对应将注解信息存放到类、字段、方法自己的属性上。

  在我们的AnnotationTest类被编译后，在对应的AnnotationTest.class文件中会包含一个RuntimeVisibleAnnotations属性，由于这个注解是作用在类上，所以此属性被添加到类的属性集上。即Test注解的键值对value=test会被记录起来。而当JVM加载AnnotationTest.class文件字节码时，就会将RuntimeVisibleAnnotations属性值保存到AnnotationTest的Class对象中，于是就可以通过AnnotationTest.class.getAnnotation(Test.class)获取到Test注解对象，进而再通过Test注解对象获取到Test里面的属性值

#### 如何解析注解？

- 解析注解分为两类：类注解解析，方法注解解析

- 类注解解析主要分为以下三步：

  **第一步：**使用类加载器加载类；

  **第二步：**找到类上面的注解；

  **第三步：**获取注解实例。

- 方法注解解析 

  - 解析方法上面的注解和解析类注解差不多类似，只是获取注解的时候需要使用不同的方法，并且使用不同的接受对象去接受 

- 参考链接 ：https://blog.csdn.net/ILV_XJ/article/details/103340405