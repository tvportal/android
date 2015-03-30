package com.mitechlt.tvportal.play.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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

import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.plus.PlusShare;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.mitechlt.tvportal.play.CastApplication;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.adapters.PagerAdapter;
import com.mitechlt.tvportal.play.fragments.FavoritesFragment;
import com.mitechlt.tvportal.play.fragments.MoviesFragment;
import com.mitechlt.tvportal.play.fragments.RecentFragment;
import com.mitechlt.tvportal.play.fragments.TVShowFragment;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;
import com.mitechlt.tvportal.play.views.SlidingTabLayout;
import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private final String TAG = ((Object) this).getClass().getSimpleName();

    private SharedPreferences mPrefs;
    private PagerAdapter mAdapter;
    private SearchView mSearchView;
    private static final String ARG_FIRST_RUN = "first_run";

    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;

    private RelativeLayout adlayout;

    private String mQuery;

    @Override
    protected void onNewIntent(Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
            if (mAdapter != null) {

                String query = intent.getStringExtra(SearchManager.QUERY);

                Fragment fragment = mAdapter.getItem(2);
                if (fragment instanceof MoviesFragment) {
                    ((MoviesFragment) fragment).restartWithSearchQuery(query);
                }
                fragment = mAdapter.getItem(3);
                if (fragment instanceof TVShowFragment) {
                    ((TVShowFragment) fragment).restartWithSearchQuery(query);
                }

                if (mSearchView != null) {
                    mSearchView.setQuery(query, false);
                    mSearchView.clearFocus();
                }
            }

        } else {
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setTheme(mPrefs.getString("current_theme", "holo_light").equals("holo_dark") ? R.style.AppThemeDark : R.style.AppThemeLight);

        super.onCreate(savedInstanceState);
        if (!isFullScreen()) {
            AppUtils.setStatusTint(this);
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_main);

        AppUtils.performAsyncVersionCheck(this);

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

        mAdapter = new PagerAdapter(this, R.array.tab_titles);

        mAdapter.add(RecentFragment.newInstance());
        mAdapter.add(FavoritesFragment.newInstance());
        mAdapter.add(MoviesFragment.newInstance());
        mAdapter.add(TVShowFragment.newInstance());

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        //holds page in memory so sliding doesnt loose focus and have to reload
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(2);

        final SlidingTabLayout slidingTabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabs.setShouldExpand(false);
        slidingTabs.setViewPager(pager);

        //AppUtils.showPromoDialog(this);
        AppUtils.showStartupDialog(this);

        if (getIntent().getBooleanExtra(ARG_FIRST_RUN, true) && savedInstanceState == null) {
            AppUtils.showSocialDialog(this, false);
        }

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
                AppUtils.showToast(MainActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                AppUtils.showToast(MainActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
                if (!SettingsActivity.isFtuShown(MainActivity.this)) {
                    SettingsActivity.setFtuShown(MainActivity.this);

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
    protected void onSaveInstanceState(Bundle outState) {

        // Don't call through to onSaveInstanceState, so our fragments are not saved.
        // This means we don't create new instances of our fragments,
        // so future calls to restart their loaders won't fail

        //super.onSaveInstanceState(outState);
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

        getMenuInflater().inflate(R.menu.main, menu);

        if (mCastManager != null) {
            mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    if (query == null || TextUtils.isEmpty(query)) {
                        return false;
                    }

                    if (query.toLowerCase().trim().equals("loyal tv portal fan") || query.toLowerCase().trim().equals("loyal tvportal fan")) {
                        AppUtils.makeLoyaltyUpgradeDialog(MainActivity.this);
                        return true;
                    }

                    if (query.toLowerCase().trim().equals("reddit")) {
                        AppUtils.makeEasterEggDialog(MainActivity.this);
                        return true;
                    }

                    if (mAdapter != null) {
                        Fragment fragment = mAdapter.getItem(2);
                        if (fragment instanceof MoviesFragment) {
                            ((MoviesFragment) fragment).restartWithSearchQuery(query);
                        }
                        fragment = mAdapter.getItem(3);
                        if (fragment instanceof TVShowFragment) {
                            ((TVShowFragment) fragment).restartWithSearchQuery(query);
                        }
                    }
                    mSearchView.clearFocus();

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {

                    if (mQuery != null && mQuery.equals(query)) {
                        return false;
                    }

                    if (mAdapter != null) {
                        Fragment fragment = mAdapter.getItem(2);
                        if (fragment instanceof MoviesFragment) {
                            ((MoviesFragment) fragment).restartWithSearchQuery(query);
                        }
                        fragment = mAdapter.getItem(3);
                        if (fragment instanceof TVShowFragment) {
                            ((TVShowFragment) fragment).restartWithSearchQuery(query);
                        }
                    }

                    mQuery = query;

                    return true;
                }
            });
        }


        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                if (mAdapter != null) {
                    Fragment fragment = mAdapter.getItem(2);
                    if (fragment instanceof MoviesFragment) {
                        ((MoviesFragment) fragment).restartWithSearchQuery(null);
                    }
                    fragment = mAdapter.getItem(3);
                    if (fragment instanceof TVShowFragment) {
                        ((TVShowFragment) fragment).restartWithSearchQuery(null);
                    }
                }
                return true;
            }
        });

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchableInfo);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (AppUtils.isPremiumInstalled(this)) {
            menu.removeItem(R.id.action_upgrade);
        }
        if (!AppUtils.isChromecastPluginInstalled(this)) {
            menu.removeItem(R.id.media_route_menu_item);

        }
        if (AppUtils.isChromecastPluginInstalled(this)) {
            menu.removeItem(R.id.cast_upgrade);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean isFullScreen() {
        int flg = getWindow().getAttributes().flags;
        boolean flag = false;
        if ((flg & 1024) == 1024) {
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_search) {
            if (mSearchView != null) {
                mSearchView.setIconified(false);
            }
        }

        switch (id) {

            case R.id.action_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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
                final Dialog dialog = new Dialog(MainActivity.this);
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

                        Intent intent = getIntent();
                        intent.putExtra(ARG_FIRST_RUN, false);
                        finish();
                        startActivity(intent);
                    }

                });

                builder.show();
                break;
            }

            case R.id.action_about: {
                final WebView webView = new WebView(this);
                webView.loadUrl("file:///android_asset/info.html");
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