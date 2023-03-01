MOVE-IN TESTING
	• The enabled address_lookup for the portal configs are only in the following directories:
		• 01
		• 39
		• 41

DEVELOPMENT TESTING
	• ensure that the correct values are set in the TestSuite.properties file
	• ensure that the Dev API server is turned on
	• ensure that the crontab on the Dev API server is running
	• ensure that the engine is running the CRM
	• ensure that the recaptchaKey being used in the portal_config.json is '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI'. That's to ensure that the captcha would not be displayed.
	• ensure that on the Dev API the CRM access credentials is correct
		• url = 'https://selenium-crm.blueacorns.com.au/api.php' (just update this URL per instance)
		• uuid = '679b784009f311eb8ed60a85abb929f8'
		• key = 'ff51ed7d02eb45384702e-ec203d355d'
	• Gmail credentials where Make Payment success or declined emails are sent (aside from success@simulator.amazonses.com dummy email). Right now the test cases do not check the actual email sent
		• testing.portal.blueacorns@gmail.com
		• BlueOak4$
		• Recovery Email: nino.bueno@devtac.ph
		• Recovery SMS: 0906 861 9083
	• for the test virus file, you can visit 'https://www.eicar.org/?page_id=3950' and download 'eicar.com.txt' then update the file extension to .pdf. Make sure you disable your antivirus first before downloading the file. This may cause issues if this file is saved in repository and someone tries to clone.

1) Stand alone Payment Portal that shows all fields
	• 

2) Embedded Payment Portal that shows all fields
	• 



PRODUCTION TESTING


P.S. Hit Alt + Numpad 7 for the bullet icon