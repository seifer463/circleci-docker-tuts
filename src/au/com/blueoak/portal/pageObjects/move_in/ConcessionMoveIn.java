package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ConcessionMoveIn {

	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_concession")
	public WebElement header;
	
	@FindBy(name = "label_concession")
	public WebElement lblConcessionQuestion;

	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_yes']/label/span[1]/input")
	public WebElement addConcessionYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterAddConcessionYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerAddConcessionYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_yes']/label/span[2]")
	public WebElement lblAddConcessionYes;

	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_no']/label/span[1]/input")
	public WebElement addConcessionNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterAddConcessionNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerAddConcessionNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_concession_no']/label/span[2]")
	public WebElement lblAddConcessionNo;
	
	// this is the text displayed when the state does not have concession details
	@FindBy(name = "concession_not_available_text")
	public WebElement concessionNotAvailableText;
	
	@FindBy(xpath = "//mat-form-field[@name='field_concession_card_holder']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCardHolderName;
	
	@FindBy(name = "concession_card_holder")
	public WebElement cardHolderName;
	
	// this is for checking if the element exists or not
	@FindBy(name = "concession_card_holder")
	public List<WebElement> cardHolderNameList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_concession_card_holder']/div/div[2]")
	public WebElement underlineCardHolderName;
	
	@FindBy(name = "hint_concession_card_holder")
	public WebElement hintCardHolderName;
	
	@FindBy(name = "label_card_type")
	public WebElement floaterLblTypeOfConcessionCard;
	
	@FindBy(name = "selected_card_type_number")
	public WebElement typeOfConcessionCard;
	
	// this is for checking if the element exists or not
	@FindBy(name = "selected_card_type_number")
	public List<WebElement> typeOfConcessionCardList;
	
	@FindBy(xpath = "//mat-select[@name='selected_card_type_number']/div/div[1]/span")
	public WebElement lblColorTypeOfConcessionCard;
	
	// these are the options from the Concession Card Type
	@FindBy(name = "selected_card_type_number_option")
	public List<WebElement> typeOfConcessionCardOptions;
	
	@FindBy(xpath = "//mat-form-field[@name='field_card_type']/div/div[2]")
	public WebElement underlineTypeOfConcessionCard;

	@FindBy(name = "hint__card_type_number_option")
	public WebElement hintTypeOfConcessionCard;
	
	// this is for the div where the type of concession cards are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-select-') and @role='listbox']")
	public WebElement typeOfConcessionCardDiv;
	
	@FindBy(xpath = "//mat-form-field[@name='field_card_holder_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCardNumber;
	
	@FindBy(name = "card_holder_number")
	public WebElement cardNumber;
	
	// this is for checking if the field is displayed or not
	@FindBy(name = "card_holder_number")
	public List<WebElement> cardNumberList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_card_holder_number']/div/div[2]")
	public WebElement underlineCardNumber;
	
	@FindBy(name = "hint_card_holder_number")
	public WebElement hintCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_concession_expiry']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblCardNumExpiry;
	
	// this is the Concession Card Number Expiry
	@FindBy(name = "input_concession_expiry")
	public WebElement cardNumExpiry;
	
	// for checking if the field is displayed or not
	@FindBy(name = "input_concession_expiry")
	public List<WebElement> cardNumExpiryList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_concession_expiry']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconCardNumExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='field_concession_expiry']/div/div[2]")
	public WebElement underlineCardNumExpiry;
	
	@FindBy(name = "hint_concession_expiry")
	public WebElement hintCardNumExpiry;
	
	// this is the authorisation text for "method": "UPLOAD"
	@FindBy(name = "concession_authorisation_upload_text")
	public WebElement lblAuthorisationForUpload;
	
	@FindBy(xpath = "//p[@name='concession_authorisation_upload_text']/a")
	public WebElement linkLblAuthorisationUpload;
	
	// this is the authorisation text for "method": "QUESTION"
	@FindBy(name = "concession_authorisation_question_text")
	public WebElement lblAuthorisationForQuestion;
	
	// this is where the text 'Drag-and-drop file here or click to browse for file'
	// is located which we include in the assertion
	@FindBy(xpath = "//app-concession/div/app-uploader/div/div[@id='dropArea']")
	public WebElement dragAndDropText;
	
	// this is the drag and drop section for concession
	@FindBy(xpath = "//ejs-uploader[@name='concession_uploader']/div")
	public WebElement dragAndDropArea;
	
	// this is the drag and drop section for concession
	// for checking if the element exists
	@FindBy(xpath = "//ejs-uploader[@name='concession_uploader']/div")
	public List<WebElement> dragAndDropAreaList;
	
	// this is the list of elements for the files uploaded for concession
	@FindBy(xpath = "//ejs-uploader[@name='concession_uploader']/div/ul[@class='e-upload-files']/li[starts-with(@class,'e-upload-file-list')]")
	public List<WebElement> dragAndDropUploadedFiles;
	
	@FindBy(id = "concession_open_uploader")
	public WebElement linkDragAndDropClickToBrowse;
	
	@FindBy(xpath = "//app-concession//app-uploader/div[starts-with(@class,'uploadfile')]")
	public WebElement dragAndDropBorder;
	
	// this is the error when the max upload files limit is reached
	@FindBy(name = "hint_concession_file_count_exceed")
	public WebElement dragAndDropCountExceedError;
	
	// Yes option for "method": "QUESTION"
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_yes']/label/span[1]/input")
	public WebElement agreeYes;
	
	// Yes option for "method": "QUESTION"
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_yes']/label/span[1]/input")
	public List<WebElement> agreeYesList;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterAgreeYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerAgreeYes;
	
	// No option for "method": "QUESTION"
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_no']/label/span[1]/input")
	public WebElement agreeNo;
	
	// No option for "method": "QUESTION"
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_no']/label/span[1]/input")
	public List<WebElement> agreeNoList;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterAgreeNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_agreement_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerAgreeNo;
	
	@FindBy(xpath = "//mat-dialog-container[starts-with(@id,'mat-dialog-')]")
	public WebElement dialogContainer;
	
	// this is when removing uploaded files
	@FindBy(xpath = "//div[@name='dialog_content']/p")
	public WebElement dialogContainerText;
	
	@FindBy(name = "dialog_button_yes")
	public WebElement dialogRemoveFileYes;
	
	@FindBy(name = "dialog_button_no")
	public WebElement dialogRemoveFileNo;
	
	// this is the previous button
	@FindBy(name = "button_prev_concession")
	public WebElement previous;

	// this is the next button
	@FindBy(name = "button_next_concession")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public ConcessionMoveIn(WebDriver driver) {

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
	public ConcessionMoveIn(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}
	
	
}
