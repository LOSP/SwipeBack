package us.shandian.mod.swipeback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;

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

import java.util.HashMap;
import java.util.ArrayList;

public class ModSwipeBack implements IXposedHookZygoteInit, IXposedHookLoadPackage
{

	public static final String PACKAGE_NAME = ModSwipeBack.class.getPackage().getName();
	
	private ArrayList<String> mBannedPackages = new ArrayList<String>();
	
	private HashMap<Activity, SwipeBackActivityHelper> mHelpers = new HashMap<Activity, SwipeBackActivityHelper>();
	
	@Override
	public void initZygote(StartupParam param) throws Throwable
	{
		try {
			loadBannedApps();
			
			XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						banLaunchers(activity);
						if (isAppBanned(activity.getApplication().getApplicationInfo().packageName)) {
							return;
						}
						SwipeBackActivityHelper helper = new SwipeBackActivityHelper(activity);
						try {
							helper.onActivityCreate();
							helper.getSwipeBackLayout().setEnableGesture(true);
							helper.getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTTOM);
						} catch (Throwable t) {
							XposedBridge.log(t);
							helper = null;
						}
						mHelpers.put(activity, helper);
					}
			});
			
			XposedHelpers.findAndHookMethod(Activity.class, "onPostCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						SwipeBackActivityHelper helper = mHelpers.get(activity);
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
						SwipeBackActivityHelper helper = mHelpers.get(activity);
						Object ret = param.getResult();
						if (ret == null && helper != null) {
							ret = helper.findViewById((Integer) param.args[0]);
							param.setResult(ret);
						}
					}
			});
			
			XposedHelpers.findAndHookMethod(Activity.class, "finish", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						SwipeBackActivityHelper helper = mHelpers.get(activity);
						if (helper != null) {
							mHelpers.remove(activity);
						}
					}
			});
			
			Class<?> activityRecord = XposedHelpers.findClass("com.android.server.am.ActivityRecord", null);
			XposedBridge.hookAllConstructors(activityRecord, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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

}
