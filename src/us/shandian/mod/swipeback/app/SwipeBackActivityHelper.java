
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
import de.robv.android.xposed.XposedHelpers;

import us.shandian.mod.swipeback.widget.SwipeBackLayout;
import us.shandian.mod.swipeback.hook.ModSwipeBack;

/**
 * @author Yrom
 */
public class SwipeBackActivityHelper {
	public static boolean recycle = true;
	
    private Activity mActivity;
	private Context mGbContext;
	private boolean mIsTranslucent;

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
                if (recycle && !mIsTranslucent && state == SwipeBackLayout.STATE_IDLE && scrollPercent == 0) {
                    convertActivityFromTranslucent();
                }
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
				if (recycle && !mIsTranslucent) {
					convertActivityToTranslucent();
				}
            }

            @Override
            public void onScrollOverThreshold() {

            }
        });
    }

    public void onPostCreate() {
        mSwipeBackLayout.attachToActivity(mActivity);
		mIsTranslucent = isTranslucent();
		if (recycle && !mIsTranslucent) {
			convertActivityFromTranslucent();
		}
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
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
	
	public void setSensitivity(float sensitivity) {
		mSwipeBackLayout.setSensitivity(mActivity, sensitivity);
	}
	
	public boolean isTranslucent() {
		if (!recycle) return false;
		try {
			return ((ColorDrawable) mActivity.getWindow().getDecorView().getBackground()).getColor() == mActivity.getResources().getColor(android.R.color.transparent);
		} catch (Throwable t) {
			return false;
		}
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
            /*Method method = Activity.class.getDeclaredMethod("convertFromTranslucent", null);
            method.setAccessible(true);
            method.invoke(mActivity, null);*/
			XposedHelpers.callMethod(mActivity, "convertFromTranslucent");
        } catch (Throwable t) {
			XposedBridge.log(t);
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
            /*Class<?>[] classes = Activity.class.getDeclaredClasses();
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
            });*/
			XposedHelpers.callMethod(mActivity, "convertToTranslucent", null);
        } catch (Throwable t) {
			XposedBridge.log(t);
        }
    }
}
