package au.com.blueoak.portal.pageObjects.customer_portal;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginCustomer {
	
	WebDriver driver;
	
	@FindBy(name = "login_logo")
	public WebElement loginLogo;

	@FindBy(name = "login_title")
	public WebElement lblLoginTitle;
	
	@FindBy(id = "email_address")
	public WebElement emailAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='login_field_email']/div/div[2]")
	public WebElement underlineEmailAddress;
	
	@FindBy(id = "password")
	public WebElement password;
	
	@FindBy(xpath = "//mat-form-field[@name='login_field_password']/div/div[2]")
	public WebElement underlinePassword;
	
	@FindBy(name = "login_show_password")
	public WebElement showPasswordIcon;
	
	@FindBy(name = "log_in")
	public WebElement loginBtn;
	
	@FindBy(name = "link_reset_password")
	public WebElement resetPasswordLink;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public LoginCustomer(WebDriver driver) {
		
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
	public LoginCustomer(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
}
