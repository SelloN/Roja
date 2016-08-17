package com.natech.roja.Notifications;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Restaurants.HistoryActivity;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private Bitmap remote_picture = null;
    private static final String CREDENTIALS_FILE = "tableIDs",USER_ID = "userID";
    private SharedPreferences credentialsFile;
    public ReviewService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.i("Service","Service is starting");
        checkPendingReviews();
        getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                putString(CommonIdentifiers.getDate(),new SimpleDateFormat("yyyy-MM-dd").format(new Date())).commit();
        return START_STICKY;
    }

    void checkPendingReviews(){
        new Thread(new Runnable() {
            @SuppressWarnings("TryWithIdenticalCatches")
            @Override
            public void run() {
                try{

                    Looper.prepare();
                    credentialsFile = getSharedPreferences(CREDENTIALS_FILE,MODE_PRIVATE);
                    final String userID = credentialsFile.getString(USER_ID,null);
                    URL url = new URL(Server.getPendingReviews());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String results = bufferedReader.readLine();
                    bufferedReader.close();
                    //Toast.makeText(ReviewService.this, results, Toast.LENGTH_LONG).show();
                    //Log.i("Service",results);

                    if(results != null){
                        if(!results.equalsIgnoreCase("none pending"))
                            setContent(results);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }

    void setContent(String json){

        try {
            JSONObject jsonParent = new JSONObject(json);
            JSONArray jsonArray = jsonParent.optJSONArray("pendingReviews");
            for(int x = 0; x < jsonArray.length(); x++){
                JSONObject jsonChild = jsonArray.getJSONObject(x);
                String restName = jsonChild.optString("restName");
                String photoDir = jsonChild.optString("photoDir");
                sendNotification(photoDir, restName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String photoDir, String restName){

        NotificationCompat.BigPictureStyle notiStyle = new
                NotificationCompat.BigPictureStyle();
        notiStyle.setBigContentTitle("Last Visited "+restName);
        notiStyle.setSummaryText("Please review the place in your history");


        try {
            remote_picture = BitmapFactory.decodeStream(
                    (InputStream) new URL(photoDir).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        notiStyle.bigPicture(remote_picture);
        Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 1000};
        vibrator.vibrate(pattern,0);
        vibrator.cancel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,HistoryActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher4).setContentTitle("Roja").
                setStyle(notiStyle).setContentText("Please review your last visited places").setSound(soundUri);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.addAction(R.drawable.ic_history2, "Open My History", pendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
