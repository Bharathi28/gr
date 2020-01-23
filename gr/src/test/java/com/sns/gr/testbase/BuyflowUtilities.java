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
import org.openqa.selenium.support.ui.Select;

import com.sns.gr.testbase.DBLibrary;

public class BuyflowUtilities {
	
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	
	public void click_cta(WebDriver driver, String env, String brand, String campaign, String category) throws ClassNotFoundException, SQLException, InterruptedException {
		String step = "";
		if(category.equalsIgnoreCase("kit")) {
			step="Ordernow";
		}
		else if(category.equalsIgnoreCase("product")) {
			step="Shop";
		}
		else if(category.equalsIgnoreCase("subscribe")) {
			step="Shop";
		}
		String query = "select * from cta_locators where brand='" + brand + "' and campaign='" + campaign + "' and step='" + step + "';";
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch",query);
		
		if(env.toLowerCase().contains("dev")){
			String realm = DBUtilities.get_realm(brand);
			String elementlocator = locator.get(0).get("qalocator").toString();
			String elementvalue = locator.get(0).get("qavalue").toString();	
			if(realm.equalsIgnoreCase("R2")) {
				elementvalue = elementvalue.replace("qa", env.toLowerCase());
			}
			else {
				elementvalue = elementvalue.replace("grdev", env.toLowerCase()+".dw4.grdev");
			}
			if(!(elementvalue.equalsIgnoreCase("n/a"))) {
				WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
				element.click();
			}
		}
		else {
			String elementlocator = locator.get(0).get(env.toLowerCase() + "locator").toString();
			String elementvalue = locator.get(0).get(env.toLowerCase() + "value").toString();
			
			if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("specialCore"))) {
				if(driver.findElement(By.xpath("//div[@class='header-promo-slider']")).getAttribute("style").toString().equalsIgnoreCase("display: none;")) {
					driver.findElement(By.xpath("//div[@class='header-promo']")).click();
				}
			}
			
