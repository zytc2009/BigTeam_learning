package com.zy.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * 给定一个经过编码的字符串，返回它解码后的字符串。
 * 编码规则为: k[encoded_string]，表示其中方括号内部的 encoded_string 正好重复 k 次。注意 k 保证为正整数。
 * 你可以认为输入字符串总是有效的；输入字符串中没有额外的空格，且输入的方括号总是符合格式要求的。
 * 此外，你可以认为原始数据不包含数字，所有的数字只表示重复的次数 k ，例如不会出现像 3a 或 2[4] 的输入。
 *
 * 示例 1：
 *
 * 输入：s = "3[a]2[bc]"
 * 输出："aaabcbc"
 * 示例 2：
 *
 * 输入：s = "3[a2[c]]"
 * 输出："accaccacc"
 * 示例 3：
 *
 * 输入：s = "2[abc]3[cd]ef"
 * 输出："abcabccdcdcdef"
 * 示例 4：
 *
 * 输入：s = "abc3[cd]xyz"
 * 输出："abccdcdcdxyz"
 *
 * https://leetcode-cn.com/problems/decode-string/solution/decode-string-fu-zhu-zhan-fa-di-gui-fa-by-jyd/
 */

class Solution {
  public class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode(int x) { val = x; }
  }
    public String decodeString(String s) {
        return dfs(s, 0)[1];
    }

    private String[] dfs(String s, int i) {
       StringBuilder builder = new StringBuilder();
       int nums = 0;
       while(i<s.length()){
           if(s.charAt(i) >= '0' && s.charAt(i) <= '9')
               //暂不考虑乱序串，如2a2[b]
               nums = nums * 10 + Integer.parseInt(String.valueOf(s.charAt(i)));
           else if(s.charAt(i) == '[') {
               String[] tmp = dfs(s, i + 1);
               i = Integer.parseInt(tmp[0]);
               while(nums > 0) {
                   builder.append(tmp[1]);
                   nums--;
               }
           }
           else if(s.charAt(i) == ']')
               return new String[] { String.valueOf(i), builder.toString() };
           else {
               if(nums > 0){//如果有乱序串，如2a2[b]处理
                  //先添加数字字符
               }
               builder.append(String.valueOf(s.charAt(i)));
           }
           i++;
       }
       return new String[]{String.valueOf(i), builder.toString()};
    }
}