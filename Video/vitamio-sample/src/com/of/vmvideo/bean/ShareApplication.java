package com.of.vmvideo.bean;

/**
 * Created by MR.XIE on 2018/9/27.
 * 分享的应用
 */
public class ShareApplication {
    private int share_image_id;//应用图标的资源ID
    private String share_name;//应用图标的名称

    public ShareApplication(int share_image_id, String share_name) {
        this.share_image_id = share_image_id;
        this.share_name = share_name;
    }

    public int getShare_image_id() {
        return share_image_id;
    }

    public void setShare_image_id(int share_image_id) {
        this.share_image_id = share_image_id;
    }

    public String getShare_name() {
        return share_name;
    }

    public void setShare_name(String share_name) {
        this.share_name = share_name;
    }

    @Override
    public String toString() {
        return "ShareApplication{" +
                "share_image_id=" + share_image_id +
                ", share_name='" + share_name + '\'' +
                '}';
    }
}
