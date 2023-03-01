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
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_in.MoveInDevBase;
import au.com.blueoak.portal.pageObjects.move_in.AcceptanceMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AccountDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.AdditionalNoteMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ManagerHolidayLettingMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class SmallBusExistingContact03 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	TradeWasteMoveIn tradewastemovein;
	AccountDetailsMoveIn accountdetailsmovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
	PostalAddressMoveIn postaladdressmovein;
	ManagerHolidayLettingMoveIn managerholidaylettingmovein;
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
	String initialDate3rdPartyPref;

	/**
	 * This is the Move In Date
	 */
	String ownerMoveInDate;

	/**
	 * This is the Move In Date for CRM
	 */
	String ownerMoveInDateCRM;

	/**
	 * This is the Settlement Date
	 */
	String ownerSettlementDate;

	/**
	 * This is the Settlement Date for CRM
	 */
	String ownerSettlementDateCRM;

	/**
	 * This is the Discharge Start Date for Trade Waste
	 */
	String tradeWasteDischargeStartDate;

	/**
	 * This is the Discharge Start Date for Trade Waste
	 */
	String tradeWasteDischargeStartDateCRM;

	/** 
	 * 
	 * */
	String mainContactFirstName;

	/** 
	 * 
	 * */
	String mainContactLastName;

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

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "03\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "04\\", "custom_en.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(false, false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("75", "Davis", StreetTypesEnum.st, "NORWOOD", AustralianStatesEnum.vic, "5067",
					AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=", "&account_category=",
					"&move_in_date=", "&tenancy_type=", "&tenancy_number=", "&tenancy_street_number=",
					"&tenancy_street_name=", "&tenancy_street_type=", "&tenancy_suburb=", "&tenancy_postcode=",
					"&tenancy_state=", "&account_type=", "&business_number=1234567890", "&business_trading_name=",
					"&contact_first_name=", "&contact_last_name=", "&mobile_number=", "&business_hour_phone=",
					"&after_hour_phone=", "&email_address=",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=", "&account_category=",
					"&move_in_date=", "&tenancy_type=", "&tenancy_number=", "&tenancy_street_number=",
					"&tenancy_street_name=", "&tenancy_street_type=", "&tenancy_suburb=", "&tenancy_postcode=",
					"&tenancy_state=", "&account_type=", "&business_number=1234567890", "&business_trading_name=",
					"&contact_first_name=", "&contact_last_name=", "&mobile_number=", "&business_hour_phone=",
					"&after_hour_phone=", "&email_address=",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
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
		tradewastemovein = new TradeWasteMoveIn(driver);
		accountdetailsmovein = new AccountDetailsMoveIn(driver);
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver);
		additionalcontactmovein = new AdditionalContactMoveIn(driver);
		postaladdressmovein = new PostalAddressMoveIn(driver);
		managerholidaylettingmovein = new ManagerHolidayLettingMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
	}

	/** 
	 * 
	 * */
	@Test(priority = 1)
	public void verifySupplyDetails(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		// if it's embedded
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}
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
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields that are not editable
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields are empty
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
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(stNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(stName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(stType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(city), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(state), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(postcode), assertionErrorMsg(getLineNumber()));
			// verify the fields that are not editable
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
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
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
		// verify Supply Connected fields and labels are not displayed
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
		// verify that the life support introduction is not displayed
		String lifeSupIntro = getDisplayedText(supplydetailsmovein.lblLifeSupIntro, false);
		softAssertion.assertTrue(StringUtils.isBlank(lifeSupIntro), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify medical cooling not displayed
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingNoList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify that No Postal Address section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 aCcOuNt dEtAiLs",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify that No Postal Address section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 aCcOuNt dEtAiLs",
					"3 tRaDe wAsTe", "4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify that No Postal Address section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			// verify the fix for bug ticket BBPRTL-1988
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 aCcOuNt dEtAiLs",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.owner);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not ticked
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
			// verify the fields are now editable
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
			// verify the fields are now editable
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's open a new tab then correct the street type and state
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			openNewTabAndSwitchToIt();
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("75", "Davis", StreetTypesEnum.CDS, "NORWOOD", AustralianStatesEnum.SA, "5067",
					AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
			// let's switch to the Move-In Iframe
			if (getPortalType().equals("embedded")) {
				// let's switch to the Move-In Iframe
				switchToMoveInEmbeddedIframe(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}

			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields that are not editable
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
			// verify the immediately displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 aCcOuNt dEtAiLs",
					"3 tRaDe wAsTe", "4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDateOwner);
		}

		String future20Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 20, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future20D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 20, DATE_MONTH_YEAR_FORMAT_DASH);
		String future19Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 19, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future19D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 19, DATE_MONTH_YEAR_FORMAT_DASH);
		this.ownerMoveInDate = future20Days;
		this.ownerMoveInDateCRM = future20D;
		this.ownerSettlementDate = future19Days;
		this.ownerSettlementDateCRM = future19D;
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(future20Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleNo);

		clickElementAction(supplydetailsmovein.ownerSettleNo);
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.settlementDateOwner, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.settlementDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(future19Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);

		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
		// verify the field 'Who is responsible for paying the account' is not displayed
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			supplydetailsmovein.supplyAddComplexName.sendKeys("9 Garden Walk");
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Villa", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("Happy Valley Retirement Village");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("75");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Davis");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Cul-de-sac", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("NORWOOD");
			supplydetailsmovein.supplyAddState.sendKeys("South Australia", Keys.TAB);
			pauseSeleniumExecution(600);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// verify that the spinner is not displayed since public holiday checking
			// is disabled in the portal config
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddPostcode.sendKeys("5067");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Villa", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("Happy Valley Retirement Village");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

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
		// verify also not ticked
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
								false, "Intermittent Peritoneal Dialysis Machine"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"), 0),
				assertionErrorMsg(getLineNumber()));
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
		softAssertion
				.assertFalse(isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
						false, "Ventilator for Life Support"), 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify no attachment
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt ",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		// let's click the life support again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// upload life support
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Smaller tif file.tif", "typing jim carrey.gif",
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
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Smaller tif file .tif 0.8 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(supplydetailsmovein.next)) {
				scrollIntoView(supplydetailsmovein.next);
			}
		}
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 2, dependsOnMethods = { "verifySupplyDetails" })
	public void verifyAccountDetails(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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

			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify fields are in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.commercial);
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.companyName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS and lang files
			softAssertion.assertEquals(getDisplayedPlaceholder(accountdetailsmovein.abnOrAcn, true),
					"cOmPaNy aBn oR AcN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(accountdetailsmovein.companyName, true),
					"cOmPaNy/eNtItY NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(accountdetailsmovein.tradingName, true), "tRaDiNg nAmE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy aBn oR AcN"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy/eNtItY NaMe"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "tRaDiNg nAmE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineCompanyName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify radio buttons are editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.companyName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
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
			// verify radio buttons are editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.commercial);
			// verify fields that are in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.companyName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			String abnAcn = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			String companyName = getDisplayedValue(accountdetailsmovein.companyName, false);
			String tradingName = getDisplayedValue(accountdetailsmovein.tradingName, false);
			softAssertion.assertEquals(abnAcn, "1234567890", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(companyName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tradingName), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.abnOrAcn);
			deleteAllTextFromField();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "2 aCcOuNt dEtAiLs",
				"3 tRaDe wAsTe", "4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
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

		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				"We are no longer in the Account Details section");

		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy aBn oR AcN"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy/eNtItY NaMe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "tRaDiNg nAmE"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineCompanyName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnRequired, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintCompNameRequired, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnRequired), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintCompNameRequired), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation still works
		// when you entered invalid values for abn
		accountdetailsmovein.abnOrAcn.sendKeys("44 533 556 209");
		accountdetailsmovein.companyName.sendKeys("123 456 7890");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				"We are no longer in the Account Details section");

		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnInvalid, true), "iNvAlId aBn/aCn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintCompNameInvalid, true),
				"tHe eNtItY NaMe sHoUlD Be aLpHaNuMeRiC", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.floaterLblAbnOrAcn, true),
				"cOmPaNy aBn oR AcN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.floaterLblCompanyName, true),
				"cOmPaNy/eNtItY NaMe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnInvalid), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintCompNameInvalid), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblAbnOrAcn), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblCompanyName), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.abnOrAcn), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.companyName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineCompanyName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();
		clickElementAction(accountdetailsmovein.companyName);
		deleteAllTextFromField();

		// verify the validation still works
		// when you entered invalid values for acn
		accountdetailsmovein.abnOrAcn.sendKeys("079509739", Keys.TAB);
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnInvalid, true), "iNvAlId aBn/aCn",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();

		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn4"), Keys.TAB);
		accountdetailsmovein.companyName.sendKeys(getProp("test_data_valid_company_name_abn3_abn4"));
		// go to residential then go back to commercial
		clickElementAction(accountdetailsmovein.residential);
		pauseSeleniumExecution(500);
		clickElementAction(accountdetailsmovein.commercial);
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmovein.abnOrAcn);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify hint errors not displayed
		accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.hintAbnAcnInvalid, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		accountdetailsmovein.abnOrAcn.sendKeys("1");
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnInvalid, true), "iNvAlId aBn/aCn",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.abnOrAcn);
		// delete the number 1 that was added
		accountdetailsmovein.abnOrAcn.sendKeys(Keys.BACK_SPACE);
		clickElementAction(accountdetailsmovein.tradingName);
		waitForCssToRender();
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblAbnOrAcn), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblCompanyName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.abnOrAcn), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.companyName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineCompanyName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the section header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
		softAssertion.assertEquals(header, "2 aCcOuNt dEtAiLs", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are now in the next section
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				"We are not yet in the Trade Waste section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * This is for ticket BBPRTL-774
	 * 
	 * - verify the fields are not in error state initially - verify the initial
	 * fields that are hidden - verify that clicking the header of the next section
	 * validates fields - verify clicking Previous would redirect the user into the
	 * Supply Details section - verify the required fields - verify users can still
	 * go back to the previous section once the hidden fields are displayed - verify
	 * that the Trade Waste Equipments are initially not in error state - choose all
	 * Trade Waste Equipments - verify the values in the Business Activity - verify
	 * the validation on the Other text field for Trade Waste Equipment - verify
	 * validations for Maximum instantaneous flow rate - verify the validations for
	 * Maximum daily discharge volume - choose all Discharge Days - enter values for
	 * Discharge Hours Start Hour and Minutes - verify deleting from the Discharge
	 * Hours Start Hour would also clear the Minutes - verify the Discharge Hours
	 * Start Hour increment button - verify the Discharge Hours Start Minutes
	 * increment button - enter values for Discharge Hours End Hour and Minutes -
	 * verify deleting from the Discharge Hours End Hour would also clear the
	 * Minutes - verify the Discharge Hours End Hour increment button - verify the
	 * Discharge Hours End Minutes increment button - verify we can upload files -
	 * verify the section header
	 * 
	 */
	@Test(priority = 3, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyTradeWaste(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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
		// verify CSS and lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create aCcOuNt dEtAiLs", "3 tRaDe wAsTe",
						"4 Main Account Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeQuestion, true),
				"wIlL TrAdE WaStE Be dIsChArGeD?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblTradeWasteDischargeYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblTradeWasteDischargeNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeQuestion), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblTradeWasteDischargeYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblTradeWasteDischargeNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(tradewastemovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(tradewastemovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeNo, 0),
				assertionErrorMsg(getLineNumber()));
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
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go back to the previous section
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.commercial, 0), "We are not in the Account Details section");
		scrollPageDown(700);
		clickElementAction(tradewastemovein.header);
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
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(tradewastemovein.tradeWasteDischargeYes);
		clickElementAction(tradewastemovein.lblDischargeQuestion);
		pauseSeleniumExecution(300);
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not in error state
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
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeYes),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteDischargeNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteDischargeNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblEquipmentQuestion, true),
				"iS TrAdE WaStE EqUiPmEnT InStAlLeD?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblTradeWasteEquipYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblTradeWasteEquipNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblEquipmentQuestion), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblTradeWasteEquipYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblTradeWasteEquipNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(tradewastemovein.labelInput, "bUsInEsS AcTiViTy aT ThE PrOpErTy"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeInfoHeader, true),
				"dIsChArGe iNfOrMaTiOn", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeInfoHeader), HEADER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeInfoIntro, true),
				"pLeAsE AnSwEr tHe bElOw dEtAiLs tO ThE BeSt oF YoUr aBiLiTy. If yOu aRe uNsUrE YoU MaY LeAvE It bLaNk.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeInfoIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(tradewastemovein.labelInput, "mAxImUm iNsTaNtAnEoUs fLoW RaTe (LiTrEs / sEcOnD)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(tradewastemovein.labelInput, "mAxImUm dAiLy dIsChArGe vOlUmE (lItReS)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(tradewastemovein.labelInput, "dIsChArGe sTaRt dAtE (dd/MM/yyyy)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(tradewastemovein.iconDischargeStartDate), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineMaxFlowRate), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineMaxDischargeVolume),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineDischargeStartDate),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeDays, true),
				"dIsChArGe dAyS (sElEcT OnE Or mOrE)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeDays), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sU")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "mO")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tU")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "wE")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tH")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "fR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sA")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sU")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "mO")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tU")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "wE")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tH")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "fR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sA")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "sU")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "mO")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "tU")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "wE")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "tH")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "fR")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.dischargeDaysOptions, true, "sA")), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeHoursStart, true),
				"dIsChArGe hOuRs sTaRtInG", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.lblDischargeHoursEnd, true),
				"dIsChArGe hOuRs eNdInG", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeHoursStart), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblDischargeHoursEnd), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursStartHourInc),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursStartMinInc),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursStartHourDec),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursStartMinDec),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.dischargeHoursStartHour),
				DISCHARGE_HRS_INPUT_BORDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.dischargeHoursStartMin),
				DISCHARGE_HRS_INPUT_BORDER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify fix for ticket BBPRTL-2045
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursStartAmPm), DISCHARGE_AM_PM_BTN_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursEndHourInc),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursEndMinInc),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursEndHourDec),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(tradewastemovein.dischargeHoursEndMinDec),
				DISCHARGE_HRS_INC_DEC_BTN_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.dischargeHoursEndHour),
				DISCHARGE_HRS_INPUT_BORDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.dischargeHoursEndMin),
				DISCHARGE_HRS_INPUT_BORDER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify fix for ticket BBPRTL-2045
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursEndAmPm), DISCHARGE_AM_PM_BTN_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(tradewastemovein.dragAndDropBorder), UPLOAD_AREA_BORDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// click the header of the next section to validate
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);

		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
				assertionErrorMsg(getLineNumber()));
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
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(tradewastemovein.labelInput, "bUsInEsS AcTiViTy aT ThE PrOpErTy"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.hintBusinessActivity, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.hintBusinessActivity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// verify we can go back to the previous section
		clickElementAction(tradewastemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not in the Account Details section");
		scrollPageDown(200);
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);

		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteEquipYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.tradeWasteEquipNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(tradewastemovein.radioOuterTradeWasteEquipNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(tradewastemovein.radioInnerTradeWasteEquipNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(tradewastemovein.labelInput, "bUsInEsS AcTiViTy aT ThE PrOpErTy"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.hintBusinessActivity, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.hintBusinessActivity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		// verify values are not ticked
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease extractor - filter"),
				0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent pump"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Lint trap"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"sOlId sEtTlEmEnT PiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE ExTrAcToR - FiLtEr")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(800);
		// click the header of the next section to validate
		clickElementAction(mainaccountcontactmovein.header);
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
		// verify CSS
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.hintBusinessActivity, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.hintBusinessActivity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageUp(500);
		// verify we can go back to the previous section
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not in the Account Details section");
		scrollPageDown(700);
		clickElementAction(tradewastemovein.header);
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
		// verify CSS
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.hintBusinessActivity, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.hintBusinessActivity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// choose all options for the Waste Equipment
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
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"));
		// verify the options displayed in the Business Activity
		clickElementAction(tradewastemovein.businessActivity);
		pauseSeleniumExecution(1000);
		List<String> businessActTypes = null;
		try {
			businessActTypes = getAllMatOptionsValues(tradewastemovein.businessActivityDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			tradewastemovein = new TradeWasteMoveIn(driver);
			businessActTypes = getAllMatOptionsValues(tradewastemovein.businessActivityDiv);
		}
		List<String> expectedBusinessActTypes = new ArrayList<>(
				Arrays.asList("rEtAiL FoOd bUsInEsS", "rEtAiL MoToR VeHiClE", "oThEr"));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.floaterLblBusinessActivity, true),
				"bUsInEsS AcTiViTy aT ThE PrOpErTy", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.floaterLblBusinessActivity), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.optionRetailFood), MAT_OPTION_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.optionRetailFood), MAT_OPTION_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.optionOther), MAT_OPTION_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(businessActTypes, expectedBusinessActTypes, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		chooseFromList(tradewastemovein.businessActivityDiv, 1);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(tradewastemovein.businessActivity, true);
		softAssertion.assertEquals(typeChosen, "rEtAiL FoOd bUsInEsS", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"sOlId sEtTlEmEnT PiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true,
						"sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE TrAp")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "gReAsE ExTrAcToR - FiLtEr")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl/pLaTe sEpArAtOr")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "dIlUtIoN PiT")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oIl tRaP-GaRaGe sUmP")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sOlId sEtTlEmEnT PiT")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "bAsKeT TrAp")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "sIlVeR ReCoVeRy uNiT")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "pLaStEr aRrEsToR")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "hOlDiNg tAnK")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "cOoLiNg pIt")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT TaNk fInAl")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "eFfLuEnT PuMp")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "lInT TrAp")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.floaterLblBusinessActivity), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblColorBusinessActivity), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineBusinessActivity),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// click the header of the next section to validate
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify the Other option is in error state
		String errorMsg = getDisplayedText(tradewastemovein.hintTradeWasteOtherTextField, true);
		// verify the fix for bug ticket BBPRTL-2046
		softAssertion.assertEquals(errorMsg, "sPeCiFy tHe nAmE Of tHe eQuIpMeNt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(tradewastemovein.businessActivity, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineTradeWasteOtherTextField),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.hintTradeWasteOtherTextField), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(tradewastemovein.tradeWasteOtherTextField);
		// verify the expected number of characters allowed which is 50
		tradewastemovein.tradeWasteOtherTextField.sendKeys("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
		String otherText = getDisplayedValue(tradewastemovein.tradeWasteOtherTextField, false);
		int otherTextCount = otherText.length();
		softAssertion.assertEquals(otherText, "Lorem ipsum dolor sit amet, consectetur adipiscing",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(otherTextCount, 50, assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(tradewastemovein.tradeWasteOtherTextField), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineTradeWasteOtherTextField),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(tradewastemovein.tradeWasteOtherTextField);
		deleteAllTextFromField();
		tradewastemovein.tradeWasteOtherTextField.sendKeys("\"Other\" Equipment's");
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
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validations for Maximum instantaneous flow rate
		tradewastemovein.maxFlowRate.sendKeys("0123...96457", Keys.TAB);
		String maxFlow = getDisplayedValue(tradewastemovein.maxFlowRate, false);
		assertEquals(maxFlow, "123.965", assertionErrorMsg(getLineNumber()));
		clickElementAction(tradewastemovein.maxFlowRate);
		deleteAllTextFromField();
		tradewastemovein.maxFlowRate.sendKeys("9877.5204", Keys.TAB);
		maxFlow = getDisplayedValue(tradewastemovein.maxFlowRate, false);
		softAssertion.assertEquals(maxFlow, "9,877.52", assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.floaterLblMaxFlowRate, true),
				"mAxImUm iNsTaNtAnEoUs fLoW RaTe (LiTrEs / sEcOnD)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.floaterLblMaxFlowRate), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.maxFlowRate), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineMaxFlowRate),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validations for Maximum daily discharge volume
		tradewastemovein.maxDischargeVolume.sendKeys("0123...96457");
		clickElementAction(tradewastemovein.maxFlowRate);
		String maxDischarge = getDisplayedValue(tradewastemovein.maxDischargeVolume, false);
		assertEquals(maxDischarge, "123.965", assertionErrorMsg(getLineNumber()));
		clickElementAction(tradewastemovein.maxDischargeVolume);
		deleteAllTextFromField();
		tradewastemovein.maxDischargeVolume.sendKeys("10,098.05246");
		clickElementAction(tradewastemovein.maxFlowRate);
		maxDischarge = getDisplayedValue(tradewastemovein.maxDischargeVolume, false);
		softAssertion.assertEquals(maxDischarge, "10,098.052", assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.floaterLblMaxDischargeVolume, true),
				"mAxImUm dAiLy dIsChArGe vOlUmE (lItReS)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.floaterLblMaxDischargeVolume), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.maxDischargeVolume), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineMaxDischargeVolume),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		String dischargeStartDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String dischargeStartDateCRM = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(tradewastemovein.dischargeStartDate);
		pauseSeleniumExecution(1000);
		tradewastemovein.dischargeStartDate.sendKeys(dischargeStartDate);
		this.tradeWasteDischargeStartDate = dischargeStartDate;
		this.tradeWasteDischargeStartDateCRM = dischargeStartDateCRM;
		// click on the upload section to dismiss the calendar
		clickElementAction(tradewastemovein.lblDischargeHoursEnd);
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(tradewastemovein.floaterLblDischargeStartDate, true),
				"dIsChArGe sTaRt dAtE (dd/MM/yyyy)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.floaterLblDischargeStartDate), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dischargeStartDate), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(tradewastemovein.iconDischargeStartDate), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(tradewastemovein.underlineDischargeStartDate),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// tick all discharge days
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"));
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"));
		clickElementAction(tradewastemovein.lblDischargeDays);
		// verify CSS
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sU")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "mO")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tU")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "wE")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tH")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "fR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sA")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sU")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "mO")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tU")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "wE")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "tH")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "fR")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, true, "sA")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the discharge hours start
		String startAmPm = getDisplayedText(tradewastemovein.dischargeHoursStartAmPm, true);
		softAssertion.assertEquals(startAmPm, "AM", assertionErrorMsg(getLineNumber()));
		tradewastemovein.dischargeHoursStartHour.sendKeys("09");
		pauseSeleniumExecution(500);
		String startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, true);
		softAssertion.assertEquals(startMin, "00", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.dischargeHoursStartMin);
		deleteAllTextFromField();
		// verify that start hours got deleted
		String startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, false);
		verifyStringIsBlank(startHour);
		// enter the values again because deleting from the mins
		// deletes also the values from the hour
		tradewastemovein.dischargeHoursStartHour.sendKeys("09", Keys.TAB);
		tradewastemovein.dischargeHoursStartMin.sendKeys("59", Keys.TAB);
		// click the arrow up for the Start Hour
		clickElementAction(tradewastemovein.dischargeHoursStartHourInc);
		startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, true);
		verifyTwoStringsAreEqual(startHour, "10", true);
		// click the arrow down for the Start Hour
		clickElementAction(tradewastemovein.dischargeHoursStartHourDec);
		startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, true);
		verifyTwoStringsAreEqual(startHour, "09", true);
		// click the arrow up for the Start Min
		clickElementAction(tradewastemovein.dischargeHoursStartMinInc);
		startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, true);
		startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, true);
		softAssertion.assertEquals(startHour, "10", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(startMin, "00", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click the arrow down for the Start Min
		clickElementAction(tradewastemovein.dischargeHoursStartMinDec);
		startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, true);
		startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, true);
		softAssertion.assertEquals(startHour, "09", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(startMin, "59", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		startAmPm = getDisplayedText(tradewastemovein.dischargeHoursStartAmPm, true);
		verifyTwoStringsAreEqual(startAmPm, "AM", true);

		// verify the discharge hours end
		String endAmPm = getDisplayedText(tradewastemovein.dischargeHoursEndAmPm, true);
		softAssertion.assertEquals(endAmPm, "AM", assertionErrorMsg(getLineNumber()));
		tradewastemovein.dischargeHoursEndHour.sendKeys("10");
		pauseSeleniumExecution(500);
		String endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, true);
		softAssertion.assertEquals(endMin, "00", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.dischargeHoursEndMin);
		deleteAllTextFromField();
		// verify that end hours got deleted
		String endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, false);
		verifyStringIsBlank(endHour);
		// enter the values again because deleting from the mins
		// deletes also the values from the hour
		tradewastemovein.dischargeHoursEndHour.sendKeys("10", Keys.TAB);
		tradewastemovein.dischargeHoursEndMin.sendKeys("59", Keys.TAB);
		// click the arrow up for the End Hour
		clickElementAction(tradewastemovein.dischargeHoursEndHourInc);
		endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, true);
		verifyTwoStringsAreEqual(endHour, "11", true);
		// click the arrow down for the End Hour
		clickElementAction(tradewastemovein.dischargeHoursEndHourDec);
		endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, true);
		verifyTwoStringsAreEqual(endHour, "10", true);
		// click the arrow up for the End Min
		clickElementAction(tradewastemovein.dischargeHoursEndMinInc);
		endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, true);
		endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, true);
		softAssertion.assertEquals(endHour, "11", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(endMin, "00", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// click the arrow down for the End Min
		clickElementAction(tradewastemovein.dischargeHoursEndMinDec);
		endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, true);
		endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, true);
		softAssertion.assertEquals(endHour, "10", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(endMin, "59", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.dischargeHoursEndAmPm);
		endAmPm = getDisplayedText(tradewastemovein.dischargeHoursEndAmPm, true);
		verifyTwoStringsAreEqual(endAmPm, "PM", true);

		// verify CSS
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursStartAmPm), DISCHARGE_AM_PM_BTN_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(tradewastemovein.dischargeHoursStartAmPm);
		pauseSeleniumExecution(300);
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursStartAmPm),
				DISCHARGE_AM_PM_BTN_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursEndAmPm), DISCHARGE_AM_PM_BTN_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(tradewastemovein.dischargeHoursEndAmPm);
		pauseSeleniumExecution(300);
		softAssertion.assertEquals(getAmPmButtonCss(tradewastemovein.dischargeHoursEndAmPm),
				DISCHARGE_AM_PM_BTN_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dischargeHoursStartHour), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dischargeHoursStartMin), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dischargeHoursEndHour), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dischargeHoursEndMin), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify upload section not in error state
		softAssertion.assertFalse(isElementInError(tradewastemovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify the text in the upload section
		String uploadIntro = getDisplayedText(tradewastemovein.lblTradeWasteAttachmentIntro, true);
		softAssertion.assertEquals(uploadIntro,
				"Please upload all site plans and work drawings below. We will review all the documentation provided and be in contact to arrange a site inspection before permit is issued. You can review our trade waste pricing here.",
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(tradewastemovein.lblTradeWasteAttachmentIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.linkLblTradeWasteAttachmentIntro), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(tradewastemovein.linkLblTradeWasteAttachmentIntro);
		softAssertion.assertEquals(getLabelCss(tradewastemovein.linkLblTradeWasteAttachmentIntro),
				LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// upload trade waste files
		uploadTradeWasteFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg", "Sprin't 02 Story 'Board.pdf");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check the number of files uploaded
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Trade Waste ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));

			// mouse hover to ensure we have activity
			hoverToElementAction(tradewastemovein.dragAndDropArea);
			// add additional wait time because the file g'alaxy-'wallpaper.jpeg
			// cannot be viewed in the preview/corrupted
			waitForFilesToBeUploaded(25000);
			// check the number of files uploaded
			actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME,
					" is <", Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
		}

		String dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		// verify the files that were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.linkDragAndDropClickToBrowse), DRAG_AND_DROP_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg"), true),
				"dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(tradewastemovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf"),
				true), "dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(getElementFrmMatStepHdrTag(tradewastemovein.matStepHeader, "Trade Waste"),
				true);
		softAssertion.assertEquals(header, "3 tRaDe wAsTe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.header), SECTION_HEADER_FOCUSED_CSTM,
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
	public void verifyMainContact(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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
		// verify the notification introduction is not displayed
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.lblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationHeader, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
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
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		String billsNotifText = getDisplayedText(mainaccountcontactmovein.lblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(mainaccountcontactmovein.lblAcctnotifAndRemindersNotif, true);
		String marketComNotifText = getDisplayedText(mainaccountcontactmovein.lblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText, "info Account Notifications and Reminders",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications not ticked
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
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
		// verify no tooltip is displayed
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		hoverToElementAction(mainaccountcontactmovein.billsNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.billsNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.marketingComNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.marketingComNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify the url prefill
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.firstName, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.lastName, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.emailAddress, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.businessPhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
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
		// verify all assertions
		softAssertion.assertAll();

		String firstName = "Dominic";
		String lastName = "Blank";
		this.mainContactFirstName = firstName;
		this.mainContactLastName = lastName;
		mainaccountcontactmovein.firstName.sendKeys(firstName);
		mainaccountcontactmovein.lastName.sendKeys("Blañk");
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		mainaccountcontactmovein.businessPhone.sendKeys("0829821210");
		mainaccountcontactmovein.afterhoursPhone.sendKeys("+09278169824");

		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		// ñ letter is not an english character so it would be removed automatically
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Blak",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true), "+09278169824",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.lastName);
		deleteAllTextFromField();
		mainaccountcontactmovein.lastName.sendKeys(lastName);

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact01(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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
		softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationIntro, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the notification header is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationHeader, 0),
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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0, 3),
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
		// verify the Contact secret code is not displayed
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ContactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		String billsNotifText = getDisplayedText(additionalcontactmovein.addCont1LblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(additionalcontactmovein.addCont1LblAcctnotifAndRemindersNotif,
				true);
		String marketComNotifText = getDisplayedText(additionalcontactmovein.addCont1LblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText, "info Account Notifications and Reminders",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should not be ticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
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
		// verify the tooltip message for each notification
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		hoverToElementAction(additionalcontactmovein.addCont1BillsNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1BillsNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipIcon);
		softAssertion.assertFalse(
				isElementExists(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1MarketingComNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1MarketingComNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0, 3),
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
		// verify all assertions
		softAssertion.assertAll();

		String firstName = "Melanie";
		String lastName = "Banks";
		additionalcontactmovein.addCont1FirstName.sendKeys("MelaÑie");
		additionalcontactmovein.addCont1LastName.sendKeys(lastName);
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+8361085965230");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("0829821210");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+09278169824");
		// Ñ letter is not an english character so it would be removed automatically
		softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1FirstName, false), "Melaie",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1FirstName);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1FirstName.sendKeys(firstName);

		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details",
				"create aCcOuNt dEtAiLs", "create tRaDe wAsTe",
				concatStrings("create Main Account Contact (", this.mainContactFirstName, " ", this.mainContactLastName,
						")"),
				concatStrings("5 Additional Contact (", firstName, " ", lastName, ")"), "6 Direct Debit",
				"7 Additional Note", "8 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify postal address is now displayed
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "create aCcOuNt dEtAiLs",
				"create tRaDe wAsTe",
				concatStrings("create Main Account Contact (", this.mainContactFirstName, " ", this.mainContactLastName,
						")"),
				concatStrings("5 Additional Contact (", firstName, " ", lastName, ")"), "6 Postal Address",
				"7 Direct Debit", "8 Additional Note", "9 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not yet in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact01" })
	public void verifyPostalAddress(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
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

		// verify the required fields
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
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

		postaladdressmovein.addLine01.sendKeys("Add Line 01");
		postaladdressmovein.addLine02.sendKeys("Add Line 02");
		postaladdressmovein.addLine03.sendKeys("Add Line 03");
		postaladdressmovein.addLine04.sendKeys("Add Line 04");
		postaladdressmovein.city.sendKeys("Suburb");
		postaladdressmovein.state.sendKeys("VIC");
		postaladdressmovein.postcode.sendKeys("90210");
		postaladdressmovein.country.sendKeys("Australia");
		// click to collapse the dropdown
		clickElementAction(postaladdressmovein.postcode);

		clickElementAction(postaladdressmovein.sameSupAddressYes);
		pauseSeleniumExecution(500);
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = { "verifyPostalAddress" })
	public void verifyDirectDebitDetails(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.lblNoDirectDebit, true),
				"nO, i wIlL PaY BiLlS By tHe dUe dAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblNoDirectDebit), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterNoDirectDebit),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerNoDirectDebit),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCard);
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		String loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		assertEquals(loadingMsg, "Creating secure area for credit card entry...",
				"Credit Card initialization progress bar text is not correct");
		moveInDirectDebitCCProgBarLoad();

		// enter valid fields for credit card name, credit card number and expiry
		switchToMWIframe();
		clickElementAction(directdebitmovein.creditCardName);
		deleteAllTextFromField();
		directdebitmovein.creditCardName.sendKeys("Howard Stark");
		clickElementAction(directdebitmovein.creditCardNumber);
		deleteAllTextFromField();
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_10"), true, 300);
		clickElementAction(directdebitmovein.creditCardExpiry);
		deleteAllTextFromField();
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 4;
		String monthStr = Integer.toString(month);
		String expYrStrFull = Integer.toString(expYr);
		String expYrStr = getString(expYrStrFull, 2, 4);
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
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify no authorisation text is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		// verify no authorisation checkbox is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify no authorisation text is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblBankAccountAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		// verify no authorisation checkbox is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationBankAccountList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCard);
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		assertEquals(loadingMsg, "Creating secure area for credit card entry...",
				"Credit Card initialization progress bar text is not correct");
		moveInDirectDebitCCProgBarLoad();

		// verify that the credit card details got removed
		switchToMWIframe();
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardName, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardNumber, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardExpiry, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter credit card details again
		clickElementAction(directdebitmovein.creditCardName);
		deleteAllTextFromField();
		directdebitmovein.creditCardName.sendKeys("Howard Stark");
		clickElementAction(directdebitmovein.creditCardNumber);
		deleteAllTextFromField();
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_10"), true, 300);
		clickElementAction(directdebitmovein.creditCardExpiry);
		deleteAllTextFromField();
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		month = 5;
		monthStr = Integer.toString(month);
		expYrStrFull = Integer.toString(expYr);
		expYrStr = getString(expYrStrFull, 2, 4);
		monthStr = concatStrings("0", monthStr);
		expiry = concatStrings(monthStr, "/", expYrStr);
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

		// verify we get redirected immediately into the next section
		clickElementAction(directdebitmovein.next);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note details section");

		clickElementAction(additionalnotemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0), "We are not yet in the Direct Debit section");

		// verify values are still there
		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Howard Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_11"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		pauseSeleniumExecution(500);
		clickElementAction(directdebitmovein.creditCard);
		// verify values are still there
		actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Howard Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_11"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
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
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify no authorisation text is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblBankAccountAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		// verify no authorisation checkbox is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationBankAccountList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		// verify no authorisation text is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblBankAccountAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		// verify no authorisation checkbox is displayed
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationBankAccountList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCard);
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.progressBarTextList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify values are still there
		actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Howard Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_11"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		directdebitmovein.bankAccountName
				.sendKeys(concatStrings(this.mainContactFirstName, " ", this.mainContactLastName));
		directdebitmovein.accountBSB.sendKeys("000010");
		directdebitmovein.accountNumber.sendKeys("000012200");

		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance page section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 8, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAcceptanceDetails(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify each section
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);

		softAssertion.assertEquals(movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
				" (Settlement on ", this.ownerSettlementDate, ")"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_valid_company_name_abn3_abn4"), " ABN/ACN ", getProp("test_data_valid_abn3")),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2047
		softAssertion.assertEquals(tradeWaste,
				"tRaDe wAsTe update wIlL DiScHaRgE TrAdE WaStE iNsTaLlEd fOlLoWiNg eQuIpMeNt gReAsE TrAp pLaStEr aRrEsToR gReAsE ExTrAcToR - FiLtEr hOlDiNg tAnK oIl/pLaTe sEpArAtOr cOoLiNg pIt dIlUtIoN PiT eFfLuEnT TaNk fInAl oIl tRaP-GaRaGe sUmP eFfLuEnT PuMp sOlId sEtTlEmEnT PiT lInT TrAp bAsKeT TrAp \"Other\" Equipment's sIlVeR ReCoVeRy uNiT bUsInEsS AcTiViTy iS 'rEtAiL FoOd bUsInEsS'",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo, concatStrings(
				"dIsChArGe iNfOrMaTiOn update mAx iNsTaNtAnEoUs fLoW RaTe 9,877.52 lItReS / SeCoNd mAx dAiLy dIsChArGe vOlUmE 10,098.052 lItReS dIsChArGe sTaRt dAtE iS ",
				this.tradeWasteDischargeStartDate,
				" dIsChArGe dAyS aRe sU, mO, tU, wE, tH, fR, sA dIsChArGe hOuRs bEtWeEn 09:59 AM aNd 10:59 PM uPlOaDeD 2 sItE pLaNs"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update ", this.mainContactFirstName, " ", this.mainContactLastName,
						" Email Address: ", getProp("test_dummy_email_lower_case"),
						" Business Phone: 0829821210 A/Hours Phone: +09278169824"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Melanie Banks Email Address: ", getProp("test_dummy_email_lower_case"),
						" Mobile Phone: +8361085965230 Business Phone: 0829821210 A/Hours Phone: +09278169824"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal, Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(postAdd,
					"Postal Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(postAdd,
					"Postal Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Dominic Blank BSB: 000010 / Num: 000012200",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// remove the attachment "g'alaxy-'wallpaper.jpeg"
		// in the Life Support details
		clickExactLinkNameFromElement(acceptancemovein.lifeSupportRow, "update");
		pauseSeleniumExecution(1000);
		// verify the existing attachments
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Smaller tif file .tif 0.8 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(400);
		// let's remove an uploaded file
		deleteUploadedFiles(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg");
		// verify the text displayed
		String containerText = getDisplayedText(supplydetailsmovein.dialogContainerText, true);
		portalmovein = new PortalMoveIn(driver);
		// verify CSS and lang files
		// verify the fix for bug ticket BBPRTL-2051
		softAssertion.assertEquals(containerText, "aRe yOu sUrE YoU WoUlD LiKe tO ReMoVe tHiS FiLe?",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(supplydetailsmovein.dialogContainer), DIALOG_CONTAINER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.dialogContainerText), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(supplydetailsmovein.dialogRemoveFileYes), DIALOG_YES_AND_OK_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(supplydetailsmovein.dialogRemoveFileNo), DIALOG_NO_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.overlayBackdrop), OVERLAY_BACKDROP_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.dialogRemoveFileYes);
		pauseSeleniumExecution(1000);
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Smaller tif file .tif 0.8 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(900);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we go to the Trade Waste section
		clickExactLinkNameFromElement(acceptancemovein.tradeWasteRow, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Trade Waste section
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(800);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we go to the Trade Waste section
		clickExactLinkNameFromElement(acceptancemovein.dischargeInfoRow, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the Trade Waste section
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the original attachment is still there
		dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		scrollPageDown(300);
		// let's remove an uploaded file
		deleteUploadedFiles(tradewastemovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf");
		// verify the text displayed
		containerText = getDisplayedText(tradewastemovein.dialogContainerText, true);
		portalmovein = new PortalMoveIn(driver);
		// verify CSS and lang files
		// verify the fix for bug ticket BBPRTL-2051
		softAssertion.assertEquals(containerText, "aRe yOu sUrE YoU WoUlD LiKe tO ReMoVe tHiS FiLe?",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(tradewastemovein.dialogContainer), DIALOG_CONTAINER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(tradewastemovein.dialogContainerText), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(tradewastemovein.dialogRemoveFileYes), DIALOG_YES_AND_OK_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(tradewastemovein.dialogRemoveFileNo), DIALOG_NO_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.overlayBackdrop), OVERLAY_BACKDROP_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.dialogRemoveFileYes);
		pauseSeleniumExecution(1000);
		dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		scrollPageDown(600);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(500);
		clickExactLinkNameFromElement(acceptancemovein.postalAddressRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(postaladdressmovein.sameSupAddressNo);
		// verify the fix for bug ticket BBPRTL-1469
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.addLine01, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.addLine02, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.addLine03, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.addLine04, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.city, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.state, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.postcode, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(postaladdressmovein.country, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(postaladdressmovein.sameSupAddressYes);
		scrollPageDown(600);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify the acceptance page again
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
				" (Settlement on ", this.ownerSettlementDate, ")"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_valid_company_name_abn3_abn4"), " ABN/ACN ", getProp("test_data_valid_abn3")),
				assertionErrorMsg(getLineNumber()));
		// verify fix for bug ticket BBPRTL-2047
		softAssertion.assertEquals(tradeWaste,
				"tRaDe wAsTe update wIlL DiScHaRgE TrAdE WaStE iNsTaLlEd fOlLoWiNg eQuIpMeNt gReAsE TrAp pLaStEr aRrEsToR gReAsE ExTrAcToR - FiLtEr hOlDiNg tAnK oIl/pLaTe sEpArAtOr cOoLiNg pIt dIlUtIoN PiT eFfLuEnT TaNk fInAl oIl tRaP-GaRaGe sUmP eFfLuEnT PuMp sOlId sEtTlEmEnT PiT lInT TrAp bAsKeT TrAp \"Other\" Equipment's sIlVeR ReCoVeRy uNiT bUsInEsS AcTiViTy iS 'rEtAiL FoOd bUsInEsS'",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo, concatStrings(
				"dIsChArGe iNfOrMaTiOn update mAx iNsTaNtAnEoUs fLoW RaTe 9,877.52 lItReS / SeCoNd mAx dAiLy dIsChArGe vOlUmE 10,098.052 lItReS dIsChArGe sTaRt dAtE iS ",
				this.tradeWasteDischargeStartDate,
				" dIsChArGe dAyS aRe sU, mO, tU, wE, tH, fR, sA dIsChArGe hOuRs bEtWeEn 09:59 AM aNd 10:59 PM uPlOaDeD 1 sItE pLaN"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update ", this.mainContactFirstName, " ", this.mainContactLastName,
						" Email Address: ", getProp("test_dummy_email_lower_case"),
						" Business Phone: 0829821210 A/Hours Phone: +09278169824"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Melanie Banks Email Address: ", getProp("test_dummy_email_lower_case"),
						" Mobile Phone: +8361085965230 Business Phone: 0829821210 A/Hours Phone: +09278169824"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal, Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(postAdd,
					"Postal Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(postAdd,
					"Postal Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Dominic Blank BSB: 000010 / Num: 000012200",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 9, dependsOnMethods = { "verifyAcceptanceDetails" })
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

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 13, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
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
		String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
		String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		this.sourceID = sessionSourceId;
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadStandaloneMoveInPortal(true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// let's refresh the browser and confirm
			// that the session and local storage was not cleared
			refreshBrowser(1, 5000);
			loadEmbeddedMoveInPortal(true, true);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// refreshing the page should take us back into the 3rd party prefill URL
			refreshBrowser(1, 5000);
			// re-enter the same values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("75", "Davis", StreetTypesEnum.st, "NORWOOD", AustralianStatesEnum.vic, "5067",
					AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
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
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				// verify the expected number of keys
				softAssertion.assertEquals(sessionLength, 13, assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"),
						assertionErrorMsg(getLineNumber()));
				// verify the expected number of keys
				softAssertion.assertEquals(sessionLength, 16, assertionErrorMsg(getLineNumber()));
			}
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the session storage are not empty
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertFalse(sessionKeys.contains("readOnlyIfThirdParty"),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(sessionKeys.contains("move-in-query-headless1"),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(sessionKeys.contains("move-in.extra_data"),
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
				String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
				String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
				softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless),
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			}
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify the supply details
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, true),
					this.ownerMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.settlementDateOwner, true),
					this.ownerSettlementDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddComplexName, true),
						"9 Garden Walk", assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddComplexName, true),
						"dummy complex", assertionErrorMsg(getLineNumber()));
				// verify the fields that are not editable
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
			}
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, true), "Villa",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, true),
					"Happy Valley Retirement Village", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, true), "75",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddStreetName, true), "Davis",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddStreetType, true), "Cul-de-sac",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddCity, true), "NORWOOD",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddState, true), "South Australia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddPostcode, true), "5067",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
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
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingYesList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingNoList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			scrollPageDown(500);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			// verify all files still uploaded
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Smaller tif file.tif .image/tiff 0.8 MB fIlE UpLoAdEd sUcCeSsFuLlY typing jim carrey.gif .image/gif 0.5 MB fIlE UpLoAdEd sUcCeSsFuLlY",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);

			// verify the account details
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, true),
					getProp("test_data_valid_abn3"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.companyName, true),
					getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.tradingName, false)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(tradewastemovein.header);
			pauseSeleniumExecution(1000);

			// verify the details for the Trade Waste
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Grease trap"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false,
							"Grease extractor - filter"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil/plate separator"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Dilution pit"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Oil trap-garage sump"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Solid settlement pit"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Basket trap"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Silver recovery unit"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Plaster arrestor"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Holding tank"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Cooling pit"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(tradewastemovein.tradeWasteEquipOptions, false, "Effluent tank final"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
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
			String otherEquipVal = getDisplayedValue(tradewastemovein.tradeWasteOtherTextField, true);
			softAssertion.assertEquals(otherEquipVal, "\"Other\" Equipment's", assertionErrorMsg(getLineNumber()));
			String busAct = getDisplayedText(tradewastemovein.businessActivity, true);
			softAssertion.assertEquals(busAct, "rEtAiL FoOd bUsInEsS", assertionErrorMsg(getLineNumber()));
			String flowRate = getDisplayedValue(tradewastemovein.maxFlowRate, true);
			String dischargeVol = getDisplayedValue(tradewastemovein.maxDischargeVolume, true);
			String dischargeDate = getDisplayedValue(tradewastemovein.dischargeStartDate, true);
			softAssertion.assertEquals(flowRate, "9,877.52", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeVol, "10,098.052", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeDate, this.tradeWasteDischargeStartDate,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 0),
					assertionErrorMsg(getLineNumber()));
			String startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, true);
			String startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, true);
			String startAmPm = getDisplayedText(tradewastemovein.dischargeHoursStartAmPm, true);
			String endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, true);
			String endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, true);
			String endAmPm = getDisplayedText(tradewastemovein.dischargeHoursEndAmPm, true);
			softAssertion.assertEquals(startHour, "09", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(startMin, "59", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(startAmPm, "AM", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(endHour, "10", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(endMin, "59", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(endAmPm, "PM", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(tradewastemovein.dragAndDropArea, 0, 3),
					assertionErrorMsg(getLineNumber()));
			scrollPageDown(600);
			dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
			String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);

			// verify the main account contact section
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, true),
					this.mainContactFirstName, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, true),
					this.mainContactLastName, assertionErrorMsg(getLineNumber()));
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
			// verify add another contact link is not displayed
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			// verify the notification introduction is not displayed
			softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.lblNotificationIntro, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the notification header is displayed
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblNotificationHeader, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
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
			// verify the notifications not ticked
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, true),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, true), "0829821210",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true),
					"+09278169824", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalcontactmovein.addCont1Header);
			pauseSeleniumExecution(1000);

			// verify the additional contacts section
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1FirstName, true), "Melanie",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1LastName, true), "Banks",
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
			softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationIntro, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the notification header is displayed
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LblNotificationHeader, 0),
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
			softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BillsSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MarketingComSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			// verify the notifications that should not be ticked
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, true),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, true),
					"+8361085965230", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, true),
					"0829821210", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, true),
					"+09278169824", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(postaladdressmovein.header);
			pauseSeleniumExecution(1000);

			// verify the postal address section
			softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);

			// verify the direct debit section
			softAssertion.assertTrue(isElementTicked(directdebitmovein.bankAccount, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(directdebitmovein.bankAccountName, true), "Dominic Blank",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountBSB, true), "000010",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(directdebitmovein.accountNumber, true), "000012200",
					assertionErrorMsg(getLineNumber()));
			directdebitmovein = new DirectDebitMoveIn(driver, 0);
			// verify no authorisation text is displayed
			softAssertion.assertFalse(isElementExists(directdebitmovein.lblBankAccountAuthorisationList),
					assertionErrorMsg(getLineNumber()));
			// verify no authorisation checkbox is displayed
			softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationBankAccountList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.header);
			pauseSeleniumExecution(1000);

			// verify the additional note section
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(additionalnotemovein.notesArea, false)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(acceptancemovein.header);
			pauseSeleniumExecution(1000);

			// verify the acceptance page again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
			String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
			String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
			String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
			String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
			String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
			String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
			String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
			String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion
					.assertEquals(movingIn,
							concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
									" (Settlement on ", this.ownerSettlementDate, ")"),
							assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertEquals(servAdd,
						"Service Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				softAssertion.assertEquals(servAdd,
						"Service Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(lifeSup,
					"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, concatStrings("Account Details update Commercial Account ",
					getProp("test_data_valid_company_name_abn3_abn4"), " ABN/ACN ", getProp("test_data_valid_abn3")),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-2047
			softAssertion.assertEquals(tradeWaste,
					"tRaDe wAsTe update wIlL DiScHaRgE TrAdE WaStE iNsTaLlEd fOlLoWiNg eQuIpMeNt gReAsE TrAp pLaStEr aRrEsToR gReAsE ExTrAcToR - FiLtEr hOlDiNg tAnK oIl/pLaTe sEpArAtOr cOoLiNg pIt dIlUtIoN PiT eFfLuEnT TaNk fInAl oIl tRaP-GaRaGe sUmP eFfLuEnT PuMp sOlId sEtTlEmEnT PiT lInT TrAp bAsKeT TrAp \"Other\" Equipment's sIlVeR ReCoVeRy uNiT bUsInEsS AcTiViTy iS 'rEtAiL FoOd bUsInEsS'",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeInfo, concatStrings(
					"dIsChArGe iNfOrMaTiOn update mAx iNsTaNtAnEoUs fLoW RaTe 9,877.52 lItReS / SeCoNd mAx dAiLy dIsChArGe vOlUmE 10,098.052 lItReS dIsChArGe sTaRt dAtE iS ",
					this.tradeWasteDischargeStartDate,
					" dIsChArGe dAyS aRe sU, mO, tU, wE, tH, fR, sA dIsChArGe hOuRs bEtWeEn 09:59 AM aNd 10:59 PM uPlOaDeD 1 sItE pLaN"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					concatStrings("Main Account Contact update ", this.mainContactFirstName, " ",
							this.mainContactLastName, " Email Address: ", getProp("test_dummy_email_lower_case"),
							" Business Phone: 0829821210 A/Hours Phone: +09278169824"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (Email)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Data,
					concatStrings("Melanie Banks Email Address: ", getProp("test_dummy_email_lower_case"),
							" Mobile Phone: +8361085965230 Business Phone: 0829821210 A/Hours Phone: +09278169824"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal, Email, SMS) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertEquals(postAdd,
						"Postal Address update 9 Garden Walk Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
						assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				softAssertion.assertEquals(postAdd,
						"Postal Address update dummy complex Villa Happy Valley Retirement Village, 75 Davis Cul-de-sac NORWOOD, South Australia, 5067",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(directDebit,
					"Direct Debit update Bank Account Account Name: Dominic Blank BSB: 000010 / Num: 000012200",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));
			
			// test the fix for bug ticket BBPRTL-2185
			// let's confirm the keys in the session storage
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("third_party_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 10, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionThirdPartyData = storage.getItemFromSessionStorage("third_party_data");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionThirdPartyData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			
			// let's enter the same data for each section
			verifySupplyDetails(true);
			verifyAccountDetails(false);
			verifyTradeWaste(false);
			verifyMainContact(false);
			verifyAdditionalContact01(false);
			verifyPostalAddress(false);
			verifyDirectDebitDetails(false);
			verifyAcceptanceDetails(false);

			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			// verify the local and session keys after refreshing the page
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 11, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's enter the same data for each section
			verifySupplyDetails(false);
			verifyAccountDetails(false);
			verifyTradeWaste(false);
			verifyMainContact(false);
			verifyAdditionalContact01(false);
			verifyPostalAddress(false);
			verifyDirectDebitDetails(false);
			verifyAcceptanceDetails(false);

			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			// let's confirm the keys in the session storage
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
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the session storage are not empty
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_number");
			sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		sessionSourceId = storage.getItemFromSessionStorage("source_id");
		this.sourceID = sessionSourceId;

		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 14, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1700);
		// tick all 2 checkboxes
		clickElementAction(acceptancemovein.firstCheckbox);
		if (getPortalType().equals("standalone")) {
			// we use javaScriptClickButton to click it because the method
			// clickButton mistakenly clicks the link which opens a new tab in standalone
			javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		} else if (getPortalType().equals("embedded")) {
			clickElementAction(acceptancemovein.secondCheckbox);
		}

		// add the property files before submitting the request
		addProp("SmallBusExistingContact03_ownerSettlementDate", this.ownerSettlementDate);
		addProp("SmallBusExistingContact03_ownerMoveInDateCRM", this.ownerMoveInDateCRM);
		addProp("SmallBusExistingContact03_ownerSettlementDateCRM", this.ownerSettlementDateCRM);
		addProp("SmallBusExistingContact03_tradeWasteDischargeStartDateCRM", this.tradeWasteDischargeStartDateCRM);
		addProp("SmallBusExistingContact03_sourceID", this.sourceID);
		addProp("SmallBusExistingContact03_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("SmallBusExistingContact03_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

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
