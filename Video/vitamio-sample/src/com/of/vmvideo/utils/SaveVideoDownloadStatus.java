package com.of.vmvideo.utils;

import android.app.NotificationManager;
import android.content.Context;

import org.w3c.dom.ls.LSInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MR.XIE on 2018/10/15.
 * 用来保存所有视频下载的状态
 */
public class SaveVideoDownloadStatus {

    private Context context=null;
    private int position=0;//视频的下标
    private  String downLoadVideoAddress=null;//视频下载的地址
    private String  saveDownLoadVideoAddress=null;//保存视频的地址
    private String videoName=null;//视频名称
    private long downByteNumber=0;//当前下载的字符总个数
    private long videoSize=0;//视频总的字符个数
    private int currentID=100;//通知的ID
    private String DownLoadSavePath=null;//保存所有下载的视频地址
    private HttpUtil.LocalPlay localPlay=null;
    private String coverImg=null;//视频的图片
    private static List<SaveVideoDownloadStatus> videoDownloadStatusList=new ArrayList<>();
    private boolean isDownLoading=false;//是否正在下载
        public SaveVideoDownloadStatus() {
    }


    public SaveVideoDownloadStatus(Context context, int position, String downLoadVideoAddress, String saveDownLoadVideoAddress, String videoName, long downByteNumber, long videoSize, int currentID, String downLoadSavePath, HttpUtil.LocalPlay localPlay, String coverImg) {
        this.context = context;
        this.position = position;
        this.downLoadVideoAddress = downLoadVideoAddress;
        this.saveDownLoadVideoAddress = saveDownLoadVideoAddress;
        this.videoName = videoName;
        this.downByteNumber = downByteNumber;
        this.videoSize = videoSize;
        this.currentID = currentID;
        DownLoadSavePath = downLoadSavePath;
        this.localPlay = localPlay;
        this.coverImg = coverImg;
    }



    public boolean isDownLoading() {
        return isDownLoading;
    }

    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }

    //通过通知的Id删除一个下载视频的状态
    public  static void deleteOneDownLoadStatusByCurrentID(int currentID){

        for(SaveVideoDownloadStatus saveVideoDownloadStatus:videoDownloadStatusList){
            if(saveVideoDownloadStatus.getCurrentID()==currentID){
                videoDownloadStatusList.remove(saveVideoDownloadStatus);
                break;
            }
        }
    }
    //通过下载地址删除一个暂停的视频
    public static void deleteDownloadVideoByAddress(String address){
      for(SaveVideoDownloadStatus saveVideoDownloadStatus:videoDownloadStatusList){
          if(saveVideoDownloadStatus.getDownLoadVideoAddress().trim().contentEquals(address)){
              videoDownloadStatusList.remove(saveVideoDownloadStatus);
              break;
          }
      }

    }
    //通过视频地址获取当前在暂停列表中的id
    public static int getDownloadIndexByVideoAddress(String address){
          for(int i=0;i<videoDownloadStatusList.size();i++){
              SaveVideoDownloadStatus saveVideoDownloadStatus=videoDownloadStatusList.get(i);
              if(saveVideoDownloadStatus.getDownLoadVideoAddress().trim().contentEquals(address)){
                  return i;
              }
          }
          return -1;
    }

  //通过Id设置暂停视频的下载状态
    public synchronized static void setDownloadStatus(int index){
            if(index<0||index>=videoDownloadStatusList.size()){
                return;
            }
            SaveVideoDownloadStatus saveVideoDownloadStatus=videoDownloadStatusList.get(index);
            saveVideoDownloadStatus.setDownLoading(true);
           videoDownloadStatusList.set(index,saveVideoDownloadStatus);
    }


    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public HttpUtil.LocalPlay getLocalPlay() {
        return localPlay;
    }

    public void setLocalPlay(HttpUtil.LocalPlay localPlay) {
        this.localPlay = localPlay;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDownLoadVideoAddress() {
        return downLoadVideoAddress;
    }

    public void setDownLoadVideoAddress(String downLoadVideoAddress) {
        this.downLoadVideoAddress = downLoadVideoAddress;
    }

    public String getSaveDownLoadVideoAddress() {
        return saveDownLoadVideoAddress;
    }

    public void setSaveDownLoadVideoAddress(String saveDownLoadVideoAddress) {
        saveDownLoadVideoAddress = saveDownLoadVideoAddress;
    }

    public long getDownByteNumber() {
        return downByteNumber;
    }

    public void setDownByteNumber(long downByteNumber) {
        this.downByteNumber = downByteNumber;
    }

    public long getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(long videoSize) {
        this.videoSize = videoSize;
    }

    public int getCurrentID() {
        return currentID;
    }

    public void setCurrentID(int currentID) {
        this.currentID = currentID;
    }

    public String getDownLoadSavePath() {
        return DownLoadSavePath;
    }

    public void setDownLoadSavePath(String downLoadSavePath) {
        DownLoadSavePath = downLoadSavePath;
    }

    public static List<SaveVideoDownloadStatus> getVideoDownloadStatusList() {
        return videoDownloadStatusList;
    }

    public static void setVideoDownloadStatusList(List<SaveVideoDownloadStatus> videoDownloadStatusList) {
        SaveVideoDownloadStatus.videoDownloadStatusList = videoDownloadStatusList;
    }

    @Override
    public String toString() {
        return "SaveVideoDownloadStatus{" +
                "context=" + context +
                ", position=" + position +
                ", downLoadVideoAddress='" + downLoadVideoAddress + '\'' +
                ", SaveDownLoadVideoAddress='" + saveDownLoadVideoAddress + '\'' +
                ", downByteNumber=" + downByteNumber +
                ", videoSize=" + videoSize +
                ", currentID=" + currentID +
                ", DownLoadSavePath='" + DownLoadSavePath + '\'' +
                '}';
    }
}
