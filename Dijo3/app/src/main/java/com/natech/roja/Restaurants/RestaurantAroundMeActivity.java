package com.natech.roja.Restaurants;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.natech.roja.Analytics;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.OffsiteOrders.OffSiteMenuActivity;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class RestaurantAroundMeActivity extends AppCompatActivity {
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RestaurantInfoAdapter adapter;
    private List<Restaurant> restaurantList;
    @SuppressWarnings("FieldCanBeLocal")
    private String restName, rating, phone;
    private int restID, offSiteCode;
    private ImageView header;
    final private static String TAG = RestaurantAroundMeActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private Boolean isLoaded = false, isLoaded2 = false, isFavourited = false, homeDelivery = false;
    private String openTime,closeTime;
    private int currentDay;
    private Tracker tracker;
    private String loyaltySystem;
    private com.melnykov.fab.FloatingActionButton favFab;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_around_me);
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
        restID = getIntent().getExtras().getInt(CommonIdentifiers.getRestId());
        restName = getIntent().getExtras().getString(CommonIdentifiers.getRestName());
        rating = getIntent().getExtras().getString(CommonIdentifiers.getRating());
        Toolbar toolbar = (Toolbar)findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.myPrimaryColor));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        header = (ImageView)findViewById(R.id.header);
        RelativeLayout noConnectionView = (RelativeLayout) findViewById(R.id.noNetwork);
        setRecyclerView();

        if(NetworkUtil.getConnectivityStatus(this)) {
            new GetRestaurantHeader().execute();
            new GetRestaurantDetails().execute();
            checkFavouriteRestaurant();
        }
        else {
            noConnectionView.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition),
                    "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.callFAB);
        favFab = (com.melnykov.fab.FloatingActionButton)findViewById(R.id.favouriteFB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* if(isLoaded) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }else
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Please Wait", Snackbar.LENGTH_LONG).show();*/
                //Activate this code when you enable offline ordering for restaurants, but first change the
                //floating action button icon to a shopping cart

                if(isLoaded) {
                    if(offSiteCode != 0) {
                        getTime:for(int x = 0; x < restaurantList.get(5).getTradingHours().size(); x++){
                            //Log.i(TAG, "==> "+restaurantList.get(5).getTradingHours().get(x).getDayIndex());
                            if(currentDay == restaurantList.get(5).getTradingHours().get(x).getDayIndex())
                            {
                                openTime = restaurantList.get(5).getTradingHours().get(x).getTime().substring(0,5);
                                closeTime = restaurantList.get(5).getTradingHours().get(x).getTime().substring(8,13);
                                break getTime;
                            }

                        }

                        if(NetworkUtil.getConnectivityStatus(RestaurantAroundMeActivity.this))
                            new CheckTradingHours().execute();
                        else
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "No Internet Connection", Snackbar.LENGTH_LONG).show();


                    }else
                        Snackbar.make(findViewById(R.id.snackbarPosition),
                                "This Restaurant Does Not Take Orders", Snackbar.LENGTH_LONG).show();
                }else{
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Please Wait", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        favFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLoaded2) {
                    if(isFavourited)
                        favouriteRestaurant("unfav");
                    else
                        favouriteRestaurant("fav");
                }
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Please Wait", Snackbar.LENGTH_LONG).show();

            }
        });

        final String TUTORIAL = "LearnedRestaurantActivity";
        if(!getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
            final ShowcaseView showcaseView = new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.callFAB, this))
                    .hideOnTouchOutside().
                            setContentTitle("Place Orders").setStyle(R.style.CustomShowcaseTheme3)
                    .setContentText("Touch this button to open the restaurant's menu and place orders").
                            hideOnTouchOutside().build();
            showcaseView.setButtonText("Got It!");
            showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {

                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                            putBoolean(TUTORIAL,true).apply();
                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                }
            });
        }
    }

    private void favouriteRestaurant(final String type){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Server.getFavouriteRestaurant());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+
                            URLEncoder.encode(String.valueOf(restID),"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                                    getString(CommonIdentifiers.getUserId(),null),"UTF-8");
                    if(type.equalsIgnoreCase("fav"))
                        postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("favourite","UTF-8");
                    else if(type.equalsIgnoreCase("unfav"))
                        postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("unfavourite","UTF-8");

                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    Log.i("", "New Response : " + line);

                    if(line != null) {
                        if(line.equalsIgnoreCase("successful") && type.equalsIgnoreCase("fav")) {
                            updateFavouriteFAB("fav");
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "Favourited", Snackbar.LENGTH_LONG).show();
                            isFavourited = true;
                        }else if(line.equalsIgnoreCase("successful") && type.equalsIgnoreCase("unfav")) {
                            updateFavouriteFAB("unfav");
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "Unfavourited", Snackbar.LENGTH_LONG).show();
                            isFavourited = false;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    private void updateFavouriteFAB(final String type){
        if(type.equalsIgnoreCase("fav")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //favFab.setImageDrawable(R.drawable.ic_favourite_white);
                    favFab.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.ic_favourite_white));
                }
            });
        }else if(type.equalsIgnoreCase("unfav")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //favFab.setBackgroundResource(R.drawable.ic_favourite_line);
                    favFab.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.ic_favourite_line));
                }
            });
        }
    }

    private void checkFavouriteRestaurant(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Server.getFavouriteRestaurant());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+
                            URLEncoder.encode(String.valueOf(restID),"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                                    getString(CommonIdentifiers.getUserId(),null),"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("check","UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    Log.i("", "New Response : " + line);

                    if(line != null) {
                        if(line.equalsIgnoreCase("favourited")) {
                            updateFavouriteFAB("fav");
                            //favFab.setBackgroundResource(R.drawable.ic_favourite_white);
                            isFavourited = true;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                isLoaded2 = true;
            }

        }).start();
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
        Log.i(TAG,"analytics "+getClass().getSimpleName());
        tracker.setScreenName("Activity~" + getClass().getSimpleName());
        tracker.setPage("RestID~ "+restID);
        tracker.enableAdvertisingIdCollection(true);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        tracker.send(new HitBuilders.EventBuilder().setCategory("Restaurant Hit").setLabel(String.valueOf(restID)).
                setAction(restName).build());
    }
    private void setRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.scrollableRestaurant);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        restaurantList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        adapter = new RestaurantInfoAdapter(this, restaurantList);
        recyclerView.setAdapter(adapter);
    }
    private void showProgressDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(RestaurantAroundMeActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    private void hideProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

    private void openRestaurant(){
        Intent intent = new Intent(RestaurantAroundMeActivity.this, OffSiteMenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("offSiteCode",String.valueOf(offSiteCode));
        bundle.putString(CommonIdentifiers.getRestName(), restName);
        bundle.putString(CommonIdentifiers.getRestId(), String.valueOf(restID));
        bundle.putString("restEmail",restaurantList.get(1).getEmail());
        bundle.putString(CommonIdentifiers.getLoyaltySystem(),loyaltySystem);
        bundle.putBoolean(CommonIdentifiers.getHomeDelivery(),homeDelivery);
        //Log.i(TAG,"home==> "+homeDelivery);
        /*if(restaurantList.get(0).getIsFranchise()) {
            bundle.putBoolean("isFranchise",true);
            bundle.putString("franchiseID", restaurantList.get(0).getFranchiseID());
        }else
            bundle.putBoolean("isFranchise",false);*/
        bundle.putString(CommonIdentifiers.getFranchId(), restaurantList.get(0).getFranchiseID());
        intent.putExtras(bundle);
        startActivity(intent);
        //Log.i(TAG,"opening rest");
    }



    class CheckTradingHours extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getCheckTradingHours());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode("openTime","UTF-8")+"="+URLEncoder.encode(openTime,"UTF-8");
                postData += "&"+URLEncoder.encode("closeTime","UTF-8")+"="+URLEncoder.encode(closeTime,"UTF-8");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);

                if(line != null) {
                    if(line.equalsIgnoreCase("open"))
                        return "open";
                    else if(line.equalsIgnoreCase("closed"))
                        return "closed";
                    else
                        return "Error";
                }else
                    return "Error";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
        }
        @Override
        protected void onPostExecute(String arg){
            hideProgressDialog();
            if(arg.equalsIgnoreCase("open")){
                openRestaurant();
            }else if(arg.equalsIgnoreCase("closed")){
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Restaurant Closed", Snackbar.LENGTH_LONG).show();

            }else if(arg.equalsIgnoreCase("error")){
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();

            }
        }
    }
    private class GetRestaurantHeader extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(Server.getRestaurantHeaderDetails());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(restID),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("review","UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line =  reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                if(line != null)
                    return line;
                else
                    return "Error";

            }catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
        }
        @Override
        protected void onPostExecute(String arg)
        {
            if(!arg.equalsIgnoreCase("Error"))
                drawHeader(arg);
            else if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }
    private void drawHeader(String jsonHeader)
    {
        try{
            JSONObject jsonResponse = new JSONObject(jsonHeader);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("restaurant");
            String restaurantName;
            String photoDir;
            for(int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                restaurantName = jsonChildNode.optString("restaurantName");
                photoDir = jsonChildNode.optString("photoDir");
                collapsingToolbarLayout.setTitle(restaurantName);
                Picasso.with(this).load(photoDir).into(header);
            }


        }catch (JSONException e){
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
            //Log.i("", e.toString());
        }
    }

    private class GetRestaurantDetails extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection;
            try {
                URL url = new URL(Server.getRestaurantDetails());
                connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(restID),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("restaurant","UTF-8");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                //Log.i(TAG,"+++ "+connection.getResponseCode());
                //Log.i(TAG,"+++ "+connection.getResponseMessage());
                return line;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String arg){
            progressBar.setVisibility(View.GONE);
            if(!arg.equalsIgnoreCase("Error")){
                buildUI(arg);

            }else if(arg.equalsIgnoreCase("Error")){
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();

            }
        }
    }

    private void buildUI(String jsonResult){
        List<TradingHours> tradingHoursList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject jsonDay = jsonObject.getJSONObject("day");
            currentDay = jsonDay.optInt("currentDay");
            //Log.i(TAG, "++Day++ "+ currentDay);
            JSONArray jsonArray = jsonObject.optJSONArray("restInfo");
            for(int x = 0; x < jsonArray.length(); x++){
                JSONObject jsonChild = jsonArray.getJSONObject(x);
                String description = jsonChild.optString("description");
                String address = jsonChild.optString("address");
                double lat = jsonChild.optDouble("lat");
                double lon = jsonChild.optDouble("lng");
                int tradingDay = jsonChild.optInt("day");
                String email = jsonChild.optString("email");
                phone = jsonChild.optString("telephone");
                int franchiseID = jsonChild.optInt("franchiseID");
                offSiteCode = jsonChild.optInt("offSiteCode");
                loyaltySystem = jsonChild.optString("loyalty");
                int isHalal = jsonChild.optInt("halal");
                int isAlcohol = jsonChild.optInt("alcohol");
                int isDelivery = jsonChild.optInt("delivery");
                if(isDelivery == 1)
                    homeDelivery = true;
                int isWifi = jsonChild.optInt("wifi");
                int packageCode = jsonChild.optInt("package");
                isLoaded = true;
                String tradingTime;
                if(!jsonChild.optString("openTime").substring(0,5).equalsIgnoreCase("55:55"))
                    tradingTime = jsonChild.optString("openTime").substring(0,5)+" - "+jsonChild.optString("closeTime").substring(0,5);
                else
                    tradingTime = "Closed";

                if(x == 0) {
                    Restaurant restaurantRating = new Restaurant();
                    restaurantRating.setRestaurantName(restName);
                    restaurantRating.setRestID(String.valueOf(restID));
                    restaurantRating.setFranchiseID(String.valueOf(franchiseID));
                    restaurantRating.setShowReviews(packageCode);
                    /*if(franchiseID != 0){

                        restaurantRating.setIsFranchise(true);
                    }*/
                    restaurantRating.setRating(rating);
                    restaurantRating.setIsRating(true);
                    restaurantList.add(restaurantRating);


                    Restaurant restaurantDesc = new Restaurant();
                    restaurantDesc.setDescription(description);
                    restaurantDesc.setIsAboutUs(true);
                    restaurantList.add(restaurantDesc);

                    Restaurant restaurantDirection = new Restaurant();
                    restaurantDirection.setDirections(address);
                    restaurantDirection.setRestaurantLabel(restName.charAt(0));
                    restaurantDirection.setLat(lat);
                    restaurantDirection.setLon(lon);
                    restaurantDirection.setIsDirection(true);
                    restaurantList.add(restaurantDirection);

                    Restaurant restaurantContact = new Restaurant();
                    restaurantContact.setEmail(email);
                    restaurantContact.setPhone(phone);
                    restaurantContact.setIsContact(true);
                    restaurantList.add(restaurantContact);

                    Restaurant restaurantFeatures =  new Restaurant();
                    restaurantFeatures.setIsAlcohol(isAlcohol);
                    restaurantFeatures.setIsHalal(isHalal);
                    restaurantFeatures.setIsDelivery(isDelivery);
                    restaurantFeatures.setIsWifi(isWifi);
                    restaurantFeatures.setIsFeatures(true);
                    restaurantList.add(restaurantFeatures);
                }

               tradingHoursList.add(new TradingHours(tradingDay,tradingTime));



            }
            Restaurant restaurantTrading = new Restaurant();
            restaurantTrading.setTradingHours(tradingHoursList);
            restaurantTrading.setCurrentDay(currentDay);
            restaurantTrading.setIsTradingHours(true);
            restaurantList.add(restaurantTrading);

            updateAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateAdapter(){
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
        //getMenuInflater().inflate(R.menu.menu_restaurant_around_me, menu);
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
