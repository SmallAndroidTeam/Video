package io.vov.vitamio.bean;

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MR.XIE on 2018/9/8.
 * 视频的一些属性
 */
public class Video {
    private int vdieoId=0;
    private String videoPath=null;//播放路径(如果视频已下载则此为本地路径)
    private String videoName=null;//名称
    private String resolution=null;//分辨率
    private long size=0;//大小
    private long date=0;//添加时间
    private long duration=0;//时长
    private String thumbnailPath=null;//缩略图的网络地址
    private Bitmap Thumbnail=null;//缩略图
    private Integer progress=0;//播放进度(最大值为1000）
   private String networkVideoAddress=null;//如果是本地视频则为空

    public Video(String videoPath, String videoName, String resolution, long size, long date, long duration, Bitmap thumbnail, Integer progress) {
        this.videoPath = videoPath;
        this.videoName = videoName;
        this.resolution = resolution;
        this.size = size;
        this.date = date;
        this.duration = duration;
        Thumbnail = thumbnail;
        this.progress = progress;
    }

    public Video(int vdieoId, String videoPath, String videoName, String resolution, long size, long date, long duration) {
        this.vdieoId = vdieoId;
        this.videoPath = videoPath;
        this.videoName = videoName;
        this.resolution = resolution;
        this.size = size;
        this.date = date;
        this.duration = duration;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getNetworkVideoAddress() {
        return networkVideoAddress;
    }

    public void setNetworkVideoAddress(String networkVideoAddress) {
        this.networkVideoAddress = networkVideoAddress;
    }

    public Bitmap getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        Thumbnail = thumbnail;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Video() {
    }

    public int getVdieoId() {
        return vdieoId;
    }

    public void setVdieoId(int vdieoId) {
        this.vdieoId = vdieoId;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Video{" +
                "vdieoId=" + vdieoId +
                ", videoPath='" + videoPath + '\'' +
                ", videoName='" + videoName + '\'' +
                ", resolution='" + resolution + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", duration=" + duration +
                '}';
    }
    //将毫秒变为日期
    public static String ConvertDate(long duration){
        final   SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  format.format(duration);
    }
}
