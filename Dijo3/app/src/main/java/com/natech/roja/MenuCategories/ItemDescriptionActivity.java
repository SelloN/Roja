package com.natech.roja.MenuCategories;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
public class ItemDescriptionActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences, extrasSharedPreferences;
    private SharedPreferences.Editor editor, extrasEditor;
    private String selectedItem;
    private String note = "";
    private int size;
    private int extrasSize;
    private int menuID;
    private ImageButton addToCart;
    private TextView itemPriceTV;
    private Boolean isInCart = false, hasExtras,isFlavoured = false,isFlavoured2 = false,isSized = false,
            hasSize = false, hasFlavour = false, hasFlavour2 = false;
    private Dialog dialog, noteDialog;
    private List<Extras> extrasList;
    private List<String> extraTypes, selectedTypes;
    private double newExtrasPrice;
    private double prevExtrasPrice;
    private double price;
    private static final String EXTRAS_FILE = "extras", CART_FILE = "CartList";
    private ProgressBar progress;
    private EditText noteED;
    private static final String TAG = ItemDescriptionActivity.class.getSimpleName();



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_description);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTV = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // selectedItemTV = (TextView)findViewById(R.id.itemHead);
        itemPriceTV = (TextView)findViewById(R.id.itemPriceDescription);
        TextView itemDescriptionTV = (TextView) findViewById(R.id.itemDescriptionTV);
        progress = (ProgressBar)findViewById(R.id.progressBar);
        addToCart = (ImageButton)findViewById(R.id.addToCart);
        TextView noExtrasTV = (TextView)findViewById(R.id.noExtrasTV);
        sharedPreferences = this.getApplicationContext().getSharedPreferences(CART_FILE, MODE_PRIVATE);
        extrasSharedPreferences = this.getApplicationContext().getSharedPreferences(EXTRAS_FILE,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        extrasEditor = extrasSharedPreferences.edit();
        //extract bundle extras
        selectedItem = getIntent().getExtras().getString("Selected Item");
        String itemPrice = getIntent().getExtras().getString("Item Price");
        price = Double.parseDouble(itemPrice.substring(1));
        //double originalPrice = price;
        menuID = getIntent().getExtras().getInt("menuID");
        String itemDescription = getIntent().getExtras().getString("description");
        hasExtras = getIntent().getExtras().getBoolean("hasExtras");
        toolbarTV.setText(selectedItem);
        //final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        //toolbar.startAnimation(toolbarAnim);
        itemDescriptionTV.setText(itemDescription);
        itemPriceTV.setText(itemPrice);
        setIndex();
        checkIfInCart();
        if(hasExtras) {
            if(NetworkUtil.getConnectivityStatus(this))
                getExtras();
            else
                Snackbar.make(findViewById(R.id.snackbarPosition), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
        else
            noExtrasTV.setVisibility(View.VISIBLE);

        if(!isInCart) {
            extrasEditor.remove("note" + menuID);
            extrasEditor.commit();
        }

        if(price == 0){
            Snackbar.make(findViewById(R.id.snackbarPosition),
                    "Ask your waitron for the latest price on this item", Snackbar.LENGTH_LONG).show();
        }

        if(extrasSharedPreferences.contains("note"+menuID))
            note = extrasSharedPreferences.getString("note" + menuID, null);


        final ImageButton noteFAB = (ImageButton)findViewById(R.id.noteFAB);

        noteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog();
            }
        });
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isInCart){
                    if(hasFlavour || hasSize){
                        if(hasFlavour && hasFlavour2 && hasSize){
                            if(isFlavoured && isSized && isFlavoured2)
                                showDialog();
                            else
                                Snackbar.make(findViewById(R.id.snackbarPosition),
                                        "Please choose a size/type", Snackbar.LENGTH_SHORT).show();
                        }
                        else if(hasFlavour && hasFlavour2){
                            if(isFlavoured2 && isFlavoured) {
                                showDialog();
                            }else
                                Snackbar.make(findViewById(R.id.snackbarPosition),
                                        "Please choose a size/type", Snackbar.LENGTH_SHORT).show();
                        }
                        else if(hasFlavour && hasSize){
                            if(isFlavoured && isSized)
                                showDialog();
                            else
                                Snackbar.make(findViewById(R.id.snackbarPosition),
                                        "Please choose a size/type", Snackbar.LENGTH_SHORT).show();
                        }else if(hasFlavour && isFlavoured)
                            showDialog();
                        else if(hasSize && isSized)
                            showDialog();
                        else
                            Snackbar.make(findViewById(R.id.snackbarPosition),
                                    "Please choose a size/type", Snackbar.LENGTH_SHORT).show();
                    }else
                        showDialog();
                }
                else
                    Snackbar.make(findViewById(R.id.snackbarPosition), "Item Already In Cart", Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(ItemDescriptionActivity.this, "Item Already In Cart", Toast.LENGTH_SHORT).show();
            }
        });

        final String TUTORIAL = "LearnedItemDescriptionActivity";
        if(!getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
            final ShowcaseView showcaseView[] = new ShowcaseView[2];
            showcaseView[0] = new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.noteFAB, this)).hideOnTouchOutside().
                    setContentTitle("Waiter's Note").setContentText("Touch this button to leave a short note for your waiter").setStyle(R.style.CustomShowcaseTheme3).build();
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
                    showcaseView[1] = new ShowcaseView.Builder(ItemDescriptionActivity.this).
                            setTarget(new ViewTarget(R.id.addToCart, ItemDescriptionActivity.this)).
                            setContentTitle("Add Item").setContentText("Touch this button to add this item to your cart").
                            hideOnTouchOutside().
                            setStyle(R.style.CustomShowcaseTheme3).build();
                    showcaseView[1].setButtonText("Got It!");
                    RelativeLayout.LayoutParams layoutParams
                            = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                    layoutParams.setMargins(15, 15, 15, 15);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    showcaseView[1].setButtonPosition(layoutParams);
                    showcaseView[1].setOnShowcaseEventListener(new OnShowcaseEventListener() {
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

    void setIndex(){
        if(sharedPreferences.contains("Size"))
            size = sharedPreferences.getInt("Size",0);
        else
        {
            size = 0;
            editor.putInt("Size",0);
            editor.commit();
        }

        if(extrasSharedPreferences.contains("Size"))
            extrasSize = extrasSharedPreferences.getInt("Size",0);
        else{
            extrasSize = 0;
            //extrasEditor.putInt("Size",0);
            //extrasEditor.commit();
        }
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
                    String postData = URLEncoder.encode("menuID", "UTF-8")+"="+URLEncoder.encode(String.valueOf(menuID),"UTF-8");
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
                }catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }
            @Override
        protected void onPostExecute(String arg){
                progress.setVisibility(View.INVISIBLE);
                if(!arg.equalsIgnoreCase("Error"))
                    loadExtras(arg);
                else if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(findViewById(R.id.snackbarPosition),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();

            }
        }.execute(null,null,null);
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
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.descriptionFrame);
       getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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

            //Log.i(TAG," Type --> "+extraTypes.get(x));
            if(extraTypes.get(x).equalsIgnoreCase("Choice of"))
                hasFlavour = true;
            else if(extraTypes.get(x).equalsIgnoreCase("Size"))
                hasSize = true;
            else if(extraTypes.get(x).equalsIgnoreCase("Choice of2"))
                hasFlavour2 = true;


            for(int i = 0; i < extrasList.size(); i++){
                final RadioButton radioButton;
                if(extrasList.get(i).getExtraType().equals(extraTypes.get(x))&&!extrasList.get(i).getExtraType().equals("Add On")){
                    radioButton = new RadioButton(this);
                    radioButton.setTextColor(getResources().getColor(R.color.switch_thumb_disabled_material_dark));
                    /*
                    If the extra has no extra cost then do not append its string with a price
                     */

                    if(extrasList.get(i).getExtraPrice() != 0.0) {
                        radioButton.setText(extrasList.get(i).getExtra() + " R" +
                                String.format("%1$,.2f", extrasList.get(i).getExtraPrice()));
                    }
                    else
                        radioButton.setText(extrasList.get(i).getExtra());

                    /*
                    Check if the item's extras have already been loaded to the file, if they have then check those items. If
                    the item is not in the system cart but its contents are in the extras file, then remove those extras from the
                    file
                     */
                    if(!isInCart){
                        if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                            extrasEditor.remove(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID);

                    }else{
                        if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                            radioButton.setChecked(true);
                    }

                    radioButton.setId(i);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                            int pos = radioButton.getId();

                            /*
                            Calculate the total amount with the cost of the extras
                             */
                            if (compoundButton.isChecked()) {
                                newExtrasPrice = extrasList.get(pos).getExtraPrice();
                                extrasEditor.putFloat(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID, (float)extrasList.get(pos).getExtraPrice());
                                if (!selectedTypes.contains(extrasList.get(pos).getExtraType())) {
                                    price = price + newExtrasPrice;
                                    selectedTypes.add(extrasList.get(pos).getExtraType());
                                    itemPriceTV.setText("R" + String.format("%1$,.2f", price));

                                }
                                extrasSize++;
                                //Log.i("ItemDesc ","Extra Type "+extrasList.get(pos).getExtraType());
                                if(extrasList.get(pos).getExtraType().equalsIgnoreCase("Choice of"))
                                    isFlavoured = true;
                                else if(extrasList.get(pos).getExtraType().equalsIgnoreCase("Size"))
                                    isSized = true;
                                else if(extrasList.get(pos).getExtraType().equalsIgnoreCase("Choice of2"))
                                    isFlavoured2 = true;
                               // extrasEditor.putInt("Size",extrasSize);

                            } else {
                                extrasEditor.remove(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID);
                                prevExtrasPrice = extrasList.get(pos).getExtraPrice();
                                price = price - prevExtrasPrice;
                                price = price + newExtrasPrice;
                                itemPriceTV.setText("R" + String.format("%1$,.2f", price));
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
                    if(!isInCart){
                        if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                            extrasEditor.remove(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID);
                    }else{
                        if(extrasSharedPreferences.contains(extrasList.get(i).getExtra()+"-"+extrasList.get(i).getExtraID()+"_"+menuID))
                            checkBox.setChecked(true);
                    }
                    checkBox.setId(i);
                    checkBox.setText(extrasList.get(i).getExtra() + " R" + String.format("%1$,.2f", extrasList.get(i).getExtraPrice()));
                    checkBox.setTextColor(getResources().getColor(R.color.switch_thumb_disabled_material_dark));
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int pos = checkBox.getId();
                            if(compoundButton.isChecked()){
                                newExtrasPrice = extrasList.get(pos).getExtraPrice();
                                    extrasEditor.putFloat(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID, (float)extrasList.get(pos).getExtraPrice());
                                    price = price + newExtrasPrice;
                                    selectedTypes.add(extrasList.get(pos).getExtraType());
                                    itemPriceTV.setText("R" + String.format("%1$,.2f", price));
                                    extrasSize++;
                                    //extrasEditor.putInt("Size",extrasSize);

                            }else{
                                extrasEditor.remove(extrasList.get(pos).getExtra()+"-"+extrasList.get(pos).getExtraID()+"_"+menuID);
                                prevExtrasPrice = extrasList.get(pos).getExtraPrice();
                                price = price - prevExtrasPrice;
                                itemPriceTV.setText("R" + String.format("%1$,.2f", price));
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

    void checkIfInCart()
    {
        int size = sharedPreferences.getInt("Size",0);
        //Log.i("Pref Size",""+size);
        for(int x = 0; x < size; x++)
        {
            if(selectedItem.equals(sharedPreferences.getString("Selected Item"+x,null))) {
                addToCart.setImageResource(R.drawable.ic_buy_red);
                isInCart = true;
            }
        }
    }
    void addToCart()
    {


        editor.putString("Selected Item" + size, selectedItem);
        editor.putInt("menuID" + size, menuID);
        editor.putFloat("Item Price" + size, Float.valueOf(itemPriceTV.getText().toString().substring(1)));
        editor.putInt("Quantity" + size, 1);
        editor.putBoolean("hasExtras"+size,hasExtras);
        size++;
        editor.putInt("Size",size);
        editor.commit();
        extrasEditor.commit();
        //Log.i("size", "Size" + size);
        //Log.i("","ALL EXTRAS "+extrasSharedPreferences.getAll().toString());
        Snackbar.make(MenuItemsListActivity.menuListActivty.findViewById(R.id.snackbarPositionList),
                "Added To Cart", Snackbar.LENGTH_SHORT).show();
//        hideDialog();
        finish();
    }

    void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage(R.string.add_to_cart);
        builder.setNegativeButton("Cancel",null);
        builder.setPositiveButton("Add",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(menuID > 0) {
                    addToCart();
                    isInCart = false;
                }else{
                    int x;
                    if(getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                            contains(CommonIdentifiers.getMenuIdError()))
                        x = getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                                getInt(CommonIdentifiers.getMenuIdError(),0)+1;
                    else
                        x = 0;

                    getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                            putInt(CommonIdentifiers.getMenuIdError(),x).apply();
                    if(x >= 3)
                        Snackbar.make(findViewById(R.id.snackbarPosition),
                                "A persistent error has been detected. Please rescan the table code", Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(findViewById(R.id.snackbarPosition),
                                "Something really went wrong, please try again.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
        /*
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_cart_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Button yesButton = (Button)dialog.findViewById(R.id.yesAdd);
        Button noButton = (Button)dialog.findViewById(R.id.noAdd);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialog();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menuID > 0) {
                    addToCart();
                    isInCart = false;
                }else{
                    hideDialog();
                    int x;
                    if(getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                            contains(CommonIdentifiers.getMenuIdError()))
                        x = getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).
                                getInt(CommonIdentifiers.getMenuIdError(),0)+1;
                    else
                        x = 0;

                    getSharedPreferences(AppSharedPreferences.getSettings(),MODE_PRIVATE).edit().
                            putInt(CommonIdentifiers.getMenuIdError(),x).apply();
                    if(x >= 3)
                        Snackbar.make(findViewById(R.id.snackbarPosition),
                                "A persistent error has been detected. Please rescan the table code", Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(findViewById(R.id.snackbarPosition),
                                "Something really went wrong, please try again.", Snackbar.LENGTH_LONG).show();
                }
            }
        });*/
    }

    void hideDialog()
    {
        dialog.dismiss();
    }

   /* @Override
    public void onPause()
    {
        super.onPause();
        finish();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_item_description, menu);
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
