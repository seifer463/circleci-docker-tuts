package au.com.blueoak.portal.dev.make_payment.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.make_payment.MakePaymentDevBase;
import au.com.blueoak.portal.pageObjects.make_payment.Buttons;
import au.com.blueoak.portal.pageObjects.make_payment.CssStyling;
import au.com.blueoak.portal.pageObjects.make_payment.Header;
import au.com.blueoak.portal.pageObjects.make_payment.InputFields;
import au.com.blueoak.portal.pageObjects.make_payment.Labels;
import au.com.blueoak.portal.pageObjects.make_payment.ProgressBar;
import au.com.blueoak.portal.pageObjects.make_payment.ToastMsg;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class AllFieldsMakePayment extends MakePaymentDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	InputFields inputfields;
	Buttons buttons;
	ToastMsg toastmsg;
	CssStyling cssstyling;
	Header header;
	Labels labels;
	ProgressBar progressbar;
	AccessS3BucketWithVfs s3Access;
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/** 
	 * This will check if we already logged into the CRM
	 * */
	private boolean isCrmLoggedIn = false;
	
	/** 
	 * 
	 * */
	private String getMerchantId(int id) throws SQLException {
		
		String query = concatStrings("SELECT `merchant_id` FROM `bbeng_billers_banking` WHERE `id` = ", Integer.toString(id), ";");
		String result = executeQuery(query);
		return result;
	}
	
	/** 
	 * 
	 * */
	private void updateMerchantId(String newVal, int id) throws SQLException {
		
		if (newVal == null) {
			String query = concatStrings("UPDATE `bbeng_billers_banking` SET `merchant_id` = NULL WHERE `id` = ", Integer.toString(id), ";");
			executeUpdate(query);
		} else {
			String query = concatStrings("UPDATE `bbeng_billers_banking` SET `merchant_id` = '", newVal, "' WHERE `id` = ", Integer.toString(id), ";");
			executeUpdate(query);
		}
	}
	
	/** 
	 * Get a column value from bbeng_banking_records in the given accountNum
	 * */
	private String getBankingRecordVal(String columnName, String accountNum) throws SQLException {
		
		String query = concatStrings("SELECT ", columnName,
				" FROM `bbeng_banking_records` WHERE customer_account_number='", accountNum, "' ORDER BY id DESC;");
		logDebugMessage(concatStrings("Will be running this query in the DB -> ", query));
		String result = executeQuery(query);
		return result;
	}
	
	@BeforeClass
	public void beforeClass() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);
		
		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		
		// upload the portal_config.css we are using
		uploadMakePaymentCustomCss(s3Access);
		
		// upload the portal_config.json we are using
		uploadMakePaymentConfig(s3Access, "01\\", "portal_config.json");
		
		// let's access the portal we are testing with
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			loadMakePayment();
		}
	}
	
	@AfterClass
	public void afterClass() {
		
		deleteMakePaymentGlobalLangFiles(s3Access);
		
		saveProp();
		logTestClassEnd(className);
	}
	
	@BeforeMethod
	public void beforeMethod() {
		
		// let's initialize the page objects
		inputfields = new InputFields(driver);
		buttons = new Buttons(driver);
		toastmsg = new ToastMsg(driver);
		cssstyling = new CssStyling(driver);
		header = new Header(driver);
		labels = new Labels(driver);
		progressbar = new ProgressBar(driver);
	}
	
	/** 
	 * For ticket BBPRTL-141
	 * 
	 * Let's verify the required fields error
	 * by just clicking on the field by field
	 * and entering invalid values
	 * */
	@Test(priority = 1)
	public void verifyRequiredFields_01() {
		
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		
		clickElementAction(inputfields.accountNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		// let's switch in the mwframe
		switchToMWIframe();
		clickElementAction(inputfields.cardName);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.cardNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.cardExpiry);
		clickElementAction(inputfields.cardCvv);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(inputfields.paymentAmount);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.emailReceipt);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		
		// we will just click the Make Payment
		// but it should not submit
		clickElementAction(buttons.makePayment);
		
		// let's verify the expected fields that should be in error
		// and not in error
		softAssertion.assertTrue(isElementInError(inputfields.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// let's switch in the mwframe
		switchToMWIframe();
		softAssertion.assertTrue(isElementInError(inputfields.cardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardCvv, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(inputfields.paymentAmount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.emailReceipt, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		inputfields.accountNumber.sendKeys(getProp("test_data_01"));
		// let's switch in the mwframe
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_40"));
		// card number is invalid
		slowSendKeys(inputfields.cardNumber, getProp("test_data_03"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_04"));
		inputfields.cardCvv.sendKeys(getProp("test_data_05"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_06"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_07"));
		
		// we will just click the Make Payment
		// but it should not submit
		clickElementAction(buttons.makePayment);
		
		// let's verify the expected fields that should be in error
		// and not in error
		softAssertion.assertTrue(isElementInError(inputfields.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// let's switch in the mwframe
		switchToMWIframe();
		softAssertion.assertTrue(isElementInError(inputfields.cardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardCvv, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(inputfields.paymentAmount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.emailReceipt, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		softAssertion.assertTrue(isMakePaymentBtnEnabled(),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		inputfields.paymentAmount.sendKeys("999.9999", Keys.TAB);
		// verify the Large Amount tickbox is not displayed
		buttons = new Buttons(driver, 0);
		softAssertion.assertFalse(isElementExists(buttons.largeAmtCheckboxList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		inputfields.paymentAmount.sendKeys("1000");
		// verify we can see the Large Amount tickbox displayed
		buttons = new Buttons(driver, 1);
		softAssertion.assertTrue(isElementExists(buttons.largeAmtCheckboxList),
				assertionErrorMsg(getLineNumber()));
		// verify not ticked by default
		softAssertion.assertFalse(isElementTicked(buttons.largeAmtCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(buttons.largeAmtCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the CSS and lang files
		softAssertion.assertEquals(getDisplayedText(labels.largeAmount, true), "Large payment amount detected, tick to confirm the amount is correct.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.largeAmount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.largeAmtCheckboxOuter),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.largeAmtCheckboxInner),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(buttons.makePayment);
		// verify the checkbox in error state
		softAssertion.assertTrue(isElementInError(buttons.largeAmtCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.largeAmtCheckboxOuter),
				CHECKBOX_OUTER_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.largeAmtCheckboxInner),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/**
	 * For ticket BBPRTL-141
	 * 
	 * Here we will verify that:
	 * 
	 * - currency and currency significant digits are according to the portal_config
	 * - if we are entering payment amount, currency is not displayed
	 * - if we input the currency, it will be ignored
	 * - decimal places should be displayed when updating the value
	 * - that if users put a decimal place and there's already in place, it would just combine the decimal values
	 * (e.g. current value is 10711.8500 then users input 10711.85.265)
	 * - that if users cannot put a separator
	 * - that if users input a currency that's not the one specified in the portal_config,
	 * it would use the one in specified in the portal_config
	 * - verify the standard email validations
	 * - verify that if Email a payment receipt is unticked, the email address field is hidden
	 * - verify that email address field only allows 255 characters and can be separated by comma and semi colon
	 * 
	 *  */
	@Test(priority = 2)
	public void verifyRequiredFields_02() {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// let's verify the currency symbol
		// and currency significant digits
		// according to the portal_config
		String propPayAmount = getProp("test_data_10");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		// click outside to trigger a change
		clickElementAction(inputfields.emailReceipt);
		String payAmountActual = inputfields.paymentAmount.getAttribute("value");
		int propPayAmountInt = Integer.parseInt(propPayAmount);
		DecimalFormat df = new DecimalFormat(getProp("currency_thousand_separator"));
		String convertedRaw = df.format(propPayAmountInt);
		String converted = addMissingZeroes(convertedRaw, super.getProp("currency_significant_digits"));
		String currency_symbol = getProp("currency_symbol");
		String payAmountExpected = currency_symbol + converted;
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// let's clear the value then input a payment amount
		// with a currency symbol and decimals bigger than the
		// specified currency significant digits in the portal_config
		// clear() function does not work, so will use backspace instead
		inputfields.paymentAmount.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		propPayAmount = getProp("test_data_11");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		// click outside to trigger a change
		clickElementAction(inputfields.emailReceipt);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
		float propPayAmountFloat = Float.parseFloat(propPayAmount);
		df = new DecimalFormat();
		String maxFracDig = getProp("currency_significant_digits");
		int maxFracDigInt = Integer.parseInt(maxFracDig);
		df.setMaximumFractionDigits(maxFracDigInt);
		df.setRoundingMode(RoundingMode.UP);
		converted = df.format(propPayAmountFloat);
		payAmountExpected = currency_symbol + converted;
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// we will update the value the payment amount and will input the
		// currency symbol as well
		// clear first the current value
		inputfields.paymentAmount.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		propPayAmount = getProp("test_data_12");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		// click outside to trigger a change
		clickElementAction(inputfields.emailReceipt);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
		// let's remove the currency symbol from the test data
		propPayAmount = propPayAmount.substring(1);
		propPayAmountFloat = Float.parseFloat(propPayAmount);
		df = new DecimalFormat();
		convertedRaw = df.format(propPayAmountFloat);
		converted = addMissingZeroes(convertedRaw, super.getProp("currency_significant_digits"));
		payAmountExpected = currency_symbol + converted;
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// verify that currency is not displayed when you click on the field
		// then the decimal places are also displayed accordingly
		clickElementAction(inputfields.paymentAmount);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
		payAmountExpected = addMissingZeroes(propPayAmount, super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// this scenario 'verify that if users input another decimal, it would just combine the values'
		// is no longer applicable since now it's being cleared instead of combining
		// we will update the assertion
		clickElementAction(inputfields.paymentAmount);
		inputfields.paymentAmount.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
		propPayAmount = getProp("test_data_13");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		// click outside to trigger a change
		clickElementAction(inputfields.emailReceipt);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
//		String prop12And13Amount = getProp("test_data_12").substring(1) + getProp("test_data_13").substring(1);
//		propPayAmountFloat = Float.parseFloat(prop12And13Amount);
//		df = new DecimalFormat();
//		df.setMaximumFractionDigits(maxFracDigInt);
//		df.setRoundingMode(RoundingMode.UP);
//		converted = df.format(propPayAmountFloat);
//		payAmountExpected = currency_symbol + converted;
//		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		verifyStringIsBlank(payAmountActual);
		
		// verify that users cannot put thousand separator
		// we will update the value the payment amount and will input the
		// currency symbol as well
		// clear first the current value
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		propPayAmount = getProp("test_data_14");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
		// let's remove the last character from the data
		payAmountExpected = propPayAmount.substring(0, propPayAmount.length() - 1);
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);

		// that if users input a currency that's not the one specified in the portal_config,
		// it would use the one in specified in the portal_config
		// clear the current value first
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		propPayAmount = getProp("test_data_15");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		// click outside to trigger a change
		clickElementAction(inputfields.emailReceipt);
		payAmountActual = inputfields.paymentAmount.getAttribute("value");
		// let's remove the currency symbol from the test data
		// then add the expected currency symbol
		payAmountExpected = currency_symbol + propPayAmount.substring(1);
		payAmountExpected = addMissingZeroes(payAmountExpected, super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// verify the standard email validations
		String propEmail = getProp("test_data_16");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		String emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_17");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_18");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_19");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_20");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_21");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: " + propEmail, true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_22");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: tralala@testing,@testing", true);
		
		// let's clear the values
		inputfields.emailReceipt.clear();
		propEmail = getProp("test_data_189");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		emailError = getDisplayedText(labels.hintEmailAdd, true);
		// verify the special characters did not got trimmed
		verifyTwoStringsAreEqual(emailError, "These email addresses are invalid: ~!@#$%^&*()_+|`-=\\{}[]:\",'<>?,./", true);
		
		// verify that if Email a payment receipt is unticked, 
		// the email address field is hidden
		assertTrue(isElementTicked(buttons.emailCheckbox, 0), "The Email a payment receipt checkbox is not ticked!");
		// untick the button
		clickElementAction(buttons.emailCheckbox);
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.emailReceiptList), "The email address field is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// verify that email address field only allows 255 characters
		// and can be separated by comma and semi-colon
		// tick back the email address
		clickElementAction(buttons.emailCheckbox);
		assertTrue(isElementDisplayed(inputfields.emailReceipt, 0), "The email address field is not displayed!");
		propEmail = getProp("test_data_23");
		inputfields.emailReceipt.sendKeys(propEmail);
		// click the Payment Amount to trigger a validation
		clickElementAction(inputfields.paymentAmount);
		String emailField = getDisplayedValue(inputfields.emailReceipt, false);
		// verify the email address did not enter additional email once character limit is reached
		softAssertion.assertEquals(emailField,
				"email1.!#@testing.com,emai$%l.two@testing.com;email&three@testing.com;email'4@testing.com;email5*+-/@testing.com,email=?^6@testing.com,_email7`@testing.com;email{8}@testing.com,email9@testing.com,email10@testing.com,email11@testing.com;email12@testing.com",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/**
	 * This is for ticket BBPRTL-144
	 * 
	 * - verify that we can see the toast message and the timeout_warning is ticking every second
	 * - verify that the toast will disappear when we hover the mouse
	 * - verify toast will disappear once you click on the message.
	 * - verify that toast will not disappear if you are in the MWPayframe iframe
	 * 
	 *  */
	@Test(priority = 3)
	public void verifyTimeout() {

		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();

		inputfields.accountNumber.sendKeys(getProp("test_data_08"));
		// let's wait for the timeout session to appear
		// the value in the portal_config is set to 120 seconds
		// and a timeout_warning of 30 seconds
		// so we will wait for the toast element to appear
		// added padding of 6 seconds
		boolean isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's get the toast message
			String toastMsg = getDisplayedText(toastmsg.toastLoc, true);
			// sometimes the actual seconds vs the expected seconds does not match
			// resulting for the test case to fail sometimes
            // so will use contains for now without asserting the seconds countdown
			verifyStringContains(true, toastMsg, "You are currently inactive! This window will automatically refresh in ");
			// let's pause again for the timer
			pauseSeleniumExecution(1000);
			int counter = 16;
			int maxCounter = 27;
			while (counter < maxCounter) {
				String assertMsgExp = "You are currently inactive! This window will automatically refresh in ";
				// let's instantiate the class to get again the elements
				toastmsg = new ToastMsg(driver, 0);
				toastMsg = getDisplayedText(toastmsg.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				verifyStringContains(true, toastMsg, assertMsgExp);
				// let's pause for the timer
				pauseSeleniumExecution(1000);
				counter++;
			}
		} else {
			fail("The Toast Timeout message was not displayed");
		}
		// we will just pause until the toast message disappears
		// and reloads the page
		pauseSeleniumExecution(20000);
		toastmsg = new ToastMsg(driver, 0);
		boolean isToastDisplayed = isElementExists(toastmsg.toastLocList);
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		assertFalse(isToastDisplayed, "The Toast Message is displayed!");
		// let's verify that the account number is cleared
		// since the page was reloaded
		loadMakePayment();
		String accountNum = inputfields.accountNumber.getAttribute("value");
		verifyStringIsBlank(accountNum);

		// let's verify that the toast will disappear once we hover the mouse
		clickElementAction(inputfields.accountNumber);
		// as implemented in ticket BBPRTL-746
		// only if the make payment has data for any of the following:
		// - Account Number
		// - Card Number
		// - Email Address
		// will the page inactivity timeout appear
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify toast message did not display
		assertFalse(isElementDisp, "The Toast Message is displayed!");
		// put value in the Email Address to verify
		// that the toast message would appear
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			hoverToElementAction(inputfields.paymentAmount);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			boolean isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isToastDisp, "The Toast Message is displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		// let's verify that the toast message will disappear once you click
		// on that toast message
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			clickElementAction(toastmsg.toastLoc);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			boolean isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isToastDisp, "The Toast Message is displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		inputfields.emailReceipt.sendKeys(getProp("test_data_58"), ", ");
		// let's verify that the toast will disappear once you start typing
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's put the email
			inputfields.emailReceipt.sendKeys(getProp("test_data_24"));
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			boolean isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isToastDisp, "The Toast Message is displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		// let's verify that the toast will not disappear if you just hover
		// or type in the MWPayframe iframe fields
		// let's switch in the mwframe
		switchToMWIframe();
		clickElementAction(inputfields.cardName);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			switchToMWIframe();
			clickElementAction(inputfields.cardNumber);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			if (getPortalType().equals("standalone")) {
				// let's switch out of the mwframe
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			boolean isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isToastDisp, "The Toast Message is not displayed!");
			
			switchToMWIframe();
			inputfields.cardCvv.sendKeys(getProp("test_data_25"));
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			if (getPortalType().equals("standalone")) {
				// let's switch out of the mwframe
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isToastDisp, "The Toast Message is not displayed!");

			switchToMWIframe();
			inputfields.cardName.sendKeys(getProp("test_data_26"));
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			if (getPortalType().equals("standalone")) {
				// let's switch out of the mwframe
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isToastDisp, "The Toast Message is not displayed!");

			switchToMWIframe();
			hoverToElementAction(inputfields.cardExpiry);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsg = new ToastMsg(driver, 0);
			if (getPortalType().equals("standalone")) {
				// let's switch out of the mwframe
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			isToastDisp = isElementExists(toastmsg.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertTrue(isToastDisp, "The Toast Message is not displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		clickElementAction(buttons.makePayment);
		// let's just put a pause because selenium
		// to make sure that the toast message disappeared
		// before looking it up again
		pauseSeleniumExecution(1000);
		toastmsg = new ToastMsg(driver, 0);
		boolean isDisp = isElementExists(toastmsg.toastLocList);
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		assertFalse(isDisp, "The Toast Message is displayed!");
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS labels for the placeholders
	 * - verify the CSS styling for the underlines when there's no input
	 * 
	 * */
	@Test(priority = 4)
	public void verifyCssOnLoad() {

		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(inputfields.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// let's switch in the mwframe
		switchToMWIframe();
		softAssertion.assertFalse(isElementInError(inputfields.cardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(inputfields.cardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(inputfields.cardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(inputfields.cardCvv, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertFalse(isElementInError(inputfields.paymentAmount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(inputfields.emailReceipt, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify ticked by default
		softAssertion.assertTrue(isElementTicked(buttons.emailCheckbox, 0),
				assertionErrorMsg(getLineNumber()));

		// verify the labels and CSS
		if (getPortalType().equals("standalone")) {
			softAssertion.assertEquals(getDisplayedText(header.head1, true),
					"Pay my Test & `BlueBilling's - (dev.portal) [,:;\"]! Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(header.head1), MAIN_HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBoxShadowProp(cssstyling.boxShadow), BOX_SHADOW_BORDER_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			setImplicitWait(0);
			softAssertion.assertFalse(isElementDisplayed(header.head1, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedText(header.head1, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBoxShadowProp(cssstyling.boxShadow), "none",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		softAssertion.assertEquals(getCssColorProp(buttons.helpIcon), HELP_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.creditCardDeclaration, true),
				"Payment Sucharge Fees: A credit/debit card surcharge of 1.5% (inc. GST) applies for Visa and Mastercard, 3% surcharge (inc GST) applies for AMEX and Diners Card. Please check our Terms and Conditions the debit payment will be based on the bills issued according to each bill cycle",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclaration), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.email, true), "Email a payment receipt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.email), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.emailCheckboxOuter),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.emailCheckboxInner), CHECKBOX_INNER_TICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclarationLink), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));

		hoverToElementAction(labels.creditCardDeclarationLink);
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclarationLink), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		
		// verify the color for the placeholders
		// verify the color of the underlines
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		switchToMWIframe();
		softAssertion.assertEquals(getLabelCss(labels.cardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertEquals(getPlaceholderCss(labels.labelInput, "Payment Amount"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Email Address (separate multiple emails with a comma)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(buttons.makePayment, true), "Make Payment",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.makePayment), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS styling for the underlines when there's a valid input
	 * - verify the the css styling for the placeholder when there's a valid input
	 * - verify the text color of the input text when it's a valid input
	 * 
	 * */
	@Test(priority = 5)
	public void verifyValidInput() {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// input valid values
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_26"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_43"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_24"), Keys.TAB);
		waitForCssToRender();
		
		// verify the CSS and displayed labels
		// verify the fix for bug ticket BBPRTL-1555
		softAssertion.assertEquals(getDisplayedText(labels.floaterAcccountNum, true),
				"Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterAcccountNum), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.accountNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		switchToMWIframe();
		softAssertion.assertEquals(getDisplayedText(labels.cardName, true), "Name on Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardName), FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardName), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardNumber, true), "Card Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardNumber), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardExpiry, true), "Expiry (MM/YY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardExpiry), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardCvv, true), "CVV",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardCvv), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertEquals(getDisplayedText(labels.floaterPaymentAmt, true), "Payment Amount",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterPaymentAmt), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.paymentAmount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the processing fee CSS
		softAssertion.assertEquals(getProcessingFeeCss(cssstyling.processingFee), PROCESSING_FEE_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.payProcessingFee), PROCESSING_FEE_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.floaterEmailAdd, true),
				"Email Address (separate multiple emails with a comma)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterEmailAdd), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.emailReceipt), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the Total Amount
		softAssertion.assertEquals(getLabelCss(labels.payTotal), PAY_TOTAL_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the Make Payment button
		softAssertion.assertEquals(getDisplayedText(buttons.makePayment, true), "Make Payment",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.makePayment), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS styling for the underlines when there's an invalid input
	 * - verify the the css styling for the placeholder there's an invalid input
	 * - verify the text color of the input text when it's an invalid input
	 * 
	 * */
	@Test(priority = 6)
	public void verifyInvalidInput() {
		
		// let's clear the data entered in the previous test case
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// input invalid values
		inputfields.accountNumber.sendKeys(getProp("test_data_01"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_37"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_03"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_04"));
		inputfields.cardCvv.sendKeys(getProp("test_data_05"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_06"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_07"));
		// just clicked on the make payment to trigger validation
		clickElementAction(buttons.makePayment);
		
		// verify the fix for ticket BBPRTL-2106
		softAssertion.assertEquals(getDisplayedText(labels.floaterAcccountNum, true),
				"Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterAcccountNum), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.accountNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintAccountNumber, true), "Account number is invalid",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		switchToMWIframe();
		softAssertion.assertEquals(getDisplayedText(labels.cardName, true), "Name on Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardName), FLOATER_LABEL_MW_FRAME_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardName), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardName, true), "Name is invalid",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardNumber, true), "Card Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), FLOATER_LABEL_MW_FRAME_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardNumber), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardNumber, true), "Card number is invalid",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardExpiry, true), "Expiry (MM/YY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), FLOATER_LABEL_MW_FRAME_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardExpiry), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardExpiry, true), "Card expiry is invalid",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.cardCvv, true), "CVV",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), FLOATER_LABEL_MW_FRAME_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardCvv), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardCvv, true), "CVV invalid",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardCvv), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertEquals(getDisplayedText(labels.floaterPaymentAmt, true), "Payment Amount",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterPaymentAmt), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.paymentAmount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		String minimumAmount = getProp("currency_minimum_amount");
		int propPayAmountInt = Integer.parseInt(minimumAmount);
		DecimalFormat df = new DecimalFormat(getProp("currency_thousand_separator"));
		String convertedRaw = df.format(propPayAmountInt);
		String converted = addMissingZeroes(convertedRaw, super.getProp("currency_significant_digits"));
		String currency_symbol = getProp("currency_symbol");
		String payAmountExpected = currency_symbol + converted;
		softAssertion.assertEquals(getDisplayedText(labels.hintPaymentAmt, true),
				concatStrings("Minimum of ", payAmountExpected, " required!"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintPaymentAmt), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.floaterEmailAdd, true),
				"Email Address (separate multiple emails with a comma)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterEmailAdd), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.emailReceipt), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintEmailAdd, true),
				concatStrings("These email addresses are invalid: ", getProp("test_data_07")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintEmailAdd), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS styling for the underlines when there's an error
	 * about required field
	 * 
	 * */
	@Test(priority = 7)
	public void verifyUlineRequiredInput() {
		
		// let's clear the data entered in the previous test case
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// click on button to trigger validation
		clickElementAction(buttons.makePayment);
		waitForCssToRender();
		
		// verify the fields in error state
		softAssertion.assertTrue(isElementInError(inputfields.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintAccountNumber, true), "Account number is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		switchToMWIframe();
		// verify the fix for bug ticket BBPRTL-2107
		softAssertion.assertTrue(isElementInError(inputfields.cardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardCvv, 2, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		// because of bug ticket BBPRTL-2003, the placeholder color does not change
		// to the error color, so we assert the current behavior for now
		softAssertion.assertEquals(getLabelCss(labels.cardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(inputfields.paymentAmount, 5, 0),
				"Payment Amount is not in error state");
		softAssertion.assertEquals(getPlaceholderCss(labels.labelInput, "Payment Amount"), PLACEHOLDER_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		String minimumAmount = getProp("currency_minimum_amount");
		int propPayAmountInt = Integer.parseInt(minimumAmount);
		DecimalFormat df = new DecimalFormat(getProp("currency_thousand_separator"));
		String convertedRaw = df.format(propPayAmountInt);
		String converted = addMissingZeroes(convertedRaw, super.getProp("currency_significant_digits"));
		String currency_symbol = getProp("currency_symbol");
		String payAmountExpected = currency_symbol + converted;
		softAssertion.assertEquals(getDisplayedText(labels.hintPaymentAmt, true),
				concatStrings("Minimum of ", payAmountExpected, " required!"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintPaymentAmt), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.emailReceipt, 5, 0),
				"Email Address is not in error state");
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Email Address (separate multiple emails with a comma)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintEmailAdd, true), "Email address is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintEmailAdd), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's click on all fields
		clickElementAction(inputfields.accountNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		switchToMWIframe();
		clickElementAction(inputfields.cardName);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.cardNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.cardExpiry);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.cardCvv);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(inputfields.paymentAmount);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.emailReceipt);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		// click on button to trigger validation
		clickElementAction(buttons.makePayment);
		waitForCssToRender();
		
		softAssertion.assertTrue(isElementInError(inputfields.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintAccountNumber, true), "Account number is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		switchToMWIframe();
		// verify the fields in error state
		softAssertion.assertTrue(isElementInError(inputfields.cardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.cardCvv, 2, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the MW payframe fields are now in error state since we clicked those fields
		softAssertion.assertEquals(getLabelCss(labels.cardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardName, true), "Card holder name is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardNumber, true), "Card number is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardExpiry, true), "Card expiry is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintCardCvv, true), "CVV required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintCardCvv), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(inputfields.paymentAmount, 5, 0),
				"Payment Amount is not in error state");
		softAssertion.assertEquals(getPlaceholderCss(labels.labelInput, "Payment Amount"), PLACEHOLDER_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		minimumAmount = getProp("currency_minimum_amount");
		propPayAmountInt = Integer.parseInt(minimumAmount);
		df = new DecimalFormat(getProp("currency_thousand_separator"));
		convertedRaw = df.format(propPayAmountInt);
		converted = addMissingZeroes(convertedRaw, super.getProp("currency_significant_digits"));
		currency_symbol = getProp("currency_symbol");
		payAmountExpected = currency_symbol + converted;
		softAssertion.assertEquals(getDisplayedText(labels.hintPaymentAmt, true),
				concatStrings("Minimum of ", payAmountExpected, " required!"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintPaymentAmt), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(inputfields.emailReceipt, 5, 0),
				"Email Address is not in error state");
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Email Address (separate multiple emails with a comma)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.hintEmailAdd, true), "Email address is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.hintEmailAdd), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the placeholder when input is focused
	 * - verify the underline when input is focused
	 * - verify the text color when text is entered
	 * (without triggering the validation e.g. hitting on next field)
	 * 
	 * */
	@Test(priority = 8)
	public void verifyInputFocused() {
		
		// let's clear the data entered in the previous test case
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify the placeholder, underline and text color for the account number
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.floaterAcccountNum, true),
				"Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterAcccountNum), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.accountNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		switchToMWIframe();
		// verify the placeholder, underline and text color for the card name		
		inputfields.cardName.sendKeys(getProp("test_data_26"));
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.cardName, true), "Name on Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardName), FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardName), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardName),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the placeholder, underline and text color for the card number	
		// since the MW payframe immediately shifts to the next
		// field when input is valid, we need to go back to the card number
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		clickElementAction(inputfields.cardExpiry);
		clickElementAction(inputfields.cardNumber);
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.cardNumber, true), "Card Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardNumber), FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardNumber), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardNumber),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the placeholder, underline and text color for the card expiry
		// since the MW payframe immediately shifts to the next
		// field when input is valid, we need to go back to the card expiry
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		clickElementAction(inputfields.cardCvv);
		clickElementAction(inputfields.cardExpiry);
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.cardExpiry, true), "Expiry (MM/YY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardExpiry), FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardExpiry), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardExpiry),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the placeholder, underline and text color for the card cvv		
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.cardCvv, true), "CVV",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.cardCvv), FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.cardCvv), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(inputfields.cardCvv),
				UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		
		// verify the placeholder, underline and text color for the payment amount
		inputfields.paymentAmount.sendKeys(getProp("test_data_43"));
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.floaterPaymentAmt, true), "Payment Amount",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterPaymentAmt), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.paymentAmount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlinePayAmt), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the placeholder, underline and text color for the email receipt
		inputfields.emailReceipt.sendKeys(getProp("test_data_24"));
		// just added a pause to fix issue where color is not correctly captured
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(labels.floaterEmailAdd, true),
				"Email Address (separate multiple emails with a comma)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.floaterEmailAdd), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(inputfields.emailReceipt), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineEmailReceipt), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the css for the buttons
	 * - verify the css for the help page
	 * - verify the css for the processing fee
	 * - verify the css for the total amount
	 * - verify the css for the successful payment
	 * - verify the css for the declined payment
	 * 
	 * */
	@Test(priority = 9)
	public void verifyLabels() {
		
		// let's clear the data entered in the previous test case
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify the labels and CSS
		if (getPortalType().equals("standalone")) {
			softAssertion.assertEquals(getDisplayedText(header.head1, true),
					"Pay my Test & `BlueBilling's - (dev.portal) [,:;\"]! Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(header.head1), MAIN_HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			setImplicitWait(0);
			softAssertion.assertFalse(isElementDisplayed(header.head1, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedText(header.head1, false)),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		softAssertion.assertEquals(getCssColorProp(buttons.helpIcon), HELP_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.creditCardDeclaration, true),
				"Payment Sucharge Fees: A credit/debit card surcharge of 1.5% (inc. GST) applies for Visa and Mastercard, 3% surcharge (inc GST) applies for AMEX and Diners Card. Please check our Terms and Conditions the debit payment will be based on the bills issued according to each bill cycle",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclaration), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(labels.email, true), "Email a payment receipt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.email), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(buttons.emailCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.emailCheckboxOuter),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.emailCheckboxInner), CHECKBOX_INNER_TICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclarationLink), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));

		hoverToElementAction(labels.creditCardDeclarationLink);
		softAssertion.assertEquals(getLabelCss(labels.creditCardDeclarationLink), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the help page text color and buttons
		clickElementAction(buttons.helpIcon);
		boolean isDisp = waitForElement(labels.imgAcctNum, 5, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isDisp) {
			String imgLoc = labels.imgAcctNum.getAttribute("src");
			softAssertion.assertEquals(getDisplayedText(labels.acctNumSample, true),
					"Your account number can be found at the marked position on your bill",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(imgLoc.contains(S3_PORTAL_CONFIG_BUCKET_NAME),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(imgLoc.contains("bill_account_number.svg"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(labels.acctNumSample), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getButtonCss(buttons.helpIconClose), ACCT_NUM_CLOSE_BUTTON_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			
			clickElementAction(buttons.helpIconClose);
			pauseSeleniumExecution(2000);
		} else {
			fail("The Sample Account number svg image was not displayed");
		}
		
		// let's populate the fields account number and card number 
		// to get the processing fee
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		switchToMWIframe();
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_43"));
		// click on the email to trigger validation
		clickElementAction(inputfields.emailReceipt);
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = concatStrings(getProp("currency_symbol"),
				addMissingZeroes(getProp("test_data_44"), super.getProp("currency_significant_digits")));
		String totalAmt = concatStrings(getProp("currency_symbol"),
				addMissingZeroes(getProp("test_data_45"), super.getProp("currency_significant_digits")));
		softAssertion
				.assertEquals(payProcFee,
						concatStrings("Processing fee of ", procFeeAmt, " is applicable. A total of ", totalAmt,
								" will be processed"),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getProcessingFeeCss(cssstyling.processingFee), PROCESSING_FEE_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.payProcessingFee), PROCESSING_FEE_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// enter in a valid email address
		inputfields.emailReceipt.sendKeys(getProp("test_data_24"), Keys.TAB);
		String total = getDisplayedText(labels.payTotal, true);
		waitForCssToRender();
		// verify the Total Amount
		softAssertion.assertEquals(total, concatStrings("TOTAL ", totalAmt),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.payTotal), PAY_TOTAL_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the Make Payment button
		softAssertion.assertEquals(getDisplayedText(buttons.makePayment, true), "Make Payment",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.makePayment), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));		
		// verify all assertions
		softAssertion.assertAll();
		
		// let's input the missing fields to make a successful payment
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_26"));
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(buttons.makePayment);
		waitForCssToRender();
		assertEquals(getButtonCss(buttons.makePayment), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the text we are displaying
		assertEquals(getDisplayedText(labels.paymentProgBar, true), "Processing payment...",
				assertionErrorMsg(getLineNumber()));
		assertEquals(getLabelCss(labels.paymentProgBar), PROCESSING_REQUEST_MSG_CSTM,
				assertionErrorMsg(getLineNumber()));
		assertEquals(getCssStrokeProp(cssstyling.spinner), SPINNER_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		String progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG_MP;
		String progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG_MP;
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
		String remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
		softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
				"The initial progress bar color is not correct");
		softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
				"The remaining progress bar color is not correct");
		// verify all assertions
		softAssertion.assertAll();
		
		makePaymentBtnProgBarLoad();
		String resultIcon = cssstyling.responseIcon.getAttribute("src");
		softAssertion.assertTrue(resultIcon.contains("svg/successfull.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.responseHeader), RESPONSE_HEADER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.emailSentTo), EMAIL_SENT_TO_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		buttons = new Buttons(driver, 0);
		softAssertion.assertFalse(isElementExists(buttons.tryAgainList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// let's refresh the page
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// let's input all fields to process declined payment
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		// get inside the mypayframe
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_26"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_185"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_46"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_24"), Keys.TAB);
		pauseSeleniumExecution(500);
		clickElementAction(buttons.makePayment);
		waitForCssToRender();
		assertEquals(getButtonCss(buttons.makePayment), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the text we are displaying
		assertEquals(getDisplayedText(labels.paymentProgBar, true), "Processing payment...",
				assertionErrorMsg(getLineNumber()));
		assertEquals(getLabelCss(labels.paymentProgBar), PROCESSING_REQUEST_MSG_CSTM,
				assertionErrorMsg(getLineNumber()));
		assertEquals(getCssStrokeProp(cssstyling.spinner), SPINNER_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG_MP;
		progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG_MP;
		js = (JavascriptExecutor) driver;
		initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
		remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
		softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
				"The initial progress bar color is not correct");
		softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
				"The remaining progress bar color is not correct");
		// verify all assertions
		softAssertion.assertAll();
		
		makePaymentBtnProgBarLoad();
		resultIcon = cssstyling.responseIcon.getAttribute("src");
		softAssertion.assertTrue(resultIcon.contains("svg/unsuccessfull.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.responseHeader), RESPONSE_HEADER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.declinedMwResp), RESPONSE_MW_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.emailSentTo), EMAIL_SENT_TO_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.tryAgain), TRY_AGAIN_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the background color
	 * - verify the box shadow
	 * - verify the footer background
	 * - verify the icon business name link
	 * - verify the support text
	 * 
	 * */
	@Test(priority = 10)
	public void verifyFooter() {
		
		if (getPortalType().equals("standalone")) {
			logDebugMessage("We need to run the verifyFooter() since we are on the standalone portal type");
			// let's just refresh the page to ensure we have a clean portal
			refreshBrowser(1, 5000);
			loadMakePayment();
			
			// initialize the Soft Assert
			SoftAssert softAssertion = new SoftAssert();
			
			softAssertion.assertEquals(getCssBackgrndColorProp(cssstyling.bodyBackGrnd), BODY_BACKGROUND_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(cssstyling.footer), FOOTER_BACKGROUND_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(labels.clientLink.getAttribute("href"), "https://blueoak.com.au/",
					assertionErrorMsg(getLineNumber()));
			String imgSrc = cssstyling.clientImg.getAttribute("src");
			softAssertion.assertTrue(imgSrc.contains("logo_company.png"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(cssstyling.footer, true),
					"Need Help? Call us on 1234 567 890, or email us at support.test@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(labels.footerHelp), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(labels.footerHelpNumber), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(labels.footerHelpEmail), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			
			hoverToElementAction(labels.footerHelpNumber);
			softAssertion.assertEquals(getLabelCss(labels.footerHelpNumber), LINK_LABEL_HOVER_CSTM,
					assertionErrorMsg(getLineNumber()));
			
			hoverToElementAction(labels.footerHelpEmail);
			softAssertion.assertEquals(getLabelCss(labels.footerHelpEmail), LINK_LABEL_HOVER_CSTM,
					assertionErrorMsg(getLineNumber()));
						
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded")) {
			logDebugMessage("Don't need to run the verifyFooter() test case since we are on the embedded portal type");
		}
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * For ticket BBPRTL-131
	 * 
	 * - process a successful payment with cvv 000
	 * - verify the payment processing fee will show since
	 * bbeng_billers_banking_charges.apply_at = ON_SUBMIT
	 * - verify the transaction is recorded in the corresponding account in CRM
	 * - verify the processing fee in the email notification
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 11)
	public void verifySuccessfulPayment_01() throws SQLException {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_50"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_26"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_49"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_51"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_52"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_53"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_50"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_50"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_52"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_51"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_53"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_53"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_50"), super.getProp("mc_pay_method"), super.getProp("test_data_53"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_50"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_50"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_50"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_50"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_50"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_50"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_50"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "17.00", false);
		verifyTwoStringsAreEqual(feeAmount, "0.23", false);
		verifyTwoStringsAreEqual(feeTax, "0.02", false);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_MC", false);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - submit payment and verify no processing fee should be applied
	 * - verify no processing fee is displayed in the email notification
	 * - verify that the To: in the email contains to recipients
	 * - verify the transaction is recorded in the corresponding account in CRM
	 * 
	 * @throws SQLException 
	 * */
	@Test(priority = 12)
	public void verifySuccessfulPayment_02() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_54"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_55"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_37"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_38"));
		inputfields.cardCvv.sendKeys(getProp("test_data_39"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_56"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_57"), Keys.TAB);
		// verify that email is not in error state
		assertFalse(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is in error state");
		// clear the invalid email
		clickElementAction(inputfields.emailReceipt);
		deleteAllTextFromField();
		// input valid email
		inputfields.emailReceipt.sendKeys(getProp("test_data_160"));
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_56"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				concatStrings("Payment receipt has been emailed to ", getProp("test_data_59")), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_54"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_54"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 1);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 0, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 6);
		String crmTotal = "$" + super.getProp("test_data_56");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 7, crmTotal);
		crmTotal = "$-" + super.getProp("test_data_56");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, crmTotal);
		
		// remove the .00 from the data
		String payAmt = getStringUntil(super.getProp("test_data_56"), ".");
		payAmt = normalizeSpaces(payAmt);
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_54"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_54"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_54"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_54"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_54"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_54"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_54"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_54"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "18.00", false);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_VI", false);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - verify that the processing fee will not be displayed
	 * because it's on ON_SUCCESS
	 * - verify two recipients is displayed in the email To:
	 * - verify that no processing fee is displayed in the email notification
	 * - verify the transaction is recorded in the corresponding account in CRM
	 * 
	 * @throws SQLException 
	 * */
	@Test(priority = 13)
	public void verifySuccessfulPayment_03() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_60"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_61"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_40"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_41"));
		inputfields.cardCvv.sendKeys(getProp("test_data_42"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_62"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_63"), Keys.TAB);
		// verify that email is not in error state
		assertFalse(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is in error state");
		// clear the invalid email
		clickElementAction(inputfields.emailReceipt);
		deleteAllTextFromField();
		// input valid email
		inputfields.emailReceipt.sendKeys(getProp("test_data_161"));
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_62"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				concatStrings("Payment receipt has been emailed to ", getProp("test_data_64")), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// we will just pause because there was instance where the transaction
		// records were not yet created, so we pause for 30 seconds
		pauseSeleniumExecution(30000);
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_60"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_60"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Amex surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_65"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_66"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_62"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_62"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(), super.getProp("test_data_60"),
				super.getProp("amex_pay_method"), super.getProp("test_data_62"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_60"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_60"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_60"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_60"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_60"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_60"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_60"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "19.00", false);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_AX", false);
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * 
	 * - we will enter an email address then will tick off the checkbox for email
	 * then ensure no email was sent
	 * - processing fee will be involved
	 * - updated the payment amount then verify that the calculation is changed accordingly
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 14)
	public void verifySuccessfulPayment_04() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_67"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_68"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_40"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_41"));
		inputfields.cardCvv.sendKeys(getProp("test_data_42"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_69"));
		// processing fee should be displayed immediately after entering the payment amount
		// but just put a pause to ensure it's displayed before we look for the element
		pauseSeleniumExecution(500);
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_70"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_71"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		// test the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		inputfields.emailReceipt.sendKeys(getProp("test_data_64"));
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// let's tick off the Email a payment receipt
		clickElementAction(buttons.emailCheckbox);
		// verify email is hidden
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify that the make payment is still enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// verify the processing fee text again
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_70"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_71"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// here we will update the payment amount and verify that the calculation changed
		// use backspace to clear the values since clear() does not work
		inputfields.paymentAmount.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		inputfields.paymentAmount.sendKeys(super.getProp("test_data_76"));
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_77"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_78"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		// verify the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		// verify that the email section is not displayed
		boolean isDisp;
		try {
			setImplicitWait(1);
			isDisp = labels.emailSentTo.isDisplayed();
		} catch (NoSuchElementException nsee) {
			isDisp = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		assertFalse(isDisp, "The successful email sent text is displayed");
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_67"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_67"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card surcharge [Biller 2] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_77"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_76"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_78"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_78"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_67"), super.getProp("amex_pay_method"), super.getProp("test_data_78"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_67"), super.getProp("amex_pay_method"), super.getProp("test_data_78"));
		verifyStringIsBlank(receiptAdd);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_67"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_67"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_67"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_67"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_67"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_67"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_67"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "21.00", false);
		verifyTwoStringsAreEqual(feeAmount, "0.28", false);
		verifyTwoStringsAreEqual(feeTax, "0.03", false);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_AX", false);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - after entering the payment amount, we will tick off the checkbox for email
	 * then ensure no email was sent
	 * - no processing fee will be involved
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 15)
	public void verifySuccessfulPayment_05() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_72"));
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_73"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_40"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_41"));
		inputfields.cardCvv.sendKeys(getProp("test_data_42"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_74"));
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// let's tick off the Email a payment receipt
		clickElementAction(buttons.emailCheckbox);
		// verify email is hidden
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// verify we can see the Large Amount tickbox displayed
		buttons = new Buttons(driver, 0);
		softAssertion.assertTrue(isElementExists(buttons.largeAmtCheckboxList),
				assertionErrorMsg(getLineNumber()));
		// verify not ticked by default
		softAssertion.assertFalse(isElementTicked(buttons.largeAmtCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(buttons.largeAmtCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the CSS and lang files
		softAssertion.assertEquals(getDisplayedText(labels.largeAmount, true), "Large payment amount detected, tick to confirm the amount is correct.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(labels.largeAmount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.largeAmtCheckboxOuter),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.largeAmtCheckboxInner),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's tick the large amount checkbox
		clickElementAction(buttons.largeAmtCheckbox);
		softAssertion.assertTrue(isElementTicked(buttons.largeAmtCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the make payment is enabled
		softAssertion.assertTrue(isMakePaymentBtnEnabled(),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(cssstyling.largeAmtCheckboxOuter),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(cssstyling.largeAmtCheckboxInner),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_74"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		// verify that the email section is not displayed
		boolean isDisp;
		try {
			setImplicitWait(1);
			isDisp = labels.emailSentTo.isDisplayed();
		} catch (NoSuchElementException nsee) {
			isDisp = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		assertFalse(isDisp, "The successful email sent text is displayed");
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_72"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_72"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 4);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the first row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 0, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_74"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_74"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, crmTotal);
		// verify the second row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, "07/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 2, "000000219-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 5, "21/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 6, "$8,266.08");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, "reference");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_72"), super.getProp("amex_pay_method"), super.getProp("test_data_75"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_72"), super.getProp("amex_pay_method"), super.getProp("test_data_75"));
		verifyStringIsBlank(receiptAdd);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_72"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_72"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_72"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_72"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_72"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_72"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_72"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "1000.00", false);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_AX", false);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - verify that we will still be able to process payment
	 * even if initially the users did not complete the MW fields
	 * (e.g. Missing Card Name and Incorrect CVV)
	 * - then we will update the account number to something else but same biller
	 * with the former
	 * - verify the email sent
	 * - verify that if the same email address, the api would only send it once
	 * per unique recipient
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 16)
	public void verifySuccessfulPayment_06() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_79"));
		switchToMWIframe();
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_42"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_80"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_83"));
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_81"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_82"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		pauseSeleniumExecution(3000);
		// let's verify that card name and cvv is in error state
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is not error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// let's click again the make payment
		clickElementAction(buttons.makePayment);
		pauseSeleniumExecution(3000);
		// let's verify again that card name and cvv is in error state
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is not error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_81"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_82"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's switch in the mwframe
		switchToMWIframe();
		// lets input the card name
		inputfields.cardName.sendKeys(super.getProp("test_data_84"));
		// let's clear the cvv and enter correct one
		inputfields.cardCvv.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE);
		inputfields.cardCvv.sendKeys(super.getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// lets update the account number
		// but clear first using backspace since clear() is not working
		inputfields.accountNumber.sendKeys(Keys.END, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		inputfields.accountNumber.sendKeys(super.getProp("test_data_85"), Keys.TAB);
		// put a pause here because initially the Total displayed is TOTAL €21.6700
		// however after a few milli seconds/seconds it changes to the correct one which is TOTAL €22.0000
		pauseSeleniumExecution(2000);
		// verify the processing fee text still the same since the new account
		// falls into the same biller
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_81"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_82"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// verify that the account number is correct
		String acctNum = inputfields.accountNumber.getAttribute("value");
		verifyTwoStringsAreEqual(acctNum, super.getProp("test_data_85"), true);
		// verify the fix for ticket BBPRTL-665
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_86"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_85"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_85"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_81"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_80"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_82"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_82"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_85"), super.getProp("mc_pay_method"), super.getProp("test_data_82"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receipt_add = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_85"), super.getProp("mc_pay_method"), super.getProp("test_data_82"));
		verifyTwoStringsAreEqual(receipt_add, super.getProp("test_data_86"), true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_85"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_85"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_85"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_85"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_85"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_85"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_85"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "22.00", false);
		verifyTwoStringsAreEqual(feeAmount, "0.30", false);
		verifyTwoStringsAreEqual(feeTax, "0.03", false);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_MC", false);
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * 
	 * - verify that we will still be able to process payment
	 * even if initially the users did not complete the MW fields
	 * (e.g. Missing CVV and Incorrect Expiry)
	 * - then we will update the account number to something else that has
	 * a different biller
	 * - then verify the email sent
	 * 
	 * */
	@Test(priority = 17)
	public void verifySuccessfulPayment_07() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_88"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_89"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_90"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_91"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_94"));
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_92"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_93"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		pauseSeleniumExecution(3000);
		// let's verify that card expiry and cvv is in error state
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		
		// let's update the expiry
		inputfields.cardExpiry.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE);
		inputfields.cardExpiry.sendKeys(super.getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(super.getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// lets update the account number
		clickElementAction(inputfields.accountNumber);
		deleteAllTextFromField();
		inputfields.accountNumber.sendKeys(super.getProp("test_data_95"), Keys.TAB);
		// put a pause to fix ElementClickInterceptedException
		pauseSeleniumExecution(1000);
		clickElementAction(inputfields.emailReceipt);
		inputfields.emailReceipt.sendKeys(Keys.TAB);
		// put a pause because there's a delay until the processing fee gets hidden
		pauseSeleniumExecution(500);
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		total = getDisplayedText(labels.payTotal, true);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_91"), super.getProp("currency_significant_digits"));
		// verify the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// verify the fix for ticket BBPRTL-665
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		// verify the fix for ticket BBPRTL-1152
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_95"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_95"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 2] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_96"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$" + super.getProp("test_data_97"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_91"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, "$" + super.getProp("test_data_98"));
		// verify the values created in the 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "14/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "PAY000001005");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Payment via BPAY");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 6);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 7, "$187.21");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$160.43");
		
		// let's run a query in the DB to check if the transaction was approved
		// remove the .0 from the data
		String payAmt = getStringUntil(super.getProp("test_data_91"), ".");
		payAmt = normalizeSpaces(payAmt);
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_95"), super.getProp("mc_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_95"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_95"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_95"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_95"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_95"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_95"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_95"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "23.00", false);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_MC", false);
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * 
	 * - verify that we will still be able to process payment
	 * even if initially the users did not complete the MW fields
	 * (e.g. Missing Card Name, Expiry, and CVV)
	 * - then we will update the card number to something else that has
	 * a different calculation
	 * - verify that every click on make payment with missing required MW
	 * fields would not create records in the DB
	 * - then verify the email sent
	 * 
	 * */
	@Test(priority = 18)
	public void verifySuccessfulPayment_08() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_99"));
		switchToMWIframe();
		slowSendKeys(inputfields.cardNumber, getProp("test_data_40"), true, 300);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_100"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String total = getDisplayedText(labels.payTotal, true);
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_100"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// let's click make payment
		clickElementAction(buttons.makePayment);
		pauseSeleniumExecution(3000);
		// let's verify that card name, card expiry and cvv is in error state
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Card Name is not error state");
		assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		// let's verify no records created in the DB
		assertEquals(super.getNumOfRecordsInDbPayProc(super.getProp("test_data_99")), 0, "The number of records created in the DB is not correct.");
		// enter the missing mw required fields
		inputfields.cardName.sendKeys(super.getProp("test_data_101"));
		// remove the current card number
		inputfields.cardNumber.clear();
		// let's verify that the field was cleared
		String cardNumVal = inputfields.cardNumber.getAttribute("value");
		verifyStringIsBlank(cardNumVal);
		slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		inputfields.cardExpiry.sendKeys(super.getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(super.getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_102"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_103"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		// verify the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// verify the fix for ticket BBPRTL-665
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_99"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_99"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_102"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_100"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_103"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_103"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_99"), super.getProp("mc_pay_method"), super.getProp("test_data_103"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_99"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_99"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_99"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_99"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_99"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_99"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_99"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "24.00", false);
		verifyTwoStringsAreEqual(feeAmount, "0.32", false);
		verifyTwoStringsAreEqual(feeTax, "0.03", false);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_MC", false);
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * 
	 * - we will process a payment with processing fee
	 * for a reference account
	 * - verify that once the Email checkbox is ticked, the email 
	 * address is removed
	 * 
	 * */
	@Test(priority = 19)
	public void verifySuccessfulPayment_09() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_104"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_105"));
		slowSendKeys(inputfields.cardNumber, className, true, 300);
		slowSendKeys(inputfields.cardNumber, getProp("test_data_37"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_38"));
		inputfields.cardCvv.sendKeys(getProp("test_data_39"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_106"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_24"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_107"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_108"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// untick the email a payment receipt
		clickElementAction(buttons.emailCheckbox);
		// verify the email address field is hidden
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.emailReceiptList), "The Email Address input field is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's tick the email a payment receipt again
		clickElementAction(buttons.emailCheckbox);
		// verify the email address field is now displayed
		assertTrue(isElementDisplayed(inputfields.emailReceipt, 0), "The Email Address input field is hidden");
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"), Keys.TAB);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_104"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_104"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Visa surcharge [Biller 3] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_107"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "reference");
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_108"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_108"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		// verify the values created in the 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "01/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "000000095-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 5, "15/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 6, "$443.93");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "reference");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_104"), super.getProp("visa_pay_method"), super.getProp("test_data_108"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_104"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_104"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_104"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_104"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_104"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_104"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_104"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "2", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "26.00", false);
		verifyTwoStringsAreEqual(feeAmount, "0.43", false);
		verifyTwoStringsAreEqual(feeTax, "0.04", false);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_VI", false);
	}
	
	/** 
	 * For ticket BBPRTL-143
	 * For ticket BBPRTL-142
	 * 
	 * - verify declined payments
	 * - verify declined payment does not create a record in the crm
	 * - verify an email is still sent to the user
	 * 
	 * */
	@Test(priority = 20)
	public void verifyDeclinedPayment_01() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_109"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_110"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_185"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_111"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_83"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_112"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_113"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// sometimes the initial click does not process the payment
		// so we keep on trying until the progress bar is displayed
		makePaymentBtnRetryClick(3);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment processing failed", true);
		verifyTwoStringsAreEqual(labels.declinedMwResp.getText(), "Transaction declined", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Failed payment details has been emailed to " + super.getProp("test_data_86"), true);
		// verify the Try Again button is displayed
		assertTrue(isElementDisplayed(buttons.tryAgain, 0), "Try Again button is not displayed");
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		String acctNumRemovedSpaces = StringUtils.deleteWhitespace(super.getProp("test_data_109"));
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_109"), true, 1, false, false);
		crmClickRecordExactLinkText(acctNumRemovedSpaces);
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records are created
		assertFalse(crmIsDataAvailableInTable(transactions), "Data is displayed in the transactions subpanel");
		
		// let's run a query in the DB to check if the transaction was declined
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(), acctNumRemovedSpaces,
				super.getProp("mc_pay_method"), super.getProp("test_data_113"));
		verifyTwoStringsAreEqual(respCode, "2", true);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - process a declined payment
	 * - no processing fee
	 * - no email that should be sent out
	 * 
	 * */
	@Test(priority = 21)
	public void verifyDeclinedPayment_02() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_114"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_115"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_186"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_38"));
		inputfields.cardCvv.sendKeys(getProp("test_data_39"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_116"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// untick the email checkbox
		clickElementAction(buttons.emailCheckbox);
		// verify that the email input field is not displayed
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.emailReceiptList), "The Email Receipt input field is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the Make Payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is not displayed");
		// verify the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_116"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment processing failed", true);
		verifyTwoStringsAreEqual(labels.declinedMwResp.getText(), "Transaction declined", true);
		// verify the Try Again button is displayed
		assertTrue(isElementDisplayed(buttons.tryAgain, 0), "Try Again button is not displayed");
		// verify that the email section is not displayed
		labels = new Labels(driver, 0);
		softAssertion.assertFalse(isElementExists(labels.emailSentToList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// click the Try Again
		clickElementAction(buttons.tryAgain);
		loadMakePayment();
		// verify we see the payment amount field
		assertTrue(isElementDisplayed(inputfields.paymentAmount, 0), "The Payment Amount field is not displayed");
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_114"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_114"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records are created
		assertFalse(crmIsDataAvailableInTable(transactions), "Data is displayed in the transactions subpanel");
		
		// let's run a query in the DB to check if the transaction was declined
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_114"), super.getProp("visa_pay_method"), super.getProp("test_data_116"));
		verifyTwoStringsAreEqual(respCode, "2", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_114"), super.getProp("visa_pay_method"), super.getProp("test_data_116"));
		verifyStringIsBlank(receiptAdd);
	}
	
	/** 
	 * For ticket BBPRTL-142
	 * 
	 * - declined payment for a transaction
	 * - process for a reference account
	 * - no processing fee since biller charges is ON_SUCCESS
	 * - check the email to the recipient
	 * 
	 * */
	@Test(priority = 22)
	public void verifyDeclinedPayment_03() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_117"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_118"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_187"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_119"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_119"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment processing failed", true);
		verifyTwoStringsAreEqual(labels.declinedMwResp.getText(), "Transaction declined", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Failed payment details has been emailed to " + super.getProp("test_data_58"), true);
		// verify the Try Again button is displayed
		assertTrue(isElementDisplayed(buttons.tryAgain, 0), "Try Again button is not displayed");
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_117"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_117"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records are created
		assertFalse(crmIsDataAvailableInTable(transactions), "Data is displayed in the transactions subpanel");
		
		// let's run a query in the DB to check if the transaction was declined
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_117"), super.getProp("mc_pay_method"), super.getProp("test_data_119"));
		verifyTwoStringsAreEqual(respCode, "2", true);
	}
	
	/** 
	 * Here we will initially put the account number
	 * of an account that belong to payment_gateway id 1, Biller ID 1
	 * then before submitting the payment update the account number
	 * payment_gateway id 5 (Different Merchant Warrior details) Biller ID 5.
	 * Payment should be successful however the transaction should not be created
	 * since in the bbeng_billers_banking there's no merchant_id in id 3
	 * 
	 * */
	@Test(priority = 23)
	public void verifySuccessfulPayment_10() throws SQLException {
		
		// let's make sure that there' no merchant_id
		// for bbeng_billers_banking id 3
		updateMerchantId(null, 3);
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_165"));
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_172"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_37"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_38"));
		inputfields.cardCvv.sendKeys(getProp("test_data_39"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_173"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_174"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_175"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's update the account number
		clickElementAction(inputfields.accountNumber);
		deleteAllTextFromField();
		inputfields.accountNumber.sendKeys(getProp("test_data_171"));
		clickElementAction(inputfields.emailReceipt);
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_173"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		// verify the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// make sure merchant_id is null
		String merchantId = getMerchantId(3);
		verifyStringIsBlank(merchantId);
		clickElementAction(buttons.makePayment);
		// sometimes the initial click does not process the payment
		// so we keep on trying until the progress bar is displayed
		makePaymentBtnRetryClick(3);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_171"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_171"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records created
		assertFalse(crmIsDataAvailableInTable(transactions), "There are records found in the transactions table");
		
		// remove the .00 from the data
		String payAmt = getStringUntil(super.getProp("test_data_173"), ".");
		payAmt = normalizeSpaces(payAmt);
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_171"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_171"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_171"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_171"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_171"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_171"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_171"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_171"));
		verifyStringIsBlank(bankingId);
		verifyStringIsBlank(txDate);
		verifyStringIsBlank(txAmount);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyStringIsBlank(txChannel);
		verifyStringIsBlank(txMethod);
	}
	
	/** 
	 * Here we will initially put the account number
	 * of an account that belong to payment_gateway id 1, Biller ID 1
	 * then before submitting the payment update the account number
	 * payment_gateway id 5 (Different Merchant Warrior details) Biller ID 5.
	 * Payment should be successful however the transaction should now be created
	 * since in the bbeng_billers_banking there's already a merchant_id in id 3
	 * 
	 * This is also to confirm that the make payment page is not using
	 * the Merchant Warrior details that is defined the portal_config.json,
	 * instead it calls a certain endpoint to get merchant warrior details
	 * upon hitting the Make Payment button then uses that to process the payment.
	 * 
	 * */
	@Test(priority = 24)
	public void verifySuccessfulPayment_11() throws SQLException {
		
		// let's make sure that there' no merchant_id
		// for bbeng_billers_banking id 3
		updateMerchantId("591bd3fad3c2d", 3);
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// input all the fields
		inputfields.accountNumber.sendKeys(getProp("test_data_165"));
		switchToMWIframe();
		// slowed the input of the card name because this test failed
		// because only 'P' was entered in the Name on Card
		slowSendKeys(inputfields.cardName, getProp("test_data_172"), true, 200);
		slowSendKeys(inputfields.cardNumber, getProp("test_data_37"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_38"));
		inputfields.cardCvv.sendKeys(getProp("test_data_39"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_173"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_174"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_175"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's update the account number
		clickElementAction(inputfields.accountNumber);
		deleteAllTextFromField();
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.accountNumber);
		inputfields.accountNumber.sendKeys(getProp("test_data_171"));
		clickElementAction(inputfields.paymentAmount);
		inputfields.paymentAmount.sendKeys(Keys.END, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		pauseSeleniumExecution(500);
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		inputfields.paymentAmount.sendKeys(getProp("test_data_176"));
		clickElementAction(inputfields.emailReceipt);
		// verify we have specified the correct payment amount
		String actualPayAmt = getDisplayedValue(inputfields.paymentAmount, true);
		verifyStringContains(true, actualPayAmt, getProp("test_data_176"));
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_176"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// make sure merchant_id has correct value
		String merchantId = getMerchantId(3);
		verifyTwoStringsAreEqual(merchantId, "591bd3fad3c2d", true);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment successfully processed", true);
		verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
				"Payment receipt has been emailed to " + super.getProp("test_data_58"), true);
		
		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_171"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_171"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 1);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 0, 2, "PAY000");
		crmVerifyListOrSubpanelContainsValue(transactions, 0, 3, "Payment");
		crmVerifyListOrSubpanelContainsValue(transactions, 0, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 6);
		String crmTotal = "$" + super.getProp("test_data_176");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 7, crmTotal);
		crmTotal = "$-" + super.getProp("test_data_176");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, crmTotal);
		
		// remove the .00 from the data
		String payAmt = getStringUntil(super.getProp("test_data_176"), ".");
		payAmt = normalizeSpaces(payAmt);
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_171"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_171"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_171"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_171"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_171"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_171"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_171"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_171"));
		String todayDb = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		verifyTwoStringsAreEqual(bankingId, "3", false);
		verifyTwoStringsAreEqual(txDate, todayDb, false);
		verifyTwoStringsAreEqual(txAmount, "545.00", false);
		verifyStringIsBlank(feeAmount);
		verifyStringIsBlank(feeTax);
		verifyTwoStringsAreEqual(txChannel, "INTERNET", false);
		verifyTwoStringsAreEqual(txMethod, "CREDIT_CARD_VI", false);
	}

	
	
}