package com.sns.gr.pixel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sns.gr.testbase.CommonUtilities;
import com.sns.gr.testbase.DBUtilities;
import com.sns.gr.testbase.MailUtilities;
import com.sns.gr.testbase.PixelUtilities;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

public class PixelParallel {

	static CommonUtilities comm_obj = new CommonUtilities();
	static MailUtilities mailObj = new MailUtilities();
	
	List<List<String>> buyflowOverallOutput = new ArrayList<List<String>>();
	static List<String> attachmentList = new ArrayList<String>();

	static String sendReportTo = "manibharathi@searchnscore.com , banuchitra@searchnscore.com";
	
	@DataProvider(name="pixelInput", parallel=true)
	public Object[][] testData() {
		Object[][] arrayObject = comm_obj.getExcelData(System.getProperty("user.dir")+"/Input_Output/PixelValidation/pixel_testdata.xlsx", "Run Data");
		return arrayObject;
	}
	
	@Test(dataProvider="pixelInput")
	public void pixel(String env, String brand, String campaign, String flow, String pixelStr) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
		System.setProperty("webdriver.firefox.driver", System.getProperty("user.dir")+"/Drivers/geckodriver.exe");
//		System.setProperty("webdriver.chrome.logfile", "C:\\chromedriver78.log");
//		System.setProperty("webdriver.chrome.verboseLogging", "true");
				
		// start the proxy
	    BrowserMobProxy proxy = new BrowserMobProxyServer();
	    proxy.setTrustAllServers(true);
	    proxy.start(0);

	    // get the Selenium proxy object
	    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);	    
	    
	    ChromeOptions options = new ChromeOptions();
	    options.addArguments("--ignore-certificate-errors");

	    // configure it as a desired capability
	    DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
	    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
	    capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	    capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
	    
	    List<List<String>> output = new ArrayList<List<String>>();
	    
	    List<String> header_list = new ArrayList<String>();
		header_list.add("Test Run Environment");
		header_list.add("Brand");
		header_list.add("Campaign");
		header_list.add("Pixel");
		header_list.add("Event");
		header_list.add("Event Fired Pages");
		header_list.add("Status Code");
		header_list.add("Data URL");
		output.add(header_list);
	    
	    DBUtilities db_obj = new DBUtilities();
		PixelUtilities pixel_obj = new PixelUtilities();		
		
		String realm = DBUtilities.get_realm(brand);
		
		HashMap<Integer, HashMap> overallOutput = new LinkedHashMap<Integer, HashMap>();
								
		String url = "";
		url = db_obj.getUrl(brand, campaign, env);
		System.out.println(url);
		
		if(!((env.equalsIgnoreCase("qa")) || (env.equalsIgnoreCase("prod")) || (env.equalsIgnoreCase("stg")))) {
			url = db_obj.getUrl(brand, campaign, "stg");
			url = url.replace("stg", env.toLowerCase());
		}
		
		int noOfTestRuns = 0;
		List<String> pixelslist = new ArrayList<String>();
			
		String temp = pixelStr.toLowerCase();
		if(pixelStr.toLowerCase().contains("cake")) {				
			noOfTestRuns++;
			pixelslist.add("Cake");
			temp = temp.replace("cake", "");
		}
		if(pixelStr.toLowerCase().contains("harmonyconversiontracking")) {				
			noOfTestRuns++;
			pixelslist.add("HarmonyConversionTracking");
			temp = temp.replace("harmonyconversiontracking", "");
		}
		if(pixelStr.toLowerCase().contains("linkshare")) {				
			noOfTestRuns++;
			pixelslist.add("LinkShare");
			temp = temp.replace("linkshare", "");
		}
		if(pixelStr.toLowerCase().contains("starmobile")) {			
			noOfTestRuns++;
			pixelslist.add("StarMobile");
			temp = temp.replace("starmobile", "");
		}
		if(pixelStr.toLowerCase().contains("data+math")) {			
			noOfTestRuns++;
			pixelslist.add("Data+Math");
			temp = temp.replace("data+math", "");
		}
		if(pixelStr.toLowerCase().contains("propelmedia")) {			
			noOfTestRuns++;
			pixelslist.add("PropelMedia");
			temp = temp.replace("propelmedia", "");
		}
		Pattern p = Pattern.compile("[a-zA-Z]+");
		Matcher m = p.matcher(temp);
		if (m.find()) {
			noOfTestRuns++;
		}
		List<List<String>> buyflowOutput = pixel_obj.generateTestRuns(capabilities, proxy, env, brand, campaign, flow, pixelslist, noOfTestRuns, url);	
		buyflowOverallOutput.addAll(buyflowOutput);
			
		WebDriver driver = new ChromeDriver();
	    driver.manage().window().maximize();
	    driver.get("https://ericduran.github.io/chromeHAR/");
	    WebDriverWait wait = new WebDriverWait(driver,50);
