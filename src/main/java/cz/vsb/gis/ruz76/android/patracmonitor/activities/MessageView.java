package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import cz.msebera.android.httpclient.Header;
import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.dao.StorageDao;
import cz.vsb.gis.ruz76.android.patracmonitor.domain.Message;

public class MessageView extends AppCompatActivity {

    private Message message;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = getIntent().getExtras();
        int messageid = bundle.getInt("messageid");
        message = StorageDao.getMessage(getApplicationContext(), messageid);

        TextView textViewFromId = findViewById(R.id.textViewFromId);
        textViewFromId.setText(message.getFromId());

        TextView textViewMessage = findViewById(R.id.textViewMessage);
        textViewMessage.setText(message.getMessage());

        TextView textViewDtCreated = findViewById(R.id.textViewDtCreated);
        textViewDtCreated.setText(message.getDt_created());

        Button buttonShowAttachment = findViewById(R.id.buttonShowAttachment);

        if (message.getFile().length() > 1) {
            buttonShowAttachment.setEnabled(true);
        } else {
            buttonShowAttachment.setEnabled(false);
        }

        buttonShowAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileExists()) {
                   showFile();
                } else {
                   downloadFile();
                }
            }
        });

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private String getType(String filename) {
        String parts[] = filename.split("\\.");
        String extension = parts[parts.length - 1];
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
                return "image/jpeg";
            case "xml":
                return "text/xml";
            case "gpx":
                return "application/gpx+xml";
            default:
                return "image/png";
        }
    }

    private boolean fileExists() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
        path = path + "/" + message.getFile();
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void showFile() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
        path = path + "/" + message.getFile();
        String type = getType(message.getFile());
        Intent i = new Intent();
        i.setAction(android.content.Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setDataAndType(Uri.parse("file://"+path), type);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException exception) {
            Toast toast = Toast.makeText(MessageView.this, R.string.can_not_open_activity + " " + type, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void downloadFile() {
        String url = "http://gisak.vsb.cz/patrac/message.php?operation=getfile&searchid="
                + message.getSearchid()
                + "&filename=" + message.getFile();
        if (message.getShared() == 0) {
            url += "&id=" + sharedPrefs.getString("sessionid", "");
        } else {
            url += "&id=shared";
        }

        Log.d("MessageView", "URl: " + url);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
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
                Toast toast = Toast.makeText(MessageView.this, R.string.can_not_connect_to_server, Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    private void processResponse(byte[] response) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
        OutputStream output = null;
        try {
            output = new FileOutputStream(path + "/" + message.getFile());
            output.write(response);
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showFile();
    }
}
