package com.invoice.service;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Service
@Slf4j
public class AWSS3Service {

    @Value("${AWS_INVOICE_BUCKET_NAME}")
    private String awsInvoceBucketName;

    private AmazonS3 amazonS3;

    public AWSS3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String storeInvoiceToBucket(String egsPrefix,File invoicePDF,String invoiceID) {
        String awsS3URL = StringUtils.EMPTY;
        try {
            log.info("Saving pdf in S3 id: {}",invoiceID);

            StringBuffer fileName = new StringBuffer();
            fileName.append(egsPrefix).append("/");
            fileName.append(invoiceID).append(LocalDateTime.now()).append(".pdf");

            AmazonS3 s3 = this.amazonS3;
            s3.putObject(awsInvoceBucketName, fileName.toString(),invoicePDF);
            awsS3URL = s3.getUrl(awsInvoceBucketName, fileName.toString()).toExternalForm();
        }catch (Exception ex){
            log.error("Exception in AWSS3Service storeInvoiceToBucket id: {} Exception: {}",invoiceID,ex.getMessage());
        }
        return awsS3URL;
    }

}
