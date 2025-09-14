package com.sunboat.adminWeb.business.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户服务：从数据源加载用户信息（示例用内存用户，实际项目替换为数据库查询）
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 实际项目中应从数据库查询用户信息
        if ("user".equals(username)) {
            // 用户"user"拥有USER角色
            UserDetails user = User.builder()
                    .username("user")
                    .password("{noop}password") // {noop}表示不加密（测试用，生产环境需用BCrypt）
                    .authorities(Collections.singletonList(() -> "ROLE_USER"))
                    .build();
            return user;
        } else if ("admin".equals(username)) {
            // 用户"admin"拥有ADMIN角色
            UserDetails admin = User.builder()
                    .username("admin")
                    .password("{noop}admin")
                    .authorities(Collections.singletonList(() -> "ROLE_ADMIN"))
                    .build();
            return admin;
        } else {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
    }
}
    