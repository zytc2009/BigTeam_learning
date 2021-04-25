[toc]

## WebRTC网络知识

### 1、NAT(Network Address Translator)

**NAT的工作原理：**

<img src="image\NAT.png" height="300" align="left"/>

其实就是一个地址的映射，上图左半部分是几台内网机器，有自己的内网IP，他们之间可以通讯，但与互联网没法通讯 ，因此需要NAT进行转换。如11.0.25.23转换成122.35.10.11，每台内网主机实际要映射成不同的端口，IP不变。

**NAT产生的原因**
1，IPv4的地址不够
2，出于网络安全的原因

**NAT种类**
1，完全锥型NAT(Full Cone NAT)
      当内网中的某一台主机经过NAT的映射后形成外网IP地址和端口号，外网的所有主机只要知道这个地址就都可以向这台主机发送数据,安全性低；
2，地址限制性锥型NAT(Address Restricted Cone NAT)
      对出去请求的时候会对出去的IP地址做一些限制，当回来的时候只有出去的IP地址主机才能回消息，也就是我没有向你发送请求你向我发数据是不允许的；
3，端口限制锥形NAT(Port Restricted Cone NAT)
      在IP地址限制的基础上又增加了端口的限制，给某台主机的某个应用发送，也就是只有有个应用或者说这个端口回来的数据才会接收；
4，对称型NAT(Symmetric NAT)
      当经过NAT转换的时候，内网的主机出外网的时候形成的映射并不是一个，而是多个IP地址和端口，也就是说访问不同的主机他会形成自己的IP地址和端口，这就更加严格，你想知道我的IP地址都很困难。我请求A出去的是一个IP地址和端口，A告诉B有一个内网他的映射地址是这个，通过这个地址访问的时候实际是不能访问的，因为如果内网的主机与第三个主机进行连接的时候会新建一个IP地址和端口，基本不能穿越。

**NAT穿越的原理**
下边对每一种NAT类型的穿越原理进行介绍。
1，完全锥型NAT
<img src="image\完全锥NAT.png" height="300" align="left"/>

















   左边是内网自己的IP地址和端口X,y，通过防火墙转换之后形成外网的IP地址和端口A,b，外边的三台主机要想与内网的主机进行通讯的时候，首先要由内网的主机向外请求，请求外网中的某一台主机，这样会在NAT上打一个洞，形成一个外网的IP地址和端口，然后其他的主机只要获得了这个IP地址和端口都可以通过防火墙与内网主机进行通讯。
2，地址限制锥形  

<img src="image\地址限制锥NAT.png" height="300" align="left"/>

  会在防火墙上形成一个五元组：内网主机的IP地址和端口，外网映射后的公网IP地址和端口以及要请求的主机的IP地址和端口。同样的第一步要由内网向外网发送请求，在NAT上形成一个映射表，外网主机就可以和内网主机通讯。如图可以看到，向P这个主机发送请求，P可以通过不同的应用或者说端口向内网主机发送消息，但对于其他主机，由于IP地址的限制只有P这台主机的所有应用可以通讯
3，端口限制锥形

<img src="image\端口限制锥NAT.png" height="300" align="left"/>

 更加严格，不仅对IP地址限制，对端口也有限制，如图请求P这台主机的q端口，那么只有这个主机的这个端口可以进行通讯。
4，对称型NAT

<img src="image\对称型NAT.png" height="300" align="left"/>

前边三个在我防火墙上形成的公网IP地址和端口时不变的，想要访问的主机要找是能找到的(虽然有的通有的不通)，而对称型会形成多个公网地址，对于每一台主机都会形成不同的IP地址和端口，如图，M,n只有向C,d发送才能成功，向A,b发送就没法通讯。
基本步骤：
1，C1，C2向STUN发消息
2，交换公网IP及端口
3，C1->C2，C2->C1，甚至端口猜测
        想要穿越C1和C2都要知道彼此的地址，就要向STUN服务发消息，STUN服务拿到他们地址后就能拿到他们对应的IP和端口，然后进行交换公网的IP和端口信息，然后按照类型进行打通。

**NAT穿越组合**
<img src="image\NAT穿越组合.png" height="400" align="left"/>























**NAT类型判断**

<img src="image\NAT类型判断.png" height="300" align="left"/>

<img src="image\NAT类型检测.png" height="300" align="left"/>

### 2、STUN(Simple Tranversal of UDP Through NAT)

STUN介绍

STUN存在的目的就是进行NAT穿越
STUN是典型的客户端/服务器模式。客户端发送请求，服务端进行响应
RFC STUN规范

RFC3489/STUN
Simple Traversal of UDP Through NAT       通过UDP进行穿越
RFC5389/STUN
Session Traversal Utilities for NAT    一系列穿越NAT的工具，包括UDP和TCP两套穿越方式
STUN协议

消息头，包括20字节的STUN header
Body中可以有0个或多个Attribute
消息头有哪些组成呢：
2个字节(16bit)类型     2个字节消息长度，不包括消息头本身    16个字节事务ID，请求与响应事务ID相同
消息头的格式：
<img src="image\STUN_header格式.png" height="300" align="left"/>

















下边详细介绍一下header中的这些字段的具体含义。
**STUN Message Type(消息类型，2字节):**

- 前两位必须是00，以区分复用同一端口时STUN协议
- 2位用于分类，即C0和C1
- 12位用于定义请求/指示        

<img src="image\STUN_Message_type.png" height="200" align="left"/>

**C0C1**

- **0b00:表示是一个请求**
- **0b01:表示是一个指示**
- **0b10:表示是请求成功的响应**
- **0b11:表示是请求失败的响应**

<img src="image\STUN消息类型.png" height="200" align="left"/> 









<img src="image\STUN_Message_类型判断.png" height="200" align="left"/>

**大小端模式：**

大端模式：数据的高字节保存在内存的低地址中
小端模式：数据的高字节保存在内存的高地址中
网络字节顺序：采用大端排序方式
Message Length:消息长度,2字节

Magic Cookie:4字节，固定魔法数，看到这个魔法数就可以判断为STUN消息

Transaction ID:

4字节，32位，固定值0x2112A442，通过它可以判断客户端是否可以识别某些属性
12字节，96位，标识同一个事务的请求和响应
SRUN Message Body:

