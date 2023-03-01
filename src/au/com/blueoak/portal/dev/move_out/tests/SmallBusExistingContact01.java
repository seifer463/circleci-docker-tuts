package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
import au.com.blueoak.portal.pageObjects.move_out.ToastMsgMoveOut;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class SmallBusExistingContact01 extends MoveOutDevBase {
	
	/** 
	 * Initialize the page objects factory
	 * */
	SupplyDetailsMoveOut supplydetailsmoveout;
	AccountDetailsMoveOut accountdetailsmoveout;
	AccountContactMoveOut accountcontactmoveout;
	ForwardingAddressMoveOut forwardingaddressmoveout;
	AdditionalNoteMoveOut additionalnotemoveout;
	AcceptanceMoveOut acceptancemoveout;
	ToastMsgMoveOut toastmsgmoveout;
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/** 
	 * The Move-out date to use for assertions
	 * */
	private String moveOutDate1;
	
	/** 
	 * Just a another different format date for the Move-Out
	 * */
	private String moveOutDate2;
	
	/** 
	 * Birthdate for assertions
	 * */
	private String dateOfBirth1;
	
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
		toastmsgmoveout = new ToastMsgMoveOut(driver);
	}

	/** 
	 * 
	 * - verify the validations in the Passport
	 * - verify online request would be created and the contact would be linked
	 * 		to an existing one since name and mobile are the same
	 * 
	 * @throws ParseException 
	 * */
	@Test(priority = 1)
	public void createOnlineReqSmallBus01() throws ParseException {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// populate the Supply Details section
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate1 = today;
		this.moveOutDate2 = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
		supplydetailsmoveout.supplyAddTenancyNum.sendKeys("20");
		supplydetailsmoveout.supplyAddStreetNum.sendKeys("239-245");
		supplydetailsmoveout.supplyAddStreetName.sendKeys("Gympie");
		supplydetailsmoveout.supplyAddStreetType.sendKeys("Terrace", Keys.TAB);
		supplydetailsmoveout.supplyAddCity.sendKeys("Noosaville");
		supplydetailsmoveout.supplyAddState.sendKeys("Queensland", Keys.TAB);
		supplydetailsmoveout.supplyAddPostcode.sendKeys("4566");
		
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
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		// test the fix in ticket BBPRTL-612
		softAssertion.assertEquals(tenancyType, "Unit",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "20",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "239-245",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Gympie",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Terrace",
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
		
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
		
		// populate the Account Details section
		accountdetailsmoveout.accountNum.sendKeys("100040008797");
		clickElementAction(accountdetailsmoveout.next);
		assertFalse(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "BlueBilling Account Number"), 5, 0), "Account Number is in error state");
		assertTrue(isElementInError(accountdetailsmoveout.residential, 5, 0), "Residential radio is not in error state");
		assertTrue(isElementInError(accountdetailsmoveout.commercial, 5, 0), "Commercial radio is not in error state");
		clickElementAction(accountdetailsmoveout.residential);
		clickElementAction(accountdetailsmoveout.next);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
		
		// populate the Account Contact section
		accountcontactmoveout.firstName.sendKeys("tom tri");
		accountcontactmoveout.lastName.sendKeys("ly");
		// populate the BirthDate
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR)-18;
		String birthYr = Integer.toString(birthYrRaw);
		today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		clickElementAction(accountcontactmoveout.dateOfBirth);
		pauseSeleniumExecution(2000);
		validBirthDate = validBirthDate + birthYr;
		// got a org.openqa.selenium.ElementNotInteractableException: element not interactable
		// on this line below on jenkins
		// so we try to collapse first the calendar before entering the birth date
		clickElementAction(accountcontactmoveout.dateOfBirth);
		accountcontactmoveout.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		this.dateOfBirth1 = validBirthDate;
		clickElementAction(accountcontactmoveout.emailNotif);
		// untick the Postal
		clickElementAction(accountcontactmoveout.postalNotif);
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case"));
		accountcontactmoveout.mobilePhone.sendKeys("+61469941139");
		accountcontactmoveout.contactSecretCode.sendKeys("Contact Secret Code");
		// verify the validations in the Passport
		clickElementAction(accountcontactmoveout.passport);
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Passport Number"), 5, 0),
				"Passport Number is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Country of Issue"), 5, 0),
				"Country of Issue is not error state");
		// verify special characters not allowed
		slowSendKeys(accountcontactmoveout.passportNumber, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 200);
		String passportNumber = getDisplayedValue(accountcontactmoveout.passportNumber, true);
		verifyStringIsBlank(passportNumber);
		accountcontactmoveout.passportNumber.sendKeys("     CBD987651 023   ");
		passportNumber = getDisplayedValue(accountcontactmoveout.passportNumber, true);
		verifyTwoStringsAreEqual(passportNumber, "CBD9876510", true);
		// verify an error is returned when a non-existent country is entered
		accountcontactmoveout.passportCountry.sendKeys("Wakanda", Keys.TAB);
		clickElementAction(accountcontactmoveout.next);
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Passport Number"), 5, 0),
				"Passport Number is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Country of Issue"), 5, 0),
				"Country of Issue is not error state");
		// clear the invalid value
		clickElementAction(accountcontactmoveout.passportCountry);
		deleteAllTextFromField();
		accountcontactmoveout.passportCountry.sendKeys("australia");
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(500);
		clickElementAction(accountcontactmoveout.next);
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Passport Number"), 5, 0),
				"Passport Number is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Country of Issue"), 5, 0),
				"Country of Issue is not error state");
		// clear the invalid value
		clickElementAction(accountcontactmoveout.passportCountry);
		deleteAllTextFromField();
		accountcontactmoveout.passportCountry.sendKeys("united");
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		List<String> countries = null;
		try {
			countries = getAllMatOptionsValues(accountcontactmoveout.countriesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			accountcontactmoveout = new AccountContactMoveOut(driver);
			countries = getAllMatOptionsValues(accountcontactmoveout.countriesDiv);
		}
		verifyStringContainsInEachListPacket(countries, "united", false);
		verifyNumOfMatOptionValuesDisp(accountcontactmoveout.countriesDiv, 4);
		// choose fourth from the list
		chooseFromList(accountcontactmoveout.countriesDiv, 4);
		pauseSeleniumExecution(1000);
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
		
		// just put empty spaces for the additional note
		additionalnotemoveout.notes.sendKeys("           	");
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
		
		// verify the acceptance page
		scrollPageDown(500);
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update Unit 20, 239-245 Gympie Terrace Noosaville, Queensland, 4566",
				true);
		verifyTwoStringsAreEqual(acctDetails, "Account Details update Residential Account", true);
		verifyTwoStringsAreEqual(acctContact,
				concatStrings("Account Contact update tom tri ly Email Address: ", getProp("test_email_dummy_lower_case"),
						" Mobile Phone: +61469941139 Birthdate: ", this.dateOfBirth1,
						" Personal Id: Passport (CBD9876510, United States) Contact Secret: (Contact Secret Code)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		// let's update it to Commercial
		clickExactLinkNameFromElement(acceptancemoveout.accountDetails, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(1000);
		// put invalid ABN
		accountdetailsmoveout.abnOrAcn.sendKeys("65 079 509 739");
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(300);
		// hit the header to make sure it did not go to the next section
		clickElementAction(accountcontactmoveout.header);
		// verify the ABN/ACN finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0), "Company ABN or ACN is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0), "Trading Name is in error state");
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		// put invalid ACN
		accountdetailsmoveout.abnOrAcn.sendKeys("079 509 739");
		// hit the header to make sure it did not go to the next section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(300);
		clickElementAction(accountdetailsmoveout.next);
		// verify the ABN/ACN finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0), "Company ABN or ACN is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0), "Trading Name is in error state");
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		// put a valid ACN
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_acn2"));
		accountdetailsmoveout.tradingName.sendKeys("Trading's LLC'");
		// verify the ABN/ACN finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0), "Company ABN or ACN is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0), "Trading Name is in error state");
		// verify the displayed label
		String abnAcnAndCompany = getDisplayedValue(accountdetailsmoveout.abnOrAcn, true);
		String tradingName = getDisplayedValue(accountdetailsmoveout.tradingName, true);
		verifyTwoStringsAreEqual(abnAcnAndCompany, concatStrings(getProp("test_data_valid_acn2"), " (",
				getProp("test_data_valid_company_name_acn1_acn2"), ")"), true);
		verifyTwoStringsAreEqual(tradingName, "Trading's LLC'", true);
		scrollPageDown(500);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify per line
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update Unit 20, 239-245 Gympie Terrace Noosaville, Queensland, 4566",
				true);
		verifyTwoStringsAreEqual(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_acn1_acn2"), " (Trading's LLC') ABN/ACN ",
						getProp("test_data_valid_acn2")),
				true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update tom tri ly Email Address: ",
				getProp("test_email_dummy_lower_case"), " Mobile Phone: +61469941139 Contact Secret: (Contact Secret Code)"), true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		// let's update the Account Contact
		clickExactLinkNameFromElement(acceptancemoveout.accountContact, "update");
		pauseSeleniumExecution(1000);
		// tick postal
		clickElementAction(accountcontactmoveout.postalNotif);
		scrollPageDown(500);
		// go the acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify we are automatically redirected into the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine01, 0),
				"We are not yet in the Forwarding Address section");
		forwardingaddressmoveout.addLine01.sendKeys("Unit 75");
		forwardingaddressmoveout.addLine02.sendKeys("903 David Low Way");
		forwardingaddressmoveout.city.sendKeys("Marcoola");
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
		verifyTwoStringsAreEqual(addLine1, "Unit 75", true);
		verifyTwoStringsAreEqual(addLine2, "903 David Low Way", true);
		verifyStringIsBlank(addLine3);
		verifyStringIsBlank(addLine4);
		verifyTwoStringsAreEqual(forwCity, "Marcoola", true);
		verifyTwoStringsAreEqual(forwState, "Queensland", true);
		verifyTwoStringsAreEqual(forwPostcode, "4564", true);
		verifyTwoStringsAreEqual(forwCountry, "Australia", true);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		scrollPageDown(200);
		// verify per line
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd,
				"Service Address update Unit 20, 239-245 Gympie Terrace Noosaville, Queensland, 4566",
				true);
		verifyTwoStringsAreEqual(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_acn1_acn2"), " (Trading's LLC') ABN/ACN ",
						getProp("test_data_valid_acn2")),
				true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update tom tri ly Email Address: ",
				getProp("test_email_dummy_lower_case"), " Mobile Phone: +61469941139 Contact Secret: (Contact Secret Code)"), true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email, Postal", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Unit 75 903 David Low Way Marcoola, Queensland, 4564 Australia", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update None Specified", true);
		
		// verify the session details saved
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
		
		scrollPageDown(500);
		// let's tick 2 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		
		// add the property files before submitting the request
		addProp("SmallBusExistingContact01_moveOutDate1", this.moveOutDate1);
		addProp("SmallBusExistingContact01_moveOutDate2", this.moveOutDate2);
		addProp("SmallBusExistingContact01_sourceID", this.sourceID);
		addProp("SmallBusExistingContact01_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("SmallBusExistingContact01_dateSubmittedDash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

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