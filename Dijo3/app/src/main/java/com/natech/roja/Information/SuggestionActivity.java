package com.natech.roja.Information;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@SuppressWarnings({"TryWithIdenticalCatches", "ConstantConditions"})
public class SuggestionActivity extends AppCompatActivity {

    private EditText restaurantNameED, cityED, extrasED;
    private ProgressDialog progressDialog;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        final String username = getIntent().getExtras().getString(InformationActivity.USER_NAME);
        final String userID = getIntent().getExtras().getString(InformationActivity.USER_ID);
        Toolbar toolbar = (Toolbar)findViewById(R.id.shadowToolBar);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);

        toolbarTitle.setText("Suggest An Eatery");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        final Animation contentAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        toolbarLayout.startAnimation(toolbarAnim);
        restaurantNameED = (EditText)findViewById(R.id.suggestRestaurantNameTV);
        cityED = (EditText)findViewById(R.id.suggestCityTV);
        extrasED = (EditText)findViewById(R.id.suggestExtrasTV);

        final Button submitBtn = (Button)findViewById(R.id.suggestBtn);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                restaurantNameED.setVisibility(View.VISIBLE);
                cityED.setVisibility(View.VISIBLE);
                extrasED.setVisibility(View.VISIBLE);
                submitBtn.setVisibility(View.VISIBLE);

                restaurantNameED.startAnimation(contentAnim);
                cityED.startAnimation(contentAnim);
                extrasED.startAnimation(contentAnim);
                submitBtn.startAnimation(contentAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!restaurantNameED.getText().toString().isEmpty() && !cityED.getText().toString().isEmpty()){
                    String restaurantName = restaurantNameED.getText().toString().trim();
                    String city = cityED.getText().toString().trim();

                    String extras;
                    if(!extrasED.getText().toString().isEmpty())
                        extras = extrasED.getText().toString().trim();
                    else
                        extras = "None";
                    showProgressBar();
                    sendSuggestion(username,userID,restaurantName,city,extras);
                }
            }
        });
    }

    void sendSuggestion(final String username, final String userID,final String restaurantName, final String city,final String extras){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(Server.getSubmitFeedback());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode("suggestion","UTF-8");
                    postData += "&"+URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                    postData += "&"+URLEncoder.encode("userName","UTF-8")+"="+URLEncoder.encode(username,"UTF-8");
                    postData += "&"+URLEncoder.encode("restaurantName","UTF-8")+"="+URLEncoder.encode(restaurantName,"UTF-8");
                    postData += "&"+URLEncoder.encode("city","UTF-8")+"="+URLEncoder.encode(city,"UTF-8");
                    postData += "&"+URLEncoder.encode("extras","UTF-8")+"="+URLEncoder.encode(extras,"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    //Log.i("", "New Response : " + line);
                    if(line != null){
                        if(line.equalsIgnoreCase("Submitted")) {
                            hideProgressBar();
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "Suggestion Submitted. Thank You!", Snackbar.LENGTH_LONG).show();
                            clearEditText();
                        }
                        else {
                            hideProgressBar();
                            errorSnackBar();
                        }
                    }else {
                        hideProgressBar();
                        errorSnackBar();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    errorSnackBar();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    errorSnackBar();
                } catch (IOException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    errorSnackBar();
                }
            }
        }).start();

    }

    void clearEditText(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                restaurantNameED.getText().clear();
                cityED.getText().clear();
                extrasED.getText().clear();
            }
        });
    }

    void errorSnackBar(){
        Snackbar.make(findViewById(R.id.snackbarPosition),
                "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
    }

    void showProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(SuggestionActivity.this);
                progressDialog.setMessage("Submitting...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });

    }
    void hideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_suggestion, menu);
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
