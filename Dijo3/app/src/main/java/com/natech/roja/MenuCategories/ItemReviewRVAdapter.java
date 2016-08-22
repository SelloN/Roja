package com.natech.roja.MenuCategories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.natech.roja.R;
import com.natech.roja.Restaurants.Review;

import java.util.List;

/**
 * Created by Sello on 2015/07/29.
 */
@SuppressWarnings("DefaultFileTemplate")
public class ItemReviewRVAdapter extends RecyclerView.Adapter<ItemReviewRVAdapter.ReviewHolder> {

    private final List<Review> reviewList;
    public  ItemReviewRVAdapter(List<Review> reviewList){
        this.reviewList = reviewList;
    }
    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item,parent,false);
        return new ReviewHolder(view);
    }


    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {

        holder.ratingTV.setText(String.format("%1$,.1f",Float.valueOf(reviewList.get(position).getRating())));
        holder.userTV.setText(reviewList.get(position).getUserName());
        holder.reviewTV.setText(reviewList.get(position).getReview());
        holder.dateTV.setText(reviewList.get(position).getDate().substring(0,10));

    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewHolder extends RecyclerView.ViewHolder{

        final TextView userTV;
        final TextView dateTV;
        final TextView reviewTV;
        final TextView ratingTV;

        public ReviewHolder(View itemView){
            super(itemView);
            userTV = (TextView)itemView.findViewById(R.id.userDetails);
            dateTV = (TextView)itemView.findViewById(R.id.reviewDateTV);
            reviewTV = (TextView)itemView.findViewById(R.id.reviewTV);
            ratingTV = (TextView)itemView.findViewById(R.id.ratingsTV);
            //Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            //ratingCV.startAnimation(animation);

        }
    }
}
