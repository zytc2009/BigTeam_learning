Android代码规范文档

| 时间         | 版本    | 内容   | 修改人  |
| ---------- | ----- | ---- | ---- |
| 2019/10/21 | 1.0.0 | 首次编写 | 史守峰  |

[TOC]

## 代码规范执行标准


**1.必须加注释，包括类，方法和关键语句**

**2.不要有太长的方法，如果有，按业务拆分方法**

**3.import不要使用通配符,不要换行**

**4.类成员区块划分**

 建议使用注释将源文件分为明显的区块，区块划分如下

1. 常量声明区
2. UI控件成员变量声明区
3. 普通成员变量声明区
4. 内部接口声明区
5. 初始化相关方法区
6. 事件响应方法区
7. 普通逻辑方法区
8. 重载的逻辑方法区
9. 发起异步任务方法区
10. 异步任务回调方法区
11. 生命周期回调方法区（出去onCreate()方法）
12. 内部类声明区

**5.当一个类有多个构造函数，或是多个同名方法，这些函数/方法应该按顺序出现在一起，中间不要放进其它函数/方法。**

**6. 一行一个语句**

**7.块注释风格**

块注释与其周围的代码在同一缩进级别。它们可以是/* ... */风格，也可以是// ...风格。对于多行的/* ... */注释，后续行必须从*开始， 并且与前一行的*对齐。
以下示例注释都是OK的。

```
/** This is // And so /* Or you can * okay. // is this. * even do this. */
 */

```

**8.@Override：能用则用**

**9.静态成员：使用类进行调用**

**10.不允许public成员变量**

**11.每完成一个独立的小功能就要代码提交，不要累积很多**

**12.每次提交一定要确保提交的代码是完整的，可编译运行的**

**13.代码提交前要用指定的模板格式化，提交的注释一定要写清楚本次提交完成的任务，要确保研发和测试都能看明白，如果是解决bug，要写bug描述，不要写bug号**

**14.尽量使用卫语句**

**15.不用的代码要删除**

**16.第三方依赖要写到具体模块，不要都写到最底层模块中**

**17.常量字符串最好按业务分组管理，如报数的事件，请求的接口地址**

**18.如果方法中确实需要throw异常，方法必须有异常声明，throw的异常需要时自定义的**



## 命名规范：

### 1. 包名

包名全部小写，连续的单词只是简单地连接起来，不使用下划线。
采用反域名命名规则，全部使用小写字母。一级包名为com，二级包名为xx（可以是公司或则个人的随便），三级包名根据应用进行命名，四级包名为模块名或层级名。
例如：com.szy.kitchen

| 包名                                       | 此包中包含                              |
| ---------------------------------------- | ---------------------------------- |
| com.xx.应用名称缩写.activity                   | 页面用到的Activity类 (activitie层级名用户界面层) |
| com.xx.应用名称缩写.base                       | 基础共享的类                             |
| com.xx.应用名称缩写.adapter                    | 页面用到的Adapter类 (适配器的类)              |
| com.xx.应用名称缩写.util                       | 此包中包含：公共工具方法类（util模块名）             |
| com.xx.应用名称缩写.bean                       | 下面可分：vo、po、dto 此包中包含：JavaBean类     |
| com.xx.应用名称缩写.model                      | 此包中包含：模型类                          |
| com.xx.应用名称缩写.db                         | 数据库操作类                             |
| com.xx.应用名称缩写.view (或者 com.xx.应用名称缩写.widget ) | 自定义的View类等                         |
| com.xx.应用名称缩写.service                    | Service服务                          |
| com.xx.应用名称缩写.receiver                   | BroadcastReceiver服务                |

> 注意：
> 如果项目采用MVP，所有M、V、P抽取出来的接口都放置在相应模块的i包下，所有的实现都放置在相应模块的impl下

### 2. 类名

类名都以UpperCamelCase风格编写。
类名通常是名词或名词短语，接口名称有时可能是形容词或形容词短语。现在还没有特定的规则或行之有效的约定来命名注解类型。
名词，采用大驼峰命名法，尽量避免缩写，除非该缩写是众所周知的， 比如HTML,URL，如果类名称中包含单词缩写，则单词缩写的每个字母均应大写。

