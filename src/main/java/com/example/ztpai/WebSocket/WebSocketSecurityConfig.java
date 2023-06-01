package com.example.ztpai.WebSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/ws/**").permitAll()
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf()
                .and()
                .headers()
                .frameOptions().sameOrigin()
                .and()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .build();
    }
}
