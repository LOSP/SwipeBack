package us.shandian.mod.swipeback.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.preference.MultiSelectListPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;
import java.util.HashSet;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.targets.ViewTarget;
import com.espian.showcaseview.targets.PointTarget;
import com.espian.showcaseview.targets.ActionViewTarget;
import com.espian.showcaseview.targets.Target;

import us.shandian.mod.swipeback.R;
import us.shandian.mod.swipeback.hook.ModSwipeBack;
import us.shandian.mod.swipeback.ui.SwipeBackBlacklist;

public class SwipeBackSettings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private final String FIRST_RUN = "first_run";
	
	private SharedPreferences prefs;
	
	private SwitchPreference mSwipeEnable;
	private MultiSelectListPreference mSwipeEdge;
	private EditTextPreference mSwipeEdgeSize;
	private SwitchPreference mSwipeBlacklist;
	
	private ConfigOptions mConfig;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.swipeback_settings);
		
		prefs = getSharedPreferences(ModSwipeBack.PREFS, Context.MODE_WORLD_READABLE);
		
		mSwipeEnable = (SwitchPreference) findPreference(ModSwipeBack.SWIPEBACK_ENABLE);
		mSwipeEnable.setChecked(prefs.getBoolean(ModSwipeBack.SWIPEBACK_ENABLE, true));
		mSwipeEnable.setOnPreferenceClickListener(this);
		mSwipeEnable.setOnPreferenceChangeListener(this);
		
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
		
		mSwipeEdgeSize = (EditTextPreference) findPreference(ModSwipeBack.SWIPEBACK_EDGE_SIZE);
		int size = prefs.getInt(ModSwipeBack.SWIPEBACK_EDGE_SIZE, 50);
		mSwipeEdgeSize.setDefaultValue(String.valueOf(size));
		mSwipeEdgeSize.setSummary(size + " dip");
		mSwipeEdgeSize.setOnPreferenceChangeListener(this);
		
		mSwipeBlacklist = (SwitchPreference) findPreference(ModSwipeBack.SWIPEBACK_BLACKLIST);
		mSwipeBlacklist.setChecked(prefs.getBoolean(ModSwipeBack.SWIPEBACK_BLACKLIST, false));
		mSwipeBlacklist.setOnPreferenceClickListener(this);
		mSwipeBlacklist.setOnPreferenceChangeListener(this);
		
		// First-run tutorial
		if (!prefs.getBoolean(FIRST_RUN, true)) return;
		
		mConfig = new ConfigOptions();
		mConfig.insert = ShowcaseView.INSERT_TO_VIEW;
		ShowcaseView showcase = ShowcaseView.insertShowcaseView(new PointTarget(0, 0), this, R.string.tutorial_welcome_title, R.string.tutorial_welcome_content, mConfig);
		showcase.setButtonText(getResources().getString(R.string.tutorial_next));
		showcase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView)
				{
					ShowcaseView showcase = ShowcaseView.insertShowcaseView(new PointTarget(0, 0), SwipeBackSettings.this, R.string.tutorial_enable_title, R.string.tutorial_enable_content, mConfig);
					showcase.setButtonText(getResources().getString(R.string.tutorial_next));
					showcase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

							@Override
							public void onShowcaseViewHide(ShowcaseView showcaseView)
							{
								ShowcaseView showcase = ShowcaseView.insertShowcaseView(new PointTarget(0, 100), SwipeBackSettings.this, R.string.tutorial_edge_title, R.string.tutorial_edge_content, mConfig);
								showcase.setButtonText(getResources().getString(R.string.tutorial_next));
								showcase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

										@Override
										public void onShowcaseViewHide(ShowcaseView showcaseView)
										{
											ShowcaseView showcase = ShowcaseView.insertShowcaseView(new PointTarget(0, 0), SwipeBackSettings.this, R.string.tutorial_over_title, R.string.tutorial_over_content, mConfig);
											showcase.setButtonText(getResources().getString(R.string.tutorial_finish));
											prefs.edit().putBoolean(FIRST_RUN, false).commit();
										}

										@Override
										public void onShowcaseViewDidHide(ShowcaseView showcaseView)
										{

										}

										@Override
										public void onShowcaseViewShow(ShowcaseView showcaseView)
										{

										}


									});
							}

							@Override
							public void onShowcaseViewDidHide(ShowcaseView showcaseView)
							{

							}

							@Override
							public void onShowcaseViewShow(ShowcaseView showcaseView)
							{

							}


						});
				}

				@Override
				public void onShowcaseViewDidHide(ShowcaseView showcaseView)
				{
					
				}

				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView)
				{
					
				}

			
		});
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
		} else if (preference == mSwipeEdgeSize) {
			int size = Integer.parseInt((String) newValue);;
			if (size <= 0) {
				size = 50;
			}
			prefs.edit().putInt(ModSwipeBack.SWIPEBACK_EDGE_SIZE, size).commit();
			mSwipeEdgeSize.setSummary(size + " dip");
		} else if (preference == mSwipeEnable) {
			prefs.edit().putBoolean(ModSwipeBack.SWIPEBACK_ENABLE, (Boolean) newValue).commit();
		} else if (preference == mSwipeBlacklist) {
			prefs.edit().putBoolean(ModSwipeBack.SWIPEBACK_BLACKLIST, (Boolean) newValue).commit();
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference == mSwipeEnable) {
			prefs.edit().putBoolean(ModSwipeBack.SWIPEBACK_ENABLE, mSwipeEnable.isChecked()).commit();
		} else if(preference == mSwipeBlacklist) {
			Intent i = new Intent();
			i.setClass(this, SwipeBackBlacklist.class);
			startActivity(i);
		}
		return true;
	}
	
}
