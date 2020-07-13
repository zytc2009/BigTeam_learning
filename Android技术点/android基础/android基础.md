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

传递顺序：activity—>ViewGroup-àView

当点击事件产生后首先传递给Activity，调用Activity的dispatchTouchEvent（），在该方法中执行getWindow().superDispatchTouchEvent()即调用了PhoneWindow的事件分发，进而调用了DecorView 的 superDispatchTrackballEvent() 方法也就执行了ViewGroup的dispatchEvent

> Activity => ViewGroup => View 的顺序进行事件分发。
>
> onTouch() 执行总优先于 onClick()。
>
> - dispatchTouchEvent()
> - onInterceptTouchEvent()
> - onTouchEvent()

> https://www.jianshu.com/p/d3758eef1f72