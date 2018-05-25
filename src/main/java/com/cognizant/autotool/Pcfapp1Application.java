package com.cognizant.autotool;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.cognizant.autotool.CFUtil;
import com.cognizant.autotool.JsonUtil;

import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@EnableScheduling
@SpringBootApplication
public class Pcfapp1Application {

	public static void main(String[] args) {
		SpringApplication.run(Pcfapp1Application.class, args);
	}
	    @CrossOrigin
		@RequestMapping(value = "/{instance}", method = GET, produces = APPLICATION_JSON_VALUE)
		public ResponseEntity<String> taskStatus(@PathVariable String instance) {
			String target = null;
			ResponseEntity<String> response = null;
			float Mem = 0;

			try {
				
				if(instance.equalsIgnoreCase("cumuluslabs"))
				{
					 target = "https://api.system.cumuluslabs.io";
				}
				else if (instance.equalsIgnoreCase("digifabricpcf"))
				{
					 target = "https://api.system.dev.digifabricpcf.com";
				}
				
				String accessToken = CFUtil.login(instance);
				Mem =  CFUtil.getAllAppDetails(target, accessToken);		
				System.out.println("Total Occupied Memory(In Gb): "+Mem+"Gb");
				
				Map<String, Object> message = new HashMap<String, Object>();
				message.put("Total Occupied Memory(In Gb):", Mem  );
				
				MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
				headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
				response = new ResponseEntity<String>(JsonUtil.getJsonString(message), headers, ACCEPTED);
				
				//System.out.println("#############################################"+resourcesData+"##########################################");


			} catch (Exception exception) {
				response = new ResponseEntity<String>(INTERNAL_SERVER_ERROR);
			}
			
			return response;
			
		}
}
