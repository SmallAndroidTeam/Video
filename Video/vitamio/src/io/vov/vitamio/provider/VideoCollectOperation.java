package io.vov.vitamio.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.R;
import io.vov.vitamio.bean.Video;
import io.vov.vitamio.utils.CommonUtils;
import io.vov.vitamio.utils.Crypto;

/**
 * 视频收藏操作（数据库操作）
 * Created by MR.XIE on 2018/9/16.
 */
public class VideoCollectOperation {
    private Context context;
    private final static  String dbName="VidelCollect.db";//数据库的名称
    private final  static String tableName="VideoCollect";
    private final static  String videoDownTableName="VideoDownload";
    private VideoCollectDatabaseHelper videoCollectDatabaseHelper;
   private final static String TAG="movie4";
    public VideoCollectOperation(Context context) {
        this.context = context;
        this.videoCollectDatabaseHelper=new VideoCollectDatabaseHelper(context,dbName,null,1);
    }

    //存储下载的视频和保存的视频地址信息到数据库
    public synchronized boolean saveVideoDownload(String downloadVideoPath,String saveVideoPath){
        SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
         try{
             if(isExistsVideoSave(saveVideoPath))
             {
                 sqLiteDatabase.execSQL("update "+videoDownTableName+" set VIDEO_DOWNLOAD_PATH=? where VIDEO_SAVE_PATH=?",new String[]{downloadVideoPath,saveVideoPath});
             }else{
                 sqLiteDatabase.execSQL("insert into "+videoDownTableName+"(VIDEO_SAVE_PATH,VIDEO_DOWNLOAD_PATH) values (?,?)",new String[]{saveVideoPath,downloadVideoPath});
             }
              sqLiteDatabase.setTransactionSuccessful();
         }catch (Exception e){
             e.printStackTrace();
             return false;
         }finally {
                 sqLiteDatabase.endTransaction();
                 sqLiteDatabase.close();
         }
         return true;
    }

    //删除存储的地址
    public synchronized  void deleteSaveVideo(String saveVideoPath){
        SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try{
            sqLiteDatabase.execSQL("delete from "+videoDownTableName+" where VIDEO_SAVE_PATH=?",new String[]{saveVideoPath});
          sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e){
          e.printStackTrace();
        }finally {
          sqLiteDatabase.endTransaction();
          sqLiteDatabase.close();
        }
    }

    //判断存储的地址是否已存在下载表中
    public synchronized  boolean isExistsVideoSave(String saveVideoPath){
        SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        Cursor cursor=sqLiteDatabase.rawQuery("select * from "+videoDownTableName+" where VIDEO_SAVE_PATH=?",new String[]{saveVideoPath});
        if(cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    //判断下载的地址是否已存在下载表中
    public synchronized boolean isExistsVideoDownload(String downloadVideoPath){
        SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        Cursor cursor=sqLiteDatabase.rawQuery("select * from "+videoDownTableName+" where VIDEO_DOWNLOAD_PATH=?",new String[]{downloadVideoPath});
        if(cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }

    }

    //通过下载地址获取保存的地址的绝对路径
    public synchronized String getSavePathByDownloadPath(String downloadVideoPath){
        SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        Cursor cursor=sqLiteDatabase.rawQuery("select VIDEO_SAVE_PATH from "+videoDownTableName+" where VIDEO_DOWNLOAD_PATH=?",new String[]{downloadVideoPath});
        if(cursor.moveToFirst()){
           // Log.i("movie2", "getSavePathByDownloadPath: "+"存在");
            return cursor.getString(cursor.getColumnIndex("VIDEO_SAVE_PATH"));
        }else{
            return null;
        }
    }




    //存储收藏的视频信息到数据库
    public synchronized  boolean saveVideoCollect(Video video){
        final SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try{
            ContentValues values=new ContentValues();
            values.put("VIDEO_NAME",video.getVideoName());
            values.put("VIDEO_THUMBNAIL",bitmapToBytes(video.getThumbnail()));
            values.put("VIDEO_SIZE",(float)video.getSize());
            values.put("VIDEO_PROGRESS",video.getProgress());
            values.put("VIDEO_PATH",video.getVideoPath());
            values.put("VIDEO_RESOLUTION",video.getResolution());
            values.put("VIDEO_DATE",(float)video.getDate());
            values.put("VIDEO_DURATION",(float)video.getDuration());
            sqLiteDatabase.insert(tableName,null,values);
            sqLiteDatabase.setTransactionSuccessful();

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        finally {
                sqLiteDatabase.endTransaction();
                sqLiteDatabase.close();
        }

        return true;
    }

    //获取收藏的所有视频
    public synchronized  List<Video> getVideoCollect(){
        List<Video> videoList=new ArrayList<>();
        final  SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        Cursor cursor=sqLiteDatabase.rawQuery("select * from "+tableName+" order by VIDEO_DATE asc",null);
        if(cursor.moveToFirst()){
            do{
                 String videoPath=cursor.getString(cursor.getColumnIndex("VIDEO_PATH"));//路径
                if(!CommonUtils.isNetUrl(videoPath)) {//如果不是网络地址
                    if (!new File(videoPath).exists()) {//如果该路径不存在
                        cancelCollectVide(videoPath);
                        continue;
                    }
                }

                 String videoName=cursor.getString(cursor.getColumnIndex("VIDEO_NAME"));//名称
                 String resolution=cursor.getString(cursor.getColumnIndex("VIDEO_RESOLUTION"));//分辨率
                 long size= (long) cursor.getFloat(cursor.getColumnIndex("VIDEO_SIZE"));//大小
                 long date= (long) cursor.getFloat(cursor.getColumnIndex("VIDEO_DATE"));//添加时间
                 long duration= (long) cursor.getFloat(cursor.getColumnIndex("VIDEO_DURATION"));//时长
                 Bitmap Thumbnail=bytesToBitmap(cursor.getBlob(cursor.getColumnIndex("VIDEO_THUMBNAIL")));//缩略图
                 Integer progress=cursor.getInt(cursor.getColumnIndex("VIDEO_PROGRESS"));//播放进度(最大值为1000）
                //android.util.Log.i("movie2", "love:11 "+progress);
                Video video=new Video(videoPath,videoName,resolution,size,date,duration,Thumbnail,progress);
                videoList.add(video);
            }while (cursor.moveToNext());
        }
        return  videoList;
    }

   //判断视频是否已经收藏通过路径
   public synchronized boolean isExistsByPath(String path){

        final  SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
                 Cursor cursor=sqLiteDatabase.rawQuery("select * from "+tableName+" where VIDEO_PATH=?",new String[]{path});
                 if(cursor.moveToFirst()){
                     return true;
                 }else{
                     return false;
                 }
    }

 //取消收藏视频
 public synchronized boolean cancelCollectVide(String path){
        final  SQLiteDatabase sqLiteDatabase=videoCollectDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.execSQL("delete from "+tableName+" where VIDEO_PATH=?",new String[]{path});
            sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
     return true;
    }

    //把Bitmap图片转化为字节
    private static byte[] bitmapToBytes(Bitmap bitmap){
        if (bitmap == null) {
            return null;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 将Bitmap压缩成PNG编码，质量为100%存储
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);//除了PNG还有很多常见格式，如jpeg等。
        return os.toByteArray();
    }

    private static Bitmap bytesToBitmap(byte[] bytes){
        if(bytes==null)
            return null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        return bitmap;
    }
}
