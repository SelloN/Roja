package com.natech.roja.LogIn;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


import com.natech.roja.MainActivity;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

@SuppressWarnings({"TryWithIdenticalCatches", "ConstantConditions", "SameParameterValue"})
public class LogInActivity extends AppCompatActivity {

    private Button logInBtn, signUpBtn;
    private TextView messageTV;
    private EditText emailED;
    public EditText passwordED;
    private CheckBox rememberCB;
    private static final String  EMAIL = "email", PASSWORD = "password",
            REMEMBER_ME = "rememberMe", LOG_STATE = "logState";
    private SharedPreferences credentialsFile, idFile;
    private SharedPreferences.Editor editor, idEditor;
    private static Boolean isChecked = false, isAutoLogIn = false,isRemember = false;
    private String email, password,userID;
    private ProgressDialog progressDialog;
    private static final String TAG = "LogIn Activity";
    public static LogInActivity logInActivity;
    private boolean isUptodate = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
        credentialsFile = getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE);
        editor = credentialsFile.edit();
        idEditor = idFile.edit();


        if(idFile.contains(LOG_STATE)){
            if(idFile.getBoolean(LOG_STATE,false) && credentialsFile.getBoolean(REMEMBER_ME,false)){
                setContentView(R.layout.splash_screen);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (NetworkUtil.getConnectivityStatus(LogInActivity.this)) {
                            new CheckVersion(1).execute();
                           /* checkVersion();
                            if(isUptodate) {
                                email = credentialsFile.getString(EMAIL, null);
                                password = credentialsFile.getString(PASSWORD, null);
                                isChecked = true;
                                isAutoLogIn = true;
                                checkCredentials();
                            }else {
                                setContentView(R.layout.activity_update);

                            }*/
                        } else {
                            contentSetUp();
                            Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else
                contentSetUp();
        }else
            contentSetUp();
    }



    void contentSetUp(){
        runOnUiThread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                setContentView(R.layout.activity_log_in);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
                }
                logInBtn = (Button)findViewById(R.id.logInBtn);
                signUpBtn = (Button)findViewById(R.id.signUpBtnLog);
                messageTV = (TextView)findViewById(R.id.messageTV);
                emailED = (EditText)findViewById(R.id.emailLogED);
                passwordED = (EditText)findViewById(R.id.passwordLogED);
                rememberCB = (CheckBox)findViewById(R.id.rememberCheckBox);
                isRemember = credentialsFile.getBoolean(REMEMBER_ME,false);



                if(isRemember)
                    autoLogIn();

                signUpBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent signUpIntent = new Intent(LogInActivity.this, SignUpActivity.class);
                        startActivity(signUpIntent);
                    }
                });
                logInBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isAutoLogIn = false;
                                if(NetworkUtil.getConnectivityStatus(LogInActivity.this))
                                    getCredentials();
                                else
                                    Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                            }
                        });


                    }
                });

                rememberCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        isChecked = checked;
                    }
                });

                Button forgotBtn = (Button)findViewById(R.id.forgotButton);
                forgotBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openForgotPasswordActivity();
                    }
                });
                logInActivity = LogInActivity.this;

            }
        });

    }

    class CheckVersion extends AsyncTask<String, String, String>{
        private int level;

        public CheckVersion(int level){
            this.level = level;
        }

        @Override
        public void onPreExecute(){

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                PackageInfo packageInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(),0);
                URL url = new URL(Server.getAppVersion());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                String postData = URLEncoder.encode("versionCode","UTF-8")+"="+
                        URLEncoder.encode(String.valueOf(packageInfo.versionCode),"UTF-8");
                connection.setDoOutput(true);
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                return line;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "error";
        }

        @Override
        public void onPostExecute(String args){

            if(args.equalsIgnoreCase("true")){
                if(level == 1) {
                    email = credentialsFile.getString(EMAIL, null);
                    password = credentialsFile.getString(PASSWORD, null);
                    isChecked = true;
                    isAutoLogIn = true;
                    checkCredentials();
                }else{
                    checkCredentials();
                }

            }else if(args.equalsIgnoreCase("false")){
                if(level == 2)
                    hideProgressDialog();
                setContentView(R.layout.activity_update);
                Button updateBtn = (Button)findViewById(R.id.updateBtn);
                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" +
                                                getPackageName())));
                            }
                        }).start();

                    }
                });
            }else{
                Snackbar.make(findViewById(R.id.snackbarPosition), "Something went wrong, please try again",
                        Snackbar.LENGTH_LONG).show();
            }

        }
    }


    void openForgotPasswordActivity(){
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    void showProgressDialog(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(LogInActivity.this);
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

    void getCredentials()
    {
        messageTV.setVisibility(View.GONE);
        email = emailED.getText().toString();
        password = passwordED.getText().toString();

        if(!email.isEmpty() && !password.isEmpty())
        {
            showProgressDialog("Logging in...");
            new CheckVersion(2).execute();
            //checkVersion();
            /*if(isUptodate)
                checkCredentials();
            else {
                hideProgressDialog();
                setContentView(R.layout.activity_update);
            }*/
        }
        else {
            messageTV.setText("Please Fill All Fields");
            messageTV.setVisibility(View.VISIBLE);
        }
    }
    /*
       Backup the user's credentials for easy access
     */
    void backUpCredentials()
    {
        editor.putString(EMAIL,email);
        editor.putString(PASSWORD,password);
        editor.putBoolean(REMEMBER_ME,true);
        editor.commit();
    }
    void setMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTV.setText(message);
                messageTV.setVisibility(View.VISIBLE);
            }
        });
    }

    void checkCredentials()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //HttpURLConnection conn = null;
                    URL url = new URL(Server.getLogIn());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    String postData = URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                    postData += "&"+URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                    connection.setRequestProperty("Accept","*/*");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG,"Response "+line);

                    if(line != null)
                    {
                        if(line.contains("user found"))
                        {
                            userID = line.substring(10);
                            //Log.i(TAG,"user found");
                            if(isChecked)
                                backUpCredentials();
                            else if(!isChecked)
                                clearCredentials();

                            idEditor.putString(CommonIdentifiers.getUserId(),userID);
                            idEditor.putString(EMAIL,email);
                            idEditor.commit();
                            if(!isAutoLogIn)
                                hideProgressDialog();
                            startMainActivity();
                        }
                        else if(line.equalsIgnoreCase("wrong credentials"))
                        {
                            //Log.i(TAG,"wrong credentials");
                            if(!isAutoLogIn) {
                                hideProgressDialog();
                                setMessage("Incorrect Details");
                            }else
                                contentSetUp();
                        }
                        else if(line.equalsIgnoreCase("user not found"))
                        {
                            //Log.i(TAG,"user not found");
                            if(!isAutoLogIn) {
                                hideProgressDialog();
                                setMessage("Not Registered. Please Sign Up");
                            }
                            else
                                contentSetUp();
                        }
                    }
                } catch (MalformedURLException e) {
                    hideProgressDialog();
                    e.printStackTrace();
                    setMessage("Something Went Wrong. Please Try Again");
                } catch (IOException e) {
                    hideProgressDialog();
                    e.printStackTrace();
                    setMessage("Something Went Wrong. Please Try Again");

                }
            }
        }).start();
    }


    void startMainActivity()
    {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        Bundle bundle = new Bundle();
        bundle.putString(EMAIL,idFile.getString(EMAIL,null));
        mainIntent.putExtras(bundle);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    void autoLogIn()
    {
        email = credentialsFile.getString(EMAIL,"");
        password = credentialsFile.getString(PASSWORD,"");
        emailED.setText(email);
        passwordED.setText(password);
        rememberCB.setChecked(true);
        isChecked = true;
    }

    void clearCredentials()
    {
        editor.remove(EMAIL);
        editor.remove(PASSWORD);
        editor.putBoolean(REMEMBER_ME,false);
        editor.commit();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_log_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
