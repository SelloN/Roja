package com.natech.roja.MenuCategories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sello on 2015/06/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RestaurantHolder> {

    private final List<Category> categoryList;
    private static final String LOG_TAG = "MyRecyclerViewAdapter";
    private final Context context;
    private RVAdapter rvAdapter;

    public class RestaurantHolder extends RecyclerView.ViewHolder
    {
        final TextView category;
        final TextView description;
        final ImageView foodCatPhoto;
        int selectedPosition;
        final RelativeLayout categoryLayout;
        final RelativeLayout specialsLayout;

        RestaurantHolder(View itemView)
        {
            super(itemView);
            category = (TextView)itemView.findViewById(R.id.category);
            description = (TextView)itemView.findViewById(R.id.description);
            foodCatPhoto = (ImageView)itemView.findViewById(R.id.foodCatPhoto);
            categoryLayout = (RelativeLayout)itemView.findViewById(R.id.categoryLayout);
            specialsLayout = (RelativeLayout)itemView.findViewById(R.id.specialLayout);
            //Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            //cv.startAnimation(animation);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    //Log.i(LOG_TAG,"cat id = "+rvAdapter.getCatID(selectedPosition)+" pos "+selectedPosition );
                    if(categoryList.get(getAdapterPosition()).hasExtras()){
                        Bundle b = new Bundle();
                        b.putInt(CommonIdentifiers.getMenuCatId(), rvAdapter.getCatID(getAdapterPosition()));
                        b.putString("category", category.getText().toString());
                        b.putString("class", context.getClass().getSimpleName());
                        Intent intent = new Intent(context, NestedCategory.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtras(b);
                        context.startActivity(intent);

                    }else {
                        selectedPosition = getAdapterPosition();
                        Bundle b = new Bundle();
                        b.putInt("catID", rvAdapter.getCatID(selectedPosition));
                        b.putString("category", category.getText().toString());
                        b.putString("class", context.getClass().getSimpleName());
                        if(categoryList.get(getAdapterPosition()).isNested())
                            b.putString(CommonIdentifiers.getMenuType(),"nested");
                        else
                            b.putString(CommonIdentifiers.getMenuType(),"main");
                        Intent intent = new Intent(context, MenuItemsListActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtras(b);
                        context.startActivity(intent);
                        // b.putInt("catID",g);
                    /*Intent intent = new Intent(context,MenuList.class);
                    intent.putExtras(b);
                    context.startActivity(intent);*/
                    }
                }
            });
        }
    }


    public RVAdapter(Context context)
    {
        this.context = context;
        rvAdapter = this;
        //this.categoryList = categoryList;
        categoryList = new ArrayList<>();
    }

    int getCatID(int position)
    {
        return categoryList.get(position).getCatID();
    }

    public void addItem(Category category)
    {
        categoryList.add(category);
    }

    @Override
    public RestaurantHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);
        return new RestaurantHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantHolder restaurantHolder, int i) {

        if(categoryList.get(i).isSpecials()){
            restaurantHolder.specialsLayout.setVisibility(View.VISIBLE);

        }else {
            //Log.i("",categoryList.get(i).getPhotoDir());
            restaurantHolder.description.setText(categoryList.get(i).getDescription());
            restaurantHolder.category.setText(categoryList.get(i).getCategory());
            if(!categoryList.get(i).isNested())
                Picasso.with(context).load(categoryList.get(i).getPhotoDir()).into(restaurantHolder.foodCatPhoto);
            else
                restaurantHolder.foodCatPhoto.setVisibility(View.GONE);
            restaurantHolder.categoryLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

}
