package com.sns.gr.pingdom;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.MailUtilities;

public class GetCharts {
	CommonUtilities comm_obj = new CommonUtilities();

	By login = By.xpath("//button[contains(text(),'Log in')]");
	By img = By.xpath("//i[@class='chart-line icon']");
	By RUM = By.xpath("//h4[text()='Visitor Insights (RUM)']");
	By Log_img = By.xpath("//div[@class='default']");
	By Log_out = By.xpath("//button[contains(text(),'Log out')]");
	String def_date;
	String from_date;
	By page_2 = By.xpath("//body//button[2]");
	String USA = new String("United States");
	Scanner in = new Scanner(System.in);
	List<String> brandlist;
	MailUtilities mailObj = new MailUtilities();
	String sendReportTo = "aaqil@searchnscore.com, manibharathi@searchnscore.com";

	@Parameters({ "daycount" })
	@Test
	public void pingdomCharts(int count) throws IOException, InterruptedException {

		String USA = new String("United States");

		if (count == 1) {
			System.out.println("No.of Brands : ");
			int num = in.nextInt();

			System.out.println("Brands Name : ");
			brandlist = new ArrayList<String>();
			for (int i = 0; i < num; i++) {
				String s = in.next();
				brandlist.add(s);
			}
			// brandlist = Arrays.asList("Trydermaflash", "Volaire");

		} else {

			brandlist = Arrays.asList("Sub-D", "MeaningfulBeauty", "CrepeErase", "Mally", "WestmoreBeauty",
					"Smileactives", "SpecificBeauty", "Seacalmskin", "Trydermaflash", "Volaire");
		}

		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/Drivers/chromedriver.exe");
		WebDriver driver = new ChromeDriver();

		driver.manage().window().maximize();
		driver.get("https://my.pingdom.com");
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		JavascriptExecutor je = (JavascriptExecutor) driver;
		XMLSlideShow ppt = new XMLSlideShow();

		WebElement username = driver.findElement(By.xpath("//input[@placeholder='Email']"));
		username.sendKeys("manibharathi@searchnscore.com");

		WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
		password.sendKeys("snsGR_123");

		WebElement login_button = driver.findElement(login);

		new Actions(driver).moveToElement(login_button).click().perform();

		driver.switchTo().frame("main-frame");
		driver.findElement(RUM).click();
		driver.switchTo().defaultContent();		

			for (String brand : brandlist) {

				System.out.println(brand);
				driver.switchTo().frame("main-frame");
				By Sub = By.xpath("//a[contains(text(),'" + brand + "')]");
				if(brand.equalsIgnoreCase("Volaire")) {
					je.executeScript("window.scrollBy(0,document.body.scrollHeight)", 0);
					comm_obj.waitUntilElementAppears(driver, "//body//button[2]");
					driver.findElement(page_2).click();
					Thread.sleep(5000);
					je.executeScript("window.scrollTo(0, 0)", 0);
				}
				else {
					while(driver.findElements(Sub).size() == 0){
						je.executeScript("window.scrollTo(0, 200)", 0);
					}
				}
				driver.findElement(Sub).click();
				driver.switchTo().defaultContent();	

				By all_country = By.xpath("//button[contains(text(),'All countries')]");
				By US = By.xpath("(//div[text()='" + USA + "'])[1]");

				By format = By.xpath("//span[contains(text(),'Last 24 hours')]");
				By Custom = By.xpath("//li[contains(text(),'Custom')]");
						
				driver.switchTo().frame("main-frame");

				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((all_country)));
				new Actions(driver).moveToElement(driver.findElement(all_country)).click().perform();

				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((US)));
				new Actions(driver).moveToElement(driver.findElement(US)).click().perform();

				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((format)));
				new Actions(driver).moveToElement(driver.findElement(format)).click().perform();

				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((Custom)));
				new Actions(driver).moveToElement(driver.findElement(Custom)).click().perform();

				By fromdate = By.xpath("//div[@class='modal__wrapper___SJHUP']//div[1]//div[1]//div[1]//div[1]//input[1]");
				By datetm = By.xpath("(//input[@class='input__root___2vgaY'])[2]");
				By datefm = By.xpath("(//input[@class='input__root___2vgaY'])[1]");
				def_date = driver.findElement(fromdate).getAttribute("value");
				from_date = date(def_date, count);

				System.out.println("From Date : " + date(def_date, count) + " To Date : " + def_date);
				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((datefm)));

				WebElement Selectdatefrom = driver.findElement(datefm);
				for (int i = 1; i <= count; i++) {
					Selectdatefrom.sendKeys(Keys.ARROW_LEFT);
				}
				WebElement Selectdateto = driver.findElement(datetm);
				Selectdateto.sendKeys(Keys.ENTER);

				By cancel = By.xpath("//button[contains(text(),'Cancel')]");
				By apply = By.xpath("//button[contains(text(),'Apply')]");

				Thread.sleep(2200);
				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((apply)));
				new Actions(driver).moveToElement(driver.findElement(apply)).click().perform();
				Thread.sleep(1000);

				if (isDisplayed(driver, cancel) == true) {
					new Actions(driver).moveToElement(driver.findElement(cancel)).click().perform();
				}

				By Top = By.xpath("//h2[contains(text(),'Top platforms')]/../../..");
				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((Top)));
				WebElement chart = driver.findElement(Top);// Country);//
				// je.executeScript("arguments[0].scrollIntoView(false);",
				// chart);
				je.executeScript("window.scrollBy(0,650)");
				Thread.sleep(2000);

				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				BufferedImage fullImg = ImageIO.read(screenshot);
				Point point = chart.getLocation();

