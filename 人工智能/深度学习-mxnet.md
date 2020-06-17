2.预备知识

数据操作：

```
from mxnet import nd   //导包

x = nd.arange(12)  //创建一个行向量
x.reshape((3, 4)) //把行向量x的形状改为(3, 4)

数据初始化
nd.zeros((2, 3, 4)) //创建一个各元素为0，形状为(2, 3, 4)的张量
nd.ones((3, 4)) //创建各元素为1的张量。
nd.array([[2, 1, 4, 3], [1, 2, 3, 4], [4, 3, 2, 1]])

X * Y， X + Y  元素运算，对应位的元素进行运算，如果行列数不一致，会触发广播机制
nd.dot(X, Y.T) X与Y的转置矩阵相乘

索引
X[1:3] 左闭右开指定范围,第一行到第三行元素，不含第三行
X[1, 2] = 9  第1行第2列元素赋值
X[1:2, :] = 12  第一行所有元素赋值

内存开销
Y = Y + X    //Y是新的内存
Z = Y.zeros_like() //创建和Y形状相同且元素为0的NDArray
Z[:] = X + Y  //X+Y的结果通过[:]写进Z对应的内存,但是X+Y的结果依然开了临时内存
nd.elemwise_add(X, Y, out=Z) 使用运算符全名函数中的out参数，避免临时内存开销

NDArray和NumPy相互变换
import numpy as np
P = np.ones((2, 3))
D = nd.array(P) //NumPy转NDArray
D.asnumpy()  //NDArray实例变换成NumPy

```

广播机制

```
A = nd.arange(3).reshape((3, 1))
B = nd.arange(2).reshape((1, 2))
要计算A + B，那么A中第一列的3个元素被广播（复制）到了第二列，而B中第一行的2个元素被广播（复制）到了第二行和第三行。如此，就可以对2个3行2列的矩阵按元素相加。
```

自动求梯度

```
x.attach_grad() //申请存储梯度所需要的内存。
with autograd.record()://要求MXNet记录与求梯度有关的计算,默认不记录
     y = 2 * nd.dot(x.T, x)
y.backward() //自动求梯度
autograd.is_training() //查看是否是训练模式

X.asscalar() 将向量X转换成标量，且向量X只能为一维含单个元素的向量
```

查阅文档

```
查找模块里的所有函数和类
print(dir(nd.random))  dir查看nd.random模块中所有的成员或属性
help(nd.ones_like)  help查找特定函数和类的使用
```

























参考文章：

1.[深度学习](https://zh.d2l.ai/)

