# LxuCache-Client
    
中文|[English](https://github.com/65487123/LxuCache-Client/blob/master/README-EN.md)

    Self-developed java client of distributed cache middleware. 

# 	How to use：
    1. Add maven dependency
     <dependency>
         <groupId>com.lzp.lxucache</groupId>
         <artifactId>lxuCache-client</artifactId>
         <version>1.x.x</version>
     </dependency>


    2、
     Stand-alone mode:
     new CacheClient(ip, port);
     Use this object to add, delete, modify and check the server cache

     Cluster mode
     new CacheClusterClient(HostAndportList);
     The parameter is all the master and slave nodes of the server, 
     the client will automatically find all the master nodes and do the negative
     Load balancing, the usage method is the same as the stand-alone mode, if 
     the master hangs,The client will automatically find the new master node. 
     The user will not perceive it. Currently does not support adding a new master 
     node or deleting the original master node at runtime (you can add or delete slave nodes)
    
     3. Realize the connection pool yourself.

## Major changes in the new version

    1.0.1：Originally the return value was a Response object, which was serialized using protobuf. Version 1.0.1 changed the return value 
    to a single string, canceling protobuf serialization.
 __________________________________________________________________________________________________________________________________________________________________
                          
[中文](https://github.com/65487123/LxuCache-Client/blob/master/README.md)|English 

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
    
## 新版本主要改动
    1.0.1：原先返回值是一个Respnse对象，用的protobuf序列化，1.0.1版本把返回值改为单个字符串，取消protobuf序列化。
