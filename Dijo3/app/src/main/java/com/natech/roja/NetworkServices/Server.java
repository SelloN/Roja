package com.natech.roja.NetworkServices;

/**
 * Created by Tshepo on 2015/08/28.
 * The server class holds all the links to the remote PHP files on
 * the server.
 */
public class Server {

    private static final String domain = "https://dijoapp.co.za/app_files/dijo/";

    public static String getFavouriteRestaurant(){
        return domain+"favourite_restaurant.php";
    }

    public static String getSubmitClaim(){
        return domain+"submit_claim.php";
    }

    public static String getRewards(){
        return domain+"get_rewards.php";
    }

    public static String getPoints(){
        return domain+"get_points.php";
    }

    public static String getUploadPhotoLink(String userID){
        return domain+"userPhotos/IMG_" + userID + ".png";
    }

    public static String getAddressBook(){
        return domain+"address_book.php";
    }

    public static String getUploadPhoto(){
        return domain+"upload_photo.php";
    }

    public static String getModifyOrder(){
        return domain+"modify_order.php?";
    }

    public static String getPlaceOffSiteOrder(){
        return domain+"place_offsite_order.php?";
    }

    public static String getTableLogID(){
        return domain+"get_table_log.php?";
    }

    public static String getCancelOffsiteOrder(){
        return domain+"cancel_offsite_order.php";
    }

    public static String getUpdateOrder(){
        return domain+"GCM/gcm_update_order.php?";
    }

    public static String getMenuItems(){
        return domain+"getMenuItems.php?";
    }

    public static String getMenuReviews(){
        return domain+"get_menu_reviews.php?";
    }

    public static String getRestaurantReviews(){
        return domain+"get_restaurant_reviews.php?";
    }

    public static String getHistory(){
        return domain+"get_history.php?";
    }

    public static String getItemExtras(){
        return domain+"item_extras.php?";
    }

    public static String getWeeklyContent(){
        return domain+"get_weekly_content.php?";
    }

    public static String getUserDetails(){
        return domain+"get_user_details.php?";
    }

    public static String getUserID(){
        return domain+"get_user_id.php?";
    }

    public static String getTrends(){
        return domain+"get_trends2.php?";
    }

    public static String getTestURL(){
        return domain+"test2.php";
    }

    public static String getMap(double lat, double lon){
        return "http://maps.google.com/maps?daddr="+lat+","+lon;
    }

    public static String getStaticMap(char label, double lat, double lon){
        return "http://maps.google.com/maps/api/staticmap?center=-"+lat+","+
                lon+"&zoom=15" +
                "&size=500x150&sensor=false&markers=color:red%7Clabel:"+label+"%7C"+
                lat+","+lon;
    }

    public static String getPrivacy(){
        return domain+"policies/index.html";
    }

    public static String getPromotions(){
        return domain+"getPromotions.php";
    }

    public static String getRestaurantHeaderDetails(){
        return domain+"getRestaurantDetails.php?";
    }

    public static String getRestaurantDetails(){
        return domain+"get_restaurant_details.php?";
    }

    public static String getCheckTradingHours(){
        return domain+"check_trading_hours.php?";
    }

    public static String getSubmitReview(){
        return domain+"submit_review.php?";
    }

    public static String getPlaces(){
        return domain+"get_places_latest.php?";
    }

    public static String getPendingReviews(){
        return domain+"pending_reviews.php?";
    }

    public static String getReviewAverages(){
        return domain+"get_review_averages.php?";
    }

    public static String getMenu(){
        return domain+"getMenu.php?";
    }

    public static String getLockTable(){
        return domain+"lock_table_user.php?";
    }

    public static String getSaveUser(){
        return domain+"GCM/save_user.php?";
    }

    public static String getEmailConfirmation(){
        return domain+"email_confirmation.php?";
    }

    public static String getResetPassword(){
        return domain+"reset_password.php?";
    }

    public static String getConfirmEmailKey(){
        return domain+"confirm_email_key.php?";
    }

    public static String getLogIn(){
        return domain+"log_in.php?";
    }

    public static String getAppVersion(){
        return domain+"app_version.php";
    }

    public static String getPlaceOrder(){
        return domain+"place_order.php?";
    }

    public static String getGCMOrder(){
        return domain+"GCM/gcm_order.php?";
    }

    public static String getOrder(){
        return domain+"get_order.php?";
    }

    public static String getRestaurantWeek(){
        return domain+"get_restaurant_week.php?";
    }

    public static String getArticles(){
        return domain+"get_articles.php?";
    }

    public static String getSubmitFeedback(){
        return domain+"submit_feedback.php?";
    }

    public static String getUpdateUserDetails(){
        return domain+"update_user_details.php?";
    }


}
