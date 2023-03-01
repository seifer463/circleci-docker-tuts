package au.com.blueoak.portal.pageObjects.move_out;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PortalMoveOut {
	
	WebDriver driver;
	
	// this is the body background when standalone
	@FindBy(xpath = "//body[@class='bodyBackground']")
	public WebElement bodyBackground;
	
	@FindBy(name = "footer_block")
	public WebElement footer;
	
	@FindBy(name = "footer_help")
	public WebElement footerText;
	
	@FindBy(xpath = "//p[@name='footer_help']/a[1]")
	public WebElement linkFooterHelpNumber;
	
	@FindBy(xpath = "//p[@name='footer_help']/a[2]")
	public WebElement linkFooterHelpEmail;
	
	public PortalMoveOut(WebDriver driver) {

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
	public PortalMoveOut(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
