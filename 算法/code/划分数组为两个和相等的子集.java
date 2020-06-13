package com.zy.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 题目：划分数组为两个和相等的子集
 *多重背包，使用动态规划解决。dp[i][j]表示前i个数，和为j。
 * 1). 判断数组中所有数的和是否为偶数，因为奇数是不可能有解的；
 * 2). dp[i][j] = Math.max(dp[i - 1][j],dp[i - 1][j - nums[i - 1]] + nums[i - 1])
 * 3).如果最后dp[nums.length][sum / 2] = sum / 2,则返回true.
 */

class Solution { //60ms
    public boolean canPartition(int[] nums) {
        if(nums.length == 0) return false;
        int sum = 0;
        for(int n : nums){//求所有数的和
            sum += n;
        }
        if(sum % 2 == 1) return false;//不存在两个和相等的子集
        sum = sum / 2;
        int[][] dp = new int[nums.length + 1][sum + 1];
        for(int i = 0;i <= nums.length;i ++){
            for(int j = 0;j <= sum;j ++){
                if(i == 0) {//表示前0个数，所以价值均为0；
                    dp[i][j] = 0;
                }else if(j < nums[i - 1]){//在装第i-1个数时，先判断剩余容量j是否大于nums[i-1]
                    dp[i][j] = dp[i - 1][j]; //小于表示空间不够，所以维持不变
                }else{//空间够，就通过比较大小来判断是否该放入第i-1个数
                    dp[i][j]=Math.max(dp[i - 1][j],dp[i - 1][j - nums[i - 1]] + nums[i - 1]);
                }
            }
        }
        return dp[nums.length][sum] == sum;
    }
}

/**
 *  定义dp[i]表示数字i是否是原数组的任意个子集合之和，初始化dp[0]为true，
 *  我们需要遍历原数组中的数字，对于遍历到的每个数字nums[i]，
 *  我们需要更新我们的dp数组，要更新[nums[i], sum]之间的值，
 *  那么对于这个区间中的任意一个数字j，如果dp[j - nums[i]]为true的话，那么dp[j]就一定为true。
 *  递推公式如下：dp[j] = dp[j] || dp[j - nums[i]]         (nums[i] <= j <= target)
 */
class Solution { //33ms
    public boolean canPartition(int[] nums) {
        if(nums.length == 0) return false;
        int sum = 0;
        for(int n : nums){//求所有数的和
            sum += n;
        }
        if(sum % 2 == 1) return false;//不存在两个和相等的子集
        sum = sum / 2;
        boolean[] dp = new boolean[sum + 1];
        dp[0] = true;
        for (int i = 0;i < nums.length;i ++){
            for (int j = sum;j >= nums[i];j --){
                dp[j] = dp[j] || dp[j - nums[i]];
            }
        }
        return dp[dp.length - 1];
    }
}
