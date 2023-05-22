package com.invoice.controller;

import com.invoice.dto.ZakatRequestDTO;
import com.invoice.dto.zakat.ZakatClearanceResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceResponseDTO;
import com.invoice.dto.zakat.ZakatReportingResponseDTO;
import com.invoice.service.ZakatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ZakatController {

    private final ZakatService zakatService;

    @PostMapping(produces = "application/json", value = "/clearance")
    public ResponseEntity<ZakatClearanceResponseDTO> clearanceInvoice(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber)
    {

        log.info("request: clearanceInvoice() zakatRequest:{},",zakatRequestDTO);
        ZakatClearanceResponseDTO zakatClearanceResponseDTO = zakatService.clearance(zakatRequestDTO);

        log.info("response: clearanceInvoice(): {}", ResponseEntity.ok().body(zakatClearanceResponseDTO));
        return ResponseEntity.ok().body(zakatClearanceResponseDTO);
    }

    @PostMapping(produces = "application/json", value = "/reporting")
    public ResponseEntity<ZakatReportingResponseDTO> reportInvoice(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber)
    {

        log.info("request: reportInvoice() zakatRequest:{},",zakatRequestDTO);
        ZakatReportingResponseDTO zakatReportingResponseDTO = zakatService.reporting(zakatRequestDTO);

        log.info("response: reportInvoice(): {}", ResponseEntity.ok().body(zakatReportingResponseDTO));
        return ResponseEntity.ok().body(zakatReportingResponseDTO);
    }

    @PostMapping(produces = "application/json", value = "/compliance")
    public ResponseEntity<ZakatComplianceResponseDTO> compliance(@Valid @RequestBody ZakatRequestDTO zakatRequestDTO,@RequestHeader("vat_number") String vatNumber, @RequestHeader("egs_serial_no") String egsSerialNumber)
    {

        log.info("request: complianceInvoice() zakatRequest:{},",zakatRequestDTO);
        ZakatComplianceResponseDTO zakatComplianceResponseDTO = zakatService.compliance(zakatRequestDTO);

        log.info("response: complianceInvoice(): {}", ResponseEntity.ok().body(zakatComplianceResponseDTO));
        return ResponseEntity.ok().body(zakatComplianceResponseDTO);
    }
}
