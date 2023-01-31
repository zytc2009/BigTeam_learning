//基于Qt5.12学习

### 1. Qt项目文件：

  widget.h  widget.cpp main.cpp widget.ui

```c++
//Widget.h
namespace Ui {
class Widget; //只有一个按钮，btn_send
}
class Widget : public QWidget
{
    Q_OBJECT
public:
    explicit Widget(QWidget *parent = nullptr);
private slots:
  int on_btn_send_clicked();
signals:
  void sig_send(int);
private:
    Ui::Widget *ui;
};
```

  编译生成：

   MakeFile，ui_widget.h,  moc_widget.cpp, moc_widget.o, 

   main.o, widget.o, UiTest.exe, UiTest.pdb

### 2. moc_widget分析

```c++
struct qt_meta_stringdata_Widget_t {
    QByteArrayData data[4];
    char stringdata0[37];//数据长度
};
#define QT_MOC_LITERAL(idx, ofs, len) \    Q_STATIC_BYTE_ARRAY_DATA_HEADER_INITIALIZER_WITH_OFFSET(len, \
    qptrdiff(offsetof(qt_meta_stringdata_Widget_t, stringdata0) + ofs \
        - idx * sizeof(QByteArrayData)) \
    )
//只读的静态变量
static const qt_meta_stringdata_Widget_t qt_meta_stringdata_Widget = {
    {
QT_MOC_LITERAL(0, 0, 6), // "Widget"
QT_MOC_LITERAL(1, 7, 8), // "sig_send"
QT_MOC_LITERAL(2, 16, 0), // ""
QT_MOC_LITERAL(3, 17, 19) // "on_btn_send_clicked"
    },
    "Widget\0sig_send\0\0on_btn_send_clicked"
};
#undef QT_MOC_LITERAL
```

qt_meta_stringdata_Widget是一个只读的静态变量，变量名中qt_meta_stringdata_为固定字段，Widget为对应的类名。qt_meta_stringdata_Widget的类型为qt_meta_stringdata_Widget_t, 是个结构体，有两个数组成员，每个数组的长度是都是动态的。数组data有4个元素，元素排列顺序为当前类、第一个信号、占位符、其它信号、其它槽，**信号在槽前面**，信号和槽各自的顺序以声明的顺序排列，示例有1个信号和1个槽，所以加上类和占位符共4个元素，至少有1个信号或槽时后面就有1个占位符，否则只有当前类1个元素；每个元素都使用了QT_MOC_LITERAL参数宏，第一个参数表示元素索引，第二个参数表示元素在stringdata中的偏移量，第三个参数表示元素对应的字符串长度，实际上就是对QByteArrayData进行初始化。stringdata是个字符数组，长度与data数组中的元素有关，顺序保存了data数组中各元素对应的字符串表示，即类名、信号名和槽名，占位符不占据任何长度，各个字段之间以空\0分隔，示例中这个值为`Widget\0sig_send\0\0on_btn_send_clicked`。

**qt_meta_data_Widget** 变量

```c++
static const uint qt_meta_data_Widget[] = {
 // content:
       8,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   14, // methods：2个信号和槽
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       1,       // signalCount：1个信号
 // signals: name, argc, parameters, tag, flags  信号信息
       1,    1,   24,  2, 0x06 /* Public */,
 // slots: name, argc, parameters, tag, flags   槽信息
       3,    0,   27,  2, 0x08 /* Private */,
 // signals: parameters
    QMetaType::Void,
 // slots: parameters
    QMetaType::Int,
       0        // eod
};
```

### 3.有用的宏

宏Q_OBJECT、signals、Q_SIGNALS、slots、Q_SLOTS、emit等是非常有用的，在头文件QtCore/qobjectdefs.h中定义，根据是否为moc编译而分为两个版本，源码如下。

