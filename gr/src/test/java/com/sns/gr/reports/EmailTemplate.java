package com.sns.gr.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sns.gr.testbase.CommonUtilities;

public class EmailTemplate {
	static CommonUtilities commObj = new CommonUtilities();
	
	public static void main(String[] args) throws IOException {
		
		File file = new File("F:\\Automation\\Buyflow\\DailyOrders\\Run Output\\" + commObj.generateFileName("EmailTemplate") + ".xlsx");
		XSSFWorkbook workbook = null;
		// Check file existence 
	    if (file.exists() == false) {
	        // Create new file if it does not exist
	        workbook = new XSSFWorkbook();
	    } 
	    else {
	        FileInputStream inputStream = new FileInputStream(new File("F:\\Automation\\Buyflow\\DailyOrders\\Run Output\\" + commObj.generateFileName("EmailTemplate") + ".xlsx"));
	        workbook = new XSSFWorkbook(inputStream);
	    }	    
		XSSFSheet Sheet = workbook.createSheet("EmailTemplate");
		
		String[] brands = {"MeaningfulBeauty", "Sub-D", "PrincipalSecret", "ReclaimBotanical", "TryDermaFlash", "SpecificBeauty", "Marajo", "DermaFlash", "SheerCover",
				"Volaire", "CrepeErase", "Smileactives", "Dr.Denese", "Mally", "WestmoreBeauty", "SeaCalm"};		
		
		for(String brand : brands) {
			File input_file = new File("F:\\Automation\\Buyflow\\DailyOrders\\Result.xlsx");
			FileInputStream inputstream = new FileInputStream(input_file);
			Workbook testData = new XSSFWorkbook(inputstream);
			Sheet dataSheet = testData.getSheet(brand);
			Row row;
			List<List<String>> brandBox = new ArrayList<List<String>>();
			
			List<String> brandRow = new ArrayList<String>();
			brandRow.add(brand);
			brandBox.add(brandRow);			
			
			for(int itr = 1; itr <= dataSheet.getLastRowNum(); itr++) {
				row = dataSheet.getRow(itr);
				if(row == null) {
					break;
				}				
				List<String> brandData = new ArrayList<String>();
				
				String ppid = row.getCell(3).getStringCellValue();
				String confNum = row.getCell(4).getStringCellValue();
				String EmailReceived = row.getCell(5).getStringCellValue();
				
				brandData.add(ppid);
				brandData.add(confNum);
				brandData.add(EmailReceived);
				brandBox.add(brandData);
			}			
			generateEmailTemplate(brandBox, workbook, Sheet);			
		}
		FileOutputStream outputStream = new FileOutputStream(new File("F:\\Automation\\Buyflow\\DailyOrders\\Run Output\\" + commObj.generateFileName("EmailTemplate") + ".xlsx"));
	    workbook.write(outputStream);
	    workbook.close();
	    outputStream.close();
	}
	
	public static void generateEmailTemplate(List<List<String>> brandBox, XSSFWorkbook workbook, XSSFSheet Sheet) {
		System.out.println(brandBox.get(0).get(0));
		List<Integer> excelNum = getRowAndColumnNumber(workbook, Sheet, brandBox.size());
		int rowNum = excelNum.get(0);
		int columnNum = excelNum.get(1);
		System.out.println(rowNum+","+columnNum);
		int rowMerge = rowNum;
		int columnMerge = columnNum;
		
		XSSFCellStyle LeftBorder_cellstyle = workbook.createCellStyle();
		LeftBorder_cellstyle.setBorderLeft(BorderStyle.THIN);
		
		XSSFCellStyle RightBorder_cellstyle = workbook.createCellStyle();
		RightBorder_cellstyle.setBorderRight(BorderStyle.THIN);
		
		XSSFCellStyle BottomBorder_cellstyle = workbook.createCellStyle();
		BottomBorder_cellstyle.setBorderBottom(BorderStyle.THIN);
		
		XSSFRow newRow = null;	
		
		if((columnNum == 0)) {
			for(int k=rowNum ; k <= (rowNum+(brandBox.size())) ; k++) {
				newRow = Sheet.createRow(k);
			}	
		}
		
		for(List<String> list : brandBox) {
			String rowExists = checkRowExists(Sheet, rowNum);
			if(rowExists.equalsIgnoreCase("true")) {
				newRow = Sheet.getRow(rowNum);
			}
			else {
				newRow = Sheet.createRow(rowNum);
			}
			
			columnNum = excelNum.get(1);
			for(String value : list) {
				Cell cell = newRow.createCell(columnNum);
				cell.setCellValue(value);
						
				if(rowNum == (brandBox.size()-1)) {
					cell.setCellStyle(LeftBorder_cellstyle);
					cell.setCellStyle(RightBorder_cellstyle);
					cell.setCellStyle(BottomBorder_cellstyle);
				}
				else if(rowNum > rowMerge) {
					if(columnNum == columnMerge) {
						cell.setCellStyle(LeftBorder_cellstyle);
					}
					else if(columnNum == (columnMerge+2)) {
						cell.setCellStyle(RightBorder_cellstyle);
					}
				}	
				columnNum++;
			}			
			rowNum++;
		}
		
		for(int i=0; i<columnNum; i++) {
			Sheet.autoSizeColumn(i);
		}
		CellRangeAddress region = new CellRangeAddress(rowMerge, rowMerge, columnMerge, columnMerge+2);
		Sheet.addMergedRegion(region);
		
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, Sheet);
		RegionUtil.setBorderTop(BorderStyle.THIN, region, Sheet);
	    RegionUtil.setBorderLeft(BorderStyle.THIN, region, Sheet);
	    RegionUtil.setBorderRight(BorderStyle.THIN, region, Sheet);
	    	    
