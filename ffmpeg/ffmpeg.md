[Toc]

### 项目组成

#### include目录

**libavcodec**：用于各种类型声音/图像编解码；

| 文件            | 简要说明                           |
| --------------- | ---------------------------------- |
| allcodecs.c     | 简单的注册类函数                   |
| avcodec.h       | 编解码相关结构体定义和函数原型声明 |
| dsputil.c       | 限幅数组初始化                     |
| imgconvert.c    | 颜色空间转换相关函数实现           |
| utils_codec.c   | 一些解码相关的工具类函数的实现     |
| mpeg4audio.c    | mpeg4 音频编解码器的函数实现       |
| mpeg4video.c    | mpeg4 视频编解码器的函数实现       |
| mpeg4videodec.c | mpeg4 视频解码器的函数实现         |
| mpeg4videoenc.c | mpeg4 视频编码器的函数实现         |

**libavutil**：包含Hash器，解码器和一些公共的工具函数；

| avutil.h      | 简单的像素格式宏定义         |
| ------------- | ---------------------------- |
| bswap.h       | 简单的大小端转换函数的实现   |
| commom.h      | 公共的宏定义和简单函数的实现 |
| mathematics.c | 数学运算函数实现             |
| rational.h    | 分数相关表示的函数实现       |

**libavformat**：包含多种多媒体容器格式的封装、解封装工具和基本IO访问

| 文件           | 简要说明                           |
| -------------- | ---------------------------------- |
| allformats.c   | 简单的注册类函数                   |
| avio.c         | 无缓冲 IO 相关函数实现             |
| aviobuf.c      | 有缓冲数据 IO 相关函数实现         |
| cutils.c       | 简单的字符串操作函数               |
| utils_format.c | 文件和媒体格式相关的工具函数的实现 |
| file.c         | 文件 io 相关函数                   |
| avi.c          | AVI 格式的相关函数定义             |
| avidec.c       | AVI 格式 DEMUXER 相关函数定义      |
| avienc.c       | AVI 格式 MUXER 相关函数定义        |

**libswscale**：用于视频场景比例缩放、色彩映射转换；

**libpostproc**：用于后期效果处理；

**libavdevice**：用于音视频数据采集、渲染和回放等功能的设备相关接口

**ibswresample**：用于音频混音、重采样和格式转换等功能

**libavfilter**：包含多媒体处理常用的滤镜、过滤器功能

> fmpeg的官方网站是：http://ffmpeg.org/
>
> 编译好的windows可用版本的下载地址（官网中可以连接到这个网站，和官方网站保持同步）： http://ffmpeg.zeranoe.com/builds/
>
> 该网站中的FFMPEG分为3个版本：Static，Shared，Dev。
>
> 前两个版本可以直接在命令行中使用，他们的区别在于：Static里面只有3个应用程序：ffmpeg.exe，ffplay.exe，ffprobe.exe，每个exe的体积都很大，相关的Dll已经被编译到exe里面去了。Shared里面除了3个应用程序：ffmpeg.exe，ffplay.exe，ffprobe.exe之外，还有一些Dll，比如说avcodec-54.dll之类的。Shared里面的exe体积很小，他们在运行的时候，到相应的Dll中调用功能。
>
> Dev版本是用于开发的，里面包含了库文件xxx.lib以及头文件xxx.h，这个版本不包含exe文件。
>

打开系统命令行界面，切换到ffmpeg所在的目录，就可以使用这3个应用程序了。

#### bin目录

**ffmpeg**：该项目提供的一个工具，可用于格式转换、解码或电视卡即时编码等；

**ffprobe**：ffprobe是用于查看文件格式的应用程序。

**ffplay**：是一个简单的播放器，使用ffmpeg 库解析和解码，通过SDL显示；

**ffsever**：一个 HTTP 多媒体即时广播串流服务器；

#### share目录

share目录下有ffmpeg和man目录

ffmpeg下有个examples目录，常用功能的例子都能找到



### ffmpeg源码结构图

#### 编码：

![](images\struct_encode.jpg)



**函数背景色**
函数在图中以方框的形式表现出来。不同的背景色标志了该函数不同的作用：