消息头后有-或多个属性
每个属性进行TLV编码：Type，Length，Value   

<img src="image\STUN_TLV.png" width="400" align="left"/>

<img src="image\RFC3489定义的属性.png" width="500" align="left"/>

<img src="image\STUN属性的使用.png" width="500" align="left"/>

### 3、TURN(Traversal Using Relays around NAT)

TURN介绍

目的是解决对称NAT无法穿越的问题，遇到对称型等无法穿越时将数据传给TURN服务，由TURN服务中介转给其他接收者，或者其他接收者发送数据给TURN服务，转送给client端
建立在STUN之上，消息格式使用STUN格式消息，协议头和body基本一样，外壳形式一样内容有所不同
TRUN Client要去服务端分配一个公共IP和Port用于接收或发送数据 
<img src="image\TURN例子.png" width="500" align="left"/>





















<img src="image\TURN传输协议.png" width="400" align="left"/>

<img src="image\TURN_send_data.png" width="500" align="left"/>

<img src="image\TURN_channel.png" width="500" align="left"/>



总结一下使用流程：

STUN binding
Caller TURN allocation
Caller sends offer
Callee TURN allocation
Callee answers OK
Exchage candidate IP address
ICE check for P2P connection
If P2P unsuccessful, make relay connection

### 4、ICE(Interactive Connectivity Establishment)

什么是ICE？
       ICE，Interactive Connectivity Establishment，交互式连通建立方式，并非一种新的协议，它不需要对STUN、TURN或RSIP进行扩展就可适用于各种NAT，需要两端进行交互才能创建连接。
       ICE是通过综合运用上面某几种协议，使之在最适合的情况下工作，以弥补单独使用其中任何一种所带来的固有缺陷。
对于SIP来说，ICE只需要定义一些SDP(Session Description Protocol)附加属性即可，对于别的多媒体信令协议也需要制定一些相应的机制来实现。

两个端之间通过ICE建立交互连接的过程：

<img src="image\ICE建立交互连接的过程.png" width="500" align="left"/>

首先两个终端Peer都知道自己的外网IP地址，那如何知道另一方的地址呢，想要建立连接，就需要通过信令服务器转发彼此信息，交换IP地址和端口建立连接。这里涉及到几个核心概念：

ICE Candiate:

1. 每个Candiate是一个地址
   每个候选者包括：协议、IP、端口、类型
   例如：a = Candiate:...UDP ...192.168.1.2 1816 type host
2. Candiate类型
   主机候选者：电脑本地的IP地址和端口
   反射候选者：穿越NAT后映射的外网地址
   中继候选者：进行NAT穿越很多时候是穿越不成功的，这时候需要服务器进行中转形成的地址       <img src="image\Candiate关系图.png" width="300" align="left"/>





















3. 收集Candiate
   Host Candiate：本机所有IP和指定端口
   Reflexive Candiate：映射的Candiate通过STUN/TURN服务获取  
   Relay Candiate：向中继服务器发请求，拿到中继地址，通过TURN协议获取

**ICE具体做些什么**

收集Candiate
对Candiate Pair排序   进行Candiate交换
连通性检查    发送侦测包进行检测

<img src="image\ICE时序图.png" width="500" align="left"/>

<img src="image\ICE示例.png" width="500" align="left"/>

**ICE技术**
      基于IP的语音、数据、视频等业务在NGN（Next Generation Network）网络中所面临的一个实际困难就是如何有效地穿透各种NAT（Network Address Translator）/FW(Fire Wall)的问题。对此，SIP（会话初始化协议）以往的解决方法ALGs（(Application Layer Gateway Service)）、STUN、TURN等方式。
      现在有一种新的媒体会话信令穿透NAT/FW的解决方案-交互式连通建立方式ICE。它通过综合利用现有协议，以一种更有效的方式来组织会话建立过程，使之在不增加任何延迟同时比STUN等单一协议更具有健壮性、灵活性。多媒体会话信令协议是在准备建立媒体流传输的代理之间交互信息的协议，例如SIP、RTSP（real time streaming protocol）等。

### webrtc的整体交互过程

<img src="image\webrtc的整体交互过程.png" width="500" align="left"/>



## 加解密

### 1.基本概念

**什么是加密技术**?
       加密技术是电子商务采取的主要安全保密措施，是最常用的安全保密手段,利用技术手段把重要的数据变为乱码（加密）传送，到达目的地后再用相同或不同的手段还原（解密）。加密技术包括两个元素：算法和密钥。算法是将普通的文本（或者可以理解的信息）与一串数字（密钥）的结合，产生不可理解的密文的步骤，密钥是用来对数据进行编码和解码的一种算法。在安全保密中，可通过适当的密钥加密技术和管理机制来保证网络的信息通讯安全。密钥加密技术的密码体制分为对称密钥体制和非对称密钥体制两种。相应地，对数据加密的技术分为两类，即对称加密（私人密钥加密）和非对称加密（公开密钥加密）。对称加密以数据加密标准（DES，Data Encryption Standard）算法为典型代表，非对称加密通常以RSA（Rivest Shamir Ad1eman）算法为代表。对称加密的加密密钥和解密密钥相同，而非对称加密的加密密钥和解密密钥不同，加密密钥可以公开而解密密钥需要保密。

**什么是对称加密技术**?
       对称加密采用了对称密码编码技术，它的特点是文件加密和解密使用相同的密钥，即加密密钥也可以用作解密密钥，这种方法在密码学中叫做对称加密算法，对称加密算法使用起来简单快捷，密钥较短，且破译困难，除了数据加密标准（DES），另一个对称密钥加密系统是国际数据加密算法（IDEA），它比DES的加密性好，而且对计算机功能要求也没有那么高。IDEA加密标准由PGP（Pretty Good Privacy）系统使用。

**什么是非对称加密技术**?
       1976年，美国学者Dime和Henman为解决信息公开传送和密钥管理问题，提出一种新的密钥交换协议，允许在不安全的媒体上的通讯双方交换信息，安全地达成一致的密钥，这就是“公开密钥系统”。相对于“对称加密算法”这种方法也叫做“非对称加密算法”。与对称加密算法不同，非对称加密算法需要两个密钥：公开密钥（publickey）和私有密 （privatekey）。公开密钥与私有密钥是一对，如果用公开密钥对数据进行加密，只有用对应的私有密钥才能解密；如果用私有密钥对数据进行加密，那么只有用对应的公开密钥才能解密。因为加密和解密使用的是两个不同的密钥，所以这种算法叫作非对称加密算法。

