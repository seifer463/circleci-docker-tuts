package au.com.blueoak.portal.pageObjects.move_in;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class TradeWasteMoveIn {
	
	WebDriver driver;
	
	// this is for checking the whole section header text
	@FindBy(xpath = "//mat-step-header[starts-with(@id,'cdk-step-label-')]")
	public List<WebElement> matStepHeader;
	
	@FindBy(name = "stepper_trade_waste")
	public WebElement header;
	
	// this is for checking if the element exists or not
	@FindBy(name = "stepper_trade_waste")
	public List<WebElement> headerList;
	
	@FindBy(name = "label_trade_waste_discharged")
	public WebElement lblDischargeQuestion;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_yes']/label/span[1]/input")
	public WebElement tradeWasteDischargeYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterTradeWasteDischargeYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerTradeWasteDischargeYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_yes']/label/span[2]")
	public WebElement lblTradeWasteDischargeYes;
		
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_no']/label/span[1]/input")
	public WebElement tradeWasteDischargeNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterTradeWasteDischargeNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerTradeWasteDischargeNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_discharged_no']/label/span[2]")
	public WebElement lblTradeWasteDischargeNo;
	
	@FindBy(name = "label_trade_waste_equipment")
	public WebElement lblEquipmentQuestion;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_yes']/label/span[1]/input")
	public WebElement tradeWasteEquipYes;
	
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_yes']/label/span[1]/input")
	public List<WebElement> tradeWasteEquipYesList;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_yes']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterTradeWasteEquipYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_yes']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerTradeWasteEquipYes;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_yes']/label/span[2]")
	public WebElement lblTradeWasteEquipYes;
	
	// this is for checking if the element exists or not
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_no']/label/span[1]/input")
	public WebElement tradeWasteEquipNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_no']/label/span[1]/input")
	public List<WebElement> tradeWasteEquipNoList;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_no']/label/span[1]/span[@class='mat-radio-outer-circle']")
	public WebElement radioOuterTradeWasteEquipNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_no']/label/span[1]/span[@class='mat-radio-inner-circle']")
	public WebElement radioInnerTradeWasteEquipNo;
	
	@FindBy(xpath = "//mat-radio-button[@name='trade_waste_equipment_no']/label/span[2]")
	public WebElement lblTradeWasteEquipNo;
	
	@FindBy(name = "label_trade_waste_select_equipment")
	public WebElement lblSelectTradeWasteEquipment;
	
	// the checkboxes for the trade waste equipments
	@FindBy(name = "selected_option_trade_waste_equipment")
	public List<WebElement> tradeWasteEquipOptions;
	
	@FindBy(name = "trade_waste_equipment_value")
	public WebElement tradeWasteOtherTextField;
	
	@FindBy(xpath = "//mat-form-field[@name='field_trade_waste_equipment_value']/div/div[2]")
	public WebElement underlineTradeWasteOtherTextField;
	
	@FindBy(name = "hint_trade_waste_equipment")
	public WebElement hintTradeWasteOtherTextField;
	
	@FindBy(name = "label_business_activity")
	public WebElement floaterLblBusinessActivity;
	
	@FindBy(name = "select_business_activity")
	public WebElement businessActivity;
	
	// this is for checking if the field exists or not
	@FindBy(name = "select_business_activity")
	public List<WebElement> businessActivityList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_business_activity']/div/div[2]")
	public WebElement underlineBusinessActivity;
	
	@FindBy(name = "hint_business_activity")
	public WebElement hintBusinessActivity;
	
	@FindBy(name = "retail_food_business")
	public WebElement optionRetailFood;
	
	@FindBy(name = "retail_motor_vehicle")
	public WebElement optionRetailMotor;
	
	@FindBy(name = "other_business_activity")
	public WebElement optionOther;
	
	// this is for the div where the business activity options are housed
	@FindBy(xpath = "//div[starts-with(@id,'mat-select-') and @role='listbox']")
	public WebElement businessActivityDiv;
	
	@FindBy(xpath = "//mat-select[@name='select_business_activity']/div/div[1]/span")
	public WebElement lblColorBusinessActivity;
	
	@FindBy(name = "label_discharge_information")
	public WebElement lblDischargeInfoHeader;
	
	@FindBy(name = "label_discharge_information")
	public List<WebElement> lblDischargeInfoHeaderList;
	
	@FindBy(name = "label_discharge_introduction")
	public WebElement lblDischargeInfoIntro;
	
	@FindBy(name = "label_discharge_introduction")
	public List<WebElement> lblDischargeInfoIntroList;
	
	@FindBy(name = "label_max_flow_rate")
	public WebElement floaterLblMaxFlowRate;
	
	@FindBy(name = "max_flow_rate")
	public WebElement maxFlowRate;
	
	@FindBy(name = "max_flow_rate")
	public List<WebElement> maxFlowRateList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_max_flow_rate']/div/div[2]")
	public WebElement underlineMaxFlowRate;
	
	@FindBy(name = "label_max_discharge_volume")
	public WebElement floaterLblMaxDischargeVolume;
	
	@FindBy(name = "max_discharge_volume")
	public WebElement maxDischargeVolume;
	
	@FindBy(name = "max_discharge_volume")
	public List<WebElement> maxDischargeVolumeList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_max_discharge_volume']/div/div[2]")
	public WebElement underlineMaxDischargeVolume;
	
	@FindBy(name = "label_discharge_start_date")
	public WebElement floaterLblDischargeStartDate;
	
	@FindBy(name = "input_discharge_start_date")
	public WebElement dischargeStartDate;
	
	@FindBy(name = "input_discharge_start_date")
	public List<WebElement> dischargeStartDateList;
	
	@FindBy(xpath = "//mat-form-field[@name='field_discharge_start_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button")
	public WebElement datePickerDischargeStartDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_discharge_start_date']//mat-datepicker-toggle[starts-with(@class,'mat-datepicker-toggle')]/button/span//*[local-name()='svg']/*[local-name()='path']")
	public WebElement iconDischargeStartDate;
	
	@FindBy(xpath = "//mat-form-field[@name='field_discharge_start_date']/div/div[2]")
	public WebElement underlineDischargeStartDate;
	
	@FindBy(name = "label_discharge_days")
	public WebElement lblDischargeDays;
	
	// for checking if the elements exists
	@FindBy(name = "label_discharge_days")
	public List<WebElement> lblDischargeDaysList;
	
	// this is for checking for the entire group of the Discharge Days
	@FindBy(name = "selection_list_days_of_week")
	public List<WebElement> dischargeDaysCheckboxGroupList;
	
	// these are the elements for the Discharge Days
	@FindBy(name = "selected_option_day_of_week")
	public List<WebElement> dischargeDaysOptions;
	
	@FindBy(xpath = "//div[@class='start-hours']/p")
	public WebElement lblDischargeHoursStart;
	
	@FindBy(xpath = "//div[@class='start-hours']/p")
	public List<WebElement> lblDischargeHoursStartList;
	
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[1]/input")
	public WebElement dischargeHoursStartHour;
	
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[1]/input")
	public List<WebElement> dischargeHoursStartHourList;
	
	// this is the arrow up button to increment the start hours
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[1]/button[1]")
	public WebElement dischargeHoursStartHourInc;
	
	// this is the arrow down button to decrement the start hours
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[1]/button[2]")
	public WebElement dischargeHoursStartHourDec;
	
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[3]/input")
	public WebElement dischargeHoursStartMin;
	
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[3]/input")
	public List<WebElement> dischargeHoursStartMinList;
	
	// this is the arrow up button to increment the start minutes
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[3]/button[1]")
	public WebElement dischargeHoursStartMinInc;
	
	// this is the arrow up button to decrement the start minutes
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[3]/button[2]")
	public WebElement dischargeHoursStartMinDec;
	
	@FindBy(xpath = "//div[@class='start-hours']/ngb-timepicker/fieldset/div/div[5]/button")
	public WebElement dischargeHoursStartAmPm;
	
	// this is when it's in error state
	@FindBy(name = "hint_discharge_start_hour")
	public WebElement hintDischargeHoursStart;
	
	@FindBy(xpath = "//div[@class='end-hours']/p")
	public WebElement lblDischargeHoursEnd;
	
	@FindBy(xpath = "//div[@class='end-hours']/p")
	public List<WebElement> lblDischargeHoursEndList;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[1]/input")
	public WebElement dischargeHoursEndHour;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[1]/input")
	public List<WebElement> dischargeHoursEndHourList;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[1]/button[1]")
	public WebElement dischargeHoursEndHourInc;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[1]/button[2]")
	public WebElement dischargeHoursEndHourDec;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[3]/input")
	public WebElement dischargeHoursEndMin;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[3]/input")
	public List<WebElement> dischargeHoursEndMinList;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[3]/button[1]")
	public WebElement dischargeHoursEndMinInc;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[3]/button[2]")
	public WebElement dischargeHoursEndMinDec;
	
	@FindBy(xpath = "//div[@class='end-hours']/ngb-timepicker/fieldset/div/div[5]/button")
	public WebElement dischargeHoursEndAmPm;
	
	// this is when it's in error state
	@FindBy(name = "hint_discharge_end_hour")
	public WebElement hintDischargeHoursEnd;
	
	@FindBy(name = "trade_waste_attachments_text")
	public WebElement lblTradeWasteAttachmentIntro;
	
	@FindBy(name = "trade_waste_attachments_text")
	public List<WebElement> lblTradeWasteAttachmentIntroList;
	
	@FindBy(xpath = "//p[@name='trade_waste_attachments_text']/p/a")
	public WebElement linkLblTradeWasteAttachmentIntro;
	
	// this is where the text 'Drag-and-drop file here or click to browse for file'
	// is located which we include in the assertion
	@FindBy(xpath = "//app-trade-waste/app-uploader/div/div[@id='dropArea']")
	public WebElement dragAndDropText;
	
	// this is the drag and drop section for Trade Waste
	@FindBy(xpath = "//ejs-uploader[@name='trade_waste_uploader']/div")
	public WebElement dragAndDropArea;
	
	// this is for checking if the element exists or not
	@FindBy(xpath = "//ejs-uploader[@name='trade_waste_uploader']/div")
	public List<WebElement> dragAndDropAreaList;
	
	// this is the list of elements for the files uploaded for Trade Waste
	@FindBy(xpath = "//ejs-uploader[@name='trade_waste_uploader']/div/ul[@class='e-upload-files']/li[starts-with(@class,'e-upload-file-list')]")
	public List<WebElement> dragAndDropUploadedFiles;
	
	@FindBy(id = "trade_waste_open_uploader")
	public WebElement linkDragAndDropClickToBrowse;
	
	@FindBy(xpath = "//app-trade-waste//app-uploader/div[starts-with(@class,'uploadfile')]")
	public WebElement dragAndDropBorder;
	
	@FindBy(name = "hint_trade_waste_file_count_exceed")
	public WebElement dragAndDropCountExceedError;
	
	@FindBy(xpath = "//mat-dialog-container[starts-with(@id,'mat-dialog-')]")
	public WebElement dialogContainer;
	
	// this is when removing uploaded files
	@FindBy(xpath = "//div[@name='dialog_content']/p")
	public WebElement dialogContainerText;
	
	@FindBy(name = "dialog_button_yes")
	public WebElement dialogRemoveFileYes;
	
	@FindBy(name = "dialog_button_no")
	public WebElement dialogRemoveFileNo;
	
	@FindBy(name = "button_prev_trade_waste")
	public WebElement previous;
	
	@FindBy(name = "button_next_trade_waste")
	public WebElement next;
	
	// this is for checking if the css is correct
	// for the label/placeholder
	@FindBy(xpath = "//label[starts-with(@id,'mat-form-field-label-')]")
	public List<WebElement> labelInput;
	
	public TradeWasteMoveIn(WebDriver driver) {
		
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
	public TradeWasteMoveIn(WebDriver driver, int implicitWaitInSec) {
		
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(implicitWaitInSec, TimeUnit.SECONDS);
		PageFactory.initElements(driver, this);
	}

}
