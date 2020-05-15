/**
 *   输入一棵二叉树的根节点，求该树的深度。从根节点到叶节点依次经过的节点
 *   （含根、叶节点）形成树的一条路径，最长路径的长度为树的深度。
 *
 * 例如：
 *     3
 *    / \
 *   4  8
 *     /  \
 *    6  2
 *
 * 返回它的最大深度 3 。
 *
 *
 *   @author 马泽佳
 */

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        // 递归返回左右节点的深度。
        int left = maxDepth(root.left);
        int right = maxDepth(root.right);
        // 得到左右子树深度的最大值 +1 就是当前节点的深度。
        return Math.max(left, right) + 1;
    }
}