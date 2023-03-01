package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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

import au.com.blueoak.portal.dev.move_in.MoveInDevBase;
import au.com.blueoak.portal.pageObjects.move_in.AcceptanceMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AccountDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalNoteMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ConcessionMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact09 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
	TradeWasteMoveIn tradewastemovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
	ConcessionMoveIn concessionmovein;
	DirectDebitMoveIn directdebitmovein;
	AdditionalNoteMoveIn additionalnotemovein;
	AcceptanceMoveIn acceptancemovein;
	AccessS3BucketWithVfs s3Access;

	/**
	 * Store the name of the class for logging
	 */
	String className;

	/** 
	 * */
	String tenantMoveInDate;

	/** 
	 * 
	 * */
	String dateOfBirthMain;

	/** 
	 * 
	 * */
	String dateOfBirthAddCont1;

	/** 
	 * 
	 * */
	String creditCardExpiry;

	/** 
	 * 
	 * */
	String creditCardExpiryMonth;

	/** 
	 * 
	 * */
	String creditCardExpiryYearFull;

	/**
	 * This is the source ID of the portal session
	 *
	 */
	String sourceID;

	/**
	 * The ID of the online request created
	 */
	String onlineReqId;

	@BeforeClass
	public void beforeClass() {

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);

		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "15\\", "portal_config.json");
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "15\\", "portal_config.json");
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(false, false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "15\\", "portal_config.json");
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("505", "Burwood", StreetTypesEnum.HWY, "Vermont South", AustralianStatesEnum.VIC,
					"3133", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "35\\", "elec_portal_config.json");
			// using the format DATE_MONTH_YEAR_FORMAT_DASH
			String moveInDate = concatStrings("12-", String.valueOf(getCurrentMonth(false)), "-",
					String.valueOf(getCurrentYear()));
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=elec_portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&tenancy_street_number=505", "&tenancy_street_name=Burwood", "&tenancy_street_type=",
					StreetTypesEnum.HWY.name().toLowerCase(), "&tenancy_suburb=Vermont South", "&tenancy_postcode=3133",
					"&tenancy_state=", AustralianStatesEnum.VIC.name(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&extra_data=4012888888881881");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "35\\", "elec_portal_config.json");
			// using the format DATE_MONTH_YEAR_FORMAT_DASH
			String moveInDate = concatStrings("12-", String.valueOf(getCurrentMonth(false)), "-",
					String.valueOf(getCurrentYear()));
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=elec_portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&tenancy_street_number=505", "&tenancy_street_name=Burwood", "&tenancy_street_type=",
					StreetTypesEnum.HWY.name().toLowerCase(), "&tenancy_suburb=Vermont South", "&tenancy_postcode=3133",
					"&tenancy_state=", AustralianStatesEnum.VIC.name(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", "&business_trading_name=",
					"&contact_first_name=", "&contact_last_name=", "&mobile_number=", "&business_hour_phone=",
					"&after_hour_phone=", "&email_address=", "&extra_data=4012888888881881");
			accessPortal(urlPrefill, true);
			loadEmbeddedMoveInPortal(false, false);
		} else {
			throw new SkipException(concatStrings(
					"Verify your test run parameters as it does not match any known combination, skipping test class ",
					this.className));
		}
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
		concessionmovein = new ConcessionMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
	}

	/**
	 * 
	 *  */
	@Test(priority = 1)
	public void verifySupplyDetails() {

		// let's switch to the Move-In Iframe
		// if it's embedded
		embeddedMoveInSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify the radio buttons are not selected
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false)),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-2011
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false)),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
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
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedQuestionList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyConnectedList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyDisconnectedList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyUnknownList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingNoList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the expected sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Trade Waste", "4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.owner);
		assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false)),
				"Move In Date Owner is not blank/empty");
		String future5Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 5, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(future5Days, Keys.TAB);
		// initial click the dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the sections displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Holiday Letting/Rental Company Details", "5 Direct Debit",
					"6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the sections displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Trade Waste", "4 Main Account Contact", "5 Holiday Letting/Rental Company Details",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.propManager);
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.whoIsResponsiblePropMan, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.whoIsResponsibleOwner, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the displayed sections
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Tenant/Owner Account Contact",
							"4 Manager/Agent Company Details", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Trade Waste", "4 Tenant/Owner Account Contact", "5 Manager/Agent Company Details",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		String future6Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 6, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(future6Days, Keys.TAB);
		// let's click an already clicked radio to collapse calendar
		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// go back to owner
			clickElementAction(supplydetailsmovein.owner);
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false), future5Days,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.whoIsResponsiblePropMan, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.whoIsResponsibleOwner, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the sections displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Holiday Letting/Rental Company Details", "5 Direct Debit",
					"6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.tenant);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// go back to owner
			clickElementAction(supplydetailsmovein.owner);
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false), future5Days,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.whoIsResponsiblePropMan, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.whoIsResponsibleOwner, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the sections displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Trade Waste", "4 Main Account Contact", "5 Holiday Letting/Rental Company Details",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.tenant);
			// clear the value on the prefill
			clearDateField(supplydetailsmovein.moveInDateTenant);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's put a valid lease commencement date as 4 days from future
		String future4Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 4, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future4Days);
		this.tenantMoveInDate = future4Days;
		// click tenant again to ensure that calendar is collapsed
		clickElementAction(supplydetailsmovein.tenant);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddStreetNum.sendKeys("505");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Burwood");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Highway", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Vermont South");
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("3133");
		}

		// verify fields are populated correctly
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "505", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
		supplydetailsmovein.supplyAddTenancyType.sendKeys("Site", Keys.TAB);
		supplydetailsmovein.supplyAddTenancyNum.sendKeys("1000");

		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Intermittent Peritoneal Dialysis Machine"));
		// verify that the upload section is displayed
		// and not in error state
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.dragAndDropArea, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's click the life support again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(supplydetailsmovein.dragAndDropArea);
		// upload life support and medical cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf", "typing jim carrey.gif");
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
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
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
				"We're not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 2, dependsOnMethods = { "verifySupplyDetails" })
	public void verifyAccountDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn3"), Keys.TAB);
		accountdetailsmovein.tradingName.sendKeys("JaH Trading's");
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");

		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				"We are not yet in the Trade Waste section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 *  */
	@Test(priority = 3, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyTradeWaste() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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

		clickElementAction(tradewastemovein.tradeWasteDischargeYes);
		pauseSeleniumExecution(500);
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

		clickElementAction(tradewastemovein.tradeWasteEquipNo);
		clickElementAction(tradewastemovein.businessActivity);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(tradewastemovein.businessActivityDiv, 3);
		chooseFromList(tradewastemovein.businessActivityDiv, 2);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(tradewastemovein.businessActivity, true);
		softAssertion.assertEquals(typeChosen, "Retail motor vehicle", assertionErrorMsg(getLineNumber()));

		tradewastemovein.maxDischargeVolume.sendKeys("0123...0096457", Keys.TAB);
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"));

		// upload trade waste files
		uploadTradeWasteFiles(ARTIFACTS_DIR, "Smaller file tiff file.tiff", "typing jim carrey.gif");
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
		String dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		// verify the files that were uploaded were only 2
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload Smaller file tiff file .tiff 0.6 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 4, dependsOnMethods = { "verifyTradeWaste" })
	public void verifyMainContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		// verify the notification introduction is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationHeader, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		// verify the disabled checkbox but set as default ticked in portal config
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.billsPostalList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.acctnotifAndRemindersEmailList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.marketingComSMSList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.firstName.sendKeys("Koushou ");
		mainaccountcontactmovein.lastName.sendKeys("Shinogi ");

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// did a loop to fix an issue where the 1st click
		// did not display the 1st additional contact section
		// only happens when use_session_store == false
		try {
			int counter = 0;
			int maxRetry = 3;
			additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
			boolean isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
			while (!isDisplayed && counter < maxRetry) {
				clickElementAction(mainaccountcontactmovein.addAnotherContact);
				pauseSeleniumExecution(1000);
				isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
				counter++;
			}
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contact section"); // verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		// verify the notification introduction is not displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationHeader, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the remove additional contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ContactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		// verify the disabled checkbox but set as default ticked in portal config
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1BillsPostalList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AcctnotifAndRemindersEmailList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1MarketingComSMSList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1FirstName.sendKeys("Kureha ");
		additionalcontactmovein.addCont1LastName.sendKeys("Shinogi ");

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact" })
	public void verifyDirectDebitDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify that values are not ticked by default
		softAssertion.assertFalse(isElementTicked(directdebitmovein.bankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.creditCard, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.noDirectDebit, 0),
				assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.noDirectDebit, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the fix for bug ticket BBPRTL-1966
		directdebitmovein.bankAccountName.sendKeys("123~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		directdebitmovein.accountBSB.sendKeys("00");
		directdebitmovein.accountNumber.sendKeys("111", Keys.TAB);

		clickElementAction(directdebitmovein.creditCard);
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		assertEquals(loadingMsg, "Creating secure area for credit card entry...",
				"Credit Card initialization progress bar text is not correct");
		moveInDirectDebitCCProgBarLoad();

		// enter valid fields for credit card name, credit card number and expiry
		switchToMWIframe();
		directdebitmovein.creditCardName.sendKeys("Pepper Potts-Stark");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_14"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 3;
		String monthStr = Integer.toString(month);
		String expYrStrFull = Integer.toString(expYr);
		String expYrStr = getString(expYrStrFull, 2, 4);
		monthStr = concatStrings("0", monthStr);
		String expiry = concatStrings(monthStr, "/", expYrStr);
		directdebitmovein.creditCardExpiry.sendKeys(expiry, Keys.TAB);
		this.creditCardExpiry = expiry;
		this.creditCardExpiryMonth = monthStr;
		this.creditCardExpiryYearFull = expYrStrFull;
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
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify field not displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0), "We are not yet in the Direct Debit section");

		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Pepper Potts-Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_15"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify field not displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account declaration text
		declaration = getDisplayedText(directdebitmovein.lblBankAccountDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Dishonor Fees: A dishonor fee of $15.00 (inc GST) applied for failed direct debt. the bank payment will be based on the bills issued according to each bill cycle. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account authorisation text
		authorisation = getDisplayedText(directdebitmovein.lblBankAccountAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise SR Global Solutions Pty Ltd ACN 132 951 172 (\"Merchant Warrior\"), Direct Debit User ID Number 397351, to debit my/our account on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the Bulk Electronic Clearing System (BECS) as per the service agreement provided. Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the Direct Debit section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccountName, 0),
				"We are no longer in the Direct Debit section");
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account declaration text
		declaration = getDisplayedText(directdebitmovein.lblBankAccountDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Dishonor Fees: A dishonor fee of $15.00 (inc GST) applied for failed direct debt. the bank payment will be based on the bills issued according to each bill cycle. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account authorisation text
		authorisation = getDisplayedText(directdebitmovein.lblBankAccountAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise SR Global Solutions Pty Ltd ACN 132 951 172 (\"Merchant Warrior\"), Direct Debit User ID Number 397351, to debit my/our account on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the Bulk Electronic Clearing System (BECS) as per the service agreement provided. Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// go back to the credit card option
		// details should not be removed
		clickElementAction(directdebitmovein.creditCard);
		actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Pepper Potts-Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_15"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify field not displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAcceptanceDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify each section
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Site 1000, 505 Burwood Highway Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Intermittent Peritoneal Dialysis Machine Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_abn3_abn4"), " (JaH Trading's) ABN/ACN ",
						getProp("test_data_valid_abn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradeWaste,
				"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Retail motor vehicle'",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo,
				"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 123.01 Litres Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' Uploaded 2 site plans",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Koushou Shinogi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, "Kureha Shinogi", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Pepper Potts-Stark Card: ending ",
						getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - concession section
		// - property manager/ letting agent
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// go to accounts
		clickExactLinkNameFromElement(acceptancemovein.accountDetailsRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		clickElementAction(accountdetailsmovein.residential);
		scrollPageDown(900);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are redirected into the Main Account Contact section immediately
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0), assertionErrorMsg(getLineNumber()));
		// verify that radio button not ticked by default
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.medicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		// verify the disabled checkbox but set as default ticked in portal config
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.billsPostalList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.acctnotifAndRemindersEmailList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.marketingComSMSList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.datePickerDateOfBirth);
		// click the label in the background to choose a year from the calendar
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		pauseSeleniumExecution(500);
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		pauseSeleniumExecution(500);
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a month from the calendar
		clickElementAction(mainaccountcontactmovein.linkLblNotificationIntro);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a date from the calendar
		clickElementAction(mainaccountcontactmovein.lblBillsNotif);
		// click to ensure that calendar is collapsed
		clickElementAction(mainaccountcontactmovein.firstName);
		// verify we have a value on the Date of Birth
		String dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		this.dateOfBirthMain = dateOfBirth;
		// verify that Date of Birth is not empty
		softAssertion.assertFalse(StringUtils.isBlank(this.dateOfBirthMain), assertionErrorMsg(getLineNumber()));
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.driversLicence);
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("Abcdef");
		mainaccountcontactmovein.driversLicenceState.sendKeys("New South Wales");
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		scrollPageDown(600);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are automatically redirected into the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the remove additional contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify Provide None option not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ProvideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ContactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		// verify the disabled checkbox but set as default ticked in portal config
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1BillsPostalList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AcctnotifAndRemindersEmailList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1MarketingComSMSList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1DateOfBirth);
		pauseSeleniumExecution(1000);
		// click the label in the background to choose a year from the calendar
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		pauseSeleniumExecution(500);
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		pauseSeleniumExecution(500);
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a month from the calendar
		clickElementAction(additionalcontactmovein.addCont1LinkLblNotificationIntro);
		pauseSeleniumExecution(500);
		// click the label in the background to choose a date from the calendar
		clickElementAction(additionalcontactmovein.addCont1LblBillsNotif);
		// click to ensure that calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1FirstName);
		// verify we have a value on the Date of Birth
		this.dateOfBirthAddCont1 = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		;
		// verify Date of Birth is not empty
		softAssertion.assertFalse(StringUtils.isBlank(this.dateOfBirthAddCont1), assertionErrorMsg(getLineNumber()));
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Passport);
		additionalcontactmovein.addCont1PassportNumber.sendKeys("Qwerty");
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Philippines");
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		scrollPageDown(600);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are redirected into the concession section immediately
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(concessionmovein.addConcessionYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(concessionmovein.addConcessionNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.addConcessionYes);
		concessionmovein.cardHolderName.sendKeys("Bruce Wayne");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 4);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 4);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumber.sendKeys("1212009840");
		// let's upload concession card details
		uploadConcessionFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		// just added extra time to make sure files are uploaded
		// instead of using the usual wait time
		waitForFilesToBeUploaded(90000);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Acceptance ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		String dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		// verify only 1 file was uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(500);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify each section again
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Site 1000, 505 Burwood Highway Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Intermittent Peritoneal Dialysis Machine Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				mainContact, concatStrings("Main Account Contact update Koushou Shinogi Birthdate: ",
						this.dateOfBirthMain, " Personal Id: Driver Licence (Abcdef, New South Wales)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Kureha Shinogi Birthdate: ",
				this.dateOfBirthAddCont1, " Personal Id: Passport (Qwerty, Philippines)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Bruce Wayne DVA Gold Card 1212009840 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Pepper Potts-Stark Card: ending ",
						getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - trade waste
		// - discharge info
		// - property manager/ letting agent
		// - postal address
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 8, dependsOnMethods = { "verifyAcceptanceDetails" })
	public void verifySessionDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-1488
			// let's confirm the keys in the session storage
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the session storage are not empty
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the fix for ticket BBPRTL-1843
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the dev 3rd party prefill page
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);

			storage = new BrowserLocalSessionStorage(driver);
			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

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
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 18, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			this.sourceID = sessionSourceId;

			// verify the values for Supply Details
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, false);
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.tenantMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Site", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1000", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "505", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertTrue(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Intermittent Peritoneal Dialysis Machine"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Chronic Positive Airways Pressure Respirator"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Crigler Najjar Syndrome Phototherapy Equipment"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Ventilator for Life Support"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
					assertionErrorMsg(getLineNumber()));
			if (concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea)
					.contains("requirement Sprin't 02 Story 'Board.pdf")) {
				softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
						"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully typing jim carrey.gif .image/gif 0.5 MB File uploaded successfully",
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
						"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement typing jim carrey.gif .image/gif 0.5 MB File uploaded successfully Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
						assertionErrorMsg(getLineNumber()));
			}
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Account Details section
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainDoB = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, false);
			String driversNum = getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, false);
			String driversState = getDisplayedValue(mainaccountcontactmovein.driversLicenceState, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Koushou", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Shinogi", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainDoB, this.dateOfBirthMain, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(driversNum, "Abcdef", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(driversState, "New South Wales", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create Supply Details", "create Account Details",
							"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
							"5 Concession", "create Direct Debit", "create Additional Note", "create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersPostal);
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
					"5 Postal Address", "6 Concession", "create Direct Debit", "create Additional Note",
					"create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersPostal);
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
					"5 Concession", "create Direct Debit", "create Additional Note", "create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Contact section
			String addCont1FName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
			String addCont1LName = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
			String addCont1DoB = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, false);
			String passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false);
			String passportCountry = getDisplayedValue(additionalcontactmovein.addCont1PassportCountry, false);
			String addCont1mainEmail = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false);
			String addCont1mainMob = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
			String addCont1mainBus = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
			String addCont1mainAfter = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
			softAssertion.assertEquals(addCont1FName, "Kureha", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1LName, "Shinogi", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1DoB, this.dateOfBirthAddCont1, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(passportNum, "Qwerty", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(passportCountry, "Philippines", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainAfter), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalcontactmovein.addCont1Next);
			pauseSeleniumExecution(1000);

			// verify Concession section
			String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
			String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
			String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
			dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
			String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Bruce Wayne", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "DVA Gold Card", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, "1212009840", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(concessionmovein.next);
			pauseSeleniumExecution(1000);

			// verify Direct Debit Details
			String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
			String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
			String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
			softAssertion.assertTrue(isElementTicked(directdebitmovein.creditCard, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardName, "Pepper Potts-Stark", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardNum, getProp("test_data_15"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardExp, this.creditCardExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(directdebitmovein.changeCreditCardDetails, 0),
					assertionErrorMsg(getLineNumber()));
			directdebitmovein = new DirectDebitMoveIn(driver, 0);
			// verify field not displayed
			softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.next);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
			String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
			String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
			String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
			String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
			String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
			String concession = getDisplayedText(acceptancemovein.concessionRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update Site 1000, 505 Burwood Highway Vermont South, Victoria, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life support required using the following equipment Oxygen Concentrator Intermittent Peritoneal Dialysis Machine Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					concatStrings("Main Account Contact update Koushou Shinogi Birthdate: ", this.dateOfBirthMain,
							" Personal Id: Driver Licence (Abcdef, New South Wales)"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertEquals(addContact1Data,
							concatStrings("Kureha Shinogi Birthdate: ", this.dateOfBirthAddCont1,
									" Personal Id: Passport (Qwerty, Philippines)"),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					"Concession update Bruce Wayne DVA Gold Card 1212009840 Concession Card Uploaded",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit,
					concatStrings("Direct Debit update Credit Card Name On Card: Pepper Potts-Stark Card: ending ",
							getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - trade waste
			// - discharge info
			// - property manager/ letting agent
			// - postal address
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the fix for ticket BBPRTL-1843
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 28, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				// we will mimic a refresh by accessing the standalone move in page
				// since the current url is the url prefill
				// since there's still values in the session keys
				// it should populate the data correctly
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				// we will mimic a refresh by accessing the embedded move in page
				// since the current url is the url prefill
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 28, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			this.sourceID = sessionSourceId;

			// verify the values for Supply Details
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, false);
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.tenantMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Site", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1000", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "505", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertTrue(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Intermittent Peritoneal Dialysis Machine"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Chronic Positive Airways Pressure Respirator"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Crigler Najjar Syndrome Phototherapy Equipment"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Ventilator for Life Support"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
					assertionErrorMsg(getLineNumber()));
			if (concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea)
					.contains("requirement Sprin't 02 Story 'Board.pdf")) {
				softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
						"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully typing jim carrey.gif .image/gif 0.5 MB File uploaded successfully",
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
						"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement typing jim carrey.gif .image/gif 0.5 MB File uploaded successfully Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
						assertionErrorMsg(getLineNumber()));
			}
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Account Details section
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainDoB = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, false);
			String driversNum = getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, false);
			String driversState = getDisplayedValue(mainaccountcontactmovein.driversLicenceState, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Koushou", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Shinogi", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainDoB, this.dateOfBirthMain, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(driversNum, "Abcdef", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(driversState, "New South Wales", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create Supply Details", "create Account Details",
							"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
							"5 Concession", "create Direct Debit", "create Additional Note", "create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersPostal);
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
					"5 Postal Address", "6 Concession", "create Direct Debit", "create Additional Note",
					"create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersPostal);
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			actualSectionNames = getAllSectionNames(true);
			expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create Account Details",
					"3 Main Account Contact (Koushou Shinogi)", "create Additional Contact (Kureha Shinogi)",
					"5 Concession", "create Direct Debit", "create Additional Note", "create Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Contact section
			String addCont1FName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
			String addCont1LName = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
			String addCont1DoB = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, false);
			String passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false);
			String passportCountry = getDisplayedValue(additionalcontactmovein.addCont1PassportCountry, false);
			String addCont1mainEmail = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false);
			String addCont1mainMob = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
			String addCont1mainBus = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
			String addCont1mainAfter = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
			softAssertion.assertEquals(addCont1FName, "Kureha", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1LName, "Shinogi", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1DoB, this.dateOfBirthAddCont1, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(passportNum, "Qwerty", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(passportCountry, "Philippines", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainAfter), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalcontactmovein.addCont1Next);
			pauseSeleniumExecution(1000);

			// verify Concession section
			String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
			String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
			String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
			dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
			String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Bruce Wayne", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "DVA Gold Card", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, "1212009840", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(concessionmovein.next);
			pauseSeleniumExecution(1000);

			// verify Direct Debit Details
			String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
			String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
			String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
			softAssertion.assertTrue(isElementTicked(directdebitmovein.creditCard, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardName, "Pepper Potts-Stark", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardNum, getProp("test_data_15"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(actCreditCardExp, this.creditCardExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(directdebitmovein.changeCreditCardDetails, 0),
					assertionErrorMsg(getLineNumber()));
			directdebitmovein = new DirectDebitMoveIn(driver, 0);
			// verify field not displayed
			softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.next);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
			String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
			String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
			String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
			String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
			String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
			String concession = getDisplayedText(acceptancemovein.concessionRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update Site 1000, 505 Burwood Highway Vermont South, Victoria, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life support required using the following equipment Oxygen Concentrator Intermittent Peritoneal Dialysis Machine Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					concatStrings("Main Account Contact update Koushou Shinogi Birthdate: ", this.dateOfBirthMain,
							" Personal Id: Driver Licence (Abcdef, New South Wales)"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertEquals(addContact1Data,
							concatStrings("Kureha Shinogi Birthdate: ", this.dateOfBirthAddCont1,
									" Personal Id: Passport (Qwerty, Philippines)"),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					"Concession update Bruce Wayne DVA Gold Card 1212009840 Concession Card Uploaded",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit,
					concatStrings("Direct Debit update Credit Card Name On Card: Pepper Potts-Stark Card: ending ",
							getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - trade waste
			// - discharge info
			// - property manager/ letting agent
			// - postal address
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 29, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);
		// verify checkboxes not ticked
		softAssertion.assertFalse(isElementTicked(acceptancemovein.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
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

		// verify checkboxes ticked
		softAssertion.assertTrue(isElementTicked(acceptancemovein.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(acceptancemovein.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(acceptancemovein.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// add the property files before submitting the request
		addProp("ResiNewContact09_tenantMoveInDate", this.tenantMoveInDate);
		addProp("ResiNewContact09_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiNewContact09_dateOfBirthAddCont1", this.dateOfBirthAddCont1);
		addProp("ResiNewContact09_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiNewContact09_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiNewContact09_sourceID", this.sourceID);
		addProp("ResiNewContact09_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));

		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			logDebugMessage(concatStrings(
					"Will be deleting all attachments in the presign upload bucket before submitting the request for test class ",
					this.className));
			// delete all files in the presign upload bucket before submitting the request
			s3Access.deleteAllObjectsInDevPresignUploadBucket();
		}

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
	}

}
