package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import activity.DirectBroadActivity;
import io.vov.vitamio.demo.R;

/**
 * Created by MR.XIE on 2018/9/19.
 */
public class DirectBroadFragment extends Fragment {

    private ImageButton directBroadImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.directbroad_fragment,container,false);
        initView(view);
        setClickListener();
        return view;
    }

    private void setClickListener() {
        directBroadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), DirectBroadActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView(View view) {
        directBroadImageButton = (ImageButton)view.findViewById(R.id.directBroad_play_stop);

    }
}
