package com.natech.roja.CloudServices;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.natech.roja.R;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";
    private static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

        //Log.d("Wakeful", "Receiving");
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);
        String msg = null;

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    //sendNotification("Send error: " + extras.toString(), null, null);
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                   // sendNotification("Deleted messages on server: "
                     //       + extras.toString(), null, null);
                    // If it's a regular GCM message, do some work.
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    sendNotification(extras.getString("tableNo"),extras.getString("logID"));
                    Log.i(TAG, "Received: " + extras.toString());
                    //Log.i(TAG, "Message: " + extras.getString(message));
                    //Log.i(TAG, "Received: " + extras.toString());

                    break;
            }
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		//GcmBroadcastReceiver.completeWakefulIntent(intent);
        /*Intent i = new Intent();
        i.setAction("com.natech.dijo");
        i.putExtra("message",msg);
        sendBroadcast(i);*/

		
		
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	@SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    private void sendNotification(String tableNo, String logID) {

        Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 1000};
        vibrator.vibrate(pattern,0);
        vibrator.cancel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                new Intent(this,SplashOrder.class).putExtra("tableNo",tableNo).putExtra("logID",logID),PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher2).setContentTitle("Dijo").
                setStyle(new NotificationCompat.BigTextStyle().bigText("Your Order Is Ready")).
                setContentText("You may come collect your order").setSound(soundUri);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}