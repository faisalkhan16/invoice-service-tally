package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse
{

    //TODO make response more elegant and standardized
    @JsonProperty(value = "id")
    private String seqId;
    @JsonProperty(value = "invoice_id")
    private String invoiceId;
    @JsonProperty(value="qr_code")
    private String qrcode;
    @JsonProperty(value="status")
    private String zatcaStatus;
    @JsonProperty(value = "pdf")
    private String pdfContent;
    @JsonProperty(value = "xml")
    private String xmlContent;
    @JsonProperty(value="qr_code_file")
    private String qrcodeFile;
    @JsonProperty(value="Validation_results")
    private ZakatValidationResultModel validationResults;
}
