#### MVVM模式

MVVM是Model-View-ViewModel的简写，【Model】获取数据。【View】展示页面。【ViewModel】mvvm模式的核心，它是连接view和model的桥梁。它有两个方向：一是将【Model】转化成【View】，即将数据刷新到页面。实现的方式可以用数据绑定。二是将【View】转化成【Model】，即将页面转化成数据。实现的方式是：DOM 事件监听。这两个方向都实现的，我们称之为数据的双向绑定。

在MVVM的框架下视图和模型是不能直接通信的。它们通过ViewModel来通信，ViewModel通常要实现一个observer观察者，当数据发生变化，ViewModel能够监听到数据的这种变化，然后通知到对应的视图做自动更新，而当用户操作视图，ViewModel也能监听到视图的变化，然后通知数据做改动，这实际上就实现了数据的双向绑定。并且MVVM中的View 和 ViewModel可以互相通信。



#### 基本元素类：

```
public class MainDataBean {
    private String title;
    private String desc;
    //省略set/get
}
//model类
public class MainModel {
    public MainDataBean loadData(){
        MainDataBean dataBean = new MainDataBean();
        dataBean.setTitle("标题栏");
        dataBean.setDesc("这是内容");
        return dataBean;
    }
}
//ViewModel类
public class MainViewModel extends ViewModel {
    //添加变量，我们主要是观察这个数据变化，来刷新页面
    private MutableLiveData<MainDataBean> mainData;
    private MainModel model;

    public MainViewModel(){
        mainData = new MutableLiveData<>();
        model = new MainModel();
    }

    public MutableLiveData<MainDataBean> getMainData() {
        return mainData;
    }

    public void loadData(){
        mainData.postValue(model.loadData());
    }
}
```



#### 不使用dataBinding

```
//布局
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
    <TextView
        android:id="@+id/tv_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:ignore="MissingConstraints" />

    <TextView
        android:id="@+id/tv_desc"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</androidx.constraintlayout.widget.ConstraintLayout>

//主页面
public class MainActivityNoDataBinding extends AppCompatActivity {
    private MainViewModel mainViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nobind);
		//创建viewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //对数据变化添加观察者
        mainViewModel.getMainData().observe(this, new Observer<MainDataBean>() {
            @Override
            public void onChanged(MainDataBean mainDataBean) {
                //数据变化刷新ui
                ((TextView)findViewById(R.id.tv_title)).setText(mainDataBean.getTitle());
                ((TextView) findViewById(R.id.tv_desc)).setText(mainDataBean.getDesc());
            }
        });
        //加载数据
        mainViewModel.loadData();
    }
}
```

#### 使用dataBinding

首先需要打开开关，在module中的build.gradle中添加

```
android {
    //启用dataBinding
    dataBinding{
        enabled true
    }
}
```

修改布局文件,要按databing的规范来写

```
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>
        <import type="com.app.template.mvvm.MainDataBean"/>
        <variable
            name="mainData" //变量名，便于view的引用
            type="com.app.template.mvvm.MainDataBean"//数据的类名
            />
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{mainData.title}"//数据关联view
            tools:ignore="MissingConstraints" />

        <TextView
            android:id="@+id/tv_desc"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{mainData.desc}" />//数据关联view

    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```

> ```
> 最外层用<layout>标签嵌套，注意layout的首字母是小写的“l”
> <layout>标签的下面紧跟着一个<data>标签，这个标签其实就是让我们进行数据绑定的一个标签
> <data>标签中，包含着<variable>标签，这个标签就是我们将“变量”放置的位置
> <variable>标签里面分别有<type>  <name>两个标签，分别来标识变量类型和变量名称
> <type>标签 标识变量类型，比如java.lang.String这就是String类型，com.guaju.mvvm.bean.User 这个就是一个我自定义的一个User类型
> <name>标签 表示的就是我们定义的一个变量名称，这个变量名称我们会在下方的布局和对应的java代码中引用到 
> ```

**敲黑板**：如果页面复杂，有多个类型数据，可以在布局的 data中声明多个variable即可，一旦哪个数据有变化，就可以更新相关的view



