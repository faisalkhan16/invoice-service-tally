package com.invoice.controller;

import com.invoice.dto.*;
import com.invoice.exception.RequestValidationException;
import com.invoice.exception.SellerNotFoundException;
import com.invoice.service.InvoiceService;
import com.invoice.service.SellerService;
import com.invoice.util.CommonUtils;
import com.invoice.util.HttpUtils;
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

    private final SellerService sellerService;

    @PostMapping(produces = "application/json", value = "/invoice")
    public ResponseEntity<InvoiceResponse> generateInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: generateInvoice() ip: {} {}", invoiceDTOWrapper,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        invoiceDTO.setSellerVatNumber(credentialDTO.getSellerVatNumber());
        invoiceDTO.setSerialNumber(credentialDTO.getSerialNo());
        InvoiceResponse invoiceResponse = invoiceService.generateInvoice(invoiceDTO);

        log.info("response: generateInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @PostMapping(produces = "application/json", value = "/report")
    public ResponseEntity<InvoiceResponse> generateReportInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: generateReportInvoice() ip: {} {}", invoiceDTOWrapper,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        invoiceDTO.setSellerVatNumber(credentialDTO.getSellerVatNumber());
        invoiceDTO.setSerialNumber(credentialDTO.getSerialNo());
        InvoiceResponse invoiceResponse = invoiceService.generateReportInvoice(invoiceDTO);

        log.info("response: generateReportInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/issuesimplified")
    public ResponseEntity<InvoiceReportResponse> issueSimplified(@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: reportInvoice() ip: {}",ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceReportResponse invoiceResponse = invoiceService.sendSimplifiedInvoiceToZatca();
        log.info("response: issueSimplified():", ResponseEntity.ok().body(invoiceResponse));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/issuestandard")
    public ResponseEntity<InvoiceReportResponse> issueStandard(@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: reportInvoice() ip: {}",ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceReportResponse invoiceResponse = invoiceService.sendStandardInvoiceToZatca();

        log.info("response: issueStandard():", ResponseEntity.ok().body(invoiceResponse));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @GetMapping(produces = "application/json", value = "/invoice")
    public ResponseEntity<InvoiceDTOWrapper> getInvoice(@RequestParam String invoiceId, @RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: getInvoice() ip: {} {}", invoiceId,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceDTOWrapper invoiceDTOWrapper = invoiceService.getInvoiceByInvoiceID(invoiceId);

        log.info("response: getInvoice(): {}", ResponseEntity.ok().body(invoiceDTOWrapper.getInvoiceDTO().getId()));
        return ResponseEntity.ok().body(invoiceDTOWrapper);
    }

    @PostMapping(produces = "application/json", value = "/retry")
    public ResponseEntity<InvoiceResponse> retryInvoice(@Valid @RequestBody InvoiceDTOWrapper invoiceDTOWrapper,@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: retryInvoice() ip: {} {}", invoiceDTOWrapper,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceDTO invoiceDTO = invoiceDTOWrapper.getInvoiceDTO();
        InvoiceResponse invoiceResponse = invoiceService.retryInvoice(credentialDTO,invoiceDTO);

        log.info("response: retryInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    private CredentialDTO validateCredential(String username, String password, String vatNumber, String egsSerialNumber, String ipAddress){

        log.info("invoiceController: validateCredential() username: {} IpAddres: {} vat_number: {} egs_serial_no: {}",username,ipAddress,vatNumber,egsSerialNumber);

        if(CommonUtils.isNullOrEmptyString(username) || CommonUtils.isNullOrEmptyString(password)){
            throw new RequestValidationException("username and password is required");
        }

        if(CommonUtils.isNullOrEmptyString(vatNumber) || CommonUtils.isNullOrEmptyString(egsSerialNumber)){
            throw new RequestValidationException("vat_number and egs_serial_no is required");
        }

        CredentialDTO credentialDTO = new CredentialDTO();
        credentialDTO.setUsername(username);
        credentialDTO.setPassword(password);
        credentialDTO.setSellerVatNumber(vatNumber);
        credentialDTO.setSerialNo(egsSerialNumber);
        credentialDTO.setIpAddress(ipAddress);

        return credentialDTO;
    }

}
