/**
 *   [用两个栈来实现一个队列]
 *   [题目]
 *   用两个栈来实现一个队列，完成队列的Push和Pop操作。
 *	队列中的元素为int类型。
 *   实现思路：
 *		队列：先入先出
 *		栈：  先入后出
 *		stack1中加入元素，stack2中存储要出的元素，也就是队首的元素。
 *		push: 往stack1中push元素。
 *		pop:  stack1中是加入的元素，stack2中是要出的元素。
 *			先判断stack2是否为空，若为空，将stack1中的所有元素pop至stack2中，取出stack2的栈顶元素pop出去；
 *　　　		若不为空，直接取出stack2的栈顶元素pop出去；
 *   @author 马泽佳
 */

public class StackToQueue {

    private Stack<Integer> stack1;
    private Stack<Integer> stack2;

    /** Initialize your data structure here. */
    public MyQueue() {
        stack1 = new Stack<>();
        stack2 = new Stack<>();
    }

    /** Push element x to the back of queue. */
    public void push(int x) {
        stack1.push(x);
    }

    /** Removes the element from in front of queue and returns that element. */
    public int pop() {
        if(stack1.isEmpty() && stack2.isEmpty()){
            throw new RuntimeException("Queue is Empty");
        }
        if(stack2.isEmpty()){
            while(!stack1.isEmpty()){
                stack2.push(stack1.pop());
            }
        }
        return stack2.pop();

    }

    /** Get the front element. */
    public int peek() {
        if(stack1.isEmpty() && stack2.isEmpty()){
            throw new RuntimeException("Queue is Empty");
        }
        if(stack2.isEmpty()){
            while(!stack1.isEmpty()){
                stack2.push(stack1.pop());
            }
        }
        return stack2.peek();
    }

    /** Returns whether the queue is empty. */
    public boolean empty() {
        return stack1.isEmpty() && stack2.isEmpty();
    }
}