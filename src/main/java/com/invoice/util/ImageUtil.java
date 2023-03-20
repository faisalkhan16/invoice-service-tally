package com.invoice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Base64;
import java.util.UUID;

@Component
@Slf4j
public class ImageUtil {

    @Value("${TEMP_FILE_PATH}")
    private String TEMP_FILE_PATH;

    @Value("${TEMP_FILE_DIR}")
    private String TEMP_FILE_DIR;

    private static final String FILE_EXTENSION = ".png";

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


    public File generateFile(String image) {
        try {


            StringBuffer fileName = new StringBuffer();
            fileName.append(getFilePath()).append(FILE_EXTENSION);

            byte[] data = decode(image);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName.toString()));
            outputStream.write(data);
            outputStream.close();

            File imageFile = getFile(fileName.toString());

            return imageFile;
        } catch (Exception e) {
            log.error("Exception in generateFile in ImageUtil :{}", e.getStackTrace());
            return null;
        }
    }

    public static byte[] decode(String base64) throws Exception {
        return Base64.getDecoder().decode(base64.getBytes());
    }

}