```c++
// The following macros are our "extensions" to C++
// They are used, strictly speaking, only by the moc.
#ifndef Q_MOC_RUN
#ifndef QT_NO_META_MACROS
# if defined(QT_NO_KEYWORDS)
#  define QT_NO_EMIT
# else
#   ifndef QT_NO_SIGNALS_SLOTS_KEYWORDS
#     define slots
#     define signals public
#   endif
# endif
# define Q_SLOTS
# define Q_SIGNALS public
# define Q_EMIT
#ifndef QT_NO_EMIT
# define emit
#endif
// others ...
// QT_NO_META_MACROS
//
/* qmake ignore Q_OBJECT */
#define Q_OBJECT \
public: \
    QT_WARNING_PUSH \
    Q_OBJECT_NO_OVERRIDE_WARNING \
    static const QMetaObject staticMetaObject; \
    virtual const QMetaObject *metaObject() const; \
    virtual void *qt_metacast(const char *); \
    virtual int qt_metacall(QMetaObject::Call, int, void **); \
    QT_TR_FUNCTIONS \
private: \
    Q_OBJECT_NO_ATTRIBUTES_WARNING \
    Q_DECL_HIDDEN_STATIC_METACALL static void qt_static_metacall(QObject *, QMetaObject::Call, int, void **); \
    QT_WARNING_POP \
    struct QPrivateSignal {}; \
    QT_ANNOTATE_CLASS(qt_qobject, "")
#else // Q_MOC_RUN
#define slots slots
#define signals signals
#define Q_SLOTS Q_SLOTS
#define Q_SIGNALS Q_SIGNALS
#define Q_OBJECT Q_OBJECT
//...
#endif //Q_MOC_RUN
//... QT_NO_META_MACROS
#ifndef QT_NO_META_MACROS
#ifndef QT_NO_DEBUG
# define QLOCATION "\0" __FILE__ ":" QT_STRINGIFY(__LINE__)
# ifndef QT_NO_KEYWORDS
#  define METHOD(a)   qFlagLocation("0"#a QLOCATION)
# endif
# define SLOT(a)     qFlagLocation("1"#a QLOCATION)
# define SIGNAL(a)   qFlagLocation("2"#a QLOCATION)
#else
# ifndef QT_NO_KEYWORDS
#  define METHOD(a)   "0"#a
# endif //非debug版,参数前加数字，信号为2，槽为1
# define SLOT(a)     "1"#a
# define SIGNAL(a)   "2"#a
#endif

#define QMETHOD_CODE  0                        // member type codes
#define QSLOT_CODE    1
#define QSIGNAL_CODE  2
#endif // QT_NO_META_MACROS
```

**Q_OBJECT** 声明的函数由moc编译时实现，另外还实现了信号，前面提到了信号只声明不定义，其实信号也是函数，只不过由moc实现，moc_widget.cpp相关源码及分析如下：

```c++
void Widget::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a){
    if (_c == QMetaObject::InvokeMetaMethod) {//调用函数
        auto *_t = static_cast<Widget *>(_o);
        Q_UNUSED(_t)
        switch (_id) {//调用对应的信号或槽
        case 0: _t->sig_send(); break;
        case 1: { int _r = _t->on_btn_send_clicked();
            if (_a[0]) *reinterpret_cast< int*>(_a[0]) = std::move(_r); }  break;
        default: ;
        }
    } else if (_c == QMetaObject::IndexOfMethod) {
        int *result = reinterpret_cast<int *>(_a[0]);
        {
            using _t = void (Widget::*)();
            if (*reinterpret_cast<_t *>(_a[1]) == static_cast<_t>(&Widget::sig_send)) {//通过成员指针对信号地址进行检查
                *result = 0;//返回信号id
                return;
            }
        }
    }
}
//所有的元数据
QT_INIT_METAOBJECT const QMetaObject Widget::staticMetaObject = { {
    &QWidget::staticMetaObject,
    qt_meta_stringdata_Widget.data,
    qt_meta_data_Widget,
    qt_static_metacall,
    nullptr,
    nullptr
} };

const QMetaObject *Widget::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->dynamicMetaObject() : &staticMetaObject;
}
//获取参数_clname对应类的指针
// qt_meta_stringdata_Widget.stringdata的第一个数据段保存的是类名
void *Widget::qt_metacast(const char *_clname)
{
    if (!_clname) return nullptr;
    if (!strcmp(_clname, qt_meta_stringdata_Widget.stringdata0))
        return static_cast<void*>(this);
    return QWidget::qt_metacast(_clname);//父类查找
}
//根据参数_id及_c执行不同的动作，信号和槽的总数为2
int Widget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QWidget::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 2)//信号和槽的总数为2
            qt_static_metacall(this, _c, _id, _a);
        _id -= 2;
    } else if (_c == QMetaObject::RegisterMethodArgumentMetaType) {
        if (_id < 2)//信号和槽的总数为2
            *reinterpret_cast<int*>(_a[0]) = -1;
        _id -= 2;
    }
    return _id;
}
// SIGNAL 0 第0个信号，如果有多个，依次。
// 发送信号其实调用的就是这个信号函数
// 信号函数由moc通过QMetaObject::activate实现
// 第一个参数为当前对象指针this
// 第二个参数为上面介绍的staticMetaObject
// 第三个参数为从0开始的信号索引
void Widget::sig_send(int _t1)
{
    void *_a[] = { nullptr, const_cast<void*>(reinterpret_cast<const void*>(&_t1)) };
    QMetaObject::activate(this, &staticMetaObject, 0, _a);
}
```

