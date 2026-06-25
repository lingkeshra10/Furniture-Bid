package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.NotificationDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.NotificationEntity;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.store.MockDataStore;
import com.furniturebid.mockapi.util.PaginationHelper;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling notification retrieval and read-status management.
 */
@Service
public class NotificationService {

    private final MockDataStore dataStore;

    public NotificationService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Retrieves paginated notifications for a user, sorted by createdAt descending.
     *
     * @param userId   the user ID
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return paginated response of notification DTOs
     */
    public PaginatedResponse<NotificationDto> getNotifications(String userId, Integer page, Integer pageSize) {
        List<NotificationEntity> notifications = dataStore.getNotificationsForUser(userId);

        List<NotificationDto> sorted = notifications.stream()
                .sorted(Comparator.comparing(NotificationEntity::getCreatedAt).reversed())
                .map(this::toNotificationDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(sorted, page, pageSize);
    }

    /**
     * Marks a single notification as read.
     *
     * @param userId         the user ID
     * @param notificationId the notification ID to mark as read
     * @throws NotFoundException if the notification is not found for the user
     */
    public void markAsRead(String userId, String notificationId) {
        List<NotificationEntity> notifications = dataStore.getNotificationsForUser(userId);

        NotificationEntity notification = notifications.stream()
                .filter(n -> n.getId().toString().equals(notificationId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Notification", notificationId));

        notification.setRead(true);
    }

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId the user ID
     */
    public void markAllAsRead(String userId) {
        List<NotificationEntity> notifications = dataStore.getNotificationsForUser(userId);

        notifications.forEach(n -> n.setRead(true));
    }

    // ========== Private Helper Methods ==========

    private NotificationDto toNotificationDto(NotificationEntity entity) {
        return new NotificationDto(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getAuctionId(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}
