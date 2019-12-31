package com.sns.gr.testbase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.sns.gr.testbase.DBLibrary;
import com.sns.gr.setup.BaseTest;
import com.sns.gr.testbase.BuyflowUtilities;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class PixelUtilities {
	
	DBUtilities db_obj = new DBUtilities();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	
	public String getUci(String pixel) throws ClassNotFoundException, SQLException {
		String[] cake_uci= {"-KE-","-O-DI-RT-","-O-PS-BR-","-O-PD-RT-"};
		String[] linkshare_uci= {"-O-AF-","-O-PM-IN-KE-","-O-PM-SP-KE-","-O-PM-PS-KE-"};
		int rnd;
		String uci = null;
		if(pixel.equalsIgnoreCase("Cake")) {
			rnd = new Random().nextInt(cake_uci.length);
			uci = cake_uci[rnd];
		}
		else if(pixel.equalsIgnoreCase("Linkshare")) {
			rnd = new Random().nextInt(linkshare_uci.length);
			uci = linkshare_uci[rnd];
		}
	    return uci;
	}

	public Har defineNewHar(BrowserMobProxy proxy, String name) {
		return proxy.newHar(name);
	}
	
	public void getHarData(BrowserMobProxy proxy, String filename) {
		// get the HAR data
	    Har har = proxy.getHar();
	    
        try {
			har.writeTo(new File(filename));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public List<List<String>> generateTestRuns(DesiredCapabilities capabilities, BrowserMobProxy proxy, String env, String brand, String campaign, List<String> pixellist, int runs, String url) throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		String query = "";
		if((brand.equalsIgnoreCase("smileactives")) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("TryCrepeErase")) || (brand.equalsIgnoreCase("Volaire")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("WestmoreBeauty"))) {
			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' order by RAND() limit " + runs;
		}
		else if(brand.equalsIgnoreCase("SeaCalmSkin")) {
			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Product' order by RAND() limit " + runs;
		}
		else {
			query = "select * from r2offers where brand='" + brand + "' and campaign='" + campaign + "' and category='kit' order by RAND() limit " + runs;
		}				
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);		
		int i=0;
		String lastChar = url.substring(url.length() - 1);
		String joinChar;
		if(url.contains("=")) {
			joinChar = "&";
		}
		else {
			if(lastChar.equalsIgnoreCase("/")) {
				joinChar = "?";
			}
			else {
				joinChar = "/?";
			}
		}		
		List<List<String>> overallOutput = new ArrayList<List<String>>();
		List<String> buyflowOutput = new ArrayList<String>();
		for(String pixel : pixellist) {
			if(pixel.equalsIgnoreCase("cake")) {
				String uci = getUci("Cake");
				String uciurl = url + joinChar + "UCI=" + uci;
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, uciurl,"Cake", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("harmonyconversiontracking")) {		
				String appendurl = url + joinChar + "hConversionEventId=AQEAAZQF2gAmdjQwMDAwMDE2OS0zYmI0LTM2ZTMtYTIyNy0yNjZlOTY2Mzk4MTjaACRlM2U1MzMxYi00ZTIxLTQ5YzgtMDAwMC0wMjFlZjNhMGJjYzPaACRmOTkyNWRkZi1lMzA0LTQ0ZjEtOTJmOC1mMTUyM2VlOTVkZjKFXOX1ZlAb6-YsLP1N4nV5ZwzJa4oaNWsQ9iHh0H1Pdg";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, appendurl,"HarmonyConversionTracking", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("linkshare")) {		
				String uci = getUci("Linkshare");
				String uciurl = url + joinChar + "UCI=" + uci;
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, uciurl,"Linkshare", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("starmobile")) {	
				String appendurl = url + joinChar + "sessionid=hello12345";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, appendurl,"StarMobile", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("data+math")) {	
				String appendurl = url + joinChar + "uci=hellohi";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, appendurl,"Data+Math", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("propelmedia")) {	
				String appendurl = "";
				if(brand.toLowerCase().contains("crepeerase")) {
					appendurl  = url + joinChar + "variabletoken=cstoken" ;
				}
				if(brand.equalsIgnoreCase("Smileactives")) {
					appendurl  = url + joinChar + "variabletoken=saagaintoken" ;
				}
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, appendurl,"PropelMedia", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			i++;
		}
		if(runs > i) {
			buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, url,"Default", offerdata.get(i));
			overallOutput.add(buyflowOutput);
		}
		return overallOutput;
	}		
	
	public List<String> generateHARFiles(DesiredCapabilities capabilities, BrowserMobProxy proxy, String env, String brand, String campaign, String url, String pixel, Map<String, Object> offerdata) throws ClassNotFoundException, SQLException, InterruptedException, IOException {

		// start the browser up
	    @SuppressWarnings("deprecation")
	    
	    WebDriver driver = new ChromeDriver(capabilities);	    
//	    WebDriver driver = new RemoteWebDriver(new URL("http://192.168.0.22:4444/wd/hub"), capabilities);
	    driver.manage().window().maximize();

	    JavascriptExecutor jse = (JavascriptExecutor) driver;
	    
	    // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
	    proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
	    
	    String pattern = "";
	    if(url.contains("UCI")) {
	    	if(pixel.toLowerCase().contains("cake")) {
	    		pattern = "Cake";
	    	}
	    	if(pixel.toLowerCase().contains("linkshare")) {
	    		pattern = "Linkshare";
	    	}
	    }
	    else if(url.contains("hConversionEventId")) {
	    	pattern = "HarmonyConversionTracking";
	    }
	    else if(url.contains("sessionid")) {
	    	pattern = "StarMobile";
	    }
	    else if(url.contains("uci=hellohi")) {
	    	pattern = "Data+Math";
	    }
	    else if(url.contains("variabletoken")) {
	    	pattern = "PropelMedia";
	    }   
	    	    
	    ///////////////////////////////////////////////////////////		    
	    // Home Page
		if((!((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("mywbfeb19")))) && (!((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("order30fsh2b"))))){
			defineNewHar(proxy, brand + "HomePage");		 
		    driver.get(url);	    
		    if(driver.findElements(By.xpath("//div[@id='holiday-popup-content']")).size() != 0) {
				driver.navigate().refresh();
			}
		    Thread.sleep(10000);
		    getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_homepage" + pattern + ".har");
		}	    
	    
	    //////////////////////////////////////////////////////////
        // SAS Page
	    defineNewHar(proxy, brand + "SASPage");
	        
	    // Navigate to SAS Page
	    if(((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("mywbfeb19"))) || ((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("order30fsh2b")))){
	    	driver.get(url);	    
		    Thread.sleep(10000);
	    }
	    	
	    else if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
	        bf_obj.click_cta(driver, env, brand, campaign, "Kit");
	        sas_obj.select_kit(driver, offerdata, brand, campaign);
	    }
	    else if(brand.equalsIgnoreCase("SeaCalmSkin")) {
	        driver.findElement(By.xpath("//a[@href='/shop']")).click();
	        sas_obj.select_offer(driver, env, brand, campaign, offerdata);
	        driver.findElement(By.xpath("//button[@id='add-to-cart']")).click();
	    }
	    else {
	    	 bf_obj.click_cta(driver, env, brand, campaign, "Kit");
	    }
	        
	    Thread.sleep(10000);
