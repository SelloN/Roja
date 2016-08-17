package com.natech.roja.Information;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.HomeContent.WebViewActivity;
import com.natech.roja.NetworkServices.Server;
import com.natech.roja.R;


/**
 * Created by Tshepo on 2015/08/14.
 */
@SuppressWarnings("DefaultFileTemplate")
public class InfoRVAdapter extends RecyclerView.Adapter<InfoRVAdapter.InfoHolder> {

    private final String[] infoItems = {"About Roja","Rate us on Play Store", "Send us feedback",
            "Suggest a eatery","Privacy Policy","Notifications"};
    private final Context context;
    private final String userName;
    private final String userID;

    public InfoRVAdapter(Context context, String userName, String userID){
        this.context = context;
        this.userID = userID;
        this.userName = userName;
    }

    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item,parent,false);
        return new InfoHolder(view);
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {

        if(position == 0){
            holder.infoIcon.setImageResource(R.drawable.ic_about);
            holder.infoItem.setText(infoItems[position]);
        }
        else if(position == 1){
            holder.infoIcon.setImageResource(R.drawable.ic_star_outline_pink);
            holder.infoItem.setText(infoItems[position]);
        }
        else if(position == 2){
            holder.infoIcon.setImageResource(R.drawable.ic_feed_back);
            holder.infoItem.setText(infoItems[position]);
        }
        else if(position == 3){
            holder.infoIcon.setImageResource(R.drawable.ic_suggest);
            holder.infoItem.setText(infoItems[position]);
        }

       else if(position == 5){
            holder.infoIcon.setImageResource(R.drawable.ic_notifications);
            holder.infoItem.setText(infoItems[position]);
        }
        else if(position == 4){
            holder.infoIcon.setImageResource(R.drawable.ic_privacy);
            holder.infoItem.setText(infoItems[position]);
        }
       /* else if(position == 6){
            holder.infoIcon.setImageResource(R.drawable.ic_document);
            holder.infoItem.setText(infoItems[position]);
        }*/

    }

    @Override
    public int getItemCount() {
        return infoItems.length;
    }

    class InfoHolder extends RecyclerView.ViewHolder{

        final ImageView infoIcon;
        final TextView infoItem;
        final RelativeLayout infoContainer;

        InfoHolder(View itemView){
            super(itemView);
            infoIcon = (ImageView)itemView.findViewById(R.id.infoIcon);
            infoItem = (TextView)itemView.findViewById(R.id.infoItem);
            infoContainer = (RelativeLayout)itemView.findViewById(R.id.infoContainer);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);

            infoContainer.startAnimation(animation);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()==0){
                        Intent intent = new Intent(context, AboutActivity.class);
                        context.startActivity(intent);
                    }
                    else if(getAdapterPosition() == 2){
                        Intent intent = new Intent(context, FeedBackActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(InformationActivity.USER_NAME,userName);
                        bundle.putString(InformationActivity.USER_ID,userID);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                    else if(getAdapterPosition() == 3){
                        Intent intent = new Intent(context, SuggestionActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(InformationActivity.USER_NAME,userName);
                        bundle.putString(InformationActivity.USER_ID,userID);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                    else if(getAdapterPosition() == 5){
                        Intent intent = new Intent(context, NotificationsActivity.class);
                        context.startActivity(intent);

                    }
                    else if(getAdapterPosition() == 1){

                        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY  |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            context.startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                                            context.getPackageName())));
                        }
                    }
                    else if(getAdapterPosition() == 4){
                        Intent webView = new Intent(context, WebViewActivity.class);
                        Bundle b = new Bundle();
                        b.putString("type","privacy");
                        b.putString("link", Server.getPrivacy());
                        webView.putExtras(b);
                        context.startActivity(webView);
                    }

                }
            });
        }

    }
}
