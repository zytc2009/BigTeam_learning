自定义APT

**APT**(Annotation Processing Tool)即**注解处理器**，是一种处理注解的工具，确切的说它是javac的一个工具，它用来在**编译时**扫描和处理注解。注解处理器以**Java代码**(或者编译过的字节码)作为输入，生成**.java文件**作为输出。

简单来说就是在编译期，通过注解生成**.java**文件。

使用**APT**的优点就是方便、简单，可以少些很多重复的代码。

用过**ButterKnife**、**Dagger**、**EventBus**等注解框架的同学就能感受到，利用这些框架可以少些很多代码，只要写一些注解就可以了。其实，他们不过是通过注解，生成了一些代码。通过对**APT**的学习，你就会发现他们的秘密~

好的，进入正题，我们开始我们的实验，

我在用json解析数据的时候，发现接口返回的数据层次太多，解析太慢，接口暂时不改，只能客户端想办法，我对比了目前常用的Json解析，主要以下三种：

- Gson
- JackJson
- FastJson

我做了大量测试，发现还是达不到我的要求，发现解析太慢了，我们知道json解析主要是通过反射拿到数据类型和成员，而java反射太慢了，于是只能想办法，能不能自己解析，对比发现自己解析确实要快很多，但是对于开发来讲，写大量的解析代码，太浪费时间了，于是就想能不能通过APT实现。

首先看一个简答的数据结构，

```
public class Teacher extends BaseData {
    private String name;
    private int age;
    ...省略 set/get
    
    //数据解析
    void bindData(JSONObject data) {
    	name =data.optString("name");
    	age = (int) data.opt("age");
   }
}
```

我们期望生成bindData方法，替我们完成数据解析，为了约束类的使用，我统一在BaseData增加抽象方法声明。

然后就是APT的处理代码了，

新建工程，在工程中创建两个 **java** 的module，一个annotation的用于声明注解，就是存放类似于@Override的这些东西。另一个compiler用于处理声明的注解。

### 1.声明注解类

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // 目标
public @interface BindData {
    String value();
}
```

这里有几点需要注意：

@interface 注解类需要用这个来标识
@Target(ElementType.FIELD) 这个表示注解作用在变量上，还有其他的类型这里我们都先跳过
@Retention(RetentionPolicy.CLASS)    

RetentionPolicy的取值按生命周期来划分可分为3类：

​	RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
​	RetentionPolicy.CLASS：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
​	RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；



通过自定义注解，添加到我们的数据类里，如下所示：

```
public class Teacher extends BaseData {
    @BindData("name")
    protected String name;

    @BindData("age")
    protected int age;
}
```



### 2.注解处理器

看compiler模块，APT的运行需要在module中添加依赖：

```
dependencies {
    implementation 'com.google.auto.service:auto-service:1.0-rc2' 
    implementation project(':apt-annotation')
}
```

然后我们看注解处理器代码：

![apt_processor](..\images\apt_processor.png)

 这个类主要有4个方法：

`init`：初始化。可以得到`ProcessingEnviroment`，`ProcessingEnviroment`提供很多有用的工具类`Elements`, `Types` 和 `Filer`

`getSupportedAnnotationTypes`：指定这个注解处理器是注册给哪个注解的，这里说明是注解`BindData`

`getSupportedSourceVersion`：指定使用的Java版本，通常这里返回`SourceVersion.latestSupported()`

`process`：可以在这里写扫描、分析和处理注解的代码，生成Java文件（**process中的代码下面详细说明**）

 然后说说process的处理，这个是核心，对我们上面的需求来说，我们主要是要生成一个代码解析类，里面根据类成员，生成解析处理方法。

主要的流程如下：

  1）获取包名和类名

```
String packageName = entry.getKey().split("_")[0];
String typeName = entry.getKey().split("_")[1];
ClassName className = ClassName.get(packageName, typeName);
```

  2）构建解析类	

```
ClassName generatedClassName = ClassName.get(packageName, typeName + "ParseHelper");
System.out.println("Processor " +generatedClassName );
TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName).addModifiers(Modifier.PUBLIC).addAnnotation(Keep.class);
```

  3）构建解析方法  

```
MethodSpec.Builder bindViewsMethodBuilder = MethodSpec.methodBuilder(NameStore.Method.BIND_DATA)//方法名
.addModifiers(Modifier.PUBLIC) //公有属性
.addModifiers(Modifier.STATIC) //静态方法
.returns(void.class) //无返回值
.addParameter(jsonObjectClassName, NameStore.Variable.ANDROID_DATA)//增加函数参数     .addParameter(className, NameStore.Variable.ANDROID_BEAN); //增加函数参数，类型+参数名 
```

  4）添加解析代码

​      这个代码较多，大家直接看github上的代码就行

  5）方法添加到解析类

​     classBuilder.addMethod(bindViewsMethodBuilder.build());

   6）生成java文件     

```
  JavaFile.builder(packageName, classBuilder.build())
  .build()
  .writeTo(filer);
```

   编译，生成TeacherParseHelper类：     

```
public class TeacherParseHelper {
  public static void bindData(JSONObject data, Teacher bean) {
    bean.name = (java.lang.String) data.optString("name");
    bean.age = (int)data.optInt("age");
  }
}
```

### 3.使用

   由于apt都是生成的独立文件，我改造了一下我的数据类

```
public class Teacher extends BaseData {
    @BindData("name")
    protected String name;

    @BindData("age")
    protected int age;

    @Override
    public Teacher parseData(JSONObject data) {
        //调用apt生成的解析方法
        TeacherParseHelper.bindData(data, this);
        return this;
    }
 }
```

 然后，我在我的页面做个测试：   

```
 public void dataParseTest(View view) {
        String result = DataParserUtil.readAssetsFileData(this, "json.txt");
        //解析测试
        Log.d("Parse","dataParseTest() system start");
        //系统的解析
        for(int i=0;i<10000;i++){
            try {
                JSONObject object = new JSONObject(result);
                GradeData gradeData = new GradeData().parseData(object);
                if(i==5000) {
                    Log.d("Parse", "dataParseTest() system end" + gradeData.getUserData());
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        Log.d("Parse","dataParseTest() system end");

        for(int i=0;i<10000;i++){
            GradeData gradeData = DataParserUtil.parseObject(result, GradeData.class);
        }
        Log.d("Parse","dataParseTest() fast end");
    }
```

运行结果：

```
2020-06-07 20:05:57.107 22233-22233/com.szy.lesson_aop D/Parse: dataParseTest() system start
2020-06-07 20:05:57.240 22233-22233/com.szy.lesson_aop D/Parse: dataParseTest() system end
2020-06-07 20:05:57.431 22233-22233/com.szy.lesson_aop D/Parse: dataParseTest() fast end
```

证实我们自己的解析确实要快一些，[下载代码](https://github.com/zytc2009/Demo_Aop)

当然也可以有其他的方法实现，比如自定义CodeGenerator生成，ASM也可以做到，你们也许还有更好的方法，欢迎留言！

好了，如果你们还有什么疑问，可也可以加入我们一起学习,这是我们在整理的学习资料，大家可以关注 https://github.com/zytc2009/BigTeam_learning

