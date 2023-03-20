package com.invoice.service;

import com.invoice.model.InvoiceMaster;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class HeaderFooter extends PdfPageEventHelper
{
    private float width = 0;
    private PdfTemplate total = null;

    private Font normal, normalSmall;
    private InvoiceMaster invoiceMaster;
    private String imageLogoPath;

    public HeaderFooter(InvoiceMaster invoiceMaster,String logoPath)
    {
        try
        {
            this.invoiceMaster = invoiceMaster;
            this.imageLogoPath = logoPath;

            this.normal = new Font(BaseFont.createFont("/trado.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 8);
            this.normalSmall = new Font(BaseFont.createFont("/trado.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 6);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onOpenDocument(PdfWriter writer, Document document)
    {
        try
        {
            total = writer.getDirectContent().createTemplate(30,12);
            width = PageSize.A3.getWidth() - 20;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onStartPage(PdfWriter writer, Document document)
    {
        try
        {
            BaseFont bf = BaseFont.createFont("/trado.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font h1Font = new Font(bf, 14f, Font.BOLD);

            Font uFont = new Font(bf, 10f);
            uFont.setStyle("border,underline");

            BaseColor bgcolor = new BaseColor(17, 119, 44);

            PdfPTable table = new PdfPTable(5);
            table.setTotalWidth(width);

            String titleArabic = "فاتورة ضريبية";
            String titleEnglish = "Tax Invoice";

            if(invoiceMaster.getSubType().startsWith("02"))
            {
                titleArabic = "فاتورة ضريبية مبسطة";
                titleEnglish = "Simplified Tax Invoice";
            }

            if(invoiceMaster.getType().equals("381"))
            {
                titleArabic = "اشعار دائن";
                titleEnglish = "Credit Note";
            }
            else if(invoiceMaster.getType().equals("383"))
            {
                titleArabic = "اشعار مدین";
                titleEnglish = "Debit Note";
            }

            Paragraph paragraph = new Paragraph(new Chunk(titleArabic, h1Font)); // arabic title
            paragraph.add(Chunk.NEWLINE);
            paragraph.add(new Chunk(titleEnglish, h1Font));

            PdfPCell cell = null;

            try{

                Image logoImage = Image.getInstance(imageLogoPath);
                logoImage.scaleAbsolute(60f, 30f);

                cell = new PdfPCell(logoImage,false);
                cell.setPadding(3f);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(bgcolor);

                table.addCell(cell);

            }
            catch(Exception e)
            {
                Paragraph emptyText = new Paragraph("");
                cell = new PdfPCell(emptyText);
                cell.setPadding(3f);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(bgcolor);
                table.addCell(cell);
            }

            cell = new PdfPCell(paragraph);
            cell.setPaddingRight(10f);
            cell.setBorder(0);
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(4);

            table.addCell(cell);

            table.writeSelectedRows(0, 10, 10, PageSize.A3.getHeight() - 10, writer.getDirectContent());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onEndPage(PdfWriter pdfWriter, Document document)
    {
        try
        {
            BaseColor bgcolor = new BaseColor(17, 119, 44);

            PdfPTable table = new PdfPTable(3);
            table.setTotalWidth(width);
            table.setWidths(new int[]{24, 24, 2});
            table.getDefaultCell().setFixedHeight(10);
            table.getDefaultCell().setBorder(Rectangle.TOP);

            PdfPCell cell = new PdfPCell();
            cell.setBorder (0);
            cell.setBorderWidthTop (1);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPhrase(new Phrase("Website: https://www.example.com, Phone: +966551231234, Email: example@hotmai.com", normalSmall));
            cell.setBorderColor(bgcolor);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder (0);
            cell.setBorderWidthTop (1);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPhrase(new Phrase(String.format("Page %d of", pdfWriter.getPageNumber()), normal));
            cell.setBorderColor(bgcolor);
            table.addCell(cell);

            cell = new PdfPCell(Image.getInstance(total));
            cell.setBorder (0);
            cell.setBorderWidthTop (1);
            cell.setBorderColor(bgcolor);
            table.addCell(cell);

            table.writeSelectedRows(0, 10, 10, 25, pdfWriter.getDirectContent());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onCloseDocument(PdfWriter writer, Document document)
    {
        try
        {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber()), normal), 2, 2, 0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
