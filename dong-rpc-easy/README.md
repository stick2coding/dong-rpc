# rpc框架

## web服务器

如果服务提供者需要对外提供服务，就需要一个web服务器，能够接收处理请求，并返回结果

这里选用的是高性能的NIO框架Vert.x来做为RPC框架的服务器

（也可以使用springboot内嵌的Tomcat，NIO框架netty等来实现）