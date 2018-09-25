package activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;


import broadcast.NetWorkChangeReceiver;
import fragment.CollectFragment;
import fragment.LocalFragment;
import fragment.DirectBroadFragment;
import fragment.OnlineFragment;
import fragment.PersonalCentreFragment;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.toast.oneToast;
import saveDate.SaveCollectFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    private Fragment localFragment,onlineFragment,collectFragment,personalCentreFragment,directBroadFragment;
    public static final String TAG="movie";
    private RadioButton rb_localvideo;
    private RadioButton rb_netvideo;
    private RadioButton rb_netdirectbroad;
    private RadioButton rb_collectvideo;
    private RadioButton rb_personalcentre;
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
        rb_localvideo.setChecked(true);

        seletTab(0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initClickListener() {
        rb_localvideo.setOnClickListener(this);
        rb_netvideo.setOnClickListener(this);
        rb_netdirectbroad.setOnClickListener(this);
        rb_collectvideo.setOnClickListener(this);
        rb_personalcentre.setOnClickListener(this);

    }

    private void ininView() {
        rb_localvideo = (RadioButton)this.findViewById(R.id.rb_localvideo);
        rb_netvideo = (RadioButton)this.findViewById(R.id.rb_netvideo);
        rb_netdirectbroad = (RadioButton)this.findViewById(R.id.rb_netdirectbroad);
        rb_collectvideo = (RadioButton)this.findViewById(R.id.rb_collectvideo);
        rb_personalcentre = (RadioButton)this.findViewById(R.id.rb_personalcentre);
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.rb_localvideo:
               seletTab(0);
               break;
           case R.id.rb_netvideo:
               seletTab(1);
               break;
           case R.id.rb_netdirectbroad:
               seletTab(2);
               break;
           case R.id.rb_collectvideo:
               seletTab(3);
               break;
           case R.id.rb_personalcentre:
               seletTab(4);
               break;
               default:
                   break;
       }
    }
    private void seletTab(int i){

     //   oneToast.showMessage(this,""+i);
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
               if(directBroadFragment==null){
                   directBroadFragment=new DirectBroadFragment();
                   fragmentTransaction.add(R.id.mainFragment,directBroadFragment);
               }else{
                   fragmentTransaction.show(directBroadFragment);
               }
               break;
           case 3:
           if(collectFragment==null){
                collectFragment=new CollectFragment();
                fragmentTransaction.add(R.id.mainFragment,collectFragment);
           }else{
               fragmentTransaction.show(collectFragment);
           }
           SaveCollectFragment.setCollectFragment(collectFragment);
           break;
           case 4:
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
        if(directBroadFragment!=null)
        {fragmentTransaction.hide(directBroadFragment);
        }
        if(collectFragment!=null)
        {fragmentTransaction.hide(collectFragment);
        }
        if(personalCentreFragment!=null)
        {fragmentTransaction.hide(personalCentreFragment);
        }
    }


}
