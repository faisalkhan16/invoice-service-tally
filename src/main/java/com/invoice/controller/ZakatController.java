package com.invoice.controller;

import com.invoice.dto.CredentialDTO;
import com.invoice.dto.ZakatRequestDTO;
import com.invoice.dto.zakat.ZakatClearanceResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceResponseDTO;
import com.invoice.dto.zakat.ZakatReportingResponseDTO;
import com.invoice.exception.RequestValidationException;
import com.invoice.service.ZakatService;
import com.invoice.util.CommonUtils;
import com.invoice.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ZakatController {

    private final ZakatService zakatService;

    @PostMapping(produces = "application/json", value = "/clearance")
    public ResponseEntity<ZakatClearanceResponseDTO> clearanceInvoice(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO, @RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {

        String ip = HttpUtils.getRequestIP(request);
        log.info("request: clearanceInvoice() zakatRequest: Username: {}, ip: {},",username,ip, zakatRequestDTO);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);
        ZakatClearanceResponseDTO zakatClearanceResponseDTO = zakatService.clearance(credentialDTO,zakatRequestDTO);

        log.info("response: clearanceInvoice(): {}", ResponseEntity.ok().body(zakatClearanceResponseDTO));
        return ResponseEntity.ok().body(zakatClearanceResponseDTO);
    }

    @PostMapping(produces = "application/json", value = "/reporting")
    public ResponseEntity<ZakatReportingResponseDTO> reportInvoice(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO, @RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {

        String ip = HttpUtils.getRequestIP(request);
        log.info("request: reportInvoice() zakatRequest: Username: {}, ip: {},",username,ip, zakatRequestDTO);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);
        ZakatReportingResponseDTO zakatReportingResponseDTO = zakatService.reporting(credentialDTO,zakatRequestDTO);

        log.info("response: reportInvoice(): {}", ResponseEntity.ok().body(zakatReportingResponseDTO));
        return ResponseEntity.ok().body(zakatReportingResponseDTO);
    }

    @PostMapping(produces = "application/json", value = "/compliance")
    public ResponseEntity<ZakatComplianceResponseDTO> compliance(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO, @RequestHeader("username") String username, @RequestHeader("password") String password,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {

        String ip = HttpUtils.getRequestIP(request);
        log.info("request: complianceInvoice() zakatRequest: Username: {}, ip: {},",username,ip, zakatRequestDTO);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);
        ZakatComplianceResponseDTO zakatComplianceResponseDTO = zakatService.compliance(credentialDTO,zakatRequestDTO);

        log.info("response: complianceInvoice(): {}", ResponseEntity.ok().body(zakatComplianceResponseDTO));
        return ResponseEntity.ok().body(zakatComplianceResponseDTO);
    }

    private CredentialDTO validateCredential(String username, String password,String vatNumber, String egsSerialNumber,String ipAddress){

        log.info("zakatController: validateCredential() username: {} IpAddres: {} vatNumber: {} egsSerialNumber: {}",username,ipAddress,vatNumber,egsSerialNumber);

        if(CommonUtils.isNullOrEmptyString(username) || CommonUtils.isNullOrEmptyString(password)){
            throw new RequestValidationException("Username and Password is required");
        }

        if(CommonUtils.isNullOrEmptyString(vatNumber) || CommonUtils.isNullOrEmptyString(egsSerialNumber)){
            throw new RequestValidationException("VATNumber and EGSSerialNo is required");
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
