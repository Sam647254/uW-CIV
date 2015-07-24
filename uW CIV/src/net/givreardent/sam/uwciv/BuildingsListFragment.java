package net.givreardent.sam.uwciv;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;

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
import android.widget.Toast;
import net.givreardent.sam.uwciv.fetchers.Fetcher;
import net.givreardent.sam.uwciv.internal.status;

public class BuildingsListFragment extends Fragment {
	private GoogleMap map;
	private ArrayList<Item> entries;
	private ListView list;
	private TextView emptyView;
	private MapView mapView;
	private Marker focusedMarker;
	private boolean mapVisible = false;
	private ClusterManager<ClusterItemImplementation> clusterManager;
	private HashMap<String, Item> buildingHashMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<>();
		getActivity().setTitle(getResources().getStringArray(R.array.building_weather_menu_entries)[0]);
		setHasOptionsMenu(true);
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
				setupClusters();
			}
		});
		mapView.setVisibility(View.GONE);
		list = (ListView) v.findViewById(R.id.goosenest_listview);
		emptyView = (TextView) v.findViewById(android.R.id.empty);
		list.setEmptyView(emptyView);
		list.setAdapter(new BuildingAdapter(entries));
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Item item = (Item) parent.getItemAtPosition(position);
				if (Double.isNaN(item.latitude) || Double.isNaN(item.longitude)) {
					Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();
					return;
				}
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(item.coordinates, 15F);
				map.animateCamera(update);
				item.marker.showInfoWindow();
			}
		});
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_buildings, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.show_map:
			if (mapVisible) {
				mapView.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
				mapVisible = false;
			} else {
				mapView.setVisibility(View.VISIBLE);
				list.setVisibility(View.GONE);
				mapVisible = true;
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
		new FetchListTask().execute();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
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
	
	private void refresh() {
		((BuildingAdapter) list.getAdapter()).notifyDataSetChanged();
		for (Item item : entries)
			if (!(Double.isNaN(item.latitude) || Double.isNaN(item.longitude))) {
				// item.marker = map.addMarker(new MarkerOptions().position(item.coordinates).title(item.code).snippet(item.name));
				clusterManager.addItem(new ClusterItemImplementation(item.coordinates, item.code, item.name));
				buildingHashMap.put(item.code, item);
			}
	}
	
	private void setupClusters() {
		clusterManager = new ClusterManager<>(getActivity(), map);
		map.setOnCameraChangeListener(clusterManager);
		map.setOnMarkerClickListener(clusterManager);
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker arg0) {
				Item building = buildingHashMap.get(arg0.getTitle());
			}
		});
		clusterManager.setOnClusterItemClickListener(new OnClusterItemClickListener<BuildingsListFragment.ClusterItemImplementation>() {

			@Override
			public boolean onClusterItemClick(ClusterItemImplementation item) {
				if (focusedMarker != null) {
					focusedMarker.remove();
				}
				focusedMarker = map.addMarker(item.getMarkerOptions());
				focusedMarker.showInfoWindow();
				map.animateCamera(CameraUpdateFactory.newLatLng(item.coordinates));
				return true;
			}
		});
		map.getUiSettings().setMapToolbarEnabled(true);
	}
	
	private class Section {
		String name;
		double latitude, longitude;
	}

	private class Item {
		String ID, code, name;
		double latitude, longitude;
		String[] altNames;
		Section[] sections;
		Marker marker;
		LatLng coordinates;
	}
	
	private class BuildingAdapter extends ArrayAdapter<Item> {
		public BuildingAdapter(ArrayList<Item> buildings) {
			super(getActivity(), 0, buildings);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_building, parent, false);
			Item item = getItem(position);
			TextView code = (TextView) convertView.findViewById(R.id.building_code);
			code.setText(item.code);
			if (Double.isNaN(item.latitude) || Double.isNaN(item.longitude))
				code.setTextColor(getResources().getColor(android.R.color.darker_gray));
			TextView name = (TextView) convertView.findViewById(R.id.building_name);
			name.setText(item.name);
			return convertView;
		}
	}

	private class FetchListTask extends AsyncTask<Void, Void, status> {

		@Override
		protected status doInBackground(Void... params) {
			JSONArray data;
			try {
				data = Fetcher.getBuildingsList();
				if (data == null)
					return status.dataError;
				for (int i = 0; i < data.length(); ++i) {
					JSONObject building = data.getJSONObject(i);
					Item item = new Item();
					item.ID = building.getString("building_id");
					item.code = building.getString("building_code");
					item.name = building.getString("building_name");
					JSONArray altNames = building.optJSONArray("alternate_names");
					if (altNames == null)
						item.altNames = null;
					else {
						item.altNames = new String[altNames.length()];
						for (int j = 0; j < altNames.length(); ++j) {
							item.altNames[j] = altNames.getString(j);
						}
					}
					item.latitude = building.optDouble("latitude");
					item.longitude = building.optDouble("longitude");
					JSONArray sections = building.optJSONArray("building_sections");
					if (sections == null)
						item.sections = null;
					else {
						item.sections = new Section[sections.length()];
						for (int j = 0; j < sections.length(); ++j) {
							Section section = new Section();
							section.name = sections.getJSONObject(j).optString("section_name");
							section.longitude = sections.getJSONObject(j).optDouble("longitude");
							section.latitude = sections.getJSONObject(j).optDouble("latitude");
							item.sections[j] = section;
						}
					}
					item.coordinates = new LatLng(item.latitude, item.longitude);
					entries.add(item);
				}
			} catch (JSONException e) {
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
				emptyView.setText(R.string.data_error);
			else
				emptyView.setText(R.string.connection_error);
		}
	}
	
	public class ClusterItemImplementation implements ClusterItem {
		private LatLng coordinates;
		private MarkerOptions markerOptions;
		
		public ClusterItemImplementation(LatLng coordinates, String code, String name) {
			this.coordinates = coordinates;
			markerOptions = new MarkerOptions().title(code).position(coordinates).snippet(name);			
		}
		
		public MarkerOptions getMarkerOptions() {
			return markerOptions;
		}

		@Override
		public LatLng getPosition() {
			return coordinates;
		}
		
	}
}
