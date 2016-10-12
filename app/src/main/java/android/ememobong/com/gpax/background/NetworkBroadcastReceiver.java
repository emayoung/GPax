package android.ememobong.com.gpax.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by ememobong on 19/09/2016.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    private Intent mServiceIntent;
    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null){
            if(networkInfo.isConnected()){
//                neetwork is available start the Notification Service
                mServiceIntent = new Intent(context, NotificationService.class);

                context.startService(mServiceIntent);

            }
            else  {
//                this method is  not called don't know why

                Intent intent1 = new Intent(context, NotificationService.class);
                context.stopService(intent1);

            }

        }
        else {
//                this method is not called don;t know why

            Intent intent1 = new Intent(context, NotificationService.class);
            context.stopService(intent1);
        }
    }
}
