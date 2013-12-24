package us.shandian.mod.swipeback.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;

import java.util.List;
import java.util.ArrayList;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.ModSwipeBack;
import us.shandian.mod.swipeback.adapter.ApplicationAdapter;

public class SwipeBackBlacklist extends ListActivity
{
	private Context mContext;
	private ApplicationAdapter mAdapter;
	
	private SharedPreferences blacklist;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Enable the "back" option
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Init the blacklist
		blacklist = getSharedPreferences(ModSwipeBack.BLACKLIST, Context.MODE_WORLD_READABLE);
		
		// Init the list
		mContext = this;
		mAdapter = new ApplicationAdapter(mContext, R.id.save_blacklist, getAppList());
		readFromBlacklist();
		
		setListAdapter(mAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.blacklist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.save_blacklist:
				writeToBlacklist();
				finish();
				break;
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private ArrayList<ApplicationInfo> getAppList() {
		List<ApplicationInfo> list = mContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo info : list) {
			try {
				if (null != mContext.getPackageManager().getLaunchIntentForPackage(info.packageName) 
					&& !info.packageName.equals(mContext.getApplicationInfo().packageName) 
					&& (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					applist.add(info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return applist;
	}
	
	private void readFromBlacklist() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			boolean isBanned = blacklist.getBoolean(mAdapter.getItem(i).packageName, false);
			mAdapter.setChecked(i, isBanned);
		}
	}
	
	private void writeToBlacklist() {
		SharedPreferences.Editor edit = blacklist.edit();
		for (int i = 0; i < mAdapter.getCount(); i++) {
			edit.putBoolean(mAdapter.getItem(i).packageName, mAdapter.isChecked(i));
		}
		edit.commit();
	}
}