//				BufferedImage eleScreenshot = fullImg.getSubimage(220, 260, 1147, 248);
				BufferedImage eleScreenshot = fullImg.getSubimage(275, 330, 1080, 308);
				ImageIO.write(eleScreenshot, "png", screenshot);

				File screenshotLocation = new File(System.getProperty("user.dir") + "\\Input_Output\\Pingdom\\" + brand + ".png");
				Files.copy(screenshot, screenshotLocation);

				XSLFSlide slide = ppt.createSlide();

				File image = new File(System.getProperty("user.dir") + "\\Input_Output\\Pingdom\\" + brand + ".png");
				byte[] picture = IOUtils.toByteArray(new FileInputStream(image));

				XSLFPictureData idx = ppt.addPicture(picture, PictureType.PNG);
				XSLFPictureShape pic = slide.createPicture(idx);

				pic.setAnchor(new java.awt.Rectangle(20, 130, 720, 280));
//				pic.setAnchor(new java.awt.Rectangle(70, 200, 800, 370));

				XSLFTextBox shape = slide.createTextBox();
				XSLFTextParagraph p = shape.addNewTextParagraph();
				XSLFTextRun r = p.addNewTextRun();
				r.setText("From Date : " + date(def_date, count) + " Till Date : " + def_date + "\n" + " Brand Name : " + brand);
				r.setFontColor(new Color(0, 0, 0));
				r.setFontSize(25d);

//				} else {
//					System.out.println("Wrong brand name :" + Dy);
//				}
//				}

				driver.switchTo().defaultContent();
//				driver.switchTo().parentFrame();
				driver.findElement(By.xpath("//a[text()='Visitor Insights (RUM)']")).click();
//				new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((RUM)));
//				new Actions(driver).moveToElement(driver.findElement(RUM)).click().perform();
			}
			Date date = new Date();
			SimpleDateFormat date_form = new SimpleDateFormat("ddMMyyyy");
			String date_pptname = date_form.format(date);

			File file = new File(System.getProperty("user.dir") + "\\Input_Output\\Pingdom\\" + date_pptname + ".pptx");
			FileOutputStream out = new FileOutputStream(file);

			// saving the changes to a file
			ppt.write(out);
			System.out.println("image added successfully");

			String subject = "";
			if (count == 1) {
				subject = "Pingdom Charts - Today : ";
			} else if (count == 7) {
				subject = "Pingdom Charts - Last 7 days : ";
			}

			List<String> attachmentList = new ArrayList<String>();
			attachmentList.add(System.getProperty("user.dir") + "\\Input_Output\\Pingdom\\" + date_pptname + ".pptx");

			mailObj.sendEmail(subject, sendReportTo, attachmentList);

			ppt.close();
//		}
		driver.switchTo().defaultContent();
		driver.switchTo().parentFrame();
		new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((Log_img)));
		new Actions(driver).moveToElement(driver.findElement(Log_img)).click().perform();
		new WebDriverWait(driver, 500).until(ExpectedConditions.visibilityOfElementLocated((Log_out)));
		new Actions(driver).moveToElement(driver.findElement(Log_out)).click().perform();
		driver.close();
		driver.quit();
	}

	public boolean isElementPresent(WebDriver driver, By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public int frame(WebDriver driver) {
		int size = 0;
		int T;
		int L = 5;
		int S = 2;
		int F = 3;
		T = driver.findElements(By.tagName("iframe")).size();
		if (T >= 4) {
			size = T - L;

		} else if (T == 2) {
			size = T - S;
		} else if (T == 3) {
			size = T - F;
		}
		System.out.println(size);
		return size;

	}

	public String date(String def_date, int count) {

		DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		// Date date = new Date();

		LocalDate date2 = getDateFromString(def_date, format);
		Date date = convertToDateViaSqlDate(date2);
		// System.out.println("Current Date " + dateFormat.format(date));
		// Convert Date to Calendar
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		// Perform addition/subtraction

		c.add(Calendar.DATE, -count);

		// Convert calendar back to Date
		Date currentDatePlusOne = c.getTime();

		// System.out.println("Updated Date " +
		// dateFormat.format(currentDatePlusOne));
		String Cdate = dateFormat.format(currentDatePlusOne);
		return Cdate;
	}

	public static LocalDate getDateFromString(String string, DateTimeFormatter format) {
		// Convert the String to Date in the specified format
		LocalDate date = LocalDate.parse(string, format);
		return date;
	}

	public static Date convertToDateViaSqlDate(LocalDate dateToConvert) {
		return java.sql.Date.valueOf(dateToConvert);
	}

	public boolean isDisplayed(WebDriver driver, By by) {
		List<WebElement> buttons = driver.findElements(by);
		if (buttons.size() > 0 && buttons.get(0).isDisplayed()) {
			return true;
		}
		return false;
	}

}
