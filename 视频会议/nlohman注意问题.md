nlohmann::json 

//字符串转对象

nlohmann::json object = nlohmann::json::parse(stringData);

//解析，需要先判断是否包含contains

```c++
if(object.contains(key) && object[key] != nullptr){
        return object[key].get<bool>();
}
long id = object["id"].get<long>();
//普通字符串
string method = info["method"].get<std::string>();
//如果是对象类型，用dump，{"key":{}}
string method = info["method"].dump();
```

json解析内部会做**类型判断**，所以一定要注意类型，**不同的类型转换会异常**

隐式转换：string直接赋值给json，就会被强转成json串，不会变成json对象，这样你就无法提取属性了

