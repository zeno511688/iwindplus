# iwindplus-base-redis

Redis 模块，提供 Redis 的增强功能，包括分布式锁、幂等性控制、限流、防重复提交、序列号生成等功能。

## 功能特性

- ✅ 分布式锁（@RedisLock）- 支持可重入锁、公平锁、读写锁等多种锁类型
- ✅ 幂等性控制（@RedisIdempotent）- 防止重复请求，支持缓存返回结果
- ✅ 限流（@RedisRateLimiter）- 基于令牌桶算法的分布式限流
- ✅ 防重复提交（@RedisRepeatSubmit）- 防止表单重复提交
- ✅ 序列号生成（RedissonSerialNumOperation）- 分布式序列号生成
- ✅ Redis 序列化 - 支持 Kryo、Protostuff、Gzip 压缩等多种序列化方式
- ✅ Redis Key 解析器 - 支持用户、客户端 IP、服务节点等多种 Key 解析策略
- ✅ Redis 缓存管理 - 集成 Spring Cache，支持 @Cacheable 等注解

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-redis</artifactId>
</dependency>
```

## 核心功能

### 1. @RedisLock - 分布式锁

基于 Redisson 实现的分布式锁，支持多种锁类型。

#### 配置示例

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password
      database: 0
```

#### 功能说明

@RedisLock 支持以下锁类型：

1. **LOCK** - 可重入锁（ReentrantLock）
2. **FAIR_LOCK** - 公平锁（FairLock）
3. **READ_LOCK** - 读锁（ReadWriteLock）
4. **WRITE_LOCK** - 写锁（ReadWriteLock）
5. **SPIN_LOCK** - 自旋锁（SpinLock）

#### 使用示例

##### 1. 可重入锁

```java
@Service
public class OrderService {
    
    @Resource
    private OrderMapper orderMapper;
    
    // 可重入锁（默认）
    @RedisLock(names = "order", keys = "#orderId")
    public OrderVO getOrder(Long orderId) {
        return orderMapper.selectById(orderId);
    }
    
    // 更新订单（防止并发更新）
    @RedisLock(names = "order", keys = "#orderId", leaseTime = 10, waitTime = 5)
    public void updateOrder(Long orderId, OrderDTO dto) {
        OrderDO order = orderMapper.selectById(orderId);
        order.setStatus(dto.getStatus());
        orderMapper.updateById(order);
    }
}
```

##### 2. 公平锁

```java
@Service
public class StockService {
    
    @Resource
    private StockMapper stockMapper;
    
    // 公平锁（按请求顺序获取锁）
    @RedisLock(
        names = "stock",
        keys = "#productId",
        lockType = RedisLockTypeEnum.FAIR_LOCK,
        leaseTime = 10,
        waitTime = 5
    )
    public boolean deductStock(Long productId, Integer quantity) {
        StockDO stock = stockMapper.selectById(productId);
        if (stock.getQuantity() < quantity) {
            return false;
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        stockMapper.updateById(stock);
        return true;
    }
}
```

##### 3. 读写锁

```java
@Service
public class CacheService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    // 读锁（多个线程可以同时读）
    @RedisLock(
        names = "cache",
        keys = "#key",
        lockType = RedisLockTypeEnum.READ_LOCK
    )
    public Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    // 写锁（独占锁，其他线程不能读也不能写）
    @RedisLock(
        names = "cache",
        keys = "#key",
        lockType = RedisLockTypeEnum.WRITE_LOCK,
        leaseTime = 10
    )
    public void setCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
}
```

##### 4. 自定义 Key 解析器

```java
// 使用用户 ID 作为 Key 的一部分（默认）
@RedisLock(names = "order", keys = "#orderId", keyResolver = UserRedisKeyResolver.class)
public OrderVO getOrder(Long orderId) {
    // ...
}

// 使用客户端 IP 作为 Key 的一部分
@RedisLock(names = "api", keys = "#apiName", keyResolver = ClientIpRedisKeyResolver.class)
public void callApi(String apiName) {
    // ...
}

// 使用服务节点作为 Key 的一部分
@RedisLock(names = "task", keys = "#taskId", keyResolver = ServerNodeRedisKeyResolver.class)
public void executeTask(Long taskId) {
    // ...
}

// 使用默认解析器（不添加额外信息）
@RedisLock(names = "order", keys = "#orderId", keyResolver = DefaultRedisKeyResolver.class)
public OrderVO getOrder(Long orderId) {
    // ...
}
```

