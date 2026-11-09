package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.Result;
import com.ncist.dto.ChangePasswordDTO;
import com.ncist.dto.ForgotCodeDTO;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.dto.ResetPasswordDTO;
import com.ncist.dto.UpdateProfileDTO;
import com.ncist.service.AuthService;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.ForgotCodeVO;
import com.ncist.vo.LoginVO;
import com.ncist.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器。
 *
 * <p>提供图形验证码、注册、登录、登出、个人信息维护与密码重置等认证流程。
 * 验证码、注册、登录、发送重置验证码与重置密码为公开接口，
 * 其余接口需要携带有效登录 token。接口前缀 {@code /api/user}。</p>
 */
@Tag(name = "用户认证", description = "注册、登录、登出、Token 刷新等认证相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class AuthController {
    private final AuthService authService;

    /**
     * 获取图形验证码。
     *
     * <p>公开接口，返回验证码唯一标识与图片，登录、注册与重置密码时需回传。</p>
     *
     * @return 验证码信息，包含用于后续校验的 {@code uuid} 与 Base64 图片
     */
    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = authService.getCaptcha();
        return Result.success(captchaVO);
    }

    /**
     * 注册普通用户账号。
     *
     * <p>公开接口，注册成功后直接返回登录态，无需再次登录。
     * 用户名需唯一，图形验证码必须与 {@code captchaUuid} 匹配且只能使用一次。</p>
     *
     * @param registerDTO 注册参数，包含用户名、密码、手机号/邮箱、角色与验证码
     * @return 登录结果，包含 token 与用户信息
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    /**
     * 账号密码登录。
     *
     * <p>公开接口，校验账号、密码与图形验证码后生成并缓存 token；
     * 账号被禁用或凭证错误时返回相应业务错误。</p>
     *
     * @param loginDTO 登录参数，包含用户名、密码与图形验证码
     * @return 登录结果，包含 token 与用户信息
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前用户的基本信息，包含角色、头像与联系方式
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo() {
        return Result.success(authService.getUserInfo(UserContext.getUserId()));
    }

    /**
     * 修改当前登录用户的个人资料。
     *
     * @param dto 资料参数，包含昵称、手机号、邮箱与头像；
     *            字段为 {@code null} 或空字符串时表示不修改
     * @return 更新后的用户信息
     */
    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.success(authService.updateProfile(UserContext.getUserId(), dto));
    }

    /**
     * 修改当前登录用户的密码。
     *
     * <p>需校验原密码；修改成功后删除服务端缓存的 token，用户需重新登录。</p>
     *
     * @param dto 密码参数，包含原密码与新密码
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(UserContext.getUserId(), dto);
        return Result.success();
    }

    /**
     * 退出登录。
     *
     * <p>删除服务端缓存的 token，使当前登录态立即失效。</p>
     *
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(UserContext.getUserId());
        return Result.success();
    }

    /**
     * 发送重置密码验证码。
     *
     * <p>公开接口，通过图形验证码校验后按账号生成一次性重置验证码；
     * 课程设计阶段以日志模拟短信/邮件发送，验证码同时随响应返回。</p>
     *
     * @param dto 参数包含账号与图形验证码
     * @return 重置验证码信息，包含后续重置所需的 {@code uuid} 与脱敏账号
     */
    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/forgot-code")
    public Result<ForgotCodeVO> sendForgotCode(@Valid @RequestBody ForgotCodeDTO dto) {
        return Result.success(authService.sendForgotCode(dto));
    }

    /**
     * 通过验证码重置密码。
     *
     * <p>公开接口，校验一次性验证码后更新密码，并使该用户已有的登录 token 失效。</p>
     *
     * @param dto 参数包含重置验证码 {@code uuid}、验证码与新密码
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success();
    }
}
