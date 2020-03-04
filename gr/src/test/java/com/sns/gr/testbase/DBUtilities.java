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

	public static Map<String, Object> get_offerdata(String ppid, String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		
		String realm = get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "' and category='" + category + "';";	
		List<Map<String, Object>> offerdata = DBLibrary.dbAction("fetch", query);
		//System.out.println(query);
		return offerdata.get(0);		
	}
	
	public static String get_realm(String brand) throws ClassNotFoundException, SQLException {
		String realmQuery = "select * from brand_realm where brand ='" + brand + "';";
		List<Map<String, Object>> realmResult = DBLibrary.dbAction("fetch", realmQuery);
		String realm = realmResult.get(0).get("realm").toString();
		return realm;
	}
	
	public String getUrl(String brand, String campaign, String env) throws ClassNotFoundException, SQLException {
		String query = "select * from brand where brandname='" + brand + "' and campaign='" + campaign + "';";
		List<Map<String, Object>> branddata = DBLibrary.dbAction("fetch", query);		
		String url = branddata.get(0).get(env.toLowerCase() + "url").toString();
		return url;
	}
	
	public List<String> get_unique_categories(String brand, String campaign) throws ClassNotFoundException, SQLException {
		String query = "select distinct category from r4offers where brand='" + brand + "' and campaign='" + campaign + "';";
		List<Map<String, Object>> unique_categories = DBLibrary.dbAction("fetch", query);
		
		List<String> categories = new ArrayList<String>();
		for(Map<String, Object> category : unique_categories) {
			String catg = category.get("category").toString();
			categories.add(catg);
		}
		return categories;
	}
	
	public List<String> getCategory(String brand, String campaign, String ppid) throws ClassNotFoundException, SQLException {
		String realm = get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";	
		
		String query = "select distinct category from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and ppid='" + ppid + "';";
		List<Map<String, Object>> unique_categories = DBLibrary.dbAction("fetch", query);
		
		List<String> categories = new ArrayList<String>();
		for(Map<String, Object> category : unique_categories) {
			String catg = category.get("category").toString();
			categories.add(catg);
		}
		return categories;
	}
	
	public List<Map<String, Object>> fetch_all_by_category(String brand, String campaign, String category) throws ClassNotFoundException, SQLException {
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";
		List<Map<String, Object>> data = null;
		if(category.equalsIgnoreCase("subscribe")) {
			String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and subscribe='Yes';";	
			data = DBLibrary.dbAction("fetch", query);
		}
		else {
			String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='" + category + "' and status='Active';";	
			data = DBLibrary.dbAction("fetch", query);
		}
		return data;
	}
	
	public List<Map<String, Object>> fetch_all_30day_kits(String brand, String campaign) throws ClassNotFoundException, SQLException {	
		String realm = DBUtilities.get_realm(brand);
		String tableName = realm.toLowerCase() + "offers";
		String supply;
		if(realm.equalsIgnoreCase("R4")) {
			supply = "supply";
		}
		else {
			supply = "supplysize";
		}
		
		String query = "select * from " + tableName + " where brand='" + brand + "' and campaign='" + campaign + "' and category='kit' and " + supply + "='30';";	
		List<Map<String, Object>> data = DBLibrary.dbAction("fetch", query);
		return data;
	}
	
	public static List<String> fetch_r2_pricing(String ppid) throws ClassNotFoundException, SQLException{
		List<String> priceArr = new ArrayList<String>();
		String query = "select * from r2offers where ppid='" + ppid + "';";
		List<Map<String, Object>> data = DBLibrary.dbAction("fetch", query);
		System.out.println(query);
		priceArr.add(data.get(0).get("contipricing").toString());
		priceArr.add(data.get(0).get("contishipping").toString());
		priceArr.add(data.get(0).get("entrypricing").toString());
		priceArr.add(data.get(0).get("entryshipping").toString());
		
		return priceArr;
	}
	
	public String checkPPUPresent(String brand, String campaign) throws ClassNotFoundException, SQLException {
		String campQuery = "select * from campaign_pages where brand='" + brand + "' and campaign='" + campaign + "';";
		List<Map<String, Object>> camplist = DBLibrary.dbAction("fetch", campQuery);
		Map<String, Object> map = camplist.get(0);
		String ppupresent;
		if(map.get("upsellpage").toString().equalsIgnoreCase("Yes")) {
			ppupresent = "Yes";
		}
		else {
			ppupresent = "No";
		}
		return ppupresent;
	}
	
	// Pixel Validation
	public List<String> getAllEvents(String pixel) throws ClassNotFoundException, SQLException {
		String query = "select * from pixels where pixelname='" + pixel + "';";
		List<Map<String, Object>> pixeldata = DBLibrary.dbAction("fetch",query);	
		
		List<String> events = new ArrayList<String>();
		for(Map<String, Object> entry :pixeldata) {
			String name = entry.get("eventname").toString();
			events.add(name);
		}
		return events;
	}
	
	public int checkBrandPixelCompatibility(String brand, String event) throws ClassNotFoundException, SQLException {
				
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "';";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);		
				
		return joinlist.size();
	}
	
	public List<String> getPages(String brand, String campaign) throws ClassNotFoundException, SQLException {
		
		String campQuery = "select * from campaign_pages where brand='" + brand + "' and campaign='" + campaign + "';";
		List<Map<String, Object>> camplist = DBLibrary.dbAction("fetch", campQuery);
		Map<String, Object> map = camplist.get(0);
		
		List<String> campaignPageList = new ArrayList<String>();
		if(map.get("homepage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("homepage");
		}
		if(map.get("saspage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("saspage");
		}
		if(map.get("duopage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("duopage");
		}
		if(map.get("checkoutpage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("checkoutpage");
		}
		if(map.get("paypalreviewpage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("paypalreviewpage");
		}
		if(map.get("upsellpage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("upsellpage");
		}
		if(map.get("confirmationpage").toString().equalsIgnoreCase("Yes")) {
			campaignPageList.add("confirmationpage");
		}		
		return campaignPageList;
	}
	
	public List<String> getFiringPages(String brand, String campaign, String flow, String pixel, String event) throws ClassNotFoundException, SQLException {

		List<String> campaignPageList = getPages(brand, campaign);
								
		String pixelQuery = "select * from pixels where pixelname='" + pixel + "' and eventname='" + event + "';";
		List<Map<String, Object>> pixellist = DBLibrary.dbAction("fetch", pixelQuery);
		String pages = pixellist.get(0).get("firingpages").toString();
		
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
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "';";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);
		String pattern = joinlist.get(0).get("searchpattern").toString();
		return pattern;
	}
	
	public String getPixelBrandId(String brand, String event) throws ClassNotFoundException, SQLException {
		String joinquery = "select * from brand_pixel where brand='" + brand + "' and event='" + event + "';";
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",joinquery);
		String id = joinlist.get(0).get("pixelbrandid").toString();
		return id;
	}
	
	public String getdescription(String brand, String campaign, String ppid, String realm) throws ClassNotFoundException, SQLException {
		if(ppid.contains("single")) {
		String[] arr = ppid.split(",");
		ppid = arr[0];
		}
		String query = "select * from "+realm+"offers where brand = '"+brand+"' and campaign = '"+campaign+"' and ppid = '"+ppid+"';";
		System.out.println(query);
		List<Map<String, Object>> joinlist = DBLibrary.dbAction("fetch",query);
		String description = joinlist.get(0).get("description").toString();
		System.out.println(description);
		return description;
	}
}
