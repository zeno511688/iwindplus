
# iwindplus 项目开发规范指南

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。本规范基于项目实际结构与依赖配置生成。

## 一、项目概览

- **项目名称**：iwindplus
- **工作目录**：`/Users/zengdegui/IdeaProjects/iwindplus-github/iwindplus`
- **代码作者**：zengdegui
- **操作系统**：Mac OS X
- **构建工具**：Maven
- **SDK版本**：JDK 17.0.5 (Java 17)

## 二、目录结构

项目采用多模块 Maven 架构，主要分为基础组件库 (`iwindplus-base`) 和微服务应用 (`iwindplus-server`)。

```text
iwindplus
├── iwindplus-base (基础组件模块)
│   ├── iwindplus-base-domain (通用实体模块)
│   ├── iwindplus-base-util (工具类模块)
│   ├── iwindplus-base-web (Web通用配置)
│   ├── iwindplus-base-webmvc (Web MVC配置)
│   ├── iwindplus-base-mybatis (MyBatis Plus数据库模块)
│   ├── iwindplus-base-redis (Redis缓存模块)
│   ├── iwindplus-base-feign (Feign远程调用模块)
│   ├── iwindplus-base-oss (对象存储模块)
│   ├── iwindplus-base-mail (邮件模块)
│   ├── iwindplus-base-rocket (RocketMQ模块)
│   ├── iwindplus-base-kafka (Kafka模块)
│   ├── iwindplus-base-rabbit (RabbitMQ模块)
│   ├── iwindplus-base-monitor (监控模块)
│   ├── iwindplus-base-swagger (Swagger文档模块)
│   ├── iwindplus-base-i18n (国际化模块)
│   └── ... (其他基础组件)
├── iwindplus-server (微服务业务模块)
│   ├── iwindplus-auth (认证服务)
│   │   ├── iwindplus-auth-api (API接口定义)
│   │   ├── iwindplus-auth-client (Feign客户端)
│   │   ├── iwindplus-auth-domain (领域模型)
│   │   └── iwindplus-auth-server (服务实现)
│   ├── iwindplus-mgt (管理服务)
│   ├── iwindplus-log (日志服务)
│   ├── iwindplus-setup (设置服务)
│   ├── iwindplus-flow (流程服务)
│   ├── iwindplus-im (即时通讯服务)
│   ├── iwindplus-binlog (Binlog服务)
│   └── iwindplus-monitor (监控服务)
└── pom.xml (父POM)
```

## 三、技术栈要求

- **核心框架**：Spring Boot 3.x / Spring Cloud
- **语言版本**：Java 17
- **微服务组件**：
  - Spring Cloud Alibaba (Nacos 注册中心/配置中心)
  - Spring Cloud OpenFeign (服务调用)
  - Spring Cloud LoadBalancer (负载均衡)
- **数据库相关**：
  - MyBatis Plus (ORM框架，替代JPA)
  - MySQL
  - Redis / Redisson
- **容器与Web**：
  - Undertow (Web服务器，**禁止**使用 Tomcat)
- **工具库**：
  - Lombok (简化代码)
  - Hutool (常用工具集)
  - MapStruct (对象转换，推荐使用)
- **其他中间件**：
  - RocketMQ / Kafka / RabbitMQ (消息队列)
  - Resilience4j (熔断降级)
  - Snail Job / XXL-Job (分布式任务调度)

## 四、分层架构规范

根据项目依赖，项目使用 MyBatis Plus 作为持久层框架，架构分层调整如下：

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库；参数校验使用 `@Valid`；统一返回包装类     |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 继承 `IService` (MyBatis Plus)；事务注解 `@Transactional` 仅用于此类 |
| **Mapper**     | 数据库访问与 SQL 映射             | 继承 `BaseMapper`；复杂 SQL 编写在对应 XML 文件中             |
| **Entity/DO**  | 映射数据库表结构                   | 使用 `@TableName` 等注解；禁止直接返回给前端                   |
| **Client**     | Feign 远程调用接口定义             | 定义接口供其他服务调用，需配合 `FeignClient` 使用              |

### 模块依赖原则

- **API 模块 (`*-api`)**：存放 Feign 接口定义、DTO、常量，仅依赖 `domain` 和基础库。
- **Server 模块 (`*-server`)**：具体业务实现，依赖 `api`、`client` 及 `iwindplus-base-*` 组件。
- **Client 模块 (`*-client`)**：内部服务调用的 Feign 客户端实现。

## 五、安全与性能规范

### 输入校验

- 使用 JSR-303 校验注解（位于 `jakarta.validation.constraints.*`）。
- Controller 层方法参数需添加 `@Valid` 或 `@Validated` 注解。

### 数据库操作

- **禁止**在循环中执行 SQL 操作。
- 大批量数据操作使用 MyBatis Plus 的 `saveBatch` 或自定义批量 SQL。
- 分页查询使用 `MybatisPlusInterceptor` 内置的分页插件。

### Web 容器选择

- 项目默认排除 `spring-boot-starter-tomcat`，**必须**使用 `spring-boot-starter-undertow` 以提升性能。

## 六、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  | 备注                       |
|------------|----------------------|-----------------------|----------------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |                            |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |                            |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |                            |
| 包名       | 全小写               | `com.iwindplus.auth`  |                            |

### 注释规范

- **语言要求**：注释内容必须使用**中文**。
- **类注释**：必须包含作者、日期、类功能描述。
  ```java
  /**
   * 用户服务实现类.
   *
   * @author zengdegui
   * @since 2026-08-20
   */
  public class UserServiceImpl { ... }
  ```
- **方法注释**：需说明功能、参数含义及返回值。

### 实体类简化工具

- 使用 Lombok 注解：
  - `@Data` (Getter/Setter/ToString/EqualsAndHashCode)
  - `@NoArgsConstructor` (无参构造)
  - `@AllArgsConstructor` (全参构造)
  - `@Builder` (构建者模式，推荐用于 DTO/VO)

## 七、扩展性与日志规范

### 接口优先原则

- 业务 Service 应优先定义接口，实现类放在 `impl` 包下。

### 日志记录

- 使用 `@Slf4j` 注解。
- 日志级别严格控制，生产环境避免输出 DEBUG 级别日志。
- 日志文件配置使用 `logstash-logback-encoder` (项目已集成)。

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，通用逻辑下沉至 `iwindplus-base` 模块 |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
