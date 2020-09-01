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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sns.gr.testbase.CommonUtilities;

public class CheckEmailMailnesia {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/Drivers/chromedriver.exe");
		System.setProperty("webdriver.firefox.driver", System.getProperty("user.dir") + "/Drivers/geckodriver.exe");

		Scanner in = new Scanner(System.in);

		System.out.println("No.of Brands : ");
		int num = in.nextInt();

		System.out.println("Brands:");
		List<String> brands = new ArrayList<String>();
		for (int i = 0; i < num; i++) {
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

		for (String brand : brands) {
			File input_file = new File(System.getProperty("user.dir")
					+ "\\Input_Output\\BuyflowValidation\\Run Output\\BuyflowResults_8312020.xlsx");// BuyflowResults_572020BuyflowResults_8212020
			// File input_file = new
			// File("F:\\Automation\\Buyflow\\DailyOrders\\Run
			// Output\\BuyflowResults_10232019.xlsx");
			FileInputStream inputstream = new FileInputStream(input_file);
			Workbook testData = new XSSFWorkbook(inputstream);
			Sheet dataSheet = testData.getSheet(brand);
			Row row;

			for (int itr = 1; itr <= dataSheet.getLastRowNum(); itr++) {
				String C_URL = "http://mailnesia.com/captcha.html";
				String URL = "http://mailnesia.com/";
				driver.get(URL);
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

				row = dataSheet.getRow(itr);
				String env = row.getCell(0).getStringCellValue();
				String campaign = row.getCell(2).getStringCellValue();
				// String email = row.getCell(3).getStringCellValue();
				String email = row.getCell(4).getStringCellValue();
				String offercode = row.getCell(5).getStringCellValue();
				By mail_input = By.xpath("//input[@id='mailbox']");
				By go = By.xpath("//input[@id='sm']");
				By mail_rec = By.xpath("//table[@class='email']//tbody//tr[1]//td[2]");
				WebElement emailBox = driver.findElement(mail_input);
				emailBox.clear();
				emailBox.sendKeys(email);

				WebElement checkInbox = driver.findElement(go);
				checkInbox.click();
				if (URL == C_URL) {
					driver.get(URL);
				}
				List<WebElement> mailList = driver.findElements(mail_rec);

				if (mailList.size() == 0) {
					System.out.println(brand + " - " + offercode + " - " + email + " - " + "No mails available");
				} else if (mailList.size() >= 1) {
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
		driver.quit();
	}
}