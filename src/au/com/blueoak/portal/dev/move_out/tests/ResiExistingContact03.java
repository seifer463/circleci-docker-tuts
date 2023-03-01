package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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

public class ResiExistingContact03 extends MoveOutDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	SupplyDetailsMoveOut supplydetailsmoveout;
	AccountDetailsMoveOut accountdetailsmoveout;
	AccountContactMoveOut accountcontactmoveout;
	ForwardingAddressMoveOut forwardingaddressmoveout;
	AdditionalNoteMoveOut additionalnotemoveout;
	AcceptanceMoveOut acceptancemoveout;
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/** 
	 * The Move-out date to use for assertions
	 * */
	private String moveOutDate1;
	
	/** 
	 * Just a different format date for the Move-Out
	 * */
	private String moveOutDate2;
	
	/**
	 * The source id value
	 *  */
	private String sourceID;
	
	@BeforeClass
	public void beforeClass() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);	
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		
		// upload the correct portal_config.json we are testing
		uploadMoveOutConfig(s3Access, "03\\", "portal_config.json");
		
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
     * 
     * 
     * 
     * */
    @Test(priority = 1)
	public void verifySupplyDetails() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList(
						"1 Supply Details",
						"2 Account Details",
						"3 Account Contact",
						"4 Additional Note",
						"5 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
    	String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
    	this.moveOutDate1 = today;
    	this.moveOutDate2 = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
    	supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
    	
		// verify the fix for bug ticket BBPRTL-2112
    	supplydetailsmoveout.supplyAddComplexName.sendKeys("Q1");
    	supplydetailsmoveout.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
    	supplydetailsmoveout.supplyAddStreetName.sendKeys("Surfers Paradise");
    	supplydetailsmoveout.supplyAddStreetType.sendKeys("Boulevard", Keys.TAB);
    	supplydetailsmoveout.supplyAddCity.sendKeys("Gold Coast");
    	supplydetailsmoveout.supplyAddState.sendKeys("Queensland");
    	clickElementAction(supplydetailsmoveout.supplyAddPostcode);
		pauseSeleniumExecution(600);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
    	supplydetailsmoveout.supplyAddPostcode.sendKeys("4217");
    	clickElementAction(supplydetailsmoveout.next);
    	pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
	}
	
    /** 
     * - verify account number not displayed
     * 
     * */
    @Test(priority = 2, dependsOnMethods = {"verifySupplyDetails"})
	public void verifyAccountDetails() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
    	
    	// verify account number not displayed
    	accountdetailsmoveout = new AccountDetailsMoveOut(driver, 1);
    	assertFalse(isElementExists(accountdetailsmoveout.accountNumList), "Account Number is displayed");
    	setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
    	clickElementAction(accountdetailsmoveout.residential);
    	
    	clickElementAction(accountcontactmoveout.header);
    	pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
    }
    
    /** 
     * 
     * 
     * */
    @Test(priority = 3, dependsOnMethods = {"verifyAccountDetails"})
	public void verifyAccountContact() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
    	
    	accountcontactmoveout = new AccountContactMoveOut(driver, 1);
    	// verify Date of Birth is not displayed
    	assertFalse(isElementExists(accountcontactmoveout.dateOfBirthList), "Date of Birth is displayed");
    	// verify the header does not exists
    	assertFalse(isElementExists(accountcontactmoveout.personalIdHeaderList), "Personal Identification Header is displayed");
    	// verify the Personal Identification radio are not displayed
    	assertFalse(isElementExists(accountcontactmoveout.driversLicenceList), "Driver Licence is displayed");
    	assertFalse(isElementExists(accountcontactmoveout.passportList), "Passport is displayed");
    	assertFalse(isElementExists(accountcontactmoveout.medicareCardList), "Medicare card is displayed");
    	assertFalse(isElementExists(accountcontactmoveout.provideNoneList), "Provide None is displayed");
    	// verify contact secret code is not displayed
    	assertFalse(isElementExists(accountcontactmoveout.contactSecretCodeList), "Contact Secret Code is displayed");
    	setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
    	
    	accountcontactmoveout.firstName.sendKeys("roger");
    	accountcontactmoveout.lastName.sendKeys("buckle");
    	// verify email is ticked
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(accountcontactmoveout.emailNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// un-tick email
		clickElementAction(accountcontactmoveout.emailNotif);
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.emailNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
    	
    	// these are invalid without the + sign
    	accountcontactmoveout.mobilePhone.sendKeys("61257753012");
    	accountcontactmoveout.businessPhone.sendKeys("61397031098");
    	accountcontactmoveout.afterhoursPhone.sendKeys("61485691260", Keys.TAB);
		// verify the phone numbers in error state
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
    	
    	// remove the values
    	clickElementAction(accountcontactmoveout.mobilePhone);
    	deleteAllTextFromField();
    	clickElementAction(accountcontactmoveout.businessPhone);
    	deleteAllTextFromField();
    	clickElementAction(accountcontactmoveout.afterhoursPhone);
    	deleteAllTextFromField();
    	
    	// enter the valid values
    	accountcontactmoveout.businessPhone.sendKeys("+3970310054324");
    	accountcontactmoveout.afterhoursPhone.sendKeys("+485690");

		scrollPageDown(500);
		clickElementAction(acceptancemoveout.header);
    	pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
    }
    
    /** 
     * 
     * 
     * */
    @Test(priority = 4, dependsOnMethods = {"verifyAccountContact"})
	public void verifyAcceptanceDetails() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
    	
		scrollPageDown(500);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update Q1 Surfers Paradise Boulevard Gold Coast, Queensland, 4217", true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact,
				"Account Contact update roger buckle Business Phone: +3970310054324 A/Hours Phone: +485690", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// verify the fix for bug ticket BBPRTL-1119
		softAssertion.assertTrue(sessionKeys.contains("source_id"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"),
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
		
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		String sessionApplicationID = storage.getItemFromSessionStorage("application_id");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
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
		
		scrollPageDown(700);
		// verify the required tickbox are in error state
		clickElementAction(acceptancemoveout.submit);
		softAssertion.assertTrue(isElementInError(acceptancemoveout.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemoveout.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// click all 2 checkboxes
		clickElementAction(acceptancemoveout.secondCheckbox);
		clickElementAction(acceptancemoveout.thirdCheckbox);
		
		String lbl2ndChkBox = getDisplayedText(acceptancemoveout.lblSecondCheckbox, true);
		String lbl3rdChkBox = getDisplayedText(acceptancemoveout.lblThirdCheckbox, true);
		softAssertion.assertEquals(lbl2ndChkBox,
				"I/We agree to the Terms and Conditions and also acknowledge BlueOak's Standard Fee Schedule.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lbl3rdChkBox,
				"I/We have added enquiries@blueoak.com.au to my email contacts or white list where required",
				assertionErrorMsg(getLineNumber()));
		acceptancemoveout = new AcceptanceMoveOut(driver, 0);
		// verify the first checkbox is not displayed
		softAssertion.assertFalse(isElementExists(acceptancemoveout.firstCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemoveout.lblFirstCheckboxList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// add the property files before submitting the request
		addProp("ResiExistingContact03_moveOutDate1", this.moveOutDate1);
		addProp("ResiExistingContact03_moveOutDate2", this.moveOutDate2);
		addProp("ResiExistingContact03_sourceID", this.sourceID);
		addProp("ResiExistingContact03_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		
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