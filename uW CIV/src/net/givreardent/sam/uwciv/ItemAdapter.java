package net.givreardent.sam.uwciv;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<Item> {
	private Context context;
	
	public ItemAdapter(ArrayList<Item> items, Context context) {
		super(context, 0, items);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.list_main, null);
		TextView title = (TextView) convertView.findViewById(R.id.main_title);
		title.setText(getItem(position).title);
		TextView descr = (TextView) convertView.findViewById(R.id.main_descr);
		descr.setText(getItem(position).descr);
		return convertView;
	}
}
