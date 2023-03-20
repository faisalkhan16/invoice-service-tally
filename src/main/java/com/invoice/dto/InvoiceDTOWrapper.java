package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.invoice.validator.InvoiceRequestConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@InvoiceRequestConstraint
public class InvoiceDTOWrapper {

    @JsonProperty(value = "data")
    private InvoiceDTO invoiceDTO;
}
