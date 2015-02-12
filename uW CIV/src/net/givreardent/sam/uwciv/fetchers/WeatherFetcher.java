package net.givreardent.sam.uwciv.fetchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public final class WeatherFetcher {
	private static final String APIKey = "81a599eb8d4742d9164739ae09541547";

	public static JSONObject getWeatherJSON() throws JSONException {
		URL JSONURL = null;
		HttpsURLConnection connection = null;
		StringBuilder builder = new StringBuilder();
		try {
			JSONURL = new URL("https://api.uwaterloo.ca/v2/weather/current.json?api_key=" + APIKey);
			connection = (HttpsURLConnection) JSONURL.openConnection();
			InputStream in = connection.getInputStream();
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inReader);
			if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
				;
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
		return main.getJSONObject("data");
	}
}
