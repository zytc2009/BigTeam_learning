### JVM部分：

运行时数据区域（内存模型）
垃圾回收机制
垃圾回收算法
Minor GC和Full GC触发条件
各垃圾回收器的特点及区别
双亲委派模型
JDBC和双亲委派模型关系
JVM中内存分配机制
jvm虚拟机，堆和栈的结构
jvm虚拟机，堆和栈的结构，栈帧，JMM
GC回收算法
JVM,JMM,java加载对象的步骤，classLoader,GC回收算法



### Java基础部分：

HashMap和ConcurrentHashMap区别
ConcurrentHashMap的数据结构
高并发HashMap的环是如何产生的？
Atomic类如何保证原子性（CAS操作）
为什么要使用线程池？
核心线程池ThreadPoolExecutor的参数
如何控制线程池线程的优先级
Boolean占几个字节
HashMap和HashTable，ConcurrentHashMap的差别
ConcurrentHashMap的1.7和1.8版本差异
LinkedHashMap实现原理
ArrayDeque实现原理
Java Object中有哪些方法？equals和hashCode方法什么时候会被重写？
Java中悲观锁与乐观锁，举例并说明其相关实现？
CAS实现原理
Synchronized底层原理，java锁机制
synchronized和ReentrantLock的实现差异
synchronized对不同方法的修饰，持有的锁对象的差异
ThreadLocal实现原理
volatile用法
说一下对于泛型的理解
泛型擦除
什么情况下不会出现泛型擦除
说一下对于线程安全的理解，Java中线程安全与不安全的集合类有那些？
Java的垃圾回收机制
Java类加载机制
平时项目中对于锁的应用
Java线程池默认提供了哪些类型？分别适合什么场景？
Java线程中interrupt()、interrupted()和isInterrupted()分别代表什么意思？
动态代理与静态代理
SparyArray和ArrayMap的实现原理？
stack栈的特点，自定义stack结构
java容器，hashmap和hashtable区别，hashmap原理，扩容流程，扰动算法的优势
ArrayList和LinkendList区别，List泛型擦除，为什么反射能够在ArrayList< String >中添加int类型
线程安全的单例模式有哪几种
java泛型，协变和逆变
基础类型字节，汉字占几个字节，线程和进程
hashmap为什么大于8才转化为红黑树，加载因子为什么是0.75
线程内存模型，线程间通信

finally 能否用return，可以，会生效

4中引用：强，软，弱，虚

**重写equals方法的要求：**1、自反性：对于任何非空引用x，x.equals(x)应该返回true。2、对称性：对于任何引用x和y，如果x.equals(y)返回true，那么y.equals(x)也应该返回true。3、传递性：对于任何引用x、y和z，如果x.equals(y)返回true，y.equals(z)返回true，那么x.equals(z)也应该返回true。4、一致性：如果x和y引用的对象没有发生变化，那么反复调用x.equals(y)应该返回同样的结果。5、非空性：对于任意非空引用x，x.equals(null)应该返回false。

**类的双亲委托机制**：当某个特定的类加载器在接收到加载类的请求时，首先将该加载任务发送给父类加载器，若父类加载器仍有父类，则继续向上追溯，直到最高级。如果最高级父类能够加载到该类，则成功返回，否则由其子类进行加载。以此类推，如果到最后一个子类还不能成功加载，则抛出一个异常。作用：可以保证java核心库或第三方库的安全（防止低一级加载器加载的类覆盖高级加载器加载的类）