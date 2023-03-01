package au.com.blueoak.portal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.apache.commons.vfs2.VFS;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import com.google.common.base.Function;

import au.com.blueoak.portal.pageObjects.customer_portal.LoginCustomer;
import au.com.blueoak.portal.pageObjects.make_payment.InputFields;
import au.com.blueoak.portal.pageObjects.make_payment.ProgressBar;
import au.com.blueoak.portal.pageObjects.move_in.AccountDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;
import au.com.blueoak.portal.utility.CssTestConstantsCustom;
import au.com.blueoak.portal.utility.CssTestConstantsDefault;
import au.com.blueoak.portal.utility.ExcelToCSVConvertor;
import au.com.blueoak.portal.utility.GifDecoder;
import au.com.blueoak.portal.utility.UtilityPortalTestConstants;

public abstract class BaseTesting
		implements UtilityPortalTestConstants, CssTestConstantsCustom, CssTestConstantsDefault {

	/** Logger for the AbstractTest class */
	protected static final Log LOG = LogFactory.getLog(BaseTesting.class);

	/** JDBC template to load the database */
	private static JdbcTemplate jdbcTemplate;

	/** Data source to execute scripts on database */
	private static DriverManagerDataSource dataSource;

	/** Properties to be loaded to run the tests */
	private static Properties prop;

	private ApplicationContext appContext;

	/** This is the type of the portal */
	private String portalType;

	/** This is how data is populated in the portal */
	private String populateDataMethod;

	/** This is for the Stand Alone Portal url we are testing for Make Payment */
	private String standaloneUrlMakePayment;

	/**
	 * This is for the web site that has the embedded portal url we are testing for
	 * Make Payment
	 */
	private String embeddedUrlMakePayment;

	/** This is for the instance id of the Make Payment */
	private String instanceIdMakePayment;

	/** This is for the Stand Alone Portal url we are testing for Move In */
	private String standaloneUrlMoveIn;

	/**
	 * This is for the web site that has the embedded portal url we are testing for
	 * Move In
	 */
	private String embeddedUrlMoveIn;

	/** This is for the dev third party prefill url for Move In */
	private String thirdPartyPrefillUrlMoveIn;

	/** This is for the instance id of the Stand Alone Portal for Move In */
	private String instanceIdMoveIn;

	/** This is for the Stand Alone Portal url we are testing for Move Out */
	private String standaloneUrlMoveOut;

	/**
	 * This is for the web site that has the embedded portal url we are testing for
	 * Move Out
	 */
	private String embeddedUrlMoveOut;

	/** This is for the dev third party prefill url for Move Out */
	private String thirdPartyPrefillUrlMoveOut;

	/** This is for the instance id of the Stand Alone Portal for Move Out */
	private String instanceIdMoveOut;
	
	/** This is for the Stand Alone Portal url we are testing for Connection */
	private String standaloneUrlConnection;

	/**
	 * This is for the web site that has the embedded portal url we are testing for
	 * Connection
	 */
	private String embeddedUrlConnection;

	/** This is for the dev third party prefill url for Connection */
	private String thirdPartyPrefillUrlConnection;

	/** This is for the instance id of the Stand Alone Portal for Connection */
	private String instanceIdConnection;

	/** This is for the Stand Alone Portal url we are testing for Customer Portal */
	private String standaloneUrlCustomerPortal;

	/**
	 * This is for the web site that has the embedded portal url we are testing for
	 * Customer Portal
	 */
	private String embeddedUrlCustomerPortal;

	/** This is for the instance id of the Stand Alone Portal for Customer Portal */
	private String instanceIdCustomerPortal;

	/** This is for the CRM login url */
	private String crmUrl;

	/** This is for the CRM login username */
	private String crmUsername;

	/** This is for the CRM login password */
	private String crmPassword;

	/** This is for DB url */
	private String dbUrl;

	/** This is for the DB username */
	private String dbUsername;

	/** This is for the DB password */
	private String dbPassword;

	/** This is for the identifier to identify who runs the automation test suite */
	private String automationSrc;

	/** This is for the S3 Access Key ID */
	private String awsAccessKeyId;

	/** This is for the S3 Secret Access Key */
	private String awsSecretAccessKey;

	/** This is the folder of the portal we are testing with */
	private String awsPortalConfigFolderName;

	/** This is the folder for the crm artifacts we are testing with */
	private String awsCrmArtifactsFolderName;

	/** This is the folder for the engine artifacts we are testing with */
	private String awsEngineArtifactsFolderName;
	
	/** This is the cloudfront bucket where the portal codes are deployed */
	private String awsCloudFrontOriginBucketName;
	
	/** This is the Distribution ID for cloudfront bucket where the portal codes are deployed */
	private String awsDistributionId;
	
	/** This if we need to run the invalidation for cloudfront */
	private boolean runInvalidation;

	static {
		try {
			// let's initialize the property file before running any tests
			InputStream input = new FileInputStream(PORTAL_TEST_SUITE_PROP_FILE);
			prop = new Properties();
			prop.load(input);
			if (LOG.isInfoEnabled()) {
				LOG.info("The '" + PORTAL_TEST_SUITE_PROP_FILE
						+ "' file was successfully initialized from AbstractTesting class static method");
			}

			dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName("com.mysql.jdbc.Driver");
			if (prop.getProperty("test_suite_aut_src").equals("jenkins")) {
				dataSource
						.setUrl(prop.getProperty("test_suite_db_login_url") + System.getenv("RUN_ON_INSTANCE"));
			} else {
				dataSource.setUrl(prop.getProperty("test_suite_db_login_url"));
			}
			dataSource.setUsername(prop.getProperty("test_suite_db_login_username"));
			dataSource.setPassword(prop.getProperty("test_suite_db_login_password"));
			// initializing the JDBC template
			jdbcTemplate = new JdbcTemplate(dataSource);
			if (LOG.isInfoEnabled()) {
				LOG.info("JDBC template successfully initialized.");
			}
		} catch (FileNotFoundException fnfe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("FileNotFoundException is encountered. See message for more details -> " + fnfe.getMessage());
			}
			try {
				throw (new FileNotFoundException(
						"FileNotFoundException is encountered. See log file for more details."));
			} catch (FileNotFoundException e) {
			}
		} catch (IOException ioe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("IOException is encountered. See message for more details -> " + ioe.getMessage());
			}
			try {
				throw (new IOException("IOException is encountered. See log file for more details."));
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Check if the file is an excel file based on the file extension.
	 *
	 * @param fileObject
	 * @return
	 */
	private boolean isFileExcel(FileObject fileObject) {

		return fileObject.getName().getBaseName().toUpperCase().endsWith(ExcelToCSVConvertor.XLS)
				|| fileObject.getName().getBaseName().toUpperCase().endsWith(ExcelToCSVConvertor.XLSX);
	}

	/**
	 * Get the content of the file as one long string. If the file is a PDF, the
	 * contact is extracted as text.
	 *
	 * @param fileObj
	 * @return
	 * @throws FileSystemException
	 */
	private String getFileContent(FileObject fileObj, int lineNumber) throws FileSystemException {

		String content = null;
		switch (fileObj.getName().getExtension().toLowerCase()) {
		case "pdf":
			content = parsePdfFile(fileObj);
			break;

		case "csv":
			content = parseCsvFile(fileObj, lineNumber);
			break;
		}

		// return the content of the file
		return (content);
	}

	/**
	 * Parse the PDF file and return its content as a String.
	 *
	 * @param fileObj
	 * @return
	 */
	private String parsePdfFile(FileObject fileObj) throws FileSystemException {

		String content = null;
		try (InputStream stream = fileObj.getContent().getInputStream();
				PDDocument pdfDocument = PDDocument.load(stream);) {
			if (pdfDocument.isEncrypted()) {
				pdfDocument.decrypt("");
				pdfDocument.setAllSecurityToBeRemoved(true);
			}
			content = new PDFTextStripper().getText(pdfDocument);
		} catch (Exception exception) {
			try {
				throw (new FileSystemException(
						"Problem getting content of PDF file located at -> " + fileObj.getName().getURI(), exception));
			} catch (FileSystemException fse) {
				if (LOG.isErrorEnabled())
					LOG.error(fse);
				throw (new FileSystemException(
						"The PDF file located at -> " + fileObj.getName().getURI() + " may be corrupted", fse));
			}
		}
		// return the content of the file
		return (content);
	}

	/**
	 * Parse the CSV file and return its contents as a String
	 *
	 *
	 * @param fileObj
	 * @return
	 */
	private String parseCsvFile(FileObject fileObj, int lineNumber) throws FileSystemException {

		StringBuilder content = new StringBuilder();
		String line = "";
		String singleLineContent = StringUtils.EMPTY;
		try (BufferedReader br = new BufferedReader(new FileReader(new File(fileObj.getName().getPath())))) {
			int lineCounter = 0;
			while ((line = br.readLine()) != null) {
				lineCounter++;
				if (lineCounter == lineNumber) {
					// populate single line output
					singleLineContent = line;
					break;
				} else {
					content.append(line);
				}
			}
		} catch (Exception exception) {
			try {
				throw (new FileSystemException(
						"Problem getting content of CSV file located at -> " + fileObj.getName().getURI(), exception));
			} catch (FileSystemException fse) {
				if (LOG.isErrorEnabled())
					LOG.info(fse);
				throw (new FileSystemException(
						"The CSV file located at -> " + fileObj.getName().getURI() + " may be corrupted", fse));
			}
		}

		return (singleLineContent.equals(StringUtils.EMPTY)) ? content.toString() : singleLineContent;
	}

	/**
	 * Use this to verify if loading status has appeared for: - Move In - Move Out -
	 * Customer Portal
	 */
	private boolean isPortalLoadDisplayed(long timeout, boolean waitToAppear) {

		FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class);

		// check if the "Loading" status to appear
		boolean loadingPresent = false;
		try {
			setImplicitWait(1);
			// check if the "loading" element is present in the table
			loadingPresent = fluentWait.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Checking the Loading screen");

					boolean appeared;
					try {
						appeared = driver
								.findElement(
										By.xpath("//div[@class='container']/mat-card/mat-card-content/mat-spinner"))
								.isDisplayed();
						appeared = true;
					} catch (NoSuchElementException nse) {
						appeared = false;
					}

					if (appeared) {
						logDebugMessage("The spinner element WAS found");
					} else {
						logDebugMessage("The spinner element WAS NOT found");
					}

					logDebugMessage("Loading screen " + (appeared ? "appeared" : "not displayed")
							+ ", we are waiting a maximum of " + timeout + " seconds for it to "
							+ (waitToAppear ? "appear" : "disappear"));

					// see if we are waiting for it to appear or
					// disappear
					if (waitToAppear && appeared)
						return (true);
					else if (waitToAppear && !appeared)
						return (false);
					else if (!waitToAppear && appeared)
						return (false);
					else if (!waitToAppear && !appeared)
						return (true);
					else
						return (true);

				}
			}).booleanValue();
		} catch (Exception exception) {
			logDebugMessage("Done waiting a maximum of " + timeout + " seconds for the Loading screen to "
					+ (waitToAppear ? "appear" : "disappear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		return (loadingPresent);
	}
	
	/** 
	 * Use this to wait until the CSS renders
	 * */
	private void waitForCssToRender(int waitTimeInMilliSec) {
		
		pauseSeleniumExecution(waitTimeInMilliSec);
	}
	
	/** 
	 * Use this to wait when clicking the Next button
	 * or header of the next section because the sections
	 * are still adjusting.
	 * 
	 * Or if you are opening or closing a modal dialog
	 * */
	private void waitForScreenToRender(int waitTimeInMilliSec) {
		
		pauseSeleniumExecution(waitTimeInMilliSec);
	}

	/** Driver we are using */
	protected WebDriver driver;

	/** VFS manager */
	protected FileSystemManager fsManager;

	/**
	 * Initialize the chrome web driver
	 * 
	 * @param language possible values: (en-US, en, fil)
	 * 
	 */
	protected void initChromeDriver(String language) {

		System.setProperty("webdriver.chrome.driver", concatStrings(DRIVERS_DIR, "chromedriver.exe"));
		logDebugMessage(
				concatStrings("Successfully set the chrome driver located in '", DRIVERS_DIR, "chromedriver.exe'"));
		DesiredCapabilities jsCapabilities = DesiredCapabilities.chrome();
		ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<>();
		// specify the language to use
		prefs.put("intl.accept_languages", language);
		// specify the downloads directory
		prefs.put("download.default_directory", DOWNLOADS_DIR);
		options.setExperimentalOption("prefs", prefs);
		jsCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		
		// this is when we are running jenkins and with local machine
		// let's make the fonts smaller so the fields would fit
		// in the screen in jenkins
		options.addArguments("force-device-scale-factor=0.50");
		options.addArguments("high-dpi-support=0.50");
		options.addArguments("--disable-extensions");
		// This option was deprecated, see
		// https://sqa.stackexchange.com/questions/32444/how-to-disable-infobar-from-chrome
		options.addArguments("--disable-infobars");
		// added the following options below to fix
		// an intermittent issue
		// org.openqa.selenium.SessionNotCreatedException: session not created
		// from timeout: Timed out receiving message from renderer: 600.000
		// https://stackoverflow.com/questions/48450594/selenium-timed-out-receiving-message-from-renderer
		options.addArguments("enable-automation");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-browser-side-navigation");
		options.addArguments("--disable-gpu");
		driver = new ChromeDriver(options);
		logDebugMessage(
				concatStrings("Successfully initialized the Chrome WebDriver using the language '", language, "'"));
		
//		// this is when running using Docker
//		// this is the URL where the WebDriver communicates 'http://localhost:4444/wd/hub'
//		// this is the grid console URL when using selenium hub 'http://localhost:4444/grid/console'
//		// and to verify if it's configured correctly
//		URL url = null;
//		try {
//			// this is where the WebDriver communicates
//			url = new URL("http://localhost:4444/wd/hub");
//		} catch (MalformedURLException murle) {
//			murle.printStackTrace();
//		}
//		driver = new RemoteWebDriver(url, jsCapabilities);
//		logDebugMessage(
//				concatStrings("Successfully initialized the Chrome WebDriver using the language '", language, "'"));
	}

	/**
	 * README
	 * 
	 * Currently we won't be able to use this on the remote windows machine
	 * because the firefox version we use there is v32.0. That will not work on the
	 * selenium v3.141.59 and gecko driver v0.25.0. Running this on mozilla v32.0
	 * would cause firefox to crash.
	 * 
	 * Initialize the firefox web drier
	 */
	protected void initFirefoxDriver(String language) {

		System.setProperty("webdriver.gecko.driver", concatStrings(DRIVERS_DIR, "geckodriver.exe"));
		logDebugMessage(concatStrings("Successfully set the gecko driver located in '", DRIVERS_DIR, "geckodriver.exe'"));
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("intl.accept_languages", language);
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		firefoxOptions.setProfile(profile);
		firefoxOptions.setCapability("marionette", true);
		driver = new FirefoxDriver(firefoxOptions);
		logDebugMessage(
				concatStrings("Successfully initialized the Firefox WebDriver using the language '", language, "'"));
	}

	/** 
	 * 
	 * */
	public enum AccountCategoryEnum {

		TENANT("Tenant"), tEnAnT("tEnAnT"), OWNER("Owner"), LETTING_POOL("Holiday Rental or Letting"),
		RUM("Property Manager or Letting Agent");

		private String labelText;

		private AccountCategoryEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * Different account types across Move In, Move Out and Connection
	 */
	public enum AccountTypesEnum {

		RESIDENTIAL("Residential"), SMALL_BUSINESS("Small Business"), sMAll_BUSiNeSS(null);

		private String labelText;

		private AccountTypesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * Enum values for Australian states
	 */
	public enum AustralianStatesEnum {

		ACT("Australian Capital Territory"), NSW("New South Wales"), NT("Northern Territory"), QLD("Queensland"),
		qLD("qUEensLanD"), SA("South Australia"), TAS("Tasmania"), VIC("Victoria"), WA("Western Australia"),
		vic("victoria"), WRONG_VALUE("Wrong State");

		private String labelText;

		private AustralianStatesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * List of countries being used when testing
	 */
	public enum CountriesEnum {

		AU("Australia"), AX("Åland Islands"), CD("Congo, Democratic Republic of"), PH("Philippines");

		private String labelText;

		private CountriesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	public enum PortalTypesEnum {

		STANDALONE("Standalone"), EMBEDDED("Embedded");

		private String labelText;

		private PortalTypesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * Different tenancy types along with the abbreviated versions. The label is the
	 * abbreviated version.
	 */
	public enum TenancyTypesEnum {

		Apr(null), Apart(null), Aprt(null), Apt(null), aPT(null), Atm(null), Apartment(null), aParTmEntS(null),
		aP(null), House("H"), Lot("L"), Office("O"), Shop("Shp"), Site(null), Suite("S"), Tenancy("T"), Unit("U"),
		uNiT(null), Villa("V"), NA("Not applicable"), NAs("Not applicables");

		private String labelText;

		private TenancyTypesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * List of Street Types being used when testing
	 */
	public enum StreetTypesEnum {

		HWY("Highway"), TCE("Terrace"), ST("Street"), st("street"), sT("sTrEEt"), STs("Streets"), RD("Road"),
		PDE("Parade"), DR("Drive"), AVE("Avenue"), CDS("Cul-de-sac"), FTRK("Fire Track"), FtRk("fiRE TRacK"),
		WRONG_VALUE("Wrong St Type");

		private String labelText;

		private StreetTypesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * List of Portal Names when uploading instance level portal configuration
	 */
	public enum PortalNamesEnum {

		Connection("connection"), CustomerPortal("customer-portal"), MakePayment("make-payment"), MoveIn("move-in"),
		MoveOut("move-out");

		private String labelText;

		private PortalNamesEnum(String labelText) {

			this.labelText = labelText;
		}

		public String getLabelText() {

			return (labelText);
		}
	}

	/**
	 * Let's just get the current test suite running then log it
	 */
	@BeforeTest
	public void testSuiteStart(ITestContext ctx) {

		String suiteName = ctx.getCurrentXmlTest().getSuite().getName();
		logDebugMessage(concatStrings("<><><><><> Start of Test Suite {", suiteName, "} <><><><><>"));
		this.awsAccessKeyId = prop.getProperty("test_suite_aws_access_key_id");
		this.awsSecretAccessKey = prop.getProperty("test_suite_aws_secret_access_key");
		if (prop.getProperty("test_suite_aut_src").equals("jenkins")) {
			this.awsPortalConfigFolderName = System.getenv("RUN_ON_INSTANCE")
					+ prop.getProperty("test_suite_aws_portal_config_folder_name");
		} else {
			this.awsPortalConfigFolderName = prop.getProperty("test_suite_aws_portal_config_folder_name");
		}
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		if (suiteName.toLowerCase().contains("move in")) {
			logDebugMessage("We are in the Move In test suite, uploading the concession_card_definition.json");
			// upload the concession card we are testing
			try {
				String s3FileNameToReplace = "concession_card_definition.json";
				String fileToUpload = concatStrings(PORTAL_CONCESSION_DEF_DIR, "01\\", s3FileNameToReplace);
				s3Access.uploadConfigFileIntoS3Bucket(fileToUpload, S3_PORTAL_CONFIG_BUCKET_NAME,
						getAwsPortalConfigFolderName(), s3FileNameToReplace);
				String logMsg = concatStrings("Uploaded the ", s3FileNameToReplace, " file located in '", fileToUpload,
						"' into the S3 Bucket '", S3_PORTAL_CONFIG_BUCKET_NAME, "', inside the directory '",
						getAwsPortalConfigFolderName(), "'");
				logDebugMessage(logMsg);
			} catch (FileSystemException fse) {
				logDebugMessage("A FileSystemException has been encountered. Please see error for more details -> "
						+ fse.getMessage());
				throw (new ErrorMessageException(
						"A FileSystemException has been encountered. Please see error for more details -> "
								+ fse.getMessage()));
			}
			if (!suiteName.toLowerCase().contains("setup")) {
				logDebugMessage(
						"Checking if we need to delete the files inside the bucket 'development-presign-upload' for Move In test suite");
				if (System.getenv("DELETE_PRESIGN_BUCKET") != null) {
					if (System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
						logDebugMessage("We need to delete files inside the bucket 'development-presign-upload'");
						// let's make sure we are working with a clean development-presign-upload bucket
						// when running the Move In test cases
						s3Access.deleteAllObjectsInDevPresignUploadBucket();
					} else if (System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("no")) {
						logDebugMessage("No need to delete files inside the bucket 'development-presign-upload'");
					} else {
						throw (new IllegalArgumentException(concatStrings("The DELETE_PRESIGN_BUCKET '",
								System.getenv("DELETE_PRESIGN_BUCKET"), "' is not an expected value")));
					}
				} else {
					logDebugMessage("No need to do anything in the Dev Presign bucket");
				}
			}
		}
	}

	/**
	 * Let's just get the current test suite running then log it
	 */
	@AfterTest
	public void testSuiteEnd(ITestContext ctx) {

		String suiteName = ctx.getCurrentXmlTest().getSuite().getName();
		logDebugMessage(concatStrings("><><><><>< End of Test Suite {", suiteName, "} ><><><><><"));
	}

	/**
	 * Initialize session details.
	 *
	 */
	@BeforeClass
	public void ini() throws FileSystemException {

		// initialize the file system if required
		if (fsManager == null) {
			fsManager = VFS.getManager();
			logDebugMessage("Successfully initialized the File System Manager");
		}
	}

	/**
	 * Setup the required information to start testing
	 * 
	 * 
	 */
	public void setupTestProp() {

		try {
			// let's initialize the property file before running any tests
			InputStream input = new FileInputStream(PORTAL_TEST_SUITE_PROP_FILE);
			prop = new Properties();
			prop.load(input);
			logInfoMessage(concatStrings("The '", PORTAL_TEST_SUITE_PROP_FILE,
					"' file was successfully initialized from AbstractTesting class setTestSuiteProp method"));

			String protocol = "https://";
			this.automationSrc = prop.getProperty("test_suite_aut_src");
			if (getAutomationSource().toLowerCase().equals("jenkins")) {
				// let's load all the jenkins details from the properties file
				this.portalType = System.getenv("PORTAL_TYPE");
				this.populateDataMethod = System.getenv("POPULATE_DATA_METHOD");
				String instanceName = System.getenv("RUN_ON_INSTANCE");
				String portalUrl;
				if (System.getenv("PORTAL_URL").equals("staging")) {
					portalUrl = "-portal-staging";
					this.awsCloudFrontOriginBucketName = S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_STAGING;
					this.awsDistributionId = S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_STAGING;
				} else if (System.getenv("PORTAL_URL").equals("development")) {
					portalUrl = "-portal";
					this.awsCloudFrontOriginBucketName = S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_DEV;
					this.awsDistributionId = S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_DEV;
				} else {
					throw (new IllegalArgumentException(concatStrings("The PORTAL_URL '", System.getenv("PORTAL_URL"),
							"' is not an expected value")));
				}
				
				this.standaloneUrlMakePayment = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_make_payment"));
				this.embeddedUrlMakePayment = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_make_payment"));
				this.instanceIdMakePayment = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_make_payment"));
				
				this.standaloneUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_move_in"));
				this.embeddedUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_move_in"));
				this.thirdPartyPrefillUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_move_in"));
				this.instanceIdMoveIn = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_move_in"));
				
				this.standaloneUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_move_out"));
				this.embeddedUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_move_out"));
				this.thirdPartyPrefillUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_move_out"));
				this.instanceIdMoveOut = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_move_out"));
				
				this.standaloneUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_connection"));
				this.embeddedUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_connection"));
				this.thirdPartyPrefillUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_connection"));
				this.instanceIdConnection = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_connection"));

				this.standaloneUrlCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_customer_portal"));
				this.embeddedUrlCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_customer_portal"));
				this.instanceIdCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_customer_portal"));

				this.crmUrl = concatStrings(protocol, instanceName, prop.getProperty("test_suite_crm_url"));
				this.dbUrl = concatStrings(prop.getProperty("test_suite_db_login_url"), instanceName);
				this.awsPortalConfigFolderName = concatStrings(instanceName,
						prop.getProperty("test_suite_aws_portal_config_folder_name"));
				this.awsCrmArtifactsFolderName = concatStrings(
						prop.getProperty("test_suite_aws_crm_artifacts_folder_name"), instanceName);
				this.awsEngineArtifactsFolderName = concatStrings(
						prop.getProperty("test_suite_aws_engine_artifacts_folder_name"), instanceName);
			} else {
				// let's load all the details from the properties file
				this.portalType = prop.getProperty("test_suite_portal_type");
				this.populateDataMethod = prop.getProperty("test_suite_populate_data_method");
				String instanceName = prop.getProperty("test_suite_instance_name");
				String portalUrl;
				if (prop.getProperty("test_suite_portal_url").equals("staging")) {
					portalUrl = "-portal-staging";
					this.awsCloudFrontOriginBucketName = S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_STAGING;
					this.awsDistributionId = S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_STAGING;
				} else if (prop.getProperty("test_suite_portal_url").equals("development")) {
					portalUrl = "-portal";
					this.awsCloudFrontOriginBucketName = S3_PORTAL_CLOUDFRONT_ORIGIN_BUCKET_NAME_DEV;
					this.awsDistributionId = S3_PORTAL_CLOUDFRONT_DISTRIBUTION_ID_DEV;
				} else {
					throw (new IllegalArgumentException(concatStrings("The portal url '",
							prop.getProperty("test_suite_portal_url"), "' is not an expected value")));
				}
				
				this.standaloneUrlMakePayment = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_make_payment"));
				this.embeddedUrlMakePayment = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_make_payment"));
				this.instanceIdMakePayment = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_make_payment"));
				
				this.standaloneUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_move_in"));
				this.embeddedUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_move_in"));
				this.thirdPartyPrefillUrlMoveIn = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_move_in"));
				this.instanceIdMoveIn = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_move_in"));
				
				this.standaloneUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_move_out"));
				this.embeddedUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_move_out"));
				this.thirdPartyPrefillUrlMoveOut = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_move_out"));
				this.instanceIdMoveOut = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_move_out"));
				
				this.standaloneUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_connection"));
				this.embeddedUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_connection"));
				this.thirdPartyPrefillUrlConnection = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_3rdpartyprefill_url_connection"));
				this.instanceIdConnection = concatStrings(instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_connection"));

				this.standaloneUrlCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_standalone_url_customer_portal"));
				this.embeddedUrlCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_embedded_url_customer_portal"));
				this.instanceIdCustomerPortal = concatStrings(protocol, instanceName, portalUrl,
						prop.getProperty("test_suite_instance_id_customer_portal"));
				
				this.crmUrl = concatStrings(protocol, instanceName, prop.getProperty("test_suite_crm_url"));
				this.dbUrl = concatStrings(prop.getProperty("test_suite_db_login_url"), instanceName);
				this.awsPortalConfigFolderName = concatStrings(instanceName,
						prop.getProperty("test_suite_aws_portal_config_folder_name"));
				this.awsCrmArtifactsFolderName = concatStrings(
						prop.getProperty("test_suite_aws_crm_artifacts_folder_name"), instanceName);
				this.awsEngineArtifactsFolderName = concatStrings(
						prop.getProperty("test_suite_aws_engine_artifacts_folder_name"), instanceName);
			}
			this.crmUsername = prop.getProperty("test_suite_crm_username");
			this.crmPassword = prop.getProperty("test_suite_crm_password");
			
			this.dbUsername = prop.getProperty("test_suite_db_login_username");
			this.dbPassword = prop.getProperty("test_suite_db_login_password");
			
			this.awsAccessKeyId = prop.getProperty("test_suite_aws_access_key_id");
			this.awsSecretAccessKey = prop.getProperty("test_suite_aws_secret_access_key");
			
			this.runInvalidation = Boolean.valueOf(prop.getProperty("test_suite_run_invalidation").toLowerCase());
		} catch (FileNotFoundException fnfe) {
			logFatalMessage("Property File was not found on the system");
		} catch (IOException ioe) {
			logFatalMessage("An error occured while loading the file input");
		}

		if (getAutomationSource().toLowerCase().equals("local")) {
			// unless the method to connect to the DB is changed,
			// we will not be able to connect to the database locally
			// it's because of the restrictions added on the dev databases
			logDebugMessage("No need to connect to the database since we are testing locally");
		} else {
			// will only be able to connect to the DB using the current code
			// from the windows machines
			try {
				TestDatabase.open(dbUrl, dbUsername, dbPassword);
				logDebugMessage("Successfully opened the connection to the Database");
			} catch (ClassNotFoundException cnte) {
				logFatalMessage(
						"ClassNotFoundException occurred while initializing the connection to the database. See this message for more details -> "
								+ cnte);
			} catch (SQLException sqle) {
				logFatalMessage(
						"SQLException occurred while initializing the connection to the database. See this message for more details -> "
								+ sqle);
			}
		}
	}

	/**
	 * Let's navigate into the test portal
	 */
	public void accessPortal(String url, boolean maximizeWindow) {

		goToUrl(url, false);
		if (maximizeWindow) {
			driver.manage().window().maximize();
			logDebugMessage(concatStrings("Successfully accessed the site using the URL:\n", url,
					"\nand maximizeWindow [", String.valueOf(maximizeWindow), "]"));

		} else {
			logDebugMessage(concatStrings("Successfully accessed the site using the URL:\n", url,
					"\nand maximizeWindow [", String.valueOf(maximizeWindow), "]"));
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		pauseSeleniumExecution(PORTAL_WAIT_TIME_AFTER_ACCESSING);
	}
	
	/** 
	 * Use this for the error message for soft assertions
	 * */
	public String assertionErrorMsg(String lineNumber) {
		
		String errorMsg = concatStrings("Assertion error in line number [", lineNumber, "] -");
		return errorMsg;
	}

	/**
	 * Use this method to hit the back navigation of the browser
	 */
	public void browserBack() {

		driver.navigate().back();
		pauseSeleniumExecution(500);
	}

	/**
	 * Would be used for browser related alerts (e.g. Do you want to leave this
	 * site? ) Passing true args will accept the dialog while passing false will
	 * just dismiss the dialog and with no action taken
	 */
	public void browserPopupAccept(boolean accept) {

		Alert alert = driver.switchTo().alert();
		logDebugMessage("Switching to the alert prompted by the browser");

		if (accept) {
			alert.accept();
			logDebugMessage("Accepted the alert prompted by the browser");
		} else {
			alert.dismiss();
			logDebugMessage("Dismissed the alert prompted by the browser");
		}
	}

	/**
	 * Clean the download director of any files.
	 * 
	 * @throws FileSystemException
	 */
	public void cleanDownloadDir() throws FileSystemException {

		// Create the directory structure if it dosen't exit, no changes if it does
		File dir = new File(DOWNLOADS_DIR);
		if (!dir.exists())
			dir.mkdirs();

		// get the file object that was specified and ensure we are dealing with
		// a file an not directory
		FileObject[] mainFiles = fsManager.resolveFile(DOWNLOADS_DIR).getChildren();
		for (int i = 0; i < mainFiles.length; i++) {
			// ensure we are only working with files,
			// if we are try to delete it
			if (mainFiles[i].getType() == FileType.FILE)
				mainFiles[i].delete();
		}

		logInfoMessage("Done deleting the files in the download directory -> " + DOWNLOADS_DIR);
	}

	/***
	 * Use this to concatenate certain strings into one
	 */
	public String concatStrings(String... strings) {

		String value;
		List<String> listOfStrings = Arrays.asList(strings);
		StringBuilder stringBuilder = new StringBuilder();

		for (String string : listOfStrings) {
			stringBuilder.append(string);
		}
		value = stringBuilder.toString();

		return value;
	}

	/**
	 * Use this to choose an address from the Supply Address dropdown
	 * 
	 * @param elementDivList
	 * @param location       pass 1 as the first option from the drop down list
	 */
	public void chooseFromList(WebElement elementDivList, int location) {

		// index starts with 0,1,2,etc...
		int loc = location - 1;
		List<WebElement> matOptions = elementDivList
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));

		matOptions.get(loc).click();
		logDebugMessage(concatStrings("Clicked the option from List in location [", String.valueOf(location), "]"));
	}
	
	/**
	 * Use this to choose an address by specifying the address name
	 * 
	 * @param elementDivList
	 * @param startsWithAddress
	 */
	public void chooseAddress(WebElement elementDivList, String... startsWithAddress) {

		List<WebElement> matOptions = elementDivList
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));
		List<String> addressesToChooseFrom = Arrays.asList(startsWithAddress);
		boolean addressFound = false;
		for (WebElement result : matOptions) {
			if (!addressFound) {
				String addressName;
				try {
					addressName = result.getText();
				} catch (StaleElementReferenceException sere) {
					PageFactory.initElements(driver, this);
					addressName = result.getText();
				}
				// let's remove the word 'place' that appears at the start of the returned addresses
				addressName = getString(addressName, 6);
				addressName = StringUtils.normalizeSpace(addressName.toLowerCase());
				for (String addressToChooseFrom : addressesToChooseFrom) {
					addressToChooseFrom = StringUtils.normalizeSpace(addressToChooseFrom.toLowerCase());
					logDebugMessage(concatStrings("Validating that this actual address '", addressName, "' starts with '",
							addressToChooseFrom, "'"));
					if (addressName.startsWith(addressToChooseFrom)) {
						result.click();
						addressFound = true;
						logDebugMessage(concatStrings("Clicked the option from the List that starts with address '",
								addressToChooseFrom, "'"));
						break;
					}
				}
			} else {
				break;
			}
		}
		assertTrue(addressFound, concatStrings("Unable to find the address that starts with either of the following:\n",
				addressesToChooseFrom.toString()));
	}

	/**
	 * Use this to choose a Tenancy Type from the Supply Details
	 */
	public void chooseTenancyType(WebElement elementDivList, String startsWithTenancyType) {

		elementDivList.sendKeys(startsWithTenancyType);
		pauseSeleniumExecution(1000);
		List<WebElement> matOptions = elementDivList
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));
		for (WebElement element : matOptions) {
			String tenancyType = element.getText();
			tenancyType = StringUtils.normalizeSpace(tenancyType);
			logDebugMessage(concatStrings("Validating that this Tenancy Type in the portal '", tenancyType,
					"' starts with '", startsWithTenancyType, "'"));
			if (tenancyType.toLowerCase().startsWith(startsWithTenancyType.toLowerCase())) {
				element.click();
				logDebugMessage(concatStrings("Clicked the option from the List that starts with Tenancy Type '",
						startsWithTenancyType, "'"));
				break;
			}
		}
	}

	/**
	 * Use this to click an attachment from the email table
	 * 
	 */
	public void clickAttachmentFromEmail(String exactLinkName) {

		String exactLinkNameUpd = StringUtils.normalizeSpace(exactLinkName);
		WebElement emailTable = getEmailTable();
		emailTable.findElement(By.linkText(exactLinkNameUpd)).click();
		logDebugMessage(concatStrings("Clicked the exactLinkNameUpd '", exactLinkNameUpd, "'"));
	}

	/**
	 * Use this mostly if you click on Next, Previous and Submit
	 * 
	 * To avoid the ElementNotInteractableException,
	 * ElementClickInterceptedException
	 * 
	 */
	public void clickElementAction(WebElement element) {

		try {
			logDebugMessage(concatStrings("Going to click the element ", element.toString()));
			setImplicitWait(0);
			Actions actions = new Actions(driver);
			actions.moveToElement(element).click().perform();
			logDebugMessage("Successfully clicked the element");
		} catch (Exception e) {
			logDebugMessage(concatStrings("An exception has been encountered while trying to click the element {",
					element.toString(), "}. Please see message for more details -> ", e.getMessage()));
			throw e;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to click the link name that contains the string from the given
	 * element
	 * 
	 */
	public void clickContainsLinkNameFromElement(WebElement elementLoc, String containsLinkName) {

		logDebugMessage(concatStrings("Will be clicking the link name that contains '", containsLinkName,
				"' from the supplied WebElement {", elementLoc.toString(), "}"));
		WebElement element = elementLoc.findElement(By.partialLinkText(containsLinkName));
		clickElementAction(element);
	}

	/**
	 * Use this to click the exact link name from the given element
	 * 
	 */
	public void clickExactLinkNameFromElement(WebElement elementLoc, String exactLinkName) {

		logDebugMessage(concatStrings("Will be clicking the exact link name '", exactLinkName,
				"' from the supplied WebElement {", elementLoc.toString(), "}"));
		WebElement element = elementLoc.findElement(By.linkText(exactLinkName));
		clickElementAction(element);
	}

	/** 
	 * 
	 * */
	public void clickEscapeKey() {

		Actions action = new Actions(driver);
		action.sendKeys(Keys.ESCAPE).build().perform();
		logDebugMessage("Escape key clicked");
	}

	/**
	 * Use this to clear the local and session storage. You need to open the browser
	 * and access a page before you can use this method.
	 */
	public void clearLocalAndSessionStorage() {

		logDebugMessage("Will be clearing the browser local and session storage");
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		storage.clearLocalStorage();
		storage.clearSessionStorage();
		logDebugMessage("Successfully cleared the browser local and session storage");
	}

	/**
	 * We used the Backspace because the native webElement.clear() method does not
	 * work
	 */
	public void clearDateField(WebElement dateField) {

		clickElementAction(dateField);
		pauseSeleniumExecution(1000);
		dateField.sendKeys(Keys.END, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE);
	}

	/**
	 * If you want to copy a certain text that was previously selected.
	 * 
	 * Used in conjunction with selectAllTextFromField() method.
	 * 
	 * Make sure the focus is still on the field and text are already selected.
	 * 
	 * This is the shortcut Control + C for Windows.
	 * 
	 */
	public void copyTextFromField() {

		Actions actions = new Actions(driver);
		actions.keyDown(Keys.CONTROL);
		actions.sendKeys("c");
		actions.keyUp(Keys.CONTROL);
		actions.perform();

		logDebugMessage("Successfully executed the copyTextFromField() method");
	}

	/**
	 * Close the opened windows.
	 *
	 * @param keepCurrentWindow pass true if you want to keep current window
	 */
	public void closeOpenedWindows(boolean keepCurrentWindow) {

		// get the current window in case we do not want to close it
		String currentWindow = driver.getWindowHandle();

		// go through all the windows and close them if required
		for (String handle : driver.getWindowHandles()) {
			// only close the next window if we don't want to keep the
			// current'one
			// or if it's not the current window
			if (!handle.equals(currentWindow) || !keepCurrentWindow) {
				logDebugMessage(concatStrings("Will be closing window '", currentWindow, "'"));
				driver.switchTo().window(handle);
				driver.close();
				logDebugMessage("Trying to close the driver/browser");
				if (isBrowserAlertDisplayed()) {
					browserPopupAccept(true);
				}
				logDebugMessage(concatStrings("Finished closing the window '", currentWindow, "'"));
			}
		}
		// get the current window in case we do not want to close it
		if (keepCurrentWindow) {
			logDebugMessage(concatStrings(
					"Will be closing all the windows opened, but will retain the current window. Will be switching to current window '",
					currentWindow, "'"));
			driver.switchTo().window(currentWindow);
			logDebugMessage(concatStrings("Successfully switched to current window '", currentWindow, "'"));
		} else {
			logDebugMessage("Will be closing all the windows opened");
		}
	}

	/**
	 * This is the shortcut Control + A + Delete for windows.
	 * 
	 * Make sure the focus is already in the field where you want the text to be
	 * deleted.
	 * 
	 */
	public void deleteAllTextFromField() {

		Actions actions = new Actions(driver);
		actions.keyDown(Keys.CONTROL);
		actions.sendKeys("a");
		actions.sendKeys(Keys.DELETE);
		actions.keyUp(Keys.CONTROL);
		actions.perform();

		logDebugMessage("Successfully executed the deleteAllTextFromField() method");
	}

	/**
	 * Use this to delete files that were uploaded in the upload section
	 * 
	 * e.g. Showing the trash bin icon
	 * 
	 * 
	 */
	public void deleteUploadedFiles(List<WebElement> uploadedFiles, String fileNameToDelete) {

		WebElement foundElement = null;
		logDebugMessage(concatStrings("Will be looking for the element in the upload section that has the file name '",
				fileNameToDelete, "'"));

		if (uploadedFiles != null && uploadedFiles.size() > 0) {
			for (WebElement webElement : uploadedFiles) {
				String webElementStr = webElement.getAttribute("data-file-name");
				if (webElementStr.equals(fileNameToDelete)) {
					foundElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString(),
							" that has the file name '", fileNameToDelete, "'"));
					break;
				}
			}
		}

		if (foundElement == null) {
			throw (new IllegalArgumentException(
					concatStrings("Could not find the file name '", fileNameToDelete, "' to delete")));
		} else {
			WebElement deleteBtn = foundElement.findElement(By.xpath(".//span[@class='e-icons e-file-delete-btn']"));
			clickElementAction(deleteBtn);
			logDebugMessage(concatStrings("Clicked the delete button for the uploaded file '", fileNameToDelete, "'"));
		}
	}
	
	/**
	 * Use this to remove the global language files
	 */
	public void deleteGlobalLangFiles(AccessS3BucketWithVfs s3Access, PortalNamesEnum portalNameEnum,
			String... langFilesToDelete) {

		for (String langFile : langFilesToDelete) {
			s3Access.deleteGlobalLangFile(getCloudFrontOriginBucketName(), portalNameEnum.getLabelText(),
					S3_PORTAL_LANG_FILES_DIR, langFile);
		}
	}
	
	/**
	 * Use this to remove the custom language files
	 */
	public void deleteCustomLangFiles(AccessS3BucketWithVfs s3Access, PortalNamesEnum portalNameEnum,
			String... langFilesToDelete) {

		for (String langFile : langFilesToDelete) {
			s3Access.deleteCustomLangFile(getAwsPortalConfigFolderName(), portalNameEnum.getLabelText(),
					S3_PORTAL_LANG_FILES_DIR, langFile);
		}
	}

	/**
	 * Execute a query and get the first row, first column result and return it.
	 *
	 * @param query
	 * @return the first column of the first row in the result set
	 */
	public static String executeQuery(String query) throws SQLException {

		String result = null;

		if (null != TestDatabase.getInstance() && null != TestDatabase.getInstance().getConnection()) {
			// we have an active connection
			final Statement stmt = TestDatabase.getInstance().getConnection().createStatement();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Will be running this query in the DB -> '" + query + "'");
			}
			final ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				result = rs.getString(1);
			rs.close();
			stmt.close();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("The result of the query is -> '" + result + "'");
		}

		// return the result of the query
		return (result);
	}

	/**
	 * Run a query to update a data in the database.
	 *
	 * @param query
	 */
	public static void executeUpdate(String query) throws SQLException {

		if (null != TestDatabase.getInstance() && null != TestDatabase.getInstance().getConnection()) {
			// we have an active connection
			final Statement stmt = TestDatabase.getInstance().getConnection().createStatement();
			stmt.executeUpdate(query);
			stmt.close();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Execution has been completed for -> '" + query + "'");
			}
		}
	}

	/**
	 * Execute some SQL scripts from a file.
	 *
	 * @param sqlResourcePath
	 * @param continueOnError
	 * @throws DataAccessException
	 */
	@SuppressWarnings("deprecation")
	public void executeSqlScript(String sqlResourcePath, boolean continueOnError) {

		try {
			if (null != jdbcTemplate) {
				logDebugMessage("Executing SQL script " + sqlResourcePath + " ... ");

				appContext = new ClassPathXmlApplicationContext("spring_blueoak_portal.xml");
				Resource resource = appContext.getResource(TEST_DATA_LOCATION + sqlResourcePath + ".sql");
				JdbcTestUtils.executeSqlScript(jdbcTemplate, resource, continueOnError);

				logDebugMessage("Execution has been completed");
			} else {
				logErrorMessage("JDBC template is not initialized");
			}
		} catch (ScriptStatementFailedException ssfe) {
			logFatalMessage("A ScriptStatementFailedException is encountered. See message for more details -> "
					+ ssfe.getMessage());
			throw (new IllegalArgumentException(
					"ScriptStatementFailedException exception occurred when executing the sql file in '"
							+ sqlResourcePath + "'. See message for more details -> " + ssfe));
		} catch (DataAccessException dae) {
			logFatalMessage(
					"A DataAccessException is encountered. See message for more details -> " + dae.getMessage());
			throw (new IllegalArgumentException(
					"DataAccessException exception occurred when executing the sql file in '" + sqlResourcePath
							+ "'. See message for more details -> " + dae));
		}
	}

	/**
	 * Use this to switch to the Move In Iframe at the start of the test case.
	 * 
	 * Only applicable for embedded Move In portal.
	 */
	public void embeddedMoveInSwitchFrame(long implicitWaitInSec) {

		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-In Iframe
			switchToMoveInEmbeddedIframe(implicitWaitInSec);
		}
	}
	
	/**
	 * Use this to switch to the Move Out Iframe at the start of the test case.
	 * 
	 * Only applicable for embedded Move Out portal.
	 */
	public void embeddedMoveOutSwitchFrame(long implicitWaitInSec) {

		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-In Iframe
			switchToMoveOutEmbeddedIframe(implicitWaitInSec);
		}
	}
	
	/**
	 * Use this to switch to the Connection Iframe at the start of the test case.
	 * 
	 * Only applicable for embedded Move Out portal.
	 */
	public void embeddedConnectionSwitchFrame(long implicitWaitInSec) {

		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-In Iframe
			switchToConnectionEmbeddedIframe(implicitWaitInSec);
		}
	}

	/**
	 * Use this to switch to the Customer Portal Iframe at the start of the test
	 * case.
	 * 
	 * Only applicable for embedded Customer Portal.
	 */
	public void embeddedCustomerPortalSwitchFrame(long implicitWaitInSec) {

		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-In Iframe
			switchToCustomerPortalEmbeddedIframe(implicitWaitInSec);
		}
	}

	/**
	 * Use this to format a string date to any date format
	 * 
	 * @param dateToFormat
	 * @param formatOfdateToFormat is the date format of the passed dateToFormat
	 *                             args
	 * 
	 * @throws ParseException
	 */
	public String formatDateToAnyDateFormat(String dateToFormat, SimpleDateFormat formatOfdateToFormat,
			SimpleDateFormat dateFormatToReturn) throws ParseException {

		String formattedVal = dateFormatToReturn.format(formatOfdateToFormat.parse(dateToFormat));

		logDebugMessage(
				"The String to be returned of method formatDateToAnyDateFormat(String, SimpleDateFormat, SimpleDateFormat) is '"
						+ formattedVal + "'");
		return formattedVal;
	}

	/**
	 * Use this to get the portal type
	 * 
	 */
	public String getPortalType() {

		return portalType;
	}

	/**
	 * Use this to get how the data is populated in the portal
	 */
	public String getPopulateDataMethod() {

		return populateDataMethod;
	}

	/**
	 * Get the url for the standalone portal we are testing for Make Payment
	 */
	public String getStandaloneUrlMakePayment() {

		return standaloneUrlMakePayment;
	}

	/**
	 * Get the url for the embedded portal we are testing for Make Payment
	 */
	public String getEmbeddedUrlMakePayment() {

		return embeddedUrlMakePayment;
	}

	/**
	 * Get the instance id for the payment portal for Make Payment
	 */
	public String getInstanceIdMakePayment() {

		return instanceIdMakePayment;
	}

	/**
	 * Get the url for the standalone portal we are testing for Move In
	 */
	public String getStandaloneUrlMoveIn() {

		return standaloneUrlMoveIn;
	}

	/**
	 * Get the url for the embedded portal we are testing for Move In
	 */
	public String getEmbeddedUrlMoveIn() {

		return embeddedUrlMoveIn;
	}

	public String getThirdPartyPrefillUrlMoveIn() {

		return thirdPartyPrefillUrlMoveIn;
	}

	/**
	 * Get the instance id for the for Move In
	 */
	public String getInstanceIdMoveIn() {

		return instanceIdMoveIn;
	}

	/**
	 * Get the url for the standalone portal we are testing for Move Out
	 */
	public String getStandaloneUrlMoveOut() {

		return standaloneUrlMoveOut;
	}

	/**
	 * Get the url for the embedded portal we are testing for Move Out
	 */
	public String getEmbeddedUrlMoveOut() {

		return embeddedUrlMoveOut;
	}

	public String getThirdPartyPrefillUrlMoveOut() {

		return thirdPartyPrefillUrlMoveOut;
	}

	/**
	 * Get the instance id for the portal for Move Out
	 */
	public String getInstanceIdMoveOut() {

		return instanceIdMoveOut;
	}
	
	/**
	 * Get the url for the standalone portal we are testing for Connection
	 */
	public String getStandaloneUrlConnection() {

		return standaloneUrlConnection;
	}

	/**
	 * Get the url for the embedded portal we are testing for Connection
	 */
	public String getEmbeddedUrlConnection() {

		return embeddedUrlConnection;
	}

	public String getThirdPartyPrefillUrlConnection() {

		return thirdPartyPrefillUrlConnection;
	}

	/**
	 * Get the instance id for the portal for Connection
	 */
	public String getInstanceIdConnection() {

		return instanceIdConnection;
	}

	/**
	 * Get the url for the standalone portal we are testing for Customer Portal
	 */
	public String getStandaloneUrlCustomerPortal() {

		return standaloneUrlCustomerPortal;
	}

	/**
	 * Get the url for the embedded portal we are testing for Customer Portal
	 */
	public String getEmbeddedUrlCustomerPortal() {

		return embeddedUrlCustomerPortal;
	}

	/**
	 * Get the instance id for the portal for Customer Portal
	 */
	public String getInstanceIdCustomerPortal() {

		return instanceIdCustomerPortal;
	}

	/**
	 * Use this to get the CRM url
	 */
	public String getCRMUrl() {

		return crmUrl;
	}

	/**
	 * Use this to get the CRM username
	 */
	public String getCRMUsername() {

		return crmUsername;
	}

	/**
	 * Use this to get the CRM password
	 */
	public String getCRMPassword() {

		return crmPassword;
	}

	/**
	 * Use this to get the automation source
	 */
	public String getAutomationSource() {

		return automationSrc;
	}

	/**
	 * Use this to get the AWS Access Key Id
	 * 
	 */
	public String getAwsAccessKeyId() {

		return awsAccessKeyId;
	}

	/**
	 * Use this to get the AWS Secret Access Key
	 */
	public String getAwsSecretAccessKey() {

		return awsSecretAccessKey;
	}

	/**
	 * Use this to get the AWS S3 folder name of the portal we are testing
	 */
	public String getAwsPortalConfigFolderName() {

		return awsPortalConfigFolderName;
	}

	/**
	 * Use this to get the AWS S3 folder name for the crm artifacts
	 */
	public String getAwsCrmArtifactsFolderName() {

		return awsCrmArtifactsFolderName;
	}

	/**
	 * Use this to get the AWS S3 folder name for the engine artifacts
	 */
	public String getAwsEngineArtifactsFolderName() {

		return awsEngineArtifactsFolderName;
	}

	/**
	 * Use this to get the bucket name where the codes are deployed
	 */
	public String getCloudFrontOriginBucketName() {

		return awsCloudFrontOriginBucketName;
	}
	
	/** 
	 * Use this to get the Distribution ID from the portal codes
	 * are deployed
	 * */
	public String getDistributionId() {
		
		return awsDistributionId;
	}
	
	/** 
	 * 
	 * */
	public boolean getRunInvalidation() {
		
		return runInvalidation;
	}

	/**
	 * Use this to get the css on the supplied property name
	 */
	public String getCssProp(WebElement element, String propName) {

		String cssValue = element.getCssValue(propName);
		return cssValue;
	}
	
	/** 
	 * 
	 * */
	public String getCssBorderProp(WebElement element) {

		String border = getCssProp(element, "border");
		return border;
	}

	/**
	 * Use this to get the CSS background color of the specified element.
	 * 
	 * Useful for checking the border colors in radio buttons.
	 */
	public String getCssBorderColorProp(WebElement element) {

		String borderColor = getCssProp(element, "border-color");
		return borderColor;
	}
	
	/**
	 * Use this to get the CSS background of the specified element
	 */
	public String getCssBackgrndProp(WebElement element) {

		String background = getCssProp(element, "background");
		return background;
	}

	/**
	 * Use this to get the CSS background color of the specified element
	 */
	public String getCssBackgrndColorProp(WebElement element) {

		String backgroundColor = getCssProp(element, "background-color");
		return backgroundColor;
	}

	/**
	 *
	 */
	public String getCssBoxShadowProp(WebElement element) {

		String boxShadow = getCssProp(element, "box-shadow");
		return boxShadow;
	}
	
	/**
	 * Use this to get the CSS color of the specified element
	 */
	public String getCssColorProp(WebElement element) {

		String color = getCssProp(element, "color");
		return color;
	}
	
	/**
	 * Use this to get the CSS color of the specified element
	 */
	public String getCssFillProp(WebElement element) {

		String color = getCssProp(element, "fill");
		return color;
	}

	/**
	 * Use this to get the CSS font family/type of the specified element
	 */
	public String getCssFontFamilyProp(WebElement element) {

		String fontFamily = getCssProp(element, "font-family");
		return fontFamily;
	}

	/**
	 * Use this to get the CSS font size of the specified element
	 */
	public String getCssFontSizeProp(WebElement element) {

		String fontSize = getCssProp(element, "font-size");
		return fontSize;
	}
	
	/** 
	 * 
	 * */
	public String getCssStrokeProp(WebElement element) {

		String fontSize = getCssProp(element, "stroke");
		return fontSize;
	}
	
	/** 
	 * 
	 * */
	public String getCssBorderBottomProp(WebElement element) {
		
		String borderBottom = getCssProp(element, "border-bottom");
		return borderBottom;
	}
	
	/** 
	 * Use this to get the usual CSS for label elements.
	 * 
	 * Don't rearrange the arguments passed in cssValuesToGet variable because there's
	 * already expected assertions using this arrangement.
	 * */
	public List<String> getLabelCss(WebElement element) {

		List<String> cssValuesToGet = Arrays.asList(getCssColorProp(element), getCssFontSizeProp(element),
				getCssFontFamilyProp(element));
		List<String> cssValues = new ArrayList<>();

		int counter = 0;
		while (counter < cssValuesToGet.size()) {
			String cssVal = cssValuesToGet.get(counter);
			cssValues.add(cssVal);
			counter++;
		}

		return cssValues;
	}
	
	/** 
	 * Use this to get the usual CSS for buttons previous, next, etc...
	 * 
	 * Don't rearrange the arguments passed in cssValuesToGet variable because there's
	 * already expected assertions using this arrangement.
	 * */
	public List<String> getButtonCss(WebElement element) {

		List<String> cssValuesToGet = Arrays.asList(getCssBackgrndColorProp(element), getCssColorProp(element),
				getCssFontSizeProp(element), getCssFontFamilyProp(element));
		List<String> cssValues = new ArrayList<>();

		int counter = 0;
		while (counter < cssValuesToGet.size()) {
			String cssVal = cssValuesToGet.get(counter);
			cssValues.add(cssVal);
			counter++;
		}

		return cssValues;
	}
	
	/** 
	 * This is for the AM/PM button in the Trade Waste
	 * */
	public List<String> getAmPmButtonCss(WebElement element) {

		List<String> cssValuesToGet = Arrays.asList(getCssBackgrndColorProp(element), getCssBorderColorProp(element),
				getCssColorProp(element), getCssFontSizeProp(element), getCssFontFamilyProp(element));
		List<String> cssValues = new ArrayList<>();

		int counter = 0;
		while (counter < cssValuesToGet.size()) {
			String cssVal = cssValuesToGet.get(counter);
			cssValues.add(cssVal);
			counter++;
		}

		return cssValues;
	}
	
	/** 
	 * 
	 * */
	public String getRadioCheckboxOuterCss(WebElement radioCheckboxOuter) {
		
		String radioCheckboxOuterCss = getCssBorderColorProp(radioCheckboxOuter);
		return radioCheckboxOuterCss;
	}
	
	/** 
	 * 
	 * */
	public String getRadioCheckboxInnerCss(WebElement radioCheckboxInner) {
		
		String radioCheckboxInnerCss = getCssBackgrndColorProp(radioCheckboxInner);
		return radioCheckboxInnerCss;
	}
	
	/**
	 * 
	 * */
	public String getMatPseudoChkbxOuterCss(WebElement matPseudoCheckboxElement) {

		String matPseudoChkbxOuterCss = getCssColorProp(matPseudoCheckboxElement);
		return matPseudoChkbxOuterCss;
	}
	
	/** 
	 * 
	 * */
	public String getMatPseudoChkbxInnerCss(WebElement matPseudoCheckboxElement) {
		
		String matPseudoChkbxInnerCss = getCssBackgrndColorProp(matPseudoCheckboxElement);
		return matPseudoChkbxInnerCss;
	}
	
	/** 
	 * 
	 * */
	public List<String> getPlaceholderCss(List<WebElement> labelInput, String placholderLabel) {

		List<String> placeholderCss = getLabelCss(
				getElementFrmLblNameInput(labelInput, true, placholderLabel));
		return placeholderCss;
	}
	
	/** 
	 * Use this to get the usual CSS for Make Payment CSS for processing fee
	 * 
	 * Don't rearrange the arguments passed in cssValuesToGet variable because there's
	 * already expected assertions using this arrangement.
	 * */
	public List<String> getProcessingFeeCss(WebElement element) {

		List<String> cssValuesToGet = Arrays.asList(getCssBackgrndColorProp(element), getCssColorProp(element),
				getCssBorderProp(element), getCssFontSizeProp(element), getCssFontFamilyProp(element));
		List<String> cssValues = new ArrayList<>();

		int counter = 0;
		while (counter < cssValuesToGet.size()) {
			String cssVal = cssValuesToGet.get(counter);
			cssValues.add(cssVal);
			counter++;
		}

		return cssValues;
	}
	
	/** 
	 * 
	 * */
	public String getUnderlineCss(WebElement underlineElement) {
		
		String underlineCss = getCssBackgrndColorProp(underlineElement);
		return underlineCss;
	}
	
	/** 
	 * 
	 * */
	public String getUnderlineMWPayframeNotFocused(WebElement underlineElement) {
		
		String underlineCss = getCssBorderBottomProp(underlineElement);
		return underlineCss;
	}
	
	/** 
	 * 
	 * */
	public String getUnderlineMWPayframeFocusedOrError(WebElement underlineElement) {
		
		String underlineCss = getCssBorderColorProp(underlineElement);
		return underlineCss;
	}
	
	/**
	 * 
	 * */
	public String getUploadAreaBorderCss(WebElement uploadAreaBorderElement) {

		String css = getCssBorderProp(uploadAreaBorderElement);
		return css;
	}
	
	/** 
	 * This is the dialog container when removing uploaded files or contacts
	 * */
	public List<String> getDialogContainerCss(WebElement element) {

		List<String> cssValuesToGet = Arrays.asList(getCssBoxShadowProp(element), getCssBackgrndProp(element),
				getCssColorProp(element), getCssFontSizeProp(element), getCssFontFamilyProp(element));
		List<String> cssValues = new ArrayList<>();

		int counter = 0;
		while (counter < cssValuesToGet.size()) {
			String cssVal = cssValuesToGet.get(counter);
			cssValues.add(cssVal);
			counter++;
		}

		return cssValues;
	}
	
	/** 
	 * 
	 * */
	

	/**
	 * If you have this string "quick brown fox jumps over the lazy dog" and
	 * beginIndex=15 and endIndex 20; result would be " jump" notice the space
	 * before the text
	 */
	public String getString(String rawString, int beginIndex, int endIndex) {

		String trimmedString = rawString.substring(beginIndex, endIndex);
		return trimmedString;
	}

	/**
	 * 
	 * Pass the rawString to trim. Say you have the string '44,640 (actual) / 44,640
	 * (expected)' and you want to trim the string from the beginning until the
	 * character '/'; you pass the string args '/' into the untilChar param
	 * 
	 * Based on that example, that would return the string '44,640 (actual) '
	 */
	public String getStringUntil(String rawString, String untilChar) {

		String trimmedString = StringUtils.substringBefore(rawString, untilChar);
		return trimmedString;
	}

	/**
	 * Pass the rawString to trim. Say you have the string '44,640 (actual) / 44,640
	 * (expected)' and you want to trim the string from the '/' character until the
	 * end of the string; you pass the string args '/' into the afterChar param
	 * 
	 * You can also pass a certain string like if you want to get the id from the
	 * URL https://selenium-crm.blueacorns.com.au/#bbcrm_OnlineRequests/10 You can
	 * pass rawString as 'OnlineRequests/'
	 * 
	 * Based on that example, it would return to you string ' 44,640 (expected)'
	 */
	public String getStringAfter(String rawString, String afterChar) {

		String trimmedString = StringUtils.substringAfter(rawString, afterChar);
		return trimmedString;
	}

	/**
	 * If you have this string "Welcome to Tutorialspoint.com" and beginIndex = 10;
	 * result would be " Tutorialspoint.com" notice the space before the text
	 * 
	 * @param beginIndex
	 */
	public String getString(String rawString, int beginIndex) {

		String trimmedString = rawString.substring(beginIndex);
		return trimmedString;
	}

	/**
	 * Use this to get the name of the Test class currently running
	 */
	public String getTestClassExecuting() {

		String testClassName = this.getClass().getSimpleName();
		return testClassName;
	}

	/**
	 * Use this to get the number of addresses displayed on the search result
	 * 
	 * @param matOptionDiv is the div where the mat-option is housed
	 */
	public int getNumOfMatOptionValues(WebElement matOptionDiv) {

		List<WebElement> matOptions = matOptionDiv
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));

		ListIterator<WebElement> listIterator = matOptions.listIterator();
		List<String> stringValueArray = new ArrayList<String>();
		while (listIterator.hasNext()) {
			WebElement value = listIterator.next();
			String stringValueRaw = value.getText();
			String stringValue = StringUtils.normalizeSpace(stringValueRaw);
			stringValueArray.add(stringValue);
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		return stringValueArray.size();
	}

	/**
	 * Use this to get the current year
	 */
	public int getCurrentYear() {

		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		logDebugMessage(
				concatStrings("The value to be returned by getCurrentYear() is ", Integer.toString(currentYear)));
		return currentYear;
	}

	/**
	 * Use this to get the current month.
	 * 
	 * @param returnIndexBased pass true if you want to return an indexed based
	 *                         month (i.e. January would return 0) OR pass false if
	 *                         you want to return the month as not indexed based
	 *                         (i.e. January would return 1)
	 * 
	 */
	public int getCurrentMonth(boolean returnIndexBased) {

		Calendar calendar = Calendar.getInstance();
		int currentMonth;
		if (returnIndexBased) {
			currentMonth = calendar.get(Calendar.MONTH);
			String converted = new SimpleDateFormat("MMM").format(calendar.getTime());
			logDebugMessage(concatStrings("The value to be returned by getCurrentMonth(boolean) is ", converted, "[",
					Integer.toString(currentMonth), "]"));
		} else {
			currentMonth = calendar.get(Calendar.MONTH) + 1;
			logDebugMessage(concatStrings("The value to be returned by getCurrentMonth(boolean) is ",
					Integer.toString(currentMonth)));
		}
		return currentMonth;
	}

	/** 
	 * 
	 * */

	/**
	 * Use this to get the current date with the specified time zone and converts
	 * the format according to the passed args
	 */
	public String getCurrentDateWithTimeZone(TimeZone timeZone, SimpleDateFormat dateFormatToReturn) {

		// create the calendar that holds the current date at the specified time zone
		Calendar calendar = new GregorianCalendar(timeZone);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		String value = dateFormatToReturn.format(calendar.getTime());
		logDebugMessage(
				"The value to be returned for method getCurrentDateWithTimeZone(TimeZone, SimpleDateFormat) is '"
						+ value + "'");
		return value;
	}

	/**
	 * Get a date that is a number of days from the current date at the specified
	 * time zone.
	 *
	 * @param timeZone
	 * @param daysAdj            pass negative value for previous day/months
	 * @param dateFormatToReturn is the format for the value to be returned
	 * 
	 * @return
	 */
	public String getSpecificDateWithTimeZone(TimeZone timeZone, int daysAdj, SimpleDateFormat dateFormatToReturn) {

		// create the calendar that holds the current date at the specified time
		// zone
		Calendar calendar = new GregorianCalendar(timeZone);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// adjust the date as specified
		calendar.add(Calendar.DATE, daysAdj);

		String value = dateFormatToReturn.format(calendar.getTime());
		logDebugMessage(
				"The value to be returned for method getDateInUserTimeZone(TimeZone, int, SimpleDateFormat) is '"
						+ value + "'");
		return value;
	}

	/**
	 * Use this to get the date that has the last date of the month in the given
	 * month and year.
	 * 
	 * 
	 * @param timeZone
	 * @param month              number starts at 1 for January
	 * @param year
	 * @param dateFormatToReturn
	 */
	public String getLastDateOfSpecificMonthYear(TimeZone timeZone, int month, int year,
			SimpleDateFormat dateFormatToReturn) {

		logDebugMessage("We are now inside getDateFromSpecificMonthYear(TimeZone, int, int, SimpleDateFormat) method");
		logDebugMessage(concatStrings("The passed value of month <", Integer.toString(month),
				"> and the value of year <", Integer.toString(year), ">"));
		// Calendar.MONTH is zero indexed based (i.e. January returns 0)
		// so we deduct it first
		int monthUpd = month - 1;
		logDebugMessage(concatStrings("The updated value of month to use is <", Integer.toString(monthUpd),
				"> and the value of year to use is <", Integer.toString(year), ">"));
		// create the calendar that holds the current date at the specified time zone
		Calendar calendar = new GregorianCalendar(timeZone);
		calendar.set(Calendar.MONTH, monthUpd);
		calendar.set(Calendar.YEAR, year);
		int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		logDebugMessage(concatStrings("The lastDayOfMonth to use is <", Integer.toString(lastDayOfMonth), ">"));
		calendar.set(Calendar.DATE, lastDayOfMonth);
		logDebugMessage(concatStrings("The updated calender time is ", calendar.getTime().toString()));

		String value = dateFormatToReturn.format(calendar.getTime());
		logDebugMessage(concatStrings(
				"The value to be returned for method getDateFromSpecificMonthYear(TimeZone, int, int, SimpleDateFormat) is '",
				value, "'"));
		return value;
	}

	/**
	 * Get the Page Title
	 */
	public String getPageTitle() {

		String title = driver.getTitle();
		title = StringUtils.normalizeSpace(title);
		logDebugMessage(concatStrings("The value to be returned by the method getPageTitle() is '", title, "'"));
		return title;
	}

	/**
	 * Use this to get the number of windows opens
	 */
	public int getNumWindowsOpen() {

		// get all the current windows
		Set<String> activeWindowList = driver.getWindowHandles();
		int totalWindowsOpen = activeWindowList.size();
		logDebugMessage(concatStrings("The number of windows open is <", String.valueOf(totalWindowsOpen), ">"));
		return totalWindowsOpen;
	}

	/**
	 * Get all the text inside
	 * <p>
	 * tags in the specified string
	 */
	public List<String> getAllDataInPTag(String htmlData) {

		Document doc = Jsoup.parse(htmlData);
		Elements p = doc.getElementsByTag("p");
		List<String> pTagValues = new ArrayList<String>();
		for (Element x : p) {
			pTagValues.add(x.text());
		}
		// remove the extra comma generated
		pTagValues.removeAll(Arrays.asList("", null));

		logDebugMessage(concatStrings("The value to be returned by getAllDataInPTag(String) is ", pTagValues.toString(),
				" and the size is [", String.valueOf(pTagValues.size()), "]"));
		return pTagValues;
	}

	/**
	 * Use this to get the data of each
	 * <p>
	 * tag in the String body
	 * 
	 * @param indexLoc p tag location. Starts at 1 being the first p tag
	 */
	public String getDataInListByIndex(List<String> data, int indexLoc) {

		int pTagLocFinal = indexLoc - 1;
		String pTextRaw1 = data.get(pTagLocFinal).toString();
		// delete the text that is enclosed in <>
		String pTextRaw2 = pTextRaw1.replaceAll("\\<[^()]*\\>", "");
		// delete the =(equals) sign from the string
		String pTextRaw3 = pTextRaw2.replace("=", "");
		String pText = StringUtils.normalizeSpace(pTextRaw3);
		
		logDebugMessage(
				concatStrings("The value to be returned by getDataInListByIndex(List<String>, int) is '", pText, "'"));
		return pText;
	}

	/**
	 * Use this to get the last 4 digits of a credit card number
	 */
	public String getLast4DigitsOfString(String cardNubmer) {

		String last4Char = "";

		if (cardNubmer.length() > 4) {
			last4Char = cardNubmer.substring(cardNubmer.length() - 4);
		} else {
			last4Char = cardNubmer;
		}

		return last4Char;
	}

	/**
	 * Use this to get the line number for the code that was executed
	 * 
	 */
	public String getLineNumber() {

		int num = new Throwable().getStackTrace()[1].getLineNumber();
		String lineNum = String.valueOf(num);

		return lineNum;
	}

	/**
	 * Use this to get all addresses returned from the Supply Address for: - Move In
	 * - Move Out - Connection
	 * 
	 * @param supplyAddDiv is the div tag where role="listbox"
	 */
	public List<String> getAllSupplyAddress(WebElement supplyAddDiv) {

		List<WebElement> matOptions = supplyAddDiv.findElements(By.xpath(
				"//mat-option[starts-with(@class,'mat-option ') and @role='option' and @name='supply_address_dropdown_values']"));

		ListIterator<WebElement> listIterator = matOptions.listIterator();
		List<String> stringValueArray = new ArrayList<String>();
		while (listIterator.hasNext()) {
			WebElement value = listIterator.next();
			String stringValueRaw = value.getText();
			String stringValue = StringUtils.normalizeSpace(stringValueRaw);
			if (stringValue.startsWith("place ")) {
				int beginInd = 6;
				int lastInd = stringValue.length();
				String stringNewValue = getString(stringValue, beginInd, lastInd);
				stringNewValue = StringUtils.normalizeSpace(stringNewValue);
				stringValueArray.add(stringNewValue);
			} else {
				stringValueArray.add(stringValue);
			}
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		// let's separate each text in ; for logging
		String joined = StringUtils.join(stringValueArray, ";");
		logDebugMessage(concatStrings("The List<String> to be returned by getAllSupplyAddress(WebElement) are -> [",
				joined, "]"));

		return stringValueArray;
	}

	/**
	 * Use this to get all addresses returned from the Postal/Forwarding Address
	 * for: - Move In - Move Out - Connection
	 * 
	 * @param supplyAddDiv is the div tag where role="listbox"
	 */
	public List<String> getAllPostalAddress(WebElement postalAddDiv) {

		List<WebElement> matOptions = postalAddDiv.findElements(By.xpath(
				"//mat-option[starts-with(@class,'mat-option ') and @role='option' and @name='postal_address_dropdown_values']"));

		ListIterator<WebElement> listIterator = matOptions.listIterator();
		List<String> stringValueArray = new ArrayList<String>();
		while (listIterator.hasNext()) {
			WebElement value = listIterator.next();
			String stringValueRaw = value.getText();
			String stringValue = StringUtils.normalizeSpace(stringValueRaw);
			if (stringValue.startsWith("place ")) {
				int beginInd = 6;
				int lastInd = stringValue.length();
				String stringNewValue = getString(stringValue, beginInd, lastInd);
				stringNewValue = StringUtils.normalizeSpace(stringNewValue);
				stringValueArray.add(stringNewValue);
			} else {
				stringValueArray.add(stringValue);
			}
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		// let's separate each text in ; for logging
		String joined = StringUtils.join(stringValueArray, ";");
		logDebugMessage(concatStrings("The List<String> to be returned by getAllPostalAddress(WebElement) are -> [",
				joined, "]"));

		return stringValueArray;
	}

	/**
	 * Use this to get all the addresses in the Company Address for Holiday Letting
	 * or Property Manager
	 * 
	 */
	public List<String> getAllCompanyAddress(WebElement companyAddDiv) {

		List<WebElement> matOptions = companyAddDiv.findElements(By.xpath(
				"//mat-option[starts-with(@class,'mat-option ') and @role='option' and @name='letting_address_dropdown_values']"));

		ListIterator<WebElement> listIterator = matOptions.listIterator();
		List<String> stringValueArray = new ArrayList<String>();
		while (listIterator.hasNext()) {
			WebElement value = listIterator.next();
			String stringValueRaw = value.getText();
			String stringValue = StringUtils.normalizeSpace(stringValueRaw);
			if (stringValue.startsWith("place ")) {
				int beginInd = 6;
				int lastInd = stringValue.length();
				String stringNewValue = getString(stringValue, beginInd, lastInd);
				stringNewValue = StringUtils.normalizeSpace(stringNewValue);
				stringValueArray.add(stringNewValue);
			} else {
				stringValueArray.add(stringValue);
			}
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		// let's separate each text in ; for logging
		String joined = StringUtils.join(stringValueArray, ";");
		logDebugMessage(concatStrings("The List<String> to be returned by getAllCompanyAddress(WebElement) are -> [",
				joined, "]"));

		return stringValueArray;
	}

	/**
	 * Use this to get all the Mat Options values that has the element
	 * '//mat-option[starts-with(@class,'mat-option ') and @role='option']'
	 * 
	 * @param divRoleListbox is the div tag where role="listbox"
	 */
	public List<String> getAllMatOptionsValues(WebElement divRoleListbox) {

		List<WebElement> matOptions = divRoleListbox
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));

		ListIterator<WebElement> listIterator = matOptions.listIterator();
		List<String> stringValueArray = new ArrayList<String>();
		while (listIterator.hasNext()) {
			WebElement value = listIterator.next();
			String stringValueRaw = value.getText();
			String stringValue = StringUtils.normalizeSpace(stringValueRaw);
			if (stringValue.startsWith("place ")) {
				int beginInd = 6;
				int lastInd = stringValue.length();
				String stringNewValue = getString(stringValue, beginInd, lastInd);
				stringNewValue = StringUtils.normalizeSpace(stringNewValue);
				stringValueArray.add(stringNewValue);
			} else {
				stringValueArray.add(stringValue);
			}
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		// let's separate each text in ; for logging
		String joined = StringUtils.join(stringValueArray, ";");
		logDebugMessage(concatStrings("The List<String> to be returned by getAllMatOptionsValues(WebElement) are -> [",
				joined, "]"));

		return stringValueArray;
	}

	/**
	 * Use this to get the display placeholder in the provided element.
	 * 
	 * Useful when getting values in input fields.
	 * 
	 * @param element
	 * @param normalizeSpaces
	 */
	public String getDisplayedPlaceholder(WebElement element, boolean normalizeSpaces) {

		String displayedPlaceholder;
		if (normalizeSpaces) {
			String displayedValueRaw = element.getAttribute("data-placeholder");
			displayedPlaceholder = StringUtils.normalizeSpace(displayedValueRaw);
		} else {
			displayedPlaceholder = element.getAttribute("data-placeholder");
		}

		logDebugMessage(concatStrings("The value to be returned by getDisplayedPlaceholder(WebElement, boolean) is '",
				displayedPlaceholder, "'"));
		return displayedPlaceholder;
	}

	/**
	 * Use this to get the display value in the provided element.
	 * 
	 * Useful when getting values in input fields.
	 * 
	 * @param element
	 * @param normalizeSpaces
	 */
	public String getDisplayedValue(WebElement element, boolean normalizeSpaces) {

		String displayedValue;
		if (normalizeSpaces) {
			String displayedValueRaw = element.getAttribute("value");
			displayedValue = StringUtils.normalizeSpace(displayedValueRaw);
		} else {
			displayedValue = element.getAttribute("value");
		}

		logDebugMessage(concatStrings("The value to be returned by getDisplayedValue(WebElement, boolean) is '",
				displayedValue, "'"));
		return displayedValue;
	}

	/**
	 * Use this to get the display text in the provided element using getText()
	 * method.
	 * 
	 * Useful when getting the text in labels.
	 * 
	 * @param element
	 * @param normalizeSpaces
	 */
	public String getDisplayedText(WebElement element, boolean normalizeSpaces) {

		String displayedText;
		if (normalizeSpaces) {
			String displayedTextRaw = element.getText();
			displayedText = StringUtils.normalizeSpace(displayedTextRaw);
		} else {
			displayedText = element.getText();
		}

		logDebugMessage(concatStrings("The value to be returned by getDisplayedText(WebElement, boolean) is '",
				displayedText, "'"));
		return displayedText;
	}
	
	/**
	 * Use this to get the display placeholder in the provided element.
	 * 
	 * Useful when getting values in input fields.
	 * 
	 * @param element
	 * @param normalizeSpaces
	 */
	public String getDisplayedTitle(WebElement element, boolean normalizeSpaces) {

		String title;
		if (normalizeSpaces) {
			String titleRaw = element.getAttribute("title");
			title = StringUtils.normalizeSpace(titleRaw);
		} else {
			title = element.getAttribute("title");
		}

		logDebugMessage(
				concatStrings("The value to be returned by getDisplayedTitle(WebElement, boolean) is '", title, "'"));
		return title;
	}

	/**
	 * Get the all the rows in the table body, hence excluding any header rows.
	 *
	 * @param table
	 * @return
	 */
	public List<WebElement> getTableRows(WebElement table) {

		// get the body of the table
		WebElement tableBody = table.findElement(By.tagName("tbody"));
		// get all the rows in the table and then return the one required
		return (tableBody.findElements(By.tagName("tr")));
	}

	/**
	 * Get the entire specified row in the specified table.
	 *
	 * @param table
	 * @param rowNum
	 * @return
	 */
	public WebElement getTableRow(WebElement table, int rowNum) {

		// get all the rows in the table and then return the one required
		return (getTableRows(table).get(rowNum));
	}

	/**
	 * Get all the cells in the specified row in the specified table.
	 *
	 * @param table
	 * @param rowNum
	 * @return
	 */
	public List<WebElement> getTableRowCells(WebElement table, int rowNum) {

		return (getTableRow(table, rowNum).findElements(By.tagName("td")));
	}

	/**
	 * Use this to get the email name when you are in the Email record view
	 */
	public String getEmailName() {

		WebElement contentTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
		String emailName = contentTable.findElement(By.xpath("//div[@class='moduleTitle']")).getText();
		emailName = StringUtils.normalizeSpace(emailName);
		logDebugMessage(concatStrings("The value to be returned by getEmailName() is '", emailName, "'"));
		return emailName;
	}

	/**
	 * Use this to get the table element of the email record
	 */
	public WebElement getEmailTable() {

		WebElement contentTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
		WebElement detailview = contentTable.findElement(By.xpath(".//div[@class='detail view']"));
		WebElement table = detailview.findElement(By.tagName("table"));

		logDebugMessage(concatStrings("The WebElement to return by getEmailTable() is {", table.toString(), "}"));
		return table;
	}

	/**
	 * Use this to get the data in each cell and per row from the email record view
	 * 
	 * @param row     starts with number 0 being the first row (e.g. In the email
	 *                record it's the row with Assigned to and Date Sent)
	 * @param cellLoc starts with number 0 being the first cell (e.g. In the email
	 *                record row 1, you will see Assigned to: [Data] and Date Sent:
	 *                [Data]. Starting from Assigned to, if you want to get the data
	 *                on the 'Assigned to', you will pass 1. Then if you want to get
	 *                the display label of Assigned to, then you will pass 0)
	 */
	public String getEmailDataPerCell(int row, int cellLoc) {

		List<WebElement> rowCellData = getTableRowCells(getEmailTable(), row);
		String cellRecord = rowCellData.get(cellLoc).getText();
		cellRecord = StringUtils.normalizeSpace(cellRecord);

		logDebugMessage(
				concatStrings("The value to be returned by getEmailDataPerCell(int, int) is '", cellRecord, "'"));
		return cellRecord;
	}
	
	/** 
	 * @param location starts at 1 being the first data from the list
	 * */
	public WebElement getElementFromList(WebElement elementDivList, int location) {

		// index starts with 0,1,2,etc...
		int loc = location - 1;
		List<WebElement> matOptions = elementDivList
				.findElements(By.xpath("//mat-option[starts-with(@class,'mat-option ') and @role='option']"));

		WebElement option = matOptions.get(loc);
		return option;
	}

	/**
	 * Use this if you want to get the text of a specific p tag in the body of the
	 * Email record view (bwc module)
	 * 
	 * @param pTagLoc is the number where your p tag is located when you inspect
	 *                element and copy xpath. Number starts at 1 being the first p
	 *                tag.
	 */
	public String getValueOfPtagInEmailBody(WebElement table, int pTagLoc) {

		List<WebElement> rowCells = getTableRowCells(table, 7);
		WebElement htmlBody = rowCells.get(1).findElement(By.xpath(".//div[@id='html_div']"));
		String pTagVal = htmlBody.findElement(By.xpath(".//p[" + pTagLoc + "]")).getText();

		logDebugMessage(concatStrings("The String to be returned by getValueOfPtagInEmailBody(WebElement, int) is '",
				pTagVal, "'"));
		return pTagVal;
	}

	/** 
	 * 
	 * */
	public String getWholeEmailBody(WebElement table, boolean normalizeSpaces) {

		String htmlBodyString = null;
		List<WebElement> rowCells = getTableRowCells(table, 7);
		String htmlBodyStringRaw = rowCells.get(1).findElement(By.xpath(".//div[@id='html_div']")).getText();
		if (normalizeSpaces) {
			htmlBodyString = normalizeSpaces(htmlBodyStringRaw);
		} else {
			htmlBodyString = htmlBodyStringRaw;
		}

		logDebugMessage(concatStrings("The value to be returned by getWholeEmailBody(WebElement, boolean) is '",
				htmlBodyString, "'"));
		return htmlBodyString;
	}

	/**
	 * <pre>
	 * Use this if you want to get each string in a 'Whole String'
	 * separated by line breaks.
	 * 
	 * For example, in the SMS record view, the message body
	 * 
	 * &#64;param description is the exact location of the message
	 * body enclosed in br tags
	 * @param normalizeSpace If true string 'Account Name:      Human Shoes   '
	 * would become 'Account Name: Human Shoes'
	 * </pre>
	 */
	public List<String> getEachTextInBreakTags(String description, boolean normalizeSpace) {

		String textsRaw[] = StringUtils.split(description, "\r\n");
		List<String> stringValueArray = new ArrayList<String>();

		for (String string : textsRaw) {
			if (normalizeSpace) {
				String stringNormalized = StringUtils.normalizeSpace(string);
				stringValueArray.add(stringNormalized);
			} else {
				stringValueArray.add(string);
			}
		}
		// let's remove the extra column generated
		// before returning the final stringValueArray
		stringValueArray.removeAll(Arrays.asList("", null));

		logDebugMessage(concatStrings("The List<String> to be returned by getEachTextInBreakTags(String, boolean) is ",
				stringValueArray.toString()));
		return stringValueArray;
	}

	/**
	 * Use this to get the the sms message body
	 * 
	 */
	public String getSmsMessageBody(boolean normalizeSpaces) {

		// get the content of record view
		WebElement record = driver.findElement(By.xpath("//div[@class='record']"));
		WebElement dataDesc = record.findElement(By.xpath(".//div[@data-type='sms-message']"));
		String descriptionRaw = dataDesc.findElement(By.xpath("./span[@data-fieldname='description']/span/div"))
				.getText();

		if (normalizeSpaces) {
			String description = StringUtils.normalizeSpace(descriptionRaw);
			logDebugMessage(
					concatStrings("The value to be returned by getSmsMessageBody(boolean) is '", description, "'"));
			return description;
		} else {
			logDebugMessage(
					concatStrings("The value to be returned by getSmsMessageBody(boolean) is '", descriptionRaw, "'"));
			return descriptionRaw;
		}
	}

	/**
	 * Get the content of the specific file. If the file is a ZIP file, then the
	 * content will be obtained from within the ZIP file. Also, if the file is a
	 * PDF, the content will be obtained as one long string.
	 *
	 * @param waitPeriod    period to wait (millisec) before looking for files
	 * @param afterDownload pass true if you want selenium to wait before checking
	 *                      the downloaded files
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 */
	public Map<String, String> getFileContentFromDownloadDir(int waitPeriod, boolean afterDownload)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		return (getFileContentFromDownloadDir(waitPeriod, -1, afterDownload));
	}

	/**
	 * Get the content of files within the download directory. This will only
	 * process PDF and CSV files. If any other file also needs to be processed they
	 * should be unpacked using {@link #unpackFiles()}.
	 *
	 * @param waitPeriod    period to wait (millisec) before looking for files
	 * @param lineNumber    zero for all file content otherwise a positive line
	 *                      number of wanting a specific line in file
	 * @param afterDownload pass true if you want to wait before checking the
	 *                      downloaded files. Specially useful if the file is zipped
	 *                      or big file size
	 * 
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 */
	public Map<String, String> getFileContentFromDownloadDir(int waitPeriod, int lineNumber, boolean afterDownload)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		// check if the call is straight after download as that is the only time
		// we need to wait and unpack any files
		if (afterDownload) {
			// wait a little while to ensure the files have been downloaded
			if (LOG.isInfoEnabled())
				LOG.info(concatStrings("Will be waiting for ", String.valueOf(waitPeriod),
						" milli seconds before checking the downloaded file"));
			pauseSeleniumExecution(waitPeriod);
			// unpack the files
			unpackFiles();
		}

		// get the file object that was specified and ensure we are dealing with
		// a file an not directory
		FileObject[] mainFiles = fsManager.resolveFile(DOWNLOADS_DIR).getChildren();
		final Map<String, String> fileContent = new HashMap<String, String>();

		// go through all the files and get their content
		for (int i = 0; i < mainFiles.length; i++) {
			// ensure we are only working with files
			if (mainFiles[i].getType() == FileType.FILE) {
				// get the content of the file
				final String content = getFileContent(mainFiles[i], lineNumber);
				if (au.com.blueoak.portal.utility.StringUtils.hasText(content))
					fileContent.put(mainFiles[i].getName().getBaseName(), content);
			}
			mainFiles[i].close();
		}

		logDebugMessage(concatStrings("The value of fileContent in line ", String.valueOf(lineNumber),
				" from getFileContentFromDownloadDir(int, int, boolean) method -> ", String.valueOf(fileContent)));
		// return the content of the file
		return (fileContent);
	}

	/**
	 * Use this to verify the number of attachments from an email record view
	 * 
	 * @param trLoc                    is the Attachments location for the
	 *                                 <tr>
	 *                                 tag
	 * @param expectedNumOfAttachments
	 */
	public List<String> getEmailAttachmentNames(int trLoc) {

		WebElement emailTable = getEmailTable();
		WebElement attachmentsTrLoc = emailTable.findElement(By.xpath("./tbody/tr[" + trLoc + "]/td[2]"));
		List<WebElement> attachments = attachmentsTrLoc.findElements(By.xpath("./slot/a"));
		List<String> attachmentsNames = new ArrayList<>();

		for (WebElement element : attachments) {
			String attachNameRaw = element.getText();
			String attachName = StringUtils.normalizeSpace(attachNameRaw);
			attachmentsNames.add(attachName);
		}

		logDebugMessage(concatStrings("The List<String> to be returned by getEmailAttachmentNames(int) is ",
				String.valueOf(attachmentsNames)));
		return attachmentsNames;
	}

	/**
	 * Use this to get the label element from the specified label name.
	 * 
	 * Useful when checking if the field is in error state
	 * 
	 * or when you want to get the css of label.
	 * 
	 * But only useful for input fields
	 * 
	 * @param inputElement
	 * @param caseSensitive pass true if you want the checking to be done in case
	 *                      sensitive manner
	 * @param labelName     is the label of the element you want to capture
	 */
	public WebElement getElementFrmLblNameInput(List<WebElement> inputElement, boolean caseSensitive,
			String labelName) {

		WebElement foundElement = null;
		String labelNameUpd = StringUtils.deleteWhitespace(labelName);

		if (caseSensitive) {
			logDebugMessage(concatStrings("Will be looking for the element that has the label name '", labelName,
					"' and checking would be case sensitive"));
			if (inputElement != null && inputElement.size() > 0) {
				for (WebElement webElement : inputElement) {
					String webElementStrRaw = webElement.getText();
					String webElementStr = StringUtils.deleteWhitespace(webElementStrRaw);
					if (webElementStr.equals(labelNameUpd)) {
						foundElement = webElement;
						logDebugMessage(concatStrings("WebElement is found. '", webElementStr,
								"' is equal to the supplied label name '", labelNameUpd, "'"));
						break;
					}
				}
			}
		} else {
			logDebugMessage(concatStrings("Will be looking for the element that has the label name '", labelName,
					"' and checking would be non-case sensitive"));
			if (inputElement != null && inputElement.size() > 0) {
				for (WebElement webElement : inputElement) {
					String webElementStrRaw = webElement.getText().toLowerCase();
					String webElementStr = StringUtils.deleteWhitespace(webElementStrRaw);
					if (webElementStr.equals(labelNameUpd.toLowerCase())) {
						foundElement = webElement;
						logDebugMessage(concatStrings("WebElement is found. '", webElementStr,
								"' is equal to the supplied label name '", labelNameUpd.toLowerCase(), "'"));
						break;
					}
				}
			}
		}

		return foundElement;
	}

	/**
	 * Use this to get the mat-step-header element from the specified section name.
	 * 
	 * Useful when checking for the whole section name
	 * 
	 * @param matStepHeaderTag
	 * @param containsSectionName is the section name you want to find
	 */
	public WebElement getElementFrmMatStepHdrTag(List<WebElement> matStepHeaderTag, String containsSectionName) {

		WebElement foundElement = null;
		String sectionNameUpd = StringUtils.deleteWhitespace(containsSectionName);
		logDebugMessage(concatStrings("Will be looking for the element that contains the section name '",
				containsSectionName, "' and checking would be non-case sensitive"));

		if (matStepHeaderTag != null && matStepHeaderTag.size() > 0) {
			for (WebElement webElement : matStepHeaderTag) {
				String webElementStrRaw = webElement.getText().toLowerCase();
				String webElementStr = StringUtils.deleteWhitespace(webElementStrRaw);
				if (webElementStr.contains(sectionNameUpd.toLowerCase())) {
					foundElement = webElement;
					logDebugMessage(concatStrings("WebElement is found. '", webElementStr,
							"' contains the supplied section name '", sectionNameUpd.toLowerCase(), "'"));
					break;
				}
			}
		}

		return foundElement;
	}
	
	/** 
	 * Use this to get the mat icon background colors for all the section headers
	 * */
	public List<String> getMatIconHeadersBckgrndColors() {

		List<WebElement> matIconsElements = driver.findElements(By.xpath(
				"//mat-step-header[starts-with(@id,'cdk-step-label-')]/div[starts-with(@class,'mat-step-icon')]"));
		List<String> matIconsBckgrndColors = new ArrayList<>();

		for (WebElement element : matIconsElements) {
			String matIconBckgrndColor = getCssBackgrndColorProp(element);
			matIconsBckgrndColors.add(matIconBckgrndColor);
		}

		logDebugMessage(concatStrings("The List<String> to be returned by getMatIconHeadersBckgrndColors() is ",
				matIconsBckgrndColors.toString(), " while the size of the list is <",
				Integer.toString(matIconsBckgrndColors.size()), ">"));
		return matIconsBckgrndColors;
	}

	/**
	 * Use this to get all the text inside the <mat-step-header> tags
	 */
	public List<String> getAllSectionNames(boolean normalizeSpaces) {

		List<WebElement> matStpHeaders = driver
				.findElements(By.xpath("//mat-step-header[starts-with(@id,'cdk-step-label-')]"));
		List<String> sectionNames = new ArrayList<>();

		for (WebElement element : matStpHeaders) {
			String elementStrRaw = element.getText();
			if (normalizeSpaces) {
				String elementStr = StringUtils.normalizeSpace(elementStrRaw);
				sectionNames.add(elementStr);
			} else {
				sectionNames.add(elementStrRaw);
			}
		}

		logDebugMessage(concatStrings("The List<String> to be returned by getAllSectionNames(boolean) is ",
				sectionNames.toString(), " while the size of the list is <", Integer.toString(sectionNames.size()),
				">"));
		return sectionNames;
	}

	/**
	 * Use this to get the div tag where the class is 'mat-step ng-tns-c9-1
	 * ng-star-inserted'
	 * 
	 * These div tags are the main elements for each section in the Move-In and
	 * Move-Out portal
	 * 
	 */
	public WebElement getMainElementForSection(String sectionName) {

		logDebugMessage(
				concatStrings("We will be looking for the parent element that exactly matches the section header '",
						sectionName, "'"));
		WebElement foundElement = null;
		WebElement initialElement = null;

		List<WebElement> elements = driver
				.findElements(By.xpath("//mat-step-header[starts-with(@id,'cdk-step-label-0')]/div[3]/p"));
		for (WebElement webElement : elements) {
			if (webElement != null) {
				String webElementStrRaw = webElement.getText();
				String webElementStr = StringUtils.normalizeSpace(webElementStrRaw);
				if (webElementStr.equals(sectionName)) {
					initialElement = webElement;
					break;
				}
			}
		}

		// let's get to the parent element
		WebElement parent = initialElement.findElement(By.xpath("./.."));
		String parentClass = parent.getAttribute("class");
		// check if it's the parent element we are looking for
		if (!parentClass.contains("mat-step ng-tns-c9")) {
			// still not the parent element we are looking for
			// let's go up 1 notch
			parent = parent.findElement(By.xpath("./.."));
			parentClass = parent.getAttribute("class");
			// check if it's the parent element we are looking for
			if (!parentClass.contains("mat-step ng-tns-c9")) {
				// still not the parent element we are looking for
				// let's go up 1 notch
				parent = parent.findElement(By.xpath("./.."));
				parentClass = parent.getAttribute("class");
			}
			// check if it's the parent element we are looking for
			if (!parentClass.contains("mat-step ng-tns-c9")) {
				// still not the parent element we are looking for
				// let's go up 1 notch
				parent = parent.findElement(By.xpath("./.."));
				parentClass = parent.getAttribute("class");
			}
			// check if it's the parent element we are looking for
			if (!parentClass.contains("mat-step ng-tns-c9")) {
				// still not the parent element we are looking for
				// let's go up 1 notch
				parent = parent.findElement(By.xpath("./.."));
			}
		}
		foundElement = parent;

		String foundElementClass = foundElement.getAttribute("class");
		logDebugMessage(concatStrings("Found the main parent element we are looking for, the class is '",
				foundElementClass, "'"));
		return foundElement;
	}

	/**
	 * Use this to get the element of a <mat-pseudo-checkbox> element
	 * 
	 * @param pseudoCheckboxElements
	 * @param caseSensitiveCheck 
	 * @param elementLabelName
	 */
	public WebElement getMatPseudoChkbxElement(List<WebElement> pseudoCheckboxElements, boolean caseSensitiveCheck,
			String elementLabelName) {

		logDebugMessage(concatStrings("Will be getting the <mat-pseudo-checkbox> element that has the label '",
				elementLabelName, "'"));
		WebElement initialElement = null;
		for (WebElement webElement : pseudoCheckboxElements) {
			String elementLbl = webElement.getText();
			elementLbl = StringUtils.normalizeSpace(elementLbl);
			elementLabelName = StringUtils.normalizeSpace(elementLabelName);
			if (caseSensitiveCheck) {
				if (elementLbl.equals(elementLabelName)) {
					initialElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString()));
					break;
				}
			} else {
				if (elementLbl.toLowerCase().equals(elementLabelName.toLowerCase())) {
					initialElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString()));
					break;
				}
			}
		}

		WebElement foundElement = initialElement.findElement(By.xpath(".//mat-pseudo-checkbox"));
		return foundElement;
	}
	
	/**
	 * Use this to get the element of a <mat-list-option> element
	 * 
	 * @param pseudoCheckboxElements
	 * @param caseSensitiveCheck 
	 * @param elementLabelName
	 */
	public WebElement getMatListOptionElement(List<WebElement> pseudoCheckboxElements, boolean caseSensitiveCheck,
			String elementLabelName) {

		logDebugMessage(concatStrings("Will be getting the <mat-list-option> element that has the label '",
				elementLabelName, "'"));
		WebElement initialElement = null;
		for (WebElement webElement : pseudoCheckboxElements) {
			String elementLbl = webElement.getText();
			elementLbl = StringUtils.normalizeSpace(elementLbl);
			elementLabelName = StringUtils.normalizeSpace(elementLabelName);
			if (caseSensitiveCheck) {
				if (elementLbl.equals(elementLabelName)) {
					initialElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString()));
					break;
				}
			} else {
				if (elementLbl.toLowerCase().equals(elementLabelName.toLowerCase())) {
					initialElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString()));
					break;
				}
			}
		}

		return initialElement;
	}

	/**
	 * Use this to get a time stamp
	 */
	public String getTimeStamp(SimpleDateFormat timeStampFormat) {

		String time = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		if (timeStampFormat == null) {
			time = String.valueOf(timestamp.getTime());
		} else {
			time = timeStampFormat.format(timestamp);
		}

		logDebugMessage(concatStrings("The value to be returned by getTimeStamp(SimpleDateFormat) is ", time));
		return time;
	}

	/**
	 * Get the filename of the file from the specified directory
	 * 
	 * @param directoryPath see UtilityBillingTestConstants class for the sample
	 *                      paths
	 */
	public List<String> getFileNamesFromDir(String directoryPath) {

		List<String> stringValueArray = new ArrayList<String>();
		File files = new File(directoryPath);

		for (File file : files.listFiles()) {
			String fileName = file.getName();
			stringValueArray.add(fileName);
		}

		logDebugMessage(concatStrings("The captured fileNames from the directory '", directoryPath, "' is/are -> ",
				stringValueArray.toString()));
		return stringValueArray;
	}

	/**
	 * Use this to get the total execution time in milli seconds
	 */
	public long getTotalExecutionInMilliSec(long nanoTimeStart, long nanoTimeEnd) {

		long nanoDuration = (nanoTimeEnd - nanoTimeStart);
		long convertedToMilliSec = (nanoDuration / 1000000);
		return convertedToMilliSec;
	}

	/**
	 * Use this to get the total execution time in seconds
	 */
	public long getTotalExecutionInSec(long nanoTimeStart, long nanoTimeEnd) {

		long nanoDuration = (nanoTimeEnd - nanoTimeStart);
		long convertedToMilliSec = (nanoDuration / 1000000);
		long convertedToSec = (convertedToMilliSec / 1000);
		return convertedToSec;
	}

	/**
	 * Use this to get the total execution time in minutes
	 */
	public long getTotalExecutionInMin(long nanoTimeStart, long nanoTimeEnd) {

		long nanoDuration = (nanoTimeEnd - nanoTimeStart);
		long convertedToMilliSec = (nanoDuration / 1000000);
		long convertedToSec = (convertedToMilliSec / 1000);
		long convertedToMin = (convertedToSec / 60);
		return convertedToMin;
	}
	
	/** 
	 * 
	 * */
	public List<List<String>> getAllLabelCss(WebElement element) {

		List<WebElement> pTags = element.findElements(By.xpath(".//p"));
		List<List<String>> group = new ArrayList<List<String>>();
		for (WebElement pTag : pTags) {
			String displayedVal = pTag.getText();
			displayedVal = StringUtils.deleteWhitespace(displayedVal);
			if (StringUtils.isNotBlank(displayedVal)) {
				List<String> labelExpCss = getLabelCss(pTag);
				group.add(labelExpCss);
			}
		}

		logDebugMessage(concatStrings("The value to be returned by getAllLabelCss(WebElement) is ", group.toString(),
				" and the size is <", Integer.toString(group.size()), ">"));
		return group;
	}

	/**
	 * Access this specific url
	 * 
	 */
	public void goToUrl(String url, boolean waitAfterEnterUrl) {

		logDebugMessage(concatStrings("Will go to this site using the URL:\n", url));
		driver.get(url);
		logDebugMessage("Now accessed the URL");
		if (waitAfterEnterUrl) {
			pauseSeleniumExecution(PORTAL_WAIT_TIME_AFTER_ACCESSING);
		}
	}

	/**
	 * Use this to hover the mouse to a specific element
	 * 
	 */
	public void hoverToElementAction(WebElement element) {

		Actions actions = new Actions(driver);
		actions.moveToElement(element).perform();
		logDebugMessage(concatStrings("Hovered mouse into the element ", element.toString()));
	}

	/**
	 * README When using this method, make sure that you minimize/turn-off the
	 * implicit wait, otherwise it would consume the entire duration of the implicit
	 * wait. After using this method, you can return the original implicit wait.
	 * 
	 * Using a page factory design, use this to confirm if an element is exists or
	 * not in the webpage.
	 * 
	 * Had to create this because in a page factory design, if you use
	 * isElementDisplayed(WebElement) and the element does not exist in the webpage,
	 * (e.g. cannot locate element in the Elements tab chrome development tool) the
	 * Page Factory returns a NoSuchElementException
	 */
	public boolean isElementExists(List<WebElement> elements) {

		logDebugMessage(concatStrings("Checking if the supplied WebElements exists on the webpage. Elements -> ",
				elements.toString()));
		if (elements.isEmpty()) {
			logDebugMessage(concatStrings("The supplied elements IS empty, the element does not exist"));
			return false;
		} else {
			logDebugMessage(concatStrings("The supplied elements IS NOT empty, the element exists"));
			return true;
		}
	}

	/**
	 * README Don't use this if the element does not exist in the webpage. (e.g.
	 * cannot locate element in the Elements tab chrome development tool)
	 * 
	 * Since we are using Page Factory, it has an invoke() method that tries to
	 * locate the element when you are referring to this element.
	 * NoSuchElementException element would not be able to catch that exception
	 * since it's thrown in the Page Factory class
	 * 
	 * Use this to verify if a field or element is hidden or not (but is still
	 * displayed in the webpage)
	 * 
	 * Use {@link #isElementDisplayed(WebElement,int)} instead
	 * 
	 */
	public boolean isElementDisplayed(WebElement element, int implicitWait) {

		boolean isDisplayed;
		logDebugMessage(concatStrings("Checking if the supplied WebElement is displayed on the webpage. Element -> ",
				element.toString()));
		try {
			setImplicitWait(implicitWait);
			isDisplayed = element.isDisplayed();
			String elementText = element.getText();
			if (StringUtils.isBlank(elementText)) {
				logDebugMessage("The supplied element is displayed.");
			} else {
				String logMsg = concatStrings("The supplied element is displayed. The displayed text is '", elementText,
						"'");
				logDebugMessage(logMsg);
			}
		} catch (NoSuchElementException nsee) {
			isDisplayed = false;
			logDebugMessage("The supplied element is not displayed.");
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		return isDisplayed;
	}

	/**
	 * Use this to verify if our field or MW is in error state
	 * 
	 * @param firstMethodMax  this is the max number of retries in checking the
	 *                        parent element using the 1st method. Pass 0(zero) if
	 *                        you want to turn off checking of parent element -
	 *                        meaning only the main element would be checked.
	 * @param secondMethodMax this is the max number of retries in checking the
	 *                        parent element using the 2nd method. Pass 0(zero) if
	 *                        you want to turn off checking of parent element -
	 *                        meaning only the main element would be checked.
	 */
	public boolean isElementInError(WebElement elementToCheck, int firstMethodMax, int secondMethodMax) {

		logDebugMessage(concatStrings("Checking if the main WebElement is in error state.\nElement -> ",
				elementToCheck.toString(), ".\nClass attribute is '", elementToCheck.getAttribute("class"), "'"));

		// let's check if the main element is now showing in error state
		// To check if an element is invalid, it must contain the text
		// 'ng-invalid' AND 'ng-touched' in the class attribute
		// OR if class attribute has text 'error'
		if (elementToCheck.getAttribute("class").toLowerCase().contains("ng-invalid")
				&& elementToCheck.getAttribute("class").toLowerCase().contains("ng-touched")) {
			logDebugMessage("The main WebElement is now in error state");
			return true;
		}
		if (elementToCheck.getAttribute("class").toLowerCase().contains("error")) {
			logDebugMessage("The main WebElement is now in error state");
			return true;
		}
		logDebugMessage("The main WebElement is not in error state");

		boolean firstMethodInError = false;
		if (firstMethodMax != 0) {
			logDebugMessage(concatStrings(
					"Checking the parent element class using 'ng-invalid' and 'ng-touched'. Max number of parent check is <",
					Integer.toString(firstMethodMax), ">"));
			// let's check the parent nodes since the main element is not in error state
			// check if the 1st parent is in error state
			WebElement parent1 = elementToCheck.findElement(By.xpath(".."));
			String classAttrib1 = parent1.getAttribute("class").toLowerCase();
			for (int i = 1; i < firstMethodMax; i++) {
				logDebugMessage(concatStrings("The value of classAttrib1 is: ", classAttrib1));
				if (!classAttrib1.contains("ng-invalid") && !classAttrib1.contains("ng-touched")) {
					// check if the parent again if in error state
					parent1 = parent1.findElement(By.xpath(".."));
					classAttrib1 = parent1.getAttribute("class").toLowerCase();
				}
			}
			if (classAttrib1.contains("ng-invalid") && classAttrib1.contains("ng-touched")) {
				firstMethodInError = (classAttrib1.contains("ng-invalid") && classAttrib1.contains("ng-touched"));
				logDebugMessage(concatStrings("The value of classAttrib1 is: ", classAttrib1));
			}
		} else {
			logDebugMessage("No need to execute 1st method of checking for error state since value is zero");
		}

		boolean secondMethodInError = false;
		if (secondMethodMax != 0) {
			logDebugMessage(
					concatStrings("Checking the parent element class using 'error'. Max number of parent check is <",
							Integer.toString(secondMethodMax), ">"));
			// let's check the parent nodes since the main element is not in error state
			// check if the 1st parent is in error state
			WebElement parent2 = elementToCheck.findElement(By.xpath(".."));
			String classAttrib2 = parent2.getAttribute("class").toLowerCase();
			for (int i = 1; i < secondMethodMax; i++) {
				logDebugMessage(concatStrings("The value of classAttrib2 is: ", classAttrib2));
				if (!classAttrib2.contains("error")) {
					// check if the parent again if in error state
					parent2 = parent2.findElement(By.xpath(".."));
					classAttrib2 = parent2.getAttribute("class").toLowerCase();
				}
			}
			if (classAttrib2.contains("error")) {
				secondMethodInError = (classAttrib2.contains("error"));
				logDebugMessage(concatStrings("The value of classAttrib2 is: ", classAttrib2));
			}
		} else {
			logDebugMessage("No need to execute 2nd method of checking for error state since value is zero");
		}

		logDebugMessage(concatStrings("The value for firstMethodInError <", String.valueOf(firstMethodInError),
				"> while value for secondMethodInError <", String.valueOf(secondMethodInError), ">"));
		if (firstMethodInError) {
			return firstMethodInError;
		} else {
			return secondMethodInError;
		}
	}

	/**
	 * Use this to verify if an element is ticked
	 */
	public boolean isElementTicked(WebElement element, int implicitWait) {

		logDebugMessage(
				concatStrings("Checking if the supplied WebElement is ticked. Element -> ", element.toString()));
		boolean isTicked;
		try {
			setImplicitWait(implicitWait);
			isTicked = element.isSelected();
		} catch (Exception e) {
			String logMsg = concatStrings(
					"An exception has been encountered in method isElementTicked(WebElement). Please see message for more details -> ",
					e.getMessage());
			logDebugMessage(logMsg);
			throw e;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		logDebugMessage(concatStrings("The element is ", (isTicked ? "ticked" : "not ticked")));
		return isTicked;
	}

	/**
	 * Use this to verify if an element is in read only or not
	 */
	public boolean isElementEnabled(WebElement element, int implicitWait) {

		logDebugMessage(
				concatStrings("Checking if the supplied WebElement is enabled. Element -> ", element.toString()));
		boolean isEnabled;
		try {
			setImplicitWait(implicitWait);
			isEnabled = element.isEnabled();
		} catch (Exception e) {
			String logMsg = concatStrings(
					"An exception has been encountered in method isElementEnabled(WebElement). Please see message for more details -> ",
					e.getMessage());
			logDebugMessage(logMsg);
			throw e;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		logDebugMessage(concatStrings("The element is ", (isEnabled ? "enabled" : "disabled or in Read-Only mode")));
		return isEnabled;
	}

	/**
	 * Use this for checkbox elements <mat-pseudo-checkbox> if it's ticked or not
	 * 
	 */
	public boolean isMatPseudoChckbxTicked(WebElement matPseudoChkboxElement, int implicitWait) {

		logDebugMessage(concatStrings("Checking if this <mat-pseudo-checkbox> element is ticked. Element -> ",
				matPseudoChkboxElement.toString()));
		boolean isTicked;
		try {
			setImplicitWait(implicitWait);
			String className = matPseudoChkboxElement.getAttribute("class").toLowerCase();
			if (className.contains("checkbox-checked")) {
				isTicked = true;
			} else {
				isTicked = false;
			}
		} catch (Exception e) {
			String logMsg = concatStrings(
					"An exception has been encountered in method isMatCheckboxTicked(WebElement). Please see message for more details -> ",
					e.getMessage());
			logDebugMessage(logMsg);
			throw e;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		logDebugMessage(concatStrings("The <mat-pseudo-checkbox> element is ", (isTicked ? "ticked" : "not ticked")));
		return isTicked;
	}
	
	/**
	 * This would create an invalidation request in the specified distributionID in
	 * the cloudfront.
	 * 
	 * You can have more information on this link:
	 * https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/Invalidation.html
	 * 
	 * @param runInvalidation    pass true if you want to invalidate files
	 * @param waitUntilCompleted pass true if you want to wait until the
	 *                           invalidation request is completed before moving on
	 * @param filesLocation      don't pass anything if you want to invalidate the
	 *                           whole thing, but if you want to invalidate certain
	 *                           folders or files you can do pass this
	 *                           '/ASTERISK/assets/i18n/*' or
	 *                           '/ASTERISK/assets/i18n/fil.json'. Note that you
	 *                           should use the actual '*' symbol, not the word
	 *                           ASTERISK
	 * 
	 */
	public void invalidateCloudfront(boolean runInvalidation, AccessS3BucketWithVfs s3Access,
			boolean waitUntilCompleted, String... filesLocation) {

		if (runInvalidation) {
			if (waitUntilCompleted) {
				long totalStartime = logNanoTimeStamp();
				s3Access.createInvalidationRequest(getDistributionId(), waitUntilCompleted, filesLocation);
				long totalEndtime = logNanoTimeStamp();
				logDebugMessage(concatStrings("Total time to complete the invalidation request is <",
						String.valueOf(getTotalExecutionInSec(totalStartime, totalEndtime)), "> seconds"));
			} else {
				s3Access.createInvalidationRequest(getDistributionId(), waitUntilCompleted, filesLocation);
			}
		}
	}

	/**
	 * Check if a browser alert is displayed
	 */
	public boolean isBrowserAlertDisplayed() {

		logDebugMessage("Checking if a browser alert is displayed");
		try {
			Alert alt = new WebDriverWait(driver, 1).until(ExpectedConditions.alertIsPresent());
			if (alt != null) {
				String alert = alt.getText();
				logDebugMessage("A browser alert IS displayed. The browser alert is '" + alert + "'");
				return true;
			} else {
				logDebugMessage("A browser alert IS NOT displayed");
				return false;
			}
		} catch (NoAlertPresentException nape) {
			logDebugMessage("A browser alert IS NOT displayed. NoAlertPresentException has been catched.");
			return false;
		} catch (TimeoutException toe) {
			logDebugMessage("A browser alert IS NOT displayed. TimeoutException has been catched.");
			return false;
		} catch (UnreachableBrowserException ube) {
			logDebugMessage("A browser alert IS NOT displayed. UnreachableBrowserException has been catched.");
			return false;
		} catch (NoSuchSessionException nsse) {
			logDebugMessage("A browser alert IS NOT displayed. NoSuchSessionException has been catched.");
			return false;
		}
	}

	/**
	 * Check if the image is corrupted or not.
	 * 
	 * - worked on .jpeg - worked on .jpg - worked on .gif - worked on .tiff -
	 * worked on .tif - worked on .png
	 */
	public boolean isImageValid(String fileLoc) {

		boolean isValid;

		if (fileLoc.toLowerCase().contains(".gif")) {
			try {
				// close any previous stream
				FileInputStream data = new FileInputStream(fileLoc);
				data.close();

				// open it again
				data = new FileInputStream(fileLoc);
				GifDecoder.read(data);
				data.close();
				isValid = true;
			} catch (Exception e) {
				LOG.fatal(
						"Exception encountered when reading .gif image in method isValidImage(File). Please see stack trace -> ",
						e);
				isValid = false;
			}
		} else {
			try {
				// close any previous stream
				FileInputStream data = new FileInputStream(fileLoc);
				data.close();

				// open it again
				data = new FileInputStream(fileLoc);
				ImageIO.read(data).flush();
				data.close();
				isValid = true;
			} catch (Exception e) {
				LOG.fatal("Exception encountered in method isValidImage(String). Please see stack trace -> ", e);
				isValid = false;
			}
		}

		logDebugMessage(concatStrings("The provided image in this location '", fileLoc, "' is ",
				(isValid ? "valid" : "invalid")));
		return isValid;
	}

	/**
	 * verify if the element is visible in/within the viewport.
	 * 
	 */
	public boolean isElementWithinViewport(WebElement element) {

		logDebugMessage(concatStrings("Going to check if this element is within viewport -> ", element.toString()));
		boolean isWithinViewport = (Boolean) ((JavascriptExecutor) driver).executeScript(
				"var elem = arguments[0],                 " + "  box = elem.getBoundingClientRect(),    "
						+ "  cx = box.left + box.width / 2,         " + "  cy = box.top + box.height / 2,         "
						+ "  e = document.elementFromPoint(cx, cy); " + "for (; e; e = e.parentElement) {         "
						+ "  if (e === elem)                        " + "    return true;                         "
						+ "}                                        " + "return false;                            ",
				element);

		logDebugMessage(concatStrings("The supplied element is ",
				(isWithinViewport ? "within viewport" : "not within viewport")));
		return isWithinViewport;
	}
	
	/**
	 * 
	 * Only use this method if an only if the native WebElement.click() method does
	 * not work OR if the clickButton(WebElement) does not work either
	 * 
	 * Use this to click an element using javascript
	 * 
	 */
	public void javaScriptClickElementAction(WebElement element) {

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].click();", element);
		String msg = concatStrings("Used JavascriptExecutor to click the element {", element.toString(), "}");
		logDebugMessage(msg);
	}

	/**
	 * Use this when you are expecting for the loading screen to appear and
	 * disappear in: - Move In - Move Out - Customer Portal
	 */
	public void loadPortal() {

		if (isPortalLoadDisplayed(PORTAL_LOAD_TIMEOUT_START, true)) {
			// loading screen appeared, now let's wait
			// until it disappears
			isPortalLoadDisplayed(PORTAL_LOAD_TIMEOUT_END, false);
		}
	}

	/**
	 * Use this after accessing the Standalone Move In portal
	 * 
	 * @param verifyElementDisp pass true if you want to verify if the
	 *                          SupplyDetailsMoveIn header is displayed
	 * 
	 */
	public void loadStandaloneMoveInPortal(boolean verifyElementDisp) {

		logDebugMessage("Would be loading the Standalone Move In Portal");
		loadPortal();
		if (verifyElementDisp) {
			// make sure that the elements are now displayed
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		logDebugMessage("Finished loading the Standalone Move In Portal");
	}

	/**
	 * Use this after accessing the Embedded Move In portal
	 * 
	 * @param switchToIframe    pass true if you want to switch immediately into the
	 *                          iFrame
	 * @param verifyElementDisp pass true if you want to verify if the
	 *                          SupplyDetailsMoveIn header is displayed
	 * 
	 */
	public void loadEmbeddedMoveInPortal(boolean switchToIframe, boolean verifyElementDisp) {

		logDebugMessage("Would be loading the Embedded Move In Portal");
		waitForMoveInIFrameToDisplay(PORTAL_IFRAME_WAIT_TIMEOUT);
		if (switchToIframe == false && verifyElementDisp == true) {
			fail("Verify the args passed. Param switchToIframe needs to be [true] if you want to verify the elements");
		} else {
			if (switchToIframe) {
				embeddedMoveInSwitchFrame(1);
				loadPortal();
			} else {
				loadPortal();
			}
			if (verifyElementDisp) {
				// make sure that the elements are now displayed
				SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}
		}
		logDebugMessage("Finished loading the Embedded Move In Portal");
	}

	/**
	 * Use this after accessing the Standalone Customer portal
	 * 
	 */
	public void loadStandaloneCustomerPortal() {

		logDebugMessage("Would be loading the Standalone Customer Portal");
		loadPortal();
		// make sure that the elements are now displayed
		LoginCustomer logincustomer = new LoginCustomer(driver);
		waitUntilElementIsDisplayed(logincustomer.loginBtn, PORTAL_ELEMENT_WAIT_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		logDebugMessage("Finished loading the Standalone Customer Portal");
	}

	/**
	 * Use this after accessing the Embedded Customer portal
	 * 
	 */
	public void loadEmbeddedCustomerPortal() {

		logDebugMessage("Would be loading the Embedded Customer Portal");
		waitForCustomerPortalIFrameToDisplay(PORTAL_IFRAME_WAIT_TIMEOUT);
		embeddedCustomerPortalSwitchFrame(1);
		loadPortal();
		// make sure that the elements are now displayed
		LoginCustomer logincustomer = new LoginCustomer(driver);
		waitUntilElementIsDisplayed(logincustomer.loginBtn, PORTAL_ELEMENT_WAIT_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		logDebugMessage("Finished loading the Embedded Customer Portal");
	}

	/**
	 * Use this to log the Start of the Test class
	 */
	public void logTestClassStart(String className) {

		logDebugMessage("***** Start of Test Class {" + className + "} *****");
	}

	/**
	 * Use this to log the End of the Test class
	 */
	public void logTestClassEnd(String className) {

		logDebugMessage("~~~~~ End of Test Class {" + className + "} ~~~~~");
	}

	/**
	 * Use this to log the Start of the current Test case running
	 */
	public void logStartOfCurrentTestCase(String currentTestCase) {

		logDebugMessage("+++++ Start of Test Case {".concat(currentTestCase).concat("} +++++"));
	}

	/**
	 * Use this to log the End of the current Test case running
	 */
	public void logEndOfCurrentTestCase(String currentTestCase) {

		logDebugMessage("----- End of Test Case {".concat(currentTestCase).concat("} -----"));
	}

	/**
	 * <pre>
	 * README
	 * Logging levels. Arranged from Highest to Lowest
	 * 
	 * fatal - Severe errors that cause premature termination.
	 * error - Other runtime errors or unexpected conditions.
	 * warn - Use of deprecated APIs, poor use of API, 'almost' errors, other runtime situations that are undesirable or unexpected, but not necessarily "wrong".
	 * info - Interesting runtime events (startup/shutdown).
	 * debug - detailed information on the flow through the system.
	 * trace - more detailed information.
	 * 
	 * reference: https://commons.apache.org/proper/commons-logging/guide.html
	 * </pre>
	 */

	/**
	 * Use this to log a Fatal message
	 */
	public void logFatalMessage(String fatalMessage) {

		if (LOG.isFatalEnabled())
			LOG.fatal(fatalMessage);
	}

	/**
	 * Use this to log an Error message
	 */
	public void logErrorMessage(String errorMessage) {

		if (LOG.isErrorEnabled())
			LOG.info(errorMessage);
	}

	/**
	 * Use this to log a Warning message
	 */
	public void logWarnMessage(String warningMessage) {

		if (LOG.isWarnEnabled())
			LOG.warn(warningMessage);
	}

	/**
	 * Use this to log an Info message
	 */
	public void logInfoMessage(String infoMessage) {

		if (LOG.isInfoEnabled())
			LOG.info(infoMessage);
	}

	/**
	 * Use this to log a Debug message
	 */
	public void logDebugMessage(String debugMessage) {

		if (LOG.isDebugEnabled())
			LOG.debug(debugMessage);
	}

	/**
	 * Use this to log a Trace message
	 */
	public void logTraceMessage(String traceMessage) {

		if (LOG.isTraceEnabled())
			LOG.trace(traceMessage);
	}

	/**
	 * Used for capturing screenshots when debugging a code in headless environment.
	 * 
	 * @param msg this will serve as the filename for the screenshot
	 */

	public void logScreenshot(String fileName) {

		// if directory to save the screenshot
		// does not exist - make one
		File dir = new File(TEST_LOG_SCREENSHOTS);
		if (!dir.exists())
			dir.mkdirs();

		try {
			// take a screenshot of the current screen
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			// save the screenshot
			File screenshot = new File(concatStrings(TEST_LOG_SCREENSHOTS, fileName, ".jpg"));
			FileUtils.copyFile(scrFile, screenshot);
			logDebugMessage(concatStrings("Successfully captured a screenshot. File name is '", fileName, "'"));
		} catch (IOException e) {
			logDebugMessage(
					"An exception occured while trying to save the screenshot for debug logging. See exception message for details -> "
							+ e.getStackTrace());
		}
	}

	/**
	 * Another way to log the time
	 */
	public long logNanoTimeStamp() {

		long nanoTime = System.nanoTime();
		return nanoTime;
	}
	
	/**
	 * Use this when the progress bar is displayed and waiting for it to disappear.
	 * 
	 * Can be used when making a payment.
	 * 
	 * Will return a TimeoutException if progress bar did not appear or disappear
	 * with the PROGRESS_BAR_TIMEOUT config.
	 */
	public void makePaymentBtnProgBarLoad() {

		FluentWait<WebDriver> wait_1 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_APPEAR_TIMEOUT))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

		logDebugMessage(concatStrings("Will be waiting for the Make Payment progress bar to appear within <",
				Integer.toString(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_APPEAR_TIMEOUT), "> seconds"));

		boolean isProgBarDisp = false;
		try {
			isProgBarDisp = wait_1.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Checking if the Make Payment progress bar appeared");
					boolean stillChecking = true;
					boolean finishedChecking = false;
					int counter = 0;
					int maxRetry = 10;
					while (stillChecking && !finishedChecking && counter < maxRetry) {
						try {
							ProgressBar progBar = new ProgressBar(driver, 0);
							if (isElementExists(progBar.paymentList)) {
								logDebugMessage("The Make Payment progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Make Payment progress bar is not yet displayed");
								stillChecking = true;
								finishedChecking = false;
							}
							pauseSeleniumExecution(200);
							counter++;
						} catch (StaleElementReferenceException sere) {
							logDebugMessage(
									"StaleElementReferenceException encountered while waiting for the Make Payment progress bar to display. Will reinitialize the page object then look for it again and confirm if it's displayed or not");
							ProgressBar progBar = new ProgressBar(driver, 0);
							if (isElementExists(progBar.paymentList)) {
								logDebugMessage("The Make Payment progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Make Payment progress bar is not yet displayed");
								stillChecking = true;
								finishedChecking = false;
							}
							pauseSeleniumExecution(200);
							counter++;
						}
					}
					return finishedChecking;
				}
			}).booleanValue();
		} catch (TimeoutException toe) {
			logDebugMessage(concatStrings("Finished waiting for <",
					Integer.toString(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_APPEAR_TIMEOUT),
					"> seconds for the Make Payment progress bar to appear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		assertTrue(isProgBarDisp, "The Make Payment progress bar did not display!");

		if (isProgBarDisp) {
			logDebugMessage(concatStrings("Make Payment progress bar is displayed, will now be waiting within <",
					Integer.toString(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
					"> seconds for it to disappear"));

			FluentWait<WebDriver> wait_2 = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_DISAPPEAR_TIMEOUT))
					.pollingEvery(Duration.ofMillis(500))
					.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

			boolean progBarDisappeared = false;
			try {
				progBarDisappeared = wait_2.until(new Function<WebDriver, Boolean>() {
					public Boolean apply(WebDriver driver) {
						logDebugMessage("Checking if the Make Payment progress bar disappeared");
						boolean stillDisplayed = true;
						boolean finishedChecking = false;
						int counter = 0;
						int maxRetry = 10;
						while (stillDisplayed && !finishedChecking && counter < maxRetry) {
							try {
								ProgressBar progBar = new ProgressBar(driver, 0);
								if (isElementExists(progBar.paymentList)) {
									logDebugMessage("Make Payment progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Make Payment progress bar is no longer displayed");
									return true;
								}
								//pauseSeleniumExecution(1000);
								counter++;
							} catch (StaleElementReferenceException sere) {
								logDebugMessage(
										"StaleElementReferenceException encountered while waiting for the Make Payment progress bar to disappear, will reinitialize the ProgressBar page object");
								ProgressBar progBar = new ProgressBar(driver, 0);
								if (isElementExists(progBar.paymentList)) {
									logDebugMessage("Make Payment progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Make Payment progress bar is no longer displayed");
									return true;
								}
								//pauseSeleniumExecution(1000);
								counter++;
							}
						}
						return finishedChecking;
					}
				}).booleanValue();
			} catch (TimeoutException toe) {
				logDebugMessage(concatStrings("Finished waiting for <",
						Integer.toString(MAKE_PAYMENT_PORTAL_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
						"> seconds for the Make Payment progress bar to disappear"));
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}

			assertTrue(progBarDisappeared, "The Make Payment progress bar is still displayed!");
		}
	}
	
	/**
	 * Use this when the progress bar is displayed and waiting for it to disappear.
	 * 
	 * Can be used when hitting Next(if only account number is displayed)
	 * 
	 * Will return a TimeoutException if progress bar did not appear or disappear
	 * with the PROGRESS_BAR_TIMEOUT config.
	 */
	public void nextBtnProgBarLoad() {

		FluentWait<WebDriver> wait_1 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_APPEAR_TIMEOUT))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

		logDebugMessage(concatStrings("Will be waiting for the Next button progress bar to appear within <",
				Integer.toString(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_APPEAR_TIMEOUT), "> seconds"));

		boolean isProgBarDisp = false;
		try {
			isProgBarDisp = wait_1.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Checking if the Next button progress bar appeared");
					boolean stillChecking = true;
					boolean finishedChecking = false;
					int counter = 0;
					int maxRetry = 10;
					while (stillChecking && !finishedChecking && counter < maxRetry) {
						try {
							ProgressBar progBar = new ProgressBar(driver, 0);
							if (isElementExists(progBar.nextList)) {
								logDebugMessage("The Next button progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Next button progress bar is not yet displayed");
								stillChecking = true;
								finishedChecking = false;
							}
							pauseSeleniumExecution(200);
							counter++;
						} catch (StaleElementReferenceException sere) {
							logDebugMessage(
									"StaleElementReferenceException encountered while waiting for the  Next button progress bar to display. Will reinitialize the page object then look for it again and confirm if it's displayed or not");
							ProgressBar progBar = new ProgressBar(driver, 0);
							if (isElementExists(progBar.nextList)) {
								logDebugMessage("The Next button progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Next button progress bar is not yet displayed");
								stillChecking = true;
								finishedChecking = false;
							}
							pauseSeleniumExecution(200);
							counter++;
						}
					}
					return finishedChecking;
				}
			}).booleanValue();
		} catch (TimeoutException toe) {
			logDebugMessage(concatStrings("Finished waiting for <",
					Integer.toString(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_APPEAR_TIMEOUT),
					"> seconds for the Next progress bar to appear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		InputFields inputfields = new InputFields(driver, 0);
		ProgressBar progBar = new ProgressBar(driver, 0);
		if (isElementExists(inputfields.paymentAmountList)) {
			logDebugMessage("The Make Payment is now showing the full page. Next Progress bar may have been missed");
			boolean isDisp = isElementExists(progBar.nextList);
			logDebugMessage("The Next button is " + (isDisp ? "still displayed" : "no longer displayed"));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isProgBarDisp, "The Next button progress bar did not display");
		}

		if (isProgBarDisp) {
			logDebugMessage(concatStrings("Next button progress bar is displayed, will now be waiting within <",
					Integer.toString(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
					"> seconds for it to disappear"));

			FluentWait<WebDriver> wait_2 = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_DISAPPEAR_TIMEOUT))
					.pollingEvery(Duration.ofMillis(500))
					.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

			boolean progBarDisappeared = false;
			try {
				progBarDisappeared = wait_2.until(new Function<WebDriver, Boolean>() {
					public Boolean apply(WebDriver driver) {
						logDebugMessage("Checking if the  Next button progress bar disappeared");
						boolean stillDisplayed = true;
						boolean finishedChecking = false;
						int counter = 0;
						int maxRetry = 10;
						while (stillDisplayed && !finishedChecking && counter < maxRetry) {
							try {
								ProgressBar progBar = new ProgressBar(driver, 0);
								if (isElementExists(progBar.nextList)) {
									logDebugMessage("Next button progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Next button progress bar is no longer displayed");
									return true;
								}
								pauseSeleniumExecution(1000);
								counter++;
							} catch (StaleElementReferenceException sere) {
								logDebugMessage(
										"StaleElementReferenceException encountered while waiting for the Next button progress bar to disappear, will reinitialize the ProgressBar page object");
								ProgressBar progBar = new ProgressBar(driver, 0);
								if (isElementExists(progBar.nextList)) {
									logDebugMessage("Next button progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Next button progress bar is no longer displayed");
									return true;
								}
								pauseSeleniumExecution(1000);
								counter++;
							}
						}
						return finishedChecking;
					}
				}).booleanValue();
			} catch (TimeoutException toe) {
				logDebugMessage(concatStrings("Finished waiting for <",
						Integer.toString(MAKE_PAYMENT_PORTAL_NEXT_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
						"> seconds for the Next progress bar to disappear"));
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}

			inputfields = new InputFields(driver, 0);
			progBar = new ProgressBar(driver, 0);
			if (isElementExists(inputfields.paymentAmountList)) {
				logDebugMessage(
						"The Make Payment is now showing the full page. Next Progress bar should no longer be displayed");
				boolean isDisp = isElementExists(progBar.nextList);
				logDebugMessage("The Next button is " + (isDisp ? "still displayed" : "no longer displayed"));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			} else {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				assertTrue(progBarDisappeared, "The Next button progress bar is still displayed!");
			}
		}
	}
	
	/**
	 * Use this to normalize the spaces on the given string
	 */
	public String normalizeSpaces(String rawText) {

		String text = StringUtils.normalizeSpace(rawText);
		logDebugMessage(concatStrings("The value to be returned by normalizeSpaces(String) is '", text, "'"));
		return text;
	}
	
	/**
	 * Use this to open a new tab and switch to it
	 */
	public void openNewTabAndSwitchToIt() {

		logDebugMessage("Will be opening a new tab using the same browser");
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.open()");
		logDebugMessage("Successfully opened a new tab using the same browser");
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tabs.get(1));
		logDebugMessage("Successfully switched to the new tab");
		pauseSeleniumExecution(3000);
	}
	
	/**
	 * Use this to scroll the page up
	 * 
	 * @param yPixels is the number of pixel vertical. You can use 250 as the args.
	 */
	public void pageScrollUp(int yPixels) {

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String script = "window.scrollBy(0,-" + yPixels + ")";
		jse.executeScript(script);
		logDebugMessage(
				concatStrings("Successfully scrolled the page up using <", String.valueOf(yPixels), "> pixels"));
	}

	/**
	 * Use this to scroll the page down
	 * 
	 * @param yPixels is the number of pixel vertical. You can use 250 as the args.
	 */
	public void pageScrollDown(int yPixels) {

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String script = "window.scrollBy(0," + yPixels + ")";
		jse.executeScript(script);
		logDebugMessage(
				concatStrings("Successfully scrolled the page down using <", String.valueOf(yPixels), "> pixels"));
	}
	
	/**
	 * This is the shortcut Control + V for windows.
	 * 
	 * Used in conjunction with selectAllTextFromField() and copyTextFromField()
	 * methods.
	 * 
	 * Make sure the focus is already on the field where you want the text to be
	 * copied.
	 * 
	 */
	public void pasteTextToField() {

		Actions actions = new Actions(driver);
		actions.keyDown(Keys.CONTROL);
		actions.sendKeys("v");
		actions.keyUp(Keys.CONTROL);
		actions.perform();

		logDebugMessage("Successfully executed the pastTextToField() method");
	}

	/**
	 * Use this to pause selenium execution in the specified time in milli seconds.
	 */
	public void pauseSeleniumExecution(int milliSeconds) {

		logDebugMessage("Will verify if the passed args are correct before pausing selenium execution");
		String millisecString = Integer.toString(milliSeconds);

		try {
			switch (millisecString) {
			case "0":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));

			case "00":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));

			case "000":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));

			case "0000":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));

			case "00000":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));

			case "000000":
				throw (new IllegalArgumentException("The passed " + milliSeconds + " milli seconds is not allowed"));
			}
		} catch (IllegalArgumentException iae) {
			logFatalMessage(
					"IllegalArgumentException was encountered. See exception details -> " + iae.getStackTrace());
		}

		logDebugMessage(
				"Args passed is correct, selenium execution will now pause for " + milliSeconds + " milli seconds.");

		try {
			synchronized (Thread.currentThread()) {
				Thread.currentThread().wait(milliSeconds);
			}
		} catch (InterruptedException ie) {
			logFatalMessage("An InterruptedException has occured. See exception message for more details -> "
					+ ie.getStackTrace());
		}

		logDebugMessage("Finished waiting for " + milliSeconds + " milli seconds. Selenium execution will now resume.");
	}

	/**
	 * Use this to refresh the browser
	 * 
	 * @param refreshTimes            is the number of refresh you want
	 * @param pauseDurationInMilliSec is the pause duration after doing the browser
	 *                                refresh
	 */
	public void refreshBrowser(int refreshTimes, int pauseDurationInMilliSec) {

		logDebugMessage(concatStrings("Will be refreshing the page for <", Integer.toString(refreshTimes),
				"> time(s) and will pause selenium execution between each refresh for ",
				Integer.toString(pauseDurationInMilliSec), " milli seconds"));
		int counter = 1;
		while (counter <= refreshTimes) {
			driver.navigate().refresh();
			logDebugMessage("Done refreshing the page. Current refresh attempts -> " + counter);
			pauseSeleniumExecution(pauseDurationInMilliSec);
			;
			counter++;
		}
	}
	
	/**
	 * Use this to remove files that were uploaded in the upload section
	 * 
	 * e.g. Showing the x button icon
	 * 
	 */
	public void removeUploadedFiles(List<WebElement> uploadedFiles, String fileNameToRemove) {

		WebElement foundElement = null;
		logDebugMessage(concatStrings("Will be looking for the element in the upload section that has the file name '",
				fileNameToRemove, "'"));

		if (uploadedFiles != null && uploadedFiles.size() > 0) {
			for (WebElement webElement : uploadedFiles) {
				String webElementStr = webElement.getAttribute("data-file-name");
				if (webElementStr.equals(fileNameToRemove)) {
					foundElement = webElement;
					logDebugMessage(concatStrings("Found the element ", webElement.toString(),
							" that has the file name '", fileNameToRemove, "'"));
					break;
				}
			}
		}

		if (foundElement == null) {
			throw (new IllegalArgumentException(
					concatStrings("Could not find the file name '", fileNameToRemove, "' to remove")));
		} else {
			WebElement deleteBtn = foundElement.findElement(By.xpath(".//span[@class='e-icons e-file-remove-btn']"));
			clickElementAction(deleteBtn);
			logDebugMessage(concatStrings("Clicked the remove button for the uploaded file '", fileNameToRemove, "'"));
		}
	}
	
	/**
	 * For example if the focus is in the Account Number (e.g. selenium clicked the
	 * Account Number field) and you want to select all the text in there, you can
	 * use this method.
	 * 
	 * This is the shortcut Control + A for Windows.
	 * 
	 */
	public void selectAllTextFromField() {

		Actions actions = new Actions(driver);
		actions.keyDown(Keys.CONTROL);
		actions.sendKeys("a");
		actions.keyUp(Keys.CONTROL);
		actions.perform();

		logDebugMessage("Successfully executed the selectAllTextFromField() method");
	}
	
	/**
	 * Use this if the native WebElement.sendKeys() does not work and is throwing
	 * ElementNotInteractableException
	 * 
	 */
	public void sendKeysAction(WebElement element, CharSequence... charSeq) {

		Actions actions = new Actions(driver);
		actions.moveToElement(element).sendKeys(charSeq).perform();
		String msg = concatStrings("Used the actionSendKeys to send character(s) into the element ",
				element.toString());
		logDebugMessage(msg);
	}
	
	/**
	 * Use this to set the implicit wait value of the driver
	 */
	public void setImplicitWait(long maxWaitTimeInSec) {

		// turn off the implicit wait
		// or change to smaller wait time
		// or return to it's original value
		driver.manage().timeouts().implicitlyWait(maxWaitTimeInSec, TimeUnit.SECONDS);
		logDebugMessage("Changed the value of driver implicit wait to <" + maxWaitTimeInSec + "> second(s)");
	}

	/**
	 * Use this to automatically scroll into the element so it's within the
	 * viewport.
	 * 
	 */
	public void scrollIntoView(WebElement element) {

		WebElement myElement = new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(element));
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].scrollIntoView();", myElement);
		logDebugMessage(concatStrings("Successfully scrolled into the view of this element -> ", element.toString()));
	}

	/**
	 * Use this to input the characters slower using send_keys
	 * 
	 * @param inputElement
	 * @param textToInput
	 * @param sendKeysEnd     pass true if you want to pass Keys.END at every
	 *                        character
	 * @param intervalMillSec
	 */
	public void slowSendKeys(WebElement inputElement, String textToInput, boolean sendKeysEnd, int intervalMillSec) {

		if (sendKeysEnd) {
			logDebugMessage(concatStrings("Will enter this string '", textToInput, "' with a [",
					String.valueOf(intervalMillSec),
					"] milli seconds interval in between characters and sending also Keys.END after each character"));
			for (int i = 0; i < textToInput.length(); i++) {
				char c = textToInput.charAt(i);
				String s = String.valueOf(c);
				pauseSeleniumExecution(intervalMillSec);
				inputElement.sendKeys(s, Keys.END);
			}
		} else {
			logDebugMessage(concatStrings("Will enter this string '", textToInput, "' with a [",
					String.valueOf(intervalMillSec),
					"] milli seconds interval in between characters and sending also Keys.END after each character"));
			for (int i = 0; i < textToInput.length(); i++) {
				char c = textToInput.charAt(i);
				String s = String.valueOf(c);
				pauseSeleniumExecution(intervalMillSec);
				inputElement.sendKeys(s);
			}
		}
	}

	/**
	 * Use this when submitting the request
	 * 
	 */
	public void submitRequest(WebElement elementToWait) {

		logDebugMessage(concatStrings("The form has been submitted. Will be waiting for this element to display -> ",
				elementToWait.toString()));
		assertTrue(waitForElement(elementToWait, PORTAL_SUBMIT_REQUEST_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT),
				concatStrings("Error submitting the request. Waited for <",
						Integer.toString(PORTAL_SUBMIT_REQUEST_TIMEOUT), "> seconds for this element to appear. -> ",
						elementToWait.toString()));
	}

	/**
	 * Use this to switch to the Merchant Warrior iframe to enter Credit Card
	 * details in the Make Payment portal
	 * 
	 * OR
	 * 
	 * Enter Direct Debit details credit card section in the Move In portal using
	 * Merchant Warrior iframe
	 * 
	 */
	public void switchToMWIframe() {

		try {
			logDebugMessage("Will be switching now to Merchant Warrior Iframe");
			setImplicitWait(1);
			driver.switchTo().frame("mwIframe");
			logDebugMessage("Successfully switched to the Merchant Warrior 'mwIframe' Iframe");
		} catch (Exception e) {
			logDebugMessage(concatStrings("An exception has been encountered. Please see message for more details -> ",
					e.getMessage()));
			throw e;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to switch outside of any iframe
	 * 
	 */
	public void switchToDefaultContent() {

		try {
			logDebugMessage("Will be switching now to default content");
			setImplicitWait(1);
			driver.switchTo().defaultContent();
			logDebugMessage("Successfully switched to the default content");
		} catch (Exception e) {
			logDebugMessage(concatStrings("An exception has been encountered. Please see message for more details -> ",
					e.getMessage()));
			throw e;
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to switch to the parent iframe
	 * 
	 * Useful when you are inside 'iframe1', then you switch to 'iframe2' then you
	 * want to go back to 'iframe1'
	 * 
	 */
	public void switchToParentFrame() {

		try {
			logDebugMessage("Will be switching now to the parent iframe");
			setImplicitWait(1);
			driver.switchTo().parentFrame();
			logDebugMessage("Successfully switched to the parent iframe");
		} catch (Exception e) {
			logDebugMessage(concatStrings("An exception has been encountered. Please see message for more details -> ",
					e.getMessage()));
			throw e;
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to switch into the Bluebilling Iframe for move in embedded.
	 * 
	 * Not sure why but when just using the normal switch into the Bluebilling
	 * Iframe, it waits for the IMPLICIT_WAIT_TIMEOUT default value (even if the
	 * frame is already present) before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bluebilling
	 * iframe
	 */
	public void switchToMoveInEmbeddedIframe(long implicitWaitInSec) {

		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-move-in-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-move-in-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-move-in-iframe' iframe");
		} finally {
			// return the original value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to switch into the Bluebilling Iframe for move out embedded.
	 * 
	 * Not sure why but when just using the normal switch into the Bluebilling
	 * Iframe, it waits for the IMPLICIT_WAIT_TIMEOUT default value (even if the
	 * frame is already present) before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bb iframe
	 */
	public void switchToMoveOutEmbeddedIframe(long implicitWaitInSec) {

		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-move-out-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-move-out-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-move-out-iframe' iframe");
		} finally {
			// return the original value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/**
	 * Use this to switch into the Bluebilling Iframe for connection embedded.
	 * 
	 * Not sure why but when just using the normal switch into the Bluebilling
	 * Iframe, it waits for the IMPLICIT_WAIT_TIMEOUT default value (even if the
	 * frame is already present) before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bb iframe
	 */
	public void switchToConnectionEmbeddedIframe(long implicitWaitInSec) {

		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-connection-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-connection-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-connection-iframe' iframe");
		} finally {
			// return the original value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * Use this to switch into the Bluebilling Iframe for customer portal embedded.
	 * 
	 * Not sure why but when just using the normal switch into the Bluebilling
	 * Iframe, it waits for the IMPLICIT_WAIT_TIMEOUT default value (even if the
	 * frame is already present) before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bluebilling
	 * iframe
	 */
	public void switchToCustomerPortalEmbeddedIframe(long implicitWaitInSec) {

		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-customer-portal-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-customer-portal-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-customer-portal-iframe' iframe");
		} finally {
			// return the original value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/**
	 * Close down the session once testing is finished.
	 */
	public void tearDown() {

		// close all the windows including the current one
		closeOpenedWindows(false);
		logDebugMessage("Closing the WebDriver instance");
		// close the driver
		driver.quit();
		logDebugMessage("Successfully closed the WebDriver instance");
		logDebugMessage("Closing the database connection");
		// close down the database connection
		TestDatabase.close();
		logDebugMessage("Successfully closed the database connection");
	}
	
	/**
	 * README There is an an issue where a currency field in excel is not converted
	 * correctly into a csv file. For example, in excel a field has value of
	 * -561.18, but when converted into csv file the value became like this
	 * -561.179999999999
	 * 
	 * This goes through all the files in the download directory and extracts files
	 * from any ZIP files and any Excel files (XLS/XLSX format) are converted into
	 * CSV format.
	 * 
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 * 
	 */
	@SuppressWarnings("deprecation")
	public void unpackFiles() throws IOException, EncryptedDocumentException, InvalidFormatException {

		// check if the download directory exists
		FileObject downloadDir = fsManager.resolveFile(DOWNLOADS_DIR);
		if (!downloadDir.exists()) {
			LOG.info(MessageFormat.format("directory {0} does not exist", DOWNLOADS_DIR));
			return;
		}

		// go through all the files and if any ZIP file is found extract its
		// content into it's own file
		FileObject[] mainFiles = downloadDir.getChildren();
		LOG.info(MessageFormat.format("found {0} {0,choice,0#file|1#file|1<files} in directory {1}", mainFiles.length,
				DOWNLOADS_DIR));
		for (int i = 0; i < mainFiles.length; i++) {
			// ensure we are only working with files
			if (mainFiles[i].getType() == FileType.FILE) {
				// check if the file is a ZIP file first
				if (mainFiles[i].getName().getExtension().equalsIgnoreCase("ZIP")) {
					// dealing with a ZIP file, obtain the ZIP file object version now
					final FileObject zipFile = fsManager.resolveFile("zip:" + mainFiles[i].getName().getURI());
					// read the content of the ZIP file
					final FileObject[] zipChildren = zipFile.getChildren();
					for (int j = 0; j < zipChildren.length; j++) {
						OutputStream outputStream = null;
						FileObject outputFile = null;
						try {
							// get the next file in the ZIP archive and extract it
							final String outputFileName = new StringBuilder(zipFile.getParent().toString())
									.append(File.separator).append(zipChildren[j].getName().getBaseName()).toString();
							outputFile = fsManager.resolveFile(outputFileName);
							outputFile.createFile();
							// write the content of the extracted file
							outputStream = outputFile.getContent().getOutputStream();
							outputStream.write(FileUtil.getContent(zipChildren[j]));
						} finally {
							if (outputStream != null) {
								outputStream.flush();
								outputStream.close();
							}

							zipChildren[j].close();
						}

						// if the extracted file was an excel file convert it's content into CSV
						if (isFileExcel(outputFile))
							ExcelToCSVConvertor.convertXLSToCSV(outputFile);
					}

					try {
						// close the ZIP file - need to do it via closing of
						// the ZIP file system (lib bug)
						zipFile.getFileSystem().getFileSystemManager().closeFileSystem(zipFile.getFileSystem());
					} catch (Exception e) {
					}
				}
				// check if we are dealing with
				else if (isFileExcel(mainFiles[i])) {
					// dealing with an excel file, extract it's content into the directory
					ExcelToCSVConvertor.convertXLSToCSV(mainFiles[i]);
				}
			}

			mainFiles[i].close();
		}
	}
	
	/**
	 * Update the service point, gate meter and complex details, this will update
	 * the following fields for each accounts
	 * 
	 * bbeng_service_points.snp_life_support (where bbeng_service_points.gate_meter
	 * = 'F') bbeng_service_points.snp_life_support (where
	 * bbeng_service_points.gate_meter = 'T')
	 * bbeng_service_points.snp_service_points (where
	 * bbeng_service_points.gate_meter = 'T')
	 * bbeng_service_points.snp_on_market_pnts (where
	 * bbeng_service_points.gate_meter = 'T') bbeng_site_details.snp_accounts
	 * bbeng_site_details.snp_service_type bbeng_site_details.snp_open_date
	 * bbeng_site_details.snp_close_date bbeng_site_details.snp_gate_meters
	 * bbeng_site_details.snp_service_points bbeng_site_details.snp_on_market
	 * bbeng_site_details.snp_life_support
	 * 
	 * @param date should be passed in DB date format
	 */
	public void updateServiceStatusAll(String date) throws SQLException {

		try {
			logDebugMessage("Will now update snap shot values related to Service Points, Gate Meters and Complexes");
			executeUpdate("CALL sp_snp_service_status_all('" + date + "');");
		} catch (SQLException sqle) {
			throw (new SQLException(
					"An SQLException was encountered while trying to run the Stored Procedure sp_snp_service_status_all. See message for more details -> "
							+ sqle.getMessage()));
		}
	}

	/**
	 * Update the snapshot values for all accounts, this will update the following
	 * fields for each accounts
	 * 
	 * bbeng_account_details.snp_account_open bbeng_account_details.snp_open_date
	 * bbeng_account_details.snp_close_date
	 * bbeng_account_details.snp_life_support_status
	 * bbeng_account_details.snp_final_billed bbeng_account_details.snp_network_area
	 * bbeng_account_details.snp_tenancy_address
	 * bbeng_account_details.snp_tenancy_suburb
	 * bbeng_account_details.snp_tenancy_state
	 * bbeng_account_details.snp_tenancy_postcode
	 * bbeng_account_details.snp_service_type
	 * bbeng_account_details.snp_cnsmp_profile
	 * bbeng_account_details.snp_on_market_status
	 * bbeng_account_details.snp_market_identifier
	 * bbeng_account_details.snp_payment_plan_count
	 * 
	 * @param date should be passed in DB date format
	 * 
	 * @throws SQLException
	 */
	public void updateAccountStatusAll(String date) throws SQLException {

		try {
			logDebugMessage("Will now update snap shot values related to Accounts");
			executeUpdate("CALL sp_snp_account_status_all('" + date + "');");
		} catch (SQLException sqle) {
			throw (new SQLException(
					"An SQLException was encountered while trying to run the Stored Procedure sp_snp_account_status_all. See message for more details -> "
							+ sqle.getMessage()));
		}
	}

	/**
	 * Update the Accounts Transactions
	 * 
	 * @param date should be passed in DB date format
	 */
	public void updateAccountTransactionsAll(String date) throws SQLException {

		try {
			logDebugMessage("Will now update snap shot values related to Accounts Transactions");
			executeUpdate("CALL sp_snp_account_transactions_all('" + date + "');");
		} catch (SQLException sqle) {
			throw (new SQLException(
					"An SQLException was encountered while trying to run the Stored Procedure sp_snp_account_transactions_all. See message for more details -> "
							+ sqle.getMessage()));
		}
	}
	
	/** 
	 * 
	 * */
	public void uploadConfig(AccessS3BucketWithVfs s3Access, String portalConfigDir, String dirNum,
			String s3FileNameToReplaceOrUpload) {

		// upload the correct portal_config.json we are testing
		try {
			String fileToUpload = concatStrings(portalConfigDir, dirNum, s3FileNameToReplaceOrUpload);
			s3Access.uploadConfigFileIntoS3Bucket(fileToUpload, S3_PORTAL_CONFIG_BUCKET_NAME,
					getAwsPortalConfigFolderName(), s3FileNameToReplaceOrUpload);
			String logMsg = concatStrings("Uploaded the ", s3FileNameToReplaceOrUpload, " file located in '",
					fileToUpload, "' into the S3 Bucket '", S3_PORTAL_CONFIG_BUCKET_NAME, "', inside the directory '",
					getAwsPortalConfigFolderName(), "'");
			logDebugMessage(logMsg);
		} catch (FileSystemException fse) {
			logDebugMessage("A FileSystemException has been encountered. Please see error for more details -> "
					+ fse.getMessage());
			throw (new ErrorMessageException(
					"A FileSystemException has been encountered. Please see error for more details -> "
							+ fse.getMessage()));
		}
	}
	
	/** 
	 * Use this to upload custom CSS config in the portal config bucket
	 * */
	public void uploadCustomCss(AccessS3BucketWithVfs s3Access, String cssDir,
			String s3FileNameToReplaceOrUpload) {

		try {
			// upload the portal_config.css we are using
			String fileToUpload = concatStrings(cssDir, s3FileNameToReplaceOrUpload);
			s3Access.uploadConfigFileIntoS3Bucket(fileToUpload, S3_PORTAL_CONFIG_BUCKET_NAME,
					getAwsPortalConfigFolderName(), s3FileNameToReplaceOrUpload);
			logDebugMessage(concatStrings("Uploaded the ", s3FileNameToReplaceOrUpload, " file located in '",
					fileToUpload, "' into the S3 Bucket '", S3_PORTAL_CONFIG_BUCKET_NAME, "', inside the directory '",
					getAwsPortalConfigFolderName(), "'"));
			
			// upload the related font styles being used in the CSS
			String fontsLoc = concatStrings(cssDir, "fonts\\");
			List<String> fontsList = getFileNamesFromDir(fontsLoc);
			for (String font : fontsList) {
				String fontToUpload = concatStrings(fontsLoc, font);
				s3Access.uploadConfigFileIntoS3Bucket(fontToUpload, S3_PORTAL_CONFIG_BUCKET_NAME,
						getAwsPortalConfigFolderName().concat("/fonts"), font);
			}
			
			logDebugMessage(concatStrings("Uploaded all the files ", fontsList.toString(), " from the directory ",
					cssDir, "fonts", "' into the S3 bucket '", S3_PORTAL_CONFIG_BUCKET_NAME,
					"', inside the directory '", getAwsPortalConfigFolderName(), "/fonts'"));
		} catch (FileSystemException fse) {
			logDebugMessage(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage()));
			throw (new ErrorMessageException(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage())));
		}
	}
	
	/** 
	 * Upload the custom language file
	 * */
	public void uploadCustomLangFile(AccessS3BucketWithVfs s3Access, String customLangFileDir,
			PortalNamesEnum portalNameEnum, String dirNum, String s3FileNameToReplaceOrUpload) {

		try {
			String fileToUpload = concatStrings(customLangFileDir, dirNum, s3FileNameToReplaceOrUpload);
			String portalName = portalNameEnum.getLabelText();
			s3Access.uploadCustomLangFiles(fileToUpload, S3_PORTAL_CONFIG_BUCKET_NAME, getAwsPortalConfigFolderName(),
					portalName, S3_PORTAL_LANG_FILES_DIR, s3FileNameToReplaceOrUpload);
			String logMsg = concatStrings("Uploaded the custom language file ", s3FileNameToReplaceOrUpload,
					" located in '", fileToUpload, "' into the S3 Bucket '", S3_PORTAL_CONFIG_BUCKET_NAME,
					"', inside the directory '", getAwsPortalConfigFolderName(), "/", portalName,
					S3_PORTAL_LANG_FILES_DIR, "'");
			logDebugMessage(logMsg);
		} catch (FileSystemException fse) {
			logDebugMessage("A FileSystemException has been encountered. Please see error for more details -> "
					+ fse.getMessage());
			throw (new ErrorMessageException(
					"A FileSystemException has been encountered. Please see error for more details -> "
							+ fse.getMessage()));
		}
	}
	
	/** 
	 * 
	 * */
	public void uploadGlobalLangFile(AccessS3BucketWithVfs s3Access, String globalLangFileDir,
			PortalNamesEnum portalNameEnum, String dirNum, String s3FileNameToReplaceOrUpload) {

		try {
			String fileToUpload = concatStrings(globalLangFileDir, dirNum, s3FileNameToReplaceOrUpload);
			String s3PortalFolderName = portalNameEnum.getLabelText();
			s3Access.uploadGlobalLangFiles(fileToUpload, getCloudFrontOriginBucketName(), s3PortalFolderName,
					s3FileNameToReplaceOrUpload);
			logDebugMessage(concatStrings("Uploaded the global language file '", s3FileNameToReplaceOrUpload,
					"' located in '", fileToUpload, "' into the S3 bucket '", getCloudFrontOriginBucketName(),
					"', inside the portal folder name '", s3PortalFolderName, "'"));
		} catch (FileSystemException fse) {
			logDebugMessage(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage()));
			throw (new ErrorMessageException(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage())));
		}
	}

	/**
	 * Use this to loop through @param listArray and verify that it has a string
	 * that starts with @param startsWithString
	 * 
	 * @param listArray
	 * 
	 * @param assertionMethod       pass true to use 'assertTrue' assertion method
	 *                              while false for 'assertFalse'
	 * 
	 * @param startsWithString
	 * 
	 * @param caseSensitiveChecking pass true for case sensitive checking
	 * 
	 */
	public void verifyStringStartsWithInList(List<String> listArray, boolean assertionMethod, String startsWithString,
			boolean caseSensitiveChecking) {

		// let's separate each text in ; for logging
		String joined = StringUtils.join(listArray, "; ");
		String logMsg = concatStrings("Validating as ", (caseSensitiveChecking ? "case sensitive" : "case insensitive"),
				" that the List array -> [", joined, "]", " has a string that starts with '", startsWithString,
				"' using the assertion method ", (assertionMethod ? "assertTrue" : "assertFalse"));
		logDebugMessage(logMsg);

		if (assertionMethod) {
			if (caseSensitiveChecking) {
				boolean stringFound = false;
				for (int i = 0; i < listArray.size(); i++) {
					String stringToCheck = listArray.get(i);
					if (stringToCheck.startsWith(startsWithString)) {
						stringFound = true;
						break;
					}
				}
				assertTrue(stringFound, "The List -> " + listArray + " does not have a string that starts with '"
						+ startsWithString + "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				boolean stringFound = false;
				for (int i = 0; i < listArray.size(); i++) {
					String stringToCheck = listArray.get(i).toLowerCase();
					if (stringToCheck.startsWith(startsWithString.toLowerCase())) {
						stringFound = true;
						break;
					}
				}
				assertTrue(stringFound, "The List -> " + listArray + " does not have a string that starts with '"
						+ startsWithString + "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			}
		} else {
			if (caseSensitiveChecking) {
				boolean stringFound = false;
				for (int i = 0; i < listArray.size(); i++) {
					String stringToCheck = listArray.get(i);
					if (stringToCheck.startsWith(startsWithString)) {
						stringFound = true;
						break;
					}
				}
				assertFalse(stringFound, "The List -> " + listArray + " has a string that starts with '"
						+ startsWithString + "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				boolean stringFound = false;
				for (int i = 0; i < listArray.size(); i++) {
					String stringToCheck = listArray.get(i).toLowerCase();
					if (stringToCheck.startsWith(startsWithString.toLowerCase())) {
						stringFound = true;
						break;
					}
				}
				assertFalse(stringFound, "The List -> " + listArray + " has a string that starts with '"
						+ startsWithString + "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			}
		}
	}

	/**
	 * This would verify that each string in the list array contains stringContains
	 * 
	 * @param caseSensitiveChecking pass true if you want the checking to be case
	 *                              sensitive
	 */
	public void verifyStringContainsInEachListPacket(List<String> listArray, String stringContains,
			boolean caseSensitiveChecking) {

		// let's separate each text in ; for logging
		String joined = StringUtils.join(listArray, "; ");
		logDebugMessage("Validating as " + (caseSensitiveChecking ? "case sensitive" : "case insensitive")
				+ " that string '" + stringContains + "' is present in every packet in List array -> [" + joined + "]");

		if (caseSensitiveChecking) {
			for (String string : listArray) {
				assertTrue(string.contains(stringContains), "This string -> [" + string
						+ "] in the list array does not contain -> [" + stringContains + "]");
			}
		} else {
			for (String string : listArray) {
				assertTrue(string.toLowerCase().contains(stringContains.toLowerCase()),
						"This string -> [" + string.toLowerCase() + "] in the list array does not contain -> ["
								+ stringContains.toLowerCase() + "]");
			}
		}
	}

	/**
	 * This would verify that in the array, it has at least 1 stringToCompare in the
	 * packets.
	 * 
	 * @param listArray             pass an args with type List<String>
	 * @param assertionMethod       pass true if you want to use assertTrue
	 *                              assertion; false for assertFalse
	 * @param stringToCompare       pass the args to compare using contains
	 *                              operator.
	 * @param caseSensitiveChecking pass true if you want it to be case sensitive
	 *                              when checking
	 * 
	 */
	public void verifyStringContainsInList(List<String> listArray, boolean assertionMethod, String stringToCompare,
			boolean caseSensitiveChecking) {

		// let's separate each text in ; for logging
		String joined = StringUtils.join(listArray, "; ");
		String logMsg = concatStrings("Validating as ", (caseSensitiveChecking ? "case sensitive" : "case insensitive"),
				" that string '", stringToCompare, "' contains in List array -> [", joined, "]",
				" using the assertion method ", (assertionMethod ? "'assertTrue'" : "'assertFalse'"));
		logDebugMessage(logMsg);

		if (assertionMethod) {
			if (caseSensitiveChecking) {
				assertTrue(listArray.toString().contains(stringToCompare),
						"The List -> " + listArray + " does not contain '" + stringToCompare
								+ "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				assertTrue(listArray.toString().toLowerCase().contains(stringToCompare.toLowerCase()),
						"The List -> " + listArray + " does not contain '" + stringToCompare
								+ "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			}
		} else {
			if (caseSensitiveChecking) {
				assertFalse(listArray.toString().contains(stringToCompare), "The List -> " + listArray + " contains '"
						+ stringToCompare + "', caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				assertFalse(listArray.toString().toLowerCase().contains(stringToCompare.toLowerCase()),
						"The List -> " + listArray + " contains '" + stringToCompare + "', caseSensitiveChecking is ["
								+ caseSensitiveChecking + "]");
			}
		}
	}

	/**
	 * This would verify that in the array, it has at least 1 stringToCompare in the
	 * packets.
	 * 
	 * @param listArray             pass an args with type List<String>
	 * @param assertionMethod       pass true if you want to use assertTrue
	 *                              assertion; false for assertFalse
	 * @param stringsToCompare      pass the list of stringsToCompare to check in
	 *                              the listArray
	 * @param caseSensitiveChecking pass true if you want it to be case sensitive
	 *                              when checking
	 * 
	 */
	public void verifyStringContainsInAnyStringInList(List<String> listArray, boolean assertionMethod,
			List<String> stringsToCompare, boolean caseSensitiveChecking) {

		// let's separate each text in ; for logging
		String joinedListArray = StringUtils.join(listArray, "; ");
		String joinedStringsToCompare = StringUtils.join(stringsToCompare, "; ");
		String logMsg = concatStrings("Validating as ", (caseSensitiveChecking ? "case sensitive" : "case insensitive"),
				" that any of the string listed in List array -> [", joinedStringsToCompare,
				"] contains in List array -> [", joinedListArray, "]", " using the assertion method ",
				(assertionMethod ? "'assertTrue'" : "'assertFalse'"));
		logDebugMessage(logMsg);

		if (assertionMethod) {
			if (caseSensitiveChecking) {
				boolean containsString = false;
				for (String string : stringsToCompare) {
					logDebugMessage(concatStrings("Validating that this list [", joinedListArray,
							"] contains the string '", string, "'"));
					if (listArray.toString().contains(string)) {
						containsString = true;
						break;
					}
				}
				assertTrue(containsString,
						"The List -> " + listArray + " does not contain any of the string listed in -> "
								+ stringsToCompare + ", caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				boolean containsString = false;
				for (String string : stringsToCompare) {
					logDebugMessage(concatStrings("Validating that this list [", joinedListArray.toLowerCase(),
							"] contains the string '", string.toLowerCase(), "'"));
					if (listArray.toString().toLowerCase().contains(string.toLowerCase())) {
						containsString = true;
						break;
					}
				}
				assertTrue(containsString,
						"The List -> " + listArray.toString().toLowerCase()
								+ " does not contain any of the string listed in -> " + stringsToCompare
								+ ", caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			}
		} else {
			if (caseSensitiveChecking) {
				boolean containsString = false;
				for (String string : stringsToCompare) {
					logDebugMessage(concatStrings("Validating that this list [", joinedListArray,
							"] does not contain the string '", string, "'"));
					if (listArray.toString().contains(string)) {
						containsString = true;
						break;
					}
				}
				assertFalse(containsString, "The List -> " + listArray + " contains any of the string listed in -> "
						+ stringsToCompare + ", caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			} else {
				boolean containsString = false;
				for (String string : stringsToCompare) {
					logDebugMessage(concatStrings("Validating that this list [", joinedListArray.toLowerCase(),
							"] does not contain the string '", string.toLowerCase(), "'"));
					if (listArray.toString().toLowerCase().contains(string.toLowerCase())) {
						containsString = true;
						break;
					}
				}
				assertFalse(containsString,
						"The List -> " + listArray.toString().toLowerCase()
								+ " contains any of the string listed in -> " + stringsToCompare
								+ ", caseSensitiveChecking is [" + caseSensitiveChecking + "]");
			}
		}
	}

	/**
	 * Use this to verify the expected number of mat-option values displayed
	 * 
	 */
	public void verifyNumOfMatOptionValuesDisp(WebElement matOptionDiv, int expectedNum) {

		int actualAddresses = getNumOfMatOptionValues(matOptionDiv);
		assertEquals(actualAddresses, expectedNum, "The expected number of displayed addresses is incorrect");
	}

	/**
	 * Use this to verify if two Strings are equal and removing all the spaces
	 * before validation
	 */
	public void verifyTwoStringsAreEqualNoSpaces(String actual, String expected) {

		logDebugMessage(
				"Will be deleting all the white spaces in the string '" + actual + "' and string '" + expected + "'");
		String value1 = StringUtils.deleteWhitespace(actual);
		String value2 = StringUtils.deleteWhitespace(expected);
		logDebugMessage("Validating that this actual string '" + value1 + "' is equal to this expected string '"
				+ value2 + "'");
		assertTrue(value1.equals(value2),
				"This actual string '" + value1 + "' is not equal to this expected string '" + value2 + "'");
	}

	/**
	 * Use this to verify if two Strings are equal
	 * 
	 * @param actual
	 * @param expected
	 * @param normalizeSpaces pass true if you want any leading and trailing spaces
	 *                        removed then spaces in between updated to 1 space.
	 */
	public void verifyTwoStringsAreEqual(String actual, String expected, boolean normalizeSpaces) {

		if (normalizeSpaces) {
			logDebugMessage("Need to normalize the spaces in the actual string '" + actual + "' and expected string '"
					+ expected + "'");
			String value1 = StringUtils.normalizeSpace(actual);
			String value2 = StringUtils.normalizeSpace(expected);
			logDebugMessage("Validating that this actual string '" + value1 + "' is equal to this expected string '"
					+ value2 + "' with normalizeSpaces [" + normalizeSpaces + "]");
			assertTrue(value1.equals(value2),
					"This actual string '" + value1 + "' is not equal to this expected string '" + value2 + "'");
		} else {
			logDebugMessage("No need to normalize the spaces in the supplied string args");
			logDebugMessage("Validating that this actual string '" + actual + "' is equal to this expected string '"
					+ expected + "' with normalizeSpaces [" + normalizeSpaces + "]");
			assertTrue(actual.equals(expected),
					"This actual string '" + actual + "' is not equal to this expected string '" + expected + "'");
		}
	}

	/**
	 * Use this to verify that the provided string is blank/empty/null
	 */
	public void verifyStringIsBlank(String actual) {

		logDebugMessage(concatStrings(
				"Validating that the provided string is blank/empty/null. The value of the provided string is '",
				actual, "'"));
		assertTrue(StringUtils.isBlank(actual),
				concatStrings("The provided string is not blank/empty/null. It has a value of '", actual, "'"));
	}

	/**
	 * Use this to verify if a text contains a certain string or not.
	 * 
	 * Pass true to use the assertTrue assertion, otherwise use assertFalse
	 * 
	 * This is case sensitive.
	 * 
	 * @param assertMethod
	 * @param actual
	 * @param containsText
	 */
	public void verifyStringContains(boolean assertMethod, String actual, String containsText) {

		if (assertMethod) {
			logDebugMessage(
					"Validating that the actual string '" + actual + "' contains the text '" + containsText + "'");
			assertTrue(actual.contains(containsText),
					"The actual string '" + actual + "' does not contain the text '" + containsText + "'");
		} else {
			logDebugMessage("Validating that the actual string '" + actual + "' does not contain the text '"
					+ containsText + "'");
			assertFalse(actual.contains(containsText),
					"The actual string '" + actual + "' contains the text '" + containsText + "'");
		}
	}

	/**
	 * Checks if the CharSequence contains any character in the given set of
	 * characters.
	 * 
	 * A null CharSequence will return false. A null search CharSequence will return
	 * false.
	 * 
	 * StringUtils.containsAny(null, *) = false StringUtils.containsAny("", *) =
	 * false StringUtils.containsAny(*, null) = false StringUtils.containsAny(*, "")
	 * = false StringUtils.containsAny("zzabyycdxx", "za") = true
	 * StringUtils.containsAny("zzabyycdxx", "by") = true
	 * StringUtils.containsAny("zzabyycdxx", "zy") = true
	 * StringUtils.containsAny("zzabyycdxx", "\tx") = true
	 * StringUtils.containsAny("zzabyycdxx", "$.#yF") = true
	 * StringUtils.containsAny("aba", "z") = false
	 * 
	 * @param actual
	 * @param containsAnyText
	 */
	public void verifyStringContainsAny(String actual, String containsAnyText) {

		logDebugMessage("Validating that the actual string '" + actual + "' contains any of the text '"
				+ containsAnyText + "'");
		assertTrue(StringUtils.containsAny(actual, containsAnyText),
				"The actual string '" + actual + "' does not contain any of the text '" + containsAnyText + "'");
	}

	/**
	 * Use this to verify that the string starts with a specific text
	 */
	public void verifyStringStartsWith(String actual, String startsWith) {

		logDebugMessage("Validating that the actual string '" + actual + "' start with the text '" + startsWith + "'");
		assertTrue(actual.startsWith(startsWith),
				"The actual string '" + actual + "' does not starts with the text '" + startsWith + "'");
	}

	/**
	 * The param expectedValues and expectedValues should have the same order Also
	 * it's case sensitive Does not matter with the spaces (e.g. actualValues
	 * {String1, String2} expectedValues {String1,String2} )
	 * 
	 * @param actualValues
	 * @param expectedValues
	 */
	public void verifyTwoListsAreEqual(List<String> actualValues, List<String> expectedValues) {

		// let's separate each text in ; for logging
		String joined1 = StringUtils.join(actualValues, ";");
		String joined2 = StringUtils.join(expectedValues, ";");

		logDebugMessage("Validating that this actual List -> [" + joined1 + "] is equal to this expected List -> ["
				+ joined2 + "]");
		logDebugMessage("The size of actualValues is:" + actualValues.size() + " while the size of expectedValues is:"
				+ expectedValues.size());
		assertTrue(actualValues.equals(expectedValues), "The actualValues List -> " + actualValues
				+ " is not equal to the expectedValues List -> " + expectedValues);
	}

	/**
	 * This can be used when checking the content of the PDF file. The content of
	 * the file is retrieved in 1 long string.
	 * 
	 * @param expectedFileName              is the filename when the file was
	 *                                      downloaded
	 * @param expectNumberOfDownloadedFiles
	 * @param waitTimeBeforeChecking        milli seconds to wait before checking
	 *                                      the downloaded file
	 * @param assertionMethod               pass true if you are expecting the
	 *                                      contents args to be present in the
	 *                                      downloaded file
	 * @param expectedValues                expected values in the file
	 * 
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void verifyPdfContent(String expectedFileName, int expectNumberOfDownloadedFiles, int waitTimeBeforeChecking,
			boolean assertionMethod, String... expectedValues)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		// get the content of every file in the download directory
		Map<String, String> fileContentMap = getFileContentFromDownloadDir(waitTimeBeforeChecking, true);
		// check that the right number of files have been downloaded
		assertEquals(fileContentMap.size(), expectNumberOfDownloadedFiles, "Number of downloaded files is incorrect!");

		// get the file name of the downloaded file
		String fileNameRaw = getStringUntil(fileContentMap.toString(), "=");
		// let's remove that extra { character at the beginning
		String fileName = getString(fileNameRaw, 1);

		// only get the content of the file we are interested in
		String fileContent = fileContentMap.values().iterator().next();

		// let's put each texts separated by break tags
		// in an array
		List<String> stringValueArray = getEachTextInBreakTags(fileContent, false);
		// let's separate each text in ; for logging
		String joined1 = StringUtils.join(expectedValues, ";");
		String joined2 = StringUtils.join(stringValueArray, ";");

		logDebugMessage("Validating this file '" + fileName + "' using assert method(" + assertionMethod
				+ "). The expected values to be checked -> " + joined1 + ". The actual fileContent captured is -> ["
				+ joined2 + "]");

		// verify the expected file name
		verifyTwoStringsAreEqual(fileName, expectedFileName, true);

		if (assertionMethod) {
			// now let's verify that the expected value(s)
			// is/are seen in joined2
			for (String expectedValue : expectedValues) {
				assertTrue(joined2.contains(expectedValue),
						"The file " + fileName + " does not contain '" + expectedValue + "' in the file content");
			}
		} else {
			// now let's verify that the expected value(s)
			// is/are not seen in joined2
			for (String expectedValue : expectedValues) {
				assertFalse(joined2.contains(expectedValue),
						"The file " + fileName + " contains '" + expectedValue + "' in the file content");
			}
		}
	}

	/**
	 * Use this to verify the number of attachments from an email record view
	 * 
	 * @param trLoc                    is the Attachments location for the
	 *                                 <tr>
	 *                                 tag
	 * @param expectedNumOfAttachments
	 */
	public void verifyNumOfEmailAttachments(int trLoc, int expectedNumOfAttachments) {

		WebElement emailTable = getEmailTable();
		WebElement attachmentsTrLoc = emailTable.findElement(By.xpath("./tbody/tr[" + trLoc + "]/td[2]"));
		List<WebElement> attachments = attachmentsTrLoc.findElements(By.xpath("./slot/a"));
		assertEquals(attachments.size(), expectedNumOfAttachments,
				"The expected number of email attachments is incorrect");
	}
	
	/**
	 * Use this when waiting for the ABN/ACN to finish searching
	 * 
	 */
	public void waitForAbnAcnToFinishSearch(int timeoutInSec, String errorMsg) {

		logDebugMessage(concatStrings("Will waiting for the ABN/ACN to finish searching. Timeout is [",
				String.valueOf(timeoutInSec), "]"));
		try {
			AccountDetailsMoveIn accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			waitUntilElementNoLongerExists(accountdetailsmovein.loadingAbnAcnList, timeoutInSec,
					PORTAL_IMPLICIT_WAIT_TIMEOUT, errorMsg);
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		logDebugMessage("Finished waiting for ABN/ACN to finish searching");
	}
	
	/** 
	 * Use this to wait until the CSS renders
	 * */
	public void waitForCssToRender() {
		
		waitForCssToRender(400);
	}
	
	/** 
	 * Use this to wait when clicking the Next button
	 * or header of the next section because the sections
	 * are still adjusting.
	 * 
	 * Or if you are opening or closing a modal dialog
	 * */
	public void waitForScreenToRender() {
		
		waitForScreenToRender(1000);
	}
	
	/**
	 * Wait for element to appear, using fluent wait.
	 * 
	 * @param maxTimeInSec
	 * @param implicitWait
	 * @param byElement
	 *
	 * @return if the element appeared
	 */
	public boolean waitForElement(WebElement webElement, int maxTimeInSec, int originalImplicitWait) {

		// turn off the implicit wait
		setImplicitWait(0);

		FluentWait<WebDriver> waitTimer = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(maxTimeInSec)).pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);

		// check if the element appeared
		boolean elementPresent = false;

		// log that we are waiting for the element to appear
		logDebugMessage(String.format("Waiting a maximum of %d sec(s) for this element to appear -> %s", maxTimeInSec,
				webElement.toString()));

		try {
			elementPresent = waitTimer.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					WebElement element = webElement;
					return element != null && element.isDisplayed();
				}
			}).booleanValue();
		} catch (TimeoutException toe) {
			logWarnMessage("Done waiting a maximum of " + maxTimeInSec
					+ " seconds but element was not found. See exception details -> " + toe.getMessage());
			elementPresent = false;
		} finally {
			// turn back on the implicit wait
			setImplicitWait(originalImplicitWait);
		}

		// log that we are done waiting for the element to appear
		if (elementPresent)
			logDebugMessage("This element was found " + webElement.toString());

		return elementPresent;
	}

	/**
	 * Wait for element to appear, using fluent wait.
	 *
	 * @param byElement
	 * @param maxTimeInSec
	 * @return if the element appeared
	 */
	public boolean waitForElement(final By byElement, int maxTimeInSec, int originalImplicitWait) {

		// turn off the implicit wait
		setImplicitWait(0);

		FluentWait<WebDriver> waitTimer = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(maxTimeInSec)).pollingEvery(Duration.ofMillis(1000))
				.ignoring(NoSuchElementException.class);

		// check if the element appeared
		boolean elementPresent = false;

		// log that we are waiting for the element to appear
		if (LOG.isInfoEnabled())
			LOG.info(String.format("Waiting a maximum of %d sec(s) for this element to appear -> %s", maxTimeInSec,
					byElement.toString()));

		try {
			elementPresent = waitTimer.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					WebElement element = driver.findElement(byElement);
					return element != null && element.isDisplayed();
				}
			}).booleanValue();
		} catch (TimeoutException te) {
			logDebugMessage("Done waiting a maximum of " + maxTimeInSec
					+ " seconds but element was not found. See exception details -> " + te.getMessage());
			elementPresent = false;
		} finally {
			// turn back on the implicit wait
			setImplicitWait(originalImplicitWait);
		}

		// log that we are done waiting for the element to appear
		if (elementPresent)
			logDebugMessage("Element was found using this method {" + byElement.toString() + "}");

		return elementPresent;
	}

	/**
	 * Wait for element to be clickable
	 * 
	 * @param element to wait
	 * @param timeout to wait until clickable. This is in seconds.
	 */
	public void waitForElementToBeClickable(WebElement element, int timeout) {

		WebDriverWait wait = new WebDriverWait(driver, timeout);
		wait.until(ExpectedConditions.elementToBeClickable(element));
	}
	
	/** 
	 * Use this to wait until the error message in the email address changes
	 * */
	public void waitForEmailErrorToChange() {
		
		waitForEmailErrorToChange(600);
	}
	
	/** 
	 * Use this to wait until the error message in the email address changes
	 * */
	public void waitForEmailErrorToChange(int waitTimeInMilliSec) {
		
		pauseSeleniumExecution(waitTimeInMilliSec);
	}
	
	/**
	 * Wait for the files to be displayed in the upload section and for the files to
	 * be uploaded in the S3 bucket
	 * 
	 */
	public void waitForFilesToBeUploaded(int waitTimeInMilliSec) {

		logDebugMessage(concatStrings("Will now be waiting [", Integer.toString(waitTimeInMilliSec),
				"] milli seconds for the uploaded files to display in the upload section and in the S3 bucket"));
		pauseSeleniumExecution(waitTimeInMilliSec);
	}
	
	/**
	 * Use this to wait for a specific I frame to display
	 */
	public void waitForIFrameToDisplay(long maxWaitInSec, String iFrameID) {

		logDebugMessage(concatStrings("Will be waiting for a maximum of <", Long.toString(maxWaitInSec),
				"> second(s) for the iFrame with ID '", iFrameID, "' to display"));
		FluentWait<WebDriver> waitTimer = new FluentWait<WebDriver>(driver);
		waitTimer.withTimeout(Duration.ofSeconds(maxWaitInSec));
		waitTimer.pollingEvery(Duration.ofMillis(500));
		waitTimer.ignoring(StaleElementReferenceException.class);
		waitTimer.until(ExpectedConditions.visibilityOfElementLocated(By.id(iFrameID)));
		logDebugMessage(concatStrings("The iFrame '", iFrameID, "' is now displayed"));

		// this does not work, sometimes it hangs
		// even though the page is already loaded, it still keeps on waiting
		// not sure if it's because of ignoring(WebDriverException.class)
//		WebDriverWait wait = new WebDriverWait(driver, maxWaitInSec);
//		wait.ignoring(StaleElementReferenceException.class).ignoring(WebDriverException.class)
//				.until(ExpectedConditions.visibilityOfElementLocated(By.id(iFrameID)));
	}
	
	/**
	 * Find the page with a specific title.
	 *
	 * @param title
	 */
	public void waitForPageTitle(String title, int timeout) {

		logDebugMessage("Will be waiting for <" + timeout + "> seconds for the Page title '" + title + "'");
		WebDriverWait wait = new WebDriverWait(driver, timeout);
		wait.until(ExpectedConditions.titleContains(title));
	}

	/**
	 * Look for a set of page title
	 * 
	 * @param timeout
	 * @param implicitWait
	 * @param titles
	 */
	public void waitForPageTitle(int timeout, int originalImplicitWait, String... titles) {

		try {
			List<String> listOfPageTitles = Arrays.asList(titles);
			logDebugMessage(concatStrings("Will be waiting for <", String.valueOf(timeout),
					"> seconds for the Page title(s) '", listOfPageTitles.toString(), "'"));
			// turn off the implicit wait
			setImplicitWait(0);
			FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeout))
					.pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class);
			for (String title : titles)
				wait.until(ExpectedConditions.titleContains(title));
		} finally {
			// turn back on the implicit wait
			setImplicitWait(originalImplicitWait);
		}
	}

	/**
	 * Use this to wait for the Make Payment IFrame to display
	 */
	public void waitForMakePaymentIFrameToDisplay(long maxWaitInSec) {

		waitForIFrameToDisplay(maxWaitInSec, "bluebilling-make-payment-iframe");
	}

	/**
	 * Use this to wait for the Move In IFrame to display
	 */
	public void waitForMoveInIFrameToDisplay(long maxWaitInSec) {

		waitForIFrameToDisplay(maxWaitInSec, "bluebilling-move-in-iframe");
	}

	/**
	 * Use this to wait for the Move Out IFrame to display
	 */
	public void waitForMoveOutIFrameToDisplay(long maxWaitInSec) {

		waitForIFrameToDisplay(maxWaitInSec, "bluebilling-move-out-iframe");
	}

	/**
	 * Use this to wait for the Customer Portal IFrame to display
	 */
	public void waitForCustomerPortalIFrameToDisplay(long maxWaitInSec) {

		waitForIFrameToDisplay(maxWaitInSec, "bluebilling-customer-portal-iframe");
	}

	/**
	 * Wait for element to appear, using fluent wait.
	 * 
	 * @param maxTimeInSec
	 * @param implicitWait
	 * @param byElement
	 *
	 * @return if the element appeared
	 */
	public void waitUntilElementIsDisplayed(WebElement webElement, int maxTimeInSec, int originalImplicitWait) {

		// turn off the implicit wait
		setImplicitWait(0);

		FluentWait<WebDriver> waitTimer = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(maxTimeInSec)).pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);

		// log that we are waiting for the element to appear
		logDebugMessage(String.format("Waiting a maximum of %d sec(s) for this element to appear -> %s", maxTimeInSec,
				webElement.toString()));

		try {
			waitTimer.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					WebElement element = webElement;
					return element != null && element.isDisplayed();
				}
			});
		} catch (TimeoutException toe) {
			String message = concatStrings("Done waiting a maximum of ", String.valueOf(maxTimeInSec),
					" seconds but element was not found. See exception details -> ", toe.getMessage());
			logWarnMessage(message);
			throw (new TimeoutException(message));
		} finally {
			// turn back on the implicit wait
			setImplicitWait(originalImplicitWait);
		}
	}

	/**
	 * Use this to wait until the element no longer exists
	 */
	public void waitUntilElementNoLongerExists(List<WebElement> elements, int timeoutInSec, long origImplicitWait,
			String errorMsg) {

		// turn off the implicit wait
		setImplicitWait(0);

		FluentWait<WebDriver> waitTimer = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(timeoutInSec)).pollingEvery(Duration.ofMillis(500));

		try {
			waitTimer.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return !isElementExists(elements);
				}
			});
		} catch (TimeoutException toe) {
			logWarnMessage(concatStrings("Done waiting a maximum of ", String.valueOf(timeoutInSec),
					" seconds but element was still found. See exception details -> ", toe.getMessage()));
			throw (new TimeoutException(concatStrings("Done waiting a maximum of <", String.valueOf(timeoutInSec),
					"> second(s) however ", errorMsg)));
		} finally {
			// turn back on the implicit wait
			setImplicitWait(origImplicitWait);
		}
	}

	/**
	 * Use this to wait until supplied elements are visible
	 */
	public void waitUntilElementIsVisible(int timeOutInSec, WebElement element) {

		logDebugMessage(concatStrings("Will be waiting for the element to be visible -> ", element.toString()));
		WebDriverWait wait = new WebDriverWait(driver, timeOutInSec);
		wait.until(ExpectedConditions.visibilityOf(element));
		logDebugMessage("The supplied WebElements are visible");
	}

}
