package com.cognizant.autotool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.JSONParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;

import com.cognizant.autotool.JsonUtil;
import com.cognizant.autotool.RestUtil;

import ch.qos.logback.core.net.SyslogOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("unused")
public final class CFUtil {

	private CFUtil() {
		// Helper class constructor.
	}
     
	public static HashMap<String, String> form_headers(String authToken) throws Exception {
		Map<String, String> httpDetail = new HashMap<String, String>();
		httpDetail.put(HttpHeaders.AUTHORIZATION, "bearer " + authToken);
		httpDetail.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		httpDetail.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		return (HashMap<String, String>) httpDetail;
	}
	
	public static String login(String instance) {
		String login_response = null;
		try {
			String loginUrl=null;
			if(instance.equals("cumuluslabs")){
				loginUrl = "https://login.system.cumuluslabs.io/oauth/token?grant_type=password&username=pcfadmin&password=pcfadmin123";
			}else if(instance.equals("digifabricpcf")){
				loginUrl = "https://login.system.dev.digifabricpcf.com/oauth/token?grant_type=password&username=pcfadmin&password=pcfadmin123";
			}
			Map<String, String> httpDetail = new HashMap<String, String>();
			httpDetail.put(HttpHeaders.AUTHORIZATION, "Basic Y2Y6");
			httpDetail.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			httpDetail.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
			login_response = RestUtil.posthttps(loginUrl, httpDetail, StringUtils.EMPTY);
		} catch (Exception exception) {
//			System.out.println("error");
		}
		return JsonUtil.getJsonMap(login_response).get("access_token").toString();
	}
	
//	public static String scriptpart(String accessToken) {
//		try {
//			Process proc = Runtime.getRuntime().exec("sh D:\\scripts\\Hello.sh");
//			BufferedReader read = new BufferedReader(new InputStreamReader(
//                    proc.getInputStream()));
//			try {
//                proc.waitFor();
//            } catch (InterruptedException e) {
//                System.out.println(e.getMessage());
//            }
//            while (read.ready()) {
//                System.out.println(read.readLine());
//            }
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//		}
//		return null;
//		
//	}
	
	public static List<Map<String, Object>> getOrgDetails(String authToken, String target) {
		String org_reponse = null;
		JSONObject parsedvalue = null;
		List<Map<String, Object>> orgDetails = new ArrayList<>();

		try {

			HashMap<String, String> httpDetail = form_headers(authToken);
			org_reponse = RestUtil.gethttps(target + "/v2/organizations", httpDetail);
			parsedvalue = jsonparser(org_reponse);
			int resources = ((JSONArray) parsedvalue.get("resources")).size();
			for (int i = 0; i < resources; i++) {
				JSONObject item = (JSONObject) ((JSONArray) parsedvalue.get("resources")).get(i);
				JSONObject entity = (JSONObject) item.get("entity");
				Map<String, Object> orgMap = new HashMap<>();
				orgMap.put("org_guid", (String) ((JSONObject) item.get("metadata")).get("guid"));
				orgDetails.add(orgMap);
					break;
				} 
		} catch (Exception e) {
			// throw new ServiceException("Exception in cf space api.", e);
		}
		return orgDetails;
	}
	


//	public static Map<String, Object> getAllAppDetails(String target, String spaceID, String accessToken) {
//		String summaryResponse = null;
//		Map<String, Object> appJson = null;
//		try {
//			HashMap<String, String> httpDetail = form_headers(accessToken);
//			String url = target + "/v2/spaces/" + spaceID + "/apps";
//			summaryResponse = RestUtil.gethttps(url, httpDetail);
//			appJson = JsonUtil.getJsonMap(summaryResponse);
//		} catch (Exception exception) {
//			// throw new ServiceException("Exception in CFLogin.", exception);
//		}
//		return appJson;
//	}
	@Scheduled(cron="0 0 10 * * *")
	public static Float getAllAppDetails(String target, String accessToken) {
		String summaryResponse = null;
		float b=0;
		String nU=null;
		List<String> nextURL = new ArrayList<>();
		String nextURLResponse = null;
		JSONObject parsedvalue = null;
		JSONObject URLparsedvalue = null;
		JSONArray jar=null;
		ArrayList<Object> al = new ArrayList<>();
		Map<String, Object> orgMap = new HashMap<>();	
		List<Map<String, Object>> orgDetails = new ArrayList<>();
		int a= 0;
		try {
			HashMap<String, String> httpDetail = form_headers(accessToken);
			summaryResponse = RestUtil.gethttps(target + "/v2/apps", httpDetail);
			parsedvalue = jsonparser(summaryResponse);
			int resources = ((JSONArray) parsedvalue.get("resources")).size();
			for (int i = 0; i < resources; i++) {
				JSONObject item = (JSONObject) ((JSONArray) parsedvalue.get("resources")).get(i);
				JSONObject entity = (JSONObject) item.get("entity");
				al.add(entity.get("memory"));
			}
			nU=(String)parsedvalue.get("next_url"); 
			while(nU!=null) {
				nextURLResponse = RestUtil.gethttps(target + nU, httpDetail);
				URLparsedvalue=jsonparser(nextURLResponse);
				int URLresources = ((JSONArray) URLparsedvalue.get("resources")).size();
				for (int i = 0; i < URLresources; i++) {
					JSONObject URLitem = (JSONObject) ((JSONArray) parsedvalue.get("resources")).get(i);
					JSONObject URLentity = (JSONObject) URLitem.get("entity");
					al.add(URLentity.get("memory"));
				}
				nU=(String) URLparsedvalue.get("next_url"); 
				nextURL.add(nU);
			}			
			for (int i = 0; i < al.size(); i++) {
				
				a = a+ Integer.parseInt( al.get(i).toString());
			}
			b= a/1024;
			
		} catch (Exception exception) {
			// throw new ServiceException("Exception in CFLogin.", exception);
		}
		return b;
	}
	
	public static JSONObject jsonparser(String inputobject) {
		JSONParser parser = new JSONParser();
		JSONObject responseObj = null;
		try {
			responseObj = (JSONObject) parser.parse(inputobject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseObj;
	}
	
}
