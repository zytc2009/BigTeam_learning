# Handler机制

 Handler是一套消息处理机制，用来更新UI，可以发送消息和也可以通过它处理消息。

总结：

Handler，Message，looper 和 MessageQueue 构成了安卓的消息机制，handler创建后可以通过 sendMessage 将消息加入消息队列，然后 looper不断的将消息从 MessageQueue 中取出来，回调到 Hander 的 handleMessage方法，从而实现线程的通信。

 消息传递：消息发送，消息循环，消息分发
时间不准确，受阻塞等待时间影响，也受前一个消息处理影响

从两种情况来说，第一在UI线程创建Handler,此时我们不需要手动开启looper，因为在应用启动时，在ActivityThread的main方法中就创建了一个当前主线程的looper，并开启了消息队列，消息队列是一个无限循环，为什么无限循环不会ANR?因为可以说，应用的整个生命周期就是运行在这个消息循环中的，安卓是由事件驱动的，Looper.loop不断的接收处理事件，每一个点击触摸或者Activity每一个生命周期都是在Looper.loop的控制之下的，looper.loop一旦结束，应用程序的生命周期也就结束了。我们可以想想什么情况下会发生ANR，第一，事件没有得到处理，第二，事件正在处理，但是没有及时完成，而对事件进行处理的就是looper，所以只能说事件的处理如果阻塞会导致ANR，而不能说looper的无限循环会ANR

 

另一种情况就是在子线程创建Handler,此时由于这个线程中没有默认开启的消息队列，所以我们需要手动调用looper.prepare(),并通过looper.loop开启消息

 

主线程Looper从消息队列读取消息，当读完所有消息时，主线程阻塞。子线程往消息队列发送消息，并且往管道文件写数据，主线程即被唤醒，从管道文件读取数据，主线程被唤醒只是为了读取消息，当消息读取完毕，再次睡眠。因此loop的循环并不会对CPU性能有过多的消耗。

 

**为什么要有Handler？**

答：Android在设计的时候，封装了一套消息创建、传递、处理机制，如果不遵循这样的机制就没办法更新UI信息，就会抛出异常。



**handler使用流程？**

1. 首先几个核心类分别是Handler， looper（消息循环） ，messagequeue（消息队列），Message（消息体）。

2. 每个Handler都有一个与之关联的Looper和对应的MessageQueue

    消息是链表结构，有延时消息，会插入消息。

3. handler调用sendmsg或post方法将message加入与之绑定的looper中的messagequeue中。

   发消息: 加入消息队列-》nativeWake-》nativeMessageQueue->wake -》write(wakeFd)唤醒Looper的wait

4. Looper的loop()中消息循环时，会从消息队列中通过queue.next()取出每个消息msg(如果next()取出的消息为空，则线程阻塞)，然后执行msg.target.dispatchMessage(msg)方法，将消息分发给对应的Handler实例去处理消息

   Loop()取消息， nativePollOnce(ptr, nextTime) -1表示一直等待
   Looper的核心是native looper的pollnner方法中的epoll_wait(fd)方法调用，阻塞等消息

