package com.natech.roja.HomeContent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.natech.roja.MainActivity;
import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tshepo on 2015/07/09.
 */
@SuppressWarnings("DefaultFileTemplate")
public class PromotionsRVAdapter extends RecyclerView.Adapter<PromotionsRVAdapter.PromotionHolder> {

    private final List<Promotions> promotionsList;
    private final Context context;
    public  PromotionsRVAdapter(Context context, List<Promotions>promotionsList)
    {
        this.context = context;
        this.promotionsList = promotionsList;
    }
    public class PromotionHolder extends RecyclerView.ViewHolder
    {
        final ImageView imageView;
        final TextView caption;
        public PromotionHolder(View itemView)
        {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.promoImage);
            caption = (TextView)itemView.findViewById(R.id.promoCaption);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.mainActivity.sendHit(promotionsList.get(getAdapterPosition()).getRestID(),
                            promotionsList.get(getAdapterPosition()).getRestName());
                    Intent webView = new Intent(context, WebViewActivity.class);
                    Bundle b = new Bundle();
                    b.putString("type","promotion");
                    b.putString("promoLink",promotionsList.get(getAdapterPosition()).getPromoLink());
                    b.putString(CommonIdentifiers.getRestId(),promotionsList.get(getAdapterPosition()).getRestID());
                    b.putString(CommonIdentifiers.getRestName(),promotionsList.get(getAdapterPosition()).getRestName());
                    webView.putExtras(b);
                    context.startActivity(webView);
                }
            });*/

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.mainActivity.sendHit(promotionsList.get(getAdapterPosition()).getRestID(),
                            promotionsList.get(getAdapterPosition()).getRestName());

                    if(promotionsList.get(getAdapterPosition()).getPromoType() == 1) {
                        Intent splashIntent = new Intent(context, PromoSplashActivity.class);
                        Bundle b = new Bundle();
                        b.putString(CommonIdentifiers.getRestId(), promotionsList.get(getAdapterPosition()).getRestID());
                        b.putString(CommonIdentifiers.getRestName(), promotionsList.get(getAdapterPosition()).getRestName());
                        b.putString("largePhoto",promotionsList.get(getAdapterPosition()).getLargePhotoDir());
                        b.putString("desc",promotionsList.get(getAdapterPosition()).getDescription());
                        splashIntent.putExtras(b);
                        context.startActivity(splashIntent);
                    }else if(promotionsList.get(getAdapterPosition()).getPromoType() == 2){

                        Intent webView = new Intent(context, WebViewActivity.class);
                        Bundle b = new Bundle();
                        b.putString("type","promotion");
                        b.putString("promoLink",promotionsList.get(getAdapterPosition()).getPromoLink());
                        b.putString(CommonIdentifiers.getRestId(),promotionsList.get(getAdapterPosition()).getRestID());
                        b.putString(CommonIdentifiers.getRestName(),promotionsList.get(getAdapterPosition()).getRestName());
                        webView.putExtras(b);
                        context.startActivity(webView);
                    }
                }
            });


        }
    }

    @Override
    public PromotionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promo_item,parent,false);
        return new PromotionHolder(view);
    }

    @Override
    public void onBindViewHolder(PromotionHolder holder, int position)
    {
        holder.caption.setText(promotionsList.get(position).getCaption());
        //holder.imageView.setAnimation(animation);
        Picasso.with(context).load(promotionsList.get(position).getPhotoDir()).into(holder.imageView);
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return promotionsList.size();
    }




}
