package io.vov.vitamio.demo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.vov.vitamio.demo.R;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class PersonalCentreFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.personal_centre_fragment,container,false);
        return  view;
    }
}
