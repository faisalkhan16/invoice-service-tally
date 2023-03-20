package com.invoice.dto;

import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceMaster;
import lombok.*;

import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFGeneratorDTO
{
    private InvoiceMaster invoiceMaster;
    private List<InvoiceLine> invoiceLines;
    private String xml;
}
