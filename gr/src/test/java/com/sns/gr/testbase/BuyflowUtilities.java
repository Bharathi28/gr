package com.sns.gr.testbase;

import java.sql.SQLException;
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

public class BuyflowUtilities {
	
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	
	public void click_cta(WebDriver driver, String env, String brand, String campaign, String category) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String step = "";
		if(category.equalsIgnoreCase("Kit")) {
			step = "Ordernow";
		}
		else {
			step = "Shop";
		}
				
		String query = "select * from cta_locators where brand='" + brand + "' and campaign='" + campaign + "' and step='" + step + "'";
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
	
	public boolean checkIfProduct(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		List<String> categories = db_obj.getCategory(brand, campaign, ppid);
		boolean product = false;
		if((categories.contains("kit")) && (categories.contains("Product"))){
			product = false;
		}
		else if(categories.contains("kit")){
			product = false;
		}

		else if(categories.contains("Product")||categories.contains("ShopKit")){

			product = true;
		}
		return product;
	}
	public boolean checkIfShopKit(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		List<String> categories = db_obj.getCategory(brand, campaign, ppid);
		boolean shopkit = false;
		if(categories.contains("Product")){
			shopkit = false;
		}
		else if(categories.contains("ShopKit")){
			shopkit = true;
		}
		return shopkit;
	}
	
	public void move_to_sas(WebDriver driver, String env, String brand, String campaign, String offercode, String category, String nav) throws ClassNotFoundException, SQLException, InterruptedException {
		System.out.println("Moving to SAS Page...");
		if(offercode.contains("single")){
			category ="Product";
		}
		if(nav.equalsIgnoreCase("brands-nav")) {
			driver.findElement(By.xpath("(//button[@class='menu-icon'])[1]")).click();
			Thread.sleep(1000);				
		}
		else {
			click_cta(driver,env,brand,campaign,category);
		}
		Thread.sleep(2000);
	}
	
	public void move_to_checkout(WebDriver driver, String brand, String campaign, String offer, String category) throws InterruptedException, ClassNotFoundException, SQLException {
		System.out.println("Moving to Checkout Page...");				
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		
		if(brand.contains("BodyFirm-")) {
			if(brand.contains("CrepeErase")) {
				brand = "CrepeErase";
			}
			else if(brand.contains("SpotFade")) {
				brand = "SpotFade";
			}
			else {
				brand = "BodyFirm";
			}
		}
		
		Thread.sleep(2000);
		// Check if the page is already in checkout
		if(driver.findElements(By.id("dwfrm_personinf_contact_email")).size() != 0) {
			
		}
		else if(driver.findElements(By.id("dwfrm_cart_billing_billingAddress_email_emailAddress")).size() != 0) {
			
		}
		else {			
			if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("crepeerase")) && (category.equalsIgnoreCase("product"))){
				campaign = "crepeerase";
			}
			List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "MoveToCheckout", category);
			
			String elementlocator = locator.get(0).get("ELEMENTLOCATOR").toString();
			String elementvalue = locator.get(0).get("ELEMENTVALUE").toString();
			
			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
