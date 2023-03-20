package com.invoice.mapper;

import com.invoice.model.InvoiceReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InvoiceReportMapper implements RowMapper<InvoiceReport> {
    @Override
    public InvoiceReport mapRow(ResultSet resultSet, int row) throws SQLException {
        InvoiceReport invoiceReport = new InvoiceReport();
        try{
            invoiceReport.setSeqId(resultSet.getLong("SEQ_ID"));
            invoiceReport.setId(resultSet.getString("ID"));
            invoiceReport.setSubType(resultSet.getString("SUB_TYPE"));
            invoiceReport.setBuyerEmail(resultSet.getString("BUYER_EMAIL"));
            invoiceReport.setInvoiceHash(resultSet.getString("INVOICE_HASH"));
            invoiceReport.setUuid(resultSet.getString("UUID"));
            invoiceReport.setInvoice(resultSet.getString("SIGNED_XML"));
            invoiceReport.setCertificate(resultSet.getString("CERT"));
            invoiceReport.setSecretKey(resultSet.getString("CERT_KEY"));
            invoiceReport.setCertStatus(resultSet.getString("CERT_STS"));
            invoiceReport.setQrCode(resultSet.getString("QR_CODE"));
            invoiceReport.setPdf(resultSet.getString("PDF"));
            return invoiceReport;
        }catch (Exception ex){
            log.error("Exception in InvoiceReportMapper : {}",ex.getMessage());
        }

        return null;
    }
}
