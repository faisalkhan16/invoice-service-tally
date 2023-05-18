package com.invoice.mapper;

import com.invoice.model.InvoiceLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InvoiceLineMapper implements RowMapper<InvoiceLine> {
    @Override
    public InvoiceLine mapRow(ResultSet rs, int rowNum) throws SQLException {

        InvoiceLine invoiceLine =  new InvoiceLine();

        try {
            invoiceLine.setLineId(rs.getInt("LINE_ID"));
            invoiceLine.setSeqRef(rs.getLong("SEQ_REF"));
            invoiceLine.setName(rs.getString("NAME"));
            invoiceLine.setQuantity(rs.getDouble("QUANTITY"));
            invoiceLine.setNetPrice(rs.getDouble("NET_PRICE"));
            invoiceLine.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
            invoiceLine.setDiscount(rs.getDouble("DISCOUNT"));
            invoiceLine.setTotalTaxableAmount(rs.getDouble("TOTAL_TAXABLE_AMOUNT"));
            invoiceLine.setTaxRate(rs.getDouble("TAX_RATE"));
            invoiceLine.setTaxAmount(rs.getDouble("TAX_AMOUNT"));
            invoiceLine.setSubTotal(rs.getDouble("SUBTOTAL"));
            invoiceLine.setStatus(rs.getString("STATUS"));
            invoiceLine.setItemTaxCategoryCode(rs.getString("VAT_CTGRY"));
            invoiceLine.setExemptionReasonCode(rs.getString("EXMP_RSN_CD"));
            invoiceLine.setExemptionReasonText(rs.getString("EXMP_RSN_TXT"));
            invoiceLine.setSkuCode(rs.getString("SKU_CODE"));

        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        return invoiceLine;
    }
}
