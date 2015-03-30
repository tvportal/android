package com.mitechlt.tvportal.play;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//lib to email us crashes
@ReportsCrashes(formKey = "", // we dont need a formkey for email
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = R.drawable.ic_launcher,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogOkToast = R.string.crash_dialog_ok_toast,
        mailTo = "tvportalbugs@gmail.com"
)


public class TVPortalApplication extends Application {

    public static String ARG_LAUNCH_COUNT = "launch_count";
    private static Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        SharedPreferences mPrefs;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.edit().putInt(ARG_LAUNCH_COUNT, (mPrefs.getInt(ARG_LAUNCH_COUNT, 0)) + 1).apply();
        //google analytics minimal just amount of install's and sessions
        mTracker = GoogleAnalytics.getInstance(this).getTracker(Config.GA_PROPERTY_ID);
        mTracker.send(MapBuilder.createEvent("UX", "appstart", null, null)
                        .set(Fields.SESSION_CONTROL, "start")
                        .build()
        );
        //Check if the user upgraded via the server:
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppUtils.isServerUpgraded(TVPortalApplication.this);
                return null;
            }
        }.execute();

    }


}
