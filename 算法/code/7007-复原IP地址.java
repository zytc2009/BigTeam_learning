package com.zy.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 给定一个只包含数字的字符串，复原它并返回所有可能的 IP 地址格式。
 * 有效的 IP 地址正好由四个整数（每个整数位于 0 到 255 之间组成），整数之间用 '.' 分隔。
 *
 * 示例:
 *
 * 输入: "25525511135"
 * 输出: ["255.255.11.135", "255.255.111.35"]
 */
class Solution {
    public List<String> restoreIpAddresses(String s) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        backtrace(s, 0, cur, 0, res);
        return res;
    }


    /**
     * @param s  原始串
     * @param index 查找位置
     * @param cur  当前ip串信息
     * @param depth 递归深度
     * @param res
     */
    public void backtrace(String s, int index, StringBuilder cur, int depth, List<String> res) {
        int len = cur.length();
        if(depth == 4) {
            // 如果字符串都取完了，可以加入结果集
            if(index == s.length()) {
                // 移除掉最后的"."
                cur.deleteCharAt(cur.length() - 1);
                res.add(cur.toString());
            }
            return;
        }
        // 每组ip地址的长度，最大为3
        for(int i = 1; i <= 3; i++) {
            if(index + i > s.length()){//防止越界
                break;
            }
            // 注意ip数字的合法性，不能大于255
            if(Integer.parseInt(s.substring(index, index + i)) > 255){
                break;
            }
            int num = Integer.parseInt(s.substring(index, index + i));
            // 不能存在01.001.01.01，主要是判断第一位是不是0，除非就是0
            if(String.valueOf(num).length() != i){
                break;
            }
            cur.append(s.substring(index, index + i));
            cur.append(".");
            backtrace(s, index + i, cur, depth + 1, res);
            cur.setLength(len);
        }
    }

}