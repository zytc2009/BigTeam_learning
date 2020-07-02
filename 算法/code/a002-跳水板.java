//你正在使用一堆木板建造跳水板。有两种类型的木板，其中长度较短的木板长度为shorter，长度较长的木板长度为longer。你必须正好使用k块木板。编写一个方
//法，生成跳水板所有可能的长度。 
// 返回的长度需要从小到大排列。 
// 示例： 
// 输入：
//shorter = 1
//longer = 2
//k = 3
//输出： {3,4,5,6}
// 
// 提示： 
// 0 < shorter <= longer
// 0 <= k <= 100000 
// Related Topics 递归 记忆化

//解题思路
//        0 < shorter <= longer
//        0 <= k <= 100000
//        由于三个变量的取值范围，考虑到两种特殊情况
//        1、当k为0时，返回一个空数组；
//        2、当shorter和longer相等时，返回shorter*k即可；
//        3、最后就是通常情况了，根据数学归纳法，2种长度板子，必须用k块，不同的情况共k+1中，公式为(k - i) * shorter + i * longer
//        （PS：好像指定数组大小，初始化会快一些）

class Solution {
    public int[] divingBoard(int shorter, int longer, int k) {
        if (k == 0) {//当0块时，就没有数据
            return new int[0];
        }
        if (shorter == longer) {//当最长和最短相等时，就只有一种
            int result[] = new int[1];
            result[0] = shorter * k;
            return result;
        }
        int result[] = new int[k + 1];
        //循环把可能的结果往数组里面塞
        for (int i = 0; i <= k; i++) {
            result[i] = (k - i) * shorter + i * longer;
        }
        return result;
    }
}


