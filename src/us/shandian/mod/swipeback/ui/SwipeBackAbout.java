package us.shandian.mod.swipeback.ui;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import us.shandian.mod.swipeback.R;
import android.view.*;

public class SwipeBackAbout extends PreferenceActivity implements OnPreferenceClickListener
{
	private final String SWIPE_VERSION = "swipe_version";
	private final String SWIPE_COMMUNITY = "swipe_community";
	
	private Preference mVersion;
	private Preference mCommunity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.swipeback_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Get version name
		mVersion = findPreference(SWIPE_VERSION);
		String version;
		try {
			version = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionName;
		} catch (Exception e) {
			version = "0.0.0";
		}
		mVersion.setSummary(version);
		
		mCommunity = findPreference(SWIPE_COMMUNITY);
		mCommunity.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference == mCommunity) {
			// Access the community url
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.swipe_community_url)));
			startActivity(intent);
			return true;
		} else {
			return false;
		}
	}
}
