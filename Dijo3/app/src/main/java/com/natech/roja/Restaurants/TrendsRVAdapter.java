package com.natech.roja.Restaurants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/07/31.
 */
@SuppressWarnings("DefaultFileTemplate")
public class TrendsRVAdapter extends RecyclerView.Adapter<TrendsRVAdapter.TrendsHolder> {

    private final List<Restaurant> restaurantList;
    private final Context context;

    public TrendsRVAdapter(List<Restaurant> restaurantList, Context context){
        this.restaurantList =restaurantList;
        this.context = context;
    }
    @Override
    public TrendsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trend_item,parent,false);
        return new TrendsHolder(view);
    }

    @Override
    public void onBindViewHolder(TrendsHolder holder, int position) {

        holder.averageTV.setText(String.format("%1$.1f",Double.valueOf(restaurantList.get(position).getRating())));
        holder.leaderTV.setText(String.valueOf(position+1));
        holder.rateCountTV.setText(restaurantList.get(position).getRateCount()+" ratings");
        holder.restaurantTV.setText(restaurantList.get(position).getRestaurantName());
        Picasso.with(context).load(restaurantList.get(position).getThumbDir()).into(holder.restaurantIV);
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    class TrendsHolder extends RecyclerView.ViewHolder{
        final ImageView restaurantIV;
        final TextView restaurantTV, rateCountTV, averageTV, leaderTV;
        final CardView cardView;
        public TrendsHolder(View itemView){
            super(itemView);
            restaurantIV = (ImageView)itemView.findViewById(R.id.restaurantPic);
            restaurantTV = (TextView)itemView.findViewById(R.id.restaurantNameTV);
            rateCountTV = (TextView)itemView.findViewById(R.id.ratingsCountTV);
            leaderTV = (TextView)itemView.findViewById(R.id.leaderTV);
            averageTV = (TextView)itemView.findViewById(R.id.ratingsTV);
            cardView = (CardView)itemView.findViewById(R.id.trendsCV);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            cardView.startAnimation(animation);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TrendsActivity.trendsActivity.sendHit(String.valueOf(restaurantList.get(getAdapterPosition()).getRestID()),
                            restaurantList.get(getAdapterPosition()).getRestaurantName());
                    Intent intent = new Intent(context, TrendReviewsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(CommonIdentifiers.getRestName(),restaurantList.get(getAdapterPosition()).getRestaurantName());
                    bundle.putInt(CommonIdentifiers.getRestId(), restaurantList.get(getAdapterPosition()).getRestID());
                    bundle.putString(CommonIdentifiers.getRating(),restaurantList.get(getAdapterPosition()).getRating());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }
    }
}
