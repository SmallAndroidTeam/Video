package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.vov.vitamio.toast.oneToast;

public class NetWorkChangeReceiver extends BroadcastReceiver {
     private NetWorkChange netWorkChange;

    public void setNetWorkChange(NetWorkChange netWorkChange) {
        this.netWorkChange = netWorkChange;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isAvailable()){//当前网络可用
            if(netWorkChange!=null){
                netWorkChange.netAvailable();
            }
        }else{
            if(netWorkChange!=null){
                netWorkChange.netUnAvailable();
            }
        }

    }
    public interface NetWorkChange{
        void netAvailable();//网络可用
        void netUnAvailable();//网络不可用
    }

}
