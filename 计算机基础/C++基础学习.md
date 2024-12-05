[toc]

### 一、C++基础

#### 1、面向对象的三大特性：封装、继承、多态

**1)封装：**
封装是实现面向对象程序设计的第一步，封装就是将数据或函数等集合在一个个的单元中（我们称之为类）

封装的意义在于保护或者防止代码（数据）被我们无意中破坏。

2). 继承：

继承主要实现重用代码，节省开发时间。

子类可以继承父类的一些东西。

a.**公有继承(public)**公有继承的特点是基类的公有成员和保护成员作为派生类的成员时，它们都保持原有的状态（基类的私有成员仍然是私有的，不能被这个派生类的子类所访问）。

b.**私有继承(private)**私有继承的特点是基类的公有成员和保护成员都作为派生类的私有成员（并且不能被这个派生类的子类所访问）。要**谨慎使用**。

 private继承可以造成 empty base最优化。

```c++
class Empty{} //sizeof(Empty) == 1
class HoldsAsInt ： private Empty{
private:
	int x;
}
//sizeof(HoldsAsInt) == 4
```

c.**保护继承(protected)**保护继承的特点是基类的所有公有成员和保护成员都成为派生类的保护成员（并且只能被它的派生类成员函数或友元访问，基类的私有成员仍然是私有的）。

这里特别提一下虚继承。虚继承是解决C++多重继承问题（其一，浪费存储空间；第二，存在二义性问题）的一种手段。比如菱形继承，典型的应用就是 iostream, 其继承于 istream 和 ostream，而 istream 和 ostream 又继承于 ios。

d.**多重继承**

**二义性**：继承的两个父类有同样的方法，会导致编译报错

**二义性消除**：改为虚拟继承

```c++
class A{};
class B: virtual public A{};
class C: virtual public A{};
class D: public B, public C{};
D d; A *pd = &d;
```

B和C继承A都拥有A的一份复制，D就拥有两份，一旦向上转型就会报错

**多继承的构造顺序**： 基类构造按继承顺序，非虚拟基类按构造顺序，成员按声明顺序，最后类的构造

**3).多态：**

多态是指通过基类的指针或者引用，在运行时动态调用实际绑定对象函数的行为。与之相对应的编译时绑定函数称为静态绑定。多态是设计模式的基础，多态是框架的基础。



#### 2、类的访问权限：private、protected、public、final

final 关键字用于虚函数时可以防止虚函数被子类重写，用于类时可以防止类被继承。

#### 3、类的构造函数、析构函数、赋值函数、拷贝函数

**C++自行生成的成员函数**：C++98的4个特种成员函数：默认构造函数(无参)，[析构函数](https://so.csdn.net/so/search?q=析构函数&spm=1001.2101.3001.7020)、复制构造函数、复制赋值函数。

```c++
class Widget {
public:
	Widget();//默认构造函数
	~Widget(); //默认析构函数
	Widget(const Widget& w); //默认复制构造函数
	Widget& operator=(const Widget& w);//默认复制赋值函数
};
```

C++11之后又加入了两个新的特种函数：

```c++
Widget(Widget&& w);//移动构造函数
Widget& operator=(Widget&& w);//移动赋值运算符
```

赋值操作符必须返回一个reference to *this， 如果发生自我赋值，可以先做个证同测试

operator=不仅不具备自我赋值安全性，也不具备异常安全性

复制对象不要忘记每一个成员及所有base class成员

绝对不要在构造和析构过程中调用 virtual 函数

**移动和复制**：
1、在支持移动操作的成员上执行**移动**操作；
2、在不支持移动操作的成员上执行**复制**操作。
若发生了复制操作，则移动操作就不会在已有声明的前提下被生成。

两种复制操作是独立的：声明了其中一个不会阻止编译器生成另一个；
两种移动操作不彼此独立：声明了其中一个会阻止编译器生成另一个。

**一旦显示声明了复制操作，此类便不再生成移动操作，反之亦然**。

若声明了复制构造函数，构造方法必须实现

移动操作的生成条件：
1、该类未声明任何复制操作；
2、该类未声明任何移动操作；
3、该类未声明任何析构函数。 （TODO:待验证)


#### 4、移动构造函数与拷贝构造函数对比

#### 5、深拷贝与浅拷贝的区别

- 深拷贝是指拷贝后对象的逻辑状态相同，而浅拷贝是指拷贝后对象的物理状态相同；默认拷贝构造函数属于浅拷贝。
- 当系统中有成员指代了系统中的资源时，需要深拷贝。比如指向了动态内存空间，打开了外存中的文件或者使用了系统中的网络接口等。如果不进行深拷贝，比如动态内存空间，可能会出现多次被释放的问题。是否需要定义拷贝构造函数的原则是，是类是否有成员调用了系统资源，如果定义拷贝构造函数，一定是定义深拷贝，否则没有意义。

#### 6、空类有哪些函数？空类的大小

​     大小为1

#### 7、内存分区：全局区、堆区、栈区、常量区、代码区

​    float遵从的是IEEE R32.24 ,而double 遵从的是R64.53

​    无论是单精度还是双精度在存储中都分为三个部分：

1). 符号位(Sign) : 0代表正，1代表为负
2). 指数位（Exponent）:用于存储科学计数法中的指数数据，并且采用移位存储
3). 尾数部分（Mantissa）：尾数部分

#### 8、C++与C的区别

1). C++是C的超集;
2). C是一个结构化语言，它的重点在于算法和数据结构。C程序的设计首要考虑的是如何通过一个过程，对输入（或环境条件）进行运算处理得到输出（或实现过程（事务）控制），而对于C++，首要考虑的是如何构造一个对象模型，让这个模型能够契合与之对应的问题域，这样就可以通过获取对象的状态信息得到输出或实现过程（事务）控制。

int fun() 和 int fun(void)的区别?
这里考察的是c 中的默认类型机制。

在c中，int fun() 会解读为返回值为int(即使前面没有int，也是如此，但是在c++中如果没有返回类型将报错)，输入类型和个数没有限制， 而int fun(void)则限制输入类型为一个void。
在c++下，这两种情况都会解读为返回int类型，输入void类型。

#### 9、struct与class的区别

#### 10、struct内存对齐 

#### 11、new delete与malloc free的区别 

1). malloc与free是C++/C语言的标准库函数，new/delete是C++的运算符。它们都可用于申请动态内存和释放内存。
2). 对于非内部数据类型的对象而言，光用maloc/free无法满足动态对象的要求。对象在创建的同时要自动执行构造函数，对象在消亡之前要自动执行析构函数。
由于malloc/free是库函数而不是运算符，不在编译器控制权限之内，不能够把执行构造函数和析构函数的任务强加于malloc/free。因此C++语言需要一个能完成动态内存分配和初始化工作的运算符new，以一个能完成清理与释放内存工作的运算符delete。注意new/delete不是库函数。
最后补充一点体外话，new 在申请内存的时候就可以初始化（如下代码）， 而malloc是不允许的。另外，由于malloc是库函数，需要相应的库支持，因此某些简易的平台可能不支持，但是new就没有这个问题了，因为new是C++语言所自带的运算符。