> 粉红色背景函数：FFmpeg的API函数。
> 白色背景的函数：FFmpeg的内部函数。
> 黄色背景的函数：URLProtocol结构体中的函数，包含了读写各种协议的功能。
> 绿色背景的函数：AVOutputFormat结构体中的函数，包含了读写各种封装格式的功能。
> 蓝色背景的函数：AVCodec结构体中的函数，包含了编解码的功能。

**区域**
整个关系图可以分为以下几个区域：

> 左边区域——架构函数区域：这些函数并不针对某一特定的视频格式。
> 右上方黄色区域——协议处理函数区域：不同的协议（RTP，RTMP，FILE）会调用不同的协议处理函数。
> 右边中间绿色区域——封装格式处理函数区域：不同的封装格式（MKV，FLV，MPEG2TS，AVI）会调用不同的封装格式处理函数。
> 右边下方蓝色区域——编解码函数区域：不同的编码标准（HEVC，H.264，MPEG2）会调用不同的编解码函数。

**箭头线**
为了把调用关系表示的更明显，图中的箭头线也使用了不同的颜色：

    红色的箭头线：标志了编码的流程。
    其他颜色的箭头线：标志了函数之间的调用关系。其中：
    调用URLProtocol结构体中的函数用黄色箭头线标识；
    调用AVOutputFormat结构体中的函数用绿色箭头线标识；
    调用AVCodec结构体中的函数用蓝色箭头线标识。
**函数所在的文件**
每个函数标识了它所在的文件路径。

左边区域（架构函数）

**右上区域**（URLProtocol协议处理函数）
URLProtocol结构体包含如下协议处理函数指针：

```
url_open()：打开
url_read()：读取
url_write()：写入
url_seek()：调整进度
url_close()：关闭
```

【例子】不同的协议对应着上述接口有不同的实现函数，举几个例子：
File协议（即文件）对应的URLProtocol结构体ff_file_protocol：
url_open() -> file_open() -> open()
url_read() -> file_read() -> read()
url_write() -> file_write() -> write()
url_seek() -> file_seek() -> lseek()

url_close() -> file_close() -> close()

RTMP协议（libRTMP）对应的URLProtocol结构体ff_librtmp_protocol：
url_open() -> rtmp_open() -> RTMP_Init(), RTMP_SetupURL(), RTMP_Connect(), RTMP_ConnectStream()
url_read() -> rtmp_read() -> RTMP_Read()
url_write() -> rtmp_write() -> RTMP_Write()
url_seek() -> rtmp_read_seek() -> RTMP_SendSeek()

url_close() -> rtmp_close() -> RTMP_Close()
UDP协议对应的URLProtocol结构体ff_udp_protocol：
url_open() -> udp_open()
url_read() -> udp_read()
url_write() -> udp_write()
url_seek() -> udp_close()
url_close() -> udp_close()

右中区域（AVOutputFormat封装格式处理函数）
AVOutputFormat包含如下封装格式处理函数指针：

```
write_header()：写文件头
write_packet()：写一帧数据
write_trailer()：写文件尾
```

【例子】不同的封装格式对应着上述接口有不同的实现函数，举几个例子：
FLV封装格式对应的AVOutputFormat结构体ff_flv_muxer：
write_header() -> flv_write_header()
write_packet() –> flv_write_packet()

【例子】不同的封装格式对应着上述接口有不同的实现函数，举几个例子：
FLV封装格式对应的AVOutputFormat结构体ff_flv_muxer：
write_header() -> flv_write_header()
write_packet() –> flv_write_packet()

write_trailer() -> flv_write_trailer()
MKV封装格式对应的AVOutputFormat结构体ff_matroska_muxer：
write_header() -> mkv_write_header()
write_packet() –> mkv_write_flush_packet()

write_trailer() -> mkv_write_trailer()
MPEG2TS封装格式对应的AVOutputFormat结构体ff_mpegts_muxer：
write_header() -> mpegts_write_header()
write_packet() –> mpegts_write_packet()

write_trailer() -> mpegts_write_end()
AVI封装格式对应的AVOutputFormat结构体ff_avi_muxer：
write_header() -> avi_write_header()
write_packet() –> avi_write_packet()
write_trailer() -> avi_write_trailer()

