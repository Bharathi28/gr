package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartLangUtilities {
	
	public String getKitName(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		String query1 = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "';";
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query1);
		
		System.out.println(brand);
		System.out.println(campaign);
		System.out.println(ppid);
		String kitName = offerdata.get(0).get("description").toString();
		System.out.println(kitName);
		return kitName;
	}
	
	public String checkOffers(String brand, String campaign, String kitName) throws ClassNotFoundException, SQLException {
		
		String query = "select * from campaign_offers where brand='" + brand + "' and campaign='" + campaign + "' and kit='" + kitName + "';";
		System.out.println(query);
		
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		if(locator.size() == 0) {
			return "No Offer";
		}
		else {
			return "Offer Available";
		}
	}
	
	public Map<String, Object> getOffers(String brand, String campaign, String kitName) throws ClassNotFoundException, SQLException {		
		String query = "select * from campaign_offers where brand='" + brand + "' and campaign='" + campaign + "' and kit='" + kitName + "';";
		System.out.println(query);
		
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		return locator.get(0);
	}		
	
	public String offerValidation(String brand, String campaign, String kitName) throws ClassNotFoundException, SQLException {
		String offersPresent = checkOffers(brand, campaign, kitName);
		String expectedPercentage;
		if(offersPresent.equalsIgnoreCase("Offer Available")) {
			Map<String, Object> campaignOffer = getOffers(brand, campaign, kitName);
			// Assertions
			expectedPercentage = campaignOffer.get("offer").toString();
		}
		else {
			expectedPercentage = "0";
		}
		return expectedPercentage;
	}
	
	public String validate_subtotal(String cart_lang_price, String checkout_subtotal, String expPercent) {
		
		String result = "";
		
		if(cart_lang_price.contains("$")) {
			cart_lang_price = cart_lang_price.replace("$", "");
		}
		if(checkout_subtotal.contains("$")) {
			checkout_subtotal = checkout_subtotal.replace("$", "");
		}		
		
		System.out.println(cart_lang_price);
		System.out.println(checkout_subtotal);
		System.out.println(expPercent);
		
		Double lang_price_value = null;
		Double subtotal_value = null;
		if(cart_lang_price.equalsIgnoreCase(" ")) {
			result = "PASS";
		}
		else {
			lang_price_value = Double.valueOf(cart_lang_price);
			subtotal_value = Double.valueOf(checkout_subtotal);
			
			if(subtotal_value <= lang_price_value) {
				double int_result1 = (lang_price_value - subtotal_value);
				double roundOff1 = Math.round(int_result1 * 100.0) / 100.0;
				
				double int_result2 = lang_price_value/100;
				double roundOff2 = Math.round(int_result2 * 10000.0) / 10000.0;
				
				double percent = roundOff1/roundOff2;
				int percentvalue = (int)percent;
				String actualPercent = String.valueOf(percentvalue);
				System.out.println(expPercent);
				System.out.println(actualPercent);
				System.out.println("Percent : " + percentvalue);
				if(expPercent.equalsIgnoreCase(actualPercent)) {
					if(percentvalue <= 30) {
						result = "PASS";
					}
					else {
						System.out.println("Expected percentage and Actual Percentage crossed margin value of 30%");
						result = "Expected percentage and Actual Percentage crossed margin value of 30%";
					}
				}
				else {
					result = "FAIL";
				}
			}
			else if(subtotal_value > lang_price_value) {
				result = "FAIL";
			}
		}
		System.out.println(result);
		return result;
	}
	
	public String get_cart_language(WebDriver driver) {
		String cart_lang = "";
		if(driver.findElements(By.xpath("//div[@class='continuity-summary']//p")).size() != 0) {
			cart_lang = driver.findElement(By.xpath("//div[@class='continuity-summary']//p")).getText();
		}
		else {
			cart_lang = "No Cart Language";
		}
		return cart_lang;
	}
	
	public String[] parse_cart_language(String language) {
		language = language.replaceAll("[^0-9.$]+", "");
		language = language.substring(language.indexOf("$"));
		language = language.substring(0, language.length() - 1);
		language = language.substring(0, language.length() - 1);
		
		language = language.replace("$", " ");
		String[] price_arr = language.split(" ");		
		return price_arr;
	}
	
	public String[] parse_installments_language(String language) {
		
		language = language.replace("1st Payment:", "");
		language = language.replace("2nd Payment:", "");
		language = language.replace("3rd Payment:", "");
		language = language.replaceAll("[^0-9.$]+", "");
		System.out.println(language);
		language = language.substring(language.indexOf("$"));
		
		language = language.replace("$", " ");
		language = language.substring(1);
		String[] price_arr = language.split(" ");
		return price_arr;
	}
	
	public String get_installments_text(WebDriver driver) {
		String ins_text = driver.findElement(By.xpath("//div[@class='disclouserText installment-summary small-12 float-left clearfix']")).getText();
		return ins_text;
	}
}