int *p = new int(1);
特别的，在C++中，如下的代码，用new创建一个对象(new 会触发构造函数， delete会触发析构函数)，但是malloc仅仅申请了一个空间，所以在C++中引入new和delete来支持面向对象。

#### 12、内存泄露的情况 

   用动态存储分配函数动态开辟的空间，在使用完毕后未释放，结果导致一直占据该内存单元即为内存泄露。

1). 使用的时候要记得指针的长度.
2). malloc的时候得确定在那里free.
3). 对指针赋值的时候应该注意被赋值指针需要不需要释放.
4). 动态分配内存的指针最好不要再次赋值.
5). 在C++中应该优先考虑使用智能指针.

#### 13、sizeof与strlen对比 



#### 14、指针与引用的区别 



#### 15、野指针产生与避免 



#### 16、多态：动态多态、静态多态 



#### 17、虚函数实现动态多态的原理、虚函数与纯虚函数的区别 

带有多态性质的基础类应该声明一个virtual析构函数，如果class 带有任何virtual,它就应该拥有 virtual 析构函数

**纯虚函数也可以实现**，只是调用的时候，必须明确类名

通过虚函数表实现。类中含有虚函数，系统会为这个类分配一个指针成员指向一张虚函数表，表中每一项指向一个虚函数的地址，实现上就是一个函数指针数组。虚函数表既有继承性又有多态性。每个派生类的虚函数表继承了他各个基类的虚函数表，如果基类虚函数表中包含某一项，则派生类的虚函数表也将包含同一项，但两项的值可能不同。如果派生类覆写了该项对象的虚函数，则派生类的虚函数表的该项指向重载后的虚函数。

**构造函数调用虚函数：**构造函数中，虚拟机制不起作用，不会向下匹配 到派生类

```c++
class A{
public:
    virtual void print(){ cout << "A::print()"<< endl; }
};
class B : public A{
public:
    virtual void print(){ cout << "B::print()"<< endl; }
};
void print(A a){
    a.print();
} 
int main(){
	A a, *pa, *pb; 
    B b;
    pa=&a;pb = &b;
    b.print();
    pa->print();
    pb->print();
    print(b);
}
//输出结果：
B::print()  
A::print()
B::print()
A::print()
```

**虚函数参数**：参数默认值编译时决定

```c++
class A{
public:
    virtual void g(int a=3){cout <<"A::g()"<< a <<endl;}
};
class B : public A{
public:
    virtual void g(int a=5){
        cout <<"B::g()"<< a <<endl;
    }
};

A* pA = new B();
pA->g();
//输出结果：B::g() 3
```

#### 18、继承时，父类的析构函数是否为虚函数？构造函数能不能为虚函数？为什么？ 



**基类采用虚析构函数可以防止内存泄漏**。如果基类中不是虚析构函数，则子类的析构函数不会被调用，因此会造成内存泄漏。

#### 19、静态多态：重写、重载、模板 

#### 20、static关键字：修饰局部变量、全局变量、类中成员变量、类中成员函数 

**静态成员变量**

静态成员变量需要在类内声明（加static），在类外初始化（不能加static），如下例所示；
静态成员变量在类外单独分配存储空间，位于全局数据区，因此静态成员变量的生命周期不依赖于类的某个对象，而是所有类的对象共享静态成员变量；
可以通过对象名直接访问公有静态成员变量；
可以通过类名直接调用公有静态成员变量，即不需要通过对象，这一点是普通成员变量所不具备的。
class example{
public:
static int m_int; //static成员变量
};

int example::m_int = 0; //没有static

cout<<example::m_int; //可以直接通过类名调用静态成员变量

**静态成员函数**

 静态成员函数是类所共享的；
静态成员函数可以访问静态成员变量，但是不能直接访问普通成员变量（需要通过对象来访问）；需要注意的是普通成员函数既可以访问普通成员变量，也可以访问静态成员变量；
可以通过对象名直接访问公有静态成员函数；
可以通过类名直接调用公有静态成员函数，即不需要通过对象，这一点是普通成员函数所不具备的。
class example{
private:
static int m_int_s; //static成员变量
int m_int;
static int getI() //静态成员函数在普通成员函数前加static即可
{
  return m_int_s; //如果返回m_int则报错，但是可以return d.m_int是合法的
}
};

cout<<example::getI(); //可以直接通过类名调用静态成员变量


#### 21、const关键字：修饰变量、指针、类对象、类中成员函数 

    1).定义只读变量，或者常量（只读变量和常量的区别参考下面一条）;
    2).修饰函数的参数和函数的返回值;
    3).修饰函数的定义体，这里的函数为类的成员函数，被const修饰的成员函数代表不能修改成员变量的值，因此const成员函数只能调用const成员函数；
    4).只读对象。只读对象只能调用const成员函数。
```c++
class Screen {
public:
const char cha； //const成员变量
char get() const; //const成员函数
};

const Screen screen； //只读对象
```

c中的const仅仅是从编译层来限定，不允许对const 变量进行赋值操作，在运行期是无效的，所以并非是真正的常量（比如通过指针对const变量是可以修改值的），但是c++中是有区别的，c++在编译时会把const常量加入符号表，以后（仍然在编译期）遇到这个变量会从符号表中查找，所以在C++中是不可能修改到const变量的。
补充：

```c++
//常指针（指向常量的指针）指针本身可以改变，然而指针所指向的值却不可以改变。
const int *p  
//指针常量,指针指向的内容可以改变，然而指针指向的地址却不可以改变。
int* const p
//指向常量的常指针，指针所保存的地址不可变，指针所指向的数值也不可变。
const int const *p
```

1）c中的局部const常量存储在栈空间，全局const常量存在只读存储区，所以全局const常量也是无法修改的，它是一个只读变量。
2）这里需要说明的是，常量并非仅仅是不可修改，而是相对于变量，它的值在编译期已经决定，而不是在运行时决定。
3）c++中的const 和宏定义是有区别的，宏是在预编译期直接进行文本替换，而const发生在编译期，是可以进行类型检查和作用域检查的。
4）c语言中只有enum可以实现真正的常量。
5）c++中只有用字面量初始化的const常量会被加入符号表，而变量初始化的const常量依然只是只读变量。
6）c++中const成员为只读变量，可以通过指针修改const成员的值，另外const成员变量只能在初始化列表中进行初始化。

下面我们通过代码来看看区别。
同样一段代码，在c编译器下，打印结果为*pa = 4， 4
在c++编译下打印的结果为 *pa = 4， 8

```
int main(void)
{
    const int a = 8;
    int *pa = (int *)&a;
    *pa = 4;
    printf("*pa = %d, a = %d", *pa, a);
    return 0;
}
```

