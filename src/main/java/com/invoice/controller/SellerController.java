package com.invoice.controller;

import com.invoice.dto.SellerDTO;
import com.invoice.security.JwtTokenUtil;
import com.invoice.security.SecurityConstants;
import com.invoice.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping(produces = "application/json", value = "/seller")
    public ResponseEntity<SellerDTO> getSeller(HttpServletRequest request)
    {
        log.info("request: getSeller()");
        String token = jwtTokenUtil.getTokenFromAuthHeader(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        String vatNumber = jwtTokenUtil.getVatNumberFromToken(token);
        String egsSerialNumber = jwtTokenUtil.getEgsSerialNumberFromToken(token);
        SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial(vatNumber,egsSerialNumber);
        log.info("response: getSeller() SellerId: {}", ResponseEntity.ok().body(sellerDTO.getSellerNinNumber()));
        return ResponseEntity.ok(sellerDTO);
    }
}
