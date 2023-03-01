package au.com.blueoak.portal.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang3.StringUtils;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Utility for interacting with an Email application
 */
public class AccessEmail {

	private Folder folder;

	public enum EmailFolder {
		
		INBOX("INBOX"), SPAM("SPAM");

		private String text;

		private EmailFolder(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
	
	/**
	 * @throws GeneralSecurityException  
	 * 
	 * 
	 * README check https://javaee.github.io/javamail/OAuth2
	 * */
	public Session getSessionGmail() throws GeneralSecurityException {
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File("TestSuite.properties")));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		}
		
		MailSSLSocketFactory sf = new MailSSLSocketFactory();
		sf.setTrustAllHosts(true);
	    props.setProperty("mail.imaps.host", "imap.gmail.com");
	    props.setProperty("mail.store.protocol", "imaps");
	    props.setProperty("mail.imaps.user", "testing.portal.blueacorns@gmail.com");
	    props.setProperty("mail.imaps.password", "BlueOak4$");
	    props.setProperty("mail.imaps.port", "993");
	    props.setProperty("mail.imaps.auth", "true");
	    props.setProperty("mail.imaps.starttls.enable", "true");
	    props.put("mail.imaps.ssl.enable", "true");
	    props.put("mail.imaps.ssl.socketFactory", sf);
	    
	    props.setProperty("mail.debug", "true");
		
