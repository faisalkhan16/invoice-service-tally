package com.invoice.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AWSS3Configuration {


    @Bean
    public AmazonS3 amazonS3ServiceWithCredentials(@Value("${AWS_ACCESS_KEY}") String awsAccessKey,
                                                   @Value("${AWS_SECRET_KEY}") String awsSecretKey) {
        log.info("configuring aws s3 with credentials and region");
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                .withRegion(Regions.US_EAST_1).build();
    }

}
