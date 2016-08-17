package com.natech.roja.HomeContent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Restaurants.RestaurantAroundMeActivity;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class PromoSplashActivity extends AppCompatActivity {

    private String restID;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_splash);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar2);
        //TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        //LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        //toolbarTitle.setText("");
        restID = getIntent().getExtras().getString(CommonIdentifiers.getRestId());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView promoDescTV = (TextView)findViewById(R.id.promo_desc);
        ImageView promoImage = (ImageView)findViewById(R.id.promo_pic);
        FloatingActionButton visitFAB = (FloatingActionButton)findViewById(R.id.visitFAB);

        visitFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetRestaurantRating().execute();
            }
        });


        Picasso.with(this).load(getIntent().getExtras().getString("largePhoto")).into(promoImage);
        promoDescTV.setText(getIntent().getExtras().getString("desc"));
      //  TextView logTV = (TextView)findViewById(R.id.logID);
        //tableNoTV.setText(getIntent().getExtras().getString("tableNo"));
//        logTV.setText("Order No: ");
    }

    private void openRestaurant(String rating){
        Intent intent = new Intent(PromoSplashActivity.this,RestaurantAroundMeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CommonIdentifiers.getRestName(),getIntent().getExtras().getString(CommonIdentifiers.getRestName()));
        bundle.putInt(CommonIdentifiers.getRestId(), Integer.valueOf(restID));
        bundle.putString(CommonIdentifiers.getRating(),rating);
        intent.putExtras(bundle);
        hideProgressDialog();
        startActivity(intent);
    }

    private class GetRestaurantRating extends AsyncTask<String,Void,String> {

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

    void showProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(PromoSplashActivity.this);
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
       // getMenuInflater().inflate(R.menu.menu_promo_splash, menu);
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
