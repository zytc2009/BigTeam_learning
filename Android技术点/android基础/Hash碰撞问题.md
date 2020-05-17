# Hash碰撞问题总结

| 修改时间  | 修改人 | 修改内容 |
| --------- | ------ | -------- |
| 2020/5/17 | 王洪宾 |          |

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

我们现在来说明，oldCapacity假设为16(00010000), int highBit = e.hash & oldCapacity能够得到高位的值，因为低位全为0，经过与操作过后，低位一定是0。J 在这里是index，J 与 高位的值进行与操作过后，就能得到在扩容后面的新的index值。这样做的好处是尽可能少的改变节点的位置。



### 参考文档





