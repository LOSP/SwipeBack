package us.shandian.mod.swipeback;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;

import us.shandian.mod.swipeback.app.SwipeBackActivityHelper;
import us.shandian.mod.swipeback.SwipeBackLayout;

import java.util.HashMap;

public class ModSwipeBack implements IXposedHookZygoteInit
{

	public static final String PACKAGE_NAME = ModSwipeBack.class.getPackage().getName();
	
	private HashMap<Activity, SwipeBackActivityHelper> mHelpers = new HashMap<Activity, SwipeBackActivityHelper>();
	
	@Override
	public void initZygote(StartupParam param) throws Throwable
	{
		try {
			XposedBridge.log("SwipeBack started");
			XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}



}
