package com.sns.gr.cartlang;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CartLangUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

public class CartLanguageParallel {
	
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	CartLangUtilities lang_obj = new CartLangUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	SASUtilities sas_obj = new SASUtilities();
	MailUtilities mailObj = new MailUtilities();
	
	List<List<String>> output = new ArrayList<List<String>>();
	String sendReportTo = "manibharathi@searchnscore.com , banuchitra@searchnscore.com";
	
	@DataProvider(name="cartLangInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = comm_obj.getExcelData(System.getProperty("user.dir") + "\\Input_Output\\CartLanguagePriceValidation\\cartlang_kittestdata.xlsx", "Sheet1");
		return arrayObject;
	}

	@Test(dataProvider="cartLangInput")
	public void CompleteValidation(String env, String brand, String campaign, String categories, String browser) throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
				
		String[] categoryArr = categories.split(",");			
		for(String category : categoryArr) {				
			System.out.println(category);
			
			List<Map<String, Object>> all_offers;
			if(category.equalsIgnoreCase("SubscribeandSave")) {
				all_offers = db_obj.fetch_all_by_category(brand, campaign, category);
			}
			else {
				if((brand.equalsIgnoreCase("ReclaimBotanical")) || (brand.equalsIgnoreCase("PrincipalSecret")) || (brand.equalsIgnoreCase("SheerCover"))){
					all_offers = db_obj.fetch_all_by_category(brand, campaign, category);
				}
				else {
					all_offers = db_obj.fetch_all_30day_kits(brand, campaign);
				}
			}			
			
			System.out.println(all_offers.size());
			
			for(Map<String, Object> offer : all_offers) {				
					
				String realm = DBUtilities.get_realm(brand);
				
				String url = "";
				if((env.equalsIgnoreCase("qa")) || (env.equalsIgnoreCase("prod")) || (env.equalsIgnoreCase("stg"))) {
					url = db_obj.getUrl(brand, campaign, env);
					System.out.println(url);
				}
				else {
					if(realm.equalsIgnoreCase("R2")) {
						url = "https://storefront:eComweb123@" + brand + "." + env + ".dw2.grdev.com";
					}
					else if(realm.equalsIgnoreCase("R4")) {
						url = "https://storefront:eComweb123@" + brand + "." + env + ".dw4.grdev.com";
					}
				}
										
				BaseTest base_obj = new BaseTest();			
				WebDriver driver = base_obj.setUp(browser, "Local");
				driver.get(url);
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				
				if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
					driver.findElement(By.xpath("//button[@id='details-button']")).click();
					driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
				}
					
//				if(category.equalsIgnoreCase("Kit")) {
					bf_obj.click_cta(driver, env, brand, campaign, category);
//				}
//				else {
//					bf_obj.click_cta(driver, env, brand, campaign, "Shop");
//				}
				
				
				int subscribe = 0;
				String tempCategory = "";
				if(category.equalsIgnoreCase("SubscribeandSave")) {
					subscribe = 1;
					tempCategory = "Product";
				}
				else {
					tempCategory = category;
				}
					
////				if(category.equalsIgnoreCase("Kit")) {			
//					sas_obj.select_offer(driver, env, brand, campaign, offer);
//				}
//				else if(category.equalsIgnoreCase("Product")) {
//													
//					Thread.sleep(3000);
//					sas_obj.select_offer(driver, env, brand, campaign, offer);
//				}
//				else if(category.equalsIgnoreCase("Subscribe")) {
//					// Subscribe
//					sas_obj.select_product(driver, offer, brand, campaign);
//					sas_obj.select_fragrance(driver, offer, brand, campaign);
//					sas_obj.select_subscribe(driver, offer, brand, campaign);
//				}			
				
				sas_obj.select_offer(driver, env, brand, campaign, offer, category, subscribe);
				
//				bf_obj.move_to_checkout(driver, brand, campaign, offer.get("PPID").toString(), 0);
				bf_obj.move_to_checkout(driver, brand, campaign, tempCategory);
				
				List<String> campaignPages = db_obj.getPages(brand, campaign);
				boolean upsell = campaignPages.contains("upsellpage");
				System.out.println("Upsell : " + upsell);
										
				String[] lang_price_arr;
				String cart_lang_price = " ";
				String cart_lang_shipping = " ";	
					
				// 30-Day
				String ppid = lang_obj.get_ppid(driver, brand, campaign, offer);
									
				System.out.println(offer.get("DESCRIPTION").toString());
					
				String cart_lang = lang_obj.get_cart_language(driver, brand);						
				if(cart_lang.equalsIgnoreCase("No Cart Language")) {
					cart_lang_price = " ";
					cart_lang_shipping = " ";
				}
				else {
					lang_price_arr = lang_obj.parse_cart_language(cart_lang);		
					cart_lang_price = lang_price_arr[1];
					cart_lang_shipping = lang_price_arr[2];
				}			
					
				String checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
				String checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");									
					
				String expectedPercentage;
				String result = null;
				if(realm.equalsIgnoreCase("R4")) {
					expectedPercentage = lang_obj.offerValidation(brand, campaign, offer.get("DESCRIPTION").toString());									
					// Assertions
					result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
				}
				else {
					result = lang_obj.validate_r2_price(ppid, cart_lang_price, cart_lang_shipping, checkout_subtotal, checkout_shipping);
				}				
				
				List<String> output_row_30 = new ArrayList<String>();
				output_row_30.add(env);
				output_row_30.add(brand);
				output_row_30.add(campaign);
				output_row_30.add(ppid);
				output_row_30.add(cart_lang_price);
				output_row_30.add(cart_lang_shipping);
				output_row_30.add(checkout_subtotal);	
				output_row_30.add(checkout_shipping);	
				output_row_30.add(result);
				output.add(output_row_30);
						
				System.out.println(output_row_30);
				System.out.println(output);					
				
				if(upsell) {
					// 90 - Day						
					bf_obj.fill_out_form(driver, brand, campaign, "VISA", "same", "90");
					bf_obj.complete_order(driver, brand, "VISA");
					bf_obj.upsell_confirmation(driver, brand, campaign, "Yes");
					
					ppid = lang_obj.get_ppid(driver, brand, campaign, offer);		
					cart_lang = lang_obj.get_cart_language(driver, brand);												
					lang_price_arr = lang_obj.parse_cart_language(cart_lang);
							
					cart_lang_price = lang_price_arr[1];
					cart_lang_shipping = lang_price_arr[2];					
						
					checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
					checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");					
						
					if(realm.equalsIgnoreCase("R4")) {
						expectedPercentage = lang_obj.offerValidation(brand, campaign, offer.get("DESCRIPTION").toString());					
						// Assertions
						result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
					}
					else {
						System.out.println("Checkout subtotal " + checkout_subtotal);
						System.out.println("Checkout shipping " + checkout_shipping);
						System.out.println("Cart lang subtotal " + cart_lang_price);
						System.out.println("Cart lang shipping " + cart_lang_shipping);
						result = lang_obj.validate_r2_price(ppid, cart_lang_price, cart_lang_shipping, checkout_subtotal, checkout_shipping);
					}						
					
					List<String> output_row_90 = new ArrayList<String>();
					output_row_90.add(env);
					output_row_90.add(brand);
					output_row_90.add(campaign);
					output_row_90.add(ppid);
					output_row_90.add(cart_lang_price);
					output_row_90.add(cart_lang_shipping);
					output_row_90.add(checkout_subtotal);	
					output_row_90.add(checkout_shipping);	
					output_row_90.add(result);
					output.add(output_row_90);
						
					System.out.println(output_row_90);
					System.out.println(output);			
				}
				driver.close();
			}
		}
	}// Test end
	
	@AfterSuite
	public void populateExcel() throws IOException {
		String file = comm_obj.populateOutputExcel(output, "CartLangPricingValidationResults", System.getProperty("user.dir") + "\\Input_Output\\CartLanguagePriceValidation\\Kit\\");
		List<String> attachmentList = new ArrayList<String>();
		attachmentList.add(file);
		mailObj.sendEmail("Cart Language Price Validation Results", sendReportTo, attachmentList);
	}
}
