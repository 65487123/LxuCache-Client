# LxuCache-Client
缓存中间件客户端， 使用方法：
1、添加maven依赖
G com.lzp.lxucache
A lxuCache-client
V 1.0-SNAPSHOT   
<dependency>
    <groupId>com.lzp.lxucache</groupId>
    <artifactId>lxuCache-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
2、new CacheClient(ip，port); 
通过这个对象对服务端的缓存进行增删改查，用法和Map一样
