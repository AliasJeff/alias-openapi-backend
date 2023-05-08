# API开放平台项目笔记

[TOC]

## 1. 项目介绍

### 线上地址

http://182.92.113.148/

### 源码

- Github: 

前端 https://github.com/AliasJeff/alias-openapi-frontend

后端 https://github.com/AliasJeff/alias-openapi-backend

- Gitee: 

前端 https://gitee.com/AliasJeff/alias-openapi-frontend

后端 https://gitee.com/AliasJeff/alias-openapi-backend

### 应用场景

API开放平台能够在前后端分离的开发流程中，方便前端开发者调用后端接口，同时也为用户和开发者提供现成的系统的功能。

### 功能

用户注册登录后即开通调用接口的权限，用户可以浏览、调用接口，购买调用次数，后台对调用进行统计。管理员可以发布接口、管理接口。

## 2. 业务流程

<img src="/Users/zhexun/Library/Application%20Support/typora-user-images/image-20230502184346171.png" alt="image-20230502184346171" style="zoom:50%;" />

## 3. 技术选型

### 前端

- React

- Ant Design Pro (脚手架)

- Ant Design Procomponents (组件库)
- Umi (应用框架)
- Umi Request (网络请求库)

### 后端

- Java Spring Boot
- Spring Boot Starter (SDK开发)
- Dubbo (RPC)
- Nacos (用于服务注册、动态路由)
- Spring Cloud Gateway (API网关)
- Redis
- Mybatis-Plus

## 4. 项目演示(截图)

1. 登录注册

用户输入账号密码登录，后台保存登录态。

用户填写邮箱，后台发送邮箱验证码，验证密码和邮箱进行注册。

