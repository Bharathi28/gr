package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sns.gr.testbase.DBLibrary;

public class PricingUtilities {
	
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();	

	public List<Map<String, Object>> get_pricing_locator(String realm, String brand, String campaign, String step, String offer) throws ClassNotFoundException, SQLException {
		String query = "select * from pricing_locators where ";
		String include_realm = "realm='" + realm + "'";
		String include_brand = "brand='" + brand + "'";
		String include_campaign = "campaign='" + campaign + "'";
		String include_step = "step='" + step + "'";
		String include_offer = "offer='" + offer + "'";
			
		if(realm != null) {
			query = query + include_realm;
			if((brand != null) || (campaign != null) || (step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		
		if(brand != null) {
			query = query + include_brand;
			if((campaign != null) || (step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		if(campaign != null) {
			query = query + include_campaign;
			if((step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		if(brand != null) {
			query = query + include_brand;
			if((step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		if(step != null) {
			query = query + include_step;
			if(offer != null) {
				query = query + " and ";
			}
		}
		if(offer != null) {
			query = query + include_offer;
		}					
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		return locator;		
	}
		
	public String fetch_sas_current_price (WebDriver driver, String env, String brand, String campaign, String offercode) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		
		Map<String, Object> offerdata = DBUtilities.get_offerdata(offercode, brand, campaign, "Kit");
		String kit_name = offerdata.get("DESCRIPTION").toString();
		
		List<Map<String, Object>> locator = get_pricing_locator(realm, brand, campaign, "SAS Current Price",kit_name);	
		if(!(locator.get(0).get("ELEMENTVALUE").toString().equalsIgnoreCase("n/a"))) {
			WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString());
			String current_price = elmt.getText();	
			return current_price;
		}
		else {
			return "n/a";
		}
	}

	public String fetch_sas_strike (WebDriver driver, String env, String brand, String campaign, String offercode) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String strike = "";
		if(brand.equalsIgnoreCase("R4")) {			
			List<Map<String, Object>> locator = get_pricing_locator(realm, null, null, "SAS Strike", null);
			WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString());
			strike = elmt.getText();	
		}		
		return strike;
	}
	
	public String fetch_shop_pricing(WebDriver driver, String env, String brand, String campaign, String offercode) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		Map<String, Object> offerdata = DBUtilities.get_offerdata(offercode, brand, campaign, "Product");
		String kit_name = offerdata.get("DESCRIPTION").toString();
		
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "Product", kit_name);
				
		while(comm_obj.find_mulwebelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString()).size() == 0){
			jse.executeScript("window.scrollBy(0,1000)", 0);
		}
		
//		List<Map<String, Object>> pricing_locator = get_pricing_locator(realm, brand, campaign, "Shop Price",kit_name);		
//		WebElement elmt = comm_obj.find_webelement(driver, pricing_locator.get(0).get("elementlocator").toString(), pricing_locator.get(0).get("elementvalue").toString());
//		String shop_price = elmt.getText();	
//		return shop_price;
		String shop_price = null;
		
		if(brand.equalsIgnoreCase("Mally")) {
			shop_price = driver.findElement(By.xpath(locator.get(0).get("ELEMENTVALUE").toString() + "//span[@class='current-price oneshotPrice out-btn-price']")).getText();
		}
		else if(brand.equalsIgnoreCase("WestmoreBeauty")){
			shop_price = driver.findElement(By.xpath(locator.get(0).get("ELEMENTVALUE").toString() + "/..//span")).getText();
		}		
		return shop_price;
	}
	
	public String fetch_pricing (WebDriver driver, String env, String brand, String campaign, String pricing) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);	
		List<Map<String, Object>> locator = null;
		if(pricing.contains("Checkout")){
			locator = get_pricing_locator(realm, null, null, pricing, null);		
		}
		else if((pricing.contains("Confirmation")) || (pricing.contains("Paypal"))){
			if(realm.equalsIgnoreCase("R4")) {
				locator = get_pricing_locator(realm, null, null, pricing, null);
			}
			else if(realm.equalsIgnoreCase("R2")) {
				locator = get_pricing_locator(realm, brand, null, pricing, null);
			}
		}
		String text = "";
		for(Map<String,Object> loc : locator) {
			String elementvalue = loc.get("ELEMENTVALUE").toString();
			if((pricing.contains("Checkout")) && (campaign.equalsIgnoreCase("crepeerase"))){
				elementvalue = "(" + elementvalue + ")[2]";
			}
			if((pricing.contains("Checkout")) && (brand.equalsIgnoreCase("spotfade"))){
				elementvalue = "(" + elementvalue + ")[2]";
			}
			if((pricing.contains("Checkout")) && (campaign.equalsIgnoreCase("Core")) && (brand.equalsIgnoreCase("AllKind"))){
				elementvalue = "(" + elementvalue + ")[2]";
			}
			if((pricing.contains("Checkout Shipping")) && ((campaign.equalsIgnoreCase("Core")) && (brand.equalsIgnoreCase("MeaningfulBeauty")))){
				elementvalue = "(" + elementvalue + ")[2]";
			}
			if((pricing.contains("Checkout")) && (brand.contains("BodyFirm"))){
				if(pricing.contains("Shipping")) {
					elementvalue = "(" + elementvalue + ")[3]";
				}
				else {
					elementvalue = "(" + elementvalue + ")[2]";
				}
			}					
//			System.out.println("pricing: " + elementvalue);
			if(driver.findElements(By.xpath(elementvalue)).size() != 0) {
				text = driver.findElement(By.xpath(elementvalue)).getText();
 				break;
			}
		}
		return text;
	}
	
//	// R4
//	public String fetch_sas_strike (WebDriver driver, String env, String brand, String campaign, String offercode) throws ClassNotFoundException, SQLException {
//		String strike = "";
//		if(!(brand.equalsIgnoreCase("Smileactives"))) {
//			List<Map<String, Object>> offerdata = mt_obj.get_offerdata(offercode, brand, campaign);
//			String kit_name = offerdata.get(0).get("description").toString().toLowerCase();
//			
//			List<Map<String, Object>> locator = get_pricing_locator(brand, campaign, "SAS Strike", kit_name);
//			WebElement elmt = bf_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
//			strike = elmt.getText();			
//		}
//		return strike;
//	}
}