package com.mitechlt.tvportal.play;

import android.app.Application;
import android.content.Context;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.mitechlt.tvportal.play.utils.Config;

public class CastApplication extends Application {
    private static VideoCastManager mCastMgr = null;
    public static final double VOLUME_INCREMENT = 0.05;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppContext = getApplicationContext();
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);

    }

    public static VideoCastManager getCastManager(Context context) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(context, Config.CHROMECAST_APPLICATION_ID,
                    null, null);
            mCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_LOCKSCREEN |
                            VideoCastManager.FEATURE_DEBUGGING
            );

        }
        mCastMgr.setContext(context);
        return mCastMgr;
    }

}
