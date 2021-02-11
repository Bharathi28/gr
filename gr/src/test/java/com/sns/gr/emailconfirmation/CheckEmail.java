package com.sns.gr.emailconfirmation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.sns.gr.testbase.CommonUtilities;

public class CheckEmail {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
		System.setProperty("webdriver.firefox.driver", System.getProperty("user.dir")+"/Drivers/geckodriver.exe");		
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("No.of Brands : ");
		int num = in.nextInt();
		
		System.out.println("Brands:");
		List<String> brands = new ArrayList<String>();
		for(int i=0; i<num; i++) {
			String s = in.next();
			brands.add(s);
		}
		System.out.println(brands);			
		
		List<List<String>> output = new ArrayList<List<String>>();
		
		List<String> header_list = new ArrayList<String>();
		header_list.add("Brand");
		header_list.add("Campaign");
		header_list.add("Offercode");
		header_list.add("Email");
		output.add(header_list);
		
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
				
		for(String brand : brands) {
//			File input_file = new File(System.getProperty("user.dir")+"\\Input_Output\\BuyflowValidation\\Run Output\\BuyflowResults_572020");
			File input_file = new File("E:\\Automation\\Buyflow\\02112021\\Daily Orders - 02112021.xlsx");
			FileInputStream inputstream = new FileInputStream(input_file);
			Workbook testData = new XSSFWorkbook(inputstream);
			Sheet dataSheet = testData.getSheet(brand);
			Row row;
			
			for(int itr = 1; itr <= dataSheet.getLastRowNum(); itr++) {		
				
				driver.get("http://www.yopmail.com/en/");
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				
				row = dataSheet.getRow(itr);
				String env = row.getCell(0).getStringCellValue();
				String campaign = row.getCell(2).getStringCellValue();
//				String email = row.getCell(3).getStringCellValue();
				String email = row.getCell(4).getStringCellValue();
				String offercode = row.getCell(5).getStringCellValue();		
				
				if(email.contains("testbuyer")) {
					continue;
				}
				
				WebElement emailBox = driver.findElement(By.xpath("//input[@id='login']"));
				emailBox.clear();
				emailBox.sendKeys(email);
				
				WebElement checkInbox = driver.findElement(By.xpath("//input[@value='Check Inbox']"));
				checkInbox.click();
				
				driver.switchTo().frame("ifinbox");
				List<WebElement> mailList = driver.findElements(By.xpath("//div[@class='m']//span[@class='lmf']"));
				driver.switchTo().defaultContent();				
				
				if(mailList.size() == 0) {
					System.out.println(brand + " - " + offercode + " - " + email + " - " + "No mails available");
				}
				else if(mailList.size() >= 1) {
					System.out.println(brand + " - " + offercode + " - " + email + " - " + "Email Received");
					List<String> output_row = new ArrayList<String>();
					output_row.add(brand);
					output_row.add(campaign);
					output_row.add(offercode);
					output_row.add(email);
					output_row.add("Email Received");
					output.add(output_row);
				}
			}
			testData.close();
			inputstream.close();
		}
		in.close();
	}
}
