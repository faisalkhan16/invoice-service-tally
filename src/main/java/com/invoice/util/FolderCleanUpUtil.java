package com.invoice.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;

@Component
@Slf4j
public class FolderCleanUpUtil {


    @Value("${TEMP_FILE_PATH}")
    private String generatedQRCodePath;

    @Value("${TEMP_FILE_DIR}")
    private String generatedQRCodeDir;

    private String getFilePath() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(generatedQRCodePath).append(generatedQRCodeDir).append(File.separator);
        return filePath.toString();
    }

    public void cleanTempDirectory() {

        try {
            File directory = new File(getFilePath());
            FileUtils.cleanDirectory(directory);
        }
        catch (Exception ex){
            log.info("Exception in FolderCleanUpUtil cleanTempDirectory at: {}",ex.getMessage());
        }
    }

    @Scheduled(cron ="${CRON_FOLDER_CLEANUP}")
    private void jobTempFolderCleanup(){

        log.info("jobTempFolderCleanup at: {}", LocalDate.now());
        cleanTempDirectory();

    }

}
