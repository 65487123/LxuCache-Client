# LxuCache-Client
    
    
    Self-developed java client of distributed cache middleware. 

# 	How to use：
    1. Add maven dependency
     <dependency>
         <groupId>com.lzp.lxucache</groupId>
         <artifactId>lxuCache-client</artifactId>
         <version>1.0-SNAPSHOT</version>
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

________________________________________________________________________________________________________________________________________________________________________________


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
