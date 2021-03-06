package com.uwetrottmann.shopr.ui.explanation;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.model.LatLng;
import com.uwetrottmann.shopr.R;
import com.uwetrottmann.shopr.adapters.NavDrawerAdapter;
import com.uwetrottmann.shopr.eval.TestSetupActivity;
import com.uwetrottmann.shopr.importer.ImporterActivity;
import com.uwetrottmann.shopr.mindmap.ClothingTypeFragment;
import com.uwetrottmann.shopr.mindmap.ColorFragment;
import com.uwetrottmann.shopr.mindmap.PriceRangeFragment;
import com.uwetrottmann.shopr.mindmap.SexFragment;
import com.uwetrottmann.shopr.model.NavDrawerItem;
import com.uwetrottmann.shopr.model.NavMenuItem;
import com.uwetrottmann.shopr.model.NavMenuSection;
import com.uwetrottmann.shopr.model.ui.NavDrawerActivityConfiguration;
import com.uwetrottmann.shopr.settings.AppSettings;
import com.uwetrottmann.shopr.ui.LocationHandler;
import com.uwetrottmann.shopr.ui.SettingsActivity;

import de.greenrobot.event.EventBus;

public class MainActivityExplanation extends AbstractNavDrawerActivity {
	private LocationHandler locationHandler;
	private NavDrawerActivityConfiguration navDrawerActivityConfiguration;
	private Fragment currentFragment = RecommendationsFragment.newInstance();
	private int recommendationsNavDrawerItemPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationHandler = LocationHandler.getInstance(this);
		locationHandler.initLocationClientOrExit();
		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.content,
							RecommendationsTabFragment.newInstance()).commit();
		}

		// Use fake location unless otherwise stated
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putBoolean(AppSettings.KEY_USING_DIVERSITY, true).commit();
	}

	@Override
	protected NavDrawerActivityConfiguration getNavDrawerConfiguration() {
		NavDrawerItem[] menu = new NavDrawerItem[] {
				NavMenuItem.create(101,
						getString(R.string.drawer_section_title_home),
						"ic_menu_home", false, this),
				NavMenuItem.create(102,
						getString(R.string.drawer_section_title_favourites),
						"ic_action_heart", true, this),
				NavMenuSection.create(200,
						getString(R.string.drawer_section_title_mind_map)),
				NavMenuItem.create(201, "Overview", "", false, this),
				NavMenuItem.create(202, "Clothing Type", "", false, this),
				NavMenuItem.create(203, "Color", "", false, this),
				NavMenuItem.create(204, "Gender", "", false, this),
				NavMenuItem.create(205, "Price Range", "", false, this) };
		
		recommendationsNavDrawerItemPos = 0;

		navDrawerActivityConfiguration = new NavDrawerActivityConfiguration();
		navDrawerActivityConfiguration
				.setMainLayout(R.layout.explanation_activity_main);
		navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
		navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
		navDrawerActivityConfiguration.setNavItems(menu);
		navDrawerActivityConfiguration
				.setDrawerShadow(R.drawable.drawer_shadow);
		navDrawerActivityConfiguration.setDrawerOpenDesc(R.string.drawer_open);
		navDrawerActivityConfiguration
				.setDrawerCloseDesc(R.string.drawer_close);
		navDrawerActivityConfiguration.setBaseAdapter(new NavDrawerAdapter(
				this, R.layout.common_drawer_list_item, menu));
		return navDrawerActivityConfiguration;
	}

	@Override
	protected void onNavItemSelected(int id) {
		switch ((int) id) {
		case 101:
			currentFragment = RecommendationsTabFragment.newInstance();
			replaceContent(currentFragment);
			break;
		case 102:
			currentFragment = FavouritesFragment.newInstance();
			replaceContent(currentFragment);
			break;
		case 201:
			currentFragment = new MindMapOverviewFragment();
			replaceContent(currentFragment);
			break;
		case 202:
			currentFragment = new ClothingTypeFragment();
			replaceContent(currentFragment);
			break;
		case 203:
			currentFragment = new ColorFragment();
			replaceContent(currentFragment);
			break;
		case 204:
			currentFragment = new SexFragment();
			replaceContent(currentFragment);
			break;
		case 205:
			currentFragment = new PriceRangeFragment();
			replaceContent(currentFragment);
			break;
		}
	}

	private void replaceContent(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content, fragment).commit();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (AppSettings.isUsingFakeLocation(this)) {
			// use fake location (Marienplatz, Munich)
			locationHandler.setLastLocation(new LatLng(48.137314, 11.575253));
			// send out location update event immediately
			EventBus.getDefault().postSticky(
					locationHandler.newLocationUpdateEvent());
		} else {
			locationHandler.connectLocationClient();
		}

		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		if (!AppSettings.isUsingFakeLocation(this)) {
			locationHandler.disconnectLocationClient();
		}
		super.onStop();

		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onBackPressed() {
		if (!(currentFragment instanceof RecommendationsTabFragment)) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.content,
							RecommendationsTabFragment.newInstance()).commit();
			selectItem(0);
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_restart_test:
			startActivity(new Intent(this, TestSetupActivity.class));
			// clean this activity up
			finish();
			return true;
		case R.id.action_import:
			startActivity(new Intent(this, ImporterActivity.class));
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
