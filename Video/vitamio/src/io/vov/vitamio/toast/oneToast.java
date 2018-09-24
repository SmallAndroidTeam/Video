package io.vov.vitamio.toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class oneToast {
    private static Toast toast;
    @SuppressLint("ShowToast")
    public static void showMessage(Context context, String message){
        if(toast==null){
            toast=Toast.makeText(context,message,Toast.LENGTH_SHORT);
        }else{
            toast.cancel();
            toast=Toast.makeText(context,message,Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    public static void hideToast(){
        if(toast!=null) {
            toast.cancel();
        }
    }
}
