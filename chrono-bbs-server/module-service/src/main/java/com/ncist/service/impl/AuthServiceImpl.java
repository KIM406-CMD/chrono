package com.ncist.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.ncist.common.enums.AuthErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.utils.JwtUtil;
import com.ncist.dto.ChangePasswordDTO;
import com.ncist.dto.ForgotCodeDTO;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.dto.ResetPasswordDTO;
import com.ncist.dto.UpdateProfileDTO;
import com.ncist.entity.User;
import com.ncist.mapper.UserMapper;
import com.ncist.service.AuthService;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.ForgotCodeVO;
import com.ncist.vo.LoginVO;
import com.ncist.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务实现。
 *
 * <p>基于 MySQL 中的用户表与 Redis 中的验证码/Token/重置验证码完成认证流程。
 * 验证码、重置验证码均为一次性使用；登录 Token 以 {@code token:{userId}} 存储，
 * 与 JWT 有效期保持一致（2 小时），实现服务端可控的单设备登录与强制下线。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户数据访问组件。 */
    private final UserMapper userMapper;

    /** Redis 字符串操作模板，用于验证码、Token 与重置验证码。 */
    private final StringRedisTemplate stringRedisTemplate;

    /** 密码编码器，使用 BCrypt 对密码进行加密与校验。 */
    private final PasswordEncoder passwordEncoder;

    /** 图形验证码 Redis key 前缀。 */
    private static final String CAPTCHA_PREFIX = "captcha:";
    /** 图形验证码有效期（分钟）。 */
    private static final long CAPTCHA_TIMEOUT = 2;
    /** 登录 Token Redis key 前缀。 */
    private static final String TOKEN_PREFIX = "token:";
    /** 登录 Token 有效期（小时），与 JWT 有效期一致。 */
    private static final long TOKEN_TIMEOUT = 2;      // 小时，与 JWT 有效期一致
    /** 重置密码验证码 Redis key 前缀。 */
    private static final String RESET_CODE_PREFIX = "reset:code:";
    /** 重置密码时验证码与用户 ID 映射的 Redis key 前缀。 */
    private static final String RESET_ACCOUNT_PREFIX = "reset:account:";
    /** 重置密码验证码有效期（分钟）。 */
    private static final long RESET_TIMEOUT = 5;     // 分钟

    /**
     * {@inheritDoc}
     *
     * <p>验证码内容以 uuid 为键写入 Redis，有效期 2 分钟。</p>
     */
    @Override
    public CaptchaVO getCaptcha() {
        String uuid = createUuid();
        LineCaptcha captcha = createLineCaptcha();
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64Data();

        stringRedisTemplate.opsForValue()
                .set(CAPTCHA_PREFIX + uuid, code, CAPTCHA_TIMEOUT, TimeUnit.MINUTES);

        return CaptchaVO.builder()
                .uuid(uuid)
                .image(imageBase64)
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>验证码校验通过后立即删除，防止重复使用；注册成功后直接生成登录态并写入 Redis。</p>
     */
    @Override
    public LoginVO register(RegisterDTO registerDTO) {
        validateAndDeleteCaptcha(registerDTO.getCaptchaUuid(), registerDTO.getCaptchaCode());

        if (userMapper.selectByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(AuthErrorCode.USERNAME_EXISTS);
        }
        if (userMapper.selectByPhone(registerDTO.getPhone()) != null) {
            throw new BusinessException(AuthErrorCode.PHONE_EXISTS);
        }
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .phone(registerDTO.getPhone())
                .email(registerDTO.getEmail())
                // 注册固定为普通用户，商家角色只能由入驻审核通过后升级
                .role(UserRoleEnum.USER.getCode())
                .nickname(registerDTO.getUsername())
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        return buildLoginVO(user);
    }

    /**
     * {@inheritDoc}
     *
     * <p>登录成功后新 Token 覆盖 Redis 中的旧 Token，旧 Token 随即失效。</p>
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        validateAndDeleteCaptcha(loginDTO.getCaptchaUuid(), loginDTO.getCaptchaCode());

        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USERNAME_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.PASSWORD_ERROR);
        }

        return buildLoginVO(user);
    }

    /** {@inheritDoc} */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = requireUser(userId);
        return toUserInfoVO(user);
    }

    /** {@inheritDoc} */
    @Override
    public UserInfoVO updateProfile(Long userId, UpdateProfileDTO dto) {
        requireUser(userId);
        userMapper.updateProfile(userId, dto.getNickname(), dto.getPhone(), dto.getEmail(), dto.getAvatar());
        return toUserInfoVO(requireUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>密码更新后删除 {@code token:{userId}}，使当前 Token 立即失效。</p>
     */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.PASSWORD_ERROR);
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword()));
        // 改密后强制下线
        stringRedisTemplate.delete(TOKEN_PREFIX + userId);
    }

    /** {@inheritDoc} */
    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(TOKEN_PREFIX + userId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>重置验证码与用户 ID 映射分别写入两个 Redis key，有效期 5 分钟；
     * 当前阶段不接入短信/邮件通道，验证码直接记录日志并返回。</p>
     */
    @Override
    public ForgotCodeVO sendForgotCode(ForgotCodeDTO dto) {
        validateAndDeleteCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());

        User user = userMapper.selectByAccount(dto.getAccount());
        if (user == null) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_NOT_FOUND);
        }
        String code = RandomUtil.randomNumbers(6);
        String uuid = createUuid();
        stringRedisTemplate.opsForValue()
                .set(RESET_CODE_PREFIX + uuid, code, RESET_TIMEOUT, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue()
                .set(RESET_ACCOUNT_PREFIX + uuid, String.valueOf(user.getId()), RESET_TIMEOUT, TimeUnit.MINUTES);
        // 课程设计阶段模拟短信/邮件发送
        log.info("[模拟发送] 账号({}) 的重置密码验证码为 {}，{} 分钟内有效", maskAccount(user), code, RESET_TIMEOUT);

        return ForgotCodeVO.builder()
                .uuid(uuid)
                .maskAccount(maskAccount(user))
                .code(code)
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>验证码校验通过后立即删除两个 Redis key，确保一次性使用；
     * 密码更新后删除该用户 Token。</p>
     */
    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        String codeKey = RESET_CODE_PREFIX + dto.getUuid();
        String accountKey = RESET_ACCOUNT_PREFIX + dto.getUuid();

        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        String cachedUserId = stringRedisTemplate.opsForValue().get(accountKey);
        if (cachedCode == null || !cachedCode.equals(dto.getCode()) || cachedUserId == null) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_ERROR);
        }
        // 验证码一次性使用
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(accountKey);

        User user = requireUser(Long.valueOf(cachedUserId));
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(dto.getNewPassword()));
        // 重置密码后强制下线
        stringRedisTemplate.delete(TOKEN_PREFIX + user.getId());
    }

    // ===== 私有方法 =====

    /**
     * 校验图形验证码并立即删除，保证验证码一次性使用。
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码（忽略大小写）
     * @throws BusinessException 验证码不存在、已过期或输入错误时抛出
     */
    private void validateAndDeleteCaptcha(String uuid, String code) {
        String captchaCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(code)) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_ERROR);
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + uuid);
    }

    /**
     * 按 ID 查询用户，不存在时抛出业务异常。
     *
     * @param userId 用户 ID
     * @return 用户实体
     * @throws BusinessException 用户不存在时抛出
     */
    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USERNAME_NOT_FOUND);
        }
        return user;
    }

    /**
     * 生成 JWT 并写入 Redis，组装登录结果。
     *
     * <p>Redis 中仅保留该用户最新的 Token，实现单设备登录。</p>
     *
     * @param user 已通过认证的用户
     * @return 登录结果，包含 Token 与用户信息
     */
    private LoginVO buildLoginVO(User user) {
        String token = JwtUtil.generateToken(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));
        // JWT + Redis 双校验：服务端记录当前有效 token（单设备登录，重复登录踢掉旧 token）
        stringRedisTemplate.opsForValue()
                .set(TOKEN_PREFIX + user.getId(), token, TOKEN_TIMEOUT, TimeUnit.HOURS);

        return LoginVO.builder()
                .token(token)
                .userInfo(toUserInfoVO(user))
                .build();
    }

    /**
     * 将用户实体复制为视图对象。
     *
     * @param user 用户实体
     * @return 用户信息视图对象
     */
    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }

    /**
     * 对账号进行脱敏展示：手机号保留前 3 位与后 4 位，邮箱保留首字符与域名。
     *
     * @param user 用户实体
     * @return 脱敏后的账号；无手机号与邮箱时返回用户名
     */
    private String maskAccount(User user) {
        String phone = user.getPhone();
        if (phone != null && !phone.isEmpty()) {
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            int atIndex = email.indexOf('@');
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return user.getUsername();
    }

    /**
     * 生成无短横线的 UUID，用作验证码唯一标识。
     *
     * @return 32 位 UUID 字符串
     */
    public String createUuid() {
        return IdUtil.simpleUUID();
    }

    /**
     * 创建图形验证码（130x40，4 位字符，40 条干扰线）。
     *
     * @return 线性干扰图形验证码
     */
    public LineCaptcha createLineCaptcha() {
        return CaptchaUtil.createLineCaptcha(130, 40, 4, 40);
    }
}
