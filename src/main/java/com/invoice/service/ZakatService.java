package com.invoice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.dto.CredentialDTO;
import com.invoice.dto.PDFGeneratorDTO;
import com.invoice.dto.SellerDTO;
import com.invoice.dto.ZakatRequestDTO;
import com.invoice.dto.zakat.ZakatClearanceResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceResponseDTO;
import com.invoice.dto.zakat.ZakatReportingResponseDTO;
import com.invoice.dto.zakat.ZakatApplicationRequestDTO;
import com.invoice.exception.SellerNotFoundException;
import com.invoice.exception.ZakatException;
import com.invoice.model.Email;
import com.invoice.repository.EmailRepositoryImpl;
import com.invoice.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZakatService {

    private final ZakatServiceApplication zakatServiceApplication;
    private final SellerService sellerService;

    private final EmailRepositoryImpl emailRepository;

    private final XMLParser xmlParser;

    private final QRCodeUtil qrCodeUtil;

    private  final PDFGenerator pdfGenerator;

    private final AWSS3Service awss3Service;

    private final ImageUtil imageUtil;

    private final PDFFileUtil pdfFileUtil;

    @Value("${IS_B2B_ARCHIVE_CLOUD}")
    private boolean isB2BArchiveCLoud;

    @Value("${IS_B2B_EMAIL_SENT}")
    private boolean isB2BEmailSent;

    @Value("${IS_B2C_ARCHIVE_CLOUD}")
    private boolean isB2CArchiveCLoud;

    @Value("${IS_B2C_EMAIL_SENT}")
    private boolean isB2CEmailSent;

    public ZakatReportingResponseDTO reporting(ZakatApplicationRequestDTO zakatInvoiceRequest, String certificate, String secretKey) throws WebClientResponseException {

        log.info("ZakatService Reporting Request {}", zakatInvoiceRequest);

        ZakatReportingResponseDTO zakatReportingResponseDTO = zakatServiceApplication.reporting(zakatInvoiceRequest,certificate,secretKey);

        log.info("ZakatService Reporting Response {}", zakatReportingResponseDTO);

        return zakatReportingResponseDTO;
    }

    public ZakatClearanceResponseDTO clearance(ZakatApplicationRequestDTO zakatInvoiceRequest, String certificate, String secretKey) throws WebClientResponseException {

        log.info("ZakatService Clearance Request {}", zakatInvoiceRequest);

        ZakatClearanceResponseDTO zakatClearanceResponseDTO = zakatServiceApplication.clearance(zakatInvoiceRequest,certificate,secretKey);

        log.info("ZakatService Clearance Response {}", zakatClearanceResponseDTO);

        return zakatClearanceResponseDTO;
    }

    public ZakatComplianceResponseDTO compliance(ZakatApplicationRequestDTO zakatInvoiceRequest, String certificate, String secretKey) throws WebClientResponseException{

        log.info("ZakatService Compliance Request {}", zakatInvoiceRequest);

        ZakatComplianceResponseDTO zakatComplianceResponseDTO = zakatServiceApplication.compliance(zakatInvoiceRequest,certificate,secretKey);

        log.info("ZakatService Compliance Response {}", zakatComplianceResponseDTO);

        return zakatComplianceResponseDTO;
    }

    public ZakatClearanceResponseDTO clearance(CredentialDTO credentialDTO,ZakatRequestDTO zakatRequestDTO) {

        log.info("ZakatService clearance Request {}", zakatRequestDTO);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        ZakatClearanceResponseDTO zakatClearanceResponseDTO = new ZakatClearanceResponseDTO();

        try {

            ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
            zakatApplicationRequestDTO.setInvoice(zakatRequestDTO.getInvoice());
            zakatApplicationRequestDTO.setInvoiceHash(zakatRequestDTO.getInvoiceHash());
            zakatApplicationRequestDTO.setUuid(zakatRequestDTO.getUuid());

            zakatClearanceResponseDTO = zakatServiceApplication.clearance(zakatApplicationRequestDTO, zakatRequestDTO.getCertificate(), zakatRequestDTO.getSecretKey());

            if (null != zakatClearanceResponseDTO && Constants.VLD_STS_PASS.equalsIgnoreCase(zakatClearanceResponseDTO.getValidationResults().getStatus())) {

                SAXReader xmlReader = new SAXReader();

                org.dom4j.Document doc = xmlReader.read(new ByteArrayInputStream(Base64.getDecoder().decode(zakatClearanceResponseDTO.getClearedInvoice())));

                XPath xpath = DocumentHelper.createXPath("/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
                xpath.setNamespaceURIs(CommonUtils.getNameSpacesMap());

                String signedXML = doc.getRootElement().asXML();

                PDFGeneratorDTO pdfGeneratorDTO = xmlParser.parseXMLInvoice(signedXML);

                File qrCodeFile = qrCodeUtil.generateQRCode(xpath.selectSingleNode(doc).getText(),zakatRequestDTO.getInvoiceID());

                SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                File imageFile = null;
                String imagePath = "";
                if(null != sellerDTO.getImage() && !CommonUtils.isNullOrEmptyString(sellerDTO.getImage())) {
                    imageFile= imageUtil.generateFile(sellerDTO.getImage());
                    imagePath = imageFile.getPath();
                }

                File pdfFile = pdfGenerator.generatePDF(pdfGeneratorDTO.getInvoiceMaster(),pdfGeneratorDTO.getInvoiceLines(),qrCodeFile.getPath(),signedXML,imagePath);

                if(null != pdfFile) {

                    if(isB2BEmailSent && !CommonUtils.isNullOrEmptyString(zakatRequestDTO.getBuyerEmail())) {
                        Email email = new Email();
                        email.setId(zakatRequestDTO.getInvoiceID());
                        email.setBuyerEmail(zakatRequestDTO.getBuyerEmail());
                        createEmailEntry(email);
                    }

                    if(isB2BArchiveCLoud) {
                        String egsPrefix = zakatRequestDTO.getInvoiceID().substring(0,zakatRequestDTO.getInvoiceID().length()-12);
                        awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, zakatRequestDTO.getInvoiceID());
                    }

                }

            }

            log.info("ZakatService clearance Response {}", zakatClearanceResponseDTO);

        } catch(WebClientResponseException ex){
            log.error("WebClientResponseException In ZakatService Clearance Response UUID : {} = {} = {}", zakatRequestDTO.getUuid(), ex.getStatusCode(), ex.getResponseBodyAsString());
            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer.append("2").append("$").append(zakatRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
            errorMessageBuffer.append(ex.getResponseBodyAsString());

            throw new ZakatException(errorMessageBuffer.toString());
        } catch (DocumentException e) {

            log.error("DocumentException In ZakatService Clearance Response UUID : {} = {}", zakatRequestDTO.getUuid(),e.getMessage());
            throw new RuntimeException(e);
        }

        return zakatClearanceResponseDTO;

    }

    public ZakatReportingResponseDTO reporting(CredentialDTO credentialDTO,ZakatRequestDTO zakatRequestDTO) {

        log.info("ZakatService reporting Request {}", zakatRequestDTO);

        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        ZakatReportingResponseDTO zakatReportingResponseDTO = new ZakatReportingResponseDTO();

        try {
            ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
            zakatApplicationRequestDTO.setInvoice(zakatRequestDTO.getInvoice());
            zakatApplicationRequestDTO.setInvoiceHash(zakatRequestDTO.getInvoiceHash());
            zakatApplicationRequestDTO.setUuid(zakatRequestDTO.getUuid());

            zakatReportingResponseDTO = zakatServiceApplication.reporting(zakatApplicationRequestDTO, zakatRequestDTO.getCertificate(), zakatRequestDTO.getSecretKey());

            if (null != zakatReportingResponseDTO && Constants.VLD_STS_PASS.equalsIgnoreCase(zakatReportingResponseDTO.getValidationResults().getStatus())) {

                if (isB2CEmailSent && !CommonUtils.isNullOrEmptyString(zakatRequestDTO.getBuyerEmail())) {
                    Email email = new Email();
                    email.setId(zakatRequestDTO.getInvoiceID());
                    email.setBuyerEmail(zakatRequestDTO.getBuyerEmail());
                    createEmailEntry(email);
                }

                if(isB2CArchiveCLoud) {
                    File pdfFile = pdfFileUtil.generateFile(zakatRequestDTO.getPdf());
                    String egsPrefix = zakatRequestDTO.getInvoiceID().substring(0,zakatRequestDTO.getInvoiceID().length()-12);
                    awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, zakatRequestDTO.getInvoiceID());
                }

            }

            log.info("ZakatService reporting Response {}", zakatReportingResponseDTO);

        } catch(WebClientResponseException ex){
            log.error("WebClientResponseException In ZakatService reporting Response UUID : {} = {} = {}", zakatRequestDTO.getUuid(), ex.getStatusCode(), ex.getResponseBodyAsString());

            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer.append("1").append("$").append(zakatRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
            errorMessageBuffer.append(ex.getResponseBodyAsString());

            throw new ZakatException(errorMessageBuffer.toString());
        }

        return zakatReportingResponseDTO;

    }

    public ZakatComplianceResponseDTO compliance(CredentialDTO credentialDTO,ZakatRequestDTO zakatRequestDTO) {

        log.info("ZakatService compliance Request {}", zakatRequestDTO);


        if(!sellerService.validateCredential(credentialDTO)){
            throw new SellerNotFoundException("Invalid Credentials");
        }

        ZakatComplianceResponseDTO zakatComplianceResponseDTO = new ZakatComplianceResponseDTO();

        try {

            ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
            zakatApplicationRequestDTO.setInvoice(zakatRequestDTO.getInvoice());
            zakatApplicationRequestDTO.setInvoiceHash(zakatRequestDTO.getInvoiceHash());
            zakatApplicationRequestDTO.setUuid(zakatRequestDTO.getUuid());

            zakatComplianceResponseDTO = zakatServiceApplication.compliance(zakatApplicationRequestDTO, zakatRequestDTO.getCertificate(), zakatRequestDTO.getSecretKey());

            log.info("ZakatService ZakatService Response {}", zakatComplianceResponseDTO);

        } catch(WebClientResponseException ex){
            log.error("Exception In ZakatService Compliance {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer.append("4").append("$").append(zakatRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
            errorMessageBuffer.append(ex.getResponseBodyAsString());
            throw new ZakatException(errorMessageBuffer.toString());
        }

        return zakatComplianceResponseDTO;

    }

    private void createEmailEntry(Email email){
        log.info("Zakat Service createEmailEntry: {}",email);
        emailRepository.createEmail(email);
    }
}
