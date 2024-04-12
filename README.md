项目总共拆分为了五个微服务：

- `cart-service`：购物车服务
- `item-service`：商品服务
- `pay-service`：用户服务
- `trade-service`：交易服务
- `user-service`：用户服务

此外的三个模块主要作用是

- `hm-common`中主要放一些微服务的共享代码
- `hm-api`中主要存放各个微服务所提供的远程调用接口
- `hm-gateway`是网关服务，用于路由转发用户的请求