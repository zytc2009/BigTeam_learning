/*
给定一个二叉树，返回其按层次遍历的节点值。 （即逐层地，从左到右访问所有节点）。

例如:
给定二叉树: [3,9,20,null,null,15,7],

    3
   / \
  9  20
    /  \
   15   7
返回其层次遍历结果：

[
  [3],
  [9,20],
  [15,7]
]
*/

#include <iostream>
#include <vector>
#include <algorithm>
#include <queue>

using namespace std;

struct TreeNode{
    int val;
    TreeNode *left;
    TreeNode *right;
    TreeNode(int x):val(x), left(NULL), right(NULL) {}
};

/*
class Solution{
public:
    vector<vector<int> > Print(TreeNode* pRoot){
        vector<vector<int> > ans;
        if(pRoot == NULL)
            return ans;

        queue<TreeNode*> Q;
        Q.push(pRoot);
        while(!Q.empty()){
            vector<int> temp;
            for(int i=0, n=Q.size(); i<n; i++){
                TreeNode *pCur = Q.front();
                Q.pop();
                temp.push_back(pCur->val);
                if(pCur->left)
                    Q.push(pCur->left);
                if(pCur->right)
                    Q.push(pCur->right);
            }
            ans.push_back(temp);
        }

        return ans;
    }
};
*/

//采用先序遍历递归
class Solution {
public:    
    void pre(TreeNode *root,int depth, vector<vector<int>> &res)
    {
        if(root==NULL)
            return ;
		
		//增加一层
        if(depth>=res.size())
            res.push_back(vector<int>{});
		//根节点
        res[depth].push_back(root->val);
		//左子树
        pre(root->left,depth+1,res);
		//右子树
        pre(root->right,depth+1,res);
    }
	
    vector<vector<int>> Print(TreeNode* root) {
            vector<vector<int>> ans;
            pre(root,0,ans);
            return ans;
    }
};

int main()
{
    return 0;
}