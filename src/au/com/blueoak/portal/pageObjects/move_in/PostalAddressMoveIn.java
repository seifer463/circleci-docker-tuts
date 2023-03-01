package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PostalAddressMoveIn {

	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_postal_address")
	public WebElement header;
	
	@FindBy(name = "label_postal_address")
	public WebElement lblPostalAddressQuestion;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_yes']/label/span[1]/input")
	public WebElement sameSupAddressYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterSameSupAddressYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerSameSupAddressYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_yes']/label/span[2]")
	public WebElement lblSameSupAddressYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterSameSupAddressNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerSameSupAddressNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_no']/label/span[1]/input")
	public WebElement sameSupAddressNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_same_postal_no']/label/span[2]")
	public WebElement lblSameSupAddressNo;

	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_postal_address']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblPostalAddSearch;
	
	@FindBy(name = "address_lookup_postal_address")
	public WebElement postalAddSearch;
	
	// when checking if the field exists or not
	@FindBy(name = "address_lookup_postal_address")
	public List<WebElement> postalAddSearchList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_postal_address']/div/div[1]/div[2]/mat-icon")
	public WebElement iconPostalAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_lookup_postal_address']/div/div[2]")
	public WebElement underlinePostalAddSearch;
	
	@FindBy(name = "hint_lookup_postal_address")
	public WebElement hintPostalAddSearch;

	// this is for the div where the supply addresses are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement postalAddressesDiv;

	// this is the link for cannot find address
	@FindBy(name = "cannot_find_address_postal_address")
	public WebElement postalAddCantFindAdd;

	// this is the link for the Quick Address Search
	// or the Manual Address Search
	@FindBy(name = "button_quick_address_search_postal_address")
	public WebElement postalAddQuickAddSearch;
	
	// this is the link for the Manual Address Search
	@FindBy(name = "button_manual_address_search_postal_address")
	public WebElement postalAddManualAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_one']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddLine01;

	@FindBy(name = "address_one")
	public WebElement addLine01;
	
	@FindBy(name = "address_one")
	public List<WebElement> addLine01List;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_one']/div/div[2]")
	public WebElement underlineAddLine01;
	
	@FindBy(name = "hint_address_one")
	public WebElement hintAddLine01;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_two']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddLine02;

	@FindBy(name = "address_two")
	public WebElement addLine02;
	
	@FindBy(name = "address_two")
	public List<WebElement> addLine02List;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_two']/div/div[2]")
	public WebElement underlineAddLine02;
	
	@FindBy(name = "hint_address_two")
	public WebElement hintAddLine02;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_three']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddLine03;

	@FindBy(name = "address_three")
	public WebElement addLine03;
	
	@FindBy(name = "address_three")
	public List<WebElement> addLine03List;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_three']/div/div[2]")
	public WebElement underlineAddLine03;
	
	@FindBy(name = "hint_address_three")
	public WebElement hintAddLine03;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_four']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAddLine04;

	@FindBy(name = "address_four")
	public WebElement addLine04;
	
	@FindBy(name = "address_four")
	public List<WebElement> addLine04List;
	
	@FindBy(xpath = "//mat-form-field[@name='field_address_four']/div/div[2]")
	public WebElement underlineAddLine04;
	
	@FindBy(name = "hint_address_three")
	public WebElement hintAddLine04;

	@FindBy(xpath = "//mat-form-field[@name='field_postal_city_suburb']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCity;
	
	@FindBy(name = "postal_city_suburb")
	public WebElement city;
	
	@FindBy(name = "postal_city_suburb")
	public List<WebElement> cityList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_city_suburb']/div/div[2]")
	public WebElement underlineCity;
	
	@FindBy(name = "hint_postal_city_suburb")
	public WebElement hintCity;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_state_selected']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblState;

	@FindBy(name = "postal_state_selected")
	public WebElement state;
	
	@FindBy(name = "postal_state_selected")
	public List<WebElement> stateList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_state_selected']/div/div[2]")
	public WebElement underlineState;
	
	@FindBy(name = "hint_postal_state_selected")
	public WebElement hintState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_code']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblPostcode;

	@FindBy(name = "postal_code")
	public WebElement postcode;
	
	@FindBy(name = "postal_code")
	public List<WebElement> postcodeList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_code']/div/div[2]")
	public WebElement underlinePostcode;
	
	@FindBy(name = "hint_postal_code")
	public WebElement hintPostcode;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_search_autocomplete']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCountry;

	@FindBy(name = "postal_search_autocomplete")
	public WebElement country;
	
	@FindBy(name = "postal_search_autocomplete")
	public List<WebElement> countryList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_postal_search_autocomplete']/div/div[2]")
	public WebElement underlineCountry;
	
	@FindBy(name = "hint_postal_search_autocomplete")
	public WebElement hintCountry;

	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	@FindBy(name = "button_prev_postal_address")
	public WebElement previous;

	@FindBy(name = "button_next_postal_address")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;

	public PostalAddressMoveIn(WebDriver driver) {

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
	public PostalAddressMoveIn(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}