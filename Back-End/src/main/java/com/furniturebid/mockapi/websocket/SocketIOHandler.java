package com.furniturebid.mockapi.websocket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.furniturebid.mockapi.security.JwtUtility;
import io.jsonwebtoken.Claims;
import io.netty.handler.codec.http.HttpHeaders;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket.IO event handler for real-time WebSocket communication.
 * Handles auction subscriptions, notification subscriptions, and broadcasts.
 */
@Component
public class SocketIOHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketIOHandler.class);

    private final SocketIOServer server;
    private final JwtUtility jwtUtility;

    /** Maps client session IDs to authenticated user IDs */
    private final Map<UUID, String> sessionUserMap = new ConcurrentHashMap<>();

    public SocketIOHandler(SocketIOServer server, JwtUtility jwtUtility) {
        this.server = server;
        this.jwtUtility = jwtUtility;
    }

    @PostConstruct
    public void registerListeners() {
        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);

        server.addEventListener("join:auction", String.class, (client, auctionId, ackRequest) -> {
            onJoinAuction(client, auctionId);
        });

        server.addEventListener("leave:auction", String.class, (client, auctionId, ackRequest) -> {
            onLeaveAuction(client, auctionId);
        });

        server.addEventListener("subscribe:notifications", String.class, (client, userId, ackRequest) -> {
            onSubscribeNotifications(client, userId);
        });
    }

    // ========== Connection Lifecycle ==========

    /**
     * Authenticates the client on connect by validating the JWT token
     * from the handshake auth.token parameter.
     */
    private void onConnect(SocketIOClient client) {
        String token = extractToken(client);

        if (token == null || token.isBlank()) {
            emitErrorAndDisconnect(client, "Authentication required: no token provided");
            return;
        }

        try {
            Claims claims = jwtUtility.parseToken(token);
            String userId = claims.getSubject();
            sessionUserMap.put(client.getSessionId(), userId);
            log.info("WebSocket client connected: sessionId={}, userId={}", client.getSessionId(), userId);
        } catch (Exception e) {
            emitErrorAndDisconnect(client, "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Cleans up session mapping on disconnect.
     * Room cleanup is handled automatically by netty-socketio.
     */
    private void onDisconnect(SocketIOClient client) {
        String userId = sessionUserMap.remove(client.getSessionId());
        log.info("WebSocket client disconnected: sessionId={}, userId={}", client.getSessionId(), userId);
    }

    // ========== Event Handlers ==========

    /**
     * Joins the client to an auction room for real-time bid updates.
     */
    private void onJoinAuction(SocketIOClient client, String auctionId) {
        if (!isAuthenticated(client)) {
            emitErrorAndDisconnect(client, "Not authenticated");
            return;
        }
        client.joinRoom(auctionId);
        log.debug("Client {} joined auction room: {}", client.getSessionId(), auctionId);
    }

    /**
     * Removes the client from an auction room.
     */
    private void onLeaveAuction(SocketIOClient client, String auctionId) {
        if (!isAuthenticated(client)) {
            return;
        }
        client.leaveRoom(auctionId);
        log.debug("Client {} left auction room: {}", client.getSessionId(), auctionId);
    }

    /**
     * Subscribes the client to their personal notification room.
     */
    private void onSubscribeNotifications(SocketIOClient client, String userId) {
        if (!isAuthenticated(client)) {
            emitErrorAndDisconnect(client, "Not authenticated");
            return;
        }
        String notificationRoom = getNotificationRoom(userId);
        client.joinRoom(notificationRoom);
        log.debug("Client {} subscribed to notifications for user: {}", client.getSessionId(), userId);
    }

    // ========== Broadcast Methods (called by services) ==========

    /**
     * Broadcasts a bid update to all clients in the auction room.
     *
     * @param auctionId     the auction room identifier
     * @param bidUpdateData the bid update payload
     */
    public void broadcastBidUpdate(String auctionId, Object bidUpdateData) {
        server.getRoomOperations(auctionId).sendEvent("bid:update", bidUpdateData);
        log.debug("Broadcast bid:update to auction room: {}", auctionId);
    }

    /**
     * Sends an outbid notification to a specific user's notification room.
     *
     * @param userId    the user to notify
     * @param outbidData the outbid notification payload
     */
    public void sendOutbidNotification(String userId, Object outbidData) {
        String room = getNotificationRoom(userId);
        server.getRoomOperations(room).sendEvent("outbid", outbidData);
        log.debug("Sent outbid notification to user: {}", userId);
    }

    /**
     * Sends a general notification to a specific user's notification room.
     *
     * @param userId           the user to notify
     * @param notificationData the notification payload
     */
    public void sendNotification(String userId, Object notificationData) {
        String room = getNotificationRoom(userId);
        server.getRoomOperations(room).sendEvent("notification", notificationData);
        log.debug("Sent notification to user: {}", userId);
    }

    // ========== Additional Event Emission ==========

    /**
     * Emits an auction:ending event to all clients in the auction room.
     *
     * @param auctionId        the auction room identifier
     * @param minutesRemaining minutes until auction ends
     */
    public void emitAuctionEnding(String auctionId, int minutesRemaining) {
        Map<String, Object> payload = Map.of(
                "auctionId", auctionId,
                "minutesRemaining", minutesRemaining
        );
        server.getRoomOperations(auctionId).sendEvent("auction:ending", payload);
        log.debug("Emitted auction:ending to room {}: {} minutes remaining", auctionId, minutesRemaining);
    }

    /**
     * Emits an auction:won event to the winning user's notification room.
     *
     * @param userId  the winning user
     * @param wonData the auction won payload
     */
    public void emitAuctionWon(String userId, Object wonData) {
        String room = getNotificationRoom(userId);
        server.getRoomOperations(room).sendEvent("auction:won", wonData);
        log.debug("Emitted auction:won to user: {}", userId);
    }

    /**
     * Emits an auction:lost event to the losing user's notification room.
     *
     * @param userId   the losing user
     * @param lostData the auction lost payload
     */
    public void emitAuctionLost(String userId, Object lostData) {
        String room = getNotificationRoom(userId);
        server.getRoomOperations(room).sendEvent("auction:lost", lostData);
        log.debug("Emitted auction:lost to user: {}", userId);
    }

    // ========== Helper Methods ==========

    /**
     * Extracts the JWT token from the client's handshake data.
     * Checks the URL parameter "token" first, then falls back to the Authorization header.
     */
    private String extractToken(SocketIOClient client) {
        // Try URL parameter first (standard Socket.IO auth approach)
        String token = client.getHandshakeData().getSingleUrlParam("token");
        if (token != null && !token.isBlank()) {
            return token;
        }

        // Try Authorization header as fallback
        HttpHeaders headers = client.getHandshakeData().getHttpHeaders();
        String authHeader = headers.get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private boolean isAuthenticated(SocketIOClient client) {
        return sessionUserMap.containsKey(client.getSessionId());
    }

    private void emitErrorAndDisconnect(SocketIOClient client, String message) {
        client.sendEvent("error", Map.of("message", message));
        log.warn("Rejecting WebSocket connection: {}", message);
        client.disconnect();
    }

    private String getNotificationRoom(String userId) {
        return "notifications:" + userId;
    }
}
