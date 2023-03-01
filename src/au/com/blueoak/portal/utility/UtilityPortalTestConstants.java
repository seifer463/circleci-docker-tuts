package au.com.blueoak.portal.utility;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public interface UtilityPortalTestConstants {
	
	/** Test suite properties file name */
	public static final String PORTAL_TEST_SUITE_PROP_FILE = "TestSuite.properties";
	
	/** This is the root directory location */
	public static final String ROOT_DIR = System.getProperty("user.dir");
	
	/** Folder where all the test data are located */
	public static final String TEST_DATA_LOCATION = "file:/" + ROOT_DIR + "/test_data/";
	
	/** Directory where the required browser drivers are stored */
	public static final String DRIVERS_DIR = ROOT_DIR + "\\drivers\\";
	
    /** Directory where the required libraries are stored */
    public static final String LIBS_DIR = ROOT_DIR + "\\libs\\";
    
    /** Directory where the artifacts for uploading files are stored */
    public static final String ARTIFACTS_DIR = ROOT_DIR + "\\test_data\\artifacts\\";
    
    /** Directory where the artifacts for uploading files are stored */
    public static final String TEST_VIRUS_FILES_DIR = ROOT_DIR + "\\test_data\\test_virus_file\\";
    
    /** directory into which files will be downloaded */
    public static final String DOWNLOADS_DIR = ROOT_DIR + "\\bin\\downloads";
    
    /** Location where the log screenshots will be saved */
    public static final String TEST_LOG_SCREENSHOTS = ROOT_DIR + "\\test-output\\logScreenshots\\";
    
    /** Location where the failed test cases screenshots will be saved */
    public static final String TEST_FAILED_SCREENSHOTS =  ROOT_DIR + "\\test-output\\failedTestCases\\";
    
    /* Location where the engine artifacts attachments are stored FOR online request */
    public static final String ONLINE_REQUESTS_ENGINE_ARTIFACTS_DIR = ROOT_DIR + "\\test_data\\engine_artifacts\\online_request\\attachments\\";
    
    /** define the Database date format */
    public static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /** define the MM/dd/yyyy format */
    public static final SimpleDateFormat MONTH_DATE_YEAR_FORMAT_SLASH = new SimpleDateFormat("MM/dd/yyyy");
    
    /** define the MM-dd-yyyy format */
    public static final SimpleDateFormat MONTH_DATE_YEAR_FORMAT_DASH = new SimpleDateFormat("MM-dd-yyyy");
    
    /** define the dd/MM/yyyy format */
    public static final SimpleDateFormat DATE_MONTH_YEAR_FORMAT_SLASH = new SimpleDateFormat("dd/MM/yyyy");
    
    /** define the dd-MM-yyyy format */
    public static final SimpleDateFormat DATE_MONTH_YEAR_FORMAT_DASH = new SimpleDateFormat("dd-MM-yyyy");
    
    /** output would be 2021.03.24.16.34.26 */
    public static final SimpleDateFormat YEAR_MONTH_DATE_DOTS = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    
    /** output would be 2021-03-24 16:48:05 */
    public static final SimpleDateFormat YEAR_MONTH_DATE_DASH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** output would be 2021-03-24T16:44:39.083+08:00 */
    public static final SimpleDateFormat YEAR_MONTH_DATE_EXACT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    
    /** output would be 02 January 2018 India Standard Time */
    public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("dd MMMM yyyy zzzz");
    
    /** output would be Tue, 02 Jan 2018 18:07:59 IST */
	public static final SimpleDateFormat COMPLETE_DATE_FORMAT_WITH_DAY_AND_TIMEZONE = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");

    /** 11-Sep-1989 12:25:15 PM Coordinated Universal Time */
	public static final SimpleDateFormat COMPLETE_DATE_FORMAT_WITH_DAY_TIME = new SimpleDateFormat(
			"dd-MMM-yyyy hh:mm:ss aa z");

    /** This is the timeout for waiting on the browser page title */
    public static final int PAGE_TITLE_WAIT_TIMEOUT = 30;
    
    /** A time zone for Australia/Melbourne */
    public static final TimeZone MELBOURNE_TIME_ZONE = TimeZone.getTimeZone("Australia/Melbourne");
    
	/**
	 * This is the timeout for checking if the files were uploaded in the S3 and if
	 * the uploaded files are already displaying in the upload section in milli
	 * seconds
	 */
	public static final int PORTAL_FILE_UPLOAD_WAIT_TIMEOUT = 35000;
    
    /** This is the driver implicit wait timeout for the portal in seconds*/
    public static final int PORTAL_IMPLICIT_WAIT_TIMEOUT = 5;
    
    /** This is the default maximum wait for elements to display in the portal in seconds */
    public static final int PORTAL_ELEMENT_WAIT_TIMEOUT = 5;
    
    /** This is the default maximum wait for the iframe to display in the portal in seconds */
    public static final int PORTAL_IFRAME_WAIT_TIMEOUT = 10;
    
    /** timeout for waiting the loading screen to appear */
    public static final int PORTAL_LOAD_TIMEOUT_START = 4;
    
    /** timeout for waiting the loading screen to disappear */
    public static final int PORTAL_LOAD_TIMEOUT_END = 8;
    
    /** timeout for waiting for the response after submitting online request in seconds */
    public static final int PORTAL_SUBMIT_REQUEST_TIMEOUT = 60;
    
    /** portal wait time after accessing the portal in milli seconds */
    public static final int PORTAL_WAIT_TIME_AFTER_ACCESSING = 3000;
    
    /** Location where the concession card definitions are stored */
    public static final String PORTAL_CONCESSION_DEF_DIR = ROOT_DIR + "\\test_data\\concession_card_def\\";
    
    /** Location where the global custom CSS is stored */
    public static final String PORTAL_CUSTOM_CSS_DIR = ROOT_DIR + "\\test_data\\styles\\custom\\";
    
    /** Timeout for the ABN/ACN searching */
    public static final int PORTAL_ABN_ACN_SEARCH_TIMEOUT = 4;
    
    /** The region for the AWS */
    public static final String AWS_REGION_S3_PATH = ".s3-ap-southeast-2.amazonaws.com";
    
    /** The region for the AWS */
    public static final String AWS_REGION_CLIENT_BUILDER = "ap-southeast-2";
    
    /** This is the bucket where the portal codes are deployed for development URL */
    public static final String S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_DEV = "blueacorns-portal-cloud-front";
    
    /** This is the distribution ID for S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_DEV */
    public static final String S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_DEV = "E2KYGIYCW92CRU";
    
    /** This is the bucket where the portal codes are deployed for staging URL */
    public static final String S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_STAGING = "au-com-blueacorns-app-binary";
    
    /** This is the distribution ID for S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_STAGING */
    public static final String S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_STAGING = "E3SSMLQSHHH3NX";
    
    /** this is the bucket for the portal configurations */
    public static final String S3_PORTAL_CONFIG_BUCKET_NAME = "au-com-blueacorns-app-config";
    
    /** this is the bucket name for the portal presign upload */
    public static final String S3_PORTAL_PRESIGN_BUCKET_NAME = "development-presign-upload";
    
    /** this is the directory for the custom language files in any portal */
    public static final String S3_PORTAL_LANG_FILES_DIR = "/i18n/";
    
    /** this is the bucket for the crm and engine artifacts */
    public static final String S3_CRM_ENGINE_ARTIFACTS_BUCKET_NAME = "bluebilling-artifacts";
    
	/** Location where the Make Payment language files are stored */
	public static final String MAKE_PAYMENT_PORTAL_GLOBAL_LANG_FILES_DIR = ROOT_DIR
			+ "\\test_data\\make_payment\\language_files\\global\\";

    /** Location where the Make Payment portal configs are stored */
    public static final String MAKE_PAYMENT_PORTAL_CONFIGS_DIR = ROOT_DIR + "\\test_data\\make_payment\\portal_configs\\";
    
    /** This is the timeout for the Next progress bar to appear*/
    public static final int MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_APPEAR_TIMEOUT = 2;
    
    /** This is the timeout for the Next progress bar to disappear*/
    public static final int MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_DISAPPEAR_TIMEOUT = 10;
    
    /** This is the timeout for the Make Payment progress bar to appear*/
    public static final int MAKE_PAYMENT_PORTAL_PROGRESS_BAR_APPEAR_TIMEOUT = 2;
    
    /** This is the timeout for the Make Payment progress bar to disappear*/
    public static final int MAKE_PAYMENT_PORTAL_PROGRESS_BAR_DISAPPEAR_TIMEOUT = 12;
    
	/** Location where the Move In Global language files are stored */
	public static final String MOVE_IN_PORTAL_GLOBAL_LANG_FILES_DIR = ROOT_DIR
			+ "\\test_data\\move_in\\language_files\\global\\";
	
	/** Location where the Move In Custom language files are stored */
	public static final String MOVE_IN_PORTAL_CUSTOM_LANG_FILES_DIR = ROOT_DIR
			+ "\\test_data\\move_in\\language_files\\custom\\";

    /** Location where the Move In portal configs are stored */
    public static final String MOVE_IN_PORTAL_CONFIGS_DIR = ROOT_DIR + "\\test_data\\move_in\\portal_configs\\";
    
    /** This is the timeout for the Direct Debit progress bar to appear*/
    public static final int MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_APPEAR_TIMEOUT = 2;
    
    /** This is the timeout for the Direct Debit progress bar to disappear*/
    public static final int MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_DISAPPEAR_TIMEOUT = 16;
    
    /** Location where the Move Out language files are stored */
    public static final String MOVE_OUT_PORTAL_LANG_FILES_DIR = ROOT_DIR + "\\test_data\\move_out\\language_files\\";
        
    /** Location where the Move Out portal configs are stored */
    public static final String MOVE_OUT_PORTAL_CONFIGS_DIR = ROOT_DIR + "\\test_data\\move_out\\portal_configs\\";
    
    /** Location where the Customer portal configs are stored */
    public static final String CUSTOMER_PORTAL_CONFIGS_DIR = ROOT_DIR + "\\test_data\\customer_portal\\portal_configs\\";
    
    /** Location where the Customer portal css are stored */
    public static final String CUSTOMER_PORTAL_CSS_DIR = ROOT_DIR + "\\test_data\\customer_portal\\style\\";
    
	/** Location where the Customer Custom language files are stored */
	public static final String CUSTOMER_PORTAL_CUSTOM_LANG_FILES_DIR = ROOT_DIR
			+ "\\test_data\\customer_portal\\language_files\\custom\\";
  
    /** This is the driver implicit wait timeout for the CRM */
    public static final int CRM_IMPLICIT_WAIT_TIMEOUT = 15;
    
    /** timeout for waiting the table load to appear */
    public static final int CRM_TABLE_LOAD_TIMEOUT_START = 5;
    
    /** timeout for waiting the table load to disappear but only once it has appeared */
    public static final int CRM_TABLE_LOAD_TIMEOUT_END = 120;
    
    /** wait time before accessing the downloaded file from the CRM */
    public static final int CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES = 10000;
    
    /** login timeout period */
    public static final int CRM_LOGIN_TIMEOUT = 180;
    
    /** timeout for waiting the page load to appear */
    public static final int CRM_PAGE_LOAD_TIMEOUT_START = 5;
    
    /** timeout for waiting the page load to disappear */
    public static final int CRM_PAGE_LOAD_TIMEOUT_END = 60;
    
    /** timeout for waiting any element in the CRM */
    public static final int CRM_WAIT_ELEMENT_TIMEOUT = 30;
    
}
