package com.natech.roja.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            //---Alarmmanger bug. Resolve big first-----
            if(context.getSharedPreferences(AppSharedPreferences.getSettings(),Context.MODE_PRIVATE).
                    getBoolean(CommonIdentifiers.getNotifyActive(), false)
                    && context.getSharedPreferences(AppSharedPreferences.getSettings(),Context.MODE_PRIVATE).
                    getBoolean(CommonIdentifiers.getReviewNotifications(), false)
                    && context.getSharedPreferences(AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).
                    getBoolean(AppSharedPreferences.getLogState(), false) )
            {
                PendingIntent reviewsPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ReviewReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

                Calendar cur_cal = new GregorianCalendar();
                cur_cal.setTimeInMillis(System.currentTimeMillis());
                Random random = new Random();
                int low = 7;
                int high = 22;
                int hour = random.nextInt(high - low) + low;
                int minute = random.nextInt(60 - 1) + 1;
                Calendar cal = new GregorianCalendar();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE,minute);
                cal.set(Calendar.SECOND,5);
                cal.set(Calendar.MILLISECOND,0);
                //cal.setTimeInMillis(System.currentTimeMillis());
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,reviewsPendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+AlarmManager.INTERVAL_DAY , AlarmManager.INTERVAL_DAY, reviewsPendingIntent);
                Log.i("Boot", "Alarm reinstated after booting");
            }

            //---16 October 2015-----
        }
    }
}
