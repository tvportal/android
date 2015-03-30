package com.mitechlt.tvportal.play.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.devspark.robototextview.widget.RobotoEditText;
import com.devspark.robototextview.widget.RobotoTextView;
import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.TVPortalApplication;
import com.mitechlt.tvportal.play.activities.LocalPlayerActivity;
import com.mitechlt.tvportal.play.activities.SettingsActivity;
import com.mitechlt.tvportal.play.adapters.ChooserAdapter;
import com.mitechlt.tvportal.play.adapters.SocialAdapter;
import com.mitechlt.tvportal.play.async.AsyncVersionCheck;
import com.mitechlt.tvportal.play.databases.FavoritesContentProvider;
import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.mitechlt.tvportal.play.databases.RecentContentProvider;
import com.mitechlt.tvportal.play.databases.RecentTable;
import com.mitechlt.tvportal.play.databases.TrackingContentProvider;
import com.mitechlt.tvportal.play.databases.TrackingTable;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Some useful utilities for determining various information about the device.
 */
public class AppUtils {

    private static final String TAG = "AppUtils";

    private static final String ARG_PROMO_DIALOG_SHOW = "promo_dialog_show";
    private static final String ARG_STARTUP_DIALOG_SHOWN = "startup_dialog_shown";
    private static final String ARG_SOCIAL_POPUP_SHOW_ON_LAUNCH = "social_popup_show_on_launch";
    private static Context context;

    private AppUtils() {
    }

    public static interface DefaultPlayer {
        public final static int LOCAL = 0;
        public final static int EXTERNAL = 1;
    }

    /**
     * @param context Context
     * @return true if the premium version has been installed
     */


