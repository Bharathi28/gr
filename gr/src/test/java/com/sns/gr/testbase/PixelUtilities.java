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
import org.openqa.selenium.chrome.ChromeOptions;
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
	
	public List<List<String>> generateTestRuns(DesiredCapabilities capabilities, BrowserMobProxy proxy, String env, String brand, String campaign, String flow, List<String> pixellist, int runs, String url) throws ClassNotFoundException, SQLException, InterruptedException, IOException {
		String origcampaign = campaign;
		String tempcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(tempcampaign.equals("n/a"))){
			campaign = tempcampaign;
		}
		
		String query = "";
//		if((brand.equalsIgnoreCase("SeaCalmSkin")) || (brand.equalsIgnoreCase("FixMDSkin")) || (brand.equalsIgnoreCase("smileactives")) || ((brand.equalsIgnoreCase("SeaCalmSkin")) && (campaign.equalsIgnoreCase("specialoffer"))) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("TryCrepeErase")) || (brand.equalsIgnoreCase("Volaire")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("WestmoreBeauty"))) {
//			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active' order by RAND() limit " + runs;
//		}
//		else {
//			query = "select * from r2offers where brand='" + brand + "' and campaign='" + campaign + "' and category='kit' and status='Active' order by RAND() limit " + runs;
//		}		
		if((brand.equalsIgnoreCase("SeaCalmSkin")) || (brand.equalsIgnoreCase("FixMDSkin")) || (brand.equalsIgnoreCase("Smileactives")) || ((brand.equalsIgnoreCase("SeaCalmSkin")) && (campaign.equalsIgnoreCase("specialoffer"))) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("Volaire")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("WestmoreBeauty"))) {
			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}
		else {
			query = "select * from r2offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}	
		List<Map<String, Object>> offers = DBLibrary.dbAction("fetch", query);
		
		campaign = origcampaign;
		List<Map<String, Object>> offerdata = new ArrayList<Map<String, Object>>();
		Random rand = new Random(); 
 
		for(int i=0; i<runs; i++) {
			offerdata.add(offers.get(rand.nextInt(offers.size())));
		}		
		
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
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, uciurl,"Cake", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("harmonyconversiontracking")) {		
				String appendurl = url + joinChar + "hConversionEventId=AQEAAZQF2gAmdjQwMDAwMDE2OS0zYmI0LTM2ZTMtYTIyNy0yNjZlOTY2Mzk4MTjaACRlM2U1MzMxYi00ZTIxLTQ5YzgtMDAwMC0wMjFlZjNhMGJjYzPaACRmOTkyNWRkZi1lMzA0LTQ0ZjEtOTJmOC1mMTUyM2VlOTVkZjKFXOX1ZlAb6-YsLP1N4nV5ZwzJa4oaNWsQ9iHh0H1Pdg";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, appendurl,"HarmonyConversionTracking", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("linkshare")) {		
				String uci = getUci("Linkshare");
				String uciurl = url + joinChar + "UCI=" + uci;
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, uciurl,"Linkshare", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("starmobile")) {	
				String appendurl = url + joinChar + "sessionid=hello12345";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, appendurl,"StarMobile", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			if(pixel.equalsIgnoreCase("data+math")) {	
				String appendurl = url + joinChar + "uci=hellohi";
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, appendurl,"Data+Math", offerdata.get(i));
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
				buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, appendurl,"PropelMedia", offerdata.get(i));
				overallOutput.add(buyflowOutput);
			}
			i++;
		}
		if(runs > i) {			
			buyflowOutput = generateHARFiles(capabilities, proxy, env, brand, campaign, flow, url,"Default", offerdata.get(i));
			overallOutput.add(buyflowOutput);
		}
		return overallOutput;
	}		
	
	public List<String> generateHARFiles(DesiredCapabilities capabilities, BrowserMobProxy proxy, String env, String brand, String campaign, String flow, String url, String pixel, Map<String, Object> offerdata) throws ClassNotFoundException, SQLException, InterruptedException, IOException {

		// start the browser up
	    @SuppressWarnings("deprecation")
	    
//	    WebDriver driver = new FirefoxDriver(capabilities);
	    WebDriver driver = new ChromeDriver(capabilities);	    
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
	    	    
	    String origcampaign = campaign;
		String tempcampaign = comm_obj.campaign_repeat(brand, campaign, "locators");
		if(!(tempcampaign.equals("n/a"))){
			campaign = tempcampaign;
		}
		
	    ///////////////////////////////////////////////////////////		    
	    // Home Page
		if((!((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("mywbfeb19")))) && (!((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("order30fsh2b"))))){
			defineNewHar(proxy, brand + "HomePage");		 
		    driver.get(url);	   
		    
		    if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
				driver.findElement(By.xpath("//button[@id='details-button']")).click();
				driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
			}
		    else if(driver.findElements(By.xpath("//button[@id='advancedButton']")).size() != 0) {
		    	driver.findElement(By.xpath("//button[@id='advancedButton']")).click();
		    	driver.findElement(By.xpath("//button[@id='exceptionDialogButton']")).click();
		    }
		    
		    if(driver.findElements(By.xpath("//div[@id='holiday-popup-content']")).size() != 0) {
				driver.navigate().refresh();
			}
		    Thread.sleep(10000);
		    getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_homepage_" + pattern + "_" + flow +".har");
		}	    
	    
	    //////////////////////////////////////////////////////////
        // SAS Page
	    defineNewHar(proxy, brand + "SASPage");
	        
	    
		
	    // Navigate to SAS Page
	    if(((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("mywbfeb19"))) || ((brand.equalsIgnoreCase("CrepeErase")) && (campaign.equalsIgnoreCase("order30fsh2b")))){
	    	driver.get(url);	    
	    	
	    	if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
				driver.findElement(By.xpath("//button[@id='details-button']")).click();
				driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
			} 
	    	
		    Thread.sleep(10000);
	    }	    	
	    else if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
	        bf_obj.click_cta(driver, env, brand, origcampaign, "Kit");
	        sas_obj.select_kit(driver, offerdata, brand, campaign);
	    }
	    else if((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("Core"))) {
	    	bf_obj.click_cta(driver, env, brand, origcampaign, "Kit");
	    	sas_obj.select_kit(driver, offerdata, brand, campaign);
	    }
	    else {
	    	 bf_obj.click_cta(driver, env, brand, origcampaign, "Kit");
	    }
	        
	    Thread.sleep(10000);
