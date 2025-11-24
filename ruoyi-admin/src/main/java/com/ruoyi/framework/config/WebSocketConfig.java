package com.ruoyi.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 客户端订阅消息的前缀
        config.enableSimpleBroker("/topic");
        // 服务端接收消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个/ws的端点，客户端通过这个端点进行连接
        registry.addEndpoint("/ws")
                // 解决跨域问题
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
