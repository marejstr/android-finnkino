package com.kerttuli.marej.finnkinoelokuvat.intentService;

import android.content.ContentValues;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

abstract class FetchTemplate {

    abstract URL getUrl() throws MalformedURLException;

    abstract LinkedList<ContentValues> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException;

    abstract void DeleteAndBulkInsert(ContentValues[] cvArray);

    public void fetch() {
        URL url = null;
        try {
            url = getUrl();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //fetch XML from Finnkino
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        LinkedList<ContentValues> contentValuesList = new LinkedList<>();

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(in, null);

            // parse xml
            contentValuesList = parseXML(parser);

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

        ContentValues[] cvArray = contentValuesList.toArray(
                new ContentValues[contentValuesList.size()]);

        DeleteAndBulkInsert(cvArray);

    }
}