然后我们修改主页面测试，**注意变化**：首先setContentView被替换成DataBindingUtil.setContentView，其次页面的刷新不用你自己处理了，直接更新DataBinding对象的数据就行，是不是省去不少代码？

```
public class MainActivity extends AppCompatActivity implements Handler.Callback {
    private MainViewModel mainViewModel;
    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        //获取dataBinding对象
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getMainData().observe(this, new Observer<MainDataBean>() {
            @Override
            public void onChanged(MainDataBean mainDataBean) {
                //通知页面刷新
                activityMainBinding.setMainData(mainDataBean);
            }
        });
		//加载数据
        mainViewModel.loadData();
    }
}
```

上面这种方式，只有你更新DataBinding对象的数据才会刷新页面，那如果想属性变化就局部刷新怎么处理呢？

#### 局部属性刷新

当我们将MainDataBean类继承BaseObservable类后，

1、给之前的get方法添加 "@Bindable"注解

2、给之前的set方法的最后边添加"notifyPropertyChanged(BR.数据属性)"

```
public class MainDataBean extends BaseObservable {
    private String title;
    private String desc;

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
        notifyPropertyChanged(BR.desc);
    }
}

```

来个测试：

```
//5秒后改变局部数据试试
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        mainViewModel.getMainData().getValue().setDesc("局部刷新");
    }
}, 5000);
```



还有一种办法，定义ObserverField对象，自己查一下用法就行，我们项目的数据结构一般跟接口对应，所以没有采用这种方式



#### 显示图片

首先数据增加image属性

```bash
然后增加静态方法，并加注解   
@BindingAdapter("bind:img")
public static void setImage(ImageView view, String url){
     //显示图片，暂时用log代替
     Log.d("MainDataBean", "setImage() url="+url);
}    
这个方法的位置没有要求，只要项目能访问就行，我是放到viewmodel中了，可以放到公共类中，便于复用
    
注解中双引号中的“bind:img” ,“bind”是规范，即必须要写的，而后边的“img”就是我们 定制的自定义属性，将来我们设置图片的时候，就必须使用这个名字“img”去设置图片的url，记住这里是一一对应的哦

这里需要注意的是，有时as对“bind:img”的写法不是很懂，这个时候我们可以使用{“img”}的方式单独设置

布局中增加：
<ImageView
     android:layout_width="120dp"
     android:layout_height="120dp"
     app:img="@{mainData.image}"  //img就是BindingAdapter的注解声明，保持一致
     tools:ignore="MissingConstraints" />
```

**BindingAdapter使用要谨慎**，它可以覆盖系统的方法的

```
@BindingAdapter("android:text")
public static void setText(TextView view, String text){
    //将替换所有使用binding的页面的文本展示
    view.setText("恭喜您中了一个亿现金！");
}
```

于是就出现了这样的画面：

![bindadapter](..\images\bindadapter.png)

  开不开心，意不意外？

这样我们就完成了数据的展示刷新，后续就可以类似的处理RecylerView和ViewPager了，我们只以RecylerView为例说明

#### RecylerView的刷新

数据类，布局不介绍了

```
class CommonViewHolder<T> extends RecyclerView.ViewHolder {
    private ViewDataBinding viewDataBinding;
    private int BR_id;//item的布局中的数据声明，见上文的variable声明

    public CommonViewHolder(@NonNull View itemView, int BR_id) {
        super(itemView);
        this.BR_id = BR_id;
    }

    public ViewDataBinding getViewDataBinding() {
        return viewDataBinding;
    }
    //因为要刷新view，所以传递过来
    public void setViewDataBinding(ViewDataBinding viewDataBinding) {
        this.viewDataBinding = viewDataBinding;
    }
   
    public void setData(T t){
        viewDataBinding.setVariable(BR_id, t);
        //立即刷新，需要在主线程调用
        viewDataBinding.executePendingBindings();
    }
}
适配器类：
public class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {
     private Context context;
     private LayoutInflater layoutInflater;
     private List<T> dataList;

     public CommonRecyclerViewAdapter(Context context){
         this.context = context;
         layoutInflater = LayoutInflater.from(context);
     }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //临时测试用的，实际项目中一般是抽象方法
        ViewDataBinding dataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.main_list_view,  parent, false);
        CommonViewHolder holder = new CommonViewHolder(dataBinding.getRoot(), BR.itemData);
        //用于holder刷新
        holder.setViewDataBinding(dataBinding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public synchronized int getItemCount() {
         if(dataList != null){
             return dataList.size();
         }
        return 0;
    }

    public synchronized void setDataList(List<T> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
}
```