##### 5. SpEL 表达式

```java
@Service
public class OrderService {
    
    // 支持SpEL表达式
    @RedisLock(names = "order", keys = "#order.id + '-' + #order.userId")
    public void createOrder(OrderDTO order) {
        // ...
    }
    
    // 多个 Key
    @RedisLock(names = {"order", "product"}, keys = {"#orderId", "#productId"})
    public void bindOrderProduct(Long orderId, Long productId) {
        // ...
    }
}
```

#### 分布式锁流程

```
┌─────────────────────────────────────────────────────────┐
│              分布式锁流程                                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  方法调用                                                │
│    │                                                     │
│    └─> RedisLockAspect 拦截                              │
│        │                                                 │
│        ├─> 解析 Key（使用 keyResolver）                  │
│        │   - UserRedisKeyResolver: user:{userId}:...     │
│        │   - ClientIpRedisKeyResolver: ip:{ip}:...       │
│        │   - ServerNodeRedisKeyResolver: node:{ip}:...   │
│        │   - DefaultRedisKeyResolver: ...                │
│        │                                                 │
│        ├─> 获取锁（根据 lockType）                       │
│        │   - LOCK: 可重入锁                              │
│        │   - FAIR_LOCK: 公平锁                           │
│        │   - READ_LOCK: 读锁                             │
│        │   - WRITE_LOCK: 写锁                            │
│        │   - SPIN_LOCK: 自旋锁                           │
│        │                                                 │
│        ├─> 等待获取锁（waitTime）                        │
│        │   ├─> 成功 ──> 执行方法                         │
│        │   └─> 失败 ──> 抛出异常                         │
│        │                                                 │
│        └─> 释放锁                                        │
│            ├─> 自动释放（leaseTime 到期）                │
│            └─> 手动释放（方法执行完成）                  │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 2. @RedisIdempotent - 幂等性控制

防止重复请求，支持缓存返回结果。

#### 配置示例

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

#### 功能说明

@RedisIdempotent 提供以下功能：

1. **幂等性保证**：防止重复请求
2. **结果缓存**：缓存第一次请求的结果，后续请求直接返回缓存结果
3. **处理状态**：记录请求处理状态（处理中、处理成功、处理失败）
4. **请求 ID**：支持请求 ID 校验

#### 使用示例

##### 1. 基本使用

```java
@Service
public class PaymentService {
    
    @Resource
    private PaymentMapper paymentMapper;
    
    // 幂等性控制（默认返回缓存结果）
    @RedisIdempotent(names = "payment", keys = "#paymentId")
    public PaymentVO createPayment(Long paymentId, PaymentDTO dto) {
        PaymentDO payment = new PaymentDO();
        payment.setId(paymentId);
        payment.setAmount(dto.getAmount());
        paymentMapper.insert(payment);
        return payment;
    }
}
```

##### 2. 自定义过期时间

```java
@Service
public class OrderService {
    
    // 处理中过期时间：30秒，成功后过期时间：600秒
    @RedisIdempotent(
        names = "order",
        keys = "#orderId",
        processingTtl = 30,
        successTtl = 600
    )
    public OrderVO createOrder(Long orderId, OrderDTO dto) {
        OrderDO order = new OrderDO();
        order.setId(orderId);
        orderMapper.insert(order);
        return order;
    }
}
```

##### 3. 请求 ID 校验

```java
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    @Resource
    private PaymentService paymentService;
    
    // 需要请求 ID（从请求头获取）
    @RedisIdempotent(
        names = "payment",
        keys = "#requestId",
        requireRequestId = true
    )
    @PostMapping
    public ResultVO<PaymentVO> createPayment(
        @RequestHeader("X-Request-Id") String requestId,
        @RequestBody PaymentDTO dto
    ) {
        PaymentVO payment = paymentService.createPayment(dto);
        return ResultVO.success(payment);
    }
}
```

##### 4. 结果处理模式

```java
// RETURN_CACHE：返回缓存结果（默认）
@RedisIdempotent(
    names = "order",
    keys = "#orderId",
    resultMode = IdempotentResultModeEnum.RETURN_CACHE
)
public OrderVO createOrder(Long orderId, OrderDTO dto) {
    // 第一次请求：执行方法，缓存结果
    // 后续请求：直接返回缓存结果
}

