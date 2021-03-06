package com.sns.gr.catchpoint;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.sns.gr.testbase.MailUtilities;

public class GetChartsDaily {

	MailUtilities mailObj = new MailUtilities();
//	String sendReportTo = "rg@searchnscore.com , indhuja@searchnscore.com , vanitha@searchnscore.com , megavannan@searchnscore.com , sangeetha@searchnscore.com , sangeetha.vellingiri@searchnscore.com , preethi.mani@searchnscore.com , selvakumar@searchnscore.com , steephan@searchnscore.com , pavithra@searchnscore.com";
	String sendReportTo = "steephan@searchnscore.com";
	
	@Parameters({ "daycount" })
	@Test
	public void catchpointCharts(int count) throws IOException, InterruptedException {
		String timeframetext = null;

//		List<String> brandlistfor1day = Arrays.asList("Meaningful Beauty [Chrome]", "MB[Mobie-Web test]", "CXT- USER Navigation Flow", "MB CXT Mobile Transaction Test",
//			"SubD [Chrome]", "SubD[Mobie-Web test]", "ITC CXT-Desktop", "IT CXT Mobile",
//			"Crepe Erase [Chrome]", "CE[Mobile-Web Test]", "CrepeErase Transactional Test - Desktop", "CrepeErase Transactional Test - Mobile",
//			"VP [Chrome]", "Smileactives Brand.com-Desktop", "Smileactives Brand.com-Mobile", "SA CXT Desktop", "SA CXT Mobile",
//			"SB Desktop", "SB Mobile", "SB CXT-Chrome", "trydermaflash Desktop[Chrome]","trydermaflash Mobile",
//			"Mally Core[Desktop]", "Mally Core[Mobile]", "Mally Transactional Test - Desktop", "Mally Transactional Test - Mobile",
//			"Volaire ACQ Desktop", "Volaire ACQ MOBILE",
//			"Westmore ACQ- Desktop", "Westmore ACQ-Mobile", "WestMoreBeauty CXT Desktop", "Westmorebeauty CXT Mobile", 
//			"Dr. Denese ACQ Desktop", "Dr. Denese ACQ Mobile",
//			"SeaCalm- Desktop", "SeaCalm-Mobile", "SeaCalm Transactional Test - Desktop", "SeaCalm Transactional Test - Mobile",
//			"FixMDSkin - Desktop", "FixMDSkin - Mobile");		
		
		List<String> brandlistfor1day = Arrays.asList("Mally Core[Desktop]", "Mally Core[Mobile]", "SubD [Chrome]", "SubD[Mobie-Web test]",
				 "Volaire ACQ Desktop", "Volaire ACQ MOBILE", "trydermaflash Desktop[Chrome]","trydermaflash Mobile", "VP [Chrome]");	
		
//		List<String> brandlistfor1day = Arrays.asList("Smileactives Brand.com-Desktop", "Smileactives Brand.com-Mobile", "SB Desktop", "SB Mobile",
//				"SeaCalm- Desktop", "SeaCalm-Mobile");
		
//		List<String> brandlistfor1day = Arrays.asList("Meaningful Beauty [Chrome]", "MB[Mobie-Web test]",
//				"Crepe Erase [Chrome]", "CE[Mobile-Web Test]","Westmore ACQ- Desktop", "Westmore ACQ-Mobile");		
		
		List<String> brandlistfor7days = Arrays.asList("Crepe Erase [Chrome]", "Meaningful Beauty [Chrome]",
				"Westmore ACQ- Desktop","Smileactives Brand.com-Desktop",
				"Volaire ACQ Desktop","SB Desktop", "trydermaflash Desktop[Chrome]","Mally Core[Desktop]",
				"SubD [Chrome]", "SeaCalm- Desktop","VP [Chrome]", 
				"Dr. Denese ACQ Desktop","FixMDSkin - Desktop");

		ArrayList<String> brandarraylist = new ArrayList<String>();
		
		if(count==1) {
			timeframetext = "Last 24 Hours";
			brandarraylist.addAll(brandlistfor1day);
		}
		else if(count==7){
			timeframetext = "Last 7 Days";
			brandarraylist.addAll(brandlistfor7days);
		}
		else {
			System.out.println("You have entered an invalid number. We are setting 1 day as default");
		}
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		driver.manage().window().maximize();
		driver.get("https://portal.catchpoint.com/ui/Content/Home.aspx?da=3069");
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		
		JavascriptExecutor je = (JavascriptExecutor) driver;
		XMLSlideShow ppt = new XMLSlideShow();		
		
		WebElement username = driver.findElement(By.id("Email")); 
		username.sendKeys("vanitha@searchnscore.com");
		
		WebElement password = driver.findElement(By.id("Password"));
		password.sendKeys("Chamy@1307");
		
		WebElement login_button = driver.findElement(By.id("LoginButton"));
		login_button.click();
		String[] brand_array = new String[brandarraylist.size()];
		for(int i=0;i<brandarraylist.size();i++){
			brand_array[i] = brandarraylist.get(i);
		}		
		for (String brand : brand_array) {
			je.executeScript("window.scrollTo(0, 0);");
			Thread.sleep(2000);			
				
			WebElement analysis_page = driver.findElement(By.className("chts"));
			analysis_page.click();
			
			Select testtype = new Select(driver.findElement(By.id("TestTypeDropdown")));
			if((brand.contains("CXT")) || (brand.contains("Transactional"))) {
				testtype.selectByVisibleText("Transaction");
			}
			else {
				testtype.selectByVisibleText("Web");
			}
			
			WebElement test_button = driver.findElement(By.id("SelectTestButton"));
			test_button.click();
			Thread.sleep(1000);
			
			WebElement brand_link = null;
			if(driver.findElements(By.partialLinkText(brand)).size() != 0) {
				brand_link = driver.findElement(By.partialLinkText(brand));
			}
			else if(driver.findElements(By.linkText("Next >")).size() != 0) {
				driver.findElement(By.linkText("Next >")).click();
				Thread.sleep(3000);
				if(driver.findElements(By.partialLinkText(brand)).size() != 0) {
					System.out.println("after next click");
					brand_link = driver.findElement(By.partialLinkText(brand));
				}
			}
			else {
				driver.findElement(By.id("ctl00_ctl00_ContentPlaceholder1_DetailContentPlaceholder_PerformanceHeaderSection1_TestSearch1_DoneButton")).click();
				continue;
			}
			
			System.out.println(brand);
			WebElement brand_row = brand_link.findElement(By.xpath("../.."));
			System.out.println(brand_row.getText());
			
			WebElement brand_checkbox = brand_row.findElement(By.tagName("span")).findElement(By.tagName("input"));
			brand_checkbox.click();
			
			driver.findElement(By.id("ctl00_ctl00_ContentPlaceholder1_DetailContentPlaceholder_PerformanceHeaderSection1_TestSearch1_DoneButton")).click();
			
			Select timeframe = new Select(driver.findElement(By.id("TimeframeSetOptions")));
			timeframe.selectByVisibleText(timeframetext);
			
			WebElement draw_button = driver.findElement(By.id("DrawChartButton"));
			draw_button.click();
			
			if(driver.findElements(By.id("DefaultChartText")).size() != 0) { 
				ppt.createSlide();
				continue;
			}
			
			WebElement chart = driver.findElement(By.id("Chart2"));
			je.executeScript("arguments[0].scrollIntoView(true);",chart);
			
			Thread.sleep(2000);
			
			File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			BufferedImage fullImg = ImageIO.read(screenshot);
			
			Point point = chart.getLocation();
//			int eleWidth = chart.getSize().getWidth();
//			int eleHeight = chart.getSize().getHeight();
		
//			BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), 0, 950, 540);
			BufferedImage eleScreenshot= fullImg.getSubimage(400, 0, 1200, 670);
			ImageIO.write(eleScreenshot, "png", screenshot);
				
			File screenshotLocation = new File(System.getProperty("user.dir") + "\\Input_Output\\Catchpoint\\" + brand +".png");
			Files.copy(screenshot, screenshotLocation);
			
			XSLFSlide slide = ppt.createSlide();
			
			File image = new File(System.getProperty("user.dir") + "\\Input_Output\\Catchpoint\\" + brand +".png");
		    byte[] picture = IOUtils.toByteArray(new FileInputStream(image));
		    
		    XSLFPictureData idx = ppt.addPicture(picture, PictureType.PNG);
		    XSLFPictureShape pic = slide.createPicture(idx);
		    pic.setAnchor(new java.awt.Rectangle(28, 37, 650, 220));
		}
		Date date = new Date();
		SimpleDateFormat date_form = new SimpleDateFormat("ddMMyyyy");
		String date_pptname = date_form.format(date);
	    
	    File file = new File(System.getProperty("user.dir") + "\\Input_Output\\Catchpoint\\" + date_pptname +".pptx");
        FileOutputStream out = new FileOutputStream(file);
      
        //saving the changes to a file
        ppt.write(out);
        System.out.println("image added successfully");
        out.close();
	    driver.close();
	    
	    String subject = "";
	    if(count==1) {
	    	subject = "Catchpoint Charts - Today";
		}
		else if(count==7){
			subject = "Catchpoint Charts - Last 7 days";
		}
	
	    List<String> attachmentList = new ArrayList<String>();
		attachmentList.add(System.getProperty("user.dir") + "\\Input_Output\\Catchpoint\\" + date_pptname +".pptx");
		
	    mailObj.sendEmail(subject, sendReportTo, attachmentList);
	    ppt.close();
    }
}
