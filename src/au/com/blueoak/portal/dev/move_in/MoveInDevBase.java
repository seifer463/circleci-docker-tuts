package au.com.blueoak.portal.dev.move_in;

import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;

import au.com.blueoak.portal.dev.DevBaseTesting;
import au.com.blueoak.portal.pageObjects.move_in.AcceptanceMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ThirdPartyPrefillMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class MoveInDevBase extends DevBaseTesting {
	
	/** This is where the test data are located */
	private static final String PROP_TEST_DATA = "TestDataDevMoveIn.properties";
	
	/** This is where we would save the test data */
	private static final String PROP_SAVED_TEST_DATA = "log/SavedTestDataDevMoveIn.properties";
	
	private static Properties prop = new Properties();
	
	/** 
	 * Load the property details
	 *  */
	static {

		try {
			// let's initialize the property file before running any tests
			InputStream inputData = new FileInputStream(PROP_TEST_DATA);
			prop = new Properties();
			prop.load(inputData);
			
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
	/** 
	 * Constructor when initializing this class
	 * */
	public MoveInDevBase() {
		
		super.setupTestProp();
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
	 * Use this to clear and hide the selenium upload fields
	 * for Life Support uploading
	 * */
	protected void clearLifeSupUploadFiles(int numOfFilesToRemove) {

		int lengthFile = numOfFilesToRemove;
		int uploadID1 = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-electricity",
					Integer.toString(uploadID1), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-electricity" + uploadID1
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(500);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-electricity" + uploadID1 + "' and @type='file']")).clear();
			// send the file location in the field
			logDebugMessage(concatStrings("Cleared the input field 'bbprtl-file-upload-electricity",
					Integer.toString(uploadID1), "'"));
			uploadID1++;
		}

		// hide those fields again
		int uploadID2 = 1;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to hide the displayed upload 'bbprtl-file-upload-electricity",
					Integer.toString(uploadID2), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-electricity" + uploadID2
					+ "').setAttribute('style', 'display:none');");
			pauseSeleniumExecution(500);
			logDebugMessage(concatStrings("Hidden the input field 'bbprtl-file-upload-electricity",
					Integer.toString(uploadID2), "'"));
			uploadID2++;
		}

		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-electricity').setAttribute('style','display:none');");
		logDebugMessage(concatStrings("Hidden the button 'bbprtl-file-upload-button-electricity"));
	}
	
	/** 
	 * Use this to clear and hide the selenium upload fields
	 * for Trade Waste uploading
	 * */
	protected void clearTradeWasteUploadFiles(int numOfFilesToRemove) {

		int lengthFile = numOfFilesToRemove;
		int uploadID1 = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-trade-waste",
					Integer.toString(uploadID1), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-trade-waste" + uploadID1
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(500);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-trade-waste" + uploadID1 + "' and @type='file']")).clear();
			// send the file location in the field
			logDebugMessage(concatStrings("Cleared the input field 'bbprtl-file-upload-trade-waste",
					Integer.toString(uploadID1), "'"));
			uploadID1++;
		}

		// hide those fields again
		int uploadID2 = 1;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to hide the displayed upload 'bbprtl-file-upload-trade-waste",
					Integer.toString(uploadID2), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-trade-waste" + uploadID2
					+ "').setAttribute('style', 'display:none');");
			pauseSeleniumExecution(500);
			logDebugMessage(concatStrings("Hidden the input field 'bbprtl-file-upload-trade-waste",
					Integer.toString(uploadID2), "'"));
			uploadID2++;
		}

		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-trade-waste').setAttribute('style','display:none');");
		logDebugMessage(concatStrings("Hidden the button 'bbprtl-file-upload-button-electricity"));
	}
	
	/** 
	 * Use this to clear and hide the selenium upload fields
	 * for Concession uploading
	 * */
	protected void clearConcessionUploadFiles(int numOfFilesToRemove) {

		int lengthFile = numOfFilesToRemove;
		int uploadID1 = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-concession",
					Integer.toString(uploadID1), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-concession" + uploadID1
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(500);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-concession" + uploadID1 + "' and @type='file']")).clear();
			// send the file location in the field
			logDebugMessage(concatStrings("Cleared the input field 'bbprtl-file-upload-concession",
					Integer.toString(uploadID1), "'"));
			uploadID1++;
		}

		// hide those fields again
		int uploadID2 = 1;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to hide the displayed upload 'bbprtl-file-upload-concession",
					Integer.toString(uploadID2), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-concession" + uploadID2
					+ "').setAttribute('style', 'display:none');");
			pauseSeleniumExecution(500);
			logDebugMessage(concatStrings("Hidden the input field 'bbprtl-file-upload-concession",
					Integer.toString(uploadID2), "'"));
			uploadID2++;
		}

		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-concession').setAttribute('style','display:none');");
		logDebugMessage(concatStrings("Hidden the button 'bbprtl-file-upload-button-electricity"));
	}
	
	/**
	 * Use this to construct the URL prefill to be used in populating the data in
	 * the portal
	 */
	protected String constructUrlPrefill(PortalTypesEnum portalTypeEnum, String... prefillData) {

		logDebugMessage("Will start constructing the URL for the prefill");
		String urlPrefill = null;

		String portalType = portalTypeEnum.name();
		String portalTypeUrl = null;
		switch (portalType) {
		case "STANDALONE":
			portalTypeUrl = concatStrings(getStandaloneUrlMoveIn(), "?");
			break;

		case "EMBEDDED":
			portalTypeUrl = concatStrings(getEmbeddedUrlMoveIn(), "move-in?");
			break;
		}

		String combinedData;
		List<String> listOfStrings = Arrays.asList(prefillData);
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : listOfStrings) {
			stringBuilder.append(string);
		}
		combinedData = stringBuilder.toString();

		urlPrefill = concatStrings(portalTypeUrl, combinedData);
		logDebugMessage("Finished constructing the URL for the prefill");
		
		return urlPrefill;
	}
	
	/** 
	 * Use this to delete the file specified from the file
	 * (e.g. The one showing the Trash bin icon)
	 * 
	 * Or 
	 * 
	 * Use this to delete the file specified from the file
	 * (e.g. The one showing the X icon)
	 * 
	 * */
	protected void deleteOrRemoveUploadFiles(List<WebElement> elementFiles, String xpathLoc, String fileName) {
		
		logDebugMessage(concatStrings("We would be deleting/removing the file '", fileName, "'"));
		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}
		WebElement deleteBtn = foundElement.findElement(By.xpath(xpathLoc));
		clickElementAction(deleteBtn);
		pauseSeleniumExecution(500);
		// click yes on the pop up
		WebElement yesBtn = driver.findElement(By.xpath("//*[starts-with(@id,'mat-dialog-')]/confirm-dialog/div[2]/button[1]"));
		clickElementAction(yesBtn);
	}
	
	/** 
	 * Delete the global language files being used throughout the testing
	 * to ensure that it would not be cached by cloudfront
	 * */
	protected void deleteMoveInGlobalLangFiles(AccessS3BucketWithVfs s3Access) {

		deleteGlobalLangFiles(s3Access, PortalNamesEnum.MoveIn, "FIL.json", "fil.json", "en-in.json");
	}

	/**
	 * Delete specific global language files
	 */
	protected void deleteMoveInGlobalLangFiles(AccessS3BucketWithVfs s3Access, String... langFilesToDelete) {

		deleteGlobalLangFiles(s3Access, PortalNamesEnum.MoveIn, langFilesToDelete);
	}

	/**
	 * Delete specific custom language files
	 */
	protected void deleteMoveInCustomLangFiles(AccessS3BucketWithVfs s3Access, String... langFilesToDelete) {

		deleteCustomLangFiles(s3Access, PortalNamesEnum.MoveIn, langFilesToDelete);
	}
	
	/** 
	 * 
	 * */
	protected WebElement getUploadedElementFileName(List<WebElement> elementFiles, String fileName) {

		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}

		foundElement = foundElement.findElement(By.xpath("./span[@class='e-file-container']/span[1]"));
		return foundElement;
	}
	
	/** 
	 * 
	 * */
	protected WebElement getUploadedElementFileType(List<WebElement> elementFiles, String fileName) {

		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}

		foundElement = foundElement.findElement(By.xpath("./span[@class='e-file-container']/span[2]"));
		return foundElement;
	}
	
	/** 
	 * 
	 * */
	protected WebElement getUploadedElementFileSize(List<WebElement> elementFiles, String fileName) {

		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}

		foundElement = foundElement.findElement(By.xpath("./span[@class='e-file-container']/span[3]"));
		return foundElement;
	}
	
	/** 
	 * 
	 * */
	protected WebElement getUploadedElementFileStatus(List<WebElement> elementFiles, String fileName) {

		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}

		foundElement = foundElement.findElement(By.xpath("./span[@class='e-file-container']/span[4]"));
		return foundElement;
	}
	
	/** 
	 * 
	 * */
	protected WebElement getUploadedElementFileIcon(List<WebElement> elementFiles, String fileName) {

		List<WebElement> files = elementFiles;
		WebElement foundElement = null;
		for (WebElement webElement : files) {
			String fileToRemove = StringUtils.normalizeSpace(webElement.getAttribute("data-file-name")).toLowerCase();
			String fileNameToDeleteFinal = StringUtils.normalizeSpace(fileName);
			if (fileNameToDeleteFinal.toLowerCase().equals(fileToRemove)) {
				foundElement = webElement;
				break;
			}
		}

		foundElement = foundElement.findElement(By.xpath("./span[starts-with(@class,'e-icons')]"));
		return foundElement;
	}
	
	/**
	 * Get the value of the property
	 */
	protected String getProp(String key) {

		String value = prop.getProperty(key);
		logDebugMessage(concatStrings("The value to be returned by getProp(String) using the key (", key, ") is '",
				value, "'"));
		return value;
	}
	
	/** 
	 * Use this when the progress bar is displayed and waiting for it to disappear.
	 * 
	 * Can be used when adding Credit Card Details for Direct Debit in Move In portal
	 * 
	 * Will return a TimeoutException if progress bar did not appear or disappear
	 * with the PROGRESS_BAR_TIMEOUT config.
	 * */
	protected void moveInDirectDebitCCProgBarLoad() {

		FluentWait<WebDriver> wait_1 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_APPEAR_TIMEOUT))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

		logDebugMessage(
				concatStrings("Will be waiting for the Move In Direct Debit Credit Card progress bar to appear within <",
						Integer.toString(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_APPEAR_TIMEOUT), "> seconds"));

		boolean isProgBarDisp = false;
		try {
			isProgBarDisp = wait_1.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Checking if the Move In Direct Debit Credit Card progress bar appeared");
					boolean stillChecking = true;
					boolean finishedChecking = false;
					int counter = 0;
					int maxRetry = 10;
					while (stillChecking && !finishedChecking && counter < maxRetry) {
						try {
							DirectDebitMoveIn ddMoveIn = new DirectDebitMoveIn(driver, 0);
							if (isElementExists(ddMoveIn.progressBarList)) {
								logDebugMessage("The Move In Direct Debit Credit Card progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Move In Direct Debit Credit Card progress bar is not yet displayed");
								stillChecking = true;
								finishedChecking = false;
							}
							pauseSeleniumExecution(200);
							counter++;
						} catch (StaleElementReferenceException sere) {
							logDebugMessage(
									"StaleElementReferenceException encountered while waiting for the Move In Direct Debit Credit Card progress bar to display. Will reinitialize the page object then look for it again and confirm if it's displayed or not");
							DirectDebitMoveIn ddMoveIn = new DirectDebitMoveIn(driver, 0);
							if (isElementExists(ddMoveIn.progressBarList)) {
								logDebugMessage("The Move In Direct Debit Credit Card progress bar is now displayed");
								return true;
							} else {
								logDebugMessage("The Move In Direct Debit Credit Card progress bar is not yet displayed");
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
					Integer.toString(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_APPEAR_TIMEOUT),
					"> seconds for the Move In Direct Debit Credit Card bar to appear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		DirectDebitMoveIn directDebit = new DirectDebitMoveIn(driver, 0);
		if (isElementExists(directDebit.readOnlyCreditCardNameList)) {
			logDebugMessage(
					"We can now see the Name on Credit Card read only, the Direct Debit Progress bar might have been missed");
			boolean isDisp = isElementExists(directDebit.progressBarList);
			logDebugMessage(concatStrings("The Direct Debit loading is ",
					(isDisp ? "still displayed" : "no longer displayed")));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else if (isElementExists(directDebit.lblCreditCardDeclarationList)) {
			logDebugMessage(
					"We can now see the MW payframe loaded, the Direct Debit Progress bar might have been missed");
			boolean isDisp = isElementExists(directDebit.progressBarList);
			logDebugMessage(concatStrings("The Direct Debit loading is ",
					(isDisp ? "still displayed" : "no longer displayed")));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isProgBarDisp, "The Move In Direct Debit Credit Card progress bar did not display!");
		}
		
		if (isProgBarDisp) {
			logDebugMessage(
					concatStrings("Move In Direct Debit Credit Card progress bar is displayed, will now be waiting within <",
							Integer.toString(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
							"> seconds for it to disappear"));

			FluentWait<WebDriver> wait_2 = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_DISAPPEAR_TIMEOUT))
					.pollingEvery(Duration.ofMillis(500))
					.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

			boolean progBarDisappeared = false;
			try {
				progBarDisappeared = wait_2.until(new Function<WebDriver, Boolean>() {
					public Boolean apply(WebDriver driver) {
						logDebugMessage("Checking if the Move In Direct Debit Credit Card progress bar disappeared");
						boolean stillDisplayed = true;
						boolean finishedChecking = false;
						int counter = 0;
						int maxRetry = 10;
						while (stillDisplayed && !finishedChecking && counter < maxRetry) {
							try {
								DirectDebitMoveIn ddMoveIn = new DirectDebitMoveIn(driver, 0);
								if (isElementExists(ddMoveIn.progressBarList)) {
									logDebugMessage("Move In Direct Debit Credit Card progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Move In Direct Debit Credit Card progress bar is no longer displayed");
									return true;
								}
								pauseSeleniumExecution(1000);
								counter++;
							} catch (StaleElementReferenceException sere) {
								logDebugMessage(
										"StaleElementReferenceException encountered while waiting for the Move In Direct Debit Credit Card progress bar to disappear, will reinitialize the ProgressBar page object");
								DirectDebitMoveIn ddMoveIn = new DirectDebitMoveIn(driver, 0);
								if (isElementExists(ddMoveIn.progressBarList)) {
									logDebugMessage("Move In Direct Debit Credit Card progress bar is still displayed");
									stillDisplayed = true;
									finishedChecking = false;
								} else {
									logDebugMessage("Move In Direct Debit Credit Card progress bar is no longer displayed");
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
						Integer.toString(MOVE_IN_PORTAL_DIRECT_DEBIT_CC_PROGRESS_BAR_DISAPPEAR_TIMEOUT),
						"> seconds for the Move In Direct Debit Credit Card progress bar to disappear"));
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}

			assertTrue(progBarDisappeared, "The Move In Direct Debit Credit Card progress bar is still displayed! -");
		}
	}
	
	/**
	 * Use this to scroll the page up for Move In form
	 * 
	 */
	protected void scrollPageUp(int yPixels) {
		
		if (getPortalType().equals("standalone")) {
			// standalone forms need more pixels
			int standAlonePixels = yPixels + 150;
			pageScrollUp(standAlonePixels);
		} else if (getPortalType().equals("embedded")) {
			// we need to switch out first from the move in iframe
			// before we can scroll up
			switchToDefaultContent();
			// scroll up the page
			pageScrollUp(yPixels);
			// go back to the move in iframe
			switchToMoveInEmbeddedIframe(1);
		}
	}
	
	/**
	 * Use this to scroll the page down for Move In form
	 * 
	 */
	protected void scrollPageDown(int yPixels) {
		
		if (getPortalType().equals("standalone")) {
			// standalone forms need more pixels
			int standAlonePixels = yPixels + 150;
			pageScrollDown(standAlonePixels);
		} else if (getPortalType().equals("embedded")) {
			// we need to switch out first from the move in iframe
			// before we can scroll up
			switchToDefaultContent();
			// scroll up the page
			pageScrollDown(yPixels);
			// go back to the move in iframe
			switchToMoveInEmbeddedIframe(1);
		}
	}

	/**
	 * Use this to populate the 3rd party prefill for Move In
	 * 
	 * @param moveInDate          should be in the format MM/DD/YYYY (e.g.
	 *                            12/25/2000)
	 * @param waitForPortalToLoad pass true if you want to wait until the portal is
	 *                            loaded
	 * 
	 */
	protected void populate3rdPartyPrefill(String streetNum, String streetName, StreetTypesEnum stTypeEnum,
			String suburb, AustralianStatesEnum stateEnum, String postCode, AccountTypesEnum acctTypeEnum,
			AccountCategoryEnum acctCategoryEnum, String moveInDate, boolean waitForPortalToLoad) {

		logDebugMessage("Will start populating the Dev 3rd Party Prefill page");
		ThirdPartyPrefillMoveIn thirdpartyprefillmovein = new ThirdPartyPrefillMoveIn(driver);
		
		waitUntilElementIsDisplayed(thirdpartyprefillmovein.supplyCompleteAdd, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		thirdpartyprefillmovein.supplyCompleteAdd.clear();
		
		if (streetNum == null) {
			thirdpartyprefillmovein.supplyStreetNum.clear();
		} else {
			thirdpartyprefillmovein.supplyStreetNum.clear();
			thirdpartyprefillmovein.supplyStreetNum.sendKeys(streetNum);
		}

		if (streetName == null) {
			thirdpartyprefillmovein.supplyStreetName.clear();
		} else {
			thirdpartyprefillmovein.supplyStreetName.clear();
			thirdpartyprefillmovein.supplyStreetName.sendKeys(streetName);
		}
		
		if (stTypeEnum == null) {
			thirdpartyprefillmovein.supplyStreetTypeShort.clear();
			thirdpartyprefillmovein.supplyStreetTypeLong.clear();
		} else {
			thirdpartyprefillmovein.supplyStreetTypeShort.clear();
			thirdpartyprefillmovein.supplyStreetTypeShort.sendKeys(stTypeEnum.name());

			thirdpartyprefillmovein.supplyStreetTypeLong.clear();
			thirdpartyprefillmovein.supplyStreetTypeLong.sendKeys(stTypeEnum.getLabelText());
		}

		if (suburb == null) {
			thirdpartyprefillmovein.supplyCitySuburb.clear();
		} else {
			thirdpartyprefillmovein.supplyCitySuburb.clear();
			thirdpartyprefillmovein.supplyCitySuburb.sendKeys(suburb);
		}

		if (stateEnum == null) {
			thirdpartyprefillmovein.supplyStateShort.clear();
			thirdpartyprefillmovein.supplyStateLong.clear();
		} else {
			thirdpartyprefillmovein.supplyStateShort.clear();
			thirdpartyprefillmovein.supplyStateShort.sendKeys(stateEnum.name());

			thirdpartyprefillmovein.supplyStateLong.clear();
			thirdpartyprefillmovein.supplyStateLong.sendKeys(stateEnum.getLabelText());
		}

		if (postCode == null) {
			thirdpartyprefillmovein.supplyPostcode.clear();
		} else {
			thirdpartyprefillmovein.supplyPostcode.clear();
			thirdpartyprefillmovein.supplyPostcode.sendKeys(postCode);
		}
		
		if (acctTypeEnum != null) {
			String accountType = acctTypeEnum.getLabelText();
			switch (accountType) {
			case "Residential":
				clickElementAction(thirdpartyprefillmovein.accountResidential);
				break;

			case "Small Business":
				clickElementAction(thirdpartyprefillmovein.accountCommercial);
				break;
			}
		}

		if (acctCategoryEnum != null) {
			String accountCategory = acctCategoryEnum.name();
			switch (accountCategory) {
			case "TENANT":
				clickElementAction(thirdpartyprefillmovein.supplyTenant);
				break;

			case "OWNER":
				clickElementAction(thirdpartyprefillmovein.supplyOwner);
				break;

			case "RUM":
				clickElementAction(thirdpartyprefillmovein.supplyPropMan);
				break;
			}
		}
		
		if (moveInDate == null) {
			thirdpartyprefillmovein.supplyMoveInDate.clear();
		} else {
			thirdpartyprefillmovein.supplyMoveInDate.clear();
			thirdpartyprefillmovein.supplyMoveInDate.sendKeys(moveInDate);
		}

		clickElementAction(thirdpartyprefillmovein.submit);
		pauseSeleniumExecution(3000);
		
		if (waitForPortalToLoad) {
			loadEmbeddedMoveInPortal(false, false);
		}
		
		logDebugMessage("Finished populating the Dev 3rd Party Prefill page");
	}
	
	/** 
	 * Did this because there was an issue where initial click
	 * on the submit button did not work.
	 * 
	 * But we would just try again once after the initial click.
	 * */
	protected void retryClickSubmit(int implicitWait) {

		logDebugMessage("Checking if we need to click the Submit button again");
		try {
			AcceptanceMoveIn acceptancemovein = new AcceptanceMoveIn(driver, implicitWait);
			if (isElementExists(acceptancemovein.submittingMessageList)) {
				logDebugMessage("No need to click the Submit button again");
			} else {
				logDebugMessage("Initial click on the Submit button did not work, will click again.");
				clickElementAction(acceptancemovein.submit);
			}
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/** 
	 * Use this to read a Global Lang file
	 * */
	protected String readMoveInGlobalLangFile(AccessS3BucketWithVfs s3Access, String fileToRead) {

		String file = s3Access.readGlobalLangFileFromS3(getCloudFrontOriginBucketName(),
				PortalNamesEnum.MoveIn.getLabelText(), fileToRead);
		return file;
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
	 * Use this to upload files on the Life Support for Electricity
	 * */
	protected void uploadLifeSupMedCoolingFiles(String filesLocation, String... filesToUpload) {

		int lengthFile = filesToUpload.length;
		int uploadID = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-electricity",
					Integer.toString(uploadID), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-electricity" + uploadID
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(1000);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-electricity" + uploadID + "' and @type='file']"))
					.sendKeys(filesLocation + filesToUpload[i]);
			// send the file location in the field
			logDebugMessage(concatStrings("Pasted the file to upload '", filesLocation) + filesToUpload[i]);
			uploadID++;
		}

		logDebugMessage("Going to display the hidden upload button 'bbprtl-file-upload-button-electricity'");
		// display the hidden field for submitting the upload field
		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-electricity').setAttribute('style','display:block');");

		// submit the uploaded file
		driver.findElement(By.xpath("//input[@id='bbprtl-file-upload-button-electricity']")).click();
		logDebugMessage("Clicked the hidden upload button to upload the files in the Life Support and Medical Cooling");
	}
	
	/**
	 * Use this to upload files on the Trade Waste section
	 */
	protected void uploadTradeWasteFiles(String filesLocation, String... filesToUpload) {

		int lengthFile = filesToUpload.length;
		int uploadID = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-trade-waste",
					Integer.toString(uploadID), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-trade-waste" + uploadID
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(1000);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-trade-waste" + uploadID + "' and @type='file']"))
					.sendKeys(filesLocation + filesToUpload[i]);
			// send the file location in the field
			logDebugMessage(concatStrings("Pasted the file to upload '", filesLocation) + filesToUpload[i]);
			uploadID++;
		}

		logDebugMessage("Going to display the hidden upload button 'bbprtl-file-upload-button-trade-waste'");
		// display the hidden field for submitting the upload field
		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-trade-waste').setAttribute('style','display:block');");

		// submit the uploaded file
		driver.findElement(By.xpath("//input[@id='bbprtl-file-upload-button-trade-waste']")).click();
		logDebugMessage("Clicked the hidden upload button to upload the files in the Trade Waste");
	}
	
	/**
	 * Use this to upload files on the Concession section
	 */
	protected void uploadConcessionFiles(String filesLocation, String... filesToUpload) {

		int lengthFile = filesToUpload.length;
		int uploadID = 1;

		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < lengthFile; i++) {

			logDebugMessage(concatStrings("Going to display the hidden upload 'bbprtl-file-upload-concession",
					Integer.toString(uploadID), "'"));
			// display the hidden field for upload field in loop
			js.executeScript("document.getElementById('bbprtl-file-upload-concession" + uploadID
					+ "').setAttribute('style', 'display:block');");
			pauseSeleniumExecution(1000);
			driver.findElement(
					By.xpath("//input[@id='bbprtl-file-upload-concession" + uploadID + "' and @type='file']"))
					.sendKeys(filesLocation + filesToUpload[i]);
			// send the file location in the field
			logDebugMessage(concatStrings("Pasted the file to upload '", filesLocation) + filesToUpload[i]);
			uploadID++;
		}

		logDebugMessage("Going to display the hidden upload button 'bbprtl-file-upload-button-concession'");
		// display the hidden field for submitting the upload field
		js.executeScript(
				"document.getElementById('bbprtl-file-upload-button-concession').setAttribute('style','display:block');");

		// submit the uploaded file
		driver.findElement(By.xpath("//input[@id='bbprtl-file-upload-button-concession']")).click();
		logDebugMessage("Clicked the hidden upload button to upload the files in the Concession");
	}
	
	/**
	 * This would create an invalidation request in the specified distributionID in
	 * the cloudfront only for move in files.
	 * 
	 * You can have more information on how to specify the path:
	 * https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/Invalidation.html
	 * 
	 */
	protected void invalidateMoveInGlobalLangFiles(AccessS3BucketWithVfs s3Access) {

//		String path = concatStrings("/*/", PortalNamesEnum.MoveIn.getLabelText(), "/assets", S3_PORTAL_LANG_FILES_DIR,
//				"*");
//		String path = concatStrings("/*/assets", S3_PORTAL_LANG_FILES_DIR, "*");
		invalidateCloudfront(getRunInvalidation(), s3Access, true);
	}
	
	/** 
	 * Upload the global custom CSS
	 * */
	public void uploadMoveInCustomCss(AccessS3BucketWithVfs s3Access) {

		uploadCustomCss(s3Access, PORTAL_CUSTOM_CSS_DIR, "portal_config.css");
	}
	
	/** 
	 * Upload Move In specific custom language files
	 * */
	public void uploadMoveInCustomLangFile(AccessS3BucketWithVfs s3Access, String dirNum,
			String s3FileNameToReplaceOrUpload) {

		uploadCustomLangFile(s3Access, MOVE_IN_PORTAL_CUSTOM_LANG_FILES_DIR, PortalNamesEnum.MoveIn, dirNum,
				s3FileNameToReplaceOrUpload);
	}
	
	/**
	 * Upload Move In specific portal config json file
	 */
	public void uploadMoveInConfig(AccessS3BucketWithVfs s3Access, String directoryNum,
			String fileToUploadOrReplace) {

		uploadConfig(s3Access, MOVE_IN_PORTAL_CONFIGS_DIR, directoryNum, fileToUploadOrReplace);
	}
	
	/** 
	 * Upload Move In specific global language files
	 * */
	public void uploadMoveInGlobalLangFile(AccessS3BucketWithVfs s3Access, String dirNum,
			String s3FileNameToReplaceOrUpload) {

		uploadGlobalLangFile(s3Access, MOVE_IN_PORTAL_GLOBAL_LANG_FILES_DIR, PortalNamesEnum.MoveIn, dirNum,
				s3FileNameToReplaceOrUpload);
	}
	
	
}