//实现一个函数，检查二叉树是否平衡。在这个问题中，平衡树的定义如下：任意一个节点，其两棵子树的高度差不超过 1。 示例 1: 给定二叉树 [3,9,20,nu
//ll,null,15,7]     3    / \   9  20     /  \    15   7 返回 true 。示例 2: 给定二叉树 [1,2,
//2,3,3,null,null,4,4]       1      / \     2   2    / \   3   3  / \ 4   4 返回 fal
//se 。 Related Topics 树 深度优先搜索


//leetcode submit region begin(Prohibit modification and deletion)
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
    private boolean flag = true;
    public boolean isBalanced(TreeNode root) {
        dfs(root);
        return flag;
    }

    private int dfs(TreeNode n){
        if(n == null || !flag)
            return 0;
        int leftDepth = dfs(n.left)+1;
        int rightDepth = dfs(n.right)+1;
        if(Math.abs(leftDepth-rightDepth) > 1)
            flag = false;
        return Math.max(leftDepth,rightDepth);
    }
}

