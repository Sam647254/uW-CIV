package net.givreardent.sam.uwciv;

import android.app.Fragment;

public class WeatherActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new WeatherFragment();
	}

}
