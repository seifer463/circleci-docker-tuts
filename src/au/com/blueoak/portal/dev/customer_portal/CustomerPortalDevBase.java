package au.com.blueoak.portal.dev.customer_portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.blueoak.portal.dev.DevBaseTesting;
import au.com.blueoak.portal.utility.AccessS3BucketWithVfs;

public class CustomerPortalDevBase extends DevBaseTesting {
	
	/** 
	 * Color is darkblue, #00008B
	 * */
	private String textColorWithOpacity = "rgba(0, 0, 139, 1)";
	
	/** 
	 * Color is chocolate, #D2691E
	 * */
	private String labelColorWithOpacity = "rgba(210, 105, 30, 1)";
	
	/** 
	 * Color is pink, #FFC0CB
	 * */
	private String mainColorWithOpacity = "rgba(255, 192, 203, 1)";
	
	/** 
	 * Font family light
	 * */
	private String fontFamilyLight = "\"Boing Light\", \"Helvetica Neue\", sans-serif";
	
	/** 
	 * Font family bold
	 * */
	private String fontFamilySemibold = "\"Boing Medium\", \"Helvetica Neue\", sans-serif";
	
	/** 
	 * 
	 * */
	private String fontSizeSmallTxt = "12px";
	
	/** 
	 * 
	 * */
	private String fontSizeRegularTxt = "16px";
	
	/** 
	 * 
	 * */
	private String fontSizeLargeTxt = "22px";
	
	/** 
	 * 
	 * */
	public List<String> mainHeaderExpCss = new ArrayList<>(
			Arrays.asList(
					textColorWithOpacity,
					fontSizeLargeTxt,
					fontFamilySemibold));
	
	/** 
	 * 
	 * */
	public List<String> smallTextExpCss = new ArrayList<>(
			Arrays.asList(
					mainColorWithOpacity,
					fontSizeSmallTxt,
					fontFamilyLight));
	
	/** 
	 *
	 * */
	public List<String> placeholderExpCss = new ArrayList<>(
			Arrays.asList(
					labelColorWithOpacity,
					fontSizeRegularTxt,
					fontFamilyLight));
	
	/** 
	 * 
	 * */
	public List<String> buttonExpCss = new ArrayList<>(
			Arrays.asList(
					mainColorWithOpacity,
					"rgba(240, 255, 240, 1)",
					fontSizeRegularTxt,
					fontFamilyLight));
	
	/** 
	 * 
	 * */
	public String iconExpCss = mainColorWithOpacity;
	
	/** 
	 * 
	 * */
	public String underlineExpCssNotFocused = labelColorWithOpacity;
	
	/** 
	 * Constructor when initializing this class
	 * */
	public CustomerPortalDevBase() {
		
		super.setupTestProp();
	}
	
	/** 
	 * Upload Customer Portal specific custom CSS
	 * */
	public void uploadCustomerPortalCustomCss(AccessS3BucketWithVfs s3Access) {

		uploadCustomCss(s3Access, CUSTOMER_PORTAL_CSS_DIR, "customer_portal.css");
	}
	
	/** 
	 * Upload Customer Portal specific custom language files
	 * */
	public void uploadCustomerPortalCustomLangFile(AccessS3BucketWithVfs s3Access, String dirNum,
			String s3FileNameToReplaceOrUpload) {

		uploadCustomLangFile(s3Access, CUSTOMER_PORTAL_CUSTOM_LANG_FILES_DIR, PortalNamesEnum.CustomerPortal, dirNum,
				s3FileNameToReplaceOrUpload);
	}
	
	/**
	 * Upload Customer Portal specific portal config json file
	 */
	public void uploadCustomerPortalConfig(AccessS3BucketWithVfs s3Access, String directoryNum,
			String fileToUploadOrReplace) {

		uploadConfig(s3Access, CUSTOMER_PORTAL_CONFIGS_DIR, directoryNum, fileToUploadOrReplace);
	}

}