# iwindplus-base-mybatis

MyBatis Plus 模块，提供 MyBatis Plus 的增强功能，包括字段自动填充、多租户、字段加密脱敏、乐观锁、分页、防全表更新等功能。

## 功能特性

- ✅ 字段自动填充（MyBatisAutoFillHandler）- 自动填充创建人、创建时间、更新人、更新时间等字段
- ✅ 多租户支持（MybatisTenantLineHandler）- 自动添加租户 ID 过滤条件
- ✅ 字段加密脱敏（MybatisFieldCryptoManager）- 数据库字段加密存储、脱敏显示
- ✅ 乐观锁支持 - 自动处理版本号字段
- ✅ 分页插件 - 物理分页，支持多种数据库
- ✅ 防全表更新删除 - 防止误操作全表更新或删除
- ✅ 占位符替换 - 支持 SQL 占位符替换
- ✅ 事务增强（MybatisTransactionAspect）- 事务异常处理增强

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-mybatis</artifactId>
</dependency>
```

## 核心功能

### 1. MyBatisAutoFillHandler - 字段自动填充

自动填充创建人、创建时间、更新人、更新时间等公共字段。

#### 配置示例

```yaml
mybatis-plus:
  field:
    fill:
      enabled: true  # 启用字段自动填充（默认 true）
      enabled-insert-strict: true  # 插入是否严格模式（默认 true）
```

#### 功能说明

MyBatisAutoFillHandler 会自动填充以下字段：

**插入时自动填充：**
- `created_timestamp` - 创建时间戳
- `created_by` - 创建人姓名
- `created_id` - 创建人 ID
- `modified_timestamp` - 更新时间戳
- `modified_by` - 更新人姓名
- `modified_id` - 更新人 ID
- `deleted` - 删除标记（默认 0）
- `version` - 乐观锁版本号（默认 0）
- `user_id` - 用户 ID
- `org_id` - 组织 ID

**更新时自动填充：**
- `modified_timestamp` - 更新时间戳
- `modified_by` - 更新人姓名
- `modified_id` - 更新人 ID

#### 使用示例

##### 1. 实体类定义

```java
@Data
@TableName("t_user")
public class UserDO extends DbBaseDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String name;
    
    private String email;
    
    // 继承 DbBaseDO，自动包含以下字段：
    // - created_timestamp
    // - created_by
    // - created_id
    // - modified_timestamp
    // - modified_by
    // - modified_id
    // - deleted
    // - version
    // - user_id
    // - org_id
}
```

##### 2. 插入数据

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public void saveUser(UserDTO dto) {
        UserDO user = new UserDO();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        
        // 插入时自动填充以下字段：
        // - created_timestamp: 当前时间戳
        // - created_by: 当前用户姓名
        // - created_id: 当前用户 ID
        // - modified_timestamp: 当前时间戳
        // - modified_by: 当前用户姓名
        // - modified_id: 当前用户 ID
        // - deleted: 0
        // - version: 0
        // - user_id: 当前用户 ID
        // - org_id: 当前组织 ID
        userMapper.insert(user);
    }
}
```

##### 3. 更新数据

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public void updateUser(Long id, UserDTO dto) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        
        // 更新时自动填充以下字段：
        // - modified_timestamp: 当前时间戳
        // - modified_by: 当前用户姓名
        // - modified_id: 当前用户 ID
        userMapper.updateById(user);
    }
}
```

##### 4. 严格模式 vs 非严格模式

```java
// 严格模式（enabled-insert-strict: true）
// 插入时总是覆盖公共字段，不能自定义值
@Service
public class UserService {
    
    public void saveUser(UserDTO dto) {
        UserDO user = new UserDO();
        user.setName(dto.getName());
        user.setCreatedBy("自定义创建人");  // 无效，会被覆盖为当前用户姓名
        userMapper.insert(user);
    }
}

// 非严格模式（enabled-insert-strict: false）
// 插入时如果字段有值，则不覆盖；如果字段为空，则自动填充
@Service
public class UserService {
    
