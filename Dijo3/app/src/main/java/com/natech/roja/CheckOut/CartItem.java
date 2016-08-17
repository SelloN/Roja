package com.natech.roja.CheckOut;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tshepo on 2015/06/18.
 * CartItem stores the values that are associated with the items the user selects from the menu.
 */
@SuppressWarnings("SameParameterValue")
class CartItem
{

    private final String cartItem;
    private String time;
    private String orderID;
    private String note;
    private final double price;
    private final int quantity;
    private final int menuID;
    private final Boolean hasExtras;
    private Boolean extrasAdded;
    private Boolean hasNote;
    private Boolean isCancelled;
    private final List<String> extrasList;

    /*****CartItem constructors****/
    public CartItem(String cartItem, double price, int quantity,int menuID,Boolean hasExtras)
    {
        this.cartItem = cartItem;
        this.price = price;
        this.quantity = quantity;
        this.menuID = menuID;
        this.hasExtras = hasExtras;
        extrasList = new ArrayList<>();
        extrasAdded = false;
        hasNote = false;
    }

    public CartItem(String orderID, String cartItem, double price, int quantity,int menuID,Boolean hasExtras, String time,int status)
    {
        this.orderID = orderID;
        this.cartItem = cartItem;
        this.price = price;
        this.quantity = quantity;
        this.menuID = menuID;
        this.hasExtras = hasExtras;
        extrasList = new ArrayList<>();
        extrasAdded = false;
        hasNote = false;
        isCancelled = false;
        this.time = time;
        if(status == 1)
            isCancelled = false;
        else if(status == 0)
            isCancelled = true;
    }
    /*****CartItem setter methods****/
    public void addExtra(String extra){
        extrasList.add(extra);
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setHasNote(Boolean hasNote){
        this.hasNote = hasNote;
    }
    public void setIsCancelled(Boolean isCancelled){
        this.isCancelled = isCancelled;
    }
    public void setExtrasAdded(Boolean extrasAdded){
        this.extrasAdded = extrasAdded;
    }

    /*****CartItem getter methods****/
    public String getCartItem(){
       return cartItem;
   }
    public double getPrice(){
        return price;
    }
    public int getQuantity(){
        return quantity;
    }
    public int getMenuID(){
        return menuID;
    }
    public Boolean getHasExtras(){
        return hasExtras;
    }
    public Boolean getExtrasAdded(){
        return extrasAdded;
    }
    public Boolean getHasNote(){
        return hasNote;
    }
    public Boolean getIsCancelled(){
        return isCancelled;
    }
    public String getNote(){
        return note;
    }
    public String getOrderID(){
        return orderID;
    }
    public String getTime(){
        return time;
    }
    public List<String> getExtrasList(){
        return extrasList;
    }


}
