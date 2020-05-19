/**
 * 输入一个链表的头节点，从尾到头反过来返回每个节点的值（用数组返回）。
 * 示例 1：
 * 输入：head = [1,3,2]
 * 输出：[2,3,1]
 *
 * 解题思路：先获取到链表长度，然后从数组尾部开始存放元素
 */
public Solution {

    public int[] reversePrint(ListNode head) {
        int count = 0;
        ListNode temp = head;
        while(temp != null){
            count++;
            temp = temp.next;
        }

        temp = head;
        int[] result = new int[count];
        for(int i = count - 1;i >= 0;i--){
            result[i] = temp.val;
            temp = temp.next;
        }

        return result;
    }
}
