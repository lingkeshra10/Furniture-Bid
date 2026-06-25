package com.furniturebid.mockapi.config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the netty-socketio WebSocket server.
 * Runs on a separate port from the REST API to avoid conflicts.
 */
@Configuration
public class SocketIOConfig {

    @Value("${socketio.port:9092}")
    private int socketIOPort;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(socketIOPort);
        config.setOrigin("*");
        config.setAllowCustomRequests(true);

        server = new SocketIOServer(config);
        server.start();
        return server;
    }

    @PreDestroy
    public void stopSocketIOServer() {
        if (server != null) {
            server.stop();
        }
    }
}
