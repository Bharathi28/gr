package com.sns.gr.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CurrentActiveCampaigns {
	
	public static void main(String[] args) throws IOException {
		System.setProperty("webdriver.chrome.driver", "F:\\Automation\\Drivers\\chromedriver_win32\\chromedriver.exe");
		
		File input_file = new File("F:\\Automation\\Active Campaigns\\Current Active Campaigns - Daily\\Current Active Campaigns.xlsx");
		FileInputStream inputstream = new FileInputStream(input_file);
		Workbook testData = new XSSFWorkbook(inputstream);
		Sheet dataSheet = testData.getSheet("Sheet1");
		
		List<HashMap<String, List<String>>> overallOutput = new ArrayList<HashMap<String, List<String>>>();
		
//		System.out.println(dataSheet.getLastRowNum());
		for(int itr=0; itr<dataSheet.getLastRowNum() ; itr++) {
			Row row = dataSheet.getRow(itr);
			if(row == null) {
				continue;
			}
			String type = row.getCell(0).getCellTypeEnum().toString();
			String value = row.getCell(0).getStringCellValue();
//			System.out.println("Type,Value : " + type + " " + value);
			
			String brand = "";
			String campaign = "";
			if(value.toLowerCase().contains("pathing by campaign")) {
				HashMap<String, List<String>> brandMap= new LinkedHashMap<String, List<String>>();
				List<String> activeCampaigns = new ArrayList<String>();
				
				itr++;
				Row nextrow = dataSheet.getRow(itr);
				String nextvalue = nextrow.getCell(0).getStringCellValue();
//				System.out.println(nextvalue);
				
				while((!(nextvalue.toLowerCase().contains("pathing by campaign"))) && (!(nextvalue.toLowerCase().contains("end")))){
					
					String[] arr = nextvalue.split(":");
					campaign = arr[0];
					if(campaign.toLowerCase().contains("cat")) {
						campaign = campaign.replace("cat-", "");
					}
					
					brand = arr[1];
					if(brand.equalsIgnoreCase("df")) {
						brand="DermaFlash";
					}
					else if(brand.equalsIgnoreCase("sb")) {
						brand="SpecificBeauty";
					}
					else if(brand.equalsIgnoreCase("cp")) { 
						brand="Sub-D";
					}
					else if(brand.equalsIgnoreCase("mallybeauty")) {
						brand="Mally";
					}					
					if(!(activeCampaigns.contains(campaign))) {
						activeCampaigns.add(campaign);
					}
										
					itr++;
					nextrow = dataSheet.getRow(itr);
					if(nextrow == null) {
						continue;
					}
					nextvalue = nextrow.getCell(0).getStringCellValue();
//					System.out.println(nextvalue);
				}
				
				brandMap.put(brand, activeCampaigns);
				System.out.println(brandMap);
				overallOutput.add(brandMap);
				itr--;
			}
		}
//		writeToSheet(overallOutput, String fileName, String sheetName)
	}
	
	public static void writeToSheet(HashMap map, String fileName, String sheetName) throws IOException {	
		
	}
}