**右下区域**（AVCodec编解码函数）
AVCodec包含如下编解码函数指针：

```
init()：初始化
encode2()：编码一帧数据
close()：关闭
```

【例子】不同的编解码器对应着上述接口有不同的实现函数，举几个例子：
HEVC编码器对应的AVCodec结构体ff_libx265_encoder：
init() -> libx265_encode_init() -> x265_param_alloc(), x265_param_default_preset(), x265_encoder_open()
encode2() -> libx265_encode_frame() -> x265_encoder_encode()

【例子】不同的编解码器对应着上述接口有不同的实现函数，举几个例子：
HEVC编码器对应的AVCodec结构体ff_libx265_encoder：
init() -> libx265_encode_init() -> x265_param_alloc(), x265_param_default_preset(), x265_encoder_open()
encode2() -> libx265_encode_frame() -> x265_encoder_encode()

close() -> libx265_encode_close() -> x265_param_free(), x265_encoder_close()
H.264编码器对应的AVCodec结构体ff_libx264_encoder：
init() -> X264_init() -> x264_param_default(), x264_encoder_open(), x264_encoder_headers()
encode2() -> X264_frame() -> x264_encoder_encode()

close() -> X264_close() -> x264_encoder_close()
VP8编码器（libVPX）对应的AVCodec结构体ff_libvpx_vp8_encoder：
init() -> vpx_init() -> vpx_codec_enc_config_default()
encode2() -> vp8_encode() -> vpx_codec_enc_init(), vpx_codec_encode()

close() -> vp8_free() -> vpx_codec_destroy()
**MPEG2**编码器对应的AVCodec结构体ff_mpeg2video_encoder：
init() -> encode_init()
encode2() -> ff_mpv_encode_picture()
close() -> ff_mpv_encode_end()

#### 解码

![](images\struct_decode.jpg)

右上区域（URLProtocol协议处理函数）
URLProtocol结构体包含如下协议处理函数指针：

```
url_open()：打开
url_read()：读取
url_write()：写入
url_seek()：调整进度
url_close()：关闭
```

【例子】不同的协议对应着上述接口有不同的实现函数，举几个例子：
File协议（即文件）对应的URLProtocol结构体ff_file_protocol：

【例子】不同的协议对应着上述接口有不同的实现函数，举几个例子：
File协议（即文件）对应的URLProtocol结构体ff_file_protocol：

```
url_open() -> file_open() -> open()
url_read() -> file_read() -> read()
url_write() -> file_write() -> write()
url_seek() -> file_seek() -> lseek()
url_close() -> file_close() -> close()
```


RTMP协议（libRTMP）对应的URLProtocol结构体ff_librtmp_protocol：

```
url_open() -> rtmp_open() -> RTMP_Init(), RTMP_SetupURL(), RTMP_Connect(), RTMP_ConnectStream()
url_read() -> rtmp_read() -> RTMP_Read()
url_write() -> rtmp_write() -> RTMP_Write()
url_seek() -> rtmp_read_seek() -> RTMP_SendSeek()
url_close() -> rtmp_close() -> RTMP_Close()
```


UDP协议对应的URLProtocol结构体ff_udp_protocol：

```
url_open() -> udp_open()
url_read() -> udp_read()
url_write() -> udp_write()
url_seek() -> udp_close()
url_close() -> udp_close()
```

右中区域（AVInputFormat封装格式处理函数）
AVInputFormat包含如下封装格式处理函数指针：

```
read_probe()：检查格式
read_header()：读取文件头
read_packet()：读取一帧数据
read_seek()：调整进度
read_close()：关闭
```

【例子】不同的封装格式对应着上述接口有不同的实现函数，举几个例子：
FLV封装格式对应的AVInputFormat结构体ff_flv_demuxer：

```
read_probe() -> flv_probe() –> probe()
read_header() -> flv_read_header() -> create_stream() -> avformat_new_stream()
read_packet() -> flv_read_packet()
read_seek() -> flv_read_seek()
read_close() -> flv_read_close()
```


