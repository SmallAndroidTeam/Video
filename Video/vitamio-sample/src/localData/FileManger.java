package localData;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.bean.Video;


/**
 * Created by MR.XIE on 2018/9/8.
 * 获取本机的各种文件
 */
public class FileManger {
    private static FileManger fileManger;
    private static Context context;
    private  static ContentResolver contentResolver;
    private static Object lock=new Object();
    public FileManger() {
    }
    public  static FileManger getInstance(Context mcontext){
        if(fileManger==null){
            synchronized (lock){
                fileManger=new FileManger();
                context=mcontext;
                contentResolver=mcontext.getContentResolver();
            }
        }
        return fileManger;
    }
    /**
     * 获取本机的视频列表
     */
     public  List<Video> getVideos(){
         List<Video> videos=new ArrayList<>();
         Cursor c=null;
         try{
         c=contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Video.Media.DEFAULT_SORT_ORDER);
         if(c.moveToFirst()){
                 do{
                     String path=c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                     if(!new File(path).exists()){
                         continue;
                     }
                     int id=c.getInt(c.getColumnIndex(MediaStore.Video.Media._ID));
                     String name=c.getString(c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                     String resoultion=c.getString(c.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                     long size=c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE));
                     long duration=c.getLong(c.getColumnIndex(MediaStore.Video.Media.DURATION));
                     long date=c.getLong(c.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
                     Video video=new Video(id,path,name,resoultion,size,date,duration);
                     videos.add(video);
                 }while (c.moveToNext());
             }
         }
         catch (Exception e){
             e.printStackTrace();
         }finally {
             if(c!=null){
                 c.close();
             }
         }
         return videos;
     }

    /**
     * 通过ID获取视频的缩略图
     * @param id
     * @return
     */
     public  Bitmap getVideoThumbnailById(int id){
         Bitmap bitmap=null;
         BitmapFactory.Options options=new BitmapFactory.Options();
         options.inDither=false;
         options.inPreferredConfig=Bitmap.Config.ARGB_8888;
         bitmap=MediaStore.Video.Thumbnails.getThumbnail(contentResolver,id,MediaStore.Images.Thumbnails.MICRO_KIND,options);
         return  bitmap;
     }
}
