package com.natech.roja.CheckOut;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Sello on 2015/06/22.
 */
@SuppressWarnings({"TryWithIdenticalCatches", "DefaultFileTemplate"})
public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderHolder>{

    private final List<CartItem> orderList;
    private final Context context;
    private ProgressDialog progressDialog;
    private static String TYPE = "type";

    public class OrderHolder extends RecyclerView.ViewHolder
    {
        final TextView orderedItemTV;
        final TextView orderedItemPriceTV;
        final TextView orderedItemQuantityTV;
        final TextView timeTV;

        OrderHolder(final View itemView)
        {
            super(itemView);
            orderedItemPriceTV = (TextView)itemView.findViewById(R.id.orderedPrice);
            orderedItemTV = (TextView)itemView.findViewById(R.id.orderedItem);
            orderedItemQuantityTV = (TextView)itemView.findViewById(R.id.orderedItemQuantity);
            timeTV = (TextView)itemView.findViewById(R.id.timeStamp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSnackBar("Press and hold to cancel or restore an item");
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showProgressDialog();
                    if(!orderList.get(getAdapterPosition()).getIsCancelled()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String message = context.getSharedPreferences(AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).
                                        getString(CommonIdentifiers.getName(),null)
                                        +" canceled "+orderList.get(getAdapterPosition()).getCartItem();
                                if(setCancellationStatus("cancel", orderList.get(getAdapterPosition()).getOrderID())) {
                                    orderList.get(getAdapterPosition()).setIsCancelled(true);
                                    if(context.getSharedPreferences(AppSharedPreferences.getIdFile(),
                                            Context.MODE_PRIVATE).getString(TYPE,null).equalsIgnoreCase("onSite")) {
                                        sendUpdateViaGCM(message);
                                        OrderedFragment.orderedFragment.fragmentActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateTotals("Cancel", getAdapterPosition());
                                                itemView.setBackgroundColor(context.getResources().getColor(R.color.cancelled));
                                                showSnackBar("Item Cancelled");
                                            }
                                        });
                                    }

                                }else
                                    showSnackBar("Something went wrong. Please try again");


                                hideProgressDialog();
                            }
                        }).start();

                    }
                    else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String message = context.getSharedPreferences(AppSharedPreferences.getIdFile(),Context.MODE_PRIVATE).
                                        getString(CommonIdentifiers.getName(),null)+" restored "+orderList.get(getAdapterPosition()).getCartItem();
                                if(setCancellationStatus("restore",orderList.get(getAdapterPosition()).getOrderID())) {
                                    orderList.get(getAdapterPosition()).setIsCancelled(false);
                                    if(context.getSharedPreferences(AppSharedPreferences.getIdFile(),
                                            Context.MODE_PRIVATE).getString(TYPE,null).equalsIgnoreCase("onSite")) {
                                        sendUpdateViaGCM(message);
                                        OrderedFragment.orderedFragment.fragmentActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateTotals("Restore", getAdapterPosition());
                                                itemView.setBackgroundColor(context.getResources().
                                                        getColor(R.color.background_holo_light));
                                                showSnackBar("Item Restored");
                                            }
                                        });
                                    }

                                }else
                                    showSnackBar("Something went wrong. Please try again");

                                hideProgressDialog();
                            }
                        }).start();

                    }
                    return true;
                }
            });
            //Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            //orderContainer.startAnimation(animation);

        }
    }

    void showSnackBar(final String message){
        Snackbar.make(OrderedFragment.orderedFragment.fragmentActivity.findViewById(R.id.snackbarPositionCart),
                message, Snackbar.LENGTH_SHORT).show();

    }

    void updateTotals(final String type,final int position){
        OrderedFragment.orderedFragment.fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type){
                    case "Cancel":
                        OrderedFragment.orderedFragment.actualTotal =
                                OrderedFragment.orderedFragment.actualTotal -
                                        (float)orderList.get(position).getPrice();
                        OrderedFragment.orderedFragment.potentialTotal =
                                OrderedFragment.orderedFragment.potentialTotal -
                                        (float)orderList.get(position).getPrice();
                        OrderedFragment.orderedFragment.totalTV.
                                setText("R" + String.format("%1$,.2f", OrderedFragment.orderedFragment.potentialTotal));
                        OrderedFragment.orderedFragment.actualTotalTV.
                                setText("R" + String.format("%1$,.2f", OrderedFragment.orderedFragment.actualTotal));
                        break;
                    case "Restore":
                        OrderedFragment.orderedFragment.actualTotal =
                                OrderedFragment.orderedFragment.actualTotal +
                                        (float)orderList.get(position).getPrice();
                        OrderedFragment.orderedFragment.potentialTotal =
                                OrderedFragment.orderedFragment.potentialTotal +
                                        (float)orderList.get(position).getPrice();
                        OrderedFragment.orderedFragment.totalTV.
                                setText("R" + String.format("%1$,.2f", OrderedFragment.orderedFragment.potentialTotal));
                        OrderedFragment.orderedFragment.actualTotalTV.
                                setText("R"+String.format("%1$,.2f",OrderedFragment.orderedFragment.actualTotal));
                        break;
                }
            }
        });
    }

    void showProgressDialog(){
        OrderedFragment.orderedFragment.fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Please Wait...");
                progressDialog.show();
            }
        });

    }

    void hideProgressDialog(){
        OrderedFragment.orderedFragment.fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

    public OrderListAdapter(List<CartItem> orderList, Context context)
    {
        this.orderList = orderList;
        this.context = context;
    }

    @Override
    public OrderHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item,viewGroup,false);
        return new OrderHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderHolder orderHolder, int i) {
        orderHolder.orderedItemQuantityTV.setText(String.valueOf(orderList.get(i).getQuantity()));
        orderHolder.orderedItemTV.setText(orderList.get(i).getCartItem());
        orderHolder.orderedItemPriceTV.setText(String.valueOf("R"+String.format("%1$,.2f",orderList.get(i).getPrice())));
        orderHolder.timeTV.setText(orderList.get(i).getTime());

        if(orderList.get(i).getIsCancelled())
            orderHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cancelled));
        else
            orderHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.background_holo_light));
       // orderHolder.orderedItemPriceTV.setText(String.valueOf(orderList.get(i).price));

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }


    private Boolean setCancellationStatus(final String type, final String orderID){
        URL url;
        try {
            url = new URL(Server.getModifyOrder());
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            String postData = URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode(type,"UTF-8");
            postData += "&"+URLEncoder.encode("orderID","UTF-8")+"="+URLEncoder.encode(orderID,"UTF-8");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
            outputStreamWriter.write(postData);
            outputStreamWriter.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            //Log.i("", "New Response : " + line);
            return line != null && line.equalsIgnoreCase("Updated");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendUpdateViaGCM(final String message){

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                String TAG = "Sending Message VIA GCM";
                //Log.i(TAG,"++++++ "+CheckOutFragment.tableLogID);
                try {
                    //Log.i(TAG,"Attempting GCM POST");
                    url = new URL(Server.getUpdateOrder());
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(20000);
                    connection.setReadTimeout(20000);
                    connection.setDoOutput(true);
                    String postData = URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode("Modification","UTF-8");
                    postData += "&"+URLEncoder.encode("registrationIDs","UTF-8")+"="+URLEncoder.encode(CheckOutFragment.waiterRegID,"UTF-8");
                    postData += "&"+URLEncoder.encode("tableID","UTF-8")+"="+URLEncoder.encode(CheckOutFragment.tableID,"UTF-8");
                    postData += "&"+URLEncoder.encode("tableMessage","UTF-8")+"="+URLEncoder.encode(message,"UTF-8");
                    postData += "&"+URLEncoder.encode("tableLogID","UTF-8")+"="+URLEncoder.encode(CheckOutFragment.tableLogID,"UTF-8");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(postData);
                    outputStreamWriter.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    //Log.i(TAG, "New Response : " + line);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    hideProgressDialog();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    hideProgressDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                    hideProgressDialog();
                }

            }
        }).start();

    }
}
