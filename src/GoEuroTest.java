
import java.applet.Applet;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * This Java Applet is a solution to the test task
 * https://github.com/goeuro/dev-test
 * 
 * @author Philip Brinkmann
 * @version 1.0, 16.09.2016
 */
public class GoEuroTest extends Applet{

	/**
	 * The parameters
	 */
	private static final long serialVersionUID = 1L;
	/** The name of the city for which we want to process the search */
	private static String cityName;
	
	/**
	 * The user gives a city name through the command line.
	 * This can cover multiple words (as in "Bernau bei Berlin").
	 * @param args
	 */
	public static void main(String[] args) {
		//first, check whether there is user input
		if (args.length > 0) {
			//now, interpret the user input as a city name, possibly containing several words
			String t = "";
			for (String s: args) {
				t += s+" ";
			}
			t.trim(); //through away the last empty space
			t = t.replaceAll(" ", "_"); //the webpage cannot be opened with blank spaces, but works with underscore as well
			cityName = t;
			try {
				//the url from which we can get the JSON file
				URL url = new URL("http://api.goeuro.com/api/v2/position/suggest/en/"+cityName);
				try {
					//open an input stream to the url and retrieve the JSON from there
					InputStream in = url.openConnection().getInputStream();
					JsonReader reader = Json.createReader(in);
					JsonStructure jsondata = reader.read();
					//now, test the JSON data structure; we expect an array that contains objects
					switch(jsondata.getValueType()){
					case ARRAY:
						//convert the retrieved JSON data to an array and go through all entries
						JsonArray dataarray = (JsonArray) jsondata;
						//open the outputfile to write line by line the data from the array
						PrintWriter outfile = new PrintWriter("queryresult.csv", "UTF-8");
						for (JsonValue val: dataarray) {
							switch(val.getValueType()) {
							case OBJECT:
								//convert val into Json object and read the field names
								JsonObject dataobject = (JsonObject) val;
								for (String name: dataobject.keySet()) {
									//now, filter for the fields we want and write the data into the outfile
									if (name.contentEquals("_id")) {
										JsonNumber id = (JsonNumber) dataobject.get(name);
										outfile.append(id.toString()+", ");
									}else if (name.contentEquals("name") || name.contentEquals("type")) {
										JsonString st = (JsonString) dataobject.get(name);
										outfile.append(st.getString()+", ");
									}else if (name.contentEquals("geo_position")) {
										JsonObject pos = (JsonObject) dataobject.get(name);
										JsonNumber lat = (JsonNumber) pos.get("latitude");
										JsonNumber lon = (JsonNumber) pos.get("longitude");
										outfile.append(lat.toString()+", "+lon.toString()+"\n");
									}
								}
								break;
							default:
								//then the JSON array did not contain (only) objects
								System.err.println("The entry in JSON ARRAY is no OBJECT.");
							}
						}
						outfile.close();
						break;
					default:
						//then we did not retrieve an JSON array
						System.err.println("The JSON data is not an ARRAY.");
					}
				} catch (IOException e) {
					//if the url could not be opened
					e.printStackTrace();
					System.err.println("Could not open the webpage of GoEuro.");
				}
			} catch (MalformedURLException e) {
				//if there went something wrong with the url
				e.printStackTrace();
				System.err.println("Could not form an url.");
			}
		}else{
			//if the user did not enter a city name
			System.err.println("Please specify a city name.");
		}
	}

}
