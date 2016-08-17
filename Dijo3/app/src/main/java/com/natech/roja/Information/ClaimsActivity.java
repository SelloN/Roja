package com.natech.roja.Information;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ClaimsActivity extends AppCompatActivity {

    private static final String TAG = ClaimsActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private RelativeLayout noConnectionView;
    private String userID;
    private RecyclerView recyclerView;
    private List<Points> pointsList;
    private ClaimsRVAdapter adapter;
    public  static ClaimsActivity claimsActivity;
    private Dialog dialog;
    private ProgressDialog progressDialog;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar =  (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        TextView pointsTV = (TextView)findViewById(R.id.pointsTV);
        toolbarTV.setText(getIntent().getExtras().getString(CommonIdentifiers.getRestName()));
        pointsTV.setText("You have "+getIntent().getExtras().getInt(CommonIdentifiers.getPoints())+" points");
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noConnectionView = (RelativeLayout)findViewById(R.id.noNetwork);
        userID = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE)
                .getString(CommonIdentifiers.getUserId(),null);
        setRecyclerview();
        //Log.i(TAG,"userID = "+userID);
        if(NetworkUtil.getConnectivityStatus(ClaimsActivity.this))
                    getRewards();
        else {
            noConnectionView.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }

        claimsActivity = this;

    }

    private void setRecyclerview(){
        recyclerView = (RecyclerView)findViewById(R.id.claimsRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        pointsList = new ArrayList<>();
        adapter = new ClaimsRVAdapter(pointsList,getIntent().getExtras().getInt(CommonIdentifiers.getPoints()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

    }

    public void showClaimDialog(final int points,final int restPointID, final String reward){

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage("Are you sure you want to make this claim for "+points+" points?");
        builder.setPositiveButton("Claim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(NetworkUtil.getConnectivityStatus(ClaimsActivity.this))
                    new SubmitClaim(getIntent().getExtras().getInt(CommonIdentifiers.getPoints())
                            ,points,restPointID,reward).execute(null, null, null);
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection",
                            Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

        /*AppCompatDialog compatDialog = new AppCompatDialog(this);
        compatDialog.setTitle("Claim Reward");
        compatDialog.show();*/

        /*
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.clear_cart_dialog);
        dialog.setCanceledOnTouchOutside(true);
        TextView messageTV = (TextView) dialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Are you sure you want to make this claim for "+points+" points?");
        Button yesBtn = (Button) dialog.findViewById(R.id.yesClear);
        Button noBtn = (Button) dialog.findViewById(R.id.noClear);
        dialog.show();
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(NetworkUtil.getConnectivityStatus(ClaimsActivity.this))
                    new SubmitClaim(getIntent().getExtras().getInt(CommonIdentifiers.getPoints())
                            ,points,restPointID,reward).execute(null, null, null);
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection",
                            Snackbar.LENGTH_LONG).show();
            }
        });*/
    }

    void showProgressDialog(String message){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideProgressDialog(){
        progressDialog.dismiss();
    }

    private class SubmitClaim extends AsyncTask<String, Void, String>{

        int updatedPoints;
        String restPointID;
        String reward;
        public SubmitClaim(int userPoints, int points,int restPointID, String reward){
            updatedPoints = userPoints - points;
            this.restPointID = String.valueOf(restPointID);
            this.reward = reward;
        }

        @Override
        protected void onPreExecute(){
            showProgressDialog("Processing Claim... Please Wait");
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getSubmitClaim());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getUserId(), "UTF-8")
                        +"="+URLEncoder.encode(userID,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getPoints(),"UTF-8")
                        +"="+URLEncoder.encode(String.valueOf(updatedPoints),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getPointsId(),"UTF-8")
                        +"="+URLEncoder.encode(String.valueOf(getIntent().getExtras().
                        getInt(CommonIdentifiers.getPointsId())),"UTF-8");
                postData += "&"+URLEncoder.encode("restPointID","UTF-8")
                        +"="+URLEncoder.encode(restPointID,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getUserEmail(),"UTF-8")
                        +"="+URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                        getString(CommonIdentifiers.getUserEmail(), null),"UTF-8");
                postData += "&"+URLEncoder.encode("reward","UTF-8")
                        +"="+URLEncoder.encode(reward,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRestName(),"UTF-8")
                        +"="+URLEncoder.encode(getIntent().getExtras().getString(CommonIdentifiers.getRestName()),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getRestEmail(),"UTF-8")
                        +"="+URLEncoder.encode(getIntent().getExtras().getString(CommonIdentifiers.getRestEmail()),"UTF-8");

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i(TAG,url.toString()+postData);
                Log.i(TAG, line);
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
        protected void onPostExecute(String arg){
            hideProgressDialog();

            if(arg.equalsIgnoreCase("Successful")) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("refresh", true);
                bundle.putInt(CommonIdentifiers.getPoints(),updatedPoints);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }else
                Snackbar.make(findViewById(R.id.snackbarPosition), "Something went wrong. Please try again",
                        Snackbar.LENGTH_LONG).show();

        }
    }

    private void getRewards(){
        new AsyncTask<String, Void, String>(){

            @Override
            protected void onPreExecute(){
                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(Server.getRewards());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getRestId(), "UTF-8")
                            +"="+URLEncoder.encode(getIntent().getExtras().getString(CommonIdentifiers.getRestId()),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG,url.toString()+postData);
                    Log.i(TAG, line);
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
                // historyList.remove(historyList.size()-1);
                if(!args.equalsIgnoreCase("limit") && !args.equalsIgnoreCase("Error")) {
                    addItems(args);
                }
                else if(args.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }

        }.execute();
    }
    private void addItems(String jsonResult){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonObject.optJSONArray("rewards");

            for(int i = 0; i < jsonMainNode.length();i++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(i);
                int points = jsonChild.optInt("points");
                int restPointID = jsonChild.optInt("restPointID");
                String reward = jsonChild.optString("reward");
                pointsList.add(new Points(points,reward,restPointID));
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_claims, menu);
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
