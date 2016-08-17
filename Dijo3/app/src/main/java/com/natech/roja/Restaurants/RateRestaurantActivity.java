package com.natech.roja.Restaurants;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.natech.roja.MenuCategories.MenuItem;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class RateRestaurantActivity extends AppCompatActivity {

    private OrderHistoryRVAdapter adapter;
    private List<MenuItem> orderList;
    private String restName,restID,logID,review,userID;
    private int menuID, orderID, restPosition;
    private ImageView header;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private Boolean hasOrders = false, isRated = false;
    private TextView noOrdersTV;
    private Dialog dialog;
    private float rate = 5;
    public static RateRestaurantActivity rateRestaurantActivity;




    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_restaurant);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        restID = getIntent().getExtras().getString(CommonIdentifiers.getRestId());
        logID = getIntent().getExtras().getString(CommonIdentifiers.getLogId());
        restName = getIntent().getExtras().getString(CommonIdentifiers.getRestName());
        isRated = getIntent().getExtras().getBoolean("restRated");
        restPosition = getIntent().getExtras().getInt("restPosition");
        rateRestaurantActivity =this;
        SharedPreferences idFile = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE);
        userID = idFile.getString(CommonIdentifiers.getUserId(), null);
        Toolbar toolbar = (Toolbar)findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.myPrimaryColor));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noOrdersTV = (TextView)findViewById(R.id.noOrdersTV);
        header = (ImageView)findViewById(R.id.header);
        setRecyclerView();
        if(NetworkUtil.getConnectivityStatus(this)) {
            new GetRestaurantHeader().execute();
            new GetRestaurantRating().execute();
            new GetTableLogStatus().execute();
        }
        else
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
       FloatingActionButton rateFAB = (FloatingActionButton)findViewById(R.id.rateFAB);
        rateFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRated) {
                    if (NetworkUtil.getConnectivityStatus(RateRestaurantActivity.this))
                        openRateDialog(restName, true, 0, 0,0);
                    else
                        Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
                else
                    showRatedSnackBar();
            }
        });
        final String TUTORIAL = "LearnedRateRestaurantActivity";
        if(!getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
            final ShowcaseView showcaseView = new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.rateFAB, this)).
                    hideOnTouchOutside().
                    setContentTitle("Rate Restaurant").setStyle(R.style.CustomShowcaseTheme3)
                    .setContentText("Touch this button to rate and review the restaurant").
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

     void openRateDialog(String title, final Boolean isRestaurantReview,int menuID,int orderID,final int position){
        /* AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);
         builder.setTitle("Rate "+title);
         builder.setView(R.layout.rate_dialog);
         builder.setPositiveButton(R.string.submit,new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {

             }
         });
         builder.setNegativeButton(R.string.cancel,null);
         builder.show();*/
        dialog = new Dialog(RateRestaurantActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_dialog);
        TextView titleTV = (TextView)dialog.findViewById(R.id.rateHeader);
        final TextView rateTV = (TextView)dialog.findViewById(R.id.rateTV);
        Button submitBtn = (Button)dialog.findViewById(R.id.submitRateBtn);
        Button cancelBtn = (Button)dialog.findViewById(R.id.cancelRateBtn);
        RatingBar ratingBar = (RatingBar)dialog.findViewById(R.id.ratingsBar);
        final EditText reviewED = (EditText)dialog.findViewById(R.id.reviewED);
        titleTV.setText("Rate "+title);
        dialog.show();

        this.menuID = menuID;
        this.orderID = orderID;

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float ratings, boolean b) {
                rate = ratings;
                rateTV.setText(String.format("%1$,.1f",ratings));
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeRateDialog();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                review = reviewED.getText().toString();
                //noinspection ResultOfMethodCallIgnored
                review.trim();
                //Log.i("","Is Restaurant Review "+isRestaurantReview);
                if(!review.isEmpty()) {
                    if(NetworkUtil.getConnectivityStatus(RateRestaurantActivity.this)) {
                        if (isRestaurantReview)
                            new SubmitRestaurantReview(true).execute();
                        else
                            new SubmitMenuReview(position,false).execute();
                    }
                    else
                        Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    private void closeRateDialog(){
        dialog.dismiss();
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting... Please Wait");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void hideProgressDialog(){
        progressDialog.dismiss();
    }

    @SuppressWarnings("SameParameterValue")
    private class SubmitRestaurantReview extends AsyncTask<String, Void, String>{

        private final Boolean isRestaurantReview;
        public SubmitRestaurantReview(Boolean isRestaurantReview){
            this.isRestaurantReview = isRestaurantReview;
        }

        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            try {
                url = new URL(Server.getSubmitReview());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("restaurant","UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(restID),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRating(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(rate),"UTF-8");
                postData += "&"+URLEncoder.encode("review","UTF-8")+"="+URLEncoder.encode(review,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getLogId(),"UTF-8")+"="+URLEncoder.encode(logID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                ///Log.i("", "New Response : " + line);
                if(line != null){
                    if(line.equalsIgnoreCase("Review Added")) {
                        isRated = true;
                        HistoryActivity.historyActivity.historyList.get(restPosition).setIsRated(isRated);
                        showSuccessSnackBarNotification(0,isRestaurantReview);
                    }
                    else
                        showFailureNotification();
                }else
                    showFailureNotification();
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
            if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private class SubmitMenuReview extends AsyncTask<String, Void, String>{

        private final int position;
        private final Boolean isRestaurantReview;
        public SubmitMenuReview(int position, Boolean isRestaurantReview){
            this.position = position;
            this.isRestaurantReview = isRestaurantReview;
        }

        @Override
        protected void onPreExecute(){
            hideNoPlacedOrders();
            showProgressDialog();

        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            try {
                url = new URL(Server.getSubmitReview());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("menu","UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getMenuId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(menuID),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserId(),"UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                postData += "&"+URLEncoder.encode("orderID","UTF-8")+"="+URLEncoder.encode(String.valueOf(orderID),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRating(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(rate),"UTF-8");
                postData += "&"+URLEncoder.encode("review","UTF-8")+"="+URLEncoder.encode(review,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                if(line != null){
                    if(line.equalsIgnoreCase("Review Added"))
                        showSuccessSnackBarNotification(position,isRestaurantReview);
                    else
                        showFailureNotification();
                }
                else
                    showFailureNotification();
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
            if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSuccessSnackBarNotification(int position, Boolean isRestaurantReview){
        hideProgressDialog();
        //orderList.clear();
        if(!isRestaurantReview) {
            orderList.get(position).setIsRated(true);
            updateSpecificPosition(position);
        }else
            isRated = true;
        //updateAdapter();
        closeRateDialog();
        Snackbar.make(findViewById(R.id.snackbarPosition), "Review Submitted. Thank You!", Snackbar.LENGTH_SHORT).show();
        //new GetRestaurantRating().execute();
    }

    void showRatedSnackBar(){
        Snackbar.make(findViewById(R.id.snackbarPosition), "You Have Already Submitted Your Review", Snackbar.LENGTH_SHORT).show();
    }
    private void showFailureNotification(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
                TextView errorTV = (TextView)dialog.findViewById(R.id.errorTV);
                errorTV.setVisibility(View.VISIBLE);
            }
        });

    }

    private void setRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.scrollableviewHistory);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        orderList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        adapter = new OrderHistoryRVAdapter(orderList,this);
        recyclerView.setAdapter(adapter);
    }

    private class  GetTableLogStatus extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String...args) {
            URL url;
            try {
                url = new URL(Server.getReviewAverages());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getLogId(), "UTF-8")+"="+URLEncoder.encode(logID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                if(line != null){

                    if(!line.equalsIgnoreCase("0"))
                        isRated = true;
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
            if(arg.equals("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
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
                String postData = URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(restID,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("review","UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line =  reader.readLine();
                reader.close();
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

    private class GetRestaurantRating extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute(){
            showProgressBar();
        }
        @Override
        protected String doInBackground(String... strings) {

            URL url;
            try {
                url = new URL(Server.getReviewAverages());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("restRating","UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRestId(),"UTF-8")+"="+URLEncoder.encode(String.valueOf(restID),"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                //Log.i("","+++ "+connection.getResponseCode());
                //Log.i("","+++ "+connection.getResponseMessage());
                if(line != null){
                    addRatingsItems(line);
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
            if(arg.equals("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }

    }

    private class GetOrderedItems extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            try {
                url = new URL(Server.getOrder());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8")+"="+URLEncoder.encode("rate","UTF-8");
                postData += "&"+URLEncoder.encode("tableLogID","UTF-8")+"="+URLEncoder.encode(logID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("", "New Response : " + line);
                if(line != null){
                    addOrderedItems(line);
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

            hideProgressBar();
            if(arg.equals("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    private void addRatingsItems(String json){
        try{
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("restRating");
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String rating;
                if(!jsonChildNode.optString("rating").equalsIgnoreCase("null"))
                    rating = jsonChildNode.optString("rating");
                else
                    rating = "0.0";
                String location = jsonChildNode.optString("city");
                String description = jsonChildNode.optString("description");
                orderList.add(new MenuItem(
                        new Restaurant(null,description,rating,location,restID)));

            }

            updateAdapter();
            new GetOrderedItems().execute();
        }catch (JSONException e){
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
            //Log.i("", e.toString());
        }
    }

    private void addOrderedItems(String json){
        try{
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("order");
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String menuItem = jsonChildNode.optString("menuItem");
                double price = jsonChildNode.optDouble("itemPrice");
                int menuID = jsonChildNode.optInt("menuID");
                int rateCode = jsonChildNode.optInt("rated");
                int orderID = jsonChildNode.optInt("orderID");
                //Log.i("","orderID "+orderID);
                orderList.add(new MenuItem(menuItem,price,menuID,rateCode,orderID));
                hasOrders =true;
            }
            updateAdapter();
        }catch (JSONException e){
            if(!hasOrders)
                showNoPlacedOrders();
            //Log.i("", e.toString());
        }
    }

    private void updateAdapter()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateSpecificPosition(final int position){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
            }
        });
    }
    private void showProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
    private void hideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void showNoPlacedOrders(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noOrdersTV.setVisibility(View.VISIBLE);
            }
        });
    }
    private void hideNoPlacedOrders(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noOrdersTV.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void drawHeader(String jsonHeader)
    {
        try{
            JSONObject jsonResponse = new JSONObject(jsonHeader);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("restaurant");
            String restaurantName;
            String photoDir;
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_rate_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
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
