package com.sns.gr.cartlang;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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

public class CartLanguageValidationComplete {
	
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	CartLangUtilities lang_obj = new CartLangUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	SASUtilities sas_obj = new SASUtilities();

	@Test
	public void CompleteValidation() throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		System.setProperty("webdriver.chrome.driver", "F:\\Automation\\Drivers\\chromedriver_win32\\chromedriver.exe");		
		
		// Detailed Output
		List<List<String>> output = new ArrayList<List<String>>();
		
		List<String> header_list = new ArrayList<String>();
		header_list.add("Environment");
		header_list.add("Brand");
		header_list.add("Campaign");
		header_list.add("PPID");
		header_list.add("Shop Price");
		header_list.add("SAS Price");
		header_list.add("PDP Price");
		header_list.add("Cart Price");
		header_list.add("Cart Language Price");
		header_list.add("Cart Language Shipping");
		header_list.add("Installments");
		header_list.add("Checkout Subtotal");
		header_list.add("Checkout Shipping");
		header_list.add("Result");
				
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
				List<Map<String, Object>> all_offers = db_obj.fetch_all_by_category(brand, campaign, category);
				System.out.println(all_offers.size());
				for(Map<String, Object> offer : all_offers) {
					List<String> output_row = new ArrayList<String>();
					output_row.add(env);
					output_row.add(brand);
					output_row.add(campaign);
					
					String shop_price = "";
					String sas_price = "";
					String product_price = "";
					String inst_price = "";
					
					output_row.add(offer.get("ppid").toString());
					
					System.out.println(offer);
					
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
						// SAS Page price - only for Kits
						if(category.equalsIgnoreCase("Kit")) {
							sas_price = pr_obj.fetch_sas_current_price(driver, env, brand, campaign, offer.get("ppid").toString());
							System.out.println("SAS Price : " + sas_price);
						}				
						sas_obj.select_offer(driver, env, brand, campaign, offer);
					}
					else if(category.equalsIgnoreCase("Product")) {
						
						// Shop Page price - fetching from 'Shop Now' button					
						shop_price = pr_obj.fetch_shop_pricing(driver, env, brand, campaign, offer.get("ppid").toString());
						System.out.println("Shop Page Price : " + shop_price);
							
						Thread.sleep(3000);
						sas_obj.select_offer(driver, env, brand, campaign, offer);
						
						// PDP Page price - only for Products										
						product_price = driver.findElement(By.xpath("//div[contains(@class,'one-time active')]//strong")).getText();
						System.out.println("PDP Price : " + product_price);
					}
					else if(category.equalsIgnoreCase("Subscribe")) {
						// Subscribe
						sas_obj.select_product(driver, offer, brand, campaign);
						sas_obj.select_fragrance(driver, offer, brand, campaign);
						sas_obj.select_subscribe(driver, offer, brand, campaign);
					}			
					
					output_row.add(shop_price);	
					output_row.add(sas_price);
					output_row.add(product_price);
					
					bf_obj.move_to_checkout(driver, brand, campaign, offer.get("ppid").toString(), 0);
					String supply = offer.get("supply").toString();
					
					String[] lang_price_arr;
					String[] inst_arr;
					
					String cart_lang_price = " ";
					String cart_lang_shipping = " ";
					if(supply.equalsIgnoreCase("30")) {
						String cart_lang = lang_obj.get_cart_language(driver, brand);
						
						String total_price = driver.findElement(By.xpath("(//div[@class='cart-product-items clearfix'])[1]//div[2]//ul//li[contains(@class,'item-total')]//span[2]")).getText();
						System.out.println("Total Price : " + total_price);
						output_row.add(total_price);
						
						System.out.println("30 day cart language : " + cart_lang);
						
						if(cart_lang.equalsIgnoreCase("No Cart Language")) {
							output_row.add("");
							output_row.add("");
						}
						else {
							lang_price_arr = lang_obj.parse_cart_language(cart_lang);
							
							cart_lang_price = lang_price_arr[1];
							cart_lang_shipping = lang_price_arr[2];
							
							output_row.add(cart_lang_price);
							output_row.add(cart_lang_shipping);
						}
					}				
					else if(supply.equalsIgnoreCase("90")) {
						bf_obj.fill_out_form(driver, brand, campaign, "VISA", "same", supply);
						bf_obj.complete_order(driver, brand, "VISA");
						bf_obj.upsell_confirmation(driver, brand, campaign, "Yes");
						String cart_lang = lang_obj.get_cart_language(driver, brand);
						
						String total_price = driver.findElement(By.xpath("(//div[@class='cart-product-items clearfix'])[1]//div[2]//ul//li[contains(@class,'item-total')]//span[2]")).getText();
						
						System.out.println("Total Price : " + total_price);
						System.out.println("90 day cart language : " + cart_lang);
						lang_price_arr = lang_obj.parse_cart_language(cart_lang);
						
						String installments = lang_obj.get_installments_text(driver);		
						System.out.println("Installments Text : " + installments);
						inst_arr = lang_obj.parse_installments_language(installments);
						inst_price = String.join(",", inst_arr);
						
						output_row.add(total_price);			
						
						cart_lang_price = lang_price_arr[1];
						cart_lang_shipping = lang_price_arr[2];
											
						output_row.add(cart_lang_price);
						output_row.add(cart_lang_shipping);
					}
					output_row.add(inst_price);
					String checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
					String checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
									
					System.out.println("Checkout subtotal : " + checkout_subtotal);
					System.out.println("Checkout shipping : " + checkout_shipping);
					
					System.out.println();
					System.out.println();
					
					output_row.add(checkout_subtotal);		
					output_row.add(checkout_shipping);	
					
					String offersPresent = lang_obj.checkOffers(brand, campaign, offer.get("ppid").toString());
					String expectedPercentage;
					if(offersPresent.equalsIgnoreCase("Offer Available")) {
						Map<String, Object> campaignOffer = lang_obj.getOffers(brand, campaign, offer.get("ppid").toString());
						// Assertions
						expectedPercentage = campaignOffer.get("offerPercent").toString();
					}
					else {
						expectedPercentage = "0";
					}
					// Assertions
					String result = lang_obj.validate_subtotal(cart_lang_price, checkout_subtotal, expectedPercentage);
					output_row.add(result);
					
					output.add(output_row);			
					
					comm_obj.write_output(output, brand, campaign, "CartLangValidation", "F:\\Automation\\Buyflow\\Cart Language Validation\\");		
					
					driver.close();
				}
			}
		}
	}
}