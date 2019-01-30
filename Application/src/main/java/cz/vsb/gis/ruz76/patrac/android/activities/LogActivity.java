package cz.vsb.gis.ruz76.patrac.android.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.vsb.gis.ruz76.patrac.android.R;
import cz.vsb.gis.ruz76.patrac.android.helpers.LogHelper;
import cz.vsb.gis.ruz76.patrac.android.helpers.Network;

public class LogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setupActionBar();
        showLog();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_log_action:
                sendLog();
                return true;
            case R.id.save_log_action:
                saveLog();
                return true;
        }
        return false;
    }

    private void showLog() {
        TextView mLogText = (TextView) findViewById(R.id.textViewLog);
        mLogText.setMovementMethod(new ScrollingMovementMethod());
        mLogText.setText(LogHelper.getLog());
    }

    private void saveLog() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD_HH-mm-ss");
        String fileName = "patracLog" + simpleDateFormat.format(new Date()) + ".log";
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
        try {
            PrintWriter output = new PrintWriter(new FileOutputStream(path + "/" + fileName));
            output.print(LogHelper.getLog());
            output.flush();
            output.close();
            Toast toast = Toast.makeText(LogActivity.this, getString(R.string.log_saved) + " " + path + "/" + fileName, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            Toast toast = Toast.makeText(LogActivity.this, getString(R.string.log_not_saved), Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }
    }

    private void sendLog() {
        Toast toast = Toast.makeText(LogActivity.this, getString(R.string.log_not_saved), Toast.LENGTH_LONG);
        toast.show();
    }
}
