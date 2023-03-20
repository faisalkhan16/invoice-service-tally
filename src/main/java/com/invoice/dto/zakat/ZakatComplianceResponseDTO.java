package com.invoice.dto.zakat;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZakatComplianceResponseDTO {


    @JsonProperty("clearanceStatus")
    private String clearanceStatus;

    @JsonProperty("reportingStatus")
    private String reportingStatus;

    @JsonProperty("qrSellertStatus")
    private String qrSellertStatus;

    @JsonProperty("qrBuyertStatus")
    private String qrBuyertStatus;

    @JsonProperty("validationResults")
    private ZakatValidationResultModel validationResults;
}
