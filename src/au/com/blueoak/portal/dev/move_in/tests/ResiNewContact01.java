package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.JavascriptException;
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
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact01 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
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
	 * This is for the Tenant lease commencement date
	 */
	String tenantMoveInDate;

	/**
	 * This is for the Tenant lease commencement date for the CRM
	 */
	String tenantMoveInDateCRM;

	/** 
	 * 
	 * */
	String initialDate3rdPartyPref;

	/** 
	 * 
	 * */
	String moveInDateUrlPrefill;

	/**
	 * This is for the medicare expiry to be used in the acceptance page assertions
	 */
	String medicareExpiryMain;

	/**
	 * This is for the medicare expiry month for CRM checking
	 */
	String medicareExpiryMainMonth;

	/**
	 * This is for the medicare expiry month for CRM checking
	 */
	int medicareExpiryMainMonthInt;

	/**
	 * This is for the medicare expiry year for CRM checking
	 */
	String medicareExpiryMainYear;

	/**
	 * This is for the medicare expiry to be used in the acceptance page assertions
	 */
	String medicareExpiryAddCont1;

	/**
	 * This is for the medicare expiry month in the crm
	 */
	String medicareExpiryAddCont1Month;

	/**
	 * This is for the medicare expiry month in the crm
	 */
	int medicareExpiryAddCont1MonthInt;

	/**
	 * This is for the medicare expiry year in the crm
	 */
	String medicareExpiryAddCont1Year;

	/**
	 * This is for the direct debit credit card expiry in the acceptance page
	 * assertions
	 */
	String creditCardExpiry;

	/**
	 * This is for the credit card expiry month for CRM checking
	 */
	String creditCardExpiryMonth;

	/**
	 * This is for the credit card expiry year for CRM checking
	 */
	String creditCardExpiryYearFull;

	/**
	 * This is for the concession card expiry to be used in the acceptance page
	 * assertions
	 */
	String concessionExpiry;

	/**
	 * This is the Date of Birth for the Main Contact in the acceptance page
	 * assertions
	 * 
	 */
	String dateOfBirthMain;

	/**
	 * This is the Date of Birth for the Additional Contact 1 in the acceptance page
	 * assertions
	 * 
	 */
	String dateOfBirthAddContact1;

	/**
	 * This is the Date of Birth for the Additional Contact 2 in the acceptance page
	 * assertions
	 * 
	 */
	String dateOfBirthAddContact2;

	/**
	 * The source id value
	 */
	String sourceID1;

	/**
	 * The source id value
	 */
	String sourceID2;

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

		// upload the portal_config.css we are using
		uploadMoveInCustomCss(s3Access);

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "01\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

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
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.st, "Alexandra Headland", AustralianStatesEnum.vic,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, " config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&complex_name='001 Complex's", "&tenancy_type=", TenancyTypesEnum.uNiT.name(),
					"&tenancy_number=16", "&tenancy_street_number=6", "&tenancy_street_name=Mari",
					"&tenancy_street_type=", StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland",
					"&tenancy_postcode=4572", "&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
					"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
					"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
					getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -11,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, " config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&complex_name='001 Complex's", "&tenancy_type=", TenancyTypesEnum.uNiT.name(),
					"&tenancy_number=16", "&tenancy_street_number=6", "&tenancy_street_name=Mari",
					"&tenancy_street_type=", StreetTypesEnum.sT.name(), "&tenancy_suburb=Alexandra Headland",
					"&tenancy_postcode=4572", "&tenancy_state=", AustralianStatesEnum.qLD.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Monkey", "&contact_last_name=Luffy's",
					"&mobile_number=ABCDEFGHIJKLmnopqrstuvwxyz", "&business_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz",
					"&after_hour_phone=ABCDEFGHIJKLmnopqrstuvwxyz", "&email_address=",
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
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver);
		additionalcontactmovein = new AdditionalContactMoveIn(driver);
		postaladdressmovein = new PostalAddressMoveIn(driver);
		concessionmovein = new ConcessionMoveIn(driver);
		directdebitmovein = new DirectDebitMoveIn(driver);
		additionalnotemovein = new AdditionalNoteMoveIn(driver);
		acceptancemovein = new AcceptanceMoveIn(driver);
	}

	/**
	 * For ticket BBPRTL-217
	 * 
	 * - verify the fields are not in error state when the form loads - click Next
	 * button then verify the required fields - verify the required fields when
	 * Tenant is selected - verify the required fields when Owner is selected -
	 * verify the required fields when Property Manager or Letting Agent is selected
	 * - verify the validations for the allowed Tenant Move In date for past and
	 * future - verify the required fields for the Supply Address - verify the
	 * allowed values for the Tenancy Type, Street Type, State - verify that only
	 * address from Australia are displayed - verify the Supply Address would be
	 * overridden when users type in new address - upload life support/medical
	 * cooling attachment - verify the section header displayed
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

		// here we turned on the custom language file checking
		// and specified the browser language as Filipino.
		// uploaded the custom_en.json and custom_fil.json, however
		// there's no global language file fil.json
		// so we verify that it used the custom_en.json

		// let's verify the header and introduction
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

			portalmovein = new PortalMoveIn(driver);
			softAssertion.assertEquals(getCssBoxShadowProp(portalmovein.boxShadow), BOX_SHADOW_BORDER_CSTM,
					assertionErrorMsg(getLineNumber()));
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

			portalmovein = new PortalMoveIn(driver);
			softAssertion.assertEquals(getCssBoxShadowProp(portalmovein.boxShadow), BOX_SHADOW_BORDER_CSTM,
					assertionErrorMsg(getLineNumber()));
		}

		// verify the radio buttons are not selected
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify CSS and lang files
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
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true), "iNvAlId dAtE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddComplexName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetNum), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetType), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddCity), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddState), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddPostcode), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// should be ticked
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify that the supply address fields are editable
			softAssertion.assertTrue(isElementEnabled(supplydetailsmovein.supplyAddComplexName, 0),
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
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 sUpPlY DeTaIlS",
					"create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 pOsTaL AdDrEsS",
					"5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
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
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify button is displayed
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyAddQuickAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify CSS
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
			// verify CSS
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddComplexName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddTenancyType), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddTenancyNum), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetNum), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetType), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddCity), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddState), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddPostcode), LABEL_CSTM,
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddManualAddressSearchList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify CSS and lang files
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddSearch, true),
					"pLeAsE StArT TyPiNg sUpPlY AdDrEsS", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkLblSupplyConnectedIntro), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnectedQuestion, true),
				"iS SeRvIcE CuRrEnTlY CoNnEcTeD (i.e. On)?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblSupplyConnectedQuestion), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyConnected, true), "cOnNeCtEd",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyDisconnected, true), "dIsCoNnEcTeD",
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
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
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(400);
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
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
			// verify CSS
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_ERROR_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS and lang files
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
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true), "iNvAlId dAtE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyType, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyNum, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddTenancyType), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddTenancyNum), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetType), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddState), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddStreetType, true),
					"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddStreetType), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddState, true),
					"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddState), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS and lang files
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
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true), "iNvAlId dAtE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
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
		// verify the CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the required fields for the Tenant date
			clickElementAction(supplydetailsmovein.tenant);
			clickElementAction(supplydetailsmovein.lblMovingInHeader);
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify css ang lang files
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
			// test the fix for bug ticket BBPRTL-2088
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.moveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.moveInDateTenant);
			pauseSeleniumExecution(1000);
			clickElementAction(supplydetailsmovein.moveInDateTenant);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
					"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.next);
			waitForScreenToRender();
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant),
					DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify CSS
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
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify CSS
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
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// verify required fields for the Owner date
		clickElementAction(supplydetailsmovein.owner);
		clickElementAction(supplydetailsmovein.lblMovingInHeader);
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
		// verify Who is responsible for paying the account? fields not yet displayed
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsibleOwnerList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.whoIsResponsiblePropManList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify css ang lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwner),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwner), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.moveInDateOwner, true),
				"mOvE In dAtE (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "mOvE In dAtE (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerSettle, true),
				"sEtTlEmEnT DaTe sAmE As mOvE In dAtE?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerSettle), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerSettleYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerSettleNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerSettleYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerSettleNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHoliday, true),
				"iS ThIs a hOlIdAy rEnTaL Or hOlIdAy lEtTiNg?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHoliday), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHolidayYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHolidayNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHolidayYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHolidayNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateOwner, true),
				"mOvE In dAtE (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateOwner), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateOwner, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateOwner), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(supplydetailsmovein.hintMoveInDateOwner, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(400);
		// click Next to validate
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false)),
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
		// verify CSS
		softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "mOvE In dAtE (DD/MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateOwner), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateOwner), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateOwner, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateOwner), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(supplydetailsmovein.hintMoveInDateOwner, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerSettleNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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
		clickElementAction(supplydetailsmovein.lblMovingInHeader);
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
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
		// verify css ang lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManager),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManager),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.moveInDatePropMan, true),
				"oWnEr aGrEeMeNt dAtE (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(supplydetailsmovein.labelInput, "oWnEr aGrEeMeNt dAtE (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDatePropMan),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDatePropMan),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManSettle, true),
				"sEtTlEmEnT DaTe sAmE As oWnEr aGrEeMeNt dAtE?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManSettle), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManSettleYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManSettleNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManSettleYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblPropManSettleNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHoliday, true),
				"iS ThIs a hOlIdAy rEnTaL Or hOlIdAy lEtTiNg?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHoliday), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHolidayYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwnerPropManHolidayNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHolidayYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblOwnerPropManHolidayNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDatePropMan, true),
				"oWnEr aGrEeMeNt dAtE (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDatePropMan), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDatePropMan),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDatePropMan),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDatePropMan, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDatePropMan), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// click Next to validate
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
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
		// verify CSS
		softAssertion.assertEquals(
				getPlaceholderCss(supplydetailsmovein.labelInput, "oWnEr aGrEeMeNt dAtE (DD/MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDatePropMan),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDatePropMan),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDatePropMan, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDatePropMan), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterPropManSettleNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerPropManSettleNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterOwnerPropManHolidayNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerOwnerPropManHolidayNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddSearch, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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

		clickElementAction(supplydetailsmovein.tenant);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT", "4 pOsTaL AdDrEsS",
							"5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDateTenant);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify Trade Waste section not displayed
			// verify that no additional contact section is displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 sUpPlY DeTaIlS",
					"create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 pOsTaL AdDrEsS",
					"5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDateTenant);
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
		// click Tenant option to dismiss calendar
		clickElementAction(supplydetailsmovein.tenant);
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
				"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true), "iNvAlId dAtE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
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
		clearDateField(supplydetailsmovein.moveInDateTenant);

		// let's get the current date then get a date 21 days into the future
		// verify that an error is returned
		String future21Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future21Days, Keys.TAB);
		// click Tenant option to dismiss calendar
		clickElementAction(supplydetailsmovein.tenant);
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
				"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintMoveInDateTenant, true), "iNvAlId dAtE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintMoveInDateTenant), HINT_LABEL_CSTM,
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
		clearDateField(supplydetailsmovein.moveInDateTenant);

		// let's put a valid lease commencement date as 10 days from past
		String past10Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past10D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(past10Days, Keys.TAB);
		this.tenantMoveInDate = past10Days;
		this.tenantMoveInDateCRM = past10D;
		// click button again to dismiss the calendar
		clickElementAction(supplydetailsmovein.tenant);
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblMoveInDateTenant, true),
				"lEaSe cOmMeNcEmEnT DaTe (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblMoveInDateTenant), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.moveInDateTenant), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(supplydetailsmovein.iconMoveInDateTenant), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineMoveInDateTenant),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the required fields in the supply address
			clickElementAction(supplydetailsmovein.supplyAddSearch);
			waitForCssToRender();
			// verify CSS and lang files
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddSearch, true),
					"pLeAsE StArT TyPiNg sUpPlY AdDrEsS", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddSearch), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_ERROR_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddCantFindAdd, true),
					"search cAnNoT FiNd aDdReSs? ClIcK HeRe tO CoMpLeTe dEtAiLs mAnUaLlY",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndColorProp(supplydetailsmovein.supplyAddCantFindAdd),
					GOOGLELOOKUP_CANNOTFIND_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddCantFindAdd), MAT_OPTION_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddCantFindAdd);
			// verify fields not in error state
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
			// verify CSS and language files
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddQuickAddressSearch, true),
					"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddQuickAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddCity, true), "cItY/SuBuRb",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddPostcode, true), "pOsTcOdE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "cOmPlEx nAmE (iF KnOwN)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT NaMe"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT TyPe"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "cItY/SuBuRb"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTaTe"), PLACEHOLDER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "pOsTcOdE"), PLACEHOLDER_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.next);
			waitForScreenToRender();
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
			// verify the CSS and lang files
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "cOmPlEx nAmE (iF KnOwN)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy tYpE"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "tEnAnCy nUmBeR"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT NaMe"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTrEeT TyPe"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "cItY/SuBuRb"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "sTaTe"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(supplydetailsmovein.labelInput, "pOsTcOdE"),
					PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyType, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyNum, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddStreetName, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddStreetType, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddCity, true), "rEqUiReD FiElD",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddState, true), "rEqUiReD FiElD",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddPostcode, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddTenancyType), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddTenancyNum), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddStreetName), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddStreetType), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddCity), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddState), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintSupplyAddPostcode), HINT_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
					"cOmPlEx nAmE (iF KnOwN)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddComplexName.sendKeys("Testing Complex");

			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
					"tEnAnCy tYpE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(getElementFromList(supplydetailsmovein.supplyAddTenancyType, 1)),
					MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndProp(supplydetailsmovein.supplyAddTenancyTypeDiv),
					MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// verify error is displayed for tenancy type entered in lower case
			supplydetailsmovein.supplyAddTenancyType.sendKeys("unit", Keys.TAB);

			clickElementAction(supplydetailsmovein.supplyAddTenancyNum);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
					"tEnAnCy nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddStreetNum);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
					"sTrEeT NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddStreetNum.sendKeys("Testing Street Num");

			clickElementAction(supplydetailsmovein.supplyAddStreetName);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
					"sTrEeT NaMe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddStreetName.sendKeys("Testing Street Name");

			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
					"sTrEeT TyPe", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType),
					FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(getElementFromList(supplydetailsmovein.supplyAddStreetType, 1)),
					MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndProp(supplydetailsmovein.supplyAddStreetTypeDiv),
					MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// verify error is displayed for street type entered in lower case
			supplydetailsmovein.supplyAddStreetType.sendKeys("alley", Keys.TAB);

			clickElementAction(supplydetailsmovein.supplyAddCity);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
					"cItY/SuBuRb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddCity.sendKeys("Testing City/Suburb");

			clickElementAction(supplydetailsmovein.supplyAddState);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "sTaTe",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(getElementFromList(supplydetailsmovein.supplyAddState, 1)),
					MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndProp(supplydetailsmovein.supplyAddStateDiv),
					MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// verify error is displayed for state entered in lower case
			supplydetailsmovein.supplyAddState.sendKeys("tasmania", Keys.TAB);

			clickElementAction(supplydetailsmovein.supplyAddPostcode);
			waitForCssToRender();
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
					"pOsTcOdE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			supplydetailsmovein.supplyAddPostcode.sendKeys("Testing Postcode");

			clickElementAction(supplydetailsmovein.next);
			waitForScreenToRender();
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
			// verifiy CSS and lang files
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyType, true),
					"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddTenancyNum, true),
					"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddStreetType, true),
					"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.hintSupplyAddState, true),
					"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddComplexName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddTenancyNum),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetNum),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddStreetType),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddCity),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddState),
					UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddPostcode),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's verify we can change the address and existing
			// values should be overridden
			clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
			// verify CSS and lang files
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.supplyAddManualAddressSearch, true),
					"mAnUaL AdDrEsS SeArCh", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddManualAddressSearch),
					MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			slowSendKeys(supplydetailsmovein.supplyAddSearch, "Unit 16/ 6 Mari Street Alexandra", true, 300);
			// put a pause to avoid another stale element
			pauseSeleniumExecution(2000);
			softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(getElementFromList(supplydetailsmovein.supplyAddSearch, 1)),
					MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssBackgrndProp(supplydetailsmovein.supplyAddressesDiv),
					MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's confirm first that the records retrieved are only from Australia
			List<String> addresses1 = null;
			try {
				addresses1 = getAllSupplyAddress(supplydetailsmovein.supplyAddressesDiv);
			} catch (StaleElementReferenceException sere) {
				// let's initialize the page objects because we get a stale element
				supplydetailsmovein = new SupplyDetailsMoveIn(driver);
				addresses1 = getAllSupplyAddress(supplydetailsmovein.supplyAddressesDiv);
			}
			// let's separate each text in ; for logging
			String joined = StringUtils.join(addresses1, "; ");
			for (String address : addresses1) {
				softAssertion.assertTrue(address.toLowerCase().contains("australia"),
						concatStrings("These list of addresses ", joined.toString(),
								" does not contain 'australia' in line number [", getLineNumber(), "]"));
				// verify all assertions
				softAssertion.assertAll();
			}
			chooseAddress(supplydetailsmovein.supplyAddressesDiv, "unit 16/6 Mari Street, Alexandra Headland QLD",
					"unit 16/6 Mari Street, Alexandra Headland Queensland");
			pauseSeleniumExecution(1000);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(
					supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddComplexName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetNum, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
				softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
					|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				softAssertion.assertEquals(complexName, "'001 Complex's", assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "victoria", assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "6", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mari", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Alexandra Headland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4572", assertionErrorMsg(getLineNumber()));
		// verify that no spinner is displayed in the state
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// let's put a complex name
			supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
			clickElementAction(supplydetailsmovein.lblSupplyAddHeader);
			waitForCssToRender();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			// let's put a complex name
			supplydetailsmovein.supplyAddComplexName.sendKeys("'001 Complex's");
			// enter the Tenancy Type and Number
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("16");
			supplydetailsmovein.supplyAddStreetType.clear();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Street", Keys.TAB);
			supplydetailsmovein.supplyAddState.clear();
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
		}
		// verify CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddComplexName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyType), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddTenancyNum), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetNum), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddStreetType), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddCity), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddState), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.floaterLblSupplyAddPostcode), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddComplexName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddTenancyType), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddTenancyNum), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetNum), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddStreetType), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddCity), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddState), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.supplyAddPostcode), LABEL_CSTM,
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
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyAddQuickAddressSearch);
		softAssertion.assertEquals(
				getPlaceholderCss(supplydetailsmovein.labelInput, "pLeAsE StArT TyPiNg sUpPlY AdDrEsS"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(supplydetailsmovein.iconSupplyAddSearch), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineSupplyAddSearch),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.supplyAddManualAddressSearch);
		// verify the address is still there
		complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
		tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
		tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
		stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
		stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
		stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
		city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
		state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
		postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
		softAssertion.assertEquals(complexName, "'001 Complex's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
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
		clickElementAction(supplydetailsmovein.lblSupplyConnectedQuestion);
		waitForCssToRender();
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyConnected),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyDisconnected),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterSupplyUnknown),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyConnected),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyDisconnected),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerSupplyUnknown),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));

		// verify the Life Support Introduction
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblLifeSupIntro, true),
				"If you or a member of your household depend on electricity for life support equipment, you need to let us know. You can let us know by registering your device below. Once we’ve registered you for life support, we’ll advise your local energy distributor and send you a medical confirmation form and practical advice on what to do in the event of a power failure or interruption (planned or unplanned). You will need to complete the Medical Confirmation Form, have your medical practitioner sign it and then return it to us.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lblLifeSupIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));

		// choose yes for Life Support
		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(supplydetailsmovein.lblLifeSupQuestion);
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupYes),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupYes), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterLifeSupNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerLifeSupNo), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
				"pLeAsE SeLeCt oNe oR MoRe lIfE SuPpOrT DeViCeS In-uSe:", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lifeSupEquipIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr"), 3, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
				true, "iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE"), 3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe"), 3,
				0), assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
								"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertFalse(
						isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
								"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT"), 3, 0),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "vEnTiLaToR FoR LiFe sUpPoRt"),
				3, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(
				isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr"), 4,
						0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"oXyGeN CoNcEnTrAtOr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"hAeMoDiAlYsIs mAcHiNe")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true,
								"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
						LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true, "vEnTiLaToR FoR LiFe sUpPoRt")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(getMatListOptionElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineLifeSuppOtherInput),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(supplydetailsmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(400);
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
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
		// verify CSS
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxOuterCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				PSEUDOCHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify upload section and medical cooling in error state
		// verify fix for ticket BBPRTL-1156
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(supplydetailsmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(300);
		clickElementAction(supplydetailsmovein.medCoolingYes);
		clickElementAction(supplydetailsmovein.lblMedCoolingQuestion);
		waitForCssToRender();
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingYes),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(supplydetailsmovein.radioOuterMedCoolingNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(supplydetailsmovein.radioInnerMedCoolingNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(supplydetailsmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
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
		clickElementAction(supplydetailsmovein.lblLifeSupQuestion);
		// verify CSS
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"oXyGeN CoNcEnTrAtOr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"hAeMoDiAlYsIs mAcHiNe")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxOuterCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oXyGeN CoNcEnTrAtOr")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getMatPseudoChkbxInnerCss(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "hAeMoDiAlYsIs mAcHiNe")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true,
						"vEnTiLaToR FoR LiFe sUpPoRt")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getMatPseudoChkbxInnerCss(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, true, "oThEr")),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the Other option is in error state
		String errorMsg = getDisplayedText(supplydetailsmovein.hintLifeSuppOtherTextField, true);
		softAssertion.assertEquals(errorMsg, "sPeCiFy tHe nAmE Of tHe eQuIpMeNt", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineLifeSuppOtherInput),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.hintLifeSuppOtherTextField), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		// verify CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.lifeSuppOtherInput), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(supplydetailsmovein.underlineLifeSuppOtherInput),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.lifeSuppOtherInput);
		deleteAllTextFromField();
		supplydetailsmovein.lifeSuppOtherInput.sendKeys("\"Other\" Equipment's");
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

		scrollPageDown(500);
		// let's click the medical cooling again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.medCoolingYes);
		// upload life support and medical cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf", "typing jim carrey.gif",
				"g'alaxy-'wallpaper.jpeg");
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
				logDebugMessage(concatStrings("The expected number of attachments in class '", this.className,
						"' in the Supply Details is not yet the expected. Will be waiting again, current attempt(s) is <",
						Integer.toString(counter), ">"));
				// hover mouse to ensure there's an activity
				hoverToElementAction(supplydetailsmovein.dragAndDropArea);
				// let's add another wait time because the g'alaxy-'wallpaper.jpeg
				// cannot be previewed in the CRM/corrupted
				waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
				// check the number of files uploaded
				actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				logDebugMessage(concatStrings(this.className, " actualSize for S3 bucket ",
						S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
						"> and attachments ID's is/are -> ", objectIds.toString()));
				counter++;
			}
//			softAssertion.assertEquals(actualSize, expectedNumOfFiles, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}

		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.linkDragAndDropClickToBrowse),
				DRAG_AND_DROP_LINK_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg"),
				true), "dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getUploadedElementFileName(supplydetailsmovein.dragAndDropUploadedFiles,
								"Sprin't 02 Story 'Board.pdf")),
						UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getUploadedElementFileType(supplydetailsmovein.dragAndDropUploadedFiles,
								"Sprin't 02 Story 'Board.pdf")),
						UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getUploadedElementFileStatus(supplydetailsmovein.dragAndDropUploadedFiles,
								"Sprin't 02 Story 'Board.pdf")),
						UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getUploadedElementFileSize(supplydetailsmovein.dragAndDropUploadedFiles,
								"Sprin't 02 Story 'Board.pdf")),
						UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf"),
				true), "dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getCssColorProp(getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles,
								"Sprin't 02 Story 'Board.pdf")),
						UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif"),
				true), "dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files for button
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(supplydetailsmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 sUpPlY DeTaIlS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-219
	 * 
	 * - verify the required fields using the Next Button - verify the previous
	 * button will go to Supply Details - verify the cancelled ABN - verify the
	 * cancelled ACN - verify the invalid ABN - verify the invalid ACN - verify
	 * valid ABN
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

			// verify CSS and Lang files
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblAccountType, true), "aCcOuNt tYpE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblAccountType), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblResidential, true), "rEsIdEnTiAl",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblResidential), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblCommercial, true), "cOmMeRcIaL",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblCommercial), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create sUpPlY DeTaIlS", "2 aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 dIrEcT DeBiT", "6 aDdItIoNaL NoTe", "7 aCcEpTaNcE"));
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

			clickElementAction(accountdetailsmovein.next);
			waitForScreenToRender();
			// verify fields are in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify we can go to the previous section even though there are required
			// fields
			clickElementAction(accountdetailsmovein.previous);
			waitForScreenToRender();
			// verify we are in the Supply Details section
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			scrollPageDown(700);
			// go back to Account Details
			clickElementAction(supplydetailsmovein.next);
			waitForScreenToRender();

			// verify fields are still in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify CSS
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
					RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
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

			// verify the fields are not displayed
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
					assertionErrorMsg(getLineNumber()));
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.tradingName, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);

			// verify CSS and Lang files
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblAccountType, true), "aCcOuNt tYpE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblAccountType), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
					RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblResidential, true), "rEsIdEnTiAl",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblResidential), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblCommercial, true), "cOmMeRcIaL",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblCommercial), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(accountdetailsmovein.commercial);
		clickElementAction(accountdetailsmovein.lblCommercialDetails);
		waitForCssToRender();
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
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
			// verify there are no values
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.abnOrAcn, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.tradingName, false)),
					assertionErrorMsg(getLineNumber()));
			// verify CSS
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.lblCommercialDetails, true),
					"cOmMeRcIaL EnTiTy dEtAiLs", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.lblCommercialDetails), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(accountdetailsmovein.abnOrAcn, true),
					"cOmPaNy aBn oR AcN", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(accountdetailsmovein.tradingName, true), "tRaDiNg nAmE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy aBn oR AcN"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "tRaDiNg nAmE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
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

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// verify we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify required fields for commercial
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "cOmPaNy aBn oR AcN"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnRequired, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnRequired), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(accountdetailsmovein.labelInput, "tRaDiNg nAmE"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.abnOrAcn);
		waitForCssToRender();
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.floaterLblAbnOrAcn, true),
				"cOmPaNy aBn oR AcN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblAbnOrAcn), FLOATER_LABEL_CSTM,
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
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// click next again
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
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
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		// verify all assertions
		softAssertion.assertAll();

		// click next again
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
				"pRoViDeD AbN/AcN Is cUrReNtLy nOt aCtIvE", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnCancelled), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
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
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// click next again
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
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
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		// verify all assertions
		softAssertion.assertAll();

		// click next again
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// make sure we are still in the account details section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the error message displayed
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
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
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.abnOrAcn);
		deleteAllTextFromField();

		// verify valid ABN
		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn2"), Keys.TAB);
		clickElementAction(accountdetailsmovein.tradingName);
		waitForCssToRender();
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.floaterLblTradingName, true), "tRaDiNg nAmE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.floaterLblTradingName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		accountdetailsmovein.tradingName.sendKeys("Trading's LLC", Keys.TAB);
		// pause to ensure it finished searching
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the displayed ABN with company details
		String abnAcnAndCompany = getDisplayedValue(accountdetailsmovein.abnOrAcn, true);
		String tradingName = getDisplayedValue(accountdetailsmovein.tradingName, true);
		softAssertion.assertEquals(abnAcnAndCompany, concatStrings(getProp("test_data_valid_abn2"), " (",
				getProp("test_data_valid_company_name_abn1_abn2"), ")"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradingName, "Trading's LLC", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.abnOrAcn), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.tradingName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify the CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.abnOrAcn), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.tradingName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
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
				"nO MaTcHeS FoUnD FoR PrOvIdEd aBn/aCn", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.hintAbnAcnNotFound), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn), UNDERLINE_ERROR_CSTM,
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
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.abnOrAcn), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(accountdetailsmovein.iconAbnOrAcn), GLOBE_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineAbnOrAcn),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.tradingName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(accountdetailsmovein.underlineTradingName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(accountdetailsmovein.matStepHeader, "Account Details").getText());
		softAssertion.assertEquals(header, "2 aCcOuNt dEtAiLs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.residential);
		clickElementAction(accountdetailsmovein.lblAccountType);
		waitForCssToRender();
		// verify the CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterResidential),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerResidential),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(accountdetailsmovein.radioOuterCommercial),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(accountdetailsmovein.radioInnerCommercial),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(accountdetailsmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(accountdetailsmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();
		// verify we are now in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-307
	 * 
	 * - verify fields are not in error state initially - verify the required fields
	 * when clicking Next button - verify Provide None option is not displayed in
	 * the Personal Identification - verify the Bills SMS option is not available -
	 * verify that the Account Notifications and Reminders Postal option is not
	 * available - verify each notification text for each - verify the notification
	 * options that should be ticked by default and what's not ticked - verify that
	 * hovering the icon would display the notification tooltip - verify the
	 * required fields for the notifications - click Previous button and verify that
	 * we are in the Account Details section - verify the validations for Email
	 * Address - verify the validations when Medicare Card is chosen - verify the
	 * validations for Mobile Phone - verify the validations for Business Phone -
	 * verify the validations for After Hours Phone - verify that Add Another
	 * Contact link will validate the fields first before opening the Additional
	 * Contact section - verify the displayed header for the section
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
		softAssertion.assertEquals(billsNotifText, "info bIlLs(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info aCcOuNt nOtIfIcAtIoNs aNd rEmInDeRs (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info mArKeTiNg cOmMuNiCaTiOnS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblBillsNotif), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblAcctnotifAndRemindersNotif), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblMarketingComNotif), LABEL_CSTM,
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
		softAssertion.assertEquals(getCssColorProp(mainaccountcontactmovein.billsNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(mainaccountcontactmovein.acctnotifAndRemindersNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(mainaccountcontactmovein.marketingComNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify CSS and Lang files
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.firstName, true), "fIrSt nAmE",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "fIrSt nAmE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineFirstName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.lastName, true),
					"lAsT/FaMiLy nAmE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "lAsT/FaMiLy nAmE"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineLastName),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.dateOfBirth, true),
					"dAtE Of bIrTh (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "dAtE Of bIrTh (DD/MM/YYYY)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPersonalIDHeader, true),
					"pErSoNaL IdEnTiFiCaTiOn", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPersonalIDHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterDriversLicence),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerDriversLicence),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblDriversLicence, true),
					"aUsTrAlIaN DrIvErS LiCeNcE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblDriversLicence), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterPassport),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerPassport),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPassport, true), "pAsSpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPassport), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterMedicareCard),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerMedicareCard),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblMedicareCard, true),
					"mEdIcArE CaRd", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblMedicareCard), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblNotificationHeader, true),
					"hOw wOuLd yOu lIkE To bE NoTiFiEd?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblNotificationHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblNotificationIntro, true),
					"Paper bills put upward pressure on prices so we encourage you to use our eBilling service. Visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblNotificationIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.linkLblNotificationIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(mainaccountcontactmovein.linkLblNotificationIntro);
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.linkLblNotificationIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			// verify the fix for ticket BBPRTL-2032
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPostalNotifHeader, true), "pOsTaL",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPostalNotifHeader),
					NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblEmailNotifHeader, true), "eMaIl",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblEmailNotifHeader),
					NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblSMSNotifHeader, true), "sMs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblSMSNotifHeader), NOTIF_HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify the fix for ticket BBPRTL-2032
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.billsSMSNotAvail, true), "n/a",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.billsSMSNotAvail),
					NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getDisplayedText(mainaccountcontactmovein.acctnotifAndRemindersPostalNotAvail, true), "n/a",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.acctnotifAndRemindersPostalNotAvail),
					NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsPostal),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersSMS),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComPostal),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComSMS),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsPostal),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersSMS),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComPostal),
					CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComSMS),
					CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.emailAddress, true),
					"eMaIl aDdReSs", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "eMaIl aDdReSs"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.mobilePhone, true),
					"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "mObIlE PhOnE NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.businessPhone, true),
					"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "bUsInEsS HoUrS PhOnE NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.afterhoursPhone, true),
					"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "aFtEr hOuRs pHoNe nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
					"create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 pOsTaL AdDrEsS",
					"5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
			List<String> expectedSectionMatIconColor = new ArrayList<>(
					Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
							MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
			softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
					assertionErrorMsg(getLineNumber()));
			// check CSS for each expected header
			softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblFirstName), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblLastName), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.firstName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lastName), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineFirstName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineLastName),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.dateOfBirth, true),
					"dAtE Of bIrTh (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "dAtE Of bIrTh (DD/MM/YYYY)"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth),
					DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPersonalIDHeader, true),
					"pErSoNaL IdEnTiFiCaTiOn", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPersonalIDHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterDriversLicence),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerDriversLicence),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblDriversLicence, true),
					"aUsTrAlIaN DrIvErS LiCeNcE", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblDriversLicence), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterPassport),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerPassport),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPassport, true), "pAsSpOrT",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPassport), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterMedicareCard),
					RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerMedicareCard),
					RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblMedicareCard, true),
					"mEdIcArE CaRd", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblMedicareCard), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblNotificationHeader, true),
					"hOw wOuLd yOu lIkE To bE NoTiFiEd?", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblNotificationHeader), HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblNotificationIntro, true),
					"Paper bills put upward pressure on prices so we encourage you to use our eBilling service. Visit our Terms and Conditions",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblNotificationIntro), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.linkLblNotificationIntro), LINK_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			hoverToElementAction(mainaccountcontactmovein.linkLblNotificationIntro);
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.linkLblNotificationIntro),
					LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblPostalNotifHeader, true), "pOsTaL",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblPostalNotifHeader),
					NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblEmailNotifHeader, true), "eMaIl",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblEmailNotifHeader),
					NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblSMSNotifHeader, true), "sMs",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblSMSNotifHeader), NOTIF_HEADER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			// verify the fix for ticket BBPRTL-2032
			softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.billsSMSNotAvail, true), "n/a",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.billsSMSNotAvail),
					NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getDisplayedText(mainaccountcontactmovein.acctnotifAndRemindersPostalNotAvail, true), "n/a",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.acctnotifAndRemindersPostalNotAvail),
					NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsPostal),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersSMS),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComPostal),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComEmail),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComSMS),
					CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsPostal),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersSMS),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComPostal),
					CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComEmail),
					CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComSMS),
					CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblEmailAddress), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblEmailAddress), FLOATER_LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.emailAddress), LABEL_CSTM,
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
					UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.mobilePhone, true),
					"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "mObIlE PhOnE NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.businessPhone, true),
					"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "bUsInEsS HoUrS PhOnE NuMbEr"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.afterhoursPhone, true),
					"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					getPlaceholderCss(mainaccountcontactmovein.labelInput, "aFtEr hOuRs pHoNe nUmBeR"),
					PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
					UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.contactSecretCode, true),
				"sEcReT CoDe, sPeEdS Up vErIfIcAtIoN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(mainaccountcontactmovein.labelInput, "sEcReT CoDe , sPeEdS Up vErIfIcAtIoN"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineContactSecretCode),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.addAnotherContact, true),
				"add_circle aDd aNoThEr cOnTaCt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.addAnotherContact),
				ADD_REMOVE_CONTACT_BUTTON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(mainaccountcontactmovein.iconAddAnotherContact),
				ADD_ANOTHER_CONTACT_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(mainaccountcontactmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(mainaccountcontactmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the url prefill
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Monkey",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Luffy's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.businessPhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false)),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
					"create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 pOsTaL AdDrEsS",
					"5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.firstName);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.lastName);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.emailAddress);
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
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
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
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsPostal),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the Postal Address section is still displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the Postal Address section is still displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs", "3 mAiN AcCoUnT CoNtAcT",
							"4 pOsTaL AdDrEsS", "5 cOnCeSsIoN", "6 dIrEcT DeBiT", "7 aDdItIoNaL NoTe", "8 aCcEpTaNcE"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintFirstName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintFirstName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintLastName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintLastName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmptyMobilePhone, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmptyMobilePhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintContactSecretCode, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintContactSecretCode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "fIrSt nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineFirstName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "lAsT/FaMiLy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineLastName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "dAtE Of bIrTh (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterDriversLicence),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterPassport),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterMedicareCard),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsPostal),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "eMaIl aDdReSs"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "mObIlE PhOnE NuMbEr"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(mainaccountcontactmovein.labelInput, "bUsInEsS HoUrS PhOnE NuMbEr"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "aFtEr hOuRs pHoNe nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(mainaccountcontactmovein.labelInput, "sEcReT CoDe , sPeEdS Up vErIfIcAtIoN"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineContactSecretCode),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous even though required fields not yet supplied
		clickElementAction(mainaccountcontactmovein.previous);
		waitForScreenToRender();
		// verify we are in the account details section
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// go back to the main account contact section
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();

		// verify it will return an error for email address 'email test@testing.com'
		String invalidEmail = "email test@testing.com";
		String validEmail = "emailtest@testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		waitForEmailErrorToChange();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblEmailAddress, true),
				"eMaIl aDdReSs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblEmailAddress), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		clickElementAction(mainaccountcontactmovein.mobilePhone);
		waitForEmailErrorToChange();
		// verify space got trimmed
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false), validEmail,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address '@testing.com'
		invalidEmail = "@testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address ' @testing.com'
		invalidEmail = " @testing.com";
		String invalidEmailExp = "@testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmailExp), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email~testing.com'
		invalidEmail = "email~testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email`testing.com'
		invalidEmail = "email`testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email!testing.com'
		invalidEmail = "email!testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email#testing.com'
		invalidEmail = "email#testing.com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing,com'
		invalidEmail = "email@testing,com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing/com'
		invalidEmail = "email@testing/com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing<com'
		invalidEmail = "email@testing<com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing>com'
		invalidEmail = "email@testing>com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing?com'
		invalidEmail = "email@testing?com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing;com'
		invalidEmail = "email@testing;com";
		// system automatically updated the semi-colon to comma
		// when displaying into the error message
		invalidEmailExp = "email@testing,com";
		clickElementAction(mainaccountcontactmovein.emailAddress);
		mainaccountcontactmovein.emailAddress.sendKeys(invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmailExp), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.emailAddress);
		deleteAllTextFromField();

		String firstName = "Monkey";
		String lastName = "Luffy's";

		clickElementAction(mainaccountcontactmovein.firstName);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblFirstName, true), "fIrSt nAmE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblFirstName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineFirstName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.lastName);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblLastName, true),
				"lAsT/FaMiLy nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblLastName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineLastName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertAll();

		mainaccountcontactmovein.firstName.sendKeys(firstName);
		mainaccountcontactmovein.lastName.sendKeys(lastName);

		clickElementAction(mainaccountcontactmovein.medicareCard);
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.medicareCardNumber, true),
				"mEdIcArE CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(mainaccountcontactmovein.medicareCardExpiry, true),
				"eXpIrY (MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterDriversLicence),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterPassport),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.radioOuterMedicareCard),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.radioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "mEdIcArE CaRd nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconMedicareCardExpiry),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.medicareCardNumber);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblMedicareCardNumber, true),
				"mEdIcArE CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblMedicareCardNumber),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblMedicareCardExpiry, true),
				"eXpIrY (MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblMedicareCardExpiry),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconMedicareCardExpiry),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation for Medicare Card
		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "mEdIcArE CaRd nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardNumber, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardExpiry, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the invalid medicare number
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		// click the label header to dismiss the calendar
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.medicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardNumber, true),
				"iNvAlId mEdIcArE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(mainaccountcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardExpiry, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the invalid expiry
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
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(mainaccountcontactmovein.medicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.medicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardNumber, true),
				"iNvAlId mEdIcArE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.medicareCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintMedicareCardExpiry, true),
				"iNvAlId dAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintMedicareCardExpiry), HINT_LABEL_CSTM,
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
		this.medicareExpiryMain = medCareExp;
		this.medicareExpiryMainMonth = concatStrings("0", monthStr);
		this.medicareExpiryMainMonthInt = month;
		this.medicareExpiryMainYear = expYrStr;

		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		verifyStringContains(true, medCareExp, medExp);

		// let's verify the validations for the mobile phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.mobilePhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblMobilePhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.mobilePhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		softAssertion.assertEquals(mobPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblMobilePhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.mobilePhone.sendKeys("  012 345  678 9  1234  ");
		mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(mobPhone, "+01234567891234", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblMobilePhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the validations for the business phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.businessPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblBusinessPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.businessPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		softAssertion.assertEquals(busPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblBusinessPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.businessPhone.sendKeys("  987 654  321 0  02345  ");
		busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(busPhone, "+987654321002345", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblBusinessPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the validations for the after hours phone
		// verify that alpha characters not allowed
		slowSendKeys(mainaccountcontactmovein.afterhoursPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(afterHoursPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblAfterhoursPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(mainaccountcontactmovein.afterhoursPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		softAssertion.assertEquals(afterHoursPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblAfterhoursPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.afterhoursPhone.sendKeys("  654 321  098 7  732500  ");
		afterHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(afterHoursPhone, "+6543210987732500", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblAfterhoursPhone), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		// verify CSS and lang files
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintInvalidMobilePhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintInvalidBusinessPhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintInvalidAfterhoursPhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintInvalidMobilePhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintInvalidBusinessPhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintInvalidAfterhoursPhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblContactSecretCode, true),
				"sEcReT CoDe, sPeEdS Up vErIfIcAtIoN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblContactSecretCode),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineContactSecretCode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.mobilePhone.clear();
		mainaccountcontactmovein.businessPhone.clear();
		mainaccountcontactmovein.afterhoursPhone.clear();
		// enter valid phone numbers
		mainaccountcontactmovein.mobilePhone.sendKeys("0212345680");
		mainaccountcontactmovein.businessPhone.sendKeys("0387643210");
		mainaccountcontactmovein.afterhoursPhone.sendKeys("0465320980", Keys.TAB);

		// verify CSS
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintContactSecretCode, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintContactSecretCode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.contactSecretCode.sendKeys("Sekrekt's #001");
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterBillsEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(mainaccountcontactmovein.checkBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsPostal),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerBillsEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComPostal),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(mainaccountcontactmovein.checkBoxInnerMarketingComSMS),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.firstName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lastName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.medicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.medicareCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.emailAddress), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.mobilePhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.businessPhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.afterhoursPhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.contactSecretCode), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineFirstName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineLastName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMedicareCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineEmailAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineContactSecretCode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the section header
		String actSectionHeader = normalizeSpaces(
				getElementFrmMatStepHdrTag(mainaccountcontactmovein.matStepHeader, "Main Account Contact").getText());
		String expSectionHeader = concatStrings("3 mAiN AcCoUnT CoNtAcT (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		waitForScreenToRender();
		// verify we are in the in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-346
	 * 
	 * - verify fields are not in error state initially - verify the Provide None
	 * option is not displayed - verify SMS checkbox is not displayed for Bills -
	 * verify the Postal checkbox is not displayed for Account Notifications and
	 * Reminders - verify each notification text label - verify the default
	 * notifications that should be ticked and what should not - verify hovering
	 * into the image would display the tooltip message - verify the required fields
	 * for the notification settings - verify hitting Next button would validate the
	 * fields - verify hitting Previous would redirect us in the previous section
	 * which is Main Account Contact - verify that the Add Another Contact link is
	 * no longer displayed in the Main Account Contact section - verify the
	 * validations for Email Address - verify the validations when Medicare Card is
	 * chosen - verify the validations for Mobile Phone - verify the validations for
	 * Business Phone - verify the validations for After Hours Phone - verify we
	 * cannot go to the additional contact if a required field not yet populated -
	 * verify the action to Add Another contact is displayed - verify the action
	 * remove additional contact is displayed - verify the section header
	 * 
	 */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact01() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		softAssertion.assertEquals(billsNotifText, "info bIlLs(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info aCcOuNt nOtIfIcAtIoNs aNd rEmInDeRs (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info mArKeTiNg cOmMuNiCaTiOnS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblBillsNotif), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblAcctnotifAndRemindersNotif), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.lblMarketingComNotif), LABEL_CSTM,
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
		softAssertion.assertEquals(getCssColorProp(additionalcontactmovein.addCont1BillsNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getCssColorProp(additionalcontactmovein.addCont1AcctnotifAndRemindersNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(additionalcontactmovein.addCont1MarketingComNotifTooltipIcon),
				NOTIFICATION_ICON_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify CSS and Lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
				"create aCcOuNt dEtAiLs", "create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 aDdItIoNaL CoNtAcT",
				"5 pOsTaL AdDrEsS", "6 cOnCeSsIoN", "7 dIrEcT DeBiT", "8 aDdItIoNaL NoTe", "9 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1FirstName, true),
				"fIrSt nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "fIrSt nAmE"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineFirstName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1LastName, true),
				"lAsT/FaMiLy nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "lAsT/FaMiLy nAmE"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineLastName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1DateOfBirth, true),
				"dAtE Of bIrTh (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "dAtE Of bIrTh (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconDateOfBirth),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineDateOfBirth),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblPersonalIdentification, true),
				"pErSoNaL IdEnTiFiCaTiOn", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblPersonalIdentification),
				HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterDriversLicence),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblDriversLicence, true),
				"aUsTrAlIaN DrIvErS LiCeNcE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblDriversLicence), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterPassport),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblPassport, true), "pAsSpOrT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblPassport), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterMedicareCard),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblMedicareCard, true),
				"mEdIcArE CaRd", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblMedicareCard), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblNotificationHeader, true),
				"hOw wOuLd yOu lIkE To bE NoTiFiEd?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblNotificationHeader),
				HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblNotificationIntro, true),
				"Paper bills put upward pressure on prices so we encourage you to use our eBilling service. Visit our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblNotificationIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LinkLblNotificationIntro),
				LINK_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		hoverToElementAction(mainaccountcontactmovein.linkLblNotificationIntro);
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LinkLblNotificationIntro),
				LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify fix for ticket BBPRTL-2032
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblPostalNotifHeader, true),
				"pOsTaL", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblPostalNotifHeader),
				NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblEmailNotifHeader, true), "eMaIl",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblEmailNotifHeader),
				NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1LblSMSNotifHeader, true), "sMs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LblSMSNotifHeader),
				NOTIF_HEADER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-2032
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1BillsSMSNotAvail, true), "n/a",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1BillsSMSNotAvail),
				NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getDisplayedText(additionalcontactmovein.addCont1AcctnotifAndRemindersPostalNotAvail, true), "n/a",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1AcctnotifAndRemindersPostalNotAvail),
				NOT_AVAIL_NOTIF_TYPE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsPostal),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComEmail),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1EmailAddress, true),
				"eMaIl aDdReSs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "eMaIl aDdReSs"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1MobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "mObIlE PhOnE NuMbEr"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1BusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "bUsInEsS HoUrS PhOnE NuMbEr"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1AfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "aFtEr hOuRs pHoNe nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1ContactSecretCode, true),
				"sEcReT CoDe, sPeEdS Up vErIfIcAtIoN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(additionalcontactmovein.labelInput, "sEcReT CoDe , sPeEdS Up vErIfIcAtIoN"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineContactSecretCode),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1RemAdditionalContact, true),
				"remove_circle_outline rEmOvE ThIs cOnTaCt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1RemAdditionalContact),
				ADD_REMOVE_CONTACT_BUTTON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(additionalcontactmovein.addCont1IconRemAdditionalContact),
				ADD_ANOTHER_CONTACT_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1AddAnotherContact, true),
				"add_circle aDd aNoThEr cOnTaCt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1AddAnotherContact),
				ADD_REMOVE_CONTACT_BUTTON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(additionalcontactmovein.addCont1IconAddAnotherContact),
				ADD_ANOTHER_CONTACT_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1Previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalcontactmovein.addCont1Previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1Next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalcontactmovein.addCont1Next), NEXT_BUTTON_CSTM,
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
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
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
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintFirstName, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintFirstName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintLastName, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintLastName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmptyBusinessPhone, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmptyBusinessPhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmptyAfterhoursPhone, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmptyAfterhoursPhone),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintContactSecretCode, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintContactSecretCode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "fIrSt nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineFirstName),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "lAsT/FaMiLy nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineLastName),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "dAtE Of bIrTh (DD/MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconDateOfBirth),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineDateOfBirth),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterDriversLicence),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterPassport),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterMedicareCard),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsPostal),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterBillsEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersEmail),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterAcctnotifAndRemindersSMS),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComPostal),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComEmail),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxOuterCss(additionalcontactmovein.addCont1CheckBoxOuterMarketingComSMS),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerBillsEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerAcctnotifAndRemindersSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComPostal),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComEmail),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getRadioCheckboxInnerCss(additionalcontactmovein.addCont1CheckBoxInnerMarketingComSMS),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "eMaIl aDdReSs"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "mObIlE PhOnE NuMbEr"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "bUsInEsS HoUrS PhOnE NuMbEr"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "aFtEr hOuRs pHoNe nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(additionalcontactmovein.labelInput, "sEcReT CoDe , sPeEdS Up vErIfIcAtIoN"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineContactSecretCode),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
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
		waitForScreenToRender();

		String firstName = "Roronoa";
		String lastName = "Zoro";

		clickElementAction(additionalcontactmovein.addCont1FirstName);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblFirstName, true),
				"fIrSt nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblFirstName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineFirstName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1LastName);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblLastName, true),
				"lAsT/FaMiLy nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblLastName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineLastName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertAll();

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
		waitForScreenToRender();

		// verify this would no longer return an error for email address 'email
		// test@testing.com'
		// because the space in between was cleared automatically
		String invalidEmail = "email test@testing.com";
		String validEmail = "emailtest@testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		waitForEmailErrorToChange();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblEmailAddress, true),
				"eMaIl aDdReSs", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblEmailAddress),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address ' @testing.com'
		invalidEmail = " @testing.com";
		String invalidEmailExp = "@testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmailExp), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email~testing.com'
		invalidEmail = "email~testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email`testing.com'
		invalidEmail = "email`testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email!testing.com'
		invalidEmail = "email!testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email#testing.com'
		invalidEmail = "email#testing.com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing,com'
		invalidEmail = "email@testing,com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing/com'
		invalidEmail = "email@testing/com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing<com'
		invalidEmail = "email@testing<com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing>com'
		invalidEmail = "email@testing>com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing?com'
		invalidEmail = "email@testing?com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmail), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();
		// verify it will return an error for email address 'email@testing;com'
		invalidEmail = "email@testing;com";
		// system automatically updated the semi-colon to comma
		// when displaying into the error message
		invalidEmailExp = "email@testing,com";
		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		sendKeysAction(additionalcontactmovein.addCont1EmailAddress, invalidEmail, Keys.TAB);
		waitForEmailErrorToChange();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintEmailAddress, true),
				concatStrings("iNvAlId eMaIl aDdReSs: ", invalidEmailExp), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintEmailAddress), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1EmailAddress);
		deleteAllTextFromField();

		clickElementAction(additionalcontactmovein.addCont1MedicareCard);
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1MedicareCardNumber, true),
				"mEdIcArE CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont1MedicareCardExpiry, true),
				"eXpIrY (MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterDriversLicence),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterPassport),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont1RadioOuterMedicareCard),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont1RadioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "mEdIcArE CaRd nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconMedicareCardExpiry),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1MedicareCardNumber);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblMedicareCardNumber, true),
				"mEdIcArE CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblMedicareCardNumber),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblMedicareCardExpiry, true),
				"eXpIrY (MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblMedicareCardExpiry),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconMedicareCardExpiry),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation for Medicare Card
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "mEdIcArE CaRd nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardNumber, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardExpiry, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the invalid medicare number
		additionalcontactmovein.addCont1MedicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		// click the label header to dismiss the calendar
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		clickElementAction(postaladdressmovein.header);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MedicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardNumber, true),
				"iNvAlId mEdIcArE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardExpiry, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardExpiry), HINT_LABEL_CSTM,
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
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MedicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardNumber, true),
				"iNvAlId mEdIcArE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MedicareCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconMedicareCardExpiry),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintMedicareCardExpiry, true),
				"iNvAlId dAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintMedicareCardExpiry), HINT_LABEL_CSTM,
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
		int month = 2;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, "/", expYrStr);
		// click field to fix the issue in the ElementNotInteractableExpection
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(additionalcontactmovein.addCont1MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);
		this.medicareExpiryAddCont1Month = concatStrings("0", monthStr);
		this.medicareExpiryAddCont1MonthInt = month;
		this.medicareExpiryAddCont1Year = expYrStr;

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
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		String medCareExp = getDisplayedValue(additionalcontactmovein.addCont1MedicareCardExpiry, true);
		softAssertion.assertTrue(medCareExp.contains(medExp), assertionErrorMsg(getLineNumber()));
		this.medicareExpiryAddCont1 = medCareExp;
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the validations for the mobile phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1MobilePhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblMobilePhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1MobilePhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		softAssertion.assertEquals(mobPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblMobilePhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1MobilePhone.sendKeys("  654 000  658 0  6582  ");
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(mobPhone, "+65400065806582", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblMobilePhone, true),
				"mObIlE PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblMobilePhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the validations for the business phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1BusinessPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblBusinessPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1BusinessPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		softAssertion.assertEquals(busPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblBusinessPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1BusinessPhone.sendKeys("  023 952  032 6  35478  ");
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(busPhone, "+023952032635478", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblBusinessPhone, true),
				"bUsInEsS HoUrS PhOnE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblBusinessPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the validations for the after hours phone
		// verify that alpha characters not allowed
		slowSendKeys(additionalcontactmovein.addCont1AfterhoursPhone, "ABCDEFGHIJKLmnopqrstuvwxyz", true, 100);
		String afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		// verify no alpha characters got entered
		softAssertion.assertTrue(StringUtils.isBlank(afterHoursPhone), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify only + is allowed for special characters
		slowSendKeys(additionalcontactmovein.addCont1AfterhoursPhone, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true, 100);
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		softAssertion.assertEquals(afterHoursPhone, "+", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("  102 635  712 8  941250  ");
		afterHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
		// verify users cannot put space
		softAssertion.assertEquals(afterHoursPhone, "+1026357128941250", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone, true),
				"aFtEr hOuRs pHoNe nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblAfterhoursPhone),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		// verify CSS and lang files
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintInvalidMobilePhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintInvalidBusinessPhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintInvalidAfterhoursPhone, true),
				"iNvAlId vAlUe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintInvalidMobilePhone), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintInvalidBusinessPhone),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintInvalidAfterhoursPhone),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1MobilePhone.clear();
		additionalcontactmovein.addCont1BusinessPhone.clear();
		additionalcontactmovein.addCont1AfterhoursPhone.clear();
		// enter valid phone numbers
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0702058654");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("0800987490");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+0123456789123", Keys.TAB);

		// verify CSS
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we cannot go to the additional contact if a required field not yet
		// populated
		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
		pauseSeleniumExecution(1000);
		assertTrue(isElementInError(additionalcontactmovein.addCont1ContactSecretCode, 5, 0),
				"Additional Contact 1 Secret Code not in error state");
		additionalcontactmovein.addCont1ContactSecretCode.sendKeys("Sekrekt's #002", Keys.TAB);

		// verify the displayed color input values
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FirstName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1LastName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MedicareCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MedicareCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1EmailAddress), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1MobilePhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1BusinessPhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1AfterhoursPhone), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1ContactSecretCode), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineFirstName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineLastName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMedicareCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineEmailAddress),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineMobilePhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineBusinessPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineAfterhoursPhone),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineContactSecretCode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the section header
		String actSectionHeader = normalizeSpaces(getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader,
				"Additional Contact (".concat(firstName)).getText());
		String expSectionHeader = concatStrings("4 aDdItIoNaL CoNtAcT (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		waitForScreenToRender();
		// verify we are in the 2nd additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				"We are not yet in the 2nd Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-346
	 * 
	 * - verify fields are not in error state initially - verify the Provide None
	 * option is not displayed - verify SMS checkbox is not displayed for Bills -
	 * verify the Postal checkbox is not displayed for Account Notifications and
	 * Reminders - verify the notification labels - verify the notification settings
	 * that should be ticked by default and which is not - verify hovering in the
	 * icons will display the tooltip message for the notifications - verify hitting
	 * Next button would validate the required fields - verify hitting Previous
	 * section would redirect us into the 1st Additional Contact - verify that in
	 * the 1st Additional Contact we do not see the Add Another Contact button -
	 * verify that in the 1st Additional Contact we see the Remove this contact
	 * button - verify the validations for the Drivers Licence - verify the required
	 * fields for the notifications - verify the action to Add Another contact is
	 * not displayed since limit is reached - verify we see the action to remove
	 * additional contact - verify the header in the section
	 * 
	 */
	@Test(priority = 5, dependsOnMethods = { "verifyAdditionalContact01" })
	public void verifyAdditionalContact02() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		String billsNotifText = getDisplayedText(additionalcontactmovein.addCont2LblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(additionalcontactmovein.addCont2LblAcctnotifAndRemindersNotif,
				true);
		String marketComNotifText = getDisplayedText(additionalcontactmovein.addCont2LblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info bIlLs(additional fees apply for mailing bills)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText,
				"info aCcOuNt nOtIfIcAtIoNs aNd rEmInDeRs (no additional payments)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info mArKeTiNg cOmMuNiCaTiOnS",
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
		String billsTooltipMsg = getDisplayedText(additionalcontactmovein.addCont2BillsNotifTooltipMsg, true);
		softAssertion.assertEquals(billsTooltipMsg, "Bill delivery method", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipIcon);
		String acctNotifAndRemTooltipMsg = getDisplayedText(
				additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipMsg, true);
		softAssertion.assertEquals(acctNotifAndRemTooltipMsg,
				"All account and bill reminders as well as any account issues", assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2MarketingComNotifTooltipIcon);
		String marketComTooltipMsg = getDisplayedText(additionalcontactmovein.addCont2MarketingComNotifTooltipMsg,
				true);
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

		String firstName = "Nico";
		String lastName = "Robin's";
		additionalcontactmovein.addCont2FirstName.sendKeys(firstName);
		String firstNameAct = getDisplayedValue(additionalcontactmovein.addCont2FirstName, true);
		softAssertion.assertEquals(firstNameAct, firstName, assertionErrorMsg(getLineNumber()));
		additionalcontactmovein.addCont2LastName.sendKeys(lastName);
		String lastNameAct = getDisplayedValue(additionalcontactmovein.addCont2LastName, true);
		softAssertion.assertEquals(lastNameAct, lastName, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont2DriversLicence);
		clickElementAction(additionalcontactmovein.addCont2LblNotificationHeader);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont2DriversLicenceNumber, true),
				"aUsTrAlIaN DrIvErS LiCeNcE NuMbEr", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(additionalcontactmovein.addCont2DriversLicenceState, true),
				"sTaTe iSsUeD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont2RadioOuterDriversLicence),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont2RadioInnerDriversLicence),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont2RadioOuterPassport),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont2RadioInnerPassport),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(additionalcontactmovein.addCont2RadioOuterMedicareCard),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(additionalcontactmovein.addCont2RadioInnerMedicareCard),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(additionalcontactmovein.labelInput, "aUsTrAlIaN DrIvErS LiCeNcE NuMbEr"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceNumber),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "sTaTe iSsUeD"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceState),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify that the drivers licence number and state issued are in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.adCont2HintDriversLicenceNumber, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont2HintDriversLicenceState, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.adCont2HintDriversLicenceNumber),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2HintDriversLicenceState),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(additionalcontactmovein.labelInput, "aUsTrAlIaN DrIvErS LiCeNcE NuMbEr"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "sTaTe iSsUeD"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceState),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify special characters not allowed
		slowSendKeys(additionalcontactmovein.addCont2DriversLicenceNumber, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", true,
				200);
		String driversNumber = getDisplayedValue(additionalcontactmovein.addCont2DriversLicenceNumber, true);
		verifyStringIsBlank(driversNumber);

		// verify invalid drivers licence number
		// and state
		additionalcontactmovein.addCont2DriversLicenceNumber.sendKeys("QA");
		clickElementAction(additionalcontactmovein.addCont2LblNotificationHeader);
		// verify that the drivers licence number and state issued are in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.adCont2HintDriversLicenceNumber, true),
				"iNvAlId aUsTrAlIaN DrIvErS LiCeNcE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont2HintDriversLicenceState, true),
				"rEqUiReD FiElD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.adCont2HintDriversLicenceNumber),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2HintDriversLicenceState),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceNumber),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(additionalcontactmovein.labelInput, "sTaTe iSsUeD"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceState),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont2DriversLicenceNumber);
		deleteAllTextFromField();
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS ang lang files
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont2HintDriversLicenceState, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2HintDriversLicenceState),
				HINT_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceState),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// clear the drivers licence number and state
		clickElementAction(additionalcontactmovein.addCont2DriversLicenceNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont2DriversLicenceState);
		deleteAllTextFromField();
		additionalcontactmovein.addCont2DriversLicenceNumber.sendKeys("01235987510");
		additionalcontactmovein.addCont2DriversLicenceState.sendKeys("Australian Capital Territory", Keys.TAB);

		// verify CSS
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2DriversLicenceNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2DriversLicenceState), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont2UnderlineDriversLicenceState),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		String actSectionHeader = normalizeSpaces(getElementFrmMatStepHdrTag(additionalcontactmovein.matStepHeader,
				"Additional Contact (".concat(firstName)).getText());
		String expSectionHeader = concatStrings("5 aDdItIoNaL CoNtAcT (", firstName, " ", lastName, ")");
		softAssertion.assertEquals(actSectionHeader, expSectionHeader, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
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
		waitForScreenToRender();
		// verify we are in the postal address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-352
	 * 
	 * - verify the fields are not in error initially - verify clicking Next button
	 * would validate fields - verify the required fields - verify clicking Previous
	 * would redirect us into the 2nd Additional Contact - verify the allowed values
	 * when entering Country - verify the address would be overridden when user
	 * inputs another address - verify that the addresses displayed includes address
	 * from other countries - verify the header for the section name
	 * 
	 */
	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact02" })
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
		// verify the fix for ticket BBPRTL-646
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(postaladdressmovein.sameSupAddressNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and Lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
				"create aCcOuNt dEtAiLs", "create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)",
				"create aDdItIoNaL CoNtAcT (Roronoa Zoro)", "create aDdItIoNaL CoNtAcT (Nico Robin's)",
				"6 pOsTaL AdDrEsS", "7 cOnCeSsIoN", "8 dIrEcT DeBiT", "9 aDdItIoNaL NoTe", "10 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.lblPostalAddressQuestion, true),
				"pOsTaL AdDrEsS SaMe aS SuPpLy aDdReSs?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.lblSameSupAddressYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.lblSameSupAddressNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.lblPostalAddressQuestion), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.lblSameSupAddressYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.lblSameSupAddressNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(postaladdressmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(postaladdressmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
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
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous
		clickElementAction(postaladdressmovein.previous);
		waitForScreenToRender();
		// verify we are in the 2nd additional contact
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				"We are not yet in the 2nd Additional Contact section");
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
		clickElementAction(postaladdressmovein.lblPostalAddressQuestion);
		// verify field not in error state
		softAssertion.assertFalse(isElementInError(postaladdressmovein.postalAddSearch, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.postalAddManualAddSearch, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(postaladdressmovein.radioOuterSameSupAddressNo),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(postaladdressmovein.radioInnerSameSupAddressNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.postalAddManualAddSearch, true),
				"mAnUaL AdDrEsS SeArCh", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.postalAddSearch, true),
				"pLeAsE StArT TyPiNg pOsTaL AdDrEsS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postalAddManualAddSearch),
				MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(postaladdressmovein.labelInput, "pLeAsE StArT TyPiNg pOsTaL AdDrEsS"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostalAddSearch),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(postaladdressmovein.postalAddSearch, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintPostalAddSearch, true),
				"sElEcT FrOm dRoPdOwN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(postaladdressmovein.labelInput, "pLeAsE StArT TyPiNg pOsTaL AdDrEsS"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostalAddSearch), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintPostalAddSearch), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous
		clickElementAction(postaladdressmovein.previous);
		waitForScreenToRender();
		// verify we are in the 2nd additional contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);

		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(postaladdressmovein.postalAddSearch, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddSearch);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.floaterLblPostalAddSearch, true),
				"pLeAsE StArT TyPiNg pOsTaL AdDrEsS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblPostalAddSearch), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostalAddSearch),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.postalAddCantFindAdd, true),
				"search cAnNoT FiNd aDdReSs? ClIcK HeRe tO CoMpLeTe dEtAiLs mAnUaLlY",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(postaladdressmovein.postalAddCantFindAdd),
				GOOGLELOOKUP_CANNOTFIND_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postalAddCantFindAdd), MAT_OPTION_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		slowSendKeys(postaladdressmovein.postalAddSearch, "132 Mitcham", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(getElementFromList(postaladdressmovein.postalAddSearch, 1)),
				MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndProp(postaladdressmovein.postalAddressesDiv),
				MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddCantFindAdd);
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.postalAddQuickAddSearch, 0),
				assertionErrorMsg(getLineNumber()));
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.addLine01, true), "aDdReSs lInE 1",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.addLine02, true), "aDdReSs lInE 2",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.addLine03, true), "aDdReSs lInE 3",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.addLine04, true), "aDdReSs lInE 4",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.city, true), "cItY/SuBuRb",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.state, true), "sTaTe",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.postcode, true), "pOsTcOdE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.country, true), "cOuNtRy",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 1"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 2"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "cItY/SuBuRb"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "sTaTe"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "pOsTcOdE"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "cOuNtRy"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 1"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 2"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "cItY/SuBuRb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "sTaTe"), PLACEHOLDER_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "pOsTcOdE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "cOuNtRy"), PLACEHOLDER_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintAddLine01, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintAddLine02, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintAddLine03, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintAddLine04, true),
				"aT LeAsT 1 AdDrEsS LiNe rEqUiReD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintCity, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintState, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintPostcode, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintCountry, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintAddLine01), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintAddLine02), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintAddLine03), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintAddLine04), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintCity), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintState), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintPostcode), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintCountry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous even though required fields not populated
		clickElementAction(postaladdressmovein.previous);
		waitForScreenToRender();
		// verify we are in the 2nd additional contact
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				"We are not yet in the 2nd Additional Contact sectio");
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalcontactmovein.addCont2Next)) {
				scrollIntoView(additionalcontactmovein.addCont2Next);
			}
		}
		// hit next
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(postaladdressmovein.addLine01, 0),
				"We are not yet in the Postal Address section");

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
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblAddLine01), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine01), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.addLine02.sendKeys("Add-#02");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblAddLine02), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine02), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.addLine03.sendKeys("Add-#03");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblAddLine03), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine03), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.addLine04.sendKeys("Add-#04");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblAddLine04), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine04), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.city.sendKeys("City/Suburb");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblCity), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.city), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.state.sendKeys("State");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblState), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.state), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.postcode.sendKeys("Postcode");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblPostcode), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postcode), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.country.sendKeys("Dressrosa");
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblCountry), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.country.sendKeys(Keys.TAB);
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.floaterLblCountry), FLOATER_LABEL_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.hintCountry, true), "sElEcT FrOm dRoPdOwN",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.hintCountry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.country);
		deleteAllTextFromField();
		postaladdressmovein.country.sendKeys("australia", Keys.TAB);
		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
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
		List<String> allCountries = getAllMatOptionsValues(postaladdressmovein.countriesDiv);

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

		// let's update the address using the lookup
		softAssertion.assertEquals(getDisplayedText(postaladdressmovein.postalAddQuickAddSearch, true),
				"qUiCk aDdReSs sEaRcH", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postalAddQuickAddSearch),
				MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddQuickAddSearch);
		waitForCssToRender();
		softAssertion.assertEquals(
				getPlaceholderCss(postaladdressmovein.labelInput, "pLeAsE StArT TyPiNg pOsTaL AdDrEsS"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostalAddSearch),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// input the forwarding address
		slowSendKeys(postaladdressmovein.postalAddSearch, "Community 40 Mascar ST", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		// let's confirm first that the records retrieved are not only from Australia
		List<String> addresses2 = null;
		try {
			addresses2 = getAllPostalAddress(postaladdressmovein.postalAddressesDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			postaladdressmovein = new PostalAddressMoveIn(driver);
			addresses2 = getAllPostalAddress(postaladdressmovein.postalAddressesDiv);
		}
		// verify that the addresses are not only from Australia
		verifyStringContainsInAnyStringInList(addresses2, true, allCountries, true);
		chooseAddress(postaladdressmovein.postalAddressesDiv, "40 Mascar Street, Upper Mount Gravatt QLD",
				"40 Mascar Street, Upper Mount Gravatt Queensland");
		pauseSeleniumExecution(1000);

		// verify the fields are populated correctly
		String add01 = getDisplayedValue(postaladdressmovein.addLine01, true);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, true);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, true);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, true);
		String city = getDisplayedValue(postaladdressmovein.city, true);
		String state = getDisplayedValue(postaladdressmovein.state, true);
		String postcode = getDisplayedValue(postaladdressmovein.postcode, true);
		String country = getDisplayedValue(postaladdressmovein.country, true);
		softAssertion.assertEquals(add01, "Community", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "40 Mascar Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Upper Mount Gravatt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4122", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 3"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(postaladdressmovein.labelInput, "aDdReSs lInE 4"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// updated address 3 and 4
		postaladdressmovein.addLine03.sendKeys("Add-#03");
		postaladdressmovein.addLine04.sendKeys("Add-#04");
		clickElementAction(postaladdressmovein.lblPostalAddressQuestion);
		waitForCssToRender();
		// verify the input labels
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine01), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine02), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine03), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.addLine04), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.city), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.state), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postcode), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.country), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine01),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine02),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine03),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineAddLine04),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCity), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineState), UNDERLINE_FOCUSED_OR_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostcode),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlineCountry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(postaladdressmovein.matStepHeader, "Postal Address").getText());
		softAssertion.assertEquals(header, "6 pOsTaL AdDrEsS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddQuickAddSearch);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedPlaceholder(postaladdressmovein.postalAddSearch, true),
				"pLeAsE StArT TyPiNg pOsTaL AdDrEsS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.postalAddManualAddSearch),
				MANUAL_AND_QUICK_ADD_SRCH_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getPlaceholderCss(postaladdressmovein.labelInput, "pLeAsE StArT TyPiNg pOsTaL AdDrEsS"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(postaladdressmovein.iconPostalAddSearch), GLOBE_ICON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(postaladdressmovein.underlinePostalAddSearch),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.postalAddManualAddSearch);
		// verify the address is still there
		add01 = getDisplayedValue(postaladdressmovein.addLine01, true);
		add02 = getDisplayedValue(postaladdressmovein.addLine02, true);
		add03 = getDisplayedValue(postaladdressmovein.addLine03, true);
		add04 = getDisplayedValue(postaladdressmovein.addLine04, true);
		city = getDisplayedValue(postaladdressmovein.city, true);
		state = getDisplayedValue(postaladdressmovein.state, true);
		postcode = getDisplayedValue(postaladdressmovein.postcode, true);
		country = getDisplayedValue(postaladdressmovein.country, true);
		softAssertion.assertEquals(add01, "Community", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "40 Mascar Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add03, "Add-#03", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add04, "Add-#04", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Upper Mount Gravatt", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4122", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
		// verify we are in the Concession section
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not in the Concession section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-353
	 * 
	 * - verify required the fields when hitting Next - verify clicking Previous
	 * would redirect us back to Postal Address - verify the required fields when
	 * chosen Yes - verify the list of concession card types displayed according to
	 * Supply State - verify the validation for the regex for the Type of Concession
	 * Card chosen - verify the validations for the expiry - verify the config for
	 * the number of attachments allowed - verify the header for the section name
	 * 
	 */
	@Test(priority = 7, dependsOnMethods = { "verifyPostalAddress" })
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
		// verify CSS and Lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
				"create aCcOuNt dEtAiLs", "create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)",
				"create aDdItIoNaL CoNtAcT (Roronoa Zoro)", "create aDdItIoNaL CoNtAcT (Nico Robin's)",
				"create pOsTaL AdDrEsS", "7 cOnCeSsIoN", "8 dIrEcT DeBiT", "9 aDdItIoNaL NoTe", "10 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.lblConcessionQuestion, true),
				"aDd cOnCeSsIoN DeTaIlS To tHe aCcOuNt?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.lblAddConcessionYes, true), "yEs",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.lblAddConcessionNo, true), "nO",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.lblConcessionQuestion), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.lblAddConcessionYes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.lblAddConcessionNo), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionYes),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(concessionmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(concessionmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionYes),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionNo),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous button
		clickElementAction(concessionmovein.previous);
		waitForScreenToRender();
		// verify we are in the Postal Address Section
		assertTrue(isElementDisplayed(postaladdressmovein.country, 0), "We are not yet in the Postal Address section");

		// go back to the Concession details
		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();
		clickElementAction(concessionmovein.addConcessionYes);
		clickElementAction(concessionmovein.lblConcessionQuestion);
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
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionYes),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(concessionmovein.radioOuterAddConcessionNo),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionYes),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(concessionmovein.radioInnerAddConcessionNo),
				RADIO_INNER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(concessionmovein.cardHolderName, true), "cArD HoLdEr nAmE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "cArD HoLdEr nAmE"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "tYpE Of cOnCeSsIoN CaRd"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardHolderName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineTypeOfConcessionCard),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.linkDragAndDropClickToBrowse), DRAG_AND_DROP_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(concessionmovein.dragAndDropBorder), UPLOAD_AREA_BORDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintCardHolderName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintTypeOfConcessionCard, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "cArD HoLdEr nAmE"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "tYpE Of cOnCeSsIoN CaRd"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardHolderName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineTypeOfConcessionCard),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintCardHolderName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintTypeOfConcessionCard), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(concessionmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous even though there's required fields
		clickElementAction(concessionmovein.previous);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(postaladdressmovein.country, 0), "We are not yet in the Postal Address section");

		// go back to Concession section
		clickElementAction(concessionmovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(concessionmovein.addConcessionNo, 0), "We are not yet in the Concession section");

		clickElementAction(concessionmovein.addConcessionNo);
		assertTrue(isElementTicked(concessionmovein.addConcessionNo, 0),
				"Add concession details to the account? No option not yet ticked");

		clickElementAction(concessionmovein.addConcessionYes);
		assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
				"Add concession details to the account? Yes option not yet ticked");

		// verify fields no longer in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
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

		clickElementAction(concessionmovein.cardHolderName);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(concessionmovein.floaterLblCardHolderName, true),
				"cArD HoLdEr nAmE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.floaterLblCardHolderName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardHolderName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(getDisplayedText(concessionmovein.floaterLblTypeOfConcessionCard, true),
				"tYpE Of cOnCeSsIoN CaRd", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.floaterLblTypeOfConcessionCard), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true, "Queensland Seniors Card")),
				MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getLabelCss(
						getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true, "DVA Gold Card")),
				MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true,
								"Pensioner Card Centrelink")),
						MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true,
								"Pensioner Card Veteran Affairs")),
						MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true,
								"Centrelink Health Care Card")),
						MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true,
								"Home Parks and Multi Units")),
						MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(
						getLabelCss(getMatListOptionElement(concessionmovein.typeOfConcessionCardOptions, true,
								"Repatriation Heath Card (RHC)")),
						MAT_OPTION_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndProp(concessionmovein.typeOfConcessionCardDiv),
				MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM, assertionErrorMsg(getLineNumber()));
		// not enabled in the concession_card_definition.json
		softAssertion.assertFalse(actualConcessionTypes.toString().contains("Asylum Seeker"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actualConcessionTypes, expectedConcessionTypes, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		softAssertion.assertEquals(typeChosen, "Queensland Seniors Card", assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getDisplayedPlaceholder(concessionmovein.cardNumber, true), "cOnCeSsIoN CaRd nUmBeR",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(concessionmovein.cardNumExpiry, true), "eXpIrY (MM/YYYY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "cOnCeSsIoN CaRd nUmBeR"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "eXpIrY (MM/YYYY)"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(concessionmovein.iconCardNumExpiry), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumber), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumExpiry), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
		// verify the required fields for Card Number and Expiry
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintCardNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintCardNumExpiry, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "cOnCeSsIoN CaRd nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(concessionmovein.labelInput, "eXpIrY (MM/YYYY)"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(concessionmovein.iconCardNumExpiry), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumber), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumExpiry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintCardNumExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(concessionmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.cardNumber);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(concessionmovein.floaterLblCardNumber, true),
				"cOnCeSsIoN CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.floaterLblCardNumber), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		clickElementAction(concessionmovein.cardNumExpiry);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(concessionmovein.floaterLblCardNumExpiry, true), "eXpIrY (MM/YYYY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.floaterLblCardNumExpiry), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation in the concession card number
		// and expiry date
		concessionmovein.cardNumber.sendKeys("+61426037890");
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
		// collapse the calendar
		clickElementAction(concessionmovein.lblAuthorisationForUpload);
		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintCardNumber, true),
				"iNcOrReCt cOnCeSsIoN CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(concessionmovein.hintCardNumExpiry, true), "iNvAlId dAtE",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(concessionmovein.iconCardNumExpiry), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumber), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumExpiry), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.hintCardNumExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUploadAreaBorderCss(concessionmovein.dragAndDropBorder),
				UPLOAD_AREA_BORDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// clear the values for the concession card number and expiry
		clickElementAction(concessionmovein.cardNumber);
		deleteAllTextFromField();
		clearDateField(concessionmovein.cardNumExpiry);

		// put a valid concession card number and expiry
		concessionmovein.cardNumber.sendKeys("378282246310005");
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
		String authorisationTextExp = "'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession. Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our Terms and Conditions";
		softAssertion.assertEquals(getDisplayedText(concessionmovein.lblAuthorisationForUpload, true),
				authorisationTextExp, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.lblAuthorisationForUpload), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.linkLblAuthorisationUpload), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(concessionmovein.linkLblAuthorisationUpload);
		softAssertion.assertEquals(getLabelCss(concessionmovein.linkLblAuthorisationUpload), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify input labels
		softAssertion.assertEquals(getLabelCss(concessionmovein.cardHolderName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.lblColorTypeOfConcessionCard), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.cardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.cardNumExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardHolderName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineTypeOfConcessionCard),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(concessionmovein.iconCardNumExpiry), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(concessionmovein.underlineCardNumExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
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
			int expectedNumOfFiles = 5;
			int counter = 1;
			int maxWaitRetry = 3;
			while (actualSize < expectedNumOfFiles && counter <= maxWaitRetry) {
				logDebugMessage(concatStrings("The expected number of attachments in class '", this.className,
						"' in the Concession is not yet the expected. Will be waiting again, current attempt(s) is <",
						Integer.toString(counter), ">"));
				waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
				// check the number of files uploaded
				actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
				logDebugMessage(concatStrings(this.className, " actualSize for S3 bucket ",
						S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
						"> and attachments ID's is/are -> ", objectIds.toString()));
				counter++;
			}
//			softAssertion.assertEquals(actualSize, expectedNumOfFiles, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}

		String dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		// verify only 2 files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the CSS
		softAssertion.assertEquals(getLabelCss(concessionmovein.dragAndDropText), DRAG_AND_DROP_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.linkDragAndDropClickToBrowse), DRAG_AND_DROP_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf"),
				true), "dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(concessionmovein.dragAndDropUploadedFiles, "Sprin't 02 Story 'Board.pdf")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileName(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileType(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_NAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileStatus(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_STATUS_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(
				getUploadedElementFileSize(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_FILE_SIZE_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedTitle(
				getUploadedElementFileIcon(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg"), true),
				"dElEtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(
				getUploadedElementFileIcon(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg")),
				UPLOADED_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(concessionmovein.matStepHeader, "Concession").getText());
		softAssertion.assertEquals(header, "7 cOnCeSsIoN", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_FOCUSED_CSTM,
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
			if (!isElementWithinViewport(concessionmovein.next)) {
				scrollIntoView(concessionmovein.next);
			}
		}
		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
		// verify we are in the Direct Debit Details section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.lblSetupDirectDebit, 0),
				"We are not in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-247
	 * 
	 * - verify the initial required fields - verify the 3rd option is not displayed
	 * - verify the required fields for Bank Account - verify the required fields
	 * for Credit Card - verify we can hit previous even though required fields not
	 * populated - verify the payment declaration for Bank and Credit Card - verify
	 * the payment authorization for Bank Account and Credit Card - verify the
	 * validations for credit card number and expiry - verify that switching from
	 * Credit Card to Bank Account does not lose data - verify hitting the change
	 * credit card button then hitting cancel does not lose the data
	 * 
	 */
	@Test(priority = 8, dependsOnMethods = { "verifyConcessionDetails" })
	public void verifyDirectDebitDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		portalmovein = new PortalMoveIn(driver);

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
		// verify CSS and lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS",
				"create aCcOuNt dEtAiLs", "create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)",
				"create aDdItIoNaL CoNtAcT (Roronoa Zoro)", "create aDdItIoNaL CoNtAcT (Nico Robin's)",
				"create pOsTaL AdDrEsS", "create cOnCeSsIoN", "8 dIrEcT DeBiT", "9 aDdItIoNaL NoTe", "10 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.lblSetupDirectDebit, true),
				"sEtUp dIrEcT DeBiT (aUtOmAtEd pAyMeNt) On tHe aCcOuNt?", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.lblBankAccount, true), "yEs, A BaNk aCcOuNt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.lblCreditCard, true), "yEs, A CrEdIt cArD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblSetupDirectDebit), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblBankAccount), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblCreditCard), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(directdebitmovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(directdebitmovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous button
		// even though there are required fields not populated
		clickElementAction(directdebitmovein.previous);
		waitForScreenToRender();
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");
		scrollPageDown(500);
		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
		// verify we are in the next section
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");

		// verify fields still in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		clickElementAction(directdebitmovein.lblSetupDirectDebit);
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
		// verify CSS and lang files
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(directdebitmovein.bankAccountName, true),
				"bAnK AcCoUnT NaMe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(directdebitmovein.accountBSB, true), "aCcOuNt bSb",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedPlaceholder(directdebitmovein.accountNumber, true), "aCcOuNt nUmBeR",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "bAnK AcCoUnT NaMe"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt bSb"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt nUmBeR"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineBankAccountName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountBSB), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountNumber),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationBankAccount),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationBankAccount),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "bAnK AcCoUnT NaMe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt bSb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineBankAccountName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountBSB), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountNumber), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintBankAccountName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountBSB, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintBankAccountName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountBSB), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationBankAccount),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationBankAccount),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify CSS
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "bAnK AcCoUnT NaMe"),
				PLACEHOLDER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt bSb"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt nUmBeR"), PLACEHOLDER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineBankAccountName),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountBSB), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountNumber),
				UNDERLINE_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationBankAccount),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationBankAccount),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccountName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountBSB, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.accountNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "bAnK AcCoUnT NaMe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt bSb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineBankAccountName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountBSB), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountNumber), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintBankAccountName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountBSB, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintBankAccountName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountBSB), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationBankAccount),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationBankAccount),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we can hit previous button even though there are required fields not
		// populated
		clickElementAction(directdebitmovein.previous);
		waitForScreenToRender();
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
		waitForScreenToRender();
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
		// verify CSS
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "bAnK AcCoUnT NaMe"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt bSb"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getPlaceholderCss(directdebitmovein.labelInput, "aCcOuNt nUmBeR"),
				PLACEHOLDER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineBankAccountName), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountBSB), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineAccountNumber), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintBankAccountName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountBSB, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintAccountNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintBankAccountName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountBSB), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2035
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintAccountNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationBankAccount),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationBankAccount),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the Bank Account declaration text
		String declaration = getDisplayedText(directdebitmovein.lblBankAccountDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Dishonor Fees: A dishonor fee of $15.00 (inc GST) applied for failed direct debt. the bank payment will be based on the bills issued according to each bill cycle. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblBankAccountDeclaration), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblBankAccountDeclaration), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(directdebitmovein.linkLblBankAccountDeclaration);
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblBankAccountDeclaration), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the Bank Account authorisation text
		String authorisation = getDisplayedText(directdebitmovein.lblBankAccountAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise SR Global Solutions Pty Ltd ACN 132 951 172 (\"Merchant Warrior\"), Direct Debit User ID Number 397351, to debit my/our account on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the Bulk Electronic Clearing System (BECS) as per the service agreement provided. Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblBankAccountAuthorisation), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblBankAccountAuthorisation), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(directdebitmovein.linkLblBankAccountAuthorisation);
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblBankAccountAuthorisation),
				LINK_LABEL_HOVER_CSTM, assertionErrorMsg(getLineNumber()));
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
		// verify CSS and lang files
		assertEquals(loadingMsg, "cReAtInG SeCuRe aReA FoR CrEdIt cArD EnTrY...",
				"Credit Card initialization progress bar text is not correct");
		assertEquals(getLabelCss(directdebitmovein.progressBarText), PROCESSING_REQUEST_MSG_CSTM,
				"The expected Label CSS is not correct");
		assertEquals(getCssStrokeProp(portalmovein.spinner), SPINNER_ICON_CSTM,
				"The expected color for the spinner is not correct");
		String progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG;
		String progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG;
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
		String remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
		softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
				"The initial progress bar color is not correct");
		softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
				"The remaining progress bar color is not correct");
		// verify all assertions
		softAssertion.assertAll();
		moveInDirectDebitCCProgBarLoad();
		// put a pause to make sure all fields and images are loaded
		pauseSeleniumExecution(2000);

		clickElementAction(directdebitmovein.lblSetupDirectDebit);
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterBankAccount),
				RADIO_OUTER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.radioOuterCreditCard),
				RADIO_OUTER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerBankAccount), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.radioInnerCreditCard), RADIO_INNER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify fields not in error state
		switchToMWIframe();
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardName, true),
				"nAmE On cReDiT CaRd", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardNumber, true),
				"cReDiT CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardExpiry, true),
				"cReDiT CaRd eXpIrY (mM/Yy)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2002
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();
		switchToMWIframe();
		// verify the required fields
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardExpiry, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// because of bug ticket BBPRTL-2003, the placeholder color does not change
		// to the error color, so we assert the current behavior for now
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify we are still in the Direct Debit details
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the Credit Card payment declaration text
		declaration = getDisplayedText(directdebitmovein.lblCreditCardDeclaration, true);
		softAssertion.assertEquals(declaration,
				"Payment Sucharge Fees: A credit/debit card surcharge of 1.5% (inc. GST) applies for Visa and Mastercard, 3% surcharge (inc GST) applies for AMEX and Diners Card. Please check our Terms and Conditions the debit payment will be based on the bills issued according to each bill cycle",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblCreditCardDeclaration), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblCreditCardDeclaration), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(directdebitmovein.linkLblCreditCardDeclaration);
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblCreditCardDeclaration), LINK_LABEL_HOVER_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify the Credit Card authorization text
		authorisation = getDisplayedText(directdebitmovein.lblCreditCardAuthorisation, true);
		softAssertion.assertEquals(authorisation,
				"Payment I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our Terms and Conditions",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.lblCreditCardAuthorisation), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblCreditCardAuthorisation), LINK_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(directdebitmovein.linkLblCreditCardAuthorisation);
		softAssertion.assertEquals(getLabelCss(directdebitmovein.linkLblCreditCardAuthorisation), LINK_LABEL_HOVER_CSTM,
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

		// verify we can hit previous button
		// even though there are required fields not populated
		clickElementAction(directdebitmovein.previous);
		waitForScreenToRender();
		// verify we are in the concession card details
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0), "We are not yet in the Concession section");

		clickElementAction(concessionmovein.next);
		waitForScreenToRender();
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardName, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardNumber, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardExpiry, true), "rEqUiReD FiElD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		switchToMWIframe();
		// verify CSS
		clickElementAction(directdebitmovein.creditCardName);
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName),
				FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCardNumber);
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.creditCardExpiry);
		waitForCssToRender();
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// put invalid value for Credit Card Name
		directdebitmovein.creditCardName.sendKeys(getProp("test_data_01"));
		// put an invalid value for Credit Card Number
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_02"), true, 250);
		// put a prev month date
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
		boolean isDoubleDigit = (prevMonth > 9 && prevMonth < 100) || (prevMonth < -9 && prevMonth > -100);
		String prevMonthStr = Integer.toString(prevMonth);
		String curYearStr = Integer.toString(curYear);
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
		waitForScreenToRender();
		// verify fields in error state
		switchToMWIframe();
		// verify the required fields
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardName, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardNumber, 2, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCardExpiry, 2, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		// verify the fix for bug ticket BBPRTL-2036
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardName, true), "nAmE Is iNvAlId",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardNumber, true),
				"cArD NuMbEr iS InVaLiD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.hintCreditCardExpiry, true), "cArD HaS ExPiReD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardName), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardNumber), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.hintCreditCardExpiry), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardName, true),
				"nAmE On cReDiT CaRd", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardNumber, true),
				"cReDiT CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardExpiry, true),
				"cReDiT CaRd eXpIrY (mM/Yy)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName),
				FLOATER_LABEL_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				FLOATER_LABEL_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				FLOATER_LABEL_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardName), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardNumber), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardExpiry), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
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
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 2;
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
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardName, true),
				"nAmE On cReDiT CaRd", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardNumber, true),
				"cReDiT CaRd nUmBeR", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.placeholderCreditCardExpiry, true),
				"cReDiT CaRd eXpIrY (mM/Yy)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName),
				FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardName), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardNumber), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.creditCardExpiry), LABEL_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeFocusedOrError(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		directdebitmovein = new DirectDebitMoveIn(driver, 1);
		try {
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
			loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
			assertEquals(loadingMsg, "Creating secure area for credit card entry...",
					"Credit Card initialization progress bar text is not correct");
		} catch (StaleElementReferenceException sere) {
			logDebugMessage(
					"StaleElementReferenceException encountered while trying to check for the progress bar text");
			directdebitmovein = new DirectDebitMoveIn(driver);
			assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
					"Credit Card initialization progress bar is not displayed");
			loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
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
		
		portalmovein = new PortalMoveIn(driver, 0);
		if (isElementExists(portalmovein.spinnerList)) {
			logDebugMessage("The loading spinner is displayed");
			try {
				softAssertion.assertEquals(getCssStrokeProp(portalmovein.spinner), SPINNER_ICON_CSTM,
						assertionErrorMsg(getLineNumber()));
				// verify all assertions
				softAssertion.assertAll();
			} catch (StaleElementReferenceException sere) {
				logDebugMessage(
						"StaleElementReferenceException encountered in checking the loading spinner, checking if the element still exists");
				// verify first if it's displayed
				portalmovein = new PortalMoveIn(driver, 0);
				if (isElementExists(portalmovein.spinnerList)) {
					logDebugMessage("The loading spinner is still displayed");
					softAssertion.assertEquals(getCssStrokeProp(portalmovein.spinner), SPINNER_ICON_CSTM,
							assertionErrorMsg(getLineNumber()));
					// verify all assertions
					softAssertion.assertAll();
				} else {
					logDebugMessage("The loading spinner is no longer displayed");
				}
			} catch (NoSuchElementException nsee) {
				logDebugMessage("The loading spinner is no longer displayed when we checked for the CSS");
			} finally {
				setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}
		} else {
			logDebugMessage("The loading spinner is not displayed");
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		try {
			if (isElementExists(directdebitmovein.progressBarTextList)) {
				logDebugMessage("Progress bar is still displayed, let's check the CSS");
				progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG;
				progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG;
				js = (JavascriptExecutor) driver;
				initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
				remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
				softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
						"The initial progress bar color is not correct");
				softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
						"The remaining progress bar color is not correct");
				// verify all assertions
				softAssertion.assertAll();
			}
		} catch (JavascriptException jse) {
			logDebugMessage("JavascriptException exception encountered. We might have missed the progress bar");
			assertFalse(isElementExists(directdebitmovein.progressBarTextList),
					"Loading message still displayed however we cannot get the CSS elements for the progress bar");
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);

		// verify in read only mode
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardExpiry, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardNumber),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardExpiry),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.authorisationCreditCard);
		clickElementAction(directdebitmovein.lblSetupDirectDebit);
		// verify CSS
		softAssertion.assertEquals(getRadioCheckboxOuterCss(directdebitmovein.checkBoxOuterAuthorisationCreditCard),
				CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getRadioCheckboxInnerCss(directdebitmovein.checkBoxInnerAuthorisationCreditCard),
				CHECKBOX_INNER_TICKED_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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

		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.changeCreditCardDetails, true),
				"create cHaNgE CrEdIt cArD DeTaIlS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(directdebitmovein.iconChangeCreditCardDetails),
				CHANGE_OR_CANCEL_CC_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.changeCreditCardDetails),
				CHANGE_OR_CANCEL_CC_DETAILS_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// hit the Change Credit Card Details btn
		clickElementAction(directdebitmovein.changeCreditCardDetails);
		assertTrue(isElementDisplayed(directdebitmovein.progressBarText, 1),
				"Credit Card initialization progress bar is not displayed");
		loadingMsg = getDisplayedText(directdebitmovein.progressBarText, true);
		// verify CSS and lang files
		assertEquals(loadingMsg, "cReAtInG SeCuRe aReA FoR CrEdIt cArD EnTrY...",
				"Credit Card initialization progress bar text is not correct");
		assertEquals(getLabelCss(directdebitmovein.progressBarText), PROCESSING_REQUEST_MSG_CSTM,
				"The expected Label CSS is not correct");
		assertEquals(getCssStrokeProp(portalmovein.spinner), SPINNER_ICON_CSTM,
				"The expected color for the spinner is not correct");
		progBarInitialScript = SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG;
		progBarRemainingScript = SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG;
		js = (JavascriptExecutor) driver;
		initialProgBarBackGroundColor = (String) js.executeScript(progBarInitialScript);
		remainingProgBarBackGroundColor = (String) js.executeScript(progBarRemainingScript);
		softAssertion.assertEquals(initialProgBarBackGroundColor, PROGRESS_BAR_INITIAL_CSTM,
				"The initial progress bar color is not correct");
		softAssertion.assertEquals(remainingProgBarBackGroundColor, PROGRESS_BAR_REMAINING_CSTM,
				"The remaining progress bar color is not correct");
		// verify all assertions
		softAssertion.assertAll();
		moveInDirectDebitCCProgBarLoad();

		// verify the fields are blank
		switchToMWIframe();
		String ccName = getDisplayedValue(directdebitmovein.creditCardName, true);
		String ccNum = getDisplayedValue(directdebitmovein.creditCardNumber, true);
		String ccExp = getDisplayedValue(directdebitmovein.creditCardExpiry, true);
		softAssertion.assertTrue(StringUtils.isBlank(ccName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(ccNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(ccExp), assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardName), PLACEHOLDER_MW_FRAME_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardNumber),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.placeholderCreditCardExpiry),
				PLACEHOLDER_MW_FRAME_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardName),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardNumber),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineMWPayframeNotFocused(directdebitmovein.creditCardExpiry),
				UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM, assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify all assertions
		softAssertion.assertAll();

		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(directdebitmovein.cancelCreditCardChange, true),
				"create cAnCeL ChAnGe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssColorProp(directdebitmovein.iconCancelCreditCardChange),
				CHANGE_OR_CANCEL_CC_ICON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.cancelCreditCardChange),
				CHANGE_OR_CANCEL_CC_DETAILS_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's cancel the change
		clickElementAction(directdebitmovein.cancelCreditCardChange);
		pauseSeleniumExecution(1000);
		actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		// verify in read only mode
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementEnabled(directdebitmovein.readOnlyCreditCardExpiry, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardName, "Nick Fury's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_04"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardName), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardNumber),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.floaterLblReadOnlyCreditCardExpiry),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardName), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardNumber), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.readOnlyCreditCardExpiry), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardName),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardNumber),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(directdebitmovein.underlineReadOnlyCreditCardExpiry),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(directdebitmovein.matStepHeader, "Direct Debit").getText());
		softAssertion.assertEquals(header, "8 dIrEcT DeBiT", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();
		// verify we are in the additional notes section
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Notes section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-364
	 * 
	 * - verify not required - verify entering blank spaces should not be sent -
	 * verify next and previous buttons - verify the section header displayed
	 * 
	 */
	@Test(priority = 9, dependsOnMethods = { "verifyDirectDebitDetails" })
	public void verifyAdditionalNote() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify CSS and lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs",
						"create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "create aDdItIoNaL CoNtAcT (Roronoa Zoro)",
						"create aDdItIoNaL CoNtAcT (Nico Robin's)", "create pOsTaL AdDrEsS", "create cOnCeSsIoN",
						"create dIrEcT DeBiT", "9 aDdItIoNaL NoTe", "10 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.lblEnterNotes, true),
				"pLeAsE EnTeR AnY SpEcIaL NoTeS ThAt yOu wIsH To pAsS OnTo oUr cUsToMeR SeRvIcE StAfF",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.notesLengthCounter, true), "0/256",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.lblEnterNotes), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify fix for bug ticket BBPRTL-2037
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.notesLengthCounter), NOTE_LENGTH_COUNTER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalnotemovein.underlineNotesArea), UNDERLINE_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalnotemovein.previous), PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.next, true), "nExT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalnotemovein.next), NEXT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify it's not required so we can hit next
		// and go to the next section
		clickElementAction(additionalnotemovein.next);
		waitForScreenToRender();

		// verify we are in the acceptance page section
		softAssertion.assertEquals(getDisplayedText(acceptancemovein.lblAcceptanceIntro, true),
				"yOu aRe aLmOsT FiNiShEd, ThAnK YoU FoR YoUr pAtIeNcE. pLeAsE ReViEw tHe bElOw dEtAiLs bEfOrE SuBmItTiNg tHe fOrM",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.lblAcceptanceIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1800);
		// hit previous from Acceptance Page
		clickElementAction(acceptancemovein.previous);
		waitForScreenToRender();

		// verify we are in the Additional notes section
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0), "Notes section is not displayed");
		// hit previous from Additional Notes section
		clickElementAction(additionalnotemovein.previous);
		waitForScreenToRender();

		// verify we are in the Direct Debit section
		assertTrue(isElementTicked(directdebitmovein.creditCard, 0), "Credit Card option is no longer ticked");
		// go back to the additional notes section
		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();

		clickElementAction(additionalnotemovein.notesArea);
		softAssertion.assertEquals(getUnderlineCss(additionalnotemovein.underlineNotesArea),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter the additional notes as only white spaces
		additionalnotemovein.notesArea.sendKeys("                    ");
		softAssertion.assertEquals(getDisplayedText(additionalnotemovein.notesLengthCounter, true), "20/256",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(additionalnotemovein.matStepHeader, "Additional Note").getText());
		softAssertion.assertEquals(header, "9 aDdItIoNaL NoTe", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.next);
		waitForScreenToRender();
		// verify we are in the acceptance page now
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * For ticket BBPRTL-366
	 * 
	 * - verify each section in the acceptance section - verify the update link in
	 * the Moving In line - verify that updating the state in the Supply Details
	 * would affect the Concession Card types - verify that after updating the
	 * State, when Acceptance header is clicked we are redirected into the
	 * Concession section - verify that the Type of Concession Card is in error
	 * state and Concession Card Number and Expiry is not displayed - verify that
	 * the original files uploaded files are still there - verify the updated list
	 * for the Type of Concession Card - verify the update link in the Service
	 * Address line - verify the update link in the Life Support line - verify that
	 * deleted attachments in the Life Support are no longer sent into the API -
	 * verify the update link in the Account Details line - verify the update link
	 * in the Main Account Contact line - verify the validations for the Date of
	 * Birth for Main Contact - verify the update link for Main Account Contact
	 * Notification line - verify the update link for the 1st Additional Contact
	 * line - verify the validations for the Date of Birth for 1st Additional
	 * Contact - verify the update link for the 1st Additional Contact Notification
	 * line - verify the update link for the 2nd Additional Contact line - verify
	 * the validations for the Date of Birth for 2nd Additional Contact - verify the
	 * update link for the 2nd Additional Contact Notification line - verify the
	 * update link for the Postal Address line - verify the update link for the
	 * Concession section - verify that deleted attachments in the Concession are no
	 * longer sent into the API - verify the update link for the Direct Debit -
	 * verify the update link for the Additional Notes line - verify acceptance page
	 * each line after the changes - verify that Postal Address is still displayed
	 * even though no Postal notification is chosen since it's required - verify the
	 * required fields when clicking the Submit button - verify the Cancel Message
	 * and clicking Cancel No would not delete the data in the form - verify
	 * clicking Previous button would redirect us back into the Additional Notes
	 * section
	 * 
	 */
	@Test(priority = 10, dependsOnMethods = { "verifyAdditionalNote" })
	public void verifyAcceptanceDetails() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify CSS and lang files
		// verify CSS and lang files
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(
				Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs",
						"create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "create aDdItIoNaL CoNtAcT (Roronoa Zoro)",
						"create aDdItIoNaL CoNtAcT (Nico Robin's)", "create pOsTaL AdDrEsS", "create cOnCeSsIoN",
						"create dIrEcT DeBiT", "create aDdItIoNaL NoTe", "10 aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		List<String> actualSectionMatIconColor = getMatIconHeadersBckgrndColors();
		List<String> expectedSectionMatIconColor = new ArrayList<>(
				Arrays.asList(MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM,
						MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM, MATICON_SECTION_HEADER_FOCUSED_CSTM));
		softAssertion.assertEquals(actualSectionMatIconColor, expectedSectionMatIconColor,
				assertionErrorMsg(getLineNumber()));
		// check CSS for each expected header
		softAssertion.assertEquals(getLabelCss(supplydetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(accountdetailsmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont2Header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(postaladdressmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(directdebitmovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalnotemovein.header), SECTION_HEADER_NOT_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.header), SECTION_HEADER_FOCUSED_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(acceptancemovein.lblAcceptanceIntro, true),
				"yOu aRe aLmOsT FiNiShEd, ThAnK YoU FoR YoUr pAtIeNcE. pLeAsE ReViEw tHe bElOw dEtAiLs bEfOrE SuBmItTiNg tHe fOrM",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.lblAcceptanceIntro), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(getLabelCss(acceptancemovein.movingInRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.movingInRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> movingInRowData = getAllLabelCss(acceptancemovein.movingInRowData);
		softAssertion.assertEquals(movingInRowData.size(), 2, assertionErrorMsg(getLineNumber()));
		for (List<String> label : movingInRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.serviceAddressRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.serviceAddressRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> serviceAddressRowData = getAllLabelCss(acceptancemovein.serviceAddressRowData);
		softAssertion.assertEquals(serviceAddressRowData.size(), 4, assertionErrorMsg(getLineNumber()));
		for (List<String> label : serviceAddressRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.lifeSupportRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.lifeSupportRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> lifeSupportRowData = getAllLabelCss(acceptancemovein.lifeSupportRowData);
		softAssertion.assertEquals(lifeSupportRowData.size(), 3, assertionErrorMsg(getLineNumber()));
		for (List<String> label : lifeSupportRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.accountDetailsRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.accountDetailsRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> accountDetailsRowData = getAllLabelCss(acceptancemovein.accountDetailsRowData);
		softAssertion.assertEquals(accountDetailsRowData.size(), 1, assertionErrorMsg(getLineNumber()));
		for (List<String> label : accountDetailsRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.mainContactRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.mainContactRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> mainContactRowData = getAllLabelCss(acceptancemovein.mainContactRowData);
		softAssertion.assertEquals(mainContactRowData.size(), 7, assertionErrorMsg(getLineNumber()));
		for (List<String> label : mainContactRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.mainContactNotifRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.mainContactNotifRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> mainContactNotifRowData = getAllLabelCss(acceptancemovein.mainContactNotifRowData);
		softAssertion.assertEquals(mainContactNotifRowData.size(), 3, assertionErrorMsg(getLineNumber()));
		for (List<String> label : mainContactNotifRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact1Lbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact1Update), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> addContact1RowData = getAllLabelCss(acceptancemovein.addContact1Data);
		softAssertion.assertEquals(addContact1RowData.size(), 7, assertionErrorMsg(getLineNumber()));
		for (List<String> label : addContact1RowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact1NotifRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact1NotifRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> addContact1NotifRowData = getAllLabelCss(acceptancemovein.addContact1NotifRowData);
		softAssertion.assertEquals(addContact1NotifRowData.size(), 3, assertionErrorMsg(getLineNumber()));
		for (List<String> label : addContact1NotifRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact2Lbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact2Update), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> addContact2RowData = getAllLabelCss(acceptancemovein.addContact2Data);
		softAssertion.assertEquals(addContact2RowData.size(), 7, assertionErrorMsg(getLineNumber()));
		for (List<String> label : addContact2RowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact2NotifRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.addContact2NotifRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> addContact2NotifRowData = getAllLabelCss(acceptancemovein.addContact2NotifRowData);
		softAssertion.assertEquals(addContact2NotifRowData.size(), 3, assertionErrorMsg(getLineNumber()));
		for (List<String> label : addContact2NotifRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.postalAddressRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.postalAddressRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> postalAddressRowData = getAllLabelCss(acceptancemovein.postalAddressRowData);
		softAssertion.assertEquals(postalAddressRowData.size(), 6, assertionErrorMsg(getLineNumber()));
		for (List<String> label : postalAddressRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.concessionRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.concessionRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> concessionRowData = getAllLabelCss(acceptancemovein.concessionRowData);
		softAssertion.assertEquals(concessionRowData.size(), 4, assertionErrorMsg(getLineNumber()));
		for (List<String> label : concessionRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.directDebitRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.directDebitRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> directDebitRowData = getAllLabelCss(acceptancemovein.directDebitRowData);
		softAssertion.assertEquals(directDebitRowData.size(), 3, assertionErrorMsg(getLineNumber()));
		for (List<String> label : directDebitRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getLabelCss(acceptancemovein.additionalNoteRowLbl), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.additionalNoteRowUpdate), UPDATE_LINK_CSTM,
				assertionErrorMsg(getLineNumber()));
		List<List<String>> additionalNoteRowData = getAllLabelCss(acceptancemovein.additionalNoteRowData);
		softAssertion.assertEquals(additionalNoteRowData.size(), 1, assertionErrorMsg(getLineNumber()));
		for (List<String> label : additionalNoteRowData) {
			softAssertion.assertEquals(label, LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		}

		softAssertion.assertEquals(getDisplayedText(acceptancemovein.cancel, true), "cAnCeL",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(acceptancemovein.previous, true), "pReViOuS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(acceptancemovein.submit, true), "sUbMiT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.cancel), CANCEL_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.previous), LAST_PREVIOUS_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.submit), SUBMIT_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));

		// verify each section
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String addContact2lbl = getDisplayedText(acceptancemovein.addContact2Lbl, true);
		String addContact2UpdLink = getDisplayedText(acceptancemovein.addContact2Update, true);
		String addContact2Data = getDisplayedText(acceptancemovein.addContact2Data, true);
		String addContact2Notif = getDisplayedText(acceptancemovein.addContact2NotifRow, true);
		String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("mOvInG In uPdAtE mOvInG In aS tEnAnT ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"sErViCe aDdReSs uPdAtE '001 Complex's Unit 16, 6 Mari Street Alexandra Headland, Queensland, 4572 sErViCe cUrReNtLy cOnNeCtEd",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"lIfE SuPpOrT uPdAtE lIfE SuPpOrT ReQuIrEd uSiNg tHe fOlLoWiNg eQuIpMeNt oXyGeN CoNcEnTrAtOr iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE hAeMoDiAlYsIs mAcHiNe cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT vEnTiLaToR FoR LiFe sUpPoRt \"Other\" Equipment's mEdIcAl cOoLiNg rEqUiReD mEdIcAl cErTiFiCaTe pRoViDeD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "aCcOuNt dEtAiLs uPdAtE rEsIdEnTiAl aCcOuNt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, concatStrings(
				"mAiN AcCoUnT CoNtAcT uPdAtE Monkey Luffy's eMaIl aDdReSs: ", getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: 0212345680 bUsInEsS PhOnE: 0387643210 a/hOuRs pHoNe: 0465320980 pErSoNaL Id: mEdIcArE CaRd (2428 77813 2/1, ",
				this.medicareExpiryMain, ") cOnTaCt sEcReT: (Sekrekt's #001)"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"mAiN AcCoUnT CoNtAcT nOtIfIcAtIoN uPdAtE bIlLs (pOsTaL, eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (pOsTaL, eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "aDdItIoNaL CoNtAcT 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Roronoa Zoro eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: 0702058654 bUsInEsS PhOnE: 0800987490 a/hOuRs pHoNe: +0123456789123 pErSoNaL Id: mEdIcArE CaRd (2428 77813 2, ",
				this.medicareExpiryAddCont1, ") cOnTaCt sEcReT: (Sekrekt's #002)"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"aDdItIoNaL CoNtAcT 1 nOtIfIcAtIoN uPdAtE bIlLs (pOsTaL, eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (pOsTaL, eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2lbl, "aDdItIoNaL CoNtAcT 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Data, concatStrings("Nico Robin's eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: +61432587140 bUsInEsS PhOnE: +61369854220 a/hOuRs pHoNe: +61228987540 pErSoNaL Id: dRiVeR LiCeNcE (01235987510, Australian Capital Territory) cOnTaCt sEcReT: (Sekrekt's #003)"),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2039
		softAssertion.assertEquals(addContact2Notif,
				"aDdItIoNaL CoNtAcT 2 nOtIfIcAtIoN uPdAtE bIlLs (nOnE) nOtIfIcAtIoNs aNd rEmInDeRs (nOnE) mArKeTiNg (pOsTaL, eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"pOsTaL AdDrEsS uPdAtE Community 40 Mascar Street Add-#03 Add-#04 Upper Mount Gravatt, Queensland, 4122 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(concession,
						concatStrings("cOnCeSsIoN uPdAtE Steven Roger's Queensland Seniors Card 378282246310005 (",
								this.concessionExpiry, ") cOnCeSsIoN CaRd uPlOaDeD"),
						assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2038
		softAssertion.assertEquals(directDebit,
				concatStrings("dIrEcT DeBiT uPdAtE cReDiT CaRd nAmE On cArD: Nick Fury's cArD: eNdInG ",
						getProp("test_data_05"), " / eXp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "aDdItIoNaL NoTe uPdAtE nOnE SpEcIfIeD",
				assertionErrorMsg(getLineNumber()));
		// verify Trade Waste not displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify we go to the supply details section
		clickExactLinkNameFromElement(acceptancemovein.movingInRow, "uPdAtE");
		waitForScreenToRender();
		assertTrue(isElementDisplayed(supplydetailsmovein.supplyAddState, 0), assertionErrorMsg(getLineNumber()));
		clickElementAction(supplydetailsmovein.supplyAddState);
		// delete the existing value Queensland
		supplydetailsmovein.supplyAddState.sendKeys(Keys.END, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
				Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
		// update the State as Victoria
		supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
		pauseSeleniumExecution(500);
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				"We are not yet in the Concession details section");

		// verify the fix for ticket BBPRTL-648
		String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, false);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the Type of Concession Card is in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the values that was cleared and was not
		softAssertion.assertEquals(cardHolder, "Steven Roger's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cardType), assertionErrorMsg(getLineNumber()));
		// verify the Concession Card Number and Expiry is not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the original attachment is still displayed
		String dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
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
		List<String> expectedConcessionTypes = new ArrayList<>(Arrays.asList("Pensioner Card Centrelink",
				"Pensioner Card Veteran Affairs", "Health Care Card", "DVA Gold Card"));
		softAssertion.assertEquals(actualConcessionTypes, expectedConcessionTypes, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		verifyTwoStringsAreEqual(typeChosen, "Pensioner Card Centrelink", true);
		// verify the concession card number and expiry is empty
		String concessionCardNum = getDisplayedValue(concessionmovein.cardNumber, true);
		String concessionExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		softAssertion.assertTrue(StringUtils.isBlank(concessionCardNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(concessionExp), assertionErrorMsg(getLineNumber()));
		// verify the original attachment is still displayed
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// put a valid concession card number and expiry
		concessionmovein.cardNumber.sendKeys("00654876400");
		int month = 12;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(monthStr, "/", expYrStr, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForUpload);
		pauseSeleniumExecution(1000);
		concessionExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		this.concessionExpiry = concessionExp;
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		// verify we go to the Supply Details
		clickExactLinkNameFromElement(acceptancemovein.serviceAddressRow, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the supply details
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// update the postcode
		clickElementAction(supplydetailsmovein.supplyAddPostcode);
		deleteAllTextFromField();
		supplydetailsmovein.supplyAddPostcode.sendKeys("90210");
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageUp(450);
		// verify we go to the Supply Details
		clickExactLinkNameFromElement(acceptancemovein.lifeSupportRow, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the supply details
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(550);
		// let's verify the files uploaded
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				"The Supply Details drag and drop uploaded files is not correct");
		// let's remove a file that was uploaded
		deleteUploadedFiles(supplydetailsmovein.dragAndDropUploadedFiles, "typing jim carrey.gif");
		// verify the text displayed
		String containerText = getDisplayedText(supplydetailsmovein.dialogContainerText, true);
		portalmovein = new PortalMoveIn(driver);
		// verify CSS and lang files
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
		waitForScreenToRender();
		// verify the file was removed
		dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
				"The Supply Details drag and drop uploaded files after removing an attachment is not correct");
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		// verify we go to the Account Details section
		clickExactLinkNameFromElement(acceptancemovein.accountDetailsRow, "uPdAtE");
		waitForScreenToRender();
		assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0), assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-1470
		// click commercial and assert that there are no previous values saved
		clickElementAction(accountdetailsmovein.commercial);
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.abnOrAcn, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(accountdetailsmovein.tradingName, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.residential);
		// verify residential is ticked
		assertTrue(isElementTicked(accountdetailsmovein.residential, 0), "Residential radio button is not ticked");
		scrollPageDown(1100);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.mainContactRow)) {
				scrollIntoView(acceptancemovein.mainContactRow);
			}
		}
		// verify we go to the main contact section
		clickExactLinkNameFromElement(acceptancemovein.mainContactRow, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the Main Account Contact section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.dateOfBirth);
		pauseSeleniumExecution(1000);
		clickElementAction(mainaccountcontactmovein.dateOfBirth);
		waitForCssToRender();
		// verify CSS and lang files
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.floaterLblDateOfBirth, true),
				"dAtE Of bIrTh (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblDateOfBirth), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.lblPersonalIDHeader);
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
		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();
		// verify we're still in the main contact details section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblDateOfBirth),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.dateOfBirth), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth), DATEPICKER_ICON_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth), UNDERLINE_ERROR_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.hintDateOfBirth, true),
				"iNvAlId dAtE Of bIrTh, MuSt bE At lEaSt 18 YeArS OlD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.hintDateOfBirth), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the current date of birth value
		String dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		softAssertion.assertEquals(dateOfBirth, expectedDOB, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// initial click to resolve ElementNotInteractableException exception
		clearDateField(mainaccountcontactmovein.dateOfBirth);
		// get the current date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
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
		// verify CSS
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.floaterLblDateOfBirth), FLOATER_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(mainaccountcontactmovein.dateOfBirth), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(mainaccountcontactmovein.iconDateOfBirth), DATEPICKER_ICON_VALID_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(mainaccountcontactmovein.underlineDateOfBirth),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1100);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.mainContactNotifRow)) {
				scrollIntoView(acceptancemovein.mainContactNotifRow);
			}
		}
		// verify we go to the main contact section
		clickExactLinkNameFromElement(acceptancemovein.mainContactNotifRow, "uPdAtE");
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason can't untick using the clickButton
		// so will use javascript instead
		// untick all Postal
		javaScriptClickElementAction(mainaccountcontactmovein.billsPostal);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComPostal);
		// verify that the checkboxes are not ticked
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.billsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.marketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we have a value on the Date of birth
		String actualDob = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		softAssertion.assertEquals(actualDob, dateOfBirth, assertionErrorMsg(getLineNumber()));
		// verify the expected sections
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs",
				"3 mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "create aDdItIoNaL CoNtAcT (Roronoa Zoro)",
				"create aDdItIoNaL CoNtAcT (Nico Robin's)", "create pOsTaL AdDrEsS", "create cOnCeSsIoN",
				"create dIrEcT DeBiT", "create aDdItIoNaL NoTe", "create aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1100);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(acceptancemovein.addContact1Update)) {
				scrollIntoView(acceptancemovein.addContact1Update);
			}
		}
		// verify we go the 1st Additional Contact
		clickExactLinkNameFromElement(acceptancemovein.addContact1Row, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the 1st Additional Contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1DateOfBirth);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1DateOfBirth);
		waitForCssToRender();
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1FloaterLblDateOfBirth, true),
				"dAtE Of bIrTh (DD/MM/YYYY)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblDateOfBirth),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconDateOfBirth),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineDateOfBirth),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1LblPersonalIdentification);
		// verify the validations for Date of Birth
		birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		birthYr = Integer.toString(birthYrRaw);
		// get the current date and add 1 day
		todayPlus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		// let's remove the current year then concatenate birthYr
		invalidBirthDate = getString(todayPlus1, 0, todayPlus1.length() - 4);
		invalidBirthDate = invalidBirthDate + birthYr;
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(additionalcontactmovein.addCont1DateOfBirth);
		pauseSeleniumExecution(1000);
		additionalcontactmovein.addCont1DateOfBirth.sendKeys(invalidBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);
		expectedDOB = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify we're still in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS and lang files
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblDateOfBirth),
				FLOATER_LABEL_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1DateOfBirth), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconDateOfBirth),
				DATEPICKER_ICON_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineDateOfBirth),
				UNDERLINE_ERROR_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(additionalcontactmovein.addCont1HintDateOfBirth, true),
				"iNvAlId dAtE Of bIrTh, MuSt bE At lEaSt 18 YeArS OlD", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1HintDateOfBirth), HINT_LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the current date of birth value
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		softAssertion.assertEquals(dateOfBirth, expectedDOB, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// initial click to resolve ElementNotInteractableException exception
		clearDateField(additionalcontactmovein.addCont1DateOfBirth);
		// get the current date
		String todayminus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -1, DATE_MONTH_YEAR_FORMAT_SLASH);
		validBirthDate = getString(todayminus1, 0, todayminus1.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		additionalcontactmovein.addCont1DateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1ContactSecretCode);
		// verify we have a value on the Date of Birth
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		this.dateOfBirthAddContact1 = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify CSS
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1FloaterLblDateOfBirth),
				FLOATER_LABEL_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.addCont1DateOfBirth), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssFillProp(additionalcontactmovein.addCont1IconDateOfBirth),
				DATEPICKER_ICON_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getUnderlineCss(additionalcontactmovein.addCont1UnderlineDateOfBirth),
				UNDERLINE_FOCUSED_OR_VALID_CSTM, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1000);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(200);
		// verify we go to the 1st Additional Contact
		clickExactLinkNameFromElement(acceptancemovein.addContact1NotifRow, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the 1st Additional Contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason can't untick using the clickButton
		// so will use javascript instead
		// untick all Postal
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsPostal);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComPostal);
		// verify that the checkboxes are not ticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we have a value on the Date of birth
		actualDob = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		softAssertion.assertEquals(actualDob, dateOfBirth, assertionErrorMsg(getLineNumber()));
		// verify the expected sections
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs",
				"create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "4 aDdItIoNaL CoNtAcT (Roronoa Zoro)",
				"create aDdItIoNaL CoNtAcT (Nico Robin's)", "create pOsTaL AdDrEsS", "create cOnCeSsIoN",
				"create dIrEcT DeBiT", "create aDdItIoNaL NoTe", "create aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(900);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(200);
		// verify we go to the 2nd Additional Contact
		clickExactLinkNameFromElement(acceptancemovein.addContact2Row, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the 2nd Additional Contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// get the current date
		String todayminus2 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -2, DATE_MONTH_YEAR_FORMAT_SLASH);
		validBirthDate = getString(todayminus2, 0, todayminus2.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(additionalcontactmovein.addCont2DateOfBirth);
		pauseSeleniumExecution(1000);
		additionalcontactmovein.addCont2DateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont2ContactSecretCode);
		// verify we have a value on the Date of Birth
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont2DateOfBirth, true);
		this.dateOfBirthAddContact2 = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(900);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(700);
		// verify we go to the 2nd Additional Contact
		clickExactLinkNameFromElement(acceptancemovein.addContact2NotifRow, "uPdAtE");
		waitForScreenToRender();
		// verify we are in the 2nd Additional Contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason can't untick using the clickButton
		// so will use javascript instead
		// untick all Postal
		javaScriptClickElementAction(additionalcontactmovein.addCont2MarketingComPostal);
		// verify that the checkboxes are not ticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we have a value on the Date of birth
		actualDob = getDisplayedValue(additionalcontactmovein.addCont2DateOfBirth, true);
		softAssertion.assertEquals(actualDob, dateOfBirth, assertionErrorMsg(getLineNumber()));
		// verify the expected sections
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(Arrays.asList("create sUpPlY DeTaIlS", "create aCcOuNt dEtAiLs",
				"create mAiN AcCoUnT CoNtAcT (Monkey Luffy's)", "create aDdItIoNaL CoNtAcT (Roronoa Zoro)",
				"5 aDdItIoNaL CoNtAcT (Nico Robin's)", "create pOsTaL AdDrEsS", "create cOnCeSsIoN",
				"create dIrEcT DeBiT", "create aDdItIoNaL NoTe", "create aCcEpTaNcE"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(900);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(700);
		// verify we go to the Postal Address
		clickExactLinkNameFromElement(acceptancemovein.postalAddressRow, "uPdAtE");
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.postalAddQuickAddSearch, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's update the address using the lookup
		clickElementAction(postaladdressmovein.postalAddQuickAddSearch);
		// input the forwarding address
		slowSendKeys(postaladdressmovein.postalAddSearch, "Unit 7B/11 Innovation PKWY", true, 300);
		// put a pause to avoid another stale element
		pauseSeleniumExecution(2000);
		chooseAddress(postaladdressmovein.postalAddressesDiv, "unit 7b/11 Innovation Parkway, Birtinya QLD",
				"unit 7b/11 Innovation Parkway, Birtinya Queensland");
		pauseSeleniumExecution(1000);
		// verify the fields are populated correctly
		String add01 = getDisplayedValue(postaladdressmovein.addLine01, true);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, true);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, true);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, true);
		String city = getDisplayedValue(postaladdressmovein.city, true);
		String state = getDisplayedValue(postaladdressmovein.state, true);
		String postcode = getDisplayedValue(postaladdressmovein.postcode, true);
		String country = getDisplayedValue(postaladdressmovein.country, true);
		softAssertion.assertEquals(add01, "Unit 7B", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "11 Innovation Parkway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Birtinya", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4575", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(400);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(1500);
		// verify we go to the Concession section
		clickExactLinkNameFromElement(acceptancemovein.concessionRow, "uPdAtE");
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's verify the concession upload area
		// verify the original attachment are still displayed
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		if (concatStrings(dragAndDropText, " ", concessionUploadArea).contains(
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board.pdf")) {
			assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
					"The Concession drag and drop text and attachments is not correct");
		} else {
			assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
					"The Concession drag and drop text and attachments is not correct");
		}
		scrollPageDown(200);
		// let's delete an uploaded file
		deleteUploadedFiles(concessionmovein.dragAndDropUploadedFiles, "g'alaxy-'wallpaper.jpeg.image/jpeg");
		// verify the text displayed
		containerText = getDisplayedText(concessionmovein.dialogContainerText, true);
		portalmovein = new PortalMoveIn(driver);
		// verify CSS and lang files
		softAssertion.assertEquals(containerText, "aRe yOu sUrE YoU WoUlD LiKe tO ReMoVe tHiS FiLe?",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(concessionmovein.dialogContainer), DIALOG_CONTAINER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(concessionmovein.dialogContainerText), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(concessionmovein.dialogRemoveFileYes), DIALOG_YES_AND_OK_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(concessionmovein.dialogRemoveFileNo), DIALOG_NO_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.overlayBackdrop), OVERLAY_BACKDROP_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.dialogRemoveFileYes);
		pauseSeleniumExecution(1000);
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
				"The Concession drag and drop text and attachments is not correct");
		scrollPageDown(600);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(1500);
		// verify we go to the Direct Debit section
		clickExactLinkNameFromElement(acceptancemovein.directDebitRow, "uPdAtE");
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.creditCard, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(600);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(1500);
		// verify we go to the Additional Contacts section
		clickExactLinkNameFromElement(acceptancemovein.additionalNoteRow, "uPdAtE");
		waitForScreenToRender();
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		additionalnotemovein.notesArea.sendKeys("   The   Quick   Brown   Fox   Jumps   Over  The   Lazy    Dog    ");
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance details section");

		scrollPageDown(1500);
		// verify each line
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		addContact2lbl = getDisplayedText(acceptancemovein.addContact2Lbl, true);
		addContact2UpdLink = getDisplayedText(acceptancemovein.addContact2Update, true);
		addContact2Data = getDisplayedText(acceptancemovein.addContact2Data, true);
		addContact2Notif = getDisplayedText(acceptancemovein.addContact2NotifRow, true);
		postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		concession = getDisplayedText(acceptancemovein.concessionRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("mOvInG In uPdAtE mOvInG In aS tEnAnT ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"sErViCe aDdReSs uPdAtE '001 Complex's Unit 16, 6 Mari Street Alexandra Headland, Victoria, 90210 sErViCe cUrReNtLy cOnNeCtEd",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"lIfE SuPpOrT uPdAtE lIfE SuPpOrT ReQuIrEd uSiNg tHe fOlLoWiNg eQuIpMeNt oXyGeN CoNcEnTrAtOr iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE hAeMoDiAlYsIs mAcHiNe cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT vEnTiLaToR FoR LiFe sUpPoRt \"Other\" Equipment's mEdIcAl cOoLiNg rEqUiReD mEdIcAl cErTiFiCaTe pRoViDeD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "aCcOuNt dEtAiLs uPdAtE rEsIdEnTiAl aCcOuNt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("mAiN AcCoUnT CoNtAcT uPdAtE Monkey Luffy's eMaIl aDdReSs: ",
						getProp("test_dummy_email_lower_case"),
						" mObIlE PhOnE: 0212345680 bUsInEsS PhOnE: 0387643210 a/hOuRs pHoNe: 0465320980 bIrThDaTe: ",
						this.dateOfBirthMain, " pErSoNaL Id: mEdIcArE CaRd (2428 77813 2/1, ", this.medicareExpiryMain,
						") cOnTaCt sEcReT: (Sekrekt's #001)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"mAiN AcCoUnT CoNtAcT nOtIfIcAtIoN uPdAtE bIlLs (eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "aDdItIoNaL CoNtAcT 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Roronoa Zoro eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: 0702058654 bUsInEsS PhOnE: 0800987490 a/hOuRs pHoNe: +0123456789123 bIrThDaTe: ",
				this.dateOfBirthAddContact1, " pErSoNaL Id: mEdIcArE CaRd (2428 77813 2, ", this.medicareExpiryAddCont1,
				") cOnTaCt sEcReT: (Sekrekt's #002)"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"aDdItIoNaL CoNtAcT 1 nOtIfIcAtIoN uPdAtE bIlLs (eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2lbl, "aDdItIoNaL CoNtAcT 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Data, concatStrings("Nico Robin's eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: +61432587140 bUsInEsS PhOnE: +61369854220 a/hOuRs pHoNe: +61228987540 bIrThDaTe: ",
				this.dateOfBirthAddContact2,
				" pErSoNaL Id: dRiVeR LiCeNcE (01235987510, Australian Capital Territory) cOnTaCt sEcReT: (Sekrekt's #003)"),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2039
		softAssertion.assertEquals(addContact2Notif,
				"aDdItIoNaL CoNtAcT 2 nOtIfIcAtIoN uPdAtE bIlLs (nOnE) nOtIfIcAtIoNs aNd rEmInDeRs (nOnE) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"pOsTaL AdDrEsS uPdAtE Unit 7B 11 Innovation Parkway Birtinya, Queensland, 4575 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(concession,
						concatStrings("cOnCeSsIoN uPdAtE Steven Roger's Pensioner Card Centrelink 00654876400 (",
								this.concessionExpiry, ") cOnCeSsIoN CaRd uPlOaDeD"),
						assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2038
		softAssertion.assertEquals(directDebit,
				concatStrings("dIrEcT DeBiT uPdAtE cReDiT CaRd nAmE On cArD: Nick Fury's cArD: eNdInG ",
						getProp("test_data_05"), " / eXp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "aDdItIoNaL NoTe uPdAtE The Quick Brown Fox Jumps Over The Lazy Dog",
				assertionErrorMsg(getLineNumber()));
		// verify Trade Waste not displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(700);
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

		// verify the Cancel button
		clickElementAction(acceptancemovein.cancel);
		pauseSeleniumExecution(1000);
		// verify the Cancel message
		String cancelMsg = getDisplayedText(acceptancemovein.dialogContainerText, true);
		// verify CSS and lang files
		softAssertion.assertEquals(cancelMsg,
				"cAnCeL ReQuEsT AnD ReMoVe dEtAiLs aRe yOu sUrE YoU LiKe yOu lIkE To cAnCeL YoUr sUbMiSsIoN? iF YoU ArE HaViNg aNy iSsUe cOmPlEtInG ThIs fOrM Or hAvE AnY QuEsTiOn, PlEaSe dO NoT HeSiTaTe tO CoNtAcT OuR SuPpOrT TeAm",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(acceptancemovein.dialogContainer), DIALOG_CONTAINER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.dialogContainerHeader), DIALOG_CONTAINER_HEADER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.dialogContainerMsg), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.yesCancelRequest), DIALOG_YES_AND_OK_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.noCancelRequest), DIALOG_NO_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(acceptancemovein.noCancelRequest);

		// verify we can go to the previous section
		clickElementAction(acceptancemovein.previous);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0), "We are not in the Additional Notes section");

		// go back to the acceptance page
		clickElementAction(additionalnotemovein.next);
		waitForScreenToRender();
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
	 * data are not cleared in the form - verify each section using Next button and
	 * confirm the expected values - verify that the acceptance page section is
	 * still the same values - verify the required fields when clicking the Submit
	 * button - tick all 3 checkboxes then submit the request - verify the expected
	 * number of uploaded fields in the S3_PORTAL_PRESIGN_BUCKET_NAME - wait until
	 * the response header shows success - click the OK dialog from the response
	 * header - verify close message displayed - verify that all the keys and values
	 * on the session storage are cleared - verify that the keys and values on the
	 * local storage are not cleared
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
		this.sourceID1 = sessionSourceId;
		logDebugMessage(concatStrings("The value of sourceID1 is '", this.sourceID1, "'"));
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
		// let's confirm the values stored in the session storage are not empty
		sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
		sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		sessionAddNotes = storage.getItemFromSessionStorage("move-in.notes");
		sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		sessionAppId = storage.getItemFromSessionStorage("application_id");
		sessionSourceId = storage.getItemFromSessionStorage("source_id");
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
		this.sourceID2 = sessionSourceId;
		logDebugMessage(concatStrings("The value of sourceID2 is '", this.sourceID2, "'"));
		// let's confirm the values stored in the local storage
		localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
		localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the Supply Details to confirm that the values are still there
		String tenantDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, true);
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, true);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, true);
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenantDate, this.tenantMoveInDate, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(complexName, "'001 Complex's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyNum, "16", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "6", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Mari", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Street", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Alexandra Headland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "90210", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		String otherText = getDisplayedValue(supplydetailsmovein.lifeSuppOtherInput, false);
		softAssertion.assertEquals(otherText, "\"Other\" Equipment's", assertionErrorMsg(getLineNumber()));
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
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd mEdIcAl cErTiFiCaTe aSsOcIaTeD WiTh yOuR LiFe sUpPoRt rEqUiReMeNt Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.next);
		waitForScreenToRender();

		// let's verify the Account Details to confirm that the values are still there
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.next);
		waitForScreenToRender();

		// let's verify the Main Account Contact to confirm that the values are still
		// there
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, true);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, true);
		String dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		String medCareNum = getDisplayedValue(mainaccountcontactmovein.medicareCardNumber, true);
		String medCareExp = getDisplayedValue(mainaccountcontactmovein.medicareCardExpiry, true);
		String emailAdd = getDisplayedValue(mainaccountcontactmovein.emailAddress, true);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, true);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, true);
		String aHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, true);
		String secretCode = getDisplayedValue(mainaccountcontactmovein.contactSecretCode, true);
		softAssertion.assertEquals(firstName, "Monkey", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Luffy's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateOfBirth, this.dateOfBirthMain, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(mainaccountcontactmovein.medicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(medCareNum, "2428778132/1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(medCareExp, this.medicareExpiryMain, assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked or not
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
		softAssertion.assertEquals(emailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0212345680", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "0387643210", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(aHoursPhone, "0465320980", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(secretCode, "Sekrekt's #001", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(mainaccountcontactmovein.next)) {
				scrollIntoView(mainaccountcontactmovein.next);
			}
		}
		clickElementAction(mainaccountcontactmovein.next);
		waitForScreenToRender();

		// let's verify the 1st Additional Contact to confirm that the values are still
		// there
		firstName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, true);
		lastName = getDisplayedValue(additionalcontactmovein.addCont1LastName, true);
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		medCareNum = getDisplayedValue(additionalcontactmovein.addCont1MedicareCardNumber, true);
		medCareExp = getDisplayedValue(additionalcontactmovein.addCont1MedicareCardExpiry, true);
		emailAdd = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, true);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, true);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, true);
		aHoursPhone = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, true);
		secretCode = getDisplayedValue(additionalcontactmovein.addCont1ContactSecretCode, true);
		softAssertion.assertEquals(firstName, "Roronoa", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Zoro", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateOfBirth, this.dateOfBirthAddContact1, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MedicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(medCareNum, "2428778132", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(medCareExp, this.medicareExpiryAddCont1, assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked or not
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
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0702058654", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "0800987490", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(aHoursPhone, "+0123456789123", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(secretCode, "Sekrekt's #002", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalcontactmovein.addCont1Next)) {
				scrollIntoView(additionalcontactmovein.addCont1Next);
			}
		}
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);

		// let's verify the 2nd Additional Contact to confirm that the values are still
		// there
		firstName = getDisplayedValue(additionalcontactmovein.addCont2FirstName, true);
		lastName = getDisplayedValue(additionalcontactmovein.addCont2LastName, true);
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont2DateOfBirth, true);
		String driversLicence = getDisplayedValue(additionalcontactmovein.addCont2DriversLicenceNumber, true);
		String driversLicenceState = getDisplayedValue(additionalcontactmovein.addCont2DriversLicenceState, true);
		emailAdd = getDisplayedValue(additionalcontactmovein.addCont2EmailAddress, true);
		mobPhone = getDisplayedValue(additionalcontactmovein.addCont2MobilePhone, true);
		busPhone = getDisplayedValue(additionalcontactmovein.addCont2BusinessPhone, true);
		aHoursPhone = getDisplayedValue(additionalcontactmovein.addCont2AfterhoursPhone, true);
		secretCode = getDisplayedValue(additionalcontactmovein.addCont2ContactSecretCode, true);
		softAssertion.assertEquals(firstName, "Nico", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Robin's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateOfBirth, this.dateOfBirthAddContact2, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2DriversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(driversLicence, "01235987510", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(driversLicenceState, "Australian Capital Territory",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should be ticked or not
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
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2MarketingComSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(emailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61432587140", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(busPhone, "+61369854220", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(aHoursPhone, "+61228987540", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(secretCode, "Sekrekt's #003", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalcontactmovein.addCont2Next)) {
				scrollIntoView(additionalcontactmovein.addCont2Next);
			}
		}
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);

		// let's verify the Postal Address to confirm that the values are still there
		String add01 = getDisplayedValue(postaladdressmovein.addLine01, true);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, true);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, true);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, true);
		city = getDisplayedValue(postaladdressmovein.city, true);
		state = getDisplayedValue(postaladdressmovein.state, true);
		postcode = getDisplayedValue(postaladdressmovein.postcode, true);
		String country = getDisplayedValue(postaladdressmovein.country, true);
		softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.postalAddQuickAddSearch, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add01, "Unit 7B", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "11 Innovation Parkway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Birtinya", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4575", assertionErrorMsg(getLineNumber()));
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
			if (!isElementWithinViewport(postaladdressmovein.next)) {
				scrollIntoView(postaladdressmovein.next);
			}
		}
		clickElementAction(postaladdressmovein.next);
		waitForScreenToRender();

		// let's verify the Concession to confirm that the values are still there
		String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
		String cardExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		softAssertion.assertEquals(cardHolder, "Steven Roger's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardType, "Pensioner Card Centrelink", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNum, "00654876400", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardExp, this.concessionExpiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload dRaG-AnD-DrOp fIlE HeRe oR cLiCk tO BrOwSe fOr fIlE tO UpLoAd a sCaN Or pIcTuRe oF YoUr cOnCeSsIoN CaRd Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB fIlE UpLoAdEd sUcCeSsFuLlY",
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
			if (!isElementWithinViewport(concessionmovein.next)) {
				scrollIntoView(concessionmovein.next);
			}
		}
		clickElementAction(concessionmovein.next);
		waitForScreenToRender();

		// let's verify the Direct Debit to confirm that the values are still there
		softAssertion.assertTrue(isElementTicked(directdebitmovein.creditCard, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.changeCreditCardDetails, 0),
				assertionErrorMsg(getLineNumber()));
		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Nick Fury's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, "5123XXXXXX2346", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, this.creditCardExpiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		waitForScreenToRender();

		// verify the values in the additional note and confirm it's still there
		String notes = getDisplayedValue(additionalnotemovein.notesArea, false);
		softAssertion.assertEquals(notes, "The   Quick   Brown   Fox   Jumps   Over  The   Lazy    Dog",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.next);
		waitForScreenToRender();

		// verify the values in the acceptance section
		scrollPageDown(2000);
		// verify each line
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSup = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String addContact2lbl = getDisplayedText(acceptancemovein.addContact2Lbl, true);
		String addContact2UpdLink = getDisplayedText(acceptancemovein.addContact2Update, true);
		String addContact2Data = getDisplayedText(acceptancemovein.addContact2Data, true);
		String addContact2Notif = getDisplayedText(acceptancemovein.addContact2NotifRow, true);
		String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("mOvInG In uPdAtE mOvInG In aS tEnAnT ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"sErViCe aDdReSs uPdAtE '001 Complex's Unit 16, 6 Mari Street Alexandra Headland, Victoria, 90210 sErViCe cUrReNtLy cOnNeCtEd",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"lIfE SuPpOrT uPdAtE lIfE SuPpOrT ReQuIrEd uSiNg tHe fOlLoWiNg eQuIpMeNt oXyGeN CoNcEnTrAtOr iNtErMiTtEnT PeRiToNeAl dIaLySiS MaChInE hAeMoDiAlYsIs mAcHiNe cHrOnIc pOsItIvE AiRwAyS PrEsSuRe rEsPiRaToR cRiGlEr nAjJaR SyNdRoMe pHoToThErApY EqUiPmEnT vEnTiLaToR FoR LiFe sUpPoRt \"Other\" Equipment's mEdIcAl cOoLiNg rEqUiReD mEdIcAl cErTiFiCaTe pRoViDeD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "aCcOuNt dEtAiLs uPdAtE rEsIdEnTiAl aCcOuNt",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("mAiN AcCoUnT CoNtAcT uPdAtE Monkey Luffy's eMaIl aDdReSs: ",
						getProp("test_dummy_email_lower_case"),
						" mObIlE PhOnE: 0212345680 bUsInEsS PhOnE: 0387643210 a/hOuRs pHoNe: 0465320980 bIrThDaTe: ",
						this.dateOfBirthMain, " pErSoNaL Id: mEdIcArE CaRd (2428778132/1, ", this.medicareExpiryMain,
						") cOnTaCt sEcReT: (Sekrekt's #001)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"mAiN AcCoUnT CoNtAcT nOtIfIcAtIoN uPdAtE bIlLs (eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "aDdItIoNaL CoNtAcT 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, concatStrings("Roronoa Zoro eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: 0702058654 bUsInEsS PhOnE: 0800987490 a/hOuRs pHoNe: +0123456789123 bIrThDaTe: ",
				this.dateOfBirthAddContact1, " pErSoNaL Id: mEdIcArE CaRd (2428778132, ", this.medicareExpiryAddCont1,
				") cOnTaCt sEcReT: (Sekrekt's #002)"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"aDdItIoNaL CoNtAcT 1 nOtIfIcAtIoN uPdAtE bIlLs (eMaIl) nOtIfIcAtIoNs aNd rEmInDeRs (eMaIl, sMs) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2lbl, "aDdItIoNaL CoNtAcT 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2UpdLink, "uPdAtE", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Data, concatStrings("Nico Robin's eMaIl aDdReSs: ",
				getProp("test_dummy_email_lower_case"),
				" mObIlE PhOnE: +61432587140 bUsInEsS PhOnE: +61369854220 a/hOuRs pHoNe: +61228987540 bIrThDaTe: ",
				this.dateOfBirthAddContact2,
				" pErSoNaL Id: dRiVeR LiCeNcE (01235987510, Australian Capital Territory) cOnTaCt sEcReT: (Sekrekt's #003)"),
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2039
		softAssertion.assertEquals(addContact2Notif,
				"aDdItIoNaL CoNtAcT 2 nOtIfIcAtIoN uPdAtE bIlLs (nOnE) nOtIfIcAtIoNs aNd rEmInDeRs (nOnE) mArKeTiNg (eMaIl, sMs)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"pOsTaL AdDrEsS uPdAtE Unit 7B 11 Innovation Parkway Birtinya, Queensland, 4575 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(concession,
						concatStrings("cOnCeSsIoN uPdAtE Steven Roger's Pensioner Card Centrelink 00654876400 (",
								this.concessionExpiry, ") cOnCeSsIoN CaRd uPlOaDeD"),
						assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBPRTL-2038
		softAssertion.assertEquals(directDebit,
				concatStrings("dIrEcT DeBiT uPdAtE cReDiT CaRd nAmE On cArD: Nick Fury's cArD: eNdInG ",
						getProp("test_data_05"), " / eXp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "aDdItIoNaL NoTe uPdAtE The Quick Brown Fox Jumps Over The Lazy Dog",
				assertionErrorMsg(getLineNumber()));
		// verify Trade Waste not displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 5, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// verify the required fields for the checkboxes
		clickElementAction(acceptancemovein.submit);
		// verify the tickboxes in error state
		softAssertion.assertTrue(isElementInError(acceptancemovein.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(acceptancemovein.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify the source ID is different now
		softAssertion.assertNotEquals(sourceID1, sourceID2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// tick all 3 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		javaScriptClickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("ResiNewContact01_tenantMoveInDate", this.tenantMoveInDate);
		addProp("ResiNewContact01_tenantMoveInDateCRM", this.tenantMoveInDateCRM);
		addProp("ResiNewContact01_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiNewContact01_medicareExpiryMainMonth", this.medicareExpiryMainMonth);
		addProp("ResiNewContact01_medicareExpiryMainYear", this.medicareExpiryMainYear);
		addProp("ResiNewContact01_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiNewContact01_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiNewContact01_dateOfBirthAddContact1", this.dateOfBirthAddContact1);
		addProp("ResiNewContact01_medicareExpiryAddCont1Month", this.medicareExpiryAddCont1Month);
		addProp("ResiNewContact01_medicareExpiryAddCont1Year", this.medicareExpiryAddCont1Year);
		addProp("ResiNewContact01_dateOfBirthAddContact2", this.dateOfBirthAddContact2);
		addProp("ResiNewContact01_medicareExpiryMainMonthInt", Integer.toString(this.medicareExpiryMainMonthInt));
		addProp("ResiNewContact01_medicareExpiryMainYear", this.medicareExpiryMainYear);
		addProp("ResiNewContact01_medicareExpiryAddCont1MonthInt",
				Integer.toString(this.medicareExpiryAddCont1MonthInt));
		addProp("ResiNewContact01_medicareExpiryAddCont1Year", this.medicareExpiryAddCont1Year);
		addProp("ResiNewContact01_sourceID", this.sourceID2);
		addProp("ResiNewContact01_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiNewContact01_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

		// submit the request
		clickElementAction(acceptancemovein.submit);
		// did this because there was an issue where initial click did not work
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
		// verify CSS and lang files
		softAssertion.assertEquals(respHeader, "yOuR ApPlIcAtIoN HaS BeEn a sUcCeSs!",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(acceptancemovein.okDialog, true), "oK",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(acceptancemovein.dialogContainer), DIALOG_CONTAINER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.dialogContainerHeader), DIALOG_CONTAINER_HEADER_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.dialogContainerMsg), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(acceptancemovein.okDialog), DIALOG_YES_AND_OK_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.okDialog);
		pauseSeleniumExecution(1000);
		String closeMsg = getDisplayedText(acceptancemovein.closeMessage, true);
		// verify CSS and lang files
		softAssertion.assertEquals(closeMsg,
				"tHiS WiNdOw/tAb iS No lOnGeR ReQuIrEd, FoR PrIvAcY ReAsOnS We eNcOuRaGe yOu tO ClOsE It",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(acceptancemovein.closeMessage), LABEL_CSTM,
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