package com.invoice.dto.zakat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZakatComplianceCISDResponseDTO {

    @JsonProperty("requestID")
    private String requestID;

    @JsonProperty("dispositionMessage")
    private String dispositionMessage;

    @JsonProperty("binarySecurityToken")
    private String binarySecurityToken;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("validationResults")
    private ZakatValidationResultModel validationResults;
}
