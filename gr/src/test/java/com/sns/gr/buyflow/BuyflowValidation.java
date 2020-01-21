package com.sns.gr.buyflow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class BuyflowValidation{

	CommonUtilities comm_obj = new CommonUtilities();
	BaseTest base_obj = new BaseTest();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	DBUtilities db_obj = new DBUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	MailUtilities mailObj = new MailUtilities();
	
	List<List<String>> output = new ArrayList<List<String>>();
	String sendReportTo = "manibharathi@searchnscore.com , banuchitra@searchnscore.com";
	
//	@BeforeSuite
//	public void getEmailId() {
//		Scanner in = new Scanner(System.in);
//		System.out.println("Enter Email id : ");
//		sendReportTo = in.next();
//	}

	@DataProvider(name="buyflowInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = null;

		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK); 
		
		if(day==7){
			arrayObject = comm_obj.getExcelData("C:\\Automation\\Buyflow\\DailyOrders\\SaturdayInput.xlsx", "rundata");
		}
		else if(day==1){
			arrayObject = comm_obj.getExcelData("C:\\Automation\\Buyflow\\DailyOrders\\SundayInput.xlsx", "rundata");
		}
		else{
			arrayObject = comm_obj.getExcelData("C:\\Automation\\Buyflow\\DailyOrders\\run_input.xlsx", "rundata");
		}		
		return arrayObject;
	}
	
	@Test(dataProvider="buyflowInput")
	public void buyflow(String env, String brand, String campaign,String supply, String ppid, String url, String shipbill, String cc, String upsell, String browser) throws IOException, ClassNotFoundException, SQLException, InterruptedException {		
									
		BaseTest base_obj = new BaseTest();			
		WebDriver driver = base_obj.setUp(browser, "Local");
		driver.get(url);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);		
		
		if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
			driver.findElement(By.xpath("//button[@id='details-button']")).click();
			driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
		}
		
		int singleCheck = 0;
		String str = "";
		String[] offer_array = ppid.split(",");		
			
		for(int i = 0; i < offer_array.length; i++) {				
			if((offer_array[i].contains("single")) || (bf_obj.checkIfProduct(brand, campaign, offer_array[i]))){
				singleCheck = 1;
			}
			bf_obj.move_to_sas(driver, env, brand, campaign, offer_array[i]);
			String ppidStr = sas_obj.get_offer(driver, env, brand, campaign, offer_array[i]);
			if(i == ((offer_array.length)-1)) {
				bf_obj.move_to_checkout(driver, brand, campaign, ppidStr, singleCheck);
			}
			else {
				bf_obj.move_to_checkout(driver, brand, campaign, ppidStr, singleCheck);
				bf_obj.click_logo(driver, brand, campaign);
			}
			str = str + ppidStr + ",";
			singleCheck = 0;
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
						
		String email = bf_obj.fill_out_form(driver, brand, cc, shipbill, "30");
		System.out.println("Email : " + email);							

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollBy(0,-200)", 0);
			
		String checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
		String checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
		String checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Salestax");
		String checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Total");
			
		String checkout_pricing = checkout_subtotal + " ; " + checkout_shipping + " ; " + checkout_salestax + " ; " + checkout_total;	
			
		bf_obj.complete_order(driver, brand, cc);
		Thread.sleep(1000);
			
		if(driver.findElements(By.id("popup-place-order-fd")).size() != 0) {
			String additionalOrderPopup = driver.findElement(By.id("popup-place-order-fd")).getAttribute("aria-hidden");
			if(additionalOrderPopup.equalsIgnoreCase("false")) {
				driver.findElement(By.xpath("//a[text()='Place Additional Order']")).click();
			}
		}
					
		Thread.sleep(2000);		
		String ppu = db_obj.checkPPUPresent(brand, campaign);
		if(ppu.equalsIgnoreCase("Yes")) {
			bf_obj.upsell_confirmation(driver, brand, campaign, upsell);
		}

		Thread.sleep(2000);
		
		conf_offercode = bf_obj.fetch_confoffercode(driver, brand, ppid.contains("single"));
			
		System.out.println("Expected Offercode : " + ppid);
		System.out.println("Actual Offercode : " + conf_offercode);
			
		Thread.sleep(2000);
			
		Screenshot confpage = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);		 
		ImageIO.write(confpage.getImage(),"PNG",new File("C:\\Automation\\Buyflow\\DailyOrders\\Screenshots\\" + brand + "\\" + ppid +".png"));
			
		String conf_num = bf_obj.fetch_conf_num(driver, brand);
		System.out.println("Confirmation Number : " + conf_num);	
			
		String last_char = conf_offercode.substring(conf_offercode.length() - 1);
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
		output_row.add(email);
		output_row.add(ppid);
		output_row.add(conf_offercode);
		output_row.add(conf_num);
		output_row.add("Yes");
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
		String file = comm_obj.populateOutputExcel(output, "BuyflowResults", "C:\\Automation\\Buyflow\\DailyOrders\\Run Output\\");
		mailObj.sendEmail("Buyflow Results", sendReportTo, file);
	}
}