#### 22、extern关键字：修饰全局变量 

C++语言支持函数重载，C语言不支持函数重载，函数被C++编译器编译后在库中的名字与C语言的不同，假设某个函数原型为：

void foo(int x, int y);
该函数被C编译器编译后在库中的名字为 _foo, 而C++编译器则会产生像: _foo_int_int 之类的名字。为了解决此类名字匹配的问题，C++提供了C链接交换指定符号 extern “C”。

#### 23、volatile和atomic

​     防止重排序，线程可见性，不能保证原子性，非线程安全。避免编译器指令优化 。

     volatile int len = 0;
     如果两个线程同时 ++；
    len ++;
    每个线程先取到 len ==0;
    都加 1；结果可能还是 1；

 根据c++11的标准：

volatile 只能防止编译器优化变量，强制载入寄存器，而不能提供原子性；代码的相对顺序不会被编译器重排。

```c++
void f() {//可能会被优化成空函数
  int a = 0;
  for (int i = 0; i < 1000; ++i) {
    a += i;
  }
}
```

VC++中只有早期版本给volatile 提供原子性，在最新版中，微软为了遵守标准，去掉了volatile 的原子性；

atomic：所谓访问原子性就是 Read，Write 操作是否存在中间状态

load(), store(),   exchange() 存值同时返回原值

compare_exchange_strong(&expected， desired)  CAS，无论是否成功，都会在expected变量输出原值

默认情况下C++11中的原子类型的变量在线程中总是保持着顺序执行的特性（memory_order默认参数memory_order_seq_cst 全部存取都按照顺序执行，非原子类型没有必要，因为不需要在线程间同步）。我们称这样的特性为“顺序一致”。 特殊平台，可以手动指定memory_order参数来提高性能。

**memory的总体分类和内存序的对应**：

- **memory_order_seq_cst:**
  这是所有atomic操作内存序参数的默认值，语义上就是要求底层提供顺序一致性模型，不存在任何重排，可以解决一切问题，但是效率最低。
- **memory_order_release/acquire/consume:**
  提供release、acquire或者consume, release语意的一致性保障
  它的语义是：我们允许cpu或者编译器做一定的指令乱序重排，但是由于tso, pso的存在，可能产生的store-load乱序store-store乱序导致问题，那么涉及到多核交互的时候，就需要手动使用release, acquire去避免这样的这个问题了。简单来说就是允许大部分写操作乱序（只要不影响代码正确性的话），对于乱序影响正确性的那些部分，程序员自己使用对应的内存序代码来控制。
- **memory_order_relaxed:**
  这种内存序对应的就是RMO，完全放开，让编译器和cpu自由搞，很容易出问题，除非你的代码是那种不论怎么重排都不影响正确性的逻辑，那么选择这种内存序确实能提升最大性能。

```c++
int a = 0;
std::atomic<int> b(0);
void func1(){
    a = 1;
    b.store(2,std::memory_order_release);//-a的写操作不会重排到b的写操作之后
}
void func2(){
    while(b.load(std::memory_order_acquire) != 2);
    cout<<a<<endl;//-a的读操作不会重排到b的读操作之前
}
```

**c++内存屏障**

 只是保证执行顺序，防止不了代码被编译优化

#### 24、四种类型转换：static_cast、dynamic_cast、const_cast、reinterpret_cast 

**1)static_cast**

用于基本类型间的转换，但不能用于基本类型指针和引用的转换

**不允许不同类型之间指针/引用的转换(有父子关系的类对象除外)**

用于有继承关系类对象间的转换和基类指针/引用转成子类指针/引用，用户自己要防止越界访问

**2)reinterpret_cast**

用于不同类型指针间的类型转换，相当于C的强制转换，转换后是否会出问题，需要开发人员保证

不能用于**变量类型间的转换**（static_cast）

用于整数和指针间的类型转换

**3)const_cast**

用于去掉变量的const属性，但常数的const是不能去掉的 

```c++
const int a = 10;
int b  = const_cast<int>(a); //编译报错
```

转换的目标类型必须是指针或者引用

**4)dynamic_cast**

限制较多；只能处理类对象和指针；

用于有继承关系的类指针间的转换
用于有交叉关系的类指针间的转换
具有类型检查的功能
需要虚函数的支持

#### 25、四种智能指针及底层实现：auto_ptr、unique_ptr、shared_ptr、weak_ptr 

```c++
share_ptr<int> sp(new int(10));
weak_ptr<int> wp(sp);
if(!wp.expired()){
	share_ptr<int> sp2 = wp.lock();//转换为share_ptr
	*sp2 = 100;//使用
}
```



```c++
//自实现
template<typename T>
class unique_ptr {
    public:
        unique_ptr(T * ptr = nullptr): _ptr(ptr){
        }
    	
        //移动构造函数
        unique_ptr(unique_ptr<T>&& scopedptr) noexcept : _ptr(scopedptr._ptr){
       	 	std::cout << "move construct..." << std::endl;
        	scopedptr._ptr =  nullptr;
    	}
    	//移动赋值运算符
    	unique_ptr& operator=(unique_ptr<T> && scopedptr) noexcept {
            std::cout << "move assignment..." << std::endl;
            if(this != &scopedptr){
                _ptr = scopedptr._ptr;
               scopedptr._ptr = nullptr;
            }
       		 return *this;
   		}

        T* operator->(){
            return this->_ptr;
        }

        T& operator*(){
            return *(this->_ptr);
        }

        T* get(){
            return this->_ptr;
        }

        ~ScopedPtr(){
            if(_ptr != nullptr){
                delete _ptr;
            }
        }
    protected:
    	//拷贝构造函数及赋值操作符全部隐藏
	     ScopedPtr(ScopedPtr<T> & scopedptr){}
         ScopedPtr<T> & operator=(ScopedPtr<T> & scopedptr){}

    private:
        T *_ptr;
};
```

```c++
class SharedPtr {
    public:
        ...
        SharedPtr(T *ptr = nullptr): _ptr(ptr), _ref_count(new int(1)){
        }

        SharedPtr(SharedPtr<T> & scopedptr): _ptr(scopedptr._ptr), _ref_count(scopedptr._ref_count){
            ++（*_ref_count);
        }

        SharedPtr & operator=(SharedPtr<T> & scopedptr){
            if(this != &scopedptr){
                _release();
                _ptr(scopedptr._ptr);
                _ref_conut(scopedptr._ptr);
                ++(*_ref_count);
            }

            return *this;
        }

        ~SharedPtr(){
            _release();
        }

        int* getCount(){
            return *_ref_count; 
        }
    protected:
        void _release() {
            std::cout << "deconstruct...: count=" << ((*_ref_count) -1)  << std::endl;
            if(--(*_ref_count) == 0){
                delete _ptr;
                delete _ref_count;
            }
        }

    private:
        ...
        int *_ref_count;   //引用计数
};
```

