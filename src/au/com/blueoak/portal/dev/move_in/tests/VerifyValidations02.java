package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.customer_portal.CustomerPortalDevBase;
import au.com.blueoak.portal.dev.move_in.MoveInDevBase;
import au.com.blueoak.portal.pageObjects.connection.ConnectionDetailsConnection;
import au.com.blueoak.portal.pageObjects.connection.PortalConnection;
import au.com.blueoak.portal.pageObjects.customer_portal.LoginCustomer;
import au.com.blueoak.portal.pageObjects.move_in.AcceptanceMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AccountDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalNoteMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ConcessionMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ManagerHolidayLettingMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ToastMsgMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.pageObjects.move_out.AcceptanceMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountContactMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AccountDetailsMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.AdditionalNoteMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.ForwardingAddressMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.PortalMoveOut;
import au.com.blueoak.portal.pageObjects.move_out.SupplyDetailsMoveOut;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class VerifyValidations02 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
	TradeWasteMoveIn tradewastemovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
	PostalAddressMoveIn postaladdressmovein;
	ManagerHolidayLettingMoveIn managerholidaylettingmovein;
	ConcessionMoveIn concessionmovein;
	DirectDebitMoveIn directdebitmovein;
	AdditionalNoteMoveIn additionalnotemovein;
	AcceptanceMoveIn acceptancemovein;
	ToastMsgMoveIn toastmsgmovein;
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
	String moveInDateUrlPrefill;

	private void populateAllSectionsResidential(String testCaseName, SimpleDateFormat dateFormat,
			String dateSeparator) {

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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			String tenantDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, true);
			softAssertion.assertEquals(tenantDate, this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		String past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, dateFormat);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(past11Days, Keys.TAB);
		// click the tenant again to ensure that calendar is collapsed
		clickElementAction(supplydetailsmovein.tenant);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
		String future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, dateFormat);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future21Days, Keys.TAB);
		// click the tenant again to ensure that calendar is collapsed
		clickElementAction(supplydetailsmovein.tenant);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
		String past10Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, dateFormat);
		this.residentialMoveInDate = past10Days;
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(past10Days, Keys.TAB);
		// click button again to dismiss the calendar
		clickElementAction(supplydetailsmovein.tenant);
		String tenantDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, true);
		softAssertion.assertEquals(tenantDate, past10Days, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("16");
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
		// let's put a complex name
		supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
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
		assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		endTime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		// verify the Postal Address section is still displayed
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "3 Main Account Contact",
						"4 Postal Address", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
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
		String medExp = concatStrings(prevMonthStr, dateSeparator, curYearStr);
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
		medExp = concatStrings(monthStr, dateSeparator, expYrStr);
		// click to fix ElementNotInteractableException
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(mainaccountcontactmovein.medicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		String medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		// verify the fix for bug ticket BBPRTL-1970
		medExp = concatStrings(monthStr, dateSeparator, expYrStr);
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
		mainaccountcontactmovein.mobilePhone.sendKeys("  485 625  10  ");
		mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		// verify users cannot put space
		assertEquals(mobPhone, "48562510", assertionErrorMsg(getLineNumber()));
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
		mainaccountcontactmovein.businessPhone.sendKeys("  051 320  747 0  ");
		busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		// verify users cannot put space
		assertEquals(busPhone, "0513207470", assertionErrorMsg(getLineNumber()));
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
		mainaccountcontactmovein.afterhoursPhone.sendKeys("  060 385  114 9  ");
		afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		// verify users cannot put space
		assertEquals(afterHoursPhone, "0603851149", assertionErrorMsg(getLineNumber()));
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
		mainaccountcontactmovein.mobilePhone.sendKeys("0836987570");
		mainaccountcontactmovein.businessPhone.sendKeys("0701098743");
		mainaccountcontactmovein.afterhoursPhone.sendKeys("0478340469");
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		medExp = concatStrings(prevMonthStr, dateSeparator, curYearStr);
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
		month = 1;
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		monthStr = Integer.toString(month);
		expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, dateSeparator, expYrStr);
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(additionalcontactmovein.addCont1MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		medCareExp = getDisplayedValue(additionalcontactmovein.addCont1MedicareCardExpiry, true);
		// verify the fix for bug ticket BBPRTL-1970
		medExp = concatStrings(monthStr, dateSeparator, expYrStr);
		verifyStringContains(true, medCareExp, medExp);
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
		additionalcontactmovein.addCont1MobilePhone.sendKeys("  +61 1  9856   4712  ");
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		// verify users cannot put space
		assertEquals(mobPhone, "+61198564712", assertionErrorMsg(getLineNumber()));
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
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("  +  61  5  9856  4712   ");
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		// verify users cannot put space
		assertEquals(busPhone, "+61598564712", assertionErrorMsg(getLineNumber()));
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
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+ 61   6 9856  471 2  ");
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		// verify users cannot put space
		assertEquals(afterHoursPhone, "+61698564712", assertionErrorMsg(getLineNumber()));
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
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0793007450");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("0886680347");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+0123456789012");
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		// verify field not in error state
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		concessionmovein.cardNumExpiry.sendKeys(prevMonthStr, dateSeparator, curYearStr, Keys.TAB);
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
		String conExp = concatStrings(monthStr, dateSeparator, expYrStr);
		concessionmovein.cardNumExpiry.sendKeys(conExp, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForUpload);
		pauseSeleniumExecution(1000);
		String concessionExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		// verify the fix for bug ticket BBPRTL-1970
		conExp = concatStrings(monthStr, dateSeparator, expYrStr);
		verifyStringContains(true, concessionExp, conExp);
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		assertEquals(loadingMsg, "Creating secure area for credit card entry...",
				"Credit Card initialization progress bar text is not correct");
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
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
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
				" [Additional Note] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(startTime, endTime)), ">"));

		/*
		 * End of Additional Note section
		 */

		logDebugMessage("Finished populating the Supply Details until Additional Notes section");
		long totalEndtime = logNanoTimeStamp();
		logDebugMessage(concatStrings("VerifyValidations02.populateAllSectionsResidential.", testCaseName,
				" [Total] execution time in minutes <",
				String.valueOf(getTotalExecutionInMin(totalStartime, totalEndtime)), ">"));
	}

	private void populateAllSectionsCommercial(String testCaseName, SimpleDateFormat dateFormat, int numOfAttachLifeSup,
			int numOfAttachTradeWaste, String mainFirstName, String mainLastName) {

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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
		String future12Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, dateFormat);
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Bella Vista");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Heathcote");
			supplydetailsmovein.supplyAddState.sendKeys("New South Wales", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("2233");
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
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(supplydetailsmovein.medCoolingYes);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
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
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.commercial);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
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
		String dischargeStartDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -366, dateFormat);
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
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details",
				"create Account Details", "create Trade Waste", "4 Main Account Contact", "5 Postal Address",
				"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
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
		mainaccountcontactmovein.mobilePhone.sendKeys("0285399360");
		mainaccountcontactmovein.businessPhone.sendKeys("+61890052366");
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
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("0819312650");
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
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		assertEquals(loadingMsg, "Creating secure area for credit card entry...",
				"Credit Card initialization progress bar text is not correct");
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
		managerholidaylettingmovein = new ManagerHolidayLettingMoveIn(driver);
		concessionmovein = new ConcessionMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
		toastmsgmovein = new ToastMsgMoveIn(driver);
		portalmovein = new PortalMoveIn(driver);
	}

	/**
	 * Verify that an error would be displayed if the wrong portal config is
	 * specified in the url prefill
	 */
	@Test(priority = 1)
	public void verifyUrlPrefillValidation01() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_configs.json",
						"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
						"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=12345678", "&business_hour_phone=45678912",
						"&after_hour_phone=78912345", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_configs.json",
						"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
						"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=12345678", "&business_hour_phone=45678912",
						"&after_hour_phone=78912345", "&email_address=", getProp("test_dummy_email_lower_case"),
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
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionUrlPrefill = storage.getItemFromSessionStorage("urlPrefill");
			String session3rdPartyData = storage.getItemFromSessionStorage("third_party_data");
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
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true),
					"Problem loading move-in portal, please try again shortly", assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1996
			softAssertion.assertTrue(isElementDisplayed(portalmovein.tryAgain, 0), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(portalmovein.tryAgain);
			loadPortal();
			// verify that elements are still displayed
			softAssertion.assertTrue(isElementDisplayed(portalmovein.apiHeader, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(portalmovein.responseError, 0),
					assertionErrorMsg(getLineNumber()));
			// verify that an error is displayed since extra data is not valid
			softAssertion.assertEquals(getDisplayedText(portalmovein.apiHeader, true), "Unable to load move-in portal",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmovein.responseError, true),
					"Problem loading move-in portal, please try again shortly", assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1996
			softAssertion.assertTrue(isElementDisplayed(portalmovein.tryAgain, 0), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * Verify that url prefill would still work if only config was specified using
	 * Residential
	 */
	@Test(priority = 2)
	public void verifyUrlPrefillValidation02() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "45\\", "agency_electricity_config.json");

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);
			}

			long endTime1 = logNanoTimeStamp();
			logDebugMessage(concatStrings("verifyUrlPrefillValidation02 [Accessed Portal] execution time in seconds <",
					String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

			// populate all sections until acceptance page
			populateAllSectionsResidential("verifyUrlPrefillValidation02", DATE_MONTH_YEAR_FORMAT_DASH, "-");

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
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 16, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddNotes), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			long endTime2 = logNanoTimeStamp();
			logDebugMessage(concatStrings("verifyUrlPrefillValidation02 [Remaining] execution time in seconds <",
					String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * Verify that url prefill would still work if only config was specified using
	 * Commercial
	 */
	@Test(priority = 3)
	public void verifyUrlPrefillValidation03() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "45\\", "agency_electricity_config.json");

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);
			}

			long endTime1 = logNanoTimeStamp();
			logDebugMessage(concatStrings("verifyUrlPrefillValidation03 [Accessed Portal] execution time in seconds <",
					String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

			// populate all sections until acceptance page
			populateAllSectionsCommercial("verifyUrlPrefillValidation03", DATE_MONTH_YEAR_FORMAT_DASH, 2, 2, "Glenn",
					"O'brien");

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
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 15, assertionErrorMsg(getLineNumber()));
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
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			long endTime2 = logNanoTimeStamp();
			logDebugMessage(concatStrings("verifyCancelYes02 [Remaining] execution time in seconds <",
					String.valueOf(getTotalExecutionInSec(startTime2, endTime2)), ">"));
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * <pre>
	 * Here we will:
	 * - prefill with a defined config
	 * - then assert the expected values
	 * - then using the same tab, we will enter a different URL prefill
	 * 		and we will not define the config using an almost the same URL prefill
	 * - then assert the expected values
	 * - verify the fix for bug ticket BBPRTL-1998
	 * </pre>
	 */
	@Test(priority = 4)
	public void verifyUrlPrefillValidation04() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "37\\", "agency_electricity_config.json");

			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "38\\", "portal_config.json");

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 4, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 4,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.RESIDENTIAL.name(), "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=0211111111", "&business_hour_phone=0322222222",
						"&after_hour_phone=0433333333", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data=01234567890", "&complex_name='001 Complex's");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=", TenancyTypesEnum.Apt.name(),
						"&tenancy_number=1328", "&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast",
						"&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach",
						"&tenancy_postcode=4221", "&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(),
						"&account_type=", AccountTypesEnum.RESIDENTIAL.name(), "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=0211111111", "&business_hour_phone=0322222222",
						"&after_hour_phone=0433333333", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data=01234567890", "&complex_name='001 Complex's");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, true);
			}

			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(complexName, "'001 Complex's", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "1328", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Gold Coast", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
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
			// verify the page is fully loaded
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Michael's O'Connell)", "4 Postal Address",
					"5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyUnknown);
			scrollPageDown(500);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingNo);
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");

			// verify Account Details section
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields are not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			// verify Main Account Contact section
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, true), "Michael's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, true), "O'Connell",
					assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			// verify Date of Birth is displayed
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.dateOfBirth, 0),
					assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			// verify personal identification displayed
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblPersonalIDHeader, 0),
					assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.driversLicenceList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.passportList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.medicareCardList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.provideNoneList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the notifications that should be ticked by default and not
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
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
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, true),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, true), "0211111111",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, true), "0322222222",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true), "0433333333",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.provideNone);
			mainaccountcontactmovein.contactSecretCode.sendKeys("Sekrekt's");
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressNo, 0),
					"We are not yet in the Postal Address section");

			clickElementAction(postaladdressmovein.sameSupAddressYes);
			clickElementAction(postaladdressmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(concessionmovein.addConcessionNo, 0),
					"We are not yet in the Concession section");

			clickElementAction(concessionmovein.addConcessionNo);
			clickElementAction(concessionmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0),
					"We are not yet in the Direct Debit section");

			BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
			List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
			long sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			List<String> localKeys = storage.getAllKeysFromLocalStorage();
			long localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-complex_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 31, assertionErrorMsg(getLineNumber()));
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			String sessionQueryComplexName = storage.getItemFromSessionStorage("move-in-query-complex_name");
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			String sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			String sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			String sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			String sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			String sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			String sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			String sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryComplexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStType),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyPostcode),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyCity), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStName),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyState),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStNum),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			logDebugMessage("We will be updating the URL Prefill for verifyUrlPrefillValidation04");
			int days = 6;
			moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, days, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, days,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "account_category=",
						AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate, "&tenancy_type=",
						TenancyTypesEnum.House.getLabelText(), "&tenancy_number=#123-A", "&tenancy_street_number=90210",
						"&tenancy_street_name=Beverly Hills", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael",
						"&contact_last_name= O'Connell's", "&mobile_number=11111111", "&business_hour_phone=22222222",
						"&after_hour_phone=33333333", "&email_address=", getProp("test_dummy_email_upper_case"),
						"&extra_data=01234567890");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "account_category=",
						AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate, "&tenancy_type=",
						TenancyTypesEnum.House.getLabelText(), "&tenancy_number=#123-A", "&tenancy_street_number=90210",
						"&tenancy_street_name=Beverly Hills", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael",
						"&contact_last_name= O'Connell's", "&mobile_number=11111111", "&business_hour_phone=22222222",
						"&after_hour_phone=33333333", "&email_address=", getProp("test_dummy_email_upper_case"),
						"&extra_data=01234567890");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, true);
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

			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 29, assertionErrorMsg(getLineNumber()));
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStType),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyPostcode),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyCity), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStName),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyState),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyStNum),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));

			// verify that automatically the date is converted into slash, even though we
			// pass it as dash
			String expectedDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, days, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false), expectedDate,
					assertionErrorMsg(getLineNumber()));
			// verify populated correctly
			complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "House", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "#123-A", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "90210", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Beverly Hills", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
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
			// verify the page is fully loaded
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "create Account Details",
					"3 Trade Waste", "4 Main Account Contact (Michael O'Connell's)", "5 Postal Address",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyUnknown);
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingNo);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");

			// verify Account Details section
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields are editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the values
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, true),
					concatStrings(getProp("test_data_valid_acn1"), " (",
							getProp("test_data_valid_company_name_acn1_acn2"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, true), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
					"We are not yet in the Trade Waste section");

			clickElementAction(tradewastemovein.tradeWasteDischargeNo);
			clickElementAction(tradewastemovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
					"We are not yet in the Main Account Contact section");

			// verify Main Account Contact section
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, true), "Michael",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, true), "O'Connell's",
					assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			// verify Date of Birth is not displayed
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
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, true),
					getProp("test_dummy_email_upper_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, true), "11111111",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, true), "22222222",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true), "33333333",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.contactSecretCode, false)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
					assertionErrorMsg(getLineNumber()));
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"create Trade Waste", "4 Main Account Contact (Michael O'Connell's)", "5 Direct Debit",
					"6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * <pre>
	 * Here we will:
	 * - prefill with a defined config
	 * - then assert the expected values
	 * - then using the same tab, we will enter a differnt URL prefill
	 * 		and we will define the a different config using an almost the same URL prefill
	 * - then assert the expected values
	 * - verify the fix for bug ticket BBPRTL-1999
	 * </pre>
	 */
	@Test(priority = 5)
	public void verifyUrlPrefillValidation05() {

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			logDebugMessage(concatStrings("This test case does not apply using this configuration. Portal type '",
					getPortalType(), "' and Populate Data method '", getPopulateDataMethod(), "'"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "43\\", "agency_electricity_config.json");

			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "44\\", "agency_electricity_config_no_prop.json");

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
			}
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 2, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 2,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=",
						TenancyTypesEnum.Villa.getLabelText().toLowerCase(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=0211111111", "&business_hour_phone=0322222222",
						"&after_hour_phone=0433333333", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data=01234567890");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config.json", "&account_category=", AccountCategoryEnum.OWNER.name(),
						"&move_in_date=", moveInDate, "&tenancy_type=",
						TenancyTypesEnum.Villa.getLabelText().toLowerCase(), "&tenancy_number=1328",
						"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
						StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
						"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael's",
						"&contact_last_name= O'Connell", "&mobile_number=0211111111", "&business_hour_phone=0322222222",
						"&after_hour_phone=0433333333", "&email_address=", getProp("test_dummy_email_lower_case"),
						"&extra_data=01234567890");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, true);
			}

			BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
			String extraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertEquals(extraData, "01234567890", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Villa", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "1328", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Gold Coast", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
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
			// verify the page is fully loaded
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Trade Waste", "4 Main Account Contact (Michael's O'Connell)",
					"5 Additional Contact", "6 Postal Address", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
			clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
			clickElementAction(supplydetailsmovein.supplyUnknown);
			scrollPageDown(500);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingNo);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");

			// verify Account Details section
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields are not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the values
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, true),
					concatStrings(getProp("test_data_valid_acn1"), " (",
							getProp("test_data_valid_company_name_acn1_acn2"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, true), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeNo, 0),
					"We are not yet in the Trade Waste section");

			// verify Trade Waste section
			clickElementAction(tradewastemovein.tradeWasteDischargeNo);
			clickElementAction(tradewastemovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			// verify Main Account Contact section
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, true), "Michael's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, true), "O'Connell",
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
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the notifications that should be ticked by default and not
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
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
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, true),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, true), "0211111111",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, true), "0322222222",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true), "0433333333",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			mainaccountcontactmovein.contactSecretCode.sendKeys("Sekrekt's");
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
					"We are not yet in the Additional Contact section");

			additionalcontactmovein.addCont1FirstName.sendKeys("FirstName");
			additionalcontactmovein.addCont1LastName.sendKeys("LastName");
			additionalcontactmovein.addCont1EmailAddress.sendKeys("sample.email@testing.com");
			additionalcontactmovein.addCont1MobilePhone.sendKeys("+61474329541");
			additionalcontactmovein.addCont1BusinessPhone.sendKeys("+61773245546");
			additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+61893653470");
			additionalcontactmovein.addCont1ContactSecretCode.sendKeys("Sekretoe");
			clickElementAction(additionalcontactmovein.addCont1Next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
					"We are not yet in the Postal Address section");

			clickElementAction(postaladdressmovein.sameSupAddressYes);
			clickElementAction(postaladdressmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
					"We are not yet in the Holiday Letting/Rental Company Details section");

			managerholidaylettingmovein.companyName.sendKeys("Comp name");
			managerholidaylettingmovein.companyContactNum.sendKeys("Anytime");
			managerholidaylettingmovein.address01.sendKeys("Add-01");
			managerholidaylettingmovein.city.sendKeys("City");
			managerholidaylettingmovein.state.sendKeys("State");
			managerholidaylettingmovein.postCode.sendKeys("90210");
			managerholidaylettingmovein.country.sendKeys("Australia", Keys.TAB);
			clickElementAction(managerholidaylettingmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
					"We are not yet in the Direct Debit section");

			clickElementAction(directdebitmovein.noDirectDebit);
			clickElementAction(directdebitmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
					"We are not yet in the Additional Note section");

			additionalnotemovein.notesArea.sendKeys("The Quick Brown Fox Jumps Over The Lazy Dog.");
			clickElementAction(additionalnotemovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
					"We are not yet in the Acceptance section");

			List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
			long sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			List<String> localKeys = storage.getAllKeysFromLocalStorage();
			long localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.notes"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-mobile_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-after_hour_phone"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 36, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			logDebugMessage("We will be updating the URL Prefill for verifyUrlPrefillValidation05");
			int days = -2;
			moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -2, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, days,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						"config=agency_electricity_config_no_prop.json", "&account_category=",
						AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate, "&tenancy_street_number=6501",
						"&tenancy_street_name=Zone 1", "&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(),
						"&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221", "&tenancy_state=",
						AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael", "&contact_last_name=Jordak's",
						"&extra_data=378282246310005");
				goToUrl(urlPrefill, true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						"config=agency_electricity_config_no_prop.json", "&account_category=",
						AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate, "&tenancy_street_number=6501",
						"&tenancy_street_name=Zone 1", "&tenancy_street_type=", StreetTypesEnum.HWY.getLabelText(),
						"&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221", "&tenancy_state=",
						AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Michael", "&contact_last_name=Jordak's",
						"&extra_data=378282246310005");
				goToUrl(urlPrefill, true);
				loadEmbeddedMoveInPortal(true, true);
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

			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_postcode"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_state"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_suburb"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_trading_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 24, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));

			// verify that the extra_data was updated
			extraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertEquals(extraData, "378282246310005", assertionErrorMsg(getLineNumber()));
			String expectedDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, days, DATE_MONTH_YEAR_FORMAT_DASH);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false), expectedDate,
					assertionErrorMsg(getLineNumber()));
			// verify populated correctly
			complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "6501", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Zone 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify not ticked
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
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
			// verify the page is fully loaded
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "create Account Details",
					"3 Trade Waste", "4 Main Account Contact (Michael Jordak's)", "5 Direct Debit", "6 Additional Note",
					"7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// make sure we can interact with Supply Details
			clickElementAction(supplydetailsmovein.supplyAddStreetNum);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetName);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddCity);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddComplexName.sendKeys("Complex Name");
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
			supplydetailsmovein.supplyAddStreetNum.sendKeys("123");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Arabasta");
			supplydetailsmovein.supplyAddCity.sendKeys("East Blue");

			// verify populated correctly
			complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertEquals(complexName, "Complex Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "123", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Arabasta", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "East Blue", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.propManSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyConnected);
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingNo);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");

			// verify Account Details section
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields are editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the values
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, true),
					concatStrings(getProp("test_data_valid_acn1"), " (",
							getProp("test_data_valid_company_name_acn1_acn2"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, true), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeNo, 0),
					"We are not yet in the Trade Waste section");

			// verify Trade Waste section
			clickElementAction(tradewastemovein.tradeWasteDischargeNo);
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			// verify Main Account Contact section
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, true), "Michael",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, true), "Jordak's",
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
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the notifications that should be ticked by default and not
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
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
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.emailAddress, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.mobilePhone, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.businessPhone, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify postal would be displayed
			javaScriptClickElementAction(mainaccountcontactmovein.marketingComPostal);
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"create Trade Waste", "4 Main Account Contact (Michael Jordak's)", "5 Postal Address",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			mainaccountcontactmovein.emailAddress.sendKeys("tralala@testing.com");
			mainaccountcontactmovein.mobilePhone.sendKeys("+0985485365");
			mainaccountcontactmovein.businessPhone.sendKeys("+639268585205");
			mainaccountcontactmovein.afterhoursPhone.sendKeys("+1111111321585");
			mainaccountcontactmovein.contactSecretCode.sendKeys("wetweeew");
			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
					"We are not yet in the Additional Contact section");

			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1FirstName, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1LastName, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, true)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1ContactSecretCode, true)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Customer Portal, then go to Move In and verify that the
	 * correct config is picked up
	 */
	@Test(priority = 6)
	public void verifyCssAndLangFileValidation01() {

		// upload the correct portal_config.json we are testing
		CustomerPortalDevBase customerPort = new CustomerPortalDevBase();
		uploadMoveInCustomCss(s3Access);
		customerPort.uploadCustomerPortalCustomCss(s3Access);

		// upload the portal configs we are using
		customerPort.uploadCustomerPortalConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// upload the custom language files we are going to use
		customerPort.uploadCustomerPortalCustomLangFile(s3Access, "02\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlCustomerPortal(), true);
			loadStandaloneCustomerPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlCustomerPortal(), true);
			loadEmbeddedCustomerPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		CustomerPortalDevBase portal = new CustomerPortalDevBase();
		LoginCustomer logincustomer = new LoginCustomer(driver);
		softAssertion.assertEquals(getDisplayedText(logincustomer.lblLoginTitle, true),
				"BlueBilling Online Portal Login", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(logincustomer.lblLoginTitle), portal.mainHeaderExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(logincustomer.labelInput, "Email Address"),
				portal.placeholderExpCss, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(logincustomer.labelInput, "Password"), portal.placeholderExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(logincustomer.showPasswordIcon), portal.iconExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(logincustomer.underlineEmailAddress),
				portal.underlineExpCssNotFocused, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(logincustomer.underlinePassword), portal.underlineExpCssNotFocused,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(logincustomer.loginBtn, true), "Log in",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(logincustomer.loginBtn), portal.buttonExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(logincustomer.resetPasswordLink, true),
				"Forgot password? First time user?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(logincustomer.resetPasswordLink), portal.smallTextExpCss,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs",
					"3 mAiN AcCoUnT CoNtAcT", "4 dIrEcT DeBiT", "5 aDdItIoNaL NoTe", "6 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			slowSendKeys(supplydetailsmovein.supplyAddSearch, "Unit 16/ 6 Mari Street Alexandra", true, 300);
			chooseAddress(supplydetailsmovein.supplyAddressesDiv, "unit 16/6 Mari Street, Alexandra Headland QLD",
					"unit 16/6 Mari Street, Alexandra Headland Queensland");
			pauseSeleniumExecution(1000);
			clickElementAction(supplydetailsmovein.supplyConnected);
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "6", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Mari", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Alexandra Headland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4572", assertionErrorMsg(getLineNumber()));
			scrollPageDown(500);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingYes);
			// upload med cooling files
			uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Test PNG Type 01.png");
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
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Test PNG Type 01 .png 0.1 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			waitForCssToRender();
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 cOnCeSsIoN", "5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// prefilled values should not be in error state
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

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			supplydetailsmovein.supplyAddTenancyNum.sendKeys("01-100");
			clickElementAction(supplydetailsmovein.supplyConnected);
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingYes);
			// upload med cooling files
			uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Test PNG Type 01.png");
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
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Test PNG Type 01 .png 0.1 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Monkey Luffy's)", "4 Postal Address",
					"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyConnectedList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyDisconnectedList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyUnknownList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"Lease Commencement Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			supplydetailsmovein.supplyAddTenancyNum.sendKeys("01-100");
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.lifeSupNo);
			clickElementAction(supplydetailsmovein.medCoolingYes);
			// upload med cooling files
			uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Test PNG Type 01.png");
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
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Test PNG Type 01 .png 0.1 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Move In then to go Customer Portal and verify that the
	 * correct config is picked up
	 */
	@Test(priority = 7)
	public void verifyCssAndLangFileValidation02() {

		// upload the correct portal_config.json we are testing
		CustomerPortalDevBase customerPort = new CustomerPortalDevBase();
		uploadMoveInCustomCss(s3Access);
		customerPort.uploadCustomerPortalCustomCss(s3Access);

		// upload the portal configs we are using
		customerPort.uploadCustomerPortalConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// upload the custom language files we are going to use
		customerPort.uploadCustomerPortalCustomLangFile(s3Access, "02\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify in error state
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
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
			// verify the values
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "6", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Mari", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Alexandra Headland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4572", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.supplyConnected, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Test PNG Type 01.png .image/png 0.1 MB fIlE UpLoAdEd sUcCeSsFuLlY",
					assertionErrorMsg(getLineNumber()));

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "cOmPlEx nAmE (iF KnOwN)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs",
					"3 mAiN AcCoUnT CoNtAcT", "4 dIrEcT DeBiT", "5 aDdItIoNaL NoTe", "6 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("6-10", "Mari-chan", StreetTypesEnum.RD, "Beverly Hills", AustralianStatesEnum.QLD,
					"90210", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
			// verify the expected values
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "6-10", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Mari-chan", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Beverly Hills", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "90210", assertionErrorMsg(getLineNumber()));
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 tRaDe wAsTe",
							"4 mAiN AcCoUnT CoNtAcT", "5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateOwner, true),
					"mOvE In dAtE (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateOwner), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(tradewastemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify that there's no attachment
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.medCoolingYes);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt ",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 20, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 20,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate, "&complex_name='002 Complex's",
						"&tenancy_type=", TenancyTypesEnum.Villa.name(), "&tenancy_number=01-100",
						"&tenancy_street_number=6-10", "&tenancy_street_name=Mari-chan", "&tenancy_street_type=",
						StreetTypesEnum.RD.name(), "&tenancy_suburb=Beverly Hills", "&tenancy_postcode=90210",
						"&tenancy_state=", AustralianStatesEnum.QLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate, "&complex_name='002 Complex's",
						"&tenancy_type=", TenancyTypesEnum.Villa.name(), "&tenancy_number=01-100",
						"&tenancy_street_number=6-10", "&tenancy_street_name=Mari-chan", "&tenancy_street_type=",
						StreetTypesEnum.RD.name(), "&tenancy_suburb=Beverly Hills", "&tenancy_postcode=90210",
						"&tenancy_state=", AustralianStatesEnum.QLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyConnectedList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyDisconnectedList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyUnknownList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the expected values
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			softAssertion.assertEquals(complexName, "'002 Complex's", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Villa", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "01-100", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "6-10", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Mari-chan", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Beverly Hills", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "90210", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.medCoolingNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Monkey Luffy's)", "4 Postal Address",
					"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateOwner, true),
					"Move In Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateOwner), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify that there's no attachment
			scrollPageDown(400);
			clickElementAction(supplydetailsmovein.medCoolingYes);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlCustomerPortal(), true);
			loadStandaloneCustomerPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlCustomerPortal(), true);
			loadEmbeddedCustomerPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		CustomerPortalDevBase portal = new CustomerPortalDevBase();
		LoginCustomer logincustomer = new LoginCustomer(driver);
		softAssertion.assertEquals(getDisplayedText(logincustomer.lblLoginTitle, true),
				"BlueBilling Online Portal Login", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(logincustomer.lblLoginTitle), portal.mainHeaderExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(logincustomer.labelInput, "Email Address"),
				portal.placeholderExpCss, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(logincustomer.labelInput, "Password"), portal.placeholderExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(logincustomer.showPasswordIcon), portal.iconExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(logincustomer.underlineEmailAddress),
				portal.underlineExpCssNotFocused, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(logincustomer.underlinePassword), portal.underlineExpCssNotFocused,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(logincustomer.loginBtn, true), "Log in",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(logincustomer.loginBtn), portal.buttonExpCss,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(logincustomer.resetPasswordLink, true),
				"Forgot password? First time user?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(logincustomer.resetPasswordLink), portal.smallTextExpCss,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Move Out, then go to Move In and verify that the correct
	 * config is picked up
	 */
	@Test(priority = 8)
	public void verifyCssAndLangFileValidation03() {

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveOut(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveOut(), true);
			loadPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		SupplyDetailsMoveOut supplymoveout = new SupplyDetailsMoveOut(driver);
		AccountDetailsMoveOut accountmoveout = new AccountDetailsMoveOut(driver);
		AccountContactMoveOut contactmoveout = new AccountContactMoveOut(driver);
		ForwardingAddressMoveOut addressmoveout = new ForwardingAddressMoveOut(driver);
		AdditionalNoteMoveOut notesmoveout = new AdditionalNoteMoveOut(driver);
		AcceptanceMoveOut acceptancemoveout = new AcceptanceMoveOut(driver);

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplymoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// let's verify the header and introduction
		if (getPortalType().equals("standalone")) {
			String header = getDisplayedText(supplymoveout.lblMainHeader, true);
			String headerIntro = getDisplayedText(supplymoveout.lblSupplyDetailsIntro, true);
			softAssertion.assertEquals(header, "Selenium BlueBilling Move Out Request",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblMainHeader), MAIN_HEADER_LABEL_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"The below online form notifies Blue Oak Solution Pty Ltd (BlueOak) that you are moving out of a property that BlueOak supplies electricity and/or utilities. We'll process your disconnection request and send any further communication including a final bill to your new address. If you wish to use BlueOak as your electricity supplier for your new address, you can also do this through the form below",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyDetailsIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			PortalMoveOut portalmoveout = new PortalMoveOut(driver);
			softAssertion.assertEquals(getCssBackgrndColorProp(portalmoveout.bodyBackground), BODY_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(portalmoveout.footer), FOOTER_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmoveout.footerText, true),
					"Need Help? Call us on 1300 584 628, or email us at support@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.footerText), LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpNumber), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpEmail), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalmoveout.linkFooterHelpNumber);
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpNumber), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalmoveout.linkFooterHelpEmail);
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpEmail), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			String headerIntro = getDisplayedText(supplymoveout.lblSupplyDetailsIntro, true);
			supplymoveout = new SupplyDetailsMoveOut(driver, 0);
			softAssertion.assertFalse(isElementExists(supplymoveout.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"The below online form notifies Blue Oak Solution Pty Ltd (BlueOak) that you are moving out of a property that BlueOak supplies electricity and/or utilities. We'll process your disconnection request and send any further communication including a final bill to your new address. If you wish to use BlueOak as your electricity supplier for your new address, you can also do this through the form below",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyDetailsIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		}

		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
				"3 Account Contact", "4 Forwarding Address", "5 Additional Note", "6 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplymoveout.header), SECTION_HEADER_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(contactmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(addressmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(notesmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplymoveout.moveOutDate, true),
				"Move Out Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplymoveout.labelInput, "Move Out Date (DD/MM/YYYY)"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplymoveout.iconMoveOutDate), DATEPICKER_ICON_VALID_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplymoveout.underlineMoveOutDate), UNDERLINE_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplymoveout.lblSupplyAddHeader, true), "Supply Address",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyAddHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplymoveout.supplyAddSearch, true),
				"Please start typing supply address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplymoveout.labelInput, "Please start typing supply address"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(supplymoveout.iconSupplyAddSearch), GLOBE_ICON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplymoveout.underlineSupplyAddSearch),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplymoveout.next, true), "Next",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(supplymoveout.next), NEXT_BUTTON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT", "4 pOsTaL AdDrEsS",
							"5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// prefilled values should not be in error state
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

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "create Account Details",
					"3 Main Account Contact (Monkey Luffy's)", "4 Postal Address", "5 Direct Debit",
					"6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"Lease Commencement Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Move In, then go to Move Out and verify that the correct
	 * config is picked up
	 */
	@Test(priority = 9)
	public void verifyCssAndLangFileValidation04() {

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT", "4 pOsTaL AdDrEsS",
							"5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// prefilled values should not be in error state
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

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Monkey Luffy's)", "4 Postal Address",
					"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"Lease Commencement Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveOut(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveOut(), true);
			loadPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		SupplyDetailsMoveOut supplymoveout = new SupplyDetailsMoveOut(driver);
		AccountDetailsMoveOut accountmoveout = new AccountDetailsMoveOut(driver);
		AccountContactMoveOut contactmoveout = new AccountContactMoveOut(driver);
		ForwardingAddressMoveOut forwardingmoveout = new ForwardingAddressMoveOut(driver);
		AdditionalNoteMoveOut notesmoveout = new AdditionalNoteMoveOut(driver);
		AcceptanceMoveOut acceptancemoveout = new AcceptanceMoveOut(driver);

		// let's switch to the Move-Out Iframe
		// if it's embedded
		embeddedMoveOutSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplymoveout.header, PORTAL_ELEMENT_WAIT_TIMEOUT, PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// let's verify the header and introduction
		if (getPortalType().equals("standalone")) {
			String header = getDisplayedText(supplymoveout.lblMainHeader, true);
			String headerIntro = getDisplayedText(supplymoveout.lblSupplyDetailsIntro, true);
			softAssertion.assertEquals(header, "Selenium BlueBilling Move Out Request",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblMainHeader), MAIN_HEADER_LABEL_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"The below online form notifies Blue Oak Solution Pty Ltd (BlueOak) that you are moving out of a property that BlueOak supplies electricity and/or utilities. We'll process your disconnection request and send any further communication including a final bill to your new address. If you wish to use BlueOak as your electricity supplier for your new address, you can also do this through the form below",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyDetailsIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			PortalMoveOut portalmoveout = new PortalMoveOut(driver);
			softAssertion.assertEquals(getCssBackgrndColorProp(portalmoveout.bodyBackground), BODY_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(portalmoveout.footer), FOOTER_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalmoveout.footerText, true),
					"Need Help? Call us on 1300 584 628, or email us at support@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.footerText), LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpNumber), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpEmail), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalmoveout.linkFooterHelpNumber);
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpNumber), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalmoveout.linkFooterHelpEmail);
			softAssertion.assertEquals(getLabelCss(portalmoveout.linkFooterHelpEmail), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			String headerIntro = getDisplayedText(supplymoveout.lblSupplyDetailsIntro, true);
			supplymoveout = new SupplyDetailsMoveOut(driver, 0);
			softAssertion.assertFalse(isElementExists(supplymoveout.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"The below online form notifies Blue Oak Solution Pty Ltd (BlueOak) that you are moving out of a property that BlueOak supplies electricity and/or utilities. We'll process your disconnection request and send any further communication including a final bill to your new address. If you wish to use BlueOak as your electricity supplier for your new address, you can also do this through the form below",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyDetailsIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		}

		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
				"3 Account Contact", "4 Forwarding Address", "5 Additional Note", "6 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplymoveout.header), SECTION_HEADER_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(contactmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(forwardingmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(notesmoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemoveout.header), SECTION_HEADER_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplymoveout.moveOutDate, true),
				"Move Out Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplymoveout.labelInput, "Move Out Date (DD/MM/YYYY)"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplymoveout.iconMoveOutDate), DATEPICKER_ICON_VALID_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplymoveout.underlineMoveOutDate), UNDERLINE_NOT_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplymoveout.lblSupplyAddHeader, true), "Supply Address",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplymoveout.lblSupplyAddHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplymoveout.supplyAddSearch, true),
				"Please start typing supply address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplymoveout.labelInput, "Please start typing supply address"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(supplymoveout.iconSupplyAddSearch), GLOBE_ICON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplymoveout.underlineSupplyAddSearch),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplymoveout.next, true), "Next",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(supplymoveout.next), NEXT_BUTTON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Connection, then go to Move In and verify that the
	 * correct config is picked up
	 */
	@Test(priority = 10)
	public void verifyCssAndLangFileValidation05() {

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the portal configs we are using
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlConnection(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlConnection(), true);
			loadPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		ConnectionDetailsConnection connectiondetails = new ConnectionDetailsConnection(driver);

		// let's switch to the Connection Iframe
		// if it's embedded
		embeddedConnectionSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(connectiondetails.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// let's verify the header and introduction
		if (getPortalType().equals("standalone")) {
			String header = getDisplayedText(connectiondetails.lblMainHeader, true);
			String headerIntro = getDisplayedText(connectiondetails.lblConnectionIntro, true);
			softAssertion.assertEquals(header, "Selenium BlueBilling Connection Request",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblMainHeader), MAIN_HEADER_LABEL_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Please check our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(connectiondetails.linkLblConnectionIntro);
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL_HOVER2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			PortalConnection portalconnection = new PortalConnection(driver);
			softAssertion.assertEquals(getCssBackgrndColorProp(portalconnection.bodyBackground),
					BODY_BACKGROUND_DEFAULT, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(portalconnection.footer), FOOTER_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalconnection.footerText, true),
					"Need Help? Call us on 1300 584 628, or email us at support@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.footerText), LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpNumber), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpEmail), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalconnection.linkFooterHelpNumber);
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpNumber), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalconnection.linkFooterHelpEmail);
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpEmail), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			connectiondetails = new ConnectionDetailsConnection(driver, 0);
			String headerIntro = getDisplayedText(connectiondetails.lblConnectionIntro, true);
			softAssertion.assertFalse(isElementExists(connectiondetails.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Please check our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(connectiondetails.linkLblConnectionIntro);
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL_HOVER2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		}

		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("1 Connection Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
						"5 Project Manager", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.header), SECTION_HEADER_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSubmittedAsHeader, true), "I am a:",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSubmittedAsHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblPropertyOwner, true), "Property Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblManagingAgent, true),
				"Managing Agent (authorised by property owner)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblPropertyOwner), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblManagingAgent), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(connectiondetails.radioOuterPropertyOwner),
				RADIO_OUTER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(connectiondetails.radioInnerPropertyOwner),
				RADIO_INNER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(connectiondetails.radioOuterManagingAgent),
				RADIO_OUTER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(connectiondetails.radioInnerManagingAgent),
				RADIO_INNER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(connectiondetails.settlementDate, true),
				"Settlement Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(connectiondetails.labelInput, "Settlement Date (DD/MM/YYYY)"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(connectiondetails.iconSettlementDate), DATEPICKER_ICON_VALID_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineSettlementDate),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblConnectionAddHeader, true),
				"Connection Address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionAddHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(connectiondetails.connectionAddSearch, true),
				"Please start typing supply address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(connectiondetails.labelInput, "Please start typing supply address"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(connectiondetails.iconConnectionAddSearch), GLOBE_ICON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineConnectionAddSearch),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSiteAccessHzrdHeader, true),
				"Site Access Hazard", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSiteAccessHzrdHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSiteAccessHzrdIntro, true),
				"To ensure safety of our staff, please select any hazards that exists at the site, otherwise select no hazard exists",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSiteAccessHzrdIntro), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Dog")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Electric fence")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true,
						"Electrical safety issue")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Asbestos")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Other")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "No hazard exists")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Dog")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Electric fence")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true,
						"Electrical safety issue")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Asbestos")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Other")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "No hazard exists")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineSiteAccessHzrdOtherInput),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.next, true), "Next",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(connectiondetails.next), NEXT_BUTTON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT", "4 pOsTaL AdDrEsS",
							"5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// prefilled values should not be in error state
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

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "create Account Details",
					"3 Main Account Contact (Monkey Luffy's)", "4 Postal Address", "5 Direct Debit",
					"6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"Lease Commencement Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			expectedSectionMatIconColor = new ArrayList<>(Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
					MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}
	}

	/**
	 * verify the fix is still working for bug ticket BBPRTL-1987
	 * 
	 * Here we would access Move In, then go to Connection and verify that the
	 * correct config is picked up
	 */
	@Test(priority = 11)
	public void verifyCssAndLangFileValidation06() {

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the portal configs we are using
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "40\\", "agency_electricity_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(false);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(false, false);
			}

			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			if (getPortalType().equals("standalone")) {
				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded")) {
				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));

				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT", "4 pOsTaL AdDrEsS",
							"5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);

			// let's switch to the Move-In Iframe
			// if it's embedded
			embeddedMoveInSwitchFrame(1);
			// make sure that the elements are now displayed
			waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
					PORTAL_IMPLICIT_WAIT_TIMEOUT);

			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// should not be displayed since values were prefilled
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// prefilled values should not be in error state
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

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "i aM A:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "tEnAnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "oWnEr",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"pRoPeRtY MaNaGeR Or lEtTiNg aGeNt", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "sUpPlY AdDrEsS",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true),
					"sUpPlY CoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true),
					"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(supplydetailsmovein.linkLblSupplyConnectedIntro);
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
					"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true),
					"dIsCoNnEcTeD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyUnknown, true), "dO NoT KnOw",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDisconnected), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyUnknown), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "lIfE SuPpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"iS AnYoNe aT ThIs pRoPeRtY DePeNdEnT On eLeCtRiCiTy fOr lIfE SuPpOrT EqUiPmEnT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"iS EqUiPmEnT In uSe fOr mEdIcAl cOoLiNg pUrPoSe aT ThE SuPpLy aDdReSs?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "yEs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "nO",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_DASH);
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadStandaloneMoveInPortal(false);

				String header = getDisplayedText(supplydetailsmovein.lblMainHeader, true);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertEquals(header, "Selenium BlueBilling mOvE In rEqUeSt",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMainHeader), MAIN_HEADER_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.bodyBackground), BODY_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.footer), FOOTER_BACKGROUND_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getDisplayedText(portalmovein.footerText, true),
						"nEeD HeLp? CaLl uS On 1300 584 628 , oR EmAiL Us aT support@bluebilling.com.au",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.footerText), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpNumber);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpNumber), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(portalmovein.linkFooterHelpEmail);
				softAssertion.assertEquals(getLabelCss(portalmovein.linkFooterHelpEmail), LINK_LABEL_HOVER_CSTM,
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
						" config=agency_electricity_config.json", "&account_category=",
						AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate, "&complex_name='001 Complex's",
						"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=16",
						"&tenancy_street_number=6", "&tenancy_street_name=Mari", "&tenancy_street_type=",
						StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland", "&tenancy_postcode=4572",
						"&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
						AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
						"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
						"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
						"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
						getProp("test_dummy_email_lower_case"), "&extra_data=4012888888881881");
				accessPortal(urlPrefill, true);
				loadEmbeddedMoveInPortal(false, false);

				// let's switch to the Move-In Iframe
				// if it's embedded
				embeddedMoveInSwitchFrame(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);

				supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
				String headerIntro = getDisplayedText(supplydetailsmovein.lblSupplyDetailsIntro, true);
				softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblMainHeaderList),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(headerIntro,
						"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Terms and Conditions",
						assertionErrorMsg(getLineNumber()));
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyDetailsIntro), LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro), LINK_LABEL_CSTM,
						assertionErrorMsg(getLineNumber()));
				hoverToElementAction(supplydetailsmovein.linkLblSupplyDetailsIntro);
				softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyDetailsIntro),
						LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			}

			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are not editable
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Monkey Luffy's)", "4 Postal Address",
					"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is not displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify the CSS and display labels
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMovingInHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
					"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManager), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"Lease Commencement Date (DD-MM-YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "Supply Address",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyAddHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"Postcode", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			setImplicitWait(0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupHeader, true), "Life Support",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
					"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupQuestion, true),
					"Is anyone at this property dependent on electricity for life support equipment",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingQuestion, true),
					"Is equipment in use for Medical Cooling purpose at the Supply Address?",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingQuestion), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingYes, true), "Yes",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMedCoolingNo, true), "No",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingYes), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblMedCoolingNo), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlConnection(), true);
			loadPortal();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlConnection(), true);
			loadPortal();
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}

		ConnectionDetailsConnection connectiondetails = new ConnectionDetailsConnection(driver);

		// let's switch to the Connection Iframe
		// if it's embedded
		embeddedConnectionSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(connectiondetails.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// let's verify the header and introduction
		if (getPortalType().equals("standalone")) {
			String header = getDisplayedText(connectiondetails.lblMainHeader, true);
			String headerIntro = getDisplayedText(connectiondetails.lblConnectionIntro, true);
			softAssertion.assertEquals(header, "Selenium BlueBilling Connection Request",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblMainHeader), MAIN_HEADER_LABEL_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Please check our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(connectiondetails.linkLblConnectionIntro);
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL_HOVER2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			PortalConnection portalconnection = new PortalConnection(driver);
			softAssertion.assertEquals(getCssBackgrndColorProp(portalconnection.bodyBackground),
					BODY_BACKGROUND_DEFAULT, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(portalconnection.footer), FOOTER_BACKGROUND_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(portalconnection.footerText, true),
					"Need Help? Call us on 1300 584 628, or email us at support@bluebilling.com.au",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.footerText), LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpNumber), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpEmail), LINK_LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalconnection.linkFooterHelpNumber);
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpNumber), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(portalconnection.linkFooterHelpEmail);
			softAssertion.assertEquals(getLabelCss(portalconnection.linkFooterHelpEmail), LINK_LABEL_HOVER1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded")) {
			connectiondetails = new ConnectionDetailsConnection(driver, 0);
			String headerIntro = getDisplayedText(connectiondetails.lblConnectionIntro, true);
			softAssertion.assertFalse(isElementExists(connectiondetails.lblMainHeaderList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(headerIntro,
					"This agreement authorises Blue Oak Solutions Pty Ltd (BlueOak) to manage the supply of your electricity and/or utilities. For any help completing this form or to setup a direct debit facility please call our office on 1300 584 628 during business hours. If your residence does not currently have power, it may take up to three business days to re-connect your power. Please check our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionIntro), LABEL1_DEFAULT,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL2_DEFAULT,
					assertionErrorMsg(getLineNumber()));

			hoverToElementAction(connectiondetails.linkLblConnectionIntro);
			softAssertion.assertEquals(getLabelCss(connectiondetails.linkLblConnectionIntro), LINK_LABEL_HOVER2_DEFAULT,
					assertionErrorMsg(getLineNumber()));
		}

		// verify displayed sections
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("1 Connection Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
						"5 Project Manager", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT,
						MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT, MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.header), SECTION_HEADER_FOCUSED_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSubmittedAsHeader, true), "I am a:",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSubmittedAsHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblPropertyOwner, true), "Property Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblManagingAgent, true),
				"Managing Agent (authorised by property owner)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblPropertyOwner), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblManagingAgent), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(connectiondetails.radioOuterPropertyOwner),
				RADIO_OUTER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(connectiondetails.radioInnerPropertyOwner),
				RADIO_INNER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(connectiondetails.radioOuterManagingAgent),
				RADIO_OUTER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(connectiondetails.radioInnerManagingAgent),
				RADIO_INNER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(connectiondetails.settlementDate, true),
				"Settlement Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(connectiondetails.labelInput, "Settlement Date (DD/MM/YYYY)"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(connectiondetails.iconSettlementDate), DATEPICKER_ICON_VALID_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineSettlementDate),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblConnectionAddHeader, true),
				"Connection Address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblConnectionAddHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(connectiondetails.connectionAddSearch, true),
				"Please start typing supply address", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(connectiondetails.labelInput, "Please start typing supply address"),
				PLACEHOLDER_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(connectiondetails.iconConnectionAddSearch), GLOBE_ICON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineConnectionAddSearch),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSiteAccessHzrdHeader, true),
				"Site Access Hazard", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSiteAccessHzrdHeader), HEADER_LABEL_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.lblSiteAccessHzrdIntro, true),
				"To ensure safety of our staff, please select any hazards that exists at the site, otherwise select no hazard exists",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(connectiondetails.lblSiteAccessHzrdIntro), LABEL1_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Dog")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Electric fence")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true,
						"Electrical safety issue")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Asbestos")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Other")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "No hazard exists")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Dog")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Electric fence")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true,
						"Electrical safety issue")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Asbestos")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "Other")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(connectiondetails.siteAccessHzrdOptions, true, "No hazard exists")),
				CHECKBOX_INNER_UNTICKED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(connectiondetails.underlineSiteAccessHzrdOtherInput),
				UNDERLINE_NOT_FOCUSED_DEFAULT, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(connectiondetails.next, true), "Next",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(connectiondetails.next), NEXT_BUTTON_DEFAULT,
				assertionErrorMsg(getLineNumber()));
		// verify all assertion
		softAssertion.assertAll();
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would display if you populated the data until Acceptance page - verify also
	 * that the inactivity timeout would be displayed when a pop-op dialog is
	 * displayed
	 */
	@Test(priority = 12)
	public void verifyInactivityTimeout01() {

		long startTime1 = logNanoTimeStamp();

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadEmbeddedMoveInPortal(true, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout01 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// populate all sections until acceptance page
			populateAllSectionsResidential("verifyInactivityTimeout01", DATE_MONTH_YEAR_FORMAT_DASH, "-");
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(1800);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}

			// move the mouse and verify that the inactivity timeout was not displayed
			hoverToElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			toastmsgmovein = new ToastMsgMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(toastmsgmovein.toastLocList),
					"The inactivity timeout is still displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertion
			softAssertion.assertAll();

			scrollPageDown(800);
			clickElementAction(acceptancemovein.header);
			pauseSeleniumExecution(1000);
			// verify we are in the acceptance page now
			softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
					"We are not yet in the Acceptance details section");
			// verify all assertions
			softAssertion.assertAll();

			scrollPageDown(1800);
			clickElementAction(acceptancemovein.cancel);
			pauseSeleniumExecution(1000);
			// verify the Cancel message
			String cancelMsg = getDisplayedText(acceptancemovein.dialogContainerText, true);
			verifyTwoStringsAreEqual(cancelMsg,
					"Cancel Request and Remove Details Are you sure you like you like to cancel your submission? If you are having any issue completing this form or have any question, please do not hesitate to contact our support team",
					true);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}

			// verify that the inactivity timeout would be dismissed
			clickElementAction(acceptancemovein.noCancelRequest);
			pauseSeleniumExecution(1000);
			toastmsgmovein = new ToastMsgMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(toastmsgmovein.toastLocList),
					"The inactivity timeout is still displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertion
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would be dismissed if you hover the mouse - would be dismissed if you click
	 * on a button/field - woudl be dismissed if you type in on a field
	 */
	@Test(priority = 13)
	public void verifyInactivityTimeout02() {

		long startTime1 = logNanoTimeStamp();

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadEmbeddedMoveInPortal(true, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout02 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// populate all sections until acceptance page
			populateAllSectionsCommercial("verifyInactivityTimeout02", DATE_MONTH_YEAR_FORMAT_DASH, 2, 2, "Glenn",
					"O'brien");
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(1800);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}

			// move the mouse and verify that the inactivity timeout was not displayed
			hoverToElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);
			toastmsgmovein = new ToastMsgMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(toastmsgmovein.toastLocList),
					"The inactivity timeout is still displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertion
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);
			scrollPageUp(300);
			isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}

			clickElementAction(supplydetailsmovein.propManager);
			pauseSeleniumExecution(1000);
			toastmsgmovein = new ToastMsgMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(toastmsgmovein.toastLocList),
					"The inactivity timeout is still displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertion
			softAssertion.assertAll();

			scrollPageUp(300);
			isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}

			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			supplydetailsmovein.supplyAddComplexName.sendKeys("T");
			pauseSeleniumExecution(1000);
			toastmsgmovein = new ToastMsgMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(toastmsgmovein.toastLocList),
					"The inactivity timeout is still displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertion
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would display even if only Tenant option is ticked
	 */
	@Test(priority = 14)
	public void verifyInactivityTimeout03() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name());
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name());
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout03 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.tenant);
			assertTrue(isElementTicked(supplydetailsmovein.tenant, 0), "Tenant option is not ticked");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			assertTrue(isElementTicked(supplydetailsmovein.tenant, 0), "Tenant option is not ticked");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(300);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would display even you only clicked Next button without data
	 */
	@Test(priority = 15)
	public void verifyInactivityTimeout04() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout04 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageDown(100);
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			scrollPageDown(100);
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(300);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would display
			// since we only put the config in the URL prefill
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would display even you only clicked next header button without data
	 */
	@Test(priority = 16)
	public void verifyInactivityTimeout05() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&move_in_date=", moveInDate);
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&move_in_date=", moveInDate);
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout05 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageDown(150);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			scrollPageDown(150);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(300);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would not display when you only clicked the cannot find address
	 */
	@Test(priority = 17)
	public void verifyInactivityTimeout06() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "41\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill(null, null, null, null, null, null, AccountTypesEnum.SMALL_BUSINESS,
					AccountCategoryEnum.OWNER, null, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "account_category=",
					AccountCategoryEnum.OWNER.name(), "&config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "account_category=",
					AccountCategoryEnum.RUM.name(), "&config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout06 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.supplyAddSearch);
			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
			clickElementAction(supplydetailsmovein.supplyAddSearch);
			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
			clickElementAction(supplydetailsmovein.supplyAddSearch);
			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(300);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in manual");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would not display is there's no data entered
	 */
	@Test(priority = 18)
	public void verifyInactivityTimeout07() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "28\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill(null, null, null, null, null, null, null, null, null, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json");
			goToUrl(urlPrefill, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout07 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(300);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in manual");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would display
			// since we only put the config in the URL prefill
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/**
	 * Verify the inactivity timeout:
	 * 
	 * - would still display even if use_session_store == false and there's data
	 */
	@Test(priority = 19)
	public void verifyInactivityTimeout08() {

		long startTime1 = logNanoTimeStamp();

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "42\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "06\\", "custom_en.json");

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
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			// let's make sure the session storage is cleared
			// since it's supposed to be cleared in the previous test case
			clearLocalAndSessionStorage();

			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DB_DATE_FORMAT);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Apt.name(), "&tenancy_number=1328",
					"&tenancy_street_number=1328", "&tenancy_street_name=Gold Coast", "&tenancy_street_type=",
					StreetTypesEnum.HWY.getLabelText(), "&tenancy_suburb=Palm Beach", "&tenancy_postcode=4221",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=12345678", "&business_hour_phone=45678912", "&after_hour_phone=78912345",
					"&email_address=", getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			goToUrl(urlPrefill, true);
			loadEmbeddedMoveInPortal(true, true);
		} else {
			throw new SkipException(
					"Verify your test run parameters as it does not match any known combination, skipping test case");
		}
		long endTime1 = logNanoTimeStamp();
		logDebugMessage(concatStrings("verifyInactivityTimeout08 [Accessed Portal] execution time in seconds <",
				String.valueOf(getTotalExecutionInSec(startTime1, endTime1)), ">"));

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// populate all sections until acceptance page
			populateAllSectionsResidential("verifyInactivityTimeout01", DATE_MONTH_YEAR_FORMAT_DASH, "-");
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageUp(1800);

			// let's wait for the toast message to display
			// the value in the portal_config.json for idle_time is 120 seconds
			// while the value for timeout_warning is 30 seconds
			// so we will wait for the toast element to appear
			// setting a timeout of 94 seconds
			// added padding of 4 seconds
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 94, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			if (isElementDisp) {
				// let's get the toast message
				pauseSeleniumExecution(1000);
				toastmsgmovein = new ToastMsgMoveIn(driver, 0);
				String toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
				// verify the fix for bug ticket BBPRTL-2048
				// sometimes the actual seconds vs the expected seconds does not match
				// resulting for the test case to fail sometimes
				// so will use contains for now without asserting the seconds countdown
				verifyStringContains(true, toastMsg,
						"yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ");
				pauseSeleniumExecution(1000);
				int counter = 0;
				int maxCounter = 10;
				while (counter < maxCounter) {
					String assertMsgExp = "yOu hAvE BeEn iNaCtIvE FoR 90 sEcOnDs, fOr pRiVaCy rEaSoNs tHiS FoRm wIlL AuToMaTiCaLlY Be cLeArEd iN ";
					// let's instantiate the class to get again the elements
					toastmsgmovein = new ToastMsgMoveIn(driver, 0);
					toastMsg = getDisplayedText(toastmsgmovein.toastLoc, true);
					setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
					verifyStringContains(true, toastMsg, assertMsgExp);
					// let's pause for the timer
					pauseSeleniumExecution(1000);
					counter++;
				}
			} else {
				fail("The inactivity timeout message was not displayed");
			}
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			scrollPageUp(1800);

			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in thirdPartyPrefill");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			SupplyDetailsMoveIn supplydetailsmovein = new SupplyDetailsMoveIn(driver);
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);

			scrollPageUp(300);
			// verify that the inactivity timeout would not display
			boolean isElementDisp = waitForElement(toastmsgmovein.toastLoc, 100, PORTAL_IMPLICIT_WAIT_TIMEOUT);
			assertFalse(isElementDisp, "The inactivity timeout message was displayed in urlPrefill");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}
}
