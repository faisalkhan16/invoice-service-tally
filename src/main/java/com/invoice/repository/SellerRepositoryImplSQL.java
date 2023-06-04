package com.invoice.repository;

import com.invoice.exception.SellerNotFoundException;
import com.invoice.mapper.SellerMapper;
import com.invoice.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

@Slf4j
@Repository
@Qualifier("SQLService")
public class SellerRepositoryImplSQL implements SellerRepository{

    @Autowired
    @Qualifier("JdbcTemplateMysql")
    private JdbcTemplate jdbcTemplateSecondary;

    @Autowired
    @Qualifier("JdbcTemplateH2")
    private JdbcTemplate jdbcTemplatePrimary;

    public Seller getSellerByVatAndSerial(){
        try {

            String sql = "SELECT TOP (1) SEQ_ID, ID_NUMBER, ID_TYPE, VAT_NUMBER, NAME_E, NAME_A, BUILDING_NO, STREET, LINE, DISTRICT, ADDITIONAL_NO, POSTAL_CD, CITY, COUNTRY, CNAME, SERIAL_NO, LOCATION, INDUSTRY, INV_TYPES, OUNIT, CSR, PRIVATE_KEY, REQUEST_ID_COMP, CERT_COMP, KEY_COMP, CERT_PROD, KEY_PROD, REQUEST_ID_PROD, SLR_STS, CERT_STS, ZATCA_FLG, IMAGE  FROM invslr;";

            return jdbcTemplateSecondary.queryForObject(sql, new SellerMapper());

        }catch (EmptyResultDataAccessException ex){
            log.error("EmptyResultDataAccessException in SellerRepositoryImpl getSellerByVatAndSerial {} ",ex.getMessage());
            throw new SellerNotFoundException("Seller is not registered");
        }catch(Exception ex){
            log.error("Exception in SellerRepositoryImpl getSellerByVatAndSerial {} ",ex.getMessage());
            return null;
        }
    }

    public int checkIsSellerExists(String vatNumber,String serialNumber)
    {
        try
        {
            String sql = "SELECT COUNT(1) FROM invslr WHERE  VAT_NUMBER = ? AND SERIAL_NO = ?;";

            return jdbcTemplateSecondary.queryForObject(sql, Integer.class,new Object[]{vatNumber, serialNumber});

        }catch(Exception ex){
            log.error("Exeption in SellerRepositoryImpl checkIsSellerExists VatNumber: {} SerialNo: {} Exception: {}",vatNumber,serialNumber,ex.getMessage());
            return 0;
        }
    }

