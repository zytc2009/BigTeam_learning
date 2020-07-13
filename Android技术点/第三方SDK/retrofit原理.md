[Toc]

Retrofit的代码精简明了，设计巧妙。我们一起学习一下他的原理

#### 使用

首先我们清楚，他是对Okhttp的一种封装，所以他依赖Okhttp，需要操作OkhttpClient

```
OkHttpClient client=new OkHttpClient.Builder().build();

Retrofit retrofit=new Retrofit.Builder().
   addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create())).  //设置gson转换器,将返回的json数据转为实体  
   addCallAdapterFactory(RxJavaCallAdapterFactory.create()).//设置CallAdapter工厂
   baseUrl(ApiServise.HOST).
   client(client) //设置客户端okhttp相关参数
   .build();
```

一般我们请求都是需要JSON转换，所以添加JSON转换器，

为了使Retrofit支持Rxjava使用，添加工厂，后文我们讲解工厂类

```
addCallAdapterFactory(RxJavaCallAdapterFactory.create())
```

然后我们就可以写我们的接口请求逻辑了

```
返回数据实体类：
public class BaseReponse<T> {
    private int code;                   //响应码
    private String message;             //提示信息
    private T result;                  //返回的具体数据
    ...省略set/get方法
}

/**
 * 请求方法类
  */
public interface ApiServise {
    @FormUrlEncoded
    @POST("/getWangYiNews")
    Call<BaseReponse<List<NewsBean>>> getWangYiNews(@Field("page") String page, @Field("count") String count);
}
```

然后我们开测试一下，记得开网络权限

我们现在只是声明一个请求方法，他返回Call对象, 然后我们可以选择同步或异步调用，同Okhttp使用，注意一点，如果是同步调用，记得开线程。

```
retrofit.create(ApiServise.class).getWangYiNews("1", "5").enqueue(new  Callback<BaseReponse<List<NewsBean>>>() {
    @Override
    public void onResponse(Call<BaseReponse<List<NewsBean>>> call, Response<BaseReponse<List<NewsBean>>> response) {
    }

    @Override
    public void onFailure(Call<BaseReponse<List<NewsBean>>> call, Throwable t) {

    }
});
```

这样就完成了一个简单的请求

然后我们看怎么使用RxJava，我们在前文已经加入了Rxjava支持，所以使用起来非常简单

```
首先定义一个观察者类，只是为了统一以后的请求处理
public abstract class BaseObserver<T> implements Observer<BaseReponse<T>> {
    private Context mContext;

    public BaseObserver(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof ConnectException ||
                e instanceof TimeoutException ||
                e instanceof NetworkErrorException ||
                e instanceof UnknownHostException) {
            try {
                onFailure(e, false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                onFailure(e, true);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onNext(BaseReponse<T> tBaseReponse) {
        if (HttpUtils.isRequestSuccess(tBaseReponse.getCode())) {
            onSuccess(tBaseReponse);
        } else {
            onCodeError(tBaseReponse);
        }
    }

    //请求成功且返回码为200的回调方法,这里抽象方法申明
    public abstract void onSuccess(BaseReponse<T> tBaseReponse);

    //请求成功但返回的code码不是200的回调方法,这里抽象方法申明
    public abstract void onCodeError(BaseReponse tBaseReponse);

    //请求失败回调方法,这里抽象方法申明
    public abstract void onFailure(Throwable e, boolean netWork) throws Exception;
}
```

然后在ApiService中增加RxJava的接口

```
public interface ApiServise {
   ...省略第一个接口

    @FormUrlEncoded         //post请求必须要申明该注解
    @POST("/getWangYiNews")   //方法名
    Observable<BaseReponse<List<NewsBean>>> getWangYiNews2(@Field("page") String page,  @Field("count") String count);//请求参数
}
```

这样就可以了，然后我们写个测试

```
retrofit.create(ApiServise.class).getWangYiNews2("1", "5").
	subscribeOn(Schedulers.io()).
	observeOn(AndroidSchedulers.mainThread()).
	subscribe(new BaseObserver<List<NewsBean>>(context) {
    	@Override
   		public void onSuccess(BaseReponse<List<NewsBean>> t) {
    	}

    	@Override
    	public void onCodeError(BaseReponse baseReponse) {
    	}

    	@Override
    	public void onFailure(Throwable e, boolean netWork) throws Exception {
    	}
});
```

