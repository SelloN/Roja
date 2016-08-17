package com.natech.roja.Restaurants;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.natech.roja.Analytics;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class TrendsActivity extends AppCompatActivity {

    private TrendsRVAdapter adapter;
    private List<Restaurant> restaurantList;
    private RelativeLayout noNetwork;
    private ProgressBar progressBar;
    private Tracker tracker;
    public static TrendsActivity trendsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);
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
        Toolbar toolbar = (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        TextView toolBarTitle = (TextView)findViewById(R.id.toolbar_title);
        noNetwork = (RelativeLayout)findViewById(R.id.noNetwork);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        toolBarTitle.setText("Trending Restaurants");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setRecyclerView();
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        toolbarLayout.startAnimation(toolbarAnim);
        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(NetworkUtil.getConnectivityStatus(TrendsActivity.this))
                    new GetTrends().execute();
                else {
                    noNetwork.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        trendsActivity = this;
    }
    @Override
    public void onStart(){
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        tracker.setScreenName("Activity~" + getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendHit(String restID,String restName){
        tracker.send(new HitBuilders.EventBuilder().setCategory("Trending Restaurant Hit").
                setLabel(restID).setAction(restName).build());
    }

    void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.trendsRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        restaurantList = new ArrayList<>();
        adapter = new TrendsRVAdapter(restaurantList,this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private class GetTrends extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String...args) {
            URL url;
            try {
                url = new URL(Server.getTrends());
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                if(line != null){
                    loadTrends(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "Error";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String arg){
            progressBar.setVisibility(View.GONE);
            if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    void loadTrends(String json){
        try{
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("trends");
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String restaurantName = jsonChildNode.optString("restaurantName");
                double rating = jsonChildNode.optDouble("rating");
                String restID = jsonChildNode.optString("restID");
                String rateCount = jsonChildNode.optString("count");
                String thumbDir = jsonChildNode.optString("thumbPhotoDir");

                restaurantList.add(new Restaurant(restaurantName,String.valueOf(rating),restID,thumbDir,rateCount,0));
            }
            updateAdapter();
        }catch (JSONException e){
            Log.i("", e.toString());
        }
    }

    void updateAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_trends, menu);
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