### 4.connect 原理

####  1.方法签名：

QMetaMethod的methodSignature()返回方法的签名，签名是个字符串。

```c++
QByteArray QMetaMethodPrivate::signature() const
{
    QByteArray result;
    result.reserve(256);
    result += name();
    result += '(';
    QList<QByteArray> argTypes = parameterTypes();
    for (int i = 0; i < argTypes.size(); ++i) {
        if (i)
            result += ',';
        result += argTypes.at(i);
    }
    result += ')';
    return result;
}
```

签名实际是方法声明去掉返回值和参数名称后的字符串。
例如：
信号：`void clicked(bool state)` 签名`clicked(bool)`。

#### 2.connect源码分析：

```c++
QMetaObject::Connection QObject::connect(const QObject *sender, const char *signal,
                                     const QObject *receiver, const char *method,
                                     Qt::ConnectionType type)
{
    // 先判断函数参数是否有效
    if (sender == 0 || receiver == 0 || signal == 0 || method == 0) {
        return QMetaObject::Connection(0);
    }
    QByteArray tmp_signal_name; 
    // 检查信号对应的宏SIGNAL是否正确使用
    // SIGNAL在信号前面添加了数字2
    // check_signal_macro通过这个数字2进行检查
    // 是否正确使用了SIGNAL， 因为是按字符串匹配的签名，所以不能含有参数名信息
    if (!check_signal_macro(sender, signal, "connect", "bind"))
        return QMetaObject::Connection(0);
    const QMetaObject *smeta = sender->metaObject();
    const char *signal_arg = signal;
    ++signal; // 跳过SIGNAL宏中的数字2
    QArgumentTypeArray signalTypes;
    Q_ASSERT(QMetaObjectPrivate::get(smeta)->revision >= 7); // moc设置了revision为7
    // 提取信号名signalName和参数列表signalTypes
    // decodeMethodSignature函数使用了strchr函数定位左、右括号在signal字符串中的位置
    QByteArray signalName = QMetaObjectPrivate::decodeMethodSignature(signal, signalTypes);
    // 提取信号索引signal_index
    // 从当前类到父类查找signalName对应的索引
    // 失败时返回-1
    int signal_index = QMetaObjectPrivate::indexOfSignalRelative(
            &smeta, signalName, signalTypes.size(), signalTypes.constData());
    if (signal_index < 0) {
        // check for normalized signatures
        tmp_signal_name = QMetaObject::normalizedSignature(signal - 1);
        signal = tmp_signal_name.constData() + 1;
 
        signalTypes.clear();
        signalName = QMetaObjectPrivate::decodeMethodSignature(signal, signalTypes);
        smeta = sender->metaObject();
        signal_index = QMetaObjectPrivate::indexOfSignalRelative(
                &smeta, signalName, signalTypes.size(), signalTypes.constData());
    }
    if (signal_index < 0) {
        err_method_notfound(sender, signal_arg, "connect");
        err_info_about_objects("connect", sender, receiver);
        return QMetaObject::Connection(0);
    }
    signal_index = QMetaObjectPrivate::originalClone(smeta, signal_index);
    signal_index += QMetaObjectPrivate::signalOffset(smeta);
 
    // 同理下面获取槽的名字和索引
   // 因为信号可以连接到槽和另外一个信号
   // 所以对槽进行处理时还要判断是否为信号
    QByteArray tmp_method_name;
    int membcode = extract_code(method);
 
    if (!check_method_code(membcode, receiver, method, "connect"))
        return QMetaObject::Connection(0);
    const char *method_arg = method;
    ++method; // skip code
 
    QByteArray methodName;
    QArgumentTypeArray methodTypes;
    const QMetaObject *rmeta = receiver->metaObject();
    int method_index_relative = -1;
    Q_ASSERT(QMetaObjectPrivate::get(rmeta)->revision >= 7);
    switch (membcode) {
    case QSLOT_CODE:
        method_index_relative = QMetaObjectPrivate::indexOfSlotRelative(
                &rmeta, methodName, methodTypes.size(), methodTypes.constData());
        break;
    case QSIGNAL_CODE:
        method_index_relative = QMetaObjectPrivate::indexOfSignalRelative(
                &rmeta, methodName, methodTypes.size(), methodTypes.constData());
        break;
    }
    if (method_index_relative < 0) {
        // check for normalized methods
        tmp_method_name = QMetaObject::normalizedSignature(method);
        method = tmp_method_name.constData();
 
        methodTypes.clear();
        methodName = QMetaObjectPrivate::decodeMethodSignature(method, methodTypes);
        // rmeta may have been modified above
        rmeta = receiver->metaObject();
        switch (membcode) {
        case QSLOT_CODE:
            method_index_relative = QMetaObjectPrivate::indexOfSlotRelative(
                    &rmeta, methodName, methodTypes.size(), methodTypes.constData());
            break;
        case QSIGNAL_CODE:
            method_index_relative = QMetaObjectPrivate::indexOfSignalRelative(
                    &rmeta, methodName, methodTypes.size(), methodTypes.constData());
            break;
        }
    }
 
    if (method_index_relative < 0) {
        err_method_notfound(receiver, method_arg, "connect");
        err_info_about_objects("connect", sender, receiver);
        return QMetaObject::Connection(0);
    }
    // 检查信号与槽的参数列表是否一致
    if (!QMetaObjectPrivate::checkConnectArgs(signalTypes.size(), signalTypes.constData(),
                                              methodTypes.size(), methodTypes.constData())) {
        return QMetaObject::Connection(0);
    }
    // 对connect的类型进行处理
    int *types = 0;
    if ((type == Qt::QueuedConnection)
            && !(types = queuedConnectionTypes(signalTypes.constData(), signalTypes.size()))) {
        return QMetaObject::Connection(0);
    }
    // 最后通过QMetaObjectPrivate::connect进行真正的connect
#ifndef QT_NO_DEBUG
    QMetaMethod smethod = QMetaObjectPrivate::signal(smeta, signal_index);
    QMetaMethod rmethod = rmeta->method(method_index_relative + rmeta->methodOffset());
    check_and_warn_compat(smeta, smethod, rmeta, rmethod);
#endif
    QMetaObject::Connection handle = QMetaObject::Connection(QMetaObjectPrivate::connect(
        sender, signal_index, smeta, receiver, method_index_relative, rmeta ,type, types));
    return handle;
}
```

