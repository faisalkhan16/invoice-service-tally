package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerDTO
{
    @JsonProperty(value = "id_number")
    private String sellerNinNumber;
    @JsonProperty(value = "id_type")
    private String sellerIdTyp;
    @JsonProperty(value = "vat_number")
    private String sellerVatNumber;
    @JsonProperty(value = "english_name")
    private String sellerEName;
    @JsonProperty(value = "arabic_name")
    private String sellerAName;
    @JsonProperty(value = "building_number")
    private String sellerBuildingNo;
    @JsonProperty(value = "street_name")
    private String sellerStreet;
    @JsonProperty(value = "address_line")
    private String sellerLine;
    @JsonProperty(value = "district")
    private String sellerDistrict;
    @JsonProperty(value = "additional_number")
    private String sellerAdditionalNo;
    @JsonProperty(value = "postal_code")
    private String sellerPostalCode;
    @JsonProperty(value = "city")
    private String sellerCity;
    @JsonProperty(value = "region")
    private String sellerRegion;
    @JsonProperty(value = "country")
    private String sellerCountry;
    @JsonProperty(value = "certificate_name")
    private String certificateName;
    @JsonProperty(value = "egs_serial_no")
    private String serialNo;
    @JsonProperty(value = "location")
    private String location;
    @JsonProperty(value = "industry")
    private String industry;
    @JsonProperty(value = "invoice_type")
    private String invTypes;
    @JsonProperty(value = "o_unit")
    private String ounit;
    @JsonProperty(value = "csr")
    private String csr;
    @JsonProperty("private_key")
    private String privateKey;
    @JsonProperty("request_id_compliance")
    private String requestIdCompliance;
    @JsonProperty("certificate_compliance")
    private String certificateCompliance;
    @JsonProperty("key_compliance")
    private String keyCompliance;
    @JsonProperty("certificate_production")
    private String certificateProduction;
    @JsonProperty("key_production")
    private String keyProduction;
    @JsonProperty("request_id_production")
    private String requestIdProduction;
    @JsonProperty("seller_status")
    private String sellerStatus;
    @JsonProperty("certificate_status")
    private String certificateStatus;
    @JsonProperty("zatca_approved")
    private String zatcaFlag;

    @JsonSerialize
    @JsonDeserialize
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty(value = "image")
    private String image;

}
