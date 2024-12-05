下载地址：

https://github.com/google/breakpad 或者

 [download depot_tools](http://dev.chromium.org/developers/how-tos/install-depot-tools) and ensure that they’re in your `PATH`.

```
mkdir breakpad && cd breakpad
fetch breakpad
cd src
```

下载gyp，进入gyp 目录，输入命令 setup.py install ，完成gyp的安装，依赖的Python2.7， 安装完会添加到Script目录（gyp.exe），加入系统path

进入breakpad的src目录：

```
gyp –-no-circular-check “.\client\windows\breakpad_client.gyp”
```



