package au.com.blueoak.portal.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * This is the constants for checking the custom CSS for the following:
 * - Move In
 * - Move Out
 * - Make Payment
 * - Connection
 * */
public interface CssTestConstantsCustom {
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String MAIN_COLOR_WITH_OPACITY_CSTM = "rgba(255, 140, 0, 1)";
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String MAIN_COLOR_WITHOUT_OPACITY_CSTM = "rgb(255, 140, 0)";
	
	/** 
	 * Color is green, #008000
	 * */
	public static final String TEXT_COLOR_WITH_OPACITY_CSTM = "rgba(0, 128, 0, 1)";
	
	/** 
	 * Color is green, #008000
	 * */
	public static final String TEXT_COLOR_WITHOUT_OPACITY_CSTM = "rgb(0, 128, 0)";

	/** 
	 * Color is fuchsia/magenta, #FF00FF
	 * */
	public static final String LABEL_COLOR_WITH_OPACITY_CSTM = "rgba(255, 0, 255, 1)";
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String ERROR_COLOR_WITH_OPACITY_CSTM = "rgba(0, 0, 139, 1)";
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String ERROR_COLOR_WITHOUT_OPACITY_CSTM = "rgb(0, 0, 139)";
	
	/** 
	 * Color is crimson, #DC143C
	 * */
	public static final String DISABLED_COLOR_WITH_OPACITY_CSTM = "rgba(220, 20, 60, 1)";
	
	/** 
	 * This is the lining in the box when displaying in standalone
	 * and when there are pop op dialogs displayed
	 * 
	 * mediumaquamarine, #66CDAA
	 * */
	public static final String BOX_SHADOW_BORDER_CSTM = "rgb(102, 205, 170) 0px 0px 10px 0px";
	
	/** 
	 * Font family light
	 * */
	public static final String FONT_LIGHT_CSTM = "\"Boing Light\", \"Helvetica Neue\", sans-serif";
	
