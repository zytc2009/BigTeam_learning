/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 *
 */
 //递归法
class Solution {

    private List<Integer> result;

    public List<Integer> preorderTraversal(TreeNode root) {
        if(result == null){
            result = new ArrayList<>();
        }
        if(root == null){
            return result;
        }

        if(root.left != null){
            preorderTraversal(root.left);
        }

        if(root.right != null){
            preorderTraversal(root.right);
        }

        result.add(root.val);

        return result;
    }
}

//非递归方法
class Solution {

	public  void postOrder(TreeNode root) {
		//待处理节点
		Stack<TreeNode> src = new Stack<TreeNode>();
		//存储结果，逆序
		Stack<TreeNode> res = new Stack<TreeNode>();
		//根节点入栈
		src.push(root);
		while(!src.isEmpty()){
			//出栈
			TreeNode p = src.pop();
			//根节点数据入结果栈
			res.push(p);
			//先压左树，因为是逆序的
			if(p.left != null){
				src.push(p.left);
			}
			//后压右树，因为是逆序的
			if(p.right != null){
				src.push(p.right);
			}
		}
		//输出最终后序遍历的结果
		while(!res.isEmpty()){
			System.out.print(res.pop().val + " ");
		}	
	}
}

