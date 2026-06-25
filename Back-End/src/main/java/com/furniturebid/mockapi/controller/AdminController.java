package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.response.*;
import com.furniturebid.mockapi.exception.ForbiddenException;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling admin endpoints for user management, listing management,
 * reports, and analytics. All endpoints require admin role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ========== User Management ==========

    /**
     * GET /api/admin/users - Paginated list of all users (admin only).
     */
    @GetMapping("/users")
    public ResponseEntity<PaginatedResponse<AdminUserRowDto>> getUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requireAdmin(request);
        PaginatedResponse<AdminUserRowDto> response = adminService.getUsers(page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/users/{userId}/suspend - Suspend a user (admin only).
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(HttpServletRequest request,
                                            @PathVariable String userId) {
        requireAdmin(request);
        adminService.suspendUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/admin/users/{userId}/activate - Activate a user (admin only).
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(HttpServletRequest request,
                                             @PathVariable String userId) {
        requireAdmin(request);
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/admin/users/{userId} - Delete a user (admin only).
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(HttpServletRequest request,
                                           @PathVariable String userId) {
        requireAdmin(request);
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ========== Listing Management ==========

    /**
     * GET /api/admin/listings - Paginated list of all listings (admin only).
     */
    @GetMapping("/listings")
    public ResponseEntity<PaginatedResponse<AdminListingRowDto>> getListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requireAdmin(request);
        PaginatedResponse<AdminListingRowDto> response = adminService.getListings(page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/listings/{listingId} - Remove a listing (admin only).
     */
    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<Void> removeListing(HttpServletRequest request,
                                              @PathVariable String listingId) {
        requireAdmin(request);
        adminService.removeListing(listingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/admin/listings/{listingId}/flag - Flag a listing (admin only).
     */
    @PutMapping("/listings/{listingId}/flag")
    public ResponseEntity<Void> flagListing(HttpServletRequest request,
                                            @PathVariable String listingId) {
        requireAdmin(request);
        adminService.flagListing(listingId);
        return ResponseEntity.noContent().build();
    }

    // ========== Reports ==========

    /**
     * GET /api/admin/listings/reports - Paginated list of all listing reports (admin only).
     */
    @GetMapping("/listings/reports")
    public ResponseEntity<PaginatedResponse<ListingReportDto>> getReports(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requireAdmin(request);
        PaginatedResponse<ListingReportDto> response = adminService.getReports(page, pageSize);
        return ResponseEntity.ok(response);
    }

    // ========== Analytics ==========

    /**
     * GET /api/admin/analytics/summary - Analytics summary (admin only).
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsSummaryDto> getAnalyticsSummary(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        requireAdmin(request);
        AnalyticsSummaryDto summary = adminService.getAnalyticsSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/admin/analytics/auction-trends - Auction trends over time (admin only).
     */
    @GetMapping("/analytics/auction-trends")
    public ResponseEntity<List<AuctionTrendDto>> getAuctionTrends(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        requireAdmin(request);
        List<AuctionTrendDto> trends = adminService.getAuctionTrends(startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    /**
     * GET /api/admin/analytics/category-distribution - Category distribution (admin only).
     */
    @GetMapping("/analytics/category-distribution")
    public ResponseEntity<List<CategoryDistributionDto>> getCategoryDistribution(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        requireAdmin(request);
        List<CategoryDistributionDto> distribution = adminService.getCategoryDistribution(startDate, endDate);
        return ResponseEntity.ok(distribution);
    }

    /**
     * GET /api/admin/analytics/top-sellers - Top sellers by revenue (admin only).
     */
    @GetMapping("/analytics/top-sellers")
    public ResponseEntity<List<TopSellerDto>> getTopSellers(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        requireAdmin(request);
        List<TopSellerDto> topSellers = adminService.getTopSellers(startDate, endDate);
        return ResponseEntity.ok(topSellers);
    }

    // ========== Helper ==========

    /**
     * Checks that the authenticated user has the "admin" role.
     * Throws ForbiddenException if the user is not an admin.
     */
    private void requireAdmin(HttpServletRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        if (authUser == null || !"admin".equals(authUser.getRole())) {
            throw new ForbiddenException();
        }
    }
}
