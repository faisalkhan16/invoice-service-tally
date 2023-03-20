package com.invoice.service;

import com.invoice.dto.CredentialDTO;
import com.invoice.dto.ImageDTO;
import com.invoice.dto.SellerDTO;
import com.invoice.exception.SellerNotFoundException;
import com.invoice.mapper.MapSeller;
import com.invoice.model.Seller;
import com.invoice.repository.SellerRepositoryImpl;
import com.invoice.util.CommonUtils;
import com.invoice.util.Constants;
import com.invoice.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;


@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {


    private final SellerRepositoryImpl sellerRepository;
    private  final MapSeller mapSeller;
    private final SellerServiceApplication sellerServiceApplication;

    public SellerDTO getSellerByVatAndSerial(CredentialDTO credentialDTO){

        log.info("Seller Service getSellerByVatAndSerial()");

        if(!validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        SellerDTO sellerDTO = sellerServiceApplication.getSellerByVatAndSerial(credentialDTO.getSellerVatNumber(), credentialDTO.getSerialNo());

        log.info("Seller Service getSellerByVatAndSerial Response {}", sellerDTO);

        ImageDTO imageDTO = sellerServiceApplication.getImageByVatAndSerial(credentialDTO.getSellerVatNumber(), credentialDTO.getSerialNo());

        log.info("Seller Service getSellerByVatAndSerial Response {}", imageDTO);

        if (null != imageDTO && !CommonUtils.isNullOrEmptyString(imageDTO.getImage())) {
            sellerDTO.setImage(imageDTO.getImage());
        }

        sellerDTO = createSeller(sellerDTO);

        return sellerDTO;
    }

    public SellerDTO getSellerByVatAndSerial(){

        SellerDTO sellerDTO = null;

        log.info("SellerService getSellerByVatAndSerial from DB");

        Seller seller = sellerRepository.getSellerByVatAndSerial();

        sellerDTO = mapSeller.sellerToSellerDTO(seller);

        log.info("SellerService getSellerByVatAndSerial from DB Response {}", sellerDTO);

        return sellerDTO;
    }

    public SellerDTO createSeller(SellerDTO sellerDTO){

        Seller seller = mapSeller.sellerDTOToSeller(sellerDTO);
        log.info("SellerService createSeller VAT: {} Serial: {} ", seller.getSellerVatNumber(),seller.getSerialNo());

            int isSellerExists =  sellerRepository.checkIsSellerExists(seller.getSellerVatNumber(),seller.getSerialNo());

            if(isSellerExists > 0){
                updateSeller(seller);
            }else{
                sellerRepository.createSeller(seller);
            }

            createSellerInfo(seller);

            log.info("SellerService createSeller Response {}", seller);

        return sellerDTO;
    }

    public Seller createSellerInfo(Seller seller){

        log.info("SellerService createSellerInfo VAT: {} Serial: {} ", seller.getSellerVatNumber(),seller.getSerialNo());

            int isSellerInfoExists =  sellerRepository.checkIsSellerInfoExists(seller.getSellerVatNumber(),seller.getSerialNo());

            if(isSellerInfoExists > 0){
                updateSellerInfo(seller);
            }else{
                sellerRepository.createSellerInfo(seller);
            }

            Constants.SELLER_EXPIRE_DATE = seller.getExpiryDate();

            log.info("SellerService createSellerInfo Response {}", seller);

        return seller;
    }

    private Seller updateSeller(Seller seller){

        log.info("SellerService updateSeller VAT: {} Serial: {} ", seller.getSellerVatNumber(),seller.getSerialNo());

        try {


            sellerRepository.update(seller);
            log.info("SellerService updateSeller Response ", seller);


        } catch(Exception ex){
            log.error("Exception in SellerService updateSeller VatNubmer and SerialNumber:{} {} {} "+ seller.getSellerVatNumber(),seller.getSerialNo(),ex.getStackTrace());
        }

        return seller;
    }

    private Seller updateSellerInfo(Seller seller){

        log.info("SellerService updateSellerInfo VAT: {} Serial: {} ", seller.getSellerVatNumber(),seller.getSerialNo());

        try {


            sellerRepository.updateSellerInfo(seller);
            log.info("SellerService updateSellerInfo Response {}", seller);


        } catch(Exception ex){
            log.error("Exception in SellerService updateSellerInfo VatNubmer and SerialNumber:{} {} {} "+ seller.getSellerVatNumber(),seller.getSerialNo(),ex.getStackTrace());
        }

        return seller;
    }

    public boolean validateCredential(CredentialDTO credentialDTO) {
        {

            log.info("SellerService validateCredential username: {} ipAddress: {}", credentialDTO.getUsername(),credentialDTO.getIpAddress());

            boolean isValidCredential = sellerServiceApplication.validateCredential(credentialDTO);

            log.info("SellerService validateCredential Response {}", isValidCredential);

            return isValidCredential;

        }
    }

    @Scheduled(cron = "0 0 23 * * *")
    private void jobUpdateSeller(){

        log.info("jobUpdateSeller at: {}", LocalDate.now());
        getSellerByVatAndSerial();

    }

}
