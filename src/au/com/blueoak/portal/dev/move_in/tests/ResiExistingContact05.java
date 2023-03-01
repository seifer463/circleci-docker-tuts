package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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

public class ResiExistingContact05 extends MoveInDevBase {

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
		uploadMoveInConfig(s3Access, "24\\", "portal_config.json");

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
			populate3rdPartyPrefill(null, null, null, null, null, null, AccountTypesEnum.RESIDENTIAL,
					AccountCategoryEnum.TENANT, null, true);
		} else if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.STANDALONE, "business_number=",
					getProp("test_data_valid_acn2"), "&business_trading_name=My Cloud", "&contact_first_name=Sanjeev",
					"&contact_last_name=Dhaka", "&account_category=", AccountCategoryEnum.TENANT.name(),
					"&extra_data={\"Community\":\"Arc By Crown\",\"CTS\":\"\",\"Service Fee\":\" \",\"Electricity\":{\"Elecrate\":\"22.37 c/kWh (inc GST)\",\"ElecSupply\":\"88 c/day (inc GST)\",\"ElecCom\":\"\"},\"Cooktop\":{\"Gasrate\":\"27.5 c/day (inc GST)\",\"GasComments\":\"\"},\"HW\":{\"HWrate\":\"0.176 c/ltr (inc GST)\",\"HWSupply\":\"49.5 c/day (inc GST)\",\"ThermalCom\":\"\"}}");
			accessPortal(urlPrefill, true);
			loadStandaloneMoveInPortal(false);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			String urlPrefill = constructUrlPrefill(PortalTypesEnum.EMBEDDED, "business_number=",
					getProp("test_data_cancelled_abn2"), "&business_trading_name=My Cloud",
					"&contact_first_name=Sanjeev", "&contact_last_name=Dhaka", "&account_category=",
					AccountCategoryEnum.TENANT.name(),
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

		// verify the radio buttons are not selected
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertFalse(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.tenant, 0),
					assertionErrorMsg(getLineNumber()));
			// verify the fix for bug ticket BBPRTL-1610
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(
					StringUtils.isBlank(getDisplayedValue(supplydetailsmovein.moveInDateTenant, false)),
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
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			clickElementAction(supplydetailsmovein.tenant);
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
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
			// verify fields are in read only
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
			// verify all assertions
			softAssertion.assertAll();

			if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
				clickElementAction(supplydetailsmovein.next);
				pauseSeleniumExecution(1000);
			} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
					|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
				clickElementAction(accountdetailsmovein.header);
				pauseSeleniumExecution(1000);
			}
			// verify fields in error state
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.moveInDateTenant, 5, 0),
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
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupYes, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(supplydetailsmovein.lifeSupNo, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's put a valid lease commencement date as 2 days from future
		String future2Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 2, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDateTenant);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDateTenant.sendKeys(future2Days, Keys.TAB);
		// click button again to collapse the calendar
		clickElementAction(supplydetailsmovein.tenant);

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			supplydetailsmovein.supplyAddStreetNum.sendKeys("510");
			supplydetailsmovein.supplyAddStreetName.sendKeys("Burwood Highway Service");
			supplydetailsmovein.supplyAddStreetType.sendKeys("Road", Keys.TAB);
			supplydetailsmovein.supplyAddCity.sendKeys("Vermont South");
			supplydetailsmovein.supplyAddPostcode.sendKeys("3133");
			supplydetailsmovein.supplyAddState.sendKeys("Victoria", Keys.TAB);
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
		softAssertion.assertEquals(stNum, "510", assertionErrorMsg(getLineNumber()));
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

		supplydetailsmovein.supplyAddTenancyType.sendKeys("Not applicable", Keys.TAB);
		// verify that tenancy number is not editable
		softAssertion.assertFalse(isElementEnabled(supplydetailsmovein.supplyAddTenancyNum, 0),
				assertionErrorMsg(getLineNumber()));
		supplydetailsmovein.supplyAddState.clear();
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
		softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		clickElementAction(
				getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"));

		// let's click the life support again to fix an issue
		// where the hidden upload button is not clicked the first time
		clickElementAction(supplydetailsmovein.lifeSupYes);
		// upload life support and medical cooling files
		uploadLifeSupMedCoolingFiles(ARTIFACTS_DIR, "g'alaxy-'wallpaper.jpeg");
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
				"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper .jpeg 0.4 MB File uploaded successfully",
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
			// verify fields not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
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
			// verify fields not editable
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.residential, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementEnabled(accountdetailsmovein.commercial, 0),
					assertionErrorMsg(getLineNumber()));
			// verify not in error state
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isElementInError(accountdetailsmovein.commercial, 5, 0),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();

			clickElementAction(accountdetailsmovein.next);
			pauseSeleniumExecution(1000);
			// verify in error state
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.residential, 5, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementInError(accountdetailsmovein.commercial, 5, 0),
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
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.abnOrAcn, false),
					getProp("test_data_cancelled_abn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(accountdetailsmovein.tradingName, false), "My Cloud",
					assertionErrorMsg(getLineNumber()));
			// verify the error displayed
			softAssertion.assertEquals(getDisplayedText(accountdetailsmovein.hintAbnAcnCancelled, true),
					"Provided ABN/ACN is currently not active", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.dateOfBirth, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.driversLicence, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.passport, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.medicareCard, 5, 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementInError(mainaccountcontactmovein.provideNone, 5, 0),
				assertionErrorMsg(getLineNumber()));
		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
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
		// verify all assertions
		softAssertion.assertAll();

		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			mainaccountcontactmovein.firstName.sendKeys("Sanjeev");
			mainaccountcontactmovein.lastName.sendKeys("Dhaka");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.firstName, false), "Sanjeev",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(getDisplayedValue(mainaccountcontactmovein.lastName, false), "Dhaka",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

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

		clickElementAction(mainaccountcontactmovein.medicareCard);
		mainaccountcontactmovein.medicareCardNumber.sendKeys("2428778132");
		// initial click to resolve ElementNotInteractableException exception
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		int month = 9;
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String monthStr = Integer.toString(month);
		String expYrStr = Integer.toString(expYr);
		String medExp = concatStrings(monthStr, "/", expYrStr);
		// click to fix ElementNotInteractableException
		clickElementAction(mainaccountcontactmovein.medicareCardExpiry);
		pauseSeleniumExecution(1000);
		slowSendKeys(mainaccountcontactmovein.medicareCardExpiry, medExp, true, 250);
		// ensure that the calendar is collapsed
		clickElementAction(mainaccountcontactmovein.afterhoursPhone);
		mainaccountcontactmovein.mobilePhone.sendKeys("0352537499");

		// update the supply details as Property Manager
		scrollPageUp(400);
		clickElementAction(supplydetailsmovein.header);
		pauseSeleniumExecution(1000);
		clickElementAction(supplydetailsmovein.propManager);
		// let's put a valid lease commencement date as 14 days from future
		String future14Days = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, 14, DATE_MONTH_YEAR_FORMAT_SLASH);
		clickElementAction(supplydetailsmovein.moveInDatePropMan);
		pauseSeleniumExecution(1000);
		supplydetailsmovein.moveInDatePropMan.sendKeys(future14Days, Keys.TAB);
		this.propManMoveInDate = future14Days;
		// initial click on the same field to dismiss the calendar
		// because the Keys.TAB on selenium did not dismiss the calendar
		clickElementAction(supplydetailsmovein.propManSettleYes);
		clickElementAction(supplydetailsmovein.propManSettleYes);
		clickElementAction(supplydetailsmovein.ownerPropManHolidayNo);

		scrollPageDown(500);
		// go back to the Main Account Contact details
		clickElementAction(mainaccountcontactmovein.header);
		pauseSeleniumExecution(1000);

		mainaccountcontactmovein = new MainAccountContactMoveIn(driver, 0);
		// verify Date of Birth not displayed
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.dateOfBirth, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
		// verify personal identification not displayed
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.lblPersonalIDHeader, 0),
				assertionErrorMsg(getLineNumber()));
		// verify medicare fields are not displayed
		setImplicitWait(0);
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.medicareCardNumber, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
		softAssertion.assertFalse(isElementDisplayed(mainaccountcontactmovein.medicareCardExpiry, 0),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(0);
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.driversLicenceList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.passportList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.medicareCardList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(mainaccountcontactmovein.provideNoneList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();

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
		clickElementAction(concessionmovein.next);
		pauseSeleniumExecution(1000);
		// verify we are still in the Concession section
		assertTrue(isElementDisplayed(concessionmovein.addConcessionYes, 0),
				"We are no longer in the Concession section");

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
		int month = 5;
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
	@Test(priority = 5, dependsOnMethods = { "verifyConcessionDetails" })
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
	@Test(priority = 6, dependsOnMethods = { "verifyDirectDebitDetails" })
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
				concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
						this.propManMoveInDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(servAdd,
				"Service Address update 510 Burwood Highway Service Road Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport,
				"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContact, "Main Account Contact update Sanjeev Dhaka Mobile Phone: 0352537499",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mainContactNotif,
				"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				concession, concatStrings("Concession update Peter Parker Queensland Seniors Card ",
						getProp("test_data_14"), " (", this.concessionExpiry, ") Concession Card Uploaded"),
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
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7, dependsOnMethods = { "verifyAcceptanceDetails" })
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
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
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
			String localCaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localCaptcha), assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			// verify the fix for ticket BBPRTL-1843
			// verify that the data would not be cleared when refreshing the page
			// since the config use_session_store == false is being ignored here
			// because data is from 3rd party
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
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
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			String sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
			String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
					assertionErrorMsg(getLineNumber()));
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
			String localCaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localCaptcha), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
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
			softAssertion.assertEquals(sessionLength, 14, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionQueryHeadless = storage.getItemFromSessionStorage("move-in-query-headless1");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
					assertionErrorMsg(getLineNumber()));
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
			localCaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localCaptcha), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "510", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Intermittent Peritoneal Dialysis Machine"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					0), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Ventilator for Life Support"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB File uploaded successfully",
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
			softAssertion.assertEquals(mainFName, "Sanjeev", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Dhaka", assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(mainMob, "0352537499", assertionErrorMsg(getLineNumber()));
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
			dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
			String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Peter Parker", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "Queensland Seniors Card", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, getProp("test_data_14"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardExpiry, this.concessionExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);

			// verify Direct Debit details
			softAssertion.assertTrue(isElementTicked(directdebitmovein.noDirectDebit, 0),
					assertionErrorMsg(getLineNumber()));
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
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
							this.propManMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update 510 Burwood Highway Service Road Vermont South, Queensland, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					"Main Account Contact update Sanjeev Dhaka Mobile Phone: 0352537499",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					concatStrings("Concession update Peter Parker Queensland Seniors Card ", getProp("test_data_14"),
							" (", this.concessionExpiry, ") Concession Card Uploaded"),
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
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			// verify the fix for ticket BBPRTL-1843
			// verify that the data would not be cleared when refreshing the page
			// since the config use_session_store == false is being ignored here
			// because data is from 3rd party
			softAssertion.assertTrue(sessionKeys.contains("move-in.supply_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
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
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			String sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			String sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			String sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			String sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			String sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			String sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
			String sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			String sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			String sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			String sessionAppId = storage.getItemFromSessionStorage("application_id");
			String sessionSourceId = storage.getItemFromSessionStorage("source_id");
			String sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			String sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			String sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			String sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			String sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			String sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			String sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			String sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			String localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			String localCaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localCaptcha), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(sessionKeys.contains("move-in_life_support_file"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.account_details"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.main_contact"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in.concession_card"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(sessionKeys.contains("move-in_concession_file"),
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
			// verify the expected number of keys
			softAssertion.assertEquals(sessionLength, 19, assertionErrorMsg(getLineNumber()));
			// let's confirm the keys in the local storage
			softAssertion.assertTrue(localKeys.contains("raygun4js-userid"), assertionErrorMsg(getLineNumber()));
			// verify the expected number of keys
			softAssertion.assertEquals(localLength, 2, assertionErrorMsg(getLineNumber()));
			sessionSupplyDetails = storage.getItemFromSessionStorage("move-in.supply_details");
			sessionLifeSupportAttachedFiles = storage.getItemFromSessionStorage("move-in_life_support_file");
			sessionAccountDetails = storage.getItemFromSessionStorage("move-in.account_details");
			sessionMainContact = storage.getItemFromSessionStorage("move-in.main_contact");
			sessionConcessionDetails = storage.getItemFromSessionStorage("move-in.concession_card");
			sessionConcessionAttachedFiles = storage.getItemFromSessionStorage("move-in_concession_file");
			sessionDirectDebit = storage.getItemFromSessionStorage("move-in.direct_debit");
			sessionPortalConfig = storage.getItemFromSessionStorage("portalConfiguration");
			sessionMoveInSteps = storage.getItemFromSessionStorage("move-in.steps");
			sessionAppId = storage.getItemFromSessionStorage("application_id");
			sessionSourceId = storage.getItemFromSessionStorage("source_id");
			sessionReadOnly3rdParty = storage.getItemFromSessionStorage("readOnlyIfThirdParty");
			sessionExtraData = storage.getItemFromSessionStorage("move-in.extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionSupplyDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionLifeSupportAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAccountDetails), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMainContact), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionDetails),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionConcessionAttachedFiles),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionDirectDebit), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionPortalConfig), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionMoveInSteps), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionAppId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionReadOnly3rdParty), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionExtraData), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionSourceId), assertionErrorMsg(getLineNumber()));
			sessionQueryAcctCategory = storage.getItemFromSessionStorage("move-in-query-account_category");
			sessionQueryAbnAcn = storage.getItemFromSessionStorage("move-in-query-business_number");
			sessionQueryTradingName = storage.getItemFromSessionStorage("move-in-query-business_trading_name");
			sessionQueryFirstName = storage.getItemFromSessionStorage("move-in-query-contact_first_name");
			sessionQueryLastName = storage.getItemFromSessionStorage("move-in-query-contact_last_name");
			sessionQueryExtraData = storage.getItemFromSessionStorage("move-in-query-extra_data");
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAcctCategory),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryAbnAcn), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryTradingName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryFirstName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryLastName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(sessionQueryExtraData), assertionErrorMsg(getLineNumber()));
			this.sourceID = sessionSourceId;
			// let's confirm the values stored in the local storage
			localRaygunUserId = storage.getItemFromLocalStorage("raygun4js-userid");
			localCaptcha = storage.getItemFromLocalStorage("_grecaptcha");
			softAssertion.assertFalse(StringUtils.isBlank(localRaygunUserId), assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(StringUtils.isBlank(localCaptcha), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertTrue(StringUtils.isBlank(complexName), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(tenancyType, "Not applicable", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(tenancyNum), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stNum, "510", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stName, "Burwood Highway Service", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(stType, "Road", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(city, "Vermont South", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(state, "Queensland", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(postcode, "3133", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isElementTicked(supplydetailsmovein.lifeSupYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Oxygen Concentrator"),
					0), assertionErrorMsg(getLineNumber()));
			softAssertion
					.assertFalse(
							isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions,
									false, "Intermittent Peritoneal Dialysis Machine"), 0),
							assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(isMatPseudoChckbxTicked(
					getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Haemodialysis Machine"),
					0), assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false,
							"Ventilator for Life Support"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertFalse(
					isMatPseudoChckbxTicked(
							getMatPseudoChkbxElement(supplydetailsmovein.lifeSuppEquipOptions, false, "Other"), 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", lifeSupMedCoolingUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload medical certificate associated with your life support requirement g'alaxy-'wallpaper.jpeg .image/jpeg 0.4 MB File uploaded successfully",
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
			softAssertion.assertEquals(mainFName, "Sanjeev", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainLName, "Dhaka", assertionErrorMsg(getLineNumber()));
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
			softAssertion.assertEquals(mainMob, "0352537499", assertionErrorMsg(getLineNumber()));
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
			dragAndDropText = getDisplayedText(concessionmovein.dragAndDropText, true);
			String concessionUploadArea = getDisplayedText(concessionmovein.dragAndDropArea, true);
			softAssertion.assertTrue(isElementTicked(concessionmovein.addConcessionYes, 0),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardHolder, "Peter Parker", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardType, "Queensland Seniors Card", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardNum, getProp("test_data_14"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(cardExpiry, this.concessionExpiry, assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concatStrings(dragAndDropText, " ", concessionUploadArea),
					"cloud_upload Drag-and-drop file here or click to browse for file to upload a scan or picture of your concession card Sprin't 02 Story 'Board.pdf .application/pdf 0.4 MB File uploaded successfully",
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			clickElementAction(directdebitmovein.header);
			pauseSeleniumExecution(1000);

			// verify Direct Debit details
			softAssertion.assertTrue(isElementTicked(directdebitmovein.noDirectDebit, 0),
					assertionErrorMsg(getLineNumber()));
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
			String directDebit = getDisplayedText(acceptancemovein.directDebitRow, true);
			String addNote = getDisplayedText(acceptancemovein.additionalNoteRow, true);
			softAssertion.assertEquals(movingIn,
					concatStrings("Moving In update Moving in as Property Manager or Letting Agent ",
							this.propManMoveInDate),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(servAdd,
					"Service Address update 510 Burwood Highway Service Road Vermont South, Queensland, 3133",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(lifeSupport,
					"Life Support update Life support required using the following equipment Oxygen Concentrator Medical Certificate Provided",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(acctDetails, "Account Details update Residential Account",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContact,
					"Main Account Contact update Sanjeev Dhaka Mobile Phone: 0352537499",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(mainContactNotif,
					"Main Account Contact Notification update Bills (None) Notifications and Reminders (None) Marketing (None)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(concession,
					concatStrings("Concession update Peter Parker Queensland Seniors Card ", getProp("test_data_14"),
							" (", this.concessionExpiry, ") Concession Card Uploaded"),
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
		addProp("ResiExistingContact05_propManMoveInDate", this.propManMoveInDate);
		addProp("ResiExistingContact05_sourceID", this.sourceID);
		addProp("ResiExistingContact05_dateSubmittedSlash",
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
