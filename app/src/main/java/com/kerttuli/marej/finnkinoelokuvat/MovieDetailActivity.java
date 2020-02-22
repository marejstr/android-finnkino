package com.kerttuli.marej.finnkinoelokuvat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

// Activity shows details on one item in the schedule recycleview
public class MovieDetailActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    private Uri mUri;
    private String mTitle;
    private static final String EVENT_URI_EXTRA = "event_uri_extra";
    private static final String OMDB_URI_EXTRA = "omdb_uri_extra";
    private static final int DETAIL_LOADER = 101;
    private static final int EVENT_LOADER = 102;
    private static final int OMDB_LOADER = 103;


    private static final int EVENT_ARRAY_SIZE = 6;
    private static final int SYNOPSIS_INDEX = 0;
    private static final int ACTORS_INDEX = 1;
    private static final int RATING_IMAGE_URL_INDEX = 2;
    private static final int DESCRIPTOR_URL_INDEX = 3;
    private static final int DIRECTORS_INDEX = 4;
    private static final int VIDEO_ID_INDEX = 5;

    private static final int OMDB_ARRAY_SIZE = 3;
    private static final int IMDB_RATING_INDEX = 0;
    private static final int IMDB_ID_INDEX = 1;
    private static final int METASCORE_INDEX = 2;


    private static final String[] MOVIE_DETAIL_PROJECTION = {
            FinnkinoDbContract.ScheduleEntry.TITLE,
            FinnkinoDbContract.ScheduleEntry.START_TIME,
            FinnkinoDbContract.ScheduleEntry.END_TIME,
            FinnkinoDbContract.ScheduleEntry.DATE,
            FinnkinoDbContract.ScheduleEntry.THEATRE_AUDITORIUM,
            FinnkinoDbContract.ScheduleEntry.ORIGINAL_TITLE,
            FinnkinoDbContract.ScheduleEntry.GENRES,
            FinnkinoDbContract.ScheduleEntry.SHOW_URL,
            FinnkinoDbContract.ScheduleEntry.LENGTH_IN_MINUTES,
            FinnkinoDbContract.ScheduleEntry.LANDSCAPE_IMAGE_URL,
            FinnkinoDbContract.ScheduleEntry.SMALL_ICON_URL,
            FinnkinoDbContract.ScheduleEntry.FINNKINO_ID
    };

    //private android.support.design.widget.CoordinatorLayout coordinatorLayout;
    private ImageView landscapeImageView;
    private ImageView iconImageView;
    private ImageView ratingImageView;
    private ImageView[] descriptorImageViews;
    private TextView titleTextView;
    private TextView dateTimeTextView;
    private TextView theatreAuditoriumTextView;
    private ExpandableTextView expandableSynopsisTextView;
    private TextView orginalTitleTextView;
    private TextView genreTextView;
    private TextView runningTimeTextView;
    private TextView directorTextView;
    private TextView actorsTextView;
    private TextView imdbRatingTextView;
    private TextView metascoreTextView;
    private ImageButton imdbButton;
    private ImageView mPlayImage;
    private NestedScrollView nestedScrollView;
    //private LinearLayout listItemLayout;
    private String mVideoId;


    private String mImdbID;
    private String mShowUrl;
    private String mFinnkinoId;

    @Override
    protected void onStart() {
        super.onStart();
        mPlayImage.setImageResource(R.drawable.ic_play_button);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //coordinatorLayout = (CoordinatorLayout) findViewById(R.id.detail_coordinator);

        //listItemLayout = (LinearLayout) findViewById(R.id.list_item_layout);
        landscapeImageView = (ImageView) findViewById(R.id.detail_backdrop_iv);
        iconImageView = (ImageView) findViewById(R.id.detail_icon_iv);
        ratingImageView = (ImageView) findViewById(R.id.rating_image_view);
        titleTextView = (TextView) findViewById(R.id.detail_title);
        dateTimeTextView = (TextView) findViewById(R.id.detail_date_time);
        theatreAuditoriumTextView = (TextView) findViewById(R.id.detail_theatre_auditorium);
        expandableSynopsisTextView = (ExpandableTextView) findViewById(R.id.expand_text_view);
        orginalTitleTextView = (TextView) findViewById(R.id.detail_orginal_title);
        genreTextView = (TextView) findViewById(R.id.detail_genre);
        runningTimeTextView = (TextView) findViewById(R.id.detail_running_time);
        directorTextView = (TextView) findViewById(R.id.detail_director);
        actorsTextView = (TextView) findViewById(R.id.detail_actors);
        imdbRatingTextView = (TextView) findViewById(R.id.imdb_rating_tv);
        metascoreTextView = (TextView) findViewById(R.id.metascore_tv);
        descriptorImageViews = new ImageView[]{
                (ImageView)findViewById(R.id.descriptor_image_view1),
                (ImageView)findViewById(R.id.descriptor_image_view2),
                (ImageView)findViewById(R.id.descriptor_image_view3),
                (ImageView)findViewById(R.id.descriptor_image_view4)
        };
        imdbButton = (ImageButton) findViewById(R.id.imdb_button);
        ImageButton finnkinoButton = (ImageButton) findViewById(R.id.finnkino_button_detail);
        ImageButton metaButton = (ImageButton) findViewById(R.id.meta_button);
        nestedScrollView = (NestedScrollView) findViewById(R.id.detail_nested_scrollview);
        mPlayImage = (ImageView) findViewById(R.id.youtube_play);

        final ImageSwitcher backButtonSwitcher = (ImageSwitcher) findViewById(R.id.detail_back_button_switcher);
        backButtonSwitcher.setImageResource(R.drawable.ic_arrow_back_24px);
        backButtonSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextSwitcher toolbarTitleSwitcher = (TextSwitcher) findViewById(R.id.detail_toolbar_text_switcher);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toolbarTitleSwitcher.setInAnimation(in);
        toolbarTitleSwitcher.setOutAnimation(out);
        backButtonSwitcher.setInAnimation(in);
        backButtonSwitcher.setOutAnimation(out);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.detail_appbar);

        // called when appbar offset changes
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean textShowing = false;
            int scrollRange;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = appBarLayout.getTotalScrollRange();
                int pixelFromTop = scrollRange + verticalOffset;

                if (pixelFromTop < 100 && !textShowing) {
                    toolbarTitleSwitcher.setText(mTitle);
                    backButtonSwitcher.setImageResource(R.drawable.ic_arrow_back_black_24px);
                    //detailMainToolbar.setNavigationIcon(R.drawable.filter_black);
                    textShowing = true;
                } else if (pixelFromTop > 100 && textShowing) {
                    toolbarTitleSwitcher.setText(" ");
                    backButtonSwitcher.setImageResource(R.drawable.ic_arrow_back_24px);
                    //detailMainToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24px);
                    textShowing = false;
                }
            }
        });

        // get intent and set up view
        Bundle extras = getIntent().getExtras();
        mUri = getIntent().getData();
        final String eventID = extras.getString(MainActivity.EXTRA_EVENT_ID);
        final String originalTitle = extras.getString(MainActivity.EXTRA_ORIGINAL_TITLE);

        if (mUri == null) {
            Log.e(TAG, "uri is null");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String iconTransitionName = extras.getString(MainActivity.EXTRA_ICON_TRANSITION_NAME);
            iconImageView.setTransitionName(iconTransitionName);
            String backgroundTransitionName = extras.getString(MainActivity.EXTRA_BACKGROUND_TRANSITION_NAME);
            //coordinatorLayout.setTransitionName(backgroundTransitionName);
            //nestedScrollView.setTransitionName(backgroundTransitionName);
            //listItemLayout.setTransitionName(backgroundTransitionName);
        }

        // Button disabled currently due to imdb api issues
        imdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageManager packageManager = getPackageManager();
                Uri imdbUrl = Uri.parse("imdb:///title/" + mImdbID);
                Intent imdbIntent = new Intent(Intent.ACTION_VIEW, imdbUrl);
                if (imdbIntent.resolveActivity(packageManager) != null) {
                    startActivity(imdbIntent);
                } else {
                    Uri webpage = Uri.parse("http://www.imdb.com/title/" + mImdbID);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent);

                    }
                }
            }});

        // Buy tickets on finnkino site
        finnkinoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                PackageManager packageManager = getPackageManager();
                Uri webpage = Uri.parse("https://m.finnkino.fi/Websales/Show/" + mFinnkinoId);
                //Uri webpage = Uri.parse(mShowUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);

                }
            }
        });

        // Show metacritic search for movie
        metaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempTitle = originalTitle.replaceAll("\\(.*?\\) ?", "");
                tempTitle = tempTitle.replaceAll("3D", "");

                PackageManager packageManager = getPackageManager();
                Uri webpage = Uri.parse("http://www.metacritic.com/search/movie/" + tempTitle + "/results");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                }
            }
        });

        // Play youtube video
        mPlayImage.setOnClickListener(new View.OnClickListener() {
            String apiKey = "API key removed";
            boolean autoplay = true;
            boolean boxmode = false;

            @Override
            public void onClick(View v) {
                if (mVideoId != null) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                            MovieDetailActivity.this, apiKey,
                            mVideoId, 0, autoplay, boxmode);

                    List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 0);
                    if (resolveInfo != null && !resolveInfo.isEmpty()) {
                        mPlayImage.setImageResource(R.drawable.ic_threedots);
                        startActivityForResult(intent, 1);
                    } else {
                        YouTubeInitializationResult.SERVICE_MISSING
                                .getErrorDialog(MovieDetailActivity.this, 2).show();
                    }
                }
            }
        });


        Bundle eventUriBundle = new Bundle();
        eventUriBundle.putString(EVENT_URI_EXTRA, "https://www.finnkino.fi/xml/Events/?eventID=" + eventID);

        String tempOrigTitle = originalTitle.replaceAll("\\(.*?\\) ?", "");
        tempOrigTitle = tempOrigTitle.replaceAll("3D", "");
        tempOrigTitle = tempOrigTitle.replaceAll("\\s", "+");
        Bundle omdbUriBundle = new Bundle();
        omdbUriBundle.putString(OMDB_URI_EXTRA, "https://www.omdbapi.com/?t=" + tempOrigTitle);

        // Load data from database
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, new cursorLoaderCallbacks(this));
        getSupportLoaderManager().initLoader(EVENT_LOADER, eventUriBundle, new asyncLoaderCallbacks(this));
        getSupportLoaderManager().initLoader(OMDB_LOADER, omdbUriBundle, new asyncLoaderCallbacks(this));
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Rect scrollBounds = new Rect();
            nestedScrollView.getHitRect(scrollBounds);
            if (iconImageView.getLocalVisibleRect(scrollBounds)) {
                finishAfterTransition();
            } else {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    // Database loader
    private class cursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context cursorLoaderContext;
        public cursorLoaderCallbacks(Context context) {
            cursorLoaderContext = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case DETAIL_LOADER:
                    return new CursorLoader(
                            cursorLoaderContext,
                            mUri,
                            MOVIE_DETAIL_PROJECTION,
                            null,
                            null,
                            null
                    );

                default:
                    throw new RuntimeException("Loader not implemented" + id);
            }
        }

        // Set data for views with loader callback
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case DETAIL_LOADER:

                    if (data == null || !data.moveToFirst()) {
                        return;
                    }

                    Glide.with(cursorLoaderContext)
                            .load(data.getString(
                                    data.getColumnIndexOrThrow(
                                            FinnkinoDbContract.ScheduleEntry.LANDSCAPE_IMAGE_URL)))
                            .dontAnimate()
                            .into(landscapeImageView);

                    Glide.with(cursorLoaderContext)
                            .load(data.getString(
                                    data.getColumnIndexOrThrow(
                                            FinnkinoDbContract.ScheduleEntry.SMALL_ICON_URL)))
                            .dontAnimate()
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e,
                                                           String model,
                                                           Target<GlideDrawable> target,
                                                           boolean isFirstResource) {
                                    supportStartPostponedEnterTransition();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource,
                                                               String model,
                                                               Target<GlideDrawable> target,
                                                               boolean isFromMemoryCache,
                                                               boolean isFirstResource) {
                                    supportStartPostponedEnterTransition();
                                    return false;
                                }
                            })
                            .into(iconImageView);

                    //TODO check if string empty
                    mTitle = data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.TITLE));

                    String startEndTime =
                            data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.START_TIME))
                                    +  " - " +
                                    data.getString(data.getColumnIndexOrThrow(
                                            FinnkinoDbContract.ScheduleEntry.END_TIME))
                                    +  "     " +
                                    data.getString(data.getColumnIndexOrThrow(
                                            FinnkinoDbContract.ScheduleEntry.DATE));
                    dateTimeTextView.setText(startEndTime);

                    titleTextView.setText(
                            data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.TITLE)));

                    theatreAuditoriumTextView.setText(
                            data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.THEATRE_AUDITORIUM)));

                    orginalTitleTextView.setText(
                            data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.ORIGINAL_TITLE)));

                    genreTextView.setText(
                            data.getString(data.getColumnIndexOrThrow(
                                    FinnkinoDbContract.ScheduleEntry.GENRES)));

                    String runningTime = data.getString(data.getColumnIndexOrThrow(FinnkinoDbContract.ScheduleEntry.LENGTH_IN_MINUTES)) + " min";
                    runningTimeTextView.setText(runningTime);

                    mShowUrl = data.getString(data.getColumnIndexOrThrow(
                            FinnkinoDbContract.ScheduleEntry.SHOW_URL));

                    mFinnkinoId = data.getString(data.getColumnIndexOrThrow(
                            FinnkinoDbContract.ScheduleEntry.FINNKINO_ID));

                    break;

                default:
                    throw new RuntimeException("Loader not implemented" + loader.getId());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class asyncLoaderCallbacks implements LoaderManager.LoaderCallbacks<String[]> {

        private Context asyncLoaderContext;

        private asyncLoaderCallbacks(Context context) {
            asyncLoaderContext = context;
        }

        @Override
        public Loader<String[]> onCreateLoader(int id, final Bundle args) {

            switch (id) {
                case EVENT_LOADER:
                    return new AsyncTaskLoader<String[]>(asyncLoaderContext) {
                        @Override
                        protected void onStartLoading() {
                            if (args != null) {
                                forceLoad();
                            }
                        }

                        @Override
                        public String[] loadInBackground() {

                            String stringUrl = args.getString(EVENT_URI_EXTRA);

                            URL url = null;
                            try {
                                url = new URL(stringUrl);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            //fetch XML from Finnkino
                            HttpURLConnection urlConnection = null;
                            InputStream inputStream = null;
                            String[] stringValues = new String[EVENT_ARRAY_SIZE];

                            try {
                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setReadTimeout(10000);
                                urlConnection.setConnectTimeout(15000);
                                urlConnection.setRequestMethod("GET");
                                urlConnection.connect();

                                inputStream = urlConnection.getInputStream();

                                XmlPullParser parser = Xml.newPullParser();
                                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                                parser.setInput(inputStream, null);

                                int eventType = parser.getEventType();
                                String name;

                                String synopsis = "";
                                String ratingImageUrl = "";
                                String actors = "";
                                String directors = "";
                                String contentDescriptorUrls = "";
                                String videoID = "";

                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    name = parser.getName();
                                    switch (eventType) {
                                        case XmlPullParser.START_TAG:
                                            switch (name) {
                                                case "Synopsis":
                                                    synopsis = parser.nextText();
                                                    break;
                                                case "RatingImageUrl":
                                                    ratingImageUrl = parser.nextText();
                                                    break;
                                                case "Cast":
                                                    actors = readActors(parser);
                                                    break;
                                                case "Directors":
                                                    directors = readDirectors(parser);
                                                    break;
                                                case "ContentDescriptors":
                                                    contentDescriptorUrls = readDescriptorUrls(parser);
                                                    break;
                                                case "Location":
                                                    videoID = parser.nextText();
                                                    break;
                                            }
                                            break;

                                        case XmlPullParser.END_TAG:
                                            if (name.equals("Event")) {
                                                stringValues[SYNOPSIS_INDEX] = synopsis;
                                                stringValues[RATING_IMAGE_URL_INDEX] = ratingImageUrl;
                                                stringValues[ACTORS_INDEX] = actors;
                                                stringValues[DESCRIPTOR_URL_INDEX] = contentDescriptorUrls;
                                                stringValues[DIRECTORS_INDEX] = directors;
                                                stringValues[VIDEO_ID_INDEX] = videoID;

                                            }
                                            break;
                                    }
                                    eventType = parser.next();
                                }


                            } catch (IOException | XmlPullParserException e) {
                                e.printStackTrace();
                            } finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return stringValues;
                        }

                        private String readDirectors(XmlPullParser parser) throws XmlPullParserException, IOException {

                            StringBuilder builder = new StringBuilder();
                            int directorEventType = parser.next();
                            String directorsTagName = parser.getName();
                            String prefix = "";

                            while (!(directorEventType == XmlPullParser.END_TAG && directorsTagName.equals("Directors"))) {
                                switch (directorEventType) {
                                    case XmlPullParser.START_TAG:
                                        switch (directorsTagName) {
                                            case "FirstName":
                                                String testName = parser.nextText();
                                                builder.append(prefix);
                                                builder.append(testName);
                                                prefix = ", ";
                                                break;
                                            case "LastName":
                                                String testLastName = parser.nextText();
                                                builder.append(" ");
                                                builder.append(testLastName);
                                                break;
                                        }
                                        break;
                                }
                                directorEventType = parser.next();
                                directorsTagName = parser.getName();
                            }
                            return builder.toString();
                        }

                        private String readActors(XmlPullParser parser) throws XmlPullParserException, IOException {

                            StringBuilder builder = new StringBuilder();
                            int actorEventType = parser.next();
                            String actorsTagName = parser.getName();
                            String prefix = "";

                            while (!(actorEventType == XmlPullParser.END_TAG && actorsTagName.equals("Cast"))) {
                                switch (actorEventType) {
                                    case XmlPullParser.START_TAG:
                                        switch (actorsTagName) {
                                            case "FirstName":
                                                String testName = parser.nextText();
                                                builder.append(prefix);
                                                builder.append(testName);
                                                prefix = ", ";
                                                break;
                                            case "LastName":
                                                String testLastName = parser.nextText();
                                                builder.append(" ");
                                                builder.append(testLastName);
                                                break;
                                        }
                                        break;
                                }
                                actorEventType = parser.next();
                                actorsTagName = parser.getName();
                            }
                            return builder.toString();
                        }

                        private String readDescriptorUrls(XmlPullParser parser) throws IOException, XmlPullParserException {

                            StringBuilder builder = new StringBuilder();
                            int descriptorEventType = parser.next();
                            String descriptorsTagName = parser.getName();
                            String prefix = "";

                            while (!(descriptorEventType == XmlPullParser.END_TAG &&
                                    descriptorsTagName.equals("ContentDescriptors"))) {

                                switch (descriptorEventType) {
                                    case XmlPullParser.START_TAG:
                                        switch (descriptorsTagName) {
                                            case "ImageURL":
                                                String imageUrl = parser.nextText();
                                                builder.append(prefix);
                                                builder.append(imageUrl);
                                                prefix = ",";
                                                break;
                                        }
                                        break;
                                }
                                descriptorEventType = parser.next();
                                descriptorsTagName = parser.getName();
                            }
                            return builder.toString();
                        }
                    };

                case OMDB_LOADER:
                    return new AsyncTaskLoader<String[]>(asyncLoaderContext) {

                        @Override
                        protected void onStartLoading() {
                            if (args != null) {
                                //TODO kolla att inte kallas på orientation change
                                //Bodre bara kalla force load om vi inte ännu har data
                                forceLoad();
                            }
                        }

                        @Override
                        public String[] loadInBackground() {
                            String[] stringValues = new String[OMDB_ARRAY_SIZE];

                            String stringUrl = args.getString(OMDB_URI_EXTRA);

                            URL url = null;
                            try {
                                url = new URL(stringUrl);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            //fetch XML from Finnkino
                            HttpURLConnection urlConnection = null;
                            InputStream inputStream = null;
                            String responseString = null;

                            try {
                                assert url != null;
                                urlConnection = (HttpURLConnection) url.openConnection();
                                inputStream = urlConnection.getInputStream();

                                Scanner scanner = new Scanner(inputStream);
                                scanner.useDelimiter("\\A");

                                boolean hasInput = scanner.hasNext();
                                if (hasInput) {
                                    responseString = scanner.next();
                                }
                                scanner.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                           //parse json
                            if (responseString != null) {
                                try {
                                    JSONObject omdbJson = new JSONObject(responseString);

                                    //TODO check for error

                                    stringValues[IMDB_RATING_INDEX] = omdbJson.getString("imdbRating");
                                    stringValues[IMDB_ID_INDEX] = omdbJson.getString("imdbID");
                                    stringValues[METASCORE_INDEX] = omdbJson.getString("Metascore");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            return stringValues;
                        }
                    };

                default:
                    throw new RuntimeException("Loader not implemented " + String.valueOf(id));
            }
        }


        @Override
        public void onLoadFinished(Loader<String[]> loader, String[] data) {
            switch (loader.getId()) {
                case EVENT_LOADER:
                    expandableSynopsisTextView.setText(data[SYNOPSIS_INDEX]);
                    actorsTextView.setText(data[ACTORS_INDEX]);
                    directorTextView.setText(data[DIRECTORS_INDEX]);
                    mVideoId = data[VIDEO_ID_INDEX];

                    Glide.with(asyncLoaderContext)
                            .load(data[RATING_IMAGE_URL_INDEX])
                            .into(ratingImageView);

                    String[] splitArray = data[DESCRIPTOR_URL_INDEX].split(",");

                    for (int i = 0; i < splitArray.length; i++) {

                        Glide.with(asyncLoaderContext)
                                .load(splitArray[i])
                                .into(descriptorImageViews[i]);
                        descriptorImageViews[i].setVisibility(View.VISIBLE);
                    }

                    break;

                case OMDB_LOADER:

                    if (data[IMDB_RATING_INDEX] != null && data[IMDB_RATING_INDEX].equals("N/A")) {
                        imdbRatingTextView.setText("-");
                    } else {
                        imdbRatingTextView.setText(data[IMDB_RATING_INDEX]);
                    }

                    if (data[METASCORE_INDEX] != null && data[METASCORE_INDEX].equals("N/A")) {
                        metascoreTextView.setText("-");
                    } else {
                        metascoreTextView.setText(data[METASCORE_INDEX]);
                    }

                    if (data[IMDB_ID_INDEX] == null) {
                        imdbButton.setEnabled(false);
                    } else {
                        mImdbID = data[IMDB_ID_INDEX];
                    }

                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<String[]> loader) {

        }
    }
}
