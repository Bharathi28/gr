package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SASUtilities {
	
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();	
		
	public List<String> fetch_random_singles(String brand, String campaign, int count) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";		
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='Product' and status<>'Inactive' order by RAND() limit " + count;
		List<Map<String, Object>> singles = DBLibrary.dbAction("fetch",query);
		
		List<String> single_offers = new ArrayList<String>();
		for(Map<String, Object> product : singles) {
			single_offers.add(product.get("ppid").toString());
		}
		System.out.println("Chosen single : " + single_offers);
		return single_offers;
	}
	
	public String get_offer(WebDriver driver, String env, String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException, InterruptedException {		
		String ppid_str = "";
		Map<String, Object> offerdata;
		
		if(ppid.contains("single")) {
			String[] single_array = ppid.split(" ");
			String no_of_singles_str = single_array[0];
			int no_of_singles = Integer.parseInt(no_of_singles_str);
			
			List<String> single_offers = fetch_random_singles(brand, campaign, no_of_singles);
			
			for(String single_offer : single_offers) {
				offerdata = DBUtilities.get_offerdata(single_offer, brand, campaign, "Product");
				select_offer(driver,env,brand,campaign,offerdata);
				ppid_str = single_offer + ",";
			}			
		}
		else {
			ppid_str = ppid;
			if(bf_obj.checkIfProduct(brand, campaign, ppid)) {
				offerdata = DBUtilities.get_offerdata(ppid, brand, campaign, "Product");
			}
			else {
				offerdata = DBUtilities.get_offerdata(ppid, brand, campaign, "Kit");
			}			
			select_offer(driver,env,brand,campaign,offerdata);
		}
		String last_char = ppid_str.substring(ppid_str.length() - 1);
		if(last_char.equalsIgnoreCase(",")) {
			ppid_str = ppid_str.substring(0, ppid_str.length() - 1);
		}
		return ppid_str;
	}	
	
	public void select_offer(WebDriver driver, String env, String brand, String campaign, Map<String, Object> offerdata) throws ClassNotFoundException, SQLException, InterruptedException {
				
		String pagepattern = offerdata.get("pagepattern").toString().toLowerCase();
		String[] patternarr = pagepattern.split("-");
		for(String pattern : patternarr) {
			switch(pattern){  
	    	case "kit":
	    		select_kit(driver, offerdata, brand, campaign);
	    		break;  
	    	case "gift":
	    		select_gift(driver, offerdata, brand, campaign);
	    		break;  
	    	case "supply":
	    		select_supply(driver, offerdata, brand, campaign);
	    		break;
	    	case "shade":
	    		select_shade(driver, offerdata, brand, campaign);
	    		break;
	    	case "kitshade":
				select_kitshade(driver, offerdata, brand, campaign);
				break;
	    	case "kitshade2":
				select_kitshade2(driver, offerdata, brand, campaign);
				break;
			case "giftshade":
				select_giftshade(driver, offerdata, brand, campaign);
				break;
	    	case "duo":
	    		select_duo(driver, offerdata, brand, campaign);
	    		break;
	    	case "fragrance":
	    		select_fragrance(driver, offerdata, brand, campaign);
	    		break;
	    	case "productaddon":
	    		select_productaddon(driver, offerdata, brand, campaign);
	    		break;
	    	case "supplyvalue":
	    		select_supplyvalue(driver, offerdata, brand, campaign);
	    		break;
	    	case "frequency":
	    		select_frequency(driver, offerdata, brand, campaign);
	    		break;
	    	case "easypay":
	    		select_easypay(driver, offerdata, brand, campaign);
	    		break;
	    	case "product":
	    		select_product(driver, offerdata, brand, campaign);
	    		break;
	    	case "onetime":
				select_onetime(driver, offerdata, brand, campaign);
				break;
			}
		}		
		add_product_to_cart(driver, brand, campaign);
	}
	
	public void add_product_to_cart(WebDriver driver, String brand, String campaign) {
		if((brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("DermaFlash")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("Smileactives"))) {
			if(driver.findElements(By.id("add-to-cart")).size() != 0) {
				driver.findElement(By.id("add-to-cart")).click();
			}
		}
	}
	
	public void select_kit(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kit = offerdata.get("description").toString().toLowerCase();
		JavascriptExecutor jse = (JavascriptExecutor) driver;
//		WebDriverWait wait = new WebDriverWait(driver, 10);
		
		List<Map<String, Object>> kit_loc = comm_obj.get_element_locator(brand, campaign, "Kit", kit);
		Thread.sleep(2000);
		WebElement kit_elmt = comm_obj.find_webelement(driver, kit_loc.get(0).get("elementlocator").toString(), kit_loc.get(0).get("elementvalue").toString());
		Thread.sleep(2000);
//		wait.until(ExpectedConditions.elementToBeClickable(kit_elmt));
		
		if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
			Actions act = new Actions(driver);
			act.moveToElement(kit_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = kit_loc.get(0).get("elementvalue").toString() + "//div//div[@class='product-wrap']//div[3]//div//a";
			kit_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}
		if((brand.equalsIgnoreCase("FixMDSkin")) && (campaign.equalsIgnoreCase("Core"))){
			jse.executeScript("window.scrollBy(0,250)", 0);
			Thread.sleep(2000);
		}
		
		kit_elmt.click();		
		Thread.sleep(1000);
		if((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("Core"))) {
			jse.executeScript("window.scrollBy(0,700)", 0);
			driver.findElement(By.xpath("(//button[@class='button primary next-section'])[1]")).click();
		}
		if(((brand.equalsIgnoreCase("Volaire")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("Volaire")) && (campaign.equalsIgnoreCase("newcc"))) || ((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("fb")))){
			jse.executeScript("window.scrollBy(0,1000)", 0);
			Thread.sleep(2000);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}
		if((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("Core"))){
			jse.executeScript("window.scrollBy(0,1000)", 0);
			Thread.sleep(2000);
			driver.findElement(By.xpath("//button[@class='button checkout']")).click();
		}
		if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("specialCore"))) {
			jse.executeScript("window.scrollBy(0,800)", 0);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}
		
		if(!(brand.equalsIgnoreCase("SpecificBeauty"))) {
			if(driver.findElements(By.cssSelector("#kit ~ .market a.buttons-next")).size() != 0) {
				driver.findElement(By.cssSelector("#kit ~ .market a.buttons-next")).click();
			}	
		}
		Thread.sleep(1000);
	}
	
	public void select_gift(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String gift = offerdata.get("gift").toString().toLowerCase();
		
		if(!(gift.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> gift_loc = comm_obj.get_element_locator(brand, campaign, "Gift", gift);
			WebElement gift_elmt = comm_obj.find_webelement(driver, gift_loc.get(0).get("elementlocator").toString(), gift_loc.get(0).get("elementvalue").toString());
			Thread.sleep(2000);
			gift_elmt.click();
			Thread.sleep(1000);								
		}
		if((brand.equalsIgnoreCase("CrepeErase"))  && (campaign.equalsIgnoreCase("Core"))) {
			jse.executeScript("window.scrollBy(0,200)", 0);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}
		else if((brand.equalsIgnoreCase("MeaningfulBeauty")) && (campaign.equalsIgnoreCase("Core"))) {
			driver.findElement(By.xpath("//button[@class='button checkout-special-offer']")).click();
		}
		else if((brand.equalsIgnoreCase("MeaningfulBeauty")) && (campaign.equalsIgnoreCase("20offdeluxe"))) {
			driver.findElement(By.xpath("//button[@class='button checkout-special-offer']")).click();
		}		
		else if(driver.findElements(By.cssSelector("#gift ~ .market a.buttons-next")).size() != 0) {
			driver.findElement(By.cssSelector("#gift ~ .market a.buttons-next")).click();
		}
		Thread.sleep(1000);
	}

	public void select_supply(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kit = offerdata.get("description").toString().toLowerCase();
		String supply = offerdata.get("supplysize").toString().toLowerCase();
		
		List<Map<String, Object>> supply_loc = comm_obj.get_element_locator(brand, campaign, "Supply", supply + " " + kit);
		WebElement elmt = comm_obj.find_webelement(driver, supply_loc.get(0).get("elementlocator").toString(), supply_loc.get(0).get("elementvalue").toString());
		Thread.sleep(1000);
		elmt.click();
		Thread.sleep(1000);
		
		if(driver.findElements(By.cssSelector("#supply ~ .market a.buttons-next")).size() != 0) {
			driver.findElement(By.cssSelector("#supply ~ .market a.buttons-next")).click();
		}
	}
	
	public void select_shade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String name = offerdata.get("description").toString().toLowerCase();
		String shade = offerdata.get("shade").toString().toLowerCase();
		String category = offerdata.get("category").toString().toLowerCase();
		
		if(!(shade.equalsIgnoreCase("n/a"))) {
			
			List<Map<String, Object>> shade_loc = null;		
			if(category.equalsIgnoreCase("Kit")) {
				shade_loc = comm_obj.get_element_locator(brand, campaign, "Shade", shade);
			}
			else if(category.equalsIgnoreCase("Product")) {
				shade_loc = comm_obj.get_element_locator(brand, campaign, "Shade", shade + " " + name);
			} 
						
			WebElement shade_elmt = comm_obj.find_webelement(driver, shade_loc.get(0).get("elementlocator").toString(), shade_loc.get(0).get("elementvalue").toString());
			Thread.sleep(1000);
			shade_elmt.click();
			Thread.sleep(1000);
		}	
	}
	
	public void select_kitshade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String kitname = offerdata.get("description").toString();
		String kitshade = offerdata.get("kitshade").toString();
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "KitShade", kitshade + " " + kitname);
		
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
		Thread.sleep(2000);
		elmt.click();
		Thread.sleep(2000);
		if((brand.equalsIgnoreCase("Mally")) && ((campaign.equalsIgnoreCase("Core"))||(campaign.equalsIgnoreCase("save10")))) {
			if(kitname.contains("Fresh")) {
				if(driver.findElements(By.xpath("(//button[@class='button primary next-section'])[1]")).size() != 0) {
					driver.findElement(By.xpath("(//button[@class='button primary next-section'])[1]")).click();
				}
			}
			else if(kitname.contains("Matte")) {
				if(driver.findElements(By.xpath("(//button[@class='button primary next-section'])[2]")).size() != 0) {
					driver.findElement(By.xpath("(//button[@class='button primary next-section'])[2]")).click();
				}
			}
		}
		else if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("pnld")) && (kitname.equalsIgnoreCase("Flawless Body Kit"))){
			jse.executeScript("window.scrollBy(0,1000)", 0);
			Thread.sleep(2000);
			driver.findElement(By.xpath("//button[@class='button checkout-basic active']")).click();
		}
	}
	
	public void select_kitshade2(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String kitname = offerdata.get("description").toString();
		String kitshade2 = offerdata.get("kitshade2").toString();
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "KitShade2", kitshade2 + " " + kitname);
		
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
		Thread.sleep(2000);
		elmt.click();
		Thread.sleep(2000);
		
		if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("pnld"))){
			jse.executeScript("window.scrollBy(0,1000)", 0);
			Thread.sleep(2000);
			driver.findElement(By.xpath("//button[@class='button checkout active']")).click();
		}
	}
	
	public void select_giftshade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
				
		String giftshade = offerdata.get("giftshade").toString();		
		
		if(!(giftshade.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "GiftShade", giftshade);
			WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
			elmt.click();
			Thread.sleep(2000);
		}
		driver.findElement(By.xpath("//button[@class='button checkout']")).click();
	}

	public void select_duo(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kitname = offerdata.get("description").toString().toLowerCase();
		String duo = offerdata.get("duo").toString().toLowerCase();
		
		if(!(duo.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> duo_loc;
			if(brand.equalsIgnoreCase("Dr.Denese")) {
				duo_loc = comm_obj.get_element_locator(brand, campaign, "Duo", duo + " " + kitname);
			}
			else {
				duo_loc = comm_obj.get_element_locator(brand, campaign, "Duo", duo);
			}
			
			Thread.sleep(1000);
			WebElement duo_elmt = comm_obj.find_webelement(driver, duo_loc.get(0).get("elementlocator").toString(), duo_loc.get(0).get("elementvalue").toString());
			Thread.sleep(1000);
			duo_elmt.click();
			Thread.sleep(2000);
		}	
	}
	
	public void select_easypay(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String easypay = offerdata.get("easypay").toString().toLowerCase();
		String ppid = offerdata.get("ppid").toString().toLowerCase();
		String kit = offerdata.get("description").toString().toLowerCase();
		String description = offerdata.get("description").toString().toLowerCase();
		String category = offerdata.get("category").toString().toLowerCase();
		
		if(!(easypay.equalsIgnoreCase("n/a"))&& !((campaign.equalsIgnoreCase("newcc"))|| (category.equalsIgnoreCase("Product"))) ) {
			List<Map<String, Object>> pay_loc = comm_obj.get_element_locator(brand, campaign, "EasyPay", easypay + " " + kit);
			Thread.sleep(1000);
			WebElement pay_elmt = comm_obj.find_webelement(driver, pay_loc.get(0).get("elementlocator").toString(), pay_loc.get(0).get("elementvalue").toString());
			Thread.sleep(1000);
			pay_elmt.click();
			Thread.sleep(2000);

			if((brand.equalsIgnoreCase("DermaFlash")) && (campaign.equalsIgnoreCase("oneluxepnl2-ps"))) {
				driver.findElement(By.xpath("//button[@class='show-next']")).click();
				Thread.sleep(2000);
				driver.findElement(By.xpath("//button[@data-variant-id='" + ppid.toUpperCase() + "']")).click();
			}
		}	
		if((brand.equalsIgnoreCase("DermaFlash")) && (((campaign.equalsIgnoreCase("newcc")) && (easypay.equalsIgnoreCase("3pay"))) || ((campaign.equalsIgnoreCase("core")) && (easypay.equalsIgnoreCase("3pay")) && (category.equalsIgnoreCase("Product")))) ) {
			if(description.equalsIgnoreCase("DermaFlash"))
			{
				driver.findElement(By.xpath("(//select[@class = 'easyPay'])[1]")).click();
				driver.findElement(By.xpath("(//select[@class = 'easyPay'])[1]/option[2]")).click();
			}
			else {
				driver.findElement(By.xpath("(//select[@class = 'easyPay'])[last()]")).click();
				driver.findElement(By.xpath("(//select[@class = 'easyPay'])[last()]/option[2]")).click();
			}
		}
	}
	
	public void select_fragrance(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
						
		String kitname = offerdata.get("description").toString().toLowerCase();
		String fragrance = offerdata.get("fragrance").toString().toLowerCase();
		String category = offerdata.get("category").toString().toLowerCase();
		
		if((!(fragrance.equalsIgnoreCase("n/a")))  && (!(fragrance.equals(" ")))){
			
			List<Map<String, Object>> frag_loc = null;
			if(category.equalsIgnoreCase("Kit")) {
				frag_loc = comm_obj.get_element_locator(brand, campaign, "Fragrance", fragrance + " " + kitname);
			}
			else if(category.equalsIgnoreCase("Product")) {
				frag_loc = comm_obj.get_element_locator(brand, campaign, "Product-Fragrance", fragrance + " " + kitname);
			}
			if((category.equalsIgnoreCase("Product")) && (brand.equalsIgnoreCase("Mally"))){
				while(comm_obj.find_mulwebelement(driver, frag_loc.get(0).get("elementlocator").toString(), frag_loc.get(0).get("elementvalue").toString()).size() == 0){
					if(driver.findElements(By.xpath("//button[@class='slick-next slick-arrow valid']")).size() != 0) {
						driver.findElement(By.xpath("//button[@class='slick-next slick-arrow valid']")).click();
					}
				}
			}
			
			WebElement frag_elmt = comm_obj.find_webelement(driver, frag_loc.get(0).get("elementlocator").toString(), frag_loc.get(0).get("elementvalue").toString());
			Thread.sleep(2000);
			if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))) {
				WebElement fragParent = comm_obj.find_webelement(driver, "xpath", frag_loc.get(0).get("elementvalue").toString() + "/../../../..");
				if(fragParent.getAttribute("class").toLowerCase().contains("selected")) {
					
				}
				else {
					frag_elmt.click();
				}
			}
			else {
				frag_elmt.click();
			}			
			
			Thread.sleep(2000);
		}
	}
	
	public void select_productaddon(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String productaddon = offerdata.get("product").toString().toLowerCase();
		String[] addonArr = productaddon.split(",");
						
		if(!(productaddon.equalsIgnoreCase("n/a"))) {
			for(String addon : addonArr) {
				List<Map<String, Object>> addon_loc = comm_obj.get_element_locator(brand, campaign, "Productaddon", addon);			
				
				WebElement addon_elmt = comm_obj.find_webelement(driver, addon_loc.get(0).get("elementlocator").toString(), addon_loc.get(0).get("elementvalue").toString());
				Thread.sleep(1000);
				addon_elmt.click();
				Thread.sleep(1000);
			}
		}		
	}
	
	public void select_supplyvalue(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String supplyvalue = offerdata.get("supplyvalue").toString().toLowerCase();
		
		if(!(supplyvalue.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> value_loc = comm_obj.get_element_locator(brand, campaign, "SupplyValue", supplyvalue);
			WebElement value_elmt = comm_obj.find_webelement(driver, value_loc.get(0).get("elementlocator").toString(), value_loc.get(0).get("elementvalue").toString());
			value_elmt.click();
		}		
		Thread.sleep(1000);
	}
	
	public void select_frequency(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String product_name = offerdata.get("description").toString().toLowerCase();
		String frequency = offerdata.get("frequency").toString();
		if(!(frequency.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> freq_loc = comm_obj.get_element_locator(brand, campaign, "Frequency", product_name);
			WebElement freq_elmt = comm_obj.find_webelement(driver, freq_loc.get(0).get("elementlocator").toString(), freq_loc.get(0).get("elementvalue").toString());
			Select freq = new Select(freq_elmt);
			freq.selectByVisibleText(frequency);
		}	
		Thread.sleep(1000);
	}
	
	public void select_product(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		
		String product_name = offerdata.get("description").toString().toLowerCase();
		
		List<Map<String, Object>> prod_loc = comm_obj.get_element_locator(brand, campaign, "Product", product_name);			
		
		while(comm_obj.find_mulwebelement(driver, prod_loc.get(0).get("elementlocator").toString(), prod_loc.get(0).get("elementvalue").toString()).size() == 0){
			jse.executeScript("window.scrollBy(0,1000)", 0);
		}
		WebElement prod_elmt = comm_obj.find_webelement(driver, prod_loc.get(0).get("elementlocator").toString(), prod_loc.get(0).get("elementvalue").toString());
		Thread.sleep(2000);
		
		if(((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("Core")))) {
			Actions act = new Actions(driver);
			act.moveToElement(prod_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = prod_loc.get(0).get("elementvalue").toString() + "//div//div[@class='product-wrap']//div[5]//div//a";
			prod_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}		
		prod_elmt.click();					
		Thread.sleep(1000);
		
		if((brand.equalsIgnoreCase("SpecificBeauty")) || (brand.equalsIgnoreCase("SeaCalmSkin")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("WestmoreBeauty"))){
			if(driver.findElements(By.id("add-to-cart")).size() != 0){
				driver.findElement(By.id("add-to-cart")).click();
			}
		}
	}
	
	public void select_onetime(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String product = offerdata.get("description").toString();
		String fragrance = offerdata.get("fragrance").toString();
		String frag = "";
		if((fragrance.equalsIgnoreCase(" ")) || (fragrance.equalsIgnoreCase("n/a"))){
			frag = product;
		}
		else {
			frag = fragrance + " " + product;
		}
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "Product-Onetime", frag);
						
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
		Thread.sleep(2000);	
		elmt.click();
		Thread.sleep(2000);
	}
	
	public void select_subscribe(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String product = offerdata.get("description").toString();
		String fragrance = offerdata.get("fragrance").toString();
		String frag = "";
		if(fragrance.equalsIgnoreCase(" ")) {
			frag = product;
		}
		else {
			frag = fragrance + " " + product;
		}
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "Product-Subscribe", frag);
				
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("elementlocator").toString(), locator.get(0).get("elementvalue").toString());
		Thread.sleep(1000);	
		elmt.click();
		Thread.sleep(1000);
		driver.findElement(By.id("add-to-cart")).click();
	}
}
