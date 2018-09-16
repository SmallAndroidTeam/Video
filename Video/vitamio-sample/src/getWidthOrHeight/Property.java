package getWidthOrHeight;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

/**
 * Created by MR.XIE on 2018/9/8.
 */
public class Property {
    public static int getWindowWidth(Context context){
        int width=((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
        return width;
    }
    public static int getWindowHeight(Context context){
        int height=((Activity)context).getWindowManager().getDefaultDisplay().getHeight();
        return height;
    }

    //获取控件的宽度
    public static int getControlWidth(View view){
     int w=View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
     int h=View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
     view.measure(w,h);
     return view.getMeasuredWidth();
    }

  //获取下划线的宽度
 public static int getTableLayoutWidth(TableLayout tableLayout){
      LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) tableLayout.getLayoutParams();
      return   layoutParams.width;
 }

 //获取主页中底部按钮布局的高度
 public static int getLinearLayoutHeight(LinearLayout linearLayout){
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
      return layoutParams.height;
 }

}
