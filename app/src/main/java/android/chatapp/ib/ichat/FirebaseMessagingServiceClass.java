package android.chatapp.ib.ichat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ibrah on 7/17/2017.
 */

public class FirebaseMessagingServiceClass extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notiTitle = remoteMessage.getNotification().getTitle();
        String notiMessage = remoteMessage.getNotification().getBody();
        String noticlick = remoteMessage.getNotification().getClickAction();
        String userid = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notiTitle)
                .setContentText(notiMessage);

        int notiId = (int) System.currentTimeMillis();

        Intent resultIntent = new Intent(noticlick);
        resultIntent.putExtra("userid",userid);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(notiId,mBuilder.build());


    }
}
