package activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import fragment.CollectFragment;
import fragment.LocalFragment;
import fragment.OnlineFragment;
import fragment.PersonalCentreFragment;
import getWidthOrHeight.Property;
import io.vov.vitamio.demo.R;
import saveDate.SaveCollectFragment;
import toast.oneToast;

public class MainActivity extends FragmentActivity implements View.OnClickListener {


    private LinearLayout localLayout;
    private ImageView localImageView;
    private TextView localTextView;
    private LinearLayout onlineLayout;
    private ImageView onlineImageView;
    private TextView onlineTextView;
    private LinearLayout collectLayout;
    private ImageView collectImageView;
    private TextView collectTextView;
    private LinearLayout personalCentreLayout;
    private ImageView personalCentreImageView;
    private TextView personalCentreTextView;
    private Fragment localFragment,onlineFragment,collectFragment,personalCentreFragment;
    private FrameLayout mainFragment;
    private LinearLayout mainLinearLayout;
    public static final String TAG="movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //无标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //全屏
//       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        ininView();

        initClickListener();
        seletTab(0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            initPosition();//初始化位置等属性
        }

    }

    private void initClickListener() {
        localLayout.setOnClickListener(this);
        onlineLayout.setOnClickListener(this);
        collectLayout.setOnClickListener(this);
        personalCentreLayout.setOnClickListener(this);
    }

    private void ininView() {
        mainFragment = (FrameLayout)this.findViewById(R.id.mainFragment);
        mainLinearLayout = (LinearLayout)this.findViewById(R.id.mainLinearLayout);

        localLayout = (LinearLayout)this.findViewById(R.id.localLayout);
        localImageView = (ImageView)this.findViewById(R.id.localImageView);
        localTextView = (TextView)this.findViewById(R.id.localTextView);

        onlineLayout = (LinearLayout)this.findViewById(R.id.onlineLayout);
        onlineImageView = (ImageView)this.findViewById(R.id.onlineImageView);
        onlineTextView = (TextView)this.findViewById(R.id.onlineTextView);

        collectLayout = (LinearLayout)this.findViewById(R.id.collectLayout);
        collectImageView = (ImageView)this.findViewById(R.id.collectImageView);
        collectTextView = (TextView)this.findViewById(R.id.collectTextView);


        personalCentreLayout = (LinearLayout)this.findViewById(R.id.PersonalCentreLayout);
        personalCentreImageView = (ImageView)this.findViewById(R.id.PersonalCentreImageView);
        personalCentreTextView = (TextView)this.findViewById(R.id.PersonalCentreTextView);
    }
    private void initPosition() {
    int windowHeight= Property.getWindowHeight(this);
    int mainLinearLayoutHeight=Property.getLinearLayoutHeight(mainLinearLayout);
    //设置mainFragment的高度
        //Log.i(TAG, "initPosition: -----------"+windowHeight+"/"+mainLinearLayoutHeight);
        ViewGroup.LayoutParams layoutParams=(ViewGroup.LayoutParams)mainFragment.getLayoutParams();
        layoutParams.height=windowHeight-mainLinearLayoutHeight;
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.localLayout:
               seletTab(0);
               break;
           case R.id.onlineLayout:
               seletTab(1);
               break;
           case R.id.collectLayout:
               seletTab(2);
               break;
           case R.id.PersonalCentreLayout:
               seletTab(3);
               break;
               default:
                   break;
       }
    }
    private void seletTab(int i){

        oneToast.showMessage(this,""+i);
        final FragmentManager fragmentManager=getSupportFragmentManager();
         FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
       switch (i){
           case 0:
               if(localFragment==null){
                   localFragment=new LocalFragment();
                   fragmentTransaction.add(R.id.mainFragment,localFragment);
               }else{
                   fragmentTransaction.show(localFragment);
               }
               break;
           case 1:
               if(onlineFragment==null){
                   onlineFragment=new OnlineFragment();
                   fragmentTransaction.add(R.id.mainFragment,onlineFragment);
               }else{
                   fragmentTransaction.show(onlineFragment);
               }
               break;
           case 2:
           if(collectFragment==null){
                collectFragment=new CollectFragment();
                fragmentTransaction.add(R.id.mainFragment,collectFragment);
           }else{
               fragmentTransaction.show(collectFragment);
           }
           SaveCollectFragment.setCollectFragment(collectFragment);
           break;
           case 3:
               if(personalCentreFragment==null){
                   personalCentreFragment=new PersonalCentreFragment();
                   fragmentTransaction.add(R.id.mainFragment,personalCentreFragment);
               }else{
                   fragmentTransaction.show(personalCentreFragment);
               }
               break;
               default:
                   break;

       }
       fragmentTransaction.commit();
    }

    private  void hideFragment(FragmentTransaction fragmentTransaction){
        if(localFragment!=null)
        {fragmentTransaction.hide(localFragment);
        }
        if(onlineFragment!=null)
        {fragmentTransaction.hide(onlineFragment);
        }
        if(collectFragment!=null)
        {fragmentTransaction.hide(collectFragment);
        }
        if(personalCentreFragment!=null)
        {fragmentTransaction.hide(personalCentreFragment);
        }
    }
}
