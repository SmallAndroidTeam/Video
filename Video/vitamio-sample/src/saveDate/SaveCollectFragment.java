package saveDate;

import android.support.v4.app.Fragment;

import fragment.CollectFragment;


/**
 * 用来存储收藏界面
 * Created by MR.XIE on 2018/9/16.
 */
public class SaveCollectFragment {
    private static Fragment collectFragment=null;

    public static void setCollectFragment(Fragment Fragment) {
        collectFragment = Fragment;
    }

    public static Fragment getCollectFragment(){
   return collectFragment;
    }
}
