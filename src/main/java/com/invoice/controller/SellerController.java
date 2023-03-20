package com.invoice.controller;

import com.invoice.dto.CredentialDTO;
import com.invoice.dto.InvoiceDTOWrapper;
import com.invoice.dto.SellerDTO;
import com.invoice.exception.RequestValidationException;
import com.invoice.service.SellerService;
import com.invoice.util.CommonUtils;
import com.invoice.util.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping(produces = "application/json", value = "/seller")
    public ResponseEntity<SellerDTO> getSeller(@RequestHeader("username") String username, @RequestHeader("password") String password, @RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber, HttpServletRequest request)
    {
        String ip = HttpUtils.getRequestIP(request);
        log.info("request: getSeller() ip: {}",ip);
        CredentialDTO credentialDTO = validateCredential(username,password,vatNumber,egsSerialNumber,ip);
        SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial(credentialDTO);
        log.info("response: getSeller() SellerId: {}", ResponseEntity.ok().body(sellerDTO.getSellerNinNumber()));
        return ResponseEntity.ok(sellerDTO);
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
