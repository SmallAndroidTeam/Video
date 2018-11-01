package com.of.vmvideo.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.of.vmvideo.R;



/**
 * Created by MR.XIE on 2018/9/24.
 */
public class LinebroadcastaloneFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.linebroadcast_alone_fragment,container,false);
       return  view;
    }
}
