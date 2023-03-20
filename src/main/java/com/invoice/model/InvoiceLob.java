package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceLob {

    private long id;
    private long seqId;
    private String xml;
    private String signedXML;
    private String invocieHash;
    private String qrCode;
    private String zatcaResponse;
    private String pdf;

}
