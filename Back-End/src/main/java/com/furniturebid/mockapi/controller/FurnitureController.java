package com.furniturebid.mockapi.controller;

import tools.jackson.databind.json.JsonMapper;
import com.furniturebid.mockapi.dto.request.FlagRequest;
import com.furniturebid.mockapi.dto.response.FurnitureListingDto;
import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.Dimensions;
import com.furniturebid.mockapi.exception.ForbiddenException;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.FurnitureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller handling furniture listing endpoints.
 * Public endpoints: GET catalog and GET listing detail.
 * Protected endpoints: POST create, PUT flag, DELETE remove.
 */
@RestController
@RequestMapping("/api/furniture")
public class FurnitureController {

    private final FurnitureService furnitureService;
    private final JsonMapper jsonMapper;

    public FurnitureController(FurnitureService furnitureService, JsonMapper jsonMapper) {
        this.furnitureService = furnitureService;
        this.jsonMapper = jsonMapper;
    }

    /**
     * GET /api/furniture - Browse catalog with filtering, sorting, and pagination.
     * Public endpoint (no auth required).
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<FurnitureListingSummaryDto>> getCatalog(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {

        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                category, condition, priceMin, priceMax, location, sort, page, pageSize);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/furniture/{id} - Get listing detail.
     * Public endpoint (no auth required).
     */
    @GetMapping("/{id}")
    public ResponseEntity<FurnitureListingDto> getListingById(@PathVariable String id) {
        FurnitureListingDto listing = furnitureService.getListingById(id);
        return ResponseEntity.ok(listing);
    }

    /**
     * POST /api/furniture - Create a new listing (multipart/form-data).
     * Only sellers and admins can create listings.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FurnitureListingDto> createListing(
            HttpServletRequest request,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("condition") String condition,
            @RequestParam(value = "dimensions", required = false) String dimensionsJson,
            @RequestParam("startingPrice") BigDecimal startingPrice,
            @RequestParam(value = "reservePrice", required = false) BigDecimal reservePrice,
            @RequestParam("auctionEndDate") String auctionEndDate,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");

        // Only sellers and admins can create listings
        if ("buyer".equalsIgnoreCase(authUser.getRole())) {
            throw new ForbiddenException("Only sellers and admins can create listings");
        }

        // Parse dimensions JSON string if provided
        Dimensions dimensions = null;
        if (dimensionsJson != null && !dimensionsJson.isBlank()) {
            try {
                dimensions = jsonMapper.readValue(dimensionsJson, Dimensions.class);
            } catch (Exception e) {
                // If parsing fails, leave dimensions as null
            }
        }

        // Parse auction end date
        Instant endDate = Instant.parse(auctionEndDate);

        // Generate mock image URLs from file names
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                if (originalFilename != null) {
                    imageUrls.add("/uploads/furniture/" + originalFilename);
                }
            }
        }

        FurnitureListingDto created = furnitureService.createListing(
                title, description, category, condition, dimensions,
                startingPrice, reservePrice, endDate, imageUrls, authUser.getUserId());

        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/furniture/{id}/flag - Flag a listing for review.
     * Requires authentication.
     */
    @PutMapping("/{id}/flag")
    public ResponseEntity<Void> flagListing(
            HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody FlagRequest flagRequest) {

        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");

        furnitureService.flagListing(id, flagRequest.getReason(), authUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/furniture/{id} - Remove a listing.
     * Only the listing owner or an admin can delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            HttpServletRequest request,
            @PathVariable String id) {

        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");

        furnitureService.deleteListing(id, authUser.getUserId(), authUser.getRole());

        return ResponseEntity.noContent().build();
    }
}