#### 26、shared_ptr中的循环引用怎么解决？（weak_ptr） 

用weak_ptr改造上文的SharedPtr

```c++
class Counter
{
    public:
        Counter():s(0),w(0){};
        int s; //存放share_ptr引用计数
        int w; //存放weak_ptr引用计数
};
template<typename T> class WeakPtr;
template<typename T>
SharedPtr {
    public:
        SharedPtr(T * ptr = nullptr):_ptr(ptr){
            _cnt = new Counter();
           if(_ptr != nullptr){
                 _cnt->s = 1;
           }
        }

        ~SharedPtr(){
            _release();
        }

        SharedPtr(SharedPtr<T> & sharedptr): _ptr(sharedptr._ptr), _cnt(sharedptr._cnt){
            _cnt->s++;
        }

        SharedPtr(WeakPtr<T> & weakptr): _ptr(weakptr._ptr), _cnt(weakptr._cnt){
            _cnt->s++;
        }

        SharedPtr & operator=(SharedPtr<T> & sharedptr){
            if(this != & sharedptr){
                _release();
                _ptr = sharedptr._ptr;
                _cnt = sharedptr._cnt;
                _cnt->s++;
            }
            return *this;
        }

        T& operator *(){
            return *_ptr;
        }

        T* operator ->(){
            return _ptr;
        }

        //friend class WeakPtr<T>;

    protected:
       void _release(){
            _cnt->s--;
            std::cout << "release "<<_cnt->s << std::endl;
            if(_cnt->s < 1) {
                delete _ptr;
                if(_cnt->w <1) {
                    delete _cnt;
                    _cnt=nullptr;
                }
            }
        }

    private:
        T * _ptr;
        Counter * _cnt;
};

template<typename T>
class WeakPtr{
    public:
        WeakPtr():_ptr(nullptr), _cnt(nullptr){}

        ~WeakPtr(){
            _release();
        }

        WeakPtr(WeakPtr<T> & weakptr):_ptr(weakptr._ptr), _cnt(weakptr._cnt){
            _cnt->w++;
        }

        WeakPtr(SharedPtr<T> & sharedptr):_ptr(sharedptr._ptr), _cnt(sharedptr._cnt){
            _cnt->w++;
        }

        WeakPtr & operator=(WeakPtr<T> & weakptr){
            if(this != &weakptr){
                _release();
                _ptr = weakptr._ptr;
                _cnt = weakptr._cnt;
                _cnt->w++;
            }
            return *this;
        }

        WeakPtr & operator=(SharedPtr<T> & sharedptr){
            _release();
            _ptr = sharedptr._ptr;
            _cnt = sharedptr._cnt;
            _cnt->w++;
            return *this;
        }

        SharedPtr<T> lock(){
                return SharedPtr<T>(*this);
        }

        bool expired(){
                if(_cnt && _cnt->s > 0){
                    std::cout<<"empty "<<_cnt->s<<std::endl;
                    return false;
                }
                return true;
        }

        //friend class SharedPtr<T>;

    protected:
        void _release(){
            if(_cnt){
                _cnt->w--;
                std::cout<<"weakptr release"<<_cnt->w<<std::endl;
                if(_cnt->w < 1 && _cnt->s <1) {
                        //delete cnt;
                        _cnt=nullptr;
                }
            }
        }

    private:
        T * _ptr;
        Counter * _cnt;
};
```



#### 27、vector与list比较 

#### 28、vector迭代器失效的情况 

#### 29、map与unordered_map对比 

#### 30、set与unordered_set对比 

#### 31、STL容器空间配置器  

#### 32.函数调用过程

1).参数拷贝（压栈），注意顺序是从右到左，即c-b-a；
2).保存d = fun(a, b, c)的下一条指令，即cout< 3).跳转到fun()函数，注意，到目前为止，这些都是在main()中进行的；
fun()=====
4).移动ebp、esp形成新的栈帧结构;
5).压栈（push）形成临时变量并执行相关操作;
6).return一个值;
7).出栈（pop）;
8).恢复main函数的栈帧结构;
9).返回main函数;

#### 33.宏和内联（inline）函数的比较？

1）首先宏是C中引入的一种预处理功能；
2）内联（inline）函数是C++中引用的一个新的关键字；C++中推荐使用内联函数来替代宏代码片段；
3）内联函数将函数体直接扩展到调用内联函数的地方，这样减少了参数压栈，跳转，返回等过程；
4)  由于内联发生在编译阶段，所以内联相较宏，是有参数检查和返回值检查的，因此使用起来更为安全；
5)  需要注意的是， inline会向编译期提出内联请求，但是是否内联由编译期决定（当然可以通过设置编译器，强制使用内联）；
6)  由于内联是一种优化方式，在某些情况下，即使没有显示的声明内联，比如定义在class内部的方法，编译器也可能将其作为内联函数。
7)  内联函数不能过于复杂，最初C++限定不能有任何形式的循环，不能有过多的条件判断，不能对函数进行取地址操作等，但是现在的编译器几乎没有什么限制，基本都可以实现内联。

#### 34. 头文件中的 ifndef/define/endif 是干什么用的? 该用法和 program once 的区别？

相同点:

它们的作用是防止头文件被重复包含。

不同点:

ifndef 由语言本身提供支持，但是 program once 一般由编译器提供支持，也就是说，有可能出现编译器不支持的情况(主要是比较老的编译器)。
通常运行速度上 ifndef 一般慢于 program once，特别是在大型项目上， 区别会比较明显，所以越来越多的编译器开始支持 program once。
ifndef 作用于某一段被包含（define 和 endif 之间）的代码， 而 program once 则是针对包含该语句的文件， 这也是为什么 program once 速度更快的原因。
如果用 ifndef 包含某一段宏定义，当这个宏名字出现“撞车”时，可能会出现这个宏在程序中提示宏未定义的情况（在编写大型程序时特性需要注意，因为有很多程序员在同时写代码）。相反由于program once 针对整个文件， 因此它不存在宏名字“撞车”的情况， 但是如果某个头文件被多次拷贝，program once 无法保证不被多次包含，因为program once 是从物理上判断是不是同一个头文件，而不是从内容上。

#### 35.i++和++i的区别

```
//i++实现代码为：
int operator++(int)
{
    int temp = *this;
    ++*this;
    return temp;
}//返回一个int型的对象本身
 
// ++i实现代码为：
int& operator++()
{
    *this += 1;
    return *this;
}//返回一个int型的对象引用
```

#### 36.左值引用与右值引用

可以取地址的，有名字的，非临时的就是**左值**
不能取地址的，没有名字的，临时的，通常生命周期就在某个表达式之内的就是**右值**

左值引用就是我们通常所说的引用，左值引用通常可以看作是变量的别名。

```c++
int a = 10
int &b = a
 
int &c = 10	// 错误，无法对一个立即数做引用 
const int &d = 10	// 正确， 常引用引用常数量是ok的，其等价于 const int temp = 10; const int &d = temp
```

