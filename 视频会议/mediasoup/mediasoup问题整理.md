1.注意类型

 CreateSendTransport， CreateRecvTransport等一些主要方法的参数，如果是json格式，你不需要传递就传nullptr或者{}

 有些参数会校验内容，需要参数是齐备的

2.注意listener

 CreateSendTransport，  CreateRecvTransport 接口需要注入listener，如果你传递nullptr，接口不报错，因为接口没有校验参数

**但是**： SendTransport ->Produce 的时候，会用到创建Transport的时候传递的listener，而且不判空，直接用了，会造成异常  

```c++
//异常产生的位置
SendTransport::Produce()  --> SendHandler->Send() --> Handler::SetupTransport() --> 
privateListener->OnConnect()
```

3.