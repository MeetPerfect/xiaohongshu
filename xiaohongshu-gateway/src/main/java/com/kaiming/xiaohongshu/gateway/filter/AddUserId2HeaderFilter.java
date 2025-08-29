package com.kaiming.xiaohongshu.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.constant.GlobalConstants;
import com.kaiming.xiaohongshu.gateway.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * ClassName: AddUserId2HeaderFilter
 * Package: com.kaiming.xiaohongshu.gateway.filter
 * Description: 转发请求时，将用户 ID 添加到 Header 请求头中，透传给下游服务
 *
 * @Auther gongkaiming
 * @Create 2025/5/17 13:28
 * @Version 1.0
 */
@Component
@Slf4j
//@Order(-90)
public class AddUserId2HeaderFilter implements GlobalFilter {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_HEADER_KEY = "Authorization";
    
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";
    
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");
        // 从请求头中获取Token 数据
        List<String> tokenList = exchange.getRequest().getHeaders().get(TOKEN_HEADER_KEY);
        
        if (CollUtil.isEmpty(tokenList)) {
            // // 若请求头中未携带 Token，则直接放行
            return chain.filter(exchange);
        }
        
        // 获取 Token 值
        String tokenValue = tokenList.get(0);
        // 去除 Bearer 前缀
        String token = tokenValue.replace(TOKEN_HEADER_VALUE_PREFIX, "");
        
        // 构建 Redis Key
        String tokenRedisKey = RedisKeyConstants.SA_TOKEN_TOKEN_KEY_PREFIX + token;
        
        // 查询 Redis 获取用户Id
        Integer userId = (Integer) redisTemplate.opsForValue().get(tokenRedisKey);
        
        if (Objects.isNull(userId)) {
            return chain.filter(exchange);
        }
        log.info("## 当前登录的用户 ID: {}", userId);

        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(GlobalConstants.USER_ID, String.valueOf(userId)))
                .build();

        return chain.filter(newExchange);
    }
}
