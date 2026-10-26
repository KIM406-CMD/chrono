# 测试规范

## 1. 测试分层

本项目采用三层测试体系，逐层递进验证系统正确性。

| 层次 | 范围 | 依赖处理 | 执行速度 | 稳定性 |
|------|------|----------|----------|--------|
| **单元测试** | 单个类/方法逻辑 | 全部 Mock | 毫秒级 | 极高 |
| **集成测试** | 多组件协作（Service + Mapper + DB） | 部分真实（H2 内存库） | 秒级 | 高 |
| **E2E 测试** | 完整 HTTP 请求链路 | 全部真实 | 毫秒~秒级 | 中 |

### 1.1 单元测试

- 测试对象：Service 层业务逻辑
- 外部依赖（数据库、Redis）全部使用 Mockito 模拟
- 不启动 Spring 容器，只验证 Java 代码逻辑

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 单元测试")
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Spy
    @InjectMocks
    private AuthServiceImpl authService;
}
```

### 1.2 集成测试

- 测试对象：Service + Mapper + 真实数据库完整链路
- 使用真实 MySQL 和 Redis（与开发环境共用）
- 验证 SQL 正确性、事务回滚、数据一致性
- 需要启动 Spring 容器，使用 `@SpringBootTest`
- 测试数据操作必须**回滚**或**清理**，不污染数据库

```java
@SpringBootTest
@Transactional  // 测试结束后自动回滚，不污染数据
@DisplayName("AuthService 集成测试")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;
}
```

### 1.3 E2E 测试

- 测试对象：Controller → Service → Mapper → DB 完整 HTTP 链路
- 模拟真实 HTTP 请求，验证接口契约、参数校验、响应格式
- 使用 `MockMvc` 或 `TestRestTemplate`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("AuthController E2E 测试")
class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;
}
```

---

## 2. 技术栈

| 组件 | 用途 | 版本 |
|------|------|------|
| JUnit 5 | 测试框架 | 随 Spring Boot 3.4.5 |
| Mockito | 单元测试 Mock | 随 spring-boot-starter-test |
| MockMvc | E2E 模拟 HTTP | 随 spring-boot-starter-test |
| MySQL | 集成测试数据库（真实库） | 8.x |
| Redis | 集成测试缓存（真实服务） | 7.x |
| Maven Surefire | 测试执行 + 报告 | 3.x（随 Spring Boot） |

---

## 3. 集成测试数据库方案

使用**真实 MySQL + Redis**，与开发环境共用同一套基础设施。

### 3.1 测试配置文件

`src/test/resources/application-test.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chrono_bbs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: chrono
    password: chrono123
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379

mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

---

## 4. 覆盖率目标

| 级别 | 目标 | 说明 |
|------|------|------|
| 关键模块 | **≥70%** | 认证、支付、订单等核心业务 |
| 普通模块 | **≥60%** | 分类、地址等辅助模块 |
| 工具类 | 按需 | JWT 工具、加密工具等 |

---

## 5. 测试数据管理

### 5.1 原则

- 每个测试方法**独立**构造数据，不依赖其他测试的副作用
- 使用 **Builder 模式** + **工厂方法**复用数据创建逻辑
- 常量抽到类的静态字段中

### 5.2 示例

```java
class UserServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PHONE = "13800000001";

    // 工厂方法：快速创建测试 DTO
    private RegisterDTO buildRegisterDTO(String username, String phone, String email,
                                          Integer role, String captchaUuid, String captchaCode) {
        return RegisterDTO.builder()
                .username(username)
                .password("123456")
                .phone(phone)
                .email(email)
                .role(role)
                .captchaUuid(captchaUuid)
                .captchaCode(captchaCode)
                .build();
    }
}
```

---

## 6. 测试场景覆盖标准

每个 Service 方法的测试必须覆盖：

### 6.1 正常流程（Happy Path）

验证输入合法时，方法返回预期结果、正确调用依赖。

```java
@Test
@DisplayName("应当成功注册新用户并返回 Token")
void shouldReturnLoginVOWithTokenAndUserInfo() { ... }
```

### 6.2 关键异常（Sad Path）

至少覆盖以下异常类型（根据业务实际存在的情况选取）：

| 异常类型 | 示例 |
|----------|------|
| 参数校验失败 | 验证码错误、验证码过期 |
| 业务规则冲突 | 用户名已存在、手机号已注册 |
| 资源不存在 | 用户不存在 |
| 权限不足 | 无权限访问 |

```java
@Test
@DisplayName("验证码错误时应当抛出 BusinessException")
void shouldThrowWhenCaptchaCodeIsWrong() { ... }

@Test
@DisplayName("用户名已存在时应当抛出 BusinessException")
void shouldThrowWhenUsernameExists() { ... }
```

---

## 7. 测试报告

使用 **Maven Surefire** 默认报告格式，通过命令行直接查看结果。

### 7.1 执行命令

```bash
# 运行全部测试
mvn test

# 运行指定模块
mvn test -pl module-start

# 运行指定测试类
mvn test -pl module-start -Dtest=AuthServiceImplTest

# 运行指定测试方法
mvn test -pl module-start -Dtest=AuthServiceImplTest#shouldRegisterNewUser
```

### 7.2 报告位置

```
module-start/target/surefire-reports/
├── com.ncist.service.AuthServiceImplTest.txt    # 文本报告
└── TEST-com.ncist.service.AuthServiceImplTest.xml # XML 报告
```

### 7.3 输出示例

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. 测试执行策略

| 场景 | 执行方式 |
|------|----------|
| 开发阶段 | 本地 IDE 直接运行目标测试类/方法 |
| 提交前检查 | `mvn test` 跑全部测试 |
| 集成测试 | `mvn test -P integration` 激活集成测试 profile |

---

## 9. 命名规范

### 9.1 方法命名

采用 **英文语义化命名** + **`@DisplayName` 中文描述** 的双重命名方式。

| 部分 | 规范 | 示例 |
|------|------|------|
| 方法名 | `should` + 预期行为 + `When` + 条件 | `shouldThrowWhenCaptchaCodeIsWrong` |
| @DisplayName | 简洁中文描述 | `"验证码错误时应当抛出 BusinessException"` |

### 9.2 类命名

| 测试类型 | 类名格式 | 示例 |
|----------|----------|------|
| 单元测试 | `{类名}Test` | `AuthServiceImplTest` |
| 集成测试 | `{类名}IntegrationTest` | `AuthServiceIntegrationTest` |
| E2E 测试 | `{类名}E2ETest` | `AuthControllerE2ETest` |

### 9.3 包结构

```
module-start/src/test/java/com/ncist/
├── service/          # Service 单元测试
│   └── AuthServiceImplTest.java
├── integration/      # 集成测试
│   └── AuthServiceIntegrationTest.java
└── e2e/              # E2E 测试
    └── AuthControllerE2ETest.java
```