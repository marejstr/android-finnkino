package com.kerttuli.marej.finnkinoelokuvat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Adapter for schedule recycler view
public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Cursor mScheduleCursor;
    private Cursor mLocationsCursor;
    private Cursor mDatesCursor;
    private ScheduleClickHandler mClickHandler;

    private String mLastSearchedLocation;
    private String mLastSearchedStartTime;
    private String mLastSearchedAgeRating;
    private String mLastSearchedDate;
    private String mLastSearchedCursorDate;

    private String mSelectedLocationID;

    private static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_MOVIE = 1;


    public ScheduleAdapter(@NonNull Context context, ScheduleClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View headerView = LayoutInflater.from(mContext).inflate(R.layout.header_list_item, parent, false);
                return new SpinnerViewHolder(headerView);

            case VIEW_TYPE_MOVIE:
                View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_list_item_linear, parent, false);
                return new MovieListItemViewHolder(view);

            default:
                throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                final SpinnerViewHolder spinnerViewHolder = (SpinnerViewHolder) holder;

                String[] fromLocationColumns = {FinnkinoDbContract.LocationEntry.LOCATION_NAME};
                int[] toLocationViews = {android.R.id.text1};
                SimpleCursorAdapter locationAdapter = new SimpleCursorAdapter(
                        mContext,
                        android.R.layout.simple_spinner_item,
                        mLocationsCursor,
                        fromLocationColumns,
                        toLocationViews,
                        0
                );
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerViewHolder.locationSpinner.setAdapter(locationAdapter);

                String[] fromDateColumns = {FinnkinoDbContract.DateEntry.DATE_TIME};
                int[] toDateViews = {android.R.id.text1};
                SimpleCursorAdapter dateAdapter = new SimpleCursorAdapter(
                        mContext,
                        android.R.layout.simple_spinner_item,
                        mDatesCursor,
                        fromDateColumns,
                        toDateViews,
                        0
                );
                dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerViewHolder.datesSpinner.setAdapter(dateAdapter);

                ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(mContext,
                        R.array.time_spinner, android.R.layout.simple_spinner_item);
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerViewHolder.timeSpinner.setAdapter(timeAdapter);

                ArrayAdapter<CharSequence> ratingAdapter = ArrayAdapter.createFromResource(mContext,
                        R.array.rating_spinner, android.R.layout.simple_spinner_item);
                ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerViewHolder.ratingSpinner.setAdapter(ratingAdapter);

                spinnerViewHolder.spinnerDefaultsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spinnerViewHolder.datesSpinner.setSelection(0);
                        spinnerViewHolder.ratingSpinner.setSelection(0);
                        spinnerViewHolder.timeSpinner.setSelection(0);

                        SharedPreferences sharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(mContext);
                        String area = sharedPreferences.getString(
                                mContext.getString(R.string.default_location_key),
                                mContext.getString(R.string.default_location_name));

                        for (int i = 0; i < spinnerViewHolder.locationSpinner.getCount(); i++) {
                            Cursor value = (Cursor) spinnerViewHolder.locationSpinner.getItemAtPosition(i);
                            String location = value.getString(
                                    value.getColumnIndexOrThrow(
                                            FinnkinoDbContract.LocationEntry.FINNKINO_LOCATION_ID));
                            if (location.equals(area)) {
                                spinnerViewHolder.locationSpinner.setSelection(i);
                            }
                        }
                    }
                });

                spinnerViewHolder.spinnerApplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Cursor locationCursor = (Cursor) spinnerViewHolder.locationSpinner.getSelectedItem();
                        mSelectedLocationID = locationCursor.getString(locationCursor.getColumnIndexOrThrow(
                                FinnkinoDbContract.LocationEntry.FINNKINO_LOCATION_ID));
                        //TODO kolla igenom hela bindonviewholder för fel som denna
                        mLastSearchedLocation = mSelectedLocationID;
                        /*
                        mLastSearchedLocation = locationCursor.getString(locationCursor.getColumnIndexOrThrow(
                                FinnkinoDbContract.LocationEntry.LOCATION_NAME));
                                */

                        Cursor dateCursor = (Cursor) spinnerViewHolder.datesSpinner.getSelectedItem();

                        mLastSearchedCursorDate = dateCursor.getString(dateCursor.getColumnIndexOrThrow(
                                FinnkinoDbContract.DateEntry.DATE_TIME));
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                        String todaysDate = simpleDateFormat.format(calendar.getTime());
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        String tomorrowsDate = simpleDateFormat.format(calendar.getTime());

                        if (mLastSearchedCursorDate.equals(mContext.getString(R.string.today))) {
                            mLastSearchedDate = todaysDate;
                        } else if (mLastSearchedCursorDate.equals(mContext.getString(R.string.tomorrow))) {
                            mLastSearchedDate = tomorrowsDate;
                        } else {
                            mLastSearchedDate = mLastSearchedCursorDate.substring(5);
                        }

                        mLastSearchedStartTime = spinnerViewHolder.timeSpinner.getSelectedItem().toString();

                        mLastSearchedAgeRating = spinnerViewHolder.ratingSpinner.getSelectedItem().toString();
                        if (mLastSearchedAgeRating.equals("S")) {
                            mLastSearchedAgeRating = "0";
                        } else if (mLastSearchedAgeRating.equals("Kaikki")) {
                            mLastSearchedAgeRating = "99";
                        }

                        mClickHandler.onApplyButtonClick(mSelectedLocationID, mLastSearchedDate,
                                mLastSearchedStartTime, mLastSearchedAgeRating);
                    }
                });

                for (int i = 0; i < spinnerViewHolder.locationSpinner.getCount(); i++) {
                    Cursor value = (Cursor) spinnerViewHolder.locationSpinner.getItemAtPosition(i);
                    String location = value.getString(
                            value.getColumnIndexOrThrow(
                                    FinnkinoDbContract.LocationEntry.FINNKINO_LOCATION_ID));
                    if (location.equals(mLastSearchedLocation)) {
                        spinnerViewHolder.locationSpinner.setSelection(i);
                    }
                }

                for (int i = 0; i < spinnerViewHolder.datesSpinner.getCount(); i++) {
                    Cursor value = (Cursor) spinnerViewHolder.datesSpinner.getItemAtPosition(i);
                    String date = value.getString(
                            value.getColumnIndexOrThrow(
                                    FinnkinoDbContract.DateEntry.DATE_TIME));
                    if (date.equals(mLastSearchedCursorDate)) {
                        spinnerViewHolder.datesSpinner.setSelection(i);
                    }
                }

                for (int i = 0; i < spinnerViewHolder.timeSpinner.getCount(); i++) {
                    String time = spinnerViewHolder.timeSpinner.getItemAtPosition(i).toString();
                    if (time.equals(mLastSearchedStartTime)) {
                        spinnerViewHolder.timeSpinner.setSelection(i);
                    }
                }

                for (int i = 0; i < spinnerViewHolder.ratingSpinner.getCount(); i++) {
                    String rating = spinnerViewHolder.ratingSpinner.getItemAtPosition(i).toString();
                    if (rating.equals(mLastSearchedAgeRating)) {
                        spinnerViewHolder.ratingSpinner.setSelection(i);
                    }
                }

                break;
            case VIEW_TYPE_MOVIE:
                final MovieListItemViewHolder listItemViewHolder = (MovieListItemViewHolder) holder;
                //position -1 ? pga första viewn
                mScheduleCursor.moveToPosition(position - 1);

                listItemViewHolder.titleTextView.setText(
                        mScheduleCursor.getString(
                                mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.TITLE)
                        )
                );

                String date = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.DATE));

                String startTime = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.START_TIME));

                int subheadBoolean;
                int thisTime = Integer.parseInt(startTime.substring(0,2));

                if (position > 1) {

                    mScheduleCursor.moveToPosition(position - 2);
                    String lastStartTime = mScheduleCursor.getString(
                            mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.START_TIME));

                    int lastTime = Integer.parseInt(lastStartTime.substring(0, 2));

                    if (thisTime > lastTime) {
                        subheadBoolean = 1;
                    } else {
                        subheadBoolean = 0;
                    }

                    mScheduleCursor.moveToPosition(position - 1);
                } else {
                    subheadBoolean = 1;
                }

                String hourString;
                listItemViewHolder.listSubtitleTextView.setVisibility(View.VISIBLE);
                String[] dateArray = date.split("\\.");
                int day = Integer.parseInt(dateArray[0]);
                int month = Integer.parseInt(dateArray[1]);
                int year = Integer.parseInt(dateArray[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day);
                SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE dd.MM", new Locale("fi", "FI"));
                String displayDate = displayDateFormat.format(cal.getTime());

                if (subheadBoolean == 1) {

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String todaysDate = simpleDateFormat.format(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    String tomorrowsDate = simpleDateFormat.format(calendar.getTime());

                    if (todaysDate.equals(date)) {
                        hourString = startTime.substring(0, 2) + ":00   Tänään";
                    } else if (tomorrowsDate.equals(date)) {
                        hourString = startTime.substring(0, 2) + ":00    Huomenna";
                    } else {
                        hourString = startTime.substring(0, 2) + ":00      " + displayDate;
                    }
                    listItemViewHolder.listSubtitleTextView.setText(hourString);
                } else {
                    listItemViewHolder.listSubtitleTextView.setVisibility(View.GONE);
                }

                String endTime = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.END_TIME));

                listItemViewHolder.timeTextView.setText(startTime + " - " + endTime);

                listItemViewHolder.auditoriumTextView.setText(
                        mScheduleCursor.getString(
                                mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.THEATRE_AUDITORIUM)
                        ));

                listItemViewHolder.genreTextView.setText(
                        mScheduleCursor.getString(
                                mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.GENRES)
                        )
                );

                String rating = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.RATING)
                );
                if (rating.equals("0")) {
                    listItemViewHolder.ratingTextView.setText("S");
                } else {
                    listItemViewHolder.ratingTextView.setText(rating);
                }

                Glide.with(mContext).load(
                        mScheduleCursor.getString(
                                mScheduleCursor.getColumnIndexOrThrow(
                                        FinnkinoDbContract.ScheduleEntry.SMALL_ICON_URL)))
                        .dontAnimate()
                        .into(listItemViewHolder.movieIconImageView);

                final String finnkinoID = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.FINNKINO_ID));

                final String eventID = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.EVENT_ID));

                final String originalTitle = mScheduleCursor.getString(
                        mScheduleCursor.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.ORIGINAL_TITLE));

                final String iconTransitionName = "icon_id_" + finnkinoID;
                final String backgroundTransitionName = "background_id_" + finnkinoID;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    listItemViewHolder.movieIconImageView.setTransitionName(iconTransitionName);
                    holder.itemView.setTransitionName(backgroundTransitionName);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClickHandler.onScheduleClick(
                                finnkinoID,
                                listItemViewHolder.movieIconImageView,
                                holder.itemView,
                                iconTransitionName,
                                backgroundTransitionName,
                                eventID,
                                originalTitle);
                    }
                });

                break;

            default:
                throw new RuntimeException("View type not supported" + viewType);
        }
    }

    @Override
    public int getItemCount() {
        if (mScheduleCursor == null || mScheduleCursor.getCount() == 0) {
            return 1;
        }else {
            return mScheduleCursor.getCount() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }

        else {
            return VIEW_TYPE_MOVIE;
        }
    }

    public void swapCursor(Cursor newCursor, int id) {
        switch (id) {
            case MainActivity.SCHEDULE_LOADER_ID:
                mScheduleCursor = newCursor;
                notifyDataSetChanged();
                //notifyItemRangeRemoved(1, 7);
                //notifyItemRangeInserted(1, 7);
                break;
            case MainActivity.LOCATIONS_LOADER_ID:
                mLocationsCursor = newCursor;
                notifyItemChanged(0);
                break;
            case MainActivity.DATES_LOADER_ID:
                mDatesCursor = newCursor;
                notifyItemChanged(0);
                break;
            default:
                throw new RuntimeException("Cursor not implemented " + id);
        }
    }

    public String getLastSearchedLocation() {
        return mLastSearchedLocation;
    }

    public String getLastSearchedStartTime() {
        return mLastSearchedStartTime;
    }

    public String getLastSearchedAgeRating() {
        return mLastSearchedAgeRating;
    }

    public String getLastSearchedDate() {
        return mLastSearchedDate;
    }

    public void setLastSearchedLocation(String lastSearchedLocation) {
        this.mLastSearchedLocation = lastSearchedLocation;
    }

    public void setLastSearchedStartTime(String lastSearchedStartTime) {
        this.mLastSearchedStartTime = lastSearchedStartTime;
    }

    public void setLastSearchedAgeRating(String lastSearchedAgeRating) {
        this.mLastSearchedAgeRating = lastSearchedAgeRating;
    }

    public void setLastSearchedDate(String lastSearchedDate) {
        this.mLastSearchedDate = lastSearchedDate;
    }

    private class MovieListItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView movieIconImageView;
        final TextView titleTextView;
        final TextView listSubtitleTextView;
        final TextView timeTextView;
        final TextView auditoriumTextView;
        final TextView genreTextView;
        final TextView ratingTextView;

        public MovieListItemViewHolder(View itemView) {
            super(itemView);

            listSubtitleTextView = (TextView) itemView.findViewById(R.id.list_subtitle);
            movieIconImageView = (ImageView) itemView.findViewById(R.id.movie_icon_iv);
            titleTextView = (TextView) itemView.findViewById(R.id.title_tv);
            timeTextView = (TextView) itemView.findViewById(R.id.time_tv);
            auditoriumTextView = (TextView) itemView.findViewById(R.id.auditorium_tv);
            genreTextView = (TextView) itemView.findViewById(R.id.genre_tv);
            ratingTextView = (TextView) itemView.findViewById(R.id.rating_tv);
        }
    }

    private class SpinnerViewHolder extends RecyclerView.ViewHolder {

        final Spinner locationSpinner;
        final Spinner datesSpinner;
        final Spinner timeSpinner;
        final Spinner ratingSpinner;
        final Button spinnerApplyButton;
        final Button spinnerDefaultsButton;

        public SpinnerViewHolder(View itemView) {
            super(itemView);

            locationSpinner = (Spinner) itemView.findViewById(R.id.location_spinner);
            datesSpinner = (Spinner) itemView.findViewById(R.id.date_spinner);
            timeSpinner = (Spinner) itemView.findViewById(R.id.time_spinner);
            ratingSpinner = (Spinner) itemView.findViewById(R.id.rating_spinner);
            spinnerApplyButton = (Button) itemView.findViewById(R.id.filterApplyButton);
            spinnerDefaultsButton = (Button) itemView.findViewById(R.id.defaults_button);

        }
    }
}
