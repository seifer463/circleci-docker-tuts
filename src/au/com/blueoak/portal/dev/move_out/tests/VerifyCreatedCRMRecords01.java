package au.com.blueoak.portal.dev.move_out.tests;

import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.dev.move_out.MoveOutDevBase;
import au.com.blueoak.portal.pageObjects.crm.OnlineRequestRecordView;
import au.com.blueoak.portal.pageObjects.crm.SMSRecordView;

public class VerifyCreatedCRMRecords01 extends MoveOutDevBase {

	/**
	 * Initialize the page objects factory
	 */
	OnlineRequestRecordView onlinerequestrecordview;
	SMSRecordView smsrecordview;

	/**
	 * Store the name of the class for logging
	 */
	private String className;

	/**
	 * This will check if we already logged into the CRM
	 */
	private boolean isCrmLoggedIn = false;

	/**
	 * The ID of the online request created
	 */
	private String onlineReqId;

	/** 
	 * 
	 * */
	private String onlineReqId1;

	/** 
	 * 
	 * */
	private String onlineReqId2;

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
	private String getCommsIDToUpdateFromSMS(String parentID) throws SQLException {

		String query = new StringBuilder(
				"SELECT sms_message_id FROM `bbcrm_smsrecipients` WHERE parent_type = 'bbcrm_OnlineRequestContacts' AND parent_id = '")
				.append(parentID).append("';").toString();

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
	 * - verify the contact online request would be created with no existing contact
	 * - verify that the record was created in the CRM - verify the notes created -
	 * verify the email and sms that was sent and is correctly parsed - verify the
	 * pdf and is correctly parsed - verify the attachment sent - verify the
	 * instance id saved in the DB - verify the source id from session matches the
	 * one saved in the DB
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Natsu", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);

		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		String commsId = getCommsIDToUpdateFromSMS(olId);
		updateCommunicationStatus("sent", commsId);
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveOutContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveOutContactPersonalIdValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		// engine removed the leading zero
		softAssertion.assertEquals(acctName, "12345678912",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiNewContact01_moveOutDate1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "'x44 Complex's Unit 16 6 Mari ST Alexandra Headland, Queensland, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Unit 16 6 Mari ST Alexandra Headland, QLD, 4572",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Natsu Dragneel's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAhoursPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "0411234567",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "'007 tralala's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId,
				concatStrings("Medicare Card (24287781321, ", getProp("ResiNewContact01_medCareExpiry"), ")"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("ResiNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(accountTooltip, "Account not found in the system",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactNameTooltip, "Specified contact is not associated with the specified account",
				assertionErrorMsg(getLineNumber()));
		// verify no image is displayed
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueAccountType),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactSecretCode),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactBirthDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactPersonalId),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// verify the email sent
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				assertionErrorMsg(getLineNumber()));
		// verify the first record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "SMS");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request SMS for Move Out");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify the second record
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 1, "Online Request Email for Move Out sent to Dragneel's, Natsu");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 1, 3, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 1, 4);
		// verify all assertions
		softAssertion.assertAll();

		String mainWindow = crmGetWindowHandle();

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move Out sent to Dragneel's, Natsu");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move Out sent to Dragneel's, Natsu", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		verifyTwoStringsAreEqual(getEmailName(), "Online Request Email for Move Out sent to Dragneel's, Natsu", true);
		verifyStringIsBlank(getEmailDataPerCell(0, 1));
		verifyStringStartsWith(getEmailDataPerCell(0, 3), getProp("ResiNewContact01_dateSubmittedSlash"));
		verifyTwoStringsAreEqual(getEmailDataPerCell(1, 1), "Global", true);
		verifyStringIsBlank(getEmailDataPerCell(1, 3));
		verifyTwoStringsAreEqual(getEmailDataPerCell(2, 1), "energy.intel@bluebilling.com.au", true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(3, 1),
				concatStrings("Natsu Dragneels <", getProp("test_email_dummy_lower_case"), ">"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(4, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(5, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(6, 1),
				"Online Request Email for Move Out sent to Dragneel's, Natsu", true);
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		verifyTwoStringsAreEqual(p1, "Online Request Email for Move Out", true);

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move Out",
				"Request Account Type: Residential", "Request Account Category: ", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Natsu", "Request Main Contact Last Name: Dragneel's",
				"Request Postal Address: Unit 16 6 Mari ST", "Alexandra Headland, QLD 4572"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		verifyTwoListsAreEqual(p2ActualValues, p2ExpectedValues);

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: 'x44 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4572", "Request Electricity Life Support: ",
				"Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		verifyTwoListsAreEqual(p3ActualValues, p3ExpectedValues);

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: ",
				"Request Holiday Rental: ", "Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("ResiNewContact01_moveOutDate3"), "Request Current Stage: NEW",
				"Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		verifyTwoListsAreEqual(p4ActualValues, p4ExpectedValues);

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
		String reqSubmitAssertion = "Request Submitted Date and Time: " + dateSubmitUpd;
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		verifyTwoListsAreEqual(p5ActualValues, p5ExpectedValues);

		verifyTwoStringsAreEqual(p6, "Services Action Taken:", true);

		verifyTwoStringsAreEqual(p7, "Services All Fields:", true);

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = super.getEmailAttachmentNames(9);
		verifyStringContainsInList(attachmentNames, true, "ONLINE REQUEST PDF FOR MOVE OUT.pdf", true);
		verifyStringContainsInList(attachmentNames, true, "SPRINT 02 STORY BOARD.PDF", true);
		// verify we have the correct number of attachments
		verifyNumOfEmailAttachments(9, 2);
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE OUT.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE OUT.pdf", 1, 8000, true, "Online Request PDF for Move Out",
				"Request Type: Move Out", "Request Account Type: Residential", "Request Account Category: ",
				"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
				"Request Main Contact Salutation: ", "Request Main Contact First Name: Natsu",
				"Request Main Contact Last Name: Dragneel's", "Request Postal Address: Unit 16 6 Mari ST",
				"Alexandra Headland, QLD 4572", "Request Complex Name: 'x44 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4572", "Request Electricity Life Support: ",
				"Request Water Life Support: ", "Request Direct Debit: ", "Request Holiday Rental: ",
				"Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("ResiNewContact01_moveOutDate3"), "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
				"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("SPRINT 02 STORY BOARD.PDF");
		verifyPdfContent("SPRINT 02 STORY BOARD.PDF", 1, 8000, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move Out");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move Out", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		String to = getDisplayedText(smsrecordview.recipients, true);
		verifyTwoStringsAreEqual(to, "Natsu Dragneel's", true);
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move Out",
				"Request Type: Move Out", "Request Account Type: Residential", "Request Account Category:",
				"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
				"Request Main Contact Salutation:", "Request Main Contact First Name: Natsu",
				"Request Main Contact Last Name: Dragneel's", "Request Postal Address: Unit 16 6 Mari ST",
				"Alexandra Headland, QLD 4572", "Request Complex Name: 'x44 Complex's",
				"Request Tenancy Address: Unit 16 6 Mari ST", "Request Tenancy Suburb: Alexandra Headland",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4572", "Request Electricity Life Support:",
				"Request Water Life Support:", "Request Direct Debit:", "Request Holiday Rental:",
				"Request Move In Date:", "Request Settlement Date:",
				"Request Move Out Date: " + getProp("ResiNewContact01_moveOutDate3"), "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
				"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		verifyTwoListsAreEqual(actualValues, expectedValues);

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the attached notes
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 2);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify the 2nd row
		crmVerifyListOrSubpanelEqualsValue(notes, 1, 1, "Additional Note from Customer");
		crmVerifyListOrSubpanelStartsWith(notes, 1, 2, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 1, 3, getProp("ResiNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 1, 4);
		// verify preview for 1st record
		WebElement preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Request Acceptance Details");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5,
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a>. (ACCEPTED) I/We have added enquiries@blueoak.com.au to my email contacts or white list where required less");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);
		// verify the preview for the 2nd row
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Additional Note from Customer");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5, "~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("ResiNewContact01_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * 
	 * - verify the online request would be created and contact should be linked to
	 * an existing one since Name and Email Address is the same - verify that the
	 * record was created in the CRM - verify the notes created - verify the email
	 * and sms that was sent and is correctly parsed - verify the pdf and is
	 * correctly parsed - verify the attachment sent - verify the instance id saved
	 * in the DB - verify the source id from session matches the one saved in the DB
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Michael", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		String commsId = getCommsIDToUpdateFromSMS(olId);
		updateCommunicationStatus("sent", commsId);
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveOutContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveOutContactPersonalIdValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String contactSecretCodeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("data-title"));
		String contactBirthdateTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("data-title"));
		String contactPersonalIdTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		String contactSecretCodeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("src"));
		String contactBirthdateImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("src"));
		String contactPersonalIdImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("src"));
		softAssertion.assertEquals(acctName, "100040009993 (Human Shoes)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact01_moveOutDate1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Unit 24 287 Gympie TCE Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Bldg 7, 1000 Ann Street Add-03 updated Fortitude Valley, QLD, 4006 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName,
				"Michael O'Connell (Will be merged with additional contact Mr. Michael O'Connell)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "0363021485",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAhoursPhone, "0411234567",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "0238921111",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "'x44 tralala's",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiExistingContact01_dateOfBirth1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Driver's License (01235987510, Australian Capital Territory)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (Postal, Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("ResiExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(accountTypeTooltip,
				"Account type supplied does not match existing account’s which is set to Small Business",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactNameTooltip, "Specified contact is not associated with the specified account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCodeTooltip,
				"Secret code supplied but one is NOT set for the existing contact",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdateTooltip,
				"Birthdate supplied but one is NOT set for the existing contact",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalIdTooltip,
				"Personal identification supplied but one is NOT set for the existing contact.",
				assertionErrorMsg(getLineNumber()));
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(accountTypeImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactSecretCodeImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactBirthdateImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactPersonalIdImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();
		
		crmScrollPage(10, Keys.DOWN);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the email sent
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				assertionErrorMsg(getLineNumber()));
		// verify the first record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "SMS");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request SMS for Move Out");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify the second record
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 1, "Online Request Email for Move Out sent to O'Connell, Michael");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 1, 3, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 1, 4);
		// verify all assertions
		softAssertion.assertAll();

		String mainWindow = crmGetWindowHandle();

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move Out sent to O'Connell, Michael");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move Out sent to O'Connell, Michael", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		verifyTwoStringsAreEqual(getEmailName(), "Online Request Email for Move Out sent to O'Connell, Michael", true);
		verifyStringIsBlank(getEmailDataPerCell(0, 1));
		verifyStringStartsWith(getEmailDataPerCell(0, 3), getProp("ResiExistingContact01_dateSubmittedSlash"));
		verifyTwoStringsAreEqual(getEmailDataPerCell(1, 1), "Global", true);
		verifyStringIsBlank(getEmailDataPerCell(1, 3));
		verifyTwoStringsAreEqual(getEmailDataPerCell(2, 1), "energy.intel@bluebilling.com.au", true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(3, 1),
				concatStrings("Michael OConnell <", getProp("test_email_dummy_lower_case"), ">"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(4, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(5, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(6, 1),
				"Online Request Email for Move Out sent to O'Connell, Michael", true);
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		verifyTwoStringsAreEqual(p1, "Online Request Email for Move Out", true);

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move Out",
				"Request Account Type: Residential", "Request Account Category: ", "Request Account VIP Status: ",
				"Request Company Name: ", "Request Company Trading Name: ", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: Michael", "Request Main Contact Last Name: O'Connell",
				"Request Postal Address: Bldg 7,", "1000 Ann Street", "Add-03 updated", "Fortitude Valley, QLD 4006",
				"AU"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		verifyTwoListsAreEqual(p2ActualValues, p2ExpectedValues);

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Unit 24 287 Gympie TCE", "Request Tenancy Suburb: Noosaville",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4566", "Request Electricity Life Support: ",
				"Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		verifyTwoListsAreEqual(p3ActualValues, p3ExpectedValues);

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: ",
				"Request Holiday Rental: ", "Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("ResiExistingContact01_moveOutDate2"), "Request Current Stage: NEW",
				"Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		verifyTwoListsAreEqual(p4ActualValues, p4ExpectedValues);

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
		String reqSubmitAssertion = "Request Submitted Date and Time: " + dateSubmitUpd;
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		verifyTwoListsAreEqual(p5ActualValues, p5ExpectedValues);

		verifyTwoStringsAreEqual(p6, "Services Action Taken:", true);

		verifyTwoStringsAreEqual(p7, "Services All Fields:", true);

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = super.getEmailAttachmentNames(9);
		verifyStringContainsInList(attachmentNames, true, "ONLINE REQUEST PDF FOR MOVE OUT.pdf", true);
		verifyStringContainsInList(attachmentNames, true, "SPRINT 02 STORY BOARD.PDF", true);
		// verify we have the correct number of attachments
		verifyNumOfEmailAttachments(9, 2);
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE OUT.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE OUT.pdf", 1, 8000, true, "Online Request PDF for Move Out",
				"Request Type: Move Out", "Request Account Type: Residential", "Request Account Category: ",
				"Request Account VIP Status: ", "Request Company Name: ", "Request Company Trading Name: ",
				"Request Main Contact Salutation: ", "Request Main Contact First Name: Michael",
				"Request Main Contact Last Name: O'Connell", "Request Postal Address: Bldg 7,", "1000 Ann Street",
				"Add-03 updated", "Fortitude Valley, QLD 4006", "AU", "Request Complex Name: ",
				"Request Tenancy Address: Unit 24 287 Gympie TCE", "Request Tenancy Suburb: Noosaville",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4566", "Request Electricity Life Support: ",
				"Request Water Life Support: ", "Request Direct Debit: ", "Request Holiday Rental: ",
				"Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("ResiExistingContact01_moveOutDate2"), "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time: ",
				"Services Action Taken: ", "Services All Fields:", "Regards,", "--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("SPRINT 02 STORY BOARD.PDF");
		verifyPdfContent("SPRINT 02 STORY BOARD.PDF", 1, 8000, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move Out");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move Out", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		String to = getDisplayedText(smsrecordview.recipients, true);
		verifyTwoStringsAreEqual(to, "Michael O'Connell", true);
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move Out",
				"Request Type: Move Out", "Request Account Type: Residential", "Request Account Category:",
				"Request Account VIP Status:", "Request Company Name:", "Request Company Trading Name:",
				"Request Main Contact Salutation:", "Request Main Contact First Name: Michael",
				"Request Main Contact Last Name: O'Connell", "Request Postal Address: Bldg 7,", "1000 Ann Street",
				"Add-03 updated", "Fortitude Valley, QLD 4006", "AU", "Request Complex Name:",
				"Request Tenancy Address: Unit 24 287 Gympie TCE", "Request Tenancy Suburb: Noosaville",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4566", "Request Electricity Life Support:",
				"Request Water Life Support:", "Request Direct Debit:", "Request Holiday Rental:",
				"Request Move In Date:", "Request Settlement Date:",
				"Request Move Out Date: " + getProp("ResiExistingContact01_moveOutDate2"), "Request Current Stage: NEW",
				"Request Current State: REACHED", reqSubmitAssertion, "Request Completed Date and Time:",
				"Services Action Taken:", "Services All Fields:", "Regards,", "--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		verifyTwoListsAreEqual(actualValues, expectedValues);

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the attached notes
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 2);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify the 2nd row
		crmVerifyListOrSubpanelEqualsValue(notes, 1, 1, "Additional Note from Customer");
		crmVerifyListOrSubpanelStartsWith(notes, 1, 2, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 1, 3, getProp("ResiExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 1, 4);
		// verify preview for 1st record
		WebElement preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Request Acceptance Details");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5,
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a>. (ACCEPTED) I/We have added enquiries@blueoak.com.au to my email contacts or white list where required less");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);
		// verify the preview for the 2nd row
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		preview = crmGetPreview(notes, 1);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Additional Note from Customer");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5, "Added an additional note from update link");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("ResiExistingContact01_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * 
	 * 
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 * 
	 */
	@Test(priority = 3)
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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("tom", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		String commsId = getCommsIDToUpdateFromSMS(olId);
		updateCommunicationStatus("sent", commsId);
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveOutCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String contactSecretCodeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("data-title"));
		String contactCompanyTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		String contactSecretCodeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("src"));
		String contactCompanyImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("src"));
		softAssertion.assertEquals(acctName, "100040008797 (Butchers Club)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact01_moveOutDate1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Unit 20 239-245 Gympie TCE Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Unit 75 903 David Low Way Marcoola, QLD, 4564 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "tom tri ly (Will be merged with additional contact (Tom) Tri Ly)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAhoursPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "+61469941139",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Contact Secret Code",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company, concatStrings(getProp("test_data_valid_company_name_acn1_acn2"),
				" T/a Trading's LLC' ", getProp("test_data_valid_acn1")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (Postal, Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("SmallBusExistingContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTypeTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactNameTooltip,
				"Specified contact is associated with the account but it’s not the primary contact",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCodeTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactCompanyTooltip),
				assertionErrorMsg(getLineNumber()));
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(accountTypeImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_warning.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactSecretCodeImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactCompanyImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the email sent
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				assertionErrorMsg(getLineNumber()));
		// verify the first record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "SMS");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request SMS for Move Out");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("SmallBusExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify the second record
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 1, "Online Request Email for Move Out sent to ly, tom tri");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 1, 3, getProp("SmallBusExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 1, 4);
		// verify all assertions
		softAssertion.assertAll();

		String mainWindow = crmGetWindowHandle();

		// lets verify the content of the email
		crmClickRecord(comms, 1, 1, "Online Request Email for Move Out sent to ly, tom tri");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT,
				"Online Request Email for Move Out sent to ly, tom tri", " Emails", "BlueBilling");
		crmSwitchToBwcIframe();
		verifyTwoStringsAreEqual(getEmailName(), "Online Request Email for Move Out sent to ly, tom tri", true);
		verifyStringIsBlank(getEmailDataPerCell(0, 1));
		verifyStringStartsWith(getEmailDataPerCell(0, 3), getProp("SmallBusExistingContact01_dateSubmittedSlash"));
		verifyTwoStringsAreEqual(getEmailDataPerCell(1, 1), "Global", true);
		verifyStringIsBlank(getEmailDataPerCell(1, 3));
		verifyTwoStringsAreEqual(getEmailDataPerCell(2, 1), "energy.intel@bluebilling.com.au", true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(3, 1),
				concatStrings("tom tri ly <", getProp("test_email_dummy_lower_case"), ">"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(4, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(5, 1), getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(getEmailDataPerCell(6, 1), "Online Request Email for Move Out sent to ly, tom tri",
				true);
		// let's get the content of the email body
		// per <p> tag
		String p1 = getValueOfPtagInEmailBody(getEmailTable(), 1);
		String p2 = getValueOfPtagInEmailBody(getEmailTable(), 2);
		String p3 = getValueOfPtagInEmailBody(getEmailTable(), 3);
		String p4 = getValueOfPtagInEmailBody(getEmailTable(), 4);
		String p5 = getValueOfPtagInEmailBody(getEmailTable(), 5);
		String p6 = getValueOfPtagInEmailBody(getEmailTable(), 6);
		String p7 = getValueOfPtagInEmailBody(getEmailTable(), 7);

		verifyTwoStringsAreEqual(p1, "Online Request Email for Move Out", true);

		List<String> p2ExpectedValues = new ArrayList<>(Arrays.asList("Request Type: Move Out",
				"Request Account Type: Small Business", "Request Account Category: ", "Request Account VIP Status: ",
				"Request Company Name: THE HAIRHOUSE WAREHOUSE PTY. LTD.",
				"Request Company Trading Name: Trading's LLC'", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
				"Request Postal Address: Unit 75", "903 David Low Way", "Marcoola, QLD 4564", "AU"));
		List<String> p2ActualValues = getEachTextInBreakTags(p2, false);
		verifyTwoListsAreEqual(p2ActualValues, p2ExpectedValues);

		List<String> p3ExpectedValues = new ArrayList<>(Arrays.asList("Request Complex Name: ",
				"Request Tenancy Address: Unit 20 239-245 Gympie TCE", "Request Tenancy Suburb: Noosaville",
				"Request Tenancy State: QLD", "Request Tenancy Postcode: 4566", "Request Electricity Life Support: ",
				"Request Water Life Support: "));
		List<String> p3ActualValues = getEachTextInBreakTags(p3, false);
		verifyTwoListsAreEqual(p3ActualValues, p3ExpectedValues);

		List<String> p4ExpectedValues = new ArrayList<>(Arrays.asList("Request Direct Debit: ",
				"Request Holiday Rental: ", "Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("SmallBusExistingContact01_moveOutDate2"),
				"Request Current Stage: NEW", "Request Current State: REACHED"));
		List<String> p4ActualValues = getEachTextInBreakTags(p4, false);
		verifyTwoListsAreEqual(p4ActualValues, p4ExpectedValues);

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
		String reqSubmitAssertion = "Request Submitted Date and Time: " + dateSubmitUpd;
		List<String> p5ExpectedValues = new ArrayList<>(
				Arrays.asList(reqSubmitAssertion, "Request Completed Date and Time: "));
		List<String> p5ActualValues = getEachTextInBreakTags(p5, false);
		verifyTwoListsAreEqual(p5ActualValues, p5ExpectedValues);

		verifyTwoStringsAreEqual(p6, "Services Action Taken:", true);

		verifyTwoStringsAreEqual(p7, "Services All Fields:", true);

		// clear the content of the download directory
		cleanDownloadDir();
		crmScrollPageBwcModules(20, Keys.DOWN);
		// verify the expected attachments names
		List<String> attachmentNames = super.getEmailAttachmentNames(9);
		verifyStringContainsInList(attachmentNames, true, "ONLINE REQUEST PDF FOR MOVE OUT.pdf", true);
		verifyStringContainsInList(attachmentNames, true, "SPRINT 02 STORY BOARD.PDF", true);
		// verify we have the correct number of attachments
		verifyNumOfEmailAttachments(9, 2);
		// download the attachment
		clickAttachmentFromEmail("ONLINE REQUEST PDF FOR MOVE OUT.pdf");
		// verify the sent pdf
		verifyPdfContent("ONLINE REQUEST PDF FOR MOVE OUT.pdf", 1, 8000, true, "Online Request PDF for Move Out",
				"Request Type: Move Out", "Request Account Type: Small Business", "Request Account Category: ",
				"Request Account VIP Status: ", "Request Company Name: THE HAIRHOUSE WAREHOUSE PTY. LTD.",
				"Request Company Trading Name: Trading's LLC'", "Request Main Contact Salutation: ",
				"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
				"Request Postal Address: Unit 75", "903 David Low Way", "Marcoola, QLD 4564", "AU",
				"Request Complex Name: ", "Request Tenancy Address: Unit 20 239-245 Gympie TCE",
				"Request Tenancy Suburb: Noosaville", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4566",
				"Request Electricity Life Support: ", "Request Water Life Support: ", "Request Direct Debit: ",
				"Request Holiday Rental: ", "Request Move In Date: ", "Request Settlement Date: ",
				"Request Move Out Date: " + getProp("SmallBusExistingContact01_moveOutDate2"),
				"Request Current Stage: NEW", "Request Current State: REACHED", reqSubmitAssertion,
				"Request Completed Date and Time: ", "Services Action Taken: ", "Services All Fields:", "Regards,",
				"--BlueOak Support");

		// clear the content of the download directory
		cleanDownloadDir();
		// verify the sent attachment
		// download the attachment
		clickAttachmentFromEmail("SPRINT 02 STORY BOARD.PDF");
		verifyPdfContent("SPRINT 02 STORY BOARD.PDF", 1, 8000, true, "Accounts",
				"Create New Account Wizard - Direct Debit", "Link Contact - Account Contact",
				"New Contact - Account Contact", "New Communication - SMS");

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the sms sent
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// click the record
		crmClickRecord(comms, 0, 1, "Online Request SMS for Move Out");
		crmSwitchToWindow(2);
		// wait for the page title
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Online Request SMS for Move Out", " SMS",
				"BlueBilling");
		// click more link
		clickElementAction(smsrecordview.moreLessLink);
		String to = getDisplayedText(smsrecordview.recipients, true);
		verifyTwoStringsAreEqual(to, "tom tri ly", true);
		String msgBody = getSmsMessageBody(false);
		List<String> expectedValues = new ArrayList<>(Arrays.asList("Online Request SMS for Move Out",
				"Request Type: Move Out", "Request Account Type: Small Business", "Request Account Category:",
				"Request Account VIP Status:", "Request Company Name: THE HAIRHOUSE WAREHOUSE PTY. LTD.",
				"Request Company Trading Name: Trading's LLC'", "Request Main Contact Salutation:",
				"Request Main Contact First Name: tom tri", "Request Main Contact Last Name: ly",
				"Request Postal Address: Unit 75", "903 David Low Way", "Marcoola, QLD 4564", "AU",
				"Request Complex Name:", "Request Tenancy Address: Unit 20 239-245 Gympie TCE",
				"Request Tenancy Suburb: Noosaville", "Request Tenancy State: QLD", "Request Tenancy Postcode: 4566",
				"Request Electricity Life Support:", "Request Water Life Support:", "Request Direct Debit:",
				"Request Holiday Rental:", "Request Move In Date:", "Request Settlement Date:",
				"Request Move Out Date: " + getProp("SmallBusExistingContact01_moveOutDate2"),
				"Request Current Stage: NEW", "Request Current State: REACHED", reqSubmitAssertion,
				"Request Completed Date and Time:", "Services Action Taken:", "Services All Fields:", "Regards,",
				"--BlueOak Support less"));
		List<String> actualValues = getEachTextInBreakTags(msgBody, false);
		verifyTwoListsAreEqual(actualValues, expectedValues);

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		// verify the attached notes
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 1);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("SmallBusExistingContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("SmallBusExistingContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify preview for 1st record
		WebElement preview = crmGetPreview(notes, 0);
		clickElementAction(onlinerequestrecordview.notesDescriptionMoreLessLink);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Request Acceptance Details");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5,
				"(ACCEPTED) I/We confirm the above details are accurate and by clicking submit I understand an account will be setup with these details (ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a>. (DECLINED) I/We have added enquiries@blueoak.com.au to my email contacts or white list where required less");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("SmallBusExistingContact01_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * - verify the crm records created in createOnlineReqRec01 and
	 * createOnlineReqRec02
	 * 
	 * @throws SQLException
	 * 
	 */
	@Test(priority = 4)
	public void verifyCrmResiExistingContact02_createOnlineReqRec01_02() throws SQLException {

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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		crmOpenListView("bbcrm_OnlineRequests", null, false);
		String mainWindow = crmGetWindowHandle();
		WebElement onlineReq = crmGetListViewTableWithSearch("Brad", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		String olId = getIdOfOnlineRequest();
		this.onlineReqId1 = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveOutContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveOutContactPersonalIdValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String contactSecretCodeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("data-title"));
		String contactBirthdateTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("data-title"));
		String contactPersonalIdTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		String contactSecretCodeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("src"));
		String contactBirthdateImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("src"));
		String contactPersonalIdImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("src"));
		softAssertion.assertEquals(acctName, "100060006796 (MR BRAD HARRIS)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact02_firstCRM_moveOutDate1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Unit 40 272 Weyba RD Noosaville, Queensland, 4566",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Unit 7B 11 Innovation Parkway Address #04 Birtinya, QLD, 4575 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "brad Harrison (Will be merged with main contact Brad Harrison)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61469041930",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAhoursPhone, "0260042131",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "0238931121",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "SEKreT's 000",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, getProp("ResiExistingContact02_firstCRM_dateOfBirth1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Passport (Pass12340, Åland Islands)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (Postal, Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		verifyTwoStringsAreEqual(dateSubmittedUpd, dateCreated, true);
		softAssertion.assertTrue(StringUtils.isBlank(accountTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTypeTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactNameTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCodeTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBirthdateTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactPersonalIdTooltip),
				assertionErrorMsg(getLineNumber()));
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(accountTypeImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactSecretCodeImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactBirthdateImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactPersonalIdImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// verify the email sent and sms not displayed since failed
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// verify the number of records created
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				assertionErrorMsg(getLineNumber()));
		// verify the record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request Email for Move Out sent to Harrison, brad");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify all assertions
		softAssertion.assertAll();

		// verify the notes section
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 1);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("ResiExistingContact02_firstCRM_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId1);
		verifyTwoStringsAreEqual(dbSourceId, getProp("ResiExistingContact02_firstCRM_sourceID1"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId1);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);

		driver.close();
		crmSwitchCurrentWindow(mainWindow);

		crmOpenListView("bbcrm_OnlineRequests", null, false);
		onlineReq = crmGetListViewTableWithSearch("Smith", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		olId = getIdOfOnlineRequest();
		this.onlineReqId2 = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		contactBirthdate = getDisplayedText(onlinerequestrecordview.moveOutContactBirthdateValue, true);
		contactPersonalId = getDisplayedText(onlinerequestrecordview.moveOutContactPersonalIdValue, true);
		notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		contactSecretCodeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("data-title"));
		contactBirthdateTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("data-title"));
		contactPersonalIdTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("data-title"));
		accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		contactSecretCodeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("src"));
		contactBirthdateImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactBirthDate.get(0).getAttribute("src"));
		contactPersonalIdImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactPersonalId.get(0).getAttribute("src"));
		verifyTwoStringsAreEqual(acctName, "100060004395 (MANDALIA P/L)", true);
		verifyTwoStringsAreEqual(request, "Move Out", true);
		verifyTwoStringsAreEqual(progress, "New / Waiting Verification Normal / Normal", true);
		verifyTwoStringsAreEqual(dateReq, getProp("ResiExistingContact02_secondCRM_moveOutDate2"), true);
		verifyTwoStringsAreEqual(reqTenancy, "75 Davis ST Allenstown, Queensland, 4700", true);
		verifyTwoStringsAreEqual(acctType, "Residential", true);
		verifyTwoStringsAreEqual(forwAdd, "Unit 50 20 Baywater Drive Address #03 Address #04 Twin Waters, QLD, 4564 AU",
				true);
		verifyStringContains(true, dateSubmitted, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		verifyStringContains(true, dateCreated, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		verifyTwoStringsAreEqual(contactName,
				"Susan & Les Smith (Will be merged with additional contact Susan & Les Smith)", true);
		verifyStringIsBlank(contactBusPhone);
		verifyTwoStringsAreEqual(contactAhoursPhone, "+61823014785", true);
		verifyTwoStringsAreEqual(contactMobPhone, "1800986510", true);
		verifyTwoStringsAreEqual(contactEmailAdd, getProp("test_email_dummy_lower_case"), true);
		verifyTwoStringsAreEqual(contactSecretCode, "#000-Abcs", true);
		verifyTwoStringsAreEqual(contactBirthdate, getProp("ResiExistingContact02_secondCRM_dateOfBirth2"), true);
		String query = new StringBuilder(
				"SELECT medicare_expiration_year FROM `bbcrm_personalidentification` WHERE contact_id = '1ae3e80f-cc5e-ba40-711e-561cb3ae3f72';")
				.toString();
		String medExpYr = executeQuery(query);
		verifyTwoStringsAreEqual(contactPersonalId, "Medicare Card (2428778132, 02/" + medExpYr + ")", true);
		verifyTwoStringsAreEqual(notifications, "Bills (Postal)", true);
		verifyTwoStringsAreEqual(dateCompleted, "Pending", true);
		verifyStringContains(true, dateModified, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		dateSubmittedUpd = dateSubmitted + " by";
		verifyTwoStringsAreEqual(dateSubmittedUpd, dateCreated, true);
		verifyStringIsBlank(accountTooltip);
		verifyStringIsBlank(accountTypeTooltip);
		verifyTwoStringsAreEqual(contactNameTooltip, "Specified contact is not associated with the specified account",
				true);
		verifyTwoStringsAreEqual(contactSecretCodeTooltip,
				"Secret code supplied does not match the existing contact’s which is set to #000-Abc's", true);
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR) - 18;
		String birthYr = Integer.toString(birthYrRaw);
		String todayMinus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -1, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(todayMinus1, 0,
				getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash").length() - 4);
		validBirthDate = validBirthDate + birthYr;
		verifyTwoStringsAreEqual(contactBirthdateTooltip,
				"Birthdate supplied does not match the existing contact’s birthdate of " + validBirthDate, true);
		verifyTwoStringsAreEqual(contactPersonalIdTooltip,
				"Personal identification supplied does not match the existing contact’s which is Medicare Card (2428778132/1, 02/"
						+ medExpYr + ")",
				true);
		// verify the correct image displayed
		verifyStringContains(true, accountImgSrcTooltip, "status_correct.svg");
		verifyStringContains(true, accountTypeImgSrcTooltip, "status_correct.svg");
		verifyStringContains(true, contactNameImgSrcTooltip, "status_error.svg");
		verifyStringContains(true, contactSecretCodeImgSrcTooltip, "status_error.svg");
		verifyStringContains(true, contactBirthdateImgSrcTooltip, "status_error.svg");
		verifyStringContains(true, contactPersonalIdImgSrcTooltip, "status_error.svg");

		crmScrollPage(10, Keys.DOWN);
		// verify the email sent and sms not displayed since failed
		comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// verify the number of records created
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				assertionErrorMsg(getLineNumber()));
		// verify the record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request Email for Move Out sent to Smith, Susan & Les");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify all assertions
		softAssertion.assertAll();

		// verify the notes section
		crmScrollPage(14, Keys.DOWN);
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 2);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify the 2nd row
		crmVerifyListOrSubpanelEqualsValue(notes, 1, 1, "Additional Note from Customer");
		crmVerifyListOrSubpanelStartsWith(notes, 1, 2, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 1, 3, getProp("ResiExistingContact02_secondCRM_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 1, 4);
		// preview the record
		notes = crmSortTableDescWithinPanel("Notes", 1, false);
		WebElement preview = crmGetPreview(notes, 1);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Additional Note from Customer");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5, "Many White spaces entered from the portal");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId2);
		verifyTwoStringsAreEqual(dbSourceId, getProp("ResiExistingContact02_secondCRM_sourceID2"), true);
		// also confirm the instance ID saved in the DB
		dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId2);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * - verify the crm records created in createOnlineReqRecord01
	 * 
	 * @throws SQLException
	 * 
	 */
	@Test(priority = 5)
	public void verifyCrmSmallBusNewContact01() throws SQLException {

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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("sanger", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveOutCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String contactCompanyTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		String contactCompanyImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("src"));
		softAssertion.assertEquals(acctName, "100060002498 (MR D FEENEY & MS L SANGER)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusNewContact01_moveOutDate1"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "132 Mitcham RD Donvale, Victoria, 3111",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Unit 301 192 Marine Parade Coolangatta, QLD, 4225 AU",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "L Sanger",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactBusPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactAhoursPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "1300569089",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_lower_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "Boy George 101",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_valid_company_name_abn1_abn2"), " ", getProp("test_data_valid_abn1")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (Postal)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("SmallBusNewContact01_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactNameTooltip, "Specified contact is not associated with the specified account",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactCompanyTooltip,
				"The existing account is not a commercial account and cannot compare details",
				assertionErrorMsg(getLineNumber()));
		// verify no image is displayed
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueAccountType),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactSecretCode),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactCompanyImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// verify the email sent and sms not displayed since failed
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 2,
				assertionErrorMsg(getLineNumber()));
		// verify the record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request Email for Move Out sent to Sanger, L");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("SmallBusNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify all assertions
		softAssertion.assertAll();

		// verify the notes section
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 2);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("SmallBusNewContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("SmallBusNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify the 2nd row
		crmVerifyListOrSubpanelEqualsValue(notes, 1, 1, "Additional Note from Customer");
		crmVerifyListOrSubpanelStartsWith(notes, 1, 2, getProp("SmallBusNewContact01_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 1, 3, getProp("SmallBusNewContact01_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 1, 4);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("SmallBusNewContact01_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * @throws SQLException
	 * 
	 * 
	 */
	@Test(priority = 6)
	public void verifyCrmSmallBusExistingContact02() throws SQLException {

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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Justin", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		String commsId = getCommsIDToUpdateFromSMS(olId);
		updateCommunicationStatus("sent", commsId);
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify the expected values
		String acctName = getDisplayedText(onlinerequestrecordview.requestAccountName, true);
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String company = getDisplayedText(onlinerequestrecordview.moveOutCompany, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("data-title"));
		String accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String contactSecretCodeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("data-title"));
		String contactCompanyTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("data-title"));
		String accountImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccount.get(0).getAttribute("src"));
		String accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		String contactSecretCodeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactSecretCode.get(0).getAttribute("src"));
		String contactCompanyImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueCompany.get(0).getAttribute("src"));
		softAssertion.assertEquals(acctName, "100060003595 (MR JUSTIN O'DAY)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("SmallBusExistingContact02_moveOutDate2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Bowen CDS Sydney, Australian Capital Territory, 90210",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Small Business",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd,
				"Address-#01 Address-#02 Address-#03 Address-#04 Dressrosa, East Blue, 90210 US",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactName, "Justin O'day (Will be merged with main contact Justin O'Day)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+61298710987",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAhoursPhone, "+61486501260",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactMobPhone, "0332878850",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactEmailAdd, getProp("test_email_dummy_upper_case"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCode, "sekrekt's code #01",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(company,
				concatStrings(getProp("test_data_cancelled_acn1_acn2"), " T/a OP LLC's ",
						getProp("test_data_cancelled_acn1")),
				assertionErrorMsg(getLineNumber()));
		// In the portal SMS was chosen however on the CRM, SMS is not enabled
		// that's why it's not showing here
		softAssertion.assertEquals(notifications, "Bills (Email)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("SmallBusExistingContact02_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(accountTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(accountTypeTooltip,
				"Account type supplied does not match existing account’s which is set to Residential",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactNameTooltip),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactSecretCodeTooltip,
				"Secret code supplied does not match the existing contact’s which is set to sekrekt'scode#01",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactCompanyTooltip,
				"The existing account is not a commercial account and cannot compare details",
				assertionErrorMsg(getLineNumber()));
		// verify the correct image displayed
		softAssertion.assertTrue(accountImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(accountTypeImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_correct.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactSecretCodeImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactCompanyImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		crmScrollPage(10, Keys.DOWN);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 3,
				assertionErrorMsg(getLineNumber()));
		// verify the email sent
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		// verify the first record
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 0, "SMS");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 1, "Online Request SMS for Move Out");
		crmVerifyListOrSubpanelEqualsValue(comms, 0, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 0, 3, getProp("SmallBusExistingContact02_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 0, 4);
		// verify the second record
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 0, "Emails");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 1, "Online Request Email for Move Out sent to O'day, Justin");
		crmVerifyListOrSubpanelEqualsValue(comms, 1, 2, "Sent");
		crmVerifyListOrSubpanelStartsWith(comms, 1, 3, getProp("SmallBusExistingContact02_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(comms, 1, 4);
		// verify all assertions
		softAssertion.assertAll();

		// verify the attached notes
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 1);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Additional Note from Customer");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("SmallBusExistingContact02_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("SmallBusExistingContact02_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("SmallBusExistingContact02_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

	/**
	 * - verify that the hierarchy for checking for the existing contact to be
	 * linked is
	 * 
	 * - First and Last name AND - Email Address OR - Mobile Phone OR - Business
	 * Phone OR - After Hours Phone
	 * 
	 * @throws SQLException
	 * 
	 */
	@Test(priority = 7)
	public void verifyCrmResiExistingContact03() throws SQLException {

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

		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();

		// navigate into the accounts list view
		crmOpenListView("bbcrm_OnlineRequests", null, false);
		WebElement onlineReq = crmGetListViewTableWithSearch("Buckle", false, 1, false, false);
		// verify only 1 record is displayed
		crmVerifyNumOfRecordsInListViewOrSubpanel(onlineReq, 1);
		
		crmClickRecordExactLinkText("Move Out");
		crmSwitchToWindow(1, true, 0, Keys.DOWN);
		// let's update the sms to sent
		String olId = getIdOfOnlineRequest();
		String commsId = getCommsIDToUpdateFromSMS(olId);
		updateCommunicationStatus("sent", commsId);
		this.onlineReqId = olId;
		// let's click the show more
		clickElementAction(onlinerequestrecordview.showMoreLink);
		// verify account name not displayed
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		assertFalse(isElementExists(onlinerequestrecordview.requestAccountNameList), "Account Name field is displayed");
		setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		// verify the expected values
		String request = getDisplayedText(onlinerequestrecordview.moveOutRequestValue, true);
		String progress = getDisplayedText(onlinerequestrecordview.moveOutProgressValue, true);
		String dateReq = getDisplayedText(onlinerequestrecordview.moveOutDateRequiredValue, true);
		String reqTenancy = getDisplayedText(onlinerequestrecordview.moveOutRequestedTenancyValue, true);
		String acctType = getDisplayedText(onlinerequestrecordview.moveOutAccountTypeValue, true);
		String forwAdd = getDisplayedText(onlinerequestrecordview.moveOutForwardingAddressValue, true);
		String dateSubmitted = getDisplayedText(onlinerequestrecordview.moveOutDateSubmittedValue, true);
		String dateCreated = getDisplayedText(onlinerequestrecordview.moveOutDateCreatedValue, true);
		String contactName = getDisplayedText(onlinerequestrecordview.moveOutContactNameValue, true);
		String contactBusPhone = getDisplayedText(onlinerequestrecordview.moveOutContactBusPhoneValue, true);
		String contactAhoursPhone = getDisplayedText(onlinerequestrecordview.moveOutContactAhrPhoneValue, true);
		String contactMobPhone = getDisplayedText(onlinerequestrecordview.moveOutContactMobPhoneValue, true);
		String contactEmailAdd = getDisplayedText(onlinerequestrecordview.moveOutContactEmailAddValue, true);
		String contactSecretCode = getDisplayedText(onlinerequestrecordview.moveOutContactSecretCodeValue, true);
		String contactBirthdate = getDisplayedText(onlinerequestrecordview.moveOutContactBirthdateValue, true);
		String contactPersonalId = getDisplayedText(onlinerequestrecordview.moveOutContactPersonalIdValue, true);
		String notifications = getDisplayedText(onlinerequestrecordview.moveOutNotificationsValue, true);
		String dateCompleted = getDisplayedText(onlinerequestrecordview.moveOutDateCompletedValue, true);
		String dateModified = getDisplayedText(onlinerequestrecordview.moveOutDateModifiedValue, true);
		String accountTypeTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("data-title"));
		String contactNameTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("data-title"));
		String accountTypeImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueAccountType.get(0).getAttribute("src"));
		String contactNameImgSrcTooltip = normalizeSpaces(
				onlinerequestrecordview.moveOutTooltipValueContactName.get(0).getAttribute("src"));
		softAssertion.assertEquals(request, "Move Out",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(progress, "New / Waiting Verification Normal / Normal",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateReq, getProp("ResiExistingContact03_moveOutDate2"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(reqTenancy, "Q1 Surfers Paradise BVD Gold Coast, Queensland, 4217",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(acctType, "Residential",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(forwAdd, "Surfers Paradise BVD Gold Coast, QLD, 4217",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateSubmitted.contains(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateCreated.contains(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		// README
		// bug documented in ticket BBCRM-9754
		softAssertion.assertEquals(contactName, "roger buckle (Will be merged with additional contact Roger Buckle)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBusPhone, "+3970310054324",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactAhoursPhone, "+485690",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactMobPhone),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactEmailAdd),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(StringUtils.isBlank(contactSecretCode),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactBirthdate, "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactPersonalId, "Not Specified",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(notifications, "Bills (None)",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(dateCompleted, "Pending",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(dateModified.contains(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		String dateSubmittedUpd = dateSubmitted + " by";
		softAssertion.assertEquals(dateSubmittedUpd, dateCreated,
				assertionErrorMsg(getLineNumber()));
		// verify the tooltip message
		softAssertion.assertEquals(accountTypeTooltip,
				"Account type supplied does not match existing account’s which is set to",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(contactNameTooltip, "Specified contact is not associated with the specified account",
				assertionErrorMsg(getLineNumber()));
		// verify no image is displayed
		onlinerequestrecordview = new OnlineRequestRecordView(driver, 0);
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueAccount),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactSecretCode),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactBirthDate),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertFalse(isElementExists(onlinerequestrecordview.moveOutTooltipValueContactPersonalId),
				assertionErrorMsg(getLineNumber()));
		setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		softAssertion.assertTrue(accountTypeImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertTrue(contactNameImgSrcTooltip.contains("status_error.svg"),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the fix for bug ticket BBCRM-9753
		// verify the communications record
		crmScrollPage(10, Keys.DOWN);
		// sort again to refresh the content
		crmSortTableAscWithinPanel("Communications", 1, false);
		// verify the number of records
		WebElement comms = crmSortTableDescWithinPanel("Communications", 1, false);
		softAssertion.assertEquals(crmGetNumOfRecordsInSubpanel("Communications", false), 1,
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 0), "Emails",
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 1),
				concatStrings("BlueBilling Move Out ", getInstanceIdMoveOut(), " WEB_FORM roger buckle"),
				assertionErrorMsg(getLineNumber()));
		// to ensure that Status == Sent, make sure that the config_override.php is set
		// to
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['enabled']
		// = true;
		// $sugar_config['bluebilling']['bbcrm_OnlineRequests']['send_remote']['email_address']
		// = 'success@simulator.amazonses.com';
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 2), "Sent",
				assertionErrorMsg(getLineNumber()));
		String dateCreatedWebForm = crmGetListOrSubpanelValue(comms, 0, 3);
		softAssertion.assertTrue(dateCreatedWebForm.startsWith(getProp("ResiExistingContact03_dateSubmittedSlash")),
				assertionErrorMsg(getLineNumber()));
		softAssertion.assertEquals(crmGetListOrSubpanelValue(comms, 0, 4), "Administrator",
				assertionErrorMsg(getLineNumber()));
		// verify that no SMS record was created
		softAssertion.assertTrue(StringUtils.isBlank(getIdOfEmailOrSMS("bbcrm_smsrecipients", "roger buckle")),
				assertionErrorMsg(getLineNumber()));
		// verify that no Email record was created
		softAssertion.assertTrue(
				StringUtils.isBlank(
						getIdOfEmailOrSMS("emails", "Online Request Email for Move Out sent to buckle, roger")),
				assertionErrorMsg(getLineNumber()));
		// verify all assertions
		softAssertion.assertAll();

		// verify the attached notes
		crmScrollPage(14, Keys.DOWN);
		WebElement notes = crmSortTableDescWithinPanel("Notes", 1, false);
		// verify the number of records created
		crmVerifyNumOfRecordsInListViewOrSubpanel(notes, 1);
		// verify the 1st row
		crmVerifyListOrSubpanelEqualsValue(notes, 0, 1, "Request Acceptance Details");
		crmVerifyListOrSubpanelStartsWith(notes, 0, 2, getProp("ResiExistingContact03_dateSubmittedSlash"));
		crmVerifyListOrSubpanelStartsWith(notes, 0, 3, getProp("ResiExistingContact03_dateSubmittedSlash"));
		crmVerifyListorSubpanelValueIsBlank(notes, 0, 4);
		// verify preview
		WebElement preview = crmGetPreview(notes, 0);
		crmVerifyIfPreviewValueCorrect(preview, 2, "Request Acceptance Details");
		crmVerifyIfPreviewValueIsBlank(preview, 3);
		crmVerifyIfPreviewValueCorrect(preview, 4, "Not Specified");
		crmVerifyIfPreviewValueCorrect(preview, 5,
				"(ACCEPTED) I/We agree to the <a href=\"https://www.blueoak.com.au/terms-and-condition.pdf\" target=\"_blank\" rel=\"nofollow\">Terms and Conditions</a> and also acknowledge <a href=\"https://www.blueoak.com.au/our-prices\" target=\"_blank\" rel=\"nofollow\">BlueOak's Standard Fee Schedule</a>. (ACCEPTED) I/We have added enquiries@blueoak.com.au to my email contacts or white list where required");
		crmVerifyIfPreviewValueCorrect(preview, 6, "Global (Primary)");
		crmVerifyIfPreviewValueIsBlank(preview, 7);
		crmVerifyIfPreviewValueIsBlank(preview, 8);

		// let's verify the source ID from the session
		// and the one saved in the DB are the same
		String dbSourceId = getOnlineRequestDbValue("request_id_at_source", this.onlineReqId);
		verifyTwoStringsAreEqual(dbSourceId, getProp("ResiExistingContact03_sourceID"), true);
		// also confirm the instance ID saved in the DB
		String dbInstanceId = getOnlineRequestDbValue("instance_id", this.onlineReqId);
		verifyTwoStringsAreEqual(dbInstanceId, super.getInstanceIdMoveOut(), true);
	}

}