**什么是数字签名**？
       数字签名（又称公钥数字签名）是只有信息的发送者才能产生的别人无法伪造的一段数字串，这段数字串同时也是对信息的发送者发送信息真实性的一个有效证明。它是一种类似写在纸上的普通的物理签名，但是使用了公钥加密领域的技术来实现的，用于鉴别数字信息的方法。一套数字签名通常定义两种互补的运算，一个用于签名，另一个用于验证。数字签名是非对称密钥加密技术与数字摘要技术的应用。

### 2、SSL

SSL是Secure Sockets Layer（安全套接层协议）的缩写，可以在Internet上提供秘密性传输。Netscape公司在推出第一个Web浏览器的同时，提出了SSL协议标准。其目标是保证两个应用间通信的保密性和可靠性,可在服务器端和用户端同时实现支持。已经成为Internet上保密通讯的工业标准。
        SSL能使用户/服务器应用之间的通信不被攻击者窃听，并且始终对服务器进行认证，还可选择对用户进行认证。SSL协议要求建立在可靠的传输层协议(TCP)之上。SSL协议的优势在于它是与应用层协议独立无关的，高层的应用层协议(例如：HTTP，FTP，TELNET等)能透明地建立于SSL协议之上。SSL协议在应用层协议通信之前就已经完成加密算法、通信密钥的协商及服务器认证工作。在此之后应用层协议所传送的数据都会被加密，从而保证通信的私密性。
        SSL是利用公开密钥的加密技术（RSA）来作为用户端与服务器端在传送机密资料时的加密通讯协定。SSL是利用公开密钥的加密技术（RSA）来作为用户端与服务器端在传送机密资料时的加密通讯协定。

### 3、OpenSSL

OpenSSL实际上就是开源的SSL，它可以实现：秘钥证书管理、对称加密和非对称加密 。

```
1. openssl: 多用途的命令行工具，包openssl，可以执行交互或批量命令。
2. libcrypto: 加密算法库，包openssl-libs。
3. libssl：加密模块应用库，实现了ssl及tls，包nss。
```

 **加密命令**

```
工具： openssl enc, gpg
算法： 3des, aes, blowfish, twofish、3des等。
常用选项有：
-in filename：指定要加密的文件存放路径
-out filename：指定加密后的文件存放路径
-salt：自动插入一个随机数作为文件内容加密，默认选项 加点盐：）
-e：加密；
-d：解密，解密时也可以指定算法，若不指定则使用默认算法，但一定要与加密时的算法一致
-a/-base64：当进行加解密时，他只对数据进行运算，有时需要进行base64转换，设置
此选项后加密结果进行base64编码，解密前先进行base64编码。
加密解密算法可以通过自己制定，有什么算法可以通过openssl help enc去查看加密内容。
enc命令：
帮助： man enc
加密：
openssl enc -e -des3 -a -salt -in testfile -out testfile.cipher
解密：
openssl enc -d -des3 -a -salt –in testfile.cipher -out testfile
openssl ?

```

**对称加密算法**
       OpenSSL一共提供了8种对称加密算法，其中7种是分组加密算法，仅有的一种流加密算法是RC4。这7种分组加密算法分别是AES、DES、Blowfish、CAST、IDEA、RC2、RC5，都支持电子密码本模式（ECB）、加密分组链接模式（CBC）、加密反馈模式（CFB）和输出反馈模式（OFB）四种常用的分组密码加密模式。其中，AES使用的加密反馈模式（CFB）和输出反馈模式（OFB）分组长度是128位，其它算法使用的则是64位。事实上，DES算法里面不仅仅是常用的DES算法，还支持三个密钥和两个密钥3DES算法。

**非对称加密算法**
        OpenSSL一共实现了4种非对称加密算法，包括DH算法、RSA算法、DSA算法和椭圆曲线算法（EC）。DH算法一般用户密钥交换。RSA算法既可以用于密钥交换，也可以用于数字签名，当然，如果你能够忍受其缓慢的速度，那么也可以用于数据加密。DSA算法则一般只用于数字签名。

<img src="image\openssl使用.png" width="300" align="left"/>

### 4、TLS

 传输层安全性协议（英语：Transport Layer Security，缩写作TLS），及其前身安全套接层（Secure Sockets Layer，缩写作SSL）是一种安全协议，目的是为互联网通信提供安全及数据完整性保障。该协议由两层组成： TLS 记录协议（TLS Record）和 TLS 握手协议（TLS Handshake）。
       TLS协议采用主从式架构模型，用于在两个应用程序间透过网络创建起安全的连线，防止在交换数据时受到窃听及篡改。             TLS协议的优势是与高层的应用层协议（如HTTP、FTP、Telnet等）无耦合。应用层协议能透明地运行在TLS协议之上，由TLS协议进行创建加密通道需要的协商和认证。应用层协议传送的数据在通过TLS协议时都会被加密，从而保证通信的私密性。         TLS协议是可选的，必须配置客户端和服务器才能使用。主要有两种方式实现这一目标：一个是使用统一的TLS协议通信端口（例如：用于HTTPS的端口443）；另一个是客户端请求服务器连接到TLS时使用特定的协议机制（例如：邮件、新闻协议和STARTTLS）。一旦客户端和服务器都同意使用TLS协议，他们通过使用一个握手过程协商出一个有状态的连接以传输数据。通过握手，客户端和服务器协商各种参数用于创建安全连接：

当客户端连接到支持TLS协议的服务器要求创建安全连接并列出了受支持的密码组合（加密密码算法和加密哈希函数），握手开始。

服务器从该列表中决定加密和散列函数，并通知客户端。

服务器发回其数字证书，此证书通常包含服务器的名称、受信任的证书颁发机构（CA）和服务器的公钥。

客户端确认其颁发的证书的有效性。

为了生成会话密钥用于安全连接，客户端使用服务器的公钥加密随机生成的密钥，并将其发送到服务器，只有服务器才能使用自己的私钥解密。

