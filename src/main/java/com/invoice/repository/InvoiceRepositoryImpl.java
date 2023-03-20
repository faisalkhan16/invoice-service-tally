package com.invoice.repository;

import com.invoice.mapper.*;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceLob;
import com.invoice.model.InvoiceMaster;
import com.invoice.model.InvoiceReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class InvoiceRepositoryImpl {

    @Autowired
    @Qualifier("JdbcTemplate2")
    private JdbcTemplate jdbcTemplateSecondary;

    @Value("${INVOICE_ID_PREFIX}")
    private String invoiceIDPrefix;

    private String getNextInvoiceId()
    {
        try
        {
            String sql = "SELECT IFNULL(MAX(CONVERT(SUBSTRING(ID, 11), DECIMAL)),0) + 1 FROM invoice_master WHERE SUBSTRING(ID, 5, 6) = DATE_FORMAT(now(), '%y%m%d')";

            int id = jdbcTemplateSecondary.queryForObject(sql, Integer.class);

            return invoiceIDPrefix + new SimpleDateFormat("yyMMdd").format(new java.util.Date()) + String.format("%06d", id);
        }
        catch (Exception ex)
        {
            log.error("Exeption in InvoiceRepoImpl getNextInvoiceId : {}",ex.getMessage());
            return "";
        }
    }

    public long createInvoiceMaster(InvoiceMaster invoiceMaster){
        try
        {
            String sql = "INSERT INTO invoice_master(" +
                    "   ID,TYPE,SUB_TYPE,ISSUE_DATE,SUPPLY_DATE,SUPPLY_END_DATE,STATUS," +
                    "   SELLER_ID_NUMBER,SELLER_ID_TYPE,SELLER_VAT_NUMBER,SELLER_E_NAME,SELLER_A_NAME," +
                    "   SELLER_BUILDING_NO,SELLER_STREET,SELLER_LINE,SELLER_DISTRICT,SELLER_ADDITIONAL_NO,SELLER_POSTAL_CD," +
                    "   SELLER_CITY,SELLER_COUNTRY," +
                    "   BUYER_ID_NUMBER,BUYER_ID_TYPE,BUYER_VAT_NUMBER,BUYER_A_NAME," +
                    "   BUYER_BUILDING_NO,BUYER_STREET,BUYER_LINE,BUYER_DISTRICT,BUYER_ADDITIONAL_NO,BUYER_POSTAL_CD," +
                    "   BUYER_CITY,BUYER_COUNTRY,TOTAL_AMOUNT,DISCOUNT,TAXABLE_AMOUNT,TOTAL_VAT,TAX_INCLUSIVE_AMOUNT," +
                    "   ORIGINAL_INV_ID,SELLER_REGION,PAYMENT_MEANS_CODE,BUYER_E_NAME,BUYER_REGION,INVOICE_NOTE_REASON, UUID, SRC_ID," +
                    "   INVOICE_CRNCY, FX_RATE, TAX_AMT_SAR, TTL_AMT_SAR, PO_ID, CNTRCT_ID, EGS_SERIAL_NO,BUYER_EMAIL,BUYER_MOBILE" +
                    " ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, getNextInvoiceId());
                ps.setString(2, invoiceMaster.getType());
                ps.setString(3, invoiceMaster.getSubType());
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setDate(5, Date.valueOf(invoiceMaster.getSupplyDate()));
                ps.setDate(6, (null != invoiceMaster.getSupplyEndDate()?Date.valueOf(invoiceMaster.getSupplyEndDate()):null));
                ps.setString(7, invoiceMaster.getStatus());
                ps.setString(8, invoiceMaster.getSellerId());
                ps.setString(9, invoiceMaster.getSellerIdTyp());
                ps.setString(10, invoiceMaster.getSellerVatNumber());
                ps.setString(11, invoiceMaster.getSellerEName());
                ps.setString(12, invoiceMaster.getSellerAName());
                ps.setString(13, invoiceMaster.getSellerBuildingNo());
                ps.setString(14, invoiceMaster.getSellerStreet());
                ps.setString(15, invoiceMaster.getSellerLine());
                ps.setString(16, invoiceMaster.getSellerDistrict());
                ps.setString(17, invoiceMaster.getSellerAdditionalNo());
                ps.setString(18, invoiceMaster.getSellerPostalCode());
                ps.setString(19, invoiceMaster.getSellerCity());
                ps.setString(20, invoiceMaster.getSellerCountry());
                ps.setString(21, invoiceMaster.getBuyerIdNumber());
                ps.setString(22, invoiceMaster.getBuyerIdTyp());
                ps.setString(23, invoiceMaster.getBuyerVatNumber());
                ps.setString(24, invoiceMaster.getBuyerAName());
                ps.setString(25, invoiceMaster.getBuyerBuildingNo());
                ps.setString(26, invoiceMaster.getBuyerStreet());
                ps.setString(27, invoiceMaster.getBuyerLine());
                ps.setString(28, invoiceMaster.getBuyerDistrict());
                ps.setString(29, invoiceMaster.getBuyerAdditionalNo());
                ps.setString(30, invoiceMaster.getBuyerPostalCode());
                ps.setString(31, invoiceMaster.getBuyerCity());
                ps.setString(32, invoiceMaster.getBuyerCountry());
                ps.setDouble(33, invoiceMaster.getTotalAmount());
                ps.setDouble(34, invoiceMaster.getDiscount());
                ps.setDouble(35, invoiceMaster.getTaxableAmount());
                ps.setDouble(36, invoiceMaster.getTotalVAT());
                ps.setDouble(37, invoiceMaster.getTaxInclusiveAmount());
                ps.setString(38, invoiceMaster.getOriginalInvoiceId());
                ps.setString(39, invoiceMaster.getSellerRegion());
                ps.setString(40, invoiceMaster.getPaymentMeansCode());
                ps.setString(41, invoiceMaster.getBuyerEName());
                ps.setString(42, invoiceMaster.getBuyerRegion());
                ps.setString(43,invoiceMaster.getInvoiceNoteReason());
                ps.setString(44, String.valueOf(UUID.randomUUID()));
                ps.setString(45, invoiceMaster.getId());
                ps.setString(46, invoiceMaster.getCurrency());
                ps.setDouble(47, invoiceMaster.getFxRate());
                ps.setDouble(48, invoiceMaster.getTaxSAR());
                ps.setDouble(49, invoiceMaster.getTotalSAR());
                ps.setString(50, invoiceMaster.getPurchaseOrderID());
                ps.setString(51, invoiceMaster.getContractID());
                ps.setString(52, invoiceMaster.getSerialNumber());
                ps.setString(53,invoiceMaster.getBuyerEmail());
                ps.setString(54,invoiceMaster.getBuyerMobile());

                return ps;
            },generatedKeyHolder);

            long seqId = generatedKeyHolder.getKey().longValue();
            return seqId;
        }
        catch (Exception ex)
        {
            log.error("Exeption in InvoiceRepoImpl createInvoiceMaster : {}",ex.getMessage());
            return 0;
        }
    }

    public void createInvoiceLine(InvoiceLine invoiceLine){
        try {

            String sql = "INSERT INTO invoice_line" +
                    "(LINE_ID, SEQ_REF, NAME, QUANTITY, NET_PRICE, TOTAL_AMOUNT, DISCOUNT, TOTAL_TAXABLE_AMOUNT, TAX_RATE, TAX_AMOUNT, SUBTOTAL, STATUS, VAT_CTGRY, EXMP_RSN_CD, EXMP_RSN_TXT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, invoiceLine.getLineId());
                ps.setLong(2, invoiceLine.getSeqRef());
                ps.setString(3, invoiceLine.getName());
                ps.setDouble(4, invoiceLine.getQuantity());
                ps.setDouble(5, invoiceLine.getNetPrice());
                ps.setDouble(6, invoiceLine.getTotalAmount());
                ps.setDouble(7, invoiceLine.getDiscount());
                ps.setDouble(8, invoiceLine.getTotalTaxableAmount());
                ps.setDouble(9, invoiceLine.getTaxRate());
                ps.setDouble(10, invoiceLine.getTaxAmount());
                ps.setDouble(11, invoiceLine.getSubTotal());
                ps.setString(12, invoiceLine.getStatus());
                ps.setString(13, invoiceLine.getItemTaxCategoryCode());
                ps.setString(14, invoiceLine.getExemptionReasonCode());
                ps.setString(15, invoiceLine.getExemptionReasonText());
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl createInvoiceLine SEQ ID:{} {}",invoiceLine.getSeqRef(),ex.getMessage());
        }
    }

    public void createInvoiceLOBS(InvoiceMaster invoiceMaster){
        try {

            String sql = "INSERT INTO invlobs" +
                    "(SEQ_REF, QR_CODE, INVOICE_HASH, INVOICE_XML, SIGNED_XML, ZATCA_RESPONSE) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setLong(1, invoiceMaster.getSeqId());
                ps.setString(2, invoiceMaster.getQrCode());
                ps.setString(3, invoiceMaster.getInvocieHash());
                ps.setString(4, invoiceMaster.getXml());
                ps.setString(5, invoiceMaster.getSignedXML());
                ps.setString(6, invoiceMaster.getZatcaResponse());
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl createInvoiceLOBS SEQ ID:{} {}",invoiceMaster.getSeqId(),ex.getMessage());
        }
    }

    public List<InvoiceMaster> getInvoices(){

        try{
            String sql = "SELECT SEQ_ID, ID, TYPE, SUB_TYPE, ISSUE_DATE, SUPPLY_DATE, SUPPLY_END_DATE, STATUS, " +
                    "SELLER_ID_NUMBER, SELLER_ID_TYPE, SELLER_VAT_NUMBER, SELLER_E_NAME, SELLER_A_NAME, " +
                    "SELLER_BUILDING_NO, SELLER_STREET, SELLER_LINE, SELLER_DISTRICT, SELLER_ADDITIONAL_NO, SELLER_POSTAL_CD, " +
                    "SELLER_CITY, SELLER_COUNTRY, " +
                    "BUYER_ID_NUMBER, BUYER_ID_TYPE, BUYER_VAT_NUMBER, BUYER_A_NAME, " +
                    "BUYER_BUILDING_NO, BUYER_STREET, BUYER_LINE, BUYER_DISTRICT, BUYER_ADDITIONAL_NO, BUYER_POSTAL_CD, " +
                    "BUYER_CITY, BUYER_COUNTRY, TOTAL_AMOUNT, DISCOUNT, TAXABLE_AMOUNT, TOTAL_VAT, TAX_INCLUSIVE_AMOUNT, " +
                    "ORIGINAL_INV_ID,SELLER_REGION,PAYMENT_MEANS_CODE,BUYER_E_NAME,BUYER_REGION,INVOICE_NOTE_REASON, UUID, " +
                    "INVOICE_CRNCY, FX_RATE, TAX_AMT_SAR, TTL_AMT_SAR, PO_ID, CNTRCT_ID, EGS_SERIAL_NO, CLRNC_STS,BUYER_EMAIL,BUYER_MOBILE " +
                    "FROM invoice_master WHERE STATUS = 'C' ORDER BY SEQ_ID ASC;";

            return jdbcTemplateSecondary.query(sql, new InvoiceMapper());

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getInvoices : {}",ex.getMessage());
            return null;
        }
    }


    public List<InvoiceMaster> getSimplifiedInvoices(){

        try{
            String sql = "SELECT SEQ_ID, SUB_TYPE, UUID, INVOICE_HASH, INVOICE_XML, SIGNED_XML ,QR_CODE, SELLER_VAT_NUMBER, EGS_SERIAL_NO,invoice_master.ID,BUYER_EMAIL,BUYER_MOBILE FROM invoice_master,invlobs  WHERE STATUS = 'P' and SEQ_ID = SEQ_REF AND SUB_TYPE LIKE '02%' ORDER BY SEQ_ID ASC;";

            return jdbcTemplateSecondary.query(sql, new InvoiceMasterMapper());

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getReportInvoices : {}",ex.getMessage());
            return null;
        }
    }

    public List<InvoiceMaster> getStandardInvoices(){

        try{
            String sql = "SELECT SEQ_ID,SUB_TYPE, UUID, INVOICE_HASH, INVOICE_XML, SIGNED_XML ,QR_CODE, SELLER_VAT_NUMBER, EGS_SERIAL_NO,invoice_master.ID,BUYER_EMAIL,BUYER_MOBILE FROM invoice_master,invlobs  WHERE STATUS = 'P' and SEQ_ID = SEQ_REF AND SUB_TYPE LIKE '01%' ORDER BY SEQ_ID ASC;";

            return jdbcTemplateSecondary.query(sql, new InvoiceMasterMapper());

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getReportInvoices : {}",ex.getMessage());
            return null;
        }
    }

    public InvoiceMaster getInvoice(long id)
    {
        try{
            String sql = "SELECT SEQ_ID, ID, TYPE, SUB_TYPE, ISSUE_DATE, SUPPLY_DATE, SUPPLY_END_DATE, STATUS, " +
                    "SELLER_ID_NUMBER, SELLER_ID_TYPE, SELLER_VAT_NUMBER, SELLER_E_NAME, SELLER_A_NAME, " +
                    "SELLER_BUILDING_NO, SELLER_STREET, SELLER_LINE, SELLER_DISTRICT, SELLER_ADDITIONAL_NO, SELLER_POSTAL_CD, " +
                    "SELLER_CITY, SELLER_COUNTRY, " +
                    "BUYER_ID_NUMBER, BUYER_ID_TYPE, BUYER_VAT_NUMBER, BUYER_A_NAME, " +
                    "BUYER_BUILDING_NO, BUYER_STREET, BUYER_LINE, BUYER_DISTRICT, BUYER_ADDITIONAL_NO, BUYER_POSTAL_CD, " +
                    "BUYER_CITY, BUYER_COUNTRY, TOTAL_AMOUNT, DISCOUNT, TAXABLE_AMOUNT, TOTAL_VAT, TAX_INCLUSIVE_AMOUNT, " +
                    "ORIGINAL_INV_ID, SELLER_REGION, PAYMENT_MEANS_CODE, BUYER_E_NAME, BUYER_REGION, INVOICE_NOTE_REASON, UUID, " +
                    "INVOICE_CRNCY, FX_RATE, TAX_AMT_SAR, TTL_AMT_SAR, PO_ID, CNTRCT_ID, EGS_SERIAL_NO, CLRNC_STS,BUYER_EMAIL,BUYER_MOBILE " +
                    "FROM invoice_master WHERE SEQ_ID = ?;";

            return jdbcTemplateSecondary.queryForObject(sql, new InvoiceMapper(),new Object[]{id});

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getInvoice SEQ ID:{} {}",id,ex.getMessage());
            return null;
        }
    }

    public String getPreviousInvocieHash(Long id)
    {
        try
        {
            //for first invoice return base64 hash of 0
            if(id == 1)
            {
                return "NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ==";
            }

            id = id - 1;

            String sql = "SELECT INVOICE_HASH from invlobs  WHERE SEQ_REF = ?;";

            return jdbcTemplateSecondary.queryForObject(sql, String.class,new Object[]{id});


        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getPreviousInvocieHash invoiceID:{} Exception: {}",id,ex.getStackTrace());
            return null;
        }
    }


    public List<InvoiceLine> getInvoiceLines(){

        try{
            String sql = "SELECT LINE_ID, SEQ_REF, NAME, QUANTITY, NET_PRICE, TOTAL_AMOUNT, DISCOUNT, TOTAL_TAXABLE_AMOUNT, TAX_RATE, " +
                    "TAX_AMOUNT, SUBTOTAL, STATUS, VAT_CTGRY, EXMP_RSN_CD, EXMP_RSN_TXT " +
                    "FROM invoice_line WHERE STATUS = 'V';";

            return jdbcTemplateSecondary.query(sql, new InvoiceLineMapper());

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getInvoiceLines : {}",ex.getMessage());
            return null;
        }

    }

    public List<InvoiceLine> getInvoiceLinesByInvoiceId(long invoiceMasterId){

        try{
            String sql = "SELECT LINE_ID, SEQ_REF, NAME, QUANTITY, NET_PRICE, TOTAL_AMOUNT, DISCOUNT, TOTAL_TAXABLE_AMOUNT, TAX_RATE, " +
                    "TAX_AMOUNT, SUBTOTAL, STATUS, VAT_CTGRY, EXMP_RSN_CD, EXMP_RSN_TXT " +
                    "FROM invoice_line WHERE SEQ_REF = ?;";

            return jdbcTemplateSecondary.query(sql,new InvoiceLineMapper(), new Object[]{invoiceMasterId});

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getInvoiceLinesByInvoiceId SEQ ID:{} {}",invoiceMasterId,ex.getMessage());
            return null;
        }

    }

    public void updateInvoiceStatus(Long sequenceNo, String status){
        try {

            String sql = "UPDATE invoice_master SET STATUS = ?, UPD_DT = now() WHERE SEQ_ID = ?";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, status);
                ps.setLong(2, sequenceNo);
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl updateInvoiceStatus SEQ ID:{} {}",sequenceNo,ex.getMessage());
        }
    }

    public void updateInvoiceLineStatus(Long sequenceNo, String status){
        try {

            String sql = "UPDATE invoice_line SET STATUS = ? WHERE SEQ_REF = ?";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, status);
                ps.setLong(2, sequenceNo);
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl updateInvoiceLineStatus SEQ ID:{} {}",sequenceNo,ex.getMessage());
        }
    }

    public void updatesInvoiceLOBS(InvoiceMaster invoiceMaster) {
        try {

            String sql = "UPDATE invlobs SET QR_CODE = ?, SIGNED_XML = ?, ZATCA_RESPONSE = ? WHERE SEQ_REF = ?";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, invoiceMaster.getQrCode());
                ps.setString(2, invoiceMaster.getSignedXML());
                ps.setString(3, invoiceMaster.getZatcaResponse());
                ps.setLong(4, invoiceMaster.getSeqId());
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl updatesInvoiceLOBS SEQ ID:{} {}",invoiceMaster.getSeqId(),ex.getMessage());
        }
    }

    public void updateInvoiceResponse(InvoiceMaster invoiceMaster) {
        try {

            String sql = "UPDATE invoice_master SET CLRNC_STS = ?, VLD_STS = ?, STS_DT = now() WHERE SEQ_ID = ?";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, invoiceMaster.getZatcaStatus());
                ps.setString(2, invoiceMaster.getValidationStatus());
                ps.setLong(3, invoiceMaster.getSeqId());
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl updateInvoiceResponse SEQ ID:{} {}",invoiceMaster.getSeqId(),ex.getMessage());
        }
    }



    public List<InvoiceMaster> getPendingInvoices(){

        try{
            String sql = "SELECT SEQ_ID, ID, TYPE, SUB_TYPE, ISSUE_DATE, SUPPLY_DATE, SUPPLY_END_DATE, STATUS, " +
                    "SELLER_ID_NUMBER, SELLER_ID_TYPE, SELLER_VAT_NUMBER, SELLER_E_NAME, SELLER_A_NAME, " +
                    "SELLER_BUILDING_NO, SELLER_STREET, SELLER_LINE, SELLER_DISTRICT, SELLER_ADDITIONAL_NO, SELLER_POSTAL_CD, " +
                    "SELLER_CITY, SELLER_COUNTRY, " +
                    "BUYER_ID_NUMBER, BUYER_ID_TYPE, BUYER_VAT_NUMBER, BUYER_A_NAME, " +
                    "BUYER_BUILDING_NO, BUYER_STREET, BUYER_LINE, BUYER_DISTRICT, BUYER_ADDITIONAL_NO, BUYER_POSTAL_CD, " +
                    "BUYER_CITY, BUYER_COUNTRY, TOTAL_AMOUNT, DISCOUNT, TAXABLE_AMOUNT, TOTAL_VAT, TAX_INCLUSIVE_AMOUNT, " +
                    "ORIGINAL_INV_ID,SELLER_REGION,PAYMENT_MEANS_CODE,BUYER_E_NAME,BUYER_REGION,INVOICE_NOTE_REASON, UUID, " +
                    "INVOICE_CRNCY, FX_RATE, TAX_AMT_SAR, TTL_AMT_SAR, PO_ID, CNTRCT_ID, EGS_SERIAL_NO, CLRNC_STS,BUYER_EMAIL,BUYER_MOBILE " +
                    "FROM invoice_master WHERE STATUS = 'P' ORDER BY SEQ_ID ASC;";

            return jdbcTemplateSecondary.query(sql, new InvoiceMapper());

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getPendingInvoices : {}",ex.getMessage());
            return null;
        }
    }

    public String getInvoiceId(long seqId) {
        try{
            String sql = "SELECT ID FROM invoice_master WHERE SEQ_ID = ?;";

            return jdbcTemplateSecondary.queryForObject(sql, String.class,new Object[]{seqId});

        }catch(Exception ex){
            log.error("Exeption in InvoiceRepoImpl getInvoiceId SEQ ID: {} {}",seqId,ex.getMessage());
            return null;
        }
    }

    public void savePDF(String pdf, Long seqRef) {
        try {

            String sql = "UPDATE invlobs SET PDF = ? WHERE SEQ_REF = ?";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, pdf);
                ps.setLong(2, seqRef);
                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in InvoiceRepoImpl savePDF SEQ ID:{} {}",seqRef,ex.getMessage());
        }
    }

    public InvoiceMaster getInvoiceById(String idNumber)
    {
        try {
            String sql = "SELECT SEQ_ID, ID, TYPE, SUB_TYPE, ISSUE_DATE, SUPPLY_DATE, SUPPLY_END_DATE, STATUS, " +
                    "SELLER_ID_NUMBER, SELLER_ID_TYPE, SELLER_VAT_NUMBER, SELLER_E_NAME, SELLER_A_NAME, " +
                    "SELLER_BUILDING_NO, SELLER_STREET, SELLER_LINE, SELLER_DISTRICT, SELLER_ADDITIONAL_NO, SELLER_POSTAL_CD, " +
                    "SELLER_CITY, SELLER_COUNTRY, " +
                    "BUYER_ID_NUMBER, BUYER_ID_TYPE, BUYER_VAT_NUMBER, BUYER_A_NAME, " +
                    "BUYER_BUILDING_NO, BUYER_STREET, BUYER_LINE, BUYER_DISTRICT, BUYER_ADDITIONAL_NO, BUYER_POSTAL_CD, " +
                    "BUYER_CITY, BUYER_COUNTRY, TOTAL_AMOUNT, DISCOUNT, TAXABLE_AMOUNT, TOTAL_VAT, TAX_INCLUSIVE_AMOUNT, " +
                    "ORIGINAL_INV_ID,SELLER_REGION,PAYMENT_MEANS_CODE,BUYER_E_NAME,BUYER_REGION,INVOICE_NOTE_REASON, UUID, " +
                    "INVOICE_CRNCY, FX_RATE, TAX_AMT_SAR, TTL_AMT_SAR, PO_ID, CNTRCT_ID, EGS_SERIAL_NO, CLRNC_STS,BUYER_EMAIL,BUYER_MOBILE " +
                    "FROM invoice_master WHERE ID = ? ORDER BY SEQ_ID DESC;";

            return jdbcTemplateSecondary.queryForObject(sql, new InvoiceMapper(), new Object[]{idNumber});
        } catch (Exception ex) {
            log.error("Exeption in InvoiceRepoImpl getInvoiceById ID: {} : {}",idNumber, ex.getMessage());
            return null;
        }
    }

    public InvoiceLob getInvoiceLobById(Long seqId)
    {
        try {
            String sql = "SELECT ID, SEQ_REF, QR_CODE, INVOICE_HASH, INVOICE_XML, SIGNED_XML, PDF, ZATCA_RESPONSE "+
                    "FROM invlobs WHERE SEQ_REF = ?;";

            return jdbcTemplateSecondary.queryForObject(sql, new InvoiceLobMapper(), new Object[]{ seqId});
        } catch (Exception ex) {
            log.error("Exeption in InvoiceRepoImpl getInvoiceLobById SeqId {} : {}",seqId, ex.getMessage());
            return null;
        }
    }

    public long saveInvoiceReport(InvoiceReport invoiceReport)
    {
        try {
            String sql = "INSERT INTO invoice_report" +
                    "(ID,UUID,SUB_TYPE,BUYER_EMAIL,INVOICE_HASH,SIGNED_XML,CERT,CERT_KEY,CR_DT,STS,CERT_STS,QR_CODE,PDF,VAT_NUMBER,EGS_SERIAL_NO,INVOICE_DT ) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?);";

            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, invoiceReport.getId());
                ps.setString(2, invoiceReport.getUuid());
                ps.setString(3, invoiceReport.getSubType());
                ps.setString(4, invoiceReport.getBuyerEmail());
                ps.setString(5, invoiceReport.getInvoiceHash());
                ps.setString(6, invoiceReport.getInvoice());
                ps.setString(7, invoiceReport.getCertificate());
                ps.setString(8, invoiceReport.getSecretKey());
                ps.setDate(9, Date.valueOf(LocalDate.now()));
                ps.setString(10, "C");
                ps.setString(11,invoiceReport.getCertStatus());
                ps.setString(12,invoiceReport.getQrCode());
                ps.setString(13,invoiceReport.getPdf());
                ps.setString(14,invoiceReport.getVatNumber());
                ps.setString(15,invoiceReport.getSerialNo());
                ps.setTimestamp(16, Timestamp.valueOf(invoiceReport.getInvoiceTimeStamp()));
                return ps;
            },generatedKeyHolder);

            long seqId = generatedKeyHolder.getKey().longValue();
            return seqId;

        } catch (Exception ex) {
            log.error("Exeption in InvoiceRepoImpl saveInvoiceReport: ID : {} {}",invoiceReport.getId(), ex.getMessage());
            return 0;
        }
    }

    public void updateInvoiceReport(InvoiceReport invoiceReport)
    {
        try {
            String sql = "UPDATE invoice_report SET STS = ?, UPD_DT = now(), ZATCA_RESPONSE = ?, SIGNED_XML = ?, QR_CODE = ?, PDF = ? WHERE SEQ_ID = ?;";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, invoiceReport.getStatus());
                ps.setString(2, invoiceReport.getZatcaResponse());
                ps.setString(3, invoiceReport.getInvoice());
                ps.setString(4, invoiceReport.getQrCode());
                ps.setString(5, invoiceReport.getPdf());
                ps.setLong(6, invoiceReport.getSeqId());
                return ps;
            });

        } catch (Exception ex) {
            log.error("Exeption in InvoiceRepoImpl updateInvoiceReport: SEQ ID : {} {}",invoiceReport.getSeqId(), ex.getMessage());

        }
    }

}
