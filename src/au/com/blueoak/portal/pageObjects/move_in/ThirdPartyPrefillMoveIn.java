package au.com.blueoak.portal.pageObjects.move_in;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ThirdPartyPrefillMoveIn {
	
	WebDriver driver;
	
	@FindBy(id = "move_in_date")
	public WebElement supplyMoveInDate;
	
	@FindBy(id = "addressSearch")
	public WebElement supplyCompleteAdd;
	
	@FindBy(id = "streetNumber")
	public WebElement supplyStreetNum;
	
	@FindBy(id = "streetName")
	public WebElement supplyStreetName;
	
	@FindBy(id = "streetTypeSelected")
	public WebElement supplyStreetTypeShort;
	
	@FindBy(id = "searchStringStreetType")
	public WebElement supplyStreetTypeLong;
	
	@FindBy(id = "citySuburb")
	public WebElement supplyCitySuburb;
	
	@FindBy(id = "stateSelected")
	public WebElement supplyStateShort;
	
	@FindBy(id = "supplyStateSearchString")
	public WebElement supplyStateLong;
	
	@FindBy(id = "post_code")
	public WebElement supplyPostcode;
	
	@FindBy(id = "residential")
	public WebElement accountResidential;
	
	@FindBy(id = "commercial")
	public WebElement accountCommercial;
	
	@FindBy(id = "tenant")
	public WebElement supplyTenant;
	
	@FindBy(id = "owner")
	public WebElement supplyOwner;
	
	@FindBy(id = "property_manager")
	public WebElement supplyPropMan;
	
	@FindBy(xpath = "//input[@type='button' and @value='Submit']")
	public WebElement submit;
	
	public ThirdPartyPrefillMoveIn(WebDriver driver) {

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
	public ThirdPartyPrefillMoveIn(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
