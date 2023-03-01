package au.com.blueoak.portal.pageObjects.connection;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ConnectionDetailsConnection {
	
	WebDriver driver;
	
	@FindBy(name = "main_header")
	public WebElement lblMainHeader;
	
	// this is for checking if the element exists
	@FindBy(name = "main_header")
	public List<WebElement> lblMainHeaderList;
	
	@FindBy(name = "connection_introduction")
	public WebElement lblConnectionIntro;
	
	@FindBy(xpath = "//p[@name='connection_introduction']/a")
	public WebElement linkLblConnectionIntro;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_connection_details")
	public WebElement header;
	
	@FindBy(name = "label_submitted_as")
	public WebElement lblSubmittedAsHeader;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_property_owner']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterPropertyOwner;

	@FindBy(xpath = "//mat-radio-button[@name='radio_managing_agent']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterManagingAgent;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_property_owner']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerPropertyOwner;

	@FindBy(xpath = "//mat-radio-button[@name='radio_managing_agent']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerManagingAgent;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_property_owner']/label/span[2]")
	public WebElement lblPropertyOwner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_managing_agent']/label/span[2]")
	public WebElement lblManagingAgent;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_property_owner']/label/span[1]/input")
	public WebElement propertyOwner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_managing_agent']/label/span[1]/input")
	public WebElement managingAgent;
	
	@FindBy(name = "input_settlement_date")
	public WebElement settlementDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerSettlementDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconSettlementDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']/div/div[2]")
	public WebElement underlineSettlementDate;
	
	@FindBy(name = "label_supply_address")
	public WebElement lblConnectionAddHeader;
	
	@FindBy(name = "input_supply_address")
	public WebElement connectionAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_address']/div/div[1]/div[2]/mat-icon")
	public WebElement iconConnectionAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_address']/div/div[2]")
	public WebElement underlineConnectionAddSearch;
	
	@FindBy(name = "label_access_hazard")
	public WebElement lblSiteAccessHzrdHeader;
	
	@FindBy(name = "label_introduction_hazard")
	public WebElement lblSiteAccessHzrdIntro;
	
	// the checkboxes for the Site Access Hazard options
	@FindBy(name = "selected_harard")
	public List<WebElement> siteAccessHzrdOptions;
	
	@FindBy(name = "input_other_hazard")
	public WebElement siteAccessHzrdOtherInput;
	
	@FindBy(xpath = "//mat-form-field[@name='field_other_hazard']/div/div[2]")
	public WebElement underlineSiteAccessHzrdOtherInput;
	
	@FindBy(name = "button_next_connection_details")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public ConnectionDetailsConnection(WebDriver driver) {
		
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
	public ConnectionDetailsConnection(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}