/**
 * 链表中倒数第k个节点
 * 输入一个链表，输出该链表中倒数第k个节点。为了符合大多数人的习惯，
 * 本题从1开始计数，即链表的尾节点是倒数第1个节点。例如，一个链表有6个节点，
 * 从头节点开始，它们的值依次是1、2、3、4、5、6。这个链表的倒数第3个节点是值为4的节点。
 *
 *
 */
class Solution {
    public ListNode getKthFromEnd(ListNode head, int k) {
        if(head == null || k <= 0){
            return null;
        }
        ListNode nodePre;
        ListNode nodeAfter = head;
        int curr = 0;
        while(nodeAfter != null && curr != k - 1){
            nodeAfter = nodeAfter.next;
            curr++;
        }
        //如果总节点数都不在k个当中，则无效
        if(nodeAfter == null){
            return null;
        }

        nodePre = head;
        while(nodeAfter.next != null){
            nodeAfter = nodeAfter.next;
            nodePre = nodePre.next;
        }
        return nodePre;
    }
}