| 类               | 描述                                       | 例如                                       |
| --------------- | ---------------------------------------- | ---------------------------------------- |
| Activity 类      | Activity为后缀标识                            | 欢迎页面类WelcomeActivity                     |
| Adapter类        | Adapter 为后缀标识                            | 新闻详情适配器 NewDetailAdapter                 |
| 解析类             | Parser为后缀标识                              | 首页解析类HomePosterParser                    |
| 工具方法类           | Util或Manager为后缀标识（与系统或第三方的Utils区分）或功能+Util | 线程池管理类：ThreadPoolManager <p> 日志工具类：LogUtil（Logger也可）<p> 打印工具类：PrinterUtil |
| 数据库类            | 以DBHelper后缀标识                            | 新闻数据库：NewDBHelper                        |
| Service类        | 以Service为后缀标识                            | 时间服务TimeServiceBroadcast                 |
| Receiver类       | 以Receiver为后缀标识                           | 推送接收JPushReceiver                        |
| ContentProvider | 以Provider为后缀标识                           |                                          |
| 自定义的共享基础类       | 以Base开头                                  | BaseActivity,BaseFragment                |

测试类的命名以它要测试的类的名称开始，以Test结束。
例如：HashTest 或 HashIntegrationTest。
接口（interface）：命名规则与类一样采用大驼峰命名法，多以able或ible结尾，如
interface Runnable ;
interface Accessible。

> 注意：
> 如果项目采用MVP，所有Model、View、Presenter的接口都以I为前缀，不加后缀，其他的接口采用上述命名规则。

### 3 方法名

方法名都以 LowerCamelCase 风格编写。
方法名通常是动词或动词短语。

| 方法                   | 说明                                   |
| -------------------- | ------------------------------------ |
| initXX()             | 初始化相关方法,使用init为前缀标识，如初始化布局initView() |
| isXX() checkXX()     | 方法返回值为boolean型的请使用is或check为前缀标识      |
| getXX()              | 返回某个值的方法，使用get为前缀标识                  |
| handleXX()           | 对数据进行处理的方法，尽量使用handle为前缀标识           |
| displayXX()/showXX() | 弹出提示框和提示信息，使用display/show为前缀标识       |
| saveXX()             | 与保存数据相关的，使用save为前缀标识                 |
| resetXX()            | 对数据重组的，使用reset前缀标识                   |
| clearXX()            | 清除数据相关的                              |
| removeXXX()          | 清除数据相关的                              |
| drawXXX()            | 绘制数据或效果相关的，使用draw前缀标识                |

下划线可能出现在JUnit测试方法名称中用以分隔名称的逻辑组件。一个典型的模式是：test_，例如testPop_emptyStack。
并不存在唯一正确的方式来命名测试方法。

### 4. 常量名

常量名命名模式为CONSTANT_CASE，全部字母大写，用下划线分隔单词。那，到底什么算是一个常量？
每个常量都是一个静态final字段，但不是所有静态final字段都是常量。在决定一个字段是否是一个常量时，考虑它是否真的感觉像是一个常量。
例如，如果任何一个该实例的观测状态是可变的，则它几乎肯定不会是一个常量。只是永远不打算改变对象一般是不够的，它要真的一直不变才能将它示为常量。

```
// Constants
static final int NUMBER = 5;
static final ImmutableListNAMES = ImmutableList.of("Ed", "Ann");
static final Joiner COMMA_JOINER = Joiner.on(','); // because Joiner is immutable
static final SomeMutableType[] EMPTY_ARRAY = {};
enum SomeEnum { ENUM_CONSTANT }
// Not constants
static String nonFinal = "non-final";
final String nonStatic = "non-static";
static final SetmutableCollection = new HashSet();
static final ImmutableSetmutableElements = ImmutableSet.of(mutable);
static final Logger logger = Logger.getLogger(MyClass.getName());
static final String[] nonEmptyArray = {"these", "can", "change"};

```

这些名字通常是名词或名词短语。


### 6. 资源文件命名规范

##### 1). 资源布局文件（XML文件（layout布局文件））： 全部小写，采用下划线命名法

1. contentview 命名
   必须以全部单词小写，单词间以下划线分割，使用名词或名词词组。
   所有Activity或Fragment的contentView必须与其类名对应，对应规则为：
   将所有字母都转为小写，将类型和功能调换（也就是后缀变前缀）。
   例如：`activity_main.xml`
2. Dialog命名：`dialog_描述.xml`
   例如：`dialog_hint.xml`
3. PopupWindow命名：`ppw_描述.xml`
   例如：`ppw_info.xml`
