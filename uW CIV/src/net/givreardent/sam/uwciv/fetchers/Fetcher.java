package net.givreardent.sam.uwciv.fetchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import android.util.Log;
import net.givreardent.sam.uwciv.internal.data;

public class Fetcher {
	protected static final String APIKey = data.APIKey;
	public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(Fetcher.dateFormat);
	
	public static JSONObject getWeather() throws JSONException {
		return (JSONObject) getData("http://api.uwaterloo.ca/v2/weather/current.json", false);
	}
	
	public static JSONArray getGooseWatch() throws JSONException {
		return (JSONArray) getData("http://api.uwaterloo.ca/v2/resources/goosewatch.json", true);
	}
	
	public static JSONArray getBuildingsList() throws JSONException {
		return (JSONArray) getData("https://api.uwaterloo.ca/v2/buildings/list.json", true);
	}
	
	private static Object getData(String requestURL, boolean isArray) throws JSONException {
		URL JSONURL = null;
		HttpURLConnection connection = null;
		StringBuilder builder = new StringBuilder();
		try {
			JSONURL = new URL(Uri.parse(requestURL).buildUpon().appendQueryParameter("key", APIKey).build().toString());
			connection = (HttpURLConnection) JSONURL.openConnection();
			InputStream in = connection.getInputStream();
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inReader);
			if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
				return null;
			String line;
			do {
				line = reader.readLine();
				builder.append(line);
			} while (line != null);
		} catch (IOException e) {
			Log.e("tag", "Error: ", e);
			return null;
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		JSONTokener tokener = new JSONTokener(builder.toString());
		JSONObject main = (JSONObject) tokener.nextValue();
		if (isArray)
			return main.getJSONArray("data");
		else
			return main.getJSONObject("data");
	}
}
