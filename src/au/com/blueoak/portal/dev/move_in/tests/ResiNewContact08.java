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
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact08 extends MoveInDevBase {

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

	/** 
	 * 
	 * */
	String propManMoveInDate;

	/** 
	 * 
	 * */
	String settlementDatePropMan;

	/** 
	 * 
	 * */
	String tenantMoveInDate;

	/** 
	 * 
	 * */
	String tenantMoveInDateCRM;

	/** 
	 * 
	 * */
	String dateOfBirthMain;

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

		// remove the previous lang files
		deleteMoveInCustomLangFiles(s3Access, "custom_en.json", "custom_fil.json", "custom_EN.json",
				"custom_en-au.json");

		// upload the global language file we are using
		uploadMoveInGlobalLangFile(s3Access, "02\\", "fil.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "01\\", "custom_en.json");

		// let's access the portal we are testing with
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "12\\", "portal_config.json");
			accessPortal(getStandaloneUrlMoveIn(), true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "12\\", "portal_config.json");
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(false, false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "12\\", "portal_config.json");
			accessPortal(getThirdPartyPrefillUrlMoveIn(), true);
			// enter the values of the prefill
			String initialDate = null;
			populate3rdPartyPrefill("6", "Mari", StreetTypesEnum.ST, "Alexandra Headland", AustralianStatesEnum.QLD,
					"4572", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "32\\", "portal_config.json");
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "account_category=",
					AccountCategoryEnum.RUM.name(), "&tenancy_type=",
					TenancyTypesEnum.Unit.getLabelText().toLowerCase(), "&complex_name='002 Complex\"s",
					"&tenancy_number=6", "&tenancy_street_number=6", "&tenancy_street_name=Mari",
					"&tenancy_street_type=", StreetTypesEnum.st.name(), "&tenancy_suburb=Alexandra Headland",
					"&tenancy_postcode=4572", "&tenancy_state=", AustralianStatesEnum.QLD.name().toLowerCase(),
					"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=0123Abcdef",
					"&contact_first_name=Doppo", "&contact_last_name=Orochi", "&after_hour_phone=", "&email_address=",
					getProp("test_dummy_email_lower_case"),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			// upload the correct portal_config.json we are testing
			uploadMoveInConfig(s3Access, "32\\", "portal_config.json");
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "account_category=",
					AccountCategoryEnum.RUM.name(), "&tenancy_type=",
					TenancyTypesEnum.Unit.getLabelText().toLowerCase(), "&complex_name='002 Complex\"s",
					"&tenancy_number=6", "&tenancy_street_number=6", "&tenancy_street_name=Mari",
					"&tenancy_street_type=", StreetTypesEnum.st.name(), "&tenancy_suburb=Alexandra Headland",
					"&tenancy_postcode=4572", "&tenancy_state=", AustralianStatesEnum.QLD.name().toLowerCase(),
					"&account_type=", AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=0123Abcdef",
					"&contact_first_name=Doppo", "&contact_last_name=Orochi", "&after_hour_phone=", "&email_address=",
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

		// here we set the browser language preference as Filipino
		// then uploaded a global fil.json language file
		// uploaded a custom_en.json custom language file only
		// so we assert that no language files from custom_en.json are used on the
		// portal
		// and it only used the fil.json global language file
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblMovingInHeader, true), "fil I am a:",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblTenant, true), "fil Tenant",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblOwner, true), "fil Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblPropManager, true),
				"fil Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lblSupplyAddHeader, true), "fil Supply Address",
				assertionErrorMsg(getLineNumber()));
		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddComplexName, true),
					"fil Complex Name (if known)", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyType, true),
					"fil Tenancy Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddTenancyNum, true),
					"fil Tenancy Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetNum, true),
					"fil Street Number", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetName, true),
					"fil Street Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddStreetType, true),
					"fil Street Type", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddCity, true),
					"fil City/Suburb", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddState, true), "fil State",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedPlaceholder(supplydetailsmovein.supplyAddPostcode, true),
					"fil Postcode", assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
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
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Trade Waste",
							"4 Main Account Contact (Doppo Orochi)", "5 Additional Contact",
							"6 Manager/Agent Company Details", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

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
		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
		}
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
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lifeSupYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lifeSupNoList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingYesList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.medCoolingNoList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblLifeSupIntroList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.propManager);
		}
		String future1Day = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 1, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(future1Day, Keys.TAB);
		this.propManMoveInDate = future1Day;
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.propManSettleNo);
		clickElementAction(supplydetailsmovein.propManSettleNo);
		String past1D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -1, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.settlementDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.settlementDatePropMan.sendKeys(past1D, Keys.TAB);
		this.settlementDatePropMan = past1D;
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("6");
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "6", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Unit", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("6");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(complexName, "'002 Complex\"s", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Unit", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "6", assertionErrorMsg(getLineNumber()));
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
		// verify all assertions
		softAssertion.assertAll();
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// let's put a complex name
			supplydetailsmovein.supplyAddComplexName.sendKeys("'002 Complex\"s");
		}

		// update the state
		clickElementAction(supplydetailsmovein.supplyAddState);
		deleteAllTextFromField();
		supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);

		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddComplexName, true),
				"fil Complex Name (if known)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyType, true),
				"fil Tenancy Type", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddTenancyNum, true),
				"fil Tenancy Number", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetNum, true),
				"fil Street Number", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetName, true),
				"fil Street Name", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddStreetType, true),
				"fil Street Type", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddCity, true),
				"fil City/Suburb", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddState, true), "fil State",
				assertionErrorMsg(getLineNumber()));
		// verify fix for bug ticket BBPRTL-2050
		softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.floaterLblSupplyAddPostcode, true),
				"fil Postcode", assertionErrorMsg(getLineNumber()));
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
			accountdetailsmovein.abnOrAcn.sendKeys("1234567890", Keys.TAB);
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			// verify in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify field does not exists
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(accountdetailsmovein.companyNameList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.residential);
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.commercial);
			accountdetailsmovein.abnOrAcn.sendKeys("ABCDEF", Keys.TAB);
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
			// verify in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify field does not exists
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(accountdetailsmovein.companyNameList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.residential);
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify in error state
			String abnAcn = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			softAssertion.assertEquals(abnAcn, "0123Abcdef", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify field does not exists
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(accountdetailsmovein.companyNameList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.residential);
			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(
					isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, CRM_IMPLICIT_WAIT_TIMEOUT),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(
					isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, CRM_IMPLICIT_WAIT_TIMEOUT),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.firstName.sendKeys("Doppo");
			mainaccountcontactmovein.lastName.sendKeys("Orochi");
			mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(mainaccountcontactmovein.next);
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
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
			assertFalse(isElementTicked(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0),
					"Account Notifications and Reminders SMS is still ticked");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		mainaccountcontactmovein.mobilePhone.sendKeys("0100998640", Keys.TAB);
		// verify that mobile phone is in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		mainaccountcontactmovein.mobilePhone.clear();
		mainaccountcontactmovein.mobilePhone.sendKeys("0410928640", Keys.TAB);

		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact() {

		// let's switch to the Move-In Iframe
		embeddedMoveInSwitchFrame(1);

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

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
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1Passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify that Provide None option is not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ProvideNoneList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		softAssertion.assertFalse(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS);
			assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersSMS, 0),
					"Account Notifications and Reminders SMS for 1st Additional Contact still ticked");
		}
		additionalcontactmovein.addCont1FirstName.sendKeys("Kiyosumi");
		additionalcontactmovein.addCont1LastName.sendKeys("Katou");
		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("000000");
		additionalcontactmovein.addCont1DriversLicenceState.sendKeys("Northern Territory", Keys.TAB);
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0612345678", Keys.TAB);
		// verify mobile phone in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1MobilePhone.clear();
		additionalcontactmovein.addCont1MobilePhone.sendKeys("0787654321");

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
		// verify we are in the concession details
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyAdditionalContact" })
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
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionNo, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.addConcessionYes);
		concessionmovein.cardHolderName.sendKeys("Rey Ms Terio");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 4);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 4);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		assertEquals(typeChosen, "DVA Gold Card", "The chosen Card Type is not correct");

		concessionmovein.cardNumber.sendKeys("9698664000");
		scrollPageDown(500);
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// this is to verify that we can skip the Manager/Agent Company Details
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyConcessionDetails" })
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

		clickElementAction(directdebitmovein.noDirectDebit);
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
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String addContact1lbl = getDisplayedText(acceptancemovein.addContact1Lbl, true);
		String addContact1UpdLink = getDisplayedText(acceptancemovein.addContact1Update, true);
		String addContact1Data = getDisplayedText(acceptancemovein.addContact1Data, true);
		String addContact1Notif = getDisplayedText(acceptancemovein.addContact1NotifRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String propManLettingAgent = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate, " (Settlement on ", this.settlementDatePropMan, ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update '002 Complex\"s Unit 6, 6 Mari Street Alexandra Headland, Victoria, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Doppo Orochi Email Address: ",
						getProp("test_dummy_email_lower_case"), " Mobile Phone: 0410928640"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Kiyosumi Katou Email Address: ", getProp("test_dummy_email_lower_case"),
						" Mobile Phone: 0787654321 Personal Id: Driver Licence (000000, Northern Territory)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Rey Ms Terio DVA Gold Card 9698664000 No Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(propManLettingAgent, "Property Manager / Letting Agent update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following not displayed
		// - life support
		// - trade waste
		// - postal
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.lifeSupportRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// update the supply details as Tenant
		// then go back to Acceptance page
		clickExactLinkNameFromElement(acceptancemovein.movingInRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), "We are not yet in the Supply Details section");
		clickElementAction(supplydetailsmovein.tenant);
		// try to fix the org.openqa.selenium.ElementNotInteractableException
		pauseSeleniumExecution(1000);
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String td = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(today, Keys.TAB);
		this.tenantMoveInDate = today;
		this.tenantMoveInDateCRM = td;
		// click the button again to dismiss the calendar
		clickElementAction(supplydetailsmovein.tenant);
		// update the state too
		clickElementAction(supplydetailsmovein.supplyAddState);
		deleteAllTextFromField();
		supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
		scrollPageDown(900);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are redirected into the main contact immediately
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that radio button not ticked by default
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.driversLicence, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.passport, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.medicareCard, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(mainaccountcontactmovein.provideNone, 0),
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
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.provideNone, 5, 0),
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
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(mainaccountcontactmovein.dateOfBirth);
		pauseSeleniumExecution(1000);
		String todayMinus8 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -8, DATE_MONTH_YEAR_FORMAT_SLASH);
		String birthYr = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 18);
		String validBirthDate = getString(todayMinus8, 0, todayMinus8.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		mainaccountcontactmovein.dateOfBirth.sendKeys(validBirthDate, Keys.TAB);
		pauseSeleniumExecution(1000);
		clickElementAction(mainaccountcontactmovein.emailAddress);
		// verify we have a value on the Date of Birth
		String dateOfBirth = getDisplayedValue(mainaccountcontactmovein.dateOfBirth, true);
		this.dateOfBirthMain = dateOfBirth;
		// verify Date of Birth is not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.passport);
		mainaccountcontactmovein.passportNumber.sendKeys("0Ab8400");
		mainaccountcontactmovein.passportCountry.sendKeys("Congo, Democratic Republic of");
		clickElementAction(mainaccountcontactmovein.lblNotificationHeader);
		scrollPageDown(900);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify we are redirected immediately into the Concession section
		String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
		String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, false);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the Type of Concession Card is in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the values that was cleared and was not
		softAssertion.assertEquals(cardHolder, "Rey Ms Terio", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cardType), assertionErrorMsg(getLineNumber()));
		// verify the Concession Card Number and Expiry is not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 2);
		pauseSeleniumExecution(1000);
		String typeChosen = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
		softAssertion.assertEquals(typeChosen, "DVA Gold Card", assertionErrorMsg(getLineNumber()));
		// verify not in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.cardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify empty
		softAssertion.assertTrue(StringUtils.isBlank(getDisplayedValue(concessionmovein.cardNumber, false)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		concessionmovein.cardNumber.sendKeys("9698664000");
		scrollPageDown(900);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify each section again
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
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
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update '002 Complex\"s Unit 6, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Doppo Orochi Email Address: ",
						getProp("test_dummy_email_lower_case"), " Mobile Phone: 0410928640 Birthdate: ",
						this.dateOfBirthMain, " Personal Id: Passport (0Ab8400, Congo, Democratic Republic of)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Kiyosumi Katou Email Address: ", getProp("test_dummy_email_lower_case"),
						" Mobile Phone: 0787654321 Personal Id: Driver Licence (000000, Northern Territory)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Rey Ms Terio DVA Gold Card 9698664000 No Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following not displayed
		// - life support
		// - trade waste
		// - postal
		// - Property Manager
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.lifeSupportRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickExactLinkNameFromElement(acceptancemovein.mainContactRow, "update");
		pauseSeleniumExecution(1000);
		// verify we are in the main contact
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lastName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.provideNone);
		scrollPageDown(900);
		clickElementAction(acceptancemovein.header);
		pauseSeleniumExecution(1000);

		// verify each section again
		movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
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
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update '002 Complex\"s Unit 6, 6 Mari Street Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Doppo Orochi Email Address: ",
						getProp("test_dummy_email_lower_case"), " Mobile Phone: 0410928640 Birthdate: ",
						this.dateOfBirthMain),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Kiyosumi Katou Email Address: ", getProp("test_dummy_email_lower_case"),
						" Mobile Phone: 0787654321 Personal Id: Driver Licence (000000, Northern Territory)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Rey Ms Terio DVA Gold Card 9698664000 No Concession Card Uploaded",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following not displayed
		// - life support
		// - trade waste
		// - postal
		// - Property Manager
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.lifeSupportRowList),
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 11, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_first_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-contact_last_name"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-email_address"),
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
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionPropManager = storage.getItemFromSessionStorage("move-in.letting_agent");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPropManager), assertionErrorMsg(getLineNumber()));
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
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
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
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryEmailAdd = storage.getItemFromSessionStorage("move-in-query-email_address");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryEmailAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		this.sourceID = sessionSourceId;
		// let's confirm the values stored in the local storage
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
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
//			softAssertion.assertEquals(actualSize, 21, "Incorrect number of objects inside the bucket '"
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
		addProp("ResiNewContact08_tenantMoveInDate", this.tenantMoveInDate);
		addProp("ResiNewContact08_tenantMoveInDateCRM", this.tenantMoveInDateCRM);
		addProp("ResiNewContact08_dateOfBirthMain", this.dateOfBirthMain);
		addProp("ResiNewContact08_sourceID", this.sourceID);
		addProp("ResiNewContact08_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));
		addProp("ResiNewContact08_dateSubmittedDash",
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
