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

public class AcctNumLangFiles extends MakePaymentDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	Buttons buttons;
	Header header;
	InputFields inputfields;
	ProgressBar progressbar;
	Labels labels;
	ToastMsg toastmsg;
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
		buttons = new Buttons(driver);
		header = new Header(driver);
		inputfields = new InputFields(driver);
		progressbar = new ProgressBar(driver);
		labels = new Labels(driver);
		toastmsg = new ToastMsg(driver);
	}
	
	/** 
	 * For ticket BBPRTL-207
	 * 
	 * <pre>
	 * Verify the language file for
	 * - placeholder
	 * - required error
	 * - invalid error
	 * - verify that if certain key-value does not exist in the global language file,
	 * 		it would get the key-value from the default en.json language file.
	 * </pre>
	 * */
	@Test(priority = 1)
	public void verifyLangFiles() {
		
		if (getPortalType().equals("embedded")) {
			// let's switch into the bluebilling iframe
			switchToMakePaymentEmbeddedIframe(1);
		}
		
		// put a pause because on the initial load
		// it seems the loading element could not be found
		pauseSeleniumExecution(5000);
		loadMakePayment();
		
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
			String support = getDisplayedText(labels.supportText, true);
			softAssertion.assertEquals(support,
					"Need Help? Call us on 1234 567 890, or email us at support.test@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			softAssertion.assertTrue(StringUtils.isBlank(h1),
					assertionErrorMsg(getLineNumber()));
			labels = new Labels(driver, 0);
			softAssertion.assertFalse(isElementExists(labels.supportTextList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		String acctNum = getDisplayedPlaceholder(inputfields.accountNumber, false);
		softAssertion.assertEquals(acctNum, "Test & `BlueBilling's - (dev.portal) [,:;\"]! Numero ng Akaunto",
				assertionErrorMsg(getLineNumber()));
		String nxtBtnText = getDisplayedText(buttons.next, true);
		softAssertion.assertEquals(nxtBtnText, "Sunod",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the required error label
		clickElementAction(inputfields.accountNumber);
		inputfields.accountNumber.sendKeys(Keys.TAB);
		String acctNumError = getDisplayedText(labels.hintAccountNumber, true);
		// test the fix for ticket BBPRTL-661
		verifyTwoStringsAreEqual(acctNumError, "Kinakailangan ang numero ng akaunto", true);
		
		// verify the invalid error label
		inputfields.accountNumber.sendKeys(getProp("test_data_25"), Keys.TAB);
		String acctNumInv = getDisplayedText(labels.hintAccountNumber, true);
		verifyTwoStringsAreEqual(acctNumInv, "Account number is invalid", true);
		
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
		
		// verify the progress bar text
		inputfields.accountNumber.clear();
		acctNum = inputfields.accountNumber.getAttribute("value");
		verifyStringIsBlank(acctNum);
		
		inputfields.accountNumber.sendKeys(getProp("test_data_33"), Keys.TAB);
		// click the next button
		clickElementAction(buttons.next);
		String progBarText = getDisplayedText(labels.nextProgBar, true);
		verifyTwoStringsAreEqual(progBarText, "Paglikha ng secure na koneksyon sa pagbabayad ... mangyaring maghintay.", true);
		
		nextBtnProgBarLoad();
		acctNum = inputfields.accountNumber.getAttribute("value");
		verifyTwoStringsAreEqual(acctNum, getProp("test_data_33"), true);
	}
}
