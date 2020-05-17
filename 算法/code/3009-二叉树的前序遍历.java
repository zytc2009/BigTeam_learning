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

    private List<Integer> result;

    public List<Integer> preorderTraversal(TreeNode root) {
        if(result == null){
            result = new ArrayList<>();
        }
        if(root == null){
            return result;
        }
        result.add(root.val);
        if(root.left != null){
            preorderTraversal(root.left);
        }
        if(root.right != null){
            preorderTraversal(root.right);
        }
        return result;
    }
}