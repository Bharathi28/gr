package com.sns.gr.pixel;

import org.testng.TestNG;

public class PixelRunner {
	static TestNG testng;

	public static void main(String[] args) {
		
		testng = new TestNG();
		
		testng.setTestClasses(new Class[] {PixelParallel.class});
		testng.setDataProviderThreadCount(5);
		testng.run();
	}

}
