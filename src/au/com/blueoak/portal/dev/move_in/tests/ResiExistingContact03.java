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
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiExistingContact03 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	SupplyDetailsMoveIn supplydetailsmovein;
	AccountDetailsMoveIn accountdetailsmovein;
	MainAccountContactMoveIn mainaccountcontactmovein;
	AdditionalContactMoveIn additionalcontactmovein;
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
	String dateOfBirthAddContact2;

	/** 
	 * 
	 * */
	String medicareExpiryMain;

	/** 
	 * 
	 * */
	String medicareExpiryMainMonth;

	/** 
	 * 
	 * */
	int medicareExpiryMainMonthInt;

	/** 
	 * 
	 * */
	String medicareExpiryMainYear;

	/**
	 * 
	 *  */
	String medicareExpiryAddCont2;

	/**
	 * 
	 *  */
	String medicareExpiryAddCont2Month;

	/** 
	 * 
	 * */
	int medicareExpiryAddCont2MonthInt;

	/** 
	 * 
	 * */
	String medicareExpiryAddCont2Year;

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
	String tenantMoveInDate;

	/** 
	 * 
	 * */
	String tenantMoveInDateCRM;

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
		deleteMoveInCustomLangFiles(s3Access, "custom_en.json", "custom_fil.json", "custom_EN.json");

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "05\\", "portal_config.json");

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
			populate3rdPartyPrefill(null, "Yilka", StreetTypesEnum.WRONG_VALUE, "Cosmo Newbery",
					AustralianStatesEnum.WRONG_VALUE, "6440", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM,
					initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// using the format DATE_MONTH_YEAR_FORMAT_SLASH
			String moveInDate = concatStrings("12/", String.valueOf(getCurrentMonth(false)), "/",
					String.valueOf(getCurrentYear()));
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "account_category=",
					AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate, "&complex_name=", "&tenancy_type=",
					TenancyTypesEnum.aParTmEntS.name(), "&tenancy_number=Apt-#01", "&tenancy_street_name=Yilka",
					"&tenancy_street_type=", StreetTypesEnum.DR.name(), "&tenancy_suburb=Cosmo Newbery",
					"&tenancy_postcode=6440", "&tenancy_state=", AustralianStatesEnum.WA.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=012345", "&contact_last_name=987650",
					"&mobile_number=1300852060", "&after_hour_phone=", "&email_address=@testing.com",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// using the format DATE_MONTH_YEAR_FORMAT_SLASH
			String moveInDate = concatStrings("12/", String.valueOf(getCurrentMonth(false)), "/",
					String.valueOf(getCurrentYear()));
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "account_category=",
					AccountCategoryEnum.RUM.name(), "&move_in_date=", moveInDate, "&complex_name=", "&tenancy_type=",
					TenancyTypesEnum.aParTmEntS.name(), "&tenancy_number=Apt-#01", "&tenancy_street_name=Yilka",
					"&tenancy_street_type=", StreetTypesEnum.DR.name(), "&tenancy_suburb=Cosmo Newbery",
					"&tenancy_postcode=6440", "&tenancy_state=", AustralianStatesEnum.WA.name(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=012345", "&contact_last_name=987650",
					"&mobile_number=1300852060", "&after_hour_phone=", "&email_address=@testing.com",
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

		// so here the custom language file setting is false in the config
		// and the browser language is Filipino
		// however there's no global language file fil.json
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
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
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
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Additional Contact", "5 Concession",
					"6 Manager/Agent Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-2011
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
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details",
							"3 Main Account Contact (012345 987650)", "4 Additional Contact", "5 Concession",
							"6 Manager/Agent Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
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
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.lblSupplyConnectedQuestionList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyConnectedList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyDisconnectedList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.supplyUnknownList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify that the life support introduction is not displayed
		String lifeSupIntro = getDisplayedText(supplydetailsmovein.lblLifeSupIntro, false);
		softAssertion.assertTrue(StringUtils.isBlank(lifeSupIntro), assertionErrorMsg(getLineNumber()));
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
			supplydetailsmovein.supplyAddComplexName.sendKeys("Cosmo Newbery Community");
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Suite", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("Apt-#01");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Yilka");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Drive", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Cosmo Newbery");
			supplydetailsmovein.supplyAddState.sendKeys("Western Australia", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("6440");

			scrollPageDown(500);
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);

			softAssertion.assertTrue(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManager, 5, 0),
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

			scrollPageDown(500);
			clickElementAction(accountdetailsmovein.header);
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

			openNewTabAndSwitchToIt();
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill(null, "Yilka", StreetTypesEnum.DR, "Cosmo Newbery", AustralianStatesEnum.WA, "6440",
					AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM, initialDate, true);
			// let's switch to the Move-In Iframe
			if (getPortalType().equals("embedded")) {
				// let's switch to the Move-In Iframe
				switchToMoveInEmbeddedIframe(1);
				// make sure that the elements are now displayed
				waitUntilElementIsDisplayed(supplydetailsmovein.header, PORTAL_ELEMENT_WAIT_TIMEOUT,
						PORTAL_IMPLICIT_WAIT_TIMEOUT);
			}

			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			// for now the dev prefill cannot populate the data for Property Manager
			// so we assert it to be blank for now
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			// verify that it's also in error state
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManSettleNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Additional Contact", "5 Concession",
					"6 Manager/Agent Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			supplydetailsmovein.supplyAddTenancyType.sendKeys("Suite", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("Apt-#01");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
			// verify fields in error state
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
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddCity, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddState, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddPostcode, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1991
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
			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details",
							"3 Main Account Contact (012345 987650)", "4 Additional Contact", "5 Concession",
							"6 Manager/Agent Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Suite", Keys.TAB);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(supplydetailsmovein.supplyDisconnected);
		assertTrue(isElementTicked(supplydetailsmovein.supplyDisconnected, 0),
				"The Supply Disconnected option is not ticked");

		clickElementAction(supplydetailsmovein.lifeSupYes);
		scrollPageDown(500);
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.medCoolingNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Ventilator for Life Support"));
		clickElementAction(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
				"Chronic Positive Airways Pressure Respirator"));
		// let's click the life support again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// upload life support that is marked as virus
		uploadLifeSupMedCoolingFiles(TEST_VIRUS_FILES_DIR, "eicar.com.pdf");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement eicar.com .pdf 0.1 KB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify the section header
		String header = getDisplayedText(
				getElementFrmMatStepHdrTag(supplydetailsmovein.matStepHeader, "Supply Details"), true);
		softAssertion.assertEquals(header, "1 Supply Details", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(400);
		clickElementAction(supplydetailsmovein.medCoolingNo);

		scrollPageUp(400);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.propManager);

			// verify the expected sections that should be displayed
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Additional Contact", "5 Manager/Agent Company Details",
					"6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		clickElementAction(supplydetailsmovein.datePickerMoveInDatePropMan);
		// since the calendar pop-op overlaps with this field
		// we click supplyAddTenancyType so it would choose a date
		// from the calendar
		clickElementAction(supplydetailsmovein.supplyAddTenancyType);
		assertFalse(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
				"Failed to click a date from the calendar for Move In Date");
		clickElementAction(supplydetailsmovein.propManSettleNo);
		clickElementAction(supplydetailsmovein.datePickerSettlementDatePropMan);
		// since the calendar pop-op overlaps with this field
		// we click supplyAddStreetNum so it would choose a date
		// from the calendar
		clickElementAction(supplydetailsmovein.supplyAddStreetNum);
		assertFalse(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.settlementDatePropMan, false)),
				"Failed to click a date from the calendar for Settlement Date");
		clickElementAction(supplydetailsmovein.ownerPropManHolidayYes);
		clickElementAction(supplydetailsmovein.whoIsResponsibleOwner);
		this.propManMoveInDate = getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false);
		this.propManSettlementDate = getDisplayedValue(supplydetailsmovein.settlementDatePropMan, false);

		scrollPageDown(400);
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				"We are not yet in the Account Details section");
		// verify all assertions
		softAssertion.assertAll();

		// verify the fix for bug ticket BBPRTL-2005
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		logDebugMessage(concatStrings("The value of sessionSupplyDetails is ", sessionSupplyDetails));
		String data1 = "\"tenant_selected\":false,\"tenant_date\":null,\"owner_selected\":false,\"owner_date\":null,\"manager_selected\":true,\"project_manager_date\":\""
				.concat(String.valueOf(getCurrentYear()));
		String data2 = "\"settlement_selected_yes\":false,\"settlement_selected_no\":true,\"settlement_date\":null,\"property_settlement_date\":\""
				.concat(String.valueOf(getCurrentYear()));
		logDebugMessage(concatStrings("The value of data1 is ", data1));
		logDebugMessage(concatStrings("The value of data2 is ", data2));
		softAssertion.assertTrue(sessionSupplyDetails.contains(data1), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionSupplyDetails.contains(data2), assertionErrorMsg(getLineNumber()));
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
			// verify not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
			// verify not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
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
	public void verifyMainContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// verify the text defined
		softAssertion.assertEquals(getDisplayedText(mainaccountcontactmovein.lblResponsibleForPaying, true),
				"Please provide the Tenant/Owner contact who will be responsible for ensuring that the account is paid when it becomes due.",
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
			// verify the prefilled values
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "012345",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "987650",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false), "@testing.com",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "1300852060",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.businessPhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false)),
					assertionErrorMsg(getLineNumber()));

			clickElementAction(mainaccountcontactmovein.next);
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
		}

		mainaccountcontactmovein.firstName.sendKeys("Peter");
		mainaccountcontactmovein.lastName.sendKeys("Tsoubos");
		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.mobilePhone.sendKeys("1300852060");
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
		// verify that add contact link is now displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1Next, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
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

		String firstNameAddCont1 = "elizabeth";
		String lastNameAddCont1 = "treonze";
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
		String todayminus2 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -2, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(todayminus2, 0, todayminus2.length() - 4);
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

		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		// verify not in error state
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
		// verify we are in the additional contact section
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1LastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1Passport);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		// verify fields no longer in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the minimum characters error
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("A123");
		additionalcontactmovein.addCont1DriversLicenceState.sendKeys("western australia", Keys.TAB);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify no error if minimum characters is set
		clickElementAction(additionalcontactmovein.addCont1DriversLicenceNumber);
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("4");
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify no characters entered if we reach the max number of allowed characters
		// which is 12 characters
		clickElementAction(additionalcontactmovein.addCont1DriversLicenceNumber);
		deleteAllTextFromField();
		clickElementAction(additionalcontactmovein.addCont1DriversLicenceState);
		deleteAllTextFromField();
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("XyZ12387648023");
		additionalcontactmovein.addCont1DriversLicenceState.sendKeys("Western Australia", Keys.TAB);
		softAssertion.assertEquals(getDisplayedValue(additionalcontactmovein.addCont1DriversLicenceNumber, false),
				"XyZ123876480", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				getDisplayedValue(additionalcontactmovein.addCont1DriversLicenceNumber, false).length(), 12,
				assertionErrorMsg(getLineNumber()));

		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1BillsSMS);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0800838420");
		// put a pause because for some reason
		// it goes to the concession section after clicking
		// additionalcontactmovein.addCont1AddAnotherContact
		pauseSeleniumExecution(1000);

		clickElementAction(additionalcontactmovein.addCont1AddAnotherContact);
		pauseSeleniumExecution(1000);
		setImplicitWait(0);
		softAssertion.assertFalse(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"Concession section should not be displayed");
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2FirstName, 0),
				"We are not yet in the 2nd Additional Contacts section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
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
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2ProvideNone, 0),
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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2ProvideNone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsPostal, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersPostal, 0, 3),
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
		// verify the Contact secret code is not displayed
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2ContactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify each notification text
		String billsNotifText = getDisplayedText(additionalcontactmovein.addCont2LblBillsNotif, true);
		String acctNotifAndRemText = getDisplayedText(additionalcontactmovein.addCont2LblAcctnotifAndRemindersNotif,
				true);
		String marketComNotifText = getDisplayedText(additionalcontactmovein.addCont2LblMarketingComNotif, true);
		softAssertion.assertEquals(billsNotifText, "info Bills", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctNotifAndRemText, "info Account Notifications and Reminders",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketComNotifText, "info Marketing Communications",
				assertionErrorMsg(getLineNumber()));
		// verify the notifications that should not be ticked
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2BillsSMS, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersPostal, 0),
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
		// verify the tooltip message for each notification
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		hoverToElementAction(additionalcontactmovein.addCont2BillsNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2BillsNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipIcon);
		softAssertion.assertFalse(
				isElementExists(additionalcontactmovein.addCont2AcctnotifAndRemindersNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		hoverToElementAction(additionalcontactmovein.addCont2MarketingComNotifTooltipIcon);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2MarketingComNotifTooltipMsgList),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact is not displayed
		softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont2AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify remove contact is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify the required fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2FirstName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2LastName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2ProvideNone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsPostal, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsEmail, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BillsSMS, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AcctnotifAndRemindersPostal, 0, 3),
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
		// verify all assertions
		softAssertion.assertAll();

		String firstNameAddCont2 = "James";
		String lastNameAddCont2 = "Grasso";
		additionalcontactmovein.addCont2FirstName.sendKeys(firstNameAddCont2);
		additionalcontactmovein.addCont2LastName.sendKeys(lastNameAddCont2);

		// verify the validations for Date of Birth
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		// get the current date
		String todayminus3 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -3, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(todayminus3, 0, todayminus3.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(additionalcontactmovein.addCont2DateOfBirth);
		pauseSeleniumExecution(1000);
		additionalcontactmovein.addCont2DateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(additionalcontactmovein.addCont2AfterhoursPhone);
		// verify we have a value on the Date of Birth
		String dateOfBirth = getDisplayedValue(additionalcontactmovein.addCont2DateOfBirth, true);
		this.dateOfBirthAddContact2 = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont2MedicareCard);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the validation for Medicare Card
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the 2nd Additional Contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2MedicareCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the invalid medicare number
		additionalcontactmovein.addCont2MedicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		// verify we are still in the 2nd Additional Contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2MedicareCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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
		slowSendKeys(additionalcontactmovein.addCont2MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont2LblNotificationHeader);
		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// clear medicare card number and expiry
		clickElementAction(additionalcontactmovein.addCont2MedicareCardNumber);
		deleteAllTextFromField();
		clearDateField(additionalcontactmovein.addCont2MedicareCardExpiry);
		// put valid medicare card number
		additionalcontactmovein.addCont2MedicareCardNumber.sendKeys("2428 77813 2/1");
		int month = 3;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, "/", expYrStr);
		slowSendKeys(additionalcontactmovein.addCont2MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		this.medicareExpiryAddCont2Month = concatStrings("0", monthStr);
		this.medicareExpiryAddCont2MonthInt = month;
		this.medicareExpiryAddCont2Year = expYrStr;

		javaScriptClickElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersEmail, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont2AcctnotifAndRemindersSMS, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		String medCareExp = getDisplayedValue(additionalcontactmovein.addCont2MedicareCardExpiry, true);
		softAssertion.assertTrue(medCareExp.contains(medExp), assertionErrorMsg(getLineNumber()));
		this.medicareExpiryAddCont2 = medCareExp;
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are still in the 2nd Additional Contact
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont2MedicareCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont2MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont2AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont2AddAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(additionalcontactmovein.addCont2RemAdditionalContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont2EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont2MobilePhone.sendKeys("+61425228522");

		clickElementAction(additionalcontactmovein.addCont2Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact02" })
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
		// verify the text
		softAssertion.assertEquals(getDisplayedText(concessionmovein.concessionNotAvailableText, true),
				"We cannot apply concession directly to your account. However, please consult our comprehensive online guide that walks you through how to go about claiming your concession directly from the appropriate government organisation.",
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardHolderNameList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.address04, 0),
				"We are not yet in the Manager/Agent Company Details");
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
				"Please specify the property company via which the property is being managed",
				assertionErrorMsg(getLineNumber()));
		// verify the fields are not in error state
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
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
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify the required fields
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
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
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.companyName.sendKeys("The Company LLC");
		managerholidaylettingmovein.companyContactNum.sendKeys("+658450977 6-9 PM");
		managerholidaylettingmovein.address02.sendKeys("653 Magnolia Street");
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		// verify the required fields
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.companyContactNum, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address01, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address02, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address03, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(managerholidaylettingmovein.address04, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.city, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.state, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.postCode, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(managerholidaylettingmovein.country, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.city.sendKeys("Amsterdam");
		managerholidaylettingmovein.state.sendKeys("North Blue");
		managerholidaylettingmovein.postCode.sendKeys("6501");
		managerholidaylettingmovein.country.sendKeys("Åland Islands");

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
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
		directdebitmovein.bankAccountName.sendKeys("Thor Odinson");
		directdebitmovein.accountBSB.sendKeys("123456");
		directdebitmovein.accountNumber.sendKeys("009846540");
		// verify the authorisation text and checkbox not displayed
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblBankAccountAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationBankAccountList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");
		clickElementAction(additionalnotemovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccountName, 0),
				"We are not yet in the Direct Debit section");

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
		directdebitmovein.creditCardName.sendKeys("Doward Stark");
		clickElementAction(directdebitmovein.creditCardNumber);
		deleteAllTextFromField();
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_10"), true, 300);
		clickElementAction(directdebitmovein.creditCardExpiry);
		deleteAllTextFromField();
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
		// verify the authorisation text and checkbox not displayed
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalnotemovein.header);
		moveInDirectDebitCCProgBarLoad();
		waitForScreenToRender();
		assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				"We are not yet in the Additional Note section");

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
	@Test(priority = 9, dependsOnMethods = { "verifyDirectDebitDetails" })
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
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String managerAgent = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate, " (Settlement on ", this.propManSettlementDate,
						") Holiday Rental / Letting"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update Cosmo Newbery Community Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update dummy complex Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Chronic Positive Airways Pressure Respirator Ventilator for Life Support Medical Cooling NOT REQUIRED Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Tenant/Owner Account Contact update Peter Tsoubos Email Address: ",
						getProp("test_dummy_email_upper_case"), " Mobile Phone: 1300852060"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Tenant/Owner Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("elizabeth treonze Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: 0800838420 Birthdate: ", this.dateOfBirthAddContact1,
						" Personal Id: Driver Licence (XyZ123876480, Western Australia)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (Email, SMS) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2lbl, "Additional Contact 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Data,
				concatStrings("James Grasso Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61425228522 Birthdate: ", this.dateOfBirthAddContact2,
						" Personal Id: Medicare Card (2428 77813 2/1, ", this.medicareExpiryAddCont2, ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Notif, concatStrings(
				"Additional Contact 2 Notification update Bills (None) Notifications and Reminders (Email, SMS) Marketing (None)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession, "Concession update Applicable", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(managerAgent,
				"Property Manager / Letting Agent update The Company LLC +658450977 6-9 PM 653 Magnolia Street Amsterdam, North Blue, 6501 Åland Islands",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Doward Stark Card: ending ",
						getProp("test_data_12"), " / Exp: ", this.creditCardExpiry),
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
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify we go to the supply details section
		clickExactLinkNameFromElement(acceptancemovein.movingInRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), "We are not yet in the Supply Details section");
		clickElementAction(supplydetailsmovein.tenant);
		// did this to fix an issue in jenkins where it does not click
		// the Tenant option
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 1);
		boolean isDisp = isElementDisplayed(supplydetailsmovein.moveInDateTenant, 0);
		logDebugMessage((isDisp ? "Tenant radio button was successfully clicked the first time"
				: "Tenant radio button was NOT successfully clicked the first time"));
		int counter = 1;
		int maxRetry = 5;
		while (isDisp == false && counter <= maxRetry) {
			logDebugMessage(
					concatStrings("Tenant was not clicked the first time, will try again. Current attempt(s) is <",
							Integer.toString(counter), ">"));
			clickElementAction(supplydetailsmovein.tenant);
			pauseSeleniumExecution(500);
			isDisp = isElementDisplayed(supplydetailsmovein.moveInDateTenant, 0);
			counter++;
		}
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
//		String future16Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 16, DATE_MONTH_YEAR_FORMAT_SLASH);
//		String future16D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 16, DATE_MONTH_YEAR_FORMAT_DASH);
//		clickButton(supplydetailsmovein.moveInDateTenant);
//		pauseSeleniumExecution(1000);
//		supplydetailsmovein.moveInDateTenant.sendKeys(future16Days, Keys.TAB);
//		// click the button again to ensure that the calendar is collapsed
//		clickButton(supplydetailsmovein.tenant);
		clickElementAction(supplydetailsmovein.datePickerMoveInDateTenant);
		clickElementAction(supplydetailsmovein.supplyAddCity);
		// since the calendar pop-op overlaps with this field
		// we click supplyAddCity so it would choose a date
		// from the calendar
		this.tenantMoveInDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, false);
		this.tenantMoveInDateCRM = this.tenantMoveInDate.replaceAll("/", "-");
		softAssertion.assertFalse(StringUtils.isBlank(this.tenantMoveInDate), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(this.tenantMoveInDateCRM), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1500);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);
		// verify the fix for bug ticket BBPRTL-2005
		BrowserLocalSessionStorage storage = new BrowserLocalSessionStorage(driver);
		List<String> sessionKeys = storage.getAllKeysFromSessionStorage();
		long sessionLength = storage.getSessionStorageLength();
		logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
				String.valueOf(sessionLength), ">"));
		List<String> localKeys = storage.getAllKeysFromLocalStorage();
		long localLength = storage.getLocalStorageLength();
		logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
				String.valueOf(localLength), ">"));
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		logDebugMessage(concatStrings("The value of sessionSupplyDetails is ", sessionSupplyDetails));
		String data1 = "\"tenant_selected\":true,\"tenant_date\":\"".concat(String.valueOf(getCurrentYear()));
		logDebugMessage(concatStrings("The value of data1 is ", data1));
		softAssertion.assertTrue(sessionSupplyDetails.contains(data1), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionSupplyDetails.contains(
				"\"owner_selected\":false,\"owner_date\":null,\"manager_selected\":false,\"project_manager_date\":null,\"settlement_selected_yes\":null,\"settlement_selected_no\":false,\"settlement_date\":null,\"property_settlement_date\":null,\"holiday_rental_yes\":null,\"holiday_rental_no\":null,\"responsibleForPaying\":null,"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify that we are redirected immediately into the main contact details
		// section
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");
		assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				"We are not yet in the Main Account Contact section");
		// verify the fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.provideNone, 5, 0),
				assertionErrorMsg(getLineNumber()));
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
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, true);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, true);
		softAssertion.assertEquals(firstName, "Peter", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Tsoubos", assertionErrorMsg(getLineNumber()));
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
		clickElementAction(mainaccountcontactmovein.driversLicence);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the not allowed characters
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		mainaccountcontactmovein.driversLicenceState.sendKeys("East Blue", Keys.TAB);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("AbC123657");
		clickElementAction(mainaccountcontactmovein.driversLicenceState);
		deleteAllTextFromField();
		mainaccountcontactmovein.driversLicenceState.sendKeys("Australian Capital Territory", Keys.TAB);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.driversLicenceNumber, false), "AbC123657",
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
		// verify the maximum characters allowed
		mainaccountcontactmovein.passportNumber.sendKeys("qwerTy12367890");
		mainaccountcontactmovein.passportCountry.sendKeys("Åland Islands", Keys.TAB);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.passportNumber, true), "qwerTy1236",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.medicareCard);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the invalid medicare number
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428 77813", Keys.TAB);
		clickElementAction(additionalcontactmovein.addCont1Header);
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
		clickElementAction(additionalcontactmovein.addCont1Header);
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
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428778132");
		int month = 3;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		medExp = concatStrings(monthStr, "/", expYrStr);
		// click to fix ElementNotInteractableException
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
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
		scrollPageDown(1200);
		// go back to the acceptance page
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

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
		addContact2lbl = getDisplayedText(acceptancemovein.addContact2Lbl, true);
		addContact2UpdLink = getDisplayedText(acceptancemovein.addContact2Update, true);
		addContact2Data = getDisplayedText(acceptancemovein.addContact2Data, true);
		addContact2Notif = getDisplayedText(acceptancemovein.addContact2NotifRow, true);
		concession = getDisplayedText(acceptancemovein.concessionRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update Cosmo Newbery Community Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update dummy complex Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(servAdd,
					"Service Address update Suite Apt-#01, Yilka Drive Cosmo Newbery, Western Australia, 6440 Service currently disconnected",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Chronic Positive Airways Pressure Respirator Ventilator for Life Support Medical Cooling NOT REQUIRED Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Peter Tsoubos Email Address: ",
						getProp("test_dummy_email_upper_case"), " Mobile Phone: 1300852060 Birthdate: ",
						this.dateOfBirthMain, " Personal Id: Medicare Card (2428778132, ", this.medicareExpiryMain,
						")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("elizabeth treonze Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: 0800838420 Birthdate: ", this.dateOfBirthAddContact1,
						" Personal Id: Driver Licence (XyZ123876480, Western Australia)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (Email, SMS) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2lbl, "Additional Contact 2", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Data,
				concatStrings("James Grasso Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +61425228522 Birthdate: ", this.dateOfBirthAddContact2,
						" Personal Id: Medicare Card (2428 77813 2/1, ", this.medicareExpiryAddCont2, ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact2Notif, concatStrings(
				"Additional Contact 2 Notification update Bills (None) Notifications and Reminders (Email, SMS) Marketing (None)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession, "Concession update Applicable", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Doward Stark Card: ending ",
						getProp("test_data_12"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("merchant_warrior"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the fix for bug ticket BBPRTL-2026
			softAssertion.assertFalse(sessionKeys.contains("public_holidays"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 32, assertionErrorMsg(getLineNumber()));
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
		String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
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
		softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
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
			// verify the fix for bug ticket BBPRTL-2026
			String sessionPublicHolidays = storage.getItemFromSessionStorage("public_holidays");
			softAssertion.assertTrue(StringUtils.isBlank(sessionPublicHolidays), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryMoveInDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTenancyNum), assertionErrorMsg(getLineNumber()));
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

		// verify in the S3 bucket development-presign-upload that the files are there
		// before submitting the request
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			int actualSize = s3Access.getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			List<String> objectIds = s3Access.getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
			logDebugMessage(concatStrings("Before submitting the request for class '", this.className,
					"', actualSize for S3 bucket ", S3_PORTAL_PRESIGN_BUCKET_NAME, " is <",
					Integer.toString(actualSize), "> and attachments ID's is/are -> ", objectIds.toString()));
//			softAssertion.assertEquals(actualSize, 12, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);
		// verify the required fields for the checkboxes
		clickElementAction(acceptancemovein.submit);
		// verify the tickboxes in error state
		softAssertion.assertTrue(isElementInError(acceptancemovein.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		// verify the 3rd checkbox not displayed
		// and also the text from the checkbox
		softAssertion.assertFalse(isElementExists(acceptancemovein.thirdCheckboxList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.lblThirdCheckboxList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// tick all 2 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);

		// add the property files before submitting the request
		addProp("ResiExistingContact03_tenantMoveInDate", this.tenantMoveInDate);
		addProp("ResiExistingContact03_tenantMoveInDateCRM", this.tenantMoveInDateCRM);
		addProp("ResiExistingContact03_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiExistingContact03_medicareExpiryMainMonth", this.medicareExpiryMainMonth);
		addProp("ResiExistingContact03_medicareExpiryMainYear", this.medicareExpiryMainYear);
		addProp("ResiExistingContact03_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiExistingContact03_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiExistingContact03_dateOfBirthAddContact1", this.dateOfBirthAddContact1);
		addProp("ResiExistingContact03_dateOfBirthAddContact2", this.dateOfBirthAddContact2);
		addProp("ResiExistingContact03_medicareExpiryAddCont2Month", this.medicareExpiryAddCont2Month);
		addProp("ResiExistingContact03_medicareExpiryAddCont2Year", this.medicareExpiryAddCont2Year);
		addProp("ResiExistingContact03_medicareExpiryMainMonthInt", Integer.toString(this.medicareExpiryMainMonthInt));
		addProp("ResiExistingContact03_medicareExpiryAddCont2MonthInt",
				Integer.toString(this.medicareExpiryAddCont2MonthInt));
		addProp("ResiExistingContact03_sourceID", this.sourceID);
		addProp("ResiExistingContact03_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiExistingContact03_dateSubmittedDash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH));

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
	}

}
