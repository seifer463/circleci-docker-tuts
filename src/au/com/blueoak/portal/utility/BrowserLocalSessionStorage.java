package au.com.blueoak.portal.utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class BrowserLocalSessionStorage {

	private JavascriptExecutor jse;

	// constructor
	public BrowserLocalSessionStorage(WebDriver driver) {

		this.jse = (JavascriptExecutor) driver;
	}

	/**
	 * README
	 * This is for Browser Local Storage
	 */
	
	/** 
	 * Remove an item by passing the Key
	 * */
	public void removeItemFromLocalStorage(String key) {
		
		jse.executeScript(String.format("window.localStorage.removeItem('%s');", key));
	}

	/** 
	 * Check if a certain item is present by passing the key
	 * */
	public boolean isItemPresentInLocalStorage(String key) {
		
		if (jse.executeScript(String.format("return window.localStorage.getItem('%s');", key)) == null)
			return false;
		else
			return true;
	}

	/** 
	 * Get the item value by passing the corresponding key
	 * */
	public String getItemFromLocalStorage(String key) {
		
		return (String) jse.executeScript(String.format("return window.localStorage.getItem('%s');", key));
	}

	/** 
	 * Get the key name by passing the index number.
	 * 
	 * @param keyIndex starts at 0 being the first number
	 * */
	public String getKeyFromLocalStorage(int keyIndex) {
		
		return (String) jse.executeScript(String.format("return window.localStorage.key('%s');", keyIndex));
	}

	/** 
	 * Get the number of items stored
	 * */
	public Long getLocalStorageLength() {
		
		return (Long) jse.executeScript("return window.localStorage.length;");
	}

	/** 
	 * Use this to set the Key,Value in the storage
	 * */
	public void setItemInLocalStorage(String key, String value) {
		
		jse.executeScript(String.format("window.localStorage.setItem('%s','%s');", key, value));
	}

	/** 
	 * Use this to clear all the items
	 * */
	public void clearLocalStorage() {
		
		jse.executeScript(String.format("window.localStorage.clear();"));
	}
	
	/** 
	 * Use this to get all the keys in the storage
	 * 
	 * */
	public List<String> getAllKeysFromLocalStorage() {
		
		List<String> keys = new ArrayList<>();
		
		for (int i = 0; i < getLocalStorageLength(); i++) {
			String keyRaw = getKeyFromLocalStorage(i);
			String key = StringUtils.normalizeSpace(keyRaw);
			keys.add(key);
		}
		
		return keys;
	}

	/**
	 * README
	 * This is for Browser Session Storage
	 */
	
	/** 
	 * Remove an item by passing the Key
	 * */
	public void removeItemFromSessionStorage(String key) {

		jse.executeScript(String.format("window.sessionStorage.removeItem('%s');", key));
	}

	/**
	 * Check if a certain item is present by passing the key
	 */
	public boolean isItemPresentInSessionStorage(String key) {

		if (jse.executeScript(String.format("return window.sessionStorage.getItem('%s');", key)) == null)
			return false;
		else
			return true;
	}

	/**
	 * Get the item value by passing the corresponding key
	 */
	public String getItemFromSessionStorage(String key) {

		return (String) jse.executeScript(String.format("return window.sessionStorage.getItem('%s');", key));
	}
	
	/**
	 * Get the key name by passing the index number.
	 * 
	 * @param keyIndex starts at 0 being the first number
	 */
	public String getKeyFromSessionStorage(int keyIndex) {

		return (String) jse.executeScript(String.format("return window.sessionStorage.key('%s');", keyIndex));
	}

	/**
	 * Get the number of items stored
	 */
	public Long getSessionStorageLength() {

		return (Long) jse.executeScript("return window.sessionStorage.length;");
	}

	/**
	 * Use this to set the Key,Value in the storage
	 */
	public void setItemInSessionStorage(String key, String value) {

		jse.executeScript(String.format("window.sessionStorage.setItem('%s','%s');", key, value));
	}

	/**
	 * Use this to clear all the items
	 */
	public void clearSessionStorage() {

		jse.executeScript(String.format("window.sessionStorage.clear();"));
	}
	
	/** 
	 * Use this to get all the keys in the storage
	 * 
	 * */
	public List<String> getAllKeysFromSessionStorage() {
		
		List<String> keys = new ArrayList<>();
		
		for (int i = 0; i < getSessionStorageLength(); i++) {
			String keyRaw = getKeyFromSessionStorage(i);
			String key = StringUtils.normalizeSpace(keyRaw);
			keys.add(key);
		}
		
		return keys;
	}

}
