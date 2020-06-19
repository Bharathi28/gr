package com.sns.gr.testbase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.WebDriver;

import com.sns.gr.testbase.DBLibrary;

public class DBUtilities {
	static CommonUtilities comm_obj = new CommonUtilities();

	public static Map<String, Object> get_offerdata(String ppid, String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String realm = get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "' and category='" + category + "' and status ='Active'";	
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);
		System.out.println(query);
		return offerdata.get(0);		
	}
	
	public String[] get_combo(String brand, String campaign) throws ClassNotFoundException, SQLException {
		String query = "select * from brand_combo where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> combodata = DBLibrary.dbAction("fetch", query);	
		String[] brandArr = null;
		if(combodata.size() > 0) {
			String data = combodata.get(0).get("COMBOLIST").toString();
			brandArr = data.split(",");
		}		
		return brandArr;
	}
	
	public static String get_realm(String brand) throws ClassNotFoundException, SQLException {
		String realmQuery = "select * from brand_realm where brand ='" + brand + "'";
		List<Map<String, Object>> realmResult = DBLibrary.dbAction("fetch", realmQuery);
		String realm = realmResult.get(0).get("REALM").toString();
		return realm;
	}
	
	public String getUrl(String brand, String campaign, String env) throws ClassNotFoundException, SQLException {
		String query = "select * from brand where brandname='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> branddata = DBLibrary.dbAction("fetch", query);		
		String url = "";
		if(env.toLowerCase().contains("dev")) {
			url = branddata.get(0).get("STGURL").toString();
			url = url.replace(".stg.", "."+ env.toLowerCase() +".");
		}
		else {
			url = branddata.get(0).get(env.toUpperCase() + "URL").toString();
		}		
		return url;
	}
	
	public String getPageUrl(String brand, String campaign, String page, String env) throws ClassNotFoundException, SQLException {
		String query = "select * from page_urls where brand='" + brand + "' and campaign='" + campaign + "' and page='" + page + "'";
		List<Map<String, Object>> pagedata = DBLibrary.dbAction("fetch", query);		
		String url = "";
		if(env.toLowerCase().contains("dev")) {
			url = pagedata.get(0).get("STGURL").toString();
			url = url.replace(".stg.", "."+ env.toLowerCase() +".");
		}
		else {
			url = pagedata.get(0).get(env.toUpperCase() + "URL").toString();
		}		
		return url;
	}
	
	public List<String> get_unique_categories(String brand, String campaign) throws ClassNotFoundException, SQLException {
		String query = "select distinct category from r4offers where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> unique_categories = DBLibrary.dbAction("fetch", query);
		
		List<String> categories = new ArrayList<String>();
		for(Map<String, Object> category : unique_categories) {
			String catg = category.get("CATEGORY").toString();
			categories.add(catg);
		}
		return categories;
	}
	
	public String isProduct(String brand, String ppid) throws ClassNotFoundException, SQLException {
		String realm = get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
		String isproduct = "No";
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and ppid='" + ppid + "'";
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);
		String category = offerdata.get(0).get("CATEGORY").toString();
		if(category.equalsIgnoreCase("Product")) {
			isproduct = "Yes";
		}
		return isproduct;
	}
	
	public String isShopKit(String brand, String ppid) throws ClassNotFoundException, SQLException {
		String realm = get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
		String isshopkit = "No";
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and ppid='" + ppid + "'";
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);
		
		for(Map<String, Object> map : offerdata) {
			String category = map.get("CATEGORY").toString();
			if(category.equalsIgnoreCase("ShopKit")) {
				isshopkit = "Yes";
				break;
			}
		}		
		return isshopkit;
	}
		
	public List<Map<String, Object>> fetch_all_by_category(String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";
		List<Map<String, Object>> data = null;
		if(category.equalsIgnoreCase("SubscribeandSave")) {
			String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and subscribe='Yes' and status='Active'";		
			data = DBLibrary.dbAction("fetch", query);
		}
		else {
			String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='" + category + "' and status='Active'";	
			data = DBLibrary.dbAction("fetch", query);
		}
		return data;
	}
	
	public List<Map<String, Object>> fetch_all_30day_kits(String brand, String campaign) throws ClassNotFoundException, SQLException {	
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "offers");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";
		String supply;
		if(realm.equalsIgnoreCase("R4")) {
			supply = "supply";
		}
		else {
			supply = "supplysize";
		}
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='kit' and " + supply + "='30' and status='Active'";	
		List<Map<String, Object>> data = DBLibrary.dbAction("fetch", query);
		return data;
	}
	
	public static List<String> fetch_r2_pricing(String ppid) throws ClassNotFoundException, SQLException{
		List<String> priceArr = new ArrayList<String>();
		String query = "select * from r2offers where ppid='" + ppid + "'";
		List<Map<String, Object>> data = DBLibrary.dbAction("fetch", query);
//		System.out.println(query);
		priceArr.add(data.get(0).get("CONTIPRICING").toString());
		priceArr.add(data.get(0).get("CONTISHIPPING").toString());
		priceArr.add(data.get(0).get("ENTRYPRICING").toString());
		priceArr.add(data.get(0).get("ENTRYSHIPPING").toString());
		
		return priceArr;
	}
	
	public String checkPPUPresent(String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "pages");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String campQuery = "select * from campaign_pages where brand='" + brand + "' and campaign='" + campaign + "'";		
		List<Map<String, Object>> camplist = DBLibrary.dbAction("fetch", campQuery);
		Map<String, Object> map = camplist.get(0);
		String ppupresent = null;
		
		if(category.equalsIgnoreCase("Kit")) {
			if(map.get("UPSELLPAGE").toString().equalsIgnoreCase("Yes")) {
				ppupresent = "Yes";
			}
			else {
				ppupresent = "No";
			}
		}
		else if(category.equalsIgnoreCase("ShopKit")) {
			if(map.get("SHOPKITUPSELL").toString().equalsIgnoreCase("Yes")) {
				ppupresent = "Yes";
			}
			else {
				ppupresent = "No";
			}
		}
		return ppupresent;
	}
	
	// Pixel Validation
	public List<String> getAllEvents(String pixel) throws ClassNotFoundException, SQLException {
		String query = "select * from pixels where pixelname='" + pixel + "'";
		List<Map<String, Object>> pixeldata = DBLibrary.dbAction("fetch",query);	
		
		List<String> events = new ArrayList<String>();
		for(Map<String, Object> entry :pixeldata) {
			String name = entry.get("EVENTNAME").toString();
			events.add(name);
		}
		return events;
	}
	
	public int checkBrandPixelCompatibility(String brand, String event) throws ClassNotFoundException, SQLException {
				
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "'";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);		
		return joinlist.size();
	}
	
	public List<String> getPages(String brand, String campaign) throws ClassNotFoundException, SQLException {
		
		String origcampaign = comm_obj.campaign_repeat(brand, campaign, "pages");
		if(!(origcampaign.equals("n/a"))){
			campaign = origcampaign;
		}
		
		String campQuery = "select * from campaign_pages where brand='" + brand + "' and campaign='" + campaign + "'";
		List<Map<String, Object>> camplist = DBLibrary.dbAction("fetch", campQuery);
		Map<String, Object> map = camplist.get(0);
		
		List<String> campaignPageList = new ArrayList<String>();
		if(map.get("HOMEPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("homepage");
		}
		if(map.get("SASPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("saspage");
		}
		if(map.get("DUOPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("duopage");
		}
		if(map.get("CHECKOUTPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("checkoutpage");
		}
		if(map.get("PAYPALREVIEWPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("paypalreviewpage");
		}
		if(map.get("UPSELLPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("upsellpage");
		}
		if(map.get("CONFIRMATIONPAGE").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("confirmationpage");
		}		
		return campaignPageList;
	}
	
	public List<String> getFiringPages(String brand, String campaign, String flow, String pixel, String event) throws ClassNotFoundException, SQLException {

		List<String> campaignPageList = getPages(brand, campaign);
								
		String pixelQuery = "select * from pixels where pixelname='" + pixel + "' and eventname='" + event + "'";
		List<Map<String, Object>> pixellist = DBLibrary.dbAction("fetch", pixelQuery);
		String pages = pixellist.get(0).get("FIRINGPAGES").toString();
		
		String[] pageArr = pages.split(",");
		List<String> pageList = new ArrayList<String>();
		
		for(String value : pageArr) {
			if(value.equalsIgnoreCase("All")) {
				pageList.addAll(campaignPageList);
				if(flow.equalsIgnoreCase("ccflow")) {
					pageList.remove("paypalreviewpage");
				}
			}
			if(value.equalsIgnoreCase("Home")) {
				if(campaignPageList.contains("homepage")) {
					pageList.add("homepage");
				}
			}
			if(value.equalsIgnoreCase("SAS")) {
				if(campaignPageList.contains("saspage")) {
					pageList.add("saspage");
				}
			}
			if(value.equalsIgnoreCase("Checkout")) {
				if(campaignPageList.contains("checkoutpage")) {
					pageList.add("checkoutpage");
				}
			}		
			if(value.equalsIgnoreCase("Checkout/PaypalReview")) {
				if(flow.equalsIgnoreCase("paypalflow")) {
					if(campaignPageList.contains("paypalreviewpage")) {
						pageList.add("paypalreviewpage");
					}
				}	
				else {
					if(campaignPageList.contains("checkoutpage")) {
						pageList.add("checkoutpage");
					}
				}
			}					
			if(value.equalsIgnoreCase("Confirmation")) {
				if(campaignPageList.contains("confirmationpage")) {
					pageList.add("confirmationpage");
				}
			}	
			if(value.equalsIgnoreCase("Upsell/Confirmation")) {
				if(campaignPageList.contains("upsellpage")) {
					pageList.add("upsellpage");
				}
				else {
					pageList.add("confirmationpage");
				}
			}
		}
		return pageList;
	}	
	
	public String getSearchPattern(String brand, String event) throws ClassNotFoundException, SQLException {
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "'";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);
		String pattern = joinlist.get(0).get("SEARCHPATTERN").toString();
		return pattern;
	}
	
	public String getPixelBrandId(String brand, String event) throws ClassNotFoundException, SQLException {
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "'";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);
		String id = joinlist.get(0).get("PIXELBRANDID").toString();
		return id;
	}
	
	public String getdescription(String brand, String campaign, String ppid, String realm) throws ClassNotFoundException, SQLException {
		if(ppid.contains("single")) {
		String[] arr = ppid.split(",");
		ppid = arr[0];
		}
		String query = "select * from "+realm+"offers where brand = '"+brand+"' and campaign = '"+campaign+"' and ppid = '"+ppid+"'";
//		System.out.println(query);
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",query);
		String description = joinlist.get(0).get("DESCRIPTION").toString();
//		System.out.println(description);
		return description;
	}
}
