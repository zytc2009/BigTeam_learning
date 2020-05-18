## 组件消息总线modular-event

### 解决不同组件定义了重名消息的问题

其实这个问题还是比较好解决的，实现的方式就是采用两级HashMap的方式解决。第一级HashMap的构建以ModuleName作为Key，第二级HashMap作为Value；第二级HashMap以消息名称EventName作为Key，LiveData作为Value。查找的时候先用组件名称ModuleName在第一级HashMap中查找，如果找到则用消息名EventName在第二级HashName中查找。整个结构如下图所示：

![img](https:////upload-images.jianshu.io/upload_images/13818615-f695e8d648ddc657?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

消息总线结构

### 对消息总线的约束

我们希望消息总线框架有以下约束：

1. 只能订阅和发送在组件中预定义的消息。换句话说，使用者不能发送和订阅临时消息。
2. 消息的类型需要在定义的时候指定。
3. 定义消息的时候需要指定属于哪个组件。

### 如何实现这些约束

1. 在消息定义文件上使用注解，定义消息的类型和消息所属Module。
2. 定义注解处理器，在编译期间收集消息的相关信息。
3. 在编译器根据消息的信息生成调用时需要的interface，用接口约束消息发送和订阅。
4. 运行时构建基于两级HashMap的LiveData存储结构。
5. 运行时采用interface+动态代理的方式实现真正的消息订阅和发送。

整个流程如下图所示：

![img](https:////upload-images.jianshu.io/upload_images/13818615-4dbca6868ea65828?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

实现流程

### 消息总线modular-event的结构

- **modular-event-base**：定义Anotation及其他基本类型
- **modular-event-core**：modular-event核心实现
- **modular-event-compiler**：注解处理器
- **modular-event-plugin**：Gradle Plugin

#### Anotation

- **@ModuleEvents**：消息定义



```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ModuleEvents {
    String module() default "";
}
```

- **@EventType**：消息类型



```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface EventType {
    Class value();
}
```

#### 消息定义

通过@ModuleEvents注解一个定义消息的Java类，如果@ModuleEvents指定了属性module，那么这个module的值就是这个消息所属的Module，如果没有指定属性module，则会把定义消息的Java类所在的包的包名作为消息所属的Module。

在这个消息定义java类中定义的消息都是public static final String类型。可以通过@EventType指定消息的类型，@EventType支持java原生类型或自定义类型，如果没有用@EventType指定消息类型，那么消息的类型默认为Object，下面是一个消息定义的示例：



```java
//可以指定module，若不指定，则使用包名作为module名
@ModuleEvents()
public class DemoEvents {

    //不指定消息类型，那么消息的类型默认为Object
    public static final String EVENT1 = "event1";

    //指定消息类型为自定义Bean
    @EventType(TestEventBean.class)
    public static final String EVENT2 = "event2";

    //指定消息类型为java原生类型
    @EventType(String.class)
    public static final String EVENT3 = "event3";
}
```

#### interface自动生成

我们会在modular-event-compiler中处理这些注解，一个定义消息的Java类会生成一个接口，这个接口的命名是EventsDefineOf+消息定义类名，例如消息定义类的类名为DemoEvents，自动生成的接口就是EventsDefineOfDemoEvents。消息定义类中定义的每一个消息，都会转化成接口中的一个方法。使用者只能通过这些自动生成的接口使用消息总线。我们用这种巧妙的方式实现了对消息总线的约束。前文提到的那个消息定义示例DemoEvents.java会生成一个如下的接口类：



```java
package com.sankuai.erp.modularevent.generated.com.meituan.jeremy.module_b_export;

public interface EventsDefineOfDemoEvents extends com.sankuai.erp.modularevent.base.IEventsDefine {
  com.sankuai.erp.modularevent.Observable<java.lang.Object> EVENT1();

  com.sankuai.erp.modularevent.Observable<com.meituan.jeremy.module_b_export.TestEventBean> EVENT2(
      );

  com.sankuai.erp.modularevent.Observable<java.lang.String> EVENT3();
}
```

关于接口类的自动生成，我们采用了[square/javapoet](https://github.com/square/javapoet)来实现，网上介绍JavaPoet的文章很多，这里就不再累述。

#### 使用动态代理实现运行时调用

有了自动生成的接口，就相当于有了一个壳，然而壳下面的所有逻辑，我们通过动态代理来实现，简单介绍一下代理模式和动态代理：

- **代理模式**：
   给某个对象提供一个代理对象，并由代理对象控制对于原对象的访问，即客户不直接操控原对象，而是通过代理对象间接地操控原对象。
- **动态代理**：
   代理类是在运行时生成的。也就是说Java编译完之后并没有实际的class文件，而是在运行时动态生成的类字节码，并加载到JVM中。

在动态代理的InvocationHandler中实现查找逻辑：

1. 根据interface的typename得到ModuleName。
2. 调用的方法的methodname即为消息名。
3. 根据ModuleName和消息名找到相应的LiveData。
4. 完成后续订阅消息或者发送消息的流程。

消息的订阅和发送可以用链式调用的方式编码：

- 订阅消息



```java
ModularEventBus
        .get()
        .of(EventsDefineOfModuleBEvents.class)
        .EVENT1()
        .observe(this, new Observer<TestEventBean>() {
            @Override
            public void onChanged(@Nullable TestEventBean testEventBean) {
                Toast.makeText(MainActivity.this, "MainActivity收到自定义消息: " + testEventBean.getMsg(),
                        Toast.LENGTH_SHORT).show();
            }
        });
```

- 发送消息



```java
ModularEventBus
        .get()
        .of(EventsDefineOfModuleBEvents.class)
        .EVENT1()
        .setValue(new TestEventBean("aa"));
```

#### 订阅和发送的模式

- 订阅消息的模式

1. **observe**：生命周期感知，onDestroy的时候自动取消订阅。
2. **observeSticky**：生命周期感知，onDestroy的时候自动取消订阅，Sticky模式。
3. **observeForever**：需要手动取消订阅。
4. **observeStickyForever**：需要手动取消订阅，Sticky模式。

- 发送消息的模式

1. **setValue**：主线程调用。
2. **postValue**：后台线程调用。



#### 相关文章

1.[Android组件化方案及组件消息总线modular-event实战](https://www.jianshu.com/p/ad590a8b3a00)