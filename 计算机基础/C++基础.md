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
auto iter = std::remove(std::begin(vector), std::end(vector), "none");//移除了所有匹配string("none")的元素,size不变，用空串代替
words.erase(iter, std::end(vector));//移除末尾空串

//map遍历
map<int,string*>::iterator it;
for(it=m.begin();it!=m.end();)
{
        cout<<"key: "<<it->first <<" value: "<<*it->second<<endl;
        delete it->second;
        m.erase(it++);
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

### 排序

```c++
std::vector<std::string> v;
std::sort(v.begin(), v.end());

std::vector<int> v;
std::sort(v.begin(), v.end(), [](int a, int b){
        return a<b; //正序
});
```



### 线程:





### 线程同步:

```
std::mutex m_Mutex;
std::unique_lock<std::mutex> lock(m_Mutex);
```



### unique_ptr实现原理





### 智能指针转换：

```
1、std::static_pointer_cast()：当指针是智能指针时候，向上转换（子类转父类），用static_cast 则转换不了，此时需要使用static_pointer_cast。

2、std::dynamic_pointer_cast()：当指针是智能指针时候，向下转换，用dynamic_cast 则转换不了，此时需要使用dynamic_pointer_cast。

3、std::const_pointer_cast()：功能与std::const_cast()类似

4、std::reinterpret_pointer_cast()：功能与std::reinterpret_cast()类似
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

如何一层一层地遍历一棵树,
BFS.
 1 // 给定跟节点 求出BFS遍历二叉树的锅。
 2     public List<TreeNode> Bfs_tree(TreeNode root){
 3         Queue<TreeNode> myq = new LinkedList<>();
 4         List<TreeNode> res = new ArrayList<>();
 5         if(root==null) return null;
 6         myq.add(root);
 7         while(!myq.isEmpty()){
 8             int len = myq.size();
 9             for(int i=0;i<len;i++){
10                 if(myq.peek().left!=null) myq.add(myq.peek().left);
11                 if(myq.peek().right!=null) myq.add(myq.peek().right);
12                 res.add(myq.poll());
13             }
14         }
15         return res;
16     }

2.堆排序和快排的区别：
平均都是nlogn
堆排序最坏复杂度nlogn , 快排 n*n
最排序空间复杂度(辅助空间),堆排序O(1),快排O(logn~n)
但是快排可以在大多数计算机体系上比较高效地完成.所以平均来说是最快的.

堆排序使用二叉树,快排本质是分治思想.

编程题：
5条赛道，25个人选前三的那个题：
5轮，每次第一再比一次：a1,b1,c1,d1,f1。然后a2,a3,b1,b2,c1选2,3

  
一个是砝码称重问题：https://www.cnblogs.com/fangyan5218/p/4589289.html

int sum;  ///表示输入的砝码的总质量
int ma[6];  ///六种砝码的个数
int weight[6]={1,2,3,5,10,20};  ///六种砝码的重量
char dp[1001]; ///标记位
void exeDP()
{
    int i,j,z;
    dp[0]=1;
    for(i=0;i<6;i++)    ///六种砝码
    {
        for(j=0;j<ma[i];j++)    ///每种砝码的个数
        {
            for(z=sum;z>=weight[i];z--) ///判断每种质量是否可以被称出
            {
                if(dp[z-weight[i]]==1)
                    dp[z]=1;
            }
        }
    }
}

