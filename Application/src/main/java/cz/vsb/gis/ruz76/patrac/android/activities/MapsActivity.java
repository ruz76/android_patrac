package cz.vsb.gis.ruz76.patrac.android.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

import cz.vsb.gis.ruz76.patrac.android.domain.RequestMode;
import cz.vsb.gis.ruz76.patrac.android.domain.Status;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequestUpdate;
import cz.vsb.gis.ruz76.patrac.android.R;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequest;
import cz.vsb.gis.ruz76.patrac.android.helpers.Network;
import cz.vsb.gis.ruz76.patrac.android.listeners.GpxListener;

/**
 * Class for downloading GPX tracks.
 */
public class MapsActivity extends Activity implements LocationListener, GetRequestUpdate {

    private List<String> gpx_list;
    private List<String> gpx_list_files;
    private ArrayAdapter<String> arrayAdapter;
    private ListView gpxListView;
    private TextView mTextStatus;
    private MapView map = null;
    private LocationManager locationManager;
    private Context context;
    private Marker startMarker;
    private List<Overlay> overlays;
    private boolean readingLocations = false;

    private Timer timerRefreshPositions;
    MapsActivity.RefreshPositions myRefreshPositionsTask;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle(getString(R.string.tracks));

        mTextStatus = (TextView) findViewById(R.id.textViewStatus);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setUpMapView();

        String[] gpxs = new String[] { getString(R.string.local_track), getString(R.string.searchers_positions), getString(R.string.searchers_tracks) };
        gpx_list = new ArrayList<String>(Arrays.asList(gpxs));
        gpx_list_files = new ArrayList<String>();
        gpx_list_files.add("local.gpx");
        gpx_list_files.add("server_last.gpx");
        gpx_list_files.add("server.gpx");

        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, gpx_list);

        gpxListView = (ListView) findViewById(R.id.gpxListView);
        gpxListView.setAdapter(arrayAdapter);

        setupActionBar();

        GpxListener gpxListener = new GpxListener();
        gpxListener.setMapsActivity(this);
        gpxListener.setmTextStatus(mTextStatus);
        gpxListView.setOnItemClickListener(gpxListener);

        setRefreshPositionsTimer();
    }

    @Override
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * Timer for call on duty state.
     */
    private void setRefreshPositionsTimer() {
        if (timerRefreshPositions != null) {
            timerRefreshPositions.cancel();
        }

        timerRefreshPositions = new Timer();
        myRefreshPositionsTask = new MapsActivity.RefreshPositions();
        timerRefreshPositions.schedule(myRefreshPositionsTask, 0, 1000 * 10);
    }

    @SuppressLint("MissingPermission")
    private void setUpMapView() {

        context = this.getApplicationContext();
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double newlat = 49.8;
        double newlon = 18.1;
        if (locationGPS != null) {
            newlat = locationGPS.getLatitude();
            newlon = locationGPS.getLongitude();
        }

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(newlat, newlon);
        mapController.setCenter(startPoint);
        mapController.setZoom(12d);

        startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(context.getResources().getDrawable(R.drawable.ic_my_location_black_24dp));
        startMarker.setTitle(getString(R.string.this_device));
        map.getOverlays().add(startMarker);
        //getLocations();

        //startMarker.setIcon(context.getResources().getDrawable(R.drawable.ic_info_black_24dp));
        //mapController.zoomTo(9, 1000L);
    }

    private void getLocations() {
        GetRequest getRequest = new GetRequest();
        getRequest.setActivity(this);
        getRequest.setTextStatus(mTextStatus);
        getRequest.execute(Status.endPoint + "operation=getlocations&searchid=" + Status.searchid);
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        startMarker.setPosition(startPoint);
        //map.getController().setCenter(startPoint);
        map.invalidate();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void processResponse(String result) {
        readingLocations = false;
        if (result != null) {
            if (overlays != null) {
                map.getOverlays().removeAll(overlays);
            }
            String[] searchers = result.split("\n");
            overlays = new ArrayList<>();
            for (int i = 0; i < searchers.length; i++) {
                String[] items = searchers[i].split(";");
                if (items.length > 5) {
                    Marker itemMarker = new Marker(map);
                    double lon = Double.parseDouble(items[4].split(" ")[0]);
                    double lat = Double.parseDouble(items[4].split(" ")[1]);
                    GeoPoint startPoint = new GeoPoint(lat, lon);
                    itemMarker.setPosition(startPoint);
                    itemMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    // TODO set color according to state of the object
                    itemMarker.setIcon(context.getResources().getDrawable(R.drawable.ic_my_location_green_24dp));
                    itemMarker.setTitle(items[3] + "\n" + items[0]);
                    overlays.add(itemMarker);
                    map.getOverlays().add(itemMarker);
                }
            }
            map.invalidate();
        }
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
                    if (!readingLocations && Network.getInstance().isNetworkAvailable()) {
                        readingLocations = true;
                        getLocations();
                    }
                }
            });
        }

    }

}
