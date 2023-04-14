package com.invoice.exception;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.dto.zakat.ZakatClearanceResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceCISDResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceResponseDTO;
import com.invoice.dto.zakat.ZakatReportingResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class InvoiceExceptionHandler {

    @ExceptionHandler(ZakatException.class)
    public ResponseEntity<?> zakatService(ZakatException exception, WebRequest request) {

        String[] errors = exception.getMessage().split(java.util.regex.Pattern.quote("$"));
        int arrayLength = errors.length;
        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("ERROR");
        errorDetails.setHttpStatus(errors[2]);
        if(arrayLength>3) {
            errorDetails.setMessage(errors[3]);
        }
        if(arrayLength>4) {
            String validationResult = errors[4];
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                if("1".equalsIgnoreCase(errors[0])){
                    errorDetails.setInvoiceUUID(errors[1]);
                    ZakatReportingResponseDTO zakatReportingResponseDTO = objectMapper.readValue(validationResult, ZakatReportingResponseDTO.class);
                    errorDetails.setValidationResult(zakatReportingResponseDTO.getValidationResults());
                }
                if("2".equalsIgnoreCase(errors[0])){
                    errorDetails.setInvoiceUUID(errors[1]);
                    ZakatClearanceResponseDTO zakatClearanceResponseDTO = objectMapper.readValue(validationResult, ZakatClearanceResponseDTO.class);
                    errorDetails.setValidationResult(zakatClearanceResponseDTO.getValidationResults());
                }
                if("3".equalsIgnoreCase(errors[0])){
                    errorDetails.setCcr(errors[1]);
                    ZakatComplianceCISDResponseDTO zakatComplianceCISDResponseDTO = objectMapper.readValue(validationResult, ZakatComplianceCISDResponseDTO.class);
                    errorDetails.setValidationResult(zakatComplianceCISDResponseDTO.getValidationResults());
                }
                if("4".equalsIgnoreCase(errors[0])){
                    errorDetails.setInvoiceUUID(errors[1]);
                    ZakatComplianceResponseDTO zakatComplianceResponseDTO = objectMapper.readValue(validationResult, ZakatComplianceResponseDTO.class);
                    errorDetails.setValidationResult(zakatComplianceResponseDTO.getValidationResults());
                }
            } catch(JsonProcessingException e){
                log.error("Exception in InvoiceExceptionHandler zakatServiceException {}",e.getMessage());
            }
        }
        errorDetails.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }


    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<?> requestParametersValidation(RequestValidationException exception, WebRequest request) {

        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("VALIDATION ERROR");
        errorDetails.setHttpStatus(HttpStatus.BAD_REQUEST.toString());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setTimestamp(LocalDateTime.now());

        log.info("requestParametersValidation {}",errorDetails);

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }

    @ExceptionHandler(SellerException.class)
    public ResponseEntity<?> sellerException(SellerException exception, WebRequest request) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CustomError errorDetails = null;
        try {
            errorDetails = objectMapper.readValue(exception.getMessage(), CustomError.class);

        } catch(JsonProcessingException e){
            log.error("Exception in InvoiceExceptionHandler sellerException {}",e.getMessage());
        }

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<?> sellerNotFound(SellerNotFoundException exception, WebRequest request) {

        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("ERROR");
        errorDetails.setHttpStatus(HttpStatus.NOT_FOUND.toString());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }
    @ExceptionHandler(ServiceDownException.class)
    public ResponseEntity<?> serviceDown(ServiceDownException exception, WebRequest request) {

        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("ERROR");
        errorDetails.setHttpStatus(HttpStatus.NOT_FOUND.toString());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }

    @ExceptionHandler(SystemExpireException.class)
    public ResponseEntity<?> systemExpire(SystemExpireException exception, WebRequest request) {

        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("ERROR");
        errorDetails.setHttpStatus(HttpStatus.NOT_FOUND.toString());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorDetails,HttpStatus.OK);
    }

    @ExceptionHandler(BuyerEmailNotFoundException.class)
    public ResponseEntity<?> buyerEmailNotFound(BuyerEmailNotFoundException exception, WebRequest request) {

        CustomError errorDetails = new CustomError();
        errorDetails.setStatus("ERROR");
        errorDetails.setHttpStatus(HttpStatus.NOT_FOUND.toString());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorDetails,HttpStatus.BAD_REQUEST);
    }
}