4. 列表项命名：`item_描述.xml`
   例如：`item_city.xml`
5. 包含项命名：`模块_(位置)描述.xml`
   例如：`activity_main_head.xml`、`activity_main_bottom.xml`
   注意：通用的包含项命名采用：`项目名称缩写_描述.xml`
   例如：`xxxx_title.xml`

##### 2). 资源文件（图片drawable文件夹下）：

全部小写，采用下划线命名法，加前缀区分
命名模式：可加后缀 `_small` 表示小图, `_big` 表示大图，逻辑名称可由多个单词加下划线组成，采用以下规则：
`用途_模块名_逻辑名称`
`用途_模块名_颜色`
`用途_逻辑名称`
`用途_颜色`
说明：用途也指控件类型（具体见UI控件缩写表）
例如：
`btn_main_home.png`按键
`divider_maket_white.png` 分割线
`ic_edit.png` 图标
`bg_main.png` 背景
`btn_red.png` 红色按键
`btn_red_big.png` 红色大按键
`ic_head_small.png` 小头像
`bg_input.png`输入框背景
`divider_white.png`白色分割线
如果有多种形态如按钮等除外如 `btn_xx.xml`（selector）

| 名称                     | 功能                           |
| ---------------------- | ---------------------------- |
| `btn_xx`               | 按钮图片使用`btn_整体效果`（selector）   |
| `btn_xx_normal`        | 按钮图片使用`btn_正常情况效果`           |
| `btn_xx_pressed`       | 按钮图片使用`btn_点击时候效果`           |
| `btn_xx_focused`       | `state_focused`聚焦效果          |
| `btn_xx_disabled`      | `state_enabled` (false)不可用效果 |
| `btn_xx_checked`       | `state_checked`选中效果          |
| `btn_xx_selected`      | `state_selected`选中效果         |
| `btn_xx_hovered`       | `state_hovered`悬停效果          |
| `btn_xx_checkable`     | `state_checkable`可选效果        |
| `btn_xx_activated`     | `state_activated`激活的         |
| `btn_xx_windowfocused` | `state_window_focused`       |
| `bg_head`              | 背景图片使用`bg_功能_说明`             |
| `def_search_cell`      | 默认图片使用`def_功能_说明`            |
| `ic_more_help`         | 图标图片使用`ic_功能_说明`             |
| `seg_list_line`        | 具有分隔特征的图片使用`seg_功能_说明`       |
| `sel_ok`               | 选择图标使用`sel_功能_说明`            |

##### 3). values中name命名

| 类别      | 命名                                       | 示例                                       |
| ------- | ---------------------------------------- | ---------------------------------------- |
| strings | strings的name命名使用下划线命名法，采用以下规则：<p>模块名+逻辑名称 | main_menu_about 主菜单按键文字<p>friend_title好友模块标题栏<p>friend_dialog_del好友删除提示<p>login_check_email登录验证<p>dialog_title 弹出框标题<p>button_ok确认键 loading加载文字<p> |
| colors  | colors的name命名使用下划线命名法，采用以下规则：<p>模块名+逻辑名称 颜色 | friend_info_bgfriend_bg transparent gray |
| styles  | styles的name命名使用Camel命名法，采用以下规则：模块名+逻辑名称  | main_tabBottom                           |

##### 4). layout中的id命名

控件类型缩写 + 下划线 + 控件逻辑名称，比如登录按钮 btn_login.

控件的缩写:

| 控件           | 缩写   |
| ------------ | ---- |
| LayoutView   | lv   |
| RelativeView | rv   |
| TextView     | tv   |
| Button       | btn  |
| ImageButton  | img  |
| ImageView    | iv   |
| CheckBox     | chk  |
| RadioButton  | rb   |
| DatePicker   | dp   |
| EditText     | et   |
| TimePicker   | tp   |
| ToggleButton | tb   |
| ProgressBar  | pb   |
| WdbView      | wv   |
| RantingBar   | rb   |
| Tab          | tab  |
| ListView     | lv   |
| MapView      | mv   |


## 阿里巴巴Android开发规范

阿里巴巴Android开发规范 是业界比较推崇的开发规范，对android开发进行了很好的指导、和建议规范作用。所以我们建议开发者可以阅读一下阿里巴巴android开发规范，能帮助我们避免因代码不规范引起的隐患等问题。

文档见附件！


## 附录

推荐一本关于代码规范的书籍能帮我们改善我们的代码

《代码整洁之道》 见附件