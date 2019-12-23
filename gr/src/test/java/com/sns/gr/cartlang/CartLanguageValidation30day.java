package com.sns.gr.cartlang;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CartLangUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

import net.lightbody.bmp.BrowserMobProxy;

public class CartLanguageValidation30day {

	BuyflowUtilities bf_obj = new BuyflowUtilities();
	CartLangUtilities lang_obj = new CartLangUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	SASUtilities sas_obj = new SASUtilities();

	@Test
	public void CompleteValidation() throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		System.setProperty("webdriver.chrome.driver", "F:\\Automation\\Drivers\\chromedriver_win32\\chromedriver.exe");
		
		// Output
		List<List<String>> output = new ArrayList<List<String>>();
		
		List<String> header_list = new ArrayList<String>();
		header_list.add("Environment");
		header_list.add("Brand");
		header_list.add("Campaign");
		header_list.add("PPID");
		header_list.add("Continuity Language Price");
		header_list.add("Continuity Shipping");
		header_list.add("Checkout Subtotal");
		header_list.add("Checkout Shipping");
		header_list.add("");
		
		output.add(header_list);
		
		String[][] testdata = comm_obj.getExcelData("F:\\Automation\\Buyflow\\Cart Language Validation\\run_input.xlsx", "Sheet1");
		
		for(int itr=0; itr<testdata.length; itr++) {
			String env = testdata[itr][0];
			String brand = testdata[itr][1];
			String campaign = testdata[itr][2];
			String categories = testdata[itr][3];
			String[] categoryArr = categories.split(",");
			String browser = testdata[itr][4];
			
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
										
					System.out.println(offer);
					System.out.println(offer.get("ppid").toString());
					String[] lang_price_arr;
					String cart_lang_price = " ";
					String cart_lang_shipping = " ";	
					
					// 30-Day
					List<String> output_row_30 = new ArrayList<String>();
					output_row_30.add(env);
					output_row_30.add(brand);
					output_row_30.add(campaign);		
					String ppid = driver.findElement(By.xpath("(//span[@class='PPID disclaimer-ppid'])[1]")).getText();
					output_row_30.add(ppid);
					
					String cart_lang = lang_obj.get_cart_language(driver);						
					if(cart_lang.equalsIgnoreCase("No Cart Language")) {
						output_row_30.add("");
						output_row_30.add("");
					}
					else {
						lang_price_arr = lang_obj.parse_cart_language(cart_lang);
							
						cart_lang_price = lang_price_arr[1];
						cart_lang_shipping = lang_price_arr[2];
							
						output_row_30.add(cart_lang_price);
						output_row_30.add(cart_lang_shipping);
					}			
					
					String checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
					String checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");	
				
					output_row_30.add(checkout_subtotal);	
					output_row_30.add(checkout_shipping);					
					
					String expectedPercentage = lang_obj.offerValidation(brand, campaign, ppid);
									
					// Assertions
					String result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
					output_row_30.add(result);
					
					output.add(output_row_30);		
					System.out.println(output_row_30);
					System.out.println(output);
										
					if((!(brand.equalsIgnoreCase("Dr.Denese"))) && (!((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("sawb19")))) && (!((brand.equalsIgnoreCase("MeaningfulBeauty")) && (campaign.equalsIgnoreCase("mb7holidaydeluxe25off"))))) {
						// 90 - Day
						List<String> output_row_90 = new ArrayList<String>();
						output_row_90.add(env);
						output_row_90.add(brand);
						output_row_90.add(campaign);							
						
						bf_obj.fill_out_form(driver, brand, "VISA", "same", "90");
						bf_obj.complete_order(driver, brand);
						bf_obj.upsell_confirmation(driver, brand, campaign, "Yes");
						cart_lang = lang_obj.get_cart_language(driver);
							
						ppid = driver.findElement(By.xpath("(//span[@class='PPID disclaimer-ppid'])[1]")).getText();
						output_row_90.add(ppid);
						
						lang_price_arr = lang_obj.parse_cart_language(cart_lang);
							
						cart_lang_price = lang_price_arr[1];
						cart_lang_shipping = lang_price_arr[2];
												
						output_row_90.add(cart_lang_price);
						output_row_90.add(cart_lang_shipping);
						
						checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
						checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
					
						output_row_90.add(checkout_subtotal);	
						output_row_90.add(checkout_shipping);	
						
						expectedPercentage = lang_obj.offerValidation(brand, campaign, ppid);
					
						// Assertions
						result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
						output_row_90.add(result);
						
						output.add(output_row_90);	
						System.out.println(output_row_90);
						System.out.println(output);			
					}
					driver.close();
				}
			}
			comm_obj.write_output(output, brand, campaign, "CartLangValidation", "F:\\Automation\\Buyflow\\Cart Language Validation\\");
		}// per row
	}// Test end
}
