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

public interface AuthService {

    CaptchaVO getCaptcha();

    LoginVO register(RegisterDTO registerDTO);

    LoginVO login(@Valid LoginDTO loginDTO);

    UserInfoVO getUserInfo(Long userId);

    UserInfoVO updateProfile(Long userId, UpdateProfileDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);

    void logout(Long userId);

    ForgotCodeVO sendForgotCode(ForgotCodeDTO dto);

    void resetPassword(ResetPasswordDTO dto);
}
