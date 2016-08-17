package com.natech.roja.MenuCategories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.natech.roja.R;

import java.util.List;

/**
 * Created by Tshepo on 2015/06/20.
 */
@SuppressWarnings("DefaultFileTemplate")
public class MenuListRVAdapter extends RecyclerView.Adapter<MenuListRVAdapter.MenuItemHolder>
{
    private final List<MenuItem> menuItemList;
    private final Context context;

    public MenuListRVAdapter(Context context, List<MenuItem> menuItemList)
    {
        this.context = context;
        this.menuItemList = menuItemList;
    }



    public class MenuItemHolder extends RecyclerView.ViewHolder
    {
        final TextView menuItemTV;
        final TextView menuItemPriceTV;
        final Button reviewsBtn;
        final RelativeLayout dummyArea;
        final ImageView halalImg;
        final ImageView vegImg;
        MenuItemHolder(View itemView)
        {
            super(itemView);
            menuItemTV = (TextView)itemView.findViewById(R.id.menuItem);
            menuItemPriceTV = (TextView)itemView.findViewById(R.id.itemPrice);
            reviewsBtn = (Button)itemView.findViewById(R.id.reviewsBtn);
            dummyArea = (RelativeLayout)itemView.findViewById(R.id.dummyArea);
            halalImg = (ImageView)itemView.findViewById(R.id.halalImg);
            vegImg = (ImageView)itemView.findViewById(R.id.vegImg);

            //Event handler for clicking on the whole card
           itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ItemDescriptionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putString("Selected Item",menuItemTV.getText().toString());
                    //bundle.putString("Item Price",menuItemPriceTV.getText().toString());
                    bundle.putString("Item Price","R"+String.format("%1$,.2f",menuItemList.get(getAdapterPosition()).getItemPrice()));
                    bundle.putInt("menuID",menuItemList.get(getAdapterPosition()).getMenuID());
                    bundle.putBoolean("hasExtras",menuItemList.get(getAdapterPosition()).getHasExtras());
                    bundle.putString("description",menuItemList.get(getAdapterPosition()).getItemDesc());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            reviewsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MenuItemReviewsActivity.class);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle b = new Bundle();
                    b.putInt("menuID",menuItemList.get(getAdapterPosition()).getMenuID());
                    b.putString("menuItem",menuItemTV.getText().toString());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            dummyArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MenuItemReviewsActivity.class);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle b = new Bundle();
                    b.putInt("menuID",menuItemList.get(getAdapterPosition()).getMenuID());
                    b.putString("menuItem",menuItemTV.getText().toString());
                    intent.putExtras(b);
                    context.startActivity(intent);

                }
            });



        }
    }

    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.menu_item,viewGroup,false);
        return new MenuItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final MenuItemHolder menuItemHolder,int i) {
        menuItemHolder.menuItemTV.setText(menuItemList.get(i).getMenuItem());
        if(menuItemList.get(i).getItemPrice() > 0)
            menuItemHolder.menuItemPriceTV.setText("R"+String.format("%1$,.2f",menuItemList.get(i).getItemPrice()));
        else
            menuItemHolder.menuItemPriceTV.setText("N/A");
        if(menuItemList.get(i).getIsHalal())
            menuItemHolder.halalImg.setVisibility(View.VISIBLE);
        else
            menuItemHolder.halalImg.setVisibility(View.GONE);

        if(menuItemList.get(i).getIsVeg())
            menuItemHolder.vegImg.setVisibility(View.VISIBLE);
        else
            menuItemHolder.vegImg.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

}
