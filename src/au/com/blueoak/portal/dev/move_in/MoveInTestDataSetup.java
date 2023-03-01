package au.com.blueoak.portal.dev.move_in;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.commons.vfs2.FileSystemException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.ErrorMessageException;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class MoveInTestDataSetup extends MoveInDevBase {
	
	/**
	 * Use this to run all stored procedures regarding the snap shot values in the
	 * Accounts, Accounts Transactions, Service Points, Gate Meters and Complexes
	 * 
	 * @param date should be in db format date. (yyyy-MM-dd)
	 * 
	 * @throws SQLException
	 */
	private void updateSnapShotValues(String date) throws SQLException {

		updateServiceStatusAll(date);
		updateAccountStatusAll(date);
		updateAccountTransactionsAll("2001-01-01");
	}
	
	/** 
	 * let's update the records that would use the value of instance id
	 * 
	 * @throws SQLException 
	 * */
	private void updateRecForInstanceID(String tableName, String instanceIdColumnName, String valueToUpdate,
			int idOfRecord) throws SQLException {

		String query = new StringBuilder("UPDATE ").append(tableName).append(" SET ").append(instanceIdColumnName)
				.append(" = '").append(valueToUpdate).append("' WHERE `id` = ").append(idOfRecord).append(";")
				.toString();
		
		executeUpdate(query);
	}

	/**
	 * Let's insert all the data we will use in testing the Move Out
	 */
	@Test(priority = 1)
	public void setupTestData() throws SQLException {
		
		// initialize the Soft Assert
		SoftAssert softAssertion = new SoftAssert();
		
		executeSqlScript("clean_up/engine_database_clean", false);
		executeSqlScript("clean_up/crm_database_clean", false);
		executeSqlScript("sample_test_data/sample_test_data_for_report", false);
		// run the migration script for the communications
		// to transfer it to the new table
		executeSqlScript("update_scripts/communications_migration", false);
		// get the current date today
		// then format in DB date
		String today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DB_DATE_FORMAT);
		// add the strategies
		executeSqlScript("sample_test_data/online_request_strategies_and_stages", false);
		// add the email, sms and pdf templates
		executeSqlScript("sample_test_data/online_requests_sms_email_pdf_templates", false);
		// add the instance id
		updateRecForInstanceID("bbeng_billers", "instance_id", getInstanceIdMoveIn(), 1);
		updateRecForInstanceID("bbdef_online_request_strategies", "instance_id", getInstanceIdMoveIn(), 2);
		// ensure that the snapshot values are updated for accounts an complexes
		updateSnapShotValues(today);
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		// upload the attachment in the engine artifacts
		// that would be attached in the email that will be sent
		// when submitting Move In online request
		try {
			String fileToUpload = ONLINE_REQUESTS_ENGINE_ARTIFACTS_DIR
					.concat("move_in\\A52B0D0913CF565CC5871BF727501221");
			String s3FileNameToReplace = "A52B0D0913CF565CC5871BF727501221";
			s3Access.uploadEngineArtifactsForOnlineReqAttachments(fileToUpload, S3_CRM_ENGINE_ARTIFACTS_BUCKET_NAME,
					getAwsEngineArtifactsFolderName(), s3FileNameToReplace);
			logDebugMessage(concatStrings("Uploaded the engine artifact '", s3FileNameToReplace, "' located in '",
					fileToUpload, "' into the S3 bucket '", S3_CRM_ENGINE_ARTIFACTS_BUCKET_NAME,
					"', inside the directory '", getAwsEngineArtifactsFolderName(), "'"));
		} catch (FileSystemException fse) {
			logDebugMessage(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage()));
			throw (new ErrorMessageException(
					concatStrings("A FileSystemException has been encountered. Please see error for more details -> ",
							fse.getMessage())));
		}
		
		// login into the CRM
		crmLogin(true);
		
		// ensure that the All SMS config are removed
		crmRemoveSMSConfig();
		
        // let's populate the email settings
		crmSetEmailSettings("Bluebilling Dev", "bluebilling.dev@bluebilling.com.au",
				"other", true, "email-smtp.us-west-2.amazonaws.com",
				"AKIA6PTETTD5CLOCEDGA",
				"BMQplPnx3kUk4xIMFTqMtGkvbJRpnaOOqEAyG9fFjAi8", true, "587",
				"TLS");

		// turn off all BlueBilling schedulers
		crmMassUpdateSchedulerStatus("(BlueBilling)", false);
		
		// turn on Online Request schedulers
		crmMassUpdateSchedulerStatus("(BlueBilling) Online Request Notifications", true);
		
		softAssertion.assertTrue(crmVerifyEngineState(), "The engine is not turned On!");
		
		// Let's update some existing contact 
		// open the contact list view
		crmOpenListView("Contacts", null, false);
		String mainWindow = crmGetWindowHandle();
		
		// For the scenario where same First, Last Name and Email address
		crmGetListViewTableWithSearch("Michael O'Connell", false, 1, false, false);
		crmClickRecordExactLinkText("Mr. Michael O'Connell");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys(getProp("test_dummy_email_lower_case"));
		// enter the mobile phone +613249469510
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+613249469510");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email", "sms");
		crmSetContactNotificationInRow(3, "email", "sms");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Justin O'Day
		// Online request contact should be linked in this contact
		// although the email address typed in the Online Request is upper case
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("O'Day", false, 1, false, false);
		crmClickRecordExactLinkText("Justin O'Day");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys(getProp("test_dummy_email_lower_case"));
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("0335298750");
		// enter the contact secret code that matches the portal
		// only that there's no spaces here but portal has
		driver.findElement(By.xpath("//input[@type='text' and @name='secret_code']")).sendKeys("sekrekt'scode#01");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Mr. Justin O'Day
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("O'Day", false, 1, false, false);
		crmClickRecordExactLinkText("Mr. Justin O'Day");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("other.oday.donotsend@testing.com");
		// enter the mobile phone to be entered in the portal
		// but still should not be linked to this contact because of the hierarchy
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+61235298750");
		// fix the org.openqa.selenium.ElementNotInteractableException
		pauseSeleniumExecution(1000);
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']")).click();
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("+451 2555 566", Keys.TAB);
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		crmOpenListView("Contacts", null, false);
		// For the scenario where same First, Last Name and Mobile Phone
		// even though there's a parenthesis, it should be picked up
		// and on the mobile phone on the portal + sign included
		crmGetListViewTableWithSearch("(Tom)", false, 1, false, false);
		crmClickRecordExactLinkText("(Tom) Tri Ly");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("tom.tri.ly@testing.com");
		// enter the mobile phone +61 469 9411 39
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("61 469 9411 39");
		// enter the contact secret code
		driver.findElement(By.xpath("//input[@type='text' and @name='secret_code']")).sendKeys("Contact Secret Code");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email", "sms");
		crmSetContactNotificationInRow(3, "email", "sms");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		crmOpenListView("Contacts", null, false);
		// For the scenario where same First, Last Name and Business Phone
		// even though on the portal there's + sign but this one does not have
		crmGetListViewTableWithSearch("Brad Harrison", false, 1, false, false);
		crmClickRecordExactLinkText("Brad Harrison");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email as Brad.should.not.send@testing.com
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Brad.should.not.send@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("61 3 9113 2260", Keys.TAB);
		// fix the org.openqa.selenium.ElementNotInteractableException
		pauseSeleniumExecution(1000);
		// enter the contact secret code that matches the portal
		// only that the case is not the same
		driver.findElement(By.xpath("//input[@type='text' and @name='secret_code']")).sendKeys("sekret's 000");
		// enter the Birthdate that matches the portal
		int birthYrRaw = Calendar.getInstance().get(Calendar.YEAR)-18;
		String birthYr = Integer.toString(birthYrRaw);
		today = getCurrentDateWithTimeZone(MELBOURNE_TIME_ZONE, DATE_MONTH_YEAR_FORMAT_SLASH);
		String validBirthDate = getString(today, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		driver.findElement(By.xpath("//input[@type='text' and @aria-label='Birthdate']")).sendKeys(validBirthDate);
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Paul & Mary Toniolo that is linked to account 100060003991
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Toniolo", false, 1, false, false);
		crmClickRecordExactLinkText("Paul & Mary Toniolo");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Paul.Mary.Toniolo@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("61 2 9873 2550", Keys.TAB);
		// fix the org.openqa.selenium.ElementNotInteractableException
		pauseSeleniumExecution(1000);
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Ms. Emma Harding-Grimmond that is linked to account 100060003793
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Harding-Grimmond", false, 1, false, false);
		crmClickRecordExactLinkText("Ms. Emma Harding-Grimmond");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Emma.Harding.Grimmond@testing.com");
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("61 2 3698 5000", Keys.TAB);
		// fix the org.openqa.selenium.ElementNotInteractableException
		pauseSeleniumExecution(1000);
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Roger Buckle that is linked to account 100060007190
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Buckle", false, 1, false, false);
		crmClickRecordExactLinkText("Roger Buckle");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("roger.that@testing.com");
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("61 2 3698 5000");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
				
		// update the record for Prof. Roger & Coll Buckle that is linked to account 100060007190
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Buckle", false, 1, false, false);
		crmClickRecordExactLinkText("Prof. Roger & Coll Buckle");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// update the first name
		driver.findElement(By.xpath("//input[@name='first_name']")).click();
		deleteAllTextFromField();
		driver.findElement(By.xpath("//input[@name='first_name']")).sendKeys("Roger");
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("prof.roger@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("61 3 9874 3250");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Ms. Betty Xu that is linked to account 200010004493
		// should be linked to this record because of the hierarchy
		// 1. Should match Email Address first
		// 2. If not match then try Mobile number next
		// 3. If not match then try Business number next
		// 4. If not match then try After hours number next
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Xu", false, 1, false, false);
		crmClickRecordExactLinkText("Ms. Betty Xu");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("ms.betty.lafea@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("+61 8 1130 8850");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Betty Xu that is not linked to any account
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Xu", false, 1, false, false);
		crmClickRecordExactLinkText("Betty Xu");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("ms.betty.lafea@testing.com");
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("+61 7 1931 2650");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Dominic Blank that is linked to account 100040009399
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Dominic", false, 1, false, false);
		crmClickRecordExactLinkText("Dominic Blank");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("lower.enye.lastname@testing.com");
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("09278169824");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Ms. Melanie Banks that is linked to account 200010003990
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Banks", false, 1, false, false);
		crmClickRecordExactLinkText("Ms. Melanie Banks");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("upper.enye.firstname@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("0829821210");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Mey Yeak that is linked to account 200010014492
		// verify that contact would still be linked here
		// even though in the portal it used 0295663212 as mobile phone
		// but in the CRM it used +612 9566 3212
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Yeak", false, 1, false, false);
		crmClickRecordExactLinkText("Mey Yeak");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// update the first name
		driver.findElement(By.xpath("//input[@name='last_name']")).click();
		deleteAllTextFromField();
		driver.findElement(By.xpath("//input[@name='last_name']")).sendKeys("Yeak Jr.");
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("yeak.jr@testing.com");
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+612 9566 3212");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Mohammad Erfanian-Nozar that is linked to account 100060000997
		// verify that the contact would still be linked here even though in the portal
		// we used the mobile phone as +61700858730
		// however in the CRM we used 07 0085 8730
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Erfanian-Nozar", false, 1, false, false);
		crmClickRecordExactLinkText("Mohammad Erfanian-Nozar");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// update the first name
		driver.findElement(By.xpath("//input[@name='last_name']")).click();
		deleteAllTextFromField();
		driver.findElement(By.xpath("//input[@name='last_name']")).sendKeys("Erfanian-Nozar Sr");
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Mohammad.eli@testing.com");
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("07 0085 8730");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Mr. Peter Tsoubos that is linked to account 100050000890
		// should be linked here even though in the portal email was uppper case
		// and here we used lower case
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Tsoubos", false, 1, false, false);
		crmClickRecordExactLinkText("Mr. Peter Tsoubos");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys(getProp("test_dummy_email_lower_case"));
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("10658520");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Peter Tsoubos that is linked to account 100050000395, 100050000197, 100050000296
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Tsoubos", false, 1, false, false);
		crmClickRecordExactLinkText("Peter Tsoubos");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("peter.parker@testing.com");
		// enter the mobile phone that was entered in the portal
		// but still contact from portal should not be linked to this contact
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("1300852060");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Elizabeth Treonze that is linked to account 100070001597
		// contact in the portal should be linked to this CRM contact
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Treonze", false, 1, false, false);
		crmClickRecordExactLinkText("Elizabeth Treonze");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Elizabeth.Treonze@testing.com");
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("08 0083 8420");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Ms. Elizabeth Treonze that is linked to account 100070002694
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Treonze", false, 1, false, false);
		crmClickRecordExactLinkText("Ms. Elizabeth Treonze");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Ms.Elizabeth.Treonze@testing.com");
		// enter the mobile phone just slightly different from the first Elizabeth Treonze
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("08 0183 8420");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Mr. James Grasso that is linked to account 200010017693
		// should be linked here even though in the portal email was uppper case
		// and here we used lower case
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Grasso", false, 1, false, false);
		crmClickRecordExactLinkText("Mr. James Grasso");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// update the last name
		driver.findElement(By.xpath("//input[@name='last_name']")).click();
		deleteAllTextFromField();
		driver.findElement(By.xpath("//input[@name='last_name']")).sendKeys("Grasso Jr.");
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys(getProp("test_dummy_email_lower_case"));
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+61 4 2522 8522");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		crmOpenListView("Contacts", null, false);
		// For the scenario where same First, Last Name and After Hours Phone
		// even though there's & sign, this should be picked up as existing contact
		crmGetListViewTableWithSearch("Susan", false, 1, false, false);
		crmClickRecordExactLinkText("Susan & Les Smith");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email as Susan.and.Geno@testing.com
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Susan.and.Geno@testing.com");
		// enter the after hours phone 05 56 987 80
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("08 2702 2617");
		// enter the contact secret code different from the portal
		driver.findElement(By.xpath("//input[@type='text' and @name='secret_code']")).sendKeys("#000-Abc's");
		// enter the Birthdate that is different from the portal
		birthYrRaw = Calendar.getInstance().get(Calendar.YEAR)-18;
		birthYr = Integer.toString(birthYrRaw);
		String todayMinus1 = getSpecificDateWithTimeZone(MELBOURNE_TIME_ZONE, -1, DATE_MONTH_YEAR_FORMAT_SLASH);
		validBirthDate = getString(todayMinus1, 0, today.length() - 4);
		validBirthDate = validBirthDate + birthYr;
		driver.findElement(By.xpath("//input[@type='text' and @aria-label='Birthdate']")).sendKeys(validBirthDate);
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		// set a personal identification where the medicare on the portal does not have
		// the reference number while on the contact it has
		// let's update the current expiry of the medicare card so it's updated dynamically
		int expYr = Calendar.getInstance().get(Calendar.YEAR) + 1;
		String expYrStr = Integer.toString(expYr);
		String query = new StringBuilder("UPDATE `bbcrm_personalidentification` SET `medicare_expiration_year` = '")
				.append(expYrStr).append("' WHERE `id` = 'c961bf47-ffcf-12e5-b4e4-5f8d7a871532';").toString();
		executeUpdate(query);
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Rasha Ehsara that is linked to account 200010021190
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Rasha", false, 1, false, false);
		crmClickRecordExactLinkText("Rasha Ehsara");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+09198561256");
		// to trigger an event and to ensure that we can click
		// the sms notifications
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.click();
		// set the notification methods
		crmSetContactNotificationInRow(2, "sms");
		crmSetContactNotificationInRow(3, "sms");
		crmSetContactNotificationInRow(4, "sms");
		crmSetContactNotificationInRow(5, "sms");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Alan Feil that is linked to account
		// verify that the contact would not be linked here
		// because in the portal we used 0471073251 in the business phone
		// and here we used +61 4 71073251
		// 0 and +61 comparing only happens in mobile phones
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Alan Feil", false, 1, false, false);
		crmClickRecordExactLinkText("Alan Feil");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Alan.Feil@testing.com");
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("+61 4 71073251");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Catherine Tripp that is linked to account 100060004395
		// verify that the contact would not be linked here
		// because in the portal we used +61785215055 in the after hours phone
		// and here we used 07 85215055
		// 0 and +61 comparing only happens in mobile phones
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Catherine Tripp", false, 1, false, false);
		crmClickRecordExactLinkText("Catherine Tripp");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Catherine.Tripp@testing.com");
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("07 85215055");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the contact Mr. Sanjeev Dhaka that is linked into the account 100040011395
		// contact should be linked here even though in the portal
		// we used the mobile 0352537499
		// but here we used 613 52537499
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Sanjeev Dhaka", false, 1, false, false);
		crmClickRecordExactLinkText("Mr. Sanjeev Dhaka");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// Enter the email
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("Sanjeev.Dhaka@testing.com");
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("613 52537499");
		// set the notification methods
		crmSetContactNotificationInRow(2, "email");
		crmSetContactNotificationInRow(3, "email");
		crmSetContactNotificationInRow(4, "email");
		crmSetContactNotificationInRow(5, "postal");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// update the record for Peter Nguyen that is linked to account 200010002299
		// contact should not be linked here because the mobile phone is missing 1 number
		// from the one specified in the portal
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("Peter Nguyen", false, 1, false, false);
		crmClickRecordExactLinkText("Peter Nguyen");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+86131090778");
		// to trigger an event and to ensure that we can click
		// the sms notifications
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.click();
		// set the notification methods
		crmSetContactNotificationInRow(2, "sms");
		crmSetContactNotificationInRow(3, "sms");
		crmSetContactNotificationInRow(4, "sms");
		crmSetContactNotificationInRow(5, "sms");
		// save the record
		driver.findElement(By.linkText("Save")).click();
		crmSuccess();
		driver.close();
		crmSwitchCurrentWindow(mainWindow);
		
		// this should be at the last part
		// to ensure that even if the engine is off
		// all other commands were executed
		softAssertion.assertAll();
	}

}
