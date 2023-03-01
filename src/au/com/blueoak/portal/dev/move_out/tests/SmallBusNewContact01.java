package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_out.MoveOutDevBase;
import au.com.blueoak.portal.pageObjects.move_out.AcceptanceMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountContactMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountDetailsMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AdditionalNoteMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.ForwardingAddressMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.SupplyDetailsMoveOut;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class SmallBusNewContact01 extends MoveOutDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveOut supplydetailsmoveout;
	AccountDetailsMoveOut accountdetailsmoveout;
	AccountContactMoveOut accountcontactmoveout;
	ForwardingAddressMoveOut forwardingaddressmoveout;
	AdditionalNoteMoveOut additionalnotemoveout;
	AcceptanceMoveOut acceptancemoveout;

	/**
	 * Store the name of the class for logging
	 */
	private String className;

	/**
	 * The Move-out date to use for assertions
	 */
	private String moveOutDate1;

	/**
	 * The source id value
	 */
	private String sourceID;

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
		accountdetailsmoveout = new AccountDetailsMoveOut(driver);
		accountcontactmoveout = new AccountContactMoveOut(driver);
		forwardingaddressmoveout = new ForwardingAddressMoveOut(driver);
		additionalnotemoveout = new AdditionalNoteMoveOut(driver);
		acceptancemoveout = new AcceptanceMoveOut(driver);
	}

	/** 
	 * - create an online request that has an existing contact
	 * 		whose name is the same with the portal contact
	 * 
	 * */
	@Test(priority = 1)
	public void createOnlineReqRecord01() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// populate the Supply Address
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "132 Mitchell", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are only from Australia
		List<String> addresses1 = null;
		try {
			addresses1 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
			addresses1 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		}
		verifyStringContainsInEachListPacket(addresses1, "132 Mitchell", true);
		// let's update the address	
		clickElementAction(supplydetailsmoveout.supplyAddSearch);
		supplydetailsmoveout.supplyAddSearch.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "am", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are only from Australia
		addresses1 = null;
		try {
			addresses1 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
			addresses1 = getAllSupplyAddress(supplydetailsmoveout.supplyAddressesDiv);
		}
		verifyStringContainsInEachListPacket(addresses1, "132 Mitcham", true);
		// choose first one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "132 Mitcham Road, Donvale VIC");
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
		verifyStringIsBlank(unitNum);
		verifyTwoStringsAreEqual(stNum, "132", false);
		verifyTwoStringsAreEqual(stName, "Mitcham", false);
		verifyTwoStringsAreEqual(stType, "Road", false);
		verifyTwoStringsAreEqual(city, "Donvale", false);
		verifyTwoStringsAreEqual(state, "Victoria", false);
		verifyTwoStringsAreEqual(postcode, "3111", false);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// verify the fix for bug ticket BBPRTL-2112
		// populate the tenancy type
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		// lets verify the header section name
		String headerDetails = getDisplayedText(getElementSectionHeader("Supply Details", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "1 Supply Details", true);
		// populate the Supply Details section
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate1 = today;
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
		
		// populate the Account Details
		accountdetailsmoveout.accountNum.sendKeys("100060002498");
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(500);
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_abn1"));
		clickElementAction(accountdetailsmoveout.next);
		headerDetails = getDisplayedText(getElementSectionHeader("Account Details", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "2 Account Details", true);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
		
		// populate the Account Contact
		accountcontactmoveout.firstName.sendKeys("L");
		accountcontactmoveout.lastName.sendKeys("Sanger");
		headerDetails = getDisplayedText(getElementSectionHeader("Account Contact", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "3 Account Contact (L Sanger)", true);
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case")); 
		accountcontactmoveout.mobilePhone.sendKeys("1300569089");
		accountcontactmoveout.contactSecretCode.sendKeys("Boy George 101");
		headerDetails = getDisplayedText(getElementSectionHeader("Account Contact", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "3 Account Contact (L Sanger)", true);
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.forwAddress, 0),
				"We are not in the Forwarding Address section");
		
		// populate the forwarding address
		slowSendKeys(forwardingaddressmoveout.forwAddress, "Unit 301 192 Marine Parade Cool", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// choose first one from the list
		chooseAddress(forwardingaddressmoveout.forwAddressesDiv, "unit 301/192 Marine Parade, Coolangatta QLD");
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, true);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, true);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, true);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, true);
		String forwCity = getDisplayedValue(forwardingaddressmoveout.city, true);
		String forwState = getDisplayedValue(forwardingaddressmoveout.state, true);
		String forwPostcode = getDisplayedValue(forwardingaddressmoveout.postcode, true);
		String country = getDisplayedValue(forwardingaddressmoveout.country, true);
		verifyTwoStringsAreEqual(addLine1, "Unit 301", false);
		verifyTwoStringsAreEqual(addLine2, "192 Marine Parade", false);
		verifyStringIsBlank(addLine3);
		verifyStringIsBlank(addLine4);
		verifyTwoStringsAreEqual(forwCity, "Coolangatta", false);
		verifyTwoStringsAreEqual(forwState, "Queensland", false);
		verifyTwoStringsAreEqual(forwPostcode, "4225", false);
		verifyTwoStringsAreEqual(country, "Australia", false);
		headerDetails = getDisplayedText(getElementSectionHeader("Forwarding Address", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "4 Forwarding Address", true);
		clickElementAction(additionalnotemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
		
		// populate the additional note
		additionalnotemoveout.notes.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		headerDetails = getDisplayedText(getElementSectionHeader("Additional Note", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "5 Additional Note", true);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
		
		scrollPageDown(200);
		headerDetails = getDisplayedText(getElementSectionHeader("Acceptance", 6), true);
		verifyTwoStringsAreEqual(headerDetails, "6 Acceptance", true);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update 132 Mitcham Road Donvale, Victoria, 3111", true);
		verifyTwoStringsAreEqual(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_valid_company_name_abn1_abn2"), " ABN/ACN ", getProp("test_data_valid_abn1")), true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update L Sanger Email Address: ",
				getProp("test_email_dummy_lower_case"), " Mobile Phone: 1300569089 Contact Secret: (Boy George 101)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Postal", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Unit 301 192 Marine Parade Coolangatta, Queensland, 4225 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true);
		
		// check the session details
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
		logDebugMessage(concatStrings("The value of sourceID1 is '", this.sourceID, "'"));
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		scrollPageDown(700);
		// tick only 2 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		
		// add the property files before submitting the request
		addProp("SmallBusNewContact01_moveOutDate1", this.moveOutDate1);
		addProp("SmallBusNewContact01_sourceID", this.sourceID);
		addProp("SmallBusNewContact01_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		
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
		assertTrue(waitForElement(acceptancemoveout.dialogContainerText, PORTAL_SUBMIT_REQUEST_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT), "The dialog container is not displayed");
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
