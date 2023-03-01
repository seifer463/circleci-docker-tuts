package au.com.blueoak.portal.dev.make_payment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import au.com.blueoak.portal.dev.DevBaseTesting;
import au.com.blueoak.portal.pageObjects.make_payment.Buttons;
import au.com.blueoak.portal.pageObjects.make_payment.Labels;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class MakePaymentDevBase extends DevBaseTesting {

	/** This is where the test data are located */
	private static final String PROP_TEST_DATA = "TestDataDevMakePayment.properties";
	
	/** This is where we would save the test data */
	private static final String PROP_SAVED_TEST_DATA = "log/SavedTestDataDevMakePayment.properties";
	
	private static Properties prop = new Properties();
	
	/** 
	 * Load the property details
	 *  */
	static {

		try {
			// let's initialize the property file before running any tests
			InputStream input = new FileInputStream(PROP_TEST_DATA);
			prop = new Properties();
			prop.load(input);
			if (LOG.isInfoEnabled()) {
				LOG.info("Successfully loaded this property file '" + PROP_TEST_DATA + "' for the test data");
			}
		} catch (FileNotFoundException fnfe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("FileNotFoundException is encountered. See message for more details -> " + fnfe.getMessage());
			}
		} catch (IOException ioe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("IOException is encountered. See message for more details -> " + ioe.getMessage());
			}
		}
	}
	
	/** 
	 * Use this to verify if loading status has appeared
	 * */
	private boolean isMakePaymentLoadDisplayed(long timeout, boolean waitToAppear) {
		
		FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);
		
		// check if the "Loading" status to appear
		boolean loadingPresent = false;
		try {
			setImplicitWait(1);
			// check if the "loading" element is present in the table
			loadingPresent = fluentWait.until(
					new Function<WebDriver, Boolean>() {
						public Boolean apply(WebDriver driver) {
							logDebugMessage("Checking the Loading screen");
							
							boolean appeared;
							try {
								appeared = driver.findElement(By.xpath(
										"//div[@class='container ng-star-inserted']/mat-card/mat-spinner")).isDisplayed();
								appeared = true;
							} catch (NoSuchElementException nse) {
								appeared = false;
							}
							
							if (appeared) {
								logDebugMessage("The spinner element WAS found");
							} else {
								logDebugMessage("The spinner element WAS NOT found");
							}

							logDebugMessage("Loading screen "
									+ (appeared ? "appeared" : "not displayed")
									+ ", we are waiting a maximum of "
									+ timeout + " seconds for it to "
									+ (waitToAppear ? "appear" : "disappear"));

							// see if we are waiting for it to appear or disappear
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
			logDebugMessage("Done waiting a maximum of " + timeout
					+ " seconds for the Loading screen to "
					+ (waitToAppear ? "appear" : "disappear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		return (loadingPresent);
	}
	
	/** 
	 * Constructor when initializing this class
	 * */
	public MakePaymentDevBase() {
		
		super.setupTestProp();
	}
	
	/** 
	 * Use this to verify that the Make Payment button is enabled or disabled
	 * */
	protected boolean isMakePaymentBtnEnabled() {
		
		boolean isButtonEnabled;
		
		try {
			setImplicitWait(1);
			Buttons buttons = new Buttons(driver);
			String disabledAttribute = buttons.makePayment.getAttribute("disabled");
			logDebugMessage("The value of disabledAttribute is '" + disabledAttribute + "'");
			if (!StringUtils.isBlank(disabledAttribute)) {
				logDebugMessage("The disabled attribute is found on the Make Payment button - Make Payment button is disabled");
				isButtonEnabled = false;
			} else {
				logDebugMessage("The disabled attribute is not found on the Make Payment button - Make Payment button is not disabled");
				isButtonEnabled = true;
			}
		} catch (NoSuchElementException nsee) {
			logDebugMessage("The disabled attribute is not found on the Make Payment button - Make Payment button is not disabled");
			isButtonEnabled = true;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		
		return isButtonEnabled;
	}
	
	/** 
	 * Use this to verify that the Next button is enabled or disabled
	 * */
	protected boolean isNextBtnEnabled() {
		
		boolean isButtonEnabled;
		
		try {
			setImplicitWait(1);
			Buttons buttons = new Buttons(driver);
			String disabledAttribute = buttons.next.getAttribute("disabled");
			logDebugMessage("The value of disabledAttribute is '" + disabledAttribute + "'");
			if (!StringUtils.isBlank(disabledAttribute)) {
				logDebugMessage("The disabled attribute is found on the Next button - Next button is disabled");
				isButtonEnabled = false;
			} else {
				logDebugMessage("The disabled attribute is not found on the Next button - Next button is not disabled");
				isButtonEnabled = true;
			}
		} catch (NoSuchElementException nsee) {
			logDebugMessage("The disabled attribute is not found on the Next button - Next button is not disabled");
			isButtonEnabled = true;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		
		return isButtonEnabled;
	}
	
	/** 
	 * Use this to add the missing zeroes in the decimal
	 * 
	 * @param stringToAdd
	 * @param decimalPlaces
	 * */
	protected String addMissingZeroes(String stringToAdd, String decimalPlaces) {
		
		// regex matcher if a string has a decimal point
		Pattern p = Pattern.compile("[\\.]");
		Matcher m = p.matcher(stringToAdd);
		boolean hasDecimal = m.find();
		
		String valToReturn;
		
		if (hasDecimal) {
			logDebugMessage("There's an existing decimal found in the stringToAdd '" + stringToAdd + "'");
			StringBuilder sb = new StringBuilder();
			String trimmed = getStringAfter(stringToAdd, ".");
			int trimmedSize = trimmed.length();
			String decimalPoints = decimalPlaces;
			int decimalPointsConv = Integer.parseInt(decimalPoints);
			int decimalsToAdd = decimalPointsConv - trimmedSize;
			for (int i = 0; i < decimalsToAdd; i++) {
				sb.append("0");
			}
			String valToReturnRaw = sb.toString();
			valToReturn = stringToAdd + valToReturnRaw;
		} else {
			logDebugMessage("There's no existing decimal in the stringToAdd '" + stringToAdd + "'");
			StringBuilder sb = new StringBuilder();
			String decimalPoints = decimalPlaces;
			int decimalPointsConv = Integer.parseInt(decimalPoints);
			for (int i = 0; i < decimalPointsConv; i++) {
				sb.append("0");
			}
			String valToReturnRaw = sb.toString();
			valToReturn = stringToAdd + "." + valToReturnRaw;
		}
		
		logDebugMessage("The value to be returned by addMissingZeroes(String) is '" + valToReturn + "'");
		return valToReturn;
	}
	
	
	/**
	 * Use this when you are expecting for the loading screen
	 * to appear and disappear
	 *  */
	protected void loadMakePayment() {
		
		if (isMakePaymentLoadDisplayed(PORTAL_LOAD_TIMEOUT_START, true)) {
			// loading screen appeared, now let's wait
			// until it disappears
			isMakePaymentLoadDisplayed(PORTAL_LOAD_TIMEOUT_END, false);
		}
	}
	
	/**
	 * Add or update a property value.
	 *
	 * @param key
	 * @param value
	 */
	protected void addProp(String key, String value) {

		prop.put(key, value);
		logDebugMessage(concatStrings("Successfully added the value '", value, "' into the property key '", key, "'"));
	}
	
	/** 
	 * Delete the global language files being used throughout the testing
	 * to ensure that it would not be cached by cloudfront
	 * */
	protected void deleteMakePaymentGlobalLangFiles(AccessS3BucketWithVfs s3Access) {

		deleteGlobalLangFiles(s3Access, PortalNamesEnum.MakePayment, "fil.json");
	}
	
    /**
     * Save the properties to a file.
     */
	protected void saveProp() {

		try {
			final OutputStream output = new FileOutputStream(PROP_SAVED_TEST_DATA);
			prop.store(output, null);
			logDebugMessage(concatStrings("Successfully saved this property file -> '", PROP_SAVED_TEST_DATA, "'"));
		} catch (FileNotFoundException fnfe) {
			logDebugMessage(
					"FileNotFoundException was encountered while trying to save properties file. See error message for more details -> "
							+ fnfe.getMessage());
		} catch (IOException ioe) {
			logDebugMessage(
					"IOException was encountered while trying to save properties file. See error message for more details -> "
							+ ioe.getMessage());
		}
	}
	
	/**
	 * Get the value of the property
	 */
	protected String getProp(String key) {

		String value = prop.getProperty(key);
		logDebugMessage("The value to be returned by getProp(String) using the key (" + key + ") is '" + value + "'");
		return value;
	}
	
	/** 
	 * Use this to get the value of the specified column
	 * */
	protected String getPaymentTransactionColValue(String columnName, String instanceId, String accountNum, String payMethod, String payAmt)
			throws SQLException {

		final String query = new StringBuilder("SELECT ").append(columnName)
				.append(" FROM bbprt_payment_processing WHERE instance_id = '").append(instanceId)
				.append("' AND account_number = '").append(accountNum).append("' AND payment_method = '")
				.append(payMethod).append("' AND payment_amount LIKE ").append(payAmt)
				.append(" ORDER BY submitted_timestamp DESC LIMIT 1;").toString();

		String result = executeQuery(query);
		return result;
	}
	
	/** 
	 * Get the number of records that has the same payment_amount
	 * in the bbprt_payment_processing
	 * 
	 * @throws SQLException 
	 * */
	protected int getNumOfRecordsInDbPayProc(String acctNum) throws SQLException {
		
		final String query = new StringBuilder(
				"SELECT COUNT(*) FROM `bbprt_payment_processing` WHERE account_number = '").append(acctNum)
						.append("' GROUP BY payment_amount HAVING COUNT(*) > 1 ORDER BY submitted_timestamp DESC;")
						.toString();
		
		// execute the query
		String result = executeQuery(query);
		int converted;
		
		if (StringUtils.isBlank(result)) {
			converted = 0;
		} else {
			converted = Integer.parseInt(result);
		}
		
		logDebugMessage("The value to be returned by the method getNumOfRecordsInDbPayProc(String) is <" + converted + ">");
		return converted;
	}
	
	/** 
	 * Use this to update the value of the account number
	 * */
	protected void updateAcctNum(String currentAcctNum, String newAcctNum) throws SQLException {
		
		final String query = new StringBuilder("UPDATE bbeng_account_details SET account_number = '").append(newAcctNum)
				.append("' WHERE account_number = '").append(currentAcctNum).append("';").toString();
		
		executeUpdate(query);
	}
	
	/** 
	 * Use this to switch into the Bluebilling Iframe
	 * for payment portals embedded.
	 * 
	 * Not sure why but when just using the normal switch
	 * into the Bluebilling Iframe, it waits for the 
	 * IMPLICIT_WAIT_TIMEOUT default value (even if the frame is already present)
	 * before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bb iframe
	 * */
	protected void switchToMakePaymentEmbeddedIframe(long implicitWaitInSec) {
		
		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-make-payment-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-make-payment-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-make-payment-iframe' iframe");
		} finally {
			// return the orignal value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/** 
	 * Use this to retry the click in the Make Payment button
	 * if the initial click did not process the payment
	 * 
	 * */
	protected void makePaymentBtnRetryClick(int maxRetryCounter) {

		logDebugMessage("Checking if we need to click again the Make Payment button");
		try {
			Buttons buttons = new Buttons(driver, 0);
			Labels labels = new Labels(driver, 0);
			int counter = 0;
			boolean isProgBarDisp = isElementExists(labels.paymentProgBarList);
			if (!isProgBarDisp) {
				logDebugMessage(
						"The Progress bar is still not displayed after the initial click of the Make Payment button. Will retry to click Make Payment button.");
				while (!isProgBarDisp && counter < maxRetryCounter) {
					clickElementAction(buttons.makePayment);
					logDebugMessage(
							concatStrings("Clicked the Make Payment button again. The current number of clicks is [",
									Integer.toString(counter + 1), "]"));
					pauseSeleniumExecution(300);
					isProgBarDisp = isElementExists(labels.paymentProgBarList);
					counter++;
				}
			} else {
				logDebugMessage(
						"No need to click again the Make Payment button because the initial click processed the payment");
			}
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/** 
	 * Use this to retry the click in the Next button
	 * if the initial click did not process the request
	 * 
	 * */
	protected void nextBtnRetryClick(int maxRetryCounter) {

		logDebugMessage("Checking if we need to click again the Next button");
		try {
			Buttons buttons = new Buttons(driver, 0);
			Labels labels = new Labels(driver, 0);
			int counter = 0;
			boolean isProgBarDisp = isElementExists(labels.nextProgBarList);
			if (!isProgBarDisp) {
				logDebugMessage(
						"The Progress bar is still not displayed after the initial click of the Next button. Will retry to click Next button.");
				while (!isProgBarDisp && counter < maxRetryCounter) {
					clickElementAction(buttons.next);
					logDebugMessage(
							concatStrings("Clicked the Next button again. The current number of clicks is [",
									Integer.toString(counter + 1), "]"));
					pauseSeleniumExecution(300);
					isProgBarDisp = isElementExists(labels.nextProgBarList);
					counter++;
				}
			} else {
				logDebugMessage(
						"No need to click again the Next button because the initial click processed the request");
			}
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/** 
	 * Upload the global custom CSS
	 * */
	public void uploadMakePaymentCustomCss(AccessS3BucketWithVfs s3Access) {

		uploadCustomCss(s3Access, PORTAL_CUSTOM_CSS_DIR, "portal_config.css");
	}
	
	/**
	 * Upload Make Payment specific portal config json file
	 */
	public void uploadMakePaymentConfig(AccessS3BucketWithVfs s3Access, String directoryNum,
			String fileToUploadOrReplace) {

		uploadConfig(s3Access, MAKE_PAYMENT_PORTAL_CONFIGS_DIR, directoryNum, fileToUploadOrReplace);
	}
	
	/** 
	 * Upload Make Payment specific global language files
	 * */
	public void uploadMakePaymentGlobalLangFile(AccessS3BucketWithVfs s3Access, String dirNum,
			String s3FileNameToReplaceOrUpload) {

		uploadGlobalLangFile(s3Access, MAKE_PAYMENT_PORTAL_GLOBAL_LANG_FILES_DIR, PortalNamesEnum.MakePayment, dirNum,
				s3FileNameToReplaceOrUpload);
	}
		
	
}
