|  修改日期  | 修改者 | 修改描述 |
| :--------: | :----: | :------: |
| 2020-05-23 | 王洪宾 |          |



### H5和原生的交互

​    1）WebView 提供了Js 和 原生相互调用的接口

​    2）DSBridge ：

​         跨平台，支持Android，ios，前端

​         双向调用，js可以调用native, native可以调用js;

​         支持同步／异步调用  但是不建议使用同步，除非代码极其简单，我在项目中只有命名空间区分使用的这个



### DsBridge使用

#####   android注册js Api：

​    java 代码注册，js 调用的函数都封装在 JsApi 这个对象中，注意用的是DWebView 的 addJavascriptObject，不是原生WebView的addJavascriptInterface 方法.   

```
//注册对象，DsBridge内部按命名空间管理
DWebView.addJavascriptObject(new JsApi(), namespace);

//DWebView的方法
private Map<String, Object> javaScriptNamespaceInterfaces = new HashMap();
public void addJavascriptObject(Object object, String namespace) {
    if (namespace == null) {
       namespace = "";
    }
    if (object != null) {
    	//对象按命名空间管理起来，便于查找，可以看后面的DSBridge实现原理分析
       this.javaScriptNamespaceInterfaces.put(namespace, object);
    }
}

//java对象
public class JsApi{
    //同步API
    @JavascriptInterface
    public String testSyn(Object msg)  {
        return msg + "［syn call］";
    }

    //异步API
    @JavascriptInterface
    public void testAsyn(Object msg, CompletionHandler<String> handler) {
        handler.complete(msg+" [ asyn call]");
    }
}
```

可以看到，DSBridge是通过命名空间的方式集中、统一地管理API。由于安全原因，所有Java API 必须有"@JavascriptInterface" 标注。  



#####  web调用native方法：

```
//获取javascript bridge对象
var dsBridge=require("dsbridge")

//调用Native api
如果添加一个Java API object到DWebView为它指定了命名空间。在 javascript 中就可以通过bridge.call("namespace.api",...)来调用Java API object中的原生API了。

如果命名空间是空(null或空字符串）, 那么这个添加的 Java API object就没有命名空间。在 javascript 通过 bridge.call("api",...)调用。

//同步调用
var str=dsBridge.call("namespace.testSyn","msg");
var str=dsBridge.call("testSyn","msg");

//异步调用
dsBridge.call("namespace.testAsyn","msg", function (v) {
  alert(v);
})
dsBridge.call("testAsyn","msg", function (v) {
  alert(v);
})

//注册 javascript API，供java直接调用 
 dsBridge.register('addValue',function(l,r){
     return l+r;
 })
```

​	dsBridge.call(method,[args,callback])

​	method: api函数名

​	args:参数，类型：json, 可选参数

​	callback(String returnValue):仅调用异步api时需要.



##### 在native中也直接调用 Javascript API

```
dwebView.callHandler("addValue",new Object[]{3,4},new OnReturnValue<Integer>(){
     @Override
     public void onValue(Integer retValue) {
        Log.d("jsbridge","call succeed,return value is "+retValue);
     }
});
```

#####  

### DSBridge 实现原理分析

DSBridge-Android一共三个java 文件，DWebView.java，CompletionHandler.java，OnReturnValue.java

DWebView 类 继承自 WebView 

```
在DWebView 的构造函数中调用init()，完成一些WebView 的设置,init中最重要的事，注对象，这是webview的通用方法
addJavascriptInterface(this.innerJavascriptInterface, "_dsbridge");
```

我们看一下innerJavascriptInterface对象实现，有两个方法PrintDebugInfo和call，PrintDebugInfo只是打印调试信息，我们只看call方法就行，下面只列出了关键代码

```
@JavascriptInterface
public String call(String methodName, String argStr) {
	//方法解析， 解析成命名空间和方法名
    String[] nameStr = parseNamespace(methodName.trim());
    methodName = nameStr[1];
    Object jsb = javaScriptNamespaceInterfaces.get(nameStr[0]);
    ...
    //解析参数argStr
     JSONObject args = new JSONObject(argStr);
     if (args.has("_dscbstub")) {
           callback = args.getString("_dscbstub");
     }
     if(args.has("data")) {
        arg = args.get("data");
     }
     ...
      try {
           //通过反射，判断是异步
            method = cls.getMethod(methodName, Object.class, CompletionHandler.class);
            asyn = true;
        } catch (Exception var16) {
             //参数解析异常，说明是同步方法，按同步处理
             method = cls.getMethod(methodName, Object.class);
		}
	 ...
	 //判断用户的方法是否有JavascriptInterface声明
     JavascriptInterface annotation = (JavascriptInterface)method.getAnnotation(JavascriptInterface.class);
     if (annotation == null) {
         //如果没有声明，则报错，打印错误信息，并返回给js
         this.PrintDebugInfo(error);
         return ret.toString();       
     }
	 ...
	 //调用用户注册的对象的方法，见签名介绍的：android注册js Api
	 method.invoke(jsb, arg, new CompletionHandler() {
         private void complete(Object retValue, boolean complete) {
             ...
             //等用户回调后，传递结果给js
         	 DWebView.this.evaluateJavascript(script);
         	 ...
         }
     });         
}
```

通过上面的分析，我们可以看出来，DsBridge其实是做了一层封装，从整体上来讲还是webview的通用的方式，

使用上，统一封装了交互，保证了多端统一

所以，虽然用户注册多个监听对象，但是对于webview来讲，只是注册了一个对象，当js调用的时候，DWebview再分发信息。

如果你能明白上面所说的，就可以自己写个DSBridge了，如果你有什么更好的建议，欢迎和我交流！

