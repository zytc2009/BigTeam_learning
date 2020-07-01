OkHttp使用：

```
//同步请求
Response response = new OkHttpClient().newCall(request).execute();
//异步请求
new OkHttpClient().newCall(request).enqueue(new Callback);
```

我们可以看到，第一步是创建RealCall对象，然后才是执行或放入队列

```
@Override 
public Call newCall(Request request) {
    return RealCall.newRealCall(this, request, false /* for web socket */);
}
```

然后看RealCall类中的execute方法和

```
  @Override public Response execute() throws IOException {
    synchronized (this) {//只能执行一次
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    eventListener.callStart(this);//事件回调
    try {
      client.dispatcher().executed(this);//把当前请求加入正在处理的队列
      Response result = getResponseWithInterceptorChain();//创建RealInterceptorChain处理当前请求
      if (result == null) throw new IOException("Canceled");
      return result;
    } catch (IOException e) {
      eventListener.callFailed(this, e);//事件回调
      throw e;
    } finally {
      client.dispatcher().finished(this);
    }
  }
  
 @Override public void enqueue(Callback responseCallback) {
 	synchronized (this) {//一个请求只能添加一次
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    eventListener.callStart(this);//事件回调
    client.dispatcher().enqueue(new AsyncCall(responseCallback));//把当前请求加入待处理的队列
  }
```

#### 责任链模式

然后我们看一下getResponseWithInterceptorChain()

```
 final Request client; //通过newCall方法传过来
 final Request originalRequest; //通过newCall方法传过来
 
Response getResponseWithInterceptorChain() throws IOException {
    // Build a full stack of interceptors.
    List<Interceptor> interceptors = new ArrayList<>();
    interceptors.addAll(client.interceptors());
    interceptors.add(retryAndFollowUpInterceptor);
    interceptors.add(new BridgeInterceptor(client.cookieJar()));
    interceptors.add(new CacheInterceptor(client.internalCache()));
    interceptors.add(new ConnectInterceptor(client));
    if (!forWebSocket) {
      interceptors.addAll(client.networkInterceptors());
    }
    interceptors.add(new CallServerInterceptor(forWebSocket));

    Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
        originalRequest, this, eventListener, client.connectTimeoutMillis(),
        client.readTimeoutMillis(), client.writeTimeoutMillis());
    return chain.proceed(originalRequest);
}
```

将客户端自定义的拦截器和Okhttp内置的拦截器放到一个List集合interceptors里面，然后跟最初构建的Request对象一块创建了RealInterceptorChain对象，RealInterceptorChain是Chain接口的实现类。这样就把
一系列拦截器组合成一个链跟请求绑定起来，最终调用RealInterceptorChain的proceed来返回一个Response；
简单用图来表示一下此时这个链的关系，可以如下表示：(下图是网上找的，详细链接看文章末尾的相关文章)
![okhttp_interceptor_chain](..\images\okhttp_interceptor_chain.png)

从上图可以看出Okhttp添加拦截器有两个位置：
1、调用OkhttpClient对象addInterceptor方法添加的拦截器集合，会添加到拦截器链的顶部位置。
2、调用OkhttpClient对象addNetwordInterceptor方法添加的拦截器集合，会将这些拦截器插入到ConnectInterceptor和CallServiceInterceptor两个拦截器的中间。

这两种自定义拦截器的区别就是：通过addInterceptor添加的拦截器可以不需要调用proceed方法。
而addNetwordInterceptor的拦截器则必须拦截器链的procceed方法，以确保CallServerInterceptor拦截器的执行。

那么怎么样让 这些拦截器对象逐一运行呢？getResponseWithInterceptorChain方法的最后调用了RealInterceptorChain的proceed方法，该方法直接调用了该对象的重载方法：

```
//正式开始调用拦截器工作  
public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec,RealConnection connection){  
    //省略部分与本文无关的代码

    // 调用链中的下一个拦截器
    RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
        connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
        writeTimeout);
    Interceptor interceptor = interceptors.get(index);
    Response response = interceptor.intercept(next);

   //确保每个拦截器都调用了proceed方法（）
    if (httpCodec != null && index + 1 < interceptors.size() && next.calls != 1) {
      throw new IllegalStateException("network interceptor " + interceptor
          + " must call proceed() exactly once");
    }
   //省略部分与本文无关的代码
    return response;
  }
```

