package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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

public class ResiNewContact01 extends MoveOutDevBase {
	
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
	 * Just a another different format date for the Move-Out
	 * */
	private String moveOutDate2;
	
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
		
		// upload the portal_config.css we are using
//		uploadMoveOutCustomCss(s3Access);
		
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
     * For ticket BBPRTL-422
     * 
     * - verify the required fields in the Supply Details section
     * - verify allowed past date in config is 5
     * - verify allowed future date in config is 10
     * - verify the required fields in the supply address
     * - verify only Australia country address is displayed
     * - verify the number of addresses to be displayed
     * - verify users can update the address by searching a new one
     * - verify validations using the Next button
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
						"4 Forwarding Address",
						"5 Additional Note",
						"6 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames,
				assertionErrorMsg(getLineNumber()));
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
		// and users hit Next
		clickElementAction(supplydetailsmoveout.next);
		// we put a pause since Supply Address does not go immediately to error state
		pauseSeleniumExecution(2000);
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Please start typing supply address"), 5, 0),
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
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clearDateField(supplydetailsmoveout.moveOutDate);
		
		// let's get the current date then get a date 11 days in the future
		// verify that an error is returned
		String future11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(future11Days, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.header);
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clearDateField(supplydetailsmoveout.moveOutDate);
		
		// verify the required fields in the supply address
		clickElementAction(supplydetailsmoveout.supplyAddSearch);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmoveout.supplyAddSearch);
		clickElementAction(supplydetailsmoveout.supplyAddCantFindAdd);
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0),
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
		softAssertion.assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's supply a valid Move Out date
		String past5Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.moveOutDate1 = past5Days;
		String pastFiveD = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_DASH);
		this.moveOutDate2 = pastFiveD;
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(past5Days, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		
		// let's supply a supply address from the search field
		clickElementAction(supplydetailsmoveout.supplyAddQuickAddSearch);
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "10/132 Mitcham Road", true, 200);
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
		verifyStringContainsInEachListPacket(addresses1, "Australia", true);
		// choose first one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "10/132 Mitcham Road, Donvale VIC");
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
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "10",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "132",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mitcham",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Road",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Donvale",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3111",
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
		// let's verify we can change the address and the fields are updated
		clickElementAction(supplydetailsmoveout.supplyAddQuickAddSearch);
		// we need to slow down entering the supply address
		// because google address is showing wrong address
		// had to put a slash because of an issue where it does not populate
		// the address correctly
		slowSendKeys(supplydetailsmoveout.supplyAddSearch, "Unit 16/ 6 Mari ST Alexandra Headland", true, 300);
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
		// choose 2nd one from the list
		chooseAddress(supplydetailsmoveout.supplyAddressesDiv, "unit 16/6 Mari St");
		pauseSeleniumExecution(1000);
		// confirm it's populated correctly
		complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyType, true);
		unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		softAssertion.assertTrue(StringUtils.isBlank(complexName),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Unit",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "16",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "6",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mari",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Alexandra Headland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4572",
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
		supplydetailsmoveout.supplyAddComplexName.sendKeys("'x44 Complex's");
		// hit Next button
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
	}
	
	/** 
	 * For ticket BBPRTL-423
	 * 
	 * - verify the required fields
	 * - verify alpha characters not allowed
	 * - verify special characters not allowed
	 * - verify required ABN/ACN
	 * - verify Invalid ABN
	 * - verify Invalid ACN
	 * - verify valid ABN
	 * - verify valid ACN
	 * - verify the previous button
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 2, dependsOnMethods = {"verifySupplyDetails"})
	public void verifyAccountDetails() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify first fields are not in error state
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(accountdetailsmoveout.next);
		// verify the required fields
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify that alpha characters not allowed
		slowSendKeys(accountdetailsmoveout.accountNum, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 200);
		String accountNum = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		softAssertion.assertTrue(StringUtils.isBlank(accountNum),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify special characters not allowed
		slowSendKeys(accountdetailsmoveout.accountNum, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 200);
		accountNum = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		softAssertion.assertTrue(StringUtils.isBlank(accountNum),
				assertionErrorMsg(getLineNumber()));
		
		// put an account number
		accountdetailsmoveout.accountNum.sendKeys("012345678912");
		
		// verify required ABN/ACN
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmoveout.next);
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's verify that we can go to the previous section
		// even if required fields are not yet supplied
		// hit the previous button
		clickElementAction(accountdetailsmoveout.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the supply details section
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		softAssertion.assertEquals(complexName, "'x44 Complex's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(unitNum, "16",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "6",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mari",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Alexandra Headland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4572",
				assertionErrorMsg(getLineNumber()));
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		
		// verify Invalid ABN
		accountdetailsmoveout.abnOrAcn.sendKeys("65 079 509 739");
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(500);
		// hit next again twice to ensure that it did not go to the next section
		clickElementAction(accountdetailsmoveout.next);
		// put a pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit residential then go back to commercial again
		clickElementAction(accountdetailsmoveout.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.abnOrAcn, 0),
				"We are not in the Account Details section");
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		
		// verify Invalid ACN
		accountdetailsmoveout.abnOrAcn.sendKeys("079 509 739");
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(500);
		// hit next again twice to ensure that it did not go to the next section
		clickElementAction(accountcontactmoveout.header);
		// put a pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit residential then go back to commercial again
		clickElementAction(accountdetailsmoveout.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(500);
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.abnOrAcn, 0),
				"We are not in the Account Details section");
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		
		// verify valid ABN
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_abn4"));
		clickElementAction(accountdetailsmoveout.next);
		// put a pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the displayed label
		String abnAcnAndCompany = getDisplayedValue(accountdetailsmoveout.abnOrAcn, true);
		String tradingName = getDisplayedValue(accountdetailsmoveout.tradingName, true);
		softAssertion.assertEquals(
				abnAcnAndCompany, concatStrings(getProp("test_data_valid_abn4"), " (",
						getProp("test_data_valid_company_name_abn3_abn4"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tradingName),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		
		// verify valid ACN
		accountdetailsmoveout.tradingName.sendKeys("Trading's");
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_acn4"), Keys.TAB);
		// put a pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the displayed label
		abnAcnAndCompany = getDisplayedValue(accountdetailsmoveout.abnOrAcn, true);
		tradingName = getDisplayedValue(accountdetailsmoveout.tradingName, true);
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_acn4"), " (",
				getProp("test_data_valid_company_name_acn3_acn4"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's choose residential
		clickElementAction(accountdetailsmoveout.residential);
		// click next
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
	}
	
	/** 
	 * This is for ticket BBPRTL-425
	 * 
	 * - verify the required fields for Residential
	 * - verify Postal notification ticked by default
	 * - verify the Previous button
	 * - verify the Email Address validations
	 * - verify the Date of Birth validations
	 * - verify the validations for the Medicare Card
	 * - verify the validations for the Mobile Phone
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 3, dependsOnMethods = {"verifyAccountDetails"})
	public void verifyMainContactDetails() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify fields are not in error state
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Date of Birth (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.postalNotif, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountcontactmoveout.emailNotif, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code ,  used to speed up verification process"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that Postal notification is ticked by default
		// and Email notification is not ticked
		softAssertion.assertTrue(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.emailNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify SMS is not displayed
		accountcontactmoveout = new AccountContactMoveOut(driver, 0);
		softAssertion.assertFalse(isElementExists(accountcontactmoveout.smsNotifList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// let's un-tick the Postal checkbox
		clickElementAction(accountcontactmoveout.postalNotif);
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		String dateofBirth = getDisplayedValue(accountcontactmoveout.dateOfBirth, true);
		softAssertion.assertTrue(StringUtils.isBlank(dateofBirth),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify it will return an error for email address 'email test@testing.com'
		String invalidEmail = "email test@testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		// verify that the space was automatically trimmed
		softAssertion.assertEquals(getDisplayedValue(accountcontactmoveout.emailAddress, false),
				StringUtils.deleteWhitespace(invalidEmail),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address '@testing.com'
		invalidEmail = "@testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address ' @testing.com'
		invalidEmail = " @testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email~testing.com'
		invalidEmail = "email~testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email`testing.com'
		invalidEmail = "email`testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email!testing.com'
		invalidEmail = "email!testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email#testing.com'
		invalidEmail = "email#testing.com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email@testing,com'
		invalidEmail = "email@testing,com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email@testing/com'
		invalidEmail = "email@testing/com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email@testing<com'
		invalidEmail = "email@testing<com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email@testing>com'
		invalidEmail = "email@testing>com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// verify it will return an error for email address 'email@testing?com'
		invalidEmail = "email@testing?com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();

		// verify it will return an error for email address 'email@testing;com'
		invalidEmail = "email@testing;com";
		clickElementAction(accountcontactmoveout.emailAddress);
		accountcontactmoveout.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		clickElementAction(accountcontactmoveout.emailAddress);
		deleteAllTextFromField();
		
		// click Next
		clickElementAction(accountcontactmoveout.next);
		// let's verify the required fields for Residential
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Date of Birth (DD/MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.postalNotif, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountcontactmoveout.emailNotif, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
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
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code ,  used to speed up verification process"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// click previous
		clickElementAction(accountcontactmoveout.previous);
		pauseSeleniumExecution(1000);
		// confirm we're in the Account details section
		String accountNumAct = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		softAssertion.assertEquals(accountNumAct, "012345678912",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(accountdetailsmoveout.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click next
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(2000);
		
		accountcontactmoveout.firstName.sendKeys("Natsu");
		accountcontactmoveout.lastName.sendKeys("Dragneel's");
		
		// let's choose medicare card and verify the validations
		clickElementAction(accountcontactmoveout.medicareCard);
		scrollPageDown(500);
		// click next
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify that the medicare number and expiry are in error state
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Medicare Card Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Expiry (MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the invalid medicare number
		accountcontactmoveout.medicareCardNumber.sendKeys("2428 77813");
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Medicare Card Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Expiry using the past months of the current year is invalid
		// we get the indexed based months
		int prevMonth = getCurrentMonth(true);
		// if it's January, then we set the previous month as December
		if (prevMonth == 0) {
			prevMonth = 12;
		}
		// if it's January, we should set the year as last year
		int curYear = getCurrentYear();
		if (prevMonth == 12) {
			curYear = curYear - 1;
		}
		String prevMonthStr = Integer.toString(prevMonth);
		String curYearStr = Integer.toString(curYear);
		clickElementAction(accountcontactmoveout.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(accountcontactmoveout.medicareCardExpiry, concatStrings(prevMonthStr, "/", curYearStr), true,
				250);
		accountcontactmoveout.medicareCardExpiry.sendKeys(Keys.TAB);
		// click Medicare Card again to ensure that the calendar collapsed
		clickElementAction(accountcontactmoveout.medicareCard);
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Expiry (MM/YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's clear the medicare number
		clickElementAction(accountcontactmoveout.medicareCardNumber);
		deleteAllTextFromField();
		// let's clear the medicare expiry
		clickElementAction(accountcontactmoveout.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		clearDateField(accountcontactmoveout.medicareCardExpiry);
		accountcontactmoveout.medicareCardNumber.sendKeys("2428 77813 2/1");
		int month = 2;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		clickElementAction(accountcontactmoveout.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(accountcontactmoveout.medicareCardExpiry, concatStrings(monthStr, "/", expYrStr), true, 250);
		accountcontactmoveout.medicareCardExpiry.sendKeys(Keys.TAB);
		// click Medicare Card again to ensure that the calendar collapsed
		clickElementAction(accountcontactmoveout.medicareCard);
		
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_lower_case"));
		// verify the fix in ticket BBPRTL-1143
		String medCareExp = getDisplayedValue(accountcontactmoveout.medicareCardExpiry, true);
		softAssertion.assertTrue(medCareExp.contains(concatStrings(monthStr, "/", expYrStr)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's verify the validations for the mobile phone
		// verify that alpha characters not allowed
		slowSendKeys(accountcontactmoveout.mobilePhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		// verify only + is allowed for special characters
		slowSendKeys(accountcontactmoveout.mobilePhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		String mobPhone = getDisplayedValue(accountcontactmoveout.mobilePhone, true);
		softAssertion.assertEquals(mobPhone, "+",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(accountcontactmoveout.emailAddress);
		clickElementAction(accountcontactmoveout.mobilePhone);
		deleteAllTextFromField();
		accountcontactmoveout.mobilePhone.sendKeys("  012 345  678 9  ");
		mobPhone = getDisplayedValue(accountcontactmoveout.mobilePhone, true);
		// verify users cannot put space
		// enter invalid mobile phone
		softAssertion.assertEquals(mobPhone, "0123456789",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		String medicareExp = getDisplayedValue(accountcontactmoveout.medicareCardExpiry, true);
		this.medCareExpiry = medicareExp;
		
		clickElementAction(accountcontactmoveout.contactSecretCode);
		accountcontactmoveout.contactSecretCode.sendKeys("'007 tralala's");
		clickElementAction(accountcontactmoveout.postalNotif);
		// verify postal notif is ticked
		softAssertion.assertTrue(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the phone numbers in error state
		softAssertion.assertTrue(
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
		
		clickElementAction(accountcontactmoveout.mobilePhone);
		deleteAllTextFromField();
		accountcontactmoveout.mobilePhone.sendKeys("0411234567");
		
		// let's hit next
		clickElementAction(accountcontactmoveout.next);
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
	 * - verify required fields when hitting next
	 * - verify previous button
	 * - verify that the address are overridden/cleared when new address is chosen
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 4, dependsOnMethods = {"verifyMainContactDetails"})
	public void verifyForwAddDetails() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// verify forwarding address is not in error state
		softAssertion.assertFalse(isElementInError(forwardingaddressmoveout.forwAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// hit next button twice to verify required field
		clickElementAction(forwardingaddressmoveout.next);
		// there's a delay before the error is displayed after hitting next
		pauseSeleniumExecution(2000);
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput,
						false, "Please start typing forwarding address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's click the previous button
		clickElementAction(forwardingaddressmoveout.previous);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(accountcontactmoveout.contactSecretCode, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit next from Main Contact
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(forwardingaddressmoveout.forwAddress, 0),
				assertionErrorMsg(getLineNumber()));
		// verify still in error state
		softAssertion.assertTrue(
				isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false,
						"Please start typing forwarding address"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
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
		
		// verify we can go to previous section
		clickElementAction(forwardingaddressmoveout.previous);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(accountcontactmoveout.contactSecretCode, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit next from Main Contact
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine01, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
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
		
		// verify fields are required
		clickElementAction(forwardingaddressmoveout.next);
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

		clickElementAction(forwardingaddressmoveout.quickAddSearch);
		// input the forwarding address
		slowSendKeys(forwardingaddressmoveout.forwAddress, "Unit 805 9 Bowen Bridge RD", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(forwardingaddressmoveout.forwAddressesDiv, "unit 805/9 Bowen Bridge Rd, Bowen Hills QLD");
		pauseSeleniumExecution(1000);
		// let's verify the populated fields
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, false);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, false);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, false);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, false);
		String city = getDisplayedValue(forwardingaddressmoveout.city, false);
		String state = getDisplayedValue(forwardingaddressmoveout.state, false);
		String postcode = getDisplayedValue(forwardingaddressmoveout.postcode, false);
		String country = getDisplayedValue(forwardingaddressmoveout.country, false);
		softAssertion.assertEquals(addLine1, "Unit 805",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addLine2, "9 Bowen Bridge Road",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine4),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Bowen Hills",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4006",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's add an address 3 and 4
		forwardingaddressmoveout.addLine03.sendKeys("Address 03");
		forwardingaddressmoveout.addLine04.sendKeys("Address 04");
		
		// let's change the address
		clickElementAction(forwardingaddressmoveout.quickAddSearch);
		// input the forwarding address
		// test the fix for ticket BBPRTL-785
		slowSendKeys(forwardingaddressmoveout.forwAddress, "Community/40 Mascar St Upper Mount", true, 400);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(forwardingaddressmoveout.forwAddressesDiv, "40 Mascar St, Upper Mount Gravatt QLD");
		pauseSeleniumExecution(1000);
		// let's verify the populated fields
		addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, false);
		addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, false);
		addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, false);
		addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, false);
		city = getDisplayedValue(forwardingaddressmoveout.city, false);
		state = getDisplayedValue(forwardingaddressmoveout.state, false);
		postcode = getDisplayedValue(forwardingaddressmoveout.postcode, false);
		country = getDisplayedValue(forwardingaddressmoveout.country, false);
		softAssertion.assertEquals(addLine1, "Community",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addLine2, "40 Mascar Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine4),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Upper Mount Gravatt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4122",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's click Next button
		clickElementAction(forwardingaddressmoveout.next);
		// put a pause after always after hitting next
		// to fix an issue where wrong elements are clicked
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
	}
	
	/** 
	 * For ticket BBPRTL-427
	 * 
	 * - verify it's not required
	 * - put special characters as notes
	 * - verify we can hit previous
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = {"verifyForwAddDetails"})
	public void verifyAddNotesDetails() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		// let's click next to verify its not required
		clickElementAction(additionalnotemoveout.next);
		pauseSeleniumExecution(1000);
		scrollPageDown(900);
		// confirm we are on the acceptance page
		softAssertion.assertFalse(isElementTicked(acceptancemoveout.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's hit the previous button
		clickElementAction(acceptancemoveout.previous);
		pauseSeleniumExecution(1000);
		scrollPageDown(700);
		
		// enter special notes with special characters
		additionalnotemoveout.notes.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		String notes = getDisplayedValue(additionalnotemoveout.notes, true);
		softAssertion.assertEquals(notes, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify we can hit previous
		clickElementAction(additionalnotemoveout.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the forwarding address
		String addLine1 = getDisplayedValue(forwardingaddressmoveout.addLine01, false);
		String addLine2 = getDisplayedValue(forwardingaddressmoveout.addLine02, false);
		String addLine3 = getDisplayedValue(forwardingaddressmoveout.addLine03, false);
		String addLine4 = getDisplayedValue(forwardingaddressmoveout.addLine04, false);
		String city = getDisplayedValue(forwardingaddressmoveout.city, false);
		String state = getDisplayedValue(forwardingaddressmoveout.state, false);
		String postcode = getDisplayedValue(forwardingaddressmoveout.postcode, false);
		String country = getDisplayedValue(forwardingaddressmoveout.country, false);
		softAssertion.assertEquals(addLine1, "Community",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addLine2, "40 Mascar Street",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addLine4),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Upper Mount Gravatt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4122",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// click next from forwarding details
		clickElementAction(forwardingaddressmoveout.next);
		pauseSeleniumExecution(1000);
		
		// verify we still have values
		notes = getDisplayedValue(additionalnotemoveout.notes, false);
		softAssertion.assertEquals(notes, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit next from notes
		clickElementAction(additionalnotemoveout.next);
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
	 * - verify each section
	 * - forwarding address is optional, so if Postal is not selected the section is not displayed
	 * - verify that the update link works and is being redirected accordingly
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = {"verifyAddNotesDetails"})
	public void verifyAcceptanceDetails() {
		
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		scrollPageDown(900);
		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList(
						"create Supply Details",
						"create Account Details",
						"create Account Contact (Natsu Dragneel's)",
						"create Forwarding Address",
						"create Additional Note",
						"6 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames,
				assertionErrorMsg(getLineNumber()));
		// verify that the checkboxes are not in error state
		softAssertion.assertFalse(isElementInError(acceptancemoveout.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemoveout.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemoveout.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify the displayed texts
		String lbl1stChkBox = getDisplayedText(acceptancemoveout.lblFirstCheckbox, true);
		String lbl2ndChkBox = getDisplayedText(acceptancemoveout.lblSecondCheckbox, true);
		String lbl3rdChkBox = getDisplayedText(acceptancemoveout.lblThirdCheckbox, true);
		softAssertion.assertEquals(lbl1stChkBox,
				"I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lbl2ndChkBox,
				"I/We agree to the Terms and Conditions and also acknowledge BlueOak's Standard Fee Schedule.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lbl3rdChkBox,
				"I/We have added enquiries@blueoak.com.au to my email contacts or white list where required",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// click confirm and verify that checkboxes are in error state
		clickElementAction(acceptancemoveout.submit);
		softAssertion.assertTrue(isElementInError(acceptancemoveout.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemoveout.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemoveout.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		softAssertion.assertEquals(movingOut, concatStrings("Moving Out update ", this.moveOutDate1),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 'x44 Complex's Unit 16, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctContact,
				concatStrings("Account Contact update Natsu Dragneel's Email Address: ",
						getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0411234567 Personal Id: Medicare Card (2428 77813 2/1, ", this.medCareExpiry,
						") Contact Secret: ('007 tralala's)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(finalBillDel, "Final Bill Delivery update Postal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd,
				"Forwarding Address update Community 40 Mascar Street Upper Mount Gravatt, Queensland, 4122 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify that Forwarding Address section would be hidden
		// since it's not required in the portal config
		clickExactLinkNameFromElement(acceptancemoveout.accountContact, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(accountcontactmoveout.postalNotif);
		clickElementAction(accountcontactmoveout.emailNotif);
		// verify that the Postal is unticked
		softAssertion.assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the Email is ticked
		softAssertion.assertTrue(isElementTicked(accountcontactmoveout.emailNotif, 0),
				assertionErrorMsg(getLineNumber()));
		// verify displayed sections
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(
				Arrays.asList(
						"create Supply Details",
						"create Account Details",
						"3 Account Contact (Natsu Dragneel's)",
						"create Additional Note",
						"5 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		scrollPageDown(500);
		// verify per line
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		softAssertion.assertEquals(movingOut, concatStrings("Moving Out update ", this.moveOutDate1),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 'x44 Complex's Unit 16, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctContact,
				concatStrings("Account Contact update Natsu Dragneel's Email Address: ",
						getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0411234567 Personal Id: Medicare Card (2428 77813 2/1, ", this.medCareExpiry,
						") Contact Secret: ('007 tralala's)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(finalBillDel, "Final Bill Delivery update Email",
				assertionErrorMsg(getLineNumber()));
		acceptancemoveout = new AcceptanceMoveOut(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemoveout.forwardingAddressList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(addNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the update link on Moving Out section
		clickExactLinkNameFromElement(acceptancemoveout.movingOut, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Supply Details section
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmoveout.moveOutDate, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(supplydetailsmoveout.supplyAddState);
		deleteAllTextFromField();
		supplydetailsmoveout.supplyAddState.sendKeys("Victoria", Keys.TAB);
		pauseSeleniumExecution(600);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// let's update the state
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify the update link on Service Address section
		clickExactLinkNameFromElement(acceptancemoveout.serviceAddress, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Supply Details section
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmoveout.supplyAddComplexName, 0),
				assertionErrorMsg(getLineNumber()));
		// let's revert the state
		clickElementAction(supplydetailsmoveout.supplyAddState);
		deleteAllTextFromField();
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
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify the update link on Account Details section
		clickExactLinkNameFromElement(acceptancemoveout.accountDetails, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.accountNum, 0), "The Account Number is not displayed");
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify the update link on Account Contact section
		clickExactLinkNameFromElement(acceptancemoveout.accountContact, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0), "The First Name is not displayed");
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify the update link on Account Contact section
		clickExactLinkNameFromElement(acceptancemoveout.finalBillDelivery, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementTicked(accountcontactmoveout.emailNotif, 0), "The Email notification is not ticked");
		scrollPageDown(500);
		// go back to the Acceptance page
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		
		// verify the update link on Additional Notes section
		clickExactLinkNameFromElement(acceptancemoveout.additionalNotes, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Additional Notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "The Notes text area is not displayed");
		scrollPageDown(500);
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
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		softAssertion.assertEquals(movingOut, concatStrings("Moving Out update ", this.moveOutDate1),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 'x44 Complex's Unit 16, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctContact,
				concatStrings("Account Contact update Natsu Dragneel's Email Address: ",
						getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0411234567 Personal Id: Medicare Card (2428 77813 2/1, ", this.medCareExpiry,
						") Contact Secret: ('007 tralala's)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(finalBillDel, "Final Bill Delivery update Email",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's tick all 3 checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		clickElementAction(acceptancemoveout.thirdCheckbox);
	}
	
	/** 
	 * For ticket BBPRTL-584
	 * 
	 * - verify the key names in the session storage
	 * - verify nothing is specified in the local storage
	 * - verify that hitting refresh would not remove the details for each section
	 * - verify validations using the Next button
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = {"verifyAcceptanceDetails"})
	public void verifySessionDetails() {
		
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
		this.sourceID1 = sessionSourceID;
		logDebugMessage(concatStrings("The value of sourceID1 is '", this.sourceID1, "'"));
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify that if we refresh the browser session is not cleared
		refreshBrowser(1, 5000);
		if (getPortalType().equals("embedded")) {
			// let's switch to the Move-Out Iframe
			switchToMoveOutEmbeddedIframe(1);
		}
		loadPortal();
		
		// verify the fix for bug ticket BBPRTL-2111
		// verify the displayed sections
		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList(
						"1 Supply Details",
						"2 Account Details",
						"3 Account Contact (Natsu Dragneel's)",
						"4 Additional Note",
						"5 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames,
				assertionErrorMsg(getLineNumber()));
		
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
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
		
		// verify the expected values in the supply details
		String moveOut = getDisplayedValue(supplydetailsmoveout.moveOutDate, true);
		String complexName = getDisplayedValue(supplydetailsmoveout.supplyAddComplexName, true);
		String unitNum = getDisplayedValue(supplydetailsmoveout.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmoveout.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmoveout.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmoveout.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmoveout.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmoveout.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmoveout.supplyAddPostcode, true);
		verifyTwoStringsAreEqual(moveOut, this.moveOutDate1, true);
		verifyTwoStringsAreEqual(complexName, "'x44 Complex's", true);
		verifyTwoStringsAreEqual(unitNum, "16", true);
		verifyTwoStringsAreEqual(stNum, "6", true);
		verifyTwoStringsAreEqual(stName, "Mari", true);
		verifyTwoStringsAreEqual(stType, "Street", true);
		verifyTwoStringsAreEqual(city, "Alexandra Headland", true);
		verifyTwoStringsAreEqual(state, "Queensland", true);
		verifyTwoStringsAreEqual(postcode, "4572", true);
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmoveout.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// hit Next
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
		
		// verify the expected values in the account details
		String accountNumAct = getDisplayedValue(accountdetailsmoveout.accountNum, true);
		verifyTwoStringsAreEqual(accountNumAct, "012345678912", true);
		assertTrue(isElementTicked(accountdetailsmoveout.residential, 0), "Residential radio is not ticked");
		// hit Next
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0),
				"We are not in the Account Contact section");
		
		// verify the expected values in the account contact
		scrollPageDown(500);
		String firstName = getDisplayedValue(accountcontactmoveout.firstName, true);
		String lastName = getDisplayedValue(accountcontactmoveout.lastName, true);
		String dateOfBirth = getDisplayedValue(accountcontactmoveout.dateOfBirth, true);
		String medicareNum = getDisplayedValue(accountcontactmoveout.medicareCardNumber, true);
		String medicareExp = getDisplayedValue(accountcontactmoveout.medicareCardExpiry, true);
		String emailAdd = getDisplayedValue(accountcontactmoveout.emailAddress, true);
		String mobNum = getDisplayedValue(accountcontactmoveout.mobilePhone, true);
		String busPhone = getDisplayedValue(accountcontactmoveout.businessPhone, true);
		String aHoursPhone = getDisplayedValue(accountcontactmoveout.afterhoursPhone, true);
		String secretCode = getDisplayedValue(accountcontactmoveout.contactSecretCode, true);
		verifyTwoStringsAreEqual(firstName, "Natsu", true);
		verifyTwoStringsAreEqual(lastName, "Dragneel's", true);
		verifyStringIsBlank(dateOfBirth);
		assertFalse(isElementTicked(accountcontactmoveout.driversLicence, 0), "Drivers Licence is ticked");
		assertFalse(isElementTicked(accountcontactmoveout.passport, 0), "Passport is ticked");
		assertTrue(isElementTicked(accountcontactmoveout.medicareCard, 0), "Medicare Card is not ticked");
		assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0), "Postal Notification is ticked");
		assertTrue(isElementTicked(accountcontactmoveout.emailNotif, 0), "Email Notification is not ticked");
		verifyTwoStringsAreEqual(medicareNum, "2428 77813 2/1", true);
		verifyTwoStringsAreEqual(medicareExp, this.medCareExpiry, true);
		verifyTwoStringsAreEqual(emailAdd, getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(mobNum, "0411234567", true);
		verifyStringIsBlank(busPhone);
		verifyStringIsBlank(aHoursPhone);
		verifyTwoStringsAreEqual(secretCode, "'007 tralala's", true);
		// hit Next
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Additional note section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0),
				"We are not in the Additional Note section");
		
		// verify the expected values in the Additional note
		String notes = getDisplayedValue(additionalnotemoveout.notes, true);
		verifyTwoStringsAreEqual(notes, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true);
		// hit Next
		clickElementAction(additionalnotemoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
		
		// verify the expected values in the Acceptance page
		scrollPageDown(900);
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		softAssertion.assertEquals(movingOut, concatStrings("Moving Out update ", this.moveOutDate1),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 'x44 Complex's Unit 16, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctContact,
				concatStrings("Account Contact update Natsu Dragneel's Email Address: ",
						getProp("test_email_dummy_lower_case"),
						" Mobile Phone: 0411234567 Personal Id: Medicare Card (2428 77813 2/1, ", this.medCareExpiry,
						") Contact Secret: ('007 tralala's)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(finalBillDel, "Final Bill Delivery update Email",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		acceptancemoveout = new AcceptanceMoveOut(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemoveout.forwardingAddressList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertFalse(isElementTicked(acceptancemoveout.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemoveout.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemoveout.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		sessionSourceID = storage.getItemFromSessionStorage("source_id");
		this.sourceID2 = sessionSourceID;
		logDebugMessage(concatStrings("The value of sourceID2 is '", this.sourceID2, "'"));
		// verify that they are no longer the same
		softAssertion.assertNotEquals(sourceID1, sourceID2,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's tick the checkboxes
		clickElementAction(acceptancemoveout.firstCheckbox);
		clickElementAction(acceptancemoveout.secondCheckbox);
		clickElementAction(acceptancemoveout.thirdCheckbox);
		
		// add the property files before submitting the request
		addProp("ResiNewContact01_moveOutDate1", this.moveOutDate1);
		addProp("ResiNewContact01_medCareExpiry", this.medCareExpiry);
		addProp("ResiNewContact01_moveOutDate3", this.moveOutDate2);
		addProp("ResiNewContact01_sourceID", this.sourceID2);
		addProp("ResiNewContact01_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiNewContact01_dateSubmittedDash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));
		
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
