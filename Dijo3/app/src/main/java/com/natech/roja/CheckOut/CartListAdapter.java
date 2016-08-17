package com.natech.roja.CheckOut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;

import java.util.List;

/**
 * Created by Tshepo on 2015/06/16.
 * Recyclerview adapter that holds the items of the cart
 */
public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.RestaurantHolder>{

    private final List<CartItem> cartList;
    private static final String LOG_TAG = CartListAdapter.class.getSimpleName();
    private final Context context;
    private final SharedPreferences.Editor editor;
    private final TextView cartTotalTV;
    private final TextView potentialTotalTV;
    private final RelativeLayout emptyCartTV;

    public class RestaurantHolder extends RecyclerView.ViewHolder
    {
        final TextView cartItem, cartPrice, cartQuantity;
        final ImageView deleteIcon;
        int selectedPosition;
        float price, cartTotal, potentialTotal;
        Boolean isCartEmpty = true;
        final LinearLayout cartContainer;

        RestaurantHolder(View itemView)
        {
            super(itemView);
            cartItem = (TextView)itemView.findViewById(R.id.cartItem);
            cartPrice = (TextView)itemView.findViewById(R.id.cartItemPrice);
            cartQuantity = (TextView)itemView.findViewById(R.id.itemQuantity);
            deleteIcon = (ImageView)itemView.findViewById(R.id.delete);
            cartContainer = (LinearLayout)itemView.findViewById(R.id.cartContainer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = getAdapterPosition();
                    Intent intent = new Intent(context, EditItemActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putString("Selected Item",cartItem.getText().toString());
                    bundle.putString("Item Price",cartPrice.getText().toString());
                    bundle.putString("Quantity",cartQuantity.getText().toString());
                    bundle.putInt(CommonIdentifiers.getMenuId(),cartList.get(selectedPosition).getMenuID());
                    bundle.putInt("Item Position",selectedPosition);
                    bundle.putBoolean("hasExtras",cartList.get(selectedPosition).getHasExtras());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    Log.i(LOG_TAG,"Position = "+selectedPosition );
                }
            });
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = getAdapterPosition();

                    cartList.remove(selectedPosition);
                    notifyItemRemoved(selectedPosition);
                    price = Float.valueOf(cartPrice.getText().toString().substring(1));
                    Log.i(LOG_TAG, "Removal Price = " + price);
                    cartTotal = Float.valueOf(cartTotalTV.getText().toString().substring(1)) - price;
                    potentialTotal = Float.valueOf(potentialTotalTV.getText().toString().substring(1)) - price;
                    cartTotalTV.setText("R"+String.format("%1$,.2f",cartTotal));
                    potentialTotalTV.setText("R"+String.format("%1$,.2f",potentialTotal));
                    editor.clear();
                    getItemCount();
                    Log.i("Count",""+getItemCount());
                    editor.putInt("Size",getItemCount());
                    for(int x = 0; x <  getItemCount(); x++)
                    {
                        editor.putString("Selected Item"+x,cartList.get(x).getCartItem());
                        editor.putFloat("Item Price"+x,(float)(cartList.get(x).getPrice()));
                        editor.putInt("Quantity"+x,cartList.get(x).getQuantity());
                        isCartEmpty = false;

                    }
                    editor.commit();

                    if(isCartEmpty)
                        emptyCartTV.setVisibility(View.VISIBLE);
                    else
                        emptyCartTV.setVisibility(View.INVISIBLE);
                    isCartEmpty = true;
                }
            });
        }
    }

    public CartListAdapter(Context context, List<CartItem> cartList, TextView cartTotalTV,
                           TextView potentialTotalTV, RelativeLayout emptyCartTV)
    {
        this.context = context;
        this.cartList = cartList;
        this.cartTotalTV = cartTotalTV;
        this.potentialTotalTV = potentialTotalTV;
        this.emptyCartTV = emptyCartTV;
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("CartList", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public void remove()
    {
        //remove(position);
        //Log.i("=== ",""+cartList.get(position).price);

        cartList.remove(0);
        //notifyItemRemoved(0);
        //cartList.remove(position);
        //notifyItemRemoved(position);
    }

    public void clearList()
    {
        cartList.clear();
//        notifyDataSetChanged();
    }


    @Override
    public RestaurantHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_item,viewGroup,false);
        return new RestaurantHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantHolder restaurantHolder, int i) {

        restaurantHolder.cartQuantity.setText(String.valueOf(cartList.get(i).getQuantity()));
        restaurantHolder.cartPrice.setText(String.valueOf("R"+String.format("%1$,.2f",cartList.get(i).getPrice())));
        restaurantHolder.cartItem.setText(cartList.get(i).getCartItem());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

}
