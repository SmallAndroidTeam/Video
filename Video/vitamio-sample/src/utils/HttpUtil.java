package utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
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
import java.net.URL;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import activity.MainActivity;
import activity.PlayActivity;
import fragment.SdCardFragment;
import io.vov.vitamio.R;
import io.vov.vitamio.provider.VideoCollectOperation;
import io.vov.vitamio.toast.oneToast;
import saveDate.SaveCollectFragment;

/**
 * Created by MR.XIE on 2018/9/20.
 * 下载网络视频
 */
public class HttpUtil {
    private final  static String TAG="movie";
    private Context context;
    private  NotificationManager manager;
    private  NotificationCompat.Builder builder;
    private   static int notifYID=100;
    private int currentID=100;
    private PendingIntent downNotification;
    private PendingIntent click;
   private  final static String id ="0x1";
   private  final  static  String description="下载通知";
   private final  static int EmergencyLevel=NotificationManager.IMPORTANCE_LOW;//通知的紧急程度
   private final  static String resultId="0x2";//下载结束的通知
    private final  static String resultDescription="下载结束通知";
    private final  static int resultEmergencyLevel=NotificationManager.IMPORTANCE_HIGH;//下载结束后通知的紧急程度
   private String videoName;
   private long videoSize=0;
   private VideoDownload videoDownload=null;
   private String downLoadVideoPath=null;
   private LocalPlay localPlay;
   private int position;
   private boolean isDownSuccess=false;
   public static List<String> downloadingVideoPath=new ArrayList<>();//保存正在下载的视频地址
    public void setLocalPlay(LocalPlay localPlay) {
        this.localPlay = localPlay;
    }

    public HttpUtil(Context context,int position) {
        this.context = context;
        this.position=position;
        currentID=notifYID;
          notifYID++;
          manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Intent nowPlayingIntent=new Intent(Intent.ACTION_MAIN);
        nowPlayingIntent.setAction(Intent.ACTION_MAIN);
        nowPlayingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        nowPlayingIntent.setComponent(new ComponentName("io.vov.vitamio.demo","activity.MainActivity"));
        nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
         click = PendingIntent.getActivity(context,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent intent=new Intent(context, MainActivity.class);
        downNotification=PendingIntent.getActivity(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

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

    public  void sendHttpRequest(final String address,final  String videoName)
    {
        downloadingVideoPath.add(address);
        this.videoName=videoName;
        downLoadVideoPath=address;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String  newVideoName= VideoDownload.getNoExistVideoPath(address,videoName);//获取一个不存在的路径
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
                    int downByteNumber=0;//下载的字符个数
                    byte[] line = new byte[1024];
                    int readCount=0;
                    int dowmloadPercent=0;
                   videoDownload=new VideoDownload(context,newVideoName);//获取下载的对象
                    while((readCount=inputStream.read(line))!=-1){
                       if(!videoDownload.saveVideoByLine(line,readCount)){//如果下载保存失败
                          videoDownload.closeWriter();//关闭下载流
                           onPostExecute(false);
                           return;
                       }
                        downByteNumber+=readCount;
                        line=new byte[1024];
                        dowmloadPercent= (int) (100.0*downByteNumber/videoSize);
                        onProgressUpdate(dowmloadPercent);
                    }
                    videoDownload.closeWriter();//下载完关闭下载流
                    //下载完更新本地视频视图
                    if(SaveCollectFragment.getSdCardFragment()!=null){
                        ((SdCardFragment)SaveCollectFragment.getSdCardFragment()).downLoadVideoUpdateView();
                    }
                    Log.i("movie2", "run: 下载完成");
                    isDownSuccess=true;
                    onPostExecute(true);

                }catch (Exception e){
                    e.printStackTrace();
                    if(!isDownSuccess)
                    onPostExecute(false);
                    return;
                }finally {
                    if(connection!=null){
                        connection.disconnect();
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

           }else{
               builder.setProgress(0,0,true);
           }
        builder.setContentText("下载"+progress+"%");
            manager.notify(currentID,builder.build());
    }
  private   void onPostExecute(Boolean aBoolean) {
      downloadingVideoPath.remove(downLoadVideoPath);
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
          if(videoDownload!=null){
              VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
              videoCollectOperation.saveVideoDownload(downLoadVideoPath,videoDownload.getVideoAbsolutePath());
          }
          //下载成功后本地播放
                if(localPlay!=null){
              localPlay.localPlay(position);
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
          if(videoDownload!=null){
              VideoCollectOperation videoCollectOperation=new VideoCollectOperation(context);
              videoCollectOperation.deleteSaveVideo(videoDownload.getVideoAbsolutePath());
             if(new File(videoDownload.getVideoAbsolutePath()).exists()){
                 new File(videoDownload.getVideoAbsolutePath()).delete();
             }
          }
          Log.i("movie2", videoName+"下载失败 ");
      }
  }

  public interface   LocalPlay{
        void localPlay(int position);//本地播放
    }
}
