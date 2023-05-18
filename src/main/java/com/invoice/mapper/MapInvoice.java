package com.invoice.mapper;

import com.invoice.dto.InvoiceDTO;
import com.invoice.model.InvoiceMaster;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface MapInvoice {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "subType", source = "subType")
    @Mapping(target = "originalInvoiceId", source = "originalInvoiceId")
    @Mapping(target = "supplyDate", source = "supplyDate")
    @Mapping(target = "supplyEndDate", source = "supplyEndDate")
    @Mapping(target = "purchaseOrderID", source = "purchaseOrderID")
    @Mapping(target = "contractID", source = "contractID")
    @Mapping(target = "sellerId", source = "sellerId")
    @Mapping(target = "sellerIdTyp", source = "sellerIdTyp")
    @Mapping(target = "sellerVatNumber", source = "sellerVatNumber")
    @Mapping(target = "sellerEName", source = "sellerEName")
    @Mapping(target = "sellerAName", source = "sellerAName")
    @Mapping(target = "sellerBuildingNo", source = "sellerBuildingNo")
    @Mapping(target = "sellerStreet", source = "sellerStreet")
    @Mapping(target = "sellerLine", source = "sellerLine")
    @Mapping(target = "sellerDistrict", source = "sellerDistrict")
    @Mapping(target = "sellerAdditionalNo", source = "sellerAdditionalNo")
    @Mapping(target = "paymentMeansCode", source = "paymentMeansCode")
    @Mapping(target = "invoiceNoteReason", source = "invoiceNoteReason")
    @Mapping(target = "sellerPostalCode", source = "sellerPostalCode")
    @Mapping(target = "sellerCity", source = "sellerCity")
    @Mapping(target = "sellerRegion", source = "sellerRegion")
    @Mapping(target = "sellerCountry", source = "sellerCountry")
    @Mapping(target = "buyerIdNumber", source = "buyerIdNumber")
    @Mapping(target = "buyerAName", source = "buyerAName")
    @Mapping(target = "buyerEName", source = "buyerEName")
    @Mapping(target = "buyerIdTyp", source = "buyerIdTyp")
    @Mapping(target = "buyerVatNumber", source = "buyerVatNumber")
    @Mapping(target = "buyerBuildingNo", source = "buyerBuildingNo")
    @Mapping(target = "buyerStreet", source = "buyerStreet")
    @Mapping(target = "buyerLine", source = "buyerLine")
    @Mapping(target = "buyerDistrict", source = "buyerDistrict")
    @Mapping(target = "buyerAdditionalNo", source = "buyerAdditionalNo")
    @Mapping(target = "buyerPostalCode", source = "buyerPostalCode")
    @Mapping(target = "buyerCity", source = "buyerCity")
    @Mapping(target = "buyerRegion", source = "buyerRegion")
    @Mapping(target = "buyerCountry", source = "buyerCountry")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "discount", source = "discount")
    @Mapping(target = "taxableAmount", source = "taxableAmount")
    @Mapping(target = "totalVAT", source = "totalVAT")
    @Mapping(target = "taxInclusiveAmount", source = "taxInclusiveAmount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "fxRate", source = "fxRate")
    @Mapping(target = "taxSAR", source = "taxSAR")
    @Mapping(target = "totalSAR", source = "totalSAR")
    @Mapping(target = "serialNumber", source = "serialNumber")
    @Mapping(target = "buyerEmail", source = "buyerEmail")
    @Mapping(target = "buyerMobile", source = "buyerMobile")
    @Mapping(target = "paymentTerms", source = "paymentTerms")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceMaster invoiceDTOToInvoice (InvoiceDTO invoiceDTO);

    @InheritInverseConfiguration(name = "invoiceDTOToInvoice")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceDTO invoiceToInvoiceDTO (InvoiceMaster invoiceMaster);

    @InheritConfiguration(name = "invoiceToInvoiceDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<InvoiceDTO> invoicesToInvoiceDTOs (List<InvoiceMaster> invoiceMasterList);
}
