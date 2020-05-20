package com.sns.gr.cxt;

import org.testng.TestNG;

public class CXTRunner {
	
	static TestNG testng;

	public static void main(String[] args) {
		
		testng = new TestNG();
		
		testng.setTestClasses(new Class[] {CXTValidation.class});
		testng.setDataProviderThreadCount(5);
		testng.run();
	}

}
