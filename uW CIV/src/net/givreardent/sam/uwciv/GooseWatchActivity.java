package net.givreardent.sam.uwciv;

import android.app.Fragment;

public class GooseWatchActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new GooseWatchFragment();
	}

}
