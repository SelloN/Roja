package com.natech.roja.MenuCategories;

/**
 * Created by Sello on 2015/07/27.
 */
@SuppressWarnings("DefaultFileTemplate")
public class Extras {

    private final String extra;
    private final double extraPrice;
    private final String extraType;
    private final int extraID;
    public Extras(String extra, double extraPrice, String extraType, int extraID){
        this.extra = extra;
        this.extraID = extraID;
        this.extraPrice = extraPrice;
        this.extraType = extraType;
    }
    public String getExtra(){
        return extra;
    }
    public String getExtraType(){
        return extraType;
    }
    public int getExtraID(){
        return extraID;
    }
    public double getExtraPrice(){
        return extraPrice;
    }
}
