package com.pet.interceptor;

import com.pet.constant.JwtClaimsConstant;
import com.pet.context.BaseContext;
import com.pet.properties.JwtProperties;
import com.pet.service.UserTokenService;
import com.pet.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户端 jwt 令牌校验拦截器。
 *
 * <p>除校验签名外还要求令牌存在于 Redis，剩余有效期不足时自动续期，
 * 新令牌通过响应头返回给客户端。</p>
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserTokenService userTokenService;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            if (!userTokenService.isActive(userId, token)) {
                response.setStatus(401);
                return false;
            }
            BaseContext.setCurrentId(userId);

            //3、剩余有效期不足时续期，并通过响应头下发新令牌
            String refreshed = userTokenService.refreshIfNeeded(userId, token);
            if (refreshed != null) {
                response.setHeader(jwtProperties.getUserTokenName(), refreshed);
            }
            //4、通过，放行
            return true;
        } catch (Exception ex) {
            //5、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}