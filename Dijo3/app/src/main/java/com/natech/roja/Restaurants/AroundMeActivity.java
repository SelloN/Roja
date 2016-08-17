package com.natech.roja.Restaurants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"ALL", "ConstantConditions"})
public class AroundMeActivity extends AppCompatActivity {

    private GoogleApiClient apiClient;
    private static final String TAG = AroundMeActivity.class.getSimpleName();
    private LocationRequest locationRequest;
    private Location lastLocation;
    private List<Address> addresses;
    private List<Restaurant> restaurants;
    private TextView currentPlaceTV, noRestaurantsTV;
    private RelativeLayout noGpsLayout;
    private RestaurantsRVAdapter adapter;
    private ProgressBar progressBar;
    private Handler locationHandler;
    private Boolean isConnectedFirst = false;
    private SharedPreferences.Editor credentialsEditor;
    private SharedPreferences credentialsFile;
    private Dialog settingsDialog;
    private static final int GPS_REQUEST_CODE = 1;
    private boolean fromGPSsettings = false;
    private Tracker tracker;
    public static AroundMeActivity aroundMeActivity;
    private static final int current_page = 1;
    private RecyclerView recyclerView;
    private String lat, lon;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around_me);
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
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        //LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        toolbarTitle.setText("Around Me");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        credentialsFile = getSharedPreferences(AppSharedPreferences.getCredentialsFile(), MODE_PRIVATE);
        credentialsEditor = credentialsFile.edit();
        setRecyclerView();
        /*
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final boolean locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);*/

        //final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        //toolbarLayout.startAnimation(toolbarAnim);

        currentPlaceTV = (TextView)findViewById(R.id.currentPlaceTV);
        noRestaurantsTV = (TextView)findViewById(R.id.noRestaurantsTV);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noGpsLayout = (RelativeLayout)findViewById(R.id.noGPS);
        RelativeLayout noNetwork = (RelativeLayout) findViewById(R.id.noNetwork);
        ImageButton getLocationBtn = (ImageButton)findViewById(R.id.getLocationBtn);
        aroundMeActivity = this;
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //refreshLocation();

                if(GPSstatus()) {
                    hideNoRestaurants();
                    setCurrentPlaceTV("Detecting Location...");
                    restaurants.clear();
                    updateAdapter();
                    showProgressBar();
                    new GetLocation();
                }else
                    openSettings();
            }
        });
        currentPlaceTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GPSstatus()) {
                    hideNoRestaurants();
                    setCurrentPlaceTV("Detecting Location...");
                    restaurants.clear();
                    updateAdapter();
                    showProgressBar();
                    new GetLocation();
                }else
                    openSettings();
            }
        });

        Boolean isLocationStored = false;
        if(!credentialsFile.contains(CommonIdentifiers.getLocale()) && GPSstatus()) {
            new GetLocation();
            isLocationStored = false;
        }else if(!credentialsFile.contains(CommonIdentifiers.getLocale()) && !GPSstatus()){
            showGPSDialog();
        }
        else {
            isLocationStored = true;
            getStoredLocation();
        }

        final String TUTORIAL = "LearnedAroundMeActivity";
        if(!getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
            final ShowcaseView showcaseView = new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.getLocationBtn, this))
                    .hideOnTouchOutside().
                    setContentTitle("Detect Location").setStyle(R.style.CustomShowcaseTheme3)
                    .setContentText("Touch this button to use your phone's GPS to detect your current" +
                            " location and retrieve the restaurants using Dijo around you").
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
        recyclerView.addOnScrollListener(new EndlessRecyclerScroll(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMorePlaces(current_page);
            }
        });



    }
    void showGPSDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("Location Detection");
        builder.setMessage("Please turn on your GPS to view restaurants around you");

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressBar.setVisibility(View.INVISIBLE);
                showNoRestaurants();
            }
        });
        builder.setPositiveButton("Turn On GPS",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openSettings();
            }
        });
        builder.show();
    }
    private void loadMorePlaces(int current_page){
        if(NetworkUtil.getConnectivityStatus(this))
            loadPlaces(lat, lon, current_page);
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
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


    void openSettings(){
        fromGPSsettings = true;
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST_CODE);
    }

    private boolean GPSstatus(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    void setRecyclerView(){
        restaurants = new ArrayList<>();
        adapter = new RestaurantsRVAdapter(restaurants,this,"around");
        recyclerView = (RecyclerView) findViewById(R.id.restaurantsRV);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
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
                noRestaurantsTV.setVisibility(View.VISIBLE);
            }
        });
    }

    void hideNoRestaurants(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noRestaurantsTV.setVisibility(View.GONE);
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

    void showNoLocation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurrentPlaceTV("Tap To Retry");
                noGpsLayout.setVisibility(View.VISIBLE);
            }
        });
    }
    void hideNoLocation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noGpsLayout.setVisibility(View.GONE);
            }
        });
    }

    void showSettingsDialog(){
        settingsDialog = new Dialog(this);
        settingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(R.layout.clear_cart_dialog);
        settingsDialog.setCanceledOnTouchOutside(true);
        TextView messageTV = (TextView)settingsDialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Your GPS is turned off, would you like to turn it on?");
        Button yesBtn = (Button)settingsDialog.findViewById(R.id.yesClear);
        Button noBtn = (Button)settingsDialog.findViewById(R.id.noClear);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });
    }


    void loadPlaces(final String lat, final String lon,final int page){
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressBar();
                try {
                    URL url = new URL(Server.getPlaces());
                    //URL url = new URL(Server.getTestURL());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("lat", "UTF-8")+"="+URLEncoder.encode(lat,"UTF-8");
                    postData += "&"+URLEncoder.encode("lon","UTF-8")+"="+URLEncoder.encode(lon,"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getCurrentPage(),"UTF-8")
                            +"="+URLEncoder.encode(String.valueOf(page),"UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG, "Response " + line);

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

    void getStoredLocation(){
        double lt = (double) credentialsFile.getFloat(CommonIdentifiers.getLat(), 0);
        double ln = (double) credentialsFile.getFloat(CommonIdentifiers.getLon(), 0);
        String locality = credentialsFile.getString(CommonIdentifiers.getLocale(),null);

        setCurrentPlaceTV(locality);
        lat = String.valueOf(lt);
        lon = String.valueOf(ln);
        loadPlaces(lat, lon,current_page);

    }

    @SuppressWarnings("PointlessArithmeticExpression")
    class GetLocation implements GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks , LocationListener {

        public GetLocation(){

            buildGoogleApiClient();
        }

        synchronized void buildGoogleApiClient() {
            apiClient = new GoogleApiClient.Builder(AroundMeActivity.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            apiClient.connect();
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000);

        }

        @Override
        public void onConnected(Bundle bundle) {


            if(!isConnectedFirst) {
                showProgressBar();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            locationHandler = new Handler();
                            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, GetLocation.this);
                            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    apiClient);
                            final Geocoder geoCoder = new Geocoder(AroundMeActivity.this, Locale.getDefault());

                            if (lastLocation != null) {
                                addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                setCurrentPlaceTV(addresses.get(0).getLocality());
                                credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                                credentialsEditor.putFloat(CommonIdentifiers.getLon(), (float) lastLocation.getLongitude());
                                credentialsEditor.putString(CommonIdentifiers.getLocale(), addresses.get(0).getLocality());
                                credentialsEditor.commit();
                                hideProgressBar();
                                hideNoLocation();
                                lat = String.valueOf(lastLocation.getLatitude());
                                lon = String.valueOf(lastLocation.getLongitude());
                                loadPlaces(lat, lon, current_page);
                            } else {
                                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, GetLocation.this);
                                lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                        apiClient);

                                if (lastLocation != null) {
                                    //Log.i(TAG, "Location Found");
                                    addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                    setCurrentPlaceTV(addresses.get(0).getLocality());
                                    credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                                    credentialsEditor.putFloat(CommonIdentifiers.getLon(),(float) lastLocation.getLongitude());
                                    credentialsEditor.putString(CommonIdentifiers.getLocale(),addresses.get(0).getLocality());
                                    credentialsEditor.apply();

                                    hideNoLocation();
                                    lat = String.valueOf(lastLocation.getLatitude());
                                    lon = String.valueOf(lastLocation.getLongitude());
                                    loadPlaces(lat, lon, current_page);
                                } else {
                                    /*hideProgressBar();
                                    showNoLocation();
                                    setCurrentPlaceTV("Tap To Retry");*/
                                    //Log.i(TAG, "No Recent Locations");
                                    new GetLocation();
                                }


                            }
                            Looper.loop();
                        } catch (IOException e) {
                            e.printStackTrace();
                            hideProgressBar();
                        }

                    }

                }).start();
            }



        }

        @Override
        public void onConnectionSuspended(int i) {
            Snackbar.make(findViewById(R.id.snackbarPosition), "GPS Connection Problems", Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
                showNoLocation();
        }
    }

   /* @SuppressWarnings("PointlessArithmeticExpression")
    class RefreshLocation implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


        public RefreshLocation() {

            if(apiClient == null)
                buildGoogleApiClient();
        }

        synchronized void buildGoogleApiClient() {
            apiClient = new GoogleApiClient.Builder(AroundMeActivity.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            apiClient.connect();
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000);

        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onConnected(Bundle bundle) {

            hideNoLocation();
            showProgressBar();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG,"++++");
                        Looper.prepare();
                        locationHandler = new Handler();
                        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, RefreshLocation.this);
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                apiClient);
                        final Geocoder geoCoder = new Geocoder(AroundMeActivity.this, Locale.getDefault());

                        if (lastLocation != null) {
                            addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                            setCurrentPlaceTV(addresses.get(0).getLocality());
                            credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                            credentialsEditor.putFloat(CommonIdentifiers.getLon(),(float) lastLocation.getLongitude());
                            credentialsEditor.putString(CommonIdentifiers.getLocale(),addresses.get(0).getLocality());
                            Log.i(TAG, "Sublocality--> "+addresses.get(0).getSubAdminArea());
                            credentialsEditor.commit();
                            hideProgressBar();
                            lat = String.valueOf(lastLocation.getLatitude());
                            lon = String.valueOf(lastLocation.getLongitude());
                            loadPlaces(lat, lon, current_page);
                            Log.i(TAG, "here 1");

                        } else {
                            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, RefreshLocation.this);
                            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    apiClient);

                            if (lastLocation != null) {
                                //Log.i(TAG, "Location Found");
                                addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                setCurrentPlaceTV(addresses.get(0).getLocality());
                                credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                                credentialsEditor.putFloat(CommonIdentifiers.getLon(),(float) lastLocation.getLongitude());
                                credentialsEditor.putString(CommonIdentifiers.getLocale(),addresses.get(0).getLocality());
                                credentialsEditor.commit();
                                hideProgressBar();
                                //Log.i(TAG, "here 2");
                                lat = String.valueOf(lastLocation.getLatitude());
                                lon = String.valueOf(lastLocation.getLongitude());
                                loadPlaces(lat, lon, current_page);
                            } else {
                                /*hideProgressBar();
                                showNoLocation();
                                setCurrentPlaceTV("Tap To Retry");
                                Snackbar.make(findViewById(R.id.snackbarPosition), "GPS problems. Please attempt to reconnect outdoors",
                                        Snackbar.LENGTH_LONG).show();
                                //Log.i(TAG, "here 3");
                                //Log.i(TAG, "No Recent Locations");
                               // new RefreshLocation();
                            }


                        }
  //                      Looper.loop();
                    } catch (IOException e) {
                        e.printStackTrace();
                        hideProgressBar();
                    }

                }

            }).start();

        }

        @Override
        public void onConnectionSuspended(int i) {
            Snackbar.make(findViewById(R.id.snackbarPosition), "GPS Connection Problems", Snackbar.LENGTH_LONG).show();

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            showNoLocation();
        }
    }*/
    void handlePlaces(String json){

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonMainNode = jsonObject.optJSONArray("places");

            for(int x = 0; x < jsonMainNode.length(); x++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(x);
                String restName = jsonChild.optString("restaurantName");
                Double distance = jsonChild.optDouble("distance");
                String rating;
                if(!jsonChild.optString("rating").equalsIgnoreCase("null"))
                    rating = jsonChild.optString("rating");
                else
                    rating = "0";

                String restID = jsonChild.optString("restaurantID");
                String thumbDir = jsonChild.optString("thumbPhotoDir");
                restaurants.add(new Restaurant(restName,rating,restID,thumbDir,null,distance));
                isConnectedFirst = true;
            }
            updateAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
            showNoRestaurants();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(apiClient!=null && !isConnectedFirst)
            apiClient.connect();
        if(fromGPSsettings && GPSstatus()){
            fromGPSsettings = false;
            setCurrentPlaceTV("Detecting Location...");
            restaurants.clear();
            updateAdapter();
            new GetLocation();
        }
        //Log.i(TAG, "analytics " + getClass().getSimpleName());
        tracker.setScreenName("Activity~" + getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        //Log.i(TAG,"called On Resume");
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(apiClient != null) {
            if (apiClient.isConnected()) {
                apiClient.disconnect();
            }
        }
    }

    public void sendHit(String restID, String restName){
        tracker.send(new HitBuilders.EventBuilder().setCategory("AroundMe Hit").
                setLabel(restID).setAction(restName).build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       /*getMenuInflater().inflate(R.menu.menu_around_me, menu);
       SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default*/
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
    @Override
    public boolean onSearchRequested() {
        //Log.i(TAG, "in onSearchRequested");
        /*override this to notify you when the search dialog
        is activated. return the super class implementation*/
        return super.onSearchRequested();
    }

    public void doSearch(@SuppressWarnings("UnusedParameters") View view) {
        //Log.i(TAG, "in onDoSearch");
        onSearchRequested();
    }


    void setCurrentPlaceTV(final String place){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentPlaceTV.setText(place);
            }
        });
    }

}
