package au.com.blueoak.portal.dev;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.google.common.base.Function;

import au.com.blueoak.portal.BaseTesting;
import au.com.blueoak.portal.ErrorMessageException;

public abstract class DevBaseTesting extends BaseTesting {
	
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
	 * Log the name of the test case before running it.
	 * 
	 * */
	@BeforeMethod
	public void startOfTestCase(ITestResult testResult) {
		
		String testCaseName = testResult.getMethod().toString();
		testCaseName = testCaseName.substring(0, testCaseName.indexOf("("));
		logStartOfCurrentTestCase(testCaseName);
	}
	
    /**
     * Log the name of the test case after running it.
     * 
     * Capture the screenshot for failed test cases only.
     * Note that it does not include the URL.
     */
	@AfterMethod
	public void endOfTestCase(ITestResult testResult) {
		
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
				// take a screenshot of the current screen
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				// save the screenshot
				File screenshot = new File(concatStrings(TEST_FAILED_SCREENSHOTS, testCaseName, ".jpg"));
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
	
	/**
	 * Wait for a period of time to see if the page load has appeared.
	 * 
	 * @param timeout
	 * @param waitToAppear
	 * @return indicate if the page load actually appeared or not
	 */
	private boolean crmIsLoadAppeared(long timeout, final boolean waitToAppear) {

		FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);

		// check if the "Loading" status to appear
		boolean loadingPresent = false;
		try {
			// check if the "loading" element is present in the table
			loadingPresent = fluentWait.until(
					new Function<WebDriver, Boolean>() {
						public Boolean apply(WebDriver driver) {
							logDebugMessage("Checking the Loading status");
							
							setImplicitWait(1);
							boolean appeared;
							try {
								appeared = driver.findElement(By.xpath(
										"//div[@id='alerts']/div[@class='alert-wrapper']/div[@class='alert alert-process']"))
										.isDisplayed();
								appeared = true;
							} catch (NoSuchElementException nse) {
								appeared = false;
							}
							
							if (appeared) {
								logDebugMessage("The Loading element WAS found");
							} else {
								logDebugMessage("The Loading element WAS NOT found");
							}

							logDebugMessage("Loading message "
									+ (appeared ? "appeared" : "not displayed")
									+ ", we are waiting a maximum of "
									+ timeout + " seconds for it to "
									+ (waitToAppear ? "appear" : "disappear"));

							// see if we are waiting for it to appear or disappear
							if (waitToAppear && appeared)
								return (true);
							else if (waitToAppear && !appeared)
								return (false);
							else if (!waitToAppear && appeared)
								return (false);
							else if (!waitToAppear && !appeared)
								return (true);
							else
								return (true);

						}
					}).booleanValue();
		} catch (Exception exception) {
			logDebugMessage("Done waiting for " + timeout
					+ " seconds for the 'Loading' message to "
					+ (waitToAppear ? "appear" : "disappear"));
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		return (loadingPresent);
	}
	
	/**
	 * Get the all the rows in the table body, hence excluding any header rows.
	 *
	 * @param table
	 * @return
	 */
	private List<WebElement> crmGetTableheaderRows(WebElement table) {

		// get the body of the table
		WebElement tableBody = table.findElement(By.tagName("thead"));
		// get all the rows in the table and then return the one required
		return (tableBody.findElements(By.tagName("tr")));
	}
	
	/**
	 * Get the entire specified row in the specified table.
	 *
	 * @param table
	 * @param rowNum
	 * @return
	 */
	private WebElement crmGetTableheaderRow(WebElement table, int rowNum) {

		// get all the rows in the table and then return the one required
		return (crmGetTableheaderRows(table).get(rowNum));
	}
	
	/**
	 * Check if there is a page navigation warning dialog showing. The page
	 * navigation warning dialog is the one that is shown warning user about
	 * potentially unsaved data may be lost if they navigate away from the
	 * current page.
	 * 
	 * @param maxWaitTime
	 *            is the max seconds for implicit wait
	 *
	 */
	private boolean crmIsPageNavigationWarningDialogShown(long maxWaitTime) {

		// check if we see the alert warning element
		boolean isDisplayed;

		try {
			// set the implicit wait to smaller time
			setImplicitWait(maxWaitTime);
			logDebugMessage(concatStrings(
					"Checking if the Navigation warning is displayed and will implicitly wait a maximum of ",
					String.valueOf(maxWaitTime), " second(s) for it to display"));

			isDisplayed = driver
					.findElement(
							By.xpath("//div[@class='alert alert-warning alert-block']"))
					.isDisplayed();
		} catch (NoSuchElementException nse) {
			isDisplayed = false;
		} finally {
			// turn back on the implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		logDebugMessage("Page Confirmation Warning dialog is "
				+ (isDisplayed ? "displayed" : "NOT displayed"));

		// return the result
		return (isDisplayed);
	}
	
	/**
	 * Use this if you don't want to automatically close/confirm the page
	 * navigation dialog if prompted
	 * 
	 * @param menuName
	 *            is the name of the module as shown in the URL
	 * @param subMenuName
	 *            is the name is sub menu as displayed in the user
	 * @param autoCloseConfirmationDialog
	 *            pass true if you want to automatically close the generated
	 *            confirmation dialog
	 * */
	private void crmClickMenu(String menuName, String subMenuName,
			boolean autoCloseConfirmationDialog) {

		if (subMenuName == null) {
			logDebugMessage(concatStrings("Navigating into the list view -> ", menuName));
		} else {
			logDebugMessage(concatStrings("Navigating into the list view -> ", subMenuName, " through ", menuName));
		}

		// find the menu bar
		WebElement menuBar = driver.findElement(
				By.xpath("//div[@class='navbar navbar-fixed-top']"))
				.findElement(By.xpath(".//div[@class='module-list']"));

		WebElement mainMenu = menuBar.findElement(
				By.xpath(".//ul[@data-container='module-list']")).findElement(
				By.xpath(".//li[@data-module='" + menuName + "']"));

		// check if the sub menu is null
		if (subMenuName == null) {
			mainMenu.click();
		} else {

			// click on the arrow button next to menu
			mainMenu.findElement(
					By.xpath(".//button[@class='btn btn-invisible dropdown-toggle'  and  @title='More']"))
					.click();
			logDebugMessage("Clicked the arrow button next to the menu to display the submenu list");

			// verify first that the subMenu list is displayed
			boolean isSubMenuDisp;
			try {
				setImplicitWait(2);
				mainMenu.findElement(By
						.xpath(".//div[@class='dropdown-menu scroll']"));
				isSubMenuDisp = true;
				logDebugMessage("Sub menu list is displayed.");
			} catch (ElementNotVisibleException enve) {
				isSubMenuDisp = false;
				logDebugMessage("Sub menu list is not yet displayed.");
			} finally {
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}

			int counter = 0;
			int maxCounter = 5;
			while (!isSubMenuDisp && counter < maxCounter) {
				logDebugMessage("Will try to click the dropdown button to display the submenu lists");
				// click on the arrow button next to menu
				mainMenu.findElement(
						By.xpath(".//button[@class='btn btn-invisible dropdown-toggle'  and  @title='More']"))
						.click();
				try {
					setImplicitWait(2);
					mainMenu.findElement(By
							.xpath(".//div[@class='dropdown-menu scroll']"));
					isSubMenuDisp = true;
					logDebugMessage("Sub menu list is displayed.");
				} catch (ElementNotVisibleException enve) {
					isSubMenuDisp = false;
					logDebugMessage("Sub menu list is not yet displayed.");
				} finally {
					setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
				}
				counter++;
			}

			// surrounded with try/catch the part where we get
			// the text of the subMenus because we
			// get a StaleElement exception if most of the test cases
			// in the same class is accessing the same list view
			int attempts = 0;
			int maxAttempts = 10;
			WebElement ulist = null;
			List<WebElement> listvalues = null;
			List<String> subMenus = null;
			while (attempts < maxAttempts) {
				try {
					ulist = mainMenu.findElement(
							By.xpath(".//div[@class='dropdown-menu scroll']"))
							.findElement(By.xpath(".//ul[@role='menu']"));
					listvalues = ulist.findElements(By.tagName("li"));

					// we will verify that the subMenuName specified in the args
					// is found in the submenu list
					ListIterator<WebElement> listIterator = listvalues
							.listIterator();
					subMenus = new ArrayList<String>();
					while (listIterator.hasNext()) {
						WebElement value = listIterator.next();
						String stringValue = value.getText();
						String stringValueLwr = stringValue.toLowerCase();
						subMenus.add(stringValueLwr);
					}
				} catch (StaleElementReferenceException sre) {
					logDebugMessage("StaleElementReferenceException was encountered and catched. See exception message for details -> "
							+ sre);
					ulist = mainMenu.findElement(
							By.xpath(".//div[@class='dropdown-menu scroll']"))
							.findElement(By.xpath(".//ul[@role='menu']"));
					listvalues = ulist.findElements(By.tagName("li"));
				}
				logDebugMessage(concatStrings("The current attempts to click the menu in the list view is -> ",
						String.valueOf(attempts)));
				attempts++;
			}
			// let's remove the extra column generated
			// before returning the final subMenus
			subMenus.removeAll(Arrays.asList("", null));
			verifyStringContainsInList(subMenus, true, subMenuName, false);

			for (int i = 0; i < listvalues.size(); i++) {
				// search for the sub menu then click it
				if (listvalues.get(i).getText().contains(subMenuName)) {
					listvalues.get(i).click();
					break;
				}
			}
		}

		if (autoCloseConfirmationDialog) {
			// close the page navigation warning dialog if shown
			// and if requested
			crmClosePageNavigationWarningDialog(5);
		}
	}
	
	/**
	 * Check if there is any active filter in place in the current list view.
	 *
	 */
	private boolean crmIsAnyFilterActive() {

		boolean result;

		try {
			// turn off the implicit wait
			setImplicitWait(0);

			// see if we have the filter header displayed
			result = driver.findElement(By.xpath(".//div[@class='filter-header']")).isDisplayed();
			String activeFilter = driver.findElement(By.xpath(".//div[@class='filter-header']")).getText();
			activeFilter = StringUtils.normalizeSpace(activeFilter);

			if (result) {
				logDebugMessage(concatStrings("An active filter is found. Filter name is -> '", activeFilter, "'"));
			} else {
				logDebugMessage("No active filter is found.");
			}
		} finally {
			// turn back on the implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		return (result);
	}
	
	/**
	 * Use this to know if the list view is expanded
	 * 
	 * @param withinDrawer
	 *            if it's within a drawer or another drawer opens up pass true
	 * 
	 * */
	private boolean crmIsListViewExpanded(boolean withinDrawer) {

		boolean isExpanded = false;
		String angle = null;

		if (withinDrawer) {
			angle = driver
					.findElement(
							By.xpath("//div[@class='drawer transition active']"))
					.findElement(By.xpath(".//div[@class='headerpane']"))
					.findElement(
							By.xpath(".//div[@class='btn-toolbar pull-right dropdown']"))
					.findElement(
							By.xpath(".//button[@class='btn btn-invisible sidebar-toggle' and (@data-original-title='Open/Close Dashboard' or @title='Open/Close Dashboard')]/i"))
					.getAttribute("class");
		} else {
			angle = driver
					.findElement(By.xpath("//div[@class='headerpane']"))
					.findElement(
							By.xpath(".//div[@class='btn-toolbar pull-right dropdown']"))
					.findElement(
							By.xpath(".//button[@class='btn btn-invisible sidebar-toggle' and (@data-original-title='Open/Close Dashboard' or @title='Open/Close Dashboard')]/i"))
					.getAttribute("class");
		}

		assertTrue(angle.contains("icon-double-angle-right") || angle.contains("icon-double-angle-left"),
				"The expected value of attribute 'class' is not correct!");

		if (angle.contains("icon-double-angle-right")) {
			isExpanded = false;
		}
		if (angle.contains("icon-double-angle-left")) {
			isExpanded = true;
		}

		logDebugMessage("The current list view is "
				+ (isExpanded ? "expanded" : "not expanded"));
		return isExpanded;
	}
	
	/**
	 * Sort the specific table on the specified column in ASC
	 *
	 * @param table
	 * @param columnIndex
	 * @return
	 */
	private WebElement crmSortTableAsc(WebElement relativeElement, By tableXPath,
			int columnIndex) {

		// find the table in the required table
		WebElement table = null;
		if (relativeElement == null)
			table = driver.findElement(tableXPath);
		else
			table = relativeElement.findElement(tableXPath);

		// check if the table contains any data
		if (crmIsDataAvailableInTable(table)) {

			// data available, now we can do
			// the sorting, click on the
			// column heading (first row in the header)
			// find the headers
			WebElement headerCell = table.findElement(By.tagName("thead"))
					// find the header rows and get the first header row
					.findElements(By.tagName("tr")).get(0)
					.findElements(By.tagName("th"))
					// get the column that we are interested in
					.get(columnIndex);
			// get the column header name
			String columnName = headerCell.getText();
			logInfoMessage(concatStrings("Sorting the column ", columnName, " in ASC order"));
			// check the sorting status of the column
			String sortingDetails = headerCell.getAttribute("class");
			if (sortingDetails.contains("sorting_asc")) {
				// column current sorted ascending
				// do nothing as it is what we want
				logDebugMessage(concatStrings("The column ", columnName,
						" is already sorted in ASC order. No need to do any sorting."));
			} else if (sortingDetails.contains("sorting_desc")) {
				logDebugMessage(concatStrings("The column ", columnName,
						" is sorted in DESC order. Will need to click it again to sort it in ASC."));
				// column current sorted DESC
				// click ones to sort it
				headerCell.click();
				// wait until the table has been refreshed
				crmLoad();
			} else {
				// unsorted, need to click twice as first click is always
				// descending
				logDebugMessage(concatStrings("The column ", columnName,
						" is not sorted in any way. Will need to click the column first to sort in DESC."));
				headerCell.click();
				// wait until the table has been refreshed
				crmLoad();
				logDebugMessage(concatStrings("Will click ", columnName, " again to sort it in ASC"));
				// call the sorting again to ensure we get things ascending
				crmSortTableAsc(relativeElement, tableXPath, columnIndex);
			}

			// return the table we were working on (but refresh the details
			// first)
			if (relativeElement == null)
				table = driver.findElement(tableXPath);
			else
				table = relativeElement.findElement(tableXPath);
		} else {
			logDebugMessage("No need to sort the table since there is no data");
		}

		return (table);
	}
	
	/**
	 * Sort table in Desc order
	 *
	 * @param relativeElement
	 * @param tableXPath
	 * @param columnIndex
	 * @return
	 */
	private WebElement crmSortTableDesc(WebElement relativeElement, By tableXPath,
			int columnIndex) {

		// find the table in the required table
		WebElement table = null;
		if (relativeElement == null)
			table = driver.findElement(tableXPath);
		else
			table = relativeElement.findElement(tableXPath);

		// check if the table contains any data
		if (crmIsDataAvailableInTable(table)) {

			// data available, now we can do
			// the sorting, click on the
			// column heading (first row in the header)
			// find the headers
			WebElement headerCell = table.findElement(By.tagName("thead"))
					// find the header rows and get the first header row
					.findElements(By.tagName("tr")).get(0)
					.findElements(By.tagName("th"))
					// get the column that we are interested in
					.get(columnIndex);

			// get the column header name
			String columnName = headerCell.getText();
			logInfoMessage(concatStrings("Sorting the column ", columnName, " in DESC order"));
			// check the sorting status of the column
			String sortingDetails = headerCell.getAttribute("class");
			if (sortingDetails.contains("sorting_asc")) {
				logDebugMessage(concatStrings("The column ", columnName,
						" is sorted in ASC. Will need to click the column to sort into DESC."));
				// column current sorted ascending, click ones to sort it
				headerCell.click();
				// wait until the table has been refreshed
				crmLoad();
			} else if (sortingDetails.contains("sorting_desc")) {
				// column current sorted descending .. do nothing as it is what
				// we want
				logDebugMessage(
						concatStrings("The column ", columnName, " is now sorted in DESC. No need to do any sorting."));
			} else {
				logDebugMessage(concatStrings("The column ", columnName,
						" is not sorted in any way. Will click the column to sort it in DESC."));
				// unsorted, need to click once as first click is always
				// descending
				headerCell.click();
				// wait until the table has been refreshed
				crmLoad();
			}

			// return the table we were working on (but refresh the details
			// first)
			if (relativeElement == null)
				table = driver.findElement(tableXPath);
			else
				table = relativeElement.findElement(tableXPath);
		} else {
			logDebugMessage("No need to sort the table since there is no data");
		}

		return (table);
	}
	
	/**
	 * Choose the Field to filter the records. Useful when searching using
	 * multiple filters.
	 * 
	 * @param field
	 *            type in the Field display label
	 * @param filterBoyRowNum
	 *            pass the row number location of the div class filter-body. The
	 *            number starts at 1 being the first filter row
	 * @param withinDrawer
	 *            pass true if within the Search and Select
	 * */
	private void crmCreateFilterName(String field, int filterBoyRowNum,
			boolean withinDrawer) {

		// find the filter field
		WebElement filter = null;
		if (withinDrawer)
			// find the filter within a draw
			filter = driver.findElement(
					By.xpath("//div[@class='drawer transition active']"))
					.findElement(By.xpath(".//div[@class='search-filter']"));
		else
			// find the filter outside of a draw
			filter = driver.findElement(By
					.xpath("//div[@class='search-filter']"));

		// no active filter so start creating one
		filter.findElement(By.linkText("Filter")).click();
		// selecting the create
		List<WebElement> values = driver
				.findElements(By
						.xpath("//div[@class='select2-result-label' and starts-with(@id, 'select2-result-label-')]"));
		values.get(0).click();

		// get the filter area
		WebElement filterArea = filter.findElement(By
				.xpath(".//div[@class='filter-options extend']"));

		WebElement filterBody = filterArea.findElement(By
				.xpath(".//div[@class='filter-definition-container']/div["
						+ filterBoyRowNum + "]"));

		WebElement filedSelect = filterBody
				.findElement(By
						.xpath(".//div[@class='controls span4' and  @data-filter='field']"));
		filedSelect.findElement(By.linkText("Select...")).click();

		// note: can't do relative to drawer becoz this div is outside the
		// drawer.
		WebElement dropactive = driver
				.findElement(By
						.xpath("//div[@id='select2-drop' and  @class='select2-drop select2-display-none select2-with-searchbox select2-drop-active']"));
		dropactive
				.findElement(
						By.xpath(".//input[@class ='select2-input select2-focused' and starts-with(@id, 's2id_autogen')]"))
				.click();
		dropactive
				.findElement(
						By.xpath(".//input[@class ='select2-input select2-focused' and starts-with(@id, 's2id_autogen')]"))
				.sendKeys(field);

		WebElement ulist = dropactive.findElement(By
				.xpath(".//ul[@class='select2-results' and  @role='listbox']"));
		List<WebElement> listvalues = ulist.findElements(By.tagName("li"));
		for (int i = 0; i < listvalues.size(); i++) {
			if (listvalues.get(i).getText().equalsIgnoreCase(field)) {
				listvalues.get(i).click();
				break;
			}
		}

		logDebugMessage(concatStrings("Selected filter -> '", field, "' in the specified filterBoyRowNum ",
				String.valueOf(filterBoyRowNum)));
	}
	
	/**
	 * Choose the Operator to filter the records. Useful when searching using
	 * multiple filters.
	 * 
	 * @param operation
	 *            type in the operator display label
	 * @param filterBoyRowNum
	 *            pass the row number location of the div class filter-body. The
	 *            number starts at 1 being the first filter row
	 * @param withinDrawer
	 *            pass true if within the Search and Select
	 * */
	private void crmCreateFilterOperator(String operation, int filterBoyRowNum,
			boolean withinDrawer) {

		WebElement filterextend = null;
		if (withinDrawer)
			// find the filter within a drawer
			filterextend = driver.findElement(
					By.xpath("//div[@class='drawer transition active']"))
					.findElement(
							By.xpath(".//div[@class='filter-options extend']"));
		else
			// find the filter outside of a drawer
			filterextend = driver.findElement(By
					.xpath("//div[@class='filter-options extend']"));

		WebElement filterBody = filterextend.findElement(By
				.xpath(".//div[@class='filter-definition-container']/div["
						+ filterBoyRowNum + "]"));

		WebElement operator = filterBody
				.findElement(By
						.xpath(".//div[@class='controls span4' and  @data-filter='operator']"));
		operator.findElement(By.linkText("Select...")).click();
		// note: can't do relative to drawer becoz this div is outside the
		// drawer
		WebElement slectdrop = driver
				.findElement(By
						.xpath("//div[@id='select2-drop' and @class='select2-drop select2-display-none select2-drop-active']"));
		WebElement ulist = slectdrop.findElement(By
				.xpath(".//ul[@class='select2-results' and  @role='listbox']"));
		List<WebElement> listvalues = ulist.findElements(By.tagName("li"));
		for (int i = 0; i < listvalues.size(); i++) {
			if (listvalues.get(i).getText().contains(operation)) {
				listvalues.get(i).click();
				break;
			}
		}

		logDebugMessage("Selected filter operator -> '" + operation
				+ "' in the specified filterBoyRowNum " + filterBoyRowNum);
		crmLoad();
	}
	
	/**
	 * Get the all the rows in the table body, hence excluding any header rows.
	 *
	 * @param table
	 * @return
	 */
	private List<WebElement> crmGetTableRows(WebElement table) {

		// get the body of the table
		WebElement tableBody = table.findElement(By.tagName("tbody"));
		// get all the rows in the table and then return the one required
		return (tableBody.findElements(By.tagName("tr")));
	}
	
	/**
	 * Check if a sub-panel with a specific name is open or closed.
	 *
	 * @param name
	 * @return
	 * @throws IllegalArgumentException
	 *             if no sub-panel with the specified name has been found
	 */
	private boolean crmIsSubPanelOpen(String subpanel) throws IllegalArgumentException {

		// find all the sub-panel on the current page
		final WebElement subPanel = crmFindSubPanel(subpanel);
		if (subPanel == null)
			throw (new IllegalArgumentException("Sub-panel '" + subpanel
					+ "' not found"));
		// sub-panel found, check if it's open
		try {
			setImplicitWait(0);
			// this element identifier is when a subpanel is closed
			subPanel.findElement(By.xpath(".//li[contains(@class,'closed')]"));
			logDebugMessage("Subpanel '" + subpanel + "' is closed");
			return false;
		} catch (NoSuchElementException nse) {
			logDebugMessage("Subpanel '" + subpanel + "' is open");
			return true;
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/**
	 * Get the entire specified row in the specified table.
	 *
	 * @param table
	 * @param rowNum
	 * @return
	 */
	private WebElement crmGetTableRow(WebElement table, int rowNum) {

		// get all the rows in the table and then return the one required
		return (crmGetTableRows(table).get(rowNum));
	}
	
	/**
	 * Get all the cells in the specified row in the specified table.
	 *
	 * @param table
	 * @param rowNum
	 * @return
	 */
	private List<WebElement> crmGetTableRowCells(WebElement table, int rowNum) {

		return (crmGetTableRow(table, rowNum).findElements(By.tagName("td")));
	}
	
	/**
	 * Check if the specified table contains any data.
	 * 
	 * @param table
	 */
	protected boolean crmIsDataAvailableInTable(WebElement table) {

		boolean dataAvailable = true;
		try {
			// turn off the implicit wait
			setImplicitWait(0);
			logInfoMessage("Checking if there is any data in table");

			// find the grand-parent in which the table exists
			WebElement gParent = table.findElement(By.xpath("../.."));
			// check if no data is available
			if (gParent.getText().contains("No data available."))
				dataAvailable = false;

			LOG.info(MessageFormat.format(
					"found {0,choice,0#NO data|1#data} in table", dataAvailable ? 1
							: 0));
		} finally {
			// turn back on the implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		return (dataAvailable);
	}
	
	/**
	 * Verify if there's a saved filter chosen in the list view. Useful in the
	 * Historical Billing list view and verify that the default filter 'Active
	 * Bill' is set automatically
	 * 
	 * */
	protected boolean crmIsAnySavedFilterChosen(boolean withinDrawer) {

		String chosenFilter;

		if (withinDrawer) {
			WebElement drawer = driver.findElement(By
					.xpath("//div[@class='drawer transition active']"));
			chosenFilter = drawer
					.findElement(
							By.xpath("//div[starts-with(@class,'choice-filter')]/span[@class='choice-filter-label']"))
					.getText();
		} else {
			chosenFilter = driver
					.findElement(
							By.xpath("//div[starts-with(@class,'choice-filter')]/span[@class='choice-filter-label']"))
					.getText();
		}

		String trimmedFilter = chosenFilter.replaceAll("\\s+", "");
		if (trimmedFilter.equals("Create")) {
			logDebugMessage("There is no saved filter currently set in the list view");
			return false;
		} else {
			logDebugMessage("This is the currently saved filter chosen in the list view -> '"
					+ chosenFilter + "'");
			return true;
		}
	}
	
	/** 
	 * Use this to verify if you are in the login page
	 * 
	 * */
	protected boolean crmAreWeInLoginPage() {
		
		logDebugMessage("Checking if we are in the CRM login page");
		boolean areWeInLogingPage;
		try {
			setImplicitWait(1);
			driver.findElement(By.name("login_button"));
			areWeInLogingPage = true;
			logDebugMessage("We are the CRM login page");
		} catch (NoSuchElementException nsee) {
			areWeInLogingPage = false;
			logDebugMessage("We are not in the CRM login page");
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
		
		return areWeInLogingPage;
	}
	
	/**
	 * Login into the BlueBilling CRM.
	 * 
	 * @param waitForDoneMsg
	 *            pass true if you are expecting the Done message to appear
	 *            after logging in
	 */
	protected void crmLogin(boolean waitForDoneMsg) {

		logDebugMessage("Will be logging into the CRM");
		// open the application URL and maximize the screen
		driver.get(getCRMUrl());
		logDebugMessage("Accessed the CRM URL -> '" + super.getCRMUrl() + "'");
		if (getAutomationSource().toLowerCase().equals("jenkins")) {
			Dimension dimension = new Dimension(2560, 1440);
			logDebugMessage("We are running automation through Jenkins, will set the window Dimension size as '".concat(dimension.toString().concat("'")));
			driver.manage().window().setSize(dimension);
			int newWidth = driver.manage().window().getSize().getWidth();
			int newHeight = driver.manage().window().getSize().getHeight();
			String newWidthStr = Integer.toString(newWidth);
			String newHeightStr = Integer.toString(newHeight);
			String logMsg = concatStrings("The new window width is [", newWidthStr, "] while the new window height is [", newHeightStr, "]");
			logDebugMessage(logMsg);
		} else {
			driver.manage().window().maximize();
			logDebugMessage("Maximized the CRM window");
		}

		// LOG into the application
		waitForElement(By.name("login_button"), CRM_LOGIN_TIMEOUT, 30);
		driver.findElement(By.name("username")).sendKeys(super.getCRMUsername());
		driver.findElement(By.name("password")).sendKeys(super.getCRMPassword());
		driver.findElement(By.name("login_button")).click();
		logDebugMessage("Clicked the Login button");

		if (waitForDoneMsg) {
			logDebugMessage("Waiting for the Done button to appear");
			// wait for the into screen to appear and click on the DONE button
			waitForElement(By.xpath("//a[@href='#'and @title='Done']"), CRM_LOGIN_TIMEOUT, 30);
			driver.findElement(By.xpath("//a[@href='#'and @title='Done']"))
					.click();
		} else {
			logDebugMessage("No need to wait for the Done button to appear");
			crmLoad();
			if (crmIsDoneBtnDisplayed(3)) {
				driver.findElement(By.xpath("//a[@href='#'and @title='Done']"))
						.click();
			}
		}

		// see if there is warning displayed regarding the time zone
		if (crmIsWarningMessageDisplayed(3)) {
			crmCloseWarningAlert();
		}
		logDebugMessage("Successfully logged in to the CRM");
	}
	
	/**
	 * Use this to navigate into the crm home page
	 *  */
	protected void crmNavigateHomepage() {
		
		driver.get(getCRMUrl());
		logDebugMessage("Accessed the CRM URL -> '" + super.getCRMUrl() + "'");
		driver.manage().window().maximize();
		logDebugMessage("Maximized the CRM window");
		
		// see if there is warning displayed regarding the time zone
		if (crmIsWarningMessageDisplayed(3)) {
			crmCloseWarningAlert();
		}
		logDebugMessage("Successfully navigated in the CRM Home Page");
	}
	
	/**
	 * Use this to verify if the Done button is displayed after users logged in
	 * the CRM
	 * */
	protected boolean crmIsDoneBtnDisplayed(long implicitWait) {

		boolean isDisplayed;
		logDebugMessage("Checking if the Done button is displayed");

		try {
			setImplicitWait(implicitWait);
			driver.findElement(By.xpath("//a[@href='#'and @title='Done']"));
			isDisplayed = true;
			logDebugMessage("Done button is displayed in the Home page");
		} catch (NoSuchElementException nse) {
			isDisplayed = false;
			logDebugMessage("Done button is NOT displayed in the Home page");
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		if (isDisplayed) {
			logDebugMessage("The Done button is displayed");
		} else {
			logDebugMessage("The Done button is not displayed");
		}

		return isDisplayed;
	}
	
	/**
	 * Check if the "Loading" at the top of the screen appears. If it does
	 * appear, wait until it disappears. This is used to wait until the page has
	 * completely loaded.
	 */
	protected void crmLoad() {

		// loop here until we know that there is no more page loads
		// (as there could be multiple)
		int loopCount = 1;
		int maxAttempts = 6;
		while (loopCount < maxAttempts) {

			logDebugMessage("Will now check if Loading message is displayed. Current attempts -> "
						+ loopCount);

			// check if the 'Page Loading' status to appear
			if (crmIsLoadAppeared(CRM_PAGE_LOAD_TIMEOUT_START, true)) {
				// loading message appeared, now let's wait
				// until it disappears
				crmIsLoadAppeared(CRM_PAGE_LOAD_TIMEOUT_END, false);
				logDebugMessage("Loading message was seen and now disappeared. Will now check if another one shows up");
			} else {
				// there is no more page loads to wait for, exit loop
				break;
			}

			// indicate that we will try to search again
			loopCount++;
		}

		// check if loading is still present
		// throw a time out exception if it still is
		if (loopCount == maxAttempts) {
			throw (new TimeoutException(
					"Loading message is taking too long to disappear"));
		}

		// just put a pause to avoid potential stale element
		pauseSeleniumExecution(1000);

		if (crmIsErrorMessageDisplayed(0)) {
			String errorMessage = driver.findElement(
					By.xpath("//div[@class='alert alert-danger alert-block']"))
					.getText();
			// report this error as an exception
			throw (new ErrorMessageException(
					"Error message is displayed while waiting for loading message to appear or disappear, message is -> '"
							+ errorMessage + "'"));
		}

		// README
		// Why do we need to get the success message?
		// commented this out for now until we are sure
		// that this is not needed - Nino (Nov 16, 2021)
		// check if the success message is displayed
//		if (crmIsSuccessMessageDisplayed(0)) {
//			String successMessage = driver
//					.findElement(
//							By.xpath("//div[@class='alert alert-success alert-block']"))
//					.getText();
//			logDebugMessage("Success message is displayed while waiting for loading message to appear or disappear, message is -> '"
//					+ successMessage + "'");
//		}

		// README
		// Why do we need to get the success message?
		// commented this out for now until we are sure
		// that this is not needed - Nino (Nov 16, 2021)
		// check if the success message is displayed
//		if (crmIsWarningMessageDisplayed(0)) {
//			String warningMessage = driver
//					.findElement(
//							By.xpath("//div[@class='alert alert-warning alert-block']"))
//					.getText();
//			logDebugMessage("Warning message is displayed while waiting for loading message to appear or disappear, message is -> '"
//					+ warningMessage + "'");
//		}
	}
	
	/**
	 * returns true if error message is shown
	 * 
	 * @param implicitWait
	 *            put the max seconds to wait for the error message to display
	 * */
	protected boolean crmIsErrorMessageDisplayed(long implicitWait) {

		try {
			setImplicitWait(implicitWait);
			WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
			wrapper.findElement(By.xpath(".//div[@class='alert alert-danger alert-block']"));
			logDebugMessage("Error alert is displayed");
			return true;
		} catch (NoSuchElementException nsee) {
			logDebugMessage("No error alert is displayed");
			return false;
		} catch (StaleElementReferenceException sere) {
			try {
				setImplicitWait(implicitWait);
				WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
				wrapper.findElement(By.xpath(".//div[@class='alert alert-danger alert-block']"));
				logDebugMessage("Error alert is displayed");
				return true;
			} catch (NoSuchElementException nsee) {
				logDebugMessage("No error alert is displayed");
				return false;
			} finally {
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * returns true if warning message is shown
	 * 
	 * @param implicitWait
	 *            put the max seconds to wait for the warning message to display
	 * */
	protected boolean crmIsWarningMessageDisplayed(long implicitWait) {

		try {
			setImplicitWait(implicitWait);
			WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
			wrapper.findElement(By.xpath(".//div[@class='alert alert-warning alert-block']"));
			logDebugMessage("Warning alert is displayed");
			return true;
		} catch (NoSuchElementException nsse) {
			logDebugMessage("No warning alert is displayed");
			return false;
		} catch (StaleElementReferenceException sere) {
			try {
				setImplicitWait(implicitWait);
				WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
				wrapper.findElement(By.xpath(".//div[@class='alert alert-warning alert-block']"));
				logDebugMessage("Warning alert is displayed");
				return true;
			} catch (NoSuchElementException nsse) {
				logDebugMessage("No warning alert is displayed");
				return false;
			} finally {
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}

	/**
	 * returns true if success message is shown
	 * 
	 * @param implicitWait
	 *            put the max seconds to wait for the success message to display
	 * */
	protected boolean crmIsSuccessMessageDisplayed(long implicitWait) {

		try {
			setImplicitWait(implicitWait);
			WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
			wrapper.findElement(By.xpath(".//div[@class='alert alert-success alert-block']"));
			logDebugMessage("Success alert is displayed");
			return true;
		} catch (NoSuchElementException nsee) {
			logDebugMessage("No success alert is displayed");
			return false;
		} catch (StaleElementReferenceException sere) {
			try {
				setImplicitWait(implicitWait);
				WebElement wrapper = driver.findElement(By.xpath("//div[@class='alert-wrapper']"));
				wrapper.findElement(By.xpath(".//div[@class='alert alert-success alert-block']"));
				logDebugMessage("Success alert is displayed");
				return true;
			} catch (NoSuchElementException nsee) {
				logDebugMessage("No success alert is displayed");
				return false;
			} finally {
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/**
	 * close the warning alert
	 */
	protected void crmCloseWarningAlert() {

		WebDriverWait wait6 = new WebDriverWait(driver, 40);
		wait6.until(
				ExpectedConditions.visibilityOfElementLocated(By
						.xpath("//div[@class='alert alert-warning alert-block']")))
				.getText().contains("Warning");
		driver.findElement(
				By.xpath("//div[@class='alert alert-warning alert-block']"))
				.findElement(
						By.xpath(".//button[@class='close' and  @data-action='close']"))
				.click();
		logDebugMessage("Warning alert closed");
	}
	
	/** 
	 * 
	 * */
	protected void crmCloseErrorAlert() {

		WebDriverWait wait6 = new WebDriverWait(driver, 40);
		wait6.until(
				ExpectedConditions.visibilityOfElementLocated(By
						.xpath("//div[@class='alert alert-danger alert-block']")))
				.getText().contains("Error");
		driver.findElement(
				By.xpath("//div[@class='alert alert-danger alert-block']"))
				.findElement(
						By.xpath(".//button[@class='close' and  @data-action='close']"))
				.click();
		logDebugMessage("Error alert closed");
	}
	
	/**
	 * Check if there is a page navigation warning dialog showing and if there
	 * is close it so we can move away from the current page. The Page
	 * navigation warning dialog is the one that is shown warning user about
	 * potentially unsaved data may be lost if they navigate away from the
	 * current page.
	 * 
	 * @param maxWaitTime
	 *            is the max seconds for the implicit wait
	 */
	protected void crmClosePageNavigationWarningDialog(long maxWaitTime) {

		// see if navigation warning is displayed
		if (crmIsPageNavigationWarningDialogShown(maxWaitTime)) {
			WebElement alertConfirm = driver
					.findElement(
							By.xpath("//div[@class='alert alert-warning alert-block']"))
					.findElement(By.xpath(".//div[@class='row-fluid']"));
			// click on the confirm button
			if (alertConfirm.findElement(
					By.xpath(".//a[@data-action='confirm']")).isDisplayed())
				alertConfirm.findElement(
						By.xpath(".//a[@data-action='confirm']")).click();
			logDebugMessage("Page warning dialog has been closed by clicking on the confirm button");
		} else {
			logDebugMessage("Page warning dialog is not displayed, no action needed");
		}
	}
	
	/**
	 * Check if the "loading" table appeared. If it does, it will wait until it
	 * has disappeared. This function can be used to check if the content of a
	 * list view (including Search and Select) table data is fully loaded.
	 * 
	 * @param withinDrawer
	 *            pass true if it's within an active drawer or Search and Select
	 *            list view
	 */
	protected void crmTableLoad(boolean withinDrawer) {

		FluentWait<WebDriver> wait_1 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(CRM_TABLE_LOAD_TIMEOUT_START))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class,
						StaleElementReferenceException.class);

		boolean loadPresent = false;
		try {
			// turn off the implicit wait
			setImplicitWait(0);
			// check if the "loading" element is present in the table
			loadPresent = wait_1.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Checking for the Table Loading dialog to appear");

					boolean appeared = false;

					// get the footer element
					WebElement footerElement = null;
					if (withinDrawer) {
						logDebugMessage("Looking for the footerElement from the Search and Select list view");
						// find the active drawer
						footerElement = driver
								.findElement(
										By.xpath("//div[@class='drawer transition active']"))
								.findElement(
										By.xpath(".//div[@class='pagination']"))
								.findElement(
										By.xpath(".//div[@class='main-content']"))
								.findElement(
										By.xpath(".//div[@class='block-footer']"));
					} else {
						logDebugMessage("Looking for the footerElement from the list view");
						footerElement = driver
								.findElement(
										By.xpath("//div[@class='main-content']"))
								.findElement(
										By.xpath("./div[@class='pagination']"))
								.findElement(
										By.xpath(".//div[@class='block-footer']"));
					}

					logDebugMessage("The value of footerElement1 -> '"
							+ footerElement.getText() + "'");
					WebElement table = null;
					if (withinDrawer) {
						table = driver.findElement(By
								.xpath("//table[@class='table table-striped dataTable search-and-select']"));
					} else {
						table = driver.findElement(By
								.xpath("//table[@class='table table-striped dataTable reorderable-columns']"));
					}
					// see what text is in the footer element
					// it contains the loading, see the next step is to
					// wait until it disappears
					if (footerElement.getText().contains("Loading")) {
						appeared = true;
						logDebugMessage("Table Loading message appeared");
					} else if (crmIsDataAvailableInTable(table)) {
						// it contains 'the no data element', so there is
						// no point looking for the table load
						// indicate that it has already appears so
						// below it will just check once if disappeared
						appeared = true;
						logDebugMessage("Table Loading has been missed since it already shows 'No data available'");
					} else {
						// at this stage we have not loading or no data
						// message in the footer so check if there
						// is any data in the table
						if (table.findElement(By.tagName("tr")).isDisplayed()) {
							appeared = true;
							logDebugMessage("Table Loading has been missed since there is already data showing");
						}
					}
					return (appeared);
				}
			}).booleanValue();
		} catch (TimeoutException te) {
			logDebugMessage("Timeout of <"
					+ CRM_TABLE_LOAD_TIMEOUT_START
					+ "> seconds has been reached while waiting for the Table Loading to appear.");
		} catch (Exception exception) {
			logDebugMessage("The Table Loading dialog did not appear and a general exception was encountered. See message for more details -> "
					+ exception.getMessage());
		} finally {
			// turn back on the implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		if (loadPresent) {
			boolean loadDisappeared;
			// "loading" element was seen before, so now wait until it
			// disappears
			wait_1.withTimeout(Duration.ofSeconds(CRM_TABLE_LOAD_TIMEOUT_END));
			try {
				loadDisappeared = wait_1.until(
						new Function<WebDriver, Boolean>() {
							public Boolean apply(WebDriver driver) {
								logDebugMessage("Checking for the Table Loading dialog to disappear");

								boolean disappeared = false;
								try {
									// turn off the implicit wait
									setImplicitWait(0);
									// get the footer element
									WebElement footerElement = null;
									if (withinDrawer) {
										logDebugMessage("Looking for the footerElement from the Search and Select list view");
										// find the active drawer
										footerElement = driver
												.findElement(
														By.xpath("//div[@class='drawer transition active']"))
												.findElement(
														By.xpath(".//div[@class='pagination']"))
												.findElement(
														By.xpath(".//div[@class='main-content']"))
												.findElement(
														By.xpath(".//div[@class='block-footer']"));
									} else {
										logDebugMessage("Looking for the footerElement from the list view");
										footerElement = driver
												.findElement(
														By.xpath("//div[@class='main-content']"))
												.findElement(
														By.xpath("./div[@class='pagination']"))
												.findElement(
														By.xpath(".//div[@class='block-footer']"));
									}
									logDebugMessage("The value of footerElement2 -> '"
											+ footerElement.getText() + "'");
									// check if the loading has disappeared
									if (!footerElement.getText().contains(
											"Loading")) {
										disappeared = true;
										logDebugMessage("The Table Loading element does not contain 'Loading', will assume that Table Loading dialog now disappeared");
									}
								} catch (NoSuchElementException nse) {
									disappeared = true;
									logDebugMessage("Table Loading dialog disappeared since footerElement is no longer displayed");
								} catch (Exception exception) {
									disappeared = true;
									logDebugMessage("A general exception was encountered while waiting for Table Loading to disappear. See message for more details -> "
											+ exception);
								} finally {
									// turn back on the implicit wait
									setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
								}

								return (disappeared);
							}
						}).booleanValue();
			} catch (TimeoutException te) {
				logDebugMessage("Timeout of <"
						+ CRM_TABLE_LOAD_TIMEOUT_END
						+ "> seconds has been reached while waiting for the Table Loading to disappear");
				loadDisappeared = false;
			} catch (Exception exp) {
				logDebugMessage("A general exception was encountered while waiting for Table Loading to disappear. Will assume table loading message disappeared. See message for more details -> "
						+ exp);
				loadDisappeared = true;
			}
			if (!loadDisappeared) {
				logDebugMessage("The loadDisappeared variable is ["
						+ loadDisappeared
						+ "]. A TimeoutException exception will be thrown.");
				throw (new TimeoutException(
						"Table Loading message is taking too long to disappear"));
			}
		}
	}
	
	/**
	 * Check if there is any active filter in the current list view and if there
	 * is close it.
	 */
	protected void crmCloseAnyActiveFilters() {

		// see if we have the filter area open
		if (crmIsAnyFilterActive()) {
			// filter area is open, get the filter area
			// element to work with
			WebElement filter = driver.findElement(
					By.xpath("//div[@class='filter-header']")).findElement(
					By.xpath(".//div[@class='pull-right']"));
			// click on the cancel button
			if (filter.findElement(
					By.xpath(".//a[@track='click:filter-close']"))
					.isDisplayed())
				filter.findElement(
						By.xpath(".//a[@track='click:filter-close']")).click();
			logDebugMessage("Active filter has been closed in the list view");
			// wait for the page to load after the clearing of the filter
			crmLoad();
		}
	}
	
	/**
	 * If there's a saved filter currently chosen in the list view close it
	 * */
	protected void crmCloseAnyChosenFilter(boolean withinDrawer) {

		if (crmIsAnySavedFilterChosen(false)) {
			driver.findElement(
					By.xpath("//div[starts-with(@class,'choice-filter')]/span[@class='choice-filter-close']"))
					.click();
			logDebugMessage("Removed the current saved filter set in the list view");
			// wait for the page to load after the closing the chosen filter
			crmLoad();
		}
	}
	
	/**
	 * Use this if you don't want to automatically close/confirm the page
	 * navigation dialog if prompted when opening list view
	 * */
	protected void crmOpenListView(String menuName, String subMenuName,
			boolean autoCloseConfirmationDialog) {

		int attempts = 0;
		int maxAttempts = 4;
		while (attempts < maxAttempts) {
			try {
				// let's close the error message if it's displayed
				if (crmIsErrorMessageDisplayed(1)) {
					logDebugMessage("An error message is displayed from the openListView(String, String, boolean) method. Getting error message details.");
					String errorMsg = crmGetErrorMessage(false);
					logDebugMessage("Error Message is -> '" + errorMsg + "'");
					crmCloseErrorAlert();
				}
			} catch (StaleElementReferenceException sere) {
				logDebugMessage("A StaleElementReferenceException was encountered and catched in the openListView(String, String, boolean) method. See exception details -> "
						+ sere.getMessage());
				// put a pause to fix issue
				pauseSeleniumExecution(1000);
			}
			attempts++;
		}

		// click on the submenu item within the menu
		crmClickMenu(menuName, subMenuName, autoCloseConfirmationDialog);

		// wait for the content of the table to load
		crmTableLoad(false);

		// close any active filter in the current list view
		crmCloseAnyActiveFilters();

		// close if there are any chosen saved filter
		crmCloseAnyChosenFilter(false);
	}
	
	/**
	 * Use this if you want to directly click a record without the need to sort
	 * the records.
	 * 
	 * Please note that you should supply the exact link text displayed.
	 * */
	protected void crmClickRecordExactLinkText(String exactLinkText) {

		driver.findElement(By.linkText(exactLinkText)).click();
		logDebugMessage("Clicked the record that has the exact link text '"
				+ exactLinkText + "'");
	}
	
	/**
	 * Use this when you need to click a record from list view or sub panel
	 * 
	 * @param rowNum
	 *            if the first record from the list. It starts with 0
	 * @param columNum
	 *            starts at 0 being the first column list or subpanel. There are
	 *            subpanels that do not have bulk actions (e.g. Communications
	 *            subpanel)
	 * @param table
	 *            is the list view or subpanel table
	 * @param linkName
	 *            is the whole link text to click
	 * 
	 *            Please note that the column for the bulk actions is actually
	 *            the first column.
	 * */
	protected void crmClickRecord(WebElement table, int rowNum, int columNum,
			String linkName) {

		List<WebElement> rowCells = crmGetTableRowCells(table, rowNum);
		rowCells.get(columNum).findElement(By.linkText(linkName)).click();
		logDebugMessage("Clicked the record '" + linkName
				+ "' located in row number " + rowNum
				+ " and in column number " + columNum);
	}
	
	/**
	 * Use this to click the more link to show more records in the subpanel
	 * */
	protected void crmClickMoreRecordsSubpanel(String subpanel) {

		if (!crmIsSubPanelOpen(subpanel)) {
			crmToggleSubpanel(subpanel);
		}
		WebElement panel = crmFindSubPanel(subpanel);
		panel.findElement(By.xpath(".//ul/li/div[3]/button")).click();
		logDebugMessage("Clicked the show more records in the subpanel '" + subpanel + "'");
		crmLoad();
	}
	
	/**
	 * Switch to a specific windows. This also has the option for waiting to
	 * page to load and scroll a set number of row.
	 *
	 * @param pos
	 *            window position
	 * @param waitLoad
	 *            should we wait for the page to load
	 * @param scrollRows
	 *            how many rows to scroll
	 * @param Keys
	 *            in which direction to scroll the page
	 */
	protected void crmSwitchToWindow(int pos, boolean waitLoad, int scrollRows,
			Keys Keys) {

		logDebugMessage("Will pause selenium for 500 milli seconds to get the windows to appear");
		// wait a little while to get the windows to appear
		pauseSeleniumExecution(500);

		// get all the current windows
		Set<String> activeWindowList = driver.getWindowHandles();
		// switch to the specific window
		logDebugMessage("Total windows open " + activeWindowList.size());
		driver.switchTo().window((String) activeWindowList.toArray()[pos]);
		int windowPos = pos + 1;
		logDebugMessage("Switching to window " + windowPos);

		// wait for the page to load if required
		if (waitLoad) {
			logDebugMessage("Need to wait for the page to load");
			// put a pause to fix issue where selenium gets stuck
			// for 3 hours before resuming testing
			pauseSeleniumExecution(8000);
			crmLoad();
			logDebugMessage("Page loading has finished");
		} else {
			logDebugMessage("No need to wait for the Loading to finish");
		}

		// scroll the page as required
		crmScrollPage(scrollRows, Keys);
	}
	
	protected void crmSwitchToWindow(int pos) {

		logDebugMessage("Will pause selenium for 500 milli seconds to get the windows to appear");
		// wait a little while to get the windows to appear
		pauseSeleniumExecution(500);

		// get all the current windows
		Set<String> activeWindowList = driver.getWindowHandles();
		logDebugMessage("Total windows open " + activeWindowList.size());
		// switch to the specific window
		int windowPos = pos + 1;
		logDebugMessage("Switching to window " + windowPos);
		driver.switchTo().window((String) activeWindowList.toArray()[pos]);
		logDebugMessage("Successfully switched to window " + windowPos);
		logDebugMessage("Need to wait for the page to load");
		// put a pause to fix issue where selenium gets stuck
		// for 3 hours before resuming testing
		pauseSeleniumExecution(8000);
		crmLoad();
		logDebugMessage("Page loading has finished");
	}
	
	/** 
	 * Use this to switch to the bwc iframe for legacy modules in the crm
	 * 
	 * */
	protected void crmSwitchToBwcIframe() {
		
		try {
			logDebugMessage("Will be switching now to bwc-frame");
			setImplicitWait(1);
			driver.switchTo().frame("bwc-frame");
			logDebugMessage("Successfully switched to the bwc-frame");
		} catch (Exception e) {
			logDebugMessage("An exception has been encountered. Please see message for more details -> " + e.getMessage());
			throw e;
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
	}
	
	/**
	 * Capture the string ID handle of a browser
	 */
	protected String crmGetWindowHandle() {

		String handle = driver.getWindowHandle();
		return handle;
	}
	
	/**
	 * Use this to get the number of records in the subpanel
	 * */
	protected int crmGetNumOfRecordsInSubpanel(String subpanelName, boolean withLoad) {

		WebElement subpanel = crmOpenSubPanel(subpanelName, withLoad);
		// capture all the data in the subpanel view
		List<WebElement> rows = getTableRows(subpanel);
		int numOfRecords = rows.size();
		logDebugMessage("The number of records in the subpanel " + subpanelName
				+ " is " + numOfRecords);
		return numOfRecords;
	}
	
	/**
	 * The param displayLabel is used as a key to get the value in the HashMap
	 * for that corresponding key The labels variable will get the labels in the
	 * preview while the values variable will get the corresponding value of
	 * that label We pass them in the map to create a key->value pair
	 **/
	protected String crmGetPreviewDataByLabel(WebElement preview, String previewLabel) {

		List<WebElement> labels = preview
				.findElements(By.xpath("./div/div[1]"));
		List<WebElement> values = preview
				.findElements(By.xpath("./div/div[2]"));
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < values.size(); i++) {
			String keyRaw = labels.get(i).getText();
			String valueRaw = values.get(i).getText();
			String key = StringUtils.normalizeSpace(keyRaw);
			String value = StringUtils.normalizeSpace(valueRaw);
			map.put(key, value);
		}
		String value = map.get(previewLabel);
		logDebugMessage("The captured value in the provided previewLabel -> "
				+ previewLabel + " is -> '" + value + "'");
		return value;
	}
	
	/**
	 * The Param rowNum start at 1 being the first row-fluid class
	 * 
	 * @param preview
	 * @param rowFluidNum
	 *            starts at 1 being the first display value
	 * */
	protected String crmGetPreviewDataByRowFluidNum(WebElement preview, int rowFluidNum) {

		WebElement value = preview.findElement(By.xpath("./div[" + rowFluidNum
				+ "]/div[2]"));
		String rawPreviewValue = value.getText();
		logDebugMessage("The value of rawPreviewValue -> '" + rawPreviewValue
				+ "'");
		return rawPreviewValue;
	}
	
	/** 
	 * Get this to get the element of the row-fluid class
	 * */
	protected WebElement crmGetPreviewWebElementByRowFluidNum(WebElement preview, int rowFluidNum) {
		
		WebElement elementValue = preview.findElement(By.xpath("./div[" + rowFluidNum + "]"));
		return elementValue;
	}
	
	/** 
	 * 
	 * */
	protected String crmGetErrorMessage(boolean withinDrawer) {

		String errorMessage = null;

		if (withinDrawer) {
			errorMessage = driver
					.findElement(
							By.xpath("//div[@class='drawer transition active']"))
					.findElement(
							By.xpath(".//div[@class='alert alert-danger alert-block']"))
					.getText();
		} else {
			errorMessage = driver.findElement(
					By.xpath("//div[@class='alert alert-danger alert-block']"))
					.getText();
		}

		logDebugMessage("The value of errorMessage -> '" + errorMessage + "'");
		return errorMessage;
	}
	
	/**
	 * This would verify if the expected value contains in the list view or
	 * subpanel For param rowRecord, the number starts at 0 being the first on
	 * the list or subpanel
	 * 
	 * For param columnPos, the number starts at 0 being the first column list
	 * or subpanel Please note that the column for the bulk actions is actually
	 * the first column. There are subpanels that do not have bulk actions (e.g.
	 * Communications subpanel)
	 * 
	 * @param table
	 * @param rowRecord
	 * @param columnPos
	 * @return the captured string
	 * 
	 * */
	protected String crmGetListOrSubpanelValue(WebElement table, int rowRecord,
			int columnPos) {

		List<WebElement> rowCells = crmGetTableRowCells(table, rowRecord);
		String capturedValueRaw = rowCells.get(columnPos).getText();
		String capturedValue = StringUtils.normalizeSpace(capturedValueRaw);
		logDebugMessage("The capturedValue from row " + rowRecord
				+ " and columnPos " + columnPos + " is -> '" + capturedValue
				+ "'");
		return capturedValue;
	}
	
	/**
	 * Get the table with search from a list view.
	 *
	 * @param searchStr
	 *            search string, null will not do any searching
	 * @param hitEnterAfterSearchStr TODO
	 * @param sortColumn
	 *            column number (zero based index) to sort the result on please
	 *            note that the tick boxes in the list view is considered number
	 *            0
	 * @param sortOrderAsc
	 *            sort the specified column in ascending order, false means sort
	 *            in descending order
	 * @param expandTable
	 *            should the table view area be expanded, hiding the preview
	 *            area
	 * @param columnNames
	 *            list of columns that are to be active, all others will be
	 *            turned off
	 * @return
	 */
	protected WebElement crmGetListViewTableWithSearch(String searchStr,
			boolean hitEnterAfterSearchStr, int sortColumn, boolean sortOrderAsc,
			boolean expandTable, String... columnNames) {

		// see if the table needs to be expanded
		if (expandTable) {
			logDebugMessage("The list view is requested to be expanded");
			// let's check first if the list view is already expanded
			if (crmIsListViewExpanded(false)) {
				logDebugMessage("The list view is expanded already - no need to do anything.");
			} else {
				// expand the table view area
				crmToggleOpenCloseDashboard(false);
				logDebugMessage("The list view is now expanded");
			}
		} else {
			logDebugMessage("The list view is requested not to be expanded");
			// let's check first if the list view is expanded
			if (crmIsListViewExpanded(false)) {
				// hit the toggle to collapse the list view
				crmToggleOpenCloseDashboard(false);
				logDebugMessage("The list view is no longer expanded");
			} else {
				logDebugMessage("The list view is not expanded already - no need to do anything.");
			}
		}

		// check if we need to do any searching
		if (searchStr != null) {
			logDebugMessage("Doing a quick search in the list view using the text -> "
					+ searchStr);
			if (hitEnterAfterSearchStr) {
				// searching is required and we need to hit Enter afterwards
				driver.findElement(
						By.xpath("//input[@class='search-name' and @type='text']"))
						.sendKeys(searchStr, Keys.ENTER);
			} else {
				// searching is required
				driver.findElement(
						By.xpath("//input[@class='search-name' and @type='text']"))
						.sendKeys(searchStr);
			}
			// wait for the page to refresh
			crmLoad();
		} else {
			logDebugMessage("Does not need to do any searching in the list view");
		}

		// create the XPATH for the table
		final By tableXPath = By
				.xpath(".//table[@class='table table-striped dataTable reorderable-columns']");

		// show the required columns if specified
		if (columnNames != null && columnNames.length > 0) {
			// we need to control which
			// columns to display,
			// convert the array into a
			// list to work with
			final List<String> listOfColumns = Arrays.asList(columnNames);
			logDebugMessage("Will need to display the following columns in the list view -> " + listOfColumns);
			// find the table, obtain the header row and get the list of
			// available columns
			WebElement table = driver.findElement(tableXPath);
			WebElement headerRow = crmGetTableheaderRow(table, 0);
			headerRow
					.findElement(
							By.xpath(".//button[@data-original-title='Columns' or @title='Columns']"))
					.click();
			// search for all the column buttons
			List<WebElement> buttons = headerRow.findElement(
					By.xpath("//ul[@class='dropdown-menu left']"))
					.findElements(By.tagName("button"));
			for (int cnt = 0; cnt < buttons.size(); cnt++) {
				// get the name of the column in the list
				final String colName = StringUtils.trimToNull(StringUtils
						.normalizeSpace(buttons.get(cnt).getText()));
				// check if this column is to be active
				final boolean toBeActive = listOfColumns.contains(colName);
				// is column currently active
				final boolean currentlyActive = buttons.get(cnt)
						.getAttribute("class").contains("active");
				// we will need to click on the column name if currently not
				// active but we need it active or if we don't
				// want it but it is active
				if ((toBeActive && !currentlyActive)
						|| (!toBeActive && currentlyActive))
					buttons.get(cnt).click();
				// need to refresh the list of button again
				table = driver.findElement(tableXPath);
				headerRow = crmGetTableheaderRow(table, 0);
				buttons = headerRow.findElement(
						By.xpath("//ul[@class='dropdown-menu left']"))
						.findElements(By.tagName("button"));
			}
			// click on the header side to remove the focus
			// this causes issues finding elements
			// if we don't collapse the show more columns
			driver.findElement(
					By.xpath("//div[@class='headerpane']")).click();
			logDebugMessage("Finished displaying the columns. Clicked on the header to ensure that the show columns is collapsed");
		} else {
			logDebugMessage("No need to display additional columns. The default ones are the one displayed.");
		}
		
		// return the table but refresh it before doing so
		return (sortOrderAsc ? crmSortTableAsc(null, tableXPath, sortColumn)
				: crmSortTableDesc(null, tableXPath, sortColumn));
	}
	
	/** 
	 * Use this to get the number of records in the list view or subpanel
	 * */
	protected int crmGetNumOfRecordsInListViewOrSubpanel(WebElement table) {

		// capture all the data in the list view
		List<WebElement> rows = crmGetTableRows(table);

		logDebugMessage(
				concatStrings("The value to be returned by crmGetNumOfRecordsInListViewOrSubpanel(WebElement) is <",
						Integer.toString(rows.size()), ">"));
		return (rows.size());
	}
	
	/** 
	 * 
	 * */
	protected void crmSwitchCurrentWindow(String mainWindow) {

		driver.switchTo().window(mainWindow);
		logDebugMessage("Successfully switched to window -> " + mainWindow);
	}
	
	/**
	 * @param rows
	 *            - how many times the key should be pressed This would only
	 *            work on sidecar record views(e.g. views that has subpanels and
	 *            preview) and list view. Because I click a certain button that
	 *            is only visible in this view to transfer the focus. Also the
	 *            dashboard open/close should not be toggled so that selenium
	 *            can still see the element we click at the end of this method
	 */
	protected void crmScrollPage(int rows, Keys Keys) {

		logDebugMessage("Going to scroll the page by <" + rows + "> key press");
		// click something in the page to transfer the focus
		// and be able to scroll down using keys arrow down
		WebElement body = driver.findElement(By
				.xpath("//div[@class='btn-group']/a"));
		body.click();
		for (int i = 0; i < rows; i++)
			body.sendKeys(Keys);
		pauseSeleniumExecution(3000);
		// click on the preview side to remove the focus
		// this causes issues finding elements
		// if we don't remove the focus
		driver.findElement(
				By.xpath("//div[@class='side sidebar-content span4']")).click();
	}
	
	/**
	 * Use this to scroll the page for bwc/legacy modules. For example the
	 * Emails record view, calls list and record view.
	 * 
	 * */
	protected void crmScrollPageBwcModules(int numKeyPress, Keys keys) {

		logDebugMessage("Going to scroll the page in the BWC module by " + numKeyPress + " key press, direction " + keys);
		// click something in the page to transfer the focus
		// and be able to scroll down using keys arrow down
		WebElement moduleTitle = driver.findElement(By
				.xpath("//div[@class='moduleTitle']/h2"));
		moduleTitle.click();
		for (int i = 0; i < numKeyPress; i++) {
			try {
				setImplicitWait(1);
				Actions actions = new Actions(driver);
				actions.sendKeys(keys).perform();
			} catch (Exception e) {
				throw (new ErrorMessageException(
						"An exception has been encountered. Please see message for more details -> " + e.getMessage()));
			} finally {
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}
		}
		pauseSeleniumExecution(3000);
	}
	
	/**
	 * 
	 * Select a dropdown type filter. You can input multiple search values
	 * 
	 * Useful when searching using multiple filters.
	 * 
	 * @param field
	 *            the field filter
	 * @param operator
	 *            type in the operator display label
	 * @param filterValue
	 *            the filter to search. You can specify multiple values
	 * @param filterBoyRowNum
	 *            pass the row number location of the div class filter-body. The
	 *            number starts at 1 being the first filter row
	 * @param withinDrawer
	 *            pass true if within the Search and Select
	 * */
	protected void crmCreateDropdownTypeFilter(String field, String operator, int filterBoyRowNum, boolean withinDrawer,
			String... filterValues) {

		crmCreateFilterName(field, filterBoyRowNum, withinDrawer);
		crmCreateFilterOperator(operator, filterBoyRowNum, withinDrawer);

		WebElement filterextend = null;
		if (withinDrawer)
			filterextend = driver.findElement(
					By.xpath("//div[@class='drawer transition active']"))
					.findElement(
							By.xpath(".//div[@class='filter-options extend']"));
		else
			filterextend = driver.findElement(By
					.xpath(".//div[@class='filter-options extend']"));

		WebElement filterBody = filterextend.findElement(By
				.xpath(".//div[@class='filter-definition-container']/div["
						+ filterBoyRowNum + "]"));

		WebElement valueSelect = filterBody
				.findElement(By
						.xpath(".//div[@class='controls span4' and  @data-filter='value']"));
		
		List<String> listOfValues = Arrays.asList(filterValues);
		
		for (String string : listOfValues) {
			WebElement Ulist = valueSelect.findElement(By
					.xpath(".//ul[@class='select2-choices']"));
			Ulist.findElement(By.xpath(".//li[@class='select2-search-field']/input")).click();
			Ulist.findElement(By.xpath(".//li[@class='select2-search-field']/input")).sendKeys(string);
			List<WebElement> results = driver.findElements(By.xpath("//div[starts-with(@id,'select2-result-label-')]"));
			results.get(0).click();
			crmLoad();
		}
		
		logDebugMessage("Selected filter value(s) -> " + listOfValues + " in the specified filterBoyRowNum " + filterBoyRowNum);

		if (withinDrawer) {
			crmTableLoad(withinDrawer);
		} else {
			crmTableLoad(withinDrawer);
		}
	}
	
	/**
	 * Use this to toggle the Open/Close Dashboard in the record or list view
	 * 
	 * @param withinDrawer
	 *            if it's within a drawer or another drawer opens up pass true
	 * 
	 * */
	protected void crmToggleOpenCloseDashboard(boolean withinDrawer) {

		if (withinDrawer) {
			driver.findElement(
					By.xpath("//div[@class='drawer transition active']"))
					.findElement(
							By.xpath(".//button[@class='btn btn-invisible sidebar-toggle' and (@data-original-title='Open/Close Dashboard' or @title='Open/Close Dashboard')]"))
					.click();
			logDebugMessage("Clicked the Open/Close Dashboard toggle to expand the view");
		} else {
			driver.findElement(
					By.xpath("//button[@class='btn btn-invisible sidebar-toggle' and (@data-original-title='Open/Close Dashboard' or @title='Open/Close Dashboard')]"))
					.click();
			logDebugMessage("Clicked the Open/Close Dashboard toggle");
		}

		pauseSeleniumExecution(1000);
	}
	
	/** 
	 * Use this to verify the number of records in the list view or subpanel
	 * 
	 * */
	protected void crmVerifyNumOfRecordsInListViewOrSubpanel(WebElement table,
			int expectedNumOfRecords) {

		// capture all the data in the list view
		List<WebElement> rows = crmGetTableRows(table);
		logDebugMessage("Verifying that the expected number of records <"
				+ expectedNumOfRecords
				+ "> against the actual number of records of <"
				+ rows.size() + ">");
		// verify the expected number for records
		assertEquals(rows.size(), expectedNumOfRecords, "The expected number of records is not correct!");
	}
	
	/**
	 * Get the element that holds the specific sub-panel.
	 *
	 * @param name
	 * @return
	 */
	protected WebElement crmFindSubPanel(String subpanel) {

		WebElement foundElement = null;
		// find all the sub-panel on the current page
		logDebugMessage("Looking for all the subpanels on the page");
		List<WebElement> divs = driver
				.findElements(By
						.xpath("//div[starts-with(@class,'filtered tabbable tabs-left')]"));

		if (divs != null && divs.size() > 0) {
			for (WebElement subPanel : divs) {
				if (subPanel.findElement(By.tagName("h4")).getText()
						.equalsIgnoreCase(subpanel)) {
					// sub-panel has been found, stop searching
					foundElement = subPanel;
					logDebugMessage("Found the subpanel '"
							+ subPanel.findElement(By.tagName("h4")).getText()
							+ "' in the page and it's equal to '" + subpanel
							+ "' which we are looking for");
					break;
				}
			}
		}
		
		return (foundElement);
	}
	
	/**
	 * Ensure a sub-panel with a specific name is open.
	 *
	 * @param name
	 */
	protected WebElement crmOpenSubPanel(String subpanel, boolean withLoad) {

		// find all the sub-panel on the current page
		WebElement subPanel = crmFindSubPanel(subpanel);
		if (subPanel == null)
			throw (new IllegalArgumentException("Sub-panel '" + subpanel
					+ "' not found"));
		logDebugMessage("Going to check if the subpanel '" + subpanel
				+ "' is open/toggled");

		// force the sup-panel to be open
		if (!crmIsSubPanelOpen(subpanel)) {
			logDebugMessage("Subpanel is closed - will toggle to open it");
			// sub-panel is not open so open it,
			// this may load in new content into the panel
			subPanel.findElement(By.xpath(".//a[@class='btn btn-invisible']")).click();
			logDebugMessage("Clicked the toggle icon");
			if (withLoad) {
				// wait for the page load to appear
				crmLoad();
				// need to obtain the sub-panel object again as it's content may
				// have changed
				subPanel = crmFindSubPanel(subpanel);
			} else {
				// need to obtain the sub-panel object again as it's content may
				// have changed
				subPanel = crmFindSubPanel(subpanel);
			}
		}

		// return the sub-panel element
		return (subPanel);
	}
	
	/**
	 * Ensure the content of the table is sorted in descending order. This opens
	 * the panel if required.
	 *
	 * @param panelName
	 * @param columnIndex
	 */
	protected WebElement crmSortTableDescWithinPanel(String panelName, int columnIndex, boolean withSubpanelLoad) {

		logDebugMessage("Going to capture the table details in subpanel -> "
				+ panelName + " and will sort in columnIndex <" + columnIndex
				+ "> in ASC");
		// find the required sub-panel
		WebElement subpanels = crmOpenSubPanel(panelName, withSubpanelLoad);
		// return the table we were working on (but refresh the details first)
		return (crmSortTableDesc(subpanels,
				By.xpath(".//table[@class='table table-striped dataTable']"),
				columnIndex));
	}
	
	/**
	 * Ensure the content of the table is sorted in ascending order. This opens
	 * the panel if required.
	 *
	 * @param panelName
	 * @param columnIndex
	 */
	protected WebElement crmSortTableAscWithinPanel(String panelName, int columnIndex, boolean withSubpanelLoad) {

		logDebugMessage("Going to get the WebElement details from subpanel -> "
				+ panelName + " and will sort in columnIndex <" + columnIndex
				+ "> in ASC");
		// find the required sub-panel
		WebElement subpanels = crmOpenSubPanel(panelName, withSubpanelLoad);
		// return the table we were working on (but refresh the details first)
		return (crmSortTableAsc(subpanels,
				By.xpath(".//table[@class='table table-striped dataTable']"),
				columnIndex));
	}
	
	/**
	 * This would verify if the expected value contains in the list
	 * view(including Search and Select) or subpanel.
	 * 
	 * For param columnPos, the number starts at 0 being the first column list
	 * or subpanel. Please note that the column for the bulk actions is actually
	 * the first column (e.g. 0). There are subpanels that do not have bulk
	 * actions (e.g. Communications subpanel). Also please note that this is
	 * case sensitive
	 * 
	 * @param table
	 * @param rowRecord
	 * @param columnPos
	 *            the number starts at 0 being the first column on the list or
	 *            subpanel.
	 * @param expectedValue
	 * 
	 * */
	protected void crmVerifyListOrSubpanelContainsValue(WebElement table,
			int rowRecord, int columnPos, String expectedValue) {

		List<WebElement> rowCells = crmGetTableRowCells(table, rowRecord);
		String actualValueRaw = rowCells.get(columnPos).getText();
		String actualValue = StringUtils.normalizeSpace(actualValueRaw);
		logDebugMessage("Validating in the specified table in rowRecord "
				+ rowRecord + " if this expectedValue -> '" + expectedValue
				+ "' in column " + columnPos
				+ " is present in the actualValue -> '" + actualValue + "'");
		assertTrue(actualValue.contains(expectedValue),
				"The actualValue '" + actualValue + "' does not contain the expectedValue '" + expectedValue
						+ "' in rowRecord <".concat(Integer.toString(rowRecord).concat("> and in columnPos <")
								.concat(Integer.toString(columnPos).concat(">"))));
	}

	/**
	 * This would verify if the expected value equals in the list view(including
	 * Search and Select) or subpanel.
	 * 
	 * For param columnPos, the number starts at 0 being the first column list
	 * or subpanel. Please note that the column for the bulk actions is actually
	 * the first column (e.g. 0). There are subpanels that do not have bulk
	 * actions (e.g. Communications subpanel). Also please note that this is
	 * case sensitive
	 * 
	 * @param table
	 *            the WebElement
	 * @param rowRecord
	 *            the number starts at 0 being the first record on the list or
	 *            subpanel
	 * @param columnPos
	 *            the number starts at 0 being the first column on the list or
	 *            subpanel.
	 * @param expectedValue
	 *            the expected value assertion
	 * 
	 * */
	protected void crmVerifyListOrSubpanelEqualsValue(WebElement table,
			int rowRecord, int columnPos, String expectedValue) {

		List<WebElement> rowCells = crmGetTableRowCells(table, rowRecord);
		String actualValueRaw = rowCells.get(columnPos).getText();
		String actualValue = StringUtils.normalizeSpace(actualValueRaw);
		logDebugMessage("Validating in the specified table in rowRecord "
				+ rowRecord + " if this expectedValue -> '" + expectedValue
				+ "' in column " + columnPos
				+ " is equal to the actualValue -> '" + actualValue + "'");
		assertTrue(actualValue.equals(expectedValue),
				"The expectedValue '" + expectedValue + "' is not equal to the actualValue '" + actualValue
						+ "' in rowRecord <".concat(Integer.toString(rowRecord)
								.concat("> and in columnPos <".concat(Integer.toString(columnPos).concat(">")))));
	}
	
	/**
	 * <pre>
	 * Use this to verify that the specified column in the subpanel
	 * or list view is blank.
	 * 
	 * StringUtils.isNotBlank(null)       = false
	 * StringUtils.isNotBlank("")         = false
	 * StringUtils.isNotBlank(" ")        = false
	 * StringUtils.isNotBlank("bob")      = true
	 * StringUtils.isNotBlank("  bob  ")  = true
	 * 
	 * @param rowRecord the number starts at 0 being the first on the list or subpanel.
	 * 
	 * @param columnPos the number starts at 0 being the first column list or subpanel. Please
	 * note that the column for the bulk actions is actually the first column.
	 * 
	 * </pre>
	 * */
	protected void crmVerifyListorSubpanelValueIsBlank(WebElement table,
			int rowRecord, int columnPos) {

		String value = crmGetListOrSubpanelValue(table, rowRecord, columnPos);
		logDebugMessage("Validating in the specified table in rowRecord "
				+ rowRecord + " if the column " + columnPos + " is blank");
		assertTrue(StringUtils.isBlank(value), "The captured value in row " + rowRecord + " and column "
				+ columnPos + " is not blank or null");
	}
	
	/**
	 * This would verify if the expected value starts with in the list
	 * view(including Search and Select) or subpanel.
	 * 
	 * For param columnPos, the number starts at 0 being the first column list
	 * or subpanel. Please note that the column for the bulk actions is actually
	 * the first column (e.g. 0). There are subpanels that do not have bulk
	 * actions (e.g. Communications subpanel). Also please note that this is
	 * case sensitive
	 * 
	 * @param table
	 *            the WebElement
	 * @param rowRecord
	 *            the number starts at 0 being the first record on the list or
	 *            subpanel
	 * @param columnPos
	 *            the number starts at 0 being the first column on the list or
	 *            subpanel.
	 * @param expectedValue
	 *            the expected value assertion
	 * 
	 * */
	protected void crmVerifyListOrSubpanelStartsWith(WebElement table,
			int rowRecord, int columnPos, String expectedValue) {

		List<WebElement> rowCells = crmGetTableRowCells(table, rowRecord);
		String actualValueRaw = rowCells.get(columnPos).getText();
		String actualValue = StringUtils.normalizeSpace(actualValueRaw);
		logDebugMessage("Validating in the specified table in rowRecord "
				+ rowRecord + " if this expectedValue -> '" + expectedValue
				+ "' in column " + columnPos
				+ " starts with the actualValue -> '" + actualValue + "'");
		assertTrue(actualValue.startsWith(expectedValue),
				"The expectedValue '" + expectedValue + "' does not start with the actualValue '" + actualValue
						+ "' in rowRecord <".concat(Integer.toString(rowRecord)
								.concat("> and in columnPos <".concat(Integer.toString(columnPos).concat(">")))));
	}
	
	/** 
	 * Use this to mass update the status
	 * of the schedulers
	 * */
	protected void crmMassUpdateSchedulerStatus(String searchStr, boolean activate) {
		
		logDebugMessage("Will be " + (activate ? "turning ON" : "turning OFF") + " the scheduler(s) that has the name '"
				+ searchStr + "'");
        // via the Admin area access the Scheduler list view
        WebElement headerbar = driver.findElement(By.xpath("//div[@id='header']"))
            .findElement(By.xpath(".//div[@class='navbar navbar-fixed-top']"))
            .findElement(By.xpath(".//div[@class='nav-collapse']"));
        headerbar.findElement(By.xpath(".//ul[@id='userList']")).findElement(By.xpath(".//button[@id='userTab']"))
            .click();
        driver.findElement(By.linkText("Admin")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Administration", "BlueBilling");
        pauseSeleniumExecution(2000);
        crmSwitchToBwcIframe();
        driver.findElement(By.linkText("Scheduler")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Schedulers", "BlueBilling");
        pauseSeleniumExecution(2000);
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // clear the search results area before applying to search as otherwise it will not work
        WebElement mainTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
        mainTable.findElement(By.xpath(".//form[@id='search_form']"))
            .findElement(By.xpath(".//input[@id='name_basic']")).clear();
        mainTable.findElement(By.xpath(".//form[@id='search_form']"))
            .findElement(By.xpath(".//input[@id='search_form_submit' and  @value='Search' and  @name='button']"))
            .click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Schedulers", "BlueBilling");
        pauseSeleniumExecution(2000);
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // search for the required schedulers
        mainTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
        mainTable.findElement(By.xpath(".//form[@id='search_form']"))
            .findElement(By.xpath(".//input[@id='name_basic']")).sendKeys(searchStr);
        mainTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
        // verify the search string is provided
        String search = mainTable.findElement(By.xpath(".//form[@id='search_form']"))
                .findElement(By.xpath(".//input[@id='name_basic']")).getAttribute("value");
        verifyTwoStringsAreEqual(search, searchStr, true);
        mainTable.findElement(By.xpath(".//form[@id='search_form']"))
            .findElement(By.xpath(".//input[@id='search_form_submit' and  @value='Search' and  @name='button']"))
            .click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Schedulers", "BlueBilling");
        pauseSeleniumExecution(2000);
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // click the select all checkbox
        mainTable = driver.findElement(By.xpath("//table[@id='contentTable']"));
        // click the more actions button
        mainTable.findElement(By.xpath(".//input[@id='massall_top']")).click();
        
        // add wait time until button is clickable
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(".//ul[@id='actionLinkTop']")));
        
        WebElement actionButtons = mainTable.findElement(By.xpath(".//ul[@id='actionLinkTop']"));
        actionButtons.findElement(By.xpath(".//span[starts-with(@class, 'ab')]")).click();
        driver.findElement(By.linkText("Mass Update")).click();
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // go to the mass update form
        WebElement massUpdateForm = mainTable.findElement(By.xpath(".//div[@id='mass_update_div']"));
        WebElement massUpdateTable = massUpdateForm.findElement(By.xpath(".//table[@id='mass_update_table']"));
        logDebugMessage("Will be looking for xpath './/select[@id='mass_status']'");
        Select dropdown = new Select(massUpdateTable.findElement(By.xpath(".//select[@id='mass_status']")));
        dropdown.selectByVisibleText(activate ? "Active" : "Inactive");
        logDebugMessage("Selected the dropdown value '" + (activate ? "Active" : "Inactive") + "'");
        
        // save the records
        WebElement massUpdateTableSave = massUpdateForm.findElement(By.xpath("./table[2]"));
        massUpdateTableSave.findElement(By.xpath(".//input[@id='update_button']")).click();
        pauseSeleniumExecution(1000);
        // click the confirm on the
        // browser generated pop-op
        browserPopupAccept(true);
        pauseSeleniumExecution(5000);
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "Schedulers", "BlueBilling");
        pauseSeleniumExecution(2000);
        // get-out of the frame
        switchToDefaultContent();
		logDebugMessage((activate ? "Turned ON" : "Turned OFF") + " the scheduler(s) that has the name '"
				+ searchStr + "'");
	}
	
	/**
	 * Get the preview element for a specific row in the table.
	 *
	 * @param table
	 * @param rowNum
	 *            starts at 0 being the first record on the list
	 * @return
	 */
	protected WebElement crmGetPreview(WebElement table, int rowNum) {

		logDebugMessage("Going to hit the preview in row -> " + rowNum);
		// get the required row
		WebElement row = crmGetTableRow(table, rowNum);
		// within the row find the preview button element and click on it
		row.findElement(
				By.xpath(".//a[starts-with(@class,'rowaction btn') and (@data-original-title='Preview' or @title='Preview')]"))
				.click();
		logDebugMessage("Clicked the preview button, will now wait for the loading message to appear");
		// wait for the page to load
		crmLoad();
		// get the preview web element and return it
		return (driver.findElement(By
				.xpath("//div[@class='block preview-data']")));
	}
	
	/**
	 * The Param rowNum start at 1 being the first value in the preview
	 * 
	 * @param preview
	 * @param rowFluidNum
	 *            is the first 'row-fluid' class after the 'block preview-data'
	 *            class
	 * @param expectedValue
	 */
	protected void crmVerifyIfPreviewValueCorrect(WebElement preview,
			int rowFluidNum, String expectedValue) {

		WebElement label = preview.findElement(By.xpath("./div[" + rowFluidNum
				+ "]/div[1]"));
		String displayLabelRaw = label.getText();
		String displayLabel = StringUtils.normalizeSpace(displayLabelRaw);
		WebElement value = preview.findElement(By.xpath("./div[" + rowFluidNum
				+ "]/div[2]"));
		String previewValueRaw = value.getText();
		String previewValue = StringUtils.normalizeSpace(previewValueRaw);
		logDebugMessage("Validating in the '" + displayLabel
				+ "' display label, rowFluidNum <" + rowFluidNum + "> if the actual preview value '" + previewValue
				+ "' is equal to the expected preview value '" + expectedValue
				+ "'");
		assertTrue(previewValue.equals(expectedValue), "In the '" + displayLabel + "' display label, rowFluidNum <" + rowFluidNum + ">; the actual display value '" + previewValue
				+ "' is not equal to the expected display value '"
				+ expectedValue + "'");
	}
	
	/**
	 * Use this to verify if preview value is blank
	 * */
	protected void crmVerifyIfPreviewValueIsBlank(WebElement preview,
			String previewDisplayLabel) {

		String previewValue = crmGetPreviewDataByLabel(preview, previewDisplayLabel);
		logDebugMessage("Validating that the value in the preview display label '"
				+ previewDisplayLabel + "' is blank");
		assertTrue(StringUtils.isBlank(previewValue), "The value in the preview display label '"
				+ previewDisplayLabel + "' is not blank!");
	}
	
	/**
	 * Use this to verify if preview value is blank
	 * */
	protected void crmVerifyIfPreviewValueIsBlank(WebElement preview, int rowFluidNum) {

		WebElement value = preview.findElement(By.xpath("./div[" + rowFluidNum
				+ "]/div[2]"));
		String previewValueRaw = value.getText();
		String previewValue = StringUtils.normalizeSpace(previewValueRaw);
		logDebugMessage("Validating that in the rowFluidNum <" + rowFluidNum
				+ "> the preview value is blank");
		assertTrue(StringUtils.isBlank(previewValue), "The value is not blank in rowFluidNum <" + rowFluidNum + ">");
	}
	
	protected void crmRemoveSMSConfig() {
    	
		logDebugMessage("Will be removing the SMS config providers");
        // via the Admin area access the Connectors section
        WebElement headerbar = driver.findElement(By.xpath("//div[@id='header']"))
            .findElement(By.xpath(".//div[@class='navbar navbar-fixed-top']"))
            .findElement(By.xpath(".//div[@class='nav-collapse']"));
        headerbar.findElement(By.xpath(".//ul[@id='userList']")).findElement(By.xpath(".//button[@id='userTab']"))
            .click();
        driver.findElement(By.linkText("Admin")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Administration", "BlueBilling");
        crmSwitchToBwcIframe();
        driver.findElement(By.linkText("Connectors")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Connector Settings", "BlueBilling");
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // choose Set Connector Properties
        WebElement mainTable = driver.findElement(By.xpath("//table[@class='edit view small']"));
        mainTable.findElement(By.name("connectorConfig")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Set Connector Properties", "BlueBilling");
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // Click on SMS Configuration
        // wait till the Twitter section is loaded
        waitForElement(By.id("ext_rest_twitter_oauth_consumer_key"), PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT);
        driver.findElement(By.linkText("SMS Configuration")).click();
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Set Connector Properties", "BlueBilling");
        // wait till the SMS Configuration section is loaded
        waitForElement(By.id("ext_rest_sms_smsprovider"), PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT);
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        logDebugMessage("Will be looking for id 'ext_rest_sms_smsprovider'");
        Select dropdown = new Select(driver.findElement(By.id("ext_rest_sms_smsprovider")));   
        logDebugMessage("Found the id 'ext_rest_sms_smsprovider'");
        // Choose the CDYNE provider
        // then the clear the values
        dropdown.selectByVisibleText("CDYNE");
        logDebugMessage("Selected CDYNE SMS provider");
        driver.findElement(By.id("ext_rest_sms_assigneddid")).clear();
        driver.findElement(By.id("ext_rest_sms_licensekey")).clear();
        logDebugMessage("Cleared the credentials for CDYNE provider");
        
        // Choose the Twilio provider
        // then the clear the values
        dropdown.selectByVisibleText("Twilio");
        logDebugMessage("Selected Twilio SMS provider");
        driver.findElement(By.id("ext_rest_sms_twilioActiveNumber")).clear();
        driver.findElement(By.id("ext_rest_sms_twilioAccountSID")).clear();
        driver.findElement(By.id("ext_rest_sms_twilioAuthToken")).clear();
        logDebugMessage("Cleared the credentials for Twilio provider");
        
        // README
        // for some reason it takes 2-3 minutes before clicking the connectors_top_save button
        // already tried changing implicit wait but still the same
        // Save the settings
        logDebugMessage("Will be looking for the element 'connectors_bottom_save'");
        // try javascript click next if it still takes times
        WebElement saveBtn = driver.findElement(By.id("connectors_bottom_save"));
        javaScriptClickElementAction(saveBtn);
        logDebugMessage("Clicked 'connectors_bottom_save' to save the connectors settings");
        waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Connector Settings", "BlueBilling");
        switchToDefaultContent();
        logDebugMessage("Finished clearing SMS providers credentials");
    }
	
	/**
	 * Set the System wide Email Settings. This is for sending emails from the CRM
	 *
	 * @param emailProvider - currently possible values are gmail, yahoomail, exchange, other
	 * @param sslOrTls - currently possible values are -none-, SSL, TLS
	 * 
	 */
	protected void crmSetEmailSettings(String fromName, String fromAddress,
			String emailProvider, boolean useSmtpAuth, String mailServer,
			String userName, String password, boolean forAllUsers, String port,
			String sslOrTls) {
    	
		logDebugMessage("Will be setting up the Global Email Settings");
        // via the Admin area access the Email Settings section
        WebElement headerbar = driver.findElement(By.xpath("//div[@id='header']"))
            .findElement(By.xpath(".//div[@class='navbar navbar-fixed-top']"))
            .findElement(By.xpath(".//div[@class='nav-collapse']"));
        headerbar.findElement(By.xpath(".//ul[@id='userList']")).findElement(By.xpath(".//button[@id='userTab']"))
            .click();
        driver.findElement(By.linkText("Admin")).click();
        super.waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Administration", "BlueBilling");
        crmSwitchToBwcIframe();
        driver.findElement(By.linkText("Email Settings")).click();
        super.waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Email Settings", "BlueBilling");
        switchToDefaultContent();
        crmSwitchToBwcIframe();
        
        // wait till we can see the From Name section
        waitForElement(By.id("notify_fromname"), CRM_WAIT_ELEMENT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT);
        // populate the From Name
        driver.findElement(By.id("notify_fromname")).clear();
        driver.findElement(By.id("notify_fromname")).sendKeys(fromName);
     	// populate the From Address      
        driver.findElement(By.id("notify_fromaddress")).clear();
        driver.findElement(By.id("notify_fromaddress")).sendKeys(fromAddress);
        // choose the Email Provider
        driver.findElement(By.id(emailProvider)).click();
        // set the SMTP Mail Server
        driver.findElement(By.id("mail_smtpserver")).clear();
        driver.findElement(By.id("mail_smtpserver")).sendKeys(mailServer);
        // let's get the state of SMTP Authentication
        boolean smtpAuthenticationState = driver.findElement(By.id("mail_smtpauth_req")).isSelected();
        
        // if Use SMTP Authentication is ticked and useSmtpAuth is true
        // do nothing and just output a note
        if (smtpAuthenticationState && useSmtpAuth) {
			logDebugMessage("Use SMTP Authentication is currently ticked, no action needed");
		}
        // if Use SMTP Authentication is not ticked and useSmtpAuth is true
        // let's tick Use SMTP Authentication
        if (!smtpAuthenticationState && useSmtpAuth) {
        	driver.findElement(By.id("mail_smtpauth_req")).click();
		}
        // if Use SMTP Authentication is ticked and useSmtpAuth is false
        // un-tick Use SMTP Authentication
        if (smtpAuthenticationState && !useSmtpAuth) {
        	driver.findElement(By.id("mail_smtpauth_req")).click();
		}
        // if Use SMTP Authentication is not ticked and useSmtpAuth is false
        // do nothing and just output a note
        if (!smtpAuthenticationState && !useSmtpAuth) {
			logDebugMessage("Use SMTP Authentication is currently NOT ticked, no action needed");
		}
        
        // set the Username
        driver.findElement(By.id("mail_smtpuser")).clear();
        driver.findElement(By.id("mail_smtpuser")).sendKeys(userName);
        boolean isChangePassLinkDisplayed;
        try {
    		// set implicit wait to 2 seconds
        	setImplicitWait(2);
    		driver.findElement(By.linkText("Change password"));
    		logDebugMessage("The Change password link is displayed");
    		isChangePassLinkDisplayed = true;
		} catch (NoSuchElementException nse) {
			logDebugMessage("The Change password link is not displayed");
			isChangePassLinkDisplayed = false;
		} finally {
			// turn back the normal implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
        
        if (isChangePassLinkDisplayed) {
            // click the Change password link
            // and put the password
            driver.findElement(By.linkText("Change password")).click();
            waitForElement(By.id("mail_smtppass"), CRM_WAIT_ELEMENT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT);
            driver.findElement(By.id("mail_smtppass")).clear();
            driver.findElement(By.id("mail_smtppass")).sendKeys(password);
		} else {
            driver.findElement(By.id("mail_smtppass")).clear();
            driver.findElement(By.id("mail_smtppass")).sendKeys(password);
		}
        
        // let's get the state of Allow users to use this account for outgoing email
        boolean allowUsersToUseEmailState = driver.findElement(By.id("notify_allow_default_outbound")).isSelected();
        
        // if Allow users to use this account for outgoing email is ticked and forAllUsers is true
        // do nothing and just output a note
        if (allowUsersToUseEmailState && forAllUsers) {
			logDebugMessage("Allow users to use this account for outgoing email is currently ticked, no action needed");
		}
        // if Allow users to use this account for outgoing email is not ticked and forAllUsers is true
        // let's tick Allow users to use this account for outgoing email
        if (!smtpAuthenticationState && forAllUsers) {
        	driver.findElement(By.id("mail_smtpauth_req")).click();
		}
        // if Allow users to use this account for outgoing email is ticked and forAllUsers is false
        // un-tick Allow users to use this account for outgoing email
        if (smtpAuthenticationState && !forAllUsers) {
        	driver.findElement(By.id("mail_smtpauth_req")).click();
		}
        // if Allow users to use this account for outgoing email is not ticked and forAllUsers is false
        // do nothing and just output a note
        if (!smtpAuthenticationState && !forAllUsers) {
			logDebugMessage("Allow users to use this account for outgoing email is currently NOT ticked, no action needed");
		}
        
        // set the SMTP Port
        driver.findElement(By.id("mail_smtpport")).clear();
        driver.findElement(By.id("mail_smtpport")).sendKeys(port);
        
        // choose the value for Enable SMTP over SSL or TLS?
        Select enableSSLorTLS = new Select(driver.findElement(By.id("mail_smtpssl")));
        enableSSLorTLS.selectByVisibleText(sslOrTls);
        
        driver.findElement(By.id("btn_save")).click();
        super.waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, 20, "Administration", "BlueBilling");
        waitForElement(By.linkText("User Management"), CRM_WAIT_ELEMENT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT);
        //switch back to the default content 
        switchToDefaultContent();
        logDebugMessage("Finished setting up Global Email Settings");
    }
	
	/**
	 * Let's verify if engine is turned on or off
	 * 
	 * @return true if the engine version is displaying in the About section and if
	 *         the version displayed there is not based on the Apiary
	 * 
	 */
	protected boolean crmVerifyEngineState() {

		// via the Admin area access the About section
		WebElement headerbar = driver.findElement(By.xpath("//div[@id='header']"))
				.findElement(By.xpath(".//div[@class='navbar navbar-fixed-top']"))
				.findElement(By.xpath(".//div[@class='nav-collapse']"));
		headerbar.findElement(By.xpath(".//ul[@id='userList']")).findElement(By.xpath(".//button[@id='userTab']"))
				.click();
		driver.findElement(By.linkText("About")).click();
		waitForPageTitle(PAGE_TITLE_WAIT_TIMEOUT, CRM_IMPLICIT_WAIT_TIMEOUT, "About", "BlueBilling");

		boolean engineLoadingDone = false;
		try {
			setImplicitWait(12);
			driver.findElement(By.xpath("//span[starts-with(@class,'bluebilling-copyright-version')]"));
			engineLoadingDone = true;
		} catch (NoSuchElementException nsee) {
			logDebugMessage("The engine is turned Off because the element was not found.");
			return false;
		} finally {
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}

		if (engineLoadingDone) {
			// verify we can see the link
			driver.findElement(By.linkText("Blue Oak Solutions Pty Ltd."));
			// verify the engine is not pointed in the
			// Apiary Document
			String crmEngineValue = driver
					.findElement(By.xpath("//span[starts-with(@class,'bluebilling-copyright-version')]/b")).getText();
			// This is the current value of the engine
			// in the Apiary for this end point -> /system/version
			String apiaryEngineValue = "BlueBilling, Version 3.0.1 (Build 925)";
			logDebugMessage(concatStrings("The captured value of the engine in the CRM is -> '",
					StringUtils.normalizeSpace(crmEngineValue), "'"));

			crmEngineValue = StringUtils.deleteWhitespace(crmEngineValue);
			apiaryEngineValue = StringUtils.deleteWhitespace(apiaryEngineValue);

			// if true, then it's connected in the apiary
			// not in the live/production engine
			if (crmEngineValue.equals(apiaryEngineValue)) {
				logDebugMessage("The engine is turned Off. It's showing the Apiary engine value.");
				return false;
			} else {
				logDebugMessage("The engine is turned On.");
				return true;
			}
		} else {
			logDebugMessage("The engine is turned Off because it only keeps on loading.");
			return false;
		}
	}
	
	/**
	 * use to set the methods for the notification 
	 * 
	 * @param notifRow Bills is row 1, etc
	 * @param methods 'postal','email','sms'
	 * @param string 
	 */
	protected void crmSetContactNotificationInRow(int notifRow, String... methods) {
		
		int lengthMethods = methods.length;
		
		for (int i = 0; i < lengthMethods; i++) {
			logDebugMessage("Would be clicking the method " + methods[i] + " in row " + notifRow);
			WebElement notifConfig = driver.findElement(By.xpath("//div[@id='NotificationEditConfig']/table[@id='notification-wrapper']/tbody"));
			WebElement notifRowElem = notifConfig.findElement(By.xpath("./tr[@id='notification-group-checkbox'][" + notifRow + "]"));
			// click the method specified
			WebElement notifMethod = notifRowElem
					.findElement(By
							.xpath("./td[@class='notification-method']/input[@type='checkbox' and starts-with(@class, '" + methods[i] + "')]"));
			notifMethod.click();
			logDebugMessage("Successfully clicked the method " + methods[i] + " in row " + notifRow);
		}
	}
	
	/**
	 * README
	 * When hitting save and there's a delay before the loading is displayed,
	 * there's a huge chance that the selenium would get stuck if you use
	 * this method.
	 * To fix this, please ensure that you use the method
	 * 'waitForLoading(3, 5)'
	 * please note that the arguments are already passed on that example.
	 * 
	 * TODO 
	 * Update this method where you add a parameter, and this parameter is
	 * your identifier that the success method is successful. For example, in
	 * account record view, after the success message appeared and disappeared
	 * you will see the Edit mode. So you will pass an args to the location of
	 * the edit mode web element. This is because there are screens where no
	 * error message is reported if there is an issue saving a specific record.
	 * An example is in the  record view, when you hit edit and the
	 * Accounts Cutover Date is before the date in the First Bill Date, the
	 * Accounts Cutover Date is in error state but no error message is
	 * displayed. This will cause for this method to pass and also the test case
	 * may pass.
	 * 
	 * Wait for the 'success' message to appear and disappear.
	 * 
	 * Nino - I have not confirmed this but it seems that this does not work
	 * well when the application saves and there's a subsequent loading message.
	 * For example when saving an account(fresh created account), it shows a
	 * loading message, then success message, then another loading message. OR
	 * when the success message is too quick to disappear. This causes the
	 * selenium test cases to freeze and get stuck on this stage. It takes 3
	 * hours before it will execute the next command/test.
	 * 
	 * Nino - I think another issue where the screen hangs is when saving a
	 * contact from the record view. (e.g. Open contact from list view then edit
	 * save). The 'Success' message is on top and another one 'Loading' below.
	 * 
	 * This works best if the record you are saving would only display a loading
	 * initially then lastly a success message. (No loading message that
	 * follows)
	 */
	protected void crmSuccess() {
		
		FluentWait<WebDriver> wait_1 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(CRM_IMPLICIT_WAIT_TIMEOUT))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);

		// check if the success message is appearing
		boolean loadingPresent = false;
		try {
			
			/**
			 * Start Add: KPanaglima 07/28
			 * Added an if condition to handle success alerts 
			 * that disappear in a short span of time. If there is none,
			 * it will just continue based on the conditions below.
			 * 
			 */
			if(driver
				.findElement(
						By.xpath("//div[@class='alert alert-success alert-block']")).isDisplayed()){
				logDebugMessage("Success Message Displayed");
			}
			
			/**
			 * End Add: KPanaglima 07/28
			 */
			 
			
			// turn off the implicit wait
			setImplicitWait(0);
			// check if the success message is appearing or an error to appear
			loadingPresent = wait_1.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					logDebugMessage("Starting the process in the success() method");

					boolean success = false;
					int attempts = 1;
					int maxAttempts = 31;

					while (!success && attempts < maxAttempts) {
						logDebugMessage("Checking if the success message appeared");
						try {
							logDebugMessage("Entered Try Block");
							success = driver
									.findElement(
											By.xpath("//div[@class='alert alert-success alert-block']"))
									.isDisplayed();
							logDebugMessage("After FindElement");
							String successMessage = driver
									.findElement(
											By.xpath("//div[@class='alert alert-success alert-block']"))
									.getText();
							logDebugMessage("Success message is now displayed. Message is -> '"
									+ successMessage + "'");
						} catch (NoSuchElementException nsee) {
							success = false;
						} catch (StaleElementReferenceException sere) {
							logDebugMessage("StaleElementReferenceException encountered while trying to wait for the success message, will try to look for the element again.");
							try {
								success = driver
										.findElement(
												By.xpath("//div[@class='alert alert-success alert-block']"))
										.isDisplayed();

								String successMessage = driver
										.findElement(
												By.xpath("//div[@class='alert alert-success alert-block']"))
										.getText();
								logDebugMessage("Success message is now displayed. Message is -> '"
										+ successMessage + "'");
							} catch (NoSuchElementException nsee) {
								success = false;
							}
						}
						attempts++;
					}

					if (!success) {
						// success has not appeared, check if an
						// error has appeared
						logDebugMessage("Checking if an error appeared since success message is not yet displayed");

						boolean error = false;

						try {
							error = driver
									.findElement(
											By.xpath("//div[@class='alert alert-danger alert-block']"))
									.isDisplayed();
						} catch (NoSuchElementException nsee) {
							logDebugMessage("Error message is not displayed");
						} catch (StaleElementReferenceException sere) {
							logDebugMessage("StaleElementReferenceException encountered while trying to check for the error message, will try to look for the element again.");
							try {
								error = driver
										.findElement(
												By.xpath("//div[@class='alert alert-danger alert-block']"))
										.isDisplayed();
							} catch (NoSuchElementException nsee) {
								logDebugMessage("Error message is not displayed");
							}
						}

						if (error) {
							// error has occurred, get the error message
							logDebugMessage("An Error has occured, now getting the error details");
							String errorMessage = driver
									.findElement(
											By.xpath("//div[@class='alert alert-danger alert-block']"))
									.getText();
							// report this error as an exception
							throw (new ErrorMessageException(
									"Error occurred while waiting for the success message, error message is ... "
											+ errorMessage));
						}

					}
					return (success);
				}
			}).booleanValue();
		} catch (ErrorMessageException exception) {
			logErrorMessage("ErrorMessageException is encountered. See message for more details -> "
					+ exception);
			throw (exception);
		} catch (TimeoutException toe) {
			logDebugMessage("Done waiting a maximum of <"
					+ CRM_IMPLICIT_WAIT_TIMEOUT
					+ "> seconds while waiting for the Success or Error message to appear");
		} catch (Exception exception) {
			logDebugMessage("A general exception was reached while waiting a maximum of <"
					+ CRM_IMPLICIT_WAIT_TIMEOUT
					+ "> seconds for the Success or Error message to appear. Please see this exception for more details -> "
					+ exception);
		} finally {
			// turn back on the implicit wait
			setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
		}
		
		FluentWait<WebDriver> wait_2 = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(60))
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class);

		if (loadingPresent) {
			// success message was seen before, so now wait
			// until it disappears
			try {
				// turn off the implicit wait
				setImplicitWait(0);
				wait_2.until(new Function<WebDriver, Boolean>() {
					public Boolean apply(WebDriver driver) {
						logDebugMessage("Now checking if the success message disappeared");

						boolean stillDisplayed = true;
						boolean finishedChecking = false;
						int attempts = 1;
						int maxAttempts = 46;

						while (stillDisplayed && attempts < maxAttempts
								&& !finishedChecking) {
							try {
								stillDisplayed = driver
										.findElement(
												By.xpath("//div[@class='alert alert-success alert-block']"))
										.isDisplayed();
							} catch (NoSuchElementException nsee) {
								stillDisplayed = false;
								finishedChecking = true;
								logDebugMessage("Success message is no longer displayed");
							} catch (StaleElementReferenceException sere) {
								logDebugMessage("StaleElementReferenceException encountered while trying to wait for the success message to disappear, will try to look for the element again.");
								try {
									stillDisplayed = driver
											.findElement(
													By.xpath("//div[@class='alert alert-success alert-block']"))
											.isDisplayed();
								} catch (NoSuchElementException nsee) {
									stillDisplayed = false;
									finishedChecking = true;
									logDebugMessage("Success message is no longer displayed");
								}
							}
							attempts++;
						}

						return (finishedChecking);
					}
				});
			} catch (TimeoutException toe) {
				throw (new TimeoutException(
						"Done waiting <"
								+ CRM_IMPLICIT_WAIT_TIMEOUT
								+ "> seconds but the Success message is still displayed. See error exception details -> "
								+ toe.getMessage()));
			} catch (Exception exception) {
				logDebugMessage("A general exception was encountered while waiting for the Success message to disappear. See message for more details -> "
						+ exception.getMessage());
			} finally {
				// turn back on the implicit wait
				setImplicitWait(CRM_IMPLICIT_WAIT_TIMEOUT);
			}
		}

		logDebugMessage("Done with success() method, will now check if Loading is displayed");

		// make sure loading is no longer present
		crmLoad();
	}
	
	/**
	 * created this because sometimes the openSubPanel does not work it may
	 * cause issues to other test cases if I try to resolve the issue in the
	 * openSubPanel method
	 * 
	 * @param subpanelName
	 * @return
	 */
	protected void crmToggleSubpanel(String subpanelName) {

		WebElement subPanel = crmFindSubPanel(subpanelName);
		subPanel.findElement(By.xpath(".//a[@class='btn btn-invisible']"))
				.click();
		logDebugMessage("Toggled the subpanel '" + subpanelName + "'");
	}
	
	
	
}