    public void saveUser(UserDTO dto) {
        UserDO user = new UserDO();
        user.setName(dto.getName());
        user.setCreatedBy("自定义创建人");  // 有效，不会被覆盖
        userMapper.insert(user);
    }
}
```

#### 字段自动填充流程

```
┌─────────────────────────────────────────────────────────┐
│              字段自动填充流程                            │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  插入操作（INSERT）                                      │
│    │                                                     │
│    ├─> 获取当前用户信息（UserContextHolder）             │
│    │   - userId: 当前用户 ID                             │
│    │   - orgId: 当前组织 ID                             │
│    │   - realName: 当前用户姓名                          │
│    │                                                     │
│    ├─> 判断严格模式                                      │
│    │   ├─> 严格模式：总是填充公共字段                    │
│    │   └─> 非严格模式：字段为空才填充                    │
│    │                                                     │
│    └─> 自动填充字段                                      │
│        - created_timestamp: 当前时间戳                   │
│        - created_by: 当前用户姓名                        │
│        - created_id: 当前用户 ID                         │
│        - modified_timestamp: 当前时间戳                  │
│        - modified_by: 当前用户姓名                       │
│        - modified_id: 当前用户 ID                        │
│        - deleted: 0                                      │
│        - version: 0                                      │
│        - user_id: 当前用户 ID                            │
│        - org_id: 当前组织 ID                             │
│                                                          │
│  更新操作（UPDATE）                                      │
│    │                                                     │
│    ├─> 获取当前用户信息（UserContextHolder）             │
│    │                                                     │
│    └─> 自动填充字段                                      │
│        - modified_timestamp: 当前时间戳                  │
│        - modified_by: 当前用户姓名                       │
│        - modified_id: 当前用户 ID                        │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 2. MybatisTenantLineHandler - 多租户支持

自动添加租户 ID 过滤条件，实现多租户数据隔离。

#### 配置示例

```yaml
mybatis-plus:
  tenant:
    enabled: true  # 启用多租户（默认 false）
    tenant-id-column: org_id  # 租户 ID 字段名（默认 org_id）
    ignored-table:  # 忽略的表（不添加租户过滤）
      - t_sys_config
      - t_sys_dict
      - t_sys_menu
```

#### 功能说明

MybatisTenantLineHandler 会自动处理多租户：

1. **自动添加租户过滤**：查询、更新、删除时自动添加 `org_id = ?` 条件
2. **忽略指定表**：可以配置不需要租户过滤的表
3. **租户 ID 来源**：从 UserContextHolder 获取当前用户的 orgId

#### 使用示例

##### 1. 启用多租户

```yaml
mybatis-plus:
  tenant:
    enabled: true
    tenant-id-column: org_id
    ignored-table:
      - t_sys_config
      - t_sys_dict
```

##### 2. 查询数据（自动添加租户过滤）

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public List<UserDO> listUsers() {
        // 自动添加 WHERE org_id = ? 条件
        // SQL: SELECT * FROM t_user WHERE org_id = 100
        return userMapper.selectList(null);
    }
    
    public UserDO getUserById(Long id) {
        // 自动添加 WHERE org_id = ? 条件
        // SQL: SELECT * FROM t_user WHERE id = ? AND org_id = 100
        return userMapper.selectById(id);
    }
}
```

##### 3. 更新数据（自动添加租户过滤）

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public void updateUser(Long id, UserDTO dto) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setName(dto.getName());
        
        // 自动添加 WHERE org_id = ? 条件
        // SQL: UPDATE t_user SET name = ? WHERE id = ? AND org_id = 100
        userMapper.updateById(user);
    }
}
```

##### 4. 删除数据（自动添加租户过滤）

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public void deleteUser(Long id) {
        // 自动添加 WHERE org_id = ? 条件
        // SQL: DELETE FROM t_user WHERE id = ? AND org_id = 100
        userMapper.deleteById(id);
    }
}
```

##### 5. 忽略租户过滤

```java
// 方式一：配置忽略的表
mybatis-plus:
  tenant:
    ignored-table:
      - t_sys_config  # 该表不会添加租户过滤

