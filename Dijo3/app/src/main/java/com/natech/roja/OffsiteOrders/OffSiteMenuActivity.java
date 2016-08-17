package com.natech.roja.OffsiteOrders;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.natech.roja.CheckOut.CheckOutActivity;
import com.natech.roja.MenuCategories.Category;
import com.natech.roja.MenuCategories.RVAdapter;
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

@SuppressWarnings({"ConstantConditions", "MismatchedQueryAndUpdateOfCollection"})
public class OffSiteMenuActivity extends AppCompatActivity {
    private RVAdapter adapter;
    private ProgressBar progressBar;
    private String restID;
    private String franchID;
    private final static  String  REST_ID = "restID",EXTRAS_FILE = "extras", CART_FILE = "CartList", TYPE = "type", REST_EMAIL = "restEmail",TABLE_ID = "tableID";
    private SharedPreferences.Editor idEditor, extrasEditor, editor, editorOrders;
    private static final String TAG = OffSiteMenuActivity.class.getSimpleName();
    private Dialog leaveTableDialog;
    private RelativeLayout noConnectionView;
    private Boolean isFranchise = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_site_menu);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        SharedPreferences idFile = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE);
        idEditor = idFile.edit();
        String restName = getIntent().getExtras().getString("restName");
        restID = getIntent().getExtras().getString("restID");



        Toolbar toolbar =  (Toolbar)findViewById(R.id.shadowToolBar);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText(restName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        noConnectionView = (RelativeLayout)findViewById(R.id.noNetwork);
        setRecyclerView();
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(CART_FILE,MODE_PRIVATE);
        SharedPreferences extrasSharedPreferences = this.getApplicationContext().getSharedPreferences(EXTRAS_FILE, MODE_PRIVATE);
        extrasEditor = extrasSharedPreferences.edit();
        editor = sharedPreferences.edit();
        SharedPreferences sharedPreferencesOrders = this.getApplicationContext().getSharedPreferences("OrderList",MODE_PRIVATE);
        franchID = getIntent().getExtras().getString(CommonIdentifiers.getFranchId());
        editorOrders = sharedPreferencesOrders.edit();
        idEditor.putString(CommonIdentifiers.getFranchId(),franchID).apply();

        if(!franchID.equalsIgnoreCase("3")){
            isFranchise = true;

            //Log.i(TAG,"Is franchise");
        }

        String oldRestID;
        if(idFile.contains(REST_ID))
            oldRestID = idFile.getString(REST_ID, "");
        else
            oldRestID = null;
        if(oldRestID != null){

            if(!oldRestID.equals(restID)){
                idEditor.putString(REST_ID,restID);
                idEditor.remove(CommonIdentifiers.getTableLogId());
                idEditor.putBoolean(CommonIdentifiers.getPreviousOffsite(),false);
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                editorOrders.apply();
                editor.apply();
                extrasEditor.apply();
                //Log.i(TAG," New Rest ID");
            }else
                showRestoreTableDialog();

        }else{
            idEditor.putString(REST_ID,restID);
        }
        idEditor.putString(REST_EMAIL,getIntent().getExtras().getString(REST_EMAIL,null));
        idEditor.putBoolean(CommonIdentifiers.getHomeDelivery(), getIntent().getExtras().getBoolean(CommonIdentifiers.getHomeDelivery()));
        idEditor.putString(CommonIdentifiers.getRestName(),restName);
        idEditor.putString(TABLE_ID,getIntent().getExtras().getString("offSiteCode"));
        idEditor.putString(CommonIdentifiers.getLoyaltySystem(),getIntent().getExtras().
                getString(CommonIdentifiers.getLoyaltySystem(), null));
        idEditor.apply();

        if(NetworkUtil.getConnectivityStatus(this))
            new GetEmail().execute();
        else {
            noConnectionView.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.snackbarPosition),
                    "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }


    }

    void showRestoreTableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage("Restore previous state?");
        builder.setPositiveButton("Restore",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                idEditor.putBoolean(CommonIdentifiers.getPreviousOffsite(),true);
                idEditor.putString(REST_ID, restID);
                idEditor.apply();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                idEditor.putString(REST_ID,restID);
                idEditor.remove(CommonIdentifiers.getTableLogId());
                idEditor.putBoolean(CommonIdentifiers.getPreviousOffsite(), false);
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                editorOrders.apply();
                editor.apply();
                extrasEditor.apply();
            }
        });
        builder.show();/*
        leaveTableDialog = new Dialog(this);
        leaveTableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveTableDialog.setContentView(R.layout.clear_cart_dialog);
        leaveTableDialog.setCanceledOnTouchOutside(false);
        TextView messageTV = (TextView)leaveTableDialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Restore State?");
        Button yesBtn = (Button)leaveTableDialog.findViewById(R.id.yesClear);
        Button noBtn = (Button)leaveTableDialog.findViewById(R.id.noClear);
        leaveTableDialog.show();
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idEditor.putString(REST_ID,restID);
                idEditor.remove(CommonIdentifiers.getTableLogId());
                idEditor.putBoolean(CommonIdentifiers.getPreviousOffsite(), false);
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                editorOrders.apply();
                editor.apply();
                extrasEditor.apply();
                leaveTableDialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idEditor.putBoolean(CommonIdentifiers.getPreviousOffsite(),true);
                idEditor.putString(REST_ID, restID);
                idEditor.apply();
                leaveTableDialog.dismiss();
            }
        });*/

    }

    void setRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainMenuRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new RVAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

    }
    void showLeaveTableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("Leave Store");
        builder.setMessage("Please Note: If you have any orders or items in the cart, they will be removed." +
                " However, orders sent through to the restaurant will not be affected");

        builder.setNegativeButton("Leave",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.clear();
                editorOrders.clear();
                idEditor.remove("tableID");
                idEditor.remove(REST_ID);
                idEditor.remove(CommonIdentifiers.getTableLogId());
                idEditor.apply();
                editorOrders.apply();
                extrasEditor.apply();
                editor.apply();
                finish();
            }
        });
        builder.setPositiveButton("Cancel",null);
        builder.show();
        /*
        leaveTableDialog = new Dialog(this);
        leaveTableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveTableDialog.setContentView(R.layout.clear_cart_dialog);
        leaveTableDialog.setCanceledOnTouchOutside(true);
        TextView messageTV = (TextView)leaveTableDialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Do You Want To Leave?");
        Button yesBtn = (Button)leaveTableDialog.findViewById(R.id.yesClear);
        Button noBtn = (Button)leaveTableDialog.findViewById(R.id.noClear);
        leaveTableDialog.show();
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveTableDialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.clear();
                editorOrders.clear();
                idEditor.remove("tableID");
                idEditor.remove(REST_ID);
                idEditor.remove(CommonIdentifiers.getTableLogId());
                idEditor.apply();
                editorOrders.apply();
                extrasEditor.apply();
                editor.apply();
                leaveTableDialog.dismiss();
                finish();
            }
        });*/
    }

    private class GetEmail extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getRestaurantDetails());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData;
                postData = URLEncoder.encode("restID", "UTF-8") + "=" + URLEncoder.encode(restID, "UTF-8");
                postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("email","UTF-8");
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
            if(!arg.equalsIgnoreCase("Error")){
                idEditor.putString(REST_EMAIL,arg);
                idEditor.commit();
                //Log.i(TAG," Rest EMail "+arg);
                if(NetworkUtil.getConnectivityStatus(OffSiteMenuActivity.this))
                    new GetCategories().execute();
                else{
                    noConnectionView.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }else if(arg.equalsIgnoreCase("Error")){
                progressBar.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
            }

        }
    }


    private class GetCategories extends AsyncTask<String, String, String>{


        @Override
        protected String doInBackground(String... strings) {
            try {
            URL url = new URL(Server.getMenu());
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            String postData;
            if(isFranchise) {
                postData = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("franchise", "UTF-8");
                postData += "&"+URLEncoder.encode("franchID","UTF-8")+"="+URLEncoder.encode(franchID,"UTF-8");
            }
            else {
                postData = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("standalone", "UTF-8");
                postData += "&"+URLEncoder.encode("restID","UTF-8")+"="+URLEncoder.encode(restID,"UTF-8");
            }

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
            progressBar.setVisibility(View.GONE);
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
                String picLink = jsonChildNode.optString("photoDir");
                String catID = jsonChildNode.optString("catID");
                int hasExtras = jsonChildNode.optInt("hasExtras");
                adapter.addItem(new Category(name,desc,
                        picLink,Integer.parseInt(catID),hasExtras,false));
            }
            updateAdapter();
        } catch (JSONException e) {
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }


    }
    void openCheckOut()
    {
        idEditor.putString(TYPE,"offSite");
        idEditor.commit();
        Intent intent = new Intent(this, CheckOutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type","offSite");
        intent.putExtras(bundle);
        startActivity(intent);

    }

    void updateAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showLeaveTableDialog();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_off_site_menu, menu);
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
                showLeaveTableDialog();
                return true;
            case R.id.check_out:
                openCheckOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
