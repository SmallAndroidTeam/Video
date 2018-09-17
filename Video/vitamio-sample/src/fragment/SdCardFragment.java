package fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import activity.PlayActivity;
import adapter.VideoAdapter;
import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import localData.FileManger;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class SdCardFragment extends Fragment {

    private ListView videoListView;
    private final  String TAG="movie";
    private VideoAdapter videoApaper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sd_card_fragment,container,false);
        initView(view);
        videoApaper = new VideoAdapter(FileManger.getInstance(container.getContext()).getVideos(),container.getContext());
        videoListView.setAdapter(videoApaper);
        initOnclickListener();//设置点击监听事件
        return  view;
    }

    private void initOnclickListener() {
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<Video> videoList= videoApaper.getVideoList();
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",i);
                startActivity(intent);
               // oneToast.showMessage(getActivity(),videoList.get(i).getVideoPath());

            }
        });
    }

    private void initView(View view) {
        videoListView = (ListView)view.findViewById(R.id.videoListView);
    }

    //删除一个不存在的视频更新视图
   public void deleteOneVideoUpateView(int position, String path){
       List<Video> videoList= videoApaper.getVideoList();
       if(position>=videoList.size()||position<0)
       {
           return;
       }
       if(videoList.get(position).getVideoPath().equals(path)){
           videoList.remove(position);
           videoApaper.setVideoList(videoList);
           videoApaper.notifyDataSetChanged();
       }
   }
}