//	    wait.until(pageLoadCondition);
	    getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_saspage_" + pattern + "_" + flow +".har");
	    ////////////////////////////////////////////////////////////  
        // Checkout Page	        
        defineNewHar(proxy, brand + "CheckoutPage");
        if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
        	sas_obj.select_kitshade(driver, offerdata, brand, campaign);
        	sas_obj.select_duo(driver, offerdata, brand, campaign);
        }
        else if((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("Core"))) {
        }
        else {
        	sas_obj.select_offer(driver, env, brand, campaign, offerdata, "Kit", 0);
        }
        if(!((brand.equalsIgnoreCase("crepeerase"))&&campaign.equalsIgnoreCase("core"))) {
        	bf_obj.move_to_checkout(driver, brand, campaign, offerdata.get("PPID").toString(), "Kit");
        }        
        String email = "";
                
        if(flow.equalsIgnoreCase("CCFlow")) {
        	Thread.sleep(4000);
            if(driver.findElements(By.xpath("//a[@id='creditCardPath']")).size() != 0) {
            	if(driver.findElement(By.xpath("//a[@id='creditCardPath']")).isDisplayed()){
            		jse.executeScript("window.scrollBy(0,-150)", 0);
            		driver.findElement(By.xpath("//a[@id='creditCardPath']")).click();
            	}
    		}	
            email = bf_obj.fill_out_form(driver, brand, campaign, "VISA", "Same", "30");
            System.out.println("Email : " + email);
            Thread.sleep(2000);
            getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_checkoutpage_" + pattern + "_" + flow +".har");
        }
        else if(flow.equalsIgnoreCase("PaypalFlow")) {
        	getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_checkoutpage_" + pattern + "_" + flow +".har");
        	defineNewHar(proxy, brand + "PaypalReviewPage");
        	email = bf_obj.fill_out_form(driver, brand, campaign, "Paypal", "Same", "30");
            System.out.println("Email : " + email);
            Thread.sleep(2000);
            getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_paypalreviewpage_" + pattern + "_" + flow +".har");
        }
        
        
        //////////////////////////////////////////////////////////     
       
		// Upsell and Confirmation Page Navigation
		String ppu = db_obj.checkPPUPresent(brand, campaign, "Kit");
		String cc = "";
		if(flow.equalsIgnoreCase("CCFlow")) {
			cc = "VISA";
		}
		else {
			cc = "Paypal";
		}
		if(ppu.equalsIgnoreCase("No")) {
			defineNewHar(proxy, brand + "ConfirmationPage");
        	// Navigate to Confirmation Page	        
        	bf_obj.complete_order(driver, brand, cc);          
            Thread.sleep(10000);
            getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_confirmationpage_" + pattern + "_" + flow +".har");
		}
		else {
			// Upsell Page
            defineNewHar(proxy, brand + "UpsellPage");
            // Navigate to Upsell Page	        
            bf_obj.complete_order(driver, brand, cc);
            Thread.sleep(20000);
            getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_upsellpage_" + pattern + "_" + flow +".har");
            
            //////////////////////////////////////////////////////////
            // Confirmation Page	        
            defineNewHar(proxy, brand + "ConfirmationPage");
            
            String upsell_value = "";
   			upsell_value = offerdata.get("UPGRADE").toString();
    	    		
            // Navigate to Confirmation Page
            bf_obj.upsell_confirmation(driver, brand, campaign, upsell_value);
            Thread.sleep(20000);
            getHarData(proxy, System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + origcampaign + "_confirmationpage_" + pattern + "_" + flow +".har");
            Thread.sleep(3000);
		}        
	
		String expected_ppid = offerdata.get("PPID").toString();
		System.out.println("Expcted PPID : " + expected_ppid);
		
        String actual_conf_ppid = bf_obj.fetch_confoffercode(driver, brand, false);
		System.out.println("Actual PPID : " + actual_conf_ppid);
        
        String conf_num = bf_obj.fetch_conf_num(driver, brand);
        System.out.println("Confirmation Number : " + conf_num);        
	
		List<String> output_row = new ArrayList<String>();
		output_row.add(env);
		output_row.add(brand);
		output_row.add(campaign);
        output_row.add(email);
        output_row.add(expected_ppid);
		output_row.add(actual_conf_ppid);
		output_row.add(conf_num);
		
        // Save Order Screenshots        
		Screenshot confpage = new AShot().takeScreenshot(driver);			
        ImageIO.write(confpage.getImage(),"PNG",new File(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Screenshots\\" + brand + "\\" + offerdata.get("PPID").toString() +".png"));	
        
        driver.close();
        return output_row;
	}
}
