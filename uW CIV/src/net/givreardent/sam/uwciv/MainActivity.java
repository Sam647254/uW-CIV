package net.givreardent.sam.uwciv;

import android.app.Fragment;


public class MainActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new MainFragment();
	}
}
