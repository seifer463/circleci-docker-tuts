package au.com.blueoak.portal.dev.make_payment;

import java.sql.SQLException;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class MakePaymentTestDataSetup extends MakePaymentDevBase {

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
	 * Let's insert all the data we will use in testing the Make Payment
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
		// ensure that the snapshot values are updated for accounts an complexes
		updateSnapShotValues(today);

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
		
		softAssertion.assertTrue(crmVerifyEngineState(), "The engine is not turned On!");
		
		// this should be at the last part
		// to ensure that even if the engine is off
		// all other commands were executed
		softAssertion.assertAll();
	}

}
