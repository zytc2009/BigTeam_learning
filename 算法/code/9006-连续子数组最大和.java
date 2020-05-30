/**
 *输入一个整型数组，数组里有正数也有负数。数组中的一个或连续多个整数组成一个子数组。求所有子数组的和的最大值。
 *
 * 要求时间复杂度为O(n)。
 *
 *
 *
 * 示例1:
 *
 * 输入: nums = [-2,1,-3,4,-1,2,1,-5,4]
 * 输出: 6
 * 解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。
 *
 * 1 <= nums.length <= 10^5
 *
 */
class Solution {
    public int maxSubArray(int[] nums) {
        if(nums.length == 1){
            return nums[0];
        }
        int[] dp = new int[nums.length + 1];
        dp[0] = nums[0];
        int res = dp[0];
        for(int i = 1;i<nums.length;i++){
            dp[i] = nums[i] + Math.max(dp[i-1],0);
            res = Math.max(dp[i],res);
        }
        return res;
    }
}
