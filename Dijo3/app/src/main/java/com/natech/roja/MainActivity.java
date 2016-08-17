package com.natech.roja;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.natech.roja.Database.DBHelper;
import com.natech.roja.HomeContent.RestaurantWeekActivity;
import com.natech.roja.HomeContent.WebViewActivity;
import com.natech.roja.HomeContent.WeeklyContent;
import com.natech.roja.Information.InformationActivity;
import com.natech.roja.Information.PointsActivity;
import com.natech.roja.LogIn.EditProfileActivity;
import com.natech.roja.LogIn.LogInActivity;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.HomeContent.Promotions;
import com.natech.roja.HomeContent.PromotionsRVAdapter;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.Notifications.ReviewReceiver;
import com.natech.roja.Notifications.ReviewService;
import com.natech.roja.Restaurants.AroundMeActivity;
import com.natech.roja.Restaurants.FavouriteActivity;
import com.natech.roja.Restaurants.HistoryActivity;
import com.natech.roja.Restaurants.TrendsActivity;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.natech.dijo.backend.registration.Registration;

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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerCallbacks, BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    private static final int GPS_REQUEST_CODE = 1;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EMAIL = "email";
    public static MainActivity mainActivity;
    private String photoDir, email;
    private static String name;
    private String surname, regID, existingRegID;
    private SharedPreferences idFile;
    private SharedPreferences credentialsFile;
    private SharedPreferences.Editor credentialsEditor;
    private PendingIntent reviewsPendingIntent;
    private Boolean hasRegID = false;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging gcm;
    private static Registration regService = null;
    private View divider;
    private TextView dividerText;
    private HashMap<String,WeeklyContent> url_maps;
    public ImageButton FAB;
    private RelativeLayout noPromotions, noGPS;
    private Tracker tracker;
    private DBHelper dbHelper;
    private boolean fromGPSsettings = false;
    private GoogleApiClient apiClient;
    private PromotionsRVAdapter promotionsRVAdapter;
    private List<Promotions> promotionsList;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dividerText = (TextView)findViewById(R.id.dividerText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Analytics analytics = (Analytics)getApplication();
        dbHelper = new DBHelper(this);
        dbHelper.checkList();

        if(GPSstatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new GetLocation();
                }
            }).start();
        }else if(!GPSstatus() && getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE).
                contains(CommonIdentifiers.getLocale())){
            updateDividerText(getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE).
                    getString(CommonIdentifiers.getLocale(),null));
            getLocalityCode(getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE).
                    getString(CommonIdentifiers.getLocale(),null));
        }else{
            showGPSDialog();
        }
        tracker = analytics.getDefaultTracker();
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        mainActivity = this;
        email = getIntent().getExtras().getString(EMAIL);
        getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).edit().
                putString(CommonIdentifiers.getUserEmail(),email).apply();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);

        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTV.setText("Roja");
        //System.loadLibrary("iconv");
        Locale.setDefault(Locale.ENGLISH);
        new GetWeeklyContent().execute();
        noPromotions = (RelativeLayout)findViewById(R.id.noPromotions);
        noGPS = (RelativeLayout)findViewById(R.id.noGPS);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fly_in);

        FAB = (ImageButton) findViewById(R.id.scanFAB);
        FAB.startAnimation(animation);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScanActivity();
            }

        });

        divider = findViewById(R.id.divider);


        credentialsFile = getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE);
        SharedPreferences settingsFile = getSharedPreferences(AppSharedPreferences.getSettings(), MODE_PRIVATE);

        if(credentialsFile.contains(AppSharedPreferences.getRegistrationId())){
            regID = credentialsFile.getString(AppSharedPreferences.getRegistrationId(),null);
            hasRegID = true;
        }else{
            hasRegID = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(NetworkUtil.getConnectivityStatus(MainActivity.this))
                        startRegistration();
                    else
                        Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }).start();

        }
        if(NetworkUtil.getConnectivityStatus(this)) {
            setupRecyclerView();

            if(!getIntent().getExtras().containsKey("SignUp")) {
                getUserDetails();

            }else{
                new GetUserID().execute();
            }
        }
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();

        //---Heavy bug with the alarmmanger. review code until it works---
        if(!settingsFile.contains(AppSharedPreferences.getReviewNotifications())) {
            Log.i(TAG,"alarm not set");
            prepareReviewsNotifications();
            settingsFile.edit().putBoolean(CommonIdentifiers.getReviewNotifications(),true).apply();
        }else if(!settingsFile.getBoolean(CommonIdentifiers.getNotifyActive(),false)
                && settingsFile.getBoolean(CommonIdentifiers.getReviewNotifications(),false)){
            Log.i(TAG,"+++++ "+settingsFile.getBoolean(CommonIdentifiers.getNotifyActive(),false)+"__ "
                    +settingsFile.getBoolean(CommonIdentifiers.getReviewNotifications(),false));
            Log.i(TAG,"alarm not set for some reason");
            prepareReviewsNotifications();
            settingsFile.edit().putBoolean(CommonIdentifiers.getReviewNotifications(),true).apply();
        }

       //-------16 October 2015------
        getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).edit().
                putBoolean(AppSharedPreferences.getLogState(),true).apply();

        //tracker.

        //get hash  key
        /*try {

            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.natech.dijo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/





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

        if(fromGPSsettings) {
            noPromotions.setVisibility(View.GONE);
            showProgresBar();
            new GetLocation();
            fromGPSsettings = false;
        }
    }

    void showNoLocation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noGPS.setVisibility(View.VISIBLE);
            }
        });
    }

    public void sendHit(String restID,String restName){
        tracker.send(new HitBuilders.EventBuilder().setCategory("Promotion Hit").setLabel(restName).setAction(restID).build());
    }

    public void prepareReviewsNotifications(){
        reviewsPendingIntent = PendingIntent.getBroadcast(this,0,new Intent(this,ReviewReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());

        Random random = new Random();
        int low = 7;
        int high = 22;
        int hour = random.nextInt(high - low) + low;
        int minute = random.nextInt(60 - 10) + 10;
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE,minute);
        cal.set(Calendar.SECOND,5);
        cal.set(Calendar.MILLISECOND,0);

        Log.i(TAG,"alarm set");
       /* if(System.currentTimeMillis() > cal.getTimeInMillis()){
            Log.i(TAG,"After set time");
        }else
            Log.i(TAG,"Before set time");

        Log.i(TAG,"Time: "+hour+":"+minute);*/
        //cal.setTimeInMillis(System.currentTimeMillis());
        Log.i(TAG,"Time: "+hour+":"+minute);
        if(cal.getTimeInMillis() >= System.currentTimeMillis() ) {
            Log.i(TAG,"Set today");
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, reviewsPendingIntent);
        }else{
            Log.i(TAG,"Set tomorrow");
            cal.set(Calendar.DATE,1);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, reviewsPendingIntent);
        }
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis()+AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY,
          //      reviewsPendingIntent);
        //Log.i(TAG,"Service has been set");


    }

    public void stopReviewsNotifications(){
        Intent alarmIntent = new Intent(this, ReviewReceiver.class);
        reviewsPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(reviewsPendingIntent);
        stopService(new Intent(this, ReviewService.class));
        Log.i(TAG,"Service has been stopped");
    }


    
    private void setSlider(String jsonResult){
        SliderLayout slider = (SliderLayout) findViewById(R.id.slider);
        url_maps = new HashMap<>();
        int size = 0;

        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("weeklyContent");
            size = jsonResponse.length();
            for (int i = 0; i < jsonMainNode.length(); i++) {

                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                url_maps.put(jsonChildNode.optString("contentID"),
                        new WeeklyContent(jsonChildNode.optInt("weeklyID"),jsonChildNode.optString("photoDir")));

            }
            for(String name : url_maps.keySet()){
                DefaultSliderView defaultSliderView =  new DefaultSliderView(this);
                defaultSliderView.image(url_maps.get(name).getPhotoDir()).
                        setScaleType(BaseSliderView.ScaleType.Fit).
                        setOnSliderClickListener(this);

                defaultSliderView.bundle(new Bundle());
                defaultSliderView.getBundle()
                        .putString("extra",name);

                slider.addSlider(defaultSliderView);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        slider.setPresetTransformer(SliderLayout.Transformer.DepthPage);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(4000);
        slider.addOnPageChangeListener(this);

    }

    private void  setupRecyclerView()
    {
        recyclerView = (RecyclerView)findViewById(R.id.promoRV);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);

    }

    private class GetWeeklyContent extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(Server.getWeeklyContent());
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i(TAG, "Response " + line);

                if(line != null){
                    return line;
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
            if(!arg.equalsIgnoreCase("Error")){
                setSlider(arg);
            }else if(arg.equalsIgnoreCase("Error")){
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems", Snackbar.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {

        switch ((baseSliderView.getBundle().get("extra").toString())){
            case "1":
                Intent webView = new Intent(this, WebViewActivity.class);
                Bundle b = new Bundle();
                b.putString("type","article");
                b.putString("weeklyID",String.valueOf(url_maps.get("1").getWeeklyID()));
                webView.putExtras(b);
                startActivity(webView);
                break;
            case "2":
                Intent intent = new Intent(this, RestaurantWeekActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("weeklyID",String.valueOf(url_maps.get("2").getWeeklyID()));
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "3":
                break;
            default:
                break;
        }

    }

    private class GetUserID extends AsyncTask<String, String, String>{

        @SuppressWarnings("TryWithIdenticalCatches")
        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(Server.getUserID());
                URLConnection connection = url.openConnection();
                String postData = URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("user","UTF-8");
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
                    return line;
                }else
                    return "Error";

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String args){
            if(!args.equalsIgnoreCase("Error")){
                try{
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(args);
                    idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
                    SharedPreferences.Editor idEditor = idFile.edit();
                    idEditor.putString(CommonIdentifiers.getUserId(),args);
                    idEditor.commit();
                    getUserDetails();
                    //new GetPromotions().execute();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }else if(args.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something went wrong. Please try again", Snackbar.LENGTH_SHORT).show();

        }
    }

    public void getUserDetails()
    {
        new AsyncTask<String, Void, String>()
        {

            @SuppressWarnings("TryWithIdenticalCatches")
            @Override
            protected String doInBackground(String... strings) {
                try {

                    idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
                    String userID = idFile.getString(CommonIdentifiers.getUserId(),null);
                    URL url = new URL(Server.getUserDetails());
                    URLConnection connection = url.openConnection();
                    String postData = URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                    postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("user","UTF-8");
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
                        return line;
                    }else
                        return "Error";

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "Error";
            }

         @Override
        public void onPostExecute(String arg){

             if(!arg.equals("Error"))
                setHeaderDetails(arg);
             else
                 Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }

        }.execute(null,null,null);
    }

    private void setHeaderDetails(String json)
    {
        try {
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("user");
            /*
                This loop gets the data from the Jason Array that stores
                the data from the Mysql database
             */
            for (int i = 0; i < jsonMainNode.length(); i++) {

                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                //Log.i(TAG,jsonChildNode.getString("name"));
                name = jsonChildNode.getString("name");
                surname = jsonChildNode.getString("surname");
                if(!jsonChildNode.getString("photoDir").equalsIgnoreCase("null"))
                    photoDir = jsonChildNode.getString("photoDir");
                else
                    photoDir = "none";
                existingRegID = jsonChildNode.getString("regID");
                //User userDetails = new User(name, surname, email, photoDir);
                getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).edit().
                        putString(CommonIdentifiers.getName(),name).apply();

            }
            mNavigationDrawerFragment.setUserData(name+" "+surname, email, photoDir);
            if(hasRegID){
                if(!regID.equals(existingRegID)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startRegistration();
                        }
                    }).start();
                }
            }




        } catch (JSONException e) {
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }


    }

    void hideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void showProgresBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private class GetPromotions extends AsyncTask<String, Void, String>
    {
        String locID;
        public GetPromotions(String locID){
            if(locID != null)
                this.locID = locID;
            else
                this.locID = "0";
        }
        @Override
        public void onPreExecute()
        {
            showProgresBar();
        }
        @Override
        public String doInBackground(String...params)
        {
            try {
                URL url = new URL(Server.getPromotions());
                URLConnection connection =   url.openConnection();
                String postData = URLEncoder.encode("locID","UTF-8")+"="+URLEncoder.encode(locID,"UTF-8");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i(TAG,line);
                if(line != null)
                    return line;
                else
                    return "Error";

            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Error";
        }

        @Override
        public void onPostExecute(String arg)
        {
            hideProgressBar();
            divider.setVisibility(View.VISIBLE);
            dividerText.setVisibility(View.VISIBLE);
            if(!arg.equals("Error"))
                drawPromotionList(arg);
            else
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    private void drawPromotionList(String jsonResult) {
        promotionsList = new ArrayList<>();


        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("promotions");

            /*
                This loop gets the data from the Jason Array that stores
                the data from the Mysql database
             */
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String caption = jsonChildNode.optString("caption");
                String photoDir = jsonChildNode.optString("photoDir");
                String promoLink = jsonChildNode.optString("promoLink");
                String restID = jsonChildNode.optString("restID");
                String restName = jsonChildNode.optString("restName");
                int promoType = jsonChildNode.optInt(("promoType"));
                String largePhotoDir = jsonChildNode.optString("largePhotoDir");
                String description = jsonChildNode.optString("desc");
                promotionsList.add(new Promotions(caption, photoDir,promoLink,restID,restName,promoType,
                        largePhotoDir,description));

            }
        } catch (JSONException e) {
            noPromotions.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }

        promotionsRVAdapter = new PromotionsRVAdapter(this, promotionsList);
        recyclerView.setAdapter(promotionsRVAdapter);

    }

        @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position)
        {
            case 0:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openTrendsActivity();
                    }
                }).start();
                break;
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openAroundMe();
                    }
                }).start();
                break;
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openFavourites();
                    }
                }).start();
                break;
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getScanActivity();
                    }
                }).start();
                break;
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openHistoryActivity();
                    }
                }).start();
                break;
            case 5:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openPointsActivity();
                    }
                }).start();
                break;
            case 6:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openEditProfileActivity();
                    }
                }).start();
                break;
            case 7:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tellABuddy();
                    }
                }).start();
                break;
            case 8:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openInfoActivity();
                    }
                }).start();
                break;
            case 9:
                logOut();
                break;

        }
    }

    private void openFavourites(){
        Intent intent = new Intent(this, FavouriteActivity.class);
        startActivity(intent);

    }

    private void openPointsActivity(){
        Intent intent = new Intent(this, PointsActivity.class);
        startActivity(intent);
    }

    private void openInfoActivity(){
        if((name != null && surname != null)) {
            idFile = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE);
            Intent intent = new Intent(this, InformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(CommonIdentifiers.getUserName(), name + " " + surname);
            bundle.putString(CommonIdentifiers.getUserId(), idFile.getString(CommonIdentifiers.getUserId(), null));
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    private void tellABuddy(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_body)+
                        "http://play.google.com/store/apps/details?id="+getPackageName());
                startActivity(Intent.createChooser(intent, "Tell Your Buddy Via"));
            }
        }).start();

        //intent.setData()
    }

    private void openAroundMe(){
        Intent intent = new Intent(this, AroundMeActivity.class);
        startActivity(intent);
    }

    private void logOut(){
        stopReviewsNotifications();
        getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                putBoolean(CommonIdentifiers.getNotifyActive(), false).commit();
        SharedPreferences.Editor idEditor = idFile.edit();
        idEditor.putBoolean(AppSharedPreferences.getLogState(),false);
        idEditor.commit();
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
        finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mNavigationDrawerFragment.isDrawerOpen()) {
                mNavigationDrawerFragment.closeDrawer();


            }else {
                //SharedPreferences.Editor idEditor = idFile.edit();
                getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                        edit().putBoolean(AppSharedPreferences.getLogState(), true).commit();
                getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                        putBoolean(CommonIdentifiers.getNotifyActive(), true).commit();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void openEditProfileActivity()
    {
        if(name != null && surname != null){
            idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle bundle = new Bundle();
            bundle.putString("name",name);
            bundle.putString("surname",surname);
            bundle.putString("picDir",photoDir);
            bundle.putString(CommonIdentifiers.getUserId(),idFile.getString(CommonIdentifiers.getUserId(),null));
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    private void openHistoryActivity(){
        idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
        String userID = idFile.getString(CommonIdentifiers.getUserId(),null);
        Intent intent = new Intent(this, HistoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CommonIdentifiers.getUserId(),userID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void openTrendsActivity(){
        Intent intent = new Intent(this, TrendsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_location) {

            if(GPSstatus()) {
                if(promotionsRVAdapter != null){
                    promotionsList.clear();
                    updateAdapter();
                    showProgresBar();

                }else{
                    noPromotions.setVisibility(View.GONE);
                    showProgresBar();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new GetLocation();
                    }
                }).start();
            }else{
                showGPSDialog();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
        Module that calls the activity that allows the user to scan the QR code
     */
    void getScanActivity()
    {
        Intent intent = new Intent(this,MScanActivity.class);
        startActivity(intent);
    }

    void startRegistration()
    {
        credentialsEditor = credentialsFile.edit();
        if(checkPlayServices())
        {
            //Log.i(TAG, "Google Play Services OK");
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
        }
        else
        {
            //Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Log.i(TAG, "No Google Play Services...Get it from the store.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this,
                                PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });

            } else {
                //Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
    void registerInBackground()
    {
                if (regService == null) {
                    Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(), null)
                            // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                            // otherwise they can be skipped
                            .setRootUrl("https://dijo-991.appspot.com/_ah/api/");
                    // end of optional local run code

                    regService = builder.build();
                }
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    String SENDER_ID = "296164418889";
                    regID = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regID;
                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    regService.register(regID).execute();
                    if (!msg.equalsIgnoreCase("SERVICE_NOT_AVAILABLE")) {

                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("server_response", msg);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);

                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }


    }

    void showGPSDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("Location Detection");
        builder.setMessage("In order to view amazing deals offered by restaurants around you please turn on your GPS");

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(promotionsRVAdapter != null && promotionsList.size()>0){
                    showNoLocation();

                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    noPromotions.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setPositiveButton("Turn On GPS",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                turnGPSon();
            }
        });
        builder.show();
    }

    private void turnGPSon(){
        fromGPSsettings = true;
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        //Log.i(TAG,"GPS is now on");
    }

    void updateAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                promotionsRVAdapter.notifyDataSetChanged();
            }
        });
    }



    // Define the Handler that receives messages from the thread and
    // update the progress
    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString(
                    "server_response");

            if ((null != aResponse)) {

                //Log.i(TAG, "	sendRegistrationIdToBackend();");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendRegistrationToServer();
                    }
                }).start();
                //sendRegistrationToServer();

            }

        }
    };

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */
    void sendRegistrationToServer()
    {
        try {
            idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
            BufferedReader reader;
            URL url = new URL(Server.getUpdateUserDetails());
            String postData = URLEncoder.encode("userID", "UTF-8")+"="+
                    URLEncoder.encode(idFile.getString(CommonIdentifiers.getUserId(),null),"UTF-8");
            postData += "&"+URLEncoder.encode("regID","UTF-8")+"="+URLEncoder.encode(regID,"UTF-8");
            postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("regID","UTF-8");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
            outputStreamWriter.write(postData);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            reader.close();
            //Log.i(TAG, "Reg to server "+regID);
            //Log.i(TAG, "New Response : " + line);

            if (line != null) {
                if (line.equalsIgnoreCase("New Device Registered successfully")) {
                    credentialsEditor.putString(AppSharedPreferences.getRegistrationId(),regID);
                    credentialsEditor.commit();
                    //Log.i(TAG,"Registered to server");
                }else{
                    //Log.i(TAG,"Failed To Register");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectGPSApi();
    }

    void getLocalityCode(String locale){
        new GetPromotions(dbHelper.getLocID(locale)).execute();

    }

    private boolean GPSstatus(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    void updateDividerText(final String location){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dividerText.setText(location);
            }
        });
    }

    void disconnectGPSApi(){
        if(apiClient != null) {
            if (apiClient.isConnected()) {
                apiClient.disconnect();
            }
        }
    }

    class GetLocation implements GoogleApiClient.OnConnectionFailedListener,
            GoogleApiClient.ConnectionCallbacks , LocationListener {


        private LocationRequest locationRequest;
        private boolean isConnectedFirst = false;
        private Handler locationHandler;
        private Location lastLocation;
        private String lon,lat;

        public GetLocation(){

            buildGoogleApiClient();
        }

        synchronized void buildGoogleApiClient() {
            apiClient = new GoogleApiClient.Builder(MainActivity.this)
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
            credentialsFile = getSharedPreferences(AppSharedPreferences.getCredentialsFile(), MODE_PRIVATE);
            credentialsEditor = credentialsFile.edit();

            if(!isConnectedFirst) {
                //showProgressBar();
                new Thread(new Runnable() {
                    public List<Address> addresses;

                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            locationHandler = new Handler();
                            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, GetLocation.this);
                            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    apiClient);
                            final Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());

                            if (lastLocation != null) {
                                addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                updateDividerText(addresses.get(0).getLocality());
                                credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                                credentialsEditor.putFloat(CommonIdentifiers.getLon(), (float) lastLocation.getLongitude());
                                credentialsEditor.putString(CommonIdentifiers.getLocale(), addresses.get(0).getLocality());
                                credentialsEditor.apply();
                                Log.i(TAG, addresses.get(0).getLocality());
                                //hideProgressBar();
                                //hideNoLocation();
                                lat = String.valueOf(lastLocation.getLatitude());
                                lon = String.valueOf(lastLocation.getLongitude());
                                getLocalityCode(addresses.get(0).getLocality());
 //                               disconnectGPSApi();
                            } else {
                                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, GetLocation.this);
                                lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                        apiClient);

                                if (lastLocation != null) {
                                    //Log.i(TAG, "Location Found");

                                    addresses = geoCoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                    updateDividerText(addresses.get(0).getLocality());
                                    credentialsEditor.putFloat(CommonIdentifiers.getLat(), (float) lastLocation.getLatitude());
                                    credentialsEditor.putFloat(CommonIdentifiers.getLon(),(float) lastLocation.getLongitude());
                                    credentialsEditor.putString(CommonIdentifiers.getLocale(),addresses.get(0).getLocality());
                                    credentialsEditor.apply();
                                    Log.i(TAG, addresses.get(0).getLocality());

                                    //hideNoLocation();
                                    lat = String.valueOf(lastLocation.getLatitude());
                                    lon = String.valueOf(lastLocation.getLongitude());
                                    getLocalityCode(addresses.get(0).getLocality());
 //                                   disconnectGPSApi();
                                } else {
                                    new GetLocation();
                                }


                            }
                            Looper.loop();
                        } catch (IOException e) {
                            e.printStackTrace();
                           // hideProgressBar();
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
            //showNoLocation();
        }
    }

}
