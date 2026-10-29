# 爱点评（aidp）

爱点评是一个本地生活点评平台，后端使用 Spring Boot + MyBatis-Plus + MySQL + Redis + RabbitMQ 独立开发。

项目业务本身不复杂（店铺、优惠券、秒杀下单），写它主要是为了把 **后端高并发处理** 和 **Redis 的深度使用** 落到真实业务链路上：缓存穿透 / 击穿、缓存一致性、分布式锁、异步秒杀、全局唯一 ID，每一项都有对应的代码实现和压测场景。

> 配套前端只是为了方便联调做的雏形，页面与交互都比较简单，仅作参考；本仓库的重点是后端服务本身。前端页面连同 Nginx 一起放在 `aidp/src/main/resources/nginx-1.18.0`，开箱可跑。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 8 |
| 框架 | Spring Boot 2.3.12、Spring MVC、AspectJ |
| 持久层 | MyBatis-Plus 3.4.3、MySQL（mysql-connector-java 5.1.47） |
| 缓存 / 中间件 | Redis、Spring Data Redis（Lettuce + commons-pool2）、Redisson 3.13.6 |
| 消息队列 | RabbitMQ 3、Spring AMQP（`RabbitTemplate` + `@RabbitListener`） |
| 工具库 | Hutool 5.7.17、Lombok、Jackson（AMQP JSON 序列化） |
| 构建 | Maven |

## 功能模块

- **用户**：手机号验证码登录（Redis 令牌）、登录状态刷新、用户信息查询、登出
- **店铺**：店铺详情查询、按类型 / 名称分页查询、店铺信息更新（缓存）
- **店铺类型**：类型列表查询（缓存）
- **优惠券**：普通券 / 秒杀券添加、按店铺查询券列表
- **秒杀下单**：Lua 脚本预校验 + RabbitMQ 异步下单 + 手动 ack + 一人一单

## 目录结构

```
aidianping/
├── aidp/                                # 后端主模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/aidp/
│       │   ├── AiDianPingApplication.java
│       │   ├── config/                  # MVC / MyBatis-Plus / Redisson / RabbitMQ / Jackson / 全局异常
│       │   ├── controller/              # 对外 HTTP 接口
│       │   ├── listener/                # RabbitMQ 消费者（手动 ack）
│       │   ├── dto/                     # Result、LoginFormDTO、UserDTO、ScrollResult
│       │   ├── entity/                  # 与数据表对应的实体
│       │   ├── mapper/                  # MyBatis-Plus Mapper
│       │   ├── service/ + service/impl/ # 业务逻辑
│       │   └── utils/                   # 拦截器、锁、缓存工具、常量、ThreadLocal
│       ├── main/resources/
│       │   ├── application.yaml         # 数据源 / Redis / 日志配置
│       │   ├── db/aidp.sql              # 建库建表 + 初始化数据
│       │   ├── mapper/VoucherMapper.xml # 自定义 SQL
│       │   ├── seckill.lua              # 秒杀预校验脚本
│       │   ├── unLock.lua               # 分布式锁解锁脚本
│       │   └── nginx-1.18.0/            # 前端页面 + Nginx（conf / html / logs）
│       └── test/java/com/aidp/          # 单元测试（唯一 ID、逻辑过期）
├── jmeter-plans/                        # JMeter 压测计划（秒杀 / 缓存 / 直查）
└── temp/                                # 压测输出与临时脚本
```

## 快速开始

### 1. 环境要求

- JDK 8（项目 `java.version` 为 1.8，建议用 JDK 8 或 17 运行）
- Maven 3.6+
- MySQL 5.7+（脚本源自 MySQL 5.6，8.x 也能用）
- Redis 5+，默认端口 6379
- RabbitMQ 3.x，默认端口 5672（管理台 15672）
- Nginx：仓库已内置 Windows 版，不需要单独安装

启动前先确保 RabbitMQ 在运行。`order.queue` 由 `RabbitMQConfig` 中的 `Queue` Bean 自动声明为 durable，不需要手工创建：

```bash
docker run -d --name aidp-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. 初始化数据库

执行 `aidp/src/main/resources/db/aidp.sql`，会创建 `aidp` 库、建表并插入店铺 / 博文 / 店铺类型等初始数据。

### 3. 修改配置

按本机环境调整 `aidp/src/main/resources/application.yaml`：

```yaml
server:
  port: 8081          # 服务端口
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/aidp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: root
  redis:
    host: 127.0.0.1
    port: 6379
    password: root    # 若本地 Redis 无密码，删掉这行
