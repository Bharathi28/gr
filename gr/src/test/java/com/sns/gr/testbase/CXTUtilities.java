package com.sns.gr.testbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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
		
		String filename = "";
		if(result.equalsIgnoreCase("Success")) {
			filename = System.getProperty("user.dir") + "\\Input_Output\\CXTValidation\\Screenshots\\" + brand + "\\" + step + "_" + dateStr +".png";
		}
		else {
			filename = System.getProperty("user.dir") + "\\Input_Output\\CXTValidation\\Run_Output\\" + brand + "_" + step + "_" + dateStr +".png";
		}
		
		Screenshot ss = null;
		if(part.equalsIgnoreCase("visiblepart")) {
			ss = new AShot().takeScreenshot(driver);
		}
		else if(part.equalsIgnoreCase("fullpage")) {
			ss = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(ShootingStrategies.scaling(1.25f), 1000)).takeScreenshot(driver);
		}
		ImageIO.write(ss.getImage(),"PNG",new File(filename));
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
		String query = "select * from cxt_offers where brand='" + brand + "' and campaign='" + campaign + "'";
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
			driver.findElement(By.xpath("//div[@id='confirmAddToKitPopup']//div[@class='confirm-now-popup']//div//button[text()='Confirm ']")).click();
		}
		else {
			driver.findElement(By.xpath("//button[@class='addBtn cxt-button secondary-button-small']")).click();
			driver.findElement(By.xpath("//div[@id='confirmKitPopupAdd']//div//div//div[3]//div[2]//a//span")).click();
		}		
		Thread.sleep(25000);
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
			productLocator = "//*[@class='product-name text-center']//a[contains(text(),'" + prodName + "')]";
		}
		else {
			productLocator = "//div[@class='product-name']//h2//a[contains(text(),'" + prodName + "')]";
		}
		
		List<WebElement> prodelmts = driver.findElements(By.xpath(productLocator));							
		while(prodelmts.size() == 0){
			jse.executeScript("window.scrollBy(0,500)", 0);
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
		System.out.println("Index: " + index);
		
		if(realm.equals("R4")) {
			WebElement prodRemove = driver.findElement(By.xpath("(//div[@class='kitimages']//div[@class='kitimages-section']//a//img)[" + index + "]"));
			Actions act = new Actions(driver);
			act.moveToElement(prodRemove).perform();
			Thread.sleep(2000);		
			driver.findElement(By.xpath("(//a[@class='button hollow remove-kitproduct removeBtn'])[" + index + "]")).click();
			driver.findElement(By.xpath("//div[@id='confirmKitPopupRemove']//div[@class='remove-product-popup']//div//button[text()='Confirm ']")).click();
		}
		else {
			WebElement prodRemove = driver.findElement(By.xpath("(//div[@class='imageWrapper'])[" + index + "]"));
			Actions act = new Actions(driver);
			act.moveToElement(prodRemove).perform();
			Thread.sleep(2000);
			driver.findElement(By.xpath("//div[@class='imageWrapper']//div[3]//div[2]//div[6]//a//span")).click();
			driver.findElement(By.xpath("//div[@id='confirmKitPopupRemove']//div//div//div[3]//div[2]//a//span")).click();
		}
		Thread.sleep(25000);
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
		signinelmt.click();		
		
		Thread.sleep(2000);
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
			System.out.println(query);
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch",query);
		return locator;		
	}
}
