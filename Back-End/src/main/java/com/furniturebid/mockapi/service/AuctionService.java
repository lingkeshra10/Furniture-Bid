package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.BidDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.PlaceBidResponse;
import com.furniturebid.mockapi.dto.response.SellerActiveListingDto;
import com.furniturebid.mockapi.dto.response.SellerCompletedAuctionDto;
import com.furniturebid.mockapi.entity.AutoBidConfig;
import com.furniturebid.mockapi.entity.BidEntity;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.exception.AuctionEndedException;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.store.MockDataStore;
import com.furniturebid.mockapi.websocket.SocketIOHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling auction-related operations: bidding, bid history,
 * auto-bid configuration, and seller-scoped listing queries.
 */
@Service
public class AuctionService {

    private static final BigDecimal MIN_INCREMENT = new BigDecimal("5.00");

    private final MockDataStore dataStore;

    @Autowired(required = false)
    private SocketIOHandler socketIOHandler;

    public AuctionService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Places a bid on an auction.
     * Validates the auction exists, is active, and the bid amount meets the minimum increment.
     *
     * @param userId    the ID of the user placing the bid
     * @param auctionId the ID of the auction to bid on
     * @param amount    the bid amount
     * @return PlaceBidResponse indicating success or failure with error message
     */
    public PlaceBidResponse placeBid(String userId, String auctionId, BigDecimal amount) {
        FurnitureListingEntity listing = dataStore.getListingById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction", auctionId));

        if (!"active".equals(listing.getStatus())) {
            throw new AuctionEndedException();
        }

        BigDecimal currentBid = listing.getCurrentBid() != null
                ? listing.getCurrentBid()
                : listing.getStartingPrice();
        BigDecimal minimumBid = currentBid.add(MIN_INCREMENT);

        if (amount.compareTo(minimumBid) < 0) {
            String error = String.format("Minimum bid amount is $%s", minimumBid.toPlainString());
            return new PlaceBidResponse(false, null, error);
        }

        // Detect previous highest bidder before placing the new bid
        String previousHighestBidderId = findCurrentHighestBidderId(auctionId);

        // Build bidder alias from user's display name
        String bidderAlias = buildBidderAlias(userId);

        // Create the bid entity
        BidEntity bid = new BidEntity(
                UUID.randomUUID(),
                UUID.fromString(auctionId),
                UUID.fromString(userId),
                bidderAlias,
                amount,
                Instant.now()
        );

        // Add bid to the auction's bid list
        dataStore.getBidsByAuction()
                .computeIfAbsent(auctionId, k -> new ArrayList<>())
                .add(bid);

        // Update listing's currentBid and bidCount
        listing.setCurrentBid(amount);
        listing.setBidCount(listing.getBidCount() + 1);

        // Map to DTO and return success
        BidDto bidDto = mapToBidDto(bid);

        // Broadcast WebSocket events after successful bid
        if (socketIOHandler != null) {
            // Broadcast bid update to all subscribers of this auction
            Map<String, Object> bidUpdatePayload = new HashMap<>();
            bidUpdatePayload.put("auctionId", auctionId);
            bidUpdatePayload.put("currentBid", amount);
            bidUpdatePayload.put("bidCount", listing.getBidCount());
            bidUpdatePayload.put("bidderAlias", bidderAlias);
            bidUpdatePayload.put("timestamp", bid.getTimestamp().atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            socketIOHandler.broadcastBidUpdate(auctionId, bidUpdatePayload);

            // Send outbid notification if there was a previous highest bidder different from current
            if (previousHighestBidderId != null && !previousHighestBidderId.equals(userId)) {
                Map<String, Object> outbidPayload = new HashMap<>();
                outbidPayload.put("auctionId", auctionId);
                outbidPayload.put("currentBid", amount);
                socketIOHandler.sendOutbidNotification(previousHighestBidderId, outbidPayload);
            }
        }

        return new PlaceBidResponse(true, bidDto, null);
    }

    /**
     * Gets paginated bid history for an auction, sorted by timestamp descending.
     *
     * @param auctionId the auction ID
     * @param page      page number (1-based)
     * @param pageSize  number of items per page
     * @return paginated list of BidDto
     */
    public PaginatedResponse<BidDto> getBidHistory(String auctionId, Integer page, Integer pageSize) {
        List<BidEntity> bids = dataStore.getBidsForAuction(auctionId);

        // Sort by timestamp descending
        List<BidEntity> sorted = bids.stream()
                .sorted(Comparator.comparing(BidEntity::getTimestamp).reversed())
                .toList();

        // Map to DTOs
        List<BidDto> bidDtos = sorted.stream()
                .map(this::mapToBidDto)
                .collect(Collectors.toList());

        return dataStore.paginate(bidDtos, page, pageSize);
    }

    /**
     * Sets an auto-bid configuration for a user on an auction.
     *
     * @param userId    the user ID
     * @param auctionId the auction ID
     * @param maxAmount the maximum amount for auto-bidding
     */
    public void setAutoBid(String userId, String auctionId, BigDecimal maxAmount) {
        dataStore.getListingById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction", auctionId));

        String key = auctionId + ":" + userId;
        AutoBidConfig config = new AutoBidConfig(UUID.fromString(userId), UUID.fromString(auctionId), maxAmount);
        dataStore.getAutoBids().put(key, config);
    }

    /**
     * Removes an auto-bid configuration for a user on an auction.
     *
     * @param userId    the user ID
     * @param auctionId the auction ID
     */
    public void removeAutoBid(String userId, String auctionId) {
        String key = auctionId + ":" + userId;
        dataStore.getAutoBids().remove(key);
    }

    /**
     * Gets paginated active listings for a seller.
     *
     * @param sellerId the seller's user ID
     * @param page     page number (1-based)
     * @param pageSize number of items per page
     * @return paginated list of SellerActiveListingDto
     */
    public PaginatedResponse<SellerActiveListingDto> getActiveListings(String sellerId, Integer page, Integer pageSize) {
        Instant now = Instant.now();

        List<SellerActiveListingDto> activeListings = dataStore.getListings().values().stream()
                .filter(listing -> sellerId.equals(listing.getSellerId().toString()))
                .filter(listing -> "active".equals(listing.getStatus()))
                .map(listing -> {
                    long timeRemaining = Duration.between(now, listing.getAuctionEndDate()).toMillis();
                    if (timeRemaining < 0) {
                        timeRemaining = 0;
                    }
                    return new SellerActiveListingDto(
                            listing.getId(),
                            listing.getTitle(),
                            listing.getCurrentBid(),
                            listing.getBidCount(),
                            timeRemaining
                    );
                })
                .collect(Collectors.toList());

        return dataStore.paginate(activeListings, page, pageSize);
    }

    /**
     * Gets paginated completed auctions for a seller.
     *
     * @param sellerId the seller's user ID
     * @param page     page number (1-based)
     * @param pageSize number of items per page
     * @return paginated list of SellerCompletedAuctionDto
     */
    public PaginatedResponse<SellerCompletedAuctionDto> getCompletedAuctions(String sellerId, Integer page, Integer pageSize) {
        List<SellerCompletedAuctionDto> completedAuctions = dataStore.getListings().values().stream()
                .filter(listing -> sellerId.equals(listing.getSellerId().toString()))
                .filter(listing -> "ended".equals(listing.getStatus()))
                .map(listing -> {
                    String winnerDisplayName = findWinnerDisplayName(listing.getId().toString());
                    boolean reserveMet = listing.getCurrentBid() != null
                            && listing.getReservePrice() != null
                            && listing.getCurrentBid().compareTo(listing.getReservePrice()) >= 0;

                    return new SellerCompletedAuctionDto(
                            listing.getId(),
                            listing.getTitle(),
                            listing.getCurrentBid(),
                            winnerDisplayName,
                            reserveMet,
                            listing.getAuctionEndDate()
                    );
                })
                .collect(Collectors.toList());

        return dataStore.paginate(completedAuctions, page, pageSize);
    }

    // ========== Private Helpers ==========

    /**
     * Finds the bidderId of the current highest bidder for an auction.
     * Returns null if there are no bids on the auction.
     */
    private String findCurrentHighestBidderId(String auctionId) {
        List<BidEntity> bids = dataStore.getBidsForAuction(auctionId);
        if (bids == null || bids.isEmpty()) {
            return null;
        }
        return bids.stream()
                .max(Comparator.comparing(BidEntity::getAmount))
                .map(bid -> bid.getBidderId().toString())
                .orElse(null);
    }

    /**
     * Builds a bidder alias from the user's display name.
     * Uses an abbreviated form (first name + last initial) or the full display name if single word.
     */
    private String buildBidderAlias(String userId) {
        return dataStore.getUserById(userId)
                .map(user -> {
                    String displayName = user.getDisplayName();
                    if (displayName == null || displayName.isBlank()) {
                        return "Anonymous";
                    }
                    String[] parts = displayName.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
                    }
                    return parts[0];
                })
                .orElse("Anonymous");
    }

    /**
     * Finds the display name of the highest bidder (winner) for a completed auction.
     */
    private String findWinnerDisplayName(String auctionId) {
        List<BidEntity> bids = dataStore.getBidsForAuction(auctionId);
        if (bids.isEmpty()) {
            return null;
        }

        // Find the bid with the highest amount
        BidEntity highestBid = bids.stream()
                .max(Comparator.comparing(BidEntity::getAmount))
                .orElse(null);

        // Look up the user's display name
        return dataStore.getUserById(highestBid.getBidderId().toString())
                .map(UserEntity::getDisplayName)
                .orElse(highestBid.getBidderAlias());
    }

    /**
     * Maps a BidEntity to a BidDto.
     */
    private BidDto mapToBidDto(BidEntity entity) {
        return new BidDto(
                entity.getId(),
                entity.getAuctionId(),
                entity.getBidderId(),
                entity.getBidderAlias(),
                entity.getAmount(),
                entity.getTimestamp()
        );
    }
}
