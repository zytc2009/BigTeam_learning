![](images\c++style.jpg)

[toc]

### 命名规范

#### 1.文件命名

​	文件名要全部小写, 可以包含下划线 (_) 

#### 2.类型命名

​	每个单词首字母均大写, 不包含下划线

```
MyExcitingClass, MyExcitingEnum
```

#### 3.变量命名

  变量 (包括函数参数) 和数据成员名一律小写, 单词之间用下划线连接. 
  类的成员变量以下划线结尾, 但结构体不用。

```c++
string table_name;

class TableInfo {
  ...
 private:
  string table_name_;  // 好 - 后加下划线.
  static Pool<TableInfo>* pool_;  // 好.
};
```

#### 4.常量命名

   声明为 constexpr 或 const 的变量, 或在程序运行期间其值始终保持不变的, 
   命名时以 “k” 开头, 大小写混合

```
const int kDaysInAWeek = 7;
```

#### 5.函数命名

```
//常规函数使用大小写混合,大驼峰
MyExcitingFunction(), MyExcitingMethod()

//取值和设值函数则要求与变量名匹配
SetPropertyA(), GetPropertyA()  //变量名propertyA_ 
```

#### 6.命名空间命名

  	命名空间以小写字母命名.注意命名冲突

```c++
webrtc， 
```

#### 7.枚举命名

枚举的命名应当和宏 一致:   ENUM_NAME.
枚举优先使用类型枚举

```c++
k开头后面使用大驼峰
enum class H264Profile {
  kProfileBaseline,
  kProfileMain,
};
```

#### 8.宏命名

全部大写, 使用下划线。 尽量不要使用宏。用const代替

```c++
#define ROUND(x) ...
#define PI_ROUNDED 3.0
```

#### 9.接口类

以 `Interface` 为后缀 (强制).

### 注释规范

// 或 /* */ 都可以;  但 // 更 常用. 

文件头的版权说明用/* */ ， 文中注释一般都是//

**TODO**：

 	对那些临时的, 短期的解决方案, 或已经够好但仍不完美的代码使用 `TODO` 注释.

 **DEPRECATED**

通过弃用注释（`DEPRECATED` comments）以标记某接口点已弃用.



### 对齐规范

#### 1.括号对齐 (webrtc源码遵循webrtc的括号对齐方式)

```
左右括号在同一列
```

#### 2.函数参数

函数参数遵循**入参在前，出参在后**

```c++
ReturnType LongClassName::ReallyReallyReallyLongFunctionName(//放不下可以对形参分行
    Type par_name1,  // 4 space indent
    Type par_name2,
    Type par_name3) 
{
    DoSomething();  // 4 space indent
  ...
}
```

#### 3.条件语句

```c++
if (condition) //一般圆括号内没空格
{  
    ...  // 4空格缩进.
} 
else 
{
    ...
}
```

#### 4.循环和开关

```c++
for (int i = 0; i < kSomeNumber; ++i) 
{
    printf("I take it back\n"); //单语句，可不用括号
}
switch (var) 
{
    case 0: 
  	{  // 4空格缩进
    		...      // 4空格缩进
    		break;
  	}
  	default: //单语句，可不用括号
  	{
    		assert(false);
  	}
}
```

#### 5.命名空间格式化

```C++
namespace foo 
{//声明嵌套命名空间时, 每个命名空间都独立成行.
namespace bar 
{

void foo() 
{  // 正确. 命名空间内没有额外的缩进.
  ...
}

} // namespace
}  // namespace
```

### 常用规范：

#### 1.函数参数

```c++
//不可修改参数，使用const &修饰，避免不必要的构建 
void method(const std::string &id) 
void method(const std::vector<UserInfo> &userList)
```

#### 2.纯虚类

```c++
//析构函数不要丢 
class RtcListener
{    
public:    
    virtual ~RtcListener() = default;        
    virtual void method()=0; 
}
```

#### 3.lamda表达式

```c++
//[=]捕获外部所有变量，并作为副本在函数中使用 
//[&]捕获外部所有变量，并作为引用在函数体中使用，如果跨线程取，会出现数据异常 
//也可以直接传递变量 
void stateChanged(const std::string &sdkId)
{    
    listenerPool->enqueue([=](){        
        cout<<sdkId.c_str();    
    });    
    
    listenerPool->enqueue([sdkId](){
        cout<<sdkId.c_str();    
    }); 
}
//注意：除非特殊情况，一般没有返回值, 不要出现return xxx;
```
