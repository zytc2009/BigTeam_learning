[toc]

### 容器使用

#### 删除和查找

```c++
//vector
std::vector<std::string> vector;
vector<string>::iterator it = v.begin();
for(; it != v.end(); ++it){ cout<<(*it)<<" ";}
for (auto val : valList)
{
    cout << val << endl;
}
vector.pop_back(); //移除了最后一个元素,但是容量不变
std::swap(std::begin(vector)+1,std::end(vector)-1);//将第二个元素和最后一个元素互相交换。然后调用 pop_back() 移除最后一个元素，这样就从容器中移除了第二个元素。
vector.shrink_to_fit();//去掉容器中多余的容量
auto iter = vector.erase(std::begin(vector)+1); //删除一个元素后，vector 的大小减 1；但容量不变
auto iter = std::remove(vector.begin(), vector.end(), "none");//移除了所有匹配string("none")的元素,size不变，用空串代替，如果是数字，用最后一个代替
words.erase(iter, std::end(vector));//移除末尾空串
//合并写法：
vector.erase(remove(vector.begin(), vector.end(), 3) /*,vector.end()*/);

//map遍历
map<int,string*>::iterator it;
for(it=m.begin();it!=m.end();)
{
        cout<<"key: "<<it->first <<" value: "<<*it->second<<endl;
        if(*it->second != "123"){
            it++;
            continue;
        }
        delete it->second;
        m.erase(it++);//删除后，移向下一个
}

//list
std::list<std::string> names {"Jane","Jim", "Jules", "July", "July"};
names.emplace_back("Ann");
std::string name("Alan");
names.emplace_back(std::move(name)); names.emplace_front("Hugo");
names.emplace(++begin(names), "Hannah");
for(const auto& name : names)
    std::cout << name << std::endl;
//移除所有值为val的元素
list.remove("July")
list.remove_if(op)	移除所有符合op(elem)为true的元素
list.clear()
 
//deque    
deque 是 double-ended queue 的缩写
deque 容器擅长在序列尾部添加或删除元素（时间复杂度为O(1)），而不擅长在序列中间添加或删除元素。
deque 容器也可以根据需要修改自身的容量和大小。
deque 还擅长在序列头部添加或删除元素，所耗费的时间复杂度也为常数阶  
deque 容器中存储元素并不能保证所有元素都存储到连续的内存空间中。
    
```

#### 赋值：

```c++
vector<int> v1;
for(int i=0;i<5;i++)//创建道具v1并给其插入元素，为之后验证服务
v1.push_back(i+1);
  
vector<int> v3;
v3.assign(v1.begin()+1,v1.end()-1);//赋值方法2：区间数据拷贝赋值

std::map<std::string, std::string> a;
std::map<std::string, std::string> b;
a.insert(b.begin(), b.end());//插入
```

#### 排序

```c++
std::vector<std::string> v;
std::sort(v.begin(), v.end());

std::vector<int> v;
std::sort(v.begin(), v.end(), [](int a, int b){
        return a<b; //正序
});
```

#### emplace_back与push_back

|                              | push_back        | emplace_back     |
| ---------------------------- | ---------------- | ---------------- |
| 是否支持右值引用             | 支持             | 支持             |
| 是否一定会发生拷贝构造       | 一定             | 不一定           |
| 是够支持直接传入多个构造参数 | 支持一个构造参数 | 支持多个构造参数 |
| 是够支持原地构造             | 不支持           | 支持             |

```c++
push_back
//支持右值引用：
//源码    
void push_back(value_type&& __x)
      { emplace_back(std::move(__x)); }
//测试
 std::string s1 = "hello";
    vector<std::string> strVec;
    strVec.push_back(std::move(s1));
    cout<<"s1="<<s1<<endl;
//总是会进行拷贝构造
vector<BaseClassTwoPara> vl;
vl.push_back(888);//构造一个临时对象，然后拷贝构造，将这个对象复制到容器中，然后立马析构掉临时对象
vl.push_back(BaseClassTwoPara(1,2));
BaseClassTwoPara b1(1, 2);
vl.push_back(std::move(b1))
    
emplace_back:
vector<BaseClassTwoPara> vl;
vl.emplace_back(888,777);//只是构造，不用拷贝构造
vl.emplace_back(BaseClassTwoPara(1,2));跟push_back相同，拷贝构造，复制到容器中，析构掉临时对象
```

