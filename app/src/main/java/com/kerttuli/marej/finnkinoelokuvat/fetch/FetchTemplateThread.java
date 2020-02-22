package com.kerttuli.marej.finnkinoelokuvat.fetch;

import android.content.ContentValues;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

abstract class FetchTemplateThread implements Runnable{

    abstract URL getUrl() throws MalformedURLException;

    abstract LinkedList<ContentValues> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException;

    abstract void DeleteAndBulkInsert(ContentValues[] cvArray);

    @Override
    public void run() {
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
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

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
