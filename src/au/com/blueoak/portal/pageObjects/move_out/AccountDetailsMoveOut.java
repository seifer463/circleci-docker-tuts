package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AccountDetailsMoveOut {
	
	WebDriver driver;
	
	// This is the Account Number
	@FindBy(name = "input_account_number")
	public WebElement accountNum;
	
	// This is the Account Number for checking if exists or not
	@FindBy(name = "input_account_number")
	public List<WebElement> accountNumList;
	
	// this is for the warning message on the account number
	@FindBy(name = "hint_aacount_number_warning")
	public List<WebElement> accountNumMsgWarning;
	
	// this is for the warning message on the account number
	@FindBy(name = "hint_account_number")
	public List<WebElement> accountNumMsgError;
	
	// This is the Residential Radio button
	@FindBy(xpath = "//input[@name='radio_group_account_type' and @value='residential']")
	public WebElement residential;
	
	// This is the Commercial Radio button
	@FindBy(xpath = "//input[@name='radio_group_account_type' and @value='commercial']")
	public WebElement commercial;
	
	@FindBy(xpath = "//mat-radio-button[@name='redidental_account_type']/label/span[1]/span[2]")
	public WebElement lblResidential;
	
	@FindBy(xpath = "//mat-radio-button[@name='commercial_account_type']/label/span[1]/span[2]")
	public WebElement lblCommercial;
	
	@FindBy(name = "input_abn_acn")
	public WebElement abnOrAcn;
	
	@FindBy(name = "input_entity_name")
	public WebElement companyName;
	
	@FindBy(name = "input_trading_name")
	public WebElement tradingName;
	
	// This is the location of all the labels for checking the error state
	// for all input fields
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	@FindBy(name = "button_prev_account_details")
	public WebElement previous;
	
	@FindBy(name = "button_next_account_details")
	public WebElement next;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_account_details")
	public WebElement header;
	
	public AccountDetailsMoveOut(WebDriver driver) {
		
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
	public AccountDetailsMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
