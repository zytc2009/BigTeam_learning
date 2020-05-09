# GreenDao的使用#

 ### 一.GreenDao的使用###

Greendao是一款用于数据库创建与管理的框架，  由于原生SQLite语言比较复杂繁琐，学习SQLite原生语言学习成本高，效率低下，所以不少公司致力于开发一款简单的数据库管理框架，较为著名的就有Greendao和ORMLite，但是就数据分析来看，Greendao的效率是高于ORMLite及其他框架的，是目前该行业的领先者。也因为Greendao的使用方法简便，且效率高使得其成为目前使用最为广泛的数据库管理框架。

 greenDAO是一个对象关系映射（ORM）的框架，能够提供一个接口通过操作对象的方式去操作关系型数据库，它能够让你操作数据库时更简单、更方便。如下图所示：

![greendao](https://images2015.cnblogs.com/blog/950883/201607/950883-20160707205004702-1618194894.png)

## 二.GreenDao的优点##

1、一个精简的库
2、性能最大化
3、内存开销最小化
4、易于使用的 APIs
5、对 Android 进行高度优化

6、支持数据库加密

## 三.GreenDao的使用##

**（一）对库的管理**

 在Module的build.gradle中

    android{
      ....
        greendao{
        schemaVersion 1//数据库版本号    
        daoPackage 'greendaos'
        targetGenDir 'src/main/java'
      }
    }
    
    dependencies {
        compile 'org.greenrobot:greendao:3.2.2' // add library
    }
**（二）对表的管理**

在数据结构中使用注解@Entity自动生成与数据结构相对应的表

**（三）数据结构与表的对应关系**

**@Entity**  

表明这个实体类会在数据库中生成一个与之相对应的表

```
@Entity
public class SourceInfo {
    @Id(autoincrement = true)//@Id 主键 Long 型，可以通过@Id(autoincrement = true)设置自增长
    private Long _id;
    @NotNull    //@NotNull：设置数据库表当前列不能为空
    @Property(nameInDb = "URL")//设置一个非默认关系映射所对应的列名，默认是使用字段名
    private  String url;
    @Transient           //@Transient：添加此标记后不会生成数据库表的列
    private  int length;
    @Property(nameInDb = "MIME")
    private String mime;
    
    **不用加get和set方法**编译会自动生成***
}
```

相关的注解说明：

- [实体@Entity注解](mailto:%E5%AE%9E%E4%BD%93@entity%E6%B3%A8%E8%A7%A3)


- > schema：告知GreenDao当前实体属于哪个schema
  > active：标记一个实体处于活跃状态，活动实体有更新、删除和刷新方法
  > nameInDb：在数据库中使用的别名，默认使用的是实体的类名
  > indexes：定义索引，可以跨越多个列
  > createInDb：标记创建数据库表

- 基础属性注解

  > @Id：主键 Long 型，[可以通过@Id](mailto:%E5%8F%AF%E4%BB%A5%E9%80%9A%E8%BF%87@id)(autoincrement = true)设置自增长
  > @Property：设置一个非默认关系映射所对应的列名，默认是使用字段名，例如：@Property(nameInDb = "name")
  > @NotNull：设置数据库表当前列不能为空
  > @Transient：添加此标记后不会生成数据库表的列

- 索引注解

  > @Index：[使用@Index作为一个属性来创建一个索引](mailto:%E4%BD%BF%E7%94%A8@index%E4%BD%9C%E4%B8%BA%E4%B8%80%E4%B8%AA%E5%B1%9E%E6%80%A7%E6%9D%A5%E5%88%9B%E5%BB%BA%E4%B8%80%E4%B8%AA%E7%B4%A2%E5%BC%95)，通过name设置索引别名，也可以通过unique给索引添加约束
  > @Unique：向数据库添加了一个唯一的约束

- 关系注解

  > @ToOne：定义与另一个实体（一个实体对象）的关系
  > @ToMany：定义与多个实体对象的关系

**（四）GreenDao的使用（增删改查方法）**

1.GreenDao的配置

（1）在project的build.gradle中的配置

```
dependencies {
   classpath 'org.greenrobot:greendao-gradle-plugin:3.2.2' // add plugin    
}
```

（2）在要使用greenDao的Module的build.gradle中的配置

```
apply plugin: 'org.greenrobot.greendao' // apply plugin


 greendao{
        schemaVersion 1//数据库版本号    
        daoPackage 'com.com.sky.downloader.greendao'//设置DaoMaster、DaoSession、Dao包名    
        targetGenDir 'src/main/java'//设置DaoMaster、DaoSession、Dao目录   
        //targetGenDirTest：设置生成单元测试目录    
       //generateTests：设置自动生成单元测试用例
        
    }
    
  dependencies {
    compile 'org.greenrobot:greendao:3.2.2' // add library
 
    // This is only needed if you want to use encrypted databases
    compile 'net.zetetic:android-database-sqlcipher:3.5.6'//加密库依赖（可选项）
}
```

### 简单的增删改查实现：

### 增

注意：Long型id，如果传入null，则GreenDao会默认设置自增长的值。

- insert(User entity)：插入一条记录

  .insertInTx(author1,author2)//保存多个实例

  ![add user.png](https://upload-images.jianshu.io/upload_images/644248-6cb46f6e91f75555.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 删

- deleteBykey(Long key) ：根据主键删除一条记录。
- delete(User entity) ：根据实体类删除一条记录，一般结合查询方法，查询出一条记录之后删除。
- deleteAll()： 删除所有记录。
  ![delete.png](https://upload-images.jianshu.io/upload_images/644248-2db8d7c5f1af11da.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 改

- update(User entity)：更新一条记录

  ![update.png](https://upload-images.jianshu.io/upload_images/644248-fbed1271e5678599.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



/**  *修改或者替换（有的话就修改，没有则替换）     */   

​    public void insertOrReplace(long i,String date)   {  

​      mDayStep = new dayStep((long) i,date,0);  

​      dao.insertOrReplace(mDayStep);  

​    }  

### 查

- loadAll()：查询所有记录
- load(Long key)：根据主键查询一条记录
- queryBuilder().list()：返回：List
- queryBuilder().where(UserDao.Properties.Name.eq("")).list()：返回：List
- queryRaw(String where,String selectionArg)：返回：List

查询记录

```
public List query(){   
	return userDao.loadAll();// 查询所有记录
}
```

```
public User query2(){        
	return userDao.loadByRowId(1);//根据ID查询
}
public List query2(){        
	return userDao.queryRaw("where AGE>?","10");//查询年龄大于10的用户
}
//查询年龄大于10的用户
public List query4(){    
	QueryBuilder builder = userDao.queryBuilder();    
	return  builder.where(UserDao.Properties.Age.gt(10)).build().list();
}
```

/** 

​     *查找符合某一字段的所有元素 

​    */  

   public void searchEveryWhere(String str)    {  

<span style="white-space:pre;"> </span>List<dayStep> mList = dao.queryBuilder()  

​              .where(dao.date.eq(str)).build().listLazy();  

   }  

**（五）数据库的升级**

在openHelper中重写onUpgrade方法

```
public class DBHelper extends DaoMaster.OpenHelper {
    public static final String DBNAME = "lenve.db";

    public DBHelper(Context context) {
        super(context, DBNAME, null);
    }
    
   @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }    
    
}
```

**（六）多个模块公用数据库**

首先声明，不建议这样使用，如果特殊情况下需要使用，需要注意：

  1.底层模块无法帮你创建数据表，需要你自己实现
  2.底层模块需要你注册自己的XXXDao类







