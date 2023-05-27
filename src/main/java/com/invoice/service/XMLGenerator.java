package com.invoice.service;

import com.invoice.dto.InvoiceDTO;
import com.invoice.dto.InvoiceLineDTO;
import com.invoice.model.InvoiceLine;
import com.invoice.model.InvoiceMaster;
import com.invoice.util.CommonUtils;
import com.invoice.mapper.MapInvoiceLine;
import com.invoice.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class XMLGenerator
{

    private final MapInvoiceLine mapInvoiceLine;

    public String generateXML(InvoiceMaster invoiceMaster, List<InvoiceLine> invoiceLines, String previousInvoiceHash)
    {
        try
        {
            DecimalFormat amountFormat = new DecimalFormat("#######0.00");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String issueTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            String docCurrency = invoiceMaster.getCurrency();
            String taxCurrency = "SAR";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Element xmlElement = null;
            Element childElement = null;
            Element subChildElement = null;
            Element subChildElement1 = null;
            Element subChildElement2 = null;

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Invoice");

            rootElement.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
            rootElement.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            rootElement.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            rootElement.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");

            xmlElement = createUBLExtensions(doc);
            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:ProfileID");
            xmlElement.appendChild(doc.createTextNode("reporting:1.0"));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:ID");
            xmlElement.appendChild(doc.createTextNode(invoiceMaster.getId()));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:UUID");
            xmlElement.appendChild(doc.createTextNode(invoiceMaster.getUuid()));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:IssueDate");
            xmlElement.appendChild(doc.createTextNode(invoiceMaster.getIssueDate().format(formatter)));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:IssueTime");
            xmlElement.appendChild(doc.createTextNode(issueTime));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:InvoiceTypeCode");
            xmlElement.appendChild(doc.createTextNode(invoiceMaster.getType()));

            xmlElement.setAttribute("name", invoiceMaster.getSubType());

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:DocumentCurrencyCode");
            xmlElement.appendChild(doc.createTextNode(docCurrency));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:TaxCurrencyCode");
            xmlElement.appendChild(doc.createTextNode(taxCurrency));

            rootElement.appendChild(xmlElement);

            if(invoiceMaster.getType().equals("388"))
            {
                xmlElement = doc.createElement("cbc:LineCountNumeric");
                xmlElement.appendChild(doc.createTextNode(String.valueOf(invoiceLines.size())));

                rootElement.appendChild(xmlElement);
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getPurchaseOrderID()))
            {
                xmlElement = doc.createElement("cac:OrderReference");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(invoiceMaster.getPurchaseOrderID()));
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            if(!invoiceMaster.getType().equals("388"))
            {
                xmlElement = doc.createElement("cac:BillingReference");

                childElement = doc.createElement("cac:InvoiceDocumentReference");

                subChildElement = doc.createElement("cbc:ID");
                subChildElement.appendChild(doc.createTextNode(invoiceMaster.getOriginalInvoiceId()));

                childElement.appendChild(subChildElement);
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getContractID()))
            {
                xmlElement = doc.createElement("cac:ContractDocumentReference");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(invoiceMaster.getContractID()));
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            //invoice counter start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("ICV"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:UUID");
            childElement.appendChild(doc.createTextNode(String.valueOf(invoiceMaster.getSeqId())));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //invoice counter end

            //previous invoice hash start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("PIH"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cac:Attachment");

            subChildElement = doc.createElement("cbc:EmbeddedDocumentBinaryObject");
            subChildElement.setAttribute("mimeCode","text/plain");

            subChildElement.appendChild(doc.createTextNode(previousInvoiceHash));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //previous invoice hash end

            //QR Code hash start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("QR"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cac:Attachment");

            subChildElement = doc.createElement("cbc:EmbeddedDocumentBinaryObject");
            subChildElement.setAttribute("mimeCode","text/plain");
            subChildElement.appendChild(doc.createTextNode(""));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //QR Code end

            //signature start
            xmlElement = doc.createElement("cac:Signature");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:Invoice"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:SignatureMethod");
            childElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:dsig:enveloped:xades"));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //signature end

            //seller start
            Element accountingSupplierParty = doc.createElement("cac:AccountingSupplierParty");

            xmlElement = doc.createElement("cac:Party");

            //party identification start
            childElement = doc.createElement("cac:PartyIdentification");

            subChildElement = doc.createElement("cbc:ID");
            subChildElement.setAttribute("schemeID", invoiceMaster.getSellerIdTyp());
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerId()));

            childElement.appendChild(subChildElement);
            xmlElement.appendChild(childElement);
            //party identification end

            //postal address start
            childElement = doc.createElement("cac:PostalAddress");

            subChildElement = doc.createElement("cbc:StreetName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerStreet()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:BuildingNumber");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerBuildingNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PlotIdentification");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerAdditionalNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CitySubdivisionName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerDistrict()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CityName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerCity()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PostalZone");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerPostalCode()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CountrySubentity");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerRegion()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:Country");

            subChildElement1 = doc.createElement("cbc:IdentificationCode");
            subChildElement1.appendChild(doc.createTextNode(invoiceMaster.getSellerCountry()));

            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //postal address end

            //party taxScheme start
            childElement = doc.createElement("cac:PartyTaxScheme");

            subChildElement = doc.createElement("cbc:CompanyID");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerVatNumber()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:TaxScheme");

            subChildElement1 = doc.createElement("cbc:ID");
            subChildElement1.appendChild(doc.createTextNode("VAT"));
            subChildElement.appendChild(subChildElement1);

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //party taxScheme end

            childElement = doc.createElement("cac:PartyLegalEntity");
            subChildElement = doc.createElement("cbc:RegistrationName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getSellerEName()));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            accountingSupplierParty.appendChild(xmlElement);

            rootElement.appendChild(accountingSupplierParty);
            //seller end

            //customer start
            Element accountingCustomerParty = doc.createElement("cac:AccountingCustomerParty");

            xmlElement = doc.createElement("cac:Party");

            //party identification start
            childElement = doc.createElement("cac:PartyIdentification");

            subChildElement = doc.createElement("cbc:ID");
            subChildElement.setAttribute("schemeID", invoiceMaster.getBuyerIdTyp());
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerIdNumber()));

            childElement.appendChild(subChildElement);
            xmlElement.appendChild(childElement);
            //party identification end

            //postal address start
            childElement = doc.createElement("cac:PostalAddress");

            subChildElement = doc.createElement("cbc:StreetName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerStreet()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:BuildingNumber");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerBuildingNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PlotIdentification");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerAdditionalNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CitySubdivisionName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerDistrict()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CityName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerCity()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PostalZone");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerPostalCode()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CountrySubentity");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerRegion()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:Country");

            subChildElement1 = doc.createElement("cbc:IdentificationCode");
            subChildElement1.appendChild(doc.createTextNode(invoiceMaster.getBuyerCountry()));

            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //postal address end

            //party taxScheme start
            childElement = doc.createElement("cac:PartyTaxScheme");

            if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getBuyerVatNumber()))
            {
                subChildElement = doc.createElement("cbc:CompanyID");
                subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerVatNumber()));
                childElement.appendChild(subChildElement);
            }

            subChildElement = doc.createElement("cac:TaxScheme");

            subChildElement1 = doc.createElement("cbc:ID");
            subChildElement1.appendChild(doc.createTextNode("VAT"));
            subChildElement.appendChild(subChildElement1);

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //party taxScheme end

            childElement = doc.createElement("cac:PartyLegalEntity");
            subChildElement = doc.createElement("cbc:RegistrationName");
            subChildElement.appendChild(doc.createTextNode(invoiceMaster.getBuyerEName()));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            accountingCustomerParty.appendChild(xmlElement);

            rootElement.appendChild(accountingCustomerParty);
            //customer end

            //delivery date start
            xmlElement = doc.createElement("cac:Delivery");

            childElement = doc.createElement("cbc:ActualDeliveryDate");
            childElement.appendChild(doc.createTextNode(invoiceMaster.getSupplyDate().format(formatter)));

            xmlElement.appendChild(childElement);

            if(invoiceMaster.getSupplyEndDate() != null && !invoiceMaster.getSupplyEndDate().equals(invoiceMaster.getSupplyDate()))
            {
                childElement = doc.createElement("cbc:LatestDeliveryDate");
                childElement.appendChild(doc.createTextNode(invoiceMaster.getSupplyEndDate().format(formatter)));

                xmlElement.appendChild(childElement);
            }

            rootElement.appendChild(xmlElement);
            //delivery date end


            if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getPaymentMeansCode())
                    || !CommonUtils.isNullOrEmptyString(invoiceMaster.getInvoiceNoteReason()))
            {
                //Payment means start
                xmlElement = doc.createElement("cac:PaymentMeans");

                if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getPaymentMeansCode()))
                {
                    childElement = doc.createElement("cbc:PaymentMeansCode");
                    childElement.appendChild(doc.createTextNode(invoiceMaster.getPaymentMeansCode()));

                    xmlElement.appendChild(childElement);
                }

                if(!invoiceMaster.getType().equals("388"))
                {
                    childElement = doc.createElement("cbc:InstructionNote");
                    childElement.appendChild(doc.createTextNode(invoiceMaster.getInvoiceNoteReason()));

                    xmlElement.appendChild(childElement);
                }

                rootElement.appendChild(xmlElement);
                //Payment means end
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceMaster.getPaymentTerms())) {
                xmlElement = doc.createElement("cac:PaymentTerms");
                if (!CommonUtils.isNullOrEmptyString(invoiceMaster.getPaymentTerms())) {
                    childElement = doc.createElement("cbc:Note");
                    childElement.appendChild(doc.createTextNode(invoiceMaster.getPaymentTerms()));

                    xmlElement.appendChild(childElement);
                }
                rootElement.appendChild(xmlElement);
            }

            if(invoiceMaster.getDiscount() > 0d)
            {
                xmlElement = doc.createElement("cac:AllowanceCharge");

                childElement = doc.createElement("cbc:ChargeIndicator");
                childElement.appendChild(doc.createTextNode("false"));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:AllowanceChargeReason");
                childElement.appendChild(doc.createTextNode("Discount"));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:Amount");
                childElement.setAttribute("currencyID",docCurrency);
                childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getDiscount())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cac:TaxCategory");

                subChildElement = doc.createElement("cbc:ID");
                subChildElement.appendChild(doc.createTextNode("S"));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:Percent");
                subChildElement.appendChild(doc.createTextNode(String.valueOf(Constants.STANDARD_TAX_RATE)));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cac:TaxScheme");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode("VAT"));
                subChildElement.appendChild(subChildElement1);

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            //start tax total
            xmlElement = doc.createElement("cac:TaxTotal");

            childElement = doc.createElement("cbc:TaxAmount");
            childElement.setAttribute("currencyID", docCurrency);

            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTotalVAT())));

            xmlElement.appendChild(childElement);

            //loop for VAT Breakdowns
            List<InvoiceLine> vatBreakdowns = CommonUtils.getInvoiceBreakDown(invoiceLines, invoiceMaster.getDiscount());

            for (InvoiceLine vatBreakDown: vatBreakdowns)
            {
                //start tax subtotal
                childElement = doc.createElement("cac:TaxSubtotal");

                subChildElement = doc.createElement("cbc:TaxableAmount");
                subChildElement.setAttribute("currencyID",docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(vatBreakDown.getTotalTaxableAmount())));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:TaxAmount");
                subChildElement.setAttribute("currencyID",docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(vatBreakDown.getTaxAmount())));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cac:TaxCategory");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getItemTaxCategoryCode()));
                subChildElement.appendChild(subChildElement1);

                subChildElement1 = doc.createElement("cbc:Percent");
                subChildElement1.appendChild(doc.createTextNode(String.valueOf(vatBreakDown.getTaxRate())));

                subChildElement.appendChild(subChildElement1);

                if(!CommonUtils.isNullOrEmptyString(vatBreakDown.getExemptionReasonCode()))
                {
                    subChildElement1 = doc.createElement("cbc:TaxExemptionReasonCode");
                    subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getExemptionReasonCode()));

                    subChildElement.appendChild(subChildElement1);
                }

                if(!CommonUtils.isNullOrEmptyString(vatBreakDown.getExemptionReasonText()))
                {
                    subChildElement1 = doc.createElement("cbc:TaxExemptionReason");
                    subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getExemptionReasonText()));

                    subChildElement.appendChild(subChildElement1);
                }

                subChildElement1 = doc.createElement("cac:TaxScheme");

                subChildElement2 = doc.createElement("cbc:ID");
                subChildElement2.appendChild(doc.createTextNode("VAT"));
                subChildElement1.appendChild(subChildElement2);

                subChildElement.appendChild(subChildElement1);

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //end tax subtotal
            }

            rootElement.appendChild(xmlElement);
            //end tax total

            //start tax total without subtotals
            xmlElement = doc.createElement("cac:TaxTotal");

            childElement = doc.createElement("cbc:TaxAmount");
            childElement.setAttribute("currencyID", taxCurrency);

            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTotalVAT())));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //end tax total without subtotals

            //start legal monetary total
            xmlElement = doc.createElement("cac:LegalMonetaryTotal");

            childElement = doc.createElement("cbc:LineExtensionAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTotalAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:TaxExclusiveAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTaxableAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:TaxInclusiveAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTaxInclusiveAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:AllowanceTotalAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getDiscount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:PayableAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceMaster.getTaxInclusiveAmount())));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //end legal monetary total

            for (InvoiceLine invl: invoiceLines)
            {
                //invoiceLine start
                xmlElement = doc.createElement("cac:InvoiceLine");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(String.valueOf(invl.getLineId())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:InvoicedQuantity");
                childElement.setAttribute("unitCode", "PCE");
                childElement.appendChild(doc.createTextNode(String.valueOf(invl.getQuantity())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:LineExtensionAmount");
                childElement.setAttribute("currencyID",docCurrency);
                childElement.appendChild(doc.createTextNode(amountFormat.format(invl.getTotalTaxableAmount())));

                xmlElement.appendChild(childElement);

                //line level discount
                if(invl.getDiscount() > 0)
                {
                    childElement = doc.createElement("cac:AllowanceCharge");

                    subChildElement = doc.createElement("cbc:ChargeIndicator");
                    subChildElement.appendChild(doc.createTextNode("false"));
                    childElement.appendChild(subChildElement);

                    subChildElement = doc.createElement("cbc:AllowanceChargeReason");
                    subChildElement.appendChild(doc.createTextNode("discount"));
                    childElement.appendChild(subChildElement);

                    subChildElement = doc.createElement("cbc:Amount");
                    subChildElement.setAttribute("currencyID",docCurrency);
                    subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getDiscount())));
                    childElement.appendChild(subChildElement);

                    xmlElement.appendChild(childElement);
                }

                //line TaxTotal start
                childElement = doc.createElement("cac:TaxTotal");

                subChildElement = doc.createElement("cbc:TaxAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getTaxAmount())));
                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:RoundingAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getSubTotal())));
                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //line TaxTotal End

                //start Item
                childElement = doc.createElement("cac:Item");

                subChildElement = doc.createElement("cbc:Name");
                subChildElement.appendChild(doc.createTextNode(invl.getName()));

                childElement.appendChild(subChildElement);

                //classifiedTaxCategory start
                subChildElement = doc.createElement("cac:ClassifiedTaxCategory");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode(invl.getItemTaxCategoryCode()));

                subChildElement.appendChild(subChildElement1);

                //tax rate must not be provided for out of scope category
                if(!invl.getItemTaxCategoryCode().equalsIgnoreCase("O"))
                {
                    subChildElement1 = doc.createElement("cbc:Percent");
                    subChildElement1.appendChild(doc.createTextNode(String.valueOf(invl.getTaxRate())));

                    subChildElement.appendChild(subChildElement1);
                }

                //TODO check exemptionReasonCode and ExemptionReasonText

                subChildElement1 = doc.createElement("cac:TaxScheme");

                subChildElement2 = doc.createElement("cbc:ID");
                subChildElement2.appendChild(doc.createTextNode("VAT"));
                subChildElement1.appendChild(subChildElement2);

                subChildElement.appendChild(subChildElement1);
                //classifiedTaxCategory end

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //end Item

                //price start
                childElement = doc.createElement("cac:Price");

                subChildElement = doc.createElement("cbc:PriceAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getNetPrice())));
                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //price end

                rootElement.appendChild(xmlElement);
                //invoiceLineEnd
            }

            doc.appendChild(rootElement);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();

            return output.replaceAll(" xmlns=\"\"","");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Element createUBLExtensions(Document doc)
    {
        Element xmlElement = doc.createElement("ext:UBLExtensions");

        try
        {
            Element childElement = null;
            Element subChildElement = null;
            Element subChildElement1 = null;
            Element subChildElement2 = null;

            childElement = doc.createElement("ext:UBLExtension");

            subChildElement = doc.createElement("ext:ExtensionURI");
            subChildElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:dsig:enveloped:xades"));
            childElement.appendChild(subChildElement);

            //extension content start
            subChildElement = doc.createElement("ext:ExtensionContent");

            subChildElement1 = doc.createElement("sig:UBLDocumentSignatures");
            subChildElement1.setAttribute("xmlns:sig","urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
            subChildElement1.setAttribute("xmlns:sac","urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
            subChildElement1.setAttribute("xmlns:sbc","urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");

            subChildElement2 = doc.createElement("sac:SignatureInformation");

            Element signatureId = doc.createElement("cbc:ID");
            signatureId.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:1"));
            subChildElement2.appendChild(signatureId);

            Element referencedSignatureId = doc.createElement("sbc:ReferencedSignatureID");
            referencedSignatureId.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:Invoice"));
            subChildElement2.appendChild(referencedSignatureId);

            //signature start
            Element signature = doc.createElement("ds:Signature");
            signature.setAttribute("xmlns:ds","http://www.w3.org/2000/09/xmldsig#");
            signature.setAttribute("Id","signature");

            //signedInfo start
            Element signedInfo = doc.createElement("ds:SignedInfo");

            Element canonicalizationMethod = doc.createElement("ds:CanonicalizationMethod");
            canonicalizationMethod.setAttribute("Algorithm","http://www.w3.org/2006/12/xml-c14n11");
            signedInfo.appendChild(canonicalizationMethod);

            Element signatureMethod = doc.createElement("ds:SignatureMethod");
            signatureMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            signedInfo.appendChild(signatureMethod);

            Element reference = doc.createElement("ds:Reference");
            reference.setAttribute("Id", "invoiceSignedData");
            reference.setAttribute("URI","");

            //transforms start
            Element transforms = doc.createElement("ds:Transforms");

            Element transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            Element xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::ext:UBLExtensions)"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::cac:Signature)"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::cac:AdditionalDocumentReference[cbc:ID='QR'])"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/2006/12/xml-c14n11");

            transforms.appendChild(transform);

            reference.appendChild(transforms);
            //transforms end

            Element digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");

            reference.appendChild(digestMethod);

            Element digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode(""));

            reference.appendChild(digestValue);

            signedInfo.appendChild(reference);

            reference = doc.createElement("ds:Reference");
            reference.setAttribute("Type","http://www.w3.org/2000/09/xmldsig#SignatureProperties");
            reference.setAttribute("URI","#xadesSignedProperties");

            digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");

            reference.appendChild(digestMethod);

            digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode(""));

            reference.appendChild(digestValue);

            signedInfo.appendChild(reference);

            signature.appendChild(signedInfo);
            //signedInfo end

            Element signatureValue = doc.createElement("ds:SignatureValue");
            signatureValue.appendChild(doc.createTextNode(""));
            signature.appendChild(signatureValue);

            //key info start
            Element keyInfo = doc.createElement("ds:KeyInfo");

            Element x509Data = doc.createElement("ds:X509Data");
            Element x509Certificate = doc.createElement("ds:X509Certificate");
            x509Certificate.appendChild(doc.createTextNode("")); //TODO get certificate from configuration and place here

            x509Data.appendChild(x509Certificate);
            keyInfo.appendChild(x509Data);

            signature.appendChild(keyInfo);
            //key info end
            //object start
            Element object = doc.createElement("ds:Object");

            Element qualifyingProperties = doc.createElement("xades:QualifyingProperties");
            qualifyingProperties.setAttribute("xmlns:xades","http://uri.etsi.org/01903/v1.3.2#");
            qualifyingProperties.setAttribute("Target","signature");

            Element signedProperties = doc.createElement("xades:SignedProperties");
            signedProperties.setAttribute("Id","xadesSignedProperties");

            Element signedSignatureProperties = doc.createElement("xades:SignedSignatureProperties");

            Element signingTime = doc.createElement("xades:SigningTime");
            signingTime.appendChild(doc.createTextNode(""));
            signedSignatureProperties.appendChild(signingTime);

            Element signingCertificate = doc.createElement("xades:SigningCertificate");
            Element cert = doc.createElement("xades:Cert");

            Element certDigest = doc.createElement("xades:CertDigest");

            digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");
            certDigest.appendChild(digestMethod);

            digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode(""));
            certDigest.appendChild(digestValue);

            cert.appendChild(certDigest);

            Element issueSerial = doc.createElement("xades:IssuerSerial");
            Element issuerName = doc.createElement("ds:X509IssuerName");
            issuerName.appendChild(doc.createTextNode("CN=TSZEINVOICE-SubCA-1, DC=extgazt, DC=gov, DC=local"));//TODO
            issueSerial.appendChild(issuerName);

            Element serialNo = doc.createElement("ds:X509SerialNumber");
            serialNo.appendChild(doc.createTextNode(""));//TODO fill certificate serial no here
            issueSerial.appendChild(serialNo);

            cert.appendChild(issueSerial);

            signingCertificate.appendChild(cert);
            signedSignatureProperties.appendChild(signingCertificate);
            signedProperties.appendChild(signedSignatureProperties);
            qualifyingProperties.appendChild(signedProperties);
            object.appendChild(qualifyingProperties);
            signature.appendChild(object);
            //object end

            subChildElement2.appendChild(signature);
            //signature end

            subChildElement1.appendChild(subChildElement2);
            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);
            //extension content end

            xmlElement.appendChild(childElement);

            //UBLExtenstions end
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return xmlElement;
    }

    public String validateRequestViaXML(InvoiceDTO invoiceDTO)
    {
        try
        {
            List<InvoiceLineDTO> invoiceLines = invoiceDTO.getInvoiceLines();

            DecimalFormat amountFormat = new DecimalFormat("#######0.00");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String issueTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            String docCurrency = invoiceDTO.getCurrency();
            String taxCurrency = "SAR";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Element xmlElement = null;
            Element childElement = null;
            Element subChildElement = null;
            Element subChildElement1 = null;
            Element subChildElement2 = null;

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Invoice");

            rootElement.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
            rootElement.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            rootElement.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            rootElement.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");

            xmlElement = createUBLExtensionsSample(doc);
            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:ProfileID");
            xmlElement.appendChild(doc.createTextNode("reporting:1.0"));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:ID");
            xmlElement.appendChild(doc.createTextNode(invoiceDTO.getId()));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:UUID");
            xmlElement.appendChild(doc.createTextNode(String.valueOf(UUID.randomUUID())));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:IssueDate");
            xmlElement.appendChild(doc.createTextNode(LocalDate.now().format(formatter)));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:IssueTime");
            xmlElement.appendChild(doc.createTextNode(issueTime));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:InvoiceTypeCode");
            xmlElement.appendChild(doc.createTextNode(invoiceDTO.getType()));

            xmlElement.setAttribute("name", invoiceDTO.getSubType());

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:DocumentCurrencyCode");
            xmlElement.appendChild(doc.createTextNode(docCurrency));

            rootElement.appendChild(xmlElement);

            xmlElement = doc.createElement("cbc:TaxCurrencyCode");
            xmlElement.appendChild(doc.createTextNode(taxCurrency));

            rootElement.appendChild(xmlElement);

            if(invoiceDTO.getType().equals("388"))
            {
                xmlElement = doc.createElement("cbc:LineCountNumeric");
                xmlElement.appendChild(doc.createTextNode(String.valueOf(invoiceLines.size())));

                rootElement.appendChild(xmlElement);
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getPurchaseOrderID()))
            {
                xmlElement = doc.createElement("cac:OrderReference");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(invoiceDTO.getPurchaseOrderID()));
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            if(!invoiceDTO.getType().equals("388"))
            {
                xmlElement = doc.createElement("cac:BillingReference");

                childElement = doc.createElement("cac:InvoiceDocumentReference");

                subChildElement = doc.createElement("cbc:ID");
                subChildElement.appendChild(doc.createTextNode(invoiceDTO.getOriginalInvoiceId()));

                childElement.appendChild(subChildElement);
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getContractID()))
            {
                xmlElement = doc.createElement("cac:ContractDocumentReference");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(invoiceDTO.getContractID()));
                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            //invoice counter start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("ICV"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:UUID");
            childElement.appendChild(doc.createTextNode("11")); //hard-code counter value as it doesnot need to be validated

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //invoice counter end

            //previous invoice hash start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("PIH"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cac:Attachment");

            subChildElement = doc.createElement("cbc:EmbeddedDocumentBinaryObject");
            subChildElement.setAttribute("mimeCode","text/plain");

            subChildElement.appendChild(doc.createTextNode("NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ=="));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //previous invoice hash end

            //QR Code hash start
            xmlElement = doc.createElement("cac:AdditionalDocumentReference");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("QR"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cac:Attachment");

            subChildElement = doc.createElement("cbc:EmbeddedDocumentBinaryObject");
            subChildElement.setAttribute("mimeCode","text/plain");
            subChildElement.appendChild(doc.createTextNode("ARlBbCBTYWxhbSBTdXBwbGllcyBDby4gTFREAg8zMDAwNTUxODQ0MDAwMDMDFDIwMjEtMDQtMjVUMTU6MzA6MDBaBAcxMDM1LjAwBQYxMzUuMDAGLG1mVkNpcHlaUG1IZzFpU3QreWJSYlJMaFAreGZuSDVmZnNMYXdkaXU2UEk9B1gwVjAQBgcqhkjOPQIBBgUrgQQACgNCAATTAK9lrTVko9rkq6ZYcc9HDRZP4b9S4zA4Km7YXJ+snTVhLkzU0HsmSX9Un8jDhRTOHDKaft8C/uuUY934vuMNCCEAnHTyqYXeVhBdCUO9gq4nX73oEgVZCjZ8STz9QY7Sy1sJIBkN9Q56qQGMZly02uwNYqXPAagxEF1tqxImEczcDbK2"));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //QR Code end

            //signature start
            xmlElement = doc.createElement("cac:Signature");

            childElement = doc.createElement("cbc:ID");
            childElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:Invoice"));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:SignatureMethod");
            childElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:dsig:enveloped:xades"));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //signature end

            //seller start
            Element accountingSupplierParty = doc.createElement("cac:AccountingSupplierParty");

            xmlElement = doc.createElement("cac:Party");

            //party identification start
            childElement = doc.createElement("cac:PartyIdentification");

            subChildElement = doc.createElement("cbc:ID");
            subChildElement.setAttribute("schemeID", invoiceDTO.getSellerIdTyp());
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerId()));

            childElement.appendChild(subChildElement);
            xmlElement.appendChild(childElement);
            //party identification end

            //postal address start
            childElement = doc.createElement("cac:PostalAddress");

            subChildElement = doc.createElement("cbc:StreetName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerStreet()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:BuildingNumber");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerBuildingNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PlotIdentification");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerAdditionalNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CitySubdivisionName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerDistrict()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CityName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerCity()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PostalZone");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerPostalCode()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CountrySubentity");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerRegion()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:Country");

            subChildElement1 = doc.createElement("cbc:IdentificationCode");
            subChildElement1.appendChild(doc.createTextNode(invoiceDTO.getSellerCountry()));

            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //postal address end

            //party taxScheme start
            childElement = doc.createElement("cac:PartyTaxScheme");

            subChildElement = doc.createElement("cbc:CompanyID");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerVatNumber()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:TaxScheme");

            subChildElement1 = doc.createElement("cbc:ID");
            subChildElement1.appendChild(doc.createTextNode("VAT"));
            subChildElement.appendChild(subChildElement1);

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //party taxScheme end

            childElement = doc.createElement("cac:PartyLegalEntity");
            subChildElement = doc.createElement("cbc:RegistrationName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getSellerEName()));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            accountingSupplierParty.appendChild(xmlElement);

            rootElement.appendChild(accountingSupplierParty);
            //seller end

            //customer start
            Element accountingCustomerParty = doc.createElement("cac:AccountingCustomerParty");

            xmlElement = doc.createElement("cac:Party");

            //party identification start
            childElement = doc.createElement("cac:PartyIdentification");

            subChildElement = doc.createElement("cbc:ID");
            subChildElement.setAttribute("schemeID", invoiceDTO.getBuyerIdTyp());
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerIdNumber()));

            childElement.appendChild(subChildElement);
            xmlElement.appendChild(childElement);
            //party identification end

            //postal address start
            childElement = doc.createElement("cac:PostalAddress");

            subChildElement = doc.createElement("cbc:StreetName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerStreet()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:BuildingNumber");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerBuildingNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PlotIdentification");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerAdditionalNo()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CitySubdivisionName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerDistrict()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CityName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerCity()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:PostalZone");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerPostalCode()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cbc:CountrySubentity");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerRegion()));
            childElement.appendChild(subChildElement);

            subChildElement = doc.createElement("cac:Country");

            subChildElement1 = doc.createElement("cbc:IdentificationCode");
            subChildElement1.appendChild(doc.createTextNode(invoiceDTO.getBuyerCountry()));

            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //postal address end

            //party taxScheme start
            childElement = doc.createElement("cac:PartyTaxScheme");

            if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getBuyerVatNumber()))
            {
                subChildElement = doc.createElement("cbc:CompanyID");
                subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerVatNumber()));
                childElement.appendChild(subChildElement);
            }

            subChildElement = doc.createElement("cac:TaxScheme");

            subChildElement1 = doc.createElement("cbc:ID");
            subChildElement1.appendChild(doc.createTextNode("VAT"));
            subChildElement.appendChild(subChildElement1);

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);
            //party taxScheme end

            childElement = doc.createElement("cac:PartyLegalEntity");
            subChildElement = doc.createElement("cbc:RegistrationName");
            subChildElement.appendChild(doc.createTextNode(invoiceDTO.getBuyerEName()));

            childElement.appendChild(subChildElement);

            xmlElement.appendChild(childElement);

            accountingCustomerParty.appendChild(xmlElement);

            rootElement.appendChild(accountingCustomerParty);
            //customer end

            //delivery date start
            xmlElement = doc.createElement("cac:Delivery");

            childElement = doc.createElement("cbc:ActualDeliveryDate");
            childElement.appendChild(doc.createTextNode(invoiceDTO.getSupplyDate().format(formatter)));

            xmlElement.appendChild(childElement);

            if(invoiceDTO.getSupplyEndDate() != null && !invoiceDTO.getSupplyEndDate().equals(invoiceDTO.getSupplyDate()))
            {
                childElement = doc.createElement("cbc:LatestDeliveryDate");
                childElement.appendChild(doc.createTextNode(invoiceDTO.getSupplyEndDate().format(formatter)));

                xmlElement.appendChild(childElement);
            }

            rootElement.appendChild(xmlElement);
            //delivery date end


            if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getPaymentMeansCode())
                    || !CommonUtils.isNullOrEmptyString(invoiceDTO.getInvoiceNoteReason()))
            {
                //Payment means start
                xmlElement = doc.createElement("cac:PaymentMeans");

                if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getPaymentMeansCode()))
                {
                    childElement = doc.createElement("cbc:PaymentMeansCode");
                    childElement.appendChild(doc.createTextNode(invoiceDTO.getPaymentMeansCode()));

                    xmlElement.appendChild(childElement);
                }

                if(!invoiceDTO.getType().equals("388"))
                {
                    childElement = doc.createElement("cbc:InstructionNote");
                    childElement.appendChild(doc.createTextNode(invoiceDTO.getInvoiceNoteReason()));

                    xmlElement.appendChild(childElement);
                }

                rootElement.appendChild(xmlElement);
                //Payment means end
            }

            if(!CommonUtils.isNullOrEmptyString(invoiceDTO.getPaymentTerms())) {
                xmlElement = doc.createElement("cac:PaymentTerms");
                if (!CommonUtils.isNullOrEmptyString(invoiceDTO.getPaymentTerms())) {
                    childElement = doc.createElement("cbc:Note");
                    childElement.appendChild(doc.createTextNode(invoiceDTO.getPaymentTerms()));

                    xmlElement.appendChild(childElement);
                }
                rootElement.appendChild(xmlElement);
            }

            if(invoiceDTO.getDiscount() > 0d)
            {
                xmlElement = doc.createElement("cac:AllowanceCharge");

                childElement = doc.createElement("cbc:ChargeIndicator");
                childElement.appendChild(doc.createTextNode("false"));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:AllowanceChargeReason");
                childElement.appendChild(doc.createTextNode("Discount"));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:Amount");
                childElement.setAttribute("currencyID",docCurrency);
                childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getDiscount())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cac:TaxCategory");

                subChildElement = doc.createElement("cbc:ID");
                subChildElement.appendChild(doc.createTextNode("S"));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:Percent");
                subChildElement.appendChild(doc.createTextNode(String.valueOf(Constants.STANDARD_TAX_RATE)));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cac:TaxScheme");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode("VAT"));
                subChildElement.appendChild(subChildElement1);

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);

                rootElement.appendChild(xmlElement);
            }

            //start tax total
            xmlElement = doc.createElement("cac:TaxTotal");

            childElement = doc.createElement("cbc:TaxAmount");
            childElement.setAttribute("currencyID", docCurrency);

            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTotalVAT())));

            xmlElement.appendChild(childElement);

            //loop for VAT Breakdowns
            List<InvoiceLine> vatBreakdowns = CommonUtils.getInvoiceBreakDown(mapInvoiceLine.invoiceLineDTOsToInvoiceLines(invoiceLines), invoiceDTO.getDiscount());

            for (InvoiceLine vatBreakDown: vatBreakdowns)
            {
                //start tax subtotal
                childElement = doc.createElement("cac:TaxSubtotal");

                subChildElement = doc.createElement("cbc:TaxableAmount");
                subChildElement.setAttribute("currencyID",docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(vatBreakDown.getTotalTaxableAmount())));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:TaxAmount");
                subChildElement.setAttribute("currencyID",docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(vatBreakDown.getTaxAmount())));

                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cac:TaxCategory");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getItemTaxCategoryCode()));
                subChildElement.appendChild(subChildElement1);

                subChildElement1 = doc.createElement("cbc:Percent");
                subChildElement1.appendChild(doc.createTextNode(String.valueOf(vatBreakDown.getTaxRate())));

                subChildElement.appendChild(subChildElement1);

                if(!CommonUtils.isNullOrEmptyString(vatBreakDown.getExemptionReasonCode()))
                {
                    subChildElement1 = doc.createElement("cbc:TaxExemptionReasonCode");
                    subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getExemptionReasonCode()));

                    subChildElement.appendChild(subChildElement1);
                }

                if(!CommonUtils.isNullOrEmptyString(vatBreakDown.getExemptionReasonText()))
                {
                    subChildElement1 = doc.createElement("cbc:TaxExemptionReason");
                    subChildElement1.appendChild(doc.createTextNode(vatBreakDown.getExemptionReasonText()));

                    subChildElement.appendChild(subChildElement1);
                }

                subChildElement1 = doc.createElement("cac:TaxScheme");

                subChildElement2 = doc.createElement("cbc:ID");
                subChildElement2.appendChild(doc.createTextNode("VAT"));
                subChildElement1.appendChild(subChildElement2);

                subChildElement.appendChild(subChildElement1);

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //end tax subtotal
            }

            rootElement.appendChild(xmlElement);
            //end tax total

            //start tax total without subtotals
            xmlElement = doc.createElement("cac:TaxTotal");

            childElement = doc.createElement("cbc:TaxAmount");
            childElement.setAttribute("currencyID", taxCurrency);

            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTotalVAT())));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //end tax total without subtotals

            //start legal monetary total
            xmlElement = doc.createElement("cac:LegalMonetaryTotal");

            childElement = doc.createElement("cbc:LineExtensionAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTotalAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:TaxExclusiveAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTaxableAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:TaxInclusiveAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTaxInclusiveAmount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:AllowanceTotalAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getDiscount())));

            xmlElement.appendChild(childElement);

            childElement = doc.createElement("cbc:PayableAmount");
            childElement.setAttribute("currencyID",docCurrency);
            childElement.appendChild(doc.createTextNode(amountFormat.format(invoiceDTO.getTaxInclusiveAmount())));

            xmlElement.appendChild(childElement);

            rootElement.appendChild(xmlElement);
            //end legal monetary total


            for (InvoiceLineDTO invl: invoiceLines)
            {
                //invoiceLine start
                xmlElement = doc.createElement("cac:InvoiceLine");

                childElement = doc.createElement("cbc:ID");
                childElement.appendChild(doc.createTextNode(String.valueOf(invl.getLineId())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:InvoicedQuantity");
                childElement.setAttribute("unitCode", "PCE");
                childElement.appendChild(doc.createTextNode(String.valueOf(invl.getQuantity())));

                xmlElement.appendChild(childElement);

                childElement = doc.createElement("cbc:LineExtensionAmount");
                childElement.setAttribute("currencyID",docCurrency);
                childElement.appendChild(doc.createTextNode(amountFormat.format(invl.getTotalTaxableAmount())));

                xmlElement.appendChild(childElement);

                //line level discount
                if(invl.getDiscount() > 0)
                {
                    childElement = doc.createElement("cac:AllowanceCharge");

                    subChildElement = doc.createElement("cbc:ChargeIndicator");
                    subChildElement.appendChild(doc.createTextNode("false"));
                    childElement.appendChild(subChildElement);

                    subChildElement = doc.createElement("cbc:AllowanceChargeReason");
                    subChildElement.appendChild(doc.createTextNode("discount"));
                    childElement.appendChild(subChildElement);

                    subChildElement = doc.createElement("cbc:Amount");
                    subChildElement.setAttribute("currencyID",docCurrency);
                    subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getDiscount())));
                    childElement.appendChild(subChildElement);

                    xmlElement.appendChild(childElement);
                }

                //line TaxTotal start
                childElement = doc.createElement("cac:TaxTotal");

                subChildElement = doc.createElement("cbc:TaxAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getTaxAmount())));
                childElement.appendChild(subChildElement);

                subChildElement = doc.createElement("cbc:RoundingAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getSubTotal())));
                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //line TaxTotal End

                //start Item
                childElement = doc.createElement("cac:Item");

                subChildElement = doc.createElement("cbc:Name");
                subChildElement.appendChild(doc.createTextNode(invl.getName()));

                childElement.appendChild(subChildElement);

                //classifiedTaxCategory start
                subChildElement = doc.createElement("cac:ClassifiedTaxCategory");

                subChildElement1 = doc.createElement("cbc:ID");
                subChildElement1.appendChild(doc.createTextNode(invl.getItemTaxCategoryCode()));

                subChildElement.appendChild(subChildElement1);

                //tax rate must not be provided for out of scope category
                if(!invl.getItemTaxCategoryCode().equalsIgnoreCase("O"))
                {
                    subChildElement1 = doc.createElement("cbc:Percent");
                    subChildElement1.appendChild(doc.createTextNode(String.valueOf(invl.getTaxRate())));

                    subChildElement.appendChild(subChildElement1);
                }

                subChildElement1 = doc.createElement("cac:TaxScheme");

                subChildElement2 = doc.createElement("cbc:ID");
                subChildElement2.appendChild(doc.createTextNode("VAT"));
                subChildElement1.appendChild(subChildElement2);

                subChildElement.appendChild(subChildElement1);
                //classifiedTaxCategory end

                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //end Item

                //price start
                childElement = doc.createElement("cac:Price");

                subChildElement = doc.createElement("cbc:PriceAmount");
                subChildElement.setAttribute("currencyID", docCurrency);
                subChildElement.appendChild(doc.createTextNode(amountFormat.format(invl.getNetPrice())));
                childElement.appendChild(subChildElement);

                xmlElement.appendChild(childElement);
                //price end

                rootElement.appendChild(xmlElement);
                //invoiceLineEnd
            }

            doc.appendChild(rootElement);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();

            return output.replaceAll(" xmlns=\"\"","");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Element createUBLExtensionsSample(Document doc)
    {
        Element xmlElement = doc.createElement("ext:UBLExtensions");

        try
        {
            Element childElement = null;
            Element subChildElement = null;
            Element subChildElement1 = null;
            Element subChildElement2 = null;

            childElement = doc.createElement("ext:UBLExtension");

            subChildElement = doc.createElement("ext:ExtensionURI");
            subChildElement.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:dsig:enveloped:xades"));
            childElement.appendChild(subChildElement);

            //extension content start
            subChildElement = doc.createElement("ext:ExtensionContent");

            subChildElement1 = doc.createElement("sig:UBLDocumentSignatures");
            subChildElement1.setAttribute("xmlns:sig","urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
            subChildElement1.setAttribute("xmlns:sac","urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
            subChildElement1.setAttribute("xmlns:sbc","urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");

            subChildElement2 = doc.createElement("sac:SignatureInformation");

            Element signatureId = doc.createElement("cbc:ID");
            signatureId.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:1"));
            subChildElement2.appendChild(signatureId);

            Element referencedSignatureId = doc.createElement("sbc:ReferencedSignatureID");
            referencedSignatureId.appendChild(doc.createTextNode("urn:oasis:names:specification:ubl:signature:Invoice"));
            subChildElement2.appendChild(referencedSignatureId);

            //signature start
            Element signature = doc.createElement("ds:Signature");
            signature.setAttribute("xmlns:ds","http://www.w3.org/2000/09/xmldsig#");
            signature.setAttribute("Id","signature");

            //signedInfo start
            Element signedInfo = doc.createElement("ds:SignedInfo");

            Element canonicalizationMethod = doc.createElement("ds:CanonicalizationMethod");
            canonicalizationMethod.setAttribute("Algorithm","http://www.w3.org/2006/12/xml-c14n11");
            signedInfo.appendChild(canonicalizationMethod);

            Element signatureMethod = doc.createElement("ds:SignatureMethod");
            signatureMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            signedInfo.appendChild(signatureMethod);

            Element reference = doc.createElement("ds:Reference");
            reference.setAttribute("Id", "invoiceSignedData");
            reference.setAttribute("URI","");

            //transforms start
            Element transforms = doc.createElement("ds:Transforms");

            Element transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            Element xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::ext:UBLExtensions)"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::cac:Signature)"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xpath-19991116");

            xPath = doc.createElement("ds:XPath");
            xPath.appendChild(doc.createTextNode("not(//ancestor-or-self::cac:AdditionalDocumentReference[cbc:ID='QR'])"));
            transform.appendChild(xPath);

            transforms.appendChild(transform);

            transform = doc.createElement("ds:Transform");
            transform.setAttribute("Algorithm", "http://www.w3.org/2006/12/xml-c14n11");

            transforms.appendChild(transform);

            reference.appendChild(transforms);
            //transforms end

            Element digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");

            reference.appendChild(digestMethod);

            Element digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode("mfVCipyZPmHg1iSt+ybRbRLhP+xfnH5ffsLawdiu6PI="));

            reference.appendChild(digestValue);

            signedInfo.appendChild(reference);

            reference = doc.createElement("ds:Reference");
            reference.setAttribute("Type","http://www.w3.org/2000/09/xmldsig#SignatureProperties");
            reference.setAttribute("URI","#xadesSignedProperties");

            digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");

            reference.appendChild(digestMethod);

            digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode("M2ZkZWViYTg3OGYwNGQ3ZjhkOGJiNWUyZjlhODViMTc1YTg0MmE4MDFmNjU1MWJhYmYyYWFlMDc4MjRmMGVlOQ=="));

            reference.appendChild(digestValue);

            signedInfo.appendChild(reference);

            signature.appendChild(signedInfo);
            //signedInfo end

            Element signatureValue = doc.createElement("ds:SignatureValue");
            signatureValue.appendChild(doc.createTextNode(""));
            signature.appendChild(signatureValue);

            //key info start
            Element keyInfo = doc.createElement("ds:KeyInfo");

            Element x509Data = doc.createElement("ds:X509Data");
            Element x509Certificate = doc.createElement("ds:X509Certificate");
            x509Certificate.appendChild(doc.createTextNode("MIID3DCCA4KgAwIBAgITbwAAZIQwd/uzGGbr+QABAABkhDAKBggqhkjOPQQDAjBjMRUwEwYKCZImiZPyLGQBGRYFbG9jYWwxEzARBgoJkiaJk/IsZAEZFgNnb3YxFzAVBgoJkiaJk/IsZAEZFgdleHRnYXp0MRwwGgYDVQQDExNUU1pFSU5WT0lDRS1TdWJDQS0xMB4XDTIyMDMxNTA4MjkwNVoXDTIyMDMxNzA4MjkwNVowTzELMAkGA1UEBhMCU0ExEzARBgNVBAoTCmhheWEgeWFnIDMxFzAVBgNVBAsTDmFtbWFuIEJyYW5jaGNoMRIwEAYDVQQDEwkxMjcuMC4wLjEwVjAQBgcqhkjOPQIBBgUrgQQACgNCAATTAK9lrTVko9rkq6ZYcc9HDRZP4b9S4zA4Km7YXJ+snTVhLkzU0HsmSX9Un8jDhRTOHDKaft8C/uuUY934vuMNo4ICKjCCAiYwgYsGA1UdEQSBgzCBgKR+MHwxHTAbBgNVBAQMFDEtaGF5YXwyLTIzNHwzLTcxMTExMR8wHQYKCZImiZPyLGQBAQwPMzAwMDU1MTg0NDAwMDAzMQ0wCwYDVQQMDAQxMTExMREwDwYDVQQaDAhaYXRjYSAxMjEYMBYGA1UEDwwPRm9vZCBCdXNzaW5lc3MzMB0GA1UdDgQWBBSgmIWD6bPfbbKkmTwOJRXvIbH9HjAfBgNVHSMEGDAWgBR2YIz7BqCsZ1c1nc+arKcrmTW1LzBOBgNVHR8ERzBFMEOgQaA/hj1odHRwOi8vdHN0Y3JsLnphdGNhLmdvdi5zYS9DZXJ0RW5yb2xsL1RTWkVJTlZPSUNFLVN1YkNBLTEuY3JsMIGtBggrBgEFBQcBAQSBoDCBnTBuBggrBgEFBQcwAYZiaHR0cDovL3RzdGNybC56YXRjYS5nb3Yuc2EvQ2VydEVucm9sbC9UU1pFaW52b2ljZVNDQTEuZXh0Z2F6dC5nb3YubG9jYWxfVFNaRUlOVk9JQ0UtU3ViQ0EtMSgxKS5jcnQwKwYIKwYBBQUHMAGGH2h0dHA6Ly90c3RjcmwuemF0Y2EuZ292LnNhL29jc3AwDgYDVR0PAQH/BAQDAgeAMB0GA1UdJQQWMBQGCCsGAQUFBwMCBggrBgEFBQcDAzAnBgkrBgEEAYI3FQoEGjAYMAoGCCsGAQUFBwMCMAoGCCsGAQUFBwMDMAoGCCqGSM49BAMCA0gAMEUCIARehvaSyyUUyKpOrF/loNYHOWypNKttFPpUIm4dLwyaAiEAiNlHW6XNGo3sETvQxqVF4bx5AAw14BmXiYic1ZrtAOM=")); //TODO get certificate from configuration and place here

            x509Data.appendChild(x509Certificate);
            keyInfo.appendChild(x509Data);

            signature.appendChild(keyInfo);
            //key info end
            //object start
            Element object = doc.createElement("ds:Object");

            Element qualifyingProperties = doc.createElement("xades:QualifyingProperties");
            qualifyingProperties.setAttribute("xmlns:xades","http://uri.etsi.org/01903/v1.3.2#");
            qualifyingProperties.setAttribute("Target","signature");

            Element signedProperties = doc.createElement("xades:SignedProperties");
            signedProperties.setAttribute("Id","xadesSignedProperties");

            Element signedSignatureProperties = doc.createElement("xades:SignedSignatureProperties");

            Element signingTime = doc.createElement("xades:SigningTime");
            signingTime.appendChild(doc.createTextNode("2022-03-18T14:13:54Z"));
            signedSignatureProperties.appendChild(signingTime);

            Element signingCertificate = doc.createElement("xades:SigningCertificate");
            Element cert = doc.createElement("xades:Cert");

            Element certDigest = doc.createElement("xades:CertDigest");

            digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");
            certDigest.appendChild(digestMethod);

            digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode("ZjFmMmY0NWM0M2NjMmY0MGM0ODkzNGI5NDg0Mjg0ODhkYzMwZDFkOThlYTI5YjNlNmU1ODk3MDQ3ZGE4MzdlZg=="));
            certDigest.appendChild(digestValue);

            cert.appendChild(certDigest);

            Element issueSerial = doc.createElement("xades:IssuerSerial");
            Element issuerName = doc.createElement("ds:X509IssuerName");
            issuerName.appendChild(doc.createTextNode("CN=TSZEINVOICE-SubCA-1, DC=extgazt, DC=gov, DC=local"));//TODO
            issueSerial.appendChild(issuerName);

            Element serialNo = doc.createElement("ds:X509SerialNumber");
            serialNo.appendChild(doc.createTextNode("2475382850646064994238214165482959904908010628"));//TODO fill certificate serial no here
            issueSerial.appendChild(serialNo);

            cert.appendChild(issueSerial);

            signingCertificate.appendChild(cert);
            signedSignatureProperties.appendChild(signingCertificate);
            signedProperties.appendChild(signedSignatureProperties);
            qualifyingProperties.appendChild(signedProperties);
            object.appendChild(qualifyingProperties);
            signature.appendChild(object);
            //object end

            subChildElement2.appendChild(signature);
            //signature end

            subChildElement1.appendChild(subChildElement2);
            subChildElement.appendChild(subChildElement1);
            childElement.appendChild(subChildElement);
            //extension content end

            xmlElement.appendChild(childElement);

            //UBLExtenstions end
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return xmlElement;
    }
}