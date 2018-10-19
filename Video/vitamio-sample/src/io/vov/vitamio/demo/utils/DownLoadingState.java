package io.vov.vitamio.demo.utils;

/**
 * Created by MR.XIE on 2018/10/18.
 * 正在下载的视频的下载状态
 */
public class DownLoadingState {
    private  String downLoadVideoAddress=null;//视频下载的地址
    private String  downLoadVideoName=null;//视频下载的名称
    private String coverImg=null;//视频的缩略图地址
    private long downByteNumber=0;//当前下载的字符总个数
    private long videoSize=0;//视频总的字符个数
    private int currentState=0;//当前的下载状态 （0是下载，1是暂停,2是删除下载）
    private int targetState=0;//目标的下载状态
    private boolean isDownComplete=false;//是否下载完成（下载失败也叫完成)

    public DownLoadingState() {
    }

    public DownLoadingState(String downLoadVideoAddress, String downLoadVideoName, String coverImg, int currentState, int targetState) {
        this.downLoadVideoAddress = downLoadVideoAddress;
        this.downLoadVideoName = downLoadVideoName;
        this.coverImg = coverImg;
        this.currentState = currentState;
        this.targetState = targetState;
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

    public String getDownLoadVideoName() {
        return downLoadVideoName;
    }

    public void setDownLoadVideoName(String downLoadVideoName) {
        this.downLoadVideoName = downLoadVideoName;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public boolean isDownComplete() {
        return isDownComplete;
    }

    public void setDownComplete(boolean downComplete) {
        isDownComplete = downComplete;
    }

    public String getDownLoadVideoAddress() {
        return downLoadVideoAddress;
    }

    public void setDownLoadVideoAddress(String downLoadVideoAddress) {
        this.downLoadVideoAddress = downLoadVideoAddress;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getTargetState() {
        return targetState;
    }

    public void setTargetState(int targetState) {
        this.targetState = targetState;
    }

    @Override
    public String toString() {
        return "DownLoadingState{" +
                "downLoadVideoAddress='" + downLoadVideoAddress + '\'' +
                ", currentState=" + currentState +
                ", targetState=" + targetState +
                '}';
    }
}