利用随机数，双方生成用于加密和解密的对称密钥。这就是TLS协议的握手，握手完毕后的连接是安全的，直到连接（被）关闭。如果上述任何一个步骤失败，TLS握手过程就会失败，并且断开所有的连接。

### 5、DTLS协议

DTLS简介
       简单说，DTLS（Datagram Transport Layer Security）实现了在UDP协议之上的TLS安全层。由于基于TCP的SSL/TLS没有办法处理UDP报文的丢包及重排序（这些问题一般交给UDP的上层应用解决），DTLS在原本TLS的基础上做了一些小改动（复用大部分TLS的代码）来解决如下UDP上实现TLS的问题：

TLS记录层内记录的强关联性及无序号
握手协议的可靠性
包丢失重传机制（UDP无重传机制）
无法按序接收（握手需要对包按顺序处理，而UDP包的到达并非按序，包头没有TCP那样的Seq/Ack number）
握手协议包长（证书之类传输可能达到KB级别）导致的UDP分包组包（类似于UDP在IP层的分包）
重复包（Replay）检测
TLS是基于TCP协议的
DTLS基于UDP协议的
OpenSSL实现了TLS和DTLS两种都有
<img src="image\DTLS握手协议.png" width="400" align="left"/>





















<img src="image\DTLS握手过程.png" width="400" align="left"/>

<img src="image\DTLS时序图.png" width="350" align="left"/>



### 6、SRTP

​       SRTP(SecureReal-time Transport Protocol) 安全实时传输协议，SRTP是在实时传输协议(Real-time Transport Protocol)基础上所定义的一个协议，旨在为单播和多播应用程序中的实时传输协议的数据提供加密、消息认证、完整性保证和重放保护安全实时传输协议。
​      SRTP使用身份验证和加密，以最大程度地降低诸如拒绝服务之类的攻击风险。 它由IETF（Internet工程任务组）于2004年发布，名称为RFC3711。SRTP就像DTLS是用于WebRTC技术的安全协议之一。

<img src="image\SRTP握手过程.png" width="350" align="left"/>



## 流媒体协议

### RTP：实时传输协议（Real-time Transport Protocol） 

RTP是一种基于包的传输协议，它用来传输实时数据。在网络上传输数据包的延迟和误差是不可避免的，对此RTP包头包含时间戳、丢失保护、载荷标识、源标识和安全性信息。这些信息用于在应用层实现数据包丢失恢复、拥塞控制等。
        RTP通常运行于UDP的上层，以利用UDP的复用和求和校验功能。RTP是在两个主机之间提供基于连接的、稳定的数据流，而UDP是在网络上提供一种非连接数据报服务。如图是采用了UDP/IP包封装的RTP包

<img src="image\RTP包格式.png" width="350" align="left"/>

 	Real-time Transport Protocol)是用于Internet上针对多媒体数据流的一种传输层协议。RTP协议详细说明了在互联网上传递音频和视频的标准数据包格式。RTP协议常用于流媒体系统（配合RTCP协议），视频会议和一键通（Push to Talk）系统（配合H.323或SIP），使它成为IP电话产业的技术基础。RTP协议和RTP控制协议RTCP一起使用，而且它是建立在UDP协议上的。 

​	RTP 本身并没有提供按时发送机制或其它服务质量（QoS）保证，它依赖于低层服务去实现这一过程。 RTP 并不保证传送或防止无序传送，也不确定底层网络的可靠性。 RTP 实行有序传送， RTP 中的序列号允许接收方重组发送方的包序列，同时序列号也能用于决定适当的包位置，例如：在视频解码中，就不需要顺序解码。

​	RTP 由两个紧密链接部分组成： RTP ― 传送具有实时属性的数据；RTP 控制协议（RTCP） ― 监控服务质量并传送正在进行的会话参与者的相关信息。

### RTCP

​        实时传输控制协议（Real-time Transport Control Protocol或RTP Control Protocol或简写RTCP）是实时传输协议（RTP）的一个姐妹协议。RTCP为RTP媒体流提供信道外（out-of-band）控制。RTCP本身并不传输数据，但和RTP一起协作将多媒体数据打包和发送。RTCP定期在流多媒体会话参加者之间传输控制数据。RTCP的主要功能是为RTP所提供的服务质量（Quality of Service）提供反馈。

RTCP收集相关媒体连接的统计信息，例如：传输字节数，传输分组数，丢失分组数，jitter，单向和双向网络延迟等等。网络应用程序可以利用RTCP所提供的信息试图提高服务质量，比如限制信息流量或改用压缩比较小的编解码器。RTCP本身不提供数据加密或身份认证。SRTCP可以用于此类用途。

### SRTP & SRTCP

​       安全实时传输协议（Secure Real-time Transport Protocol或SRTP）是在实时传输协议（Real-time Transport Protocol或RTP）基础上所定义的一个协议，旨在为单播和多播应用程序中的实时传输协议的数据提供加密、消息认证、完整性保证和重放保护。它是由David Oran（思科）和Rolf Blom（爱立信）开发的，并最早由IETF于2004年3月作为RFC3711发布。

  由于实时传输协议和可以被用来控制实时传输协议的会话的实时传输控制协议（RTP Control Protocol或RTCP）有着紧密的联系，安全实时传输协议同样也有一个伴生协议，它被称为安全实时传输控制协议（Secure RTCP或SRTCP）；安全实时传输控制协议为实时传输控制协议提供类似的与安全有关的特性，就像安全实时传输协议为实时传输协议提供的那些一样。
    在使用实时传输协议或实时传输控制协议时，使不使用安全实时传输协议或安全实时传输控制协议是可选的；但即使使用了安全实时传输协议或安全实时传输控制协议，所有它们提供的特性（如加密和认证）也都是可选的，这些特性可以被独立地使用或禁用。唯一的例外是在使用安全实时传输控制协议时，必须要用到其消息认证特性

### RTSP

​       是由Real Networks和Netscape共同提出的。该协议定义了一对多应用程序如何有效地通过IP网络传送多媒体数据。RTSP提供了一个可扩展框架，使实时数据，如音频与视频的受控、点播成为可能。数据源包括现场数据与存储在剪辑中的数据。该协议目的在于控制多个数据发送连接，为选择发送通道，如UDP、多播UDP与TCP提供途径，并为选择基于RTP上发送机制提供方法。