右值引用是 C++11 新增的特性，其形式如下所示。右值引用用来绑定到右值，绑定到右值以后本来会被销毁的右值的生存期会延长至与绑定到它的右值引用的生存期。

```c++
int &&var = 10;	//正确，10会被直接存放在寄存器中，所以它是右值
 
int a = 10
int &&b = a	// 错误， a 为左值 
int &&c = var	// 错误，var 为左值
int && c = a;  // 错误，a在内存中有空间，所以是左值
int && d = a + 5; // 正确，虽然 a 在内存中，但 a+5 的结果放在寄存器中，它没有在内存中分配空间，因此是右值 
int &&d = move(a)	// ok, 通过move得到左值的右值引用
    
//通用引用,可接收左值也可接收右值
auto && b2 = 5;   //通用引用，可以接收右值
auto && d2 = a;   //通用引用，可以接收左值
const auto && e2 = a; //错误，加了const就不再是通用引

//有两种类型的通用引用: 一种是auto，另一种是通过模板定义的T&&。实际上auto就是模板中的T，它们是等价的
template<typename T>
void f(T&& param){
    std::cout << "the value is "<< param << std::endl;
}

//通用引用有两个条件：一，必须是T&&的形式，由于auto等价于T，所以auto && 符合这个要求；二，T类型要可以推导，也就是说它必须是个模板，而auto是模板的一种变型，因此b是通用引用
```

在汇编层面右值引用做的事情和常引用是相同的，即产生临时量来存储常量。但是，唯一 一点的区别是，右值引用可以进行读写操作，而常引用只能进行读操作。

```c++
template <typename T>
void f(T param);

template <typename T>
void func(T& param);

template <typename T>
void function(T&& param);

int main(int argc, char *argv[]) {

    int x = 10;         // x是int
    int & rr = x;       // rr是 int &
    const int cx = x;   // cx是const int
    const int& rx = x;  // rx是const int &
    int *pp = &x;       // pp是int *

    //下面是传值的模板，由于传入参数的值不影响原值，所以参数类型退化为原始类型
    f(x);               // T是int
    f(cx);              // T是int
    f(rx);              // T是int
    f(rr);              // T是int
    f(pp);              // T是int*，指针比较特殊，直接使用

    //下面是传引用模板, 如果输入参数类型有引用，则去掉引用;如果没有引用，则输入参数类型就是T的类型
    func(x);            // T为int
    func(cx);           // T为const int
    func(rx);           // T为const int
    func(rr);           // T为int
    func(pp);           // T是int*，指针比较特殊，直接使用

    //下面是通用引用模板，与引用模板规则一致
    function(x);        // T为int&
    function(5);        // T为int
}
```



#### 37. std::move函数

std::move可以将任何一值变成右值

```c++
template <typename T>
typename remove_reference<T>::type&& move(T&& t)//参数类型称为通用引用类型,可左值也可右值
{
    return static_case<typename remove_reference<T>::type&&>(t);
}
```

move的返回类型为类型成员，C++的类成员有成员函数、成员变量、静态成员三种类型。C++11之后又增加了一种成员称为类型成员。类型成员与静态成员一样，它们都属于类而不属于对象，访问它时也与访问静态成员一样用`::`访问。

```C++
template <typename T>
struct remove_reference{
    typedef T type;  //定义T的类型别名为type
};

template <typename T>
struct remove_reference<T&> //左值引用
{
    typedef T type;
}

template <typename T>
struct remove_reference<T&&> //右值引用
{
   typedef T type;
}
```

经过remove_reference处理后，T的引用被剔除了



#### 38.移动构造函数

```c++
class A {
    public:
        A(A && a){//右值
            std::cout << "A move construct ..." << std::endl;
            ptr_ = a.ptr_;//减少了对堆空间的频繁操作
            a.ptr_ = nullptr;
        }
        ...
};
```

std::move可以将任何一值变成右值

```C++
std::vector<A> vec;
vec.push_back(std::move(A()));
```

```c++
std::move(a);
//翻译过来就成了以下代码：

int && move(int&& && t){
    return static_case<int&&>(t);
}
//或
int && move(int& && t){
    return static_case<int&&>(t);
}
//引用折叠:左值引用总是折叠为左值引用，右值引用总是折叠为右值引用。
int & & 折叠为 int&
int & && 折叠为 int&
int && & 折叠为 int&
int && && 折叠为 int &&

//std::move就变成了下面的样子：
int && move(int &&t){
    return static_case<int&&>(t);
}
int && move(int& t){
    return static_case<int&&>(t);
}    
```

#### 40.std::function

代替C函数指针

可以将一个重载了**()**操作符的对象赋值给它，这样就可以像调用函数一样使用该对象了

```c++
struct A {
    void operator()() {
        std::cout << "This is A Object" << std::endl;
    }
};

int main(...){
    A a;
    std::function<void()> func = a;
    func();
}
```

function的实现原理：

```c++
class myfunction<R(Arg0)> function{
    private:
        template<typename F>
        class __callable: public __callbase {

            public:
                callable(F functor)
                    : functor(functor){}

                virtual R operator()(Arg0 arg0) {//virtual关键字
                    return functor(arg0);
                }

            private:
                F functor;
        };

    public:
        template<typename F>
        myfunction(F f): base_(new __callable<F>(f)){
        }

        ~myfunction(){
            if(base_) {
                delete base_;
                base_ = nullptr;
            }
        }
};
```



参考书籍： 《C++ Primer》（第5版）、《STL源码  剖析》、《深度探索C++对象模型》  

下载地址： https://pan.baidu.com/share/init?surl=qqAR6iqjur1sfmzeZjcrwg  提取码：m6gx  

#### 41.lock_guard和unique_lock

lock_guard是一个互斥量包装程序，它提供了一种方便的RAII（Resource acquisition is initialization ）风格的机制来在作用域块的持续时间内拥有一个互斥量。

特点如下：

- 创建即加锁，作用域结束自动析构并解锁，无需手工解锁
- 不能中途解锁，必须等作用域结束才解锁
- 不能复制

unique_lock是一个通用的互斥量锁定包装器，它允许延迟锁定，限时深度锁定，递归锁定，锁定所有权的转移以及与条件变量一起使用。

unique_lock 是 lock_guard 的升级加强版，它具有 lock_guard 的所有功能，同时又具有其他很多方法，使用起来更强灵活方便，能够应对更复杂的锁定需要。但是消耗资源也较多

特点如下：

- 创建时可以不锁定（通过指定第二个参数为std::defer_lock），而在需要时再锁定
- 可以随时加锁解锁
- 作用域规则同 lock_grard，析构时自动释放锁
- 不可复制，可移动
- 条件变量需要该类型的锁作为参数（此时必须使用unique_lock）

需要使用锁的时候，首先考虑使用 lock_guard



