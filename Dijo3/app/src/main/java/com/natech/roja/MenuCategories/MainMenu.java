package com.natech.roja.MenuCategories;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.natech.roja.CheckOut.CheckOutActivity;
import com.natech.roja.NetworkServices.NetworkUtil;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@SuppressWarnings({"TryWithIdenticalCatches", "ConstantConditions"})
public class MainMenu extends AppCompatActivity {

    private RecyclerView rv;
    private String tableID;
    private ProgressBar progress;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView header;
    private SharedPreferences.Editor editor, editorOrders;
    private static final String TAG = "Main Menu";
    private SharedPreferences.Editor idEditor, extrasEditor;
    private String waiterRegID, waiterName, waiterID, userID,tableLogID, oldRestID, newRestID;
    private int franchID;
    private Dialog dialog, leaveTableDialog;
    private static MainMenu mainMenu;
    private Boolean isFranchise = false, waiterLoaded = false, isRestore = false;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences("CartList", MODE_PRIVATE);
        SharedPreferences idFile = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE);
        idEditor = idFile.edit();
        SharedPreferences extrasSharedPreferences = this.getApplicationContext().getSharedPreferences(AppSharedPreferences.getExtrasFile(), MODE_PRIVATE);
        extrasEditor = extrasSharedPreferences.edit();
        mainMenu = this;
        editor = sharedPreferences.edit();
        SharedPreferences sharedPreferencesOrders = this.getApplicationContext().getSharedPreferences("OrderList", MODE_PRIVATE);
        editorOrders = sharedPreferencesOrders.edit();
        header = (ImageView) findViewById(R.id.header);
        RelativeLayout noNetwork = (RelativeLayout) findViewById(R.id.noNetwork);
        setSupportActionBar(toolbar);
        tableID = getIntent().getExtras().getString(CommonIdentifiers.getTableId());
        //Log.i(TAG,"previous  log id "+idFile.getString(CommonIdentifiers.getTableLogId(),null));
        //Log.i(TAG,"previous  table id "+idFile.getString(CommonIdentifiers.getTableId(),null));

        userID = idFile.getString(CommonIdentifiers.getUserId(), "");

        if (idFile.contains(CommonIdentifiers.getRestId()))
            oldRestID = idFile.getString(CommonIdentifiers.getRestId(), "");
        else
            oldRestID = null;

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setContentScrimColor(getResources().getColor(R.color.myPrimaryColor));
        collapsingToolbar.setTitle("");
        ImageButton waiterFAB = (ImageButton) findViewById(R.id.waiterFAB);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        rv = (RecyclerView) findViewById(R.id.scrollableview);
        rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(linearLayoutManager);

        if (tableID.equalsIgnoreCase("Error"))
            showMessageDialog("Invalid QR Code");
        else {
            if (NetworkUtil.getConnectivityStatus(this)) {
                         /*
                    Check if the scanned QR code has returned the tableID that is the same as the previously
                    stored one.
                 */
                if (tableID.equalsIgnoreCase(idFile.getString(CommonIdentifiers.getTableId(), null)))
                    showRestoreTableDialog();
                else {
                    collapsingToolbar.setTitle("Please Wait...");
                    idEditor.putString(CommonIdentifiers.getTableId(), tableID);
                    idEditor.commit();
                    new GetRestaurantHeader().execute();
                }
                //Log.i(TAG,"++++++++++++++++++++++++ ");

            }
            else {
                noNetwork.setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        }

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fly_in);
        waiterFAB.startAnimation(animation);


        waiterFAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (NetworkUtil.getConnectivityStatus(MainMenu.this)) {
                    if (waiterLoaded) {
                        String message = getSharedPreferences(AppSharedPreferences.getIdFile(), Context.MODE_PRIVATE).
                                getString(CommonIdentifiers.getName(), null) + " needs you";
                        summonWaiter(message);
                    } else
                        Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "Please Wait", Snackbar.LENGTH_SHORT).show();
                } else
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "No Internet Connection", Snackbar.LENGTH_LONG).show();

                return true;
            }
        });

        waiterFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waiterLoaded)
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "Your Waitron is " + waiterName, Snackbar.LENGTH_SHORT).show();
                else
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "Please Wait", Snackbar.LENGTH_SHORT).show();
            }
        });
        invalidateOptionsMenu();
        final String TUTORIAL = "LearnedMainMenu";

        if (!getSharedPreferences(AppSharedPreferences.getSettings(), MODE_PRIVATE).getBoolean(TUTORIAL, false)) {
            final ShowcaseView[] showcaseView = new ShowcaseView[2];
            showcaseView[0] = new ShowcaseView.Builder(MainMenu.this).setTarget(new ViewTarget(R.id.waiterFAB, MainMenu.this)).
                    setStyle(R.style.CustomShowcaseTheme3).setContentTitle("Waitron Summoner").hideOnTouchOutside().
                    setContentText("Touch this button once to see who is serving you or touch and hold to summon your waiteron").build();
            showcaseView[0].setButtonText("Got It");
            RelativeLayout.LayoutParams layoutParams
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
            layoutParams.setMargins(15, 15, 15, 15);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            showcaseView[0].setButtonPosition(layoutParams);
            showcaseView[0].setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {

                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                                putBoolean(TUTORIAL,true).apply();
                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                }
            });
        }
    }

    void showMessageDialog(final String message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new Dialog(MainMenu.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.message_dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                TextView messageTV = (TextView)dialog.findViewById(R.id.messageDialogTV);
                Button okBtn = (Button)dialog.findViewById(R.id.okDialog);
                messageTV.setText(message);
                dialog.show();
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideMessageDialog();
                        finish();
                    }
                });
            }
        });
    }

    void hideMessageDialog()
    {
        dialog.dismiss();
    }
    private class GetRestaurantHeader extends  AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(Server.getRestaurantHeaderDetails());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(tableID,"UTF-8");
                postData += "&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("menu","UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line =  reader.readLine();
                reader.close();
                //Log.i(TAG,line);
                if(line != null){
                    return line;
                }
                else
                    return "Error";
            }catch (IOException e) {

                e.printStackTrace();
                return "Error";
            }
        }
        @Override
        protected void onPostExecute(String arg)
        {
                if (!arg.equalsIgnoreCase("Error"))
                    drawHeader(arg);
                else if (arg.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_SHORT).show();

        }
    }

    private class GetCategories extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(Server.getMenu());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData;
                if(isFranchise) {
                    postData = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("franchise", "UTF-8");
                    postData += "&"+URLEncoder.encode("franchID","UTF-8")+"="+URLEncoder.encode(String.valueOf(franchID),"UTF-8");
                }
                else {
                    postData = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("standalone", "UTF-8");
                    postData += "&"+URLEncoder.encode("restID","UTF-8")+"="+URLEncoder.encode(newRestID,"UTF-8");
                }

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                reader.close();
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
        protected void onPostExecute(String result) {
            progress.setVisibility(View.INVISIBLE);
            if(!result.equalsIgnoreCase("Error"))
                ListDrawer(result);
            else if(result.equalsIgnoreCase("Error"))
                Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_SHORT).show();


        }
    }

    void drawHeader(String jsonResult)
    {
        try{
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("restaurant");
            String restaurantName;
            String photoDir;
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                //restaurantID
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                restaurantName = jsonChildNode.optString("restaurantName");
                photoDir = jsonChildNode.optString("photoDir");
                waiterRegID = jsonChildNode.optString("regID");
                waiterID = jsonChildNode.optString("waiterID");
                newRestID = jsonChildNode.optString("restaurantID");
                franchID = jsonChildNode.optInt("franchiseID");
                waiterName = jsonChildNode.optString("name");
                collapsingToolbar.setTitle(restaurantName);

                if(franchID != 0 && franchID != 3)
                    isFranchise = true;
                waiterLoaded = true;
                Picasso.with(this).load(photoDir).into(header);
            }
            //Log.i("","Waiter REg ID "+waiterRegID);

            if(oldRestID != null){

                if(!oldRestID.equals(newRestID)){
                    idEditor.putString(CommonIdentifiers.getRestId(),newRestID);
                    editor.clear();
                    editorOrders.clear();
                    extrasEditor.clear();
                    editorOrders.commit();
                    editor.commit();
                    extrasEditor.commit();
                    //Log.i(TAG," New Rest ID");
                }else
                    idEditor.putString(CommonIdentifiers.getRestId(),newRestID);

            }else{
                idEditor.putString(CommonIdentifiers.getRestId(),newRestID);
            }


            //Log.i(TAG,"Waiter Reg ID = "+waiterRegID);
            //Log.i(TAG,"Waiter ID ="+waiterID);
            idEditor.putString(CommonIdentifiers.getWaiterId(),waiterID);
            idEditor.putString(CommonIdentifiers.getWaiterRegId(),waiterRegID);

            idEditor.apply();

            if(!isRestore)
                getTableLogID();
            else {
                tableLogID = getSharedPreferences(AppSharedPreferences.getIdFile(), MODE_PRIVATE).
                        getString(CommonIdentifiers.getTableLogId(), null);
                new GetCategories().execute();
            }


        }catch (JSONException e){
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
            //Log.i(TAG, e.toString());
        }
    }

    void getTableLogID(){


        new AsyncTask<String, Void, String>(){



            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(Server.getLockTable());
                    URLConnection connection = url.openConnection();


                    String postData = URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(tableID,"UTF-8");
                    postData += "&"+URLEncoder.encode("waiterID","UTF-8")+"="+URLEncoder.encode(waiterID,"UTF-8");
                    postData += "&"+URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG, "New Response Here : " + line);

                    if(line != null){
                        if(line.contains("LastID")){
                            tableLogID = line.substring(6);
                            //Log.i(TAG, "Table Log ID : " + tableLogID);
                            idEditor.putString(CommonIdentifiers.getTableLogId(),tableLogID);
                            idEditor.commit();
                            GetCategories task2 = new GetCategories();
                            task2.execute();

                        }
                        else if(line.equalsIgnoreCase("Table unavailable")){
                            showMessageDialog("Waiter Has Not Locked Table");
                        }
                    }
                    else {
                        mainMenu.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setVisibility(View.INVISIBLE);
                                Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                                        "Network Problems. Please Try Again", Snackbar.LENGTH_SHORT).show();

                            }
                        });

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "Error";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
                return "";
            }

            @Override
        protected void onPostExecute(String arg){
                if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_SHORT).show();

            }
        }.execute();
    }

    void ListDrawer(String jsonResult) {
        RVAdapter adapter = new RVAdapter(this);
        rv.setAdapter(adapter);


        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("category");

            /*
                This loop gets the data from the Jason Array that stores
                the data from the Mysql database
             */
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String name = jsonChildNode.optString("categoryName");
                String desc = jsonChildNode.optString("description");
                String picLink = jsonChildNode.optString("photoDir");
                String catID = jsonChildNode.optString("catID");
                int hasExtras = jsonChildNode.optInt("hasExtras");
                adapter.addItem(new Category(name,desc,picLink,Integer.parseInt(catID),hasExtras,false));
                //list.add(new Category(name,desc,pics[i]));
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }


    }

    void summonWaiter(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(Server.getUpdateOrder());
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode("Summon","UTF-8");
                    postData += "&"+URLEncoder.encode("registrationIDs","UTF-8")+"="+URLEncoder.encode(waiterRegID,"UTF-8");
                    postData += "&"+URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(tableID,"UTF-8");
                    postData += "&"+URLEncoder.encode("userID","UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                                    getString(CommonIdentifiers.getUserId(),null),"UTF-8");
                    postData += "&"+URLEncoder.encode("tableMessage","UTF-8")+"="+URLEncoder.encode(message,"UTF-8");
                    postData += "&"+URLEncoder.encode(CommonIdentifiers.getRegId(),"UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getCredentialsFile(),
                                    MODE_PRIVATE).getString(AppSharedPreferences.getRegistrationId(), null),"UTF-8");
                    postData += "&"+URLEncoder.encode("tableLogID","UTF-8")+"="+
                            URLEncoder.encode(getSharedPreferences(AppSharedPreferences.getIdFile(),MODE_PRIVATE).
                                    getString(CommonIdentifiers.getTableLogId(), null),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    reader.close();
                    //Log.i(TAG, "Reg : " + url.toString());
                    //Log.i(TAG, "New Response : " + line);
                    Snackbar.make(findViewById(R.id.snackbarPositionWaiter), "Waiter Summoned", Snackbar.LENGTH_SHORT).show();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    void openCheckOut()
    {
        idEditor.putString(CommonIdentifiers.getType(),"onSite");
        idEditor.commit();
        Intent intent = new Intent(this, CheckOutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type","onSite");
        intent.putExtras(bundle);
        startActivity(intent);

    }

    void showLeaveTableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage("Leave your table?");
        builder.setPositiveButton("Leave",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                idEditor.remove("tableID");
                idEditor.remove(CommonIdentifiers.getRestId());
                idEditor.remove(CommonIdentifiers.getTableLogId());
                getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                        putInt(CommonIdentifiers.getMenuIdError(),0).commit();
                idEditor.commit();
                editorOrders.commit();
                extrasEditor.commit();
                editor.commit();
                if(getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                        getBoolean(CommonIdentifiers.getFatalOrderError(),false)){
                    editor.clear();
                    editorOrders.clear();
                    extrasEditor.clear();
                    getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                            edit().putBoolean(CommonIdentifiers.getFatalOrderError(),false).commit();
                    editorOrders.commit();
                    editor.commit();
                    extrasEditor.commit();

                }
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
        /*
        leaveTableDialog = new Dialog(this);
        leaveTableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveTableDialog.setContentView(R.layout.clear_cart_dialog);
        leaveTableDialog.setCanceledOnTouchOutside(true);
        TextView messageTV = (TextView)leaveTableDialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Leave Your Table?");
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
                extrasEditor.clear();
                idEditor.remove("tableID");
                idEditor.remove(CommonIdentifiers.getRestId());
                idEditor.remove(CommonIdentifiers.getTableLogId());
                getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                        putInt(CommonIdentifiers.getMenuIdError(),0).commit();
                idEditor.commit();
                editorOrders.commit();
                extrasEditor.commit();
                editor.commit();
                if(getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                        getBoolean(CommonIdentifiers.getFatalOrderError(),false)){
                    editor.clear();
                    editorOrders.clear();
                    extrasEditor.clear();
                    getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                            edit().putBoolean(CommonIdentifiers.getFatalOrderError(),false).commit();
                    editorOrders.commit();
                    editor.commit();
                    extrasEditor.commit();

                }
                leaveTableDialog.dismiss();
                finish();
            }
        });*/
    }

    void showRestoreTableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage("Restore table state?");
        builder.setPositiveButton("Restore",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                collapsingToolbar.setTitle("Please Wait...");
                isRestore = true;
                new GetRestaurantHeader().execute();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                collapsingToolbar.setTitle("Please Wait...");
                isRestore = false;
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                editorOrders.apply();
                editor.apply();
                extrasEditor.apply();
                new GetRestaurantHeader().execute();
            }
        });
        builder.setCancelable(false);
        builder.show();

        /*
        leaveTableDialog = new Dialog(this);
        leaveTableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveTableDialog.setContentView(R.layout.clear_cart_dialog);
        leaveTableDialog.setCanceledOnTouchOutside(false);
        TextView messageTV = (TextView)leaveTableDialog.findViewById(R.id.messageCartTV);
        messageTV.setText("Restore Table State?");
        Button yesBtn = (Button)leaveTableDialog.findViewById(R.id.yesClear);
        Button noBtn = (Button)leaveTableDialog.findViewById(R.id.noClear);
        leaveTableDialog.show();
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsingToolbar.setTitle("Please Wait...");
                isRestore = false;
                leaveTableDialog.dismiss();
                editor.clear();
                editorOrders.clear();
                extrasEditor.clear();
                editorOrders.apply();
                editor.apply();
                extrasEditor.apply();
                new GetRestaurantHeader().execute();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsingToolbar.setTitle("Please Wait...");
                isRestore = true;
                leaveTableDialog.dismiss();
                new GetRestaurantHeader().execute();
            }
        });*/

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showLeaveTableDialog();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
