### 1. FutureTask的用法

  一种非常经典的设计模式，这种设计模式主要就利用空间换时间得到概念，也就是说异步执行（需要开启一个新的线程）。一般是通过继承`Thread`类或者实现`Runnable`接口来创建多线程，jdk1.5之后，Java提供了`Callable`接口来封装子任务，`Callable`接口可以获取返回结果。

与`FutureTask`相关的类或接口，有`Runnable`，`Callable`，`Future`，直接从`Callable`开始。

#### Callable接口

```java
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
```

#### Future接口

`Future`接口表示异步计算的结果，通过`Future`接口提供的方法，可以很方便的查询异步计算任务是否执行完成，获取异步计算的结果，取消未执行的异步任务，或者中断异步任务的执行，接口定义如下：

```java
public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    V get() throws InterruptedException, ExecutionException;
    V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;
}
```

1. `cancel(boolean mayInterruptIfRunning)`：取消子任务的执行，如果这个子任务已经执行结束，或者已经被取消，或者不能被取消，这个方法就会执行失败并返回`false`；如果子任务还没有开始执行，那么子任务会被取消，不会再被执行；如果子任务已经开始执行了，但是还没有执行结束，根据`mayInterruptIfRunning`的值，如果`mayInterruptIfRunning = true`，那么会中断执行任务的线程，然后返回true，如果参数为false，会返回true，不会中断执行任务的线程。这个方法在`FutureTask`的实现中有很多值得关注的地方，后面再细说。
    需要注意，这个方法执行结束，返回结果之后，再调用`isDone()`会返回true。
2. `isCancelled()`，判断任务是否被取消，如果任务执行结束（正常执行结束和发生异常结束，都算执行结束）前被取消，也就是调用了`cancel()`方法，并且`cancel()`返回true，则该方法返回true，否则返回false.
3. `isDone()`:判断任务是否执行结束，正常执行结束，或者发生异常结束，或者被取消，都属于结束，该方法都会返回true.
4. `V get()`:获取结果，如果这个计算任务还没有执行结束，该调用线程会进入阻塞状态。如果计算任务已经被取消，调用`get()`会抛出`CancellationException`，如果计算过程中抛出异常，该方法会抛出`ExecutionException`，如果当前线程在阻塞等待的时候被中断了，该方法会抛出`InterruptedException`。
5. `V get(long timeout, TimeUnit unit)`：带超时限制的get()，等待超时之后，该方法会抛出`TimeoutException`。

#### FutureTask

`FutureTask`可以像`Runnable`一下，封装异步任务，然后提交给`Thread`或线程池执行，然后获取任务执行结果。原因在于`FutureTask`实现了`RunnableFuture`接口，`RunnableFuture`是什么呢，其实就是`Runnable`和`Callable`的结合，它继承自`Runnable`和`Callable`。

```java
public class FutureTask<V> implements RunnableFuture<V> 

public interface RunnableFuture<V> extends Runnable, Future<V> 
```



### FutureTask使用

1. FutureTask + Thread
上面介绍过，FutureTask有Runnable接口和Callable接口的特征，可以被Thread执行。

```
//step1:封装一个计算任务，实现Callable接口   
class Task implements Callable<Boolean> {
    @Override
    public Boolean call() throws Exception {
        try {
            for (int i = 0; i < 10; i++) {
                Log.d(TAG, "task......." + Thread.currentThread().getName() + "...i = " + i);
                //模拟耗时操作
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, " is interrupted when calculating, will stop...");
            return false; // 注意这里如果不return的话，线程还会继续执行，所以任务超时后在这里处理结果然后返回
        }
        return true;
    }
}

//step2:创建计算任务，作为参数，传入FutureTask
Task task = new Task();
FutureTask futureTask = new FutureTask(task);
//step3:将FutureTask提交给Thread执行
Thread thread1 = new Thread(futureTask);
thread1.setName("task thread 1");
thread1.start();

//step4:获取执行结果，由于get()方法可能会阻塞当前调用线程，如果子任务执行时间不确定，最好在子线程中获取执行结果
try {
    // boolean result = (boolean) futureTask.get();
    boolean result = (boolean) futureTask.get(5, TimeUnit.SECONDS);
    Log.d(TAG, "result:" + result);
} catch (InterruptedException e) {
    Log.e(TAG, "守护线程阻塞被打断...");
    e.printStackTrace();
} catch (ExecutionException e) {
    Log.e(TAG, "执行任务时出错...");
    e.printStackTrace();
} catch (TimeoutException e) {
    Log.e(TAG, "执行超时...");
    futureTask.cancel(true);
    e.printStackTrace();
} catch (CancellationException e) {
    //如果线程已经cancel了，再执行get操作会抛出这个异常
    Log.e(TAG, "future已经cancel了...");
    e.printStackTrace();
}
```

