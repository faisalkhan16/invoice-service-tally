package com.invoice.util;

import com.github.f4b6a3.ulid.UlidCreator;
import com.invoice.model.InvoiceLine;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.*;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Getter
@Slf4j
public class CommonUtils {

    public static Process p;

    public static String getUlid() {
        return UlidCreator.getMonotonicUlid(Instant.now().toEpochMilli()).toRfc4122().toString();
    }


    public static Map<String, String> getNameSpacesMap()
    {
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

    public static boolean isNullOrEmptyString(String string)
    {
        try
        {
            if (null == string || "".equals(string.trim()) || "null".equals(string))
                return true;
            else
                return false;
        }
        catch(Exception ex)
        {
            log.error("Exception in CommonUtils isNullOrEmptyString {}",ex.getMessage());
            return true;
        }
    }

    public static Result validateInvoice(File invoice) throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidCanonicalizerException, CanonicalizationException
    {
        Result result = new Result();
        Result xsdResult = validateXMLSchema(invoice, "zatcaValidation/Schemas/xsds/UBL2.1/xsd/maindoc/UBL-Invoice-2.1.xsd");

        if (!mergeResults(result, xsdResult).isValid())
        {
            result.setStage(StageEnum.XSD);
            return result;
        }
        else
        {
            Result enResult = validateEnSchema(invoice, "zatcaValidation/Rules/schematrons/CEN-EN16931-UBL.xsl", "EN");
            mergeResults(result, enResult);
            if (!enResult.isValid())
            {
                result.setStage(StageEnum.EN);
                return enResult;
            }
            else
            {
                Result ksaResult = validateEnSchema(invoice, "zatcaValidation/Rules/schematrons/20210819_ZATCA_E-invoice_Validation_Rules.xsl", "KSA");

                if (!mergeResults(result, ksaResult).isValid())
                {
                    result.setStage(StageEnum.KSA);
                    return result;
                }
                /*else
                {
                    Result qrCodeResult;
                    if (Utils.isInvoiceSimplified(invoice))
                    {
                        Validator qrCodeValidator = new QrCodeValidator();
                        qrCodeResult = qrCodeValidator.validate(invoice);
                        if (!mergeResults(result, qrCodeResult).isValid())
                        {
                            result.setStage(StageEnum.QR);
                            return result;
                        }
                    }
                }*/
            }
        }

        return result;
    }

    private static Result mergeResults(Result initial, Result toBeMerged)
    {
        Map<String, String> errors = new HashMap();
        if (toBeMerged.getError() != null) {
            errors.putAll(toBeMerged.getError());
            initial.setError(errors);
        }

        initial.setValid(initial.isValid() && toBeMerged.isValid());
        switch(toBeMerged.getStage()) {
            case QR:
                initial.setValidQrCode(initial.isValidQrCode() || toBeMerged.isValidQrCode());
                break;
            case SIGNATURE:
                initial.setValidSignature(initial.isValidSignature() || toBeMerged.isValidSignature());
        }

        return initial;
    }

    private static Result validateXMLSchema(File invoice, String schemaPath)
    {
        Result result = new Result();
        result.setStage(StageEnum.XSD);

        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            File file = new File(schemaPath);

            Schema schema = factory.newSchema(file);
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(invoice));
        } catch (SAXException | IOException var6) {
            Map<String, String> errors = new HashMap();
            if (var6 instanceof SAXParseException) {
                errors.put(var6.getClass().getSimpleName(), "Schema validation failed; XML does not comply with UBL 2.1 standards in line with ZATCA specifications");
            } else {
                errors.put(var6.getClass().getSimpleName(), var6.getMessage());
            }

            result.setError(errors);
            result.setValid(false);
        }

