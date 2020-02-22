package com.kerttuli.marej.finnkinoelokuvat.intentService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.kerttuli.marej.finnkinoelokuvat.R;
import com.kerttuli.marej.finnkinoelokuvat.data.FinnkinoDbContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by martin on 3/3/17.
 */

public class FetchDates extends FetchTemplate {

    private Context mContext;

    public FetchDates(Context context) {
        mContext = context;
    }

    @Override
    URL getUrl() throws MalformedURLException {
        return new URL("https://www.finnkino.fi/xml/ScheduleDates/");
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

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                        String todaysDate = simpleDateFormat.format(calendar.getTime());
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        String tomorrowsDate = simpleDateFormat.format(calendar.getTime());

                        if (todaysDate.equals(date)) {
                            date = mContext.getString(R.string.today);
                        } else if (tomorrowsDate.equals(date)) {
                            date = mContext.getString(R.string.tomorrow);
                        } else {
                            int day = Integer.parseInt(splitArray[2]);
                            int month = Integer.parseInt(splitArray[1]);
                            int year = Integer.parseInt(splitArray[0]);

                            calendar.set(year, month - 1, day);
                            SimpleDateFormat displayDateFormat =
                                    new SimpleDateFormat("EEE,  dd.MM.yyyy", new Locale("fi", "FI"));
                            date = displayDateFormat.format(calendar.getTime());
                        }

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