#### 42.C++11新特性：enable_shared_from_this

在异步调用中，存在一个保活机制，异步函数执行的时间点我们是无法确定的，然而异步函数可能会使用到异步调用之前就存在的变量。为了保证该变量在异步函数执期间一直有效，我们可以传递一个指向自身的share_ptr给异步函数，这样在异步函数执行期间share_ptr所管理的对象就不会析构

```c++
struct Data : std::enable_shared_from_this<Data> //继承enable_shared_from_this
{
public:
	std::shared_ptr<Data> getSharePtr() {
		return shared_from_this();
	}
	~Data() { std::cout << "Data::~Data()" << std::endl; }
};
```



#### 43. C++11新特性：emplace

新引入的的三个成员emlace_front、empace 和 emplace_back,这些操作构造而不是拷贝元素到容器中，这些操作分别对应push_front、insert和push_back，允许我们将元素放在容器头部、一个指定的位置和容器尾部。

**两者的区别** 

当调用insert时，我们将元素类型的对象传递给insert，元素的对象被拷贝到容器中，而当我们使用emplace时，我们将参数传递元素类型的构造函，emplace使用这些参数在容器管理的内存空间中直接构造元素。

```c++
class MyString
{
public:
    MyString(const char *str = NULL);
    MyString(const MyString &other);
    ~MyString(void);
    MyString & operator = (const MyString &other);
};

{
    std::vector<MyString> vStr;
    vStr.reserve(100);    
    vStr.push_back(MyString("can ge ge blog"));//构造临时对象，拷贝构造将临时对象复制到vector
    //销毁临时对象和vector中的元素
}
{
     std::vector<MyString> vStr;
     vStr.emplace_back("hello world");//构造对象复制到vector
    //销毁vector中的元素
}
```



#### 44.C++11新特性：decltype

decltype与auto关键字一样，用于进行编译时类型推导。
decltype实际上有点像auto的反函数，auto可以让你声明一个变量，而decltype则可以从一个变量或表达式中得到类型

```c++
const int&& foo();
int i;
struct A { double x; };
const A* a = new A();
 
decltype(foo())  x1;  // const int&&      (1)
decltype(i)      x2;  // int              (2)
decltype(a->x)   x3;  // double           (3)
decltype((a->x)) x4;  // double&          (4)
```

decltype推导三规则
1.如果e是一个没有带括号的标记符表达式或者类成员访问表达式（上例中的(2)和(3)），那么的decltype(e)就是e所代表的实体的类型。如果没有这种类型或者e是一个被重载的函数，则会导致编译错误。

2.如果e是一个函数调用或者一个重载操作符调用，那么decltype(e)就是该函数的返回类型（上例中的 (1)）。

3.如果e不属于以上所述的情况，则假设e的类型是T：当e是一个左值时，decltype(e)就是T&；否则（e是一个右值），decltype(e)是T。上例中的(4)即属于这种情况。在这个例子中，e实际是(a->x)，由于有这个括号，因此它不属于前面两种情况，所以应当以本条作为判别依据。而(a->x)是一个左值，因此会返回double &。

```c++
#include <iostream>
#include <vector>
using namespace std;
 
void Overloaded(int){ };
void Overloaded(char,char){ };//重载函数
const bool Func_1(int){ return true; };
const bool &Func_2(int){ return  true; };
 
int main()
{
	int i = 4;
	const int j = 5;
	int arr[5] = { 0 };
	int *ptr = arr;
	struct S{ double d; }s;
 
 
	//规则一：推导为的其类型
	decltype(arr) var1;               //int[5] 标记符表达式
	decltype(ptr) var2;               //int *  标记符表达式
	decltype(s.d) var3;               //doubel 成员访问表达式
	//decltype(Overloaded(1)) var4;   //重载函数。编译错误。
 
	//规则二：推导为函数调用的返回类型
	decltype(Func_1(1)) var5 = true;  //bool，这是因为函数返回的是一个纯右值，对于纯右值，
	                                  //只有类类型可以携带CV限定符，其他一般忽略掉CV限定符。
	decltype(Func_2(1)) var6 = true;  //const bool &
 
	//规则三：左值，推导为类型的引用
	decltype((i))var7 = i;            //int&
	decltype(true ? i : i) var8 = i;  //int&  条件表达式返回左值。
	decltype(++i) var9 = i;           //int&  ++i返回i的左值。
	decltype(arr[5]) var10 = i;       //int&  []操作返回左值
	decltype(*ptr)var11 = i;          //int&  *操作返回左值
	decltype("hello")var12 = "hello"; //const char(&)[6] 字符串字面常量为左值，且为const左值。
 
	//右值，则推导为本类型
	decltype(1) var13=10;             //int	
	decltype(i++) var14 = i;          //int i++返回右值   
 
	system("pause");
	return 0;
}
```

可以利用C++11标准库中添加的模板类is_lvalue_reference来判断表达式是否为左值：
std::cout << std::is_lvalue_reference<decltype(++i)>::value << std::endl;
结果1表示为左值，结果为0为非右值。

#### 45.Thread和线程安全注解

```
 thread t{}, thread t{f, args}  构造，在线程中执行f(args)
 t.join() 阻塞等待
 t.detach() 线程独立
 this_thread::yield() 给其他线程运行机会，当前线程不会阻塞，也不休眠。用来等待atomic状态改变以及用于协调多线程。
 thread_local：专有变量
```

```c++
atomic_bool isReady = false; //volatile：防止共享变量被缓存，导致线程跑来跑去
atomic<int> mycount = 0;

void task(int arg) {
  cout<<"arg="<<arg<<endl;
  while (!isReady) {
    this_thread::yield(); //出让时间片，等待下一次调用
  }

  for (int i = 0; i < 100; i++) {
    mycount++;
  }
}
```

线程安全注解:

**CAPABILITY**//该宏负责指定相关的类使用线程安全检测机制
**SCOPED_CAPABILITY**//负责实现RAII样式锁定的类属性，即在构造时获取能力，析构时释放能力。其他和CAPABILITY类似。GUARDED_BY //声明数据成员受给定功能保护。对数据的读取操作需要共享访问，而写入操作需要独占访问。
PT_GUARDED_BY和GUARDED_BY类似，用于指针和智能指针，用户保护指针指向的数据。
ACQUIRED_BEFORE 需要在另一个能力获取之前被调用
ACQUIRED_AFTER 需要在另一个能力获取之后被调用
REQUIRES 用来修饰函数，使其调用线程必须具有对给定功能的独占访问权限。被修饰的函数在进入前必须已经持有能力，函数退出时不在持有能力。
这个变量的概念比较绕，实际上转化为代码之后，比较好理解，下面时clang提供的官方代码

```c++
Mutex mu1, mu2;
int a GUARDED_BY(mu1);
int b GUARDED_BY(mu2);

void foo() REQUIRES(mu1, mu2) {
  a = 0;
  b = 0;
}

void test() {
  mu1.Lock();
  foo();         // Warning!  Requires mu2.
  mu1.Unlock();
}
```