        return result;
    }

    private static Result validateEnSchema(File invoice, String schemaPath, String schemaType)
    {
        Result result = new Result();
        if (schemaType.equalsIgnoreCase("EN"))
        {
            result.setStage(StageEnum.EN);
        }
        else
        {
            result.setStage(StageEnum.KSA);
        }

        HashMap errors = new HashMap();

        try
        {
            Processor processor = new Processor(false);
            XsltCompiler compiler = processor.newXsltCompiler();
            XsltExecutable xslt = compiler.compile(new StreamSource(new File(schemaPath)));
            XsltTransformer transformer = xslt.load();
            transformer.setSource(new StreamSource(invoice));
            XdmDestination chainResult = new XdmDestination();
            transformer.setDestination(chainResult);
            transformer.transform();
            XdmNode rootnode = chainResult.getXdmNode();
            Iterator var10 = ((XdmNode)rootnode.children().iterator().next()).children().iterator();

            while(var10.hasNext()) {
                XdmNode node = (XdmNode)var10.next();
                if (node.getNodeName() != null && "failed-assert".equals(node.getNodeName().getLocalName())) {
                    String res = ((XdmNode)node.children().iterator().next()).getStringValue();
                    errors.put(node.attribute("id"), trim(res));
                }
            }

            if (!errors.isEmpty())
            {
                result.setError(errors);
                result.setValid(false);
            }
        }
        catch (Exception var13)
        {
            errors.put(var13.getClass().getSimpleName(), var13.getMessage());
            result.setError(errors);
            result.setValid(false);
        }
        return result;
    }

    private static String trim(String s) {
        for(s = s.replace("\n", "").replace("\t", " "); s.indexOf("  ") != -1; s = s.replace("  ", " ")) {
        }

        return s.trim();
    }

    public static String transform(String type, byte[] certificateRequest)
    {
        try
        {
            PemObject pemObject = new PemObject(type, certificateRequest);
            StringWriter stringWriter = new StringWriter();
            PEMWriter pemWriter = new PEMWriter(stringWriter);
            pemWriter.writeObject(pemObject);
            pemWriter.close();
            stringWriter.close();
            return stringWriter.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static List<InvoiceLine> getInvoiceBreakDown(List<InvoiceLine> invoiceLines, double documentLevelDiscount)
    {
        List<InvoiceLine> invoiceBreakDowns = new ArrayList<>();
        try
        {
            LinkedHashMap<String, InvoiceLine> breakDownMap = new LinkedHashMap<>();

            //handling S,Z,E,O (check if standard rate other than 15 exists, if yes then implement segregate rates for standard category)
            for (InvoiceLine invoiceLine: invoiceLines)
            {
                if(breakDownMap.containsKey(invoiceLine.getItemTaxCategoryCode()))
                {
                    breakDownMap.get(invoiceLine.getItemTaxCategoryCode()).setTotalTaxableAmount(invoiceLine.getTotalTaxableAmount() + breakDownMap.get(invoiceLine.getItemTaxCategoryCode()).getTotalTaxableAmount());
                    breakDownMap.get(invoiceLine.getItemTaxCategoryCode()).setTaxAmount(invoiceLine.getTaxAmount() + breakDownMap.get(invoiceLine.getItemTaxCategoryCode()).getTaxAmount());
                }
                else
                {
                    InvoiceLine breakdownModel = new InvoiceLine();
                    breakdownModel.setItemTaxCategoryCode(invoiceLine.getItemTaxCategoryCode());
                    breakdownModel.setTotalTaxableAmount(invoiceLine.getTotalTaxableAmount());
                    breakdownModel.setTaxAmount(invoiceLine.getTaxAmount());
                    breakdownModel.setTaxRate(invoiceLine.getTaxRate());
                    breakdownModel.setExemptionReasonCode(invoiceLine.getExemptionReasonCode());
                    breakdownModel.setExemptionReasonText(invoiceLine.getExemptionReasonText());

                    breakDownMap.put(breakdownModel.getItemTaxCategoryCode(), breakdownModel);
                }
            }

            //for S category, subtract document level allowance from taxable amount and recalculate VAT rate for the category
            if(documentLevelDiscount > 0 && breakDownMap.containsKey("S"))
            {
                breakDownMap.get("S").setTotalTaxableAmount(breakDownMap.get("S").getTotalTaxableAmount() - documentLevelDiscount);
                breakDownMap.get("S").setTaxAmount(breakDownMap.get("S").getTotalTaxableAmount() * Constants.STANDARD_TAX_RATE / 100);
            }

            invoiceBreakDowns.addAll(breakDownMap.values());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return invoiceBreakDowns;
    }

    public static boolean isUpToTwoDecimal(double value){
        if(BigDecimal.valueOf(value).scale() <= 2){
            return  true;
        }else{
            return false;
        }
    }

    public static boolean isValidEmail(String email){
        return true;
    }

    public static BigDecimal doubleToBigDecimal(double value){
        BigDecimal bigDecimal = new BigDecimal(value).setScale( 2 , RoundingMode.HALF_UP);
        return bigDecimal;
    }
}