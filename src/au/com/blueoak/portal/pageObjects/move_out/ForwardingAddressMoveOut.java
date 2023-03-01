package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ForwardingAddressMoveOut {
	
	WebDriver driver;
	
	@FindBy(name = "postal_address_text")
	public WebElement forwAddressIntro;
	
	// This is the Forwarding Address
	@FindBy(name = "input_postal_address")
	public WebElement forwAddress;
	
	// This is for checking if displayed
	@FindBy(name = "input_postal_address")
	public List<WebElement> forwAddressList;
	
	// this is for the div where the forwarding addresses are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement forwAddressesDiv;
	
	// this is the link for cannot find address
	@FindBy(xpath = "//mat-option[@name='cannot_find_address_postal']")
	public WebElement forwAddCantFindAdd;
	
	@FindBy(name = "button_prev_postal_address")
	public WebElement previous;
	
	@FindBy(name = "button_next_postal_address")
	public WebElement next;
	
	@FindBy(name = "input_address_one")
	public WebElement addLine01;
	
	@FindBy(name = "input_address_two")
	public WebElement addLine02;
	
	@FindBy(name = "input_address_three")
	public WebElement addLine03;
	
	@FindBy(name = "input_address_four")
	public WebElement addLine04;
	
	@FindBy(name = "input_postal_suburb")
	public WebElement city;
	
	@FindBy(name = "input_postal_state")
	public WebElement state;
	
	@FindBy(name = "input_postal_postcode")
	public WebElement postcode;
	
	@FindBy(name = "input_postal_country")
	public WebElement country;
	
	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	@FindBy(name = "button_quick_address_search_postal")
	public WebElement quickAddSearch;
	
	// this is for checking if the element is displayed or not
	@FindBy(name = "button_quick_address_search_postal")
	public List<WebElement> quickAddSearchList;
	
	// This is the location of all the labels for checking the error state
	// for all input fields
	@FindBy(xpath = "//mat-vertical-stepper[@role='tablist']/div[4]//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_postal_address")
	public WebElement header;
	
	public ForwardingAddressMoveOut(WebDriver driver) {
		
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
	public ForwardingAddressMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}