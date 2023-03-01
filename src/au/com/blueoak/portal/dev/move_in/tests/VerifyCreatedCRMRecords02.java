package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
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

public class VerifyCreatedCRMRecords02 extends MoveInDevBase {

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
	 * Use this to click the Hide message body button on Communications > Preview >
	 * Message
	 */
	private void clickHideMessageBody(WebElement rowFluidElement) {

		WebElement hideMsgBtn = rowFluidElement
				.findElement(By.xpath(".//div[@data-name='preview_message']/span/div[2]/button[@id='hide-message']"));
		clickElementAction(hideMsgBtn);
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
	public void verifyCrmResiNewContact03()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("sacha", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Sacha Eia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Villa 5 5 Albion RD",
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
				getProp("ResiNewContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact03_propManMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiNewContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiNewContact03_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Residential / Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "S Eia",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Sacha Eia (Will be merged with contact Ms. Sacha Eia)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 11)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 12)),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13)),
					"Villa 5 5 Albion RD Box Hill, Victoria, 3128", assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13)),
					"dummy complex Villa 5 5 Albion RD Box Hill, Victoria, 3128", assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "None Active",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 16)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
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
		softAssertion.assertEquals(acctName, "S Eia", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact03_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(reqTenancy, "Villa 5 5 Albion RD Box Hill, Victoria, 3128",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(reqTenancy, "dummy complex Villa 5 5 Albion RD Box Hill, Victoria, 3128",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Villa 5 5 Albion RD Box Hill, VIC, 3128",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(contactName, "Sacha Eia (Will be merged with contact Ms. Sacha Eia)",
				assertionErrorMsg(getLineNumber()));
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
			assertTrue(dateModified.startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Cherelyn Seal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Cherelyn Seal (Will be merged with contact Cherelyn Seal)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
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
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Cherelyn Seal");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Cherelyn Seal", assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Cherelyn Seal)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValueList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactMobPhoneValueList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactSecretCodeValueList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_pen_veterns",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Sanji Vinsmoke",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "0098767142100",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_pen_veterns",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Sanji Vinsmoke",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "0098767142100",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S Eia",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(DECLINED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (DECLINED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
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
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		// clear the downloads first
		cleanDownloadDir();
		String attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.contains("Test PNG Type 01.png")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S Eia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Test PNG Type 01.png less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Test PNG Type 01.png",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Test PNG Type 01.png");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Test PNG Type 01.png", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Test PNG Type 01.png")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.contains("Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S Eia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Smaller tif file.tif less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller tif file.tif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller tif file.tif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller tif file.tif", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller tif file.tif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else {
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 3rd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		// clear the downloads first
		cleanDownloadDir();
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.contains("Test PNG Type 01.png")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S Eia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Test PNG Type 01.png less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Test PNG Type 01.png",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Test PNG Type 01.png");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Test PNG Type 01.png", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Test PNG Type 01.png")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.contains("Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S Eia",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Smaller tif file.tif less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller tif file.tif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller tif file.tif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller tif file.tif", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller tif file.tif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else {
			fail("There's a different attachment in the notes");
		}

