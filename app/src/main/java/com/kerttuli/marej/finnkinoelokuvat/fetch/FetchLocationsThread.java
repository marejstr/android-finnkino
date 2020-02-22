package com.kerttuli.marej.finnkinoelokuvat.fetch;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class FetchLocationsThread extends FetchTemplateThread {

    private Context mContext;

    public FetchLocationsThread(Context context) {
        mContext = context;
    }

    @Override
    URL getUrl() throws MalformedURLException {
        return new URL("http://www.finnkino.fi/xml/TheatreAreas/");
    }

    @Override
    LinkedList<ContentValues> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

        LinkedList<ContentValues> contentValuesList = new LinkedList<>();
        String name;
        String areaID = "";
        String areaName = "";

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            ContentValues cV = new ContentValues();
            name = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    //TODO make a switch?
                    if (name.equals("ID")) {
                        areaID = parser.nextText();
                    } else if (name.equals("Name")) {
                        areaName = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (name.equals("TheatreArea")) {
                        cV.put(FinnkinoDbContract.LocationEntry.FINNKINO_LOCATION_ID, areaID);
                        cV.put(FinnkinoDbContract.LocationEntry.LOCATION_NAME, areaName);
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
                FinnkinoDbContract.LocationEntry.LOCATIONS_URI,
                null,
                null
        );

        finnkinoContentResolver.bulkInsert(
                FinnkinoDbContract.LocationEntry.LOCATIONS_URI,
                cvArray
        );
    }
}
