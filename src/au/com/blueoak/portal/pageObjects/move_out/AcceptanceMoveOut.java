package au.com.blueoak.portal.pageObjects.move_out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AcceptanceMoveOut {
	
	WebDriver driver;
	
	@FindBy(name = "label_acceptance_message")
	public WebElement acceptanceIntroMsg;
	
	@FindBy(name = "acceptance_moving_out")
	public WebElement movingOut;
	
	@FindBy(name = "acceptance_service_address")
	public WebElement serviceAddress;
	
	@FindBy(name = "acceptance_account_details")
	public WebElement accountDetails;
	
	@FindBy(name = "acceptance_account_contact")
	public WebElement accountContact;
	
	@FindBy(name = "acceptance_final_delivery")
	public WebElement finalBillDelivery;
	
	@FindBy(name = "acceptance_forwarding_address")
	public WebElement forwardingAddress;
	
	// this is for verifying if the element exists or not
	@FindBy(name = "acceptance_forwarding_address")
	public List<WebElement> forwardingAddressList;
	
	@FindBy(name = "acceptance_additional_notes")
	public WebElement additionalNotes;
	
	// this is for the Account created confirmation
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/input")
	public WebElement firstCheckbox;
	
	// for checking if the element exists
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/input")
	public List<WebElement> firstCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/input")
	public WebElement secondCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/input")
	public List<WebElement> secondCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/input")
	public WebElement thirdCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/input")
	public List<WebElement> thirdCheckboxList;
	
	@FindBy(name = "acceptance_text_0")
	public WebElement lblFirstCheckbox;
	
	// for checking if the element exists
	@FindBy(name = "acceptance_text_0")
	public List<WebElement> lblFirstCheckboxList;
	
	@FindBy(name = "acceptance_text_1")
	public WebElement lblSecondCheckbox;
	
	@FindBy(name = "acceptance_text_1")
	public List<WebElement> lblSecondCheckboxList;
	
	@FindBy(name = "acceptance_text_2")
	public WebElement lblThirdCheckbox;
	
	@FindBy(name = "acceptance_text_2")
	public List<WebElement> lblThirdCheckboxList;
	
	// this is when all sections are displayed
	@FindBy(name = "button_cancel_acceptance")
	public WebElement cancel;
	
	@FindBy(name = "button_prev_acceptance")
	public WebElement previous;
	
	@FindBy(name = "button_submit_acceptance")
	public WebElement submit;
	
	// the submit loading progress bar
	@FindBy(xpath = "//p[@class='progressBar']")
	public WebElement progressBar;
	
	// this is when checking if displayed or not
	@FindBy(xpath = "//p[@class='progressBar']")
	public List<WebElement> progressBarList;
	
	// This is the Ok button when it's a success or failed
	@FindBy(name = "dialog_button_ok")
	public WebElement okDialogContainer;
	
	@FindBy(xpath = "//p[@class='close-message']")
	public WebElement closeDialog;
	
	@FindBy(name = "dialog_with_type_button_yes")
	public WebElement yesCancelRequest;
	
	@FindBy(name = "dialog_with_type_button_no")
	public WebElement noCancelRequest;
	
	// this is the dialog container header
	@FindBy(name = "dialog_response_header")
	public WebElement dialogContainerHeader;
	
	// this is the text on the dialog overlay
	// for failed or successful
	// or when canceling the request
	@FindBy(name = "dialog_content")
	public WebElement dialogContainerText;
	
	@FindBy(name = "stepper_acceptance")
	public WebElement header;
	
	public AcceptanceMoveOut(WebDriver driver) {
		
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
	public AcceptanceMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