		crmScrollPage(10, Keys.UP);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Sacha Eia")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Cherelyn Seal")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Eia, Sacha")),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Sacha Eia"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"name\":\"g'alaxy-'wallpaper.jpeg\"}]}"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":true,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"trade_waste\":null,\"business_identity\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_pen_veterns\",\"card_holder\":\"Sanji Vinsmoke\",\"card_number\":\"0098767142100\",\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact03_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
	}

	/** 
	 * 
	 * */
	@Test(priority = 2)
	public void verifyCrmResiNewContact04()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("baki", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Baki Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Office 301 192 Marine PDE",
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
				getProp("ResiNewContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact04_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiNewContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiNewContact04_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Residential / Tenant",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "B Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Baki Hanma (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "1300277230",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13)),
				"Office 301 192 Marine PDE Coolangatta, Northern Territory, 4225", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 16)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
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
		softAssertion.assertEquals(acctName, "B Hanma", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact04_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Office 301 192 Marine PDE Coolangatta, Northern Territory, 4225",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Add 01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Add 04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ City ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, State ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ AX",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_17"), ") (Card: ending ", getProp("test_data_16"),
						" / Exp: ", getProp("ResiNewContact04_creditCardExpiryMonth"), "-",
						getProp("ResiNewContact04_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Baki Hanma (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "1300277230", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Email) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
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
		// verify num of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Yuujiro Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "+111111111111",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+2222222222222",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+0000000000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Yuujiro Hanma (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "+111111111111",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+2222222222222",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+0000000000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 6)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 7));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Postal, Email, SMS", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "B Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "B Hanma",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "B Hanma",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		// sms not displayed since it failed
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Baki Hanma"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Hanma, Baki", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the WEB_FORM
		comms = crmSortTableAscWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Baki Hanma"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Baki Hanma"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Baki Hanma"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Baki Hanma"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\",\"Other's Machinery\\\"s\"],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":null,\"property_manager\":null,\"payment_method\":{\"credit_card\":{\"card_holder\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact04_sourceID"),
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
	public void verifyCrmResiNewContact05()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("kim", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "sung kim",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Suite 19 19 Agnes ST",
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
				getProp("ResiNewContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact05_propManMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiNewContact05_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiNewContact05_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Residential / Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "S kim",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// will assert the current value for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"sung kim (Will be merged with contact Sung Kim)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 11)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 12)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13)),
				"Suite 19 19 Agnes ST Fortitude Valley, Victoria, 4006", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14), "Required (Electricity)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "None Active",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 16)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
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
		softAssertion.assertEquals(acctName, "S kim", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact05_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Suite 19 19 Agnes ST Fortitude Valley, Victoria, 4006",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Suite 19 19 Agnes ST Fortitude Valley, VIC, 4006",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(contactName, "sung kim (Will be merged with contact Sung Kim)",
				assertionErrorMsg(getLineNumber()));
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
			assertTrue(dateModified.startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "hao quach",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"hao quach (Will be merged with contact Hao Quach)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact05_dateOfBirthAddCont1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "009847100 (Åland Islands)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_pen_veterns",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Dr. Stephen Strange",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "0984651450",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_pen_veterns",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Dr. Stephen Strange",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "0984651450",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 4, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S kim",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S kim",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: One Piece LLC Contact Number: 6 to 9 pm Address: AddLine01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, AddLine02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, AddLine03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, AddLine04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, City ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, State ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, AX",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S kim",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 4th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "S kim",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> eicar.com.pdf (FILE CONTAINED VIRUS SO IT WAS NOT ATTACHED TO THIS NOTE AND WAS DELETED) less",
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
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "sung kim")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "hao quach")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to kim, sung")),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact05_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM sung kim"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Intermittent Peritoneal Dialysis Machine\",\"Crigler Najjar Syndrome Phototherapy Equipment\"],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_pen_veterns\",\"card_holder\":\"Dr. Stephen Strange\",\"card_number\":\"0984651450\",\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact05_sourceID"),
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
	@Test(priority = 4)
	public void verifyCrmResiNewContact06()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("nguyen", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Peter Nguyen",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Shop 303 192 Marine PDE",
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
				getProp("ResiNewContact06_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact06_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Peter Nguyen");
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
		softAssertion.assertEquals(acctName, "P Nguyen", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact06_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(reqTenancy, "Shop 303 192 Marine PDE Coolangatta, Queensland, 4225",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(reqTenancy,
					"dummy complex Shop 303 192 Marine PDE Coolangatta, Queensland, 4225",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(postalAdd, "Shop 303 192 Marine Parade Coolangatta, QLD, 4225",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(postalAdd, "dummy complex Shop 303 192 Marine Parade Coolangatta, QLD, 4225",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Peter Nguyen (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+861310907788", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiNewContact06_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Driver's License (068654110, Tasmania)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (Postal) Account Issues (Postal) Account Changes (Postal) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
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
		// verify the concession section
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		WebElement preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "P Nguyen",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "P Nguyen",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: World Government Contact Number: Anytime :) Address: addLine01 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, addLine02 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, addLine03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, addLine04 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, city ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, state ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, postcode ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, AX",
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
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "P Nguyen",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 2nd record
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"Peter Nguyen (+861310907788)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		// test the fix for bug ticket BBCRM-10947
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedSMS,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		String dateSubmitUpdRaw1 = normalizeSpaces(dateSubmitted);
		String dateSubmitUpdRaw2 = dateSubmitUpdRaw1.replace("/", "-");
		String dateSubmitUpdRaw3 = dateSubmitUpdRaw2.toUpperCase();
		// let's get the time
		String recordViewTime = getString(dateSubmitUpdRaw3, 11, dateSubmitUpdRaw3.length());
		logDebugMessage("The value of recordViewTime is '" + recordViewTime + "'");
		int recordViewTimeLength = recordViewTime.length();
		String recordViewTimeLengthStr = Integer.toString(recordViewTimeLength);
		String missingZeroStr = Integer.toString(6);
		logDebugMessage("The value of recordViewTimeLength is '" + recordViewTimeLength + "'");
		String dateSubmitUpd;
		if (recordViewTimeLengthStr.equals(missingZeroStr)) {
			// the time is single digit and is missing zero
			String todaySubmitted = getProp("ResiNewContact06_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Nguyen Request Postal Address: Shop 303 192 Marine Parade Coolangatta, QLD 4225 Request Complex Name: Request Tenancy Address: Shop 303 192 Marine PDE Request Tenancy Suburb: Coolangatta Request Tenancy State: QLD Request Tenancy Postcode: 4225 Request Electricity Life Support: In Use Request Water Life Support: Request Direct Debit: Request Holiday Rental: Yes Request Move In Date: ",
					getProp("ResiNewContact06_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("ResiNewContact06_ownerMoveInDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Nguyen Request Postal Address: dummy complex Shop 303 192 Marine Parade Coolangatta, QLD 4225 Request Complex Name: dummy complex Request Tenancy Address: Shop 303 192 Marine PDE Request Tenancy Suburb: Coolangatta Request Tenancy State: QLD Request Tenancy Postcode: 4225 Request Electricity Life Support: In Use Request Water Life Support: Request Direct Debit: Request Holiday Rental: Yes Request Move In Date: ",
					getProp("ResiNewContact06_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("ResiNewContact06_ownerMoveInDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		softAssertion.assertEquals(to, "Peter Nguyen", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 1st record
		comms = crmSortTableAscWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"),
				assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
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
		messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact06_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Nguyen"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Haemodialysis Machine\"],\"attachments\":[]},{\"service_type\":\"WATER\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,"),
				assertionErrorMsg(getLineNumber()));
		// verify no additional contact sent
		softAssertion.assertFalse(emailBody.contains("[{\"main_contact\":false,"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":false,\"concession_card\":null,\"property_manager\":{\"manager_name\":\"World Government\","),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact06_sourceID"),
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
	@Test(priority = 5)
	public void verifyCrmResiNewContact07()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Tony", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Tony Michael",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Lot 51 502-514 Burwood HWY",
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
				getProp("ResiNewContact07_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact07_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(acctName, "T Michael", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact07_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Lot 51 502-514 Burwood HWY Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Lot 51 502-514 Burwood Highway Vermont South, VIC, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Tony Michael (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "1800503291", assertionErrorMsg(getLineNumber()));
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
			assertTrue(dateModified.startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Davut Ataman",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		WebElement preview = crmGetPreview(addContacts, 0);
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Davut Ataman (Will be merged with contact Davut Ataman)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact07_dateOfBirthAddCont1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "00Abc98450 (Western Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "Postal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		// verify the concession section
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "T Michael",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "T Michael",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Contact Number: Address: Add-03, AX", assertionErrorMsg(getLineNumber()));
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Tony Michael")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Davut Ataman")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils
						.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Michael, Tony")),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact07_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Tony Michael"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[{\"id\":\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"name\":\"typing jim carrey.gif\"}"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"name\":\"Sprin't 02 Story 'Board.pdf\"}"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"medical_cooling\":true,\"trade_waste\":null,\"business_identity\":null,\"account_contacts\":[{"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact07_sourceID"),
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
	@Test(priority = 6)
	public void verifyCrmResiNewContact08()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Doppo", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Doppo Orochi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Unit 6 6 Mari ST",
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
				getProp("ResiNewContact08_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact08_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Doppo Orochi");
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
		softAssertion.assertEquals(acctName, "D Orochi", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact08_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "'002 Complex\"s Unit 6 6 Mari ST Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Unit 6 6 Mari ST Alexandra Headland, QLD, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Doppo Orochi (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0410928640", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiNewContact08_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
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
		// verify num of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Kiyosumi Katou",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "0787654321",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "DVA Gold Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Rey Ms Terio",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "9698664000",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "DVA Gold Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Rey Ms Terio",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "9698664000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "D Orochi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> No concession card has been attached. less",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "D Orochi",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Orochi, Doppo", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 3rd record
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"Doppo Orochi (0410928640)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		String messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		// test the fix for bug ticket BBCRM-10947
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedSMS,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		String messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		String dateSubmitUpdRaw1 = normalizeSpaces(dateSubmitted);
		String dateSubmitUpdRaw2 = dateSubmitUpdRaw1.replace("/", "-");
		String dateSubmitUpdRaw3 = dateSubmitUpdRaw2.toUpperCase();
		// let's get the time
		String recordViewTime = getString(dateSubmitUpdRaw3, 11, dateSubmitUpdRaw3.length());
		logDebugMessage("The value of recordViewTime is '" + recordViewTime + "'");
		int recordViewTimeLength = recordViewTime.length();
		String recordViewTimeLengthStr = Integer.toString(recordViewTimeLength);
		String missingZeroStr = Integer.toString(6);
		logDebugMessage("The value of recordViewTimeLength is '" + recordViewTimeLength + "'");
		String dateSubmitUpd;
		if (recordViewTimeLengthStr.equals(missingZeroStr)) {
			// the time is single digit and is missing zero
			String todaySubmitted = getProp("ResiNewContact08_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Doppo Request Main Contact Last Name: Orochi Request Postal Address: Unit 6 6 Mari ST Alexandra Headland, QLD 4572 Request Complex Name: '002 Complex\"s Request Tenancy Address: Unit 6 6 Mari ST Request Tenancy Suburb: Alexandra Headland Request Tenancy State: QLD Request Tenancy Postcode: 4572 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("ResiNewContact08_tenantMoveInDateCRM"),
				" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
				dateSubmitUpd,
				" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		softAssertion.assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		softAssertion.assertEquals(to, "Doppo Orochi", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableAscWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)), concatStrings("Doppo Orochi (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Orochi, Doppo", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		// test the fix for bug ticket BBCRM-10947
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), dateCreatedEmail,
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// verify Show message body link and verify the content
		clickShowMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// verify that the message got displayed
		assertTrue(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		dateSubmitUpdRaw1 = normalizeSpaces(dateSubmitted);
		dateSubmitUpdRaw2 = dateSubmitUpdRaw1.replace("/", "-");
		dateSubmitUpdRaw3 = dateSubmitUpdRaw2.toUpperCase();
		// let's get the time
		recordViewTime = getString(dateSubmitUpdRaw3, 11, dateSubmitUpdRaw3.length());
		logDebugMessage("The value of recordViewTime is '" + recordViewTime + "'");
		recordViewTimeLength = recordViewTime.length();
		recordViewTimeLengthStr = Integer.toString(recordViewTimeLength);
		missingZeroStr = Integer.toString(6);
		logDebugMessage("The value of recordViewTimeLength is '" + recordViewTimeLength + "'");
		if (recordViewTimeLengthStr.equals(missingZeroStr)) {
			// the time is single digit and is missing zero
			String todaySubmitted = getProp("ResiNewContact08_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Doppo Request Main Contact Last Name: Orochi Request Postal Address: Unit 6 6 Mari ST Alexandra Headland, QLD 4572 Request Complex Name: '002 Complex\"s Request Tenancy Address: Unit 6 6 Mari ST Request Tenancy Suburb: Alexandra Headland Request Tenancy State: QLD Request Tenancy Postcode: 4572 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("ResiNewContact08_tenantMoveInDateCRM"),
				" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
				dateSubmitUpd,
				" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		assertFalse(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the subject is clickable and it would open the Email record view
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Orochi, Doppo");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Orochi, Doppo", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Orochi, Doppo",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Doppo Orochi <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Orochi, Doppo",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 1st record
		comms = crmSortTableAscWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"),
				assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
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
		messageBodyAct = getDisplayedText(crmGetPreviewWebElementByRowFluidNum(preview, 5), true);
		// verify the message body is not blank
		assertTrue(StringUtils.isNotBlank(messageBodyAct), assertionErrorMsg(getLineNumber()));
		clickExactLinkNameFromElement(preview,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		team = normalizeSpaces(getEmailDataPerCell(1, 1));
		relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact08_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Doppo Orochi"),
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
		softAssertion.assertTrue(emailBody.contains(
				"\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,\"account_contacts\":["),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"qld_dva_gold\","),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact08_sourceID"),
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
	@Test(priority = 7)
	public void verifyCrmSmallBusNewContact02()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Atsushi", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Atsushi Suedou",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "House 53 514 Burwood HWY",
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
				getProp("SmallBusNewContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusNewContact02_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(acctName, getProp("test_data_valid_company_name_acn3_acn4"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact02_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy,
				"~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ House 53 514 Burwood HWY Vermont South, Queensland, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ House 53 514 Burwood Highway Vermont South, QLD, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Atsushi Suedou (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"), " ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Mitsunari Tokugawa",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		WebElement preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Mitsunari Tokugawa (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
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
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		// clear the content of the download directory
		cleanDownloadDir();
		String description = crmGetPreviewDataByLabel(preview, "Description");
		// did this since we won't know if the arrangement might change in the future
		if (description.contains("Site plan 1 of 2: Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller file tiff file.tiff");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller file tiff file.tiff")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 2 of 2: Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller file tiff file.tiff");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller file tiff file.tiff")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 2 of 2: typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("typing jim carrey.gif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(filesSize, 1, assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
			// verify if image is not corrupted
			// TODO
			// catch the exception java.lang.AssertionError
			// if encountered, refresh the page
			// wait for a couple of seconds (variable in utility)
			// then sort the subpanel again
			// delete download directory
			// then download the file again and assert
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\typing jim carrey.gif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 1 of 2: typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("typing jim carrey.gif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\typing jim carrey.gif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else {
			fail("The attachment name on the Trade Waste is not of expected value");
		}

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		// clear the content of the download directory
		cleanDownloadDir();
		description = crmGetPreviewDataByLabel(preview, "Description");
		// did this since we won't know if the arrangement might change in the future
		if (description.contains("Site plan 1 of 2: Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller file tiff file.tiff");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller file tiff file.tiff")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 2 of 2: Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("Smaller file tiff file.tiff");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller file tiff file.tiff")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 2 of 2: typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("typing jim carrey.gif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\typing jim carrey.gif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 1 of 2: typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("typing jim carrey.gif");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\typing jim carrey.gif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else {
			fail("The attachment name on the Trade Waste is not of expected value");
		}

		// verify the preview for the 3rd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"),
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
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Atsushi Suedou")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Mitsunari Tokugawa")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(
						getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Suedou, Atsushi")),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Atsushi Suedou"),
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
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":{\"will_discharge\":true,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":false,\"concession_card\":null,\"property_manager\":null,\"payment_method\":null,\"additional_notes\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusNewContact02_sourceID"),
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
	@Test(priority = 8)
	public void verifyCrmSmallBusNewContact03()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Izou", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Izou Motobe",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Office 100 502 Burwood HWY",
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
				getProp("SmallBusNewContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusNewContact03_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact03_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Office 100 502 Burwood HWY Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd,
				"Add04-~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ City/Suburb-~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, State-~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Postcode-~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ AX",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Izou Motobe (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_abn3_abn4"),
				" T/a JaH Trading's ", getProp("test_data_valid_abn3")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Postal)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
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
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "No trade waste will be discharged",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		WebElement preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "No trade waste will be discharged",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " T/a JaH Trading's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
				"no trade waste will be discharged at the property", assertionErrorMsg(getLineNumber()));
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
		WebElement comms = crmSortTableAscWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"),
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
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Izou Motobe")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils
						.isBlank(getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Motobe, Izou")),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusNewContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Izou Motobe"),
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
		softAssertion.assertTrue(emailBody.contains(
				"\"medical_cooling\":false,\"trade_waste\":{\"will_discharge\":false},\"business_identity\":{\"name\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"AX\"},\"property_manager\":null,\"payment_method\":null,\"additional_notes\":\"\",\"acceptance\":[{\"text\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusNewContact03_sourceID"),
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
	@Test(priority = 9)
	public void verifyCrmResiNewContact09()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Koushou", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Koushou Shinogi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Site 1000 505 Burwood HWY",
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
				getProp("ResiNewContact09_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact09_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(acctName, "K Shinogi", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact09_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Site 1000 505 Burwood HWY Vermont South, Victoria, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Site 1000 505 Burwood HWY Vermont South, VIC, 3133",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_17"), ") (Card: ending ", getProp("test_data_16"),
						" / Exp: ", getProp("ResiNewContact09_creditCardExpiryMonth"), "-",
						getProp("ResiNewContact09_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Koushou Shinogi (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiNewContact09_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Driver's License (Abcdef, New South Wales)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Kureha Shinogi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		WebElement preview = crmGetPreview(addContacts, 0);
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Kureha Shinogi (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact09_dateOfBirthAddCont1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Qwerty (Philippines)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Concession Cards", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the subpanel records
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_dva_gold",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Bruce Wayne",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "1212009840",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_dva_gold",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Bruce Wayne",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "1212009840",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 5, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 2).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 3).startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
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
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		// since there are 2 Electricity Life Support Material, we won't know the
		// arrangement next
		String attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.startsWith("Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			if (System.getenv("DELETE_PRESIGN_BUCKET") != null
					&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
						"Sprin't 02 Story 'Board.pdf (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE)",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Sprin't 02 Story 'Board.pdf",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Sprin't 02 Story 'Board.pdf",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (attachmentName.startsWith("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			if (System.getenv("DELETE_PRESIGN_BUCKET") != null
					&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
						"typing jim carrey.gif (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE)",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "typing jim carrey.gif",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 4th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		// since there are 4 Electricity Life Support Material, we won't know the
		// arrangement next
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.startsWith("Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			if (System.getenv("DELETE_PRESIGN_BUCKET") != null
					&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
						"Sprin't 02 Story 'Board.pdf (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE)",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Sprin't 02 Story 'Board.pdf",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Sprin't 02 Story 'Board.pdf",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else if (attachmentName.startsWith("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			if (System.getenv("DELETE_PRESIGN_BUCKET") != null
					&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
						"typing jim carrey.gif (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE)",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
						assertionErrorMsg(getLineNumber()));
			} else {
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "typing jim carrey.gif",
						assertionErrorMsg(getLineNumber()));
				softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "typing jim carrey.gif",
						assertionErrorMsg(getLineNumber()));
			}
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// verify all assertions
			softAssertion.assertAll();
		} else {
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "K Shinogi",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		if (System.getenv("DELETE_PRESIGN_BUCKET") != null
				&& System.getenv("DELETE_PRESIGN_BUCKET").toLowerCase().equals("yes")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Sprin't 02 Story 'Board.pdf (AN ERROR OCCURRED WHEN VERIFYING THE FILE STATUS, SO IT WAS NOT ATTACHED TO THIS NOTE) less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Sprin't 02 Story 'Board.pdf less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Sprin't 02 Story 'Board.pdf",
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Koushou Shinogi")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Kureha Shinogi")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(
						getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Shinogi, Koushou")),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact09_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Koushou Shinogi"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"settlement_date\":null,\"holiday_rental\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\",\"Intermittent Peritoneal Dialysis Machine\"],\"attachments\":[{\"id\":\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[],\"attachments\":[]}],"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_dva_gold\",\"card_holder\":\"Bruce Wayne\",\"card_number\":\"1212009840\",\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
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
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":\"4012888888881881"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact09_sourceID"),
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
	@Test(priority = 10)
	public void verifyCrmResiNewContact10()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Strydum", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Gerry Strydum",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Villa 200-A 20 Baywater DR",
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
				getProp("ResiNewContact10_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact10_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(acctName, "G Strydum", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact10_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Villa 200-A 20 Baywater DR Twin Waters, Victoria, 4564",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Villa 200-A 20 Baywater DR Twin Waters, VIC, 4564",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Gerry Strydum (new contact)", assertionErrorMsg(getLineNumber()));
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
			assertTrue(dateModified.startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
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
		// verify num of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 1,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Kaoru Hanayama",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		WebElement preview = crmGetPreview(addContacts, 0);
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Kaoru Hanayama (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
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
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableAscWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableAscWithinPanel("Notes", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 1, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "G Strydum",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"),
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
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record were created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Gerry Strydum")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "Kaoru Hanayama")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		// verify the fix for bug ticket BBCRM-11903
		softAssertion.assertTrue(
				StringUtils.isBlank(
						getIdOfEmailOrSMS("emails", "Online Request Email for Move In sent to Strydum, Gerry")),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact10_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Gerry Strydum"),
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
		// concession card not enabled so no object would be sent
		softAssertion.assertFalse(
				emailBody.contains("\"postal_address\":null,\"concession_applicable\":false,\"concession_card\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"property_manager\":null,\"payment_method\":null,\"additional_notes\":"),
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
			softAssertion.assertTrue(emailBody.contains("\"extra_data\":\"6011000990139424"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact10_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

}