// 方式二：使用 @InterceptorIgnore 注解（MyBatis Plus 提供）
@InterceptorIgnore(tenantLine = "true")
@Select("SELECT * FROM t_user")
List<UserDO> selectAllUsers();
```

#### 多租户过滤流程

```
┌─────────────────────────────────────────────────────────┐
│              多租户过滤流程                              │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  执行 SQL                                                │
│    │                                                     │
│    └─> MybatisTenantLineHandler 拦截                     │
│        │                                                 │
│        ├─> 获取租户 ID（UserContextHolder.getOrgId()）   │
│        │                                                 │
│        ├─> 判断是否忽略表                                │
│        │   ├─> 在 ignored-table 中 ──> 不添加过滤        │
│        │   └─> 不在 ignored-table 中 ──> 添加过滤        │
│        │                                                 │
│        └─> 自动添加租户过滤条件                          │
│            - SELECT ──> WHERE org_id = ?                 │
│            - UPDATE ──> WHERE org_id = ?                 │
│            - DELETE ──> WHERE org_id = ?                 │
│                                                          │
│  执行带租户过滤的 SQL                                     │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 3. MybatisFieldCryptoManager - 字段加密脱敏

数据库字段加密存储、脱敏显示。

#### 配置示例

```yaml
mybatis-plus:
  field:
    crypto:
      enabled: true  # 启用字段加密脱敏（默认 false）
      enabled-input-encrypt: true  # 启用输入加密（默认 false）
      enabled-input-sensitive: true  # 启用输入脱敏（默认 false）
      enabled-output-decrypt: true  # 启用输出解密（默认 false）
      enabled-sign: false  # 启用加签（数据防篡改）（默认 false）
      algorithm: AES  # 加密算法（默认 AES）
      key: "your-secret-key"  # 密钥
      public-key: "your-public-key"  # 公钥（RSA 使用）
      private-key: "your-private-key"  # 私钥（RSA 使用）
      secret-key: "your-sign-secret-key"  # 签名密钥（数据防篡改）
```

#### 功能说明

MybatisFieldCryptoManager 提供以下功能：

1. **输入加密**：插入、更新时自动加密敏感字段
2. **输入脱敏**：插入、更新时自动脱敏敏感字段
3. **输出解密**：查询时自动解密加密字段
4. **数据加签**：防止数据被篡改

#### 使用示例

##### 1. 实体类定义

```java
@Data
@TableName("t_user")
public class UserDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String name;
    
    @TableFieldSafe  // 标记为加密字段
    private String idCard;  // 身份证号（加密存储）
    
    @TableFieldSensitive(type = SensitiveTypeEnum.MOBILE)  // 标记为脱敏字段
    private String mobile;  // 手机号（脱敏显示）
    
    @TableFieldSafe
    @TableFieldSensitive(type = SensitiveTypeEnum.BANK_CARD)
    private String bankCard;  // 银行卡号（加密存储 + 脱敏显示）
}
```

##### 2. 插入数据（自动加密）

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public void saveUser(UserDTO dto) {
        UserDO user = new UserDO();
        user.setName(dto.getName());
        user.setIdCard("330102199001011234");  // 身份证号
        user.setMobile("13800138000");  // 手机号
        user.setBankCard("6222021234567890123");  // 银行卡号
        
        // 插入时自动处理：
        // - idCard: 加密存储（AES 加密）
        // - mobile: 脱敏存储（138****8000）
        // - bankCard: 加密存储 + 脱敏显示
        userMapper.insert(user);
    }
}
```

##### 3. 查询数据（自动解密）

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public UserDO getUser(Long id) {
        UserDO user = userMapper.selectById(id);
        
        // 查询时自动处理：
        // - idCard: 自动解密（330102199001011234）
        // - mobile: 保持脱敏（138****8000）
        // - bankCard: 自动解密 + 脱敏显示
        return user;
    }
}
```

#### 字段加密脱敏流程

