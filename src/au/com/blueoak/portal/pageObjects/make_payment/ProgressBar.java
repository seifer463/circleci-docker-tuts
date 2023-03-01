package au.com.blueoak.portal.pageObjects.make_payment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class ProgressBar {

	WebDriver driver;

	@FindBy(how = How.ID, using = "progress_bar1")
	public WebElement next;
	
	// this is for checking if the element is displayed or not
	@FindBy(how = How.ID, using = "progress_bar1")
	public List<WebElement> nextList;

	@FindBy(how = How.ID, using = "progress_bar")
	public WebElement payment;
	
	// this is for checking if the element is displayed or not
	@FindBy(how = How.ID, using = "progress_bar")
	public List<WebElement> paymentList;

	public ProgressBar(WebDriver driver) {

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
	public ProgressBar(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}