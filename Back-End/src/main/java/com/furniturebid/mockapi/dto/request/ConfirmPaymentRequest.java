package com.furniturebid.mockapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ConfirmPaymentRequest {

    @NotBlank
    private String paymentIntentId;

    public ConfirmPaymentRequest() {
    }

    public ConfirmPaymentRequest(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}
