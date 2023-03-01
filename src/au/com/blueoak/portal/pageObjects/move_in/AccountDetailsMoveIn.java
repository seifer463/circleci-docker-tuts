package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AccountDetailsMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_account_details")
	public WebElement header;
	
	@FindBy(name = "label_account_type")
	public WebElement lblAccountType;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_residential']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterResidential;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_residential']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerResidential;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_residential']/label/span[2]")
	public WebElement lblResidential;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_residential']/label/span[1]/input")
	public WebElement residential;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_commercial']/label/span[1]//span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterCommercial;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_commercial']/label/span[1]//span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerCommercial;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_commercial']/label/span[2]")
	public WebElement lblCommercial;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_commercial']/label/span[1]/input")
	public WebElement commercial;
		
	@FindBy(name = "label_comercial_entity")
	public WebElement lblCommercialDetails;
	
	@FindBy(xpath = "//mat-form-field[@name='field_abn_details']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAbnOrAcn;
	
	@FindBy(name = "abn_details")
	public WebElement abnOrAcn;
	
	@FindBy(xpath = "//mat-form-field[@name='field_abn_details']/div/div[2]")
	public WebElement underlineAbnOrAcn;
	
	@FindBy(name = "hint_invalid_abn_details")
	public WebElement hintAbnAcnRequired;
	
	@FindBy(name = "hint_not_found")
	public WebElement hintAbnAcnNotFound;
	
	@FindBy(name = "hint_invalid_abn_acn")
	public WebElement hintAbnAcnInvalid;
	
	@FindBy(name = "hint_abn_not_active")
	public WebElement hintAbnAcnCancelled;
	
	@FindBy(name = "hint_required_entity")
	public WebElement hintCompNameRequired;
	
	@FindBy(name = "hint_entity_name")
	public WebElement hintCompNameInvalid;
	
	@FindBy(xpath = "//mat-form-field[@name='field_abn_details']/div/div[1]/div[2]/mat-icon")
	public WebElement iconAbnOrAcn;
	
	@FindBy(xpath = "//mat-form-field[@name='field_entity_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCompanyName;
	
	@FindBy(name = "entity_name")
	public WebElement companyName;
	
	// this is for checking if the element exists 
	@FindBy(name = "entity_name")
	public List<WebElement> companyNameList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_entity_name']/div/div[2]")
	public WebElement underlineCompanyName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_trading_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblTradingName;
	
	@FindBy(name = "trading_name")
	public WebElement tradingName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_trading_name']/div/div[2]")
	public WebElement underlineTradingName;
	
	// this is the spinner when entering ABN/ACN
	@FindBy(xpath = "//mat-spinner[@role='progressbar']")
	public List<WebElement> loadingAbnAcnList;
	
	@FindBy(name = "button_prev_account_details")
	public WebElement previous;
	
	@FindBy(name = "button_next_account_details")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public AccountDetailsMoveIn(WebDriver driver) {
		
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
	public AccountDetailsMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
