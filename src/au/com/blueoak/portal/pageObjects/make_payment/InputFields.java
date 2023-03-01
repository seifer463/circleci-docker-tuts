package au.com.blueoak.portal.pageObjects.make_payment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class InputFields {
	
	WebDriver driver;
	
	@FindBy(how = How.ID, using = "account_number")
	public WebElement accountNumber;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardName")
	public WebElement cardName;
	
	// this is for checking if the element is displayed or not
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardName")
	public List<WebElement> cardNameList;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardNumberMask")
	public WebElement cardNumber;
	
	// this is for checking if the element is displayed or not
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardNumberMask")
	public List<WebElement> cardNumberList;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardExpiryMask")
	public WebElement cardExpiry;
	
	// this is for checking if the element is displayed or not
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardExpiryMask")
	public List<WebElement> cardExpiryList;
	
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardCSC")
	public WebElement cardCvv;
	
	// this is for checking if the element is displayed or not
	// make sure to enter the iframe of the Merchant Warrior first
	// before calling this WebElement.
	// Otherwise you will not be able to find the element.
	@FindBy(how = How.ID, using = "paymentCardCSC")
	public List<WebElement> cardCvvList;
	
	@FindBy(how = How.ID, using = "payment_amount")
	public WebElement paymentAmount;
	
	// this is for checking if the element is displayed or not
	@FindBy(how = How.ID, using = "payment_amount")
	public List<WebElement> paymentAmountList;
	
	@FindBy(how = How.ID, using = "email_field")
	public WebElement emailReceipt;
	
	// this is for checking if the element is displayed or not
	@FindBy(how = How.ID, using = "email_field")
	public List<WebElement> emailReceiptList;
	
	public InputFields(WebDriver driver) {
		
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
	public InputFields(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
	
}