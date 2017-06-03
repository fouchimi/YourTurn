package com.social.yourturn.utils;

import android.os.Build;

import org.joda.time.DateTime;

import java.io.InputStream;
import java.io.OutputStream;
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

    public static boolean hasLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
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

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
