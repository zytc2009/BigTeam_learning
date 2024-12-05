### Mars相关模块

comm 可以独立使用的公共库，包括 socket、线程、消息队列、协程等；

sdt 网络诊断组件；

stn 信令分发网络模块

xlog 日志模块

third_party：项目依赖的第三方代码

### MarsWrapper：

start()

```c++
mars::stn::SetCallback(mars::stn::StnCallBack::Instance());
mars::app::SetCallback(mars::app::AppCallBack::Instance());
mars::baseevent::OnCreate();

mars::baseevent::OnForeground(true);
mars::stn::MakesureLonglinkConnected();
```

mars::stn::MakesureLonglinkConnected()：

```c++
LongLink::__Run(): 
//channel:default-longlink
//连接socket
__RunConnect(conn_profile);
//死循环
__RunReadWrite():
if (!alarmnoopinterval.IsWaiting()) {//检查是否在waiting
     //检查是否需要空请求
	if (__NoopReq(noop_xlog, alarmnooptimeout, has_late_toomuch)) {
        __NotifySmartHeartbeatHeartReq(xxx);
     }
}
if (nooping && (alarmnooptimeout.Status() == Alarm::kInit || alarmnooptimeout.Status() == Alarm::kCancel)) {
            goto End;//结束
}
//检查发送队列，如果有数据，设置写属性
if (!lstsenddata_.empty()) sel.Write_FD_SET(_sock);
//找一个最合适的socket，最长10分钟
int retsel = sel.Select(10 * 60 * 1000);
//检查是否有异常
//发送数据
//接收数据
recv()；
```

### stn内部：

```
启动即执行：
sdt_logic __initbind_baseprjevent
sdt_logic onCreate
SdtCore SdtCore()
```

### 数据包结构：

```c++
//头部20个字节，LongLinkEncoder::longlink_pack
__STNetMsgXpHeader st = {0};
st.head_length = htonl(sizeof(__STNetMsgXpHeader));
st.client_version = htonl(sg_client_version);
st.cmdid = htonl(_cmdid);
st.seq = htonl(_seq);
st.body_length = htonl(_body.Length());
            
_packed.AllocWrite(sizeof(__STNetMsgXpHeader) + _body.Length());
_packed.Write(&st, sizeof(st));
            
if (NULL != _body.Ptr()) _packed.Write(_body.Ptr(), _body.Length());
```

### 日志打印：

```c++
std::string dataLog = "";
const char* data = (const char*)_packed;
for (size_t i = 0; i < _packed_len; i++) {
     int high = data[i] / 16, low = data[i] % 16;
     dataLog += (high < 10) ? ('0' + high) : ('a' + high - 10);
     dataLog += (low < 10) ? ('0' + low) : ('a' + low - 10);
     dataLog += " ";
}
std::cout << "__unpack_test len:" << _packed_len << ",data:" << dataLog << std::endl;
```

### stn代码

```c++
NetCore __OnLongLinkNetworkError  //单例    
->netcheck_logic_ UpdateLongLinkInfo --> __StartNetCheck()
-> NetSource::GetLongLinkHosts() //静态方法  
->sdt::StartActiveCheck()  //静态方法 检测网络
->SdtCore::StartCheck()   //单例    
```

### NetCore分析

```
主要是NetSource，如果能约束好就可以支持多个通道
NetCore需要支持多实例
```

NetCore::Singleton是个单例，会初始化
 ->__InitLongLink
->更改连接IP和端口信息UpdateLongLinkInfo
->__StartNetCheck->ShouldNetCheck 组合IP和端口组合成 check_ipport_list
->sdt::StartActiveCheck()  插入check_list列表中 // 检测网络
SdtCore::__RunOn->StartDoCheck探测，对象创建执行__RunOn
tcp_send以NonBlock进行写入，for循环串行，noop_send
ReportNetCheckResult 上报checkresult_profiles  上报

### TLS 支持

在mars现有代码的模型中，比较难做TLS握手的扩展，mars团队目前正在考虑修改源码来满足类似TLS这种安全通信协议的扩展。如果你急着要想对mars做TLS扩展的话，需要对mars源码进行修改，这里有一个修改方案：

**（1）**在longlink和shortlink中，在**RunConnect或者__RunReadWrite**函数中，复合连接complexconnect建立好socket连接后，做一次TLS握手操作 

**（2）**在**RunReadWrite**函数中，对目前直接调用系统read()和write()函数进行改下，改成支持TLS加解密的读写函数。

  因为mars已经依赖boost和ssl了，所以比较容易改

### 智能心跳原理

```
LongLinkConnectMonitor，Longlink
```

### 弱网心跳的时间



### 重连间隔时间

```c++
//whb: type
enum {
    kTaskConnect,
    kLongLinkConnect,
    kNetworkChangeConnect,
};

//whb: active state
enum {
    kForgroundOneMinute,
    kForgroundTenMinute,
    kForgroundActive,
    kBackgroundActive,
    kInactive,
};

//whb: interval(s): active state order
static unsigned long const sg_interval[][5]  = {
    {5,  10, 20,  30,  300}, //kTaskConnect
    {15, 30, 240, 300, 600}, //kLongLinkConnect
    {0,  0,  0,   0,   0},  //kNetworkChangeConnect
};
```

```c++
static unsigned long __Interval(int _type, const ActiveLogic& _activelogic) {
    //whb:(s)
    unsigned long interval = sg_interval[_type][__CurActiveState(_activelogic)];
    
    if (kLongLinkConnect != _type) return interval;//非长连接

    if (__CurActiveState(_activelogic) == kInactive || __CurActiveState(_activelogic) == kForgroundActive) {  // now - LastForegroundChangeTime>10min
        if (!_activelogic.IsActive() && GetAccountInfo().username.empty()) {
            interval = kNoAccountInfoInactiveInterval;//7 * 24 * 60 * 60
        } else if (kNoNet == getNetInfo()) {//*3+salt
            interval = interval * kNoNetSaltRate + kNoNetSaltRise;
        } else if (GetAccountInfo().username.empty()) {//*2+salt
            interval = interval * kNoAccountInfoSaltRate + kNoAccountInfoSaltRise;
        } else {
            // default value
			interval += rand() % (20);
        }
    }
    return interval;
}
```



### 多通道支持

```c++
//Test multichannel
mars::stn::LonglinkConfig linkConfig("test_env");
linkConfig.is_keep_alive = true;
linkConfig.host_list.push_back("192.168.201.10");
mars::stn::CreateLonglink_ext(linkConfig);
mars::stn::MakesureLonglinkConnected_ext("test_env");
```