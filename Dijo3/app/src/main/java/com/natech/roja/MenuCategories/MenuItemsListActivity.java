package com.natech.roja.MenuCategories;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class MenuItemsListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayout progress;
    private int catID;
    public static Activity menuListActivty;
    private SharedPreferences idFile;
    private RelativeLayout noNetwork;
    private RelativeLayout noSpecials;
    private String type;
    private Boolean hasItems = false, isNested;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_items_list);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        menuListActivty = this;
        Toolbar toolbar = (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTV.setText(getIntent().getExtras().getString("category"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        toolbarLayout.startAnimation(toolbarAnim);
        catID = getIntent().getExtras().getInt("catID");
        isNested = getIntent().getExtras().getString(CommonIdentifiers.getMenuType(),null).equalsIgnoreCase("nested");
        if(getIntent().getExtras().getString("class").equalsIgnoreCase("MainMenu"))
            type = "onSite";
        else
            type = "offSite";
        idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);

        //Log.i("","++++ "+catID);
        setupRecyclerView();
        progress  = (LinearLayout)findViewById(R.id.progress);
        noNetwork = (RelativeLayout)findViewById(R.id.noNetwork);
        noSpecials = (RelativeLayout)findViewById(R.id.noSpecialsTV);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(NetworkUtil.getConnectivityStatus(MenuItemsListActivity.this))
                            new GetMenuItems().execute();
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

    void setupRecyclerView()
    {
        recyclerView = (RecyclerView)findViewById(R.id.menuListRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    private class GetMenuItems extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            showProgressBar();
        }
        @Override
        protected String doInBackground(String...params){
            try {
                URL url = new URL(Server.getMenuItems());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData =  URLEncoder.encode("catID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(catID), "UTF-8");
                if(!isNested)
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8") + "=" + URLEncoder.encode("main", "UTF-8");
                else
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(), "UTF-8") + "=" + URLEncoder.encode("nested", "UTF-8");

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i("",line);
                //Log.i("",postData);
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
        protected void onPostExecute(String result) {
            hideProgressBar();
            if(!result.equalsIgnoreCase("Error"))
                ListDrawer(result);
            else if(result.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPositionList),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    void hideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.INVISIBLE);
            }
        });
    }

    void showProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
    }

    void ListDrawer(String jsonResult) {
        List<com.natech.roja.MenuCategories.MenuItem> menuItems = new ArrayList<>();



        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("menuItems");

            /*
                This loop gets the data from the Jason Array that stores
                the data from the Mysql database
             */
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String menuItem = jsonChildNode.optString("menuItem");
                String desc = jsonChildNode.optString("itemDescription");
                String itemPrice = jsonChildNode.optString("itemPrice");
                String menuID = jsonChildNode.optString("menuID");
                int hasExtras = jsonChildNode.optInt("hasExtras");
                int isHalal = jsonChildNode.optInt("halal");
                int isVeg = jsonChildNode.optInt("veg");
                //Log.i("MENU","menuID "+menuID);
                menuItems.add(new com.natech.roja.MenuCategories.MenuItem(menuItem, desc, Double.parseDouble(itemPrice),
                        Integer.parseInt(menuID), hasExtras,isHalal,isVeg));
                hasItems = true;
            }
            MenuListRVAdapter menuListRVAdapter = new MenuListRVAdapter(this, menuItems);
            recyclerView.setAdapter(menuListRVAdapter);
        } catch (JSONException e) {
            if(!hasItems)
                showNoSpecials();
        }


    }
    private void showNoSpecials(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noSpecials.setVisibility(View.VISIBLE);
            }
        });
    }

    void openCheckOut()
    {
        SharedPreferences.Editor idEditor = idFile.edit();
        idEditor.putString(CommonIdentifiers.getType(), type);
        idEditor.apply();
        Intent intent = new Intent(this, CheckOutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type",type);
        intent.putExtras(bundle);
        startActivity(intent);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu_items_list, menu);
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
            case R.id.check_out:
                openCheckOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
