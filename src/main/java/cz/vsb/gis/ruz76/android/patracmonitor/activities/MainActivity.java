package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.services.TrackingService;

public class MainActivity extends AppCompatActivity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final int PERMISSIONS_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String status = sharedPrefs.getString("status", "");

        switch (status) {
            case "callonduty":
                Log.d("MainActivity", "onCreate callonduty");
                startIt();
                startCallOnDutyActivity();
                finish();
                break;
            case "readytogo":
                Log.d("MainActivity", "onCreate readytogo");
                startIt();
                finish();
                break;
            case "calltocome":
                Log.d("MainActivity", "onCreate calltocome");
                startIt();
                startCallToComeActivity();
                finish();
                break;
            case "onduty":
                Log.d("MainActivity", "onCreate onduty");
                startIt();
                startOnDutyActivity();
                finish();
                break;
            default:
                Log.d("MainActivity", "onCreate other status");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setValues();

        Button buttonArrive = findViewById(R.id.buttonConnect);
        buttonArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  if (checkSettings()) {
                      connect();
                  } else {
                      Toast toast = Toast.makeText(MainActivity.this, R.string.not_full_input, Toast.LENGTH_LONG);
                      toast.show();
                  }
            }
        });

        Button buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR();
            }
        });
    }

    private void scanQR() {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(this, getString(R.string.pref_scanner_not_found), getString(R.string.pref_scanner_not_found_download), getString(R.string.pref_scanner_not_found_download_yes), getString(R.string.pref_scanner_not_found_download_no)).show();
        }
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, R.string.pref_scanner_identifier + ":" + contents + " " + R.string.pref_scanner_format + ":" + format, Toast.LENGTH_LONG);
                toast.show();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("searchId", contents);
                editor.commit();

                ((EditText) findViewById(R.id.editTextIdSearch)).setText(contents);
            }
        }
    }

    private void startCallOnDutyActivity() {
        Intent callOnDuty = new Intent(MainActivity.this, CallOnDuty.class);
        startActivity(callOnDuty);
    }

    private void startCallToComeActivity() {
        Intent callToCome = new Intent(MainActivity.this, CallToCome.class);
        startActivity(callToCome);
    }

    private void startOnDutyActivity() {
        Intent onDuty = new Intent(MainActivity.this, OnDuty.class);
        startActivity(onDuty);
    }

    private void setValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = sharedPrefs.getString("userName", "");
        String userId = sharedPrefs.getString("userId", "");
        String searchId = sharedPrefs.getString("searchId", "");
        ((EditText) findViewById(R.id.editTextName)).setText(userName);
        ((EditText) findViewById(R.id.editTextId)).setText(userId);
        ((EditText) findViewById(R.id.editTextIdSearch)).setText(searchId);
    }

    private boolean checkSettings() {
        String userName = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        String userId = ((EditText) findViewById(R.id.editTextId)).getText().toString();
        String searchId = ((EditText) findViewById(R.id.editTextIdSearch)).getText().toString();
        if (!userName.isEmpty()) {
            if (!userId.isEmpty() || !searchId.isEmpty()) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("userName", userName);
                editor.putString("userId", userId);
                editor.putString("searchId", searchId);
                if (!searchId.isEmpty()) {
                    editor.putString("status", "onduty");
                }
                editor.commit();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void connect() {
        startIt();
    }

    private void connectToFirebase() {

    }

    private void startIt() {
        //Check whether GPS tracking is enabled//

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }

//Check whether this app has access to the location permission//

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

//If the location permission has been granted, then start the TrackerService//

        if (permission == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED && permission3 == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {

//If the app doesn’t currently have access to the user’s location, then request access//

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

//If the permission has been granted...//

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 3
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
        ) {

//...then start the GPS tracking service//

            startTrackerService();
        } else {

//If the user denies the permission request, then display a toast with some more information//

            Toast.makeText(this,  R.string.enable_gps, Toast.LENGTH_SHORT).show();
        }
    }

//Start the TrackerService//

    private void startTrackerService() {
        Log.d("MainActivity", "startTrackerService");
        startService(new Intent(this, TrackingService.class));

        //Notify the user that tracking has been enabled//

        Toast.makeText(this, R.string.gps_enabled, Toast.LENGTH_SHORT).show();

        //Close MainActivity//

        finish();
    }

}
