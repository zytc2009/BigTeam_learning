[TOC]
# 什么路由
什么是路由
路由的概念广泛用于计算机网络中，指路由器从一个接口收到数据包，根据数据路由包的目的地址进行定向并转发到另一个接口的工程。路由器用于连接多个逻辑分开的网络。

我们需要将各个module 看作一个个不同的网络，而Router（路由器）就是连接各个模块中页面跳转的中转站。这个中转站可以拦截不安全的跳转或者设定一些特定的拦截服务。

原生跳转方式有很多局限性。这里借用了ARouter对跳转分析的一张图，这张图可以很容易的反映出原生跳转和路由跳转的差异。


# 实现组件跳转方式
## 传统跳转几种方式
- 第一种，通过intent跳转
- 第二种，通过aidl跳转
- 第三种，通过scheme协议跳转

## 为何需要路由
- 显示Intent：项目庞大以后，类依赖耦合太大，不适合组件化拆分
- 隐式Intent：协作困难，调用时候不知道调什么参数
- 每个注册了Scheme的Activity都可以直接打开，有安全风险
- AndroidMainfest集中式管理比较臃肿
- 无法动态修改路由，如果页面出错，无法动态降级
- 无法动态拦截跳转，譬如未登录的情况下，打开登录页面，登录成功后接着打开刚才想打开的页面
  

# ARouter 配置和优势
## 配置
- [ARouter具体使用](https://github.com/alibaba/ARouter)

## 优势
- 支持直接解析标准URL进行跳转，并自动注入参数到目标页面中
- 支持多模块工程使用
- 支持添加多个拦截器，自定义拦截顺序
- 支持依赖注入，可单独作为依赖注入框架使用
- 支持InstantRun
- 支持MultiDex(Google方案)
- 映射关系按组分类、多级管理，按需初始化
- 支持用户指定全局降级与局部降级策略
- 页面、拦截器、服务等组件均自动注册到框架
-支持多种方式配置转场动画
- 支持获取Fragment
-哦 完全支持Kotlin以及混编

## 典型的应用：
- 从外部URL映射到内部页面，以及参数传递与解析
- 跨模块页面跳转，模块间解耦
- 拦截跳转过程，处理登陆、埋点等逻辑
- 跨模块API调用，通过控制反转来做组件解耦

更多具体详见ARouter GitHub 官网介绍。


## ARouter的结构
- ARouter主要由三部分组成，包括对外提供的api调用模块、注解模块以及编译时通过注解生产相关的类模块。
    - arouter-annotation注解的声明和信息存储类的模块
    - arouter-compiler编译期解析注解信息并生成相应类以便进行注入的模块
    - arouter-api核心调用Api功能的模块
- annotation 模块
    - Autowired,Interceptor,Param,Route 都是开发过程中需要使用的注解
- compiler 
    - AutowiredProcessor，InterceptorProcessor,RouteProcessor。分别对应 annotation 包中Autowired,Interceptor,Param,Route 在项目中编译产生相关的类文件。
- api 模块
    - 主要是ARouter具体实现和对外暴露使用的api。



# ARouter 工作流程

▍ARouter 基础原理

- 在代码里加入的@Route注解，会在编译时期通过apt生成一些存储path和activityClass映射关系的类文件，然后app进程启动的时候会拿到这些类文件，把保存这些映射关系的数据读到内存里(保存在map里)，然后在进行路由跳转的时候，通过build()方法传入要到达页面的路由地址。
    
- 添加@Route注解然后编译一下，就可以生成这个类。
    
- ARouter会通过它自己存储的路由表找到路由地址对应的Activity.class(activity.class = map.get(path))，然后new Intent()，当调用ARouter的withString()方法它的内部会调用intent.putExtra(String name, String value)，调用navigation()方法，它的内部会调用startActivity(intent)进行跳转，这样便可以实现两个相互没有依赖的module顺利的启动对方的Activity了。


## 初始化流程
- 初始化代码如下所示

```
public static void init(Application application) {
    if (!hasInit) {
        logger = _ARouter.logger;
        _ARouter.logger.info(Consts.TAG, "ARouter init start.");
        hasInit = _ARouter.init(application);

        if (hasInit) {
            _ARouter.afterInit();
        }

        _ARouter.logger.info(Consts.TAG, "ARouter init over.");
    }
}
```
- 然后在看_ARouter.init(application) 代码如下所示

```
protected static synchronized boolean init(Application application) {
    mContext = application;
    LogisticsCenter.init(mContext, executor);
    logger.info(Consts.TAG, "ARouter init success!");
    hasInit = true;
    mHandler = new Handler(Looper.getMainLooper());

    return true;
}

```

- 综上所述，整个初始化的流程大概就是：
    - 初始化运行时的上下文环境
    - 初始化日志logger
    - 寻找router相关的类
    - 解析并且缓存路由相关信息
    - 初始化拦截服务

##  跳转页面流程
![image](https://note.youdao.com/yws/public/resource/215b4d3e8565a75e27e38c334dab4c42/xmlnote/WEBRESOURCEea212e1170ae7a99d8e4d0dc70ebf19d/853)

## 如何获取Fragment 实例

模块之间的通讯通过Arouter来实现。

例如，main模块中要获取到message模块中的一个Fragment类，但两个模块又不能直接依赖。通过Arouter的暴露服务来通讯可以解决。

先在CommonLibraray中定义一个IProvider子类，

- 定义一个服务.

```
public interface MessageListProvider extends IProvider {
    Fragment createFragment();
}
```
- 定义该服务路径

```
public final class RouterPath {
    /**
     * 消息列表的路径
     */
   public static  final  String PATH_MESSAGE_LIST="/MessageService/message_list";
}
```
- 然后在Message组件中，实现该服务

```
@Route(path = RouterPath.PATH_MESSAGE_LIST)
public class MessageListProviderImpl implements MovieListProvider {
    @Override
    public Fragment createFragment() {
        MessageListFragment fragment=MessageListFragment.newInstance();
        MessageListPresenter presenter=new MessageListPresenter(fragment);
        return fragment;
    }
    @Override
    public void init(Context context) {

    }
}
```
