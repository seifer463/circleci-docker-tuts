package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AcceptanceMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_acceptance")
	public WebElement header;
	
	@FindBy(name = "label_acceptance_message")
	public WebElement lblAcceptanceIntro;
	
	@FindBy(name = "acceptance_move_in")
	public WebElement movingInRow;
	
	@FindBy(xpath = "//div[@name='acceptance_move_in']/div[1]/p/b")
	public WebElement movingInRowLbl;
	
	@FindBy(xpath = "//div[@name='acceptance_move_in']/div[2]/p/a")
	public WebElement movingInRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_move_in']/div[3]")
	public WebElement movingInRowData;
	
	@FindBy(name = "acceptance_service_address")
	public WebElement serviceAddressRow;
	
	@FindBy(xpath = "//div[@name='acceptance_service_address']/div[1]/p/b")
	public WebElement serviceAddressRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_service_address']/div[2]/p/a")
	public WebElement serviceAddressRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_service_address']/div[3]")
	public WebElement serviceAddressRowData;
	
	@FindBy(name = "acceptance_life_support")
	public WebElement lifeSupportRow;
	
	@FindBy(name = "acceptance_life_support")
	public List<WebElement> lifeSupportRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_life_support']/div[1]/p/b")
	public WebElement lifeSupportRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_life_support']/div[2]/p/a")
	public WebElement lifeSupportRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_life_support']/div[3]")
	public WebElement lifeSupportRowData;
	
	@FindBy(name = "acceptance_trade_waste")
	public WebElement tradeWasteRow;
	
	// this is for checking if the fields exists or not
	@FindBy(name = "acceptance_trade_waste")
	public List<WebElement> tradeWasteRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_trade_waste']/div[1]/p/b")
	public WebElement tradeWasteRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_trade_waste']/div[2]/p/a")
	public WebElement tradeWasteRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_trade_waste']/div[3]")
	public WebElement tradeWasteRowData;
	
	@FindBy(name = "acceptance_discharge_information")
	public WebElement dischargeInfoRow;
	
	// this is for checking if the element exists or not
	@FindBy(name = "acceptance_discharge_information")
	public List<WebElement> dischargeInfoRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_discharge_information']/div[1]/p/b")
	public WebElement dischargeInfoRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_discharge_information']/div[2]/p/a")
	public WebElement dischargeInfoRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_discharge_information']/div[3]")
	public WebElement dischargeInfoRowData;
	
	@FindBy(name = "acceptance_account_details")
	public WebElement accountDetailsRow;
	
	@FindBy(xpath = "//div[@name='acceptance_account_details']/div[1]/p/b")
	public WebElement accountDetailsRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_account_details']/div[2]/p/a")
	public WebElement accountDetailsRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_account_details']/div[3]")
	public WebElement accountDetailsRowData;
	
	@FindBy(name = "acceptance_main_account")
	public WebElement mainContactRow;
	
	@FindBy(xpath = "//div[@name='acceptance_main_account']/div[1]/p/b")
	public WebElement mainContactRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_main_account']/div[2]/p/a")
	public WebElement mainContactRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_main_account']/div[3]")
	public WebElement mainContactRowData;
	
	@FindBy(name = "acceptance_main_contact_notification")
	public WebElement mainContactNotifRow;
	
	@FindBy(xpath = "//div[@name='acceptance_main_contact_notification']/div[1]/p/b")
	public WebElement mainContactNotifRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_main_contact_notification']/div[2]/p/a")
	public WebElement mainContactNotifRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_main_contact_notification']/div[3]")
	public WebElement mainContactNotifRowData;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]")
	public WebElement addContact1Row;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[1]/p/b")
	public WebElement addContact1Lbl;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[1]/p/b")
	public List<WebElement> addContact1LblList;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[2]/p/a")
	public WebElement addContact1Update;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[2]/p/a")
	public List<WebElement> addContact1UpdateList;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[3]")
	public WebElement addContact1Data;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0']/div[1]/div[3]")
	public List<WebElement> addContact1DataList;
	
	@FindBy(name = "acceptance_add_contact0_notification")
	public WebElement addContact1NotifRow;
	
	@FindBy(name = "acceptance_add_contact0_notification")
	public List<WebElement> addContact1NotifRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0_notification']/div[1]/p/b")
	public WebElement addContact1NotifRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_add_contact0_notification']/div[2]/p/a")
	public WebElement addContact1NotifRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact0_notification']/div[3]")
	public WebElement addContact1NotifRowData;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1']/div[1]")
	public WebElement addContact2Row;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1']/div[1]/div[1]/p/b")
	public WebElement addContact2Lbl;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1']/div[1]/div[2]/p/a")
	public WebElement addContact2Update;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1']/div[1]/div[3]")
	public WebElement addContact2Data;
	
	@FindBy(name = "acceptance_add_contact1_notification")
	public WebElement addContact2NotifRow;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1_notification']/div[1]/p/b")
	public WebElement addContact2NotifRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_add_contact1_notification']/div[2]/p/a")
	public WebElement addContact2NotifRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_add_contact1_notification']/div[3]")
	public WebElement addContact2NotifRowData;
	
	@FindBy(name = "acceptance_postal_address")
	public WebElement postalAddressRow;
	
	// this is for checking if the element exists or not
	@FindBy(name = "acceptance_postal_address")
	public List<WebElement> postalAddressRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_postal_address']/div[1]/p/b")
	public WebElement postalAddressRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_postal_address']/div[2]/p/a")
	public WebElement postalAddressRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_postal_address']/div[3]")
	public WebElement postalAddressRowData;
	
	@FindBy(name = "acceptance_concession")
	public WebElement concessionRow;
	
	// this is for checking if the element exists or not
	@FindBy(name = "acceptance_concession")
	public List<WebElement> concessionRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_concession']/div[1]/p/b")
	public WebElement concessionRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_concession']/div[2]/p/a")
	public WebElement concessionRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_concession']/div[3]")
	public WebElement concessionRowData;
	
	@FindBy(name = "acceptance_property_manager")
	public WebElement propManLettingAgentRow;
	
	@FindBy(name = "acceptance_property_manager")
	public List<WebElement> propManLettingAgentRowList;
	
	@FindBy(xpath = "//div[@name='acceptance_property_manager']/div[1]/p/b")
	public WebElement propManLettingAgentRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_property_manager']/div[2]/p/a")
	public WebElement propManLettingAgentRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_property_manager']/div[3]")
	public WebElement propManLettingAgentRowData;
	
	@FindBy(name = "acceptance_direct_debit")
	public WebElement directDebitRow;
	
	@FindBy(xpath = "//div[@name='acceptance_direct_debit']/div[1]/p/b")
	public WebElement directDebitRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_direct_debit']/div[2]/p/a")
	public WebElement directDebitRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_direct_debit']/div[3]")
	public WebElement directDebitRowData;
	
	@FindBy(name = "acceptance_additional_notes")
	public WebElement additionalNoteRow;
	
	@FindBy(xpath = "//div[@name='acceptance_additional_notes']/div[1]/p/b")
	public WebElement additionalNoteRowLbl;

	@FindBy(xpath = "//div[@name='acceptance_additional_notes']/div[2]/p/a")
	public WebElement additionalNoteRowUpdate;
	
	@FindBy(xpath = "//div[@name='acceptance_additional_notes']/div[3]")
	public WebElement additionalNoteRowData;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/input")
	public WebElement firstCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/input")
	public List<WebElement> firstCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterFirstCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_0']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerFirstCheckbox;
	
	@FindBy(name = "acceptance_text_0")
	public WebElement lblFirstCheckbox;
	
	@FindBy(name = "acceptance_text_0")
	public List<WebElement> lblFirstCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/input")
	public WebElement secondCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/input")
	public List<WebElement> secondCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterSecondCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_1']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerSecondCheckbox;
	
	@FindBy(name = "acceptance_text_1")
	public WebElement lblSecondCheckbox;
	
	@FindBy(name = "acceptance_text_1")
	public List<WebElement> lblSecondCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/input")
	public WebElement thirdCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/input")
	public List<WebElement> thirdCheckboxList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterThirdCheckbox;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_acceptance_2']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerThirdCheckbox;
	
	@FindBy(name = "acceptance_text_2")
	public WebElement lblThirdCheckbox;
	
	@FindBy(name = "acceptance_text_2")
	public List<WebElement> lblThirdCheckboxList;
	
	@FindBy(name = "button_cancel_acceptance")
	public WebElement cancel;
	
	@FindBy(name = "button_prev_acceptance")
	public WebElement previous;
	
	@FindBy(name = "button_submit_acceptance")
	public WebElement submit;
	
	@FindBy(name = "submitting_message")
	public WebElement submittingMessage;
	
	// this is for checking if the element exists or not
	@FindBy(name = "submitting_message")
	public List<WebElement> submittingMessageList;
	
	@FindBy(xpath = "//mat-dialog-container[starts-with(@id,'mat-dialog-')]")
	public WebElement dialogContainer;
	
	// This is the dialog container for checking the text when canceling
	// or the response after the request was submitted
	@FindBy(name = "dialog_content")
	public WebElement dialogContainerText;
	
	// this is the dialog container header
	@FindBy(name = "dialog_response_header")
	public WebElement dialogContainerHeader;
	
	@FindBy(xpath = "//div[@name='dialog_content']/p[2]")
	public WebElement dialogContainerMsg;
	
	@FindBy(name = "dialog_button_ok")
	public WebElement okDialog;
	
	@FindBy(name = "dialog_with_type_button_yes")
	public WebElement yesCancelRequest;
	
	@FindBy(name = "dialog_with_type_button_no")
	public WebElement noCancelRequest;
	
	@FindBy(name = "label_close_message")
	public WebElement closeMessage;
	
	public AcceptanceMoveIn(WebDriver driver) {
		
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
	public AcceptanceMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
