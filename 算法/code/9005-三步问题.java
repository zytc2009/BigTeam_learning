/**
 *三步问题。有个小孩正在上楼梯，楼梯有n阶台阶，小孩一次可以上1阶、2阶或3阶。实现一种方法，
 * 计算小孩有多少种上楼梯的方式。结果可能很大，你需要对结果模1000000007。
 *
 *  示例1:
 *
 *  输入：n = 3
 *  输出：4
 *  说明: 有四种走法
 *
 *
 *  示例2:
 *
 *  输入：n = 5
 *  输出：13
 *
 *
 */


class Solution {
    public int waysToStep(int n) {
        if(n == 1){
            return 1;
        }

        if(n == 2){
            return 2;
        }

        if(n == 3){
            return 4;
        }

        int[] dp = new int[n + 1];

        dp[1] = 1;
        dp[2] = 2;
        dp[3] = 4;

        int mod = 10 0000 0007;

        for(int i = 4;i<=n;i++){
            dp[i] = dp[i-1] % mod ;
            dp[i] = (dp[i] + dp[i-2]) % mod;
            dp[i] = (dp[i] + dp[i-3]) % mod;
        }
        return dp[n];
    }
	
	另一种方案是：
	int waysToStep(int n){
        if (n < 4) {
            return n == 3 ? 4 : n;
        }
        //用变量替换数组
        int a = 1, b = 2, c = 4;
        for (int i = 4; i <= n; ++i) {
            int temp = (a + b) % 1000000007 + c;
            a = b;
            b = c;
            c = temp % 1000000007;
        }
        return c;
    }
}
