package com.mitechlt.tvportal.play.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.CastApplication;


public class SettingsActivity extends PreferenceActivity {

    public static final String ARG_DEFAULT_PLAYER = "default_player";
    public static final String FTU_SHOWN_KEY = "ftu_shown";
    public static final String VOLUME_SELCTION_KEY = "volume_target";

    private VideoCastManager mCastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mCastManager = CastApplication.getCastManager(this);

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.application_preference);

        final Preference clearDefaultPlayer = findPreference("pref_clear_default_player");
        if (clearDefaultPlayer != null) {
            clearDefaultPlayer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    prefs.edit().remove(ARG_DEFAULT_PLAYER).apply();
                    return true;
                }
            });
        }
    }

    public static boolean isFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(FTU_SHOWN_KEY, false);
    }

    public static void setFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putBoolean(FTU_SHOWN_KEY, true).commit();
    }

    @Override
    protected void onResume() {
        if (null != mCastManager) {
            mCastManager.incrementUiCounter();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (null != mCastManager) {
            mCastManager.decrementUiCounter();
        }
        super.onPause();
    }
}