2. Future + ExecutorService

```
//创建任务
Task task = new Task();
//创建线程池，将Callable类型的task提交给线程池执行，通过Future获取子任务的执行结果
ExecutorService executorService = Executors.newCachedThreadPool();
final Future<Boolean> future = executorService.submit(task);
//通过future获取执行结果
boolean result = (boolean) future.get();
```

3.FutureTask + ExecutorService

```
//将FutureTask提交给线程池执行
ExecutorService executorService = Executors.newCachedThreadPool();
executorService.execute(futureTask);
```

### 2. 开发中的问题。

FutureTask使用还是比较简单的，`FutureTask`与`Runnable`，最大的区别有两个，一个是可以获取执行结果，另一个是可以取消，使用方法可以参考以上步骤，不过在项目中使用FutureTask出现了以下两个问题：

1. 有的情况下，使用 `futuretask.cancel(true)`方法并不能真正的结束子任务执行。
2. `FutureTask`的`get(long timeout, TimeUnit unit)`方法，是等待timeout时间后，获取子线程的执行结果，但是如果子任务执行结束了，但是超时时间还没有到，这个方法也会返回结果。

### FutureTask的源码

#### 成员变量

1. **volatile int state** 任务的运行状态

```
 private static final int NEW          = 0;
 private static final int COMPLETING   = 1;
 private static final int NORMAL       = 2;
 private static final int EXCEPTIONAL  = 3;
 private static final int CANCELLED    = 4;
 private static final int INTERRUPTING = 5;
 private static final int INTERRUPTED  = 6;
```

2.  Callable<V> callable，封装了计算任务，可获取计算结果

3. `Object outcome`，用来保存计算任务的返回结果，或者执行过程中抛出的异常。

4. `volatile Thread runner`，指向当前在运行`Callable`任务的线程，

5. `volatile WaitNode waiters`，`WaitNode`是FutureTask的内部类，表示一个阻塞队列

#### 成员函数