```
┌─────────────────────────────────────────────────────────┐
│              字段加密脱敏流程                            │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  插入/更新操作                                           │
│    │                                                     │
│    ├─> MybatisInputInterceptor 拦截                      │
│    │   │                                                 │
│    │   ├─> 输入加密（enabled-input-encrypt: true）       │
│    │   │   - @TableFieldSafe 字段                        │
│    │   │   - 使用 AES/RSA 加密                           │
│    │   │                                                 │
│    │   └─> 输入脱敏（enabled-input-sensitive: true）     │
│    │       - @TableFieldSensitive 字段                   │
│    │       - 根据类型脱敏（手机、身份证、银行卡等）       │
│    │                                                     │
│    └─> 存储到数据库                                      │
│        - idCard: 加密后的密文                            │
│        - mobile: 脱敏后的值                              │
│        - bankCard: 加密后的密文                          │
│                                                          │
│  查询操作                                                │
│    │                                                     │
│    └─> MybatisOutputInterceptor 拦截                     │
│        │                                                 │
│        └─> 输出解密（enabled-output-decrypt: true）      │
│            - @TableFieldSafe 字段                        │
│            - 使用 AES/RSA 解密                           │
│            - 返回明文                                    │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 4. MybatisPlusInterceptor - 插件集合

MyBatis Plus 插件集合，包含多个内置插件。

#### 配置示例

```yaml
mybatis-plus:
  plugin:
    enabled: true  # 启用插件（默认 true）
```

#### 功能说明

MybatisPlusInterceptor 包含以下插件：

1. **ReplacePlaceholderInnerInterceptor** - 占位符替换插件
2. **OptimisticLockerInnerInterceptor** - 乐观锁插件
3. **PaginationInnerInterceptor** - 分页插件
4. **BlockAttackInnerInterceptor** - 防全表更新删除插件
5. **TenantLineInnerInterceptor** - 多租户插件（可选）

#### 使用示例

##### 1. 乐观锁

```java
@Data
@TableName("t_product")
public class ProductDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String name;
    
    private Integer stock;
    
    @Version  // 乐观锁版本号字段
    private Integer version;
}

@Service
public class ProductService {
    
    @Resource
    private ProductMapper productMapper;
    
    public boolean deductStock(Long id, Integer quantity) {
        ProductDO product = productMapper.selectById(id);
        
        if (product.getStock() < quantity) {
            return false;
        }
        
        product.setStock(product.getStock() - quantity);
        
        // 更新时自动处理版本号：
        // SQL: UPDATE t_product SET stock = ?, version = version + 1 
        //      WHERE id = ? AND version = ?
        int rows = productMapper.updateById(product);
        
        return rows > 0;  // 如果版本号不匹配，返回 false
    }
}
```

##### 2. 分页

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    public IPage<UserDO> pageUsers(Integer page, Integer size) {
        Page<UserDO> pageParam = new Page<>(page, size);
        
        // 自动添加分页条件：
        // SQL: SELECT * FROM t_user LIMIT 0, 10
        return userMapper.selectPage(pageParam, null);
    }
}
```

##### 3. 防全表更新删除

```java
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    // ❌ 全表更新会被拦截
    public void updateAll() {
        UserDO user = new UserDO();
        user.setName("新名称");
        
        // 抛出异常：Prohibition of full table update operation
        userMapper.update(user, null);
    }
    
    // ❌ 全表删除会被拦截
    public void deleteAll() {
        // 抛出异常：Prohibition of full table delete operation
        userMapper.delete(null);
    }
    
    // ✅ 带 WHERE 条件的更新
    public void updateByOrgId(Long orgId) {
        UserDO user = new UserDO();
        user.setName("新名称");
        
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getOrgId, orgId);
        
        userMapper.update(user, wrapper);
    }
}
```

## 配置属性

