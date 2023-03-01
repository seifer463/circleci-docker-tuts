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
import au.com.blueoak.portal.pageObjects.move_in.ConcessionMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.DirectDebitMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.MainAccountContactMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.PostalAddressMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.SupplyDetailsMoveIn;
import au.com.blueoak.portal.pageObjects.move_in.TradeWasteMoveIn;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;
import au.com.blueoak.portal.utility.BrowserLocalSessionStorage;

public class SmallBusNewContact04 extends MoveInDevBase {

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
	String tenantMoveInDate;

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

		// upload the correct portal_config.json we are testing
		uploadMoveInConfig(s3Access, "17\\", "portal_config.json");

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
			populate3rdPartyPrefill("506", "Burwood", StreetTypesEnum.HWY, "Vermont South", AustralianStatesEnum.VIC,
					"3133", AccountTypesEnum.SMALL_BUSINESS, AccountCategoryEnum.TENANT, null, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.LETTING_POOL.name(), "&tenancy_street_number=506",
					"&tenancy_street_name=Burwood", "&tenancy_street_type=", StreetTypesEnum.HWY.name().toUpperCase(),
					"&tenancy_suburb=Vermont South", "&tenancy_postcode=3133", "&tenancy_state=",
					AustralianStatesEnum.VIC.getLabelText().toUpperCase(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_cancelled_abn1"),
					"&business_trading_name=JaH Trading's", "&contact_first_name=Alan", "&contact_last_name=Feil",
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "config=portal_config.json",
					"&account_category=", AccountCategoryEnum.LETTING_POOL.name(), "&tenancy_street_number=506",
					"&tenancy_street_name=Burwood", "&tenancy_street_type=", StreetTypesEnum.HWY.name().toUpperCase(),
					"&tenancy_suburb=Vermont South", "&tenancy_postcode=3133", "&tenancy_state=",
					AustralianStatesEnum.VIC.getLabelText().toUpperCase(), "&account_type=",
					AccountTypesEnum.SMALL_BUSINESS.name(), "&business_number=", getProp("test_data_cancelled_abn1"),
					"&business_trading_name=JaH Trading's", "&contact_first_name=Alan", "&contact_last_name=Feil",
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
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.tenant);
			assertTrue(StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false)),
					"Move In Date is not empty/blank");
		}
		// let's put a valid lease commencement date as 4 days from future
		String future19Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 19, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future19Days, Keys.TAB);
		this.tenantMoveInDate = future19Days;
		// click the tenant option again to ensure that calendar is collapsed
		clickElementAction(supplydetailsmovein.tenant);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddStreetNum.sendKeys("506");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Burwood");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Highway", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Vermont South");
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
			supplydetailsmovein.supplyAddPostcode.sendKeys("3133");
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
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stNum, "506", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
		supplydetailsmovein.supplyAddTenancyType.sendKeys("Apartment", Keys.TAB);
		supplydetailsmovein.supplyAddTenancyNum.sendKeys("010");

		clickElementAction(supplydetailsmovein.lifeSupNo);
		clickElementAction(supplydetailsmovein.medCoolingYes);

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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the error displayed
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
					"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.commercial, 0),
					"We are no longer in the Account Details section");

			// verify fields in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the error displayed
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
					"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.abnOrAcn);
			deleteAllTextFromField();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		accountdetailsmovein.abnOrAcn.sendKeys(getProp("test_data_valid_abn3"), Keys.TAB);
		waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			accountdetailsmovein.tradingName.sendKeys("JaH Trading's");
		}
		softAssertion.assertEquals(
				getDisplayedValue(accountdetailsmovein.abnOrAcn, false), concatStrings(getProp("test_data_valid_abn3"),
						" (", getProp("test_data_valid_company_name_abn3_abn4"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, false), "JaH Trading's",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(accountdetailsmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(tradewastemovein.tradeWasteDischargeYes, 0),
				"We are not yet in the Trade Waste section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/**
	 * 
	 *  */
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

		clickElementAction(tradewastemovein.tradeWasteDischargeYes);
		pauseSeleniumExecution(500);
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

		clickElementAction(tradewastemovein.tradeWasteEquipNo);
		clickElementAction(tradewastemovein.businessActivity);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(tradewastemovein.businessActivityDiv, 3);
		chooseFromList(tradewastemovein.businessActivityDiv, 2);
		pauseSeleniumExecution(1000);
		// verify we chose the correct one
		String typeChosen = getDisplayedText(tradewastemovein.businessActivity, true);
		softAssertion.assertEquals(typeChosen, "Retail motor vehicle", assertionErrorMsg(getLineNumber()));

		tradewastemovein.maxDischargeVolume.sendKeys("0123...0096457", Keys.TAB);
		clickElementAction(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"));

		// upload trade waste files
		uploadTradeWasteFiles(ARTIFACTS_DIR, "Smaller file tiff file.tiff");
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
		// verify the files that were uploaded were only 1
		softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
				"cloud_upload Drag-and-drop file here or click to browse for file to upload Smaller file tiff file .tiff 0.6 MB File uploaded successfully",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(500);
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);
		// verify we are in the next section
		softAssertion.assertTrue(isElementDisplayed(mainaccountcontactmovein.firstName, 0),
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
			mainaccountcontactmovein.firstName.sendKeys("Alan");
			mainaccountcontactmovein.lastName.sendKeys("Feil");
		}
		mainaccountcontactmovein.businessPhone.sendKeys("0471073251");

		clickElementAction(mainaccountcontactmovein.addAnotherContact);
		pauseSeleniumExecution(1000);

		// did a loop to fix an issue where it's not clicked the first time
		// only happens when use_session_store == false
		int counter = 0;
		int maxRetry = 2;
		boolean isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
		while (!isDisplayed && counter < maxRetry) {
			clickElementAction(mainaccountcontactmovein.addAnotherContact);
			pauseSeleniumExecution(1000);
			isDisplayed = isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0);
			counter++;
		}

		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1FirstName, 0),
				"We are not yet in the 1st Additional Contact section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyMainContact" })
	public void verifyAdditionalContact() {

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
		// verify the remove additional contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
				assertionErrorMsg(getLineNumber()));
		// verify add another contact link is displayed
		softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
				assertionErrorMsg(getLineNumber()));
		additionalcontactmovein = new AdditionalContactMoveIn(driver, 0);
		// verify contact secret code not displayed
		softAssertion.assertFalse(isElementExists(additionalcontactmovein.addCont1ContactSecretCodeList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		additionalcontactmovein.addCont1FirstName.sendKeys("Catherine");
		additionalcontactmovein.addCont1LastName.sendKeys("Tripp");
		additionalcontactmovein.addCont1AfterhoursPhone.sendKeys("+61785215055");

		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1MarketingComPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the sections displayed
		List<String> actualSectionNames = getAllSectionNames(true);
		List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("create Supply Details",
				"create Account Details", "create Trade Waste", "create Main Account Contact (Alan Feil)",
				"5 Additional Contact (Catherine Tripp)", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		javaScriptClickElementAction(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal);
		softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
				assertionErrorMsg(getLineNumber()));
		// verify the sections displayed
		actualSectionNames = getAllSectionNames(true);
		expectedSectionNames = new ArrayList<>(
				Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
						"create Main Account Contact (Alan Feil)", "5 Additional Contact (Catherine Tripp)",
						"6 Postal Address", "7 Direct Debit", "8 Additional Note", "9 Acceptance"));
		logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
		softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(additionalcontactmovein.addCont1Next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(postaladdressmovein.sameSupAddressNo, 0),
				"We are not yet in the Postal Address section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6, dependsOnMethods = { "verifyAdditionalContact" })
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

		postaladdressmovein.addLine02.sendKeys("170 Mitcham Road");
		postaladdressmovein.city.sendKeys("Donvale");
		postaladdressmovein.state.sendKeys("Victoria");
		postaladdressmovein.postcode.sendKeys("3111");
		postaladdressmovein.country.sendKeys("Australia", Keys.TAB);

		String add01 = getDisplayedValue(postaladdressmovein.addLine01, false);
		String add02 = getDisplayedValue(postaladdressmovein.addLine02, false);
		String add03 = getDisplayedValue(postaladdressmovein.addLine03, false);
		String add04 = getDisplayedValue(postaladdressmovein.addLine04, false);
		String city = getDisplayedValue(postaladdressmovein.city, false);
		String state = getDisplayedValue(postaladdressmovein.state, false);
		String postcode = getDisplayedValue(postaladdressmovein.postcode, false);
		String country = getDisplayedValue(postaladdressmovein.country, false);
		softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "170 Mitcham Road", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		postaladdressmovein.addLine04.sendKeys("add#04");

		clickElementAction(postaladdressmovein.next);
		pauseSeleniumExecution(1000);
		softAssertion.assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0),
				"We are not yet in the Direct Debit section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
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
	@Test(priority = 8, dependsOnMethods = { "verifyDirectDebitDetails" })
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
		String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
		String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
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
				concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update Apartment 010, 506 Burwood Highway Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails,
				concatStrings("Account Details update Commercial Account ",
						getProp("test_data_valid_company_name_abn3_abn4"), " (JaH Trading's) ABN/ACN ",
						getProp("test_data_valid_abn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(tradeWaste,
				"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Retail motor vehicle'",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dischargeInfo,
				"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 123.01 Litres Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' Uploaded 1 site plan",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Alan Feil Business Phone: 0471073251",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Data, "Catherine Tripp A/Hours Phone: +61785215055",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContact1Notif,
				"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postAdd,
				"Postal Address update 170 Mitcham Road add#04 Donvale, Victoria, 3111 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - concession section
		// - property manager/ letting agent
		acceptancemovein = new AcceptanceMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
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

		// verify the fix for bug ticket BBPRTL-1488
		// let's confirm the keys in the session storage
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 4, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the session storage are not empty
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the fix for ticket BBPRTL-1843
			// verify that the data would not be cleared when refreshing the page
			// since the config use_session_store == false is being ignored here
			// because data is from 3rd party
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 15, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			// let's confirm the values stored in the local storage
			String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// we will mimic a refresh by accessing the embedded move in page
			// since the current url is the dev 3rd party prefill page
			// since there's still values in the session keys
			// it should populate the data correctly
			accessPortal(getEmbeddedUrlMoveIn(), true);
			loadEmbeddedMoveInPortal(true, true);

			storage = new BrowserLocalSessionStorage(driver);
			sessionKeys = storage.getAllKeysFromSessionStorage();
			sessionLength = storage.getSessionStorageLength();
			logDebugMessage(concatStrings("The value of sessionKeys ", sessionKeys.toString(), " and the size is <",
					String.valueOf(sessionLength), ">"));
			localKeys = storage.getAllKeysFromLocalStorage();
			localLength = storage.getLocalStorageLength();
			logDebugMessage(concatStrings("The value of localKeys ", localKeys.toString(), " and the size is <",
					String.valueOf(localLength), ">"));

			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-headless1"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 15, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify Supply Details section
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, false);
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.tenantMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "010", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "506", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);

			// verify Account Details section
			String company = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			String trading = getDisplayedValue(accountdetailsmovein.tradingName, false);
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(company,
					concatStrings(getProp("test_data_valid_abn3"), " (",
							getProp("test_data_valid_company_name_abn3_abn4"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(trading, "JaH Trading's", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(tradewastemovein.header);
			pauseSeleniumExecution(1000);

			// verify Trade Waste section
			String busAct = getDisplayedText(tradewastemovein.businessActivity, true);
			String flowRate = getDisplayedValue(tradewastemovein.maxFlowRate, false);
			String dischargeVol = getDisplayedValue(tradewastemovein.maxDischargeVolume, true);
			String dischargeDate = getDisplayedValue(tradewastemovein.dischargeStartDate, false);
			String startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, false);
			String startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, false);
			String endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, false);
			String endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, false);
			dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
			String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(busAct, "Retail motor vehicle", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(flowRate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeVol, "123.01", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(dischargeDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(startHour), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(startMin), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(endHour), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(endMin), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload Smaller file tiff file.tiff .image/tiff 0.6 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact Details
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Alan", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Feil", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
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
			softAssertion.assertTrue(StringUtils.isBlank(mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainBus, "0471073251", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
							"4 Main Account Contact (Alan Feil)", "create Additional Contact (Catherine Tripp)",
							"create Postal Address", "create Direct Debit", "create Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalcontactmovein.addCont1Header);
			pauseSeleniumExecution(1000);

			// verify Additional Contact section
			String addCont1FName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
			String addCont1LName = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
			String addCont1mainEmail = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false);
			String addCont1mainMob = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
			String addCont1mainBus = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
			String addCont1mainAfter = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
			softAssertion.assertEquals(addCont1FName, "Catherine", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1LName, "Tripp", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
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
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1mainAfter, "+61785215055", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(postaladdressmovein.header);
			pauseSeleniumExecution(1000);

			// verify the Postal Address section
			softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
					assertionErrorMsg(getLineNumber()));
			String add01 = getDisplayedValue(postaladdressmovein.addLine01, false);
			String add02 = getDisplayedValue(postaladdressmovein.addLine02, false);
			String add03 = getDisplayedValue(postaladdressmovein.addLine03, false);
			String add04 = getDisplayedValue(postaladdressmovein.addLine04, false);
			city = getDisplayedValue(postaladdressmovein.city, false);
			state = getDisplayedValue(postaladdressmovein.state, false);
			postcode = getDisplayedValue(postaladdressmovein.postcode, false);
			String country = getDisplayedValue(postaladdressmovein.country, false);
			softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add02, "170 Mitcham Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add04, "add#04", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);

			// verify Direct Debit section
			softAssertion.assertTrue(isElementTicked(directdebitmovein.noDirectDebit, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.header);
			waitForScreenToRender();

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(acceptancemovein.header);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
			String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
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
					concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update Apartment 010, 506 Burwood Highway Vermont South, Victoria, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails,
					concatStrings("Account Details update Commercial Account ",
							getProp("test_data_valid_company_name_abn3_abn4"), " (JaH Trading's) ABN/ACN ",
							getProp("test_data_valid_abn3")),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tradeWaste,
					"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Retail motor vehicle'",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeInfo,
					"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 123.01 Litres Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' Uploaded 1 site plan",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact, "Main Account Contact update Alan Feil Business Phone: 0471073251",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Data, "Catherine Tripp A/Hours Phone: +61785215055",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postAdd,
					"Postal Address update 170 Mitcham Road add#04 Donvale, Victoria, 3111 Australia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - concession section
			// - property manager/ letting agent
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the fix for ticket BBPRTL-1843
			// verify that the data would not be cleared when refreshing the page
			// since the config use_session_store == false is being ignored here
			// because data is from 3rd party
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 28, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			String sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			String sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
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
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			String sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				// we will mimic a refresh by accessing the standalone move in page
				// since the current url is the url prefill
				// since there's still values in the session keys
				// it should populate the data correctly
				accessPortal(getStandaloneUrlMoveIn(), true);
				loadStandaloneMoveInPortal(true);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
				// we will mimic a refresh by accessing the embedded move in page
				// since the current url is the url prefill
				// since there's still values in the session keys
				// it should populate the data correctly
				accessPortal(getEmbeddedUrlMoveIn(), true);
				loadEmbeddedMoveInPortal(true, true);
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

			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.trade_waste"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_trade_waste_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.additional_contact"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.postal_address"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("portalConfiguration"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.steps"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("application_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("source_id"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-account_category"),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-extra_data"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in-query-config"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 28, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("_grecaptcha"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionTradeWaste = storage.getItemFromSessionStorage("move-in.trade_waste");
			sessionTradeWasteFile = storage.getItemFromSessionStorage("move-in_trade_waste_file");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionAddContacts = storage.getItemFromSessionStorage("move-in.additional_contact");
			sessionPostalAdd = storage.getItemFromSessionStorage("move-in.postal_address");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWaste), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionTradeWasteFile), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAddContacts), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPostalAdd), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
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
			sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			sessionQueryConfig = storage.getItemFromSessionStorage("move-in-query-config");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryConfig), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			localGrecaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localGrecaptcha), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify Supply Details section
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDateTenant, false);
			String complexName = getDisplayedValue(supplydetailsmovein.supplyAddComplexName, false);
			String tenancyType = getDisplayedValue(supplydetailsmovein.supplyAddTenancyType, false);
			String tenancyNum = getDisplayedValue(supplydetailsmovein.supplyAddTenancyNum, false);
			String stNum = getDisplayedValue(supplydetailsmovein.supplyAddStreetNum, false);
			String stName = getDisplayedValue(supplydetailsmovein.supplyAddStreetName, false);
			String stType = getDisplayedValue(supplydetailsmovein.supplyAddStreetType, false);
			String city = getDisplayedValue(supplydetailsmovein.supplyAddCity, false);
			String state = getDisplayedValue(supplydetailsmovein.supplyAddState, false);
			String postcode = getDisplayedValue(supplydetailsmovein.supplyAddPostcode, false);
			String dragAndDropText = getDisplayedText(supplydetailsmovein.dragAndDropText, true);
			String lifeSupMedCoolingUploadArea = getDisplayedText(supplydetailsmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.tenantMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Apartment", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "010", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "506", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Highway", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement ",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);

			// verify Account Details section
			String company = getDisplayedValue(accountdetailsmovein.abnOrAcn, false);
			String trading = getDisplayedValue(accountdetailsmovein.tradingName, false);
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(company,
					concatStrings(getProp("test_data_valid_abn3"), " (",
							getProp("test_data_valid_company_name_abn3_abn4"), ")"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(trading, "JaH Trading's", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(tradewastemovein.header);
			pauseSeleniumExecution(1000);

			// verify Trade Waste section
			String busAct = getDisplayedText(tradewastemovein.businessActivity, true);
			String flowRate = getDisplayedValue(tradewastemovein.maxFlowRate, false);
			String dischargeVol = getDisplayedValue(tradewastemovein.maxDischargeVolume, true);
			String dischargeDate = getDisplayedValue(tradewastemovein.dischargeStartDate, false);
			String startHour = getDisplayedValue(tradewastemovein.dischargeHoursStartHour, false);
			String startMin = getDisplayedValue(tradewastemovein.dischargeHoursStartMin, false);
			String endHour = getDisplayedValue(tradewastemovein.dischargeHoursEndHour, false);
			String endMin = getDisplayedValue(tradewastemovein.dischargeHoursEndMin, false);
			dragAndDropText = getDisplayedText(tradewastemovein.dragAndDropText, true);
			String tradeWasteUploadArea = getDisplayedText(tradewastemovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteDischargeYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(tradewastemovein.tradeWasteEquipNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(busAct, "Retail motor vehicle", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(flowRate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeVol, "123.01", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(dischargeDate), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Su"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Mo"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Tu"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "We"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Th"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Fr"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isElementTicked(getMatPseudoChkbxElement(tradewastemovein.dischargeDaysOptions, false, "Sa"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(startHour), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(startMin), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(endHour), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(endMin), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", tradeWasteUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload Smaller file tiff file.tiff .image/tiff 0.6 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact Details
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Alan", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Feil", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
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
			softAssertion.assertTrue(StringUtils.isBlank(mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainBus, "0471073251", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("create Supply Details", "create Account Details", "create Trade Waste",
							"4 Main Account Contact (Alan Feil)", "create Additional Contact (Catherine Tripp)",
							"create Postal Address", "create Direct Debit", "create Additional Note", "9 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalcontactmovein.addCont1Header);
			pauseSeleniumExecution(1000);

			// verify Additional Contact section
			String addCont1FName = getDisplayedValue(additionalcontactmovein.addCont1FirstName, false);
			String addCont1LName = getDisplayedValue(additionalcontactmovein.addCont1LastName, false);
			String addCont1mainEmail = getDisplayedValue(additionalcontactmovein.addCont1EmailAddress, false);
			String addCont1mainMob = getDisplayedValue(additionalcontactmovein.addCont1MobilePhone, false);
			String addCont1mainBus = getDisplayedValue(additionalcontactmovein.addCont1BusinessPhone, false);
			String addCont1mainAfter = getDisplayedValue(additionalcontactmovein.addCont1AfterhoursPhone, false);
			softAssertion.assertEquals(addCont1FName, "Catherine", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1LName, "Tripp", assertionErrorMsg(getLineNumber()));
			// verify notification not ticked
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsPostal, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsEmail, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(additionalcontactmovein.addCont1BillsSMS, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(additionalcontactmovein.addCont1AcctnotifAndRemindersPostal, 0),
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
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainEmail), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainMob), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(addCont1mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addCont1mainAfter, "+61785215055", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1RemAdditionalContact, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementDisplayed(additionalcontactmovein.addCont1AddAnotherContact, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(postaladdressmovein.header);
			pauseSeleniumExecution(1000);

			// verify the Postal Address section
			softAssertion.assertTrue(isElementTicked(postaladdressmovein.sameSupAddressNo, 0),
					assertionErrorMsg(getLineNumber()));
			String add01 = getDisplayedValue(postaladdressmovein.addLine01, false);
			String add02 = getDisplayedValue(postaladdressmovein.addLine02, false);
			String add03 = getDisplayedValue(postaladdressmovein.addLine03, false);
			String add04 = getDisplayedValue(postaladdressmovein.addLine04, false);
			city = getDisplayedValue(postaladdressmovein.city, false);
			state = getDisplayedValue(postaladdressmovein.state, false);
			postcode = getDisplayedValue(postaladdressmovein.postcode, false);
			String country = getDisplayedValue(postaladdressmovein.country, false);
			softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add02, "170 Mitcham Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add04, "add#04", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);

			// verify Direct Debit section
			softAssertion.assertTrue(isElementTicked(directdebitmovein.noDirectDebit, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.header);
			waitForScreenToRender();

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(acceptancemovein.header);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String tradeWaste = getDisplayedText(acceptancemovein.tradeWasteRow, true);
			String dischargeInfo = getDisplayedText(acceptancemovein.dischargeInfoRow, true);
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
					concatStrings("Moving In update Moving in as Tenant ", this.tenantMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update Apartment 010, 506 Burwood Highway Vermont South, Victoria, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails,
					concatStrings("Account Details update Commercial Account ",
							getProp("test_data_valid_company_name_abn3_abn4"), " (JaH Trading's) ABN/ACN ",
							getProp("test_data_valid_abn3")),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tradeWaste,
					"Trade Waste update Will discharge trade waste No trade waste equipment installed Business activity is 'Retail motor vehicle'",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(dischargeInfo,
					"Discharge Information update Max instantaneous flow rate 'not known' Max daily discharge volume 123.01 Litres Discharge Start Date 'not known' Discharge Days 'not known' Discharge Hours 'not known' Uploaded 1 site plan",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact, "Main Account Contact update Alan Feil Business Phone: 0471073251",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1lbl, "Additional Contact 1", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1UpdLink, "update", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Data, "Catherine Tripp A/Hours Phone: +61785215055",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addContact1Notif,
					"Additional Contact 1 Notification update Bills (None) Notifications and Reminders (Postal) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postAdd,
					"Postal Address update 170 Mitcham Road add#04 Donvale, Victoria, 3111 Australia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit, "Direct Debit update None Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - concession section
			// - property manager/ letting agent
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.concessionRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.propManLettingAgentRowList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
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
//			softAssertion.assertEquals(actualSize, 3, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1500);
		// tick all 3 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		javaScriptClickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("SmallBusNewContact04_tenantMoveInDate", this.tenantMoveInDate);
		addProp("SmallBusNewContact04_sourceID", this.sourceID);
		addProp("SmallBusNewContact04_dateSubmittedSlash",
				getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH));

		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			logDebugMessage(concatStrings(
					"Will be deleting all attachments in the presign upload bucket before submitting the request for test class ",
					this.className));
			// delete all files in the presign upload bucket before submitting the request
			s3Access.deleteAllObjectsInDevPresignUploadBucket();
		}

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