			if(!(elementvalue.equalsIgnoreCase("n/a"))) {
				WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
				Thread.sleep(2000);
				element.click();
			}
		}		
	}
	
	public void click_logo(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException {
		List<Map<String, Object>> logo_locators = comm_obj.get_element_locator(brand, campaign, "Logo", null);
		for(Map<String,Object> logo : logo_locators) {
			
			String elementvalue = logo.get("elementvalue").toString();
			if(driver.findElements(By.xpath(elementvalue)).size() != 0) {
				driver.findElement(By.xpath(elementvalue)).click();
				break;
			}
		}
	}		
	
	public boolean checkIfProduct(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		List<String> categories = db_obj.getCategory(brand, campaign, ppid);
		boolean product = false;
		if((categories.contains("kit")) && (categories.contains("product"))){
			product = false;
		}
		else if(categories.contains("kit")){
			product = false;
		}
		else if(categories.contains("product")){
			product = true;
		}
		return product;
	}
	
	public void move_to_sas(WebDriver driver, String env, String brand, String campaign, String offercode) throws ClassNotFoundException, SQLException, InterruptedException {
		
		if((offercode.contains("single")) || (checkIfProduct(brand, campaign, offercode))){
			click_cta(driver,env,brand,campaign,"Product");
		}
		else {
			click_cta(driver,env,brand,campaign,"Kit");
		}
	}
	
	public void move_to_checkout(WebDriver driver, String brand, String campaign, String offer, int singlecheck) throws InterruptedException, ClassNotFoundException, SQLException {
		String category;
		if(offer.contains(",")) {
			category = "Product";
		}
		else {
			if(singlecheck == 1) {
				category = "Product";
			}
			else {
				category = "Kit";
			}
		}
				
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		Thread.sleep(2000);
		// Check if the page is already in checkout
		if(driver.findElements(By.id("dwfrm_personinf_contact_email")).size() != 0) {
			
		}
		else if(driver.findElements(By.id("dwfrm_cart_billing_billingAddress_email_emailAddress")).size() != 0) {
			
		}
		else {			
			List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "MoveToCheckout", category);
			
			String elementlocator = locator.get(0).get("elementlocator").toString();
			String elementvalue = locator.get(0).get("elementvalue").toString();
			
			WebElement element = comm_obj.find_webelement(driver, elementlocator, elementvalue);
			element.click();
			Thread.sleep(2000);
		}	
	}
	
	public void upsell_confirmation(WebDriver driver, String brand, String campaign, String upsell) throws InterruptedException, ClassNotFoundException, SQLException {
		Thread.sleep(1000);
				
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "Upsell", upsell);
			
		String elementlocator = locator.get(0).get("elementlocator").toString();
		String elementvalue = locator.get(0).get("elementvalue").toString();
		
		List<WebElement> elements = comm_obj.find_mulwebelement(driver, elementlocator, elementvalue);
		if(elements.size() != 0) {
			comm_obj.find_webelement(driver, elementlocator, elementvalue).click();
		}
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
			if(single) {
				int arrSize = arr.length;
				System.out.println("length : " + arrSize);
				if((brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("WestmoreBeauty")) || (brand.equalsIgnoreCase("Smileactives"))) {
					offercode = arr[1] + "," + arr[4];
				}
				else if((brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally"))) {
					offercode = arr[1] + "," + arr[7];
				}
				else if(brand.equalsIgnoreCase("Dr.Denese")) {
					if(arrSize>9) {
						offercode = arr[1] + "," + arr[10];
					}
					else {
						offercode = arr[1] + "," + arr[7];
					}
					
				}
				else {
					offercode = arr[1];
				}
			}
			else {
				offercode = arr[1];
			}			
			
			for(String a : arr) {
				System.out.println(a);
			}
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
	
	public void complete_order(WebDriver driver, String brand, String cc) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		WebElement comp_order_element;
		if(realm.equalsIgnoreCase("R2")) {
			comp_order_element = driver.findElement(By.id("contYourOrder"));
		}
		else {
			if(cc.equalsIgnoreCase("paypal")) {
				comp_order_element = driver.findElement(By.id("submitButton"));
			}
			else {
				comp_order_element = driver.findElement(By.id("trigerPlaceOrder"));
			}			
		}
		comp_order_element.click();
	}
	
	public void fill_form_field(WebDriver driver, String realm, String field, String value) throws ClassNotFoundException, SQLException {
		
		String query = "select * from form_fields_locators where realm='" + realm + "' and form='Checkout' and field='" + field + "';";
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch", query);
		
		String elementlocator = locator.get(0).get("elementlocator").toString();
		String elementvalue = locator.get(0).get("elementvalue").toString();
		
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
	
	public String fill_out_form(WebDriver driver, String brand, String cc, String shipbill, String supply) throws InterruptedException, ClassNotFoundException, SQLException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String realm = DBUtilities.get_realm(brand);
		
		if(cc.equalsIgnoreCase("paypal")) {
			driver.findElement(By.xpath("//div[@id='paypalSection']//div//div")).click();
			Thread.sleep(5000);

			String winHandleBefore = driver.getWindowHandle();
			for(String winHandle : driver.getWindowHandles()){
			   driver.switchTo().window(winHandle);
			}

			driver.findElement(By.xpath("//div[@id='loginSection']//div//div[2]//a")).click();
			Thread.sleep(5000);
			driver.findElement(By.xpath("//div[@id='login_emaildiv']//div//input")).sendKeys("testbuyer2@guthy-renker.com");
			Thread.sleep(2000);
			if(driver.findElements(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).size() != 0) {
				driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-click-next']")).click();
				Thread.sleep(2000);
			}
			driver.findElement(By.xpath("//div[@id='login_passworddiv']//div//input")).sendKeys("123456789");
			Thread.sleep(2000);
			driver.findElement(By.xpath("//button[@class='button actionContinue scTrack:unifiedlogin-login-submit']")).click();
			Thread.sleep(6000);
			driver.findElement(By.xpath("//div[@class='buttons reviewButton']//button")).click();
			Thread.sleep(2000);
			driver.findElement(By.xpath("//input[@id='confirmButtonTop']")).click();
			Thread.sleep(7000);

			driver.switchTo().window(winHandleBefore);
			Thread.sleep(7000);
			driver.findElement(By.xpath("//input[@id='dwfrm_cart_agree']")).click();
			Thread.sleep(2000);						
			return "testbuyer2@guthy-renker.com";
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
			fill_form_field(driver, realm, "City", "El Segundo");
			fill_form_field(driver, realm, "State", "CA");		
			
			if(supply.equalsIgnoreCase("30")) {			
				fill_form_field(driver, realm, "Zip", "90245");
			}
			else if(supply.equalsIgnoreCase("90")) {
				fill_form_field(driver, realm, "Zip", "81002");
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
				fill_form_field(driver, realm, "CardNumber", "4111111111111122");
			}
			else {
				fill_form_field(driver, realm, "CardNumber", getCCNumber(cc));
			}		
			fill_form_field(driver, realm, "Month", "12");
			fill_form_field(driver, realm, "Year", "2020");
			fill_form_field(driver, realm, "Agree", "");	
			
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
	
//	public String calculateKitProductCount(String[] arr, String brand, String campaign) throws ClassNotFoundException, SQLException {
//		String result = "";
//		int kit_count = 0;
//		int prod_count = 0;
//		
//		for(String code : arr) {
//			List<Map<String, Object>> offerdata = get_offerdata(code, brand, campaign);
//			String category = offerdata.get(0).get("category").toString();
//			if(category.equalsIgnoreCase("kit")) {
//				kit_count++;
//			}
//			else if(category.equalsIgnoreCase("product")) {
//				prod_count++;
//			}
//		}
//		result = kit_count + " kit " + prod_count + " product";
//		return result;
//	}
	
//	public void calculateIndividualProductCount(String[] arr, int count, String brand, String campaign) throws ClassNotFoundException, SQLException {
//		List<String> products = new ArrayList<String>();
//		HashMap<String,Integer> prodCount=new LinkedHashMap<String,Integer>();
//		
//		for(String code : arr) {
//			List<Map<String, Object>> offerdata = get_offerdata(code, brand, campaign);
//			String category = offerdata.get(0).get("category").toString();
//			
//			if(category.equalsIgnoreCase("product")) {
//				if(!(products.contains(code))) {
//					products.add(code);
//				}
//			}
//		}				
//		System.out.println(products);
//		
//		for(String code : products) {
//			int number = 0;
//			for(String offer : arr) {
//				if(offer.equalsIgnoreCase(code)) {
//					number++;
//				}
//			}
//			System.out.println(code + " : " + number);
//			prodCount.put(code, number);
//		}
//		System.out.println(prodCount);
//	}
}
