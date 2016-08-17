package com.natech.roja.HomeContent;

/**
 * Created by Tshepo on 2015/08/23.
 */
@SuppressWarnings("DefaultFileTemplate")
public class WeeklyContent {

    private final int weeklyID;
    private final String photoDir;

    public WeeklyContent(int weeklyID, String photoDir){
        this.weeklyID = weeklyID;
        this.photoDir = photoDir;
    }

    public String getPhotoDir(){
        return photoDir;
    }

    public int getWeeklyID(){
        return weeklyID;
    }
}
