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
import au.com.blueoak.portal.pageObjects.move_in.ManagerHolidayLettingMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiExistingContact02 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
	PostalAddressMoveIn postaladdressmovein;
	ConcessionMoveIn concessionmovein;
	ManagerHolidayLettingMoveIn managerholidaylettingmovein;
	DirectDebitMoveIn directdebitmovein;
	AdditionalNoteMoveIn additionalnotemovein;
	AcceptanceMoveIn acceptancemovein;
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
	 * 
	 * */
	String moveInDateUrlPrefill;

	/**
	 * This will be the Move In Date
	 */
	String ownerMoveInDate;

	/**
	 * This will be the Move In date for CRM
	 */
	String ownerMoveInDateCRM;

	/**
	 * This will be the Settlement Date
	 */
	String ownerSettlementDate;

	/**
	 * This will be the Settlement Date for CRM
	 */
	String ownerSettlementDateCRM;

	/**
	 * Date of Birth for Main Contact
	 */
	String dateOfBirthMain;

	/**
	 * For the credit card expiry to be used for assertions
	 */
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
	 * 
	 * */
	String sourceID;

	/**
	 * The ID of the online request created
	 */
	String onlineReqId;

	@BeforeClass
	@Override
	public void startTest() {

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);

		super.setupTestProp();
		super.initChromeDriver("fil");

		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// remove the previous global lang files
		String fileToCheck = "fil.json";
		deleteMoveInGlobalLangFiles(s3Access, fileToCheck);
		// let's verify that the file is indeed deleted
		String contents = readMoveInGlobalLangFile(s3Access, fileToCheck);
		assertTrue(StringUtils.isBlank(contents),
				concatStrings("The global language file '", fileToCheck, "' is not yet deleted"));

		// remove the previous custom lang files
		deleteMoveInCustomLangFiles(s3Access, "custom_en.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "39\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "07\\", "custom_fil.json");

		// let's invalidate the global lang files directory
		// to ensure that cloudfront did not cache any of our test global language files
		invalidateMoveInGlobalLangFiles(s3Access);

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
			populate3rdPartyPrefill("287", "Gympie", StreetTypesEnum.WRONG_VALUE, "Noosaville",
					AustralianStatesEnum.WRONG_VALUE, "4566", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM,
					initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Unit.getLabelText(), "&tenancy_number=24",
					"&tenancy_street_number=287", "&tenancy_street_name=Gympie", "&tenancy_street_type=",
					StreetTypesEnum.TCE.name(), "&tenancy_suburb=Noosaville", "&tenancy_postcode=4566",
					"&tenancy_state=", AustralianStatesEnum.QLD.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&contact_first_name=     tom      tri     ",
					"&contact_last_name=     ly     ", "&mobile_number=123456789", "&business_hour_phone=123456789",
					"&after_hour_phone=123456789", "&email_address=", getProp("test_dummy_email_upper_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.Unit.getLabelText(), "&tenancy_number=24",
					"&tenancy_street_number=287", "&tenancy_street_name=Gympie", "&tenancy_street_type=",
					StreetTypesEnum.TCE.name(), "&tenancy_suburb=Noosaville", "&tenancy_postcode=4566",
					"&tenancy_state=", AustralianStatesEnum.QLD.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&contact_first_name=     tom      tri     ",
					"&contact_last_name=     ly     ", "&mobile_number=123456789", "&business_hour_phone=123456789",
					"&after_hour_phone=123456789", "&email_address=", getProp("test_dummy_email_upper_case"),
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
		accountdetailsmovein = new AccountDetailsMoveIn(driver);
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver);
		additionalcontactmovein = new AdditionalContactMoveIn(driver);
		postaladdressmovein = new PostalAddressMoveIn(driver);
		concessionmovein = new ConcessionMoveIn(driver);
		managerholidaylettingmovein = new ManagerHolidayLettingMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
	}

	/**
	 * 
	 * - verify the headers and supply details introduction text - verify fields are
	 * not in error state initially - verify that no additional contact section is
	 * displayed immediately - verify the section header name
	 * 
	 */
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

		// verify the header
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

		// here we enabled the custom language file in the portal config
		// and uploaded the custom language file, however there's no
		// global fil.json language file
		// so we verify that the global en.json language file was used instead
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I am a:",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "Tenant",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
				"Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// for now the dev prefill cannot populate the data for Property Manager
			// so we assert it to be blank for now
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			// verify additional contact displayed immediately
			// verify Property manager section displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Additional Contact", "5 Postal Address", "6 Concession",
							"7 Manager/Agent Company Details", "8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
			// verify the expected sections displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact (tom tri ly)",
							"4 Additional Contact", "5 Postal Address", "6 Concession",
							"7 Manager/Agent Company Details", "8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
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
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
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

		clickElementAction(supplydetailsmovein.owner);
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Postal Address", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact (tom tri ly)",
							"4 Postal Address", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's put a valid move in date as 10 days from past
		String past6Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -6, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past6D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -6, DATE_MONTH_YEAR_FORMAT_DASH);
		String past7Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -7, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past7D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -7, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(past6Days, Keys.TAB);
		this.ownerMoveInDate = past6Days;
		this.ownerMoveInDateCRM = past6D;
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleNo);
		clickElementAction(supplydetailsmovein.ownerSettleNo);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(past7Days, Keys.TAB);
		this.ownerSettlementDate = past7Days;
		this.ownerSettlementDateCRM = past7D;

		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		// verify the holiday rental required fields
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		// verify Holiday Letting/Rental Company Details section added
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Postal Address", "5 Holiday Letting/Rental Company Details",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Postal Address", "5 Concession",
					"6 Holiday Letting/Rental Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (tom tri ly)", "4 Postal Address", "5 Concession",
					"6 Holiday Letting/Rental Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsibleOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsiblePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// click next button to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.whoIsResponsibleOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.whoIsResponsiblePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.whoIsResponsiblePropMan);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify that the Main Contact section header changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Property Manager Account Contact", "4 Postal Address",
					"5 Holiday Letting/Rental Company Details", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify that the Main Contact section header changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Property Manager Account Contact", "4 Postal Address", "5 Concession",
					"6 Holiday Letting/Rental Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify that the Main Contact section header changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Property Manager Account Contact (tom tri ly)", "4 Postal Address",
					"5 Concession", "6 Holiday Letting/Rental Company Details", "7 Direct Debit", "8 Additional Note",
					"9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddManualAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddSearch);
			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);

			supplydetailsmovein.supplyAddComplexName.sendKeys("test complex");
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("24");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("287");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Gympie");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Terrace", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Noosaville");
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("4566");
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
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertEquals(complexName, "test complex", assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
					|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "24", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "287", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Gympie", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Terrace", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Noosaville", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4566", assertionErrorMsg(getLineNumber()));
			// verify that the Street Type is not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "287", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Gympie", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Wrong St Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Noosaville", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Wrong State", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "4566", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			// enter the Tenancy Type and Number
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("24");
			supplydetailsmovein.supplyAddStreetType.clear();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Terrace", Keys.TAB);
			supplydetailsmovein.supplyAddState.clear();
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
		clickElementAction(supplydetailsmovein.supplyAddSearch);
		clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);
		// verify that the address was cleared
		complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		// verify the fix for ticket BBPRTL-2087
		softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(stNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(stName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(stType), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(city), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(state), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(postcode), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
		slowSendKeys(supplydetailsmovein.supplyAddSearch, "Unit 24 287 Gympie TCE", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(supplydetailsmovein.supplyAddressesDiv, "unit 24/287 Gympie Terrace, Noosaville Queensland");
		pauseSeleniumExecution(1000);
		complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "24", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "287", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Gympie", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Terrace", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noosaville", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4566", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyUnknown);
		scrollPageDown(700);
		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(supplydetailsmovein.medCoolingNo);
		// upload life support files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Smaller file tiff file.tiff");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Smaller file tiff file .tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the life support equipment not in error state
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
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// verify it would be in error state
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		scrollPageDown(400);
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
		// verify all assertions
		softAssertion.assertAll();

		// choose other
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"));
		// verify it would be in error state
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		scrollPageDown(400);

		// verify the Other option is in error state
		String errorMsg = getDisplayedText(supplydetailsmovein.hintLifeSuppOtherTextField, true);
		softAssertion.assertEquals(errorMsg, "Specify the name of the equipment", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		supplydetailsmovein.lifeSuppOtherInput.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");

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
	 * - verify fields are not in error state initially - verify the section header
	 * name
	 * 
	 */
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

			// verify the section header
			String header = normalizeSpaces(
					getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
			softAssertion.assertEquals(header, "2 Account Details", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify ticked
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));

			// verify the section header
			String header = normalizeSpaces(
					getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
			softAssertion.assertEquals(header, "2 Account Details", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify the fields are not in error state initially - verify the
	 * introduction text - verify Provide None option not displayed in the Personal
	 * Identification - verify SMS checkbox is not displayed for Bills - verify the
	 * Postal checkbox is not displayed for Account Notifications and Reminders -
	 * verify the notification text labels - verify the notifications that should be
	 * ticked by default and not - verify that hovering into the icon would display
	 * the tooltip message of the notifications - verify all the required fields by
	 * clicking the Add Another Contact link - verify the validations when Passport
	 * option is selected - verify the section header name
	 * 
	 */
	@Test(priority = 3, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyMainContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify that radio button not ticked by default
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.medicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		String respForPayingText = getDisplayedText(mainaccountcontactmovein.lblResponsibleForPaying, true);
		softAssertion.assertEquals(respForPayingText,
				"Please provide the Property Manager contact who will be responsible for ensuring that the account is paid when it becomes due.",
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
		String acctNotifAndRemTooltipMsg = getDisplayedText(
				mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipMsg, true);
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.marketingComNotifTooltipIcon);
		String marketComTooltipMsg = getDisplayedText(mainaccountcontactmovein.marketingComNotifTooltipMsg, true);
		softAssertion.assertEquals(marketComTooltipMsg, "Marketing related communications",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// put an existing contact
		String firstNameExp = "tom tri";
		String lastNameExp = "ly";

		// verify url prefill values
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), firstNameExp,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), lastNameExp,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_upper_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "123456789",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "123456789",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false), "123456789",
					assertionErrorMsg(getLineNumber()));
			// verify phone numbers not in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

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
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields are in error state
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

			mainaccountcontactmovein.firstName.sendKeys("     tom      tri     ");
			mainaccountcontactmovein.lastName.sendKeys("     ly     ");
			// click outside to trigger an event
			clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
			// verify the spaces got normalized
			String firstNameAct = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String lastNameAct = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			softAssertion.assertEquals(firstNameAct, firstNameExp, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lastNameAct, lastNameExp, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields are in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
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
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		// verify the validations for Date of Birth
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		// get the current date and add 1 day
		String todayPlus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		// let's remove the current year then concatenate birthYr
		String invalidBirthDate = getString(todayPlus1, 0, todayPlus1.length() - 4);
		invalidBirthDate = invalidBirthDate + birthYr;
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(mainaccountcontactmovein.dateOfBirth);
		pauseSeleniumExecution(1000);
		mainaccountcontactmovein.dateOfBirth.sendKeys(invalidBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(mainaccountcontactmovein.contactSecretCode);
		String expectedDOB = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		scrollPageDown(400);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we're still in the main contact details section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the current date of birth value
		String dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		softAssertion.assertEquals(dateOfBirth, expectedDOB, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// initial click to resolve ElementNotInteractableException exception
		clearDateField(mainaccountcontactmovein.dateOfBirth);
		String todayMinus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -1, DATE_MONTH_YEAR_FORMAT_SLASH);
		birthYr = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 18);
		String validBirthDate = getString(todayMinus1, 0, todayMinus1.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		mainaccountcontactmovein.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(mainaccountcontactmovein.contactSecretCode);
		// verify we have a value on the Date of Birth
		dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		this.dateOfBirthMain = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.passport);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
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
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();

		// verify that we cannot enter special characters in the Passport number
		mainaccountcontactmovein.passportNumber.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		// verify that entering non-existent country will return an error
		mainaccountcontactmovein.passportCountry.sendKeys("Wakanda", Keys.TAB);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		String passportNum = getDisplayedValue(mainaccountcontactmovein.passportNumber, false);
		String passportCountry = getDisplayedValue(mainaccountcontactmovein.passportCountry, false);
		softAssertion.assertTrue(StringUtils.isBlank(passportNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isNotBlank(passportCountry), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		mainaccountcontactmovein.passportCountry.clear();

		// verify that the minimum number of passport number is 6, so we enter only 5
		// alpha numeric
		// and also verify we cannot enter spaces
		mainaccountcontactmovein.passportNumber.sendKeys("   A   b  c   1   2     ");
		// verify that a country entered in lower case will also return an error
		mainaccountcontactmovein.passportCountry.sendKeys("australia", Keys.TAB);
		passportNum = getDisplayedValue(mainaccountcontactmovein.passportNumber, false);
		softAssertion.assertEquals(passportNum, "Abc12", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.passportNumber);
		deleteAllTextFromField();
		clickElementAction(mainaccountcontactmovein.passportCountry);
		deleteAllTextFromField();

		// enter valid passport number
		mainaccountcontactmovein.passportNumber.sendKeys("   A   b  c   1   2   3  ");
		// enter valid passport country
		mainaccountcontactmovein.passportCountry.sendKeys("Australia", Keys.TAB);
		passportNum = getDisplayedValue(mainaccountcontactmovein.passportNumber, false);
		softAssertion.assertEquals(passportNum, "Abc123", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// used javaScriptClickButton() because for some reason if the
		// checkbox is in error state, checkbox is not clicked using clickButton()
		javaScriptClickElementAction(mainaccountcontactmovein.billsEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
			mainaccountcontactmovein.mobilePhone.sendKeys("+61469941139");
			mainaccountcontactmovein.businessPhone.sendKeys("+61460032240");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(mainaccountcontactmovein.mobilePhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.businessPhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.afterhoursPhone);
			deleteAllTextFromField();
			mainaccountcontactmovein.mobilePhone.sendKeys("+61469941139");
			mainaccountcontactmovein.businessPhone.sendKeys("+61460032240");
		}
		mainaccountcontactmovein.contactSecretCode.sendKeys("'sekrekts-01");

		// verify mobile and business phone not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Property Manager Account Contact")
						.getText());
		String expSectionHeader = concatStrings("3 Property Manager Account Contact (", firstNameExp, " ", lastNameExp,
				")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// verify we are in the in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields are not in error state initially - verify the Provide None
	 * option is not displayed - verify SMS checkbox is not displayed for Bills -
	 * verify the Postal checkbox is not displayed for Account Notifications and
	 * Reminders - verify the notification text labels - verify hovering over the
	 * icon would display the notification tooltip - verify the default
	 * notifications that are ticked and which are not - verify that clicking the
	 * Add Another Contact link validates the fields - verify the validations if
	 * Passport option is selected - verify the section header names
	 * 
	 */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// did this because for the Main Account Contact
		// the node name is also app-account-contact
		String wholeForm = getDisplayedText(additionalcontactmovein.wholeForm.get(1), true);
		// verify the fix for bug ticket BBPRTL-2044
		softAssertion.assertFalse(wholeForm.contains(
				"Please provide the Property Manager contact who will be responsible for ensuring that the account is paid when it becomes due."),
				assertionErrorMsg(getLineNumber()));
		wholeForm = wholeForm.toLowerCase();
		softAssertion.assertFalse(wholeForm.contains(
				"please provide the property manager contact who will be responsible for ensuring that the account is paid when it becomes due."),
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
		String billsNotifText = getDisplayedText(additionalcontactmovein.addCont1LblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(additionalcontactmovein.addCont1LblAcctnotifAndRemindersNotif,
				true);
		String marketComNotifText = getDisplayedText(additionalcontactmovein.addCont1LblMarketingComNotif, true);
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
		String billsTooltipMsg = getDisplayedText(additionalcontactmovein.addCont1BillsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipIcon);
		String acctNotifAndRemTooltipMsg = getDisplayedText(
				additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipMsg, true);
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont1MarketingComNotifTooltipIcon);
		String marketComTooltipMsg = getDisplayedText(additionalcontactmovein.addCont1MarketingComNotifTooltipMsg,
				true);
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

		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
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

		// let's put an existing contact
		String firstNameExp = "Brad";
		String lastNameExp = "Harrison";
		additionalcontactmovein.addCont1FirstName.sendKeys("   Brad  ");
		additionalcontactmovein.addCont1LastName.sendKeys("    Harrison   ");

		clickElementAction(additionalcontactmovein.addCont1Passport);
		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
		pauseSeleniumExecution(1000);
		// verify the spaces got removed
		String firstNameAct = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
		String lastNameAct = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
		softAssertion.assertEquals(firstNameAct, firstNameExp, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastNameAct, lastNameExp, assertionErrorMsg(getLineNumber()));
		// verify the fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
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

		// verify that we cannot enter special characters in the Passport number
		additionalcontactmovein.addCont1PassportNumber.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		// verify that entering non-existent country will return an error
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Wakanda", Keys.TAB);
		String passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false);
		softAssertion.assertTrue(StringUtils.isBlank(passportNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1PassportCountry);
		deleteAllTextFromField();

		// verify that the minimum number of passport number is 6, so we enter only 5
		// alpha numeric
		// and also verify we cannot enter spaces
		additionalcontactmovein.addCont1PassportNumber.sendKeys("   A   b  c   1   2     ");
		// verify that a country entered in lower case will also return an error
		additionalcontactmovein.addCont1PassportCountry.sendKeys("australia", Keys.TAB);
		passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false);
		softAssertion.assertEquals(passportNum, "Abc12", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1PassportNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont1PassportCountry);
		deleteAllTextFromField();

		// enter valid passport number
		additionalcontactmovein.addCont1PassportNumber.sendKeys("   D   f  g   8   9   0  ");
		// enter valid passport country
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Australia", Keys.TAB);
		passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false);
		softAssertion.assertEquals(passportNum, "Dfg890", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("61231132240");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("61391132260");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("61451532290");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("'sekrekts-02");

		// verify in phone numbers in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1MobilePhone.clear();
		additionalcontactmovein.addCont1BusinessPhone.clear();
		additionalcontactmovein.addCont1AfterhoursPhone.clear();
		// enter valid numbers
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+61231132240");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("+61391132260");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+61451532290");
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);

		// verify in phone numbers not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String actSectionHeader = normalizeSpaces(getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader,
				"Additional Contact (".concat(firstNameExp)).getText());
		String expSectionHeader = concatStrings("4 Additional Contact (", firstNameExp, " ", lastNameExp, ")");
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

		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the postal address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not yet in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields are not in error state initially - verify the section header
	 * name
	 * 
	 */
	@Test(priority = 5, dependsOnMethods = { "verifyAdditionalContact" })
	public void verifyPostalAddress() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.sameSupAddressNo);
		clickElementAction(postaladdressmovein.postalAddSearch);
		clickElementAction(postaladdressmovein.postalAddCantFindAdd);
		postaladdressmovein.addLine01.sendKeys("Address - #01");
		postaladdressmovein.addLine02.sendKeys("Address - #02");
		postaladdressmovein.addLine03.sendKeys("Address - #03");
		postaladdressmovein.addLine04.sendKeys("Address - #04");
		postaladdressmovein.city.sendKeys("Baltigo");
		postaladdressmovein.state.sendKeys("East Blue");
		postaladdressmovein.postcode.sendKeys("6501");
		postaladdressmovein.country.sendKeys("Philippines", Keys.TAB);

		clickElementAction(postaladdressmovein.postalAddQuickAddSearch);
		clickElementAction(postaladdressmovein.postalAddSearch);
		clickElementAction(postaladdressmovein.postalAddCantFindAdd);
		// verify the address was cleared
		String add01 = getDisplayedValue(postaladdressmovein.addLine01, false);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, false);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, false);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, false);
		String city = getDisplayedValue(postaladdressmovein.city, false);
		String state = getDisplayedValue(postaladdressmovein.state, false);
		String postcode = getDisplayedValue(postaladdressmovein.postcode, false);
		String country = getDisplayedValue(postaladdressmovein.country, false);
		softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add02), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(city), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(state), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(postcode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(country), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.sameSupAddressYes);
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Postal Address").getText());
		softAssertion.assertEquals(header, "5 Postal Address", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields not in error state initially
	 * 
	 */
	@Test(priority = 6, dependsOnMethods = { "verifyPostalAddress" })
	public void verifyConcessionDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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

		clickElementAction(concessionmovein.addConcessionYes);
		// verify the fix for ticket BBPRTL-647
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

		concessionmovein.cardHolderName.sendKeys("Nick Fury");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 2);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		softAssertion.assertEquals(typeChosen, "DVA Gold Card", assertionErrorMsg(getLineNumber()));
		concessionmovein.cardNumber.sendKeys("006486786400");
		// verify the expiry is not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// let's upload concession card details
		uploadConcessionFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(60000);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Concession ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		String dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Concession").getText());
		softAssertion.assertEquals(header, "6 Concession", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(700);
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Holiday Letting/Rental Company Details
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We are not in the Holiday Letting/Rental Company Details");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify the intro message - verify the fields are not in error state
	 * initially - verify fix for bug ticket BBPRTL-880 - verify clicking the
	 * Previous button would redirect us into the previous section
	 * 
	 */
	@Test(priority = 7, dependsOnMethods = { "verifyConcessionDetails" })
	public void verifyHolidayLettingDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify the intro message
		String introMsg = getDisplayedText(managerholidaylettingmovein.lblPropManHolidayLettingIntro, true);
		softAssertion.assertEquals(introMsg,
				"Please specify the property company via which the property is being managed",
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the fields are in error state
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the fix for ticket BBPRTL-880
		clickElementAction(managerholidaylettingmovein.companyName);
		clickElementAction(managerholidaylettingmovein.companyAddress);
		clickElementAction(managerholidaylettingmovein.cannotFindAdd);
		managerholidaylettingmovein.address01.sendKeys("Add-01");
		managerholidaylettingmovein.address02.sendKeys("Add-02");
		managerholidaylettingmovein.address03.sendKeys("Add-03");
		managerholidaylettingmovein.address04.sendKeys("Add-04");
		managerholidaylettingmovein.city.sendKeys("City/Suburb");
		managerholidaylettingmovein.state.sendKeys("State");
		managerholidaylettingmovein.postCode.sendKeys("90210");
		managerholidaylettingmovein.country.sendKeys("Australia", Keys.TAB);

		scrollPageDown(500);
		// verify we can go to the previous section by clicking the button
		clickElementAction(managerholidaylettingmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");

		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Holiday Letting/Rental Company Details")
						.getText());
		softAssertion.assertEquals(header, "7 Holiday Letting/Rental Company Details",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.companyName.sendKeys("Mr. Michael O'Connell");
		managerholidaylettingmovein.companyContactNum.sendKeys("+654980654840");

		clickElementAction(managerholidaylettingmovein.quickAddSearch);
		clickElementAction(managerholidaylettingmovein.companyAddress);
		clickElementAction(managerholidaylettingmovein.cannotFindAdd);
		// verify the address got cleared
		String add01 = getDisplayedValue(managerholidaylettingmovein.address01, false);
		String add02 = getDisplayedValue(managerholidaylettingmovein.address02, false);
		String add03 = getDisplayedValue(managerholidaylettingmovein.address03, false);
		String add04 = getDisplayedValue(managerholidaylettingmovein.address04, false);
		String city = getDisplayedValue(managerholidaylettingmovein.city, false);
		String state = getDisplayedValue(managerholidaylettingmovein.state, false);
		String postcode = getDisplayedValue(managerholidaylettingmovein.postCode, false);
		String country = getDisplayedValue(managerholidaylettingmovein.country, false);
		softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add02), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(city), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(state), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(postcode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(country), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(managerholidaylettingmovein.quickAddSearch);
		slowSendKeys(managerholidaylettingmovein.companyAddress, "U40 272 Weyba Road Noosaville", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(managerholidaylettingmovein.companyAddressesDiv, "u40/272 Weyba Road, Noosaville Queensland");
		pauseSeleniumExecution(1000);
		// verify the fields are populated correctly
		// blank add 03 and add 04
		add01 = getDisplayedValue(managerholidaylettingmovein.address01, true);
		add02 = getDisplayedValue(managerholidaylettingmovein.address02, true);
		add03 = getDisplayedValue(managerholidaylettingmovein.address03, true);
		add04 = getDisplayedValue(managerholidaylettingmovein.address04, true);
		city = getDisplayedValue(managerholidaylettingmovein.city, true);
		state = getDisplayedValue(managerholidaylettingmovein.state, true);
		postcode = getDisplayedValue(managerholidaylettingmovein.postCode, true);
		country = getDisplayedValue(managerholidaylettingmovein.country, true);
		softAssertion.assertEquals(add01, "U40", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "272 Weyba Road", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Noosaville", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4566", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyContactNum, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		// verify we're in the next section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0),
				"We're not yet in the Direct Debit details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields are not initially in error state - verify fix for bug ticket
	 * BBPRTL-996 - verify that when we enter credit card then all required fields
	 * are ticked, it would go to the next section correctly
	 * 
	 */
	@Test(priority = 8, dependsOnMethods = { "verifyHolidayLettingDetails" })
	public void verifyDirectDebitDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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

		clickElementAction(directdebitmovein.bankAccount);
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

		// verify the bug fix in the comment for ticket BBPRTL-996
		clickElementAction(directdebitmovein.authorisationBankAccount);
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the bug fix in the comment for ticket BBPRTL-996
		clickElementAction(additionalnotemovein.header);
		waitForScreenToRender();
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCard);
		moveInDirectDebitCCProgBarLoad();
		// put a pause to make sure all fields and images are loaded
		pauseSeleniumExecution(2000);
		clickElementAction(directdebitmovein.authorisationCreditCard);
		clickElementAction(additionalnotemovein.header);
		waitForScreenToRender();
		// verify the fields are in error state
		switchToMWIframe();
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
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				"Credit Card authorization is not in error state");
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go back to the previous section using header
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We are not yet in the Holiday Letting Details section");
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);

		// verify the fields are still in error state
		switchToMWIframe();
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
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				"Credit Card authorization is not in error state");
		// verify all assertions
		softAssertion.assertAll();

		switchToMWIframe();
		// enter credit card details
		directdebitmovein.creditCardName.sendKeys("Tony Stark Jr.");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_07"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 10;
		String monthStr = Integer.toString(month);
		String expYrStrFull = Integer.toString(expYr);
		String expYrStr = getString(expYrStrFull, 2, 4);
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
		// verify that the authorisation checkbox is ticked already
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Direct Debit").getText());
		softAssertion.assertEquals(header, "8 Direct Debit", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// go to the next section
		clickElementAction(additionalnotemovein.header);
		moveInDirectDebitCCProgBarLoad();
		waitForScreenToRender();
		// verify we are in the Additional notes section
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Notes section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify that we can enter only spaces and it would update the counter
	 * 
	 */
	@Test(priority = 9, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAdditionalNote() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify it's not required so we can hit header of next section
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are in the acceptance page section
		String reviewText = getDisplayedText(acceptancemovein.lblAcceptanceIntro, true);
		assertEquals(reviewText,
				"You are almost finished, thank you for your patience. Please review the below details before submitting the form",
				"We are not yet in the Acceptance section");

		scrollPageUp(600);
		clickElementAction(additionalnotemovein.header);
		waitForScreenToRender();
		// verify we are back in the Additional Notes section
		assertTrue(isElementDisplayed(additionalnotemovein.lblEnterNotes, 0),
				"We are not yet in the Additional Notes section");

		// verify we can go back to the previous section
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Direct Debit section
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0),
				"We are not yet in the Direct Debit details section");
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);

		// verify the introduction message
		String intro = getDisplayedText(additionalnotemovein.lblEnterNotes, true);
		softAssertion.assertEquals(intro,
				"Please enter any special notes that you wish to pass onto our customer service staff",
				assertionErrorMsg(getLineNumber()));
		// enter the additional notes as only 10 white spaces
		additionalnotemovein.notesArea.sendKeys("          ");
		String counter = getDisplayedText(additionalnotemovein.notesLengthCounter, true);
		softAssertion.assertEquals(counter, "10/256", assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Additional Note").getText());
		softAssertion.assertEquals(header, "9 Additional Note", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the acceptance page now
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify each line if correct values - verify we can update the life support
	 * and attach files - verify we can update the discharge information and attach
	 * files - verify we can update the concession card and ensure that the previous
	 * date is not sent into the API
	 * 
	 */
	@Test(priority = 10, dependsOnMethods = { "verifyAdditionalNote" })
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
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String postalAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String holidayLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String additionalNotes = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(
				movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
						" (Settlement on ", this.ownerSettlementDate, ") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566 Service connection currently unknown",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Medical Cooling NOT REQUIRED Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Property Manager Account Contact update tom tri ly Email Address: ",
						getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61469941139 Business Phone: +61460032240 Birthdate: ", this.dateOfBirthMain,
						" Personal Id: Passport (Abc123, Australia) Contact Secret: ('sekrekts-01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Property Manager Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Brad Harrison Email Address: ",
				getProp("test_dummy_email_upper_case"),
				" Mobile Phone: +61231132240 Business Phone: +61391132260 A/Hours Phone: +61451532290 Personal Id: Passport (Dfg890, Australia) Contact Secret: ('sekrekts-02)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Nick Fury DVA Gold Card 006486786400 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"Holiday Letting / Rental Company update Mr. Michael O'Connell +654980654840 U40 272 Weyba Road Noosaville, Queensland, 4566 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Tony Stark Jr. Card: ending ",
						getProp("test_data_08"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we go to the supply details section
		clickExactLinkNameFromElement(acceptancemovein.lifeSupportRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), "We're not yet in the Supply Details section");
		scrollPageDown(400);
		clickElementAction(supplydetailsmovein.lifeSupNo);
		clickElementAction(supplydetailsmovein.medCoolingYes);
		pauseSeleniumExecution(500);
		// verify that the previous uploaded file is still there
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Smaller file tiff file.tiff .image/tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		String fileToUpload = "Smaller file tiff file.tiff";
		// upload the same file again
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, fileToUpload);
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(3000);
		// verify that an error is displayed
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.dragAndDropDuplicateError, true), concatStrings(
				fileToUpload,
				" cannot be uploaded as they already are present. Please remove these existing files and upload again"),
				assertionErrorMsg(getLineNumber()));
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Acceptance ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
		}
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Smaller file tiff file.tiff .image/tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(800);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// just verify that we have correct session keys
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 36, assertionErrorMsg(getLineNumber()));
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
		String sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAcctDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAdditionalContact = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionCard = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionApplicationID = storage.getItemFromSessionStorage("application_id");
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAcctDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAdditionalContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionCard), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionApplicationID), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceID), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
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

		scrollPageDown(900);
		// verify we go to the concession section
		clickExactLinkNameFromElement(acceptancemovein.concessionRow, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the concession section
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0), "We're not yet in the Concession section");
		// choose No
		clickElementAction(concessionmovein.addConcessionNo);
		pauseSeleniumExecution(500);
		// verify the fields got hidden
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardHolderNameList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.typeOfConcessionCardList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
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
		postalAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		concession = getDisplayedText(acceptancemovein.concessionRow, true);
		holidayLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		additionalNotes = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(
				movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
						" (Settlement on ", this.ownerSettlementDate, ") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566 Service connection currently unknown",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Property Manager Account Contact update tom tri ly Email Address: ",
						getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61469941139 Business Phone: +61460032240 Birthdate: ", this.dateOfBirthMain,
						" Personal Id: Passport (Abc123, Australia) Contact Secret: ('sekrekts-01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Property Manager Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Brad Harrison Email Address: ",
				getProp("test_dummy_email_upper_case"),
				" Mobile Phone: +61231132240 Business Phone: +61391132260 A/Hours Phone: +61451532290 Personal Id: Passport (Dfg890, Australia) Contact Secret: ('sekrekts-02)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession, "Concession update None Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"Holiday Letting / Rental Company update Mr. Michael O'Connell +654980654840 U40 272 Weyba Road Noosaville, Queensland, 4566 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Tony Stark Jr. Card: ending ",
						getProp("test_data_08"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Acceptance").getText());
		softAssertion.assertEquals(header, "10 Acceptance", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify the session keys and ensure values are not empty - verify the local
	 * keys and ensure values are not empty - refresh the browser then submit the
	 * request
	 * 
	 */
	@Test(priority = 11, dependsOnMethods = { "verifyAcceptanceDetails" })
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 35, assertionErrorMsg(getLineNumber()));
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
		String sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAcctDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAdditionalContact = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionCard = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionApplicationID = storage.getItemFromSessionStorage("application_id");
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAcctDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAdditionalContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionCard), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionApplicationID), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceID), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		this.sourceID = sessionSourceID;
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
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

		// let's confirm the keys in the session storage again
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 35, assertionErrorMsg(getLineNumber()));
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
		sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		sessionAcctDetails = storage.getItemFromSessionStorage("move-in.account_details");
		sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		sessionAdditionalContact = storage.getItemFromSessionStorage("move-in.additional_contact");
		sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		sessionConcessionCard = storage.getItemFromSessionStorage("move-in.concession_card");
		sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		sessionApplicationID = storage.getItemFromSessionStorage("application_id");
		sessionSourceID = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAcctDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAdditionalContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionCard), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionApplicationID), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSourceID), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			String sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			String sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMobNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryBusNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAfterHrNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		this.sourceID = sessionSourceID;
		// let's confirm the values stored in the local storage
		localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1000);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0), assertionErrorMsg(getLineNumber()));

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
		String postalAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String holidayLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String additionalNotes = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(
				movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
						" (Settlement on ", this.ownerSettlementDate, ") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566 Service connection currently unknown",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Property Manager Account Contact update tom tri ly Email Address: ",
						getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61469941139 Business Phone: +61460032240 Birthdate: ", this.dateOfBirthMain,
						" Personal Id: Passport (Abc123, Australia) Contact Secret: ('sekrekts-01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Property Manager Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Brad Harrison Email Address: ",
				getProp("test_dummy_email_upper_case"),
				" Mobile Phone: +61231132240 Business Phone: +61391132260 A/Hours Phone: +61451532290 Personal Id: Passport (Dfg890, Australia) Contact Secret: ('sekrekts-02)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Unit 24, 287 Gympie Terrace Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession, "Concession update None Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"Holiday Letting / Rental Company update Mr. Michael O'Connell +654980654840 U40 272 Weyba Road Noosaville, Queensland, 4566 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Tony Stark Jr. Card: ending ",
						getProp("test_data_08"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));

		// verify the element are not ticked
		softAssertion.assertFalse(isElementTicked(acceptancemovein.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1000);
		clickElementAction(acceptancemovein.firstCheckbox);
		if (getPortalType().equals("standalone")) {
			// we use javaScriptClickButton to click it because the method
			// clickButton mistakenly clicks the link which opens a new tab in standalone
			javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		} else if (getPortalType().equals("embedded")) {
			clickElementAction(acceptancemovein.secondCheckbox);
		}
		// verify the 1st and 2nd checkboxes are ticked
		softAssertion.assertTrue(isElementTicked(acceptancemovein.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(acceptancemovein.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));

		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 4, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// add the property files before submitting the request
		addProp("ResiExistingContact02_ownerSettlementDate", this.ownerSettlementDate);
		addProp("ResiExistingContact02_ownerMoveInDateCRM", this.ownerMoveInDateCRM);
		addProp("ResiExistingContact02_ownerSettlementDateCRM", this.ownerSettlementDateCRM);
		addProp("ResiExistingContact02_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiExistingContact02_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiExistingContact02_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiExistingContact02_sourceID", this.sourceID);
		addProp("ResiExistingContact02_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiExistingContact02_dateSubmittedDash",
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