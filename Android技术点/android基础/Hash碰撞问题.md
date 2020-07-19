# HashMap原理

| 修改时间  | 修改人 | 修改内容 |
| --------- | ------ | -------- |
| 2020/5/17 | 王洪宾 |          |

#### HashMap原理

原理：数组+单链表+红黑树（1.8之前是数组+链表）

继承自Map

线程不安全存取过程没有加锁

hashmap中允许存在null键和null值

初始大小为16，扩容2n

为何扩容：因为数据结构是数组，所以涉及到扩容，每次扩容为原来2倍

为何容量是2的倍数：计算index时使用hash & （length-1）如果是2到倍数那么length-1 是奇数，可以用位运算代替取余运算，提高效率

HashMap碰撞问题：

由于hash算法有可能使不同的元素计算出同一个hashcode，当多个不同key值的hashcode相同并要存入hashmap时，称为hashmap的碰撞。

hashmap的碰撞使用拉链法解决冲突，把HashCode相同的Value连成链表. get的时候根据Key又去桶里找，如果获得的是链表说明是冲突的，此时还需要检测Key是否相同。

put：

1. 判断数组 table 是否为 null 或长度为 0，如果是则执行 resize() 进行扩容。

2. 计算键值 key 对应的数组下标 i，如果 table[i]==null，则直接新建节点添加，转向步骤 6。

3. 如果table[i] 不为空，判断 key 是否就在 table[i] 的首个元素，如果是则直接对 value 进行赋值，并返回旧的 value，算法结束。

4. 如果不是，判断 table[i] 是否为红黑树，如果是红黑树，则转入对红黑树的操作（这一块不展开讲解）。

5. 如果不是红黑树，遍历 table[i]，如果遍历过程中发现 key 已存在，则直接对 value 赋值，并返回旧的 value，算法结束。否则，将键值对插入链表尾部，然后判断插入后链表长度是否大于 8，如果是，就把链表转换为红黑树。

6. 插入成功后，判断实际存在的键值对数量 size 是否超多了最大容量 threshold，如果超过，进行扩容。

#### hashmap为什么大于8才转化为红黑树，加载因子为什么是0.75

TreeNodes占用空间是普通Nodes的两倍,当hashCode离散性很好的时候，树型bin用到的概率非常小，因为数据均匀分布在每个bin中，几乎不会有bin中链表长度会达到阈值。但是在随机hashCode下，离散性可能会变差。不过理想情况下随机hashCode算法下所有bin中节点的分布频率会遵循泊松分布，我们可以看到，一个bin中链表长度达到8个元素的概率为0.00000006，几乎是不可能事件。所以，之所以选择8，不是拍拍屁股决定的，而是根据概率统计决定的。

因为Map中桶的元素初始化是链表保存的，其查找性能是O(n)，而树结构能将查找性能提升到O(log(n))。

加载因子过高，例如为1，虽然减少了空间开销，提高了空间利用率，但同时也增加了查询时间成本；
加载因子过低，例如0.5，虽然可以减少查询时间成本，但是空间利用率很低，同时提高了rehash操作的次数。
在设置初始容量时应该考虑到映射中所需的条目数及其加载因子，以便最大限度地减少rehash操作次数，所以，一般在使用HashMap时建议根据预估值设置初始容量，减少扩容操作。
选择0.75作为默认的加载因子，完全是时间和空间成本上寻求的一种折衷选择



#### 你觉得HashMap的元素顺序和什么有关？

> HashMap 底层是 hash 数组和单向链表实现，数组中的每个元素都是链表，由 Node 内部类（实现 Map.Entry<K,V>接口）实现，HashMap 通>过 put & get 方法存储和获取。

> 存储对象时，将 K/V 键值传给 put() 方法：①、调用 hash(K) 方法计算 K 的 hash 值，然后结合数组长度，计算得数组下标；②、调整数组大小（当容器中的元素个数大于 capacity * loadfactor 时，容器会进行扩容resize 为 2n）；
> ③、i.如果 K 的 hash 值在 HashMap 中不存在，则执行插入，若存在，则发生碰撞；
> ii.如果 K 的 hash 值在 HashMap 中存在，且它们两者 equals 返回 true，则更新键值对；
> iii. 如果 K 的 hash 值在 HashMap 中存在，且它们两者 equals 返回 false，则插入链表的尾部（尾插法）或者红黑树中（树的添加方式）。

> （JDK 1.7 之前使用头插法、JDK 1.8 使用尾插法）
> （注意：当碰撞导致链表大于 TREEIFY_THRESHOLD = 8 时，就把链表转换成红黑树）

> 获取对象时，将 K 传给 get() 方法：①、调用 hash(K) 方法（计算 K 的 hash 值）从而获取该键值所在链表的数组下标；②、顺序遍历链表，equals()方法查找相同 Node 链表中 K 值对应的 V 值。

hashCode 是定位的，存储位置；equals是定性的，比较两者是否相等

### 碰撞的原因

HashMap是最常用的集合类框架之一，它实现了Map接口，所以存储的元素也是键值对映射的结构，并允许使用null值和null键，null键只能有一个，其内元素是无序的。HashMap内部也是一个线性的数组存储数据。 既然是线性数组，为什么能随机存取？这里HashMap用了一个算法：

int hash = key.hashCode(); // 这个hashCode方法这里不详述,只要理解每个key的hash是一个固定的int值
int index = hash & (tab.length - 1);//tab的默认大小是16，扩容的方式是 old*2。

问题：如果两个key通过hash%Entry[].length得到的index相同，就产生了**碰撞**，那会不会有覆盖的危险？

### 碰撞的解决

