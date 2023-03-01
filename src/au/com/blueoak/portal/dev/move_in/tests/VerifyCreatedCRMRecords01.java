package au.com.blueoak.portal.dev.move_in.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class VerifyCreatedCRMRecords01 extends MoveInDevBase {

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
	 * - verify the record got created in the Online Request module - verify the
	 * expected values in the online request record view - verify the additional
	 * contacts displayed in the subpanel - verify the additional contacts displayed
	 * in the record view - verify the concession card displayed in the subpanel -
	 * verify the concession card displayed in the preview - verify the notes
	 * created in the subpanel - verify no attachments for the Water Life Support -
	 * verify the attachments for the Electricity Life Support - verify the
	 * attachments for the Concession Card - verify the notes created in the preview
	 * - verify the communications records in the subpanel - verify the content of
	 * the email sent - verify the content of the attachments in the email sent -
	 * verify the content of the SMS - verify the content of the WEB_FORM created -
	 * verify that the ID for the online request in the DB matches the one we had in
	 * the session storage - verify that we have the correct instance ID saved in
	 * the DB
	 * 
	 */
	@Test(priority = 1)
	public void verifyCrmResiNewContact01()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Monkey", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Monkey Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Unit 16 6 Mari ST",
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
				getProp("ResiNewContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact01_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiNewContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiNewContact01_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Residential / Tenant",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "M Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Monkey Luffy's (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 9), "0387643210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 10), "0465320980",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "0212345680",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13),
				getProp("ResiNewContact01_dateOfBirthMain"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14),
				concatStrings("Medicare Card (24287781321, ", getProp("ResiNewContact01_medicareExpiryMainMonth"), "/",
						getProp("ResiNewContact01_medicareExpiryMainYear"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
				"'001 Complex's Unit 16 6 Mari ST Alexandra Headland, Victoria, 90210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 17), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 20)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Monkey Luffy\\'s");
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
		softAssertion.assertEquals(acctName, "M Luffy's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact01_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "'001 Complex's Unit 16 6 Mari ST Alexandra Headland, Victoria, 90210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Unit 7B 11 Innovation Parkway Birtinya, QLD, 4575 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_06"), ") (Card: ending ", getProp("test_data_05"),
						" / Exp: ", getProp("ResiNewContact01_creditCardExpiryMonth"), "-",
						getProp("ResiNewContact01_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Monkey Luffy's (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "0387643210", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "0465320980", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0212345680", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Sekrekt's #001", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiNewContact01_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId,
				concatStrings("Medicare Card (24287781321, ", getProp("ResiNewContact01_medicareExpiryMainMonth"), "/",
						getProp("ResiNewContact01_medicareExpiryMainYear"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Email) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 2,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Roronoa Zoro",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "0800987490",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+0123456789123",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "0702058654",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		// verify 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 1), "Nico Robin's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 2), "+61369854220",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 3), "+61228987540",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 4), "+61432587140",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Roronoa Zoro (new contact)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "0800987490",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+0123456789123",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "0702058654",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact01_dateOfBirthAddContact1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				concatStrings("2428778132 (", getProp("ResiNewContact01_medicareExpiryAddCont1Month"), "/",
						getProp("ResiNewContact01_medicareExpiryAddCont1Year"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Email, SMS", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview record for the 2nd additional contact
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		preview = crmGetPreview(addContacts, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Nico Robin's (new contact)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "+61369854220",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+61228987540",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61432587140",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact01_dateOfBirthAddContact2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				"01235987510 (Australian Capital Territory)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Email, SMS", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Roronoa Zoro");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Roronoa Zoro", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "0800987490", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+0123456789123", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "0702058654", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "Sekrekt's #002", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, getProp("ResiNewContact01_dateOfBirthAddContact1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId,
				concatStrings("Medicare Card (2428778132, ", getProp("ResiNewContact01_medicareExpiryAddCont1Month"),
						"/", getProp("ResiNewContact01_medicareExpiryAddCont1Year"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (Email) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		// verify additional contact record view for 2nd record
		crmClickRecord(addContacts, 1, 1, "Nico Robin's");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName, true);
		addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue, true);
		addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue, true);
		addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue, true);
		softAssertion.assertEquals(addContactName, "Nico Robin's", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "+61369854220", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+61228987540", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61432587140", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "Sekrekt's #003", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, getProp("ResiNewContact01_dateOfBirthAddContact2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Driver's License (01235987510, Australian Capital Territory)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_pen_cnssn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Steven Roger's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "00654876400",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_pen_cnssn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Steven Roger's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "00654876400",
				assertionErrorMsg(getLineNumber()));
		String expYr = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), concatStrings("31/12/", expYr),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// click More notes... link to display all records
		crmClickMoreRecordsSubpanel("Notes");
		crmScrollPage(10, Keys.DOWN);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 7, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 6th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 5, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 5, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 7th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 6, 1), "Additional Note from Customer",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 2).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 3).startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 6, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
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
		cleanDownloadDir();
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		// since there are 2 Electricity Life Support Material, we won't know the
		// arrangement next
		String attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.equals("g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
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
			// verify if image is not corrupted
			// TODO
			// catch the exception java.lang.AssertionError
			// if encountered, refresh the page
			// wait for a couple of seconds (variable in utility)
			// then sort the subpanel again
			// delete download directory
			// then download the file again and assert
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\g'alaxy-'wallpaper.jpeg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.equals("Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
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
		} else {
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 4th record
		cleanDownloadDir();
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		// since there are 4 Electricity Life Support Material, we won't know the
		// arrangement next
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.equals("g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
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
			// verify if image is not corrupted
			// TODO
			// catch the exception java.lang.AssertionError
			// if encountered, refresh the page
			// wait for a couple of seconds (variable in utility)
			// then sort the subpanel again
			// delete download directory
			// then download the file again and assert
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\g'alaxy-'wallpaper.jpeg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.equals("Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
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
		} else {
			fail("There a different attachment in the notes");
		}

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) <b>Payment</b> I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a>",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 6th record
		cleanDownloadDir();
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 5);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Sprin't 02 Story 'Board.pdf less",
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

		// verify the preview for the 7th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 6);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Additional Note from Customer",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M Luffy's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
				"The Quick Brown Fox Jumps Over The Lazy Dog", assertionErrorMsg(getLineNumber()));
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Luffy's, Monkey", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("Monkey")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Monkey Luffy's (0212345680) Roronoa Zoro (0702058654)", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Roronoa Zoro (0702058654) Monkey Luffy's (0212345680)", assertionErrorMsg(getLineNumber()));
		}
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
			String todaySubmitted = getProp("ResiNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Monkey Request Main Contact Last Name: Luffy's Request Postal Address: Unit 7B 11 Innovation Parkway Birtinya, QLD 4575 AU Request Complex Name: '001 Complex's Request Tenancy Address: Unit 16 6 Mari ST Request Tenancy Suburb: Alexandra Headland Request Tenancy State: VIC Request Tenancy Postcode: 90210 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
				getProp("ResiNewContact01_tenantMoveInDateCRM"),
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
		if (to.startsWith("Monkey")) {
			softAssertion.assertEquals(to, "Monkey Luffy's Roronoa Zoro", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Roronoa Zoro Monkey Luffy's", assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("Monkey Luffys (", getProp("test_dummy_email_lower_case"), ") Roronoa Zoro (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Luffy's, Monkey", assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Monkey Request Main Contact Last Name: Luffy's Request Postal Address: Unit 7B 11 Innovation Parkway Birtinya, QLD 4575 AU Request Complex Name: '001 Complex's Request Tenancy Address: Unit 16 6 Mari ST Request Tenancy Suburb: Alexandra Headland Request Tenancy State: VIC Request Tenancy Postcode: 90210 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
				getProp("ResiNewContact01_tenantMoveInDateCRM"),
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Luffy's, Monkey");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Luffy's, Monkey", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Luffy's, Monkey",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Luffy's, Monkey");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Luffy's, Monkey", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Luffy's, Monkey",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Monkey Luffys <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, concatStrings("Roronoa Zoro <", getProp("test_dummy_email_lower_case"), ">, ",
				getProp("test_dummy_email_lower_case")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Luffy's, Monkey",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move In",
				"Request Account Type: Residential", "Request Account Category: Tenant", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Monkey", "Request Main Contact Last Name: Luffy's",
				"Request Postal Address: Unit 7B", "11 Innovation Parkway", "Birtinya, QLD 4575", "AU"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: '001 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 90210",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: Credit Card", "Request Holiday Rental: No",
						concatStrings("Request Move In Date: ", getProp("ResiNewContact01_tenantMoveInDateCRM")),
						"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
						"Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("ResiNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Residential", "Request Account Category: Tenant",
				"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
				"Request Main Contact Salutation: ", "Request Main Contact First Name: Monkey",
				"Request Main Contact Last Name: Luffy's", "Request Postal Address: Unit 7B", "11 Innovation Parkway",
				"Birtinya, QLD 4575", "AU", "Request Complex Name: '001 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 90210",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("ResiNewContact01_tenantMoveInDateCRM")),
				"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
				"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("Monkey")) {
			softAssertion.assertEquals(to, "Monkey Luffy's Roronoa Zoro", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Roronoa Zoro Monkey Luffy's", assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Residential", "Request Account Category: Tenant",
				"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
				"Request Main Contact Salutation:", "Request Main Contact First Name: Monkey",
				"Request Main Contact Last Name: Luffy's", "Request Postal Address: Unit 7B", "11 Innovation Parkway",
				"Birtinya, QLD 4575", "AU", "Request Complex Name: '001 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 90210",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("ResiNewContact01_tenantMoveInDateCRM")),
				"Request Settlement Date:", "Request Move Out Date:", "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
				"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Monkey Luffy's"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		int curYear = getCurrentYear();
		String medicareExpMain = getLastDateOfSpecificMonthYear(MELBOURNE_TIME_ZONE,
				Integer.parseInt(getProp("ResiNewContact01_medicareExpiryMainMonthInt")),
				Integer.parseInt(getProp("ResiNewContact01_medicareExpiryMainYear")), DB_DATE_FORMAT);
		String medicareExpAddCont1 = getLastDateOfSpecificMonthYear(MELBOURNE_TIME_ZONE,
				Integer.parseInt(getProp("ResiNewContact01_medicareExpiryAddCont1MonthInt")),
				Integer.parseInt(getProp("ResiNewContact01_medicareExpiryAddCont1Year")), DB_DATE_FORMAT);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		logDebugMessage(concatStrings("The value of medicareExpMain is '", medicareExpMain, "'"));
		logDebugMessage(concatStrings("The value of medicareExpAddCont1 is '", medicareExpAddCont1, "'"));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"settlement_date\":null,"), assertionErrorMsg(getLineNumber()));
		// there's an intermittent issue here where the value we got from
		// medicareExpMain variable is '<cur_year_plus_one>-03-03'
		// need to look at the method getDateFromSpecificMonthYear
		int curYearUpd = curYear + 1;
		String dateIssue = concatStrings(String.valueOf(curYearUpd), "-03-03");
		if (dateIssue.equals(medicareExpMain)) {
			logDebugMessage("We encountered the intermittent issue in the medicareExpMain date");
			// we just assume here that the expiry month would be Feb (02)
			softAssertion.assertTrue(emailBody.contains(
					concatStrings("\"medicare_card\":{\"medicare_number\":\"2428778132\\/1\",\"expiry_date\":\"",
							String.valueOf(curYearUpd), "-02-2")),
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertTrue(emailBody.contains(
					concatStrings("\"medicare_card\":{\"medicare_number\":\"2428778132\\/1\",\"expiry_date\":\"",
							medicareExpMain, "\"}},\"notification\":")),
					assertionErrorMsg(getLineNumber()));
		}
		// there's an intermittent issue here where the value we got from
		// medicareExpAddCont1 variable is '<cur_year_plus_one>-03-03'
		// need to look at the method getDateFromSpecificMonthYear
		if (dateIssue.equals(medicareExpAddCont1)) {
			logDebugMessage("We encountered the intermittent issue in the medicareExpAddCont1 date");
			// we just assume here that the expiry month would be Feb (02)
			softAssertion.assertTrue(emailBody
					.contains(concatStrings("\"medicare_card\":{\"medicare_number\":\"2428778132\",\"expiry_date\":\"",
							String.valueOf(curYearUpd), "-02-2")),
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertTrue(
					emailBody.contains(
							concatStrings("\"medicare_card\":{\"medicare_number\":\"2428778132\",\"expiry_date\":\"",
									medicareExpAddCont1, "\"}},\"notification\":")),
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_pen_cnssn\",\"card_holder\":\"Steven Roger's\",\"card_number\":\"00654876400\","),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"expiry_year\":"
				+ getProp("ResiNewContact01_creditCardExpiryYearFull") + "},\"authorisation\":{\"text\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact01_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/**
	 * 
	 * - verify the record got created in the Online Request module - verify the
	 * expected values in the online request record view - verify that the Main
	 * Contact got linked into an existing contact since it has the same Name and
	 * Email Address - verify the additional contacts displayed in the subpanel -
	 * verify the additional contacts displayed in the record view - verify that the
	 * Additional Contact got linked into an existing contact since it has the same
	 * Name and Mobile Number - verify the concession card displayed in the subpanel
	 * - verify the concession card displayed in the preview - verify the notes
	 * created in the subpanel - verify no attachments for the Water Life Support -
	 * verify the attachments for the Electricity Life Support - verify the
	 * attachments for the Concession Card - verify the notes created in the preview
	 * - verify the communications records in the subpanel - verify the content of
	 * the email sent - verify the content of the attachments in the email sent -
	 * verify the content of the SMS - verify the content of the WEB_FORM created -
	 * verify that the ID for the online request in the DB matches the one we had in
	 * the session storage - verify that we have the correct instance ID saved in
	 * the DB
	 * 
	 */
	@Test(priority = 2)
	public void verifyCrmResiExistingContact01()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Connell", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Michael O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Apartment 1328 1328 Gold Coast HWY",
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
				getProp("ResiExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiExistingContact01_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiExistingContact01_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Residential / Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Michael O'Connell (Will be merged with contact Mr. Michael O'Connell)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "0898560139",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13),
				"Driver's License (Abc123456780, Queensland)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
				"Apartment 1328 1328 Gold Coast HWY Palm Beach, Queensland, 4221", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Bank Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Michael O\\'Connell");
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
		softAssertion.assertEquals(acctName, "M O'Connell", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact01_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Apartment 1328 1328 Gold Coast HWY Palm Beach, Queensland, 4221",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Disconnected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Apartment 1328 1328 Gold Coast Highway Palm Beach, QLD, 4221",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "Bank Account (BSB: 012340 / Num: 0132889840)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Michael O'Connell (Will be merged with contact Mr. Michael O'Connell)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0898560139", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Sekrekt's-#01", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Driver's License (Abc123456780, Queensland)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Email) Reminders (Email) Account Issues (Email) Account Changes (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Justin O'day",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "1800121655",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+451255556612",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61235298750",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Justin O'day (Will be merged with contact Justin O'Day)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "1800121655",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+451255556612",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61235298750",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Pass123456 (Åland Islands)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Email", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Justin O'day");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Justin O'day", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Justin O'Day)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "1800121655", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+451255556612", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61235298750", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "Sekrekt's-#02", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Passport (Pass123456, Åland Islands)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "Pensioner Concession Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Tony Stark",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "01238578690",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Pensioner Concession Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Tony Stark",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "01238578690",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		crmClickMoreRecordsSubpanel("Notes");
		crmScrollPage(10, Keys.DOWN);
		// verify the number of records in the subpanel, put the correct number
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 7, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 6th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 5, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 5, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 5, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 7th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 6, 1), "Additional Note from Customer",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 2).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 6, 3).startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 6, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Company Name ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Contact Number: Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Address: Unit 402B, 100 Bowen Street, Add-##03, Add-##04, Spring Hill, QLD, 4000, AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 4th record
		cleanDownloadDir();
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Test PNG Type 01.png",
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
		assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Test PNG Type 01.png")),
				concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) <b>Payment</b> I/We hereby authorise SR Global Solutions Pty Ltd ACN 132 951 172 (\"Merchant Warrior\"), Direct Debit User ID Number 397351, to debit my/our account on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the Bulk Electronic Clearing System (BECS) as per the service agreement provided. <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a>",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the previous for the 6th record
		cleanDownloadDir();
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 5);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> Smaller file tiff file.tiff less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "Smaller file tiff file.tiff",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		crmClickRecordExactLinkText("Smaller file tiff file.tiff");
		crmLoad();
		logDebugMessage(concatStrings("We will be waiting for <",
				Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
				"> milli seconds before checking for the downloaded file."));
		pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
		// verify that the downloaded files are correct
		files = getFileNamesFromDir(DOWNLOADS_DIR);
		filesSize = files.size();
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
		// sometimes get a java.lang.NullPointerException
		// so added the jai_imageio-1.1.jar in attempt to fix the issue
		assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller file tiff file.tiff")),
				concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));

		// verify the preview for the 7th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 6);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Additional Note from Customer",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "M O'Connell",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to O'Connell, Michael", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"Michael O'Connell (0898560139)", assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Michael Request Main Contact Last Name: O'Connell Request Postal Address: Apartment 1328 1328 Gold Coast Highway Palm Beach, QLD 4221 Request Complex Name: Request Tenancy Address: Apartment 1328 1328 Gold Coast HWY Request Tenancy Suburb: Palm Beach Request Tenancy State: QLD Request Tenancy Postcode: 4221 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiExistingContact01_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiExistingContact01_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		softAssertion.assertEquals(to, "Michael O'Connell", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)), concatStrings("Michael OConnell (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to O'Connell, Michael", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Michael Request Main Contact Last Name: O'Connell Request Postal Address: Apartment 1328 1328 Gold Coast Highway Palm Beach, QLD 4221 Request Complex Name: Request Tenancy Address: Apartment 1328 1328 Gold Coast HWY Request Tenancy Suburb: Palm Beach Request Tenancy State: QLD Request Tenancy Postcode: 4221 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiExistingContact01_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiExistingContact01_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to O'Connell, Michael");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to O'Connell, Michael", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to O'Connell, Michael",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to O'Connell, Michael");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to O'Connell, Michael", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to O'Connell, Michael",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Michael OConnell <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to O'Connell, Michael",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Residential",
						"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
						"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: Michael", "Request Main Contact Last Name: O'Connell",
						"Request Postal Address: Apartment 1328", "1328 Gold Coast Highway", "Palm Beach, QLD 4221"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Apartment 1328 1328 Gold Coast HWY", "Request Tenancy Suburb: Palm Beach",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4221",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: Bank Account", "Request Holiday Rental: Yes",
						concatStrings("Request Move In Date: ", getProp("ResiExistingContact01_ownerMoveInDateCRM")),
						concatStrings("Request Settlement Date: ",
								getProp("ResiExistingContact01_ownerSettlementDateCRM")),
						"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("ResiExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Michael", "Request Main Contact Last Name: O'Connell",
				"Request Postal Address: Apartment 1328", "1328 Gold Coast Highway", "Palm Beach, QLD 4221",
				"Request Complex Name: ", "Request Tenancy Address: Apartment 1328 1328 Gold Coast HWY",
				"Request Tenancy Suburb: Palm Beach", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4221",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Bank Account", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiExistingContact01_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiExistingContact01_ownerSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		softAssertion.assertEquals(to, "Michael O'Connell", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status:",
				"Request Company Name:", "Request Company Trading Name:", "Request Main Contact Salutation:",
				"Request Main Contact First Name: Michael", "Request Main Contact Last Name: O'Connell",
				"Request Postal Address: Apartment 1328", "1328 Gold Coast Highway", "Palm Beach, QLD 4221",
				"Request Complex Name:", "Request Tenancy Address: Apartment 1328 1328 Gold Coast HWY",
				"Request Tenancy Suburb: Palm Beach", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4221",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Bank Account", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiExistingContact01_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiExistingContact01_ownerSettlementDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Michael O'Connell"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"account_number\":\"0132889840\"},\"authorisation\":{\"text\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiExistingContact01_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/** 
	 * 
	 * 
	 * */
	@Test(priority = 3)
	public void verifyCrmResiExistingContact02()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("tom", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "tom tri ly",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Unit 24 287 Gympie TCE",
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
				getProp("ResiExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiExistingContact02_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiExistingContact02_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Residential / Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "T t ly",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"tom tri ly (Will be merged with contact (Tom) Tri Ly)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 9), "+61460032240",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "+61469941139",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13),
				getProp("ResiExistingContact02_dateOfBirthMain"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14), "Passport (Abc123, Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
				"Unit 24 287 Gympie TCE Noosaville, Queensland, 4566", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 17), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 20)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("tom tri ly");
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
		softAssertion.assertEquals(acctName, "T t ly", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact02_ownerSettlementDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Unit 24 287 Gympie TCE Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Unknown", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Unit 24 287 Gympie Terrace Noosaville, QLD, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_09"), ") (Card: ending ", getProp("test_data_08"),
						" / Exp: ", getProp("ResiExistingContact02_creditCardExpiryMonth"), "-",
						getProp("ResiExistingContact02_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "tom tri ly (Will be merged with contact (Tom) Tri Ly)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61460032240", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61469941139", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "'sekrekts-01", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiExistingContact02_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Passport (Abc123, Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Email) Reminders (Email) Account Issues (Email) Account Changes (Email) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Brad Harrison",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "+61391132260",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+61451532290",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61231132240",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview of the additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Brad Harrison (Will be merged with contact Brad Harrison)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "+61391132260",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+61451532290",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61231132240",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Dfg890 (Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Email", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		crmClickRecord(addContacts, 0, 1, "Brad Harrison");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Brad Harrison", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Brad Harrison)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "+61391132260", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+61451532290", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61231132240", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "'sekrekts-02", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Passport (Dfg890, Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (Email) Account Issues (Email) Account Changes (Email) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		WebElement concession = crmSortTableDescWithinPanel("Concession Cards", 1, false);
		// verify no records created
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// no attachments for the life support created because life support is set to
		// false
		// verify expected number of records
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 3, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));

		// preview the 1st record
		preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "T t ly",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the previous for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "T t ly",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Mr. Michael O'Connell Contact Number: +654980654840 Address: U40, 272 Weyba Road, Noosaville, QLD, 4566, AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the previous for the 3rd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "T t ly",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) <b>Payment</b> I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a>",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to ly, tom tri", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"tom tri ly (+61469941139)", assertionErrorMsg(getLineNumber()));
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
		// verify the fix for ticket BBCRM-10947
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
			String todaySubmitted = getProp("ResiExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: tom tri Request Main Contact Last Name: ly Request Postal Address: Unit 24 287 Gympie Terrace Noosaville, QLD 4566 Request Complex Name: Request Tenancy Address: Unit 24 287 Gympie TCE Request Tenancy Suburb: Noosaville Request Tenancy State: QLD Request Tenancy Postcode: 4566 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiExistingContact02_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiExistingContact02_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		softAssertion.assertEquals(to, "tom tri ly", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("tom tri ly (", getProp("test_dummy_email_lower_case"), ") Brad Harrison (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to ly, tom tri", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBCRM-10947
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
			String todaySubmitted = getProp("ResiExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: tom tri Request Main Contact Last Name: ly Request Postal Address: Unit 24 287 Gympie Terrace Noosaville, QLD 4566 Request Complex Name: Request Tenancy Address: Unit 24 287 Gympie TCE Request Tenancy Suburb: Noosaville Request Tenancy State: QLD Request Tenancy Postcode: 4566 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiExistingContact02_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiExistingContact02_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to ly, tom tri");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to ly, tom tri", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to ly, tom tri",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to ly, tom tri");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to ly, tom tri", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to ly, tom tri",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("tom tri ly <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, concatStrings("Brad Harrison <", getProp("test_dummy_email_lower_case"), ">, ",
				getProp("test_dummy_email_lower_case")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to ly, tom tri",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Residential",
						"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
						"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
						"Request Postal Address: Unit 24", "287 Gympie Terrace", "Noosaville, QLD 4566"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Unit 24 287 Gympie TCE", "Request Tenancy Suburb: Noosaville",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4566",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
						concatStrings("Request Move In Date: ", getProp("ResiExistingContact02_ownerMoveInDateCRM")),
						concatStrings("Request Settlement Date: ",
								getProp("ResiExistingContact02_ownerSettlementDateCRM")),
						"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("ResiExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
				"Request Postal Address: Unit 24", "287 Gympie Terrace", "Noosaville, QLD 4566",
				"Request Complex Name: ", "Request Tenancy Address: Unit 24 287 Gympie TCE",
				"Request Tenancy Suburb: Noosaville", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4566",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiExistingContact02_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiExistingContact02_ownerSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		softAssertion.assertEquals(to, "tom tri ly", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status:",
				"Request Company Name:", "Request Company Trading Name:", "Request Main Contact Salutation:",
				"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
				"Request Postal Address: Unit 24", "287 Gympie Terrace", "Noosaville, QLD 4566",
				"Request Complex Name:", "Request Tenancy Address: Unit 24 287 Gympie TCE",
				"Request Tenancy Suburb: Noosaville", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4566",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiExistingContact02_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiExistingContact02_ownerSettlementDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM tom tri ly"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[{\"id\":\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"name\":\"Smaller file tiff file.tiff\"}]},{\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":true,\"trade_waste\":null,"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiExistingContact02_sourceID"),
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
	public void verifyCrmSmallBusExistingContact01()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Paul", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Paul & Mary Toniolo",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Unit 2 14 Second AVE",
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
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusExistingContact01_propManSettlementDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("SmallBusExistingContact01_propManSettlementDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Small Business / Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 8),
				getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9)),
				"Paul & Mary Toniolo (Will be merged with contact Paul & Mary Toniolo)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 10), "+61298732550",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 11)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), "+61469411390",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
				"Unit 2 14 Second AVE Glenelg East, Queensland, 5045", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Not Required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Paul & Mary Toniolo");
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
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, getProp("test_data_valid_company_name_acn1_acn2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact01_propManSettlementDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Unit 2 14 Second AVE Glenelg East, Queensland, 5045",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Unit 2 14 Second Avenue Glenelg East, QLD, 5045",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_06"), ") (Card: ending ", getProp("test_data_05"),
						" / Exp: ", getProp("SmallBusExistingContact01_creditCardExpiryMonth"), "-",
						getProp("SmallBusExistingContact01_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Paul & Mary Toniolo (Will be merged with contact Paul & Mary Toniolo)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61298732550", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61469411390", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_acn1_acn2"), " ", getProp("test_data_valid_acn1")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Postal) Reminders (SMS) Account Issues (SMS) Account Changes (SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Emma Harding-Grimmond",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "0298732500",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+61236985000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61469411390",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Emma Harding-Grimmond (Will be merged with contact Emma Harding-Grimmond)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "0298732500",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+61236985000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61469411390",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 6)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 7));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "Email", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Emma Harding-Grimmond");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Emma Harding-Grimmond", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Emma Harding-Grimmond)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "0298732500", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+61236985000", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61469411390", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./AddCont1",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (SMS) Account Issues (SMS) Account Changes (SMS) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 5, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 2).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 3).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 2).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 3).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 2).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 3).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 2).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 3).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 2).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 3).startsWith(
				getProp("SmallBusExistingContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		// clear the content of the download directory
		cleanDownloadDir();
		String description = crmGetPreviewDataByLabel(preview, "Description");
		// did this since we won't know if the arrangement might change in the future
		if (description.contains("Site plan 2 of 2: Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: Sprin't 02 Story 'Board.pdf",
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
		} else if (description.contains("Site plan 1 of 2: Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: Sprin't 02 Story 'Board.pdf",
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
		} else if (description.contains("Site plan 2 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 1 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
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
		if (description.contains("Site plan 2 of 2: Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: Sprin't 02 Story 'Board.pdf",
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
		} else if (description.contains("Site plan 1 of 2: Sprin't 02 Story 'Board.pdf")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: Sprin't 02 Story 'Board.pdf",
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
		} else if (description.contains("Site plan 2 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 2 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 1 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' Site plan 1 of 2: planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
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
				getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Comp Name Contact Number: 1 to 6 pm Address: Unit 301, 192 Marine Parade, Coolangatta, QLD, 4225, AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_acn1_acn2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) <b>Payment</b> I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a>",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Toniolo, Paul & Mary", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("Paul")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Paul & Mary Toniolo (+61469411390) Emma Harding-Grimmond (+61469411390)",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Emma Harding-Grimmond (+61469411390) Paul & Mary Toniolo (+61469411390)",
					assertionErrorMsg(getLineNumber()));
		}
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
		// verify the fix for bug ticket ticket BBCRM-10947
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
			String todaySubmitted = getProp("SmallBusExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: THE HAIRHOUSE WAREHOUSE PTY. LTD. Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Paul & Mary Request Main Contact Last Name: Toniolo Request Postal Address: Unit 2 14 Second Avenue Glenelg East, QLD 5045 Request Complex Name: Request Tenancy Address: Unit 2 14 Second AVE Request Tenancy Suburb: Glenelg East Request Tenancy State: QLD Request Tenancy Postcode: 5045 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("SmallBusExistingContact01_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact01_propManSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		if (to.startsWith("Paul")) {
			softAssertion.assertEquals(to, "Paul & Mary Toniolo Emma Harding-Grimmond",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Emma Harding-Grimmond Paul & Mary Toniolo",
					assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)), concatStrings("Paul & Mary Toniolo (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Toniolo, Paul & Mary", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("SmallBusExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: THE HAIRHOUSE WAREHOUSE PTY. LTD. Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Paul & Mary Request Main Contact Last Name: Toniolo Request Postal Address: Unit 2 14 Second Avenue Glenelg East, QLD 5045 Request Complex Name: Request Tenancy Address: Unit 2 14 Second AVE Request Tenancy Suburb: Glenelg East Request Tenancy State: QLD Request Tenancy Postcode: 5045 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("SmallBusExistingContact01_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact01_propManSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Toniolo, Paul & Mary");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Toniolo, Paul & Mary", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Toniolo, Paul & Mary",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				" Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Toniolo, Paul & Mary");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Toniolo, Paul & Mary", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Toniolo, Paul & Mary",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to,
				concatStrings("Paul & Mary Toniolo <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Toniolo, Paul & Mary",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Small Business",
						"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
						concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn1_acn2")),
						"Request Company Trading Name: ", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: Paul & Mary", "Request Main Contact Last Name: Toniolo",
						"Request Postal Address: Unit 2", "14 Second Avenue", "Glenelg East, QLD 5045"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Unit 2 14 Second AVE", "Request Tenancy Suburb: Glenelg East",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 5045", "Request Electricity Life Support: ",
				"Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: Credit Card",
				"Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact01_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact01_propManSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("SmallBusExistingContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn1_acn2")),
				"Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Paul & Mary", "Request Main Contact Last Name: Toniolo",
				"Request Postal Address: Unit 2", "14 Second Avenue", "Glenelg East, QLD 5045",
				"Request Complex Name: ", "Request Tenancy Address: Unit 2 14 Second AVE",
				"Request Tenancy Suburb: Glenelg East", "Request Tenancy State: QLD", "Request Tenancy Postcode: 5045",
				"Request Electricity Life Support: ", "Request Water Life Support: ",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact01_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact01_propManSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("Paul")) {
			softAssertion.assertEquals(to, "Paul & Mary Toniolo Emma Harding-Grimmond",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Emma Harding-Grimmond Paul & Mary Toniolo",
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status:",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn1_acn2")),
				"Request Company Trading Name:", "Request Main Contact Salutation:",
				"Request Main Contact First Name: Paul & Mary", "Request Main Contact Last Name: Toniolo",
				"Request Postal Address: Unit 2", "14 Second Avenue", "Glenelg East, QLD 5045", "Request Complex Name:",
				"Request Tenancy Address: Unit 2 14 Second AVE", "Request Tenancy Suburb: Glenelg East",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 5045", "Request Electricity Life Support:",
				"Request Water Life Support:", "Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact01_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact01_propManSettlementDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				" Emails", "BlueBilling");
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Paul & Mary Toniolo"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,\"property_manager\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusExistingContact01_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 * 
	 */
	@Test(priority = 5)
	public void verifyCrmSmallBusExistingContact02()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Roger", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "roger buckle",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "ATM 2 14 Smith ST",
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
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusExistingContact02_propManMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("SmallBusExistingContact02_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Small Business / Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " (T/a Trading's \"Inc.\")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 8),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9)),
				"roger buckle (Will be merged with contact Prof. Roger Buckle)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 10), "+61398743250",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "+61236985000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), "+61426037890",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
				"ATM 2 14 Smith ST Collingwood, Victoria, 3066", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("roger buckle");
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
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName,
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact02_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "ATM 2 14 Smith ST Collingwood, Victoria, 3066",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Disconnected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "ATM 2 14 Smith Street Collingwood, VIC, 3066",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_06"), ") (Card: ending ", getProp("test_data_05"),
						" / Exp: ", getProp("SmallBusExistingContact02_creditCardExpiryMonth"), "-",
						getProp("SmallBusExistingContact02_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "roger buckle (Will be merged with contact Prof. Roger Buckle)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61398743250", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "+61236985000", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61426037890", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Sekretoe'#01", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_abn1_abn2"),
				" T/a Trading's \"Inc.\" ", getProp("test_data_valid_abn1")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (Email) Reminders (None) Account Issues (None) Account Changes (None) Marketing (SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "betty Xu",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "+61811308850",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+61719312650",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "1300444956",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 5)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		// because of bug ticket BBCRM-11782, the Salutation/Title not included in the
		// view
		// expected value should be Ms. Betty Xu, so we assert the current value for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"betty Xu (Will be merged with contact Betty Xu)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "+61811308850",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+61719312650",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "1300444956",
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
		softAssertion.assertEquals(bills, "Postal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "betty Xu");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactSecretCode = getDisplayedText(onlinerequestrecordview.moveInAddContactSecretCodeValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "betty Xu", assertionErrorMsg(getLineNumber()));
		// because of bug ticket BBCRM-11782, the Salutation/Title not included in the
		// view
		// expected value should be Ms. Betty Xu, so we assert the current value for now
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Betty Xu)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "+61811308850", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+61719312650", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "1300444956", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactSecretCode, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (Postal) Reminders (SMS) Account Issues (SMS) Account Changes (SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 5, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 2).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 3).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 2).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 3).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 2).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 3).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 2).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 3).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 2).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 3).startsWith(
				getProp("SmallBusExistingContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No trade waste equipment installed Business activity is Other Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' No site plans uploaded",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
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

		// verify the preview for the 4th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Contact Number: Company Contact Num ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Address: Star City",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " T/a Trading's \"Inc.\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
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
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to buckle, roger", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("roger")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"roger buckle (+61426037890) betty Xu (1300444956)", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"betty Xu (1300444956) roger buckle (+61426037890)", assertionErrorMsg(getLineNumber()));
		}
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
		// verify the fix for bug ticket ticket BBCRM-10947
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
			String todaySubmitted = getProp("SmallBusExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_abn1_abn2"),
				" Request Company Trading Name: Trading's \"Inc.\" Request Main Contact Salutation: Request Main Contact First Name: roger Request Main Contact Last Name: buckle Request Postal Address: ATM 2 14 Smith Street Collingwood, VIC 3066 Request Complex Name: Request Tenancy Address: ATM 2 14 Smith ST Request Tenancy Suburb: Collingwood Request Tenancy State: VIC Request Tenancy Postcode: 3066 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("SmallBusExistingContact02_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact02_propManMoveInDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		if (to.startsWith("roger")) {
			softAssertion.assertEquals(to, "roger buckle betty Xu", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "betty Xu roger buckle", assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)), concatStrings("roger buckle (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to buckle, roger", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("SmallBusExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_abn1_abn2"),
				" Request Company Trading Name: Trading's \"Inc.\" Request Main Contact Salutation: Request Main Contact First Name: roger Request Main Contact Last Name: buckle Request Postal Address: ATM 2 14 Smith Street Collingwood, VIC 3066 Request Complex Name: Request Tenancy Address: ATM 2 14 Smith ST Request Tenancy Suburb: Collingwood Request Tenancy State: VIC Request Tenancy Postcode: 3066 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("SmallBusExistingContact02_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact02_propManMoveInDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to buckle, roger");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to buckle, roger", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to buckle, roger",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to buckle, roger");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to buckle, roger", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to buckle, roger",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("roger buckle <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to buckle, roger",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Small Business",
						"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
						concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn1_abn2")),
						"Request Company Trading Name: Trading's \"Inc.\"", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: roger", "Request Main Contact Last Name: buckle",
						"Request Postal Address: ATM 2", "14 Smith Street", "Collingwood, VIC 3066"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: ATM 2 14 Smith ST", "Request Tenancy Suburb: Collingwood",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3066",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: Credit Card",
				"Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("SmallBusExistingContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn1_abn2")),
				"Request Company Trading Name: Trading's \"Inc.\"", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: roger", "Request Main Contact Last Name: buckle",
				"Request Postal Address: ATM 2", "14 Smith Street", "Collingwood, VIC 3066", "Request Complex Name: ",
				"Request Tenancy Address: ATM 2 14 Smith ST", "Request Tenancy Suburb: Collingwood",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3066",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("roger")) {
			softAssertion.assertEquals(to, "roger buckle betty Xu", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "betty Xu roger buckle", assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status:",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn1_abn2")),
				"Request Company Trading Name: Trading's \"Inc.\"", "Request Main Contact Salutation:",
				"Request Main Contact First Name: roger", "Request Main Contact Last Name: buckle",
				"Request Postal Address: ATM 2", "14 Smith Street", "Collingwood, VIC 3066", "Request Complex Name:",
				"Request Tenancy Address: ATM 2 14 Smith ST", "Request Tenancy Suburb: Collingwood",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3066",
				"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
				"Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusExistingContact02_propManMoveInDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM roger buckle"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusExistingContact02_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 * 
	 */
	@Test(priority = 6)
	public void verifyCrmSmallBusNewContact01()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("glenn", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Glenn O'brien",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Tenancy 20 20 Bella Vista ST",
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
				getProp("SmallBusNewContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusNewContact01_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("SmallBusNewContact01_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("SmallBusNewContact01_ownerMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Small Business / Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" (T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./)"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 8),
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9)),
				"Glenn O'brien (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 10), "+61311300856",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "+61401331008",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), "+61288500321",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
				"Tenancy 20 20 Bella Vista ST Heathcote, New South Wales, 2233", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Not Required",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "None Active",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Glenn O\\'brien");
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
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
				" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact01_ownerMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Tenancy 20 20 Bella Vista ST Heathcote, New South Wales, 2233",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Connected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Not Required", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Owner", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Tenancy 20 20 Bella Vista ST Heathcote, NSW, 2233",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Glenn O'brien (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61311300856", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "+61401331008", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "+61288500321", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Email, SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Roshan Britto",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "+61311300856",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+451267286623",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61288500321",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Roshan Britto (new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "+61311300856",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+451267286623",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61288500321",
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
		softAssertion.assertEquals(bills, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Roshan Britto");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Roshan Britto", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "+61311300856", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+451267286623", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61288500321", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactSecretCodeValueList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (Email) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"No trade waste equipment installed Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days 'not known' Discharge hours 'not known' No site plans uploaded",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"),
						" T/a ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to O'brien, Glenn", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("Glenn")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Glenn O'brien (+61288500321) Roshan Britto (+61288500321)", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Roshan Britto (+61288500321) Glenn O'brien (+61288500321)", assertionErrorMsg(getLineNumber()));
		}
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
		// verify the fix for bug ticket ticket BBCRM-10947
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
			String todaySubmitted = getProp("SmallBusNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_acn3_acn4"),
				" Request Company Trading Name: ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Request Main Contact Salutation: Request Main Contact First Name: Glenn Request Main Contact Last Name: O'brien Request Postal Address: Tenancy 20 20 Bella Vista ST Heathcote, NSW 2233 Request Complex Name: Request Tenancy Address: Tenancy 20 20 Bella Vista ST Request Tenancy Suburb: Heathcote Request Tenancy State: NSW Request Tenancy Postcode: 2233 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("SmallBusNewContact01_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusNewContact01_ownerMoveInDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		if (to.startsWith("Glenn")) {
			softAssertion.assertEquals(to, "Glenn O'brien Roshan Britto", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Roshan Britto Glenn O'brien", assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("Glenn Obrien (", getProp("test_dummy_email_lower_case"), ") Roshan Britto (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to O'brien, Glenn", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("SmallBusNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_acn3_acn4"),
				" Request Company Trading Name: ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./ Request Main Contact Salutation: Request Main Contact First Name: Glenn Request Main Contact Last Name: O'brien Request Postal Address: Tenancy 20 20 Bella Vista ST Heathcote, NSW 2233 Request Complex Name: Request Tenancy Address: Tenancy 20 20 Bella Vista ST Request Tenancy Suburb: Heathcote Request Tenancy State: NSW Request Tenancy Postcode: 2233 Request Electricity Life Support: Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("SmallBusNewContact01_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusNewContact01_ownerMoveInDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to O'brien, Glenn");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to O'brien, Glenn", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to O'brien, Glenn",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to O'brien, Glenn");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to O'brien, Glenn", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to O'brien, Glenn",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Glenn Obrien <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, concatStrings("Roshan Britto <", getProp("test_dummy_email_lower_case"), ">, ",
				getProp("test_dummy_email_lower_case")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to O'brien, Glenn",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move In",
				"Request Account Type: Small Business", "Request Account Category: Owner",
				"Request Account VIP Status: ",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
				"Request Company Trading Name: ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Glenn", "Request Main Contact Last Name: O'brien",
				"Request Postal Address: Tenancy 20 20 Bella Vista ST", "Heathcote, NSW 2233"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Tenancy 20 20 Bella Vista ST", "Request Tenancy Suburb: Heathcote",
				"Request Tenancy State: NSW", "Request Tenancy Postcode: 2233", "Request Electricity Life Support: ",
				"Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: ", "Request Holiday Rental: No",
						concatStrings("Request Move In Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
						concatStrings("Request Settlement Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
						"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("SmallBusNewContact01_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Small Business", "Request Account Category: Owner",
				"Request Account VIP Status: ",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
				"Request Company Trading Name: ~!@#$%^&*()_+|`-=\\{}[]:\";'?,./", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Glenn", "Request Main Contact Last Name: O'brien",
				"Request Postal Address: Tenancy 20 20 Bella Vista ST", "Heathcote, NSW 2233", "Request Complex Name: ",
				"Request Tenancy Address: Tenancy 20 20 Bella Vista ST", "Request Tenancy Suburb: Heathcote",
				"Request Tenancy State: NSW", "Request Tenancy Postcode: 2233", "Request Electricity Life Support: ",
				"Request Water Life Support: ", "Request Direct Debit: ", "Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("Glenn")) {
			softAssertion.assertEquals(to, "Glenn O'brien Roshan Britto", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Roshan Britto Glenn O'brien", assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Small Business", "Request Account Category: Owner",
				"Request Account VIP Status:",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
				"Request Company Trading Name: ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./", "Request Main Contact Salutation:",
				"Request Main Contact First Name: Glenn", "Request Main Contact Last Name: O'brien",
				"Request Postal Address: Tenancy 20 20 Bella Vista ST", "Heathcote, NSW 2233", "Request Complex Name:",
				"Request Tenancy Address: Tenancy 20 20 Bella Vista ST", "Request Tenancy Suburb: Heathcote",
				"Request Tenancy State: NSW", "Request Tenancy Postcode: 2233", "Request Electricity Life Support:",
				"Request Water Life Support:", "Request Direct Debit:", "Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusNewContact01_ownerMoveInDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Glenn O'brien"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusNewContact01_sourceID"),
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
	public void verifyCrmSmallBusExistingContact03()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Dominic", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Dominic Blank",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3),
				"Villa Happy Valley Retirement Village 75 Davis CDS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 4), "New",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 5), "Waiting Verification",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 6), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 7), "Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetListOrSubpanelValue(onlineReq, 0, 8),
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusExistingContact03_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("SmallBusExistingContact03_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Small Business / Owner",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 8),
				getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9)),
				"Dominic Blank (Will be merged with contact Dominic Blank)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 10), "0829821210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "+09278169824",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 12)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
					"9 Garden Walk Villa Happy Valley Retirement Village 75 Davis CDS NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
					"dummy complex Villa Happy Valley Retirement Village 75 Davis CDS NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Required (Electricity, Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Bank Account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Melanie Banks");
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
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, getProp("test_data_valid_company_name_abn3_abn4"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact03_ownerSettlementDate"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(reqTenancy,
					"9 Garden Walk Villa Happy Valley Retirement Village 75 Davis CDS NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(reqTenancy,
					"dummy complex Villa Happy Valley Retirement Village 75 Davis CDS NORWOOD, South Australia, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity, Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Owner", assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(postalAdd,
					"9 Garden Walk Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA, 5067",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(postalAdd,
					"dummy complex Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA, 5067",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(directDebit, "Bank Account (BSB: 000010 / Num: 000012200)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Dominic Blank (Will be merged with contact Dominic Blank)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "0829821210", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAHrsPhone, "+09278169824", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_abn3_abn4"), " ", getProp("test_data_valid_abn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Melanie Banks",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 2), "0829821210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 3), "+09278169824",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+8361085965230",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Melanie Banks (Will be merged with contact Melanie Banks)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "0829821210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "+09278169824",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+8361085965230",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 6)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 7));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Postal, Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Postal, Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Postal, Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Melanie Banks");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactAfterHrsPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValue,
				true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Melanie Banks", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Melanie Banks)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBusPhone, "0829821210", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactAfterHrsPhone, "+09278169824", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+8361085965230", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactSecretCodeValueList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (Postal, Email, SMS) Account Issues (Postal, Email, SMS) Account Changes (Postal, Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 5, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 2).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 3).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 2).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 3).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 2).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 3).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 2).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 3).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 2).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 3).startsWith(
				getProp("SmallBusExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		// clear the content of the download directory
		cleanDownloadDir();
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)), concatStrings(
				"Installed equipments are gReAsE TrAp, pLaStEr aRrEsToR, gReAsE ExTrAcToR - FiLtEr, hOlDiNg tAnK, oIl/pLaTe sEpArAtOr, cOoLiNg pIt, dIlUtIoN PiT, eFfLuEnT TaNk fInAl, oIl tRaP-GaRaGe sUmP, eFfLuEnT PuMp, sOlId sEtTlEmEnT PiT, lInT TrAp, bAsKeT TrAp, \"Other\" Equipment's, sIlVeR ReCoVeRy uNiT Business activity is Retail food business Max instantaneous flow rate is 9;877*520000 Litres / second Max daily discharge volume is 10;098*052000 Litres Discharge start date is ",
				getProp("SmallBusExistingContact03_tradeWasteDischargeStartDateCRM"),
				" Discharge days are SUN, MON, TUE, WED, THU, FRI, SAT Discharge hours (24 hour format) between 09:59 and 22:59 Site plan : g'alaxy-'wallpaper.jpeg less"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
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

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5),
				"No medical certificate has been attached.", assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
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
		// clear the content of the download directory
		cleanDownloadDir();
		String description = crmGetPreviewDataByRowFluidNum(preview, 5);
		if (description.startsWith("Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Smaller tif file.tif", assertionErrorMsg(getLineNumber()));
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
			files = getFileNamesFromDir(DOWNLOADS_DIR);
			filesSize = files.size();
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
			// sometimes get a java.lang.NullPointerException
			// so added the jai_imageio-1.1.jar in attempt to fix the issue
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Smaller tif file.tif")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.startsWith("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
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
			files = getFileNamesFromDir(DOWNLOADS_DIR);
			filesSize = files.size();
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
			fail("The attachment in the description is not of expected one");
		}

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		// clear the content of the download directory
		cleanDownloadDir();
		description = crmGetPreviewDataByRowFluidNum(preview, 5);
		if (description.startsWith("Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Smaller tif file.tif", assertionErrorMsg(getLineNumber()));
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
			files = getFileNamesFromDir(DOWNLOADS_DIR);
			filesSize = files.size();
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
		} else if (description.startsWith("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_abn3_abn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"typing jim carrey.gif", assertionErrorMsg(getLineNumber()));
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
			files = getFileNamesFromDir(DOWNLOADS_DIR);
			filesSize = files.size();
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
			fail("The attachment in the description is not of expected one");
		}

		String mainWindow = crmGetWindowHandle();

		crmScrollPage(10, Keys.UP);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Blank, Dominic", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBCRM-11822
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"Melanie Banks (+8361085965230)", assertionErrorMsg(getLineNumber()));
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
		// verify the fix for bug ticket ticket BBCRM-10947
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
			String todaySubmitted = getProp("SmallBusExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = "";
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
					getProp("test_data_valid_company_name_abn3_abn4"),
					" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Dominic Request Main Contact Last Name: Blank Request Postal Address: 9 Garden Walk Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA 5067 Request Complex Name: 9 Garden Walk Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS Request Tenancy Suburb: NORWOOD Request Tenancy State: SA Request Tenancy Postcode: 5067 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: No Request Move In Date: ",
					getProp("SmallBusExistingContact03_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("SmallBusExistingContact03_ownerSettlementDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
					getProp("test_data_valid_company_name_abn3_abn4"),
					" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Dominic Request Main Contact Last Name: Blank Request Postal Address: dummy complex Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA 5067 Request Complex Name: dummy complex Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS Request Tenancy Suburb: NORWOOD Request Tenancy State: SA Request Tenancy Postcode: 5067 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: No Request Move In Date: ",
					getProp("SmallBusExistingContact03_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("SmallBusExistingContact03_ownerSettlementDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		assertFalse(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		softAssertion.assertEquals(to, "Melanie Banks", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("Dominic Blank (", getProp("test_dummy_email_lower_case"), ") Melanie Banks (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Blank, Dominic", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("SmallBusExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
					getProp("test_data_valid_company_name_abn3_abn4"),
					" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Dominic Request Main Contact Last Name: Blank Request Postal Address: 9 Garden Walk Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA 5067 Request Complex Name: 9 Garden Walk Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS Request Tenancy Suburb: NORWOOD Request Tenancy State: SA Request Tenancy Postcode: 5067 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: No Request Move In Date: ",
					getProp("SmallBusExistingContact03_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("SmallBusExistingContact03_ownerSettlementDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Owner Request Account VIP Status: Request Company Name: ",
					getProp("test_data_valid_company_name_abn3_abn4"),
					" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Dominic Request Main Contact Last Name: Blank Request Postal Address: dummy complex Villa Happy Valley Retirement Village 75 Davis Cul-de-sac NORWOOD, SA 5067 Request Complex Name: dummy complex Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS Request Tenancy Suburb: NORWOOD Request Tenancy State: SA Request Tenancy Postcode: 5067 Request Electricity Life Support: In Use Request Water Life Support: In Use Request Direct Debit: Bank Account Request Holiday Rental: No Request Move In Date: ",
					getProp("SmallBusExistingContact03_ownerMoveInDateCRM"), " Request Settlement Date: ",
					getProp("SmallBusExistingContact03_ownerSettlementDateCRM"),
					" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		assertFalse(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the subject is clickable and it would open the Email record view
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Blank, Dominic");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Blank, Dominic", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Blank, Dominic",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Blank, Dominic");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Blank, Dominic", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Blank, Dominic",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Dominic Blank <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, concatStrings("Melanie Banks <", getProp("test_dummy_email_lower_case"), ">, ",
				getProp("test_dummy_email_lower_case")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Blank, Dominic",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			p2ExpectedValues = new ArrayList<>(
					Arrays.asList("Request Type: Move In", "Request Account Type: Small Business",
							"Request Account Category: Owner", "Request Account VIP Status: ",
							concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
							"Request Company Trading Name: ", "Request Main Contact Salutation: ",
							"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
							"Request Postal Address: 9 Garden Walk Villa Happy Valley Retirement Village",
							"75 Davis Cul-de-sac", "NORWOOD, SA 5067"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			p2ExpectedValues = new ArrayList<>(
					Arrays.asList("Request Type: Move In", "Request Account Type: Small Business",
							"Request Account Category: Owner", "Request Account VIP Status: ",
							concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
							"Request Company Trading Name: ", "Request Main Contact Salutation: ",
							"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
							"Request Postal Address: dummy complex Villa Happy Valley Retirement Village",
							"75 Davis Cul-de-sac", "NORWOOD, SA 5067"));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: 9 Garden Walk",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: dummy complex",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use"));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: Bank Account",
				"Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact03_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("SmallBusExistingContact03_ownerSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("SmallBusExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
					"Request Type: Move In", "Request Account Type: Small Business", "Request Account Category: Owner",
					"Request Account VIP Status: ",
					concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
					"Request Company Trading Name: ", "Request Main Contact Salutation: ",
					"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
					"Request Postal Address: 9 Garden Walk Villa Happy Valley Retirement Village",
					"75 Davis Cul-de-sac", "NORWOOD, SA 5067", "Request Complex Name: 9 Garden Walk",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
					"Request Direct Debit: ", "Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact03_ownerMoveInDateCRM")),
					concatStrings("Request Settlement Date: ",
							getProp("SmallBusExistingContact03_ownerSettlementDateCRM")),
					"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
					reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
					"Services All Fields:", "Regards,", "--BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
					"Request Type: Move In", "Request Account Type: Small Business", "Request Account Category: Owner",
					"Request Account VIP Status: ",
					concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
					"Request Company Trading Name: ", "Request Main Contact Salutation: ",
					"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
					"Request Postal Address: dummy complex Villa Happy Valley Retirement Village",
					"75 Davis Cul-de-sac", "NORWOOD, SA 5067", "Request Complex Name: dummy complex",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
					"Request Direct Debit: ", "Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact03_ownerMoveInDateCRM")),
					concatStrings("Request Settlement Date: ",
							getProp("SmallBusExistingContact03_ownerSettlementDateCRM")),
					"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
					reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
					"Services All Fields:", "Regards,", "--BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		softAssertion.assertEquals(to, "Melanie Banks", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In", "Request Type: Move In",
					"Request Account Type: Small Business", "Request Account Category: Owner",
					"Request Account VIP Status:",
					concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
					"Request Company Trading Name:", "Request Main Contact Salutation:",
					"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
					"Request Postal Address: 9 Garden Walk Villa Happy Valley Retirement Village",
					"75 Davis Cul-de-sac", "NORWOOD, SA 5067", "Request Complex Name: 9 Garden Walk",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
					"Request Direct Debit: Bank Account", "Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact03_ownerMoveInDateCRM")),
					concatStrings("Request Settlement Date: ",
							getProp("SmallBusExistingContact03_ownerSettlementDateCRM")),
					"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
					reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
					"Services All Fields:", "Regards,", "--BlueOak Support less"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In", "Request Type: Move In",
					"Request Account Type: Small Business", "Request Account Category: Owner",
					"Request Account VIP Status:",
					concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_abn3_abn4")),
					"Request Company Trading Name:", "Request Main Contact Salutation:",
					"Request Main Contact First Name: Dominic", "Request Main Contact Last Name: Blank",
					"Request Postal Address: dummy complex Villa Happy Valley Retirement Village",
					"75 Davis Cul-de-sac", "NORWOOD, SA 5067", "Request Complex Name: dummy complex",
					"Request Tenancy Address: Villa Happy Valley Retirement Village 75 Davis CDS",
					"Request Tenancy Suburb: NORWOOD", "Request Tenancy State: SA", "Request Tenancy Postcode: 5067",
					"Request Electricity Life Support: In Use", "Request Water Life Support: In Use",
					"Request Direct Debit: Bank Account", "Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact03_ownerMoveInDateCRM")),
					concatStrings("Request Settlement Date: ",
							getProp("SmallBusExistingContact03_ownerSettlementDateCRM")),
					"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
					reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
					"Services All Fields:", "Regards,", "--BlueOak Support less"));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Dominic Blank"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Oxygen Concentrator\"],\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"service_type\":\"WATER\",\"required\":true,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"trade_waste\":{\"will_discharge\":true,\"installed_equipment\":[\"gReAsE TrAp\",\"pLaStEr aRrEsToR\",\"gReAsE ExTrAcToR - FiLtEr\",\"hOlDiNg tAnK\",\"oIl\\/pLaTe sEpArAtOr\",\"cOoLiNg pIt\",\"dIlUtIoN PiT\",\"eFfLuEnT TaNk fInAl\",\"oIl tRaP-GaRaGe sUmP\",\"eFfLuEnT PuMp\",\"sOlId sEtTlEmEnT PiT\",\"lInT TrAp\",\"bAsKeT TrAp\",\"\\\"Other\\\" Equipment's\",\"sIlVeR ReCoVeRy uNiT\"],\"business_activity\":\"Retail food business\",\"discharge_details\":{\"max_flow_rate\":9877.52,\"max_daily_volume\":10098.052,\"timing\":{\"start_date\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"days\":[\"SUN\",\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\"],\"start_hour\":\"09:59\",\"end_hour\":\"22:59\"},\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,"),
				assertionErrorMsg(getLineNumber()));
		// verify no direct debit authorisation got sent
		softAssertion.assertTrue(emailBody.contains(
				"\"payment_method\":{\"bank_account\":{\"account_name\":\"Dominic Blank\",\"account_bsb\":\"000010\",\"account_number\":\"000012200\"}},\"additional_notes\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusExistingContact03_sourceID"),
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
	public void verifyCrmSmallBusExistingContact04()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Yeak", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Mey Yeak Jr",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Site #01 Great Eastern HWY",
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
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("SmallBusExistingContact04_propManMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("SmallBusExistingContact04_propManMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Small Business / Property Manager or Letting Agent", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 8),
				getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9)),
				"Mey Yeak Jr (Will be merged with contact Mey Yeak Jr.)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 11)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), "0295663212",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 13)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 14)),
				"Goldfields Woodlands National Park Site #01 Great Eastern HWY Victoria Rock, Western Australia, 6429",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 15), "Required (Electricity)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "None Active",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 17)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Mey Yeak Jr");
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
		String company = getDisplayedText(onlinerequestrecordview.moveInCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveInNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveInDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveInDateModifiedValue, true);
		softAssertion.assertEquals(acctName, getProp("test_data_valid_company_name_acn3_acn4"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact04_propManMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy,
				"Goldfields Woodlands National Park Site #01 Great Eastern HWY Victoria Rock, Western Australia, 6429",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(supplyStateReported, "Unknown", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Electricity)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Property Manager or Letting Agent",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Site #01 Great Eastern HWY Victoria Rock, WA, 6429",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit, "None Active", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Mey Yeak Jr (Will be merged with contact Mey Yeak Jr.)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "0295663212", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_acn3_acn4"), " ", getProp("test_data_valid_acn3")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (SMS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Mohammad Erfanian-Nozar Sr.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61700858730",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"Mohammad Erfanian-Nozar Sr. (Will be merged with contact Mohammad Erfanian-Nozar Sr)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61700858730",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 6)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 7));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Mohammad Erfanian-Nozar Sr.");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Mohammad Erfanian-Nozar Sr.", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact,
				"(Will be merged with contact Mohammad Erfanian-Nozar Sr)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactBusPhone), assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValueList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactSecretCodeValueList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertEquals(addContactMobPhone, "+61700858730", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		crmScrollPage(6, Keys.DOWN);
		// verify no concession subpanel is not displayed
		WebElement concession = crmFindSubPanel("Concession Cards");
		softAssertion.assertTrue(Objects.isNull(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// click More notes... link to display all records
		crmClickMoreRecordsSubpanel("Notes");
		crmScrollPage(10, Keys.DOWN);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 6, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 0, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Will discharge trade waste",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 1, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 2, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 3, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 4, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 6th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 5, 1), "Electricity Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 5, 2).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(crmGetListOrSubpanelValue(notes, 5, 3).startsWith(
				getProp("SmallBusExistingContact04_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 5, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		// clear the content of the download directory
		cleanDownloadDir();
		String description = crmGetPreviewDataByLabel(preview, "Description");
		if (description.contains("Site plan 1 of 2: Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 1 of 2: Smaller tif file.tif",
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
		} else if (description.contains("Site plan 2 of 2: Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 2 of 2: Smaller tif file.tif",
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
		} else if (description.contains("Site plan 2 of 2: Test PNG Type 01.png")) {
			// just put a pause to potentially fix the java.io.EOFException: Unexpected end
			// of ZLIB input stream
			pauseSeleniumExecution(10000);
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 2 of 2: Test PNG Type 01.png",
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
			// got an java.io.EOFException: Unexpected end of ZLIB input stream
			// that's because the downloaded file is incomplete and the server was still
			// trying to transfer some data but the file was already downloaded.
			// When I checked the screenshot for the preview, it shows that the image
			// preview
			// just shows half of the image.
			// verify if image is not corrupted
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\Test PNG Type 01.png")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.contains("Site plan 1 of 2: Test PNG Type 01.png")) {
			// just put a pause to potentially fix the java.io.EOFException: Unexpected end
			// of ZLIB input stream
			pauseSeleniumExecution(10000);
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 1 of 2: Test PNG Type 01.png",
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
		} else {
			fail("The attached files are not of expected ones");
		}

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		// clear the content of the download directory
		cleanDownloadDir();
		description = crmGetPreviewDataByLabel(preview, "Description");
		if (description.contains("Site plan 1 of 2: Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 1 of 2: Smaller tif file.tif",
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
		} else if (description.contains("Site plan 2 of 2: Smaller tif file.tif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 2 of 2: Smaller tif file.tif",
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
		} else if (description.contains("Site plan 2 of 2: Test PNG Type 01.png")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 2 of 2: Test PNG Type 01.png",
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
		} else if (description.contains("Site plan 1 of 2: Test PNG Type 01.png")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Will discharge trade waste",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Installed equipments are Grease trap, \"Other\" Equipment's Business activity is Retail motor vehicle Max instantaneous flow rate 'not known' Max daily discharge volume 'not known' Discharge start date 'not known' Discharge days is SAT Discharge hours (24 hour format) between 12:00 and 11:00 Site plan 1 of 2: Test PNG Type 01.png",
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
		} else {
			fail("The attached files are not of expected ones");
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
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a> (DECLINED) I/We will add enquiries@blueoak.com.au to my email contacts or white list where required less",
				assertionErrorMsg(getLineNumber()));
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Property Manager Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"Manager: Contact Number: Address: Testing Add-#03 ~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./, Beverly Hills, California, 90210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		// clear the content of the download directory
		cleanDownloadDir();
		description = crmGetPreviewDataByRowFluidNum(preview, 5);
		if (description.startsWith("planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.startsWith("Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
		} else {
			fail("The attachment in the description is not of expected one");
		}

		// verify the preview for the 6th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 5);
		// clear the content of the download directory
		cleanDownloadDir();
		description = crmGetPreviewDataByRowFluidNum(preview, 5);
		if (description.startsWith("planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (description.startsWith("Smaller file tiff file.tiff")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Electricity Life Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
					getProp("test_data_valid_company_name_acn3_acn4"), assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"Smaller file tiff file.tiff", assertionErrorMsg(getLineNumber()));
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
		} else {
			fail("The attachment in the description is not of expected one");
		}

		String mainWindow = crmGetWindowHandle();

		crmScrollPage(10, Keys.UP);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Yeak Jr, Mey", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"),
				assertionErrorMsg(getLineNumber()));
		// make sure that status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("Mey")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Mey Yeak Jr (0295663212) Mohammad Erfanian-Nozar Sr. (+61700858730)",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Mohammad Erfanian-Nozar Sr. (+61700858730) Mey Yeak Jr (0295663212)",
					assertionErrorMsg(getLineNumber()));
		}
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
		// verify the fix for bug ticket ticket BBCRM-10947
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
			String todaySubmitted = getProp("SmallBusExistingContact04_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Property Manager or Letting Agent Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_acn3_acn4"),
				" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Mey Request Main Contact Last Name: Yeak Jr Request Postal Address: Site #01 Great Eastern HWY Victoria Rock, WA 6429 Request Complex Name: Goldfields Woodlands National Park Request Tenancy Address: Site #01 Great Eastern HWY Request Tenancy Suburb: Victoria Rock Request Tenancy State: WA Request Tenancy Postcode: 6429 Request Electricity Life Support: In Use Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("SmallBusExistingContact04_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact04_propManSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		// verify that the subject is clickable and it would open the SMS record view
		clickExactLinkNameFromElement(preview, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		String to = getDisplayedText(smsrecordview.recipients, true);
		if (to.startsWith("Mey")) {
			softAssertion.assertEquals(to, "Mey Yeak Jr Mohammad Erfanian-Nozar Sr.",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Mohammad Erfanian-Nozar Sr. Mey Yeak Jr",
					assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("Mohammad Erfanian-Nozar Sr. (", getProp("test_dummy_email_lower_case"), ") ",
						getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Yeak Jr, Mey", assertionErrorMsg(getLineNumber()));
		messageLinkName = getMessageLinkName(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		softAssertion.assertEquals(messageLinkName, "Show message body", assertionErrorMsg(getLineNumber()));
		// verify that the message body is not yet displayed
		softAssertion.assertFalse(
				isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Sent",
				assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("SmallBusExistingContact04_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Small Business Request Account Category: Property Manager or Letting Agent Request Account VIP Status: Request Company Name: ",
				getProp("test_data_valid_company_name_acn3_acn4"),
				" Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Mey Request Main Contact Last Name: Yeak Jr Request Postal Address: Site #01 Great Eastern HWY Victoria Rock, WA 6429 Request Complex Name: Goldfields Woodlands National Park Request Tenancy Address: Site #01 Great Eastern HWY Request Tenancy Suburb: Victoria Rock Request Tenancy State: WA Request Tenancy Postcode: 6429 Request Electricity Life Support: In Use Request Water Life Support: Request Direct Debit: Request Holiday Rental: No Request Move In Date: ",
				getProp("SmallBusExistingContact04_propManMoveInDateCRM"), " Request Settlement Date: ",
				getProp("SmallBusExistingContact04_propManSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Yeak Jr, Mey");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Yeak Jr, Mey", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Yeak Jr, Mey",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the field is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Yeak Jr, Mey");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Yeak Jr, Mey", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Yeak Jr, Mey",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to,
				concatStrings("Mohammad Erfanian-Nozar Sr. <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Yeak Jr, Mey",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Small Business",
						"Request Account Category: Property Manager or Letting Agent", "Request Account VIP Status: ",
						concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
						"Request Company Trading Name: ", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: Mey", "Request Main Contact Last Name: Yeak Jr",
						"Request Postal Address: Site #01 Great Eastern HWY", "Victoria Rock, WA 6429"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Complex Name: Goldfields Woodlands National Park",
						"Request Tenancy Address: Site #01 Great Eastern HWY", "Request Tenancy Suburb: Victoria Rock",
						"Request Tenancy State: WA", "Request Tenancy Postcode: 6429",
						"Request Electricity Life Support: In Use", "Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: ",
				"Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact04_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact04_propManSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("SmallBusExistingContact04_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Property Manager or Letting Agent", "Request Account VIP Status: ",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
				"Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Mey", "Request Main Contact Last Name: Yeak Jr",
				"Request Postal Address: Site #01 Great Eastern HWY", "Victoria Rock, WA 6429",
				"Request Complex Name: Goldfields Woodlands National Park",
				"Request Tenancy Address: Site #01 Great Eastern HWY", "Request Tenancy Suburb: Victoria Rock",
				"Request Tenancy State: WA", "Request Tenancy Postcode: 6429",
				"Request Electricity Life Support: In Use", "Request Water Life Support: ", "Request Direct Debit: ",
				"Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact04_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact04_propManSettlementDateCRM")),
				"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time: ", "Services Action Taken: ",
				"Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("Mey")) {
			softAssertion.assertEquals(to, "Mey Yeak Jr Mohammad Erfanian-Nozar Sr.",
					assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "Mohammad Erfanian-Nozar Sr. Mey Yeak Jr",
					assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Small Business",
				"Request Account Category: Property Manager or Letting Agent", "Request Account VIP Status:",
				concatStrings("Request Company Name: ", getProp("test_data_valid_company_name_acn3_acn4")),
				"Request Company Trading Name:", "Request Main Contact Salutation:",
				"Request Main Contact First Name: Mey", "Request Main Contact Last Name: Yeak Jr",
				"Request Postal Address: Site #01 Great Eastern HWY", "Victoria Rock, WA 6429",
				"Request Complex Name: Goldfields Woodlands National Park",
				"Request Tenancy Address: Site #01 Great Eastern HWY", "Request Tenancy Suburb: Victoria Rock",
				"Request Tenancy State: WA", "Request Tenancy Postcode: 6429",
				"Request Electricity Life Support: In Use", "Request Water Life Support:", "Request Direct Debit:",
				"Request Holiday Rental: No",
				concatStrings("Request Move In Date: ", getProp("SmallBusExistingContact04_propManMoveInDateCRM")),
				concatStrings("Request Settlement Date: ",
						getProp("SmallBusExistingContact04_propManSettlementDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("SmallBusExistingContact04_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Mey Yeak Jr"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"ELECTRICITY\",\"required\":true,\"equipment\":[\"Ventilator for Life Support\"],\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"service_type\":\"WATER\",\"required\":false,\"equipment\":[],\"attachments\":[]}],\"medical_cooling\":false,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"trade_waste\":{\"will_discharge\":true,\"installed_equipment\":[\"Grease trap\",\"\\\"Other\\\" Equipment's\"],\"business_activity\":\"Retail motor vehicle\",\"discharge_details\":{\"max_flow_rate\":null,\"timing\":{\"days\":[\"SAT\"],\"start_hour\":\"12:00\",\"end_hour\":\"11:00\"},\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(emailBody.contains("\"concession_applicable\":false,\"concession_card\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"payment_method\":null,\"additional_notes\":\"\",\"acceptance\":[{\"text\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("SmallBusExistingContact04_sourceID"),
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
	public void verifyCrmResiExistingContact03()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Tsoubos", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Peter Tsoubos",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Suite Apt-#01 Yilka DR",
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
				getProp("ResiExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiExistingContact03_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiExistingContact03_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiExistingContact03_tenantMoveInDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Residential / Tenant",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "P Tsoubos",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Peter Tsoubos (Will be merged with contact Mr. Peter Tsoubos)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 11), "1300852060",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 12), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13),
				getProp("ResiExistingContact03_dateOfBirthMain"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14),
				concatStrings("Medicare Card (2428778132, ", getProp("ResiExistingContact03_medicareExpiryMainMonth"),
						"/", getProp("ResiExistingContact03_medicareExpiryMainYear"), ")"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
					"Cosmo Newbery Community Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
					"dummy complex Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
					"Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Required (Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 17), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 20)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Peter Tsoubos");
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
		softAssertion.assertEquals(acctName, "P Tsoubos", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact03_tenantMoveInDate"),
				assertionErrorMsg(getLineNumber()));
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			softAssertion.assertEquals(reqTenancy,
					"Cosmo Newbery Community Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			softAssertion.assertEquals(reqTenancy,
					"dummy complex Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			softAssertion.assertEquals(reqTenancy, "Suite Apt-#01 Yilka DR Cosmo Newbery, Western Australia, 6440",
					assertionErrorMsg(getLineNumber()));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		softAssertion.assertEquals(supplyStateReported, "Disconnected", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Tenant", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Suite Apt-#01 Yilka DR Cosmo Newbery, WA, 6440",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_13"), ") (Card: ending ", getProp("test_data_12"),
						" / Exp: ", getProp("ResiExistingContact03_creditCardExpiryMonth"), "-",
						getProp("ResiExistingContact03_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Peter Tsoubos (Will be merged with contact Mr. Peter Tsoubos)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(mobPhone, "1300852060", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiExistingContact03_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId,
				concatStrings("Medicare Card (2428778132, ", getProp("ResiExistingContact03_medicareExpiryMainMonth"),
						"/", getProp("ResiExistingContact03_medicareExpiryMainYear"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Additional Contacts", false), 2,
				assertionErrorMsg(getLineNumber()));
		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "elizabeth treonze",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "0800838420",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		// verify 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 1), "James Grasso",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 1, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 1, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 4), "+61425228522",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 1, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 1)),
				"elizabeth treonze (Will be merged with contact Elizabeth Treonze)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "0800838420",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiExistingContact03_dateOfBirthAddContact1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "XyZ123876480 (Western Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "Email", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview record for the 2nd additional contact
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		preview = crmGetPreview(addContacts, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "James Grasso (new contact)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61425228522",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiExistingContact03_dateOfBirthAddContact2"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				concatStrings("24287781321 (", getProp("ResiExistingContact03_medicareExpiryAddCont2Month"), "/",
						getProp("ResiExistingContact03_medicareExpiryAddCont2Year"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 13));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "elizabeth treonze");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "elizabeth treonze", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(Will be merged with contact Elizabeth Treonze)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "0800838420", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, getProp("ResiExistingContact03_dateOfBirthAddContact1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Driver's License (XyZ123876480, Western Australia)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (Email) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValueList),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactSecretCodeValueList),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify all assertions
		softAssertion.assertAll();
		clickElementAction(onlinerequestrecordview.moveInAddContactCloseBtn);
		pauseSeleniumExecution(3000);

		// verify additional contact record view for 2nd record
		crmClickRecord(addContacts, 1, 1, "James Grasso");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName, true);
		addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue, true);
		addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue, true);
		softAssertion.assertEquals(addContactName, "James Grasso", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61425228522", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, getProp("ResiExistingContact03_dateOfBirthAddContact2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId,
				concatStrings("Medicare Card (24287781321, ",
						getProp("ResiExistingContact03_medicareExpiryAddCont2Month"), "/",
						getProp("ResiExistingContact03_medicareExpiryAddCont2Year"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValueList),
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
		// verify no records displayed
		softAssertion.assertFalse(crmIsDataAvailableInTable(concession), assertionErrorMsg(getLineNumber()));

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 2, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "P Tsoubos",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"eicar.com.pdf (FILE CONTAINED VIRUS SO IT WAS NOT ATTACHED TO THIS NOTE AND WAS DELETED)",
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
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Request Acceptance Details",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "P Tsoubos",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a>",
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
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Tsoubos, Peter", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		if (normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)).startsWith("Peter")) {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"Peter Tsoubos (1300852060) James Grasso (+61425228522)", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
					"James Grasso (+61425228522) Peter Tsoubos (1300852060)", assertionErrorMsg(getLineNumber()));
		}
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
			String todaySubmitted = getProp("ResiExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: Cosmo Newbery Community Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: dummy complex Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		if (to.startsWith("Peter")) {
			softAssertion.assertEquals(to, "Peter Tsoubos James Grasso", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "James Grasso Peter Tsoubos", assertionErrorMsg(getLineNumber()));
		}
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				concatStrings("Peter Tsoubos (", getProp("test_dummy_email_lower_case"), ") James Grasso (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Tsoubos, Peter", assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: Cosmo Newbery Community Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: dummy complex Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			messageBodyExp = concatStrings(
					"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Tenant Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Peter Request Main Contact Last Name: Tsoubos Request Postal Address: Suite Apt-#01 Yilka DR Cosmo Newbery, WA 6440 Request Complex Name: Request Tenancy Address: Suite Apt-#01 Yilka DR Request Tenancy Suburb: Cosmo Newbery Request Tenancy State: WA Request Tenancy Postcode: 6440 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: No Request Move In Date: ",
					getProp("ResiExistingContact03_tenantMoveInDateCRM"),
					" Request Settlement Date: Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
					dateSubmitUpd,
					" Request Completed Date and Time: Services Action Taken: Services All Fields: Regards, --BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		assertEquals(messageBodyAct, messageBodyExp, assertionErrorMsg(getLineNumber()));
		clickHideMessageBody(crmGetPreviewWebElementByRowFluidNum(preview, 5));
		// turn off implicit wait since we're checking for an element
		// that is no longer displayed to avoid waiting extra seconds.
		setImplicitWait(0);
		// verify that the message got hidden
		assertFalse(isElementDisplayed(getMessageBodyElement(crmGetPreviewWebElementByRowFluidNum(preview, 5)), 0),
				assertionErrorMsg(getLineNumber()));
		// verify that the subject is clickable and it would open the Email record view
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Tsoubos, Peter");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Tsoubos, Peter", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Tsoubos, Peter",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Tsoubos, Peter");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Tsoubos, Peter", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Tsoubos, Peter",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Peter Tsoubos <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, concatStrings("James Grasso <", getProp("test_dummy_email_lower_case"), ">, ",
				getProp("test_dummy_email_lower_case")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Tsoubos, Peter",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move In",
				"Request Account Type: Residential", "Request Account Category: Tenant", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Peter", "Request Main Contact Last Name: Tsoubos",
				"Request Postal Address: Suite Apt-#01 Yilka DR", "Cosmo Newbery, WA 6440"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: Cosmo Newbery Community",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: dummy complex",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use"));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: Credit Card", "Request Holiday Rental: No",
						concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
						"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
						"Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("ResiExistingContact03_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
					"Request Type: Move In", "Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
					"Request Main Contact Salutation: ", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name: Cosmo Newbery Community",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
					"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
					"Request Type: Move In", "Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
					"Request Main Contact Salutation: ", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name: dummy complex",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
					"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
					"Request Type: Move In", "Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
					"Request Main Contact Salutation: ", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name: ",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support: ",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
					"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		if (to.startsWith("Peter")) {
			softAssertion.assertEquals(to, "Peter Tsoubos James Grasso", assertionErrorMsg(getLineNumber()));
		} else {
			softAssertion.assertEquals(to, "James Grasso Peter Tsoubos", assertionErrorMsg(getLineNumber()));
		}
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = null;
		if (getPortalType().equals("standalone") && getPopulateDataMethod().equals("manual")
				|| getPortalType().equals("embedded") && getPopulateDataMethod().equals("manual")) {
			expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In", "Request Type: Move In",
					"Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
					"Request Main Contact Salutation:", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name: Cosmo Newbery Community",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support:",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date:", "Request Move Out Date:", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
					"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("thirdPartyPrefill")) {
			expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In", "Request Type: Move In",
					"Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
					"Request Main Contact Salutation:", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name: dummy complex",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support:",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date:", "Request Move Out Date:", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
					"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		} else if (getPortalType().equals("embedded") && getPopulateDataMethod().equals("urlPrefill")
				|| getPortalType().equals("standalone") && getPopulateDataMethod().equals("urlPrefill")) {
			expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In", "Request Type: Move In",
					"Request Account Type: Residential", "Request Account Category: Tenant",
					"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
					"Request Main Contact Salutation:", "Request Main Contact First Name: Peter",
					"Request Main Contact Last Name: Tsoubos", "Request Postal Address: Suite Apt-#01 Yilka DR",
					"Cosmo Newbery, WA 6440", "Request Complex Name:",
					"Request Tenancy Address: Suite Apt-#01 Yilka DR", "Request Tenancy Suburb: Cosmo Newbery",
					"Request Tenancy State: WA", "Request Tenancy Postcode: 6440", "Request Electricity Life Support:",
					"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
					"Request Holiday Rental: No",
					concatStrings("Request Move In Date: ", getProp("ResiExistingContact03_tenantMoveInDateCRM")),
					"Request Settlement Date:", "Request Move Out Date:", "Request Current Stage: NEW",
					"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
					"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		} else {
			fail(concatStrings("Verify the assertion(s) for this configuration. Portal type '", getPortalType(),
					"' and Populate Data method '", getPopulateDataMethod(), "'"));
		}
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Peter Tsoubos"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		String medicareExpMain = getLastDateOfSpecificMonthYear(MELBOURNE_TIME_ZONE,
				Integer.parseInt(getProp("ResiExistingContact03_medicareExpiryMainMonthInt")),
				Integer.parseInt(getProp("ResiExistingContact03_medicareExpiryMainYear")), DB_DATE_FORMAT);
		String medicareExpAddCont2 = getLastDateOfSpecificMonthYear(MELBOURNE_TIME_ZONE,
				Integer.parseInt(getProp("ResiExistingContact03_medicareExpiryAddCont2MonthInt")),
				Integer.parseInt(getProp("ResiExistingContact03_medicareExpiryAddCont2Year")), DB_DATE_FORMAT);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		logDebugMessage(concatStrings("The value of medicareExpMain is '", medicareExpMain, "'"));
		logDebugMessage(concatStrings("The value of medicareExpAddCont2 is '", medicareExpAddCont2, "'"));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[\"Chronic Positive Airways Pressure Respirator\",\"Ventilator for Life Support\"],\"attachments\":[{\"id\":\""),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains("\"settlement_date\":null,"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,\"account_contacts\":[{"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(concatStrings(
				"\"medicare_card\":{\"medicare_number\":\"2428778132\",\"expiry_date\":\"", medicareExpMain, "\"")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody
				.contains(concatStrings("\"medicare_card\":{\"medicare_number\":\"2428778132\\/1\",\"expiry_date\":\"",
						medicareExpAddCont2, "\"")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"existing_contact_id\":\"\"}],\"concession_applicable\":true,\"concession_card\":null,\"property_manager\":null,\"payment_method\":{"),
				assertionErrorMsg(getLineNumber()));
		// verify no direct debit authorisation is sent by the portal
		softAssertion.assertTrue(emailBody.contains(
				",\"expiry_year\":" + Integer.parseInt(getProp("ResiExistingContact03_creditCardExpiryYearFull"))
						+ "}},\"additional_notes\":"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiExistingContact03_sourceID"),
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
	public void verifyCrmResiNewContact02()
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
		WebElement onlineReq = crmGetListViewTableWithSearch("Rashideh", false, 1, false, false);

		softAssertion.assertEquals(crmGetNumOfRecordsInListViewOrSubpanel(onlineReq), 1, concatStrings(
				"Record not created in the Online Request list view, see line number [", getLineNumber(), "]"));
		// verify first that there's a result returned before checking further
		softAssertion.assertAll();

		// verify the list view values
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 2), "Rashideh Maroun",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 3), "Villa 5 5 7 Albion RD",
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
				getProp("ResiNewContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(onlineReq, 0, 9),
				getProp("ResiNewContact02_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));

		// verify the preview
		WebElement preview = crmGetPreview(onlineReq, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.startsWith(crmGetPreviewDataByRowFluidNum(preview, 2),
				getProp("ResiNewContact02_dateSubmittedSlash")), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3),
				getProp("ResiNewContact02_ownerSettlementDate"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 4)),
				"New / Waiting Verification Normal / Normal", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				"Residential / Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "R Maroun",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8)),
				"Rashideh Maroun (Will be merged with contact Rashideh Maroun)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 9)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 10)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 11)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 12)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 13),
				getProp("ResiNewContact02_dateOfBirthMain"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 14), "Passport (aDv123, Philippines)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 15)),
				"Villa 5 5 7 Albion RD Box Hill, Victoria, 3128", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 16), "Required (Water)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 17), "Credit Card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 18)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 19)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 20)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions for list view and preview
		softAssertion.assertAll();

		crmClickRecordExactLinkText("Move In");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String commsId = getCommsIDToUpdateFromSMS("Iain Stuart");
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
		softAssertion.assertEquals(acctName, "R Maroun", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move In", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact02_ownerSettlementDate"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Villa 5 5 7 Albion RD Box Hill, Victoria, 3128",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(supplyStateReported), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(lifeSupport, "Required (Water)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctCategory, "Holiday Rental or Letting", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(postalAdd, "Villa 5 5 7 Albion RD Box Hill, VIC, 3128",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(directDebit,
				concatStrings("Credit Card (", getProp("test_data_17"), ") (Card: ending ", getProp("test_data_16"),
						" / Exp: ", getProp("ResiNewContact02_creditCardExpiryMonth"), "-",
						getProp("ResiNewContact02_creditCardExpiryYearFull"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(refAcct, "No", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-11887, the contact got linked
		// so we assert the current behavior for now
		softAssertion.assertEquals(contactName, "Rashideh Maroun (Will be merged with contact Rashideh Maroun)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAHrsPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(mobPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiNewContact02_dateOfBirthMain"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Passport (aDv123, Philippines)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications,
				"Bills (None) Reminders (None) Account Issues (None) Account Changes (None) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		try {
			assertTrue(dateModified.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 1), "Iain Stuart",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(addContacts, 0, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 4), "+61425228522",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(addContacts, 0, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));

		// verify the preview record for the 1st additional contact
		preview = crmGetPreview(addContacts, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Iain Stuart (new contact)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 3)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "+61425228522",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 5), getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6),
				getProp("ResiNewContact02_dateOfBirthAddContact1"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 7)),
				assertionErrorMsg(getLineNumber()));
		String bills = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 8));
		String reminders = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 9));
		String acctIssues = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 10));
		String acctChanges = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 11));
		String marketing = normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 12));
		softAssertion.assertEquals(bills, "None", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reminders, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctIssues, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctChanges, "Email, SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(marketing, "None", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify additional contact record view for 1st record
		addContacts = crmSortTableDescWithinPanel("Additional Contacts", 1, false);
		crmClickRecord(addContacts, 0, 1, "Iain Stuart");
		crmLoad();
		clickElementAction(onlinerequestrecordview.moveInAddContactShowMoreLink);
		String addContactName = getDisplayedText(onlinerequestrecordview.moveInAddContactName, true);
		String addContactExistingContact = getDisplayedText(onlinerequestrecordview.moveInAddContactExistingContactName,
				true);
		String addContactBusPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactBusPhoneValue, true);
		String addContactMobPhone = getDisplayedText(onlinerequestrecordview.moveInAddContactMobPhoneValue, true);
		String addContactBirthdate = getDisplayedText(onlinerequestrecordview.moveInAddContactBirthdateValue, true);
		String addContactPersonalId = getDisplayedText(onlinerequestrecordview.moveInAddContactPersonalIDValue, true);
		String addContactDateModifiedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateModifiedByValue,
				true);
		String addContactEmailAdd = getDisplayedText(onlinerequestrecordview.moveInAddContactEmailAddValue, true);
		String addContactNotifications = getDisplayedText(onlinerequestrecordview.moveInAddContactNotifications, true);
		String addContactDateCreatedBy = getDisplayedText(onlinerequestrecordview.moveInAddContactDateCreatedByValue,
				true);
		softAssertion.assertEquals(addContactName, "Iain Stuart", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactExistingContact, "(new contact)", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(addContactBusPhone), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactMobPhone, "+61425228522", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactBirthdate, getProp("ResiNewContact02_dateOfBirthAddContact1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactPersonalId, "Not Specified", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateModifiedBy, "by", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactEmailAdd, getProp("test_dummy_email_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactNotifications,
				"Notifications Bills (None) Reminders (Email, SMS) Account Issues (Email, SMS) Account Changes (Email, SMS) Marketing (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(addContactDateCreatedBy, "by", assertionErrorMsg(getLineNumber()));
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveInAddContactAfterHrsPhoneValueList),
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
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 1), "vic_health_card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 2), "Steven Roger's Jr.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(concession, 0, 3), "01321687450",
				assertionErrorMsg(getLineNumber()));

		// verify the preview
		preview = crmGetPreview(concession, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "vic_health_card",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "Steven Roger's Jr.",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "01321687450",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 5)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(6, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records in the subpanel
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Notes", false), 5, assertionErrorMsg(getLineNumber()));
		// verify the 1st record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 0, 1), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 2).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 0, 3).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 1, 1), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 2).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 1, 3).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 2, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 2).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 2, 3).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 2, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 4th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 3, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 2).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 3, 3).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 3, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 5th record in the subpanel
		softAssertion.assertEquals(crmGetListOrSubpanelValue(notes, 4, 1), "Concession Card Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 2).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				crmGetListOrSubpanelValue(notes, 4, 3).startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(notes, 4, 4)),
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(notes, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Water Life Support Material",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
				"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the preview for the 2nd record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Direct Debit Authorisation",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
				"(ACCEPTED) <b>Payment</b> I/We hereby authorise to debit my/our credit card on behalf of ALZHEIMER'S australia ACT incorporated ABN 66 342 708 600 through the payment gateway as per the service agreement provided. Please check our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a>",
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
		// clear the downloads first
		cleanDownloadDir();
		String attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.contains("planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> planet_in_deep_space-wallpaper-1920x1080.jpg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.contains("g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> g'alaxy-'wallpaper.jpeg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
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
		} else if (attachmentName.contains("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> typing jim carrey.gif less",
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
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 4th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 3);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		// clear the downloads first
		cleanDownloadDir();
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.contains("planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> planet_in_deep_space-wallpaper-1920x1080.jpg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.contains("g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> g'alaxy-'wallpaper.jpeg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
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
		} else if (attachmentName.contains("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> typing jim carrey.gif less",
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
			fail("There's a different attachment in the notes");
		}

		// verify the preview for the 5th record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 4);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		// clear the downloads first
		cleanDownloadDir();
		attachmentName = crmGetPreviewDataByLabel(preview, "Description");
		if (attachmentName.contains("planet_in_deep_space-wallpaper-1920x1080.jpg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> planet_in_deep_space-wallpaper-1920x1080.jpg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7),
					"planet_in_deep_space-wallpaper-1920x1080.jpg", assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
			// let's verify the picture if it's valid and if it was downloaded
			crmClickRecordExactLinkText("planet_in_deep_space-wallpaper-1920x1080.jpg");
			crmLoad();
			logDebugMessage(concatStrings("We will be waiting for <",
					Integer.toString(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES),
					"> milli seconds before checking for the downloaded file."));
			pauseSeleniumExecution(CRM_WAIT_TIME_BEFORE_CHECKING_DOWNLOADED_FILES);
			// verify that the downloaded files are correct
			List<String> files = getFileNamesFromDir(DOWNLOADS_DIR);
			int filesSize = files.size();
			softAssertion.assertEquals(files.get(0), "planet_in_deep_space-wallpaper-1920x1080.jpg",
					assertionErrorMsg(getLineNumber()));
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
			assertTrue(isImageValid(concatStrings(DOWNLOADS_DIR, "\\planet_in_deep_space-wallpaper-1920x1080.jpg")),
					concatStrings("Downloaded image is corrupted. Check line number [", getLineNumber(), "]"));
		} else if (attachmentName.contains("g'alaxy-'wallpaper.jpeg")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> g'alaxy-'wallpaper.jpeg less",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 6), "Global (Primary)",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 7), "g'alaxy-'wallpaper.jpeg",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 8)),
					assertionErrorMsg(getLineNumber()));
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
		} else if (attachmentName.contains("typing jim carrey.gif")) {
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Concession Card Support Material",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), "R Maroun",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4), "Not Specified",
					assertionErrorMsg(getLineNumber()));
			softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 5)),
					"(ACCEPTED) 'Company' is seeking your consent to collect and use your name, address, payment and concession card information from the Department of Human Services/Department of Veteran Affairs to validate your concession eligibility. This authority is only effective for the period that you are a customer of 'Company' and you can revoke your consent at any time by contacting us. If you do not provide your consent you may not be eligible for the concession. You are required to notify us and your card issuer of any changes in your circumstances which may affect your eligibility for a concession.<br/><br/>Do you understand and consent to 'Company' accessing your information held by the Department of Human Services/Department of Veteran Affairs? Visit our <a href=\"https://www.blueoak.com.au/trade_waste_pricing.html\" target=\"_blank\">Terms and Conditions</a> typing jim carrey.gif less",
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
			fail("There's a different attachment in the notes");
		}

		String mainWindow = crmGetWindowHandle();

		crmScrollPage(10, Keys.UP);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the Communications subpanel
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				"The number of expected record(s) in the Communications subpanel is incorrect");

		// verify the 1st record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "SMS", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1), "Online Request SMS for Move In",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedSMS = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedSMS.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 0, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 2nd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 1),
				"Online Request Email for Move In sent to Maroun, Rashideh", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 1, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedEmail = crmGetListOrSubpanelValue(comms, 1, 3);
		softAssertion.assertTrue(dateCreatedEmail.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(crmGetListOrSubpanelValue(comms, 1, 4)),
				assertionErrorMsg(getLineNumber()));
		// verify the 3rd record
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 1),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 2), "Sent", assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 2, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 2, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));

		// verify the preview for the 1st record
		preview = crmGetPreview(comms, 0);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "SMS",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 2), "Enrgy Int12",
				assertionErrorMsg(getLineNumber()));
		// verify the fix for bug ticket BBCRM-11822
		softAssertion.assertEquals(normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)),
				"Iain Stuart (+61425228522)", assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiNewContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String messageBodyExp = concatStrings(
				"Message Hide message body Online Request SMS for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Rashideh Request Main Contact Last Name: Maroun Request Postal Address: Villa 5 5 7 Albion RD Box Hill, VIC 3128 Request Complex Name: Request Tenancy Address: Villa 5 5 7 Albion RD Request Tenancy Suburb: Box Hill Request Tenancy State: VIC Request Tenancy Postcode: 3128 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiNewContact02_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiNewContact02_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		softAssertion.assertEquals(to, "Iain Stuart", assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 2nd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 1);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(
				normalizeSpaces(crmGetPreviewDataByRowFluidNum(preview, 3)), concatStrings("Iain Stuart (",
						getProp("test_dummy_email_lower_case"), ") ", getProp("test_dummy_email_lower_case")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				"Online Request Email for Move In sent to Maroun, Rashideh", assertionErrorMsg(getLineNumber()));
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
			String todaySubmitted = getProp("ResiNewContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		messageBodyExp = concatStrings(
				"Message Hide message body Online Request Email for Move In Request Type: Move In Request Account Type: Residential Request Account Category: Holiday Rental or Letting Request Account VIP Status: Request Company Name: Request Company Trading Name: Request Main Contact Salutation: Request Main Contact First Name: Rashideh Request Main Contact Last Name: Maroun Request Postal Address: Villa 5 5 7 Albion RD Box Hill, VIC 3128 Request Complex Name: Request Tenancy Address: Villa 5 5 7 Albion RD Request Tenancy Suburb: Box Hill Request Tenancy State: VIC Request Tenancy Postcode: 3128 Request Electricity Life Support: Request Water Life Support: In Use Request Direct Debit: Credit Card Request Holiday Rental: Yes Request Move In Date: ",
				getProp("ResiNewContact02_ownerMoveInDateCRM"), " Request Settlement Date: ",
				getProp("ResiNewContact02_ownerSettlementDateCRM"),
				" Request Move Out Date: Request Current Stage: NEW Request Current State: REACHED Request Submitted Date and Time: ",
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
		clickExactLinkNameFromElement(preview, "Online Request Email for Move In sent to Maroun, Rashideh");
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Maroun, Rashideh", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		String emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Maroun, Rashideh",
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the preview for the 3rd record
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		preview = crmGetPreview(comms, 2);
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 1), "Emails",
				assertionErrorMsg(getLineNumber()));
		// due to bug ticket BBCRM-10948, the From is not populated
		softAssertion.assertTrue(StringUtils.isBlank(crmGetPreviewDataByRowFluidNum(preview, 2)),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 3), getProp("test_dummy_email_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetPreviewDataByRowFluidNum(preview, 4),
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"),
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"));
		crmSwitchToWindow(2);
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"), " Emails",
				"BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		softAssertion.assertEquals(emailName,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move In sent to Maroun, Rashideh");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move In sent to Maroun, Rashideh", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		emailName = normalizeSpaces(getEmailName());
		String assignedTo = normalizeSpaces(getEmailDataPerCell(0, 1));
		String dateSent = normalizeSpaces(getEmailDataPerCell(0, 3));
		String team = normalizeSpaces(getEmailDataPerCell(1, 1));
		String relatedTo = normalizeSpaces(getEmailDataPerCell(1, 3));
		String from = normalizeSpaces(getEmailDataPerCell(2, 1));
		to = normalizeSpaces(getEmailDataPerCell(3, 1));
		String cc = normalizeSpaces(getEmailDataPerCell(4, 1));
		String bcc = normalizeSpaces(getEmailDataPerCell(5, 1));
		String subject = normalizeSpaces(getEmailDataPerCell(6, 1));
		softAssertion.assertEquals(emailName, "Online Request Email for Move In sent to Maroun, Rashideh",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, "energy.intel@bluebilling.com.au", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, concatStrings("Iain Stuart <", getProp("test_dummy_email_lower_case"), ">"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(cc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(bcc, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request Email for Move In sent to Maroun, Rashideh",
				assertionErrorMsg(getLineNumber()));
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		softAssertion.assertEquals(p1, "Online Request Email for Move In", assertionErrorMsg(getLineNumber()));

		List<String> p2ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Type: Move In", "Request Account Type: Residential",
						"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
						"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
						"Request Main Contact First Name: Rashideh", "Request Main Contact Last Name: Maroun",
						"Request Postal Address: Villa 5 5 7 Albion RD", "Box Hill, VIC 3128"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		softAssertion.assertEquals(p2ActualValues, p2ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Villa 5 5 7 Albion RD", "Request Tenancy Suburb: Box Hill",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3128", "Request Electricity Life Support: ",
				"Request Water Life Support: In Use"));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		softAssertion.assertEquals(p3ActualValues, p3ExpectedValues, assertionErrorMsg(getLineNumber()));

		List<String> p4ExpectedValues = new ArrayList<>(
				Arrays.asList("Request Direct Debit: Credit Card", "Request Holiday Rental: Yes",
						concatStrings("Request Move In Date: ", getProp("ResiNewContact02_ownerMoveInDateCRM")),
						concatStrings("Request Settlement Date: ", getProp("ResiNewContact02_ownerSettlementDateCRM")),
						"Request Move Out Date: ", "Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		softAssertion.assertEquals(p4ActualValues, p4ExpectedValues, assertionErrorMsg(getLineNumber()));

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
			String todaySubmitted = getProp("ResiNewContact02_dateSubmittedDash");
			dateSubmitUpd = todaySubmitted + " 0" + recordViewTime;
		} else {
			dateSubmitUpd = dateSubmitUpdRaw3;
		}
		String reqSubmitAssertion = concatStrings("Request Submitted Date and Time: ", dateSubmitUpd);
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		softAssertion.assertEquals(p5ActualValues, p5ExpectedValues, assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p6), "Services Action Taken:", assertionErrorMsg(getLineNumber()));

		softAssertion.assertEquals(normalizeSpaces(p7), "Services All Fields:", assertionErrorMsg(getLineNumber()));

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = getEmailAttachmentNames(9);
		softAssertion.assertTrue(attachmentNames.toString().contains("ONLINE REQUEST PDF FOR MOVE IN.pdf"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(attachmentNames.toString().contains("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(attachmentNames.size(), 2, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE IN.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE IN.pdf", 1, 8000, true, "Online Request PDF for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Rashideh", "Request Main Contact Last Name: Maroun",
				"Request Postal Address: Villa 5 5 7 Albion RD", "Box Hill, VIC 3128", "Request Complex Name: ",
				"Request Tenancy Address: Villa 5 5 7 Albion RD", "Request Tenancy Suburb: Box Hill",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3128", "Request Electricity Life Support: ",
				"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
				"Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiNewContact02_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiNewContact02_ownerSettlementDateCRM")),
				"Request Settlement Date: ", "Request Move Out Date: ", "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
				"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF");
		verifyPdfContent("ACCOUNT_NEWACCOUNTWIZARD_STORYBOARD-1.PDF", 1, 8000, true,
				"Create New Account Wizard - Services & Charging", "Business ABN/ACN", "Bill Delivery Method",
				"Direct Debit via Bank Account", "BBCRM-36");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move In");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move In", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		to = getDisplayedText(smsrecordview.recipients, true);
		subject = getDisplayedText(smsrecordview.subject, true);
		softAssertion.assertEquals(to, "Iain Stuart", assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject, "Online Request SMS for Move In", assertionErrorMsg(getLineNumber()));
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move In",
				"Request Type: Move In", "Request Account Type: Residential",
				"Request Account Category: Holiday Rental or Letting", "Request Account VIP Status:",
				"Request Company Name:", "Request Company Trading Name:", "Request Main Contact Salutation:",
				"Request Main Contact First Name: Rashideh", "Request Main Contact Last Name: Maroun",
				"Request Postal Address: Villa 5 5 7 Albion RD", "Box Hill, VIC 3128", "Request Complex Name:",
				"Request Tenancy Address: Villa 5 5 7 Albion RD", "Request Tenancy Suburb: Box Hill",
				"Request Tenancy State: VIC", "Request Tenancy Postcode: 3128", "Request Electricity Life Support:",
				"Request Water Life Support: In Use", "Request Direct Debit: Credit Card",
				"Request Holiday Rental: Yes",
				concatStrings("Request Move In Date: ", getProp("ResiNewContact02_ownerMoveInDateCRM")),
				concatStrings("Request Settlement Date: ", getProp("ResiNewContact02_ownerSettlementDateCRM")),
				"Request Move Out Date:", "Request Current Stage: NEW", "Request Current State: REACHED",
				reqSubmitAssertion, "Request Completed Date and Time:", "Services Action Taken:",
				"Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		softAssertion.assertEquals(actualValues, expectedValues, assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the WEB_FORM
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 2, 1,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"));
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"), " Emails",
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
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(assignedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSent.startsWith(getProp("ResiNewContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(team, "Global", assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(relatedTo), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(from, getProp("default_email_from_address"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(to, getProp("test_dummy_email_lower_case"), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(cc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(bcc), assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(subject,
				concatStrings("BlueBilling Move In ", getInstanceIdMoveIn(), " WEB_FORM Rashideh Maroun"),
				assertionErrorMsg(getLineNumber()));
		String emailBody = getWholeEmailBody(getEmailTable(), true);
		logDebugMessage(concatStrings("The value of emailBody is:\n", emailBody));
		softAssertion.assertFalse(StringUtils.isBlank(emailBody), assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains(
						"{\"service_type\":\"ELECTRICITY\",\"required\":false,\"equipment\":[],\"attachments\":[]},"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"{\"service_type\":\"WATER\",\"required\":true,\"equipment\":[\"Haemodialysis Machine\"],\"attachments\":[{\"id\":"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(
				emailBody.contains("\"medical_cooling\":false,\"trade_waste\":null,\"business_identity\":null,"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(emailBody.contains(
				"\"concession_applicable\":true,\"concession_card\":{\"card_type\":\"vic_health_card\",\"card_holder\":\"Steven Roger's Jr.\",\"card_number\":\"01321687450\",\"authorisation\":{\"method\":\"UPLOAD\",\"accepted\":true,\"text\":\"'Company'"),
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
		softAssertion.assertEquals(dbSourceId, getProp("ResiNewContact02_sourceID"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dbInstanceId, getInstanceIdMoveIn(), assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		driver.close();
		crmSwitchCurrentWindow(mainWindow);
	}

}