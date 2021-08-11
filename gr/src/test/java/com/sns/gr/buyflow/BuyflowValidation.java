package com.sns.gr.buyflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
	
	String sendReportTo = "manibharathi@searchnscore.com";
	String testSet = "Core";
	
	@BeforeSuite
	public void getEmailId() {

//		System.setProperty("email", "aaqil@searchnscore.com,manibharathi@searchnscore.com");
//		System.setProperty("testset", "Top 3");
//		
		//sendReportTo = System.getProperty("email");
		//testSet = System.getProperty("testset");		
	}
	
	@DataProvider(name="buyflowInput", parallel=true)
	public Object[][] testData() {
		
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		System.out.println(day);
		
		Object[][] arrayObject = null;
		
		if(day == 7) {
			arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "Saturday");
		}
		else if(day == 1) {
			arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "Sunday");
		}
		else {
			if(testSet.equalsIgnoreCase("Core")) {
				arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "Core");
			}
			else if(testSet.equalsIgnoreCase("Top 3")){
				arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "Top 3");
			}
		}
		
//		arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/run_input.xlsx", "rundata");
		return arrayObject;
	}
	
	@Test(dataProvider="buyflowInput")
	public void buyflow(String env, String brand, String campaign, String category, String supply, String ppid, String url, String shipbill, String cc, String browser) throws IOException, ClassNotFoundException, SQLException, InterruptedException {		
									
		// Create Required Directories
		File newDirectory = new File(System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation", "Run Output");
		newDirectory.mkdir();
		newDirectory = new File(System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation", "Screenshots");
		newDirectory.mkdir();
		newDirectory = new File(System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation\\Screenshots", brand);
		newDirectory.mkdir();				
				
		BaseTest base_obj = new BaseTest();			
		WebDriver driver = base_obj.setUp(browser, "Local");
		driver.get(url);
		System.out.println("Loaded " + brand + " site...");
		driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);		
		
		if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
			driver.findElement(By.xpath("//button[@id='details-button']")).click();
			driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
		}
		
		String[] offer_array = ppid.split(",");	
		String[] category_array = category.split(",");
		
		if(offer_array[offer_array.length-1].contains("single")) {
		
			String[] single_array = offer_array[offer_array.length-1].split(" ");
			String no_of_singles_str = single_array[0];
			int no_of_singles = Integer.parseInt(no_of_singles_str);
				
			List<String> single_offers = sas_obj.fetch_random_singles(brand, "Core", no_of_singles);
					
			ppid = offer_array[0];
				
			for(String single_offer : single_offers) {
				ppid = ppid + "," + single_offer;
			}			
		}		
		
		String last_char = ppid.substring(ppid.length() - 1);
		if(last_char.equalsIgnoreCase(",")) {
			ppid = ppid.substring(0, ppid.length() - 1);
		}
		
		offer_array = ppid.split(",");		
				
		for(int i = 0; i < offer_array.length; i++) {	
				
			String current_category = category_array[i];
			System.out.println(current_category);
			
			bf_obj.move_to_sas(driver, env, brand, campaign, offer_array[i], current_category);
			sas_obj.get_offer(driver, env, brand, campaign, offer_array[i], current_category);
			if(i == ((offer_array.length)-1)) {
				bf_obj.move_to_checkout(driver, brand, campaign, current_category);
			}
			else {
				bf_obj.move_to_checkout(driver, brand, campaign, current_category);
				bf_obj.click_logo(driver, brand, campaign);
			}
		}		
		
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
		if(!(email.contains("testbuyer"))) {
			cc = "Visa";
		}

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
			checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review SalesTax");
			checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Paypal Review Total");
		}
		else {
			checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
			checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
			checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout SalesTax");
			checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Total");
		}			
		String checkout_pricing = checkout_subtotal + " ; " + checkout_shipping + " ; " + checkout_salestax + " ; " + checkout_total;
			
		bf_obj.complete_order(driver, brand, cc);
		Thread.sleep(2000);			
		
		String ppuPresent = db_obj.checkPPUPresent(brand, campaign, category_array[0]);
		
		if(category.contains("Kit")) {
			String upsell = bf_obj.check_upsell_select(brand, campaign, ppid, category);
			if((brand.equalsIgnoreCase("PrincipalSecret")) && (ppid.equalsIgnoreCase("BL6R33"))) {
				ppuPresent = "Yes";
				upsell = "No";
			}
			if((brand.equalsIgnoreCase("PrincipalSecret")) && (ppid.equalsIgnoreCase("BL6R34"))) {
				ppuPresent = "Yes";
				upsell = "Yes";
			}
			if((brand.equalsIgnoreCase("AllKind")) && (ppid.equalsIgnoreCase("EL2A0008"))) {
				ppuPresent = "No";
			}
			if(ppuPresent.equalsIgnoreCase("Yes")) {
				bf_obj.upsell_confirmation(driver, brand, campaign, upsell);
				Thread.sleep(2000);
			}
		}	
		
		conf_offercode = bf_obj.fetch_confoffercode(driver, brand);
			
		System.out.println("Expected Offercode : " + ppid);
		System.out.println("Actual Offercode : " + conf_offercode);
		String status = (ppid.equalsIgnoreCase(conf_offercode))?"PASS":"FAIL";		
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
		String conf_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation SalesTax");
		String conf_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Total");
			
		String conf_pricing = conf_subtotal + " ; " + conf_shipping + " ; " + conf_salestax + " ; " + conf_total;	
			
		List<String> output_row = new ArrayList<String>();
		output_row.add(env);
		output_row.add(brand);
		output_row.add(campaign);
		output_row.add(category);
		output_row.add(email);
		output_row.add(ppid);
		output_row.add(conf_offercode);
		output_row.add(status);
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
		
		Path testoutput_path = Paths.get(System.getProperty("user.dir") + "\\test-output\\emailable-report.html");
		Path target_path = Paths.get(System.getProperty("user.dir") + "\\target\\surefire-reports\\emailable-report.html");
		if (Files.exists(testoutput_path)) {
			attachmentList.add(System.getProperty("user.dir") + "\\test-output\\emailable-report.html");
		}
		else if (Files.exists(target_path)){
			attachmentList.add(System.getProperty("user.dir") + "\\target\\surefire-reports\\emailable-report.html");
		}	
		
		mailObj.sendEmail("Buyflow Results", sendReportTo, attachmentList);
	}
}
