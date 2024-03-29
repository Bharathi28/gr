package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sns.gr.testbase.DBLibrary;
import com.sns.gr.testbase.DBUtilities;

public class BuyflowUtilities {
	
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	
	public void click_cta(WebDriver driver, String env, String brand, String campaign, String step) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
				
		String query = "select * from cta_locators where brand='" + brand + "' and campaign='" + campaign + "' and step='" + step + "'";
//		System.out.println(query);
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch",query);
		String elementlocator = "";
		String elementvalue = "";
		if(env.toLowerCase().contains("dev")){
			elementlocator = locator.get(0).get("STGLOCATOR").toString();
			elementvalue = locator.get(0).get("STGVALUE").toString();	
		}
		else {
			elementlocator = locator.get(0).get(env.toUpperCase() + "LOCATOR").toString();
			elementvalue = locator.get(0).get(env.toUpperCase() + "VALUE").toString();
		}
			
		if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("specialCore"))) {
			if(driver.findElement(By.xpath("//div[@class='header-promo-slider']")).getAttribute("style").toString().equalsIgnoreCase("display: none;")) {
				driver.findElement(By.xpath("//div[@class='header-promo']")).click();
			}
		}			
		
		if((brand.equalsIgnoreCase("BodyFirm-CrepeErase")) || (brand.equalsIgnoreCase("BodyFirm-SpotFade"))) {
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
		}
		
		if(!(elementvalue.equalsIgnoreCase("n/a"))) {
			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);	
			Thread.sleep(2000);
			//element.click();
			jse.executeScript("arguments[0].click();", element); 
		}	
	}
	
	public void click_logo(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException {
		List<Map<String, Object>> logo_locators = comm_obj.get_element_locator(brand, campaign, "Logo", null);
		for(Map<String,Object> logo : logo_locators) {
			String elementvalue = logo.get("ELEMENTVALUE").toString();
			if(driver.findElements(By.xpath(elementvalue)).size() != 0) {
				driver.findElement(By.xpath(elementvalue)).click();
				break;
			}
		}
	}		
	
	public String campaign_repeat_validation(String categoryy, String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
				
		String tempCategory = "";
		int subscribe = 0;
		if(categoryy.equalsIgnoreCase("SubscribeandSave")) {
			tempCategory = "Product";
			subscribe = 1;
			if(!(campaign.equals("Core"))) {
				campaign="Core";
			}
		}
		else if(categoryy.equalsIgnoreCase("Product")) {
			tempCategory = categoryy;
			if(!(campaign.equals("Core"))) {
				campaign="Core";
			}
		}
		else if((categoryy.equalsIgnoreCase("ShopKit")) || (categoryy.equalsIgnoreCase("Kit"))){
			String isproduct = db_obj.isProduct(brand, ppid);
			if(isproduct.equalsIgnoreCase("Yes")) {
				tempCategory = "Product";
				if(!(campaign.equals("Core"))) {
					campaign="Core";
				}
			}
			else {
				tempCategory = categoryy;
			}
		}
		return tempCategory + "/" + campaign + "/" + subscribe;
	}
	
	public List<String> check_ppid_in_combo(String brand, String campaign, String ppid, String category) throws ClassNotFoundException, SQLException {
				
		String[] brandArr = db_obj.get_combo(brand, campaign);
		List<String> brand_campaign_list = new ArrayList<String>();
				
		for(String arrelmt : brandArr) {
			String[] brand_campaign = arrelmt.split("-");
			String temp_brand = brand_campaign[0];
			String temp_campaign = brand_campaign[1];
			
			String ppidPresent = check_ppid_in_brand(temp_brand, temp_campaign, ppid);
			
			if(ppidPresent == "Yes") {
				brand_campaign_list.add(temp_brand);
				brand_campaign_list.add(temp_campaign);
				break;
			}
		}			
		return brand_campaign_list;		
	}
	
	public String check_ppid_in_brand(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		String realm = db_obj.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
					
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "' and status ='Active'";
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);	
		int size = offerdata.size();
		String ppidPresent = "No";
		if(size > 0) {
			ppidPresent = "Yes";
		}
		return ppidPresent;
	}
	
	public void combo_navigation_to_sas(WebDriver driver, String env, String combobrand, String combocampaign, String brand, String campaign, String nav, String category) throws ClassNotFoundException, SQLException, InterruptedException {
		Thread.sleep(4000);
		List<Map<String, Object>> brand_logo = comm_obj.get_element_locator(combobrand, combocampaign, "BrandLogo", brand);
		driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
		Thread.sleep(2000);
		if(driver.findElements(By.xpath("//li[@class='nav-brand-spotfade nav-mainmenu']//a")).size() != 0) {
			
			driver.findElement(By.xpath("//li[@class='nav-brand-spotfade nav-mainmenu']//a")).click();
			Thread.sleep(3000);
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
		}
		Thread.sleep(1000);
		
		WebElement elmt = comm_obj.find_webelement(driver, brand_logo.get(0).get("ELEMENTLOCATOR").toString(), brand_logo.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		elmt.click();
		
		if((category.equalsIgnoreCase("Product")) || (category.equalsIgnoreCase("ShopKit"))) {
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
		}
		click_cta(driver,env,brand,campaign,category);
	}
	
	public void move_to_sas(WebDriver driver, String env, String brand, String campaign, String offercode, String category) throws ClassNotFoundException, SQLException, InterruptedException {
	
		if(brand.equalsIgnoreCase("BodyFirm-CrepeErase")) {
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
			Thread.sleep(1000);
			driver.findElement(By.xpath("//img[@alt='Crepe Erase']")).click();
		}
		else if(brand.equalsIgnoreCase("BodyFirm-SpotFade")) {
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
			Thread.sleep(1000);
			driver.findElement(By.xpath("//img[@alt='Spot Fade']")).click();
		}
		String step = "";
		if(brand.equalsIgnoreCase("BodyFirm")) {
			step = "Shop";
		}
		else {
			if((category.equalsIgnoreCase("Product")) || (category.equalsIgnoreCase("SubscribeandSave")) || (category.equalsIgnoreCase("ShopKit"))){
				step = "Shop";
			}
			else if (category.equalsIgnoreCase("Kit")) {
				step = "Ordernow";
			}
		}		
		
//		if(offercode.contains("single")){
//			category ="Product";
//		}
//		if(nav.equalsIgnoreCase("brands-nav")) {
//			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
//			Thread.sleep(1000);				
//		}
		
		click_cta(driver,env,brand,campaign,step);
		Thread.sleep(2000);
	}
	
	public void move_to_checkout(WebDriver driver, String brand, String campaign, String category) throws InterruptedException, ClassNotFoundException, SQLException {
		System.out.println("Moving to Checkout Page...");	

		JavascriptExecutor jse = (JavascriptExecutor) driver;
				
		Thread.sleep(2000);
		// Check if the page is already in checkout
		if(driver.findElements(By.id("dwfrm_personinf_contact_email")).size() != 0) {
			
		}
		else if(driver.findElements(By.id("dwfrm_cart_billing_billingAddress_email_emailAddress")).size() != 0) {
			
		}
		else {						
			if((category.equalsIgnoreCase("SubscribeandSave")) || (category.equalsIgnoreCase("ShopKit"))) {
				category = "Product";
			}
			
			List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "MoveToCheckout", category);
			
			String elementlocator = locator.get(0).get("ELEMENTLOCATOR").toString();
			String elementvalue = locator.get(0).get("ELEMENTVALUE").toString();			

			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);	
			Thread.sleep(3000);
			element.click();
			Thread.sleep(2000);			
		}	
	}
	
	public String check_upsell_select(String brand, String campaign, String ppid, String category) throws ClassNotFoundException, SQLException {
		String realm = db_obj.get_realm(brand);
		String upsell = "";
		String upsell_brand = "";
		String upsell_campaign = "";
		String kit_ppid = ppid;
		String[] offer_array = ppid.split(",");
		String[] category_array = category.split(",");
		
		String ppuPresent = db_obj.checkPPUPresent(brand, campaign, category_array[0]);
		
		if(ppuPresent.equalsIgnoreCase("Yes")) {
			if(ppid.contains(",")) {		
				if(realm.equalsIgnoreCase("R4")) {
//					List<String> offer_list = Arrays.asList(offer_array);
//					int list_size = offer_list.size();
//					
//					for(int i = list_size-1; i>0; i--) {
//						if(brand.equalsIgnoreCase("BodyFirm")) {
//							List<String> combo_brand_campaign = check_ppid_in_combo(brand, campaign, offer_list.get(i), category);	
//							upsell_brand = combo_brand_campaign.get(0);
//							upsell_campaign = combo_brand_campaign.get(1);
//						}
//						else {
//							upsell_brand = brand;
//							upsell_campaign = campaign;
//						}	
//						String isproduct = db_obj.isProduct(upsell_brand, offer_list.get(i));
//						if(isproduct.equalsIgnoreCase("Yes")) {
//							kit_ppid = kit_ppid.replace("," + offer_list.get(i), "");
//						}
//						else {
//							break;
//						}
//					}
//					
//					String upsell_offer = kit_ppid;
//					if(kit_ppid.contains(",")) {			
//						if(brand.equalsIgnoreCase("BodyFirm")) {
//							upsell_offer = offer_array[1];
//						}
//						else {
//							upsell_offer = check_deluxe_kit(upsell_brand, upsell_campaign, kit_ppid, category);
//						}
//					}				
					if((category_array[0].contains("Kit")) && (category_array[1].contains("Kit"))) {
//						if(brand.equalsIgnoreCase("BodyFirm")) {
							Map<String, Object> offerdata = DBUtilities.get_offerdata(offer_array[1], upsell_brand, upsell_campaign, category_array[1]);
							upsell = offerdata.get("UPGRADE").toString();
//						}
//						else {
//							String upsell_offer = check_deluxe_kit(brand, campaign, ppid, category);
//							Map<String, Object> offerdata = DBUtilities.get_offerdata(upsell_offer, brand, campaign, category_array[0]);
//							upsell = offerdata.get("UPGRADE").toString();
//						}
					}
					else {
						Map<String, Object> offerdata = DBUtilities.get_offerdata(offer_array[0], brand, campaign, category_array[0]);
						upsell = offerdata.get("UPGRADE").toString();
					}					
				}
				else {
					Map<String, Object> offerdata = DBUtilities.get_offerdata(offer_array[0], brand, campaign, category_array[0]);
					upsell = offerdata.get("UPGRADE").toString();
				}
			}
			else {
				Map<String, Object> offerdata = DBUtilities.get_offerdata(ppid, brand, campaign, category);
				upsell = offerdata.get("UPGRADE").toString();
			}
		}
		return upsell;
	}
	
	// Assuming that there is only two offercodes seperated by comma
	public String check_deluxe_kit(String brand, String campaign, String ppid, String category) throws ClassNotFoundException, SQLException {
		String upsell_offer = "";
		List<String> kit_type = new ArrayList<String>();
		String[] offer_arr = ppid.split(",");
		
		for(String offer : offer_arr) {			
			Map<String, Object> offerdata = DBUtilities.get_offerdata(offer, brand, campaign, category);
			String type = offerdata.get("KIT_TYPE").toString();
			kit_type.add(type);
		}	
		
		if(kit_type.get(0).equalsIgnoreCase(kit_type.get(1))){
			upsell_offer = offer_arr[1];
		}
		else if(kit_type.get(0).equalsIgnoreCase("Deluxe")){
				upsell_offer = offer_arr[0];
		}
		else if(kit_type.get(1).equalsIgnoreCase("Deluxe")){
			upsell_offer = offer_arr[1];
		}
		return upsell_offer;
	}
	
	public void upsell_confirmation(WebDriver driver, String brand, String campaign, String upsell) throws InterruptedException, ClassNotFoundException, SQLException {
		
		Thread.sleep(4000);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
				
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "Upsell", upsell);
			
		String elementlocator = locator.get(0).get("ELEMENTLOCATOR").toString();
		String elementvalue = locator.get(0).get("ELEMENTVALUE").toString();
		jse.executeScript("window.scrollBy(0,350)", 0);
		Thread.sleep(4000);
		
		comm_obj.find_webelement(driver, elementlocator, elementvalue).click();
	}
	
	public String fetch_offercode(WebDriver driver, String brand) throws ClassNotFoundException, SQLException {
		String offercode = "";
		String realm = DBUtilities.get_realm(brand);
		
		List<WebElement> offercode_elmt;
		if(realm.equalsIgnoreCase("R2")) {
			offercode_elmt = driver.findElements(By.className("offerCodeID"));
		}
		else {
			offercode_elmt = driver.findElements(By.xpath("//span[@class='hide PPID']"));
		}
		
		int count = offercode_elmt.size();
		for(WebElement elmt : offercode_elmt) {
			offercode = offercode + elmt.getText();
			if(count > 1) {
				offercode = offercode + ",";
			}
		}
		return offercode;
	}
	
	public String fetch_confoffercode(WebDriver driver, String brand) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String realm = DBUtilities.get_realm(brand);
		String offercode = "";
		Thread.sleep(4000);
		if(realm.equalsIgnoreCase("R2")) {
			// Also supplies to SB
			if(brand.equalsIgnoreCase("Sub-D")){
				offercode = (String) jse.executeScript("return app.omniMap.MainOfferCode");
			}
			else {
				Thread.sleep(2000);
				List<WebElement> offercode_elmt = null;
				if(driver.findElements(By.className("offerCode")).size() != 0) {
					offercode_elmt = driver.findElements(By.className("offerCode"));
				}
				else if(driver.findElements(By.xpath("//div[@class='ordersummary show-desktop']//div[contains(@id, 'orderSummary')]")).size() != 0){
					offercode_elmt = driver.findElements(By.xpath("//div[@class='ordersummary show-desktop']//div[contains(@id, 'orderSummary')]"));
				}
				int count = offercode_elmt.size();
				for(WebElement elmt : offercode_elmt) {
					if(elmt.getText().contains("-")) {
						String[] arr = elmt.getText().split("-");
						arr[0] = arr[0].replace(" ", "");
						offercode = offercode + arr[0];
					}
					else {
						offercode = offercode + elmt.getText();
					}
					if(count > 1) {
						offercode = offercode + ",";
					}
					count--;
				}
			}
		}
		else {
			
			String products = (String) jse.executeScript("return app.variableMap.products");
			String[] arr = products.split(";");
			int arrSize = arr.length;
			for(int i=1; i<arrSize; i=i+3) {
				offercode = offercode + arr[i] + ",";
			}
			offercode = offercode.substring(0, offercode.length() - 1);
		}		
		return offercode;
	}
	
	public String fetch_conf_num(WebDriver driver, String brand) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		WebElement conf_num_elmt;
		if(realm.equalsIgnoreCase("R2")) {
			conf_num_elmt = driver.findElement(By.id("orderConfirmNum"));
		}
		else {
			conf_num_elmt = driver.findElement(By.xpath("//div[@class='orderReceived']//span"));
		}
		String conf_num = conf_num_elmt.getText();
		return conf_num;
	}
	
	public void complete_order(WebDriver driver, String brand, String cc) throws ClassNotFoundException, SQLException, InterruptedException {
//		System.out.println("Completing Order");
		WebDriverWait wait = new WebDriverWait(driver,50);
		String realm = DBUtilities.get_realm(brand);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		WebElement comp_order_element = null;
		if(realm.equalsIgnoreCase("R2")) {
			if(cc.toLowerCase().contains("paypal")) {
//			if(driver.findElements(By.xpath("//button[@class='cta-submit btn-primary']")).size() != 0) {
				comp_order_element = driver.findElement(By.xpath("//button[@class='cta-submit btn-primary']"));
			}
			else {
//				if(driver.findElements(By.id("contYourOrder")).size() != 0) {
					comp_order_element = driver.findElement(By.id("contYourOrder"));
//				}
				
			}
		}
		else {
			if(cc.toLowerCase().contains("paypal")) {
//			if(driver.findElements(By.id("submitButton")).size() != 0) {	
				comp_order_element = driver.findElement(By.id("submitButton"));
			}
			else {
				if(brand.equalsIgnoreCase("FixMDSkin")) {
					jse.executeScript("window.scrollBy(0,250)", 0);
					Thread.sleep(2000);
				}
				comp_order_element = driver.findElement(By.id("trigerPlaceOrder"));
			}			
		}
		wait.until(ExpectedConditions.visibilityOf(comp_order_element));
		wait.until(ExpectedConditions.elementToBeClickable(comp_order_element));
		Thread.sleep(5000);
//		comp_order_element.click();
		jse.executeScript("arguments[0].click();", comp_order_element);	
	}
	
	public void clear_form_field(WebDriver driver, String realm, String field) throws ClassNotFoundException, SQLException {
		String query = "select * from form_fields_locators where realm='" + realm + "' and form='Checkout' and field='" + field + "'";
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		
		String elementlocator = locator.get(0).get("ELEMENTLOCATOR").toString();
		String elementvalue = locator.get(0).get("ELEMENTVALUE").toString();
		
		WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
		element.clear();
	}
	
	public void fill_form_field(WebDriver driver, String realm, String field, String value) throws ClassNotFoundException, SQLException {
		
		String query = "select * from form_fields_locators where realm='" + realm + "' and form='Checkout' and field='" + field + "'";
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		
		String elementlocator = locator.get(0).get("ELEMENTLOCATOR").toString();
		String elementvalue = locator.get(0).get("ELEMENTVALUE").toString();
		
		if((field.equalsIgnoreCase("State")) || (field.equalsIgnoreCase("Month")) || (field.equalsIgnoreCase("Year"))){
			Select sel_element = new Select(comm_obj.find_webelement(driver, elementlocator, elementvalue));
			sel_element.selectByValue(value);
		}
		if((field.equalsIgnoreCase("Agree")) || (field.equalsIgnoreCase("UseAsBilling")) || (field.equalsIgnoreCase("UseAsShipping"))) {
			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
			element.click();
		}
		else {
			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
//			Thread.sleep(2000);
//			element.click();
			element.sendKeys(value);
		}
	}
	
	public String fill_out_form(WebDriver driver, String brand, String campaign, String cc, String shipbill, String supply) throws InterruptedException, ClassNotFoundException, SQLException {
		WebDriverWait wait = new WebDriverWait(driver,50);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String realm = DBUtilities.get_realm(brand);
		String email = "";
		Thread.sleep(2000);
		if(cc.equalsIgnoreCase("paypal")) {
			if(realm.equalsIgnoreCase("R4")) {
				driver.findElement(By.xpath("//div[@id='paypalSection']//div//div")).click();
			}
			else {
				driver.findElement(By.xpath("//button[@class='PayPalExpressButton']")).click();
			}
			
			String winHandleBefore = driver.getWindowHandle();
			for(String winHandle : driver.getWindowHandles()){
			   driver.switchTo().window(winHandle);
			   driver.manage().window().maximize();
			   Thread.sleep(2000);
			}				
			
			if(driver.findElements(By.xpath("//div[@id='loginSection']//div//div[2]//a")).size() != 0) {
				Thread.sleep(4000);
				driver.findElement(By.xpath("//div[@id='loginSection']//div//div[2]//a")).click();
				Thread.sleep(2000);
				email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
			}
			else if(driver.findElements(By.xpath("//a[text()='Log In']")).size() != 0) {
				Thread.sleep(4000);
				driver.findElement(By.xpath("//a[text()='Log In']")).click();
				Thread.sleep(2000);
				email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
			}

//			Thread.sleep(10000);
//			driver.switchTo().frame(0);
	
			if(driver.findElements(By.xpath("//a[text()='Log In']")).size() != 0) {
				Thread.sleep(2000);
				driver.findElement(By.xpath("//a[text()='Log In']")).click();
				Thread.sleep(2000);
				email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
			}
			else {
//				while(driver.findElements(By.xpath("//a[text()='Log In']")).size() == 0) {
				while(driver.findElements(By.xpath("//div[@id='login_emaildiv']//div//input")).size() == 0) {	
					if(driver.findElements(By.xpath("//section[@id='genericError']//div//div[2]")).size() != 0) {
						driver.close();
						driver.switchTo().window(winHandleBefore);
						Thread.sleep(2000);
						email = ccPayment(driver, jse, realm, brand, campaign, "Visa", shipbill, supply);
					}
					else if(driver.findElements(By.xpath("//div[contains(text(),\"Things don't appear to be working at the moment\")]")).size() != 0) {
						driver.close();
						driver.switchTo().window(winHandleBefore);
						Thread.sleep(2000);
						email = ccPayment(driver, jse, realm, brand, campaign, "Visa", shipbill, supply);
					}
					else if(driver.findElements(By.xpath("//div[@id='loginSection']//div//div[2]//a")).size() != 0) {
						driver.findElement(By.xpath("//div[@id='loginSection']//div//div[2]//a")).click();
						Thread.sleep(2000);
						email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
					}
					else if(driver.findElements(By.xpath("//button[text()='Log In']")).size() != 0) {
						Thread.sleep(2000);
						driver.findElement(By.xpath("//button[text()='Log In']")).click();
						Thread.sleep(2000);
						email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
					}
					else if(driver.findElements(By.xpath("//div[@id='login_emaildiv']//div//input")).size() != 0) {
						Thread.sleep(2000);
						email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
					}
					if(!(email.equalsIgnoreCase(""))) {
						break;
					}
				}
				if(driver.findElements(By.xpath("//div[@id='login_emaildiv']//div//input")).size() != 0) {
					Thread.sleep(2000);
					email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
				}
				if(driver.findElements(By.xpath("//div[@id='loginSection']//div//div[2]//a")).size() != 0) {
					Thread.sleep(2000);
					driver.findElement(By.xpath("//div[@id='loginSection']//div//div[2]//a")).click();
					Thread.sleep(2000);
					email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
				}
				else if(driver.findElements(By.xpath("//button[text()='Log In']")).size() != 0) {
					Thread.sleep(2000);
					driver.findElement(By.xpath("//button[text()='Log In']")).click();
					Thread.sleep(2000);
					email = paypalPayment(driver, wait, jse, winHandleBefore, realm);
				}
			}							
		}
		else {
			email = ccPayment(driver, jse, realm, brand, campaign, cc, shipbill, supply);
		}
		return email;
	}	
	
	public String ccPayment(WebDriver driver, JavascriptExecutor jse, String realm, String brand, String campaign, String cc, String shipbill, String supply) throws ClassNotFoundException, SQLException, InterruptedException {
		String alpha = RandomStringUtils.randomAlphabetic(9);
		String num = RandomStringUtils.randomNumeric(4);
		String email = alpha + "-" + num + "@yopmail.com";
		
		Thread.sleep(3000);
		fill_form_field(driver, realm, "Email", email.toLowerCase());
		if((brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("JLoBeauty")) || (brand.equalsIgnoreCase("AllKind"))){
			driver.findElement(By.xpath("(//input[contains(@class,'input-text password')])[1]")).sendKeys("Grcweb123!");
		}
		fill_form_field(driver, realm, "PhoneNumber", "8887878787");					
		fill_form_field(driver, realm, "FirstName", firstName());
		fill_form_field(driver, realm, "LastName", lastName());
//		fill_form_field(driver, realm, "AddressLine1", "15149 Georgia Ave");
		fill_form_field(driver, realm, "AddressLine1", "123 QATest st");

		if(campaign.equalsIgnoreCase("ca")) {
			fill_form_field(driver, realm, "City", "Anywhere");
			fill_form_field(driver, realm, "State", "NB");		
			fill_form_field(driver, realm, "Zip", "E3B7K6");
		}
		else {
//			fill_form_field(driver, realm, "City", "Paramount");
			fill_form_field(driver, realm, "City", "El Segundo");
			fill_form_field(driver, realm, "State", "CA");					
			
			if(supply.equalsIgnoreCase("30")) {		
//				fill_form_field(driver, realm, "Zip", "90723");	
				fill_form_field(driver, realm, "Zip", "90245");	
			}
			else if(supply.equalsIgnoreCase("90")) {
				fill_form_field(driver, realm, "Zip", "81002");
			}
		}			
						
		Thread.sleep(2000);
		WebElement shipbill_elmt = null;
		if(driver.findElements(By.xpath("//input[@id='dwfrm_personinf_useAsBillingAddress']")).size() != 0) {
			shipbill_elmt = driver.findElement(By.xpath("//input[@id='dwfrm_personinf_useAsBillingAddress']"));
		}
		else if(driver.findElements(By.xpath("//input[@id='dwfrm_cart_billing_billingAddress_useAsShippingAddress']")).size() != 0) {
			shipbill_elmt = driver.findElement(By.xpath("//input[@id='dwfrm_cart_billing_billingAddress_useAsShippingAddress']"));
		}
		jse.executeScript("window.scrollBy(0,200)", 0);
		if(shipbill.equalsIgnoreCase("same")) {
			if(!(shipbill_elmt.isSelected())) {
				shipbill_elmt.click();
			}
		}
		else {
			if(shipbill_elmt.isSelected()) {
				shipbill_elmt.click();
			}
			
			fill_form_field(driver, realm, "ShippingFirstName", firstName());
			fill_form_field(driver, realm, "ShippingLastName", lastName());
			fill_form_field(driver, realm, "ShippingAddressLine1", "123 Anywhere st");
			fill_form_field(driver, realm, "ShippingCity", "Huntsville");
			fill_form_field(driver, realm, "ShippingState", "AL");
			fill_form_field(driver, realm, "ShippingZip", "35801");
		}		
				
		if((supply.equalsIgnoreCase("90")) && (brand.equalsIgnoreCase("Volaire"))){	
			fill_form_field(driver, realm, "CardNumber", "4111111111111111");
		}
		else {
			fill_form_field(driver, realm, "CardNumber", getCCNumber(cc));
		}		
		fill_form_field(driver, realm, "Month", "12");
		fill_form_field(driver, realm, "Year", "2024");	
		
		if(realm.equalsIgnoreCase("R4")) {
			fill_form_field(driver, realm, "CVV", "349");			
		}
		jse.executeScript("window.scrollBy(0,200)", 0);
		Thread.sleep(2000);
		fill_form_field(driver, realm, "Agree", "");
		Thread.sleep(2000);
		return (email.toLowerCase());
	}
	
	public String paypalPayment(WebDriver driver, WebDriverWait wait, JavascriptExecutor jse, String winHandleBefore, String realm) throws ClassNotFoundException, SQLException, InterruptedException {

		// Email Field
		Thread.sleep(5000);
		driver.findElement(By.xpath("//div[@id='login_emaildiv']//div//input")).sendKeys("testbuyer1@guthy-renker.com");
		
		if(driver.findElements(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).size() != 0) {
			driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).click();
		}
			
		// Password Field
		Thread.sleep(4000);
		driver.findElement(By.xpath("//div[@id='login_passworddiv']//div//input")).sendKeys("123456789");
				
		// Login button
		Thread.sleep(2000);
		if(driver.findElements(By.xpath("//*[@id='acceptAllButton']")).size()!=0) {
			driver.findElement(By.xpath("//*[@id='acceptAllButton']")).click();
		}

		driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-submit']")).click();							
		Thread.sleep(5000);
		
		// Agree and Continue
		jse.executeScript("window.scrollBy(0,800)", 0);
		Thread.sleep(3000);
