package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceLine {

    private int lineId;
    private Long seqRef;
    private String name;
    private String itemTaxCategoryCode;
    private String exemptionReasonCode;
    private String exemptionReasonText;
    private double quantity;
    private double netPrice;
    private double totalAmount;
    private double discount;
    private double totalTaxableAmount;
    private double taxRate;
    private double taxAmount;
    private double subTotal;
    private String status;

    private String skuCode;

}
