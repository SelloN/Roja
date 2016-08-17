package com.natech.roja.Restaurants;

import android.os.Build;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.natech.roja.Analytics;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    private Tracker tracker;
    private ArrayList<Restaurant> restaurants;
    private RestaurantsRVAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RelativeLayout noFavourites;
    public static FavouriteActivity favouriteActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
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
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        toolbarTV.setText("My Favourites");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRecyclerView();
        RelativeLayout noNetwork = (RelativeLayout) findViewById(R.id.noNetwork);
        noFavourites = (RelativeLayout)findViewById(R.id.noFavourites);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        loadPlaces();
        favouriteActivity = this;


    }
    void setRecyclerView(){
        restaurants = new ArrayList<>();
        adapter = new RestaurantsRVAdapter(restaurants,this,"favourite");
        recyclerView = (RecyclerView) findViewById(R.id.restaurantsRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    void loadPlaces(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressBar();
                try {
                    URL url = new URL(Server.getFavouriteRestaurant());
                    //URL url = new URL(Server.getTestURL());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("get","UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                            getString(CommonIdentifiers.getUserId(), null),"UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    Log.i("", "Response " + line);

                    if(line != null){
                        if(progressBar != null)
                            hideProgressBar();
                        if(!line.equalsIgnoreCase("limit"))
                            handlePlaces(line);
                    }else {
                        hideProgressBar();
                    }
                } catch (MalformedURLException e) {
                    hideProgressBar();
                    e.printStackTrace();
                } catch (IOException e) {
                    hideProgressBar();
                    e.printStackTrace();
                }


            }
        }).start();
    }

    void handlePlaces(String json){

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonMainNode = jsonObject.optJSONArray("places");

            for(int x = 0; x < jsonMainNode.length(); x++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(x);
                String restName = jsonChild.optString("restaurantName");
                String rating;
                if(!jsonChild.optString("rating").equalsIgnoreCase("null"))
                    rating = jsonChild.optString("rating");
                else
                    rating = "0";

                String restID = jsonChild.optString("restaurantID");
                String thumbDir = jsonChild.optString("thumbPhotoDir");
                restaurants.add(new Restaurant(restName,rating,restID,thumbDir,null,0));
            }
            updateAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
            showNoRestaurants();
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

    void showNoRestaurants(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noFavourites.setVisibility(View.VISIBLE);
            }
        });
    }

    void showProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    void hideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void sendHit(String restID, String restName){
        tracker.send(new HitBuilders.EventBuilder().setCategory("Favourite Hit").
                setLabel(restID).setAction(restName).build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_favourite, menu);
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