    public void createSeller(Seller seller) {
        try
        {
            String sql = "INSERT INTO invslr" +
                    "(ID_NUMBER, ID_TYPE, VAT_NUMBER, NAME_E, NAME_A, BUILDING_NO, STREET, LINE, DISTRICT, ADDITIONAL_NO, POSTAL_CD, CITY, COUNTRY, CNAME, SERIAL_NO, LOCATION, INDUSTRY," +
                    " INV_TYPES, OUNIT, CSR, PRIVATE_KEY, REQUEST_ID_COMP, CERT_COMP, KEY_COMP, SLR_STS, CERT_STS,ZATCA_FLG, CR_DT, CERT_PROD, KEY_PROD , REQUEST_ID_PROD, IMAGE)" +
                    "VALUES" +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,GETDATE(),? , ? ,?,?);";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, seller.getSellerNinNumber());
                ps.setString(2, seller.getSellerIdTyp());
                ps.setString(3, seller.getSellerVatNumber());
                ps.setString(4, seller.getSellerEName());
                ps.setString(5, seller.getSellerAName());
                ps.setString(6, seller.getSellerBuildingNo());
                ps.setString(7, seller.getSellerStreet());
                ps.setString(8, seller.getSellerLine());
                ps.setString(9, seller.getSellerDistrict());
                ps.setString(10, seller.getSellerAdditionalNo());
                ps.setString(11, seller.getSellerPostalCode());
                ps.setString(12, seller.getSellerCity());
                ps.setString(13, seller.getSellerCountry());
                ps.setString(14, seller.getCertificateName());
                ps.setString(15, seller.getSerialNo());
                ps.setString(16, seller.getLocation());
                ps.setString(17, seller.getIndustry());
                ps.setString(18, seller.getInvTypes());
                ps.setString(19, seller.getOunit());
                ps.setString(20, seller.getCsr());
                ps.setString(21, seller.getPrivateKey());
                ps.setString(22, seller.getRequestIdCompliance());
                ps.setString(23, seller.getCertificateCompliance());
                ps.setString(24, seller.getKeyCompliance());
                ps.setString(25, seller.getSellerStatus());
                ps.setString(26, seller.getCertificateStatus());
                ps.setString(27, seller.getZatcaFlag());
                ps.setString(28, seller.getCertificateProduction());
                ps.setString(29, seller.getKeyProduction());
                ps.setString(30, seller.getRequestIdProduction());
                ps.setString(31, seller.getImage());

                return ps;
            });

        }
        catch (Exception ex)
        {
            log.error("Exception in SellerRepositoryImpl createSeller VatNubmer and SerialNumber:{} {} {} "+ seller.getSellerVatNumber(),seller.getSerialNo(),ex.getMessage());
        }
    }

    public void update(Seller seller) {

        try
        {
            String sql = "UPDATE invslr" +
                    " SET ID_NUMBER = ?, ID_TYPE = ? , NAME_E = ? , NAME_A = ?, BUILDING_NO = ?, STREET = ?, LINE = ?, DISTRICT = ?, ADDITIONAL_NO = ?, POSTAL_CD = ?, CITY = ?, COUNTRY = ? , CNAME = ? , LOCATION = ? , INDUSTRY = ? ," +
                    " INV_TYPES = ? , OUNIT = ? , CSR = ? , PRIVATE_KEY = ? , REQUEST_ID_COMP = ? , CERT_COMP = ? , KEY_COMP = ? , SLR_STS = ? ,CERT_PROD = ? , KEY_PROD = ? , REQUEST_ID_PROD = ? , CERT_STS = ?  ,ZATCA_FLG = ?, UPD_DT = GETDATE(), IMAGE = ? WHERE VAT_NUMBER = ? AND SERIAL_NO = ? ;";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, seller.getSellerNinNumber());
                ps.setString(2, seller.getSellerIdTyp());
                ps.setString(3, seller.getSellerEName());
                ps.setString(4, seller.getSellerAName());
                ps.setString(5, seller.getSellerBuildingNo());
                ps.setString(6, seller.getSellerStreet());
                ps.setString(7, seller.getSellerLine());
                ps.setString(8, seller.getSellerDistrict());
                ps.setString(9, seller.getSellerAdditionalNo());
                ps.setString(10, seller.getSellerPostalCode());
                ps.setString(11, seller.getSellerCity());
                ps.setString(12, seller.getSellerCountry());
                ps.setString(13, seller.getCertificateName());
                ps.setString(14, seller.getLocation());
                ps.setString(15, seller.getIndustry());
                ps.setString(16, seller.getInvTypes());
                ps.setString(17, seller.getOunit());
                ps.setString(18, seller.getCsr());
                ps.setString(19, seller.getPrivateKey());
                ps.setString(20, seller.getRequestIdCompliance());
                ps.setString(21, seller.getCertificateCompliance());
                ps.setString(22, seller.getKeyCompliance());
                ps.setString(23, seller.getSellerStatus());
                ps.setString(24, seller.getCertificateProduction());
                ps.setString(25, seller.getKeyProduction());
                ps.setString(26, seller.getRequestIdProduction());
                ps.setString(27, seller.getCertificateStatus());
                ps.setString(28, seller.getZatcaFlag());
                ps.setString(29, seller.getImage());
                ps.setString(30, seller.getSellerVatNumber());
                ps.setString(31, seller.getSerialNo());

                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in SellerRepositoryImpl update serial no:{} {}",seller.getSerialNo(),ex.getMessage());
        }
    }

    public int checkIsSellerInfoExists(String vatNumber,String serialNumber)
    {
        try
        {
            String sql = "SELECT COUNT(1) FROM SELLER_INFO;";

            return jdbcTemplatePrimary.queryForObject(sql, Integer.class);

        }catch(Exception ex){
            log.error("Exeption in SellerRepositoryImpl checkIsSellerInfoExists VatNumber: {} SerialNo: {} Exception: {}",vatNumber,serialNumber,ex.getMessage());
            return 0;
        }
    }

    public LocalDate getExpiryDate()
    {
        LocalDate expiryDate = LocalDate.now().minusDays(1);
        try
        {
            String sql = "SELECT TOP (1) EXPIRY_DT FROM SELLER_INFO;";

            Date expiryDateSQL = jdbcTemplatePrimary.queryForObject(sql, Date.class);

            if(null != expiryDateSQL){

                expiryDate = expiryDateSQL.toLocalDate();
                return expiryDate;

            }

        }catch(Exception ex){
            log.error("Exeption in SellerRepositoryImpl getExpiryDate Exception: {}",ex.getMessage());
        }
        return expiryDate;

    }

    public void createSellerInfo(Seller seller)
    {
        try
        {
            String sql = "INSERT INTO SELLER_INFO (VAT_NUMBER,SERIAL_NO,EXPIRY_DT) VALUES (?,?,?);";

            jdbcTemplatePrimary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, seller.getSellerVatNumber());
                ps.setString(2, seller.getSerialNo());
                ps.setDate(3, Date.valueOf(seller.getExpiryDate()));

                return ps;
            });
        }
        catch (Exception ex)
        {
            log.error("Exception in SellerRepositoryImpl createSellerInfo VatNubmer and SerialNumber:{} {} {} "+ seller.getSellerVatNumber(),seller.getSerialNo(),ex.getMessage());
        }
    }

    public void updateSellerInfo(Seller seller) {

        try
        {
            String sql = "UPDATE SELLER_INFO" +
                    " SET EXPIRY_DT = ?;";

            jdbcTemplatePrimary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setDate(1, Date.valueOf(seller.getExpiryDate()));

                return ps;
            });

        }catch (Exception ex){
            log.error("Exeption in SellerRepositoryImpl updateSellerInfo serial no:{} {}",seller.getSerialNo(),ex.getMessage());
        }
    }
}
