package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PortalMoveIn {
	
	WebDriver driver;
	
	@FindBy(name = "label_api_header")
	public WebElement apiHeader;
	
	@FindBy(name = "label_response_error")
	public WebElement responseError;
	
	// the background when you delete an uploaded file or contact
	@FindBy(xpath = "//div[@class='cdk-overlay-container']/div[starts-with(@class,'cdk-overlay-backdrop')]")
	public WebElement overlayBackdrop;
	
	// this is the body background when standalone
	@FindBy(xpath = "//body[@class='bodyBackground']")
	public WebElement bodyBackground;
	
	// for the box shadow border when accessing thru standalone
	@FindBy(xpath = "//app-root/div/app-move-in-form/div[starts-with(@class,'container')]/mat-card")
	public WebElement boxShadow;
	
	@FindBy(name = "footer_block")
	public WebElement footer;
	
	@FindBy(name = "footer-help")
	public WebElement footerText;
	
	@FindBy(xpath = "//p[@name='footer-help']/a[1]")
	public WebElement linkFooterHelpNumber;
	
	@FindBy(xpath = "//p[@name='footer-help']/a[2]")
	public WebElement linkFooterHelpEmail;
	
	@FindBy(name = "try_again")
	public WebElement tryAgain;
	
	// this is the color of the spinner
	@FindBy(xpath = "//*[name() = 'svg']/*[name()='circle']")
	public WebElement spinner;
	
	@FindBy(xpath = "//*[name() = 'svg']/*[name()='circle']")
	public List<WebElement> spinnerList;

	public PortalMoveIn(WebDriver driver) {

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
	public PortalMoveIn(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
}
