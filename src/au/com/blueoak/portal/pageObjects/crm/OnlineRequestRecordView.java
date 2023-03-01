package au.com.blueoak.portal.pageObjects.crm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class OnlineRequestRecordView {
	
	WebDriver driver;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[1]/h1/span[2]/span/span[2]/span/div")
	public WebElement requestAccountName;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[1]/h1/span[2]/span/span[2]/span/div")
	public List<WebElement> requestAccountNameList;
	
	/** 
	 * Start
	 * 
	 * Elements for Move Out request
	 * */
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[1]/span/span/span[1]/span/span")
	public WebElement moveOutRequestValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[1]/span/span/span[2]/span/span")
	public WebElement moveOutProgressValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[1]/span/span/span[3]/span/span")
	public WebElement moveOutDateRequiredValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[1]/span/span/span[4]/span/span")
	public WebElement moveOutRequestedTenancyValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[1]/span/span/span[1]/span[1]/span")
	public WebElement moveOutAccountTypeValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[1]/span/span/span[2]/span[1]/span")
	public WebElement moveOutForwardingAddressValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[9]/div[1]/span/span")
	public WebElement moveOutDateSubmittedValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[10]/div[1]/span/span")
	public WebElement moveOutDateCreatedValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[1]/span[1]/span")
	public WebElement moveOutContactNameValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[2]/span[1]/span")
	public WebElement moveOutContactBusPhoneValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[3]/span[1]/span")
	public WebElement moveOutContactAhrPhoneValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[4]/span[1]/span")
	public WebElement moveOutContactMobPhoneValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[5]/span[1]/span")
	public WebElement moveOutContactEmailAddValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[6]/span[1]/span")
	public WebElement moveOutContactSecretCodeValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[3]/span[1]/span")
	public WebElement moveOutCompany;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[1]/span[1]/span")
	public WebElement moveOutContactBirthdateValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[2]/span[1]/span")
	public WebElement moveOutContactPersonalIdValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[4]/span[1]/span")
	public WebElement moveOutNotificationsValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[9]/div[2]/span/span")
	public WebElement moveOutDateCompletedValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[10]/div[2]/span/span")
	public WebElement moveOutDateModifiedValue;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[1]/h1/span[2]/span/span[2]/span/div/img")
	public List<WebElement> moveOutTooltipValueAccount;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[1]/span/span/span[1]/span[1]/span/div/img")
	public List<WebElement> moveOutTooltipValueAccountType;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[1]/span[1]/span/div/img")
	public List<WebElement> moveOutTooltipValueContactName;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[2]/div[2]/span/span/span[6]/span[1]/span/div/img")
	public List<WebElement> moveOutTooltipValueContactSecretCode;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[3]/span[1]/span/div[1]/span/img")
	public List<WebElement> moveOutTooltipValueCompany;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[1]/span[1]/span/div/img")
	public List<WebElement> moveOutTooltipValueContactBirthDate;
	
	@FindBy(xpath = "//*[@id='record-online-request']/div/div[2]/div[5]/div[2]/span/span/span[2]/span[1]/span/div/img")
	public List<WebElement> moveOutTooltipValueContactPersonalId;
	/** 
	 * End
	 * 
	 * Elements for Move Out request
	 * */
	
	/** 
	 * Start
	 * 
	 * Elements for Move In request
	 * */
	@FindBy(xpath = "//span[@data-fieldname='record_request']/span[@class='detail']")
	public WebElement moveInRequestValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_progress']/span[@class='detail']")
	public WebElement moveInProgressValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_date_required']/span[@class='detail']")
	public WebElement moveInDateRequiredValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_requested_tenancy']/span[@class='detail']")
	public WebElement moveInRequestedTenancyValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_supply_state']/span[@class='detail']")
	public WebElement moveInSupplyStateReportedByCust;
	
	@FindBy(xpath = "//span[@data-fieldname='record_active_life_support']/span[@class='detail']")
	public WebElement moveInActiveLifeSupport;
	
	@FindBy(xpath = "//span[@data-fieldname='record_account_type_vip']/span[@class='detail']")
	public WebElement moveInAccountTypeValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_account_category']/span[@class='detail']")
	public WebElement moveInAccountCategoryValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_postal_address']/span[@class='detail']")
	public WebElement moveInPostalAddValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_direct_debit']/span[@class='detail']")
	public WebElement moveInDirectDebitValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_reference_account']/span[@class='detail']")
	public WebElement moveInReferenceAcctValue;
	
	@FindBy(xpath = "//div[@data-type='datetimecombo-date-submitted']/span/span[@class='detail']")
	public WebElement moveInDateSubmittedValue;
	
	@FindBy(xpath = "//div[@data-type='fieldset-date-entered']/span/span[@class='detail']")
	public WebElement moveInDateCreatedValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_name']/span[@class='detail']")
	public WebElement moveInContactNameValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_business_phone']/span[@class='detail']")
	public WebElement moveInContactBusPhoneValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_home_phone']/span[@class='detail']")
	public WebElement moveInContactAfterHrsPhoneValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_mobile']/span[@class='detail']")
	public WebElement moveInContactMobPhoneValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_email_address']/span[@class='detail']")
	public WebElement moveInContactEmailAddValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_secret_code']/span[@class='detail']")
	public WebElement moveInContactSecretCodeValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_company']/span[@class='detail']")
	public WebElement moveInCompany;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_birthdate']/span[@class='detail']")
	public WebElement moveInContactBirthdateValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_contact_personal_identifications']/span[@class='detail']")
	public WebElement moveInContactPersonalIDValue;
	
	@FindBy(xpath = "//span[@data-fieldname='record_notification_settings']/span[@class='detail']")
	public WebElement moveInNotificationsValue;
	
	@FindBy(xpath = "//div[@data-type='fieldset-date-completed']/span/span[@class='detail']")
	public WebElement moveInDateCompletedValue;
	
	@FindBy(xpath = "//div[@data-type='fieldset-date-modified']/span/span[@class='detail']")
	public WebElement moveInDateModifiedValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//a[@name='close_button']")
	public WebElement moveInAddContactCloseBtn;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//a[@name='edit_button']")
	public WebElement moveInAddContactEditBtn;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='full_name']/span[@class='detail']")
	public WebElement moveInAddContactName;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='existing_contact_name']/span[@class='detail']/div")
	public WebElement moveInAddContactExistingContactName;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='phone_business']/span[@class='detail']")
	public WebElement moveInAddContactBusPhoneValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[1]/div[2]")
	public WebElement moveInAddContactAfterHrsPhoneValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[1]/div[2]")
	public List<WebElement> moveInAddContactAfterHrsPhoneValueList;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[2]/div[2]")
	public WebElement moveInAddContactMobPhoneValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[2]/div[2]")
	public List<WebElement> moveInAddContactMobPhoneValueList;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[3]/div[2]")
	public WebElement moveInAddContactSecretCodeValue;
	
	// this is when checking if the element exists or not
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_left']/span[@class='column-left detail']/span[3]/div[2]")
	public List<WebElement> moveInAddContactSecretCodeValueList;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='date_of_birth']/span[@class='detail']")
	public WebElement moveInAddContactBirthdateValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='personal_id_collection']/span[@class='detail']")
	public WebElement moveInAddContactPersonalIDValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='date_modified_by']/span[@class='detail']")
	public WebElement moveInAddContactDateModifiedByValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='date_entered_by']/span[@class='detail']")
	public WebElement moveInAddContactDateCreatedByValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='email_address']/span[@class='detail']")
	public WebElement moveInAddContactEmailAddValue;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//span[@data-fieldname='column_right']/span[@class='column-right detail']")
	public WebElement moveInAddContactNotifications;
	
	@FindBy(xpath = "//div[@class='drawer transition active']//button[starts-with(@class,'btn-link btn-invisible more')]")
	public WebElement moveInAddContactShowMoreLink;

	@FindBy(xpath = "//div[@class='drawer transition active']//button[starts-with(@class,'btn-link btn-invisible less')]")
	public WebElement moveInAddContactShowLessLink;
	/** 
	 * End
	 * 
	 * Elements for Move In request
	 * */
	
	@FindBy(xpath = "//button[starts-with(@class,'btn-link btn-invisible more')]")
	public WebElement showMoreLink;
	
	@FindBy(xpath = "//button[starts-with(@class,'btn-link btn-invisible less')]")
	public WebElement showLessLink;
	
	@FindBy(xpath = "//*[@id='preview-online-request']/div/div[5]/div[2]/span/div/button")
	public WebElement notesDescriptionMoreLessLink;
	
	public OnlineRequestRecordView(WebDriver driver) {
		
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}
	
	/**
	 * 
	 * @param driver
	 * @param implicitWait define this to specify the wait time to look for the
	 *                     element. Useful when you are trying to verify if an
	 *                     element exists or not on the page.
	 */
	public OnlineRequestRecordView(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
