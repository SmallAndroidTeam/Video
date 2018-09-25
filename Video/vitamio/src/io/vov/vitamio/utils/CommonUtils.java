package io.vov.vitamio.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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
     * 判断是不是网络地址
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
    //判断当前的网络是否可用
    public  static boolean net_avaiable(Context context){
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isAvailable()){//当前网络可用
            return true;
        }else{
            return false;
        }
    }
}
