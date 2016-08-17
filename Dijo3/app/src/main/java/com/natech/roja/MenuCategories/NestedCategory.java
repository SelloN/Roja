package com.natech.roja.MenuCategories;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.CheckOut.CheckOutActivity;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class NestedCategory extends AppCompatActivity {
    private RVAdapter adapter;
    private ProgressBar progressBar;
    private final static  String  REST_ID = "restID",EXTRAS_FILE = "extras", CART_FILE = "CartList", TYPE = "type",
            REST_EMAIL = "restEmail",TABLE_ID = "tableID";
    private SharedPreferences.Editor idEditor, extrasEditor, editor, editorOrders;
    private static final String TAG = NestedCategory.class.getSimpleName();
    private int menu_cat_id;
    private RelativeLayout noNetwork;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_category);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTV.setText(getIntent().getExtras().getString("category"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        toolbarLayout.startAnimation(toolbarAnim);
        menu_cat_id = getIntent().getExtras().getInt(CommonIdentifiers.getMenuCatId());
        if(getIntent().getExtras().getString("class").equalsIgnoreCase("MainMenu"))
            type = "onSite";
        else
            type = "offSite";


        //idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
        Log.i(TAG,"menu cat id "+menu_cat_id);
        setRecyclerView();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noNetwork = (RelativeLayout)findViewById(R.id.noNetwork);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(NetworkUtil.getConnectivityStatus(NestedCategory.this))
                            new GetCategories().execute();
                        else {
                            noNetwork.setVisibility(View.VISIBLE);
                            Snackbar.make(findViewById(R.id.snackbarPositionList), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }).start();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    void setRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainMenuRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new RVAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
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

    private class GetCategories extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute(){
            showProgressBar();
        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getMenu());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getMenuCatId(), "UTF-8") + "=" +
                        URLEncoder.encode(String.valueOf(menu_cat_id), "UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getMenuType(),"UTF-8")+"="+URLEncoder.encode("nested","UTF-8");
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
            hideProgressBar();
            if(!arg.equalsIgnoreCase("Error")){
                drawMenu(arg);
            }else if(arg.equalsIgnoreCase("Error")){
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    void drawMenu(String jsonResult) {

        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("category");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String name = jsonChildNode.optString("categoryName");
                String desc = jsonChildNode.optString("description");
                int catExtraID = jsonChildNode.optInt("catExtraID");
                adapter.addItem(new Category(name,desc,
                        null,catExtraID,0,true));
            }
            updateAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
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
    void openCheckOut()
    {
        getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).edit()
                .putString(CommonIdentifiers.getType(), type).commit();
        Intent intent = new Intent(this, CheckOutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type",type);
        intent.putExtras(bundle);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nested_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                finish();
                return true;
            case R.id.check_out:
                openCheckOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
