package com.natech.roja.Restaurants;

import java.util.List;

/**
 * Created by Tshepo on 2015/07/31.
 */
@SuppressWarnings({"SameParameterValue", "DefaultFileTemplate"})
public class Restaurant {

    private String restaurantName,description, rating, location,restID, thumbDir,rateCount, directions, email, phone,franchiseID;
    private double distance, lat, lon;
    private Boolean isAboutUs = false;
    private Boolean isDirection = false;
    private Boolean isRating = false;
    private Boolean isContact = false;
    private Boolean isFranchise = false;
    private Boolean isAlcohol = false;
    private Boolean isDelivery = false;
    private Boolean isHalal = false;
    private Boolean isWifi = false;
    private char restaurantLabel;
    private List<TradingHours> tradingHours;
    private int currentDay;
    @SuppressWarnings("FieldCanBeLocal")
    private Boolean isTradingHours;
    private Boolean isFeatures = false;
    private Boolean showReviews = false;

    public Restaurant(String restaurantName, String description, String rating, String location,
                      String restID){
        this.description = description;
        this.restaurantName = restaurantName;
        this.location = location;
        this.rating = rating;
        this.restID = restID;
    }

    public Restaurant(String restaurantName, String rating,String restID,String thumbDir, String rateCount,double distance){

        this.restaurantName = restaurantName;
        this.rating = rating;
        this.restID = restID;
        this.thumbDir = thumbDir;
        this.rateCount = rateCount;
        this.distance = distance;
    }

    public Restaurant(){

    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setRating(String rating){
        this.rating = rating;
    }

    public void setShowReviews(int packageCode){
        showReviews = packageCode != 1;
    }

    public void setCurrentDay(int currentDay){
        this.currentDay = currentDay;
    }

    public void setIsFranchise(Boolean isFranchise){
        this.isFranchise = isFranchise;
    }

    public void setFranchiseID(String franchiseID){
        this.franchiseID = franchiseID;
    }

    public void setTradingHours(List<TradingHours> tradingHours){
        this.tradingHours = tradingHours;
    }

    public void setRestaurantLabel(char label){
        restaurantLabel = label;
    }

    public void setIsAlcohol(int isAlcohol){
        this.isAlcohol = isAlcohol == 1;
    }

    public void setIsDelivery(int isDelivery){
        this.isDelivery = isDelivery == 1;
    }

    public void setIsHalal(int isHalal){
        this.isHalal = isHalal == 1;
    }

    public void setIsWifi(int isWifi){
        this.isWifi = isWifi == 1;
    }

    public void setRestaurantName(String restaurantName){
        this.restaurantName = restaurantName;
    }

    public void setRestID(String restID){
        this.restID = restID;
    }

    public Boolean getShowReviews(){
        return showReviews;
    }

    public void setDirections(String directions){
        this.directions = directions;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public void setLon(double lon){
        this.lon = lon;
    }

    public String getDirections(){
        return directions;
    }

    public void setIsTradingHours(Boolean isTradingHours){
        //noinspection UnusedAssignment
        this.isTradingHours = isTradingHours;
    }

    public void setIsAboutUs(Boolean isAboutUs){
        this.isAboutUs = isAboutUs;
    }

    public void setIsDirection(Boolean isDirection){
        this.isDirection = isDirection;
    }

    public void setIsFeatures(Boolean isFeatures){
        this.isFeatures = isFeatures;
    }

    public void setIsRating(Boolean isRating){
        this.isRating = isRating;
    }

    public void setIsContact(Boolean isContact){
        this.isContact = isContact;
    }

    public Boolean getIsAboutUs(){
        return isAboutUs;
    }

    public Boolean getIsDirection(){
        return isDirection;
    }

    public Boolean getIsRating(){
        return isRating;
    }

    public Boolean getIsContact(){
        return isContact;
    }

    public Boolean getIsFranchise(){
        return isFranchise;
    }

    public String getRateCount(){
        return rateCount;
    }
    public String getThumbDir(){
        return thumbDir;
    }
    public String getRestaurantName(){
        return restaurantName;
    }

    public String getDescription(){
        return description;
    }

    public String getRating(){
        return rating;
    }

    public String getLocation(){
        return location;
    }

    public int getRestID(){
        return Integer.parseInt(restID);
    }

    public int getCurrentDay(){
        return currentDay;
    }

    public double getDistance(){
        return distance;
    }

    public double getLat(){
        return lat;
    }

    public double getLon(){
        return lon;
    }

    public List<TradingHours> getTradingHours(){
        return tradingHours;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }

    public String getFranchiseID(){
        return franchiseID;
    }

    public Boolean getIsAlcohol(){
        return isAlcohol;
    }

    public Boolean getIsDelivery(){
        return isDelivery;
    }

    public Boolean getIsWifi(){
        return isWifi;
    }

    public Boolean getIsHalal(){
        return isHalal;
    }

    public Boolean getIsFeatures(){
        return isFeatures;
    }

    public char getRestaurantLabel(){
        return restaurantLabel;
    }

// --Commented out by Inspection START (2015/09/15 06:16 PM):
//    public Boolean getIsTradingHours(){
//        return isTradingHours;
//    }
// --Commented out by Inspection STOP (2015/09/15 06:16 PM)
}
