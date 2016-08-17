package com.natech.roja.HomeContent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.natech.roja.Analytics;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Restaurants.RestaurantAroundMeActivity;
import com.natech.roja.Utilities.CommonIdentifiers;

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

@SuppressWarnings("ConstantConditions")
public class WebViewActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private String weeklyID;
    private WebView webView;
    private RelativeLayout noConnectionView;
    private TextView toolbarTitle;
    private String type;
    private String restID;
    private ProgressDialog progressDialog;
    private Tracker tracker;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        type = getIntent().getExtras().getString("type");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noConnectionView = (RelativeLayout)findViewById(R.id.noNetwork);
        toolbarTitle.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Analytics analytics = (Analytics)getApplication();
        tracker = analytics.getDefaultTracker();
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
 //       WebView.setWebContentsDebuggingEnabled(false);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView web,String url, Bitmap bitmap){
                toolbarTitle.setText("Loading...");
            }
            @Override
            public void onPageFinished(WebView web, String url){

                toolbarTitle.setText(web.getTitle());

            }

            @Override
            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error){
                if(error.hasError(SslError.SSL_INVALID) || error.hasError(SslError.SSL_EXPIRED) || error.hasError(SslError.SSL_UNTRUSTED)) {
                    handler.cancel();
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
                }else
                    handler.proceed();
            }
        });
        if(NetworkUtil.getConnectivityStatus(WebViewActivity.this))

            if(type.equalsIgnoreCase("article")) {
                weeklyID = getIntent().getExtras().getString("weeklyID");
                new GetLink().execute();
            }else if(type.equalsIgnoreCase("promotion")){
                String promoLink = getIntent().getExtras().getString("promoLink");
                loadWebPage(promoLink);
            }
            else if(type.equalsIgnoreCase("privacy")){
                String promoLink = getIntent().getExtras().getString("link");
                loadWebPage(promoLink);
            }
        else {
            noConnectionView.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        //Log.i(TAG,"analytics "+getClass().getSimpleName());
        tracker.setScreenName("Activity~" + getClass().getSimpleName());
        tracker.enableAdvertisingIdCollection(true);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        tracker.send(new HitBuilders.EventBuilder().setCategory("WebView Hit").setLabel(type).
                setAction(String.valueOf(restID)).build());
    }

    void loadWebPage(final String link){
        if(NetworkUtil.getConnectivityStatus(WebViewActivity.this))
            webView.loadUrl(link);
        else {
            noConnectionView.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }

    }

    private class GetLink extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            showProgressBar();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getArticles());
                URLConnection connection = url.openConnection();
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
                }else {
                    return "Error";
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
        }

        @Override
        protected void onPostExecute(String arg){
            hideProgressBar();
            if(!arg.equalsIgnoreCase("Error")){
                loadWebPage(arg);

            }else if(arg.equalsIgnoreCase("Error")){
                errorSnackBar();
            }

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    void errorSnackBar(){
        Snackbar.make(findViewById(R.id.snackbarPosition),
                "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
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

    void showProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(WebViewActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    void hideProgressDialog(){
        progressDialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(type.equalsIgnoreCase("promotion"))
            getMenuInflater().inflate(R.menu.menu_web_view, menu);
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
                new GetRestaurantRating().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openRestaurant(String rating){
        Intent intent = new Intent(WebViewActivity.this,RestaurantAroundMeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CommonIdentifiers.getRestName(),getIntent().getExtras().getString(CommonIdentifiers.getRestName()));
        bundle.putInt(CommonIdentifiers.getRestId(), Integer.valueOf(restID));
        bundle.putString(CommonIdentifiers.getRating(),rating);
        intent.putExtras(bundle);
        hideProgressBar();
        startActivity(intent);
    }
    private class GetRestaurantRating extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }
        @Override
        protected String doInBackground(String... strings) {

            URL url;
            restID = getIntent().getExtras().getString(CommonIdentifiers.getRestId());
            //Log.i("", "New Response : " + restID);
            try {
                url = new URL(Server.getReviewAverages());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("restaurant","UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(restID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                //Log.i("","+++ "+connection.getResponseCode());
               // Log.i("","+++ "+connection.getResponseMessage());
                if(line != null){
                    openRestaurant(line);
                }else
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

            return "";
        }

        @Override
        protected void onPostExecute(String arg){
            hideProgressDialog();
            if(arg.equals("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Something went wrong. Please try again", Snackbar.LENGTH_LONG).show();
        }

    }
}
