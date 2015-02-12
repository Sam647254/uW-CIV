package net.givreardent.sam.uwciv;

import java.util.ArrayList;

import net.givreardent.sam.uwciv.fetchers.WeatherFetcher;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WeatherFragment extends ListFragment {
	private ArrayList<Item> entries;
	
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, parent, false);
		WeatherAdapter adapter = new WeatherAdapter(entries);
		setListAdapter(adapter);
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getListView().setClickable(false);
		new FetchWeatherTask().execute();
	}
	
	public void refresh() {
		((WeatherAdapter) getListAdapter()).notifyDataSetChanged();
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
		private static final String temp = "temperature_current_c";

		@Override
		protected Void doInBackground(Void... params) {
			try {
				JSONObject data = WeatherFetcher.getWeatherJSON();
				entries.get(0).value = Integer.toString((int) data.getDouble(temp));
				Log.d("tag", "Now updaing temperature");
			} catch (JSONException e) {
				Log.e("tag", "Error: ", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void items) {
			refresh();
		}
	}
}
