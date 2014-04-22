package com.uwetrottmann.shopr.ui;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.uwetrottmann.androidutils.Maps;
import com.uwetrottmann.shopr.R;
import com.uwetrottmann.shopr.adapters.FavouriteItemAdapter;
import com.uwetrottmann.shopr.adapters.FavouriteItemAdapter.OnFavouriteItemDisplayListener;
import com.uwetrottmann.shopr.algorithm.model.Item;
import com.uwetrottmann.shopr.loaders.FavouriteItemLoader;
import com.uwetrottmann.shopr.ui.LocationHandler.LocationUpdateEvent;

import de.greenrobot.event.EventBus;

/**
 * Shows a list of clothing items the user can critique by tapping an up or down
 * vote button.
 */
public class FavouriteItemListFragment extends Fragment implements
		LoaderCallbacks<List<Item>>, OnFavouriteItemDisplayListener {

	public static final String TAG = "Favourite Item List";

	// I = 9, T = 20
	private static final int LOADER_ID = 920;
	private static final int REQUEST_CODE = 12;
	private GridView mGridView;
	private FavouriteItemAdapter mAdapter;
	private TextView emtpyView;

	private boolean mIsInitialized;

	public static FavouriteItemListFragment newInstance() {
		return new FavouriteItemListFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favourite_items_list,
				container, false);

		mGridView = (GridView) v.findViewById(R.id.gridViewItemList);
		emtpyView = (TextView) v.findViewById(R.id.textViewItemListEmpty);
		mGridView.setEmptyView(emtpyView);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new FavouriteItemAdapter(getActivity(), this);

		mGridView.setAdapter(mAdapter);

		Bundle args = new Bundle();
		args.putBoolean("isinit", false);
		getLoaderManager().initLoader(LOADER_ID, args, this);

		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().registerSticky(this, LocationUpdateEvent.class);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.item_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_restart:
			onInitializeItems();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<List<Item>> onCreateLoader(int loaderId, Bundle args) {
		return new FavouriteItemLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<Item>> loader, List<Item> data) {
		mAdapter.clear();
		mAdapter.addAll(data);
		onUpdateShops(data);

		if (data.size() == 0) {
			emtpyView.setText(R.string.favourite_list_empty);
		}
	}

	public class ShopUpdateEvent {
		/**
		 * Holds a list of shop ids and how many recommendations are shown for
		 * each shop.
		 */
		Map<Integer, Integer> shopMap;
	}

	/**
	 * Post {@link ShopUpdateEvent} based on current list of recommendations.
	 * 
	 * @param data
	 */
	private void onUpdateShops(List<Item> data) {
		ShopUpdateEvent event = new ShopUpdateEvent();

		// get shops and number of items per shop
		event.shopMap = Maps.newHashMap();
		for (Item item : data) {
			int shopId = item.shopId();
			int count = 1;

			if (event.shopMap.containsKey(shopId)) {
				count = event.shopMap.get(shopId);
				count++;
			}

			event.shopMap.put(shopId, count);
		}

		EventBus.getDefault().postSticky(event);
	}

	@Override
	public void onLoaderReset(Loader<List<Item>> loader) {
		mAdapter.clear();
	}

	@Override
	public void onItemDisplay(Item item) {
		// display details
		Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
		intent.putExtra(ItemDetailsActivity.InitBundle.ITEM_ID, item.id());
		startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
			Log.d(TAG, "Received recommendation update, requerying");
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}

	public void onEvent(LocationUpdateEvent event) {
		if (!mIsInitialized) {
			Log.d(TAG, "Received location update, requerying");
			mIsInitialized = true;
			onInitializeItems();
		}
	}

	private void onInitializeItems() {
		Bundle args = new Bundle();
		args.putBoolean("isinit", true);
		getLoaderManager().restartLoader(LOADER_ID, args, this);
	}
}
