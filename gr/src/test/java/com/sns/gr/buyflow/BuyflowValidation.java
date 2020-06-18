package com.sns.gr.buyflow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class BuyflowValidation {

	CommonUtilities comm_obj = new CommonUtilities();
	BaseTest base_obj = new BaseTest();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	DBUtilities db_obj = new DBUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	MailUtilities mailObj = new MailUtilities();
	Scanner in = new Scanner(System.in);
	
	List<List<String>> output = new ArrayList<List<String>>();
	String sendReportTo = "";
	
	@BeforeSuite
	public void getEmailId() {
		System.out.println("Enter Email id : ");
		sendReportTo = in.next();
	}
	
	@DataProvider(name="buyflowInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = null;
		arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "rundata");
		return arrayObject;
	}
	
	@Test(dataProvider="buyflowInput")
	public void buyflow(String env, String brand, String campaign, String categoryy, String nav, String supply, String ppid, String url, String shipbill, String cc, String browser) throws IOException, ClassNotFoundException, SQLException, InterruptedException {		
									
		BaseTest base_obj = new BaseTest();			
		WebDriver driver = base_obj.setUp(browser, "Local");
		driver.get(url);
		System.out.println("Loaded " + brand + " site...");
		driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);		
		
		if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
			driver.findElement(By.xpath("//button[@id='details-button']")).click();
			driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
		}
		int subscribe =  0;
		String str = "";
		String[] offer_array = ppid.split(",");	
		
		if(offer_array[offer_array.length-1].contains("single")) {
			categoryy = "Kit";
			String[] single_array = offer_array[offer_array.length-1].split(" ");
			String no_of_singles_str = single_array[0];
			int no_of_singles = Integer.parseInt(no_of_singles_str);
				
			String single_brand = "";
			String single_campaign = "";
			if(brand.equalsIgnoreCase("BodyFirm")) {
				List<String> combo_brand_campaign = bf_obj.check_ppid_in_combo(brand, campaign, offer_array[0], categoryy);
				single_brand = combo_brand_campaign.get(0);
				single_campaign = combo_brand_campaign.get(1);
			}
			else {
				single_brand = brand;
				single_campaign = campaign;
			}
			List<String> single_offers = sas_obj.fetch_random_singles(single_brand, single_campaign, no_of_singles);
					
			ppid = offer_array[0];
				
			for(String single_offer : single_offers) {
				ppid = ppid + "," + single_offer;
			}			
		}		
		
		String last_char = ppid.substring(ppid.length() - 1);
		if(last_char.equalsIgnoreCase(",")) {
			ppid = ppid.substring(0, ppid.length() - 1);
		}
		System.out.println(ppid);
		
		String tempCategory = "";
		String tempCampaign = campaign;
		offer_array = ppid.split(",");		
				
		for(int i = 0; i < offer_array.length; i++) {	
			System.out.println(offer_array[i]);
			System.out.println(categoryy);
			
			String offer_brand = "";
			String offer_campaign = "";			
			
			if(brand.equalsIgnoreCase("BodyFirm")){
				List<String> combo_brand_campaign = bf_obj.check_ppid_in_combo(brand, campaign, offer_array[i], categoryy);	
				offer_brand = combo_brand_campaign.get(0);
				offer_campaign = combo_brand_campaign.get(1);
				System.out.println("offer---" + offer_array[i]);
				System.out.println("offer_brand---" + offer_brand);
				System.out.println("offer_campaign---" + offer_campaign);
				String isProduct = db_obj.isProduct(offer_brand, offer_array[i]);
				if(isProduct.equalsIgnoreCase("yes")) {
					tempCategory = "Product";
				}
				else {
					tempCategory = "Kit";
				}
				
				if(nav.equalsIgnoreCase("brands-nav")) {
					bf_obj.combo_navigation_to_sas(driver, env, brand, campaign, offer_brand, offer_campaign, nav, tempCategory);
				}
				else {
					bf_obj.move_to_sas(driver, env, offer_brand, offer_campaign, offer_array[i], tempCategory, nav);
				}
			}
			else {
				offer_brand = brand;
				offer_campaign = campaign;
				String isProduct = db_obj.isProduct(offer_brand, offer_array[i]);
				if(isProduct.equalsIgnoreCase("yes")) {
					tempCategory = "Product";
				}
				else {
					tempCategory = "Kit";
				}
				bf_obj.move_to_sas(driver, env, offer_brand, offer_campaign, offer_array[i], tempCategory, nav);
			}
			
			String camp_cat_val = bf_obj.campaign_repeat_validation(categoryy, offer_brand, offer_campaign, offer_array[i]);
			String[] camp_cat_val_arr = camp_cat_val.split("-");
			
			tempCategory = camp_cat_val_arr[0];
			campaign = camp_cat_val_arr[1];
			subscribe =  Integer.parseInt(camp_cat_val_arr[2]);
			System.out.println(tempCategory);					
			
			sas_obj.get_offer(driver, env, offer_brand, offer_campaign, offer_array[i], tempCategory, subscribe, nav);
			if(i == ((offer_array.length)-1)) {
				bf_obj.move_to_checkout(driver, offer_brand, offer_campaign, tempCategory);
			}
			else {
				bf_obj.move_to_checkout(driver, offer_brand, offer_campaign, tempCategory);
				bf_obj.click_logo(driver, brand, campaign);
			}
		}		
		campaign = tempCampaign;
		
		if(driver.findElements(By.xpath("//a[@id='creditCardPath']")).size() != 0) {
			if(driver.findElement(By.xpath("//a[@id='creditCardPath']")).isDisplayed()){
				driver.findElement(By.xpath("//a[@id='creditCardPath']")).click();
			}
		}			
			
		String conf_offercode = null;
			
		// For 30-day supply cart ppid and confirmation ppid are same
		if(supply.contains("30")) {
			conf_offercode = bf_obj.fetch_offercode(driver, brand);
		}
						
		String email = bf_obj.fill_out_form(driver, brand, campaign, cc, shipbill, "30");
		System.out.println("Email : " + email);							

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollBy(0,-200)", 0);
			
		String checkout_subtotal = "";
		String checkout_shipping = "";
		String checkout_salestax = "";
		String checkout_total = "";
		
		String realm = DBUtilities.get_realm(brand);
		
		if((cc.equalsIgnoreCase("Paypal")) && (realm.equalsIgnoreCase("R2"))) {
			checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review Subtotal");
			checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review Shipping");
			checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review Salestax");
			checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review Total");
		}
		else {
			checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
			checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
			checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Salestax");
			checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Total");
		}			
		String checkout_pricing = checkout_subtotal + " ; " + checkout_shipping + " ; " + checkout_salestax + " ; " + checkout_total;
			
		bf_obj.complete_order(driver, brand, cc);
		Thread.sleep(2000);			
		
		if(categoryy.contains("Kit")) {
			String upsell = bf_obj.check_upsell_select(brand, campaign, ppid, categoryy, nav);
			bf_obj.upsell_confirmation(driver, brand, campaign, upsell);
			Thread.sleep(2000);
		}	
		
		conf_offercode = bf_obj.fetch_confoffercode(driver, brand, ppid.contains("single"));
			
		System.out.println("Expected Offercode : " + ppid);
		System.out.println("Actual Offercode : " + conf_offercode);
			
		Thread.sleep(2000);
		Screenshot confpage = new AShot().takeScreenshot(driver);
		ImageIO.write(confpage.getImage(),"PNG",new File(System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation\\Screenshots\\" + brand + "\\" + campaign + "_" + ppid +".png"));
		
		String conf_num = bf_obj.fetch_conf_num(driver, brand);
		System.out.println("Confirmation Number : " + conf_num);	
			
		last_char = conf_offercode.substring(conf_offercode.length() - 1);
		if(last_char.equalsIgnoreCase(",")) {
			conf_offercode = conf_offercode.substring(0, conf_offercode.length() - 1);
		}		
			
		String conf_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Subtotal");
		String conf_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Shipping");
		String conf_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Salestax");
		String conf_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Total");
			
		String conf_pricing = conf_subtotal + " ; " + conf_shipping + " ; " + conf_salestax + " ; " + conf_total;	
			
		List<String> output_row = new ArrayList<String>();
		output_row.add(env);
		output_row.add(brand);
		output_row.add(campaign);
		output_row.add(categoryy);
		output_row.add(email);
		output_row.add(ppid);
		output_row.add(conf_offercode);
		output_row.add(conf_num);
		output_row.add(checkout_pricing);
		output_row.add(conf_pricing);	
		output_row.add(shipbill);	
		output_row.add(cc);	
		output_row.add(browser);	
		output.add(output_row);
		
		driver.close();
	}
	
	@AfterSuite
	public void populateExcel() throws IOException {
		String file = comm_obj.populateOutputExcel(output, "BuyflowResults", System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation\\Run Output\\");
		
		List<String> attachmentList = new ArrayList<String>();
		attachmentList.add(file);
		mailObj.sendEmail("Buyflow Results", sendReportTo, attachmentList);
	}
}
