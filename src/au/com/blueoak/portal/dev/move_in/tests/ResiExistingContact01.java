package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
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
import au.com.blueoak.portal.pageObjects.move_in.ManagerHolidayLettingMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiExistingContact01 extends MoveInDevBase {

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
	 * */
	String moveInDateUrlPrefill;

	/**
	 * This is for the Owner Move In date
	 */
	String ownerMoveInDate;

	/**
	 * This is for the Owner Move In date for CRM
	 */
	String ownerMoveInDateCRM;

	/**
	 * This is for the Owner Settlement date
	 */
	String ownerSettlementDate;

	/**
	 * This is for the Owner Settlement date
	 */
	String ownerSettlementDateCRM;

	/**
	 * This is the Concession Card expiry
	 */
	String concessionExpiry;

	/**
	 * This is the source ID of the portal session
	 *
	 */
	String sourceID;

	@BeforeClass
	public void beforeClass() {

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);

		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "02\\", "custom_en.json");

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
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("Testing Street Num", "Testing Street Name", StreetTypesEnum.st,
					"Testing City/Suburb", AustralianStatesEnum.vic, "Testing Postcode", AccountTypesEnum.RESIDENTIAL,
					AccountCategoryEnum.OWNER, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&complex_name=Testing Complexity", "&tenancy_type=Lots",
					"&nbsp;&tenancy_number=&nbsp;Testing&nbsp;Tenancy&nbsp;Num&nbsp;",
					"&tenancy_street_number=Testing Street Num", "&tenancy_street_name=Testing Street Name",
					"&tenancy_street_type=Purok", "&tenancy_suburb=Testing City/Suburb",
					"&tenancy_postcode=Testing Postcode", "&tenancy_state=Leyte", "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&business_hour_phone=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&after_hour_phone=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&email_address=",
					getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.OWNER.name(), "&move_in_date=", moveInDate,
					"&complex_name=Testing Complexity", "&tenancy_type=Lots",
					"&nbsp;&tenancy_number=&nbsp;Testing&nbsp;Tenancy&nbsp;Num&nbsp;",
					"&tenancy_street_number=Testing Street Num", "&tenancy_street_name=Testing Street Name",
					"&tenancy_street_type=Purok", "&tenancy_suburb=Testing City/Suburb",
					"&tenancy_postcode=Testing Postcode", "&tenancy_state=Leyte", "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Michael's", "&contact_last_name= O'Connell",
					"&mobile_number=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&business_hour_phone=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&after_hour_phone=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&email_address=",
					getProp("test_dummy_email_lower_case"),
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
		tradewastemovein = new TradeWasteMoveIn(driver);
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
	 * - verify the fields are not in error initially - verify that clicking the
	 * header of the next section validates fields - verify the required fields for
	 * Tenant option - verify the required fields for Owner option - verify the
	 * required fields for Property Manager or Letting Agent - verify the valid
	 * dates for past and future dates for Owner Move In - verify the valid dates
	 * for past and future dates for Owner Settlement Date - verify the Holiday
	 * Rental fields - verify the responsible for paying the account fields - verify
	 * the required fields in the Supply Address - verify the Supply Connected
	 * elements - verify the section header name
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

		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS and lang files
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact", "4 Postal Address",
							"5 Direct Debit", "6 Additional Note", "7 Acceptance"));
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
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Postal Address", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify the CSS and lang files
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
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateOwner, true),
					"Move In Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateOwner),
					FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateOwner), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateOwner, true), "iNvAlId dAtE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateOwner), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify not ticked
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false),
					"Testing Tenancy Num", assertionErrorMsg(getLineNumber()));
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			// verify concession displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Michael's O'Connell)", "4 Postal Address",
					"5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
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

		scrollPageDown(350);
		clickElementAction(accountdetailsmovein.header);
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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
		clickElementAction(supplydetailsmovein.tenant);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
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

		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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

		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.owner);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.owner);
			clearDateField(supplydetailsmovein.moveInDateOwner);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		String past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		supplydetailsmovein.moveInDateOwner.sendKeys(past11Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.next);
		// click next button to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
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
		clearDateField(supplydetailsmovein.moveInDateOwner);

		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		String future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(future21Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(accountdetailsmovein.header);
		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
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
		scrollPageUp(300);
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDateOwner);

		// let's put a valid move in date as 10 days from past
		String past10Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(past10Days, Keys.TAB);
		this.ownerMoveInDate = past10Days;

		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerSettleNo);
		// verify the validations for the Owner Settlement Date
		clickElementAction(supplydetailsmovein.ownerSettleNo);
		clickElementAction(supplydetailsmovein.lblOwnerSettle);
		pauseSeleniumExecution(500);
		// verify field is not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.settlementDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleNo),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.settlementDateOwner, true),
				"sEtTlEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sEtTlEmEnT DaTe (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconSettlementDateOwner),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDateOwner),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(past11Days, Keys.TAB);
		// click owner again to dismiss the calendar
		clickElementAction(supplydetailsmovein.owner);
		scrollPageDown(400);
		// click next button to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.settlementDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the CSS and lang files
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSettlementDateOwner, true),
				"sEtTlEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSettlementDateOwner),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.settlementDateOwner), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconSettlementDateOwner),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDateOwner),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSettlementDateOwner, true), "iNvAlId dAtE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSettlementDateOwner), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's clear the date using back space since clear() does not work
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		// got this exception when trying to delete the value
		// org.openqa.selenium.ElementNotInteractableException: element not interactable
		// so we just click the input date again in attempt to fix the issue
		clearDateField(supplydetailsmovein.settlementDateOwner);

		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(future21Days, Keys.TAB);
		scrollPageDown(400);
		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.settlementDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.settlementDateOwner);

		// let's put a valid move in date as 9 days from past
		String past9Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -9, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(past9Days, Keys.TAB);
		this.ownerSettlementDate = past9Days;
		// verify CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSettlementDateOwner), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.settlementDateOwner), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDateOwner),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the holiday rental required fields
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsibleOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsiblePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Postal Address", "5 hOlIdAy lEtTiNg/rEnTaL CoMpAnY DeTaIlS",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Postal Address", "5 Concession",
					"6 hOlIdAy lEtTiNg/rEnTaL CoMpAnY DeTaIlS", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
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
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Michael's O'Connell)", "4 Postal Address",
					"5 Concession", "6 hOlIdAy lEtTiNg/rEnTaL CoMpAnY DeTaIlS", "7 Direct Debit", "8 Additional Note",
					"9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
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
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.whoIsResponsibleOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.whoIsResponsiblePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the required fields in the supply address
			clickElementAction(supplydetailsmovein.supplyAddSearch);
			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);

			// click header of next section to validate
			clickElementAction(accountdetailsmovein.header);
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

			supplydetailsmovein.supplyAddComplexName.sendKeys("Testing Complexity");
			// verify error is displayed for tenancy type if value is non-existent
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Lots", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("Testing Tenancy Num");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("Testing Street Num");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Testing Street Name");
			// verify error is displayed for street type if value is non-existent
			supplydetailsmovein.supplyAddStreetType.sendKeys("Purok", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Testing City/Suburb");
			// verify error is displayed for state if value is non-existent
			supplydetailsmovein.supplyAddState.sendKeys("Leyte", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("Testing Postcode");

			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "Testing Street Num", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Testing Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Testing City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "Testing Postcode", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
			softAssertion.assertEquals(complexName, "Testing Complexity", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Lots", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "Testing Tenancy Num", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "Testing Street Num", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Testing Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Purok", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Testing City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Leyte", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "Testing Postcode", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify we can change the address and existing
		// values should be overridden
		clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
		slowSendKeys(supplydetailsmovein.supplyAddSearch, "apt1328/1328 Gold Coast Highway Palm", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(supplydetailsmovein.supplyAddressesDiv, "apt 1328/1328 Gold Coast Highway, Palm Beach QLD");
		pauseSeleniumExecution(1000);

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
		softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "1328", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Gold Coast", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
		// verify that no spinner is displayed in the state
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the Supply Connected elements
		String supplyConHeader = getDisplayedText(supplydetailsmovein.lblSupplyConnectedHeader, true);
		String supplyConIntro = getDisplayedText(supplydetailsmovein.lblSupplyConnectedIntro, true);
		String supplyConQuestion = getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true);
		softAssertion.assertEquals(supplyConHeader, "Supply Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyConIntro,
				"Please confirm if the service is connected at the above address. If it's currently not connected or you are unsure please contact our customer service team to arrange connection. This is a Test 01 This is a Test 02 Please visit our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyConQuestion, "Is service currently connected (i.e. on)?",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyDisconnected);
		// choose yes for Medical Cooling
		clickElementAction(supplydetailsmovein.medCoolingYes);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		// verify Life Support and Upload section is in error state
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.lifeSupNo);
		scrollPageDown(500);
		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// upload med cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Test PNG Type 01.png");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
		// check the number of files uploaded
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Supply Details ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
			int expectedNumOfFiles = 3;
			int counter = 1;
			int maxWaitRetry = 3;
			while (actualSize < expectedNumOfFiles && counter <= maxWaitRetry) {
				// hover mouse to make sure there's activity
				hoverToElementAction(supplydetailsmovein.dragAndDropArea);
				// add another wait because file was corrupted in the CRM
				waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
				// check the number of files uploaded
				actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Supply Details ",
						S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
						"> and attachments ID's is/are -> ", objectIds.toString()));
				counter++;
			}
//			softAssertion.assertEquals(actualSize, expectedNumOfFiles, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}

		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Test PNG Type 01 .png 0.1 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-219
	 * 
	 * - verify the required fields by clicking the header of the next section -
	 * verify we can go back to the previous section by clicking the header of the
	 * previous section - verify the cancelled ABN - verify the cancelled ACN -
	 * verify the invalid ABN - verify the invalid ACN - verify valid ABN
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
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			// verify fields are in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify we can go back to the previous section
			clickElementAction(supplydetailsmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(supplydetailsmovein.medCoolingYes, 0),
					"We are not yet in the Supply Details section");
			scrollPageDown(500);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			// verify we are back in the account details section
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");

			// verify fields are still in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));

			// verify fields are editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));

			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(accountdetailsmovein.commercial);
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify values got cleared
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.abnOrAcn, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.tradingName, false)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, false),
					concatStrings(getProp("test_data_valid_acn2"), " (",
							getProp("test_data_valid_company_name_acn1_acn2"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, false), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// using the clear() method does not clear the value
			// in the ABN/ACN field
			clickElementAction(accountdetailsmovein.abnOrAcn);
			deleteAllTextFromField();
			clickElementAction(accountdetailsmovein.tradingName);
			deleteAllTextFromField();
		}

		clickElementAction(tradewastemovein.header);
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
		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(200);
		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
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

		// verify cancelled acn
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_cancelled_acn2"));
		clickElementAction(accountdetailsmovein.tradingName);
		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(200);
		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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
		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(200);
		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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
		// click header to validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(200);
		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// click header to validate
		clickElementAction(tradewastemovein.header);
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

		// verify valid ACN
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_acn6"), Keys.TAB);
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
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_acn6"), " (",
				getProp("test_data_valid_company_name_acn5_acn6"), ")"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(valueAbnAbc, getProp("test_data_valid_acn6"), assertionErrorMsg(getLineNumber()));
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
		// click header to validate
		clickElementAction(tradewastemovein.header);
		String abnAcnInvalid = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		softAssertion.assertTrue(abnAcnInvalid.contains("143 526 0961"), assertionErrorMsg(getLineNumber()));
		pauseSeleniumExecution(500);
		// click header to validate
		clickElementAction(tradewastemovein.header);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		// verify we are still in the account details
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
		// delete the number 1 that was added
		accountdetailsmovein.abnOrAcn.sendKeys(Keys.BACK_SPACE);
		clickElementAction(accountdetailsmovein.tradingName);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		abnAcnAndCompany = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		tradingName = getDisplayedValue(accountdetailsmovein.tradingName, true);
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_acn6"), " (",
				getProp("test_data_valid_company_name_acn5_acn6"), ")"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's LLC", assertionErrorMsg(getLineNumber()));

		// verify the section header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
		softAssertion.assertEquals(header, "2 Account Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.residential);
		// verify that the Trade Waste header is no longer displayed
		tradewastemovein = new TradeWasteMoveIn(driver, 0);
		assertFalse(isElementExists(tradewastemovein.headerList), "The Trade Waste section is still displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// put a pause to fix an issue where it does not go to the next section
		pauseSeleniumExecution(1000);

		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify that the main account contact introduction text is not displayed -
	 * verify fields are not in error state initially - verify Provide None option
	 * not displayed in the Personal Identification - verify SMS checkbox is not
	 * displayed for Bills - verify the Postal checkbox is not displayed for Account
	 * Notifications and Reminders - verify each notification text labels - verify
	 * the notification settings that are ticked by default and which is not -
	 * verify hovering in the icon would display the tooltip message for each
	 * notification - verify that clicking the header of the next section validates
	 * fields - verify we can go to the previous section by clicking the header of
	 * the previous section - verify the validation when Australian Drivers Licence
	 * option is selected - verify the notification header text and the introduction
	 * text - verify the section header names - verify we go to the Postal Address
	 * when clicking Next button even if no Postal notification is ticked since it's
	 * required
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
		// verify the text is not displayed
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.lblResponsibleForPayingList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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

		// verify the url prefill
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Michael's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "O'Connell",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "+",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "+",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false), "+",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify phone numbers not in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.firstName);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.lastName);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.emailAddress);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.mobilePhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.businessPhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.afterhoursPhone);
			deleteAllTextFromField();
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

		clickElementAction(postaladdressmovein.header);
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

		scrollPageUp(500);
		// verify we can hit previous even though required fields not yet supplied
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the account details section
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// go back to the main account contact section
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify all assertions
		softAssertion.assertAll();

		// let's put an existing contact
		String firstName = "Michael";
		String lastName = "O'Connell";
		mainaccountcontactmovein.firstName.sendKeys(firstName);
		mainaccountcontactmovein.lastName.sendKeys(lastName);
		// verify date of birth is empty
		String dob = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, false);
		verifyStringIsBlank(dob);

		clickElementAction(mainaccountcontactmovein.driversLicence);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// verify the validations for the Australian Drivers Licence
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the minimum number of characters allowed which is 5 alphanumeric
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("Ab01");
		// verify that small caps still cause error for the state
		mainaccountcontactmovein.driversLicenceState.sendKeys("queensland", Keys.TAB);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.driversLicenceNumber.clear();
		mainaccountcontactmovein.driversLicenceState.clear();
		// verify the maximum number of characters allowed which is 12
		// so we entered 14 characters
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("Abc12345678012");
		mainaccountcontactmovein.driversLicenceState.sendKeys("Queensland", Keys.TAB);
		String driversLicenceNumber = getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, false);
		softAssertion.assertEquals(driversLicenceNumber, "Abc123456780", assertionErrorMsg(getLineNumber()));

		// verify the text
		String notifHeader = getDisplayedText(mainaccountcontactmovein.lblNotificationHeader, true);
		String notifIntro = getDisplayedText(mainaccountcontactmovein.lblNotificationIntro, true);
		softAssertion.assertEquals(notifHeader, "How would you like to be notified?",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifIntro,
				"Paper bills put upward pressure on prices so we encourage you to use our eBilling service. Visit our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		mainaccountcontactmovein.mobilePhone.sendKeys("0198560139", Keys.TAB);
		javaScriptClickElementAction(mainaccountcontactmovein.billsEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		// verify mobile phone in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the ticked notifications
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
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

		// enter valid number
		mainaccountcontactmovein.mobilePhone.clear();
		mainaccountcontactmovein.mobilePhone.sendKeys("0898560139");
		mainaccountcontactmovein.contactSecretCode.sendKeys("Sekrekt's-#01");
		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Main Account Contact").getText());
		String expSectionHeader = concatStrings("3 Main Account Contact (", firstName, " ", lastName, ")");
		verifyTwoStringsAreEqual(actSectionHeader, expSectionHeader, true);

		scrollPageDown(500);
		// verify we are in the next section
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not yet in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields are not in error state initially - verify that clicking the
	 * header of the next section validates fields - verify the fields in the postal
	 * address fields - verify we can go back to the previous section by clicking
	 * the header of the previous section - verify small case country will still be
	 * not allowed or in error state - verify the section headers
	 * 
	 */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyPostalAddress() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.header);
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

		// verify we can hit header of previous section
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the main contact
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(400);
		clickElementAction(postaladdressmovein.header);
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
		assertFalse(isElementInError(postaladdressmovein.postalAddSearch, 5, 0), assertionErrorMsg(getLineNumber()));

		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(postaladdressmovein.postalAddSearch, 5, 0), assertionErrorMsg(getLineNumber()));

		// verify we can hit header of previous section
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the main contact
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0), assertionErrorMsg(getLineNumber()));

		scrollPageDown(500);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);

		// verify field still in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.postalAddSearch, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddSearch);
		clickElementAction(postaladdressmovein.postalAddCantFindAdd);

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

		clickElementAction(concessionmovein.header);
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

		// verify we can hit previous even though required fields not populated
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Main Account Contact
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(postaladdressmovein.header)) {
				scrollIntoView(postaladdressmovein.header);
			}
		}
		// hit header
		clickElementAction(postaladdressmovein.header);
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

		// populate the address
		postaladdressmovein.addLine01.sendKeys("Add-#01");
		postaladdressmovein.addLine02.sendKeys("Add-#02");
		postaladdressmovein.addLine03.sendKeys("Add-#03");
		postaladdressmovein.addLine04.sendKeys("Add-#04");
		postaladdressmovein.city.sendKeys("Dressrosa");
		postaladdressmovein.state.sendKeys("East Blue");
		postaladdressmovein.postcode.sendKeys("6501");
		postaladdressmovein.country.sendKeys("australia", Keys.TAB);
		clickElementAction(concessionmovein.header);
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
		postaladdressmovein.country.sendKeys("Australia", Keys.TAB);

		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Postal Address").getText());
		verifyTwoStringsAreEqual(header, "4 Postal Address", true);

		scrollPageDown(500);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Concession section
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not in the Concession section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify fields are not in error initially - verify clicking the header of
	 * the next section validates fields - verify the required fields - verify we
	 * can go back to the previous section by clicking the header of the previous
	 * section - verify the expected concession card types - verify the validations
	 * for the concession card number and expiry - verify we can upload files -
	 * verify the section header name
	 * 
	 */
	@Test(priority = 5, dependsOnMethods = { "verifyPostalAddress" })
	public void verifyConcessionDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
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

		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go to previous section
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Postal Address Section
		assertTrue(isElementDisplayed(postaladdressmovein.country, 0), "We are not in the Postal Address section");
		// go back to the Concession details
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);

		clickElementAction(concessionmovein.addConcessionYes);
		// verify fields are not in error state
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

		clickElementAction(managerholidaylettingmovein.header);
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
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Postal Address Section
		String country = getDisplayedValue(postaladdressmovein.country, true);
		verifyTwoStringsAreEqual(country, "Australia", true);
		// go back to the Concession details
		clickElementAction(concessionmovein.header);
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

		concessionmovein.cardHolderName.sendKeys("Tony Stark");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 6);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		softAssertion.assertEquals(typeChosen, "Home Parks and Multi Units", assertionErrorMsg(getLineNumber()));
		// verify the Concession Card Number and expiry is displayed
		softAssertion.assertTrue(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation in the Concession Card Number and expiry
		concessionmovein.cardNumber.sendKeys("+63 (03) 0984645");
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
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(prevMonthStr, "/", curYearStr, Keys.TAB);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.cardNumber);
		deleteAllTextFromField();
		clearDateField(concessionmovein.cardNumExpiry);
		// put valid value
		concessionmovein.cardNumber.sendKeys("01238578690");
		int month = 2;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(monthStr, "/", expYrStr, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForUpload);
		pauseSeleniumExecution(1000);
		String concessionExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		this.concessionExpiry = concessionExp;

		// upload the files
		uploadConcessionFiles(ARTIFACTS_DIR, "Smaller file tiff file.tiff");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Smaller file tiff file .tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Concession").getText());
		softAssertion.assertEquals(header, "5 Concession", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the Holiday Letting/Rental Company Details
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We are not in the Holiday Letting/Rental Company Details");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify the fields are not in error state initially - verify clicking the
	 * header of the next section validates the required fields - verify we can go
	 * back to the previous section by clicking the header of the previous section -
	 * verify the required fields for the company address - verify that entering
	 * lower case for the country will return an error - verify fix for bug ticket
	 * BBPRTL-550 - verify that the address can be overridden when users enters a
	 * new address in the lookup - verify that the addresses returned are not only
	 * from Australia - verify the section header name
	 * 
	 */
	@Test(priority = 6, dependsOnMethods = { "verifyConcessionDetails" })
	public void verifyHolidayLettingDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify the intro message
		String introMsg = getDisplayedText(managerholidaylettingmovein.lblPropManHolidayLettingIntro, true);
		softAssertion.assertEquals(introMsg,
				"pLeAsE SpEcIfY ThE PrOpErTy cOmPaNy vIa wHiCh tHe pRoPeRtY Is bEiNg mAnAgEd",
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		managerholidaylettingmovein = new ManagerHolidayLettingMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(managerholidaylettingmovein.manualAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify CSS and lang files
		// verify the fix for ticket BBPRTL-2040
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.lblPropManHolidayLettingIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.companyName, true),
				"cOmPaNy nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.companyContactNum, true),
				"cOmPaNy cOnTaCt nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.companyAddress, true),
				"pLeAsE StArT TyPiNg cOmPaNy aDdReSs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy nAmE"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy cOnTaCt nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyContactNum),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(managerholidaylettingmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(managerholidaylettingmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		// verify fix for bug ticket BBPRTL-550
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS ang lang files
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy cOnTaCt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyName),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyContactNum),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyName, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyContactNum, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyAddress, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyContactNum), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go to the previous section
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);

		// verify fields are still in error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS ang lang files
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy cOnTaCt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyName, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyContactNum, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCompanyAddress, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyContactNum), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.companyName.sendKeys("Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		managerholidaylettingmovein.companyContactNum
				.sendKeys("Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		clickElementAction(managerholidaylettingmovein.lblPropManHolidayLettingIntro);
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCompanyName, true),
				"cOmPaNy nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCompanyName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.companyName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCompanyContactNum, true),
				"cOmPaNy cOnTaCt nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCompanyContactNum),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.companyContactNum), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyContactNum),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(managerholidaylettingmovein.companyAddress);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCompanyAddress, true),
				"pLeAsE StArT TyPiNg cOmPaNy aDdReSs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCompanyAddress),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.cannotFindAdd, true),
				"search cAnNoT FiNd aDdReSs? ClIcK HeRe tO CoMpLeTe dEtAiLs mAnUaLlY",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(managerholidaylettingmovein.cannotFindAdd),
				GOOGLELOOKUP_CANNOTFIND_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.cannotFindAdd), MAT_OPTION_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(managerholidaylettingmovein.cannotFindAdd);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.city, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.postCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.quickAddSearch, true),
				"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.quickAddSearch),
				MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.address01, true),
				"aDdReSs lInE 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.address02, true),
				"aDdReSs lInE 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.address03, true),
				"aDdReSs lInE 3", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.address04, true),
				"aDdReSs lInE 4", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.city, true), "cItY/SuBuRb",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.state, true), "sTaTe",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.postCode, true), "pOsTcOdE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(managerholidaylettingmovein.country, true), "cOuNtRy",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 1"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 2"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cItY/SuBuRb"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "sTaTe"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "pOsTcOdE"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOuNtRy"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress01),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress02),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress03),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress04),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCity),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineState),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlinePostCode),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify the fields are in error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.city, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.postCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintAddress01, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintAddress02, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintAddress03, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintAddress04, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCity, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintState, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintPostCode, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCountry, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress01), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress02), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress03), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress04), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintState), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintPostCode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCountry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 1"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 2"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cItY/SuBuRb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "sTaTe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "pOsTcOdE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOuNtRy"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress01),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress02),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress03),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress04),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineState), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlinePostCode), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go back to the previous section
		clickElementAction(managerholidaylettingmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);

		// verify the fields are still in error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.address04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.city, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.postCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress01), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress02), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress03), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintAddress04), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintState), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintPostCode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCountry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 1"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 2"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cItY/SuBuRb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "sTaTe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "pOsTcOdE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOuNtRy"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress01),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress02),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress03),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress04),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineState), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlinePostCode), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.address01.sendKeys("Add-01");
		managerholidaylettingmovein.address02.sendKeys("Add-02");
		managerholidaylettingmovein.address03.sendKeys("Add-03");
		managerholidaylettingmovein.address04.sendKeys("Add-04");
		managerholidaylettingmovein.city.sendKeys("City");
		managerholidaylettingmovein.state.sendKeys("State");
		managerholidaylettingmovein.postCode.sendKeys("Postcode");
		clickElementAction(managerholidaylettingmovein.country);
		List<String> allCountries = getAllMatOptionsValues(managerholidaylettingmovein.countriesDiv);
		managerholidaylettingmovein.country.sendKeys("austraila", Keys.TAB);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify the fields are not in error state except country
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.city, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.postCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.hintCountry, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblAddress01, true),
				"aDdReSs lInE 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblAddress01), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.address01), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress01),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblAddress02, true),
				"aDdReSs lInE 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblAddress02), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.address02), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress02),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblAddress03, true),
				"aDdReSs lInE 3", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblAddress03), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.address03), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress03),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblAddress04, true),
				"aDdReSs lInE 4", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblAddress04), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.address04), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineAddress04),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCity, true), "cItY/SuBuRb",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCity), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.city), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCity),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblState, true), "sTaTe",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblState), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.state), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineState),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblPostCode, true), "pOsTcOdE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblPostCode), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.postCode), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlinePostCode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCountry, true), "cOuNtRy",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCountry), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		assertTrue(isElementDisplayed(managerholidaylettingmovein.quickAddSearch, 0),
				assertionErrorMsg(getLineNumber()));
		// let's update the address
		clickElementAction(managerholidaylettingmovein.quickAddSearch);
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.manualAddSearch, true),
				"mAnUaL AdDrEsS SeArCh", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.manualAddSearch),
				MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.hintCompanyAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		slowSendKeys(managerholidaylettingmovein.companyAddress, "Unit 402B 100 Bowen ST Spring", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(getElementFromList(managerholidaylettingmovein.companyAddress, 1)),
				MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndProp(managerholidaylettingmovein.companyAddressesDiv),
				MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's confirm first that the records retrieved are not only from Australia
		List<String> addresses = null;
		try {
			addresses = getAllCompanyAddress(managerholidaylettingmovein.companyAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			managerholidaylettingmovein = new ManagerHolidayLettingMoveIn(driver);
			addresses = getAllCompanyAddress(managerholidaylettingmovein.companyAddressesDiv);
		}
		// verify that the addresses are not only from Australia
		verifyStringContainsInAnyStringInList(addresses, true, allCountries, true);
		chooseAddress(managerholidaylettingmovein.companyAddressesDiv, "unit 402b/100 Bowen Street, Spring Hill QLD",
				"unit 402b/100 Bowen St, Spring Hill QLD");
		pauseSeleniumExecution(1000);

		// verify the fields are populated correctly
		// blank add 03 and add 04
		String add01 = getDisplayedValue(managerholidaylettingmovein.address01, true);
		String add02 = getDisplayedValue(managerholidaylettingmovein.address02, true);
		String add03 = getDisplayedValue(managerholidaylettingmovein.address03, true);
		String add04 = getDisplayedValue(managerholidaylettingmovein.address04, true);
		String city = getDisplayedValue(managerholidaylettingmovein.city, true);
		String state = getDisplayedValue(managerholidaylettingmovein.state, true);
		String postcode = getDisplayedValue(managerholidaylettingmovein.postCode, true);
		String country = getDisplayedValue(managerholidaylettingmovein.country, true);
		softAssertion.assertEquals(add01, "Unit 402B", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "100 Bowen Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Spring Hill", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4000", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCountry, true), "cOuNtRy",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCountry), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		String header = normalizeSpaces(getElementFrmMatStepHdrTag(managerholidaylettingmovein.matStepHeader,
				"Holiday Letting/Rental Company Details").getText());
		softAssertion.assertEquals(header, "6 hOlIdAy lEtTiNg/rEnTaL CoMpAnY DeTaIlS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify we're in the next section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0),
				"We're not yet in the Direct Debit details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 * - verify that the fields are not in error state initially - verify the 3rd
	 * option is not displayed - verify the required fields are validated when
	 * clicking the header of the next section - verify the required fields for Bank
	 * Account - verify we can go back to the to previous section when clicking the
	 * header of the previous section - verify the validations for the Bank Account
	 * Name - verify the validations for the Account BSB - verify the validations
	 * for the Account Number - verify fix for bug ticket BBPRTL-996 - verify the
	 * section header names - verify we can skip the additional notes section and
	 * jump right to Acceptance page
	 * 
	 */
	@Test(priority = 7, dependsOnMethods = { "verifyHolidayLettingDetails" })
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

		clickElementAction(additionalnotemovein.header);
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
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We're not yet in the Holiday Letting/Rental Company Details");
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);

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

		clickElementAction(additionalnotemovein.header);
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
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
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

		clickElementAction(additionalnotemovein.header);
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
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can go to previous section even though there are required fields
		// not
		// populated
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We're not yet in the Holiday Letting/Rental Company Details");
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);

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

		// verify we cannot enter special characters in the Bank Account Name
		directdebitmovein.bankAccountName.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		// verify we cannot enter alphabets in the Account BSB
		directdebitmovein.accountBSB.sendKeys("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		// verify we cannot enter alphabets in the Account Number
		directdebitmovein.accountNumber.sendKeys("ABCDEFGHIJKLMNOPQRSTUVWXYZ", Keys.TAB);
		String bankAcctName = getDisplayedValue(directdebitmovein.bankAccountName, false);
		String bankAcctBsb = getDisplayedValue(directdebitmovein.accountBSB, false);
		String bankAcctNum = getDisplayedValue(directdebitmovein.accountNumber, false);
		softAssertion.assertTrue(StringUtils.isBlank(bankAcctName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bankAcctBsb), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bankAcctNum), assertionErrorMsg(getLineNumber()));
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
		// verify the maximum characters for Bank Account which is 50
		directdebitmovein.bankAccountName.sendKeys("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
		// verify the required number of characters for Account BSB which is 6
		directdebitmovein.accountBSB.sendKeys("1");
		// verify the required number of characters for Account Number which is 10 and
		// at lease 6 digits
		directdebitmovein.accountNumber.sendKeys("2", Keys.TAB);
		bankAcctName = getDisplayedValue(directdebitmovein.bankAccountName, false);
		softAssertion.assertEquals(bankAcctName, "Lorem ipsum dolor sit amet consectetur adipiscing",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		String hintErrorAcctBsb = getDisplayedText(directdebitmovein.hintAccountBSB, true);
		String hintErrorAcctNum = getDisplayedText(directdebitmovein.hintAccountNumber, true);
		softAssertion.assertEquals(hintErrorAcctBsb, "Account BSB should be 6 digits long",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(hintErrorAcctNum, "Account Number should be at least 6 digits long",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.bankAccountName);
		deleteAllTextFromField();
		clickElementAction(directdebitmovein.accountBSB);
		deleteAllTextFromField();
		clickElementAction(directdebitmovein.accountNumber);
		deleteAllTextFromField();
		// put valid Bank Account Name
		directdebitmovein.bankAccountName.sendKeys("Michael O'Connell");
		// put valid Account BSB with extra digit to verify that only 6 digits are
		// allowed
		directdebitmovein.accountBSB.sendKeys("0123405");
		// put valid Account Number with extra digit to verify that only up 10 digits
		// are allowed
		directdebitmovein.accountNumber.sendKeys("01328898406");
		bankAcctName = getDisplayedValue(directdebitmovein.bankAccountName, false);
		bankAcctBsb = getDisplayedValue(directdebitmovein.accountBSB, false);
		bankAcctNum = getDisplayedValue(directdebitmovein.accountNumber, false);
		softAssertion.assertEquals(bankAcctName, "Michael OConnell", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bankAcctBsb, "012340", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bankAcctNum, "0132889840", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Direct Debit").getText());
		softAssertion.assertEquals(header, "7 Direct Debit", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.authorisationBankAccount);
		scrollPageDown(500);
		// skip the additional details section
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
	 * - verify the expected values for each line of the acceptance page - verify
	 * the update link from Moving In line and update the move in date and
	 * settlement date - verify the Trade Waste update link - verify the Discharge
	 * info update link and remove an uploaded file - verify the update link for the
	 * Main Contact then add an additional Contact - verify the fields are not in
	 * error state initially for the additional contact - verify the Provide None
	 * option is not displayed - verify SMS checkbox is not displayed for Bills -
	 * verify the Postal checkbox is not displayed for Account Notifications and
	 * Reminders - verify the notification labels for the additional contact -
	 * verify the notification settings that should be ticked by default and which
	 * should be not ticked - verify hovering into the icon would display the
	 * notification tooltip message - click the header of acceptance page to
	 * validate the required fields for the additional contact - verify clicking the
	 * Previous button from the additional contact would redirect us into the Main
	 * Account Contact section - verify the update link for the Postal Address then
	 * choose Yes for same as supply address question. Then verify that doing so
	 * would hide the fields for the address - verify the update link for the
	 * Concession section and update the card type. Then verify that the original
	 * card number was not cleared and expiry got hidden - verify the update link
	 * for the Holiday Letting and update the address - verify the update link for
	 * Direct Debit then try the validation again for the bank account authorisation
	 * - verify the update link for the additional notes then add special characters
	 * in the notes area - verify the acceptance page again each line for the
	 * updated values - click the Submit button then verify the required fields -
	 * verify that clicking the previous section would redirect us into the
	 * Additional Notes section
	 * 
	 */
	@Test(priority = 8, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAcceptanceDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(acceptancemovein.propManLettingAgentRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.propManLettingAgentRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> propManLettingAgentRowData = getAllLabelCss(acceptancemovein.propManLettingAgentRowData);
		softAssertion.assertEquals(propManLettingAgentRowData.size(), 6, assertionErrorMsg(getLineNumber()));
		for (List<String> label : propManLettingAgentRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
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
				"Service Address update Apartment 1328, 1328 Gold Coast Highway Palm Beach, Queensland, 4221 Service currently disconnected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, concatStrings(
				"Main Account Contact update Michael O'Connell Email Address: ", getProp("test_dummy_email_lower_case"),
				" Mobile Phone: 0898560139 Personal Id: Driver Licence (Abc123456780, Queensland) Contact Secret: (Sekrekt's-#01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Add-#01 Add-#02 Add-#03 Add-#04 Dressrosa, East Blue, 6501 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(concession,
						concatStrings("Concession update Tony Stark Home Parks and Multi Units 01238578690 (",
								this.concessionExpiry, ") Concession Card Uploaded"),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"hOlIdAy lEtTiNg / rEnTaL CoMpAnY update Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Unit 402B 100 Bowen Street Spring Hill, Queensland, 4000 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Michael OConnell BSB: 012340 / Num: 0132889840",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify no trade waste displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify we go to the supply details section
		clickExactLinkNameFromElement(acceptancemovein.movingInRow, "update");
		pauseSeleniumExecution(1000);
		// let's update the move in date
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDateOwner);
		// let's put a valid move in date as 10 days from past
		String past9Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -9, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past9D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -9, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(past9Days, Keys.TAB);
		this.ownerMoveInDate = past9Days;
		this.ownerMoveInDateCRM = past9D;
		// let's update the settlement date
		clearDateField(supplydetailsmovein.settlementDateOwner);
		// let's put a valid move in date as 9 days from past
		String past8Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -8, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past8D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -8, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(past8Days, Keys.TAB);
		// let's tick an already ticked field
		// to dismiss the calendar
		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
		this.ownerSettlementDate = past8Days;
		this.ownerSettlementDateCRM = past8D;
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(supplydetailsmovein.lifeSupYes)) {
				scrollIntoView(supplydetailsmovein.lifeSupYes);
			}
		}
		// update life support
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// verify fields not in error state
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
		scrollPageDown(300);
		// verify that the Other checkbox would be ticked automatically if we enter a
		// character
		clickElementAction(supplydetailsmovein.lifeSuppOtherInput);
		// verify checkbox not yet ticked
		softAssertion.assertFalse(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		supplydetailsmovein.lifeSuppOtherInput.sendKeys("0");
		// verify checkbox ticked
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we go to the Main Account Contact section
		clickExactLinkNameFromElement(acceptancemovein.mainContactRow, "update");
		pauseSeleniumExecution(1000);
		// let's add another contact
		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1DriversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MedicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields are not in error state
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
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
		scrollPageDown(600);
		// hit header of acceptance to validate
		clickElementAction(acceptancemovein.header);
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
		String firstName = "Justin";
		String lastName = "O'day";
		additionalcontactmovein.addCont1FirstName.sendKeys(firstName);
		additionalcontactmovein.addCont1LastName.sendKeys(lastName);
		// verify the validations for the Passport
		clickElementAction(additionalcontactmovein.addCont1Passport);
		// click header to validate
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify error when characters entered is less than 6
		additionalcontactmovein.addCont1PassportNumber.sendKeys("Abc12");
		// verify error returned when country entered is lower case
		additionalcontactmovein.addCont1PassportCountry.sendKeys("australia", Keys.TAB);
		// click header to validate
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields are in error state
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
		// verify that when the maximum characters allowed is reached, it's no longer
		// entering the characters
		additionalcontactmovein.addCont1PassportNumber.sendKeys("Pass-123456789");
		// verify that entering non-existent country will return an error
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Arabasta", Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		String passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, true);
		softAssertion.assertEquals(passportNum, "Pass123456", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1PassportCountry);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Åland Islands", Keys.TAB);
		assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		// verify the expected notifications ticked
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
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		// enter the existing mobile
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+61235298750");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("1800121655");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+451255556612");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("Sekrekt's-#02");
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader, "Additional Contact").getText());
		String expSectionHeader = concatStrings("4 Additional Contact (", firstName, " ", lastName, ")");
		verifyTwoStringsAreEqual(actSectionHeader, expSectionHeader, true);
		scrollPageDown(600);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
		// verify we go to the Postal Address section
		clickExactLinkNameFromElement(acceptancemovein.postalAddressRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				"We are not yet in the Postal Address section");
		clickElementAction(postaladdressmovein.sameSupAddressYes);
		pauseSeleniumExecution(1000);
		// verify the fields got hidden
		postaladdressmovein = new PostalAddressMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(postaladdressmovein.addLine01List),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.addLine02List),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.addLine03List),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.addLine04List),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.cityList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.stateList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.postcodeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(postaladdressmovein.countryList), assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
		// verify we go the concession section
		clickExactLinkNameFromElement(acceptancemovein.concessionRow, "update");
		pauseSeleniumExecution(1000);
		// let's update the card type
		clickElementAction(concessionmovein.typeOfConcessionCard);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 3);
		pauseSeleniumExecution(1000);
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
		// verify we chose the correct one
		softAssertion.assertEquals(typeChosen, "Pensioner Card Centrelink", assertionErrorMsg(getLineNumber()));
		// verify old value not cleared
		softAssertion.assertEquals(cardNum, "01238578690", assertionErrorMsg(getLineNumber()));
		concessionmovein = new ConcessionMoveIn(driver, 0);
		// verify the expiry got hidden
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.header)) {
				scrollIntoView(acceptancemovein.header);
			}
		}
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(600);
		// verify we go to the holiday letting/rental company details
		clickExactLinkNameFromElement(acceptancemovein.propManLettingAgentRow, "update");
		pauseSeleniumExecution(1000);
		// let's update the address
		managerholidaylettingmovein.address03.sendKeys("Add-##03");
		managerholidaylettingmovein.address04.sendKeys("Add-##04");
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.header)) {
				scrollIntoView(acceptancemovein.header);
			}
		}
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(700);
		// verify we go to the Direct Debit section
		clickExactLinkNameFromElement(acceptancemovein.directDebitRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccountName, 0),
				"We are not yet in the Direct Debit section");
		// untick the checkbox
		clickElementAction(directdebitmovein.authorisationBankAccount);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				"The Bank Account authorization is not in error state");
		clickElementAction(directdebitmovein.authorisationBankAccount);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.header)) {
				scrollIntoView(acceptancemovein.header);
			}
		}
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance Page section");

		scrollPageDown(1000);
		// verify we go to the Additional Note
		clickExactLinkNameFromElement(acceptancemovein.additionalNoteRow, "update");
		pauseSeleniumExecution(1000);
		additionalnotemovein.notesArea.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify the acceptance page again
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
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
				"Service Address update Apartment 1328, 1328 Gold Coast Highway Palm Beach, Queensland, 4221 Service currently disconnected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment 0 Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		// verify no trade waste displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(mainContact, concatStrings(
				"Main Account Contact update Michael O'Connell Email Address: ", getProp("test_dummy_email_lower_case"),
				" Mobile Phone: 0898560139 Personal Id: Driver Licence (Abc123456780, Queensland) Contact Secret: (Sekrekt's-#01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Justin O'day Email Address: ",
				getProp("test_dummy_email_upper_case"),
				" Mobile Phone: +61235298750 Business Phone: 1800121655 A/Hours Phone: +451255556612 Personal Id: Passport (Pass123456, Åland Islands) Contact Secret: (Sekrekt's-#02)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Apartment 1328, 1328 Gold Coast Highway Palm Beach, Queensland, 4221",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Tony Stark Pensioner Card Centrelink 01238578690 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"hOlIdAy lEtTiNg / rEnTaL CoMpAnY update Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Unit 402B 100 Bowen Street Add-##03 Add-##04 Spring Hill, Queensland, 4000 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Michael OConnell BSB: 012340 / Num: 0132889840",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(2000);
		// verify the required fields for the checkboxes
		clickElementAction(acceptancemovein.submit);
		// verify the tickboxes in error state
		softAssertion.assertTrue(isElementInError(acceptancemovein.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemovein.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageUp(1500);
		// verify we can go to the previous section
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0), "We are not in the Additional Notes section");

		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not in the Acceptance section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-213
	 * 
	 * - verify the keys in the session storage and the expected number - verify the
	 * keys in the local storage and the expected number - verify the values for
	 * each key in the session storage is not empty - verify the values for each key
	 * in the local storage is not empty - refresh the browser and verify that the
	 * data are not cleared in the form - verify each section by clicking the header
	 * of the next section and confirm the expected values - verify that the
	 * acceptance page section is still the same values - verify the required fields
	 * when clicking the Submit button - tick all 3 checkboxes then submit the
	 * request - verify the expected number of uploaded fields in the
	 * S3_PORTAL_PRESIGN_BUCKET_NAME - wait until the response header shows success
	 * - click the OK dialog from the response header - verify close message
	 * displayed - verify that all the keys and values on the session storage are
	 * cleared - verify that the keys and values on the local storage are not
	 * cleared
	 * 
	 */
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

		// let's confirm the session and local storage
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-complex_name"),
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 39, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAcctDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAdditionalContact = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionCard = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionAdditionalNotes = storage.getItemFromSessionStorage("move-in.notes");
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
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAdditionalNotes), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryComplexName = storage.getItemFromSessionStorage("move-in-query-complex_name");
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
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryComplexName), assertionErrorMsg(getLineNumber()));
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
		// let's confirm the session and local storage
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-move_in_date"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-complex_name"),
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 39, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
		sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		sessionAcctDetails = storage.getItemFromSessionStorage("move-in.account_details");
		sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		sessionAdditionalContact = storage.getItemFromSessionStorage("move-in.additional_contact");
		sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		sessionConcessionCard = storage.getItemFromSessionStorage("move-in.concession_card");
		sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		sessionAdditionalNotes = storage.getItemFromSessionStorage("move-in.notes");
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
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAdditionalNotes), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryComplexName = storage.getItemFromSessionStorage("move-in-query-complex_name");
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
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryComplexName), assertionErrorMsg(getLineNumber()));
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

		// let's verify the Supply Details to confirm that the values are still there
		String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDateOwner, true);
		String settlementDate = getDisplayedValue(supplydetailsmovein.settlementDateOwner, true);
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, true);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, true);
		String streetNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, true);
		String streetName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, true);
		String streetType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, true);
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(moveInDate, this.ownerMoveInDate, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(settlementDate, this.ownerSettlementDate, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.whoIsResponsibleOwner, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetNum, "1328", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetName, "Gold Coast", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(streetType, "Highway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Palm Beach", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4221", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.supplyDisconnected, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
				assertionErrorMsg(getLineNumber()));
		scrollPageDown(700);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBPRTL-1243, the uploaded file contains '.png'
		// so we include it in the assertion for now
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Test PNG Type 01.png .image/png 0.1 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);

		// verify the data for the Account Details
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);

		// verify the data for the Main Account Contact
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.lblResponsibleForPayingList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, true);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, true);
		String birthdate = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		String licenseNum = getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, true);
		String stateIssued = getDisplayedValue(mainaccountcontactmovein.driversLicenceState, true);
		String emailAdd = getDisplayedValue(mainaccountcontactmovein.emailAddress, true);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, true);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, true);
		String afterHPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true);
		String contactSecretCode = getDisplayedValue(mainaccountcontactmovein.contactSecretCode, true);
		softAssertion.assertEquals(firstName, "Michael", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "O'Connell", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(birthdate), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(licenseNum, "Abc123456780", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stateIssued, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.billsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0898560139", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(afterHPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Sekrekt's-#01", assertionErrorMsg(getLineNumber()));
		// verify the Add Another Contact link is no longer displayed
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);

		// verify the details for the Additional Contact
		firstName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, true);
		lastName = getDisplayedValue(additionalcontactmovein.addCont1LastName, true);
		birthdate = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		String passportNum = getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, true);
		String passportCountry = getDisplayedValue(additionalcontactmovein.addCont1PassportCountry, true);
		emailAdd = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, true);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, true);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, true);
		afterHPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, true);
		contactSecretCode = getDisplayedValue(additionalcontactmovein.addCont1ContactSecretCode, true);
		softAssertion.assertEquals(firstName, "Justin", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "O'day", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(birthdate), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(passportNum, "Pass123456", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(passportCountry, "Åland Islands", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61235298750", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "1800121655", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(afterHPhone, "+451255556612", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Sekrekt's-#02", assertionErrorMsg(getLineNumber()));
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
		scrollPageDown(500);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);

		// verify the postal address values
		softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressYes, 0),
				assertionErrorMsg(getLineNumber()));
		postaladdressmovein = new PostalAddressMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(postaladdressmovein.postalAddSearchList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);

		// verify the concession details
		String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		String cardNumber = getDisplayedValue(concessionmovein.cardNumber, true);
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardHolder, "Tony Stark", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardType, "Pensioner Card Centrelink", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNumber, "01238578690", assertionErrorMsg(getLineNumber()));
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// not sure why there's .png added at the file name so added that in the
		// assertion for now
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Smaller file tiff file.tiff .image/tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(managerholidaylettingmovein.header)) {
				scrollIntoView(managerholidaylettingmovein.header);
			}
		}
		clickElementAction(managerholidaylettingmovein.header);
		pauseSeleniumExecution(1000);

		// verify the holiday letting values
		String compName = getDisplayedValue(managerholidaylettingmovein.companyName, true);
		String compContactNum = getDisplayedValue(managerholidaylettingmovein.companyContactNum, true);
		String address01 = getDisplayedValue(managerholidaylettingmovein.address01, true);
		String address02 = getDisplayedValue(managerholidaylettingmovein.address02, true);
		String address03 = getDisplayedValue(managerholidaylettingmovein.address03, true);
		String address04 = getDisplayedValue(managerholidaylettingmovein.address04, true);
		city = getDisplayedValue(managerholidaylettingmovein.city, true);
		state = getDisplayedValue(managerholidaylettingmovein.state, true);
		postcode = getDisplayedValue(managerholidaylettingmovein.postCode, true);
		String country = getDisplayedValue(managerholidaylettingmovein.country, true);
		softAssertion.assertEquals(compName, "Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(compContactNum, "Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(address01, "Unit 402B", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(address02, "100 Bowen Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(address03, "Add-##03", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(address04, "Add-##04", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Spring Hill", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4000", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
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

		// verify the values for Direct Debit is still there
		String bankAccountName = getDisplayedValue(directdebitmovein.bankAccountName, true);
		String acctBsb = getDisplayedValue(directdebitmovein.accountBSB, true);
		String acctNum = getDisplayedValue(directdebitmovein.accountNumber, true);
		softAssertion.assertTrue(isElementTicked(directdebitmovein.bankAccount, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bankAccountName, "Michael OConnell", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctBsb, "012340", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNum, "0132889840", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);

		// verify the value for Additional Notes is still there
		String notesArea = getDisplayedValue(additionalnotemovein.notesArea, true);
		verifyTwoStringsAreEqual(notesArea, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");

		scrollPageDown(1000);
		// verify the values in the acceptance section
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
				"Service Address update Apartment 1328, 1328 Gold Coast Highway Palm Beach, Queensland, 4221 Service currently disconnected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment 0 Medical Cooling Required Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(mainContact, concatStrings(
				"Main Account Contact update Michael O'Connell Email Address: ", getProp("test_dummy_email_lower_case"),
				" Mobile Phone: 0898560139 Personal Id: Driver Licence (Abc123456780, Queensland) Contact Secret: (Sekrekt's-#01)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (Email) Notifications and Reminders (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Justin O'day Email Address: ",
				getProp("test_dummy_email_upper_case"),
				" Mobile Phone: +61235298750 Business Phone: 1800121655 A/Hours Phone: +451255556612 Personal Id: Passport (Pass123456, Åland Islands) Contact Secret: (Sekrekt's-#02)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Apartment 1328, 1328 Gold Coast Highway Palm Beach, Queensland, 4221",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Tony Stark Pensioner Card Centrelink 01238578690 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(holidayLetting,
				"hOlIdAy lEtTiNg / rEnTaL CoMpAnY update Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Unit 402B 100 Bowen Street Add-##03 Add-##04 Spring Hill, Queensland, 4000 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Michael OConnell BSB: 012340 / Num: 0132889840",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
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

		scrollPageDown(1700);
		// verify the required fields for the checkboxes
		clickElementAction(acceptancemovein.submit);
		// verify the tickboxes in error state
		softAssertion.assertTrue(isElementInError(acceptancemovein.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemovein.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// tick all 3 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		javaScriptClickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("ResiExistingContact01_ownerMoveInDate", this.ownerMoveInDate);
		addProp("ResiExistingContact01_ownerMoveInDateCRM", this.ownerMoveInDateCRM);
		addProp("ResiExistingContact01_ownerSettlementDateCRM", this.ownerSettlementDateCRM);
		addProp("ResiExistingContact01_sourceID", this.sourceID);
		addProp("ResiExistingContact01_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiExistingContact01_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

		// submit the request
		clickElementAction(acceptancemovein.submit);
		// did this because there was an issue where initial click
		// on the submit button did not work
		retryClickSubmit(1);
		waitForCssToRender();

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
			// verify the CSS and lang files
			softAssertion.assertEquals(getButtonCss(acceptancemovein.cancel), CANCEL_BUTTON_DISABLED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getButtonCss(acceptancemovein.previous), LAST_PREVIOUS_BUTTON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getButtonCss(acceptancemovein.submit), SUBMIT_BUTTON_DISABLED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(submitMsg, "sUbMiTtInG YoUr rEqUeSt...", assertionErrorMsg(getLineNumber()));
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			boolean isStillSubmitting;
			if (isElementExists(acceptancemovein.submittingMessageList)) {
				softAssertion.assertEquals(getLabelCss(acceptancemovein.submittingMessage), PROCESSING_REQUEST_MSG_CSTM,
						assertionErrorMsg(getLineNumber()));
				isStillSubmitting = true;
			} else {
				logDebugMessage(
						"Submitting message no longer displayed. No need to check the CSS for the message, spinner and progress bar");
				isStillSubmitting = false;
			}
			PortalMoveIn portalmovein = new PortalMoveIn(driver, 0);
			if (isStillSubmitting) {
				if (isElementExists(portalmovein.spinnerList)) {
					softAssertion.assertEquals(getCssStrokeProp(portalmovein.spinner), SPINNER_ICON_CSTM,
							assertionErrorMsg(getLineNumber()));
					String progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG;
					String progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG;
					JavascriptExecutor js = (JavascriptExecutor) driver;
					String initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
					String remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
					softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
							"The initial progress bar color is not correct");
					softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
							"The remaining progress bar color is not correct");
				}
			} else {
				logDebugMessage(
						"Submitting message no longer displayed. No need to check the CSS for the spinner and progress bar");
			}
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
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