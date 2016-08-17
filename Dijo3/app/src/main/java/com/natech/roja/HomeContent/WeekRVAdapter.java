package com.natech.roja.HomeContent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/08/22.
 */
@SuppressWarnings("DefaultFileTemplate")
public class WeekRVAdapter extends RecyclerView.Adapter<WeekRVAdapter.WeekHolder>{

    private final List<RestaurantWeek> restaurant;
    private final Context context;

    public WeekRVAdapter(List<RestaurantWeek> restaurant, Context context){
        this.restaurant =  restaurant;
        this.context = context;
    }
    @Override
    public WeekHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_week_item,parent,false);
        return new WeekHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekHolder holder, int position) {

        if(position == 0){
            holder.locationLayout.setVisibility(View.VISIBLE);
            holder.directionsTV.setText(restaurant.get(position).getAddress());
            String mapLink = "http://maps.google.com/maps/api/staticmap?center=-"+restaurant.get(position).getLat()+","+
                    restaurant.get(position).getLon()+"&zoom=15" +
                    "&size=500x150&sensor=false&markers=color:red%7Clabel:R%7C"+
                    restaurant.get(position).getLat()+","+restaurant.get(position).getLon();
            Picasso.with(context).load(mapLink).into(holder.mapsImage);
        }
        else if(position == 1){
            holder.storyLayout.setVisibility(View.VISIBLE);
            holder.storyTV.setText(restaurant.get(position).getStory());
            holder.cardView.setClickable(false);
            holder.cardView.setForeground(null);
        }

    }

    @Override
    public int getItemCount() {
        return restaurant.size();
    }

    class WeekHolder extends RecyclerView.ViewHolder{

        final RelativeLayout locationLayout, storyLayout;
        final TextView storyTV, directionsTV;
        final ImageView mapsImage;
        final CardView cardView;

        public WeekHolder(View itemView){
            super(itemView);
            locationLayout = (RelativeLayout)itemView.findViewById(R.id.directionsLayout);
            storyLayout = (RelativeLayout)itemView.findViewById(R.id.storyLayout);

            storyTV = (TextView)itemView.findViewById(R.id.storyTV);
            directionsTV = (TextView)itemView.findViewById(R.id.directionsTV);
            mapsImage = (ImageView)itemView.findViewById(R.id.mapImage);

            cardView = (CardView)itemView.findViewById(R.id.restaurantItemCV);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(restaurant.get(getAdapterPosition()).getIsAddress()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=" + restaurant.get(getAdapterPosition()).getLat() + "," +
                                        restaurant.get(getAdapterPosition()).getLon()));
                        PackageManager packageManager = context.getPackageManager();
                        List activities = packageManager.queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);
                        boolean isIntentSafe = activities.size() > 0;
                        //intent.
                        if (isIntentSafe)
                            context.startActivity(intent);
                    }
                }
            });
        }
    }
}
