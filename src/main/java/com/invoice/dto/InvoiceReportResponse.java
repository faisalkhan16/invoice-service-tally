package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceReportResponse {

    @JsonProperty(value = "total_pending")
    private int totalPendingInvoices;
    @JsonProperty(value = "total_processed")
    private int totalProcessedInvoices;
    @JsonProperty(value = "total_failed")
    private int totalFailedInvoice;
    @JsonProperty(value = "status")
    private String status;
}
