package us.shandian.mod.swipeback.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.preference.MultiSelectListPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;
import java.util.HashSet;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.provider.SettingsProvider;
import us.shandian.mod.swipeback.ui.SwipeBackPerApp;
import us.shandian.mod.swipeback.ui.preference.SeekBarPreference;

public class SwipeBackSettings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private SwitchPreference mSwipeEnable;
	private SwitchPreference mRecycleSurface;
	private MultiSelectListPreference mSwipeEdge;
	private EditTextPreference mSwipeEdgeSize;
	private SeekBarPreference mSwipeSensitivity;
	
	private String prefix;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		getActionBar().setTitle(extras.getString("us.shandian.mod.swipeback.TITLE"));
		prefix = extras.getString("us.shandian.mod.swipeback.PREFIX");
		
		addPreferencesFromResource(R.xml.swipeback_settings);
		
		mSwipeEnable = (SwitchPreference) findPreference(SettingsProvider.SWIPEBACK_ENABLE);
		mSwipeEnable.setChecked(SettingsProvider.getBoolean(this, prefix, SettingsProvider.SWIPEBACK_ENABLE, true));
		mSwipeEnable.setOnPreferenceClickListener(this);
		mSwipeEnable.setOnPreferenceChangeListener(this);
		
		mRecycleSurface = (SwitchPreference) findPreference(SettingsProvider.SWIPEBACK_RECYCLE_SURFACE);
		mRecycleSurface.setChecked(SettingsProvider.getBoolean(this, prefix, SettingsProvider.SWIPEBACK_RECYCLE_SURFACE, true));
		mRecycleSurface.setOnPreferenceClickListener(this);
		mRecycleSurface.setOnPreferenceChangeListener(this);
		
		mSwipeEdge = (MultiSelectListPreference) findPreference(SettingsProvider.SWIPEBACK_EDGE);
		int edge = SettingsProvider.getInt(this, prefix, SettingsProvider.SWIPEBACK_EDGE, 0 | SettingsProvider.SWIPEBACK_EDGE_LEFT);
		Set<String> edges = new HashSet<String>();
		StringBuilder summary = new StringBuilder();
		if ((edge & SettingsProvider.SWIPEBACK_EDGE_LEFT) != 0) {
			edges.add(String.valueOf(SettingsProvider.SWIPEBACK_EDGE_LEFT));
			summary.append(getResources().getString(R.string.swipe_edge_left)).append(" ");
		}
		if ((edge & SettingsProvider.SWIPEBACK_EDGE_RIGHT) != 0) {
			edges.add(String.valueOf(SettingsProvider.SWIPEBACK_EDGE_RIGHT));
			summary.append(getResources().getString(R.string.swipe_edge_right)).append(" ");
		}
		if ((edge & SettingsProvider.SWIPEBACK_EDGE_BOTTOM) != 0) {
			edges.add(String.valueOf(SettingsProvider.SWIPEBACK_EDGE_BOTTOM));
			summary.append(getResources().getString(R.string.swipe_edge_bottom)).append(" ");
		}
		mSwipeEdge.setValues(edges);
		mSwipeEdge.setSummary(summary.toString());
		mSwipeEdge.setOnPreferenceChangeListener(this);
		
		mSwipeEdgeSize = (EditTextPreference) findPreference(SettingsProvider.SWIPEBACK_EDGE_SIZE);
		int size = SettingsProvider.getInt(this, prefix, SettingsProvider.SWIPEBACK_EDGE_SIZE, 50);
		mSwipeEdgeSize.setDefaultValue(String.valueOf(size));
		mSwipeEdgeSize.setSummary(size + " dip");
		mSwipeEdgeSize.setOnPreferenceChangeListener(this);
		
		mSwipeSensitivity = (SeekBarPreference) findPreference(SettingsProvider.SWIPEBACK_SENSITIVITY);
		float sensitivity = SettingsProvider.getFloat(this, prefix, SettingsProvider.SWIPEBACK_SENSITIVITY, 1.0f);
		mSwipeSensitivity.setValue((int) (sensitivity * 100));
		
		mSwipeSensitivity.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
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
					case SettingsProvider.SWIPEBACK_EDGE_LEFT:
						edge |= SettingsProvider.SWIPEBACK_EDGE_LEFT;
						summary.append(getResources().getString(R.string.swipe_edge_left)).append(" ");
						break;
					case SettingsProvider.SWIPEBACK_EDGE_RIGHT:
						edge |= SettingsProvider.SWIPEBACK_EDGE_RIGHT;
						summary.append(getResources().getString(R.string.swipe_edge_right)).append(" ");
						break;
					case SettingsProvider.SWIPEBACK_EDGE_BOTTOM:
						edge |= SettingsProvider.SWIPEBACK_EDGE_BOTTOM;
						summary.append(getResources().getString(R.string.swipe_edge_bottom)).append(" ");
						break;
				}
			}
			SettingsProvider.putInt(this, prefix, SettingsProvider.SWIPEBACK_EDGE, edge);
			mSwipeEdge.setSummary(summary.toString());
		} else if (preference == mSwipeEdgeSize) {
			int size = Integer.parseInt((String) newValue);;
			if (size <= 0) {
				size = 50;
			}
			SettingsProvider.putInt(this, prefix, SettingsProvider.SWIPEBACK_EDGE_SIZE, size);
			mSwipeEdgeSize.setSummary(size + " dip");
		} else if (preference == mSwipeEnable) {
			SettingsProvider.putBoolean(this, prefix, SettingsProvider.SWIPEBACK_ENABLE, (Boolean) newValue);
		} else if (preference == mRecycleSurface) {
			SettingsProvider.putBoolean(this, prefix, SettingsProvider.SWIPEBACK_RECYCLE_SURFACE, (Boolean) newValue);
		} else if (preference == mSwipeSensitivity) {
			SettingsProvider.putFloat(this, prefix, SettingsProvider.SWIPEBACK_SENSITIVITY, (Integer) newValue / 100.0f);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference == mSwipeEnable) {
			SettingsProvider.putBoolean(this, prefix, SettingsProvider.SWIPEBACK_ENABLE, mSwipeEnable.isChecked());
		} else if (preference == mRecycleSurface) {
			SettingsProvider.putBoolean(this, prefix, SettingsProvider.SWIPEBACK_RECYCLE_SURFACE, mRecycleSurface.isChecked());
		}
		return true;
	}
	
}
