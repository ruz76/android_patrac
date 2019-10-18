package cz.vsb.gis.ruz76.android.patracmonitor.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.vsb.gis.ruz76.android.patracmonitor.activities.MainActivity;
import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.dao.StorageDao;
import cz.vsb.gis.ruz76.android.patracmonitor.utils.Sound;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    private SharedPreferences sharedPrefs;
    public static int TRACKING_SERVICE_ID = 535468971;
    Notification.Builder builder;
    PendingIntent broadcastIntent;
    public static Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        startIt();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startIt();
    }

    private void startIt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(TRACKING_SERVICE_ID, getNotification());
            } else {
                buildNotification();
                startForeground(TRACKING_SERVICE_ID, builder.build());
            }
        } else {
            startService(new Intent(this, TrackingService.class));
        }
        //handler.post(runnableCode);
        requestLocationUpdates();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackingService");
        if ((wakeLock != null) &&           // we have a WakeLock
                (wakeLock.isHeld() == false)) {
            wakeLock.acquire();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//

        request.setInterval(10000);

        //Get the most accurate location data available//
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        StorageDao.saveLocationToDB(location, getApplicationContext());
                        int lastLocationId = sharedPrefs.getInt("lastLocationId", 0);
                        Log.d("lastLocationId", String.valueOf(lastLocationId));
                        String locations = StorageDao.getLocations(getApplicationContext(), lastLocationId);
                        String userName = sharedPrefs.getString("userName", "NN");
                        String userId = sharedPrefs.getString("userId", "");
                        String status = sharedPrefs.getString("status", "waiting");
                        String searchId = sharedPrefs.getString("searchId", "");
                        String sessionId = sharedPrefs.getString("sessionid", "");
                        int lastReceivedMessageId = sharedPrefs.getInt("lastReceivedMessageId", 0);
                        String payload = "{";
                        payload += "\"username\":" + "\"" + userName + "\",";
                        payload += "\"userid\":" + "\"" + userId + "\",";
                        payload += "\"sessionid\":" + "\"" + sessionId + "\",";
                        payload += "\"status\":" + "\"" + status + "\",";
                        payload += "\"lastreceivedmessageid\":" + "\"" + lastReceivedMessageId + "\",";
                        if (sharedPrefs.getInt("arriveatchanged", 0) == 1) {
                            String arriveat = sharedPrefs.getString("arriveat", "Neuvedeno");
                            payload += "\"arriveat\":" + "\"" + arriveat + "\",";
                        }
                        if (sharedPrefs.getInt("firebaseidchanged", 0) == 1) {
                            String firebaseid = sharedPrefs.getString("firebaseid", "Neuvedeno");
                            payload += "\"firebaseid\":" + "\"" + firebaseid + "\",";
                        }
                        payload += "\"searchid\":" + "\"" + searchId + "\",";
                        payload += "\"locations\":" + locations;
                        payload += "}";
                        sendPost(payload);
                        Log.d("Handlers Locations", payload);
                    }
                }
            }, null);
        }
    }

    private void buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);


        // Create the persistent notification//
        builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))

                //Make this notification ongoing so it can’t be dismissed by the user//

                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.tracking_enabled);
    }


    private Notification getNotification() {

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("tracking_channel_01", "Tracking Channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), "tracking_channel_01").setAutoCancel(true);
            return builder.build();
        }

        return null;
    }

    private void createNotificationOnDemand(String text, int messageid) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, "tracking_channel_01")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.patrac))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        // notificationId est un identificateur unique par notification qu'il vous faut définir
        notificationManager.notify(messageid, notifBuilder.build());
        //} else {

        //}
    }

    private void createCallOnDutyNotification() {
        createNotificationOnDemand(getString(R.string.callonduty), 1);
    }

    private void createCallToComeNotification() {
        createNotificationOnDemand(getString(R.string.calltocome), 2);
    }

    private void createNewMessageOnServer() {
        createNotificationOnDemand(getString(R.string.newmessageonserver), 3);
    }

    private void processOtherStatusResponse(String status) {
        Log.i("RESPONSE STATUS OTHER", status);
    }

    private void processCallOnDutyResponse() {
        String status = sharedPrefs.getString("status", "waiting");
        if (status.equalsIgnoreCase("waiting")) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("status", "callonduty");
            editor.commit();
        }
        if (status.equalsIgnoreCase("callonduty")) {
            createCallOnDutyNotification();
        }
    }

    private void processCallToComeResponse() {
        String status = sharedPrefs.getString("status", "waiting");
        if (status.equalsIgnoreCase("readytogo")) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("status", "calltocome");
            editor.commit();
        }
        if (status.equalsIgnoreCase("calltocome")) {
            createCallToComeNotification();
        }
    }

    private void processWaitingResponse() {
        String status = sharedPrefs.getString("status", "waiting");
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (!status.equalsIgnoreCase("waiting")) {
            editor.putString("searchName", "Neuvedeno");
            editor.putString("searchDesc", "Neuvedeno");
            editor.putString("searchId", "");
        }
        editor.putString("status", "waiting");
        editor.commit();
    }

    private void processResponse(byte[] responseBody) {
        Log.i("RESPONSE", new String(responseBody));
        try {
            JSONObject jsonObj = new JSONObject(new String(responseBody));
            int lastLocationId = jsonObj.getInt("lastlocationid");
            //Log.d("lastLocationId", String.valueOf(lastLocationId));
            String searchName = jsonObj.getString("searchname");
            String searchDesc = jsonObj.getString("searchdesc");
            String searchId = jsonObj.getString("searchid");
            int firebaseIdChanged = jsonObj.optInt("firebaseidchanged", 0);
            int countOfNewMessages = jsonObj.optInt("countofnewmessages", 0);
            String sessionid = jsonObj.optString("sessionid", "NN");
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("lastLocationId", lastLocationId);
            editor.putString("searchName", searchName);
            editor.putString("searchDesc", searchDesc);
            editor.putString("searchId", searchId);
            if (firebaseIdChanged == 1) {
                editor.putInt("firebaseidchanged", 0);
                editor.putString("sessionid", sessionid);
            }
            editor.commit();

            String statusFromServer = jsonObj.getString("status");
            switch (statusFromServer) {
                case "waiting":
                    processWaitingResponse();
                    break;
                case "callonduty":
                    Sound.playRing(context, 1000);
                    processCallOnDutyResponse();
                    break;
                case "calltocome":
                    Sound.playRing(context, 1000);
                    processCallToComeResponse();
                    break;
                default:
                    if (countOfNewMessages > 0) {
                        Sound.playRing(context, 500);
                        createNewMessageOnServer();
                    }
                    processOtherStatusResponse(statusFromServer);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendPost(String content) {
        RequestParams params = new RequestParams();
        params.put("version", "json1.0");
        params.put("content", content);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://gisak.vsb.cz/patrac/sync.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                processResponse(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                if (bytes != null) {
                    Log.e("RESPONSE FAILURE", statusCode + ": " + new String(bytes));
                } else {
                    Log.e("RESPONSE FAILURE", "null");
                }
            }
        });
    }
}