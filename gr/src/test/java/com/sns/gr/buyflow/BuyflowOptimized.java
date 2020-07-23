package com.sns.gr.buyflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBLibrary;
import com.sns.gr.testbase.SASUtilities;

public class BuyflowOptimized {
	
	CommonUtilities comm_obj = new CommonUtilities();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	BaseTest base_obj = new BaseTest();
	
	@DataProvider(name="buyflowInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = null;
		arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/new_run_input.xlsx", "rundata");
		return arrayObject;
	}
	
	@Test(dataProvider="buyflowInput")
	public void buyflow(String env, String brand, String campaign, String category, String kitppid, String giftppid, String url, String shipbill, String cc, String browser) throws IOException, ClassNotFoundException, SQLException, InterruptedException {	
		
		String[][] merchData = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/BuyflowValidation/Merchandising Input/" + brand + ".xlsx", campaign);
		int PPIDcolumn = getPPIDColumn(merchData, kitppid);
		
		// Get PPID Column from Merchandising Input file
		if(PPIDcolumn == 0) {
			System.out.println(kitppid + " doesn't exist in " + brand + " - " + campaign);
		}
		else {
			System.out.println("PPID in column " + PPIDcolumn);		
			HashMap<String, String> offerdata = getColumnData(merchData, PPIDcolumn);
			
			Iterator itr = offerdata.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry mapElement = (Map.Entry)itr.next(); 
	            System.out.println(mapElement.getKey() + " : " + mapElement.getValue()); 
	        } 
			
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
				String postpu = checkPostPU(kitppid, PPIDcolumn, merchData);
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
				
				
				
				// Collect current Offer related details
				
				// Kit name
				String kitname = offerdata.get("Actual Kit Name (as in site)");
				System.out.println("Kit Name : " + kitname);
				
				//Gift name
				String giftname = getGiftname(brand, campaign, giftppid);
				System.out.println("Gift Name : " + giftname);
				
				// Check Supplysize of PPID
				int supplysize = checkSupplySize(kitppid, PPIDcolumn, merchData);
				System.out.println(kitppid + " SupplySize - " + supplysize);
				
				// Check PrePU for current offercode
				String offerprepu = offerdata.get("Value pack - Pre Purchase Upsell");
				if(!(offerprepu.equalsIgnoreCase("No"))) {
					offerprepu="Yes";
				}
				
				// Check PostPU for current offercode
				String offerpostpu = offerdata.get("Post Purchase Upsell to PPID");
				if(offerpostpu.contains(kitppid)) {
					offerpostpu="Yes";
				}
				else {
					offerpostpu="No";
				}
				
				// Check Gifts
				String gifts = "";				
				if(offerpostpu.equalsIgnoreCase("Yes")) {
					gifts = offerdata.get("Post Purchase Upsell Gift choices");
				}
				else {
					gifts = offerdata.get("Entry Gift");
				}
				
				//***********************************************************************
				// Launch Browser
				BaseTest base_obj = new BaseTest();			
				WebDriver driver = base_obj.setUp(browser, "Local");
				driver.get(url);
				System.out.println("Loaded " + brand + " site...");
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);	
				
				// Move to SAS
				bf_obj.click_cta(driver, env, brand, campaign, "Ordernow");
				
				// SAS Page Validations
				// Price Validation		
				String expectedSASPrice = offerdata.get("Entry Pricing");												
				String actualSASPrice = fetchSASPrice(driver, brand, campaign, kitname);
							
				System.out.println("Expected entry price: " + expectedSASPrice);
				System.out.println("Actual entry price: " + actualSASPrice);
				
				if(expectedSASPrice.equals(actualSASPrice)) {
					System.out.println("SAS Entry Price Validation - PASS");
				}
				else {
					System.out.println("SAS Entry Price Validation - FAIL");
				}
				
				// Gift Validation
				checkGifts(driver, brand, campaign, gifts);
				
				// Select offer				
				select_offer(driver, brand, campaign, pagepattern, offerdata, giftppid, offerprepu);
				
				// Move to Checkout
				
				// Fill out form
				String email = bf_obj.fill_out_form(driver, brand, campaign, cc, shipbill, "30");
				System.out.println("Email : " + email);	
				
				// Checkout Page Validation
				// Validate Added Kit
				String kitresult = checkAddedLineItem(driver, brand, campaign, "Kit", kitname);
				if(kitresult.equalsIgnoreCase("PASS")) {
					System.out.println(kitname + "  added to cart.");
				}
				else {
					System.out.println("Wrong kit - " + kitname + "  added to cart.");
				}
				
				// Validate Added Gift
				String giftresult = checkAddedLineItem(driver, brand, campaign, "Gift", giftppid);
				if(giftresult.equalsIgnoreCase("PASS")) {
					System.out.println(giftname + "  added to cart.");
				}
				else {
					System.out.println("Wrong gift - " + giftname + "  added to cart.");
				}
				
				// Validate Checkout pricing
				
			}					
			
		}
	}
	
	public String checkAddedLineItem(WebDriver driver, String brand, String campaign, String offer, String name) throws ClassNotFoundException, SQLException, InterruptedException {
		String result = "FAIL";
		String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='CheckoutLineItem' and offer='" + offer + "'";
		List<Map<String, Object>> lineitemloc = DBLibrary.dbAction("fetch", query);
		
		WebElement lineitem_elmt = comm_obj.find_webelement(driver, lineitemloc.get(0).get("ELEMENTLOCATOR").toString(), lineitemloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);
		
		String lineitemname = lineitem_elmt.getText();
		if(lineitemname.contains(name)) {
			result = "PASS";
		}
		return result;
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
	
	public void checkGifts(WebDriver driver, String brand, String campaign, String gifts) throws ClassNotFoundException, SQLException {
				
		String[] gift_arr = gifts.split(",");		
		for(String gift : gift_arr) {
			String[] giftvalues = gift.split("-");
			String giftname = giftvalues[0].trim();
			String giftppid = giftvalues[1].trim();
			
			String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='Gift' and offer like '%" + giftname + "%'";
			List<Map<String, Object>> giftloc = DBLibrary.dbAction("fetch", query);
			
			if(driver.findElement(By.xpath(giftloc.get(0).get("ELEMENTVALUE").toString())).isDisplayed()) {
				System.out.println(giftname + " is present on SAS");
			}
			else {
				System.out.println(giftname + " is not present on SAS");
			}
		}		
	}
	
	public String getGiftname(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='Gift' and offer like '%" + ppid + "%'";
		List<Map<String, Object>> giftloc = DBLibrary.dbAction("fetch", query);
		
		String[] giftarr = giftloc.get(0).get("OFFER").toString().split("-");
		String giftname = giftarr[0];
		return giftname;		
	}
	
	public String fetchSASPrice(WebDriver driver, String brand, String campaign, String kitname) throws ClassNotFoundException, SQLException, InterruptedException {
		String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='SASKitPrice' and offer='" + kitname + "'";
		List<Map<String, Object>> priceloc = DBLibrary.dbAction("fetch", query);
		
		WebElement priceelmt = comm_obj.find_webelement(driver, priceloc.get(0).get("ELEMENTLOCATOR").toString(), priceloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		String sasprice = priceelmt.getText();
		return sasprice;
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
//			System.out.println("currentrowname : " + currentrowname);
			for(String name : expectedrowNames) {
				if(currentrowname.contains(name)) {
					for(int j=1; j<merchData[i].length; j++) {
						String rowPPID = merchData[i][j];
//						System.out.println("rowPPID : " + rowPPID);
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
