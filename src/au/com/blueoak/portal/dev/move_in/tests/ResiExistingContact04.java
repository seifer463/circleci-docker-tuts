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

public class ResiExistingContact04 extends MoveInDevBase {

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
	String propManMoveInDate;

	/** 
	 * 
	 * */
	String moveInDateUrlPrefill;

	/**
	 * 
	 *  */
	String concessionExpiry;

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
		uploadMoveInConfig(s3Access, "27\\", "portal_config.json");

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
			populate3rdPartyPrefill("520", "Burwood Highway Service", StreetTypesEnum.RD, "Vermont South",
					AustralianStatesEnum.VIC, "3133", AccountTypesEnum.RESIDENTIAL, AccountCategoryEnum.RUM, null,
					true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE,
					"config   	=  	portal_config.json   	", "  &		  account_category  =  	",
					AccountCategoryEnum.RUM.name(), "  	&  	move_in_date=  	 	", moveInDate,
					"	   	&  		tenancy_type  	   =   	", TenancyTypesEnum.House.name().toUpperCase(),
					"   &    	tenancy_number  =   	1328	 ",
					"  	  	&   	  	tenancy_street_number   	=  	520  	",
					"   &   		tenancy_street_name  	=  	Burwood Highway Service   	 	",
					"   	&  	 		tenancy_street_type  =		 ", StreetTypesEnum.RD.name(),
					"  &  		 tenancy_suburb  	=  	Vermont South   	",
					"  	  	&   		tenancy_postcode=3133", "  	&	tenancy_state 	 =   	",
					AustralianStatesEnum.VIC.name(), "&account_type   	=    	", AccountTypesEnum.RESIDENTIAL.name(),
					"   	&   		business_number  	= 		098465ABC   	  ",
					"   &   	business_trading_name   	=   	My Cloud		  ",
					"   	&  		contact_first_name  	=  	Rasha  		",
					"  	&   	contact_last_name  	=  	Ehsara   		",
					"   	&   	mobile_number 		=  		33333333",
					"	  &   	business_hour_phone		 = 		 44444444",
					"		   &	  after_hour_phone		 =		  55555555		  ",
					" 		  &email_address		=    	", getProp("test_dummy_email_lower_case"),
					"	   	&   	extra_data  	=  	  {\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}   		  ");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String moveInDate = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DB_DATE_FORMAT);
			this.moveInDateUrlPrefill = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3,
					DATE_MONTH_YEAR_FORMAT_SLASH);
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED,
					"config   	=  	portal_config.json   	", "  &		  account_category  =  	",
					AccountCategoryEnum.RUM.name(), "  	&  	move_in_date=  	 	", moveInDate,
					"	   	&  		tenancy_type  	   =   	", TenancyTypesEnum.House.name().toUpperCase(),
					"   &    	tenancy_number  =   	1328	 ",
					"  	  	&   	  	tenancy_street_number   	=  	520  	",
					"   &   		tenancy_street_name  	=  	Burwood Highway Service   	 	",
					"   	&  	 		tenancy_street_type  =		 ", StreetTypesEnum.RD.name(),
					"  &  		 tenancy_suburb  	=  	Vermont South   	",
					"  	  	&   		tenancy_postcode=3133", "  	&	tenancy_state 	 =   	",
					AustralianStatesEnum.VIC.name(), "&account_type   	=    	", AccountTypesEnum.RESIDENTIAL.name(),
					"   	&   		business_number  	= 		098465ABC   	  ",
					"   &   	business_trading_name   	=   	My Cloud		  ",
					"   	&  		contact_first_name  	=  	Rasha  		",
					"  	&   	contact_last_name  	=  	Ehsara   		",
					"   	&   	mobile_number 		=  		33333333",
					"	  &   	business_hour_phone		 = 		 44444444",
					"		   &	  after_hour_phone		 =		  55555555		  ",
					" 		  &email_address		=    	", getProp("test_dummy_email_lower_case"),
					"	   	&   	extra_data  	=  	  {\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}   		  ");
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false)),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			this.propManMoveInDate = this.moveInDateUrlPrefill;
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
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyConnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyDisconnected, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyUnknown, 5, 0),
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
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.lblSupplyConnectedHeaderList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementExists(supplydetailsmovein.lblSupplyConnectedQuestionList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.lblSupplyConnectedIntroList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
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
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.tenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.owner, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManager, 5, 0),
					assertionErrorMsg(getLineNumber()));
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(accountdetailsmovein.header);
			pauseSeleniumExecution(1000);
			// verify fields in error state
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.moveInDatePropMan, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.propManSettleNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.ownerPropManHolidayNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyType, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(supplydetailsmovein.supplyAddTenancyNum, 5, 0),
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
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.propManager);

			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(
					Arrays.asList("1 Supply Details", "2 Account Details", "3 Main Account Contact",
							"4 Manager/Agent Company Details", "5 Direct Debit", "6 Additional Note", "7 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's put a valid lease commencement date as 3 days from future
			String future3Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DATE_MONTH_YEAR_FORMAT_SLASH);
			this.propManMoveInDate = future3Days;
			clickElementAction(supplydetailsmovein.moveInDatePropMan);
			pauseSeleniumExecution(1000);
			supplydetailsmovein.moveInDatePropMan.sendKeys(future3Days, Keys.TAB);
			// click button again to collapse the calendar
			clickElementAction(supplydetailsmovein.propManager);
			clickElementAction(supplydetailsmovein.propManSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyConnected);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact", "4 Concession",
					"5 Manager/Agent Company Details", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// let's put a valid lease commencement date as 3 days from future
			String future3Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 3, DATE_MONTH_YEAR_FORMAT_SLASH);
			this.propManMoveInDate = future3Days;
			clickElementAction(supplydetailsmovein.moveInDatePropMan);
			pauseSeleniumExecution(1000);
			supplydetailsmovein.moveInDatePropMan.sendKeys(future3Days, Keys.TAB);
			// click button again to collapse the calendar
			clickElementAction(supplydetailsmovein.propManager);
			clickElementAction(supplydetailsmovein.propManSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyConnected);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the displayed sections
			List<String> actualSectionNames = getAllSectionNames(true);
			List<String> expectedSectionNames = new ArrayList<>(Arrays.asList("1 Supply Details",
					"create Account Details", "3 Main Account Contact (Rasha Ehsara)", "4 Concession",
					"5 Manager/Agent Company Details", "6 Direct Debit", "7 Additional Note", "8 Acceptance"));
			logDebugMessage(concatStrings("Expected displayed sections ", expectedSectionNames.toString()));
			softAssertion.assertEquals(actualSectionNames, expectedSectionNames, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(supplydetailsmovein.propManSettleYes);
			clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);
			clickElementAction(supplydetailsmovein.supplyConnected);
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			supplydetailsmovein.supplyAddStreetNum.sendKeys("520");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Burwood Highway Service");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
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
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(tenancyType), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));

			clickElementAction(supplydetailsmovein.supplyAddComplexName);
			deleteAllTextFromField();
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "House", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyNum, "1328", assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(stNum, "520", assertionErrorMsg(getLineNumber()));
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

		if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			clickElementAction(supplydetailsmovein.supplyAddTenancyType);
			deleteAllTextFromField();
			clickElementAction(supplydetailsmovein.supplyAddPostcode);
		}

		supplydetailsmovein.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		// verify that tenancy number is not editable
		softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
				assertionErrorMsg(getLineNumber()));
		pauseSeleniumExecution(600);
		supplydetailsmovein = new SupplyDetailsMoveIn(driver, 0);
		// verify that the spinner is not displayed since public holiday checking
		// is disabled in the portal config
		softAssertion.assertFalse(isElementExists(supplydetailsmovein.supplyAddStateSpinnerList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(supplydetailsmovein.lifeSupNo);
		clickElementAction(supplydetailsmovein.medCoolingYes);
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupNo, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.medCoolingYes, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(supplydetailsmovein.dragAndDropArea, 0, 3),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

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

			clickElementAction(accountdetailsmovein.residential);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify radio buttons not ticked
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementTicked(accountdetailsmovein.commercial, 0),
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
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.commercial);
			waitForAbnAcnToFinishSearch(PORTAL_ABN_ACN_SEARCH_TIMEOUT, "the ABN/ACN lookup is still searching");

			// verify fields in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify displayed values
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, false), "098465ABC",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, false), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify the error displayed
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
					"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);
			assertTrue(isElementDisplayed(accountdetailsmovein.residential, 0),
					"We are no longer in the Account Details section");

			// verify fields in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.abnOrAcn, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.tradingName, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify displayed values
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, false), "098465ABC",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, false), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify the error displayed
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnNotFound, true),
					"No matches found for provided ABN/ACN", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.dateOfBirth, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
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
			mainaccountcontactmovein.firstName.sendKeys("Rasha");
			mainaccountcontactmovein.lastName.sendKeys("Ehsara");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Rasha",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Ehsara",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.emailAddress, false),
					getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.mobilePhone, false), "33333333",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.businessPhone, false), "44444444",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false), "55555555",
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

		mainaccountcontactmovein.mobilePhone.sendKeys("+09198561256");
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
	@Test(priority = 4, dependsOnMethods = { "verifyMainContact" })
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
		// verify not in error state
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
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeYes, 5, 0), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(concessionmovein.agreeNo, 5, 0), assertionErrorMsg(getLineNumber()));
		// verify that the Concession Card Number and Concession Card Number Expiry is
		// initially not displayed
		concessionmovein = new ConcessionMoveIn(driver, 0);
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumberList), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(concessionmovein.cardNumExpiryList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

		concessionmovein.cardHolderName.sendKeys("Dr. Stephen Strange");
		clickElementAction(concessionmovein.typeOfConcessionCard);
		pauseSeleniumExecution(1000);
		verifyNumOfMatOptionValuesDisp(concessionmovein.typeOfConcessionCardDiv, 4);
		chooseFromList(concessionmovein.typeOfConcessionCardDiv, 1);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumber.sendKeys("0032168451200");
		int month = 5;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		clickElementAction(concessionmovein.cardNumExpiry);
		pauseSeleniumExecution(1000);
		concessionmovein.cardNumExpiry.sendKeys(monthStr, "/", expYrStr, Keys.TAB);
		clickElementAction(concessionmovein.lblAuthorisationForQuestion);
		pauseSeleniumExecution(1000);
		String concessionExp = getDisplayedValue(concessionmovein.cardNumExpiry, true);
		this.concessionExpiry = concessionExp;
		clickElementAction(concessionmovein.agreeNo);

		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are in the Manager/Agent Company Details section
		softAssertion.assertTrue(isElementDisplayed(managerholidaylettingmovein.companyName, 0),
				"We are not in the Manager/Agent Company Details section");
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5, dependsOnMethods = { "verifyConcessionDetails" })
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

		// verify not required
		clickElementAction(managerholidaylettingmovein.next);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(directdebitmovein.bankAccount, 0), "We are not yet in the Direct Debit section");

		// go back
		clickElementAction(directdebitmovein.previous);
		pauseSeleniumExecution(1000);
		assertTrue(isElementDisplayed(managerholidaylettingmovein.companyContactNum, 0),
				"We are not in the Manager/Agent Company Details section");

		// verify the intro message
		introMsg = getDisplayedText(managerholidaylettingmovein.lblPropManHolidayLettingIntro, true);
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

		managerholidaylettingmovein.companyName.sendKeys("The Company Name");
		managerholidaylettingmovein.companyContactNum.sendKeys("The Company Contact Number");
		managerholidaylettingmovein.address02.sendKeys("180 Mitcham Road");
		managerholidaylettingmovein.city.sendKeys("Donvale");
		managerholidaylettingmovein.state.sendKeys("Victoria");
		managerholidaylettingmovein.postCode.sendKeys("3111");
		managerholidaylettingmovein.country.sendKeys("Australia");

		String add01 = getDisplayedValue(managerholidaylettingmovein.address01, false);
		String add02 = getDisplayedValue(managerholidaylettingmovein.address02, false);
		String add03 = getDisplayedValue(managerholidaylettingmovein.address03, false);
		String add04 = getDisplayedValue(managerholidaylettingmovein.address04, false);
		String city = getDisplayedValue(managerholidaylettingmovein.city, false);
		String state = getDisplayedValue(managerholidaylettingmovein.state, false);
		String postcode = getDisplayedValue(managerholidaylettingmovein.postCode, false);
		String country = getDisplayedValue(managerholidaylettingmovein.country, false);
		softAssertion.assertTrue(StringUtils.isBlank(add01), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(add02, "180 Mitcham Road", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add03), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(add04), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		managerholidaylettingmovein.address01.sendKeys("Add & 01");
		managerholidaylettingmovein.address03.sendKeys("Add & 03");
		managerholidaylettingmovein.address04.sendKeys("Add & 04");

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
	@Test(priority = 6, dependsOnMethods = { "verifyManagerAgentDetails" })
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
		// verify in error state
		softAssertion.assertTrue(isElementInError(directdebitmovein.bankAccount, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.creditCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(isElementInError(directdebitmovein.noDirectDebit, 5, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(directdebitmovein.bankAccount);
		directdebitmovein.bankAccountName.sendKeys("Dr. Stephen Strange");
		directdebitmovein.accountBSB.sendKeys("003281");
		directdebitmovein.accountNumber.sendKeys("00129845710651");

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
		String propManLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
		String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
		String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
		softAssertion.assertEquals(movingIn,
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 520 Burwood Highway Service Road Vermont South, Victoria, 3133 Service currently connected",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Rasha Ehsara Mobile Phone: +09198561256",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion
				.assertEquals(concession,
						concatStrings("Concession update Dr. Stephen Strange Pensioner Card Centrelink 0032168451200",
								" (", this.concessionExpiry, ") No Authorisation Provided"),
						assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(propManLetting,
				"Property Manager / Letting Agent update The Company Name The Company Contact Number Add & 01 180 Mitcham Road Add & 03 Add & 04 Donvale, Victoria, 3111 Australia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				"Direct Debit update Bank Account Account Name: Dr Stephen Strange BSB: 003281 / Num: 0012984571",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addNote, "Additional Note update None Specified",
				assertionErrorMsg(getLineNumber()));
		// verify the following are not displayed
		// - trade waste
		// - discharge info
		// - postal address
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
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(sessionAppId, "move-in", assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the fix for ticket BBPRTL-1843
			// verify that the data would not be cleared when refreshing the page
			// since the config use_session_store == false is being ignored here
			// because data is from 3rd party
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 13, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 13, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
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
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryHeadless), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify the Supply Details section
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false);
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
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.propManMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.supplyConnected, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "520", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
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
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Account Details
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Rasha", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Ehsara", assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(mainMob, "+09198561256", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);

			// verify Concession section
			String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
			String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
			String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
			String cardExpiry = getDisplayedValue(concessionmovein.cardNumExpiry, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Dr. Stephen Strange", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "Pensioner Card Centrelink", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, "0032168451200", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardExpiry, this.concessionExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(concessionmovein.agreeNo, 0), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(concessionmovein.next);
			pauseSeleniumExecution(1000);

			// verify Manager/Agent Company details
			String companyName = getDisplayedValue(managerholidaylettingmovein.companyName, false);
			String companyContactNum = getDisplayedValue(managerholidaylettingmovein.companyContactNum, false);
			String add01 = getDisplayedValue(managerholidaylettingmovein.address01, false);
			String add02 = getDisplayedValue(managerholidaylettingmovein.address02, false);
			String add03 = getDisplayedValue(managerholidaylettingmovein.address03, false);
			String add04 = getDisplayedValue(managerholidaylettingmovein.address04, false);
			city = getDisplayedValue(managerholidaylettingmovein.city, false);
			state = getDisplayedValue(managerholidaylettingmovein.state, false);
			postcode = getDisplayedValue(managerholidaylettingmovein.postCode, false);
			String country = getDisplayedValue(managerholidaylettingmovein.country, false);
			softAssertion.assertEquals(companyName, "The Company Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(companyContactNum, "The Company Contact Number",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add01, "Add & 01", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add02, "180 Mitcham Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add03, "Add & 03", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add04, "Add & 04", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(managerholidaylettingmovein.next);
			pauseSeleniumExecution(1000);

			// verify Direct Debit details
			softAssertion.assertTrue(isElementTicked(directdebitmovein.bankAccount, 0),
					assertionErrorMsg(getLineNumber()));
			String bankAccountName = getDisplayedValue(directdebitmovein.bankAccountName, false);
			String accountBsb = getDisplayedValue(directdebitmovein.accountBSB, false);
			String accountNum = getDisplayedValue(directdebitmovein.accountNumber, false);
			softAssertion.assertEquals(bankAccountName, "Dr Stephen Strange", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(accountBsb, "003281", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(accountNum, "0012984571", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.next);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
			String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
			String concession = getDisplayedText(acceptancemovein.concessionRow, true);
			String propManLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
							this.propManMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update 520 Burwood Highway Service Road Vermont South, Victoria, 3133 Service currently connected",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					"Main Account Contact update Rasha Ehsara Mobile Phone: +09198561256",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					concatStrings("Concession update Dr. Stephen Strange Pensioner Card Centrelink 0032168451200", " (",
							this.concessionExpiry, ") No Authorisation Provided"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(propManLetting,
					"Property Manager / Letting Agent update The Company Name The Company Contact Number Add & 01 180 Mitcham Road Add & 03 Add & 04 Donvale, Victoria, 3111 Australia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit,
					"Direct Debit update Bank Account Account Name: Dr Stephen Strange BSB: 003281 / Num: 0012984571",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - trade waste
			// - discharge info
			// - postal address
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 33, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			String sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
			String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
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
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
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
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.letting_agent"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.direct_debit"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("readOnlyIfThirdParty"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.extra_data"), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(sessionLength, 33, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 1, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			sessionLettingAgent = storage.getItemFromSessionStorage("move-in.letting_agent");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLettingAgent), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
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
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			// verify the Supply Details section
			String moveInDate = getDisplayedValue(supplydetailsmovein.moveInDatePropMan, false);
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
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManager, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(moveInDate, this.propManMoveInDate, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.propManSettleYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.ownerPropManHolidayNo, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.supplyConnected, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "520", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
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
			clickElementAction(supplydetailsmovein.next);
			pauseSeleniumExecution(1000);

			// verify Account Details
			softAssertion.assertTrue(isElementTicked(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.header);
			pauseSeleniumExecution(1000);

			// verify Main Account Contact
			String mainFName = getDisplayedValue(mainaccountcontactmovein.firstName, false);
			String mainLName = getDisplayedValue(mainaccountcontactmovein.lastName, false);
			String mainEmail = getDisplayedValue(mainaccountcontactmovein.emailAddress, false);
			String mainMob = getDisplayedValue(mainaccountcontactmovein.mobilePhone, false);
			String mainBus = getDisplayedValue(mainaccountcontactmovein.businessPhone, false);
			String mainAfter = getDisplayedValue(mainaccountcontactmovein.afterhoursPhone, false);
			softAssertion.assertEquals(mainFName, "Rasha", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Ehsara", assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(mainMob, "+09198561256", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainBus), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(mainAfter), assertionErrorMsg(getLineNumber()));
			mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.addAnotherContactList),
					assertionErrorMsg(getLineNumber()));
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(mainaccountcontactmovein.next);
			pauseSeleniumExecution(1000);

			// verify Concession section
			String cardHolder = getDisplayedValue(concessionmovein.cardHolderName, true);
			String cardType = getDisplayedText(concessionmovein.typeOfConcessionCard, true);
			String cardNum = getDisplayedValue(concessionmovein.cardNumber, true);
			String cardExpiry = getDisplayedValue(concessionmovein.cardNumExpiry, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Dr. Stephen Strange", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "Pensioner Card Centrelink", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, "0032168451200", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardExpiry, this.concessionExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(concessionmovein.agreeNo, 0), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(concessionmovein.next);
			pauseSeleniumExecution(1000);

			// verify Manager/Agent Company details
			String companyName = getDisplayedValue(managerholidaylettingmovein.companyName, false);
			String companyContactNum = getDisplayedValue(managerholidaylettingmovein.companyContactNum, false);
			String add01 = getDisplayedValue(managerholidaylettingmovein.address01, false);
			String add02 = getDisplayedValue(managerholidaylettingmovein.address02, false);
			String add03 = getDisplayedValue(managerholidaylettingmovein.address03, false);
			String add04 = getDisplayedValue(managerholidaylettingmovein.address04, false);
			city = getDisplayedValue(managerholidaylettingmovein.city, false);
			state = getDisplayedValue(managerholidaylettingmovein.state, false);
			postcode = getDisplayedValue(managerholidaylettingmovein.postCode, false);
			String country = getDisplayedValue(managerholidaylettingmovein.country, false);
			softAssertion.assertEquals(companyName, "The Company Name", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(companyContactNum, "The Company Contact Number",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add01, "Add & 01", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add02, "180 Mitcham Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add03, "Add & 03", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(add04, "Add & 04", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Donvale", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Victoria", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3111", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(country, "Australia", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(managerholidaylettingmovein.next);
			pauseSeleniumExecution(1000);

			// verify Direct Debit details
			softAssertion.assertTrue(isElementTicked(directdebitmovein.bankAccount, 0),
					assertionErrorMsg(getLineNumber()));
			String bankAccountName = getDisplayedValue(directdebitmovein.bankAccountName, false);
			String accountBsb = getDisplayedValue(directdebitmovein.accountBSB, false);
			String accountNum = getDisplayedValue(directdebitmovein.accountNumber, false);
			softAssertion.assertEquals(bankAccountName, "Dr Stephen Strange", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(accountBsb, "003281", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(accountNum, "0012984571", assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.next);
			pauseSeleniumExecution(1000);

			// verify Additional Note
			String notesArea = getDisplayedValue(additionalnotemovein.notesArea, false);
			softAssertion.assertTrue(StringUtils.isBlank(notesArea), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(additionalnotemovein.next);
			pauseSeleniumExecution(1000);

			// verify each section again
			String movingIn = getDisplayedText(acceptancemovein.movingInRow, true);
			String servAdd = getDisplayedText(acceptancemovein.serviceAddressRow, true);
			String lifeSupport = getDisplayedText(acceptancemovein.lifeSupportRow, true);
			String acctDetails = getDisplayedText(acceptancemovein.accountDetailsRow, true);
			String mainContact = getDisplayedText(acceptancemovein.mainContactRow, true);
			String mainContactNotif = getDisplayedText(acceptancemovein.mainContactNotifRow, true);
			String concession = getDisplayedText(acceptancemovein.concessionRow, true);
			String propManLetting = getDisplayedText(acceptancemovein.propManLettingAgentRow, true);
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
							this.propManMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update 520 Burwood Highway Service Road Vermont South, Victoria, 3133 Service currently connected",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life Support NOT REQUIRED Medical Cooling Required Non Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					"Main Account Contact update Rasha Ehsara Mobile Phone: +09198561256",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					concatStrings("Concession update Dr. Stephen Strange Pensioner Card Centrelink 0032168451200", " (",
							this.concessionExpiry, ") No Authorisation Provided"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(propManLetting,
					"Property Manager / Letting Agent update The Company Name The Company Contact Number Add & 01 180 Mitcham Road Add & 03 Add & 04 Donvale, Victoria, 3111 Australia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(directDebit,
					"Direct Debit update Bank Account Account Name: Dr Stephen Strange BSB: 003281 / Num: 0012984571",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(addNote, "Additional Note update None Specified",
					assertionErrorMsg(getLineNumber()));
			// verify the following are not displayed
			// - trade waste
			// - discharge info
			// - postal address
			acceptancemovein = new AcceptanceMoveIn(driver, 0);
			softAssertion.assertFalse(isElementExists(acceptancemovein.tradeWasteRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.dischargeInfoRowList),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementExists(acceptancemovein.postalAddressRowList),
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
//			softAssertion.assertEquals(actualSize, 28, "Incorrect number of objects inside the bucket '"
//					.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
		}
		// verify all assertions
		softAssertion.assertAll();

		scrollPageDown(1000);
		// tick all 3 checkboxes
		javaScriptClickElementAction(acceptancemovein.firstCheckbox);
		javaScriptClickElementAction(acceptancemovein.secondCheckbox);
		javaScriptClickElementAction(acceptancemovein.thirdCheckbox);

		// add the property files before submitting the request
		addProp("ResiNewContact15_propManMoveInDate", this.propManMoveInDate);
		addProp("ResiNewContact15_concessionExpiry", this.concessionExpiry);
		addProp("ResiNewContact15_sourceID", this.sourceID);
		addProp("ResiNewContact15_dateSubmittedSlash",
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
