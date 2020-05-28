/**
 *爱丽丝和鲍勃一起玩游戏，他们轮流行动。爱丽丝先手开局。
 *
 * 最初，黑板上有一个数字 N 。在每个玩家的回合，玩家需要执行以下操作：
 *
 *
 * 	选出任一 x，满足 0 < x < N 且 N % x == 0 。
 * 	用 N - x 替换黑板上的数字 N 。
 *
 *
 * 如果玩家无法执行这些操作，就会输掉游戏。
 *
 * 只有在爱丽丝在游戏中取得胜利时才返回 True，否则返回 false。假设两个玩家都以最佳状态参与游戏。
 *
 *
 *
 * 示例 1：
 *
 * 输入：2
 * 输出：true
 * 解释：爱丽丝选择 1，鲍勃无法进行操作。
 *
 *
 * 示例 2：
 *
 * 输入：3
 * 输出：false
 * 解释：爱丽丝选择 1，鲍勃也选择 1，然后爱丽丝无法进行操作。
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/divisor-game
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 */


class Solution {
    public boolean divisorGame(int N) {
        //动态规划
        if (N == 1) {
            return false;
        }
        //dp[i]存的是操作数为i时的玩家的获胜情况
        boolean[] dp = new boolean[N+1];

        //初始化dp数组
        dp[1] = false;
        dp[2] = true;

        //遍历3-N并求解整个dp数组
        for (int i = 3; i <= N; i++) {
            //先置dp[i]为false，符合条件则置true
            dp[i] = false;

            //玩家都以最佳状态，即玩家操作i后的操作数i-x应尽可能使对手输，即dp[i-x]应尽可能为false
            //所以遍历x=1~i-1,寻找x的约数，使得dp[i-x]=false，那么dp[i]=true即当前操作数为i的玩家能获胜
            //如果找不到则为false，会输掉
            for (int x = 1; x < i; x++) {
                if (i % x == 0 && !dp[i-x]) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[N];
    }
}
