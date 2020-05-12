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
*   @author 王洪宾
*/
using namespace std;
class Queue
{
public:
    void push(int node) {
         stack1.push(node);
    }

    int pop() {
		if(stack2.empty() && stack1.empty()){
			cout << "this queue is empty" << endl;
			return 0;
		}       
	    if(stack2.empty())
		{
            while(!stack1.empty())
            {
                int t=stack1.top();
                stack2.push(t);
                stack1.pop();
            }
		}
        int s=stack2.top();
        stack2.pop();
        return s;
    }

private:
    stack<int> stack1;
    stack<int> stack2;
};

int main()
{
    Queue queue;
    queue.push(2);
    queue.push(3);
    cout << queue.pop() << endl;
    cout << queue.pop() << endl;
    cout << queue.pop() << endl;
    return 0;
}