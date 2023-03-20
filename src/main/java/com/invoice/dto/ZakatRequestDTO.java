package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZakatRequestDTO {

    @JsonProperty("invoice_hash")
    private String invoiceHash;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("invoice")
    private String invoice;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("secret_key")
    private String secretKey;

    @JsonProperty("invoice_id")
    private String invoiceID;

    @JsonProperty("pdf")
    private String pdf;
    @JsonProperty("buyer_email")
    private String buyerEmail;

    @JsonProperty(value = "buyer_mobile")
    private String buyerMobile;
}
