package com.sns.gr.cartlang;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CartLangUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

public class CartLanguageParallel {
	
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	CartLangUtilities lang_obj = new CartLangUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	SASUtilities sas_obj = new SASUtilities();
	
	List<List<String>> output = new ArrayList<List<String>>();
	
	@DataProvider(name="cartLangInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = comm_obj.getExcelData("F:\\Automation\\Buyflow\\Cart Language Validation\\run_input.xlsx", "Sheet1");
		return arrayObject;
	}

	@Test(dataProvider="cartLangInput")
	public void CompleteValidation(String env, String brand, String campaign, String categories, String browser) throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		System.setProperty("webdriver.chrome.driver", "F:\\Automation\\Drivers\\chromedriver_win32\\chromedriver.exe");
		
		String[] categoryArr = categories.split(",");			
		for(String category : categoryArr) {				
			System.out.println(category);
			
			List<Map<String, Object>> all_offers = db_obj.fetch_all_30day_kits(brand, campaign);
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
					
				bf_obj.click_cta(driver, env, brand, campaign, category);
					
				if(category.equalsIgnoreCase("Kit")) {			
					sas_obj.select_offer(driver, env, brand, campaign, offer);
				}
				else if(category.equalsIgnoreCase("Product")) {
													
					Thread.sleep(3000);
					sas_obj.select_offer(driver, env, brand, campaign, offer);
				}
				else if(category.equalsIgnoreCase("Subscribe")) {
					// Subscribe
					sas_obj.select_product(driver, offer, brand, campaign);
					sas_obj.select_fragrance(driver, offer, brand, campaign);
					sas_obj.select_subscribe(driver, offer, brand, campaign);
				}			
				
				bf_obj.move_to_checkout(driver, brand, campaign, offer.get("ppid").toString(), 0);
				
				List<String> campaignPages = db_obj.getPages(brand, campaign);
				boolean upsell = campaignPages.contains("upsellpage");
				System.out.println("Upsell : " + upsell);
										
				String[] lang_price_arr;
				String cart_lang_price = " ";
				String cart_lang_shipping = " ";	
					
				// 30-Day
				String ppid;
				if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))){
					ppid = offer.get("ppid").toString();
				}
				else {
					ppid = driver.findElement(By.xpath("(//span[@class='PPID disclaimer-ppid'])[1]")).getText();	
				}						
				System.out.println(offer.get("description").toString());
					
				String cart_lang = lang_obj.get_cart_language(driver);						
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
					
				String expectedPercentage = lang_obj.offerValidation(brand, campaign, offer.get("description").toString());									
				// Assertions
				String result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
				
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
					bf_obj.fill_out_form(driver, brand, "VISA", "same", "90");
					bf_obj.complete_order(driver, brand);
					bf_obj.upsell_confirmation(driver, brand, campaign, "Yes");
					
					cart_lang = lang_obj.get_cart_language(driver);		
					if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))){
						ppid = offer.get("ppid").toString();
					}
					else {
						ppid = driver.findElement(By.xpath("(//span[@class='PPID disclaimer-ppid'])[1]")).getText();	
					}												
					lang_price_arr = lang_obj.parse_cart_language(cart_lang);
							
					cart_lang_price = lang_price_arr[1];
					cart_lang_shipping = lang_price_arr[2];					
						
					checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
					checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");					
						
					expectedPercentage = lang_obj.offerValidation(brand, campaign, offer.get("description").toString());					
					// Assertions
					result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
					
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
		comm_obj.populateOutputExcel(output, "CartLangPricingValidationResults", "F:\\Automation\\Buyflow\\Cart Language Validation\\");
	}
}