5. 在handler的dispatchMessage方法中根据msg.callBack属性判断，若msg.callback属性不为空，则代表使用了post（Runnable r）发送消息，则直接回调Runnable对象里复写的run（），若msg.callback属性为空，则代表使用了sendMessage（Message msg）发送消息，则回调复写的handleMessage(msg）

 **何时创建Looper和messageQueue**

Looper：创建主线程时，会自动调用ActivityThread的1个静态的main（）；而main（）内则会调用Looper.prepareMainLooper()为主线程生成1个Looper对象，同时也会生成其对应的MessageQueue对象。

子线程中手动使用Looper.prepare创建looper。线程中要使用Handler就必须创建Looper。）

messageQueue：消息队列是在Looper的构造方法中创建的，即创建了Looper也就创建了对应的消息队列

生成Looper & MessageQueue对象后，则会自动进入消息循环：Looper.loop（）

 **Looper如何调用Handler？**

Looper对象中有成员MessageQueue，用来存储消息message。Handler发送的消息会默认持有handler对象（在msg.taget 中存储）。当looper在循环中取到msg时也就拿到了handler 对象。

**Handler是如何获取Looper对象的？**

Looper在创建时将对象存储在ThreadLocal中，并对外提供了静态方法Looper.mylooper获取looper实例。在Handler的构造方法中调用looper.mylooper获取实例。

**子线程Looper.looper()死循环如何跳出？**

在子线程中，如果手动为其创建了Looper，那么在所有的事情完成以后应该调用quit方法来终止消息循环，否则这个子线程就会一直处于等待（阻塞）状态，而如果退出Looper以后，这个线程就会立刻（执行所有方法并）终止，因此建议不需要的时候终止Looper。

**主线程 Looper 死循环为什么不会导致应用卡死？**

> 这里涉及到Linux pipe/epoll机制，简单说就是在主线程的MessageQueue没有消息时，便阻塞在loop的queue.next()中的nativePollOnce()方法里，此时主线程会释放CPU资源进入休眠状态，直到下个消息到达或者有事务发生，通过往pipe管道写端写入数据来唤醒主线程工作。这里采用的epoll机制，是一种IO多路复用机制，可以同时监控多个描述符，当某个描述符就绪(读或写就绪)，则立刻通知相应程序进行读或写操作，本质同步I/O，即读写是阻塞的。 所以说，主线程大多数时候都是处于休眠状态，并不会消耗大量CPU资源。

**主线程的消息循环机制是什么？**

> 事实上，会在进入死循环之前便创建了新binder线程，在代码ActivityThread.main()中：

```cpp
public static void main(String[] args) {
//创建Looper和MessageQueue对象，用于处理主线程的消息
 Looper.prepareMainLooper();

 //创建ActivityThread对象
 ActivityThread thread = new ActivityThread(); 

 //建立Binder通道 (创建新线程)
 thread.attach(false);

 Looper.loop(); //消息循环运行
 throw new RuntimeException("Main thread loop unexpectedly exited");
}
```

> **Activity的生命周期都是依靠主线程的Looper.loop**，当收到不同Message时则采用相应措施：一旦退出消息循环，那么你的程序也就可以退出了。 从消息队列中取消息可能会阻塞，取到消息会做出相应的处理。如果某个消息处理时间过长，就可能会影响UI线程的刷新速率，造成卡顿的现象。
>  thread.attach(false)方法函数中便会创建一个Binder线程（具体是指ApplicationThread，Binder的服务端，用于接收系统服务AMS发送来的事件），该Binder线程通过Handler将Message发送给主线程。「Activity 启动过程」
>
> 比如收到msg=H.LAUNCH_ACTIVITY，则调用ActivityThread.handleLaunchActivity()方法，最终会通过反射机制，创建Activity实例，然后再执行Activity.onCreate()等方法；
>
> 再比如收到msg=H.PAUSE_ACTIVITY，则调用ActivityThread.handlePauseActivity()方法，最终会执行Activity.onPause()等方法。
>
> ActivityThread通过ApplicationThread和AMS进行进程间通讯，AMS以进程间通信的方式完成ActivityThread的请求后会回调ApplicationThread中的Binder方法，然后ApplicationThread会向H发送消息，H收到消息后会将ApplicationThread中的逻辑切换到ActivityThread中去执行，即切换到主线程中去执行，这个过程就是。主线程的消息循环模型

**Handler 是如何能够线程切换**

> **Handler创建的时候会采用当前线程的Looper来构造消息循环系统，Looper在哪个线程创建，就跟哪个线程绑定**，并且Handler是在他关联的Looper对应的线程中处理消息的。
>
> 那么Handler内部如何获取到当前线程的Looper呢—–ThreadLocal。ThreadLocal可以在不同的线程中互不干扰的存储并提供数据，通过ThreadLocal可以轻松获取每个线程的Looper。当然需要注意的是线程是默认没有Looper的，如果需要使用Handler，就必须为线程创建Looper。我们经常提到的主线程，也叫UI线程，它就是ActivityThread，ActivityThread被创建时就会初始化Looper，这也是在主线程中默认可以使用Handler的原因。
>
> 系统为什么不允许在子线程中访问UI？（摘自《Android开发艺术探索》） 这是因为Android的UI控件不是线程安全的，如果在多线程中并发访问可能会导致UI控件处于不可预期的状态，那么为什么系统不对UI控件的访问加上锁机制呢？缺点有两个： 首先加上锁机制会让UI访问的逻辑变得复杂 锁机制会降低UI访问的效率，因为锁机制会阻塞某些线程的执行。 所以最简单且高效的方法就是采用单线程模型来处理UI操作。

**子线程中Toast，showDialog，的方法**

正常是不行的，报错，子线程不能操作ui，改为以下方式测试通过：

```java
new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            Toast.makeText(MainActivity.this, "run on thread", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
}).start();
```

原因是啥呢？肯定是跟handler有关

```
Toast类：
public Toast(@NonNull Context context, @Nullable Looper looper) {
     ...
     mTN = new TN(context.getPackageName(), looper);
}

TN类：
TN(String packageName, @Nullable Looper looper) {
       if (looper == null) {
          // Use Looper.myLooper() if looper is not specified.
           looper = Looper.myLooper();
           if (looper == null) {//查看当前线程的looper
             throw new RuntimeException(
                            "Can't toast on a thread that has not called Looper.prepare()");
            }
       }
       mHandler = new Handler(looper, null) { ...}
}

同理看dialog类：
 Dialog(@NonNull Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
        ...
        mListenersHandler = new ListenersHandler(this);//创建handler，需要Looper初始化
 }
```



**idlehandler**：
适用场景：延时任务；批量任务，只关注结果



 **相关源码**

```
Handler构造函数：

public Handler(Callback callback, boolean async) {
        ...// 仅贴出关键代码
        // 1. 指定Looper对象
        mLooper = Looper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
              "Can't create handler inside thread that has not called Looper.prepare()");
           }
           // Looper.myLooper()作用：获取当前线程的Looper对象；若线程无Looper对象则抛出异常
           // 即 ：若线程中无创建Looper对象，则也无法创建Handler对象
           // 故 若需在子线程中创建Handler对象，则需先创建Looper对象
           // 注：可通过Loop.getMainLooper()可以获得当前进程的主线程的Looper对象

          // 2. 绑定消息队列对象（MessageQueue）

           mQueue = mLooper.mQueue;
           // 获取该Looper对象中保存的消息队列对象（MessageQueue）
           // 至此，保证了handler对象 关联上 Looper对象中MessageQueue

} 

sendmsg发送消息：
/** 
  * 分析2：sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis)
  **/
 public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
    // 1. 获取对应的消息队列对象（MessageQueue）
	MessageQueue queue = mQueue;
	 // 2. 调用了enqueueMessage方法 ->>分析3
	 return enqueueMessage(queue, msg, uptimeMillis);
 }
 
 /** 
 * 分析3：enqueueMessage(queue, msg, uptimeMillis)
 **/
 private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
  	// 1. 将msg.target赋值为this
   	// 即 ：把 当前的Handler实例对象作为msg的target属性
	  msg.target = this;
	// 请回忆起上面说的Looper的loop()中消息循环时，会从消息队列中取出每个消息msg，然后执行msg.target.dispatchMessage(msg)去处理消息
	// 实际上则是将该消息派发给对应的Handler实例        
	// 2. 调用消息队列的enqueueMessage（）
	 // 即：Handler发送的消息，最终是保存到消息队列->>分析4
	return queue.enqueueMessage(msg, uptimeMillis）;
}

Post发送msg：
/** 
  * 源码分析：Handler.post（Runnable r）
  * 定义：属于处理者类（Handler）中的方法
  * 作用：定义UI操作、将Runnable对象封装成消息对象 & 发送 到消息队列中（Message ->> MessageQueue）
  * 注：
  *    a. 相比sendMessage()，post（）最大的不同在于，更新的UI操作可直接在重写的run（）中定义
  *    b. 实际上，Runnable并无创建新线程，而是发送 消息 到消息队列中
  */
  public final boolean post(Runnable r)
 {
	 return  sendMessageDelayed(getPostMessage(r), 0);
	 // getPostMessage(r) 的源码分析->>分析1
	// sendMessageDelayed（）的源码分析 ->>分析2
}
/** 
* 分析1：getPostMessage(r)
* 作用：将传入的Runable对象封装成1个消息对象
 **/
private static Message getPostMessage(Runnable r) {
	 // 1. 创建1个消息对象（Message）
	 Message m = Message.obtain();
	// 注：创建Message对象可用关键字new 或 Message.obtain()
	// 建议：使用Message.obtain()创建，
	// 原因：因为Message内部维护了1个Message池，用于Message的复用，使用obtain（）直接从池内获取，从而避免使用new重新分配内存
	// 2. 将 Runable对象 赋值给消息对象（message）的callback属性
	m.callback = r;
	// 3. 返回该消息对象
	return m;
} // 回到调用原处

/**
 * 分析2：dispatchMessage(msg)
 * 定义：属于处理者类（Handler）中的方法
 * 作用：派发消息到对应的Handler实例 & 根据传入的msg作出对应的操作
 */
public void dispatchMessage(Message msg) {
	// 1. 若msg.callback属性不为空，则代表使用了post（Runnable r）发送消息
	// 则执行handleCallback(msg)，即回调Runnable对象里复写的run（）
	// 上述结论会在讲解使用“post（Runnable r）”方式时讲解
	if (msg.callback != null) {
	 handleCallback(msg);
	} else {
		if (mCallback != null) {
			if (mCallback.handleMessage(msg)) {
				 return;
			}
		}
		  // 2. 若msg.callback属性为空，则代表使用了sendMessage（Message msg）发送消息（即此处需讨论的）
		 // 则执行handleMessage(msg)，即回调复写的handleMessage(msg) ->> 分析3
		handleMessage(msg);
	}
}

/** 
* 分析3：handleMessage(msg)
* 注：该方法 = 空方法，在创建Handler实例时复写 = 自定义消息处理方式
**/
public void handleMessage(Message msg) {  
     ... // 创建Handler实例时复写
}  

Looper的构造方法
private Looper(boolean quitAllowed) {
	mQueue = new MessageQueue(quitAllowed);
	// 1. 创建1个消息队列对象（MessageQueue）
	// 即 当创建1个Looper实例时，会自动创建一个与之配对的消息队列对象（MessageQueue）
	 mRun = true;
	mThread = Thread.currentThread();
}

/** 
  * 源码分析1：Looper.prepare()
  * 作用：为当前线程（子线程） 创建1个循环器对象（Looper），同时也生成了1个消息队列对象（MessageQueue）
  * 注：需在子线程中手动调用该方法
  */
 public static final void prepare() {
	if (sThreadLocal.get() != null) {
		throw new RuntimeException("Only one Looper may be created per thread");
	}
	// 1. 判断sThreadLocal是否为null，否则抛出异常
	//即 Looper.prepare()方法不能被调用两次 = 1个线程中只能对应1个Looper实例
	// 注：sThreadLocal = 1个ThreadLocal对象，用于存储线程的变量
	sThreadLocal.set(new Looper(true));
	// 2. 若为初次Looper.prepare()，则创建Looper对象 & 存放在ThreadLocal变量中
	// 注：Looper对象是存放在Thread线程里的
	// 源码分析Looper的构造方法->>分析a
}

/** 
  * 源码分析： Looper.loop()
  * 作用：消息循环，即从消息队列中获取消息、分发消息到Handler
  * 特别注意：
  *       a. 主线程的消息循环不允许退出，即无限循环
  *       b. 子线程的消息循环允许退出：调用消息队列MessageQueue的quit（）
  */
  public static void loop() {
	 ...// 仅贴出关键代码
	 // 1. 获取当前Looper的消息队列
	 final Looper me = myLooper();
	if (me == null) {
		 throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
	}
	// myLooper()作用：返回sThreadLocal存储的Looper实例；若me为null 则抛出异常
	// 即loop（）执行前必须执行prepare（），从而创建1个Looper实例
	 final MessageQueue queue = me.mQueue;
	// 获取Looper实例中的消息队列对象（MessageQueue）
	// 2. 消息循环（通过for循环）
	for (;;) {
		// 2.1 从消息队列中取出消息
		Message msg = queue.next(); 
		if (msg == null) {
			  return;
		}
		// next()：取出消息队列里的消息
		// 若取出的消息为空，则线程阻塞
		// ->> 分析1 
		// 2.2 派发消息到对应的Handler
		msg.target.dispatchMessage(msg);
		// 把消息Message派发给消息对象msg的target属性
		// target属性实际是1个handler对象
		// ->>分析2
		// 3. 释放消息占据的资源
		msg.recycle();
	}
} 

MessageQueue：

/** 
  * 分析1：queue.next()
  * 定义：属于消息队列类（MessageQueue）中的方法
  * 作用：出队消息，即从 消息队列中 移出该消息
  */
  Message next() {
      ...// 仅贴出关键代码
        // 该参数用于确定消息队列中是否还有消息
       // 从而决定消息队列应处于出队消息状态 or 等待状态
      int nextPollTimeoutMillis = 0;
	 for (;;) {
		if (nextPollTimeoutMillis != 0) {
			Binder.flushPendingCommands();
		}
		// nativePollOnce方法在native层，若是nextPollTimeoutMillis为-1，此时消息队列处于等待状态　
		nativePollOnce(ptr, nextPollTimeoutMillis);
		synchronized (this) {
			final long now = SystemClock.uptimeMillis();
			Message prevMsg = null;
			Message msg = mMessages;
			// 出队消息，即 从消息队列中取出消息：按创建Message对象的时间顺序
			if (msg != null) {
				if (now < msg.when) {
                   nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                } else {
                   // 取出了消息
                  mBlocked = false;
                  if (prevMsg != null) {
                     prevMsg.next = msg.next;
                  } else {
                      mMessages = msg.next;
                  }
                 msg.next = null;
                if (DEBUG) Log.v(TAG, "Returning message: " + msg);
	                msg.markInUse();
                    return msg;
                }
            } else {
                // 若 消息队列中已无消息，则将nextPollTimeoutMillis参数设为-1
               // 下次循环时，消息队列则处于等待状态
               nextPollTimeoutMillis = -1;
            } 
           ......
        }
         .....
    }

}// 回到分析原处
```

 

/** 

  * 源码分析2：Looper.prepareMainLooper()

  * 作用：为 主线程（UI线程） 创建1个循环器对象（Looper），同时也生成了1个消息队列对象（MessageQueue）

  * 注：该方法在主线程（UI线程）创建时自动调用，即 主线程的Looper对象自动生成，不需手动生成

  */

​    // 在Android应用进程启动时，会默认创建1个主线程（ActivityThread，也叫UI线程）

​    // 创建时，会自动调用ActivityThread的1个静态的main（）方法 = 应用程序的入口

​    // main（）内则会调用Looper.prepareMainLooper()为主线程生成1个Looper对象      

```
 /** 
   * 源码分析：main（）
    **/
 public static void main(String[] args) {
            ... // 仅贴出关键代码 
        Looper.prepareMainLooper(); 
          // 1. 为主线程创建1个Looper对象，同时生成1个消息队列对象（MessageQueue）
          // 方法逻辑类似Looper.prepare()
          // 注：prepare()：为子线程中创建1个Looper对象  
          ActivityThread thread = new ActivityThread(); 
          // 2. 创建主线程 
         Looper.loop(); 
          // 3. 自动开启 消息循环 ->>下面将详细分析 
}
```

