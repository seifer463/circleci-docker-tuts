package au.com.blueoak.portal.pageObjects.make_payment;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class Header {
	
	WebDriver driver;
	
	@FindBy(how = How.TAG_NAME, using = "h1")
	public WebElement head1;
	
	public Header(WebDriver driver) {
		
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

}
