package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SupplyDetailsMoveIn {
	
	WebDriver driver;
	
	@FindBy(name = "main_header")
	public WebElement lblMainHeader;
	
	// this is for checking if the element exists
	@FindBy(name = "main_header")
	public List<WebElement> lblMainHeaderList;
	
	@FindBy(name = "supply_details_introduction")
	public WebElement lblSupplyDetailsIntro;
	
	@FindBy(xpath = "//p[@name='supply_details_introduction']/a")
	public WebElement linkLblSupplyDetailsIntro;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	// this is the ID of the header, useful when clicking the header
	// when going to the next section
	@FindBy(name = "stepper_supply_details")
	public WebElement header;
	
	@FindBy(name = "label_moving_in_as")
	public WebElement lblMovingInHeader;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_tenant']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterTenant;

	@FindBy(xpath = "//mat-radio-button[@name='radio_owner']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterOwner;

	@FindBy(xpath = "//mat-radio-button[@name='radio_project_manager']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterPropManager;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_tenant']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerTenant;

	@FindBy(xpath = "//mat-radio-button[@name='radio_owner']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerOwner;

	@FindBy(xpath = "//mat-radio-button[@name='radio_project_manager']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerPropManager;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_tenant']/label/span[2]")
	public WebElement lblTenant;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner']/label/span[2]")
	public WebElement lblOwner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_project_manager']/label/span[2]")
	public WebElement lblPropManager;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_tenant']/label/span[1]/input")
	public WebElement tenant;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner']/label/span[1]/input")
	public WebElement owner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_project_manager']/label/span[1]/input")
	public WebElement propManager;
	
	@FindBy(xpath = "//mat-form-field[@name='field_less_commencement_date']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMoveInDateTenant;
	
	// this is the Move-In date for Tenant
	@FindBy(name = "input_less_commencement_date")
	public WebElement moveInDateTenant;
	
	// this is for checking if the element exists or not
	@FindBy(name = "input_less_commencement_date")
	public List<WebElement> moveInDateTenantList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_less_commencement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerMoveInDateTenant;
	
	@FindBy(xpath = "//mat-form-field[@name='field_less_commencement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconMoveInDateTenant;
	
	@FindBy(xpath = "//mat-form-field[@name='field_less_commencement_date']/div/div[2]")
	public WebElement underlineMoveInDateTenant;
	
	@FindBy(name = "hint_less_commencement_date")
	public WebElement hintMoveInDateTenant;
	
	@FindBy(xpath = "//mat-form-field[@name='field_moving_in_date']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMoveInDateOwner;
	
	// this is the Move-In date for Owner
	@FindBy(name = "input_moving_in_date")
	public WebElement moveInDateOwner;
	
	// this is for checking if the element exists or not
	@FindBy(name = "input_moving_in_date")
	public List<WebElement> moveInDateOwnerList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_moving_in_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerMoveInDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_moving_in_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconMoveInDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_moving_in_date']/div/div[2]")
	public WebElement underlineMoveInDateOwner;
	
	@FindBy(name = "hint_moving_in_date")
	public WebElement hintMoveInDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSettlementDateOwner;
	
	// this is the Settlement Date for Owner
	@FindBy(name = "input_settlement_date")
	public WebElement settlementDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerSettlementDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconSettlementDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date']/div/div[2]")
	public WebElement underlineSettlementDateOwner;
	
	@FindBy(name = "hint_settlement_date")
	public WebElement hintSettlementDateOwner;
	
	@FindBy(xpath = "//mat-form-field[@name='field_owner_agreement_date']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblMoveInDatePropMan;
	
	// this is the Move-In date for Property Manager or Letting Agent
	@FindBy(name = "input_owner_agreement_date")
	public WebElement moveInDatePropMan;
	
	// this for checking if the element exists or not
	@FindBy(name = "input_owner_agreement_date")
	public List<WebElement> moveInDatePropManList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_owner_agreement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerMoveInDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_owner_agreement_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconMoveInDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_owner_agreement_date']/div/div[2]")
	public WebElement underlineMoveInDatePropMan;
	
	@FindBy(name = "hint_owner_agreement_date")
	public WebElement hintMoveInDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date_property']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSettlementDatePropMan;
	
	@FindBy(name = "input_settlement_date_property")
	public WebElement settlementDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date_property']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerSettlementDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date_property']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconSettlementDatePropMan;
	
	@FindBy(xpath = "//mat-form-field[@name='field_settlement_date_property']/div/div[2]")
	public WebElement underlineSettlementDatePropMan;
	
	@FindBy(name = "hint_settlement_date_property")
	public WebElement hintSettlementDatePropMan;
	
	@FindBy(name = "label_owner_settlement_date")
	public WebElement lblOwnerSettle;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_yes']/label/span[1]/input")
	public WebElement ownerSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_no']/label/span[1]/input")
	public WebElement ownerSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterOwnerSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterOwnerSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerOwnerSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerOwnerSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_yes']/label/span[2]")
	public WebElement lblOwnerSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_settlement_no']/label/span[2]")
	public WebElement lblOwnerSettleNo;
	
	@FindBy(name = "label_manager_settlement_date")
	public WebElement lblPropManSettle;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_yes']/label/span[1]/input")
	public WebElement propManSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_no']/label/span[1]/input")
	public WebElement propManSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterPropManSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterPropManSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerPropManSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerPropManSettleNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_yes']/label/span[2]")
	public WebElement lblPropManSettleYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_manager_settlement_no']/label/span[2]")
	public WebElement lblPropManSettleNo;
	
	@FindBy(name = "label_holiday_rental")
	public WebElement lblOwnerPropManHoliday;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_yes']/label/span[1]/input")
	public WebElement ownerPropManHolidayYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_no']/label/span[1]/input")
	public WebElement ownerPropManHolidayNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterOwnerPropManHolidayYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterOwnerPropManHolidayNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerOwnerPropManHolidayYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerOwnerPropManHolidayNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_yes']/label/span[2]")
	public WebElement lblOwnerPropManHolidayYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_owner_manager_holiday_no']/label/span[2]")
	public WebElement lblOwnerPropManHolidayNo;
	
	@FindBy(name = "label_responsible_for_paying_question")
	public WebElement lblWhoIsResponsible;
	
	// this is for checking if the element exists
	@FindBy(name = "label_responsible_for_paying_question")
	public List<WebElement> lblWhoIsResponsibleList;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_owner']/label/span[1]/input")
	public WebElement whoIsResponsibleOwner;
	
	// for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_owner']/label/span[1]/input")
	public List<WebElement> whoIsResponsibleOwnerList;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_owner']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterWhoIsResponsibleOwner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_property_manager']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterWhoIsResponsiblePropMan;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_owner']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerWhoIsResponsibleOwner;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_property_manager']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerWhoIsResponsiblePropMan;
	
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_property_manager']/label/span[1]/input")
	public WebElement whoIsResponsiblePropMan;
	
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='radio_responsible_for_paying_property_manager']/label/span[1]/input")
	public List<WebElement> whoIsResponsiblePropManList;
	
	@FindBy(name = "label_supply_address")
	public WebElement lblSupplyAddHeader;
	
	@FindBy(xpath = "//mat-form-field[@name='field_lookup_supply_details']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddSearch;
	
	@FindBy(name = "address_lookup_supply_details")
	public WebElement supplyAddSearch;
	
	// this is when checking if the elements exists
	@FindBy(name = "address_lookup_supply_details")
	public List<WebElement> supplyAddSearchList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_lookup_supply_details']/div/div[1]/div[2]/mat-icon")
	public WebElement iconSupplyAddSearch;
	
	@FindBy(xpath = "//mat-form-field[@name='field_lookup_supply_details']/div/div[2]")
	public WebElement underlineSupplyAddSearch;
	
	@FindBy(name = "hint_lookup_supply_details")
	public WebElement hintSupplyAddSearch;
	
	// this is for the div where the supply addresses are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement supplyAddressesDiv;
	
	// this is the link for cannot find address
	@FindBy(name = "cannot_find_address_supply_details")
	public WebElement supplyAddCantFindAdd;
	
	// this is the link for the Quick Address Search
	@FindBy(name = "button_quick_address_search_supply_details")
	public WebElement supplyAddQuickAddressSearch;
	
	// this is the link for the Quick Address Search
	@FindBy(name = "button_quick_address_search_supply_details")
	public List<WebElement> supplyAddQuickAddressSearchList;
	
	// this is the link for the Manual Address Search
	@FindBy(name = "button_manual_address_search_supply_details")
	public WebElement supplyAddManualAddressSearch;
	
	// this is for checking if the element exists or not
	@FindBy(name = "button_manual_address_search_supply_details")
	public List<WebElement> supplyAddManualAddressSearchList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_complex_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddComplexName;
	
	@FindBy(name = "complex_name")
	public WebElement supplyAddComplexName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_complex_name']/div/div[2]")
	public WebElement underlineSupplyAddComplexName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_search_string_tenancy_type']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddTenancyType;
	
	@FindBy(name = "search_string_tenancy_type")
	public WebElement supplyAddTenancyType;
	
	@FindBy(xpath = "//mat-form-field[@name='field_search_string_tenancy_type']/div/div[2]")
	public WebElement underlineSupplyAddTenancyType;
	
	@FindBy(name = "hint_search_string_tenancy_type")
	public WebElement hintSupplyAddTenancyType;
	
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement supplyAddTenancyTypeDiv;
	
	@FindBy(xpath = "//mat-form-field[@name='field_unit_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddTenancyNum;
	
	@FindBy(name = "unit_name")
	public WebElement supplyAddTenancyNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_unit_name']/div/div[2]")
	public WebElement underlineSupplyAddTenancyNum;
	
	@FindBy(name = "hint_unit_name")
	public WebElement hintSupplyAddTenancyNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_street_number']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddStreetNum;
	
	@FindBy(name = "street_number")
	public WebElement supplyAddStreetNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_street_number']/div/div[2]")
	public WebElement underlineSupplyAddStreetNum;
	
	@FindBy(xpath = "//mat-form-field[@name='field_street_name']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddStreetName;
	
	@FindBy(name = "street_name")
	public WebElement supplyAddStreetName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_street_name']/div/div[2]")
	public WebElement underlineSupplyAddStreetName;
	
	@FindBy(name = "hint_street_name")
	public WebElement hintSupplyAddStreetName;
	
	@FindBy(xpath = "//mat-form-field[@name='field_search_string_street_type']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddStreetType;
	
	@FindBy(name = "search_string_street_type")
	public WebElement supplyAddStreetType;
	
	@FindBy(xpath = "//mat-form-field[@name='field_search_string_street_type']/div/div[2]")
	public WebElement underlineSupplyAddStreetType;
	
	@FindBy(name = "hint_search_string_street_type")
	public WebElement hintSupplyAddStreetType;
	
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement supplyAddStreetTypeDiv;
	
	@FindBy(xpath = "//mat-form-field[@name='field_city_suburb']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddCity;
	
	@FindBy(name = "city_suburb")
	public WebElement supplyAddCity;
	
	@FindBy(xpath = "//mat-form-field[@name='field_city_suburb']/div/div[2]")
	public WebElement underlineSupplyAddCity;
	
	@FindBy(name = "hint_city_suburb")
	public WebElement hintSupplyAddCity;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_state_search_string']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddState;
	
	@FindBy(name = "supply_state_search_string")
	public WebElement supplyAddState;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_state_search_string']/div/div[2]")
	public WebElement underlineSupplyAddState;
	
	@FindBy(name = "hint_supply_state_search_string")
	public WebElement hintSupplyAddState;
	
	// this is the spinner icon displayed in the supply state
	// that looks for the public holidays
	// to check if it's displayed or not
	@FindBy(xpath = "//mat-spinner[@role='progressbar']")
	public List<WebElement> supplyAddStateSpinnerList;
	
	@FindBy(xpath = "//div[starts-with(@id,'mat-autocomplete-') and @role='listbox']")
	public WebElement supplyAddStateDiv;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_postal_code']/div/div[1]/div[starts-with(@class,'mat-form-field-infix')]/span/label/span")
	public WebElement floaterLblSupplyAddPostcode;
	
	@FindBy(name = "supply_postal_code")
	public WebElement supplyAddPostcode;
	
	@FindBy(xpath = "//mat-form-field[@name='field_supply_postal_code']/div/div[2]")
	public WebElement underlineSupplyAddPostcode;
	
	@FindBy(name = "hint_supply_postal_code")
	public WebElement hintSupplyAddPostcode;
	
	@FindBy(name = "label_supply_connected")
	public WebElement lblSupplyConnectedHeader;
	
	@FindBy(name = "label_supply_connected")
	public List<WebElement> lblSupplyConnectedHeaderList;
	
	@FindBy(name = "supply_connected_introduction")
	public WebElement lblSupplyConnectedIntro;
	
	// this is for checking if the element exists or not
	@FindBy(name = "supply_connected_introduction")
	public List<WebElement> lblSupplyConnectedIntroList;
	
	@FindBy(xpath = "//p[@name='supply_connected_introduction']/a")
	public WebElement linkLblSupplyConnectedIntro;
	
	@FindBy(name = "label_supply_connected_question")
	public WebElement lblSupplyConnectedQuestion;
	
	@FindBy(name = "label_supply_connected_question")
	public List<WebElement> lblSupplyConnectedQuestionList;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_yes']/label/span[2]")
	public WebElement lblSupplyConnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_no']/label/span[2]")
	public WebElement lblSupplyDisconnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_do_not_know']/label/span[2]")
	public WebElement lblSupplyUnknown;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_yes']/label/span[1]/input")
	public WebElement supplyConnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_yes']/label/span[1]/input")
	public List<WebElement> supplyConnectedList;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterSupplyConnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerSupplyConnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_no']/label/span[1]/input")
	public WebElement supplyDisconnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_no']/label/span[1]/input")
	public List<WebElement> supplyDisconnectedList;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterSupplyDisconnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerSupplyDisconnected;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_do_not_know']/label/span[1]/input")
	public WebElement supplyUnknown;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_do_not_know']/label/span[1]/input")
	public List<WebElement> supplyUnknownList;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_do_not_know']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterSupplyUnknown;
	
	@FindBy(xpath = "//mat-radio-button[@name='supply_state_do_not_know']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerSupplyUnknown;

	@FindBy(name = "label_life_support")
	public WebElement lblLifeSupHeader;
	
	@FindBy(name = "life_support_introduction")
	public WebElement lblLifeSupIntro;
	
	@FindBy(name = "life_support_introduction")
	public List<WebElement> lblLifeSupIntroList;
	
	@FindBy(name = "label_life_support_upload")
	public WebElement lblLifeSupQuestion;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_yes']/label/span[1]/input")
	public WebElement lifeSupYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_yes']/label/span[1]/input")
	public List<WebElement> lifeSupYesList;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_yes']/label/span[2]")
	public WebElement lblLifeSupYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterLifeSupYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerLifeSupYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_no']/label/span[1]/input")
	public WebElement lifeSupNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_no']/label/span[1]/input")
	public List<WebElement> lifeSupNoList;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_no']/label/span[2]")
	public WebElement lblLifeSupNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterLifeSupNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='life_support_upload_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerLifeSupNo;
	
	@FindBy(name = "label_life_support_select_equipment")
	public WebElement lifeSupEquipIntro;
	
	// the checkboxes for the Life Support equipment
	@FindBy(name = "selected_equipment")
	public List<WebElement> lifeSuppEquipOptions;
	
	@FindBy(name = "input_other_equipment")
	public WebElement lifeSuppOtherInput;
	
	@FindBy(xpath = "//mat-form-field[@name='field_other_equipment']/div/div[2]")
	public WebElement underlineLifeSuppOtherInput;
	
	@FindBy(name = "hint_life_support_equipment")
	public WebElement hintLifeSuppOtherTextField;
	
	@FindBy(name = "label_medical_cooling_upload")
	public WebElement lblMedCoolingQuestion;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_yes']/label/span[1]/input")
	public WebElement medCoolingYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_yes']/label/span[1]/input")
	public List<WebElement> medCoolingYesList;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterMedCoolingYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerMedCoolingYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_yes']/label/span[2]")
	public WebElement lblMedCoolingYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_no']/label/span[1]/input")
	public WebElement medCoolingNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_no']/label/span[1]/input")
	public List<WebElement> medCoolingNoList;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterMedCoolingNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerMedCoolingNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='medical_cooling_upload_no']/label/span[2]")
	public WebElement lblMedCoolingNo;
	
	// this is where the text 'Drag-and-drop file here or click to browse for file'
	// is located which we include in the assertion
	@FindBy(id = "dropArea")
	public WebElement dragAndDropText;
	
	// this is the drag and drop section for life support and medical cooling
	@FindBy(xpath = "//ejs-uploader[@name='life_support_uploader']/div")
	public WebElement dragAndDropArea;
	
	@FindBy(xpath = "//ejs-uploader[@name='life_support_uploader']/div")
	public List<WebElement> dragAndDropAreaList;
	
	// this is the list of elements for the files uploaded for life support and medical cooling
	@FindBy(xpath = "//ejs-uploader[@name='life_support_uploader']/div/ul[@class='e-upload-files']/li[starts-with(@class,'e-upload-file-list')]")
	public List<WebElement> dragAndDropUploadedFiles;
	
	@FindBy(id = "life_support_open_uploader")
	public WebElement linkDragAndDropClickToBrowse;
	
	@FindBy(xpath = "//app-supply-details//app-uploader/div[starts-with(@class,'uploadfile')]")
	public WebElement dragAndDropBorder;
	
	// this is the error when the max upload files limit is reached
	@FindBy(name = "hint_life_support_file_count_exceed")
	public WebElement dragAndDropCountExceedError;
	
	// this is the error when there are duplicate files uploaded
	@FindBy(name = "hint_life_support_duplicate_found")
	public WebElement dragAndDropDuplicateError;
	
	@FindBy(xpath = "//mat-dialog-container[starts-with(@id,'mat-dialog-')]")
	public WebElement dialogContainer;
	
	// this is when removing uploaded files
	@FindBy(xpath = "//div[@name='dialog_content']/p")
	public WebElement dialogContainerText;
	
	@FindBy(name = "dialog_button_yes")
	public WebElement dialogRemoveFileYes;
	
	@FindBy(name = "dialog_button_no")
	public WebElement dialogRemoveFileNo;
	
	@FindBy(name = "button_next_supply_details")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public SupplyDetailsMoveIn(WebDriver driver) {
		
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
	public SupplyDetailsMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
