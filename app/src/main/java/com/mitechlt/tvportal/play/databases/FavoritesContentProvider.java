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

public class FavoritesContentProvider extends ContentProvider {

    private FavoritesTable database;

    // Used for the Uri Matcher
    private static final int FAVORITES = 10;

    private static final int FAVORITE_ID = 20;

    private static final String AUTHORITY = "com.mitechlt.tvportal.play.favorites.contentprovider";

    private static final String BASE_PATH = "favorites";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, FAVORITES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", FAVORITE_ID);
    }

    @Override
    public boolean onCreate() {
        database = new FavoritesTable(getContext());
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
        queryBuilder.setTables(FavoritesTable.TABLE_FAVORITES);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case FAVORITES:
                break;
            case FAVORITE_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(FavoritesTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
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
            case FAVORITES:
                id = sqlDB.insert(FavoritesTable.TABLE_FAVORITES, null, values);
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
            case FAVORITES:
                rowsDeleted = sqlDB.delete(FavoritesTable.TABLE_FAVORITES, selection,
                        selectionArgs);
                break;
            case FAVORITE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(FavoritesTable.TABLE_FAVORITES,
                            FavoritesTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(FavoritesTable.TABLE_FAVORITES,
                            FavoritesTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs
                    );
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
            case FAVORITES:
                rowsUpdated = sqlDB.update(FavoritesTable.TABLE_FAVORITES,
                        values,
                        selection,
                        selectionArgs);
                break;
            case FAVORITE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(FavoritesTable.TABLE_FAVORITES,
                            values,
                            FavoritesTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(FavoritesTable.TABLE_FAVORITES,
                            values,
                            FavoritesTable.COLUMN_ID + "=" + id
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
                FavoritesTable.COLUMN_TITLE,
                FavoritesTable.COLUMN_LINK,
                FavoritesTable.COLUMN_ID,
                FavoritesTable.COLUMN_TYPE,
                FavoritesTable.COLUMN_IMAGE,
                FavoritesTable.COLUMN_RATING};
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