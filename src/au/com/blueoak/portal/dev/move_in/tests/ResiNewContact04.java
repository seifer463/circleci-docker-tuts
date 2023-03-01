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
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact04 extends MoveInDevBase {

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
	String tenantMoveInDate;

	/** 
	 * 
	 * */
	String tenantMoveInDateCRM;

	/**
	 * 
	 *  */
	String concessionExpiry;

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

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "08\\", "portal_config.json");

		// upload the global language file we are using
		uploadMoveInGlobalLangFile(s3Access, "01\\", "FIL.json");

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
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("192", "Marine", StreetTypesEnum.PDE, "Coolangatta", AustralianStatesEnum.QLD,
					"4225", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.TENANT, initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&complex_name=", "&tenancy_type=", TenancyTypesEnum.NA.name(), "&tenancy_number=1328",
					"&tenancy_street_number=192", "&tenancy_street_name=Marine", "&tenancy_street_type=",
					StreetTypesEnum.CDS.getLabelText(), "&tenancy_suburb=Coolangatta", "&tenancy_postcode=4225",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&contact_last_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&mobile_number=1300277230",
					"&email_address=email!testing.com",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 21,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.TENANT.name(), "&move_in_date=", moveInDate,
					"&complex_name=", "&tenancy_type=", TenancyTypesEnum.NA.name(), "&tenancy_number=1328",
					"&tenancy_street_number=192", "&tenancy_street_name=Marine", "&tenancy_street_type=",
					StreetTypesEnum.CDS.getLabelText(), "&tenancy_suburb=Coolangatta", "&tenancy_postcode=4225",
					"&tenancy_state=", AustralianStatesEnum.qLD.getLabelText(), "&account_type=",
					AccountTypesEnum.RESIDENTIAL.name(), "&business_number=", getProp("test_data_valid_acn2"),
					"&business_trading_name=My Cloud", "&contact_first_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
					"&contact_last_name=~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "&mobile_number=1300277230",
					"&email_address=email!testing.com",
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

		// here we turned off the custom language file in the config
		// set a Filipino browser language preference
		// however the uploaded global language file is FIL.json is in upper case
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details",
					"3 Main Account Contact (~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./)",
					"4 Additional Contact", "5 Concession", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
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
		// verify that the life support introduction is not displayed
		String lifeSupIntro = getDisplayedText(supplydetailsmovein.lblLifeSupIntro, false);
		softAssertion.assertTrue(StringUtils.isBlank(lifeSupIntro), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.tenant);
			// verify fields not in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
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
			clickElementAction(supplydetailsmovein.lifeSupYes);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
					"Please select one or more life support devices in-use:", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3,
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
									"Intermittent Peritoneal Dialysis Machine"), 3, 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					3, 0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
									"Chronic Positive Airways Pressure Respirator"), 3, 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
							false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"),
							4, 0),
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(supplydetailsmovein.dragAndDropArea, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.lifeSupYes);
			softAssertion.assertEquals(getDisplayedText(supplydetailsmovein.lifeSupEquipIntro, true),
					"Please select one or more life support devices in-use:", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 3,
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
									"Intermittent Peritoneal Dialysis Machine"), 3, 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					3, 0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
									"Chronic Positive Airways Pressure Respirator"), 3, 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Crigler Najjar Syndrome Phototherapy Equipment"), 3, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
							false, "Ventilator for Life Support"), 3, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementInError(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"),
							4, 0),
					assertionErrorMsg(getLineNumber()));
			supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(supplydetailsmovein.dragAndDropArea, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();

			clearDateField(supplydetailsmovein.moveInDateTenant);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's put a valid lease commencement date as 8 days from future
		String future8Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 8, DATE_MONTH_YEAR_FORMAT_SLASH);
		String future8D = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 8, DATE_MONTH_YEAR_FORMAT_DASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future8Days, Keys.TAB);
		// click Tenant again to ensure that calendar is collapsed
		clickElementAction(supplydetailsmovein.tenant);
		this.tenantMoveInDate = future8Days;
		this.tenantMoveInDateCRM = future8D;

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Office", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("301");
			supplydetailsmovein.supplyAddStreetNum.sendKeys("192");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Marine");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Parade", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Coolangatta");
			supplydetailsmovein.supplyAddState.sendKeys("Queensland", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("4225");
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Office", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "301", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Parade", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(complexName, "dummy complex", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Parade", assertionErrorMsg(getLineNumber()));
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Office", Keys.TAB);
			supplydetailsmovein.supplyAddTenancyNum.sendKeys("301");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Cul-de-sac", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddStreetType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Office", Keys.TAB);
			tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			assertTrue(StringUtils.isBlank(tenancyNum), "Tenancy Number is not empty/blank");

			supplydetailsmovein.supplyAddTenancyNum.sendKeys("301");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Parade", Keys.TAB);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "192", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Marine", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Coolangatta", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "4225", assertionErrorMsg(getLineNumber()));
		// verify that the Street Type is not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddStreetType, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
				assertionErrorMsg(getLineNumber()));
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

		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		supplydetailsmovein.lifeSuppOtherInput.sendKeys("Other's Machinery\"s");
		// verify checkbox ticked
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				isMatPseudoChckbxTicked(
						getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.header);
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

			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fields are not displayed
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.abnOrAcn, 0),
					assertionErrorMsg(getLineNumber()));
			accountdetailsmovein = new AccountDetailsMoveIn(driver, 0);
			softAssertion.assertFalse(isElementDisplayed(accountdetailsmovein.tradingName, 0),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		clickElementAction(accountdetailsmovein.next);
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
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.contactSecretCodeList),
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(additionalcontactmovein.addCont1Header);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
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
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false),
					"~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false),
					"~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					"email!testing.com", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "1300277230",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.businessPhone, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false)),
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

			clickElementAction(additionalcontactmovein.addCont1Header);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.billsSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.acctnotifAndRemindersSMS, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComPostal, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComEmail, 0, 3),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.marketingComSMS, 0, 3),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		mainaccountcontactmovein.firstName.sendKeys("Baki");
		mainaccountcontactmovein.lastName.sendKeys("Hanma");
		javaScriptClickElementAction(mainaccountcontactmovein.billsEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.billsSMS);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.acctnotifAndRemindersSMS);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComEmail);
		javaScriptClickElementAction(mainaccountcontactmovein.marketingComSMS);

		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
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
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		mainaccountcontactmovein.emailAddress.sendKeys(getProp("test_dummy_email_lower_case"));
		mainaccountcontactmovein.mobilePhone.sendKeys("0185238692", Keys.TAB);
		// verify mobile phone in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.mobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.businessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.afterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter valid number
		mainaccountcontactmovein.mobilePhone.clear();
		mainaccountcontactmovein.mobilePhone.sendKeys("1300277230");

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

		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
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
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1EmailAddress, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1MobilePhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1BusinessPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1AfterhoursPhone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1FirstName.sendKeys("Yuujiro");
		additionalcontactmovein.addCont1LastName.sendKeys("Hanma");
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComEmail);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComSMS);
		javaScriptClickElementAction(additionalcontactmovein.addCont1MarketingComPostal);
		additionalcontactmovein.addCont1EmailAddress.sendKeys(getProp("test_dummy_email_upper_case"));
		additionalcontactmovein.addCont1MobilePhone.sendKeys("+0000000000");
		additionalcontactmovein.addCont1BusinessPhone.sendKeys("+111111111111");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+2222222222222");

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		// verify we are in the postal address section
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressYes, 0),
				"We are not in the Postal Address section");
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
		// verify the fix for ticket BBPRTL-646
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

		// populate the address
		postaladdressmovein.addLine01.sendKeys("Add 01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.addLine02.sendKeys("Add 02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.addLine03.sendKeys("Add 03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.addLine04.sendKeys("Add 04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.city.sendKeys("City ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.state.sendKeys("State ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.postcode.sendKeys("Postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		postaladdressmovein.country.sendKeys("Åland Islands", Keys.TAB);

		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Concession section
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not in the Concession section");
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

		concessionmovein.cardHolderName.sendKeys("Peter Parker");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumber.sendKeys(getProp("test_data_14"));
		int month = 4;
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
		uploadConcessionFiles(ARTIFACTS_DIR, "Sprin't 02 Story 'Board.pdf");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board .pdf 0.4 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Direct Debit Details section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.lblSetupDirectDebit, 0),
				"We are not in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = { "verifyConcessionDetails" })
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

		clickElementAction(directdebitmovein.creditCard);
		moveInDirectDebitCCProgBarLoad();
		// put a pause to make sure all fields and images are loaded
		pauseSeleniumExecution(2000);
		clickElementAction(additionalnotemovein.header);
		waitForScreenToRender();
		// verify the fields are in error state
		switchToMWIframe();
		// verify we are still in the direct debit section
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.creditCardName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
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
		// verify all assertions
		softAssertion.assertAll();

		switchToMWIframe();
		// enter credit card details
		directdebitmovein.creditCardName.sendKeys("Morgans Starks");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_03"), true, 300);
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		int month = 11;
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
		// verify the authorisation text and checkbox not displayed
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(acceptancemovein.header);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");

		scrollPageUp(400);
		// go back to direct debit details
		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");

		// verify the details are are not cleared
		String actCreditCardName = getDisplayedValue(directdebitmovein.readOnlyCreditCardName, true);
		String actCreditCardNum = getDisplayedValue(directdebitmovein.readOnlyCreditCardNumber, true);
		String actCreditCardExp = getDisplayedValue(directdebitmovein.readOnlyCreditCardExpiry, true);
		softAssertion.assertEquals(actCreditCardName, "Morgans Starks", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardNum, getProp("test_data_04"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(actCreditCardExp, expiry, assertionErrorMsg(getLineNumber()));
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// hit the Change Credit Card Details btn
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

		switchToMWIframe();
		// enter credit card details
		directdebitmovein.creditCardName.sendKeys("Morgan Stark");
		slowSendKeys(directdebitmovein.creditCardNumber, getProp("test_data_14"), true, 300);
		expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		month = 12;
		monthStr = Integer.toString(month);
		expYrStrFull = Integer.toString(expYr);
		expYrStr = getString(expYrStrFull, 2, 4);
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
		directdebitmovein = new DirectDebitMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(directdebitmovein.lblCreditCardAuthorisationList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(directdebitmovein.authorisationCreditCardList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// verify we can skip the Additional Note section
		clickElementAction(acceptancemovein.header);
		moveInDirectDebitCCProgBarLoad();
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(acceptancemovein.lblAcceptanceIntro, 0),
				"We are not yet in the Acceptance section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 8, dependsOnMethods = { "verifyDirectDebitDetails" })
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
		String postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Office 301, 192 Marine Parade Coolangatta, Queensland, 4225",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Other's Machinery\"s",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Baki Hanma Email Address: ",
						getProp("test_dummy_email_lower_case"), " Mobile Phone: 1300277230"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (Email, SMS) Notifications and Reminders (Email, SMS) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Yuujiro Hanma Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +0000000000 Business Phone: +111111111111 A/Hours Phone: +2222222222222"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (Postal, Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"Postal Address update Add 01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ City ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, State ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Åland Islands",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				concession, concatStrings("Concession update Peter Parker Queensland Seniors Card ",
						getProp("test_data_14"), " (", this.concessionExpiry, ") Concession Card Uploaded"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Morgan Stark Card: ending ",
						getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify no trade waste section displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		// update the Supply Details state that has no concession card details
		clickExactLinkNameFromElement(acceptancemovein.serviceAddressRow, "update");
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(supplydetailsmovein.tenant, 0), "We are not yet in the Supply Details section");
		clickElementAction(supplydetailsmovein.supplyAddState);
		deleteAllTextFromField();
		String stateToEnter = "Northern Territory";
		supplydetailsmovein.supplyAddState.sendKeys(stateToEnter);
		clickElementAction(supplydetailsmovein.supplyAddPostcode);
		pauseSeleniumExecution(600);
		softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.supplyAddState, true), stateToEnter,
				assertionErrorMsg(getLineNumber()));
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		scrollPageDown(1500);
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
		postAdd = getDisplayedText(acceptancemovein.postalAddressRow, true);
		concession = getDisplayedText(acceptancemovein.concessionRow, true);
		directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Office 301, 192 Marine Parade Coolangatta, Northern Territory, 4225",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSup,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Other's Machinery\"s",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact,
				concatStrings("Main Account Contact update Baki Hanma Email Address: ",
						getProp("test_dummy_email_lower_case"), " Mobile Phone: 1300277230"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (Email, SMS) Notifications and Reminders (Email, SMS) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data,
				concatStrings("Yuujiro Hanma Email Address: ", getProp("test_dummy_email_upper_case"),
						" Mobile Phone: +0000000000 Business Phone: +111111111111 A/Hours Phone: +2222222222222"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (None) Marketing (Postal, Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"Postal Address update Add 01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ City ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, State ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Åland Islands",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession, "Concession update Applicable", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Direct Debit update Credit Card Name On Card: Morgan Stark Card: ending ",
						getProp("test_data_16"), " / Exp: ", this.creditCardExpiry),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify no trade waste section displayed
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 34, assertionErrorMsg(getLineNumber()));
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
		String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
		String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
		String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionConcessionFile = storage.getItemFromSessionStorage("move-in_concession_file");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionMerchantWarrior = storage.getItemFromSessionStorage("merchant_warrior");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
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

		scrollPageDown(1500);
		// verify the required fields for the checkboxes
		clickElementAction(acceptancemovein.submit);
		// verify the tickboxes in error state
		softAssertion.assertTrue(isElementInError(acceptancemovein.firstCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.secondCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(acceptancemovein.thirdCheckbox, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// tick all 3 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		javaScriptClickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("ResiNewContact04_tenantMoveInDate", this.tenantMoveInDate);
		addProp("ResiNewContact04_creditCardExpiryMonth", this.creditCardExpiryMonth);
		addProp("ResiNewContact04_creditCardExpiryYearFull", this.creditCardExpiryYearFull);
		addProp("ResiNewContact04_sourceID", this.sourceID);
		addProp("ResiNewContact04_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));

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