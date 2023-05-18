package com.invoice.mapper;

import com.invoice.model.InvoiceMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InvoiceMapper implements RowMapper<InvoiceMaster> {

    @Override
    public InvoiceMaster mapRow(ResultSet rs, int rowNum) throws SQLException {


        InvoiceMaster invoiceMaster = new InvoiceMaster();
        try {
            invoiceMaster.setSeqId(rs.getLong("SEQ_ID"));
            invoiceMaster.setId(rs.getString("ID"));
            invoiceMaster.setType(rs.getString("TYPE"));
            invoiceMaster.setSubType(rs.getString("SUB_TYPE"));
            invoiceMaster.setIssueDate(rs.getDate("ISSUE_DATE").toLocalDate());
            invoiceMaster.setSupplyDate(rs.getDate("SUPPLY_DATE").toLocalDate());
            if(null != rs.getDate("SUPPLY_END_DATE")) {
                invoiceMaster.setSupplyEndDate(rs.getDate("SUPPLY_END_DATE").toLocalDate());
            }
            invoiceMaster.setStatus(rs.getString("STATUS"));

            invoiceMaster.setSellerId(rs.getString("SELLER_ID_NUMBER"));
            invoiceMaster.setSellerIdTyp(rs.getString("SELLER_ID_TYPE"));
            invoiceMaster.setSellerVatNumber(rs.getString("SELLER_VAT_NUMBER"));
            invoiceMaster.setSellerEName(rs.getString("SELLER_E_NAME"));
            invoiceMaster.setSellerAName(rs.getString("SELLER_A_NAME"));
            invoiceMaster.setSellerBuildingNo(rs.getString("SELLER_BUILDING_NO"));
            invoiceMaster.setSellerStreet(rs.getString("SELLER_STREET"));
            invoiceMaster.setSellerLine(rs.getString("SELLER_LINE"));
            invoiceMaster.setSellerDistrict(rs.getString("SELLER_DISTRICT"));
            invoiceMaster.setSellerAdditionalNo(rs.getString("SELLER_ADDITIONAL_NO"));
            invoiceMaster.setSellerPostalCode(rs.getString("SELLER_POSTAL_CD"));
            invoiceMaster.setSellerCity(rs.getString("SELLER_CITY"));
            invoiceMaster.setSellerCountry(rs.getString("SELLER_COUNTRY"));

            invoiceMaster.setBuyerIdNumber(rs.getString("BUYER_ID_NUMBER"));
            invoiceMaster.setBuyerIdTyp(rs.getString("BUYER_ID_TYPE"));
            invoiceMaster.setBuyerVatNumber(rs.getString("BUYER_VAT_NUMBER"));
            invoiceMaster.setBuyerAName(rs.getString("BUYER_A_NAME"));
            invoiceMaster.setBuyerBuildingNo(rs.getString("BUYER_BUILDING_NO"));
            invoiceMaster.setBuyerStreet(rs.getString("BUYER_STREET"));
            invoiceMaster.setBuyerLine(rs.getString("BUYER_LINE"));
            invoiceMaster.setBuyerDistrict(rs.getString("BUYER_DISTRICT"));
            invoiceMaster.setBuyerAdditionalNo(rs.getString("BUYER_ADDITIONAL_NO"));
            invoiceMaster.setBuyerPostalCode(rs.getString("BUYER_POSTAL_CD"));
            invoiceMaster.setBuyerCity(rs.getString("BUYER_CITY"));
            invoiceMaster.setBuyerCountry(rs.getString("BUYER_COUNTRY"));

            invoiceMaster.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
            invoiceMaster.setDiscount(rs.getDouble("DISCOUNT"));
            invoiceMaster.setTaxableAmount(rs.getDouble("TAXABLE_AMOUNT"));
            invoiceMaster.setTotalVAT(rs.getDouble("TOTAL_VAT"));
            invoiceMaster.setTaxInclusiveAmount(rs.getDouble("TAX_INCLUSIVE_AMOUNT"));

            invoiceMaster.setOriginalInvoiceId(rs.getString("ORIGINAL_INV_ID"));
            invoiceMaster.setSellerRegion(rs.getString("SELLER_REGION"));
            invoiceMaster.setPaymentMeansCode(rs.getString("PAYMENT_MEANS_CODE"));
            invoiceMaster.setBuyerEName(rs.getString("BUYER_E_NAME"));
            invoiceMaster.setBuyerRegion(rs.getString("BUYER_REGION"));
            invoiceMaster.setInvoiceNoteReason(rs.getString("INVOICE_NOTE_REASON"));

            invoiceMaster.setUuid(rs.getString("UUID"));

            invoiceMaster.setCurrency(rs.getString("INVOICE_CRNCY"));
            invoiceMaster.setFxRate(rs.getDouble("FX_RATE"));
            invoiceMaster.setTaxSAR(rs.getDouble("TAX_AMT_SAR"));
            invoiceMaster.setTotalSAR(rs.getDouble("TTL_AMT_SAR"));

            invoiceMaster.setPurchaseOrderID(rs.getString("PO_ID"));
            invoiceMaster.setContractID(rs.getString("CNTRCT_ID"));
            invoiceMaster.setSerialNumber(rs.getString("EGS_SERIAL_NO"));
            invoiceMaster.setBuyerEmail(rs.getString("BUYER_EMAIL"));
            invoiceMaster.setBuyerMobile(rs.getString("BUYER_MOBILE"));
            invoiceMaster.setInvoiceSrcId(rs.getString("SRC_ID"));
            invoiceMaster.setPaymentTerms(rs.getString("PAYMENT_TERMS"));

            if(null != rs.getString("CLRNC_STS")) {
                invoiceMaster.setZatcaStatus(rs.getString("CLRNC_STS"));
            }
        }
        catch (Exception ex)
        {
            log.error("Exception in InvoiceMapper: {}",ex.getMessage());
        }
        return invoiceMaster;
    }
}
