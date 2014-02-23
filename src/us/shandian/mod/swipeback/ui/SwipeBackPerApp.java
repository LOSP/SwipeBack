package us.shandian.mod.swipeback.ui;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;
import java.util.ArrayList;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.provider.SettingsProvider;
import us.shandian.mod.swipeback.adapter.ApplicationAdapter;

public class SwipeBackPerApp extends ListActivity implements OnItemClickListener
{
	private Context mContext;
	private ApplicationAdapter mAdapter;
	private ProgressDialog mDialog;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == 0) {
				setListAdapter(mAdapter);
				mDialog.dismiss();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Listener
		getListView().setOnItemClickListener(this);
		
		// Init the list
		mContext = this;
		mDialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.please_wait), true, false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				mAdapter = new ApplicationAdapter(mContext, R.id.save_blacklist, getAppList());
				mHandler.sendEmptyMessage(0);
			}
		}).start();
	}
	
	private ArrayList<ApplicationInfo> getAppList() {
		
		List<ApplicationInfo> list = mContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo info : list) {
			try {
				if (null != mContext.getPackageManager().getLaunchIntentForPackage(info.packageName) 
					&& !info.packageName.equals(mContext.getApplicationInfo().packageName)) {
					applist.add(info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return applist;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Bundle bundle = new Bundle();
		if (position == 0) {
			bundle.putString("us.shandian.mod.swipeback.PREFIX", SettingsProvider.PREFIX_GLOBAL);
		} else {
			bundle.putString("us.shandian.mod.swipeback.PREFIX", mAdapter.getItem(position).packageName);
		}
		bundle.putString("us.shandian.mod.swipeback.TITLE", ((TextView) view.findViewById(R.id.per_app_name)).getText().toString());
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_DEFAULT);
		intent.setClass(mContext, SwipeBackSettings.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