运行成功，我们可以看到跟RxJava的用法一样，拿到Observer之后就可以注册观察者，从而处理后续事情

#### 原理分析

首先，Retrofit为了统一管理，用了动态代理

retrofit.create(ApiServise.class)

```
public <T> T create(final Class<T> service) {
  return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
      new InvocationHandler() {
        private final Platform platform = Platform.get();
        @Override public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
          if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
          }
          if (platform.isDefaultMethod(method)) {
            return platform.invokeDefaultMethod(method, service, proxy, args);
          }
          //加载类的方法，这个里面会根据返回数据类型查找合适的工厂，创建CallAdapter
          ServiceMethod<Object, Object> serviceMethod =
              (ServiceMethod<Object, Object>) loadServiceMethod(method);
          //封装一个OkHttpCall对象
          OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
          //让callAdapter去执行
          return serviceMethod.callAdapter.adapt(okHttpCall);
        }
      });
}
```

我们看一下loadServiceMethod方法

```
ServiceMethod<?, ?> loadServiceMethod(Method method) {
  ServiceMethod<?, ?> result = serviceMethodCache.get(method);//做缓存，一个方法一份
  if (result != null) return result;//优先取缓存对象
  synchronized (serviceMethodCache) {
    result = serviceMethodCache.get(method);
    if (result == null) {
      result = new ServiceMethod.Builder<>(this, method).build();
      serviceMethodCache.put(method, result);//记入缓存
    }
  }
  return result;
}
```

然后跟踪ServiceMethod.Builder类

```
public ServiceMethod build() {
  callAdapter = createCallAdapter();
  //获取数据转换器
  responseConverter = createResponseConverter();
  //处理注解参数
  for (Annotation annotation : methodAnnotations) {
    parseMethodAnnotation(annotation);
  }

  int parameterCount = parameterAnnotationsArray.length;
  parameterHandlers = new ParameterHandler<?>[parameterCount];
  for (int p = 0; p < parameterCount; p++) {
    Type parameterType = parameterTypes[p];
    Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
    //查找对应的注解处理器，一种注解只能对应一种处理器
    parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations);
  }
  return new ServiceMethod<>(this);
}

private CallAdapter<T, R> createCallAdapter() {
      ...
      Type returnType = method.getGenericReturnType();
      Annotation[] annotations = method.getAnnotations();
      try {
        return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
      } catch (RuntimeException e) {
        throw methodError(e, "Unable to create call adapter for %s", returnType);
      }
}
```

继续追踪retrofit.callAdapter

```
public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
    return nextCallAdapter(null, returnType, annotations);
}
  
public CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
    Annotation[] annotations) {
  int start = adapterFactories.indexOf(skipPast) + 1;
  for (int i = start, count = adapterFactories.size(); i < count; i++) {
    //让工厂创建
    CallAdapter<?, ?> adapter = adapterFactories.get(i).get(returnType, annotations, this);
    if (adapter != null) {
      return adapter;
    }
  }

  throw new IllegalArgumentException(builder.toString());
}
```

然后我们看adapterFactories的操作，是在Retrofit的Builder中处理的，有一个系统默认的，用户可以通过addCallAdapterFactory方法添加自定义工厂，这样流程就比较清楚了

#### 总结：

我们在ApiServise接口中声明的方法返回类型取决于CallAdapterFactory的支持，默认的工厂只支持返回Call类型，也就是我们的第一种写法，我们添加RxJavaCallAdapterFactory，就增加支持返回Observable类型。

由此我们得出结论，如果你想增加其他自定义返回类型，就需要自己实现CallAdapterFactory，并调用addCallAdapterFactory添加工厂列表，这样就可以了

#### 自定义返回类型

按照我们设想的来做个例子

```
public interface HttpCallback<T> {
    void success(Response<T> response, HttpCall<T> httpCall);
    void clientError(Response<T> response, HttpCall<T> httpCall);
    void networkError(Throwable iOException, HttpCall<T> httpCall);
}
定义返回类型，这个跟okhttp的Call接口类似，只是enqueue方法的参数不同
public interface HttpCall<T> extends Cloneable {
    void cancel();
    HttpCall<T> clone();
    Response<T> execute() throws IOException;
    boolean isCanceled();
    boolean isExecuted();
    Request request();
    void enqueue(HttpCallback<T> callback);
}
```

