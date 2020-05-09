## GreenDao扩展和在项目中的使用##

自动生成的三个类

```
DaoMaster，DaoSession，XXXDao
```

## 三个类的具体功能##

DaoMaster :  里面的openhelper 用来创建表和更新表的,,并且注册所有的Dao

DaoSession:  通过map集合拿到相应的DaoConfig， 得到全部的Dao对象

XXXDao: 用来操作具体的表的

缺点  在DaoMaster中帮我们注册了所有的Dao,我们希望的是使用哪一个，注册哪一个

​         创建表 我们希望自己来做，如果有多个库多张表我们自己创建将会更清晰。

## 封装的Base类##

BaseDBHeler :中createAllTables方法创建表 ,onUpgrade更新表

CommonDaoMaster:主要功能是通过getConfig方法拿到DaoConfig的对象并注册相应的Dao



## 具体的使用##

AddressDBOpenHelper 继承自BaseDBHeler ：   重写BaseDBHeler中createAllTables方法创建表 ,onUpgrade更新表



AddressDBManager:用单利 获取对象，在构造中创建AddressDBOpenHelper 对象，创建相应的Dao对象，操作表的方法。



优点：自己好控制，比较灵活![GreenDao使用](http://oyy3c9bht.bkt.clouddn.com/blog/180329/mD3aEjGI2e.png?imageslim)

