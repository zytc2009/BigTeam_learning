### MapReduce

1.源自于Google的MapReduce论文 

2.MapReduce

   优点： 易于编程 良好的扩展性 高容错性 适合PB级以上海量数据的离线处理 

   不擅长： 实时计算 流式计算 DAG计算

3.分为Map和Reduce两部分 

​	Map阶段包含InputFormat(Read)，Mapper，Combiner，Partitioner（数据分组，不是必须）三部分 

​	Reduce阶段包含Shuffle(不是必须，但是大多需要)，Reducer，OutPutFormat(Write)

4.Split与Block 

​	Block 

​	  1)HDFS中最小的数据存储单位 

​	  2)默认是128MB 

   Split 

​     1)MapReduce中最小的计算单元 

​     2)默认与Block一一对应 

   Split与Block的对应关系是任意的，由用户控制

5.Combiner 

​	可做看local reducer 

​	合并相同的key对应的value（wordcount例子） 

​	通常与Reducer逻辑一样  

​	可以减少Map Task输出数据量（磁盘IO）和减少Reduce-Map网络传输数据量(网络IO) 

​	对于结果可叠加的才能使用，不能使用求平均值等场景

6.数据本地性（data locality） 

  如果任务运行在它将处理的数据所在的节点，则称该任务具有“数据本地性” 

  本地性可避免跨节点或机架数据传输，提高运行效率 

   数据本地性分类：  同节点(node-local)  同机架(rack-local)  其他（off-switch）

7.推测执行机制 

​	发现拖后腿的任务，为拖后腿任务启动一个备份任务，同时运行，谁先运行完，则采用谁的结果 

​	不能启用情况：  

​	 任务间存在严重的负载倾斜  特殊任务，比如任务向数据库中写数据

8.搭建Yarn集群	 

```
如果你之前已经完全启动hadoop，则yarn已启动，
	hadoop/sbin/start-yarn.sh 

验证：http://bigdata:18088（查看你的yarn-site.xml中设置的yarn.resourcemanager.webapp.address/属性）  

启动MR HistoryServer（可选） 
 sbin/mr-jobhistory-daemon.sh start  historyserver  

启动测试任务 
bin/hadoop jar  share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.3.jar  pi  100  1000  

//常用命令 
bin/yarn application  -status  application_1505620174459_0001(这是你的mapreduce任务id)
bin/yarn application -kill application_1505620174459_0001 
bin/yarn logs -applicationId application_1505620174459_0001
```

9.java编程，一般只实现Mappper的map和Reducer的reduce方法即可 我们需要考虑的主要是哪个或哪些参数做key

10.倒排索引，搜索的核心功能

11.案例：分组竞赛排名，有一组测试数据：名字,年龄，性别，分数 中间以'，'隔开  分别输出男子和女子组前3名   

 分析：以性别为key写map    

​		reduce中输出前三名  

 把数据上传到hdfs目录下的/data/member.txt，把java程序用maven打包成jar 

 执行命令:  

```
bin/hadoop jar /study/scoreFilter.jar com.zy.ScoreFilter hdfs://master:9000/data/member.txt  file:///study/output/
```