```csharp
FutureTask() 初始化state状态为NEW
    
public void run() {
    //1.判断状态是否是NEW，不是NEW，说明任务已经被其他线程执行，甚至执行结束，或者被取消了，直接返回
    //2.调用CAS方法，判断RUNNER为null的话，就将当前线程保存到RUNNER中，设置RUNNER失败，就直接返回
    if (state != NEW ||
            !U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread()))
        return;
    try {
        Callable<V> c = callable;
        if (c != null && state == NEW) {
            V result;
            boolean ran;
            try {
                //3.执行Callable任务，结果保存到result中
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                //3.1 如果执行任务过程中发生异常，将调用setException()设置异常
                result = null;
                ran = false;
                setException(ex);
            }
            //3.2 任务正常执行结束调用set(result)保存结果
            if (ran)
                set(result);
        }
    } finally {
        // runner must be non-null until state is settled to
        // prevent concurrent calls to run()
        //4. 任务执行结束，runner设置为null，表示当前没有线程在执行这个任务了
        runner = null;
        // state must be re-read after nulling runner to prevent
        // leaked interrupts
        //5. 读取状态，判断是否在执行的过程中，被中断了，如果被中断，处理中断
        int s = state;
        if (s >= INTERRUPTING)
            handlePossibleCancellationInterrupt(s);
    }
}

//获取任务的执行结果
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)//任务没有完成就等待
        s = awaitDone(false, 0L);
    return report(s);
}

private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
    long startTime = 0L;    // Special value 0L means not yet parked
    WaitNode q = null;
    boolean queued = false;
    for (;;) {
        //1. 读取状态
        //1.1 如果s > COMPLETING，表示任务已经执行结束，或者发生异常结束了，就不会阻塞，直接返回
        int s = state;
        if (s > COMPLETING) {
            if (q != null)
                q.thread = null;
            return s;
        }
        //1.2 如果s == COMPLETING，表示任务结束(正常/异常)，但是结果还没有保存到outcome字段，当前线程让出执行权，给其他线程先执行
        else if (s == COMPLETING)
            // We may have already promised (via isDone) that we are done
            // so never return empty-handed or throw InterruptedException
            Thread.yield();
        //2. 如果调用get()的线程被中断了，就从等待的线程栈中移除这个等待节点，然后抛出中断异常
        else if (Thread.interrupted()) {
            removeWaiter(q);
            throw new InterruptedException();
        }
        //3. 如果等待节点q=null,就创建一个等待节点
        else if (q == null) {
            if (timed && nanos <= 0L)
                return s;
            q = new WaitNode();
        }
        //4. 如果这个等待节点还没有加入等待队列，就加入队列头
        else if (!queued)
            queued = U.compareAndSwapObject(this, WAITERS,
                    q.next = waiters, q);
        //5. 如果设置了超时等待时间
        else if (timed) {
            //5.1 设置startTime,用于计算超时时间，如果超时时间到了，就等待队列中移除当前节点
            final long parkNanos;
            if (startTime == 0L) { // first time
                startTime = System.nanoTime();
                if (startTime == 0L)
                    startTime = 1L;
                parkNanos = nanos;
            } else {
                long elapsed = System.nanoTime() - startTime;
                if (elapsed >= nanos) {
                    removeWaiter(q);
                    return state;
                }
                parkNanos = nanos - elapsed;
            }
            // nanoTime may be slow; recheck before parking
            //5.2 如果超时时间还没有到，而且任务还没有结束，就阻塞特定时间
            if (state < COMPLETING)
                LockSupport.parkNanos(this, parkNanos);
        }
        //6. 阻塞，等待唤醒
        else
            LockSupport.park(this);
    }
}

//取消任务
public boolean cancel(boolean mayInterruptIfRunning) {
    //1.判断state是否为NEW，如果不是NEW，说明任务已经结束或者被取消了，该方法会执行返回false
    //state=NEW时，判断mayInterruptIfRunning，如果mayInterruptIfRunning=true，说明要中断任务的执行，NEW->INTERRUPTING
    //如果mayInterruptIfRunning=false,不需要中断，状态改为CANCELLED
    if (!(state == NEW &&
            U.compareAndSwapInt(this, STATE, NEW,
                    mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
        return false;
    try {    // in case call to interrupt throws exception
        if (mayInterruptIfRunning) {
            try {
                //2.读取当前正在执行子任务的线程runner,调用t.interrupt()，中断线程执行
                Thread t = runner;
                if (t != null)
                    t.interrupt();
            } finally { // final state
                //3.修改状态为INTERRUPTED
                U.putOrderedInt(this, STATE, INTERRUPTED);
            }
        }
    } finally {
        finishCompletion();
    }
    return true;
}

//
private void finishCompletion() {
    //遍历waiters等待队列
    for (WaitNode q; (q = waiters) != null;) {
        if (U.compareAndSwapObject(this, WAITERS, q, null)) {
            for (;;) {
                Thread t = q.thread;
                if (t != null) {
                    q.thread = null;
                    LockSupport.unpark(t);//调用LockSupport.unpark(t)唤醒等待返回结果的线程
                }
                WaitNode next = q.next;
                if (next == null)
                    break;
                q.next = null; //释放资源
                q = next;
            }
            break;
        }
    }
    done();//空方法，子类可以实现自定义行为
    callable = null;        //释放资源
}
```

**FutureTask执行活动图**:

![](..\images\FutureTask执行活动图.png)



相关文章：

1.https://www.jianshu.com/p/55221d045f39

