package au.com.blueoak.portal.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * These are the constants for the default CSS for the following:
 * - Move In
 * - Move Out
 * - Make Payment
 * - Connection
 * */
public interface CssTestConstantsDefault {

	public static final List<String> MAIN_HEADER_LABEL_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 1)",
					"24px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> SECTION_HEADER_FOCUSED_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 150, 218, 1)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> SECTION_HEADER_NOT_FOCUSED_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 0.87)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> HEADER_LABEL_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 1)",
					"19px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> LABEL1_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 1)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> LABEL2_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 0, 0, 1)",
					"16px",
					"Roboto, \"Helvetica Neue\", sans-serif"));
	
	public static final List<String> PLACEHOLDER_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(95, 95, 95, 1)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> LINK_LABEL1_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 150, 218, 1)",
					"16px",
					"Roboto, \"Helvetica Neue\", sans-serif"));
	
	public static final List<String> LINK_LABEL2_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 123, 255, 1)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> LINK_LABEL_HOVER1_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 150, 218, 1)",
					"16px",
					"Roboto, \"Helvetica Neue\", sans-serif"));
	
	public static final List<String> LINK_LABEL_HOVER2_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 86, 179, 1)",
					"16px",
					"\"Segoe UI\", Regular"));
	
	public static final List<String> NEXT_BUTTON_DEFAULT = new ArrayList<>(
			Arrays.asList(
					"rgba(0, 150, 218, 1)",
					"rgba(255, 255, 255, 1)",
					"12px",
					"\"Segoe UI\", Regular"));
	
	public static final String BODY_BACKGROUND_DEFAULT = "rgba(245, 245, 245, 1)";
	
	public static final String FOOTER_BACKGROUND_DEFAULT = "rgba(255, 255, 255, 1)";
	
	public static final String MATICON_SECTION_HEADER_FOCUSED_DEFAULT = "rgba(0, 150, 218, 1)";
	
	public static final String MATICON_SECTION_HEADER_NOT_FOCUSED_DEFAULT = "rgba(54, 54, 54, 1)";
	
	public static final String DATEPICKER_ICON_VALID_DEFAULT = "rgb(0, 150, 218)";
	
	public static final String UNDERLINE_NOT_FOCUSED_DEFAULT = "rgba(0, 0, 0, 1)";
	
	public static final String GLOBE_ICON_DEFAULT = "rgba(122, 122, 122, 1)";
	
	public static final String RADIO_OUTER_UNTICKED_DEFAULT = "rgba(0, 0, 0, 0.54)";
	
	public static final String RADIO_INNER_DEFAULT = "rgba(0, 150, 218, 1)";
	
	public static final String CHECKBOX_OUTER_TICKED_OR_UNTICKED_DEFAULT = "rgba(0, 0, 0, 0.54)";
	
	public static final String CHECKBOX_INNER_UNTICKED_DEFAULT = "rgba(0, 0, 0, 0)";
	
}
