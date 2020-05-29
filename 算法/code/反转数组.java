/**
 * 反转数组中内容
 * @param s 数组
 *          例如： abd   - >  dba
 *          结题思路：本题目主要是要最后一个元素到第一个去，第一个到最后一个，倒数第二个到第二个位置，
 *          第二个到倒数第二个位置，所以可以认为是从中间隔断，两轴的元素交换！
 */
public static void reverseString(char[] s) {
        int count = s.length;
        char tem ;
        for (int i = 0;i < count / 2 ;i++){
            int j = count - 1 - i; //数组倒数相对应的位置 例如长度5   1：5  2：4 这样
            tem = s[i];
            s[i] = s[j];
            s[j] = tem;
        }
}