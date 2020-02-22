package com.kerttuli.marej.finnkinoelokuvat;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;
import com.kerttuli.marej.finnkinoelokuvat.intentService.MainIntentService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ScheduleClickHandler {

    public static final String EXTRA_ICON_TRANSITION_NAME = "icon_transition_name";
    public static final String EXTRA_BACKGROUND_TRANSITION_NAME = "background_transition_name";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_ORIGINAL_TITLE = "original_title";

    public static final String LAST_SEARCHED_LOCATION = "last_searched_location";
    public static final String LAST_SEARCHED_TIME = "last_searched_time";
    public static final String LAST_SEARCHED_RATING = "last_searched_rating";
    public static final String LAST_SEARCHED_DATE = "last_searched_date";
    public static final String DATE_OF_LAST_REFRESH = "date_of_last_refresh";
    public static final String HOUR_OF_LAST_CURSOR_RESTART = "hour_of_last_restart";
    public static final String SCHEDULE_SELECTION = "schedule_selection";

    protected static final int SCHEDULE_LOADER_ID = 1;
    protected static final int LOCATIONS_LOADER_ID = 2;
    protected static final int DATES_LOADER_ID = 3;

    private String mDateofLastRefresh;
    private int mHourofLastCursorRestart;
    private String mScheduleSelection = null;
    private String[] mScheduleSelectionArgs = null;
    private RecyclerView mRecyclerView;
    private ScheduleAdapter mScheduleAdapter;
    private DividerItemDecoration mDividerItemDecoration;
    private SmoothScrollLinearLayoutManager mSmoothScrollLinearLayoutManager;
    private ProgressBar mProgressBar;
    private TextView mNoDataTextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // textview is shown only if there is no schedule data to be shown
        mNoDataTextview = (TextView) findViewById(R.id.no_data_textview);

        // Setting up recyclerview that shows schedule
        mRecyclerView = (RecyclerView) findViewById(R.id.schedule_recycler_view);
        mSmoothScrollLinearLayoutManager = new SmoothScrollLinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        );
        mRecyclerView.setLayoutManager(mSmoothScrollLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mScheduleAdapter = new ScheduleAdapter(this, this);
        mRecyclerView.setAdapter(mScheduleAdapter);

        mDividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), mSmoothScrollLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        //mRecyclerView.setItemAnimator(new FinnkinoItemAnimator());

        //setup progressbar and show it
        mProgressBar = (ProgressBar) findViewById(R.id.main_loading_indicator);
        showLoading();

        // Load saved values for spinners and latest content updates
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mScheduleAdapter.setLastSearchedLocation(settings.getString(
                LAST_SEARCHED_LOCATION, "Valitse alue/teatteri"));
        mScheduleAdapter.setLastSearchedDate(settings.getString(
                LAST_SEARCHED_DATE, "Error"));
        mScheduleAdapter.setLastSearchedAgeRating(settings.getString(
                LAST_SEARCHED_RATING, "Kaikki"));
        mScheduleAdapter.setLastSearchedStartTime(settings.getString(
                LAST_SEARCHED_TIME, "10:00"));


        Calendar calendarTest = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatTest = new SimpleDateFormat("dd.MM.yyyy");
        String backupDate = simpleDateFormatTest.format(calendarTest.getTime());

        mDateofLastRefresh = settings.getString(DATE_OF_LAST_REFRESH, "0-1.00.0000");
        mScheduleSelection = settings.getString(SCHEDULE_SELECTION, "start_time >= ?");
        mHourofLastCursorRestart = settings.getInt(HOUR_OF_LAST_CURSOR_RESTART, -1);

        // Setting up fab
        /*
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int firstVisibleItem = mSmoothScrollLinearLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    mSmoothScrollLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 1);
                } else {
                    mSmoothScrollLinearLayoutManager.smoothScrollToPosition(mRecyclerView,null, 0);
                }
            }
        });
        */

        // Get current date and check if database needs to update
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String todaysDate = simpleDateFormat.format(calendar.getTime());

        SimpleDateFormat hourDateFormat = new SimpleDateFormat("kk");
        int currentHour = Integer.parseInt(hourDateFormat.format(calendar.getTime()));
        String time = String.valueOf(currentHour) + ":00";
        if (!todaysDate.equals(mDateofLastRefresh)) {
            // Refresh schedule so that old data is not shown on startup
            refreshWithDefaultValues();
        } else {
            time = settings.getString(LAST_SEARCHED_TIME, "11:00");
        }
        mScheduleSelectionArgs = new String[]{time};

        // Load data asynchronously from database, Check onCreateLoader()
        getLoaderManager().initLoader(SCHEDULE_LOADER_ID, null, this);
        getLoaderManager().initLoader(LOCATIONS_LOADER_ID, null, this);
        getLoaderManager().initLoader(DATES_LOADER_ID, null, this);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if recyclerview is showing movies that started before this hour
        // Refresh cursor if old info is showing
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hourDateFormat = new SimpleDateFormat("kk");
        int currentHour = Integer.parseInt(hourDateFormat.format(calendar.getTime()));

        if (currentHour > mHourofLastCursorRestart) {
            mHourofLastCursorRestart = currentHour;
            //mScheduleSelection = FinnkinoDbContract.ScheduleEntry.START_TIME + " >= ?";
            String time = String.valueOf(currentHour) + ":00";
            mScheduleSelectionArgs = new String[]{time};
            getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, null, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // save values for spinners and latest content updates
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(LAST_SEARCHED_DATE, mScheduleAdapter.getLastSearchedDate());
        editor.putString(LAST_SEARCHED_LOCATION, mScheduleAdapter.getLastSearchedLocation());
        editor.putString(LAST_SEARCHED_TIME, mScheduleAdapter.getLastSearchedStartTime());
        editor.putString(LAST_SEARCHED_RATING, mScheduleAdapter.getLastSearchedAgeRating());

        editor.putString(DATE_OF_LAST_REFRESH, mDateofLastRefresh);
        editor.putString(SCHEDULE_SELECTION, mScheduleSelection);
        editor.putInt(HOUR_OF_LAST_CURSOR_RESTART, mHourofLastCursorRestart);

        editor.commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case SCHEDULE_LOADER_ID:
                Uri scheduleQueryUri = FinnkinoDbContract.ScheduleEntry.FULL_SCHEDULE_URI;

                String[] scheduleProjection = {
                        FinnkinoDbContract.ScheduleEntry._ID,
                        FinnkinoDbContract.ScheduleEntry.TITLE,
                        FinnkinoDbContract.ScheduleEntry.ORIGINAL_TITLE,
                        FinnkinoDbContract.ScheduleEntry.DATE,
                        FinnkinoDbContract.ScheduleEntry.START_TIME,
                        FinnkinoDbContract.ScheduleEntry.END_TIME,
                        FinnkinoDbContract.ScheduleEntry.RATING,
                        FinnkinoDbContract.ScheduleEntry.GENRES,
                        FinnkinoDbContract.ScheduleEntry.THEATRE_AUDITORIUM,
                        FinnkinoDbContract.ScheduleEntry.SMALL_ICON_URL,
                        FinnkinoDbContract.ScheduleEntry.FINNKINO_ID,
                        FinnkinoDbContract.ScheduleEntry.EVENT_ID,
                        FinnkinoDbContract.ScheduleEntry.LANDSCAPE_IMAGE_URL
                };

                return new CursorLoader(
                        this,
                        scheduleQueryUri,
                        scheduleProjection,
                        mScheduleSelection,
                        mScheduleSelectionArgs,
                        null);

            case LOCATIONS_LOADER_ID:

                Uri locationsQueryUri = FinnkinoDbContract.LocationEntry.LOCATIONS_URI;
                String[] locationsProjection = {
                        FinnkinoDbContract.LocationEntry._ID,
                        FinnkinoDbContract.LocationEntry.FINNKINO_LOCATION_ID,
                        FinnkinoDbContract.LocationEntry.LOCATION_NAME
                };

                return new CursorLoader(
                        this,
                        locationsQueryUri,
                        locationsProjection,
                        null,
                        null,
                        null
                );

            case DATES_LOADER_ID:

                Uri datesQueryUri = FinnkinoDbContract.DateEntry.DATE_URI;
                String[] datesProjection = {
                        FinnkinoDbContract.DateEntry._ID,
                        FinnkinoDbContract.DateEntry.DATE_TIME
                };

                return new CursorLoader(
                        this,
                        datesQueryUri,
                        datesProjection,
                        null,
                        null,
                        null
                );

            default:
                throw new RuntimeException("Loader not implemented " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case SCHEDULE_LOADER_ID:
                mScheduleAdapter.swapCursor(data, SCHEDULE_LOADER_ID);
                mSmoothScrollLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 1);
                if (data.getCount() != 0) {
                    stopShowingLoading();
                } else {
                    noScheduleData();
                }
                break;
            case LOCATIONS_LOADER_ID:
                mScheduleAdapter.swapCursor(data, LOCATIONS_LOADER_ID);
                break;
            case DATES_LOADER_ID:
                mScheduleAdapter.swapCursor(data, DATES_LOADER_ID);
                break;
            default:
                throw new RuntimeException("Loader not implemented " + loader.toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //TODO remove references to old data?
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_filter:
                int firstVisibleItem = mSmoothScrollLinearLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    mSmoothScrollLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 1);
                } else {
                    mSmoothScrollLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
                }
                return true;

            case R.id.action_refresh:
                refreshWithDefaultValues();
                return true;

            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called when user clicks an item in the recyclerview
    @Override
    public void onScheduleClick(String finnkinoID, View sharedImageView,
                                View sharedBackgroundView, String iconTransitionName,
                                String backgroundTransitionName, String eventID,
                                String originalTitle) {

        // Create intent and start detail activity with transition animation
        Intent movieDetailIntent = new Intent(this, MovieDetailActivity.class);
        movieDetailIntent.putExtra(EXTRA_ICON_TRANSITION_NAME, iconTransitionName);
        movieDetailIntent.putExtra(EXTRA_BACKGROUND_TRANSITION_NAME, backgroundTransitionName);
        movieDetailIntent.putExtra(EXTRA_EVENT_ID, eventID);
        movieDetailIntent.putExtra(EXTRA_ORIGINAL_TITLE, originalTitle);

        Uri detailUri = FinnkinoDbContract.ScheduleEntry.FULL_SCHEDULE_URI.buildUpon()
                .appendPath(String.valueOf(finnkinoID))
                .build();
        movieDetailIntent.setData(detailUri);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(sharedImageView, iconTransitionName),
                Pair.create(sharedBackgroundView, backgroundTransitionName));

        startActivity(movieDetailIntent, options.toBundle());
    }

    // Called when user clicks the apply button in the spinner view in the recyclerview
    @Override
    public void onApplyButtonClick(String location, String date, String time, String rating) {
        showLoading();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        mDateofLastRefresh = simpleDateFormat.format(calendar.getTime());

        SimpleDateFormat hourDateFormat = new SimpleDateFormat("kk");
        String currentHour = hourDateFormat.format(calendar.getTime());

        mHourofLastCursorRestart = Integer.parseInt(hourDateFormat.format(calendar.getTime()));

        StringBuilder stringBuilder = new StringBuilder(
                "https://www.finnkino.fi/xml/Schedule/?area=");
        stringBuilder.append(location);
        stringBuilder.append("&dt=");
        stringBuilder.append(date);

        MainIntentService.fetchSchedule(this, stringBuilder.toString());

        /*
        FetchScheduleThread fetchThread = new FetchScheduleThread(
                stringBuilder.toString(),
                this);
        new Thread(fetchThread).start();
        */

        mScheduleSelection = FinnkinoDbContract.ScheduleEntry.RATING + " <=" + rating + " AND "
                + FinnkinoDbContract.ScheduleEntry.START_TIME + " >= ?";

        if (date.equals(mDateofLastRefresh) &&
                Integer.parseInt(currentHour) > Integer.parseInt(time.substring(0, 2))) {

            time = currentHour + ":00";
        }

        mScheduleSelectionArgs = new String[]{time};
        getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, null, this);

    }

    public void noScheduleData() {
        stopShowingLoading();
        showNoInfo();
    }

    // refresh schedule data with the default values. Used when app is launched
    private void refreshWithDefaultValues() {

        // show a loading indicator insted of the shedule recyclerview
        showLoading();

        // get the date and hour of this refresh
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        mDateofLastRefresh = simpleDateFormat.format(calendar.getTime());
        SimpleDateFormat hourDateFormat = new SimpleDateFormat("kk");
        String time = hourDateFormat.format(calendar.getTime()) + ":00";
        mHourofLastCursorRestart = Integer.parseInt(hourDateFormat.format(calendar.getTime()));

        // get the default area from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String area = sharedPreferences.getString(getString(R.string.default_location_key),
                getString(R.string.default_location_name));

        // fetch schedule, locations and dates from finnkino using intent services
        MainIntentService.fetchSchedule(this,
                "https://www.finnkino.fi/xml/Schedule/?area=" + area + "&dt=" + mDateofLastRefresh);
        MainIntentService.fetchLocations(this);
        MainIntentService.fetchDates(this);


        mScheduleAdapter.setLastSearchedAgeRating("Kaikki");
        mScheduleAdapter.setLastSearchedStartTime("11:00");
        mScheduleAdapter.setLastSearchedDate(mDateofLastRefresh);
        // saves id not name of area
        mScheduleAdapter.setLastSearchedLocation(area);

        mScheduleSelection = FinnkinoDbContract.ScheduleEntry.START_TIME + " >= ?";
        mScheduleSelectionArgs = new String[]{time};
        getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, null, this);
    }

    private void showLoading() {
        //mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mNoDataTextview.setVisibility(View.INVISIBLE);
    }

    private void stopShowingLoading() {
        //mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void showNoInfo(){
        mNoDataTextview.setVisibility(View.VISIBLE);

    }
}
