package net.givreardent.sam.uwciv;

import java.util.ArrayList;

import net.givreardent.sam.uwciv.fetchers.WeatherFetcher;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainFragment extends ListFragment {
	private ArrayList<Item> entries;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<Item>();
		String[] names = getResources().getStringArray(R.array.main_menu_entries),
				descriptions = getResources().getStringArray(R.array.main_menu_descr);
		for (int i = 0; i < names.length; i++) {
			entries.add(new Item(names[i], descriptions[i]));
		}
		new FetchWeatherTask().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, parent, false);
		MenuAdapter adapter = new MenuAdapter(entries);
		setListAdapter(adapter);
		return v;
	}
	
	private class Item {
		String title;
		String descr;
		
		public Item(String title, String descr) {
			this.title = title;
			this.descr = descr;
		}
	}
	
	private class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.i("tag", "Got weather JSON: " + WeatherFetcher.getWeatherJSON().toString());
			} catch (JSONException e) {
				Log.e("tag", "Error: ", e);
			}
			return null;
		}
		
	}
	
	private class MenuAdapter extends ArrayAdapter<Item> {
		public MenuAdapter(ArrayList<Item> items) {
			super(getActivity(), 0, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_main, null);
			TextView title = (TextView) convertView.findViewById(R.id.main_title);
			title.setText(getItem(position).title);
			TextView descr = (TextView) convertView.findViewById(R.id.main_descr);
			descr.setText(getItem(position).descr);
			return convertView;
		}
	}
}
