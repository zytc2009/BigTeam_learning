### 进程通讯

**SetEvent**：

设置事件的状态为有状态，释放任意等待线程。
1、如果事件是手动的，此事件将保持有状态直到调用ResetEvent，这种情况下将释放多个线程；
2、如果事件是自动的，此事件设置为有状态，直到一个线程调用WaitForSingleObject，系统将设置事件的状态为无状态；
3、如果没有线程在等待，则此事件将保持有标记，直到一个线程被释放。

**SetMessage**:

PostMessage是把WM_XX送到消息队列中，而SendMessage根本不经过消息队列，其实就是直接Call 窗口对应的消息函数,下面是SendMessage的内核代码：

SendMessage(hWnd,message,wParam,lParam)
{
  WNDPROC pPrevWndFunc = GetWindowLong(hWnd,GWL_WNDPROC);
  return CallWndProc(pPrevWndFunc,hWnd,message,wParam,lParam);
}



### 部分win设备抓不到异常的问题

  https://stackoverflow.com/questions/19656946/why-setunhandledexceptionfilter-cannot-capture-some-exception-but-addvectoredexc
  之前的方法不可用了，需要用新的方法
  测试https://app.bugsplat.com/ 后台，主要是收集dmp，如果想看错误信息，需要上传exe和pdb
  分析异常信息需要exe，pdb，dmp三个文件

分析异常信息命令：

```shell
cdb -y myQtCrasher.pdb -i myQtCrasher.exe -z 123.dmp -c ".ecxr;kb;Q" #如果不想结束，可以去掉Q
//可以用windbg替换cdb， 进入图形页面
```



```
 !analyze -v  　　　　　　//找出出错的堆
 .ecxr    　　　　　　　　 //获取异常信息
 .cxr           //获取上下文信息
 !heap    　　　　　　　　 //打印出错函数的局部位置
 !for_each_frame dv /t  //显示call stack内容
 ~*kbn 　　　　　　　　　　//显示所有线程信息
 ~线程号 s      　　　　　//切换线程
 kbn                　　//显示当前线程信息
 .reload            　　//加载符号信息
 !runaway     　　　　//查看19号线程所用时间
 .load wow64exts
 !sw                   //切换到64位
```

其他搜集还没验证：

### 定位UnhandledExceptionFilter崩溃

1.~*kbn查看所有线程信息，找到崩溃的线程(搜索UnhandledExceptionFilter)

```
0f 167fd40c 77f37f1a 167fd43c 77ede304  kernel32!UnhandledExceptionFilter+0xf5  168ff904 77e3eeaa 168ff9c0  7e0be23d kernel32!UnhandledExceptionFilter+0x1f8
 168ff990 77f37f1a  77ede304  kernel32!UnhandledExceptionFilter+0x172
```

2.切换到崩溃线程

```
~ s
```

3.输入dd 167fd43c（标志灰色背景的地址）

```
167fd43c  167fd53c 167fd55c 77f11288
167fd44c  00ca8fa4 167fd474 77f071b9 fffffffe
167fd45c  167fffc4 167fd55c 167fd510 167fe2d8
167fd46c  77f071cd 167fffc4 167fd524 77f0718b
167fd47c  167fd53c 167fffc4 167fd55c 167fd510
167fd48c  77ede0ed  167fd53c 167fffc4
167fd49c  77edf96f 167fd53c 167fffc4 167fd55c
167fd4ac  167fd510 77ede0ed 78b58518 167fd53c
```

4.输入.cxr 167fd55c(第二个参数)查看上下文，然后输入kb查看线程堆栈(输入.exr 167fd53c查看错误信息)

通过以上步骤即可调出崩溃线程堆栈信息，如果想看的更清晰，可以在代码中查看，按照如下步骤。

1.点击Call Stack按钮（或View->Call Stack）查看具体的堆栈信息

2.添加源码（File->Source File Path）,路径之间用分号;分割

3.双击Call Stack中的堆栈信息，会自动定位好源代码中的某一行

4.点击Local按钮可以查看当前堆栈本地变量

### _except_handler函数（SEH异常处理函数）

```
085df400 7c9232a8 085df4ec 085dffdc 085df50c Kernel32!_except_handler3_0x61
```

输入 .exr 085df4ec 获取异常信息

输入 .cxr 085df50c 获取上下文信息

### IndexOutOfRangeException堆越界问题查询

1.找到gflags.exe，默认位置 C:\Program Files (x86)\Windows Kits\10\Debuggers\x86

2.管理员运行cmd到gflags.exe位置，输入

```
Gflag.exe /p /enable Test.exe
```

成功后会显示

```
Warning: pageheap.exe is running inside WOW64.
This scenario can be used to test x86 binaries (running inside WOW64)
but not native (IA64) binaries. path: SOFTWARE\Microsoft\Windows NT\CurrentVersion\Image File Execution Options
    vxofflinetrendquerytool.exe: page heap enabled
```

3.使用WinDbg挂载到进程，崩溃后再命令行界面上有提示信息。

### 调试死锁问题

1.~*kvn/~*kb/~*kbn查看所有线程调用堆栈

2.找到WaitForSingleObject的那一行，记录第三列的数字，为该线程正在等待的句柄(00000300)

```
  Id: 1c38.1cc Suspend:  Teb: 7ffd4000 Unfrozen
 061bfed8 77e2baf3  ffffffff  ntdll!KiFastSystemCallRet
 061bfef0 77e2baa2  ffffffff  kernel32!WaitForSingleObjectEx+0x43
 061bff04 0286cd64  ffffffff 0438f62c kernel32!WaitForSingleObject+0x12
```

3.使用!handle命令查看句柄00000300是什么类型

```
!handle  f
```

看Object Specific Information下的Mutant Owner一列，具体案例参考https://blog.csdn.net/china_jeffery/article/details/78927524

### 调试运行中的进程

```
File->Attach to a Process
File->Open Source File     //打开源文件，F9可以设置断点
```

注意：设置断点之前需要先停掉进程，点击![img](https://img2018.cnblogs.com/blog/1014271/201901/1014271-20190121155757943-134584109.png)，点击![img](https://img2018.cnblogs.com/blog/1014271/201901/1014271-20190121155828967-1504949229.png)/F5继续运行程序(F10单句调试，F11单步跟踪)

### 查看内存

#### 查看普通变量的内存

一个正在运行的程序，若要查看其变量，可以在任务管理器中的进程详细信息找到对应进程，右键创建转储文件(Dump文件)

输入~*kbn查找出所有的进程，找到类似Test!main+0x10c字样的一行，为程序的入口，一般为0号线程，点击行号可以在Command窗口中输入对应的信息（或使用命令.frame 0n11;dv /t /v）

一步一步点击，找到你要看的对象的内存即可

#### 查看静态对象的内存

之后再记录

### 其他

.reload -i命令无差别加载pdb，不匹配的pdb也会加载

lm显示加载的pdb，带有M字样的为不匹配，点开不匹配的模块可以看到模块的真正时间

.reload 正常加载

lm成功匹配的模块后边会有路径显示