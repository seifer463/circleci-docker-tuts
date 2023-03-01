package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AccountContactMoveOut {
	
	WebDriver driver;
	
	// This is the First name
	@FindBy(name = "input_first_name")
	public WebElement firstName;
	
	@FindBy(name = "input_last_name")
	public WebElement lastName;
	
	@FindBy(name = "input_date_of_birth")
	public WebElement dateOfBirth;
	
	// use this to verify when checking if it's exists or not
	@FindBy(name = "input_date_of_birth")
	public List<WebElement> dateOfBirthList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerDateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconDateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth']/div/div[2]")
	public WebElement underlineDateOfBirth;
	
	// this is the label header for personal identification
	@FindBy(name = "label_radio_group_personal_id")
	public WebElement personalIdHeader;
	
	// this is for checking if the fields exists or not
	@FindBy(name = "label_radio_group_personal_id")
	public List<WebElement> personalIdHeaderList;
	
	// this is for Australian Drivers Licence
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='drivers_license']")
	public WebElement driversLicence;
	
	// use this to verify when checking if it's exists or not
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='drivers_license']")
	public List<WebElement> driversLicenceList;
	
	@FindBy(xpath = "//mat-radio-button[@name='drivers_licence_identification']/label/span[2]")
	public WebElement lblDriversLicence;
	
	@FindBy(name = "input_drivers_license_name")
	public WebElement driversLicenceNumber;
	
	@FindBy(name = "input_license_state")
	public WebElement driversLicenceState;
	
	// this is for Passport
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='passport']")
	public WebElement passport;
	
	// use this to verify when checking if it's exists or not
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='passport']")
	public List<WebElement> passportList;
	
	@FindBy(xpath = "//mat-radio-button[@name='passport_identification']/label/span[2]")
	public WebElement lblPassport;
	
	@FindBy(name = "input_passport_number")
	public WebElement passportNumber;
	
	@FindBy(name = "input_passport_country")
	public WebElement passportCountry;
	
	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	// this is for Medicare Card
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='medicare']")
	public WebElement medicareCard;
	
	// use this to verify when checking if it's exists or not
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='medicare']")
	public List<WebElement> medicareCardList;
	
	@FindBy(xpath = "//mat-radio-button[@name='medicare_identification']/label/span[2]")
	public WebElement lblMedicareCard;
	
	@FindBy(name = "input_medical_card_number")
	public WebElement medicareCardNumber;
	
	@FindBy(name = "input_medicare_expiry")
	public WebElement medicareCardExpiry;
	
	// this is for Provided none option
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='none']")
	public WebElement provideNone;
	
	// this is for Provided none option
	@FindBy(xpath = "//input[@name='radio_group_personal_id' and @value='none']")
	public List<WebElement> provideNoneList;
	
	@FindBy(xpath = "//mat-radio-button[@name='none_identification']/label/span[2]")
	public WebElement lblProvideNone;
	
	@FindBy(name = "label_notification")
	public WebElement lblBillDeliveryHeader;
	
	@FindBy(name = "notofications_introduction")
	public WebElement lblBillDeliveryIntro;
	
	// this is for the Postal checkbox
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_postal_notifications']/label/span[1]/input")
	public WebElement postalNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_postal_notifications']/label/span[2]")
	public WebElement lblPostalNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_email_notifications']/label/span[1]/input")
	public WebElement emailNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_email_notifications']/label/span[2]")
	public WebElement lblEmailNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_sms_notifications']/label/span[1]/input")
	public WebElement smsNotif;
	
	// when checking that the element does not exists
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_sms_notifications']/label/span[1]/input")
	public List<WebElement> smsNotifList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_sms_notifications']/label/span[2]")
	public WebElement lblSMSNotif;
	
	@FindBy(name = "input_email")
	public WebElement emailAddress;
	
	@FindBy(name = "input_phone")
	public WebElement mobilePhone;
	
	@FindBy(name = "input_business_number")
	public WebElement businessPhone;
	
	@FindBy(name = "input_after_home_number")
	public WebElement afterhoursPhone;
	
	@FindBy(name = "input_secret_code")
	public WebElement contactSecretCode;
	
	// this is for checking if the field exists or not
	@FindBy(name = "input_secret_code")
	public List<WebElement> contactSecretCodeList;
	
	// This is the location of all the labels for checking the error state
	// for all input fields
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
		
	// previous button if residential
	@FindBy(name = "button_prev_account_contact")
	public WebElement previous;
	
	// next button if residential
	@FindBy(name = "button_next_account_contact")
	public WebElement next;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_account_contact")
	public WebElement header;
	
	public AccountContactMoveOut(WebDriver driver) {
		
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
	public AccountContactMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
