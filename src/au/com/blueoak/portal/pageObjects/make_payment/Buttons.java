package au.com.blueoak.portal.pageObjects.make_payment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class Buttons {
	
	WebDriver driver;
	
	// for the next button when only account number is displayed
	@FindBy(how = How.ID, using = "next_button")
	public WebElement next;
	
	// this is for checking if the element is displayed or not
	// for the next button when only account number is displayed
	@FindBy(how = How.ID, using = "next_button")
	public List<WebElement> nextList;
	
	// for the help icon button
	@FindBy(how = How.ID, using = "help_icon")
	public WebElement helpIcon;
	
	// button to close the help page
	@FindBy(how = How.ID, using = "help_close")
	public WebElement helpIconClose;
	
	// this is for checking if the element exists or not
	@FindBy(how = How.ID, using = "help_close")
	public List<WebElement> helpIconCloseList;
	
	// checkbox for the large payment amount
	@FindBy(how = How.ID, using = "large_amount_check_box-input")
	public WebElement largeAmtCheckbox;
	
	// checkbox for the email receipt
	@FindBy(how = How.ID, using = "email_check_box-input")
	public WebElement emailCheckbox;
	
	// make payment submit button
	@FindBy(how = How.ID, using = "button_submit")
	public WebElement makePayment;
	
	// this is for checking if the field is displayed
	@FindBy(how = How.ID, using = "button_submit")
	public List<WebElement> makePaymentList;
	
	// this is for checking if the field is displayed
	@FindBy(how = How.ID, using = "large_amount_check_box-input")
	public List<WebElement> largeAmtCheckboxList;
	
	// try again when something went wrong when processing payment
	@FindBy(how = How.XPATH, using = "//button[starts-with(@class,'mat-focus-indicator try-again mat-flat-button')]")
	public WebElement tryAgain;
	
	// when checking if the element exists
	@FindBy(how = How.XPATH, using = "//button[starts-with(@class,'mat-focus-indicator try-again mat-flat-button')]")
	public List<WebElement> tryAgainList;
	
	public Buttons(WebDriver driver) {
		
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}
	
	/**
	 * 
	 * @param driver
	 * @param implicitWait define this to specify the wait time to look for the
	 *                     element. Useful when you are trying to verify if an
	 *                     element exists on the page.
	 */
	public Buttons(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
}
