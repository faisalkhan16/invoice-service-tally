package com.invoice.service;

import com.invoice.configuration.WebClientConfig;
import com.invoice.dto.CredentialDTO;
import com.invoice.dto.ImageDTO;
import com.invoice.dto.SellerDTO;
import com.invoice.exception.SellerException;
import com.invoice.exception.SellerNotFoundException;
import com.invoice.exception.ServiceDownException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SellerServiceApplication {

    @Autowired
    @Qualifier("webClientSeller")
    private WebClient webClient;

    @Value("${SELLER_GET_API}")
    private String SELLER_GET_API;

    @Value("${SELLER_LOGIN_API}")
    private String SELLER_LOGIN_API;

    @Value("${SELLER_IMAGE_GET_API}")
    private String SELLER_IMAGE_GET_API;

    public SellerDTO getSellerByVatAndSerial(String vatNumber, String serialNumber){

        SellerDTO sellerDTO;

        log.info("SellerServiceApplication getSellerByVatAndSerial VAT: {} Serial: {} ", vatNumber,serialNumber);

        try {

            sellerDTO = webClient
                    .get()
                    .uri(SELLER_GET_API, uriBuilder -> uriBuilder.queryParam("vat_number", vatNumber).queryParam("egs_serial_no",serialNumber).build())
                    .retrieve()
                    .bodyToMono(SellerDTO.class).block();

            log.info("SellerServiceApplication getSellerByVatAndSerial Response ", sellerDTO);

        } catch(WebClientRequestException ex){

            log.error("Exception in SellerServiceApplication getSellerByVatAndSerial VatNubmer and SerialNumber:{} {} {} "+ vatNumber,serialNumber,ex.getStackTrace());
            throw new ServiceDownException("Seller Service Down");

        }catch(WebClientResponseException ex){
            log.error("Exception in SellerServiceApplication getSellerByVatAndSerial VatNubmer and SerialNumber:{} {} {} "+ vatNumber,serialNumber,ex.getStackTrace());
            throw new SellerException(ex.getResponseBodyAsString());
        }

        return sellerDTO;
    }


    public ImageDTO getImageByVatAndSerial(String vatNumber, String serialNumber){

        ImageDTO imageDTO = new ImageDTO();

        log.info("SellerServiceApplication getImageByVatAndSerial VAT: {} Serial: {} ", vatNumber,serialNumber);

        try {

            imageDTO = webClient
                    .post()
                    .uri(SELLER_IMAGE_GET_API, uriBuilder -> uriBuilder.queryParam("vat_number", vatNumber).queryParam("egs_serial_no",serialNumber).build())
                    .retrieve()
                    .bodyToMono(ImageDTO.class).block();

            log.info("SellerServiceApplication getImageByVatAndSerial Response ", imageDTO);
            return imageDTO;

        } catch(WebClientRequestException ex){

            log.error("Exception in SellerServiceApplication getImageByVatAndSerial VatNubmer and SerialNumber:{} {} {} "+ vatNumber,serialNumber,ex.getStackTrace());
            throw new ServiceDownException("Seller Service Down");

        } catch(WebClientResponseException ex){
            log.error("Exception in SellerServiceApplication getImageByVatAndSerial VatNubmer and SerialNumber:{} {} {} "+ vatNumber,serialNumber,ex.getStackTrace());
            return imageDTO;
        }

    }


    public boolean validateCredential(CredentialDTO credentialDTO) {
        {

            boolean isValidCredential = false;

            log.info("SellerServiceApplication validateCredential username: {} ipAddress: {} vatNumber: {}  egsSerialNumber: {}", credentialDTO.getUsername(),credentialDTO.getIpAddress(),credentialDTO.getSellerVatNumber(),credentialDTO.getSerialNo());

            try {

                isValidCredential =  webClient
                        .post()
                        .uri(SELLER_LOGIN_API)
                        .body(BodyInserters.fromValue(credentialDTO))
                        .retrieve()
                        .bodyToMono(Boolean.class).block();

                log.info("SellerServiceApplication validateCredential Response isValidCredential: {}", isValidCredential);

            }catch(WebClientRequestException ex){

                log.error("Exception in SellerServiceApplication validateCredential  username: {} ipAddress: {} vatNumber: {}  egsSerialNumber: {}", credentialDTO.getUsername(),credentialDTO.getIpAddress(),credentialDTO.getSellerVatNumber(),credentialDTO.getSerialNo(),ex.getStackTrace());
                throw new ServiceDownException("Seller Service Down");

            } catch(WebClientResponseException ex){
                log.error("Exception in SellerServiceApplication validateCredential  username: {} ipAddress: {} vatNumber: {}  egsSerialNumber: {}", credentialDTO.getUsername(),credentialDTO.getIpAddress(),credentialDTO.getSellerVatNumber(),credentialDTO.getSerialNo(),ex.getStackTrace());
                throw new SellerException(ex.getResponseBodyAsString());
            }
            return isValidCredential;

        }
    }
}
