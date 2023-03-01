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

public class SmallBusExistingContact02 extends MoveOutDevBase {
	
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
		uploadMoveOutConfig(s3Access, "02\\", "portal_config.json");
		
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
     * - verify that the address lookup is disabled hence all fields displayed
     * - verify the hidden fields for the address lookup
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
		// let's verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD-MM-YYYY)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(
								getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// hit header of the next section and confirm some fields in error state
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
    	assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD-MM-YYYY)"), 5, 0), "Move Out Date is in Error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0), "Complex Name is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0), "Tenancy Type is not in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0), "Tenancy Number is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0), "Street Number is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0), "Street Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0), "Street Type is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0), "City is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0), "State is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0), "Postcode is not error state");
		
		// hit next button and confirm same fields still in error state
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
    	assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD-MM-YYYY)"), 5, 0), "Move Out Date is in Error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0), "Complex Name is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0), "Tenancy Type is not in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0), "Tenancy Number is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0), "Street Number is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0), "Street Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0), "Street Type is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0), "City is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0), "State is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0), "Postcode is not error state");
		
		// verify the lookup address is not displayed
		supplydetailsmoveout = new SupplyDetailsMoveOut(driver, 1);
		assertFalse(isElementExists(supplydetailsmoveout.supplyAddSearchList), "The Supply Address Search field is displayed");
		assertFalse(isElementExists(supplydetailsmoveout.supplyAddQuickAddSearchList), "The Supply Address Quick Search link is displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		
		// populate move out date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
		this.moveOutDate1 = today;
		this.moveOutDate2 = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmoveout.moveOutDate);
		pauseSeleniumExecution(1000);
		supplydetailsmoveout.moveOutDate.sendKeys(today, Keys.TAB);
		// click the button to dismiss the calendar
		clickElementAction(supplydetailsmoveout.datePickerMoveOutDate);
		// verify Tenancy Type, Street Type and State returns an error if non-existent
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Community");
		supplydetailsmoveout.supplyAddStreetName.sendKeys("Bowen");
		supplydetailsmoveout.supplyAddStreetType.sendKeys("Purok");
		supplydetailsmoveout.supplyAddCity.sendKeys("Sydney");
		supplydetailsmoveout.supplyAddState.sendKeys("East Blue");
		supplydetailsmoveout.supplyAddPostcode.sendKeys("90210");
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD-MM-YYYY)"), 5, 0), "Move Out Date is in Error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0), "Complex Name is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0), "Tenancy Type is not in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0), "Tenancy Number is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0), "Street Number is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0), "Street Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0), "Street Type is not error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0), "City is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0), "State is not error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0), "Postcode is not error state");
		
		// clear the value in the Tenancy Type
		clickElementAction(supplydetailsmoveout.supplyAddTenancyType);
		deleteAllTextFromField();
		// clear the value in the Street Type
		clickElementAction(supplydetailsmoveout.supplyAddStreetType);
		deleteAllTextFromField();
		// clear the value in the State
		clickElementAction(supplydetailsmoveout.supplyAddState);
		deleteAllTextFromField();
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("apartment", Keys.TAB);
		supplydetailsmoveout.supplyAddStreetType.sendKeys("street");
		supplydetailsmoveout.supplyAddState.sendKeys("south australia", Keys.TAB);
		clickElementAction(supplydetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify Street Type and State returns an error if value is entered in lower case
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Move Out Date (DD-MM-YYYY)"), 5, 0), "Move Out Date is in Error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Complex Name (if known)"), 5, 0), "Complex Name is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Type"), 5, 0), "Tenancy Type is not in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Tenancy Number"), 5, 0), "Tenancy Number is not in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Number"), 5, 0), "Street Number is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Name"), 5, 0), "Street Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Street Type"), 5, 0), "Street Type is not error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "City/Suburb"), 5, 0), "City is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "State"), 5, 0), "State is not error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(supplydetailsmoveout.labelInput, false, "Postcode"), 5, 0), "Postcode is not error state");
		
		// clear the value in the Tenancy Type
		clickElementAction(supplydetailsmoveout.supplyAddTenancyType);
		deleteAllTextFromField();
		// clear the value in the Street Type
		clickElementAction(supplydetailsmoveout.supplyAddStreetType);
		deleteAllTextFromField();
		// clear the value in the State
		clickElementAction(supplydetailsmoveout.supplyAddState);
		deleteAllTextFromField();
		// verify the fix for bug ticket BBPRTL-2112
		supplydetailsmoveout.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		supplydetailsmoveout.supplyAddStreetType.sendKeys("Cul-de-sac");
		clickElementAction(supplydetailsmoveout.supplyAddState);
		supplydetailsmoveout.supplyAddState.sendKeys("Australian Capital Territory", Keys.TAB);
		pauseSeleniumExecution(600);
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
		// verify that we are in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.residential, 0), "We are not in the Account Details section");
	}

    /** 
     * - verify an invalid account number
     * - verify the required fields
     * - put an invalid format ABN/ACN (tweaked a valid abn/acn to be invalid)
     * - put a cancelled ABN/ACN and verify it would allow to go to the next section
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
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false,
						"BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify the required fields
		// verify the fix for bug ticket BBPRTL-2114
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false,
						"BlueBilling Account Number"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.residential, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmoveout.commercial, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// verify the invalid account number
		accountdetailsmoveout.accountNum.sendKeys("0621 697 890");
		clickElementAction(accountdetailsmoveout.commercial);
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		String errorMsg = getDisplayedText(accountdetailsmoveout.accountNumMsgError.get(0), true);
		softAssertion.assertEquals(errorMsg, "Invalid Account Number",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-621
		softAssertion.assertTrue(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// clear the account number
		clickElementAction(accountdetailsmoveout.accountNum);
		deleteAllTextFromField();
		accountdetailsmoveout.accountNum.sendKeys("100060003595");
		clickElementAction(accountdetailsmoveout.companyName);
		accountdetailsmoveout = new AccountDetailsMoveOut(driver, 1);
		// verify no error is returned
		softAssertion.assertFalse(isElementExists(accountdetailsmoveout.accountNumMsgWarning),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
    	// put an invalid ABN
		accountdetailsmoveout.companyName.sendKeys("Invalid ABN");
    	accountdetailsmoveout.abnOrAcn.sendKeys("06 473 0581 442");
    	// click next
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(500);
		// hit next again twice to ensure that it did not go to the next section
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		// verify the fix in ticket BBPRTL-622
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
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
		// verify the fix in ticket BBPRTL-622
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		clickElementAction(accountdetailsmoveout.companyName);
		deleteAllTextFromField();
		
    	// put an invalid ACN
    	accountdetailsmoveout.abnOrAcn.sendKeys("079 509 739");
    	accountdetailsmoveout.companyName.sendKeys("Invalid ACN");
    	// click next
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(500);
		// hit next again twice to ensure that it did not go to the next section
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify the fix in ticket BBPRTL-622
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
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
		// verify the fix in ticket BBPRTL-622
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		clickElementAction(accountdetailsmoveout.companyName);
		deleteAllTextFromField();
		
		// let's put a valid ACN
    	clickElementAction(accountdetailsmoveout.abnOrAcn);
    	deleteAllTextFromField();
		clickElementAction(accountdetailsmoveout.companyName);
		deleteAllTextFromField();
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_valid_acn3"));
		accountdetailsmoveout.companyName.sendKeys("Valid ACN");
		clickElementAction(accountdetailsmoveout.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmoveout.commercial);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		// add 1 to make it invalid
		accountdetailsmoveout.abnOrAcn.sendKeys("1");
		clickElementAction(accountdetailsmoveout.next);
		pauseSeleniumExecution(1000);
		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the Account Details section
		assertTrue(isElementDisplayed(accountdetailsmoveout.abnOrAcn, 0),
				"We are not in the Account Details section");
		// verify the fix in ticket BBPRTL-622
		softAssertion.assertTrue(
				isElementInError(
						getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company ABN or ACN"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Company Name/Entity Name"), 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getElementFrmLblNameInput(accountdetailsmoveout.labelInput, false, "Trading Name"), 5,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		// let's put a cancelled ACN but correct format
		clickElementAction(accountdetailsmoveout.abnOrAcn);
		deleteAllTextFromField();
		clickElementAction(accountdetailsmoveout.companyName);
		deleteAllTextFromField();
		// verify empty
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmoveout.abnOrAcn, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmoveout.companyName, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		accountdetailsmoveout.abnOrAcn.sendKeys(getProp("test_data_cancelled_acn2"));
		accountdetailsmoveout.companyName.sendKeys(getProp("test_data_cancelled_acn1_acn2"));
		accountdetailsmoveout.tradingName.sendKeys("OP LLC's");
		// let's verify the value
		softAssertion.assertEquals(getDisplayedValue(accountdetailsmoveout.abnOrAcn, true),
				getProp("test_data_cancelled_acn2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(accountdetailsmoveout.companyName, true),
				getProp("test_data_cancelled_acn1_acn2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(accountdetailsmoveout.tradingName, true), "OP LLC's",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountcontactmoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Account Contact section
		assertTrue(isElementDisplayed(accountcontactmoveout.firstName, 0), "We are not in the Account Contact section");
	}
    
    /** 
     * 
     * - verify even if Postal is not ticked, section still displayed since required
     * - verify that email and sms not required, but if Email and SMS is ticked, Bill Delivery should be required
     * 
     * */
    @Test(priority = 3, dependsOnMethods = {"verifyAccountDetails"})
    public void verifyAccountContact() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
    	
    	// verify the fields are not in error state
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				"First Name is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5, 0),
				"Last/Family Name is in error state");
		assertFalse(isElementInError(accountcontactmoveout.postalNotif, 0, 3), "Postal notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.emailNotif, 0, 3), "Email notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.smsNotif, 0, 3), "SMS notification is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"), 5, 0),
				"Mobile Phone Number is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				"Business Hours Phone Number is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				"After Hours Phone Number is in error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code (optional),  used to speed up verification process"), 5, 0),
				"Contact Secret Code , used to speed up verification process is in error state");
		// verify that Postal notification is not ticked by default
		// and Email and SMS notification are ticked by default
		assertFalse(isElementTicked(accountcontactmoveout.postalNotif, 0), "Postal notification is ticked by default");
		assertTrue(isElementTicked(accountcontactmoveout.emailNotif, 0), "Email notification is not ticked by default");
		assertTrue(isElementTicked(accountcontactmoveout.smsNotif, 0), "SMS notification is not ticked by default");
		// un tick Email and SMS
		clickElementAction(accountcontactmoveout.emailNotif);
		clickElementAction(accountcontactmoveout.smsNotif);
		accountcontactmoveout = new AccountContactMoveOut(driver, 1);
		// verify Date of Birth is not displayed
		assertFalse(isElementExists(accountcontactmoveout.dateOfBirthList), "Date of Birth is displayed");
		assertFalse(isElementExists(accountcontactmoveout.driversLicenceList), "Australian Drivers Licence is displayed");
		assertFalse(isElementExists(accountcontactmoveout.passportList), "Passport is displayed");
		assertFalse(isElementExists(accountcontactmoveout.medicareCardList), "Medicare card is displayed");
		
		// verify the required fields
    	clickElementAction(forwardingaddressmoveout.header);
    	pauseSeleniumExecution(1000);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				"First Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5, 0),
				"Last/Family Name is not error state");
		assertFalse(isElementInError(accountcontactmoveout.postalNotif, 0, 3), "Postal notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.emailNotif, 0, 3), "Email notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.smsNotif, 0, 3), "SMS notification is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is in error state");
		assertFalse(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"), 5, 0),
				"Mobile Phone Number is in error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				"Business Hours Phone Number is not error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				"After Hours Phone Number is not error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code (optional),  used to speed up verification process"), 5, 0),
				"Contact Secret Code , used to speed up verification process is in error state");
		
		// verify the email and SMS are required when ticked
		clickElementAction(accountcontactmoveout.emailNotif);
		clickElementAction(accountcontactmoveout.smsNotif);
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "First Name"), 5, 0),
				"First Name is not error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Last/Family Name"), 5, 0),
				"Last/Family Name is not error state");
		assertFalse(isElementInError(accountcontactmoveout.postalNotif, 0, 3), "Postal notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.emailNotif, 0, 3), "Email notification is in error state");
		assertFalse(isElementInError(accountcontactmoveout.smsNotif, 0, 3), "SMS notification is in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Email Address"), 5, 0),
				"Email Address is not in error state");
		assertTrue(isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Mobile Phone Number"), 5, 0),
				"Mobile Phone Number is not in error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Business Hours Phone Number"), 5, 0),
				"Business Hours Phone Number is not error state");
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "After Hours Phone Number"), 5, 0),
				"After Hours Phone Number is not error state");
		assertFalse(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput,
						false, "Contact Secret Code (optional),  used to speed up verification process"), 5, 0),
				"Contact Secret Code , used to speed up verification process is in error state");
		
		accountcontactmoveout.firstName.sendKeys("Justin");
		accountcontactmoveout.lastName.sendKeys("O'day");
		accountcontactmoveout.emailAddress.sendKeys(getProp("test_email_dummy_upper_case"));
		accountcontactmoveout.mobilePhone.sendKeys("03 3287 8850");
		accountcontactmoveout.businessPhone.sendKeys("+612 987 10987");
		accountcontactmoveout.afterhoursPhone.sendKeys("+61 486 501 260");
		accountcontactmoveout.contactSecretCode.sendKeys("sekrekt's code #01");
		
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
		
		// verify the labels
		String postalBillLbl = getDisplayedText(accountcontactmoveout.lblPostalNotif, true);
		String emailBillLbl = getDisplayedText(accountcontactmoveout.lblEmailNotif, true);
		String smsBillLbl = getDisplayedText(accountcontactmoveout.lblSMSNotif, true);
		// verify the fix for ticket BBPRTL-1118
		verifyTwoStringsAreEqual(postalBillLbl, "Postal(additional fees apply for mailing bills)", true);
		verifyTwoStringsAreEqual(emailBillLbl, "Email", true);
		verifyTwoStringsAreEqual(smsBillLbl, "SMS", true);
		
		clickElementAction(accountcontactmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Forwarding Address section
		assertTrue(isElementDisplayed(forwardingaddressmoveout.addLine01, 0),
				"We are not in the Forwarding Address section");
    }
    
    /** 
     * - verify all fields displayed since lookup disabled
     * 
     * */
    @Test(priority = 4, dependsOnMethods = {"verifyAccountContact"})
    public void verifyForwdingAddress() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
    	
		// verify the lookup field does not exist
		forwardingaddressmoveout = new ForwardingAddressMoveOut(driver, 0);
		softAssertion.assertFalse(isElementExists(forwardingaddressmoveout.forwAddressList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(forwardingaddressmoveout.quickAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify fields are not in error state
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
    	
		clickElementAction(forwardingaddressmoveout.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
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
		softAssertion
				.assertTrue(
						isElementInError(
								getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0),
						assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
    	
    	// populate fields
    	forwardingaddressmoveout.addLine01.sendKeys("Address-#01");
    	forwardingaddressmoveout.addLine02.sendKeys("Address-#02");
    	forwardingaddressmoveout.addLine03.sendKeys("Address-#03");
    	forwardingaddressmoveout.addLine04.sendKeys("Address-#04");
    	forwardingaddressmoveout.city.sendKeys("Dressrosa");
    	forwardingaddressmoveout.state.sendKeys("East Blue");
    	forwardingaddressmoveout.postcode.sendKeys("90210");
    	forwardingaddressmoveout.country.sendKeys("Mariejoe");
    	clickElementAction(forwardingaddressmoveout.next);
    	pauseSeleniumExecution(500);
    	
		clickElementAction(additionalnotemoveout.header);
		pauseSeleniumExecution(1000);
		// verify country would be in error state if populated with non-existent country
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
    	
    	// clear the country
    	clickElementAction(forwardingaddressmoveout.country);
    	deleteAllTextFromField();
		// verify country would be in error state if country specified is in lower case
		forwardingaddressmoveout.country.sendKeys("australia", Keys.TAB);
		clickElementAction(additionalnotemoveout.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(getElementFrmLblNameInput(forwardingaddressmoveout.labelInput, false, "Country"), 5, 0), "Country is in error state");
		
		// clear the country
    	clickElementAction(forwardingaddressmoveout.country);
    	deleteAllTextFromField();
		slowSendKeys(forwardingaddressmoveout.country, "united", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are not only from Australia
		List<String> countries = null;
		try {
			countries = getAllMatOptionsValues(forwardingaddressmoveout.countriesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			forwardingaddressmoveout = new ForwardingAddressMoveOut(driver);
			countries = getAllMatOptionsValues(forwardingaddressmoveout.countriesDiv);
		}
		verifyStringContainsInEachListPacket(countries, "united", false);
		// choose 4th one from the list
		chooseFromList(forwardingaddressmoveout.forwAddressesDiv, 4);
		pauseSeleniumExecution(500);
		
		clickElementAction(forwardingaddressmoveout.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		assertTrue(isElementDisplayed(additionalnotemoveout.notes, 0), "We are not in the Additional Note section");
    }
    
    /** 
     * 
     * - verify the limit in the additional note
     * */
    @Test(priority = 5, dependsOnMethods = {"verifyForwdingAddress"})
    public void verifyAdditionalNotes() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
    	
    	additionalnotemoveout.notes.sendKeys("The quick brown fox jumps over the lazy dog. Again and Again...");
    	String notes = getDisplayedValue(additionalnotemoveout.notes, true);
    	verifyTwoStringsAreEqual(notes, "The quick brown fox jumps over", true);
    	
    	clickElementAction(additionalnotemoveout.next);
    	pauseSeleniumExecution(1000);
		// verify we are in the Acceptance section
		assertTrue(isElementDisplayed(acceptancemoveout.acceptanceIntroMsg, 0),
				"We are not in the Acceptance section");
    }
    
    /** 
     * 
     * 
     * 
     * */
    @Test(priority = 6, dependsOnMethods = {"verifyAdditionalNotes"})
    public void verifyAcceptance() {
    	
		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
    	
		scrollPageDown(500);
    	// verify per line
		String movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		String servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		String acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		String acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		String finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		String forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		String addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update Bowen Cul-de-sac Sydney, Australian Capital Territory, 90210", true);
		verifyTwoStringsAreEqual(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_cancelled_acn1_acn2"), " (OP LLC's) ABN/ACN ", getProp("test_data_cancelled_acn2")),
				true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update Justin O'day Email Address: ",
				getProp("test_email_dummy_upper_case"),
				" Mobile Phone: 0332878850 Business Phone: +61298710987 A/Hours Phone: +61486501260 Contact Secret: (sekrekt's code #01)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email, SMS", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Address-#01 Address-#02 Address-#03 Address-#04 Dressrosa, East Blue, 90210 United States", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update The quick brown fox jumps over", true);
		
		// update the account to Residential
		clickExactLinkNameFromElement(acceptancemoveout.accountDetails, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmoveout.residential);
		scrollPageDown(600);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// we are redirected in the Account Contact and we expect date of birth
		// and personal identification to be in error state
		assertTrue(
				isElementInError(getElementFrmLblNameInput(accountcontactmoveout.labelInput, false, "Date of Birth (DD-MM-YYYY)"), 5, 0),
				"Date of Birth (DD-MM-YYYY) is not in error state");
		assertTrue(isElementInError(accountcontactmoveout.driversLicence, 5, 0), "Australian Drivers Licence is not in error state");
		assertTrue(isElementInError(accountcontactmoveout.passport, 5, 0), "Passport is not in error state");
		assertTrue(isElementInError(accountcontactmoveout.medicareCard, 5, 0), "Medicare Card is not in error state");
		assertTrue(isElementInError(accountcontactmoveout.provideNone, 5, 0), "Provide None is not in error state");
		// go back to the Account Contact
		// and choose Commercial again
		// previous details should still be there
		clickElementAction(accountdetailsmoveout.header);
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmoveout.commercial);
		String abnacn = getDisplayedValue(accountdetailsmoveout.abnOrAcn, true);
		String company = getDisplayedValue(accountdetailsmoveout.companyName, true);
		String trading = getDisplayedValue(accountdetailsmoveout.tradingName, true);
		verifyTwoStringsAreEqual(abnacn, getProp("test_data_cancelled_acn2"), true);
		verifyTwoStringsAreEqual(company, getProp("test_data_cancelled_acn1_acn2"), true);
		verifyTwoStringsAreEqual(trading, "OP LLC's", true);
		scrollPageDown(500);
		clickElementAction(acceptancemoveout.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Acceptance Page and confirm fix for ticket BBPRTL-625
		assertTrue(isElementDisplayed(acceptancemoveout.submit, 0), "Submit button is not displayed");
		
		// verify per lines
		movingOut = getDisplayedText(acceptancemoveout.movingOut, true);
		servAdd = getDisplayedText(acceptancemoveout.serviceAddress, true);
		acctDetails = getDisplayedText(acceptancemoveout.accountDetails, true);
		acctContact = getDisplayedText(acceptancemoveout.accountContact, true);
		finalBillDel = getDisplayedText(acceptancemoveout.finalBillDelivery, true);
		forwAdd = getDisplayedText(acceptancemoveout.forwardingAddress, true);
		addNotes = getDisplayedText(acceptancemoveout.additionalNotes, true);
		verifyTwoStringsAreEqual(movingOut, "Moving Out update " + this.moveOutDate1, true);
		verifyTwoStringsAreEqual(servAdd, "Service Address update Bowen Cul-de-sac Sydney, Australian Capital Territory, 90210", true);
		verifyTwoStringsAreEqual(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_cancelled_acn1_acn2"), " (OP LLC's) ABN/ACN ", getProp("test_data_cancelled_acn2")),
				true);
		verifyTwoStringsAreEqual(acctContact, concatStrings("Account Contact update Justin O'day Email Address: ",
				getProp("test_email_dummy_upper_case"),
				" Mobile Phone: 0332878850 Business Phone: +61298710987 A/Hours Phone: +61486501260 Contact Secret: (sekrekt's code #01)"),
				true);
		verifyTwoStringsAreEqual(finalBillDel, "Final Bill Delivery update Email, SMS", true);
		verifyTwoStringsAreEqual(forwAdd, "Forwarding Address update Address-#01 Address-#02 Address-#03 Address-#04 Dressrosa, East Blue, 90210 United States", true);
		verifyTwoStringsAreEqual(addNotes, "Additional Note update The quick brown fox jumps over", true);
    }
    
    /** 
     * - verify no session details is saved
     * 
     * */
    @Test(priority = 7, dependsOnMethods = {"verifyAcceptance"})
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
		// verify all assertions
		softAssertion.assertAll();
		
		scrollPageDown(900);
		// verify that the checkboxes are not displayed
		acceptancemoveout = new AcceptanceMoveOut(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemoveout.firstCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemoveout.secondCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemoveout.thirdCheckboxList),
				assertionErrorMsg(getLineNumber()));
		// verify that the text for the checkboxes are not displayed
		softAssertion.assertFalse(isElementExists(acceptancemoveout.lblFirstCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemoveout.lblSecondCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemoveout.lblThirdCheckboxList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		
		// add the property files before submitting the request
		addProp("SmallBusExistingContact02_moveOutDate1", this.moveOutDate1);
		addProp("SmallBusExistingContact02_moveOutDate2", this.moveOutDate2);
		addProp("SmallBusExistingContact02_sourceID", this.sourceID);
		addProp("SmallBusExistingContact02_dateSubmittedSlash", getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		
		// click the submit button
		clickElementAction(acceptancemoveout.submit);
		// did this because there was an instance where the first click
		// did not submit the request
		acceptancemoveout = new AcceptanceMoveOut(driver, 0);
		boolean isDisplayed = isElementExists(acceptancemoveout.progressBarList);
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (!isDisplayed) {
			logDebugMessage("The initial click did not submit the request, trying again now.");
			// let's click it again
			clickElementAction(acceptancemoveout.submit);
		}
		
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
