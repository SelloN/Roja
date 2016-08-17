package com.natech.roja.LogIn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class EditAddressActivity extends AppCompatActivity {

    private EditText labelET, streetET, complexET;
    private String province = "Select Province",label,street,complex = null;
    private TextView messageTV;
    private ProgressDialog progressDialog;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private AppCompatSpinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        toolbarTV.setText("Edit Address");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setSpinner();
        labelET = (EditText)findViewById(R.id.labelET);
        streetET = (EditText)findViewById(R.id.streetNameET);
        complexET = (EditText)findViewById(R.id.complexET);
        messageTV = (TextView)findViewById(R.id.messageTV);
        Button saveBtn = (Button)findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NetworkUtil.getConnectivityStatus(EditAddressActivity.this))
                    getData();
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection",
                            Snackbar.LENGTH_LONG).show();
            }
        });
        setDetails();
    }

    void setDetails(){
        labelET.setText(getIntent().getExtras().getString(CommonIdentifiers.getLabel()));
        streetET.setText(getIntent().getExtras().getString(CommonIdentifiers.getStreetAddress()));
        if(!getIntent().getExtras().getString(CommonIdentifiers.getComplex()).equalsIgnoreCase(" ") &&
                !getIntent().getExtras().getString(CommonIdentifiers.getComplex()).equalsIgnoreCase("-"))
            complexET.setText(getIntent().getExtras().getString(CommonIdentifiers.getComplex()));

        for(int x = 0; x <arrayAdapter.getCount(); x++){
            if(arrayAdapter.getItem(x).toString().equalsIgnoreCase(getIntent().getExtras().
                    getString(CommonIdentifiers.getProvince()))){
                spinner.setSelection(x);
            }
        }
    }

    void setSpinner(){
        spinner = (AppCompatSpinner)findViewById(R.id.provinceSpinner);
        arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.province_array, R.layout.spinner_item2);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.i(TAG, "SPinner== " + adapterView.getItemAtPosition(i));
                province = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void showProgressDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(EditAddressActivity.this);
                progressDialog.setMessage("Saving Address...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    void hideProgressDialog()
    {
        progressDialog.dismiss();
    }
    void setMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTV.setText(message);
                messageTV.setVisibility(View.VISIBLE);
            }
        });
    }

    void getData(){
        messageTV.setVisibility(View.GONE);
        if(!labelET.getText().toString().isEmpty() && !streetET.getText().toString().isEmpty() &&
                !province.equalsIgnoreCase("Select Province")){
            label = labelET.getText().toString();
            street = streetET.getText().toString();

            if(!complexET.getText().toString().isEmpty())
                complex = complexET.getText().toString();
            else
                complex = "-";

            new SaveChanges().execute();

        }else if(labelET.getText().toString().isEmpty()){
            setMessage("Please enter label");
        }else if(streetET.getText().toString().isEmpty()){
            setMessage("Please enter address");
        }else if(province.equalsIgnoreCase("Select Province")){
            setMessage("Please select province");
        }

    }

    class SaveChanges extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getAddressBook());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getStreetAddress(), "UTF-8")+"="+
                        URLEncoder.encode(street,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("update","UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getComplex(),"UTF-8")+"="+URLEncoder.encode(complex,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getProvince(),"UTF-8")+"="+URLEncoder.encode(province,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getLabel(),"UTF-8")+"="+URLEncoder.encode(label,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getAddressId(), "UTF-8")+"="+
                        URLEncoder.encode(getIntent().getExtras().getString(CommonIdentifiers.getAddressId()),"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line =  reader.readLine();
                reader.close();
                Log.i("", "New Response : " + line);
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
        protected void onPostExecute(String args){
            hideProgressDialog();
            if(args.equalsIgnoreCase("updated")){
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }else
                Snackbar.make(findViewById(R.id.snackbarPosition), "Something went wrong. Please try again",
                        Snackbar.LENGTH_LONG).show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_edit_address, menu);
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