MKV封装格式对应的AVInputFormat结构体ff_matroska_demuxer：

```
read_probe() -> matroska_probe()
read_header() -> matroska_read_header()
read_packet() -> matroska_read_packet()
read_seek() -> matroska_read_seek()
read_close() -> matroska_read_close()
```


MPEG2TS封装格式对应的AVInputFormat结构体ff_mpegts_demuxer：

```
read_probe() -> mpegts_probe()
read_header() -> mpegts_read_header()
read_packet() -> mpegts_read_packet() 
read_close() -> mpegts_read_close()
```


AVI封装格式对应的AVInputFormat结构体ff_avi_demuxer：

```
read_probe() -> avi_probe()
read_header() -> avi_read_header()
read_packet() -> avi_read_packet()
read_seek() -> avi_read_seek()
read_close() -> avi_read_close()
```

**右下区域（AVCodec编解码函数）**
AVCodec包含如下编解码函数指针：

```
init()：初始化
decode()：解码一帧数据
close()：关闭
```

【例子】不同的编解码器对应着上述接口有不同的实现函数，举几个例子：
**HEVC**解码对应的AVCodec结构体ff_hevc_decoder：

```
init() -> hevc_decode_init()
decode() -> hevc_decode_frame() -> decode_nal_units()
close() -> hevc_decode_free()
```

**H.264**解码对应的AVCodec结构体ff_h264_decoder：

```
init() -> ff_h264_decode_init()
decode() -> h264_decode_frame() -> decode_nal_units()
close() -> h264_decode_end()
```

**VP8**解码（libVPX）对应的AVCodec结构体ff_libvpx_vp8_decoder：

```
init() -> vpx_init() -> vpx_codec_dec_init()
decode() -> vp8_decode() -> vpx_codec_decode(), vpx_codec_get_frame()
close() -> vp8_free() -> vpx_codec_destroy()
```

**MPEG2**解码对应的AVCodec结构体ff_mpeg2video_decoder：

MPEG2解码对应的AVCodec结构体ff_mpeg2video_decoder：

```
init() -> mpeg_decode_init()
decode() -> mpeg_decode_frame()
close() -> mpeg_decode_end()
```



### 命令集

ffmpeg 命令集举例

#### 1.获取视频的信息

ffmpeg -i video.avi

#### 2.将图片序列合成视频

ffmpeg -f image2 -i image%d.jpg video.mpg
上面的命令会把当前目录下的图片（名字如：image1.jpg. image2.jpg. 等…）合并成video.mpg

#### 3.将视频分解成图片序列

ffmpeg -i video.mpg image%d.jpg
上面的命令会生成image1.jpg. image2.jpg. …
支持的图片格式有：PGM. PPM. PAM. PGMYUV. JPEG. GIF. PNG. TIFF. SGI

#### 4.为视频重新编码以适合在iPod/iPhone上播放

ffmpeg -i source_video.avi input -acodec aac -ab 128kb -vcodec mpeg4 -b 1200kb -mbd 2 -flags +4mv+trell -aic 2 -cmp 2 -subcmp 2 -s 320x180 -title X final_video.mp4
说明：

* 源视频：source_video.avi
* 音频编码：aac
* 音频位率：128kb/s
* 视频编码：mpeg4
* 视频位率：1200kb/s
* 视频尺寸：320 X 180
* 生成的视频：final_video.mp4

#### 5.为视频重新编码以适合在PSP上播放

ffmpeg -i source_video.avi -b 300 -s 320x240 -vcodec xvid -ab 32 -ar 24000 -acodec aac final_video.mp4
说明：

* 源视频：source_video.avi
* 音频编码：aac
* 音频位率：32kb/s
* 视频编码：xvid
* 视频位率：1200kb/s
* #### 视频尺寸：320 X 180
* 生成的视频：final_video.mp4

#### 6.从视频抽出声音.并存为Mp3

ffmpeg -i source_video.avi -vn -ar 44100 -ac 2 -ab 192 -f mp3 sound.mp3
说明：

* 源视频：source_video.avi
* 音频位率：192kb/s
* 输出格式：mp3
* 生成的声音：sound.mp3

