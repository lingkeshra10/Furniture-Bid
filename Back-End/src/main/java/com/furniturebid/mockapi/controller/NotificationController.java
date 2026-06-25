package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.response.NotificationDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling notification retrieval and read-status updates.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications - Return paginated notifications for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<NotificationDto>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        PaginatedResponse<NotificationDto> response =
                notificationService.getNotifications(authUser.getUserId(), page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/notifications/{id}/read - Mark a single notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(HttpServletRequest request,
                                           @PathVariable String id) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        notificationService.markAsRead(authUser.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/notifications/read-all - Mark all notifications as read for the authenticated user.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        notificationService.markAllAsRead(authUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
