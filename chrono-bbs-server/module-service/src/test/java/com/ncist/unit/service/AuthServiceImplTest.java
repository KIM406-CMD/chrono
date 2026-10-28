package com.ncist.unit.service;

import cn.hutool.captcha.LineCaptcha;
import com.ncist.common.enums.AuthErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.entity.User;
import com.ncist.mapper.UserMapper;
import com.ncist.service.impl.AuthServiceImpl;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 单元测试")
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private LineCaptcha lineCaptcha;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_UUID = "abc123def456";
    private static final String TEST_CODE = "A3B7";
    private static final String TEST_BASE64 = "data:image/png;base64,iVBORw0KGgoTEST";
    private static final String TEST_ENCODED_PASSWORD = "$2a$10$dummyEncodedPasswordHashValue";

    @BeforeEach
    void setUp() {
    }

    private void setupCaptchaMocks() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doReturn(TEST_UUID).when(authService).createUuid();
        doReturn(lineCaptcha).when(authService).createLineCaptcha();
        when(lineCaptcha.getCode()).thenReturn(TEST_CODE);
        when(lineCaptcha.getImageBase64Data()).thenReturn(TEST_BASE64);
    }

    private void setupLoginHappyPath() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("testuser")).thenReturn(buildUser(1L, "testuser", TEST_ENCODED_PASSWORD, 1, 1));
        when(passwordEncoder.matches("123456", TEST_ENCODED_PASSWORD)).thenReturn(true);
    }

    private void setupRegisterHappyPath() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername(anyString())).thenReturn(null);
        when(userMapper.selectByPhone(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn(TEST_ENCODED_PASSWORD);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));
    }

    // ==================== getCaptcha 成功场景 ====================

    @Test
    @DisplayName("getCaptcha 返回的 UUID 不为空")
    void shouldReturnNonEmptyUuid() {
        setupCaptchaMocks();
        CaptchaVO result = authService.getCaptcha();
        log.info("getCaptcha uuid -> {}", result.getUuid());
        assertNotNull(result.getUuid());
        assertFalse(result.getUuid().isEmpty());
    }

    @Test
    @DisplayName("getCaptcha 返回的 image 不为空")
    void shouldReturnNonEmptyImage() {
        setupCaptchaMocks();
        CaptchaVO result = authService.getCaptcha();
        log.info("getCaptcha image 长度 -> {}", result.getImage().length());
        assertNotNull(result.getImage());
        assertFalse(result.getImage().isEmpty());
    }

    @Test
    @DisplayName("getCaptcha 返回的 image 应以 data:image/png;base64, 开头")
    void shouldReturnImageWithCorrectPrefix() {
        setupCaptchaMocks();
        CaptchaVO result = authService.getCaptcha();
        log.info("getCaptcha image 前缀检查 -> {}", result.getImage().substring(0, Math.min(50, result.getImage().length())));
        assertTrue(result.getImage().startsWith("data:image/png;base64,"));
    }

    @Test
    @DisplayName("getCaptcha 应将验证码存入 Redis，key 格式为 captcha:{uuid}")
    void shouldStoreCaptchaInRedisWithCorrectKey() {
        setupCaptchaMocks();
        authService.getCaptcha();
        log.info("验证 Redis key -> captcha:{}", TEST_UUID);
        verify(valueOperations).set(eq("captcha:" + TEST_UUID), anyString(), eq(2L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("getCaptcha 存入 Redis 的值应为验证码文本")
    void shouldStoreCorrectCodeInRedis() {
        setupCaptchaMocks();
        authService.getCaptcha();
        log.info("Redis 存储验证码 -> {}", TEST_CODE);
        verify(valueOperations).set(anyString(), eq(TEST_CODE), anyLong(), any());
    }

    @Test
    @DisplayName("getCaptcha 存入 Redis 的 TTL 应为 2 分钟")
    void shouldSetRedisTTLTo2Minutes() {
        setupCaptchaMocks();
        authService.getCaptcha();
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        verify(valueOperations).set(anyString(), anyString(), ttlCaptor.capture(), unitCaptor.capture());
        assertEquals(2L, ttlCaptor.getValue());
        assertEquals(TimeUnit.MINUTES, unitCaptor.getValue());
        log.info("Redis TTL -> {} {}", ttlCaptor.getValue(), unitCaptor.getValue());
    }

    // ==================== 工具方法成功场景 ====================

    @Test
    @DisplayName("createUuid 应返回 32 位无横线字符串")
    void shouldReturn32CharUuidWithoutHyphens() {
        String uuid = authService.createUuid();
        log.info("createUuid -> {} (长度={})", uuid, uuid.length());
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        assertFalse(uuid.contains("-"));
    }

    @Test
    @DisplayName("createLineCaptcha 返回对象 getCode 和 getImageBase64Data 不能为空")
    void shouldReturnValidLineCaptcha() {
        LineCaptcha captcha = authService.createLineCaptcha();
        log.info("LineCaptcha code -> {}", captcha.getCode());
        assertNotNull(captcha);
        assertNotNull(captcha.getCode());
        assertFalse(captcha.getCode().isEmpty());
        assertNotNull(captcha.getImageBase64Data());
        assertFalse(captcha.getImageBase64Data().isEmpty());
    }

    // ==================== register 成功场景 ====================

    @Test
    @DisplayName("register 注册成功后应删除 Redis 中的验证码")
    void shouldDeleteCaptchaAfterSuccessfulRegister() {
        setupRegisterHappyPath();
        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);
        authService.register(dto);
        verify(stringRedisTemplate).delete("captcha:" + TEST_UUID);
        log.info("验证码删除验证通过");
    }

    @Test
    @DisplayName("register 应对密码进行 BCrypt 加密")
    void shouldEncodePasswordWithBCrypt() {
        setupRegisterHappyPath();
        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);
        authService.register(dto);
        verify(passwordEncoder).encode("123456");
        log.info("密码加密验证通过 -> 原文 123456 已被 BCrypt 加密");
    }

    @Test
    @DisplayName("register 插入的用户信息应包含所有必填字段")
    void shouldInsertUserWithAllRequiredFields() {
        setupRegisterHappyPath();
        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);
        authService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertEquals("testuser", captured.getUsername());
        assertEquals(TEST_ENCODED_PASSWORD, captured.getPassword());
        assertEquals("13800000001", captured.getPhone());
        assertEquals("test@test.com", captured.getEmail());
        assertEquals(1, captured.getRole());
        assertEquals("testuser", captured.getNickname());
        assertEquals(1, captured.getStatus());
        assertNotNull(captured.getCreatedAt());
        assertNotNull(captured.getUpdatedAt());
        log.info("用户字段验证 -> username={}, role={}, status={}, nickname={}",
                captured.getUsername(), captured.getRole(), captured.getStatus(), captured.getNickname());
    }

    @Test
    @DisplayName("register 成功应返回 LoginVO（含 token 和 userInfo）")
    void shouldReturnLoginVOWithTokenAndUserInfo() {
        setupRegisterHappyPath();
        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);
        LoginVO result = authService.register(dto);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isEmpty());
        assertNotNull(result.getUserInfo());
        assertEquals("testuser", result.getUserInfo().getUsername());
        assertEquals(1, result.getUserInfo().getRole());
        log.info("LoginVO 验证 -> token: {}, username: {}",
                result.getToken().substring(0, Math.min(20, result.getToken().length())) + "...",
                result.getUserInfo().getUsername());
    }

    @Test
    @DisplayName("register 验证码忽略大小写")
    void shouldIgnoreCaptchaCodeCase() {
        setupRegisterHappyPath();
        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, "a3b7");
        LoginVO result = authService.register(dto);
        assertNotNull(result);
        log.info("大小写忽略测试通过 -> token 不为空: {}", result.getToken() != null);
    }

    // ==================== register 失败场景 ====================

    @Test
    @DisplayName("register 校验验证码失败应抛出 BusinessException")
    void shouldThrowWhenCaptchaCodeIsWrong() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn("CORRECT");

        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, "WRONG");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(dto));
        assertEquals(AuthErrorCode.CAPTCHA_ERROR.getCode(), ex.getCode());
        log.info("验证码错误异常 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("register 验证码已过期（Redis 中不存在）应抛出 BusinessException")
    void shouldThrowWhenCaptchaIsExpired() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(null);

        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(dto));
        assertEquals(AuthErrorCode.CAPTCHA_ERROR.getCode(), ex.getCode());
        log.info("验证码过期异常 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("register 用户名已存在应抛出 BusinessException")
    void shouldThrowWhenUsernameExists() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("existing")).thenReturn(new User());

        RegisterDTO dto = buildRegisterDTO("existing", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(dto));
        assertEquals(AuthErrorCode.USERNAME_EXISTS.getCode(), ex.getCode());
        log.info("用户名重复异常 -> code={}, message={}", ex.getCode(), ex.getMessage());

        verify(stringRedisTemplate).delete("captcha:" + TEST_UUID);
    }

    @Test
    @DisplayName("register 手机号已被注册应抛出 BusinessException")
    void shouldThrowWhenPhoneExists() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(userMapper.selectByPhone("13800000001")).thenReturn(new User());

        RegisterDTO dto = buildRegisterDTO("testuser", "13800000001", "test@test.com", 1, TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(dto));
        assertEquals(AuthErrorCode.PHONE_EXISTS.getCode(), ex.getCode());
        log.info("手机号重复异常 -> code={}, message={}", ex.getCode(), ex.getMessage());

        verify(stringRedisTemplate).delete("captcha:" + TEST_UUID);
    }

    // ==================== login 成功场景 ====================

    @Test
    @DisplayName("login 验证码正确 + 用户名存在 + 状态正常 + 密码正确应返回 LoginVO")
    void shouldLoginSuccessfully() {
        setupLoginHappyPath();
        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, TEST_CODE);
        LoginVO result = authService.login(dto);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isEmpty());
        assertNotNull(result.getUserInfo());
        assertEquals("testuser", result.getUserInfo().getUsername());
        log.info("登录成功 -> token: {}..., username: {}",
                result.getToken().substring(0, Math.min(20, result.getToken().length())),
                result.getUserInfo().getUsername());
    }

    @Test
    @DisplayName("login 成功应返回 userInfo 包含正确的用户信息")
    void shouldReturnCorrectUserInfoWhenLoginSuccessfully() {
        setupLoginHappyPath();
        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, TEST_CODE);
        LoginVO result = authService.login(dto);

        assertEquals("testuser", result.getUserInfo().getUsername());
        assertEquals(1L, result.getUserInfo().getId());
        assertEquals(1, result.getUserInfo().getRole());
        log.info("userInfo 验证 -> id={}, username={}, role={}",
                result.getUserInfo().getId(),
                result.getUserInfo().getUsername(),
                result.getUserInfo().getRole());
    }

    @Test
    @DisplayName("login 成功后应删除 Redis 中的验证码")
    void shouldDeleteCaptchaAfterLoginSuccessfully() {
        setupLoginHappyPath();
        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, TEST_CODE);
        authService.login(dto);
        verify(stringRedisTemplate).delete("captcha:" + TEST_UUID);
        log.info("login 验证码删除验证通过");
    }

    // ==================== login 失败场景 ====================

    @Test
    @DisplayName("login 验证码不存在应抛出 CAPTCHA_ERROR")
    void shouldThrowWhenCaptchaNotFoundDuringLogin() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(null);

        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.CAPTCHA_ERROR.getCode(), ex.getCode());
        log.info("login 验证码过期 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("login 验证码不匹配应抛出 CAPTCHA_ERROR")
    void shouldThrowWhenCaptchaMismatchDuringLogin() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn("CORRECT");

        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, "WRONG");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.CAPTCHA_ERROR.getCode(), ex.getCode());
        log.info("login 验证码不匹配 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("login 用户名不存在应抛出 USERNAME_NOT_FOUND")
    void shouldThrowWhenUsernameNotFoundDuringLogin() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("ghost")).thenReturn(null);

        LoginDTO dto = buildLoginDTO("ghost", "123456", TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.USERNAME_NOT_FOUND.getCode(), ex.getCode());
        log.info("login 用户名不存在 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("login 账号被禁用应抛出 ACCOUNT_DISABLED")
    void shouldThrowWhenAccountDisabled() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("testuser")).thenReturn(buildUser(1L, "testuser", TEST_ENCODED_PASSWORD, 1, 0));

        LoginDTO dto = buildLoginDTO("testuser", "123456", TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.ACCOUNT_DISABLED.getCode(), ex.getCode());
        log.info("login 账号被禁用 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    @Test
    @DisplayName("login 密码错误应抛出 PASSWORD_ERROR")
    void shouldThrowWhenPasswordWrong() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + TEST_UUID)).thenReturn(TEST_CODE);
        when(userMapper.selectByUsername("testuser")).thenReturn(buildUser(1L, "testuser", TEST_ENCODED_PASSWORD, 1, 1));
        when(passwordEncoder.matches("wrongpass", TEST_ENCODED_PASSWORD)).thenReturn(false);

        LoginDTO dto = buildLoginDTO("testuser", "wrongpass", TEST_UUID, TEST_CODE);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.PASSWORD_ERROR.getCode(), ex.getCode());
        log.info("login 密码错误 -> code={}, message={}", ex.getCode(), ex.getMessage());
    }

    private User buildUser(Long id, String username, String password, Integer role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private LoginDTO buildLoginDTO(String username, String password, String captchaUuid, String captchaCode) {
        return LoginDTO.builder()
                .username(username)
                .password(password)
                .captchaUuid(captchaUuid)
                .captchaCode(captchaCode)
                .build();
    }

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