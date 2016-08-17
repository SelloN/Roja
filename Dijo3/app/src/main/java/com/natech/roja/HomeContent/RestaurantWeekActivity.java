package com.natech.roja.HomeContent;

import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.squareup.picasso.Picasso;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class RestaurantWeekActivity extends AppCompatActivity {

    private WeekRVAdapter adapter;
    private List<RestaurantWeek> restaurant;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressBar progressBar;
    private ImageView header;

    private String weeklyID, storyLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_week);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        weeklyID = getIntent().getExtras().getString("weeklyID");
        Toolbar toolbar = (Toolbar)findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.myPrimaryColor));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        header = (ImageView)findViewById(R.id.header);
        setRecyclerView();
        new GetRestaurantInfo().execute();
    }

    void setRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.scrollableRestaurantWeek);
        restaurant = new ArrayList<>();
        adapter = new WeekRVAdapter(restaurant, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    private class GetRestaurantInfo extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute(){
            showProgressBar();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            try {
                url = new URL(Server.getRestaurantWeek());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode("weeklyID", "UTF-8")+"="+URLEncoder.encode(weeklyID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                //Log.i("", "New Response : " + line);

                if(line != null){
                    return line;
                }
                else
                    return "Error";

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

        }

        @Override
        protected void onPostExecute(String arg){
            if(!arg.equalsIgnoreCase("Error")){
                drawHeader(arg);
            }
            if(arg.equals("Error")) {
                hideProgressBar();
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    void drawHeader(String jsonHeader)
    {
        try{
            JSONObject jsonResponse = new JSONObject(jsonHeader);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("weeklyRestaurant");
            String restaurantName;
            String photoDir;

            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                restaurantName = jsonChildNode.optString("restaurantName");
                photoDir = jsonChildNode.optString("photoDir");
                double lat = jsonChildNode.optDouble("lat");
                double lon = jsonChildNode.optDouble("lng");
                String address = jsonChildNode.optString("address");
                storyLink = jsonChildNode.optString("storyLink");
                restaurant.add(new RestaurantWeek(address,lat,lon));
                collapsingToolbarLayout.setTitle(restaurantName);
                Picasso.with(this).load(photoDir).into(header);
            }
            new GetStory().execute();

        }catch (JSONException e){
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
            //Log.i("", e.toString());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private class GetStory extends AsyncTask<String, String, String>{

        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(storyLink);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder story = new StringBuilder();
                String line;

                // read from the urlconnection via the bufferedreader
                while ((line = bufferedReader.readLine()) != null)
                {
                    story.append(line + "\n");
                }
                bufferedReader.close();/*
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httppost = new HttpGet(
                        storyLink);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity ht = response.getEntity();
                BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                InputStream is = buf.getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder story = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    story.append(line).append("\n");
                }*/
                //Log.i("HI", "" + story.toString());

                if(story != null){
                    return story.toString();
                }
                else
                    return "Error";

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

        }

        @Override
        protected void onPostExecute(String arg){

            hideProgressBar();
            if(!arg.equalsIgnoreCase("Error")){
                restaurant.add(new RestaurantWeek(arg));
                updateAdapter();

            }else if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
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
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_restaurant_week, menu);
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