REQUIRES_SHARED//和REQUIRES类似，只不过REQUIRES_SHARED可以共享地获取能力ACQUIRE//用来修饰函数，使其调用线程必须具有对给定功能的独占访问权限。被修饰的函数在进入前必须持有能力。ACQUIRE_SHARED//和ACQUIRE相同，只是能力可以共享
//用来修饰函数，使其调用线程必须具有对给定功能的独占访问权限。被修饰的函数退出时不在持有能力。
RELEASE_SHARED//和RELEASE相同，用于修饰释放可以共享的能力
RELEASE_GENERIC//和RELEASE相同，用于修饰释放共享的能力和非共享的能力
TRY_ACQUIRE//尝试获取能力
TRY_ACQUIRE_SHARED//尝试获取共享的能力
EXCLUDES//修饰函数一定不能具有某项能力
ASSERT_CAPABILITY//修饰调用线程已经具有给定的能力。
RETURN_CAPABILITY//修饰函数负责返回给定的能力
NO_THREAD_SAFETY_ANALYSIS//修饰函数，关闭能力检查

#### 46.锁

**recursive_mutex和mutex**  区别在于recursive_mutex允许同一线程反复加锁和解锁

```c++
recursive_mutex count_mutex;
void write(){
    count_mutex.lock();
    cout<<"lock";
    write();
    count_mutex.unlock();
}
```

**timed_mutex和recursive_timed_mutex**:  只有这两个允许限时操作

```c++
timed_mutex m{};
m.lock();  m.try_lock(); m.unlock();
m.try_lock_for(d); //等待duration,如果<0,等同try_lock()
m.try_lock_until(tp); //time point,如果早于当前时间，等同try_lock()
```

**lock_guard和unique_lock**: 防止忘记释放锁， 两个RAII类

**call_once和once_flag**

#### 47.条件变量condition_variable,condition_variable_any

```c++
    bool isReady = false;
    pool.enqueue([&](){
        std::this_thread::sleep_for(std::chrono::milliseconds(3000));
        isReady = true;//任务完成，修改变量
        std::cout << "enqueue end." << std::endl;
    });

    std::unique_lock<std::mutex> locker(mux);
    //等待isReady变为true，最长等1s
    if( condition.wait_for(locker, 1s, [&]{return isReady;})) {//可以为空函数或者不写
        //isReady = true
        std::cout << "condition locker finish waiting." << std::endl;
    }
    //isReady为true或者超时
    std::cout << "condition end." << std::endl;
```

condition_variable_any：  condition_variable是针对unique_lock设计的，condition_variable_any可以操作任何可锁对象

#### 48.并发任务

**future和promise**，shared_future

```
p.get_future();
p.set_value("");//没有copy功能，只能设置一次

f.wait()//阻塞，直到有值到来
f.wait_until(tp) //阻塞，直到有值到来或者到达时间tp
f.get() //读取的时候就会move，不能读取两次

sf->get();//可以多次读取。如果值类型为void或者引用，有特殊规则：
shared_future<void>::get(): 返回，或者抛异常
shared_future<T&>::get()：标准库返回的是指针，get将其转成T&
shared_future<T>::get()：返回 const T&
```

#### 49.packaged_task

 保存了一个任务和一个future/promise对.  没有set_value(), 有get_future();

**关键点**：执行方式和普通函数调用一样，即便任务函数和get()是在不同线程。

#### 50.async()

```
fu = async(policy, f, args)//policy:async或者deferred
fu = async(f, args)

double squre(int a){return a*a;}
auto fu = async(squre, 2);
auto d = fu.get(); 
```

async：就像创建了一个新的thread执行任务一样，但是有可能不创建，甚至可能被优化掉

deferred：对任务的future执行get()时执行任务，对于无并发可能更合适

#### 51.RAII对象

RAII（Resource Acquisition Is Initialization）资源取得时机便是初始化时机

禁止复制；对底层资源做引用计数；复制底部资源；转移底部资源的拥有权；

如果复制RAII对象，必须一并复制他管理的资源

#### 52.explicit关键字

explicit构造函数是用来防止隐式转换的

```c++
class Test{
public:
explicit Test(int n){}
}
Test t = 6;//不加explicit，就会隐式转换，加上编译就报错了，需要显式构造
```



#### 55.malloc()函数分配内存失败的原因

```
1. 内存不足。
2. 在前面的程序中出现了内存的越界访问，导致malloc()分配函数所涉及的一些信息被破坏。下次再使用malloc()函数申请内存就会失败，返回空指针NULL(0)。
```

#### 56.lock_guard以及unique_lock的区别

这两种锁都可以对std::mutex进行封装，实现RAII的效果。绝大多数情况下这两种锁是可以互相替代的，区别是unique_lock比lock_guard能提供更多的功能特性（但需要付出性能的一些代价），如下：

- unique_lock可以实现延时锁，即先生成unique_lock对象，然后在有需要的地方调用lock函数，lock_guard在对象创建时就自动进行lock操作了；
- unique_lock可以在需要的地方调用unlock操作，而lock_guard只能在其对象生命周期结束后自动Unlock；

正是由于这两个差异特性，unique_lock可以用于一次性锁多个锁以及用于条件变量的搭配使用，而lock_guard做不到。

**通过unique_lock锁多个锁：**

```
std::unique_lock<std::mutex> lk1(mutex1, std::defer_lock);
 std::unique_lock<std::mutex> lk2(mutex2, std::defer_lock);
 std::lock(lk1, lk2);
```

**通过unique_lock与条件变量一起使用：**

```
std::condition_variable cvar;
 std::mutex mmutex;
 std::unique_lock<std::mutex> lock(mmutex);
```

等待线程：

```
cvar.wait(lock, [&, this]() mutable throw() -> bool{ return this->isReady(); });
```

唤醒线程：

```
std::lock_guard<std::mutex> guard(mmutex);
 flag = true;
 std::cout<<"Data is ready"<<std::endl;
 cvar.notify_one();
```

guarded_by(x) 声明



#### 99.编译链接

**编译过程：**

1.预处理--将 xx.cpp 源文件预处理成 xx.i 文件

```
g++ -E demo.cpp -o demo.i
```

复制代码

2.编译--将 xx.i 文件编译为 xx.s 的汇编文件。此时只进行编译生成汇编代码，而不对代码以汇编的方式调试

```
g++ -S demo.i -o demo.s
```

复制代码

3.汇编--将 xx.s 文件汇编成 xx.o 的二进制目标文件

```
g++ -c demo.s -o demo.o
```

复制代码

4.链接--将 xx.o 二进制文件进行链接，最终生成可执行程序

```
g++ demo.o -o demo.out
```



### 二、计算机网络

1、OSI7层网络模型：应用层、表示层、会话层、运输层、网络层、链路层  、物理层 

