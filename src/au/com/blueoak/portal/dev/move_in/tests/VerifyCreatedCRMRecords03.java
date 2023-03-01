package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_in.MoveInDevBase;
import au.com.blueoak.portal.pageObjects.crm.OnlineRequestRecordView;
import au.com.blueoak.portal.pageObjects.crm.SMSRecordView;

public class VerifyCreatedCRMRecords03 extends MoveInDevBase {

	/**
	 * Initialize the page objects factory
	 */
	OnlineRequestRecordView onlinerequestrecordview;
	SMSRecordView smsrecordview;

	/**
	 * Store the name of the class for logging
	 */
	String className;

	/**
	 * This will check if we already logged into the CRM
	 */
	boolean isCrmLoggedIn = false;

	/**
	 * The ID of the online request created
	 */
	String onlineReqId;

	/**
	 * Use this to get the ID of the Online Request
	 * 
	 */
	private String getIdOfOnlineRequest() {

		String record = driver.getCurrentUrl();
		String id = getStringAfter(record, "OnlineRequests/");

		logDebugMessage("The value to be returned by getIdOfOnlineRequest() is '" + id + "'");
		return id;
	}

	/**
	 * Use this to get a certain value from the bbeng_online_request_details table
	 * 
	 * @throws SQLException
	 * 
	 */
	private String getOnlineRequestDbValue(String colNameToGet, String onlineReqId) throws SQLException {

		String query = new StringBuilder("SELECT ").append(colNameToGet)
				.append(" FROM `bbeng_online_request_details` WHERE id = '").append(onlineReqId).append("';")
				.toString();

		String result = executeQuery(query);
		return result;
	}

	/**
	 * Use this to get the link name in the Communications > Preview > Message
	 */
	private String getMessageLinkName(WebElement rowFluidElement) {

		String linkName = rowFluidElement.findElement(By.xpath(".//div[@data-name='preview_message']/span/div[2]"))
				.getText();
		linkName = normalizeSpaces(linkName);

		logDebugMessage(
				concatStrings("The value to be returned by getMessageLinkName(WebElement) is '", linkName, "'"));
		return linkName;
	}

	/**
	 * Use this to click the Show message body button on Communications > Preview >
	 * Message
	 */
	private void clickShowMessageBody(WebElement rowFluidElement) {

		WebElement showMsgBtn = rowFluidElement
				.findElement(By.xpath(".//div[@data-name='preview_message']/span/div[2]/button[@id='show-message']"));
		clickElementAction(showMsgBtn);
	}

	/**
	 * Use this to get the element for the message body
	 */
	private WebElement getMessageBodyElement(WebElement rowFluidElement) {

		WebElement messageBodyElement = rowFluidElement
				.findElement(By.xpath(".//div[@data-name='preview_message']/span/div[3]"));
		return messageBodyElement;
	}

	/**
	 * Use this to verify if an email or SMS was created
	 */
	private String getIdOfEmailOrSMS(String tableName, String recordName) throws SQLException {

		String query = new StringBuilder("SELECT id FROM ").append(tableName).append(" WHERE `name` = '")
				.append(recordName).append("';").toString();

		String result = executeQuery(query);
		return result;
	}

	/**
	 * Use this to get the main sms_message_id and use to the update the
	 * communications record
	 * 
	 * @throws SQLException
	 * 
	 */
	private String getCommsIDToUpdateFromSMS(String contactName) throws SQLException {

		String query = new StringBuilder(
				"SELECT sms_message_id FROM `bbcrm_smsrecipients` WHERE parent_type = 'bbcrm_OnlineRequestContacts' AND `name` = '")
				.append(contactName).append("';").toString();

		String result = executeQuery(query);
		return result;
	}

	/**
	 * Use this to update the communication records status based on the give
	 * communications ID
	 * 
	 * @throws SQLException
	 */
	private void updateCommunicationStatus(String newStatus, String commsId) throws SQLException {

		String query = new StringBuilder("UPDATE `bbcrm_communication` SET `status`='").append(newStatus)
				.append("' WHERE (`id`='").append(commsId).append("');").toString();
		executeUpdate(query);
	}

	@BeforeClass
	public void beforeClass() {

		// get the current class for logging
		this.className = getTestClassExecuting();
		logTestClassStart(className);
	}

	@AfterClass
	public void afterClass() {

		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		logTestClassEnd(className);
	}

