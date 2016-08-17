package com.natech.roja.Restaurants;

/**
 * Created by Tshepo on 2015/07/30.
 */
@SuppressWarnings({"SameParameterValue", "DefaultFileTemplate"})
class RestaurantHistory {

    private final String thumbDir;
    private final String restaurantName;
    private final String visitDate;
    private final String logID;
    private final String restID;
    private final String[] months = {"January","February","March","April",
            "May","June","July","August","September","October","November","December"};
    private Boolean isRated = false;
    private Boolean isContent = false;
    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    private Boolean isProgress = false;

    public RestaurantHistory(String restaurantName,String visitDate, String thumbDir, String logID,
                             String restID, int rated, Boolean isContent){
        this.thumbDir = thumbDir;
        this.visitDate = visitDate;
        this.restaurantName = restaurantName;
        this.logID = logID;
        this.restID = restID;
        this.isContent = isContent;
        isRated = rated != 0;
    }
// --Commented out by Inspection START (2015/08/29 11:09 PM):
//    public RestaurantHistory(Boolean isProgress){
//        this.isProgress = isProgress;
//    }
// --Commented out by Inspection STOP (2015/08/29 11:09 PM)
    public void setIsRated(Boolean isRated){
        this.isRated = isRated;
    }
    public Boolean getIsRated(){
        return isRated;
    }
    public String getThumbDir(){
        return thumbDir;
    }
    public String getRestaurantName(){
        return restaurantName;
    }
    public String getLogID(){
        return logID;
    }

    public String getRestID(){
        return restID;
    }
    public String getFormattedVisitDate(){
        return visitDate.substring(8,10)+" "+months[Integer.parseInt(visitDate.substring(5,7))-1]+" "+visitDate.substring(0,4);

    }

    public Boolean getIsContent(){
        return isContent;
    }

    @SuppressWarnings("ConstantConditions")
    public Boolean getIsProgress(){
        return isProgress;
    }
}
