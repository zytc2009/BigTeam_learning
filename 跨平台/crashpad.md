### 1.下载源码

```
$ mkdir crashpad
$ cd crashpad
$ fetch crashpad
#breakpad类似下载
```

终端断了：

```
$ git pull -r
$ gclient sync
```

### 2.编译

```shell
$ cd crashpad/crashpad
$ gn gen out/x64 --args="target_cpu=\"x64\" extra_cflags=\"/MD /GL-\" is_debug=false" --winsdk="10.0.19041.0" --ide="vs2019"
$ ninja -C out/x64

gn gen out\MD --args="extra_cflags=\"/MD /GL-\"" 
gn gen out\MDd --args="extra_cflags=\"/MDd /GL-\""
#编译会有错，但是基础库其实已经生成了，就是有点分散
#查看参数：gn args --list out/x64

out/Default/obj/client/libcommon.a, 
out/Default/obj/client/libclient.a, 
out/Default/obj/util/libutil.a, 
out/Default/obj/third_party/mini_chromium/mini_chromium/base/libbase.a.
```



windows遇到编码问题，在用ninja生成项目文件后，有
rule cc， rule cxx
找到${cflags_c}后，添加`/WX-`，这是为了忽略编码警告。中途可能还会遇到有换行符的问题, 自测没效果

编码问题：utf8 转成 utf8bom 



https://chromium.googlesource.com/crashpad/crashpad/+/refs/heads/main/doc/developing.md

https://docs.bugsplat.com/introduction/getting-started/integrations/cross-platform/qt/



### 流程分析：

#### 1.启动client

```c++
CrashpadClient *client = new CrashpadClient();
bool status = client->StartHandler(handler, reportsDir, metricsDir, url.toStdString(),annotations.toStdMap(), arguments, true, true, attachments);

CrashpadClient::StartHandler():
	CreatePipe()
    RegisterHandlers(); // SetEvent(g_signal_exception); 通知sever， 等待结束
StartHandlerProcess() //启动进程，进程名就是crashpad_handler
```

#### 2. 启动 crashpad_handler

```
HandlerMain():
	InstallCrashHandler();
	CrashReportUploadThread-》start() 
	ExceptionHandlerServer::InitializeWithInheritedDataForInitialClient();//初始化
    	new internal::ClientData //注册异常回调
	ExceptionHandlerServer.Run(exception_handler.get());//启动server
```

#### 3.ExceptionHandlerServer

```
ExceptionHandlerServer::Run:
	CreateNamedPipeInstance()
	//循环检测队列消息，直到遇到异常
	//发消息通知client结束
	//等待client结束，清除client
ExceptionHandlerServer::OnCrashDumpEvent(void* ctx, BOOLEAN):
	client->delegate()->ExceptionHandlerServerException()
		CrashReportExceptionHandler::ExceptionHandlerServerException()
			process_snapshot.Initialize()
			minidump.InitializeFromSnapshot(&process_snapshot);
			minidump.WriteEverything(new_report->Writer()) //生成minidump到文件
			database_->FinishedWritingCrashReport()
			upload_thread_->ReportPending(uuid);
	SafeTerminateProcess(client->process(), exit_code);//结束进程
```