	@BeforeMethod
	public void beforeMethod() {

		// let's initialize the page objects
		onlinerequestrecordview = new OnlineRequestRecordView(driver);
		smsrecordview = new SMSRecordView(driver);
	}

	/** 
	 * 
	 * */
	@Test(priority = 1)
	public void verifyCrmSmallBusNewContact04()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Feil", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Alan Feil",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Apartment 010 506 Burwood HWY",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("SmallBusNewContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusNewContact04_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName,
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact04_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Apartment 010 506 Burwood HWY Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "170 Mitcham Road add#04 Donvale, VIC, 3111 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Alan Feil (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "0471073251", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_abn3_abn4"),
				" T/a JaH Trading's ", getProp("test_data_valid_abn3")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		// verify num of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Catherine Tripp",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+61785215055",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the additional contact
		WebElement preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Catherine Tripp (new contact)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+61785215055",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 6)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 7));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Postal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Postal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Postal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume is 123*010000 Litres Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan : Smaller file tiff file.tiff (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume is 123*010000 Litres Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan : Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (ACCEPTED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		String mainWindow = crmGetWindowHandle();

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Alan Feil")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Catherine Tripp")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Feil, Alan")),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Alan Feil"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":true,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":true,\"trade_waste\":{\"will_discharge\":true,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"AU\"},\"property_manager\":null,\"payment_method\":null,\"additional_notes\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"{\\\"Community\\\":\\\"Arc By Crown\\\",\\\"CTS\\\":\\\"\\\",\\\"Service Fee\\\":\\\" \\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"22.37 c\\/kWh (inc GST)\\\",\\\"ElecSupply\\\":\\\"88 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"},\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\",\\\"GasComments\\\":\\\"\\\"},\\\"HW\\\":{\\\"HWrate\\\":\\\"0.176 c\\/ltr (inc GST)\\\",\\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"}}\",\"source\":{"),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusNewContact04_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/** 
	 * 
	 * */
	@Test(priority = 2)
	public void verifyCrmResiNewContact11()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Retsu", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Kaiou Retsu",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "ATM 0A-11 508 Melton HWY",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("ResiNewContact11_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact11_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveInContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveInContactPersonalIDValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, "K Retsu", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact11_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "ATM 0A-11 508 Melton HWY Sydenham, Victoria, 3037",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "175 Mitcham Road ADD-04 Donvale, VIC, 3111 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Kaiou Retsu (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Postal)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableAscWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_pen_cnssn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Clark Kent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "000000000",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_pen_cnssn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Clark Kent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "000000000",
				assertionErrorMsg(getLineNumber()));
		String expYr = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), concatStrings("31/01/", expYr),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Retsu",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 2nd record
		notes = crmSortTableAscWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Retsu",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (ACCEPTED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		String mainWindow = crmGetWindowHandle();

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Kaiou Retsu")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils
						.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Retsu, Kaiou")),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact11_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kaiou Retsu"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_pen_cnssn\",\"card_holder\":\"Clark Kent\",\"card_number\":\"000000000\",\"expiry_date\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"authorisation\":{\"method\":\"QUESTION\",\"accepted\":true,\"text\":\"'Company'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains("\"config_file\":\"elec_portal_config.json\",\"ip_address\":"),
					assertionErrorMsg(getLineNumber()));
			String data = "\"extra_data\":\"".concat(getProp("test_data_14")).concat("\",\"source\":");
			logDebugMessage(concatStrings("The value of data is: ", data));
			softAssertion.assertTrue(emailBody.contains(data), assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact11_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/** 
	 * 
	 * */
	@Test(priority = 3)
	public void verifyCrmResiNewContact12()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Jack", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Jack Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "508 Burwood Highway Service RD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("ResiNewContact12_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact12_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveInContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveInContactPersonalIDValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, "J Hanma", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact12_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "508 Burwood Highway Service RD Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Owner", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "508 Burwood Highway Service RD Vermont South, QLD, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Jack Hanma (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "qld_health_card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Barry Allen",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "1000000001",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "qld_health_card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Barry Allen",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "1000000001",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 3rd record
		preview = crmGetPreview(notes, 2);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "J Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(DECLINED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Jack Hanma")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils
						.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Hanma, Jack")),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact12_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Jack Hanma"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\"],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"qld_health_card\",\"card_holder\":\"Barry Allen\",\"card_number\":\"1000000001\",\"authorisation\":{\"method\":\"QUESTION\",\"accepted\":false,\"text\":\"'Company'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"{\\\"Community\\\":\\\"Arc By Crown\\\",\\\"CTS\\\":\\\"\\\",\\\"Service Fee\\\":\\\" \\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"22.37 c\\/kWh (inc GST)\\\",\\\"ElecSupply\\\":\\\"88 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"},\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\",\\\"GasComments\\\":\\\"\\\"},\\\"HW\\\":{\\\"HWrate\\\":\\\"0.176 c\\/ltr (inc GST)\\\",\\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"}}\",\"source\":{"),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact12_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 4)
	public void verifyCrmSmallBusNewContact05()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Kozue", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Kozue Matsumoto",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "510 Burwood Highway Service RD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("SmallBusNewContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusNewContact05_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName,
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact05_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "510 Burwood Highway Service RD Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Owner", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "510 Burwood Highway Service RD Vermont South, VIC, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_06"), ") (Card: ending ", getProp("test_data_05"),
						" / Exp: ", getProp("SmallBusNewContact05_creditCardExpiryMonth"), "-",
						getProp("SmallBusNewContact05_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Kozue Matsumoto (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_abn3_abn4"),
				" T/a JaH Trading's ", getProp("test_data_valid_abn3")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableAscWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		cleanDownloadDir();
		// verify the preview for the 1st record
		WebElement preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Sprin't 02 Story 'Board.pdf",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Sprin't 02 Story 'Board.pdf",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify the contents of the PDF
		crmClickRecordExactLinkText("Sprin't 02 Story 'Board.pdf");
		crmLoad();
		logDebugMessage(concatStrings("We will be waiting for <",
				Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
				"> milli seconds before checking for the downloaded file."));
		pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
		verifyPdfContent("Sprin't 02 Story 'Board.pdf", 1, 500, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		// verify the preview for the 2nd record
		notes = crmSortTableAscWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (ACCEPTED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 3rd record
		notes = crmSortTableAscWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan : eicar.com.pdf (FILE CONTAINED VIRUS SO IT WAS NOT ATTACHED TO THIS NOTE AND WAS DELETED)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kozue Matsumoto"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Matsumoto, Kozue", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusNewContact05_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 5)
	public void verifyCrmResiNewContact13()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Akezawa", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Emi Akezawa",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "510 Burwood Highway Service RD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("ResiNewContact13_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact13_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Emi Akezawa");
		updateCommunicationStatus("sent", commsId);
		// ensure that the communications subpanel will get new data
		refreshBrowser(1, 5000);
		crmLoad();
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveInContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveInContactPersonalIDValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, "E Akezawa", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact13_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "510 Burwood Highway Service RD Vermont South, Tasmania, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "510 Burwood Highway Service RD Vermont South, TAS, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Emi Akezawa (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61203950165", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 2nd record
		WebElement preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact13_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Emi Akezawa"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\"],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":null,\"property_manager\":null,\"payment_method\":null,\"additional_notes\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"{\\\"Community\\\":\\\"Arc By Crown\\\",\\\"CTS\\\":\\\"\\\",\\\"Service Fee\\\":\\\" \\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"22.37 c\\/kWh (inc GST)\\\",\\\"ElecSupply\\\":\\\"88 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"},\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\",\\\"GasComments\\\":\\\"\\\"},\\\"HW\\\":{\\\"HWrate\\\":\\\"0.176 c\\/ltr (inc GST)\\\",\\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"}}\",\"source\":{"),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact13_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 6)
	public void verifyCrmVerifyValidations01_verifySessionConfig04()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Zaru", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 7)
	public void verifyCrmResiExistingContact05()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Dhaka", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Sanjeev Dhaka",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "510 Burwood Highway Service RD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("ResiExistingContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiExistingContact05_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Sanjeev Dhaka");
		updateCommunicationStatus("sent", commsId);
		// ensure that the communications subpanel will get new data
		refreshBrowser(1, 5000);
		crmLoad();
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveInContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveInContactPersonalIDValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, "S Dhaka", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact05_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "510 Burwood Highway Service RD Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "510 Burwood Highway Service RD Vermont South, QLD, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Sanjeev Dhaka (Will be merged with contact Mr. Sanjeev Dhaka)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0352537499", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableAscWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "Queensland Seniors Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Peter Parker",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), getProp("test_data_14"),
				assertionErrorMsg(getLineNumber()));

		// put the correct assertions for the notes subpanel
		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 4, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		cleanDownloadDir();
		// verify the attachment for the 1st record
		crmGetPreview(notes, 0);
		// verify the contents of the PDF
		crmClickRecordExactLinkText("Sprin't 02 Story 'Board.pdf");
		crmLoad();
		logDebugMessage(concatStrings("We will be waiting for <",
				Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
				"> milli seconds before checking for the downloaded file."));
		pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
		verifyPdfContent("Sprin't 02 Story 'Board.pdf", 1, 500, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		cleanDownloadDir();
		// verify the attachment for the 2nd record
		crmGetPreview(notes, 1);
		// let's verify the picture if it's valid and if it was downloaded
		crmClickRecordExactLinkText("g'alaxy-'wallpaper.jpeg");
		crmLoad();
		logDebugMessage(concatStrings("We will be waiting for <",
				Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
				"> milli seconds before checking for the downloaded file."));
		pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
		// verify that the downloaded files are correct
		List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
		int filesSize = files.size();
		softAssertion.assertEquals(files.get(0), "g'alaxy-'wallpaper.jpeg", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(filesSize, 1, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// TODO
		// catch the exception java.lang.AssertionError
		// if encountered, refresh the page
		// wait for a couple of seconds (variable in utility)
		// then sort the subpanel again
		// delete download directory
		// then download the file again and assert
		// verify if image is not corrupted
		assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\g'alaxy-'wallpaper.jpeg")),
				concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		WebElement preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sanjeev Dhaka"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\"],\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[],\"attachments\":[]}],"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("{\"card_type\":\"qld_senior\",\"card_holder\":\"Peter Parker\",\"card_number\":\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"{\\\"Community\\\":\\\"Arc By Crown\\\",\\\"CTS\\\":\\\"\\\",\\\"Service Fee\\\":\\\" \\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"22.37 c\\/kWh (inc GST)\\\",\\\"ElecSupply\\\":\\\"88 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"},\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\",\\\"GasComments\\\":\\\"\\\"},\\\"HW\\\":{\\\"HWrate\\\":\\\"0.176 c\\/ltr (inc GST)\\\",\\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"}}\",\"source\":{"),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("ResiExistingContact05_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * 
	 * */
	@Test(priority = 8)
	public void verifyCrmVerifyValidations01_verifySessionConfig05()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Kinuyo", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// click the record
		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
				" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("VerifyValidations01_verifySessionConfig05_moveInDate"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(reqTenancy, "Tenancy 20 20 Bella Vista ST Heathcote, New South Wales, 2233",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(reqTenancy,
					"dummy complex Tenancy 20 20 Bella Vista ST Heathcote, New South Wales, 2233",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(supplyStateReported, "Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Owner", assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(postalAdd, "Tenancy 20 20 Bella Vista Street Heathcote, NSW, 2233",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(postalAdd, "dummy complex Tenancy 20 20 Bella Vista Street Heathcote, NSW, 2233",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(directDebit, "Bank Account (BSB: 000100 / Num: 001000)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				dateSubmitted.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				dateCreated.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Kinuyo Matsumoto (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61400853690", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "130285", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0332878850", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Testing 123...", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Postal) Reminders (Email) Account Issues (Email) Account Changes (Email) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				dateModified.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		// verify num of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Roshan Britto",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "+61400853690",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+960985740362",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "0332878850",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// click More notes... link to display all records
		crmClickMoreRecordsSubpanel("Notes");
		crmScrollPage(12, Keys.DOWN);

		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 7, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 6th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 5, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 5, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 7th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 6, 1), "Additional Note from Customer",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 2)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 3)
						.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 6, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify all asertions
		softAssertion.assertAll();

		cleanDownloadDir();
		// verify the attachment for the 1st record
		crmGetPreview(notes, 0);
		// verify the contents of the PDF
		crmClickRecordExactLinkText("Sprin't 02 Story 'Board.pdf");
		crmLoad();
		logDebugMessage(concatStrings("We will be waiting for <",
				Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
				"> milli seconds before checking for the downloaded file."));
		pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
		verifyPdfContent("Sprin't 02 Story 'Board.pdf", 1, 500, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		cleanDownloadDir();
		// verify the attachment for the 4th record
		WebElement preview = crmGetPreview(notes, 3);
		String attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.equals("g'alaxy-'wallpaper.jpeg")) {
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("g'alaxy-'wallpaper.jpeg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "g'alaxy-'wallpaper.jpeg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(filesSize, 1, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// TODO
			// catch the exception java.lang.AssertionError
			// if encountered, refresh the page
			// wait for a couple of seconds (variable in utility)
			// then sort the subpanel again
			// delete download directory
			// then download the file again and assert
			// verify if image is not corrupted
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\g'alaxy-'wallpaper.jpeg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.equals("Sprin't 02 Story 'Board.pdf")) {
			// verify the contents of the PDF
			crmClickRecordExactLinkText("Sprin't 02 Story 'Board.pdf");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			verifyPdfContent("Sprin't 02 Story 'Board.pdf", 1, 500, true, "Accounts",
					"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
					"New Contact - Account Contact", "New Communication - SMS");
		} else {
			fail("There's a different attachment in the notes");
		}

		cleanDownloadDir();
		// verify the attachment for the 5th record
		preview = crmGetPreview(notes, 4);
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.equals("g'alaxy-'wallpaper.jpeg")) {
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("g'alaxy-'wallpaper.jpeg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "g'alaxy-'wallpaper.jpeg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(filesSize, 1, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// TODO
			// catch the exception java.lang.AssertionError
			// if encountered, refresh the page
			// wait for a couple of seconds (variable in utility)
			// then sort the subpanel again
			// delete download directory
			// then download the file again and assert
			// verify if image is not corrupted
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\g'alaxy-'wallpaper.jpeg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.equals("Sprin't 02 Story 'Board.pdf")) {
			// verify the contents of the PDF
			crmClickRecordExactLinkText("Sprin't 02 Story 'Board.pdf");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			verifyPdfContent("Sprin't 02 Story 'Board.pdf", 1, 500, true, "Accounts",
					"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
					"New Contact - Account Contact", "New Communication - SMS");
		} else {
			fail("There's a different attachment in the notes");
		}

		crmScrollPage(10, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Kinuyo Matsumoto"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(
				dateCreatedWebForm.startsWith(getProp("VerifyValidations01_verifySessionConfig05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// TODO
		// put the assertion here for the extra data for all run types

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("VerifyValidations01_verifySessionConfig05_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));

		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 9)
	public void verifyCrmSmallBusExistingContact05()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Susan", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// click the record
		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName,
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact05_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "ATM 0A-11 508 Melton HWY Sydenham, Victoria, 3037",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "ATM 0A-11 508 Melton HWY Sydenham, VIC, 3037",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Susan & Les Smith (Will be merged with contact Susan & Les Smith)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "0827022617", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_abn3_abn4"),
				" T/a JaH Trading's ", getProp("test_data_valid_abn3")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusExistingContact05_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(8, Keys.DOWN);
		WebElement comms = crmSortTableDescWithinPanel("Communications", 2, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Susan & Les Smith"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusExistingContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		// verify the number of notes and the names of the notes created
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 1, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 2).startsWith(
				getProp("SmallBusExistingContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 3).startsWith(
				getProp("SmallBusExistingContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusExistingContact05_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));

		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 10)
	public void verifyCrmResiExistingContact04()
			throws SQLException, EncryptedDocumentException, InvalidFormatException, IOException {

		// check if there's a previous session of the CRM
		if (!isCrmLoggedIn) {
			// login into the crm and verify the payment is related to the account
			crmLogin(true);
			this.isCrmLoggedIn = true;
		} else {
			// we are already logged in so we just navigate into the crm home page
			crmNavigateHomepage();
			if (crmAreWeInLoginPage() == true) {
				crmLogin(false);
			}
		}

		// initialize Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the online request list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Rasha", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Rasha Ehsara",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "520 Burwood Highway Service RD",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("ResiNewContact15_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact15_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveInRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveInProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveInDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveInRequestedTenancyValue, true);
		String supplyStateReported = getDisplayedText(onlinerequestrecordview.moveInSupplyStateReportedByCust, true);
		String lifeSupport = getDisplayedText(onlinerequestrecordview.moveInActiveLifeSupport, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveInAccountTypeValue, true);
		String acctCategory = getDisplayedText(onlinerequestrecordview.moveInAccountCategoryValue, true);
		String postalAdd = getDisplayedText(onlinerequestrecordview.moveInPostalAddValue, true);
		String directDebit = getDisplayedText(onlinerequestrecordview.moveInDirectDebitValue, true);
		String refAcct = getDisplayedText(onlinerequestrecordview.moveInReferenceAcctValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveInDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveInDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveInContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveInContactBusPhoneValue, true);
		String contactAHrsPhone = getDisplayedText(onlinerequestrecordview.moveInContactAfterHrsPhoneValue, true);
		String mobPhone = getDisplayedText(onlinerequestrecordview.moveInContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveInContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveInContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveInContactPersonalIDValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, "R Ehsara", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact15_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "520 Burwood Highway Service RD Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "520 Burwood Highway Service RD Vermont South, VIC, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Bank Account (BSB: 003281 / Num: 0012984571)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Rasha Ehsara (Will be merged with contact Rasha Ehsara)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+09198561256", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
					assertionErrorMsg(getLineNumber()));
		} catch (AssertionError ae) {
			// date crossed issue, so we assert the current date
			String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
			softAssertion.assertTrue(dateModified.contains("12:0"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(dateModified.startsWith(today), assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		}

		crmScrollPage(10, Keys.DOWN);
		// verify the additional contacts section
		WebElement addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(addContacts), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_pen_cnssn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Dr. Stephen Strange",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "0032168451200",
				assertionErrorMsg(getLineNumber()));

		// put the correct assertions for the notes subpanel
		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		WebElement preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Ehsara",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (ACCEPTED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Ehsara",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: The Company Name Contact Number: The Company Contact Number Address: Add & 01, 180 Mitcham Road, Add & 03, Add & 04, Donvale, VIC, 3111, AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 3rd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Ehsara",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(DECLINED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(5, Keys.UP);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"),
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedWebForm,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		String to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact15_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rasha Ehsara"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},{\"service_type\":\"WATER\",\"required\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":true,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":true,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_pen_cnssn\",\"card_holder\":\"Dr. Stephen Strange\",\"card_number\":\"0032168451200\",\"expiry_date\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody
						.contains("\"authorisation\":{\"method\":\"QUESTION\",\"accepted\":false,\"text\":\"'Company'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"config_file\":\"portal_config.json\",\"ip_address\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings("\"instance_id\":\"", getInstanceIdMoveIn(), "\"")),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":null,\"source\":"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"\\\"extra_data\\\":\\\"{\\\"Community\\\":\\\"Shepherds Bay | 118 Bowden St\\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"20.79 c\\/kWh (inc GST)\\\", \\\"ElecSupply\\\":\\\"82.5 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"} ,\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\", \\\"GasComments\\\":\\\"\\\"} ,\\\"HW\\\":{\\\"HWrate\\\":\\\"0.55 c\\/ltr (inc GST)\\\", \\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"} ,\\\"RW\\\":{\\\"RWrate\\\":\\\"2.35 $\\/kL\\\",\\\"DWSupply\\\":\\\"0$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} ,\\\"WW\\\":{\\\"WWrate\\\":\\\"51.59$\\/Month\\\",\\\"DWCom\\\":\\\"\\\"} }\\\",\\\"source\\\":{"),
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertTrue(emailBody.contains(
					"\"extra_data\":\"{\\\"Community\\\":\\\"Arc By Crown\\\",\\\"CTS\\\":\\\"\\\",\\\"Service Fee\\\":\\\" \\\",\\\"Electricity\\\":{\\\"Elecrate\\\":\\\"22.37 c\\/kWh (inc GST)\\\",\\\"ElecSupply\\\":\\\"88 c\\/day (inc GST)\\\",\\\"ElecCom\\\":\\\"\\\"},\\\"Cooktop\\\":{\\\"Gasrate\\\":\\\"27.5 c\\/day (inc GST)\\\",\\\"GasComments\\\":\\\"\\\"},\\\"HW\\\":{\\\"HWrate\\\":\\\"0.176 c\\/ltr (inc GST)\\\",\\\"HWSupply\\\":\\\"49.5 c\\/day (inc GST)\\\",\\\"ThermalCom\\\":\\\"\\\"}}\",\"source\":{"),
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact15_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

}