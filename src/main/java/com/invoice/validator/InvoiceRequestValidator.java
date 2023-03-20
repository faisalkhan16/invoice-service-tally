package com.invoice.validator;

import com.invoice.dto.InvoiceDTO;
import com.invoice.dto.InvoiceLineDTO;
import com.invoice.dto.InvoiceDTOWrapper;
import com.invoice.exception.RequestValidationException;
import com.invoice.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InvoiceRequestValidator implements ConstraintValidator<InvoiceRequestConstraint, InvoiceDTOWrapper> {

    ArrayList<String> invoiceTypes = new ArrayList<>();

    ArrayList<String> paymentMeansCodes = new ArrayList<>();

    ArrayList<String> sellerNinTypes = new ArrayList<>();

    ArrayList<String> buyerNinTypes = new ArrayList<>();

    ArrayList<String> taxCategoriesCode =  new ArrayList<>();

    @Override
    public void initialize(InvoiceRequestConstraint constraintAnnotation) {

        invoiceTypes.add("388");
        invoiceTypes.add("381");
        invoiceTypes.add("383");

        paymentMeansCodes.add("10");
        paymentMeansCodes.add("30");
        paymentMeansCodes.add("42");
        paymentMeansCodes.add("48");
        paymentMeansCodes.add("1");

        sellerNinTypes.add("CRN");
        sellerNinTypes.add("MOM");
        sellerNinTypes.add("MLS");
        sellerNinTypes.add("SAG");
        sellerNinTypes.add("OTH");

        buyerNinTypes.add("TIN");
        buyerNinTypes.add("CRN");
        buyerNinTypes.add("MOM");
        buyerNinTypes.add("MLS");
        buyerNinTypes.add("700");
        buyerNinTypes.add("SAG");
        buyerNinTypes.add("NAT");
        buyerNinTypes.add("GCC");
        buyerNinTypes.add("IQA");
        buyerNinTypes.add("PAS");
        buyerNinTypes.add("OTH");

        taxCategoriesCode.add("S");
        taxCategoriesCode.add("Z");
        taxCategoriesCode.add("E");
        taxCategoriesCode.add("O");

    }

    @Override
    public boolean isValid(InvoiceDTOWrapper invoiceDTOWrapper, ConstraintValidatorContext context) {
        String errorMessage = "";
        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();

        log.info("Invoice Request: {}",invoiceDTO);

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getType()) || !invoiceTypes.contains(invoiceDTO.getType())){
            errorMessage = "invoice_type_code must be 388 for Tax Invoice, 381 for Credit Note, 383 for Debit Note";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSubType()) || invoiceDTO.getSubType().length() != 7 || (!invoiceDTO.getSubType().startsWith("01") && !invoiceDTO.getSubType().startsWith("02"))){
            errorMessage = "invoice_sub_type is invalid";
            throw new RequestValidationException(errorMessage);
        }

        if(!paymentMeansCodes.contains(invoiceDTO.getPaymentMeansCode())){
            errorMessage = "payment_means_code must be 10 for In cash, 30 for Credit, 42 for Payment to bank account, 48 for Bank card, 1 for Instrument not defined";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerId()) || invoiceDTO.getSellerId().length() != 10){
            errorMessage = "seller_id_number is invalid";
            throw new RequestValidationException(errorMessage);
        }


        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerEName())){
            errorMessage = "seller_name_english is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerAName())){
            errorMessage = "seller_name_arabic is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerBuildingNo()) || invoiceDTO.getSellerBuildingNo().length() != 4){
            errorMessage = "seller_building_no is invalid";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerStreet())){
            errorMessage = "seller_street is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerDistrict())){
            errorMessage = "seller_district is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerAdditionalNo())){
            errorMessage = "seller_additional_no is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerPostalCode()) || invoiceDTO.getSellerPostalCode().length() != 5){
            errorMessage = "seller_postal_code is invalid";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerCity())){
            errorMessage = "seller_city is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerRegion())){
            errorMessage = "seller_region is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getSellerCountry()) || invoiceDTO.getSellerCountry().length() != 2){
            errorMessage = "seller_country is invalid and should be ISO format";
            throw new RequestValidationException(errorMessage);
        }

        if(!sellerNinTypes.contains(invoiceDTO.getSellerIdTyp())){
            errorMessage = "seller_id_type must be CRN for Commercial, MOM for MOMRA, MLS for MLSD, SAG for Sagia, OTH for Other";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getId())){
            errorMessage = "id is required";
            throw new RequestValidationException(errorMessage);
        }

        if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerEmail()) && !CommonUtils.isValidEmail(invoiceDTO.getBuyerEmail())) {
            errorMessage = "buyer_email is invalid";
            throw new RequestValidationException(errorMessage);
        }

        if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerMobile()) && invoiceDTO.getBuyerMobile().length()!=9) {
            errorMessage = "buyer_mobile should be length 9 and without prefix 0";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTotalAmount()))){
            errorMessage = "total_amount is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTotalAmount())){
            errorMessage = "total_amount should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getDiscount()))){
            errorMessage = "total_discount is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getDiscount())){
            errorMessage = "total_discount should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTaxableAmount()))){
            errorMessage = "total_exclusive_vat is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTaxableAmount())){
            errorMessage = "total_exclusive_vat should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTotalVAT()))){
            errorMessage = "total_vat is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTotalVAT())){
            errorMessage = "total_vat should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }


        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTaxInclusiveAmount()))){
            errorMessage = "total_inclusive_vat is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTaxInclusiveAmount())){
            errorMessage = "total_inclusive_vat should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }


        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getFxRate()))){
            errorMessage = "exchange_rate is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getFxRate())){
            errorMessage = "exchange_rate should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTaxSAR()))){
            errorMessage = "total_tax_sar is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTaxSAR())){
            errorMessage = "total_tax_sar should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getTotalSAR()))){
            errorMessage = "total_amount_sar is required";
            throw new RequestValidationException(errorMessage);
        }else if(!CommonUtils.isUpToTwoDecimal(invoiceDTO.getTotalSAR())){
            errorMessage = "total_amount_sar should be two decimal formate #.##";
            throw new RequestValidationException(errorMessage);
        }
        if(invoiceDTO.getSubType().startsWith("01")){
            Validation validation = validateClearanceInvoice(invoiceDTO);
            if(!validation.isValid()) {
                errorMessage = validation.getMessage();
                throw new RequestValidationException(errorMessage);
            }else{
                if(invoiceDTO.getInvoiceLines().size() <= 0){
                    errorMessage = "invoice_lines are required";
                    throw new RequestValidationException(errorMessage);
                }else{
                    validation = validateInvoiceLines(invoiceDTO.getInvoiceLines());
                    if(!validation.isValid()) {
                        errorMessage = validation.getMessage();
                        throw new RequestValidationException(errorMessage);
                    }
                }
            }
            if(invoiceDTO.getType().equalsIgnoreCase("381") || invoiceDTO.getType().equalsIgnoreCase("383")){
                validation = validateCreditAndDebitNote(invoiceDTO);
                if(!validation.isValid()) {
                    errorMessage = validation.getMessage();
                    throw new RequestValidationException(errorMessage);
                }
            }
        }

        if(invoiceDTO.getSubType().startsWith("02")){
            Validation validation = validateReportInvoice(invoiceDTO);
            if(!validation.isValid()) {
                errorMessage = validation.getMessage();
                throw new RequestValidationException(errorMessage);
            }else{
                if(invoiceDTO.getInvoiceLines().size() <= 0){
                    errorMessage = "invoice_lines are required";
                    throw new RequestValidationException(errorMessage);
                }else{
                    validation = validateInvoiceLines(invoiceDTO.getInvoiceLines());
                    if(!validation.isValid()) {
                        errorMessage = validation.getMessage();
                        throw new RequestValidationException(errorMessage);
                    }
                }
            }

            if(invoiceDTO.getType().equalsIgnoreCase("381") || invoiceDTO.getType().equalsIgnoreCase("383")){
                validation = validateCreditAndDebitNote(invoiceDTO);
                if(!validation.isValid()) {
                    errorMessage = validation.getMessage();
                    throw new RequestValidationException(errorMessage);
                }
            }
        }

        return true;
    }

    private Validation validateCreditAndDebitNote(InvoiceDTO invoiceDTO){
        Validation validation = new Validation();

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getOriginalInvoiceId())){
            validation.setValid(false);
            validation.setMessage("original_invoice_id is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getInvoiceNoteReason())){
            validation.setValid(false);
            validation.setMessage("invoice_note_reason is required");
            return validation;
        }

        return validation;
    }

    private Validation validateClearanceInvoice(InvoiceDTO invoiceDTO){
        Validation validation = new Validation();

        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getSupplyDate()))){
            validation.setValid(false);
            validation.setMessage("supply_date is required");
            return validation;
        }