​     RTSP（Real Time Streaming Protocol）是用来控制声音或影像的多媒体串流协议，并允许同时多个串流需求控制，传输时所用的网络通讯协定并不在其定义的范围内，服务器端可以自行选择使用TCP或UDP来传送串流内容，它的语法和运作跟HTTP 1.1类似，但并不特别强调时间同步，所以比较能容忍网络延迟。而前面提到的允许同时多个串流需求控制（Multicast），除了可以降低服务器端的网络用量，更进而支持多方视讯会议（Video Conference）。 因为与HTTP1.1的运作方式相似，所以代理服务器《Proxy》的快取功能《Cache》也同样适用于RTSP，并因RTSP具有重新导向功能，可视实际负载情况来转换提供服务的服务器，以避免过大的负载集中于同一服务器而造成延迟。

<img src="image\RTSP.png" width="400" align="left"/>

RTSP 和RTP的关系
       RTP不象http和ftp可完整的下载整个影视文件，它是以固定的数据率在网络上发送数据，客户端也是按照这种速度观看影视文件，当影视画面播放过后，就不可以再重复播放，除非重新向服务器端要求数据。

  RTSP与RTP最大的区别在于：RTSP是一种双向实时数据传输协议，它允许客户端向服务器端发送请求，如回放、快进、倒退等操作。当然，RTSP可基于RTP来传送数据，还可以选择TCP、UDP、组播UDP等通道来发送数据，具有很好的扩展性。它时一种类似与http协议的网络应用层协议。目前碰到的一个应用：服务器端实时采集、编码并发送两路视频，客户端接收并显示两路视频。由于客户端不必对视频数据做任何回放、倒退等操作，可直接采用UDP+RTP+组播实现。

RTSP和HTTP的比较
         RTSP和HTTP所提供的的服务相同，知识RTSP是以音视频流的形式，HTTP以文本和图形的形式。

​        不同之处主要表现在两个地：1，RTSP兼容的视频服务器必须维持会话状态，以将RTSP请求和流关联起来
​               2，从本质上来说HTTP是一个不对称协议(客户端发出请求，服务器响应)，但在RTSP协议中客户端和服务器都可以发出请求

<img src="image\RTSP和HTTP的比较.png" width="200" align="left"/>

### SDP

​       会话描述协议（SDP:Session Description Protocol）为会话通知、会话邀请和其它形式的多媒体会话初始化等目的提供了多媒体会话描述。

   会话目录用于协助多媒体会议的通告，并为会话参与者传送相关设置信息。SDP 即用于将这种信息传输到接收端。SDP 完全是一种会话描述格式 ― 它不属于传输协议 ― 它只使用不同的适当的传输协议，包括会话通知协议（SAP）、会话初始协议（SIP）、实时流协议（RTSP）、MIME 扩展协议的电子邮件以及超文本传输协议（HTTP）。

SDP 的设计宗旨是通用性，它可以应用于大范围的网络环境和应用程序，而不仅仅局限于组播会话目录，但 SDP 不支持会话内容或媒体编码的协商。
在因特网组播骨干网（Mbone）中，会话目录工具被用于通告多媒体会议，并为参与者传送会议地址和参与者所需的会议特定工具信息，这由 SDP 完成。SDP 连接好会话后，传送足够的信息给会话参与者。SDP 信息发送利用了会话通知协议（SAP），它周期性地组播通知数据包到已知组播地址和端口处。这些信息是 UDP 数据包，其中包含 SAP 协议头和文本有效载荷（text payload）。这里文本有效载荷指的是 SDP 会话描述。此外信息也可以通过电子邮件或 WWW （World Wide Web） 进行发送。

SDP 文本信息包括：

会话名称和意图；
会话持续时间；
构成会话的媒体；
有关接收媒体的信息（地址等）。
协议结构
SDP 信息是文本信息，采用 UTF-8 编 码中的 ISO 10646 字符集。SDP 会话描述如下：（标注 * 符号的表示可选字段）：
v = （协议版本）
o = （所有者/创建者和会话标识符）
s = （会话名称）
i = * （会话信息）
u = * （URI 描述）
e = * （Email 地址）
p = * （电话号码）
c = * （连接信息 ― 如果包含在所有媒体中，则不需要该字段）
b = * （带宽信息）

一个或更多时间描述（如下所示）：
z = * （时间区域调整）
k = * （加密密钥）
a = * （0 个或多个会话属性行）
0个或多个媒体描述（如下所示）

时间描述
t = （会话活动时间）
r = * （0或多次重复次数）

媒体描述
m = （媒体名称和传输地址）
i = * （媒体标题）
c = * （连接信息 — 如果包含在会话层则该字段可选）
b = * （带宽信息）
k = * （加密密钥）
a = * （0 个或多个会话属性行）

### RTMP/RTMPS

​        RTMP(Real Time Messaging Protocol)实时消息传送协议是Adobe Systems公司为Flash播放器和服务器之间音频、视频和数据传输 开发的开放协议。

它有三种变种：

1)工作在TCP之上的明文协议，使用端口1935；

2)RTMPT封装在HTTP请求之中，可穿越防火墙；

3)RTMPS类似RTMPT，但使用的是HTTPS连接；

  RTMP协议(Real Time Messaging Protocol)是被Flash用于对象,视频,音频的传输.这个协议建立在TCP协议或者轮询HTTP协议之上.
  RTMP协议就像一个用来装数据包的容器,这些数据既可以是AMF格式的数据,也可以是FLV中的视/音频数据.一个单一的连接可以通过不同的通道传输多路网络流.这些通道中的包都是按照固定大小的包传输的.

### MMS

​        MMS (Microsoft Media Server Protocol)，中文“微软媒体服务器协议”，用来访问并流式接收 Windows Media 服务器中 .asf 文件的一种协议。MMS 协议用于访问 Windows Media 发布点上的单播内容。MMS 是连接 Windows Media 单播服务的默认方法。若观众在 Windows Media Player 中键入一个 URL 以连接内容，而不是通过超级链接访问内容，则他们必须使用MMS 协议引用该流。MMS的预设埠（端口）是1755

