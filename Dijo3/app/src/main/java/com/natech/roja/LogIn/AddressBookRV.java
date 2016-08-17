package com.natech.roja.LogIn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.NetworkServices.NetworkUtil;
import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.util.List;

/**
 * Created by Tshepo on 2016/04/17.
 */
public class AddressBookRV extends RecyclerView.Adapter<AddressBookRV.AddressBookHolder> {

    private List<Address> addressList;
    private Context context;

    public AddressBookRV(Context context,List<Address> addressList){
        this.addressList = addressList;
        this.context = context;
    }
    @Override
    public AddressBookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item,parent,false);
        return new AddressBookHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressBookHolder holder, int position) {
        holder.addressLabelTV.setText(addressList.get(position).getLabel());
        holder.addressCaptionTV.setText(addressList.get(position).getStreetAddress());
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public class AddressBookHolder extends RecyclerView.ViewHolder{

        TextView addressLabelTV, addressCaptionTV;

        public AddressBookHolder(View itemView) {
            super(itemView);
            addressLabelTV = (TextView)itemView.findViewById(R.id.addressLabelTV);
            addressCaptionTV = (TextView)itemView.findViewById(R.id.addressCaption);
            ImageButton deleteAddress = (ImageButton)itemView.findViewById(R.id.deleteAddressBtn);

            deleteAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(NetworkUtil.getConnectivityStatus(context)) {
                        AddressActivity.addressActivity.deleteAddress(addressList.get(getAdapterPosition()).getAddressID());
                        addressList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                    }else{
                        Snackbar.make(AddressActivity.addressActivity.findViewById(R.id.snackbarPosition),
                                "No Internet Connection", Snackbar.LENGTH_LONG).show();
                    }

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(CommonIdentifiers.getStreetAddress(),addressList.get(getAdapterPosition()).getStreetAddress());
                    bundle.putString(CommonIdentifiers.getComplex(),addressList.get(getAdapterPosition()).getComplexName());
                    bundle.putString(CommonIdentifiers.getProvince(),addressList.get(getAdapterPosition()).getProvince());
                    bundle.putString(CommonIdentifiers.getAddressId(),addressList.get(getAdapterPosition()).getAddressID());
                    bundle.putString(CommonIdentifiers.getLabel(), addressList.get(getAdapterPosition()).getLabel());
                    AddressActivity.addressActivity.openEditAddress(bundle);
                }
            });
        }
    }
}
