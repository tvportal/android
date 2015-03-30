package com.mitechlt.tvportal.play.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecentTable extends SQLiteOpenHelper {

    public static final String TABLE_RECENTS = "recents";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_SEASON = "season";
    public static final String COLUMN_EPISODE = "episode";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_NUM_SEASONS = "num_seasons";
    public static final String COLUMN_NUM_EPISODES = "num_episodes";
    public static final String COLUMN_RATING = "rating";

    private static final String DATABASE_NAME = "recents.db";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_RECENTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_LINK + " text not null, "
            + COLUMN_TYPE + " text not null, "
            + COLUMN_NUM_EPISODES + " text not null, "
            + COLUMN_NUM_SEASONS + " text not null, "
            + COLUMN_EPISODE + " text, "
            + COLUMN_SEASON + " text, "
            + COLUMN_IMAGE + " text not null, "
            + COLUMN_RATING + " integer);";

    public RecentTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(RecentTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTS);
        onCreate(db);
    }

}