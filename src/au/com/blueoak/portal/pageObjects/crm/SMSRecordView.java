package au.com.blueoak.portal.pageObjects.crm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SMSRecordView {
	
	WebDriver driver;
	
	@FindBy(xpath = "//button[@data-action='toggle']")
	public WebElement moreLessLink;
	
	@FindBy(xpath = "//div[@data-name='name']")
	public WebElement subject;
	
	@FindBy(xpath = "//div[@data-name='recipients']")
	public WebElement recipients;
	
	public SMSRecordView(WebDriver driver) {
		
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

}