// RETURN_ERROR：返回错误
@RedisIdempotent(
    names = "order",
    keys = "#orderId",
    resultMode = IdempotentResultModeEnum.RETURN_ERROR
)
public OrderVO createOrder(Long orderId, OrderDTO dto) {
    // 第一次请求：执行方法
    // 后续请求：抛出异常（重复请求）
}
```

#### 幂等性控制流程

```
┌─────────────────────────────────────────────────────────┐
│              幂等性控制流程                              │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  第一次请求                                              │
│    │                                                     │
│    ├─> RedisIdempotentAspect 拦截                        │
│    │                                                     │
│    ├─> 检查幂等 Key 是否存在                             │
│    │   └─> 不存在 ──> 设置处理中状态（processingTtl）    │
│    │                                                     │
│    ├─> 执行方法                                          │
│    │                                                     │
│    └─> 设置成功状态（successTtl）                        │
│        └─> 缓存结果                                      │
│                                                          │
│  后续请求                                                │
│    │                                                     │
│    ├─> RedisIdempotentAspect 拦截                        │
│    │                                                     │
│    ├─> 检查幂等 Key 是否存在                             │
│    │   ├─> 处理中 ──> 等待或返回错误                     │
│    │   ├─> 处理成功 ──> 返回缓存结果                     │
│    │   └─> 处理失败 ──> 重新执行                         │
│    │                                                     │
│    └─> 根据 resultMode 返回结果                          │
│        ├─> RETURN_CACHE：返回缓存结果                    │
│        └─> RETURN_ERROR：抛出异常                        │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 3. @RedisRateLimiter - 限流

基于令牌桶算法的分布式限流。

#### 使用示例

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    // 限流：每秒最多 10 个请求
    @RedisRateLimiter(names = "api", keys = "#apiName", rate = 10, rateInterval = 1)
    @GetMapping("/{apiName}")
    public ResultVO<Object> callApi(@PathVariable String apiName) {
        // ...
    }
    
    // 限流：每分钟最多 100 个请求
    @RedisRateLimiter(
        names = "api",
        keys = "#apiName",
        rate = 100,
        rateInterval = 60,
        timeUnit = TimeUnit.SECONDS
    )
    @GetMapping("/{apiName}")
    public ResultVO<Object> callApi(@PathVariable String apiName) {
        // ...
    }
}
```

### 4. @RedisRepeatSubmit - 防重复提交

防止表单重复提交。

#### 使用示例

```java
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    // 防重复提交：5 秒内不能重复提交
    @RedisRepeatSubmit(names = "order", keys = "#userId", interval = 5)
    @PostMapping
    public ResultVO<OrderVO> createOrder(@RequestBody OrderDTO dto, @RequestHeader("X-User-Id") Long userId) {
        OrderVO order = orderService.createOrder(dto);
        return ResultVO.success(order);
    }
}
```

### 5. RedissonSerialNumOperation - 序列号生成

分布式序列号生成，支持按天、按小时等维度生成。

#### 使用示例

```java
@Service
public class OrderService {
    
    @Resource
    private RedissonSerialNumOperation redissonSerialNumOperation;
    
    public String generateOrderNo() {
        // 生成订单号：ORD + 年月日 + 序列号
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long serialNum = redissonSerialNumOperation.getIncrement("order", date);
        return String.format("ORD%s%06d", date, serialNum);
    }
    
    public String generatePayNo() {
        // 生成支付号：PAY + 年月日时分 + 序列号
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        Long serialNum = redissonSerialNumOperation.getIncrement("pay", datetime);
        return String.format("PAY%s%06d", datetime, serialNum);
    }
}
```

### 6. Redis 序列化

支持多种序列化方式：

1. **KryoRedisSerializer** - Kryo 序列化（高性能）
2. **ProtostuffRedisSerializer** - Protostuff 序列化
3. **GzipRedisSerializer** - Gzip 压缩序列化（节省内存）
4. **PrefixRedisSerializer** - Key 前缀序列化

#### 配置示例

```yaml
spring:
  data:
    redis:
      key-prefix: "myapp:"  # Key 前缀
```

### 7. Redis Key 解析器

支持多种 Key 解析策略：

1. **DefaultRedisKeyResolver** - 默认解析器（不添加额外信息）
2. **UserRedisKeyResolver** - 用户解析器（添加用户 ID）
3. **ClientIpRedisKeyResolver** - 客户端 IP 解析器（添加客户端 IP）
4. **ServerNodeRedisKeyResolver** - 服务节点解析器（添加服务节点 IP）

#### 使用示例

```java
// 默认解析器
@RedisLock(names = "order", keys = "#orderId", keyResolver = DefaultRedisKeyResolver.class)
// Key: lock:order:123

