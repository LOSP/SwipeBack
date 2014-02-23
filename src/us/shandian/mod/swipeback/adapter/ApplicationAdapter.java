package us.shandian.mod.swipeback.adapter;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Locale;
import java.text.Collator;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import us.shandian.mod.swipeback.R;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo>
{
	private List<ApplicationInfo> mAppsList = null;
	private List<View> mViews = new ArrayList<View>();
	private Context mContext;
	private PackageManager mPackageManager;
	
	public ApplicationAdapter(Context context, int textViewResourceId,
	                          List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.mContext = context;
		this.mAppsList = appsList;
		mPackageManager = mContext.getPackageManager();
		
		// Sort in alphabetical
		Collections.sort(appsList, new Comparator<ApplicationInfo>() {

				@Override
				public int compare(ApplicationInfo p1, ApplicationInfo p2)
				{
					String name1 = p1.loadLabel(mPackageManager).toString();
					String name2 = p2.loadLabel(mPackageManager).toString();
					return Collator.getInstance().compare(name1, name2);
				}

			
		});
		
		// Add "Global"
		ApplicationInfo info = new ApplicationInfo();
		info.packageName = mContext.getResources().getString(R.string.swipe_global);
		mAppsList.add(0, info);
		
		for (int i = 0; i < mAppsList.size(); i++) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext
				                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View view = layoutInflater.inflate(R.layout.item_listview_perapp, null);
			ApplicationInfo data = mAppsList.get(i);
			if (null != data) {
				TextView appName = (TextView) view.findViewById(R.id.per_app_name);
				ImageView iconview = (ImageView) view.findViewById(R.id.per_app_icon);
				
				if (i > 0) {
					appName.setText(data.loadLabel(mPackageManager));
					iconview.setImageDrawable(data.loadIcon(mPackageManager));
				} else {
					appName.setText(data.packageName);
				}
			}
			mViews.add(view);
		}
	}
	
	@Override
	public int getCount() {
		return ((null != mAppsList) ? mAppsList.size() : 0);
	}
	
	@Override
	public ApplicationInfo getItem(int position) {
		return ((null != mAppsList) ? mAppsList.get(position) : null);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (null != mViews.get(position)) {
			view = mViews.get(position);
		}
		
		return view;
	}
}