	    Authenticator auth = new Authenticator() {
	        @Override
	        public PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication("testing.portal.blueacorns@gmail.com", "BlueOak4$");
	        }
	    };
	    
	    return Session.getDefaultInstance(props, auth);
	}

	/**
	 * Connects to email server with credentials provided to read from a given
	 * folder of the email application
	 * 
	 * @param emailFolder Folder in email application to interact with
	 * 
	 * @throws GeneralSecurityException 
	 * @throws MessagingException 
	 */
	public AccessEmail(EmailFolder emailFolder) throws GeneralSecurityException, MessagingException {
		
		Session gmailSession = this.getSessionGmail();
		gmailSession.setDebug(true);
		Store store = gmailSession.getStore("imaps");
		store.connect();
		folder = store.getFolder(emailFolder.getText());
		folder.open(Folder.READ_ONLY);
	}
	
	/** 
	 * Get the value of the property
	 * */
	public static String getProp(String key) {
		
		if (StringUtils.isBlank(key)) {
			throw (new IllegalArgumentException("Passing an empty/blank/null key is not allowed."));
		}
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream("TestSuite.properties");
			prop.load(input);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		}
		String value = prop.getProperty(key);
		return value;
	}

	/** GET EMAIL PROPERTIES */
	
	public static String getEmailAddressFromProp() {
		return getProp("test_suite_gmail_email");
	}

	public static String getEmailPasswordFromProp() {
		return getProp("test_suite_gmail_password");
	}

	public static String getEmailServerSmtpFromProp() {
		return getProp("test_suite_gmail_email_server_smtp");
	}
	
	public static String getEmailServerImapFromProp() {
		return getProp("test_suite_gmail_email_server_imap");
	}
	
	public static String getEmailServerImapPortFromProp() {
		return getProp("test_suite_gmail_email_server_imap_port");
	}

	/*************** EMAIL ACTIONS ********************/

	/** 
	 * Use this to open an email
	 * */
	public void openEmail(Message message) throws IOException, MessagingException {
		message.getContent();
	}
	
	/** 
	 * Use this to delete all emails or certain emails that contains a specific subject in the specified folder.
	 * Pass null or empty string in the containsSubject if you want to delete all emails.
	 * This is not case sensitive.
	 * */
	public void deleteEmails(String containsSubject, EmailFolder emailFolder) {
		
		Properties properties = new Properties();
		// server setting
		properties.put("mail.imap.host", getEmailServerImapFromProp());
		properties.put("mail.imap.port", getEmailServerImapPortFromProp());

		// SSL setting
		properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.imap.socketFactory.fallback", "false");
		properties.setProperty("mail.imap.socketFactory.port", String.valueOf(getEmailServerImapPortFromProp()));

		Session session = Session.getDefaultInstance(properties);

		try {
			// connects to the message store
			Store store = session.getStore("imap");
			store.connect(getEmailAddressFromProp(), getEmailPasswordFromProp());

			// opens the inbox folder
			Folder folderInbox = store.getFolder(emailFolder.getText());
			folderInbox.open(Folder.READ_WRITE);

			// fetches new messages from server
			Message[] arrayMessages = folderInbox.getMessages();
			
			// check if we are going to delete all email or certain emails only
			if (StringUtils.isBlank(containsSubject)) {
				// we will delete all emails
				for (int i = 0; i < arrayMessages.length; i++) {
					Message message = arrayMessages[i];
					message.setFlag(Flags.Flag.DELETED, true);
				}
			} else {
				// we will delete emails that only contains the subject
				for (int i = 0; i < arrayMessages.length; i++) {
					Message message = arrayMessages[i];
					String subject = message.getSubject();
					subject = subject.toLowerCase();
					if (subject.contains(containsSubject.toLowerCase())) {
						message.setFlag(Flags.Flag.DELETED, true);
					}
				}
			}

			// expunges the folder to remove messages which are marked deleted
			folderInbox.close(true);
			// disconnect
			store.close();
		} catch (NoSuchProviderException nspe) {
			nspe.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}

	public int getNumberOfMessages() throws MessagingException {
		return folder.getMessageCount();
	}

	public int getNumberOfUnreadMessages() throws MessagingException {
		return folder.getUnreadMessageCount();
	}

	/**
	 * Gets a message by its position in the folder. The earliest message is indexed
	 * at 1.
	 */
	public Message getMessageByIndex(int index) throws MessagingException {
		return folder.getMessage(index);
	}
	
	/**
	 * Use this to get the message using the subject
	 * Pass true if you want to search only unread messages.
	 *  */
	public Message getMessageBySubject(String subject, boolean unreadOnly) {
		
		Message msg;
		try {
			msg = getMessagesBySubject(subject, unreadOnly, 1)[0];
		} catch (UnsupportedOperationException uoe) {
			uoe.printStackTrace();
			msg = null;
		} catch (ClassCastException cce) {
			cce.printStackTrace();
			msg = null;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			msg = null;
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			msg = null;
		} catch (MessagingException me) {
			me.printStackTrace();
			msg = null;
		}
		return msg;
	}
	
	/**
	 * Searches for messages with a specific subject
	 * 
	 * @param subject     Subject to search messages for
	 * @param unreadOnly  Indicate whether to only return matched messages that are
	 *                    unread
	 * @param maxToSearch maximum number of messages to search, starting from the
	 *                    latest. For example, enter 100 to search through the last
	 *                    100 messages.
	 */
	public Message[] getMessagesBySubject(String subject, boolean unreadOnly, int maxToSearch)
			throws MessagingException, UnsupportedOperationException, ClassCastException, NullPointerException,
			IllegalArgumentException {
		
		Map<String, Integer> indices = getStartAndEndIndices(maxToSearch);

		Message messages[] = folder.search(new SubjectTerm(subject),
				folder.getMessages(indices.get("startIndex"), indices.get("endIndex")));

		if (unreadOnly) {
			List<Message> unreadMessages = new ArrayList<Message>();
			for (Message message : messages) {
				if (isMessageUnread(message)) {
					unreadMessages.add(message);
				}
			}
			messages = unreadMessages.toArray(new Message[] {});
		}

		return messages;
	}
	
	/** 
	 * Get the earliest message
	 * */
	public Message getEarliestMessage() throws MessagingException {
		return getMessageByIndex(1);
	}

	/** 
	 * Get the recent message
	 * */
	public Message getLatestMessage() throws MessagingException {
		return getMessageByIndex(getNumberOfMessages());
	}

	/**
	 * Gets all messages within the folder
	 */
	public Message[] getAllMessages() throws MessagingException {
		return folder.getMessages();
	}

	/**
	 * @param maxToGet maximum number of messages to get, starting from the latest.
	 *                 For example, enter 100 to get the last 100 messages received.
	 */
	public Message[] getMessages(int maxToGet) throws MessagingException {
		
		Map<String, Integer> indices = getStartAndEndIndices(maxToGet);
		return folder.getMessages(indices.get("startIndex"), indices.get("endIndex"));
	}

	/**
	 * Returns HTML of the email's content
	 * 
	 * @throws IOException
	 * @throws MessagingException
	 */
	public String getMessageContent(Message message) throws IOException, MessagingException {
		
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(message.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}

	/**
	 * Returns all urls from an email message with the linkText specified
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 */
	public List<String> getUrlsFromMessage(Message message, String linkText) throws IOException, MessagingException {
		
		String html = getMessageContent(message);
		List<String> allMatches = new ArrayList<String>();
		Matcher matcher = Pattern.compile("(<a [^>]+>)" + linkText + "</a>").matcher(html);
		while (matcher.find()) {
			String aTag = matcher.group(1);
			allMatches.add(aTag.substring(aTag.indexOf("http"), aTag.indexOf("\">")));
		}
		return allMatches;
	}

	private Map<String, Integer> getStartAndEndIndices(int max) throws MessagingException {
		
		int endIndex = getNumberOfMessages();
		int startIndex = endIndex - max;

		// In event that maxToGet is greater than number of messages that exist
		if (startIndex < 1) {
			startIndex = 1;
		}

		Map<String, Integer> indices = new HashMap<String, Integer>();
		indices.put("startIndex", startIndex);
		indices.put("endIndex", endIndex);

		return indices;
	}

	/**
	 * Searches an email message for a specific string
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 */
	public boolean isTextInMessage(Message message, String text) throws IOException, MessagingException {
		
		String content = getMessageContent(message);

		// Some Strings within the email have whitespace and some have break coding.
		// Need to be the same.
		content = content.replace("&nbsp;", " ");
		return content.contains(text);
	}

	public boolean isMessageInFolder(String subject, boolean unreadOnly) throws UnsupportedOperationException,
			ClassCastException, NullPointerException, IllegalArgumentException, MessagingException {
		
		int messagesFound = getMessagesBySubject(subject, unreadOnly, getNumberOfMessages()).length;
		return messagesFound > 0;
	}

	public boolean isMessageUnread(Message message) throws MessagingException {
		return !message.isSet(Flags.Flag.SEEN);
	}

}
