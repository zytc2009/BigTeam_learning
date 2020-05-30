### HBase环境搭建

1.构建在HDFS上的分布式列存储系统

2.源自于Google Bigtable的开源实现

3.将数据按照表、行和列进存储，它是一个分布式的、稀疏的、持久化存储的多维度排序表

4.相关比较：

​	HBase VS HDFS 

   	 HDFS适合批处理场景 

   	 HDFS不支持数据随机查找 

​		HDFS不支持数据更新

​	HBase VS Hive 

​		Hive适合批处理数据分析场景 

​		Hive不适合实时的数据访问

5.HBase特点： 

​	良好的扩展性 

​	读和写的强一致性 

​	高可靠性 

​	与MapReduce良好的集成

6.测试

```
//创建表，有两个列族：‘c1’和‘c2’ 
create  'table1','c1','c2'  

//写入数据  
put  'table1','row1','c1','value',ts1  

//随机查找
get  'table1',  'row1'
get 'table1','row1','c1'
get 'table1','row1','c1','c2'
get 'table1','row1',{COLUMN=>'c1',TIMESTAMP=>ts1}
get 'table1','row1',{COLUMN=>'c1',TIMERANGE=>[ts1,ts2]}  

//范围查找数据
scan 'table1'
scan 'table1',{COLUMNS=>'c1:q1'}
scan 'table1',{COLUMNS=>['c1','c2'],LIMIT=>10,STARTROW=>'xyz'}

//删除数据
delete  'table1','r1','c1',ts1  

//删除全表数据
truncate  'table1'
```

