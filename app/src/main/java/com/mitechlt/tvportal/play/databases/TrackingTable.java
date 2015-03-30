package com.mitechlt.tvportal.play.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrackingTable extends SQLiteOpenHelper {

    public static final String TABLE_TRACKING = "tracking";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EPISODE = "episode";
    public static final String COLUMN_SEASON = "season";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_WATCHED = "watched";

    private static final String DATABASE_NAME = "tracking.db";
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TRACKING + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_EPISODE + " text not null, "
            + COLUMN_SEASON + " text not null, "
            + COLUMN_LINK + " text not null, "
            + COLUMN_WATCHED + " integer not null);";

    public TrackingTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TrackingTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);
        onCreate(db);
    }

}