package com.sns.gr.testbase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

public class MailUtilities {
	
	public void sendEmail(String subject, String to, List<String> attachmentList) {
    	Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		
		final String username = "automation@searchnscore.com";
		final String password = "SNSPassw0rd!";
		String from = "automation@searchnscore.com";
		
		StringBuffer sb = new StringBuffer();
		sb.append("Hi Team,").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("PFA.").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("Thanks");		
		
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			  protected PasswordAuthentication getPasswordAuthentication() {
			       return new PasswordAuthentication(username, password);
			  }
	    });
		
		try {			
			Date date = new Date();
	 		SimpleDateFormat date_form = new SimpleDateFormat("MMddyyyy");
	 		String dateStr = date_form.format(date);
	 		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.addRecipients(Message.RecipientType.CC, 
	                InternetAddress.parse("manibharathi@searchnscore.com, banuchitra@searchnscore.com, aaqil@searchnscore.com"));
//			message.addRecipients(Message.RecipientType.CC, 
//	                InternetAddress.parse("manibharathi@searchnscore.com"));
			message.setSubject(subject + " - " + dateStr);

			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setText(sb.toString());
	        
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        messageBodyPart = new MimeBodyPart();	        
	 		
	        // Add Attachments
	        for(String filename : attachmentList) {
	        	messageBodyPart = new MimeBodyPart();
	            DataSource source = new FileDataSource(filename);
	            messageBodyPart.setDataHandler(new DataHandler(source));
	            messageBodyPart.setFileName(filename);
	            multipart.addBodyPart(messageBodyPart);
	        }	        
	        message.setContent(multipart);
	         
			Transport.send(message);
			System.out.println("Message delivered successfully");
		}
		catch(MessagingException e) {
			throw new RuntimeException(e);
		}
    }
}
