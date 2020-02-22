package com.kerttuli.marej.finnkinoelokuvat.intentService;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class MainIntentService extends IntentService {
    private static final String ACTION_FETCH_SCHEDULE =
            "com.kerttuli.marej.finnkinoelokuvat.intentService.fetch.schedule";
    private static final String ACTION_FETCH_LOCATIONS =
            "com.kerttuli.marej.finnkinoelokuvat.intentService.fetch.locations";
    private static final String ACTION_FETCH_DATES =
            "com.kerttuli.marej.finnkinoelokuvat.intentService.fetch.dates";

    private static final String EXTRA_URL =
            "com.kerttuli.marej.finnkinoelokuvat.intentService.extra.url";

    public MainIntentService() {
        super("MainIntentService");
    }

    public static void fetchSchedule(Context context, String url) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_FETCH_SCHEDULE);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }

    public static void fetchLocations(Context context) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_FETCH_LOCATIONS);
        context.startService(intent);
    }

    public static void fetchDates(Context context) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_FETCH_DATES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_FETCH_SCHEDULE:
                    final String url = intent.getStringExtra(EXTRA_URL);
                    FetchSchedule fetchSchedule = new FetchSchedule(url, getApplicationContext());
                    fetchSchedule.fetch();
                    break;
                case ACTION_FETCH_LOCATIONS:
                    FetchLocations fetchLocations = new FetchLocations(getApplicationContext());
                    fetchLocations.fetch();
                    break;
                case ACTION_FETCH_DATES:
                    FetchDates fetchDates = new FetchDates(getApplicationContext());
                    fetchDates.fetch();
                    break;
                default:
                    throw new RuntimeException("Action not implemented " + action);
            }
        }
    }
}
