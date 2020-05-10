[TOC]

# AOP

## AOP概念
AOP(Aspect Oriented Programming)就是把我们在某个方面的功能提出来与一批对象进行隔离，这样与一批对象之间降低了耦合性，可以就某个功能进行编程。


AOP只是一种思想的统称，实现这种思想的方法有挺多。AOP通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，提高开发效率。


Aop思想可以说成插桩，在类的编译期间中干一些东西，下图看一个图就明白了，主要关注一下AspectJ插入时机
![image](https://note.youdao.com/yws/public/resource/0e10f5b1f14b51263a8b0f1f3ef7d47d/xmlnote/1A4CAAD230184F4B934C4BB039B8D090/2885)



## AOP 应用场景
日志记录，性能统计，安全控制，事务处理，异常处理等等。

## AOP 主要目标
将日志记录，性能统计，安全控制，事务处理，异常处理等代码从业务逻辑代码中划分出来，通过对这些行为的分离，我们希望可以将它们独立到非指导业务逻辑的方法中，进而改变这些行为的时候不影响业务逻辑的代码。


![image](https://note.youdao.com/yws/public/resource/0e10f5b1f14b51263a8b0f1f3ef7d47d/xmlnote/98FF79FD0E4C43E9B510BF7446F5221E/2823)

上图是一个APP模块结构示例，按照照OOP的思想划分为“视图交互”，“业务逻辑”，“网络”等三个模块，而现在假设想要对所有模块的每个方法耗时（性能监控模块）进行统计。这个性能监控模块的功能就是需要横跨并嵌入众多模块里的，这就是典型的AOP的应用场景。

AOP的目标是把这些横跨并嵌入众多模块里的功能（如监控每个方法的性能） 集中起来，放到一个统一的地方来控制和管理。如果说，OOP如果是把问题划分到单个模块的话，那么AOP就是把涉及到众多模块的某一类问题进行统一管理。




## AOP 实现方式
- AspectJ: 一个 JavaTM 语言的面向切面编程的无缝扩展（适用Android）。
- Javassist for Android: 用于字节码操作的知名 java 类库 Javassist 的 Android 平台移植版。
- DexMaker: Dalvik 虚拟机上，在编译期或者运行时生成代码的 Java API。
- ASMDEX: 一个类似 ASM 的字节码操作库，运行在Android平台，操作Dex字节码。
- 
  本篇的主角就是AspectJ，下面就来看看AspectJ方式的AOP如何在Android开发中进行使用吧。

# Android中使用AspectJ
代表项目：Hugo(打印每个方法的执行时间) sa-sdk-android（全埋点技术）。
AspectJ 实际上是就突出了AOP 的思想。在此我们借助AspectJ 开源库实现面向切面编程的一个示例。

▍原理

AspectJ 意思就是Java的Aspect，Java的AOP。它的核心是ajc（编译器 aspectjtools）和 weaver（织入器 aspectjweaver）。

ajc编译器：基于Java编译器之上的，它是用来编译.aj文件，aspectj在Java编译器的基础上增加了一些它自己的关键字和方法。因此，ajc也可以编译Java代码。

weaver织入器：为了在java编译器上使用AspectJ而不依赖于Ajc编译器，aspectJ 5出现了 @AspectJ，使用注释的方式编写AspectJ代码，可以在任何Java编译器上使用。 由于AndroidStudio默认是没有ajc编译器的，所以在Android中使用@AspectJ来编写。它在代码的编译期间扫描目标程序，根据切点（PointCut）匹配,将开发者编写的Aspect程序编织（Weave）到目标程序的.class文件中，对目标程序作了重构（重构单位是JoinPoint），目的就是建立目标程序与Aspect程序的连接（获得执行的对象、方法、参数等上下文信息），从而达到AOP的目的。


## AOP术语
▍**切面（Aspect）**：一个关注点的模块化，这个关注点实现可能另外横切多个对象。其实就是共有功能的实现。如日志切面、权限切面、事务切面等。

▍**通知（Advice）**：是切面的具体实现。以目标方法为参照点，根据放置的地方不同，可分为

- 前置通知（Before）、
- 后置通知（AfterReturning）、
- 异常通知（AfterThrowing）、
- 最终通知（After）
- 环绕通知（Around）5种。
  在实际应用中通常是切面类中的一个方法，具体属于哪类通知由配置指定的。

▍**切入点（Pointcut）**：用于定义通知应该切入到哪些连接点上。不同的通知通常需要切入到不同的连接点上，这种精准的匹配是由切入点的正则表达式来定义的。 连接点（JoinPoint）：就是程序在运行过程中能够插入切面的地点。例如，方法调用、异常抛出或字段修改等。

▍**目标对象（Target Object）**：包含连接点的对象，也被称作被通知或被代理对象。这些对象中已经只剩下干干净净的核心业务逻辑代码了，所有的共有功能等代码则是等待AOP容器的切入。

▍**AOP代理（AOP Proxy）**：将通知应用到目标对象之后被动态创建的对象。可以简单地理解为，代理对象的功能等于目标对象的核心业务逻辑功能加上共有功能。代理对象对于使用者而言是透明的，是程序运行过程中的产物。

▍**编织（Weaving）**：将切面应用到目标对象从而创建一个新的代理对象的过程。这个过程可以发生在编译期、类装载期及运行期，当然不同的发生点有着不同的前提条件。譬如发生在编译期的话，就要求有一个支持这种AOP实现的特殊编译器（如AspectJ编译器）；

发生在类装载期，就要求有一个支持AOP实现的特殊类装载器；只有发生在运行期，则可直接通过Java语言的反射机制与动态代理机制来动态实现（如摇一摇）。

▍**引入（Introduction）**：添加方法或字段到被通知的类。

## 切点表达式
▍基本模式

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)
```

这里问号表示当前项可以有也可以没有，其中各项的语义如下：

- modifiers-pattern：方法的可见性，如public，protected；
- ret-type-pattern：方法的返回值类型，如int，void等；
- declaring-type-pattern：方法所在类的全路径名，如com.spring.Aspect；
- name-pattern：方法名类型，如buisinessService()；
- param-pattern：方法的参数类型，如java.lang.String；
- throws-pattern：方法抛出的异常类型，如java.lang.Exception；

为了帮助大家能够立即基本表达式的使用示例如下：

```
execution(public * com.spring.service.BusinessObject.businessService(java.lang.String,..))
```

▍通配符：

- *通配符，该通配符主要用于匹配单个单词，或者是以某个词为前缀或后缀的单词。
- ..通配符，该通配符表示0个或多个项，主要用于declaring-type-pattern和param-pattern中，如果用于declaring-type-pattern中，则表示匹配当前包及其子包，如果用于param-pattern中，则表示匹配0个或多个参数。
- +表示自身以及子类


详细参考 [Spring AOP切点表达式用法总结](https://www.cnblogs.com/zhangxufeng/p/9160869.html)




## AspectJ 集成
在Android 项目中使用AspectJ 可以使用如下方式。当然也可以使用集成好的方式如：

上海沪江团队的 gradle_plugin_android_aspectjx 一个基于AspectJ并在此基础上扩展出来可应用于Android开发平台的AOP框架，可作用于java源码，class文件及jar包，同时支持kotlin的应用。
在这里我就使用最原始的方式集成。

▍项目根目录gradle配置

```
buildscript {
    repositories {
        //省略
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.3.2'
        classpath 'org.aspectj:aspectjtools:1.8.9'
        classpath 'org.aspectj:aspectjweaver:1.8.9'
    }
}
```
▍ 模块gradle配置

```
apply plugin: 'com.android.application'
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main

android {
    // 省略
}

final def log = project.logger
final def variants = project.android.applicationVariants

variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
    }

    JavaCompile javaCompile = variant.javaCompile
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.8",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
}
dependencies {
    //省略
    implementation files('libs/aspectjrt.jar')
}
```

▍ libs 导入 jar 包

这里提供了下载地址方便大家下载使用
[aspectj-1.8.10](https://www.eclipse.org/downloads/download.php?file=/tools/aspectj/aspectj-1.8.10.jar)


# AOP 实践

## 网络监测
通常的网络监测我们都是使用 if...else 形式进行判断是否有网络，如果有网络的话我们执行购买操作，否则弹出Toast 提示用户。通常情况下在项目当中会有很多的入口方法，可能需要写多个网络监测判断，由此造成代码的臃肿，职责不单一。那么下面我们针对具体问题做出具体的实践。


▍定义注解

```
@Retention(RetentionPolicy.RUNTIME) //运行期
@Target(ElementType.METHOD) // 目标
public @interface CheckNet {
    String value();
}
```

▍定义切面

```
@Aspect
public class BehaviorAspect {

    /**
     * 找到处理的切点
     * * *(..) 处理所有方法
     */
    @Pointcut("execution(@com.szy.lesson_aop.annotation.CheckNet * *(..))")
    public void checkNetBehavior() {

    }
    /**
     * 处理切面
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("checkNetBehavior()")
    public Object weaveJointPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        CheckNet checkNet = methodSignature.getMethod().getAnnotation(CheckNet.class);
        if (checkNet!= null){
            //如何获取Context 对象
            Object object = joinPoint.getThis();// 获取当前切点所在的类
            Context context = ContextWrapper.getContext(object);
            if (context != null){
                if (!NetUtils.isNetWorkAvailable(context)){
                    Toast.makeText(context,"请检查网络",Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        }

        return joinPoint.proceed();
    }
}
```

▍使用
```
@CheckNet("分析")
public void clickShake(View view) {
    Toast.makeText(this,"调转详情页",Toast.LENGTH_LONG).show();
}


// NetUtils --> 大家记得在清单文件中声明
public class NetUtils {
    public static boolean isNetWorkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if (networkInfos != null && networkInfos.length > 0){
                for (int i = 0 ; i< networkInfos.length; i++){
                    if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

```




