package com.natech.roja.LogIn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.MainActivity;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@SuppressWarnings({"ALL", "ConstantConditions"})
public class EditProfileActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView toolbarTV;
    String name, surname, oldPassword, newPassword1,newPassword2 ;
    EditText nameED, surnameED, oldPasswordED, newPassword1ED, newPassword2ED;
    TextView messageTV;
    private static String TAG = "Edit Profile Activity";
    Boolean isFilled = false, isLength = false, isMatch = false, isPasswords = false;
    String userID;
    SharedPreferences idFile;
    ProgressDialog progressDialog;
    ImageView profilePic;
    Bitmap proPic;
    Animation toolbarAnim;
    Button saveBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar2);
        toolbarTV = (TextView)findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        final Animation contentAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        toolbar.startAnimation(toolbarAnim);

        final RelativeLayout profileContainer = (RelativeLayout)findViewById(R.id.profileContainer);

        toolbarTV.setText("Edit Profile");
        name = getIntent().getExtras().getString("name");
        surname = getIntent().getExtras().getString("surname");
        String picDir = getIntent().getExtras().getString("picDir");
        nameED = (EditText)findViewById(R.id.nameEDProfile);
        surnameED = (EditText)findViewById(R.id.surnameEDProfile);
        oldPasswordED = (EditText)findViewById(R.id.oldPasswordED);
        newPassword1ED = (EditText)findViewById(R.id.newPassword1ED);
        newPassword2ED = (EditText)findViewById(R.id.newPassword2ED);
        messageTV = (TextView)findViewById(R.id.messageTVProfile);
        profilePic = (ImageView)findViewById(R.id.profilePicEdit);
        nameED.setText(name);
        surnameED.setText(surname);
        idFile = getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE);
        userID = idFile.getString(CommonIdentifiers.getUserId(),"");
        saveBtn = (Button)findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isPasswords)
                    getAllDetails();
                else
                    getNameDetails();
            }
        });
        final Button passwordBtn = (Button)findViewById(R.id.passwordBtn);
        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordFields();
                passwordBtn.setVisibility(View.GONE);
            }
        });

        if(!picDir.equalsIgnoreCase("none"))
            Picasso.with(this).load(picDir).into(profilePic);
        else
            profilePic.setImageResource(R.drawable.user);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                profilePic.setVisibility(View.VISIBLE);
                profileContainer.setVisibility(View.VISIBLE);
                profilePic.startAnimation(contentAnim);
                profileContainer.startAnimation(contentAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
    void showProgressDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(EditProfileActivity.this);
                progressDialog.setMessage("Updating...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    void showPicProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(EditProfileActivity.this);
                progressDialog.setMessage("Uploading Photo...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }


    void hideProgressDialog()
    {
        progressDialog.dismiss();
    }

    void showPasswordFields(){

        oldPasswordED.setVisibility(View.VISIBLE);
        newPassword1ED.setVisibility(View.VISIBLE);
        newPassword2ED.setVisibility(View.VISIBLE);



        isPasswords = true;
    }

    void selectImage(){
        final CharSequence[] options = {"Take Photo","Choose From Gallery","Cancel"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(options[i].equals("Take Photo")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,0);

                }else if(options[i].equals("Choose From Gallery")){

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Select File"),1);

                }else if(options[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });

        builder.show();
    }
    void handleGalleryImage(Intent data){
        Uri imageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        @SuppressWarnings("deprecation") Cursor cursor = managedQuery(imageUri,projection,null,null,null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        Bitmap b = BitmapFactory.decodeFile(selectedImagePath);
        proPic = Bitmap.createScaledBitmap(b,200,200,false);
        File destination = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".png");
        FileOutputStream fileOutputStream;
        try {
            //noinspection ResultOfMethodCallIgnored
            destination.createNewFile();
            fileOutputStream = new FileOutputStream(destination);
            proPic.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new UploadPic().execute(destination.getPath());
        //postPic(destination.getPath());

    }

    void setProfilePic(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profilePic.setImageBitmap(proPic);
                MainActivity.mainActivity.getUserDetails();
            }
        });
    }

    class UploadPic extends AsyncTask<String,String,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String fileName = strings[0];
            HttpURLConnection conn;
            File sourceFile = new File(fileName);
            String boundary = "*****";
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist :" + fileName);
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(Server.getUploadPhoto());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dataOutputStream = new DataOutputStream(conn.getOutputStream());
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=" +"IMG_"+ userID+".png" + "" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dataOutputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    int responseCode = conn.getResponseCode();
                    String response = conn.getResponseMessage();

                    Log.i("", "New Response: " + response + " code = " + responseCode);

                    if (responseCode == 200) {

                    }
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            new UpdatePhotoLink().execute(Server.getUploadPhotoLink(userID));
        }
    }

    class UpdatePhotoLink extends AsyncTask<String,String,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            try{
                URL url = new URL(Server.getUpdateUserDetails());
                URLConnection connection = url.openConnection();
                String postData = URLEncoder.encode("photoDir","UTF-8")+"="+URLEncoder.encode(strings[0],"UTF-8");
                postData += "&"+URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                postData += "&"+URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("photo", "UTF-8");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                Log.i(TAG, "Response " + line);

                if(line != null){
                    if(line.equalsIgnoreCase("User Updated")){
                        hideProgressDialog();
                        setProfilePic();
                    }
                    else{
                        hideProgressDialog();
                        setMessage("Upload Failed. Please Try Again");}
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    void handleCameraImage(Intent data){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        proPic = (Bitmap)data.getExtras().get("data");
        proPic.compress(Bitmap.CompressFormat.PNG, 100,bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".png");
        FileOutputStream fileOutputStream;
        try {
            //noinspection ResultOfMethodCallIgnored
            destination.createNewFile();
            fileOutputStream = new FileOutputStream(destination);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new UploadPic().execute(destination.getPath());
        //postPic(destination.getPath());
        //profilePic.setImageBitmap(thumbNail);
    }

    void getNameDetails(){
        if(!nameED.getText().toString().isEmpty() &&
                !surnameED.getText().toString().isEmpty()){
            name = nameED.getText().toString();
            surname = surnameED.getText().toString();
            isFilled = true;
        } else {
            isFilled = false;
            setMessage("Please Fill In All Fields");
        }

        if(isFilled) {
            showProgressDialog();
            updateUserDetails();
        }
    }

    void getAllDetails(){
        if(!nameED.getText().toString().isEmpty() &&
                !surnameED.getText().toString().isEmpty() &&
                !oldPasswordED.getText().toString().isEmpty() &&
                !newPassword1ED.getText().toString().isEmpty() &&
                !newPassword2ED.getText().toString().isEmpty()){
            name = nameED.getText().toString();
            surname = surnameED.getText().toString();
            oldPassword = oldPasswordED.getText().toString();
            newPassword1 = newPassword1ED.getText().toString();
            newPassword2 = newPassword2ED.getText().toString();
            isFilled = true;

            if(newPassword1.length() >= 6) {
                isLength = true;
                if(matchNewPassword())
                    isMatch = true;
                else {
                    isFilled = false;
                    isLength =false;
                    isMatch = false;
                    setMessage("New Password Does Not Match");
                }
            }else {
                isFilled = false;
                isLength =false;
                isMatch = false;
                setMessage("New Password Not Long Enough");
            }

        }
        else {
            isFilled = false;
            isLength =false;
            isMatch = false;
            setMessage("Please Fill In All Fields");
        }

        if(isFilled && isLength && isMatch) {
            showProgressDialog();
            updateUserDetails();
        }

    }



    void setMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTV.setBackgroundColor(getResources().getColor(R.color.errorColor));
                messageTV.setVisibility(View.VISIBLE);
                messageTV.setText(message);
            }
        });
    }
    void setSuccessMessage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTV.setBackgroundColor(getResources().getColor(R.color.okColor));
                messageTV.setVisibility(View.VISIBLE);
                messageTV.setText("Details Updated");
                MainActivity.mainActivity.getUserDetails();
            }
        });
    }

    Boolean matchNewPassword(){
        return newPassword1.equals(newPassword2);

    }

    void updateUserDetails(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                URL url = new URL(Server.getUpdateUserDetails());
                URLConnection connection = url.openConnection();
                String postData = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8");
                postData += "&"+URLEncoder.encode("surname","UTF-8")+"="+URLEncoder.encode(surname,"UTF-8");
                postData += "&"+URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");

                if(isPasswords) {
                    postData += "&"+URLEncoder.encode("oldPassword", "UTF-8") + "=" + URLEncoder.encode(oldPassword, "UTF-8");
                    postData += "&"+URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(newPassword1, "UTF-8");
                    postData += "&"+URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
                }else{
                    postData += "&"+URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("half", "UTF-8");}
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
                Log.i(TAG, "Response " + line);

                if(line != null){
                    if(line.equalsIgnoreCase("User Updated")){
                        hideProgressDialog();
                        setSuccessMessage();
                          }
                    else if(line.equalsIgnoreCase("Passwords Do Not Match")){
                        hideProgressDialog();
                        setMessage("Incorrect Password");
                    }
                    else{
                        hideProgressDialog();
                        setMessage("Something Went Wrong. Please Try Again");}
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }).start();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){

            switch (requestCode){
                case 0:
                    showPicProgressDialog();
                    handleCameraImage(data);
                    break;
                case 1:
                    showPicProgressDialog();
                    handleGalleryImage(data);
                    break;
            }
        }
    }

    void openAddressBook(){
        Intent intent = new Intent(this, AddressActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
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
        }else if(id == R.id.address_book){
            openAddressBook();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
