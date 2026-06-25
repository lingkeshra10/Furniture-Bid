package com.furniturebid.mockapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FlagRequest {

    @NotBlank
    @Size(min = 1, max = 500)
    private String reason;

    public FlagRequest() {
    }

    public FlagRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
