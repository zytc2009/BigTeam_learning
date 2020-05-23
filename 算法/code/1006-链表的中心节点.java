/**
 * 返回链表中心节点和之后的节点
 * 例1：输入：[1,2,3,4,5]  返回 [3,4,5]
 * 例2：输入：[1,2,3,4,5,6]  返回 [4,5,6]
 * @param head 头结点
 * @return 中心为节点
 * 思路：主要时找中心为，最好理解的方式通过向队列中添加数据，
 * 来确定长度，和对应的顺序，再通过位置找中心
 *
 * 执行用时 : 0 ms, 在所有 Java 提交中击败了100.00%的用户
 * 内存消耗 :36.9 MB, 在所有 Java 提交中击败了6.25%的用户
 * author guojilong
 */
public static ListNode middleNode(ListNode head) {
        if (head == null || head.next == null){
            return head;
        }
        LinkedList<ListNode> datas = new LinkedList<>();
        ListNode tem = head;
        while (tem!=null){
            datas.add(tem);//添加队列头节点
            tem = tem.next;
        }

        int count = datas.size();
        return datas.get(count / 2);
}