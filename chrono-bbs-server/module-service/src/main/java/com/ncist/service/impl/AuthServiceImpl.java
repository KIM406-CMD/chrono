package com.ncist.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.ncist.common.enums.AuthErrorCode;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TIMEOUT = 2;
    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_TIMEOUT = 2;      // 小时，与 JWT 有效期一致
    private static final String RESET_CODE_PREFIX = "reset:code:";
    private static final String RESET_ACCOUNT_PREFIX = "reset:account:";
    private static final long RESET_TIMEOUT = 5;     // 分钟

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
                .role(registerDTO.getRole())
                .nickname(registerDTO.getUsername())
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        return buildLoginVO(user);
    }

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

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = requireUser(userId);
        return toUserInfoVO(user);
    }

    @Override
    public UserInfoVO updateProfile(Long userId, UpdateProfileDTO dto) {
        requireUser(userId);
        userMapper.updateProfile(userId, dto.getNickname(), dto.getPhone(), dto.getEmail(), dto.getAvatar());
        return toUserInfoVO(requireUser(userId));
    }

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

    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(TOKEN_PREFIX + userId);
    }

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

    private void validateAndDeleteCaptcha(String uuid, String code) {
        String captchaCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(code)) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_ERROR);
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + uuid);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USERNAME_NOT_FOUND);
        }
        return user;
    }

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

    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }

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

    public String createUuid() {
        return IdUtil.simpleUUID();
    }

    public LineCaptcha createLineCaptcha() {
        return CaptchaUtil.createLineCaptcha(130, 40, 4, 40);
    }
}
