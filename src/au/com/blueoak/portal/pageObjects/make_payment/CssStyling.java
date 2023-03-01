package au.com.blueoak.portal.pageObjects.make_payment;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class CssStyling {
	
	WebDriver driver;
	
	// for the body background of the payment portal
	@FindBy(how = How.XPATH, using = "//body[@class='bodyBackground']")
	public WebElement bodyBackGrnd;
	
	// for the box shadow border when accessing thru standalone
	@FindBy(how = How.XPATH, using = "//app-root/app-make-payment/div/mat-card")
	public WebElement boxShadow;
	
	@FindBy(how = How.ID, using = "mat-form-field-label-3")
	public WebElement acctNum;
	
	@FindBy(how = How.ID, using = "mat-form-field-label-5")
	public WebElement payAmt;
	
	@FindBy(how = How.ID, using = "mat-form-field-label-7")
	public WebElement emailReceipt;
	
	@FindBy(how = How.XPATH, using = "//*[@id='submit_materials']/mat-form-field/div/div[2]")
	public WebElement underlineAcctNum;
	
	@FindBy(how = How.XPATH, using = "//*[@id='submit_materials']/div/div[2]/mat-form-field[1]/div/div[2]")
	public WebElement underlinePayAmt;
	
	@FindBy(how = How.XPATH, using = "//*[@id='submit_materials']/div/div[2]/mat-form-field[2]/div/div[2]")
	public WebElement underlineEmailReceipt;
	
	@FindBy(how = How.XPATH, using = "//*[@id='large_amount_check_box']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement largeAmtCheckboxOuter;
	
	@FindBy(how = How.XPATH, using = "//*[@id='large_amount_check_box']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement largeAmtCheckboxInner;
	
	@FindBy(how = How.XPATH, using = "//*[@id='email_check_box']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement emailCheckboxOuter;
	
	@FindBy(how = How.XPATH, using = "//*[@id='email_check_box']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement emailCheckboxInner;
	
	@FindBy(how = How.XPATH, using = "//div[@class='processingFeeDiv']")
	public WebElement processingFee;
	
	@FindBy(how = How.XPATH, using = "//div[@class='response-page']/div[@class='icon-container']/mat-label/img")
	public WebElement responseIcon;
	
	@FindBy(how = How.XPATH, using = "//div[starts-with(@class,'footer-body')]")
	public WebElement footer;
	
	@FindBy(how = How.XPATH, using = "//div[starts-with(@class,'footer-body')]/a/img")
	public WebElement clientImg;
	
	// initial progress bar color on the next button
	// when there's no progress yet
	@FindBy(how = How.XPATH, using = "//*[@id='progress_bar1']/div[1]/div[1]")
	public WebElement progBarNext;
	
	// initial progress bar color on the make payment button
	// when there's no progress yet
	@FindBy(how = How.XPATH, using = "//*[@id='progress_bar']/div[1]/div[1]")
	public WebElement progBarPayment;
	
	// this is the color of the spinner when hitting next
	// or hitting the make payment
	@FindBy(how = How.XPATH, using = "//*[name() = 'svg']/*[name()='circle']")
	public WebElement spinner;
	
	public CssStyling(WebDriver driver) {
		
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
	public CssStyling(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
}