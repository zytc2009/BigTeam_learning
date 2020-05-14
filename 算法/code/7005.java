/**
 *   [递归操作]
 *
 *   [题目]
 *   输入一个字符串，打印出该字符串中字符的所有排列。
 *
 *  [思路]
 *	求整个字符串的全排列，可以看成两步：
 *	第一步首先求所有可能出现在第一个位置的字符，即把第一个字符和后面所有的字符交换，
 *	第二步固定第一个字符，求后面所有字符的排列。这时候仍然把后面的字符分成两部分，后面的第一个字符，
 *	和这个字符之后的所有字符，然后把后面的第一个字符和它后面的字符交换。
 *
 *  leedcode测试通过
 *
 *  有更好的方式可以进行替换
 *
 *   @author 马泽佳
 */

public class Solution{

    public String[] permutation(String s) {
        if(s == null){
            return null;
        }

        Set<String> data = new HashSet<>();
        permutation(s.toCharArray(),0,data);
        return data.toArray(new String[]{});
    }

    public void permutation(char[] chars,int pos,Set<String> result){
        if(pos == chars.length - 1){
            result.add(new String(chars));
        }

        for(int i = pos;i<chars.length;i++){
            char temp = chars[pos];
            chars[pos] = chars[i];
            chars[i] = temp;

            permutation(chars,pos + 1,result);

            temp = chars[i];
            chars[i] = chars[pos];
            chars[pos] = temp;
        }
    }


}