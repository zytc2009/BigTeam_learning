/**
 * 移除链表倒数第N个元素
 * @param head 头结点
 * @param n 倒数位置
 * @return 移除后的链表
 *  给定一个链表: 1->2->3->4->5, 和 n = 2.
 *  当删除了倒数第二个节点后，链表变为 1->2->3->5.
 *  主要思路是找到第倒数第K个节点，找到它前面一个节点，直接指向下一个节点
 * 执行用时 :0 ms, 在所有 Java 提交中击败了 100.00% 的用户
 * 内存消耗 : 37.8 MB , 在所有 Java 提交中击败了 5.43% 的用户
 */
public static ListNode removeNthFromEnd(ListNode head, int n) {
        int count = 0 ;

        ListNode tem = head;

        while (tem!=null){ //第一遍找到长度
            count++;
            tem = tem.next;
        }

        int delPos = count - n ;
        if (delPos == 0)  //特殊情况，需要移除头节点
            return head.next;

        ListNode delNote = head;
        while (delPos > 1){ //找到对应要删除的节点前节点

            delNote = delNote.next;
            delPos -= 1 ;

        }
        //移除对应的节点
        if (delNote.next != null && delNote.next.next != null) {
            delNote.next = delNote.next.next;
        } else {
            delNote.next = null;

        }

        return head;
}