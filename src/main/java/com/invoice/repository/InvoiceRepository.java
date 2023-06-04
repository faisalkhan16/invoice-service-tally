package com.invoice.repository;

import com.invoice.dto.SellerDTO;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceLob;
import com.invoice.model.InvoiceMaster;

import java.util.List;

public interface InvoiceRepository {

    public long createInvoiceMaster(InvoiceMaster invoiceMaster);

    public void createInvoiceLine(InvoiceLine invoiceLine);

    public void createInvoiceLOBS(InvoiceMaster invoiceMaster);

    public List<InvoiceMaster> getInvoices();


    public List<InvoiceMaster> getSimplifiedInvoices();

    public List<InvoiceMaster> getStandardInvoices();

    public InvoiceMaster getInvoice(long id);

    public String getPreviousInvocieHash(Long id);

    public List<InvoiceLine> getInvoiceLines();

    public List<InvoiceLine> getInvoiceLinesByInvoiceId(long invoiceMasterId);

    public void updateInvoiceStatus(Long sequenceNo, String status);

    public void updateInvoiceLineStatus(Long sequenceNo, String status);

    public void updatesInvoiceLOBS(InvoiceMaster invoiceMaster);

    public void updateInvoiceResponse(InvoiceMaster invoiceMaster);

    public List<InvoiceMaster> getPendingInvoices();

    public String getInvoiceId(long seqId);

    public void savePDF(String pdf, Long seqRef);

    public InvoiceMaster getInvoiceById(String idNumber);

    public InvoiceLob getInvoiceLobById(Long seqId);

    public String getPDFromLOB(String invoiceID);

    public InvoiceMaster updateInvoiceMaster(InvoiceMaster invoiceMaster);

    public void updateInvoiceLine(InvoiceLine invoiceLine) ;

    public void updateInvoiceLOBS(InvoiceMaster invoiceMaster);

    public String getPreviousInvocieHashForFailureInvoice(Long seqRefId, SellerDTO sellerDTO);

    public String getXML(String invoiceNumber);

}