### 线程:





### 线程同步:

```
std::mutex m_Mutex;
std::unique_lock<std::mutex> lock(m_Mutex);
```

### unique_ptr实现原理

```

```

### 智能指针转换：

```
1、std::static_pointer_cast()：当指针是智能指针时候，向上转换（子类转父类），用static_cast 则转换不了，此时需要使用static_pointer_cast。

2、std::dynamic_pointer_cast()：当指针是智能指针时候，向下转换，用dynamic_cast 则转换不了，此时需要使用dynamic_pointer_cast。

3、std::const_pointer_cast()：功能与std::const_cast()类似

4、std::reinterpret_pointer_cast()：功能与std::reinterpret_cast()类似
```

### 不同类型转换

```c++
class AbstractClassA {
public:
    virtual void someFunction() = 0;
    virtual void function2(std::string id, std::string method) = 0;
};
class ConcreteClassA:public AbstractClassA{
public:
    virtual void someFunction()override{
        cout<<"ConcreteClassA";
    }
    virtual void function2(std::string id, std::string method)override{
        cout<<"messageId="<<messageId.c_str()<<","<<method.c_str();
    }
};

class AbstractClassB {
public:
    virtual void someFunction() = 0;
    virtual void function5(std::string id, std::string method) = 0;
};

std::shared_ptr<AbstractClassA> objectA = std::make_shared<ConcreteClassA>();
AbstractClassB* objectB = reinterpret_cast<AbstractClassB*>(objectA.get());
if (objectB != nullptr) {//Change succ
    objectB->someFunction();
} 
```

### std::forward和std::move

```
std::move(dependencies) 使用
局部变量引用传递
void test(TestData &t){}
void XXX::dataTest()
{
  TestData data;
  test(std::move(data));//
}
```

### 字符串处理

#### 查找

```
从pos位置开始，查找子串str。如果找到，则返回该子字符串首次出现时其首字符的索引；否则，返回string::npos
size_type find(const string & str, size_type pos = 0) const	
size_type find(const char * s, size_type pos = 0) const	
size_type find(const char * s, size_type pos = 0, size_type n) const
size_type find(const char ch, size_type pos = 0) const

查找子字符串或字符最后一次出现的位置。
size_type rfind(const string & str, size_type pos = npos) const;
size_type rfind(const char * s, size_type pos = npos) const;
size_type rfind(const char * s, size_type pos = npos, size_type n) const;
size_type rfind(const char ch, size_type pos = npos) const;

字符串中查找参数中任何一个字符首次出现的位置，而不是整体字串
size_type find_first_of(const string & str, size_type pos = 0) const;
size_type find_first_of(const char * s, size_type pos, size_type n) const;
size_type find_first_of(const char * s, size_type pos = 0) const;
size_type find_first_of(char c, size_type pos = 0) const;

在字符串中查找参数中任何一个字符最后一次出现的位置。
size_type find_last_of(const string & str, size_type pos = npos) const;
size_type find_last_of(const char * s, size_type pos, size_type n) const;
size_type find_last_of(const char * s, size_type pos = npos) const;
size_type find_last_of(char c, size_type pos = npos) const;

字符串中查找第一个不包含在参数中的字符
size_type find_first_not_of(const string & str, size_type pos = 0) const;
size_type find_first_not_of(const char * s, size_type pos, size_type n) const;
size_type find_first_not_of(const char * s, size_type pos = 0) const;
size_type find_first_not_of(char c, size_type pos = 0) const;

查找最后一个不包含在参数中的字符。
size_type find_last_not_of(const string & str, size_type pos = npos) const;
size_type find_last_not_of(const char * s, size_type pos, size_type n) const;
size_type find_last_not_of(const char * s, size_type pos = npos) const;
size_type find_last_not_of(char c, size_type pos = npos) const;
```

#### 替换

```c++
std::string str = "abc\ndd\tvcfd\n";
std::replace(3,2,"test");
std::replace(str.begin(),str.end(),'\n',' ');
std::string result = std::regex_replace(str, std::regex("\n\t"), "");
```



### 私有虚函数多态

