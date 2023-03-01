package au.com.blueoak.portal.prod.make_payment.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import au.com.blueoak.portal.pageObjects.make_payment.Buttons;
import au.com.blueoak.portal.pageObjects.make_payment.InputFields;
import au.com.blueoak.portal.pageObjects.make_payment.Labels;
import au.com.blueoak.portal.pageObjects.make_payment.ProgressBar;
import au.com.blueoak.portal.prod.make_payment.AbstractMakePayment;

public class StndMakePayment extends AbstractMakePayment {
	
	/** 
	 * Initialize the page objects factory
	 * */
	InputFields inputfields;
	Buttons buttons;
	ProgressBar progressbar;
	Labels labels;
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/** 
	 * This is for logging the current
	 * test case running 
	 * */
	private String currentTestCase;
	
	@BeforeClass
	public void beforeClass() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);	
		
		accessPortal(getStandaloneUrlMakePayment(), true);	
	}
	
	@AfterClass
	public void afterClass() {
		
		logTestClassEnd(className);
	}
	
	@BeforeMethod
	public void beforeMethod(Method method) {
		
		// let's initialize the page objects
		inputfields = new InputFields(driver);
		buttons = new Buttons(driver);
		progressbar = new ProgressBar(driver);
		labels = new Labels(driver);
		
		// let's log the current test case executing
		this.currentTestCase = method.getName();
		logStartOfCurrentTestCase(currentTestCase);
	}
	
    /** 
     * Let's log the end of the current test case executing
     * */
    @AfterMethod
    public void afterMethod() {
    	
    	logEndOfCurrentTestCase(currentTestCase);
    }
	
    /** 
     * Here we will verify that error will be displayed on the fields
     * */
	@Test(priority = 1)
	public void verifyRequiredFields() {
		
		// let's check if only account number is displayed
		boolean isNextBtnDisp = isElementDisplayed(buttons.next, 0);
		
		if (isNextBtnDisp) {
			logDebugMessage("Only Account number is displayed on initial load");
			
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
			String propAcctNum = "  " + getProp("test_acct_num_01");
			inputfields.accountNumber.sendKeys(propAcctNum, Keys.TAB);
			// let's verify that the extra spaces are removed
			pauseSeleniumExecution(1000);
			String acctNum = inputfields.accountNumber.getAttribute("value");
			verifyTwoStringsAreEqualNoSpaces(acctNum, propAcctNum);
			clickElementAction(buttons.next);
			nextBtnProgBarLoad();
			
			clickElementAction(inputfields.accountNumber);
			// add a pause to fix an issue
			// where the field was not clicked
			pauseSeleniumExecution(500);
			// let's switch in the mwframe
			driver.switchTo().frame("mwIframe");
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
			// let's switch out of the mwframe
			driver.switchTo().defaultContent();
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
			driver.switchTo().frame("mwIframe");
			assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is not error state");
			assertTrue(isElementInError(inputfields.cardNumber, 2, 0), "The Card Number is not in error state");
			assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not in error state");
			assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
			// let's switch out of the mwframe
			driver.switchTo().defaultContent();
			assertTrue(isElementInError(inputfields.paymentAmount, 5, 0), "Payment Amount is not in error state");
			assertTrue(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is not in error state");
			
			// because of ticket BBPRTL-1241, Make Payment is now always enabled
			assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		} else {
			logDebugMessage("All fields are displayed on initial load");
			
			// verify the fields that should be displayed
			clickElementAction(inputfields.accountNumber);
			// add a pause to fix an issue
			// where the field was not clicked
			pauseSeleniumExecution(500);
			// let's switch in the mwframe
			driver.switchTo().frame("mwIframe");
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
			// let's switch out of the mwframe
			driver.switchTo().defaultContent();
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
			assertTrue(isElementInError(inputfields.accountNumber, 5, 0), "The Account Number is not in error state");
			// let's switch in the mwframe
			driver.switchTo().frame("mwIframe");
			assertTrue(isElementInError(inputfields.cardName, 2, 0), "The Name on Card is not error state");
			assertTrue(isElementInError(inputfields.cardNumber, 2, 0), "The Card Number is not in error state");
			assertTrue(isElementInError(inputfields.cardExpiry, 2, 0), "The Card Expiry is not in error state");
			assertTrue(isElementInError(inputfields.cardCvv, 2, 0), "The Card CVV is not in error state");
			// let's switch out of the mwframe
			driver.switchTo().defaultContent();
			assertTrue(isElementInError(inputfields.paymentAmount, 5, 0), "Payment Amount is not in error state");
			assertTrue(isElementInError(inputfields.emailReceipt, 5, 0), "Email Address is not in error state");
			
			// because of ticket BBPRTL-1241, Make Payment is now always enabled
			assertTrue(isMakePaymentBtnEnabled(), "The Make Payment button is disabled");
		}		
	}
	
	/**
	 * We will process a payment but it would be declined since
	 * we are using a test credit card against a production merchant details
	 *  */
	@Test(priority = 2)
	public void verifyMakePayment() {
		
		// let's just refresh the page to ensure we have a clean portal
		refreshBrowser(1, 2000);
		load();
		
		// let's check if only account number is displayed
		boolean isNextBtnDisp = isElementDisplayed(buttons.next, 0);
		
		if (isNextBtnDisp) {
			logDebugMessage("Only Account number is displayed on initial load");
			
			// let's put the account number to be able to proceed
			inputfields.accountNumber.sendKeys(super.getProp("test_acct_num_01"), Keys.TAB);
			clickElementAction(buttons.next);
			verifyTwoStringsAreEqual(labels.nextProgBar.getText(), "Creating secure payment connection...", true);
			nextBtnProgBarLoad();
			// let's make sure the account number has value
			verifyTwoStringsAreEqual(inputfields.accountNumber.getAttribute("value"), super.getProp("test_acct_num_01"), true);
			// input all remaining fields
			driver.switchTo().frame("mwIframe");
			inputfields.cardName.sendKeys(super.getProp("test_cc_name_mc"));
			slowSendKeys(inputfields.cardNumber, getProp("test_cc_num_mc"), true, 300);
			inputfields.cardExpiry.sendKeys(super.getProp("test_cc_exp_mc"));
			inputfields.cardCvv.sendKeys(super.getProp("test_cc_cvv_mc"));
			driver.switchTo().defaultContent();
			inputfields.paymentAmount.sendKeys(super.getProp("test_payment_amt_01"));
			inputfields.emailReceipt.sendKeys(super.getProp("test_email_add_02"));
			// let's generate an event to make sure the button
			// would be enabled when clicked
			clickElementAction(inputfields.accountNumber);
			clickElementAction(buttons.makePayment);
			// verify the text we are displaying
			verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
			makePaymentBtnProgBarLoad();
			verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment processing failed", true);
			String emailUsedRemovedSpaces = StringUtils.deleteWhitespace(super.getProp("test_email_add_02"));
			verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
					"Failed payment details has been emailed to " + emailUsedRemovedSpaces, true);
		} else {
			logDebugMessage("All fields are displayed on initial load");
			
			// input all the fields
			inputfields.accountNumber.sendKeys(super.getProp("test_acct_num_01"));
			driver.switchTo().frame("mwIframe");
			inputfields.cardName.sendKeys(super.getProp("test_cc_name_mc"));
			slowSendKeys(inputfields.cardNumber, getProp("test_cc_num_mc"), true, 300);
			inputfields.cardExpiry.sendKeys(super.getProp("test_cc_exp_mc"));
			inputfields.cardCvv.sendKeys(super.getProp("test_cc_cvv_mc"));
			driver.switchTo().defaultContent();
			inputfields.paymentAmount.sendKeys(super.getProp("test_payment_amt_01"));
			inputfields.emailReceipt.sendKeys(super.getProp("test_email_add_01"));
			// let's generate an event to make sure the button
			// would be enabled when clicked
			clickElementAction(inputfields.accountNumber);
			clickElementAction(buttons.makePayment);
			// verify the text we are displaying
			verifyTwoStringsAreEqual(labels.paymentProgBar.getText(), "Processing payment...", true);
			makePaymentBtnProgBarLoad();
			verifyTwoStringsAreEqual(labels.responseHeader.getText(), "Payment processing failed", true);
			String emailUsedRemovedSpaces = StringUtils.deleteWhitespace(super.getProp("test_email_add_01"));
			verifyTwoStringsAreEqual(labels.emailSentTo.getText(),
					"Failed payment details has been emailed to " + emailUsedRemovedSpaces, true);
		}
	}


}