#### 7.将wav文件转成Mp3

ffmpeg -i son_origine.avi -vn -ar 44100 -ac 2 -ab 192 -f mp3 son_final.mp3

#### 8.将.avi视频转成.mpg

ffmpeg -i video_origine.avi video_finale.mpg

#### 9.将.mpg转成.avi

ffmpeg -i video_origine.mpg video_finale.avi

#### 10.将.avi转成gif动画（未压缩）

ffmpeg -i video_origine.avi gif_anime.gif

#### 11.合成视频和音频

ffmpeg -i son.wav -i video_origine.avi video_finale.mpg

#### 12.将.avi转成.flv

ffmpeg -i video_origine.avi -ab 56 -ar 44100 -b 200 -r 15 -s 320x240 -f flv video_finale.flv

#### 13.将.avi转成dv

ffmpeg -i video_origine.avi -s pal -r pal -aspect 4:3 -ar 48000 -ac 2 video_finale.dv
或者：
ffmpeg -i video_origine.avi -target pal-dv video_finale.dv

#### 14.将.avi压缩成divx

ffmpeg -i video_origine.avi -s 320x240 -vcodec msmpeg4v2 video_finale.avi

#### 15.将Ogg Theora压缩成Mpeg dvd

ffmpeg -i film_sortie_cinelerra.ogm -s 720x576 -vcodec mpeg2video -acodec mp3 film_terminate.mpg

#### 16.将.avi压缩成SVCD mpeg2

NTSC格式：
ffmpeg -i video_origine.avi -target ntsc-svcd video_finale.mpg
PAL格式：
ffmpeg -i video_origine.avi -target pal-svcd video_finale.mpg

#### 17.将.avi压缩成VCD mpeg2

NTSC格式：
ffmpeg -i video_origine.avi -target ntsc-vcd video_finale.mpg
PAL格式：
ffmpeg -i video_origine.avi -target pal-vcd video_finale.mpg

#### 18.多通道编码

ffmpeg -i fichierentree -pass 2 -passlogfile ffmpeg2pass fichiersortie-2

#### 19.从flv提取mp3

ffmpeg -i source.flv -ab 128k dest.mp3

#### 20.多个mp3文件合并成一个mp3文件

​	**一种方法是连接到一起**

​	ffmpeg64.exe -i "concat:123.mp3|124.mp3" -acodec copy output.mp3

​	解释：-i代表输入参数

​     contact:123.mp3|124.mp3代表着需要连接到一起的音频文件

​      -acodec copy output.mp3 重新编码并复制到新文件中

​	**另一种方法是混合到一起**

​	ffmpeg64.exe -i 124.mp3 -i 123.mp3 -filter_complex amix=inputs=2:duration=first:dropout_transition=2 -f mp3 remix.mp3

​	解释：-i代表输入参数

​      -filter_complex ffmpeg滤镜功能，非常强大，详细请[查看文档](http://ffmpeg.org/ffmpeg.html)

​      amix是混合多个音频到单个音频输出

​      inputs=2代表是2个音频文件，如果更多则代表对应数字

​      duration 确定最终输出文件的长度

​        longest(最长)|shortest（最短）|first（第一个文件）

​      dropout_transition

​	The transition time, in seconds, for volume renormalization when an input stream ends. The default value is 2 seconds.

​      -f mp3  输出文件格式

#### 21.音频文件截取指定时间部分

ffmpeg64.exe -i 124.mp3 -vn -acodec copy -ss 00:00:00 -t 00:01:32 output.mp3

解释：-i代表输入参数

​     -acodec copy output.mp3 重新编码并复制到新文件中

​      -ss 开始截取的时间点

​      -t 截取音频时间长度

#### 22.音频文件格式转换

ffmpeg64.exe -i null.ape -ar 44100 -ac 2 -ab 16k -vol 50 -f mp3 null.mp3

解释：-i代表输入参数

​      -acodec aac（音频编码用AAC） 

​     -ar 设置音频采样频率

​     -ac  设置音频通道数

​     -ab 设定声音比特率

​      -vol <百分比> 设定音量



相关文章：

1.https://blog.csdn.net/king1425/article/details/70597642/

