package com.mitechlt.tvportal.play.databases;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class RecentContentProvider extends ContentProvider {

    private RecentTable database;

    // Used for the Uri Matcher
    private static final int RECENT = 20;

    private static final int RECENT_ID = 30;

    private static final String AUTHORITY = "com.mitechlt.tvportal.play.recent.contentprovider";

    private static final String BASE_PATH = "recent";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, RECENT);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RECENT_ID);
    }

    @Override
    public boolean onCreate() {
        database = new RecentTable(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(RecentTable.TABLE_RECENTS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case RECENT:
                break;
            case RECENT_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(RecentTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        //Cursor cursor = queryBuilder.query(db, projection, selection,
        //      selectionArgs, null, null, null);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, RecentTable.COLUMN_ID + " DESC");
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case RECENT:
                id = sqlDB.insert(RecentTable.TABLE_RECENTS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case RECENT:
                rowsDeleted = sqlDB.delete(RecentTable.TABLE_RECENTS, selection,
                        selectionArgs);
                break;
            case RECENT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(RecentTable.TABLE_RECENTS,
                            FavoritesTable.COLUMN_ID + "=" + id,
                            null);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case RECENT:
                rowsUpdated = sqlDB.update(RecentTable.TABLE_RECENTS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case RECENT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(RecentTable.TABLE_RECENTS,
                            values,
                            RecentTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(RecentTable.TABLE_RECENTS,
                            values,
                            RecentTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                RecentTable.COLUMN_ID,
                RecentTable.COLUMN_TYPE,
                RecentTable.COLUMN_TITLE,
                RecentTable.COLUMN_LINK,
                RecentTable.COLUMN_SEASON,
                RecentTable.COLUMN_EPISODE,
                RecentTable.COLUMN_IMAGE,
                RecentTable.COLUMN_NUM_SEASONS,
                RecentTable.COLUMN_NUM_EPISODES,
                RecentTable.COLUMN_RATING};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}