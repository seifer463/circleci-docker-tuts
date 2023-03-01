package au.com.blueoak.portal.dev.make_payment.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import au.com.blueoak.portal.BaseTesting;
import au.com.blueoak.portal.dev.make_payment.MakePaymentDevBase;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class DisabledJavascript extends BaseTesting {
	
	/** 
	 * Store the name of the class for logging
	 * */
	private String className;
	
	/** 
	 * Initialize the chrome web driver
	 * 
	 * @param language possible values: (en-US, en, fil)
	 * @param disableJavascript
	 * 
	 * */
	@Override
	protected void initChromeDriver(String language) {

		System.setProperty("webdriver.chrome.driver", concatStrings(DRIVERS_DIR, "chromedriver.exe"));
		logDebugMessage(
				concatStrings("Successfully set the chrome driver located in '", DRIVERS_DIR, "chromedriver.exe'"));
		DesiredCapabilities jsCapabilities = DesiredCapabilities.chrome();
		ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("intl.accept_languages", language);
		prefs.put("profile.default_content_setting_values.javascript", 2);
		options.setExperimentalOption("prefs", prefs);
		jsCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		driver = new ChromeDriver(options);
		logDebugMessage(
				concatStrings("Successfully initialized the Chrome WebDriver using the language '", language, "'"));
	}
	
    /** 
     * Let's start a chrome driver to test
     * but uses a different method
     * */
	@BeforeClass
	public void beforeClass() {
		
    	// get the current class for logging
    	this.className = getTestClassExecuting();
    	logTestClassStart(className);
		
		super.setupTestProp();
		this.initChromeDriver("en");
		
		AccessS3BucketWithVfs s3Access = new AccessS3BucketWithVfs(getAwsAccessKeyId(), getAwsSecretAccessKey());
		
		// upload the portal_config.css we are using
		MakePaymentDevBase makePaymentBase = new MakePaymentDevBase();
		makePaymentBase.uploadMakePaymentCustomCss(s3Access);
		
		// upload the portal_config.json we are using
		makePaymentBase.uploadMakePaymentConfig(s3Access, "01\\", "portal_config.json");
		
		// let's access the portal we are testing with
		accessPortal(getStandaloneUrlMakePayment(), true);
	}
	
	@AfterClass(alwaysRun = true)
	public void afterClass() {
		
		logTestClassEnd(className);
		logDebugMessage("All test cases are finished executing, will be closing all connections.");
		tearDown();
	}
	
    /** 
     * Let's log the start of the current test case executing
     * */
	@BeforeMethod
	public void startOfTestCase(ITestResult testResult) {
		
		String testCaseName = testResult.getMethod().toString();
		testCaseName = testCaseName.substring(0, testCaseName.indexOf("("));
		logStartOfCurrentTestCase(testCaseName);
	}
	
    /** 
     * Let's log the end of the current test case executing
     * */
    @AfterMethod
    public void afterMethod(ITestResult testResult) {
    	
		String testCaseName = testResult.getMethod().toString();
		testCaseName = testCaseName.substring(0, testCaseName.indexOf("("));
		logEndOfCurrentTestCase(testCaseName);
    	
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
	 * Verify the message we display is the javascript
	 * is disabled in the browser
	 * */
	@Test(priority = 1)
	public void verifyDispMessage() {

		if (getPortalType().equals("standalone")) {
			
			logDebugMessage("We need to run this test case since we are on the standalone portal type");
			WebElement msg = driver.findElement(By.tagName("p"));
			verifyTwoStringsAreEqual(msg.getText(),
					"To ensure correct and smooth user experience, our portal pages require JavaScript to be enabled in your browser. "
							+ "We have detected that either JavaScript is disabled or is not supported in your browser. "
							+ "Please enable JavaScript or use a browser that has JavaScript enabled, if this is not possible, please contact our customer service team to complete your payment.",
					true);
		}
		
		logDebugMessage("No need to run this test case since we are on the embedded portal type");
	}

}