package cz.vsb.gis.ruz76.patrac.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

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
        LogHelper.i("PositionReceiver", String.valueOf(new Date()));
        GetRequest getRequest = new GetRequest();
        getRequest.setActivity(this);
        if (url == null) {
            LogHelper.i("PositionReceiver", "URL is null");
            getRequest.execute("http://gisak.vsb.cz/patrac/mserver.php?date=" + new Date());
        } else {
            getRequest.execute(url + "date=" + new Date());
        }
    }

    @Override
    public void processResponse(String result) {
        LogHelper.i("PositionReceiverResponse", result);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
