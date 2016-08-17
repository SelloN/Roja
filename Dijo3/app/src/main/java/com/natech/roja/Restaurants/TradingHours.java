package com.natech.roja.Restaurants;

/**
 * Created by Tshepo on 2015/08/10.
 */
@SuppressWarnings("DefaultFileTemplate")
class TradingHours {
    private final String day, time;
    private final int dayIndex;

    public TradingHours(int dayIndex, String time){
        String[] days = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
        this.day = days[dayIndex-1];
        this.time = time;
        this.dayIndex = dayIndex-1;
    }

    public String getDay(){
        return day;
    }

    public String getTime(){
        return time;
    }

    public int getDayIndex(){
        return dayIndex;
    }
}
