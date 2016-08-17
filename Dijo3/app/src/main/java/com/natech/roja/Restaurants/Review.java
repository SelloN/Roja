package com.natech.roja.Restaurants;

/**
 * Created by Tshepo on 2015/07/29. xc
 */
public class Review {

    private final String userName, rating, date, review, restaurantName;
    @SuppressWarnings("FieldCanBeLocal")
    private final int restaurantID;

    public Review(String userName, String rating, String date, String review, String restaurantName,
                  int restaurantID){
        this.userName = userName;
        this.rating = rating;
        this.date = date;
        this.review = review;
        this.restaurantID = restaurantID;
        this.restaurantName = restaurantName;
    }

    public String getUserName(){
        return userName;
    }
    public String getRating(){
        return rating;
    }

    public String getDate(){
        return date;
    }

    public String getReview(){
        return review;
    }

    public String getRestaurantName(){
        return restaurantName;
    }

}
