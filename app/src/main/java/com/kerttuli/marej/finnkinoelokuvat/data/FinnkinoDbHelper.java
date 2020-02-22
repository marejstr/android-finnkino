package com.kerttuli.marej.finnkinoelokuvat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract.ScheduleEntry;
import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract.LocationEntry;

public class FinnkinoDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Finnkino.db";
    private static final int DATABASE_VERSION = 1;

    public FinnkinoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SCHEDULE_TABLE =
                "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " (" +
                        ScheduleEntry._ID + " INTEGER PRIMARY KEY," +
                        ScheduleEntry.FINNKINO_ID + " TEXT," +
                        ScheduleEntry.EVENT_ID + " TEXT," +
                        ScheduleEntry.DATE + " TEXT," +
                        ScheduleEntry.START_TIME + " TEXT," +
                        ScheduleEntry.END_TIME + " TEXT," +
                        ScheduleEntry.TITLE + " TEXT," +
                        ScheduleEntry.ORIGINAL_TITLE + " TEXT," +
                        ScheduleEntry.LENGTH_IN_MINUTES + " TEXT," +
                        ScheduleEntry.RATING + " INTEGER," +
                        ScheduleEntry.GENRES + " TEXT," +
                        ScheduleEntry.THEATRE + " TEXT," +
                        ScheduleEntry.THEATRE_AUDITORIUM + " TEXT," +
                        ScheduleEntry.SHOW_URL + " TEXT," +
                        ScheduleEntry.SMALL_ICON_URL + " TEXT," +
                        ScheduleEntry.LANDSCAPE_IMAGE_URL + " TEXT)";

        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);

        final String SQL_CREATE_LOCATIONS_TABLE =
                "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                        LocationEntry._ID + " INEGER PRIMARY KEY," +
                        LocationEntry.FINNKINO_LOCATION_ID + " TEXT," +
                        LocationEntry.LOCATION_NAME + " TEXT)";

        db.execSQL(SQL_CREATE_LOCATIONS_TABLE);

        final String SQL_CREATE_DATES_TABLE =
                "CREATE TABLE " + FinnkinoDbContract.DateEntry.TABLE_NAME + " (" +
                        FinnkinoDbContract.DateEntry._ID + " INEGER PRIMARY KEY," +
                        FinnkinoDbContract.DateEntry.DATE_TIME + " TEXT)";

        db.execSQL(SQL_CREATE_DATES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // not needed yet
        //TODO implement
    }
}
