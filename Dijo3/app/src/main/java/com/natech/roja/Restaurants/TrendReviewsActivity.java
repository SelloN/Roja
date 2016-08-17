package com.natech.roja.Restaurants;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.natech.roja.MenuCategories.ItemReviewRVAdapter;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
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
public class TrendReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemReviewRVAdapter adapter;
    private List<Review> reviewList;
    private LinearLayoutManager linearLayoutManager;
    private int restID;
    private ProgressBar progressBar;
    private TextView averageTV;
    private RelativeLayout noReviewsTV;
    private RatingBar ratingBar;
    private Boolean isReviews = false;
    private String restaurantName, rating;
    private Boolean isLimit = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_reviews);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        restID = getIntent().getExtras().getInt(CommonIdentifiers.getRestId());
        rating = getIntent().getExtras().getString(CommonIdentifiers.getRating());
        ratingBar = (RatingBar)findViewById(R.id.averageRating);
        noReviewsTV = (RelativeLayout)findViewById(R.id.noReviewsTV);
        ratingBar.setIsIndicator(true);

        restaurantName = getIntent().getExtras().getString("restName");
        TextView reviewsTV = (TextView)findViewById(R.id.toolbar_title);
        averageTV = (TextView)findViewById(R.id.reviewAverageTV);
        RelativeLayout noNetwork = (RelativeLayout) findViewById(R.id.noNetwork);
        reviewsTV.setText("Reviews");
        TextView restTV = (TextView)findViewById(R.id.restaurantTV);
        restTV.setText(restaurantName);
        setRecyclerView();
        setAverageHeading(rating);
        restTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openRestaurant();
            }
        });

        if(NetworkUtil.getConnectivityStatus(this)) {
            //getRating();
            int current_page = 1;
            getReviews(current_page);
        }
        else {
            noNetwork.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }

        recyclerView.addOnScrollListener(new EndlessRecyclerScroll(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page)
            {
                loadMoreReviews(current_page);
            }
        });
    }

    private void openRestaurant(){
        Intent intent = new Intent(TrendReviewsActivity.this,RestaurantAroundMeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CommonIdentifiers.getRestName(),restaurantName);
        bundle.putInt(CommonIdentifiers.getRestId(), restID);
        bundle.putString(CommonIdentifiers.getRating(),rating);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void setRecyclerView(){
        reviewList = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.RestReviewsRV);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ItemReviewRVAdapter(reviewList);
        recyclerView.setAdapter(adapter);

    }

    private void setAverageHeading(final String average){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String roundedAvg = String.format("%1$.1f",Double.parseDouble(average));
                averageTV.setText(roundedAvg);
                /*NumberFormat nf = new DecimalFormat("990.0");
                try {
                    Number avg = nf.parse(roundedAvg);
                    ratingBar.setRating(avg.floatValue());
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/

                ratingBar.setRating(Float.parseFloat(roundedAvg));
            }
        });

    }
    private void addItems(String jsonResult){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonObject.optJSONArray("reviews");

            for(int i = 0; i < jsonMainNode.length();i++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(i);
                String username = jsonChild.optString("name")+" "+jsonChild.optString("surname");
                String rating = jsonChild.optString("rating");
                String review = jsonChild.optString("review");
                String date = jsonChild.optString("date");
                reviewList.add(new Review(username,rating,date,review,restaurantName,restID));
                isReviews = true;
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            if(!isReviews)
                noReviewsTV.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    private void loadMoreReviews(int current_page){
        if(NetworkUtil.getConnectivityStatus(this)) {
            if (!isLimit)
                getReviews(current_page);
        }
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
    }
    private void getReviews(final int page){
        new AsyncTask<String, Void, String>(){
            @Override
            protected void onPreExecute(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(Server.getRestaurantReviews());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="
                            +URLEncoder.encode(String.valueOf(restID),"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getCurrentPage(),"UTF-8")+"="
                            +URLEncoder.encode(String.valueOf(page),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i("",line);
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
                progressBar.setVisibility(View.INVISIBLE);
                if(!args.equalsIgnoreCase("limit") && !args.equalsIgnoreCase("Error")) {
                    addItems(args);
                }
                else if(args.equalsIgnoreCase("limit"))
                    isLimit = true;
                else if(args.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();

                if(!isReviews)
                    noReviewsTV.setVisibility(View.VISIBLE);
            }

        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trend_reviews, menu);
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
            case R.id.open_rest:
                openRestaurant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
