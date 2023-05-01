package com.ruoyi.web.core.config;


import com.ruoyi.bang.service.OnlineMsService;
import com.ruoyi.web.controller.bang.MessageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启WebSocket支持
 * @author qq
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入一个ServerEndpointExporter,该Bean会自动注册使用@ServerEndpoint注解申明的websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


    @Autowired
    private void setRedisTemplate(OnlineMsService onlineMsService){
        MessageController.onlineMsService=onlineMsService;
    }
}

