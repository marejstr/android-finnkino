package com.kerttuli.marej.finnkinoelokuvat.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FinnkinoDbContract {

    // empty constructror to prevent instantiating of the contract class
    private FinnkinoDbContract() {}

    // name of the content provider
    public static final String CONTENT_AUTHORITY = "com.kerttuli.marej.finnkinoelokuvat";
    //used for the uri matcher
    public static final String PATH_SEDULE = "schedule";
    public static final String PATH_LOCATIONS = "location";
    public static final String PATH_DATE = "date";
    public static final String PATH_EVENT = "event";

    // base of all uris used to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class ScheduleEntry implements BaseColumns {

        public static final Uri FULL_SCHEDULE_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SEDULE)
                .build();

        public static final String TABLE_NAME = "schedule";

        //Used to identify wich show the user clicked on and start a detail activity
        public static final String FINNKINO_ID = "finnkino_id";
        public static final String EVENT_ID = "event_id";
        public static final String DATE = "date";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String TITLE = "title";
        public static final String ORIGINAL_TITLE = "origianl_title";
        public static final String LENGTH_IN_MINUTES = "length_in_minutes";
        public static final String RATING = "rating";
        public static final String GENRES = "genres";
        public static final String THEATRE = "theatre";
        public static final String THEATRE_AUDITORIUM = "theatre_auditorium";
        public static final String SHOW_URL = "show_url";
        public static final String SMALL_ICON_URL = "small_icon_url";
        public static final String LANDSCAPE_IMAGE_URL = "landscape_image_url";
    }

    public static final class LocationEntry implements BaseColumns {
        public static final Uri LOCATIONS_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_LOCATIONS)
                .build();

        public static final String TABLE_NAME = "locations";

        public static final String FINNKINO_LOCATION_ID = "finnkino_location_id";
        public static final String LOCATION_NAME = "location_name";
    }

    public static final class DateEntry implements BaseColumns {
        public static final Uri DATE_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_DATE)
                .build();

        public static final String TABLE_NAME = "dates";

        public static final String DATE_TIME = "date_time";
    }
}
