package com.ncist.service.impl;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.ncist.common.enums.AuthErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.utils.JwtUtil;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.entity.User;
import com.ncist.mapper.UserMapper;
import com.ncist.service.AuthService;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.LoginVO;
import com.ncist.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Autowired
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TIMEOUT = 2;

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
        String captchaCode = stringRedisTemplate.opsForValue()
                .get(CAPTCHA_PREFIX + registerDTO.getCaptchaUuid());
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(registerDTO.getCaptchaCode())) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_ERROR);
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + registerDTO.getCaptchaUuid());

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
                .status(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        String token = JwtUtil.generateToken(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));

        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);

        return LoginVO.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String captchaCode = stringRedisTemplate.opsForValue()
                .get(CAPTCHA_PREFIX + loginDTO.getCaptchaUuid());
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_ERROR);
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + loginDTO.getCaptchaUuid());

        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USERNAME_NOT_FOUND);
        }
        if (user.getStatus() != 0) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.PASSWORD_ERROR);
        }

        String token = JwtUtil.generateToken(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));

        UserInfoVO userInfo = new UserInfoVO();
        // 复制用户信息到 UserInfoVO
        BeanUtils.copyProperties(user, userInfo);
        // 返回查询用户信息响应
        return LoginVO.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }
    public String createUuid() {
        return IdUtil.simpleUUID();
    }

    public LineCaptcha createLineCaptcha() {
        return CaptchaUtil.createLineCaptcha(130, 40, 4, 40);
    }
}