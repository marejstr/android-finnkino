package com.kerttuli.marej.finnkinoelokuvat.fetch;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class FetchScheduleThread extends FetchTemplateThread{

    private String mUrl;
    private Context mContext;

    public FetchScheduleThread(String url, Context context) {
        mUrl = url;
        mContext = context;
    }

    @Override
    URL getUrl() throws MalformedURLException {
        return new URL(mUrl);
    }

    @Override
    LinkedList<ContentValues> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

        LinkedList<ContentValues> contentValuesList = new LinkedList<>();

        int eventType = parser.getEventType();
        String name;
        int lastItemHour = 0;

        int ratingInt = 0;
        int subheadBoolean = 0;
        String title = "";
        String originalTitle = "";
        String date = "";
        String startTime = "";
        String endTime = "";
        String genres = "";
        String lengthInMinutes = "";
        String theatreAndAuditorium = "";
        String showUrl = "";
        String smallImage = "";
        String landscapeImage = "";
        String finnkinoID = "";
        String eventID = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            ContentValues cV = new ContentValues();
            name = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    switch (name) {
                        case "Title":
                            title = parser.nextText();
                            break;
                        case "OriginalTitle":
                            originalTitle = parser.nextText();
                            break;
                        case "dttmShowStart":
                            startTime = parser.nextText();
                            String[] startSplitArray = startTime.split("-|T|:");
                            date = startSplitArray[2] + "." + startSplitArray[1] + "." + startSplitArray[0];
                            startTime = startSplitArray[3] + ":" + startSplitArray[4];
                            break;
                        case "dttmShowEnd":
                            endTime = parser.nextText();
                            String[] endSplitArray = endTime.split("-|T|:");
                            endTime = endSplitArray[3] + ":" + endSplitArray[4];
                            break;
                        case "Rating":
                            String rating = parser.nextText();
                            if (rating.equals("S") || rating.equals("Luok_vap")) {
                                ratingInt = 0;
                            } else {
                                ratingInt = Integer.parseInt(rating);
                            }
                            break;
                        case "Genres":
                            genres = parser.nextText();
                            break;
                        case "LengthInMinutes":
                            lengthInMinutes = parser.nextText();
                            break;
                        case "TheatreAndAuditorium":
                            theatreAndAuditorium = parser.nextText();
                            break;
                        case "ShowURL":
                            showUrl = parser.nextText();
                            break;
                        case "EventSmallImagePortrait":
                            smallImage = parser.nextText();
                            break;
                        case "EventLargeImageLandscape":
                            landscapeImage = parser.nextText();
                            break;
                        case "ID":
                            finnkinoID = parser.nextText();
                            break;
                        case "EventID":
                            eventID = parser.nextText();
                            break;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (name.equals("Show")) {
                        cV.put(FinnkinoDbContract.ScheduleEntry.TITLE, title);
                        cV.put(FinnkinoDbContract.ScheduleEntry.ORIGINAL_TITLE, originalTitle);
                        cV.put(FinnkinoDbContract.ScheduleEntry.DATE, date);
                        cV.put(FinnkinoDbContract.ScheduleEntry.START_TIME, startTime);
                        cV.put(FinnkinoDbContract.ScheduleEntry.END_TIME, endTime);
                        cV.put(FinnkinoDbContract.ScheduleEntry.RATING, ratingInt);
                        cV.put(FinnkinoDbContract.ScheduleEntry.GENRES, genres);
                        cV.put(FinnkinoDbContract.ScheduleEntry.LENGTH_IN_MINUTES, lengthInMinutes);
                        cV.put(FinnkinoDbContract.ScheduleEntry.THEATRE_AUDITORIUM, theatreAndAuditorium);
                        cV.put(FinnkinoDbContract.ScheduleEntry.SHOW_URL, showUrl);
                        cV.put(FinnkinoDbContract.ScheduleEntry.SMALL_ICON_URL, smallImage);
                        cV.put(FinnkinoDbContract.ScheduleEntry.LANDSCAPE_IMAGE_URL, landscapeImage);
                        cV.put(FinnkinoDbContract.ScheduleEntry.FINNKINO_ID, finnkinoID);
                        cV.put(FinnkinoDbContract.ScheduleEntry.EVENT_ID, eventID);
                        contentValuesList.add(cV);
                    }
                    break;
            }
            eventType = parser.next();
        }
        return contentValuesList;
    }

    @Override
    void DeleteAndBulkInsert(ContentValues[] cvArray) {
        ContentResolver finnkinoContentResolver = mContext.getContentResolver();
        finnkinoContentResolver.delete(
                FinnkinoDbContract.ScheduleEntry.FULL_SCHEDULE_URI,
                null,
                null
        );
        finnkinoContentResolver.bulkInsert(
                FinnkinoDbContract.ScheduleEntry.FULL_SCHEDULE_URI,
                cvArray
        );

    }
}
