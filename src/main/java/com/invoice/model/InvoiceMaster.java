package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceMaster {

    private Long seqId;
    private String id;
    private String uuid;
    private String type;
    private String subType;
    private String originalInvoiceId;
    private LocalDate issueDate;
    private LocalDate supplyDate;
    private LocalDate supplyEndDate;
    private String status;
    private String purchaseOrderID;
    private String contractID;

    private String sellerId;
    private String sellerIdTyp;
    private String sellerVatNumber;
    private String sellerEName;
    private String sellerAName;
    private String sellerBuildingNo;
    private String sellerStreet;
    private String sellerLine;
    private String sellerDistrict;
    private String sellerAdditionalNo;
    private String paymentMeansCode;
    private String invoiceNoteReason;
    private String sellerPostalCode;
    private String sellerCity;
    private String sellerRegion;
    private String sellerCountry;
    private String serialNumber;

    private String buyerIdNumber;
    private String buyerAName;
    private String buyerEName;
    private String buyerIdTyp;
    private String buyerVatNumber;
    private String buyerBuildingNo;
    private String buyerStreet;
    private String buyerLine;
    private String buyerDistrict;
    private String buyerAdditionalNo;
    private String buyerPostalCode;
    private String buyerCity;
    private String buyerRegion;
    private String buyerCountry;

    private double totalAmount;
    private double discount;
    private double taxableAmount;
    private double totalVAT;
    private double taxInclusiveAmount;

    private String xml;
    private String signedXML;
    private String invocieHash;
    private String qrCode;
    private String zatcaStatus;
    private String validationStatus;
    private String zatcaResponse;

    private String currency;
    private double fxRate;
    private double taxSAR;
    private double totalSAR;

    private String certificate;
    private String certificateKey;
    private String certificateStatus;
    private String buyerEmail;
    private String buyerMobile;


}