/*
        if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceDTO.getSupplyEndDate()))){
            validation.setValid(false);
            validation.setMessage("supply_end_date is required");
            return validation;
        }
*/

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerIdNumber()) || invoiceDTO.getBuyerIdNumber().length() != 10){
            validation.setValid(false);
            validation.setMessage("buyer_id_number is invalid");
            return validation;
        }

        if(!buyerNinTypes.contains(invoiceDTO.getBuyerIdTyp())){
            validation.setValid(false);
            validation.setMessage("buyer_id_type is invalid");
            return validation;
        }

        if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerVatNumber()) && invoiceDTO.getBuyerVatNumber().length() != 15){
            validation.setValid(false);
            validation.setMessage("buyer_vat_number is invalid");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerEName())){
            validation.setValid(false);
            validation.setMessage("buyer_name_english is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerAName())){
            validation.setValid(false);
            validation.setMessage("buyer_name_arabic is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerBuildingNo()) || invoiceDTO.getBuyerBuildingNo().length() != 4){
            validation.setValid(false);
            validation.setMessage("buyer_building_no is invalid");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerStreet())){
            validation.setValid(false);
            validation.setMessage("buyer_street is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerDistrict())){
            validation.setValid(false);
            validation.setMessage("buyer_district is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerAdditionalNo())){
            validation.setValid(false);
            validation.setMessage("buyer_additional_no is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerPostalCode()) || invoiceDTO.getBuyerPostalCode().length() != 5){
            validation.setValid(false);
            validation.setMessage("buyer_postal_code is invalid");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerCity())){
            validation.setValid(false);
            validation.setMessage("buyer_city is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerRegion())){
            validation.setValid(false);
            validation.setMessage("buyer_region is required");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerCountry()) || invoiceDTO.getBuyerCountry().length() != 2){
            validation.setValid(false);
            validation.setMessage("buyer_country is invalid and should be ISO format");
            return validation;
        }

        return validation;
    }

    private Validation validateReportInvoice(InvoiceDTO invoiceDTO){
        Validation validation = new Validation();

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerPostalCode()) || invoiceDTO.getBuyerPostalCode().length() != 5){
            validation.setValid(false);
            validation.setMessage("buyer_postal_code is invalid");
            return validation;
        }
        if(!buyerNinTypes.contains(invoiceDTO.getBuyerIdTyp())){
            validation.setValid(false);
            validation.setMessage("buyer_id_type is invalid");
            return validation;
        }

        if(CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerCountry()) || invoiceDTO.getBuyerCountry().length() != 2){
            validation.setValid(false);
            validation.setMessage("buyer_country is invalid and should be ISO format");
            return validation;
        }

        return validation;
    }

    private Validation validateInvoiceLines(List<InvoiceLineDTO> invoiceLineDTOs){
        Validation validation = new Validation();

        for(InvoiceLineDTO invoiceLineDTO:invoiceLineDTOs){

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getLineId()))){
                validation.setValid(false);
                validation.setMessage("id is required for line item");
                break;
            }else if(invoiceLineDTOs.stream().filter(lineItem -> lineItem.getLineId() == invoiceLineDTO.getLineId()).count() > 1)
            {
                validation.setValid(false);
                validation.setMessage("id should be unique in line items");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(invoiceLineDTO.getName())){
                validation.setValid(false);
                validation.setMessage("item_name is required");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getLineId()))){
                validation.setValid(false);
                validation.setMessage("invoiced_quantity is required");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getNetPrice()))){
                validation.setValid(false);
                validation.setMessage("unit_price is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getNetPrice())){
                validation.setValid(false);
                validation.setMessage("unit_price should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getTotalAmount()))){
                validation.setValid(false);
                validation.setMessage("net_price is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getTotalAmount())){
                validation.setValid(false);
                validation.setMessage("net_price should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getTotalTaxableAmount()))){
                validation.setValid(false);
                validation.setMessage("taxable_amount is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getTotalTaxableAmount())){
                validation.setValid(false);
                validation.setMessage("taxable_amount should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getTaxRate()))){
                validation.setValid(false);
                validation.setMessage("tax_rate is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getTaxRate())){
                validation.setValid(false);
                validation.setMessage("tax_rate should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getTaxAmount()))){
                validation.setValid(false);
                validation.setMessage("tax_amount is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getTaxAmount())){
                validation.setValid(false);
                validation.setMessage("tax_amount should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(String.valueOf(invoiceLineDTO.getSubTotal()))){
                validation.setValid(false);
                validation.setMessage("sub_total_incl_vat is required");
                break;
            }else if(!CommonUtils.isUpToTwoDecimal(invoiceLineDTO.getSubTotal())){
                validation.setValid(false);
                validation.setMessage("sub_total_incl_vat should be two decimal formate #.##");
                break;
            }

            if(CommonUtils.doubleToBigDecimal(invoiceLineDTO.getTotalAmount()).compareTo(CommonUtils.doubleToBigDecimal(invoiceLineDTO.getQuantity() * invoiceLineDTO.getNetPrice())) != 0){
                validation.setValid(false);
                validation.setMessage("net_price  = invoiced_quantity * unit_price");
                break;
            }

            if((invoiceLineDTO.getDiscount() > 0) && (CommonUtils.doubleToBigDecimal(invoiceLineDTO.getTotalTaxableAmount()).compareTo(CommonUtils.doubleToBigDecimal(invoiceLineDTO.getTotalAmount() - invoiceLineDTO.getDiscount())) != 0)){
                validation.setValid(false);
                validation.setMessage("taxable_amount  = net_price - discount");
                break;
            }

            if(CommonUtils.isNullOrEmptyString(invoiceLineDTO.getItemTaxCategoryCode()) || !taxCategoriesCode.contains(invoiceLineDTO.getItemTaxCategoryCode()))
            {
                validation.setValid(false);
                validation.setMessage("tax_category_code  should be S for Standard, Z for Zero rated, E for Exempt from tax, O for Out of scope");
                break;
            }else{
                if(!invoiceLineDTO.getItemTaxCategoryCode().equalsIgnoreCase("S")){
                    if(invoiceLineDTO.getExemptionReasonCode().isEmpty() || invoiceLineDTO.getExemptionReasonText().isEmpty()){
                        validation.setValid(false);
                        validation.setMessage("exemption_reason_code && exemption_reason_text is required");
                        break;
                    }
                    if(invoiceLineDTO.getTaxRate()>0){
                        validation.setValid(false);
                        validation.setMessage("tax_rate should be zero for tax_category_code other than standard");
                        break;
                    }
                }

            }

        }

        return validation;
    }
}
