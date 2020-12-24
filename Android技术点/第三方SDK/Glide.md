Glide：

```
RequestBuilder构建任务SingleRequest添加到RequestManager请求队列，并执行
```

```java
RequestManager：添加任务
void track(Target<?> target, Request request) {
  targetTracker.track(target);
  requestTracker.runRequest(request);
}
```

```java
RequestTracker类：开始任务
public void runRequest(Request request) {
  requests.add(request);
  if (!isPaused) {
    request.begin();
  } else {
    pendingRequests.add(request);
  }
}
```

SingleRequest的begin()方法

```java
@Override
public void begin() {
  ...
  if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
    onSizeReady(overrideWidth, overrideHeight);
  } else {
    target.getSize(this);
  }
  ...
}
public void onSizeReady(int width, int height) {
	。。。
   engine.load(...);//开始加载
}
```

Engine类：

```java
public <R> LoadStatus load(...) {
  //key
  EngineKey key = keyFactory.buildKey(model, signature, width, height, transformations,
      resourceClass, transcodeClass, options);
  //从缓存加载
  EngineResource<?> cached = loadFromCache(key, isMemoryCacheable);
  if (cached != null) {
    cb.onResourceReady(cached, DataSource.MEMORY_CACHE);
    return null;
  }
  
  EngineResource<?> active = loadFromActiveResources(key, isMemoryCacheable);
  if (active != null) {
    cb.onResourceReady(active, DataSource.MEMORY_CACHE);
    return null;
  }
  //已经在任务中
  EngineJob<?> current = jobs.get(key);
  if (current != null) {
    current.addCallback(cb);
    return new LoadStatus(cb, current);
  }
  //添加新任务
  EngineJob<R> engineJob = engineJobFactory.build(key, isMemoryCacheable,
      useUnlimitedSourceExecutorPool);
  DecodeJob<R> decodeJob = decodeJobFactory.build(...);
  jobs.put(key, engineJob);
  engineJob.addCallback(cb);
  engineJob.start(decodeJob);
  return new LoadStatus(cb, engineJob);
}
```

```java
DecodeJob的run方法：
public void run() {
	...
    runWrapped();//runGenerators()
    ...
}

```

```java
HttpGlideUrlLoader:
  public LoadData<InputStream> buildLoadData(GlideUrl model, int width, int height,
      Options options) {
    。。。
    return new LoadData<>(url, new HttpUrlFetcher(url, timeout));
  }

HttpUrlFetcher：获取流
public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
    final InputStream result;
    try {
      result = loadDataWithRedirects(glideUrl.toURL(), 0 /*redirects*/, null /*lastUrl*/,
          glideUrl.getHeaders());
    } catch (IOException e) {
      return;
    }
    callback.onDataReady(result);
}
```

```java
SourceGenerator:
  @Override
  public void onDataReady(Object data) {
    DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
    if (data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource()))
      。。。
    } else {
      cb.onDataFetcherReady(loadData.sourceKey, data, loadData.fetcher,
          loadData.fetcher.getDataSource(), originalKey);
    }
  }
```

DecodeJob的onDataFetcherReady方法

```java
@Override
public void onDataFetcherReady(Key sourceKey, Object data, DataFetcher<?> fetcher,
    DataSource dataSource, Key attemptedKey) {
    ...
    decodeFromRetrievedData();
  }
}

private void decodeFromRetrievedData() {
    try {
      resource = decodeFromData(currentFetcher, currentData, currentDataSource);
    } catch (GlideException e) {
    }
    if (resource != null) {//拿到Bitmap，EngineJob任务结束，通知Engine
      notifyEncodeAndRelease(resource, currentDataSource);
    } else {
      runGenerators();
    }
}
```

```java
StreamBitmapDecoder类:
@Override
public Resource<Bitmap> decode(InputStream source, int width, int height, Options options){
      。。。
      return downsampler.decode(invalidatingStream, width, height, options, callbacks);
}

Downsampler 类的主要方法：
public Resource<Bitmap> decode(InputStream is, ..., DecodeCallbacks callbacks) throws IOException {
    try {
      Bitmap result = decodeFromWrappedStreams(is, bitmapFactoryOptions,..., callbacks);
      //返回BitmapResource管理对象
      return BitmapResource.obtain(result, bitmapPool);
    } finally {
    }
}

private Bitmap decodeFromWrappedStreams(InputStream is, ...) throws IOException {
    // 4.4之前版本, inBitmap大小必须精确匹配要解码的图片
    if ((options.inSampleSize == 1 || isKitKatOrGreater) && shouldUsePool(is)) {     
      if (expectedWidth > 0 && expectedHeight > 0) {//复用
        setInBitmap(options, bitmapPool, expectedWidth, expectedHeight);
      }
    }
    Bitmap downsampled = decodeStream(is, options, callbacks, bitmapPool);
    callbacks.onDecodeComplete(bitmapPool, downsampled);

    if (downsampled != null) {
      // 缩放图片还原处理
      downsampled.setDensity(displayMetrics.densityDpi);
      rotated = TransformationUtils.rotateImageExif(bitmapPool, downsampled, orientation);
      if (!downsampled.equals(rotated)) {
        bitmapPool.put(downsampled);
      }
    }
    return rotated;
  }
```

```：
LruBitmapPool：图片池
SizeConfigStrategy：4.4以上
AttributeStrategy：4.4之前版本, inBitmap大小必须精确匹配要解码的图片
```

```java
BitmapResource：//管理图片
  @Override
  public void recycle() {//回收图片加入缓存池
    bitmapPool.put(bitmap);
  }

```

**如果数据需要加解密，需要自定义处理**

```java
public class BlueGlideModule extends AppGlideModule {

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Downsampler downsampler =
                new Downsampler(
                        registry.getImageHeaderParsers(), context.getResources().getDisplayMetrics(), glide.getBitmapPool(), glide.getArrayPool());
		//覆写encode，decode处理类
        registry.prepend(ByteBuffer.class, new MyByteBufferEncoder())
                .prepend(InputStream.class, new MyStreamEncoder(glide.getArrayPool()))
                /* Bitmaps */
                .prepend(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class, new MyByteDecoder(downsampler))
                .prepend(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class, new MyStreamDecoder(downsampler, glide.getArrayPool()));
    }    
}
```

这是参考Glide类源码中的代码写的：

```java
Glide类
registry
    .append(ByteBuffer.class, new ByteBufferEncoder())
    .append(InputStream.class, new StreamEncoder(arrayPool))
    /* Bitmaps */
    .append(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class, byteBufferBitmapDecoder)
    .append(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class, streamBitmapDecoder);
```