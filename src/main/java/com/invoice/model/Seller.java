package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seller
{
    private Long id;
    private String sellerNinNumber;
    private String sellerIdTyp;
    private String sellerVatNumber;
    private String sellerEName;
    private String sellerAName;
    private String sellerBuildingNo;
    private String sellerStreet;
    private String sellerLine;
    private String sellerDistrict;
    private String sellerAdditionalNo;
    private String sellerPostalCode;
    private String sellerCity;
    private String sellerRegion;
    private String sellerCountry;

    private String certificateName;
    private String serialNo;
    private String location;
    private String industry;
    private String invTypes;
    private String ounit;
    private String csr;
    private String privateKey;
    private String requestIdCompliance;
    private String certificateCompliance;
    private String keyCompliance;
    private String certificateProduction;
    private String keyProduction;
    private String requestIdProduction;
    private String sellerStatus;
    private String certificateStatus;
    private LocalDate createDate;
    private LocalDate updateDate;

    private String otp;

    private String zatcaFlag;

    private String image;

    private LocalDate expiryDate;

}
