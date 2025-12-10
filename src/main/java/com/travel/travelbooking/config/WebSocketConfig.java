package com.travel.travelbooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-support")          // endpoint để client connect
                .setAllowedOriginPatterns("*")       // cho phép mọi origin, dev cho dễ
                .withSockJS();                       // dùng SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // prefix cho các destination client SUBSCRIBE
        registry.enableSimpleBroker("/topic", "/queue");

        // prefix cho các destination client SEND vào (nếu sau này bạn dùng @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
