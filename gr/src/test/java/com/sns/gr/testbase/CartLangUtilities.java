package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartLangUtilities {
	
	public String getKitName(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";		
		
		String query1 = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "'";
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query1);
		
		System.out.println(brand);
		System.out.println(campaign);
		System.out.println(ppid);
		String kitName = offerdata.get(0).get("DESCRIPTION").toString();
		System.out.println(kitName);
		return kitName;
	}
	
	public String checkOffers(String brand, String campaign, String kitName) throws ClassNotFoundException, SQLException {
		
		String query = "select * from campaign_offers where brand='" + brand + "' and campaign='" + campaign + "' and kit='" + kitName + "'";
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
		String query = "select * from campaign_offers where brand='" + brand + "' and campaign='" + campaign + "' and kit='" + kitName + "'";
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
			expectedPercentage = campaignOffer.get("OFFER").toString();
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
				
				if(expPercent.contains("dollars")) {
					String dollar = expPercent.replace(" dollars", "");
					Double dollar_value = Double.valueOf(dollar);
					double dollar_roundOff = Math.round(dollar_value * 100.0) / 100.0;
										
					System.out.println(roundOff1);					
					System.out.println(dollar_roundOff);
					
					String actualvalue = String.valueOf(roundOff1);
					String expectedvalue = String.valueOf(dollar_roundOff);
					if(expectedvalue.equalsIgnoreCase(actualvalue)) {
						result = "PASS";
					}
					else {
						result = "FAIL";
					}
				}
				else {					
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
			}
			else if(subtotal_value > lang_price_value) {
				result = "FAIL";
			}
		}
		System.out.println(result);
		return result;
	}
	
	public String validate_r2_price(String ppid, String cart_lang_price, String cart_lang_shipping, String checkout_subtotal, String checkout_shipping) throws ClassNotFoundException, SQLException {
		String result = "";
		List<String> r2expectedPrice = DBUtilities.fetch_r2_pricing(ppid);
				
		checkout_subtotal = checkout_subtotal.replace("$", "");
		checkout_shipping = checkout_shipping.replace("$", "");
		checkout_subtotal = checkout_subtotal.replaceAll("[^0-9.$]+", "");
		checkout_shipping = checkout_shipping.replaceAll("[^0-9.$]+", "");
		
		if(checkout_shipping.contains("0.00")) {
			checkout_shipping = "0.00";
		}
		
		System.out.println(cart_lang_price);
		System.out.println(r2expectedPrice.get(0));
		System.out.println(cart_lang_shipping);
		System.out.println(r2expectedPrice.get(1));
		System.out.println(checkout_subtotal);
		System.out.println(r2expectedPrice.get(2));
		System.out.println(checkout_shipping);
		System.out.println(r2expectedPrice.get(3));
		
		if((cart_lang_price.equalsIgnoreCase(r2expectedPrice.get(0))) && (cart_lang_shipping.equalsIgnoreCase(r2expectedPrice.get(1))) && (checkout_subtotal.equalsIgnoreCase(r2expectedPrice.get(2))) && (checkout_shipping.equalsIgnoreCase(r2expectedPrice.get(3)))){
			result = "PASS";
		}
		else {
			result = "FAIL";
		}
		return result;
	}
	
	public String get_cart_language(WebDriver driver, String brand) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String cart_lang = "";		
		if(realm.equalsIgnoreCase("R4")) {
			if(driver.findElements(By.xpath("//div[contains(@class,'continuity-summary')]")).size() != 0) {
				cart_lang = driver.findElement(By.xpath("//div[contains(@class,'continuity-summary')]")).getText();
			}
			else {
				cart_lang = "No Cart Language";
			}

//			if(driver.findElements(By.xpath("//div[@class='continuity-summary']//p")).size() != 0) {
//				cart_lang = driver.findElement(By.xpath("//div[@class='continuity-summary']//p")).getText();
//			}
//			int size = driver.findElements(By.xpath("//div[@class='continuity-summary']//p//strong")).size();
//			if(size == 1) {
//				cart_lang = driver.findElement(By.xpath("(//div[@class='continuity-summary']//p//strong)[1]")).getText();
//			}
//			else if(size == 2) {
//				cart_lang = driver.findElement(By.xpath("(//div[@class='continuity-summary']//p//strong)[1]")).getText() + driver.findElement(By.xpath("(//div[@class='continuity-summary']//p//strong)[2]")).getText();
//			}
//			else {
//				if(driver.findElements(By.xpath("//div[@class='continuity-summary']//p")).size() != 0) {
//					if(driver.findElement(By.xpath("//div[@class='continuity-summary']//p")).getText().contains("$")) {
//						cart_lang = driver.findElement(By.xpath("//div[@class='continuity-summary']//p")).getText();
//					}
//				}
//				if(driver.findElements(By.xpath("//div[@class='continuity-summary']//p//strong")).size() != 0) {
//					if(driver.findElement(By.xpath("//div[@class='continuity-summary']//p//strong")).getText().contains("$")) {
//						cart_lang = driver.findElement(By.xpath("//div[@class='continuity-summary']//p//strong")).getText();
//					}
//				}
//				if(driver.findElements(By.xpath("//div[@class='continuity-summary']//p//strong[2]")).size() != 0) {
//					if(driver.findElement(By.xpath("//div[@class='continuity-summary']//p//strong[2]")).getText().contains("$")) {
//						cart_lang = driver.findElement(By.xpath("//div[@class='continuity-summary']//p//strong[2]")).getText();
//					}
//				}
//				else {
//					cart_lang = "No Cart Language";
//				}
//			}
		}
		else {
//			cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']")).getText();
			
			if(brand.equalsIgnoreCase("PrincipalSecret")) {
				if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[8]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[8]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[2]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[2]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[5]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[5]//strong")).getText();
				}
				else {
					cart_lang = "No Cart Language";
				}
			}
			else if(brand.equalsIgnoreCase("ReclaimBotanical")) {
				if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[5]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[5]//strong")).getText();
				}
				else {
					cart_lang = "No Cart Language";
				}
			}
			else if(brand.equalsIgnoreCase("SpecificBeauty")){
				if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[2]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[2]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[3]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[3]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[5]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[5]//strong")).getText();
				}
				else {
					cart_lang = "No Cart Language";
				}
			}
			else if(brand.equalsIgnoreCase("Sub-D")){
				if(driver.findElements(By.xpath("//div[@class='shortDescription']//p//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[2]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[2]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[3]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[3]//strong")).getText();
				}
				else if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[5]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[5]//strong")).getText();
				}
				else {
					cart_lang = "No Cart Language";
				}
			}
			else {
				if(driver.findElements(By.xpath("//div[@class='shortDescription']//p[2]//strong")).size() != 0) {
					cart_lang = driver.findElement(By.xpath("//div[@class='shortDescription']//p[2]//strong")).getText();
				}
				else {
					cart_lang = "No Cart Language";
				}
			}			
		}
		
		return cart_lang;
	}
	
	public String[] parse_cart_language(String language) {
		System.out.println("1 " + language);
		language = language.replaceAll("[^0-9.$]+", "");
		System.out.println("2 " + language);
		language = language.substring(language.indexOf("$"));
		System.out.println("3 " + language);
		while(language.endsWith(".")) {
			language = language.substring(0, language.length() - 1);
		}		
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
	
	public String get_ppid(WebDriver driver, String brand, String campaign, Map<String, Object> offer) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String ppid = null;
		if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))){
			ppid = offer.get("ppid").toString();
		}
		else if(realm.equalsIgnoreCase("R4")){
			ppid = driver.findElement(By.xpath("(//span[@class='PPID disclaimer-ppid'])[1]")).getText();	
		}	
		else {
			ppid = driver.findElement(By.xpath("//div[@class='offerCodeID']")).getText();
		}
		return ppid;
	}
}