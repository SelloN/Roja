package com.natech.roja.LogIn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Address> addressList;
    private ProgressDialog progressDialog;
    private AddressBookRV addressBookRV;
    public static AddressActivity addressActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        toolbarTV.setText("Address Book");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Button addAddressBtn = (Button)findViewById(R.id.addAddressBtn);
        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddAddress();
            }
        });
        setRecyclerview();
        new GetAddressBook("Loading Address Book...").execute();
        addressActivity = this;
    }

    void openAddAddress(){
        //startActivityForResult(Intent.createChooser(intent,"Select File"),1);

        Intent intent = new Intent(this,AddAddressActivity.class);
        startActivityForResult(intent,1);
       // startActivity(intent);
    }

    void openEditAddress(Bundle bundle){
        Intent intent = new Intent(this, EditAddressActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }

    void setRecyclerview(){
        addressList = new ArrayList<>();
        addressBookRV = new AddressBookRV(this,addressList);
        recyclerView = (RecyclerView)findViewById(R.id.addressRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(addressBookRV);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    class GetAddressBook extends AsyncTask<String, String, String>{

        private String message;
        public GetAddressBook(String message){
            this.message = message;
        }
        @Override
        protected void onPreExecute(){
            showProgressDialog(message);

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getAddressBook());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getUserId(), "UTF-8")+"="+
                        URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                        getString(CommonIdentifiers.getUserId(), null),"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("getBook","UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line =  reader.readLine();
                reader.close();
                Log.i("", "New Response : " + line);
                return line;

            }catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String arg)
        {
            hideProgressDialog();
            if(!arg.equalsIgnoreCase("Error")) {
                loadAddressBook(arg);
            }
            else if(arg.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }
    }

    void loadAddressBook(String json){
        try{
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("addressBook");
            for(int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                addressList.add(new Address(jsonChildNode.optString("addressID"),
                        jsonChildNode.optString("street"),
                        jsonChildNode.optString("complex"),
                        jsonChildNode.optString("province"),
                        jsonChildNode.optString("label")));
            }
            updateAdapter();


        }catch (JSONException e){
            //Snackbar.make(findViewById(R.id.snackbarPosition),
              //      "Something went wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, "Error" + e.toString(),
              //      Toast.LENGTH_SHORT).show();
            //Log.i("", e.toString());
        }

    }

    void deleteAddress(final String addressID){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Server.getAddressBook());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode(CommonIdentifiers.getAddressId(), "UTF-8")+"="+
                            URLEncoder.encode(addressID,"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("delete","UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line =  reader.readLine();
                    reader.close();
                    Log.i("", "New Response : " + line);

                }catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Address book empty", Snackbar.LENGTH_LONG).show();
                }

            }
        }).start();
    }

    void updateAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addressBookRV.notifyDataSetChanged();
            }
        });
    }

    void showProgressDialog(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(AddressActivity.this);
                progressDialog.setMessage(message);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    void hideProgressDialog()
    {
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                addressList.clear();
                new GetAddressBook("Refreshing Address Book...").execute();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_address, menu);
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