### MybatisProperty - MyBatis 配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `mybatis-plus.plugin.enabled` | 是否启用插件 | `true` |
| `mybatis-plus.tenant.enabled` | 是否启用多租户 | `false` |
| `mybatis-plus.tenant.tenant-id-column` | 租户 ID 字段名 | `org_id` |
| `mybatis-plus.tenant.ignored-table` | 忽略的表 | `[]` |
| `mybatis-plus.field.fill.enabled` | 是否启用字段自动填充 | `true` |
| `mybatis-plus.field.fill.enabled-insert-strict` | 插入是否严格模式 | `true` |
| `mybatis-plus.field.crypto.enabled` | 是否启用字段加密脱敏 | `false` |
| `mybatis-plus.field.crypto.enabled-input-encrypt` | 启用输入加密 | `false` |
| `mybatis-plus.field.crypto.enabled-input-sensitive` | 启用输入脱敏 | `false` |
| `mybatis-plus.field.crypto.enabled-output-decrypt` | 启用输出解密 | `false` |
| `mybatis-plus.field.crypto.enabled-sign` | 启用加签 | `false` |
| `mybatis-plus.field.crypto.algorithm` | 加密算法 | `AES` |
| `mybatis-plus.field.crypto.key` | 密钥 | `null` |
| `mybatis-plus.field.crypto.public-key` | 公钥 | `null` |
| `mybatis-plus.field.crypto.private-key` | 私钥 | `null` |
| `mybatis-plus.field.crypto.secret-key` | 签名密钥 | `null` |

## 最佳实践

### 1. 字段自动填充

```java
// ✅ 推荐：继承 DbBaseDO
@Data
@TableName("t_user")
public class UserDO extends DbBaseDO {
    // 自动包含公共字段
}

// ✅ 推荐：启用严格模式
mybatis-plus:
  field:
    fill:
      enabled-insert-strict: true

// ❌ 不推荐：手动设置公共字段
public void saveUser(UserDTO dto) {
    UserDO user = new UserDO();
    user.setCreatedTimestamp(System.currentTimeMillis());  // 不推荐
    user.setCreatedBy("张三");  // 不推荐
    userMapper.insert(user);
}
```

### 2. 多租户

```java
// ✅ 推荐：启用多租户
mybatis-plus:
  tenant:
    enabled: true
    tenant-id-column: org_id

// ✅ 推荐：配置忽略的表
mybatis-plus:
  tenant:
    ignored-table:
      - t_sys_config
      - t_sys_dict

// ❌ 不推荐：手动添加租户过滤
public List<UserDO> listUsers() {
    LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserDO::getOrgId, getCurrentOrgId());  // 不推荐，应该自动处理
    return userMapper.selectList(wrapper);
}
```

### 3. 字段加密脱敏

```java
// ✅ 推荐：使用注解标记字段
@TableFieldSafe
private String idCard;

@TableFieldSensitive(type = SensitiveTypeEnum.MOBILE)
private String mobile;

// ✅ 推荐：启用加密和脱敏
mybatis-plus:
  field:
    crypto:
      enabled: true
      enabled-input-encrypt: true
      enabled-input-sensitive: true
      enabled-output-decrypt: true

// ❌ 不推荐：手动加密
public void saveUser(UserDTO dto) {
    UserDO user = new UserDO();
    user.setIdCard(encrypt(dto.getIdCard()));  // 不推荐，应该自动处理
    userMapper.insert(user);
}
```

### 4. 乐观锁

```java
// ✅ 推荐：使用 @Version 注解
@Version
private Integer version;

// ✅ 推荐：捕获乐观锁异常
public boolean deductStock(Long id, Integer quantity) {
    try {
        ProductDO product = productMapper.selectById(id);
        product.setStock(product.getStock() - quantity);
        return productMapper.updateById(product) > 0;
    } catch (OptimisticLockingFailureException e) {
        log.warn("乐观锁冲突，库存扣减失败");
        return false;
    }
}
```

## 注意事项

1. **字段自动填充**：需要设置 UserContextHolder，否则使用默认用户
2. **多租户**：需要设置 UserContextHolder，否则无法获取租户 ID
3. **字段加密**：加密后的数据长度会增加，数据库字段长度需要预留足够空间
4. **乐观锁**：更新时需要先查询再更新，不能直接 new 对象更新
5. **防全表更新删除**：必须带 WHERE 条件，否则会被拦截
6. **分页插件**：物理分页，不是内存分页

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 DbBaseDO、BaseEnum 等
- `iwindplus-base-util`：工具类模块，提供加密、脱敏工具
- `iwindplus-base-redis`：Redis 模块，提供 MybatisRedisCache
