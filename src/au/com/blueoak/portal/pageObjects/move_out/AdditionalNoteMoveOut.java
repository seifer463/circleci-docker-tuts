package au.com.blueoak.portal.pageObjects.move_out;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdditionalNoteMoveOut {
	
	WebDriver driver;
	
	// This is the notes text area
	@FindBy(name = "textarea_notes")
	public WebElement notes;
	
	// this is the counter for the notes
	@FindBy(name = "total_note_length")
	public WebElement noteLength;
	
	// this is if Forwarding Address is not displayed
	@FindBy(name = "button_prev_note")
	public WebElement previous;
	
	@FindBy(name = "button_next_note")
	public WebElement next;
	
	@FindBy(name = "stepper_additional_note")
	public WebElement header;
	
	public AdditionalNoteMoveOut(WebDriver driver) {
		
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
	public AdditionalNoteMoveOut(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