// 用户解析器
@RedisLock(names = "order", keys = "#orderId", keyResolver = UserRedisKeyResolver.class)
// Key: lock:user:100:order:123

// 客户端 IP 解析器
@RedisLock(names = "api", keys = "#apiName", keyResolver = ClientIpRedisKeyResolver.class)
// Key: lock:ip:192.168.1.100:api:payment

// 服务节点解析器
@RedisLock(names = "task", keys = "#taskId", keyResolver = ServerNodeRedisKeyResolver.class)
// Key: lock:node:192.168.1.101:task:456
```

## 配置属性

### RedisProperty - Redis 配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `spring.data.redis.key-prefix` | Key 前缀 | `null` |
| `spring.cache.redis.time-to-live` | 缓存过期时间 | `null` |
| `spring.cache.redis.key-prefix` | 缓存 Key 前缀 | `null` |
| `spring.cache.redis.cache-null-values` | 是否缓存空值 | `true` |
| `spring.cache.redis.use-key-prefix` | 是否使用 Key 前缀 | `true` |

## 最佳实践

### 1. 分布式锁

```java
// ✅ 推荐：使用分布式锁保护共享资源
@RedisLock(names = "stock", keys = "#productId", leaseTime = 10, waitTime = 5)
public boolean deductStock(Long productId, Integer quantity) {
    StockDO stock = stockMapper.selectById(productId);
    if (stock.getQuantity() < quantity) {
        return false;
    }
    stock.setQuantity(stock.getQuantity() - quantity);
    stockMapper.updateById(stock);
    return true;
}

// ✅ 推荐：设置合理的 leaseTime 和 waitTime
@RedisLock(
    names = "order",
    keys = "#orderId",
    leaseTime = 10,  // 锁持有时间：10 秒
    waitTime = 5     // 等待获取锁时间：5 秒
)

// ❌ 不推荐：不设置 leaseTime 和 waitTime
@RedisLock(names = "order", keys = "#orderId")  // 使用默认值，可能不适合业务场景
```

### 2. 幂等性控制

```java
// ✅ 推荐：关键接口使用幂等性控制
@RedisIdempotent(names = "payment", keys = "#paymentId")
public PaymentVO createPayment(Long paymentId, PaymentDTO dto) {
    // ...
}

// ✅ 推荐：设置合理的过期时间
@RedisIdempotent(
    names = "order",
    keys = "#orderId",
    processingTtl = 30,  // 处理中：30 秒
    successTtl = 600     // 成功后：600 秒
)

// ❌ 不推荐：过期时间过长
@RedisIdempotent(
    names = "order",
    keys = "#orderId",
    successTtl = 86400  // 24 小时，占用 Redis 内存过多
)
```

### 3. 限流

```java
// ✅ 推荐：关键接口使用限流
@RedisRateLimiter(names = "api", keys = "#apiName", rate = 10, rateInterval = 1)
public ResultVO<Object> callApi(String apiName) {
    // ...
}

// ✅ 推荐：根据业务场景设置合理的限流参数
@RedisRateLimiter(
    names = "api",
    keys = "#apiName",
    rate = 100,         // 每分钟 100 个请求
    rateInterval = 60,
    timeUnit = TimeUnit.SECONDS
)
```

### 4. 防重复提交

```java
// ✅ 推荐：表单提交使用防重复提交
@RedisRepeatSubmit(names = "order", keys = "#userId", interval = 5)
public ResultVO<OrderVO> createOrder(OrderDTO dto, Long userId) {
    // ...
}

// ✅ 推荐：设置合理的间隔时间
@RedisRepeatSubmit(names = "order", keys = "#userId", interval = 5)  // 5 秒
```

## 注意事项

1. **分布式锁**：leaseTime 不宜过长，否则锁释放慢；waitTime 不宜过长，否则影响性能
2. **幂等性控制**：successTtl 不宜过长，否则占用 Redis 内存过多
3. **限流**：rate 和 rateInterval 需要根据业务场景合理设置
4. **防重复提交**：interval 需要根据业务场景合理设置
5. **序列化**：GzipRedisSerializer 可以节省 Redis 内存，但会增加 CPU 开销
6. **Key 前缀**：建议设置 Key 前缀，避免不同应用的 Key 冲突

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 BizException 等
- `iwindplus-base-util`：工具类模块，提供序列化工具
- `iwindplus-base-web`：Web 模块，提供用户上下文
