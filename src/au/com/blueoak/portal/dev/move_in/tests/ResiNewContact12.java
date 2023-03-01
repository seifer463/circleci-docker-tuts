package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
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
import au.com.blueoak.portal.pageObjects.move_in.PortalMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class ResiNewContact12 extends MoveInDevBase {

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
	PortalMoveIn portalmovein;
	AccessS3BucketWithVfs s3Access;

	/**
	 * Store the name of the class for logging
	 */
	String className;

	/** 
	 * 
	 * */
	String ownerMoveInDate;

	/** 
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
		uploadMoveInConfig(s3Access, "19\\", "portal_config.json");

		// upload the custom language files we are going to use
		uploadMoveInCustomLangFile(s3Access, "11\\", "custom_en.json");

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
			String initialDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17, MONTH_DATE_YEAR_FORMAT_SLASH);
			this.initialDate3rdPartyPref = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			populate3rdPartyPrefill("508", "Burwood Highway Service", StreetTypesEnum.RD, "Vermont South",
					AustralianStatesEnum.VIC, "3133", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.OWNER,
					initialDate, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=     	portal_config.json",
					"&account_category=	     ", AccountCategoryEnum.OWNER.name(), "&move_in_date=     	", moveInDate,
					"&tenancy_type=	     ", TenancyTypesEnum.Apt.name(), "&tenancy_number=     	1328",
					"&tenancy_street_number=	     508", "&tenancy_street_name=     	Burwood Highway Service",
					"&tenancy_street_type=   	  ", StreetTypesEnum.RD.name(),
					"&tenancy_suburb=	     	Vermont South", "&tenancy_postcode=     	     3133",
					"&tenancy_state=	     ", AustralianStatesEnum.VIC.name(), "&account_type=     	",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=	     ", getProp("test_data_valid_acn2"),
					"&business_trading_name=     	My Cloud", "&contact_first_name=	     	Jack",
					"&contact_last_name=	     	Hanma", "&mobile_number=     	     (02) 1300 5868",
					"&business_hour_phone=	     	(03) 9732 7755", "&after_hour_phone=     	 (04) 1739 8072",
					"&email_address=	     	", getProp("test_dummy_email_lower_case"),
					"&extra_data=     	     {\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17, DB_DATE_FORMAT);
			moveInDate = moveInDate.replaceAll("-", "");
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=     	portal_config.json",
					"&account_category=	     ", AccountCategoryEnum.OWNER.name(), "&move_in_date=     	", moveInDate,
					"&tenancy_type=	     ", TenancyTypesEnum.Apt.name(), "&tenancy_number=     	1328",
					"&tenancy_street_number=	     508", "&tenancy_street_name=     	Burwood Highway Service",
					"&tenancy_street_type=   	  ", StreetTypesEnum.RD.name(),
					"&tenancy_suburb=	     	Vermont South", "&tenancy_postcode=     	     3133",
					"&tenancy_state=	     ", AustralianStatesEnum.VIC.name(), "&account_type=     	",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=	     ", getProp("test_data_valid_acn2"),
					"&business_trading_name=     	My Cloud", "&contact_first_name=	     	Jack",
					"&contact_last_name=	     	Hanma", "&mobile_number=     	     (02) 1300 5868",
					"&business_hour_phone=	     	(03) 9732 7755", "&after_hour_phone=     	 (04) 1739 8072",
					"&email_address=	     	", getProp("test_dummy_email_lower_case"),
					"&extra_data=     	     {\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
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

		// verify the radio buttons are not selected
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.owner, 0),
					assertionErrorMsg(getLineNumber()));
			// verify displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.initialDate3rdPartyPref, assertionErrorMsg(getLineNumber()));
			// verify displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details", "2 Account Details",
					"3 Main Account Contact", "4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			this.ownerMoveInDate = this.initialDate3rdPartyPref;
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDateOwner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(supplydetailsmovein.moveInDateOwner, false),
					this.moveInDateUrlPrefill, assertionErrorMsg(getLineNumber()));
			// verify displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "create Account Details", "3 Main Account Contact (Jack Hanma)",
							"4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			this.ownerMoveInDate = this.moveInDateUrlPrefill;
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertFalse(isElementTicked(supplydetailsmovein.propManager, 0),
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
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.owner);

			// let's put a valid lease commencement date as 17 days from future
			String future17Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 17, DATE_MONTH_YEAR_FORMAT_SLASH);
			clickElementAction(supplydetailsmovein.moveInDateOwner);
			pauseSeleniumExecution(1000);
			supplydetailsmovein.moveInDateOwner.sendKeys(future17Days, Keys.TAB);
			this.ownerMoveInDate = future17Days;
			// initial click on the same field to dismiss the calendar
			// because the Keys.TAB on selenium did not dismiss the calendar
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);

			supplydetailsmovein.supplyAddStreetNum.sendKeys("508");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Burwood Highway Service");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Vermont South");
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("3133");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.ownerSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
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
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertFalse(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "508", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			supplydetailsmovein.supplyAddTenancyType.sendKeys("Not applicable");
			clickElementAction(supplydetailsmovein.supplyAddStreetNum);
			tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
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

		clickElementAction(supplydetailsmovein.lifeSupYes);
		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));
		clickElementAction(supplydetailsmovein.medCoolingNo);

		scrollPageDown(500);
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

			clickElementAction(accountdetailsmovein.commercial);
			// verify displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create Supply Details", "2 Account Details", "3 Main Account Contact",
							"4 Direct Debit", "5 Additional Note", "6 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn3"), Keys.TAB);
			accountdetailsmovein.tradingName.sendKeys("JaH Trading's");
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");

			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			scrollPageDown(400);
			clickElementAction(mainaccountcontactmovein.previous);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");
			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn3"), Keys.TAB);
			accountdetailsmovein.tradingName.sendKeys("JaH Trading's");
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");

			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			scrollPageDown(400);
			clickElementAction(mainaccountcontactmovein.previous);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");
			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify displayed values
			String abnAcn = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			String trading = getDisplayedValue(accountdetailsmovein.tradingName, false);
			softAssertion.assertEquals(abnAcn,
					concatStrings(getProp("test_data_valid_acn1"), " (",
							getProp("test_data_valid_company_name_acn1_acn2"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(trading, "My Cloud", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.abnOrAcn);
			deleteAllTextFromField();
			clickElementAction(accountdetailsmovein.tradingName);
			deleteAllTextFromField();

			accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn3"), Keys.TAB);
			accountdetailsmovein.tradingName.sendKeys("JaH Trading's");
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");

			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
					"We are not yet in the Main Account Contact section");

			scrollPageDown(400);
			clickElementAction(mainaccountcontactmovein.previous);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are not yet in the Account Details section");
			clickElementAction(accountdetailsmovein.residential);
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

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify fields are in error state
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify fields are not in error state
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.firstName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.lastName, 5, 0),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify Date of Birth is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.dateOfBirth, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify personal identification is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.lblPersonalIDHeader, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.driversLicenceList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.passportList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.medicareCardList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		// verify fields in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.provideNone, 5, 0),
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
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.firstName.sendKeys("Jack");
			mainaccountcontactmovein.lastName.sendKeys("Hanma");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Jack",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Hanma",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "0213005868",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "0397327755",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false), "0417398072",
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

		clickElementAction(mainaccountcontactmovein.driversLicence);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("1");
		mainaccountcontactmovein.driversLicenceState.sendKeys("East Blue", Keys.TAB);
		// verify in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.driversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we cannot go to next section
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are no longer in the Main Account Contact section");

		clickElementAction(mainaccountcontactmovein.provideNone);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");

		clickElementAction(mainaccountcontactmovein.passport);
		// verify in not error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		mainaccountcontactmovein.passportNumber.sendKeys("A");
		mainaccountcontactmovein.passportCountry.sendKeys("Wano", Keys.TAB);
		// verify in error state
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(mainaccountcontactmovein.passportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify we cannot go to next section
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are no longer in the Main Account Contact section");

		clickElementAction(mainaccountcontactmovein.provideNone);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");

		clickElementAction(mainaccountcontactmovein.medicareCard);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		mainaccountcontactmovein.medicareCardNumber.sendKeys("A1", Keys.TAB);
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

		// verify we cannot go to next section
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are no longer in the Main Account Contact section");

		clickElementAction(mainaccountcontactmovein.provideNone);
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				"We are not yet in the Main Account Contact section");

		// verify the fix for bug ticket BBPRTL-1969
		clickElementAction(mainaccountcontactmovein.driversLicence);
		clickElementAction(mainaccountcontactmovein.driversLicenceNumber);
		deleteAllTextFromField();
		clickElementAction(mainaccountcontactmovein.driversLicenceState);
		deleteAllTextFromField();
		mainaccountcontactmovein.driversLicenceNumber.sendKeys("B1");
		mainaccountcontactmovein.driversLicenceState.sendKeys("North Blue");
		// dismiss the dropdown
		clickElementAction(mainaccountcontactmovein.afterhoursPhone);
		clickElementAction(mainaccountcontactmovein.provideNone);

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);
		// verify we are in the additional contact
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

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		additionalcontactmovein.addCont1FirstName.sendKeys("Test FirstName");
		additionalcontactmovein.addCont1LastName.sendKeys("Test LastName");

		clickElementAction(additionalcontactmovein.addCont1DriversLicence);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		additionalcontactmovein.addCont1DriversLicenceNumber.sendKeys("1");
		additionalcontactmovein.addCont1DriversLicenceState.sendKeys("East Blue", Keys.TAB);
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1DriversLicenceState, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(200);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		// for embedded, even though element is outside the viewport
		// it says that the element is inside the viewport.
		// that results to a MoveTargetOutOfBoundsException
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalcontactmovein.addCont1Next)) {
				scrollIntoView(additionalcontactmovein.addCont1Next);
			}
		}

		// verify we cannot go to next section
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are no longer in the Additional Contact section");

		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");

		clickElementAction(additionalcontactmovein.addCont1Passport);
		// verify in not error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		additionalcontactmovein.addCont1PassportNumber.sendKeys("A");
		additionalcontactmovein.addCont1PassportCountry.sendKeys("Wano", Keys.TAB);
		// verify in error state
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(additionalcontactmovein.addCont1PassportCountry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(200);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		// for embedded, even though element is outside the viewport
		// it says that the element is inside the viewport.
		// that results to a MoveTargetOutOfBoundsException
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(concessionmovein.header)) {
				scrollIntoView(concessionmovein.header);
			}
		}

		// verify we cannot go to next section
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are no longer in the Additional Contact section");

		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(additionalcontactmovein.addCont1Header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the Additional Contact section");

		clickElementAction(additionalcontactmovein.addCont1MedicareCard);
		// verify not in error state
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardNumber, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(additionalcontactmovein.addCont1MedicareCardExpiry, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// enter invalid values
		additionalcontactmovein.addCont1MedicareCardNumber.sendKeys("A1", Keys.TAB);
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
		clickElementAction(additionalcontactmovein.addCont1MedicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(additionalcontactmovein.addCont1MedicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(additionalcontactmovein.addCont1LblNotificationHeader);

		scrollPageDown(200);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		// for embedded, even though element is outside the viewport
		// it says that the element is inside the viewport.
		// that results to a MoveTargetOutOfBoundsException
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(additionalcontactmovein.addCont1Next)) {
				scrollIntoView(additionalcontactmovein.addCont1Next);
			}
		}

		// verify we cannot go to next section
		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are no longer in the Additional Contact section");

		clickElementAction(additionalcontactmovein.addCont1ProvideNone);
		clickElementAction(concessionmovein.header);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are not yet in the Concession section");

		clickElementAction(concessionmovein.previous);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's delete the additional contact
		clickElementAction(additionalcontactmovein.addCont1RemAdditionalContact);
		// verify the text displayed
		String containerText = getDisplayedText(additionalcontactmovein.dialogContainerText, true);
		portalmovein = new PortalMoveIn(driver);
		// verify CSS and lang files
		softAssertion.assertEquals(containerText, "aRe yOu sUrE YoU WaNt tO DeLeTe tHiS CoNtAcT?",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDialogContainerCss(additionalcontactmovein.dialogContainer),
				DIALOG_CONTAINER_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getLabelCss(additionalcontactmovein.dialogContainerText), LABEL_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalcontactmovein.deleteContactYes),
				DIALOG_YES_AND_OK_BUTTON_CSTM, assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getButtonCss(additionalcontactmovein.deleteContactNo), DIALOG_NO_BUTTON_CSTM,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getCssBackgrndColorProp(portalmovein.overlayBackdrop), OVERLAY_BACKDROP_CSTM,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.deleteContactYes);
		pauseSeleniumExecution(1000);
		// verify we are redirected into the Main Account Contact
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the Add Contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.addAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(200);
		// for some reason in standalone, selenium clicks
		// the webelement even though it's outside the viewport.
		// causing issues because the validation is not triggered.
		// so we would check the element if it's outside the viewport
		// and if it is, we scroll the view into that element first
		// before clicking it.
		// for embedded, even though element is outside the viewport
		// it says that the element is inside the viewport.
		// that results to a MoveTargetOutOfBoundsException
		if (getPortalType().equals("standalone")) {
			if (!isElementWithinViewport(mainaccountcontactmovein.next)) {
				scrollIntoView(mainaccountcontactmovein.next);
			}
		}

		clickElementAction(mainaccountcontactmovein.next);
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
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.addConcessionNo, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.addConcessionYes);
		// verify fields not in error state
		softAssertion.assertFalse(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.agreeYes, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(concessionmovein.agreeNo, 5, 0), assertionErrorMsg(getLineNumber()));
		// verify that the Concession Card Number and Concession Card Number Expiry is
		// initially not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		// verify that the drag and drop is not displayed
		softAssertion.assertFalse(isElementExists(concessionmovein.dragAndDropAreaList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.header);
		pauseSeleniumExecution(1000);
		// verify fields in error state
		softAssertion.assertTrue(isElementInError(concessionmovein.cardHolderName, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.typeOfConcessionCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeYes, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeNo, 5, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		concessionmovein.cardHolderName.sendKeys("Barry Allen");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 7);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 5);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumber.sendKeys("1000000001");

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.agreeNo, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeYes, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeNo, 5, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(concessionmovein.agreeNo);

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
		String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 508 Burwood Highway Service Road Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Cooling NOT REQUIRED Non Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Jack Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Barry Allen Centrelink Health Care Card 1000000001 No Authorisation Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - trade waste
		// - discharge info
		// - property manager/letting agent
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 9, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 12, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 32, assertionErrorMsg(getLineNumber()));
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
		String sessionConcession = storage.getItemFromSessionStorage("move-in.concession_card");
		String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		String sessionSteps = storage.getItemFromSessionStorage("move-in.steps");
		String sessionAppId = storage.getItemFromSessionStorage("application_id");
		String sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcession), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSteps), assertionErrorMsg(getLineNumber()));
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
		String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
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
		softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 9, assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 12, assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 32, assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// let's confirm the keys in the local storage
		softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
		// verify the expected number of keys
		softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
		// let's confirm the values stored in the session storage are not empty
		sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
		sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
		sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
		sessionConcession = storage.getItemFromSessionStorage("move-in.concession_card");
		sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
		sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
		sessionSteps = storage.getItemFromSessionStorage("move-in.steps");
		sessionAppId = storage.getItemFromSessionStorage("application_id");
		sessionSourceId = storage.getItemFromSessionStorage("source_id");
		softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionConcession), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(StringUtils.isBlank(sessionSteps), assertionErrorMsg(getLineNumber()));
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
		this.sourceID = sessionSourceId;
		// let's confirm the values stored in the local storage
		localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
		softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// let's verify the Supply Details to confirm that the values are still there
		String ownerDate = getDisplayedValue(supplydetailsmovein.moveInDateOwner, true);
		String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, true);
		String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, true);
		String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, true);
		String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, true);
		String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, true);
		String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, true);
		String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, true);
		String state = getDisplayedValue(supplydetailsmovein.supplyAddState, true);
		String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, true);
		softAssertion.assertTrue(isElementDisplayed(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.owner, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(ownerDate, this.ownerMoveInDate, assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerSettleYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "508", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isMatPseudoChckbxTicked(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingNo, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(supplydetailsmovein.next);
		pauseSeleniumExecution(1000);

		// let's verify the Account Details to confirm that the values are still there
		softAssertion.assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);

		// let's verify the Main Account Contact to confirm that the values are still
		// there
		String firstName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
		String lastName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
		String emailAdd = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
		String mobPhone = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
		String busPhone = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
		String aHoursPhone = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(firstName, "Jack", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lastName, "Hanma", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertTrue(StringUtils.isBlank(emailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(busPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(aHoursPhone), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(mainaccountcontactmovein.next);
		pauseSeleniumExecution(1000);

		// let's verify the Concession to confirm that the values are still there
		String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, false);
		String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, false);
		String cardNum = getDisplayedValue(concessionmovein.cardNumber, false);
		softAssertion.assertTrue(isElementDisplayed(concessionmovein.cardHolderName, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardHolder, "Barry Allen", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardType, "Centrelink Health Care Card", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cardNum, "1000000001", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(concessionmovein.agreeNo, 0), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);

		// let's verify the Direct Debit to confirm that the values are still there
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.noDirectDebit, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(directdebitmovein.noDirectDebit, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(directdebitmovein.next);
		pauseSeleniumExecution(1000);

		// verify the values in the additional note and confirm it's still there
		String notes = getDisplayedValue(additionalnotemovein.notesArea, false);
		softAssertion.assertTrue(isElementDisplayed(additionalnotemovein.notesArea, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(notes), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(additionalnotemovein.next);
		pauseSeleniumExecution(1000);

		scrollPageDown(1500);
		// verify each section
		String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
		String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
		String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
		String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
		String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
		String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
		String concession = getDisplayedText(acceptancemovein.concessionRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Owner ", this.ownerMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 508 Burwood Highway Service Road Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Cooling NOT REQUIRED Non Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Jack Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(concession,
				"Concession update Barry Allen Centrelink Health Care Card 1000000001 No Authorisation Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - trade waste
		// - discharge info
		// - property manager/letting agent
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
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
//			softAssertion.assertEquals(actualSize, 0, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

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
		addProp("ResiNewContact12_ownerMoveInDate", this.ownerMoveInDate);
		addProp("ResiNewContact12_sourceID", this.sourceID);
		addProp("ResiNewContact12_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));

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