procced方法并没有用for循环来遍历interceptors集合，而是重新new 一个RealInterceptorChain对象，且新对象的index在原来RealInterceptorChain对象index之上进行index+1,并把新的拦截器链对象RealInterceptorChain交给当前拦截器Interceptor 的intercept方法。

然后我们可以看所有拦截器的实现，除了CallServiceInterceptor外，其他都调用了RealInterceptorChain的procced方法，从而保证每个拦截器的都被执行过, 由此体现了责任链模式，当然你可以选择先调用再处理，也可以选择先处理再调用，取决于你的需求。(下图是网上找的，详细链接看文章末尾的相关文章)

![okhttp_intercept_proceed](..\images\okhttp_intercept_proceed.jpg)

### BridgeInterceptor简单分析

该拦截器是链接客户端代码和网络代码的桥梁，它首先将客户端构建的Request对象信息构建成真正的网络请求;然后发起网络请求，最后就是讲服务器返回的消息封装成一个Response对象。

### CacheInterceptor简单分析

> 请求头信息带上上次请求获取的Last-Modified时间，key值If-Modified-Since，请求/响应的头信息里面还有两个Header:If-None-Match/Etag。当第一次请求的时候，”响应头信息”里面有一个Etag的Header可以看做是资源的标识符。再次请求的时候，”请求头信息”会包含一个If-None-Match的头信息。此时服务器取得If-None-Match后会和资源的Etag进行比如，如果相同则说明资源没改动过，那么响应304，客户端可以使用缓存；否则返回200，并且将报文的主题返回给客户端（Etag的说明可以参考百度百科)
>
> intercept方法主要做的事：
>
> 1、如果在初始化OkhttpClient的时候配置缓存，则从缓存中取caceResponse
> 2、将当前请求request和caceResponse 构建一个CacheStrategy对象
> 3、CacheStrategy这个策略对象将根据相关规则来决定caceResponse和Request是否有效，如果无效则分别将caceResponse和request设置为null
> 4、经过CacheStrategy的处理(步骤3），如果request和caceResponse都置空，直接返回一个状态码为504，且body为Util.EMPTY_RESPONSE的空Respone对象
> 5、经过CacheStrategy的处理(步骤3），resquest 为null而cacheResponse不为null，则直接返回cacheResponse对象
> 6、执行下一个拦截器发起网路请求，
> 7、如果服务器资源没有过期（状态码304）且存在缓存，则返回缓存
> 8、将网络返回的最新的资源（networkResponse）缓存到本地，然后返回networkResponse.
> https://blog.csdn.net/chunqiuwei/java/article/details/73224494

### ConnectInterceptor简单分析

```
  @Override public Response intercept(Chain chain) throws IOException {
    ...
    StreamAllocation streamAllocation = realChain.streamAllocation();
    ...
    HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
    RealConnection connection = streamAllocation.connection();

    return realChain.proceed(request, streamAllocation, httpCodec, connection);
  }
```

看上文，我们知道realChain对象是在getResponseWithInterceptorChain方法创建的，创建的时候没有创建StreamAllocation，那么它是在哪里创建的？查找责任链，可以看到在ConnectInterceptor之前只有RetryAndFollowUpInterceptor，于是很快找到**RetryAndFollowUpInterceptor**的intercept方法：

```
private StreamAllocation streamAllocation;
Response intercept(Chain chain) throws IOException {
    ...
    //初始化streamAllocation对象
    StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
        createAddress(request.url()), call, eventListener, callStackTrace);
    ...
    //执行procced方法，将streamAllocation对象传给拦截器链
    response = realChain.proceed(request, streamAllocation, null, null);
}
```

#### StreamAllocation

作用：官方注释是用来协调`Connections`、`Streams`和`Calls`这三个实体的。

HTTP通信 执行 网络请求`Call`  需要在 连接`Connection` 上建立一个新的 流`Stream`，我们将 `StreamAllocation` 称之流的桥梁，它负责为一次 **请求** 寻找 **连接** 并建立 **流**，从而完成远程通信。

```
 private RealConnection findHealthyConnection(。。。) {
    while (true) {//while循环
       //调用findConnection持续获取RealConnection对象
      RealConnection candidate = findConnection(。。。);

      synchronized (connectionPool) {
        //直接返回之
        if (candidate.successCount == 0) {
          return candidate;
        }
      }
      
      //对链接池中不健康的链接做销毁处理, 注：不健康条件：socket没有关闭，输入流没有关闭，输出流没有关闭，http2时连接没有关闭
      if (!candidate.isHealthy(doExtensiveHealthChecks)) {
        noNewStreams();
        continue;
      }
      //返回
      return candidate;
    }//end while
}

private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
      int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
      ...
    synchronized (connectionPool) {
      ...
      // 尝试使用当前的connection,需要注意这个连接可能已限制只能创建新的流
      releasedConnection = this.connection;
      toClose = releaseIfNoNewStreams();//如果有限制，尝试释放资源
      if (this.connection != null) {
        // We had an already-allocated connection and it's good.
        result = this.connection;
        releasedConnection = null;
      }
      if (!reportedAcquired) {
        // If the connection was never reported acquired, don't report it as released!
        releasedConnection = null;
      }

      if (result == null) {
        // 查找 连接池
        Internal.instance.get(connectionPool, address, this, null);
        if (connection != null) {
          foundPooledConnection = true;
          result = connection;
        } else {
          selectedRoute = route;
        }
      }
    }
    ...
    if (result != null) {//如果已经分配或池子里已存在连接，完成
      return result;
    }

    //查看其它路由. 这是阻塞操作.
    boolean newRouteSelection = false;
    if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
      newRouteSelection = true;
      routeSelection = routeSelector.next();
    }
    
    synchronized (connectionPool) {
      ...
      if (newRouteSelection) {
        //有其他路由则先去连接池查找是否有对应的连接 
      }

      if (!foundPooledConnection) {
        。。。
        //没有的话新建一个连接
        result = new RealConnection(connectionPool, selectedRoute);
        acquire(result, false);//存下来
      }
    }

    //如果连接池中已存在，返回
    if (foundPooledConnection) {
      eventListener.connectionAcquired(call, result);
      return result;
    }

    //开始TCP + TLS handshakes. This is a blocking operation.
    result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
        connectionRetryEnabled, call, eventListener);
    routeDatabase().connected(result.route());

    Socket socket = null;
    synchronized (connectionPool) {
      reportedAcquired = true;
      //放入连接池.
      Internal.instance.put(connectionPool, result);      
      // 如果另一个相同地址的复用链接同时被创建了，释放当前连接，用同一个
      if (result.isMultiplexed()) {
        socket = Internal.instance.deduplicate(connectionPool, address, this);
        result = connection;
      }
    }
    。。。
    return result;
  }
```

### RealConnection建立链接简单分析

```
//主要的方法：
public void connect(int connectTimeout, int readTimeout, int writeTimeout,
      int pingIntervalMillis, boolean connectionRetryEnabled, Call call,
      EventListener eventListener) {
      //如果连接已存在，抛出一个异常
    if (protocol != null) throw new IllegalStateException("already connected");
    ...省略部分代码...   
    while (true) {
        ////如果是https请求并且使用了http代理服务器
        if (route.requiresTunnel()) {
          //打开隧道连接
          connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
        } else {
          //打开socket连接
          connectSocket(connectTimeout, readTimeout, call, eventListener);
        }
        //建立协议，如果支持http2.0,新建
        establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
        break;
    }
    。。。
    if (http2Connection != null) {
      synchronized (connectionPool) {
        allocationLimit = http2Connection.maxConcurrentStreams();
      }
    }
  }
  
  private void connectSocket(int connectTimeout, int readTimeout, Call call,
      EventListener eventListener) throws IOException {
    Proxy proxy = route.proxy();
    Address address = route.address();
    //创建socket   
    rawSocket = proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP
        ? address.socketFactory().createSocket() 
        : new Socket(proxy);//使用SOCKS的代理服务器

    rawSocket.setSoTimeout(readTimeout);

    //打开socket链接
      Platform.get().connectSocket(rawSocket, route.socketAddress(), connectTimeout);
    //对输入和输出做处理 
    source = Okio.buffer(Okio.source(rawSocket));
    sink = Okio.buffer(Okio.sink(rawSocket));
  }

//判断连接是否可用，连接池调用
public boolean isEligible(Address address, @Nullable Route route) {
     //判断连接是否可以增加流，主要判断分配的流数是否超了，allocationLimit=1，只有Http2.0才可能为4
     if (allocations.size() >= allocationLimit || noNewStreams) return false;
     //判断是否host，代理，hostnameVerifier，证书是否一致
}
```

隧道技术（Tunneling）是HTTP的用法之一，使用隧道传递的数据（或负载）可以是不同协议的数据帧或包，或者简单的来说隧道就是利用一种网络协议来传输另一种网络协议的数据。

HTTP提供了一个CONNECT方法 ,它是HTTP/1.1协议中预留给能够将连接改为管道方式的代理服务器，该方法就是用来建议一条web隧道。客户端发送一个CONNECT请求给隧道网关请求打开一条TCP链接，当隧道打通之后，客户端通过HTTP隧道发送的所有数据会转发给TCP链接，服务器响应的所有数据会通过隧道发给客户端

```
private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call,
      EventListener eventListener) throws IOException {
    Request tunnelRequest = createTunnelRequest();
    HttpUrl url = tunnelRequest.url();
    //约束连接次数
    for (int i = 0; i < MAX_TUNNEL_ATTEMPTS; i++) {
      connectSocket(connectTimeout, readTimeout, call, eventListener);
      //请求开启隧道并返回tunnelRequest 
      tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);

      if (tunnelRequest == null) break; // Tunnel successfully created.

      //隧道未开启成功，关闭相关资源，继续循环 
      closeQuietly(rawSocket);
      rawSocket = null;
      sink = null;
      source = null;
      eventListener.connectEnd(call, route.socketAddress(), route.proxy(), null);
    }
}
private Request createTunnelRequest() {
    return new Request.Builder()
        .url(route.address().url())
        .header("Host", Util.hostHeader(route.address().url(), true))//解决Http/1.0缺少主机信息（主机名和端口号）导致虚拟服务器不可用的问题
        .header("Proxy-Connection", "Keep-Alive") //主要是解决（不支持Keep-alive首部的）代理服务器盲目转发Keep-alive给服务器，造成客户端挂起的问题（详细解释可参考《HTTP权威指南》第4章）
        .header("User-Agent", Version.userAgent())
        .build();
}

private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest,
      HttpUrl url) throws IOException {
    // Make an SSL Tunnel on the first message pair of each SSL + proxy connection.
    String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
    while (true) {
      Http1Codec tunnelConnection = new Http1Codec(null, null, source, sink);
      ...
      //发送请求打开隧道连接
      tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
      tunnelConnection.finishRequest();
      Response response = tunnelConnection.readResponseHeaders(false)
          .request(tunnelRequest)
          .build();
      ...
      switch (response.code()) {
        case HTTP_OK:
          return null;
        case HTTP_PROXY_AUTH:
          //代理认证
          tunnelRequest = route.address().proxyAuthenticator().authenticate(route, response);
          if (tunnelRequest == null) throw new IOException("Failed to authenticate with proxy");
          //认证通过，但是响应要求close，此时客户端无法在此连接发送数据
          if ("close".equalsIgnoreCase(response.header("Connection"))) {
            return tunnelRequest;
          }
          break;
          ...
      }
    }
  }
```

### OkHttp对2.0的支持

```
private void establishProtocol(ConnectionSpecSelector connectionSpecSelector,
      int pingIntervalMillis, Call call, EventListener eventListener) throws IOException {
    if (route.address().sslSocketFactory() == null) {//不是https请求即普通的http请求，那么协议定义为http/1.1
      protocol = Protocol.HTTP_1_1;
      return;
    }
    //使用SSLSocket，同时会赋值protocol和socket，主要用来判断服务器是否支持http2.0
    connectTls(connectionSpecSelector);
    
    if (protocol == Protocol.HTTP_2) {
      socket.setSoTimeout(0); // HTTP/2 connection timeouts are set per-stream.
      http2Connection = new Http2Connection.Builder(true)
          .socket(socket, route.address().url().host(), source, sink)
          .listener(this)
          .pingIntervalMillis(pingIntervalMillis)
          .build();
      http2Connection.start();
    }
}
```

Http2.0特点

**二进制分帧层**，是HTTP 2.0性能增强的核心，改变了客户端与服务器之间交互数据的方式，将传输的信息（Header、Body等）分割为更小的消息和帧，并采用二进制格式的编码。
**并行请求与响应**，客户端及服务器可以把HTTP消息分解为互不依赖的帧，然后乱序发送，最后再在另一端把这些消息组合起来。
**请求优先级**（0表示最高优先级、2^31 -1表示最低优先级），每个流可以携带一个优先值，有了这个优先值，客户端及服务器就可以在处理不同的流时采取不同的策略，以最优的方式发送流、消息和帧。但优先级的处理需要慎重，否则有可能会引入队首阻塞问题。
**单TCP连接**，HTTP 2.0可以让所有数据流共用一个连接，从而更有效的使用TCP连接
**流量控制**，控制每个流占用的资源，与TCP的流量控制实现是一模一样的。
**服务器推送**，HTTP 2.0可以对一个客户端请求发送多个响应，即除了最初请求响应外，服务器还可以额外的向客户端推送资源，而无需客户端明确地请求。
**首部（Header）压缩**，HTTP 2.0会在客户端及服务器使用“首部表”来跟踪和存储之前发送的键-值对，对于相同的数据，不会再通过每次请求和响应发送。首部表在连接存续期间始终存在，由客户端及服务器共同渐进的更新。每个新的首部键-值对要么追加到当前表的末尾，要么替换表中的值。

问题：虽然消除了`HTTP`队首阻塞现象，但`TCP`层次上仍然存在队首阻塞现象



**Http2Connection代码分析**

```
void start(boolean sendConnectionPreface) throws IOException {
    if (sendConnectionPreface) {
      //发送链接序言
      writer.connectionPreface();
      //发送setting帧
      writer.settings(okHttpSettings);
     。。。
    }
    new Thread(readerRunnable).start(); // Not a daemon thread.
}
```

**ReaderRunnable中的代码**

```
 @Override protected void execute() {
        ...
        reader.readConnectionPreface(this);
        //不断读取http2.0的数据帧
        while (reader.nextFrame(false, this)) {
        }
 }
```

**Http2Reader的nextFrame方法**

```
 public boolean nextFrame(boolean requireSettings, Handler handler) throws IOException {
    try {
      source.require(9); // Frame header size
    } catch (IOException e) {
      return false; // This might be a normal socket close.
    }
    //数据帧格式
    //  0                   1                   2                   3
    //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                 Length (24)                   |
    // +---------------+---------------+---------------+
    // |   Type (8)    |   Flags (8)   |
    // +-+-+-----------+---------------+-------------------------------+
    // |R|                 Stream Identifier (31)                      |
    // +=+=============================================================+
    // |                   Frame Payload (0...)                      ...
    // +---------------------------------------------------------------+
    ...
    switch (type) {
      case TYPE_DATA:
        readData(handler, length, flags, streamId);
        break;

      case TYPE_HEADERS:
        readHeaders(handler, length, flags, streamId);
        break;

      case TYPE_PRIORITY:
        readPriority(handler, length, flags, streamId);
        break;

      case TYPE_RST_STREAM:
        readRstStream(handler, length, flags, streamId);
        break;

      case TYPE_SETTINGS:
        readSettings(handler, length, flags, streamId);
        break;

      case TYPE_PUSH_PROMISE:
        readPushPromise(handler, length, flags, streamId);
        break;

      case TYPE_PING:
        readPing(handler, length, flags, streamId);
        break;

      case TYPE_GOAWAY:
        readGoAway(handler, length, flags, streamId);
        break;

      case TYPE_WINDOW_UPDATE:
        readWindowUpdate(handler, length, flags, streamId);
        break;

      default:
        // Implementations MUST discard frames that have unknown or unsupported types.
        source.skip(length);
    }
    return true;
  }
```

### okhttp运用的设计模式

构造者模式（OkhttpClient,Request等各种对象的创建）

工厂模式（在Call接口中，有一个内部工厂Factory接口。）

单例模式（Platform类，已经使用Okhttp时使用单例）

策略模式（在CacheInterceptor中，在响应数据的选择中使用了策略模式，选择缓存数据还是选择网络访问。）

责任链模式（拦截器的链式调用）

享元模式（Dispatcher的线程池中，不限量的线程池实现了对象复用）

#### 优点

1. 支持SPDY，允许连接同一主机的所有请求分享一个socket。 
2. 如果SPDY不可用，会使用连接池减少请求延迟。
3. 使用GZIP压缩下载内容，且压缩操作对用户是透明的。
4. 利用响应缓存来避免重复的网络请求。
5. 当网络出现问题的时候，OKHttp会依然有效，它将从常见的连接问题当中恢复。
6. 如果你的服务端有多个IP地址，当第一个地址连接失败时，OKHttp会尝试连接其他的地址，这对IPV4和IPV6以及寄宿在多个数据中心的服务而言，是非常有必要的


相关文章：

1. [Okhttp源码简单解析](https://blog.csdn.net/chunqiuwei/article/details/71939952)
2. 