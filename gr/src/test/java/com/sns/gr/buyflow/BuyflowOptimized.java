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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterSuite;
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

public class BuyflowOptimized {
	
	CommonUtilities comm_obj = new CommonUtilities();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CartLangUtilities lang_obj = new CartLangUtilities();
	MailUtilities mailObj = new MailUtilities();
	BaseTest base_obj = new BaseTest();
	
	List<List<String>> output = new ArrayList<List<String>>();
	String sendReportTo = "";
	
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
			System.out.println();
			System.out.println();
			
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
				
				System.out.println();
				System.out.println();
				
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
//				System.out.println("Loaded " + brand + " site...");
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);	
				
				// Move to SAS
				bf_obj.click_cta(driver, env, brand, campaign, "Ordernow");
				
				// SAS Page Validations
				// Price Validation							
//				String actualSASPrice = fetchSASPrice(driver, brand, campaign, kitname);
//							
//				System.out.println(kitppid + "Expected entry price: " + expectedEntryPrice);
//				System.out.println(kitppid + "Actual entry price: " + actualSASPrice);
//				
//				if(expectedEntryPrice.equals(actualSASPrice)) {
//					EntryPriceResult = "PASS";
//				}
//				else {
//					EntryPriceResult = "FAIL";
//					remarks = remarks + "Entry Kit Price on SAS Page is wrong,";
//				}
				
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
				String kitresult = checkAddedLineItem(driver, brand, campaign, "Kit", ppid30day);
				if(kitresult.equalsIgnoreCase("PASS")) {
					ppidResult = "PASS";
				}
				else {
					ppidResult = "FAIL";
					remarks = remarks + "Wrong Kit added to cart, Expected - " + ppid30day + " , Actual - " + ppidResult;
				}
				
				// Validate Added Gift
				String giftresult = checkAddedLineItem(driver, brand, campaign, "Gift", giftppid);
				if(giftresult.equalsIgnoreCase("PASS")) {
					ppidResult = "PASS";
				}
				else {
					ppidResult = "FAIL";
					remarks = remarks + "Wrong Gift added to cart, Expected - " + giftppid + " , Actual - " + ppidResult;
				}
				
				// Validate entry kit price
				String checkoutentrykitprice = getCheckoutEntryKitPrice(driver, brand, campaign, "Checkout EntryKit Price");
				if(expectedEntryPrice.equalsIgnoreCase(checkoutentrykitprice)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Entry Kit Price on Checkout Page is wrong, Expected - " + expectedEntryPrice + " , Actual - " + checkoutentrykitprice;
				}				
				
				// Validate Continuity pricing
				String cart_lang = lang_obj.get_cart_language(driver, brand);						
				String[] lang_price_arr = lang_obj.parse_cart_language(cart_lang);		
				String cart_lang_price = "$" + lang_price_arr[1];
				String cart_lang_shipping = "$" + lang_price_arr[2];		
				String cartlang_pricing = cart_lang_price + "," + cart_lang_shipping;
								
				if(continuitypricing.equalsIgnoreCase(cart_lang_price)) {
					ContinuityPriceResult = "PASS";
				}
				else {
					ContinuityPriceResult = "FAIL";
					remarks = remarks + "Continuity Price is wrong, Expected - " + continuitypricing + " , Actual - " + cart_lang_price;
				}
				
				if(continuityshipping.equalsIgnoreCase(cart_lang_shipping)) {
					ContinuityPriceResult = "PASS";
				}
				else {
					ContinuityPriceResult = "FAIL";
					remarks = remarks + "Continuity Shipping is wrong, Expected - " + continuityshipping + " , Actual - " + cart_lang_shipping;
				}				
				
				// Validate Checkout pricing
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
							
				if(expectedEntryPrice.equalsIgnoreCase(checkout_subtotal)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Checkout Subtotal does not match with the expected price, Expected - " + expectedEntryPrice + " , Actual - " + checkout_subtotal;
				}
				
				if(expectedEntryShipping.equalsIgnoreCase(checkout_shipping)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Checkout Shipping does not match with the expected shipping price, Expected - " + expectedEntryShipping + " , Actual - " + checkout_shipping;
				}
				
				bf_obj.complete_order(driver, brand, cc);
				Thread.sleep(3000);
				
