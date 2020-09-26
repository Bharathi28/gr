package com.sns.gr.testbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sns.gr.testbase.DBLibrary;

public class CommonUtilities {
	
	public String[][] getExcelData(String fileName, String sheetName) {
		String[][] arrayExcelData = null;
		try {			
			File input_file = new File(fileName);
			
			FileInputStream inputstream = new FileInputStream(input_file);
			Workbook testData = new XSSFWorkbook(inputstream);
			Sheet dataSheet = testData.getSheet(sheetName);
			
			int totalNoOfCols = dataSheet.getRow(0).getLastCellNum();
//			int totalNoOfRows = dataSheet.getLastRowNum() +1;
//			int totalNoOfRows = dataSheet.getLastRowNum();
			
			int end = 0;		
			
			//////////////////////////////////////
			int k = 1;
			String rowdata = dataSheet.getRow(k).getCell(0).getStringCellValue();
			while(!(rowdata.equalsIgnoreCase("End"))) {
				k++;
				rowdata =  dataSheet.getRow(k).getCell(0).getStringCellValue();
			}
			int totalNoOfRows = k;
//			System.out.println("Total rows:" + totalNoOfRows);
			
			//////////////////////////////////////
						
			arrayExcelData = new String[totalNoOfRows-1][totalNoOfCols];
			
			for (int i= 1 ; i < totalNoOfRows; i++) {
				for (int j=0; j < totalNoOfCols; j++) {
					String cellType = dataSheet.getRow(i).getCell(j).getCellTypeEnum().toString();
					if(cellType.equalsIgnoreCase("STRING")) {
						arrayExcelData[i-1][j] = dataSheet.getRow(i).getCell(j).getStringCellValue();
						if(arrayExcelData[i-1][j].equalsIgnoreCase("End")) {
							end = 1;
							break;
						}
					}
					else if(cellType.equalsIgnoreCase("NUMERIC")) {
						Double value = dataSheet.getRow(i).getCell(j).getNumericCellValue();
						arrayExcelData[i-1][j] = Double.toString(value);
					}
//					System.out.println(arrayExcelData[i-1][j]);
				}
				if(end == 1) {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return arrayExcelData;
	}	
	
	public synchronized void write_output(List<List<String>> output, String brand, String campaign, String fileName, String filePath){
		try {
			File file = new File(filePath + generateFileName(fileName) + ".xlsx");
			XSSFWorkbook workbook = null;
			// Check file existence 
		    if (file.exists() == false) {
		        // Create new file if it does not exist
		        workbook = new XSSFWorkbook();
		    } 
		    else {
		        FileInputStream inputStream = new FileInputStream(new File(filePath + generateFileName(fileName) + ".xlsx"));
		        workbook = new XSSFWorkbook(inputStream);
		    }
		    
			XSSFSheet resultSheet = null;
			
			// Check if the workbook is empty or not
		    if (workbook.getNumberOfSheets() != 0) {
		    	String sheetExists = checkSheetExists(workbook, brand);
		    	if(sheetExists.equalsIgnoreCase("true")) {
		    		resultSheet = workbook.getSheet(brand);
		    		output.remove(0);
		    	}
		    	else {
		    		resultSheet = workbook.createSheet(brand);
		    	}
		    }
		    else {
		    	resultSheet = workbook.createSheet(brand);
		    }
		    
		    int lastRowNum = resultSheet.getLastRowNum();
		    int newRowNum;
		    if(lastRowNum == 0) {
		    	newRowNum = lastRowNum;
		    }
		    else {
		    	newRowNum = lastRowNum + 1;
		    }
			XSSFRow newRow;		
			for(int i=0; i<output.size(); i++) {
				newRow = resultSheet.createRow(newRowNum);
				for(int j=0; j<output.get(i).size(); j++) {
					Cell cell = newRow.createCell(j);
					cell.setCellValue(output.get(i).get(j));
				}
				newRowNum++;
			}
			
			int col_count = output.get(0).size();
			for(int columnIndex = 0; columnIndex < col_count; columnIndex++) {
				resultSheet.autoSizeColumn(columnIndex);
			}
			FileOutputStream outputStream = new FileOutputStream(new File(filePath + generateFileName(fileName) + ".xlsx"));
		    workbook.write(outputStream);
		    workbook.close();
		    outputStream.close();
		    System.out.println(generateFileName(fileName) + ".xlsx written successfully");
		}
		catch (IOException e)
		{
			
		}
	}
	
	public String populateOutputExcel(List<List<String>> output, String fileName, String filePath) throws IOException {
		
		File file = new File(filePath + generateFileName(fileName) + ".xlsx");
		XSSFWorkbook workbook = null;
		// Check file existence 
	    if (file.exists() == false) {
	        // Create new file if it does not exist
	        workbook = new XSSFWorkbook();
	    } 
	    else {
	        FileInputStream inputStream = new FileInputStream(new File(filePath + generateFileName(fileName) + ".xlsx"));
	        workbook = new XSSFWorkbook(inputStream);
	    }
	    
		XSSFSheet resultSheet = null;
		
		for(List<String> row : output) {
			String brand = row.get(1);
			String campaign = row.get(2);
			
			// Check if the workbook is empty or not
		    if (workbook.getNumberOfSheets() != 0) {
		    	String sheetExists = checkSheetExists(workbook, brand);
		    	if(sheetExists.equalsIgnoreCase("true")) {
		    		resultSheet = workbook.getSheet(brand);
		    	}
		    	else {
		    		resultSheet = workbook.createSheet(brand);
		    		resultSheet = setHeader(resultSheet, fileName);
		    	}
		    }
		    else {
		    	resultSheet = workbook.createSheet(brand);
		    	resultSheet = setHeader(resultSheet, fileName);
		    }
		    
		    int lastRowNum = resultSheet.getLastRowNum();
		    int newRowNum = lastRowNum + 1;
			XSSFRow newRow = resultSheet.createRow(newRowNum);
			for(int j=0; j<row.size(); j++) {
				Cell cell = newRow.createCell(j);
				cell.setCellValue(row.get(j));
			}
			
			int col_count = output.get(0).size();
			for(int columnIndex = 0; columnIndex < col_count; columnIndex++) {
				resultSheet.autoSizeColumn(columnIndex);
			}
		}
		FileOutputStream outputStream = new FileOutputStream(new File(filePath + generateFileName(fileName) + ".xlsx"));
	    workbook.write(outputStream);
	    workbook.close();
	    outputStream.close();
	    System.out.println(generateFileName(fileName) + ".xlsx written successfully");
	    
	    return filePath + generateFileName(fileName) + ".xlsx";
	}
	
	public XSSFSheet setHeader(XSSFSheet resultSheet, String header){
		List<String> header_list = new ArrayList<String>();
		
		if(header.toLowerCase().contains("buyflow")) {
			header_list.add("Environment");
			header_list.add("Brand");
			header_list.add("Campaign");
			header_list.add("Category");
			header_list.add("e-mail");
			header_list.add("Expected PPID");
			header_list.add("Actual PPID");
			header_list.add("Confirmation Number");
			header_list.add("Checkout Pricing");		
			header_list.add("Confirmation Pricing");
			header_list.add("Shipping Billing");
			header_list.add("Card");
			header_list.add("Browser");
			
//			header_list.add("Environment");
//			header_list.add("Brand");
//			header_list.add("Campaign");
//			header_list.add("Category");
//			header_list.add("e-mail");
//			header_list.add("PPID");			
//			header_list.add("Confirmation Number");
//			header_list.add("Entry Pricing");	
//			header_list.add("Continuity Pricing");
//			header_list.add("Renewal Plan");	
//			header_list.add("Installment Plan");
//			header_list.add("Shipping Billing");
//			header_list.add("Payment Method");
//			header_list.add("Browser");
//			header_list.add("Remarks");
		}	
		else if(header.toLowerCase().contains("pixel")) {
			header_list.add("Environment");
			header_list.add("Brand");
			header_list.add("Campaign");
			header_list.add("e-mail");
			header_list.add("Expected PPID");
			header_list.add("Actual PPID");
			header_list.add("Confirmation Number");
		}
		else if(header.toLowerCase().contains("cartlang")) {
			header_list.add("Environment");
			header_list.add("Brand");
			header_list.add("Campaign");
			header_list.add("PPID");
			header_list.add("Cart Language Price");
			header_list.add("Cart Language Shipping");
			header_list.add("Checkout Subtotal");
			header_list.add("Checkout Shipping");
			header_list.add("Result");
			header_list.add("Remarks");
		}	
		else if(header.toLowerCase().contains("cxt")) {
			header_list.add("Environment");
			header_list.add("Brand");
			header_list.add("Campaign");
			header_list.add("Testcase");
			header_list.add("Result");
			header_list.add("Remarks");
		}
		
		XSSFRow firstRow = resultSheet.createRow(0);
		for(int j=0; j<header_list.size(); j++) {
			Cell cell = firstRow.createCell(j);
			cell.setCellValue(header_list.get(j));
		}
		return resultSheet;
	}
	
	public String checkSheetExists(XSSFWorkbook workbook, String sheetName) {
		String sheetExists = "false";
		int noOfSheets = workbook.getNumberOfSheets();
		for(int i=0; i<noOfSheets; i++) {
			if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {
				sheetExists = "true";
				break;
			}
		}
		return sheetExists;
	}
	
	public String generateFileName(String fileName) {
		Calendar now = Calendar.getInstance();		
		String monthStr = Integer.toString(now.get(Calendar.MONTH) + 1); // Note: zero based!
		String dayStr = Integer.toString(now.get(Calendar.DAY_OF_MONTH));  
		String yearStr = Integer.toString(now.get(Calendar.YEAR));
		
		String filename = fileName + "_" + monthStr + dayStr + yearStr;
		return filename;
	}
	
	public List<Map<String, Object>> get_element_locator(String brand, String campaign, String step, String offer) throws ClassNotFoundException, SQLException {
		
		String origcampaign = campaign_repeat(brand, campaign, "locators");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}	
		
		String query = "select * from locators_new where ";
		String include_brand = "brand='" + brand + "'";
		String include_campaign = "campaign='" + campaign + "'";
		String include_step = "step='" + step + "'";
		String include_offer = "offer='" + offer + "'";
			
		if(brand != null) {
			query = query + include_brand;
			if((campaign != null) || (step != null) || (offer != null)) {
				query = query + " and ";
			}
		}
		if(campaign != null) {
			query = query + include_campaign;
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
//		query = query + ";";
			System.out.println(query);
		List<Map<String, Object>> locator = DBLibrary.dbAction("fetch",query);
		return locator;		
	}
	
	public String campaign_repeat(String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		String origcampaign = "n/a";
		String query = "select * from campaign_repeat where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> campdetails = DBLibrary.dbAction("fetch",query);
		while(campdetails.size()==1) {
			if(category.equalsIgnoreCase("locators")) {
				origcampaign = campdetails.get(0).get("LOCATORS_SAMEAS").toString();
			}
			else if(category.equalsIgnoreCase("offers")) {
				origcampaign = campdetails.get(0).get("OFFERS_SAMEAS").toString();
			}
			else if(category.equalsIgnoreCase("pages")) {
				origcampaign = campdetails.get(0).get("PAGES_SAMEAS").toString();
			}
			query = "select * from campaign_repeat where brand='" + brand + "' and campaign='" + origcampaign + "'";
			campdetails = DBLibrary.dbAction("fetch",query);
		}
		return origcampaign;
	}
	
	public WebElement find_webelement(WebDriver driver, String elementlocator, String elementvalue) {
		WebElement element = null;
		switch(elementlocator){  
	    	case "id":
	    		element = driver.findElement(By.id(elementvalue));
	    		break;  
	    	case "name":
	    		element = driver.findElement(By.name(elementvalue));
	    		break;  
	    	case "xpath":
	    		element = driver.findElement(By.xpath(elementvalue));
	    		break;
	    	case "classname":
	    		element = driver.findElement(By.className(elementvalue));
	    		break;
	    	case "cssselector":
	    		element = driver.findElement(By.cssSelector(elementvalue));
	    		break;
	    }
		return element;  
	}
	
	public List<WebElement> find_mulwebelement(WebDriver driver, String elementlocator, String elementvalue) {
		List<WebElement> element = new ArrayList<WebElement>();
		switch(elementlocator){  
	    	case "id":
	    		element = driver.findElements(By.id(elementvalue));
	    		break;  
	    	case "name":
	    		element = driver.findElements(By.name(elementvalue));
	    		break;  
	    	case "xpath":
	    		element = driver.findElements(By.xpath(elementvalue));
	    		break;
	    	case "classname":
	    		element = driver.findElements(By.className(elementvalue));
	    		break;
	    	case "cssselector":
	    		element = driver.findElements(By.cssSelector(elementvalue));
	    		break;
	    }
		return element;  
	}
	
	public void waitUntilElementAppears(WebDriver driver, String xpath) {
		while(driver.findElements(By.xpath(xpath)).size() == 0) {
			// Do Nothing
		}
	}
}
