package au.com.blueoak.portal.prod.make_payment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import au.com.blueoak.portal.pageObjects.make_payment.Buttons;
import au.com.blueoak.portal.prod.ProdBaseTesting;

public abstract class AbstractMakePayment extends ProdBaseTesting {
	
	/** This is where the test data are located */
	private static final String PROP_TEST_DATA = "TestDataProdMakePayment.properties";
	
	private static Properties prop = new Properties();
	
	/** 
	 * Load the property details
	 *  */
	static {

		try {
			// let's initialize the property file before running any tests
			InputStream input = new FileInputStream(PROP_TEST_DATA);
			prop = new Properties();
			prop.load(input);
			if (LOG.isInfoEnabled()) {
				LOG.info("Successfully loaded this property file '" + PROP_TEST_DATA + "' for the test data");
			}
		} catch (FileNotFoundException fnfe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("FileNotFoundException is encountered. See message for more details -> " + fnfe.getMessage());
			}
		} catch (IOException ioe) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("IOException is encountered. See message for more details -> " + ioe.getMessage());
			}
		}
	}
	
	/** 
	 * Use this to verify if loading status has appeared
	 * */
	private boolean isLoadDisplayed(long timeout, boolean waitToAppear) {
		
		FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);
		
		// check if the "Loading" status to appear
		boolean loadingPresent = false;
		try {
			setImplicitWait(1);
			// check if the "loading" element is present in the table
			loadingPresent = fluentWait.until(
					new Function<WebDriver, Boolean>() {
						public Boolean apply(WebDriver driver) {
							logDebugMessage("Checking the Loading screen");

							boolean appeared = driver.findElement(By.xpath(
									"//div[@class='container ng-star-inserted']/mat-card")).isDisplayed();
							
							if (appeared) {
								logDebugMessage("The mat-card element was found");
							} else {
								logDebugMessage("The mat-card element was not found");
							}

							logDebugMessage("Loading screen "
									+ (appeared ? "appeared" : "not displayed")
									+ ", we are waiting a maximum of "
									+ timeout + " seconds for it to "
									+ (waitToAppear ? "appear" : "disappear"));

							// see if we are waiting for it to appear or
							// disappear
							if (waitToAppear && appeared)
								return (true);
							else if (waitToAppear && !appeared)
								return (false);
							else if (!waitToAppear && appeared)
								return (false);
							else
								return (true);

						}
					}).booleanValue();
		} catch (Exception exception) {
			logDebugMessage("Done waiting a maximum of " + timeout
					+ " seconds for the Loading screen to "
					+ (waitToAppear ? "appear" : "disappear"));
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}

		return (loadingPresent);
	}
	
	/** 
	 * Use this to verify that the Make Payment button is enabled or disabled
	 * */
	protected boolean isMakePaymentBtnEnabled() {
		
		boolean isButtonEnabled;
		
		try {
			setImplicitWait(1);
			Buttons buttons = new Buttons(driver);
			String val = buttons.makePayment.getAttribute("disabled");
			if (val != null) {
				logDebugMessage("The disabled attribute is found on the Make Payment button - Make Payment button is disabled");
				isButtonEnabled = false;
			} else {
				logDebugMessage("The disabled attribute is not found on the Make Payment button - Make Payment button is not disabled");
				isButtonEnabled = true;
			}
		} catch (NoSuchElementException nsee) {
			logDebugMessage("The disabled attribute is not found on the Make Payment button - Make Payment button is not disabled");
			isButtonEnabled = true;
		} finally {
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
		
		return isButtonEnabled;
	}
	
	/**
	 * Use this when you are expecting for the loading screen
	 * to appear and disappear
	 *  */
	protected void load() {
		
		if (isLoadDisplayed(PORTAL_LOAD_TIMEOUT_START, true)) {
			// loading screen appeared, now let's wait
			// until it disappears
			isLoadDisplayed(PORTAL_LOAD_TIMEOUT_END, false);
		}
	}
	
	/**
	 * Get the value of the property
	 */
	protected String getProp(String key) {

		String value = prop.getProperty(key);
		logDebugMessage("The value to be returned by getProp(String) using the key (" + key + ") is '" + value + "'");
		return value;
	}
	
	/** 
	 * Use this to switch into the Bluebilling Iframe
	 * for payment portals embedded.
	 * 
	 * Not sure why but when just using the normal switch
	 * into the Bluebilling Iframe, it waits for the 
	 * IMPLICIT_WAIT_TIMEOUT default value (even if the frame is already present)
	 * before executing the next code.
	 * 
	 * So we will just lower the implicit wait when switching into the bb iframe
	 * */
	protected void switchToBlueBIframe(long implicitWaitInSec) {
		
		try {
			// set a smaller time implicit wait
			setImplicitWait(implicitWaitInSec);
			logDebugMessage("Will be switching in the 'bluebilling-make-payment-iframe' iframe");
			// let's switch into the bluebilling iframe
			driver.switchTo().frame("bluebilling-make-payment-iframe");
			logDebugMessage("Successfully switched in the 'bluebilling-make-payment-iframe' iframe");
		} finally {
			// return the orignal value of the implicit wait
			setImplicitWait(PORTAL_IMPLICIT_WAIT_TIMEOUT);
		}
	}

}
