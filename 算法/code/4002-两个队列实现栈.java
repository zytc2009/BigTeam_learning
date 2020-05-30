/**
 *   [用两个队列来实现一个栈]
 *   [题目]
 *   用两个队列来实现一个栈，完成栈的Push和Pop操作。
 *	队列中的元素为int类型。
 *   实现思路：
 *		队列：先入先出,	栈：  先入后出
 *		任何时候两个队列总有一个是空的。
 *		push: 向非空队列中add元素。
 *		pop:  将非空队列元素除队尾最后一个元素外，
 *			  导入另一空队列中，最后一个元素出队；
 *   @author 马泽佳
 */

public class QueneToStack{

    private Queue<Integer> quene1;
    private Queue<Integer> quene2;

    /** Initialize your data structure here. */
    public MyStack() {
        quene1 = new LinkedList<>();
        quene2 = new LinkedList<>();
    }

    /** Push element x onto stack. */
    public void push(int x) {
        if(!quene1.isEmpty()){
            quene1.add(x);
        } else {
            quene2.add(x);
        }
    }

    /** Removes the element on top of the stack and returns that element. */
    public int pop() {
        if(quene1.isEmpty() && quene2.isEmpty()){
            throw new RuntimeException("Stack is Empty");
        }
        if(!quene1.isEmpty() && quene2.isEmpty()){
            while(quene1.size() > 1){
                quene2.offer(quene1.poll());
            }
            return quene1.poll();
        }

        if(!quene2.isEmpty() && quene1.isEmpty()){
            while(quene2.size() > 1){
                quene1.offer(quene2.poll());
            }
            return quene2.poll();
        }
        return 0;
    }

    /** Get the top element. */
    public int top() {

        if(!quene1.isEmpty() && quene2.isEmpty()){
            while(quene1.size() > 1){
                quene2.offer(quene1.poll());
            }
            int top = quene1.poll();
            quene2.offer(top);
            return top;
        }

        if(!quene2.isEmpty() && quene1.isEmpty()){
            while(quene2.size() > 1){
                quene1.offer(quene2.poll());
            }
            int top = quene2.poll();
            quene1.offer(top);
            return top;
        }
        return 0;
    }

    /** Returns whether the stack is empty. */
    public boolean empty() {
        return quene1.isEmpty() && quene2.isEmpty();
    }
}