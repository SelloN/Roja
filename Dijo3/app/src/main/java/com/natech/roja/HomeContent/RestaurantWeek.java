package com.natech.roja.HomeContent;

/**
 * Created by Tshepo on 2015/08/22.
 */
@SuppressWarnings("DefaultFileTemplate")
class RestaurantWeek {

    private double lat,lon;
    private String story, address;
    private Boolean isAddress = false;

    public RestaurantWeek(String story){
        this.story = story;
    }
    public RestaurantWeek( String address, double lat, double lon){
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.isAddress = true;
    }

    public Boolean getIsAddress(){
        return isAddress;
    }

    public String getStory(){
        return story;
    }

    public String getAddress(){
        return address;
    }

    public double getLat(){
        return lat;
    }

    public double getLon(){
        return lon;
    }
}
