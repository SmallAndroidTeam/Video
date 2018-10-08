package io.vov.vitamio.demo.saveDate;


import android.support.v4.app.Fragment;




/**
 * 用来存储收藏界面和SD卡界面
 * Created by MR.XIE on 2018/9/16.
 */
public class SaveCollectFragment {
    private static Fragment collectFragment=null;
    private static Fragment sdCardFragment=null;
    public static void setCollectFragment(Fragment Fragment) {
        collectFragment = Fragment;
    }

    public static Fragment getCollectFragment(){
   return collectFragment;
    }

    public static void setSdCardFragment(Fragment Fragment) {
        sdCardFragment = Fragment;
    }

    public static Fragment getSdCardFragment(){
        return sdCardFragment;
    }
}
