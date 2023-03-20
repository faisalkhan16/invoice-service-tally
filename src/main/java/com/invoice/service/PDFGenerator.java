package com.invoice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceMaster;
import com.invoice.util.Constants;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class PDFGenerator {

    @Value("${upload_path}")
    private String upload_path;

    @PostConstruct
    public void init() {

        Path path = Paths.get(upload_path);
        File filePath = new File(upload_path);

        if (!Files.exists(path)) {
            filePath.mkdir();
        }

    }

    public File generatePDF(InvoiceMaster invoiceMaster, List<InvoiceLine> invoiceLines, String qrCodeFilePath, String xml, String logoPath) {
        try {
            String issueDate = invoiceMaster.getIssueDate().toString();
            String issueTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            DecimalFormat amountFormat = new DecimalFormat("#######0.00");


            //generate QR code Finish

            String fileName = new StringBuffer().append(invoiceMaster.getSellerVatNumber())
                    .append("_").append(issueDate.replaceAll("-", "")).append("T").append(issueTime.replaceAll(":", ""))
                    .append("_").append(invoiceMaster.getId()).append(".pdf").toString();

            StringBuffer filePath = new StringBuffer();
            filePath.append(getRootFilePath()).append(invoiceMaster.getSellerVatNumber())
                    .append(File.separator).append(issueDate.replaceAll("-", "")).append(File.separator);

            File file = new File(filePath.toString());
            file.mkdirs();
            filePath.append(fileName);

            OutputStream fos = new FileOutputStream(filePath.toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Document document = new Document(PageSize.A3, 10, 10, 75, 50);
            document.setMarginMirroring(false);

            PdfWriter pdfWriter = PdfWriter.getInstance(document, fos);
            HeaderFooter headerFooter = new HeaderFooter(invoiceMaster,logoPath);
            pdfWriter.setPageEvent(headerFooter);

            BaseColor bgcolorText = new BaseColor(244, 248, 231);
            BaseColor bgcolorHeading = new BaseColor(17, 119, 44);

            BaseFont bf = BaseFont.createFont("/trado.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//            BaseFont bf = BaseFont.createFont("/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font txtFont = new Font(bf, 8f);
            Font h2Font = new Font(bf, 10f, Font.BOLD, BaseColor.WHITE);
            Font h3Font = new Font(bf, 9f, Font.BOLD, bgcolorHeading);

            document.open();

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            String invoiceNoEng = "Invoice #:";
            String invoiceNoArb = "رقم الفاتورة:";

            if (invoiceMaster.getType().equals("381")) {
                invoiceNoArb = "رقم الاشعار:";
                invoiceNoEng = "Credit Note #:";
            } else if (invoiceMaster.getType().equals("383")) {
                invoiceNoArb = "رقم الاشعار:";
                invoiceNoEng = "Debit Note #:";
            }

            Paragraph p = new Paragraph(invoiceNoEng, txtFont);
            PdfPCell cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getId(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph(invoiceNoArb, txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Issue Date:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getIssueDate().format(formatter), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("تاريخ إصدار:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Supply Date:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSupplyDate().format(formatter), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("تاريخ التوريد:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Supply End Date:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph((null != invoiceMaster.getSupplyEndDate()?invoiceMaster.getSupplyEndDate().format(formatter):null), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("تاريخ انتهاء التوريد:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph(new Chunk(Chunk.NEWLINE));
            cell = new PdfPCell(p);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setColspan(6);

            table.addCell(cell);

            //TODO add original invoice reference and issue date for credit and debit note

            //seller/ buyer table start
            //header start
            p = new Paragraph("Seller", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            table.addCell(cell);

            p = new Paragraph("");
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setPadding(3f);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            p = new Paragraph("المورد", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Customer", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setPadding(3f);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            p = new Paragraph("");
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            table.addCell(cell);

            p = new Paragraph("العميل", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
            //header end

            //name start
            p = new Paragraph("Name:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerAName(), txtFont);
            cell = new PdfPCell(p);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("اسم:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Name:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerAName(), txtFont);
            cell = new PdfPCell(p);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("اسم:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            //name end

            //VAT number start
            p = new Paragraph("VAT Registration Number:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerVatNumber(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("رقم التسجيل الضريبي:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("VAT Registration Number:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerVatNumber(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("رقم التسجيل الضريبي:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
            //VAT number end
            //address start
            p = new Paragraph("Building Number:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerBuildingNo(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("رقم المبنى:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Building Number:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerBuildingNo(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("رقم المبنى:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Street:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerStreet(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("شارع:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Street:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerStreet(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("شارع:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("District:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerDistrict(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("الحي:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("District:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerDistrict(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("الحي:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("City:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerCity(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("المدينة:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("City:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerCity(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("المدينة:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Country:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(Constants.COUNTRIES_MAP.get(invoiceMaster.getSellerCountry()), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("البلد:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Country:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(Constants.COUNTRIES_MAP.get(invoiceMaster.getBuyerCountry()), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("البلد:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            /*
            p = new Paragraph("Address:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
            StringBuffer sellerAddress = new StringBuffer();
            sellerAddress.append(invoiceMaster.getSellerBuildingNo()).append(", ").append(invoiceMaster.getSellerStreet()).append(", ");
            sellerAddress.append(invoiceMaster.getSellerDistrict()).append(", ").append(invoiceMaster.getSellerCity());
            sellerAddress.append(invoiceMaster.getSellerPostalCode()).append(" ").append(invoiceMaster.getSellerAdditionalNo());
            p = new Paragraph(sellerAddress.toString(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);
            p = new Paragraph("عنوان:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
            p = new Paragraph("Address:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
            StringBuffer buyerAddress = new StringBuffer();
            buyerAddress.append(invoiceMaster.getBuyerBuildingNo()).append(", ").append(invoiceMaster.getBuyerStreet()).append(", ");
            buyerAddress.append(invoiceMaster.getBuyerDistrict()).append(", ").append(invoiceMaster.getBuyerCity());
            buyerAddress.append(invoiceMaster.getBuyerPostalCode()).append(" ").append(invoiceMaster.getBuyerAdditionalNo());
            p = new Paragraph(buyerAddress.toString(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);
            p = new Paragraph("عنوان:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);*/
            //address end

            //other ID start
            p = new Paragraph("Other ID #:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getSellerId(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("معرف آخر:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Other ID #:", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);

            p = new Paragraph(invoiceMaster.getBuyerIdNumber(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            table.addCell(cell);

            p = new Paragraph("معرف آخر:", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
            //Other ID End
            //seller buyer end

            p = new Paragraph(new Chunk(Chunk.NEWLINE));
            cell = new PdfPCell(p);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setColspan(6);

            table.addCell(cell);

            document.add(table);

            //invoiceLine start
            table = new PdfPTable(8);
            table.setWidthPercentage(100);

            //header line 1 start
            p = new Paragraph("Line Items:", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            cell.setColspan(4);
            table.addCell(cell);

            p = new Paragraph("وصف السلع أو الخدمات ", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            cell.setColspan(4);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
            //header line 1 end

            //header line 2 start
            p = new Paragraph("Nature of Goods or Service", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("تفاصيل السلعة أو الخدمة "));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            p = new Paragraph("Unit Price", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("سعر الوحده"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Quantity", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("كمية"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            p = new Paragraph("Taxable Amount", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("المبلغ الخاضع للضريبة"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            p = new Paragraph("Discount", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("خصم"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);

            p = new Paragraph("Tax Rate", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("معدل الضريبة"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            p = new Paragraph("Tax Amount", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk(" مبلغ الضريبة "));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            p = new Paragraph("Item Subtotal Incl. VAT", txtFont);
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("المجموع متضمنًا ضريبة القيمة المضافة"));

            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            //header line 2 end

            amountFormat = new DecimalFormat("##,###,##0.00");

            for (InvoiceLine lineItem : invoiceLines) {
                //add 8 columns for each row
                p = new Paragraph(lineItem.getName(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                table.addCell(cell);

                p = new Paragraph(amountFormat.format(lineItem.getNetPrice()) + " " + invoiceMaster.getCurrency(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(String.valueOf(lineItem.getQuantity()), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(amountFormat.format(lineItem.getTotalTaxableAmount()) + " " + invoiceMaster.getCurrency(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(amountFormat.format(lineItem.getDiscount()) + " " + invoiceMaster.getCurrency(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(lineItem.getTaxRate() + "%", txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(amountFormat.format(lineItem.getTaxAmount()) + " " + invoiceMaster.getCurrency(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                p = new Paragraph(amountFormat.format(lineItem.getSubTotal()) + " " + invoiceMaster.getCurrency(), txtFont);
                cell = new PdfPCell(p);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
            }

            p = new Paragraph(new Chunk(Chunk.NEWLINE));
            cell = new PdfPCell(p);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setColspan(8);

            table.addCell(cell);

            document.add(table);
            //invoiceLineEnd


            table = new PdfPTable(6);
            table.setWidthPercentage(100);


            PdfPTable nestedTableInvoiceTotal = new PdfPTable(3);
            nestedTableInvoiceTotal.setWidthPercentage(100);

            //header start
            p = new Paragraph("Total amounts:", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("اجمالي المبالغ :", h2Font);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorHeading);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3f);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);
            //header end


            p = new Paragraph("Total (Excluding VAT)", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTotalAmount()) + " " + invoiceMaster.getCurrency(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("الإجمالي (غير شامل ضريبة القيمة المضافة)", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Discount", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getDiscount()) + " " + invoiceMaster.getCurrency(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("خصم", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Total Taxable Amount", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTaxableAmount()) + " " + invoiceMaster.getCurrency(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("إجمالي المبلغ الخاضع للضريبة ", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Total VAT", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTotalVAT()) + " " + invoiceMaster.getCurrency(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("مجموع ضريبة القيمة المضافة ", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Total VAT (SAR)", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTaxSAR()) + " SAR", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("مجموع ضريبة القيمة المضافة (ريال سعودي)", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Total Amount With VAT", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTaxInclusiveAmount()) + " " + invoiceMaster.getCurrency(), txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("المبلغ الإجمالي مع ضريبة القيمة المضافة", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("Total Amount With VAT (SAR)", txtFont);
            cell = new PdfPCell(p);
            cell.setBackgroundColor(bgcolorText);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph(amountFormat.format(invoiceMaster.getTotalSAR())+" SAR", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nestedTableInvoiceTotal.addCell(cell);

            p = new Paragraph("المبلغ الإجمالي مع ضريبة القيمة المضافة (ريال سعودي) ", txtFont);
            cell = new PdfPCell(p);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3f);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBackgroundColor(bgcolorText);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            nestedTableInvoiceTotal.addCell(cell);

            cell = new PdfPCell(nestedTableInvoiceTotal);
            cell.setColspan(4);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);


            Image qrCodeImage = Image.getInstance(qrCodeFilePath);
            qrCodeImage.scaleAbsolute(90f, 90f);

            cell = new PdfPCell(qrCodeImage, false);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setRowspan(6);
            cell.setColspan(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            document.add(table);

            StringBuffer embededFileName = new StringBuffer();
            embededFileName.append(invoiceMaster.getSellerVatNumber()).append("_")
                    .append(issueDate.replaceAll("-","")).append("T").append(issueTime.replaceAll(":",""))
                    .append("_").append(invoiceMaster.getId()).append(".xml");

            PdfDictionary parameters = new PdfDictionary();
            parameters.put(PdfName.MODDATE, new PdfDate());

            // Embeds file to the document
            PdfFileSpecification fileSpec = PdfFileSpecification.fileEmbedded(pdfWriter, null,
                    embededFileName.toString(), xml.getBytes(),  "application/xml", parameters, 0);

            fileSpec.put(new PdfName("AFRelationship"), new PdfName("Data"));

            pdfWriter.addFileAttachment(embededFileName.toString(), fileSpec);

            document.close();
            fos.close();

            File pdfFile = new File(filePath.toString());

            return pdfFile;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception in generatePDF : {}", ex.getMessage());
        }
        return null;
    }

    private String getRootFilePath() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(upload_path).append(File.separator);
        return filePath.toString();
    }
}
