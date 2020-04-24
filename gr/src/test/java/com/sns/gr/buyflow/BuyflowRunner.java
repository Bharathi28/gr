package com.sns.gr.buyflow;

import org.testng.TestNG;

public class BuyflowRunner {

	static TestNG testng;

	public static void main(String[] args) {
		
		testng = new TestNG();
		
		testng.setTestClasses(new Class[] {BuyflowValidation.class});
		testng.setDataProviderThreadCount(5);
		testng.run();

	}
}
