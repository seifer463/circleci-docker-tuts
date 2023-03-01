package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
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

public class ResiExistingContact02 extends MoveOutDevBase {
	
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
	 * The Move-out date to use for assertions
	 * */
	private String moveOutDate2;
	
	/**
	 * The value of the date of birth
	 *  */
	private String dateOfBirth1;
	
	/**
	 * The value of the date of birth
	 *  */
	private String dateOfBirth2;
	
	/** 
	 * The Medicare Expiry value
	 * */
	private String medCareExpiry;
	
	/**
	 * The source id value
	 *  */
	private String sourceID1;
	
	/**
	 * The source id value
	 *  */
	private String sourceID2;
	
	@BeforeClass
	public void beforeClass() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);	
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		
		// upload the correct portal_config.json we are testing
		uploadMoveOutConfig(s3Access, "04\\", "portal_config.json");
		
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
     * - create an online request record that has an existing contact whose
     * 		Name and Business Phone is the same
     * 
     * @throws ParseException 
     * 
     * */
    @Test(priority = 1)
	public void createOnlineReqRec01() throws ParseException {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
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
		// populate the Supply Details section
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
		supplydetailsmoveout.supplyAddTenancyNum.sendKeys("40");
		supplydetailsmoveout.supplyAddStreetNum.sendKeys("272");
		supplydetailsmoveout.supplyAddStreetName.sendKeys("Weyba");
		supplydetailsmoveout.supplyAddStreetType.sendKeys("Road", Keys.TAB);
		supplydetailsmoveout.supplyAddCity.sendKeys("Noosaville");
		supplydetailsmoveout.supplyAddState.sendKeys("Queensland", Keys.TAB);
		supplydetailsmoveout.supplyAddPostcode.sendKeys("4566");
		// confirm it's populated correctly
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, false);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Unit",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "40",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "272",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Weyba",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Road",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noosaville",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4566",
				assertionErrorMsg(getLineNumber()));
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// populate Move Out date
		// we will do this kind of choosing the date to verify
		// that the Move Out date sent into the API is not corrupted
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		// we will click the field in the background.
		// this would then click a date from the calendar
		clickElementAction(supplydetailsmoveout.supplyAddTenancyType);
		// just click the field to ensure that the calendar is collapsed
		clickElementAction(supplydetailsmoveout.supplyAddPostcode);
		this.moveOutDate1 = getDisplayedValue(supplydetailsmoveout.moveOutDate, true);
		// verify that the move out date is not empty
		softAssertion.assertFalse(StringUtils.isBlank(this.moveOutDate1),
				assertionErrorMsg(getLineNumber()));
		// lets verify the header section name
		String headerSuppDetails = getDisplayedText(getElementSectionHeader("Supply Details", 6), true);
		softAssertion.assertEquals(headerSuppDetails, "1 Supply Details",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
		
		// populate the Account Details
		accountdetailsmoveout.accountNum.sendKeys("0321 98765 9878");
		clickElementAction(accountdetailsmoveout.residential);
		// lets verify the header section name
		String headerAcctDetails = getDisplayedText(getElementSectionHeader("Account Details", 6), true);
		verifyTwoStringsAreEqual(headerAcctDetails, "2 Account Details", true);
		// verify the warning message is displayed in the account number
		String warningMsg = getDisplayedText(accountdetailsmoveout.accountNumMsgWarning.get(0), true);
		verifyTwoStringsAreEqual(warningMsg, "Warning, number should be 12 digits", true);
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
		
		// populate the Account Contact
		accountcontactmoveout.firstName.sendKeys("brad");
		accountcontactmoveout.lastName.sendKeys("Harrison");
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR)-18;
		String birthYr = Integer.toString(birthYrRaw);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		this.dateOfBirth1 = validBirthDate;
		clickElementAction(accountcontactmoveout.dateOfBirth);
		pauseSeleniumExecution(1000);
		accountcontactmoveout.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		// click the First Name to ensure that the calendar is collapsed
		clickElementAction(accountcontactmoveout.firstName);
		clickElementAction(accountcontactmoveout.passport);
		accountcontactmoveout.passportNumber.sendKeys("Pass12340");
		accountcontactmoveout.passportCountry.sendKeys("Åland Islands");
		// tick email
		clickElementAction(accountcontactmoveout.emailNotif);
		// untick the Postal
		clickElementAction(accountcontactmoveout.postalNotif);
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case"));
		accountcontactmoveout.mobilePhone.sendKeys("6223659875");
		accountcontactmoveout.businessPhone.sendKeys("0169941139");
		accountcontactmoveout.afterhoursPhone.sendKeys("0556987801");
		accountcontactmoveout.contactSecretCode.sendKeys("SEKreT's 000");
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
		accountcontactmoveout.mobilePhone.sendKeys("02 3893 1121");
		accountcontactmoveout.businessPhone.sendKeys("+61 4690 41930");
		accountcontactmoveout.afterhoursPhone.sendKeys("0260042131", Keys.TAB);
		// verify the phone numbers not in error state
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// lets verify the header section name
		String headerAcctContact = getDisplayedText(getElementSectionHeader("Account Contact", 6), true);
		verifyTwoStringsAreEqual(headerAcctContact, "3 Account Contact (brad Harrison)", true);
		// hit next
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "Notes section is not displayed");
		// lets verify the header section name
		String headerAddNotes = getDisplayedText(getElementSectionHeader("Additional Note", 6), true);
		verifyTwoStringsAreEqual(headerAddNotes, "4 Additional Note", true);
		// go back to the Account Contact section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// tick back Postal
		clickElementAction(accountcontactmoveout.postalNotif);
		clickElementAction(forwardingaddressmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine01, 0),
				"We are not in the Forwarding Address section");
		
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
		// populate the Forwarding Address
		forwardingaddressmoveout.addLine01.sendKeys("Unit 7B");
		forwardingaddressmoveout.addLine02.sendKeys("11 Innovation Parkway");
		forwardingaddressmoveout.city.sendKeys("Birtinya");
		forwardingaddressmoveout.state.sendKeys("Queensland");
		forwardingaddressmoveout.postcode.sendKeys("4575");
		forwardingaddressmoveout.country.sendKeys("Australia", Keys.TAB);
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, true);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, true);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, true);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, true);
		String forwCity = getDisplayedValue(forwardingaddressmoveout.city, true);
		String forwState = getDisplayedValue(forwardingaddressmoveout.state, true);
		String forwPostcode = getDisplayedValue(forwardingaddressmoveout.postcode, true);
		String country = getDisplayedValue(forwardingaddressmoveout.country, true);
		verifyTwoStringsAreEqual(addLine1, "Unit 7B", false);
		verifyTwoStringsAreEqual(addLine2, "11 Innovation Parkway", false);
		verifyStringIsBlank(addLine3);
		verifyStringIsBlank(addLine4);
		verifyTwoStringsAreEqual(forwCity, "Birtinya", false);
		verifyTwoStringsAreEqual(forwState, "Queensland", false);
		verifyTwoStringsAreEqual(forwPostcode, "4575", false);
		verifyTwoStringsAreEqual(country, "Australia", false);
		// populate the Address line 04
		forwardingaddressmoveout.addLine04.sendKeys("Address #04");
		// lets verify the header section name
		String headerForwAdd = getDisplayedText(getElementSectionHeader("Forwarding Address", 6), true);
		verifyTwoStringsAreEqual(headerForwAdd, "4 Forwarding Address", true);
		// let's click the acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
		
		scrollPageDown(400);
		// lets verify the header section name
		String headerAcceptance = getDisplayedText(getElementSectionHeader("Acceptance", 6), true);
		verifyTwoStringsAreEqual(headerAcceptance, "6 Acceptance", true);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update Unit 40, 272 Weyba Road Noosaville, Queensland, 4566", true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact,
				concatStrings("Account Contact update brad Harrison Email Address: ", getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0238931121 Business Phone: +61469041930 A/Hours Phone: 0260042131 Birthdate: ",
						this.dateOfBirth1,
						" Personal Id: Passport (Pass12340, Åland Islands) Contact Secret: (SEKreT's 000)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email, Postal", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Unit 7B 11 Innovation Parkway Address #04 Birtinya, Queensland, 4575 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		// update the move Out date
		clickExactLinkNameFromElement(acceptancemoveout.movingOut, "update");
		pauseSeleniumExecution(1000);
		clearDateField(supplydetailsmoveout.moveOutDate);
		String tomorrow = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate1 = tomorrow;
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(tomorrow, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		clickElementAction(supplydetailsmoveout.supplyAddComplexName);
		// lets verify the header section name
		headerSuppDetails = getDisplayedText(getElementSectionHeader("Supply Details", 6), true);
		verifyTwoStringsAreEqual(headerSuppDetails, "1 Supply Details", true);
		scrollPageDown(300);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// update the account number
		clickExactLinkNameFromElement(acceptancemoveout.accountDetails, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmoveout.accountNum);
		deleteAllTextFromField();
		accountdetailsmoveout.accountNum.sendKeys("100060006796", Keys.TAB);
		accountdetailsmoveout = new AccountDetailsMoveOut(driver, 1);
		// verify no warning message is displayed
		softAssertion.assertFalse(isElementExists(accountdetailsmoveout.accountNumMsgWarning),
				assertionErrorMsg(getLineNumber()));
		// lets verify the header section name
		headerAcctDetails = getDisplayedText(getElementSectionHeader("Account Details", 6), true);
		softAssertion.assertEquals(headerAcctDetails, "2 Account Details",
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(400);
		// go back to acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify per line
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update Unit 40, 272 Weyba Road Noosaville, Queensland, 4566", true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact,
				concatStrings("Account Contact update brad Harrison Email Address: ", getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0238931121 Business Phone: +61469041930 A/Hours Phone: 0260042131 Birthdate: ",
						this.dateOfBirth1,
						" Personal Id: Passport (Pass12340, Åland Islands) Contact Secret: (SEKreT's 000)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email, Postal", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Unit 7B 11 Innovation Parkway Address #04 Birtinya, Queensland, 4575 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		// verify session details
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
		softAssertion.assertEquals(sessionLength, 7,
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
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionApplicationID, "move-out",
				assertionErrorMsg(getLineNumber()));
		this.sourceID1 = sessionSourceID;
		logDebugMessage(concatStrings("The value of sourceID is '", this.sourceID1, "'"));
		// verify all assertions
		softAssertion.assertAll();
		
		scrollPageDown(900);
		// tick only 2 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		
		// add the property files before submitting the request
		addProp("ResiExistingContact02_firstCRM_moveOutDate1", this.moveOutDate1);
		addProp("ResiExistingContact02_firstCRM_dateOfBirth1", this.dateOfBirth1);
		addProp("ResiExistingContact02_firstCRM_sourceID1", this.sourceID1);
		addProp("ResiExistingContact02_firstCRM_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		
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
    
    /** 
     * - create an online request record that has an existing contact whose
     * 		Name and After Hours Phone is the same
     * 
     * @throws ParseException 
     * 
     * */
    @Test(priority = 2, dependsOnMethods = {"createOnlineReqRec01"})
	public void createOnlineReqRec02() throws ParseException {
		
    	refreshBrowser(1, 3000);
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
    	loadPortal();
    	
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify that the fields are empty
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.moveOutDate, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddCity, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddState, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// populate the Supply Details section
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate2 = today;
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);

		supplydetailsmoveout.supplyAddStreetNum.sendKeys("75");
		supplydetailsmoveout.supplyAddStreetName.sendKeys("Davis");
		supplydetailsmoveout.supplyAddStreetType.sendKeys("Street", Keys.TAB);
		supplydetailsmoveout.supplyAddCity.sendKeys("Allenstown");
		supplydetailsmoveout.supplyAddState.sendKeys("Queensland", Keys.TAB);
		supplydetailsmoveout.supplyAddPostcode.sendKeys("4700");
		
		// confirm it's populated correctly
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, true);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		verifyStringIsBlank(complexName);
		verifyStringIsBlank(tenancyType);
		verifyStringIsBlank(unitNum);
		verifyTwoStringsAreEqual(stNum, "75", false);
		verifyTwoStringsAreEqual(stName, "Davis", false);
		verifyTwoStringsAreEqual(stType, "Street", false);
		verifyTwoStringsAreEqual(city, "Allenstown", false);
		verifyTwoStringsAreEqual(state, "Queensland", false);
		verifyTwoStringsAreEqual(postcode, "4700", false);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// populate the Tenancy Type
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
		
		// populate the account details section
		accountdetailsmoveout.accountNum.sendKeys("100060004395");
		clickElementAction(accountdetailsmoveout.residential);
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
		
		// populate the account contact section
		accountcontactmoveout.firstName.sendKeys("Susan & Les");
		accountcontactmoveout.lastName.sendKeys("Smith");
		clickElementAction(accountcontactmoveout.datePickerDateOfBirth);
		// click the label in the background to choose a year from the calendar
		hoverToElementAction(accountcontactmoveout.lblBillDeliveryHeader);
		clickElementAction(accountcontactmoveout.lblBillDeliveryHeader);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a month from the calendar
		hoverToElementAction(accountcontactmoveout.lblPostalNotif);
		clickElementAction(accountcontactmoveout.lblPostalNotif);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a date from the calendar
		hoverToElementAction(accountcontactmoveout.lblPostalNotif);
		clickElementAction(accountcontactmoveout.lblPostalNotif);
		// click to ensure that calendar is collapsed
		clickElementAction(accountcontactmoveout.contactSecretCode);
		// verify that the Date of Birth is not empty
		this.dateOfBirth2 = getDisplayedValue(accountcontactmoveout.dateOfBirth, true);
		softAssertion.assertFalse(StringUtils.isBlank(this.dateOfBirth2),
				assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(accountcontactmoveout.medicareCard);
		accountcontactmoveout.medicareCardNumber.sendKeys("2428 77813 2");
		int month = 2;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		String medExp = concatStrings(monthStr, "/", expYrStr);
		clickElementAction(accountcontactmoveout.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(accountcontactmoveout.medicareCardExpiry, medExp, true, 250);
		accountcontactmoveout.medicareCardExpiry.sendKeys(Keys.TAB);
		// collapse the calendar
		clickElementAction(accountcontactmoveout.lastName);
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case"));
		// verify invalid phone numbers
		accountcontactmoveout.mobilePhone.sendKeys("5892 1120");
		accountcontactmoveout.businessPhone.sendKeys("+61 6 0239 0392");
		accountcontactmoveout.afterhoursPhone.sendKeys("1398620");
		accountcontactmoveout.contactSecretCode.sendKeys("#000-Abcs");
		// verify the phone numbers not in error state
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
		clickElementAction(accountcontactmoveout.mobilePhone);
		deleteAllTextFromField();
		clickElementAction(accountcontactmoveout.businessPhone);
		deleteAllTextFromField();
		clickElementAction(accountcontactmoveout.afterhoursPhone);
		deleteAllTextFromField();
		accountcontactmoveout.mobilePhone.sendKeys("1800986510");
		accountcontactmoveout.businessPhone.sendKeys("1300986510");
		accountcontactmoveout.afterhoursPhone.sendKeys("+61823014785");
		String medicareExp = getDisplayedValue(accountcontactmoveout.medicareCardExpiry, true);
		this.medCareExpiry = medicareExp;
		// remove the value we entered
		clickElementAction(accountcontactmoveout.businessPhone);
		deleteAllTextFromField();
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine01, 0),
				"We are not in the Forwarding Address section");
		
		// populate the forwarding address
		// input the forwarding address
		forwardingaddressmoveout.addLine01.sendKeys("Unit 50");
		forwardingaddressmoveout.addLine02.sendKeys("20 Baywater Drive");
		forwardingaddressmoveout.city.sendKeys("Twin Waters");
		forwardingaddressmoveout.state.sendKeys("Queensland");
		forwardingaddressmoveout.postcode.sendKeys("4564");
		forwardingaddressmoveout.country.sendKeys("Australia", Keys.TAB);
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, true);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, true);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, true);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, true);
		String forwCity = getDisplayedValue(forwardingaddressmoveout.city, true);
		String forwState = getDisplayedValue(forwardingaddressmoveout.state, true);
		String forwPostcode = getDisplayedValue(forwardingaddressmoveout.postcode, true);
		String forwCountry = getDisplayedValue(forwardingaddressmoveout.country, true);
		verifyTwoStringsAreEqual(addLine1, "Unit 50", false);
		verifyTwoStringsAreEqual(addLine2, "20 Baywater Drive", false);
		verifyStringIsBlank(addLine3);
		verifyStringIsBlank(addLine4);
		verifyTwoStringsAreEqual(forwCity, "Twin Waters", false);
		verifyTwoStringsAreEqual(forwState, "Queensland", false);
		verifyTwoStringsAreEqual(forwPostcode, "4564", false);
		verifyTwoStringsAreEqual(forwCountry, "Australia", false);
		// let's add an address 3 and 4
		forwardingaddressmoveout.addLine03.sendKeys("Address #03");
		forwardingaddressmoveout.addLine04.sendKeys("Address #04");
		scrollPageDown(200);
		// go to the notes section
		clickElementAction(forwardingaddressmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
		
		// populate additional notes
		additionalnotemoveout.notes.sendKeys("     Many     White    spaces    entered     from     the     portal    ");
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
		
		scrollPageDown(200);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate2, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update 75 Davis Street Allenstown, Queensland, 4700", true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact,
				concatStrings("Account Contact update Susan & Les Smith Email Address: ", getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 1800986510 A/Hours Phone: +61823014785 Birthdate: ", this.dateOfBirth2,
						" Personal Id: Medicare Card (2428 77813 2, ", this.medCareExpiry,
						") Contact Secret: (#000-Abcs)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Postal", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Unit 50 20 Baywater Drive Address #03 Address #04 Twin Waters, Queensland, 4564 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update Many White spaces entered from the portal", true);
		
		scrollPageDown(800);
		// tick all 3 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		clickElementAction(acceptancemoveout.thirdCheckbox);
		
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
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		this.sourceID2 = sessionSourceID;
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
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"),
				assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// add the property files before submitting the request
		addProp("ResiExistingContact02_secondCRM_moveOutDate2", this.moveOutDate2);
		addProp("ResiExistingContact02_secondCRM_dateOfBirth2", this.dateOfBirth2);
		addProp("ResiExistingContact02_secondCRM_sourceID2", this.sourceID2);
		addProp("ResiExistingContact02_secondCRM_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		
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