QMetaObjectPrivate::connect的源码:

```c++
QObjectPrivate::Connection *QMetaObjectPrivate::connect(const QObject *sender,
                                 int signal_index, const QMetaObject *smeta,
                                 const QObject *receiver, int method_index,
                                 const QMetaObject *rmeta, int type, int *types)
{
    // sender和receiver去const
    QObject *s = const_cast<QObject *>(sender);
    QObject *r = const_cast<QObject *>(receiver); 
    // 获取receiver中method的偏移量
   // 因为其method_index是个相对值
    int method_offset = rmeta ? rmeta->methodOffset() : 0;
    Q_ASSERT(!rmeta || QMetaObjectPrivate::get(rmeta)->revision >= 6);
    QObjectPrivate::StaticMetaCallFunction callFunction =
        rmeta ? rmeta->d.static_metacall : 0; 
    // 对sender和receiver上锁（mutex pool）
    QOrderedMutexLocker locker(signalSlotLock(sender),
                               signalSlotLock(receiver)); 
    // type为Qt::UniqueConnection时作特殊处理
    // 确保connect的唯一性
    if (type & Qt::UniqueConnection) {
        QObjectConnectionListVector *connectionLists = QObjectPrivate::get(s)->connectionLists;
        if (connectionLists && connectionLists->count() > signal_index) {
            const QObjectPrivate::Connection *c2 =
                (*connectionLists)[signal_index].first; 
            int method_index_absolute = method_index + method_offset; 
            while (c2) {
                if (!c2->isSlotObject && c2->receiver == receiver && c2->method() == method_index_absolute)
                    return 0;
                c2 = c2->nextConnectionList;
            }
        }
        type &= Qt::UniqueConnection - 1;
    }
    // 最后是真正的connect对象QObjectPrivate::Connection实例化
    // 存储了所有的connect信息
    // addConnection最终保存了这个connect操作
    QScopedPointer<QObjectPrivate::Connection> c(new QObjectPrivate::Connection);
    c->sender = s;
    c->signal_index = signal_index;
    c->receiver = r;
    c->method_relative = method_index;
    c->method_offset = method_offset;
    c->connectionType = type;
    c->isSlotObject = false;
    c->argumentTypes.store(types);
    c->nextConnectionList = 0;
    c->callFunction = callFunction;
    //添加到ConnectionList
    QObjectPrivate::get(s)->addConnection(signal_index, c.data());
    // 解锁
    locker.unlock();
    // connect成功后还会调用一次connectNotify函数
    // connectNotify是个虚函数
    // 我们可以重写connectNotify在connenct成功后进行额外的相关操作
    QMetaMethod smethod = QMetaObjectPrivate::signal(smeta, signal_index);
    if (smethod.isValid())
        s->connectNotify(smethod); 
    return c.take();
}
//qobject_p.h
struct ConnectionList {
     ConnectionList() : first(nullptr), last(nullptr) {}
     Connection *first;
     Connection *last;
};
```

