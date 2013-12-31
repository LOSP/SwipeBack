package us.shandian.mod.swipeback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.provider.Settings;
import android.database.ContentObserver;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

import us.shandian.mod.swipeback.app.SwipeBackActivityHelper;
import us.shandian.mod.swipeback.SwipeBackLayout;

import java.util.ArrayList;

public class ModSwipeBack implements IXposedHookZygoteInit, IXposedHookLoadPackage
{

	public static final String PACKAGE_NAME = ModSwipeBack.class.getPackage().getName();
	public static final String PREFS = "SwipeBackSettings";
	public static final String BLACKLIST = "SwipeBackBlacklist";
	
	public static final String SWIPEBACK_ENABLE = "swipeback_enable";
	public static final String SWIPEBACK_EDGE = "swipeback_edge";
	
	public static final int SWIPEBACK_EDGE_LEFT = 1;
	public static final int SWIPEBACK_EDGE_RIGHT = 2;
	public static final int SWIPEBACK_EDGE_BOTTOM = 4;
	
	private ArrayList<String> mBannedPackages = new ArrayList<String>();
	
	private static XSharedPreferences prefs;
	private static XSharedPreferences blacklist;
	
	@Override
	public void initZygote(StartupParam param) throws Throwable
	{
		try {
			loadBannedApps();
			
			prefs = new XSharedPreferences(PACKAGE_NAME, PREFS);
			prefs.makeWorldReadable();
			
			blacklist = new XSharedPreferences(PACKAGE_NAME, BLACKLIST);
			blacklist.makeWorldReadable();
			
			XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						banLaunchers(activity);
						
						if (isAppBanned(activity.getApplication().getApplicationInfo().packageName)) {
							return;
						}
						
						// Request for rotation
						boolean isRotationLocked = (Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0);
						if (!isRotationLocked) {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						}
						ContentObserver mObserver = new RotateObserver(activity, new RotateHandler(activity));
						activity.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, mObserver);
						XposedHelpers.setAdditionalInstanceField(activity, "mObserver", mObserver);
						
						// Do this only when enabled
						prefs.reload();
						if (prefs.getBoolean(SWIPEBACK_ENABLE, true)) {
							SwipeBackActivityHelper helper = new SwipeBackActivityHelper(activity);
							helper.onActivityCreate();
							helper.getSwipeBackLayout().setEnableGesture(true);
							// Get the egde
							int edge = prefs.getInt(SWIPEBACK_EDGE, 0 | SWIPEBACK_EDGE_LEFT);
							int trackEdge = 0;
							if ((edge & SWIPEBACK_EDGE_LEFT) != 0) {
								trackEdge |= SwipeBackLayout.EDGE_LEFT;
							}
							if ((edge & SWIPEBACK_EDGE_RIGHT) != 0) {
								trackEdge |= SwipeBackLayout.EDGE_RIGHT;
							}
							if ((edge & SWIPEBACK_EDGE_BOTTOM) != 0) {
								trackEdge |= SwipeBackLayout.EDGE_BOTTOM;
							}
							helper.getSwipeBackLayout().setEdgeTrackingEnabled(trackEdge);
							
							XposedHelpers.setAdditionalInstanceField(activity, "mSwipeHelper", helper);
						}
					}
			});
			
			XposedHelpers.findAndHookMethod(Activity.class, "onPostCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						SwipeBackActivityHelper helper = (SwipeBackActivityHelper) XposedHelpers.getAdditionalInstanceField(activity, "mSwipeHelper");
						
						// Try to ignore dialogs
						Class<?> styleable = XposedHelpers.findClass("com.android.internal.R.styleable", null);
						int Window_windowIsFloating = XposedHelpers.getStaticIntField(styleable, "Window_windowIsFloating");
						boolean windowIsFloating = activity.getWindow().getWindowStyle().getBoolean(Window_windowIsFloating, false);
						if (windowIsFloating) {
							helper = null;
							XposedHelpers.removeAdditionalInstanceField(activity, "mSwipeHelper");
						}
						
						if (helper != null) {
							helper.onPostCreate();
						}
					}
			});
			
			XposedHelpers.findAndHookMethod(Activity.class, "findViewById", int.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable
					{
						Activity activity = (Activity) param.thisObject;
						SwipeBackActivityHelper helper = (SwipeBackActivityHelper) XposedHelpers.getAdditionalInstanceField(activity, "mSwipeHelper");
						Object ret = param.getResult();
						if (ret == null && helper != null) {
							ret = helper.findViewById((Integer) param.args[0]);
							param.setResult(ret);
						}
					}
			});
			
			XposedHelpers.findAndHookMethod(Activity.class, "finish", new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable
					{
						// Unregister the rotate observer
						Activity activity = (Activity) param.thisObject;
						ContentObserver mObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(activity, "mObserver");
						if (mObserver != null) {
							activity.getContentResolver().unregisterContentObserver(mObserver);
							XposedHelpers.removeAdditionalInstanceField(activity, "mObserver");
						}
					}
				});
			
			Class<?> activityRecord = XposedHelpers.findClass("com.android.server.am.ActivityRecord", null);
			XposedBridge.hookAllConstructors(activityRecord, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						// If not enabled, ignore
						prefs.reload();
						if (!prefs.getBoolean(SWIPEBACK_ENABLE, true)) return;
						
						String packageName = (String) XposedHelpers.getObjectField(param.thisObject, "packageName");
						boolean isHomeActivity = false;
						
						// Try to ignore home activities
						if (Build.VERSION.SDK_INT >= 19) {
							isHomeActivity = (Boolean) XposedHelpers.callMethod(param.thisObject, "isHomeActivity", new Object[0]);
						} else {
							isHomeActivity = XposedHelpers.getBooleanField(param.thisObject, "isHomeActivity");
						}
						
						if (!isHomeActivity && !isAppBanned(packageName)) {
							// Force set to translucent
							XposedHelpers.setBooleanField(param.thisObject, "fullscreen", false);
						}
					}
			});
			
			// The following hacks are only for releases newer than 4.4 1
			if (!newerThanRelease("4.4.1")) return;
			
			XposedHelpers.findAndHookMethod("com.android.server.am.ActivityStack", null, "isActivityOverHome", activityRecord, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
					{
						// This method code is from old Android version
						// Will fix translucent stack display
						// Do nothing on older releases
						Object recordTask = XposedHelpers.getObjectField(param.args[0], "task");
						Object activities = XposedHelpers.getObjectField(recordTask, "mActivities");
						int rIndex = (Integer) XposedHelpers.callMethod(activities, "indexOf", param.args[0]);
						for (--rIndex; rIndex >= 0; --rIndex) {
							// Look down in tasks
							final Object blocker = XposedHelpers.callMethod(activities, "get", rIndex);
							boolean finishing = XposedHelpers.getBooleanField(blocker, "finishing");
							if (!finishing) {
								break;
							}
						}
							
						// Arrived bottom, but nothing found
						if (rIndex < 0) {
							return true;
						}
						
						return false;
					}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}


	@Override
	public void handleLoadPackage(LoadPackageParam param) throws Throwable {
		try {
			Class<?> classImid= XposedHelpers.findClass("me.imid.swipebacklayout.lib.SwipeBackLayout", param.classLoader);
		} catch (XposedHelpers.ClassNotFoundError e) {
			return;
		}
		mBannedPackages.add(param.packageName);
	}
	
	private boolean isAppBanned(String packageName) {
		blacklist.reload();
		if (blacklist.getBoolean(packageName, false)) {
			return true;
		}
		
		for (String name : mBannedPackages) {
			if (name.equals(packageName)) {
				return true;
			}
		}
		return false;
	}
	
	private void loadBannedApps() {
		mBannedPackages.add("com.android.systemui");
		mBannedPackages.add("com.android.internal");
	}
	
	private void banLaunchers(Context context) {
		ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(context.getPackageManager(), 0);
		if (homeInfo != null) {
			if (!isAppBanned(homeInfo.packageName)) {
				mBannedPackages.add(homeInfo.packageName);
			}
		}
	}
	
	private boolean newerThanRelease(String r) {
		int release = Integer.parseInt(Build.VERSION.RELEASE.replace(".", ""));
		int compare = Integer.parseInt(r.replace(".", ""));
		if (release < 100) {
			release = release * 10;
		}
		
		if (compare < 100) {
			compare = compare * 10;
		}
		
		return (release >= compare);
		
	}

	private class RotateObserver extends ContentObserver {
		private Activity mActivity;
		private Handler mHandler;
		
		public RotateObserver(Activity activity, Handler handler) {
			super(handler);
			mActivity = activity;
			mHandler = handler;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			boolean isRotationLocked = (Settings.System.getInt(mActivity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0);
			mHandler.sendMessage(mHandler.obtainMessage(0, isRotationLocked));
		}
	}
	
	private class RotateHandler extends Handler {
		private Activity mActivity;
		
		public RotateHandler(Activity activity) {
			mActivity = activity;
		}

		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == 0) {
				if (!(Boolean) msg.obj) {
					mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				} else {
					mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
				}
			}
		}
	}
}
