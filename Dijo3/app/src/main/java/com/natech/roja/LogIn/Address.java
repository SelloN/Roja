package com.natech.roja.LogIn;

/**
 * Created by Tshepo on 2016/04/17.
 */
public class Address {

    private String streetAddress,complexName,province,label,addressID;

    public Address(String addressID, String streetAddress, String complexName, String province, String label){
        this.addressID = addressID;
        this.streetAddress = streetAddress;
        this.complexName = complexName;
        this.province = province;
        this.label = label;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setStreetAddress(String streetAddress){
        this.streetAddress = streetAddress;
    }

    public void setComplexName(String complexName){
        this.complexName = complexName;
    }

    public void setProvince(String province){
        this.province = province;
    }

    public String getStreetAddress(){
        return streetAddress;
    }

    public String getComplexName(){
        return complexName;
    }

    public String getProvince(){
        return province;
    }

    public String getLabel(){
        return label;
    }

    public String getAddressID(){
        return addressID;
    }


}
