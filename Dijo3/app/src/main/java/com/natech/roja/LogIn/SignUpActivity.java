package com.natech.roja.LogIn;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.natech.roja.HomeContent.WebViewActivity;
import com.natech.roja.MainActivity;
import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.dijo.backend.registration.Registration;


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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"SameParameterValue", "FieldCanBeLocal"})
public class SignUpActivity extends AppCompatActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String  EMAIL = "email";
    private static final String SENDER_ID = "296164418889";
    private GoogleCloudMessaging gcm;
    private static Registration regService = null;
    private static final String TAG = "SignUp Activity";

    private SharedPreferences credentials;
    private SharedPreferences.Editor editor;

    private EditText nameED, surnameED, password1ED, password2ED, emailED, keyED;
    private String name, surname, email, password, confirmKey,regid;
    private Boolean isFilled = false, isLength = false, isMatch = false, isEmail = false, isResend = false, isPrivacy = false;

    private ProgressDialog progressDialog;
    private Dialog dialog;
    private TextView messageTV, messageConfirmTV;

    private RelativeLayout signUpLayout, confirmationLayout;
    private boolean isUptodate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        nameED = (EditText)findViewById(R.id.nameED);
        surnameED = (EditText)findViewById(R.id.surnameED);
        password1ED = (EditText)findViewById(R.id.passowrd1ED);
        password2ED = (EditText)findViewById(R.id.passowrd2ED);
        emailED = (EditText)findViewById(R.id.emailED);
        keyED = (EditText)findViewById(R.id.confirmKeyED);
        Button signUpButton = (Button)findViewById(R.id.signUpBtn);
        Button submitButton = (Button)findViewById(R.id.confirmBtn);
        Button resendBtn = (Button)findViewById(R.id.resendBtn);
        Button privacyBtn = (Button)findViewById(R.id.privacyButton);
        messageTV = (TextView)findViewById(R.id.messageTVSign);
        messageConfirmTV = (TextView)findViewById(R.id.message);
        signUpLayout = (RelativeLayout)findViewById(R.id.fieldsLayout2);
        confirmationLayout = (RelativeLayout)findViewById(R.id.confirmationLayout);
        credentials = this.getSharedPreferences(AppSharedPreferences.getCredentialsFile(),MODE_PRIVATE);
        editor = credentials.edit();
        regid = credentials.getString(AppSharedPreferences.getRegistrationId(),"");

        CheckBox privacyCB = (CheckBox)findViewById(R.id.privacyCheckBox);
        loadDeviceEmail();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NetworkUtil.getConnectivityStatus(SignUpActivity.this))
                    getDetails();
                else
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageConfirmTV.setVisibility(View.GONE);
                if(!keyED.getText().toString().isEmpty()){
                    showProgressDialog("Completing Sign Up...");
                    confirmKey = keyED.getText().toString();
                    new ConfirmSecretKey().execute();
                }
            }
        });

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageConfirmTV.setVisibility(View.GONE);
                isResend = true;
                showProgressDialog("Resending Key...");
                new SendConfirmationEmail().execute();
            }
        });

        privacyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrivacy = b;
            }
        });

        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent webView = new Intent(SignUpActivity.this, WebViewActivity.class);
                Bundle b = new Bundle();
                b.putString("type","privacy");
                b.putString("link", Server.getPrivacy());
                webView.putExtras(b);
                startActivity(webView);
            }
        });


    }

    class CheckVersion extends AsyncTask<String, String, String>{


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

            if(args.equalsIgnoreCase("true")) {
                startRegistration();
            }else if(args.equalsIgnoreCase("false")){
                hideProgressDialog();
                setContentView(R.layout.activity_update);
            }else{
                Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                        "Something went wrong, please try again",
                        Snackbar.LENGTH_LONG).show();
            }

        }
    }

    private void loadDeviceEmail(){
        AccountManager accountManager = AccountManager.get(this);
        Account account = getAccount(accountManager);

        if(account != null)
            emailED.setText(account.name);

    }

    private static Account getAccount(AccountManager accountManager){

        Account[] accounts = accountManager.getAccountsByType("com.google");

        if(accounts.length > 0)
            return accounts[0];
        else
            return null;
    }

    void getDetails()
    {
        messageTV.setVisibility(View.GONE);
       if(!emailED.getText().toString().isEmpty() &&
               !nameED.getText().toString().isEmpty() &&
               !surnameED.getText().toString().isEmpty() &&
               !password1ED.getText().toString().isEmpty() &&
               !password2ED.getText().toString().isEmpty()){

           email = emailED.getText().toString();
           surname = surnameED.getText().toString();
           password = password1ED.getText().toString();
           name = nameED.getText().toString();
           isFilled = true;

           if(isValidEmail(email)){
               isEmail = true;
               if(password.length() >= 6) {
                   isLength = true;
                   isMatch = passwordMatches(password,password2ED.getText().toString());
                   if(!isMatch) {
                       isFilled = false;
                       isLength =false;
                       isMatch = false;
                       isEmail = false;
                       setMessage("Passwords Do Not Match");
                   }
               }
               else {
                   isFilled = false;
                   isLength =false;
                   isMatch = false;
                   isEmail = false;
                   setMessage("Password Must Be More Than 6 Characters");
               }
           }else {
               isFilled = false;
               isLength =false;
               isMatch = false;
               isEmail = false;
               setMessage("Please Enter A Valid Email Account");
           }
       }else {
           isFilled = false;
           isLength =false;
           isMatch = false;
           isEmail = false;
           setMessage("Please Fill All Fields");
       }

        if(isFilled && isLength && isMatch && isEmail){
            if(isPrivacy) {
                showProgressDialog("Signing Up...");
                new CheckVersion().execute();
                /*
                if(isUptodate)
                    startRegistration();
                else {
                    hideProgressDialog();
                    setContentView(R.layout.activity_update);
                }*/
                //new SendConfirmationEmail().execute();
            }else
                setMessage("Please Accept Our Privacy Policy");

        }


    }
    void setSuccessMessage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTV.setBackgroundColor(getResources().getColor(R.color.okColor));
                messageTV.setVisibility(View.VISIBLE);
                messageTV.setText("Details Ok");
            }
        });
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

    void setMessageKey(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageConfirmTV.setText(message);
                messageConfirmTV.setBackgroundColor(getResources().getColor(R.color.errorColor));
                messageConfirmTV.setVisibility(View.VISIBLE);
            }
        });
    }
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean passwordMatches(String pass1, String pass2)
    {
        return pass1.equals(pass2);
    }
    void startRegistration()
    {
        if(checkPlayServices())
        {
            //Log.i(TAG, "Google Play Services OK");
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = credentials.getString(AppSharedPreferences.getRegistrationId(),"");
            System.out.println(regid);
            if (regid.isEmpty())
            {
                registerInBackground();
            }else{
                //Log.i(TAG, "Reg ID Not Empty");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendRegistrationToServer();
                    }
                }).start();
            }
        }
        else
        {
            //Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    void showProgressDialog(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.setMessage(message);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }
    void showConfirmLayout(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                signUpLayout.setVisibility(View.GONE);
                confirmationLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    void hideProgressDialog() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

    }

    void showMessageDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new Dialog(SignUpActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.message_dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Button okBtn = (Button)dialog.findViewById(R.id.okDialog);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        finish();
                    }
                });

            }
        });

    }

    private class ConfirmSecretKey extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            try {
                BufferedReader reader;
                URL url = new URL(Server.getConfirmEmailKey());
                String postData = URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                postData += "&"+URLEncoder.encode("confirmKey","UTF-8")+"="+URLEncoder.encode(confirmKey,"UTF-8");
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i(TAG, "New Response : " + line);

                if (line != null) {

                    if (line.equalsIgnoreCase("Correct")) {
                        return line;
                    } else if(line.equalsIgnoreCase("Incorrect"))
                        return "Incorrect";
                    else
                        return "Error";

                }else
                    return "Error";

            } catch (Exception e) {

                hideProgressDialog();
                e.printStackTrace();
                return "Error";
                //Log.i(TAG, "Exception : " + e.printStackTrace());
            }
        }

        @Override
        protected void onPostExecute(String arg){
            if(!arg.equalsIgnoreCase("Error") && !arg.equalsIgnoreCase("Incorrect")){
                startRegistration();
                setSuccessMessage();

            }else if(arg.equalsIgnoreCase("Incorrect")){
                hideProgressDialog();
                setMessageKey("Incorrect Key. Please Try Again");
            }
        }
    }

    private class SendConfirmationEmail extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Log.i("","Sending email");
                BufferedReader reader;
                URL url = new URL(Server.getEmailConfirmation());
                String postData = URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                if(isResend)
                    postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("resend","UTF-8");
                else
                    postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("first","UTF-8");
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                //Log.i(TAG, "New Response : " + line);

                if (line != null) {

                    if (line.equalsIgnoreCase("Sent")) {
                        return line;
                    } else
                        return "Error";

                }else
                    return "Error";

            } catch (Exception e) {

                hideProgressDialog();
                e.printStackTrace();
                return "Error";
                //Log.i(TAG, "Exception : " + e.printStackTrace());
            }
        }
        @Override
        protected void onPostExecute(String arg){
            if(!arg.equalsIgnoreCase("Error")){
                hideProgressDialog();

                if(!isResend)
                    showConfirmLayout();

            }
        }
    }

    void registerInBackground()
    {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                if (regService == null) {
                    Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(), null)
                            // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                            // otherwise they can be skipped
                            .setRootUrl("https://dijo-991.appspot.com/_ah/api/");
                    // end of optional local run code

                    regService = builder.build();
                }
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(SignUpActivity.this);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    regService.register(regid).execute();

                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //Log.i(TAG, "onPostExecute : " + msg);

                if (!msg.equalsIgnoreCase("SERVICE_NOT_AVAILABLE")) {

                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("server_response", msg);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);

                }
            }
            // Define the Handler that receives messages from the thread and
            // update the progress
            private final Handler handler = new Handler() {

                public void handleMessage(Message msg) {

                    String aResponse = msg.getData().getString(
                            "server_response");

                    if ((null != aResponse)) {

                        //Log.i(TAG, "	sendRegistrationIdToBackend();");

                        //Log.d(TAG, "After");

                       new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(NetworkUtil.getConnectivityStatus(SignUpActivity.this))
                                    sendRegistrationToServer();
                                else
                                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                                            "Process interrupted due to loss of internet connectivity",
                                            Snackbar.LENGTH_LONG).show();
                            }
                        }).start();
                        //sendRegistrationToServer();

                    }
                }
            };
        }.execute(null, null, null);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */
    void sendRegistrationToServer()
    {
        try {
            //Log.i("","Sending to server");
            BufferedReader reader;
            URL url = new URL(Server.getSaveUser());
            String postData = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8");
            postData += "&"+URLEncoder.encode("surname","UTF-8")+"="+URLEncoder.encode(surname,"UTF-8");
            postData += "&"+URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
            postData += "&"+URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
            postData += "&"+URLEncoder.encode("reg_id","UTF-8")+"="+URLEncoder.encode(regid,"UTF-8");
            postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("user","UTF-8");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
            outputStreamWriter.write(postData);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            reader.close();
            //Log.i(TAG, "Reg to server "+regid);
            //Log.i(TAG, "New Response : " + line);
            //Log.i(TAG, "URL : " + postData);

            if (line != null) {

                if (line
                        .equalsIgnoreCase("Username already registered")) {
                    //Log.i(TAG,"Already registered");

                    hideProgressDialog();
                    showMessageDialog();

                } else {
                    if (line
                            .equalsIgnoreCase("New User Registered successfully")) {
                        editor.putString(AppSharedPreferences.getRegistrationId(),regid);
                        editor.putString(EMAIL,email);
                        editor.commit();
                        //Log.i(TAG,"Registered to server");
                        hideProgressDialog();
                        startMainActivity();

                    }
                }

            }

        } catch (Exception e) {

            hideProgressDialog();
            e.printStackTrace();
            //Log.i(TAG, "Exception : " + e.printStackTrace());
        }

    }

    void startMainActivity()
    {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        Bundle bundle = new Bundle();
        //Log.i("","+++++ "+email);
        bundle.putString(EMAIL,email);
        bundle.putBoolean("SignUp",true);
        mainIntent.putExtras(bundle);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Log.i(TAG, "No Google Play Services...Get it from the store.");
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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