HashMap使用链表来解决碰撞问题。Entry重要的属性有*key , value, next*。在调用HashMap的put方法或get方法时，都会首先调用hashcode方法，去查找key相关的索引，当有冲突时，再调用key的equals方法判断。hashMap在每个链表节点存储键值对对象。当两个不同的键却有相同的hashCode时，碰撞发生了，对象将会存储在这个节点的下一个节点中。键对象的equals()来找到键值对。

 

```
public V put(K key, V value) {
   if (key == null)
      return putForNullKey(value); //null总是放在数组的第一个链表中

	int hash = secondaryHash(key);
    HashMapEntry<K, V>[] tab = table;
    int index = hash & (tab.length - 1);
    //遍历节点链表
    for (HashMapEntry<K, V> e = tab[index]; e != null; e = e.next) {
        if (e.hash == hash && key.equals(e.key)) {
            preModify(e);
            V oldValue = e.value;
            e.value = value;
            return oldValue;
        }
    }

    // No entry for (non-null) key is present; create one
    modCount++;
    if (size++ > threshold) {
        //如果size超过threshold，则扩充table大小
        tab = doubleCapacity();
        index = hash & (tab.length - 1);
    }
    addNewEntry(key, value, hash, index);
    return null;
}
```

### HashMap 如何解决容量不足的问题

```
 private HashMapEntry<K, V>[] doubleCapacity() {
        HashMapEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        //容量翻倍
        int newCapacity = oldCapacity * 2;
        HashMapEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size == 0) {
            return newTable;
        }

        for (int j = 0; j < oldCapacity; j++) {
            /*
             * Rehash the bucket using the minimum number of field writes.
             * This is the most subtle and delicate code in the class.
             */
            HashMapEntry<K, V> e = oldTable[j];
            if (e == null) {
                continue;
            }
            //取高位
            int highBit = e.hash & oldCapacity;
            HashMapEntry<K, V> broken = null;
            newTable[j | highBit] = e;
            for (HashMapEntry<K, V> n = e.next; n != null; e = n, n = n.next) {
                int nextHighBit = n.hash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null)
                        newTable[j | nextHighBit] = n;
                    else
                        broken.next = n;
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.next = null;
        }
        return newTable;
}
```

我们注意其中这部分的代码

int highBit = e.hash & oldCapacity;
newTable[j | highBit] = e;

我们现在来说明，oldCapacity假设为16(00010000), int highBit = e.hash & oldCapacity能够得到高位的值（高位一半的几率为1），因为低位全为0，经过与操作过后，低位一定是0。J 在这里是index，J 与 高位的值进行或操作过后，就能得到在扩容后面的新的index值。这样做的好处是尽可能少的改变节点的位置，只有一半的元素需要搬家。

比如长度16扩容到了32：

```bash
Hash 10100101 11000100 00110101
 &	 00000000 00000000 00011111    //掩码=32-1
 ----------------------------------
	 00000000 00000000 00010101    //只是第5位有可能变化
```





#### 并发集合

1、ConcurrentHashMap 支持完全并发的检索和更新，所希望的可调整并发的哈希表。此类遵守与 Hashtable 相同的功能规范，并且包括对应于 Hashtable 的每个方法的方法版本。不过，尽管所有操作都是线程安全的，但检索操作不必锁定，并且不支持以某种防止所有访问的方式锁定整个表。此类可以通过程序完全与 Hashtable 进行互操作，这取决于其线程安全，而与其同步细节无关。

2、ConcurrentSkipListMap 是基于跳表的实现，也是支持key有序排列的一个key-value数据结构，在并发情况下表现很好，是一种空间换时间的实现，ConcurrentSkipListMap是基于一种乐观锁的方式去实现高并发。

3、ConCurrentSkipListSet （在JavaSE 6新增的）提供的功能类似于TreeSet，能够并发的访问有序的set。因为ConcurrentSkipListSet是基于“跳跃列表（skip list）”实现的，只要多个线程没有同时修改集合的同一个部分，那么在正常读、写集合的操作中不会出现竞争现象。

4、CopyOnWriteArrayList 是ArrayList 的一个线程安全的变形，其中所有可变操作（添加、设置，等等）都是通过对基础数组进行一次新的复制来实现的。这一般需要很大的开销，但是当遍历操作的数量大大超过可变操作的数量时，这种方法可能比其他替代方法更 有效。在不能或不想进行同步遍历，但又需要从并发线程中排除冲突时，它也很有用。“快照”风格的迭代器方法在创建迭代器时使用了对数组状态的引用。此数组在迭代器的生存期内绝不会更改，因此不可能发生冲突，并且迭代器保证不会抛出 ConcurrentModificationException。自创建迭代器以后，迭代器就不会反映列表的添加、移除或者更改。不支持迭代器上更改元素的操作（移除、设置和添加）。这些方法将抛出 UnsupportedOperationException。

5、CopyOnWriteArraySet 线程安全的无序的集合，可以将它理解成线程安全的HashSet。有意思的是，CopyOnWriteArraySet和HashSet虽然都继承于共同的父类AbstractSet；但是，HashSet是通过“散列表(HashMap)”实现的，而CopyOnWriteArraySet则是通过“动态数组(CopyOnWriteArrayList)”实现的，并不是散列表。

6、ConcurrentLinkedQueue 是一个基于链接节点的、无界的、线程安全的队列。此队列按照 FIFO（先进先出）原则对元素进行排序，队列的头部 是队列中时间最长的元素。队列的尾部 是队列中时间最短的元素。新的元素插入到队列的尾部，队列检索操作从队列头部获得元素。当许多线程共享访问一个公共 collection 时，ConcurrentLinkedQueue 是一个恰当的选择，此队列不允许 null 元素。



### 参考文档





