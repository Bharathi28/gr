package com.sns.gr.testbase;

import java.util.ArrayList;
import java.util.List;

public class ReportUtilities {
	
	public void generateBuyflowReport() {
		List<String> header_list = new ArrayList<String>();
		header_list.add("Environment");
		header_list.add("Brand");
		header_list.add("Campaign");
		header_list.add("URL");
		header_list.add("e-mail");
		header_list.add("Offercode");
		header_list.add("Confirmation number");
		header_list.add("Checkout Pricing");
		header_list.add("Conf Pricing");
		
//		output.add(header_list);
	}

}
