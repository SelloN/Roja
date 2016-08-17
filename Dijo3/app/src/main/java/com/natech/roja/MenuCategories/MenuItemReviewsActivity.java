package com.natech.roja.MenuCategories;

import android.annotation.TargetApi;
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


import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Restaurants.EndlessRecyclerScroll;
import com.natech.roja.Restaurants.Review;
import com.natech.roja.Utilities.CommonIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class MenuItemReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemReviewRVAdapter adapter;
    private List<Review> reviewList;
    private LinearLayoutManager linearLayoutManager;
    private int menuID;
    private ProgressBar progressBar;
    private TextView averageTV;
    private RelativeLayout noReviewsTV;
    private RatingBar ratingBar;
    private Boolean isReviews = false;
    private Boolean isLimit = false;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_reviews);
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
        menuID = getIntent().getExtras().getInt(CommonIdentifiers.getMenuId());
        ratingBar = (RatingBar)findViewById(R.id.averageRating);
        noReviewsTV = (RelativeLayout)findViewById(R.id.noReviewsTV);
        ratingBar.setIsIndicator(true);
        String menuItem = getIntent().getExtras().getString("menuItem");
        TextView reviewsTV = (TextView)findViewById(R.id.toolbar_title);
        averageTV = (TextView)findViewById(R.id.reviewAverageTV);
        reviewsTV.setText("Reviews");
        TextView menuItemReview = (TextView)findViewById(R.id.menuItemTV);
        menuItemReview.setText(menuItem);
        setRecyclerView();
        //RelativeLayout averageHolder = (RelativeLayout)findViewById(R.id.averageHolder);
        //final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        //toolbar.startAnimation(toolbarAnim);
        //averageHolder.startAnimation(toolbarAnim);


        RelativeLayout noNetwork = (RelativeLayout) findViewById(R.id.noNetwork);

        if(NetworkUtil.getConnectivityStatus(this)) {
            getRating();
            int current_page = 1;
            getReviews(current_page);
        }else {
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

    void setRecyclerView(){
        reviewList = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.reviewRV);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ItemReviewRVAdapter(reviewList);
        recyclerView.setAdapter(adapter);

    }

    void getRating(){
        new AsyncTask<String, Void, String>(){

            @Override
            protected String doInBackground(String... strings) {

                URL url;
                try {
                    url = new URL(Server.getReviewAverages());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("menu","UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getMenuId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(menuID),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i("", "New Response : " + line);
                    if(line != null){

                        setAverageHeading(line);
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
                if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    void setAverageHeading(final String average){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String roundedAvg = String.format("%1$,.1f", Double.parseDouble(average));
                    averageTV.setText(roundedAvg);
                    NumberFormat nf = new DecimalFormat("990.0");
                    try {
                        Number avg = nf.parse(roundedAvg);
                        ratingBar.setRating(avg.floatValue());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //ratingBar.setRating(Float.parseFloat(roundedAvg));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Something Went Wrong. Please Try Again",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    void addItems(String jsonResult){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonObject.optJSONArray("reviews");

            for(int i = 0; i < jsonMainNode.length();i++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(i);
                String username = jsonChild.optString("name")+" "+jsonChild.optString("surname");
                String rating = jsonChild.optString("rating");
                String review = jsonChild.optString("review");
                String date = jsonChild.optString("date");
                reviewList.add(new Review(username,rating,date,review,null,0));
                isReviews = true;
            }

                adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            //Log.i("","No Reviews");
            if(!isReviews)
                noReviewsTV.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    void loadMoreReviews(int current_page){
        if(NetworkUtil.getConnectivityStatus(this))
            if(!isLimit)
                getReviews(current_page);
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
    }
    void getReviews(final int page){

        new AsyncTask<String, Void, String>(){

            @Override
            protected void onPreExecute(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(Server.getMenuReviews());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getMenuId(),"UTF-8")+"="
                            +URLEncoder.encode(String.valueOf(menuID),"UTF-8");
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
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again",
                            Snackbar.LENGTH_LONG).show();
                
                if(!isReviews)
                    noReviewsTV.setVisibility(View.VISIBLE);


            }

        }.execute(null,null,null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_menu_item_reviews, menu);
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
