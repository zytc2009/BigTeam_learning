package com.zy.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

//给定一个由表示变量之间关系的字符串方程组成的数组，每个字符串方程 equations[i] 的长度为 4，并采用两种不同的形式之一："a==b" 或 "a!
//=b"。在这里，a 和 b 是小写字母（不一定不同），表示单字母变量名。
// 只有当可以将整数分配给变量名，以便满足所有给定的方程时才返回 true，否则返回 false。
//
// 示例 1：
// 输入：["a==b","b!=a"]
//输出：false
//解释：如果我们指定，a = 1 且 b = 1，那么可以满足第一个方程，但无法满足第二个方程。没有办法分配变量同时满足这两个方程。
//
// 示例 2：
// 输入：["b==a","a==b"]
//输出：true
//解释：我们可以指定 a = 1 且 b = 1 以满足满足这两个方程。
//
// 示例 3：
// 输入：["a==b","b==c","a==c"]
//输出：true
//
// 示例 4
// 输入：["a==b","b!=c","c==a"]
//输出：false
//
// 示例 5：
// 输入：["c==c","b==d","x!=z"]
//输出：true

/**
 * 我们可以将每一个变量看作图中的一个节点，把相等的关系 == 看作是连接两个节点的边，那么由于表示相等关系的等式方程具有传递性，即如果 a==b 和 b==c 成立，则 a==c 也成立。也就是说，所有相等的变量属于同一个连通分量。因此，我们可以使用并查集来维护这种连通分量的关系。
 * 首先遍历所有的等式，构造并查集。同一个等式中的两个变量属于同一个连通分量，因此将两个变量进行合并。
 * 然后遍历所有的不等式。同一个不等式中的两个变量不能属于同一个连通分量，因此对两个变量分别查找其所在的连通分量，如果两个变量在同一个连通分量中，则产生矛盾，返回 false。
 * 如果遍历完所有的不等式没有发现矛盾，则返回 true。
 * 链接：https://leetcode-cn.com/problems/satisfiability-of-equality-equations/solution/deng-shi-fang-cheng-de-ke-man-zu-xing-by-leetcode-/
 */
class Solution {
    public boolean equationsPossible(String[] equations) {
        int length = equations.length;
        //26个字母代表等式中的变量元素
        int[] parent = new int[26];
        for (int i = 0; i < 26; i++) {
            parent[i] = i;
        }
        //先计算==式
        for (String str : equations) {
            if (str.charAt(1) == '=') {
                int index1 = str.charAt(0) - 'a';
                int index2 = str.charAt(3) - 'a';
                union(parent, index1, index2);
            }
        }
        //计算!=式
        for (String str : equations) {
            if (str.charAt(1) == '!') {
                int index1 = str.charAt(0) - 'a';
                int index2 = str.charAt(3) - 'a';
                if (find(parent, index1) == find(parent, index2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void union(int[] parent, int index1, int index2) {
        parent[find(parent, index1)] = find(parent, index2);
    }

    public int find(int[] parent, int index) {
        while (parent[index] != index) {
            System.out.println(index + "," + parent[index]);
            parent[index] = parent[parent[index]];
            index = parent[index];
        }
        return index;
    }
}