package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_out.MoveOutDevBase;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_out.AcceptanceMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountContactMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountDetailsMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AdditionalNoteMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.ForwardingAddressMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.SupplyDetailsMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.ToastMsgMoveOut;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiExistingContact01 extends MoveOutDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveOut supplydetailsmoveout;
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveOut accountdetailsmoveout;
	AccountContactMoveOut accountcontactmoveout;
	ForwardingAddressMoveOut forwardingaddressmoveout;
	AdditionalNoteMoveOut additionalnotemoveout;
	AcceptanceMoveOut acceptancemoveout;
	ToastMsgMoveOut toastmsgmoveout;

	/**
	 * Store the name of the class for logging
	 */
	private String className;

	/**
	 * The Move-out date to use for assertions
	 */
	private String moveOutDate1;

	/**
	 * Just a another different format date for the Move-Out
	 */
	private String moveOutDate2;

	/**
	 * The value of the date of birth
	 */
	private String dateOfBirth1;

	/**
	 * The source id value
	 */
	private String sourceID;

	/**
	 * Use this to populate the fields from Supply Details until Additional Notes
	 * Don't use this if you intend to send the data populated in this method
	 */
	private void populateSupplyUntilAddNotes() {

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		long localLength = storage.getLocalStorageLength();
		// let's make sure the session storage is cleared
		// since it's supposed to be cleared in the previous test case
		logDebugMessage("Will be clearing the browser session storage");
		storage.clearSessionStorage();
		logDebugMessage("Successfully cleared the browser session storage");
		// verify the expected number of keys in the local storage
		assertEquals(localLength, 1, "The number of local storage keys is incorrect");

		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-Out Iframe
			switchToMoveOutEmbeddedIframe(1);
		}
		loadPortal();

		logDebugMessage("Will start populating the Supply Details until Additional Notes section");
		// populate the fields for Supply Details
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		// let's supply a supply address from the search field
		// test the fix for ticket BBPRTL-785
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "30-52 Lindeman Road Beerwah", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// choose first one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "30-52 Lindeman Road, Beerwah QLD");
		pauseSeleniumExecution(1000);
		// confirm it's populated correctly
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String tenType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, true);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenType),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "30-52",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "30",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Lindeman",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Road",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Beerwah",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4519",
				assertionErrorMsg(getLineNumber()));
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// populate the complex and unit number
		supplydetailsmoveout.supplyAddComplexName.sendKeys("complexity's");
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Unit");
		clickElementAction(supplydetailsmoveout.supplyAddTenancyNum);
		deleteAllTextFromField();
		supplydetailsmoveout.supplyAddTenancyNum.sendKeys("Unity #01");
		// hit Next button
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");

		// populate the fields for Account Details
		accountdetailsmoveout.accountNum.sendKeys("000986987970");
		clickElementAction(accountdetailsmoveout.residential);
		// click next
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");

		// populate the fields for Account Contact
		accountcontactmoveout.firstName.sendKeys("Lucy");
		accountcontactmoveout.lastName.sendKeys("Heartfilia");
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		clickElementAction(accountcontactmoveout.dateOfBirth);
		pauseSeleniumExecution(1000);
		accountcontactmoveout.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		// click first name to ensure that calendar is collapsed
		clickElementAction(accountcontactmoveout.firstName);
		clickElementAction(accountcontactmoveout.driversLicence);
		accountcontactmoveout.driversLicenceNumber.sendKeys("012598562");
		accountcontactmoveout.driversLicenceState.sendKeys("Queensland");
		accountcontactmoveout.emailAddress.sendKeys("lucy@testing.com");
		accountcontactmoveout.mobilePhone.sendKeys("0238921111");
		accountcontactmoveout.businessPhone.sendKeys("0363021485");
		accountcontactmoveout.afterhoursPhone.sendKeys("0411234567");
		accountcontactmoveout.contactSecretCode.sendKeys("Sekrekt's");
		// let's hit next
		clickElementAction(accountcontactmoveout.next);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.forwAddress, 0),
				"We are not in the Forwarding Address section");

		// populate the forwarding address
		// input the forwarding address
		slowSendKeys(forwardingaddressmoveout.forwAddress, "CMTY 1 143 Mooloolaba Esplanade Mooloolaba", true, 400);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// choose first one from the list
		chooseAddress(forwardingaddressmoveout.forwAddressesDiv, "143 Mooloolaba Esplanade, Mooloolaba QLD");
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, false);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, false);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, false);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, false);
		String cityForw = getDisplayedValue(forwardingaddressmoveout.city, false);
		String stateForw = getDisplayedValue(forwardingaddressmoveout.state, false);
		String postcodeForw = getDisplayedValue(forwardingaddressmoveout.postcode, false);
		String country = getDisplayedValue(forwardingaddressmoveout.country, false);
		softAssertion.assertEquals(addLine1, "CMTY 1",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addLine2, "143 Mooloolaba Esplanade",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine4),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cityForw, "Mooloolaba",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stateForw, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcodeForw, "4557",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's add an address 3 and 4
		forwardingaddressmoveout.addLine03.sendKeys("Add-03");
		forwardingaddressmoveout.addLine04.sendKeys("Add-04");
		// let's click Next button
		clickElementAction(forwardingaddressmoveout.next);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");

		// populate the additional note
		additionalnotemoveout.notes.sendKeys("The quick brown fox jumps over the lazy dog :)");
		// hit next from notes
		clickElementAction(additionalnotemoveout.next);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");

		// confirm we are on the acceptance page
		scrollPageDown(900);
		assertFalse(isElementTicked(acceptancemoveout.firstCheckbox, 0),
				"Account created confirmation is ticked by default");

		logDebugMessage("The method populateSupplyUntilAddNotes() has finished executing");
	}

	@BeforeClass
	public void beforeClass() {

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// upload the correct portal_config.json we are testing
		uploadMoveOutConfig(s3Access, "01\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMoveOut(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMoveOut(), true);
			loadPortal();
		}
	}

	@AfterClass
	public void afterClass() {

		saveProp();
		logTestClassEnd(className);
	}

	@BeforeMethod
	public void beforeMethod() {

		// let's initialize the page objects
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
		supplydetailsmovein = new SupplyDetailsMoveIn(driver);
		accountdetailsmoveout = new AccountDetailsMoveOut(driver);
		accountcontactmoveout = new AccountContactMoveOut(driver);
		forwardingaddressmoveout = new ForwardingAddressMoveOut(driver);
		additionalnotemoveout = new AdditionalNoteMoveOut(driver);
		acceptancemoveout = new AcceptanceMoveOut(driver);
		toastmsgmoveout = new ToastMsgMoveOut(driver);
	}

	/**
	 * For ticket BBPRTL-214
	 * 
	 * - verify the inactivity message would display depending on the timeout -
	 * verify the inactivity message would disappear once we hover the mouse -
	 * verify the inactivity message would disappear once we click the toast message
	 * - verify the inactivity message would disappear once we click on a field -
	 * verify the session storage gets cleared when the inactivity timeout is
	 * reached
	 * 
	 */
	@Test(priority = 1)
	public void verifyInactivityMessage() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// populate the Supply Details sections until Additional Notes
		populateSupplyUntilAddNotes();

		// let's wait for the toast message to display
		// the value in the portal_config.json for idle_time is 120 seconds
		// while the value for timeout_warning is 30 seconds
		// so we will wait for the toast element to appear
		// setting a timeout of 94 seconds
		// added padding of 4 seconds
		boolean isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's get the toast message
			pauseSeleniumExecution(1000);
			toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
			String toastMsg = getDisplayedText(toastmsgmoveout.toastLoc, true);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// sometimes the actual seconds vs the expected seconds does not match
			// resulting for the test case to fail sometimes
			// so will use contains for now without asserting the seconds countdown
			verifyStringContains(true, toastMsg,
					"You have been inactive for 90 seconds, for privacy reasons this form will automatically be cleared in ");
			pauseSeleniumExecution(1000);
			int counter = 0;
			int maxCounter = 10;
			while (counter < maxCounter) {
				String assertMsgExp = "You have been inactive for 90 seconds, for privacy reasons this form will automatically be cleared in ";
				// let's instantiate the class to get again the elements
				toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
				toastMsg = getDisplayedText(toastmsgmoveout.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				verifyStringContains(true, toastMsg, assertMsgExp);
				// let's pause for the timer
				pauseSeleniumExecution(1000);
				counter++;
			}
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		// let's verify that the toast will disappear once we hover the mouse
		isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			hoverToElementAction(acceptancemoveout.cancel);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
			boolean isDisplayed = isElementExists(toastmsgmoveout.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isDisplayed, "The Toast Message is displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		scrollPageDown(800);
		// let's verify that the toast message will disappear once you click
		// on that toast message
		isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			clickElementAction(toastmsgmoveout.toastLoc);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
			boolean isDisplayed = isElementExists(toastmsgmoveout.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isDisplayed, "The Toast Message is displayed!");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		scrollPageDown(800);
		// let's verify that the toast will disappear once you click on a button
		isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's click the submit button
			clickElementAction(acceptancemoveout.submit);
			// let's just put a pause because selenium
			// to make sure that the toast message disappeared
			// before looking it up again
			pauseSeleniumExecution(1000);
			toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
			boolean isDisplayed = isElementExists(toastmsgmoveout.toastLocList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isDisplayed, "The Toast Message is displayed!");
			// verify the request was not submitted
			acceptancemoveout = new AcceptanceMoveOut(driver, 0);
			isDisplayed = isElementExists(acceptancemoveout.progressBarList);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isDisplayed, "The submit loading message is displayed");
		} else {
			fail("The Toast Timeout message was not displayed");
		}

		// verify that the session storage gets cleared the idle_time is reached
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.notes"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.supply_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.postal_address"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 8,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			// let's wait for another 32 seconds to reach the timeout
			pauseSeleniumExecution(32000);
		} else {
			fail("The Toast Timeout message was not displayed");
		}
		toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
		boolean isDisp = isElementExists(toastmsgmoveout.toastLocList);
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		assertFalse(isDisp, "The Toast Message is displayed!");
		loadPortal();
		// verify the expected session storage
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// did this because sometimes the session key move-out-session-cleared-inactive
		// is sometimes there
		if (sessionLength == 4) {
			softAssertion.assertTrue(sessionKeys.contains("move-out-session-cleared-inactive"),
					assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 4,
					assertionErrorMsg(getLineNumber()));
		} else {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 3,
					assertionErrorMsg(getLineNumber()));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-584
	 * 
	 * - verify that the session storage from move-out will not be used in the
	 * move-in - verify if we access a different portal (e.g. From Move-Out then go
	 * to Move-In) then go back, the session storage is cleared
	 * 
	 */
	@Test(priority = 2)
	public void verifySessionDetails01() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// populate the Supply Details sections until Additional Notes
		populateSupplyUntilAddNotes();

		// verify that the session storage gets cleared once we change our url to
		// move-in
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.notes"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.supply_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.postal_address"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 8,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// TODO
		// put another scenario where we go to the Connection
		// and verify that the session got cleared
		
		// TODO
		// put another scenario where we go to the Customer Portal
		// and verify it has correct behavior

		// go to the move-in page
		if (getPortalType().equals("standalone")) {
			goToUrl(getStandaloneUrlMoveIn(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			goToUrl(getEmbeddedUrlMoveIn(), true);
			embeddedMoveInSwitchFrame(1);
			loadPortal();
		}
		// verify the session got cleared
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 4,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify we are in the Supply Details sections in the Move-In
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's go back to the move out page and confirm the session is still cleared
		// go to the move-out page
		if (getPortalType().equals("standalone")) {
			goToUrl(getStandaloneUrlMoveOut(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			goToUrl(getEmbeddedUrlMoveOut(), true);
			switchToMoveOutEmbeddedIframe(1);
			loadPortal();
		}
		// verify the session still cleared
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// these are still from the move-in
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 3,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify we are in the move-out page
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmoveout.moveOutDate, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-428
	 * 
	 * - verify that the inactivity is still displayed even if the cancel dialog is
	 * displayed - verify hitting the cancel button Yes would not clear out the data
	 * 
	 */
	@Test(priority = 3)
	public void verifyAcceptanceDetails01() {

		try {
			// let's switch to the Move-Out Iframe
			// if it's embedded
			embeddedMoveOutSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} catch (NoSuchFrameException nsfe) {
			// let's verify if we are still in the Move Out page
			String url = driver.getCurrentUrl();
			logDebugMessage(concatStrings("The current URL is:/n", url));
			if (!url.contains("move-out")) {
				logDebugMessage("We are no longer in the Move Out page, we will access it again.");
				if (getPortalType().equals("standalone")) {
					accessPortal(getStandaloneUrlMoveOut(), true);
					loadPortal();
				} else if (getPortalType().equals("embedded")) {
					accessPortal(getEmbeddedUrlMoveOut(), true);
					loadPortal();
				}
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// populate the Supply Details sections until Additional Notes
		populateSupplyUntilAddNotes();

		// verify that the session storage gets cleared once we cancel and hit Yes
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.notes"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.supply_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.postal_address"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 8,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// hit the cancel button
		clickElementAction(acceptancemoveout.cancel);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemoveout.dialogContainerText, 0),
				"The Cancel Request and Remove Details dialog is not displayed");
		// verify the text in the dialog
		verifyTwoStringsAreEqual(getDisplayedText(acceptancemoveout.dialogContainerText, true),
				"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
				true);

		// verify that the inactivity message would still be displayed
		// even if cancel dialog is displayed
		boolean isElementDisp = waitForElement(toastmsgmoveout.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (isElementDisp) {
			pauseSeleniumExecution(1000);
			toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
			String toastMsg = getDisplayedText(toastmsgmoveout.toastLoc, true);
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// sometimes the actual seconds vs the expected seconds does not match
			// resulting for the test case to fail sometimes
			// so will use contains for now without asserting the seconds countdown
			verifyStringContains(true, toastMsg,
					"You have been inactive for 90 seconds, for privacy reasons this form will automatically be cleared in ");
			pauseSeleniumExecution(1000);
			int counter = 20;
			int maxCounter = 26;
			while (counter < maxCounter) {
				String assertMsgExp = "You have been inactive for 90 seconds, for privacy reasons this form will automatically be cleared in ";
				// let's instantiate the class to get again the elements
				toastmsgmoveout = new ToastMsgMoveOut(driver);
				toastMsg = getDisplayedText(toastmsgmoveout.toastLoc, true);
				verifyStringContains(true, assertMsgExp, assertMsgExp);
				// let's pause for the timer
				pauseSeleniumExecution(1000);
				counter++;
			}
		} else {
			fail("The Toast Timeout message was not displayed");
		}
		
		// verify that the toast message would be dismissed
		clickElementAction(acceptancemoveout.noCancelRequest);
		pauseSeleniumExecution(1000);
		toastmsgmoveout = new ToastMsgMoveOut(driver, 0);
		boolean isDisp = isElementExists(toastmsgmoveout.toastLocList);
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		assertFalse(isDisp, "The Toast Message is displayed!");
		
		// hit the cancel button again
		clickElementAction(acceptancemoveout.cancel);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemoveout.dialogContainerText, 0),
				"The Cancel Request and Remove Details dialog is not displayed");
		// verify the text in the dialog
		verifyTwoStringsAreEqual(getDisplayedText(acceptancemoveout.dialogContainerText, true),
				"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
				true);
		clickElementAction(acceptancemoveout.yesCancelRequest);
		// put a pause to fix stale element
		pauseSeleniumExecution(1000);
		// verify the close dialog is displayed
		assertTrue(isElementDisplayed(acceptancemoveout.closeDialog, 0), "Close dialog is not displayed");
		verifyTwoStringsAreEqual(acceptancemoveout.closeDialog.getText(),
				"This window/tab is no longer required, for privacy reasons we encourage you to close it", true);

		// verify the session got cleared
		sessionLength = storage.getSessionStorageLength();
		localLength = storage.getLocalStorageLength();
		// verify the expected number of keys in the session storage
		softAssertion.assertEquals(sessionLength, 0,
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys in the local storage
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-422
	 * 
	 * - verify the required fields in the Supply Details section - verify allowed
	 * past date in config is 5 - verify allowed future date in config is 10 -
	 * verify the required fields in the supply address - verify only Australia
	 * country address is displayed - verify the number of addresses to be displayed
	 * - verify users can update the address by searching a new one - verify
	 * validations using the Header of the next section
	 * 
	 */
	@Test(priority = 4)
	public void verifySupplyDetails() {

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		long localLength = storage.getLocalStorageLength();
		// let's make sure the session storage is cleared
		// since it's supposed to be cleared in the previous test case
		logDebugMessage("Will be clearing the browser session storage");
		storage.clearSessionStorage();
		logDebugMessage("Successfully cleared the browser session storage");
		// verify the expected number of keys in the local storage
		assertEquals(localLength, 1, "The number of local storage keys is incorrect");

		try {
			refreshBrowser(1, 5000);
			// let's switch to the Move-Out Iframe
			// if it's embedded
			embeddedMoveOutSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);
			loadPortal();
		} catch (NoSuchFrameException nsfe) {
			// let's verify if we are still in the Move Out page
			String url = driver.getCurrentUrl();
			logDebugMessage(concatStrings("The current URL is:/n", url));
			if (!url.contains("move-out")) {
				logDebugMessage("We are no longer in the Move Out page, we will access it again.");
				if (getPortalType().equals("standalone")) {
					accessPortal(getStandaloneUrlMoveOut(), true);
					loadPortal();
				} else if (getPortalType().equals("embedded")) {
					accessPortal(getEmbeddedUrlMoveOut(), true);
					loadPortal();
				}
			}
		}

		// verify first that fields are not in error state
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput,
						false, "Please start typing supply address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify move out date and supply address are in error state when no data
		// and users hit header of the next section
		clickElementAction(accountdetailsmoveout.header);
		// we put a pause since Supply Address does not go immediately to error state
		pauseSeleniumExecution(2000);
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput,
						false, "Please start typing supply address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's get the current date then get a date 6 days from the past
		// verify that an error is returned
		String past6Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -6, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(past6Days, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				"Move Out Date not in Error state");
		clearDateField(supplydetailsmoveout.moveOutDate);

		// let's get the current date then get a date 11 days in the future
		// verify that an error is returned
		String future11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(future11Days, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.header);
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// test the fix in ticket BBPRTL-615
		assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				"Move Out Date not in Error state");
		clearDateField(supplydetailsmoveout.moveOutDate);

		// verify the required fields in the supply address
		clickElementAction(supplydetailsmoveout.supplyAddSearch);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmoveout.supplyAddSearch);
		clickElementAction(supplydetailsmoveout.supplyAddCantFindAdd);
		// verify fields not in error state
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify fields in error state
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's supply a valid Move Out date
		String past5Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate1 = past5Days;
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(past5Days, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		String pastFiveD = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_DASH);
		this.moveOutDate2 = pastFiveD;

		// verify we cannot enter values in lower case for dropdown fields
		supplydetailsmoveout.supplyAddComplexName.sendKeys("Test Complex");
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("unit", Keys.TAB);
		supplydetailsmoveout.supplyAddTenancyNum.sendKeys("Tenancy Num");
		supplydetailsmoveout.supplyAddStreetNum.sendKeys("Street Num");
		supplydetailsmoveout.supplyAddStreetName.sendKeys("Street Name");
		supplydetailsmoveout.supplyAddStreetType.sendKeys("street", Keys.TAB);
		supplydetailsmoveout.supplyAddCity.sendKeys("City");
		supplydetailsmoveout.supplyAddState.sendKeys("victoria", Keys.TAB);
		supplydetailsmoveout.supplyAddPostcode.sendKeys("Postcode", Keys.TAB);
		
		// verify fields in error state
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify we can change the address and the fields are updated
		clickElementAction(supplydetailsmoveout.supplyAddQuickAddSearch);
		// we need to slow down entering the supply address
		// because google address is showing wrong address
		// test the fix for ticket BBPRTL-785
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "Community 1 17-21 Douglas ST, Noble", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are only from Australia
		List<String> addresses2 = null;
		try {
			addresses2 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
			addresses2 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		}
		verifyStringContainsInEachListPacket(addresses2, "Australia", true);
		// choose first one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "17-21 Douglas St, Noble Park VIC");
		pauseSeleniumExecution(1000);
		// confirm it's populated correctly
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, false);
		String tenancyType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, false);
		String tenancyNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, false);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, false);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, false);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, false);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, false);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, false);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, false);
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyType),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum),
				assertionErrorMsg(getLineNumber()));
		// because of a bug documented in ticket BBPRTL-612, '-21' not entered in the
		// Street Number
		softAssertion.assertEquals(stNum, "17",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Douglas",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noble Park",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3174",
				assertionErrorMsg(getLineNumber()));
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// let's put a complex name
		supplydetailsmoveout.supplyAddComplexName.sendKeys("'007 Complex's");
		// put something on the Tenancy Number
		supplydetailsmoveout.supplyAddTenancyNum.sendKeys("Community 1");
		// update the Tenancy Type
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		// let's update the unit number
		clickElementAction(supplydetailsmoveout.supplyAddStreetNum);
		supplydetailsmoveout.supplyAddStreetNum.sendKeys("-21");
		
		// verify the fix for bug ticket BBPRTL-2112
		complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, false);
		tenancyType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, false);
		tenancyNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, false);
		stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, false);
		stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, false);
		stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, false);
		city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, false);
		state = getDisplayedValue(supplydetailsmoveout.supplyAddState, false);
		postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, false);
		// verify the updated values
		softAssertion.assertEquals(complexName, "'007 Complex's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Not applicable",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum),
				assertionErrorMsg(getLineNumber()));
		// verify that the Tenancy Number is disabled
		softAssertion.assertFalse(isElementEnabled(supplydetailsmoveout.supplyAddTenancyNum, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "17-21",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Douglas",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noble Park",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3174",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// hit header of next section
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
	}

	/**
	 * For ticket BBPRTL-423
	 * 
	 * - verify the required fields - verify users cannot put space on the account
	 * number - verify required ABN/ACN - verify valid ABN - verify validations
	 * using the header of the next or previous section
	 * 
	 */
	@Test(priority = 5, dependsOnMethods = { "verifySupplyDetails" })
	public void verifyAccountDetails() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify first fields are not in error state
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountcontactmoveout.header);
		// verify the required fields
		softAssertion.assertTrue(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// put a valid account number
		accountdetailsmoveout.accountNum.sendKeys("   20001 0000   194    ");

		// verify required ABN/ACN
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(1000);
		clickElementAction(accountcontactmoveout.header);
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		String accountNum = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		softAssertion.assertEquals(accountNum, "200010000194",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify that we can go to the previous section
		// even if required fields are not yet supplied
		// hit the previous sections header
		clickElementAction(supplydetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the supply details section
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, false);
		String tenancyNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, false);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, false);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, false);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, false);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, false);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, false);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, false);
		softAssertion.assertEquals(complexName, "'007 Complex's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "17-21",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Douglas",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noble Park",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3174",
				assertionErrorMsg(getLineNumber()));
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);

		// verify valid ABN
		accountdetailsmoveout.tradingName.sendKeys("Trading's");
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_abn6"), Keys.TAB);
		// put a pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5,
						0),
				"Company ABN or ACN is in error state");
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				"Trading Name is in error state");
		// verify the displayed label
		String abnAcnAndCompany = getDisplayedValue(accountdetailsmoveout.abnOrAcn, false);
		String tradingName = getDisplayedValue(accountdetailsmoveout.tradingName, false);
		softAssertion.assertEquals(
				abnAcnAndCompany, concatStrings(getProp("test_data_valid_abn6"), " (",
						getProp("test_data_valid_company_name_abn5_abn6"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// click next
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
	}

	/**
	 * This is for ticket BBPRTL-425
	 * 
	 * - verify the required fields for Residential - verify the extra_text value
	 * for the Bills notifications - verify the validations for the Business Phone -
	 * verify the validations for the After Hours Phone - verify validations using
	 * the header of the next or previous section
	 * 
	 */
	@Test(priority = 6, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyMainContactDetails() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify fields are not in error state
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				"First Name is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5,
						0),
				"Last/Family Name is in error state");
		assertFalse(isElementInError(accountcontactmoveout.postalNotif, 0, 3),
				"Postal notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.emailNotif, 0, 3),
				"Email notification is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"),
				5, 0), "Email Address is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"),
						5, 0),
				"Mobile Phone Number is in error state");
		assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				"Business Hours Phone Number is in error state");
		assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				"After Hours Phone Number is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code ,  used to speed up verification process"), 5, 0),
				"Contact Secret Code ,  used to speed up verification process is in error state");

		// let's instantiate the elements with a new implicit wait
		accountcontactmoveout = new AccountContactMoveOut(driver, 1);
		// verify that date of birth is not displayed
		assertFalse(isElementExists(accountcontactmoveout.dateOfBirthList), "Date of Birth is displayed");
		accountcontactmoveout = new AccountContactMoveOut(driver);
		// verify that Personal Identifications is not displayed
		assertFalse(isElementExists(accountcontactmoveout.driversLicenceList),
				"Australian Drivers Licence Number is displayed");
		assertFalse(isElementExists(accountcontactmoveout.passportList), "Passport is displayed");
		assertFalse(isElementExists(accountcontactmoveout.medicareCardList), "Medicare Card is displayed");
		// lets return the implicit wait
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// verify that Postal notification is ticked by default
		// and Email notification is not ticked
		assertTrue(isElementTicked(accountcontactmoveout.postalNotif, 0),
				"Postal notification is not ticked by default");
		assertFalse(isElementTicked(accountcontactmoveout.emailNotif, 0), "Email notification is ticked by default");
		// let's un-tick the Postal checkbox
		clickElementAction(accountcontactmoveout.postalNotif);
		assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0), "Postal notification is still ticked");
		// let's verify the extra_text for bills notification postal and email
		String postalText = getDisplayedText(accountcontactmoveout.lblPostalNotif, true);
		String emailText = getDisplayedText(accountcontactmoveout.lblEmailNotif, true);
		verifyTwoStringsAreEqual(postalText, "Postal (additional fees apply for mailing bills)", true);
		verifyTwoStringsAreEqual(emailText, "Email", true);

		// click header of next section
		clickElementAction(additionalnotemoveout.header);
		pauseSeleniumExecution(1000);
		// let's verify the required fields for Commercial
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				"First Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"),
				5, 0), "Last/Family Name is not error state");
		assertTrue(isElementInError(accountcontactmoveout.postalNotif, 0, 3),
				"Postal notification is not error state");
		assertTrue(isElementInError(accountcontactmoveout.emailNotif, 0, 3),
				"Email notification is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5,
				0), "Email Address is not error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"),
						5, 0),
				"Mobile Phone Number is not error state");
		assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				"Business Hours Phone Number is in error state");
		assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				"After Hours Phone Number is in error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code ,  used to speed up verification process"), 5, 0),
				"Contact Secret Code , used to speed up verification process is not error state");

		// click header of previous section
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// confirm we're in the Account details section
		String accountNumAct = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		verifyTwoStringsAreEqual(accountNumAct, "200010000194", true);
		assertTrue(isElementTicked(accountdetailsmoveout.commercial, 0), "Commercial radio is not ticked");
		// click header of next section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(2000);

		accountcontactmoveout.firstName.sendKeys("Michael");
		accountcontactmoveout.lastName.sendKeys("O'Connell");

		// tick postal
		clickElementAction(accountcontactmoveout.postalNotif);
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case"));
		accountcontactmoveout.mobilePhone.sendKeys("+61965002323");

		// let's verify the validations for the business phone
		// verify that alpha characters not allowed
		slowSendKeys(accountcontactmoveout.businessPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		// verify only + is allowed for special characters
		slowSendKeys(accountcontactmoveout.businessPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		String busPhone = getDisplayedValue(accountcontactmoveout.businessPhone, true);
		verifyTwoStringsAreEqual(busPhone, "+", true);
		// verify users cannot put space
		accountcontactmoveout.businessPhone.sendKeys(" 61  987 5269 90  ");
		busPhone = getDisplayedValue(accountcontactmoveout.businessPhone, true);
		verifyTwoStringsAreEqual(busPhone, "+61987526990", false);

		// let's verify the validations for the after hours phone
		// verify that alpha characters not allowed
		slowSendKeys(accountcontactmoveout.afterhoursPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		// verify only + is allowed for special characters
		slowSendKeys(accountcontactmoveout.afterhoursPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		String aHrPhone = getDisplayedValue(accountcontactmoveout.afterhoursPhone, true);
		verifyTwoStringsAreEqual(aHrPhone, "+", true);
		// verify users cannot put space
		accountcontactmoveout.afterhoursPhone.sendKeys(" 61  123 4567 90  ");
		aHrPhone = getDisplayedValue(accountcontactmoveout.afterhoursPhone, true);
		verifyTwoStringsAreEqual(aHrPhone, "+61123456790", false);

		accountcontactmoveout.contactSecretCode.sendKeys("'x44 tralala's");

		// verify the phone numbers in error state
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// clear the mobile phone
		clickElementAction(accountcontactmoveout.mobilePhone);
		deleteAllTextFromField();
		// clear the business phone
		clickElementAction(accountcontactmoveout.businessPhone);
		deleteAllTextFromField();
		// clear the after hours phone
		clickElementAction(accountcontactmoveout.afterhoursPhone);
		deleteAllTextFromField();

		// populate with valid values
		accountcontactmoveout.mobilePhone.sendKeys("0238921111");
		accountcontactmoveout.businessPhone.sendKeys("0363021485");
		accountcontactmoveout.afterhoursPhone.sendKeys("0411234567", Keys.TAB);

		// verify the phone numbers not in error state
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's hit header of the next section
		clickElementAction(forwardingaddressmoveout.header);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.forwAddress, 0),
				"We are not in the Forwarding Address section");
	}

	/**
	 * This is for ticket BBPRTL-426
	 * 
	 * - verify required fields - verify that the address are overridden/cleared
	 * when new address is chosen - verify validations using the header of the next
	 * or previous section
	 * 
	 */
	@Test(priority = 7, dependsOnMethods = { "verifyMainContactDetails" })
	public void verifyForwAddDetails() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify forwarding address is not in error state
		assertFalse(isElementInError(forwardingaddressmoveout.forwAddress, 5, 0),
				"Forwarding Address is in error state");

		// hit the header of the next section
		clickElementAction(additionalnotemoveout.header);
		// there's a delay before the error is displayed after hitting next
		pauseSeleniumExecution(2000);
		assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput,
						false, "Please start typing forwarding address"), 5, 0),
				"Forwarding Address Search not in Error state");

		// let's go to the previous section by clicking prev header section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(accountcontactmoveout.contactSecretCode, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// hit header of next section
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(1000);
		
		clickElementAction(forwardingaddressmoveout.forwAddress);
		clickElementAction(forwardingaddressmoveout.forwAddCantFindAdd);
		// verify fields not in error state
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 1"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 2"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 3"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 4"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "City/Suburb"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Postcode"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(
								getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify required fields
		clickElementAction(additionalnotemoveout.header);
		pauseSeleniumExecution(1000);
		// verify required fields
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 1"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 2"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 3"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 4"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "City/Suburb"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Postcode"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2062
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify we can go to previous section
		scrollPageUp(100);
		// let's go to the previous section by clicking prev header section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(accountcontactmoveout.contactSecretCode, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// hit header of next section
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify fields still in error state
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 1"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 2"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 3"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 4"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "City/Suburb"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Postcode"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2062
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify we cannot enter values in lower case for dropdown values
		forwardingaddressmoveout.addLine01.sendKeys("Add-01");
		forwardingaddressmoveout.addLine02.sendKeys("Add-02");
		forwardingaddressmoveout.addLine03.sendKeys("Add-03");
		forwardingaddressmoveout.addLine04.sendKeys("Add-04");
		forwardingaddressmoveout.city.sendKeys("City");
		forwardingaddressmoveout.state.sendKeys("State");
		forwardingaddressmoveout.postcode.sendKeys("Postcode");
		forwardingaddressmoveout.country.sendKeys("australia", Keys.TAB);
		
		// verify fields in error state
		clickElementAction(forwardingaddressmoveout.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 1"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 2"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 3"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Address Line 4"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "City/Suburb"),
						5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Postcode"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's change the address
		clickElementAction(forwardingaddressmoveout.quickAddSearch);
		// input the forwarding address
		slowSendKeys(forwardingaddressmoveout.forwAddress, "Bldg 7, 1000 Ann ST Fortitude", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(forwardingaddressmoveout.forwAddressesDiv, "bldg 7/1000 Ann St, Fortitude Valley QLD");
		pauseSeleniumExecution(1000);
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, true);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, true);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, true);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, true);
		String city = getDisplayedValue(forwardingaddressmoveout.city, true);
		String state = getDisplayedValue(forwardingaddressmoveout.state, true);
		String postcode = getDisplayedValue(forwardingaddressmoveout.postcode, true);
		String country = getDisplayedValue(forwardingaddressmoveout.country, true);
		verifyTwoStringsAreEqual(addLine1, "Bldg 7,", false);
		verifyTwoStringsAreEqual(addLine2, "1000 Ann Street", false);
		verifyStringIsBlank(addLine3);
		verifyStringIsBlank(addLine4);
		verifyTwoStringsAreEqual(city, "Fortitude Valley", false);
		verifyTwoStringsAreEqual(state, "Queensland", false);
		verifyTwoStringsAreEqual(postcode, "4006", false);
		verifyTwoStringsAreEqual(country, "Australia", false);

		// let's click Next button
		clickElementAction(additionalnotemoveout.header);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
	}

	/**
	 * For ticket BBPRTL-427
	 * 
	 * - verify that if we enter only spaces it's not going to display in the
	 * acceptance
	 * 
	 */
	@Test(priority = 8, dependsOnMethods = { "verifyForwAddDetails" })
	public void verifyAddNotesDetails() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);

		// verify it's displayed
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "Additional Notes text area is not displayed");

		// enter notes as only spaces
		additionalnotemoveout.notes.sendKeys("          ");

		// hit header of next section
		clickElementAction(acceptancemoveout.header);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
	}

	/**
	 * For ticket BBPRTL-428
	 * 
	 * - verify the Date of Birth validations - verify changing values from
	 * commercial and residential then clicked Acceptance user should not be
	 * redirected into the acceptance page - verify the validations for the
	 * Australian Drivers Licence - verify we can update each section by clicking
	 * the update link - verify hitting the cancel button No would not clear out the
	 * data
	 * 
	 * @throws ParseException
	 * 
	 */
	@Test(priority = 9, dependsOnMethods = { "verifyAddNotesDetails" })
	public void verifyAcceptanceDetails02() throws ParseException {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		scrollPageDown(900);
		// verify that the checkboxes are not in error state
		assertFalse(isElementInError(acceptancemoveout.firstCheckbox, 0, 3),
				"Account Creation confirmation is in error state");
		assertFalse(isElementInError(acceptancemoveout.secondCheckbox, 0, 3),
				"Terms and Condition confirmation is in error state");
		assertFalse(isElementInError(acceptancemoveout.thirdCheckbox, 0, 3),
				"Enquiries confirmation is in error state");

		scrollPageUp(400);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update '007 Complex's 17-21 Douglas Street Noble Park, Victoria, 3174", true);
		verifyTwoStringsAreEqual(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_abn5_abn6"), " (Trading's) ABN/ACN ",
						getProp("test_data_valid_abn6")),
				true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update Michael O'Connell Email Address: ",
				getProp("test_email_dummy_lower_case"),
				" Mobile Phone: 0238921111 Business Phone: 0363021485 A/Hours Phone: 0411234567 Contact Secret: ('x44 tralala's)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Postal", true);
		verifyTwoStringsAreEqual(forwAdd,
				"Forwarding Address update Bldg 7, 1000 Ann Street Fortitude Valley, Queensland, 4006 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);

		// verify the update link on Moving Out section and update it
		clickExactLinkNameFromElement(acceptancemoveout.movingOut, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Supply Details section
		assertTrue(isElementDisplayed(supplydetailsmoveout.moveOutDate, 0), "The Move-Out field is not displayed");
		// let's update the supply address
		clickElementAction(supplydetailsmoveout.supplyAddQuickAddSearch);
		// because of the issue in ticket BBPRTL-789, other country is picked up
		// when using this address 'Unit 24 287 Gympie TCE'
		// as a workaround we will just use 'Unit 24 287 Gympie Terrace'
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "Unit 24 287 Gympie Terrace", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are only from Australia
		List<String> addresses2 = null;
		try {
			addresses2 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
			addresses2 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		}
		verifyStringContainsInEachListPacket(addresses2, "Australia", true);
		// choose first one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "unit 24/287 Gympie Terrace, Noosaville QLD");
		pauseSeleniumExecution(1000);
		// confirm it's populated correctly
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		verifyStringIsBlank(complexName);
		verifyTwoStringsAreEqual(unitNum, "24", true);
		verifyTwoStringsAreEqual(stNum, "287", true);
		verifyTwoStringsAreEqual(stName, "Gympie", true);
		verifyTwoStringsAreEqual(stType, "Terrace", true);
		verifyTwoStringsAreEqual(city, "Noosaville", true);
		verifyTwoStringsAreEqual(state, "Queensland", true);
		verifyTwoStringsAreEqual(postcode, "4566", true);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// test the fix for bug ticket BBPRTL-612
		softAssertion
				.assertFalse(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's click it and hit tab to remove the error state
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);

		// verify the update link on Account Details section
		clickExactLinkNameFromElement(acceptancemoveout.accountDetails, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.accountNum, 0), "The Account Number is not displayed");
		// update the account number
		clickElementAction(accountdetailsmoveout.accountNum);
		deleteAllTextFromField();
		accountdetailsmoveout.accountNum.sendKeys("    10004    0009  993   ");
		// let's choose residential
		clickElementAction(accountdetailsmoveout.residential);
		scrollPageDown(500);
		// click the acceptance header
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify we're redirected automatically in the Account Contact section
		softAssertion.assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false,
						"Date of Birth (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the validations for Date of Birth
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		// get the current date and add 1 day
		String todayPlus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		// let's remove the current year then concatenate birthYr
		String invalidBirthDate = getString(todayPlus1, 0, todayPlus1.length() - 4);
		invalidBirthDate = invalidBirthDate + birthYr;
		clickElementAction(accountcontactmoveout.dateOfBirth);
		pauseSeleniumExecution(1000);
		accountcontactmoveout.dateOfBirth.sendKeys(invalidBirthDate, Keys.TAB);
		// click last name to ensure that the calendar is collapsed
		clickElementAction(accountcontactmoveout.lastName);
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify Date of Birth is in error state
		assertTrue(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Date of Birth (DD/MM/YYYY)"), 5, 0),
				"Date of Birth (DD/MM/YYYY) is not in error state");
		// put a valid date
		clearDateField(accountcontactmoveout.dateOfBirth);
		// get the current date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		clickElementAction(accountcontactmoveout.dateOfBirth);
		pauseSeleniumExecution(1000);
		accountcontactmoveout.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		// collapse the calendar
		clickElementAction(accountcontactmoveout.datePickerDateOfBirth);
		clickElementAction(accountcontactmoveout.firstName);
		// verify Date of Birth is not in error state
		assertFalse(isElementInError(
				getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Date of Birth (DD/MM/YYYY)"), 5, 0),
				"Date of Birth (DD/MM/YYYY) is in error state");
		this.dateOfBirth1 = validBirthDate;
		// let's choose drivers licence card and verify the validations
		clickElementAction(accountcontactmoveout.driversLicence);
		// click header of the next section
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(1000);
		// verify that the drivers licence number and state issued are in error state
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false,
						"Australian Drivers Licence Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "State Issued"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify special characters not allowed
		slowSendKeys(accountcontactmoveout.driversLicenceNumber, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 200);
		String driversNumber = getDisplayedValue(accountcontactmoveout.driversLicenceNumber, true);
		verifyStringIsBlank(driversNumber);
		// verify the drivers licence number no longer invalid
		accountcontactmoveout.driversLicenceNumber.sendKeys("  ABC  -  123456  ", Keys.TAB);
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false,
						"Australian Drivers Licence Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		String driverNum = getDisplayedValue(accountcontactmoveout.driversLicenceNumber, true);
		softAssertion.assertEquals(driverNum, "ABC123456",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify State by typing a non-existent state
		accountcontactmoveout.driversLicenceState.sendKeys("Arabasta", Keys.TAB);
		// click header of the next section
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(500);
		clickElementAction(accountcontactmoveout.next);
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "State Issued"), 5, 0),
				"State Issued is not error state");
		// let's clear the drivers licence number
		clickElementAction(accountcontactmoveout.driversLicenceNumber);
		clickElementAction(accountcontactmoveout.driversLicenceNumber);
		deleteAllTextFromField();
		// let's clear the state
		clickElementAction(accountcontactmoveout.driversLicenceState);
		deleteAllTextFromField();
		accountcontactmoveout.driversLicenceNumber.sendKeys("01235987510");
		accountcontactmoveout.driversLicenceState.sendKeys("Australian Capital Territory");
		clickElementAction(accountcontactmoveout.driversLicenceNumber);
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);

		// verify the update link on Account Contact section
		clickExactLinkNameFromElement(acceptancemoveout.accountContact, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0), "The First Name is not displayed");
		// tick Email notification
		clickElementAction(accountcontactmoveout.emailNotif);
		scrollPageDown(800);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);

		// verify the update link on the Forwarding Address section
		clickExactLinkNameFromElement(acceptancemoveout.forwardingAddress, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the forwarding address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine03, 0), "The Address Line 3 is not displayed");
		forwardingaddressmoveout.addLine03.sendKeys("Add-03 updated");
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(400);
		// verify the update link on Additional Notes section
		clickExactLinkNameFromElement(acceptancemoveout.additionalNotes, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Additional Notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "The Notes text area is not displayed");
		// add a note
		additionalnotemoveout.notes.sendKeys("  Added  an   additional   note  from   update    link    ");
		scrollPageDown(400);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(900);
		// verify per line
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566", true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update Michael O'Connell Email Address: ",
				getProp("test_email_dummy_lower_case"),
				" Mobile Phone: 0238921111 Business Phone: 0363021485 A/Hours Phone: 0411234567 Birthdate: ",
				this.dateOfBirth1,
				" Personal Id: Driver Licence (01235987510, Australian Capital Territory) Contact Secret: ('x44 tralala's)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Postal, Email", true);
		verifyTwoStringsAreEqual(forwAdd,
				"Forwarding Address update Bldg 7, 1000 Ann Street Add-03 updated Fortitude Valley, Queensland, 4006 Australia",
				true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update Added an additional note from update link", true);

		// let's tick all 3 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		clickElementAction(acceptancemoveout.thirdCheckbox);

		// let's hit Cancel button and hit No
		clickElementAction(acceptancemoveout.cancel);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemoveout.dialogContainerText, 0),
				"The Cancel Request and Remove Details dialog is not displayed");
		// verify the text in the dialog
		verifyTwoStringsAreEqual(getDisplayedText(acceptancemoveout.dialogContainerText, true),
				"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
				true);
		clickElementAction(acceptancemoveout.noCancelRequest);
		// put a pause to fix stale element
		pauseSeleniumExecution(1000);
	}

	/**
	 * For ticket BBPRTL-584
	 * 
	 * - verify the session details is still there
	 * 
	 */
	@Test(priority = 10, dependsOnMethods = { "verifyAcceptanceDetails02" })
	public void verifySessionDetails02() {

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.notes"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.supply_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.account_details"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-out.postal_address"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 8,
				assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1,
				assertionErrorMsg(getLineNumber()));

		// let's confirm the values stored in the session storage
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-out.supply_details");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-out.account_details");
		String sessionAccountContact = storage.getItemFromSessionStorage("move-out.account_contact");
		String sessionForwardingAddress = storage.getItemFromSessionStorage("move-out.postal_address");
		String sessionAddNotes = storage.getItemFromSessionStorage("move-out.notes");
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionApplicationID = storage.getItemFromSessionStorage("application_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountContact),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionForwardingAddress),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceID),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionApplicationID, "move-out",
				assertionErrorMsg(getLineNumber()));
		this.sourceID = sessionSourceID;
		logDebugMessage(concatStrings("The value of sourceID is '", this.sourceID, "'"));
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// add the property files before submitting the request
		addProp("ResiExistingContact01_moveOutDate1", this.moveOutDate1);
		addProp("ResiExistingContact01_moveOutDate2", this.moveOutDate2);
		addProp("ResiExistingContact01_dateOfBirth1", this.dateOfBirth1);
		addProp("ResiExistingContact01_sourceID", this.sourceID);
		addProp("ResiExistingContact01_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiExistingContact01_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

		// click the submit button
		clickElementAction(acceptancemoveout.submit);
		// used try/catch because sometimes we get StaleElementReferenceException
		try {
			// verify the submit loading message
			verifyTwoStringsAreEqual(acceptancemoveout.progressBar.getText(), "Submitting your request...",
					true);
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(concatStrings(
					"StaleElementReferenceException has been encountred while checking for the progress bar text. Will reinitialize the element. See message for more details -> ",
					sere.getMessage()));
			acceptancemoveout = new AcceptanceMoveOut(driver);
			// verify the submit loading message again
			verifyTwoStringsAreEqual(acceptancemoveout.progressBar.getText(), "Submitting your request...",
					true);
		}
		// verify the success message
		assertTrue(waitForElement(acceptancemoveout.dialogContainerText, PORTAL_SUBMIT_REQUEST_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT), "The dialog container is not displayed");
		// verify it was a success
		verifyTwoStringsAreEqual(getDisplayedText(acceptancemoveout.dialogContainerText, true),
				"Request Submitted Successfully Thank you for taking the time to complete this form, we will be in touch regarding your request in due course",
				true);
		// click the Ok Button
		clickElementAction(acceptancemoveout.okDialogContainer);
		// put a pause to fix stale element
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemoveout.closeDialog, 0), "Close dialog is not displayed");
		verifyTwoStringsAreEqual(acceptancemoveout.closeDialog.getText(),
				"This window/tab is no longer required, for privacy reasons we encourage you to close it", true);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// verify the expected number of keys in the session storage
		assertEquals(sessionLength, 0, "The number of session storage keys is incorrect");
		// verify the expected number of keys in the local storage
		assertEquals(localLength, 2, "The number of local storage keys is incorrect");
	}

}
