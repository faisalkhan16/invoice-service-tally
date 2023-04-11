package com.invoice.util;

import java.time.LocalDate;
import java.util.HashMap;

public class Constants
{

    public static String CREATED_STATUS = "C";
    public static String INPROCESS_STATUS = "I";
    public static String PROCESSED_STATUS = "V";
    public static String FAILED_STATUS = "F";
    public static String ERROR_STATUS = "E";
    public static String PENDING_STATUS = "P";

    public final static String TRANSPOSE_STATUS = "T";
    public final static String FAILURE_STATUS = "F";
    public static String VLD_STS_PASS = "PASS";
    public static String CLRNC_STS = "CLEARED";
    public static String RPRT_STS = "REPORTED";

    public static String COMPLIANCE_CERTIFICATE_STATUS = "C";

    public static HashMap<String,String> COUNTRIES_MAP = null;

    public static LocalDate SELLER_EXPIRE_DATE;

    public static int STANDARD_TAX_RATE = 15;


}
