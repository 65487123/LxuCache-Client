# LxuCache-Client
    
                       
中文|[English](https://github.com/65487123/LxuCache-Client/blob/master/README-EN.md)

    自研分布式缓存中间件的java客户端。redis有的功能，基本都有


# 	使用方法：
    1、添加maven依赖
    <dependency>
        <groupId>com.lzp.lxucache</groupId>
        <artifactId>lxuCache-client</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


    2、
    单机模式：
    new CacheClient(ip，port);
    通过这个对象对服务端的缓存进行增删改查

    集群模式
    new CacheClusterClient(HostAndportList);
    参数为服务端所有的主从节点，客户端会自动找到所有主节点并做负载均衡，使用方法和单机模式一样，如果主挂了，
    客户端会自动找到新的主节点。用户不会感知。目前不支持运行时增加新的主节点或删除原有主节点（可以增删从节点)
    
    3、自己实现连接池。
    
## 各版本主要改动
    1.0.1：原先返回值是一个Respnse对象，用的protobuf序列化，1.0.1版本把返回值改为单个字符串，取消protobuf序列化。
    1.0.1-sr1：修复虚假唤醒问题,重写HostAndPort的hashCode()方法以提高性能。
