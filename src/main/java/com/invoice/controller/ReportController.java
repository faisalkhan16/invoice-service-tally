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
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final InvoiceService invoiceService;

    private final SellerService sellerService;
    @GetMapping(produces = "application/json", value = "/pending")
    public ResponseEntity<PendingReportResponse> pendingReports(@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: pendingReports() ip: {}",ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        List<InvoiceDTO> invoiceDTOList = invoiceService.getPendingInvoice();
        PendingReportResponse pendingReportResponse = new PendingReportResponse();
        pendingReportResponse.setInvoiceDTOList(invoiceDTOList);
        log.info("response: pendingReports():", ResponseEntity.ok().body(pendingReportResponse));
        return ResponseEntity.ok().body(pendingReportResponse);
    }


    @GetMapping(produces = "application/json", value = "/report")
    public ResponseEntity<InvoiceResponse> getInvoice(@RequestParam String invoiceNumber,@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: getInvoice() ip: {} {}", invoiceNumber,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        InvoiceResponse invoiceResponse = invoiceService.getInvoiceResponse(invoiceNumber);

        log.info("response: getInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @PostMapping(produces = "application/json", value = "/email")
    public ResponseEntity<Void> email(@RequestParam String invoiceNumber, @RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: email() invoiceNumber: {} ip: {}",invoiceNumber,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }
        invoiceService.sendEmail(invoiceNumber);
        log.info("response: email() invoiceNumber: {}",invoiceNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping(produces = "application/json", value = "/archive")
    public ResponseEntity<Void> archive(@RequestParam String invoiceNumber,@RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: archive() invoiceNumber: {} ip: {}",invoiceNumber,ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }
        invoiceService.archiveOnCloud(invoiceNumber);
        log.info("response: archive() invoiceNumber: {}",invoiceNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping(produces = "application/json", value = "/health")
    public ResponseEntity<String> invoiceReports()
    {
        return ResponseEntity.ok().body("up");
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