//	    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	    
	    if(driver.findElements(By.xpath("//button[@id='details-button']")).size() != 0) {
			driver.findElement(By.xpath("//button[@id='details-button']")).click();
			driver.findElement(By.xpath("//a[@id='proceed-link']")).click();
		}
	        
	    String[] pixelArr = pixelStr.split(",");		
			
		int j=1;
		for(String pixel : pixelArr) {	
				
			HashMap<String, HashMap> envMap = new LinkedHashMap<String, HashMap>();
			HashMap<String, HashMap> brandMap = new LinkedHashMap<String, HashMap>();
			HashMap<String, HashMap> campaignMap = new LinkedHashMap<String, HashMap>();
			HashMap<String, List<HashMap>> pixelMap = new LinkedHashMap<String, List<HashMap>>();
								
			List<String> events = db_obj.getAllEvents(pixel);		
				
			String urlpattern = "";
			if(pixel.toLowerCase().contains("cake")) {
				urlpattern = "Cake";
			}
			else if(pixel.toLowerCase().contains("harmonyconversiontracking")) {
			   	urlpattern = "HarmonyConversionTracking";
			}
			else if(pixel.toLowerCase().contains("linkshare")) {
			    urlpattern = "Linkshare";
			}
			else if(pixel.toLowerCase().contains("starmobile")) {
				urlpattern = "StarMobile";
			}
			else if(pixel.toLowerCase().contains("data+math")) {
				urlpattern = "Data+Math";
			}
			else if(pixel.toLowerCase().contains("propelmedia")) {
			    urlpattern = "PropelMedia";
			}
			List<HashMap> eventmapList = new ArrayList<HashMap>();
			for(String event : events) {					
				HashMap<String, List<HashMap>> eventMap = new LinkedHashMap<String, List<HashMap>>();
				System.out.println();
				System.out.println(event);
									
				int compatible = db_obj.checkBrandPixelCompatibility(brand, event);					
				if(compatible == 1) {
					List<String> pages = db_obj.getFiringPages(brand, campaign, flow, pixel, event);
						
					String pattern = db_obj.getSearchPattern(brand, event);
					String pixelbrandid = db_obj.getPixelBrandId(brand, event);
						
					String[] pixelIdArr = pixelbrandid.split(",");						
						
					List<HashMap> pagemapList = new ArrayList<HashMap>();
					for(String page : pages) {													
						HashMap<String, List<List<String>>> pageMap = new LinkedHashMap<String, List<List<String>>>();	
				        System.out.println(page);
//						driver.findElement(By.name("har")).sendKeys("C:\\Automation\\Pixel\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_" + page + "_" + urlpattern + "_" + flow + ".har");
						driver.findElement(By.name("har")).sendKeys(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Harfiles\\" + brand + "\\" + brand + "_" + campaign + "_" + page + "_" + urlpattern + "_" + flow + ".har");
						
						WebElement searchElmt = driver.findElement(By.id("search"));
						wait.until(ExpectedConditions.visibilityOf(searchElmt));
//						Thread.sleep(2000);
				        driver.findElement(By.id("search")).clear();
				        driver.findElement(By.id("search")).sendKeys(pattern);
				        Thread.sleep(2000);
				            
				        int noOfRows = driver.findElements(By.xpath("//tr[contains(@class, 'revealed network-item')]")).size();
				            
				        List<List<String>> outputList = new ArrayList<List<String>>();
				        for(int i=1; i<=noOfRows; i++) {			            	
					            	
					        String dataurl = driver.findElement(By.xpath("(//tr[contains(@class, 'revealed network-item')]//td[@class='name-column'])[" + i + "]")).getAttribute("title").toLowerCase();
					        String statuscode = driver.findElement(By.xpath("(//tr[contains(@class, 'revealed network-item')]//td[@class='status-column'])[" + i + "]")).getAttribute("title").toLowerCase();
					            
					        for(String id : pixelIdArr) {
					        	if(!(id.equalsIgnoreCase(" "))) {
					            	if(pattern.equalsIgnoreCase("-")) {
					            		if(dataurl.contains(id.toLowerCase())) {
					            			List<String> outputData = new ArrayList<String>();
											outputData.add(statuscode);
											outputData.add(dataurl);
											outputList.add(outputData);
											System.out.println(i + " " + statuscode + " " + dataurl);
					            		}
					            	}
					            	else {
					            		if((dataurl.contains(pattern.toLowerCase())) && (dataurl.contains(id.toLowerCase()))) {
											List<String> outputData = new ArrayList<String>();
											outputData.add(statuscode);
											outputData.add(dataurl);
											outputList.add(outputData);
											System.out.println(i + " " + statuscode + " " + dataurl);
					            		}					            			
									}
								}
								else {
									if(dataurl.contains(pattern.toLowerCase())) {
										List<String> outputData = new ArrayList<String>();
										outputData.add(statuscode);
										outputData.add(dataurl);
										outputList.add(outputData);
										System.out.println(i + " " + statuscode + " " + dataurl);
									}
								}		
					        }										            		
					    } // end of rows
				        if(outputList.size() == 0) {
				            List<String> outputData = new ArrayList<String>();
							outputData.add(" ");
							outputData.add(" ");
							outputList.add(outputData);
				        }		            				            
				        pageMap.put(page, outputList);
				        pagemapList.add(pageMap);
				    } // end of pages						
					eventMap.put(event, pagemapList);
				} // end of compatible if
				eventmapList.add(eventMap);					
			} // end of events
			pixelMap.put(pixel, eventmapList);
			campaignMap.put(campaign, pixelMap);
			brandMap.put(brand, campaignMap);
			envMap.put(env, brandMap);		
			overallOutput.put(j++, envMap);
		} // end of pixels
		writeToSheet(overallOutput, brand, campaign, flow);
		driver.close();
	} // end of main
	
	@AfterSuite
	public void populateExcel() throws IOException {
		String file = comm_obj.populateOutputExcel(buyflowOverallOutput, "Pixel_BuyflowResults", System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Pixel Orders\\");
		attachmentList.add(file);
		mailObj.sendEmail("Pixel Buyflow Results", sendReportTo, attachmentList);
	}
	
	public static void writeToSheet(HashMap map, String fileName, String sheetName, String flow) throws IOException {	
		File file = new File(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Pixel_Output\\" + fileName + "_" + flow + ".xlsx");
		XSSFWorkbook workbook = null;
		// Check file existence 
	    if (file.exists() == false) {
	        // Create new file if it does not exist
	        workbook = new XSSFWorkbook();
	    } 
	    else {
	        FileInputStream inputStream = new FileInputStream(new File(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Pixel_Output\\" + fileName + "_" + flow + ".xlsx"));
	        workbook = new XSSFWorkbook(inputStream);
	    }
	    
		XSSFSheet resultSheet = null;
		
		// Check if the workbook is empty or not
	    if (workbook.getNumberOfSheets() != 0) {
	    	String sheetExists = comm_obj.checkSheetExists(workbook, sheetName);
	    	if(sheetExists.equalsIgnoreCase("true")) {
	    		resultSheet = workbook.getSheet(sheetName);
	    	}
	    	else {
	    		resultSheet = workbook.createSheet(sheetName);
	    	}
	    }
	    else {
	    	resultSheet = workbook.createSheet(sheetName);
	    }
				
		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());
		
		XSSFCellStyle headercellstyle = workbook.createCellStyle();
		headercellstyle.setFont(font);
		headercellstyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
		headercellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headercellstyle.setAlignment(HorizontalAlignment.LEFT);
		headercellstyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headercellstyle.setBorderLeft(BorderStyle.THIN);
		headercellstyle.setBorderRight(BorderStyle.THIN);
		headercellstyle.setBorderTop(BorderStyle.THIN);
		headercellstyle.setBorderBottom(BorderStyle.THIN);
		
		XSSFCellStyle errorcellstyle = workbook.createCellStyle();
		errorcellstyle.setFillForegroundColor(IndexedColors.RED.getIndex());
		errorcellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		errorcellstyle.setAlignment(HorizontalAlignment.LEFT);
		errorcellstyle.setVerticalAlignment(VerticalAlignment.CENTER);
		errorcellstyle.setBorderLeft(BorderStyle.THIN);
		errorcellstyle.setBorderRight(BorderStyle.THIN);
		errorcellstyle.setBorderTop(BorderStyle.THIN);
		errorcellstyle.setBorderBottom(BorderStyle.THIN);
		
		XSSFRow headerRow = resultSheet.createRow(0);
		String[] headerArray = {"Test Run Environment","Brand","Campaign","Pixel","Event","Event Fired Pages","Status Code","Data URL"};
		for(int i=0; i<8; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headerArray[i]);
			cell.setCellStyle(headercellstyle);
		}		
		
		int row_num = 1;
		String brand = "";
		String campaign = "";
		
		Iterator it = map.entrySet().iterator();		
		while (it.hasNext()) {
			XSSFRow newRow = resultSheet.createRow(row_num);
			
			HashMap.Entry pair1 = (HashMap.Entry)it.next();
	        			
	        HashMap map1 = (HashMap) pair1.getValue();	        
	        Iterator it1 = map1.entrySet().iterator();
	        while(it1.hasNext()) {
	        	HashMap.Entry pair2 = (HashMap.Entry)it1.next();
	        	
	        	IndexedColors[] color_array = {IndexedColors.GREY_25_PERCENT,IndexedColors.LIGHT_TURQUOISE1,IndexedColors.LIGHT_CORNFLOWER_BLUE,
	        			IndexedColors.LIGHT_TURQUOISE,IndexedColors.LIGHT_GREEN,IndexedColors.LEMON_CHIFFON};
	    		int rnd = new Random().nextInt(color_array.length);
	    		IndexedColors color = color_array[rnd];  
	    		
	    		XSSFCellStyle cellstyle = workbook.createCellStyle();
	    		cellstyle.setFillForegroundColor(color.getIndex());
	    		cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    		cellstyle.setAlignment(HorizontalAlignment.LEFT);
	    		cellstyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    		cellstyle.setBorderLeft(BorderStyle.THIN);
	    		cellstyle.setBorderRight(BorderStyle.THIN);
	    		cellstyle.setBorderTop(BorderStyle.THIN);
	    		cellstyle.setBorderBottom(BorderStyle.THIN);
	        	
	        	// Environment
	        	Cell cell = newRow.createCell(0);
				cell.setCellValue(pair2.getKey().toString());
				cell.setCellStyle(cellstyle);
	        	
	        	HashMap map2 = (HashMap) pair2.getValue();
	        	Iterator it2 = map2.entrySet().iterator();
	        	while(it2.hasNext()) {
	        		HashMap.Entry pair3 = (HashMap.Entry)it2.next();
		        	
	        		// Brand
	        		brand = pair3.getKey().toString();
		        	Cell cell1 = newRow.createCell(1);
					cell1.setCellValue(pair3.getKey().toString());
					cell1.setCellStyle(cellstyle);
		        	
		        	HashMap map3 = (HashMap) pair3.getValue();
		        	Iterator it3 = map3.entrySet().iterator();
		        	while(it3.hasNext()) {		        		
		        				        		
		        		HashMap.Entry pair4 = (HashMap.Entry)it3.next();
		        		
		        		// Campaign
		        		campaign = pair4.getKey().toString();
			        	Cell cell2 = newRow.createCell(2);
						cell2.setCellValue(pair4.getKey().toString());
						cell2.setCellStyle(cellstyle);
			        	
			        	HashMap map4 = (HashMap) pair4.getValue();
			        	Iterator it4 = map4.entrySet().iterator();
			        	while(it4.hasNext()) {
			        		int pixelTotalSize = 0;		        		
			        				        		
			        		HashMap.Entry pair5 = (HashMap.Entry)it4.next();
			        	
			        		// Pixel
			        		Cell cell3 = newRow.createCell(3);
			        		cell3.setCellValue(pair5.getKey().toString());
			        		cell3.setCellStyle(cellstyle);
						
			        		List<HashMap> list1 = (List<HashMap>) pair5.getValue();
			        		for(HashMap eventMap : list1) {
			        			Iterator it5 = eventMap.entrySet().iterator();
			        			while(it5.hasNext()) {
			        				int eventTotalSize = 0;
			        				HashMap.Entry pair6 = (HashMap.Entry)it5.next();
								
			        				// Event
			        				Cell cell4 = newRow.createCell(4);
			        				cell4.setCellValue(pair6.getKey().toString());
			        				cell4.setCellStyle(cellstyle);
								
			        				List<HashMap> list2 = (List<HashMap>) pair6.getValue();
			        				for(HashMap pageMap : list2) {
			        					Iterator it6 = pageMap.entrySet().iterator();
			        					while(it6.hasNext()) {
			        						HashMap.Entry pair7 = (HashMap.Entry)it6.next();
										
			        						// Page
			        						Cell cell5 = newRow.createCell(5);
			        						cell5.setCellValue(pair7.getKey().toString());
			        						cell5.setCellStyle(cellstyle);
										
			        						List<List<String>> list3 = (List<List<String>>) pair7.getValue();
			        						for(List<String> list4 : list3) {
			        							int cell_num = 6;
			        							for(String str : list4) {
			        								Cell cell6 = newRow.createCell(cell_num);
			        								cell6.setCellValue(str);
			        								if(str.equalsIgnoreCase(" ")) {
			        									cell6.setCellStyle(errorcellstyle);
			        								}
			        								else {
			        									cell6.setCellStyle(cellstyle);
			        								}												
			        								cell_num++;
			        							}
			        							row_num++;
			        							newRow = resultSheet.createRow(row_num);
			        						}
			        						mergeAndSetBorder(((row_num-1)-(list3.size()-1)), row_num, 5, resultSheet);
			        						eventTotalSize = eventTotalSize + list3.size();
			        					}									
			        				}
			        				mergeAndSetBorder(((row_num-1)-(eventTotalSize-1)), row_num, 4, resultSheet);
			        				pixelTotalSize = pixelTotalSize + eventTotalSize;
			        			}
			        		}
			        		System.out.println(((row_num-1)-(pixelTotalSize-1)) + "," + (row_num-1) + ",2,2");
			        		mergeAndSetBorder(((row_num-1)-(pixelTotalSize-1)), row_num, 3, resultSheet);
			        		mergeAndSetBorder(((row_num-1)-(pixelTotalSize-1)), row_num, 2, resultSheet);
			        		mergeAndSetBorder(((row_num-1)-(pixelTotalSize-1)), row_num, 1, resultSheet);			
			        		mergeAndSetBorder(((row_num-1)-(pixelTotalSize-1)), row_num, 0, resultSheet);
			        	}
		        	}
	        	}
	        }
	    }		 		
		for(int columnIndex = 0; columnIndex <= 7; columnIndex++) {
			resultSheet.autoSizeColumn(columnIndex, true);
		}			
		
		FileOutputStream outputStream = new FileOutputStream(new File(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Pixel_Output\\" + brand + "_" + flow +".xlsx"));
	    workbook.write(outputStream);
	    workbook.close();
	    outputStream.close();
	    attachmentList.add(System.getProperty("user.dir") + "\\Input_Output\\PixelValidation\\Pixel_Output\\" + brand + "_" + flow +".xlsx");
	    System.out.println("pixel_output.xlsx written successfully");
	}
	
	public static void mergeAndSetBorder(int rowindex, int rownum, int columnindex, Sheet sheet) {
		CellRangeAddress region = new CellRangeAddress(rowindex,(rownum-1),columnindex,columnindex);
		if(region.getNumberOfCells() >= 2) {
			sheet.addMergedRegion(region);
		}
		Cell mergedcell = sheet.getRow(rowindex).getCell(columnindex);
		RegionUtil.setBorderBottom(mergedcell.getCellStyle().getBorderBottom(), region, sheet);
		RegionUtil.setBorderTop(mergedcell.getCellStyle().getBorderTop(), region, sheet);
	    RegionUtil.setBorderLeft(mergedcell.getCellStyle().getBorderLeft(), region, sheet);
	    RegionUtil.setBorderRight(mergedcell.getCellStyle().getBorderRight(), region, sheet);
	}
}
