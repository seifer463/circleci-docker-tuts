package au.com.blueoak.portal.prod;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import au.com.blueoak.portal.BaseTesting;

public abstract class ProdBaseTesting extends BaseTesting {
	
	
	/** 
	 * Let's start the test by initializing the browser
	 * using an 'en' language then open the test portal
	 * 
	 * */
	@BeforeClass
	public void startTest() {
		
		super.setupTestProp();
		super.initChromeDriver("en");
	}
	
    /**
     * Capture the screenshot of the failed test cases.
     * Note that it does not include the URL.
     */
	@AfterMethod
	public void captureFailedTest(ITestResult testResult) {
		
		// check if the last test method has failed
		if (testResult.getStatus() == ITestResult.FAILURE) {
			logDebugMessage("An error in the test case occured, will be getting a screenshot");
			// if directory to save the screenshot
			// does not exist - make one
			File dir = new File(TEST_FAILED_SCREENSHOTS);
			if (!dir.exists())
				dir.mkdirs();
			
			try {
				// construct the file name from the method that just failed
				String methodName = testResult.getMethod().toString();
				// take a screenshot of the current screen
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				// save the screenshot
				File screenshot = new File(TEST_FAILED_SCREENSHOTS
						+ methodName.substring(0, methodName.indexOf("("))
						+ ".jpg");
				FileUtils.copyFile(scrFile, screenshot);
			} catch (IOException e) {
				logDebugMessage("An exception occured while trying to save the screenshot for the failed test case. See exception message for details -> "
						+ e.getStackTrace());
			}
		}
		
		// close all opened windows except the current one
		closeOpenedWindows(true);
	}
	
	/** 
	 * Close all connections after all test cases finished running
	 * */
	@AfterClass(alwaysRun = true)
	public void finishTest() {
		
		logDebugMessage("All test cases are finished executing, will be closing all connections.");
		tearDown();
	}

}
