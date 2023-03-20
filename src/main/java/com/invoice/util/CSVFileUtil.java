package com.invoice.util;

import com.invoice.model.InvoiceReport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
public class CSVFileUtil {

    @Value("${TEMP_FILE_PATH}")
    private String TEMP_FILE_PATH;

    @Value("${csv_upload_path}")
    private String TEMP_FILE_DIR;

    private static final String FILE_EXTENSION = ".csv";

    private String getFilePath() {
        StringBuffer brFilePath = new StringBuffer();
        brFilePath.append(TEMP_FILE_PATH).append(TEMP_FILE_DIR).append(File.separator);
        return brFilePath.toString();
    }

    public static String getFileId(){
        return LocalDate.now().toString();
    }

    public LinkedList<InvoiceReport> readCSVFile(String fileName){
        LinkedList<InvoiceReport> invoiceReports = new LinkedList<>();
        try {

            StringBuffer fileNameBfr = new StringBuffer();
            fileNameBfr.append(getFilePath()).append(fileName).append(FILE_EXTENSION);

            try (BufferedReader fileReader = new BufferedReader(
                    new FileReader(String.valueOf(fileNameBfr), StandardCharsets.UTF_8))) {
                CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
                Iterable<CSVRecord> csvRecords = csvParser.getRecords();
                boolean isFirstLine = true;
                for (CSVRecord csvRecord : csvRecords) {
                    if(!isFirstLine) {
                        InvoiceReport invoiceReport = new InvoiceReport();
                        //invoiceReport.setSeqId(Long.valueOf(csvRecord.get(0)));
                        invoiceReport.setId(csvRecord.get(1));
                        invoiceReport.setSubType(csvRecord.get(2));
                        invoiceReport.setBuyerEmail(csvRecord.get(3));
                        invoiceReport.setInvoiceHash(csvRecord.get(4));
                        invoiceReport.setInvoice(csvRecord.get(5));
                        invoiceReport.setUuid(csvRecord.get(6));
                        invoiceReport.setCertificate(csvRecord.get(7));
                        invoiceReport.setSecretKey(csvRecord.get(8));
                        invoiceReport.setCertStatus(csvRecord.get(9));
                        invoiceReport.setQrCode(csvRecord.get(10));
                        invoiceReport.setPdf(csvRecord.get(11));
                        invoiceReport.setVatNumber(csvRecord.get(12));
                        invoiceReport.setSerialNo(csvRecord.get(13));
                        invoiceReport.setInvoiceTimeStamp(convertStringIntoLocalDateTime(csvRecord.get(14)));
                        invoiceReports.add(invoiceReport);
                    }else{
                        isFirstLine = false;
                    }
                }
            }

        }catch (Exception ex){
            log.error("Exception in CSVFIleUtil {}",ex.getMessage());
        }
        return invoiceReports;
    }

    private LocalDateTime convertStringIntoLocalDateTime(String timeStamp){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(timeStamp, formatter);
        return dateTime;
    }
}
