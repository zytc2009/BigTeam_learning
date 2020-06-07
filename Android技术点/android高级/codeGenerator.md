## CodeGenerator代码生成器

前言
成为一名优秀的Android开发，需要不断的学习，不断的进步，得吃着碗里的，看着锅里的，还得找菜篮子，没有什么是一成不变的，要保持敏锐的嗅觉，不断的反思自己，认知自己。多和圈子里外的人交流，避免闭门造车，避免工作几年之后，才发现用的技术淘汰了。在这里，让我们一起成长为更优秀的自己~。

代码生成器能极大的减少重复的工作，常见的代码生成器有很多，大家可以根据自己的需要安装，有GsonFormat（根据json串自动生成数据类），Parcelable Code Generator（生成Parcelable相关代码）等

安装插件过程：

1、打开Android Studio，点击左上角 File -->Settings， 选择插件Plugins , 搜索插件名字 ，安装即可，也可选择手动下载插件jar，从本地安装

2、安装完，重启就能使用了，验证的话，可以用快捷键，ALT+Insert，就能看到菜单项

今天我们要实现为数据类，生成一个方法，主要是实现自解析，功能很简单，主要是配合我的工作需要，大家可以一起熟悉一下流程


```
public class UserData extends BaseData {
    @BindData("name")
    protected String name;

    @BindData("age")
    protected int age;
    ... 省略 set/get

    //希望生成的代码
    @Override
    public UserData parseData(JSONObject data) {
       userName =data.optString("name");
       age =data.optInt("age");
    }
}
```
