package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdditionalNoteMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_additional_note")
	public WebElement header;
	
	@FindBy(name = "label_additional_note")
	public WebElement lblEnterNotes;
	
	@FindBy(name = "textarea_note")
	public WebElement notesArea;
	
	// this is for checking if the element exists or not
	@FindBy(name = "textarea_note")
	public List<WebElement> notesAreaList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_note']/div/div[2]")
	public WebElement underlineNotesArea;
	
	@FindBy(name = "total_note_length")
	public WebElement notesLengthCounter;
	
	@FindBy(name = "button_prev_note")
	public WebElement previous;
	
	@FindBy(name = "button_next_note")
	public WebElement next;
	
	public AdditionalNoteMoveIn(WebDriver driver) {
		
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
	public AdditionalNoteMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
