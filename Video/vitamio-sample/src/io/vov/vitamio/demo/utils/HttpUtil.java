package io.vov.vitamio.demo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vov.vitamio.R;
import io.vov.vitamio.demo.activity.DownloadCenterActivity;
import io.vov.vitamio.demo.activity.MainActivity;
import io.vov.vitamio.demo.fragment.SdCardFragment;
import io.vov.vitamio.demo.saveDate.SaveCollectFragment;
import io.vov.vitamio.demo.service.DownLoadService;
import io.vov.vitamio.provider.VideoCollectOperation;
import io.vov.vitamio.utils.CommonUtils;


/**
 * Created by MR.XIE on 2018/9/20.
 * 下载网络视频
 */
public class HttpUtil {
    private final  static String TAG="movie";
    private Context context;
    private  static NotificationManager manager;
    private  NotificationCompat.Builder builder;
    private   static int notifYID=100;
    private int currentID=100;
    private static  PendingIntent downNotification;
    private static PendingIntent click;
   private  final static String id ="0x1";
    public  final  static  String description="下载通知";
   public final  static int EmergencyLevel=NotificationManager.IMPORTANCE_LOW;//通知的紧急程度
   private final  static String resultId="0x2";//下载结束的通知
    private final  static String resultDescription="下载结束通知";
    private final  static int resultEmergencyLevel=NotificationManager.IMPORTANCE_HIGH;//下载结束后通知的紧急程度
   private String videoName;
    private String coverImg;//视频的缩略图地址
   private long videoSize=0;
   private VideoDownload videoDownload=null;
   private String downLoadVideoPath=null;
   private LocalPlay localPlay;
   private int position;
   private boolean isDownSuccess=false;
    private String  saveDownLoadVideoAddress=null;//保存视频的地址
    private static boolean isAlertDialogClick=false;//弹出框是否执行了点击事件
    private static boolean clickResult=false;//弹出框点击结果，false为取消继续下载，true为继续下载
    long downByteNumber=0;//下载的字符个数
    public static List<DownLoadingState> downloadingVideoPath=new ArrayList<>();//保存正在下载的视频地址和当前的下载状态和目标下载状态
    public String DownLoadSavePath=null;//保存所有下载的视频地址

    private static int startNetState=2;//一开始的网络状态
    private static int currentNetState=2;//当前的网络状态  0表示网络不可用,1表示网络为WIFI，2表示网络为GPRS
    private AlertDialog alertDialog;


    public static String getTAG() {
        return TAG;
    }

    public static int getStartNetState() {
        return startNetState;
    }

    public static void setStartNetState(int startNetState) {
        HttpUtil.startNetState = startNetState;
    }

    public static int getCurrentNetState() {
        return currentNetState;
    }

    public static void setCurrentNetState(int currentNetState) {
        HttpUtil.currentNetState = currentNetState;
    }

    public void setLocalPlay(LocalPlay localPlay) {
        this.localPlay = localPlay;
    }

    //设置下标为index的视频为目标状态为删除状态
    public  static  void setVideoDeleteTargetState(int index){
        DownLoadingState downLoadingState=downloadingVideoPath.get(index);
        downLoadingState.setTargetState(2);//设置目标状态为删除
        downloadingVideoPath.set(index,downLoadingState);

    }

