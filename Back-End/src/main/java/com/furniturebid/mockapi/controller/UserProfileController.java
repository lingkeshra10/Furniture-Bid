package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.UserDto;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller handling user profile and watchlist endpoints.
 */
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/profile - Return the authenticated user's profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(HttpServletRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        UserDto user = userService.getProfile(authUser.getUserId());
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/profile - Update displayName and/or avatarUrl.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(HttpServletRequest request,
                                                 @RequestBody Map<String, String> body) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        String displayName = body.get("displayName");
        String avatarUrl = body.get("avatarUrl");
        UserDto updatedUser = userService.updateProfile(authUser.getUserId(), displayName, avatarUrl);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * GET /api/users/watchlist - Return paginated watchlist for the authenticated user.
     */
    @GetMapping("/watchlist")
    public ResponseEntity<PaginatedResponse<FurnitureListingSummaryDto>> getWatchlist(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        PaginatedResponse<FurnitureListingSummaryDto> response =
                userService.getWatchlist(authUser.getUserId(), page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/users/watchlist/{listingId} - Add a listing to the user's watchlist.
     */
    @PostMapping("/watchlist/{listingId}")
    public ResponseEntity<Void> addToWatchlist(HttpServletRequest request,
                                               @PathVariable String listingId) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        userService.addToWatchlist(authUser.getUserId(), listingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/users/watchlist/{listingId} - Remove a listing from the user's watchlist.
     */
    @DeleteMapping("/watchlist/{listingId}")
    public ResponseEntity<Void> removeFromWatchlist(HttpServletRequest request,
                                                    @PathVariable String listingId) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        userService.removeFromWatchlist(authUser.getUserId(), listingId);
        return ResponseEntity.noContent().build();
    }
}
