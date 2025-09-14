package com.sunboat.adminWeb.business.security;


import com.sunboat.adminWeb.business.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器：拦截请求，验证JWT令牌并设置认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中获取令牌（格式：Bearer <token>）
        String header = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (header != null && header.startsWith("Bearer ")) {
            jwtToken = header.substring(7); // 截取"Bearer "后面的令牌
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken); // 解析用户名
            } catch (Exception e) {
                // 令牌解析失败（如无效、过期），不设置认证信息
                logger.error("JWT令牌解析失败: " + e.getMessage());
            }
        }

        // 2. 验证令牌并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            // 验证令牌有效性
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                // 创建认证令牌并设置到上下文
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 3. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
    