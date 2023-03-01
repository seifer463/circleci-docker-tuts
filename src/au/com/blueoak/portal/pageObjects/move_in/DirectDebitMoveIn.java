package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DirectDebitMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_direct_debit")
	public WebElement header;
	
	@FindBy(name = "label_direct_debit_setup")
	public WebElement lblSetupDirectDebit;

	@FindBy(xpath = "//mat-radio-button[@name='radio_bank_account']/label/span[1]/input")
	public WebElement bankAccount;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_bank_account']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterBankAccount;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_bank_account']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerBankAccount;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_bank_account']/label/span[2]")
	public WebElement lblBankAccount;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_credit_card']/label/span[1]/input")
	public WebElement creditCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_credit_card']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterCreditCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_credit_card']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerCreditCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_credit_card']/label/span[2]")
	public WebElement lblCreditCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_no_direct_debit']/label/span[1]/input")
	public WebElement noDirectDebit;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_no_direct_debit']/label/span[1]/input")
	public List<WebElement> noDirectDebitList;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_no_direct_debit']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterNoDirectDebit;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_no_direct_debit']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerNoDirectDebit;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_no_direct_debit']/label/span[2]")
	public WebElement lblNoDirectDebit;
	
	// this is the payment declaration for bank account
	@FindBy(name = "label_bank_account_declaration")
	public WebElement lblBankAccountDeclaration;
	
	@FindBy(xpath = "//p[@name='label_bank_account_declaration']/ul/li/a")
	public WebElement linkLblBankAccountDeclaration;
	
	@FindBy(name = "bank_account_name")
	public WebElement bankAccountName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_bank_account_name']/div/div[2]")
	public WebElement underlineBankAccountName;
	
	@FindBy(name = "hint_bank_account_name")
	public WebElement hintBankAccountName;
	
	@FindBy(name = "account_bsb")
	public WebElement accountBSB;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_bsb']/div/div[2]")
	public WebElement underlineAccountBSB;
	
	// this is the hint for the error message
	@FindBy(name = "hint_account_bsb")
	public WebElement hintAccountBSB;
	
	@FindBy(name = "bank_account_number")
	public WebElement accountNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_bank_account_number']/div/div[2]")
	public WebElement underlineAccountNumber;
	
	// this is the hint for the error message
	@FindBy(name = "hint_bank_account_number")
	public WebElement hintAccountNumber;
	
	// this is the tickbox for Bank Account authorization
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized']/label/span[1]/input")
	public WebElement authorisationBankAccount;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized']/label/span[1]/input")
	public List<WebElement> authorisationBankAccountList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterAuthorisationBankAccount;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerAuthorisationBankAccount;
	
	// this is the authorization text for Bank Account
	@FindBy(name = "bank_account_authorisation_text")
	public WebElement lblBankAccountAuthorisation;
	
	@FindBy(name = "bank_account_authorisation_text")
	public List<WebElement> lblBankAccountAuthorisationList;
	
	@FindBy(xpath = "//span[@name='bank_account_authorisation_text']/a")
	public WebElement linkLblBankAccountAuthorisation;
	
	// this is the secured by sectigo image in Bank Account or Credit Card
	@FindBy(name = "image_secure_trust_provider")
	public WebElement imgSectigo;
	
	@FindBy(name = "tl_popupSECDVtLDD")
	public WebElement sectigoIframe;
	
	// this is the pci image in the Credit Card option
	@FindBy(name = "image_pci_seal")
	public WebElement imgPciSeal;
	
	// the submit loading progress bar message when choosing Credit Card
	@FindBy(xpath = "//p[@class='progressBar']")
	public WebElement progressBarText;
	
	// this is when checking the field is not displayed
	@FindBy(xpath = "//p[@class='progressBar']")
	public List<WebElement> progressBarTextList;
	
	// this is the progress bar when choosing credit card
	@FindBy(xpath = "//mat-progress-bar[@role='progressbar']")
	public WebElement progressBar;
	
	// this is when checking the field is not displayed
	@FindBy(xpath = "//mat-progress-bar[@role='progressbar']")
	public List<WebElement> progressBarList;
	
	// this is the declaration text for Credit Card
	@FindBy(name = "label_credit_card_declaration")
	public WebElement lblCreditCardDeclaration;
	
	// this is the declaration text for Credit Card
	// for checking if it exists
	@FindBy(name = "label_credit_card_declaration")
	public List<WebElement> lblCreditCardDeclarationList;
	
	@FindBy(xpath = "//p[@name='label_credit_card_declaration']/a")
	public WebElement linkLblCreditCardDeclaration;
	
	// The name on the credit card field
	// make sure you switch to the mwIframe before accessing this field
	@FindBy(id = "paymentCardName")
	public WebElement creditCardName;
	
	@FindBy(id = "paymentCardNamePlaceholder")
	public WebElement placeholderCreditCardName;
	
	@FindBy(id = "paymentCardName-error")
	public WebElement hintCreditCardName;

	// The name on the credit card number field
	// make sure you switch to the mwIframe before accessing this field
	@FindBy(id = "paymentCardNumberMask")
	public WebElement creditCardNumber;
	
	@FindBy(id = "paymentCardNumberPlaceholder")
	public WebElement placeholderCreditCardNumber;
	
	@FindBy(id = "paymentCardNumber-error")
	public WebElement hintCreditCardNumber;
	
	// The name on the credit card number expiry field
	// make sure you switch to the mwIframe before accessing this field
	@FindBy(id = "paymentCardExpiryMask")
	public WebElement creditCardExpiry;
	
	@FindBy(id = "paymentCardExpiryPlaceholder")
	public WebElement placeholderCreditCardExpiry;
	
	@FindBy(id = "paymentCardExpiry-error")
	public WebElement hintCreditCardExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='field_name_on_card']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblReadOnlyCreditCardName;

	// this is the field when in read only mode
	@FindBy(name = "name_on_card")
	public WebElement readOnlyCreditCardName;
	
	// this is for checking if the element exists or not
	@FindBy(name = "name_on_card")
	public List<WebElement> readOnlyCreditCardNameList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_name_on_card']/div/div[2]")
	public WebElement underlineReadOnlyCreditCardName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_card_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblReadOnlyCreditCardNumber;
	
	// this is the field when in read only mode
	@FindBy(name = "card_number")
	public WebElement readOnlyCreditCardNumber;
	
	// this is for checking if the element exists or not
	@FindBy(name = "card_number")
	public List<WebElement> readOnlyCreditCardNumberList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_card_number']/div/div[2]")
	public WebElement underlineReadOnlyCreditCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_expiry_month_year']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblReadOnlyCreditCardExpiry;
	
	// this is the field when in read only mode
	@FindBy(name = "expiry_month_year")
	public WebElement readOnlyCreditCardExpiry;
	
	// this is for checking if the element exists or not
	@FindBy(name = "expiry_month_year")
	public List<WebElement> readOnlyCreditCardExpiryList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_expiry_month_year']/div/div[2]")
	public WebElement underlineReadOnlyCreditCardExpiry;
	
	@FindBy(xpath = "//button[@name='button_edit_card']/span/mat-icon")
	public WebElement iconChangeCreditCardDetails;
	
	// this is the button to change Credit Card Details
	@FindBy(name = "button_edit_card")
	public WebElement changeCreditCardDetails;
	
	// this is when checking if the element exists in the page or not
	@FindBy(name = "button_edit_card")
	public List<WebElement> changeCreditCardDetailsList;
	
	@FindBy(xpath = "//button[@name='button_cancel_edit_card']/span/mat-icon")
	public WebElement iconCancelCreditCardChange;
	
	// this is the button to cancel the credit card change
	@FindBy(name = "button_cancel_edit_card")
	public WebElement cancelCreditCardChange;
	
	// this is the tickbox for Credit Card authorization
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized_credit_card']/label/span[1]/input")
	public WebElement authorisationCreditCard;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized_credit_card']/label/span[1]/input")
	public List<WebElement> authorisationCreditCardList;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized_credit_card']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterAuthorisationCreditCard;
	
	@FindBy(xpath = "//mat-checkbox[@name='checkbox_is_authorized_credit_card']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerAuthorisationCreditCard;
	
	// this is the authorisation text for Credit Card
	@FindBy(name = "credit_card_authorisation_text")
	public WebElement lblCreditCardAuthorisation;
	
	@FindBy(name = "credit_card_authorisation_text")
	public List<WebElement> lblCreditCardAuthorisationList;
	
	@FindBy(xpath = "//span[@name='credit_card_authorisation_text']/a")
	public WebElement linkLblCreditCardAuthorisation;
	
	// this is the previous button
	@FindBy(name = "button_prev_direct_debit")
	public WebElement previous;
	
	// this is the next button
	@FindBy(name = "button_next_direct_debit")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public DirectDebitMoveIn(WebDriver driver) {

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
	public DirectDebitMoveIn(WebDriver driver, int implicitWaitInSec) {

		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
