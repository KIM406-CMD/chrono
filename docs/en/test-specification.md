# Test Specification

> [简体中文](../zh/test-specification.md) | English

## 1. Test Layers

This project adopts a three-layer test system, progressively verifying system correctness layer by layer.

| Layer | Scope | Dependency Handling | Execution Speed | Stability |
|------|------|----------|----------|--------|
| **Unit Test** | Single class/method logic | All mocked | Milliseconds | Very high |
| **Integration Test** | Multi-component collaboration (Service + Mapper + DB) | Partially real (H2 in-memory DB) | Seconds | High |
| **E2E Test** | Complete HTTP request chain | All real | Milliseconds to seconds | Medium |

### 1.1 Unit Test

- Test target: Service-layer business logic
- External dependencies (database, Redis) are all mocked with Mockito
- Does not start the Spring container; only validates Java code logic

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

### 1.2 Integration Test

- Test target: complete chain of Service + Mapper + real database
- Uses real MySQL and Redis (shared with the development environment)
- Validates SQL correctness, transaction rollback, and data consistency
- Requires starting the Spring container; uses `@SpringBootTest`
- Test data operations must be **rolled back** or **cleaned up** so they do not pollute the database

```java
@SpringBootTest
@Transactional  // automatically roll back after the test so data is not polluted
@DisplayName("AuthService 集成测试")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;
}
```

### 1.3 E2E Test

- Test target: complete HTTP chain Controller → Service → Mapper → DB
- Simulates real HTTP requests and validates the API contract, parameter validation, and response format
- Uses `MockMvc` or `TestRestTemplate`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("AuthController E2E 测试")
class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;
}
```

### 1.4 Frontend Tests

- **Unit tests**: Vitest (`src/**/*.spec.ts`), currently covering card Luhn/expiry/CVV validation and rich-text sanitization.
- **E2E smoke tests**: Playwright (`e2e/*.spec.ts`), starting the frontend with `dev:mock` to verify guest browsing of the home page, product detail and coupon center.
- **Code quality**: ESLint (`eslint.config.js`); run `npm run lint` before committing.

---

## 2. Technology Stack

| Component | Purpose | Version |
|------|------|------|
| JUnit 5 | Test framework | Bundled with Spring Boot 3.4.5 |
| Mockito | Unit test mocking | Bundled with spring-boot-starter-test |
| MockMvc | E2E HTTP simulation | Bundled with spring-boot-starter-test |
| MySQL | Integration test database (real DB) | 8.x |
| Redis | Integration test cache (real service) | 7.x |
| Maven Surefire | Test execution + reporting | 3.x (bundled with Spring Boot) |

---

## 3. Integration Test Database Plan

Use **real MySQL + Redis**: MySQL uses a dedicated `chrono_bbs_test` database, and Redis shares the same instance as the development environment.

### 3.1 Test Configuration File

`src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chrono_bbs_test?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
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

## 4. Coverage Targets

| Level | Target | Description |
|------|------|------|
| Key modules | **≥70%** | Core business such as authentication, payment, and orders |
| General modules | **≥60%** | Auxiliary modules such as categories and addresses |
| Utility classes | As needed | JWT utilities, encryption utilities, etc. |

---

## 5. Test Data Management

### 5.1 Principles

- Each test method constructs data **independently**, without relying on side effects of other tests
- Use the **Builder pattern** + **factory methods** to reuse data creation logic
- Extract constants into the static fields of the class

### 5.2 Example

```java
class UserServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PHONE = "13800000001";

    // Factory method: quickly build a test DTO
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

## 6. Test Scenario Coverage Standard

The tests for each Service method must cover:

### 6.1 Happy Path

Verify that when the input is valid, the method returns the expected result and calls dependencies correctly.

```java
@Test
@DisplayName("应当成功注册新用户并返回 Token")
void shouldReturnLoginVOWithTokenAndUserInfo() { ... }
```

### 6.2 Key Exceptions (Sad Path)

At least cover the following exception types (choose those that actually exist in the business):

| Exception Type | Example |
|----------|------|
| Parameter validation failure | Invalid captcha, expired captcha |
| Business rule conflict | Username already exists, phone number already registered |
| Resource does not exist | User does not exist |
| Insufficient permission | No permission to access |

```java
@Test
@DisplayName("验证码错误时应当抛出 BusinessException")
void shouldThrowWhenCaptchaCodeIsWrong() { ... }

@Test
@DisplayName("用户名已存在时应当抛出 BusinessException")
void shouldThrowWhenUsernameExists() { ... }
```

---

## 7. Test Report

Use the default **Maven Surefire** report format and view the results directly from the command line.

### 7.1 Commands

```bash
# Run all tests
mvn test

# Run a specific module
mvn test -pl module-start

# Run a specific test class
mvn test -pl module-start -Dtest=AuthServiceImplTest

# Run a specific test method
mvn test -pl module-start -Dtest=AuthServiceImplTest#shouldRegisterNewUser

# Frontend quality checks
cd chrono-bbs-client
npm run lint
npm run test
npm run test:e2e   # first run: npx playwright install chromium
```

### 7.2 Report Location

```
module-start/target/surefire-reports/
├── com.ncist.service.AuthServiceImplTest.txt    # Text report
└── TEST-com.ncist.service.AuthServiceImplTest.xml # XML report
```

### 7.3 Output Example

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. Test Execution Strategy

| Scenario | Execution Method |
|------|----------|
| Development phase | Run the target test class/method directly from the local IDE |
| Pre-commit check | Run all tests with `mvn test` |
| Integration tests | `mvn test -P integration` activates the integration test profile |

---

## 9. Naming Convention

### 9.1 Method Naming

Use the dual naming approach of **semantic English names** + **concise Chinese `@DisplayName` descriptions**.

| Part | Convention | Example |
|------|------|------|
| Method name | `should` + expected behavior + `When` + condition | `shouldThrowWhenCaptchaCodeIsWrong` |
| @DisplayName | Concise Chinese description | `"验证码错误时应当抛出 BusinessException"` |

### 9.2 Class Naming

| Test Type | Class Name Format | Example |
|----------|----------|------|
| Unit test | `{ClassName}Test` | `AuthServiceImplTest` |
| Integration test | `{ClassName}IntegrationTest` | `AuthServiceIntegrationTest` |
| E2E test | `{ClassName}E2ETest` | `AuthControllerE2ETest` |

### 9.3 Package Structure

```
module-start/src/test/java/com/ncist/
├── service/          # Service unit tests
│   └── AuthServiceImplTest.java
├── integration/      # Integration tests
│   └── AuthServiceIntegrationTest.java
└── e2e/              # E2E tests
    └── AuthControllerE2ETest.java
```
