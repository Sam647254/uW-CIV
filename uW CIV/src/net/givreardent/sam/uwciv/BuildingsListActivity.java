package net.givreardent.sam.uwciv;

import android.app.Fragment;

public class BuildingsListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new BuildingsListFragment();
	}

}
