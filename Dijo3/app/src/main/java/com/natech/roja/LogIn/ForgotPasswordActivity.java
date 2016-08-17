package com.natech.roja.LogIn;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private RelativeLayout emailLayout, resetPasswordLayout;
    private EditText emailED, resetKeyED, password1ED, password2ED;
    private TextView message1TV, message2TV;
    private ProgressDialog progressDialog;
    private Boolean isResend = false;
    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();
    private String email, resetKey,password1, password2;
    private Dialog dialog;
    private ImageView logo1, logo2;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        emailLayout = (RelativeLayout)findViewById(R.id.emailLayout);
        resetPasswordLayout = (RelativeLayout)findViewById(R.id.resetPasswordLayout);

        emailED = (EditText)findViewById(R.id.emailED);
        resetKeyED = (EditText)findViewById(R.id.resetKeyED);
        password1ED = (EditText)findViewById(R.id.password1ED);
        password2ED = (EditText)findViewById(R.id.password2ED);

        message1TV = (TextView)findViewById(R.id.message);
        message2TV = (TextView)findViewById(R.id.message2);

        logo1 = (ImageView)findViewById(R.id.logo1);
        logo2 = (ImageView)findViewById(R.id.logo2);

        Button resetKeyBtn = (Button)findViewById(R.id.resetKeyBtn);
        Button resetBtn = (Button)findViewById(R.id.resetBtn);
        Button resendKeyBtn = (Button)findViewById(R.id.resendResetKeyBtn);

        resetKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!emailED.getText().toString().isEmpty()) {
                    email = emailED.getText().toString();
                    if (isValidEmail(email)){
                        if(NetworkUtil.getConnectivityStatus(ForgotPasswordActivity.this))
                            new SendResetKey().execute();
                        else
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "No Internet Connection", Snackbar.LENGTH_LONG).show();
                    }else
                        setMessage1TV("Please enter a valid email");

                }

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!password1ED.getText().toString().isEmpty()
                        && !password2ED.getText().toString().isEmpty()
                        && !resetKeyED.getText().toString().isEmpty()){
                    password1 = password1ED.getText().toString();
                    password2 = password2ED.getText().toString();
                    resetKey = resetKeyED.getText().toString();

                    if(password1.equals(password2)){
                        if(password1.length() >= 6)
                            new ResetPassword().execute();
                        else
                            setMessage2TV("Password Not Long Enough");
                    }else
                        setMessage2TV("Passwords Do Not Match");
                }
            }
        });

        resendKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isResend = true;
                if(NetworkUtil.getConnectivityStatus(ForgotPasswordActivity.this))
                    new SendResetKey().execute();
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "No Internet Connection", Snackbar.LENGTH_LONG).show();

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

    private class ResetPassword extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            showProgressDialog("Resetting Password...");
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                BufferedReader reader;
                URL url = new URL(Server.getResetPassword());
                String postData = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                postData += "&"+URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password1,"UTF-8");
                postData += "&"+URLEncoder.encode("confirmKey","UTF-8")+"="+URLEncoder.encode(resetKey,"UTF-8");
                postData += "&"+URLEncoder.encode(CommonIdentifiers.getType(),"UTF-8")+"="+URLEncoder.encode("user","UTF-8");
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
                    return line;

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
            hideProgressDialog();
            if(arg.equalsIgnoreCase("Updated")){
                showMessageDialog();
            }else if(arg.equalsIgnoreCase("Incorrect Key"))
                setMessage2TV("Incorrect Key. Please Try Again");
            else
                showSnackBar();
        }
    }

    private class SendResetKey extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute(){
            showProgressDialog("Please Wait...");
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                //Log.i("", "Sending email");
                BufferedReader reader;
                URL url = new URL(Server.getEmailConfirmation());
                String postData = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email,"UTF-8");
                if(isResend)
                    postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("resend","UTF-8");
                else
                    postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("forgot","UTF-8");
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

                    if (line.equalsIgnoreCase("Sent"))
                        return line;
                    else if(line.equalsIgnoreCase("User not found"))
                        return "Not Found";
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
        protected void onPostExecute(String args){
            hideProgressDialog();

            if(args.equalsIgnoreCase("Sent")) {
                if (!isResend)
                    showResetPasswordLayout();
            }else if(args.equalsIgnoreCase("Not Found")){
                setMessage1TV("E-mail not registered");
            }else
                showSnackBar();

        }
    }

    void showMessageDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new Dialog(ForgotPasswordActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.message_dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                TextView text = (TextView)dialog.findViewById(R.id.messageDialogTV);
                text.setText("Password Reset. Log In With Your New Password");
                dialog.show();

                Button okBtn = (Button) dialog.findViewById(R.id.okDialog);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LogInActivity.logInActivity.passwordED.setText("");
                        ForgotPasswordActivity.this.
                                getSharedPreferences(AppSharedPreferences.getCredentialsFile(),
                                        MODE_PRIVATE).edit().remove("password").commit();
                        dialog.dismiss();
                        finish();
                    }
                });

            }
        });
    }

    private void showSnackBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Something went wrong. Pleas try again", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showResetPasswordLayout(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailLayout.setVisibility(View.GONE);
                logo1.setVisibility(View.GONE);
                logo2.setVisibility(View.VISIBLE);
                resetPasswordLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showProgressDialog(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                progressDialog.setMessage(message);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    private void hideProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

   private void setMessage1TV(final String message){
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               message1TV.setText(message);
               message1TV.setBackgroundColor(getResources().getColor(R.color.errorColor));
           }
       });
   }

    private void setMessage2TV(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                message2TV.setText(message);
                message2TV.setBackgroundColor(getResources().getColor(R.color.errorColor));
                message2TV.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forgot_password, menu);
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
