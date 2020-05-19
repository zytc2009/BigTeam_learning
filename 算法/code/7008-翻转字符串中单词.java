/**
 * 给定一个字符串，逐个翻转字符串中的每个单词。
 *
 * 示例:
 *
 * 输入: "the sky is blue"
 * 输出: "blue is sky the"
 */
class Solution {
    public String reverseWords(String s) {
        if(s == null){
            return null;
        }
        s = s.trim();
        if(s.length() == 1){
            return s;
        }
        StringBuilder result = new StringBuilder();
        int last = s.length() - 1;
        int lastPre = last;
        while(last >= 0){

            //从后往前找到第一个空格
            while (lastPre >= 0 && s.charAt(lastPre) != ' '){
                lastPre--;
            }

            result.append(lastPre + 1,last + 1);
            result.append(" ");

            //寻找下一个单词的最右边起始位置
            while(lastPre > 0 && s.charAt(lastPre == ' ')){
                lastPre--;
            }
            last = lastPre;
        }


        return result.toString().trim();
    }
}