package com.invoice.service;

import com.invoice.configuration.WebClientConfig;
import com.invoice.dto.zakat.*;
import com.invoice.exception.ZakatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZakatServiceApplication {

    @Autowired
    @Qualifier("webClientZatca")
    private WebClient webClient;

    @Value("${ZAKAT_REPORTING_API}")
    private String reportingAPI;

    @Value("${ZAKAT_CLEARANCE_API}")
    private String clearanceAPI;

    @Value("${ZAKAT_COMPLIANCE_API}")
    private String complianceAPI;

    public ZakatReportingResponseDTO reporting(ZakatApplicationRequestDTO zakatInvoiceRequest, String certificate, String secretKey) throws WebClientResponseException{

        log.info("ZakatServiceApplication Reporting Request {}", zakatInvoiceRequest);


            HttpHeaders headers = new HttpHeaders();
            headers.add("accept", "application/json");
            headers.add("accept-language", "en");
            headers.add("Clearance-Status", "0");
            headers.add("Accept-Version", "V2");
            headers.add("Authorization", getAuthorization(certificate, secretKey));
            headers.add("Content-Type", "application/json");


           ZakatReportingResponseDTO zakatReportingInvoiceResponse = webClient
                    .post()
                    .uri(reportingAPI)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(Mono.just(zakatInvoiceRequest), ZakatApplicationRequestDTO.class)
                    .retrieve()
                    .bodyToMono(ZakatReportingResponseDTO.class).block();

            log.info("ZakatServiceApplication Reporting Response {}", zakatReportingInvoiceResponse);

        return zakatReportingInvoiceResponse;
    }

    public ZakatClearanceResponseDTO clearance(ZakatApplicationRequestDTO zakatInvoiceRequest, String certificate, String secretKey) throws WebClientResponseException {

        log.info("ZakatServiceApplication Clearance Request {}", zakatInvoiceRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.add("accept", "application/json");
            headers.add("accept-language", "en");
            headers.add("Clearance-Status", "1");
            headers.add("Accept-Version", "V2");
            headers.add("Authorization", getAuthorization(certificate, secretKey));
            headers.add("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);


        ZakatClearanceResponseDTO zakatClearanceInvoiceResponse = webClient
                    .post()
                    .uri(clearanceAPI)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(Mono.just(zakatInvoiceRequest), ZakatApplicationRequestDTO.class)
                    .retrieve()
                    .bodyToMono(ZakatClearanceResponseDTO.class).block();

            log.info("ZakatServiceApplication Clearance Response {}", zakatClearanceInvoiceResponse);
            return zakatClearanceInvoiceResponse;
    }

    public ZakatComplianceResponseDTO compliance(ZakatApplicationRequestDTO zakatInvoiceRequest, String cert, String secretKey) throws WebClientResponseException{

        log.info("ZakatServiceApplication Compliance Request {}", zakatInvoiceRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.add("accept", "application/json");
            headers.add("Accept-Language", "en");
            headers.add("Accept-Version", "V2");
            headers.add("Authorization", getAuthorization(cert, secretKey));
            headers.add("Content-Type", "application/json");

            ZakatComplianceResponseDTO zakatComplianceInvoiceResponse = webClient
                    .post()
                    .uri(complianceAPI)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(Mono.just(zakatInvoiceRequest), ZakatApplicationRequestDTO.class)
                    .retrieve()
                    .bodyToMono(ZakatComplianceResponseDTO.class).block();

            log.info("ZakatServiceApplication Compliance Response {}", zakatComplianceInvoiceResponse);

            return  zakatComplianceInvoiceResponse;

    }

    private String getAuthorization(String certificate, String secretKey)
    {
        StringBuffer auth = new StringBuffer().append("Basic ");
        auth.append(Base64.getEncoder().encodeToString(new StringBuffer().append(Base64.getEncoder().encodeToString(certificate.getBytes(StandardCharsets.UTF_8))).append(":").append(secretKey).toString().getBytes(StandardCharsets.UTF_8))
        );
        return auth.toString();
    }

}