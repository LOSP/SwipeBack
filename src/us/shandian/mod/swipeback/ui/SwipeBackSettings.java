package us.shandian.mod.swipeback.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.SharedPreferences;
import android.content.Context;

import java.util.Set;
import java.util.HashSet;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.ModSwipeBack;

public class SwipeBackSettings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private SharedPreferences prefs;
	
	private CheckBoxPreference mSwipeEnable;
	private MultiSelectListPreference mSwipeEdge;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.swipeback_settings);
		
		prefs = getSharedPreferences(ModSwipeBack.PREFS, Context.MODE_WORLD_READABLE);
		
		mSwipeEnable = (CheckBoxPreference) findPreference(ModSwipeBack.SWIPEBACK_ENABLE);
		mSwipeEnable.setChecked(prefs.getBoolean(ModSwipeBack.SWIPEBACK_ENABLE, true));
		mSwipeEnable.setOnPreferenceClickListener(this);
		
		mSwipeEdge = (MultiSelectListPreference) findPreference(ModSwipeBack.SWIPEBACK_EDGE);
		int edge = prefs.getInt(ModSwipeBack.SWIPEBACK_EDGE, 0 | ModSwipeBack.SWIPEBACK_EDGE_LEFT);
		Set<String> edges = new HashSet<String>();
		StringBuilder summary = new StringBuilder();
		if ((edge & ModSwipeBack.SWIPEBACK_EDGE_LEFT) != 0) {
			edges.add(String.valueOf(ModSwipeBack.SWIPEBACK_EDGE_LEFT));
			summary.append(getResources().getString(R.string.swipe_edge_left)).append(" ");
		}
		if ((edge & ModSwipeBack.SWIPEBACK_EDGE_RIGHT) != 0) {
			edges.add(String.valueOf(ModSwipeBack.SWIPEBACK_EDGE_RIGHT));
			summary.append(getResources().getString(R.string.swipe_edge_right)).append(" ");
		}
		if ((edge & ModSwipeBack.SWIPEBACK_EDGE_BOTTOM) != 0) {
			edges.add(String.valueOf(ModSwipeBack.SWIPEBACK_EDGE_BOTTOM));
			summary.append(getResources().getString(R.string.swipe_edge_bottom)).append(" ");
		}
		mSwipeEdge.setValues(edges);
		mSwipeEdge.setSummary(summary.toString());
		mSwipeEdge.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference == mSwipeEdge) {
			Set<String> newValues = (Set<String>) newValue;
			int edge = 0;
			StringBuilder summary = new StringBuilder();
			for (String value : newValues) {
				switch (Integer.parseInt(value)) {
					case ModSwipeBack.SWIPEBACK_EDGE_LEFT:
						edge |= ModSwipeBack.SWIPEBACK_EDGE_LEFT;
						summary.append(getResources().getString(R.string.swipe_edge_left)).append(" ");
						break;
					case ModSwipeBack.SWIPEBACK_EDGE_RIGHT:
						edge |= ModSwipeBack.SWIPEBACK_EDGE_RIGHT;
						summary.append(getResources().getString(R.string.swipe_edge_right)).append(" ");
						break;
					case ModSwipeBack.SWIPEBACK_EDGE_BOTTOM:
						edge |= ModSwipeBack.SWIPEBACK_EDGE_BOTTOM;
						summary.append(getResources().getString(R.string.swipe_edge_bottom)).append(" ");
						break;
				}
			}
			prefs.edit().putInt(ModSwipeBack.SWIPEBACK_EDGE, edge).commit();
			mSwipeEdge.setSummary(summary.toString());
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
