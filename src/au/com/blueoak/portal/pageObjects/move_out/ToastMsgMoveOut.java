package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class ToastMsgMoveOut {
	
	WebDriver driver;
	
	@FindBy(how = How.XPATH, using = "//div[@id='toast-container']/div")
	public WebElement toastLoc;
	
	// this is for checking if the field is displayed or not
	@FindBy(how = How.XPATH, using = "//div[@id='toast-container']/div")
	public List<WebElement> toastLocList;
	
	public ToastMsgMoveOut(WebDriver driver) {

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
	public ToastMsgMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}