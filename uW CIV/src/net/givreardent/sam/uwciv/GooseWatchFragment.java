package net.givreardent.sam.uwciv;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

import net.givreardent.sam.uwciv.fetchers.Fetcher;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class GooseWatchFragment extends Fragment {
	private ListView list;
	private MapView mapView;
	private GoogleMap map;
	private ArrayList<Item> entries;
	private TextView emptyMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<>();
		getActivity().setTitle(getResources().getStringArray(R.array.building_weather_menu_entries)[3]);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_goosewatch, parent, false);
		mapView = (MapView) v.findViewById(R.id.goosenest_mapview);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(new OnMapReadyCallback() {
			
			@Override
			public void onMapReady(GoogleMap arg0) {
				map = arg0;
			}
		});
		list = (ListView) v.findViewById(R.id.goosenest_listview);
		emptyMessage = (TextView) v.findViewById(android.R.id.empty);
		emptyMessage.setText(R.string.loading);
		list.setEmptyView(emptyMessage);
		GooseWatchAdapter adapter = new GooseWatchAdapter(entries);
		list.setAdapter(adapter);
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
		new FetchGooseWatchTask().execute();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	};
	
	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	private class Item {
		String description;
		String lastUpdated;
		double longitude;
		double latitude;
	}

	private class GooseWatchAdapter extends ArrayAdapter<Item> {
		public GooseWatchAdapter(ArrayList<Item> items) {
			super(getActivity(), 0, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_goosewatch, null, false);
			TextView description = (TextView) convertView.findViewById(R.id.goosewatch_descr);
			description.setText(getItem(position).description);
			TextView updated = (TextView) convertView.findViewById(R.id.goosewatch_updated);
			updated.setText(getItem(position).lastUpdated);
			return convertView;
		}
	}
	
	private void refresh() {
		((GooseWatchAdapter) list.getAdapter()).notifyDataSetChanged();
	}

	private class FetchGooseWatchTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			JSONArray data;
			try {
				data = Fetcher.getGooseWatch();
				for (int i = 0; i < data.length(); ++i) {
					Item item = new Item();
					item.description = data.getJSONObject(i).optString("location");
					if (item.description.equals(""))
						item.description = "(no description)";
					DateFormat dF = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CANADA);
					item.lastUpdated = dF.format(Fetcher.dateFormatter
							.parse(data.getJSONObject(i).optString("updated")));
					item.longitude = data.getJSONObject(i).getDouble("longitude");
					item.latitude = data.getJSONObject(i).getDouble("latitude");
					entries.add(item);
				}
			} catch (JSONException | java.text.ParseException e) {
				Log.e("GooseWatchFragment", "Error: ", e);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				refresh();
			else
				emptyMessage.setText(R.string.goosewatch_error);
		}
	}
}