当使用 MMS 协议连接到发布点时，使用协议翻转以获得最佳连接。“协议翻转”始于试图通过 MMSU 连接客户端。 MMSU 是 MMS 协议结合 UDP 数据传送。如果 MMSU 连接不成功，则服务器试图使用 MMST。MMST 是 MMS 协议结合 TCP 数据传送。

如果连接到编入索引的 .asf 文件，想要快进、后退、暂停、开始和停止流，则必须使用 MMS。不能用 UNC 路径快进或后退。若您从独立的 Windows Media Player 连接到发布点，则必须指定单播内容的 URL。若内容在主发布点点播发布，则 URL 由服务器名和 .asf 文件名组成。例如：mms://windows_media_server/sample.asf。其中 windows_media_server 是 Windows Media 服务器名，sample.asf 是您想要使之转化为流的 .asf 文件名。
若您有实时内容要通过广播单播发布，则该 URL 由服务器名和发布点别名组成。例如：mms://windows_media_server/LiveEvents。这里 windows_media_server 是 Windows Media 服务器名，而 LiveEvents 是发布点名

### HLS

​        HTTP Live Streaming（HLS）是苹果公司(Apple Inc.)实现的基于HTTP的流媒体传输协议，可实现流媒体的直播和点播，主要应用在iOS系统，为iOS设备（如iPhone、iPad）提供音视频直播和点播方案。HLS点播，基本上就是常见的分段HTTP点播，不同在于，它的分段非常小。

​       相对于常见的流媒体直播协议，例如RTMP协议、RTSP协议、MMS协议等，HLS直播最大的不同在于，直播客户端获取到的，并不是一个完整的数据流。HLS协议在服务器端将直播数据流存储为连续的、很短时长的媒体文件（MPEG-TS格式），而客户端则不断的下载并播放这些小文件，因为服务器端总是会将最新的直播数据生成新的小文件，这样客户端只要不停的按顺序播放从服务器获取到的文件，就实现了直播。由此可见，基本上可以认为，HLS是以点播的技术方式来实现直播。由于数据通过HTTP协议传输，所以完全不用考虑防火墙或者代理的问题，而且分段文件的时长很短，客户端可以很快的选择和切换码率，以适应不同带宽条件下的播放。不过HLS的这种技术特点，决定了它的延迟一般总是会高于普通的流媒体直播协议。　

 根据以上的了解要实现HTTP Live Streaming直播，需要研究并实现以下技术关键点

- 采集视频源和音频源的数据
- 对原始数据进行H264编码和AAC编码
- 视频和音频数据封装为MPEG-TS包
- HLS分段生成策略及m3u8索引文件
- HTTP传输协议

最近打算直播上http-flv，之前用的是rtmp和hls。为什么使用http-flv,它有什么优缺点？

怎么让流媒体服务器支持flv直播？

### HTTP-FLV(HDL)

1、市场上哪家直播使用了http-flv:
通过抓包分析: 优酷的pc网页直播使用了http-flv。

斗鱼、熊猫tv、虎牙pc网页上的也使用了http-flv。

2、http-flv、rtmp和hls直播的优缺点:
A、三者的延迟性：
http-flv：低延迟，内容延迟可以做到2-5秒。
Rtmp：低延迟，内容延迟可以做到2-5秒。
Hls:：延迟较高。

B、三者的易用性:
rtmp和http-flv：播放端安装率高。只要浏览器支持FlashPlayer就能非常简易的播放。

hls：最大的优点：HTML5可以直接打开播放；这个意味着可以把一个直播链接通过微信
等转发分享，不需要安装任何独立的APP，有浏览器即可。

C、rtmp和http-flv比较：
(1) 穿墙：很多防火墙会墙掉RTMP，但是不会墙HTTP，因此HTTP FLV出现奇怪问题的概率很小。
(2) 调度：RTMP也有个302，可惜是播放器as中支持的，HTTP FLV流就支持302方便CDN纠正DNS的错误。
(3) 容错：SRS的HTTP FLV回源时可以回多个，和RTMP一样，可以支持多级热备。
(4) 简单：FLV是最简单的流媒体封装，HTTP是最广泛的协议，这两个组合在一起维护性更高，比RTMP简单多了。

3、http-flv技术实现:
HTTP协议中有个约定：content-length字段，http的body部分的长度
服务器回复http请求的时候如果有这个字段，客户端就接收这个长度的数据然后就认为数据传输完成了，
如果服务器回复http请求中没有这个字段，客户端就一直接收数据，直到服务器跟客户端的socket连接断开。

http-flv直播就是利用第二个原理，服务器回复客户端请求的时候不加content-length字段，在回复了http内容之后，紧接着发送flv数据，客户端就一直接收数据了。

## WebRTC核心之SDP详解、媒体协商

### 1.什么是SDP

    SDP(Session Description Protocol)描述会话协议，它只是一种信息格式的描述标准，本身不属于传输协议，但是可以被其他传输协议用来交换必要的信息，用于两个会话实体之间的媒体协商。
        SDP（Session Description Protocol）是一个用来描述多媒体会话的应用层控制协议，为会话通知、会话邀请和其它形式的多媒体会话初始化等目的提供了多媒体会话描述；它是一个基于文本的协议，这样就能保证协议的可扩展性比较强，这样就使其具有广泛的应用范围；SDP 完全是一种会话描述格式 ― 它不属于传输协议 ― 它只使用不同的适当的传输协议，包括会话通知协议（SAP）、会话初始协议（SIP）、实时流协议（RTSP）、MIME 扩展协议的电子邮件以及超文本传输协议（HTTP）。SDP 不支持会话内容或媒体编码的协商，所以在流媒体中只用来描述媒体信息。媒体协商这一块要用RTSP来实现。
会话目录用于协助多媒体会议的通告，并为会话参与者传送相关设置信息。SDP 即用于将这种信息传输到接收端。在因特网组播骨干网（Mbone）中，会话目录工具被用于通告多媒体会议，并为参与者传送会议地址和参与者所需的会议特定工具信息，这由 SDP 完成。SDP 连接好会话后，传送足够的信息给会话参与者。SDP 信息发送利用了会话通知协议（SAP），它周期性地组播通知数据包到已知组播地址和端口处。这些信息是 UDP 数据包，其中包含 SAP 协议头和文本有效载荷（text payload）。这里文本有效载荷指的是 SDP 会话描述。此外信息也可以通过电子邮件或 WWW （World Wide Web） 进行发送。SDP 文本信息包括：会话名称和意图； 会话持续时间； 构成会话的媒体； 有关接收媒体的信息（地址等）。

