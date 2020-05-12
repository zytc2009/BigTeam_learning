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
*   @author 王洪宾
*/
using namespace std;

class TwoQueueWorkAsStack{
   public:
   /**
    * 选一个非空的队列入队
    * @param e
    * @return
    */
    int push(int e) {
       if (queueA.size() != 0) {
           cout <<"从queueA入队" << e << endl;
           queueA.push(e);
       } else if (queueB.size() != 0) {
          cout <<"从queueB入队"  << e << endl;
           queueB.push(e);
       } else {
           cout <<"从queueA入队"  << e << endl;
           queueA.push(e);
       }
       return e;
   }

    int pop() {
       if (queueA.size() == 0 && queueB.size() == 0) {
           return 0;
       }

       int result = 0;
       if (queueA.size() != 0) {
           while (queueA.size() > 0) {
               result = queueA.front();
               queueA.pop();
               if (queueA.size() != 0) {
                  cout <<"从queueA出队并入queueB队" << result << ", ";
                   queueB.push(result);
               }
           }
           cout <<"从queueA出队" << result << endl;
       } else {
           while (queueB.size() > 0) {
               result = queueB.front();
               queueB.pop();
               if (queueB.size() != 0) {
                   cout <<"从queueB出队并入queueA队" << result << ", ";
                   queueA.push(result);
               }
           }
           cout <<"从queueB出队" << result << endl;
       }
       return result;
   }
   private:
    queue<int> queueA;
    queue<int> queueB;
};

int main()
{
    TwoQueueWorkAsStack stack;
    stack.push(2);
    stack.push(3);
    stack.pop();
	stack.push(4);
    stack.pop();	
	stack.push(5);
    stack.pop();
    return 0;
}