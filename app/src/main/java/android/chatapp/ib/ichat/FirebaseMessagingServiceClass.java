package android.chatapp.ib.ichat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ibrah on 7/17/2017.
 */

public class FirebaseMessagingServiceClass extends FirebaseMessagingService {


    Intent resultIntent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notiTitle = remoteMessage.getNotification().getTitle();
        String notiMessage = remoteMessage.getNotification().getBody();
        String noticlick = remoteMessage.getNotification().getClickAction();
        String userid = remoteMessage.getData().get("from_user_id");
        String username = remoteMessage.getData().get("from_username");
        String mom_by_id = remoteMessage.getData().get("name");
        String moment_id = remoteMessage.getData().get("moment_id");



        int notiId = (int) System.currentTimeMillis();

        resultIntent = new Intent(noticlick);
        resultIntent.putExtra("from_user_id",userid);
        resultIntent.putExtra("username",username);
        resultIntent.putExtra("name",mom_by_id);
        resultIntent.putExtra("moment_id",moment_id);


        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle(notiTitle)
                .setContentText(notiMessage)
                .setSound(alarmSound);
        mBuilder.setContentIntent(resultPendingIntent);

        //mBuilder.addAction(R.mipmap.ic_launcher,"View message",resultPendingIntent);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(notiId,mBuilder.build());


    }
}
