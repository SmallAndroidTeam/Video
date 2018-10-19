package io.vov.vitamio.demo.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by MR.XIE on 2018/9/21.
 * 视频下载
 */
public class VideoDownload {
    private  static String videoPathPrefix;
    private Context mContext;
    private String videoPath;
    private   FileOutputStream writer;
    private String videoAbsolutePath;
    public VideoDownload(Context context,String videoPath) {
        this.mContext=context;
        this.videoPath = videoPath;
        try {
            videoAbsolutePath=videoPathPrefix+videoPath;
            writer=new FileOutputStream(videoAbsolutePath,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVideoPathPrefix() {
        return videoPathPrefix;
    }

    public String getVideoAbsolutePath() {
        return videoAbsolutePath;
    }



    //继续下载执行的构造函数
    public VideoDownload(Context context,String videoAbsolutePath,boolean isAppend){
        this.mContext=context;
        this.videoAbsolutePath=videoAbsolutePath;
        try {
            writer=new FileOutputStream(videoAbsolutePath,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    //保存视频的每一行直到结束
     public boolean saveVideoByLine(byte[] line,int readCount){
           if(writer!=null){
               try {
                   writer.write(line,0,readCount);
                   return true;
               } catch (IOException e) {
                   e.printStackTrace();
                       try {
                           writer.close();
                       } catch (IOException e1) {
                           e1.printStackTrace();
                       }
                   return false;
               }
           }else{
               return false;
           }
     }






     //关闭写操作流
     public void closeWriter(){
         if(writer!=null){
             try {
                 writer.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
    public static String   initVideoPathPrefix(){

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//sd卡是否存在
            videoPathPrefix= Environment.getExternalStorageDirectory().getAbsolutePath();//sd卡存储路径
        }else{
            videoPathPrefix=Environment.getExternalStoragePublicDirectory("").getAbsolutePath();//本地存储路径
        }
        videoPathPrefix+="/Movies/";
        if(!new File(videoPathPrefix).exists()){
            new File(videoPathPrefix).mkdirs();
        }
        Log.i("movie1", "下载路径的前缀: "+videoPathPrefix);
        return videoPathPrefix;

    }

     //获取一个不存在的视频名称
     public  static  String getNoExistVideoPath(String address,String path){
         initVideoPathPrefix();
        if(path==null||address==null){
            return null;
        }
         String SuffixType=getVideoSuffix(address);
        if(SuffixType==null){//没后缀
            return null;
        }
        if(isExist(path+"."+SuffixType)){
            int index=1;

            String VideoName=getVideoNameExceptSuffix(path);

            if(VideoName==null){
                return null;
            }
            while (isExist(VideoName+index+"."+SuffixType))
            {
                index++;
            }
              return (VideoName+index+"."+SuffixType);

        }else{
            return path+"."+SuffixType;
        }

     }

     //判断文件路径是否存在,如果不存在就创建一个
     public static boolean  isExist(String path) {
        if(path==null){
            return false;
         }
        if(new File(videoPathPrefix+path).exists()){
            return true;
         }else{
            try {
                new File(videoPathPrefix+path).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("movie", videoPathPrefix+path+": 创建失败");
                return true;
            }
            return false;
         }
     }
     //获取一个视频文件的后缀
     public static String  getVideoSuffix(String path){
         String Suffix=null;
         if(path==null)
         {
             return Suffix;
         }
         int index=path.lastIndexOf(".");
         if(index==-1||(index+1>=path.length())){
             return null;
         }else{
             Suffix=path.substring(index+1,path.length()).toLowerCase();
             return Suffix;
         }
     }
     //获取一个视频文件去掉后缀格式的名字
     public static String  getVideoNameExceptSuffix(String path){
         String videoName=null;
        if(path==null)
        {
            return videoName;
        }
        int index=path.lastIndexOf(".");
        index=(index==-1)?path.length():index;
        videoName=path.substring(0,index);
        return videoName;
     }
}
