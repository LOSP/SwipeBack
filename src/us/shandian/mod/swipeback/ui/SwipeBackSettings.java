package us.shandian.mod.swipeback.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.SharedPreferences;
import android.content.Context;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.ModSwipeBack;

public class SwipeBackSettings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private SharedPreferences prefs;
	
	private CheckBoxPreference mSwipeEnable;
	private ListPreference mSwipeEdge;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.swipeback_settings);
		
		prefs = getSharedPreferences(ModSwipeBack.PREFS, Context.MODE_WORLD_READABLE);
		
		mSwipeEnable = (CheckBoxPreference) findPreference(ModSwipeBack.SWIPEBACK_ENABLE);
		mSwipeEnable.setChecked(prefs.getBoolean(ModSwipeBack.SWIPEBACK_ENABLE, true));
		mSwipeEnable.setOnPreferenceClickListener(this);
		
		mSwipeEdge = (ListPreference) findPreference(ModSwipeBack.SWIPEBACK_EDGE);
		mSwipeEdge.setValue(String.valueOf(prefs.getInt(ModSwipeBack.SWIPEBACK_EDGE, 0)));
		mSwipeEdge.setSummary(mSwipeEdge.getEntry());
		mSwipeEdge.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference == mSwipeEdge) {
			int value = Integer.parseInt((String) newValue);
			int index = mSwipeEdge.findIndexOfValue(String.valueOf(value));
			prefs.edit().putInt(ModSwipeBack.SWIPEBACK_EDGE, value).commit();
			mSwipeEdge.setSummary(mSwipeEdge.getEntries()[index]);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference == mSwipeEnable) {
			prefs.edit().putBoolean(ModSwipeBack.SWIPEBACK_ENABLE, mSwipeEnable.isChecked()).commit();
		}
		return true;
	}
	
}
