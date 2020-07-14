[Toc]

[android基础知识](android基础知识点.md)

[SharedPreferences深度学习](SharedPreference分享/SharedPrefenrence深度学习.md)

[图片的Matrix使用](Matrix使用/图片的Matrix使用.md)

[svg矢量动画调研](svg矢量动画调研.md)

[handler](handler.md)

[activity展示和view绘制原理](组件展示流程.md)

[recyclerView原理，和listView对比](recyclerView和listView对比.md)

#### 写一个死锁，死锁是怎样产生的，怎样防止死锁

死锁的四个必要条件：互斥，请求与保持，不可剥夺，循环等待
避免：检查是否会死锁，再分配；进程有限时间内得到需要的所有资源；
预防：根据必要条件后三条去预防

#### View的touch事件

    dispatchPointerEvent
     ->dispatchTouchEvent，onInterceptTouchEvent,onTouch（如果listener不为空） ->onTouchEvent
     ->dispatchGericMotionEvent->dispatchGericPointerEvent
    						->dispatchGericFocusedEvent
    					   	—>dispatchGericMotionInternal

#### 事件分发

事件传递分三个阶段：

 分发（Dispatch），拦截（intercept），消费（Consume）

拥有事件传递的类有三种：

activity，ViewGroup，View

其中Activtiy和View拥有dispatchTouchEvent和onTouchEvent

ViewGroup拥有dispatchTouchEvent和onTouchEvent，onInterceptTouchEvent

传递顺序：activity—>ViewGroup-View

当点击事件产生后首先传递给Activity，调用Activity的dispatchTouchEvent（），在该方法中执行getWindow().superDispatchTouchEvent()即调用了PhoneWindow的事件分发，进而调用了DecorView 的 superDispatchTrackballEvent() 方法也就执行了ViewGroup的dispatchEvent

> Activity => ViewGroup => View 的顺序进行事件分发。
>
> onTouch() 执行总优先于 onClick()。
>
> - dispatchTouchEvent()
> - onInterceptTouchEvent()
> - onTouchEvent()

**ViewGroup:**

![image-20200714125806198](..\images\touchevent传递.png)

**View：**

![image-20200714130001214](..\images\view_touchevent传递.png)

**KeyEvent事件传递**

![image-20200714130100422](..\images\keyevent传递.png)

**流程图涉及的主要方法和类：**

1. (PhoneWindow$)DecorView  ->  dispatchKeyEvent()
2. Activity                 ->  dispatchKeyEvent()
3. ViewGroup                ->  dispatchKeyEvent()
4. View                     ->  dispatchKeyEvent()
5. KeyEvent                 ->  dispatch()
6. View                     ->  onKeyDown/Up()

**处理流程：**

KeyEvent 事件的处理只有两个地方，一个是 Activity，另一个则是具体的 View。ViewGroup 只负责分发，不会消耗事件。同 TouchEvent 一样，返回 true 表示事件已消耗掉，返回 false 则表示事件还在。
当 KeyEvent 事件分到到具体的子 View 的 dispatchKeyEvent() 里时，View 会先去看下有没有设置 OnKeyListener 监听器，有则回调 OnKeyListener.onKey() 方法来处理事件。 

如果 View 没有设置 OnKeyListener 或者 onKey() 返回 false 时，View 会通过调用 KeyEvent 的 dispatch() 方法来回调 View 自己的 onKeyDown/Up() 来处理事件。 

如果没有重写 View 的 onKeyUp 方法，而且事件是 ok（确认）按键的 Action_Up 事件时，View 会再去检查看是否有设置 OnClickListener 监听器，有则调用 OnClickListener.onClick() 来消费事件，注意是消费，也就是说如果有对 View 设置 OnClickListener 监听器的话，而且事件没有在上面两个步骤中消费掉的话，那么就一定会在 onClick() 中被消耗掉，OnClickListener.onClick() 虽然并没有 boolean 返回值，但是 View 在内部 dispatchKeyEvent() 里分发事件给 onClick 时已经默认返回 true 表示事件被消耗掉了。 

如果 View 没有处理事件，也就是没有设置 OnKeyListener 也没有设置 OnClickListener，而且 onKeyDown/Up() 返回的是 false 时，将会通过分发事件的原路返回告知 Activity 当前事件还未被消耗，Activity 接收到 ViewGroup 返回的 false 消息时就会去通过 KeyEvent 的 dispatch() 来调用 Activity 自己的 onKeyDown/Up() 事件，将事件交给 Activity 自己处理。这就是我们常见的在 Activity 里重写 onKeyDown/Up() 来处理点击事件，但注意，这里的处理是最后才会接收到的，所以很有可能事件在到达这里之前就被消耗掉了。