	/** 
	 * Font family bold
	 * */
	public static final String FONT_SEMIBOLD_CSTM = "\"Boing Semibold\", \"Helvetica Neue\", sans-serif";
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> MAIN_HEADER_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"25px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> SECTION_HEADER_FOCUSED_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkviolet, #9400D3
	 * */
	public static final List<String> SECTION_HEADER_NOT_FOCUSED_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(148, 0, 211, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * This is the expected headers for some display label headers.
	 * 
	 * Color is green, #008000
	 * */
	public static final List<String> HEADER_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"20px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> LABEL_MW_FRAME_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is fuchsia/magenta, #FF00FF
	 * */
	public static final List<String> PLACEHOLDER_CSTM = new ArrayList<>(
			Arrays.asList(
					LABEL_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final List<String> PLACEHOLDER_ERROR_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is fuchsia/magenta, #FF00FF
	 * */
	public static final List<String> PLACEHOLDER_MW_FRAME_CSTM = new ArrayList<>(
			Arrays.asList(
					LABEL_COLOR_WITH_OPACITY_CSTM,
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> FLOATER_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 *
	 * */
	public static final List<String> FLOATER_LABEL_ERROR_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> FLOATER_LABEL_MW_FRAME_FOCUSED_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> FLOATER_LABEL_MW_FRAME_NOT_FOCUSED_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"12.8px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> FLOATER_LABEL_MW_FRAME_ERROR_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"12.8px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> DRAG_AND_DROP_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"14px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final List<String> DRAG_AND_DROP_LINK_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"14px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> UPLOADED_FILE_NAME_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"14px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is fuchsia/magenta, #FF00FF
	 * */
	public static final List<String> UPLOADED_FILE_STATUS_CSTM = new ArrayList<>(
			Arrays.asList(
					LABEL_COLOR_WITH_OPACITY_CSTM,
					"12px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is mediumvioletred, #C71585
	 * */
	public static final List<String> UPLOADED_FILE_SIZE_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(199, 21, 133, 0.54)",
					"12px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final List<String> HINT_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 *
	 * */
	public static final List<String> MANUAL_AND_QUICK_ADD_SRCH_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"12px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is sandybrown, #F4A460
	 * */
	public static final List<String> MAT_OPTION_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(244, 164, 96, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Background color is darkorange, #FF8C00, rgba(255, 140, 0, 1)
	 * Color is darkkhaki, #BDB76B, rgba(189, 183, 107, 1)
	 * */
	public static final List<String> NEXT_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(189, 183, 107, 1)",
					"13px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * Background color is tomato, #FF6347, rgba(255, 99, 71, 1)
	 * Color is dimgray, #696969, rgba(105, 105, 105, 1)
	 * */
	public static final List<String> PREVIOUS_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(255, 99, 71, 1)",
					"rgba(105, 105, 105, 1)",
					"13px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * Background color is darkorange, #FF8C00, rgba(255, 140, 0, 1)
	 * Color is steelblue, #4682B4, rgba(70, 130, 180, 1)
	 * */
	public static final List<String> CANCEL_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(70, 130, 180, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * Background color is goldenrod, #DAA520, rgba(218, 165, 32, 1)
	 * Color is bisque, #FFE4C4, rgba(255, 228, 196, 1)
	 * */
	public static final List<String> LAST_PREVIOUS_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(218, 165, 32, 1)",
					"rgba(255, 228, 196, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * Background color is darkorange, #FF8C00, rgba(255, 140, 0, 1)
	 * Color is thistle, #D8BFD8, rgba(216, 191, 216, 1)
	 * */
	public static final List<String> SUBMIT_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(216, 191, 216, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * oldlace, #FDF5E6, rgba(253, 245, 230, 1)
	 * */
	public static final List<String> ACCT_NUM_CLOSE_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(253, 245, 230, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Background color is crimson, #DC143C, rgba(220, 20, 60, 1)
	 * Color is steelblue, #4682B4, rgba(70, 130, 180, 1)
	 * */
	public static final List<String> CANCEL_BUTTON_DISABLED_CSTM = new ArrayList<>(
			Arrays.asList(
					DISABLED_COLOR_WITH_OPACITY_CSTM,
					"rgba(70, 130, 180, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * Background color is crimson, #DC143C, rgba(220, 20, 60, 1)
	 * Color is thistle, #D8BFD8, rgba(216, 191, 216, 1)
	 * */
	public static final List<String> SUBMIT_BUTTON_DISABLED_CSTM = new ArrayList<>(
			Arrays.asList(
					DISABLED_COLOR_WITH_OPACITY_CSTM,
					"rgba(216, 191, 216, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * khaki, #F0E68C, rgba(240, 230, 140, 1)
	 * */
	public static final List<String> TRY_AGAIN_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(240, 230, 140, 1)",
					"14px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * darkseagreen, #8FBC8F, rgb(143,188,143)
	 * */
	public static final List<String> DIALOG_YES_AND_OK_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"rgba(143, 188, 143, 1)",
					"13px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * wheat, #F5DEB3, rgb(245,222,179)
	 * olive, #808000, rgb(128,128,0)
	 * */
	public static final List<String> DIALOG_NO_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 255, 0, 1)",
					"rgba(128, 0, 0, 1)",
					"13px",
					FONT_SEMIBOLD_CSTM));
	
	/** 
	 * box-shadow is mediumaquamarine, #66CDAA, rgb(102,205,170)
	 * background is white, #FFFFFF, rgb(255,255,255)
	 * */
	public static final List<String> DIALOG_CONTAINER_CSTM = new ArrayList<>(
			Arrays.asList(
					BOX_SHADOW_BORDER_CSTM,
					"rgb(255, 255, 255) none repeat scroll 0% 0% / auto padding-box border-box",
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkolivegreen, #556B2F
	 * */
	public static final List<String> DIALOG_CONTAINER_HEADER_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(85, 107, 47, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is orangered, #FF4500
	 * */
	public static final List<String> LINK_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(255, 69, 0, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is teal, #008080
	 * */
	public static final List<String> LINK_LABEL_HOVER_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 128, 128, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> NOTIF_HEADER_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"14px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is green, #008000
	 * */
	public static final List<String> ADD_REMOVE_CONTACT_BUTTON_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is saddlebrown, #8B4513
	 * */
	public static final List<String> PROCESSING_REQUEST_MSG_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(139, 69, 19, 1)",
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> CHANGE_OR_CANCEL_CC_DETAILS_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"13px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> NOTE_LENGTH_COUNTER_CSTM = new ArrayList<>(
			Arrays.asList(
					TEXT_COLOR_WITH_OPACITY_CSTM,
					"14px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> UPDATE_LINK_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> DISCHARGE_AM_PM_BTN_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 0)",
					MAIN_COLOR_WITHOUT_OPACITY_CSTM,
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> DISCHARGE_AM_PM_BTN_HOVER_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					MAIN_COLOR_WITHOUT_OPACITY_CSTM,
					"rgba(240, 255, 255, 1)",
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkmagenta, #8B008B
	 * */
	public static final List<String> NOT_AVAIL_NOTIF_TYPE_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(139, 0, 139, 1)",
					"12px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * rgba(175, 238, 238, 1), paleturquoise, #AFEEEE
	 * rgb(210, 180, 140), tan, #D2B48C
	 * 
	 * */
	public static final List<String> PROCESSING_FEE_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(175, 238, 238, 1)",
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"1px solid rgb(210, 180, 140)",
					"15px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> PROCESSING_FEE_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> PAY_TOTAL_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					MAIN_COLOR_WITH_OPACITY_CSTM,
					"21px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * darkolivegreen, #556B2F, rgba(85, 107, 47, 1)
	 * */
	public static final List<String> RESPONSE_HEADER_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(85, 107, 47, 1)",
					"19px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * 
	 * */
	public static final List<String> RESPONSE_MW_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					ERROR_COLOR_WITH_OPACITY_CSTM,
					"17px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * palegreen, #98FB98, rgba(152, 251, 152, 1)
	 * */
	public static final List<String> EMAIL_SENT_TO_LABEL_CSTM = new ArrayList<>(
			Arrays.asList(
					"rgba(152, 251, 152, 1)",
					"16px",
					FONT_LIGHT_CSTM));
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String MATICON_SECTION_HEADER_FOCUSED_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is salmon, #FA8072
	 * */
	public static final String MATICON_SECTION_HEADER_NOT_FOCUSED_CSTM = "rgba(250, 128, 114, 1)";
	
	/** 
	 * This is the expected CSS for the underline
	 * on input fields when cursor is not focused.
	 * 
	 * Color is green, #008000
	 * */
	public static final String UNDERLINE_NOT_FOCUSED_CSTM = TEXT_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * This is the expected CSS for the underline
	 * on input fields when cursor is focused.
	 * 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String UNDERLINE_FOCUSED_OR_VALID_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * This is the expected CSS for the underline
	 * on input fields when cursor is not focused
	 * and in error state.
	 * 
	 * Color is darkblue, #00008B
	 * */
	public static final String UNDERLINE_ERROR_CSTM = ERROR_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String UNDERLINE_MW_FRAME_FOCUSED_OR_VALID_CSTM = MAIN_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String UNDERLINE_MW_FRAME_WITH_PROPERTY_FOCUSED_OR_VALID_CSTM = "1px solid ".concat(MAIN_COLOR_WITHOUT_OPACITY_CSTM);
	
	/** 
	 * 
	 * */
	public static final String UNDERLINE_MW_FRAME_NOT_FOCUSED_CSTM = "1px solid ".concat(TEXT_COLOR_WITHOUT_OPACITY_CSTM);
	
	/** 
	 * 
	 * */
	public static final String UNDERLINE_MW_FRAME_ERROR_CSTM = ERROR_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String UNDERLINE_MW_FRAME_WITH_PROPERTY_ERROR_CSTM = "1px solid ".concat(ERROR_COLOR_WITHOUT_OPACITY_CSTM);
	
	/** 
	 * Expected CSS for borders of the radio buttons
	 * when nothing is selected
	 * 
	 * Color is gold, #FFD700
	 * */
	public static final String RADIO_OUTER_UNTICKED_CSTM = "rgba(255, 215, 0, 0.54)";
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String RADIO_OUTER_TICKED_CSTM = MAIN_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * Expected CSS for borders of the radio buttons
	 * when in error state and nothing selected
	 * 
	 * Color is darkblue, #00008B
	 * */
	public static final String RADIO_OUTER_ERROR_CSTM = ERROR_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String RADIO_INNER_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is gold, #FFD700
	 * */
	public static final String CHECKBOX_OUTER_TICKED_OR_UNTICKED_CSTM = "rgba(255, 215, 0, 0.54)";
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String CHECKBOX_OUTER_ERROR_CSTM = ERROR_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String PSEUDOCHECKBOX_OUTER_ERROR_CSTM = ERROR_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String CHECKBOX_INNER_TICKED_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is black, #000000
	 * */
	public static final String CHECKBOX_INNER_UNTICKED_CSTM = "rgba(0, 0, 0, 0)";
	
	/** 
	 * Color is peachpuff, #FFDAB9
	 * */
	public static final String GLOBE_ICON_CSTM = "rgba(255, 218, 185, 1)";
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String GLOBE_ICON_VALID_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String GLOBE_ICON_ERROR_CSTM = ERROR_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String DATEPICKER_ICON_VALID_CSTM = MAIN_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String DATEPICKER_ICON_ERROR_CSTM = ERROR_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * Color is lavender, #E6E6FA
	 * */
	public static final String MATSELECT_OR_MATOPTION_VALUES_BCKGRND_CSTM = "rgb(230, 230, 250) none repeat scroll 0% 0% / auto padding-box border-box";

	/** 
	 * Color is lightyellow, #FFFFE0
	 * */
	public static final String GOOGLELOOKUP_CANNOTFIND_BCKGRND_CSTM = "rgba(255, 255, 224, 1)";
	
	/** 
	 * Color is powderblue, #B0E0E6
	 * */
	public static final String UPLOAD_AREA_BORDER_CSTM = "1px solid rgb(176, 224, 230)";
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	public static final String UPLOAD_AREA_BORDER_ERROR_CSTM = "1px solid ".concat(ERROR_COLOR_WITHOUT_OPACITY_CSTM);
	
	/** 
	 * Color is green, #008000
	 * */
	public static final String NOTIFICATION_ICON_CSTM = TEXT_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is darkorange, #FF8C00
	 * */
	public static final String ADD_ANOTHER_CONTACT_ICON_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String SPINNER_ICON_CSTM = MAIN_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String PROGRESS_BAR_INITIAL_CSTM = MAIN_COLOR_WITHOUT_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String PROGRESS_BAR_REMAINING_CSTM = "rgb(46, 139, 87)";
	
	/** 
	 * 
	 * */
	public static final String CHANGE_OR_CANCEL_CC_ICON_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * 
	 * */
	public static final String DISCHARGE_HRS_INC_DEC_BTN_CSTM = MAIN_COLOR_WITH_OPACITY_CSTM;
	
	/** 
	 * Color is lightseagreen, #20B2AA
	 * */
	public static final String DISCHARGE_HRS_INPUT_BORDER_CSTM = "rgb(32, 178, 170)";
	
	/** 
	 * Color is snow, #FFFAFA
	 * */
	public static final String OVERLAY_BACKDROP_CSTM = "rgba(255, 250, 250, 1)";
	
	/** 
	 * Color is lavenderblush, #FFF0F5
	 * */
	public static final String BODY_BACKGROUND_CSTM = "rgba(255, 240, 245, 1)";
	
	/** 
	 * Color is palegoldenrod, #EEE8AA
	 * */
	public static final String FOOTER_BACKGROUND_CSTM = "rgba(238, 232, 170, 1)";
	
	/** 
	 * Color is cornsilk, #FFF8DC
	 * */
	public static final String UPLOADED_ICON_CSTM = "rgba(255, 248, 220, 1)";
	
	/** 
	 * 
	 * */
	public static final String HELP_ICON_CSTM = "rgba(205, 92, 92, 1)";
	
	/** 
	 * <pre>
	 * This is script to from the mat progress bar, to get the initial progress color for Move In
	 * (e.g. when initializing MW Payframe, submitting request, etc...).
	 * 
	 * How to read the element location starting from the left:
	 * Example: mat-progress-bar.mat-progress-bar.progressBar.mat-warn > div > div.mat-progress-bar-primary.mat-progress-bar-fill.mat-progress-bar-element 
	 * - mat-progress-bar is the tag name
	 * - mat-progress-bar.progressBar.mat-warn is the class
	 * - use dot(.) to combine the tag name and the class
	 * - use > to denote the descendant tag
	 * 
	 * If for example there's a space in the class name, for example class='mat-progress-bar progressBar mat-warn',
	 * 		you should replace spaces with dot(.)
	 * </pre>
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG = "return window.getComputedStyle(document.querySelector('mat-progress-bar.mat-progress-bar.progressBar.mat-warn > div > div.mat-progress-bar-primary.mat-progress-bar-fill.mat-progress-bar-element'),':after').getPropertyValue('background-color')";
	
	/** 
	 * <pre>
	 * This is script to from the mat progress bar, to get the remaining progress color for Move In
	 * (e.g. when initializing MW Payframe, submitting request, etc...).
	 * 
	 * How to read the element location starting from the left:
	 * Example: mat-progress-bar.mat-progress-bar.progressBar.mat-warn > div > div.mat-progress-bar-primary.mat-progress-bar-fill.mat-progress-bar-element 
	 * - mat-progress-bar is the tag name
	 * - mat-progress-bar.progressBar.mat-warn is the class
	 * - use dot(.) to combine the tag name and the class
	 * - use > to denote the descendant tag
	 * 
	 * If for example there's a space in the class name, for example class='mat-progress-bar progressBar mat-warn',
	 * 		you should replace spaces with dot(.)
	 * </pre>
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG = "return window.getComputedStyle(document.querySelector('mat-progress-bar.mat-progress-bar.progressBar.mat-warn > div > div.mat-progress-bar-buffer.mat-progress-bar-element')).getPropertyValue('background-color')";

	/** 
	 * This is script to from the mat progress bar, to get the initial progress color
	 * when submitting a Make Payment
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG_MP = "return window.getComputedStyle(document.querySelector('#progress_bar > div > div.mat-progress-bar-primary.mat-progress-bar-fill.mat-progress-bar-element'),':after').getPropertyValue('background-color')";
	
	/** 
	 * This is script to from the mat progress bar, to get the remaining progress color
	 * when submitting a Make Payment
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG_MP = "return window.getComputedStyle(document.querySelector('#progress_bar > div > div.mat-progress-bar-buffer.mat-progress-bar-element')).getPropertyValue('background-color')";
	
	/** 
	 * This is script to from the mat progress bar, to get the initial progress color
	 * when clicking Next
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_INITIAL_PROG_NXT = "return window.getComputedStyle(document.querySelector('#progress_bar1 > div > div.mat-progress-bar-primary.mat-progress-bar-fill.mat-progress-bar-element'),':after').getPropertyValue('background-color')";
	
	/** 
	 * This is script to from the mat progress bar, to get the remaining progress color
	 * when clicking Next
	 * */
	public static final String SCRIPT_FOR_PROGRESS_BAR_REMAINING_PROG_NXT = "return window.getComputedStyle(document.querySelector('#progress_bar1 > div > div.mat-progress-bar-buffer.mat-progress-bar-element')).getPropertyValue('background-color')";
	
}
