package com.hmall.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import com.hmall.common.exception.UnauthorizedException;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// 用户登录拦截器
@Component
@RequiredArgsConstructor
public class AuthLoginFlobalFilter implements GlobalFilter, Ordered {

    private final JwtTool jwtTool;
    private final AuthProperties authProperties;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 判断当前请求是否需要被拦截（也就是是否需要登录信息）
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange); // 放行，由过滤器链中的下一个过滤器进行判断
        }

        // 如果当前请求需要用户信息，再去验证用户的登录信息,首先从请求头中取得token信息，返回的是一个列表，取第一个就行了
        List<String> headers = request.getHeaders().get("authorization");
        String token = null;
        if(!CollUtil.isEmpty(headers)){
            token = headers.get(0);
        }

        Long userId = null;
        // 解析并校验token
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 如果无效，拦截,返回401状态码
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }

        // TODO 如果有效，传递用户信息给微服务(具体做法就是把userID信息写入到ServerWebExchange中)
        String userInfo = userId.toString();
        ServerWebExchange newSwe = exchange.mutate().request(
                builder -> builder.header("user-info", userInfo)
        ).build();

        return chain.filter(newSwe);
    }


    private boolean isExclude(String antPath) {
        AntPathMatcher antPathMatcher = new AntPathMatcher(); // spring中用来匹配类似“item/**"这种带**的路径的一个工具包
        System.out.println(authProperties.getExcludePaths().isEmpty()); // 从authProperties取得那些可以直接放行的路径
        for (String pathPattern : authProperties.getExcludePaths()) {
//            System.out.println("pathPattern: " + pathPattern);
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() { // 定义当前过滤器的执行顺序，越小表示越先执行
        return 0;
    }
}
