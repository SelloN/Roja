package com.natech.roja.CheckOut;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.natech.roja.MenuCategories.Extras;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;

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
public class EditItemActivity extends AppCompatActivity {
    private TextView priceTV;
    private TextView quantityTV;
    private SharedPreferences extrasSharedPreferences;
    private SharedPreferences.Editor editor, extrasEditor;
    private String note = "";
    private double price, newExtrasPrice, prevExtrasPrice;
    private float price2;
    private int quantity, itemPosition, menuID,extrasSize;
    private Context context;
    private Dialog dialog, noteDialog;
    private float originalPrice;
    private List<Extras> extrasList;
    private List<String> extraTypes, selectedTypes;
    private Boolean isFirstSelect = false;
    private static final String CART_FILE = "CartList";
    private ProgressBar progress;
    private EditText noteED;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        //toolbar.startAnimation(toolbarAnim);
        priceTV = (TextView)findViewById(R.id.itemPriceDescription2);
        TextView toolbarTV = (TextView) findViewById(R.id.toolbar_title);
        toolbarTV.setText("Edit");
        TextView itemTV = (TextView) findViewById(R.id.itemHead2);
        quantityTV = (TextView)findViewById(R.id.itemDescriptionQuantityTV);
        ImageButton increaseIV = (ImageButton) findViewById(R.id.increaseQuantity);
        ImageButton decreaseIV = (ImageButton) findViewById(R.id.decreaseQuantity);
        progress = (ProgressBar)findViewById(R.id.progressBar);
        TextView noExtrasTV = (TextView)findViewById(R.id.noExtrasTV);

