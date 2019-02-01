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

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        LogHelper.i("PositionReceiver", String.valueOf(new Date()));
        GetRequest getRequest = new GetRequest();
        getRequest.setActivity(this);
        getRequest.execute(Status.endPoint + "date=" + new Date());
    }

    @Override
    public void processResponse(String result) {

    }
}
