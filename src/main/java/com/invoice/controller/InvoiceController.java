package com.invoice.controller;

import com.invoice.dto.*;
import com.invoice.exception.RequestValidationException;
import com.invoice.security.JwtTokenUtil;
import com.invoice.security.SecurityConstants;
import com.invoice.service.InvoiceService;
import com.invoice.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InvoiceController
{

    private final InvoiceService invoiceService;

    private final JwtTokenUtil jwtTokenUtil;
    @PostMapping(produces = "application/json", value = "/invoice")
    public ResponseEntity<InvoiceResponse> generateInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        log.info("request: generateInvoice() {}", invoiceDTOWrapper);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        invoiceDTO.setSellerVatNumber(vatNumber);
        invoiceDTO.setSerialNumber(egsSerialNumber);
        InvoiceResponse invoiceResponse = invoiceService.generateInvoice(invoiceDTO);

        log.info("response: generateInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @PostMapping(produces = "application/json", value = "/report")
    public ResponseEntity<InvoiceResponse> generateReportInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber,HttpServletRequest request)
    {
        log.info("request: generateReportInvoice(): {}", invoiceDTOWrapper);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        invoiceDTO.setSellerVatNumber(vatNumber);
        invoiceDTO.setSerialNumber(egsSerialNumber);
        InvoiceResponse invoiceResponse = invoiceService.generateReportInvoice(invoiceDTO);

        log.info("response: generateReportInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/issuesimplified")
    public ResponseEntity<InvoiceReportResponse> issueSimplified()
    {
        log.info("request: reportInvoice()");
        InvoiceReportResponse invoiceResponse = invoiceService.sendSimplifiedInvoiceToZatca();
        log.info("response: issueSimplified():", ResponseEntity.ok().body(invoiceResponse));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/issuestandard")
    public ResponseEntity<InvoiceReportResponse> issueStandard()
    {
        log.info("request: reportInvoice()");

        InvoiceReportResponse invoiceResponse = invoiceService.sendStandardInvoiceToZatca();

        log.info("response: issueStandard():", ResponseEntity.ok().body(invoiceResponse));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/invoice")
    public ResponseEntity<InvoiceDTOWrapper> getInvoice(@RequestParam String invoiceId)
    {
        log.info("request: getInvoice() invoiceId: {}", invoiceId);

        InvoiceDTOWrapper invoiceDTOWrapper = invoiceService.getInvoiceByInvoiceID(invoiceId);

        log.info("response: getInvoice(): {}", ResponseEntity.ok().body(invoiceDTOWrapper.getInvoiceDTO().getId()));
        return ResponseEntity.ok().body(invoiceDTOWrapper);
    }

    @PostMapping(produces = "application/json", value = "/retry")
    public ResponseEntity<InvoiceResponse> retryInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber,HttpServletRequest request)
    {
        log.info("request: retryInvoice() {}", invoiceDTOWrapper);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        InvoiceResponse invoiceResponse = invoiceService.retryInvoice(invoiceDTO);

        log.info("response: retryInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    private boolean validateCredential(String VAT_NUMBER, String EGS_SERIAL_NUMBER,String vatNumber, String egsSerialNumber){

        log.info("zakatController: validateCredential() vatNumber: {} egsSerialNumber: {}",vatNumber,egsSerialNumber);

        if(CommonUtils.isNullOrEmptyString(VAT_NUMBER) || CommonUtils.isNullOrEmptyString(EGS_SERIAL_NUMBER)){
            throw new RequestValidationException("vat_number and egs_serial_no in header param is required");
        }

        if(CommonUtils.isNullOrEmptyString(vatNumber) || CommonUtils.isNullOrEmptyString(egsSerialNumber) || !VAT_NUMBER.equalsIgnoreCase(vatNumber)|| !EGS_SERIAL_NUMBER.equalsIgnoreCase(egsSerialNumber)){
            throw new RequestValidationException("Invalid User token");
        }

        return true;
    }

}
