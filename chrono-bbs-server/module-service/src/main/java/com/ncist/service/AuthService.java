package com.ncist.service;
import com.ncist.dto.ChangePasswordDTO;
import com.ncist.dto.ForgotCodeDTO;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.dto.ResetPasswordDTO;
import com.ncist.dto.UpdateProfileDTO;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.ForgotCodeVO;
import com.ncist.vo.LoginVO;
import com.ncist.vo.UserInfoVO;
import jakarta.validation.Valid;

/**
 * 用户认证服务。
 *
 * <p>负责验证码、注册、登录、登出、资料修改与密码重置等认证流程。
 * 验证码、登录 Token 与重置密码验证码均存储在 Redis 中并设置过期时间；
 * 登录采用 JWT + Redis 双校验，同一用户仅保留最新 Token（单设备登录），
 * 改密、重置密码或登出后旧 Token 立即失效。</p>
 */
public interface AuthService {

    /**
     * 生成图形验证码，验证码内容写入 Redis（2 分钟有效）。
     *
     * @return 验证码结果，包含唯一标识 uuid 与 Base64 图片数据
     */
    CaptchaVO getCaptcha();

    /**
     * 注册新用户并直接返回登录态。
     *
     * <p>注册前校验并消费图形验证码（一次性使用），用户名与手机号必须唯一，
     * 密码以 BCrypt 加密后存储。支持注册为普通用户或商家：商家注册需填写店铺名称，
     * 并创建待审核的商家记录；管理员角色不允许注册。</p>
     *
     * @param registerDTO 注册参数，包含用户名、密码、手机号、邮箱、角色、店铺名称与验证码
     * @return 登录结果，包含 Token 与新用户信息
     * @throws BusinessException 验证码错误/过期、用户名或手机号已存在、
     *                           角色非法或商家未填写店铺名称时抛出
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 校验登录凭证并生成登录态。
     *
     * <p>登录前校验并消费图形验证码；登录成功后生成 JWT 并写入 Redis，
     * 覆盖该用户此前有效的 Token。</p>
     *
     * @param loginDTO 登录参数，包含用户名、密码与验证码
     * @return 登录结果，包含 Token 与用户信息
     * @throws BusinessException 验证码错误、账号不存在、密码错误或账号被禁用时抛出
     */
    LoginVO login(@Valid LoginDTO loginDTO);

    /**
     * 查询当前登录用户信息。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @return 用户信息
     * @throws BusinessException 用户不存在时抛出
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 修改当前用户资料。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    待修改资料，包含昵称、手机号、邮箱与头像
     * @return 修改后的用户信息
     * @throws BusinessException 用户不存在时抛出
     */
    UserInfoVO updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 修改登录密码。
     *
     * <p>需校验原密码；修改成功后立即使该用户在 Redis 中的 Token 失效，强制下线。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    修改密码参数，包含原密码与新密码
     * @throws BusinessException 用户不存在或原密码错误时抛出
     */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 退出登录，删除 Redis 中该用户的 Token 记录。
     *
     * @param userId 用户 ID，必须为当前登录用户
     */
    void logout(Long userId);

    /**
     * 发送重置密码验证码。
     *
     * <p>先校验并消费图形验证码，再校验账号存在；验证码与账号映射写入 Redis，
     * 5 分钟内有效。课程设计阶段为模拟发送，验证码会记录在日志中。</p>
     *
     * @param dto 忘记密码参数，包含账号与图形验证码
     * @return 重置验证码结果，包含 uuid、脱敏账号与验证码
     * @throws BusinessException 图形验证码错误或账号不存在时抛出
     */
    ForgotCodeVO sendForgotCode(ForgotCodeDTO dto);

    /**
     * 使用验证码重置密码。
     *
     * <p>校验 Redis 中的验证码与账号映射，验证通过后验证码一次性失效；
     * 重置成功后立即使该用户 Token 失效，强制下线。</p>
     *
     * @param dto 重置密码参数，包含 uuid、验证码与新密码
     * @throws BusinessException 验证码错误/过期或用户不存在时抛出
     */
    void resetPassword(ResetPasswordDTO dto);
}