### 2、SDP协议结构

​       SDP描述由许多文本行组成，文本行的格式为<类型>=<值>，<类型>是一个字母，<值>是结构化的文本串，其格式依<类型>而定，<type> = <value>，每个SDP有一个会话级描述、多个媒体级描述。
​       SDP的文本信息包括：

会话的名称和目的 Session Description
v = （协议版本）
o = （所有者/创建者和会话标识符）
s = （会话名称）
i = * （会话信息）
u = * （URI 描述）
e = * （Email 地址）
p = * （电话号码）
c = * （连接信息 ― 如果包含在所有媒体中，则不需要该字段）
b = * （带宽信息）

会话存活时间   Time Description
t = （会话活动时间）
r = * （0或多次重复次数）
构成会话的媒体(会话中包括多个媒体)
SDP的媒体信息  Media Description
媒体格式
传输协议
传输IP和端口
媒体负载类型(VP8、VP9、H264、H265)
m = （媒体名称和传输地址）
i = * （媒体标题）
c = * （连接信息 — 如果包含在会话层则该字段可选）
b = * （带宽信息）
k = * （加密密钥）
a = * （0 个或多个会话属性行）

### 3、SDP实例

```
v=0
//sdp版本号，一直为0,rfc4566规定
o=- 7017624586836067756 2 IN IP4 127.0.0.1
//origion/owner  o=<username> <session id> <version> <network type> <address type> <unicast-address>
//username如何没有使用-代替，7017624586836067756是整个会话的编号，2代表会话版本，如果在会话
//过程中有改变编码之类的操作，重新生成sdp时,sess-id不变，sess-version加1
s=-
//会话名,必选，没有的话使用-代替
t=0 0
//两个值分别是会话的起始时间和结束时间，这里都是0代表没有限制
a=group:BUNDLE audio video data
//需要共用一个传输通道传输的媒体，如果没有这一行，音视频，数据就会分别单独用一个udp端口来发送
a=msid-semantic: WMS h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C
//WMS是WebRTC Media Stream简称，这一行定义了本客户端支持同时传输多个流，一个流可以包括多个track,
//一般定义了这个，后面a=ssrc这一行就会有msid,mslabel等属性
m=audio 9 UDP/TLS/RTP/SAVPF 111 103 104 9 0 8 106 105 13 126
//m = <media><port><transport><fmt/payload type list>
//m=audio说明本会话包含音频，9代表音频使用端口9来传输，但是在webrtc中一现在一般不使用，如果设置为0，代表不
//传输音频,UDP/TLS/RTP/SAVPF是表示用户来传输音频支持的协议，udp，tls,rtp代表使用udp来传输rtp包，并使用tls加密
//SAVPF代表使用srtcp的反馈机制来控制通信过程,后台111 103 104 9 0 8 106 105 13 126表示本会话音频支持的编码，后台几行会有详细补充说明
c=IN IP4 0.0.0.0
//这一行表示你要用来接收或者发送音频使用的IP地址，webrtc使用ice传输，不使用这个地址
a=rtcp:9 IN IP4 0.0.0.0
//用来传输rtcp地地址和端口，webrtc中不使用
a=ice-ufrag:khLS
a=ice-pwd:cxLzteJaJBou3DspNaPsJhlQ
//以上两行是ice协商过程中的安全验证信息
a=fingerprint:sha-256 FA:14:42:3B:C7:97:1B:E8:AE:0C2:71:03:05:05:16:8F:B9:C7:98:E9:60:43:4B:5B:2C:28:EE:5C:8F3:17
//以上这行是dtls协商过程中需要的认证信息
a=setup:actpass
//以上这行代表本客户端在dtls协商过程中，可以做客户端也可以做服务端，参考rfc4145 rfc4572
a=mid:audio
//在前面BUNDLE这一行中用到的媒体标识
a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level
//上一行指出我要在rtp头部中加入音量信息，参考 rfc6464
a=sendrecv
//上一行指出我是双向通信，另外几种类型是recvonly,sendonly,inactive
a=rtcp-mux
//上一行指出rtp,rtcp包使用同一个端口来传输
//下面几行都是对m=audio这一行的媒体编码补充说明，指出了编码采用的编号，采样率，声道等
a=rtpmap:111 opus/48000/2
//可选 a=rtpmap:<fmt/payload type><encoding name>/<clock rate>[/<encodingparameters>]
a=rtcp-fb:111 transport-cc
//以上这行说明opus编码支持使用rtcp来控制拥塞，参考https://tools.ietf.org/html/draft-holmer-rmcat-transport-wide-cc-extensions-01
a=fmtp:111 minptime=10;useinbandfec=1
//可选 a=fmtp:<fmt/payload type> parameters  对rtpmap进一步说明
//对opus编码可选的补充说明,minptime代表最小打包时长是10ms，useinbandfec=1代表使用opus编码内置fec特性
a=rtpmap:103 ISAC/16000
a=rtpmap:104 ISAC/32000
a=rtpmap:9 G722/8000
a=rtpmap:0 PCMU/8000
a=rtpmap:8 PCMA/8000
a=rtpmap:106 CN/32000
a=rtpmap:105 CN/16000
a=rtpmap:13 CN/8000
a=rtpmap:126 telephone-event/8000
a=ssrc:18509423 cname:sTjtznXLCNH7nbRw
//cname用来标识一个数据源，ssrc当发生冲突时可能会发生变化，但是cname不会发生变化，也会出现在rtcp包中SDEC中，
//用于音视频同步
a=ssrc:18509423 msid:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C 15598a91-caf9-4fff-a28f-3082310b2b7a
//以上这一行定义了ssrc和WebRTC中的MediaStream,AudioTrack之间的关系，msid后面第一个属性是stream-d,第二个是track-id
a=ssrc:18509423 mslabel:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C
a=ssrc:18509423 label:15598a91-caf9-4fff-a28f-3082310b2b7a
m=video 9 UDP/TLS/RTP/SAVPF 100 101 107 116 117 96 97 99 98
//参考上面m=audio,含义类似
c=IN IP4 0.0.0.0
a=rtcp:9 IN IP4 0.0.0.0
a=ice-ufrag:khLS
a=ice-pwd:cxLzteJaJBou3DspNaPsJhlQ
a=fingerprint:sha-256 FA:14:42:3B:C7:97:1B:E8:AE:0C2:71:03:05:05:16:8F:B9:C7:98:E9:60:43:4B:5B:2C:28:EE:5C:8F3:17
a=setup:actpass
a=mid:video
a=extmap:2 urn:ietf:params:rtp-hdrext:toffset
a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time
a=extmap:4 urn:3gpp:video-orientation
a=extmap:5 http://www.ietf.org/id/draft-hol ... de-cc-extensions-01
a=extmap:6 http://www.webrtc.org/experiments/rtp-hdrext/playout-delay
a=sendrecv
a=rtcp-mux
a=rtcp-rsize
a=rtpmap:100 VP8/90000
a=rtcp-fb:100 ccm fir
//ccm是codec control using RTCP feedback message简称，意思是支持使用rtcp反馈机制来实现编码控制，fir是Full Intra Request
//简称，意思是接收方通知发送方发送幅完全帧过来
a=rtcp-fb:100 nack
//支持丢包重传，参考rfc4585
a=rtcp-fb:100 nack pli
//支持关键帧丢包重传,参考rfc4585
a=rtcp-fb:100 goog-remb
//支持使用rtcp包来控制发送方的码流
a=rtcp-fb:100 transport-cc
//参考上面opus
a=rtpmap:101 VP9/90000
a=rtcp-fb:101 ccm fir
a=rtcp-fb:101 nack
a=rtcp-fb:101 nack pli
a=rtcp-fb:101 goog-remb
a=rtcp-fb:101 transport-cc
a=rtpmap:107 H264/90000
a=rtcp-fb:107 ccm fir
a=rtcp-fb:107 nack
a=rtcp-fb:107 nack pli
a=rtcp-fb:107 goog-remb
a=rtcp-fb:107 transport-cc
a=fmtp:107 level-asymmetry-allowed=1;packetization-mode=1;profile-level-id=42e01f
//h264编码可选的附加说明
a=rtpmap:116 red/90000
//fec冗余编码，一般如果sdp中有这一行的话，rtp头部负载类型就是116，否则就是各编码原生负责类型
a=rtpmap:117 ulpfec/90000
//支持ULP FEC，参考rfc5109
a=rtpmap:96 rtx/90000
a=fmtp:96 apt=100
//以上两行是VP8编码的重传包rtp类型
a=rtpmap:97 rtx/90000
a=fmtp:97 apt=101
a=rtpmap:99 rtx/90000
a=fmtp:99 apt=107
a=rtpmap:98 rtx/90000
a=fmtp:98 apt=116
a=ssrc-group:FID 3463951252 1461041037
//在webrtc中，重传包和正常包ssrc是不同的，上一行中前一个是正常rtp包的ssrc,后一个是重传包的ssrc
a=ssrc:3463951252 cname:sTjtznXLCNH7nbRw
a=ssrc:3463951252 msid:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C ead4b4e9-b650-4ed5-86f8-6f5f5806346d
a=ssrc:3463951252 mslabel:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C
a=ssrc:3463951252 label:ead4b4e9-b650-4ed5-86f8-6f5f5806346d
a=ssrc:1461041037 cname:sTjtznXLCNH7nbRw
a=ssrc:1461041037 msid:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C ead4b4e9-b650-4ed5-86f8-6f5f5806346d
a=ssrc:1461041037 mslabel:h1aZ20mbQB0GSsq0YxLfJmiYWE9CBfGch97C
a=ssrc:1461041037 label:ead4b4e9-b650-4ed5-86f8-6f5f5806346d
m=application 9 DTLS/SCTP 5000
c=IN IP4 0.0.0.0
a=ice-ufrag:khLS
a=ice-pwd:cxLzteJaJBou3DspNaPsJhlQ
a=fingerprint:sha-256 FA:14:42:3B:C7:97:1B:E8:AE:0C2:71:03:05:05:16:8F:B9:C7:98:E9:60:43:4B:5B:2C:28:EE:5C:8F3:17
a=setup:actpass
a=mid:data
a=sctpmap:5000 webrtc-datachannel 1024
```

