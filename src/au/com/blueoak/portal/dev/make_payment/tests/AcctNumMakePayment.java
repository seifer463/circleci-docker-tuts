package au.com.blueoak.portal.dev.make_payment.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
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

public class AcctNumMakePayment extends MakePaymentDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	InputFields inputfields;
	Buttons buttons;
	ProgressBar progressbar;
	ToastMsg toastmsg;
	Header header;
	CssStyling cssstyling;
	Labels labels;
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
		uploadMakePaymentConfig(s3Access, "02\\", "portal_config.json");
		
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
		progressbar = new ProgressBar(driver);
		toastmsg = new ToastMsg(driver);
		header = new Header(driver);
		cssstyling = new CssStyling(driver);
		labels = new Labels(driver);
	}

	/** 
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
		
		clickElementAction(inputfields.accountNumber);
		
		// let's just hit the next button but should not do anything
		clickElementAction(buttons.next);
		
		// let's verify that the field is in error
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "The Account Number is not in error state");
		
		// let's verify that the other fields are hidden
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.cardNameList), "Name on Card is displayed");
		assertFalse(isElementExists(inputfields.cardNumberList), "Card Number is displayed");
		assertFalse(isElementExists(inputfields.cardExpiryList), "Expiry is displayed");
		assertFalse(isElementExists(inputfields.cardCvvList), "CVV is displayed");
		assertFalse(isElementExists(inputfields.paymentAmountList), "Payment Amount is displayed");
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// let's put the account number to be able to proceed
		String propAcctNum = "  " + getProp("test_data_08");
		inputfields.accountNumber.sendKeys(propAcctNum, Keys.TAB);
		// let's verify that the extra spaces are removed
		pauseSeleniumExecution(1000);
		String acctNum = inputfields.accountNumber.getAttribute("value");
		verifyTwoStringsAreEqualNoSpaces(acctNum, propAcctNum);
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		
		clickElementAction(inputfields.accountNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		// let's switch in the mwframe
		switchToMWIframe();
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
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
		
		// we will just click the Make Payment
		// but it should not submit
		clickElementAction(buttons.makePayment);
		
		// let's verify the expected fields that should be in error
		// and not in error
		assertFalse(isElementInError(inputfields.accountNumber, 5, 0), "The Account Number is in error state");
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is not error state");
		assertTrue(isElementInError(inputfields.cardNumber, 2, 0), "The Card Number is not in error state");
		assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not in error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		assertTrue(isElementInError(inputfields.paymentAmount, 5, 0), "Payment Amount is not in error state");
		assertTrue(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is not in error state");
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		
		// let's clear the value in the account number
		clickElementAction(inputfields.accountNumber);
		deleteAllTextFromField();
		propAcctNum = "   " + getProp("test_data_01");
		inputfields.accountNumber.sendKeys(propAcctNum);
		acctNum = inputfields.accountNumber.getAttribute("value");
		verifyTwoStringsAreEqualNoSpaces(acctNum, propAcctNum);
		// let's switch in the mwframe
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_02"));
		// credit card type not included in the portal_config.json
		slowSendKeys(inputfields.cardNumber, getProp("test_data_09"), true, 300);
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
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "The Account Number is not in error state");
		// let's switch in the mwframe
		switchToMWIframe();
		assertFalse(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is in error state");
		assertTrue(isElementInError(inputfields.cardNumber, 2, 0), "The Card Number is not in error state");
		assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not in error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		assertTrue(isElementInError(inputfields.paymentAmount, 5, 0), "Payment Amount is not in error state");
		assertTrue(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is not in error state");
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_08"), Keys.TAB);
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		
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
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
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
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
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
		verifyStringIsBlank(payAmountActual);
		
		// verify that users cannot put thousand separator
		// we will update the value the payment amount and will input the
		// currency symbol as well
		// clear first the current value
		clickElementAction(inputfields.paymentAmount);
		deleteAllTextFromField();
		propPayAmount = getProp("test_data_14");
		inputfields.paymentAmount.sendKeys(propPayAmount);
		payAmountActual = getDisplayedValue(inputfields.paymentAmount, false);
		// let's remove the last character from the data
		payAmountExpected = propPayAmount.substring(0, propPayAmount.length() - 1);
		verifyTwoStringsAreEqual(payAmountActual, payAmountExpected, false);
		
		// that if users input a currency that's not the one specified in the portal_config,
		// it would use the one in specified in the portal_config
		// clear the current value first
		inputfields.paymentAmount.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
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
	 * - verify toast will disappear once you click on the message
	 * 
	 *  */
	@Test(priority = 3)
	public void verifyTimeout_01() {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// let's put the account number
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
				counter ++;
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
		// put any value in the account number
		inputfields.accountNumber.sendKeys("01234567890");
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			hoverToElementAction(buttons.helpIcon);
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
		
		// let's verify that the toast will disappear once you start typing
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's put the account number
			inputfields.accountNumber.sendKeys(getProp("test_data_08"));
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
	@Test(priority = 4)
	public void verifyTimeout_02() {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_08"), Keys.TAB);
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		
		// let's wait for the timeout session to appear
		// the value in the portal_config is set to 120 seconds
		// and a timeout_warning of 30 seconds
		// so we will wait for the toast element to appear
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
				counter ++;
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_08"), Keys.TAB);
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		// let's verify that the toast will disappear once we hover the mouse
		clickElementAction(inputfields.accountNumber);
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
		
		// let's verify that the toast will disappear once you start typing
		isElementDisp = waitForElement(toastmsg.toastLoc, 96, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's put the account number
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
		
		// let's click the email receipt checkbox so the toast message would disappear
		clickElementAction(buttons.emailCheckbox);
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
	@Test(priority = 5)
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
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getCssBoxShadowProp(cssstyling.boxShadow), "none",
					assertionErrorMsg(getLineNumber()));
		}
		// verify the color for the placeholders
		// verify the color of the underlines
		softAssertion.assertEquals(
				getPlaceholderCss(labels.labelInput, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Account Number"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(cssstyling.underlineAcctNum), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(buttons.helpIcon), HELP_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(buttons.next, true), "NEXT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS styling for the underlines when there's a valid input
	 * - verify the the css styling for the placeholder there's a valid input
	 * - verify the text color of the input text when it's a valid input
	 * 
	 * */
	@Test(priority = 6)
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
		inputfields.accountNumber.sendKeys(getProp("test_data_33"), Keys.TAB);
		waitForCssToRender();
		
		// verify the CSS and displayed labels
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
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the CSS styling for the underlines when there's an invalid input
	 * - verify the the css styling for the placeholder there's an invalid input
	 * - verify the text color of the input text when it's an invalid input
	 * 
	 * */
	@Test(priority = 7)
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
		inputfields.accountNumber.sendKeys(getProp("test_data_01"), Keys.TAB);
		waitForCssToRender();
		
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
	@Test(priority = 8)
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
		
		// let's click on all fields
		clickElementAction(inputfields.accountNumber);
		// add a pause to fix an issue
		// where the field was not clicked
		pauseSeleniumExecution(500);
		
		// let's click on Next to trigger validation
		clickElementAction(buttons.next);
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
	@Test(priority = 9)
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
	}
	
	/** 
	 * For ticket BBPRTL-145
	 * 
	 * - verify the css for the buttons
	 * - verify the css for the help page
	 * - verify the progress bar text color
	 * - verify the progress bar color
	 * - verify the spinner color
	 * 
	 * */
	@Test(priority = 10)
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
		softAssertion.assertEquals(getDisplayedText(buttons.next, true), "NEXT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.next), NEXT_BUTTON_CSTM,
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
		
		// let's populate the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_33"), Keys.TAB);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(buttons.next, true), "NEXT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(buttons.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(buttons.next);
		waitForCssToRender();
		assertEquals(getButtonCss(buttons.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the text we are displaying
		assertEquals(getDisplayedText(labels.nextProgBar, true), "Creating secure payment connection...",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2108
		assertEquals(getLabelCss(labels.nextProgBar), PROCESSING_REQUEST_MSG_CSTM,
				assertionErrorMsg(getLineNumber()));
		assertEquals(getCssStrokeProp(cssstyling.spinner), SPINNER_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));		
		String progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG_NXT;
		String progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG_NXT;
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
		String remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
		softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
				"The initial progress bar color is not correct");
		softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
				"The remaining progress bar color is not correct");
		// verify all assertions
		softAssertion.assertAll();
		
		nextBtnProgBarLoad();
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
				"Payment Amount is not in error state");
		softAssertion.assertFalse(isElementInError(inputfields.emailReceipt, 5, 0),
				"Email Address is not in error state");

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
		
		// verify the color for the placeholders
		// verify the color of the underlines
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
	 * - verify the background color
	 * - verify the box shadow
	 * - verify the footer background
	 * - verify the icon business name link
	 * - verify the support text
	 * 
	 * */
	@Test(priority = 11)
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
	 * For ticket BBPRTL-227
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
	@Test(priority = 12)
	public void verifySuccessfulPayment_01() throws SQLException {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_162"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_162"), true);
		
		// let's click on the account number
		clickElementAction(inputfields.accountNumber);
		// let's click on the email address field
		clickElementAction(inputfields.emailReceipt);
		// let's verify that we did not go back into the screen
		// where only account number is displayed
		buttons = new Buttons(driver, 0);
		assertFalse(isElementExists(buttons.nextList), "The Next button is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// input the remaining fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_162"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_162"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_52"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$" + super.getProp("test_data_177"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_53"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, concatStrings("$", getProp("test_data_178")));
		// verify the value created in the 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "11/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "000001003-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 5, "01/01/2016");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 6, "$251.60");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$259.38");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_162"), super.getProp("mc_pay_method"), super.getProp("test_data_53"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_162"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_162"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_162"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_162"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_162"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_162"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_162"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - submit payment and verify no processing fee should be applied
	 * - verify no processing fee is displayed in the email notification
	 * - verify that the To: in the email contains to recipients
	 * - verify the transaction is recorded in the corresponding account in CRM
	 * 
	 * @throws SQLException 
	 * */
	@Test(priority = 13)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_163"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_163"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_163"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_163"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, concatStrings("$", getProp("test_data_179")));
		// verify the values in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, "13/03/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 2, "000000112-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Jan-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 5, "29/03/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 6, "$12,651.75");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, "$43,473.25");
		
		// remove the .00 from the data
		String payAmt = getStringUntil(super.getProp("test_data_56"), ".");
		payAmt = normalizeSpaces(payAmt);
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_163"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_163"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_163"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_163"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_163"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_163"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_163"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_163"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - verify that the processing fee will not be displayed
	 * because it's on ON_SUCCESS
	 * - verify two recipients is displayed in the email To:
	 * - verify that no processing fee is displayed in the email notification
	 * - verify the transaction is recorded in the corresponding account in CRM
	 * 
	 * @throws SQLException 
	 * */
	@Test(priority = 14)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_164"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_164"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_164"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_164"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 4);
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
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(), super.getProp("test_data_164"),
				super.getProp("amex_pay_method"), super.getProp("test_data_62"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_164"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_164"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_164"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_164"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_164"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_164"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_164"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - we will enter an email address then will tick off the checkbox for email
	 * then ensure no email was sent
	 * - processing fee will be involved
	 * - updated the payment amount then verify that the calculation is changed accordingly
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 15)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_165"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_165"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_165"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_165"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "reference");
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
				super.getProp("test_data_165"), super.getProp("amex_pay_method"), super.getProp("test_data_78"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_165"), super.getProp("amex_pay_method"), super.getProp("test_data_78"));
		verifyStringIsBlank(receiptAdd);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_165"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_165"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_165"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_165"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_165"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_165"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_165"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - after entering the payment amount, we will tick off the checkbox for email
	 * then ensure no email was sent
	 * - no processing fee will be involved
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 16)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_166"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_166"), true);
		
		// input all the fields
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
		assertTrue(isElementExists(buttons.largeAmtCheckboxList), "The Large Amount detected tickbox is not displayed");
		// let's tick the large amount checkbox
		clickElementAction(buttons.largeAmtCheckbox);
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
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
		crmGetListViewTableWithSearch(super.getProp("test_data_166"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_166"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 2, "000000192-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 5, "21/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 6, "$1,412.37");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, "reference");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_166"), super.getProp("amex_pay_method"), super.getProp("test_data_75"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_166"), super.getProp("amex_pay_method"), super.getProp("test_data_75"));
		verifyStringIsBlank(receiptAdd);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_166"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_166"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_166"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_166"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_166"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_166"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_166"));
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
	 * For ticket BBPRTL-227
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
	@Test(priority = 17)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_79"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_79"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		// did this because sometimes the processing payment progress bar is displayed
		// and causes issues
		labels = new Labels(driver, 1);
		if (isElementExists(labels.paymentProgBarList)) {
			makePaymentBtnProgBarLoad();
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		// did this because sometimes the processing payment progress bar is displayed
		// and causes issues
		labels = new Labels(driver, 1);
		if (isElementExists(labels.paymentProgBarList)) {
			makePaymentBtnProgBarLoad();
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		clickElementAction(inputfields.cardName);
		inputfields.cardName.sendKeys(super.getProp("test_data_84"));
		// let's just put an assertion that the data is entered into the field
		verifyTwoStringsAreEqual(inputfields.cardName.getAttribute("value"), super.getProp("test_data_84"), true);
		// let's clear the cvv and enter correct one
		clickElementAction(inputfields.cardCvv);
		inputfields.cardCvv.clear();
		inputfields.cardCvv.sendKeys(super.getProp("test_data_36"));
		// let's just put an assertion that the data is entered into the field
		verifyTwoStringsAreEqual(inputfields.cardCvv.getAttribute("value"), super.getProp("test_data_36"), true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// lets update the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		inputfields.accountNumber.sendKeys(super.getProp("test_data_167"), Keys.TAB);
		// hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_167"), true);
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(super.getProp("test_data_84"));
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
		// verify the fix for ticket BBPRTL-1147
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
		crmGetListViewTableWithSearch(super.getProp("test_data_167"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_167"));
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
				super.getProp("test_data_167"), super.getProp("mc_pay_method"), super.getProp("test_data_82"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receipt_add = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_167"), super.getProp("mc_pay_method"), super.getProp("test_data_82"));
		verifyTwoStringsAreEqual(receipt_add, super.getProp("test_data_86"), true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_167"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_167"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_167"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_167"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_167"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_167"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_167"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - verify that we will still be able to process payment
	 * even if initially the users did not complete the MW fields
	 * (e.g. Missing CVV and Incorrect Expiry)
	 * - then we will update the account number to something else that has
	 * a different biller
	 * - then verify the email sent
	 * 
	 * */
	@Test(priority = 18)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_88"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_88"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		// did this because sometimes the processing payment progress bar is displayed
		// and causes issues
		labels = new Labels(driver, 1);
		if (isElementExists(labels.paymentProgBarList)) {
			makePaymentBtnProgBarLoad();
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		// hit next
		inputfields.accountNumber.sendKeys(super.getProp("test_data_168"), Keys.TAB);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_168"), true);
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_89"));
		// intermittent issue here
		// throwing org.openqa.selenium.ElementNotInteractableException: element not interactable
		// so we catch the exception
		try {
			slowSendKeys(inputfields.cardNumber, getProp("test_data_34"), true, 300);
		} catch (ElementNotInteractableException enie) {
			logDebugMessage(concatStrings(
					"ElementNotInteractableException when trying to enter the credit card, will try to switch into the iFrame again. See error more details ->",
					enie.getLocalizedMessage()));
			if (getPortalType().equals("standalone")) {
				// let's switch out of the mwframe
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			switchToMWIframe();
		}
		inputfields.cardExpiry.sendKeys(super.getProp("test_data_35"));
		inputfields.cardCvv.sendKeys(super.getProp("test_data_36"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		total = getDisplayedText(labels.payTotal, true);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_91"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// verify the fix for ticket BBPRTL-665
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_168"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_168"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_183"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_91"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_91"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		// verify the values created in the 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "15/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "PAY000001018");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Payment via MAIL");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 6);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 7, "$371.34");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$0.00");
		
		// let's run a query in the DB to check if the transaction was approved
		// remove the .0 from the data
		String payAmt = getStringUntil(super.getProp("test_data_91"), ".");
		payAmt = normalizeSpaces(payAmt);
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_168"), super.getProp("mc_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_168"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_168"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_168"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_168"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_168"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_168"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_168"));
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
	 * For ticket BBPRTL-227
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
	@Test(priority = 19)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_169"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_169"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		// did this because sometimes the processing payment progress bar is displayed
		// and causes issues
		labels = new Labels(driver, 1);
		if (isElementExists(labels.paymentProgBarList)) {
			makePaymentBtnProgBarLoad();
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's verify that card name, card expiry and cvv is in error state
		// let's switch in the mwframe
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Card Name is not error state");
		assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not error state");
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
		// let's verify no records created in the DB
		assertEquals(super.getNumOfRecordsInDbPayProc(super.getProp("test_data_169")), 0, "The number of records created in the DB is not correct.");
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
		crmGetListViewTableWithSearch(super.getProp("test_data_169"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_169"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_102"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$" + super.getProp("test_data_181"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_103"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, "$".concat(getProp("test_data_182")));
		// verify the values created int he 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "14/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "000000420-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 5, "04/01/2016");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 6, "$708.78");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$708.78");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_169"), super.getProp("mc_pay_method"), super.getProp("test_data_103"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_169"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_169"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_169"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_169"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_169"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_169"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_169"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - we will process a payment with processing fee
	 * for a reference account
	 * - verify that once the Email checkbox is ticked, the email 
	 * address is removed
	 * 
	 * */
	@Test(priority = 20)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_170"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_170"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_105"));
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
		crmGetListViewTableWithSearch(super.getProp("test_data_170"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_170"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "000000080-0");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 5, "15/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 6, "$165.89");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "reference");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_170"), super.getProp("visa_pay_method"), super.getProp("test_data_108"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_170"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_170"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_170"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_170"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_170"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_170"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_170"));
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
	 * For ticket BBPRTL-227
	 * 
	 * - verify declined payments
	 * - verify declined payment does not create a record in the crm
	 * - verify an email is still sent to the user
	 * 
	 * */
	@Test(priority = 21)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_109"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		String removedSpacesAcctNum = super.getProp("test_data_109");
		removedSpacesAcctNum = StringUtils.deleteWhitespace(removedSpacesAcctNum);
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), removedSpacesAcctNum, true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_109"), true, 1, false, false);
		crmClickRecordExactLinkText(removedSpacesAcctNum);
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records are created
		assertFalse(crmIsDataAvailableInTable(transactions), "Data is displayed in the transactions subpanel");
		
		// let's run a query in the DB to check if the transaction was declined
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(), removedSpacesAcctNum,
				super.getProp("mc_pay_method"), super.getProp("test_data_113"));
		verifyTwoStringsAreEqual(respCode, "2", true);
	}
	
	/** 
	 * For ticket BBPRTL-227
	 * 
	 * - process a declined payment
	 * - no processing fee
	 * - no email that should be sent out
	 * 
	 * */
	@Test(priority = 22)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_114"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_114"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
		// verify only the account number is displayed
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "The Account Number field is not displayed");
		// others should be hidden
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.cardNameList), "Name on Card is displayed");
		assertFalse(isElementExists(inputfields.cardNumberList), "Card Number is displayed");
		assertFalse(isElementExists(inputfields.cardExpiryList), "Expiry is displayed");
		assertFalse(isElementExists(inputfields.cardCvvList), "CVV is displayed");
		assertFalse(isElementExists(inputfields.paymentAmountList), "Payment Amount is displayed");
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
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
	 * For ticket BBPRTL-227
	 * 
	 * - declined payment for a transaction
	 * - process for a reference account
	 * - no processing fee since biller charges is ON_SUCCESS
	 * - check the email to the recipient
	 * 
	 * */
	@Test(priority = 23)
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
		
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_117"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// did this because there's an intermittent issue
		// where it did not go to the next page
		// and the account number was blank/empty
		try {
			// let's make sure the account number has value
			verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_117"), true);
		} catch (AssertionError ae) {
			logDebugMessage("AssertionError was encountered in verifyDeclinedPayment_03 test case. Trying again.");
			// let's just refresh the page to ensure we have a clean portal
			refreshBrowser(1, 5000);
			if (getPortalType().equals("embedded")) {
				// let's switch into the bluebilling iframe
				switchToMakePaymentEmbeddedIframe(1);
			}
			loadMakePayment();

			// let's put the account number to be able to proceed
			inputfields.accountNumber.sendKeys(getProp("test_data_117"), Keys.TAB, Keys.ENTER);
			clickElementAction(buttons.next);
			verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
			nextBtnProgBarLoad();
			verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_117"), true);
		}
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
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
	 * For ticket BBPRTL-227
	 * For ticket BBPRTL-143
	 * 
	 * - verify that if an error is returned when hitting next, the next button
	 * should still be enabled
	 * - process approved payment
	 * - verify the processing fee
	 * - verify the email sent
	 * 
	 * */
	@Test(priority = 24)
	public void verifyAccNumValidations_01() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_120"));
		// let's update an account to have an invalid account number
		updateAcctNum(super.getProp("test_data_120"), super.getProp("test_data_121"));
		// generate an event
		inputfields.accountNumber.sendKeys(Keys.TAB);
		// let's hit next
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		// verify that account number is in error
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "Account Number is not in error state");
		// verify that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is not enabled");
		
		// let's hit next again
		clickElementAction(buttons.next);
		nextBtnProgBarLoad();
		// verify that account number is in error
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "Account Number is not in error state");
		// verify that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is not enabled");
		
		// let's clear the value of the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		// let's revert the value of the account number
		updateAcctNum(super.getProp("test_data_121"), super.getProp("test_data_120"));
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_120"), Keys.TAB, Keys.ENTER);
		// let's hit next one more time
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_120"), true);
		// input the remaining fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_122"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_123"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_83"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_124"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_125"), super.getProp("currency_significant_digits"));
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
		crmGetListViewTableWithSearch(super.getProp("test_data_120"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_120"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_124"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_123"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_125"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_125"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_120"), super.getProp("mc_pay_method"), super.getProp("test_data_125"));
		verifyTwoStringsAreEqual(respCode, "0", true);
	}
	
	/** 
	 * For ticket BBPRTL-227
	 * 
	 * - verify that once the user enters a valid account number then hit next
	 * then users updates the account number to an invalid one or clears the account number field,
	 * the fields should not be cleared and system should not redirect the screen
	 * where only the account number is shown
	 * - verify that once the user enters a valid account number then hit next
	 * then the user updated the account number to a valid one, the values
	 * entered in the Name on Card, Card Number, Expiry and CVV should be
	 * cleared out and system redirects the user back to the screen
	 * where only account number is shown. After hitting next, users should
	 * still see the values they entered in the payment amount and email payment
	 * receipt.
	 * 
	 * @throws SQLException 
	 * 
	 * */
	@Test(priority = 25)
	public void verifyAccNumValidations_02() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_126"), Keys.TAB, Keys.ENTER);
		// let's hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_126"), true);
		
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_127"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_128"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_83"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_129"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_130"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
		
		// let's clear the value of the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		// let's click on the payment amount
		clickElementAction(inputfields.paymentAmount);
		// verify that the account number is in error state
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "Account number is not in error state");
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// verify that the fields are still displayed
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		switchToMWIframe();
		assertTrue(isElementDisplayed(inputfields.cardName, 0), "Name on Card is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardNumber, 0), "Card Number is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardExpiry, 0), "Expiry is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardCvv, 0), "CVV is not displayed");
		String cardName = inputfields.cardName.getAttribute("value");
		String cardNum = inputfields.cardNumber.getAttribute("value");
		cardNum = StringUtils.deleteWhitespace(cardNum);
		String cardExp = inputfields.cardExpiry.getAttribute("value");
		String cardCvv = inputfields.cardCvv.getAttribute("value");
		verifyTwoStringsAreEqual(cardName, super.getProp("test_data_127"), true);
		verifyTwoStringsAreEqual(cardNum, super.getProp("test_data_34"), true);
		verifyTwoStringsAreEqual(cardExp, super.getProp("test_data_35"), true);
		verifyTwoStringsAreEqual(cardCvv, super.getProp("test_data_36"), true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		assertTrue(isElementDisplayed(inputfields.paymentAmount, 0), "Payment Amount is not displayed");
		assertTrue(isElementDisplayed(inputfields.emailReceipt, 0), "Email Receipt is not displayed");
		String paymentAmt = inputfields.paymentAmount.getAttribute("value"); 
		String emailAdd = inputfields.emailReceipt.getAttribute("value");
		String payAmtExp = addMissingZeroes(super.getProp("test_data_128"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(paymentAmt, payAmtExp, true);
		// not sure why on this test case the email address is stripped with all spaces
		// so just created another assertion that has no spaces for the email addresses
		verifyTwoStringsAreEqual(emailAdd, super.getProp("test_data_188"), true);
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_129"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_130"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		
		// let's input an invalid value for account number
		inputfields.accountNumber.sendKeys(super.getProp("test_data_131"), Keys.TAB);
		// verify that the account number is in error state
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "Account Number is not in error state");
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// verify that the fields are still displayed
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		switchToMWIframe();
		assertTrue(isElementDisplayed(inputfields.cardName, 0), "Name on Card is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardNumber, 0), "Card Number is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardExpiry, 0), "Expiry is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardCvv, 0), "CVV is not displayed");
		cardName = inputfields.cardName.getAttribute("value");
		cardNum = inputfields.cardNumber.getAttribute("value");
		cardNum = StringUtils.deleteWhitespace(cardNum);
		cardExp = inputfields.cardExpiry.getAttribute("value");
		cardCvv = inputfields.cardCvv.getAttribute("value");
		verifyTwoStringsAreEqual(cardName, super.getProp("test_data_127"), true);
		verifyTwoStringsAreEqual(cardNum, super.getProp("test_data_34"), true);
		verifyTwoStringsAreEqual(cardExp, super.getProp("test_data_35"), true);
		verifyTwoStringsAreEqual(cardCvv, super.getProp("test_data_36"), true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		assertTrue(isElementDisplayed(inputfields.paymentAmount, 0), "Payment Amount is not displayed");
		assertTrue(isElementDisplayed(inputfields.emailReceipt, 0), "Email Receipt is not displayed");
		paymentAmt = inputfields.paymentAmount.getAttribute("value"); 
		emailAdd = inputfields.emailReceipt.getAttribute("value");
		payAmtExp = getProp("currency_symbol")
				+ addMissingZeroes(super.getProp("test_data_128"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(paymentAmt, payAmtExp, true);
		// not sure why on this test case the email address is stripped with all spaces
		// so just created another assertion that has no spaces for the email addresses
		verifyTwoStringsAreEqual(emailAdd, super.getProp("test_data_188"), true);
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_129"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_130"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		
		// let's input a new valid account number
		// let's clear the value of the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		inputfields.accountNumber.sendKeys(super.getProp("test_data_132"));
		pauseSeleniumExecution(500);
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		// verify that the other fields are hidden
		boolean isFrameDisp;
		try {
			switchToMWIframe();
			isFrameDisp = true;
		} catch (NoSuchFrameException nsfe) {
			isFrameDisp = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// verify the mwpayframe is not displayed
		assertFalse(isFrameDisp, "The Merchant Warrior iframe is displayed");
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.cardNameList), "Name on Card is displayed");
		assertFalse(isElementExists(inputfields.cardNumberList), "Card Number is displayed");
		assertFalse(isElementExists(inputfields.cardExpiryList), "Expiry is displayed");
		assertFalse(isElementExists(inputfields.cardCvvList), "CVV is displayed");
		assertFalse(isElementExists(inputfields.paymentAmountList), "Payment Amount is displayed");
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// generate an event
		inputfields.accountNumber.sendKeys(Keys.TAB);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_132"), true);
		// let verify that the MW card values are cleared
		switchToMWIframe();
		cardName = inputfields.cardName.getAttribute("value");
		cardNum = inputfields.cardNumber.getAttribute("value");
		cardExp = inputfields.cardExpiry.getAttribute("value");
		cardCvv = inputfields.cardCvv.getAttribute("value");
		verifyStringIsBlank(cardName);
		verifyStringIsBlank(cardNum);
		verifyStringIsBlank(cardExp);
		verifyStringIsBlank(cardCvv);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the payment amount and email address value are still there
		paymentAmt = inputfields.paymentAmount.getAttribute("value"); 
		emailAdd = inputfields.emailReceipt.getAttribute("value");
		payAmtExp = getProp("currency_symbol")
				+ addMissingZeroes(super.getProp("test_data_128"), super.getProp("currency_significant_digits"));
		// verify the fix for ticket BBPRTL-1147
		verifyTwoStringsAreEqual(paymentAmt, payAmtExp, true);
		// not sure why on this test case the email address is stripped with all spaces
		// so just created another assertion that has no spaces for the email addresses
		verifyTwoStringsAreEqual(emailAdd, super.getProp("test_data_188"), true);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// let's click the payment amount field
		clickElementAction(inputfields.paymentAmount);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		inputfields.paymentAmount.sendKeys(Keys.TAB);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's input the same card details
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_127"));
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
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_129"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_130"), super.getProp("currency_significant_digits"));
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_132"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_132"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_129"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_128"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_130"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_130"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_132"), super.getProp("mc_pay_method"), super.getProp("test_data_130"));
		verifyTwoStringsAreEqual(respCode, "0", true);
	}
	
	/** 
	 * For ticket BBPRTL-227
	 * 
	 * - we will verify that the payment processing will not be stuck
	 * if we put an invalid cvv then eventually updating it with a correct one
	 * 
	 * */
	@Test(priority = 26)
	public void verifyAccNumValidations_03() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_133"), Keys.TAB, Keys.ENTER);
		// let's hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// did this to fix an intermittent issue where the account number is empty
		if (StringUtils.isBlank(inputfields.accountNumber.getAttribute("value"))) {
			// let's enter the account number again
			inputfields.accountNumber.sendKeys(getProp("test_data_133"), Keys.TAB, Keys.ENTER);
			// let's hit next
			clickElementAction(buttons.next);
			verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
			nextBtnProgBarLoad();
			// let's make sure the account number has value
			verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_133"), true);
		} else {
			// let's make sure the account number has value
			verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_133"), true);
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is enabled");
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_134"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_136"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_137"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_138"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
		
		// let's clear the value of the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		pauseSeleniumExecution(500);
		// verify the fields are not hidden
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		switchToMWIframe();
		assertTrue(isElementDisplayed(inputfields.cardName, 0), "Name on Card is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardNumber, 0), "Card Number is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardExpiry, 0), "Expiry is not displayed");
		assertTrue(isElementDisplayed(inputfields.cardCvv, 0), "CVV is not displayed");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		assertTrue(isElementDisplayed(inputfields.paymentAmount, 0), "Payment Amount is not displayed");
		assertTrue(isElementDisplayed(inputfields.emailReceipt, 0), "Email Receipt is not displayed");
		// verify that the make payment is still enabled since we did not change focus
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// let's input the same account number
		inputfields.accountNumber.sendKeys(super.getProp("test_data_133"));
		pauseSeleniumExecution(500);
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_137"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_138"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// verify that the make payment is enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		
		// let's clear the value of the account number
		clickElementAction(inputfields.accountNumber);
		// clear the account number
		deleteAllTextFromField();
		// let's update the account number with a new valid one
		inputfields.accountNumber.sendKeys(super.getProp("test_data_139"), Keys.TAB);
		pauseSeleniumExecution(500);
		// verify first that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is not enabled");
		// let's hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		// verify the fix for bug ticket BBPRTL-2110
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_139"), true);
		
		// input all the fields
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_140"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_40"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_41"));
		inputfields.cardCvv.sendKeys(getProp("test_data_135"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the fix for ticket BBPRTL-1147
		// at this point, we should still have values for payment amount and email address
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_137"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_138"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		clickElementAction(buttons.makePayment);
		// verify the text we are displaying
		verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
		makePaymentBtnProgBarLoad();
		// verify the cvv is in error state
		switchToMWIframe();
		assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "CVV is not in error state");
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_137"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_138"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
		// let's clear out the cvv
		switchToMWIframe();
		inputfields.cardCvv.clear();
		inputfields.cardCvv.sendKeys(super.getProp("test_data_42"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
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
		crmGetListViewTableWithSearch(super.getProp("test_data_139"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_139"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 5);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card surcharge [Biller 2] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_137"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$" + super.getProp("test_data_141"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_138"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$" + super.getProp("test_data_142");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		// verify 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "03/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "PAY000000967");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Payment via EFT");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 6);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 7, "$1,866.68");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$995.53");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_139"), super.getProp("amex_pay_method"), super.getProp("test_data_138"));
		verifyTwoStringsAreEqual(respCode, "0", true);
	}
	
	/** 
	 * For ticket BBPRTL-227
	 * 
	 * - we will process to approved payment in succession
	 * but with different billers. Ensure that it picked
	 * up the correct biller
	 * 
	 * */
	@Test(priority = 27)
	public void verifyAccNumValidations_04() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_143"), Keys.TAB, Keys.ENTER);
		// let's hit next
		clickElementAction(buttons.next);
		// did this to fix an intermittent issue where the loading message was not displayed
		nextBtnRetryClick(3);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_143"), true);
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		// input all the fields
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_144"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_145"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_83"));
		// verify the processing fee text
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_146"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_147"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(payProcFee,
				"Processing fee of " + procFeeAmt + " is applicable. A total of " + totalAmt + " will be processed",
				true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is not enabled");
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
		crmGetListViewTableWithSearch(super.getProp("test_data_143"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_143"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 3] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_146"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "reference");
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_147"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_147"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_143"), super.getProp("mc_pay_method"), super.getProp("test_data_147"));
		verifyTwoStringsAreEqual(respCode, "0", true);
		String receiptAdd = getPaymentTransactionColValue("receipt_address", super.getInstanceIdMakePayment(),
				super.getProp("test_data_143"), super.getProp("mc_pay_method"), super.getProp("test_data_147"));
		verifyTwoStringsAreEqual(receiptAdd, super.getProp("test_data_86"), true);
		
		accessPortal(getStandaloneUrlMakePayment(), true);
		loadMakePayment();
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_148"), Keys.TAB);
		// let's hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_148"), true);
		
		// input the remaining fields
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_149"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_150"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		// verify the processing fee text
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_151"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_152"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
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
		}
		
		// navigate into the accounts list view
		crmOpenListView("Accounts", null, false);
		// search using account number then sort by account number
		crmGetListViewTableWithSearch(super.getProp("test_data_148"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_148"));
		crmSwitchToWindow(2, true, 14, Keys.DOWN);
		transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify the number of records to be created
		crmVerifyNumOfRecordsInListViewOrSubpanel(transactions, 2);
		today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		// verify the values created in the 1st row
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 1, today);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 2, "un-posted");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 3, "Adjustment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 4, "Credit Card Master Card surcharge [Biller 1] (ELEC_PSC_001)");
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 5);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_151"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$-" + super.getProp("test_data_150"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		crmTotal = "$" + addMissingZeroes(super.getProp("test_data_152"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$-" + addMissingZeroes(super.getProp("test_data_152"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		
		// let's run a query in the DB to check if the transaction was approved
		respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_148"), super.getProp("mc_pay_method"), super.getProp("test_data_152"));
		verifyTwoStringsAreEqual(respCode, "0", true);
	}
	
	/** 
	 * For ticket BBPRTL-227
	 * 
	 * - we will use an account number that has no biller
	 * payment method and assert the error. Then afterwards
	 * we will process an approved payment with another account
	 * that has payment gateway
	 * 
	 * */
	@Test(priority = 28)
	public void verifyAccNumValidations_05() throws SQLException {
		
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMakePayment(), true);
			loadMakePayment();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMakePayment(), true);
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
			loadMakePayment();
		}
		
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// let's put the account number to be able to proceed
		inputfields.accountNumber.sendKeys(getProp("test_data_153"), Keys.TAB, Keys.ENTER);
		// let's hit next
		clickElementAction(buttons.next);
		// did this to fix an intermittent issue where the loading message was not displayed
		nextBtnRetryClick(3);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_153"), true);
		// verify that the account number is in error state
		// because it does not have a biller
		assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "Account Number is not in error state");
		// verify first that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		
		// let's clear the account number
		clickElementAction(inputfields.accountNumber);
		deleteAllTextFromField();
		// enter a new valid one that has payment gateway
		inputfields.accountNumber.sendKeys(getProp("test_data_154"), Keys.TAB);
		// let's hit next
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		// and not in error state
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_154"), true);
		assertFalse(isElementInError(inputfields.accountNumber, 5, 0), "Account Number is in error state");
		switchToMWIframe();
		// let's just make sure the pay frame elements are displayed
		waitForElement(inputfields.cardName, 3, 30);
		inputfields.cardName.sendKeys(getProp("test_data_155"));
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
		inputfields.paymentAmount.sendKeys(getProp("test_data_156"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_58"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_156"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.emailReceipt);
		inputfields.emailReceipt.sendKeys(Keys.TAB, Keys.ENTER);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_154"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_154"));
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
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 6, "$" + super.getProp("test_data_157"));
		crmVerifyListorSubpanelValueIsBlank(transactions, 0, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 0, 8, "$" + super.getProp("test_data_158"));
		// verify the values created in the 2nd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 1, today);
		crmVerifyListOrSubpanelStartsWith(transactions, 1, 2, "PAY000");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 3, "Payment");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 4, "Payment via INTERNET");
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 5);
		crmVerifyListorSubpanelValueIsBlank(transactions, 1, 6);
		String crmTotal = "$" + addMissingZeroes(super.getProp("test_data_156"), super.getProp("crm_currency_significant_digits"));
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 7, crmTotal);
		crmTotal = "$" + super.getProp("test_data_159");
		crmVerifyListOrSubpanelEqualsValue(transactions, 1, 8, crmTotal);
		// verify the 3rd row
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 1, "07/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 2, "000000022-1");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 3, "Utility Bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 4, "Nov-2015 Electricity bill");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 5, "24/12/2015");
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 6, "$55.98");
		crmVerifyListorSubpanelValueIsBlank(transactions, 2, 7);
		crmVerifyListOrSubpanelEqualsValue(transactions, 2, 8, "$293.92");
		
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_154"), super.getProp("mc_pay_method"), super.getProp("test_data_156"));
		verifyTwoStringsAreEqual(respCode, "0", true);
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
	@Test(priority = 29)
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
		inputfields.accountNumber.sendKeys(getProp("test_data_165"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		// did this to fix an intermittent issue where the loading message was not displayed
		nextBtnRetryClick(3);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_165"), true);
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
		inputfields.accountNumber.sendKeys(getProp("test_data_180"));
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		// verify that the other fields are hidden
		boolean isFrameDisp;
		try {
			switchToMWIframe();
			isFrameDisp = true;
		} catch (NoSuchFrameException nsfe) {
			isFrameDisp = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// verify the mwpayframe is not displayed
		assertFalse(isFrameDisp, "The Merchant Warrior iframe is displayed");
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.cardNameList), "Name on Card is displayed");
		assertFalse(isElementExists(inputfields.cardNumberList), "Card Number is displayed");
		assertFalse(isElementExists(inputfields.cardExpiryList), "Expiry is displayed");
		assertFalse(isElementExists(inputfields.cardCvvList), "CVV is displayed");
		assertFalse(isElementExists(inputfields.paymentAmountList), "Payment Amount is displayed");
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// generate an event
		inputfields.accountNumber.sendKeys(Keys.TAB);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_180"), true);
		// let verify that the MW card values are cleared
		switchToMWIframe();
		String cardName = inputfields.cardName.getAttribute("value");
		String cardNum = inputfields.cardNumber.getAttribute("value");
		String cardExp = inputfields.cardExpiry.getAttribute("value");
		String cardCvv = inputfields.cardCvv.getAttribute("value");
		verifyStringIsBlank(cardName);
		verifyStringIsBlank(cardNum);
		verifyStringIsBlank(cardExp);
		verifyStringIsBlank(cardCvv);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the payment amount and email address value are still there
		String paymentAmt = inputfields.paymentAmount.getAttribute("value"); 
		String emailAdd = inputfields.emailReceipt.getAttribute("value");
		String payAmtExp = getProp("currency_symbol")
				+ addMissingZeroes(super.getProp("test_data_173"), super.getProp("currency_significant_digits"));
		// verify the fix for ticket BBPRTL-1147
		verifyTwoStringsAreEqual(paymentAmt, payAmtExp, true);
		verifyTwoStringsAreEqual(emailAdd, super.getProp("test_data_58"), true);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// let's click the payment amount field
		clickElementAction(inputfields.paymentAmount);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		inputfields.paymentAmount.sendKeys(Keys.TAB);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's input the same card details
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
		clickElementAction(inputfields.emailReceipt);
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_173"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// make sure merchant_id is null
		String merchantId = getMerchantId(3);
		verifyStringIsBlank(merchantId);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_180"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_180"));
		crmSwitchToWindow(1, true, 14, Keys.DOWN);
		WebElement transactions = crmSortTableDescWithinPanel("Transactions", 1, false);
		// verify no records created
		assertFalse(crmIsDataAvailableInTable(transactions), "There are records found in the transactions table");
		
		// remove the .00 from the data
		String payAmt = getStringUntil(super.getProp("test_data_173"), ".");
		payAmt = normalizeSpaces(payAmt);
		// let's run a query in the DB to check if the transaction was approved
		String respCode = getPaymentTransactionColValue("response_code", super.getInstanceIdMakePayment(),
				super.getProp("test_data_180"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_180"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_180"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_180"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_180"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_180"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_180"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_180"));
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
	@Test(priority = 30)
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
		inputfields.accountNumber.sendKeys(getProp("test_data_165"), Keys.TAB, Keys.ENTER);
		clickElementAction(buttons.next);
		// did this to fix an intermittent issue where the loading message was not displayed
		nextBtnRetryClick(3);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_165"), true);
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
		inputfields.accountNumber.sendKeys(getProp("test_data_180"));
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(inputfields.accountNumber, 0), "Account Number is not displayed");
		// verify that the other fields are hidden
		boolean isFrameDisp;
		try {
			switchToMWIframe();
			isFrameDisp = true;
		} catch (NoSuchFrameException nsfe) {
			isFrameDisp = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// verify the mwpayframe is not displayed
		assertFalse(isFrameDisp, "The Merchant Warrior iframe is displayed");
		inputfields = new InputFields(driver, 0);
		assertFalse(isElementExists(inputfields.cardNameList), "Name on Card is displayed");
		assertFalse(isElementExists(inputfields.cardNumberList), "Card Number is displayed");
		assertFalse(isElementExists(inputfields.cardExpiryList), "Expiry is displayed");
		assertFalse(isElementExists(inputfields.cardCvvList), "CVV is displayed");
		assertFalse(isElementExists(inputfields.paymentAmountList), "Payment Amount is displayed");
		assertFalse(isElementExists(inputfields.emailReceiptList), "Email Receipt is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify that the next button is enabled
		assertTrue(isNextBtnEnabled(), "Next button is disabled");
		// generate an event
		inputfields.accountNumber.sendKeys(Keys.TAB);
		clickElementAction(buttons.next);
		verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
		nextBtnProgBarLoad();
		// let's make sure the account number has value
		verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), getProp("test_data_180"), true);
		// let verify that the MW card values are cleared
		switchToMWIframe();
		String cardName = inputfields.cardName.getAttribute("value");
		String cardNum = inputfields.cardNumber.getAttribute("value");
		String cardExp = inputfields.cardExpiry.getAttribute("value");
		String cardCvv = inputfields.cardCvv.getAttribute("value");
		verifyStringIsBlank(cardName);
		verifyStringIsBlank(cardNum);
		verifyStringIsBlank(cardExp);
		verifyStringIsBlank(cardCvv);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify the payment amount and email address value are still there
		String paymentAmt = inputfields.paymentAmount.getAttribute("value"); 
		String emailAdd = inputfields.emailReceipt.getAttribute("value");
		String payAmtExp = getProp("currency_symbol")
				+ addMissingZeroes(super.getProp("test_data_173"), super.getProp("currency_significant_digits"));
		// verify the fix for ticket BBPRTL-1147
		verifyTwoStringsAreEqual(paymentAmt, payAmtExp, true);
		verifyTwoStringsAreEqual(emailAdd, super.getProp("test_data_58"), true);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// because of ticket BBPRTL-1241, Make Payment is now always enabled
		assertTrue(isMakePaymentBtnEnabled(), "Make Payment button is disabled");
		// let's click the payment amount field
		clickElementAction(inputfields.paymentAmount);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		inputfields.paymentAmount.sendKeys(Keys.TAB);
		// let's verify that the processing fee is not displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's input the same card details
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
		clickElementAction(inputfields.paymentAmount);
		inputfields.paymentAmount.sendKeys(Keys.END, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		inputfields.paymentAmount.sendKeys(getProp("test_data_176"));
		// verify no processing fee is displayed
		labels = new Labels(driver, 0);
		assertFalse(isElementExists(labels.payProcessingFeeList), "The Processing Fee element is displayed!");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_176"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		// verify the fix for bug ticket BBPRTL-1426
		verifyTwoStringsAreEqual(total, "TOTAL " + totalAmt, true);
		// make sure merchant_id has correct value
		String merchantId = getMerchantId(3);
		verifyTwoStringsAreEqual(merchantId, "591bd3fad3c2d", true);
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
		crmGetListViewTableWithSearch(super.getProp("test_data_180"), true, 1, false, false);
		crmClickRecordExactLinkText(super.getProp("test_data_180"));
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
				super.getProp("test_data_180"), super.getProp("visa_pay_method"), payAmt);
		verifyTwoStringsAreEqual(respCode, "0", true);
		
		// Run a query in the DB to ensure that the records are created in the bbeng_banking_records table
		String bankingId = getBankingRecordVal("banking_id", super.getProp("test_data_180"));
		String txDate = getBankingRecordVal("tx_date", super.getProp("test_data_180"));
		String txAmount = getBankingRecordVal("tx_amount", super.getProp("test_data_180"));
		String feeAmount = getBankingRecordVal("fee_amount", super.getProp("test_data_180"));
		String feeTax = getBankingRecordVal("fee_tax", super.getProp("test_data_180"));
		String txChannel = getBankingRecordVal("tx_channel", super.getProp("test_data_180"));
		String txMethod = getBankingRecordVal("tx_method", super.getProp("test_data_180"));
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