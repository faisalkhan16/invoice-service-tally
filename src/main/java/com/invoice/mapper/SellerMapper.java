package com.invoice.mapper;

import com.invoice.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class SellerMapper implements RowMapper<Seller> {
    @Override
    public Seller mapRow(ResultSet rs, int rowNum) throws SQLException {

        Seller seller =  new Seller();
        try {

            seller.setId(rs.getLong("SEQ_ID"));
            seller.setSellerNinNumber(rs.getString("ID_NUMBER"));
            seller.setSellerIdTyp(rs.getString("ID_TYPE"));
            seller.setSellerVatNumber(rs.getString("VAT_NUMBER"));
            seller.setSellerEName(rs.getString("NAME_E"));
            seller.setSellerAName(rs.getString("NAME_A"));
            seller.setSellerBuildingNo(rs.getString("BUILDING_NO"));
            seller.setSellerStreet(rs.getString("STREET"));
            seller.setSellerLine(rs.getString("LINE"));
            seller.setSellerDistrict(rs.getString("DISTRICT"));
            seller.setSellerAdditionalNo(rs.getString("ADDITIONAL_NO"));
            seller.setSellerPostalCode(rs.getString("POSTAL_CD"));
            seller.setSellerCity(rs.getString("CITY"));
            seller.setSellerCountry(rs.getString("COUNTRY"));
            seller.setCertificateName(rs.getString("CNAME"));
            seller.setSerialNo(rs.getString("SERIAL_NO"));
            seller.setLocation(rs.getString("LOCATION"));
            seller.setIndustry(rs.getString("INDUSTRY"));
            seller.setInvTypes(rs.getString("INV_TYPES"));
            seller.setOunit(rs.getString("OUNIT"));
            seller.setCsr(rs.getString("CSR"));
            seller.setPrivateKey(rs.getString("PRIVATE_KEY"));
            seller.setRequestIdCompliance(rs.getString("REQUEST_ID_COMP"));
            seller.setCertificateCompliance(rs.getString("CERT_COMP"));
            seller.setKeyCompliance(rs.getString("KEY_COMP"));
            seller.setRequestIdProduction(rs.getString("REQUEST_ID_PROD"));
            seller.setCertificateProduction(rs.getString("CERT_PROD"));
            seller.setKeyProduction(rs.getString("KEY_PROD"));
            seller.setSellerStatus(rs.getString("SLR_STS"));
            seller.setCertificateStatus(rs.getString("CERT_STS"));
            seller.setZatcaFlag(rs.getString("ZATCA_FLG"));
            seller.setImage(rs.getString("IMAGE"));

        }
        catch (Exception ex)
        {
        log.error("Exception in SellerMapper: {}",ex.getMessage());
        }

        return seller;
    }
}
