package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
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
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.ManagerHolidayLettingMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class SmallBusNewContact01 extends MoveInDevBase {

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
	 * 
	 * */
	String ownerMoveInDate;

	/** 
	 * 
	 * */
	String ownerMoveInDateCRM;

	/** 
	 * 
	 * */
	String mainContactFirstName;

	/** 
	 * 
	 * */
	String mainContactLastName;

	/** 
	 * 
	 * */
	String tradeWasteDischargeDate;

	/**
	 * The source id value
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

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "02\\", "portal_config.json");

		// remove the previous lang files
		deleteMoveInCustomLangFiles(s3Access, "custom_en.json", "custom_fil.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "08\\", "custom_EN.json");

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
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("20", "Bella Vista", StreetTypesEnum.ST, "Heathcote", AustralianStatesEnum.NSW,
					"2233", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=", "&account_category=",
					AccountCategoryEnum.tEnAnT.name(), "&move_in_date=", moveInDate, "&tenancy_type=",
					TenancyTypesEnum.aPT.name(), "&tenancy_number=", "&tenancy_street_number=", "&tenancy_street_name=",
					"&tenancy_street_type=", StreetTypesEnum.sT.getLabelText(), "&tenancy_suburb=",
					"&tenancy_postcode=", "&tenancy_state=", "&account_type=", AccountTypesEnum.sMAll_BUSiNeSS.name(),
					"&business_number=", getProp("test_data_valid_acn4"),
					"&business_trading_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&contact_first_name=Glenn",
					"&contact_last_name=O'brien", "&mobile_number=+61288500321 ", "&business_hour_phone=+61311300856 ",
					"&after_hour_phone=+61401331008 ", "&email_address=", "email test@testing.com",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=", "&account_category=",
					AccountCategoryEnum.tEnAnT.name(), "&move_in_date=", moveInDate, "&tenancy_type=",
					TenancyTypesEnum.aPT.name(), "&tenancy_number=", "&tenancy_street_number=", "&tenancy_street_name=",
					"&tenancy_street_type=", StreetTypesEnum.sT.getLabelText(), "&tenancy_suburb=",
					"&tenancy_postcode=", "&tenancy_state=", "&account_type=", AccountTypesEnum.sMAll_BUSiNeSS.name(),
					"&business_number=", getProp("test_data_valid_acn4"),
					"&business_trading_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&contact_first_name=Glenn",
					"&contact_last_name=O'brien", "&mobile_number=+61288500321 ", "&business_hour_phone=+61311300856 ",
					"&after_hour_phone=+61401331008 ", "&email_address=", "email test@testing.com",
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
	public void verifySupplyDetails() {

		// let's switch to the Move-In Iframe
		// if it's embedded
		embeddedMoveInSwitchFrame(1);
		// make sure that the elements are now displayed
		waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
				PORTAL_IMPLICIT_WAIT_TIMEOUT);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// we are testing here that getting the language file is done in lower case
		// and the upload custom language file is in upper case
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
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
			// verify the immediately displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Trade Waste", "4 Main Account Contact",
							"5 Postal Address", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			// verify the values populated
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(stNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(stName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(city), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(state), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(postcode), assertionErrorMsg(getLineNumber()));
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
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.owner);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clearDateField(supplydetailsmovein.moveInDateOwner);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.owner);
			// verify no date is added
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		String future12Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future12D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		supplydetailsmovein.moveInDateOwner.sendKeys(future12Days, Keys.TAB);
		this.ownerMoveInDate = future12Days;
		this.ownerMoveInDateCRM = future12D;
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.whoIsResponsiblePropMan);
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Property Manager Account Contact", "4 Postal Address",
					"5 Holiday Letting/Rental Company Details", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Trade Waste", "4 Property Manager Account Contact", "5 Postal Address",
					"6 Holiday Letting/Rental Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			// verify the fix for bug ticket BBPRTL-1988
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Property Manager Account Contact (Glenn O'brien)", "4 Postal Address",
					"5 Holiday Letting/Rental Company Details", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.tenant);
		pauseSeleniumExecution(500);
		// verify that the Who is responsible fields got hidden
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblWhoIsResponsibleList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
							"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Trade Waste", "4 Main Account Contact",
							"5 Postal Address", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			// verify the fix for bug ticket BBPRTL-1988
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact (Glenn O'brien)",
							"4 Postal Address", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.owner);
		pauseSeleniumExecution(500);
		// verify move in date was not cleared
		softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, true), future12Days,
				assertionErrorMsg(getLineNumber()));
		// verify nothing is ticked
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
							"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Trade Waste", "4 Main Account Contact",
							"5 Postal Address", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify also the expected section headers
			List<String> actualSectionNames = getAllSectionNames(true);
			// verify the fix for bug ticket BBPRTL-1988
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact (Glenn O'brien)",
							"4 Postal Address", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.ownerSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Tenancy", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "20", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "20", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Bella Vista", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Heathcote", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "New South Wales", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "2233", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "20", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Bella Vista", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Heathcote", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "New South Wales", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "2233", assertionErrorMsg(getLineNumber()));

			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			// enter the Tenancy Type and Number
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify fields in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
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

			// enter the supply address
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();

			supplydetailsmovein.supplyAddTenancyType.sendKeys("Tenancy");
			clickElementAction(supplydetailsmovein.supplyAddTenancyNum);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("20");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Bella Vista");
			supplydetailsmovein.supplyAddCity.sendKeys("Heathcote");
			supplydetailsmovein.supplyAddState.sendKeys("New South Wales");
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
			supplydetailsmovein.supplyAddPostcode.sendKeys("2233");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
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
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
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
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg", "typing jim carrey.gif",
				"Sprin't 02 Story 'Board.pdf");
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.dragAndDropCountExceedError, true),
				"Unable to upload all requested files as maximum allowable files is 1",
				assertionErrorMsg(getLineNumber()));
		waitForFilesToBeUploaded(10000);
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Supply Details ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
			// verify files were not uploaded
//			softAssertion.assertEquals(actualSize, 2,
//					"Incorrect number of objects inside the bucket '".concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// clear the files to be uploaded
		// before uploading new one
		clearLifeSupUploadFiles(3);
		// upload life support and medical cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg");

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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
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
			accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_acn4"), Keys.TAB);
			accountdetailsmovein.tradingName.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_acn4"), Keys.TAB);
			accountdetailsmovein.tradingName.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
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
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.commercial);
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			// verify the values displayed
			String abnAcn = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			String tradingName = getDisplayedValue(accountdetailsmovein.tradingName, false);
			softAssertion.assertEquals(abnAcn,
					concatStrings(getProp("test_data_valid_acn4"), " (",
							getProp("test_data_valid_company_name_acn3_acn4"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tradingName, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
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

		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				"We are not yet in the Trade Waste section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields not in error state initially - verify the initial fields are
	 * hidden - click Next button then verify the required fields - click Previous
	 * button then verify you are in the Supply Details section - when moving back
	 * into the Trade Waste section from Supply Details, verify that the fields are
	 * no longer in error state - verify the header displayed for the section
	 * 
	 */
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
		assertEquals(maxFlow, "10000.279", "The max flow rate value is not correct");

		tradewastemovein.maxDischargeVolume.sendKeys("11,000.389");
		String maxDischarge = getDisplayedValue(tradewastemovein.maxDischargeVolume, false);
		assertEquals(maxDischarge, "11000.389", "The max discharge volume is not correct");

		String dischargeStartDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -366,
				DATE_MONTH_YEAR_FORMAT_SLASH);
		this.tradeWasteDischargeDate = dischargeStartDate;
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

		scrollPageDown(500);
		clickElementAction(tradewastemovein.lblDischargeHoursStart);
		// upload trade waste files
		uploadTradeWasteFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf");
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
		// verify the files that were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(getElementFrmMatStepHdrTag(tradewastemovein.matStepHeader, "Trade Waste"),
				true);
		softAssertion.assertEquals(header, "3 Trade Waste", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageUp(400);
		clickElementAction(tradewastemovein.tradeWasteDischargeNo);
		clickElementAction(tradewastemovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
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
		softAssertion.assertEquals(billsNotifText, "info Bills (additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders(no additional payments)", assertionErrorMsg(getLineNumber()));
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

		String firstName = "Glenn";
		String lastName = "O'brien";
		// verify the url prefill
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), firstName,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), lastName,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					"emailtest@testing.com", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "+61288500321",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "+61311300856",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false),
					"+61401331008", assertionErrorMsg(getLineNumber()));
			// verify phone numbers not in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.emailAddress);
			deleteAllTextFromField();
		}

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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify Postal Address section is still displayed
			// since Postal is still ticked in the Additional Contact
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details",
					"create Account Details", "create Trade Waste", "4 Main Account Contact (Glenn O'brien)",
					"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
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
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// verify Email Address becomes required if a notification is ticked
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		// verify Mobile Phone Number becomes required if a notification is ticked
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComSMS);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.contactSecretCode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		this.mainContactFirstName = firstName;
		this.mainContactLastName = lastName;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.firstName.sendKeys(firstName);
			mainaccountcontactmovein.lastName.sendKeys(lastName);
			mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
			mainaccountcontactmovein.mobilePhone.sendKeys("+61288500321");
			mainaccountcontactmovein.businessPhone.sendKeys("+61311300856");
			mainaccountcontactmovein.afterhoursPhone.sendKeys("+61401331008");

			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
					"We are not yet in the 1st Additional Contact section");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_upper_case"));

			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
					"We are not yet in the 1st Additional Contact section");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact01() {

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
		String billsNotifText = getDisplayedText(additionalcontactmovein.addCont1LblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(additionalcontactmovein.addCont1LblAcctnotifAndRemindersNotif,
				true);
		String marketComNotifText = getDisplayedText(additionalcontactmovein.addCont1LblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills (additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info Account Notifications and Reminders(no additional payments)", assertionErrorMsg(getLineNumber()));
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

		// verify Postal Address section is displayed
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
						concatStrings("create Main Account Contact (", this.mainContactFirstName, " ",
								this.mainContactLastName, ")"),
						"5 Additional Contact", "6 Postal Address", "7 Direct Debit", "8 Additional Note",
						"9 Acceptance"));
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
				"create Trade Waste", concatStrings("create Main Account Contact (", this.mainContactFirstName, " ",
						this.mainContactLastName, ")"),
				"5 Additional Contact", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BillsPostal, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0, 3),
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
		// verify all assertions
		softAssertion.assertAll();

		// verify Email Address becomes required if notification is ticked
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		// verify Mobile Phone becomes required if notification is ticked
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1FirstName.sendKeys("Roshan");
		additionalcontactmovein.addCont1LastName.sendKeys("Britto");
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+61288500321");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("+61311300856");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+451267286623");

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");

		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComPostal);

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are redirected automatically in the Postal Address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact01" })
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
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.sameSupAddressYes);
		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	@Test(priority = 7, dependsOnMethods = { "verifyPostalAddress" })
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
		// verify fields are in error state since we accessed this section
		// from the additional contact
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.noDirectDebit, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);

		String name = concatStrings(this.mainContactFirstName, " ", this.mainContactLastName);
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
		softAssertion.assertEquals(getDisplayedValue(directdebitmovein.bankAccountName, false), "Glenn Obrien",
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
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * **/
	@Test(priority = 8, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAdditionalNote() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		additionalnotemovein.notesArea.sendKeys("The quick brown foxs jumps over the lazy dog.");
		softAssertion.assertEquals(getDisplayedValue(additionalnotemovein.notesArea, false).length(), 20,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.notesLengthCounter, true), "20/20",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 9, dependsOnMethods = { "verifyAdditionalNote" })
	public void verifyAcceptanceDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify each section
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Tenancy 20, 20 Bella Vista Street Heathcote, New South Wales, 2233 Service currently connected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Intermittent Peritoneal Dialysis Machine Haemodialysis Machine Chronic Positive Airways Pressure Respirator Crigler Najjar Syndrome Phototherapy Equipment Ventilator for Life Support Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_acn3_acn4"),
						" (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./) ABN/ACN ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradeWaste, "Trade Waste update No trade waste will be discharged",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Glenn O'brien Email Address: ",
						getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61288500321 Business Phone: +61311300856 A/Hours Phone: +61401331008"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (Postal, Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Roshan Britto Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61288500321 Business Phone: +61311300856 A/Hours Phone: +451267286623"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (Email) Notifications and Reminders (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"Postal Address update Tenancy 20, 20 Bella Vista Street Heathcote, New South Wales, 2233",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Glenn Obrien BSB: 000100 / Num: 001000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update The quick brown foxs",
				assertionErrorMsg(getLineNumber()));
		// verify Concession is not displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify we go to supply details
		clickExactLinkNameFromElement(acceptancemovein.lifeSupportRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.lifeSupNo);
		clickElementAction(supplydetailsmovein.dragAndDropArea);
		clickElementAction(supplydetailsmovein.medCoolingNo);
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingNo, 0),
				assertionErrorMsg(getLineNumber()));
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementDisplayed(supplydetailsmovein.dragAndDropArea, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1000);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify the fix for bug ticket BBPRTL-1468
		// verify we to go to trade waste
		clickExactLinkNameFromElement(acceptancemovein.tradeWasteRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(tradewastemovein.tradeWasteDischargeYes);
		// verify radio not ticked
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify there are no values
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedText(tradewastemovein.businessActivity, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.maxFlowRate, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.maxDischargeVolume, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.dischargeStartDate, false)),
				assertionErrorMsg(getLineNumber()));
		// verify checkboxes no longer ticked
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isMatPseudoChckbxTicked(
								getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 0),
						assertionErrorMsg(getLineNumber()));
		// verify there are no values
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(tradewastemovein.dischargeHoursStartHour, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.dischargeHoursStartMin, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.dischargeHoursEndHour, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(tradewastemovein.dischargeHoursEndMin, false)),
				assertionErrorMsg(getLineNumber()));
		// verify the files that were uploaded is no longer there
		String dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload ",
				assertionErrorMsg(getLineNumber()));
		clickElementAction(tradewastemovein.tradeWasteEquipYes);
		// verify the checkboxes are no longer ticked
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
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(tradewastemovein.tradeWasteOtherTextField, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's put a trade waste
		clickElementAction(tradewastemovein.tradeWasteEquipNo);
		clickElementAction(tradewastemovein.businessActivity);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(tradewastemovein.businessActivityDiv, 3);
		chooseFromList(tradewastemovein.businessActivityDiv, 2);
		pauseSeleniumExecution(1000);
		scrollPageDown(1000);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we go to the main contact details
		clickExactLinkNameFromElement(acceptancemovein.mainContactNotifRow, "update");
		pauseSeleniumExecution(1000);
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
						"4 Main Account Contact (Glenn O'brien)", "create Additional Contact (Roshan Britto)",
						"create Postal Address", "create Direct Debit", "create Additional Note", "create Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComPostal);
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the postal address section got removed
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
						"4 Main Account Contact (Glenn O'brien)", "create Additional Contact (Roshan Britto)",
						"create Direct Debit", "create Additional Note", "create Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(800);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
		// verify we go to Direct Debit
		clickExactLinkNameFromElement(acceptancemovein.directDebitRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(directdebitmovein.noDirectDebit);
		scrollPageDown(800);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
		// verify we go to additional note
		clickExactLinkNameFromElement(acceptancemovein.additionalNoteRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0), "We are not in the Additional Note section");
		clickElementAction(additionalnotemovein.notesArea);
		deleteAllTextFromField();
		additionalnotemovein.notesArea.sendKeys("          ");
		scrollPageDown(600);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify each section
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Tenancy 20, 20 Bella Vista Street Heathcote, New South Wales, 2233 Service currently connected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life Support NOT REQUIRED Medical Cooling NOT REQUIRED",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_acn3_acn4"),
						" (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./) ABN/ACN ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradeWaste, concatStrings(
				"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Retail motor vehicle'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo,
				"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' No site plans uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Glenn O'brien Email Address: ",
						getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61288500321 Business Phone: +61311300856 A/Hours Phone: +61401331008"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Roshan Britto Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61288500321 Business Phone: +61311300856 A/Hours Phone: +451267286623"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (Email) Notifications and Reminders (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify Postal Address no longer displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
				assertionErrorMsg(getLineNumber()));
		// verify Concession is not displayed
		softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 10, dependsOnMethods = { "verifyAcceptanceDetails" })
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

		// let's confirm the session and local storage
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 13, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 16, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 29, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		String sessionSourceID = storage.getItemFromSessionStorage("source_id");
		this.sourceID = sessionSourceID;

		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 9, "Incorrect number of objects inside the bucket '"
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
		addProp("SmallBusNewContact01_ownerMoveInDate", this.ownerMoveInDate);
		addProp("SmallBusNewContact01_ownerMoveInDateCRM", this.ownerMoveInDateCRM);
		addProp("SmallBusNewContact01_sourceID", this.sourceID);
		addProp("SmallBusNewContact01_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("SmallBusNewContact01_dateSubmittedDash",
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