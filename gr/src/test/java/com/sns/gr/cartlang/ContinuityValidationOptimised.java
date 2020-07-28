package com.sns.gr.cartlang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;
import com.sns.gr.testbase.CartLangUtilities;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBLibrary;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PricingUtilities;
import com.sns.gr.testbase.SASUtilities;

public class ContinuityValidationOptimised {
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
		String[][] merchData = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/Merchandising Input/" + brand + ".xlsx", campaign);
				
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
				
				int PPIDcolumn = getPPIDColumn(merchData, offer.get("PPID").toString());
				// Get PPID Column from Merchandising Input file
				if(PPIDcolumn == 0) {
					System.out.println(offer.get("PPID").toString() + " doesn't exist in " + brand + " - " + campaign);
				}
				else {
					System.out.println("PPID in column " + PPIDcolumn);		
					HashMap<String, String> offerdata = getColumnData(merchData, PPIDcolumn);
					
					// Collect current campaign related data
					// Pagepattern, Pre-purchase upsell - Yes or No, Post-purchase upsell - Yes or No
					String pagepattern = getPagePattern(brand,campaign);
					if(pagepattern.equalsIgnoreCase("")) {
						System.out.println(brand + "-" + campaign + "has no pattern");
					}
					else {
						// Check Pre-purchase Upsell
						String prepu = "";
						if(pagepattern.contains("prepu")) {
							prepu = "Yes";
							System.out.println("Pre Purchase Upsell exists for " + brand + " - " + campaign);
						}
						else {
							prepu = "No";
							System.out.println("No Pre Purchase Upsell for " + brand + " - " + campaign);
						}
						
						// Check Post-purchase Upsell
						String postpu = checkPostPU(offer.get("PPID").toString(), PPIDcolumn, merchData);
						if(postpu.equalsIgnoreCase("Yes")) {
							System.out.println("Post Purchase Upsell exists for " + brand + " - " + campaign);
						}
						else {
							System.out.println("No Pre Purchase Upsell for " + brand + " - " + campaign);
						}
						
						// List pages in this campaign
						List<String> campaignpages = new ArrayList<String>();
						campaignpages.add("HomePage");
						campaignpages.add("SASPage");
						if(prepu.equalsIgnoreCase("Yes")) {
							campaignpages.add("PrePurchaseUpsell");
						}
						campaignpages.add("CheckoutPage");
						if(postpu.equalsIgnoreCase("Yes")) {
							campaignpages.add("PostPurchaseUpsell");
						}
						campaignpages.add("ConfirmationPage");
						System.out.println(campaignpages);				
						
						System.out.println();
						System.out.println();
						
						// Collect current Offer related details				
						// Kit name
						String kitname = offerdata.get("Actual Kit Name (as in site)");
						System.out.println("Kit Name : " + kitname);
						
						//Gift name
						String giftname = getGiftname(brand, campaign, "MT2A3546");
						System.out.println("Gift Name : " + giftname);
						
						// Check Supplysize of PPID
						int supplysize = checkSupplySize(offer.get("PPID").toString(), PPIDcolumn, merchData);
						System.out.println(offer.get("PPID").toString() + " SupplySize - " + supplysize);
						
						// Check PrePU for current offercode
						String offerprepu = offerdata.get("Value pack - Pre Purchase Upsell");
						if(!(offerprepu.equalsIgnoreCase("No"))) {
							offerprepu="Yes";
						}
						
						// Check PostPU for current offercode
						String offerpostpu = offerdata.get("Post Purchase Upsell to PPID");
						if(offerpostpu.contains(offer.get("PPID").toString())) {
							offerpostpu="Yes";
						}
						else {
							offerpostpu="No";
						}
						
						// Check Gifts
						String gifts = "";				
						if(offerpostpu.equalsIgnoreCase("Yes")) {
							gifts = offerdata.get("Post Purchase Upsell Gift");
						}
						else {
							gifts = offerdata.get("Entry Gift");
						}
						
						// Data from Merchandising Input file
						String ppid30day = offerdata.get("Entry PPID");	
						String expectedEntryPrice = offerdata.get("Entry Pricing");		
						String expectedEntryShipping = offerdata.get("Entry Shipping");
						String continuitypricing = offerdata.get("Continuity Pricing");	
						String continuityshipping = offerdata.get("Continuity Shipping");
						String expectedrenewalplanid = "";
						String expectedinstallmentplanid = "";
						if(supplysize == 30) {
							expectedrenewalplanid = offerdata.get("Entry Renewal Plan");
						}
						else {
							expectedrenewalplanid = offerdata.get("Post Purchase Upsell Renewal Plan");
							expectedinstallmentplanid = offerdata.get("Post Purchase Upsell Installment Plan");
						}
						
						// Intialize result variables
						String remarks = "";
						String ppidResult = "";
						String EntryPriceResult = "";
						String ContinuityPriceResult = "";
						
						//***********************************************************************
						// Launch Browser
						BaseTest base_obj = new BaseTest();			
						WebDriver driver = base_obj.setUp(browser, "Local");
						driver.get(url);
//						System.out.println("Loaded " + brand + " site...");
						driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);	
						
						// Move to SAS
						bf_obj.click_cta(driver, env, brand, campaign, "Ordernow");
						
						// Select offer				
						select_offer(driver, brand, campaign, pagepattern, offerdata, "MT2A3546", offerprepu);
						
						List<String> campaignPages = db_obj.getPages(brand, campaign);
						boolean upsell = campaignPages.contains("upsellpage");
						System.out.println("Upsell : " + upsell);
						
						
					
					}
				}
			}
		}
	}
	
	public String checkPostPU(String ppid, int column, String[][] merchData) {
		String PostPU = "";
		int postpurownum = getRowByName(merchData, "Post Purchase Upsell to PPID");
		String postpudata = merchData[postpurownum][column];
		if(postpudata.matches("^[A-Z0-9]*$")) {
			PostPU = "Yes";
		}
		else {
			PostPU = "No";
		}
		return PostPU;
	}
	
	public int checkSupplySize(String ppid, int column, String[][] merchData) {
		int supplysize = 0;
		for(int i=0; i<merchData.length; i++) {	
			String currentppid = merchData[i][column];
			if(currentppid.contains(ppid)) {
				String rowname = merchData[i][0];
				if(rowname.equalsIgnoreCase("Entry PPID")) {
					supplysize = 30;
				}
				else if(rowname.equalsIgnoreCase("Post Purchase Upsell to PPID")) {
					supplysize = 90;
				}
				break;
			}
		}
		return supplysize;
	}
		
		public String getGiftname(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
			String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='Gift' and offer like '%" + ppid + "%'";
			List<Map<String, Object>> giftloc = DBLibrary.dbAction("fetch", query);
			
			String[] giftarr = giftloc.get(0).get("OFFER").toString().split("-");
			String giftname = giftarr[0];
			return giftname;		
		}
		
		public String getPagePattern(String brand, String campaign) throws IOException {
			String pagepattern = "";
			File input_file = new File(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/Merchandising Input/" + brand + ".xlsx");
			
			FileInputStream inputstream = new FileInputStream(input_file);
			Workbook testData = new XSSFWorkbook(inputstream);
			Sheet dataSheet = testData.getSheet("PagePattern");
			
			int end = 0;		
			
			//////////////////////////////////////
			int k = 1;
			String rowdata = dataSheet.getRow(k).getCell(0).getStringCellValue();
			while(!(rowdata.equalsIgnoreCase("End"))) {
				if(rowdata.equalsIgnoreCase(campaign)) {
					pagepattern = dataSheet.getRow(k).getCell(1).getStringCellValue();
					break;
				}
				k++;
				rowdata =  dataSheet.getRow(k).getCell(0).getStringCellValue();
			}
			return pagepattern;
		}
		
		public void select_offer(WebDriver driver, String brand, String campaign, String pagepattern, HashMap<String, String> offerdata, String giftppid, String prepu) throws ClassNotFoundException, SQLException, InterruptedException {
					
			String[] patternarr = pagepattern.split("-");
			for(String pattern : patternarr) {
				switch(pattern){  
		    	case "kit":
		    		select_kit(driver, offerdata, brand, campaign);
		    		break;  
		    	case "gift":
		    		select_gift(driver, brand, campaign, giftppid);
		    		break;  	    	
		    	case "prepu":
		    		select_prepu(driver, brand, campaign, prepu);
		    		break;
				}
			}		
		}
		
		public void select_kit(WebDriver driver, HashMap<String, String> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
			String kitname = offerdata.get("Actual Kit Name (as in site)");
			
			String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='Kit' and offer='" + kitname + "'";
			List<Map<String, Object>> kitloc = DBLibrary.dbAction("fetch", query);
			
			WebElement kit_elmt = comm_obj.find_webelement(driver, kitloc.get(0).get("ELEMENTLOCATOR").toString(), kitloc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(2000);
			kit_elmt.click();
			Thread.sleep(1000);
		}
		
		public void select_gift(WebDriver driver, String brand, String campaign, String giftppid) throws ClassNotFoundException, SQLException, InterruptedException {
			String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='Gift' and offer like '%" + giftppid + "%'";
			List<Map<String, Object>> giftloc = DBLibrary.dbAction("fetch", query);
			
			WebElement gift_elmt = comm_obj.find_webelement(driver, giftloc.get(0).get("ELEMENTLOCATOR").toString(), giftloc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(2000);
			gift_elmt.click();
			Thread.sleep(1000);
			
			driver.findElement(By.xpath("//button[@class='button checkout-special-offer']")).click();
			Thread.sleep(1000);
		}

		public void select_prepu(WebDriver driver, String brand, String campaign, String prepu) throws ClassNotFoundException, SQLException, InterruptedException {
			String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='PrePU' and offer='" + prepu + "'";
			List<Map<String, Object>> prepuloc = DBLibrary.dbAction("fetch", query);
			
			WebElement prepu_elmt = comm_obj.find_webelement(driver, prepuloc.get(0).get("ELEMENTLOCATOR").toString(), prepuloc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(2000);
			prepu_elmt.click();
			Thread.sleep(1000);
		}
		
		public int getRowByName(String[][] merchData, String name) {
			int rownum = 0;
			for(int i=0; i<merchData.length; i++) {	
				String currentrow = merchData[i][0];
				if(currentrow.equalsIgnoreCase(name)) {
					rownum = i;
					break;
				}
			}
			return rownum;
		}
	
	public int getPPIDColumn(String[][] merchData, String ppid) {
		int ppidcolumn = 0;
		int temp = 0;
		String[] expectedrowNames = {"Entry PPID", "Post Purchase Upsell to PPID"};
		
		for(int i=0; i<merchData.length; i++) {			
			String currentrowname = merchData[i][0];
			System.out.println("currentrowname : " + currentrowname);
			for(String name : expectedrowNames) {
				if(currentrowname.contains(name)) {
					for(int j=1; j<merchData[i].length; j++) {
						String rowPPID = merchData[i][j];
						System.out.println("rowPPID : " + rowPPID);
			            if(rowPPID.contains(ppid)) {
			            	ppidcolumn = j;
			            	temp = 1;
			            	break;
			            }
			        }
					if(temp == 1) {
						break;
					}
				}
			}	
			if(temp == 1) {
				break;
			}
	    }
		return ppidcolumn;
	}
	
	public HashMap<String, String> getColumnData(String[][] merchData, int column) {
		LinkedHashMap<String, String> offerdata = new LinkedHashMap<String, String>();
		for(int i=0; i<merchData.length; i++) {	
			System.out.println(merchData[i][column]);
			if(!(merchData[i][column].equals("-"))) {
				if(merchData[i][0] == null) {
					merchData[i][0] = merchData[i-1][0];
				}
				offerdata.put(merchData[i][0], merchData[i][column]);
			}
		}
		return offerdata;
	}

}
