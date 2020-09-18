### 一、Pandas简介

1、Python Data Analysis Library 或 pandas 是基于NumPy 的一种工具，该工具是为了解决数据分析任务而创建的。Pandas 纳入了大量库和一些标准的数据模型，提供了高效地操作大型数据集所需的工具。pandas提供了大量能使我们快速便捷地处理数据的函数和方法。你很快就会发现，它是使Python成为强大而高效的数据分析环境的重要因素之一。

2、Pandas 是python的一个数据分析包，最初由AQR Capital Management于2008年4月开发，并于2009年底开源出来，目前由专注于Python数据包开发的PyData开发team继续开发和维护，属于PyData项目的一部分。Pandas最初被作为金融数据分析工具而开发出来，因此，pandas为时间序列分析提供了很好的支持。 Pandas的名称来自于面板数据（panel data）和python数据分析（data analysis）。panel data是经济学中关于多维数据集的一个术语，在Pandas中也提供了panel的数据类型。

3、数据结构：

Series：一维数组，与Numpy中的一维array类似。二者与Python基本的数据结构List也很相近，其区别是：List中的元素可以是不同的数据类型，而Array和Series中则只允许存储相同的数据类型，这样可以更有效的使用内存，提高运算效率。

```
s = Series{[100, "Abc", “123”]}

index	data
0	    100
1	    Abc
2	    123
```

  可以用s.values, s.index获取数据和index， 索引默认从0开始 

```python
自定义索引
S = Series{[100, "Abc", “123”], index=["title", "subtitle", "mark"]}

sd = {"python":800, "c++":900, "c":1000}
s=Series(sd)
后面的数字是索引
```



Time- Series：以时间为索引的Series。

DataFrame：二维的表格型数据结构。很多功能与R中的data.frame类似。可以将DataFrame理解为Series的容器。以下的内容主要以DataFrame为主。

Panel ：三维的数组，可以理解为DataFrame的容器。

Pandas 有两种自己独有的基本数据结构。读者应该注意的是，它固然有着两种数据结构，因为它依然是 Python 的一个库，所以，Python 中有的数据类型在这里依然适用，也同样还可以使用类自己定义数据类型。只不过，Pandas 里面又定义了两种数据类型：Series 和 DataFrame，它们让数据操作更简单了。

### 二、Pandas安装

**因为pandas是python的第三方库所以使用前需要安装一下，直接使用pip install pandas 就会自动安装pandas以及相关组件。**

