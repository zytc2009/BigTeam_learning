### 常用操作符

Kotlin 的操作符跟 RxJava 基本一致，不需要额外记忆。

##### 下标操作类

- contains —— 判断是否有指定元素
- elementAt —— 返回对应的元素，越界会抛IndexOutOfBoundsException
- firstOrNull —— 返回符合条件的第一个元素，没有 返回null
- lastOrNull —— 返回符合条件的最后一个元素，没有 返回null
- indexOf —— 返回指定元素的下标，没有 返回-1
- singleOrNull —— 返回符合条件的单个元素，如有没有符合或超过一个，返回null

##### 判断类

- any —— 判断集合中 是否有满足条件 的元素
- all —— 判断集合中的元素 是否都满足条件
- none —— 判断集合中是否 都不满足条件，是则返回true
- count —— 查询集合中 满足条件 的 元素个数
- reduce —— 从 第一项到最后一项进行累计

##### 过滤类

- filter —— 过滤 掉所有 满足条件 的元素
- filterNot —— 过滤所有不满足条件的元素
- filterNotNull —— 过滤NULL
- take —— 返回前 n 个元素

##### 转换类

- map —— 转换成另一个集合（与上面我们实现的 convert 方法作用一样）;
- mapIndexed —— 除了转换成另一个集合，还可以拿到Index(下标);
- mapNotNull —— 执行转换前过滤掉 为 NULL 的元素
- flatMap —— 自定义逻辑合并两个集合；
- groupBy —— 按照某个条件分组，返回Map；

##### 排序类

- reversed —— 反序
- sorted —— 升序
- sortedBy —— 自定义排序
- sortedDescending —— 降序