    public static boolean isPremiumInstalled(Context context) {

        PackageManager manager = context.getPackageManager();
        if (manager != null) {

            //1. Check if our premium unlocker is installed
            try {
                manager.getPackageInfo(Config.PREMIUM_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                //Our previous unlocker package was detected and is installed
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {

            }

            //2. Check if our premium unlocker (with a different package name, from the Play Store) is installed
            try {
                manager.getPackageInfo(Config.PREMIUM_PACKAGE_NAME_OLD, PackageManager.GET_ACTIVITIES);
                //Our previous unlocker package was detected and is installed
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {

            }

            //3. Check if our premium unlocker (with another different package name, from the Play Store) is installed
            try {
                manager.getPackageInfo(Config.PREMIUM_PACKAGE_NAME_OTHER, PackageManager.GET_ACTIVITIES);
                //Our previous unlocker package was detected and is installed
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {

            }

            //4. Check if the user upgraded via the server
            SharedPreferences preferences = context.getSharedPreferences(UPGRADE_PREF_FILENAME, Context.MODE_PRIVATE);
            if (preferences.getBoolean(UPGRADE_PREF_KEY, false)) {
                return true;
            }

        }
        return false;
    }

    public static boolean isChromecastPluginInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        if (manager != null) {
            try {
                manager.getPackageInfo(Config.CAST_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {

            }
        }
        return false;

    }

    //Used for the database FAVORITE_TYPE
    public static final String MOVIE = "movie";
    public static final String TVSHOW = "tvshow";

    //Menu options
    public interface OPTIONS {
        public static int FAVORITES = 0;
    }

    // Unique identifier for each fragment for menu options
    public static int TVShowFragmentId = 0;
    public static int MoviesFragmentId = 1;

    @SuppressLint("NewApi")
    public static void setStatusTint(Activity activity) {
        if (hasKitKat()) {
            Window w = activity.getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintColor(Color.parseColor("#33b5e5"));
            tintManager.setStatusBarTintEnabled(true);
        }
    }

    public static void performAsyncVersionCheck(Activity activity) {
        if (NetStatus.getInstance(activity).isOnline(activity)) {
            try {

                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(Config.PACKAGE_NAME, 0);
                String version = packageInfo.versionName;

                AsyncVersionCheck mAsyncVersionCheck = new AsyncVersionCheck(activity, version);
                mAsyncVersionCheck.execute(version);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    /**
     * @return true if API level > 8 (2.2)
     */
    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * @return true if API level > 9 (2.2)
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * @return true if API level > 11 (3.0)
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * @return true if API level > 12 (3.1)
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * @return true if API level > 14 (4.0)
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * @return true if API level > 16 (4.1)
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * @return true if API level > 18 (4.3)
     */
    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * @return true if API level > 19 (4.4)
     */
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isFavorite(Context context, String title) {

        if (title != null) {
            title = title.replace("'", "''");
            final ContentResolver resolver = context.getApplicationContext().getContentResolver();
            final String whereclause = (FavoritesTable.COLUMN_TITLE + "='" + title + "'");
            final String[] cols = new String[]{FavoritesTable.COLUMN_TITLE};
            final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
            final Cursor cursor = resolver.query(favouritesUri, cols, whereclause, null, null);
            boolean isFavourite = !(cursor != null && cursor.getCount() <= 0);
            if (cursor != null) {
                cursor.close();
            }
            return isFavourite;
        }
        return false;
    }

    public static String getFavoriteType(Context context, int position) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();
        final String[] cols = new String[]{FavoritesTable.COLUMN_TITLE, FavoritesTable.COLUMN_TYPE};
        final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(favouritesUri, cols, null, null, sortOrder);
        if (cursor != null && cursor.moveToPosition(position)) {
            String type = cursor.getString(cursor.getColumnIndex(FavoritesTable.COLUMN_TYPE));
            cursor.close();
            return type;
        }
        return null;
    }

    public static String getFavoriteTitle(Context context, int position) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();
        final String[] cols = new String[]{FavoritesTable.COLUMN_TITLE};
        final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(favouritesUri, cols, null, null, sortOrder);
        if (cursor != null && cursor.moveToPosition(position)) {
            String title = cursor.getString(cursor.getColumnIndex(FavoritesTable.COLUMN_TITLE));
            cursor.close();
            return title;
        }

        return null;
    }

    public static String getFavoriteImageUri(Context context, int position) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();
        final String[] cols = new String[]{FavoritesTable.COLUMN_IMAGE};
        final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(favouritesUri, cols, null, null, sortOrder);
        if (cursor != null && cursor.moveToPosition(position)) {
            String imageUri = cursor.getString(cursor.getColumnIndex(FavoritesTable.COLUMN_IMAGE));
            cursor.close();
            return imageUri;
        }

        return null;
    }

    public static int getFavoriteRating(Context context, int position) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();
        final String[] cols = new String[]{FavoritesTable.COLUMN_RATING};
        final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(favouritesUri, cols, null, null, sortOrder);
        if (cursor != null && cursor.moveToPosition(position)) {
            int rating = cursor.getInt(cursor.getColumnIndex(FavoritesTable.COLUMN_RATING));
            cursor.close();
            return rating;
        }

        return -1;
    }

    public static String getFavoriteLink(Context context, int position) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();
        final String[] cols = new String[]{FavoritesTable.COLUMN_LINK};
        final Uri favouritesUri = FavoritesContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(favouritesUri, cols, null, null, sortOrder);
        if (cursor != null && cursor.moveToPosition(position)) {
            String link = cursor.getString(cursor.getColumnIndex(FavoritesTable.COLUMN_LINK));
            cursor.close();
            return link;
        }
        return null;
    }

    /**
     * Saves an episode as 'watched' or 'unwatched' depending on it's current 'watched' state in the database
     *
     * @param context      the context
     * @param episode      the title of this episode
     * @param season       the season this episode belongs to
     * @param link         the link to the episode's mirrors page
     * @param forceWatched true to force the episode to be marked as watched
     */
    public static void toggleWatched(Context context, String episode, String season, String link, boolean forceWatched) {

        int watched = 1;
        if (!forceWatched && isWatched(context, episode, season)) {
            watched = 0;
        }

        ContentValues values = new ContentValues();
        values.put(TrackingTable.COLUMN_EPISODE, episode);
        values.put(TrackingTable.COLUMN_SEASON, season);
        values.put(TrackingTable.COLUMN_LINK, link);
        values.put(TrackingTable.COLUMN_WATCHED, watched);

        final ContentResolver resolver = context.getApplicationContext().getContentResolver();

        if (resolver.update(TrackingContentProvider.CONTENT_URI, values,
                TrackingTable.COLUMN_EPISODE + "='" + episode
                        + "' AND " + TrackingTable.COLUMN_SEASON + "='" + season + "'", null
        ) < 1) {
            resolver.insert(TrackingContentProvider.CONTENT_URI, values);
        }

    }

    public static boolean isWatched(Context context, String episode, String season) {

        if (episode != null) {
            episode = episode.replace("'", "''");
            final ContentResolver resolver = context.getApplicationContext().getContentResolver();
            final String whereClause = (TrackingTable.COLUMN_EPISODE + "='" + episode + "'" + " AND " + TrackingTable.COLUMN_SEASON + "='" + season + "'");
            final String[] cols = new String[]{TrackingTable.COLUMN_EPISODE, TrackingTable.COLUMN_SEASON, TrackingTable.COLUMN_WATCHED};
            final Uri trackingUri = TrackingContentProvider.CONTENT_URI;
            final Cursor cursor = resolver.query(trackingUri, cols, whereClause, null, null);
            boolean isWatched = false;
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getInt(cursor.getColumnIndex(TrackingTable.COLUMN_WATCHED)) == 1) {
                    isWatched = true;
                }
            }
            if (cursor != null) {
                cursor.close();
            }

            return isWatched;
        }
        return false;
    }

    public static boolean isRecent(Context context, String title) {

        if (title != null) {
            title = title.replace("'", "''");
            final ContentResolver resolver = context.getApplicationContext().getContentResolver();
            final String whereClause = (RecentTable.COLUMN_TITLE + "='" + title + "'");
            final String[] cols = new String[]{RecentTable.COLUMN_TITLE};
            final Uri recentUri = RecentContentProvider.CONTENT_URI;
            final Cursor cursor = resolver.query(recentUri, cols, whereClause, null, null);
            boolean isRecent = !(cursor != null && cursor.getCount() <= 0);
            if (cursor != null) {
                cursor.close();
            }
            return isRecent;
        }
        return false;
    }

    public static String getRecentType(Context context, int position) {
        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_TITLE, RecentTable.COLUMN_TYPE};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String type = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_TYPE));
            cursor.close();
            return type;
        }
        return null;
    }

    public static String getRecentTitle(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_TITLE};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String title = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_TITLE));
            cursor.close();
            return title;
        }

        return null;
    }

    public static String getRecentLink(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_LINK};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String link = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_LINK));
            cursor.close();
            return link;
        }
        return null;
    }

    public static String getRecentImage(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_IMAGE};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String link = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_IMAGE));
            cursor.close();
            return link;
        }
        return null;
    }

    public static int getRecentRating(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_RATING};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            int rating = cursor.getInt(cursor.getColumnIndex(RecentTable.COLUMN_RATING));
            cursor.close();
            return rating;
        }
        return -1;
    }

    public static String getRecentSeason(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_SEASON};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String link = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_SEASON));
            cursor.close();
            return link;
        }
        return null;
    }

    public static String getRecentEpisode(Context context, int position) {

        final ContentResolver resolver = context.getContentResolver();
        final String[] cols = new String[]{RecentTable.COLUMN_EPISODE};
        final Uri recentUri = RecentContentProvider.CONTENT_URI;
        final Cursor cursor = resolver.query(recentUri, cols, null, null, null);
        if (cursor != null && cursor.moveToPosition(position)) {
            String link = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_EPISODE));
            cursor.close();
            return link;
        }
        return null;

    }

    public static void makeEasterEggDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.action_upgrade);
        final RobotoEditText editText = new RobotoEditText(context);
        editText.setHint(R.string.easter_egg_hint);
        builder.setView(editText);

        builder.setPositiveButton(R.string.action_upgrade, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().toLowerCase().trim().equals(context.getString(R.string.easter_egg_answer).toLowerCase())) {
                    dialog.dismiss();
                    String url = Config.TV_PORTAL_WEBSITE_UPGRADE_URL;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, R.string.easter_egg_wrong_answer, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public static void showUpgradeDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_share);

        final TextView mCastText = (TextView) dialog.findViewById(R.id.text1);
        mCastText.setText(R.string.pro_purchase);
        Button button = (Button) dialog.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.android.vending");
                    intent.setData(Uri.parse("market://details?id=" + Config.PLAY_STORE_UPGRADE_PACKAGE_NAME));
                    context.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ignored) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SLIDEME_UPGRADE_URL)));
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showCastDialog(final Context context) {

        final Dialog dialog = new Dialog(context);
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
                    context.startActivity(intent);
                } catch (android.content.ActivityNotFoundException anfe) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SLIDEME_UPGRADE_URL)));
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showChooserDialog(final Context context, final MediaInfo mediaInfo) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String[] itemTitles = context.getResources().getStringArray(R.array.playback_choices);
        TypedArray itemIcons = context.getResources().obtainTypedArray(R.array.playback_icons);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.chooser_dialog_title);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_chooser, null);
        final ChooserAdapter adapter = new ChooserAdapter(context, itemTitles, itemIcons);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                listView.setItemChecked(position, true);
                switch (position) {
                    case 0:
                        break;

                    case 1:
                        if (!isPremiumInstalled(context)) {
                            showUpgradeDialog(context);
                        }
                        break;
                }
            }
        });
        builder.setView(view);
        builder.setNegativeButton(R.string.chooser_dialog_always, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Log.i(TAG, "Selected position: " + listView.getSelectedItemPosition());

                //If the user clicked 'always', and they don't have pro, and they're trying to go to third party,
                // toast that premium is required (they've already been shown an upgrade dialog)
                if (!isPremiumInstalled(context) && listView.getCheckedItemPosition() == 1) {
                    Toast.makeText(context, R.string.pro_purchase, Toast.LENGTH_LONG).show();
                    return;
                }

                switch (listView.getCheckedItemPosition()) {
                    case 0:
                        //Save the selected item
                        prefs.edit().putInt(SettingsActivity.ARG_DEFAULT_PLAYER, DefaultPlayer.LOCAL).apply();

                        Intent intent = new Intent(context, LocalPlayerActivity.class);
                        intent.putExtra("media", Utils.fromMediaInfo(mediaInfo));
                        intent.putExtra("shouldStart", true);
                        context.startActivity(intent);
                        break;

                    case 1:
                        //Save the selected item
                        prefs.edit().putInt(SettingsActivity.ARG_DEFAULT_PLAYER, DefaultPlayer.EXTERNAL).apply();

                        startIntentChooserForUrl(context, mediaInfo.getContentId());
                        break;
                }
            }
        });
        builder.setPositiveButton(R.string.chooser_dialog_once, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //If the user clicked 'once', and they don't have pro, and they're trying to go to third party,
                // toast that premium is required (they've already been shown an upgrade dialog)
                if (!isPremiumInstalled(context) && listView.getCheckedItemPosition() == 1) {
                    Toast.makeText(context, R.string.pro_purchase, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    return;
                }

                switch (listView.getCheckedItemPosition()) {
                    case 0:
                        Intent intent = new Intent(context, LocalPlayerActivity.class);
                        intent.putExtra("media", Utils.fromMediaInfo(mediaInfo));
                        intent.putExtra("shouldStart", true);
                        context.startActivity(intent);
                        break;

                    case 1:
                        startIntentChooserForUrl(context, mediaInfo.getContentId());
                        break;
                }

            }
        });
        builder.show();
    }

    public static void startIntentChooserForUrl(final Context context, final String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found to handle video playback intent");
            Toast.makeText(context, R.string.no_video_player, Toast.LENGTH_LONG).show();
        }
    }

    public static void makeLoyaltyUpgradeDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.action_upgrade);
        builder.setMessage(R.string.loyalty_upgrade_dialog_message);
        builder.setPositiveButton(R.string.action_upgrade, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String url = Config.TV_PORTAL_WEBSITE_UPGRADE_URL;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public static void showPromoDialog(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean canShowPromoDialog = prefs.getBoolean(ARG_PROMO_DIALOG_SHOW, true);

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        String month = Integer.toString(today.month);
        String day = Integer.toString(today.monthDay);

        if (Integer.parseInt(month + day) > 426) {
            return;
        }

        if (!canShowPromoDialog) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.promo_dialog_title);
        builder.setMessage(R.string.promo_dialog_message);
        builder.setNegativeButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Config.PROMO_URL));
                context.startActivity(intent);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean(ARG_PROMO_DIALOG_SHOW, false).apply();
            }
        });
        builder.setPositiveButton(R.string.btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean(ARG_PROMO_DIALOG_SHOW, false).apply();
            }
        });
        builder.setNeutralButton(R.string.btn_remind, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    public static void showStartupDialog(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Don't show if we're not at first launch
        if (prefs.getInt(TVPortalApplication.ARG_LAUNCH_COUNT, 0) != 0) {
            return;
        }

        //Don't show if the startup dialog has been seen before
        if (prefs.getBoolean(ARG_STARTUP_DIALOG_SHOWN, false)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.startup_dialog_title);
        builder.setMessage(R.string.startup_dialog_message);
        builder.setPositiveButton(R.string.action_upgrade, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String url = Config.PLAY_STORE_UPGRADE_URL;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
        prefs.edit().putBoolean(ARG_STARTUP_DIALOG_SHOWN, true).apply();
    }


    public static void showSocialDialog(final Context context, boolean forceShow) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean canShowSocialDialog = prefs.getBoolean(ARG_SOCIAL_POPUP_SHOW_ON_LAUNCH, true);

        //Don't show if we're not at or beyond 5th launch
        if (prefs.getInt(TVPortalApplication.ARG_LAUNCH_COUNT, 0) < 5 && !forceShow) {
            return;
        }

        //Don't show if we've specifically flagged not to
        if (!canShowSocialDialog && !forceShow) {
            return;
        }

        String[] itemTitles = context.getResources().getStringArray(R.array.social_titles);
        TypedArray itemIcons = context.getResources().obtainTypedArray(R.array.social_icons);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.social_dialog_title);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_social, null);
        SocialAdapter adapter = new SocialAdapter(context, itemTitles, itemIcons);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        final RobotoCheckBox checkBox = (RobotoCheckBox) view.findViewById(R.id.checkBox1);
        checkBox.setChecked(prefs.getBoolean(ARG_SOCIAL_POPUP_SHOW_ON_LAUNCH, true));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                listView.setItemChecked(position, false);

                String url = "";
                switch (position) {
                    case 0:
                        //Facebook
                        url = "https://www.facebook.com/TVPortalApp";
                        break;

                    case 1:
                        //G+
                        url = "https://plus.google.com/u/0/communities/116034521997245237036";
                        break;

                    case 2:
                        //Twitter
                        url = "https://twitter.com/tvportalapp";
                        break;

                    case 3:
                        //Reddit
                        url = "http://reddit.com/r/tvportalapp";
                        break;

                    case 4:
                        //Instagram
                        url = "http://instagram.com/tvportalapp";
                        break;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "No activity found to handle the social event click");
                    Toast.makeText(context, R.string.no_social_intent_receiver, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setView(view);
        builder.setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit().putBoolean(ARG_SOCIAL_POPUP_SHOW_ON_LAUNCH, checkBox.isChecked()).apply();
            }
        });
        builder.show();
    }

    public static boolean canShowAds(Context context) {

        if (isPremiumInstalled(context)) {
            return false;
        }

        if (!ChromeCastInstalledMoreThanAWeek(context)) {
            return false;
        }

        return true;
    }

    public static boolean isInstalledMoreThanAWeek(Context context) {

        PackageManager packageManager = context.getPackageManager();
        try {
            long installDateMillis = packageManager.getPackageInfo(Config.PACKAGE_NAME, 0).firstInstallTime;
            long currentTimeMillis = System.currentTimeMillis();

            Log.d("AppUtils", "App installed for : " + getDurationBreakdown(currentTimeMillis - installDateMillis));

            if ((currentTimeMillis - installDateMillis) > (604800000L * 4)) {
                return true;
            }

        } catch (Exception ignored) {
            //We had some kind of error. Give the user the benefit of the doubt and allow the video to be watched
            return true;
        }
        return false;
    }

    public static boolean ChromeCastInstalledMoreThanAWeek(Context context) {

        PackageManager packageManager = context.getPackageManager();
        try {
            long installDateMillis = packageManager.getPackageInfo(Config.CAST_PACKAGE_NAME, 0).firstInstallTime;
            long currentTimeMillis = System.currentTimeMillis();

            Log.d("AppUtils", "App installed for : " + getDurationBreakdown(currentTimeMillis - installDateMillis));

            if ((currentTimeMillis - installDateMillis) > 604800000L) {
                return true;
            }

        } catch (Exception ignored) {
            //We had some kind of error. Give the user the benefit of the doubt and allow the video to be watched
            return true;
        }
        return false;
    }

    public static boolean canWatchVideo(final Context context, String season, String episode) {

        Log.d(TAG, "Checking if user can watch the video...");

        if (isPremiumInstalled(context)) {
            Log.d(TAG, "Yes, premium installed.");
            return true;
        } else {
            Log.d(TAG, "Not sure yet, premium not installed.");
        }

        if (!isInstalledMoreThanAWeek(context)) {
            Log.d(TAG, "Yes, app has been installed less than a week");
            return true;
        } else {
            Log.d(TAG, "Not sure yet, app has been installed more than a week");
        }
        if (!ChromeCastInstalledMoreThanAWeek(context)) {
            Log.d(TAG, "Yes, app has been installed less than a week");
            return true;
        } else {
            Log.d(TAG, "Not sure yet, app has been installed more than a week");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String lastWatchedEpisode1 = preferences.getString(Config.LAST_WATCHED_EPISODE_1, null);
        String lastWatchedEpisode2 = preferences.getString(Config.LAST_WATCHED_EPISODE_2, null);
        String lastWatchedSeason1 = preferences.getString(Config.LAST_WATCHED_SEASON_1, null);
        String lastWatchedSeason2 = preferences.getString(Config.LAST_WATCHED_SEASON_2, null);

        //If there is no 'last watched' episode (or only one), then we can go ahead and watch a video
        if (lastWatchedEpisode1 == null || lastWatchedEpisode2 == null) {
            Log.d(TAG, "Yes, the user has only watched one episode (or none)");
            if (lastWatchedEpisode1 == null || episode.equals(lastWatchedEpisode1)) {
                Log.d(TAG, "First save slot is null. Saving there.");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Config.LAST_WATCH_TIME_1, System.currentTimeMillis());
                editor.putString(Config.LAST_WATCHED_EPISODE_1, episode);
                editor.putString(Config.LAST_WATCHED_SEASON_1, season);
                editor.apply();

            } else if (lastWatchedEpisode2 == null) {
                Log.d(TAG, "Second save slot is null. Saving there.");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Config.LAST_WATCH_TIME_2, System.currentTimeMillis());
                editor.putString(Config.LAST_WATCHED_EPISODE_2, episode);
                editor.putString(Config.LAST_WATCHED_SEASON_2, season);
                editor.apply();
            }
            return true;
        } else {
            Log.d(TAG, "Not sure yet, user has watched at least one episode");
        }

        if (lastWatchedSeason1 != null && lastWatchedSeason2 != null) {
            if ((season.equals(lastWatchedSeason1) || season.equals(lastWatchedSeason2))
                    && (episode.equals(lastWatchedEpisode1) || episode.equals(lastWatchedEpisode2))) {
                // We're just trying to watch the same episode as before.. that's ok
                Log.d(TAG, "Yes, user is watching the same episode as before. Previous episode: " + lastWatchedEpisode1 + " or " + lastWatchedEpisode2 + ". Current episode: " + episode);
                return true;
            } else {
                Log.d(TAG, "Not sure yet, episode is different to the previous one. Previous episode: " + lastWatchedEpisode1 + " or " + lastWatchedEpisode2 + ". Current episode: " + episode);
            }

            if (!(episode.equals(lastWatchedEpisode1) && season.equals(lastWatchedSeason1)) && !(episode.equals(lastWatchedEpisode2) && season.equals(lastWatchedSeason2))) {
                // We're not matching the previously watched seasons or episodes.
                // If both of the last watched episodes were within the last 5 hours,
                // No watchy-watchy

                Log.d(TAG, "Not sure yet, but the user has watched at least 2 episodes of something in the past...");

                long lastWatchTime1 = preferences.getLong(Config.LAST_WATCH_TIME_1, 0);
                long lastWatchTime2 = preferences.getLong(Config.LAST_WATCH_TIME_2, 0);

                long currentTimeMillis = System.currentTimeMillis();

                if (((currentTimeMillis - lastWatchTime1) < 18000000L) && ((currentTimeMillis - lastWatchTime2) < 18000000L)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.not_five_hours_title);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.timer, null);
                    builder.setView(layout);
                    RobotoTextView textView = (RobotoTextView) layout.findViewById(R.id.time_remaining);
                    long timeRemaining = 18000000L - (Math.min(currentTimeMillis - lastWatchTime1, currentTimeMillis - lastWatchTime2));
                    MyCount myCount = new MyCount(context, timeRemaining, textView);
                    myCount.start();
                    builder.setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setPositiveButton(R.string.action_upgrade, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String url = Config.PLAY_STORE_UPGRADE_URL;
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            context.startActivity(intent);
                        }
                    });
                    builder.show();
                    Log.d(TAG, "No. The user has watched 2 separate episodes in the last 5 hours. Episode 1: " + lastWatchedEpisode1 + " Watched: " + getDurationBreakdown(currentTimeMillis - lastWatchTime1) + ". Episode 2: " + lastWatchedEpisode2 + "Watched: " + getDurationBreakdown(currentTimeMillis - lastWatchTime2));
                    return false;
                }

                Log.v(TAG, "Yes, but now we will save that episode in the first slot which falls outside of the 5 hour bracket");

                // We're allowed to watch at least one more episode.
                // Save the episode in the first slot which falls outside of the 5 hour bracket
                if ((currentTimeMillis - lastWatchTime1) > 18000000L) {
                    Log.d(TAG, "First save slot is: " + getDurationBreakdown(currentTimeMillis - lastWatchTime1) + " old. Saving there.");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong(Config.LAST_WATCH_TIME_1, currentTimeMillis);
                    editor.putString(Config.LAST_WATCHED_EPISODE_1, episode);
                    editor.putString(Config.LAST_WATCHED_SEASON_1, season);
                    editor.apply();

                } else if ((currentTimeMillis - lastWatchTime2) > 18000000L) {
                    Log.d(TAG, "Second save slot is: " + getDurationBreakdown(currentTimeMillis - lastWatchTime2) + " old. Saving there.");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong(Config.LAST_WATCH_TIME_2, currentTimeMillis);
                    editor.putString(Config.LAST_WATCHED_EPISODE_2, episode);
                    editor.putString(Config.LAST_WATCHED_SEASON_2, season);
                    editor.apply();
                }
            }
        }

        return true;
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (String.valueOf(days) + " Days " + hours + " Hours " + minutes + " Minutes " + seconds + " Seconds");
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdownNoDays(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours + " Hours " + minutes + " Minutes " + seconds + " Seconds");
    }

    // CountDownTimer is an abstract class, so extend it and fill in methods
    public static class MyCount extends CountDownTimer {

        TextView textView;
        Context context;

        public MyCount(Context context, long millisInFuture, TextView textView) {
            super(millisInFuture, 1000);
            this.textView = textView;
            this.context = context;
        }

        @Override
        public void onFinish() {
            //This won't happen
        }

        @Override
        public void onTick(long millisUntilFinished) {
            textView.setText(String.format(context.getString(R.string.time_remaining, getDurationBreakdownNoDays(millisUntilFinished))));
        }
    }

    /**
     * SharedPreferences filename for caching the upgrade status
     */
    private static final String UPGRADE_PREF_FILENAME = "as8S4fA2R3sRat";
    private static final String UPGRADE_PREF_KEY = "ws88ml2";

    /**
     * Determines if this user is upgraded by checking the server. If the user is upgraded, it is
     * cached in a SharedPreferences instances so that future calls do not need to connect to the
     * server. If the user is not upgraded, this method will always connect to the server.
     * <p/>
     * This function blocks until the server responds, so it should not be called on the UI thread.
     *
     * @param context the context used to get a SharedPreferences instance as well as to get an
     *                AccountManager, must no be null
     * @return true if the current user is upgraded
     */
    public static boolean isServerUpgraded(Context context) {
        SharedPreferences pref = context.getSharedPreferences(UPGRADE_PREF_FILENAME, Context.MODE_PRIVATE);
        if (pref.getBoolean(UPGRADE_PREF_KEY, false)) {
            return true;
        }
        if (checkServerUpgrade(context)) {
            pref.edit().putBoolean(UPGRADE_PREF_KEY, true).commit();
            return true;
        }
        return false;
    }

    static boolean checkServerUpgrade(Context context) {
        //get Emails and post them
        List<String> emails = getEmailAddresses(context, 32);
        if (emails == null || emails.isEmpty()) {
            return false;
        }
        List<Argument> args = new ArrayList<Argument>();
        int time = (int) (System.currentTimeMillis() / 1000);
        for (String email : emails) {
            args.add(new Argument("e[]", email));
        }
        args.add(new Argument("t", Integer.toString(time)));
        args.add(new Argument("c", new BigInteger(32, new Random(System.currentTimeMillis())).toString(32)));

        String url = "http://tvportalapp.com/r/android/service/check_v1_and.php" + "?portal=" + System.currentTimeMillis();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(20000);
            con.setReadTimeout(20000);
            con.setInstanceFollowRedirects(false);
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            OutputStream out = null;
            try {
                byte[] output = buildPostBody(args);
                if (output == null) {
                    output = new byte[0];
                }
                con.setFixedLengthStreamingMode(output.length);
                out = con.getOutputStream();
                out.write(output);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            int code = con.getResponseCode();
            if (code != 200) {
                return false;
            }
            InputStream in = null;
            String response = null;
            try {
                in = con.getInputStream();
                response = toString(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            return response != null && response.contains("granted");
        } catch (Throwable t) {
            return false;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    static List<String> getEmailAddresses(Context context, int limit) {
        ArrayList<String> emails = new ArrayList<String>();
        AccountManager am = AccountManager.get(context);
        if (am == null) {
            return emails;
        }
        Account[] accounts = am.getAccountsByType("com.google");
        if (accounts == null || accounts.length == 0) {
            return emails;
        }
        for (Account a : accounts) {
            if (a.name == null || a.name.length() == 0) {
                continue;
            }
            emails.add(a.name.trim().toLowerCase());
        }
        while (emails.size() > limit) {
            emails.remove(emails.size() - 1);
        }
        return emails;
    }

    static class Argument {
        public final String name;
        public final String value;
        public final boolean encode;

        public Argument(String name, String value) {
            this(name, value, true);
        }

        public Argument(String name, String value, boolean encode) {
            this.name = name;
            this.value = value;
            this.encode = encode;
        }
    }

    static byte[] buildPostBody(List<Argument> arguments) {
        String body = buildArgumentString(arguments);
        try {
            return body.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return body.getBytes();
        }
    }

    static String buildArgumentString(List<Argument> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(arguments.size() * 32);
        boolean delimit = false;
        for (Argument arg : arguments) {
            if (delimit) {
                sb.append('&');
            } else {
                delimit = true;
            }
            sb.append(arg.name).append('=');
            if (arg.encode) {
                try {
                    sb.append(URLEncoder.encode(arg.value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    sb.append(arg.value); //default to no encoding
                }
            } else {
                sb.append(arg.value);
            }
        }
        return sb.toString();
    }

    public static String toString(InputStream is) {
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[512];
        if (is != null) {
            try {
                Reader in = new InputStreamReader(is, "UTF-8");
                try {
                    int n;
                    while ((n = in.read(buffer, 0, buffer.length)) >= 0) {
                        out.append(buffer, 0, n);
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                out.append("error");
            }
        }
        return out.toString();
    }

    @SuppressWarnings("deprecation")
    /**
     * Returns the screen/display size
     *
     * @param ctx
     * @return
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    /**
     * Shows an error dialog with a given text message.
     *
     * @param context
     * @param errorString
     */
    public static final void showErrorDialog(Context context, String errorString) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an error dialog with a text provided by a resource ID
     *
     * @param context
     * @param resourceId
     */
    public static final void showErrorDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an "Oops" error dialog with a text provided by a resource ID
     *
     * @param context
     * @param resourceId
     */
    public static final void showOopsDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.oops)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.ic_action_alerts_and_states_warning)
                .create()
                .show();
    }


    public static Point getSize(Display display) {
        if (Build.VERSION.SDK_INT >= 17) {
            Point outPoint = new Point();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            outPoint.x = metrics.widthPixels;
            outPoint.y = metrics.heightPixels;
            return outPoint;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            Point outPoint = getRealSize(display);
            if (outPoint != null)
                return outPoint;
        }
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(outPoint);
        } else {
            outPoint.x = display.getWidth();
            outPoint.y = display.getHeight();
        }
        return outPoint;
    }

    public static Point getRealSize(Display display) {
        Point outPoint = new Point();
        Method mGetRawH;
        try {
            mGetRawH = Display.class.getMethod("getRawHeight");
            Method mGetRawW = Display.class.getMethod("getRawWidth");
            outPoint.x = (Integer) mGetRawW.invoke(display);
            outPoint.y = (Integer) mGetRawH.invoke(display);
            return outPoint;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * A utility method to handle a few types of exceptions that are commonly thrown by the cast
     * APIs in this library. It has special treatments for
     * {@link TransientNetworkDisconnectionException}, {@link NoConnectionException} and shows an
     * "Oops" dialog conveying certain messages to the user. The following resource IDs can be used
     * to control the messages that are shown:
     * <p/>
     * <ul>
     * <li><code>R.string.connection_lost_retry</code></li>
     * <li><code>R.string.connection_lost</code></li>
     * <li><code>R.string.failed_to_perform_action</code></li>
     * </ul>
     *
     * @param context
     * @param e
     */
    public static void handleException(Context context, Exception e) {
        int resourceId = 0;
        if (e instanceof TransientNetworkDisconnectionException) {
            // temporary loss of connectivity
            resourceId = R.string.connection_lost_retry;

        } else if (e instanceof NoConnectionException) {
            // connection gone
            resourceId = R.string.connection_lost;
        } else if (e instanceof RuntimeException ||
                e instanceof IOException ||
                e instanceof CastException) {
            // something more serious happened
            resourceId = R.string.failed_to_perform_action;
        } else {
            // well, who knows!
            resourceId = R.string.failed_to_perform_action;
        }
        if (resourceId > 0) {
            AppUtils.showOopsDialog(context, resourceId);
        }
    }

    /**
     * Gets the version of app.
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        String versionString = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0 /* basic info */);
            versionString = info.versionName;
        } catch (Exception e) {
            // do nothing
        }
        return versionString;
    }

    /**
     * Shows a (long) toast
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     *
     * @param context
     * @param resourceId
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }
}