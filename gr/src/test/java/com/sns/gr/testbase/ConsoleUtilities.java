package com.sns.gr.testbase;

import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

public class ConsoleUtilities {	
	DBUtilities db_obj = new DBUtilities();
	BuyflowUtilities bf_obj = new BuyflowUtilities();
	SASUtilities sas_obj = new SASUtilities();
	PricingUtilities pr_obj = new PricingUtilities();
	CommonUtilities comm_obj = new CommonUtilities();
	MailUtilities mailObj = new MailUtilities();	
	public void sendEmail(String subject, String to,StringBuilder strfull) {
    	Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		
		final String username = "automation@searchnscore.com";
		final String password = "snsgr@123";
		String from = "automation@searchnscore.com";
				
		StringBuffer sb = new StringBuffer();
		sb.append("Hi Team,").append(System.lineSeparator());
		
		sb.append("Please find the console errors for the brands in PROD:");
		sb.append(System.lineSeparator());
		sb.append(strfull);
		
		//sb.append("PFA.").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("Thanks");		
		
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			  protected PasswordAuthentication getPasswordAuthentication() {
			       return new PasswordAuthentication(username, password);
			  }
	    });
		
		try {			
			Date date = new Date(0);
	 		SimpleDateFormat date_form = new SimpleDateFormat("MMddyyyy");
	 		//String dateStr = date_form.format(date);
	 		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.addRecipients(Message.RecipientType.CC, 
	                InternetAddress.parse("aaqil@searchnscore.com,manibharathi@searchnscore.com"));
//			message.addRecipients(Message.RecipientType.CC, 
//	                InternetAddress.parse("manibharathi@searchnscore.com"));
			message.setSubject(subject);

			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setText(sb.toString());
	        
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        messageBodyPart = new MimeBodyPart();	        
	 		
	        // Add Attachments
	        /*for(String filename : attachmentList) {
	        	messageBodyPart = new MimeBodyPart();
	            DataSource source = new FileDataSource(filename);
	            messageBodyPart.setDataHandler(new DataHandler(source));
	            messageBodyPart.setFileName(filename);
	            multipart.addBodyPart(messageBodyPart);
	        }	    */    
	        message.setContent(multipart);
	         
			Transport.send(message);
			System.out.println("Message delivered successfully");
		}
		catch(MessagingException e) {
			throw new RuntimeException(e);
		}
    }
	public String getcampaigndetails(WebDriver driver, String brand) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		JavascriptExecutor js = (JavascriptExecutor)driver;
		String campaign1 = null;
		if(realm.equalsIgnoreCase("R4")) {
			String appdata = js.executeScript("return app.variableMap").toString();
			String campaign = appdata.substring(appdata.indexOf("campaignId=")+11);
			campaign1 = campaign.substring(0, campaign.indexOf(","));		
		}
		else if(realm.equalsIgnoreCase("R2")) {
			if(brand.equalsIgnoreCase("TryDermaFlash")) {
				String appdata = js.executeScript("return app.omniMap").toString();
				String campaign = appdata.substring(appdata.indexOf("CampaignID=")+15);
				campaign1 = campaign.substring(0, campaign.indexOf(","));
				if(campaign1.equalsIgnoreCase("one")) {
					campaign1 = "core";
				}
			}
			else {
				String appdata = js.executeScript("return app.omniMap").toString();
				String campaign = appdata.substring(appdata.indexOf("CampaignID=")+11);
				campaign1 = campaign.substring(0, campaign.indexOf("Campaign,"));	
			}
			
		}
		return campaign1;
	}
	
	
	public StringBuilder analyzeLog(WebDriver driver,String brand,String page,StringBuilder str) {
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String message1 = null;
        for (LogEntry entry : logEntries) {
        	if((entry.getLevel().equals(Level.SEVERE))||(entry.getMessage().contains("404"))) {
        		message1 = brand+" - "+page+" - "+format.format(new Date(entry.getTimestamp())) + " " + entry.getLevel() + " " + entry.getMessage();     		
                System.out.println(message1);
                str.append("\n");
                str.append(message1);
        	}      	
        	}
        return str;
        /*if(page.equalsIgnoreCase("Checkoutpage")&&(str.length()>0)) {
        	sendEmail(brand, "banuchitra@searchnscore.com",str,brand);
        	str = new StringBuilder("");  	
    	}*/
    }
	public void selectoffercodekit(WebDriver driver,String brand,String campaign) throws ClassNotFoundException, SQLException, InterruptedException {
		String query = "";
//		if((brand.equalsIgnoreCase("SeaCalmSkin")) || (brand.equalsIgnoreCase("FixMDSkin")) || (brand.equalsIgnoreCase("smileactives")) || ((brand.equalsIgnoreCase("SeaCalmSkin")) && (campaign.equalsIgnoreCase("specialoffer"))) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("TryCrepeErase")) || (brand.equalsIgnoreCase("Volaire")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("WestmoreBeauty"))) {
//			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active' order by RAND() limit " + runs;
//		}
//		else {
//			query = "select * from r2offers where brand='" + brand + "' and campaign='" + campaign + "' and category='kit' and status='Active' order by RAND() limit " + runs;
//		}		
		if((brand.equalsIgnoreCase("SeaCalmSkin")) || (brand.equalsIgnoreCase("FixMDSkin")) || (brand.equalsIgnoreCase("Smileactives")) || (brand.equalsIgnoreCase("MeaningfulBeauty")) || (brand.equalsIgnoreCase("Volaire")) || (brand.equalsIgnoreCase("Dr.Denese")) || (brand.equalsIgnoreCase("CrepeErase")) || (brand.equalsIgnoreCase("Mally")) || (brand.equalsIgnoreCase("WestmoreBeauty"))) {
			query = "select * from r4offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}
		else if(brand.equalsIgnoreCase("BodyFirm")) {
			query = "select * from r4offers where brand in ('CrepeErase','SpotFade') and campaign='" + campaign + "' and category='ShopKit' and status='Active'";
		}
		else if(brand.equalsIgnoreCase("BodyFirm-CrepeErase")) {
			query = "select * from r4offers where brand='CrepeErase' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}
		else if(brand.equalsIgnoreCase("BodyFirm-SpotFade")) {
			query = "select * from r4offers where brand='SpotFade' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}
		else {
			query = "select * from r2offers where brand='" + brand + "' and campaign='" + campaign + "' and category='Kit' and status='Active'";
		}	
		List<Map<String, Object>> offers = DBLibrary.dbAction("fetch", query);
		
		List<Map<String, Object>> offerdata = new ArrayList<Map<String, Object>>();
		Random rand = new Random();
		offerdata.add(offers.get(rand.nextInt(offers.size())));
		
		if((brand.equalsIgnoreCase("Mally")) && (campaign.equalsIgnoreCase("Core"))) {
			sas_obj.select_kit(driver, offerdata.get(0), brand, campaign);
        	sas_obj.select_kitshade(driver, offerdata.get(0), brand, campaign);
        	sas_obj.select_duo(driver, offerdata.get(0), brand, campaign);
        }
		else if((brand.equalsIgnoreCase("Dr.Denese")) && (campaign.equalsIgnoreCase("Core"))) {
			sas_obj.select_kit(driver, offerdata.get(0), brand, campaign);
        }
		else {
        	sas_obj.select_offer(driver, "Prod", brand, campaign, offerdata.get(0), "Kit", 0);
        }		
	}

}
