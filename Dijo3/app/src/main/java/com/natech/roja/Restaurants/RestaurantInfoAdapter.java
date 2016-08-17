package com.natech.roja.Restaurants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/08/10.
 */
@SuppressWarnings("DefaultFileTemplate")
public class RestaurantInfoAdapter extends RecyclerView.Adapter<RestaurantInfoAdapter.RestaurantInfoHolder> {

    private final List<Restaurant> restaurantList;
    private final Context context;

    public RestaurantInfoAdapter(Context context, List<Restaurant> restaurantList){
        this.restaurantList =  restaurantList;
        this.context = context;
    }
    @Override
    public RestaurantInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_info_item,parent,false);
        return new RestaurantInfoHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantInfoHolder holder, int position) {
        if(restaurantList.get(position).getIsAboutUs()){
            holder.descriptionLayout.setVisibility(View.VISIBLE);
            holder.descriptionTV.setText(restaurantList.get(position).getDescription());

        }
        else if(restaurantList.get(position).getIsDirection()){
            holder.directionsLayout.setVisibility(View.VISIBLE);
            holder.directionsTV.setText(restaurantList.get(position).getDirections());
            /*String mapLink = "http://maps.google.com/maps/api/staticmap?center=-"+restaurantList.get(position).getLat()+","+
                    restaurantList.get(position).getLon()+"&zoom=15" +
                    "&size=500x150&sensor=false&markers=color:red%7Clabel:R%7C"+
                    restaurantList.get(position).getLat()+","+restaurantList.get(position).getLon();*/
            String mapLink = Server.getStaticMap(restaurantList.get(position).getRestaurantLabel(),
                    restaurantList.get(position).getLat(),restaurantList.get(position).getLon());
            Picasso.with(context).load(mapLink).into(holder.mapsImage);

        }else if(restaurantList.get(position).getIsRating()){

            if(restaurantList.get(position).getShowReviews()) {
                holder.restInfoLayout.setVisibility(View.VISIBLE);
                String roundedAvg = String.format("%1$,.1f", Double.parseDouble(restaurantList.get(position).getRating()));
                holder.averageTV.setText(roundedAvg);
                holder.averageRating.setRating(Float.parseFloat(roundedAvg));
            }
            /*NumberFormat nf = new DecimalFormat("990.0");
            try {
                Number avg = nf.parse(roundedAvg);
                holder.averageRating.setRating(avg.floatValue());
            } catch (ParseException e) {
                e.printStackTrace();
            }*/

        }else if(restaurantList.get(position).getIsContact()){
            holder.contactsLayout.setVisibility(View.VISIBLE);
            holder.phoneTV.setText(restaurantList.get(position).getPhone());
            holder.mailTV.setText(restaurantList.get(position).getEmail());

        }
        else if(restaurantList.get(position).getIsFeatures()){
            holder.featuresLayout.setVisibility(View.VISIBLE);
            if(restaurantList.get(position).getIsHalal()){
                holder.halalTV.setVisibility(View.VISIBLE);
                holder.halalIV.setVisibility(View.VISIBLE);
            }

            if(restaurantList.get(position).getIsAlcohol()){
                holder.alcoholIV.setVisibility(View.VISIBLE);
                holder.alcoholTV.setVisibility(View.VISIBLE);
            }

            if(restaurantList.get(position).getIsDelivery()){
                holder.deliveryIV.setVisibility(View.VISIBLE);
                holder.deliveryTV.setVisibility(View.VISIBLE);
            }

            if(restaurantList.get(position).getIsWifi()){
                holder.wifiIV.setVisibility(View.VISIBLE);
                holder.wifiTV.setVisibility(View.VISIBLE);
            }
        }
        else if(position == 5){
            holder.tradingTimesLayout.setVisibility(View.VISIBLE);
            holder.monTV.setText(restaurantList.get(5).getTradingHours().get(0).getDay());
            holder.monTimeTV.setText(restaurantList.get(5).getTradingHours().get(0).getTime());
            holder.tuesTV.setText(restaurantList.get(5).getTradingHours().get(1).getDay());
            holder.tuesTimeTV.setText(restaurantList.get(5).getTradingHours().get(1).getTime());
            holder.wedTV.setText(restaurantList.get(5).getTradingHours().get(2).getDay());
            holder.wedTimeTV.setText(restaurantList.get(5).getTradingHours().get(2).getTime());
            holder.thursTV.setText(restaurantList.get(5).getTradingHours().get(3).getDay());
            holder.thursTimeTV.setText(restaurantList.get(5).getTradingHours().get(3).getTime());
            holder.friTV.setText(restaurantList.get(5).getTradingHours().get(4).getDay());
            holder.friTimeTV.setText(restaurantList.get(5).getTradingHours().get(4).getTime());
            holder.satTV.setText(restaurantList.get(5).getTradingHours().get(5).getDay());
            holder.satTimeTV.setText(restaurantList.get(5).getTradingHours().get(5).getTime());
            holder.sunTV.setText(restaurantList.get(5).getTradingHours().get(6).getDay());
            holder.sunTimeTV.setText(restaurantList.get(5).getTradingHours().get(6).getTime());


            if(restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(0).getDayIndex()) {
                holder.monTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.monTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
            }
            else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(1).getDayIndex())){
                holder.tuesTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.tuesTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(2).getDayIndex())){
                holder.wedTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.wedTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(3).getDayIndex())){
                holder.thursTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.thursTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(4).getDayIndex())){
                holder.friTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.friTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(5).getDayIndex())){
                holder.satTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.satTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }else if((restaurantList.get(5).getCurrentDay() == restaurantList.get(5).getTradingHours().get(6).getDayIndex())){
                holder.sunTimeTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));
                holder.sunTV.setTextColor(context.getResources().getColor(R.color.myAccentColor));

            }

        }
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    class RestaurantInfoHolder extends RecyclerView.ViewHolder{

        final RelativeLayout restInfoLayout;
        final RelativeLayout directionsLayout;
        final RelativeLayout descriptionLayout;
        final RelativeLayout tradingTimesLayout;
        final RelativeLayout contactsLayout;
        final RelativeLayout featuresLayout;

        final TextView averageTV;
        final TextView directionsTV;
        final TextView descriptionTV;
        final TextView mailTV;
        final TextView phoneTV;
        final RatingBar averageRating;
        final ImageButton mailIB;
        final ImageButton phoneIB;
        final ImageView mapsImage;
        final ImageView deliveryIV, halalIV, alcoholIV,wifiIV;

        final CardView infoCV;

        //Trading times TextViews
        final TextView monTV, tuesTV, wedTV, thursTV, friTV, satTV, sunTV;
        final TextView monTimeTV, tuesTimeTV, wedTimeTV, thursTimeTV, friTimeTV, satTimeTV, sunTimeTV;

        //features
        final TextView halalTV,alcoholTV,deliveryTV,wifiTV;

        public RestaurantInfoHolder(View itemView){
            super(itemView);
            infoCV = (CardView)itemView.findViewById(R.id.restInfoCV);
            restInfoLayout = (RelativeLayout)itemView.findViewById(R.id.restInfoLayout);
            directionsLayout = (RelativeLayout)itemView.findViewById(R.id.directionsLayout);
            descriptionLayout = (RelativeLayout)itemView.findViewById(R.id.descriptionLayout);
            tradingTimesLayout = (RelativeLayout)itemView.findViewById(R.id.tradingTimesLayout);
            contactsLayout = (RelativeLayout)itemView.findViewById(R.id.contactsLayout);
            featuresLayout = (RelativeLayout)itemView.findViewById(R.id.featuresLayout);
            averageRating = (RatingBar)itemView.findViewById(R.id.averageRating);
            averageTV = (TextView)itemView.findViewById(R.id.averageTV);
            directionsTV = (TextView)itemView.findViewById(R.id.directionsTV);
            descriptionTV = (TextView)itemView.findViewById(R.id.descriptionTV);
            mailIB = (ImageButton)itemView.findViewById(R.id.mailBtn);
            phoneIB = (ImageButton)itemView.findViewById(R.id.phoneBtn);
            mailTV = (TextView)itemView.findViewById(R.id.mailTV);
            phoneTV = (TextView)itemView.findViewById(R.id.phoneTV);
            mapsImage = (ImageView)itemView.findViewById(R.id.mapImage);
            deliveryIV = (ImageView)itemView.findViewById(R.id.deliveryImg);
            halalIV = (ImageView)itemView.findViewById(R.id.halalImg);
            alcoholIV = (ImageView)itemView.findViewById(R.id.alcoholImg);
            wifiIV = (ImageView)itemView.findViewById(R.id.wifiImg);

            //features
            alcoholTV = (TextView)itemView.findViewById(R.id.alcoholTV);
            halalTV = (TextView)itemView.findViewById(R.id.halalTV);
            deliveryTV = (TextView)itemView.findViewById(R.id.deliveryTV);
            wifiTV = (TextView)itemView.findViewById(R.id.wifiTV);


            //Trading
            monTV = (TextView)itemView.findViewById(R.id.monTV);
            monTimeTV = (TextView)itemView.findViewById(R.id.monTimeTV);
            tuesTV = (TextView)itemView.findViewById(R.id.tuesTV);
            tuesTimeTV = (TextView)itemView.findViewById(R.id.tuesTimeTV);
            wedTV = (TextView)itemView.findViewById(R.id.wedTV);
            wedTimeTV = (TextView)itemView.findViewById(R.id.wedTimeTV);
            thursTV = (TextView)itemView.findViewById(R.id.thursTV);
            thursTimeTV = (TextView)itemView.findViewById(R.id.thursTimeTV);
            friTV = (TextView)itemView.findViewById(R.id.friTV);
            friTimeTV = (TextView)itemView.findViewById(R.id.friTimeTV);
            satTV = (TextView)itemView.findViewById(R.id.satTV);
            satTimeTV = (TextView)itemView.findViewById(R.id.satTimeTV);
            sunTV = (TextView)itemView.findViewById(R.id.sunTV);
            sunTimeTV = (TextView)itemView.findViewById(R.id.sunTimeTV);
            //tradingHoursTV = (TextView)itemView.findViewById(R.id.tradingHoursTV);

            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            infoCV.startAnimation(animation);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restaurantList.get(getAdapterPosition()).getIsDirection()){

                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(Server.getMap(restaurantList.get(getAdapterPosition()).getLat(),
                                        restaurantList.get(getAdapterPosition()).getLon())));
                        PackageManager packageManager = context.getPackageManager();
                        List activities = packageManager.queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);
                        boolean isIntentSafe = activities.size() > 0;
                        //intent.
                        if(isIntentSafe)
                            context.startActivity(intent);
                    }

                    if(restaurantList.get(getAdapterPosition()).getIsRating()){
                        Intent intent = new Intent(context, TrendReviewsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("restName",restaurantList.get(getAdapterPosition()).getRestaurantName());
                        bundle.putInt("restID", restaurantList.get(getAdapterPosition()).getRestID());
                        bundle.putString("rating",restaurantList.get(getAdapterPosition()).getRating());
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }



                }
            });

            phoneTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restaurantList.get(getAdapterPosition()).getIsContact()){
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+restaurantList.get(getAdapterPosition()).getPhone()));
                        context.startActivity(intent);
                    }
                }
            });

            phoneIB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restaurantList.get(getAdapterPosition()).getIsContact()){
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+restaurantList.get(getAdapterPosition()).getPhone()));
                        context.startActivity(intent);
                    }
                }
            });

            mailTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restaurantList.get(getAdapterPosition()).getIsContact()){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"+restaurantList.get(getAdapterPosition()).getEmail()));
                        context.startActivity(intent);
                    }
                }
            });
            mailIB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restaurantList.get(getAdapterPosition()).getIsContact()){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"+restaurantList.get(getAdapterPosition()).getEmail()));
                        context.startActivity(intent);
                    }
                }
            });

        }
    }
}
