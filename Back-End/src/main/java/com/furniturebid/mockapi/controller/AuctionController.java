package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.request.AutoBidRequest;
import com.furniturebid.mockapi.dto.request.PlaceBidRequest;
import com.furniturebid.mockapi.dto.response.BidDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.PlaceBidResponse;
import com.furniturebid.mockapi.dto.response.SellerActiveListingDto;
import com.furniturebid.mockapi.dto.response.SellerCompletedAuctionDto;
import com.furniturebid.mockapi.exception.ForbiddenException;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.AuctionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling auction bidding operations and seller listing endpoints.
 * Serves both /api/auctions and /api/seller paths.
 */
@RestController
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    /**
     * Places a bid on an auction.
     * POST /api/auctions/bids
     */
    @PostMapping("/api/auctions/bids")
    public ResponseEntity<PlaceBidResponse> placeBid(
            @Valid @RequestBody PlaceBidRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) httpRequest.getAttribute("authenticatedUser");
        PlaceBidResponse response = auctionService.placeBid(
                authUser.getUserId(),
                request.getAuctionId(),
                request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Gets bid history for an auction (public endpoint).
     * GET /api/auctions/{auctionId}/bids
     */
    @GetMapping("/api/auctions/{auctionId}/bids")
    public ResponseEntity<PaginatedResponse<BidDto>> getBidHistory(
            @PathVariable String auctionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PaginatedResponse<BidDto> response = auctionService.getBidHistory(auctionId, page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Activates auto-bid for the authenticated user on a specific auction.
     * POST /api/auctions/{auctionId}/auto-bid
     */
    @PostMapping("/api/auctions/{auctionId}/auto-bid")
    public ResponseEntity<Void> setAutoBid(
            @PathVariable String auctionId,
            @Valid @RequestBody AutoBidRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) httpRequest.getAttribute("authenticatedUser");
        auctionService.setAutoBid(authUser.getUserId(), auctionId, request.getMaxAmount());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivates auto-bid for the authenticated user on a specific auction.
     * DELETE /api/auctions/{auctionId}/auto-bid
     */
    @DeleteMapping("/api/auctions/{auctionId}/auto-bid")
    public ResponseEntity<Void> removeAutoBid(
            @PathVariable String auctionId,
            HttpServletRequest httpRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) httpRequest.getAttribute("authenticatedUser");
        auctionService.removeAutoBid(authUser.getUserId(), auctionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets active listings for the authenticated seller.
     * GET /api/seller/active-listings
     * Requires seller or admin role.
     */
    @GetMapping("/api/seller/active-listings")
    public ResponseEntity<PaginatedResponse<SellerActiveListingDto>> getActiveListings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) httpRequest.getAttribute("authenticatedUser");
        validateSellerOrAdmin(authUser);

        PaginatedResponse<SellerActiveListingDto> response = auctionService.getActiveListings(
                authUser.getUserId(), page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets completed auctions for the authenticated seller.
     * GET /api/seller/completed-auctions
     * Requires seller or admin role.
     */
    @GetMapping("/api/seller/completed-auctions")
    public ResponseEntity<PaginatedResponse<SellerCompletedAuctionDto>> getCompletedAuctions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) httpRequest.getAttribute("authenticatedUser");
        validateSellerOrAdmin(authUser);

        PaginatedResponse<SellerCompletedAuctionDto> response = auctionService.getCompletedAuctions(
                authUser.getUserId(), page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates that the authenticated user has seller or admin role.
     * Throws ForbiddenException if the user has buyer role.
     */
    private void validateSellerOrAdmin(AuthenticatedUser authUser) {
        if ("buyer".equals(authUser.getRole())) {
            throw new ForbiddenException("Only sellers and admins can access this resource");
        }
    }
}