```c++
class Base{
private:
    virtual string classID() const {
       return string("Base") ;
    }
public:
    void work() {
        cout<<"this class id is "<< classID() <<endl ; // 调用私有虚函数
    }
};

class DerivedA : public Base{
private:
    string classID() const {
       return string("DerivedA") ;
    }
};
```



### sizeof 使用规则及陷阱

```c++
int a = 0;
cout<<sizeof(a=3)<<endl;//=操作符返回左操作数的类型，所以a=3相当于int  
cout<<a<<endl;  
//输出为什么是4，0而不是期望中的4，3？？？就在于sizeof在编译阶段求值的特性。
```

### 编译优化问题：

```c++
processWidget(std::share_ptr<Widget>(new Widget), priority());
//由于new，priority， share_ptr的执行顺序不可控，容易发生内存泄露，替换成：
std::share_ptr<Widget> pw(new Widget);
processWidget(pw, priority());
```

```c++
//确保对象和成员被使用前已经初始化

```



### 接口易用

```c++
Date(int month, int day, int year);//使用起来容易犯错
//改为：
struct Day{
    explicit Day(int d):val(d){}
    int val;
}
Date(Month month, Day day, Year year);//Month可以用枚举
```

### 不想使用编译器自动生成的函数，就该明确拒绝

```
可以主动声明为私有，但是不实现
为了方便使用，可有定义基类 UnCopyable:
class UnCopyable{
protected:
	UnCopyable(){};
	~UnCopyable(){};
private:
 	UnCopyable(const UnCopyable&);//阻止copy
 	UnCopyable& operator=(const UnCopyable&);
}
子类只需要继承就可以了
```

### 尽量 pass-by-reference-to-const 替换 pass-by-value

```
bool isValidData(const DataClass& data);//避免值传递，引起拷贝构造
```

 不适用内置类型，以及STL 的迭代器和函数对象

### 设计与声明

#### 成员变量声明为private

只有两种封装：private （提供封装）和其他（不提供封装）

protected 并不比 public 根据封装性

#### 用non-member non-friend 函数替换member函数

可以增加封装性、包裹弹性（packaging flexibility）和机能扩充性

#### 若所有参数都需类型转换，使用non-member函数

#### 写一个不抛异常的swap函数

```c++
class WidgetImpl{}
class Widget{
public:
Widget& operator=(const Widget& rhs){
     *pImpl = *(rhs.pImpl);
}
void swap(Widget& other){
    using std::swap;//需要声明，否则用我们自定义的了
    swap(pImpl, other.pImpl);
}
private:
WidgetImpl* pImpl;
};

namespace std{
template <> //特化版本
void swap<Widget>(Widget& a, Widget& b){
    a.swap(b);
}
}
```

如果Widget和WidgetImpl编程模板类，就不行了

注意事项：

1）std::swap对你的类型效率不高时，提供swap成员函数，并保证这个函数不抛出异常

2）如果提供一个成员swap，也提供一个non-member swap用来调用前者。 对于类（非模板），需要特化std::swap

3)调用swap应针对std::swap 使用using声明式，然后调用swap并且不带任何“命名空间修饰”

4）为“用户定义类型”进行 std templates 全特化是最好的，但不要在std内添加不符合std标准的东西

### 实现

#### 变量定义尽可能靠近使用

尽量少的做转型，如果必要，刻意隐藏在函数后。宁可用C++-style 转型，不要用旧式转型

#### 避免返回handles（引用，指针，迭代器等）指向对象内部成分

```
class Rectangle{
public:
const Point& upperLeft()const{ return pData->ulpc;};//const成员函数，const 引用
}
```

#### 绝不重新定义继承来的non-virtual函数，绝不重新定义继承来的缺省参数值

### 模板

#### typename的双重意义

```c++
template <typename T>
void doTest(T& w){
    typename T::const_iterator itor(w.begin());//C++要求前面写typename，否则假定为非类型
    if(w.size()>0){
        w.print();
    }
};

template <typename T>
class Derived : public Base<T>::Nested{//base class lists不允许使用typename
public:
    explicit Derived(int x) 
    : Base<T>::Nested(x)//member init list 不允许使用 typename
    {
        typename Base<T>::Nested temp;   //嵌套从属类型
        ... //既不在base class lists， 也不在member init list中，
            //作为一个base class修饰符需要加 typename
    }
}
```