	    int mergedRow = region.getFirstRow();
	    int mergedColumn = region.getFirstColumn();
//	    
	    Cell mergedcell = Sheet.getRow(mergedRow).getCell(mergedColumn);
//	    
	    XSSFCellStyle cellstyle = workbook.createCellStyle();
//	    cellstyle.setBorderLeft(BorderStyle.THIN);
//		cellstyle.setBorderRight(BorderStyle.THIN);
//		cellstyle.setBorderTop(BorderStyle.THIN);
//		cellstyle.setBorderBottom(BorderStyle.THIN);
		cellstyle.setAlignment(HorizontalAlignment.CENTER);
//		
		mergedcell.setCellStyle(cellstyle);
	}
		
	public static String checkRowExists(XSSFSheet Sheet, int rowNum) {
		String rowExists = "false";
		int lastRowNum = Sheet.getLastRowNum();
		if(rowNum < lastRowNum) {
			rowExists = "true";
		}
		else {
			rowExists = "false";
		}		
		return rowExists;
	}
	
	static int nextRowNum = 0;
	
	public static List<Integer> getRowAndColumnNumber(XSSFWorkbook workbook, XSSFSheet Sheet, int height) {
		System.out.println("Height" + height);
		List<Integer> excelNum = new ArrayList<Integer>();
		int lastRowNum = Sheet.getLastRowNum();
				
		if(lastRowNum == 0) {
			excelNum.add(0);
			excelNum.add(0);
			nextRowNum = nextRowNum + height + 1;
			System.out.println("Next Row Number" + nextRowNum);
		}
		else {			
			int lastCellNum = Sheet.getRow(1).getPhysicalNumberOfCells();
			System.out.println("Last Cell Number" + lastCellNum);			
					
			if((nextRowNum + height) <= 35) {
				if(lastCellNum == 3) {
					excelNum.add(nextRowNum);
					excelNum.add(0);
				}
				else if(lastCellNum == 6) {
					excelNum.add(nextRowNum);
					excelNum.add(4);
				}
				else if(lastCellNum == 9) {
					excelNum.add(nextRowNum);
					excelNum.add(7);
				}
				else if(lastCellNum == 12) {
					excelNum.add(nextRowNum);
					excelNum.add(10);
				}

				nextRowNum = nextRowNum + height + 1;
				System.out.println("Next Row Number" + nextRowNum);
			}
			else {
				nextRowNum = 0;
				
				if(lastCellNum == 3) {
					excelNum.add(0);
					excelNum.add(4);
				}
				else if(lastCellNum == 6) {
					excelNum.add(0);
					excelNum.add(7);
				}
				else if(lastCellNum == 9) {
					excelNum.add(0);
					excelNum.add(10);
				}
				else if(lastCellNum == 12) {
					excelNum.add(0);
					excelNum.add(13);
				}
				
				nextRowNum = nextRowNum + height + 1;
				System.out.println("Next Row Number" + nextRowNum);
			}			
		}
		return excelNum;
	}
}
