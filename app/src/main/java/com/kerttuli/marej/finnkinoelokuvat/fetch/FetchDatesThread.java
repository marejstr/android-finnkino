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

public class FetchDatesThread extends FetchTemplateThread {

    private Context mContext;

    public FetchDatesThread (Context context) {
        mContext = context;
    }

    @Override
    URL getUrl() throws MalformedURLException {
        return new URL("http://www.finnkino.fi/xml/ScheduleDates/");
    }

    @Override
    LinkedList<ContentValues> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

        LinkedList<ContentValues> contentValuesList = new LinkedList<>();

        int eventType = parser.getEventType();
        String name;
        String date;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            ContentValues cV = new ContentValues();
            name = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    //TODO make a switch?
                    if (name.equals("dateTime")) {
                        date = parser.nextText();
                        String[] splitArray = date.split("-|T");
                        date = splitArray[2] + "." + splitArray[1] + "." + splitArray[0];
                        cV.put(FinnkinoDbContract.DateEntry.DATE_TIME, date);
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
                FinnkinoDbContract.DateEntry.DATE_URI,
                null,
                null
        );

        finnkinoContentResolver.bulkInsert(
                FinnkinoDbContract.DateEntry.DATE_URI,
                cvArray
        );

    }
}
