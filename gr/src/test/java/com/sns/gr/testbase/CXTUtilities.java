package com.sns.gr.testbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class CXTUtilities {
	CommonUtilities comm_obj = new CommonUtilities();
	DBUtilities db_obj = new DBUtilities();
	
	public void takeScreenshot(WebDriver driver, String brand, String step, String result, String part) throws IOException {
		Calendar now = Calendar.getInstance();		
		String monthStr = Integer.toString(now.get(Calendar.MONTH) + 1); // Note: zero based!
		String dayStr = Integer.toString(now.get(Calendar.DAY_OF_MONTH));  
		String yearStr = Integer.toString(now.get(Calendar.YEAR));
		
		String dateStr = monthStr + dayStr + yearStr;
		
		Screenshot ss = null;
		if(part.equalsIgnoreCase("visiblepart")) {
			ss = new AShot().takeScreenshot(driver);
		}
		else if(part.equalsIgnoreCase("fullpage")) {
			ss = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(ShootingStrategies.scaling(1.25f), 1000)).takeScreenshot(driver);
		}
		
		String filename = "";
		if(result.equalsIgnoreCase("Success")) {
			filename = System.getProperty("user.dir") + "\\Input_Output\\CXTValidation\\Screenshots\\" + brand + "\\" + step + "_" + dateStr +".png";
			ImageIO.write(ss.getImage(),"PNG",new File(filename));
		}
		else {
			filename = System.getProperty("user.dir") + "\\Input_Output\\CXTValidation\\Run_Output\\" + brand + "_" + step + "_" + dateStr +".png";
			ImageIO.write(ss.getImage(),"PNG",new File(filename));
			filename = System.getProperty("user.dir") + "\\Input_Output\\CXTValidation\\Screenshots\\" + brand + "\\" + step + "_" + dateStr +".png";
			ImageIO.write(ss.getImage(),"PNG",new File(filename));
		}		
	}
	
	public void openMyNextKit(WebDriver driver, String realm) throws ClassNotFoundException, SQLException {
		List<Map<String, Object>> kcloc = get_cxt_locator(realm, "OpenKC", "");		
		WebElement openkc = comm_obj.find_webelement(driver, kcloc.get(0).get("ELEMENTLOCATOR").toString(), kcloc.get(0).get("ELEMENTVALUE").toString());		
		String drawerstatus = "";
		if(realm.equals("R4")) {
			drawerstatus = openkc.getText();
		}
		else if(realm.equals("R2")) {
			drawerstatus = driver.findElement(By.xpath("//div[@id='openCloseKitDrawer']//a//div//span[2]//span")).getAttribute("class");
		}
		if((drawerstatus.contains("Show")) || (drawerstatus.contains("closed"))){
			openkc.click();
		}
	}
	
	public int getNumberofProductsinKC(WebDriver driver, String realm) throws ClassNotFoundException, SQLException {
		openMyNextKit(driver, realm);
		
		List<Map<String, Object>> kcloc = get_cxt_locator(realm, "KCLocator", "");		
		List<WebElement> kcproducts= comm_obj.find_mulwebelement(driver, kcloc.get(0).get("ELEMENTLOCATOR").toString(), kcloc.get(0).get("ELEMENTVALUE").toString());		
		System.out.println("No.of Products in KC : " + kcproducts.size());
		
		return kcproducts.size();
	}
	
	public HashMap<String,Integer> checkMyNextKit(WebDriver driver, String realm) throws ClassNotFoundException, SQLException{
				
		HashMap<String, Integer> products = new HashMap<String, Integer>();
		openMyNextKit(driver, realm);
		
		List<Map<String, Object>> kcloc = get_cxt_locator(realm, "KCLocator", "");		
		List<WebElement> kcproducts= comm_obj.find_mulwebelement(driver, kcloc.get(0).get("ELEMENTLOCATOR").toString(), kcloc.get(0).get("ELEMENTVALUE").toString());		
		System.out.println("No.of Products in KC : " + kcproducts.size());
		
		String attribute = "";
		if(realm.equals("R4")) {
			attribute = "data-itemid";
		}
		else {
			attribute = "data-id";
		}
		
		for(WebElement prod : kcproducts) {
			String ppid = prod.getAttribute(attribute);
			
			if(products.containsKey(ppid)) {
				products.put(ppid, products.get(ppid) + 1);
			}
			else {
				products.put(ppid, 1);
			}			
		}
		System.out.println(products);
		return products;
	}
	
	public Map<String, Object> pickRandomProduct(String brand, String campaign) throws ClassNotFoundException, SQLException {
		String query = "select * from cxt_offers where brand='" + brand + "' and campaign='" + campaign + "' and status='Active'";
		List<Map<String, Object>> cxtoffers = DBLibrary.dbAction("fetch", query);

		Random rand = new Random(); 
		return cxtoffers.get(rand.nextInt(cxtoffers.size()));
	}
	
	public boolean validateProdLimitinKC(String brand, String campaign, int kcproductcount) throws ClassNotFoundException, SQLException {
		String query = "select * from cxt_accounts where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> accountDetails = DBLibrary.dbAction("fetch", query);
		
		String min = accountDetails.get(0).get("MINLIMIT").toString();
		String max = accountDetails.get(0).get("MAXLIMIT").toString();
		int minlimit = Integer.parseInt(min);  
		int maxlimit = Integer.parseInt(max);  
		System.out.println("Brand Minlimit : " + minlimit);
		System.out.println("Brand Maxlimit : " + maxlimit);
		System.out.println("No.of products in KC : " + kcproductcount);
		boolean addtokc = false;
		if((kcproductcount >= minlimit) && (kcproductcount < maxlimit)) {
			addtokc = true;
		}
		else {
			addtokc = false;
		}
		return addtokc;
	}
	
	public boolean validateIndProdCountinKC(String brand, String campaign, String PPID, int count) throws ClassNotFoundException, SQLException {
		String query = "select * from cxt_offers where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + PPID + "'";
		List<Map<String, Object>> offerDetails = DBLibrary.dbAction("fetch", query);
		
		String min = offerDetails.get(0).get("MINLIMIT").toString();
		String max = offerDetails.get(0).get("MAXLIMIT").toString();
		int minlimit = Integer.parseInt(min);  
		int maxlimit = Integer.parseInt(max);
		System.out.println("Product Minlimit : " + minlimit);
		System.out.println("Product Maxlimit : " + maxlimit);
		System.out.println("No.of same products : " + count);
		boolean addprodtokc = false;
		if((count >= minlimit) && (count < maxlimit)) {
			addprodtokc = true;
		}
		else {
			addprodtokc = false;
		}
		return addprodtokc;
	}
	
	public boolean checkAddToKitOption(String brand, String campaign, String PPID) throws ClassNotFoundException, SQLException {
		String query = "select * from cxt_offers where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + PPID + "'";
		List<Map<String, Object>> offerDetails = DBLibrary.dbAction("fetch", query);
		
		String addtokit = offerDetails.get(0).get("ADDTOKIT").toString();
		boolean addprodtokc = false;
		if(addtokit.equals("Yes")) {
			addprodtokc = true;
		}
		else {
			addprodtokc = false;
		}
		return addprodtokc;
	}		
	
	public String addProductToCart(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		Map<String, Object> randProd = pickRandomProduct(brand,campaign);
		String prodPPID = randProd.get("PPID").toString();		
		String prodName = randProd.get("DESCRIPTION").toString();
		System.out.println("Chosen Product : " + prodPPID + " " + prodName);
		select_cxt_offer(driver, randProd, realm);
		if(realm.equals("R4")) {
			if(!(brand.equalsIgnoreCase("SeaCalmSkin"))) {
				if(checkAddToKitOption(brand, campaign, prodPPID)) {
					driver.findElement(By.xpath("//li[contains(@class,'one-time ')]//span[@class='pdp-radio']//input")).click();
				}
			}				
		}			
		Thread.sleep(2000);
		
		List<Map<String, Object>> buynowloc = get_cxt_locator(realm, "BuyNow", "");		
		WebElement buynowelmt = comm_obj.find_webelement(driver, buynowloc.get(0).get("ELEMENTLOCATOR").toString(), buynowloc.get(0).get("ELEMENTVALUE").toString());
		buynowelmt.click();
		Thread.sleep(2000);
		String addtocartresult = "";
		if(realm.equalsIgnoreCase("R4")) {
			if(brand.equalsIgnoreCase("SeaCalmSkin")) {
				driver.findElement(By.xpath("//a[@class='mini-cart-link']")).click();
			}
			else {
				driver.findElement(By.xpath("//a[@class='button mini-cart-link-checkout small-12']")).click();
			}
			
			String addedproduct = driver.findElement(By.xpath("//div[@id='cart-table']//div")).getAttribute("class");
			System.out.println(addedproduct);
			
			if(addedproduct.contains(prodPPID)) {
				addtocartresult = "PASS";
			}
			else {
				addtocartresult = "FAIL";
			}
		}
		else {
			WebElement minicart = driver.findElement(By.xpath("(//div[@class='mini-cart-total']//a)[2]"));
			Actions act = new Actions(driver);
			act.moveToElement(minicart).perform();
			Thread.sleep(2000);	
			driver.findElement(By.xpath("(//a[contains(text(),'View Cart')])[2]")).click();
			String checkproductincart = driver.findElement(By.xpath("//div[@class='product-list-item']//div[@class='name']//a")).getAttribute("href");
			String cartproductok = "";
			if(checkproductincart.contains(prodPPID)) {
				cartproductok = "PASS";
			}
			else {
				cartproductok = "FAIL";
			}
			Thread.sleep(1000);
			driver.findElement(By.xpath("//button[contains(text(),'Proceed to checkout')]")).click();
			Thread.sleep(1000);
			String checkproductincheckout = driver.findElement(By.xpath("//div[@class='product-list-item']//div[@class='name']//a")).getAttribute("href");
			String checkoutproductok = "";
			if(checkproductincheckout.contains(prodPPID)) {
				checkoutproductok = "PASS";
			}
			else {
				checkoutproductok = "FAIL";
			}
			if((cartproductok.equals("PASS")) && (checkoutproductok.equals("PASS"))) {
				addtocartresult = "PASS";
			}
			else {
				addtocartresult = "FAIL";
			}
			driver.findElement(By.xpath("//a[@class='cxt-link-continue-shopping']")).click();
		}
		return addtocartresult;
	}
	
	public String removeProductfromCart(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		if(realm.equals("R4")) {
			driver.findElement(By.xpath("//div[@id='cart-table']//a[@class='removeproduct']")).click();
			Thread.sleep(3000);
			if(driver.findElements(By.xpath("//button[@class='button remove-product']")).size() != 0) {
				Thread.sleep(3000);
				driver.findElement(By.xpath("//button[@class='button remove-product']")).click();
			}					
			Thread.sleep(2000);
		}
		else {
			driver.findElement(By.xpath("(//button[@class='button-text delete-product cxt-button-link-layout'])[1]")).click();
		}
		
		Thread.sleep(3000);
		String rmfromcartresult = checkShoppingCartEmpty(driver, realm);
		return rmfromcartresult;
	}
	
	public void removeAllProductsfromCart(WebDriver driver, String brand, String campaign) throws InterruptedException, ClassNotFoundException, SQLException {
		Thread.sleep(1000);
		String realm = db_obj.get_realm(brand);
		
		List<Map<String, Object>> mccountloc = get_cxt_locator(realm, "Minicartcount", "");		
		WebElement mccountelmt = comm_obj.find_webelement(driver, mccountloc.get(0).get("ELEMENTLOCATOR").toString(), mccountloc.get(0).get("ELEMENTVALUE").toString());
		String prodcount = mccountelmt.getAttribute("class");
		if(!(prodcount.contains("mini-cart-empty"))) {
			List<Map<String, Object>> mcloc = get_cxt_locator(realm, "Minicart", "");		
			WebElement mcelmt = comm_obj.find_webelement(driver, mcloc.get(0).get("ELEMENTLOCATOR").toString(), mcloc.get(0).get("ELEMENTVALUE").toString());
			if(realm.equals("R4")) {
				mcelmt.click();
				while(driver.findElements(By.xpath("//a[@class='removeproduct']")).size() != 0) {
					if(driver.findElements(By.xpath("(//a[@class='removeproduct'])[1]")).size() != 0) {
						Thread.sleep(3000);
						driver.findElement(By.xpath("(//a[@class='removeproduct'])[1]")).click();
					}
					Thread.sleep(3000);
					if(driver.findElements(By.xpath("//button[@class='button remove-product']")).size() != 0) {
						Thread.sleep(3000);
						driver.findElement(By.xpath("//button[@class='button remove-product']")).click();
					}					
					Thread.sleep(2000);
				}
				checkShoppingCartEmpty(driver, realm);
			}
			else {
				Actions act = new Actions(driver);
				act.moveToElement(mcelmt).perform();
				Thread.sleep(2000);	
				driver.findElement(By.xpath("(//a[contains(text(),'View Cart')])[2]")).click();
				while(driver.findElements(By.xpath("//button[@class='button-text delete-product cxt-button-link-layout']")).size() != 0) {
					driver.findElement(By.xpath("(//button[@class='button-text delete-product cxt-button-link-layout'])[1]")).click();
					Thread.sleep(2000);
				}				
				checkShoppingCartEmpty(driver, realm);
				ShiftTabsCXT(driver, brand, campaign, "Shop");
			}
		}
	}
	
	public String checkShoppingCartEmpty(WebDriver driver, String realm) {
		String actual = "";
		String expected = "";
		if(realm.equals("R4")) {
			actual = driver.getTitle();
			expected = "Shop";
		}
		else {
			actual = driver.findElement(By.xpath("//div[@class='cart-empty']//h1")).getText();
			expected = "Your Shopping Cart Is Empty";
		}		
		System.out.println(actual);
		System.out.println(expected);
		String rmfromcartresult = "";
		if(actual.contains(expected)) {
			System.out.println("Shopping Cart is empty");
			rmfromcartresult = "PASS";
		}
		else {
			System.out.println("Shopping Cart is not empty");
			rmfromcartresult = "FAIL";
		}
		return rmfromcartresult;
	}
	
	public void addProductToKC(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		HashMap<String,Integer> kcproducts = checkMyNextKit(driver, realm);
						
		int temp = 1;
		while(temp == 1) 
		{
			boolean addtokc = validateProdLimitinKC(brand, campaign, getNumberofProductsinKC(driver, realm));
			if(addtokc) {			
				Map<String, Object> randProd = pickRandomProduct(brand,campaign);
				String prodPPID = randProd.get("PPID").toString();		
				String prodName = randProd.get("DESCRIPTION").toString();
				System.out.println("Chosen Product : " + prodPPID + " " + prodName);
				if(checkAddToKitOption(brand, campaign, prodPPID)) {
					System.out.println("Add To Kit Option checked - ok");
					if(kcproducts.containsKey(prodPPID)) {
						System.out.println("Chosen product already in KC");
						if(validateIndProdCountinKC(brand, campaign, prodPPID, kcproducts.get(prodPPID))) {
							System.out.println("validated individual product limit against KC - ok");
							confirmProductAddition(driver, randProd, realm);
							temp = 0;
							break;
						}
					}
					else {
						System.out.println("Chosen product not in KC");
						confirmProductAddition(driver, randProd, realm);
						temp = 0;
						break;
					}
				}
				else {
					temp = 1;
				}			
			}
			else {
				System.out.println("Product could not be added since Kit Customizer has maximum number of products.");
				System.out.println("Hence removing 1 product from KC and proceeding with Addition to KC");
				removeProductFromKC(driver, brand);
				temp = 1;
			}
		}
	}
	
	public void confirmProductAddition(WebDriver driver, Map<String, Object> cxtoffer, String realm) throws InterruptedException, ClassNotFoundException, SQLException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;				
		
		select_cxt_offer(driver, cxtoffer, realm);
		
		if(realm.equals("R4")) {
			driver.findElement(By.xpath("//button[@id='add-to-cart']/..//button[2]")).click();
			String display = "none";
			while(display.equals("none")) {
				// Wait until success text appears
				display = driver.findElement(By.xpath("//div[@id='itemAddedToKitPopup']")).getAttribute("style");
				if(display.contains("block")) {
					display = "block";
				}
				else {
					display = "none";
				}
			}		
			Thread.sleep(1000);
			driver.findElement(By.xpath("//div[@id='itemAddedToKitPopup']//div[@class='confirm-now-popup']//div//button[text()='OK ']")).click();
			Thread.sleep(1000);
		}
		else {
			driver.findElement(By.xpath("//button[@class='addBtn cxt-button secondary-button-small']")).click();
			Thread.sleep(1000);
			driver.findElement(By.xpath("//div[@id='confirmKitPopupAdd']//div//div//div[3]//div[2]//a//span")).click();
			while(driver.findElements(By.xpath("//div[@class='spinner_container']")).size() != 0) {
				// Wait until spinner disappears
			}
		}		
	}	
	
	public void select_cxt_offer(WebDriver driver, Map<String, Object> cxtoffer, String realm) throws ClassNotFoundException, SQLException, InterruptedException {
		String pagepattern = cxtoffer.get("PAGEPATTERN").toString();
		String[] patternarr = pagepattern.split("-");
		for(String pattern : patternarr) {
			switch(pattern){  
	    	case "product":
	    		select_cxt_product(driver, cxtoffer, realm);
	    		break; 
	    	case "fragrance":
	    		select_cxt_fragrance(driver, cxtoffer, realm);
	    		break;
			}
		}
	}
	
	public void select_cxt_product(WebDriver driver, Map<String, Object> cxtoffer, String realm) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;	
		String prodName = cxtoffer.get("DESCRIPTION").toString();
		String productLocator = "";		
		if(realm.equals("R4")) {
			if(prodName.contains("-2A")) {
				prodName = prodName.replace("-2A", "");
				productLocator = "//*[@class='product-name text-center']//a[contains(text(),'" + prodName + "')]";
				productLocator = "(" + productLocator + ")[2]";
			}
			else {
				productLocator = "//*[@class='product-name text-center']//a[contains(text(),'" + prodName + "')]";
			}
		}
		else {
			productLocator = "//div[@class='product-name']//h2//a[contains(text(),'" + prodName + "')]";
		}
		
		List<WebElement> prodelmts = driver.findElements(By.xpath(productLocator));							
		while(prodelmts.size() == 0){
			jse.executeScript("window.scrollBy(0,250)", 0);
			Thread.sleep(2000);
		}
		Thread.sleep(1000);
		WebElement product = driver.findElement(By.xpath(productLocator));
		product.click();
		Thread.sleep(2000);	
	}
	
	public void select_cxt_fragrance(WebDriver driver, Map<String, Object> cxtoffer, String realm) throws ClassNotFoundException, SQLException, InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;	
		String brand = cxtoffer.get("BRAND").toString();
		String fragrance = cxtoffer.get("FRAGRANCE").toString();
		String ppid = cxtoffer.get("PPID").toString();
		
		String fragLocator = "";		
		if(realm.equals("R4")) {
			fragLocator = "//li[@data-variantid='" + ppid + "']//a";
		}
		else {
			if(brand.equalsIgnoreCase("ITCosmetics")) {
				fragLocator = "//li[@data-shade='" + ppid + "']";
			}
		}
		WebElement product = driver.findElement(By.xpath(fragLocator));
		product.click();
		Thread.sleep(2000);	
	}
	
	public void removeProductFromKC(WebDriver driver, String brand) throws InterruptedException, ClassNotFoundException, SQLException {		
		String realm = db_obj.get_realm(brand);
		openMyNextKit(driver, realm);
		
		List<Map<String, Object>> kcloc = get_cxt_locator(realm, "KCLocator", "");		
		List<WebElement> kcproducts= comm_obj.find_mulwebelement(driver, kcloc.get(0).get("ELEMENTLOCATOR").toString(), kcloc.get(0).get("ELEMENTVALUE").toString());		
		int index =  kcproducts.size();
		
		if(realm.equals("R4")) {
			WebElement prodRemove = driver.findElement(By.xpath("(//div[@class='kitimages']//div[@class='kitimages-section']//a//img)[" + index + "]"));
			Actions act = new Actions(driver);
			act.moveToElement(prodRemove).perform();
			Thread.sleep(2000);		
			driver.findElement(By.xpath("(//a[@class='button hollow remove-kitproduct removeBtn'])[" + index + "]")).click();
			driver.findElement(By.xpath("//div[@id='confirmKitPopupRemove']//div[@class='remove-product-popup']//div//button[text()='Confirm ']")).click();
			Thread.sleep(25000);
		}
		else {
			WebElement prodRemove = driver.findElement(By.xpath("(//div[@class='imageWrapper'])[" + index + "]"));
			Actions act = new Actions(driver);
			act.moveToElement(prodRemove).perform();
			Thread.sleep(2000);
			driver.findElement(By.xpath("//div[@class='imageWrapper']//div[3]//div[2]//div[6]//a//span")).click();
			driver.findElement(By.xpath("//div[@id='confirmKitPopupRemove']//div//div//div[3]//div[2]//a//span")).click();
			while(driver.findElements(By.xpath("//div[@class='spinner_container']")).size() != 0) {
				// Wait until spinner disappears
			}
		}
	}
	
	public String rescheduleShipment(WebDriver driver, String brand, String expecteddate, Calendar now) throws ClassNotFoundException, SQLException, InterruptedException, ParseException {
		String rescheduleresult = "PASS";
		
		String realm = db_obj.get_realm(brand);
		
		List<Map<String, Object>> reschedulebuttonloc = get_cxt_locator(realm, "RescheduleButton", "");
		List<Map<String, Object>> datepickerloc = get_cxt_locator(realm, "OpenDatePicker", "");
		List<Map<String, Object>> confirmloc = get_cxt_locator(realm, "ConfirmReschedule", "");
		
		WebElement reschedulebuttonelmt = comm_obj.find_webelement(driver, reschedulebuttonloc.get(0).get("ELEMENTLOCATOR").toString(), reschedulebuttonloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		reschedulebuttonelmt.click();
		Thread.sleep(1000);
		
		WebElement datepickerelmt = comm_obj.find_webelement(driver, datepickerloc.get(0).get("ELEMENTLOCATOR").toString(), datepickerloc.get(0).get("ELEMENTVALUE").toString());
		datepickerelmt.click();
		Thread.sleep(1000);
		
		rescheduleresult = setDate(driver, now);	
		
		WebElement confirmelmt = comm_obj.find_webelement(driver, confirmloc.get(0).get("ELEMENTLOCATOR").toString(), confirmloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		confirmelmt.click();
		Thread.sleep(1000);
		System.out.println(rescheduleresult);
		if(rescheduleresult.equals("PASS")) {			
			
			if(realm.equals("R4")) {
				String display = "block";
				while(display.equals("block")) {
					// Wait until spinner disappears
					display = driver.findElement(By.xpath("//img[@alt='Loading']/../..")).getAttribute("style");
					if(display.contains("block")) {
						display = "block";
					}
					else {
						display = "none";
					}
				}
			}
			else {
				while(driver.findElements(By.xpath("//div[@class='spinner_container']")).size() != 0) {
					// Wait until spinner disappears
				}
			}			
			Thread.sleep(2000);
			List<Map<String, Object>> actualdateloc = get_cxt_locator(realm, "RescheduledDate", "");		
			WebElement actualdateelmt = comm_obj.find_webelement(driver, actualdateloc.get(0).get("ELEMENTLOCATOR").toString(), actualdateloc.get(0).get("ELEMENTVALUE").toString());
			Thread.sleep(1000);
			String actualdate = actualdateelmt.getText();
			Thread.sleep(1000);
			System.out.println("Actual Date : " + actualdate);
			System.out.println("Expected Date : " + expecteddate);
			rescheduleresult = actualdate;
		}	
		return rescheduleresult;
	}
	
	public String setDate(WebDriver driver, Calendar now) throws ParseException, InterruptedException {
		String setdateresult = "PASS";
		String reschedule_month = new SimpleDateFormat("MMMM").format(now.getTime());
		String reschedule_year = Integer.toString(now.get(Calendar.YEAR));
		String reschedule_day = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
		
		String datepicker_month = driver.findElement(By.xpath("//span[@class='ui-datepicker-month']")).getText();
		String datepicker_year = driver.findElement(By.xpath("//span[@class='ui-datepicker-year']")).getText();
		
		setdateresult = changeMonth(driver, now, reschedule_month, reschedule_year, datepicker_month, datepicker_year);
		Thread.sleep(1000);
		int dateset = 0;
		if(setdateresult.equals("PASS")) {
			List<WebElement> weeks = driver.findElements(By.xpath("//table[@class='ui-datepicker-calendar']//tbody//tr"));
			for(int i=1; i<=weeks.size(); i++) {
				List<WebElement> days = driver.findElements(By.xpath("//table[@class='ui-datepicker-calendar']//tbody//tr[" + i + "]//td"));
				for(int j=1; j<=days.size(); j++) {				
					if(driver.findElements(By.xpath("//table[@class='ui-datepicker-calendar']//tbody//tr[" + i + "]//td[" + j + "]//a")).size() != 0) {
						WebElement dayelmt = driver.findElement(By.xpath("//table[@class='ui-datepicker-calendar']//tbody//tr[" + i + "]//td[" + j + "]//a"));
						String dpday = dayelmt.getText();
						if(dpday.equals(reschedule_day)) {
							dayelmt.click();
							dateset=1;
							break;
						}
						else {
							continue;
						}
					}				
				}
				if(dateset == 1) {
					break;
				}
			}
		}
		return setdateresult;
	}
	
	public String changeMonth(WebDriver driver, Calendar now, String reschedule_month, String reschedule_year, String datepicker_month, String datepicker_year) throws ParseException {
		String changemonthresult = "PASS";
		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");		
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12
		
		// Change Datepicker month and year to Integer format
		now.setTime(inputFormat.parse(datepicker_month));
		String dpmonthnumstr = outputFormat.format(now.getTime());
		int dpmonth = Integer.parseInt(dpmonthnumstr);
		int dpyear = Integer.parseInt(datepicker_year);
		
		// Change Reschedule month and year to Integer format
		now.setTime(inputFormat.parse(reschedule_month));
		String remonthnumstr = outputFormat.format(now.getTime());
		int remonth = Integer.parseInt(remonthnumstr);
		int reyear = Integer.parseInt(reschedule_year);
		
		if(dpyear == reyear) {
			if(dpmonth == remonth) {
			}
			else if(dpmonth>remonth) {
				int diff = dpmonth-remonth;
				while(diff>0) {
					driver.findElement(By.xpath("//a[@data-handler='prev']//span")).click();
					diff--;
				}
			}
			else if(remonth>dpmonth) {
				int diff = remonth-dpmonth;
				while(diff>0) {
					if(driver.findElements(By.xpath("//a[@data-handler='next']//span")).size() != 0) {
						driver.findElement(By.xpath("//a[@data-handler='next']//span")).click();
						diff--;
					}
					else {
						changemonthresult = "FAIL";
						driver.findElement(By.xpath("(//div[contains(text(),'Reschedule Next Shipment')])[2]")).click();
						diff--;
					}
				}
			}
		}
		else {
			if(reyear>dpyear) {
				driver.findElement(By.xpath("//a[@data-handler='next']//span")).click();
			}
			else if(dpyear>reyear) {
				driver.findElement(By.xpath("//a[@data-handler='prev']//span")).click();
			}
		}
		return changemonthresult;
	}
	
	public String getPageTitle(WebDriver driver) {
		String actualTitle = driver.getTitle();
		String[] arr = actualTitle.split(" ");		
		actualTitle = arr[0];
		return actualTitle;
	}
	
	public void LoginintoCXT(WebDriver driver, String brand, String campaign, String env) throws ClassNotFoundException, SQLException, InterruptedException {
		String getaccountquery = "select * from cxt_accounts where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> cxt_credentials = DBLibrary.dbAction("fetch", getaccountquery);
		
		String username = cxt_credentials.get(0).get(env.toUpperCase() + "_USERNAME").toString();
		String password = cxt_credentials.get(0).get(env.toUpperCase() + "_PASSWORD").toString();
		
		String realm = db_obj.get_realm(brand);
		
		List<Map<String, Object>> unloc = get_cxt_locator(realm, "Login", "Username");
		List<Map<String, Object>> pwdloc = get_cxt_locator(realm, "Login", "Password");
		List<Map<String, Object>> signinloc = get_cxt_locator(realm, "Login", "SignInButton");
		
		WebElement unelmt = comm_obj.find_webelement(driver, unloc.get(0).get("ELEMENTLOCATOR").toString(), unloc.get(0).get("ELEMENTVALUE").toString());
		unelmt.sendKeys(username);
		WebElement pwdelmt = comm_obj.find_webelement(driver, pwdloc.get(0).get("ELEMENTLOCATOR").toString(), pwdloc.get(0).get("ELEMENTVALUE").toString());
		pwdelmt.sendKeys(password);
		
		WebElement signinelmt = comm_obj.find_webelement(driver, signinloc.get(0).get("ELEMENTLOCATOR").toString(), signinloc.get(0).get("ELEMENTVALUE").toString());
		Thread.sleep(1000);
		signinelmt.click();		
		
		Thread.sleep(1000);
	}
	
	public void accessLoggedInProfile(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		List<Map<String, Object>> profileloc = get_cxt_locator(realm, "LoggedinProfile", "");
		WebElement profileelmt = comm_obj.find_webelement(driver, profileloc.get(0).get("ELEMENTLOCATOR").toString(), profileloc.get(0).get("ELEMENTVALUE").toString());
		Actions act = new Actions(driver);
		act.moveToElement(profileelmt).perform();
		Thread.sleep(2000);
	}
	
	public void moveToMyNextKit(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		List<Map<String, Object>> mynextkitloc = get_cxt_locator(realm, "Menu", "MyNextKit");		
		accessLoggedInProfile(driver, brand, campaign);
		WebElement mynextkitelmt = comm_obj.find_webelement(driver, mynextkitloc.get(0).get("ELEMENTLOCATOR").toString(), mynextkitloc.get(0).get("ELEMENTVALUE").toString());
		mynextkitelmt.click();			
		Thread.sleep(2000);
	}
	
	public void ShiftTabsCXT(WebDriver driver, String brand, String campaign, String tab) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		List<Map<String, Object>> tabloc = get_cxt_locator(realm, "Menu", tab);		
		for(Map<String,Object> loc : tabloc) {
			
			String elementvalue = loc.get("ELEMENTVALUE").toString();
			if(driver.findElements(By.xpath(elementvalue)).size() != 0) {
				Thread.sleep(10000);
				driver.findElement(By.xpath(elementvalue)).click();
				break;
			}
		}			
		Thread.sleep(2000);
	}
	
	public void LogoutCXT(WebDriver driver, String brand, String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String realm = db_obj.get_realm(brand);
		List<Map<String, Object>> logoutloc = get_cxt_locator(realm, "Logout", "");		
		accessLoggedInProfile(driver, brand, campaign);
		WebElement logoutelmt = comm_obj.find_webelement(driver, logoutloc.get(0).get("ELEMENTLOCATOR").toString(), logoutloc.get(0).get("ELEMENTVALUE").toString());
		logoutelmt.click();			
		Thread.sleep(2000);
	}
	
	public List<Map<String, Object>> get_cxt_locator(String realm, String step, String offer) throws ClassNotFoundException, SQLException {
		String query = "select * from cxt_locators where ";
		String include_realm = "realm='" + realm + "'";
		String include_step = "step='" + step + "'";
		String include_offer = "offer='" + offer + "'";
			
		if(realm != null) {
			query = query + include_realm;
			if((step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		if(step != null) {
			query = query + include_step;
			if(offer != null) {
				query = query + " and ";
			}
		}
		if(offer != null) {
			query = query + include_offer;
		}
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch",query);
		return locator;		
	}
}
