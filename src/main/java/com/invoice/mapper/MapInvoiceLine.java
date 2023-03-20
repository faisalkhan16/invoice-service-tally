package com.invoice.mapper;

import com.invoice.dto.InvoiceDTO;
import com.invoice.dto.InvoiceLineDTO;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceMaster;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface MapInvoiceLine {

    @Mapping(target = "lineId", source = "lineId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "itemTaxCategoryCode", source = "itemTaxCategoryCode")
    @Mapping(target = "exemptionReasonCode", source = "exemptionReasonCode")
    @Mapping(target = "exemptionReasonText", source = "exemptionReasonText")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "netPrice", source = "netPrice")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "discount", source = "discount")
    @Mapping(target = "totalTaxableAmount", source = "totalTaxableAmount")
    @Mapping(target = "taxRate", source = "taxRate")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "subTotal", source = "subTotal")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceLine invoiceLineDTOToInvoiceLine (InvoiceLineDTO invoiceLineDTO);

    @InheritConfiguration(name = "invoiceLineDTOToInvoiceLine")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<InvoiceLine> invoiceLineDTOsToInvoiceLines(List<InvoiceLineDTO> invoiceLineDTOList);

    @InheritConfiguration(name = "invoiceLineToInvoiceDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceLineDTO invoiceLineToInvoiceLineDTO(InvoiceLine invoiceLine);

    @InheritConfiguration(name = "invoiceLineToInvoiceDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<InvoiceLineDTO> invoiceLinesToInvoiceLineDTOs(List<InvoiceLine> invoiceLineList);

}
