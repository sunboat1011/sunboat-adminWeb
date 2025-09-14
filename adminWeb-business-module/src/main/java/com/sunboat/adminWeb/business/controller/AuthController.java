package com.sunboat.adminWeb.business.controller;

import com.sunboat.adminWeb.business.utils.JwtTokenUtil;
import com.sunboat.common.core.result.RtnResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器：处理登录请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // 登录请求参数
    static class LoginRequest {
        private String username;
        private String password;

        // Getter和Setter
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 登录响应
    static class LoginResponse {
        private String token;
        private String type = "Bearer";
        private String username;

        public LoginResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }

        // Getter
        public String getToken() { return token; }
        public String getType() { return type; }
        public String getUsername() { return username; }
    }

    /**
     * 登录接口：验证用户名密码，生成JWT令牌
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        // 1. 验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // 2. 生成JWT令牌
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        // 3. 返回令牌
        return new LoginResponse(jwtToken, userDetails.getUsername());
    }

//    @PostMapping("/register")
//    public RtnResult<Void> login(@RequestBody LoginRequest loginRequest) {
////        // 1. 验证用户名密码
////        Authentication authentication = authenticationManager.authenticate(
////                new UsernamePasswordAuthenticationToken(
////                        loginRequest.getUsername(),
////                        loginRequest.getPassword()
////                )
////        );
////
////        // 2. 生成JWT令牌
////        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
////        String jwtToken = jwtTokenUtil.generateToken(userDetails);
////
////        // 3. 返回令牌
////        return new LoginResponse(jwtToken, userDetails.getUsername());
//        return null;
//    }
}
    