关键字typename和class可以互换，都表示类型，不建议使用class。

使用关键字typename标识嵌套从属类型名称;但不得在base class lists(基类列)或 member initialization list(成员初值列)内以他作为base class 修饰符

#### 处理模板化基类内的名称

```c++
template <typename Company>
class MsgSender{
public:
 void sendClear(const MsgInfo& info);
 void sendSecret(const MsgInfo& info);
}

template <typename Company>
class Derived : public MsgSender<Company>{//base class lists不允许使用typename    
public:
    //2)告诉编译器，使用假设
    using MsgSender<Company>::sendclear;
    void sendClear(const MsgInfo& info) 
    {
        this->sendClear(info);//1)调用base class 函数前加上this->    
        sendClear(info);//2)告诉编译器，使用假设
        MsgSender<Company>::sendClear(info);//3)明白指出使用的函数在base class内
    }
}
```

#### 将与参数无关的代码抽离templetes

- Templetes 生成多个classes和多个函数，所以template  代码不该与某个造成膨胀的template 参数产生相依关系
- 因非类型模板造成的代码膨胀，一般可以消除，做法是以函数参数或class成员变量替换template 参数
- 因类型参数造成的代码膨胀，一般可减低，做法：让带有完全相同的二进制表述的具现类型共享实现码

### 单例模式

```c++
///  加锁的懒汉式实现  //
//初始化静态成员变量
SingleInstance *SingleInstance::m_SingleInstance = nullptr;
std::mutex SingleInstance::m_Mutex;
// 注意：返回的是指针的引用
SingleInstance *& SingleInstance::GetInstance()
{
    //  这里使用了两个 if 判断语句的技术称为双检锁；好处是，只有判断指针为空的时候才加锁，
    //  避免每次调用 GetInstance的方法都加锁，锁的开销毕竟还是有点大的。
    if (m_SingleInstance == nullptr) 
    {
        std::unique_lock<std::mutex> lock(m_Mutex); // 加锁
        if (m_SingleInstance == nullptr)
        {
            m_SingleInstance = new (std::nothrow) SingleInstance();
        }
    }
    return m_SingleInstance;
}
```

```c++
//静态局部变量的懒汉单例
static Single &GetInstance();
Single &Single::GetInstance()
{
    static Single signal;
    return signal;
}
```

```c++
//饿汉式单例
// 代码一运行就初始化创建实例 ，本身就线程安全
Singleton* Singleton::g_pSingleton = new (std::nothrow) Singleton();
Singleton* Singleton::GetInstance()
{
    return g_pSingleton;
}
```

使用 C++11 std::call_once 实现单例（C++11线程安全）

```c++
static std::shared_ptr<Singleton> singleton = nullptr;
static std::once_flag singletonFlag;

std::shared_ptr<Singleton> Singleton::getSingleton() {
    std::call_once(singletonFlag, [&] {
        singleton = std::shared_ptr<Singleton>(new Singleton());
    });
    return singleton;
}
```



### 打印this

```c++
//Qt：quintptr
//C++:
char str[20];
snprintf(str,sizeof(str),"%p",p);


```

### 查看静态库和动态库符号，X86和x64

```shell
nm -S  xxx.a

dumpbin   /LINKERMEMBER   *.lib
dumpbin   /EXPORTS  *.dll  >1.txt
dumpbin.exe /headers  *.lib 查看x86还是x64
```

### 执行权限问题

```
.\schema\generate.ps1 : 无法加载文件 D:\xuexi\github\MNN\schema\generate.ps1，因为在此系统上禁止运行脚本。有关详细信息
，请参阅 https:/go.microsoft.com/fwlink/?LinkID=135170 中的 about_Execution_Policies。
```

解决办法：

```
vscode或者vs打开terminal  / 或者电脑搜索Windows PowerShell然后以管理员的身份打开并执行以下命令
执行：get-ExecutionPolicy，显示Restricted，表示状态是禁止的
执行：set-ExcutionPolicy RemoteSigned
这时再执行get-ExecutionPolicy，就显示RemoteSigned
之后就不会出现这种问题情况了
```

### Cmake生成vs工程

```
cmake -G"Visual Studio 16 2019" -S . -B build_x64
cmake -G"Visual Studio 15 2017" -S . -B build_x64
```

