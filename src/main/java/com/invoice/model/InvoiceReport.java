package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceReport {

    private Long seqId;
    private String id;
    private String subType;
    private String buyerEmail;
    private String invoiceHash;
    private String uuid;
    private String invoice;
    private String certificate;
    private String secretKey;

    private String zatcaResponse;

    private String status;

    private LocalDate crDt;

    private LocalDate updDt;

    private String certStatus;

    private String qrCode;
    private String pdf;

    private String vatNumber;

    private String serialNo;

    private LocalDateTime invoiceTimeStamp;


}
