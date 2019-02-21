package cz.vsb.gis.ruz76.patrac.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;

import cz.vsb.gis.ruz76.patrac.android.activities.MainActivity;
import cz.vsb.gis.ruz76.patrac.android.domain.Status;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequest;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequestUpdate;
import cz.vsb.gis.ruz76.patrac.android.helpers.LogHelper;

/**
 * Created by jencek on 31.1.19.
 */

public class PositionReceiver extends BroadcastReceiver implements GetRequestUpdate {

    private String url = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        if (url == null) {
            url = Status.endPoint;
        }
        String response = null;
        LogHelper.i("PositionReceiver", String.valueOf(new Date()));
        GetIt getIt = new GetIt();
        if (url == null) {
            LogHelper.i("PositionReceiver", "URL is null");
            try {
                getIt.setUrl("http://gisak.vsb.cz/patrac/mserver.php?date=" + URLEncoder.encode(new Date().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                getIt.setUrl(url + "date=" + URLEncoder.encode(new Date().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        getIt.run();
        while(getIt.getResult() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogHelper.i("PositionReceiverResponse", getIt.getResult());
    }

    @Override
    public void processResponse(String result) {
        LogHelper.i("PositionReceiverResponse", result);
    }

    private class GetIt implements Runnable {

        private String urlString;
        private String result = null;

        public void setUrl(String url) {
            this.urlString = url;
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            int count;
            try {
                URL url = new URL(urlString);
                URLConnection conection = url.openConnection();
                conection.connect();
                InputStream input = new BufferedInputStream(url.openStream(),8192);
                StringBuilder stringBuilder = new StringBuilder();
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    String currentBufferString = new String(data, 0, count);
                    stringBuilder.append(currentBufferString);
                }
                input.close();
                result = stringBuilder.toString();

            } catch (Exception e) {
                //TODO show error cause exception from MapsActivity
            /*if (textStatus != null) {
                textStatus.setText(R.string.download_error);
            }*/
                cz.vsb.gis.ruz76.patrac.android.domain.Status.StatusMessages = e.getMessage();
                LogHelper.e("GetRequest: ", urlString + " " + e.getMessage());
                MainActivity.processingRequest = false;
                //cancel(true);
                e.printStackTrace();
                result = "ERROR";
            }
        }
    }
}