```

### 4. 启动

前端页面和 Nginx 都在 `aidp/src/main/resources/nginx-1.18.0` 里，所以启动分两步：先起后端，再起 Nginx。

**① 启动后端（8081）**

```bash
cd aidp
mvn spring-boot:run
```

或在 IDE 中直接运行 `AiDianPingApplication`，前提是 MySQL 和 Redis 已经启动。

**② 启动前端 Nginx（8080）**

```powershell
cd aidp\src\main\resources\nginx-1.18.0
.\nginx.exe
```

`conf/nginx.conf` 里的 `root html/aidp`、`logs/` 都是相对 Nginx 自身目录的路径，所以要先 `cd` 进这个目录再执行。

然后在浏览器打开 http://localhost:8080。页面里的请求统一走 `/api` 前缀，Nginx 会去掉前缀后转发到 `127.0.0.1:8081`，前后端同源，不需要额外处理跨域；后端接口也可以直接访问，例如 http://localhost:8081/shop-type/list。

改完 Nginx 配置或想停掉时：

```powershell
.\nginx.exe -s reload   # 重新加载配置
.\nginx.exe -s stop     # 停止
```

## 接口一览

除免登录路径外，其余接口都需要在请求头中携带登录拿到的令牌：

```
authorization: <login 接口返回的 token>
```

鉴权由两个拦截器协作完成：`RefreshTokenInterceptor`（拦截全部请求，解析令牌写入 `ThreadLocal` 并续期）、`LoginInterceptor`（拦截未登录请求，返回 401）。

`MvcConfig` 中配置的免登录路径为：`/user/code`、`/user/login`、`/shop/**`、`/voucher/**`、`/upload/**`、`/blog/hot`、`/shop-type/**`，其余路径一律要求登录。

### 用户 `/user`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/user/code?phone=` | 发送手机验证码（免登录） |
| POST | `/user/login` | 验证码登录，返回 token（免登录） |
| GET | `/user/me` | 当前登录用户 |
| GET | `/user/info/{id}` | 用户详情 |
| GET | `/user/{id}` | 按 ID 查询用户（昵称、头像） |
| POST | `/user/logout` | 登出（删除 Redis 中的 token） |

### 店铺 `/shop`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/shop/{id}` | 店铺详情，走 Redis 缓存（免登录） |
| POST | `/shop` | 新增店铺 |
| PUT | `/shop` | 更新店铺（先更新数据库，再删除缓存） |
| GET | `/shop/of/type?typeId=&current=` | 按类型分页查询 |
| GET | `/shop/of/name?name=&current=` | 按名称分页查询 |

### 店铺类型 `/shop-type`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/shop-type/list` | 类型列表，结果缓存到 `cache:shopType` |

### 博文 `/blog`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/blog` | 发布探店博文 |
| GET | `/blog/{id}` | 博文详情，包含作者昵称和头像 |
| PUT | `/blog/like/{id}` | 博文点赞 |
| GET | `/blog/of/me?current=` | 我的博文 |
| GET | `/blog/hot?current=` | 热门博文，按点赞数倒序 |

### 优惠券与秒杀

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/voucher` | 新增普通券 |
| POST | `/voucher/seckill` | 新增秒杀券（同步写入 Redis 库存） |
| GET | `/voucher/list/{shopId}` | 查询店铺优惠券列表 |
| POST | `/voucher-order/seckill/{id}` | 秒杀下单 |

统一返回体为 `Result`：`{ success, errorMsg, data, total }`；`success=false` 时 `errorMsg` 为失败原因。

## Redis Key 约定

定义见 `utils/RedisConstants.java` 与 `utils/RedisIdWorker.java`：

| Key | 类型 | 说明 | TTL |
| --- | --- | --- | --- |
| `login:code:{phone}` | String | 手机验证码 | 5 分钟 |
| `login:token:{token}` | Hash | 登录用户信息 | 36000 分钟，每次请求续期 |
| `cache:shop:{id}` | String | 店铺缓存（存 `RedisData`，逻辑过期） | 无物理 TTL |
| `cache:shopType` | String | 店铺类型列表缓存 | 无 TTL |
| `lock:shop:{id}` | String | 缓存重建互斥锁 | 10 秒 |
| `lock:order:{userId}` | — | Redisson 分布式锁 | 框架管理 |
| `seckill:stock:{voucherId}` | String | 秒杀券库存 | — |
| `seckill:order:{voucherId}` | Set | 已下单用户集合（一人一单） | — |
| `icr:{prefix}:{yyyy:MM:dd}` | String | 全局唯一 ID 的每日自增序列 | — |

`utils/RedisConstants.java` 中另有 `blog:liked:`、`feed:`、`shop:geo:`、`sign:` 等预留 Key，对应当前尚未完成的点赞 ZSet、关注 Feed 流、附近店铺、签到功能。

## 核心实现亮点

### 缓存穿透

查询数据库为空时，向 Redis 写入空字符串并设置短 TTL（`CACHE_NULL_TTL`，2 分钟），避免同一无效 ID 反复打库。

### 缓存击穿

`utils/CacheClient.java` 提供两套通用方案，均已泛型化，可复用于任意实体：

- `queryWithPassThough`：缓存穿透兜底 + 常规缓存写入
- `queryWithLogicalExpire`：逻辑过期。缓存不设物理 TTL，过期时间写入 `RedisData`；命中后判断逻辑过期，过期则抢互斥锁并提交到独立线程池重建缓存，当前请求先返回旧数据。换取了高可用（不阻塞），代价是短时间内可能读到脏数据。

互斥锁版本 `queryWithMutex` 的原始实现保留在 `ShopServiceImpl` 的注释中，可与逻辑过期版本对照阅读。

### 缓存一致性

店铺更新采用 **Cache Aside**：`@Transactional` 内先更新数据库，再删除缓存，而非更新缓存（避免并发写导致的覆盖与脏数据）。

### 分布式锁

`utils/SimpleRedisLock.java` 基于 `SET NX EX` 实现，锁的 value 为「UUID + 线程 ID」，解锁时通过 `unLock.lua` 在 Redis 侧原子比对并删除，避免误删他人锁：

```lua
if (redis.call("get", KEYS[1]) == ARGV[1]) then
    return redis.call("del", KEYS[1])
end
return 0
```

生产用法则是 Redisson（`RedissonConfig`），直接调用 `lock.tryLock()` / `lock.unlock()`。

### 优惠券秒杀

下单链路为「**Redis 预校验 → RabbitMQ 削峰 → 异步落库**」：

1. 请求进入 `VoucherOrderServiceImpl.seckillVoucher`，执行 `seckill.lua`。
2. 脚本在 Redis 内原子完成：库存是否充足 → 用户是否已下单 → 扣库存 → 记录用户，返回 `0/1/2`。
3. 通过校验后生成订单 ID，由 `RabbitTemplate` 投递到 `order.queue`，接口立即返回订单 ID（受理结果）。
4. `@RabbitListener` 以 `prefetch=1` 串行消费，消息体使用 JSON 序列化（`Jackson2JsonMessageConverter`）。
5. 消费端按 `userId` 加 Redisson 锁，在事务内做「一人一单校验 + `stock > 0` 条件更新 + 保存订单」。
6. ack 模式为手动：事务提交后 `basicAck`；处理异常时 `basicNack(requeue=true)`。

队列由 `RabbitMQConfig` 声明，应用启动时自动创建（durable）。

此外演示了全流程的演进：乐观锁扣库存 → 悲观锁一人一单 → Redis 分布式锁 → Lua 原子解锁 → Redisson → 阻塞队列 → RabbitMQ 异步下单，每一步都对应一次提交记录，适合按 git 历史逐步阅读。

### 全局唯一 ID

`utils/RedisIdWorker.java` 采用「时间戳（相对 2022-01-01 的秒数，左移 32 位）+ Redis 每日自增序列」拼出 64 位 ID：全局唯一、趋势递增、不暴露业务量级。

## 压测结果

环境：单机 16 逻辑核，JMeter 5.4.1 直连 `127.0.0.1:8081`，JMeter、服务、MySQL、Redis、RabbitMQ 同机；100 并发，ramp=0，keep-alive 开启。

| 场景 | 请求数 | 吞吐 | 平均 | P99 | 错误率 |
| --- | --- | --- | --- | --- | --- |
| 秒杀下单（10 并发） | 1,000 | 996 req/s | 8.5 ms | 52 ms | 0% |
| 秒杀下单（100 并发） | 4,000 | 1,800~2,400 req/s | 37~50 ms | 114~185 ms | 0% |
| 秒杀下单（200 并发） | 4,000 | 1,292~2,211 req/s | 77~144 ms | 194~522 ms | 1.68%~2.13%（TCP 连接被拒） |
| 限量抢购（库存 200 / 1,000 人） | 1,000 | — | — | — | 成功 200 / 拒绝 800 |

- 消费端落库约 **176 单/s**（单消费者、prefetch=1）
- 限量场景 Redis 与 MySQL 均为 200 单、200 个用户，0 超卖、0 重复
- 200 并发时的失败全部是 Tomcat accept backlog 打满导致的连接拒绝，不是业务报错

> 口径说明：单机同机压测，仅用于验证方案有效性，不代表生产环境容量。

## 已知未完成项

阅读代码时会遇到以下几处，属于项目当前状态而非缺陷：

- 预扣与落库不在同一事务：落库失败时缺少库存回补与定时对账
- 没有 publisher confirm；`basicNack(requeue=true)` 没有重试上限和死信队列
- 秒杀开始 / 结束时间未在异步链路中校验
- Redis 重启后缺少库存与已购集合的预热逻辑
- 没有支付、取消、超时关单，库存扣减后不会自动释放
- 单消费者，消费能力（约 176 单/s）明显低于接口吞吐
- `BlogController.likeBlog` 仅累加 `liked` 字段，尚未用 Redis ZSet 实现「一人一赞」与点赞排行榜
- `FollowController`、`BlogCommentsController` 为空实现，关注、Feed 流、评论功能未开发
- 未使用的预留 Redis Key：`blog:liked:`、`feed:`、`shop:geo:`、`sign:`
