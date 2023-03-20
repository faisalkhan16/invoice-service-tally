package com.invoice.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

@Component
@Slf4j
public class QRCodeUtil {

    @Value("${TEMP_FILE_PATH}")
    private String generatedQRCodePath;

    @Value("${TEMP_FILE_DIR}")
    private String generatedQRCodeDir;

    private static final String FILE_EXTENSION = ".png";

    private static final String FILE_FORMAT = "png";


    @PostConstruct
    public void init() {

        File directory = new File(generatedQRCodePath.concat(generatedQRCodeDir));
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public File generateQRCode(String qrCodeData,String invoiceID) {
        String imageFileName = getFileName(invoiceID);
        return createQRCodeImage(qrCodeData, imageFileName);
    }

    private File createQRCodeImage(String qrCodeData, String qrCodeFileName) {

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);
            MatrixToImageConfig config = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            File qrCodeFile = getQRCodeFile(qrCodeFileName);
            ImageIO.write(qrImage, FILE_FORMAT, qrCodeFile);

            return qrCodeFile;
        } catch (Exception e) {
            log.error("Unable to create image for item::{}", qrCodeData);
            e.printStackTrace();
            return null;
        }
    }

    private String getFilePath() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(generatedQRCodePath).append(generatedQRCodeDir).append(File.separator);
        return filePath.toString();
    }

    private String getFileName(String invoiceID) {
        StringBuffer fileName = new StringBuffer();
        fileName.append(invoiceID).append("-").append(UUID.randomUUID()).append(FILE_EXTENSION);
        return fileName.toString();
    }


    private File getQRCodeFile(String qrCodeFileName) {
        return new File(getFilePath() + qrCodeFileName);
    }

    public void fileCleanUp(File qrCodeFile){
        if (qrCodeFile.exists()) {
            qrCodeFile.delete();
        }
    }

}