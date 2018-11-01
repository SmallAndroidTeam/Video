package com.of.vmvideo.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.of.vmvideo.adapter.OnlineAdapter;
import com.of.vmvideo.linearLayoutManager.SmoothScrollLayoutManager;
import com.of.vmvideo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



/**
 * Created by MR.XIE on 2018/9/7.
 */
public class OnlineFragment extends Fragment {

    private RecyclerView onlineTitleRecyclerView;
   private final  static String TAG="movie2";
    private ViewPager onlineViewPager;
    private   List<Fragment> fragmentList=new ArrayList(){};
    private OnlineAdapter onlineAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.online_fragment,container,false);
       initView(view);
       onlineAdapter=new OnlineAdapter(getContext());
        onlineAdapter.setTitleName(onlineAdapter.getTitleList()[3]);//默认选中哪一个标题
        SmoothScrollLayoutManager linearLayoutManager=new SmoothScrollLayoutManager(this.getContext());//水平滚动
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        onlineTitleRecyclerView.setLayoutManager(linearLayoutManager);
        onlineTitleRecyclerView.setAdapter(onlineAdapter);
         fragmentList=new ArrayList<>();
        fragmentList.add(new LinebroadcastaloneFragment());
        fragmentList.add(new GreatDayFragment());
        fragmentList.add(new GuessYouLikeFragment());
        fragmentList.add(new FilmFragment());
        fragmentList.add(new HotTVFragment());
        fragmentList.add(new HotVarietyShowsFragment());
        fragmentList.add(new HotNewsFragment());

        FragmentPagerAdapter fragmentPagerAdapter=new fragmentAdapter(getFragmentManager(),fragmentList);
        onlineViewPager.setAdapter(fragmentPagerAdapter);
        onlineViewPager.setOffscreenPageLimit(2);

        onlineViewPager.setCurrentItem(3);

        addLinstener();
       return view;
    }


    //选择标题
    private void setOneTabPosition(final int i){
         if(i<fragmentList.size()&&i>=0){
            onlineAdapter.setTitleName(onlineAdapter.getTitleList()[i]);
            if(i==1){
                onlineTitleRecyclerView.smoothScrollToPosition(0);
            }else if(i==fragmentList.size()-2){
                onlineTitleRecyclerView.smoothScrollToPosition(fragmentList.size()-1);
            }


             final Timer timer=new Timer();
             timer.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     for(int j=0;j<onlineTitleRecyclerView.getChildCount();j++){
                         TextView titleView=onlineTitleRecyclerView.getChildAt(j).findViewById(R.id.titleView);
                         if(titleView.getText().toString().contentEquals(onlineAdapter.getTitleList()[i])){
                             titleView.setTextColor(getResources().getColor(R.color.indexTextSelect));
                             timer.cancel();
                         }else {
                             titleView.setTextColor(getResources().getColor(R.color.indexText));
                         }
                     }
                 }
             },100);

         }
    }



 private void addLinstener() {
       onlineAdapter.setonItemClickListener(new OnlineAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(View view, int position) {
               setOneTabPosition(position);
               onlineViewPager.setCurrentItem(position);
           }
       });
        onlineViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
             setOneTabPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
  /*此方法是在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2）。
               arg0==2的时辰默示滑动完毕了，
                arg0 ==1的时辰默示正在滑动，
                arg0==0的时辰默示什么都没做。*/
            }
        });
    }

     class fragmentAdapter extends FragmentPagerAdapter{
         private List<Fragment> fragmentList;

         public fragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
             super(fm);
             this.fragmentList = fragmentList;
         }

         @Override
         public Fragment getItem(int position) {
             return fragmentList.get(position);
         }

         @Override
         public int getCount() {
             return fragmentList.size();
         }
     }
    private void initView(View view) {
        onlineTitleRecyclerView = (RecyclerView)view.findViewById(R.id.onlineTitleRecyclerView);
        onlineViewPager = (ViewPager)view.findViewById(R.id.onlineViewPager);
    }
}
