package com.of.vmvideo.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.of.vmvideo.activity.DownloadCenterActivity;
import com.of.vmvideo.activity.PlayActivity;
import com.of.vmvideo.broadcast.NetWorkChangeReceiver;
import com.of.vmvideo.fragment.FilmFragment;
import com.of.vmvideo.utils.HttpUtil;
import com.of.vmvideo.utils.SaveVideoDownloadStatus;

import java.net.HttpURLConnection;
import java.util.ArrayList;


import io.vov.vitamio.toast.oneToast;
import io.vov.vitamio.utils.CommonUtils;

/**
 * 下载服务
 */
public class DownLoadService extends Service implements HttpUtil.LocalPlay, NetWorkChangeReceiver.NetWorkChange {
    public final  static String  DOWNLOAD="download";//下载
    public final  static String DELETEDOWNLOAD="delete_download";//删除下载
    public  final static String CONTINUE_DOWNLAOD="continue_download";//继续下载

    private static boolean isPauseDownload=false;//是否暂停下载
    private static Context mainActivityContext;//主页的上下文环境
    private static Context playActivityContext;//播放界面的上下文环境
    private static Context DownloadCenterActivityContext;//下载控制界面的上下文环境
    private static DownLoadSuccess downLoadSuccess;//下载成功接口
    private NetWorkChangeReceiver netWorkChangeReceiver;//网络变化广播
    private AlertDialog alertDialog;

    public static void setMainActivityContext(Context mainActivityContext) {
        DownLoadService.mainActivityContext = mainActivityContext;
    }

    public static void setPlayActivityContext(Context playActivityContext) {
        DownLoadService.playActivityContext = playActivityContext;
    }

    public static void setDownloadCenterActivityContext(Context downloadCenterActivityContext) {
        DownloadCenterActivityContext = downloadCenterActivityContext;
    }

    public static void setIsPauseDownload(boolean isPauseDownload) {
        DownLoadService.isPauseDownload = isPauseDownload;
        Log.i(TAG, "setIsPauseDownload:"+DownLoadService.isPauseDownload);
    }

    @Override
    public void onCreate() {
        //注册网络变化广播
        netWorkChangeReceiver = new NetWorkChangeReceiver();
        netWorkChangeReceiver.setNetWorkChange(this);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(FilmFragment.NET_CONNNECT_CHANGE);
        registerReceiver(netWorkChangeReceiver,intentFilter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(netWorkChangeReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
           String action=intent.getAction();
         int position= intent.getIntExtra("position",0);//视频下标
         String downLoadVideoPath=intent.getStringExtra("downLoadVideoPath");//获取下载视频的网络地址
         String videoName=intent.getStringExtra("videoName");//获取下载视频的视频名
        String coverImg=intent.getStringExtra("coverImg");
        if(downLoadVideoPath==null||videoName==null){
            return super.onStartCommand(intent, flags, startId);
        }
        switch (action){
            case DOWNLOAD:
                int currentNetState=CommonUtils.getCurrentNetState(this);
                HttpUtil.setStartNetState(currentNetState);
                HttpUtil.setCurrentNetState(currentNetState);

                HttpUtil httpUtil=new HttpUtil(this,position);
                httpUtil.setLocalPlay(this);
                httpUtil.sendHttpRequest(downLoadVideoPath,videoName,coverImg);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void localPlay(String downLoadVideoPath, int position) {
      if(downLoadSuccess!=null){
          downLoadSuccess.localPaly(downLoadVideoPath,position);
      }
    }

    public static void setDownLoadSuccess(DownLoadSuccess downLoadSuccess1) {
       downLoadSuccess = downLoadSuccess1;
    }


    //下载时网络发生变化设置HttpUntil里面的网络状态
    private final  static String TAG="movie6";

    @Override
    public void netUnAvailable() {
        HttpUtil.setCurrentNetState(CommonUtils.getCurrentNetState(this));
        Log.i(TAG, "netUnAvailable: ");
    }

    @Override
    public void netWIFIAvaiable() {

        HttpUtil.setCurrentNetState(CommonUtils.getCurrentNetState(this));
        oneToast.showMessage(getApplication(),"开始WIFI下载");
        if(isPauseDownload) {
            for(int i = 0; i< SaveVideoDownloadStatus.getVideoDownloadStatusList().size(); i++){
                SaveVideoDownloadStatus videoDownloadStatusList=SaveVideoDownloadStatus.getVideoDownloadStatusList().get(i);
                HttpUtil httpUtil=new HttpUtil(videoDownloadStatusList);
                httpUtil.continueDownLoad();
                SaveVideoDownloadStatus.setDownloadStatus(i);
            }
        }
         SaveVideoDownloadStatus.setVideoDownloadStatusList(new ArrayList<SaveVideoDownloadStatus>());
        isPauseDownload=false;//设置下载暂停为false
        Log.i(TAG, "netWIFIAvaiable: ");
    }

    @Override
    public void netGPRSAvaiable() {

        HttpUtil.setCurrentNetState(CommonUtils.getCurrentNetState(this));
        Log.i(TAG, "是否暂停下载了："+isPauseDownload);
        if(isPauseDownload){
            Log.i(TAG, "GPRS连接 ");
            Context mcontext=null;
            if(PlayActivity.isIsExists())
            {
                mcontext=playActivityContext;
            }else if(DownloadCenterActivity.isIsExists()){
                mcontext=DownloadCenterActivityContext;
            }
            else{
                mcontext=mainActivityContext;
            }

            if(alertDialog==null){
                alertDialog = new AlertDialog.Builder(mcontext).setTitle("下载").setMessage("当前处于移动数据网络，确定下载？").setNegativeButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        oneToast.showMessage(getApplication(),"使用流量下载");

                        for(int i=0;i<SaveVideoDownloadStatus.getVideoDownloadStatusList().size();i++){
                            SaveVideoDownloadStatus videoDownloadStatusList=SaveVideoDownloadStatus.getVideoDownloadStatusList().get(i);
                            HttpUtil httpUtil=new HttpUtil(videoDownloadStatusList);
                            httpUtil.continueDownLoad();
                            SaveVideoDownloadStatus.setDownloadStatus(i);
                        }
                        SaveVideoDownloadStatus.setVideoDownloadStatusList(new ArrayList<SaveVideoDownloadStatus>());

                        alertDialog=null;
                        isPauseDownload=false;//设置下载暂停为false
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog=null;
                    }
                }).setCancelable(false).show();
            }
            Log.i(TAG, "GPRS连接 111");
        }
        Log.i(TAG, "netGPRSAvaiable://///// ");
    }


    public interface  DownLoadSuccess{
         void localPaly(String downLoadVideoPath, int position);
    }
}
