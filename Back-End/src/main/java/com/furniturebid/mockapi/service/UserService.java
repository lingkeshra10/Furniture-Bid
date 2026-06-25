package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.UserDto;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.exception.ValidationException;
import com.furniturebid.mockapi.store.MockDataStore;
import com.furniturebid.mockapi.util.PaginationHelper;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service handling user profile operations and watchlist management.
 */
@Service
public class UserService {

    private final MockDataStore dataStore;

    public UserService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Retrieves a user profile by ID.
     *
     * @param userId the user ID
     * @return the user DTO
     * @throws NotFoundException if the user does not exist
     */
    public UserDto getProfile(String userId) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        return toUserDto(user);
    }

    /**
     * Updates a user's profile fields. Only non-null fields are updated.
     *
     * @param userId      the user ID
     * @param displayName the new display name (nullable, 3-50 chars if provided)
     * @param avatarUrl   the new avatar URL (nullable, max 2048 chars if provided)
     * @return the updated user DTO
     * @throws NotFoundException   if the user does not exist
     * @throws ValidationException if any field fails validation
     */
    public UserDto updateProfile(String userId, String displayName, String avatarUrl) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Map<String, String> fieldErrors = new HashMap<>();

        if (displayName != null) {
            if (displayName.length() < 3 || displayName.length() > 50) {
                fieldErrors.put("displayName", "Display name must be between 3 and 50 characters");
            }
        }

        if (avatarUrl != null) {
            if (avatarUrl.length() > 2048) {
                fieldErrors.put("avatarUrl", "Avatar URL must not exceed 2048 characters");
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Validation failed", fieldErrors);
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        return toUserDto(user);
    }

    /**
     * Retrieves a paginated list of watchlist items for a user.
     *
     * @param userId   the user ID
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return paginated response of furniture listing summaries
     */
    public PaginatedResponse<FurnitureListingSummaryDto> getWatchlist(String userId, Integer page, Integer pageSize) {
        List<String> watchlistIds = dataStore.getWatchlistForUser(userId);

        List<FurnitureListingSummaryDto> summaries = watchlistIds.stream()
                .map(listingId -> dataStore.getListingById(listingId).orElse(null))
                .filter(listing -> listing != null)
                .map(this::toListingSummaryDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(summaries, page, pageSize);
    }

    /**
     * Adds a listing to the user's watchlist.
     *
     * @param userId    the user ID
     * @param listingId the listing ID to add
     * @throws NotFoundException if the listing does not exist
     */
    public void addToWatchlist(String userId, String listingId) {
        dataStore.getListingById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing", listingId));

        dataStore.getWatchlistByUser().compute(userId, (key, existingList) -> {
            if (existingList == null) {
                List<String> newList = new ArrayList<>();
                newList.add(listingId);
                return newList;
            }
            if (!existingList.contains(listingId)) {
                existingList.add(listingId);
            }
            return existingList;
        });
    }

    /**
     * Removes a listing from the user's watchlist.
     *
     * @param userId    the user ID
     * @param listingId the listing ID to remove
     * @throws NotFoundException if the listing is not in the user's watchlist
     */
    public void removeFromWatchlist(String userId, String listingId) {
        List<String> watchlist = dataStore.getWatchlistForUser(userId);

        if (!watchlist.contains(listingId)) {
            throw new NotFoundException("Watchlist item", listingId);
        }

        dataStore.getWatchlistByUser().computeIfPresent(userId, (key, existingList) -> {
            existingList.remove(listingId);
            return existingList;
        });
    }

    // ========== Private Helper Methods ==========

    private UserDto toUserDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getRole(),
                entity.getAvatarUrl(),
                entity.getCreatedAt()
        );
    }

    private FurnitureListingSummaryDto toListingSummaryDto(FurnitureListingEntity listing) {
        String thumbnailUrl = (listing.getImages() != null && !listing.getImages().isEmpty())
                ? listing.getImages().get(0)
                : null;

        long timeRemaining = 0;
        if (listing.getAuctionEndDate() != null) {
            timeRemaining = listing.getAuctionEndDate().toEpochMilli() - Instant.now().toEpochMilli();
            if (timeRemaining < 0) {
                timeRemaining = 0;
            }
        }

        return new FurnitureListingSummaryDto(
                listing.getId(),
                listing.getTitle(),
                thumbnailUrl,
                listing.getCondition(),
                listing.getCategory(),
                listing.getCurrentBid(),
                timeRemaining
        );
    }
}
