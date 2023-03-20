package com.invoice.dto.zakat;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZakatClearanceResponseDTO {

    @JsonProperty("clearanceStatus")
    private String clearanceStatus;

    @JsonProperty("clearedInvoice")
    private String clearedInvoice;

    @JsonProperty("validationResults")
    private ZakatValidationResultModel validationResults;
}
