### Hive

1.简化了我们的mapreduce编程

2.主要做离线数据分析，非OLAP和非OLTP系统，不支持迭代

3.主要组成： 

​	用户接口：包括CLI，JDBC/ODBC，WebUI 

​	metastore： 默认存储在自带的数据库derby中，线上使用时一般换为MySQL 

​	Driver：解释器、编译器、优化器、执行器 

​	Hadoop：用MapReduce进行计算，用HDFS存储

4.数据模型： 

​	Databases，Tables，Partitions(可选)，Files 

5.Hive查询语言：HQL 为大数据场景定制的查询语句，基于SQL实现，相似度极高，大多情况可以把HQL当成SQL

6.创建表： 

```
	CREATE  [TEMPORARY]  [EXTERNAL]  TABLE  [IF  NOT  EXISTS]   [db_name.]  table_name [(col_name data_type [COMMENT   col_comment],  ...)] [COMMENT  table_comment]   [ROW  FORMAT  row_format] [STORED  AS  file_format] 
```

 row_format，行数据格式，比如行之间的分隔符之间的分隔符 如：

```
ROW FORMAT DELIMITED FIELDS TERMINATED BY ‘\t’ LINES TERMINATED BY‘\n’ STORED AS TEXTFILE;
```

 file_format，数据格式，可选的值：

​	 • TEXTFILE 

​	 • SEQUENCEFILE

​	 • ORC (后面讲) 

​	 • PARQUET（后面讲） 

​	 • 自定义格式