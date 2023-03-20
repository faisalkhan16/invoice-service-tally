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
public class InvoiceLineDTO
{
    @JsonProperty(value = "id")
    private int lineId;

    @JsonProperty(value = "item_name")
    private String name;

    @JsonProperty(value="tax_category_code")
    private String itemTaxCategoryCode;

    @JsonProperty(value="exemption_reason_code")
    private String exemptionReasonCode;

    @JsonProperty(value="exemption_reason_text")
    private String exemptionReasonText;

    @JsonProperty(value = "invoiced_quantity")
    private int quantity;

    @JsonProperty(value = "unit_price")
    private double netPrice;

    @JsonProperty(value = "net_price")
    private double totalAmount;

    @JsonProperty(value = "discount")
    private double discount;

    @JsonProperty(value = "taxable_amount")
    private double totalTaxableAmount;

    @JsonProperty(value = "tax_rate")
    private double taxRate;

    @JsonProperty(value = "tax_amount")
    private double taxAmount;

    @JsonProperty(value = "sub_total_incl_vat")
    private double subTotal;
}