        final FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu)findViewById(R.id.floatMenuEdit);
        final FloatingActionButton acceptFAB = (FloatingActionButton)findViewById(R.id.acceptFAB);
        final FloatingActionButton editNoteFAB = (FloatingActionButton)findViewById(R.id.noteEditFAB);
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(CART_FILE, MODE_PRIVATE);
        extrasSharedPreferences = this.getApplicationContext().
                getSharedPreferences(AppSharedPreferences.getExtrasFile(), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        extrasEditor = extrasSharedPreferences.edit();

        priceTV.setText(getIntent().getExtras().getString("Item Price"));
        itemTV.setText(getIntent().getExtras().getString("Selected Item"));
        quantityTV.setText(getIntent().getExtras().getString("Quantity"));
        itemPosition = getIntent().getExtras().getInt("Item Position");
        menuID = getIntent().getExtras().getInt("menuID");
        Boolean hasExtras = getIntent().getExtras().getBoolean("hasExtras");

        context = getBaseContext();
        setIndex();
        if(hasExtras)
            getExtras();
        else
            noExtrasTV.setVisibility(View.VISIBLE);

        if(extrasSharedPreferences.contains("note"+menuID))
            note = extrasSharedPreferences.getString("note" + menuID, null);

        acceptFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();
                showDialog();
            }
        });

        editNoteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();
                showNoteDialog();
            }
        });

        increaseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                quantity = Integer.parseInt(quantityTV.getText().toString());
                originalPrice = Float.valueOf(priceTV.getText().toString().substring(1)) / quantity;
                quantity = Integer.parseInt(quantityTV.getText().toString()) + 1;
                Log.i("sd", "OP " + originalPrice);
                price2 = originalPrice * quantity;

                priceTV.setText("R" + String.format("%1$,.2f", price2));
                quantityTV.setText(String.valueOf(quantity));

                //editor.putFloat("Item Price"+itemPosition,price2);
                editor.putInt("Quantity" + itemPosition, quantity);

            }
        });

        decreaseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (Integer.parseInt(quantityTV.getText().toString()) > 1) {
                    quantity = Integer.parseInt(quantityTV.getText().toString());
                    originalPrice = Float.valueOf(priceTV.getText().toString().substring(1)) / quantity;
                    quantity = Integer.parseInt(quantityTV.getText().toString()) - 1;
                    price2 = Float.valueOf(priceTV.getText().toString().substring(1)) - originalPrice;
                    Log.i("sd", "OP " + originalPrice);
                    priceTV.setText("R" + String.format("%1$,.2f", price2));
                    quantityTV.setText(String.valueOf(quantity));
                    //editor.putFloat("Item Price"+itemPosition,price2);
                    editor.putInt("Quantity" + itemPosition, quantity);
                } else
                    Toast.makeText(context, "Minimum order limit", Toast.LENGTH_SHORT).show();

            }
        });
        /*final ShowcaseView showcaseView[] = new ShowcaseView[3];

       final String TUTORIAL = "LearnedEditActivity";

       if(!getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
           acceptFAB.post(new Runnable() {
               @Override
               public void run() {

                   int[] coordinates = new int[2];
                   floatingActionsMenu.getLocationOnScreen(coordinates);
                   Log.i("", "Coordinates: X = " + coordinates[0] + " Y = " + coordinates[1]);
                   showcaseView[0] = new ShowcaseView.Builder(EditItemActivity.this).
                           setTarget(new ViewTarget(R.id.dummyArea, EditItemActivity.this)).
                           //setContentTitle("Edits").setContentText("Touch this button to edit the note for " +
                           "the waiter and save the changes made to the item").
                           setStyle(R.style.CustomShowcaseTheme3).build();
                   showcaseView[0].setButtonText("Next");
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
                       public void onShowcaseViewDidHide(ShowcaseView sv) {
                           showcaseView[1] = new ShowcaseView.Builder(EditItemActivity.this).
                                   setTarget(new ViewTarget(R.id.increaseQuantity, EditItemActivity.this)).
                                   setContentTitle("Increase Quantity").setContentText("Touch this button " +
                                   "to increase the quantity for this item").
                                   setStyle(R.style.CustomShowcaseTheme3).build();
                           showcaseView[1].setButtonText("Next");
                           RelativeLayout.LayoutParams layoutParams
                                   = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                   RelativeLayout.LayoutParams.WRAP_CONTENT);
                           layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                           layoutParams.setMargins(15, 15, 15, 15);
                           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                           showcaseView[1].setButtonPosition(layoutParams);
                           showcaseView[1].setOnShowcaseEventListener(new OnShowcaseEventListener() {
                               @Override
                               public void onShowcaseViewHide(ShowcaseView showcaseView) {
                               }

                               @Override
                               public void onShowcaseViewDidHide(ShowcaseView sv) {
                                   showcaseView[2] = new ShowcaseView.Builder(EditItemActivity.this).
                                           setTarget(new ViewTarget(R.id.decreaseQuantity, EditItemActivity.this)).
                                           setContentTitle("Decrease Quantity").setContentText("Touch this button to decrease " +
                                           "the quantity for this item").
                                           setStyle(R.style.CustomShowcaseTheme3).build();
                                   showcaseView[2].setButtonText("Got It!");
                                   RelativeLayout.LayoutParams layoutParams
                                           = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                           RelativeLayout.LayoutParams.WRAP_CONTENT);
                                   layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                                   layoutParams.setMargins(15, 15, 15, 15);
                                   layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                   showcaseView[2].setButtonPosition(layoutParams);

                                   showcaseView[2].setOnShowcaseEventListener(new OnShowcaseEventListener() {
                                       @Override
                                       public void onShowcaseViewHide(ShowcaseView showcaseView) {

                                       }

                                       @Override
                                       public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                            getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                                                    edit().putBoolean(TUTORIAL,true).apply();
                                       }

                                       @Override
                                       public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                       }
                                   });

                               }

                               @Override
                               public void onShowcaseViewShow(ShowcaseView showcaseView) {

                               }
                           });
                       }

                       @Override
                       public void onShowcaseViewShow(ShowcaseView showcaseView) {
                       }
                   });
               }
           });
       }
        */


    }

    void showNoteDialog(){
        noteDialog = new Dialog(this);
        noteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        noteDialog.setContentView(R.layout.note_dialog);
        noteDialog.show();

        noteED = (EditText)noteDialog.findViewById(R.id.noteED);
        Button saveBtn = (Button)noteDialog.findViewById(R.id.saveNoteBtn);
        Button cancelBtn = (Button)noteDialog.findViewById(R.id.cancelNoteBtn);

        if(!note.equals(""))
            noteED.setText(note);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!noteED.getText().toString().isEmpty()) {
                    extrasEditor.putString("note" + menuID, noteED.getText().toString());
                    note = noteED.getText().toString();
                }
                else{
                    extrasEditor.remove("note"+menuID);
                    note = "";
                }

                hideNoteDialog();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideNoteDialog();
            }
        });
    }

    void hideNoteDialog(){
        noteDialog.dismiss();
    }
    void hideDialog()
    {
        dialog.dismiss();
    }

    void getExtras(){
        new AsyncTask<String, Void, String>(){

            @Override
            protected void onPreExecute(){
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(Server.getItemExtras());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("menuID","UTF-8")+"="+URLEncoder.encode(String.valueOf(menuID),"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String result = reader.readLine();
                    reader.close();
                    if(result != null){
                        return result;
                    }
                    else
                        return "Error";
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String arg){
                progress.setVisibility(View.INVISIBLE);
                if(!arg.equalsIgnoreCase("Error"))
                    loadExtras(arg);
                else
                    showSnackBar();

            }
        }.execute(null,null,null);
    }

    void showSnackBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.snackbarPosition),
                        "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void loadExtras(String jsonResult){

        extrasList = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("itemExtras");

            /*
                This loop gets the data from the Jason Array that stores
                the data from the Mysql database
             */
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                int extraID = jsonChildNode.optInt("extraID");
                String extra = jsonChildNode.optString("extra");
                String extraPrice = jsonChildNode.optString("extraPrice");
                String extraType = jsonChildNode.optString("type");

                extrasList.add(new Extras(extra,Double.parseDouble(extraPrice),extraType,extraID));
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        extraTypes = new ArrayList<>();
        //get the type of extras loaded
        for(int x = 0; x < extrasList.size(); x++){
            if(!extraTypes.contains(extrasList.get(x).getExtraType()))
                extraTypes.add(extrasList.get(x).getExtraType());
        }
        selectedTypes = new ArrayList<>();
        buildExtrasUI();


    }

    void buildExtrasUI(){
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.descriptionFrameEdit);

        for(int x = 0; x < extraTypes.size(); x++){
            Boolean hasRadios =false, hasCheckBoxes = false;
            LinearLayout typeHeader = new LinearLayout(this);
            LinearLayout checkBoxes = new LinearLayout(this);
            checkBoxes.setOrientation(LinearLayout.VERTICAL);
            typeHeader.setBackgroundColor(getResources().getColor(R.color.myPrimaryColor));
            TextView headerTV = new TextView(this);
            final RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            radioGroup.setGravity(Gravity.CENTER_VERTICAL);

            for(int i = 0; i < extrasList.size(); i++){
                final RadioButton radioButton;
                if(extrasList.get(i).getExtraType().equals(extraTypes.get(x))&&!extrasList.get(i).getExtraType().equals("Add On")){
                    radioButton = new RadioButton(this);
                    radioButton.setTextColor(getResources().getColor(R.color.switch_thumb_disabled_material_dark));
                    if(extrasList.get(i).getExtraPrice() != 0.0) {
                        radioButton.setText(extrasList.get(i).getExtra() + " R" + String.format("%1$,.2f", extrasList.get(i).getExtraPrice()));
                    }
                    else
                        radioButton.setText(extrasList.get(i).getExtra());

                    //Log.i("","Edit Item "+extrasList.get(i).getExtra()+menuID);
                    if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                        radioButton.setChecked(true);
                    radioButton.setId(i);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                            int pos = radioButton.getId();
                            price = Double.parseDouble(priceTV.getText().toString().substring(1));

                            if (compoundButton.isChecked()) {
                                //Log.i("","Price "+price);

                                newExtrasPrice = extrasList.get(pos).getExtraPrice() *Integer.parseInt(quantityTV.getText().toString());
                                extrasEditor.putFloat(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID, (float)extrasList.get(pos).getExtraPrice());
                                if (!selectedTypes.contains(extrasList.get(pos).getExtraType())) {
                                    price = price + newExtrasPrice;
                                    selectedTypes.add(extrasList.get(pos).getExtraType());
                                    priceTV.setText("R" + String.format("%1$,.2f", price));
                                    isFirstSelect = true;

                                }
                                extrasSize++;
                                //extrasEditor.putInt("Size",extrasSize);
                                //Log.i("","Extras Price "+newExtrasPrice);

                            } else {
                                extrasEditor.remove(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID);
                                prevExtrasPrice = extrasList.get(pos).getExtraPrice() *Integer.parseInt(quantityTV.getText().toString());
                                //Log.i("","Previous Extras Price "+prevExtrasPrice);
                                price = price - prevExtrasPrice;
                                if(!isFirstSelect)
                                    price = price + newExtrasPrice;
                                isFirstSelect = false;
                                priceTV.setText("R" + String.format("%1$,.2f", price));
                                extrasSize--;
                                //extrasEditor.putInt("Size",extrasSize);
                            }

                        }
                    });
                    hasRadios = true;
                    radioGroup.addView(radioButton);
                }
                else if(extrasList.get(i).getExtraType().equals("Add On")){
                    final CheckBox checkBox = new CheckBox(this);
                    checkBox.setId(i);
                    if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                        checkBox.setChecked(true);
                    checkBox.setText(extrasList.get(i).getExtra() + " R" + String.format("%1$,.2f", extrasList.get(i).getExtraPrice()));
                    checkBox.setTextColor(getResources().getColor(R.color.switch_thumb_disabled_material_dark));
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            price = Double.parseDouble(priceTV.getText().toString().substring(1));
                            int pos = checkBox.getId();
                            if(compoundButton.isChecked()){
                                newExtrasPrice = extrasList.get(pos).getExtraPrice() * Integer.parseInt(quantityTV.getText().toString());
                                extrasEditor.putFloat(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID, (float)extrasList.get(pos).getExtraPrice());
                                price = price + newExtrasPrice;
                                selectedTypes.add(extrasList.get(pos).getExtraType());
                                priceTV.setText("R" + String.format("%1$,.2f", price));
                                extrasSize++;
                                //extrasEditor.putInt("Size",extrasSize);

                            }else{
                                extrasEditor.remove(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID);
                                prevExtrasPrice = extrasList.get(pos).getExtraPrice() *Integer.parseInt(quantityTV.getText().toString());
                                price = price - prevExtrasPrice;
                                priceTV.setText("R" + String.format("%1$,.2f", price));
                                extrasSize--;
                                //extrasEditor.putInt("Size",extrasSize);

                            }
                        }
                    });
                    hasCheckBoxes = true;
                    checkBoxes.addView(checkBox);

                }
            }
            if(!extraTypes.get(x).equalsIgnoreCase("Choice of2"))
                headerTV.setText(extraTypes.get(x));
            else
                headerTV.setText("Choice of");
            //headerTV.setText(extraTypes.get(x));
            headerTV.setTextColor(getResources().getColor(R.color.white));
            headerTV.setTextSize(getResources().getDimension(R.dimen.abc_text_size_caption_material));
            typeHeader.setGravity(Gravity.CENTER_HORIZONTAL);
            typeHeader.addView(headerTV);
            parentLayout.addView(typeHeader);
            if(hasRadios)
                parentLayout.addView(radioGroup);
            else if(hasCheckBoxes)
                parentLayout.addView(checkBoxes);
        }

    }

    void setIndex(){
        if(extrasSharedPreferences.contains("Size"))
            extrasSize = extrasSharedPreferences.getInt("Size",0);
        else{
            extrasSize = 0;
            //extrasEditor.putInt("Size",0);
            //extrasEditor.commit();
        }
    }

    void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage(R.string.save_changes);
        builder.setNegativeButton("Cancel",null);
        builder.setPositiveButton("Save",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveChanges();
            }
        });
        builder.show();/*
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.accept_changes_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Button yesButton = (Button)dialog.findViewById(R.id.yesAccept);
        Button noButton = (Button)dialog.findViewById(R.id.noAccept);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialog();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });*/
    }

    void saveChanges()
    {
        editor.putFloat("Item Price"+itemPosition,Float.valueOf(priceTV.getText().toString().substring(1)));
        extrasEditor.commit();
        editor.commit();
//        hideDialog();
        Intent intent = new Intent(this, CheckOutActivity.class);
        CheckOutActivity.checkOutActivity.finish();
        finish();
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_edit_item, menu);
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
