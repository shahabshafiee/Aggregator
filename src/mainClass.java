/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//import java.applet.Applet;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 *
 * @author shahab shafieihajiabady
 * 06.02.2017e
 */
//public class mainClass extends Applet {
public class mainClass {
	private static boolean debug=false;
	private static int numberOfServices=0;
	private static boolean serviceAvailability;
	private static int counter=0;
	/**
	 * @param args the command line arguments
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args)throws IOException, ParseException {
		writeTxtToFile();

		//gson test

		//		Gson gson = new GsonBuilder().create();
		//        gson.toJson("Hello", System.out);
		//        gson.toJson(123, System.out); 


		System.out.println();


		//------------------------------------------------read json file from owncloud------------------------------------
		JSONParser parser = new JSONParser();
		String s1 = null;
		try {
			s1 = readUrl("http://141.22.44.23:8080/yellowpages/activepower/getAll");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONArray o = (JSONArray) parser.parse(s1);

		//JSONArray array = (JSONArray) o.get("id");
		ArrayList<JSONObject> list = new ArrayList<>();

		numberOfServices=o.size();
		System.out.println("Number of services: "+numberOfServices);
		System.out.println();
		for (int i = 0; i < o.size(); i++) {
			list.add((JSONObject) o.get(i));
		}

		//keep the original unsorted list
		ArrayList<JSONObject> unsortedList = new ArrayList<>();
		unsortedList=list;
		System.out.println("Unsorted list: ");


		//--------------------------------------show the unsorted list---------------------------------------------
		for (JSONObject JO:unsortedList) {
			JSONObject prices=(JSONObject) JO.get("prices");
			//			System.out.println(prices.get("powerCosts"));
			System.out.println("for serviceID: "+JO.get("serviceID")+", power cost is: "+prices.get("powerCosts"));
		}
		System.out.println();

		//sort
		Collections.sort(list, new MyJSONComparator());



		//--------------------------------------show the sorted list---------------------------------------------
		System.out.println("Sorted list based on power cost:");

		for (JSONObject JO:list) {
			JSONObject prices=(JSONObject) JO.get("prices");
			//			System.out.println(prices.get("powerCosts"));
			System.out.println("for serviceID: "+JO.get("serviceID")+", power cost is: "+prices.get("powerCosts"));
		}




		//--------------------------------start the quarters arrangement--------------------------------------------
		Date date = new Date();
		System.out.println();
		//		System.out.println("System.currentTimeMillis()    "+System.currentTimeMillis());
		System.out.println("Current time in milliseconds: "+date.getTime());
		final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("Current time: "+sdf.format(date));

		//convert from milli to date
		//		Date dateFromEnergyService = new Date();
		//		dateFromEnergyService.setTime(1488368478058L);
		//		System.out.println("Current time(normal format): "+dateFromEnergyService);


		//check the availability of suppliers for the next 24 hours
		//		every hour is 86,400,000 ms 
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("check the availability of suppliers for the next 24 hours...");
		long currentDate=date.getTime();

		for (JSONObject JO:list) {	
			Date startDate=new Date();
			startDate.setTime((long) JO.get("startDate"));
			Date endDate=new Date();
			endDate.setTime((long) JO.get("endDate") );

			long startDateL=startDate.getTime();
			long endDateL=endDate.getTime();
			int i = Long.compare(currentDate,startDateL);
			int j = Long.compare(endDateL,currentDate);
			if(i==1&&j==1){
				String sericeID=(String) JO.get("serviceID");
				double  powerCosts= (double) ((JSONObject) JO.get("prices")).get("powerCosts");
				writeJsonToFile(sericeID,powerCosts);//call writeJsonToFile to create a json file with available energy services
				System.out.println();
				System.out.println("service number "+JO.get("serviceID")+" and power cost of "+((JSONObject) JO.get("prices")).get("powerCosts")+" is available.");

				System.out.println("from : "+convertDate(startDateL,"dd/MM/yyyy HH:mm:ss"));
				System.out.println("until: "+convertDate(endDateL,"dd/MM/yyyy HH:mm:ss"));
			}
		}



		//		show availability for the next 96 quarters
		System.out.println();
		System.out.println("--------------------------------showing the results for the next 96 quarters: --------------------------------");
		for (long cd = currentDate; cd < currentDate+86400000; cd+=900000) {
			counter++;

			for (JSONObject JO:list) {	
				Date startDate=new Date();
				startDate.setTime((long) JO.get("startDate"));
				//			long startDate=(long) JO.get("startDate");
				Date endDate=new Date();
				endDate.setTime((long) JO.get("endDate") );

				long startDateL=startDate.getTime();
				long endDateL=endDate.getTime();
				//				long endDate=(long) JO.get("endDate");

				int i = Long.compare(currentDate,startDateL);
				int j = Long.compare(endDateL,currentDate);
				//			double currentDate=5;
				//			double startDate=3;
				//			double endDate=6;

				if(i==1&&j==1){
					serviceAvailability=true;
					System.out.println();
					System.out.println("Quarter "+counter);
					System.out.println("from "+convertDate(cd, "HH:mm")+" until "+convertDate(cd+900000, "HH:mm"));

					System.out.println("service number "+JO.get("serviceID")+", power cost: "+((JSONObject) JO.get("prices")).get("powerCosts"));

					if (debug)					System.out.println("from : "+convertDate(startDateL,"dd/MM/yyyy HH:mm:ss"));
					if (debug)					System.out.println("until: "+convertDate(endDateL,"dd/MM/yyyy HH:mm:ss"));
				}
			}

		}
		counter=0;

		if(!serviceAvailability)System.out.println("no service :(");



		//----------------------------------------load json file from url----------------------------------
		//		String json = null;
		//		try {
		//			json = readUrl("http://141.22.44.23:8080/yellowpages/activepower/getAll");
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//
		//		Gson gson = new Gson();        
		//		Page page = gson.fromJson(json, Page.class);
		//
		//		System.out.println(page.title);
		//		System.out.println();
		//		for (Item item : page.items)
		//			System.out.println("    " + item.title);
	}

	//-------------------------------------convert date---------------------------------------------

	public static String convertDate(long dateInMilliseconds,String dateFormat) {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		//		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dateInMilliseconds);
		return formatter.format(calendar.getTime());

	}



	//-----------------------------------readUrl method----------------------------------------------------

	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read); 

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	//-----------------------------create txt file-----------------------------------

	private static void writeTxtToFile(){
		List<String> lines = Arrays.asList("The first line", "The second line");
		Path file = Paths.get("C:\\Users\\shahabpc\\Desktop\\test.txt");
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e);
		}
		//Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
	}

	//-----------------------------create json file-----------------------------------------

	@SuppressWarnings("unchecked")
	private static void writeJsonToFile(String derID,double powerCosts){
		JSONObject obj = new JSONObject();

		obj.put("derID", derID);
		obj.put("powerCosts", powerCosts);

		//	JSONArray company = new JSONArray();
		//	company.add("Compnay: eBay");
		//	company.add("Compnay: Paypal");
		//	company.add("Compnay: Google");
		//	obj.put("Company List", company);

		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter("C:\\Users\\shahabpc\\Desktop\\test.json")) {
			file.write(obj.toJSONString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



