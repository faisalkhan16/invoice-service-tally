package com.invoice.mapper;

import com.invoice.model.InvoiceLob;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceLobMapper implements RowMapper<InvoiceLob> {
    @Override
    public InvoiceLob mapRow(ResultSet resultSet, int i) throws SQLException {
        InvoiceLob invoiceLob =  new InvoiceLob();
        invoiceLob.setSeqId(resultSet.getLong("SEQ_REF"));
        invoiceLob.setId(resultSet.getLong("ID"));
        invoiceLob.setQrCode(resultSet.getString("QR_CODE"));
        invoiceLob.setInvocieHash(resultSet.getString("INVOICE_HASH"));
        invoiceLob.setXml(resultSet.getString("INVOICE_XML"));
        invoiceLob.setSignedXML(resultSet.getString("SIGNED_XML"));
        invoiceLob.setPdf(resultSet.getString("PDF"));
        invoiceLob.setZatcaResponse(resultSet.getString("ZATCA_RESPONSE"));
        return invoiceLob;
    }
}
