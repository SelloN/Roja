package com.natech.roja.CheckOut;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tshepo on 2015/06/21.
 */
@SuppressWarnings("DefaultFileTemplate")
public class OrderedFragment extends Fragment {

    FragmentActivity fragmentActivity;
    private SharedPreferences sharedPreferencesOrders;
    private RecyclerView recyclerView;
    private OrderListAdapter orderListAdapter;
    private List<CartItem> orderList;
    public TextView actualTotalTV, totalTV;
    private RelativeLayout noOrdersTV;
    public float actualTotal, potentialTotal;
    private Boolean isOrders = false;
    private String tableLogID;
    private LinearLayout progess;
    public static OrderedFragment orderedFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.ordered_fragment,container,false);
        fragmentActivity = getActivity();
        sharedPreferencesOrders = fragmentActivity.getApplicationContext().getSharedPreferences("OrderList", Context.MODE_PRIVATE);
        recyclerView = (RecyclerView)v.findViewById(R.id.orderedRV);
        actualTotalTV = (TextView)fragmentActivity.findViewById(R.id.actualTotalTV);
        totalTV = (TextView)fragmentActivity.findViewById(R.id.totalTV);
        noOrdersTV = (RelativeLayout)v.findViewById(R.id.noOrdersTV);
        progess = (LinearLayout)v.findViewById(R.id.progressOrder);
        RelativeLayout noNetwork = (RelativeLayout)v.findViewById(R.id.noNetwork);
        orderedFragment = this;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(fragmentActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        if(NetworkUtil.getConnectivityStatus(getActivity()))
            loadOrders();
        else {
            noNetwork.setVisibility(View.VISIBLE);
            Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
        return v;
    }

    public void loadOrders()
    {

        sharedPreferencesOrders = fragmentActivity.getApplicationContext().
                getSharedPreferences(AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE);
        tableLogID = sharedPreferencesOrders.getString(CommonIdentifiers.getTableLogId(),"");

        orderList = new ArrayList<>();
        orderListAdapter = new OrderListAdapter(orderList,fragmentActivity);

        new AsyncTask<String, Void, String>(){
            @Override
            protected void onPreExecute(){
                noOrdersTV.setVisibility(View.INVISIBLE);
                progess.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(String... strings) {

                 URL url;
            try {
                url = new URL(Server.getOrder());
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                String postData = URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode("cart","UTF-8");
                postData += "&"+URLEncoder.encode("tableLogID","UTF-8")+"="+URLEncoder.encode(tableLogID,"UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(postData);
                outputStreamWriter.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                //Log.i("", "New Response : " + line);
                if(line != null){
                    loadOrderedList(line);
                }
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
                return "";
            }

            @Override
            protected void onPostExecute(String arg){
                progess.setVisibility(View.INVISIBLE);
                if(arg.equalsIgnoreCase("Error"))
                    Snackbar.make(getActivity().findViewById(R.id.snackbarPositionCart),
                            "Network Problems. Please Try Again", Snackbar.LENGTH_LONG).show();


            }
        }.execute(null, null, null);
    }

    void loadOrderedList(String jsonResult){
        try{
            actualTotal = 0;
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("order");
            String menuItem;
            String itemPrice;
            String quantity, orderID;
            int status;
            float totalItemPrice;
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                menuItem = jsonChildNode.optString("menuItem");
                itemPrice = jsonChildNode.optString("itemPrice");
                quantity = jsonChildNode.optString("quantity");
                status = jsonChildNode.optInt("status");
                orderID = jsonChildNode.optString("orderID");
                String time = jsonChildNode.optString("time").substring(11,16);
                totalItemPrice = Float.parseFloat(itemPrice);

                orderList.add(new CartItem(orderID,menuItem,totalItemPrice,Integer.parseInt(quantity),0,false,time,status));
                if(status == 1)
                    actualTotal = actualTotal + totalItemPrice;
                isOrders = true;
            }

            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isOrders){
                        noOrdersTV.setVisibility(View.INVISIBLE);
                        recyclerView.setAdapter(orderListAdapter);
                    }
                    else
                        noOrdersTV.setVisibility(View.VISIBLE);

                    potentialTotal = CheckOutFragment.checkOutFragment.cartTotal + actualTotal;
                    totalTV.setText("R"+String.format("%1$,.2f",potentialTotal));
                    actualTotalTV.setText("R"+String.format("%1$,.2f",actualTotal));
                }
            });




        }catch (JSONException e){
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    noOrdersTV.setVisibility(View.VISIBLE);
                }
            });

            //Log.i("JASON ",e.toString());
        }
    }

}