写个测试代码

```
mainViewModel.getMainList().observe(this, new Observer<List<MainListItem>>() {
    @Override
    public void onChanged(List<MainListItem> mainListItems) {
        adapter.setDataList(mainListItems);
    }
});

adapter  = new CommonRecyclerViewAdapter(this);
activityMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
activityMainBinding.recyclerView.setAdapter(adapter);
```

搞定！运行，神奇的一幕就出来了。。。

#### 事件处理

页面出来了，我们就处理完model到view的绑定了，然后研究怎样添加事件处理

事件的处理，我们一般是在ViewModel中进行，所以关联VM

```
布局中添加声明：
<import type="com.app.template.mvvm.MainViewModel"/>
 <variable
      name="viewModel"
      type="com.app.template.mvvm.MainViewModel"
       />
控件添加事件：
    android:onClick="@{viewModel.onClick}"
    
然后在ViewModel中添加点击事件，我直接让VM实现View.OnClickListener，然后增加onClick方法
    public void onClick(View view){
        Log.d("MainDataBean", "onClick() ");
    }
    
最后关键一步，binding对象关联viewModel，这样你的声明才真正起作用：
activityMainBinding.setViewModel(mainViewModel);
```

如果想自定义处理，传递参数怎么办？

```
theView只是临时代指当前view
<ImageView
     android:onClick="@{(theView) -> viewModel.onClick(theView, mainData.image)}" />

//添加自定义方法，自定义处理
public void onClick(View view, String data){
    Log.d("MainDataBean", "onClick() data="+data);
}
```

#### ViewStub支持

因为是异步加载的，所以主要是确认view的binding时机和数据刷新时机

```
新建布局，两个文本：
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <data>
        <import type="com.app.template.mvvm.MainDataBean"/>
        
        <variable
            name="mainData"
            type="com.app.template.mvvm.MainDataBean"
            />
    </data>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <TextView
            android:id="@+id/tv_sub_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{mainData.title}"/>

        <TextView
            android:id="@+id/tv_sub_desc"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{mainData.desc}"/>
    </LinearLayout>
</layout>

主页面布局添加：
<ViewStub
    android:id="@+id/view_stub"
    android:layout="@layout/main_stub_view"
    app:mainData="@{mainData}" />//把主页面的数据给子view，app后面的mainData是子view中的声明
```

然后主页面调用：

```
activityMainBinding.viewStub.getViewStub().inflate();
```

如果你想监听状态activityMainBinding.viewStub.setOnInflateListener(new ViewStub.OnInflateListener()）;

#### include布局支持

基本跟viewStub一致，只加上app:mainData="@{mainData}做数据传递

```
页面布局添加，控件id跟主页面已有id不要重复，否则会有问题：
<include
    android:id="@+id/main_include_view"
    layout="@layout/main_stub_view"
    app:mainData="@{mainData}" />
```

#### 双向数据绑定

如果页面需要关注组件变化，怎末办呢？比如我有一个checkBox，用户点击可以修改状态，如何处理呢？

```
<CheckBox
    android:id="@+id/checkbox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:checked="@={mainData.rememberMe}"
    />
    
@={} 表示法（其中重要的是包含“=”符号）可接收属性的数据更改并同时监听用户更新。
```

我们在MainDataBean中添加属性rememberMe，注解声明属性需要bind

```
boolean rememberMe;
@Bindable
public boolean isRememberMe() {
    return rememberMe;
}

public void setRememberMe(boolean rememberMe) {
    if(rememberMe == this.rememberMe){//我这里为了防止死循环
        return;
    }
    this.rememberMe = rememberMe;
    notifyPropertyChanged(BR.rememberMe);
}
```

常用的功能已经演示完了，其他的问题，遇到再解决

有不清楚的，可以看代码：

https://github.com/zytc2009/AppTemplate

欢迎留言和讨论