2、TCP IP四层网络模型：应用层、运输层、网际层、接口层  

 综合OSI与TCP IP模型，学习五层网络模型：

 从上向下架构：应用层、运输层、网络层、链路层、物理层 

 链路层： 

 3、MTU 

 4、MAC地址  

 网络层：

 5、地址解析协议

 6、为啥有IP地址还需要MAC地址？同理，为啥有了MAC地址还需要IP地址？ 

7、网络层转发数据报的流程 

8、子网划分、子网掩码 

9、网络控制报文协议ICMP 

10、ICMP应用举例：PING、traceroute  

 运输层： 

11、TCP与UDP的区别及应用场景

12、TCP首部报文格式（SYN、ACK、FIN、RST必须知道）

13、TCP滑动窗口原理 

14、TCP超时重传时间选择 

15、TCP流程控制 

16、TCP拥塞控制  （一定要弄清楚与流量控制的区别） 

17、TCP三次握手及状态变化。为啥不是两次握手？ 

18、TCP四次挥手及状态变化。为啥不是三次挥手？ 

19、TCP连接释放中TIME_WAIT状态的作用 

20、SYN泛洪攻击。如何解决？ 

21、TCP粘包 

22、TCP心跳包 

23、路由器与交换机的区别 

24、UDP如何实现可靠传输 

应用层：

 25、DNS域名系统。采用TCP还是UDP协议？为什么？

26、FTP协议（了解） 

27、HTTP请求报文与响应报文首部结构 

28、HTTP1.0、HTTP1.1、HTTP2.0对比 

29、HTTP与HTTPS对比 

30、HTTPS加密流程 

31、方法：GET、HEAD、POST、PUT、DELETE

32、状态码：1 、2 b 、3 、4 b 、5** 

33、cookie与session区别

34、输入一个URL到显示页面的流程（越详细越好，搞明白这个，网络这块就差不多了）  

参考书籍： 《计算机网络》（第5版）、《TCP IP详解卷1：协议》、《图解HTTP》 


下载地址：https://pan.baidu.com/share/init?surl=fRYNn3E0yEOLiQUSyBlxKg

 提取码：69dc  

###  三、操作系统（1个月）

1、进程与线程区别 

2、线程同步的方式：互斥锁、自旋锁、读写锁、条件变量 

3、互斥锁与自旋锁的底层区别 

4、孤儿进程与僵尸进程 

5、死锁及避免 

6、多线程与多进程比较 

7、进程间通信：PIPE、FIFO、消息队列、信号量、共享内存、socket 

8、管道与消息队列对比 

9、fork进程的底层：读时共享，写时复制 

10、线程上下文切换的流程 

11、进程上下文切换的流程 

12、进程的调度算法 

13、阻塞IO与非阻塞IO 

14、同步与异步的概念 

15、静态链接与动态链接的过程 

16、虚拟内存  概念（非常重要） 

17、MMU地址翻译的具体流程 

18、缺页处理过程 

19、缺页置换算法：最久未使用算法、先进先出算法、最佳置换算法
 ps:操作系统的内容看起来不是很多，实际上每个问题答案的底层原理要弄懂还是很考验基本功的。比如：互斥锁与自旋锁的区别，实际上涉及到阻塞时线程的状态是不一样的。互斥锁阻塞的线程是挂起的，此时系统会优先执行其它可执行的线程，就会将阻塞的线程切换到可执行线程，而当临界区执行的时间非常短时，此时线程切换频繁、开销较大，此时就会采用自旋锁的方式，让阻塞的线程处于忙等状态。

参考书籍：《深入理解计算机系统 》
下载地址：https://pan.baidu.com/share/init?surl=RoDN317X-C6izxY6CwuxTA
提取码：iy8u  

### 四、网络编程（1个月）

1、多路复用  ：select、poll、epoll的区别（非常重要，几乎必问，回答得越底层越好，要会使用） 

2、手撕一个最简单的server端服务器（socket、bind、listen、accept这四个API一定要非常熟练） 

3、线程池 

4、基于事件驱动的reactor模式 

5、边沿触发与水平触发的区别 

6、非阻塞IO与阻塞IO区别  

参考书籍： b 《Unix网络编程》 ps:网络编程掌握以上几点就够了，要搞明白还是要花很久时间的。

下载地址：https://pan.baidu.com/share/init?surl=MD9WAAmSOCz5fhlUMU0bsg
 密码:bwyt 



### 五、数据结构与算法及刷题

1、数组 2、链表 3、栈 4、队列 5、堆 6、二叉树  ：二叉搜索树、平衡树、红黑树 7、B树、B+树 
8、哈希表及哈希冲突   
9、排序算法：冒泡排序、简单选择排序、插入排序、希尔排序、归并排序  、堆排序、快速排序 （要求能够面试时手写出堆排序和快速排序） 
10、二分法：旋转数组找target 11、回溯法：全排列、复原IP地址 12、动态规划（掌握基本的动态规划的几个题其实就够了，如：斐波那契数列  、接雨水、股票的最佳买入时机）  
参考书籍： 《图解算法》《剑指offer  》
ps:建议刷题与数据结构算法同时进行，这样理解得更深入。刷题网站leetcode，刷完《剑指offer》其实就能解决大部分面试手撕了。  

书籍下载：https://pan.baidu.com/s/1GZEp8KI1Fm0U4Uek7BOWAw
提取码：am4o  



### 六、mySQL数据库（7天~15天） 

1、数据存储引擎：InnoDB、myISAM、Memory 

2、数据库索引类型及原理：B+树索引、哈希表索引 

3、锁：悲观锁  、乐观锁   

4、事务：事务的四大特性（ACID）、事务并发的三大问题、事务隔离级别及实现原理 

5、多版本并发控制实现机制（MCVV）原理  

参考书籍： 《高性能MySQL》 ps:这里也可以参考本人写的博客：mysql知识点总结。
下载地址：https://pan.baidu.com/s/1-_9Cwq1qCxAr041nDWe0sg
提取码：fy0y 

### 七、项目（2个月） 

如果时间够的话就可以写一个项目，其实就是根据陈硕大神写的《Linux高性能服务器编程：使用muduo C++网络库》进行改编，当然啦，读懂这本书还是很耗时的，学习其中的思想也会受益匪浅的。 
对于非科班的学生来说，大部分都没有充足的时间的，这时候建议尽量把C++基础、计算机网络、操作系统、网络编程、数据结构与算法这五个部分的内容学得很扎实，大概6个月的时间。

资料推荐： 无意中发现一位大佬的 C++ 刷题 pdf 笔记 

https://zhuanlan.zhihu.com/p/411085520



相关文档：

1.https://www.zhihu.com/question/20291953/answer/2272142778

2.https://blog.csdn.net/weixin_55305220/article/details/118163216

3.https://blog.csdn.net/caoshangpa/article/details/79129258