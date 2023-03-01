package au.com.blueoak.portal.dev.make_payment.tests;

import static org.testng.Assert.fail;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.make_payment.MakePaymentDevBase;
import au.com.blueoak.portal.pageObjects.make_payment.Buttons;
import au.com.blueoak.portal.pageObjects.make_payment.Header;
import au.com.blueoak.portal.pageObjects.make_payment.InputFields;
import au.com.blueoak.portal.pageObjects.make_payment.Labels;
import au.com.blueoak.portal.pageObjects.make_payment.ProgressBar;
import au.com.blueoak.portal.pageObjects.make_payment.ToastMsg;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class AllFieldsLangFiles extends MakePaymentDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	InputFields inputfields;	
	Header header;	
	Labels labels;
	Buttons buttons;
	ToastMsg toastmsg;
	ProgressBar progressbar;
	AccessS3BucketWithVfs s3Access;
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/**
	 * Let's start the test by initializing the browser
	 * using 'fil' language and open the test portal
	 *  */	
	@BeforeClass
	@Override
	public void startTest() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);
		
		super.setupTestProp();
		super.initChromeDriver("fil");
		
		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		
		// upload the language file we are using
		uploadMakePaymentGlobalLangFile(s3Access, "02\\", "fil.json");
		
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
		header = new Header(driver);
		labels = new Labels(driver);
		buttons = new Buttons(driver);
		toastmsg = new ToastMsg(driver);
		progressbar = new ProgressBar(driver);
	}

	/**
	 * For ticket BBPRTL-207
	 * 
	 * <pre>
	 * - verify that the preferred language in the browser updates
	 * the fields placeholder
	 * - verify that if certain key-value does not exist in the global language file,
	 * 		it would get the key-value from the default en.json language file.
	 * </pre>
	 */
	@Test(priority = 1)
	public void verifyPlaceholderLabels() {
		
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		
		// put a pause because on the initial load
		// it seems the loading element could not be found
		pauseSeleniumExecution(5000);
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify that the page title is update from language file
		String pageTitle = getPageTitle();
		if (getPortalType().equals("standalone")) {
			softAssertion.assertEquals(pageTitle, "Magbayad ako Test & `BlueBilling's - (dev.portal) [,:;\"]! Akaunto",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			softAssertion.assertEquals(pageTitle, "Embedded example",
					assertionErrorMsg(getLineNumber()));
		}
		// let's verify that the placeholder is updated according to preferred browser language
		String h1 = getDisplayedText(header.head1, true);
		if (getPortalType().equals("standalone")) {
			softAssertion.assertEquals(h1, "Magbayad ako Test & `BlueBilling's - (dev.portal) [,:;\"]! Akaunto",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			softAssertion.assertTrue(StringUtils.isBlank(h1),
					assertionErrorMsg(getLineNumber()));
		}
		String acctNum = getDisplayedPlaceholder(inputfields.accountNumber, false);
		softAssertion.assertEquals(acctNum, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Numero ng Akaunto",
				assertionErrorMsg(getLineNumber()));
		// get inside the mypayframe
		switchToMWIframe();
		String cardName = getDisplayedText(labels.cardName, true);
		String cardNum = getDisplayedText(labels.cardNumber, true);
		String cardExp = getDisplayedText(labels.cardExpiry, true);
		String cardCvv = getDisplayedText(labels.cardCvv, true);
		softAssertion.assertEquals(cardName, "Pangalan sa card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNum, "Numero ng card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardExp, "Pag-expire (mm/yy)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardCvv, "CVV",
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		String payAmt = getDisplayedPlaceholder(inputfields.paymentAmount, false);
		String emailField = getDisplayedPlaceholder(inputfields.emailReceipt, false);
		String makePayment = getDisplayedText(buttons.makePayment, true);
		softAssertion.assertEquals(payAmt, "Payment Amount",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailField, "Email Address (paghiwalayin ang maramihang mga email na may kuwit)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(makePayment),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			String support = getDisplayedText(labels.supportText, true);
			softAssertion.assertEquals(support,
					"Need Help? Call us on 1234 567 890, or email us at support.test@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			labels = new Labels(driver, 0);
			softAssertion.assertFalse(isElementExists(labels.supportTextList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(buttons.helpIcon);
		loadMakePayment();
		String acctNumSample = getDisplayedText(labels.acctNumSample, true);
		String acctNumSampleClose = getDisplayedText(buttons.helpIconClose, true);
		softAssertion.assertEquals(acctNumSample,
				"Ang iyong numero ng akaunto ay matatagpuan sa minarkahang posisyon sa iyong kuwenta",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNumSampleClose, "Isara",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(buttons.helpIconClose);
		// put a value in the account number so timeout message would appear
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		boolean isElementDisp = waitForElement(toastmsg.toastLoc, 94, 30);
		if (isElementDisp) {
			// let's get the toast message
			String toastMsg = getDisplayedText(toastmsg.toastLoc, true);
			// sometimes the actual seconds vs the expected seconds do not match
			// resulting for this test case to sometimes fail
			// will use contains to assert message and without asserting the seconds countdown
			verifyStringContains(true, toastMsg, "Ikaw ay kasalukuyang hindi aktibo! Awtomatiko itong i-refresh ang window na ito ");
		} else {
			fail("The Toast Timeout message was not displayed");
		}
		
		// let's process a declined payment
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
		String payProcFee = getDisplayedText(labels.payProcessingFee, true);
		String procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_47"), super.getProp("currency_significant_digits"));
		String totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_48"), super.getProp("currency_significant_digits"));
		String total = getDisplayedText(labels.payTotal, true);
		softAssertion.assertEquals(
				payProcFee, concatStrings("Ang bayad sa pagpoproseso ng ", procFeeAmt, " ay naaangkop. Isang kabuuan ",
						totalAmt, " mapoproseso."),
				assertionErrorMsg(getLineNumber()));
		// ticket BBPRTL-306 will address the issue
		// in TOTAL not having language file
		softAssertion.assertEquals(total, concatStrings("Kabuuan ", totalAmt),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// click the make payment
		clickElementAction(buttons.makePayment);
		// sometimes the initial click does not process the payment
		// so we keep on trying until the progress bar is displayed
		makePaymentBtnRetryClick(3);
		String progBarText = getDisplayedText(labels.paymentProgBar, true);
		verifyTwoStringsAreEqual(progBarText, "Pinoproseso ang pagbabayad ... mangyaring maghintay.", true);
		
		makePaymentBtnProgBarLoad();
		String header = getDisplayedText(labels.responseHeader, true);
		String mwResp = getDisplayedText(labels.declinedMwResp, true);
		String emailText = getDisplayedText(labels.emailSentTo, true);
		String tryAgainText = getDisplayedText(buttons.tryAgain, true);
		softAssertion.assertEquals(header, "Nabigo ang pagproseso ng pagbabayad",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mwResp, "Transaction declined",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailText, concatStrings("Failed payment details has been emailed to ", getProp("test_data_24")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tryAgainText, "Subukan Muli",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit the try again to do another payment
		clickElementAction(buttons.tryAgain);
		loadMakePayment();
		
		// we will process a successful payment
		inputfields.accountNumber.sendKeys(getProp("test_data_33"));
		// get inside the mypayframe
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
		payProcFee = getDisplayedText(labels.payProcessingFee, true);
		procFeeAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_44"), super.getProp("currency_significant_digits"));
		totalAmt = getProp("currency_symbol")
				+ addMissingZeroes(getProp("test_data_45"), super.getProp("currency_significant_digits"));
		total = getDisplayedText(labels.payTotal, true);
		softAssertion.assertEquals(
				payProcFee, concatStrings("Ang bayad sa pagpoproseso ng ", procFeeAmt, " ay naaangkop. Isang kabuuan ",
						totalAmt, " mapoproseso."),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-306
		softAssertion.assertEquals(total, concatStrings("Kabuuan ", totalAmt),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's generate an event to make sure the button
		// would be enabled when clicked
		clickElementAction(inputfields.accountNumber);
		// click the make payment
		clickElementAction(buttons.makePayment);
		// sometimes the initial click does not process the payment
		// so we keep on trying until the progress bar is displayed
		makePaymentBtnRetryClick(3);
		progBarText = getDisplayedText(labels.paymentProgBar, true);
		verifyTwoStringsAreEqual(progBarText, "Pinoproseso ang pagbabayad ... mangyaring maghintay.", true);
		
		makePaymentBtnProgBarLoad();
		header = getDisplayedText(labels.responseHeader, true);
		emailText = getDisplayedText(labels.emailSentTo, true);
		softAssertion.assertEquals(header, "Payment successfully processed",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailText,
				concatStrings("Ang resibo sa pagbabayad ay na-email sa ", getProp("test_data_24")),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/**
	 * For ticket BBPRTL-207
	 * 
	 * <pre>
	 * - verify that the preferred language in the browser updates
	 * the fields placeholder
	 * - verify that if certain key-value does not exist in the global language file,
	 * 		it would get the key-value from the default en.json language file.
	 * </pre>
	 */
	@Test(priority = 2)
	public void verifyRequiredLabels() {
		
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// click on the fields to generate an error on the labels
		clickElementAction(inputfields.accountNumber);
		// let's switch in the mwframe
		switchToMWIframe();
		clickElementAction(inputfields.cardName);
		clickElementAction(inputfields.cardNumber);
		clickElementAction(inputfields.cardExpiry);
		clickElementAction(inputfields.cardCvv);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(inputfields.paymentAmount);
		clickElementAction(inputfields.emailReceipt);
		// generate an event
		inputfields.emailReceipt.sendKeys(Keys.TAB);
		String acctNumError = getDisplayedText(labels.hintAccountNumber, true);
		// let's switch in the mwframe
		switchToMWIframe();
		String cardNameError = getDisplayedText(labels.hintCardName, true);
		String cardNumError = getDisplayedText(labels.hintCardNumber, true);
		String cardExpError = getDisplayedText(labels.hintCardExpiry, true);
		String cardCvvError = getDisplayedText(labels.hintCardCvv, true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		String payAmtError = getDisplayedText(labels.hintPaymentAmt, true);
		String emailAddError = getDisplayedText(labels.hintEmailAdd, true);
		// we will just click the Make Payment
		// but it should not submit
		clickElementAction(buttons.makePayment);
		// test the fix for ticket BBPRTL-661
		softAssertion.assertEquals(acctNumError, "Kinakailangan ang numero ng akaunto",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNameError, "Card holder name is required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNumError, "Kinakailangan ang numero ng card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardExpError, "Kinakailangan ang pag-expire ng card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardCvvError, "Kinakailangan ang cvv",
				assertionErrorMsg(getLineNumber()));
		String minAmt = concatStrings(getProp("currency_symbol"),
				addMissingZeroes(getProp("currency_minimum_amount"), getProp("currency_significant_digits")));
		softAssertion.assertEquals(payAmtError, concatStrings("Minimum ng ", minAmt, " kailangan!"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAddError, "Kinakailangan ang email address",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}
	
	/**
	 * For ticket BBPRTL-207
	 * 
	 * <pre>
	 * - verify that the preferred language in the browser updates
	 * the fields placeholder
	 * - verify that if certain key-value does not exist in the global language file,
	 * 		it would get the key-value from the default en.json language file.
	 * </pre>
	 */
	@Test(priority = 3)
	public void verifyInvalidLabels() {
		
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// put invalid values to generate the labels
		inputfields.accountNumber.sendKeys(getProp("test_data_25"));
		// let's switch in the mwframe
		switchToMWIframe();
		inputfields.cardName.sendKeys(getProp("test_data_27"));
		slowSendKeys(inputfields.cardNumber, getProp("test_data_28"), true, 300);
		inputfields.cardExpiry.sendKeys(getProp("test_data_29"));
		inputfields.cardCvv.sendKeys(getProp("test_data_30"));
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		inputfields.paymentAmount.sendKeys(getProp("test_data_31"));
		inputfields.emailReceipt.sendKeys(getProp("test_data_32"));
		// generate an event
		inputfields.emailReceipt.sendKeys(Keys.TAB);
		String acctNumInv = getDisplayedText(labels.hintAccountNumber, true);
		// let's switch in the mwframe
		switchToMWIframe();
		String cardNameInv = getDisplayedText(labels.hintCardName, true);
		String cardNumInv = getDisplayedText(labels.hintCardNumber, true);
		String cardExpInv = getDisplayedText(labels.hintCardExpiry, true);
		String cardCvvInv = getDisplayedText(labels.hintCardCvv, true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		String payAmtInv = getDisplayedText(labels.hintPaymentAmt, true);
		String emailAddInv = getDisplayedText(labels.hintEmailAdd, true);
		softAssertion.assertEquals(acctNumInv, "Account number is invalid",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2109
		softAssertion.assertEquals(cardNameInv, "Di-wasto ang pangalan",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNumInv, "Di-wasto ang numero ng card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardExpInv, "Di-wasto ang pag-expire ng card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardCvvInv, "Hindi wasto ang haba ng card cvv",
				assertionErrorMsg(getLineNumber()));
		String minAmt = concatStrings(getProp("currency_symbol"),
				addMissingZeroes(getProp("currency_minimum_amount"), super.getProp("currency_significant_digits")));
		softAssertion.assertEquals(payAmtInv, concatStrings("Minimum ng ", minAmt, " kailangan!"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAddInv, concatStrings("These email addresses are invalid: ", getProp("test_data_32")),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's put a credit card not part of the config
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		loadMakePayment();
		
		// let's switch in the mwframe
		switchToMWIframe();
		slowSendKeys(inputfields.cardNumber, getProp("test_data_09"), true, 300);
		inputfields.cardNumber.sendKeys(Keys.TAB);
		cardNumInv = getDisplayedText(labels.hintCardNumber, true);
		verifyTwoStringsAreEqual(cardNumInv, "Ang tinukoy na uri ng card ay hindi tinatanggap", true);
		if (getPortalType().equals("standalone")) {
			// let's switch out of the mwframe
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
	}

}
