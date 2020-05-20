/**
 *
 * 给定一棵二叉树，想象自己站在它的右侧，按照从顶部到底部的顺序，返回从右侧所能看到的节点值。
 *
 * 示例:
 *
 * 输入: [1,2,3,null,5,null,4]
 * 输出: [1, 3, 4]
 * 解释:
 *
 *    1            <---
 *  /   \
 * 2     3         <---
 *  \     \
 *   5     4       <---
 *
 *  这个算法比较常规，看看是否还有更好的方式。
 *
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public List<Integer> rightSideView(TreeNode root) {
        if(root == null){
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        LinkedList<TreeNode> quene = new LinkedList<>();
        quene.offer(root);
        while(!quene.isEmpty()){
            int size = quene.size();

            for(int i = 0;i < size;i++){
                TreeNode temp = quene.poll();

                if(temp.left != null){
                    quene.offer(temp.left);
                }
                if(temp.right != null){
                    quene.offer(temp.right);
                }

                if(i == size-1){
                    result.add(temp.val);
                }
            }
        }

        return result;

    }
}