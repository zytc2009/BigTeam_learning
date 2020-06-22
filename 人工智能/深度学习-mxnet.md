### 1.环境搭建

 第一步是根据操作系统下载并安装[Miniconda](https://conda.io/en/master/miniconda.html)

 第二步是下载[全部代码](https://zh.d2l.ai/d2l-zh-1.1.zip)

 第三步是使用conda创建虚拟（运行）环境      

```
配置国内镜像来加速下载:
# 配置清华PyPI镜像（如无法运行，将pip版本升级到>=10.0.0）
pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple

使用conda创建虚拟环境并安装本书需要的软件。这里environment.yml是放置在代码压缩包中的文件
#切换到代码解压路径下，执行
conda env create -f environment.yml

若使用国内镜像后出现安装错误，首先取消PyPI镜像配置，即执行命令pip config unset global.index-url。然后重试命令conda env create -f environment.yml。
```

第四步是激活之前创建的环境。激活该环境是能够运行本书的代码的前提

```
conda activate gluon  # 若conda版本低于4.4，使用命令activate gluon
注意：退出虚拟环境，可使用命令 conda deactivate
```

第五步是打开Jupyter记事本  

```
jupyter notebook
//代码会自动下载数据集和预训练模型，并默认使用美国站点下载,国内用户可以使用一下命令启动
set MXNET_GLUON_REPO=https://apache-mxnet.s3.cn-north-1.amazonaws.com.cn/
jupyter notebook
```

这时在浏览器打开 [http://localhost:8888](http://localhost:8888/) （通常会自动打开）就可以查看和运行本书中每一节的代码了。

Enjoy yourself!

AI旅程开始了。。。。。。。。。。。。。。。。。。。。。。。。。。。

### 2.预备知识

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

numpy.random.normal(loc=0.0, scale=1.0, shape=None)  loc均值，scale标准差，shape输出的形状，默认是None，只输出一个值
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

### 3.深度学习基础知识

**3.1线性回归**

基本要素：模型，训练（数据，损失函数，解析解和数值解，模型预测）

表示方法：单层神经网络， 矢量计算

**3.2 从零开始**

```
题目：生成数据集
训练数据集样本数为1000，输入个数（特征数）为2。给定随机生成的批量样本特征X∈R<sup>1000×2</sup>，我们使用线性回归模型真实权重w=[2,−3.4]<sup>⊤</sup>和偏差b=4.2，以及一个随机噪声项ϵ来生成标签
      y=Xw+b+ϵ,
其中噪声项ϵϵ服从均值为0、标准差为0.01的正态分布。噪声代表了数据集中无意义的干扰
```

```
#省略导包，具体参考原文，见本文末尾
num_inputs = 2
num_examples = 1000
true_w = [2, -3.4]
true_b = 4.2
features = nd.random.normal(scale=1, shape=(num_examples, num_inputs))
labels = true_w[0] * features[:, 0] + true_w[1] * features[:, 1] + true_b
labels += nd.random.normal(scale=0.01, shape=labels.shape)
#features的每一行是一个长度为2的向量，而labels的每一行是一个长度为1的向量（标量）。
```

用散点图显示：

```
def use_svg_display():
    # 用矢量图显示
    display.set_matplotlib_formats('svg')

def set_figsize(figsize=(3.5, 2.5)):
    use_svg_display()
    # 设置图的尺寸
    plt.rcParams['figure.figsize'] = figsize

set_figsize()
plt.scatter(features[:, 1].asnumpy(), labels.asnumpy(), 1);  # 加分号只显示图
#plt作图函数以及use_svg_display函数和set_figsize函数定义在d2lzh包
```

效果图：

![散点图](images\散点图.png)

读取数据集：

```
# 定义函数,每次返回batch_size（批量大小）个随机样本的特征和标签
# 本函数已保存在d2lzh包中方便以后使用
def data_iter(batch_size, features, labels):
    num_examples = len(features)
    indices = list(range(num_examples))
    random.shuffle(indices)  # 样本的读取顺序是随机的
    for i in range(0, num_examples, batch_size):#0-采样数，步长为batch_size
        j = nd.array(indices[i: min(i + batch_size, num_examples)])
        yield features.take(j), labels.take(j)  # take函数根据索引返回对应元素
        
测试一下：
batch_size = 10
for X, y in data_iter(batch_size, features, labels):
    print(X, y)
    break
```

初始化模型参数

```
w = nd.random.normal(scale=0.01, shape=(num_inputs, 1))
b = nd.zeros(shape=(1,))  //偏差初始化成0
//创建梯度
w.attach_grad()
b.attach_grad()
```

定义模型

```
def linreg(X, w, b):  # 本函数已保存在d2lzh包中方便以后使用
    return nd.dot(X, w) + b
```

定义损失函数

```
#平方损失定义线性回归的损失函数，公式参考样本误差表达式
def squared_loss(y_hat, y):  # 本函数已保存在d2lzh包中方便以后使用
    return (y_hat - y.reshape(y_hat.shape)) ** 2 / 2
```

定义优化算法

```
#小批量随机梯度下降算法。自动求梯度模块计算得来的梯度是一个批量样本的梯度和。我们将它除以批量大小来得到平均值。
def sgd(params, lr, batch_size):  # 本函数已保存在d2lzh包中方便以后使用
    for param in params:
        param[:] = param - lr * param.grad / batch_size
```

开始训练模型

```
lr = 0.03 #学习率
num_epochs = 3 #训练周期
net = linreg #模型
loss = squared_loss #损失函数

for epoch in range(num_epochs):  # 训练模型一共需要num_epochs个迭代周期
    # 在每一个迭代周期中，会使用训练数据集中所有样本一次（假设样本数能够被批量大小整除）。X
    # 和y分别是小批量样本的特征和标签
    for X, y in data_iter(batch_size, features, labels):
        with autograd.record():
            l = loss(net(X, w, b), y)  # l是有关小批量X和y的损失
        l.backward()  # 小批量的损失对模型参数求梯度
        sgd([w, b], lr, batch_size)  # 使用小批量随机梯度下降迭代模型参数
    train_l = loss(net(features, w, b), labels)
    print('epoch %d, loss %f' % (epoch + 1, train_l.mean().asnumpy()))
```

训练完成后，我们可以比较学到的参数和用来生成训练集的真实参数。它们应该很接近。

 true_w, w    true_b, b

3.3简洁实现：

仍然用3.2的代码生成的数据集，我们用Gluon提供的data包读取数据，我们定义别名gdata

```
from mxnet.gluon import data as gdata

batch_size = 10
# 将训练数据的特征和标签组合
dataset = gdata.ArrayDataset(features, labels)
# 随机读取小批量
data_iter = gdata.DataLoader(dataset, batch_size, shuffle=True)
```

定义模型，导入`nn`模块，“nn”是neural networks（神经网络）的缩写。我们先定义一个模型变量`net`，它是一个`Sequential`实例。在Gluon中，`Sequential`实例可以看作是一个串联各个层的容器。在构造模型时，我们在该容器中依次添加层。当给定输入数据时，容器中的每一层将依次计算并将输出作为下一层的输入。

```
from mxnet.gluon import nn
net = nn.Sequential()#定义模型
#线性回归的输出层又叫全连接层,全连接层是一个Dense实例。我们定义该层输出个数为1。
net.add(nn.Dense(1))  #在Gluon中我们无须指定每一层输入的形状

net.initialize(init.Normal(sigma=0.01)) #初始化模型参数
loss = gloss.L2Loss()  #平方损失又称L2范数损失
#创建训练实例，学习率0.03
trainer = gluon.Trainer(net.collect_params(), 'sgd', {'learning_rate': 0.03})
#开始模型训练
num_epochs = 3
for epoch in range(1, num_epochs + 1):
    for X, y in data_iter:
        with autograd.record():
            l = loss(net(X), y)
        l.backward() #计算梯度
        trainer.step(batch_size) //梯度下降迭代
    l = loss(net(features), labels)
    print('epoch %d, loss: %f' % (epoch, l.mean().asnumpy()))
 
得到结果，权重（weight）和偏差（bias）
dense = net[0]
true_w, dense.weight.data()
true_b, dense.bias.data()
```

### 3.4. softmax回归

线性回归模型适用于输出为连续值的情景，如果输出是离散的，可以使用诸如softmax回归在内的分类模型

softmax回归模型，我们仍然采用将输入特征与权重做线性叠加，，与线性回归的一个主要不同在于，softmax回归的输出值个数等于标签里的类别数

我们将使用一个图像内容更加复杂的Fashion-MNIST数据集 ，Gluon的`data`包来下载这个数据集，第一次调用时会自动从网上获取数据

```
mnist_train = gdata.vision.FashionMNIST(train=True)#训练数据集
mnist_test = gdata.vision.FashionMNIST(train=False)#测试数据集
```

Fashion-MNIST中一共包括了10个类别，分别为t-shirt（T恤）、trouser（裤子）、pullover（套衫）、dress（连衣裙）、coat（外套）、sandal（凉鞋）、shirt（衬衫）、sneaker（运动鞋）、bag（包）和ankle boot（短靴）。以下函数可以将数值标签转成相应的文本标签。

```
# 本函数已保存在d2lzh包中方便以后使用
def get_fashion_mnist_labels(labels):
    text_labels = ['t-shirt', 'trouser', 'pullover', 'dress', 'coat',
                   'sandal', 'shirt', 'sneaker', 'bag', 'ankle boot']
    return [text_labels[int(i)] for i in labels]

#定义一个可以在一行里画出多张图像和对应标签的函数。
# 本函数已保存在d2lzh包中方便以后使用
def show_fashion_mnist(images, labels):
    d2l.use_svg_display()
    # 这里的_表示我们忽略（不使用）的变量
    _, figs = d2l.plt.subplots(1, len(images), figsize=(12, 12))
    for f, img, lbl in zip(figs, images, labels):
        f.imshow(img.reshape((28, 28)).asnumpy())
        f.set_title(lbl)
        f.axes.get_xaxis().set_visible(False)
        f.axes.get_yaxis().set_visible(False)
     
#通过方括号[]来访问任意一个样本，下面获取第一个样本的图像和标签。
feature, label = mnist_train[0]

#看一下训练数据集中前9个样本的图像内容和文本标签。
X, y = mnist_train[0:9]
show_fashion_mnist(X, get_fashion_mnist_labels(y))
```

读取小批量数据。通过`ToTensor`实例将图像数据从uint8格式变换成32位浮点数格式，并除以255使得所有像素的数值均在0到1之间。`ToTensor`实例还将图像通道从最后一维移到最前一维来方便之后介绍的卷积神经网络计算。通过数据集的`transform_first`函数，我们将`ToTensor`的变换应用在每个数据样本（图像和标签）的第一个元素，即图像之上。

```
batch_size = 256
transformer = gdata.vision.transforms.ToTensor()
if sys.platform.startswith('win'):#Windows暂不支持多进程
    num_workers = 0  # 0表示不用额外的进程来加速读取数据
else:
    num_workers = 4

train_iter = gdata.DataLoader(mnist_train.transform_first(transformer),
                              batch_size, shuffle=True,
                              num_workers=num_workers)
test_iter = gdata.DataLoader(mnist_test.transform_first(transformer),
                             batch_size, shuffle=False,
                             num_workers=num_workers)
                             
#查看读取一遍训练数据需要的时间
start = time.time()
for X, y in train_iter:
    continue
'%.2f sec' % (time.time() - start)
```

softmax回归的从零开始实现

```
#读取数据集
#我们将使用Fashion-MNIST数据集，并设置批量大小为256。
batch_size = 256
train_iter, test_iter = d2l.load_data_fashion_mnist(batch_size)


num_inputs = 784 #样本输入是高和宽均为28像素的图像
num_outputs = 10 #输出10个类别
W = nd.random.normal(scale=0.01, shape=(num_inputs, num_outputs))
b = nd.zeros(num_outputs)
#梯度
W.attach_grad()
b.attach_grad()
```

`NDArray`按维度操作，可以只对其中同列（`axis=0`）或同行（`axis=1`）的元素求和，并在结果中保留行和列这两个维度（`keepdims=True`）

```
X = nd.array([[1, 2, 3], [4, 5, 6]])
X.sum(axis=0, keepdims=True), X.sum(axis=1, keepdims=True)
```











参考文章：

1.[深度学习](https://zh.d2l.ai/)