### 4、WebRTC中的SDP

<img src="image\SDP组成.png" width="500" align="left"/>

<img src="image\SDP协商过程.png" width="400" align="left"/>

 整体过程简单分析下：
       SDP协商利用的是里请求和响应这两个模型（offer、answer）,Offerer发给Answerer的请求消息称为请求offer，内容包括媒体流类型、各个媒体流使用的编码集，以及将要用于接收媒体流的IP和端口。
Answerer收到offer之后，回复给Offerer的消息称为响应，内容包括要使用的媒体编码，是否接收该媒体流以及告诉Offerer其用于接收媒体流的IP和端口。
       Offer/Answer模型包括两个实体，一个是请求主体Offerer，另外一个是响应实体Answerer，两个实体只是在逻辑上进行区分，在一定条件可以转换。
       在WebRTC连接流程中，在创建PeerConnectionA后，就会去创建一个offerSDP，并设置为localSDP。通过signaling发送 PeerB。 peerB收到peerA的SDP后，把收到的SDP设置为RemoteSDP。在设置完成后，PeerB再生成AnswerSDP，设置为localSDP，通过signaling通道发送给PeerA，PeerA收到后AnswerSDP后，设置为RemoteSDP，以上流程完成了SDP的交换。

### 5、Webrtc媒体协商

媒体协商是为了保证交互双方通过交换信息来保证交互的正常进行，比如A用的是H264编码，通过协商告知B，B来判断自己是否可以进行相应的数据解析来确定是否可以进行交互通信。WebRTC默认情况下使用的V8引擎。

<img src="image\媒体协商过程.png" width="500" align="left"/>
















相关文章：

1.https://blog.csdn.net/xiaomucgwlmx/article/details/103204274









