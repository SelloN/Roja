package com.natech.roja.Information;

import android.content.Context;
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
 * Created by Sello on 2015/10/30.
 */
public class PointsRVAdapter extends RecyclerView.Adapter<PointsRVAdapter.PointsHolder> {

    private List<Points> pointsList;
    private Context context;

    public PointsRVAdapter(Context context,List<Points> pointsList){
        this.pointsList = pointsList;
        this.context = context;
    }

    @Override
    public PointsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PointsHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.points_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PointsHolder holder, int position) {

        holder.restaurantNameTV.setText(pointsList.get(position).getRestaurantName());
        holder.pointsTV.setText(pointsList.get(position).getPoints()+" Points");
        Picasso.with(context).load(pointsList.get(position).getThumbPhotoDir()).into(holder.restThumb);

    }

    @Override
    public int getItemCount() {
        return pointsList.size();
    }


    class PointsHolder extends RecyclerView.ViewHolder{
        final TextView restaurantNameTV;
        final TextView pointsTV;
        final ImageView restThumb;
        public PointsHolder(View itemView){
            super(itemView);
            restaurantNameTV = (TextView)itemView.findViewById(R.id.restaurantNameTV);
            pointsTV = (TextView)itemView.findViewById(R.id.pointsTV);
            restThumb  = (ImageView)itemView.findViewById(R.id.restaurantPic);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PointsActivity.pointsActivity.startClaimsActivity(
                            pointsList.get(getAdapterPosition()).getRestaurantName(),
                            pointsList.get(getAdapterPosition()).getRestID(),pointsList.get(getAdapterPosition()).getPoints(),
                            pointsList.get(getAdapterPosition()).getPointsID(),getAdapterPosition(),
                            pointsList.get(getAdapterPosition()).getRestEmail());
                }
            });
        }
    }
}
