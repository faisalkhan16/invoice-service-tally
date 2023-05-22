package com.invoice.controller;

import com.invoice.dto.*;
import com.invoice.exception.RequestValidationException;
import com.invoice.security.JwtTokenUtil;
import com.invoice.security.SecurityConstants;
import com.invoice.service.InvoiceService;
import com.invoice.util.CommonUtils;
import com.invoice.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final InvoiceService invoiceService;

    private final JwtTokenUtil jwtTokenUtil;
    @GetMapping(produces = "application/json", value = "/pending")
    public ResponseEntity<PendingReportResponse> pendingReports(@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        log.info("request: pendingReports()");

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        List<InvoiceDTO> invoiceDTOList = invoiceService.getPendingInvoice();
        PendingReportResponse pendingReportResponse = new PendingReportResponse();
        pendingReportResponse.setInvoiceDTOList(invoiceDTOList);
        log.info("response: pendingReports():", ResponseEntity.ok().body(pendingReportResponse));
        return ResponseEntity.ok().body(pendingReportResponse);
    }


    @GetMapping(produces = "application/json", value = "/report")
    public ResponseEntity<InvoiceResponse> report(@RequestParam String invoiceNumber,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        log.info("request: report() invoiceNumber: {}", invoiceNumber);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        InvoiceResponse invoiceResponse = invoiceService.getInvoiceResponse(invoiceNumber);

        log.info("response: getInvoice(): {}", ResponseEntity.ok().body(invoiceResponse.getSeqId()));
        return ResponseEntity.ok().body(invoiceResponse);
    }

    @PostMapping(produces = "application/json", value = "/email")
    public ResponseEntity<Void> email(@RequestParam String invoiceNumber,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        log.info("request: email() invoiceNumber: {}",invoiceNumber);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        invoiceService.sendEmail(invoiceNumber);
        log.info("response: email() invoiceNumber: {}",invoiceNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping(produces = "application/json", value = "/archive")
    public ResponseEntity<Void> archive(@RequestParam String invoiceNumber,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        log.info("request: archive() invoiceNumber: {}",invoiceNumber);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        invoiceService.archiveOnCloud(invoiceNumber);
        log.info("response: archive() invoiceNumber: {}",invoiceNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping(produces = "application/json", value = "/embedxml")
    public ResponseEntity<String> embedXML(@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, @RequestParam String invoiceNumber, @RequestParam MultipartFile file, HttpServletRequest request)
    {
        log.info("request: embedXML() invoiceNumber: {}",invoiceNumber);

        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumberToken = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumberToken = jwtTokenUtil.getEgsSerialNumberFromToken(token);

        validateCredential(vatNumber,egsSerialNumber,vatNumberToken,egsSerialNumberToken);

        String pdf = invoiceService.embedXML(invoiceNumber,file);
        log.info("response: embedXML() invoiceNumber: {}",invoiceNumber);
        return  ResponseEntity.ok().body(pdf);
    }

    @GetMapping(produces = "application/json", value = "/health")
    public ResponseEntity<String> invoiceReports()
    {
        return ResponseEntity.ok().body("up");
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
