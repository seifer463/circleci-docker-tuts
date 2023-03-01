package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SupplyDetailsMoveOut {
	
	WebDriver driver;
	
	@FindBy(name = "main_header")
	public WebElement lblMainHeader;
	
	// this is for checking if the element exists
	@FindBy(name = "main_header")
	public List<WebElement> lblMainHeaderList;
	
	@FindBy(name = "move_out_introduction")
	public WebElement lblSupplyDetailsIntro;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// This is the input field for the Move Out Date
	@FindBy(name = "moveDate")
	public WebElement moveOutDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_move_out_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerMoveOutDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_move_out_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconMoveOutDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_move_out_date']/div/div[2]")
	public WebElement underlineMoveOutDate;
	
	@FindBy(name = "label_supply_address")
	public WebElement lblSupplyAddHeader;
	
	@FindBy(name = "input_supply_address")
	public WebElement supplyAddSearch;
	
	// for checking if the field exists or not
	@FindBy(name = "input_supply_address")
	public List<WebElement> supplyAddSearchList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_address']/div/div[1]/div[2]/mat-icon")
	public WebElement iconSupplyAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_address']/div/div[2]")
	public WebElement underlineSupplyAddSearch;
	
	@FindBy(name = "input_complex_name")
	public WebElement supplyAddComplexName;
	
	@FindBy(name = "search_string_tenancy_type")
	public WebElement supplyAddTenancyType;
	
	@FindBy(name = "input_unit_number")
	public WebElement supplyAddTenancyNum;
	
	@FindBy(name = "input_street_number")
	public WebElement supplyAddStreetNum;
	
	@FindBy(name = "input_street_name")
	public WebElement supplyAddStreetName;
	
	@FindBy(name = "input_street_type")
	public WebElement supplyAddStreetType;
	
	@FindBy(name = "input_suburb")
	public WebElement supplyAddCity;
	
	@FindBy(name = "input_state")
	public WebElement supplyAddState;
	
	// this is the spinner icon displayed in the supply state
	// that looks for the public holidays
	// to check if it's displayed or not
	@FindBy(xpath = "//mat-spinner[@role='progressbar']")
	public List<WebElement> supplyAddStateSpinnerList;
	
	@FindBy(name = "input_postal_code")
	public WebElement supplyAddPostcode;
	
	@FindBy(name = "button_quick_address_search_supply_details")
	public WebElement supplyAddQuickAddSearch;
	
	// for checking if field is displayed or not
	@FindBy(name = "button_quick_address_search_supply_details")
	public List<WebElement> supplyAddQuickAddSearchList;
	
	// This is the location of all the labels for checking the error state
	// for all input fields
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	// this is the link for cannot find address
	@FindBy(name = "cannot_find_address_supply_details")
	public WebElement supplyAddCantFindAdd;
	
	// this is for the div where the supply addresses are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement supplyAddressesDiv;
	
	@FindBy(name = "button_next_supply_details")
	public WebElement next;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_supply_details")
	public WebElement header;
	
	public SupplyDetailsMoveOut(WebDriver driver) {
		
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
	public SupplyDetailsMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}