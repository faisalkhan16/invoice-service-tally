package com.invoice.mapper;

import com.invoice.model.InvoiceMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InvoiceMasterMapper implements RowMapper<InvoiceMaster> {
    @Override
    public InvoiceMaster mapRow(ResultSet rs, int rowNum) throws SQLException {


        InvoiceMaster invoiceMaster = new InvoiceMaster();
        try {
            invoiceMaster.setSeqId(rs.getLong("SEQ_ID"));
            invoiceMaster.setQrCode(rs.getString("QR_CODE"));
            invoiceMaster.setInvocieHash(rs.getString("INVOICE_HASH"));
            invoiceMaster.setXml(rs.getString("INVOICE_XML"));
            invoiceMaster.setUuid(rs.getString("UUID"));
            invoiceMaster.setSignedXML(rs.getString("SIGNED_XML"));
            invoiceMaster.setSellerVatNumber(rs.getString("SELLER_VAT_NUMBER"));
            invoiceMaster.setSerialNumber(rs.getString("EGS_SERIAL_NO"));
            invoiceMaster.setSubType(rs.getString("SUB_TYPE"));
            invoiceMaster.setId(rs.getString("ID"));
            invoiceMaster.setBuyerEmail(rs.getString("BUYER_EMAIL"));
            invoiceMaster.setBuyerMobile(rs.getString("BUYER_MOBILE"));

        }
        catch (Exception ex)
        {
            log.error("Exception in InvoiceLobMapper: {}",ex.getMessage());
        }
        return invoiceMaster;
    }
}
