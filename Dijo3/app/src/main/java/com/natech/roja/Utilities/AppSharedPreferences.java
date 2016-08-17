package com.natech.roja.Utilities;

/**
 * Created by Tshepo on 2015/08/28. Common Names for Shared Preferences
 */
public class AppSharedPreferences {

    private final static String ID_FILE ="tableIDs",
            REGISTRATION_ID = "registrationID",CREDENTIALS_FILE = "credentials", LOG_STATE = "logState",
            SETTINGS = "settings", REVIEW_NOTIFICATIONS = "reviewNotifications", EXTRAS_FILE = "extras";

    public static String getIdFile(){
        return ID_FILE;
    }

    public static String getCredentialsFile(){
        return CREDENTIALS_FILE;
    }

    public static String getExtrasFile(){
        return EXTRAS_FILE;
    }

    public static String getRegistrationId(){
        return REGISTRATION_ID;
    }

    public static String getLogState(){
        return LOG_STATE;
    }

    public static String getSettings(){
        return SETTINGS;
    }

// --Commented out by Inspection START (2015/10/17 04:09 PM):
    public static String getReviewNotifications(){
        return REVIEW_NOTIFICATIONS;
    }
// --Commented out by Inspection STOP (2015/10/17 04:09 PM)

}