然后我们在ApiService类增加一个方法

```
/**
 * post请求方式
 */
@FormUrlEncoded         //post请求必须要申明该注解
@POST("/getWangYiNews")   //方法名
HttpCall<BaseReponse<List<NewsBean>>> getWangYiNews(@Field("page") String page, @Field("count") String count);//请求参数
```

现在结构造好了，不能运行，因为默认callAdapter不识别

```
public class HttpCallAdapterFactory<R> extends CallAdapter.Factory {

    /* access modifiers changed from: private */
    public MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    private HttpCallAdapterFactory() {
    }

    public static HttpCallAdapterFactory create() {//创建对象
        return new HttpCallAdapterFactory();
    }

    public CallAdapter<?, ?> get(Type type, Annotation[] annotationArr, Retrofit retrofit) {
        if (Types.getRawType(type) != HttpCall.class) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            final Type parameterUpperBound = getParameterUpperBound(0, (ParameterizedType) type);
            //我们这里只返回了一种，你也可以根据type区分，来创建不同的CallAdapter
            return new CallAdapter<R, HttpCall<?>>() {
                public Type responseType() {
                    return parameterUpperBound;
                }

                @Override
                public HttpCall<?> adapt(Call<R> call) {
                    HttpCallAdapter httpCallAdapter = new HttpCallAdapter(call, HttpCallAdapterFactory.this.mMainThreadExecutor);
                    return httpCallAdapter;
                }
            };
        }
        throw new IllegalStateException("HttpCall must have generic type (e.g., HttpCall<ResponseBody>)");
    }

    class MainThreadExecutor implements Executor {
        private Handler mainHandler = new Handler(Looper.getMainLooper());

        MainThreadExecutor() {
        }

        public void execute(Runnable runnable) {
            this.mainHandler.post((Runnable) checkNotNull(runnable, "command == null"));
        }
    }
}
```

HttpCallAdapter的代码只是利用Okhttp的Call对象做一些操作，相当于一个静态代理

```
public class HttpCallAdapter<T> implements HttpCall<T> {
    private Call<T> mDelegate;
    public HttpCallAdapterFactory.MainThreadExecutor mMainThreadExecutor;

    public HttpCallAdapter(Call<T> call, HttpCallAdapterFactory.MainThreadExecutor mainThreadExecutor) {
        this.mDelegate = call;
        this.mMainThreadExecutor = mainThreadExecutor;
    }

    public Response<T> execute() throws IOException {
        return this.mDelegate.execute();
    }

    public void enqueue(final HttpCallback<T> httpCallback) {
        checkNotNull(httpCallback, "callback == null");
        this.mDelegate.enqueue(new Callback<T>() {
            public void onResponse(Call<T> call, final Response<T> response) {
                HttpCallAdapter.this.mMainThreadExecutor.execute(new Runnable() {
                    public void run() {
                        if (httpCallback == null) {
                            return;
                        }
                        int code = response.code();
                        if (HttpUtils.isRequestSuccess(code)) {
                            T body = response.body();
                            if (!response.isSuccessful()) {
                                return;
                            }
                            httpCallback.success(response, HttpCallAdapter.this);
                        } else{
                            httpCallback.clientError(response, HttpCallAdapter.this);
                        }
                    }
                });
            }

            public void onFailure(Call<T> call, final Throwable th) {
                HttpCallAdapter.this.mMainThreadExecutor.execute(new Runnable() {
                    public void run() {
                            httpCallback.networkError(th, HttpCallAdapter.this);
                    }
                });
            }
        });
    }

    public void cancel() {
        this.mDelegate.cancel();
    }

    public boolean isExecuted() {
        return this.mDelegate.isExecuted();
    }

    public boolean isCanceled() {
        return this.mDelegate.isCanceled();
    }

    public HttpCall<T> clone() {
        return new HttpCallAdapter(this.mDelegate.clone(), this.mMainThreadExecutor);
    }

    public Request request() {
        return this.mDelegate.request();
    }
}
```

然后我们把工厂类加入addCallAdapterFactory(HttpCallAdapterFactory.create())

好了，可以测试了，如果你的代码没有问题，应该能拿到结果了



具体代码可以在github查看：https://github.com/zytc2009/AppTemplate

