package au.com.blueoak.portal.dev.move_out;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import au.com.blueoak.portal.dev.DevBaseTesting;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class MoveOutDevBase extends DevBaseTesting {
	
	/** This is where the test data are located */
	private static final String PROP_TEST_DATA = "TestDataDevMoveOut.properties";
	
	/** This is where we would save the test data */
	private static final String PROP_SAVED_TEST_DATA = "log/SavedTestDataDevMoveOut.properties";
	
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
	 * Constructor when initializing this class
	 * */
	public MoveOutDevBase() {
		
		super.setupTestProp();
	}
	
	/**
	 * Add or update a property value.
	 *
	 * @param key
	 * @param value
	 */
	protected void addProp(String key, String value) {

		prop.put(key, value);
		logDebugMessage(concatStrings("Successfully added the value '", value, "' into the property key '", key, "'"));
	}
	
    /**
     * Save the properties to a file.
     */
	protected void saveProp() {

		try {
			final OutputStream output = new FileOutputStream(PROP_SAVED_TEST_DATA);
			prop.store(output, null);
			logDebugMessage(concatStrings("Successfully saved this property file -> '", PROP_SAVED_TEST_DATA, "'"));
		} catch (FileNotFoundException fnfe) {
			logDebugMessage(
					"FileNotFoundException was encountered while trying to save properties file. See error message for more details -> "
							+ fnfe.getMessage());
		} catch (IOException ioe) {
			logDebugMessage(
					"IOException was encountered while trying to save properties file. See error message for more details -> "
							+ ioe.getMessage());
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
	 * Use this to get the header with the specified header name
	 * 
	 * 
	 * @param headerName is the header name for each section
	 * @param numOfHeadersDisp is the number of headers displayed.
	 * */
	protected WebElement getElementSectionHeader(String headerName, int numOfHeadersDisp) {
		
		logDebugMessage("Will be looking for the element that contains the header name '" + headerName
				+ "' from the Acceptance section");
		WebElement foundElement = null;
		for (int i = 0; i < numOfHeadersDisp; i++) {
			WebElement element = driver.findElement(By.xpath("//*[@id='cdk-step-label-0-" + i + "']"));
			logDebugMessage("We are now on this element '" + element.toString() + "'");

			if (element != null) {
				String webElementStrRaw = element.getText().toLowerCase();
				String webElementStr = StringUtils.normalizeSpace(webElementStrRaw);
				logDebugMessage("The value of webElementStr '" + webElementStr + "'");
				if (webElementStr.contains(headerName.toLowerCase())) {
					foundElement = element;
					logDebugMessage("WebElement is found. The '" + webElementStr + "' contains the supplied label '"
							+ headerName.toLowerCase() + "'");
					break;
				}
			}
		}

		return foundElement;
	}
	
	/**
	 * Use this to scroll the page up for Move Out form
	 * 
	 */
	protected void scrollPageUp(int yPixels) {
		
		if (getPortalType().equals("standalone")) {
			// standalone forms need more pixels
			int standAlonePixels = yPixels + 150;
			pageScrollUp(standAlonePixels);
		} else if (getPortalType().equals("embedded")) {
			// we need to switch out first from the move out iframe
			// before we can scroll up
			switchToDefaultContent();
			// scroll up the page
			pageScrollUp(yPixels);
			// go back to the move in iframe
			switchToMoveOutEmbeddedIframe(1);
		}
	}
	
	/**
	 * Use this to scroll the page down for Move Out form
	 * 
	 */
	protected void scrollPageDown(int yPixels) {
		
		if (getPortalType().equals("standalone")) {
			// standalone forms need more pixels
			int standAlonePixels = yPixels + 150;
			pageScrollDown(standAlonePixels);
		} else if (getPortalType().equals("embedded")) {
			// we need to switch out first from the move out iframe
			// before we can scroll up
			switchToDefaultContent();
			// scroll up the page
			pageScrollDown(yPixels);
			// go back to the move in iframe
			switchToMoveOutEmbeddedIframe(1);
		}
	}
	
	/** 
	 * Upload Move Out specific custom CSS
	 * */
	public void uploadMoveOutCustomCss(AccessS3BucketWithVfs s3Access) {

		uploadCustomCss(s3Access, PORTAL_CUSTOM_CSS_DIR, "portal_config.css");
	}
	
	/**
	 * Upload Move Out specific portal config json file
	 */
	public void uploadMoveOutConfig(AccessS3BucketWithVfs s3Access, String directoryNum,
			String fileToUploadOrReplace) {

		uploadConfig(s3Access, MOVE_OUT_PORTAL_CONFIGS_DIR, directoryNum, fileToUploadOrReplace);
	}

}