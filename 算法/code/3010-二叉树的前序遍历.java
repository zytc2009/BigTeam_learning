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

    /**
     * 递归方式
     * @param root
     * @return
     */
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

    /**
     * 非递归
     * @param root
     * @return
     */
    public List<Integer> preorderTraversal2(TreeNode root) {
        if(result == null){
            result = new ArrayList<>();
        }
        if(root == null){
            return result;
        }

        Stack<TreeNode> stack = new Stack<>();

        while (root != null || !stack.isEmpty()){

            while (root != null){
                result.add(root.val);
                stack.push(root);
                root = root.left;
            }

            if(!stack.isEmpty()){
                root = stack.pop();
                root = root.right;
            }
        }

        return result;
    }

}