/**
 * 给定一个链表，旋转链表，将链表每个节点向右移动 k 个位置，其中 k 是非负数。
 * 输入: 1->2->3->4->5->NULL, k = 2
 * 输出: 4->5->1->2->3->NULL
 * 解释:
 * 向右旋转 1 步: 5->1->2->3->4->NULL
 * 向右旋转 2 步: 4->5->1->2->3->NULL
 *
 * @param head 头指针
 * @param k 旋转长度
 * @return
 *
 * 主要史对列表的尾巴节点移动到头节点的位置，简单的思路就是通过一个中间链表，将链表转换
 * 然后再根据转转的次数来头插数据
 *
 * author guojilong
 * 执行用时 :2 ms, 在所有 Java 提交中击败了12.69%的用户
 * 内存消耗 :39.4 MB, 在所有 Java 提交中击败了5.41%的用户
 */
public static ListNode rotateRight(ListNode head,int k){
        if(head==null||head.next==null){
            return head;
        }
        LinkedList<ListNode> content=new LinkedList<>();
        while(head!=null){
            content.add(new ListNode(head.val));
            head=head.next;
        }
        //对长度取余数，获取实际移动到最前边的数量
        int remote=k%content.size();
        for(int i=0;i<remote;i++){
            //将最后一位移动到最前边
            ListNode item=content.removeLast();
            content.addFirst(item);
        }

        ListNode tem=new ListNode(0);
        ListNode result=tem;
        for(int i=0;i<content.size();i++){
            tem.next=content.get(i);
            tem=tem.next;
        }
        return result.next;
}