![1.png](https://img1.imgtp.com/2023/05/08/8LbCtGEH.png)

![2.png](https://img1.imgtp.com/2023/05/08/NPk62lR5.png)

2. 主页

登录成功后进入主页，主页显示网站相关信息，发送第一次查询请求之后，请求数据会保存到redis缓存中，这样之后一段时间内的请求会直接从redis中查出，增强性能，提高用户体验。

![3.png](https://img1.imgtp.com/2023/05/08/Dadg8E5D.png)

3. API密钥

密钥代表用户的账号身份和所拥有的权限，使用 API 密钥可以无状态地操作平台上的资源，私钥密钥在用户注册是生成分配

![4.png](https://img1.imgtp.com/2023/05/08/6l9XkQZO.png)

4. 接口列表

![5.png](https://img1.imgtp.com/2023/05/08/cMcqlxK6.png)

5. 接口详情

接口详情显示接口状态、接口描述、请求地址、请求头、请求参数、响应头、剩余调用次数

![6.png](https://img1.imgtp.com/2023/05/08/5o4tedXt.png)

6. 在线调用接口

正确填写请求参数，后台把调用信息发送到gateway网关，网关查询并调用nacos注册中心注册的服务，响应信息到用户界面

![7.png](https://img1.imgtp.com/2023/05/08/KOLp81Os.png)

7. 管理接口（仅管理员可用）

首先判断用户权限，当用户权限为admin时可访问接口管理界面，管理员可以查看所有接口信息，新增接口、修改接口信息、下线接口、上线接口、删除接口

![8.png](https://img1.imgtp.com/2023/05/08/3XaScYop.png)

![9.png](https://img1.imgtp.com/2023/05/08/9zq5C8vX.png)

## 5. 数据库设计

### user表

| 字段名      | 数据类型 | 长度 | 约束条件                                                     | 说明                     |
| ----------- | -------- | ---- | ------------------------------------------------------------ | ------------------------ |
| id          | bigint   |      | NOT NULL AUTO_INCREMENT                                      | 主键                     |
| username    | varchar  | 256  |                                                              | 用户昵称                 |
| account     | varchar  | 256  | NOT NULL                                                     | 账号                     |
| phone       | varchar  | 256  |                                                              | 手机号                   |
| email       | varchar  | 255  | NOT NULL                                                     | 邮箱                     |
| avatar      | varchar  | 1024 |                                                              | 用户头像                 |
| gender      | tinyint  |      |                                                              | 性别                     |
| role        | varchar  | 256  | NOT NULL DEFAULT 'user'                                      | 用户角色：user / admin   |
| password    | varchar  | 512  | NOT NULL                                                     | 密码                     |
| access_key  | varchar  | 512  | NOT NULL                                                     | accessKey                |
| secret_key  | varchar  | 512  | NOT NULL                                                     | secretKey                |
| create_time | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP                           | 创建时间                 |
| update_time | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间                 |
| is_delete   | tinyint  |      | NOT NULL DEFAULT '0'                                         | 是否删除(0-未删, 1-已删) |

### interface_info表

该表用于存储接口信息。

| 字段名          | 数据类型 | 长度 | 约束条件                                                     | 说明                       |
| --------------- | -------- | ---- | ------------------------------------------------------------ | -------------------------- |
| id              | bigint   |      | NOT NULL AUTO_INCREMENT                                      | 主键                       |
| name            | varchar  | 256  | NOT NULL                                                     | 名称                       |
| description     | varchar  | 256  |                                                              | 描述                       |
| method          | varchar  | 256  | NOT NULL                                                     | 请求类型                   |
| url             | varchar  | 512  | NOT NULL                                                     | 接口地址                   |
| request_params  | text     |      |                                                              | 请求参数                   |
| request_header  | text     |      |                                                              | 请求头                     |
| response_header | text     |      |                                                              | 响应头                     |
| price           | decimal  | 10,2 | NOT NULL                                                     | 计费规则(元/条)            |
| status          | int      |      | NOT NULL DEFAULT '0'                                         | 接口状态（0-关闭，1-开启） |
| creator         | bigint   |      | NOT NULL                                                     | 创建人                     |
| create_time     | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP                           | 创建时间                   |
| update_time     | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间                   |
| is_delete       | tinyint  |      | NOT NULL DEFAULT '0'                                         | 是否删除(0-未删, 1-已删)   |

### user_interface_info表

该表用于存储用户调用接口关系信息。

| 字段名            | 数据类型 | 长度 | 约束条件                                                     | 说明                     |
| ----------------- | -------- | ---- | ------------------------------------------------------------ | ------------------------ |
| id                | bigint   |      | NOT NULL AUTO_INCREMENT                                      | 主键                     |
| user_id           | bigint   |      | NOT NULL                                                     | 调用用户 id              |
| interface_info_id | bigint   |      | NOT NULL                                                     | 接口 id                  |
| total_num         | int      |      | NOT NULL DEFAULT '0'                                         | 总调用次数               |
| left_num          | int      |      | NOT NULL DEFAULT '0'                                         | 剩余调用次数             |
| status            | int      |      | NOT NULL DEFAULT '1'                                         | 0-禁用，1-正常           |
| create_time       | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP                           | 创建时间                 |
| update_time       | datetime |      | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间                 |
| is_delete         | tinyint  |      | NOT NULL DEFAULT '0'                                         | 是否删除(0-未删, 1-已删) |

## 6. 项目设计

### alias-openapi-service微服务

- 设计通用返回类
  - 自定义错误码
  - 自定义业务异常类

- JSON数据统一序列化处理
- 跨域处理
  - 在每次请求前进行拦截，并添加一些响应头，来允许跨域请求。如果是OPTIONS请求，则返回NO_CONTENT状态码，否则继续执行后续的过滤器或者请求处理器
- AOP
  - 权限校验AOP，使用注解拦截用户请求进行权限校验
  - 请求响应日志AOP，收到请求-->拦截-->获取请求路径-->生成唯一请求id-->获取请求参数-->输出请求日志-->处理原请求
  - 接口调用次数初始化AOP，在接口信息表、用户信息表更新后，自动初始化接口请求次数
- 用户Service
  - 增删改查
  - 用户注册、登录
  - 保存、获取用户登录态
- 接口信息Service
  - 增删改查
  - 上线、下线接口
  - 接口调用，更新数据库统计调用次数并接受用户参数，校验权限，将请求转发到网关（对每个接口都要执行统计调用次数的操作，那么我们可以使用AOP切面实现，独立于接口，但缺点是只适用于单体项目以内，如果有多个团队开发自己的模拟接口，就可以使用网关实现）
- 用户接口Service
  - 增删改查
  - 查询可用接口，涉及到多表查询

- API签名认证

  用于用户鉴权，适用于无需保存登录态的场景（只用私钥密钥鉴权），确保接口请求的合法性和安全性，保护用户数据不被非法访问。

  用户每次调用接口都需要验证ak、sk

  使用MD5加密算法生成签名，在用户注册时分配私钥(accessKey)、密钥(secretKey)

  防止重放：请求加nonce随机数、加timestamp时间戳

  > 重放攻击（Replay Attack）是指攻击者截获合法用户的某个请求，然后在不经过用户的授权和知晓的情况下，将该请求发送给服务器，从而实现非法操作的一种攻击方式。重放攻击通常利用网络中的漏洞或者不安全的通信协议，来重复发送已经被截获的数据包，可能导致一些严重的后果，如恶意篡改数据、非法获取数据等。

### alias-openapi-gateway网关微服务

网关的作用：

1. **路由**
2. 负载均衡（需要用到注册中心）
3. **统一鉴权**
4. 跨域
5. **统一业务处理（缓存）**
6. **访问控制（黑白名单）**
7. 发布控制
8. **流量染色**
9. 接口保护
10. **统一日志**

我使用了SpringCloudGateway作为网关

业务逻辑：

1. 记录请求日志；
2. 进行访问控制，只允许白名单内的 IP 访问；
3. 进行用户鉴权，包括校验 accessKey、nonce、timestamp、sign 等参数，并从数据库中获取 secretKey 进行签名校验；
4. 查询用户是否还有调用次数；
5. **调用转发的接口**，并在接口调用成功后将调用次数 +1；
6. 处理响应，包括记录响应日志、修改调用次数、降级处理等。

代码编写使用GlobalFilter全局请求拦截处理（编程式，类似于AOP）

转发接口调用

- HttpClient
- RPC（Dubbo）

### alias-openapi-interface模拟接口微服务

接收网关发来的接口调用请求，提供api接口服务

提供三个不同种类的模拟接口：

1. GET
2. POST (url传参)
3. POST (Restful)

调用方式：HttpClient、Hutool

使用基本请求路径中携带的`headers`请求头，通过**反射**技术实现只通过一个基本路径动态调用API接口。

业务逻辑

1. 获取请求头信息，进行请求参数和密钥等的合法性验证。
2. 如果请求合法，则继续处理请求。首先获取请求方法，然后从请求头中获取请求路径，并根据请求路径在hashmap中查找对应的类名和方法名。
3. 如果查找到了对应的类名和方法名，则通过反射构造类对象，并从Spring容器中获取该对象的实例。接着调用该实例的方法，并将请求头中的body参数作为方法参数传入。

## 7. 项目遇到的问题

### 如何处理跨域

写一个servlet过滤器，在请求头中添加一些允许跨域请求的信息，以便浏览器可以正确地处理跨域请求。

```java
public class SimpleCORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // 强制转换为HttpServletRequest和HttpServletResponse，方便后期操作
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 添加响应头来允许跨域请求
        // 允许哪些域名访问，这里使用request.getHeader("origin")获取请求的来源域名
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
        // 允许哪些HTTP方法
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        // 预检请求的有效时间
        response.addHeader("Access-Control-Max-Age", "3600");

        response.addHeader("Access-Control-Allow-Credentials", "true");
      
        // 允许哪些请求头
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN,customercoderoute,authorization,conntectionid,Cookie");

        // 判断如果是OPTIONS请求，则返回NO_CONTENT状态码，
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            log.info("set response NO_CONTENT...");
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else { // 否则继续执行后续的过滤器或者请求处理器，这里是处理预检请求的逻辑
            log.info("chain.doFilter...");
            chain.doFilter(req, res);
        }
    }

}
```

### 怎么调用其他项目的方法

1. HTTP请求，提供一个接口，供其他项目调用
2. RPC
3. 把公共代码打成jar包，供其他项目引用（客户端SDK）

### HTTP请求怎么调用

1. 服务提供方开发一个接口（包含地址、请求方法、参数、返回值）
2. 调用方使用HTTP Client等代码包发送HTTP请求

### 如何保证数据一致性

1. 使用数据库事务（注解@Transaction）
2. 乐观锁
3. 悲观锁
4. synchronized关键字线程同步

### Redis序列化问题

Redis默认使用JdkSerializationRedisSerializer作为序列化器，只能存储字符串类型的数据，而我们在使用Redis时，可能需要存储的是复杂的数据类型，比如对象、列表、集合等。因此，我们需要将这些数据类型序列化为字符串类型，才能存储到Redis中。同时，在从Redis中读取数据时，我们也需要将字符串类型的数据反序列化为原始的数据类型，才能正确地使用这些数据。因此，为了方便地操作复杂的数据类型，我们需要编写一个序列化器，将数据序列化和反序列化。

```java
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        //Use Jackson 2Json RedisSerializer to serialize and deserialize the value of redis (default JDK serialization)
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //将类名称序列化到json串中，去掉会导致得出来的的是LinkedHashMap对象，直接转换实体对象会失败
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        //Use String RedisSerializer to serialize and deserialize the key value of redis
        RedisSerializer redisSerializer = new StringRedisSerializer();
        //key
        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(redisSerializer);
        //value
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```

## 8. 项目优化思路

重构部分代码，抽象公共服务到common项目，common项目只保留必要的公共依赖

优化代码复杂度，使用注解或者配置文件的方式，将一些配置信息（如分页大小、重试次数等）提取到外部

开放下载client-sdk项目包，允许用户根据接口编写规则上传自己的接口

实现接口次数购买功能（通过支付宝沙箱来实现付款功能）

使用Feign来实现远程调用

使用Rabbit MQ保证消息可靠性（主要用于订单流程）

在网关层中使用Sentinel来实现限流、降级等操作

使用分布式锁确保数据一致性，提高性能

加入接口防刷，可以使用注解+反射的方式，实现不同接口自定义防刷、限流

