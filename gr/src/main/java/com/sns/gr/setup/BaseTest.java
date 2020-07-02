package com.sns.gr.setup;

import java.net.MalformedURLException;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

public class BaseTest {

	WebDriver driver;
	
	public WebDriver setUp(@Optional("chrome") String browser, @Optional("grid") String environment) throws MalformedURLException {
		System.out.println(browser);
		System.out.println(environment);
		// Create Driver
		BrowserDriverFactory factory = new BrowserDriverFactory(browser);
		if (environment.equalsIgnoreCase("grid")) {
			driver = factory.createDriverGrid();
		} else {
			driver = factory.createDriver();
		}
		return driver;
	}

	public void tearDown() {
		// Closing driver
		driver.quit();
	}
}
