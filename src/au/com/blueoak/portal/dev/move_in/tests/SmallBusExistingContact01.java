package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
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

public class SmallBusExistingContact01 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	TradeWasteMoveIn tradewastemovein;
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
	 * 
	 * */
	String propManMoveInDate;

	/** 
	 * 
	 * */
	String propManMoveInDateCRM;

	/** 
	 * 
	 * */
	String propManSettlementDate;

	/** 
	 * 
	 * */
	String propManSettlementDateCRM;

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

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "03\\", "custom_en.json");

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
			populate3rdPartyPrefill("14", "Second", StreetTypesEnum.WRONG_VALUE, "Glenelg East",
					AustralianStatesEnum.WRONG_VALUE, "5045", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM,
					initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=2", "&tenancy_street_number=14",
					"&tenancy_street_name=Second", "&tenancy_street_type=", StreetTypesEnum.FtRk.getLabelText(),
					"&tenancy_suburb=Glenelg East", "&tenancy_postcode=5045", "&tenancy_state=",
					AustralianStatesEnum.SA.name(), "&account_type=", AccountTypesEnum.RESIDENTIAL.name(),
					"&business_number=", getProp("test_data_valid_acn2"), "&business_trading_name=",
					"&contact_first_name=Paul & Mary", "&contact_last_name=Toniolo & Tinola",
					"&mobile_number=     (02)     3892     1111     ",
					"&business_hour_phone=     (03)     4892     2222     ",
					"&after_hour_phone=     (04)     5892     3333     ", "&email_address=",
					getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.uNiT.name(), "&tenancy_number=2", "&tenancy_street_number=14",
					"&tenancy_street_name=Second", "&tenancy_street_type=", StreetTypesEnum.FtRk.getLabelText(),
					"&tenancy_suburb=Glenelg East", "&tenancy_postcode=5045", "&tenancy_state=",
					AustralianStatesEnum.SA.name(), "&account_type=", AccountTypesEnum.RESIDENTIAL.name(),
					"&business_number=", getProp("test_data_valid_acn2"), "&business_trading_name=",
					"&contact_first_name=Paul & Mary", "&contact_last_name=Toniolo & Tinola",
					"&mobile_number=     (02)     3892     1111     ",
					"&business_hour_phone=     (03)     4892     2222     ",
					"&after_hour_phone=     (04)     5892     3333     ", "&email_address=",
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
		tradewastemovein = new TradeWasteMoveIn(driver);
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
			// verify concession displayed immediately
			// verify additional contact displayed immediately
			// verify Property manager section displayed immediately
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Additional Contact", "5 Postal Address", "6 Concession",
							"7 mAnAgEr/aGeNt cOmPaNy dEtAiLs", "8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify CSS and lang files
			// verify the CSS and lang files
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterTenant),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerTenant), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.moveInDatePropMan, true),
					"Owner Agreement Date (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "Owner Agreement Date (DD/MM/YYYY)"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDatePropMan),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDatePropMan),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDatePropMan, true),
					"Required Field", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDatePropMan), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleYes),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleYes),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleNo),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleNo),
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
			// verify the populated data
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify not ticked
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed expected sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Paul & Mary Toniolo & Tinola)",
					"4 Additional Contact", "5 Postal Address", "6 Concession", "7 mAnAgEr/aGeNt cOmPaNy dEtAiLs",
					"8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.propManager);
			// verify Trade Waste Section not yet displayed
			// verify the additional contact section is displayed
			// verify that the mAnAgEr/aGeNt cOmPaNy dEtAiLs is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Additional Contact", "5 Postal Address",
					"6 mAnAgEr/aGeNt cOmPaNy dEtAiLs", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify Trade Waste Section not yet displayed
			// verify the additional contact section is displayed
			// verify that the mAnAgEr/aGeNt cOmPaNy dEtAiLs is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Additional Contact", "5 Postal Address", "6 Concession",
							"7 mAnAgEr/aGeNt cOmPaNy dEtAiLs", "8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify Trade Waste Section not yet displayed
			// verify the additional contact section is displayed
			// verify that the mAnAgEr/aGeNt cOmPaNy dEtAiLs is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Paul & Mary Toniolo & Tinola)",
					"4 Additional Contact", "5 Postal Address", "6 Concession", "7 mAnAgEr/aGeNt cOmPaNy dEtAiLs",
					"8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDatePropMan);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		String past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		supplydetailsmovein.moveInDatePropMan.sendKeys(past11Days, Keys.TAB);
		scrollPageDown(350);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(accountdetailsmovein.header);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDatePropMan);

		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		String future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(future21Days, Keys.TAB);
		// click header of next section to validate
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
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
		scrollPageUp(300);
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.moveInDatePropMan);

		// let's put a valid move in date as 5 days from past
		String past5Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past5D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -5, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(past5Days, Keys.TAB);
		this.propManMoveInDate = past5Days;
		this.propManMoveInDateCRM = past5D;

		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.propManSettleNo);
		// verify the validations for the Prop Man Settlement Date
		clickElementAction(supplydetailsmovein.propManSettleNo);
		clickElementAction(supplydetailsmovein.lblPropManSettle);
		pauseSeleniumExecution(500);
		// verify field is not in error state
		// verify the fix for bug ticket BBPRTL-2043
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.settlementDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleNo),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.settlementDatePropMan, true),
				"sEtTlEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sEtTlEmEnT DaTe (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconSettlementDatePropMan),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDatePropMan),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's get the current date then get a date 11 days from the past
		// verify that an error is returned
		past11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDatePropMan.sendKeys(past11Days, Keys.TAB);
		// initial click on ticked field already
		// to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.propManSettleNo);
		scrollPageDown(350);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.settlementDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSettlementDatePropMan, true),
				"sEtTlEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSettlementDatePropMan),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.settlementDatePropMan), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconSettlementDatePropMan),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDatePropMan),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSettlementDatePropMan, true),
				"iNvAlId dAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSettlementDatePropMan), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's clear the date using back space since clear() does not work
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		// got this exception when trying to delete the value
		// org.openqa.selenium.ElementNotInteractableException: element not interactable
		// so we just click the input date again in attempt to fix the issue
		clearDateField(supplydetailsmovein.settlementDatePropMan);

		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDatePropMan.sendKeys(future21Days, Keys.TAB);
		// click header of next section to validate
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.settlementDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's clear the date using back space since clear() does not work
		clearDateField(supplydetailsmovein.settlementDatePropMan);

		// let's put a valid move in date as 6 days from past
		String past6Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -6, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past6D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -6, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDatePropMan.sendKeys(past6Days, Keys.TAB);
		this.propManSettlementDate = past6Days;
		this.propManSettlementDateCRM = past6D;
		// verify CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSettlementDatePropMan), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.settlementDatePropMan), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconSettlementDatePropMan),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSettlementDatePropMan),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		// verify the holiday rental required fields
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// not sure why on thirdPartyPrefill and executed on jenkins,
			// this is in error state sometimes.
			// putting a pause to see if the issue would be fixed
			pauseSeleniumExecution(2000);
		}
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsiblePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.whoIsResponsibleOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.next);
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
			// verify the additional contact section is displayed
			// verify the Main Account Contact section name changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 tEnAnT/OwNeR AcCoUnT CoNtAcT", "4 Additional Contact", "5 Postal Address",
					"6 mAnAgEr/aGeNt cOmPaNy dEtAiLs", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			slowSendKeys(supplydetailsmovein.supplyAddSearch, "Unit2 / 14 second avenue glenelg east", true, 300);
			// put a pause to avoid another stale element
			pauseSeleniumExecution(2000);
			chooseAddress(supplydetailsmovein.supplyAddressesDiv, "unit 2/14 Second Avenue, Glenelg East SA");
			pauseSeleniumExecution(1000);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the additional contact section is displayed
			// verify the Main Account Contact section name changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 tEnAnT/OwNeR AcCoUnT CoNtAcT",
							"4 Additional Contact", "5 Postal Address", "6 Concession",
							"7 mAnAgEr/aGeNt cOmPaNy dEtAiLs", "8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the additional contact section is displayed
			// verify the Main Account Contact section name changed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 tEnAnT/OwNeR AcCoUnT CoNtAcT (Paul & Mary Toniolo & Tinola)",
					"4 Additional Contact", "5 Postal Address", "6 Concession", "7 mAnAgEr/aGeNt cOmPaNy dEtAiLs",
					"8 Direct Debit", "9 Additional Note", "10 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
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
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "2", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "14", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Second", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Avenue", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Glenelg East", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "South Australia", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "5045", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's update the state
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			pauseSeleniumExecution(600);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// verify that the spinner is not displayed since public holiday checking
			// is disabled in the portal config
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "14", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Second", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Wrong St Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Glenelg East", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Wrong State", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "5045", assertionErrorMsg(getLineNumber()));
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
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("2");
			supplydetailsmovein.supplyAddStreetType.clear();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Avenue", Keys.TAB);

			// let's update the state
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			pauseSeleniumExecution(600);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// verify that the spinner is not displayed since public holiday checking
			// is disabled in the portal config
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "2", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "14", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Second", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Fire Track", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Glenelg East", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "South Australia", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "5045", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's update the street type
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Avenue", Keys.TAB);
			// let's update the state
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			pauseSeleniumExecution(600);
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			// verify that the spinner is not displayed since public holiday checking
			// is disabled in the portal config
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

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
		clickElementAction(supplydetailsmovein.supplyConnected);

		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		scrollPageDown(500);
		clickElementAction(supplydetailsmovein.medCoolingNo);

		// upload the files
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
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement Test PNG Type 01 .png 0.1 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// remove the file that we uploaded
		// let's remove a file that was uploaded
		deleteUploadedFiles(supplydetailsmovein.dragAndDropUploadedFiles, "Test PNG Type 01.png");
		// verify the text displayed
		String containerText = getDisplayedText(supplydetailsmovein.dialogContainerText, true);
		softAssertion.assertEquals(containerText, "Are you sure you would like to remove this file?",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.dialogRemoveFileYes);
		pauseSeleniumExecution(1000);
		// verify the file was removed
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were removed
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
				assertionErrorMsg(getLineNumber()));
		// verify area in error state already
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
		// verify we are in not the next section
		softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		// upload file
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "typing jim carrey.gif");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement typing jim carrey .gif 0.5 MB File uploaded successfully",
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
	 * */
	@Test(priority = 3, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyMainContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		String wholeForm = getDisplayedText(mainaccountcontactmovein.wholeForm, true);
		softAssertion.assertFalse(wholeForm.contains("add_circle ADD ANOTHER CONTACT"),
				assertionErrorMsg(getLineNumber()));
		// verify the text displayed
		String whoIsResponsible = getDisplayedText(mainaccountcontactmovein.lblResponsibleForPaying, true);
		softAssertion.assertEquals(whoIsResponsible,
				"pLeAsE PrOvIdE ThE tEnAnT/OwNeR cOnTaCt wHo wIlL Be rEsPoNsIbLe fOr eNsUrInG ThAt tHe aCcOuNt iS PaId wHeN It bEcOmEs dUe.",
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblResponsibleForPaying), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		// verify add another contact link is not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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

		// verify the URL prefill
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Paul & Mary",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Toniolo & Tinola",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "0238921111",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "0348922222",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false), "0458923333",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.contactSecretCode, false)),
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
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
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

		String firstName = "Paul & Mary";
		String lastName = "Toniolo";
		mainaccountcontactmovein.firstName.sendKeys(firstName);
		mainaccountcontactmovein.lastName.sendKeys(lastName);
		javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		mainaccountcontactmovein.mobilePhone.sendKeys("+61 4 6941 1390");
		mainaccountcontactmovein.businessPhone.sendKeys("+61298732550");
		mainaccountcontactmovein.contactSecretCode.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");

		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Tenant/Owner Account Contact")
						.getText());
		String expSectionHeader = concatStrings("3 tEnAnT/OwNeR AcCoUnT CoNtAcT (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify that the Add Another Contact link is not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the additional contacts section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contacts section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact01() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// did this because for the Main Account Contact
		// the node name is also app-account-contact
		String wholeForm = getDisplayedText(additionalcontactmovein.wholeForm.get(1), true);
		// verify the fix for bug ticket BBPRTL-2044
		softAssertion.assertFalse(wholeForm.contains(
				"pLeAsE PrOvIdE ThE tEnAnT/OwNeR cOnTaCt wHo wIlL Be rEsPoNsIbLe fOr eNsUrInG ThAt tHe aCcOuNt iS PaId wHeN It bEcOmEs dUe."),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(wholeForm.contains("add_circle ADD ANOTHER CONTACT"),
				assertionErrorMsg(getLineNumber()));
		wholeForm = wholeForm.toLowerCase();
		softAssertion.assertFalse(wholeForm.contains(
				"please provide the tenant/owner contact who will be responsible for ensuring that the account is paid when it becomes due."),
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
		clickElementAction(postaladdressmovein.header);
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

		scrollPageUp(500);
		// verify we can go back to the previous section
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the main contact section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);

		// verify the fields are still in error state
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

		String firstName = "Emma";
		String lastName = "Harding-Grimmond";
		additionalcontactmovein.addCont1FirstName.sendKeys(firstName);
		additionalcontactmovein.addCont1LastName.sendKeys(lastName);

		// verify the validations for Date of Birth
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		// get the current date and add 1 day
		String todayPlus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		// let's remove the current year then concatenate birthYr
		String invalidBirthDate = getString(todayPlus1, 0, todayPlus1.length() - 4);
		invalidBirthDate = invalidBirthDate + birthYr;
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(additionalcontactmovein.addCont1DateOfBirth);
		pauseSeleniumExecution(1000);
		additionalcontactmovein.addCont1DateOfBirth.sendKeys(invalidBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);
		String expectedDOB = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		scrollPageDown(500);
		clickElementAction(postaladdressmovein.header);
		pauseSeleniumExecution(1000);
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we're still in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the current date of birth value
		String dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		softAssertion.assertEquals(dateOfBirth, expectedDOB, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// initial click to resolve ElementNotInteractableException exception
		clearDateField(additionalcontactmovein.addCont1DateOfBirth);
		birthYr = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 18);
		// get the current date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		additionalcontactmovein.addCont1DateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		this.dateOfBirthAddCont1 = dateOfBirth;
		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("Abc123546");
		additionalcontactmovein.addCont1DriversLicenceState.sendKeys("Queensland", Keys.TAB);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+61 4 6941 1390");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("02 987 32500");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+61 2 3698 5000");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./AddCont1");

		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Additional Contact").getText());
		String expSectionHeader = concatStrings("4 Additional Contact (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify that the button Add Another Contact is displayed
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont1AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		// verify that the button Remove This Contact is not displayed
		// can't use isElementExists(List<WebElement>) because the element exists in the
		// page
		// there's only an attribute added that makes it hidden
		softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
	@Test(priority = 5, dependsOnMethods = { "verifyAdditionalContact01" })
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

		clickElementAction(postaladdressmovein.sameSupAddressYes);

		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
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
		concessionmovein.cardHolderName.sendKeys("Natsu Dragneel");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 2);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		assertEquals(typeChosen, "DVA Gold Card", "The chosen Card Type is not correct");

		concessionmovein.cardNumber.sendKeys("00321941500");
		scrollPageDown(800);

		// let's upload concession card details
		uploadConcessionFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg", "typing jim carrey.gif");
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
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We are not yet in the Manager/Agent Company Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = { "verifyConcessionDetails" })
	public void verifyManagerAgentDetails() {

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
		// verify the fix for bug ticket BBPRTL-2042
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
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
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

		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		// verify required fields in error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy cOnTaCt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
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
		clickElementAction(managerholidaylettingmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the concession section
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

		// verify required fields in still error state
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(managerholidaylettingmovein.labelInput, "cOmPaNy cOnTaCt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(managerholidaylettingmovein.labelInput, "pLeAsE StArT TyPiNg cOmPaNy aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(managerholidaylettingmovein.iconCompanyAddress),
				GLOBE_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCompanyAddress),
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

		managerholidaylettingmovein.companyName.sendKeys("Comp Name");
		managerholidaylettingmovein.companyContactNum.sendKeys("1 to 6 pm");
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
		// verify CSS
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
		// verify CSS and lang files
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

		clickElementAction(managerholidaylettingmovein.next);
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

		// verify we can still go back to the previous section
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the concession section
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");
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
		managerholidaylettingmovein.country.sendKeys("Clover Kingdom", Keys.TAB);
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

		slowSendKeys(managerholidaylettingmovein.companyAddress, "Unit 301 192 Marine PDE Coolangatta", true, 300);
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
		chooseAddress(managerholidaylettingmovein.companyAddressesDiv, "unit 301/192 Marine Parade, Coolangatta QLD");
		pauseSeleniumExecution(1000);

		String add01 = getDisplayedValue(managerholidaylettingmovein.address01, true);
		String add02 = getDisplayedValue(managerholidaylettingmovein.address02, true);
		String add03 = getDisplayedValue(managerholidaylettingmovein.address03, true);
		String add04 = getDisplayedValue(managerholidaylettingmovein.address04, true);
		String city = getDisplayedValue(managerholidaylettingmovein.city, true);
		String state = getDisplayedValue(managerholidaylettingmovein.state, true);
		String postcode = getDisplayedValue(managerholidaylettingmovein.postCode, true);
		String country = getDisplayedValue(managerholidaylettingmovein.country, true);
		softAssertion.assertEquals(add01, "Unit 301", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "192 Marine Parade", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Coolangatta", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4225", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyContactNum, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(managerholidaylettingmovein.floaterLblCountry, true), "cOuNtRy",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.floaterLblCountry), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(managerholidaylettingmovein.underlineCountry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Manager/Agent Company Details")
						.getText());
		softAssertion.assertEquals(header, "7 mAnAgEr/aGeNt cOmPaNy dEtAiLs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(managerholidaylettingmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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

		clickElementAction(managerholidaylettingmovein.manualAddSearch);
		// verify address are still there
		add01 = getDisplayedValue(managerholidaylettingmovein.address01, true);
		add02 = getDisplayedValue(managerholidaylettingmovein.address02, true);
		add03 = getDisplayedValue(managerholidaylettingmovein.address03, true);
		add04 = getDisplayedValue(managerholidaylettingmovein.address04, true);
		city = getDisplayedValue(managerholidaylettingmovein.city, true);
		state = getDisplayedValue(managerholidaylettingmovein.state, true);
		postcode = getDisplayedValue(managerholidaylettingmovein.postCode, true);
		country = getDisplayedValue(managerholidaylettingmovein.country, true);
		softAssertion.assertEquals(add01, "Unit 301", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "192 Marine Parade", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Coolangatta", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4225", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	@Test(priority = 8, dependsOnMethods = { "verifyManagerAgentDetails" })
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

		// verify the fix for bug ticket BBPRTL-838
		// verify we can go to the previous section
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the previous section
		assertTrue(isElementDisplayed(managerholidaylettingmovein.country, 0),
				"We are not yet in the Manager/Agent Company Details section");
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);

		// verify we are in the Direct Debit section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd option is not displayed
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.noDirectDebitList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
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
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationBankAccount, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageUp(500);
		// verify the fix for bug ticket BBPRTL-838
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the concession section
		assertTrue(isElementDisplayed(concessionmovein.cardNumber, 0), "We are not yet in the Concession section");
		scrollPageDown(550);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccountName, 0),
				"We are not yet in the Direct Debit Details section");

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

		clickElementAction(directdebitmovein.authorisationBankAccount);
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);
		// verify fields still in error state
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

		scrollPageDown(550);
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(1000);
		switchToMWIframe();
		// verify the fields in error state
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
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the fix for ticket BBPRTL-874
		// verify we can go back to the previous section
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		// verify we are in the previous section
		assertTrue(isElementDisplayed(managerholidaylettingmovein.postCode, 0),
				"We are not yet in the Manager/Agent Company Details");
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0),
				"We are not yet in the Direct Debit Details section");

		switchToMWIframe();
		// verify the fields still in error state
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

		// enter valid credit card number and expiry
		switchToMWIframe();
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_03"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 9;
		String monthStr = Integer.toString(month);
		String expYrStrFull = Integer.toString(expYr);
		String expYrStr = getString(expYrStrFull, 2, 4);
		monthStr = concatStrings("0", monthStr);
		String expiry = concatStrings(monthStr, "/", expYrStr);
		directdebitmovein.creditCardExpiry.sendKeys(expiry, Keys.TAB);
		this.creditCardExpiry = expiry;
		this.creditCardExpiryMonth = monthStr;
		this.creditCardExpiryYearFull = expYrStrFull;
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
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
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(2000);
		switchToMWIframe();
		// verify the fields still in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
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
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		switchToMWIframe();
		directdebitmovein.creditCardName.sendKeys("Natasha Romanoff II");
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		clickElementAction(directdebitmovein.authorisationCreditCard);
		clickElementAction(additionalnotemovein.header);
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
		// verify the fix for ticket BBPRTL-874
		pauseSeleniumExecution(2000);
		// verify we are in the Additional notes section
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Notes section");

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
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

		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalnotemovein.header)) {
				scrollIntoView(additionalnotemovein.header);
			}
		}
		clickElementAction(additionalnotemovein.header);
		pauseSeleniumExecution(2000);
		// verify the fix for ticket BBPRTL-874
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0), "We are no longer in the Direct Debit section");
		// verify fields in error state
		switchToMWIframe();
		// verify the fields are in error state
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
		// verify field is still ticked
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's cancel the change
		clickElementAction(directdebitmovein.cancelCreditCardChange);
		pauseSeleniumExecution(1000);
		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Natasha Romanoff II", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_04"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Direct Debit").getText());
		softAssertion.assertEquals(header, "8 Direct Debit", assertionErrorMsg(getLineNumber()));
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
		// skip the Additional Notes section
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
	@Test(priority = 9, dependsOnMethods = { "verifyDirectDebitDetails" })
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
		String propManLettingAgent = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String additionalNotes = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate, " (Settlement on ", this.propManSettlementDate,
						") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Unit 2, 14 Second Avenue Glenelg East, Queensland, 5045 Service currently connected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Cooling NOT REQUIRED Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, concatStrings(
				"tEnAnT/OwNeR AcCoUnT CoNtAcT update Paul & Mary Toniolo Email Address: ",
				getProp("test_dummy_email_lower_case"),
				" Mobile Phone: +61469411390 Business Phone: +61298732550 Contact Secret: (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"tEnAnT/OwNeR AcCoUnT CoNtAcT Notification update Bills (Postal) Notifications and Reminders (SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Emma Harding-Grimmond Email Address: ",
				getProp("test_dummy_email_lower_case"),
				" Mobile Phone: +61469411390 Business Phone: 0298732500 A/Hours Phone: +61236985000 Birthdate: ",
				this.dateOfBirthAddCont1,
				" Personal Id: Driver Licence (Abc123546, Queensland) Contact Secret: (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./AddCont1)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (SMS) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Unit 2, 14 Second Avenue Glenelg East, Queensland, 5045",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Natsu Dragneel DVA Gold Card 00321941500 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(propManLettingAgent,
				"pRoPeRtY MaNaGeR / LeTtInG AgEnT update Comp Name 1 to 6 pm Unit 301 192 Marine Parade Coolangatta, Queensland, 4225 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Natasha Romanoff II Card: ending ",
						getProp("test_data_05"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickExactLinkNameFromElement(acceptancemovein.lifeSupportRow, "update");
		pauseSeleniumExecution(1000);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(supplydetailsmovein.lifeSupNo)) {
				scrollIntoView(supplydetailsmovein.lifeSupNo);
			}
		}
		clickElementAction(supplydetailsmovein.lifeSupNo);
		assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0), "Life Support was not updated to No");
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we go to the account details
		clickExactLinkNameFromElement(acceptancemovein.accountDetailsRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(accountdetailsmovein.commercial);
		pauseSeleniumExecution(1000);
		// verify it would validate
		clickElementAction(tradewastemovein.header);
		pauseSeleniumExecution(1000);
		// verify required fields for commercial
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// enter valid ACN
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_acn2"), Keys.TAB);
		// README
		// when running on jenkins, the loadingAbnAcn icon quickly disappears
		// that's why it goes to the next section, hence causing test case to fail.
		// This specific assertion aims to test that if the loadingAbnAcn icon is still
		// displayed
		// and we click accountdetailsmovein.next, it should not go to the next section.
		// When running on jenkins, it goes to the next section
		// because the loadingAbnAcn disappeared, so when we click
		// accountdetailsmovein.next
		// it goes to the next section resulting to failed assertion.
		// Cannot reproduce the test case failing manually
		// or when running on eclipse so will just skip this if running on jenkins.
		if (getAutomationSource().equals("jenkins")) {
			try {
				accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
				if (isElementExists(accountdetailsmovein.loadingAbnAcnList)) {
					// just added logging to see if loadingAbnAcn
					// still disappears too fast in the future.
					// If not then put back this validation.
					logScreenshot("SmallBusExistingContact01 is loadingAbnAcnList displayed before click");
				}
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}
		} else {
			try {
				accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
				if (isElementExists(accountdetailsmovein.loadingAbnAcnList)) {
					logScreenshot("SmallBusExistingContact01 is loadingAbnAcnList displayed before click");
					clickElementAction(accountdetailsmovein.next);
					// verify we are still in the account details
					assertTrue(isElementDisplayed(accountdetailsmovein.tradingName, 0),
							"We are no longer in the Account Details section");
				}
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}
		}
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the expected section names
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details", "2 Account Details",
				"3 Trade Waste", "create tEnAnT/OwNeR AcCoUnT CoNtAcT (Paul & Mary Toniolo)",
				"create Additional Contact (Emma Harding-Grimmond)", "create Postal Address",
				"create mAnAgEr/aGeNt cOmPaNy dEtAiLs", "create Direct Debit", "create Additional Note",
				"create Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1000);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify that we are automatically redirected in the Trade Waste section
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteDischargeNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the field is in error state
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
		pauseSeleniumExecution(500);
		// verify that the radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(tradewastemovein.tradeWasteEquipNo);
		clickElementAction(tradewastemovein.businessActivity);
		chooseFromList(tradewastemovein.businessActivityDiv, 3);
		pauseSeleniumExecution(1000);
		// upload trade waste files
		uploadTradeWasteFiles(ARTIFACTS_DIR, "planet_in_deep_space-wallpaper-1920x1080.jpg",
				"Sprin't 02 Story 'Board.pdf");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		// just increased the usual wait time to fix an issue
		// where the attachment was not yet uploaded
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
		String dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
		String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
		// verify the files that were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload planet_in_deep_space-wallpaper-1920x1080 .jpg 0.8 MB File uploaded successfully Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1100);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify each section
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		postalAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		propManLettingAgent = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		additionalNotes = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate, " (Settlement on ", this.propManSettlementDate,
						") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Unit 2, 14 Second Avenue Glenelg East, Queensland, 5045 Service currently connected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling NOT REQUIRED",
				assertionErrorMsg(getLineNumber()));
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		// verify Concession is no longer displayed
		softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(acctDetails, concatStrings("Account Details update Commercial Account ",
				getProp("test_data_valid_company_name_acn1_acn2"), " ABN/ACN ", getProp("test_data_valid_acn1")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradeWaste,
				"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Other'",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo,
				"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' Uploaded 2 site plans",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, concatStrings(
				"tEnAnT/OwNeR AcCoUnT CoNtAcT update Paul & Mary Toniolo Email Address: ",
				getProp("test_dummy_email_lower_case"),
				" Mobile Phone: +61469411390 Business Phone: +61298732550 Contact Secret: (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"tEnAnT/OwNeR AcCoUnT CoNtAcT Notification update Bills (Postal) Notifications and Reminders (SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Emma Harding-Grimmond Email Address: ",
				getProp("test_dummy_email_lower_case"),
				" Mobile Phone: +61469411390 Business Phone: 0298732500 A/Hours Phone: +61236985000 Contact Secret: (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./AddCont1)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (SMS) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Postal Address update Unit 2, 14 Second Avenue Glenelg East, Queensland, 5045",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(propManLettingAgent,
				"pRoPeRtY MaNaGeR / LeTtInG AgEnT update Comp Name 1 to 6 pm Unit 301 192 Marine Parade Coolangatta, Queensland, 4225 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Natasha Romanoff II Card: ending ",
						getProp("test_data_05"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(additionalNotes, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
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

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 17, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 20, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-business_number"),
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
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionLifeSupportFile = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
		String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
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
		this.sourceID = sessionSourceId;
		// let's confirm the values stored in the local storage
		String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);
		// verify the checkboxes are not ticked
		softAssertion.assertFalse(isElementTicked(acceptancemovein.firstCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.secondCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(acceptancemovein.thirdCheckbox, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.firstCheckbox);
		if (getPortalType().equals("standalone")) {
			// we use javaScriptClickButton to click it because the method
			// clickButton mistakenly clicks the link which opens a new tab in standalone
			javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		} else if (getPortalType().equals("embedded")) {
			clickElementAction(acceptancemovein.secondCheckbox);
		}
		// verify the checkboxes are ticked
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
//			softAssertion.assertEquals(actualSize, 9, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// add the property files before submitting the request
		addProp("SmallBusExistingContact01_propManSettlementDate", this.propManSettlementDate);
		addProp("SmallBusExistingContact01_propManMoveInDateCRM", this.propManMoveInDateCRM);
		addProp("SmallBusExistingContact01_propManSettlementDateCRM", this.propManSettlementDateCRM);
		addProp("SmallBusExistingContact01_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("SmallBusExistingContact01_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("SmallBusExistingContact01_sourceID", this.sourceID);
		addProp("SmallBusExistingContact01_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("SmallBusExistingContact01_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

		// submit the request
		clickElementAction(acceptancemovein.submit);
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
			softAssertion.assertEquals(submitMsg, "Submitting your request...", assertionErrorMsg(getLineNumber()));
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
