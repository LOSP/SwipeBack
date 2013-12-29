
package us.shandian.mod.swipeback.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;

import us.shandian.mod.swipeback.SwipeBackLayout;
import us.shandian.mod.swipeback.ModSwipeBack;

/**
 * @author Yrom
 */
public class SwipeBackActivityHelper {
    private Activity mActivity;
	private Context mGbContext;

    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackActivityHelper(Activity activity) {
        mActivity = activity;
		try {
			mGbContext = mActivity.createPackageContext(ModSwipeBack.PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
    }

    @SuppressWarnings("deprecation")
    public void onActivityCreate() {
        // mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        // mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        // mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mGbContext).inflate(
        //        us.shandian.mod.swipeback.R.layout.swipeback_layout, null);
		mSwipeBackLayout = new SwipeBackLayout(mActivity, mGbContext);
		// mSwipeBackLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                if (state == SwipeBackLayout.STATE_IDLE && scrollPercent == 0) {
                    convertActivityFromTranslucent();
                }
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                convertActivityToTranslucent();
            }

            @Override
            public void onScrollOverThreshold() {

            }
        });
    }

    public void onPostCreate() {
        mSwipeBackLayout.attachToActivity(mActivity);
        convertActivityFromTranslucent();
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }

    public View findViewById(int id) {
        if (mSwipeBackLayout != null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    /**
     * Convert a translucent themed Activity
     * {@link android.R.attr#windowIsTranslucent} to a fullscreen opaque
     * Activity.
     * <p>
     * Call this whenever the background of a translucent Activity has changed
     * to become opaque. Doing so will allow the {@link android.view.Surface} of
     * the Activity behind to be released.
     * <p>
     * This call has no effect on non-translucent activities or on activities
     * with the {@link android.R.attr#windowIsFloating} attribute.
     */
    public void convertActivityFromTranslucent() {
        try {
            Method method = Activity.class.getDeclaredMethod("convertFromTranslucent", null);
            method.setAccessible(true);
            method.invoke(mActivity, null);
        } catch (Throwable t) {
        }
    }

    /**
     * Convert a translucent themed Activity
     * {@link android.R.attr#windowIsTranslucent} back from opaque to
     * translucent following a call to {@link #convertActivityFromTranslucent()}
     * .
     * <p>
     * Calling this allows the Activity behind this one to be seen again. Once
     * all such Activities have been redrawn
     * <p>
     * This call has no effect on non-translucent activities or on activities
     * with the {@link android.R.attr#windowIsFloating} attribute.
     */
    public void convertActivityToTranslucent() {
        try {
            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
                    translucentConversionListenerClazz);
            method.setAccessible(true);
            method.invoke(mActivity, new Object[] {
                null
            });
        } catch (Throwable t) {
        }
    }
}