    public HttpUtil(Context context,int position) {
        this.context = context;
        this.position=position;
        currentID=notifYID;
          notifYID++;
          if(manager==null)
          {
              manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
          }

          if(click==null){
              final Intent nowPlayingIntent=new Intent(Intent.ACTION_MAIN);
              nowPlayingIntent.setAction(Intent.ACTION_MAIN);
              nowPlayingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
              nowPlayingIntent.setComponent(new ComponentName("io.vov.vitamio.demo","io.vov.vitamio.demo.activity.MainActivity"));
              nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
              click = PendingIntent.getActivity(context,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);

          }
      if(downNotification==null){
          final Intent intent=new Intent(context, MainActivity.class);
          downNotification=PendingIntent.getActivity(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
      }


        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
            NotificationChannel channel=new NotificationChannel(id,description,EmergencyLevel);
            channel.enableLights(true);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
           builder=new NotificationCompat.Builder(context,id)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(false).setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
                    ;
        }
        else{
             builder=new NotificationCompat.Builder(context,null).setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher).setContentIntent(click).setWhen(System.currentTimeMillis()).setOngoing(true).setPriority(Notification.PRIORITY_HIGH);
        }

    }


    public synchronized void sendHttpRequest(final String address,final  String videoName,String videoImage)
    {
        final DownLoadingState downLoadingState=new DownLoadingState(address,videoName,videoImage,0,0);
        downloadingVideoPath.add(downLoadingState);
        this.videoName=videoName;
        this.coverImg=videoImage;
        downLoadVideoPath=address;
        new Thread(new Runnable() {
            @Override
            public void run() {

                String  newVideoName= VideoDownload.getNoExistVideoPath(address,videoName);//获取一个不存在的视频名称

                 DownLoadSavePath=VideoDownload.getVideoPathPrefix();
                Log.i(TAG, "保存的路径为:"+newVideoName);
                if(newVideoName==null){
                    onPostExecute(false);
                    return;
                }
                Log.i(TAG, "地址:"+address+"//名称："+videoName+"开始下载");
                   builder.setProgress(100,0,false);
                  builder.setContentTitle("正在下载"+videoName).setContentText("下载中");
                  manager.notify(currentID,builder.build());

                  HttpURLConnection connection=null;
                try {
                    URL url=new URL(address);

                    videoDownload=new VideoDownload(context,newVideoName);//获取下载的对象
                    saveDownLoadVideoAddress=videoDownload.getVideoAbsolutePath();//

                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Accept-Encoding","identity");
                    connection.setDoInput(true);
                    connection.connect();
                    videoSize=connection.getContentLengthLong();
                    Log.i(TAG, "文件的大小: "+String.format("%.2f",1.0*videoSize/1024/1024)+"M");
                    InputStream inputStream=connection.getInputStream();
                    byte[] line = new byte[1024];
                    int readCount=0;
                    int dowmloadPercent=0;
                    int downloadingIndex=getDownloadingIndex(downLoadVideoPath); //获取当前下载的视频在下载列表中的下标
                    if(downloadingIndex==-1){//如果当前的下载视频在下载列表中不存在的话
                        onPostExecute(false);
                        return;
                    }
                    downLoadingState.setVideoSize(videoSize);
                    downloadingVideoPath.set(downloadingIndex,downLoadingState);
                    while((readCount=inputStream.read(line))!=-1){


                        if(downloadingVideoPath.get(downloadingIndex).getTargetState()==2){//如果目标的下载状态为删除

                             manager.cancel(currentID);
                             setDownLoadingStateComplete(); //设置当前视频下载完成（下载失败也叫完成)
                            //保存的视频地址删除掉并把数据库中的对应的数据删除
                            if(saveDownLoadVideoAddress!=null){
                                VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
                                videoCollectOperation.deleteSaveVideo(saveDownLoadVideoAddress);
                                if(new File(saveDownLoadVideoAddress).exists()){
                                    new File(saveDownLoadVideoAddress).delete();
                                }
                            }
                            return;
                        }


                        if(downloadingVideoPath.get(downloadingIndex).getTargetState()==1){//如果目标的下载状态为暂停
                             SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);
                             SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,address,saveDownLoadVideoAddress,
                                     videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);
                             SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                             pauseDownLoad();//暂停下载
                             //重新设置下载状态
                           DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                           downloadingVideoPath.set(downloadingIndex,downLoadingState1);
                             return;
                         }
                        if(currentNetState==0){//如果当前网络不可用

                            DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识
                            SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);
                            SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,address,saveDownLoadVideoAddress,
                                    videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                            SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                            pauseDownLoad();//暂停下载
                            //重新设置下载状态
                            DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                            downloadingVideoPath.set(downloadingIndex,downLoadingState1);
                            return;


                        }else if(currentNetState!=startNetState&&currentNetState==2){//如果当前的网络状态和一开始的网络状态不一致的话,并且此时是GPRS网络时

                             if(alertDialog==null){
                                alertDialog = new AlertDialog.Builder(context).setTitle("下载").setMessage("当前处于移动数据网络，继续下载？")
                                        .setCancelable(false).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                                                SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,address,saveDownLoadVideoAddress,
                                                        videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                                                SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                                pauseDownLoad();//暂停下载
                                                DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识

                                                alertDialog=null;
                                                isAlertDialogClick=true;
                                                clickResult=false;
                                                //重新设置下载状态
                                                DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                                downloadingVideoPath.set(getDownloadingIndex(downLoadVideoPath),downLoadingState1);
                                                return;
                                            }
                                        }).setNegativeButton("继续下载", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                isAlertDialogClick=true;
                                                clickResult=true;
                                                alertDialog=null;
                                                startNetState=2;
                                            }
                                        }).show();
                            }else{
                                while(!isAlertDialogClick){//如果弹出框没执行点击事件
                                    synchronized (Thread.currentThread()){
                                        try {
                                            wait(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if(clickResult){//如果点击了继续下载
                                    run();
                                }else if(!clickResult){//如果点击了取消
                                    SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                                    SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,address,saveDownLoadVideoAddress,
                                            videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                                    SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                    pauseDownLoad();//暂停下载

                                    //重新设置下载状态
                                    DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                    downloadingVideoPath.set(downloadingIndex,downLoadingState1);

                                    return;
                                }


                            }

                        }

                       if(!videoDownload.saveVideoByLine(line,readCount)){//如果下载保存失败
                          videoDownload.closeWriter();//关闭下载流
                           onPostExecute(false);
                           return;
                       }

                        downByteNumber+=readCount;
                        line=new byte[1024];
                        if(videoSize>0)
                        {
                            dowmloadPercent= (int) (100.0*downByteNumber/videoSize);
                        }
                        else{
                            dowmloadPercent=0;
                        }
                        downLoadingState.setDownByteNumber(downByteNumber);
                        downloadingVideoPath.set(downloadingIndex,downLoadingState);

                        onProgressUpdate(dowmloadPercent);

                    }

                    videoDownload.closeWriter();//下载完关闭下载流
                    //下载完更新本地视频视图
                    if(SaveCollectFragment.getSdCardFragment()!=null){
                        ((SdCardFragment)SaveCollectFragment.getSdCardFragment()).downLoadVideoUpdateView();
                    }
                    Log.i("movie5", "run: 下载完成");
                    isDownSuccess=true;
                    onPostExecute(true);

                }catch (SocketException ex){

                    SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                    SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,address,saveDownLoadVideoAddress,
                            videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                    SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);


                    pauseDownLoad();//暂停下载
                    DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识
                    //重新设置下载状态
                    DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                    downloadingVideoPath.set(getDownloadingIndex(downLoadVideoPath),downLoadingState1);
                    ex.printStackTrace();
                    Log.i("movie5", "捕获异常网络是否可用： "+ CommonUtils.net_avaiable(context));

                }
                catch (Exception e){
                    e.printStackTrace();
                    if(!isDownSuccess)
                    onPostExecute(false);
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }

            }
        }).start();
        }


    //继续下载后要执行的初始化
    public HttpUtil(SaveVideoDownloadStatus saveVideoDownloadStatus) {
        this.context=saveVideoDownloadStatus.getContext();
        this.position=saveVideoDownloadStatus.getPosition();
        this.currentID=saveVideoDownloadStatus.getCurrentID();
        this.downLoadVideoPath=saveVideoDownloadStatus.getDownLoadVideoAddress();
        this.saveDownLoadVideoAddress=saveVideoDownloadStatus.getSaveDownLoadVideoAddress();
        this.downByteNumber=saveVideoDownloadStatus.getDownByteNumber();
        this.videoSize=saveVideoDownloadStatus.getVideoSize();
        this.DownLoadSavePath=saveVideoDownloadStatus.getDownLoadSavePath();
       this.videoName=saveVideoDownloadStatus.getVideoName();
       this.localPlay=saveVideoDownloadStatus.getLocalPlay();
       this.coverImg=saveVideoDownloadStatus.getCoverImg();
        if(manager==null)
        {
            manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if(click==null){
            final Intent nowPlayingIntent=new Intent(Intent.ACTION_MAIN);
            nowPlayingIntent.setAction(Intent.ACTION_MAIN);
            nowPlayingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            nowPlayingIntent.setComponent(new ComponentName("io.vov.vitamio.demo","io.vov.vitamio.demo.activity.MainActivity"));
            nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            click = PendingIntent.getActivity(context,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        }
        if(downNotification==null){
            final Intent intent=new Intent(context, MainActivity.class);
            downNotification=PendingIntent.getActivity(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }


        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
            NotificationChannel channel=new NotificationChannel(id,description,EmergencyLevel);
            channel.enableLights(true);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
            builder=new NotificationCompat.Builder(context,id)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(false).setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
            ;
        }
        else{
            builder=new NotificationCompat.Builder(context,null).setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher).setContentIntent(click).setWhen(System.currentTimeMillis()).setOngoing(true).setPriority(Notification.PRIORITY_HIGH);
        }

    }

        //继续下载
        public void continueDownLoad(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!new File(saveDownLoadVideoAddress).exists()){
                    onPostExecute(false);
                    return;
                }else{
                    Log.i("movie9", "地址:"+downLoadVideoPath+"//名称："+videoName+"继续下载");

                    manager.cancel(currentID);

                    if(videoSize>0){
                        int progress= (int) (100.0*downByteNumber/videoSize);
                        builder.setProgress(100,progress,false);
                        builder.setContentText("下载"+progress+"%");
                    }else{
                        builder.setProgress(0,0,true);
                        builder.setContentText("");
                    }
                    builder.setContentTitle("正在下载 "+videoName);


                    manager.notify(currentID,builder.build());



                    int downloadingIndex=getDownloadingIndex(downLoadVideoPath); //获取当前下载的视频在下载列表中的下标
                    if(downloadingIndex==-1){//如果当前的下载视频在下载列表中不存在的话
                        onPostExecute(false);
                        return;
                    }

                    //重新设置下载状态
                    DownLoadingState  downLoadingState=new DownLoadingState(downLoadVideoPath,videoName,coverImg,0,0);
                    downLoadingState.setVideoSize(videoSize);
                    downloadingVideoPath.set(downloadingIndex,downLoadingState);

                    HttpURLConnection connection=null;
                    try {

                        URL url = new URL(downLoadVideoPath);
                        videoDownload=new VideoDownload(context,saveDownLoadVideoAddress,true);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        connection.setRequestProperty("Accept-Encoding", "identity");
                        connection.setRequestProperty("RANGE","bytes="+(downByteNumber+1)+"-"+videoSize);//从已经下载的字符后面开始下载
                        connection.setDoInput(true);
                        connection.connect();

                        InputStream inputStream=connection.getInputStream();
                        byte[] line = new byte[1024];
                        int readCount=0;
                        int dowmloadPercent=0;

                        while((readCount=inputStream.read(line))!=-1){

                            if(downloadingVideoPath.get(downloadingIndex).getTargetState()==2){//如果目标的下载状态为删除
                                manager.cancel(currentID);
                                setDownLoadingStateComplete(); //设置当前视频下载完成（下载失败也叫完成)
                                //保存的视频地址删除掉并把数据库中的对应的数据删除
                                if(saveDownLoadVideoAddress!=null){
                                    VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
                                    videoCollectOperation.deleteSaveVideo(saveDownLoadVideoAddress);
                                    if(new File(saveDownLoadVideoAddress).exists()){
                                        new File(saveDownLoadVideoAddress).delete();
                                    }
                                }
                                return;
                            }


                            if(downloadingVideoPath.get(downloadingIndex).getTargetState()==1){//如果目标的下载状态为暂停

                                SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);
                                SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,downLoadVideoPath,saveDownLoadVideoAddress,
                                        videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);
                                SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                pauseDownLoad();//暂停下载
                                //重新设置下载状态
                                DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                downloadingVideoPath.set(downloadingIndex,downLoadingState1);
                                return;
                            }



                            if(currentNetState==0){//如果当前网络不可用

                                DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识
                                SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);
                                SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,downLoadVideoPath,saveDownLoadVideoAddress,
                                        videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                                SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                pauseDownLoad();//暂停下载
                                //重新设置下载状态
                                DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                downloadingVideoPath.set(downloadingIndex,downLoadingState1);
                                return;

                            }else if(currentNetState!=startNetState&&currentNetState==2){//如果当前的网络状态和一开始的网络状态不一致的话,并且此时是GPRS网络时

                                if(alertDialog==null){
                                    alertDialog = new AlertDialog.Builder(context).setTitle("下载").setMessage("当前处于移动数据网络，继续下载？")
                                            .setCancelable(false).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                                                    SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,downLoadVideoPath,saveDownLoadVideoAddress,
                                                            videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                                                    SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                                    pauseDownLoad();//暂停下载
                                                    DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识

                                                    alertDialog=null;
                                                    isAlertDialogClick=true;
                                                    clickResult=false;
                                                    //重新设置下载状态
                                                    DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                                    downloadingVideoPath.set(getDownloadingIndex(downLoadVideoPath),downLoadingState1);
                                                    return;
                                                }
                                            }).setNegativeButton("继续下载", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    isAlertDialogClick=true;
                                                    clickResult=true;
                                                    alertDialog=null;
                                                    startNetState=2;
                                                }
                                            }).show();
                                }else{
                                    while(!isAlertDialogClick){//如果弹出框没执行点击事件
                                        synchronized (Thread.currentThread()){
                                            try {
                                                wait(10);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if(clickResult){//如果点击了继续下载
                                        run();
                                    }else if(!clickResult){//如果点击了取消
                                        SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                                        SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,downLoadVideoPath,saveDownLoadVideoAddress,
                                                videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                                        SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);
                                        pauseDownLoad();//暂停下载

                                        //重新设置下载状态
                                        DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                                        downloadingVideoPath.set(downloadingIndex,downLoadingState1);

                                        return;
                                    }


                                }

                            }

                            if(!videoDownload.saveVideoByLine(line,readCount)){//如果下载保存失败
                                videoDownload.closeWriter();//关闭下载流
                                onPostExecute(false);
                                return;
                            }

                            downByteNumber+=readCount;
                            line=new byte[1024];
                            if(videoSize>0)
                            {
                                dowmloadPercent= (int) (100.0*downByteNumber/videoSize);
                            }
                            else{
                                dowmloadPercent=0;
                            }
                            downLoadingState.setDownByteNumber(downByteNumber);
                            downloadingVideoPath.set(downloadingIndex,downLoadingState);
                            onProgressUpdate(dowmloadPercent);
                        }

                        videoDownload.closeWriter();//下载完关闭下载流
                        //下载完更新本地视频视图
                        if(SaveCollectFragment.getSdCardFragment()!=null){
                            ((SdCardFragment)SaveCollectFragment.getSdCardFragment()).downLoadVideoUpdateView();
                        }
                        isDownSuccess=true;
                        onPostExecute(true);
                    }catch (SocketException ex){

                        SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);

                        SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus(context,position,downLoadVideoPath,saveDownLoadVideoAddress,
                                videoName,downByteNumber,videoSize,currentID,DownLoadSavePath,localPlay,coverImg);

                        SaveVideoDownloadStatus.getVideoDownloadStatusList().add(saveVideoDownloadStatus);

                        pauseDownLoad();//暂停下载
                        DownLoadService.setIsPauseDownload(true);//设置下载服务暂停下载标识

                        //重新设置下载状态
                        DownLoadingState  downLoadingState1=new DownLoadingState(downLoadVideoPath,videoName,coverImg,1,1);
                        downloadingVideoPath.set(getDownloadingIndex(downLoadVideoPath),downLoadingState1);
                        ex.printStackTrace();
                        Log.i("movie9", "捕获异常网络是否可用： "+ CommonUtils.net_avaiable(context));
                    }
                    catch (Exception e){
                        Log.i("movie9", "run: 下载异常");
                        e.printStackTrace();
                        if(!isDownSuccess)
                            onPostExecute(false);
                    }finally {
                        if(connection!=null){
                            connection.disconnect();
                        }
                    }
                }
            }
        }).start();

        }

    private    void onProgressUpdate(final Integer value) {
        int progress=value;
        if(progress<=0){
            progress=0;
        }else if(progress>=100){
            progress=100;
        }
           if(videoSize>0){
               builder.setProgress(100,progress,false);
               builder.setContentText("下载"+progress+"%");

           }else{
               builder.setProgress(0,0,true);
               builder.setContentText("");
           }

            manager.notify(currentID,builder.build());
    }
  private  synchronized   void onPostExecute(Boolean aBoolean) {

     // removieDownloadVideoFromList();  //删除下载列表中的的视频
     setDownLoadingStateComplete(); //设置当前视频下载完成（下载失败也叫完成)
      SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(currentID);//通过通知ID删除保存的下载状态

      if(aBoolean==true){

          if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
             manager.cancel(currentID);
              notifYID++;
              currentID=notifYID;
              NotificationChannel channel=new NotificationChannel(resultId,resultDescription,resultEmergencyLevel);
              channel.enableLights(true);
              channel.enableVibration(true);
              manager.createNotificationChannel(channel);
              builder=new NotificationCompat.Builder(context,resultId)
                      .setSmallIcon(R.drawable.ic_launcher)
                      .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(true).setOngoing(false).setPriority(Notification.PRIORITY_HIGH)
              .setContentText(videoName+"已经下载完成").setDefaults(NotificationCompat.DEFAULT_SOUND);
          }
          else{
              builder=new NotificationCompat.Builder(context,null)
                      .setSmallIcon(R.drawable.ic_launcher)
                      .setContentIntent(click).setWhen(System.currentTimeMillis()).setOngoing(false).setPriority(Notification.PRIORITY_HIGH).setAutoCancel(true)
              .setFullScreenIntent(downNotification,true).setContentText(videoName+"已经下载完成").setDefaults(NotificationCompat.DEFAULT_SOUND);
          }
              manager.notify(currentID,builder.build());
          //下载成功之后把下载的视频地址和保存的视频地址存到数据库中
          if(saveDownLoadVideoAddress!=null){
              VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
              videoCollectOperation.saveVideoDownload(downLoadVideoPath,saveDownLoadVideoAddress);
              videoCollectOperation.keepDownloadVideoTotalSize(DownLoadSavePath);
          }
          //下载成功后本地播放
                if(localPlay!=null){

              localPlay.localPlay(downLoadVideoPath,position);

                }
          Log.i("movie2", "onPostExecute:成功 ");
      }
      else{

          if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
              manager.cancel(currentID);
              notifYID++;
              currentID=notifYID;
              NotificationChannel channel=new NotificationChannel(resultId,resultDescription,resultEmergencyLevel);
              channel.enableLights(true);
              channel.enableVibration(true);
              manager.createNotificationChannel(channel);
              builder=new NotificationCompat.Builder(context,resultId)
                      .setSmallIcon(R.drawable.ic_launcher)
                      .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(true).setOngoing(false).setPriority(Notification.PRIORITY_HIGH)
                      .setContentText(videoName+"下载失败").setDefaults(NotificationCompat.DEFAULT_SOUND);
          }
          else{
              builder=new NotificationCompat.Builder(context,null)
                      .setSmallIcon(R.drawable.ic_launcher)
                      .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(true)
                     .setOngoing(false).setPriority(Notification.PRIORITY_HIGH)
                      .setFullScreenIntent(downNotification,true).setContentText(videoName+"下载失败").setDefaults(NotificationCompat.DEFAULT_SOUND);
          }
          manager.notify(currentID, builder.build());

          //下载失败后把保存的视频地址删除掉并把数据库中的对应的数据删除
          if(saveDownLoadVideoAddress!=null){
              VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
              videoCollectOperation.deleteSaveVideo(saveDownLoadVideoAddress);
             if(new File(saveDownLoadVideoAddress).exists()){
                 new File(saveDownLoadVideoAddress).delete();
             }
          }
          Log.i("movie2", videoName+"下载失败 ");
      }
  }



  //视频暂停下载
  private  synchronized void pauseDownLoad(){
       manager.cancel(currentID);
//      if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
//          NotificationChannel channel=new NotificationChannel(id,description,EmergencyLevel);
//          channel.enableLights(true);
//          channel.enableVibration(true);
//          manager.createNotificationChannel(channel);
//          builder=new NotificationCompat.Builder(context,id)
//                  .setSmallIcon(R.drawable.ic_launcher)
//                  .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(false).setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
//          ;
//      }
//      else{
//          builder=new NotificationCompat.Builder(context,null).setAutoCancel(false)
//                  .setSmallIcon(R.drawable.ic_launcher).setContentIntent(click).setWhen(System.currentTimeMillis()).setOngoing(true).setPriority(Notification.PRIORITY_HIGH);
//      }
//
//      if(videoSize>0){
//          int progress= (int) (100.0*downByteNumber/videoSize);
//          builder.setProgress(100,progress,false);
//          builder.setContentText("下载"+progress+"%");
//      }else{
//          builder.setProgress(0,0,true);
//          builder.setContentText("");
//      }
//      builder.setContentTitle("暂停下载 "+videoName);
//        manager.notify(currentID,builder.build());
  }

  //删除下载列表中的的视频
  public synchronized  void removieDownloadVideoFromList(){
      for(int i=0;i<downloadingVideoPath.size();i++){
          if(downloadingVideoPath.get(i).getDownLoadVideoAddress().contentEquals(downLoadVideoPath)){
            downloadingVideoPath.remove(i);
            break;
          }
      }
  }

  //设置当前视频下载完成（下载失败也叫完成)
  public  synchronized  void setDownLoadingStateComplete(){
      for(int i=0;i<downloadingVideoPath.size();i++){
          if(downloadingVideoPath.get(i).getDownLoadVideoAddress().contentEquals(downLoadVideoPath)&&!downloadingVideoPath.get(i).isDownComplete()){
          DownLoadingState downLoadingState=new DownLoadingState(downloadingVideoPath.get(i).getDownLoadVideoAddress(),videoName,coverImg,1,1);
          downLoadingState.setDownComplete(true);
          downloadingVideoPath.set(i,downLoadingState);
          return;
          }
      }
  }

    //设置下标为index的视频下载完成（下载失败也叫完成)
    public static synchronized  void setDownLoadingStateComplete(int index){
     if(index<0||index>=downloadingVideoPath.size()){
         return;
     }
        DownLoadingState downLoadingState=new DownLoadingState(downloadingVideoPath.get(index).getDownLoadVideoAddress(),downloadingVideoPath.get(index).getDownLoadVideoName(),downloadingVideoPath.get(index).getCoverImg(),1,1);
        downLoadingState.setDownComplete(true);
        downloadingVideoPath.set(index,downLoadingState);
    }

  //获取当前下载的视频在下载列表中的下标
  public static synchronized int getDownloadingIndex(String downLoadVideoPath){
        for(int i=0;i<downloadingVideoPath.size();i++){
            if(downloadingVideoPath.get(i).getDownLoadVideoAddress().contentEquals(downLoadVideoPath)&&!downloadingVideoPath.get(i).isDownComplete()){
                return i;
            }
        }
        return -1;
  }

  //判断此视频是否正在下载
  public static boolean isExistList(String name){
        for(DownLoadingState downLoadingState:downloadingVideoPath){
            if(downLoadingState.getDownLoadVideoAddress().contentEquals(name)&&!downLoadingState.isDownComplete()){
                return true;
            }
        }
        return false;
  }
  public interface   LocalPlay{
        void localPlay(String downLoadVideoPath,int position);//本地播放
    }
}
