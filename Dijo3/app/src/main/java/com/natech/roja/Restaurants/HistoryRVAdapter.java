package com.natech.roja.Restaurants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/07/30.
 */
@SuppressWarnings("DefaultFileTemplate")
public class HistoryRVAdapter extends RecyclerView.Adapter<HistoryRVAdapter.HistoryHolder> {
    private final List<RestaurantHistory> historyList;
    private final Context context;

    public HistoryRVAdapter(List<RestaurantHistory> historyList, Context context){
        this.historyList = historyList;
        this.context = context;
    }

    @Override
    public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false);
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryHolder holder, int position) {

        if(historyList.get(position).getIsContent()) {
            holder.contentPanel.setVisibility(View.VISIBLE);
            holder.progressLayout.setVisibility(View.GONE);
            holder.visitDateTV.setText(historyList.get(position).getFormattedVisitDate());
            holder.restNameTV.setText(historyList.get(position).getRestaurantName());
            Picasso.with(context).load(historyList.get(position).getThumbDir()).into(holder.restPic);
        }else if(historyList.get(position).getIsProgress()){
            holder.progressLayout.setVisibility(View.VISIBLE);
            holder.contentPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HistoryHolder extends RecyclerView.ViewHolder{

        final ImageView restPic;
        final TextView restNameTV;
        final TextView visitDateTV;
        final RelativeLayout historyContainer;
        final RelativeLayout contentPanel;
        final RelativeLayout progressLayout;

        HistoryHolder(View itemView){
            super(itemView);
            restPic = (ImageView)itemView.findViewById(R.id.restaurantPic);
            restNameTV = (TextView)itemView.findViewById(R.id.restaurantNameTV);
            visitDateTV = (TextView)itemView.findViewById(R.id.visitDate);
            historyContainer = (RelativeLayout)itemView.findViewById(R.id.historyContainer);
            contentPanel = (RelativeLayout)itemView.findViewById(R.id.contentPanel);
            progressLayout = (RelativeLayout)itemView.findViewById(R.id.progressLayout);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(historyList.get(getAdapterPosition()).getIsContent()) {
                        Intent intent = new Intent(context, RateRestaurantActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(CommonIdentifiers.getRestId(), historyList.get(getAdapterPosition()).getRestID());
                        bundle.putString(CommonIdentifiers.getLogId(), historyList.get(getAdapterPosition()).getLogID());
                        bundle.putString(CommonIdentifiers.getRestName(), historyList.get(getAdapterPosition()).getRestaurantName());
                        bundle.putBoolean("restRated", historyList.get(getAdapterPosition()).getIsRated());
                        bundle.putInt("restPosition", getAdapterPosition());
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
