package cz.vsb.gis.ruz76.patrac.android.helpers;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jencek on 21.12.18.
 */

public class Network {

    private static Network mInstance;
    private volatile boolean mIsOnline = true;

    private Network() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean reachable = false;
                try {
                    Process process = java.lang.Runtime.getRuntime().exec("ping -c 1 8.8.8.8");
                    int returnVal = process.waitFor();
                    reachable = (returnVal==0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mIsOnline = reachable;
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static Network getInstance() {
        if (mInstance == null) {
            synchronized (Network.class) {
                if (mInstance == null) {
                    mInstance = new Network();
                }
            }
        }
        return mInstance;
    }

    public boolean isNetworkAvailable() {
        return mIsOnline;
    }

    /*
     public void run() {
        Runtime runtime = Runtime.getRuntime();
        while (true) {
            try {
                // ping the network
                Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int mExitValue = mIpAddrProcess.waitFor();
                if (mExitValue == 0) {
                    isNetworkAvailable = true;
                } else {
                    isNetworkAvailable = false;
                }
                // delay 5 seconds
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
