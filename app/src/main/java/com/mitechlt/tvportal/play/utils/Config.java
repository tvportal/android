package com.mitechlt.tvportal.play.utils;

import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.mitechlt.tvportal.play.databases.RecentTable;

public class Config {

    public final static String PACKAGE_NAME = "com.mitechlt.tvportal.play";

    public final static String CAST_PACKAGE_NAME = "com.mitechlt.tvportal.play.chromecast";

    public final static String PREMIUM_PACKAGE_NAME = "com.mitechlt.tvportal.play.premium2";

    public final static String PREMIUM_PACKAGE_NAME_OTHER = "com.mitechlt.tvportal.play.premium3";

    public final static String PREMIUM_PACKAGE_NAME_OLD = "com.mitechlt.tvportal.play.premium";

    public final static String GOOGLE_PLUS_URL = "https://plus.google.com/u/4/communities/116034521997245237036";


    public final static String PROMO_URL = "http://www.rafflecopter.com/rafl/display/ae28f82/";

    public final static String TV_PORTAL_WEBSITE_UPGRADE_URL = "http://tvportalapp.com/download/TVPortal_1.1.x_Premium.apk";

    public final static String SLIDEME_UPGRADE_URL = "http://slideme.org/application/tv-portal-premium-unlocker?id=com.mitechlt.tvportal.play.premium2";

    public final static String PLAY_STORE_UPGRADE_URL = "https://play.google.com/store/apps/details?id=com.mitechlt.tvportal.play.premium3";

    public final static String PLAY_STORE_UPGRADE_PACKAGE_NAME = "com.mitechlt.tvportal.play.premium3";

    public final static String PLAY_STORE_CAST_UPGRADE_PACKAGE_NAME = "com.mitechlt.tvportal.play.chromecast";


    public final static String LAST_WATCH_TIME_1 = "last_watch_time_1";

    public final static String LAST_WATCH_TIME_2 = "last_watch_time_2";

    public final static String LAST_WATCHED_SEASON_1 = "last_watched_season_1";

    public final static String LAST_WATCHED_EPISODE_1 = "last_watched_episode_1";

    public final static String LAST_WATCHED_SEASON_2 = "last_watched_season_2";

    public final static String LAST_WATCHED_EPISODE_2 = "last_watched_episode_2";

    //google analytics id
    public final static String GA_PROPERTY_ID = "UA-51779131-1";

    public final static String MOBFOX_AD_ID = "faac43911a78fc8c4abee5d3240b79ef";

    public final static String BANNER_AD_UNIT_ID = "ca-app-pub-3835186866229262/6645856032";

    public final static String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3835186866229262/2891164033";

    public final static String TEST_DEVICE_ID_1 = "A67F4255A67458D42E12695CDA84EB78";

    public final static String TEST_DEVICE_ID_2 = "34F64524C97707B4";

    public final static String REVMOB_AD_ID = "53347dee00b628f37bc0b657";


    public final static String SORT_ORDER_FAVORITE = "sort_order_favorite";
    public final static String SORT_FAVORITE_DEFAULT = FavoritesTable.COLUMN_ID;
    public final static String SORT_FAVORITE_MOVIES_AZ = FavoritesTable.COLUMN_TYPE + ", " + FavoritesTable.COLUMN_TITLE + " COLLATE NOCASE ASC";
    public final static String SORT_FAVORITE_TV_SHOWS_AZ = FavoritesTable.COLUMN_TYPE + " DESC" + ", " + FavoritesTable.COLUMN_TITLE + " COLLATE NOCASE ASC";

    public final static String SORT_ORDER_RECENT = "sort_order_recent";
    public final static String SORT_RECENT_DEFAULT = RecentTable.COLUMN_ID;

    public final static String ARG_USE_PROXY = "use_proxy";

    public final static String CHROMECAST_APPLICATION_ID = "956C9FE3";

}
