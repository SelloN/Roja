package com.natech.roja.MenuCategories;


import com.natech.roja.Restaurants.Restaurant;

/**
 * Created by Tshepo on 2015/06/20.
 */
@SuppressWarnings({"SameParameterValue", "DefaultFileTemplate"})
public class MenuItem {

    private Restaurant restaurant;
    private String menuItem;
    private double itemPrice;
    private String itemDesc;
    private int menuID, orderID;
    @SuppressWarnings("CanBeFinal")
    private Boolean hasExtras, isRestaurant, isRated, isHalal, isVeg;

    public MenuItem(String menuItem,String itemDesc, double itemPrice,int menuID,int extras,int isHalal, int isVeg)
    {
        this.menuItem = menuItem;
        this.itemPrice = itemPrice;
        this.itemDesc = itemDesc;
        this.menuID = menuID;
        hasExtras = extras != 0;
        this.isHalal = isHalal == 1;
        this.isVeg = isVeg == 1;
        isRestaurant = false;
    }

    public MenuItem(String menuItem, double itemPrice, int menuID, int rateCode,int orderID){
        this.menuID = menuID;
        this.itemPrice = itemPrice;
        this.menuItem = menuItem;
        isRestaurant = false;
        this.orderID = orderID;
        isRated = rateCode != 0;
    }


    public MenuItem(Restaurant restaurant){
        this.restaurant = restaurant;
        isRestaurant = true;
    }

    public void setIsRated(Boolean isRated){
        this.isRated = isRated;
    }

    public Boolean getIsRated(){
        return isRated;
    }

    public Boolean getIsRestaurant(){
        return isRestaurant;
    }

    public Restaurant getRestaurant(){
        return restaurant;
    }

    public int getOrderID(){
        return orderID;
    }

    //getter methods
    public String getMenuItem()
    {
        return menuItem;
    }

    public double getItemPrice()
    {
        return itemPrice;
    }

    public int getMenuID(){
        return menuID;
    }

    public Boolean getHasExtras(){
        return hasExtras;
    }

    public String getItemDesc()
    {
        return itemDesc;
    }

    public Boolean getIsHalal(){
        return isHalal;
    }

    public Boolean getIsVeg(){
        return isVeg;
    }

}
