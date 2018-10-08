package io.vov.vitamio.demo.localData;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.utils.VideoDownload;


/**
 * Created by MR.XIE on 2018/9/8.
 * 获取本机的各种文件
 */
public class FileManger {
    private static FileManger fileManger;
    private static Context context;
    private  static ContentResolver contentResolver;
    private static Object lock=new Object();
    private final  static String[] supportVideoType={
            "mp4","3gp","mkv","mov","f4v","rmvb","3g2","wmv","vob","avi","mpg","flv","gif"
    };//支持的所有的视频格式
    private final  static String TAG="movie1";
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
     * 从本机数据库中获取本机的视频列表
     */
     public  List<Video> getVideos(){
         List<Video> videos=new ArrayList<>();
         Cursor c=null;
         try{

         c=contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Video.Media.DATE_TAKEN);
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
             //获取指定目录下的视频文件，参数是从本地数据库中获取的视频列表，如果指定目录下的视频文件存在就不添加
            videos=getSpecifyDirectoryVideos(videos);
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


     //获取指定目录下的视频文件，参数是从本地数据库中获取的视频列表，如果指定目录下的视频文件存在就不添加
     public List<Video> getSpecifyDirectoryVideos(List<Video> videos){
         List<Video> needAcquireVideoList;//需要获取的视频列表
         needAcquireVideoList = videos;
         String videoPrefix= VideoDownload.initVideoPathPrefix();
         Log.i(TAG, "存储的路径为："+videoPrefix);
         File file=new File(videoPrefix);
         if(file.exists()){
           File[] files=file.listFiles();
             Log.i(TAG, "存储路径下的文件数量："+files.length);
           for(File file1:files){
               //如果为可支持的视频文件，且视频不存在列表中
            Log.i(TAG, "getSpecifyDirectoryVideos: "+file1.getName());
               if(file1.isFile()&&isVideo(file1)&&!isExistsVideoList(videos,file1.getAbsolutePath())){
               Log.i(TAG, "不存在列表中的视频文件: "+file1.getName());
                          Video video=new Video();
                          video.setVideoPath(file1.getAbsolutePath());
                          video.setVideoName(file1.getName());
                          video.setSize(getSize(file1.getAbsolutePath()));
                          video.setDate(getDate(file1.getAbsolutePath()));
                          video.setDuration(getVideoDuration(file1.getAbsolutePath()));
                       //  video.setThumbnail(getVideoThumbnail(file1.getAbsolutePath(),200,200,MediaStore.Images.Thumbnails.MICRO_KIND));
                         video.setThumbnail(getVideoThumbnailThree(file1.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
                        // video.setThumbnail(getVideoThumbnailTwo(file1.getAbsolutePath()));
                         //  Log.i(TAG, "getSpecifyDirectoryVideos: 31");
                          needAcquireVideoList.add(video);
                // Log.i(TAG, "getSpecifyDirectoryVideos: 4");
               }else{//如果是目录
     List<File> fileList=getAllFiles(videos,file1);//获取一个目录下的所有可支持的视频文件,且视频不存在列表中
                   if(fileList==null){
                       continue;
                   }
                   Log.i(TAG, file1.getAbsolutePath()+"    目录下的所有可支持的视频文件,且视频不存在列表中的视频文件的总个数为："+fileList.size());
                   for(File file2:fileList){
                       Log.i(TAG, file1.getName()+"目录下中的视频文件为："+file2.getName());
                       Video video=new Video();
                       video.setVideoPath(file2.getAbsolutePath());
                       video.setVideoName(file2.getName());
                       video.setSize(getSize(file2.getAbsolutePath()));
                       video.setDate(getDate(file2.getAbsolutePath()));
                       video.setDuration(getVideoDuration(file2.getAbsolutePath()));
                      // video.setThumbnail(getVideoThumbnailTwo(file2.getAbsolutePath()));
                      //video.setThumbnail(getVideoThumbnail(file2.getAbsolutePath(),200,200,MediaStore.Images.Thumbnails.MICRO_KIND));
                        video.setThumbnail(getVideoThumbnailThree(file2.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
                         needAcquireVideoList.add(video);
                   }

               }
           }

         }
             return needAcquireVideoList;
     }

     public List<File> getAllFiles(List<Video> videos,File file){//获取一个目录下的所有可支持的视频文件,且视频不存在列表中
       if(!file.exists()||file.isFile()){
           return null;
       }
        List<File> files=new ArrayList<>();
        File[] files1=file.listFiles();
         Log.i(TAG, "getAllFiles: "+files1.length);
        for(File file1:files1){
            //Log.i(TAG, "getAllFiles: 目录下的视频文件"+file1.getName());
            if(file1.isFile()&&isVideo(file1)&&!isExistsVideoList(videos,file1.getAbsolutePath())){//如果为可支持的视频文件，且视频不存在列表中
                files.add(file1);
            }else if(file1.isDirectory()){
                List<File> fileList=getAllFiles(videos,file1);
                if(fileList!=null)
                files.addAll(fileList);
            }
        }
        return files;
     }



     //判断视频是否已存在列表中
     public boolean isExistsVideoList(List<Video> videos,String videoAbsolutePath){
         for(Video video:videos){
             if(video.getVideoPath().contentEquals(videoAbsolutePath)){
                 return true;
             }
         }
         return false;
     }



     //判断是否为可支持的视频
     public static boolean isVideo(File file){
         if(file.exists()){
             String videoSuffix=VideoDownload.getVideoSuffix(file.getName());
             if(videoSuffix==null){
                 return false;
             }else{
               for(String videoType:supportVideoType){
                   if(videoSuffix.contentEquals(videoType)){
                       return true;
                   }
               }
             }
         }
         return false;
     }

     //判断路径是否为可支持的视频路径
     public static boolean isVideoPath(String path){
    for(String videoType:supportVideoType){
        if(path.endsWith(videoType)){
            return true;
        }
    }
    return false;
     }

     //通过绝对路径判断视频文件是否存在{
    public static boolean isExists(String videoPath){
         if(new File(videoPath).exists()){
             return  true;
         }else{
             return false;
         }
    }


    public String getVideoName(String videoPath){
         return videoPath.substring(videoPath.lastIndexOf("/")+1,videoPath.length());
    }



    //获取视频的缩略图(方法3)
    public static Bitmap getVideoThumbnailThree(String videoPath,int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap == null) return null;
        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                    200,200
                    ,
                   ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;

    }


     //获取视频的缩略图
     public static Bitmap getVideoThumbnail(String videoPath,int width,int height,int kind){
     Bitmap bitmap=null;
       //  Log.i(TAG, "getVideoThumbnailTwo: 11");
     bitmap= ThumbnailUtils.createVideoThumbnail(videoPath,kind);
        // Log.i(TAG, "getVideoThumbnailTwo: 22");
     bitmap=ThumbnailUtils.extractThumbnail(bitmap,width,height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        // Log.i(TAG, "getVideoThumbnailTwo: 33");
     return bitmap;
     }

    //获取视频的缩略图(方法2)
    public static Bitmap getVideoThumbnailTwo(String videoPath){
        Log.i(TAG, "getVideoThumbnailTwo: 11");
        MediaMetadataRetriever media=new MediaMetadataRetriever();
        Log.i(TAG, "getVideoThumbnailTwo: 22");
        media.setDataSource(videoPath);
        Log.i(TAG, "getVideoThumbnailTwo: 33");
        return media.getFrameAtTime();

    }


     //获取视频的时长
    public  static long  getVideoDuration(String videoPath){
         long time=0;
        //Log.i(TAG, "getVideoDuration: 11");
        android.media.MediaPlayer mediaPlayer=new android.media.MediaPlayer();
      //  Log.i(TAG, "getVideoDuration: 22");
        try {
            mediaPlayer.setDataSource(videoPath);
          //  Log.i(TAG, "getVideoDuration:33");
            mediaPlayer.prepare();
          //  Log.i(TAG, "getVideoDuration:44");
            time=mediaPlayer.getDuration();
           // Log.i(TAG, "getVideoDuration:55");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.i(TAG, "getVideoDuration:66"+time);
        return time;
    }

    public  static  long getDate(String videoPath){
         File file=new File(videoPath);
             return file.lastModified();
    }

    public  static  long getSize(String videoPath){
        File file=new File(videoPath);
            return file.length();
    }
}
