package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.PaymentIntentDto;
import com.furniturebid.mockapi.dto.response.PaymentRecordDto;
import com.furniturebid.mockapi.entity.PaymentEntity;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.store.MockDataStore;
import com.furniturebid.mockapi.util.PaginationHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling payment operations: creating payment intents,
 * confirming payments, and retrieving payment history.
 */
@Service
public class PaymentService {

    private final MockDataStore dataStore;

    public PaymentService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

     /**
     * Creates a new payment intent for a user's auction win.
     *
     * @param userId    the ID of the user making the payment
     * @param auctionId the ID of the auction being paid for
     * @param amount    the payment amount
     * @return PaymentIntentDto with generated ID, clientSecret, amount, currency, and status
     */
    public PaymentIntentDto createPaymentIntent(String userId, String auctionId, BigDecimal amount) {
        UUID id = UUID.randomUUID();
        String clientSecret = "pi_mock_secret_" + UUID.randomUUID();

        PaymentEntity payment = new PaymentEntity(
                id,
                UUID.fromString(userId),
                UUID.fromString(auctionId),
                clientSecret,
                amount,
                "USD",
                "requires_payment_method",
                Instant.now()
        );

        dataStore.getPayments().put(id.toString(), payment);

        return new PaymentIntentDto(id.toString(), clientSecret, "USD", "requires_payment_method", amount);
    }

    /**
     * Confirms a payment intent, updating its status to "succeeded".
     *
     * @param userId          the ID of the user confirming the payment
     * @param paymentIntentId the ID of the payment intent to confirm
     * @throws NotFoundException if the payment intent is not found
     */
    public void confirmPayment(String userId, String paymentIntentId) {
        PaymentEntity payment = dataStore.getPayments().get(paymentIntentId);

        if (payment == null) {
            throw new NotFoundException("Payment", paymentIntentId);
        }

        payment.setStatus("succeeded");
    }

    /**
     * Retrieves paginated payment history for a user, sorted by createdAt descending.
     *
     * @param userId   the ID of the user
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return PaginatedResponse containing PaymentRecordDto items
     */
    public PaginatedResponse<PaymentRecordDto> getPaymentHistory(String userId, Integer page, Integer pageSize) {
        List<PaymentRecordDto> records = dataStore.getPaymentsForUser(userId).stream()
                .sorted(Comparator.comparing(PaymentEntity::getCreatedAt).reversed())
                .map(this::toPaymentRecordDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(records, page, pageSize);
    }

    /**
     * Converts a PaymentEntity to a PaymentRecordDto.
     */
    private PaymentRecordDto toPaymentRecordDto(PaymentEntity entity) {
        return new PaymentRecordDto(
                entity.getId(),
                entity.getAuctionId(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getCreatedAt()
        );
    }
}
