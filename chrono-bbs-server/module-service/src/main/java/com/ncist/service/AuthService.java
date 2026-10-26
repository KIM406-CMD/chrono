package com.ncist.service;
import com.ncist.dto.LoginDTO;
import com.ncist.dto.RegisterDTO;
import com.ncist.vo.CaptchaVO;
import com.ncist.vo.LoginVO;
import jakarta.validation.Valid;

public interface AuthService {

    CaptchaVO getCaptcha();

    LoginVO register(RegisterDTO registerDTO);

    LoginVO login(@Valid LoginDTO loginDTO);
}