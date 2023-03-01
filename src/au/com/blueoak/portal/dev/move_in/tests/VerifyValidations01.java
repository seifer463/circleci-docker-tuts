package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_in.MoveInDevBase;
import au.com.blueoak.portal.pageObjects.move_in.AcceptanceMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AccountDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalNoteMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ConcessionMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.pageObjects.move_out.SupplyDetailsMoveOut;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class VerifyValidations01 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
	TradeWasteMoveIn tradewastemovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
	PostalAddressMoveIn postaladdressmovein;
	ConcessionMoveIn concessionmovein;
	DirectDebitMoveIn directdebitmovein;
	AdditionalNoteMoveIn additionalnotemovein;
	AcceptanceMoveIn acceptancemovein;
	PortalMoveIn portalmovein;
	AccessS3BucketWithVfs s3Access;

	/**
	 * Store the name of the class for logging
	 */
	String className;

	/** 
	 * 
	 * */
	String residentialMoveInDate;

	/** 
	 *
	 * */
	String commercialMoveInDate;

	/** 
	 * 
	 * */
	String initialDate3rdPartyPref;

	/** 
	 * 
	 * */
	String sourceID;

	/**
	 * Use {@link #populateAllSectionsResidential()} instead
	 * 
	 * @param testCaseName     the name of the test case that use this method
	 * @param readOnlySuppAdd  pass true if it's read only on the portal config
	 *                         being used
	 * @param readOnlyAcctType pass true if it's read only on the portal config
	 *                         being used
	 * 
	 */
	private void populateAllSectionsResidential(String testCaseName, boolean readOnlySuppAdd,
			boolean readOnlyAcctType) {

		long startTime = logNanoTimeStamp();
		long totalStartime = startTime;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				// let's make sure the session storage is cleared
				// since it's supposed to be cleared in the previous test case
				clearLocalAndSessionStorage();
				refreshBrowser(1, 5000);
			}
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				// let's make sure the session storage is cleared
				// since it's supposed to be cleared in the previous test case
				clearLocalAndSessionStorage();
				refreshBrowser(1, 5000);
			}
			loadEmbeddedMoveInPortal(true, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		long endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime, endTime)), ">"));
		logDebugMessage("Will start populating the Supply Details until Additional Notes section");

		/*
		 * README Start of Supply Details section
		 */

		startTime = logNanoTimeStamp();
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		// let's verify the header and introduction
		if (getPortalType().equals("standalone")) {
			String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertEquals(header, "Selenium BlueBilling Move In Request",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded")) {
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		}
		// verify the radio buttons are not selected
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			if (readOnlySuppAdd) {
				boolean isEditable;
				try {
					// try to enter values in read only fields
					supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
					supplydetailsmovein.supplyAddStreetNum.sendKeys("Test StreetNum");
					supplydetailsmovein.supplyAddStreetName.sendKeys("Test StreetName");
					supplydetailsmovein.supplyAddStreetType.sendKeys("Road");
					supplydetailsmovein.supplyAddCity.sendKeys("Test City");
					supplydetailsmovein.supplyAddState.sendKeys("Victoria");
					supplydetailsmovein.supplyAddPostcode.sendKeys("90210");
					isEditable = true;
				} catch (ElementNotInteractableException enie) {
					isEditable = false;
				}
				softAssertion.assertFalse(isEditable, assertionErrorMsg(getLineNumber()));
			}
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			if (readOnlySuppAdd) {
				boolean isEditable;
				try {
					// try to enter values in read only fields
					supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
					supplydetailsmovein.supplyAddStreetNum.sendKeys("Test StreetNum");
					supplydetailsmovein.supplyAddStreetName.sendKeys("Test StreetName");
					supplydetailsmovein.supplyAddStreetType.sendKeys("Road");
					supplydetailsmovein.supplyAddCity.sendKeys("Test City");
					supplydetailsmovein.supplyAddState.sendKeys("Victoria");
					supplydetailsmovein.supplyAddPostcode.sendKeys("90210");
					isEditable = true;
				} catch (ElementNotInteractableException enie) {
					isEditable = false;
				}
				softAssertion.assertFalse(isEditable, assertionErrorMsg(getLineNumber()));
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyConnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyDisconnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyUnknown, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the required fields for the Tenant date
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.tenant);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();
		// verify required fields for the Owner date
		clickElementAction(supplydetailsmovein.owner);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// click Next to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the required fields for the Property Manager or Letting Agent
		clickElementAction(supplydetailsmovein.propManager);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// click Next to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.tenant);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
							"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the expected sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Postal Address", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// clear the value on the prefill
			clearDateField(supplydetailsmovein.moveInDateTenant);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the fix for bug ticket BBPRTL-1988
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
							"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		String past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(past11Days, Keys.TAB);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDateTenant);
		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		String future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future21Days, Keys.TAB);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDateTenant);
		// let's put a valid lease commencement date as 10 days from past
		String past10Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.residentialMoveInDate = past10Days;
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(past10Days, Keys.TAB);
		// click button again to dismiss the calendar
		clickElementAction(supplydetailsmovein.tenant);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the required fields in the supply address
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddComplexName.sendKeys("Testing Complex");
			// verify error is displayed for tenancy type entered in lower case
			supplydetailsmovein.supplyAddTenancyType.sendKeys("unit", Keys.TAB);
			supplydetailsmovein.supplyAddStreetNum.sendKeys("Testing Street Num");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Testing Street Name");
			// verify error is displayed for street type entered in lower case
			supplydetailsmovein.supplyAddStreetType.sendKeys("alley", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Testing City/Suburb");
			// verify error is displayed for state entered in lower case
			supplydetailsmovein.supplyAddState.sendKeys("tasmania", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("Testing Postcode");
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddTenancyNum);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetNum);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetName);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddCity);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("16");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("6");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Mari");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Alexandra Headland");
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("4572");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddComplexName.sendKeys("Testing Complex");
			// verify error is displayed for tenancy type entered in lower case
			supplydetailsmovein.supplyAddTenancyType.sendKeys("unit", Keys.TAB);
			supplydetailsmovein.supplyAddStreetNum.sendKeys("Testing Street Num");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Testing Street Name");
			// verify error is displayed for street type entered in lower case
			supplydetailsmovein.supplyAddStreetType.sendKeys("alley", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Testing City/Suburb");
			// verify error is displayed for state entered in lower case
			supplydetailsmovein.supplyAddState.sendKeys("tasmania", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("Testing Postcode");
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddTenancyNum);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetNum);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetName);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddCity);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("16");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("6");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Mari");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Alexandra Headland");
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("4572");
		}
		// verify populated correctly
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
			// let's put a complex name
			supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("16");
			if (!readOnlySuppAdd) {
				// let's put a complex name
				supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "6", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mari", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Alexandra Headland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4572", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// choose supply connected
		clickElementAction(supplydetailsmovein.supplyConnected);
		// verify the Life Support Introduction
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
				"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
				assertionErrorMsg(getLineNumber()));
		// choose yes for Life Support
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// verify fields not in error state
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
				"Please select one or more life support devices in-use:", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Intermittent Peritoneal Dialysis Machine"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 3,
				0), assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Chronic Positive Airways Pressure Respirator"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(450);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Intermittent Peritoneal Dialysis Machine"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 3,
				0), assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Chronic Positive Airways Pressure Respirator"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		// verify upload section and medical cooling in error state
		// verify fix for ticket BBPRTL-1156
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify nothing is uploaded
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
				assertionErrorMsg(getLineNumber()));
		scrollPageDown(300);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.medCoolingYes);
		scrollPageDown(500);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// choose all options for Life Support Equipment
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Intermittent Peritoneal Dialysis Machine"));
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Chronic Positive Airways Pressure Respirator"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Crigler Najjar Syndrome Phototherapy Equipment"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Ventilator for Life Support"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"));
		// verify the Other option is in error state
		String errorMsg = getDisplayedText(supplydetailsmovein.hintLifeSuppOtherTextField, true);
		softAssertion.assertEquals(errorMsg, "Specify the name of the equipment", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.lifeSuppOtherInput);
		// verify the expected number of characters allowed which is 50
		supplydetailsmovein.lifeSuppOtherInput.sendKeys("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
		String otherText = getDisplayedValue(supplydetailsmovein.lifeSuppOtherInput, true);
		int otherTextCount = otherText.length();
		softAssertion.assertEquals(otherText, "Lorem ipsum dolor sit amet, consectetur adipiscing",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(otherTextCount, 50, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.lifeSuppOtherInput);
		deleteAllTextFromField();
		supplydetailsmovein.lifeSuppOtherInput.sendKeys("\"Other\" Equipment's");
		// I don't know why this specific checkbox get's unticked
		// so will tick it if it unticked
		boolean isOxygenConcentratorTicked = isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0);
		if (!isOxygenConcentratorTicked) {
			clickElementAction(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		}
		// verify the checkboxes are still ticked
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Intermittent Peritoneal Dialysis Machine"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Chronic Positive Airways Pressure Respirator"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Crigler Najjar Syndrome Phototherapy Equipment"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
						false, "Ventilator for Life Support"), 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		// let's click the medical cooling again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.medCoolingYes);
		// upload life support and medical cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf", "typing jim carrey.gif",
				"g'alaxy-'wallpaper.jpeg");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Supply Details ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Supply Details] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Supply Details section
		 */

		/*
		 * README Start of Account Details section
		 */

		startTime = logNanoTimeStamp();
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
					|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				if (readOnlyAcctType) {
					boolean isEditable;
					try {
						// try to click the radio buttons
						clickElementAction(accountdetailsmovein.residential);
						clickElementAction(accountdetailsmovein.commercial);
						isEditable = true;
					} catch (ElementNotInteractableException enie) {
						isEditable = false;
					}
					softAssertion.assertFalse(isEditable, assertionErrorMsg(getLineNumber()));
				}
			}
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify fields are in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// verify we can go to the previous section even though there are required
			// fields
			clickElementAction(accountdetailsmovein.previous);
			pauseSeleniumExecution(1000);
			// verify we are in the Supply Details section
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			scrollPageDown(700);
			// go back to Account Details
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify fields are still in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.commercial);
			assertTrue(isElementTicked(accountdetailsmovein.commercial, 0), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			if (readOnlyAcctType) {
				boolean isEditable;
				try {
					// try to click the radio buttons
					clickElementAction(accountdetailsmovein.residential);
					clickElementAction(accountdetailsmovein.commercial);
					isEditable = true;
				} catch (ElementNotInteractableException enie) {
					isEditable = false;
				}
				softAssertion.assertFalse(isEditable, assertionErrorMsg(getLineNumber()));
			}
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.commercial);
			assertTrue(isElementTicked(accountdetailsmovein.commercial, 0), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify required fields for commercial
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify cancelled abn
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_cancelled_abn2"), Keys.TAB);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(200);
		// click next again
		clickElementAction(accountdetailsmovein.next);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields still in error state
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();
		// verify cancelled acn
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_cancelled_acn2"));
		clickElementAction(accountdetailsmovein.tradingName);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(200);
		// click next again
		clickElementAction(accountdetailsmovein.next);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click next again
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();
		// verify invalid ABN
		accountdetailsmovein.abnOrAcn.sendKeys("96 125 332 211", Keys.TAB);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(200);
		// click next again
		clickElementAction(accountdetailsmovein.next);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click next again
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();
		// verify invalid acn
		accountdetailsmovein.abnOrAcn.sendKeys("095 081 745");
		clickElementAction(accountdetailsmovein.tradingName);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(200);
		// click next again
		clickElementAction(accountdetailsmovein.next);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click next again
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();
		// verify valid ABN
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn2"), Keys.TAB);
		accountdetailsmovein.tradingName.sendKeys("Trading's LLC");
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the displayed ABN with company details
		String abnAcnAndCompany = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		String tradingName = getDisplayedValue(accountdetailsmovein.tradingName, true);
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_abn2"), " (",
				getProp("test_data_valid_company_name_abn1_abn2"), ")"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's LLC", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		clickElementAction(accountdetailsmovein.abnOrAcn);
		String valueAbnAbc = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		// verify that only the abn/acn is displayed
		softAssertion.assertEquals(valueAbnAbc, getProp("test_data_valid_abn2"), assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify hint errors not displayed
		accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.hintAbnAcnCancelled, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
		softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.hintAbnAcnInvalid, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
		softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.hintAbnAcnNotFound, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		accountdetailsmovein.abnOrAcn.sendKeys("1");
		clickElementAction(accountdetailsmovein.next);
		String abnAcnInvalid = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		softAssertion.assertTrue(abnAcnInvalid.contains("66 342 708 6001"), assertionErrorMsg(getLineNumber()));
		pauseSeleniumExecution(500);
		// click next again
		clickElementAction(accountdetailsmovein.next);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.abnOrAcn);
		// delete the number 1 that was added
		accountdetailsmovein.abnOrAcn.sendKeys(Keys.BACK_SPACE);
		clickElementAction(accountdetailsmovein.tradingName);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		abnAcnAndCompany = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		tradingName = getDisplayedValue(accountdetailsmovein.tradingName, true);
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_abn2"), " (",
				getProp("test_data_valid_company_name_abn1_abn2"), ")"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's LLC", assertionErrorMsg(getLineNumber()));
		// verify the section header
		header = normalizeSpaces(
				getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
		softAssertion.assertEquals(header, "2 Account Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.residential);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are now in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Account Details] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Account Details section
		 */

		/*
		 * README Start of Main Account Contact section
		 */

		startTime = logNanoTimeStamp();
		// verify that radio button not ticked by default
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.medicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify Provide None option not displayed in the Personal Identification
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify SMS checkbox is not displayed for Bills
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.billsSMSList),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal checkbox is not displayed for Account Notifications and
		// Reminders
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.acctnotifAndRemindersPostalList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		String billsNotifText = getDisplayedText(mainaccountcontactmovein.lblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(mainaccountcontactmovein.lblAcctnotifAndRemindersNotif, true);
		String marketComNotifText = getDisplayedText(mainaccountcontactmovein.lblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked by default and not
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message for each notification
		hoverToElementAction(mainaccountcontactmovein.billsNotifTooltipIcon);
		String billsTooltipMsg = getDisplayedText(mainaccountcontactmovein.billsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipIcon);
		String acctNotifAndRemTooltipMsg = normalizeSpaces(
				mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipMsg.getText());
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.marketingComNotifTooltipIcon);
		String marketComTooltipMsg = getDisplayedText(mainaccountcontactmovein.marketingComNotifTooltipMsg, true);
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// untick notifications for Bills
		clickElementAction(mainaccountcontactmovein.billsPostal);
		clickElementAction(mainaccountcontactmovein.billsEmail);
		// untick notifications for Account Notifications and Reminders
		clickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		clickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
		// untick notifications for Marketing Communications
		clickElementAction(mainaccountcontactmovein.marketingComEmail);
		// verify all checkboxes unticked
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal Address section is no longer displayed
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "3 Main Account Contact",
						"4 Concession", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous even though required fields not yet supplied
		clickElementAction(mainaccountcontactmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the account details section
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// go back to the main account contact section
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify all assertions
		softAssertion.assertAll();
		// verify it will return an error for email address 'email test@testing.com'
		String invalidEmail = "email test@testing.com";
		String validEmail = "emailtest@testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		clickElementAction(mainaccountcontactmovein.mobilePhone);
		waitForEmailErrorToChange();
		// verify space got trimmed
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false), validEmail,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address '@testing.com'
		invalidEmail = "@testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address ' @testing.com'
		invalidEmail = " @testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email~testing.com'
		invalidEmail = "email~testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email`testing.com'
		invalidEmail = "email`testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email!testing.com'
		invalidEmail = "email!testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email#testing.com'
		invalidEmail = "email#testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing,com'
		invalidEmail = "email@testing,com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing/com'
		invalidEmail = "email@testing/com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing<com'
		invalidEmail = "email@testing<com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing>com'
		invalidEmail = "email@testing>com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing?com'
		invalidEmail = "email@testing?com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing;com'
		invalidEmail = "email@testing;com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		String firstName = "Monkey";
		String lastName = "Luffy's";
		mainaccountcontactmovein.firstName.sendKeys(firstName);
		mainaccountcontactmovein.lastName.sendKeys(lastName);
		clickElementAction(mainaccountcontactmovein.medicareCard);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the validation for Medicare Card
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the invalid medicare number
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
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
		String medExp = concatStrings(prevMonthStr, "/", curYearStr);
		// click to fix ElementNotInteractableException
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(mainaccountcontactmovein.medicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// clear medicare card number and expiry
		clickElementAction(mainaccountcontactmovein.medicareCardNumber);
		deleteAllTextFromField();
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		// click again to collapse the calendar
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		deleteAllTextFromField();
		// put valid medicare card number
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428 77813 2/1");
		int month = 2;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, "/", expYrStr);
		// click to fix ElementNotInteractableException
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(mainaccountcontactmovein.medicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		String medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		verifyStringContains(true, medCareExp, medExp);
		// let's verify the validations for the mobile phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.mobilePhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.mobilePhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		assertEquals(mobPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.mobilePhone);
		deleteAllTextFromField();
		mainaccountcontactmovein.mobilePhone.sendKeys("  012 345  678 9  ");
		mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		// verify users cannot put space
		assertEquals(mobPhone, "0123456789", assertionErrorMsg(getLineNumber()));
		// let's verify the validations for the business phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.businessPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.businessPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		assertEquals(busPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.businessPhone);
		deleteAllTextFromField();
		mainaccountcontactmovein.businessPhone.sendKeys("  987 654  321 0  ");
		busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		// verify users cannot put space
		assertEquals(busPhone, "9876543210", assertionErrorMsg(getLineNumber()));
		// let's verify the validations for the after hours phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.afterhoursPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(afterHoursPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.afterhoursPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		assertEquals(afterHoursPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(mainaccountcontactmovein.afterhoursPhone);
		deleteAllTextFromField();
		mainaccountcontactmovein.afterhoursPhone.sendKeys("  654 321  098 7  ");
		afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		// verify users cannot put space
		assertEquals(afterHoursPhone, "6543210987", assertionErrorMsg(getLineNumber()));
		// click to trigger an event
		clickElementAction(mainaccountcontactmovein.contactSecretCode);
		// verify Mobile Phone is in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that Business Hour Phone is in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that After Hours Phone is in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		mainaccountcontactmovein.mobilePhone.clear();
		mainaccountcontactmovein.businessPhone.clear();
		mainaccountcontactmovein.afterhoursPhone.clear();
		// enter valid phone numbers
		mainaccountcontactmovein.mobilePhone.sendKeys("0212345680");
		mainaccountcontactmovein.businessPhone.sendKeys("0387643210");
		mainaccountcontactmovein.afterhoursPhone.sendKeys("0465320980");
		// used javaScriptClickButton() because for some reason if the
		// checkbox is in error state, checkbox is not clicked using clickButton()
		javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.billsEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComSMS);
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we cannot add an additional contact if a required field is not yet
		// populated
		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein.contactSecretCode.sendKeys("Sekrekt's #001");
		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Main Account Contact").getText());
		String expSectionHeader = concatStrings("3 Main Account Contact (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// did a loop to fix an issue where it's not clicked the first time
		// only happens when use_session_store == false
		int counter = 0;
		int maxRetry = 2;
		boolean isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
		while (!isDisplayed && counter < maxRetry) {
			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
			counter++;
		}
		// verify we are in the in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Main Account Contact] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Main Account Contact section
		 */

		/*
		 * README Start of 1st Additional Contact section
		 */

		startTime = logNanoTimeStamp();
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1DriversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MedicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify the Provide None option is not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ProvideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify SMS checkbox is not displayed for Bills
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1BillsSMSList),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal checkbox is not displayed for Account Notifications and
		// Reminders
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AcctnotifAndRemindersPostalList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		billsNotifText = getDisplayedText(additionalcontactmovein.addCont1LblBillsNotif, true);
		acctNotifAndRemText = normalizeSpaces(additionalcontactmovein.addCont1LblAcctnotifAndRemindersNotif.getText());
		marketComNotifText = normalizeSpaces(additionalcontactmovein.addCont1LblMarketingComNotif.getText());
		softAssertion.assertEquals(billsNotifText, "info Bills(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked by default and not
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message for each notification
		hoverToElementAction(additionalcontactmovein.addCont1BillsNotifTooltipIcon);
		billsTooltipMsg = getDisplayedText(additionalcontactmovein.addCont1BillsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipIcon);
		acctNotifAndRemTooltipMsg = normalizeSpaces(
				additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipMsg.getText());
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1MarketingComNotifTooltipIcon);
		marketComTooltipMsg = normalizeSpaces(additionalcontactmovein.addCont1MarketingComNotifTooltipMsg.getText());
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// untick notifications for Bills
		clickElementAction(additionalcontactmovein.addCont1BillsPostal);
		clickElementAction(additionalcontactmovein.addCont1BillsEmail);
		// untick notifications for Account Notifications and Reminders
		clickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		clickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		// untick notifications for Marketing Communications
		clickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		// verify all checkboxes unticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous even if there are error fields
		clickElementAction(additionalcontactmovein.addCont1Previous);
		pauseSeleniumExecution(1000);
		// verify we are in the main contact details
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the Add Another Contact link is no longer displayed
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(800);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		firstName = "Roronoa";
		lastName = "Zoro";
		additionalcontactmovein.addCont1FirstName.sendKeys(firstName);
		String firstNameAct = getDisplayedValue(additionalcontactmovein.addCont1FirstName, true);
		softAssertion.assertEquals(firstNameAct, firstName, assertionErrorMsg(getLineNumber()));
		additionalcontactmovein.addCont1LastName.sendKeys(lastName);
		String lastNameAct = getDisplayedValue(additionalcontactmovein.addCont1LastName, true);
		softAssertion.assertEquals(lastNameAct, lastName, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous even though required fields not yet supplied
		clickElementAction(additionalcontactmovein.addCont1Previous);
		pauseSeleniumExecution(1000);
		// verify we are in the main account contact section
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"The Main Contact First Name is not displayed");
		scrollPageDown(500);
		// go back to the additional contact section
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify it will return an error for email address 'email test@testing.com'
		invalidEmail = "email test@testing.com";
		validEmail = "emailtest@testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont1MobilePhone);
		waitForEmailErrorToChange();
		// verify space got trimmed
		softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false), validEmail,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address '@testing.com'
		invalidEmail = "@testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address ' @testing.com'
		invalidEmail = " @testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email~testing.com'
		invalidEmail = "email~testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email`testing.com'
		invalidEmail = "email`testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email!testing.com'
		invalidEmail = "email!testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email#testing.com'
		invalidEmail = "email#testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing,com'
		invalidEmail = "email@testing,com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing/com'
		invalidEmail = "email@testing/com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing<com'
		invalidEmail = "email@testing<com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing>com'
		invalidEmail = "email@testing>com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing?com'
		invalidEmail = "email@testing?com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing;com'
		invalidEmail = "email@testing;com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont1MedicareCard);
		// verify the validation for Medicare Card
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the invalid medicare number
		additionalcontactmovein.addCont1MedicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Expiry using the past months of the current year is invalid
		// we get the indexed based months
		prevMonth = getCurrentMonth(true);
		// if it's January, then we set the previous month as December
		if (prevMonth == 0) {
			prevMonth = 12;
		}
		// if it's January, we should set the year as last year
		curYear = getCurrentYear();
		if (prevMonth == 12) {
			curYear = curYear - 1;
		}
		prevMonthStr = Integer.toString(prevMonth);
		curYearStr = Integer.toString(curYear);
		medExp = concatStrings(prevMonthStr, "/", curYearStr);
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(additionalcontactmovein.addCont1MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// clear medicare card number and expiry
		clickElementAction(additionalcontactmovein.addCont1MedicareCardNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		// click again to dismiss the calendar
		clearDateField(additionalcontactmovein.addCont1MedicareCardExpiry);
		// put valid medicare card number
		additionalcontactmovein.addCont1MedicareCardNumber.sendKeys("2428 77813 2");
		month = 2;
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		monthStr = Integer.toString(month);
		expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, "/", expYrStr);
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(additionalcontactmovein.addCont1MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		// had to use javascript to click the checkbox because for some reason
		// the clickButton(WebElement) nor WebElement.click() were working
		// let's tick all available notification groups
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComSMS);
		// verify all checkboxes unticked
		assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0), assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0), assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		softAssertion.assertTrue(medCareExp.contains(medExp), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's verify the validations for the mobile phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1MobilePhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1MobilePhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		assertEquals(mobPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1MobilePhone);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1MobilePhone.sendKeys("  00  9856  2365  ");
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		// verify users cannot put space
		assertEquals(mobPhone, "0098562365", assertionErrorMsg(getLineNumber()));
		// let's verify the validations for the business phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1BusinessPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1BusinessPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		assertEquals(busPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1BusinessPhone);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("  +61  9  0756  3987  ");
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		// verify users cannot put space
		assertEquals(busPhone, "+61907563987", assertionErrorMsg(getLineNumber()));
		// let's verify the validations for the after hours phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1AfterhoursPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		// verify no alpha characters got entered
		assertTrue(StringUtils.isBlank(afterHoursPhone), assertionErrorMsg(getLineNumber()));
		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1AfterhoursPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		assertEquals(afterHoursPhone, "+", assertionErrorMsg(getLineNumber()));
		clickElementAction(additionalcontactmovein.addCont1AfterhoursPhone);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("  1300  555  999  0  ");
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		// verify users cannot put space
		assertEquals(afterHoursPhone, "13005559990", assertionErrorMsg(getLineNumber()));
		// click to trigger an event
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);
		// verify mobile phone is in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that business phone is in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that after hours phone is in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalcontactmovein.addCont1MobilePhone.clear();
		additionalcontactmovein.addCont1BusinessPhone.clear();
		additionalcontactmovein.addCont1AfterhoursPhone.clear();
		// enter valid phone numbers
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+629856312485");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("+639856");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+0009856321789");
		// verify we cannot go to the additional contact if a required field not yet
		// populated
		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				"Additional Contact 01 Contact Secret Code not in error state");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("Sekrekt's #002");
		// verify the section header
		actSectionHeader = normalizeSpaces(getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader,
				"Additional Contact (".concat(firstName)).getText());
		expSectionHeader = concatStrings("4 Additional Contact (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		// verify the action to Add Another contact is displayed
		// and the action to remove additional contact is displayed
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont1RemAdditionalContactList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont1AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
		pauseSeleniumExecution(1000);
		// verify we are in the 2nd additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				"We are not yet in the 2nd Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [1st Additional Contact] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of 1st Additional Contact section
		 */

		/*
		 * README Start of 2nd Additional Contact section
		 */

		startTime = logNanoTimeStamp();
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2DriversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2Passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MedicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsPostal, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify the Provide None option is not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2ProvideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify SMS checkbox is not displayed for Bills
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2BillsSMSList),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal checkbox is not displayed for Account Notifications and
		// Reminders
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2AcctnotifAndRemindersPostalList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		billsNotifText = normalizeSpaces(additionalcontactmovein.addCont2LblBillsNotif.getText());
		acctNotifAndRemText = normalizeSpaces(additionalcontactmovein.addCont2LblAcctnotifAndRemindersNotif.getText());
		marketComNotifText = normalizeSpaces(additionalcontactmovein.addCont2LblMarketingComNotif.getText());
		softAssertion.assertEquals(billsNotifText, "info Bills(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked by default and not
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message for each notification
		hoverToElementAction(additionalcontactmovein.addCont2BillsNotifTooltipIcon);
		billsTooltipMsg = getDisplayedText(additionalcontactmovein.addCont2BillsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipIcon);
		acctNotifAndRemTooltipMsg = normalizeSpaces(
				additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipMsg.getText());
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2MarketingComNotifTooltipIcon);
		marketComTooltipMsg = normalizeSpaces(additionalcontactmovein.addCont2MarketingComNotifTooltipMsg.getText());
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// untick notifications for Bills
		clickElementAction(additionalcontactmovein.addCont2BillsPostal);
		clickElementAction(additionalcontactmovein.addCont2BillsEmail);
		// untick notifications for Account Notifications and Reminders
		clickElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail);
		clickElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS);
		// untick notifications for Marketing Communications
		clickElementAction(additionalcontactmovein.addCont2MarketingComEmail);
		// verify all checkboxes unticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsPostal, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous even though required fields not yet supplied
		clickElementAction(additionalcontactmovein.addCont2Previous);
		pauseSeleniumExecution(1000);
		// verify we are in the 1st additional contact section
		String addContact01FirstName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, true);
		softAssertion.assertEquals(addContact01FirstName, "Roronoa", assertionErrorMsg(getLineNumber()));
		// verify we do not see the Add Another Contact link
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		// verify we see the Remove this contact link
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont1RemAdditionalContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		// go back to the 2nd additional contact section
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		firstName = "Nico";
		lastName = "Robin's";
		additionalcontactmovein.addCont2FirstName.sendKeys(firstName);
		firstNameAct = getDisplayedValue(additionalcontactmovein.addCont2FirstName, true);
		softAssertion.assertEquals(firstNameAct, firstName, assertionErrorMsg(getLineNumber()));
		additionalcontactmovein.addCont2LastName.sendKeys(lastName);
		lastNameAct = getDisplayedValue(additionalcontactmovein.addCont2LastName, true);
		softAssertion.assertEquals(lastNameAct, lastName, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont2DriversLicence);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify that the drivers licence number and state issued are in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify special characters not allowed
		slowSendKeys(additionalcontactmovein.addCont2DriversLicenceNumber, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true,
				200);
		String driversNumber = getDisplayedValue(additionalcontactmovein.addCont2DriversLicenceNumber, true);
		verifyStringIsBlank(driversNumber);
		// verify the valid drivers licence number
		additionalcontactmovein.addCont2DriversLicenceNumber.sendKeys("  ABC  -  123456  ", Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// test the fix in ticket BBPRTL-645
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		String driverNum = getDisplayedValue(additionalcontactmovein.addCont2DriversLicenceNumber, true);
		softAssertion.assertEquals(driverNum, "ABC123456", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify State by typing a non-existent state
		additionalcontactmovein.addCont2DriversLicenceState.sendKeys("Arabasta", Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				"State Issued is not error state");
		// clear the drivers licence number and state
		clickElementAction(additionalcontactmovein.addCont2DriversLicenceNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont2DriversLicenceState);
		deleteAllTextFromField();
		additionalcontactmovein.addCont2DriversLicenceNumber.sendKeys("01235987510");
		additionalcontactmovein.addCont2DriversLicenceState.sendKeys("Australian Capital Territory", Keys.TAB);
		// had to use javascript to click the button because the
		// WebElement.click() nor the clickButton(WebElement) were working
		// tick only the required notification groups
		javaScriptClickElementAction(additionalcontactmovein.addCont2MarketingComPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont2MarketingComEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont2MarketingComSMS);
		// verify the required fields are ticked
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the others remains unticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the email and mobile phone will now be in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalcontactmovein.addCont2EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		additionalcontactmovein.addCont2MobilePhone.sendKeys("+61432587140");
		additionalcontactmovein.addCont2BusinessPhone.sendKeys("+61369854220");
		additionalcontactmovein.addCont2AfterhoursPhone.sendKeys("+61228987540");
		additionalcontactmovein.addCont2ContactSecretCode.sendKeys("Sekrekt's #003");
		// verify the section header
		actSectionHeader = normalizeSpaces(getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader,
				"Additional Contact (".concat(firstName)).getText());
		expSectionHeader = concatStrings("5 Additional Contact (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		// verify the action to Add Another contact is not displayed
		// and the action to remove additional contact is displayed
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont2RemAdditionalContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify we are in the postal address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [2nd Additional Contact] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of 2nd Additional Contact section
		 */

		/*
		 * README Start of Postal Address section
		 */

		startTime = logNanoTimeStamp();
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-646
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the postal address search is not displayed
		postaladdressmovein = new PostalAddressMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(postaladdressmovein.postalAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous
		clickElementAction(postaladdressmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the 2nd additional contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify fields are still in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the postal address search is not displayed
		postaladdressmovein = new PostalAddressMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(postaladdressmovein.postalAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.sameSupAddressNo);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.state, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous
		clickElementAction(postaladdressmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the 2nd additional contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.state, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// populate the address
		postaladdressmovein.addLine01.sendKeys("Add-#01");
		postaladdressmovein.addLine02.sendKeys("Add-#02");
		postaladdressmovein.addLine03.sendKeys("Add-#03");
		postaladdressmovein.addLine04.sendKeys("Add-#04");
		postaladdressmovein.city.sendKeys("City/Suburb");
		postaladdressmovein.state.sendKeys("State");
		postaladdressmovein.postcode.sendKeys("Postcode");
		postaladdressmovein.country.sendKeys("Dressrosa", Keys.TAB);
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify only country in error state
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.country);
		deleteAllTextFromField();
		postaladdressmovein.country.sendKeys("australia", Keys.TAB);
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify small caps country returns an error
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's get all of the countries
		clickElementAction(postaladdressmovein.country);
		deleteAllTextFromField();
		postaladdressmovein.country.sendKeys("united");
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		List<String> countries = null;
		try {
			countries = getAllMatOptionsValues(postaladdressmovein.countriesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			postaladdressmovein = new PostalAddressMoveIn(driver);
			countries = getAllMatOptionsValues(postaladdressmovein.countriesDiv);
		}
		verifyStringContainsInEachListPacket(countries, "united", false);
		verifyNumOfMatOptionValuesDisp(postaladdressmovein.countriesDiv, 4);
		// choose fourth from the list
		chooseFromList(postaladdressmovein.countriesDiv, 4);
		// verify no error on the fields
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.addLine04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.city, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.postcode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.addLine01);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.addLine02);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.addLine03);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.addLine04);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.city);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.state);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.postcode);
		deleteAllTextFromField();
		clickElementAction(postaladdressmovein.country);
		deleteAllTextFromField();
		postaladdressmovein.addLine01.sendKeys("Community");
		postaladdressmovein.addLine02.sendKeys("40 Mascar Street");
		postaladdressmovein.city.sendKeys("Upper Mount Gravatt");
		postaladdressmovein.state.sendKeys("Queensland");
		postaladdressmovein.postcode.sendKeys("4122");
		postaladdressmovein.country.sendKeys("Australia", Keys.TAB);
		// verify the fields are populated correctly
		String add01 = getDisplayedValue(postaladdressmovein.addLine01, true);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, true);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, true);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, true);
		city = getDisplayedValue(postaladdressmovein.city, true);
		state = getDisplayedValue(postaladdressmovein.state, true);
		postcode = getDisplayedValue(postaladdressmovein.postcode, true);
		String country = getDisplayedValue(postaladdressmovein.country, true);
		softAssertion.assertEquals(add01, "Community", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "40 Mascar Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Upper Mount Gravatt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4122", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// updated address 3 and 4
		postaladdressmovein.addLine03.sendKeys("Add-#03");
		postaladdressmovein.addLine04.sendKeys("Add-#04");
		// verify the header
		header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Postal Address").getText());
		softAssertion.assertEquals(header, "6 Postal Address", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Concession section
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not in the Concession section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Postal Address] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Postal Address section
		 */

		/*
		 * README Start of Concession section
		 */

		startTime = logNanoTimeStamp();
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(concessionmovein.addConcessionYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(concessionmovein.addConcessionNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous button
		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Postal Address Section
		country = getDisplayedValue(postaladdressmovein.country, true);
		verifyTwoStringsAreEqual(country, "Australia", true);
		// go back to the Concession details
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		clickElementAction(concessionmovein.addConcessionYes);
		// verify the fix for ticket BBPRTL-647
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify that the Concession Card Number and Concession Card Number Expiry is
		// initially not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify required fields
		softAssertion.assertTrue(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify that the Concession Card Number and Concession Card Number Expiry is
		// initially not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous even though there's required fields
		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Postal Address Section
		country = getDisplayedValue(postaladdressmovein.country, true);
		verifyTwoStringsAreEqual(country, "Australia", true);
		// go back to the Concession details
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify still in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify that the Concession Card Number and Concession Card Number Expiry is
		// initially not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		concessionmovein.cardHolderName.sendKeys("Steven Roger's");
		// verify the list of types available
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		// let's confirm first that the records retrieved are correct
		List<String> actualConcessionTypes = null;
		try {
			actualConcessionTypes = getAllMatOptionsValues(concessionmovein.typeOfConcessionCardDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			concessionmovein = new ConcessionMoveIn(driver);
			actualConcessionTypes = getAllMatOptionsValues(concessionmovein.typeOfConcessionCardDiv);
		}
		List<String> expectedConcessionTypes = new ArrayList<>(Arrays.asList("Queensland Seniors Card", "DVA Gold Card",
				"Pensioner Card Centrelink", "Pensioner Card Veteran Affairs", "Centrelink Health Care Card",
				"Home Parks and Multi Units", "Repatriation Heath Card (RHC)"));
		// not enabled in the concession_card_definition.json
		softAssertion.assertFalse(actualConcessionTypes.toString().contains("Asylum Seeker"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actualConcessionTypes, expectedConcessionTypes, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		verifyTwoStringsAreEqual(typeChosen, "Queensland Seniors Card", true);
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields for Card Number and Expiry
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the validation in the concession card number
		// and expiry date
		concessionmovein.cardNumber.sendKeys("+61426037890");
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(prevMonthStr, "/", curYearStr, Keys.TAB);
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// clear the values for the concession card number and expiry
		clickElementAction(concessionmovein.cardNumber);
		deleteAllTextFromField();
		clearDateField(concessionmovein.cardNumExpiry);
		// put a valid concession card number and expiry
		concessionmovein.cardNumber.sendKeys("378282246310005");
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(monthStr, "/", expYrStr, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForUpload);
		pauseSeleniumExecution(1000);
		String authorisationTextExp = "'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession. Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our Terms and Conditions";
		softAssertion.assertEquals(getDisplayedText(concessionmovein.lblAuthorisationForUpload, true),
				authorisationTextExp, assertionErrorMsg(getLineNumber()));
		// let's upload concession card details
		uploadConcessionFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf", "g'alaxy-'wallpaper.jpeg");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Concession ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		// verify only 2 files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		header = normalizeSpaces(getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Concession").getText());
		softAssertion.assertEquals(header, "7 Concession", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(concessionmovein.next)) {
				scrollIntoView(concessionmovein.next);
			}
		}
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Direct Debit Details section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.lblSetupDirectDebit, 0),
				"We are not in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Concession] execution time in minutes <", String.valueOf(getTotalExecutionInMin(startTime, endTime)),
				">"));

		/*
		 * End of Concession section
		 */

		/*
		 * README Start of Direct Debit section
		 */

		startTime = logNanoTimeStamp();
		// verify that values are not ticked by default
		softAssertion.assertFalse(isElementTicked(directdebitmovein.bankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.creditCard, 0), assertionErrorMsg(getLineNumber()));
		// verify the 3rd option is not displayed
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.noDirectDebitList),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous button even though there are required fields not
		// populated
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		String concessionCardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		verifyTwoStringsAreEqual(concessionCardHolder, "Steven Roger's", true);
		scrollPageDown(400);
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.bankAccount);
		// put a pause to make sure all fields and images are loaded
		pauseSeleniumExecution(2000);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the fix for ticket BBPRTL-996
		clickElementAction(directdebitmovein.creditCard);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		clickElementAction(directdebitmovein.bankAccount);
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can hit previous button even though there are required fields not
		// populated
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(concessionmovein.next)) {
				scrollIntoView(concessionmovein.next);
			}
		}
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the Bank Account declaration text
		String declaration = getDisplayedText(directdebitmovein.lblBankAccountDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Dishonor Fees: A dishonor fee of $15.00 (inc GST) applied for failed direct debt. the bank payment will be based on the bills issued according to each bill cycle. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account authorisation text
		String authorisation = getDisplayedText(directdebitmovein.lblBankAccountAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise SR Global Solutions Pty Ltd ACN 132 951 172 (\"Merchant Warrior\"), Direct Debit User ID Number 397351, to debit my/our account on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the Bulk Electronic Clearing System (BECS) as per the service agreement provided. Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the secured by sectigo image
		hoverToElementAction(directdebitmovein.imgSectigo);
		pauseSeleniumExecution(1000);
		// verify the iframe is displayed
		try {
			setImplicitWait(1);
			driver.switchTo().frame(directdebitmovein.sectigoIframe);
		} catch (Exception e) {
			logDebugMessage("An exception has been encountered. Please see message for more details -> "
					.concat(e.getMessage()));
			throw e;
		} finally {
			if (getPortalType().equals("standalone")) {
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		clickElementAction(directdebitmovein.creditCard);
		directdebitmovein = new DirectDebitMoveIn(driver, 1);
		try {
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
			String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
			assertEquals(loadingMsg, "Creating secure area for credit card entry...",
					"Credit Card initialization progress bar text is not correct");
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(
					"StaleElementReferenceException encountered while trying to check for the progress bar text");
			directdebitmovein = new DirectDebitMoveIn(driver);
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
			String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
			assertEquals(loadingMsg, "Creating secure area for credit card entry...",
					"Credit Card initialization progress bar text is not correct");
		} catch (AssertionError ae) {
			logDebugMessage(
					"AssertionError encountered, progress bar text was not displayed, selenium might have missed it");
		} catch (NoSuchElementException nsee) {
			logDebugMessage(
					"NoSuchElementException encountered, progress bar text was not displayed, selenium might have missed it");
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// not yet displayed, wait until loading disappears
		moveInDirectDebitCCProgBarLoad();
		// put a pause to make sure all fields and images are loaded
		pauseSeleniumExecution(2000);
		// verify fields not in error state
		switchToMWIframe();
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		switchToMWIframe();
		// verify the required fields
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the Credit Card payment declaration text
		declaration = getDisplayedText(directdebitmovein.lblCreditCardDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Sucharge Fees: A credit/debit card surcharge of 1.5% (inc. GST) applies for Visa and Mastercard, 3% surcharge (inc GST) applies for AMEX and Diners Card. Please check our Terms and Conditions the debit payment will be based on the bills issued according to each bill cycle",
				assertionErrorMsg(getLineNumber()));
		// verify the Credit Card authorization text
		authorisation = getDisplayedText(directdebitmovein.lblCreditCardAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify the pci image
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.imgPciSeal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the secured by sectigo image
		hoverToElementAction(directdebitmovein.imgSectigo);
		pauseSeleniumExecution(1000);
		// verify the iframe is displayed
		try {
			setImplicitWait(1);
			driver.switchTo().frame(directdebitmovein.sectigoIframe);
		} catch (Exception e) {
			logDebugMessage(
					"An exception has been encountered while checking the sectigo image in credit card. Please see message for more details -> "
							.concat(e.getMessage()));
			throw e;
		} finally {
			if (getPortalType().equals("standalone")) {
				switchToDefaultContent();
			} else if (getPortalType().equals("embedded")) {
				// let's go back to the parent iframe
				switchToParentFrame();
			}
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		// verify we can hit previous button even though there are required fields not
		// populated
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		concessionCardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		verifyTwoStringsAreEqual(concessionCardHolder, "Steven Roger's", true);
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");
		switchToMWIframe();
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		switchToMWIframe();
		// put invalid value for Credit Card Name
		directdebitmovein.creditCardName.sendKeys(getProp("test_data_01"));
		// put an invalid value for Credit Card Number
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_02"), true, 250);
		// put an prev month date
		// we get the indexed based months
		prevMonth = getCurrentMonth(true);
		boolean isDoubleDigit = (prevMonth > 9 && prevMonth < 100) || (prevMonth < -9 && prevMonth > -100);
		curYear = getCurrentYear();
		prevMonthStr = Integer.toString(prevMonth);
		curYearStr = Integer.toString(curYear);
		// let's only get the last 2 digits of the year
		curYearStr = getString(curYearStr, 2, 4);
		String expiryInput;
		if (isDoubleDigit) {
			expiryInput = concatStrings(prevMonthStr, "/", curYearStr);
			logDebugMessage(concatStrings("The value of expiryInput '", expiryInput, "'"));
			// put invalid value for Credit Card Expiry
			directdebitmovein.creditCardExpiry.sendKeys(expiryInput, Keys.TAB);
		} else {
			expiryInput = concatStrings("0", prevMonthStr, "/", curYearStr);
			logDebugMessage(concatStrings("The value of expiryInput '", expiryInput, "'"));
			// put invalid value for Credit Card Expiry
			directdebitmovein.creditCardExpiry.sendKeys(expiryInput, Keys.TAB);
		}
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		switchToMWIframe();
		// verify the required fields
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// enter valid fields for credit card name, credit card number and expiry
		switchToMWIframe();
		clickElementAction(directdebitmovein.creditCardName);
		deleteAllTextFromField();
		directdebitmovein.creditCardName.sendKeys("Nick Fury's");
		clickElementAction(directdebitmovein.creditCardNumber);
		deleteAllTextFromField();
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_03"), true, 300);
		clickElementAction(directdebitmovein.creditCardExpiry);
		deleteAllTextFromField();
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		month = 2;
		monthStr = Integer.toString(month);
		String expYrStrFull = Integer.toString(expYr);
		expYrStr = getString(expYrStrFull, 2, 4);
		monthStr = concatStrings("0", monthStr);
		String expiry = concatStrings(monthStr, "/", expYrStr);
		directdebitmovein.creditCardExpiry.sendKeys(expiry, Keys.TAB);
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		moveInDirectDebitCCProgBarLoad();
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.authorisationCreditCard);
		clickElementAction(directdebitmovein.bankAccount);
		pauseSeleniumExecution(500);
		clickElementAction(directdebitmovein.creditCard);
		// verify the details are are not cleared
		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Nick Fury's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_04"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// hit the Change Credit Card Details btn
		clickElementAction(directdebitmovein.changeCreditCardDetails);
		moveInDirectDebitCCProgBarLoad();
		// verify the fields are blank
		switchToMWIframe();
		String ccName = getDisplayedValue(directdebitmovein.creditCardName, true);
		String ccNum = getDisplayedValue(directdebitmovein.creditCardNumber, true);
		String ccExp = getDisplayedValue(directdebitmovein.creditCardExpiry, true);
		softAssertion.assertTrue(StringUtils.isBlank(ccName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(ccNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(ccExp), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify all assertions
		softAssertion.assertAll();
		// let's cancel the change
		clickElementAction(directdebitmovein.cancelCreditCardChange);
		pauseSeleniumExecution(1000);
		actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Nick Fury's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_04"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the header
		header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Direct Debit").getText());
		softAssertion.assertEquals(header, "8 Direct Debit", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional notes section
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Notes section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Direct Debit] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Direct Debit section
		 */

		/*
		 * README Start of Additional Note section
		 */

		startTime = logNanoTimeStamp();
		// verify that the field is displayed
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"Additional Notes text area is not displayed");
		// verify it's not required so we can hit next
		// and go to the next section
		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the acceptance page section
		String reviewText = getDisplayedText(acceptancemovein.lblAcceptanceIntro, true);
		verifyTwoStringsAreEqual(reviewText,
				"You are almost finished, thank you for your patience. Please review the below details before submitting the form",
				true);
		scrollPageDown(1900);
		// hit previous from Acceptance Page
		clickElementAction(acceptancemovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Additional notes section
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0), "Notes section is not displayed");
		// hit previous from Additional Notes section
		clickElementAction(additionalnotemovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Direct Debit section
		assertTrue(isElementTicked(directdebitmovein.creditCard, 0), "Credit Card option is no longer ticked");
		// go back to the additional notes section
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		additionalnotemovein.notesArea.sendKeys("The quick brown fox jumps over the lazy dog.");
		// verify the header
		header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Additional Note").getText());
		softAssertion.assertEquals(header, "9 Additional Note", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the acceptance page now
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Additional Note] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Additional Note section
		 */

		logDebugMessage("Finished populating the Supply Details until Additional Note section");
		long totalEndtime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsResidential.", testCaseName,
				" [Total] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(totalStartime, totalEndtime)), ">"));
	}

	/**
	 * Use {@link #populateAllSectionsCommercial(boolean,boolean,String,String)}
	 * instead
	 * 
	 * @param testCaseName     the name of the test case that use this method
	 * @param readOnlySuppAdd  pass true if it's read only on the portal config
	 *                         being used
	 * @param readOnlyAcctType pass true if it's read only on the portal config
	 *                         being used
	 * 
	 */
	private void populateAllSectionsCommercial(String testCaseName, boolean readOnlySuppAdd, boolean readOnlyAcctType,
			int numOfAttachLifeSup, int numOfAttachTradeWaste, String mainFirstName, String mainLastName) {

		long startTime = logNanoTimeStamp();
		long totalStartime = startTime;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				// let's make sure the session storage is cleared
				// since it's supposed to be cleared in the previous test case
				clearLocalAndSessionStorage();
				refreshBrowser(1, 5000);
			}
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				// let's make sure the session storage is cleared
				// since it's supposed to be cleared in the previous test case
				clearLocalAndSessionStorage();
				refreshBrowser(1, 5000);
			}
			loadEmbeddedMoveInPortal(true, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		long endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime, endTime)), ">"));
		logDebugMessage("Will start populating the Supply Details until Additional Notes section");

		/*
		 * README Start of Supply Details section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyConnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyDisconnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.supplyUnknown, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.owner);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clearDateField(supplydetailsmovein.moveInDateOwner);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		String future12Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.commercialMoveInDate = future12Days;
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		supplydetailsmovein.moveInDateOwner.sendKeys(future12Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.whoIsResponsiblePropMan);
		clickElementAction(supplydetailsmovein.tenant);
		// verify that the Who is responsible fields got hidden
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblWhoIsResponsibleList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.owner);
		// verify move in date was not cleared
		softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, true), future12Days,
				assertionErrorMsg(getLineNumber()));
		// verify nothing is selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify nothing is ticked
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the Who is responsible fields still hidden
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblWhoIsResponsibleList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
		boolean validationTriggered = false;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Bella Vista");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Heathcote");
			supplydetailsmovein.supplyAddState.sendKeys("New South Wales", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("2233");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			if (isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0)) {
				supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
				supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
				supplydetailsmovein.supplyAddStreetNum.sendKeys("20");
				supplydetailsmovein.supplyAddStreetName.sendKeys("Bella Vista");
				supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
				supplydetailsmovein.supplyAddCity.sendKeys("Heathcote");
				supplydetailsmovein.supplyAddState.sendKeys("New South Wales", Keys.TAB);
				supplydetailsmovein.supplyAddPostcode.sendKeys("2233");
			} else {
				scrollPageDown(300);
				// let's validate the fields so supply details can be updated
				clickElementAction(accountdetailsmovein.header);
				pauseSeleniumExecution(1000);
				assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
						"Supply Details is still in read only mode");
				validationTriggered = true;
				supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
				supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
				supplydetailsmovein.supplyAddStreetNum.sendKeys("20");
				supplydetailsmovein.supplyAddStreetName.sendKeys("Bella Vista");
				supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
				supplydetailsmovein.supplyAddCity.sendKeys("Heathcote");
				supplydetailsmovein.supplyAddState.sendKeys("New South Wales", Keys.TAB);
				supplydetailsmovein.supplyAddPostcode.sendKeys("2233");
			}
		}
		// verify populated correctly
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Tenancy", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "20", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "20", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Bella Vista", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Heathcote", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "New South Wales", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "2233", assertionErrorMsg(getLineNumber()));
		clickElementAction(supplydetailsmovein.supplyConnected);
		clickElementAction(supplydetailsmovein.lifeSupYes);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields are still editable
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			if (readOnlySuppAdd) {
				// verify fields are not editable
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
						assertionErrorMsg(getLineNumber()));
			} else {
				// verify fields are editable
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
						assertionErrorMsg(getLineNumber()));
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify fields not in error state
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
				"Please select one or more life support devices in-use:", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Intermittent Peritoneal Dialysis Machine"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 3,
				0), assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Chronic Positive Airways Pressure Respirator"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		if (validationTriggered) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(supplydetailsmovein.medCoolingYes);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields are still editable
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			if (readOnlySuppAdd) {
				// verify fields are not editable
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
						assertionErrorMsg(getLineNumber()));
			} else {
				// verify fields are editable
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetNum, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetName, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddStreetType, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddCity, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddState, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddPostcode, 0),
						assertionErrorMsg(getLineNumber()));
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify fields in error state
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
				"Please select one or more life support devices in-use:", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Intermittent Peritoneal Dialysis Machine"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 3,
				0), assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Chronic Positive Airways Pressure Respirator"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
								"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify nothing is uploaded
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
				assertionErrorMsg(getLineNumber()));
		scrollPageDown(300);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Intermittent Peritoneal Dialysis Machine"));
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Chronic Positive Airways Pressure Respirator"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Crigler Najjar Syndrome Phototherapy Equipment"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Ventilator for Life Support"));
		// let's click the medical cooling again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.medCoolingYes);
		// upload life support and medical cooling files
		if (numOfAttachLifeSup == 2) {
			uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg", "Sprin't 02 Story 'Board.pdf");
		} else {
			throw (new IllegalArgumentException("The args passed for expected numOfAttachLifeSup is incorrect"));
		}
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Supply Details ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		if (numOfAttachLifeSup == 2) {
			// verify all files were uploaded
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		} else {
			throw (new IllegalArgumentException("The args passed for expected numOfAttachLifeSup is incorrect"));
		}
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(300);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Supply Details] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Supply Details section
		 * 
		 */

		/*
		 * README Start of Account Details section
		 * 
		 */

		startTime = logNanoTimeStamp();
		boolean errorExpected = false;
		if (!isElementEnabled(accountdetailsmovein.residential, 0)
				&& !isElementEnabled(accountdetailsmovein.commercial, 0)) {
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			errorExpected = true;
		}
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.commercial);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			if (readOnlyAcctType) {
				softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
						assertionErrorMsg(getLineNumber()));
			}
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			if (errorExpected) {
				// verify fields in error state
				softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
						assertionErrorMsg(getLineNumber()));
			} else {
				// verify fields not in error state
				softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
						assertionErrorMsg(getLineNumber()));
			}
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.commercial);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_acn4"), Keys.TAB);
		accountdetailsmovein.tradingName.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				"We are not yet in the Trade Waste section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Account Details] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Account Details section
		 * 
		 */

		/*
		 * README Start of Trade Waste section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the field is not in error state
		softAssertion.assertFalse(isElementInError(tradewastemovein.tradeWasteDischargeYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.tradeWasteDischargeNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the other fields are still hidden
		tradewastemovein = new TradeWasteMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipNoList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.businessActivityList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxFlowRateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxDischargeVolumeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeStartDateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeDaysList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeDaysCheckboxGroupList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursStartList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursEndList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblTradeWasteAttachmentIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteDischargeYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteDischargeNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the other fields are still hidden
		tradewastemovein = new TradeWasteMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipNoList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.businessActivityList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxFlowRateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxDischargeVolumeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeStartDateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeDaysList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeDaysCheckboxGroupList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursStartList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursEndList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblTradeWasteAttachmentIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// verify we can go back to the previous section
		clickElementAction(tradewastemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.commercial, 0), "We are not in the Account Details section");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fields still in error state
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteDischargeYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteDischargeNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the other fields are still hidden
		tradewastemovein = new TradeWasteMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.tradeWasteEquipNoList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.businessActivityList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeInfoIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxFlowRateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.maxDischargeVolumeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeStartDateList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeDaysList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeDaysCheckboxGroupList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursStartList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursStartMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblDischargeHoursEndList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndHourList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dischargeHoursEndMinList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.lblTradeWasteAttachmentIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(tradewastemovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.tradeWasteDischargeYes);
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(tradewastemovein.tradeWasteEquipYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.tradeWasteEquipNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.maxFlowRate, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.maxDischargeVolume, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.dischargeStartDate, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteEquipYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteEquipNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.maxFlowRate, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.maxDischargeVolume, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.dischargeStartDate, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can go back to the previous section
		clickElementAction(tradewastemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.commercial, 0), "We are not in the Account Details section");
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		clickElementAction(tradewastemovein.tradeWasteEquipYes);
		// verify the fields are not in error state
		softAssertion.assertFalse(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"),
						3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(800);
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);
		// verify the fields in error state
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"),
						3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify we can go back to the previous section
		clickElementAction(tradewastemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.commercial, 0), "We are not in the Account Details section");
		scrollPageDown(700);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify the fields in error state
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"),
						3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isElementInError(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"));
		clickElementAction(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"));
		clickElementAction(tradewastemovein.businessActivity);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(tradewastemovein.businessActivityDiv, 3);
		chooseFromList(tradewastemovein.businessActivityDiv, 3);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(tradewastemovein.businessActivity, true);
		softAssertion.assertEquals(typeChosen, "Other", assertionErrorMsg(getLineNumber()));
		tradewastemovein.tradeWasteOtherTextField.sendKeys("\"Other\" Equipment's'z");
		// verify the checkboxes are still ticked
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"), 0),
				assertionErrorMsg(getLineNumber()));
		// Other tickbox automatically ticked
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		tradewastemovein.maxFlowRate.sendKeys("10,000.279");
		String maxFlow = getDisplayedValue(tradewastemovein.maxFlowRate, false);
		verifyTwoStringsAreEqual(maxFlow, "10000.279", false);
		tradewastemovein.maxDischargeVolume.sendKeys("11,000.389");
		String maxDischarge = getDisplayedValue(tradewastemovein.maxDischargeVolume, false);
		verifyTwoStringsAreEqual(maxDischarge, "11000.389", false);
		String dischargeStartDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -366,
				DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(tradewastemovein.dischargeStartDate);
		pauseSeleniumExecution(1000);
		tradewastemovein.dischargeStartDate.sendKeys(dischargeStartDate, Keys.TAB);
		// click field to dismiss the calendar
		clickElementAction(tradewastemovein.maxDischargeVolume);
		// verify field not in error state
		softAssertion.assertFalse(isElementInError(tradewastemovein.dischargeStartDate, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		// tick all discharge days
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"));
		tradewastemovein.dischargeHoursStartHour.sendKeys("01", Keys.TAB);
		tradewastemovein.dischargeHoursEndHour.sendKeys("12", Keys.TAB);
		if (numOfAttachTradeWaste == 2) {
			// upload trade waste files
			uploadTradeWasteFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf",
					"planet_in_deep_space-wallpaper-1920x1080.jpg");
		} else if (numOfAttachTradeWaste == 1) {
			// upload trade waste files
			uploadTradeWasteFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf");
		} else {
			throw (new IllegalArgumentException("The args passed for expected numOfAttachTradeWaste is incorrect"));
		}
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Trade Waste ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		if (numOfAttachTradeWaste == 2) {
			// verify the files that were uploaded
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully planet_in_deep_space-wallpaper-1920x1080 .jpg 0.8 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		} else if (numOfAttachTradeWaste == 1) {
			// verify the files that were uploaded
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		} else {
			throw (new IllegalArgumentException("The args passed for expected numOfAttachTradeWaste is incorrect"));
		}
		// verify the section header
		String header = getDisplayedText(getElementFrmMatStepHdrTag(tradewastemovein.matStepHeader, "Trade Waste"),
				true);
		softAssertion.assertEquals(header, "3 Trade Waste", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				"We are not yet in the Main Account Contact section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Trade Waste] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Trade Waste section
		 * 
		 */

		/*
		 * README Start of Main Account Contact section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify Date of Birth not displayed
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.dateOfBirth, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify personal identification not displayed
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.lblPersonalIDHeader, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.driversLicenceList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.passportList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.medicareCardList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact link is displayed
		softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationHeader, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the notification introduction is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify SMS checkbox is not displayed for Bills
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.billsSMSList),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal checkbox is not displayed for Account Notifications and
		// Reminders
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.acctnotifAndRemindersPostalList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		String billsNotifText = getDisplayedText(mainaccountcontactmovein.lblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(mainaccountcontactmovein.lblAcctnotifAndRemindersNotif, true);
		String marketComNotifText = getDisplayedText(mainaccountcontactmovein.lblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked by default and not
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message for each notification
		hoverToElementAction(mainaccountcontactmovein.billsNotifTooltipIcon);
		String billsTooltipMsg = getDisplayedText(mainaccountcontactmovein.billsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipIcon);
		String acctNotifAndRemTooltipMsg = normalizeSpaces(
				mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipMsg.getText());
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.marketingComNotifTooltipIcon);
		String marketComTooltipMsg = getDisplayedText(mainaccountcontactmovein.marketingComNotifTooltipMsg, true);
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// untick notifications for Bills
		javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.billsEmail);
		// untick notifications for Account Notifications and Reminders
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
		// untick notifications for Marketing Communications
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		// verify all checkboxes unticked
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Postal Address section is still displayed
		// since Postal is still ticked in the Additional Contact
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
						"4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Email Address becomes required if a notification is ticked
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		// verify Mobile Phone Number becomes required if a notification is ticked
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComSMS);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		mainaccountcontactmovein.firstName.sendKeys(mainFirstName);
		mainaccountcontactmovein.lastName.sendKeys(mainLastName);
		javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		mainaccountcontactmovein.mobilePhone.sendKeys("0332878850");
		mainaccountcontactmovein.businessPhone.sendKeys("+61400853690");
		mainaccountcontactmovein.afterhoursPhone.sendKeys("130285");
		mainaccountcontactmovein.contactSecretCode.sendKeys("Testing 123...");
		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// did a loop to fix an issue where it's not clicked the first time
		// only happens when use_session_store == false
		int counter = 0;
		int maxRetry = 2;
		boolean isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
		while (!isDisplayed && counter < maxRetry) {
			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
			counter++;
		}
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Main Account Contact] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Main Account Contact section
		 * 
		 */

		/*
		 * README Start of Additional Contact section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// verify fields are not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify Date of Birth not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1DateOfBirthList),
				assertionErrorMsg(getLineNumber()));
		// verify personal identification not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1LblPersonalIdentificationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1DriversLicenceList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1PassportList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1MedicareCardList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ProvideNoneList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationHeader, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the notification introduction is not displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the remove additional contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify SMS checkbox is not displayed for Bills
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1BillsSMSList),
				assertionErrorMsg(getLineNumber()));
		// verify the Postal checkbox is not displayed for Account Notifications and
		// Reminders
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AcctnotifAndRemindersPostalList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		billsNotifText = getDisplayedText(additionalcontactmovein.addCont1LblBillsNotif, true);
		acctNotifAndRemText = normalizeSpaces(additionalcontactmovein.addCont1LblAcctnotifAndRemindersNotif.getText());
		marketComNotifText = normalizeSpaces(additionalcontactmovein.addCont1LblMarketingComNotif.getText());
		softAssertion.assertEquals(billsNotifText, "info Bills(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked by default and not
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message for each notification
		hoverToElementAction(additionalcontactmovein.addCont1BillsNotifTooltipIcon);
		billsTooltipMsg = getDisplayedText(additionalcontactmovein.addCont1BillsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipIcon);
		acctNotifAndRemTooltipMsg = normalizeSpaces(
				additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipMsg.getText());
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1MarketingComNotifTooltipIcon);
		marketComTooltipMsg = normalizeSpaces(additionalcontactmovein.addCont1MarketingComNotifTooltipMsg.getText());
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Postal Address section is displayed
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
				"create Trade Waste",
				concatStrings("create Main Account Contact (", mainFirstName, " ", mainLastName, ")"),
				"5 Additional Contact", "6 Postal Address", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// untick notifications for Bills
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsEmail);
		// untick notifications for Account Notifications and Reminders
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		// untick notifications for Marketing Communications
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		// verify all checkboxes unticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify Postal Address section no longer displayed
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
				"create Trade Waste",
				concatStrings("create Main Account Contact (", mainFirstName, " ", mainLastName, ")"),
				"5 Additional Contact", "6 Postal Address", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MarketingComSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Email Address becomes required if notification is ticked
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		// verify Mobile Phone becomes required if notification is ticked
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComSMS);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalcontactmovein.addCont1FirstName.sendKeys("Roshan");
		additionalcontactmovein.addCont1LastName.sendKeys("Britto");
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0332878850");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("+61400853690");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+960985740362");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("Inulitz.....");
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		clickElementAction(mainaccountcontactmovein.marketingComPostal);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(directdebitmovein.header)) {
				scrollIntoView(directdebitmovein.header);
			}
		}
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are redirected automatically in the Postal Address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Additional Contact] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Additional Contact section
		 * 
		 */

		/*
		 * README Start of Postal Address section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.sameSupAddressYes);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Postal Address] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Postal Address section
		 * 
		 */

		/*
		 * README Start of Direct Debit section
		 * 
		 */

		startTime = logNanoTimeStamp();
		// verify that values are not ticked by default
		softAssertion.assertFalse(isElementTicked(directdebitmovein.bankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.creditCard, 0), assertionErrorMsg(getLineNumber()));
		// verify fields are in not error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.bankAccount);
		String name = concatStrings(mainFirstName, " ", mainLastName);
		directdebitmovein.bankAccountName.sendKeys(name);
		directdebitmovein.accountBSB.sendKeys("000100");
		directdebitmovein.accountNumber.sendKeys("001000");
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify the fields in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.authorisationBankAccount);
		clickElementAction(directdebitmovein.creditCard);
		directdebitmovein = new DirectDebitMoveIn(driver, 1);
		try {
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(
					"StaleElementReferenceException encountered while trying to check for the progress bar text");
			directdebitmovein = new DirectDebitMoveIn(driver);
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
		} catch (AssertionError ae) {
			logDebugMessage(
					"AssertionError encountered, progress bar text was not displayed, selenium might have missed it");
		} catch (NoSuchElementException nsee) {
			logDebugMessage(
					"NoSuchElementException encountered, progress bar text was not displayed, selenium might have missed it");
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		moveInDirectDebitCCProgBarLoad();
		// verify not ticked
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.bankAccount);
		// verify values are still there
		// special character was trimmed
		// so we trim our expected name also
		String fName = mainFirstName.replaceAll("[^a-zA-Z0-9]", "");
		String lName = mainLastName.replaceAll("[^a-zA-Z0-9]", "");
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.bankAccountName, false),
				concatStrings(fName, " ", lName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountBSB, false), "000100",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountNumber, false), "001000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Direct Debit] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Direct Debit section
		 * 
		 */

		/*
		 * README Start of Additional Note section
		 * 
		 */

		startTime = logNanoTimeStamp();
		additionalnotemovein.notesArea.sendKeys("The quick brown foxs jumps over the lazy dog.");
		softAssertion.assertEquals(getDisplayedValue(additionalnotemovein.notesArea, false).length(), 45,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.notesLengthCounter, true), "45/256",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");
		// verify all assertions
		softAssertion.assertAll();
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Additional Note] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Additional Note section
		 * 
		 */

		logDebugMessage("Finished populating the Supply Details until Additional Note section");
		long totalEndtime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations01.populateAllSectionsCommercial.", testCaseName,
				" [Total] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(totalStartime, totalEndtime)), ">"));
	}

	@BeforeClass
	public void beforeClass() {

		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);
	}

	@AfterClass
	public void afterClass() {

		deleteMoveInGlobalLangFiles(s3Access);

		saveProp();
		logTestClassEnd(className);
	}

	@BeforeMethod
	public void beforeMethod() {

		// let's initialize the page objects
		supplydetailsmovein = new SupplyDetailsMoveIn(driver);
		accountdetailsmovein = new AccountDetailsMoveIn(driver);
		tradewastemovein = new TradeWasteMoveIn(driver);
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver);
		additionalcontactmovein = new AdditionalContactMoveIn(driver);
		postaladdressmovein = new PostalAddressMoveIn(driver);
		concessionmovein = new ConcessionMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
		portalmovein = new PortalMoveIn(driver);
	}

	/**
	 * - verify that hitting Yes on the Cancel button would clear all data - using a
	 * residential accounts with attachments on Life Support and Concession
	 */
	@Test(priority = 1)
	public void verifyCancelYes01() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyCancelYes01 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsResidential("verifyCancelYes01", false, false);

		long startTime2 = logNanoTimeStamp();
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 15, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(2000);
		// verify the Cancel button
		clickElementAction(acceptancemovein.cancel);
		pauseSeleniumExecution(1000);
		// verify the Cancel message
		String cancelMsg = getDisplayedText(acceptancemovein.dialogContainerText, true);
		verifyTwoStringsAreEqual(cancelMsg,
				"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
				true);
		clickElementAction(acceptancemovein.yesCancelRequest);
		pauseSeleniumExecution(1000);
		String closeMsg = getDisplayedText(acceptancemovein.closeMessage, true);
		softAssertion.assertEquals(closeMsg,
				"This window/tab is no longer required, for privacy reasons we encourage you to close it",
				assertionErrorMsg(getLineNumber()));

		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(sessionLength, 0, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 1, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyCancelYes01 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that hitting Yes on the Cancel button would clear all data - using a
	 * commercial accounts with attachments on Life Support and Trade Waste
	 */
	@Test(priority = 2)
	public void verifyCancelYes02() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyCancelYes02 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsCommercial("verifyCancelYes02", false, false, 2, 2, "Glenn", "O'brien");

		long startTime2 = logNanoTimeStamp();
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
		String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(2000);
		// verify the Cancel button
		clickElementAction(acceptancemovein.cancel);
		pauseSeleniumExecution(1000);
		// verify the Cancel message
		String cancelMsg = getDisplayedText(acceptancemovein.dialogContainerText, true);
		verifyTwoStringsAreEqual(cancelMsg,
				"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
				true);
		clickElementAction(acceptancemovein.yesCancelRequest);
		pauseSeleniumExecution(1500);
		String closeMsg = getDisplayedText(acceptancemovein.closeMessage, true);
		softAssertion.assertEquals(closeMsg,
				"This window/tab is no longer required, for privacy reasons we encourage you to close it",
				assertionErrorMsg(getLineNumber()));

		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(sessionLength, 0, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 1, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyCancelYes02 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that if use_session_store == false, it would clear the data when
	 * refreshing the page - using a residential account
	 */
	@Test(priority = 3)
	public void verifySessionConfig01() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "22\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig01 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsResidential("verifySessionConfig01", true, false);

		long startTime2 = logNanoTimeStamp();
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadEmbeddedMoveInPortal(true, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the dev 3rd party prefill page
			// so if we just refresh the page, it would just go back
			// to the dev 3rd party prefill page.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// we will mimic a refresh by accessing the standalone move in page
			// since the current url is the url prefill
			// refreshing the page that way would just re-populate the fields again
			// by using the parameters from the URL - thus removing values
			// that was previously entered by the users.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the url prefill
			// refreshing the page that way would just re-populate the fields again
			// by using the parameters from the URL - thus removing values
			// that was previously entered by the users.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		storage = new BrowserLocalSessionStorage(driver);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig01 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that if use_session_store == false, it would clear the data when
	 * refreshing the page - using a commercial account
	 */
	@Test(priority = 4)
	public void verifySessionConfig02() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "22\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig02 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsCommercial("verifySessionConfig02", true, false, 2, 2, "Glenn", "O'brien");

		long startTime2 = logNanoTimeStamp();
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadEmbeddedMoveInPortal(true, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the dev 3rd party prefill page
			// so if we just refresh the page, it would just go back
			// to the dev 3rd party prefill page.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// we will mimic a refresh by accessing the standalone move in page
			// since the current url is the url prefill
			// refreshing the page that way would just re-populate the fields again
			// by using the parameters from the URL - thus removing values
			// that was previously entered by the users.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the url prefill
			// refreshing the page that way would just re-populate the fields again
			// by using the parameters from the URL - thus removing values
			// that was previously entered by the users.
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		storage = new BrowserLocalSessionStorage(driver);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig02 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that if use_session_store == true, it would clear the data when
	 * going to another portal - using a residential account
	 */
	@Test(priority = 5)
	public void verifySessionConfig03() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig03 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsResidential("verifySessionConfig03", false, false);

		long startTime2 = logNanoTimeStamp();
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 15, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);

		// TODO
		// put another scenario where we go to the Connection
		// and verify that the session got cleared

		// TODO
		// put another scenario where we go to the Customer Portal
		// and verify it has correct behavior

		// let's access a different portal
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMoveOut(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMoveOut(), true);
			switchToMoveOutEmbeddedIframe(1);
			loadPortal();
		}

		storage = new BrowserLocalSessionStorage(driver);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(sessionLength, 3, assertionErrorMsg(getLineNumber()));
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify we are in the Supply Details sections in the Move-Out
		SupplyDetailsMoveOut supplydetailsmoveout = new SupplyDetailsMoveOut(driver);
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmoveout.moveOutDate, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// go back to the move in portal
		// let's access the portal we are testing with
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			switchToMoveInEmbeddedIframe(1);
			loadPortal();
		}

		// verify the session keys got cleared
		storage = new BrowserLocalSessionStorage(driver);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig03 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that if use_session_store == true, it would not clear the data when
	 * going to another website - using a commercial account - submit the request
	 */
	@Test(priority = 6)
	public void verifySessionConfig04() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig04 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsCommercial("verifySessionConfig04", false, false, 2, 2, "Yasha", "Zaru");

		long startTime2 = logNanoTimeStamp();
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
		String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);

		// let's access a different website
		if (getPortalType().equals("standalone")) {
			accessPortal("https://www.google.com", true);
			pauseSeleniumExecution(5000);
		} else if (getPortalType().equals("embedded")) {
			accessPortal("https://www.google.com", true);
			pauseSeleniumExecution(5000);
		}

		// go back to the move in portal
		// let's access the portal we are testing with
		if (getPortalType().equals("standalone")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			embeddedMoveInSwitchFrame(1);
			loadPortal();
		} else if (getPortalType().equals("embedded")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			embeddedMoveInSwitchFrame(1);
			loadPortal();
		}

		// verify the session keys were not cleared
		storage = new BrowserLocalSessionStorage(driver);
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
		sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
		sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		sessionAppId = storage.getItemFromSessionStorage("application_id");
		sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the values stored in the local storage
		localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the supply details
		String ownerMoveInDate = getDisplayedValue(supplydetailsmovein.moveInDateOwner, true);
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		String streetNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		String streetName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		String streetType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		String postCode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String attachmentsLifeSup = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(ownerMoveInDate,
				getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, DATE_MONTH_YEAR_FORMAT_SLASH),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Tenancy", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "20", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetNum, "20", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetName, "Bella Vista", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetType, "Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Heathcote", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "New South Wales", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postCode, "2233", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.supplyConnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Intermittent Peritoneal Dialysis Machine"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Chronic Positive Airways Pressure Respirator"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Crigler Najjar Syndrome Phototherapy Equipment"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
						false, "Ventilator for Life Support"), 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		if (concatStrings(dragAndDropText, " ", attachmentsLifeSup).contains("requirement g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", attachmentsLifeSup),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB File uploaded successfully Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", attachmentsLifeSup),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);

		// verify the accounts details section
		String abnAcn = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
		String trading = getDisplayedValue(accountdetailsmovein.tradingName, false);
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(abnAcn, concatStrings(getProp("test_data_valid_acn4"), " (",
				getProp("test_data_valid_company_name_acn3_acn4"), ")"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(trading, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);

		// verify the trade waste section
		String equipOther = getDisplayedValue(tradewastemovein.tradeWasteOtherTextField, false);
		String typeChosen = getDisplayedText(tradewastemovein.businessActivity, false);
		String maxFlowRate = getDisplayedValue(tradewastemovein.maxFlowRate, false);
		String maxVolume = getDisplayedValue(tradewastemovein.maxDischargeVolume, false);
		String dischargeDate = getDisplayedValue(tradewastemovein.dischargeStartDate, false);
		String startHr = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, false);
		String startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, false);
		String startAmPm = getDisplayedText(tradewastemovein.dischargeHoursStartAmPm, true);
		String endHr = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, false);
		String endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, false);
		String endAmPm = getDisplayedText(tradewastemovein.dischargeHoursEndAmPm, true);
		dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String attachmentsTradeWaste = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(equipOther, "\"Other\" Equipment's'z", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(typeChosen, "Other", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(maxFlowRate, "10,000.279", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(maxVolume, "11,000.389", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeDate,
				getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -366, DATE_MONTH_YEAR_FORMAT_SLASH),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertTrue(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(startHr, "12", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(startMin, "00", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(startAmPm, "AM", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(endHr, "12", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(endMin, "00", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(endAmPm, "PM", assertionErrorMsg(getLineNumber()));
		if (concatStrings(dragAndDropText, " ", attachmentsTradeWaste).contains("upload Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", attachmentsTradeWaste),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully planet_in_deep_space-wallpaper-1920x1080.jpg .image/jpeg 0.8 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", attachmentsTradeWaste),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload planet_in_deep_space-wallpaper-1920x1080.jpg .image/jpeg 0.8 MB File uploaded successfully Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);

		// verify the main account contact section
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
		String emailAddress = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		String afterHours = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		String contactSecretCode = getDisplayedValue(mainaccountcontactmovein.contactSecretCode, false);
		softAssertion.assertEquals(firstName, "Yasha", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Zaru", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAddress, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0332878850", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "+61400853690", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(afterHours, "130285", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Testing 123...", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(400);
		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);

		// verify the additional contact
		firstName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
		lastName = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
		emailAddress = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		afterHours = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		contactSecretCode = getDisplayedValue(additionalcontactmovein.addCont1ContactSecretCode, false);
		softAssertion.assertEquals(firstName, "Roshan", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Britto", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAddress, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0332878850", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "+61400853690", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(afterHours, "+960985740362", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Inulitz.....", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);

		// verify the postal address
		softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);

		// verify the direct debit
		softAssertion.assertTrue(isElementTicked(directdebitmovein.bankAccount, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.bankAccountName, false), "Yasha Zaru",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountBSB, false), "000100",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountNumber, false), "001000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);

		// verify additional note
		String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
		softAssertion.assertEquals(notesArea, "The quick brown foxs jumps over the lazy dog.",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);

		scrollPageDown(1700);
		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
		List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
		logDebugMessage(concatStrings(this.className, " actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME,
				" is <", Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//		softAssertion.assertEquals(actualSize, 1,
//				"Incorrect number of objects inside the bucket '".concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		// verify all assertions
		softAssertion.assertAll();

		// tick all 3 checkboxes
		clickElementAction(acceptancemovein.firstCheckbox);
		if (getPortalType().equals("standalone")) {
			// we use javaScriptClickButton to click it because the method
			// clickButton mistakenly clicks the link which opens a new tab in standalone
			javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		} else if (getPortalType().equals("embedded")) {
			clickElementAction(acceptancemovein.secondCheckbox);
		}
		clickElementAction(acceptancemovein.thirdCheckbox);

		// submit the request
		clickElementAction(acceptancemovein.submit);
		// did this because there was an issue where initial click
		// on the submit button did not work
		retryClickSubmit(1);

		// used try/catch because sometimes we get StaleElementReferenceException
		String submitMsg = null;
		boolean submitMsgDisp = false;
		boolean checkSubmitMsg = true;
		try {
			submitMsg = getDisplayedText(acceptancemovein.submittingMessage, true);
			submitMsgDisp = true;
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(concatStrings(
					"StaleElementReferenceException has been encountred while checking for the progress bar text. Will reinitialize the element. See message for more details -> ",
					sere.getMessage()));
			// sometimes the message is not displayed and portal already returned a response
			// so we check if it no longer exists
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			if (isElementExists(acceptancemovein.submittingMessageList)) {
				submitMsg = getDisplayedText(acceptancemovein.submittingMessage, true);
				submitMsgDisp = true;
			}
		} catch (NoSuchElementException nsee) {
			logDebugMessage(
					"The 'Submitting your request...' message was missed because of retryClickSubmit(1). We will just check for the Success message");
			checkSubmitMsg = false;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		logDebugMessage(concatStrings("The value of submitMsgDisp [", String.valueOf(submitMsgDisp),
				"] while the value of checkSubmitMsg is [", String.valueOf(checkSubmitMsg), "]"));
		if (submitMsgDisp && checkSubmitMsg) {
			assertEquals(submitMsg, "Submitting your request...", assertionErrorMsg(getLineNumber()));
		}
		submitRequest(acceptancemovein.dialogContainerText);
		// verify it was a success
		String respHeader = getDisplayedText(acceptancemovein.dialogContainerHeader, true);
		softAssertion.assertEquals(respHeader, "Your application has been a success!",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.okDialog);
		pauseSeleniumExecution(1000);
		String closeMsg = getDisplayedText(acceptancemovein.closeMessage, true);
		softAssertion.assertEquals(closeMsg,
				"This window/tab is no longer required, for privacy reasons we encourage you to close it",
				assertionErrorMsg(getLineNumber()));
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertEquals(sessionLength, 0, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig04 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * - verify that if use_session_store == false, attachments would still be sent
	 * 
	 */
	@Test(priority = 7)
	public void verifySessionConfig05() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "23\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig05 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		// populate all sections until acceptance page
		populateAllSectionsCommercial("verifySessionConfig05", true, true, 2, 1, "Kinuyo", "Matsumoto");

		long startTime2 = logNanoTimeStamp();
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		this.sourceID = sessionSourceId;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"' test case 'verifySessionConfig05', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME,
					" is <", Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
		}

		scrollPageDown(1900);
		// tick all 3 checkboxes
		clickElementAction(acceptancemovein.firstCheckbox);
		if (getPortalType().equals("standalone")) {
			// we use javaScriptClickButton to click it because the method
			// clickButton mistakenly clicks the link which opens a new tab in standalone
			javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		} else if (getPortalType().equals("embedded")) {
			clickElementAction(acceptancemovein.secondCheckbox);
		}
		clickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("VerifyValidations01_verifySessionConfig05_moveInDate", this.commercialMoveInDate);
		addProp("VerifyValidations01_verifySessionConfig05_sourceID", this.sourceID);
		addProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));

		// submit the request
		clickElementAction(acceptancemovein.submit);
		// used try/catch because sometimes we get StaleElementReferenceException
		String submitMsg = null;
		boolean submitMsgDisp = false;
		try {
			submitMsg = getDisplayedText(acceptancemovein.submittingMessage, true);
			submitMsgDisp = true;
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(concatStrings(
					"StaleElementReferenceException has been encountred while checking for the progress bar text. Will reinitialize the element. See message for more details -> ",
					sere.getMessage()));
			// sometimes the message is not displayed and portal already returned a response
			// so we check if it no longer exists
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			if (isElementExists(acceptancemovein.submittingMessageList)) {
				submitMsg = getDisplayedText(acceptancemovein.submittingMessage, true);
				submitMsgDisp = true;
			}
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		if (submitMsgDisp) {
			assertEquals(submitMsg, "Submitting your request...", assertionErrorMsg(getLineNumber()));
		}
		submitRequest(acceptancemovein.dialogContainerText);
		// verify it was a success
		String respHeader = getDisplayedText(acceptancemovein.dialogContainerHeader, true);
		softAssertion.assertEquals(respHeader, "Your application has been a success!",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.okDialog);
		pauseSeleniumExecution(1000);
		String closeMsg = getDisplayedText(acceptancemovein.closeMessage, true);
		softAssertion.assertEquals(closeMsg,
				"This window/tab is no longer required, for privacy reasons we encourage you to close it",
				assertionErrorMsg(getLineNumber()));
		sessionKeys = storage.getAllKeysFromSessionStorage();
		sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		localKeys = storage.getAllKeysFromLocalStorage();
		localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertEquals(sessionLength, 0, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		long endTime2 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifySessionConfig05 [Remaining] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
	}

	/**
	 * Verify that an error would be displayed on load since the regex pattern on
	 * the portal config does not match the data passed for extra_data
	 */
	@Test(priority = 8)
	public void verifyExtraData01() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "26\\", "portal_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			goToUrl(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(true);
			// verify the page is fully loaded
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			goToUrl(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);
			// verify the page is fully loaded
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.TENANT, initialDate, false);
			loadEmbeddedMoveInPortal(true, false);

			// verify element is displayed
			waitUntilElementIsDisplayed(portalmovein.apiHeader, 10, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify elements are visible
			waitUntilElementIsVisible(10, portalmovein.apiHeader);
			waitUntilElementIsVisible(10, portalmovein.responseError);

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
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 7, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the session storage are not empty
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionThirdPartyData = storage.getItemFromSessionStorage("third_party_data");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionThirdPartyData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));

			// verify that elements are displayed
			softAssertion.assertTrue(isElementDisplayed(portalmovein.apiHeader, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(portalmovein.responseError, 0),
					assertionErrorMsg(getLineNumber()));
			// verify that an error is displayed since extra data is not valid
			softAssertion.assertEquals(getDisplayedText(portalmovein.apiHeader, true), "Unable to load move-in portal",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true), "Extra data is invalid",
					assertionErrorMsg(getLineNumber()));
			// TODO
			// verify that the Try Again button is not displayed
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DB_DATE_FORMAT);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
						"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
						"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=123456789", "&business_hour_phone=456789123",
						"&after_hour_phone=789123456", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
						"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
						"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=123456789", "&business_hour_phone=456789123",
						"&after_hour_phone=789123456", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, false);
			}

			// verify element is displayed
			waitUntilElementIsDisplayed(portalmovein.apiHeader, 10, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify elements are visible
			waitUntilElementIsVisible(10, portalmovein.apiHeader);
			waitUntilElementIsVisible(10, portalmovein.responseError);

			BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
			List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
			long sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			List<String> localKeys = storage.getAllKeysFromLocalStorage();
			long localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("urlPrefill"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 26, assertionErrorMsg(getLineNumber()));
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			String sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			String sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			String sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			String sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			String sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			String sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			String sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			String sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			String sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			String sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			String sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionUrlPrefill = storage.getItemFromSessionStorage("urlPrefill");
			String session3rdPartyData = storage.getItemFromSessionStorage("third_party_data");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStNum),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStName),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStType),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyCity), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyState),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyPostcode),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionUrlPrefill), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(session3rdPartyData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));

			// verify that elements are displayed
			softAssertion.assertTrue(isElementDisplayed(portalmovein.apiHeader, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(portalmovein.responseError, 0),
					assertionErrorMsg(getLineNumber()));
			// verify that an error is displayed since extra data is not valid
			softAssertion.assertEquals(getDisplayedText(portalmovein.apiHeader, true), "Unable to load move-in portal",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true), "Extra data is invalid",
					assertionErrorMsg(getLineNumber()));
			// TODO
			// verify that the Try Again button is not displayed
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * Verify that an error would be displayed since a regex pattern was specified,
	 * however extra_data does not have value specified
	 */
	@Test(priority = 9)
	public void verifyExtraData02() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "37\\", "agency_electricity_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DB_DATE_FORMAT);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=",
						getProp("test_data_valid_acn2"), "&business_trading_name=My Cloud",
						"&contact_first_name=Michael's", "&contact_last_name= O'Connell", "&mobile_number=11111111",
						"&business_hour_phone=22222222", "&after_hour_phone=33333333", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=",
						getProp("test_data_valid_acn2"), "&business_trading_name=My Cloud",
						"&contact_first_name=Michael's", "&contact_last_name= O'Connell", "&mobile_number=11111111",
						"&business_hour_phone=22222222", "&after_hour_phone=33333333", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, false);
			}

			// verify element is displayed
			waitUntilElementIsDisplayed(portalmovein.apiHeader, 10, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify elements are visible
			waitUntilElementIsVisible(10, portalmovein.apiHeader);
			waitUntilElementIsVisible(10, portalmovein.responseError);

			BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
			List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
			long sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			List<String> localKeys = storage.getAllKeysFromLocalStorage();
			long localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("urlPrefill"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 25, assertionErrorMsg(getLineNumber()));
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			String sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			String sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			String sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			String sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			String sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			String sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			String sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			String sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			String sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			String sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			String sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionUrlPrefill = storage.getItemFromSessionStorage("urlPrefill");
			String session3rdPartyData = storage.getItemFromSessionStorage("third_party_data");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStNum),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStName),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStType),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyCity), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyState),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyPostcode),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionUrlPrefill), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(session3rdPartyData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));

			// verify that elements are displayed
			softAssertion.assertTrue(isElementDisplayed(portalmovein.apiHeader, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(portalmovein.responseError, 0),
					assertionErrorMsg(getLineNumber()));
			// verify that an error is displayed since extra data is not valid
			softAssertion.assertEquals(getDisplayedText(portalmovein.apiHeader, true), "Unable to load move-in portal",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true), "Extra data is invalid",
					assertionErrorMsg(getLineNumber()));
			// TODO
			// verify that the Try Again button is not displayed
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * Verify that an error would be displayed since a regex pattern was specified,
	 * however extra_data attribute not defined
	 */
	@Test(priority = 10)
	public void verifyExtraData03() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "37\\", "agency_electricity_config.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DB_DATE_FORMAT);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=",
						getProp("test_data_valid_acn2"), "&business_trading_name=My Cloud",
						"&contact_first_name=Michael's", "&contact_last_name= O'Connell", "&mobile_number=11111111",
						"&business_hour_phone=22222222", "&after_hour_phone=33333333", "&email_address=",
						getProp("test_dummy_email_lower_case"));
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=",
						getProp("test_data_valid_acn2"), "&business_trading_name=My Cloud",
						"&contact_first_name=Michael's", "&contact_last_name= O'Connell", "&mobile_number=11111111",
						"&business_hour_phone=22222222", "&after_hour_phone=33333333", "&email_address=",
						getProp("test_dummy_email_lower_case"));
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, false);
			}

			// verify element is displayed
			waitUntilElementIsDisplayed(portalmovein.apiHeader, 10, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify elements are visible
			waitUntilElementIsVisible(10, portalmovein.apiHeader);
			waitUntilElementIsVisible(10, portalmovein.responseError);

			BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
			List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
			long sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			List<String> localKeys = storage.getAllKeysFromLocalStorage();
			long localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("urlPrefill"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 25, assertionErrorMsg(getLineNumber()));
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			String sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			String sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			String sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			String sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			String sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			String sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			String sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			String sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			String sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			String sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			String sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionUrlPrefill = storage.getItemFromSessionStorage("urlPrefill");
			String session3rdPartyData = storage.getItemFromSessionStorage("third_party_data");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStNum),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStName),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStType),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyCity), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyState),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyPostcode),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionUrlPrefill), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(session3rdPartyData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));

			// verify that elements are displayed
			softAssertion.assertTrue(isElementDisplayed(portalmovein.apiHeader, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(portalmovein.responseError, 0),
					assertionErrorMsg(getLineNumber()));
			// verify that an error is displayed since extra data is not valid
			softAssertion.assertEquals(getDisplayedText(portalmovein.apiHeader, true), "Unable to load move-in portal",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true), "Extra data is invalid",
					assertionErrorMsg(getLineNumber()));
			// TODO
			// verify that the Try Again button is not displayed
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}
}