//			WebDriverWait wait = new WebDriverWait(driver,50);
//			wait.until(ExpectedConditions.elementToBeClickable(element));	
			Thread.sleep(2000);
			element.click();
			Thread.sleep(2000);
		}	
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
	
	public String fetch_confoffercode(WebDriver driver, String brand, boolean single) throws ClassNotFoundException, SQLException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String realm = DBUtilities.get_realm(brand);
		String offercode = "";
		if(realm.equalsIgnoreCase("R2")) {
			// Also supplies to SB
			if(brand.equalsIgnoreCase("Sub-D")){
				offercode = (String) jse.executeScript("return app.omniMap.MainOfferCode");
			}
			else {
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
		System.out.println("Completing Order");
		WebDriverWait wait = new WebDriverWait(driver,50);
		String realm = DBUtilities.get_realm(brand);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		WebElement comp_order_element;
		if(realm.equalsIgnoreCase("R2")) {
			if(cc.toLowerCase().contains("paypal")) {
				comp_order_element = driver.findElement(By.xpath("//button[@class='cta-submit btn-primary']"));
			}
			else {
				comp_order_element = driver.findElement(By.id("contYourOrder"));
			}
		}
		else {
			if(cc.toLowerCase().contains("paypal")) {
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
		jse.executeScript("arguments[0].click();", comp_order_element);	
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
			element.sendKeys(value);
		}
	}
	
	public String fill_out_form(WebDriver driver, String brand, String campaign, String cc, String shipbill, String supply) throws InterruptedException, ClassNotFoundException, SQLException {
		WebDriverWait wait = new WebDriverWait(driver,50);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String realm = DBUtilities.get_realm(brand);
		
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

			Thread.sleep(5000);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='loginSection']//div//div[2]//a")));	
			driver.findElement(By.xpath("//div[@id='loginSection']//div//div[2]//a")).click();
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='login_emaildiv']//div//input")));			
			driver.findElement(By.xpath("//div[@id='login_emaildiv']//div//input")).sendKeys("testbuyer1@guthy-renker.com");
			
			if(driver.findElements(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).size() != 0) {
				driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).click();
			}
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='login_passworddiv']//div//input")));	
			driver.findElement(By.xpath("//div[@id='login_passworddiv']//div//input")).sendKeys("123456789");
			
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-submit']")));
			driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-submit']")).click();			
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(),'Choose a way to pay')]")));
			
			WebElement select_cc_continue = driver.findElement(By.xpath("//div[@class='buttons reviewButton']//button"));
			wait.until(ExpectedConditions.elementToBeClickable(select_cc_continue));
			jse.executeScript("arguments[0].click();", select_cc_continue);		
			
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='confirmButtonTop']")));	
			jse.executeScript("arguments[0].click();", driver.findElement(By.xpath("//input[@id='confirmButtonTop']")));
			
			wait.until(ExpectedConditions.numberOfWindowsToBe(1));
			driver.switchTo().window(winHandleBefore);
			fill_form_field(driver, realm, "Agree", "");
			return "testbuyer1@guthy-renker.com";
		}
		else {
			String alpha = RandomStringUtils.randomAlphabetic(9);
			String num = RandomStringUtils.randomNumeric(4);
			String email = alpha + "-" + num + "@yopmail.com";
			
			fill_form_field(driver, realm, "Email", email.toLowerCase());
			fill_form_field(driver, realm, "PhoneNumber", "8887878787");		
			fill_form_field(driver, realm, "FirstName", firstName());
			fill_form_field(driver, realm, "LastName", lastName());
			fill_form_field(driver, realm, "AddressLine1", "123 QATest st");
//			fill_form_field(driver, realm, "AddressLine1", "123 Anywhere street");
			if(campaign.equalsIgnoreCase("ca")) {
				fill_form_field(driver, realm, "City", "Anywhere");
				fill_form_field(driver, realm, "State", "NB");		
				fill_form_field(driver, realm, "Zip", "E3B7K6");
			}
			else {
				fill_form_field(driver, realm, "City", "El Segundo");
				fill_form_field(driver, realm, "State", "CA");		
				
				if(supply.equalsIgnoreCase("30")) {			
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
			

			if((brand.equalsIgnoreCase("Mally"))||(brand.equalsIgnoreCase("CrepeErase"))||(brand.equalsIgnoreCase("MeaningfulBeauty"))){

				driver.findElement(By.xpath("(//input[contains(@class,'input-text password')])[1]")).sendKeys("Grcweb123!");
			}
			if((supply.equalsIgnoreCase("90")) && (brand.equalsIgnoreCase("Volaire"))){	
				fill_form_field(driver, realm, "CardNumber", "4111111111111111");
			}
			else {
				fill_form_field(driver, realm, "CardNumber", getCCNumber(cc));
			}		
			fill_form_field(driver, realm, "Month", "12");
			fill_form_field(driver, realm, "Year", "2020");	
			jse.executeScript("window.scrollBy(0,200)", 0);
			Thread.sleep(2000);
			fill_form_field(driver, realm, "Agree", "");
			Thread.sleep(2000);
			return (email.toLowerCase());
		}
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
