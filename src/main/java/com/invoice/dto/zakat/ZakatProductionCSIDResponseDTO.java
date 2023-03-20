package com.invoice.dto.zakat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;

public class ZakatProductionCSIDResponseDTO {

    @JsonProperty("requestID")
    private String requestID;

    @JsonProperty("dispositionMessage")
    private String dispositionMessage;

    @JsonProperty("binarySecurityToken")
    private String binarySecurityToken;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("validationResults")
    private ZakatValidationResultModel validationResults;
}
