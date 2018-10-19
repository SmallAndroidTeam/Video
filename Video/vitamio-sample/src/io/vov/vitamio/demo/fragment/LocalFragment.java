package io.vov.vitamio.demo.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.saveDate.SaveCollectFragment;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class LocalFragment extends Fragment implements View.OnClickListener {
    private final String[] titles={"U盘","本地存储"};
    private ViewPager viewPager;
    private List<Fragment> fragmentList=new ArrayList<>();
    private TableLayout underline;
    private TextView uDiskView;
    private TextView sdCardView;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view=inflater.inflate(R.layout.local_fragment,container,false);
         initView(view);
         initPosition();//初始化位置
        fragmentList.add(new UDiskFragment());
        SdCardFragment sdCardFragment= new SdCardFragment();
        fragmentList.add(sdCardFragment);
        SaveCollectFragment.setSdCardFragment(sdCardFragment);
        FragmentPagerAdapter fragmentPagerAdapter=new fragmentAdapter(getFragmentManager(),fragmentList);
        viewPager.setOffscreenPageLimit(2);//ViewPager的缓存为帧
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(1);
        setListener();
       return  view;
    }

    public List<Fragment> getFragmentList() {
        return fragmentList;
    }

    private void initPosition() {
        setOneTabPosition(1);
       //Log.i(TAG, "initPosition: --------------"+windowWidth+"//"+controlWidth);
    }

   //设置下划线的位置,i为tab的下标

    private void setOneTabPosition(int i){
        if(i==0){
//            int windowWidth= Property.getWindowWidth(this.getActivity());
//            int controlWidth=Property.getTableLayoutWidth(underline);
//            LinearLayout.LayoutParams underlineLayoutParams= (LinearLayout.LayoutParams) underline.getLayoutParams();
//            underlineLayoutParams.leftMargin=(windowWidth/2-controlWidth)/2;
//            underline.setLayoutParams(underlineLayoutParams);
            uDiskView.setTextColor(getResources().getColor(R.color.indexTextSelect));
            sdCardView.setTextColor(getResources().getColor(R.color.indexText));

        }else if(i==1){
            uDiskView.setTextColor(getResources().getColor(R.color.indexText));
            sdCardView.setTextColor(getResources().getColor(R.color.indexTextSelect));
//            int windowWidth= Property.getWindowWidth(this.getActivity());
//            int controlWidth=Property.getTableLayoutWidth(underline);
//            LinearLayout.LayoutParams underlineLayoutParams= (LinearLayout.LayoutParams) underline.getLayoutParams();
//            underlineLayoutParams.leftMargin=windowWidth/2+(windowWidth/2-controlWidth)/2;
//            underline.setLayoutParams(underlineLayoutParams);
        }
    }

    private class fragmentAdapter extends FragmentPagerAdapter{
          List<Fragment> fragments;

         public fragmentAdapter(FragmentManager fm,List<Fragment> fragments) {
             super(fm);
             this.fragments=fragments;
         }

         @Override
         public android.support.v4.app.Fragment getItem(int position) {
             return fragments.get(position);
         }

         @Override
         public int getCount() {
             return fragments.size();
         }
     }
    private void setListener() {
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        uDiskView.setOnClickListener(this);
        sdCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
     switch (view.getId()){
         case R.id.uDisk:
             viewPager.setCurrentItem(0);
             setOneTabPosition(0);
             break;
         case R.id.sdCard:
             viewPager.setCurrentItem(1);
             ((SdCardFragment)fragmentList.get(1)).UpdateView();
             setOneTabPosition(1);
             break;
             default:
                 break;
     }
    }


    private void initView(View view) {
        viewPager = (ViewPager)view.findViewById(R.id.viewPager);
        underline = (TableLayout)view.findViewById(R.id.Underline);
        uDiskView = (TextView)view.findViewById(R.id.uDisk);
        sdCardView = (TextView)view.findViewById(R.id.sdCard);
    }

}
