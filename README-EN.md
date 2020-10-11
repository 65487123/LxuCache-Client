# LxuCache-Client
    
[中文](https://github.com/65487123/LxuCache-Client/blob/master/README.md)|English
    
    Java client of self-developed distributed cache middleware. 

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
    1.0.2：Fix spurious wakeup 
