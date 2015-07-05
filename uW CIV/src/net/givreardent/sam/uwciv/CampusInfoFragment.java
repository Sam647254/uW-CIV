package net.givreardent.sam.uwciv;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CampusInfoFragment extends ListFragment {
	private ArrayList<Item> entries;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entries = new ArrayList<Item>();
		String[] names = getResources().getStringArray(R.array.building_weather_menu_entries),
				descriptions = getResources().getStringArray(R.array.building_weather_menu_descr);
		for (int i = 0; i < names.length; i++) {
			entries.add(new Item(names[i], descriptions[i]));
		}
		getActivity().setTitle(getResources().getStringArray(R.array.main_menu_entries)[2]);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, parent, false);
		ItemAdapter adapter = new ItemAdapter(entries, getActivity());
		setListAdapter(adapter);
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i;
		switch(position) {
		case 1:
			i = new Intent(getActivity(), WeatherActivity.class);
			startActivity(i);
			break;
		case 3:
			i = new Intent(getActivity(), GooseWatchActivity.class);
			startActivity(i);
			break;
		default:
			super.onListItemClick(l, v, position, id);
		}
	}
}