![img](https://images2018.cnblogs.com/blog/1258800/201711/1258800-20171127090534722-1603022827.png)

### 三、Pandas使用

**注：本次操作是在ipython中进行**

#### **1、导入pandas模块并使用别名，以及导入Series模块，以下使用基于本次导入。**

In [1]: from pandas import Series

In [2]: import pandas as pd

#### 2、Series

Series 就如同列表一样，一系列数据，每个数据对应一个索引值。

Series 就是“竖起来”的 list：

In [3]: s = Series([1,4,'ww','tt'])

In [4]: s
Out[4]:
0　　 1
1　　 4
2　　 ww
3　　 tt
dtype: object

另外一点也很像列表，就是里面的元素的类型，由你任意决定（其实是由需要来决定）。

这里，我们实质上创建了一个 Series 对象，这个对象当然就有其属性和方法了。比如，下面的两个属性依次可以显示 Series 对象的数据值和索引：

In [5]: s.index
Out[5]: RangeIndex(start=0, stop=4, step=1)

In [8]: s.values
Out[8]: array([1, 4, 'ww', 'tt'], dtype=object)

列表的索引只能是从 0 开始的整数，Series 数据类型在默认情况下，其索引也是如此。不过，区别于列表的是，**Series 可以自定义索引**：

In [9]: s2 = Series(['wangxing','man',24],index=['name','sex','age'])

In [10]: s2
Out[10]:
name 　　 wangxing
sex　　   man
age 　　  24
dtype: object

每个元素都有了索引，就可以根据索引操作元素了。还记得 list 中的操作吗？Series 中，也有类似的操作。先看简单的，**根据索引查看其值和修改其值**：

In [12]: s2['name']
Out[12]: 'wangxing'

In [45]: s2['name'] = 'wudadiao'

In [46]: s2
Out[46]:
name 　　 wudadiao
sex 　　　man
age　　　24
dtype: object

这是不是又有点类似 dict 数据了呢？的确如此。看下面就理解了。

读者是否注意到，前面定义 Series 对象的时候，用的是列表，即 Series() 方法的参数中，第一个列表就是其数据值，如果需要定义 index，放在后面，依然是一个列表。除了这种方法之外，还可以用下面的方法定义 Series 对象：

In [13]: sd = {'python':9000,'c++':9001,'c#':9000}

In [14]: s3 = Series(sd)

In [15]: s3
Out[15]:
c# 　　　9000
c++ 　　　9001
python 　　9000
dtype: int64

现在是否理解为什么前面那个类似 dict 了？因为**本来就是可以这样定义的**。

这时候，索引依然可以自定义。**Pandas 的优势在这里体现出来，如果自定义了索引，自定的索引会自动寻找原来的索引，如果一样的，就取原来索引对应的值**，这个可以简称为“自动对齐”。

In [16]: s4 = Series(sd,index=['java','c++','c#'])

In [17]: s4
Out[17]:
java 　　 NaN
c++ 　　 9001.0
c# 　　 9000.0
dtype: float64

在 Pandas 中，**如果没有值，都对齐赋给 `NaN。`**

**Pandas 有专门的方法来判断值是否为空。**

In [19]: pd.isnull(s4)
Out[19]:
java 　　 True
c++　　 False
c# 　　 False
dtype: bool

**此外，Series 对象也有同样的方法：**

In [20]: s4.isnull()
Out[20]:
java 　　 True
c++　　 False
c# 　　 False
dtype: bool

其实，对索引的名字，是**可以从新定义**的：

In [21]: s4.index = ['语文','数学','English']

In [22]: s4
Out[22]:
语文 　　 NaN
数学　　 9001.0
English 　　 9000.0
dtype: float64

对于 Series 数据，也可以做类似下面的运算（关于运算，后面还要详细介绍）：

In [23]: s4 * 2
Out[23]:
语文 　　 NaN
数学 　　 18002.0
English 　　 18000.0
dtype: float64

In [24]: s4[s4 > 9000]
Out[24]:
数学 9001.0
dtype: float64

Series就先简要写到这，下面看pandas的另一种数据结构DataFrame.

### DataFrame

DataFrame 是一种二维的数据结构，非常接近于电子表格或者类似 mysql 数据库的形式。它的竖行称之为 columns，横行跟前面的 Series 一样，称之为 index，也就是说可以通过 columns 和 index 来确定一个主句的位置。

首先来导入模块

In [27]: from pandas import Series,DataFrame

In [26]: data = {"name":['google','baidu','yahoo'],"marks":[100,200,300],"price":[1,2,3]}

In [28]: f1 = DataFrame(data)

In [29]: f1
Out[29]:
　　marks　　 name 　　 price
0 　　 100 　　 google 　　 1
1 　　 200 　　 baidu 　　 2
2　　 300 　　 yahoo 　　 3

这是**定义一个 DataFrame 对象的常用方法——使用 dict 定义**。字典的“键”（"name"，"marks"，"price"）就是 DataFrame 的 columns 的值（名称），字典中每个“键”的“值”是一个列表，它们就是那一竖列中的具体填充数据。上面的定义中没有确定索引，所以，按照惯例（Series 中已经形成的惯例）就是从 0 开始的整数。从上面的结果中很明显表示出来，这就是一个**二维的数据结构**（类似 excel 或者 mysql 中的查看效果）。

上面的数据显示中，columns 的顺序没有规定，就如同字典中键的顺序一样，但是**在 DataFrame 中，columns 跟字典键相比，有一个明显不同，就是其顺序可以被规定**，向下面这样做：

In [31]: f2 = DataFrame(data,columns=['name','price','marks'])

In [32]: f2
Out[32]:
　　name 　　 price 　　 marks
0 　google 　　1 　　   100
1 　baidu 　　 2　　    200
2　 yahoo 　　3 　　   300

跟 Series 类似的，**DataFrame 数据的索引也能够自定义**

In [35]: f3 = DataFrame(data,columns=['name','marks','price'],index=['a','b','c'])

In [36]: f3
Out[36]:
　　name 　　 marks 　　 price
a 　 google    100 　　   1
b   baidu      200 　　  2
c   yahoo     300       3

定义 DataFrame 的方法，除了上面的之外，还可以使用**“字典套字典”**的方式。

In [40]: newdata = {'lang':{'first':'python','second':'java'},'price':{'first':5000,'second':2000}}

In [41]: f4 = DataFrame(newdata)

In [42]: f4
Out[42]:
　　　　lang 　　 price
first　　 python 　 5000
second  java 　　 2000

在字典中就规定好数列名称（第一层键）和每横行索引（第二层字典键）以及对应的数据（第二层字典值），也就是在字典中规定好了每个数据格子中的数据，没有规定的都是空。

```
DataFrame 对象的 columns 属性，能够显示素有的 columns 名称。并且，还能用下面类似字典的方式，得到某竖列的全部内容（当然包含索引）：
>>> newdata = {"lang":{"firstline":"python","secondline":"java"}, "price":{"firstline":8000}} 
>>> f4 = DataFrame(newdata) 
>>> f4 
              lang     price 
firstline     python   8000 
secondline    java     NaN 
>>> DataFrame(newdata, index=["firstline","secondline","thirdline"]) 
              lang     price 
firstline     python   8000 
secondline    java     NaN 
thirdline     NaN      NaN DataFrame 对象的 columns 属性，能够显示素有的 columns 名称。并且，还能用下面类似字典的方式，得到某竖列的全部内容（当然包含索引）：
```

In [44]: f3['name']
Out[44]:
a google
b baidu
c yahoo
Name: name, dtype: object

下面操作是给同一列赋值

newdata1 = {'username':{'first':'wangxing','second':'dadiao'},'age':{'first':24,'second':25}}

In [67]: f6 = DataFrame(newdata1,columns=['username','age','sex'])

In [68]: f6
Out[68]:
　　username 　　 age 　　 sex
first wangxing 　　 24 　　 NaN
second dadiao　　 25 　　 NaN

In [69]: f6['sex'] = 'man'

In [70]: f6
Out[70]:
　　username  age 　　 sex
first wangxing　　 24 　 man
second dadiao 　　25   man

也**可以单独的赋值**，除了能够统一赋值之外，还能够“点对点”添加数值，结合前面的 Series，既然 DataFrame 对象的每竖列都是一个 Series 对象，那么可以先定义一个 Series 对象，然后把它放到 DataFrame 对象中。如下：

ssex = Series(['男','女'],index=['first','second'])

In [72]: f6['sex'] = ssex

In [73]: f6
Out[73]:
　　username 　　 age 　　sex
first wangxing　　  24 　　  男
second dadiao 　　 25 　　 女

还可以更精准的修改数据吗？当然可以，完全仿照字典的操作：

In [74]: f6['age']['second'] = 30

In [75]: f6
Out[75]:
　　username 　　 age 　　 sex
first wangxing 　　 24 　　    男
second dadiao 　　30 　　　　女







参考http://wiki.jikexueyuan.com/project/start-learning-python/312.html所整理。





