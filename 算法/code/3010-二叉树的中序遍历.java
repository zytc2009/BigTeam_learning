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

    public List<Integer> postorderTraversal(TreeNode root) {
        if(result == null){
            result = new ArrayList<>();
        }
        if(root == null){
            return result;
        }

        if(root.left != null){
            postorderTraversal(root.left);
        }

        if(root.right != null){
            postorderTraversal(root.right);
        }

        result.add(root.val);

        return result;
    }
}

