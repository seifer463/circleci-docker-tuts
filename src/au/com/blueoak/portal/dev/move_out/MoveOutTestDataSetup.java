package au.com.blueoak.portal.dev.move_out;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.commons.vfs2.FileSystemException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import au.com.blueoak.portal.ErrorMessageException;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class MoveOutTestDataSetup extends MoveOutDevBase {
	
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
		updateRecForInstanceID("bbeng_billers", "instance_id", getInstanceIdMoveOut(), 1);
		updateRecForInstanceID("bbdef_online_request_strategies", "instance_id", getInstanceIdMoveOut(), 1);
		// ensure that the snapshot values are updated for accounts an complexes
		updateSnapShotValues(today);
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		// upload the attachment in the engine artifacts
		// that would be attached in the email that will be sent
		// when submitting Move Out online request
		try {
			String fileToUpload = ONLINE_REQUESTS_ENGINE_ARTIFACTS_DIR
					.concat("move_out\\4C10FF87622270CF05C22D059158DFF2");
			String s3FileNameToReplace = "4C10FF87622270CF05C22D059158DFF2";
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
				.sendKeys(getProp("test_email_dummy_upper_case"));
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
		
		// For the scenario where same First, Last Name and Mobile Phone
		// even though there's a parenthesis, it should be picked up
		// and on the mobile phone even though in the portal it was specified as +61469941139
		// but here on the CRM it was specified as 0469941139
		// it should still be linked here
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("(Tom)", false, 1, false, false);
		crmClickRecordExactLinkText("(Tom) Tri Ly");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		driver.findElement(
				By.xpath("//input[@class='newEmail input-append' and @type='text' and @placeholder='Add email']"))
				.sendKeys("tom.tri@dontsend.com");
		// enter the mobile phone +61 469 9411 39
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("04 6994 1139");
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
		// enter the business phone 61 4690 41930
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("61 4690 41930", Keys.TAB);
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
		
		crmOpenListView("Contacts", null, false);
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
		// enter the after hours phone 61 8 2301 4785
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("61 8 2301 4785");
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
		
		// here we will update this contact
		// and ensure that the portal contact will not be linked to this contact
		// since it's missing 1 number
		crmOpenListView("Contacts", null, false);
		crmGetListViewTableWithSearch("L Sanger", false, 1, false, false);
		crmClickRecordExactLinkText("Ms. L Sanger");
		// switch to the new window
		crmSwitchToWindow(1);
		// Select the edit button
		driver.findElement(By.xpath("//a[@name='edit_button']")).click();
		// enter the mobile phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("130056908");
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
		
		// update the record for Justin O'Day
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
				.sendKeys(getProp("test_email_dummy_upper_case"));
		// enter the mobile phone +6235298750 that's a little different from the portal
		// just added another digit
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("00332878850");
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
		// this should be picked up as an existing contact
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
		// enter the mobile phone 0332878850 that's a little different from the portal
		// without the plus sign
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_mobile']"))
				.sendKeys("+0332878850");
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
				.sendKeys(getProp("test_email_dummy_upper_case"));
		// enter the business phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_work']"))
				.sendKeys("3970 3100 54324");
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
				.sendKeys(getProp("test_email_dummy_upper_case"));
		// enter the after hours phone
		driver.findElement(
				By.xpath("//input[@type='text' and @name='phone_after_hours']"))
				.sendKeys("+485690");
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
		
		// this should be at the last part
		// to ensure that even if the engine is off
		// all other commands were executed
		softAssertion.assertAll();
	}

}
