package com.invoice.mapper;

import com.invoice.dto.SellerDTO;
import com.invoice.model.Seller;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface MapSeller
{
    @Mapping(target = "sellerNinNumber", source = "sellerNinNumber")
    @Mapping(target = "sellerIdTyp", source = "sellerIdTyp")
    @Mapping(target = "sellerVatNumber", source = "sellerVatNumber")
    @Mapping(target = "sellerEName", source = "sellerEName")
    @Mapping(target = "sellerAName", source = "sellerAName")
    @Mapping(target = "sellerBuildingNo", source = "sellerBuildingNo")
    @Mapping(target = "sellerStreet", source = "sellerStreet")
    @Mapping(target = "sellerLine", source = "sellerLine")
    @Mapping(target = "sellerDistrict", source = "sellerDistrict")
    @Mapping(target = "sellerAdditionalNo", source = "sellerAdditionalNo")
    @Mapping(target = "sellerPostalCode", source = "sellerPostalCode")
    @Mapping(target = "sellerCity", source = "sellerCity")
    @Mapping(target = "sellerRegion", source = "sellerRegion")
    @Mapping(target = "sellerCountry", source = "sellerCountry")
    @Mapping(target = "certificateName", source = "certificateName")
    @Mapping(target = "serialNo", source = "serialNo")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "invTypes", source = "invTypes")
    @Mapping(target = "ounit", source = "ounit")
    @Mapping(target = "csr", source = "csr")
    @Mapping(target = "privateKey", source = "privateKey")
    @Mapping(target = "requestIdCompliance", source = "requestIdCompliance")
    @Mapping(target = "certificateCompliance", source = "certificateCompliance")
    @Mapping(target = "keyCompliance", source = "keyCompliance")
    @Mapping(target = "certificateProduction", source = "certificateProduction")
    @Mapping(target = "keyProduction", source = "keyProduction")
    @Mapping(target = "requestIdProduction", source = "requestIdProduction")
    @Mapping(target = "sellerStatus", source = "sellerStatus")
    @Mapping(target = "certificateStatus", source = "certificateStatus")
    @Mapping(target = "zatcaFlag", source = "zatcaFlag")
    @Mapping(target = "image", source = "image")
    @Mapping(target = "expiryDate", source = "expiryDate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SellerDTO sellerToSellerDTO(Seller seller);

    @InheritInverseConfiguration(name = "sellerToSellerDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Seller sellerDTOToSeller(SellerDTO sellerDTO);
}
