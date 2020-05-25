/**
 * 移除链表中重复的节点 (未排序，保留一个重复的节点)
 * @param node 输入链表
 *             例如 ： 1 1 5 1 1 输出 1 5
 *             例如：4 2 3 1 3  输出  4231
 */
public static ListNode deleteSameNode(ListNode node){
        if (node == null || node.next == null){
            return node;
        }
        //创建去重使用的队列
        ArrayList<Integer> list = new ArrayList<>();
        ListNode per = node;
        //添加首个元素
        list.add(per.val);
        ListNode tem ;
        while (per!=null && per.next!=null){
            tem = per.next;
            int content = tem.val; //获取节点内容
            if (list.contains(content)) {
                if (tem.next!=null){ //下下一元素还是有值的，直接将下一个指向下下一个
                    per.next = tem.next;
                    continue;
                }else{ //下下一个无元素，则直接将下一个元素指向空，用于最后一个元素重复
                    per.next = null;
                    break;
                }
            }
            //正常不含有该元素则，直接平移到下一个节点
            list.add(content);
            per = per.next;
        }

        return node;
}