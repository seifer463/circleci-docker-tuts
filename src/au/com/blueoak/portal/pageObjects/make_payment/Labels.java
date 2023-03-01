package au.com.blueoak.portal.pageObjects.make_payment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class Labels {
	
	WebDriver driver;
	
	@FindBy(how = How.XPATH, using = "//label[@id='mat-form-field-label-3']/span")
	public WebElement floaterAcccountNum;
	
	@FindBy(id = "account_number_error")
	public WebElement hintAccountNumber;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardNamePlaceholder")
	public WebElement cardName;
	
	@FindBy(id = "paymentCardName-error")
	public WebElement hintCardName;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardNumberPlaceholder")
	public WebElement cardNumber;
	
	@FindBy(id = "paymentCardNumber-error")
	public WebElement hintCardNumber;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardExpiryPlaceholder")
	public WebElement cardExpiry;
	
	@FindBy(id = "paymentCardExpiry-error")
	public WebElement hintCardExpiry;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardCSCPlaceholder")
	public WebElement cardCvv;
	
	@FindBy(id = "paymentCardCSC-error")
	public WebElement hintCardCvv;
	
	@FindBy(how = How.XPATH, using = "//p[@class='account-sample-number']")
	public WebElement acctNumSample;
	
	@FindBy(how = How.XPATH, using = "//p[@class='footer-help']")
	public WebElement supportText;
	
	// this is for checking if the element exists or not
	@FindBy(how = How.XPATH, using = "//p[@class='footer-help']")
	public List<WebElement> supportTextList;
	
	@FindBy(how = How.ID, using = "processing_fee_message")
	public WebElement payProcessingFee;
	
	@FindBy(xpath = "//p[starts-with(@class,'credit-card-declaration')]")
	public WebElement creditCardDeclaration;
	
	@FindBy(xpath = "//p[starts-with(@class,'credit-card-declaration')]/a")
	public WebElement creditCardDeclarationLink;
	
	// this is for checking if the element is displayed or not
	@FindBy(how = How.ID, using = "processing_fee_message")
	public List<WebElement> payProcessingFeeList;
	
	@FindBy(how = How.XPATH, using = "//label[@id='mat-form-field-label-5']/span")
	public WebElement floaterPaymentAmt;
	
	@FindBy(id = "payment_amount_error")
	public WebElement hintPaymentAmt;
	
	@FindBy(how = How.ID, using = "total_amount")
	public WebElement payTotal;
	
	@FindBy(how = How.XPATH, using = "//p[@class='progressBar']")
	public WebElement paymentProgBar;
	
	// this is for checking if the element is displayed
	@FindBy(how = How.XPATH, using = "//p[@class='progressBar']")
	public List<WebElement> paymentProgBarList;
	
	@FindBy(how = How.ID, using = "progress_secure")
	public WebElement nextProgBar;
	
	// when checking if the element exists
	@FindBy(how = How.ID, using = "progress_secure")
	public List<WebElement> nextProgBarList;
	
	@FindBy(how = How.ID, using = "large_amount_check_box")
	public WebElement largeAmount;
	
	@FindBy(how = How.ID, using = "email_check_box")
	public WebElement email;
	
	@FindBy(how = How.XPATH, using = "//label[@id='mat-form-field-label-7']/span")
	public WebElement floaterEmailAdd;
	
	@FindBy(id = "email_field_error")
	public WebElement hintEmailAdd;
	
	@FindBy(how = How.XPATH, using = "//img[@class='account-number-image']")
	public WebElement imgAcctNum;
	
	@FindBy(how = How.XPATH, using = "//mat-label[@class='response-header']")
	public WebElement responseHeader;
	
	@FindBy(how = How.XPATH, using = "//mat-label[@class='email-sent-to']")
	public WebElement emailSentTo;
	
	// this is for checking if the element exists
	@FindBy(how = How.XPATH, using = "//mat-label[@class='email-sent-to']")
	public List<WebElement> emailSentToList;
	
	@FindBy(how = How.XPATH, using = "//mat-label[@class='error-body']")
	public WebElement declinedMwResp;
	
	@FindBy(how = How.XPATH, using = "//p[@class='footer-help']")
	public WebElement footerHelp;
	
	@FindBy(how = How.XPATH, using = "//div[starts-with(@class,'footer-body')]/a")
	public WebElement clientLink;
	
	@FindBy(how = How.XPATH, using = "//p[@class='footer-help']/a[1]")
	public WebElement footerHelpNumber;
	
	@FindBy(how = How.XPATH, using = "//p[@class='footer-help']/a[2]")
	public WebElement footerHelpEmail;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;

	public Labels(WebDriver driver) {
		
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
	public Labels(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
}
