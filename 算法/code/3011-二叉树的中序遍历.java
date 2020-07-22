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
     * 递归
     * @param root
     * @return
     */
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
				
        result.add(root.val);

        if(root.right != null){
            postorderTraversal(root.right);
        }

        return result;
    }

    /**
     * 非递归
     * @param root
     * @return
     */
    public List<Integer> postorderTraversal2(TreeNode root) {
        if(result == null){
            result = new ArrayList<>();
        }
        if(root == null){
            return result;
        }

        Stack<TreeNode> stack = new Stack<>();

        while (root != null || !stack.isEmpty()){
            while (root != null){
                stack.push(root);
                root = root.left;
            }

            if(!stack.isEmpty()){
                root = stack.pop();
                result.add(root.val);
                root = root.right;
            }
        }
        return result;
    }
}