//		driver.findElement(By.xpath("//button[@class='btn full confirmButton continueButton']")).click();
//		Thread.sleep(3000);
//		driver.findElement(By.xpath("//input[@id='confirmButtonTop']")).click();
		driver.findElement(By.xpath("//button[@id='payment-submit-btn']")).click();
						
		wait.until(ExpectedConditions.numberOfWindowsToBe(1));
		driver.switchTo().window(winHandleBefore);
		Thread.sleep(2000);
		fill_form_field(driver, realm, "Agree", "");
		return "testbuyer1@guthy-renker.com";
	}
	
	public String getCCNumber(String cc) {
		String ccnumber = "";
		if(cc.equalsIgnoreCase("visa")) {
			ccnumber="4111111111111111";
		}
		else if(cc.equalsIgnoreCase("amex")) {
			ccnumber="378282246310005";
		}
		else if(cc.equalsIgnoreCase("mastercard")) {
			ccnumber="5555555555554444";
		}
		else if(cc.equalsIgnoreCase("discover")) {
			ccnumber="6011111111111117";
		}
		return ccnumber;
	}
	
	public String firstName() {
		String[] arr= {"Dina","Jessy","Kevin","Adam","Yuthan","Yuvan","Henry","Heera","Ishaan","Rachel","Phoebe","Joey"};
		int rnd = new Random().nextInt(arr.length);
	    return arr[rnd];
	}
	
	public String lastName() {
		String[] arr= {"William","Wilson","Abraham","Bush","Jones","Darlow","Shapiro","Weaver","Geller"};
		int rnd = new Random().nextInt(arr.length);
	    return arr[rnd];
	}
}
