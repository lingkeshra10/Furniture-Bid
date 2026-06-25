package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.request.ConfirmPaymentRequest;
import com.furniturebid.mockapi.dto.request.CreatePaymentIntentRequest;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.dto.response.PaymentIntentDto;
import com.furniturebid.mockapi.dto.response.PaymentRecordDto;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling payment endpoints: create payment intent,
 * confirm payment, and retrieve payment history.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments/create-payment-intent - Create a new payment intent.
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentDto> createPaymentIntent(
            HttpServletRequest request,
            @Valid @RequestBody CreatePaymentIntentRequest body) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        PaymentIntentDto response = paymentService.createPaymentIntent(
                authUser.getUserId(),
                body.getAuctionId(),
                body.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/payments/confirm-payment - Confirm an existing payment intent.
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<Void> confirmPayment(
            HttpServletRequest request,
            @Valid @RequestBody ConfirmPaymentRequest body) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        paymentService.confirmPayment(authUser.getUserId(), body.getPaymentIntentId());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/payments/history - Retrieve paginated payment history for the authenticated user.
     */
    @GetMapping("/history")
    public ResponseEntity<PaginatedResponse<PaymentRecordDto>> getPaymentHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        PaginatedResponse<PaymentRecordDto> response =
                paymentService.getPaymentHistory(authUser.getUserId(), page, pageSize);
        return ResponseEntity.ok(response);
    }
}
