package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ManagerHolidayLettingMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_property_manager")
	public WebElement header;
	
	@FindBy(name = "label_property_manager_introduction")
	public WebElement lblPropManHolidayLettingIntro;
	
	@FindBy(xpath = "//mat-form-field[@name='field_agent_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCompanyName;
	
	@FindBy(name = "agent_name")
	public WebElement companyName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_agent_name']/div/div[2]")
	public WebElement underlineCompanyName;
	
	@FindBy(name = "hint_agent_name")
	public WebElement hintCompanyName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_contact_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCompanyContactNum;
	
	@FindBy(name = "letting_contact_number")
	public WebElement companyContactNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_contact_number']/div/div[2]")
	public WebElement underlineCompanyContactNum;
	
	@FindBy(name = "hint_letting_contact_number")
	public WebElement hintCompanyContactNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_property_letting']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCompanyAddress;
	
	@FindBy(name = "address_lookup_property_letting")
	public WebElement companyAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_property_letting']/div/div[1]/div[2]/mat-icon")
	public WebElement iconCompanyAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_property_letting']/div/div[2]")
	public WebElement underlineCompanyAddress;
	
	@FindBy(name = "hint_lookup_postal_address")
	public WebElement hintCompanyAddress;
	
	// this is for the div where the company addresses are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement companyAddressesDiv;
	
	@FindBy(name = "cannot_find_address_property_letting")
	public WebElement cannotFindAdd;
	
	@FindBy(name = "button_manual_address_search_property_letting")
	public WebElement manualAddSearch;
	
	// for checking if the element exists or not
	@FindBy(name = "button_manual_address_search_property_letting")
	public List<WebElement> manualAddSearchList;
	
	@FindBy(name = "button_quick_address_search_property_letting")
	public WebElement quickAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_one']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddress01;
	
	@FindBy(name = "letting_address_one")
	public WebElement address01;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_one']/div/div[2]")
	public WebElement underlineAddress01;
	
	@FindBy(name = "hint_letting_address_one")
	public WebElement hintAddress01;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_two']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddress02;
	
	@FindBy(name = "letting_address_two")
	public WebElement address02;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_two']/div/div[2]")
	public WebElement underlineAddress02;
	
	@FindBy(name = "hint_letting_address_two")
	public WebElement hintAddress02;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_three']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddress03;
	
	@FindBy(name = "letting_address_three")
	public WebElement address03;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_three']/div/div[2]")
	public WebElement underlineAddress03;
	
	@FindBy(name = "hint_letting_address_three")
	public WebElement hintAddress03;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_four']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddress04;
	
	@FindBy(name = "letting_address_four")
	public WebElement address04;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_address_four']/div/div[2]")
	public WebElement underlineAddress04;
	
	@FindBy(name = "hint_letting_address_four")
	public WebElement hintAddress04;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_postal_city_suburb']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCity;
	
	@FindBy(name = "letting_postal_city_suburb")
	public WebElement city;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_postal_city_suburb']/div/div[2]")
	public WebElement underlineCity;
	
	@FindBy(name = "hint_letting_postal_city_suburb")
	public WebElement hintCity;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_state_selected_postal']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblState;
	
	@FindBy(name = "letting_state_selected_postal")
	public WebElement state;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_state_selected_postal']/div/div[2]")
	public WebElement underlineState;
	
	@FindBy(name = "hint_letting_state_selected_postal")
	public WebElement hintState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_postal_code']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblPostCode;
	
	@FindBy(name = "letting_postal_code")
	public WebElement postCode;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_postal_code']/div/div[2]")
	public WebElement underlinePostCode;
	
	@FindBy(name = "hint_letting_postal_code")
	public WebElement hintPostCode;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_search_autocomplete']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCountry;
	
	@FindBy(name = "letting_search_autocomplete")
	public WebElement country;
	
	@FindBy(xpath = "//mat-form-field[@name='field_letting_search_autocomplete']/div/div[2]")
	public WebElement underlineCountry;
	
	@FindBy(name = "hint_letting_search_autocomplete")
	public WebElement hintCountry;
	
	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	@FindBy(name = "button_prev_letting_agent")
	public WebElement previous;
	
	@FindBy(name = "button_next_letting_agent")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public ManagerHolidayLettingMoveIn(WebDriver driver) {
		
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
	public ManagerHolidayLettingMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
