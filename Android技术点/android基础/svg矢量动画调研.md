# svg矢量动画调研

| 修改时间      | 修改人  | 修改内容 |
| --------- | ---- | ---- |
| 2020/3/31 | 郭记龙  | 首次创建 |

[TOC]

### 基本概念

SVG是指可伸缩矢量图形 (Scalable Vector Graphics)，它不同于传统的位图，不是通过存储图像中每一点的像素值来保存与使用图形，而是通过 XML 文件来定义一个图形。SVG 的方式是事先定义好怎么去画这个图，然后等要用的时候再把它去画出来

### 优劣势

SVG图的优点： 
优点一：不会失真（不管放大多大） 
优点二：节约成本（做一套图就行了） 
优点三：它可以被动态的改变颜色，一个图标多种使用。 
优点四：体积小

SVG图的缺点： 
缺点一：不兼容（5.0一下系统不兼容使用）。
缺点二：它不可以被用来做自定义RatingBar的背景（你如果用了SVG来做RatingBar的自定义样式背景，你会发现RatingBar只会显示一个星星）。 
缺点三：SVG图是不支持硬件加速的（所以它不能用来做图片的占位图和错误图）

### 实现逻辑

#### 获取一个SVG文件

要使用 SVG 图，那么首先我们肯定得有一个 SVG 文件。我们一般都有两种方式来获得一个 SVG 文件：自己写一个 SVG 文件，或者通过 AI 或一些网站作图之后导出它的 SVG 文件

##### 手写svg资源xml文件

SVG 文件里面存储的是如何去绘制目标图片的相关信息，所以理论上我们是可以从 0 开始写一个我们自己的 SVG 文件的，具体如下

在 res\drawable 目录下，新建一个xml文件
VectorDrawable也是Drawable的一个直接子类, 像其它Drawable那样通常情况下是在xml中定义, 它对应的xml标签是<vector/>, 基本结构如下:

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="200dp"
        android:height="200dp"
        android:viewportHeight="200.0"
        android:viewportWidth="200.0">
    <path
        android:pathData="M50,2 L80.813,2 L80.813,130 L50,130 L50,2 Z"
        android:strokeColor="#e33e2b"
        android:strokeWidth="6" />
</vector>
```

这样可以通过imageview直接加载该资源

##### 通过工具网址转换

将png图片通过<https://image.online-convert.com/convert-to-svg>

网址转换成svg图，但是有一定失败情况（获取到的svg展示不出来）



#### 加载本地的svg

##### 本地svg图片资源加载

​	1.通过放到drawable目录下的xml转换成对应的vectorDrawble，通过imageView加载，可以显示具体的图像

​	2.直接加载svg

##### 本地svg动画加载

​	目前可以通过pathview第三方库（186k）将现有的svg文件加载成动画，可设置播放时间和延迟播放，可监听动画过程

```
pathView.getPathAnimator()
                .delay(100)
                .duration(500)
                .listenerEnd(new PathView.AnimatorBuilder.ListenerEnd() {
                    @Override
                    public void onAnimationEnd() {
                        // 监听动画完成之后的跳转实行
                        Log.i(TAG,"动画执行结束");
                    }
                }).start();
```

#### 加载网络的svg

​	方法：加载网络svg可以只加载pathdata数据，如："M50,2 L80.813,2 L80.813,130 L50,130 L50,2 Z"，解析成对应的path对象，再根据画笔画出对应的路径即可

这里可以使用第三方库进行对应的pathdata节点解析操作：compile 'com.github.jorgecastilloprz:fillableloaders:1.01@aar'

```
//对应逻辑
List<SvgParam> datas = new ArrayList<>();
SvgParam svgParam = new SvgParam();
svgParam.setPaintWid(8);
svgParam.setColor(Color.parseColor("#00ff00"));
svgParam.setSvgPath(svgData);
datas.add(svgParam);

SvgParam svgParam2 = new SvgParam();
svgParam2.setPaintWid(12);
svgParam2.setColor(Color.parseColor("#0000ff"));
svgParam2.setSvgPath(svg2);
datas.add(svgParam2);

fillableLoader.setSvgPath(datas);
fillableLoader.start();
```

内部通过：SvgPathParser类解析节点，遍历所有节点，对关键的M,Z,V,H,L,C进行处理，添加到生成的path中，最终通过canvas.drawPath(path.getPath(), dashPaint);绘制到屏幕上

具体代码参照studydemo中的SvgImageActivity类

### 兼容问题

​	在Android 5.X之前的Android版本上，而在Android 5.X之后，Android中添加了对SVG的<path>标签支持

​	网上有很多博客存在兼容方式，单目前无设备，待确认

### 结论

针对5.0以上的手机可以考虑使用pathView库加载本地的svg动画，实现一些线条动画，性能消耗也不高

相关调研代码和部分实现逻辑已提交studyDemo项目中，可参考使用



### 参考文档

[SVG在Android中的使用5](https://www.jianshu.com/p/7ac6fcf972ad)

[svg转换xml](http://inloop.github.io/svg2android/)

[pathview项目](https://github.com/geftimov/android-pathview)

[Android SVG 兼容低版本API](https://www.jianshu.com/p/c91022c54ce6)

[FillableLoader加载svg](https://blog.csdn.net/urchin_dong/article/details/51793341)



