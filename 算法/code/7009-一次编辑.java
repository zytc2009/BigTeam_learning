/**
 * 字符串有三种编辑操作:插入一个字符、删除一个字符或者替换一个字符。 给定两个字符串，
 * 编写一个函数判定它们是否只需要一次(或者零次)编辑。
 *
 * 示例 1:
 *
 * 输入:
 * first = "pale"
 * second = "ple"
 * 输出: True
 *
 *
 *
 * 示例 2:
 *
 * 输入:
 * first = "pales"
 * second = "pal"
 * 输出: False
 *
 */
class Solution {
    public boolean oneEditAway(String first, String second) {
        if((first == null || first.length() == 0) && (second == null || second.length() == 0)){
            return true;
        }
        if(first == null || first.length() == 0){
            return second.length() == 1;
        }
        if(second == null || second.length() == 0){
            return first.length() == 1;
        }

        int i,j,diff;
        i = j = diff = 0;
        while(i < first.length() && j < second.length()){
            if(first.charAt(i) != second.charAt(j)){
                diff++;
                if(first.length() > second.length()){
                    i++;
                } else if(first.length() < second.length()){
                    j++;
                } else {
                    i++;
                    j++;
                }
                if(diff > 1){
                    return false;
                }
            } else {
                i++;
                j++;
            }
        }
        return diff + (first.length() - i) + (second.length() - j) <= 1;
    }
}