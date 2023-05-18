package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceDTO
{
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "invoice_type_code")
    private String type;

    @JsonProperty(value = "invoice_sub_type")
    private String subType;

    @JsonProperty(value = "original_invoice_id")
    private String originalInvoiceId;

    @JsonProperty(value="purchase_order_id")
    private String purchaseOrderID;

    @JsonProperty(value="contract_id")
    private String contractID;

    @JsonProperty(value = "supply_date")
    private LocalDate supplyDate;

    @JsonProperty(value = "supply_end_date")
    private LocalDate supplyEndDate;

    @JsonProperty(value = "seller_id_number")
    private String sellerId;

    @JsonProperty(value = "seller_id_type")
    private String sellerIdTyp;

    @JsonProperty(value = "seller_vat_number")
    private String sellerVatNumber;

    @JsonProperty(value = "seller_name_english")
    private String sellerEName;

    @JsonProperty(value = "seller_name_arabic")
    private String sellerAName;

    @JsonProperty(value = "seller_building_no")
    private String sellerBuildingNo;

    @JsonProperty(value = "seller_street")
    private String sellerStreet;

    @JsonProperty(value = "seller_address_line")
    private String sellerLine;

    @JsonProperty(value = "seller_district")
    private String sellerDistrict;

    @JsonProperty(value = "seller_additional_no")
    private String sellerAdditionalNo;

    @JsonProperty(value = "payment_means_code")
    private String paymentMeansCode;

    @JsonProperty(value = "invoice_note_reason")
    private String invoiceNoteReason;

    @JsonProperty(value = "seller_postal_code")
    private String sellerPostalCode;

    @JsonProperty(value = "seller_city")
    private String sellerCity;

    @JsonProperty(value = "seller_region")
    private String sellerRegion;

    @JsonProperty(value = "seller_country")
    private String sellerCountry;

    // @Size(min = 10, max = 10, message = "buyer id must be of length 10")
    @JsonProperty(value = "buyer_id_number")
    private String buyerIdNumber;

    @JsonProperty(value = "buyer_id_type")
    private String buyerIdTyp;

    @JsonProperty(value = "buyer_name_arabic")
    private String buyerAName;

    @JsonProperty(value = "buyer_name_english")
    private String buyerEName;

    @JsonProperty(value = "buyer_vat_number")
    private String buyerVatNumber;

    @JsonProperty(value = "buyer_building_no")
    private String buyerBuildingNo;

    @JsonProperty(value = "buyer_street")
    private String buyerStreet;

    @JsonProperty(value = "buyer_address_line")
    private String buyerLine;

    @JsonProperty(value = "buyer_district")
    private String buyerDistrict;

    @JsonProperty(value = "buyer_additional_no")
    private String buyerAdditionalNo;

    @JsonProperty(value = "buyer_postal_code")
    private String buyerPostalCode;

    @JsonProperty(value = "buyer_city")
    private String buyerCity;

    @JsonProperty(value = "buyer_region")
    private String buyerRegion;

    @JsonProperty(value = "buyer_country")
    private String buyerCountry;

    @JsonProperty(value = "invoice_lines")
    private List<InvoiceLineDTO> invoiceLines;

    @JsonProperty(value = "total_amount")
    private double totalAmount;

    @JsonProperty(value = "total_discount")
    private double discount;

    @JsonProperty(value = "total_exclusive_vat")
    private double taxableAmount;

    @JsonProperty(value = "total_vat")
    private double totalVAT;

    @JsonProperty(value = "total_inclusive_vat")
    private double taxInclusiveAmount;

    @JsonProperty(value = "invoice_currency")
    private String currency;
    @JsonProperty(value = "exchange_rate")
    private double fxRate;
    @JsonProperty(value = "total_tax_sar")
    private double taxSAR;
    @JsonProperty(value = "total_amount_sar")
    private double totalSAR;

    @JsonProperty(value = "egs_serial_no")
    private String serialNumber;

    @JsonProperty(value = "buyer_email")
    private String buyerEmail;

    @JsonProperty(value = "buyer_mobile")
    private String buyerMobile;

    @JsonProperty(value = "payment_terms")
    private String paymentTerms;
}
