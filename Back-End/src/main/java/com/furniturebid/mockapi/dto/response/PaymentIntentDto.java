package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;

public class PaymentIntentDto {

    private String id;
    private String clientSecret;
    private String currency;
    private String status;
    private BigDecimal amount;

    public PaymentIntentDto() {
    }

    public PaymentIntentDto(String id, String clientSecret, String currency,
                            String status, BigDecimal amount) {
        this.id = id;
        this.clientSecret = clientSecret;
        this.currency = currency;
        this.status = status;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
