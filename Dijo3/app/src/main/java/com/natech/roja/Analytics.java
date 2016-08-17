package com.natech.roja;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Tshepo on 2015/10/29.
 */
public class Analytics extends Application {

    private Tracker tracker;

    synchronized public Tracker getDefaultTracker(){
        if(tracker == null){
            GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);
            tracker = googleAnalytics.newTracker(R.xml.global_tracker);
            /*
            googleAnalytics.setLocalDispatchPeriod(1800);
            tracker = googleAnalytics.newTracker("UA-69437953-1");
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);*/
        }

        return tracker;
    }
}
