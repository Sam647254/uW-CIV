package net.givreardent.sam.uwciv;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

import net.givreardent.sam.uwciv.fetchers.Fetcher;
import net.givreardent.sam.uwciv.internal.status;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GooseWatchFragment extends Fragment {
	private ListView list;
	private MapView mapView;
	private GoogleMap map;
	private ArrayList<Item> entries;
	private TextView emptyMessage;
	private boolean isSatellite = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<>();
		setHasOptionsMenu(true);
		getActivity().setTitle(getResources().getStringArray(R.array.building_weather_menu_entries)[3]);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_map_list, parent, false);
		mapView = (MapView) v.findViewById(R.id.goosenest_mapview);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(new OnMapReadyCallback() {

			@Override
			public void onMapReady(GoogleMap arg0) {
				map = arg0;
				map.getUiSettings().setMapToolbarEnabled(false);
			}
		});
		list = (ListView) v.findViewById(R.id.goosenest_listview);
		emptyMessage = (TextView) v.findViewById(android.R.id.empty);
		emptyMessage.setText(R.string.loading);
		list.setEmptyView(emptyMessage);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Item item = (Item) parent.getItemAtPosition(position);
				CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(item.latitude, item.longitude));
				map.animateCamera(update);
				item.marker.showInfoWindow();
			}
		});
		GooseWatchAdapter adapter = new GooseWatchAdapter(entries);
		list.setAdapter(adapter);
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_goosewatch, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.goosewatch_setmap:
			if (isSatellite) {
				isSatellite = false;
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				item.setTitle(R.string.satellite_map);
			} else {
				isSatellite = true;
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				item.setTitle(R.string.terrain_map);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		Marker marker;
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
		for (Item item : entries) {
			item.marker = map.addMarker(new MarkerOptions().position(new LatLng(item.latitude, item.longitude)).title(
					item.description));
		}
	}

	private class FetchGooseWatchTask extends AsyncTask<Void, Void, status> {

		@Override
		protected status doInBackground(Void... params) {
			JSONArray data;
			try {
				data = Fetcher.getGooseWatch();
				if (data == null)
					return status.dataError;
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
				return status.corruptData;
			}
			return status.success;
		}

		@Override
		protected void onPostExecute(status result) {
			if (result == status.success)
				refresh();
			else if (result == status.corruptData)
				emptyMessage.setText(R.string.goosewatch_error_2);
			else
				emptyMessage.setText(R.string.goosewatch_error_1);
		}
	}
}
