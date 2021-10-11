package com.wei.music.helper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.wei.music.R;
import com.wei.music.activity.MainActivity;

public class NotifyHelper {
    
    private static NotifyHelper mInstance;
    private Context mContext;
    
    public static NotifyHelper getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new NotifyHelper(context);
        }
        return mInstance;
    }
    
    public NotifyHelper(Context context) {
        this.mContext = context;
    }
    


    public void CreateChannel(String channel_id,CharSequence channel_name,String description) {
        //8.0以上版本通知适配
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    
    public Notification createForeNotification(String channel_id, RemoteViews remoteViews){
        Intent intent=new Intent(mContext, MainActivity.class);
        PendingIntent mainIntent=PendingIntent.getActivity(mContext,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(mContext,channel_id)
            .setSmallIcon(R.drawable.ic_more)
            .setStyle(new NotificationCompat.DecoratedCustomViewStyle()) 
            .setCustomBigContentView(remoteViews)
            .setContentIntent(mainIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }
}
