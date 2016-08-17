package com.natech.roja.Restaurants;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;


import com.natech.roja.MenuCategories.MenuItem;
import com.natech.roja.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

/**
 * Created by Tshepo on 2015/07/30.
 */
@SuppressWarnings("DefaultFileTemplate")
public class OrderHistoryRVAdapter extends RecyclerView.Adapter<OrderHistoryRVAdapter.OrderHolder> {

    private final List<MenuItem> orderList;
    private final Context context;

    public OrderHistoryRVAdapter(List<MenuItem> orderList, Context context){
        this.orderList = orderList;
        this.context = context;
    }
    @Override
    public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_history_item,parent,false);
        return new OrderHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderHolder holder, int position) {

        if(!orderList.get(position).getIsRestaurant()) {
            holder.itemTV.setVisibility(View.VISIBLE);
            holder.priceTV.setVisibility(View.VISIBLE);

            if(!orderList.get(position).getIsRated())
                holder.rateBtn.setVisibility(View.VISIBLE);
            else
                holder.rateBtn.setVisibility(View.INVISIBLE);
            holder.priceTV.setText("R" + String.format("%1$,.2f", orderList.get(position).getItemPrice()));
            holder.itemTV.setText(orderList.get(position).getMenuItem());
        }
        else {
            holder.locationTV.setVisibility(View.VISIBLE);
            holder.averageTV.setVisibility(View.VISIBLE);
            holder.restDescTV.setVisibility(View.VISIBLE);
            holder.ratingBar.setVisibility(View.VISIBLE);

            String roundedAvg = String.format("%1$,.1f",Double.parseDouble(orderList.get(position).getRestaurant().getRating()));
            holder.averageTV.setText(roundedAvg);
            NumberFormat nf = new DecimalFormat("990.0");
            try {
                Number avg = nf.parse(roundedAvg);
                holder.ratingBar.setRating(avg.floatValue());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //holder.ratingBar.setRating(Float.parseFloat(roundedAvg));
            holder.restDescTV.setText(orderList.get(position).getRestaurant().getDescription());
            holder.locationTV.setText(orderList.get(position).getRestaurant().getLocation());
        }



    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderHolder extends RecyclerView.ViewHolder{

        final Button rateBtn;
        final TextView itemTV;
        final TextView priceTV;
        final CardView orderCV;

        final RatingBar ratingBar;
        final TextView restDescTV;
        final TextView averageTV;
        final TextView locationTV;

        public OrderHolder(View itemView){
            super(itemView);
            rateBtn = (Button)itemView.findViewById(R.id.rateBtn);
            itemTV = (TextView)itemView.findViewById(R.id.historyItemTV);
            priceTV = (TextView)itemView.findViewById(R.id.historyPriceTV);
            orderCV = (CardView)itemView.findViewById(R.id.orderedItemHistoryCV);
            ratingBar = (RatingBar)itemView.findViewById(R.id.averageRating);
            ratingBar.setIsIndicator(true);
            restDescTV = (TextView)itemView.findViewById(R.id.restDescTV);
            averageTV = (TextView)itemView.findViewById(R.id.averageTV);
            locationTV = (TextView)itemView.findViewById(R.id.restLocation);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            orderCV.startAnimation(animation);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition() != 0){

                        if(!orderList.get(getAdapterPosition()).getIsRated())
                            RateRestaurantActivity.rateRestaurantActivity.openRateDialog(orderList.get(getAdapterPosition()).getMenuItem(),
                                false,orderList.get(getAdapterPosition()).getMenuID(),orderList.get(getAdapterPosition()).getOrderID(), getAdapterPosition());
                        else
                            RateRestaurantActivity.rateRestaurantActivity.showRatedSnackBar();
                    }
                }
            });

            rateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition() != 0){
                        if(!orderList.get(getAdapterPosition()).getIsRated())
                            RateRestaurantActivity.rateRestaurantActivity.openRateDialog(orderList.get(getAdapterPosition()).getMenuItem(),
                                false,orderList.get(getAdapterPosition()).getMenuID(),orderList.get(getAdapterPosition()).getOrderID(),getAdapterPosition());
                        else
                            RateRestaurantActivity.rateRestaurantActivity.showRatedSnackBar();
                    }
                }
            });
        }
    }
}
