package com.invoice.validator;

import com.invoice.dto.ZakatRequestDTO;
import com.invoice.exception.RequestValidationException;
import com.invoice.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class ZakatRequestValidator implements ConstraintValidator<ZakatRequestConstraint, ZakatRequestDTO> {
    @Override
    public void initialize(ZakatRequestConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(ZakatRequestDTO zakatRequestDTO, ConstraintValidatorContext context) {
        String errorMessage = "";

        log.info("Zakat Request: {}",zakatRequestDTO);

        if(CommonUtils.isNullOrEmptyString(zakatRequestDTO.getInvoice())){
            errorMessage = "invoice_hash is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(zakatRequestDTO.getInvoiceHash())){
            errorMessage = "uuid is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(zakatRequestDTO.getUuid())){
            errorMessage = "invoice is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(zakatRequestDTO.getSecretKey())){
            errorMessage = "certificate is required";
            throw new RequestValidationException(errorMessage);
        }

        if(CommonUtils.isNullOrEmptyString(zakatRequestDTO.getCertificate())){
            errorMessage = "secret_key is required";
            throw new RequestValidationException(errorMessage);
        }
        return true;
    }
}
