package com.invoice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gazt.einvoicing.signing.service.impl.SigningServiceImpl;
import com.gazt.einvoicing.signing.service.model.InvoiceSigningResult;
import com.invoice.dto.*;
import com.invoice.dto.zakat.ZakatClearanceResponseDTO;
import com.invoice.dto.zakat.ZakatComplianceResponseDTO;
import com.invoice.dto.zakat.ZakatReportingResponseDTO;
import com.invoice.dto.zakat.ZakatApplicationRequestDTO;
import com.invoice.dto.zakat.model.ZakatInfoModel;
import com.invoice.dto.zakat.model.ZakatValidationResultModel;
import com.invoice.exception.BuyerEmailNotFoundException;
import com.invoice.exception.SellerNotFoundException;
import com.invoice.exception.ZakatException;
import com.invoice.mapper.MapInvoice;
import com.invoice.mapper.MapInvoiceLine;
import com.invoice.model.*;
import com.invoice.repository.EmailRepositoryImpl;
import com.invoice.repository.InvoiceRepositoryImpl;
import com.invoice.util.*;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.util.ECDSAUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    @Value("${IS_B2B_ARCHIVE_CLOUD}")
    private boolean isB2BArchiveCLoud;

    @Value("${IS_B2B_EMAIL_SENT}")
    private boolean isB2BEmailSent;

    @Value("${IS_B2C_ARCHIVE_CLOUD}")
    private boolean isB2CArchiveCLoud;

    @Value("${IS_B2C_EMAIL_SENT}")
    private boolean isB2CEmailSent;

    @Value("${IS_PRINT_PDF}")
    private boolean isPDFReturn;
    private final ZakatService zakatService;
    private final InvoiceRepositoryImpl invoiceRepository;
    private final MapInvoice mapInvoice;
    private final MapInvoiceLine mapInvoiceLine;
    private final SellerService sellerService;
    private final PDFGenerator pdfGenerator;
    private final XMLGenerator xmlGenerator;
    private final QRCodeUtil qrCodeUtil;
    private final ImageUtil imageUtil;
    private final CSVFileUtil csvFileUtil;

    private final EmailRepositoryImpl emailRepository;

    private final AWSS3Service awss3Service;

    private final XMLParser xmlParser;

    private final PDFFileUtil pdfFileUtil;

    public InvoiceResponse generateInvoice(InvoiceDTO invoiceDTO) {
        InvoiceResponse response = new InvoiceResponse();
        try {

            if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
                throw new SellerNotFoundException("System Expired Contact System Administrator");
            }

            if (CommonUtils.isNullOrEmptyString(invoiceDTO.getCurrency())) {
                invoiceDTO.setCurrency("SAR");
            }

            if (invoiceDTO.getFxRate() == 0) {
                invoiceDTO.setFxRate(1);
            }

            if (invoiceDTO.getCurrency().equalsIgnoreCase("SAR")) {
                invoiceDTO.setTaxSAR(invoiceDTO.getTotalVAT());
                invoiceDTO.setTotalSAR(invoiceDTO.getTaxInclusiveAmount());
            }

            Result validationResult = new Result();

            String xml = xmlGenerator.validateRequestViaXML(invoiceDTO);

            log.info("before validation XML >>>> " + xml);

            try {
                File f = File.createTempFile("signedInvoice", ".xml");
                Files.write(Path.of(f.getPath()), xml.getBytes(StandardCharsets.UTF_8));
                validationResult = CommonUtils.validateInvoice(f);
                f.delete();
            } catch (Exception e) {
                log.error("Exception in getInvoiceResponse Zatca Validation: {}", e.getStackTrace());
                validationResult.setValid(false);
                e.printStackTrace();
            }

            if (validationResult.isValid()) {

                SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                long seqId = createInvoice(invoiceDTO);
                response.setSeqId(String.valueOf(seqId));
                response.setInvoiceId(getInvoiceId(seqId));
                createInvoiceLine(invoiceDTO.getInvoiceLines(), seqId);

                InvoiceMaster invoiceMaster = invoiceRepository.getInvoice(seqId);

                List<InvoiceLine> invoiceLines = invoiceRepository.getInvoiceLinesByInvoiceId(invoiceMaster.getSeqId());
                String previousInvoiceHash = invoiceRepository.getPreviousInvocieHash(seqId);

                xml = xmlGenerator.generateXML(invoiceMaster, invoiceLines, previousInvoiceHash);
                invoiceMaster.setXml(xml);

                PrivateKey privateKey = loadPrivateKey(sellerDTO);

                InvoiceSigningResult result = loadCertificate(xml, privateKey,sellerDTO);

                //get QR code from cleared invoice
                SAXReader xmlReader = new SAXReader();
                org.dom4j.Document doc = xmlReader.read(new ByteArrayInputStream(result.getSingedXML().getBytes(StandardCharsets.UTF_8)));

                //update xml with self signedXML
                xml = doc.getRootElement().asXML();

                invoiceMaster.setStatus(Constants.INPROCESS_STATUS);
                invoiceMaster.setSignedXML(xml);
                invoiceMaster.setQrCode(result.getQrCode());
                invoiceMaster.setInvocieHash(result.getInvoiceHash());

                invoiceRepository.createInvoiceLOBS(invoiceMaster);

                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);
                invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);

                if("Y".equalsIgnoreCase(sellerDTO.getZatcaFlag()))
                {
                    ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
                    zakatApplicationRequestDTO.setInvoice(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));
                    zakatApplicationRequestDTO.setInvoiceHash(result.getInvoiceHash());
                    zakatApplicationRequestDTO.setUuid(invoiceMaster.getUuid());

                    if(Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(sellerDTO.getCertificateStatus())) {

                        //call compliance service
                        ZakatComplianceResponseDTO zatcaComplianceResponseDto = null;

                        try {
                            zatcaComplianceResponseDto = zakatService.compliance(zakatApplicationRequestDTO, sellerDTO.getCertificateCompliance(), sellerDTO.getKeyCompliance());
                        }
                        catch(WebClientResponseException ex){

                            log.error("Exception In Invoice Service Zakat Compliance Service Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                            StringBuffer errorMessageBuffer = new StringBuffer();
                            errorMessageBuffer.append("4").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                            errorMessageBuffer.append(ex.getResponseBodyAsString());

                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                            invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                            throw new ZakatException(errorMessageBuffer.toString());

                        }
                        if (null != zatcaComplianceResponseDto) {

                            invoiceMaster.setZatcaStatus(invoiceMaster.getSubType().startsWith("01")?zatcaComplianceResponseDto.getClearanceStatus():zatcaComplianceResponseDto.getReportingStatus());
                            invoiceMaster.setValidationStatus(zatcaComplianceResponseDto.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zatcaComplianceResponseDto.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);

                            response.setValidationResults(zatcaComplianceResponseDto.getValidationResults());
                            response.setXmlContent(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));
                            response.setZatcaStatus(zatcaComplianceResponseDto.getClearanceStatus());
                        }
                    }else {

                        if (invoiceMaster.getSubType().startsWith("01"))
                        {

                            ZakatClearanceResponseDTO zakatClearanceResponseDTO = null;

                            try {

                                zakatClearanceResponseDTO = zakatService.clearance(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());

                            } catch(WebClientResponseException ex){
                                log.error("Exception In Invoice Service Zakat Clearance Service Response SEQ ID: {} = {} = {}", invoiceMaster.getSeqId(), ex.getStatusCode(), ex.getResponseBodyAsString());
                                StringBuffer errorMessageBuffer = new StringBuffer();
                                errorMessageBuffer.append("2").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                                errorMessageBuffer.append(ex.getResponseBodyAsString());

                                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                                invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                                throw new ZakatException(errorMessageBuffer.toString());
                            }

                            response.setValidationResults(zakatClearanceResponseDTO.getValidationResults());
                            response.setXmlContent(zakatClearanceResponseDTO.getClearedInvoice());
                            response.setZatcaStatus(zakatClearanceResponseDTO.getClearanceStatus());

                            if (null != zakatClearanceResponseDTO.getClearedInvoice()) {
                                //read the QR code from cleared invoice

                                doc = xmlReader.read(new ByteArrayInputStream(Base64.getDecoder().decode(zakatClearanceResponseDTO.getClearedInvoice())));


                                XPath xpath = DocumentHelper.createXPath("/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
                                xpath.setNamespaceURIs(CommonUtils.getNameSpacesMap());

                                result.setQrCode(xpath.selectSingleNode(doc).getText());

                                invoiceMaster.setSignedXML(doc.getRootElement().asXML());
                                invoiceMaster.setQrCode(result.getQrCode());


                            }
                            invoiceMaster.setZatcaStatus(zakatClearanceResponseDTO.getClearanceStatus());
                            invoiceMaster.setValidationStatus(zakatClearanceResponseDTO.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zakatClearanceResponseDTO.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);

                        }
                        else {
                            ZakatReportingResponseDTO zakatReportingResponseDTO = null;

                            try {
                                zakatReportingResponseDTO = zakatService.reporting(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());
                            } catch(WebClientResponseException ex){
                                log.error("Exception In Invoice Service Zakat Reporting Service Response SEQ ID: {} = {} = {}", invoiceMaster.getSeqId(), ex.getStatusCode(), ex.getResponseBodyAsString());

                                StringBuffer errorMessageBuffer = new StringBuffer();
                                errorMessageBuffer.append("1").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                                errorMessageBuffer.append(ex.getResponseBodyAsString());

                                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                                invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                                throw new ZakatException(errorMessageBuffer.toString());
                            }

                            invoiceMaster.setZatcaStatus(zakatReportingResponseDTO.getReportingStatus());
                            invoiceMaster.setValidationStatus(zakatReportingResponseDTO.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zakatReportingResponseDTO.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);
                        }
                    }

                    invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                    invoiceRepository.updateInvoiceResponse(invoiceMaster);

                    if (Constants.VLD_STS_PASS.equalsIgnoreCase(invoiceMaster.getValidationStatus()) && (
                            invoiceMaster.getZatcaStatus().equalsIgnoreCase(Constants.CLRNC_STS) || invoiceMaster.getZatcaStatus().equalsIgnoreCase(Constants.RPRT_STS))) {
                        invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);
                        invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);

                    }
                }
                response.setXmlContent(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));

                File qrCodeFile = qrCodeUtil.generateQRCode(result.getQrCode(),invoiceMaster.getId());

                File imageFile = null;
                String imagePath = "";
                if(!CommonUtils.isNullOrEmptyString(sellerDTO.getImage())) {
                    imageFile= imageUtil.generateFile(sellerDTO.getImage());
                    imagePath = imageFile.getPath();
                }

                File pdfFile = pdfGenerator.generatePDF(invoiceMaster, invoiceLines, qrCodeFile.getPath(), invoiceMaster.getSignedXML(),imagePath);

                if(null != pdfFile) {
                    byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
                    String pdf = Base64.getEncoder().encodeToString(fileContent);
                    if(isPDFReturn) {
                        response.setPdfContent(pdf);
                    }
                    invoiceRepository.savePDF(pdf,invoiceMaster.getSeqId());

                    if(isB2BEmailSent && invoiceMaster.getSubType().startsWith("01")  && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                        Email email = new Email();
                        email.setId(invoiceMaster.getId());
                        email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                        createEmailEntry(email);
                    }

                    if(isB2CEmailSent && invoiceMaster.getSubType().startsWith("02")  && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                        Email email = new Email();
                        email.setId(invoiceMaster.getId());
                        email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                        createEmailEntry(email);
                    }

                    if(isB2BArchiveCLoud && invoiceMaster.getSubType().startsWith("01")) {
                        String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                        awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                    }

                    if(isB2CArchiveCLoud && invoiceMaster.getSubType().startsWith("02")) {
                        String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                        awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                    }
                }

                response.setQrcode(result.getQrCode());
                response.setQrcodeFile(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(qrCodeFile)));

                qrCodeUtil.fileCleanUp(qrCodeFile);
                if(null != imageFile){
                    imageUtil.fileCleanUp(imageFile);
                }

            } else {
                ZakatValidationResultModel validationResults = new ZakatValidationResultModel();
                validationResults.setStatus("FAIL");

                List<ZakatInfoModel> errorMessages = new ArrayList<>();

                if (validationResult.getError() != null && !validationResult.getError().isEmpty()) {
                    for (String err : validationResult.getError().keySet()) {
                        errorMessages.add(new ZakatInfoModel(err, "VALIDATION_ERROR", "INIT_VLD", validationResult.getError().get(err), "ERROR"));
                    }
                }

                validationResults.setErrorMessages(errorMessages);
                response.setValidationResults(validationResults);

            }
        } catch (DocumentException | IOException ex) {
            log.error("Exception in getInvoiceResponse DocumentException IOException SEQ ID: {} = {}",response.getSeqId(), ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
            invoiceRepository.updateInvoiceLineStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
            ex.printStackTrace();

        }
        return response;
    }

    public InvoiceResponse generateReportInvoice(InvoiceDTO invoiceDTO) {
        InvoiceResponse response = new InvoiceResponse();

        try {

            if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
                throw new SellerNotFoundException("System Expired Contact System Administrator");
            }

            /*if (!invoiceDTO.getSubType().startsWith("02")) {
                throw new RequestValidationException("InvoiceSubType must starts with 02 for reporting");
            }*/

            if (CommonUtils.isNullOrEmptyString(invoiceDTO.getCurrency())) {
                invoiceDTO.setCurrency("SAR");
            }

            if (invoiceDTO.getFxRate() == 0) {
                invoiceDTO.setFxRate(1);
            }

            if (invoiceDTO.getCurrency().equalsIgnoreCase("SAR")) {
                invoiceDTO.setTaxSAR(invoiceDTO.getTotalVAT());
                invoiceDTO.setTotalSAR(invoiceDTO.getTaxInclusiveAmount());
            }

            Result validationResult = new Result();

            String xml = xmlGenerator.validateRequestViaXML(invoiceDTO);

            log.info("before validation XML >>>> " + xml);


            try {
                File f = File.createTempFile("signedInvoice", ".xml");
                Files.write(Path.of(f.getPath()), xml.getBytes(StandardCharsets.UTF_8));
                validationResult = CommonUtils.validateInvoice(f);
                f.delete();
            } catch (Exception e) {
                log.error("Exception in getInvoiceResponse Zatca Validation: {}", e.getStackTrace());
                validationResult.setValid(false);
                e.printStackTrace();
            }

            if (validationResult.isValid()) {

                SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                long seqId = createInvoice(invoiceDTO);
                response.setSeqId(String.valueOf(seqId));
                response.setInvoiceId(getInvoiceId(seqId));
                createInvoiceLine(invoiceDTO.getInvoiceLines(), seqId);

                InvoiceMaster invoiceMaster = invoiceRepository.getInvoice(seqId);

                List<InvoiceLine> invoiceLines = invoiceRepository.getInvoiceLinesByInvoiceId(invoiceMaster.getSeqId());
                String previousInvoiceHash = invoiceRepository.getPreviousInvocieHash(seqId);

                xml = xmlGenerator.generateXML(invoiceMaster, invoiceLines, previousInvoiceHash);
                invoiceMaster.setXml(xml);

                PrivateKey privateKey = loadPrivateKey(sellerDTO);

                InvoiceSigningResult result = loadCertificate(xml, privateKey,sellerDTO);

                //get QR code from cleared invoice
                SAXReader xmlReader = new SAXReader();
                org.dom4j.Document doc = xmlReader.read(new ByteArrayInputStream(result.getSingedXML().getBytes(StandardCharsets.UTF_8)));

                //update xml with self signedXML
                xml = doc.getRootElement().asXML();

                invoiceMaster.setStatus(Constants.INPROCESS_STATUS);
                invoiceMaster.setSignedXML(xml);
                invoiceMaster.setQrCode(result.getQrCode());
                invoiceMaster.setInvocieHash(result.getInvoiceHash());
                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.INPROCESS_STATUS);

                invoiceRepository.createInvoiceLOBS(invoiceMaster);

                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);
                invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);

                response.setXmlContent(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));

                File qrCodeFile = qrCodeUtil.generateQRCode(result.getQrCode(),invoiceMaster.getId());

                File imageFile = null;
                String imagePath = "";
                if(!CommonUtils.isNullOrEmptyString(sellerDTO.getImage())) {
                    imageFile= imageUtil.generateFile(sellerDTO.getImage());
                    imagePath = imageFile.getPath();
                }

                File pdfFile = pdfGenerator.generatePDF(invoiceMaster, invoiceLines, qrCodeFile.getPath(), invoiceMaster.getSignedXML(),imagePath);

                if(null != pdfFile) {
                    byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
                    String pdf = Base64.getEncoder().encodeToString(fileContent);
                    if(isPDFReturn) {
                        response.setPdfContent(pdf);
                    }
                    invoiceRepository.savePDF(pdf,invoiceMaster.getSeqId());

                    if(isB2CEmailSent && invoiceMaster.getSubType().startsWith("02")  && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                        Email email = new Email();
                        email.setId(invoiceMaster.getId());
                        email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                        createEmailEntry(email);
                    }

                    // TODO CHECK weather to archive during creating invoices in P status
                    if(isB2BArchiveCLoud && invoiceMaster.getSubType().startsWith("01")) {
                        String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                        awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                    }

                    if(isB2CArchiveCLoud && invoiceMaster.getSubType().startsWith("02")) {
                        String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                        awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                    }

                }

                response.setQrcode(result.getQrCode());
                response.setQrcodeFile(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(qrCodeFile)));

                qrCodeUtil.fileCleanUp(qrCodeFile);
                if(null != imageFile){
                    imageUtil.fileCleanUp(imageFile);
                }

            } else {
                ZakatValidationResultModel validationResults = new ZakatValidationResultModel();
                validationResults.setStatus("FAIL");

                List<ZakatInfoModel> errorMessages = new ArrayList<>();

                if (validationResult.getError() != null && !validationResult.getError().isEmpty()) {
                    for (String err : validationResult.getError().keySet()) {
                        errorMessages.add(new ZakatInfoModel(err, "VALIDATION_ERROR", "INIT_VLD", validationResult.getError().get(err), "ERROR"));
                    }
                }

                validationResults.setErrorMessages(errorMessages);
                response.setValidationResults(validationResults);

            }
        } catch (DocumentException | IOException ex) {
            log.error("Exception in getInvoiceResponse DocumentException IOException SEQ ID: {} = {}",response.getSeqId(), ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
            invoiceRepository.updateInvoiceLineStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
            ex.printStackTrace();

        }
        return response;
    }


    public InvoiceReportResponse sendStandardInvoiceToZatca() {

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        InvoiceReportResponse response = new InvoiceReportResponse();

        int totalProcessed = 0;
        int totalFailed = 0;
        Long seqId = 0l;

        try {

            List<InvoiceMaster> invoiceMasterList = invoiceRepository.getStandardInvoices();
            response.setTotalPendingInvoices(invoiceMasterList.size());

            for (InvoiceMaster invoiceMaster : invoiceMasterList)
            {

                SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                if("Y".equalsIgnoreCase(sellerDTO.getZatcaFlag()) && invoiceMaster.getSubType().startsWith("01"))
                {
                    seqId = invoiceMaster.getSeqId();

                    ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
                    zakatApplicationRequestDTO.setInvoice(Base64.getEncoder().encodeToString(invoiceMaster.getSignedXML().getBytes(StandardCharsets.UTF_8)));
                    zakatApplicationRequestDTO.setInvoiceHash(invoiceMaster.getInvocieHash());
                    zakatApplicationRequestDTO.setUuid(invoiceMaster.getUuid());


                    if (Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(sellerDTO.getCertificateStatus())) {
                        //call compliance service
                        ZakatComplianceResponseDTO zatcaComplianceResponseDto = null;

                        try {
                            zatcaComplianceResponseDto = zakatService.compliance(zakatApplicationRequestDTO, sellerDTO.getCertificateCompliance(), sellerDTO.getKeyCompliance());
                        }
                        catch(WebClientResponseException ex){

                            log.error("Exception In Invoice Service Zakat Compliance Service Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                            invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                        }
                        if (null != zatcaComplianceResponseDto) {
                            invoiceMaster.setZatcaStatus(zatcaComplianceResponseDto.getClearanceStatus());
                            invoiceMaster.setValidationStatus(zatcaComplianceResponseDto.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zatcaComplianceResponseDto.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);
                        }

                    } else {
                        ZakatClearanceResponseDTO zakatClearanceResponseDTO = null;

                        try {
                            zakatClearanceResponseDTO = zakatService.clearance(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());

                        } catch(WebClientResponseException ex){

                            log.error("Exception In Invoice Service Zakat Reporting Service Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                            invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                        }

                        if (null != zakatClearanceResponseDTO) {

                            SAXReader xmlReader = new SAXReader();

                            org.dom4j.Document doc = xmlReader.read(new ByteArrayInputStream(Base64.getDecoder().decode(zakatClearanceResponseDTO.getClearedInvoice())));

                            XPath xpath = DocumentHelper.createXPath("/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
                            xpath.setNamespaceURIs(CommonUtils.getNameSpacesMap());

                            invoiceMaster.setSignedXML(doc.getRootElement().asXML());
                            invoiceMaster.setQrCode(xpath.selectSingleNode(doc).getText());

                            invoiceMaster.setZatcaStatus(zakatClearanceResponseDTO.getClearanceStatus());
                            invoiceMaster.setValidationStatus(zakatClearanceResponseDTO.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zakatClearanceResponseDTO.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);

                        }
                    }

                    invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                    invoiceRepository.updateInvoiceResponse(invoiceMaster);

                    if (Constants.VLD_STS_PASS.equalsIgnoreCase(invoiceMaster.getValidationStatus())) {
                        totalProcessed = totalProcessed + 1;
                        invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);
                        invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);

                        PDFGeneratorDTO pdfGeneratorDTO = xmlParser.parseXMLInvoice(invoiceMaster.getSignedXML());

                        File qrCodeFile = qrCodeUtil.generateQRCode(invoiceMaster.getQrCode(),invoiceMaster.getId());

                        File imageFile = null;
                        String imagePath = "";
                        if(null != sellerDTO.getImage() && !CommonUtils.isNullOrEmptyString(sellerDTO.getImage())) {
                            imageFile= imageUtil.generateFile(sellerDTO.getImage());
                            imagePath = imageFile.getPath();
                        }

                        File pdfFile = pdfGenerator.generatePDF(pdfGeneratorDTO.getInvoiceMaster(),pdfGeneratorDTO.getInvoiceLines(),qrCodeFile.getPath(),invoiceMaster.getSignedXML(),imagePath);

                        if(null != pdfFile) {
                            byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
                            String pdf = Base64.getEncoder().encodeToString(fileContent);
                            invoiceRepository.savePDF(pdf,invoiceMaster.getSeqId());

                            if(isB2BEmailSent && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                                Email email = new Email();
                                email.setId(invoiceMaster.getId());
                                email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                                createEmailEntry(email);
                            }

                            if(isB2BArchiveCLoud) {
                                String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                                awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                            }

                        }

                    } else {
                        totalFailed = totalFailed + 1;
                        invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                        invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                    }
                }
                response.setTotalProcessedInvoices(totalProcessed);
                response.setTotalFailedInvoice(totalFailed);
                response.setStatus("Success");
            }

        } catch (IOException | DocumentException ex) {
            log.error("IOException | DocumentException in InvoiceService reportInvoices Exception SEQ ID: {} = {}",seqId, ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(seqId, Constants.ERROR_STATUS);
            invoiceRepository.updateInvoiceLineStatus(seqId, Constants.ERROR_STATUS);
            ex.printStackTrace();
            response.setStatus("Error");
        }
        return response;
    }


    public InvoiceReportResponse sendSimplifiedInvoiceToZatca() {

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        InvoiceReportResponse response = new InvoiceReportResponse();

        int totalProcessed = 0;
        int totalFailed = 0;
        Long seqId = 0l;

        try {

            List<InvoiceMaster> invoiceMasterList = invoiceRepository.getSimplifiedInvoices();
            response.setTotalPendingInvoices(invoiceMasterList.size());

            for (InvoiceMaster invoiceMaster : invoiceMasterList)
            {

                SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                if("Y".equalsIgnoreCase(sellerDTO.getZatcaFlag()) && invoiceMaster.getSubType().startsWith("02"))
                {
                    seqId = invoiceMaster.getSeqId();

                    ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
                    zakatApplicationRequestDTO.setInvoice(Base64.getEncoder().encodeToString(invoiceMaster.getSignedXML().getBytes(StandardCharsets.UTF_8)));
                    zakatApplicationRequestDTO.setInvoiceHash(invoiceMaster.getInvocieHash());
                    zakatApplicationRequestDTO.setUuid(invoiceMaster.getUuid());


                    if (Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(sellerDTO.getCertificateStatus())) {
                        //call compliance service
                        ZakatComplianceResponseDTO zatcaComplianceResponseDto = null;

                        try {
                             zatcaComplianceResponseDto = zakatService.compliance(zakatApplicationRequestDTO, sellerDTO.getCertificateCompliance(), sellerDTO.getKeyCompliance());
                        }
                        catch(WebClientResponseException ex){

                            log.error("Exception In Invoice Service sendSimplifiedInvoiceToZatca Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                            invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                        }
                        if (null != zatcaComplianceResponseDto) {
                            invoiceMaster.setZatcaStatus(zatcaComplianceResponseDto.getReportingStatus());
                            invoiceMaster.setValidationStatus(zatcaComplianceResponseDto.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zatcaComplianceResponseDto.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);
                        }

                    } else {
                        ZakatReportingResponseDTO zakatReportingResponseDTO = null;

                        try {
                            zakatReportingResponseDTO = zakatService.reporting(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());

                        } catch(WebClientResponseException ex){

                            log.error("Exception In Invoice Service sendSimplifiedInvoiceToZatca Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                            invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                            invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                        }

                        if (null != zakatReportingResponseDTO) {
                            invoiceMaster.setZatcaStatus(zakatReportingResponseDTO.getReportingStatus());
                            invoiceMaster.setValidationStatus(zakatReportingResponseDTO.getValidationResults().getStatus());

                            ObjectMapper objectMapper = new ObjectMapper();
                            String zakatValidationResult = objectMapper.writeValueAsString(zakatReportingResponseDTO.getValidationResults());
                            invoiceMaster.setZatcaResponse(zakatValidationResult);

                        }
                    }

                    invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                    invoiceRepository.updateInvoiceResponse(invoiceMaster);

                    if (Constants.VLD_STS_PASS.equalsIgnoreCase(invoiceMaster.getValidationStatus())) {
                        totalProcessed = totalProcessed + 1;
                        invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);
                        invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);
                    } else {
                        totalFailed = totalFailed + 1;
                        invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                        invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                    }
                }
                response.setTotalProcessedInvoices(totalProcessed);
                response.setTotalFailedInvoice(totalFailed);
                response.setStatus("Success");
            }

        } catch (JsonProcessingException ex) {
            log.error("JsonProcessingException in InvoiceService sendSimplifiedInvoiceToZatca Exception SEQ ID: {} = {}",seqId, ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(seqId, Constants.ERROR_STATUS);
            invoiceRepository.updateInvoiceLineStatus(seqId, Constants.ERROR_STATUS);
            ex.printStackTrace();
            response.setStatus("Error");
        }
        return response;
    }

    private long createInvoice(InvoiceDTO invoiceDTO)
    {
        long seqId = 0;

        try
        {

            InvoiceMaster invoiceMaster = mapInvoice.invoiceDTOToInvoice(invoiceDTO);
            invoiceMaster.setStatus(Constants.CREATED_STATUS);
            seqId = invoiceRepository.createInvoiceMaster(invoiceMaster);

        }
        catch (Exception ex)
        {
            log.error("Exception in createInvoice SEQ ID: {} = {}",seqId, ex.getStackTrace());
            ex.printStackTrace();
        }

        return seqId;
    }

    private String getInvoiceId(long seqId) {
        String invoiceId = "";
        try {
            invoiceId = invoiceRepository.getInvoiceId(seqId);
        } catch (Exception ex) {
            log.error("Exception in getInvoiceId SEQ ID: {} = {}", seqId,ex.getStackTrace());
            ex.printStackTrace();
        }

        return invoiceId;
    }


    private void createInvoiceLine(List<InvoiceLineDTO> invoiceLineDTOList, long seqID) {
        try {
            List<InvoiceLine> invoiceLineList = this.mapInvoiceLine.invoiceLineDTOsToInvoiceLines(invoiceLineDTOList);
            for (InvoiceLine invoiceLine : invoiceLineList) {
                invoiceLine.setSeqRef(seqID);
                invoiceLine.setStatus(Constants.CREATED_STATUS);
                this.invoiceRepository.createInvoiceLine(invoiceLine);
            }
        } catch (Exception ex) {
            log.error("Exception in createInvoiceLine SEQ ID: {} = {}",seqID, ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(seqID, Constants.ERROR_STATUS);
            ex.printStackTrace();
        }
    }

    private PrivateKey loadPrivateKey(SellerDTO sellerDTO) {
        PrivateKey privateKey = null;
        String key = sellerDTO.getPrivateKey();

        if (!key.contains("-----BEGIN EC PRIVATE KEY-----") && !key.contains("-----END EC PRIVATE KEY-----")) {
            try {
                privateKey = ECDSAUtil.getPrivateKey(key);
            } catch (Exception ex) {
                try {
                    privateKey = ECDSAUtil.loadPrivateKey(key);
                } catch (Exception ex2) {
                    log.error("Exception in InvoiceService loadPrivateKey EGSSerialNumber: {} = {}",sellerDTO.getSerialNo(), ex.getStackTrace());
                    ex2.printStackTrace();
                }
            }
        }
        return privateKey;
    }

    private InvoiceSigningResult loadCertificate(String xml, PrivateKey privateKey,SellerDTO sellerDTO) {

        InvoiceSigningResult result = null;
        String certificateStr = Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(sellerDTO.getCertificateStatus()) ? sellerDTO.getCertificateCompliance() : sellerDTO.getCertificateProduction();
        try {
            result = new SigningServiceImpl().signDocument(xml, privateKey, certificateStr, "changeit");
        } catch (Exception ex) {
            log.error("Exception in InvoiceService loadCertificate signDocument EGSSerialNumber: {} = {}",sellerDTO.getSerialNo(), ex.getStackTrace());
            ex.printStackTrace();
        }
        return result;

    }

    public List<InvoiceDTO> getPendingInvoice(){

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        List<InvoiceDTO> pendingInvoices = new ArrayList<>();

        List<InvoiceMaster> invoiceMasterList = invoiceRepository.getPendingInvoices();

        for(InvoiceMaster invoiceMaster:invoiceMasterList) {
            List<InvoiceLine> invoiceLines = invoiceRepository.getInvoiceLinesByInvoiceId(invoiceMaster.getSeqId());
            List<InvoiceLineDTO> invoiceLineDTOs = mapInvoiceLine.invoiceLinesToInvoiceLineDTOs(invoiceLines);
            InvoiceDTO invoiceDTO = mapInvoice.invoiceToInvoiceDTO(invoiceMaster);
            invoiceDTO.setInvoiceLines(invoiceLineDTOs);
            pendingInvoices.add(invoiceDTO);
        }

        return pendingInvoices;
    }

    public InvoiceResponse getInvoiceResponse(String invoiceID)  {
        InvoiceResponse response = new InvoiceResponse();
        try {

            if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
                throw new SellerNotFoundException("System Expired Contact System Administrator");
            }


            InvoiceMaster invoiceMaster = invoiceRepository.getInvoiceById(invoiceID);
            if(null != invoiceMaster) {
                InvoiceLob invoiceLob = invoiceRepository.getInvoiceLobById(invoiceMaster.getSeqId());
                if(null != invoiceLob) {
                    response.setSeqId(String.valueOf(invoiceMaster.getSeqId()));
                    response.setInvoiceId(invoiceMaster.getId());
                    response.setQrcode(invoiceLob.getQrCode());
                    if(isPDFReturn) {
                        response.setPdfContent(invoiceLob.getPdf());
                    }
                    response.setXmlContent(Base64.getEncoder().encodeToString(invoiceLob.getSignedXML().getBytes(StandardCharsets.UTF_8)));

                    if (null != invoiceLob.getQrCode()  && !invoiceLob.getQrCode().isEmpty()) {
                        File qrCodeFile = qrCodeUtil.generateQRCode(invoiceLob.getQrCode(), invoiceMaster.getId());
                        response.setQrcodeFile(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(qrCodeFile)));
                        qrCodeUtil.fileCleanUp(qrCodeFile);
                    }
                    if (null != invoiceLob.getZatcaResponse() && !invoiceLob.getZatcaResponse().isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        ZakatValidationResultModel zakatValidationResult = objectMapper.readValue(invoiceLob.getZatcaResponse(), ZakatValidationResultModel.class);
                        response.setValidationResults(zakatValidationResult);
                    }

                    response.setZatcaStatus(invoiceMaster.getZatcaStatus());
                }
            }
        }catch (IOException ex){
            log.error("IOException in InvoiceService getInvoiceResponse Exception ID: {} = {}",invoiceID, ex.getStackTrace());
        }

        return response;
    }


    @Scheduled(cron = "${CRON_SIMPLIFIED_ISSUE}")
    private void jobReportSimplified(){

        log.info("jobReportSimplified at: {}",LocalDate.now());
        sendSimplifiedInvoiceToZatca();

    }

    @Scheduled(cron ="${CRON_STANDARD_ISSUE}")
    private void jobReportStandard(){

        log.info("jobReportStandard at: {}",LocalDate.now());
        sendStandardInvoiceToZatca();

    }

    private void createEmailEntry(Email email){

        log.info("Invoice Service createEmailEntry: {}",email);
        emailRepository.createEmail(email);
    }

    public String getPDF(String invoiceID){
        log.info("InvoiceService getPDF invoiceID: {}",invoiceID);
        return invoiceRepository.getPDFromLOB(invoiceID);

    }

    public InvoiceDTOWrapper getInvoiceByInvoiceID( String InvoiceID) {
        log.info("Invoice Service getInvoiceByInvoiceID: InvoiceID: {}",InvoiceID);

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        InvoiceMaster invoiceMaster = invoiceRepository.getInvoiceById(InvoiceID);

        List<InvoiceLine> invoiceLines = invoiceRepository.getInvoiceLinesByInvoiceId(invoiceMaster.getSeqId());

        InvoiceDTO  invoiceDTO = mapInvoice.invoiceToInvoiceDTO(invoiceMaster);
        invoiceDTO.setInvoiceLines(mapInvoiceLine.invoiceLinesToInvoiceLineDTOs(invoiceLines));

        InvoiceDTOWrapper invoiceDTOWrapper = new InvoiceDTOWrapper();
        invoiceDTOWrapper.setInvoiceDTO(invoiceDTO);
        return invoiceDTOWrapper;
    }

    public InvoiceResponse retryInvoice(CredentialDTO credentialDTO,InvoiceDTO invoiceDTO) {
        {
            InvoiceResponse response = new InvoiceResponse();
            try {

                if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
                    throw new SellerNotFoundException("System Expired Contact System Administrator");
                }

                if (CommonUtils.isNullOrEmptyString(invoiceDTO.getCurrency())) {
                    invoiceDTO.setCurrency("SAR");
                }

                if (invoiceDTO.getFxRate() == 0) {
                    invoiceDTO.setFxRate(1);
                }

                if (invoiceDTO.getCurrency().equalsIgnoreCase("SAR")) {
                    invoiceDTO.setTaxSAR(invoiceDTO.getTotalVAT());
                    invoiceDTO.setTotalSAR(invoiceDTO.getTaxInclusiveAmount());
                }

                Result validationResult = new Result();

                String xml = xmlGenerator.validateRequestViaXML(invoiceDTO);

                log.info("before validation XML >>>> " + xml);

                try {
                    File f = File.createTempFile("signedInvoice", ".xml");
                    Files.write(Path.of(f.getPath()), xml.getBytes(StandardCharsets.UTF_8));
                    validationResult = CommonUtils.validateInvoice(f);
                    f.delete();
                } catch (Exception e) {
                    log.error("Exception in getInvoiceResponse Zatca Validation: {}", e.getStackTrace());
                    validationResult.setValid(false);
                    e.printStackTrace();
                }

                if (validationResult.isValid()) {

                    SellerDTO sellerDTO = sellerService.getSellerByVatAndSerial();

                    InvoiceMaster invoiceMaster = mapInvoice.invoiceDTOToInvoice(invoiceDTO);
                    invoiceMaster.setStatus(Constants.CREATED_STATUS);
                    invoiceMaster = invoiceRepository.updateInvoiceMaster(invoiceMaster);
                    invoiceMaster = invoiceRepository.getInvoiceById(invoiceMaster.getId());
                    long seqId = invoiceMaster.getSeqId();
                    response.setSeqId(String.valueOf(seqId));
                    response.setInvoiceId(invoiceMaster.getId());
                    updateInvoiceLine(invoiceDTO.getInvoiceLines(), seqId);

                    List<InvoiceLine> invoiceLines = invoiceRepository.getInvoiceLinesByInvoiceId(invoiceMaster.getSeqId());
                    String previousInvoiceHash = invoiceRepository.getPreviousInvocieHashForFailureInvoice(invoiceMaster.getSeqId(),sellerDTO);

                    xml = xmlGenerator.generateXML(invoiceMaster, invoiceLines, previousInvoiceHash);
                    invoiceMaster.setXml(xml);

                    PrivateKey privateKey = loadPrivateKey(sellerDTO);

                    InvoiceSigningResult result = loadCertificate(xml, privateKey,sellerDTO);

                    SAXReader xmlReader = new SAXReader();
                    org.dom4j.Document doc = xmlReader.read(new ByteArrayInputStream(result.getSingedXML().getBytes(StandardCharsets.UTF_8)));

                    xml = doc.getRootElement().asXML();

                    invoiceMaster.setStatus(Constants.INPROCESS_STATUS);
                    invoiceMaster.setSignedXML(xml);
                    invoiceMaster.setQrCode(result.getQrCode());
                    invoiceMaster.setInvocieHash(result.getInvoiceHash());
                    invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.INPROCESS_STATUS);

                    invoiceMaster.setCertificateStatus(sellerDTO.getCertificateStatus());
                    if(Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(invoiceMaster.getCertificateStatus())) {
                        invoiceMaster.setCertificateKey(sellerDTO.getKeyCompliance());
                        invoiceMaster.setCertificate(sellerDTO.getCertificateCompliance());
                    }else {
                        invoiceMaster.setCertificateKey(sellerDTO.getKeyProduction());
                        invoiceMaster.setCertificate(sellerDTO.getCertificateProduction());
                    }

                    invoiceRepository.updateInvoiceLOBS(invoiceMaster);

                    invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);
                    invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PENDING_STATUS);

                    if("Y".equalsIgnoreCase(sellerDTO.getZatcaFlag()))
                    {
                        ZakatApplicationRequestDTO zakatApplicationRequestDTO = new ZakatApplicationRequestDTO();
                        zakatApplicationRequestDTO.setInvoice(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));
                        zakatApplicationRequestDTO.setInvoiceHash(result.getInvoiceHash());
                        zakatApplicationRequestDTO.setUuid(invoiceMaster.getUuid());

                        invoiceMaster.setCertificateStatus(sellerDTO.getCertificateStatus());
                        if(Constants.COMPLIANCE_CERTIFICATE_STATUS.equalsIgnoreCase(invoiceMaster.getCertificateStatus())) {
                            invoiceMaster.setCertificateKey(sellerDTO.getKeyCompliance());
                            invoiceMaster.setCertificate(sellerDTO.getCertificateCompliance());

                            ZakatComplianceResponseDTO zatcaComplianceResponseDto = null;

                            try {
                                zatcaComplianceResponseDto = zakatService.compliance(zakatApplicationRequestDTO, sellerDTO.getCertificateCompliance(), sellerDTO.getKeyCompliance());
                            }
                            catch(WebClientResponseException ex){

                                log.error("Exception In Invoice Service Zakat Compliance Service Response SEQ ID: {} = {} = {}", seqId, ex.getStatusCode(), ex.getResponseBodyAsString());

                                StringBuffer errorMessageBuffer = new StringBuffer();
                                errorMessageBuffer.append("4").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                                errorMessageBuffer.append(ex.getResponseBodyAsString());

                                invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                                invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                                throw new ZakatException(errorMessageBuffer.toString());

                            }
                            if (null != zatcaComplianceResponseDto) {
                                invoiceMaster.setZatcaStatus(invoiceMaster.getSubType().startsWith("01")?zatcaComplianceResponseDto.getClearanceStatus():zatcaComplianceResponseDto.getReportingStatus());
                                invoiceMaster.setValidationStatus(zatcaComplianceResponseDto.getValidationResults().getStatus());

                                ObjectMapper objectMapper = new ObjectMapper();
                                String zakatValidationResult = objectMapper.writeValueAsString(zatcaComplianceResponseDto.getValidationResults());
                                invoiceMaster.setZatcaResponse(zakatValidationResult);

                                response.setValidationResults(zatcaComplianceResponseDto.getValidationResults());
                                response.setXmlContent(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));
                                response.setZatcaStatus(zatcaComplianceResponseDto.getClearanceStatus());
                            }
                        }else {
                            invoiceMaster.setCertificateKey(sellerDTO.getKeyProduction());
                            invoiceMaster.setCertificate(sellerDTO.getCertificateProduction());

                            if (invoiceMaster.getSubType().startsWith("01"))
                            {

                                ZakatClearanceResponseDTO zakatClearanceResponseDTO = null;

                                try {

                                    zakatClearanceResponseDTO = zakatService.clearance(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());

                                } catch(WebClientResponseException ex){
                                    log.error("Exception In Invoice Service Zakat Clearance Service Response SEQ ID: {} = {} = {}", invoiceMaster.getSeqId(), ex.getStatusCode(), ex.getResponseBodyAsString());
                                    StringBuffer errorMessageBuffer = new StringBuffer();
                                    errorMessageBuffer.append("2").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                                    errorMessageBuffer.append(ex.getResponseBodyAsString());

                                    invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                    invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                    invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                                    invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                                    throw new ZakatException(errorMessageBuffer.toString());
                                }

                                response.setValidationResults(zakatClearanceResponseDTO.getValidationResults());
                                response.setXmlContent(zakatClearanceResponseDTO.getClearedInvoice());
                                response.setZatcaStatus(zakatClearanceResponseDTO.getClearanceStatus());

                                if (null != zakatClearanceResponseDTO.getClearedInvoice()) {

                                    doc = xmlReader.read(new ByteArrayInputStream(Base64.getDecoder().decode(zakatClearanceResponseDTO.getClearedInvoice())));

                                    XPath xpath = DocumentHelper.createXPath("/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
                                    xpath.setNamespaceURIs(CommonUtils.getNameSpacesMap());

                                    result.setQrCode(xpath.selectSingleNode(doc).getText());

                                    invoiceMaster.setSignedXML(doc.getRootElement().asXML());
                                    invoiceMaster.setQrCode(result.getQrCode());

                                }
                                invoiceMaster.setZatcaStatus(zakatClearanceResponseDTO.getClearanceStatus());
                                invoiceMaster.setValidationStatus(zakatClearanceResponseDTO.getValidationResults().getStatus());

                                ObjectMapper objectMapper = new ObjectMapper();
                                String zakatValidationResult = objectMapper.writeValueAsString(zakatClearanceResponseDTO.getValidationResults());
                                invoiceMaster.setZatcaResponse(zakatValidationResult);

                            }
                            else {
                                ZakatReportingResponseDTO zakatReportingResponseDTO = null;

                                try {
                                    zakatReportingResponseDTO = zakatService.reporting(zakatApplicationRequestDTO, sellerDTO.getCertificateProduction(), sellerDTO.getKeyProduction());
                                } catch(WebClientResponseException ex){
                                    log.error("Exception In Invoice Service Zakat Reporting Service Response SEQ ID: {} = {} = {}", invoiceMaster.getSeqId(), ex.getStatusCode(), ex.getResponseBodyAsString());

                                    StringBuffer errorMessageBuffer = new StringBuffer();
                                    errorMessageBuffer.append("1").append("$").append(zakatApplicationRequestDTO.getUuid()).append("$").append(ex.getStatusCode()).append("$").append(ex.getMessage()).append("$");
                                    errorMessageBuffer.append(ex.getResponseBodyAsString());

                                    invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                    invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.FAILED_STATUS);
                                    invoiceMaster.setZatcaResponse(ex.getResponseBodyAsString());
                                    invoiceRepository.updatesInvoiceLOBS(invoiceMaster);

                                    throw new ZakatException(errorMessageBuffer.toString());
                                }

                                invoiceMaster.setZatcaStatus(zakatReportingResponseDTO.getReportingStatus());
                                invoiceMaster.setValidationStatus(zakatReportingResponseDTO.getValidationResults().getStatus());

                                ObjectMapper objectMapper = new ObjectMapper();
                                String zakatValidationResult = objectMapper.writeValueAsString(zakatReportingResponseDTO.getValidationResults());
                                invoiceMaster.setZatcaResponse(zakatValidationResult);
                            }
                        }

                        invoiceRepository.updatesInvoiceLOBS(invoiceMaster);
                        invoiceRepository.updateInvoiceResponse(invoiceMaster);

                        if (Constants.VLD_STS_PASS.equalsIgnoreCase(invoiceMaster.getValidationStatus()) && (
                                invoiceMaster.getZatcaStatus().equalsIgnoreCase(Constants.CLRNC_STS) || invoiceMaster.getZatcaStatus().equalsIgnoreCase(Constants.RPRT_STS))) {
                            invoiceRepository.updateInvoiceStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);
                            invoiceRepository.updateInvoiceLineStatus(invoiceMaster.getSeqId(), Constants.PROCESSED_STATUS);

                        }
                    }

                    response.setXmlContent(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));

                    File qrCodeFile = qrCodeUtil.generateQRCode(result.getQrCode(),invoiceMaster.getId());

                    File imageFile = null;
                    String imagePath = "";
                    if(!CommonUtils.isNullOrEmptyString(sellerDTO.getImage())) {
                        imageFile= imageUtil.generateFile(sellerDTO.getImage());
                        imagePath = imageFile.getPath();
                    }

                    File pdfFile = pdfGenerator.generatePDF(invoiceMaster, invoiceLines, qrCodeFile.getPath(), invoiceMaster.getSignedXML(),imagePath);

                    if(null != pdfFile) {
                        byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
                        String pdf = Base64.getEncoder().encodeToString(fileContent);
                        if(isPDFReturn) {
                            response.setPdfContent(pdf);
                        }
                        invoiceRepository.savePDF(pdf,invoiceMaster.getSeqId());

                        if(isB2BEmailSent && invoiceMaster.getSubType().startsWith("01")  && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                            Email email = new Email();
                            email.setId(invoiceMaster.getId());
                            email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                            createEmailEntry(email);
                        }

                        if(isB2CEmailSent && invoiceMaster.getSubType().startsWith("02")  && !CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
                            Email email = new Email();
                            email.setId(invoiceMaster.getId());
                            email.setBuyerEmail(invoiceMaster.getBuyerEmail());
                            createEmailEntry(email);
                        }

                        if(isB2BArchiveCLoud && invoiceMaster.getSubType().startsWith("01")) {
                            String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                            awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                        }

                        if(isB2CArchiveCLoud && invoiceMaster.getSubType().startsWith("02")) {
                            String egsPrefix = invoiceMaster.getId().substring(0,invoiceMaster.getId().length()-12);
                            awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceMaster.getId());
                        }
                    }

                    response.setQrcode(result.getQrCode());
                    response.setQrcodeFile(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(qrCodeFile)));

                    qrCodeUtil.fileCleanUp(qrCodeFile);
                    if(null != imageFile){
                        imageUtil.fileCleanUp(imageFile);
                    }

                } else {
                    ZakatValidationResultModel validationResults = new ZakatValidationResultModel();
                    validationResults.setStatus("FAIL");

                    List<ZakatInfoModel> errorMessages = new ArrayList<>();

                    if (validationResult.getError() != null && !validationResult.getError().isEmpty()) {
                        for (String err : validationResult.getError().keySet()) {
                            errorMessages.add(new ZakatInfoModel(err, "VALIDATION_ERROR", "INIT_VLD", validationResult.getError().get(err), "ERROR"));
                        }
                    }

                    validationResults.setErrorMessages(errorMessages);
                    response.setValidationResults(validationResults);

                }
            } catch (DocumentException | IOException ex) {
                log.error("Exception in getInvoiceResponse DocumentException IOException SEQ ID: {} = {}",response.getSeqId(), ex.getStackTrace());
                invoiceRepository.updateInvoiceStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
                invoiceRepository.updateInvoiceLineStatus(Long.parseLong(response.getSeqId()), Constants.ERROR_STATUS);
                ex.printStackTrace();

            }
            return response;
        }
    }

    private void updateInvoiceLine(List<InvoiceLineDTO> invoiceLineDTOList, long seqID) {
        try {
            List<InvoiceLine> invoiceLineList = this.mapInvoiceLine.invoiceLineDTOsToInvoiceLines(invoiceLineDTOList);
            for (InvoiceLine invoiceLine : invoiceLineList) {
                invoiceLine.setSeqRef(seqID);
                invoiceLine.setStatus(Constants.CREATED_STATUS);
                this.invoiceRepository.updateInvoiceLine(invoiceLine);
            }
        } catch (Exception ex) {
            log.error("Exception in updateInvoiceLine SEQ ID: {} = {}",seqID, ex.getStackTrace());
            invoiceRepository.updateInvoiceStatus(seqID, Constants.ERROR_STATUS);
            ex.printStackTrace();
        }
    }

    public void sendEmail(Long seqID) {
        log.info("Invoice Service sendEmail: SeqID: {}",seqID);

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        InvoiceMaster invoiceMaster = invoiceRepository.getInvoice(seqID);

        if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerEmail())) {
            Email email = new Email();
            email.setId(invoiceMaster.getId());
            email.setBuyerEmail(invoiceMaster.getBuyerEmail());
            createEmailEntry(email);
        }else{
            throw new BuyerEmailNotFoundException("Buyer Email Not Found");
        }
    }

    public void archiveOnCloud(Long seqID) {
        log.info("Invoice Service archiveOnCloud: SeqID: {}",seqID);

        if(null == Constants.SELLER_EXPIRE_DATE || Constants.SELLER_EXPIRE_DATE.compareTo(LocalDate.now())<0){
            throw new SellerNotFoundException("System Expired Contact System Administrator");
        }

        String pdf = invoiceRepository.getPDF(seqID);
        String invoiceID = invoiceRepository.getInvoiceId(seqID);

        File pdfFile = pdfFileUtil.generateFile(pdf);

        if(null != pdfFile) {
            String egsPrefix = invoiceID.substring(0, invoiceID.length() - 12);
            awss3Service.storeInvoiceToBucket(egsPrefix, pdfFile, invoiceID);
        }

    }
}