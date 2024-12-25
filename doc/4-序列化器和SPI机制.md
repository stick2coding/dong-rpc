## 序列化器

我们自定义了dongserializer，在dongserializer中，我们定义了序列化方法，反序列化方法，以及序列化类型。

然后我们基于jdk的序列化器实现了序列化接口

那么在实际的开发中，我们如何选择序列化器，是不是支持用户自己选择序列化器，用户如何自己定制序列化器呢？


## 主流序列化方法对比

### json

优点：易读、跨语言
缺点：序列化后的数据相对较大，使用文本格式存储，需要额外的字符来标识键值和数据结构

### hessian

优点：二进制，数据量小、传输效率高，支持跨语言
缺点：性能较差，对象必须实现Serializabled接口，限制了序列化的范围


### kyro
优点：高性能，序列化速度快，支持自定义序列化器，适用复杂的对象结构，无需实现Serializable接口
缺点：只适用java，序列化格式不友好，不适合调试

### protobuf

优点：高效的二进制序列化，数据量非常小，跨语言、版本兼容性强
缺点：需要先定义数据格式，不适合调试

## SPI
服务提供接口

SPI机制允许用户将自己的实现通过配置的方式注册到服务中，然后系统使用反射来获取这些实现

比如 jdk中的JDBC，不同的数据库开发者可以使用jdbc库来实现自己的数据库驱动

比如servlet容器、日志框架、spring框架等，其实都用到了SPI

### 系统实现SPI

系统内置的ServiceLoader类，通过这个类可以加载META-INF/services/接口名，然后读取文件中的实现类，然后实例化实现类，最后返回实现类对象

```java
// 指定序列化器
Serializer serializer = null;
ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
for (Serializer service : serviceLoader) {
    serializer = service;
}

```

### 自定义实现（增强）

我们想要的效果是通过配置文件的方式，配置序列化的类型，然后系统可以动态加载
我们可以先有一个这样的配置，标记了序列化器的类型和实现类
然后用户使用的时候就可以通过配置序列化类型，来让系统加载指定的实现类了

```java
jdk=com.yupi.yurpc.serializer.JdkSerializer
hessian=com.yupi.yurpc.serializer.HessianSerializer
json=com.yupi.yurpc.serializer.JsonSerializer
kryo=com.yupi.yurpc.serializer.KryoSerializer

```

### 自定义序列化器

之前是通过写死的配置，那如果通过配置文件去读呢？

我们看到系统SPI机制是去META-INF/services/接口名，然后读取文件中的实现类，然后实例化实现类，最后返回实现类对象

那么我们可以仿照这种方式，让系统去读META-INF/rpc下的文件

文件名仍然为接口名，文件内容就是实现类的全路径类名