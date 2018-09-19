package io.vov.vitamio.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MR.XIE on 2018/9/13.
 */
public class CommonUtils {
    /**
     * 得到系统时间
     */
    public static String getSystemTime(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        return  simpleDateFormat.format(new Date());
    }
    /**
     * 得到屏幕的宽度
     */
    public static int getScreenWidth(Context context){
        final DisplayMetrics displayMetrics=new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
       return  displayMetrics.widthPixels;

    }
    public  static int getScreenHeight(Context context){
        final DisplayMetrics displayMetrics=new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return  displayMetrics.heightPixels;
    }
    /**
     * 判断是不是网路地址
     * @return
     */
    public static boolean isNetUrl(String url) {
        boolean reault = false;
        if (url != null) {
            if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("rtsp") || url.toLowerCase().startsWith("mms")||
                    url.toLowerCase().startsWith("rtmp")) {
                reault = true;
            }
        }
        return reault;
    }

}
