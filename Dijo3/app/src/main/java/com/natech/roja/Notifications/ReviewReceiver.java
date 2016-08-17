package com.natech.roja.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReviewReceiver extends BroadcastReceiver {
    public ReviewReceiver() {
        Log.i("","Receiver set");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Toast.makeText(context, "Yay, I have been fired!", Toast.LENGTH_LONG).show();
        //if(!context.getSharedPreferences(AppSharedPreferences.getSettings(),Context.MODE_PRIVATE).
        //
        //        getString(CommonIdentifiers.getDate(),null).equalsIgnoreCase(new SimpleDateFormat("yyyy-MM-dd").format(new Date())))
            context.startService(new Intent(context,ReviewService.class));
    }
}