//	    wait.until(pageLoadCondition);
	    getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_saspage" + pattern + ".har");
	    ////////////////////////////////////////////////////////////  
        // Checkout Page	        
        defineNewHar(proxy, brand + "CheckoutPage");
        if(brand.equalsIgnoreCase("SeaCalmSkin")) {
        	Thread.sleep(2000);
        	driver.findElement(By.xpath("(//a[@href='https://seacalmskin.grdev.com/checkout'])[1]")).click();
        }
        else if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
        	sas_obj.select_kitshade(driver, offerdata, brand, campaign);
        	sas_obj.select_duo(driver, offerdata, brand, campaign);
        }
        else {
        	sas_obj.select_offer(driver, env, brand, campaign, offerdata);
        }
        bf_obj.move_to_checkout(driver, brand, campaign, offerdata.get("ppid").toString(), 0);
                        
        Thread.sleep(4000);
        if(driver.findElements(By.xpath("//a[@id='creditCardPath']")).size() != 0) {
        	if(driver.findElement(By.xpath("//a[@id='creditCardPath']")).isDisplayed()){
        		jse.executeScript("window.scrollBy(0,-150)", 0);
        		driver.findElement(By.xpath("//a[@id='creditCardPath']")).click();
        	}
		}	
        String email = bf_obj.fill_out_form(driver, brand, "VISA", "Same", "30");
        System.out.println("Email : " + email);
        Thread.sleep(2000);
        getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_checkoutpage" + pattern + ".har");
        
		jse.executeScript("window.scrollBy(0,-200)", 0);
		
		String checkout_subtotal = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Subtotal");
		String checkout_shipping = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Shipping");
		String checkout_salestax = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Salestax");
		String checkout_total = pr_obj.fetch_pricing (driver, env, brand, campaign, "Checkout Total");
		
		String checkout_pricing = checkout_subtotal + " ; " + checkout_shipping + " ; " + checkout_salestax + " ; " + checkout_total;
        
        //////////////////////////////////////////////////////////     
       
		// Upsell and Confirmation Page Navigation
		String ppu = db_obj.checkPPUPresent(brand, campaign);
		if(ppu.equalsIgnoreCase("No")) {
			defineNewHar(proxy, brand + "ConfirmationPage");
        	// Navigate to Confirmation Page	        
        	bf_obj.complete_order(driver, brand);          
            Thread.sleep(10000);
            getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_confirmationpage" + pattern + ".har");	
		}
		else {
			// Upsell Page
            defineNewHar(proxy, brand + "UpsellPage");
            // Navigate to Upsell Page	        
            bf_obj.complete_order(driver, brand);
            Thread.sleep(20000);
            getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_upsellpage" + pattern + ".har");
            
            //////////////////////////////////////////////////////////
            // Confirmation Page	        
            defineNewHar(proxy, brand + "ConfirmationPage");
            
            String upsell_value = "";
   			upsell_value = offerdata.get("upgrade").toString();
    	    		
            // Navigate to Confirmation Page
            bf_obj.upsell_confirmation(driver, brand, campaign, upsell_value);
            Thread.sleep(20000);
            getHarData(proxy, "F:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_confirmationpage" + pattern + ".har");
            Thread.sleep(3000);
		}        
	
		String expected_ppid = offerdata.get("ppid").toString();
		System.out.println("Expcted PPID : " + expected_ppid);
		
        String actual_conf_ppid = bf_obj.fetch_confoffercode(driver, brand, false);
		System.out.println("Actual PPID : " + actual_conf_ppid);
        
        String conf_num = bf_obj.fetch_conf_num(driver, brand);
        System.out.println("Confirmation Number : " + conf_num);        
		
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
		output_row.add(actual_conf_ppid);
		output_row.add(conf_num);
		output_row.add("Yes");
		output_row.add(checkout_pricing);
		output_row.add(conf_pricing);		
//		output.add(output_row);
		
//		comm_obj.write_output(output, brand, campaign, "Pixel_BuyflowResults", "F:\\Automation\\Buyflow\\DailyOrders\\Run Output\\");	
//		driver.close();
		
        // Save Order Screenshots        
        Screenshot confpage = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);			
        ImageIO.write(confpage.getImage(),"PNG",new File("F:\\Automation\\Pixel\\Screenshots\\" + brand + "\\" + offerdata.get("ppid").toString() +".png"));	
        
        driver.close();
        return output_row;
	}
}
