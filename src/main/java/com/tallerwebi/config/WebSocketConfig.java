package com.tallerwebi.config;

import com.tallerwebi.presentacion.ChatHandshakeInterceptor;
import com.tallerwebi.presentacion.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final ChatWebSocketHandler chatHandler;
  private final ChatHandshakeInterceptor chatInterceptor;

  @Autowired
  public WebSocketConfig(
    ChatWebSocketHandler chatHandler,
    ChatHandshakeInterceptor chatInterceptor
  ) {
    this.chatHandler = chatHandler;
    this.chatInterceptor = chatInterceptor;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(chatHandler, "/ws/chat").addInterceptors(chatInterceptor);
  }
}
