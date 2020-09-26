package com.sns.gr.console;

import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.ConsoleUtilities;
import com.sns.gr.testbase.DBLibrary;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

public class ConsoleError {
	public WebDriver driver;
	
	CommonUtilities comm_obj = new CommonUtilities();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	DBUtilities db_obj = new DBUtilities();
	MailUtilities mailObj = new MailUtilities();
	ConsoleUtilities co_obj = new ConsoleUtilities();
	Scanner in = new Scanner(System.in);
	StringBuilder strfull = new StringBuilder();
	
	List<List<String>> output = new ArrayList<List<String>>();
	String sendReportTo = "yzewdie@guthy-renker.com";
	@BeforeSuite
	public void getEmailId() {
		//System.out.println("Enter Email id : ");
		//sendReportTo = in.next();
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
	    options.addArguments("--ignore-certificate-errors");

	    // configure it as a desired capability
	    DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
	    capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	    capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);		       
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        //driver = new ChromeDriver(capabilities);
	}
	@DataProvider(name="ConsoleErrorInput", parallel=false)
	public Object[][] testData() {
		Object[][] arrayObject = {{"CrepeErase"},{"Mally"},{"SpecificBeauty"},{"Sub-D"},{"Dr.Denese"},{"WestmoreBeauty"},{"MeaningfulBeauty"},{"SeaCalmSkin"},{"Volaire"},{"SmileActives"},{"ReclaimBotanical"},{"Sheercover"},{"PrincipalSecret"},{"TryDermaFlash"},{"SpotFade"},{"Theraworx"}};
		//Object[][] arrayObject = {{"TryDermaFlash"},{"SpecificBeauty"},{"sub-d"},{"ReclaimBotanical"},{"PrincipalSecret"},{"SheerCover"}};
		//Object[][] arrayObject = {{"SeaCalmSkin"},{"MeaningfulBeauty"},{"SheerCover"}};
		return arrayObject;
	}
	
	@Test(dataProvider="ConsoleErrorInput")
	public void console(String brand) throws ClassNotFoundException, SQLException, MalformedURLException, InterruptedException {
			String url = "";
			url = db_obj.getUrl(brand, "Core", "PROD");
			System.out.println(url);					
			BaseTest base_obj = new BaseTest();			
			WebDriver driver = base_obj.setUp("Chrome", "Local");
			driver.get(url);
			System.out.println(brand+" is loading");
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			System.out.println("Homepage Console Error Details:");
			Thread.sleep(3000);
			StringBuilder str = new StringBuilder("");
			str = co_obj.analyzeLog(driver,brand,"Homepage",str);
			String campaign = co_obj.getcampaigndetails(driver, brand);
			String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
			if(!(origcampaign.equals("n/a"))){
				campaign = origcampaign;
			}
			System.out.println(campaign+" campaign is loading for the brand "+brand);
			bf_obj.click_cta(driver, "Prod", brand, campaign, "Ordernow");
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			System.out.println("SASpage Console Error Details:");
			Thread.sleep(3000);
			str = co_obj.analyzeLog(driver,brand,"SASpage",str);
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			co_obj.selectoffercodekit(driver,brand,campaign);
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			System.out.println("Checkoutpage Console Error Details:");		
			str = co_obj.analyzeLog(driver,brand,"Checkoutpage",str);
			if(str.length()>0) {
				strfull.append("\n");
				strfull.append(brand+"\n");
				strfull.append(str);	
				System.out.println("Stringbuilder"+strfull);
				//co_obj.sendEmail(brand, "banuchitra@searchnscore.com",str,brand);
			}
			driver.close();
		}	
	@Test
	public void sendmail() {
		co_obj.sendEmail("Console Errors for the brands in PROD", sendReportTo, strfull);
	}
}
