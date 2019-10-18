package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import cz.vsb.gis.ruz76.android.patracmonitor.R;

public class Map extends AppCompatActivity {

    private MapView map = null;
    private List<Overlay> overlays;
    private Timer timerRefreshPositions;
    Map.RefreshPositions myRefreshPositionsTask;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue("github-firefishy-map/0.1");
        setContentView(R.layout.activity_map);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setUpMapView();
        getLocations();
        setRefreshPositionsTimer();
    }

    @Override
    public void onStart() {
        super.onStart();
        setRefreshPositionsTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        setRefreshPositionsTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
        timerRefreshPositions.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        timerRefreshPositions.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerRefreshPositions.cancel();
    }

    private void setUpMapView() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(12d);
    }

    /**
     * Timer for call on duty state.
     */
    private void setRefreshPositionsTimer() {
        if (timerRefreshPositions != null) {
            timerRefreshPositions.cancel();
        }

        timerRefreshPositions = new Timer();
        myRefreshPositionsTask = new Map.RefreshPositions();
        timerRefreshPositions.schedule(myRefreshPositionsTask, 0, 1000 * 10);
    }

    private void getLocations() {
        String searchId = sharedPrefs.getString("searchId", "");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://gisak.vsb.cz/patrac/loc.php?searchid=" + searchId, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                processResponse(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                if (bytes != null) {
                    Log.e("RESPONSE FAILURE", statusCode + ": " + new String(bytes));
                } else {
                    Log.e("RESPONSE FAILURE", "null");
                }
                Toast toast = Toast.makeText(Map.this, R.string.can_not_download_positions, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void processResponse(String result) {
        try {
            if (result != null) {
                if (overlays != null) {
                    map.getOverlays().removeAll(overlays);
                }
                String[] searchers = result.split("\n");
                overlays = new ArrayList<>();
                for (int i = 1; i < searchers.length; i++) {
                    String[] items = searchers[i].split(";");
                    if (items.length > 5) {
                        Marker itemMarker = new Marker(map);
                        double lon = Double.parseDouble(items[4].split(" ")[0]);
                        double lat = Double.parseDouble(items[4].split(" ")[1]);
                        GeoPoint startPoint = new GeoPoint(lat, lon);
                        itemMarker.setPosition(startPoint);
                        itemMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        // TODO set color according to state of the object
                        itemMarker.setIcon(getDrawable(R.drawable.ic_my_location_green_24dp));
                        String time = (items[1].length() > 19) ? items[1].substring(0, 19) : items[1];
                        itemMarker.setTitle(items[3] + "\n" + time);
                        overlays.add(itemMarker);
                        map.getOverlays().add(itemMarker);
                    }
                }
                CheckBox checkBoxAutoZoom = findViewById(R.id.checkBoxAutoZoom);
                if (checkBoxAutoZoom.isChecked()) {
                    IMapController mapController = map.getController();
                    String[] items = searchers[0].split(";");
                    GeoPoint moveTo = new GeoPoint(Double.valueOf(items[5]), Double.valueOf(items[4]));
                    mapController.setCenter(moveTo);
                    mapController.setZoom(getZoomLevel(items));
                }
                map.invalidate();
            }
        } catch (NullPointerException exception) {
            // The map view was killed. No problem, but we need to catch the exception.
        }
    }


    /**
     * Taken from https://stackoverflow.com/questions/6048975/google-maps-v3-how-to-calculate-the-zoom-level-for-a-given-bounds
     *
     * @param items
     * @return
     */
    private Double getZoomLevel(String[] items) {

        Double west = Double.valueOf(items[0]);
        Double east = Double.valueOf(items[2]);
        Double angle = east - west;

        if (angle < 0.00001) {
            return 15d;
        }

        Double north = Double.valueOf(items[3]);
        Double south = Double.valueOf(items[1]);
        Double angle2 = north - south;
        Double delta = 0d;

        if (angle2 > angle) {
            angle = angle2;
            delta = 3d;
        }

        if (angle < 0) {
            angle += 360;
        }

        return Math.floor(Math.log(960 * 360 / angle / 256) / 0.693) - 2 - delta;
    }

    /**
     * Timer task for synchronization of messages.
     */
    class RefreshPositions extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    getLocations();
                }
            });
        }

    }

}