#### 3.QMetaObject::connectSlotsByName

```c++
void QMetaObject::connectSlotsByName(QObject *o)
{
    if (!o)
        return;
    const QMetaObject *mo = o->metaObject();
    Q_ASSERT(mo);
    const QObjectList list = //list of all objects to look for matching signals including childs
            o->findChildren<QObject *>(QString())
            << o;
    //遍历当前对象的所有方法，做匹配
    for (int i = 0; i < mo->methodCount(); ++i) {
        const QByteArray slotSignature = mo->method(i).methodSignature();
        const char *slot = slotSignature.constData();
        Q_ASSERT(slot);

        // ...that starts with "on_", ...
        if (slot[0] != 'o' || slot[1] != 'n' || slot[2] != '_')
            continue;

        // ...we check each object in our list, ...
        bool foundIt = false;
        for(int j = 0; j < list.count(); ++j) {
            const QObject *co = list.at(j);
            const QByteArray coName = co->objectName().toLatin1();

            // ...discarding those whose objectName is not fitting the pattern "on_<objectName>_...", ...
            if (coName.isEmpty() || qstrncmp(slot + 3, coName.constData(), coName.size()) || slot[coName.size()+3] != '_')
                continue;

            const char *signal = slot + coName.size() + 4; // the 'signal' part of the slot name

            // ...for the presence of a matching signal "on_<objectName>_<signal>".
            const QMetaObject *smeta;
            int sigIndex = co->d_func()->signalIndex(signal, &smeta);
            if (sigIndex < 0) {
                // if no exactly fitting signal (name + complete parameter type list) could be found
                // look for just any signal with the correct name and at least the slot's parameter list.
                // Note: if more than one of thoses signals exist, the one that gets connected is
                // chosen 'at random' (order of declaration in source file)
                QList<QByteArray> compatibleSignals;
                const QMetaObject *smo = co->metaObject();
                int sigLen = qstrlen(signal) - 1; // ignore the trailing ')'
                for (int k = QMetaObjectPrivate::absoluteSignalCount(smo)-1; k >= 0; --k) {
                    const QMetaMethod method = QMetaObjectPrivate::signal(smo, k);
                    if (!qstrncmp(method.methodSignature().constData(), signal, sigLen)) {
                        smeta = method.enclosingMetaObject();
                        sigIndex = k;
                        compatibleSignals.prepend(method.methodSignature());
                    }
                }
                if (compatibleSignals.size() > 1)
                    qWarning() << "QMetaObject::connectSlotsByName: Connecting slot" << slot
                               << "with the first of the following compatible signals:" << compatibleSignals;
            }

            if (sigIndex < 0)
                continue;

            // we connect it...
            if (Connection(QMetaObjectPrivate::connect(co, sigIndex, smeta, o, i))) {
                foundIt = true;
                // ...and stop looking for further objects with the same name.
                // Note: the Designer will make sure each object name is unique in the above
                // 'list' but other code may create two child objects with the same name. In
                // this case one is chosen 'at random'.
                break;
            }
        }
        if (foundIt) {
            // we found our slot, now skip all overloads
            while (mo->method(i + 1).attributes() & QMetaMethod::Cloned)
                  ++i;
        } else if (!(mo->method(i).attributes() & QMetaMethod::Cloned)) {
            // check if the slot has the following signature: "on_..._...(..."
            int iParen = slotSignature.indexOf('(');
            int iLastUnderscore = slotSignature.lastIndexOf('_', iParen-1);
            if (iLastUnderscore > 3)
                qWarning("QMetaObject::connectSlotsByName: No matching signal for %s", slot);
        }
    }
}
```

