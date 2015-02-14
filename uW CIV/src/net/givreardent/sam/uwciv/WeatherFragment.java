package net.givreardent.sam.uwciv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import net.givreardent.sam.uwciv.fetchers.WeatherFetcher;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherFragment extends ListFragment {
	private ArrayList<Item> entries;
	private String lastUpdate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<Item>();
		getActivity().setTitle(getResources().getStringArray(R.array.building_weather_menu_entries)[1]);
		String[] names = getResources().getStringArray(R.array.weather_entries),
				descriptions = getResources().getStringArray(R.array.weather_units);
		for (int i = 0; i < names.length; i++) {
			entries.add(new Item(names[i], descriptions[i]));
		}
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, parent, false);
		WeatherAdapter adapter = new WeatherAdapter(entries);
		setListAdapter(adapter);
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_weather, menu);
	}
	
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			if (Build.VERSION.SDK_INT >= 11)
				getActivity().getActionBar().setSubtitle("Refreshing...");
			else
				Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
			new FetchWeatherTask().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getListView().setClickable(false);
		new FetchWeatherTask().execute();
	}
	
	@TargetApi(11)
	public void refresh() {
		((WeatherAdapter) getListAdapter()).notifyDataSetChanged();
		if (Build.VERSION.SDK_INT >= 11)
			getActivity().getActionBar().setSubtitle("Observation at: " + lastUpdate);
		else
			Toast.makeText(getActivity(), "Observation at: " + lastUpdate, Toast.LENGTH_LONG).show();
	}
	
	public class Item {
		String title;
		String descr;
		String value = "--";
		
		public Item(String title, String descr) {
			this.title = title;
			this.descr = descr;
		}
	}
	
	private class WeatherAdapter extends ArrayAdapter<Item> {
		public WeatherAdapter(ArrayList<Item> entries) {
			super(getActivity(), 0, entries);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_weather, null);
			Item item = getItem(position);
			TextView entry = (TextView) convertView.findViewById(R.id.weather_entry_label);
			entry.setText(item.title);
			TextView unit = (TextView) convertView.findViewById(R.id.weather_unit);
			unit.setText(item.descr);
			Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/erbos_draco_1st_open_nbp.ttf");
			TextView value = (TextView) convertView.findViewById(R.id.weather_value);
			value.setTypeface(font);
			value.setText(item.value);
			return convertView;
		}
	}
	
	private class FetchWeatherTask extends AsyncTask<Void, Void, Void> {
		private final String[] values = { "temperature_current_c", "humidex_c", "windchill_c",
				"relative_humidity_percent", "dew_point_c", "wind_speed_kph", "pressure_kpa",
				"incoming_shortwave_radiation_wm2", "temperature_24hr_max_c", "temperature_24hr_min_c",
				"precipitation_15min_mm", "precipitation_1hr_mm", "precipitation_24hr_mm"};
		private final String windDirection = "wind_direction_degrees", time = "observation_time",
				dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

		@Override
		protected Void doInBackground(Void... params) {
			JSONObject data;
			try {
				data = WeatherFetcher.getWeatherJSON();
				float direction = (float) data.getDouble(windDirection);
				float speed = (float) data.getDouble(values[5]);
				if (direction < 22.5 || direction >= 337.5)
					entries.get(5).value = "N ";
				else if (direction >=22.5 && direction < 67.5)
					entries.get(5).value = "NW";
				else if (direction >= 67.5 && direction < 112.5)
					entries.get(5).value = "W ";
				else if (direction >= 112.5 && direction < 157.5)
					entries.get(5).value = "SW";
				else if (direction >= 157.5 && direction < 202.5)
					entries.get(5).value = "S ";
				else if (direction >= 202.5 && direction < 247.5)
					entries.get(5).value = "SE";
				else if (direction >= 247.5 && direction < 292.5)
					entries.get(5).value = "E ";
				else if (direction >= 292.5 && direction < 337.7)
					entries.get(5).value = "NE";
				entries.get(5).value += String.format("%3d", (int) speed);
				String time = data.getString(this.time);
				SimpleDateFormat df = new SimpleDateFormat(dateFormat);
				DateFormat dF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.CANADA);
				lastUpdate = dF.format(df.parse(time));
			} catch (JSONException | ParseException e) {
				return null;
			}
			for (int i = 0; i < values.length; i++) {
				if (i == 5) continue;
				try {
					entries.get(i).value = String.format("%.1f", data.getDouble(values[i]));
				} catch (JSONException e) {
					Log.e("tag", "Error: ", e);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void items) {
			refresh();
		}
	}
}
