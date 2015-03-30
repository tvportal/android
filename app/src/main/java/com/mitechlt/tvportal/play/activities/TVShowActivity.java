package com.mitechlt.tvportal.play.activities;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.plus.PlusShare;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.mitechlt.tvportal.play.CastApplication;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.databases.FavoritesContentProvider;
import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.mitechlt.tvportal.play.fragments.SeasonsFragment;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;
import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;

import java.util.ArrayList;

public class TVShowActivity extends ActionBarActivity {

    private static final String TAG = "TVShowActivity";

    private String mSeries;
    private String mLink;
    private String mImageUri;
    private int mRating;
    private SharedPreferences mPrefs;
    private static final String ARG_SERIES = "series";
    private static final String ARG_LINK = "link";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_RATING = "rating";

    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;

    private RelativeLayout adlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VideoCastManager.checkGooglePlayServices(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setTheme(mPrefs.getString("current_theme", "holo_light").equals("holo_dark") ? R.style.AppThemeDark : R.style.AppThemeLight);

        super.onCreate(savedInstanceState);

        AppUtils.setStatusTint(this);
        Bundle extras = getIntent().getExtras();
        mSeries = extras.getString(ARG_SERIES);
        mLink = extras.getString(ARG_LINK);
        mImageUri = extras.getString(ARG_IMAGE_URI);
        mRating = extras.getInt(ARG_RATING);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_tv_show);