#### 4.QMetaObject::activate函数:

参数分别为信号发送者对象指针、信号在元对象数据结构中的偏移量及信号索引、信号参数.  目的就是在前面添加的connect列表中查找并调用这个信号连接的槽或者信号

```c++
void QMetaObject::activate(QObject *sender, int signalOffset, int local_signal_index, void **argv)
{
    // 信号在元对象数据结构中的实际索引
    int signal_index = signalOffset + local_signal_index;
    // 判断信号是否已经connect
    // 判断是否注册了信号监听回调函数（用于QTest）
    if (!sender->d_func()->isSignalConnected(signal_index)
        && !qt_signal_spy_callback_set.signal_begin_callback
        && !qt_signal_spy_callback_set.signal_end_callback) {
        return; // nothing connected to these signals, and no spy
    }
    // 判断信号是否被block
    if (sender->d_func()->blockSig)
        return;
    // 用于QTest
    if (sender->d_func()->declarativeData && QAbstractDeclarativeData::signalEmitted)
        QAbstractDeclarativeData::signalEmitted(sender->d_func()->declarativeData, sender,
                                                signal_index, argv);
    // 用于QTest begin
    void *empty_argv[] = { 0 };
    if (qt_signal_spy_callback_set.signal_begin_callback != 0) {
        qt_signal_spy_callback_set.signal_begin_callback(sender, signal_index,
                                                         argv ? argv : empty_argv);
    }
    // HANDLE句柄即当前的线程id
    // unix平台上通过pthread_self获取
    Qt::HANDLE currentThreadId = QThread::currentThreadId();
 
    {
    // 上锁（多线程、异步）
    QMutexLocker locker(signalSlotLock(sender));
    struct ConnectionListsRef {
        QObjectConnectionListVector *connectionLists;
        ConnectionListsRef(QObjectConnectionListVector *connectionLists) : connectionLists(connectionLists)
        {
            if (connectionLists)
                ++connectionLists->inUse;
        }
        ~ConnectionListsRef()
        {
            if (!connectionLists)
                return;
 
            --connectionLists->inUse;
            Q_ASSERT(connectionLists->inUse >= 0);
            if (connectionLists->orphaned) {
                if (!connectionLists->inUse)
                    delete connectionLists;
            }
        }
 
        QObjectConnectionListVector *operator->() const { return connectionLists; }
    };
    ConnectionListsRef connectionLists = sender->d_func()->connectionLists;
    // connectionLists为空时unlock后直接return
    if (!connectionLists.connectionLists) {
        locker.unlock();
        // 用于QTest end
        if (qt_signal_spy_callback_set.signal_end_callback != 0)
            qt_signal_spy_callback_set.signal_end_callback(sender, signal_index);
        return;
    }
    // 获取connect列表
    const QObjectPrivate::ConnectionList *list;
    if (signal_index < connectionLists->count())
        list = &connectionLists->at(signal_index);
    else
        list = &connectionLists->allsignals;
 
    do {
        QObjectPrivate::Connection *c = list->first;
        // 循环取得一个非空的Connection
        if (!c) continue;
        // We need to check against last here to ensure that signals added
        // during the signal emission are not emitted in this emission.
        QObjectPrivate::Connection *last = list->last;
 
        do {
            // 查找有效的receiver
            if (!c->receiver)
                continue;
 
            QObject * const receiver = c->receiver;
            // 判断当前线程与receiver线程是否一致
            const bool receiverInSameThread = currentThreadId == receiver->d_func()->threadData->threadId;
 
            // 根据connect类型及receiverInSameThread进行不同的处理
            // 立即执行queued_activate或者放入消息队列postEvent等待后续处理
            if ((c->connectionType == Qt::AutoConnection && !receiverInSameThread)
                || (c->connectionType == Qt::QueuedConnection)) {
                queued_activate(sender, signal_index, c, argv ? argv : empty_argv, locker);
                continue;
#ifndef QT_NO_THREAD
            } else if (c->connectionType == Qt::BlockingQueuedConnection) {
                locker.unlock();
                if (receiverInSameThread) {
                    qWarning("Qt: Dead lock detected while activating a BlockingQueuedConnection: "
                    "Sender is %s(%p), receiver is %s(%p)",
                    sender->metaObject()->className(), sender,
                    receiver->metaObject()->className(), receiver);
                }
                // 多线程时势必要用到同步机制（锁、信号量）
                QSemaphore semaphore;
                QMetaCallEvent *ev = c->isSlotObject ?
                    new QMetaCallEvent(c->slotObj, sender, signal_index, 0, 0, argv ? argv : empty_argv, &semaphore) :
                    new QMetaCallEvent(c->method_offset, c->method_relative, c->callFunction, sender, signal_index, 0, 0, argv ? argv : empty_argv, &semaphore);
                QCoreApplication::postEvent(receiver, ev);
                semaphore.acquire();
                locker.relock();
                continue;
#endif
            }
 
            QConnectionSenderSwitcher sw;
 
            if (receiverInSameThread) {
                sw.switchSender(receiver, sender, signal_index);
            }
            // 下面通过三种方法去调用信号连接的槽
            const QObjectPrivate::StaticMetaCallFunction callFunction = c->callFunction;
            const int method_relative = c->method_relative;
            if (c->isSlotObject) {
                c->slotObj->ref();
                QScopedPointer<QtPrivate::QSlotObjectBase, QSlotObjectBaseDeleter> obj(c->slotObj);
                locker.unlock();
                // 方法一 通过call调用receiver中的函数
                obj->call(receiver, argv ? argv : empty_argv);
 
                // Make sure the slot object gets destroyed before the mutex is locked again, as the
                // destructor of the slot object might also lock a mutex from the signalSlotLock() mutex pool,
                // and that would deadlock if the pool happens to return the same mutex.
                obj.reset();
 
                locker.relock();
            } else if (callFunction && c->method_offset <= receiver->metaObject()->methodOffset()) {
                //we compare the vtable to make sure we are not in the destructor of the object.
                locker.unlock();
                const int methodIndex = c->method();
                if (qt_signal_spy_callback_set.slot_begin_callback != 0)
                    qt_signal_spy_callback_set.slot_begin_callback(receiver, methodIndex, argv ? argv : empty_argv);
                // 方法二 callFunction即moc实现的qt_static_metacall
                callFunction(receiver, QMetaObject::InvokeMetaMethod, method_relative, argv ? argv : empty_argv);
 
                if (qt_signal_spy_callback_set.slot_end_callback != 0)
                    qt_signal_spy_callback_set.slot_end_callback(receiver, methodIndex);
                locker.relock();
            } else {
                const int method = method_relative + c->method_offset;
                locker.unlock();
 
                if (qt_signal_spy_callback_set.slot_begin_callback != 0) {
                    qt_signal_spy_callback_set.slot_begin_callback(receiver,
                                                                method,
                                                                argv ? argv : empty_argv);
                }
                // 方法三 通过metacall调用moc实现的qt_matacall
                metacall(receiver, QMetaObject::InvokeMetaMethod, method, argv ? argv : empty_argv);
 
                if (qt_signal_spy_callback_set.slot_end_callback != 0)
                    qt_signal_spy_callback_set.slot_end_callback(receiver, method);
 
                locker.relock();
            }
            // orphaned为true时说明connectionLists的所属QObject已经销毁
            // 尽管connectionLists是inUse但没有什么意思
            // 所以跳出循环
            if (connectionLists->orphaned)
                break;
        } while (c != last && (c = c->nextConnectionList) != 0);
 
        if (connectionLists->orphaned)
            break;
    } while (list != &connectionLists->allsignals &&
        //start over for all signals;
        ((list = &connectionLists->allsignals), true));
 
    }
    // 用于QTest（end）
    if (qt_signal_spy_callback_set.signal_end_callback != 0)
        qt_signal_spy_callback_set.signal_end_callback(sender, signal_index);
}
```

