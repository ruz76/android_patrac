package cz.vsb.gis.ruz76.android.patracmonitor.services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.activities.MainActivity;
import cz.vsb.gis.ruz76.android.patracmonitor.utils.Sound;

public class BasicFirebaseMessagingService extends FirebaseMessagingService {
    public BasicFirebaseMessagingService() {

    }

    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "XXX From: " + remoteMessage.getFrom());
        Log.d(TAG, "XXX Notification Message Body: " + remoteMessage.getNotification().getBody());

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, "tracking_channel_01")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(remoteMessage.getFrom())
                .setContentText(remoteMessage.getNotification().getBody())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        // notificationId est un identificateur unique par notification qu'il vous faut définir
        notificationManager.notify(2, notifBuilder.build());

        Sound.playRing(getApplicationContext(), 1000);

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e(TAG, "XXX NEW_TOKEN " + s);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("firebaseid", s);
        editor.putInt("firebaseidchanged", 1);
        editor.commit();
    }
}
