package com.natech.roja.Information;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Restaurants.EndlessRecyclerScroll;
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

public class PointsActivity extends AppCompatActivity {

    private static final String TAG = PointsActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private RelativeLayout noConnectionView;
    private RelativeLayout noPointsView;
    private RecyclerView recyclerView;
    private PointsRVAdapter adapter;
    private List<Points> pointsList;
    private LinearLayoutManager layoutManager;
    private boolean isPoints = false;
    private String userID;
    private static final int current_page = 1;
    public static PointsActivity pointsActivity;
    private int position = 0;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
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
        toolbarTV.setText("Points");
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noConnectionView = (RelativeLayout)findViewById(R.id.noNetwork);
        noPointsView = (RelativeLayout)findViewById(R.id.noPoints);
        userID = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE)
                .getString(CommonIdentifiers.getUserId(),null);
        setRecyclerview();
        //Log.i(TAG,"userID = "+userID);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        toolbarLayout.startAnimation(toolbarAnim);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if(NetworkUtil.getConnectivityStatus(PointsActivity.this))
                    getPoints(current_page);
                else {
                    noConnectionView.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        recyclerView.addOnScrollListener(new EndlessRecyclerScroll(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMoreHistory(current_page);
            }
        });
        pointsActivity = this;

    }

    private void setRecyclerview(){
        recyclerView = (RecyclerView)findViewById(R.id.pointsRV);
        layoutManager = new LinearLayoutManager(this);
        pointsList = new ArrayList<>();
        adapter = new PointsRVAdapter(this,pointsList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    public void startClaimsActivity(String restName, String restID, int points, int pointsID, int position,String restEmail){
        this.position = position;
        Intent intent = new Intent(this, ClaimsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CommonIdentifiers.getRestName(),restName);
        bundle.putString(CommonIdentifiers.getRestId(),restID);
        bundle.putInt(CommonIdentifiers.getPoints(),points);
        bundle.putInt(CommonIdentifiers.getPointsId(),pointsID);
        bundle.putString(CommonIdentifiers.getRestEmail(),restEmail);
        intent.putExtras(bundle);
        startActivityForResult(intent,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    if(data.getExtras().getBoolean("refresh")){
                        pointsList.get(position).setPoints(data.getExtras().getInt(CommonIdentifiers.getPoints()));
                        adapter.notifyItemChanged(position);
                        Snackbar.make(findViewById(R.id.snackbarPosition), "Claim successfully processed",
                                Snackbar.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    }


    private void getPoints(final int page){
        new AsyncTask<String, Void, String>(){

            @Override
            protected void onPreExecute(){
                if(!isPoints)
                    progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(Server.getPoints());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getUserId(), "UTF-8")
                            +"="+URLEncoder.encode(userID,"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getCurrentPage(),"UTF-8")
                            +"="+URLEncoder.encode(String.valueOf(page),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG,url.toString()+postData);
                    //Log.i(TAG, line);
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
                if(!isPoints)
                    progressBar.setVisibility(View.INVISIBLE);
                // historyList.remove(historyList.size()-1);
                if(!args.equalsIgnoreCase("limit") && !args.equalsIgnoreCase("Error")) {
                    addItems(args);
                }
                else if(!isPoints)
                    noPointsView.setVisibility(View.VISIBLE);
                else if(args.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }

        }.execute();
    }
    private void addItems(String jsonResult){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonObject.optJSONArray("points");

            for(int i = 0; i < jsonMainNode.length();i++){
                JSONObject jsonChild = jsonMainNode.getJSONObject(i);
                String restName = jsonChild.optString("restaurantName");
                String thumbDir = jsonChild.optString("thumbPhotoDir");
                String restID = jsonChild.optString("restID");
                String restEmail = jsonChild.optString("restEmail");
                int points = jsonChild.optInt("points");
                int pointsID = jsonChild.optInt("pointsID");
                pointsList.add(new Points(restName,restID,thumbDir,points,pointsID,restEmail));
                isPoints = true;
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            if(!isPoints)
                noPointsView.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    private void loadMoreHistory(int current_page){
        if(NetworkUtil.getConnectivityStatus(this))
            getPoints(current_page);
        else
            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_points, menu);
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
