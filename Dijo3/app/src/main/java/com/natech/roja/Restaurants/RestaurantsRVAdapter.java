package com.natech.roja.Restaurants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.natech.roja.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/08/10.
 */
@SuppressWarnings("DefaultFileTemplate")
public class RestaurantsRVAdapter extends RecyclerView.Adapter<RestaurantsRVAdapter.RestaurantHolder> {

    private final List<Restaurant> restaurants;
    private final Context context;
    private final String type;
    public RestaurantsRVAdapter(List<Restaurant> restaurants, Context context, String type){
        this.restaurants = restaurants;
        this.context = context;
        this.type = type;
    }
    @Override
    public RestaurantHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
        return new RestaurantHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantHolder holder, int position) {

        if(type.equalsIgnoreCase("around"))
            holder.distanceTV.setText(String.format("%1$,.1f",restaurants.get(position).getDistance())+" km");
        holder.averageTV.setText(String.format("%1$,.1f",Double.valueOf(restaurants.get(position).getRating())));
        Picasso.with(context).load(restaurants.get(position).getThumbDir()).into(holder.restaurantIV);
        holder.restaurantTV.setText(restaurants.get(position).getRestaurantName());


    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    class RestaurantHolder extends RecyclerView.ViewHolder{

        final ImageView restaurantIV;
        final TextView restaurantTV, averageTV;
        private TextView distanceTV;

        RestaurantHolder(View itemView){
            super(itemView);
            restaurantIV = (ImageView)itemView.findViewById(R.id.restaurantPic);
            restaurantTV = (TextView)itemView.findViewById(R.id.restaurantNameTV);
            if(type.equalsIgnoreCase("around"))
                distanceTV = (TextView)itemView.findViewById(R.id.distanceTV);
            averageTV = (TextView)itemView.findViewById(R.id.ratingsTV);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(type.equalsIgnoreCase("around"))
                        AroundMeActivity.aroundMeActivity.sendHit(String.valueOf(restaurants.get(getAdapterPosition()).getRestID())
                                ,restaurants.get(getAdapterPosition()).getRestaurantName());
                    else
                        FavouriteActivity.favouriteActivity.sendHit(String.valueOf(restaurants.get(getAdapterPosition()).getRestID())
                                ,restaurants.get(getAdapterPosition()).getRestaurantName());
                    Intent intent = new Intent(context, RestaurantAroundMeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("restName",restaurants.get(getAdapterPosition()).getRestaurantName());
                    bundle.putInt("restID", restaurants.get(getAdapterPosition()).getRestID());
                    bundle.putString("rating",restaurants.get(getAdapterPosition()).getRating());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });


        }
    }
}
