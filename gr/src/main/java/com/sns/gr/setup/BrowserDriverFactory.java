package com.sns.gr.setup;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

public class BrowserDriverFactory {

	private WebDriver driver;
	private String browser;

	public BrowserDriverFactory(String browser) {
		this.browser = browser.toLowerCase();
	}	

	@SuppressWarnings("deprecation")
	public WebDriver createDriver() {
		System.out.println("Starting " + browser + " locally");	

		// Creating driver
		switch (browser) {
		case "chrome":
			System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/Drivers/chromedriver.exe");
			
			ChromeOptions options = new ChromeOptions();
		    options.addArguments("--ignore-certificate-errors");

		    // configure it as a desired capability
		    DesiredCapabilities capabilities = new DesiredCapabilities();
		    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		    capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		    capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		    
			driver = new ChromeDriver(capabilities);
			driver.manage().window().maximize();
			break;
			
		case "firefox":
			System.setProperty("webdriver.firefox.driver", System.getProperty("user.dir")+"/Drivers/geckodriver.exe");
			driver = new FirefoxDriver();
			driver.manage().window().maximize();
			break;
		}
		return driver;
	}

	public WebDriver createDriverGrid() throws MalformedURLException {
		String hubUrl = "http://192.168.0.22:4444/wd/hub";		
		System.out.println("Starting " + browser + " on grid");
		ChromeOptions chromeOptions = null;
		FirefoxOptions firefoxOptions = null;
		
		// Creating driver
		switch (browser) {
		case "chrome":
			chromeOptions = new ChromeOptions();
	        chromeOptions.setCapability("platform", "WINDOWS");
	        chromeOptions.setCapability("browserName", "chrome");
	        try {
				driver = new RemoteWebDriver(new URL(hubUrl), chromeOptions);
				driver.manage().window().maximize();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
			
		case "firefox":
			firefoxOptions = new FirefoxOptions();
			try {
				driver = new RemoteWebDriver(new URL(hubUrl), firefoxOptions);
				driver.manage().window().maximize();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		}		
		return driver;
	}
}
