package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class MainAccountContactMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_account_contact_0")
	public WebElement header;
	
	@FindBy(xpath = "//app-account-contact/form[starts-with(@class,'move-in-step-form ng-untouched')]")
	public WebElement wholeForm;
	
	@FindBy(name = "label_responsible_contact_for_paying")
	public WebElement lblResponsibleForPaying;
	
	// this is for checking if the element exists or not
	@FindBy(name = "label_responsible_contact_for_paying")
	public List<WebElement> lblResponsibleForPayingList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_first_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblFirstName;
	
	@FindBy(name = "account_contact_0_first_name")
	public WebElement firstName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_first_name']/div/div[2]")
	public WebElement underlineFirstName;
	
	@FindBy(name = "hint_account_contact_0_first_name")
	public WebElement hintFirstName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_last_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblLastName;
	
	@FindBy(name = "account_contact_0_last_name")
	public WebElement lastName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_last_name']/div/div[2]")
	public WebElement underlineLastName;
	
	@FindBy(name = "hint_account_contact_0_last_name")
	public WebElement hintLastName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_0']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblDateOfBirth;
	
	@FindBy(name = "input_date_of_birth_account_contact_0")
	public WebElement dateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_0']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerDateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_0']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconDateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_0']/div/div[2]")
	public WebElement underlineDateOfBirth;
	
	@FindBy(name = "hint_date_of_birth_account_contact_0")
	public WebElement hintDateOfBirth;
	
	@FindBy(name = "account_contact_0_label_personal_identification")
	public WebElement lblPersonalIDHeader;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_driver_license']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_driver_license']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_driver_license']/label/span[1]/input")
	public WebElement driversLicence;
	
	// this is for checking if the field exists
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_driver_license']/label/span[1]/input")
	public List<WebElement> driversLicenceList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_driver_license']/label/span[2]")
	public WebElement lblDriversLicence;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_drivers_license_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblDriversLicenceNumber;
	
	@FindBy(name = "account_contact_0_drivers_license_number")
	public WebElement driversLicenceNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_drivers_license_number']/div/div[2]")
	public WebElement underlineDriversLicenceNumber;
	
	@FindBy(name = "hint_account_contact_0_drivers_license_number")
	public WebElement hintDriversLicenceNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_drivers_state']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblDriversLicenceState;
	
	@FindBy(name = "account_contact_0_drivers_state")
	public WebElement driversLicenceState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_drivers_state']/div/div[2]")
	public WebElement underlineDriversLicenceState;
	
	@FindBy(name = "hint_account_contact_0_drivers_state")
	public WebElement hintDriversLicenceState;
	
	// this is for the div where the state are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement statesDiv;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_passport']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_passport']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_passport']/label/span[1]/input")
	public WebElement passport;
	
	// this for checking if the element exists
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_passport']/label/span[1]/input")
	public List<WebElement> passportList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_passport']/label/span[2]")
	public WebElement lblPassport;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_contact_passport']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblPassportNumber;
	
	@FindBy(name = "account_contact_0_passport")
	public WebElement passportNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_contact_passport']/div/div[2]")
	public WebElement underlinePassportNumber;
	
	@FindBy(name = "hint_account_contact_0_passport")
	public WebElement hintPassportNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_passport_country']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblPassportCountry;
	
	@FindBy(name = "account_contact_0_passport_country")
	public WebElement passportCountry;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_passport_country']/div/div[2]")
	public WebElement underlinePassportCountry;
	
	@FindBy(name = "hint_account_contact_0_passport_country")
	public WebElement hintPassportCountry;
	
	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_medical']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_medical']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_medical']/label/span[1]/input")
	public WebElement medicareCard;
	
	// this is for checking if element exists
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_medical']/label/span[1]/input")
	public List<WebElement> medicareCardList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_medical']/label/span[2]")
	public WebElement lblMedicareCard;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_mediare_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMedicareCardNumber;
	
	@FindBy(name = "account_contact_0_mediare_number")
	public WebElement medicareCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_mediare_number']/div/div[2]")
	public WebElement underlineMedicareCardNumber;
	
	@FindBy(name = "hint_account_contact_0_mediare_number")
	public WebElement hintMedicareCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_0_medicare_main']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMedicareCardExpiry;
	
	@FindBy(name = "input_account_contact_0_medicare_main")
	public WebElement medicareCardExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_0_medicare_main']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconMedicareCardExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_0_medicare_main']/div/div[2]")
	public WebElement underlineMedicareCardExpiry;
	
	@FindBy(name = "hint_account_contact_0_medicare_main")
	public WebElement hintMedicareCardExpiry;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_none_identification']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterProvideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_none_identification']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerProvideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_none_identification']/label/span[2]")
	public WebElement lblProvideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_none_identification']/label/span[1]/input")
	public WebElement provideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_0_radio_none_identification']/label/span[1]/input")
	public List<WebElement> provideNoneList;
	
	// this is the div tag or the container for the whole notification area
	@FindBy(name = "account_contact_0_notification")
	public WebElement notificationContainer;
	
	@FindBy(name = "account_contact_0_notified_label")
	public WebElement lblNotificationHeader;
	
	@FindBy(name = "account_contact_0_notification_introduction")
	public WebElement lblNotificationIntro;
	
	@FindBy(xpath = "//p[@name='account_contact_0_notification_introduction']/p/a")
	public WebElement linkLblNotificationIntro;
	
	@FindBy(name = "account_contact_0_postal_header")
	public WebElement lblPostalNotifHeader;
	
	@FindBy(name = "account_contact_0_email_header")
	public WebElement lblEmailNotifHeader;
	
	@FindBy(name = "account_contact_0_sms_header")
	public WebElement lblSMSNotifHeader;
	
	// this is the label for the notification tooltip icon
	@FindBy(name = "account_contact_0_bills_tooltip")
	public WebElement billsNotifTooltipIcon;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	@FindBy(xpath = "//td[@name='account_contact_0_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement billsNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_0_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> billsNotifTooltipMsgList;
	
	// this is the label for Bills notification
	@FindBy(name = "account_contact_0_bills_notification_label")
	public WebElement lblBillsNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterBillsPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerBillsPostal;
	
	// this is the tickbox for Postal
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_postal']/label/span[1]/input")
	public WebElement billsPostal;
	
	// this is the tickbox for Postal
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_postal']/label/span[1]/input")
	public List<WebElement> billsPostalList;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterBillsEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerBillsEmail;
	
	// this is the tickbox for Email
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_email']/label/span[1]/input")
	public WebElement billsEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterBillsSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerBillsSMS;
	
	// this is the tickbox for SMS
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_sms']/label/span[1]/input")
	public WebElement billsSMS;
	
	// this is the tickbox for Bills > SMS for checking if element exists
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_bills_sms']/label/span[1]/input")
	public List<WebElement> billsSMSList;
	
	// this is for the N/A when the notification is disabled
	@FindBy(xpath = "//div[@name='account_contact_0_notification']/table/tbody/tr[1]/td[4]/p")
	public WebElement billsSMSNotAvail;
	
	// this is the label for the notification tooltip icon
	@FindBy(name = "account_contact_0_reminders_tooltip")
	public WebElement acctnotifAndRemindersNotifTooltipIcon;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	@FindBy(xpath = "//td[@name='account_contact_0_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement acctnotifAndRemindersNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_0_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> acctnotifAndRemindersNotifTooltipMsgList;
	
	// this is the label for Account Notifications and Reminders notification
	@FindBy(name = "account_contact_0_reminders_notification_label")
	public WebElement lblAcctnotifAndRemindersNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterAcctnotifAndRemindersPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerAcctnotifAndRemindersPostal;
	
	// this is the tickbox for Postal
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_postal']/label/span[1]/input")
	public WebElement acctnotifAndRemindersPostal;
	
	// this is for checking if the element is displayed
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_postal']/label/span[1]/input")
	public List<WebElement> acctnotifAndRemindersPostalList;
	
	// this is for the N/A when the notification is disabled
	@FindBy(xpath = "//div[@name='account_contact_0_notification']/table/tbody/tr[2]/td[2]/p")
	public WebElement acctnotifAndRemindersPostalNotAvail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterAcctnotifAndRemindersEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerAcctnotifAndRemindersEmail;
	
	// this is the tickbox for Email
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_email']/label/span[1]/input")
	public WebElement acctnotifAndRemindersEmail;
	
	// this is the tickbox for Email
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_email']/label/span[1]/input")
	public List<WebElement> acctnotifAndRemindersEmailList;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterAcctnotifAndRemindersSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerAcctnotifAndRemindersSMS;
	
	// this is the tickbox for SMS
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_reminders_sms']/label/span[1]/input")
	public WebElement acctnotifAndRemindersSMS;
	
	// this is the label for the notification tooltip icon
	@FindBy(name = "account_contact_0_marketing_tooltip")
	public WebElement marketingComNotifTooltipIcon;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	@FindBy(xpath = "//td[@name='account_contact_0_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement marketingComNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_0_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> marketingComNotifTooltipMsgList;
	
	// this is the label for Marketing Communications notification
	@FindBy(name = "account_contact_0_marketing_notification_label")
	public WebElement lblMarketingComNotif;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterMarketingComPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerMarketingComPostal;
	
	// this is the tickbox for Postal
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_postal']/label/span[1]/input")
	public WebElement marketingComPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterMarketingComEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerMarketingComEmail;
	
	// this is the tickbox for Email
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_email']/label/span[1]/input")
	public WebElement marketingComEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement checkBoxOuterMarketingComSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement checkBoxInnerMarketingComSMS;
	
	// this is the tickbox for SMS
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_sms']/label/span[1]/input")
	public WebElement marketingComSMS;
	
	// this is the tickbox for SMS
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_0_marketing_sms']/label/span[1]/input")
	public List<WebElement> marketingComSMSList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_contact_email']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblEmailAddress;
	
	@FindBy(name = "account_contact_0_contact_email")
	public WebElement emailAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_contact_email']/div/div[2]")
	public WebElement underlineEmailAddress;
	
	@FindBy(name = "hint_account_contact_0_contact_email")
	public WebElement hintEmailAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMobilePhone;
	
	@FindBy(name = "account_contact_0_phone_number")
	public WebElement mobilePhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_number']/div/div[2]")
	public WebElement underlineMobilePhone;
	
	@FindBy(name = "hint_empty_account_contact_0_phone_number")
	public WebElement hintEmptyMobilePhone;
	
	@FindBy(name = "hint_invalid_account_contact_0_phone_number")
	public WebElement hintInvalidMobilePhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_business']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblBusinessPhone;
	
	@FindBy(name = "account_contact_0_phone_business")
	public WebElement businessPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_business']/div/div[2]")
	public WebElement underlineBusinessPhone;
	
	@FindBy(name = "hint_account_contact_0_phone_business")
	public WebElement hintEmptyBusinessPhone;
	
	@FindBy(name = "hint_invalid_account_contact_0_phone_business")
	public WebElement hintInvalidBusinessPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_home_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblAfterhoursPhone;
	
	@FindBy(name = "account_contact_0_phone_home_number")
	public WebElement afterhoursPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_phone_home_number']/div/div[2]")
	public WebElement underlineAfterhoursPhone;
	
	@FindBy(name = "hint_account_contact_0_phone_home_number")
	public WebElement hintEmptyAfterhoursPhone;
	
	@FindBy(name = "hint_invalid_contact_0_phone_home_number")
	public WebElement hintInvalidAfterhoursPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_secret_code']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblContactSecretCode;
	
	@FindBy(name = "account_contact_0_secret_code")
	public WebElement contactSecretCode;
	
	// this is for checking if the field exists or not
	@FindBy(name = "account_contact_0_secret_code")
	public List<WebElement> contactSecretCodeList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_0_secret_code']/div/div[2]")
	public WebElement underlineContactSecretCode;
	
	@FindBy(name = "hint_account_contact_0_secret_code")
	public WebElement hintContactSecretCode;
	
	@FindBy(xpath = "//button[@name='button_add_account_contact_0']/span/mat-icon")
	public WebElement iconAddAnotherContact;
	
	@FindBy(name = "button_add_account_contact_0")
	public WebElement addAnotherContact;
	
	// this is for checking if the element exists
	@FindBy(name = "button_add_account_contact_0")
	public List<WebElement> addAnotherContactList;
	
	@FindBy(name = "button_prev_account_contact_0")
	public WebElement previous;
	
	@FindBy(name = "button_next_account_contact_0")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public MainAccountContactMoveIn(WebDriver driver) {
		
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
	public MainAccountContactMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
