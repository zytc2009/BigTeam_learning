package com.zy.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

//给定一个整数数组和一个整数 k，你需要找到该数组中和为 k 的连续的子数组的个数。
//
// 示例 1 :
//输入:nums = [1,1,1], k = 2
//输出: 2 , [1,1] 与 [1,1] 为两种不同的情况。
// 说明 :
// 数组的长度为 [1, 20,000]。
// 数组中元素的范围是 [-1000, 1000] ，且整数 k 的范围是 [-1e7, 1e7]。
//
// Related Topics 数组 哈希表

/**
 * 前缀和 + 哈希表优化
 * 我们可以基于方法一利用数据结构进行进一步的优化，我们知道方法一的瓶颈在于对每个 ii，我们需要枚举所有的 jj 来判断是否符合条件，这一步是否可以优化呢？答案是可以的。
 *
 * 我们定义 extit{pre}[i]pre[i] 为 [0..i][0..i] 里所有数的和，则 extit{pre}[i]pre[i] 可以由 extit{pre}[i-1]pre[i−1] 递推而来，即：
 *
 * extit{pre}[i]=extit{pre}[i-1]+extit{nums}[i]
 * pre[i]=pre[i−1]+nums[i]
 *
 * 那么「[j..i][j..i] 这个子数组和为 kk 」这个条件我们可以转化为
 *
 * extit{pre}[i]-extit{pre}[j-1]==k
 * pre[i]−pre[j−1]==k
 *
 * 简单移项可得符合条件的下标 jj 需要满足
 *
 * extit{pre}[j-1] == extit{pre}[i] - k
 * pre[j−1]==pre[i]−k
 *
 * 所以我们考虑以 ii 结尾的和为 kk 的连续子数组个数时只要统计有多少个前缀和为 extit{pre}[i]-kpre[i]−k 的 extit{pre}[j]pre[j] 即可。我们建立哈希表 extit{mp}mp，以和为键，出现次数为对应的值，记录 extit{pre}[i]pre[i] 出现的次数，从左往右边更新 extit{mp}mp 边计算答案，那么以 ii 结尾的答案 extit{mp}[extit{pre}[i]-k]mp[pre[i]−k] 即可在 O(1)O(1) 时间内得到。最后的答案即为所有下标结尾的和为 kk 的子数组个数之和。
 * 需要注意的是，从左往右边更新边计算的时候已经保证了extit{mp}[extit{pre}[i]-k]mp[pre[i]−k] 里记录的 extit{pre}[j]pre[j] 的下标范围是 0\leq j\leq i0≤j≤i 。同时，由于extit{pre}[i]pre[i] 的计算只与前一项的答案有关，因此我们可以不用建立 extit{pre}pre 数组，直接用 extit{pre}pre 变量来记录 pre[i-1]pre[i−1] 的答案即可。
 *
 * 作者：LeetCode-Solution
 * 链接：https://leetcode-cn.com/problems/subarray-sum-equals-k/solution/he-wei-kde-zi-shu-zu-by-leetcode-solution/
 */
public class Solution {
    public int subarraySum(int[] nums, int k) {
        int count = 0, pre = 0;
        HashMap< Integer, Integer > mp = new HashMap < > ();
        mp.put(0, 1);
        for (int i = 0; i < nums.length; i++) {
            pre += nums[i];
            if (mp.containsKey(pre - k))
                count += mp.get(pre - k);
            mp.put(pre, mp.getOrDefault(pre, 0) + 1);
        }
        return count;
    }
}