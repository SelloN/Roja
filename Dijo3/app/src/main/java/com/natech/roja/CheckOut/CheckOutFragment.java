package com.natech.roja.CheckOut;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.natech.roja.LogIn.Address;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by Tshepo on 2015/06/21. This class handles the core and critical functions of the app.
 * It sorts out the items the user has added into the cart and lists them accordingly. It also updates
 * the OrderedFragment whenever orders are sent through to the server.
 */
public class CheckOutFragment extends Fragment {

    private RecyclerView recyclerView;
    private int size, addressPosition;
    private List<CartItem> cartItemList;
    private CartListAdapter cartListAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView cartTotalTV;
    private TextView totalTV;
    private TextView actualTotalTV;
    private FragmentActivity fragmentActivity;
    float cartTotal;
    private float potentialTotal;
    private Boolean isCartLoaded = false;
    private Boolean isCartEmpty = true;
    private Dialog dialog;
    private Dialog dialog2;

    //TextView emptyCartTV;
    private static final String TAG = "Check Out";
    private final static String ID_FILE ="tableIDs";
    private SharedPreferences idFile;
    private SharedPreferences extrasSharedPreferences;
    private SharedPreferences.Editor idFileEditor;
    private SharedPreferences.Editor extrasEditor;
    public static String tableLogID, tableID, waiterRegID;
    private String type;
    private String userID;
    private String userEmail;
    private final static String TABLE_LOG_ID = "tableLogID",TABLE_ID = "tableID"
            ,LOG_ID_SENT = "logIDSuccess", EXTRAS_FILE = "extras",TYPE = "type",
            REST_EMAIL = "restEmail", EMAIL = "email";
    private RelativeLayout emptyCartTV;
    private ProgressDialog progressDialog;
    private String restEmail;
    private Boolean logIDSent = false;
    private Boolean isFirstAttempt = false, isHomeDelivery = false;
    public static CheckOutFragment checkOutFragment;
    private List<Address> addressList;
    private AlertDialog collectionDeliveryAlert, addressAlert;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.check_out_fragment,container,false);


        fragmentActivity = getActivity();
        cartTotalTV = (TextView)fragmentActivity.findViewById(R.id.cartTotalTV);
        totalTV = (TextView)fragmentActivity.findViewById(R.id.totalTV);
        actualTotalTV = (TextView)fragmentActivity.findViewById(R.id.actualTotalTV);
        emptyCartTV = (RelativeLayout)v.findViewById(R.id.emptyCartTV);
        idFile = getActivity().getSharedPreferences(ID_FILE, Context.MODE_PRIVATE);
        idFileEditor = idFile.edit();
        type = idFile.getString(TYPE,null);
        //Log.i("","====> "+type);
        if(!idFile.contains(LOG_ID_SENT))
            idFileEditor.putBoolean(LOG_ID_SENT,false);

        if(type.equalsIgnoreCase("offSite"))
            restEmail = idFile.getString(REST_EMAIL,null);

        logIDSent = idFile.getBoolean(LOG_ID_SENT,false);
        waiterRegID = idFile.getString(CommonIdentifiers.getWaiterRegId(),"");
        userID = idFile.getString(CommonIdentifiers.getUserId(),"");
        userEmail = idFile.getString(EMAIL,"");
        tableID = idFile.getString(TABLE_ID,"");
        tableLogID = idFile.getString(TABLE_LOG_ID, "");
        ///Log.i(TAG,"tableLogID  "+tableLogID);
        final FloatingActionButton clearOrderFAB =
                (FloatingActionButton)CheckOutActivity.checkOutActivity.findViewById(R.id.clearCartFAB);
        final FloatingActionButton sendOrderFAB =
                (FloatingActionButton)CheckOutActivity.checkOutActivity.findViewById(R.id.sendOrderFAB);
        final FloatingActionsMenu floatingActionsMenu =
                (FloatingActionsMenu)CheckOutActivity.checkOutActivity.findViewById(R.id.floatMenu);

        final FloatingActionButton cancelOrderFAB = (FloatingActionButton)CheckOutActivity.checkOutActivity.findViewById(R.id.cancelOrderFAB);

        if(type.equalsIgnoreCase("offSite"))
            cancelOrderFAB.setVisibility(View.VISIBLE);

        final Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_in);
        floatingActionsMenu.startAnimation(animation);

        clearOrderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();
                showDialog();
            }
        });

        sendOrderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();

                //Log.i(TAG,"home+++++ "+getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                 //       Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getHomeDelivery(),false));
                if(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getHomeDelivery(),false))
                    showDeliveryCollectionDialog();
                else
                   showDialog2();
            }
        });

        cancelOrderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();
                showDialog3();
            }
        });
        setHasOptionsMenu(true);

        recyclerView = (RecyclerView)v.findViewById(R.id.cartRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(fragmentActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        sharedPreferences = fragmentActivity.getApplicationContext().getSharedPreferences("CartList", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        extrasSharedPreferences = fragmentActivity.getApplicationContext().getSharedPreferences(EXTRAS_FILE,Context.MODE_PRIVATE);
        extrasEditor = extrasSharedPreferences.edit();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("extras",extrasSharedPreferences.getAll());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.i(TAG, "Extra JSON = " + jsonObject.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadCartList();
            }
        }).start();
        checkOutFragment =this;
        floatingActionsMenu.post(new Runnable() {
            @Override
            public void run() {

                final String TUTORIAL = "LearnedCheckOutFragment";

                if(!getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                        Context.MODE_PRIVATE).getBoolean(TUTORIAL,false)) {
                    ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity()).hideOnTouchOutside().
                            setTarget(new ViewTarget(R.id.clearCartFAB, getActivity())).
                            setContentTitle("Cart Options").setContentText("Touch this button to view the options " +
                            "to send your order to the waiter or clear your cart").
                            setStyle(R.style.CustomShowcaseTheme3).build();
                    showcaseView.setButtonText("Got it!");
                    RelativeLayout.LayoutParams layoutParams
                            = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                    layoutParams.setMargins(15, 15, 15, 15);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    showcaseView.setButtonPosition(layoutParams);
                    showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                                        Context.MODE_PRIVATE).edit().putBoolean(TUTORIAL,true).apply();
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }
                    });
                }
            }
        });
        return v;
    }

    void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setMessage(R.string.clear_cart);
        builder.setNegativeButton("Cancel",null);
        builder.setPositiveButton("Clear",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clearCart();
            }
        });
        builder.show();
    }

    void showDeliveryCollectionDialog(){

        //final AlertDialog levelDialog;
        final CharSequence[] items = {" Collection "," Delivery "};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        //builder.setTitle("Collec");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {


                switch (item) {
                    case 0:
                        isHomeDelivery = false;
                        showDialog2();
                        break;
                    case 1:
                        new GetAddresses().execute();
                        break;

                }

                collectionDeliveryAlert.dismiss();
            }
        });
        collectionDeliveryAlert = builder.create();
        collectionDeliveryAlert.show();
    }

    class GetAddresses extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            showProgressDialog("Getting your address book...");
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Server.getAddressBook());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String postData = URLEncoder.encode(CommonIdentifiers.getUserId(), "UTF-8")+"="+
                        URLEncoder.encode(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).
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
        protected void onPostExecute(String args){
            hideProgressDialog();
            if(!args.equalsIgnoreCase("error")){
                loadAddressBook(args);
            }else
                Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                        "Something went wrong. Please try again", Snackbar.LENGTH_LONG).show();

        }
    }

    void loadAddressBook(String json){

        addressList = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Choose Delivery Address");
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
            //Log.i(TAG,"size == "+addressList.size());
            final CharSequence[] items = new CharSequence[addressList.size()];
            int x = 0;
            while(x < addressList.size()){
                items[x] = addressList.get(x).getLabel();
                //Log.i(TAG,"label == "+addressList.get(x).getLabel()+" i == "+i+" x== "+x);
                x++;
            }
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    isHomeDelivery = true;
                    addressPosition = item;
                    showDialog2();
                    addressAlert.dismiss();
                }
            });
            addressAlert = builder.create();
            addressAlert.show();


        }catch (JSONException e){
            Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                    "Something went wrong. Please Try Again", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, "Error" + e.toString(),
            //      Toast.LENGTH_SHORT).show();
            //Log.i("", e.toString());
        }


    }



    void showDialog2()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setMessage(R.string.send_order);
        builder.setPositiveButton("Send",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(NetworkUtil.getConnectivityStatus(getActivity())) {
                    if(cartItemList.size() > 0) {
                        showProgressDialog("Sending Order... Please Wait");
                        if (type.equalsIgnoreCase("onSite")) {
                            try {
                                sendOrder();
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                                hideProgressDialog();
                                getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                                        Context.MODE_PRIVATE).edit().putBoolean(CommonIdentifiers.getFatalOrderError(),true).
                                        apply();
                                Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                        "Fatal Error! Please rescan the table QR code", Snackbar.LENGTH_LONG).show();
                            }

                        }
                        else if (type.equalsIgnoreCase("offSite")) {
                            if(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getPreviousOffsite(),false)
                                    && getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null) != null
                                    && !getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null).isEmpty() ) {
                                Log.i(TAG,"++++++ "+getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null)+" === "
                                        +getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                        Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getPreviousOffsite(),false));
                                sendOrderViaEmail(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null));
                            }else
                                getLogID();
                        }
                    }else
                        Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                "You Have No Items In Cart", Snackbar.LENGTH_LONG).show();
                }else
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
        /*
        dialog2 = new Dialog(CheckOutActivity.checkOutActivity);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.send_order_dialog);
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.show();
        Button yesButton = (Button)dialog2.findViewById(R.id.yesSend);
        Button noButton = (Button)dialog2.findViewById(R.id.noSend);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialog2();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(NetworkUtil.getConnectivityStatus(getActivity())) {
                    if(cartItemList.size() > 0) {
                        showProgressDialog("Sending Order... Please Wait");
                        if (type.equalsIgnoreCase("onSite")) {
                            try {
                                sendOrder();
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                                hideProgressDialog();
                                getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                                        Context.MODE_PRIVATE).edit().putBoolean(CommonIdentifiers.getFatalOrderError(),true).
                                        apply();
                                Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                        "Fatal Error! Please rescan the table QR code", Snackbar.LENGTH_LONG).show();
                            }

                        }
                        else if (type.equalsIgnoreCase("offSite")) {
                            if(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getPreviousOffsite(),false)
                                    && getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null) != null
                                    && !getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                    Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null).isEmpty() ) {
                                    sendOrderViaEmail(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                                            Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null));
                            }else
                                getLogID();
                        }
                    }else
                        Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                "You Have No Items In Cart", Snackbar.LENGTH_LONG).show();
                }else
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "No Internet Connection", Snackbar.LENGTH_LONG).show();
                hideDialog2();
            }
        });*/
    }

    void showDialog3(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setMessage("Cancel Order?");
        builder.setNegativeButton("No",null);
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getPreviousOffsite(),false)
                        && getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null) != null
                        && !getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null).isEmpty() ) {
                    cancelOffSiteOrder(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                            Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null));
                };
            }
        });
        builder.show();
        /*
        dialog = new Dialog(CheckOutActivity.checkOutActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.clear_cart_dialog);
        TextView message = (TextView)dialog.findViewById(R.id.messageCartTV);
        message.setText("Cancel Order?");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Button yesButton = (Button)dialog.findViewById(R.id.yesClear);
        Button noButton = (Button)dialog.findViewById(R.id.noClear);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialog();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialog();
                if(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getBoolean(CommonIdentifiers.getPreviousOffsite(),false)
                        && getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null) != null
                        && !getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                        Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null).isEmpty() ) {
                    cancelOffSiteOrder(getActivity().getSharedPreferences(AppSharedPreferences.getIdFile(),
                            Context.MODE_PRIVATE).getString(CommonIdentifiers.getTableLogId(),null));
                };
            }
        });*/
    }

    void showProgressDialog(String message){
        progressDialog = new ProgressDialog(fragmentActivity);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideProgressDialog(){
        progressDialog.dismiss();
    }

    void hideDialog()
    {
        dialog.dismiss();
    }
    void hideDialog2()
    {
        dialog2.dismiss();
    }

    void clearCart()
    {

        size = cartListAdapter.getItemCount();
        //Log.i("vis", "size of list "+size);
        //Log.i("vis", "size of adapter "+size);

       for (int x = 0; x < size; x++)
        {
            cartListAdapter.remove();
        }
        recyclerView.setAdapter(cartListAdapter);
        extrasEditor.clear();
        extrasEditor.apply();
        editor.clear();
        editor.apply();
        cartTotal = 0;
        potentialTotal = Float.valueOf(totalTV.getText().toString().substring(1));
        //Log.i("Check Out", ""+potentialTotal);
        totalTV.setText("R" + String.format("%1$,.2f", potentialTotal - Float.valueOf(cartTotalTV.getText().toString().substring(1))));
        cartTotalTV.setText("R0.00");
        emptyCartTV.setVisibility(View.VISIBLE);

        Snackbar.make(CheckOutActivity.checkOutActivity.findViewById(R.id.snackbarPositionCart),
                "Cart Cleared", Snackbar.LENGTH_SHORT).show();
    }

    void resetTotalTextView(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cartItemList.clear();
                cartListAdapter.clearList();
                recyclerView.setAdapter(cartListAdapter);
                emptyCartTV.setVisibility(View.VISIBLE);
                hideProgressDialog();
                cartTotalTV.setText("R0.00");
                totalTV.setText("R0.00");
                Snackbar.make(CheckOutActivity.checkOutActivity.findViewById(R.id.snackbarPositionCart),
                        "Order Sent", Snackbar.LENGTH_SHORT).show();
                OrderedFragment.orderedFragment.loadOrders();
            }
        });
    }

    void  sendOrderViaEmail(String tableLogID){
        size = cartListAdapter.getItemCount();
        Boolean isFatalError = false;
        final JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Log.i(TAG,"loyalty = "+getActivity().getSharedPreferences(
                AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getLoyaltySystem(),null));


        try {
            JSONObject json = new JSONObject();
            json.put("tableLogID",tableLogID);
            json.put("restEmail",restEmail);
            json.put("userEmail",userEmail);
            json.put(CommonIdentifiers.getFranchId(),getActivity().getSharedPreferences
                    (AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getFranchId(),null));
            json.put(CommonIdentifiers.getRestName(),getActivity().getSharedPreferences(
                    AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getRestName(),null));
            json.put(CommonIdentifiers.getRestId(),getActivity().getSharedPreferences(
                    AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getRestId(),null));
            json.put(CommonIdentifiers.getUserId(),getActivity().getSharedPreferences(
                    AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getUserId(),null));
            json.put(CommonIdentifiers.getLoyaltySystem(),getActivity().getSharedPreferences(
                    AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).getString(CommonIdentifiers.getLoyaltySystem(),null));
            if(isHomeDelivery) {
                json.put("streetAddress",addressList.get(addressPosition).getStreetAddress() );
                json.put("complex",addressList.get(addressPosition).getComplexName());
                json.put("province",addressList.get(addressPosition).getProvince());
            }else
                json.put("deliveryType","collection");

            jsonArray.put(json);
            cartLoop:for(int x = 0;x < size;x++) {
                if (cartItemList.get(x).getMenuID() > 0 && Integer.parseInt(tableLogID) > 0 &&
                        Integer.parseInt(userID) > 0 && Integer.parseInt(tableID) > 0) {
                    JSONObject jsonChild = new JSONObject();
                    jsonChild.put("item", cartItemList.get(x).getCartItem());
                    jsonChild.put("qauntity", cartItemList.get(x).getQuantity());
                    jsonChild.put("menuID", cartItemList.get(x).getMenuID());
                    jsonChild.put("price", cartItemList.get(x).getPrice());
                    if (cartItemList.get(x).getExtrasAdded()) {
                        String extras = "";
                        for (int i = 0; i < cartItemList.get(x).getExtrasList().size(); i++) {
                            extras += cartItemList.get(x).getExtrasList().get(i) + ", ";
                        }
                        extras = extras.trim();
                        extras = extras.substring(0, extras.length() - 1);
                        jsonChild.put("extras", extras);
                    } else
                        jsonChild.put("extras", "-");

                    if (cartItemList.get(x).getHasNote())
                        jsonChild.put("note", cartItemList.get(x).getNote());
                    else
                        jsonChild.put("note", "-");

                    jsonArray.put(jsonChild);
                }else{
                    hideProgressDialog();
                    getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                            Context.MODE_PRIVATE).edit().putBoolean(CommonIdentifiers.getFatalOrderError(),true).
                            apply();
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Fatal Error! Please rescan the table QR code", Snackbar.LENGTH_LONG).show();
                    isFatalError = true;
                    break cartLoop;

                }

                jsonObject.put("order", jsonArray);
                //Log.i(TAG, "Order VIA Email ===> " + jsonObject.toString());
            }
        }
        catch (JSONException e) {
            hideProgressDialog();

            e.printStackTrace();
        }



        if(!isFatalError) {
            new Thread(new Runnable() {
                @SuppressWarnings({"TryWithIdenticalCatches", "deprecation"})
                @Override
                public void run() {
                    InputStream inputStream;
                    String result;
                    try {
                        URL url = new URL(Server.getPlaceOffSiteOrder());
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Content-type", "application/json");
                        connection.setDoInput(true);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                        outputStreamWriter.write(jsonObject.toString());
                        outputStreamWriter.flush();
                        outputStreamWriter.close();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        result = reader.readLine();

                        /*HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(Server.getPlaceOffSiteOrder());
                        StringEntity se = new StringEntity(jsonObject.toString());
                        httpPost.setEntity(se);
                        httpPost.setHeader("Accept", "application/json");
                        httpPost.setHeader("Content-type", "application/json");
                        HttpResponse httpResponse = httpclient.execute(httpPost);

                        inputStream = httpResponse.getEntity().getContent();
                        if (inputStream != null)
                            result = convertInputStreamToString(inputStream);
                        else
                            result = "Did not work!";*/

                        //Log.i(TAG, "New Response : " + result);
                        //resetTotalTextView();
                        hideProgressDialog();
                        if (result != null) {
                            if (result.equalsIgnoreCase("Order Successful")) {
                                editor.clear();
                                editor.commit();
                                extrasEditor.clear();
                                extrasEditor.commit();
                                cartTotal = 0;
                                resetTotalTextView();


                            } else {
                                Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                        "Something Went Wrong. Please Try Again", Snackbar.LENGTH_LONG).show();

                            }

                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        hideProgressDialog();
                        Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        hideProgressDialog();
                        Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
                    }

                }
            }).start();
        }

    }

    void sendOrder()throws NumberFormatException
    {
        size = cartListAdapter.getItemCount();
        tableID = idFile.getString(TABLE_ID,"");
        Boolean isFatalError = false;
        final JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            JSONObject json = new JSONObject();
            json.put("tableLogID",tableLogID);
            json.put("userID",userID);
            json.put("tableID",tableID);
            jsonArray.put(json);
            cartLoop:for(int x = 0;x < size;x++){
                if(cartItemList.get(x).getMenuID() > 0 && Integer.parseInt(tableLogID) > 0 &&
                        Integer.parseInt(userID) > 0 && Integer.parseInt(tableID) > 0) {
                    JSONObject jsonChild = new JSONObject();
                    jsonChild.put("item", cartItemList.get(x).getCartItem());
                    jsonChild.put("qauntity", cartItemList.get(x).getQuantity());
                    jsonChild.put("menuID", cartItemList.get(x).getMenuID());
                    jsonChild.put("price", cartItemList.get(x).getPrice());
                    if (cartItemList.get(x).getExtrasAdded()) {
                        String extras = "";
                        for (int i = 0; i < cartItemList.get(x).getExtrasList().size(); i++) {
                            extras += cartItemList.get(x).getExtrasList().get(i) + ", ";
                        }
                        extras = extras.trim();
                        extras = extras.substring(0, extras.length() - 1);
                        jsonChild.put("extras", extras);
                    } else
                        jsonChild.put("extras", "none");

                    if (cartItemList.get(x).getHasNote())
                        jsonChild.put("note", cartItemList.get(x).getNote());
                    else
                        jsonChild.put("note", "none");

                    jsonArray.put(jsonChild);
                }else{
                    hideProgressDialog();
                    getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                            Context.MODE_PRIVATE).edit().putBoolean(CommonIdentifiers.getFatalOrderError(),true).
                            apply();
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Fatal Error! Please rescan the table QR code", Snackbar.LENGTH_LONG).show();
                    isFatalError = true;
                    break cartLoop;

                }
            }

            jsonObject.put("order",jsonArray);
            //Log.i(TAG,jsonObject.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


       if(!isFatalError) {
           final Thread thread = new Thread(new Runnable() {
               @SuppressWarnings("TryWithIdenticalCatches")
               @Override
               public void run() {
                   String result;
                   try {
                       URL url = new URL(Server.getPlaceOrder());
                       HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                       urlConnection.setDoOutput(true);
                       urlConnection.setDoInput(true);
                       urlConnection.setRequestProperty("Accept", "application/json");
                       urlConnection.setRequestProperty("Content-type", "application/json");
                       urlConnection.setRequestMethod("POST");
                       OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                       outputStreamWriter.write(jsonObject.toString());
                       outputStreamWriter.flush();
                       outputStreamWriter.close();
                       BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                       result = reader.readLine();

                       //Log.i(TAG, "New Response : " + result);
                       if (result != null) {
                           if (result.equalsIgnoreCase("Order Successful")) {
                               editor.clear();
                               editor.apply();
                               extrasEditor.clear();
                               extrasEditor.apply();
                               cartTotal = 0;

                               if (logIDSent && !isFirstAttempt) {
                                   new Thread(new Runnable() {
                                       @Override
                                       public void run() {
                                           if (NetworkUtil.getConnectivityStatus(getActivity()))
                                               sendOrderViaGCM(jsonObject);
                                           else {
                                               hideProgressDialog();
                                               Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                                       "No Internet Connection", Snackbar.LENGTH_LONG).show();
                                           }
                                       }
                                   }).start();
                               }
                               resetTotalTextView();


                           } else if (result.equalsIgnoreCase("Unavailable")) {
                               hideProgressDialog();
                               Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                       "Waiter Unavailable", Snackbar.LENGTH_LONG).show();
                           } else {
                               hideProgressDialog();
                               Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                       "Something went wrong. Please try again", Snackbar.LENGTH_LONG).show();
                           }

                       }

                   } catch (MalformedURLException e) {
                       e.printStackTrace();
                       hideProgressDialog();
                       Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                               "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
                   } catch (IOException e) {
                       e.printStackTrace();
                       hideProgressDialog();
                       Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                               "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
                   }

               }
           });

           if (!logIDSent) {
               new Thread(new Runnable() {
                   @Override
                   public void run() {

                       if (NetworkUtil.getConnectivityStatus(getActivity())) {
                           sendOrderViaGCM(jsonObject);
                           if (logIDSent)
                               thread.start();
                           else {
                               hideProgressDialog();
                               Snackbar.make(CheckOutActivity.checkOutActivity.findViewById(R.id.snackbarPositionCart),
                                       "Order Failed. Please Try Again", Snackbar.LENGTH_SHORT).show();
                           }
                       } else {
                           hideProgressDialog();
                           Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                                   "No Internet Connection", Snackbar.LENGTH_LONG).show();
                       }
                   }
               }).start();

           } else
               thread.start();
       }

    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private void sendOrderViaGCM(JSONObject jsonObject){
        URL url;
        try {
            Log.i(TAG,"Attempting GCM POST");
            url = new URL(Server.getGCMOrder());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setDoOutput(true);
            String postData = URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode("Order","UTF-8");
            postData += "&"+URLEncoder.encode("registrationIDs","UTF-8")+"="+URLEncoder.encode(waiterRegID,"UTF-8");
            postData += "&"+URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(tableID,"UTF-8");
            postData += "&"+URLEncoder.encode("order","UTF-8")+"="+URLEncoder.encode(jsonObject.toString(),"UTF-8");
            postData += "&"+URLEncoder.encode(CommonIdentifiers.getRegId(),"UTF-8")+"="+
                    URLEncoder.encode(getActivity().getSharedPreferences(AppSharedPreferences.getCredentialsFile(),
                            Context.MODE_PRIVATE).getString(AppSharedPreferences.getRegistrationId(), null),"UTF-8");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
            outputStreamWriter.write(postData);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            reader.close();
            Log.i(TAG, "New Response : " + line);
            if(line != null){
                if(line.contains("success")){

                    idFileEditor.putBoolean(LOG_ID_SENT,true);
                    idFileEditor.commit();
                    logIDSent = true;
                    isFirstAttempt = true;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            hideProgressDialog();
            Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                    "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            hideProgressDialog();
            Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                    "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            hideProgressDialog();
            Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                    "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();
        }

    }

    void loadCartList()
    {
        cartItemList = new ArrayList<>();
        cartListAdapter = new CartListAdapter(fragmentActivity,cartItemList,cartTotalTV,totalTV,emptyCartTV);
        recyclerView.setAdapter(cartListAdapter);
        //Log.i("Check Out", "loading list");
            //extrasEditor.putInt("Size",0);
            //extrasEditor.commit();

        size = sharedPreferences.getInt("Size",0);
        cartTotal = 0;
        potentialTotal = Float.valueOf(actualTotalTV.getText().toString());
        //List<Extras> extrasList = new ArrayList<>();
        Map<String, ?> allExtras = extrasSharedPreferences.getAll();
        for(int x = 0; x < size; x++)
        {
            CartItem cartItem = new CartItem(sharedPreferences.getString("Selected Item"+x,null),
                    sharedPreferences.getFloat("Item Price"+x,0),sharedPreferences.getInt("Quantity"+x,0),
                    sharedPreferences.getInt("menuID"+x,0),sharedPreferences.getBoolean("hasExtras"+x,false));

            if(extrasSharedPreferences.contains("note"+(sharedPreferences.getInt("menuID" + x, 0)))){
                if(!extrasSharedPreferences.getString("note"+(sharedPreferences.getInt("menuID" + x, 0)),null).equals(""))
                {
                    //Log.i("d","Has Note "+extrasSharedPreferences.getString("note" + (sharedPreferences.getInt("menuID" + x, 0)), null));
                    cartItem.setNote(extrasSharedPreferences.getString("note" + (sharedPreferences.getInt("menuID" + x, 0)), null));
                    cartItem.setHasNote(true);
                }else
                    cartItem.setHasNote(false);
            }

            if(sharedPreferences.getBoolean("hasExtras"+x,false)){
                for (Map.Entry<String, ?> entry : allExtras.entrySet()) {
                    if (entry.getKey().substring(entry.getKey().indexOf("_") + 1).equals(String.valueOf(sharedPreferences.getInt("menuID" + x, 0)))) {

                        String extra = entry.getKey().substring(0, entry.getKey().indexOf("-"));
                        cartItem.addExtra(extra);
                        cartItem.setExtrasAdded(true);

                    }
                }
                cartItemList.add(cartItem);

            }else
                cartItemList.add(cartItem);

            cartTotal = sharedPreferences.getFloat("Item Price"+x,0)+cartTotal;
            isCartLoaded = true;
            isCartEmpty = false;
        }


        if(isCartEmpty)
        {
            emptyCartTV.setVisibility(View.VISIBLE);
        }
        else
            emptyCartTV.setVisibility(View.INVISIBLE);
        if(isCartLoaded)
            totalTV.setText("R"+String.format("%1$,.2f",cartTotal+potentialTotal));

        cartTotalTV.setText("R"+String.format("%1$,.2f",cartTotal));
    }

    void getLogID(){


        new AsyncTask<String, Void, String>(){



            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(Server.getTableLogID());
                    URLConnection connection = url.openConnection();


                    String postData = URLEncoder.encode("userID","UTF-8")+"="+URLEncoder.encode(userID,"UTF-8");
                    postData += "&"+URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(tableID,"UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    //Log.i(TAG, "New Response Here : " + line);

                    if(line != null){
                        if(line.contains("LastID")){
                            tableLogID = line.substring(6);
                            //Log.i(TAG, "Table Log ID : " + tableLogID);
                            idFileEditor.putString(TABLE_LOG_ID,tableLogID);
                            idFileEditor.commit();
                            return tableLogID;

                        }else
                            return "Error";
                    }
                    else {

                        return "Error";

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "Error";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String arg){
                if(!arg.equalsIgnoreCase("Error"))
                    sendOrderViaEmail(arg);
                else if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();

            }
        }.execute(null, null, null);
    }

    private void cancelOffSiteOrder(final String logID){
        new AsyncTask<String, Void, String>(){

           @Override
           protected  void onPreExecute(){
               showProgressDialog("Cancelling Order");
           }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(Server.getCancelOffsiteOrder());
                    URLConnection connection = url.openConnection();


                    String postData = URLEncoder.encode(CommonIdentifiers.getTableLogId(),"UTF-8")+"="+URLEncoder.encode(logID,"UTF-8");
                    postData += "&"+URLEncoder.encode("restEmail","UTF-8")+"="+URLEncoder.encode(restEmail,"UTF-8");
                    postData += "&"+URLEncoder.encode("userEmail","UTF-8")+"="+URLEncoder.encode(userEmail,"UTF-8");
                    connection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    Log.i(TAG, "New Response Here : " + line);

                    if(line != null)
                        return line;
                    else
                        return "Error";

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "Error";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String arg){
                hideProgressDialog();
                if(arg.equalsIgnoreCase("Successful")) {
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Order Cancelled", Snackbar.LENGTH_LONG).show();
                    OrderedFragment.orderedFragment.loadOrders();
                }
                if(arg.equalsIgnoreCase("Nothing"))
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "No Order to Cancel", Snackbar.LENGTH_LONG).show();
                else if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();

            }
        }.execute(null, null, null);

    }

}