        adlayout = (RelativeLayout) findViewById(R.id.ad);
        if (AppUtils.canShowAds(this)) {
            //Starting RevMob session
            RevMob revmob = RevMob.start(this); // RevMob App ID configured in the AndroidManifest.xml file
            RevMobBanner banner = revmob.createBanner(this);
            ArrayList<String> interests = new ArrayList<String>();
            interests.add("games");
            interests.add("mobile");
            interests.add("advertising");
            revmob.setUserInterests(interests);
            adlayout.addView(banner);
            adlayout.setGravity(Gravity.BOTTOM);
        }else{
            adlayout.setVisibility(View.GONE);
        }


        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, SeasonsFragment.newInstance(mSeries, mLink, mImageUri, mRating));
            ft.commit();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mSeries);

        if (AppUtils.isChromecastPluginInstalled(this)) {
            mCastManager = CastApplication.getCastManager(this.getApplicationContext());
        }
        // -- Adding MiniController
        mMini = (MiniController) findViewById(R.id.miniController1);
        if (mCastManager != null) {
            mCastManager.addMiniController(mMini);
        }

        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                AppUtils.showToast(TVShowActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                AppUtils.showToast(TVShowActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
                if (!SettingsActivity.isFtuShown(TVShowActivity.this)) {
                    SettingsActivity.setFtuShown(TVShowActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000);
                }
            }
        };

        if (mCastManager != null) {
            mCastManager.reconnectSessionIfPossible(this, false);
        }
    }

    @Override
    protected void onResume() {
        if (AppUtils.isChromecastPluginInstalled(this)) {
            mCastManager = CastApplication.getCastManager(this);
        }
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
            mCastManager.removeVideoCastConsumer(mCastConsumer);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastManager.clearContext(this);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie_menu, menu);

        if (mCastManager != null) {
            mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (AppUtils.isFavorite(this, mSeries)) {
            menu.findItem(R.id.action_star).setIcon(R.drawable.ic_action_starred);
        } else {
            menu.findItem(R.id.action_star).setIcon(R.drawable.ic_action_unstarred);
        }
        if (AppUtils.isChromecastPluginInstalled(this)) {
            menu.removeItem(R.id.cast_upgrade);
        }
        if (!AppUtils.isChromecastPluginInstalled(this)) {
            menu.removeItem(R.id.media_route_menu_item);

        }
        if (AppUtils.isPremiumInstalled(this)) {
            menu.removeItem(R.id.action_upgrade);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings: {
                Intent intent = new Intent(TVShowActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_share: {
                Intent shareIntent = new PlusShare.Builder(this)
                        .setType("text/plain")
                        .setText("I'm watching free TV Shows and Movies with TV Portal!\n\nCheck it out:")
                        .setContentUrl(Uri.parse(Config.GOOGLE_PLUS_URL))
                        .getIntent();

                startActivityForResult(shareIntent, 0);
                break;
            }
            case R.id.cast_upgrade: {
                final Dialog dialog = new Dialog(TVShowActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_cast);

                Button button = (Button) dialog.findViewById(R.id.button1);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage("com.android.vending");
                            intent.setData(Uri.parse("market://details?id=" + Config.PLAY_STORE_CAST_UPGRADE_PACKAGE_NAME));
                            startActivity(intent);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SLIDEME_UPGRADE_URL)));
                        }
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            }

            case R.id.action_social: {
                AppUtils.showSocialDialog(this, true);
                break;
            }

            case R.id.action_star: {
                if (!AppUtils.isFavorite(this, mSeries)) {
                    ContentValues values = new ContentValues();
                    values.put(FavoritesTable.COLUMN_TITLE, mSeries);
                    values.put(FavoritesTable.COLUMN_LINK, mLink);
                    values.put(FavoritesTable.COLUMN_TYPE, AppUtils.TVSHOW);
                    values.put(FavoritesTable.COLUMN_IMAGE, mImageUri);
                    values.put(FavoritesTable.COLUMN_RATING, mRating);
                    getContentResolver().insert(FavoritesContentProvider.CONTENT_URI, values);
                    Toast.makeText(this, mSeries + getString(R.string.favourites_added), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, mSeries + getString(R.string.favourites_removed), Toast.LENGTH_SHORT).show();
                    mSeries = mSeries.replace("'", "''");
                    String where = FavoritesTable.COLUMN_TITLE + "='" + mSeries + "'";
                    getContentResolver().delete(FavoritesContentProvider.CONTENT_URI, where, null);
                }

                supportInvalidateOptionsMenu();
                return true;
            }
            case R.id.action_upgrade: {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.android.vending");
                    intent.setData(Uri.parse("market://details?id=" + Config.PLAY_STORE_UPGRADE_PACKAGE_NAME));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SLIDEME_UPGRADE_URL)));
                }
                break;
            }

            case android.R.id.home: {
                finish();
                return true;
            }

            case R.id.action_themes: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.change_themes).setItems(R.array.themes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                mPrefs.edit().putString("current_theme", "holo_light").commit();
                                break;
                            }
                            case 1: {
                                mPrefs.edit().putString("current_theme", "holo_dark").commit();
                                break;
                            }
                        }
                        if (AppUtils.hasHoneycomb()) {
                            recreate();
                        } else {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    }
                });

                builder.show();

                break;
            }

            case R.id.action_about: {
                final WebView webView = new WebView(this);
                webView.loadUrl("file:///android_res/raw/info.html");
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_about)
                        .setView(webView)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                break;
            }

            case R.id.action_feedback: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://plus.google.com/u/2/communities/116034521997245237036/"));
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isCastConnected() {
        if (mCastManager != null) {
            return mCastManager.isConnected();
        }
        return false;
    }

    private void showFtu() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCastManager != null && mCastManager.isConnected()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                changeVolume(CastApplication.VOLUME_INCREMENT);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                changeVolume(-CastApplication.VOLUME_INCREMENT);
            } else {
                // we don't want to consume non-volume key events
                return super.onKeyDown(keyCode, event);
            }
            if (mCastManager.getPlaybackStatus() == MediaStatus.PLAYER_STATE_PLAYING) {
                return super.onKeyDown(keyCode, event);
            } else {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeVolume(double volumeIncrement) {
        if (mCastManager == null) {
            return;
        }
        try {
            mCastManager.incrementVolume(volumeIncrement);
        } catch (Exception e) {
            Log.e(TAG, "onVolumeChange() Failed to change volume", e);
            AppUtils.handleException(this, e);
        }
    }
}