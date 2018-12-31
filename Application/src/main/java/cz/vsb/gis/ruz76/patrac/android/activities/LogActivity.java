package cz.vsb.gis.ruz76.patrac.android.activities;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.vsb.gis.ruz76.patrac.android.R;
import cz.vsb.gis.ruz76.patrac.android.helpers.LogHelper;

public class LogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        showLog();
    }

    private void showLog() {
        TextView mLogText = (TextView) findViewById(R.id.textViewLog);
        mLogText.setMovementMethod(new ScrollingMovementMethod());
        mLogText.setText(LogHelper.getLog());
    }
}
