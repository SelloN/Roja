package com.natech.roja.CloudServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Sello on 2015/07/11.
 */
@SuppressWarnings("DefaultFileTemplate")
public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, intent.getExtras().getString("message"),
                Toast.LENGTH_LONG).show();
    }
}
