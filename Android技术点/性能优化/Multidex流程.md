我们现在大多app都是超65536的，所以一般都要处理MultiDex的问题，所以我们在Application的attachBaseContext方法添加了MultiDex.install(this);，那么这个过程做了哪些事？

```java
MultiDex类：
public static void install(Context context) {
    if (IS_VM_MULTIDEX_CAPABLE) {//虚拟机不支持multidex的不需要处理，虚拟机版本号>=2.1的不处理，ART是不需要处理的 
       Log.i(TAG, "VM has multidex support, MultiDex support library is disabled.");
    } else if (VERSION.SDK_INT < 4) {//可以忽略了       
    } else {
        try {
            ApplicationInfo applicationInfo = getApplicationInfo(context);
            doInstallation(context, new File(applicationInfo.sourceDir), new File(applicationInfo.dataDir), "secondary-dexes", "");
        } catch (Exception var2) {
           throw new RuntimeException("MultiDex installation failed");
        }
    }
}

private static void doInstallation(Context mainContext, File sourceApk, File dataDir, String secondaryFolderName, String prefsKeyPrefix) throws Exception {
    synchronized(installedApk) {
         if (!installedApk.contains(sourceApk)) {//避免重复安装
              installedApk.add(sourceApk);//安装的apk集合                
              if (loader == null) {
               } else {
                    try {//清除之前的dex文件目录，也就是删除secondary-dexes目录
                        clearOldDexDir(mainContext);
                    } catch (Throwable var10) {
                    }
                    File dexDir = getDexDir(mainContext, dataDir, secondaryFolderName);
                    //解压提取dex文件列表
                    List<? extends File> files = MultiDexExtractor.load(mainContext, sourceApk, dexDir, prefsKeyPrefix, false);
                   //安装dex文件
                    installSecondaryDexes(loader, dexDir, files);
              }
         }
    }
}

先看安装过程：
private static void installSecondaryDexes(ClassLoader loader, File dexDir, List<? extends File> files) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
    if (!files.isEmpty()) {//区分不同系统的版本，做不同的安装处理
        if (VERSION.SDK_INT >= 19) {//主要是设置pathList的dexElements属性，具体可以看一下热修复的原理
            MultiDex.V19.install(loader, files, dexDir);
        } else if (VERSION.SDK_INT >= 14) {//主要是设置pathList的dexElements属性
            MultiDex.V14.install(loader, files, dexDir);
        } else {//14以下版本，classloader的数据结构不同了，所以反射的时候处理方式不同
            MultiDex.V4.install(loader, files);
        }
    }
}

然后看MultiDexExtractor类的dex提取方法：
static List<? extends File> load(Context context, File sourceApk, File dexDir, String prefsKeyPrefix, boolean forceReload) throws IOException {
        long currentCrc = getZipCrc(sourceApk);//获取apk的crc校验
        File lockFile = new File(dexDir, "MultiDex.lock");
        RandomAccessFile lockRaf = new RandomAccessFile(lockFile, "rw");
        FileChannel lockChannel = null; FileLock cacheLock = null;IOException releaseLockException = null;
        List files;
        try {
            lockChannel = lockRaf.getChannel();
            cacheLock = lockChannel.lock();
            
            if (!forceReload && !isModified(context, sourceApk, currentCrc, prefsKeyPrefix)) {//apk文件的crc和修改时间和sp中数据比较
                try {//没变化则取之前的缓存的zip文件，zip怎么产生的？当然是第一次解压的，看putStoredApkInfo方法
                    files = loadExistingExtractions(context, sourceApk, dexDir, prefsKeyPrefix);
                } catch (IOException var21) {//发生异常，就按首次安装处理
                    files = performExtractions(sourceApk, dexDir);
                    putStoredApkInfo(context, prefsKeyPrefix, getTimeStamp(sourceApk), currentCrc, files);
                }
            } else {//有变化或者第一次安装
                //从apk提取除主的dex外的dex文件列表
                files = performExtractions(sourceApk, dexDir);
                //把信息存储到sp，存储dex格式，apk时间戳、crc和各个dex的crc和修改时间
                putStoredApkInfo(context, prefsKeyPrefix, getTimeStamp(sourceApk), currentCrc, files);
            }
        } finally {
            if (cacheLock != null) {
                try {
                    cacheLock.release();
                } catch (IOException var20) {
                    Log.e("MultiDex", "Failed to release lock on " + lockFile.getPath());
                    releaseLockException = var20;
                }
            }
            if (lockChannel != null) {
                closeQuietly(lockChannel);
            }
            closeQuietly(lockRaf);
        }

        if (releaseLockException != null) {
            throw releaseLockException;
        } else {
            Log.i("MultiDex", "load found " + files.size() + " secondary dex files");
            return files;
        }
}

先看第一次进来的处理：
//从apk提取除主的dex外的dex文件列表
private static List<MultiDexExtractor.ExtractedDex> performExtractions(File sourceApk, File dexDir) throws IOException {
    String extractedFilePrefix = sourceApk.getName() + ".classes";
    prepareDexDir(dexDir, extractedFilePrefix);//做简单校验，删除目标目录下非法文件
    List<MultiDexExtractor.ExtractedDex> files = new ArrayList();
    ZipFile apk = new ZipFile(sourceApk);
    try {
        //提取除主的dex外的dex文件，classesXX.dex,XX是2开头的序列号
        int secondaryNumber = 2;
        for(ZipEntry dexFile = apk.getEntry("classes" + secondaryNumber + ".dex"); dexFile != null; dexFile = apk.getEntry("classes" + secondaryNumber + ".dex")) {
           String fileName = extractedFilePrefix + secondaryNumber + ".zip";
           MultiDexExtractor.ExtractedDex extractedFile = new MultiDexExtractor.ExtractedDex(dexDir, fileName);
           files.add(extractedFile);
           
           int numAttempts = 0;
           boolean isExtractionSuccessful = false;
           while(numAttempts < 3 && !isExtractionSuccessful) {
               ++numAttempts;
               //提取classesXX.dex文件到指定目录，提取过程以tmp-apkname.classesXX.zip命名，提取成功重命名为apkname.classesXX.zip
               extract(apk, dexFile, extractedFile, extractedFilePrefix);
               try {//获取dex的crc值
                   extractedFile.crc = getZipCrc(extractedFile);
                   isExtractionSuccessful = true;
                } catch (IOException var19) {
                        isExtractionSuccessful = false;
           		}
            	if (!isExtractionSuccessful) {
                        extractedFile.delete();
                }
            }
            if (!isExtractionSuccessful) {
                    throw new IOException("Could not create zip file");
            }
            ++secondaryNumber;
        }
    } finally {
         try {
                apk.close();
         } catch (IOException var18) { Log.w("MultiDex", "Failed to close resource);}
    }
    return files;
}

然后看第二次启动：
//加载已存在的dex列表
private static List<MultiDexExtractor.ExtractedDex> loadExistingExtractions(Context context, File sourceApk, File dexDir, String prefsKeyPrefix) throws IOException {
        String extractedFilePrefix = sourceApk.getName() + ".classes";
        SharedPreferences multiDexPreferences = getMultiDexPreferences(context);
        int totalDexNumber = multiDexPreferences.getInt(prefsKeyPrefix + "dex.number", 1);
        List<MultiDexExtractor.ExtractedDex> files = new ArrayList(totalDexNumber - 1);
        //处理除主dex外的其他dex文件
        for(int secondaryNumber = 2; secondaryNumber <= totalDexNumber; ++secondaryNumber) {//之前的zip文件，apkname.classesXX.zip
            String fileName = extractedFilePrefix + secondaryNumber + ".zip";
            MultiDexExtractor.ExtractedDex extractedFile = new MultiDexExtractor.ExtractedDex(dexDir, fileName);
            if (!extractedFile.isFile()) {
                throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
            }
            ...省略取信息代码
            //校验zip文件的crc、修改时间是否跟sp记录一致
            if (expectedModTime != lastModified || expectedCrc != extractedFile.crc) {
                throw new IOException("Invalid extracted dex");
            }
            files.add(extractedFile);
        }
        return files;
}

```



