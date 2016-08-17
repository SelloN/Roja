package com.natech.roja.Information;

/**
 * Created by Tshepo on 2015/10/30.
 */
public class Points {

    final String restaurantName;
    final String restID;
    final String thumbPhotoDir;
    int points;
    final int pointsID;
    final String claims;
    final int restPointID;
    final String restEmail;

    public Points(String restaurantName, String restID, String thumbPhotoDir, int points, int pointsID,String restEmail){
        this.points = points;
        this.restID = restID;
        this.thumbPhotoDir = thumbPhotoDir;
        this.restaurantName = restaurantName;
        this.restEmail = restEmail;
        this.pointsID = pointsID;
        claims = null;
        restPointID = 0;
    }

    public Points(int points,String claims,int restPointID){
        this.claims = claims;
        this.points = points;
        this.restPointID =  restPointID;
        this.restID = null;
        pointsID = 0;
        this.thumbPhotoDir = null;
        this.restaurantName = null;
        this.restEmail = null;
    }

    public String getRestEmail(){
        return restEmail;
    }

    public void setPoints(int points){
        this.points = points;
    }

    public int getRestPointID(){
        return restPointID;
    }

    public int getPointsID(){
        return pointsID;
    }

    public String getClaims(){
        return claims;
    }

    public String getRestaurantName(){
        return restaurantName;
    }

    public String getThumbPhotoDir(){
        return thumbPhotoDir;
    }

    public int getPoints(){
        return points;
    }

    public String getRestID(){
        return restID;
    }
}
