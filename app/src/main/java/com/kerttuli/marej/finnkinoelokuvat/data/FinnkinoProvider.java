package com.kerttuli.marej.finnkinoelokuvat.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class FinnkinoProvider extends ContentProvider {

    private FinnkinoDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    public static final int CODE_SCHEDULE = 100;
    public static final int CODE_DETAIL = 101;
    public static final int CODE_ALL_LOCATIONS = 102;
    public static final int CODE_DATES = 103;

    @Override
    public boolean onCreate() {
        //happens on main thread.
        mOpenHelper = new FinnkinoDbHelper(getContext());
        return false;
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String autorithy = FinnkinoDbContract.CONTENT_AUTHORITY;

        matcher.addURI(autorithy, FinnkinoDbContract.PATH_SEDULE, CODE_SCHEDULE);
        matcher.addURI(autorithy, FinnkinoDbContract.PATH_SEDULE + "/#", CODE_DETAIL);
        matcher.addURI(autorithy, FinnkinoDbContract.PATH_LOCATIONS, CODE_ALL_LOCATIONS);
        matcher.addURI(autorithy, FinnkinoDbContract.PATH_DATE, CODE_DATES);

        return matcher;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_SCHEDULE:
                db.beginTransaction();
                int scheduleRowsInserted = 0;

                try {
                    for (ContentValues value : values) {
                        long newRowId = db.insert(FinnkinoDbContract.ScheduleEntry.TABLE_NAME, null, value);
                        if (newRowId != -1) {
                            scheduleRowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                if (scheduleRowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return scheduleRowsInserted;

            case CODE_ALL_LOCATIONS:
                db.beginTransaction();
                int locationRowsInserted = 0;

                try {
                    for (ContentValues value : values) {
                        long newRowId = db.insert(FinnkinoDbContract.LocationEntry.TABLE_NAME, null, value);
                        if (newRowId != -1) {
                            locationRowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                if (locationRowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return locationRowsInserted;

            case CODE_DATES:
                db.beginTransaction();
                int dateRowsInserted = 0;

                try {
                    for (ContentValues value : values) {
                        long newRowId = db.insert(FinnkinoDbContract.DateEntry.TABLE_NAME, null, value);
                        if (newRowId != -1) {
                            dateRowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                if (dateRowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return dateRowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;
        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (selection == null) {
            selection = "1";
        }

        switch (sUriMatcher.match(uri)) {
            case CODE_SCHEDULE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FinnkinoDbContract.ScheduleEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;

            case CODE_ALL_LOCATIONS:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FinnkinoDbContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_DATES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FinnkinoDbContract.DateEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("malformed uri" + uri);

        }
        return numRowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_SCHEDULE:
                cursor = mOpenHelper.getReadableDatabase().query(
                        FinnkinoDbContract.ScheduleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_DETAIL:
                String finnID = uri.getLastPathSegment();
                String[] selectionArguments = {finnID};
                cursor = mOpenHelper.getReadableDatabase().query(
                        FinnkinoDbContract.ScheduleEntry.TABLE_NAME,
                        projection,
                        FinnkinoDbContract.ScheduleEntry.FINNKINO_ID + " = ?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_ALL_LOCATIONS:
                cursor = mOpenHelper.getReadableDatabase().query(
                        FinnkinoDbContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_DATES:
                cursor = mOpenHelper.getReadableDatabase().query(
                        FinnkinoDbContract.DateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("malformed uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