//				// Upsell page validations
//				driver.findElement(By.xpath("//i[@class='fa fa-plus']")).click();				
				
				upsell_confirmation(driver, brand, campaign, offerpostpu);
				
				// Confirmation page validations
				String conf_offercode = bf_obj.fetch_confoffercode(driver, brand);
				System.out.println("Confirmation PPIDs : " + conf_offercode);
				if(conf_offercode.contains(kitppid)){
					ppidResult = "PASS";
				}
				else {
					ppidResult = "FAIL";
					remarks = remarks + "Wrong Kit added, Expected - " + kitppid + " , Actual - " + conf_offercode;
				}
				if(conf_offercode.contains(giftppid)){
					ppidResult = "PASS";
				}
				else {
					ppidResult = "FAIL";
					remarks = remarks + "Wrong Gift added, Expected - " + giftppid + " , Actual - " + conf_offercode;
				}
				
				String conf_num = bf_obj.fetch_conf_num(driver, brand);
				System.out.println("Confirmation Number : " + conf_num);				
								
				// Confirmation Price Validation
				String conf_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Subtotal");
				String conf_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Shipping");
				String conf_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Salestax");
				String conf_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Confirmation Total");
				
				String conf_pricing = conf_subtotal + " ; " + conf_shipping + " ; " + conf_salestax + " ; " + conf_total;	
				
				String expectedFinalprice = "";
				String expectedFinalshipping = "";
				
				if(offerpostpu.equalsIgnoreCase("Yes")) {
					expectedFinalprice = offerdata.get("Post Purchase Upsell Pricing");	
					expectedFinalshipping = offerdata.get("Post Purchase Upsell Shipping");	
				}
				else {
					expectedFinalprice = expectedEntryPrice;	
					expectedFinalshipping = expectedEntryShipping;	
				}
				
				// Free Shipping price
				if(expectedFinalshipping.toLowerCase().contains("free")) {
					expectedFinalshipping = "$0.00";
				}
				
				if(expectedFinalprice.equalsIgnoreCase(conf_subtotal)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Confirmation Subtotal is wrong, Expected - " + expectedFinalprice + " , Actual - " + conf_subtotal + ",";
				}
				
				if(expectedFinalshipping.equalsIgnoreCase(conf_shipping)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Shipping price on confirmation page is wrong, Expected - " + expectedFinalshipping + " , Actual - " + conf_shipping + ",";
				}
				
				if(checkout_salestax.equalsIgnoreCase(conf_salestax)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "SalesTax on Confirmation page does not match with that of checkout page, Expected - " + checkout_salestax + " , Actual - " + conf_salestax + ",";
				}
				
				if(checkout_total.equalsIgnoreCase(conf_total)) {
					EntryPriceResult = "PASS";
				}
				else {
					EntryPriceResult = "FAIL";
					remarks = remarks + "Total Price on Confirmation page is wrong, Expected - " + checkout_total + " , Actual - " + conf_total + ",";
				}
				
				String actualrenewalplanid = getFromVariableMap(driver, "renewalPlanId");
				if(!(actualrenewalplanid.contains(expectedrenewalplanid))) {
					remarks = remarks + "Renewal Plan Id does not match, Expected - " + expectedrenewalplanid + " , Actual - " + actualrenewalplanid + ",";
				}
				System.out.println("Expected Renewal Plan Id : " + expectedrenewalplanid);	
				System.out.println("Actual Renewal Plan Id : " + actualrenewalplanid);	
				
				if(offerpostpu.equalsIgnoreCase("Yes")) {
					String actualinstallmentplanid = getFromVariableMap(driver, "paymentPlanId");
					if(!(actualinstallmentplanid.contains(expectedinstallmentplanid))) {
						remarks = remarks + "Installment Plan Id does not match, Expected - " + expectedinstallmentplanid + " , Actual - " + actualinstallmentplanid + ",";
					}
					System.out.println("Expected Installment Plan Id : " + expectedinstallmentplanid);	
					System.out.println("Actual Installment Plan Id : " + actualinstallmentplanid);
				}				
				
				List<String> output_row = new ArrayList<String>();
				output_row.add(env);
				output_row.add(brand);
				output_row.add(campaign);
				output_row.add(category);
				output_row.add(email);
				output_row.add(conf_offercode + " - " + ppidResult);
				output_row.add(conf_num);
				output_row.add(conf_pricing + " - " + EntryPriceResult);
				output_row.add(cartlang_pricing + " - " + ContinuityPriceResult);
				output_row.add(shipbill);	
				output_row.add(cc);	
				output_row.add(browser);	
				output_row.add(remarks);
				output.add(output_row);
				
				driver.close();
			}				
		}		
	}
	
	@AfterSuite
	public void populateExcel() throws IOException {
		String file = comm_obj.populateOutputExcel(output, "BuyflowResults", System.getProperty("user.dir") + "\\Input_Output\\BuyflowValidation\\Run Output\\");
		
		List<String> attachmentList = new ArrayList<String>();
		attachmentList.add(file);
		mailObj.sendEmail("Buyflow Results", sendReportTo, attachmentList);
	}
	
	public String getFromVariableMap(WebDriver driver, String variablename) {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		List<String> value = (List<String>) jse.executeScript("return app.variableMap." + variablename);
				
		String datavalue = "";
		for(String data : value) {
			datavalue = datavalue + data + ",";
		}
		return datavalue;
	}
	
	public void upsell_confirmation(WebDriver driver, String brand, String campaign, String postpu) throws InterruptedException, ClassNotFoundException, SQLException {
		String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='PostPU' and offer = '" + postpu + "'";
		List<Map<String, Object>> upsellloc = DBLibrary.dbAction("fetch", query);
		
		WebElement upsell_elmt = comm_obj.find_webelement(driver, upsellloc.get(0).get("ELEMENTLOCATOR").toString(), upsellloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);
		
		upsell_elmt.click();
		Thread.sleep(3000);
	}
	
	public String getCheckoutEntryKitPrice(WebDriver driver, String brand, String campaign, String step) throws ClassNotFoundException, SQLException, InterruptedException {
				
		String query = "select * from locators where brand='" + brand + "' and campaign='" + campaign + "' and step='" + step + "'";
		List<Map<String, Object>> priceloc = DBLibrary.dbAction("fetch", query);
		
		WebElement price_elmt = comm_obj.find_webelement(driver, priceloc.get(0).get("ELEMENTLOCATOR").toString(), priceloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);
		
		String price = price_elmt.getText();		
		return price;
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
		else {
			result = lineitemname;
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
