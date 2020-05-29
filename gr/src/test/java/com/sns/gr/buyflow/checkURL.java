package com.sns.gr.buyflow;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sns.gr.testbase.DBLibrary;

public class checkURL {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
		System.setProperty("webdriver.chrome.driver", "D:\\Bharathi\\Automation\\Drivers\\chromedriver_win32\\chromedriver.exe");
		
		String brand ="CrepeErase";
		String query = "select * from brand where brandname='" + brand + "';";
		List<Map<String, Object>> branddata = DBLibrary.dbAction("fetch", query);	
		
		for(Map<String,Object> entry : branddata) {
			String query1 = "select * from cta_locators where brand='" + brand + "' and campaign='" + entry.get("campaign").toString() + "';";
			List<Map<String, Object>> locators = DBLibrary.dbAction("fetch", query1);
			System.out.println(entry.get("campaign").toString());
//			if(!(entry.get("campaign").toString().equalsIgnoreCase("deluxe20offspring"))) {
//				continue;
//			}
			if(entry.get("campaign").toString().equalsIgnoreCase("advancedHolidayOffer")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("holidayOffer")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("specialCore")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("Core")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("order30fsh2")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("deluxe20off")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("deluxe20offpanelb")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("specialOffer")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("advancedSpecialOffer")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("order30fshadvanced")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("order30fsh2b")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("corehrt")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("advancedDeluxe20off")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("specialOfferPanelb")) {
				continue;
			}
			if(entry.get("campaign").toString().equalsIgnoreCase("pnlbb")) {
				continue;
			}
			
			
			
			
			String qaurl = entry.get("qaurl").toString();
			String produrl = entry.get("produrl").toString();
			String stgurl = entry.get("stgurl").toString();
			
			WebDriver driver = new ChromeDriver();
			JavascriptExecutor jse = (JavascriptExecutor) driver;
		    driver.manage().window().maximize();
		    driver.get(qaurl);
		    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		    String qavalue = locators.get(0).get("qavalue").toString();
		    if(!(qavalue.equalsIgnoreCase("n/a"))) {
		    	driver.findElement(By.xpath(qavalue)).click();
		    }
		    
//		    jse.executeScript("arguments[0].click();", driver.findElement(By.xpath(qavalue)));
		    
		    WebDriver driver1 = new ChromeDriver();
		    JavascriptExecutor jse1 = (JavascriptExecutor) driver1;
		    driver1.manage().window().maximize();
		    driver1.get(produrl); 
		    driver1.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		    String prodvalue = locators.get(0).get("prodvalue").toString();
		    if(!(qavalue.equalsIgnoreCase("n/a"))) {
		    	driver1.findElement(By.xpath(prodvalue)).click();
		    }
		    
//		    jse1.executeScript("arguments[0].click();", driver1.findElement(By.xpath(prodvalue)));
		    
		    WebDriver driver2 = new ChromeDriver();
		    JavascriptExecutor jse2 = (JavascriptExecutor) driver2;
		    driver2.manage().window().maximize();
		    driver2.get(stgurl);
		    driver2.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		    String stgvalue = locators.get(0).get("stgvalue").toString();
		    if(!(qavalue.equalsIgnoreCase("n/a"))) {
		    	driver2.findElement(By.xpath(stgvalue)).click();
		    }
		    
//		    jse2.executeScript("arguments[0].click();", driver2.findElement(By.xpath(stgvalue)));
		}
	}

}
