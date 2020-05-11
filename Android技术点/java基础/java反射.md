#### 什么是反射？

> 在运行状态中，对于任意一个类，都能够获取到这个类的所有属性和方法，对于任意一个对象，都能够调用它的任意一个方法和属性(包括私有的方法和属性)，这种动态获取的信息以及动态调用对象的方法的功能就称为java语言的反射机制

> 如何获取一个类的成员变量 & 成员方法 & 注解信

```
  //成员变量
  Class<Student> aClass = (Class<Student>)Class.forName("com.sl.reflect.Student");
          //1.获取字段
          //  1.1 获取所有字段 -- 字段数组
          //     可以获取公用和私有的所有字段，但不能获取父类字段
          Field[] declaredFields = aClass.getDeclaredFields();
          for (Field field:declaredFields) {
              System.out.println(field);
          }
          System.out.println("=============================");
          //  1.2获取指定字段
          Field field = aClass.getDeclaredField("name");
          System.out.println(field.getName());
  //成员方法
  Class<Student> aClass = (Class<Student>) Class.forName("com.sl.reflect.Student");
          //1.获取方法
          // 获取取clazz对应类中的所有方法--方法数组（一）
          // 不能获取private方法,并且获取从父类继承来的所有方法
          Method[] methods = aClass.getMethods();
          for (Method method:methods) {
              System.out.println(method);
          }
          System.out.println("================================");
          //2.获取方法
          // 获取取clazz对应类中的所有方法--方法数组（一）
          // 不能获取private方法,不获取从父类继承来的所有方法
          Method[] declaredMethods = aClass.getDeclaredMethods();
          for (Method method:declaredMethods) {
              System.out.println(method);
          }
          
  //获取注解
  Class<?> aClass = Class.forName("com.sl.reflect.Student");
          Object o = aClass.newInstance();
          Method method = aClass.getDeclaredMethod("setAge", Integer.class);
          int val = 6;
          AgeValidator annotation = method.getAnnotation(AgeValidator.class);
          if (annotation != null) {
              if (annotation instanceof AgeValidator) {
                 AgeValidator ageValidator =  annotation;
                 if (val < ageValidator.min() || val > ageValidator.max()) {
                     throw new RuntimeException("年龄非法");
                 }
              }
          }
```

#### 通常在项目当中用到反射多吗？都是用来干嘛？

- 业务代码中基本不用基于名称的反射。除非经过团队讨论确认非用不可，否则基本上不可能通过代码走查 

- Android中 **json数据转java对象**

- IOC容器中的**对象注入**

- 对某些Java库的**hook**

#### IOC(待完善)

