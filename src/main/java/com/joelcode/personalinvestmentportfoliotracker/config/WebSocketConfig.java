package com.joelcode.personalinvestmentportfoliotracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// WebSocket configuration for real-time portfolio updates
// Enables bi-directional communication between server and clients

// This class takes in websocket connect requests and creates STOMP rules to send out to connected users or those connected to a
// STOMP endpoint. Its basically a conversion from connect requests to json via websocket and sent back to frontend.
// This is ONLY for real time updates

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Creates the client container which contains the message and sets prefixes to tell the container where to go in the server
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for pub/sub messaging
        config.enableSimpleBroker("/topic", "/queue");

        // Set prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");

        // Set prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }


    // Creates the STOMP endpoints that the message broker will route to
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Optional: Add endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}