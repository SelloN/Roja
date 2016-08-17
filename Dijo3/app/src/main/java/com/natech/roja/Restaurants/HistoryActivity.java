package com.natech.roja.Restaurants;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.natech.roja.Analytics;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryRVAdapter adapter;
    List<RestaurantHistory> historyList;
    private static final int current_page = 1;
    private ProgressBar progressBar;
    private TextView noHistoryTV;
    private Boolean isHistory = false;
    private LinearLayoutManager layoutManager;
    private String userID;
    public static HistoryActivity historyActivity;
    private RelativeLayout noConnectionView, noHistoryView;
    private static final String TAG = HistoryActivity.class.getSimpleName();
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        cancelNotification();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Analytics analytics = (Analytics)getApplication();
        tracker = analytics.getDefaultTracker();
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        Toolbar toolbar =  (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        toolbarTV.setText("History");
        SharedPreferences credentialsFile = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE);
        userID = credentialsFile.getString(CommonIdentifiers.getUserId(), null);
        setRecyclerView();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noHistoryTV = (TextView)findViewById(R.id.noHistoryTV);
        noConnectionView = (RelativeLayout)findViewById(R.id.noNetwork);
        noHistoryView = (RelativeLayout)findViewById(R.id.noHistory);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        toolbarLayout.startAnimation(toolbarAnim);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if(NetworkUtil.getConnectivityStatus(HistoryActivity.this))
                    getHistory(current_page);
                else {
                    noConnectionView.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        recyclerView.addOnScrollListener(new EndlessRecyclerScroll(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMoreHistory(current_page);
            }
        });
        historyActivity = this;
    }

    private void setRecyclerView(){
        recyclerView = (RecyclerView)findViewById(R.id.historyRV);
        historyList = new ArrayList<>();
        adapter = new HistoryRVAdapter(historyList,this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onResume(){
        super.onResume();
        //Log.i(TAG,"analytics "+getClass().getSimpleName());
        tracker.setScreenName("Activity~" + getClass().getSimpleName());
        tracker.enableAdvertisingIdCollection(true);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void cancelNotification(){
        final int NOTIFICATION_ID = 1;
        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    private void getHistory(final int page){
        new AsyncTask<String, Void, String>(){

            @Override
            protected void onPreExecute(){
                if(!isHistory)
                    progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(Server.getHistory());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")
                            +"="+URLEncoder.encode(userID,"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getCurrentPage(),"UTF-8")
                            +"="+URLEncoder.encode(String.valueOf(page),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG,url.toString()+postData);
                    //Log.i(TAG,line);
                    if(line != null)
                        return line;
                    else
                        return "Error";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }
            @Override
            protected void onPostExecute(String args){
                if(!isHistory)
                    progressBar.setVisibility(View.INVISIBLE);
                   // historyList.remove(historyList.size()-1);
                if(!args.equalsIgnoreCase("limit") && !args.equalsIgnoreCase("Error")) {
                    addItems(args);
                }
                else if(!isHistory)
                    noHistoryView.setVisibility(View.VISIBLE);
                else if(args.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }

        }.execute();
    }
    private void addItems(String jsonResult){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonObject.optJSONArray("history");

            for(int i = 0; i < jsonMainNode.length();i++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(i);
                String restName = jsonChild.optString("restaurantName");
                String visitDate = jsonChild.optString("date");
                String thumbDir = jsonChild.optString("thumbPhotoDir");
                String logID = jsonChild.optString("logID");
                String restID = jsonChild.optString("restID");
                int rated = jsonChild.optInt("rated");
                historyList.add(new RestaurantHistory(restName,visitDate,thumbDir,logID,restID,rated,true));
                isHistory = true;
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            if(!isHistory)
                noHistoryTV.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    private void loadMoreHistory(int current_page){
        if(NetworkUtil.getConnectivityStatus(this))
            getHistory(current_page);
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
