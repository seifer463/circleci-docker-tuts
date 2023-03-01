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
import org.openqa.selenium.NoSuchElementException;
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
import au.com.blueoak.portal.pageObjects.move_in.ConcessionMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact02 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
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

	/*** 
	 * 
	 * */
	String initialDate3rdPartyPref;

	/** 
	 * 
	 * */
	String moveInDateUrlPrefill;

	/**
	 * This is the source ID of the portal session
	 *
	 */
	String sourceID;

	/** 
	 * 
	 * */
	String propManMoveInDate;

	/** 
	 * 
	 * */
	String propManSettlementDate;

	/** 
	 * 
	 * */
	String dateOfBirthMain;

	/** 
	 * 
	 * */
	String dateOfBirthAddContact1;

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
	String ownerSettlementDate;

	/** 
	 * 
	 * */
	String ownerSettlementDateCRM;

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
		super.initChromeDriver("en-au");

		s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "09\\", "custom_en.json");
		uploadMoveInCustomLangFile(s3Access, "09\\", "custom_en-au.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "06\\", "portal_config.json");
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "06\\", "portal_config.json");
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(false, false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "06\\", "portal_config.json");
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("7", "Albion", StreetTypesEnum.st, "Box Hill", AustralianStatesEnum.WRONG_VALUE,
					"3128", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "30\\", "elec_portal_config.json");
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=elec_portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.aP.name(), "&tenancy_number=5 5", "&tenancy_street_number=7",
					"&tenancy_street_name=Albion", "&tenancy_street_type=", StreetTypesEnum.STs.getLabelText(),
					"&tenancy_suburb=Box Hill", "&tenancy_postcode=3128", "&tenancy_state=",
					AustralianStatesEnum.WRONG_VALUE.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.getLabelText(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Rashideh", "&contact_last_name=Maroun",
					"&mobile_number=3892 11", "&business_hour_phone=0511 234 567", "&after_hour_phone=61 4 38921111",
					"&email_address=", "email~testing.com",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "30\\", "elec_portal_config.json");
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -10,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=elec_portal_config.json",
					"&account_category=", AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate,
					"&tenancy_type=", TenancyTypesEnum.aP.name(), "&tenancy_number=5 5", "&tenancy_street_number=7",
					"&tenancy_street_name=Albion", "&tenancy_street_type=", StreetTypesEnum.STs.getLabelText(),
					"&tenancy_suburb=Box Hill", "&tenancy_postcode=3128", "&tenancy_state=",
					AustralianStatesEnum.WRONG_VALUE.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.getLabelText(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=Rashideh", "&contact_last_name=Maroun",
					"&mobile_number=3892 11", "&business_hour_phone=0511 234 567", "&after_hour_phone=61 4 38921111",
					"&email_address=", "email~testing.com",
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
		concessionmovein = new ConcessionMoveIn(driver);
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

		// here we set the browser language as English Australia
		// and have upload custom language files for both English and English Australia
		// then we verify that it picked up the custom language file for English
		// Australia
		// verify the fix for bug ticket BBPRTL-2049
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "I AM A:",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "TENANT",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "OWNER",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
				"PROPERTY MANAGER OR LETTING AGENT", assertionErrorMsg(getLineNumber()));
		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// we verify that it picked up the global language file
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddComplexName, true),
					"Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetNum, true),
					"Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetName, true),
					"Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetType, true),
					"Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddCity, true), "City/Suburb",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddState, true), "State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddPostcode, true), "Postcode",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			// verify that it's also in error state
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
			// verify not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Additional Contact", "5 Concession",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify the supplied address
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
			softAssertion.assertEquals(stNum, "7", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Albion", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "street", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Box Hill", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Wrong State", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3128", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
			// verify the fix for bug ticket BBPRTL-1988
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact (Rashideh Maroun)",
							"4 Additional Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify the supplied address
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
			softAssertion.assertEquals(tenancyType, "aP", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "5 5", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "7", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Albion", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Streets", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Box Hill", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Wrong State", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3128", assertionErrorMsg(getLineNumber()));
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
		// verify that the life support introduction is not displayed
		String lifeSupIntro = getDisplayedText(supplydetailsmovein.lblLifeSupIntro, false);
		softAssertion.assertTrue(StringUtils.isBlank(lifeSupIntro), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify not ticked
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.owner);
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact",
							"4 Additional Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Additional Contact", "5 Concession",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact (Rashideh Maroun)",
							"4 Additional Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(supplydetailsmovein.tenant);
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact",
							"4 Concession", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact (Rashideh Maroun)", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(supplydetailsmovein.propManager);
		pauseSeleniumExecution(500);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact",
							"4 Additional Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Additional Contact", "5 Concession",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify expected displayed section
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact (Rashideh Maroun)",
							"4 Additional Contact", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify not editable
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
			// verify not in error state
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
			// verify all assertions
			softAssertion.assertAll();

			scrollPageDown(200);
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
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
			// verify fields are now editable
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
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDatePropMan);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		String future12Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 12, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future14Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 14, DATE_MONTH_YEAR_FORMAT_SLASH);
		this.propManMoveInDate = future12Days;
		this.propManSettlementDate = future14Days;
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(future12Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.propManSettleNo);

		clickElementAction(supplydetailsmovein.propManSettleNo);
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDatePropMan.sendKeys(future14Days, Keys.TAB);
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);

		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.whoIsResponsiblePropMan);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Villa", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("5 5");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("7");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Albion");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Box Hill");
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("3128");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify not editable
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
			// verify not in error state
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
			// verify all assertions
			softAssertion.assertAll();

			scrollPageDown(200);
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify fields in error state
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
			// verify fields are now editable
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
			// verify all assertions
			softAssertion.assertAll();
		} // no need for else block here since there's nothing to do for urlPrefill

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
			softAssertion.assertEquals(tenancyType, "Villa", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "5 5", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Villa", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("5 5");
			// update the correct street type
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
			// update the correct state
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Villa", Keys.TAB);
			// update the correct street type
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
			// update the correct state
			clickElementAction(supplydetailsmovein.supplyAddState);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "7", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Albion", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Box Hill", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3128", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// update the state for now
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
		softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddState, false), "Queensland",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify not ticked
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
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the Supply Details section
		assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), "We are no longer in the Supply Details section");

		// verify fields in error state
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
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"));
		// let's click the life support again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// verify no attachment
		String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
		String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
		// verify all files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// upload life support files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "planet_in_deep_space-wallpaper-1920x1080.jpg");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement planet_in_deep_space-wallpaper-1920x1080 .jpg 0.8 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
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

			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
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
			// verify not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify now editable
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.residential);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Contact Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 3, dependsOnMethods = { "verifyAccountDetails" })
	public void verifyMainContact(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify the text defined
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblResponsibleForPaying, true),
				"Please provide the Property Manager contact who will be responsible for ensuring that the account is paid when it becomes due.",
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
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Rashideh",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					"email~testing.com", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "389211",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "0511234567",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false),
					"61438921111", assertionErrorMsg(getLineNumber()));
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
		}

		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
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
			// verify all assertions
			softAssertion.assertAll();

			mainaccountcontactmovein.firstName.sendKeys("Rashideh");
			mainaccountcontactmovein.lastName.sendKeys("Maroun");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.emailAddress, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.emailAddress);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.mobilePhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.businessPhone);
			deleteAllTextFromField();
			clickElementAction(mainaccountcontactmovein.afterhoursPhone);
			deleteAllTextFromField();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contacts section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify radio buttons not ticked by default
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1DriversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1Passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MedicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1ProvideNone, 0),
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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1ProvideNone, 5, 0),
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
		// verify add another contact not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		// verify the remove additional contact link not displayed
		softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
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
		// verify fields in error state
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1ProvideNone, 5, 0),
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

		String firstNameAddCont1 = "Iain";
		String lastNameAddCont1 = "Stuart";
		additionalcontactmovein.addCont1FirstName.sendKeys(firstNameAddCont1);
		additionalcontactmovein.addCont1LastName.sendKeys(lastNameAddCont1);

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
		clickElementAction(additionalcontactmovein.addCont1AfterhoursPhone);
		String expectedDOB = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we're still in the additional contact section
		String firstName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, true);
		// verify the current date of birth value
		String dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		softAssertion.assertEquals(firstName, firstNameAddCont1, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateOfBirth, expectedDOB, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// initial click to resolve ElementNotInteractableException exception
		clearDateField(additionalcontactmovein.addCont1DateOfBirth);
		// get the current date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		additionalcontactmovein.addCont1DateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1AfterhoursPhone);
		// verify we have a value on the Date of Birth
		dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont1DateOfBirth, true);
		this.dateOfBirthAddContact1 = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the required fields in the Drivers License
		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
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
			if (!isElementWithinViewport(concessionmovein.header)) {
				scrollIntoView(concessionmovein.header);
			}
		}
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		clickElementAction(additionalcontactmovein.addCont1Previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Main Contact section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contacts section");

		clickElementAction(additionalcontactmovein.addCont1MedicareCard);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		clickElementAction(additionalcontactmovein.addCont1Previous);
		pauseSeleniumExecution(1000);
		// verify we are in the Main Contact section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contacts section");

		clickElementAction(additionalcontactmovein.addCont1Passport);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the allowed characters for passport number
		additionalcontactmovein.addCont1PassportNumber.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		// verify in lower case country will be in error
		additionalcontactmovein.addCont1PassportCountry.sendKeys("australia", Keys.TAB);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1PassportNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont1PassportCountry);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1PassportNumber.sendKeys("0138687zZ0625");
		additionalcontactmovein.addCont1PassportCountry.sendKeys("United States Minor Outlying Islands", Keys.TAB);

		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1PassportNumber, true),
				"0138687zZ0", assertionErrorMsg(getLineNumber()));
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+61425228522");

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyAdditionalContact" })
	public void verifyConcessionDetails(@Optional("true") boolean switchToIframe) {

		// let's switch to the Move-In Iframe
		if (switchToIframe) {
			embeddedMoveInSwitchFrame(1);
		}

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

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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

		concessionmovein.cardHolderName.sendKeys("Steven Roger's Jr.");
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

		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		assertEquals(typeChosen, "Queensland Seniors Card", "The chosen Card Type is not correct");

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields for Card Number and Expiry
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.cardNumExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// put a valid concession card number and expiry
		concessionmovein.cardNumber.sendKeys(getProp("test_data_14"));
		int month = 4;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(monthStr, "/", expYrStr, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForUpload);

		scrollPageDown(200);
		// let's upload concession card details
		uploadConcessionFiles(ARTIFACTS_DIR, "planet_in_deep_space-wallpaper-1920x1080.jpg", "g'alaxy-'wallpaper.jpeg",
				"typing jim carrey.gif");
		// wait for the files to display in the upload area
		// and also in the S3 bucket
		waitForFilesToBeUploaded(90000);
		// check if the file(s) is/are already uploaded in the S3 bucket
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings(this.className, " actualSize in the S3 bucket for Concession ",
					S3_PORTAL_PRESIGN_BUCKET_NAME, " is <", Integer.toString(actualSize),
					"> and attachments ID's is/are -> ", objectIds.toString()));
			int expectedNumOfFiles = 16;
			int counter = 1;
			int maxWaitRetry = 3;
			while (actualSize < expectedNumOfFiles && counter <= maxWaitRetry) {
				// hover mouse to ensure there's activity
				hoverToElementAction(concessionmovein.dragAndDropArea);
				// add another wait time because the file typing jim carrey.gif
				// cannot be previewed/corrupted in the CRM
				waitForFilesToBeUploaded(PORTAL_FILE_UPLOAD_WAIT_TIMEOUT);
				// check if the file(s) is/are already uploaded in the S3 bucket
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
		// verify only 3 files were uploaded
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card planet_in_deep_space-wallpaper-1920x1080 .jpg 0.8 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the header
		String header = normalizeSpaces(
				getElementFrmMatStepHdrTag(concessionmovein.matStepHeader, "Concession").getText());
		softAssertion.assertEquals(header, "5 Concession", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageUp(800);
		clickElementAction(supplydetailsmovein.header);
		pauseSeleniumExecution(1000);
		// let's update the state from Supply Details
		clickElementAction(supplydetailsmovein.supplyAddState);
		deleteAllTextFromField();
		supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
		scrollPageDown(800);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);

		// verify that the values that should be retained and what is not
		dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
		concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
		softAssertion.assertEquals(getDisplayedValue(concessionmovein.cardHolderName, true), "Steven Roger's Jr.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify values got cleared
		typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, false);
		softAssertion.assertTrue(StringUtils.isBlank(typeChosen), assertionErrorMsg(getLineNumber()));
		// verify the Concession Card Number and Expiry is not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertFalse(isElementInError(concessionmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card planet_in_deep_space-wallpaper-1920x1080 .jpg 0.8 MB File uploaded successfully g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully typing jim carrey .gif 0.5 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we cannot go to the next section
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				"We are no longer in the Concession section");

		// verify we cannot go to the next section
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				"We are no longer in the Concession section");

		// verify the updated list
		// verify the list of types available
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		// let's confirm first that the records retrieved are correct
		actualConcessionTypes = null;
		try {
			actualConcessionTypes = getAllMatOptionsValues(concessionmovein.typeOfConcessionCardDiv);
		} catch (StaleElementReferenceException sere) {
			// let's initialize the page objects because we get a stale element
			concessionmovein = new ConcessionMoveIn(driver);
			actualConcessionTypes = getAllMatOptionsValues(concessionmovein.typeOfConcessionCardDiv);
		}
		expectedConcessionTypes = new ArrayList<>(Arrays.asList("Pensioner Card Centrelink",
				"Pensioner Card Veteran Affairs", "Health Care Card", "DVA Gold Card"));
		softAssertion.assertEquals(actualConcessionTypes, expectedConcessionTypes, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 3);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		assertEquals(typeChosen, "Health Care Card", "The chosen Card Type is not correct");

		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertTrue(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		concessionmovein.cardNumber.sendKeys("01321687450");

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyConcessionDetails" })
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
		// verify all assertions
		softAssertion.assertAll();

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
		moveInDirectDebitCCProgBarLoad();

		// enter valid fields for credit card name, credit card number and expiry
		switchToMWIframe();
		directdebitmovein.creditCardName.sendKeys("Anthony Stark's");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_14"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 8;
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
		// verify field not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
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
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationBankAccount, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		moveInDirectDebitCCProgBarLoad();

		// verify the details are empty
		switchToMWIframe();
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardName, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardNumber, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardExpiry, false)),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		// verify not in error state
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify not ticked
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
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
		directdebitmovein.creditCardName.sendKeys("Anthony Stark's II");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_14"), true, 300);
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		month = 9;
		monthStr = Integer.toString(month);
		expYrStrFull = Integer.toString(expYr);
		expYrStr = getString(expYrStrFull, 2, 4);
		monthStr = concatStrings("0", monthStr);
		expiry = concatStrings(monthStr, "/", expYrStr);
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
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
		moveInDirectDebitCCProgBarLoad();
		waitForScreenToRender();
		// verify we are still in the direct debit section
		assertTrue(isElementDisplayed(directdebitmovein.creditCard, 0), "We are no longer in the Direct Debit section");

		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Anthony Stark's II", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_15"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.authorisationCreditCard);
		softAssertion.assertTrue(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
		waitForScreenToRender();
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		clickElementAction(directdebitmovein.noDirectDebit);
		pauseSeleniumExecution(1000);

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
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate, " (Settlement on ", this.propManSettlementDate,
						") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd, "Service Address update Villa 5 5, 7 Albion Road Box Hill, Victoria, 3128",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Haemodialysis Machine Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Property Manager Account Contact update Rashideh Maroun",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Property Manager Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Iain Stuart Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61425228522 Birthdate: ", this.dateOfBirthAddContact1,
						" Personal Id: Passport (0138687zZ0, United States Minor Outlying Islands)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Steven Roger's Jr. Health Care Card 01321687450 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
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

		// verify we go to the supply details section
		clickExactLinkNameFromElement(acceptancemovein.serviceAddressRow, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.owner);
		// verify the values
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.settlementDateOwner, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.whoIsResponsiblePropMan, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
		// verify the required fields
		scrollPageDown(500);
		clickElementAction(accountdetailsmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.settlementDateOwner, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		String future11Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 11, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future11D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 11, DATE_MONTH_YEAR_FORMAT_DASH);
		this.ownerMoveInDate = future11Days;
		this.ownerMoveInDateCRM = future11D;
		String past2Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -2, DATE_MONTH_YEAR_FORMAT_SLASH);
		String past2D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -2, DATE_MONTH_YEAR_FORMAT_DASH);
		this.ownerSettlementDate = past2Days;
		this.ownerSettlementDateCRM = past2D;
		clickElementAction(supplydetailsmovein.moveInDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateOwner.sendKeys(future11Days, Keys.TAB);
		// just click an already ticked radio to collapse the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.settlementDateOwner);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDateOwner.sendKeys(past2Days, Keys.TAB);
		// just click an already ticked radio to collapse the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		scrollPageDown(1500);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are redirected immediately into the Main Account Contact section
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				"We are not yet in the Main Account Contact section");
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, true);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, true);
		softAssertion.assertEquals(firstName, "Rashideh", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Maroun", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify Provide None option not displayed in the Personal Identification
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
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
		clickElementAction(mainaccountcontactmovein.afterhoursPhone);
		String expectedDOB = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify the fix for ticket BBPRTL-667
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify we're still in the main contact details section
		firstName = getDisplayedValue(mainaccountcontactmovein.firstName, true);
		lastName = getDisplayedValue(mainaccountcontactmovein.lastName, true);
		softAssertion.assertEquals(firstName, "Rashideh", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Maroun", assertionErrorMsg(getLineNumber()));
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
		clickElementAction(mainaccountcontactmovein.afterhoursPhone);
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
		mainaccountcontactmovein.passportNumber.sendKeys("aDv123");
		mainaccountcontactmovein.passportCountry.sendKeys("Philippines", Keys.TAB);
		scrollPageDown(1500);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(400);
		clickExactLinkNameFromElement(acceptancemovein.addContact1Row, "update");
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		scrollPageDown(500);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		scrollPageDown(600);
		clickExactLinkNameFromElement(acceptancemovein.directDebitRow, "update");
		pauseSeleniumExecution(1000);
		// check the previously entered values are not displayed
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
		moveInDirectDebitCCProgBarLoad();
		// verify the details are empty
		switchToMWIframe();
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardName, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardNumber, false)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(directdebitmovein.creditCardExpiry, false)),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone")) {
			switchToDefaultContent();
		} else if (getPortalType().equals("embedded")) {
			// let's go back to the parent iframe
			switchToParentFrame();
		}
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(directdebitmovein.authorisationCreditCard, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// let's enter the credit card details again
		// enter valid fields for credit card name, credit card number and expiry
		switchToMWIframe();
		directdebitmovein.creditCardName.sendKeys("Anthony Stark's III");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_14"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 1;
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
		softAssertion.assertFalse(isElementTicked(directdebitmovein.authorisationCreditCard, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.authorisationCreditCard);
		clickElementAction(acceptancemovein.header);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance page section");

		scrollPageDown(1200);
		// verify each section again
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
		concession = getDisplayedText(acceptancemovein.concessionRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(
				movingIn, concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate,
						" (Settlement on ", this.ownerSettlementDate, ") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd, "Service Address update Villa 5 5, 7 Albion Road Box Hill, Victoria, 3128",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Haemodialysis Machine Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				mainContact, concatStrings("Main Account Contact update Rashideh Maroun Birthdate: ",
						this.dateOfBirthMain, " Personal Id: Passport (aDv123, Philippines)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Iain Stuart Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61425228522 Birthdate: ", this.dateOfBirthAddContact1),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Steven Roger's Jr. Health Care Card 01321687450 Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Anthony Stark's III Card: ending ",
						getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
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

		// let's confirm the keys in the session storage
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
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
		String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
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
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
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
			populate3rdPartyPrefill("7", "Albion", StreetTypesEnum.st, "Box Hill", AustralianStatesEnum.WRONG_VALUE,
					"3128", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM, initialDate, true);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		sessionSourceId = storage.getItemFromSessionStorage("source_id");
		this.sourceID = sessionSourceId;

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			scrollPageDown(1000);
			clickElementAction(acceptancemovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
					"We are not yet in the Acceptance section");

			scrollPageDown(1500);
			// verify each section again
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
			String concession = getDisplayedText(acceptancemovein.concessionRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate, " (Settlement on ",
							this.ownerSettlementDate, ") Holiday Rental / Letting"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update Villa 5 5, 7 Albion Road Box Hill, Victoria, 3128",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSup,
					"Life Support update Life support required using the following equipment Haemodialysis Machine Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(
					mainContact, concatStrings("Main Account Contact update Rashideh Maroun Birthdate: ",
							this.dateOfBirthMain, " Personal Id: Passport (aDv123, Philippines)"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Data,
					concatStrings("Iain Stuart Email Address: ", getProp("test_dummy_email_upper_case"),
							" Mobile Phone: +61425228522 Birthdate: ", this.dateOfBirthAddContact1),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Email, SMS) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					"Concession update Steven Roger's Jr. Health Care Card 01321687450 Concession Card Uploaded",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit,
					concatStrings("Direct Debit update Credit Card Name On Card: Anthony Stark's III Card: ending ",
							getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
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
			// verify no checkboxes displayed
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.firstCheckboxList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.secondCheckboxList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.thirdCheckboxList),
					assertionErrorMsg(getLineNumber()));
			// verify no text are displayed from the checkbox
			softAssertion.assertFalse(isElementExists(acceptancemovein.lblFirstCheckboxList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.lblSecondCheckboxList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.lblThirdCheckboxList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
			
			// TODO finish the assertions once the ticket BBPRTL-2185 is fixed
			fail("Verify the fix for ticket BBPRTL-2185 then update the assertions here");
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-tenancy_street_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_type"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
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
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionLength, 30, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
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
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
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
			verifyMainContact(false);
			verifyAdditionalContact(false);
			verifyConcessionDetails(false);
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 36, assertionErrorMsg(getLineNumber()));
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
			sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMerchantWarrior), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			sessionQueryMoveInDate = storage.getItemFromSessionStorage("move-in-query-move_in_date");
			sessionQueryTenancyType = storage.getItemFromSessionStorage("move-in-query-tenancy_type");
			sessionQueryTenancyNum = storage.getItemFromSessionStorage("move-in-query-tenancy_number");
			sessionQueryTenancyStNum = storage.getItemFromSessionStorage("move-in-query-tenancy_street_number");
			sessionQueryTenancyStName = storage.getItemFromSessionStorage("move-in-query-tenancy_street_name");
			sessionQueryTenancyStType = storage.getItemFromSessionStorage("move-in-query-tenancy_street_type");
			sessionQueryTenancyCity = storage.getItemFromSessionStorage("move-in-query-tenancy_suburb");
			sessionQueryTenancyState = storage.getItemFromSessionStorage("move-in-query-tenancy_state");
			sessionQueryTenancyPostcode = storage.getItemFromSessionStorage("move-in-query-tenancy_postcode");
			sessionQueryAcctType = storage.getItemFromSessionStorage("move-in-query-account_type");
			sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			sessionQueryMobNum = storage.getItemFromSessionStorage("move-in-query-mobile_number");
			sessionQueryBusNum = storage.getItemFromSessionStorage("move-in-query-business_hour_phone");
			sessionQueryAfterHrNum = storage.getItemFromSessionStorage("move-in-query-after_hour_phone");
			sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
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
//			softAssertion.assertEquals(actualSize, 16, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		// add the property files before submitting the request
		addProp("ResiNewContact02_ownerSettlementDate", this.ownerSettlementDate);
		addProp("ResiNewContact02_ownerMoveInDateCRM", this.ownerMoveInDateCRM);
		addProp("ResiNewContact02_ownerSettlementDateCRM", this.ownerSettlementDateCRM);
		addProp("ResiNewContact02_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiNewContact02_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiNewContact02_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiNewContact02_dateOfBirthAddContact1", this.dateOfBirthAddContact1);
		addProp("ResiNewContact02_sourceID", this.sourceID);
		addProp("ResiNewContact02_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiNewContact02_dateSubmittedDash",
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