package com.social.yourturn.utils;

import android.os.Build;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by ousma on 4/12/2017.
 */

public class Utils {

    public static boolean hasGingerbread(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static String formatDate(long dateInMillis){

        Date date = new Date(dateInMillis);
        DateTime dt = new DateTime(date);
        String formattedDate = "";
        formattedDate += dt.getMonthOfYear() + "/";
        formattedDate += dt.getDayOfMonth() + "/";
        formattedDate += (dt.getYear()%2000);
        return formattedDate;
    }
}
