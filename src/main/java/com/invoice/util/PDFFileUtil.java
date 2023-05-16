package com.invoice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Component
@Slf4j
public class PDFFileUtil {

    @Value("${TEMP_FILE_PATH}")
    private String TEMP_FILE_PATH;

    @Value("${TEMP_FILE_DIR}")
    private String TEMP_FILE_DIR;

    private static final String FILE_EXTENSION = ".pdf";


    @PostConstruct
    public void init() {

        File directory = new File(TEMP_FILE_PATH.concat(TEMP_FILE_DIR));
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private String getFilePath() {
        StringBuffer brFilePath = new StringBuffer();
        brFilePath.append(TEMP_FILE_PATH).append(TEMP_FILE_DIR).append(File.separator).append(getFileId());
        return brFilePath.toString();
    }

    public void fileCleanUp(File imageFile){
        if (imageFile.exists()) {
            imageFile.delete();
        }
    }

    public static String getFileId(){
        return getUUID();
    }

    public static String getUUID(){
        UUID fileId = UUID.randomUUID();
        return fileId.toString();
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }


    public File generateFile(String pdf) {
        try {


            StringBuffer fileName = new StringBuffer();
            fileName.append(getFilePath()).append(FILE_EXTENSION);

            byte[] data = decode(pdf);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName.toString()));
            outputStream.write(data);
            outputStream.close();

            File pdfFile = getFile(fileName.toString());

            return pdfFile;
        } catch (Exception e) {
            log.error("Exception in generateFile in ImageUtil :{}", e.getStackTrace());
            return null;
        }
    }

    private String getFilePath(String invoiceID) {
        StringBuffer brFilePath = new StringBuffer();
        brFilePath.append(TEMP_FILE_PATH).append(TEMP_FILE_DIR).append(File.separator).append(invoiceID).append("-").append(LocalDate.now());
        return brFilePath.toString();
    }
    public File generateFile(String pdf,String invoiceID) {
        try {
            log.info("PDFUtil generateFile: {}",invoiceID);
            StringBuffer fileName = new StringBuffer();
            fileName.append(getFilePath(invoiceID)).append(FILE_EXTENSION);

            byte[] data = decode(pdf);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName.toString()));
            outputStream.write(data);

            File pdfFile = getFile(fileName.toString());

            return pdfFile;
        } catch (Exception e) {
            log.error("Exception in generateFile in PDFUtil invoiceID: {} :{}",invoiceID, e.getStackTrace());
            return null;
        }
    }

    public static byte[] decode(String base64) throws Exception {
        return Base64.getDecoder().decode(base64.getBytes());
    }

    public File savePDF(InputStream inputStream) {
        try {

            StringBuffer fileName = new StringBuffer();
            fileName.append(getFilePath()).append(FILE_EXTENSION);
            File targetFile = new File(fileName.toString());
            copyInputStreamToFile(inputStream, targetFile);
            File pdfFile = getFile(fileName.toString());

            return pdfFile;
        } catch (Exception e) {
            log.error("Exception in savePDF in FileUtil :{}", e.getMessage());
            return null;
        }
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (OutputStream output = new FileOutputStream(file, false)) {
            inputStream.transferTo(output);
        }

    }

    public File convertMultiPartToFile(MultipartFile multipartFile) {
        File convFile = new File(getFilePath() + multipartFile.getOriginalFilename());
        try {
            convFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(convFile);
            fileOutputStream.write(multipartFile.getBytes());
            fileOutputStream.close();
        } catch (Exception ex) {
            log.error("Error in ImageUtil convertMultiPartToFile :", ex);
        }
        return convFile;
    }
}
