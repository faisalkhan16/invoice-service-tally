package com.invoice.util;

import com.invoice.dto.PDFGeneratorDTO;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceMaster;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class XMLParser {
    public PDFGeneratorDTO parseXMLInvoice(String invoiceXML) {
        try {
            InvoiceMaster invoiceMaster = new InvoiceMaster();

            Document document = getXmlDocument(invoiceXML);
            Map<String, String> nameSpacesMap = getNameSpacesMap();

            String invoiceId = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:ID");
            ;
            String uuID = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:UUID");

            String issueDate = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:IssueDate");
            String issueTime = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:IssueTime");
            String issueDateTime = issueDate + " " + issueTime;
            LocalDateTime localDateTime = LocalDateTime.parse(issueDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String invoiceType = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:InvoiceTypeCode");
            String subType = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:InvoiceTypeCode/@name");

            String documentCurrency = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:DocumentCurrencyCode");
            String orderReference = "";

            if (this.getNodeXmlValue(document, nameSpacesMap, "/Invoice/cac:OrderReference/cbc:ID") != null) {
                orderReference = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:OrderReference/cbc:ID");
            }

            String contractNo = "";

            if (this.getNodeXmlValue(document, nameSpacesMap, "/Invoice/cac:ContractDocumentReference/cbc:ID") != null) {
                contractNo = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:ContractDocumentReference/cbc:ID");
            }

            String invoiceCounterValue = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AdditionalDocumentReference[cbc:ID='ICV']/cbc:UUID");
            String previousInvoiceHash = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AdditionalDocumentReference[cbc:ID='PIH']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
            String qrCode = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");

            String sellerIdType = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyIdentification/cbc:ID/@schemeID");
            String sellerId = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyIdentification/cbc:ID");
            String sellerVatNumber = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme/cbc:CompanyID");
            String sellerEName = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName");
            String sellerAName = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName");
            String sellerBuildingNo = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:BuildingNumber");
            String sellerStreet = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:StreetName");
            String sellerDistrict = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:CitySubdivisionName");
            String sellerAdditionalNo = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:PlotIdentification");
            String sellerPostalCode = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:PostalZone");
            String sellerCity = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:CityName");
            String sellerRegion = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cbc:CountrySubentity");
            String sellerCountry = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode");

            String buyerIdType = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PartyIdentification/cbc:ID/@schemeID");

            String buyerId = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PartyIdentification/cbc:ID");

            String buyerVatNumber = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PartyTaxScheme/cbc:CompanyID");
            String buyerEName = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName");
            String buyerAName = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName");
            String buyerBuildingNo = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:BuildingNumber");
            String buyerStreet = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:StreetName");
            String buyerDistrict = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:CitySubdivisionName");
            String buyerAdditionalNo = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:PlotIdentification");
            String buyerPostalCode = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:PostalZone");
            String buyerCity = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:CityName");
            String buyerRegion = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cbc:CountrySubentity");
            String buyerCountry = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingCustomerParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode");

            String supplyDate = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:Delivery/cbc:ActualDeliveryDate");
            String supplyEndDate = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:Delivery/cbc:LatestDeliveryDate");

            String paymentMeansCode = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:PaymentMeans/cbc:PaymentMeansCode");
            String invoiceNoteReason = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:PaymentMeans/cbc:InstructionNote");
            String paymentTerms = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:PaymentTerms/cbc:Note");

            String totalAmount = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:LegalMonetaryTotal/cbc:LineExtensionAmount");
            String discount = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:LegalMonetaryTotal/cbc:AllowanceTotalAmount");
            String taxableAmount = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:LegalMonetaryTotal/cbc:TaxExclusiveAmount");
            String totalInclVAT = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:LegalMonetaryTotal/cbc:TaxInclusiveAmount");

            //get VAT amount in document currency from TaxTotal which contains TaxSubtotal
            XPath xpath = DocumentHelper.createXPath("/Invoice/cac:TaxTotal");
            xpath.setNamespaceURIs(nameSpacesMap);
            List taxTotals = xpath.selectNodes(document);

            String vatAmount = "";
            String vatAmountSAR = "";

            for (int i = 0; i < taxTotals.size(); i++) {
                Node taxTotal = (DefaultElement) taxTotals.get(i);

                //taxTotal with subtotals contains TaxAmount in document currency
                if (taxTotal.selectSingleNode("./cac:TaxSubtotal") != null) {
                    vatAmount = taxTotal.selectSingleNode("./cbc:TaxAmount").getText();
                } else    //taxTotal without subtotals contains TaxAmount in accounting currency (SAR)
                {
                    vatAmountSAR = taxTotal.selectSingleNode("./cbc:TaxAmount").getText();
                }
            }

            invoiceMaster.setId(invoiceId);
            invoiceMaster.setUuid(uuID);
            invoiceMaster.setIssueDate(localDateTime.toLocalDate());
            invoiceMaster.setType(invoiceType);
            invoiceMaster.setSubType(subType);
            invoiceMaster.setCurrency(documentCurrency);
            invoiceMaster.setPurchaseOrderID(orderReference);
            invoiceMaster.setContractID(contractNo);
            invoiceMaster.setSeqId(Long.parseLong(invoiceCounterValue));
            invoiceMaster.setInvocieHash(previousInvoiceHash); //contains previousInvoiceHash in this case - not the usual current invoice Hash
            invoiceMaster.setQrCode(qrCode);
            invoiceMaster.setSellerId(sellerId);
            invoiceMaster.setSellerIdTyp(sellerIdType);

            invoiceMaster.setSellerVatNumber(sellerVatNumber);
            invoiceMaster.setSellerEName(sellerEName);
            invoiceMaster.setSellerAName(sellerAName);
            invoiceMaster.setSellerBuildingNo(sellerBuildingNo);
            invoiceMaster.setSellerStreet(sellerStreet);
            invoiceMaster.setSellerDistrict(sellerDistrict);
            invoiceMaster.setSellerAdditionalNo(sellerAdditionalNo);
            invoiceMaster.setSellerPostalCode(sellerPostalCode);
            invoiceMaster.setSellerCity(sellerCity);
            invoiceMaster.setSellerRegion(sellerRegion);
            invoiceMaster.setSellerCountry(sellerCountry);
            invoiceMaster.setBuyerIdTyp(buyerIdType);
            invoiceMaster.setBuyerIdNumber(buyerId);
            invoiceMaster.setBuyerVatNumber(buyerVatNumber);
            invoiceMaster.setBuyerEName(buyerEName);
            invoiceMaster.setBuyerAName(buyerAName);
            invoiceMaster.setBuyerBuildingNo(buyerBuildingNo);
            invoiceMaster.setBuyerStreet(buyerStreet);
            invoiceMaster.setBuyerDistrict(buyerDistrict);
            invoiceMaster.setBuyerAdditionalNo(buyerAdditionalNo);
            invoiceMaster.setBuyerPostalCode(buyerPostalCode);
            invoiceMaster.setBuyerCity(buyerCity);
            invoiceMaster.setBuyerRegion(buyerRegion);
            invoiceMaster.setBuyerCountry(buyerCountry);
            invoiceMaster.setSupplyDate(LocalDate.parse(supplyDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            if (!CommonUtils.isNullOrEmptyString(supplyEndDate)) {
                invoiceMaster.setSupplyEndDate(LocalDate.parse(supplyEndDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            invoiceMaster.setPaymentMeansCode(paymentMeansCode);
            invoiceMaster.setPaymentTerms(paymentTerms);
            invoiceMaster.setInvoiceNoteReason(invoiceNoteReason);

            invoiceMaster.setTotalAmount(Double.parseDouble(totalAmount));
            invoiceMaster.setDiscount(Double.parseDouble(discount));
            invoiceMaster.setTotalVAT(Double.parseDouble(vatAmount));
            invoiceMaster.setTaxSAR(Double.parseDouble(vatAmountSAR));

            invoiceMaster.setTaxableAmount(Double.parseDouble(taxableAmount));
            invoiceMaster.setTaxInclusiveAmount(Double.parseDouble(totalInclVAT));

            invoiceMaster.setFxRate(invoiceMaster.getTaxSAR() / invoiceMaster.getTotalVAT());

            Double totalAmountInSAR = invoiceMaster.getTaxInclusiveAmount() * invoiceMaster.getFxRate();
            invoiceMaster.setTotalSAR(totalAmountInSAR);

            List<InvoiceLine> invoiceLines = new ArrayList<>();

            xpath = DocumentHelper.createXPath("/Invoice/cac:InvoiceLine");
            xpath.setNamespaceURIs(nameSpacesMap);
            List invoiceNodes = xpath.selectNodes(document);

            for (int i = 0; i < invoiceNodes.size(); i++) {
                InvoiceLine invoiceLine = new InvoiceLine();

                Node line = (DefaultElement) invoiceNodes.get(i);

                Document lineDoc = getXmlDocument(line.asXML());

                invoiceLine.setLineId(Integer.parseInt(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cbc:ID")));
                invoiceLine.setQuantity((int) Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cbc:InvoicedQuantity")));
                invoiceLine.setTotalTaxableAmount(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cbc:LineExtensionAmount")));

                if (this.getNodeXmlValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:AllowanceCharge/cbc:Amount") != null) {
                    invoiceLine.setDiscount(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:AllowanceCharge/cbc:Amount")));
                }

                if (this.getNodeXmlValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:TaxTotal/cbc:TaxAmount") != null) {
                    invoiceLine.setTaxAmount(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:TaxTotal/cbc:TaxAmount")));
                }

                if (this.getNodeXmlValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:TaxTotal/cbc:RoundingAmount") != null) {
                    invoiceLine.setSubTotal(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:TaxTotal/cbc:RoundingAmount")));
                }

                invoiceLine.setName(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:Item/cbc:Name"));
                invoiceLine.setItemTaxCategoryCode(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:Item/cac:ClassifiedTaxCategory/cbc:ID"));

                if (!invoiceLine.getItemTaxCategoryCode().equalsIgnoreCase("O")) {
                    invoiceLine.setTaxRate(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:Item/cac:ClassifiedTaxCategory/cbc:Percent")));
                }

                if (this.getNodeXmlValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:Price/cbc:PriceAmount") != null) {
                    invoiceLine.setNetPrice(Double.parseDouble(this.getNodeXmlTextValue(lineDoc, nameSpacesMap, "/InvoiceLine/cac:Price/cbc:PriceAmount")));
                }

                for (int j = 0; j < taxTotals.size(); j++) {
                    Node taxTotal = (DefaultElement) taxTotals.get(j);

                    //taxTotal with subtotals contains TaxAmount in document currency
                    if (taxTotal.selectSingleNode("./cac:TaxSubtotal") != null) {
                        //TODO check category of current item and set
                        String exemptionReasonCode = null;
                        String exemptionReasonText = null;
                        try {
                            exemptionReasonCode = taxTotal.selectSingleNode("./cac:TaxSubtotal/cac:TaxCategory[cbc:ID='" + invoiceLine.getItemTaxCategoryCode() + "']/cbc:TaxExemptionReasonCode").getText();
                        } catch (Exception e) {

                        }

                        try {
                            exemptionReasonText = taxTotal.selectSingleNode("./cac:TaxSubtotal/cac:TaxCategory[cbc:ID='" + invoiceLine.getItemTaxCategoryCode() + "']/cbc:TaxExemptionReason").getText();
                        } catch (Exception e) {

                        }

                        invoiceLine.setExemptionReasonCode(exemptionReasonCode);
                        invoiceLine.setExemptionReasonText(exemptionReasonText);
                    }
                }

                invoiceLines.add(invoiceLine);
            }


            return new PDFGeneratorDTO(invoiceMaster, invoiceLines, invoiceXML, qrCode);
        } catch (Exception e) {
            log.error("Exception in XMLParser");
            e.printStackTrace();
            return null;
        }
    }

    private Document getXmlDocument(String xmlDocument) throws SAXException, DocumentException {
        SAXReader xmlReader = new SAXReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = xmlReader.read(new ByteArrayInputStream(xmlDocument.getBytes(StandardCharsets.UTF_8)));
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        return doc;
    }

    private Map<String, String> getNameSpacesMap() {
        Map<String, String> nameSpaces = new HashMap();
        nameSpaces.put("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
        nameSpaces.put("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
        nameSpaces.put("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
        nameSpaces.put("sig", "urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
        nameSpaces.put("sac", "urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
        nameSpaces.put("sbc", "urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");
        nameSpaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        nameSpaces.put("xades", "http://uri.etsi.org/01903/v1.3.2#");
        return nameSpaces;
    }

    private String getNodeXmlValue(Document document, Map<String, String> nameSpaces, String attributeXpath) {
        XPath xpath = DocumentHelper.createXPath(attributeXpath);
        xpath.setNamespaceURIs(nameSpaces);
        Node node = xpath.selectSingleNode(document);
        return node != null ? node.asXML() : null;
    }

    private String getNodeXmlTextValue(Document document, Map<String, String> nameSpaces, String attributeXpath) {
        try {
            XPath xpath = DocumentHelper.createXPath(attributeXpath);
            xpath.setNamespaceURIs(nameSpaces);
            return xpath.selectSingleNode(document).getText();
        } catch (Exception e) {
            return "";
        }
    }
}