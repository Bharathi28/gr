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
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='Product' and status='Active'";
//		System.out.println(query);
		List<Map<String, Object>> singles = DBLibrary.dbAction("fetch",query);
		
		List<Map<String, Object>> randsingles = new ArrayList<Map<String, Object>>();
		Random rand = new Random(); 
		for(int i=0; i<count; i++) {
			randsingles.add(singles.get(rand.nextInt(singles.size())));
		}
		
		List<String> single_offers = new ArrayList<String>();
		for(Map<String, Object> product : randsingles) {
			single_offers.add(product.get("PPID").toString());
		}
		System.out.println("Chosen single : " + single_offers);
		return single_offers;
	}
		
	public String get_offer(WebDriver driver, String env, String brand, String campaign, String ppid, String category, int subscribe, String nav) throws ClassNotFoundException, SQLException, InterruptedException {		
		
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String ppid_str = "";
		Map<String, Object> offerdata;
		
		ppid_str = ppid;		
		offerdata = DBUtilities.get_offerdata(ppid, brand, campaign, category);
		select_offer(driver, env, brand, campaign, offerdata, category, subscribe);
		return ppid_str;
	}	
	
	public void select_offer(WebDriver driver, String env, String brand, String campaign, Map<String, Object> offerdata, String category, int subscribe) throws ClassNotFoundException, SQLException, InterruptedException {
				
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String pagepattern = offerdata.get("PAGEPATTERN").toString().toLowerCase();
		if(subscribe == 1) {
			pagepattern = pagepattern.replace("onetime", "subscribe");
		}
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
	    	case "shopkit":
	    		select_shopkit(driver, offerdata, brand, campaign);
	    		break;
	    	case "onetime":
				select_onetime(driver, offerdata, brand, campaign);
				break;
	    	case "subscribe":
				select_subscribe(driver, offerdata, brand, campaign);
				break;
			}
		}		
		add_product_to_cart(driver, brand, campaign, category);
	}
	
	public void add_product_to_cart(WebDriver driver, String brand, String campaign, String category) throws InterruptedException {
		Thread.sleep(2000);
		System.out.println("Adding product to cart");
		if((category.equalsIgnoreCase("Product")) || (category.equalsIgnoreCase("ShopKit")) || (category.equalsIgnoreCase("SubscribeandSave"))) {
			if((brand.equalsIgnoreCase("BodyFirm")) || (brand.equalsIgnoreCase("SpotFade")) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("PrincipalSecret")) || (brand.equalsIgnoreCase("SpecificBeauty")) || (brand.equalsIgnoreCase("WestmoreBeauty")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("Smileactives")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("SeaCalmSkin"))){
				if(driver.findElements(By.xpath("//button[@id='add-to-cart']")).size() != 0) {
					Thread.sleep(3000);
					driver.findElement(By.xpath("//button[@id='add-to-cart']")).click();
				}
			}
		}
	}
	
	public void select_kit(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kit = offerdata.get("DESCRIPTION").toString();
		JavascriptExecutor jse = (JavascriptExecutor) driver;
//		WebDriverWait wait = new WebDriverWait(driver, 10);
		
		List<Map<String, Object>> kit_loc = comm_obj.get_element_locator(brand, campaign, "Kit", kit);
		Thread.sleep(2000);
		WebElement kit_elmt = comm_obj.find_webelement(driver, kit_loc.get(0).get("ELEMENTLOCATOR").toString(), kit_loc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(3000);
//		wait.until(ExpectedConditions.elementToBeClickable(kit_elmt));
		
		if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
			Actions act = new Actions(driver);
			act.moveToElement(kit_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = kit_loc.get(0).get("ELEMENTVALUE").toString() + "//div//div[@class='product-wrap']//div[3]//div//a";
			kit_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}

		if(brand.equalsIgnoreCase("FixMDSkin")){
			jse.executeScript("window.scrollBy(0,250)", 0);
			Thread.sleep(2000);
		}
		kit_elmt.click();	
		Thread.sleep(2000);
		if((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("Core"))) {
			jse.executeScript("window.scrollBy(0,700)", 0);
			driver.findElement(By.xpath("(//button[@class='button primary next-section'])[1]")).click();
		}
		if(((brand.equalsIgnoreCase("Volaire")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("Volaire")) && (campaign.equalsIgnoreCase("newcc"))) || ((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("fb")))){
			jse.executeScript("window.scrollBy(0,1000)", 0);
			Thread.sleep(2000);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}
		
		if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("specialCore"))) {
			jse.executeScript("window.scrollBy(0,800)", 0);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}
		if((brand.equalsIgnoreCase("CrepeErase"))  && ((campaign.equalsIgnoreCase("advancedone"))||(campaign.equalsIgnoreCase("prehctPanelb"))||(campaign.equalsIgnoreCase("prehct")))) {
			jse.executeScript("window.scrollBy(0,200)", 0);
			driver.findElement(By.xpath("//button[@class = 'button checkout special-offer']")).click();
		}
		if((brand.equalsIgnoreCase("CrepeErase"))&&(campaign.equalsIgnoreCase("10off"))) {
			jse.executeScript("window.scrollBy(0,200)", 0);
			driver.findElement(By.xpath("//button[@class = 'button checkout']")).click();
		}

		if((brand.equalsIgnoreCase("CrepeErase"))&&(campaign.equalsIgnoreCase("core_full_15neck"))) {
			jse.executeScript("window.scrollBy(0,200)", 0);
			driver.findElement(By.xpath("//div[@class = 'sas-sticky-footer']//a[contains(text(),'Proceed to Checkout')]")).click();
		}
		if(brand.equalsIgnoreCase("SpotFade")) {
			jse.executeScript("window.scrollBy(0,300)", 0);
			Thread.sleep(1000);
			driver.findElement(By.xpath("//a[@class='cta']")).click();
		}

		if((brand.equalsIgnoreCase("MeaningfulBeauty"))&&(campaign.equalsIgnoreCase("mb7deluxe20offb"))) {
			jse.executeScript("window.scrollBy(0,200)",0);
			driver.findElement(By.xpath("//button[@class = 'button checkout-special-offer']")).click();
		}
		
		if(!(brand.equalsIgnoreCase("SpecificBeauty"))) {	
			if(brand.equalsIgnoreCase("Sheercover")) {
				jse.executeScript("window.scrollBy(0,200)",0);
				Thread.sleep(1000);
				if(driver.findElements(By.xpath("//div[@id='kit']/..//div[@class='market']//a[@class='buttons-next button-gift']")).size() != 0) {
					driver.findElement(By.xpath("//div[@id='kit']/..//div[@class='market']//a[@class='buttons-next button-gift']")).click();
				}
			}
			else {
				if(driver.findElements(By.cssSelector("#kit ~ .market a.buttons-next")).size() != 0) {
					driver.findElement(By.cssSelector("#kit ~ .market a.buttons-next")).click();
				}
			}
		}
		Thread.sleep(1000);
	}
	
	public void select_gift(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String gift = offerdata.get("GIFT").toString();
		String kit = offerdata.get("DESCRIPTION").toString();
//		if((brand.equalsIgnoreCase("MeaningfulBeauty"))&&((campaign.equalsIgnoreCase("pnlsys")) || (campaign.equalsIgnoreCase("core")))&&(gift.equalsIgnoreCase("Skin Rejuvenating Trio"))) {
//			driver.findElement(By.xpath("//ul[@class = 'slick-dots']//li[2]/button")).click();
//			Thread.sleep(1000);
//		}
		
		if((brand.equalsIgnoreCase("CrepeErase")) && ((campaign.equalsIgnoreCase("Core")) || (campaign.equalsIgnoreCase("core_full_10sfwand")) || (campaign.equalsIgnoreCase("crepeerase")))){
			jse.executeScript("window.scrollBy(0,700)", 0);
//			jse.executeScript("window.scrollBy(0,900)", 0);
//			if(gift.equalsIgnoreCase("Protect and Renew Skin Trio")) {
//				Thread.sleep(3000);
//				driver.findElement(By.xpath("//ul[@class='slick-dots']//li[2]//button")).click();
//			}
			Thread.sleep(3000);
		}
//		System.out.println(gift);
		if(!(gift.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> gift_loc = comm_obj.get_element_locator(brand, campaign, "Gift", gift);
//			System.out.println(gift_loc.get(0).get("ELEMENTLOCATOR").toString());
//			System.out.println(gift_loc.get(0).get("ELEMENTVALUE").toString());
			WebElement gift_elmt = comm_obj.find_webelement(driver, gift_loc.get(0).get("ELEMENTLOCATOR").toString(), gift_loc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(4000);
//			System.out.println(gift_elmt.isDisplayed());
			gift_elmt.click();
			Thread.sleep(1000);								
		}		

		if((brand.equalsIgnoreCase("CrepeErase"))  && ((campaign.equalsIgnoreCase("corehrt"))||(campaign.equalsIgnoreCase("pnlbb"))||(campaign.equalsIgnoreCase("corehrt2")))) {
			jse.executeScript("window.scrollBy(0,500)", 0);
			driver.findElement(By.id("valuePack-next-btn")).click();
		}

		if((brand.equalsIgnoreCase("CrepeErase")) && ((campaign.equalsIgnoreCase("core")) || (campaign.equalsIgnoreCase("core_full_10sfwand")) || (campaign.equalsIgnoreCase("deluxe20offtv")) || (campaign.equalsIgnoreCase("20offDeluxeSpring")))) {
			jse.executeScript("window.scrollBy(0,200)", 0);
			driver.findElement(By.xpath("//div[@class = 'sas-sticky-footer']//a[contains(text(),'Proceed to Checkout')]")).click();
		}

//		else if((brand.equalsIgnoreCase("CrepeErase"))&&(campaign.equalsIgnoreCase("crepeerase"))) {
//			jse.executeScript("window.scrollBy(0,400)", 0);
//			if(kit.equalsIgnoreCase("Advanced 5-Piece Body + Face System")) {
//				driver.findElement(By.xpath("(//div[@class = 'checkout-button sas-kit-sticky-checkout hide-section']//button[@class = 'button checkout'])[1]")).click();
//			}
//			else {
//				driver.findElement(By.xpath("(//div[@class = 'checkout-button sas-kit-sticky-checkout hide-section']//button[@class = 'button checkout'])[2]")).click();
//			}
//		}			

		if((brand.equalsIgnoreCase("CrepeErase"))&&(campaign.equalsIgnoreCase("crepeerase"))) {
			jse.executeScript("window.scrollBy(0,400)", 0);
//			driver.findElement(By.xpath("//h3[contains(text(),'" + kit +"')]/../..//div[@class='checkout-button sas-kit-sticky-checkout hide-section']//button")).click();

		}
		else if((brand.equalsIgnoreCase("MeaningfulBeauty")) && (campaign.equalsIgnoreCase("Core"))) {
			jse.executeScript("window.scrollBy(0,300)", 0);
			driver.findElement(By.xpath("//button[@class='button checkout-special-offer']")).click();
		}
		else if((brand.equalsIgnoreCase("MeaningfulBeauty")) && (campaign.equalsIgnoreCase("20offdeluxe"))) {
			driver.findElement(By.xpath("//button[@class='button checkout-special-offer']")).click();
		}	
		else if((brand.equalsIgnoreCase("MeaningfulBeauty"))&&(campaign.equalsIgnoreCase("pnlsys"))) {
			driver.findElement(By.xpath("//button[@class = 'button checkout']")).click();
		}
		else if((brand.equalsIgnoreCase("MeaningfulBeauty"))&&(campaign.equalsIgnoreCase("bht"))) {
			driver.findElement(By.xpath("//button[@class = 'button checkout-special-offer']")).click();
		}
		else if(driver.findElements(By.cssSelector("#gift ~ .market a.buttons-next")).size() != 0) {
			driver.findElement(By.cssSelector("#gift ~ .market a.buttons-next")).click();
		}
	}

	public void select_supply(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kit = offerdata.get("DESCRIPTION").toString();
		String supply = offerdata.get("SUPPLYSIZE").toString();
		
		List<Map<String, Object>> supply_loc = comm_obj.get_element_locator(brand, campaign, "Supply", supply + " " + kit);
		WebElement elmt = comm_obj.find_webelement(driver, supply_loc.get(0).get("ELEMENTLOCATOR").toString(), supply_loc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		elmt.click();
		Thread.sleep(1000);
		
		if(driver.findElements(By.cssSelector("#supply ~ .market a.buttons-next")).size() != 0) {
			driver.findElement(By.cssSelector("#supply ~ .market a.buttons-next")).click();
		}
	}
	
	public void select_shade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String name = offerdata.get("DESCRIPTION").toString();
		String shade = offerdata.get("SHADE").toString();
		String category = offerdata.get("CATEGORY").toString();
		
		if(!(shade.equalsIgnoreCase("n/a"))) {
			
			List<Map<String, Object>> shade_loc = null;		
			if(category.equalsIgnoreCase("Kit")) {
				shade_loc = comm_obj.get_element_locator(brand, campaign, "Shade", shade);
			}
			else if(category.equalsIgnoreCase("Product")) {
				shade_loc = comm_obj.get_element_locator(brand, campaign, "Shade", shade + " " + name);
			} 
			WebElement shade_elmt = comm_obj.find_webelement(driver, shade_loc.get(0).get("ELEMENTLOCATOR").toString(), shade_loc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(1000);
			shade_elmt.click();
			Thread.sleep(1000);
		}	
	}
	
	public void select_kitshade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String kitname = offerdata.get("DESCRIPTION").toString();
		String kitshade = offerdata.get("KITSHADE").toString();
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "KitShade", kitshade + " " + kitname);
		
		if(brand.equalsIgnoreCase("WestmoreBeauty")) {
			if((campaign.equalsIgnoreCase("hybrid")) || (campaign.equalsIgnoreCase("bodycoverage"))){
				jse.executeScript("window.scrollBy(0,400)", 0);
				Thread.sleep(2000);
			}
		}
		
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(4000);
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
		if(brand.equalsIgnoreCase("WestmoreBeauty")){
			if(campaign.equalsIgnoreCase("hybrid")) {
				jse.executeScript("window.scrollBy(0,500)", 0);
				Thread.sleep(2000);
			}
		}
	}
	
	public void select_kitshade2(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String kitname = offerdata.get("DESCRIPTION").toString();
		String kitshade2 = offerdata.get("KITSHADE2").toString();
		List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "KitShade2", kitshade2 + " " + kitname);
		
		WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);
		elmt.click();
		Thread.sleep(2000);
	}
	
	public void select_giftshade(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
				
		String giftshade = offerdata.get("GIFTSHADE").toString();		
		
		if(!(giftshade.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> locator = comm_obj.get_element_locator(brand, campaign, "GiftShade", giftshade);
			WebElement elmt = comm_obj.find_webelement(driver, locator.get(0).get("ELEMENTLOCATOR").toString(), locator.get(0).get("ELEMENTVALUE").toString());
			elmt.click();
			Thread.sleep(2000);
		}
		driver.findElement(By.xpath("//button[@class='button checkout']")).click();
	}

	public void select_duo(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kitname = offerdata.get("DESCRIPTION").toString();
		String duo = offerdata.get("DUO").toString();
		
		if(!(duo.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> duo_loc = null;
			if(brand.equalsIgnoreCase("Dr.Denese")) {
				duo_loc = comm_obj.get_element_locator(brand, campaign, "Duo", duo + " " + kitname);
			}
			else {
				duo_loc = comm_obj.get_element_locator(brand, campaign, "Duo", duo);
			} 			
			Thread.sleep(1000);
			WebElement duo_elmt = comm_obj.find_webelement(driver, duo_loc.get(0).get("ELEMENTLOCATOR").toString(), duo_loc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(1000);
			duo_elmt.click();
			Thread.sleep(2000);
		}	
	}
	
	public void select_easypay(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String easypay = offerdata.get("EASYPAY").toString();
		String ppid = offerdata.get("PPID").toString();
		String kit = offerdata.get("DESCRIPTION").toString();
		String description = offerdata.get("DESCRIPTION").toString();
		String category = offerdata.get("CATEGORY").toString();
		
		if(!(easypay.equalsIgnoreCase("n/a"))&& !((campaign.equalsIgnoreCase("newcc"))|| (category.equalsIgnoreCase("Product"))) ) {
			List<Map<String, Object>> pay_loc = comm_obj.get_element_locator(brand, campaign, "EasyPay", easypay + " " + kit);
//			System.out.println(pay_loc);
			Thread.sleep(3000);
			jse.executeScript("window.scrollBy(0,250)", 0);
			WebElement pay_elmt = comm_obj.find_webelement(driver, pay_loc.get(0).get("ELEMENTLOCATOR").toString(), pay_loc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(1000);
			pay_elmt.click();
			Thread.sleep(2000);


			if((brand.equalsIgnoreCase("DermaFlash")) && (campaign.equalsIgnoreCase("oneluxepnl2-ps"))) {
				//if((easypay.equalsIgnoreCase("3pay"))&&
				driver.findElement(By.xpath("//button[@class = 'show-next']")).click();

				Thread.sleep(2000);
				driver.findElement(By.xpath("//button[@data-variant-id='" + ppid.toUpperCase() + "']")).click();
			}
		}	
		if((brand.equalsIgnoreCase("TryDermaFlash")) && (((campaign.equalsIgnoreCase("newcc")) && (easypay.equalsIgnoreCase("3pay"))) || ((campaign.equalsIgnoreCase("core")) && (easypay.equalsIgnoreCase("3pay")) && (category.equalsIgnoreCase("Product")))) ) {
			if(description.equalsIgnoreCase("TryDermaFlash"))
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
	
//	public void select_fragrance(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
//						
//		String kitname = offerdata.get("DESCRIPTION").toString();
//		String fragrance = offerdata.get("FRAGRANCE").toString();		
//		String category = offerdata.get("CATEGORY").toString();
//		
//		if((!(fragrance.equalsIgnoreCase("n/a")))  && (!(fragrance.equals(" ")))){
//			
//			List<Map<String, Object>> frag_loc = null;
//			if(category.equalsIgnoreCase("Kit")) {
//				frag_loc = comm_obj.get_element_locator(brand, campaign, "Fragrance", fragrance + " " + kitname);
//				
//				WebElement frag_elmt = comm_obj.find_webelement(driver, frag_loc.get(0).get("ELEMENTLOCATOR").toString(), frag_loc.get(0).get("ELEMENTVALUE").toString());
//				Thread.sleep(2000);
//				if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))) {
//					WebElement fragParent = comm_obj.find_webelement(driver, "xpath", frag_loc.get(0).get("ELEMENTVALUE").toString() + "/../../../..");
//					if(fragParent.getAttribute("class").toLowerCase().contains("selected")) {
//						
//					}
//					else {
//						frag_elmt.click();
//					}
//				}
//			}
//			else if((category.equalsIgnoreCase("Product")) || (category.equalsIgnoreCase("ShopKit"))) {
//				if(brand.equalsIgnoreCase("CrepeErase")) {
//					Select sel_element = new Select(driver.findElement(By.xpath("//ul[@class='variations-section clearfix']//li//div[3]//select")));
//					sel_element.selectByVisibleText(offerdata.get("FRAGRANCE").toString());
//				}
//				else {
//					String PPID = offerdata.get("PPID").toString();
//					String fragrance_loc = "//li[@data-variantid='" + PPID + "']//a";
//					if(brand.equalsIgnoreCase("Mally")) {
//						if(!(driver.findElement(By.xpath(fragrance_loc)).isDisplayed())) {
//							if(driver.findElements(By.xpath("//div[@class='product-variations large-12 small-12 clearfix']//ul//li//div[2]//ul//button[2]")).size() != 0) {
//								driver.findElement(By.xpath("//div[@class='product-variations large-12 small-12 clearfix']//ul//li//div[2]//ul//button[2]")).click();
//							}
//						}
//					}
//					Thread.sleep(1000);
//					driver.findElement(By.xpath(fragrance_loc)).click();
//					Thread.sleep(1000);
//				}	
//			}
//			Thread.sleep(2000);
//		}
//	}	
	
	public void select_fragrance(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String kitname = offerdata.get("DESCRIPTION").toString();
		String fragrance = offerdata.get("FRAGRANCE").toString();		
		String category = offerdata.get("CATEGORY").toString();
		
		if((!(fragrance.equalsIgnoreCase("n/a")))  && (!(fragrance.equals(" ")))){
			
			List<Map<String, Object>> frag_loc = null;
			if(category.equalsIgnoreCase("Kit")) {
				frag_loc = comm_obj.get_element_locator(brand, campaign, "Fragrance", fragrance + " " + kitname);						
			}
			else if(category.equalsIgnoreCase("Product")) {
				
				frag_loc = comm_obj.get_element_locator(brand, campaign, "Product-Fragrance", fragrance + " " + kitname);

				if(brand.equalsIgnoreCase("Mally")) {
					if(!(comm_obj.find_webelement(driver, frag_loc.get(0).get("ELEMENTLOCATOR").toString(), frag_loc.get(0).get("ELEMENTVALUE").toString()).isDisplayed())) {
						if(driver.findElements(By.xpath("//div[@class='product-variations large-12 small-12 clearfix']//ul//li//div[2]//ul//button[2]")).size() != 0) {
							driver.findElement(By.xpath("//div[@class='product-variations large-12 small-12 clearfix']//ul//li//div[2]//ul//button[2]")).click();
						}
					}
				}
			}
			else if(category.equalsIgnoreCase("ShopKit")) {
				frag_loc = comm_obj.get_element_locator(brand, campaign, "ShopKit-Fragrance", fragrance + " " + kitname);
			}
			
			WebElement frag_elmt = comm_obj.find_webelement(driver, frag_loc.get(0).get("ELEMENTLOCATOR").toString(), frag_loc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(2000);
			if((brand.equalsIgnoreCase("WestmoreBeauty")) && (campaign.equalsIgnoreCase("eyeoffer"))) {
				WebElement fragParent = comm_obj.find_webelement(driver, "xpath", frag_loc.get(0).get("ELEMENTVALUE").toString() + "/../../../..");
				if(fragParent.getAttribute("class").toLowerCase().contains("selected")) {
					
				}
				else {
					frag_elmt.click();
				}
			}
			else if((brand.equalsIgnoreCase("CrepeErase")) && ((category.equalsIgnoreCase("Product")) || (category.equalsIgnoreCase("ShopKit")))){
				Select sel_element = new Select(driver.findElement(By.xpath("//ul[@class='variations-section clearfix']//li//div[3]//select")));
				sel_element.selectByVisibleText(offerdata.get("FRAGRANCE").toString());
			}
			else if((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("crepeerase")) && (category.equalsIgnoreCase("Kit"))){
				Select sel_element = new Select(driver.findElement(By.id("Fragrance-citrus-Selection")));
				sel_element.selectByVisibleText(offerdata.get("FRAGRANCE").toString());
				Thread.sleep(2000);
				driver.findElement(By.xpath("//a[@class='cta']")).click();
			}
			else {
				frag_elmt.click();
			}			
			Thread.sleep(2000);
		}
	}	
	
	public void select_productaddon(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String productaddon = offerdata.get("PRODUCT").toString();
		String[] addonArr = productaddon.split(",");
						
		if(!(productaddon.equalsIgnoreCase("n/a"))) {
			for(String addon : addonArr) {
				List<Map<String, Object>> addon_loc = comm_obj.get_element_locator(brand, campaign, "Productaddon", addon);			
				
				WebElement addon_elmt = comm_obj.find_webelement(driver, addon_loc.get(0).get("ELEMENTLOCATOR").toString(), addon_loc.get(0).get("ELEMENTVALUE").toString());
				Thread.sleep(1000);
				addon_elmt.click();
				Thread.sleep(1000);
			}
		}		
	}
	
	public void select_supplyvalue(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String supplyvalue = offerdata.get("SUPPLYVALUE").toString();
		
		if(!(supplyvalue.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> value_loc = comm_obj.get_element_locator(brand, campaign, "SupplyValue", supplyvalue);
			WebElement value_elmt = comm_obj.find_webelement(driver, value_loc.get(0).get("ELEMENTLOCATOR").toString(), value_loc.get(0).get("ELEMENTVALUE").toString());
			value_elmt.click();
		}		
		Thread.sleep(1000);
	}
	
	public void select_frequency(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		
		String product_name = offerdata.get("DESCRIPTION").toString();
		String frequency = offerdata.get("FREQUENCY").toString();
		if(!(frequency.equalsIgnoreCase("n/a"))) {
			List<Map<String, Object>> freq_loc = comm_obj.get_element_locator(brand, campaign, "Frequency", product_name);
			WebElement freq_elmt = comm_obj.find_webelement(driver, freq_loc.get(0).get("ELEMENTLOCATOR").toString(), freq_loc.get(0).get("ELEMENTVALUE").toString());
			Select freq = new Select(freq_elmt);
			freq.selectByVisibleText(frequency);
		}	
		Thread.sleep(1000);
	}
	
	public void select_product(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		
		String product_name = offerdata.get("DESCRIPTION").toString();
		
		List<Map<String, Object>> prod_loc = comm_obj.get_element_locator(brand, campaign, "Product", product_name);			
		
		while(comm_obj.find_mulwebelement(driver, prod_loc.get(0).get("ELEMENTLOCATOR").toString(), prod_loc.get(0).get("ELEMENTVALUE").toString()).size() == 0){
			jse.executeScript("window.scrollBy(0,400)", 0);
		}
		WebElement prod_elmt = comm_obj.find_webelement(driver, prod_loc.get(0).get("ELEMENTLOCATOR").toString(), prod_loc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);		
		if(((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("Core")))) {
			Actions act = new Actions(driver);
			act.moveToElement(prod_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = prod_loc.get(0).get("ELEMENTVALUE").toString() + "//div//div[@class='product-wrap']//div[5]//div//a";
			prod_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);	
		}		
		else if(((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("SpotFade")) && (campaign.equalsIgnoreCase("Core")))){
			Actions act = new Actions(driver);
			act.moveToElement(prod_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = prod_loc.get(0).get("ELEMENTVALUE").toString() + "//div//div//h4//a";
			prod_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}
		prod_elmt.click();	
		Thread.sleep(1000);
	}
	
	public void select_shopkit(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		
		String product_name = offerdata.get("DESCRIPTION").toString();
		
		List<Map<String, Object>> prod_loc = comm_obj.get_element_locator(brand, campaign, "ShopKit", product_name);			
		
		while(comm_obj.find_mulwebelement(driver, prod_loc.get(0).get("ELEMENTLOCATOR").toString(), prod_loc.get(0).get("ELEMENTVALUE").toString()).size() == 0){
			jse.executeScript("window.scrollBy(0,400)", 0);
		}
		WebElement prod_elmt = comm_obj.find_webelement(driver, prod_loc.get(0).get("ELEMENTLOCATOR").toString(), prod_loc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(2000);
		
		if(((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("Smileactives")) && (campaign.equalsIgnoreCase("Core")))) {
			Actions act = new Actions(driver);
			act.moveToElement(prod_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = prod_loc.get(0).get("ELEMENTVALUE").toString() + "//div//div[@class='product-wrap']//div[5]//div//a";
			prod_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}		
		else if(((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("Core"))) || ((brand.equalsIgnoreCase("SpotFade")) && (campaign.equalsIgnoreCase("Core")))){
			Actions act = new Actions(driver);
			act.moveToElement(prod_elmt).perform();
			Thread.sleep(1000);
			
			String shop_loc = prod_loc.get(0).get("ELEMENTVALUE").toString() + "//div//div//h4//a";
			prod_elmt = comm_obj.find_webelement(driver, "xpath", shop_loc);
			Thread.sleep(2000);
		}
		prod_elmt.click();					
		Thread.sleep(1000);
	}
	
	public void select_onetime(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String PPID = offerdata.get("PPID").toString();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//input[@id='dwopt_" + PPID + "_entryKit-one-pay']")).click();
		Thread.sleep(1000);
	}
	
	public void select_subscribe(WebDriver driver, Map<String, Object> offerdata, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String PPID = offerdata.get("PPID").toString();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//input[@id='dwopt_" + PPID + "_entryKit-auto-renew']")).click();
		Thread.sleep(1000);
	}
}
