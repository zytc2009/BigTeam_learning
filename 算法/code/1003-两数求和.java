//给出两个 非空 的链表用来表示两个非负的整数。其中，它们各自的位数是按照 逆序 的方式存储的，并且它们的每个节点只能存储 一位 数字。
//
// 如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。
//
// 您可以假设除了数字 0 之外，这两个数都不会以 0 开头。
//
// 示例：
//
// 输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
//输出：7 -> 0 -> 8
//原因：342 + 465 = 807
// Related Topics 链表 数学


//leetcode submit region begin(Prohibit modification and deletion)
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode(int x) { val = x; }
 * }
 *
 * 这个题主要是链表的遍历，对应的位置求和，如果没有节点用零，
 * 如果最后一个节点求和完需要留意一点，是否存在进位的情况，添加节点
 * author gjl
 * 执行耗时:2 ms,击败了99.92% 的Java用户
 * 内存消耗:39.6 MB,击败了94.74% 的Java用户
 */
public Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if (l1 == null && l2 == null){
            return l1;
        }

        if (l1 == null){ //一个链表为空则直接反转返回
            return l2;
        }else if (l2 == null){
            return l1;
        }
        ListNode tem = new ListNode(0); //遍历使用的链表
        ListNode result = tem;
        boolean isOverTen = false; //超过10位则向下一个节点加1
        while (l1 !=null || l2!=null){ //两个都为空则无需进行

            int l1V = l1 == null ? 0 : l1.val; //获取节点的值
            int l2V = l2 == null ? 0 : l2.val;

            tem.val = l1V+l2V;
            if (isOverTen){ //上一次两个数相加大于10
                tem.val++;
            }

            if (tem.val >=10){ //超过10的处理-=10
                isOverTen = true;
                tem.val -= 10;
            }else{
                isOverTen = false;
            }
            //将还有节点的指针指向下一位
            if (l1!=null){
                l1 = l1.next;
            }if (l2!=null){
                l2 = l2.next;
            }

            if (l1 == null && l2 == null){ //最后一次如果进位
                if (isOverTen){
                    tem.next = new ListNode(1); //直接向下一位赋值1则可以
                }
            }else{ //非最后一次则指向下一位
                tem.next  = new ListNode(0);
                tem = tem.next;
            }
        }
        return result;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
