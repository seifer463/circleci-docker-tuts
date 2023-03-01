package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdditionalContactMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	// this is for the first additional contact
	@FindBy(name = "stepper_account_contact_1")
	public WebElement addCont1Header;
	
	@FindBy(name = "stepper_account_contact_2")
	public WebElement addCont2Header;
	
	// did this because for the Main Account Contact
	// the node name is also app-account-contact
	@FindBy(xpath = "//app-account-contact/form[starts-with(@class,'move-in-step-form')]")
	public List<WebElement> wholeForm;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_first_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblFirstName;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_first_name")
	public WebElement addCont1FirstName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_first_name']/div/div[2]")
	public WebElement addCont1UnderlineFirstName;
	
	@FindBy(name = "hint_account_contact_1_first_name")
	public WebElement addCont1HintFirstName;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_first_name")
	public WebElement addCont2FirstName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_last_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblLastName;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_last_name")
	public WebElement addCont1LastName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_last_name']/div/div[2]")
	public WebElement addCont1UnderlineLastName;
	
	@FindBy(name = "hint_account_contact_1_last_name")
	public WebElement addCont1HintLastName;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_last_name")
	public WebElement addCont2LastName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_1']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblDateOfBirth;
	
	// this is for the 1st additional contact
	@FindBy(name = "input_date_of_birth_account_contact_1")
	public WebElement addCont1DateOfBirth;
	
	// this is for the 1st additional contact
	// this is for checking if the element exists or not
	@FindBy(name = "input_date_of_birth_account_contact_1")
	public List<WebElement> addCont1DateOfBirthList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_1']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement addCont1IconDateOfBirth;
	
	@FindBy(xpath = "//mat-form-field[@name='field_date_of_birth_account_contact_1']/div/div[2]")
	public WebElement addCont1UnderlineDateOfBirth;
	
	@FindBy(name = "hint_date_of_birth_account_contact_1")
	public WebElement addCont1HintDateOfBirth;
	
	// this is for the 2nd additional contact
	@FindBy(name = "input_date_of_birth_account_contact_2")
	public WebElement addCont2DateOfBirth;
	
	@FindBy(name = "account_contact_1_label_personal_identification")
	public WebElement addCont1LblPersonalIdentification;
	
	// this is for checking if the element exists
	@FindBy(name = "account_contact_1_label_personal_identification")
	public List<WebElement> addCont1LblPersonalIdentificationList;
	
	// this is for the 1st additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_driver_license']/label/span[1]/input")
	public WebElement addCont1DriversLicence;
	
	// this is for the 1st additional contact
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_driver_license']/label/span[1]/input")
	public List<WebElement> addCont1DriversLicenceList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_driver_license']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont1RadioOuterDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_driver_license']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont1RadioInnerDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_driver_license']/label/span[2]")
	public WebElement addCont1LblDriversLicence;
	
	// this is for the 2nd additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_driver_license']/label/span[1]/input")
	public WebElement addCont2DriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_driver_license']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont2RadioOuterDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_driver_license']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont2RadioInnerDriversLicence;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_driver_license']/label/span[2]")
	public WebElement addCont2LblDriversLicence;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_drivers_license_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblDriversLicenceNumber;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_drivers_license_number")
	public WebElement addCont1DriversLicenceNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_drivers_license_number']/div/div[2]")
	public WebElement addCont1UnderlineDriversLicenceNumber;
	
	@FindBy(name = "hint_account_contact_1_drivers_license_number")
	public WebElement adCont1HintDriversLicenceNumber;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_drivers_license_number")
	public WebElement addCont2DriversLicenceNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_2_drivers_license_number']/div/div[2]")
	public WebElement addCont2UnderlineDriversLicenceNumber;
	
	@FindBy(name = "hint_account_contact_2_drivers_license_number")
	public WebElement adCont2HintDriversLicenceNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_drivers_state']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblDriversLicenceState;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_drivers_state")
	public WebElement addCont1DriversLicenceState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_drivers_state']/div/div[2]")
	public WebElement addCont1UnderlineDriversLicenceState;
	
	@FindBy(name = "hint_account_contact_1_drivers_state")
	public WebElement addCont1HintDriversLicenceState;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_drivers_state")
	public WebElement addCont2DriversLicenceState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_2_drivers_state']/div/div[2]")
	public WebElement addCont2UnderlineDriversLicenceState;
	
	@FindBy(name = "hint_account_contact_2_drivers_state")
	public WebElement addCont2HintDriversLicenceState;
	
	// this is for the div where the state are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement statesDiv;
	
	// this is for the 1st additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_passport']/label/span[1]/input")
	public WebElement addCont1Passport;
	
	// this is for the 1st additional contact
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_passport']/label/span[1]/input")
	public List<WebElement> addCont1PassportList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_passport']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont1RadioOuterPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_passport']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont1RadioInnerPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_passport']/label/span[2]")
	public WebElement addCont1LblPassport;
	
	// this is for the 2nd additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_passport']/label/span[1]/input")
	public WebElement addCont2Passport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_passport']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont2RadioOuterPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_passport']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont2RadioInnerPassport;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_passport']/label/span[2]")
	public WebElement addCont2LblPassport;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_contact_passport']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblPassportNumber;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_passport")
	public WebElement addCont1PassportNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_contact_passport']/div/div[2]")
	public WebElement addCont1UnderlinePassportNumber;
	
	@FindBy(name = "hint_account_contact_1_passport")
	public WebElement addCont1HintPassportNumber;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_passport")
	public WebElement addCont2PassportNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_passport_country']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblPassportCountry;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_passport_country")
	public WebElement addCont1PassportCountry;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_passport_country']/div/div[2]")
	public WebElement addCont1UnderlinePassportCountry;
	
	@FindBy(name = "hint_account_contact_1_passport_country")
	public WebElement addCont1HintPassportCountry;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_passport_country")
	public WebElement addCont2PassportCountry;
	
	// this is for the div where the countries are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement countriesDiv;
	
	// this is for the 1st additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_medical']/label/span[1]/input")
	public WebElement addCont1MedicareCard;
	
	// this is for the 1st additional contact
	// this is for checking if the element exists
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_medical']/label/span[1]/input")
	public List<WebElement> addCont1MedicareCardList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_medical']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont1RadioOuterMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_medical']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont1RadioInnerMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_medical']/label/span[2]")
	public WebElement addCont1LblMedicareCard;
	
	// this is for the 2nd additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_medical']/label/span[1]/input")
	public WebElement addCont2MedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_medical']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont2RadioOuterMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_medical']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont2RadioInnerMedicareCard;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_medical']/label/span[2]")
	public WebElement addCont2LblMedicareCard;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_mediare_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblMedicareCardNumber;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_mediare_number")
	public WebElement addCont1MedicareCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_mediare_number']/div/div[2]")
	public WebElement addCont1UnderlineMedicareCardNumber;
	
	@FindBy(name = "hint_account_contact_1_mediare_number")
	public WebElement addCont1HintMedicareCardNumber;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_mediare_number")
	public WebElement addCont2MedicareCardNumber;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_1_medicare_main']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblMedicareCardExpiry;
	
	// this is for the 1st additional contact
	@FindBy(name = "input_account_contact_1_medicare_main")
	public WebElement addCont1MedicareCardExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_1_medicare_main']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement addCont1IconMedicareCardExpiry;
	
	@FindBy(xpath = "//mat-form-field[@name='account_contact_1_medicare_main']/div/div[2]")
	public WebElement addCont1UnderlineMedicareCardExpiry;
	
	@FindBy(name = "hint_account_contact_1_medicare_main")
	public WebElement addCont1HintMedicareCardExpiry;
	
	// this is for the 2nd additional contact
	@FindBy(name = "input_account_contact_2_medicare_main")
	public WebElement addCont2MedicareCardExpiry;
	
	// this is for the 1st additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_none_identification']/label/span[1]/input")
	public WebElement addCont1ProvideNone;

	// this is for the 1st additional contact
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_none_identification']/label/span[1]/input")
	public List<WebElement> addCont1ProvideNoneList;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_none_identification']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement addCont1RadioOuterProvideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_none_identification']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement addCont1RadioInnerProvideNone;
	
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_1_radio_none_identification']/label/span[2]")
	public WebElement addCont1LblProvideNone;
	
	// this is for the 2nd additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_none_identification']/label/span[1]/input")
	public WebElement addCont2ProvideNone;
	
	// this is for checking if the element exists or not
	// this is for the 1st additional contact
	@FindBy(xpath = "//mat-radio-button[@name='account_contact_2_radio_none_identification']/label/span[1]/input")
	public List<WebElement> addCont2ProvideNoneList;
	
	// this is the div tag or the container for the whole notification area
	@FindBy(name = "account_contact_1_notification")
	public WebElement addCont1NotificationContainer;
	
	// This is for the notification header for the 1st additional contact
	@FindBy(name = "account_contact_1_notified_label")
	public WebElement addCont1LblNotificationHeader;
	
	// This is for the notification header for the 2nd additional contact
	@FindBy(name = "account_contact_2_notified_label")
	public WebElement addCont2LblNotificationHeader;
	
	// This is for the notification introduction for the 1st additional contact
	@FindBy(name = "account_contact_1_notification_introduction")
	public WebElement addCont1LblNotificationIntro;
	
	@FindBy(xpath = "//p[@name='account_contact_1_notification_introduction']/p/a")
	public WebElement addCont1LinkLblNotificationIntro;
	
	// This is for the notification introduction for the 2nd additional contact
	@FindBy(name = "account_contact_2_notification_introduction")
	public WebElement addCont2LblNotificationIntro;
	
	@FindBy(name = "account_contact_1_postal_header")
	public WebElement addCont1LblPostalNotifHeader;
	
	@FindBy(name = "account_contact_1_email_header")
	public WebElement addCont1LblEmailNotifHeader;
	
	@FindBy(name = "account_contact_1_sms_header")
	public WebElement addCont1LblSMSNotifHeader;
	
	// this is the notification tooltip icon
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_bills_tooltip")
	public WebElement addCont1BillsNotifTooltipIcon;
	
	// this is the notification tooltip icon
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_bills_tooltip")
	public WebElement addCont2BillsNotifTooltipIcon;
	
	// this is the text for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 1st additional contact
	@FindBy(xpath = "//td[@name='account_contact_1_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont1BillsNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_1_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont1BillsNotifTooltipMsgList;
	
	// this is the text for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 2nd additional contact
	@FindBy(xpath = "//td[@name='account_contact_2_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont2BillsNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_2_bills_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont2BillsNotifTooltipMsgList;
	
	// this is the label for Bills notification
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_bills_notification_label")
	public WebElement addCont1LblBillsNotif;
	
	// this is the label for Bills notification
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_bills_notification_label")
	public WebElement addCont2LblBillsNotif;
	
	// this is the tickbox for Postal
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_postal']/label/span[1]/input")
	public WebElement addCont1BillsPostal;
	
	// this is the tickbox for Postal
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_postal']/label/span[1]/input")
	public List<WebElement> addCont1BillsPostalList;

	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterBillsPostal;

	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerBillsPostal;
	
	// this is the tickbox for Postal
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_bills_postal']/label/span[1]/input")
	public WebElement addCont2BillsPostal;
	
	// this is the tickbox for Email
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_email']/label/span[1]/input")
	public WebElement addCont1BillsEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterBillsEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerBillsEmail;
	
	// this is the tickbox for Email
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_bills_email']/label/span[1]/input")
	public WebElement addCont2BillsEmail;
	
	// this is the tickbox for SMS
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_sms']/label/span[1]/input")
	public WebElement addCont1BillsSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterBillsSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerBillsSMS;
	
	// this is the tickbox for SMS
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_bills_sms']/label/span[1]/input")
	public WebElement addCont2BillsSMS;
	
	// this is the tickbox for Bills > SMS
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_bills_sms']/label/span[1]/input")
	public List<WebElement> addCont1BillsSMSList;
	
	// this is for the N/A when the notification is disabled
	@FindBy(xpath = "//div[@name='account_contact_1_notification']/table/tbody/tr[1]/td[4]/p")
	public WebElement addCont1BillsSMSNotAvail;
	
	// this is the tickbox for Bills > SMS
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_bills_sms']/label/span[1]/input")
	public List<WebElement> addCont2BillsSMSList;
	
	// this is the notification tooltip icon
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_reminders_tooltip")
	public WebElement addCont1AcctnotifAndRemindersNotifTooltipIcon;
	
	// this is the notification tooltip icon
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_reminders_tooltip")
	public WebElement addCont2AcctnotifAndRemindersNotifTooltipIcon;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 1st additional contact
	@FindBy(xpath = "//td[@name='account_contact_1_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont1AcctnotifAndRemindersNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_1_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont1AcctnotifAndRemindersNotifTooltipMsgList;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 2nd additional contact
	@FindBy(xpath = "//td[@name='account_contact_2_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont2AcctnotifAndRemindersNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_2_reminders_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont2AcctnotifAndRemindersNotifTooltipMsgList;
	
	// this is the label for Account Notifications and Reminders notification
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_reminders_notification_label")
	public WebElement addCont1LblAcctnotifAndRemindersNotif;
	
	// this is the label for Account Notifications and Reminders notification
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_reminders_notification_label")
	public WebElement addCont2LblAcctnotifAndRemindersNotif;
	
	// this is the tickbox for Postal
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_postal']/label/span[1]/input")
	public WebElement addCont1AcctnotifAndRemindersPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterAcctnotifAndRemindersPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerAcctnotifAndRemindersPostal;
	
	// this is for checking if the element is displayed
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_postal']/label/span[1]/input")
	public List<WebElement> addCont1AcctnotifAndRemindersPostalList;
	
	// this is for the N/A when the notification is disabled
	@FindBy(xpath = "//div[@name='account_contact_1_notification']/table/tbody/tr[2]/td[2]/p")
	public WebElement addCont1AcctnotifAndRemindersPostalNotAvail;
	
	// this is the tickbox for Postal
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_reminders_postal']/label/span[1]/input")
	public WebElement addCont2AcctnotifAndRemindersPostal;
	
	// this is for checking if the element is displayed
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_reminders_postal']/label/span[1]/input")
	public List<WebElement> addCont2AcctnotifAndRemindersPostalList;
	
	// this is the tickbox for Email
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_email']/label/span[1]/input")
	public WebElement addCont1AcctnotifAndRemindersEmail;
	
	// this is the tickbox for Email
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_email']/label/span[1]/input")
	public List<WebElement> addCont1AcctnotifAndRemindersEmailList;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterAcctnotifAndRemindersEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerAcctnotifAndRemindersEmail;
	
	// this is the tickbox for Email
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_reminders_email']/label/span[1]/input")
	public WebElement addCont2AcctnotifAndRemindersEmail;
	
	// this is the tickbox for SMS
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_sms']/label/span[1]/input")
	public WebElement addCont1AcctnotifAndRemindersSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterAcctnotifAndRemindersSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_reminders_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerAcctnotifAndRemindersSMS;
	
	// this is the tickbox for SMS
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_reminders_sms']/label/span[1]/input")
	public WebElement addCont2AcctnotifAndRemindersSMS;
	
	// this is the label for the notification tooltip icon
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_marketing_tooltip")
	public WebElement addCont1MarketingComNotifTooltipIcon;
	
	// this is the label for the notification tooltip icon
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_marketing_tooltip")
	public WebElement addCont2MarketingComNotifTooltipIcon;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 1st additional contact
	@FindBy(xpath = "//td[@name='account_contact_1_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont1MarketingComNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_1_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont1MarketingComNotifTooltipMsgList;
	
	// this is the label for notification tooltip message
	// make sure you already hover in the tooltip icon before using this element
	// otherwise selenium won't be able to locate the element since it's hidden
	// for the 2nd additional contact
	@FindBy(xpath = "//td[@name='account_contact_2_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public WebElement addCont2MarketingComNotifTooltipMsg;
	
	@FindBy(xpath = "//td[@name='account_contact_2_marketing_notification_label']/ngb-tooltip-window[@role='tooltip']")
	public List<WebElement> addCont2MarketingComNotifTooltipMsgList;
	
	// this is the label for Marketing Communications notification
	// for the 1st additional contact
	@FindBy(name = "account_contact_1_marketing_notification_label")
	public WebElement addCont1LblMarketingComNotif;
	
	// this is the label for Marketing Communications notification
	// for the 2nd additional contact
	@FindBy(name = "account_contact_2_marketing_notification_label")
	public WebElement addCont2LblMarketingComNotif;
	
	// this is the tickbox for Postal
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_postal']/label/span[1]/input")
	public WebElement addCont1MarketingComPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_postal']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterMarketingComPostal;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_postal']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerMarketingComPostal;
	
	// this is the tickbox for Postal
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_marketing_postal']/label/span[1]/input")
	public WebElement addCont2MarketingComPostal;
	
	// this is the tickbox for Email
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_email']/label/span[1]/input")
	public WebElement addCont1MarketingComEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_email']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterMarketingComEmail;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_email']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerMarketingComEmail;
	
	// this is the tickbox for Email
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_marketing_email']/label/span[1]/input")
	public WebElement addCont2MarketingComEmail;
	
	// this is the tickbox for SMS
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_sms']/label/span[1]/input")
	public WebElement addCont1MarketingComSMS;
	
	// this is the tickbox for SMS
	// for the 1st additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_sms']/label/span[1]/input")
	public List<WebElement> addCont1MarketingComSMSList;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_sms']/label/span[1]/span[@class='mat-checkbox-frame']")
	public WebElement addCont1CheckBoxOuterMarketingComSMS;
	
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_1_marketing_sms']/label/span[1]/span[@class='mat-checkbox-background']")
	public WebElement addCont1CheckBoxInnerMarketingComSMS;
	
	// this is the tickbox for SMS
	// for the 2nd additional contact
	@FindBy(xpath = "//mat-checkbox[@name='account_contact_2_marketing_sms']/label/span[1]/input")
	public WebElement addCont2MarketingComSMS;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_contact_email']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblEmailAddress;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_contact_email")
	public WebElement addCont1EmailAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_contact_email']/div/div[2]")
	public WebElement addCont1UnderlineEmailAddress;
	
	@FindBy(name = "hint_account_contact_1_contact_email")
	public WebElement addCont1HintEmailAddress;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_contact_email")
	public WebElement addCont2EmailAddress;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblMobilePhone;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_phone_number")
	public WebElement addCont1MobilePhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_number']/div/div[2]")
	public WebElement addCont1UnderlineMobilePhone;
	
	@FindBy(name = "hint_empty_account_contact_1_phone_number")
	public WebElement addCont1HintEmptyMobilePhone;
	
	@FindBy(name = "hint_invalid_account_contact_1_phone_number")
	public WebElement addCont1HintInvalidMobilePhone;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_phone_number")
	public WebElement addCont2MobilePhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_business']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblBusinessPhone;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_phone_business")
	public WebElement addCont1BusinessPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_business']/div/div[2]")
	public WebElement addCont1UnderlineBusinessPhone;
	
	@FindBy(name = "hint_empty_account_contact_1_phone_business")
	public WebElement addCont1HintEmptyBusinessPhone;
	
	@FindBy(name = "hint_invalid_account_contact_1_phone_business")
	public WebElement addCont1HintInvalidBusinessPhone;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_phone_business")
	public WebElement addCont2BusinessPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_home_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblAfterhoursPhone;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_phone_home_number")
	public WebElement addCont1AfterhoursPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_phone_home_number']/div/div[2]")
	public WebElement addCont1UnderlineAfterhoursPhone;
	
	@FindBy(name = "hint_empty_account_contact_1_phone_home_number")
	public WebElement addCont1HintEmptyAfterhoursPhone;
	
	@FindBy(name = "hint_invalid_contact_1_phone_home_number")
	public WebElement addCont1HintInvalidAfterhoursPhone;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_phone_home_number")
	public WebElement addCont2AfterhoursPhone;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_secret_code']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement addCont1FloaterLblContactSecretCode;
	
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_secret_code")
	public WebElement addCont1ContactSecretCode;
	
	// this is for checking if the field exists or not
	// this is for the 1st additional contact
	@FindBy(name = "account_contact_1_secret_code")
	public List<WebElement> addCont1ContactSecretCodeList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_account_contact_1_secret_code']/div/div[2]")
	public WebElement addCont1UnderlineContactSecretCode;
	
	@FindBy(name = "hint_account_contact_1_secret_code")
	public WebElement addCont1HintContactSecretCode;
	
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_secret_code")
	public WebElement addCont2ContactSecretCode;
	
	// this is for checking if the field exists or not
	// this is for the 2nd additional contact
	@FindBy(name = "account_contact_2_secret_code")
	public List<WebElement> addCont2ContactSecretCodeList;

	// previous button
	// and the first additional contact
	@FindBy(name = "button_prev_account_contact_1")
	public WebElement addCont1Previous;
	
	// previous button
	// and the second additional contact
	@FindBy(name = "button_prev_account_contact_2")
	public WebElement addCont2Previous;
	
	// next button
	// and the first additional contact
	@FindBy(name = "button_next_account_contact_1")
	public WebElement addCont1Next;
	
	// next button
	// and the second additional contact
	@FindBy(name = "button_next_account_contact_2")
	public WebElement addCont2Next;
	
	@FindBy(xpath = "//button[@name='button_remove_account_contact_1']/span/mat-icon")
	public WebElement addCont1IconRemAdditionalContact;
	
	// this is the link for removing the additional contact
	// in the 1st additional contact section
	@FindBy(name = "button_remove_account_contact_1")
	public WebElement addCont1RemAdditionalContact;
	
	// for checking if the element exists or not
	// this is the link for removing the additional contact
	// in the 1st additional contact section
	@FindBy(name = "button_remove_account_contact_1")
	public List<WebElement> addCont1RemAdditionalContactList;
	
	@FindBy(xpath = "//button[@name='button_add_account_contact_1']/span/mat-icon")
	public WebElement addCont1IconAddAnotherContact;
	
	// this is for the link for add another contact
	// in the 1st additional contact section
	@FindBy(name = "button_add_account_contact_1")
	public WebElement addCont1AddAnotherContact;
	
	// this is for the link for add another contact
	// in the 1st additional contact section
	// for checking if the element exists or not
	@FindBy(name = "button_add_account_contact_1")
	public List<WebElement> addCont1AddAnotherContactList;
	
	// this is the link for removing the additional contact
	// in the 2nd additional contact section
	@FindBy(name = "button_remove_account_contact_2")
	public WebElement addCont2RemAdditionalContact;
	
	// this is the link for removing the additional contact
	// in the 2nd additional contact section
	@FindBy(name = "button_remove_account_contact_2")
	public List<WebElement> addCont2RemAdditionalContactList;
	
	// this is for the link for add another contact
	// in the 2nd additional contact section
	@FindBy(name = "button_add_account_contact_2")
	public WebElement addCont2AddAnotherContact;
	
	// for checking if the element exists or not
	@FindBy(name = "button_add_account_contact_2")
	public List<WebElement> addCont2AddAnotherContactList;
	
	@FindBy(xpath = "//mat-dialog-container[starts-with(@id,'mat-dialog-')]")
	public WebElement dialogContainer;
	
	@FindBy(xpath = "//div[@name='dialog_content']/p")
	public WebElement dialogContainerText;
	
	@FindBy(name = "dialog_button_yes")
	public WebElement deleteContactYes;
	
	@FindBy(name = "dialog_button_no")
	public WebElement deleteContactNo;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public AdditionalContactMoveIn(WebDriver driver) {
		
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
	public AdditionalContactMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
